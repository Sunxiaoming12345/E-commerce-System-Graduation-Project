<template>
  <div class="orders">
    <h2>我的订单</h2>
    <el-loading v-loading="loading" element-loading-text="加载中...">
      <div v-if="orders.length > 0" class="orders-content">
        <el-table :data="orders" style="width: 100%">
          <el-table-column label="订单号" prop="orderNumber" width="180" />
          <el-table-column label="下单时间" prop="createTime" width="180" />
          <el-table-column label="商品名称" min-width="200">
            <template #default="scope">
              <span v-if="scope.row.orderItems && scope.row.orderItems.length > 0">
                {{ scope.row.orderItems[0].productName }}
                <span v-if="scope.row.orderItems.length > 1">等{{ scope.row.orderItems.length }}件商品</span>
              </span>
              <span v-else>暂无商品</span>
            </template>
          </el-table-column>
          <el-table-column label="商品数量" width="120">
            <template #default="scope">
              <span>{{ scope.row.orderItems ? scope.row.orderItems.reduce((sum, item) => sum + item.quantity, 0) : 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="订单金额" width="120">
            <template #default="scope">
              <span class="order-price">¥{{ scope.row.totalAmount.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="订单状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.orderStatus)">{{ getStatusText(scope.row.orderStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewOrderDetail(scope.row.orderId)">查看详情</el-button>
               <el-button v-if="scope.row.orderStatus === 0" type="success" size="small" @click="handlePayOrder(scope.row.orderId)">支付</el-button>
              <el-button v-if="scope.row.orderStatus === 0" type="danger" size="small" @click="handleCancelOrder(scope.row.orderId)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 30, 40]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </div>
      <div v-else class="empty">
        <el-empty description="暂无订单" />
      </div>
    </el-loading>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrders, cancelOrder, payOrder, getOrderDetail } from '@/api/orders'
import { ElMessage } from 'element-plus'

const router = useRouter()
const orders = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadOrders = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    const res = await getOrders(params)
    // 打印订单列表数据结构，用于调试
    console.log('Orders response:', res)
    console.log('First order:', res.records && res.records.length > 0 ? res.records[0] : 'No orders')
    
    // 为每个订单获取商品信息
    const ordersWithItems = await Promise.all(
      res.records.map(async (order, index) => {
        try {
          console.log(`Processing order ${index}:`, order)
          
          // 打印订单对象的所有属性，查看实际的字段名
          console.log(`Order ${index} keys:`, Object.keys(order))
          
          // 尝试所有可能的订单ID字段名
          let orderId = null
          const possibleIdFields = ['orderId', 'id', 'OrderId', 'ORDER_ID', 'order_id']
          for (const field of possibleIdFields) {
            if (order[field]) {
              orderId = order[field]
              console.log(`Found orderId in field '${field}':`, orderId)
              break
            }
          }
          
          // 如果还是没有找到，尝试从订单号中提取
          if (!orderId && order.orderNumber) {
            console.log(`Trying to extract orderId from orderNumber:`, order.orderNumber)
            // 假设订单号的格式是：用户ID+时间戳，我们可以尝试解析
            try {
              // 尝试将订单号转换为数字
              const numOrderNumber = parseInt(order.orderNumber)
              if (!isNaN(numOrderNumber)) {
                orderId = numOrderNumber
                console.log(`Extracted orderId from orderNumber:`, orderId)
              }
            } catch (e) {
              console.error('Failed to parse orderNumber:', e)
            }
          }
          
          // 尝试从订单详情URL中提取订单ID
          // 例如：/order-detail/43
          if (!orderId) {
            console.log(`Trying to extract orderId from viewOrderDetail function`)
            // 查看viewOrderDetail函数是如何获取订单ID的
            console.log(`viewOrderDetail function:`, viewOrderDetail.toString())
          }
          
          if (orderId) {
            try {
              console.log(`Calling getOrderDetail with orderId:`, orderId)
              // 调用订单详情接口
              const orderDetail = await getOrderDetail(orderId)
              console.log(`Order detail for order ${orderId}:`, orderDetail)
              
              // 检查订单详情的结构
              if (orderDetail) {
                console.log(`Order detail structure:`, Object.keys(orderDetail))
                
                if (orderDetail.orderItems) {
                  console.log(`Order items for order ${orderId}:`, orderDetail.orderItems)
                  return {
                    ...order,
                    orderItems: orderDetail.orderItems
                  }
                } else if (orderDetail.order && orderDetail.order.orderItems) {
                  // 检查订单详情是否嵌套在order字段中
                  console.log(`Order items found in order.orderItems:`, orderDetail.order.orderItems)
                  return {
                    ...order,
                    orderItems: orderDetail.order.orderItems
                  }
                } else {
                  console.error(`Order detail has no orderItems for order ${orderId}:`, orderDetail)
                  // 如果订单详情没有商品信息，返回空数组
                  return {
                    ...order,
                    orderItems: []
                  }
                }
              } else {
                console.error(`Order detail is null for order ${orderId}`)
                return {
                  ...order,
                  orderItems: []
                }
              }
            } catch (error) {
              console.error(`Failed to get order detail for order ID ${orderId}:`, error)
              // 如果获取订单详情失败，返回空数组
              return {
                ...order,
                orderItems: []
              }
            }
          } else {
            console.error('No orderId found in order:', order)
            // 如果没有找到订单ID，返回空数组
            return {
              ...order,
              orderItems: []
            }
          }
        } catch (error) {
          console.error(`Failed to get order items for order:`, error)
          // 如果发生错误，返回空数组
          return {
            ...order,
            orderItems: []
          }
        }
      })
    )
    
    console.log('Orders with items:', ordersWithItems)
    orders.value = ordersWithItems
    total.value = res.total
  } catch (error) {
    console.error('Failed to load orders:', error)
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  switch (status) {
    case 0:
      return 'warning'
    case 1:
      return 'success'
    case 2:
      return 'info'
    case 3:
      return 'success'
    case 4:
      return 'danger'
    default:
      return ''
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0:
      return '待支付'
    case 1:
      return '已付款'
    case 2:
      return '已发货'
    case 3:
      return '已完成'
    case 4:
      return '已取消'
    default:
      return status
  }
}

const viewOrderDetail = (id) => {
  // 跳转到订单详情页面
  router.push(`/order-detail/${id}`)
}

const handlePayOrder = (id) => {
  // 跳转到订单详情页面，让用户选择支付方式
  router.push(`/order-detail/${id}`)
}

const handleCancelOrder = async (id) => {
  try {
    await cancelOrder(id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    ElMessage.error('取消订单失败')
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadOrders()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadOrders()
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders {
  padding: 20px 0;
}

.orders h2 {
  margin-bottom: 30px;
  color: #333;
}

.orders-content {
  border: 1px solid #eaeaea;
  border-radius: 8px;
  overflow: hidden;
}

.order-price {
  color: #ff4d4f;
  font-weight: 500;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px;
  background-color: #f9f9f9;
  border-top: 1px solid #eaeaea;
}

.empty {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>