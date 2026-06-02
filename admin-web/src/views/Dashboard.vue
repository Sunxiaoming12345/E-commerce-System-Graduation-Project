<template>
  <div class="dashboard">
    <div class="page-header">
      <h2 class="page-name">工作台</h2>
      <div class="page-rule"></div>
    </div>

    <!-- Primary stats — asymmetric grid -->
    <div class="stats-primary">
      <div v-for="(card, i) in primaryCards" :key="card.label" class="stat-card" :class="card.style" :style="{ animationDelay: `${i * 0.06}s` }">
        <div class="card-top-bar"></div>
        <div class="card-body">
          <div class="stat-icon"><el-icon :size="22"><component :is="card.icon" /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-sub" v-if="card.sub">{{ card.sub }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Secondary stats -->
    <div class="stats-secondary">
      <div v-for="(s, i) in secondaryCards" :key="s.label" class="stat-sm" :class="s.style" :style="{ animationDelay: `${0.3 + i * 0.05}s` }">
        <div class="sm-accent"></div>
        <div>
          <div class="sm-value">{{ s.value }}</div>
          <div class="sm-label">{{ s.label }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getOrderStatistics } from '@/api/orders'
import { Document, Money, TrendCharts, Van } from '@element-plus/icons-vue'

const stats = ref({})

const primaryCards = computed(() => [
  { label: '总订单数', value: stats.value.totalOrderCount ?? '—', icon: Document, style: 'black', sub: '全部订单' },
  { label: '总销售额', value: stats.value.totalSales != null ? `¥${formatMoney(stats.value.totalSales)}` : '—', icon: Money, style: 'red', sub: '累计收入' },
  { label: '今日销售额', value: stats.value.todaySales != null ? `¥${formatMoney(stats.value.todaySales)}` : '—', icon: TrendCharts, style: 'yellow', sub: '当日收入' },
  { label: '待发/已发货', value: stats.value.shippedCount ?? '—', icon: Van, style: 'blue', sub: '物流状态' }
])

const secondaryCards = computed(() => [
  { label: '待支付', value: stats.value.pendingPaymentCount ?? '—', style: 'warning' },
  { label: '已支付', value: stats.value.paidCount ?? '—', style: 'success' },
  { label: '已完成', value: stats.value.completedCount ?? '—', style: 'info' },
  { label: '已取消', value: stats.value.cancelledCount ?? '—', style: 'danger' }
])

function formatMoney(v) { if (v == null) return '0.00'; return Number(v).toFixed(2) }

onMounted(async () => {
  try { stats.value = await getOrderStatistics() } catch { stats.value = {} }
})
</script>

<style scoped>
.dashboard { animation: fade-in 0.3s var(--ease); }
@keyframes fade-in { from { opacity: 0; } to { opacity: 1; } }

.page-header { margin-bottom: 28px; display: flex; align-items: center; gap: 16px; }
.page-name { font-family: var(--font-display); font-size: 32px; color: #111; margin: 0; letter-spacing: 0.06em; }
.page-rule { flex: 1; height: 4px; background: #111; min-width: 40px; }

/* === Primary Cards === */
.stats-primary {
  display: grid;
  grid-template-columns: 1.2fr 1fr 0.9fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border: 3px solid #111;
  box-shadow: var(--shadow-hard);
  overflow: hidden;
  animation: card-pop 0.35s var(--ease) both;
  transition: box-shadow 0.1s, transform 0.1s;
}

@keyframes card-pop {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.stat-card:hover {
  box-shadow: 6px 6px 0 #111;
  transform: translate(-2px, -2px);
}

.card-top-bar { height: 6px; }
.stat-card.black .card-top-bar { background: #111; }
.stat-card.red .card-top-bar { background: #e53935; }
.stat-card.yellow .card-top-bar { background: #ffb300; }
.stat-card.blue .card-top-bar { background: #1565c0; }

.card-body {
  display: flex; align-items: flex-start; gap: 14px;
  padding: 20px 22px;
}

.stat-icon {
  width: 48px; height: 48px;
  border: 2px solid #111;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  background: #fff;
}

.stat-card.black .stat-icon { background: #111; color: #fff; }
.stat-card.red .stat-icon { background: #e53935; color: #fff; border-color: #e53935; }
.stat-card.yellow .stat-icon { background: #ffb300; color: #111; border-color: #ffb300; }
.stat-card.blue .stat-icon { background: #1565c0; color: #fff; border-color: #1565c0; }

.stat-value { font-family: var(--font-display); font-size: 28px; color: #111; line-height: 1.1; }
.stat-label { font-size: 11px; color: #999; font-weight: 700; text-transform: uppercase; letter-spacing: 0.08em; margin-top: 2px; }
.stat-sub { font-size: 11px; color: #999; margin-top: 1px; }

/* === Secondary Cards === */
.stats-secondary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-sm {
  background: #fff;
  border: 3px solid #111;
  display: flex; align-items: center; gap: 14px;
  padding: 16px 18px;
  box-shadow: var(--shadow-hard-sm);
  animation: card-pop 0.3s var(--ease) both;
  transition: box-shadow 0.1s, transform 0.1s;
}

.stat-sm:hover {
  box-shadow: 4px 4px 0 #111;
  transform: translate(-1px, -1px);
}

.sm-accent { width: 5px; height: 32px; flex-shrink: 0; }
.stat-sm.warning .sm-accent { background: #e65100; }
.stat-sm.success .sm-accent { background: #1b5e20; }
.stat-sm.info .sm-accent { background: #0d47a1; }
.stat-sm.danger .sm-accent { background: #c62828; }

.sm-value { font-size: 22px; font-weight: 900; color: #111; line-height: 1.2; }
.sm-label { font-size: 11px; color: #999; font-weight: 600; text-transform: uppercase; letter-spacing: 0.06em; }

@media (max-width: 1200px) { .stats-primary { grid-template-columns: 1fr 1fr; } .stats-secondary { grid-template-columns: 1fr 1fr; } }
@media (max-width: 640px) { .stats-primary, .stats-secondary { grid-template-columns: 1fr; } }
</style>
