<template>
  <div class="refunds-page">
    <div class="container">
      <h2>我的退款</h2>
      <div v-if="refunds.length > 0">
        <div class="refund-card" v-for="r in refunds" :key="r.refundId">
          <div class="card-top">
            <span class="order-label">订单 #{{ r.orderId }}</span>
            <el-tag :type="statusType(r.status)" size="small">{{ statusText(r.status) }}</el-tag>
          </div>
          <div class="card-body">
            <div class="card-info">
              <div class="info-line"><span class="lbl">退款金额</span><span class="price">&yen;{{ r.amount?.toFixed(2) }}</span></div>
              <div class="info-line"><span class="lbl">申请原因</span><span>{{ r.reason }}</span></div>
              <div class="info-line" v-if="r.adminRemark"><span class="lbl">管理员备注</span><span>{{ r.adminRemark }}</span></div>
              <div class="info-line"><span class="lbl">申请时间</span><span>{{ $fmt(r.createTime) }}</span></div>
              <div class="info-line" v-if="r.refundTime"><span class="lbl">退款时间</span><span>{{ $fmt(r.refundTime) }}</span></div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无退款记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyRefunds } from '@/api/refunds'

const refunds = ref([])

function statusText(s) { return ['待处理', '已通过', '已拒绝', '已完成'][s] || '未知' }
function statusType(s) { return ['warning', 'success', 'danger', 'info'][s] || 'info' }

async function load() {
  try { refunds.value = await getMyRefunds() }
  catch { ElMessage.error('加载退款列表失败') }
}

onMounted(load)
</script>

<style scoped>
.refunds-page { padding: 40px 20px; min-height: 80vh; }
.container { max-width: 800px; margin: 0 auto; }
h2 { font-size: 24px; font-weight: 700; color: var(--text-primary, #1a202c); margin-bottom: 24px; }
.refund-card {
  background: var(--surface, #fff); border-radius: 12px;
  border: 1px solid var(--border, #e2e8f0); padding: 20px; margin-bottom: 12px;
}
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.order-label { font-weight: 600; color: var(--text-primary, #1a202c); }
.card-info { display: flex; flex-direction: column; gap: 8px; }
.info-line { display: flex; gap: 12px; }
.lbl { color: var(--text-secondary, #64748b); min-width: 80px; }
.price { color: #dc2626; font-weight: 600; }
</style>
