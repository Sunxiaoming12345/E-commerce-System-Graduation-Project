<template>
  <div class="pdp" v-if="product">
    <div class="container">
      <div class="breadcrumb">
        <router-link to="/home">首页</router-link> <span>/</span>
        <router-link :to="`/home?category=${product.categoryId}`">{{ product.categoryName }}</router-link> <span>/</span>
        <span class="cur">{{ product.name }}</span>
      </div>

      <div class="pdp-main anim-scale-in">
        <div class="pdp-image">
          <img :src="product.imageUrl || 'https://via.placeholder.com/600'" :alt="product.name" />
        </div>
        <div class="pdp-info">
          <h1 class="pdp-name">{{ product.name }}</h1>
          <p class="pdp-cat">{{ product.categoryName }}</p>
          <div class="pdp-price-row">
            <span class="pdp-price">&yen;{{ product.price }}</span>
            <span class="pdp-stock" :class="{ out: product.stock === 0 }">
              {{ product.stock > 0 ? `库存 ${product.stock} 件` : '已售罄' }}
            </span>
          </div>
          <div class="pdp-qty">
            <span class="qty-label">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="Math.max(product.stock,1)" size="large" />
          </div>
          <div class="pdp-actions">
            <el-button size="large" class="btn-cart" :disabled="product.stock === 0" @click="handleAddToCart">
              <el-icon><ShoppingCart /></el-icon>加入购物车
            </el-button>
            <el-button size="large" type="primary" class="btn-buy" :disabled="product.stock === 0" @click="handleBuyNow">
              立即购买
            </el-button>
          </div>
        </div>
      </div>

      <div class="pdp-desc" v-if="product.description">
        <h3>商品详情</h3>
        <p>{{ product.description }}</p>
      </div>

      <!-- 商品评价 -->
      <div class="reviews-section">
        <h3>商品评价 ({{ reviews.length }})</h3>
        <div class="review-card" v-for="r in reviews" :key="r.reviewId">
          <div class="review-top">
            <span class="review-stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
            <span class="review-time">{{ $fmt(r.createTime) }}</span>
          </div>
          <p class="review-content">{{ r.content }}</p>
        </div>
        <el-empty v-if="reviews.length === 0" description="暂无评价" :image-size="80" />

        <!-- 写评价 -->
        <div class="write-review" v-if="userStore.token">
          <h4>写评价</h4>
          <div class="rating-row">
            <span>评分：</span>
            <el-rate v-model="reviewForm.rating" :max="5" show-text :texts="['很差','较差','一般','推荐','力荐']" />
          </div>
          <el-input v-model="reviewForm.content" type="textarea" rows="3" placeholder="分享你的使用感受..." />
          <el-button type="primary" class="submit-review" @click="handleSubmitReview">提交评价</el-button>
        </div>
      </div>
      <ChatWidget v-if="userStore.token" :productId="product?.id" :productName="product?.name" />
    </div>
  </div>
  <div v-else class="empty-page"><el-empty description="商品不存在" /></div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail } from '@/api/products'
import { addToCart, getCartList } from '@/api/cart'
import { prepurchase } from '@/api/orders'
import { getProductReviews, submitReview } from '@/api/reviews'
import { useUserStore } from '@/store/user'
import { ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import ChatWidget from '@/components/ChatWidget.vue'

const route = useRoute(); const router = useRouter()
const userStore = useUserStore()
const product = ref(null); const quantity = ref(1)
const reviews = ref([])
const reviewForm = reactive({ rating: 5, content: '' })

const load = async () => {
  try {
    product.value = await getProductDetail(route.params.id)
    loadReviews()
  } catch { ElMessage.error('加载失败') }
}

async function loadReviews() {
  try { reviews.value = await getProductReviews(route.params.id) } catch { }
}

async function handleSubmitReview() {
  if (!reviewForm.content.trim()) return ElMessage.warning('请输入评价内容')
  try {
    await submitReview({ productId: product.value.id, rating: reviewForm.rating, content: reviewForm.content })
    ElMessage.success('评价成功')
    reviewForm.content = ''
    reviewForm.rating = 5
    loadReviews()
  } catch { }
}
const handleAddToCart = async () => {
  try {
    const list = await getCartList()
    const ex = list.find(i => i.productId === product.value.id)
    if (ex && ex.quantity + quantity.value > product.value.stock) return ElMessage.warning('超过库存')
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    ElMessage.success('已加入购物车')
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '加入失败') }
}
const handleBuyNow = async () => {
  try {
    await prepurchase({ productId: product.value.id, quantity: quantity.value })
    localStorage.setItem('selectedCartItems', JSON.stringify([{ productId: product.value.id, productName: product.value.name, price: product.value.price, quantity: quantity.value, imageUrl: product.value.imageUrl }]))
    localStorage.setItem('isDirectPurchase', 'true'); router.push('/order-confirm')
  } catch (e) { ElMessage.error(e?.response?.data?.msg || '操作失败') }
}
onMounted(load)
</script>

