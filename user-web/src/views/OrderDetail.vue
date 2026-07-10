<template>
  <div class="order-detail-page">
    <div class="container" v-if="order">
      <div class="top-bar">
        <el-button link @click="router.push('/orders')">
          <el-icon><ArrowLeft /></el-icon> 返回订单列表
        </el-button>
        <h2>订单详情</h2>
      </div>

      <!-- Status Timeline -->
      <div class="timeline-section">
        <div class="timeline">
          <div
            v-for="(step, idx) in steps" :key="idx"
            class="t-step" :class="{ active: step.active, done: step.done }"
          >
            <div class="t-dot">
              <el-icon v-if="step.done" :size="14"><Check /></el-icon>
              <span v-else-if="step.active" class="dot-fill" />
              <span v-else class="dot-empty" />
            </div>
            <div class="t-label">{{ step.label }}</div>
            <div class="t-time" v-if="step.time">{{ step.time }}</div>
          </div>
        </div>
      </div>

      <div class="content-grid">
        <!-- Left -->
        <div class="main-col">
          <!-- Products -->
          <div class="card">
            <h3>商品信息</h3>
            <div class="product-row" v-for="item in order.orderItems || []" :key="item.itemId">
              <img v-if="item.imageUrl" :src="item.imageUrl" class="p-img" />
              <div class="p-info">
                <span class="p-name">{{ item.productName }}</span>
                <span class="p-price">&yen;{{ item.productPrice?.toFixed(2) }} &times; {{ item.quantity }}</span>
              </div>
              <span class="p-subtotal">&yen;{{ item.subtotal?.toFixed(2) }}</span>
            </div>
          </div>

          <!-- Shipping -->
          <div class="card">
            <h3>收货信息
              <el-button v-if="order.order.orderStatus === 0 && !editingReceiver" size="small" type="primary" link @click="startEditReceiver">编辑</el-button>
            </h3>
            <div class="info-grid" v-if="!editingReceiver">
              <div><span class="lbl">收货人</span> {{ order.order.receiverName || '待填写' }}</div>
              <div><span class="lbl">电话</span> {{ order.order.receiverPhone || '待填写' }}</div>
              <div><span class="lbl">地址</span> {{ order.order.shippingAddress || '待填写' }}</div>
            </div>
            <div v-else style="display:flex;flex-direction:column;gap:12px">
              <el-input v-model="editForm.receiverName" placeholder="收货人" />
              <el-input v-model="editForm.receiverPhone" placeholder="电话" />
              <el-input v-model="editForm.shippingAddress" placeholder="地址" />
              <div>
                <el-button size="small" type="primary" @click="saveReceiver">保存</el-button>
                <el-button size="small" @click="editingReceiver=false">取消</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- Right -->
        <div class="side-col">
          <div class="card">
            <h3>订单信息</h3>
            <div class="info-grid">
              <div><span class="lbl">订单号</span> <code>{{ order.order.orderNumber }}</code></div>
              <div><span class="lbl">下单时间</span> {{ $fmt(order.order.createTime) }}</div>
              <div>
                <span class="lbl">支付方式</span>
                {{ ['支付宝','微信','余额'][order.order.paymentMethod] || '--' }}
              </div>
              <div v-if="order.payment">
                <span class="lbl">支付时间</span>
                {{ $fmt(order.payment.payTime) || '--' }}
              </div>
              <div v-if="order.payment">
                <span class="lbl">支付状态</span>
                <el-tag :type="['warning','success','danger'][order.payment.status]" size="small">
                  {{ ['待付款','已付款','已取消'][order.payment.status] || '--' }}
                </el-tag>
              </div>
              <div v-if="order.couponName">
                <span class="lbl">优惠券</span>
                <span class="coupon-info">{{ order.couponName }}
                  <template v-if="order.couponType === 0">（满{{ order.couponMinAmount }}减{{ order.couponDiscount }}）</template>
                  <template v-else>（{{ (order.couponDiscount * 10).toFixed(1) }}折）</template>
                </span>
              </div>
              <div>
                <span class="lbl">订单金额</span>
                <strong class="price">&yen;{{ order.order.totalAmount?.toFixed(2) }}</strong>
              </div>
            </div>
          </div>

          <!-- Payment countdown -->
          <div class="card countdown-card" v-if="order.order.orderStatus === 0">
            <h3>支付剩余时间</h3>
            <div class="countdown-display" :class="{ warn: countdownSeconds < 300, danger: countdownSeconds < 60 }">
              {{ formatCountdown(countdownSeconds) }}
            </div>
            <div class="pay-methods">
              <el-radio-group v-model="paymentMethod">
                <el-radio label="2">余额</el-radio>
                <el-radio label="0">支付宝</el-radio>
                <el-radio label="1">微信</el-radio>
              </el-radio-group>
            </div>
            <el-button type="primary" size="large" style="width:100%" @click="handlePay">确认支付</el-button>
            <el-button size="large" style="width:100%;margin-top:8px" @click="handleCancel">取消订单</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else description="订单不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, payOrder, cancelOrder } from '@/api/orders'
import request from '@/utils/request'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const paymentMethod = ref('2')
const countdownSeconds = ref(0)
const editingReceiver = ref(false)
const editForm = ref({ receiverName: '', receiverPhone: '', shippingAddress: '' })
let timer = null

