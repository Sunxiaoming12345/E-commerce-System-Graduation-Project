<template>
  <div class="seckill-page">
    <div class="seckill-banner">
      <h1>⚡ 限时秒杀</h1>
      <p>超值好物，限时抢购，手慢无！</p>
    </div>

    <div class="seckill-grid" v-if="list.length > 0">
      <div class="seckill-card" v-for="item in list" :key="item.id"
           :class="{ upcoming: item.status === 1, active: item.status === 2, ended: item.status === 3 }">
        <div class="card-badge" v-if="item.status === 1">即将开始</div>
        <div class="card-badge active" v-else-if="item.status === 2">抢购中</div>
        <div class="card-badge ended" v-else>已结束</div>

        <div class="card-img">
          <img :src="item.productImage || '/vite.svg'" :alt="item.productName" />
        </div>
        <div class="card-info">
          <div class="card-name">{{ item.productName }}</div>
          <div class="card-price">
            <span class="seckill-price">¥{{ item.seckillPrice }}</span>
          </div>
          <div class="card-stock">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: percent(item) + '%' }"></div>
            </div>
            <span>剩余 {{ item.stock }} 件</span>
          </div>

          <!-- 倒计时 -->
          <div class="countdown" v-if="item.status === 1">
            <span>{{ countdownText(item) }}</span>
          </div>
          <div class="countdown active" v-else-if="item.status === 2">
            <span>距结束 {{ countdownText(item) }}</span>
          </div>

          <button class="buy-btn" v-if="item.status === 1" disabled>未开始</button>
          <button class="buy-btn active" v-else-if="item.status === 2" @click="doBuy(item)">⚡ 立即抢购</button>
          <button class="buy-btn ended" v-else disabled>已结束</button>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无秒杀活动" :image-size="120" />

    <!-- 结果弹窗 -->
    <el-dialog v-model="dialog.visible" title="秒杀结果" width="360px" center>
      <div class="result-box" :class="{ fail: !dialog.ok }">
        <div class="result-icon">{{ dialog.ok ? '🎉' : '😞' }}</div>
        <div class="result-msg">{{ dialog.msg }}</div>
      </div>
      <template #footer v-if="dialog.ok">
        <el-button @click="dialog.visible = false">关闭</el-button>
        <el-button type="primary" @click="goOrders">查看订单</el-button>
      </template>
      <template #footer v-else>
        <el-button @click="dialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const list = ref([])
const dialog = ref({ visible: false, ok: false, msg: '' })
let timer = null

function goOrders() {
  dialog.value.visible = false
  router.push('/orders')
}

async function loadList() {
  try {
    const data = await request.get('/user/seckill/list')
    list.value = Array.isArray(data) ? data : []
  } catch {}
}

function percent(item) {
  if (!item.originStock || item.originStock === 0) return 0
  return Math.max(0, Math.round(item.stock / item.originStock * 100))
}

function countdownText(item) {
  const target = item.status === 1 ? new Date(item.startTime).getTime() : new Date(item.endTime).getTime()
  const diff = target - Date.now()
  if (diff <= 0) return item.status === 1 ? '即将开始...' : '已结束'
  const h = Math.floor(diff / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

async function doBuy(item) {
  try {
    const data = await request.post(`/user/seckill/${item.id}/buy`)
    dialog.value.visible = true
    if (data && data.result === 1) {
      dialog.value.ok = true
      dialog.value.msg = '秒杀成功！订单已生成，去我的订单查看'
      loadList()
    } else {
      dialog.value.ok = false
      dialog.value.msg = data?.msg || '已抢完'
    }
  } catch (e) {
    ElMessage.error(e.message || '抢购失败')
  }
}

onMounted(() => {
  loadList()
  timer = setInterval(() => { list.value = [...list.value] }, 1000)
})
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.seckill-page { max-width: 1200px; margin: 0 auto; padding: 20px; }
.seckill-banner { text-align: center; padding: 40px 0 30px; }
.seckill-banner h1 { font-size: 32px; color: #e74c3c; }
.seckill-banner p { color: #666; margin-top: 8px; }
.seckill-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 20px; }
.seckill-card { position: relative; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,.08); transition: .2s; }
.seckill-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,.12); }
.card-badge { position: absolute; top: 10px; left: 10px; padding: 4px 10px; border-radius: 20px; font-size: 12px; background: #f39c12; color: #fff; z-index: 1; }
.card-badge.active { background: #e74c3c; animation: pulse 1.5s infinite; }
.card-badge.ended { background: #95a5a6; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.6} }
.card-img { height: 180px; background: #f8f9fa; display: flex; align-items: center; justify-content: center; }
.card-img img { max-height: 140px; max-width: 100%; }
.card-info { padding: 14px; }
.card-name { font-size: 15px; font-weight: 600; margin-bottom: 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.seckill-price { font-size: 22px; font-weight: 700; color: #e74c3c; }
.progress-bar { height: 6px; background: #eee; border-radius: 3px; margin: 8px 0 4px; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #e74c3c, #f39c12); border-radius: 3px; transition: .5s; }
.card-stock { font-size: 12px; color: #999; }
.countdown { margin-top: 10px; font-size: 13px; color: #f39c12; font-weight: 600; }
.countdown.active { color: #e74c3c; }
.buy-btn { width: 100%; margin-top: 10px; padding: 10px; border: none; border-radius: 8px; font-size: 15px; cursor: pointer; transition: .2s; }
.buy-btn:disabled { background: #ccc; color: #999; cursor: not-allowed; }
.buy-btn.active { background: linear-gradient(135deg, #e74c3c, #c0392b); color: #fff; }
.buy-btn.ended { background: #f5f5f5; color: #999; }
.result-box { text-align: center; padding: 20px; }
.result-icon { font-size: 48px; margin-bottom: 12px; }
.result-msg { font-size: 16px; color: #333; }
</style>
