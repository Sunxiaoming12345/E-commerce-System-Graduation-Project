<template>
  <div class="coupons-page">
    <div class="container">
      <h2>领券中心</h2>

      <!-- 可领取优惠券 -->
      <div class="section">
        <h3>可领取优惠券</h3>
        <div class="coupon-grid" v-if="availableCoupons.length > 0">
          <div class="coupon-card" v-for="c in availableCoupons" :key="c.couponId">
            <div class="coupon-left">
              <span v-if="c.type === 0" class="amount">&yen;{{ c.discountValue }}</span>
              <span v-else class="amount">{{ (c.discountValue * 10).toFixed(1) }}<small>折</small></span>
              <span class="condition" v-if="c.minAmount > 0">满{{ c.minAmount }}可用</span>
              <span class="condition" v-else>无门槛</span>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ c.name }}</div>
              <div class="coupon-meta">
                <span>有效期：{{ $fmt(c.startTime, 'YYYY-MM-DD') }} ~ {{ $fmt(c.endTime, 'YYYY-MM-DD') }}</span>
              </div>
              <div class="coupon-stock">剩余 {{ c.totalCount - c.usedCount }}/{{ c.totalCount }}</div>
            </div>
            <el-button type="danger" round @click="handleClaim(c.couponId)">立即领取</el-button>
          </div>
        </div>
        <el-empty v-else description="暂无优惠券" :image-size="120" />
      </div>

      <!-- 我的优惠券 -->
      <div class="section">
        <h3>我的优惠券</h3>
        <el-tabs v-model="activeTab" @tab-change="loadMyCoupons">
          <el-tab-pane label="未使用" :name="0" />
          <el-tab-pane label="已使用" :name="1" />
          <el-tab-pane label="已过期" :name="2" />
        </el-tabs>
        <div class="coupon-grid" v-if="myCoupons.length > 0">
          <div class="coupon-card" v-for="c in myCoupons" :key="c.userCouponId" :class="{ used: c.userCouponStatus !== 0 }">
            <div class="coupon-left">
              <span v-if="c.type === 0" class="amount">&yen;{{ c.discountValue }}</span>
              <span v-else class="amount">{{ (c.discountValue * 10).toFixed(1) }}<small>折</small></span>
              <span class="condition" v-if="c.minAmount > 0">满{{ c.minAmount }}可用</span>
              <span class="condition" v-else>无门槛</span>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ c.name }}
                <el-tag v-if="c.isLottery === 1" type="warning" size="small" effect="dark" style="margin-left:6px">抽奖获得</el-tag>
              </div>
              <div class="coupon-meta">
                <span>{{ $fmt(c.startTime, 'YYYY-MM-DD') }} ~ {{ $fmt(c.endTime, 'YYYY-MM-DD') }}</span>
              </div>
            </div>
            <el-tag v-if="c.userCouponStatus === 1" type="info" size="small">已使用</el-tag>
            <el-tag v-else-if="c.userCouponStatus === 2" type="danger" size="small">已过期</el-tag>
          </div>
        </div>
        <el-empty v-else description="暂无优惠券" :image-size="120" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableCoupons, claimCoupon, getMyCoupons } from '@/api/coupons'

const availableCoupons = ref([])
const myCoupons = ref([])
const activeTab = ref(0)

async function loadAvailable() {
  try { availableCoupons.value = await getAvailableCoupons() }
  catch { ElMessage.error('加载可领优惠券失败') }
}

async function loadMyCoupons() {
  try { myCoupons.value = await getMyCoupons(activeTab.value) }
  catch { ElMessage.error('加载我的优惠券失败') }
}

async function handleClaim(couponId) {
  try {
    await claimCoupon(couponId)
    ElMessage.success('领取成功')
    loadAvailable()
    loadMyCoupons()
  } catch (e) {
    ElMessage.error(e?.message || '领取失败')
  }
}

onMounted(() => { loadAvailable(); loadMyCoupons() })
</script>

<style scoped>
.coupons-page { padding: 40px 20px; min-height: 80vh; background: #f8fafc; }
.container { max-width: 960px; margin: 0 auto; }
h2 { font-size: 24px; font-weight: 700; color: #1e293b; margin-bottom: 32px; }
h3 { font-size: 17px; font-weight: 600; color: #334155; margin-bottom: 16px; }
.section { margin-bottom: 40px; }

.coupon-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(420px, 1fr)); gap: 16px; }

.coupon-card {
  display: flex; align-items: center; gap: 16px;
  padding: 20px 24px; border-radius: 12px;
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
  border: 1px solid #fed7aa;
  transition: transform 0.15s, box-shadow 0.15s;
}
.coupon-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(249,115,22,0.12); }
.coupon-card.used { background: #f1f5f9; border-color: #e2e8f0; }
.coupon-card.used:hover { box-shadow: none; transform: none; }

.coupon-left {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-width: 88px; padding-right: 16px; border-right: 1px dashed #fdba74;
}
.coupon-card.used .coupon-left { border-color: #cbd5e1; }

.amount { font-size: 28px; font-weight: 800; color: #ea580c; line-height: 1.2; }
.amount small { font-size: 14px; font-weight: 600; }
.coupon-card.used .amount { color: #94a3b8; }

.condition { font-size: 12px; color: #c2410c; margin-top: 4px; }
.coupon-card.used .condition { color: #94a3b8; }

.coupon-right { flex: 1; min-width: 0; }
.coupon-name { font-weight: 600; font-size: 15px; color: #1e293b; margin-bottom: 6px; }
.coupon-meta { font-size: 12px; color: #78716c; margin-bottom: 4px; }
.coupon-stock { font-size: 12px; color: #ea580c; }
</style>
