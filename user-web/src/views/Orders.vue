<template>
  <div class="orders-page">
    <div class="container">
      <h2>我的订单</h2>

      <el-tabs v-model="activeTab" @tab-change="load">
        <el-tab-pane label="全部" :name="-1" />
        <el-tab-pane label="待支付" :name="0" />
        <el-tab-pane label="已付款" :name="1" />
        <el-tab-pane label="已发货" :name="2" />
        <el-tab-pane label="已完成" :name="3" />
        <el-tab-pane label="已取消" :name="4" />
        <el-tab-pane label="退款中" :name="5" />
        <el-tab-pane label="已退款" :name="6" />
      </el-tabs>

      <div v-if="list.length > 0">
        <div class="order-card" v-for="vo in list" :key="vo.order.orderId">
          <div class="order-header">
            <span>订单号：{{ vo.order.orderNumber }}</span>
            <span>下单时间：{{ $fmt(vo.order.createTime) }}</span>
          </div>
          <div class="order-body">
            <div class="order-items">
              <div class="item" v-for="(item, idx) in vo.items.slice(0, 3)" :key="item.itemId">
                <img :src="item.imageUrl" class="item-img" />
                <div class="item-name">{{ item.productName }}</div>
                <div class="item-price">×{{ item.quantity }}</div>
              </div>
              <div v-if="vo.items.length > 3" class="more-items">等{{ vo.items.length }}件商品</div>
            </div>
            <div class="order-summary">
              <div class="total">&yen;{{ vo.order.totalAmount.toFixed(2) }}</div>
              <el-tag :type="statusTag(vo.order.orderStatus)" size="small">{{ statusText(vo.order.orderStatus) }}</el-tag>
              <el-button size="small" @click="$router.push(`/order-detail/${vo.order.orderId}`)">查看详情</el-button>
              <el-button v-if="vo.order.orderStatus === 0" type="primary" size="small" @click="$router.push(`/order-detail/${vo.order.orderId}`)">去支付</el-button>
              <el-button v-if="vo.order.orderStatus === 0" type="danger" size="small" plain @click="handleCancel(vo.order.orderId)">取消</el-button>
              <el-button v-if="vo.order.orderStatus === 1 || vo.order.orderStatus === 2" type="warning" size="small" plain @click="openRefund(vo.order)">申请退款</el-button>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无订单" :image-size="100" />

      <div class="pagination" v-if="total > 0">
        <el-pagination v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="load" />
      </div>
    </div>

    <el-dialog v-model="refundVisible" title="申请退款" width="400px">
      <el-form>
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.amount" :min="0" :max="refundForm.maxAmount" :precision="2" />
        </el-form-item>
        <el-form-item label="退款原因">
          <el-input v-model="refundForm.reason" type="textarea" placeholder="请输入退款原因" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrders, cancelOrder } from '@/api/orders'
import { getMyRefunds, submitRefund } from '@/api/refunds'

const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const activeTab = ref(-1)
const refundVisible = ref(false)
const refundForm = ref({ orderId: null, amount: 0, maxAmount: 0, reason: '' })

const statusMap = { 0: '待支付', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消', 5: '退款中', 6: '已退款' }
const statusTagMap = { 0: 'warning', 1: '', 2: 'info', 3: 'success', 4: 'danger', 5: 'warning', 6: 'success' }
function statusText(s) { return statusMap[s] || '未知' }
function statusTag(s) { return statusTagMap[s] || 'info' }

async function load() {
  try {
    const params = { page: page.value, pageSize: pageSize.value, orderStatus: activeTab.value }
    const res = await getOrders(params)
    list.value = res.records || []
    total.value = res.total || 0
  } catch {
    ElMessage.error('加载订单失败')
  }
}

let myRefunds = []
async function loadRefunds() {
  try { myRefunds = await getMyRefunds() } catch { }
}

async function handleCancel(id) {
  try {
    await ElMessageBox.confirm('确定取消该订单吗？', '提示', { type: 'warning' })
    await cancelOrder(id)
    ElMessage.success('已取消')
    load()
  } catch { }
}

function openRefund(order) {
  refundForm.value = { orderId: order.orderId, amount: order.totalAmount, maxAmount: order.totalAmount, reason: '' }
  refundVisible.value = true
}

async function handleRefund() {
  if (!refundForm.value.reason.trim()) return ElMessage.warning('请输入退款原因')
  try {
    await submitRefund({ orderId: refundForm.value.orderId, amount: refundForm.value.amount, reason: refundForm.value.reason })
    ElMessage.success('退款申请已提交')
    refundVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e?.message || '提交失败')
  }
}

onMounted(() => { load(); loadRefunds() })
</script>

<style scoped>
.orders-page { padding: 40px 20px; min-height: 80vh; background: #f8fafc; }
.container { max-width: 960px; margin: 0 auto; }
h2 { font-size: 24px; font-weight: 700; color: #1e293b; margin-bottom: 24px; }
.order-card { background: #fff; border-radius: 12px; padding: 20px; margin-bottom: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.order-header { display: flex; justify-content: space-between; font-size: 13px; color: #64748b; margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid #f1f5f9; }
.order-body { display: flex; gap: 20px; }
.order-items { flex: 1; display: flex; gap: 12px; flex-wrap: wrap; }
.item { display: flex; flex-direction: column; align-items: center; width: 64px; }
.item-name { font-size: 11px; text-align: center; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 64px; }
.item-price { font-size: 11px; color: #64748b; }
.more-items { font-size: 12px; color: #94a3b8; display: flex; align-items: center; }
.item { display: flex; gap: 12px; margin-bottom: 8px; }
.item-img { width: 56px; height: 56px; border-radius: 8px; object-fit: cover; }
.order-summary { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; }
.total { font-size: 20px; font-weight: 700; color: #dc2626; }
.pagination { margin-top: 24px; display: flex; justify-content: center; }
</style>
