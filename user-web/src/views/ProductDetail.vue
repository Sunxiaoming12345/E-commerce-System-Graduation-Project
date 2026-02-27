<template>
  <div class="product-detail">
    <el-loading v-loading="loading" element-loading-text="加载中...">
      <div v-if="product" class="product-content">
        <div class="product-left">
          <div class="product-image">
            <img :src="product.imageUrl || 'https://via.placeholder.com/400'" :alt="product.name" />
          </div>
        </div>
        <div class="product-right">
          <h1 class="product-name">{{ product.name }}</h1>
          <p class="product-price">¥{{ product.price }}</p>
          <p class="product-category">分类: {{ product.categoryName }}</p>
          <p class="product-stock" v-if="product.stock > 0">库存: {{ product.stock }}</p>
          <p class="product-stock out-of-stock" v-else>缺货</p>
          <div class="product-quantity">
            <span>数量:</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock || 1" :step="1" />
          </div>
          <div class="product-actions">
            <el-button type="primary" size="large" @click="handleAddToCart" :disabled="product.stock <= 0">加入购物车</el-button>
            <el-button type="success" size="large" @click="handleBuyNow" :disabled="product.stock <= 0">立即购买</el-button>
          </div>
          <div class="product-description">
            <h3>商品描述</h3>
            <p>{{ product.description || '暂无描述' }}</p>
          </div>
        </div>
      </div>
      <div v-else class="empty">
        <el-empty description="商品不存在" />
      </div>
    </el-loading>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail } from '@/api/products'
import { addToCart } from '@/api/cart'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const productId = route.params.id
const product = ref(null)
const loading = ref(true)
const quantity = ref(1)

const loadProductDetail = async () => {
  try {
    loading.value = true
    const res = await getProductDetail(productId)
    product.value = res
  } catch (error) {
    console.error('Failed to load product detail:', error)
    ElMessage.error('加载商品详情失败')
  } finally {
    loading.value = false
  }
}

const handleAddToCart = async () => {
  try {
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error('加入购物车失败')
  }
}

const handleBuyNow = () => {
  // 创建订单商品信息
  const orderItem = {
    productId: product.value.id,
    productName: product.value.name,
    price: product.value.price,
    quantity: quantity.value,
    imageUrl: product.value.imageUrl
  }
  
  // 存储到本地存储
  localStorage.setItem('selectedCartItems', JSON.stringify([orderItem]))
  
  // 直接跳转到订单确认页面
  router.push('/order-confirm')
}

onMounted(() => {
  loadProductDetail()
})
</script>

<style scoped>
.product-detail {
  padding: 20px 0;
}

.product-content {
  display: flex;
  gap: 40px;
}

.product-left {
  flex: 1;
  max-width: 500px;
}

.product-image {
  border: 1px solid #eaeaea;
  border-radius: 8px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: auto;
  object-fit: cover;
}

.product-right {
  flex: 1;
  max-width: 600px;
}

.product-name {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

.product-price {
  font-size: 28px;
  font-weight: bold;
  color: #ff4d4f;
  margin-bottom: 20px;
}

.product-category {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.product-stock {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
}

.product-stock.out-of-stock {
  color: #ff4d4f;
}

.product-quantity {
  margin-bottom: 30px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-quantity span {
  font-size: 14px;
  color: #333;
}

.product-actions {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
}

.product-description {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #eaeaea;
}

.product-description h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #333;
}

.product-description p {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
}

.empty {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>