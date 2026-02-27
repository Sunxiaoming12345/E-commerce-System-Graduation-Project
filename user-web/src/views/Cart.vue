<template>
  <div class="cart">
    <h2>购物车</h2>
    <el-loading v-loading="loading" element-loading-text="加载中...">
      <div v-if="cartItems.length > 0" class="cart-content">
        <el-table ref="tableRef" :data="cartItems" style="width: 100%" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" />
          <el-table-column label="商品" min-width="200">
            <template #default="scope">
              <div class="cart-item-info">
                <img :src="scope.row.imageUrl || 'https://via.placeholder.com/80'" :alt="scope.row.productName" class="cart-item-image" />
                <div class="cart-item-details">
                  <router-link :to="`/product/${scope.row.productId}`" class="cart-item-name">{{ scope.row.productName }}</router-link>
                  <p class="cart-item-price">¥{{ scope.row.price }}</p>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="180">
            <template #default="scope">
                <el-input-number v-model="scope.row.quantity" :min="0" :max="scope.row.stock" @change="handleUpdateCartItem(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="scope">
              <span class="cart-item-subtotal">¥{{ (scope.row.quantity * scope.row.price).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
             <el-button type="danger" size="small" @click="handleRemoveCartItem(scope.row.productId)">
删除
</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="cart-summary">
          <div class="cart-total">
            <span>合计:</span>
            <span class="cart-total-price">¥{{ selectedTotalPrice.toFixed(2) }}</span>
          </div>
          <div class="cart-actions">
            <el-button @click="handleClearCart">清空购物车</el-button>
            <el-button type="primary" size="large" @click="handleCheckout" :disabled="selectedItems.length === 0">去结算</el-button>
          </div>
        </div>
      </div>
      <div v-else class="empty">
        <el-empty description="购物车为空" />
        <router-link to="/" class="go-shopping">去购物</router-link>
      </div>
    </el-loading>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCartList, updateCartItem, removeCartItem } from '@/api/cart'
import { createOrder } from '@/api/orders'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartItems = ref([])
const loading = ref(true)
const tableRef = ref(null)
const selectedItems = ref([])

const loadCartList = async () => {
  try {
    loading.value = true
    const res = await getCartList()
    cartItems.value = res
  } catch (error) {
    console.error('Failed to load cart list:', error)
    ElMessage.error('加载购物车失败')
  } finally {
    loading.value = false
  }
}

const selectedTotalPrice = computed(() => {
  return selectedItems.value.reduce((total, item) => {
    return total + (item.quantity * item.price)
  }, 0)
})

const handleUpdateCartItem = async (item) => {
  try {
    if (item.quantity === 0) {
      // 数量为0时删除商品
      await removeCartItem(item.productId)
      ElMessage.success('商品已从购物车移除')
      loadCartList()
    } else {
      // 正常更新数量
      await updateCartItem({ productId: item.productId, quantity: item.quantity })
    }
  } catch (error) {
    ElMessage.error('更新购物车失败')
    // 恢复之前的数量
    loadCartList()
  }
}

const handleRemoveCartItem = async (id) => {
  try {
    await removeCartItem(id)
    ElMessage.success('删除成功')
    loadCartList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleClearCart = async () => {
  try {
    // 批量删除购物车项
    for (const item of cartItems.value) {
      await removeCartItem(item.productId)
    }
    ElMessage.success('清空购物车成功')
    cartItems.value = []
    selectedItems.value = []
  } catch (error) {
    ElMessage.error('清空购物车失败')
  }
}

const handleCheckout = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  
  // 将选中的商品保存到本地存储
  localStorage.setItem('selectedCartItems', JSON.stringify(selectedItems.value))
  
  // 跳转到订单确认页面
  router.push('/order-confirm')
}

// 监听选择变化
const handleSelectionChange = (val) => {
  selectedItems.value = val
}

onMounted(() => {
  loadCartList()
})
</script>

<style scoped>
.cart {
  padding: 20px 0;
}

.cart h2 {
  margin-bottom: 30px;
  color: #333;
}

.cart-content {
  border: 1px solid #eaeaea;
  border-radius: 8px;
  overflow: hidden;
}

.cart-item-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.cart-item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.cart-item-details {
  flex: 1;
}

.cart-item-name {
  font-size: 14px;
  color: #333;
  text-decoration: none;
  margin-bottom: 8px;
  display: block;
}

.cart-item-name:hover {
  color: #409EFF;
}

.cart-item-price {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.cart-item-subtotal {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: 500;
}

.cart-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-top: 1px solid #eaeaea;
}

.cart-total {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.cart-total span:first-child {
  font-size: 16px;
  color: #333;
}

.cart-total-price {
  font-size: 24px;
  font-weight: bold;
  color: #ff4d4f;
}

.cart-actions {
  display: flex;
  gap: 15px;
}

.empty {
  min-height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.go-shopping {
  color: #409EFF;
  text-decoration: none;
}

.go-shopping:hover {
  text-decoration: underline;
}
</style>