const statusOrder = [0, 1, 2, 3]
const statusLabels = ['待支付', '已付款', '已发货', '已完成']
const steps = computed(() => {
  const s = order.value?.order?.orderStatus
  return statusOrder.map((v, i) => ({
    label: statusLabels[i],
    active: s === v,
    done: s > v || (s === 4 && false),
    time: s === v ? order.value?.order?.createTime : ''
  }))
})

const formatCountdown = (s) => {
  const m = Math.floor(s / 60), sec = s % 60
  return `${String(m).padStart(2,'0')}:${String(sec).padStart(2,'0')}`
}

const load = async () => {
  try {
    const res = await getOrderDetail(route.params.id)
    order.value = res
    if (res.order.orderStatus === 0 && res.paymentRemainingTime) {
      countdownSeconds.value = res.paymentRemainingTime
      startCountdown()
    }
  } catch { ElMessage.error('加载失败') }
}

const startCountdown = () => {
  clearInterval(timer)
  timer = setInterval(() => {
    if (countdownSeconds.value > 0) countdownSeconds.value--
    else { clearInterval(timer); ElMessage.warning('订单已超时'); router.push('/orders') }
  }, 1000)
}

function startEditReceiver() {
  editForm.value = {
    receiverName: order.value.order.receiverName || '',
    receiverPhone: order.value.order.receiverPhone || '',
    shippingAddress: order.value.order.shippingAddress || ''
  }
  editingReceiver.value = true
}

async function saveReceiver() {
  try {
    await request.put(`/user/orders/receiver/${order.value.order.orderId}`, editForm.value)
    order.value.order.receiverName = editForm.value.receiverName
    order.value.order.receiverPhone = editForm.value.receiverPhone
    order.value.order.shippingAddress = editForm.value.shippingAddress
    editingReceiver.value = false
    ElMessage.success('收货信息已更新')
  } catch (e) { ElMessage.error(e.message || '更新失败') }
}

const handlePay = async () => {
  try {
    await payOrder({ orderId: order.value.order.orderId, paymentMethod: parseInt(paymentMethod.value) })
    ElMessage.success('支付成功，即将跳转'); clearInterval(timer); setTimeout(() => router.push('/orders'), 1500)
  } catch { ElMessage.error('支付失败') }
}
const handleCancel = async () => {
  try {
    await cancelOrder(order.value.order.orderId)
    ElMessage.success('已取消'); clearInterval(timer); router.push('/orders')
  } catch { ElMessage.error('取消失败') }
}

onMounted(load)
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.order-detail-page { padding: 0; }
.container { max-width: 900px; margin: 0 auto; padding: 0 20px; }

.top-bar { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
.top-bar h2 { font-size: 20px; font-weight: 700; }

/* Timeline */
.timeline-section {
  background: var(--surface); border-radius: var(--radius);
  padding: 28px 32px; border: 1px solid var(--border); margin-bottom: 20px;
}
.timeline { display: flex; justify-content: space-between; }
.t-step { display: flex; flex-direction: column; align-items: center; gap: 6px; flex: 1; text-align: center; }
.t-dot {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  border: 2px solid var(--border); background: var(--surface);
  position: relative;
}
.t-step.done .t-dot { background: var(--primary); border-color: var(--primary); color: #fff; }
.t-step.active .t-dot { border-color: var(--primary); }
.t-step:not(:last-child) .t-dot::after {
  content: ''; position: absolute; left: 100%; top: 50%;
  width: calc(100% + 60px); height: 2px;
  background: var(--border); z-index: 0;
}
.t-step.done .t-dot::after { background: var(--primary); }
.dot-fill { width: 12px; height: 12px; background: var(--primary); border-radius: 50%; }
.dot-empty { width: 8px; height: 8px; background: #cbd5e1; border-radius: 50%; }
.t-label { font-size: 13px; font-weight: 500; color: var(--text); }
.t-step.done .t-label { color: var(--primary); }
.t-step.active .t-label { color: var(--primary); font-weight: 600; }
.t-time { font-size: 11px; color: var(--text-secondary); }

/* Content */
.content-grid { display: grid; grid-template-columns: 1fr 320px; gap: 20px; }

.card {
  background: var(--surface); border-radius: var(--radius);
  border: 1px solid var(--border); padding: 20px 24px; margin-bottom: 16px;
}
.card h3 { font-size: 16px; font-weight: 600; margin-bottom: 16px; padding-bottom: 10px; border-bottom: 1px solid var(--border); }

.info-grid { display: flex; flex-direction: column; gap: 12px; font-size: 14px; }
.info-grid .lbl { color: var(--text-secondary); margin-right: 4px; }
.price { color: var(--danger); font-size: 18px; }

/* Products */
.product-row {
  display: flex; align-items: center; gap: 14px;
  padding: 12px 0; border-bottom: 1px solid var(--border);
}
.product-row:last-child { border: none; }
.p-img { width: 56px; height: 56px; object-fit: cover; border-radius: 6px; flex-shrink: 0; }
.p-info { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.p-name { font-size: 14px; color: var(--text); }
.p-price { font-size: 13px; color: var(--text-secondary); }
.p-subtotal { font-size: 15px; font-weight: 600; color: var(--danger); flex-shrink: 0; }

/* Countdown */
.countdown-display {
  font-size: 36px; font-weight: 800; text-align: center;
  padding: 16px 0; color: var(--primary-dark);
}
.countdown-display.warn { color: var(--accent); }
.countdown-display.danger { color: var(--danger); animation: pulse 1s infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.5} }
.pay-methods { margin-bottom: 12px; }
</style>