<style scoped>
.pdp { padding: 0; }
.container { max-width: 1100px; margin: 0 auto; padding: 0 24px; }

.breadcrumb {
  padding: 16px 0; font-size: 13px; color: var(--text-muted);
}
.breadcrumb a { color: var(--text-secondary); text-decoration: none; }
.breadcrumb a:hover { color: var(--text); }
.breadcrumb span { margin: 0 6px; }
.breadcrumb .cur { color: var(--text); }

.pdp-main {
  display: flex; gap: 56px;
  background: var(--surface); border-radius: var(--radius);
  padding: 32px; border: 1px solid var(--border-light);
}
.pdp-image {
  flex: 0 0 460px; border-radius: var(--radius-sm); overflow: hidden; background: #f8f6f3;
}
.pdp-image img { width: 100%; display: block; }

.pdp-info { flex: 1; display: flex; flex-direction: column; gap: 20px; }
.pdp-name { font-size: 28px; font-weight: 700; color: var(--text); line-height: 1.3; }
.pdp-cat { font-size: 14px; color: var(--text-muted); }
.pdp-price-row { display: flex; align-items: baseline; gap: 16px; }
.pdp-price { font-size: 36px; font-weight: 800; color: var(--danger); }
.pdp-stock { font-size: 14px; color: var(--success); font-weight: 500; }
.pdp-stock.out { color: var(--danger); }
.pdp-qty { display: flex; align-items: center; gap: 16px; }
.qty-label { font-size: 14px; color: var(--text-secondary); }
.pdp-actions { display: flex; gap: 16px; margin-top: 8px; }
.btn-cart { height: 48px; padding: 0 28px; font-size: 15px; border-color: var(--primary); color: var(--primary); }
.btn-cart:hover { background: var(--primary); color: #fff; }
.btn-buy { height: 48px; padding: 0 40px; font-size: 15px; }

.pdp-desc {
  margin-top: 24px; background: var(--surface); border-radius: var(--radius);
  padding: 28px 32px; border: 1px solid var(--border-light);
}
.pdp-desc h3 { font-size: 18px; font-weight: 600; margin-bottom: 16px; color: var(--text); }
.pdp-desc p { font-size: 15px; line-height: 1.8; color: var(--text-secondary); }

.reviews-section {
  margin-top: 24px; background: var(--surface); border-radius: var(--radius);
  padding: 28px 32px; border: 1px solid var(--border-light);
}
.reviews-section h3 { font-size: 18px; font-weight: 600; margin-bottom: 16px; color: var(--text); }
.reviews-section h4 { font-size: 15px; font-weight: 600; margin: 20px 0 12px; color: var(--text); }
.review-card {
  padding: 14px 0; border-bottom: 1px solid var(--border-light);
}
.review-card:last-child { border-bottom: none; }
.review-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.review-stars { color: #f59e0b; font-size: 14px; }
.review-time { font-size: 12px; color: var(--text-muted); }
.review-content { font-size: 14px; color: var(--text-secondary); line-height: 1.6; }
.rating-row { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.submit-review { margin-top: 12px; }

.empty-page { min-height: 400px; display: flex; align-items: center; justify-content: center; }
</style>
