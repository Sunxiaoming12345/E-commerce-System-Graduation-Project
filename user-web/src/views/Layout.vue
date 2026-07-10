<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner">
        <router-link to="/home" class="brand">
          <span class="brand-icon">&#9826;</span>
          <span class="brand-text">邮购商城</span>
        </router-link>

        <div class="header-spacer" />

        <nav class="nav-links">
          <router-link to="/cart" class="nav-item">
            <el-icon :size="20"><ShoppingCart /></el-icon>
            <span class="nav-label">购物车</span>
          </router-link>
          <router-link to="/orders" class="nav-item">
            <el-icon :size="20"><Tickets /></el-icon>
            <span class="nav-label">订单</span>
          </router-link>
          <router-link to="/coupons" class="nav-item">
            <el-icon :size="20"><Present /></el-icon>
            <span class="nav-label">领券</span>
          </router-link>
          <router-link to="/lottery" class="nav-item">
            <el-icon :size="20"><TrophyBase /></el-icon>
            <span class="nav-label">抽奖</span>
          </router-link>
          <router-link to="/seckill" class="nav-item">
            <el-icon :size="20"><Timer /></el-icon>
            <span class="nav-label">秒杀</span>
          </router-link>
          <router-link to="/refunds" class="nav-item">
            <el-icon :size="20"><Wallet /></el-icon>
            <span class="nav-label">退款</span>
          </router-link>
          <router-link to="/profile" class="nav-item">
            <el-icon :size="20"><User /></el-icon>
            <span class="nav-label">{{ userStore.userInfo?.username || '我的' }}</span>
          </router-link>
          <span v-if="userStore.isLoggedIn" class="nav-logout" @click="handleLogout">退出</span>
        </nav>
      </div>
    </header>

    <main class="main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Present, ShoppingCart, Tickets, Timer, TrophyBase, User, Wallet } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const handleLogout = () => { userStore.logout(); router.push('/login') }
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Header */
.header {
  background: rgba(255,255,255,.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid var(--border-light);
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  height: 60px;
  gap: 20px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  flex-shrink: 0;
}
.brand-icon {
  font-size: 24px;
  color: var(--accent);
}
.brand-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
  letter-spacing: 1px;
}
.header-spacer { flex: 1; }

/* Nav */
.nav-links { display: flex; align-items: center; gap: 4px; flex-shrink: 0; }
.nav-item {
  display: flex; align-items: center; gap: 6px;
  color: var(--text-secondary); text-decoration: none;
  padding: 8px 14px; border-radius: 8px;
  font-size: 14px; font-weight: 500;
  transition: all .2s;
}
.nav-item:hover { background: var(--bg-warm); color: var(--text); }
.nav-item.router-link-active { background: var(--bg-warm); color: var(--text); }
.nav-label { font-size: 13px; }
.nav-logout {
  color: var(--text-muted); cursor: pointer; font-size: 13px;
  margin-left: 12px; padding: 4px 8px;
}
.nav-logout:hover { color: var(--danger); }

/* Main */
.main { flex: 1; padding: 0; }

/* Footer */
.footer {
  border-top: 1px solid var(--border-light);
  padding: 24px 0;
  margin-top: auto;
}
.footer-inner {
  max-width: 1200px; margin: 0 auto; padding: 0 24px;
  text-align: center;
}
.footer-copy { font-size: 13px; color: var(--text-muted); }

/* Page transition */
.page-enter-active,
.page-leave-active { transition: opacity .15s ease; }
.page-enter-from,
.page-leave-to { opacity: 0; }
</style>
