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
              <span class="value">{{ order.order.orderNumber }}</span>
            </div>
            <div class="info-item">
              <span class="label">订单状态:</span>
              <el-tag :type="getStatusType(order.order.orderStatus)">{{ getStatusText(order.order.orderStatus) }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">下单时间:</span>
              <span class="value">{{ order.order.createTime }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付方式:</span>
              <span class="value">{{ getPaymentMethodText(order.order.paymentMethod) }}</span>
            </div>
          </div>
        </div>

        <!-- 收货信息 -->
        <div class="section">
          <h3>收货信息</h3>
          <div class="shipping-info">
            <div class="info-item">
              <span class="label">收货人:</span>
              <span class="value">{{ order.order.receiverName }}</span>
            </div>
            <div class="info-item">
              <span class="label">联系电话:</span>
              <span class="value">{{ order.order.receiverPhone }}</span>
            </div>
            <div class="info-item">
              <span class="label">收货地址:</span>
              <span class="value">{{ order.order.shippingAddress }}</span>
            </div>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="section">
          <h3>商品信息</h3>
          <div class="product-info">
            <div class="product-header">
              <div class="header-item product-column">商品</div>
              <div class="header-item price-column">单价</div>
              <div class="header-item quantity-column">数量</div>
              <div class="header-item subtotal-column">小计</div>
            </div>
            <div v-for="(item, index) in order.orderItems || []" :key="item.itemId" class="product-item" :class="{ 'even': index % 2 === 1 }">
              <div class="product-column">
                <img v-if="item.imageUrl" :src="item.imageUrl" style="width: 60px; height: 60px; object-fit: cover; margin-right: 10px;" />
                <span v-else style="margin-right: 10px; width: 60px; display: inline-block;">-</span>
                {{ item.productName }}
              </div>
              <div class="price-column">¥{{ item.productPrice.toFixed(2) }}</div>
              <div class="quantity-column">{{ item.quantity }}</div>
              <div class="subtotal-column">¥{{ item.subtotal.toFixed(2) }}</div>
            </div>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="section" v-if="order.order.orderStatus === 0">
          <h3>支付方式</h3>
          <div class="payment-method">
            <el-radio-group v-model="paymentMethod">
              <el-radio label="2">余额支付</el-radio>
              <el-radio label="0">支付宝</el-radio>
              <el-radio label="1">微信支付</el-radio>
            </el-radio-group>
          </div>
          <div class="countdown-section">
            <span class="countdown-label">支付剩余时间：</span>
            <span class="countdown-time" :class="{ 'warning': countdownSeconds < 15, 'danger': countdownSeconds < 5 }">
              {{ formatCountdown(countdownSeconds) }}
            </span>
          </div>
        </div>

        <!-- 订单金额 -->
        <div class="section">
          <h3>订单金额</h3>
          <div class="amount-info">
            <div class="info-item">
              <span class="label">订单总金额:</span>
              <span class="value price">¥{{ order.order.totalAmount.toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="order-actions">
          <el-button @click="goBack">返回订单列表</el-button>
          <el-button v-if="order.order.orderStatus === 0" type="success" @click="handlePayOrder(order.order.orderId, paymentMethod)">支付</el-button>
          <el-button v-if="order.order.orderStatus === 0" type="danger" @click="handleCancelOrder(order.order.orderId)">取消</el-button>
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
import { getProductDetail } from '@/api/products'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const orderId = route.params.id
const order = ref(null)
const loading = ref(true)
const paymentMethod = ref('2') // 默认余额支付

// 倒计时相关
const countdownSeconds = ref(30) // 30秒，与后端延迟消息时间一致
const countdownTimer = ref(null)

const loadOrderDetail = async () => {
  try {
    loading.value = true
    const res = await getOrderDetail(orderId)
    order.value = res
    // 打印订单详情数据结构，用于调试
    console.log('Order detail:', res)
    if (res.orderItems) {
      console.log('Order items:', res.orderItems)
      if (res.orderItems.length > 0) {
        console.log('First order item:', res.orderItems[0])
        console.log('First order item keys:', Object.keys(res.orderItems[0]))
      }
    }
    
    // 如果订单状态是待付款，开始倒计时
    if (res.order.orderStatus === 0) {
      startCountdown()
    } else {
      // 清除倒计时
      clearCountdown()
    }
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

// 格式化倒计时显示
const formatCountdown = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 开始倒计时
const startCountdown = () => {
  // 重置倒计时为30秒，与后端延迟消息时间一致
  countdownSeconds.value = 30
  
  // 清除之前的定时器
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
  
  // 开始倒计时
  countdownTimer.value = setInterval(() => {
    if (countdownSeconds.value > 0) {
      countdownSeconds.value--
    } else {
      // 倒计时结束
      clearCountdown()
      ElMessage.warning('订单支付超时，已自动取消')
      router.push('/orders')
    }
  }, 1000)
}

// 清除倒计时
const clearCountdown = () => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
}

const handlePayOrder = async (id, method) => {
  try {
    // 先从后端获取最新的订单状态
    const latestOrder = await getOrderDetail(id)
    
    // 检查订单状态
    if (latestOrder.order.orderStatus !== 0) {
      ElMessage.warning('订单状态已发生改变，无法支付')
      router.push('/orders')
      return
    }
    
    // 执行支付操作
    await payOrder({ 
      orderId: id, 
      paymentMethod: parseInt(method) 
    })
    ElMessage.success('支付成功')
    // 清除倒计时
    clearCountdown()
    loadOrderDetail()
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败')
  }
}

const handleCancelOrder = async (id) => {
  try {
    // 先从后端获取最新的订单状态
    const latestOrder = await getOrderDetail(id)
    
    // 检查订单状态
    if (latestOrder.order.orderStatus !== 0) {
      ElMessage.warning('订单状态已发生改变，无法取消')
      router.push('/orders')
      return
    }
    
    // 执行取消操作
    await cancelOrder(id)
    ElMessage.success('订单已取消')
    // 清除倒计时
    clearCountdown()
    loadOrderDetail()
  } catch (error) {
    console.error('取消订单失败:', error)
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

/* 商品信息样式 */
.product-info {
  border: 1px solid #eaeaea;
  border-radius: 8px;
  overflow: hidden;
}

.product-header {
  display: flex;
  background-color: #f5f5f5;
  padding: 10px 20px;
  border-bottom: 1px solid #eaeaea;
}

.header-item {
  font-weight: 500;
  color: #333;
}

.product-column {
  flex: 1;
  display: flex;
  align-items: center;
}

.price-column {
  width: 100px;
  text-align: center;
}

.quantity-column {
  width: 80px;
  text-align: center;
}

.subtotal-column {
  width: 100px;
  text-align: center;
}

.product-item {
  display: flex;
  padding: 15px 20px;
  border-bottom: 1px solid #eaeaea;
  align-items: center;
}

.product-item:last-child {
  border-bottom: none;
}

.product-item.even {
  background-color: #f9f9f9;
}

.product-item img {
  vertical-align: middle;
}

.product-item .product-column {
  font-size: 14px;
  color: #333;
}

.product-item .price-column,
.product-item .quantity-column,
.product-item .subtotal-column {
  font-size: 14px;
  color: #333;
}

/* 倒计时样式 */
.countdown-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eaeaea;
}

.countdown-label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
}

.countdown-time {
  font-weight: bold;
  color: #1890ff;
  font-size: 18px;
}

.countdown-time.warning {
  color: #faad14;
}

.countdown-time.danger {
  color: #ff4d4f;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
  100% {
    opacity: 1;
  }
}
</style>