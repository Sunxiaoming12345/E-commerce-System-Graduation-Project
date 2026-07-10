<template>
  <div class="prize-list-page">
    <!-- 返回按钮 -->
    <div class="back-row">
      <router-link to="/lottery" class="back-link">← 返回抽奖</router-link>
    </div>

    <h2 class="page-title">🎁 奖池一览</h2>
    <p class="page-desc">以下为本期可抽取的全部奖品，每次抽奖消耗 ¥{{ spinCost }}</p>

    <!-- 奖品网格 -->
    <div class="prize-grid" v-loading="loading">
      <div v-for="prize in prizes" :key="prize.id" class="prize-detail-card">
        <div class="card-img-box">
          <img v-if="prize.prizeImage" :src="prize.prizeImage" alt="" />
          <span v-else class="card-emoji">{{ prizeEmoji(prize.prizeType) }}</span>
        </div>
        <div class="card-body">
          <h4 class="card-name">{{ prize.prizeName }}</h4>
          <div class="card-meta">
            <el-tag :type="typeTag(prize.prizeType)" size="small">{{ typeLabel(prize.prizeType) }}</el-tag>
            <span class="card-prob">概率：{{ (prize.probability * 100).toFixed(2) }}%</span>
          </div>
          <div class="card-values" v-if="prize.prizeType === 'BALANCE' && prize.balanceAmount">
            <span class="val-label">金额：</span>
            <span class="val-num">¥{{ prize.balanceAmount }}</span>
          </div>
          <div class="card-values" v-if="prize.prizeType === 'PHYSICAL'">
            <span class="val-label">剩余库存：</span>
            <span class="val-num">{{ prize.remainingStock }}/{{ prize.totalStock }}</span>
          </div>
          <div class="card-values" v-if="prize.prizeType === 'COUPON' && prize.couponName">
            <span class="val-label">关联优惠券：</span>
            <span class="val-num">{{ prize.couponName }}</span>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && prizes.length === 0" description="暂无奖品" />
    </div>

    <!-- 底部按钮 -->
    <div class="bottom-action">
      <router-link to="/lottery" class="go-spin-btn">🎰 去抽奖</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPrizes, getSpinCost } from '@/api/lottery'

const prizes = ref([])
const spinCost = ref('0')
const loading = ref(false)

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

onMounted(async () => {
  loading.value = true
  try {
    const [ps, cost] = await Promise.all([getPrizes(), getSpinCost()])
    prizes.value = ps || []
    spinCost.value = String(cost || '0')
  } catch { /* ignore */ }
  loading.value = false
})
</script>

<style scoped>
.prize-list-page { max-width: 680px; margin: 0 auto; padding: 24px 16px 48px; }

.back-row { margin-bottom: 16px; }
.back-link {
  display: inline-flex; align-items: center; gap: 4px;
  color: #666; text-decoration: none; font-size: 14px;
  padding: 6px 12px; border-radius: 8px; transition: all .2s;
}
.back-link:hover { color: #e74c3c; background: #fff5f5; }

.page-title { text-align: center; font-size: 22px; margin-bottom: 8px; color: var(--primary, #1a1a2e); }
.page-desc { text-align: center; font-size: 14px; color: #999; margin-bottom: 28px; }

/* 网格 */
.prize-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }

.prize-detail-card {
  background: #fff; border-radius: 14px; box-shadow: 0 2px 12px rgba(0,0,0,.07);
  display: flex; gap: 16px; padding: 16px;
  transition: all .25s; border: 2px solid transparent;
}
.prize-detail-card:hover { transform: translateY(-3px); box-shadow: 0 6px 24px rgba(0,0,0,.1); border-color: #f0e0d0; }

.card-img-box {
  width: 90px; height: 90px; border-radius: 12px; flex-shrink: 0;
  background: linear-gradient(135deg, #fff5f5, #fef5e7);
  display: flex; align-items: center; justify-content: center;
  overflow: hidden;
}
.card-img-box img { width: 100%; height: 100%; object-fit: cover; }
.card-emoji { font-size: 44px; }

.card-body { flex: 1; display: flex; flex-direction: column; gap: 8px; min-width: 0; }
.card-name { font-size: 15px; font-weight: 700; color: #333; margin: 0; word-break: break-all; }
.card-meta { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.card-prob { font-size: 12px; color: #999; }
.card-values { display: flex; align-items: center; gap: 6px; font-size: 13px; }
.val-label { color: #999; }
.val-num { color: #e74c3c; font-weight: 600; }

/* 底部 */
.bottom-action { text-align: center; margin-top: 32px; }
.go-spin-btn {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 14px 40px; border-radius: 50px;
  background: linear-gradient(135deg, #e74c3c, #e91e63); color: #fff;
  text-decoration: none; font-size: 16px; font-weight: 700;
  box-shadow: 0 4px 15px rgba(233, 30, 99, 0.35);
  transition: all .25s;
}
.go-spin-btn:hover { transform: translateY(-3px); box-shadow: 0 8px 25px rgba(233, 30, 99, 0.45); }
</style>
