<template>
  <div class="profile">
    <div class="container">
      <div class="profile-top">
        <el-avatar :size="64" :src="userStore.userInfo?.avatar">{{ (userStore.userInfo?.name || userStore.username || 'U').charAt(0) }}</el-avatar>
        <div class="top-info">
          <h2>{{ userStore.userInfo?.name || userStore.username || '用户' }}</h2>
          <p>{{ userStore.userInfo?.phone || '未绑定手机号' }}</p>
        </div>
        <div class="top-acts">
          <el-button @click="openEdit">编辑资料</el-button>
          <el-button @click="openPwd">修改密码</el-button>
          <el-button plain @click="handleLogout">退出登录</el-button>
        </div>
      </div>

      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-num">{{ orderStats.totalOrders ?? 0 }}</div>
          <div class="stat-lbl">全部订单</div>
        </div>
        <div class="stat-card">
          <div class="stat-num">&yen;{{ fmt(orderStats.totalConsumption) }}</div>
          <div class="stat-lbl">总消费</div>
        </div>
        <div class="stat-card">
          <div class="stat-num">&yen;{{ fmt(balance) }}</div>
          <div class="stat-lbl">账户余额</div>
        </div>
      </div>

      <div class="info-card">
        <h3>常用收货信息</h3>
        <div class="info-row"><span>收货人</span><span>{{ userStore.userInfo?.defaultReceiver || '未设置' }}</span></div>
        <div class="info-row"><span>联系电话</span><span>{{ userStore.userInfo?.defaultPhone || '未设置' }}</span></div>
        <div class="info-row"><span>收货地址</span><span>{{ userStore.userInfo?.defaultAddress || '未设置' }}</span></div>
      </div>

      <el-dialog v-model="dialogVisible" title="编辑个人资料" width="440px">
        <el-form :model="form" label-width="100px">
          <el-form-item label="头像">
            <el-upload :show-file-list="false" :before-upload="handleAvatar" accept="image/*">
              <img v-if="form.avatar" :src="form.avatar" style="width:64px;height:64px;border-radius:50%;cursor:pointer" />
              <el-icon v-else :size="64" style="cursor:pointer"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="默认收货人"><el-input v-model="form.defaultReceiver" /></el-form-item>
          <el-form-item label="默认联系电话"><el-input v-model="form.defaultPhone" /></el-form-item>
          <el-form-item label="默认收货地址"><el-input v-model="form.defaultAddress" type="textarea" :rows="3" /></el-form-item>
        </el-form>
        <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm">保存</el-button></template>
      </el-dialog>

      <!-- 修改密码弹窗 -->
      <el-dialog v-model="pwdVisible" title="修改密码" width="400px">
        <el-form :model="pwdForm" label-width="100px">
          <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
        </el-form>
        <template #footer><el-button @click="pwdVisible=false">取消</el-button><el-button type="primary" @click="submitPwd">确定</el-button></template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { onMounted, ref, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { getBalance } from '@/api/balance'
import { getOrderStats } from '@/api/orders'
import request from '@/utils/request'

const router = useRouter(); const userStore = useUserStore()
const balance = ref(0); const orderStats = ref({ totalOrders: 0, totalConsumption: 0 })
const dialogVisible = ref(false); const pwdVisible = ref(false)
const form = ref({ name: '', phone: '', avatar: '', defaultReceiver: '', defaultPhone: '', defaultAddress: '' })
const pwdForm = ref({ oldPassword: '', newPassword: '' })

const fmt = (v) => { const n = Number(v); return Number.isNaN(n) ? '0.00' : n.toFixed(2) }

watch(() => userStore.userInfo, (info) => { if (info) { form.value = { name: info.name || '', phone: info.phone || '', avatar: info.avatar || '', defaultReceiver: info.defaultReceiver || '', defaultPhone: info.defaultPhone || '', defaultAddress: info.defaultAddress || '' } } }, { immediate: true })

const openEdit = () => { dialogVisible.value = true }
const openPwd = () => { pwdVisible.value = true; pwdForm.value = { oldPassword: '', newPassword: '' } }

const handleAvatar = async (file) => {
  const fd = new FormData(); fd.append('file', file)
  try {
    const data = await request.post('/user/avatar', fd, { timeout: 60000 })
    if (data?.url) {
      form.value.avatar = data.url
      await userStore.updateInfo({ avatar: data.url })
      ElMessage.success('头像上传成功')
    }
  } catch (e) { ElMessage.error('上传失败') }
  return false
}

const submitForm = async () => {
  try { await userStore.updateInfo(form.value); ElMessage.success('更新成功'); dialogVisible.value = false } catch (e) { ElMessage.error(e.message||'更新失败') }
}
const submitPwd = async () => {
  try { await request.put('/user/password', pwdForm.value); ElMessage.success('密码已修改'); pwdVisible.value = false } catch (e) { ElMessage.error(e.message||'修改失败') }
}
const handleLogout = () => { userStore.logout(); router.push('/login') }

onMounted(async () => {
  userStore.fetchUserInfo()
  try { balance.value = await getBalance() } catch { /* */ }
  try { const r = await getOrderStats(); if (r) orderStats.value = { totalOrders: r.totalOrders??0, totalConsumption: r.totalConsumption??0 } } catch { /* */ }
})
</script>

<style scoped>
.profile { padding: 0; }
.container { max-width: 760px; margin: 0 auto; padding: 0 24px; }

.profile-top {
  display: flex; align-items: center; gap: 20px;
  background: var(--surface); border-radius: var(--radius);
  padding: 28px 32px; border: 1px solid var(--border-light);
  margin-bottom: 20px;
}
.avatar { background: var(--primary); color: #fff; font-size: 24px; font-weight: 700; }
.top-info { flex: 1; }
.top-info h2 { font-size: 22px; font-weight: 700; margin-bottom: 4px; }
.top-info p { font-size: 14px; color: var(--text-secondary); }
.top-acts { display: flex; gap: 10px; }

.stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin-bottom: 20px; }
.stat-card {
  background: var(--surface); border-radius: var(--radius); padding: 24px;
  border: 1px solid var(--border-light); text-align: center;
}
.stat-num { font-size: 28px; font-weight: 700; color: var(--text); margin-bottom: 4px; }
.stat-lbl { font-size: 13px; color: var(--text-secondary); }

.info-card {
  background: var(--surface); border-radius: var(--radius); padding: 24px 28px;
  border: 1px solid var(--border-light);
}
.info-card h3 { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.info-row { display: flex; margin-bottom: 12px; font-size: 14px; }
.info-row span:first-child { width: 90px; color: var(--text-secondary); }
.info-row span:last-child { color: var(--text); }
</style>
