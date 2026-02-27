<template>
  <div class="order-detail">
    <h2>订单详情</h2>
    <el-loading v-loading="loading" element-loading-text="加载中...">
      <div v-if="order" class="order-content">
        <!-- 订单基本信息 -->
        <div class="section">
          <h3>订单信息</h3>
          <div class="order-info">
            <div class="info-item">
              <span class="label">订单号:</span>
              <span class="value">{{ order.orderNumber }}</span>
            </div>
            <div class="info-item">
              <span class="label">订单状态:</span>
              <el-tag :type="getStatusType(order.orderStatus)">{{ getStatusText(order.orderStatus) }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">下单时间:</span>
              <span class="value">{{ order.createTime }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付方式:</span>
              <span class="value">{{ getPaymentMethodText(order.paymentMethod) }}</span>
            </div>
          </div>
        </div>

        <!-- 收货信息 -->
        <div class="section">
          <h3>收货信息</h3>
          <div class="shipping-info">
            <div class="info-item">
              <span class="label">收货人:</span>
              <span class="value">{{ order.receiverName }}</span>
            </div>
            <div class="info-item">
              <span class="label">联系电话:</span>
              <span class="value">{{ order.receiverPhone }}</span>
            </div>
            <div class="info-item">
              <span class="label">收货地址:</span>
              <span class="value">{{ order.shippingAddress }}</span>
            </div>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="section">
          <h3>商品信息</h3>
          <div class="product-info">
            <!-- 暂时显示商品数量，后续可以从订单详情中获取具体商品信息 -->
            <div class="info-item">
              <span class="label">商品数量:</span>
              <span class="value">1</span>
            </div>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="section" v-if="order.orderStatus === 0">
          <h3>支付方式</h3>
          <div class="payment-method">
            <el-radio-group v-model="paymentMethod">
              <el-radio label="0">支付宝</el-radio>
              <el-radio label="1">微信</el-radio>
              <el-radio label="2">余额</el-radio>
            </el-radio-group>
          </div>
        </div>

        <!-- 订单金额 -->
        <div class="section">
          <h3>订单金额</h3>
          <div class="amount-info">
            <div class="info-item">
              <span class="label">订单总金额:</span>
              <span class="value price">¥{{ order.totalAmount.toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="order-actions">
          <el-button @click="goBack">返回订单列表</el-button>
          <el-button v-if="order.orderStatus === 0" type="success" @click="handlePayOrder(order.orderId, paymentMethod)">支付</el-button>
          <el-button v-if="order.orderStatus === 0" type="danger" @click="handleCancelOrder(order.orderId)">取消</el-button>
        </div>
      </div>
      <div v-else class="empty">
        <el-empty description="订单不存在" />
      </div>
    </el-loading>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, payOrder, cancelOrder } from '@/api/orders'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const orderId = route.params.id
const order = ref(null)
const loading = ref(true)
const paymentMethod = ref('0') // 默认支付宝

const loadOrderDetail = async () => {
  try {
    loading.value = true
    const res = await getOrderDetail(orderId)
    order.value = res
  } catch (error) {
    console.error('Failed to load order detail:', error)
    ElMessage.error('加载订单详情失败')
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

const getPaymentMethodText = (method) => {
  switch (method) {
    case 0:
      return '支付宝'
    case 1:
      return '微信'
    case 2:
      return '余额'
    default:
      return '其他'
  }
}

const handlePayOrder = async (id, method) => {
  try {
    await payOrder({ 
      orderId: id, 
      paymentMethod: parseInt(method) 
    })
    ElMessage.success('支付成功')
    loadOrderDetail()
  } catch (error) {
    ElMessage.error('支付失败')
  }
}

const handleCancelOrder = async (id) => {
  try {
    await cancelOrder(id)
    ElMessage.success('订单已取消')
    loadOrderDetail()
  } catch (error) {
    ElMessage.error('取消订单失败')
  }
}

const goBack = () => {
  router.push('/orders')
}

onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
.order-detail {
  padding: 20px 0;
}

.order-detail h2 {
  margin-bottom: 30px;
  color: #333;
}

.order-content {
  max-width: 800px;
  margin: 0 auto;
}

.section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #eaeaea;
  border-radius: 8px;
}

.section h3 {
  margin-bottom: 20px;
  font-size: 16px;
  color: #333;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.info-item {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.label {
  width: 100px;
  font-size: 14px;
  color: #666;
}

.value {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.value.price {
  color: #ff4d4f;
  font-weight: 500;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 40px;
}

.empty {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>