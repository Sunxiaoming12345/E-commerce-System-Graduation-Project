<template>
  <div class="lottery-page">
    <h2 class="page-title anim-fade-up">🎰 幸运抽奖</h2>

    <!-- 状态栏 -->
    <div class="lottery-status anim-fade-up">
      <div class="status-card">
        <span class="status-label">当前余额</span>
        <span class="status-value">¥{{ balance }}</span>
      </div>
      <div class="status-card">
        <span class="status-label">抽奖消耗</span>
        <span class="status-value">¥{{ spinCost }}</span>
      </div>
      <button
        class="spin-btn"
        :disabled="spinning || !canSpin"
        @click="handleSpin"
      >
        {{ spinBtnText }}
      </button>
    </div>

    <!-- CS2 风格开箱区 -->
    <div class="case-section anim-scale-in">
      <!-- 指针指示器 -->
      <div class="case-pointer">
        <span class="pointer-arrow">▼</span>
        <span class="pointer-line"></span>
      </div>

      <!-- 视窗（overflow:hidden 裁剪） -->
      <div class="case-window" ref="windowRef">
        <!-- 加载占位 -->
        <div v-if="segments.length === 0" class="case-empty">加载中...</div>

        <!-- 滚动轨道 — 使用 transform: translateX 驱动 -->
        <div
          v-else
          class="case-track"
          :class="{ 'is-spinning': spinning }"
          :style="trackStyle"
        >
          <div
            v-for="(prize, i) in displayPrizes"
            :key="i"
            class="prize-card"
            :class="{ 'prize-win': !spinning && winPrizeId != null && prize.id === winPrizeId }"
          >
            <div class="prize-img-box">
              <img v-if="prize.prizeImage" :src="prize.prizeImage" alt="" />
              <span v-else class="prize-emoji">{{ prizeEmoji(prize.prizeType) }}</span>
            </div>
            <span class="prize-card-name">{{ prize.prizeName }}</span>
            <span class="prize-card-type">{{ typeLabel(prize.prizeType) }}</span>
          </div>
        </div>
      </div>

      <!-- 两侧渐变遮罩 -->
      <div class="case-mask case-mask-left" />
      <div class="case-mask case-mask-right" />

      <!-- 奖品详情入口 -->
      <div class="prize-link-row">
        <router-link to="/prizes" class="prize-link">
          <span>🎁</span> 查看所有奖品详情 <span>→</span>
        </router-link>
      </div>
    </div>

    <!-- 中奖弹窗 -->
    <el-dialog v-model="resultVisible" title="🎉 抽奖结果" width="380px" center :close-on-click-modal="false">
      <div class="result-content">
        <div class="result-icon">{{ resultIcon }}</div>
        <div class="result-prize">{{ resultName }}</div>
        <div class="result-detail" v-if="resultDetail">{{ resultDetail }}</div>
        <div class="result-balance" v-if="newBalance !== null">剩余余额：<strong>¥{{ newBalance }}</strong></div>
      </div>
      <template #footer>
        <el-button type="primary" @click="resultVisible = false">知道了</el-button>
      </template>
    </el-dialog>

    <!-- 抽奖记录入口 -->
    <div class="records-entry anim-fade-up">
      <router-link to="/lottery-records" class="records-link">
        <span>📋</span> 查看我的抽奖记录 <span>→</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getPrizes, getSpinCost, checkSpinStatus, doSpin } from '@/api/lottery'
import { getBalance } from '@/api/balance'

// ============================================================
// 常量
// ============================================================
const CARD_WIDTH = 150
const CARD_GAP = 16
const CARD_UNIT = CARD_WIDTH + CARD_GAP // 166
const DISPLAY_LAPS = 24 // 奖品重复遍数（足够长，确保动画不越界）

// ============================================================
// 响应式状态
// ============================================================
const balance = ref('0')
const spinCost = ref('0')
const canSpin = ref(false)
const dailyLimit = ref(0)
const todaySpins = ref(0)
const spinning = ref(false)
const segments = ref([])
const resultVisible = ref(false)
const resultName = ref('')
const resultDetail = ref('')
const newBalance = ref(null)
const winPrizeId = ref(null)

