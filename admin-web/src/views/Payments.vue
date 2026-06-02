<template>
  <div class="payments-page">
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="支付状态" clearable style="width: 120px" @change="loadList">
        <el-option label="待付款" :value="0" />
        <el-option label="已付款" :value="1" />
        <el-option label="已取消" :value="2" />
      </el-select>
      <el-select v-model="query.paymentMethod" placeholder="支付方式" clearable style="width: 120px" @change="loadList">
        <el-option label="支付宝" :value="0" />
        <el-option label="微信" :value="1" />
        <el-option label="余额" :value="2" />
      </el-select>
      <el-button type="primary" @click="loadList">查询</el-button>
    </div>
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">总支付金额</div>
          <div class="stat-value">¥ {{ formatMoney(paymentStats.totalAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">今日支付</div>
          <div class="stat-value">¥ {{ formatMoney(paymentStats.todayAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">成功笔数</div>
          <div class="stat-value">{{ paymentStats.successfulPaymentCount ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">总笔数</div>
          <div class="stat-value">{{ paymentStats.totalPaymentCount ?? '-' }}</div>
        </div>
      </el-col>
    </el-row>
    <el-table :data="list" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="paymentId" label="支付ID" width="90" />
      <el-table-column prop="orderNumber" label="订单编号" min-width="150" />
      <el-table-column prop="amount" label="金额" width="100">
        <template #default="{ row }">¥ {{ formatMoney(row.amount) }}</template>
      </el-table-column>
      <el-table-column prop="paymentMethod" label="支付方式" width="100">
        <template #default="{ row }">{{ paymentMethodText(row.paymentMethod) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">{{ paymentStatusText(row.status) }}</template>
      </el-table-column>
      <el-table-column prop="payTime" label="支付时间" width="170" />
    </el-table>
    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadList"
        @size-change="loadList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPaymentsPage, getPaymentStatistics } from '@/api/payments'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const paymentStats = ref({})
const query = reactive({ orderId: null, status: null, paymentMethod: null, page: 1, pageSize: 10 })

const paymentMethodMap = { 0: '支付宝', 1: '微信', 2: '余额' }
const paymentStatusMap = { 0: '待付款', 1: '已付款', 2: '已取消' }
function paymentMethodText(v) {
  return paymentMethodMap[v] ?? '-'
}
function paymentStatusText(v) {
  return paymentStatusMap[v] ?? '-'
}

function formatMoney(v) {
  if (v == null) return '0.00'
  return Number(v).toFixed(2)
}

async function loadList() {
  loading.value = true
  try {
    const res = await getPaymentsPage({
      orderId: query.orderId ?? undefined,
      status: query.status ?? undefined,
      paymentMethod: query.paymentMethod ?? undefined,
      page: query.page,
      pageSize: query.pageSize
    })
    list.value = res?.records ?? []
    total.value = res?.total ?? 0
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    paymentStats.value = await getPaymentStatistics()
  } catch (e) {
    paymentStats.value = {}
  }
}

onMounted(() => {
  loadStats()
  loadList()
})
</script>

<style scoped>
.payments-page { animation: fade-in 0.25s var(--ease); }
@keyframes fade-in { from { opacity: 0; } to { opacity: 1; } }

.toolbar { margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap; }

.stats-row { margin-bottom: 24px; }

.stat-card {
  background: #fff; border: 3px solid #111;
  padding: 18px 22px;
  box-shadow: var(--shadow-hard-sm);
  transition: box-shadow 0.1s, transform 0.1s;
}

.stat-card:hover {
  box-shadow: 5px 5px 0 #111;
  transform: translate(-2px, -2px);
}

.stat-label { font-size: 10px; color: #999; font-weight: 700; text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 6px; }
.stat-value { font-family: var(--font-display); font-size: 22px; color: #111; }

/* === Table === */
.payments-page :deep(.el-table) {
  --el-table-bg-color: #fff; --el-table-tr-bg-color: #fff;
  --el-table-header-bg-color: #fafaf9; --el-table-row-hover-bg-color: #f5f3f0;
  --el-table-border-color: #111; --el-table-text-color: #111;
  --el-table-header-text-color: #111;
  border: 3px solid #111; box-shadow: var(--shadow-hard);
}

.payments-page :deep(.el-table th.el-table__cell) {
  background: #111; color: #fff;
  font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em;
  border-bottom: 2px solid #111;
}
.payments-page :deep(.el-table td.el-table__cell) { border-bottom: 2px solid #e5e3e0; }

.payments-page .pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.payments-page :deep(.el-pager li) { border: 2px solid #111 !important; font-weight: 700 !important; }
.payments-page :deep(.el-pager li.is-active) { background: #111 !important; color: #fff !important; }
</style>
