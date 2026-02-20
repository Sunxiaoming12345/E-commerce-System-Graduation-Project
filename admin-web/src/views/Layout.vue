<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">后台管理</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1a1d29"
        text-color="#a0aec0"
        active-text-color="#5eead4"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Menu /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/payments">
          <el-icon><Wallet /></el-icon>
          <span>支付记录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container direction="vertical">
      <el-header class="header">
        <span class="page-title">{{ pageTitle }}</span>
        <div class="header-right">
          <span class="username">{{ userStore.username || '管理员' }}</span>
          <el-button type="danger" link @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { DataAnalysis, Goods, List, Menu, Wallet } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title || '工作台')

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background: #1a1d29;
  height: 100vh;
}
.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  font-weight: 700;
  font-size: 18px;
  color: #5eead4;
  border-bottom: 1px solid #2d3340;
}
.el-menu {
  border-right: none;
}
.header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,.08);
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a202c;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.username {
  color: #64748b;
  font-size: 14px;
}
.main {
  background: #f1f5f9;
  padding: 20px;
  overflow-auto: auto;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
