<template>
  <div class="login-page">
    <!-- Decorative grid -->
    <div class="bg-grid"></div>

    <div class="login-card">
      <div class="card-accent"></div>
      <div class="brand">
        <h1 class="title">后台管理</h1>
        <div class="title-rule"></div>
        <p class="subtitle">E-Commerce Administration System</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @submit.prevent="onSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" :prefix-icon="Lock" show-password clearable @keyup.enter="onSubmit" />
        </el-form-item>
        <el-form-item>
          <el-button class="submit-btn" :loading="loading" @click="onSubmit">
            {{ loading ? '' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="card-footer">
        <span class="dot"></span>
        SECURE ACCESS
        <span class="dot"></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { login } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value?.validate().catch(() => {})
  loading.value = true
  try {
    const res = await login(form)
    userStore.setToken(res.token || res.data?.token || '')
    userStore.setUsername(form.username)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } catch (e) {} finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
  position: relative;
}

/* === Background grid texture === */
.bg-grid {
  position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(0,0,0,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,0,0,0.03) 1px, transparent 1px);
  background-size: 48px 48px;
  pointer-events: none;
}

/* === Card === */
.login-card {
  position: relative; z-index: 1;
  width: 420px;
  background: #fff;
  border: 3px solid #111;
  box-shadow: var(--shadow-hard-lg);
  animation: card-slam 0.4s var(--ease);
}

@keyframes card-slam {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.card-accent {
  height: 6px;
  background: repeating-linear-gradient(90deg, #111 0px, #111 12px, #e53935 12px, #e53935 24px);
}

.brand { padding: 40px 40px 0; text-align: center; }

.title {
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 400;
  color: #111;
  letter-spacing: 0.08em;
  margin: 0;
}

.title-rule {
  width: 48px; height: 3px;
  background: #e53935;
  margin: 16px auto;
}

.subtitle {
  font-size: 10px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.2em;
  font-weight: 600;
}

/* === Form === */
.login-form { padding: 32px 40px 24px; }

.login-form :deep(.el-input__wrapper) {
  border: 2px solid #111;
  border-radius: 0;
  box-shadow: var(--shadow-hard-sm);
  background: #fff;
  transition: box-shadow 0.1s, transform 0.1s;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 3px 3px 0 #111;
  transform: translate(-1px, -1px);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 3px 3px 0 #e53935;
  border-color: #e53935;
}

.login-form :deep(.el-input__inner) { color: #111; font-weight: 500; }
.login-form :deep(.el-input__inner::placeholder) { color: #999; }

.submit-btn {
  width: 100%; height: 48px;
  border: 3px solid #111;
  border-radius: 0;
  background: #111;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.4em;
  box-shadow: var(--shadow-hard);
  transition: all 0.1s;
  cursor: pointer;
}

.submit-btn:hover {
  background: #e53935;
  border-color: #e53935;
  box-shadow: 5px 5px 0 #111;
  transform: translate(-2px, -2px);
}

.submit-btn:active {
  box-shadow: 1px 1px 0 #111;
  transform: translate(2px, 2px);
}

/* === Footer === */
.card-footer {
  display: flex; align-items: center; justify-content: center;
  gap: 10px; padding: 0 40px 32px;
  font-size: 9px; font-weight: 700;
  color: #999; letter-spacing: 0.2em;
}

.dot {
  width: 5px; height: 5px;
  background: #e53935;
}
</style>
