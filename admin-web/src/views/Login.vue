<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="title">后台管理系统</h1>
      <p class="subtitle">请使用管理员账号登录</p>
      <el-form ref="formRef" :model="form" :rules="rules" class="form" @submit.prevent="onSubmit">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="onSubmit">
            登录
          </el-button>
        </el-form-item>
      </el-form>
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
  } catch (e) {
    // error already shown in request interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1d29 0%, #2d3340 50%, #1e293b 100%);
}
.login-card {
  width: 400px;
  padding: 48px 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0,0,0,.25);
}
.title {
  font-size: 24px;
  font-weight: 700;
  color: #1a202c;
  text-align: center;
  margin-bottom: 8px;
}
.subtitle {
  font-size: 14px;
  color: #64748b;
  text-align: center;
  margin-bottom: 32px;
}
.form {
  margin-top: 8px;
}
.submit-btn {
  width: 100%;
}
</style>
