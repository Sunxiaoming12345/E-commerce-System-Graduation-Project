<template>
  <div class="layout">
    <header class="header">
      <div class="container">
        <div class="header-left">
          <router-link to="/" class="logo">首页</router-link>
        </div>
        <div class="header-center">
          <el-input
              v-model="searchQuery"
              placeholder="搜索商品"
              prefix-icon="el-icon-search"
              style="width: 300px"
              @keyup.enter="handleSearch"
          />
        </div>
        <div class="header-right">
          <router-link to="/cart" class="header-link">
            <el-icon><ShoppingCart /></el-icon>
            <span>购物车</span>
          </router-link>
          <router-link to="/orders" class="header-link">
            <el-icon><Tickets /></el-icon>
            <span>订单</span>
          </router-link>
          <router-link to="/profile" class="header-link">
            <el-icon><User /></el-icon>
            <span>{{ userStore.userInfo ? userStore.userInfo.username : '个人中心' }}</span>
          </router-link>
          <el-button v-if="userStore.isLoggedIn" type="text" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </header>
    <main class="main">
      <div class="container">
        <router-view />
      </div>
    </main>
    <footer class="footer">
      <div class="container">
        <p>© 孙小明</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ShoppingCart, Tickets, User } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const searchQuery = ref('')

const handleSearch = () => {
  if (searchQuery.value) {
    router.push({ path: '/home', query: { keyword: searchQuery.value } })
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #eaeaea;
  padding: 10px 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header .container {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  text-decoration: none;
}

.header-center {
  flex: 1;
  max-width: 500px;
  margin: 0 40px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-link {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #333;
  text-decoration: none;
  padding: 5px 10px;
  border-radius: 4px;
  transition: all 0.3s;
}

.header-link:hover {
  background-color: #f5f7fa;
  color: #409EFF;
}

.main {
  flex: 1;
  padding: 20px 0;
}

.footer {
  background-color: #f5f7fa;
  padding: 20px 0;
  margin-top: 40px;
}

.footer .container {
  text-align: center;
  color: #999;
}
</style>