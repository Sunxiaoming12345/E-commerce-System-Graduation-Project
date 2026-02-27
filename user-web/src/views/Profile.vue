<template>
  <div class="profile">
    <h2>个人中心</h2>
    <div class="profile-content">
      <div class="profile-info">
        <div class="profile-avatar">
          <el-avatar :size="80">{{ userStore.username?.charAt(0) || 'U' }}</el-avatar>
        </div>
        <div class="profile-details">
          <h3>{{ userStore.username || '用户' }}</h3>
        </div>
      </div>
      <div class="profile-actions">
        <el-button type="primary" @click="dialogVisible = true">编辑资料</el-button>
        <el-button type="danger" @click="userStore.logout(); router.push('/login')">退出登录</el-button>
      </div>
    </div>

    <!-- 常用收货人信息 -->
    <div class="profile-section">
      <h3>常用收货人信息</h3>
      <div class="profile-info-item">
        <span class="label">常用收货人:</span>
        <span class="value">{{ userStore.userInfo?.defaultReceiver || '未设置' }}</span>
      </div>
      <div class="profile-info-item">
        <span class="label">常用联系电话:</span>
        <span class="value">{{ userStore.userInfo?.defaultPhone || '未设置' }}</span>
      </div>
      <div class="profile-info-item">
        <span class="label">常用收货地址:</span>
        <span class="value">{{ userStore.userInfo?.defaultAddress || '未设置' }}</span>
      </div>
    </div>

    <div class="profile-stats">
      <h3>我的统计</h3>
      <div class="stats-grid">
        <div class="stat-item">
          <el-icon><Tickets /></el-icon>
          <div class="stat-content">
            <div class="stat-number">0</div>
            <div class="stat-label">全部订单</div>
          </div>
        </div>
        <div class="stat-item">
          <el-icon><Money /></el-icon>
          <div class="stat-content">
            <div class="stat-number">¥0</div>
            <div class="stat-label">总消费</div>
          </div>
        </div>
        <div class="stat-item">
          <el-icon><Wallet /></el-icon>
          <div class="stat-content">
            <div class="stat-number">¥{{ balance || 0 }}</div>
            <div class="stat-label">账户余额</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="dialogVisible" title="编辑资料">
      <el-form :model="form" label-width="120px">
        <el-form-item label="姓名">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="常用收货人">
          <el-input v-model="form.defaultReceiver" placeholder="请输入常用收货人" />
        </el-form-item>
        <el-form-item label="常用联系电话">
          <el-input v-model="form.defaultPhone" placeholder="请输入常用联系电话" />
        </el-form-item>
        <el-form-item label="常用收货地址">
          <el-input v-model="form.defaultAddress" type="textarea" :rows="3" placeholder="请输入常用收货地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { onMounted, ref, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { Tickets, Money, Wallet } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getBalance } from '@/api/balance'

const router = useRouter()
const userStore = useUserStore()
const balance = ref(0)
const dialogVisible = ref(false)
const form = ref({
  name: '',
  phone: '',
  defaultReceiver: '',
  defaultPhone: '',
  defaultAddress: ''
})

// 监听用户信息变化，更新表单
watch(() => userStore.userInfo, (newInfo) => {
  if (newInfo) {
    form.value = {
      name: newInfo.name || '',
      phone: newInfo.phone || '',
      defaultReceiver: newInfo.defaultReceiver || '',
      defaultPhone: newInfo.defaultPhone || '',
      defaultAddress: newInfo.defaultAddress || ''
    }
  }
}, { immediate: true })

const submitForm = async () => {
  try {
    const success = await userStore.updateInfo(form.value)
    if (success) {
      ElMessage.success('资料更新成功')
      dialogVisible.value = false
    } else {
      ElMessage.error('资料更新失败')
    }
  } catch (error) {
    ElMessage.error('资料更新失败')
  }
}

const fetchBalance = async () => {
  try {
    const res = await getBalance()
    balance.value = res
  } catch (error) {
    console.error('获取余额失败:', error)
  }
}

onMounted(() => {
  userStore.fetchUserInfo()
  fetchBalance()
})
</script>

<style scoped>
.profile-section {
  background-color: #f9f9f9;
  padding: 30px;
  border-radius: 8px;
  margin-bottom: 40px;
}

.profile-section h3 {
  margin: 0 0 20px 0;
  color: #333;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.profile-info-item {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.profile-info-item .label {
  width: 120px;
  font-size: 14px;
  color: #666;
}

.profile-info-item .value {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

<style scoped>
.profile {
  padding: 20px 0;
}

.profile h2 {
  margin-bottom: 30px;
  color: #333;
}

.profile-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f9f9f9;
  padding: 30px;
  border-radius: 8px;
  margin-bottom: 40px;
}

.profile-info {
  display: flex;
  align-items: center;
  gap: 30px;
}

.profile-avatar {
  margin-right: 20px;
}

.profile-details h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.profile-details p {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

.profile-actions {
  display: flex;
  gap: 15px;
}

.profile-stats {
  background-color: #f9f9f9;
  padding: 30px;
  border-radius: 8px;
}

.profile-stats h3 {
  margin: 0 0 30px 0;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-item el-icon {
  font-size: 24px;
  color: #409EFF;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}
</style>