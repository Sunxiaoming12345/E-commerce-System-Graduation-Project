<template>
  <div class="order-confirm">
    <h2>确认订单</h2>
    <el-loading v-loading="loading" element-loading-text="加载中...">
      <div class="order-content">
        <!-- 收货地址 -->
        <div class="section">
          <h3>收货信息</h3>
          <el-form :model="form" label-width="100px">
            <el-form-item label="收货人">
              <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="form.receiverPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="收货地址">
              <el-input v-model="form.shippingAddress" placeholder="请输入详细收货地址" type="textarea" :rows="3" />
            </el-form-item>
          </el-form>
        </div>

        <!-- 商品信息 -->
        <div class="section">
          <h3>商品信息</h3>
          <el-table :data="orderItems" style="width: 100%">
            <el-table-column label="商品" min-width="200">
              <template #default="scope">
                <div class="product-info">
                  <img :src="scope.row.imageUrl || 'https://via.placeholder.com/80'" :alt="scope.row.productName" class="product-image" />
                  <span class="product-name">{{ scope.row.productName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="100">
              <template #default="scope">
                ¥{{ scope.row.price }}
              </template>
            </el-table-column>
            <el-table-column label="数量" width="100">
              <template #default="scope">
                {{ scope.row.quantity }}
              </template>
            </el-table-column>
            <el-table-column label="小计" width="120">
              <template #default="scope">
                ¥{{ (scope.row.quantity * scope.row.price).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 支付方式 -->
        <div class="section">
          <h3>支付方式</h3>
          <el-radio-group v-model="form.paymentMethod">
            <el-radio label="2">余额支付</el-radio>
            <el-radio label="0">支付宝</el-radio>
            <el-radio label="1">微信支付</el-radio>
          </el-radio-group>
        </div>

        <!-- 优惠券 -->
        <div class="section" v-if="couponGroups.length > 0">
          <h3>优惠券</h3>
          <div class="coupon-list">
            <div
              v-for="g in couponGroups"
              :key="g.couponId"
              class="coupon-card"
              :class="{ selected: selectedCouponId && g.ids.includes(selectedCouponId) }"
              @click="onCouponSelect(selectedCouponId && g.ids.includes(selectedCouponId) ? null : g.ids[0])"
            >
              <div class="coupon-left">
                <span class="coupon-value" v-if="g.type === 0">¥{{ g.discountValue }}</span>
                <span class="coupon-value" v-else>{{ (g.discountValue * 10).toFixed(1) }}折</span>
                <span class="coupon-name">{{ g.name }}</span>
              </div>
              <div class="coupon-right">
                <span class="coupon-condition">满 ¥{{ g.minAmount }} 可用</span>
                <span class="coupon-expire">有效期至 {{ g.endTime?.substring(0, 10) }}</span>
              </div>
              <el-badge :value="g.count" class="coupon-count" v-if="g.count > 1" />
              <el-icon v-if="selectedCouponId && g.ids.includes(selectedCouponId)" class="coupon-check" color="#fff" :size="20"><CircleCheckFilled /></el-icon>
            </div>
          </div>
        </div>

        <!-- 订单金额 -->
        <div class="section">
          <h3>订单金额</h3>
          <div class="order-amount">
            <div class="amount-item">
              <span>商品总价：</span>
              <span>¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <div class="amount-item">
              <span>运费：</span>
              <span>¥{{ shippingFee.toFixed(2) }}</span>
            </div>
            <div class="amount-item coupon-discount" v-if="couponDiscount > 0">
              <span>优惠券抵扣：</span>
              <span>-¥{{ couponDiscount.toFixed(2) }}</span>
            </div>
            <div class="amount-item total">
              <span>实付金额：</span>
              <span>¥{{ actualAmount.toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="order-actions">
          <el-button @click="goBack">返回购物车</el-button>
          <el-button type="primary" size="large" :loading="loading" @click="submitOrder">提交订单</el-button>
        </div>
      </div>
    </el-loading>

    <!-- 余额支付确认弹窗 -->
    <el-dialog
      v-model="balanceDialogVisible"
      title="余额支付确认"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @open="startCountdown"
      @closed="onBalanceDialogClosed"
    >
      <div class="balance-info">
        <p class="balance-item">
          <span>账户当前余额：</span>
          <span class="balance-amount">¥{{ userBalance.toFixed(2) }}</span>
        </p>
        <p class="balance-item">
          <span>本次支付金额：</span>
          <span class="payment-amount">¥{{ actualAmount.toFixed(2) }}</span>
        </p>
        <p class="balance-item countdown">
          <span>支付剩余时间：</span>
          <span class="countdown-time" :class="{ 'warning': countdownSeconds < 300, 'danger': countdownSeconds < 60 }">
            {{ formatCountdown(countdownSeconds) }}
          </span>
        </p>
        <p v-if="balanceInsufficient" class="balance-warning">余额不足本次应付金额，请先充值或返回更换支付方式</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelBalancePayment">暂不支付</el-button>
          <el-button type="primary" :disabled="balanceInsufficient" @click="confirmBalancePayment">确认支付</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createOrder, payOrder, getSubmitToken } from '@/api/orders'
import { removeCartItem } from '@/api/cart'
import { getBalance } from '@/api/balance'
import { getProductDetail } from '@/api/products'
import { getMyCoupons, previewCouponDiscount } from '@/api/coupons'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const userStore = useUserStore()

// 订单商品信息
const orderItems = ref([])

// 表单数据
const form = ref({
  receiverName: '',
  receiverPhone: '',
  shippingAddress: '',
  paymentMethod: '2' // 默认余额支付
})

// 运费
const shippingFee = ref(0)

// 优惠券相关
const coupons = ref([])
const couponGroups = ref([]) // 按 coupon_id 合并后的优惠券分组
const selectedCouponId = ref(null)
const couponDiscount = ref(0) // 优惠券抵扣金额

// 余额相关
const balanceDialogVisible = ref(false)
const userBalance = ref(0)
const currentOrderNumber = ref('')

// 幂等令牌
const submitToken = ref('')

// 倒计时相关
const countdownSeconds = ref(600) // 10分钟，默认值
const countdownTimer = ref(null)

// 计算商品总价
const totalPrice = computed(() => {
  return orderItems.value.reduce((total, item) => {
    return total + (item.quantity * item.price)
  }, 0)
})

// 计算实付金额（扣除优惠券抵扣）
const actualAmount = computed(() => {
  return totalPrice.value + shippingFee.value - couponDiscount.value
})

const balanceInsufficient = computed(() => Number(userBalance.value) < Number(actualAmount.value))

// 加载订单商品信息
const loadOrderItems = async () => {
  // 从路由参数中获取选中的商品
  const selectedItems = JSON.parse(localStorage.getItem('selectedCartItems') || '[]')
  if (selectedItems.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    router.push('/cart')
    return
  }
  
  // 重新获取商品的最新信息，包括商品状态
  try {
    const updatedItems = await Promise.all(selectedItems.map(async (item) => {
      try {
        const productDetail = await getProductDetail(item.productId)
        console.log(`获取商品详情成功：productId=${item.productId}`, productDetail)
        return {
          ...item,
          status: productDetail.status, // 更新商品状态
          stock: productDetail.stock // 更新商品库存
        }
      } catch (error) {
        console.error(`获取商品详情失败：productId=${item.productId}`, error)
        // 如果获取失败，将商品状态设置为 0（已下架）
        return {
          ...item,
          status: 0
        }
      }
    }))
    console.log('更新后的商品信息：', updatedItems)
    orderItems.value = updatedItems
  } catch (error) {
    console.error('加载商品信息失败', error)
    // 如果加载失败，将所有商品状态设置为 0（已下架）
    orderItems.value = selectedItems.map(item => ({
      ...item,
      status: 0
    }))
  }
}

// 加载用户常用收货人信息
const loadUserInfo = async () => {
  await userStore.fetchUserInfo()
  if (userStore.userInfo) {
    // 设置默认值
    form.value.receiverName = form.value.receiverName || userStore.userInfo.defaultReceiver || ''
    form.value.receiverPhone = form.value.receiverPhone || userStore.userInfo.defaultPhone || ''
    form.value.shippingAddress = form.value.shippingAddress || userStore.userInfo.defaultAddress || ''
  }
}

// 加载用户余额
const loadUserBalance = async () => {
  try {
    const res = await getBalance()
    userBalance.value = res || 0
  } catch (error) {
    console.error('Failed to get balance:', error)
    userBalance.value = 0
  }
}

// 加载用户可用优惠券
const loadCoupons = async () => {
  try {
    const all = await getMyCoupons(0) // status=0 未使用
    // 过滤出满足最低消费金额的优惠券
    const filtered = (all || []).filter(c => {
      return totalPrice.value >= (c.minAmount || 0)
    })
    // 按 coupon_id 合并相同优惠券
    const groupMap = new Map()
    filtered.forEach(c => {
      const key = c.couponId
      if (groupMap.has(key)) {
        const g = groupMap.get(key)
        g.count++
        g.ids.push(c.userCouponId)
      } else {
        groupMap.set(key, { ...c, count: 1, ids: [c.userCouponId] })
      }
    })
    couponGroups.value = Array.from(groupMap.values())
    if (couponGroups.value.length > 0) {
      console.log('可用的优惠券（已合并）：', couponGroups.value)
    }
  } catch (error) {
    console.error('加载优惠券失败', error)
    couponGroups.value = []
  }
}

// 选择优惠券
const onCouponSelect = async (userCouponId) => {
  if (!userCouponId) {
    selectedCouponId.value = null
    couponDiscount.value = 0
    return
  }
  try {
    const discount = await previewCouponDiscount(userCouponId, totalPrice.value)
    selectedCouponId.value = userCouponId
    couponDiscount.value = discount || 0
    ElMessage.success(`优惠券可抵扣 ¥${(discount || 0).toFixed(2)}`)
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || '优惠券不可用')
    selectedCouponId.value = null
    couponDiscount.value = 0
  }
}

// 提交订单
const submitOrder = async () => {
  // 验证表单
  if (!form.value.receiverName || !form.value.receiverPhone || !form.value.shippingAddress) {
    ElMessage.warning('请填写完整的收货信息')
    return
  }

  // 重新获取商品的最新状态
  try {
    const latestItems = await Promise.all(orderItems.value.map(async (item) => {
      try {
        const productDetail = await getProductDetail(item.productId)
        console.log(`提交订单时获取商品详情：productId=${item.productId}`, productDetail)
        return {
          ...item,
          status: productDetail.status,
          stock: productDetail.stock
        }
      } catch (error) {
        console.error(`获取商品详情失败：productId=${item.productId}`, error)
        return {
          ...item,
          status: 0
        }
      }
    }))
    
    // 检查商品状态
    const offlineItems = latestItems.filter(item => item.status == 0)
    if (offlineItems.length > 0) {
      ElMessage.error('选中的商品中有已下架商品，无法结算')
      // 跳转到购物车页面
      setTimeout(() => {
        router.push('/cart')
      }, 1500)
      return
    }
    
    // 更新订单商品信息为最新状态
    orderItems.value = latestItems
  } catch (error) {
    console.error('检查商品状态失败', error)
    ElMessage.error('检查商品状态失败，无法提交订单')
    // 跳转到购物车页面
    setTimeout(() => {
      router.push('/cart')
    }, 1500)
    return
  }

  try {
    loading.value = true
    
    // 创建订单，携带一次性幂等令牌
    const orderData = {
      items: orderItems.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      })),
      receiverName: form.value.receiverName,
      receiverPhone: form.value.receiverPhone,
      shippingAddress: form.value.shippingAddress,
      paymentMethod: parseInt(form.value.paymentMethod),
      totalAmount: totalPrice.value + shippingFee.value,
      idempotentToken: submitToken.value
    }
    // 如果选择了优惠券，传递 userCouponId
    if (selectedCouponId.value) {
      orderData.userCouponId = selectedCouponId.value
    }

    // 调用创建订单接口
    const res = await createOrder(orderData)
    // 订单创建成功后刷新令牌，下次提交用新令牌
    try { submitToken.value = await getSubmitToken() } catch (e) { /* 静默 */ }
    currentOrderNumber.value = res.orderNumber || ''
    
    // 获取支付超时时间（秒）
    const paymentTimeout = res.paymentTimeout || 600
    console.log('支付超时时间：', paymentTimeout, '秒')
    
    // 检查是否是直接购买
    const isDirectPurchase = localStorage.getItem('isDirectPurchase') === 'true'
    
    // 如果不是直接购买，则从购物车中移除已结算的商品
    if (!isDirectPurchase) {
      for (const item of orderItems.value) {
        await removeCartItem(item.productId)
      }
    }
    
    // 清除本地存储的选中商品和直接购买标记
    localStorage.removeItem('selectedCartItems')
    localStorage.removeItem('isDirectPurchase')
    
    // 如果是余额支付，显示确认弹窗
    if (form.value.paymentMethod === '2') {
      await loadUserBalance()
      // 设置倒计时时间为从后端获取的支付超时时间
      countdownSeconds.value = paymentTimeout
      balanceDialogVisible.value = true
    } else {
      // 其他支付方式直接跳转到订单列表
      ElMessage.success('订单创建成功')
      router.push('/orders')
    }
  } catch (error) {
    // 捕获后端返回的错误信息
    if (error.response && error.response.data && error.response.data.msg) {
      ElMessage.error(error.response.data.msg)
    } else {
      ElMessage.error('提交订单失败')
    }
    console.error('Failed to submit order:', error)
  } finally {
    loading.value = false
  }
}

// 取消余额支付（含右上角关闭 ×，关闭动画结束后统一处理）
const cancelBalancePayment = () => {
  balanceDialogVisible.value = false
}

const onBalanceDialogClosed = () => {
  clearCountdown()
  router.push('/orders')
}

// 格式化倒计时显示
const formatCountdown = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 开始倒计时
const startCountdown = () => {
  // 清除之前的定时器
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
  
  // 开始倒计时
  countdownTimer.value = setInterval(() => {
    if (countdownSeconds.value > 0) {
      countdownSeconds.value--
    } else {
      ElMessage.warning('订单支付超时，已自动取消')
      balanceDialogVisible.value = false
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

// 确认余额支付
const confirmBalancePayment = async () => {
  if (!currentOrderNumber.value) {
    ElMessage.error('订单信息错误')
    return
  }

  try {
    loading.value = true

    await payOrder({
      orderNumber: currentOrderNumber.value,
      paymentMethod: 2
    })

    ElMessage.success('支付成功')
    balanceDialogVisible.value = false
  } catch (error) {
    console.error('Failed to pay order:', error)
  } finally {
    loading.value = false
  }
}

// 返回购物车
const goBack = () => {
  router.push('/cart')
}

onMounted(async () => {
  await loadOrderItems()
  await loadUserInfo()
  if (form.value.paymentMethod === '2') {
    await loadUserBalance()
  }
  await loadCoupons()
  // 获取一次性幂等令牌
  try { submitToken.value = await getSubmitToken() } catch (e) { /* 静默 */ }
})
</script>

<style scoped>
.order-confirm {
  padding: 20px 0;
}

.order-confirm h2 {
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

.product-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.product-name {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.order-amount {
  margin-top: 10px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}

.amount-item.total {
  font-weight: bold;
  font-size: 16px;
  color: #ff4d4f;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eaeaea;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 40px;
}

/* 余额支付确认弹窗样式 */
.balance-info {
  padding: 20px 0;
}

.balance-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  font-size: 16px;
}

.balance-amount {
  font-weight: bold;
  color: #1890ff;
}

.payment-amount {
  font-weight: bold;
  color: #ff4d4f;
}

.balance-warning {
  color: #f56c6c;
  font-size: 13px;
  margin-top: -8px;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 倒计时样式 */
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

/* 优惠券样式 */
.coupon-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.coupon-card {
  position: relative;
  display: flex;
  align-items: stretch;
  min-width: 280px;
  flex: 1;
  max-width: 400px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all .2s;
  background: #fafafa;
}
.coupon-card:hover {
  border-color: var(--primary, #409eff);
  box-shadow: 0 2px 8px rgba(0,0,0,.1);
}
.coupon-card.selected {
  border-color: var(--primary, #409eff);
  background: linear-gradient(135deg, #e8f4ff 0%, #f0f7ff 100%);
}
.coupon-left {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 12px 16px;
  background: var(--primary, #409eff);
  color: #fff;
  min-width: 90px;
  text-align: center;
}
.coupon-value {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
}
.coupon-name {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.9;
}
.coupon-right {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 12px 16px;
  gap: 4px;
}
.coupon-condition {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.coupon-expire {
  font-size: 12px;
  color: #999;
}
.coupon-check {
  position: absolute;
  top: 4px;
  right: 4px;
  background: var(--primary, #409eff);
  border-radius: 50%;
  padding: 2px;
}
.coupon-discount span:last-child {
  color: var(--success, #67c23a);
}

/* 已选择无优惠券时的小提示 */
.coupon-list + .section { margin-top: 24px; }
</style>