// CS2 轨道动画
const trackOffset = ref(0)       // translateX 像素值
const windowRef = ref(null)
const windowWidth = ref(400)
let animFrameId = null

// ============================================================
// 计算属性
// ============================================================
const spinBtnText = computed(() => {
  if (spinning.value) return '抽奖中...'
  if (!canSpin.value) {
    if (dailyLimit.value === 0) return '开始抽奖'
    return `今日已抽${todaySpins.value}/${dailyLimit.value}次`
  }
  if (dailyLimit.value === 0) return '开始抽奖'
  return `开始抽奖（剩余${dailyLimit.value - todaySpins.value}次）`
})

const displayPrizes = computed(() => {
  const arr = []
  for (let lap = 0; lap < DISPLAY_LAPS; lap++) {
    for (let i = 0; i < segments.value.length; i++) {
      arr.push({ ...segments.value[i], _key: `${lap}-${i}` })
    }
  }
  return arr
})

const resultIcon = computed(() => {
  if (!resultName.value) return '🎁'
  if (resultName.value === '谢谢参与') return '😅'
  if (resultName.value.includes('优惠券') || resultName.value.includes('券') || resultName.value.includes('满')) return '🎟️'
  if (resultName.value.includes('余额') || resultName.value.includes('元')) return '💰'
  if (resultName.value.includes('手机') || resultName.value.includes('电脑') || resultName.value.includes('笔记本')) return '📱'
  return '🎁'
})

/* trackStyle — translateX + translateZ(0) 确保 GPU 合成层 */
const trackStyle = computed(() => ({
  transform: `translateX(${trackOffset.value}px) translateZ(0)`,
}))

// ============================================================
// 工具函数
// ============================================================
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

// ============================================================
// 数据加载
// ============================================================
async function loadData() {
  try {
    const [ps, c, s] = await Promise.all([getPrizes(), getSpinCost(), checkSpinStatus()])
    segments.value = (ps || [])
    if (segments.value.length === 0) {
      segments.value = [{ id: 0, prizeName: '谢谢参与', prizeType: 'THANKS' }]
    }
    spinCost.value = String(c || '0')
    canSpin.value = s?.canSpin ?? false
    dailyLimit.value = s?.dailyLimit ?? 0
    todaySpins.value = s?.todaySpins ?? 0
  } catch { /* ignore */ }
  try { balance.value = String(await getBalance() || '0') } catch { /* ignore */ }
}


// ============================================================
// 视窗尺寸
// ============================================================
function updateWindowWidth() {
  if (windowRef.value) {
    windowWidth.value = windowRef.value.clientWidth
  }
}

// ============================================================
// 初始居中
// ============================================================
function centerView() {
  if (segments.value.length === 0) return
  const realCount = segments.value.length
  const winW = windowWidth.value || 400
  const midLap = Math.floor(DISPLAY_LAPS / 2)
  const centerIdx = midLap * realCount + Math.floor(realCount / 2)
  // 将中间卡片居中于视窗
  trackOffset.value = winW / 2 - (centerIdx * CARD_UNIT + CARD_WIDTH / 2)
}

// ============================================================
// CS2 风格动画 — transform: translateX
// ============================================================

/**
 * 使用 requestAnimationFrame 驱动 translateX
 * @param {number} from  起始 translateX
 * @param {number} to    目标 translateX
 * @param {number} duration 毫秒
 * @param {Function} onComplete
 */
function animateTrack(from, to, duration, onComplete) {
  const distance = to - from
  if (Math.abs(distance) < 0.5) {
    trackOffset.value = to
    if (onComplete) onComplete()
    return
  }

  const startTime = performance.now()

  function step(now) {
    const elapsed = now - startTime
    const t = Math.min(elapsed / duration, 1)
    // CS2 缓动：极快起步 + 长尾减速（power=4）
    const eased = 1 - Math.pow(1 - t, 4)
    trackOffset.value = from + distance * eased

    if (t < 1) {
      animFrameId = requestAnimationFrame(step)
    } else {
      trackOffset.value = to
      if (onComplete) onComplete()
    }
  }

  animFrameId = requestAnimationFrame(step)
}

