<template>
  <el-container class="layout">
    <!-- Sidebar -->
    <el-aside width="230px" class="aside">
      <div class="logo-area">
        <div class="logo-mark">◆</div>
        <div>
          <div class="logo-title">后台管理</div>
          <div class="logo-sub">Admin Console</div>
        </div>
      </div>

      <el-menu :default-active="activeMenu" router class="side-menu">
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon><span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon><span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><List /></el-icon><span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Menu /></el-icon><span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/payments">
          <el-icon><Wallet /></el-icon><span>支付记录</span>
        </el-menu-item>
        <el-menu-item index="/reviews">
          <el-icon><StarFilled /></el-icon><span>评价管理</span>
        </el-menu-item>
        <el-menu-item index="/coupons">
          <el-icon><Ticket /></el-icon><span>优惠券管理</span>
        </el-menu-item>
        <el-menu-item index="/refunds">
          <el-icon><Money /></el-icon><span>退款管理</span>
        </el-menu-item>
      </el-menu>

      <div class="aside-footer">
        <span class="version">V2</span>
      </div>
    </el-aside>

    <!-- Main -->
    <el-container direction="vertical" class="main-container">
      <el-header class="header">
        <span class="page-title">{{ pageTitle }}</span>
        <div class="header-right">
          <div class="user-badge">
            <span class="user-initial">{{ (userStore.username || 'A').charAt(0) }}</span>
            <span class="user-name">{{ userStore.username || '管理员' }}</span>
          </div>
          <el-button class="logout-btn" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
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
import { DataAnalysis, Goods, List, Menu, Money, StarFilled, Ticket, Wallet } from '@element-plus/icons-vue'

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
.layout { height: 100%; }

/* === Sidebar === */
.aside {
  background: #fff;
  height: 100vh;
  display: flex;
  flex-direction: column;
  border-right: 3px solid #111;
}

.logo-area {
  display: flex; align-items: center; gap: 12px;
  padding: 20px 18px;
  border-bottom: 3px solid #111;
}

.logo-mark { font-size: 22px; color: #e53935; }

.logo-title {
  font-family: var(--font-display);
  font-size: 18px; color: #111; letter-spacing: 0.04em; line-height: 1.3;
}

.logo-sub {
  font-size: 9px; color: #999; text-transform: uppercase;
  letter-spacing: 0.12em; font-weight: 700;
}

/* === Menu === */
.side-menu {
  flex: 1; border-right: none; padding: 8px; background: transparent;
}

.side-menu :deep(.el-menu-item) {
  margin-bottom: 2px;
  border: 2px solid transparent;
  color: #555;
  font-size: 13px; font-weight: 600;
  height: 42px; line-height: 42px;
  transition: all 0.08s;
}

.side-menu :deep(.el-menu-item:hover) {
  background: #f2f0ed;
  color: #111;
  border: 2px solid #111;
}

.side-menu :deep(.el-menu-item.is-active) {
  background: #111;
  color: #fff !important;
  font-weight: 700;
}

.side-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #fff;
}

/* === Aside Footer === */
.aside-footer {
  padding: 12px 18px;
  border-top: 3px solid #111;
}

.version {
  display: inline-block;
  font-size: 10px; font-weight: 900;
  color: #fff; background: #111;
  padding: 2px 8px;
}

/* === Main === */
.main-container { background: var(--bg-page); }

/* === Header === */
.header {
  height: 56px;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 28px;
  background: #fff;
  border-bottom: 3px solid #111;
}

.page-title {
  font-family: var(--font-display);
  font-size: 18px; color: #111;
}

.header-right { display: flex; align-items: center; gap: 16px; }

.user-badge {
  display: flex; align-items: center; gap: 8px;
  padding: 4px 14px 4px 4px;
  border: 2px solid #111;
  background: #fff;
}

.user-initial {
  width: 28px; height: 28px;
  background: #111; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 900;
}

.user-name { font-size: 13px; font-weight: 600; color: #111; }

.logout-btn {
  border: 2px solid #111;
  color: #111;
  font-weight: 700;
  font-size: 12px;
  padding: 6px 16px;
  transition: all 0.08s;
}

.logout-btn:hover {
  background: #e53935;
  border-color: #e53935;
  color: #fff;
}

/* === Content === */
.main {
  background: var(--bg-page);
  padding: 28px;
  overflow-y: auto;
  height: calc(100vh - 56px);
}
</style>
