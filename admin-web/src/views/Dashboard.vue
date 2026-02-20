<template>
  <div class="dashboard">
    <h2 class="page-name">工作台</h2>
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">总订单数</div>
          <div class="stat-value">{{ stats.totalOrderCount ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">总销售额</div>
          <div class="stat-value">¥ {{ formatMoney(stats.totalSales) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">今日销售额</div>
          <div class="stat-value">¥ {{ formatMoney(stats.todaySales) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">待发货</div>
          <div class="stat-value">{{ stats.pendingShipmentCount ?? '-' }}</div>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card minor">
          <div class="stat-label">待支付</div>
          <div class="stat-value">{{ stats.pendingPaymentCount ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card minor">
          <div class="stat-label">已支付</div>
          <div class="stat-value">{{ stats.paidCount ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card minor">
          <div class="stat-label">已完成</div>
          <div class="stat-value">{{ stats.completedCount ?? '-' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card minor">
          <div class="stat-label">已取消</div>
          <div class="stat-value">{{ stats.cancelledCount ?? '-' }}</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrderStatistics } from '@/api/orders'

const stats = ref({})

function formatMoney(v) {
  if (v == null) return '0.00'
  return Number(v).toFixed(2)
}

onMounted(async () => {
  try {
    stats.value = await getOrderStatistics()
  } catch (e) {
    stats.value = {}
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0 8px;
}
.page-name {
  margin-bottom: 20px;
  font-size: 20px;
  color: #1a202c;
}
.stats-row {
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,.08);
  border-left: 4px solid #5eead4;
}
.stat-card.minor {
  border-left-color: #94a3b8;
}
.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a202c;
}
</style>