/**
 * 抽奖主流程
 */
async function handleSpin() {
  if (spinning.value || !canSpin.value) return
  spinning.value = true
  winPrizeId.value = null

  try {
    // 1. 先乐观扣减抽奖消耗（动画期间显示扣减后的余额）
    const beforeBalance = parseFloat(balance.value) || 0
    const spinCostNum = parseFloat(spinCost.value) || 0
    const afterDeduct = Math.max(0, beforeBalance - spinCostNum)

    // 2. 调用后端抽奖接口
    const result = await doSpin()

    // 动画期间先显示扣减抽奖消耗后的余额，不显示中奖加上的余额
    balance.value = String(afterDeduct)
    newBalance.value = result.newBalance != null ? result.newBalance : null

    if (segments.value.length === 0) {
      spinning.value = false
      ElMessage.error('奖品数据未加载，请刷新页面')
      return
    }

    updateWindowWidth()
    const realCount = segments.value.length
    const totalWidth = realCount * CARD_UNIT
    const winW = windowWidth.value || 400

    // 2. 找到中奖奖品在 segments 中的索引
    const winId = Number(result.prizeId)
    let winIdx = segments.value.findIndex(s => Number(s.id) === winId)
    if (winIdx < 0) winIdx = segments.value.findIndex(s => s.prizeName === result.prizeName)
    if (winIdx < 0) winIdx = Math.floor(Math.random() * realCount)

    // 3. 选择目标圈数（中奖卡片落在第几圈的副本）
    const targetLap = Math.floor(DISPLAY_LAPS * 0.4) + Math.floor(Math.random() * Math.floor(DISPLAY_LAPS * 0.15))
    const spinLaps = 5 + Math.floor(Math.random() * 4) // 动画跨 5~8 圈
    const startLap = Math.max(0, targetLap - spinLaps)

    const targetIdx = targetLap * realCount + winIdx
    const startIdx  = startLap * realCount + winIdx

    // 4. 计算 targetOffset：中奖卡片居中
    const targetOffset = winW / 2 - (targetIdx * CARD_UNIT + CARD_WIDTH / 2)

    // 5. 计算 startOffset：中奖卡片从视野右侧外开始
    //    让它在视窗右侧 1.2 倍宽度处（完全不可见）
    let startOffset = winW * 1.2 - (startIdx * CARD_UNIT + CARD_WIDTH / 2)

    // 安全钳：startOffset > 0 会让轨道左边缘和视窗左边缘之间出现空白
    //         此时把轨道左边缘对齐到视窗左边缘
    if (startOffset > 0) startOffset = 0

    // 6. 瞬间跳到起点
    trackOffset.value = startOffset
    await nextTick()

    // 7. 执行 CS2 风格动画（起始 → 目标）
    const duration = 3800 + Math.floor(Math.random() * 1200) // 3.8~5.0 秒
    await new Promise(resolve => {
      animateTrack(startOffset, targetOffset, duration, () => {
        winPrizeId.value = winId
        resolve()
      })
    })

    spinning.value = false

    // 8. 弹窗展示结果
    resultName.value = result.prizeName || '谢谢参与'
    if (result.couponName) resultDetail.value = `获得优惠券：${result.couponName}`
    else if (result.balanceAmount) resultDetail.value = `获得余额：¥${result.balanceAmount}`
    else resultDetail.value = ''
    resultVisible.value = true

    if (result.newBalance != null) balance.value = String(result.newBalance)
    canSpin.value = false
    todaySpins.value++

  } catch (e) {
    if (animFrameId) cancelAnimationFrame(animFrameId)
    spinning.value = false
    ElMessage.error(e.message || '抽奖失败')
  }
}

// ============================================================
// 生命周期
// ============================================================
watch(resultVisible, (v) => { if (!v) { loadData() } })

// 奖品数据加载完成后自动居中
watch(segments, () => {
  nextTick(() => {
    updateWindowWidth()
    centerView()
  })
})

