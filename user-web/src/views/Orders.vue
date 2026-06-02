<template>
  <div class="orders-page">
    <div class="container">
      <h2>我的订单</h2>

      <div v-if="orders.length > 0">
        <div class="order-card" v-for="order in orders" :key="order.orderId">
          <div class="card-top">
            <span class="order-no">{{ order.orderNumber }}</span>
            <el-tag :type="statusType(order.orderStatus)" size="small">{{ statusText(order.orderStatus) }}</el-tag>
          </div>
          <div class="card-body">
            <div class="card-info">
              <div class="info-line">
                <span class="lbl">下单时间</span>
                <span>{{ $fmt(order.createTime) }}</span>
              </div>
              <div class="info-line">
                <span class="lbl">订单金额</span>
                <span class="price">&yen;{{ order.totalAmount?.toFixed(2) }}</span>
              </div>
            </div>
            <div class="card-actions">
              <el-button size="small" @click="router.push(`/order-detail/${order.orderId}`)">详情</el-button>
              <el-button v-if="order.orderStatus === 0" size="small" type="success" @click="router.push(`/order-detail/${order.orderId}`)">支付</el-button>
              <el-button v-if="order.orderStatus === 0" size="small" type="danger" @click="handleCancel(order.orderId)">取消</el-button>
              <el-tag v-if="getRefundStatus(order.orderId) !== null" :type="['warning','success','danger','info'][getRefundStatus(order.orderId)]" size="small">{{ refundStatusText(getRefundStatus(order.orderId)) }}</el-tag>
              <el-button v-else-if="order.orderStatus === 1 || order.orderStatus === 2" size="small" type="warning" @click="openRefund(order)">申请退款</el-button>
            </div>
          </div>
        </div>

        <div class="pagination">
          <el-pagination
            v-model:current-page="page" v-model:page-size="size"
            :page-sizes="[10,20,30]" layout="total, prev, pager, next"
            :total="total" @current-change="load" @size-change="load"
          />
        </div>
      </div>

      <el-empty v-else description="暂无订单" />
    </div>

    <!-- 退款弹窗 -->
    <el-dialog v-model="refundVisible" title="申请退款" width="420px">
      <el-form :model="refundForm" label-width="80px">
        <el-form-item label="订单编号"><span>{{ refundForm.orderNumber }}</span></el-form-item>
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.amount" :min="0.01" :max="refundForm.maxAmount" :precision="2" />
        </el-form-item>
        <el-form-item label="退款原因">
          <el-input v-model="refundForm.reason" type="textarea" rows="3" placeholder="请说明退款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRefund">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrders, cancelOrder } from '@/api/orders'
import { submitRefund, getMyRefunds } from '@/api/refunds'
import { ElMessage } from 'element-plus'

const router = useRouter()
const orders = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const refundVisible = ref(false)
const refundForm = reactive({ orderId: null, orderNumber: '', amount: 0, maxAmount: 0, reason: '' })
const myRefunds = ref([])

const load = async () => {
  try {
    const res = await getOrders({ page: page.value, pageSize: size.value })
    orders.value = res.records || []
    total.value = res.total || 0
  } catch { ElMessage.error('加载订单失败') }
}

// 加载退款记录，用于判断哪些订单已有退款申请
const loadRefunds = async () => {
  try { myRefunds.value = await getMyRefunds() } catch { }
}

// 获取某订单的退款状态 (0=待处理, 1=已通过, 3=已完成)
function getRefundStatus(orderId) {
  const r = myRefunds.value.find(rf => rf.orderId === orderId)
  return r ? r.status : null
}

function refundStatusText(s) {
  return ['退款审核中','退款已通过','退款已拒绝','已退款'][s] || ''
}

const statusType = (s) => ['warning','success','info','success','danger','warning','info'][s] || ''
const statusText = (s) => ['待支付','已付款','已发货','已完成','已取消','退款中','已退款'][s] || s

const handleCancel = async (id) => {
  try { await cancelOrder(id); ElMessage.success('已取消'); load() }
  catch { ElMessage.error('取消失败') }
}

function openRefund(order) {
  refundForm.orderId = order.orderId
  refundForm.orderNumber = order.orderNumber
  refundForm.maxAmount = order.totalAmount
  refundForm.amount = order.totalAmount
  refundForm.reason = ''
  refundVisible.value = true
}

async function handleRefund() {
  if (!refundForm.reason.trim()) return ElMessage.warning('请输入退款原因')
  try {
    await submitRefund({ orderId: refundForm.orderId, amount: refundForm.amount, reason: refundForm.reason })
    ElMessage.success('退款申请已提交')
    refundVisible.value = false
    load()
  } catch { }
}

onMounted(() => { load(); loadRefunds() })
</script>

<style scoped>
.orders-page { padding: 0; }
.container { max-width: 800px; margin: 0 auto; padding: 0 20px; }
h2 { font-size: 22px; font-weight: 700; margin-bottom: 24px; }

.order-card {
  background: var(--surface);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  padding: 16px 20px;
  margin-bottom: 12px;
  transition: box-shadow .2s;
}
.order-card:hover { box-shadow: var(--shadow); }
.card-top {
  display: flex; justify-content: space-between; align-items: center;
  padding-bottom: 12px; margin-bottom: 12px;
  border-bottom: 1px solid var(--border);
}
.order-no { font-size: 13px; color: var(--text-secondary); font-family: monospace; }
.card-body { display: flex; justify-content: space-between; align-items: flex-end; }
.card-info { display: flex; flex-direction: column; gap: 6px; }
.info-line { font-size: 14px; color: var(--text); }
.lbl { color: var(--text-secondary); margin-right: 8px; }
.price { color: var(--danger); font-weight: 600; }
.card-actions { display: flex; gap: 8px; flex-shrink: 0; }

.pagination { display: flex; justify-content: center; margin-top: 24px; }
</style>
