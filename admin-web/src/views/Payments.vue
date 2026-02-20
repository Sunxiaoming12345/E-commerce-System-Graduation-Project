<template>
  <div class="payments-page">
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="支付状态" clearable style="width: 120px" @change="loadList">
        <el-option label="未支付" :value="0" />
        <el-option label="已支付" :value="1" />
        <el-option label="支付失败" :value="2" />
      </el-select>
      <el-select v-model="query.paymentMethod" placeholder="支付方式" clearable style="width: 120px" @change="loadList">
        <el-option label="支付宝" :value="0" />
        <el-option label="微信" :value="1" />
        <el-option label="银行卡" :value="2" />
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
      <el-table-column prop="orderId" label="订单ID" width="90" />
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

const paymentMethodMap = { 0: '支付宝', 1: '微信', 2: '银行卡' }
const paymentStatusMap = { 0: '未支付', 1: '已支付', 2: '支付失败' }
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
.payments-page .toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}
.stats-row {
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,.08);
  border-left: 4px solid #5eead4;
}
.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 6px;
}
.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #1a202c;
}
.payments-page .pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