function onResize() {
  updateWindowWidth()
  if (!spinning.value) centerView()
}

onMounted(async () => {
  await loadData()
  nextTick(() => {
    updateWindowWidth()
    centerView()
  })
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  if (animFrameId) cancelAnimationFrame(animFrameId)
})
</script>

<style scoped>
.lottery-page { max-width: 640px; margin: 0 auto; padding: 24px 16px 48px; }
.page-title { text-align: center; font-size: 22px; margin-bottom: 20px; color: var(--primary, #1a1a2e); }

/* ============================================================
   状态栏
   ============================================================ */
.lottery-status { display: flex; align-items: center; justify-content: center; gap: 20px; margin-bottom: 28px; flex-wrap: wrap; }
.status-card { background: #fff; border-radius: var(--radius-sm, 10px); padding: 12px 20px; box-shadow: var(--shadow-sm); text-align: center; }
.status-label { display: block; font-size: 12px; color: #999; margin-bottom: 4px; }
.status-value { font-size: 20px; font-weight: 700; color: var(--primary, #1a1a2e); }
.spin-btn {
  padding: 14px 36px; border: none; border-radius: 50px;
  background: linear-gradient(135deg, #e74c3c, #e91e63); color: #fff;
  font-size: 16px; font-weight: 700; cursor: pointer;
  box-shadow: 0 4px 15px rgba(233, 30, 99, 0.35);
  transition: all .2s;
}
.spin-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(233, 30, 99, 0.45); }
.spin-btn:disabled { background: #ccc; cursor: not-allowed; box-shadow: none; color: #fff; }

/* ============================================================
   CS2 风格开箱视窗
   ============================================================ */
.case-section { position: relative; margin: 0 auto 36px; }

/* 指针 */
.case-pointer {
  position: absolute; top: -20px; left: 50%; transform: translateX(-50%);
  z-index: 10; display: flex; flex-direction: column; align-items: center;
  gap: 2px;
}
.pointer-arrow {
  font-size: 24px; color: #ff4444;
  filter: drop-shadow(0 2px 4px rgba(255,0,0,.4));
  animation: pointer-pulse 0.7s ease-in-out infinite;
}
.pointer-line {
  width: 3px; height: 24px;
  background: linear-gradient(180deg, #ff4444, transparent);
  border-radius: 2px;
}
@keyframes pointer-pulse {
  0%, 100% { transform: translateY(0); opacity: 1; }
  50% { transform: translateY(3px); opacity: 0.7; }
}

/* 视窗 — overflow:hidden 裁剪 */
.case-window {
  position: relative; height: 230px;
  overflow: hidden;
  border-radius: 16px;
  /* CS2 暗色背景 */
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 30%, #1a1a2e 70%, #0f3460 100%);
  border: 2px solid #2a2a4a;
  box-shadow: inset 0 0 60px rgba(0,0,0,.5), 0 4px 20px rgba(0,0,0,.3);
}

.case-empty {
  display: flex; align-items: center; justify-content: center;
  height: 100%; color: rgba(255,255,255,.5); font-size: 14px;
}

/* 轨道 — GPU 加速 translateX */
.case-track {
  display: flex; gap: 16px;
  padding: 18px 0;
  /* GPU 合成层 — 动画丝滑 */
  will-change: transform;
  transform: translateZ(0);
}

/* 动画中加一点 motion blur 感觉 */
.case-track.is-spinning .prize-card {
  filter: blur(0.5px);
}

/* 奖品卡片 */
.prize-card {
  flex-shrink: 0; width: 150px; height: 190px;
  background: rgba(255,255,255,.08);
  backdrop-filter: blur(4px);
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,.3);
  display: flex; flex-direction: column; align-items: center;
  padding: 12px 8px; gap: 8px;
  transition: box-shadow .3s, transform .3s, border-color .3s, filter .1s;
  border: 2px solid rgba(255,255,255,.1);
  position: relative;
}
.prize-card:hover { box-shadow: 0 4px 18px rgba(0,0,0,.4); transform: translateY(-2px); }

/* 中奖卡片高亮 */
.prize-card.prize-win {
  border-color: #ff4444 !important;
  background: rgba(255,68,68,.12);
  box-shadow: 0 0 0 5px rgba(255,68,68,.35), 0 8px 30px rgba(255,68,68,.25);
  animation: win-glow 0.5s ease-in-out infinite alternate;
  transform: scale(1.06);
  z-index: 2;
  filter: none !important;
}
@keyframes win-glow {
  0%   { box-shadow: 0 0 0 5px rgba(255,68,68,.35), 0 8px 30px rgba(255,68,68,.25); }
  100% { box-shadow: 0 0 0 10px rgba(255,68,68,.15), 0 12px 40px rgba(255,68,68,.4); }
}

.prize-img-box {
  width: 80px; height: 80px; border-radius: 12px;
  background: rgba(255,255,255,.06);
  display: flex; align-items: center; justify-content: center;
  overflow: hidden;
}
.prize-img-box img { width: 100%; height: 100%; object-fit: cover; }
.prize-emoji { font-size: 40px; }
.prize-card-name {
  font-size: 13px; font-weight: 600; color: rgba(255,255,255,.9); text-align: center;
  line-height: 1.3; display: -webkit-box; -webkit-line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden; word-break: break-all;
}
.prize-card-type {
  font-size: 11px; color: rgba(255,255,255,.5); padding: 2px 10px;
  background: rgba(255,255,255,.08); border-radius: 20px;
}

/* 两侧渐变遮罩 */
.case-mask {
  position: absolute; top: 0; bottom: 0; width: 70px;
  pointer-events: none; z-index: 3;
}
.case-mask-left {
  left: 0;
  background: linear-gradient(90deg, rgba(26,26,46,.95) 0%, transparent 100%);
}
.case-mask-right {
  right: 0;
  background: linear-gradient(270deg, rgba(26,26,46,.95) 0%, transparent 100%);
}

/* 奖品详情入口 */
.prize-link-row { text-align: center; margin-top: 18px; }
.prize-link {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 10px 24px; border-radius: 25px;
  background: rgba(255,255,255,.06);
  border: 1px solid rgba(255,255,255,.12);
  color: #e67e22; text-decoration: none;
  font-size: 14px; font-weight: 600;
  transition: all .25s;
  box-shadow: 0 2px 8px rgba(0,0,0,.2);
}
.prize-link:hover {
  background: rgba(255,255,255,.1);
  border-color: #e67e22;
  box-shadow: 0 4px 16px rgba(230,126,34,.25);
  transform: translateY(-2px);
}

/* ============================================================
   弹窗
   ============================================================ */
.result-content { text-align: center; padding: 16px 0; }
.result-icon { font-size: 56px; margin-bottom: 12px; }
.result-prize { font-size: 22px; font-weight: 700; color: var(--primary, #1a1a2e); margin-bottom: 8px; }
.result-detail { font-size: 14px; color: #666; margin-bottom: 4px; }
.result-balance { font-size: 14px; color: #666; margin-top: 8px; padding-top: 8px; border-top: 1px dashed #eee; }
.result-balance strong { color: #e74c3c; font-size: 16px; }

/* ============================================================
   记录表格
   ============================================================ */
.records-entry { text-align: center; margin-top: 32px; }
.records-link {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 12px 28px; border-radius: 25px;
  background: rgba(255,255,255,.06);
  border: 1px solid rgba(255,255,255,.12);
  color: #6366f1; text-decoration: none;
  font-size: 15px; font-weight: 600;
  transition: all .25s;
}
.records-link:hover { background: rgba(99,102,241,.12); border-color: #6366f1; transform: translateY(-2px); }

/* ============================================================
   入场动画
   ============================================================ */
.anim-fade-up { animation: fade-up .5s ease both; }
.anim-scale-in { animation: scale-in .45s ease both; }

@keyframes fade-up { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
@keyframes scale-in { from { opacity: 0; transform: scale(.94); } to { opacity: 1; transform: scale(1); } }
</style>
