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
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { login, register, sendVerificationCode } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter(); const route = useRoute(); const userStore = useUserStore()
const loginRef = ref(null); const regRef = ref(null)
const tab = ref('login'); const isLogin = computed(() => tab.value === 'login')
const cd = ref(0); let timer = null

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', phone: '', code: '' })
const loginRules = { username: [{ required: true, message: '请输入用户名' }], password: [{ required: true, message: '请输入密码' }] }
const regRules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }],
  phone: [{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误' }],
  code: [{ required: true, message: '请输入验证码' }]
}

const onTabChange = () => { if (isLogin.value) loginRef.value?.resetFields(); else regRef.value?.resetFields() }
const handleLogin = async () => {
  if (!loginRef.value) return
  await loginRef.value.validate(async (ok) => {
    if (!ok) return
    try { const res = await login(loginForm); userStore.setToken(res.token); userStore.setUsername(res.username); ElMessage.success('登录成功'); router.push(route.query.redirect || '/') } catch (e) { ElMessage.error(e?.message||'登录失败') }
  })
}
const sendCode = async () => {
  if (!regForm.phone) return ElMessage.warning('请输入手机号')
  try { await sendVerificationCode(regForm.phone); ElMessage.success('已发送'); cd.value = 60; timer = setInterval(() => { cd.value--; if (cd.value <= 0) clearInterval(timer) }, 1000) } catch { ElMessage.error('发送失败') }
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
