<template>
  <div class="order-confirm">
    <h2>确认订单</h2>
    <el-loading v-loading="loading" element-loading-text="加载中...">
      <div class="order-content">
        <!-- 收货地址 -->
        <div class="section">
          <h3>收货信息</h3>
          <el-form :model="form" label-width="100px">
            <el-form-item label="收货人">
              <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="form.receiverPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="收货地址">
              <el-input v-model="form.shippingAddress" placeholder="请输入详细收货地址" type="textarea" :rows="3" />
            </el-form-item>
          </el-form>
        </div>

        <!-- 商品信息 -->
        <div class="section">
          <h3>商品信息</h3>
          <el-table :data="orderItems" style="width: 100%">
            <el-table-column label="商品" min-width="200">
              <template #default="scope">
                <div class="product-info">
                  <img :src="scope.row.imageUrl || 'https://via.placeholder.com/80'" :alt="scope.row.productName" class="product-image" />
                  <span class="product-name">{{ scope.row.productName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="100">
              <template #default="scope">
                ¥{{ scope.row.price }}
              </template>
            </el-table-column>
            <el-table-column label="数量" width="100">
              <template #default="scope">
                {{ scope.row.quantity }}
              </template>
            </el-table-column>
            <el-table-column label="小计" width="120">
              <template #default="scope">
                ¥{{ (scope.row.quantity * scope.row.price).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 支付方式 -->
        <div class="section">
          <h3>支付方式</h3>
          <el-radio-group v-model="form.paymentMethod">
            <el-radio label="2">余额支付</el-radio>
            <el-radio label="0">支付宝</el-radio>
            <el-radio label="1">微信支付</el-radio>
          </el-radio-group>
        </div>

        <!-- 订单金额 -->
        <div class="section">
          <h3>订单金额</h3>
          <div class="order-amount">
            <div class="amount-item">
              <span>商品总价：</span>
              <span>¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <div class="amount-item">
              <span>运费：</span>
              <span>¥{{ shippingFee.toFixed(2) }}</span>
            </div>
            <div class="amount-item total">
              <span>实付金额：</span>
              <span>¥{{ (totalPrice + shippingFee).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="order-actions">
          <el-button @click="goBack">返回购物车</el-button>
          <el-button type="primary" size="large" @click="submitOrder">提交订单</el-button>
        </div>
      </div>
    </el-loading>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createOrder, payOrder } from '@/api/orders'
import { removeCartItem } from '@/api/cart'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const userStore = useUserStore()

// 订单商品信息
const orderItems = ref([])

// 表单数据
const form = ref({
  receiverName: '',
  receiverPhone: '',
  shippingAddress: '',
  paymentMethod: '2' // 默认余额支付
})

// 运费
const shippingFee = ref(0)

// 计算商品总价
const totalPrice = computed(() => {
  return orderItems.value.reduce((total, item) => {
    return total + (item.quantity * item.price)
  }, 0)
})

// 计算实付金额
const actualAmount = computed(() => {
  return totalPrice.value + shippingFee.value
})

// 加载订单商品信息
const loadOrderItems = () => {
  // 从路由参数中获取选中的商品
  const selectedItems = JSON.parse(localStorage.getItem('selectedCartItems') || '[]')
  if (selectedItems.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    router.push('/cart')
    return
  }
  orderItems.value = selectedItems
}

// 加载用户常用收货人信息
const loadUserInfo = async () => {
  await userStore.fetchUserInfo()
  if (userStore.userInfo) {
    // 设置默认值
    form.value.receiverName = form.value.receiverName || userStore.userInfo.defaultReceiver || ''
    form.value.receiverPhone = form.value.receiverPhone || userStore.userInfo.defaultPhone || ''
    form.value.shippingAddress = form.value.shippingAddress || userStore.userInfo.defaultAddress || ''
  }
}

// 提交订单
const submitOrder = async () => {
  // 验证表单
  if (!form.value.receiverName || !form.value.receiverPhone || !form.value.shippingAddress) {
    ElMessage.warning('请填写完整的收货信息')
    return
  }

  try {
    loading.value = true
    
    // 创建订单
    const orderData = {
      items: orderItems.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      })),
      receiverName: form.value.receiverName,
      receiverPhone: form.value.receiverPhone,
      shippingAddress: form.value.shippingAddress,
      paymentMethod: parseInt(form.value.paymentMethod),
      totalAmount: actualAmount.value
    }

    // 调用创建订单接口
    const res = await createOrder(orderData)
    
    // 从购物车中移除已结算的商品
    for (const item of orderItems.value) {
      await removeCartItem(item.productId)
    }
    
    // 跳转到支付页面或直接完成支付
    ElMessage.success('订单创建成功')
    
    // 清除本地存储的选中商品
    localStorage.removeItem('selectedCartItems')
    
    // 跳转到订单列表
    router.push('/orders')
  } catch (error) {
    ElMessage.error('提交订单失败')
    console.error('Failed to submit order:', error)
  } finally {
    loading.value = false
  }
}

// 返回购物车
const goBack = () => {
  router.push('/cart')
}

onMounted(async () => {
  loadOrderItems()
  await loadUserInfo()
})
</script>

<style scoped>
.order-confirm {
  padding: 20px 0;
}

.order-confirm h2 {
  margin-bottom: 30px;
  color: #333;
}

.order-content {
  max-width: 800px;
  margin: 0 auto;
}

.section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #eaeaea;
  border-radius: 8px;
}

.section h3 {
  margin-bottom: 20px;
  font-size: 16px;
  color: #333;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 10px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.product-name {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.order-amount {
  margin-top: 10px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}

.amount-item.total {
  font-weight: bold;
  font-size: 16px;
  color: #ff4d4f;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eaeaea;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 40px;
}
</style>
