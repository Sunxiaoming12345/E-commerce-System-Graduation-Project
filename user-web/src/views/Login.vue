<template>
  <div class="login-page">
    <div class="login-card">
      <h2>{{ isLogin ? '登录' : '注册' }}</h2>

      <el-tabs v-model="tab" @tab-change="onTabChange">
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>

      <el-form v-if="isLogin" :model="loginForm" :rules="loginRules" ref="loginRef">
        <el-form-item prop="username"><el-input v-model="loginForm.username" placeholder="用户名" size="large" /></el-form-item>
        <el-form-item prop="password"><el-input v-model="loginForm.password" type="password" placeholder="密码" show-password size="large" /></el-form-item>
        <el-form-item><el-button type="primary" size="large" class="full-btn" @click="handleLogin">登录</el-button></el-form-item>
        <div style="text-align:right"><el-button link type="primary" @click="showForgot=true">忘记密码</el-button></div>
      </el-form>

      <el-form v-else :model="regForm" :rules="regRules" ref="regRef">
        <el-form-item prop="username"><el-input v-model="regForm.username" placeholder="用户名" size="large" /></el-form-item>
        <el-form-item prop="password"><el-input v-model="regForm.password" type="password" placeholder="密码" show-password size="large" /></el-form-item>
        <el-form-item prop="phone"><el-input v-model="regForm.phone" placeholder="手机号" size="large" /></el-form-item>
        <el-form-item prop="code">
          <el-row :gutter="10" style="width:100%">
            <el-col :span="16"><el-input v-model="regForm.code" placeholder="验证码" size="large" /></el-col>
            <el-col :span="8"><el-button size="large" class="full-btn" :disabled="cd>0" @click="sendCode">{{ cd>0?`${cd}s`:'发送' }}</el-button></el-col>
          </el-row>
        </el-form-item>
        <el-form-item><el-button type="primary" size="large" class="full-btn" @click="handleRegister">注册</el-button></el-form-item>
      </el-form>
    </div>

    <!-- 忘记密码弹窗 -->
    <el-dialog v-model="showForgot" title="重置密码" width="380px" center>
      <el-form :model="forgotForm" label-width="100px">
        <el-form-item label="手机号"><el-input v-model="forgotForm.phone" placeholder="请输入注册手机号" /></el-form-item>
        <el-form-item label="验证码">
          <el-row :gutter="10" style="width:100%">
            <el-col :span="15"><el-input v-model="forgotForm.code" placeholder="验证码" /></el-col>
            <el-col :span="9"><el-button :disabled="cd2>0" @click="sendForgotCode" style="width:100%">{{ cd2>0?`${cd2}s`:'发送' }}</el-button></el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="新密码"><el-input v-model="forgotForm.newPassword" type="password" show-password placeholder="至少6位" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showForgot=false">取消</el-button><el-button type="primary" @click="handleReset">重置密码</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { login, register, sendVerificationCode } from '@/api/auth'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter(); const route = useRoute(); const userStore = useUserStore()
const loginRef = ref(null); const regRef = ref(null)
const tab = ref('login'); const isLogin = computed(() => tab.value === 'login')
const cd = ref(0); const cd2 = ref(0); let timer = null; let timer2 = null

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', phone: '', code: '' })
const loginRules = { username: [{ required: true, message: '请输入用户名' }], password: [{ required: true, message: '请输入密码' }] }
const regRules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }],
  phone: [{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误' }],
  code: [{ required: true, message: '请输入验证码' }]
}

const showForgot = ref(false)
const forgotForm = reactive({ phone: '', code: '', newPassword: '' })

const onTabChange = () => { if (isLogin.value) loginRef.value?.resetFields(); else regRef.value?.resetFields() }
const handleLogin = async () => {
  if (!loginRef.value) return
  await loginRef.value.validate(async (ok) => {
    if (!ok) return
    try { const res = await login(loginForm); userStore.setToken(res.token); userStore.setUsername(res.username); ElMessage.success('登录成功'); router.push(route.query.redirect || '/') } catch (e) { ElMessage.error(e?.message || '登录失败') }
  })
}
const sendCode = async () => {
  if (!regForm.phone) return ElMessage.warning('请输入手机号')
  try { await sendVerificationCode(regForm.phone); ElMessage.success('已发送'); cd.value = 60; timer = setInterval(() => { cd.value--; if (cd.value <= 0) clearInterval(timer) }, 1000) } catch { ElMessage.error('发送失败') }
}
const sendForgotCode = async () => {
  if (!forgotForm.phone) return ElMessage.warning('请输入手机号')
  try { await sendVerificationCode(forgotForm.phone); ElMessage.success('已发送'); cd2.value = 60; timer2 = setInterval(() => { cd2.value--; if (cd2.value <= 0) clearInterval(timer2) }, 1000) } catch { ElMessage.error('发送失败') }
}
const handleReset = async () => {
  try { await request.post('/user/reset-password', forgotForm); ElMessage.success('密码已重置，请登录'); showForgot.value = false } catch (e) { ElMessage.error(e.message||'重置失败') }
}
const handleRegister = async () => {
  if (!regRef.value) return
  await regRef.value.validate(async (ok) => {
    if (!ok) return
    try { await register(regForm); ElMessage.success('注册成功'); tab.value = 'login' } catch (e) { ElMessage.error(e?.message||'注册失败') }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(160deg, #faf9f7 0%, #f5f0eb 50%, #ede4d8 100%);
}
.login-card {
  width: 400px; padding: 40px 36px; background: var(--surface);
  border-radius: var(--radius); box-shadow: var(--shadow-lg);
}
.login-card h2 { text-align: center; font-size: 24px; font-weight: 700; margin-bottom: 20px; }
.full-btn { width: 100%; }
</style>
