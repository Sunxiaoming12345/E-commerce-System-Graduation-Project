<template>
  <div class="records-page">
    <div class="container">
      <div class="header">
        <h2>我的抽奖记录</h2>
        <router-link to="/lottery" class="back-link">← 返回抽奖</router-link>
      </div>

      <el-table :data="records" stripe v-loading="loading" empty-text="暂无抽奖记录">
        <el-table-column label="奖品" min-width="150">
          <template #default="{ row }">
            <span class="prize-cell">
              <span class="prize-icon">{{ prizeEmoji(row.prizeType) }}</span>
              {{ row.prizeName }}
              <el-tag v-if="row.couponName" type="warning" size="small" effect="plain" style="margin-left:8px">{{ row.couponName }}</el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="prizeType" label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.prizeType)" size="small">{{ typeLabel(row.prizeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="消耗" width="80">
          <template #default="{ row }">¥{{ row.spinCost }}</template>
        </el-table-column>
        <el-table-column label="余额奖励" width="100">
          <template #default="{ row }">
            <span v-if="row.balanceAmount" class="balance-reward">+¥{{ row.balanceAmount }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ $fmt(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadRecords"
          @size-change="onSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyRecords } from '@/api/lottery'

const records = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

function prizeEmoji(type) {
  const map = { COUPON: '🎟️', BALANCE: '💰', THANKS: '😊', PHYSICAL: '📦' }
  return map[type] || '🎁'
}
function typeLabel(type) {
  const map = { COUPON: '优惠券', BALANCE: '余额', THANKS: '谢谢参与', PHYSICAL: '实物' }
  return map[type] || type
}
function typeTag(type) {
  const map = { COUPON: 'success', BALANCE: 'danger', THANKS: 'info', PHYSICAL: 'warning' }
  return map[type] || 'info'
}

async function loadRecords() {
  loading.value = true
  try {
    const res = await getMyRecords(page.value, pageSize.value)
    records.value = res?.records || []
    total.value = res?.total || 0
  } catch { /* ignore */ }
  loading.value = false
}

function onSizeChange(size) {
  pageSize.value = size
  page.value = 1
  loadRecords()
}

onMounted(loadRecords)
</script>

<style scoped>
.records-page { padding: 40px 20px; min-height: 80vh; background: #f8fafc; }
.container { max-width: 800px; margin: 0 auto; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.header h2 { font-size: 22px; font-weight: 700; color: #1e293b; }
.back-link { color: #6366f1; text-decoration: none; font-size: 14px; font-weight: 600; }
.back-link:hover { text-decoration: underline; }
.prize-cell { display: flex; align-items: center; }
.prize-icon { font-size: 18px; margin-right: 6px; }
.balance-reward { color: #16a34a; font-weight: 600; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
