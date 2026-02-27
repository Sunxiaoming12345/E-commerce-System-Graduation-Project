<template>
  <div class="orders">
    <h2>我的订单</h2>
    <el-loading v-loading="loading" element-loading-text="加载中...">
      <div v-if="orders.length > 0" class="orders-content">
        <el-table :data="orders" style="width: 100%">
          <el-table-column label="订单号" prop="orderNumber" width="180" />
          <el-table-column label="下单时间" prop="createTime" width="180" />
          <el-table-column label="商品数量" width="120">
            <template #default="scope">
              <span>1</span> <!-- 暂时显示1，后续可以从订单详情中获取 -->
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
import { getOrders, cancelOrder, payOrder } from '@/api/orders'
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
    orders.value = res.records
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
      return '已支付'
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