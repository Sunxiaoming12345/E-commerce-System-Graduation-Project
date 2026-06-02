<template>
  <div class="home">
    <!-- Hero -->
    <section class="hero" v-if="!searchKeyword">
      <div class="hero-inner">
        <div class="hero-text">
          <h1 class="hero-title anim-fade-up">发现<span class="hero-accent">好物</span></h1>
          <p class="hero-sub anim-fade-up" style="animation-delay:.1s">品质生活，从这里开始</p>
          <div class="hero-search">
            <el-input v-model="searchQuery" placeholder="搜索商品..." size="large"
              :prefix-icon="Search" @keyup.enter="doSearch" class="search-inp" />
          </div>
        </div>
        <div class="hero-deco">
          <span class="deco-item">&#9826;</span>
          <span class="deco-item deco-2">&#9830;</span>
          <span class="deco-item deco-3">&#9824;</span>
        </div>
      </div>
    </section>

    <div class="container">
      <!-- Categories -->
      <div class="cats-row" v-if="!searchKeyword">
        <el-tabs v-model="activeCategory" @tab-click="handleCategoryChange">
          <el-tab-pane name="all"><template #label><span class="cat-tab">全部</span></template></el-tab-pane>
          <el-tab-pane v-for="c in categories" :key="c.id" :name="String(c.id)">
            <template #label><span class="cat-tab">{{ c.name }}</span></template>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- Search hint -->
      <div class="search-hint" v-if="searchKeyword">
        <h3>搜索 "{{ searchKeyword }}"</h3>
        <el-button link type="primary" @click="clearSearch">清除</el-button>
      </div>

      <!-- Products -->
      <div class="product-grid" v-if="products.length">
        <div class="product-card anim-fade-up" v-for="(p, idx) in products" :key="p.id"
             :style="{ animationDelay: `${Math.min(idx * 50, 300)}ms` }">
          <router-link :to="`/product/${p.id}`" class="card-img-wrap">
            <img :src="p.imageUrl || 'https://via.placeholder.com/400'" :alt="p.name" />
            <div class="card-badge" v-if="p.stock === 0">售罄</div>
          </router-link>
          <div class="card-info">
            <router-link :to="`/product/${p.id}`" class="card-name">{{ p.name }}</router-link>
            <div class="card-row">
              <span class="card-price">&yen;{{ p.price }}</span>
              <span class="card-stock" :class="{ out: p.stock === 0 }">
                {{ p.stock > 0 ? '有货' : '缺货' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty -->
      <div class="empty-state" v-else>
        <p>{{ searchKeyword ? '没有找到相关商品' : '暂无商品' }}</p>
        <el-button v-if="searchKeyword" @click="clearSearch">查看全部</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getProducts, getCategories, getProductsByCategory, searchProducts } from '@/api/products'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()
const categories = ref([])
const products = ref([])
const activeCategory = ref('all')
const searchKeyword = ref('')
const searchQuery = ref('')

const loadCategories = async () => { try { categories.value = await getCategories() } catch { /* */ } }
const loadProducts = async () => {
  try {
    if (searchKeyword.value) {
      let res = await searchProducts(searchKeyword.value)
      if (activeCategory.value !== 'all') res = res.filter(p => p.categoryId === parseInt(activeCategory.value))
      products.value = res
    } else if (activeCategory.value === 'all') {
      products.value = await getProducts()
    } else {
      products.value = await getProductsByCategory(parseInt(activeCategory.value))
    }
  } catch { products.value = [] }
}
const handleCategoryChange = (pane) => { activeCategory.value = pane.paneName; loadProducts() }
const doSearch = () => { const q = searchQuery.value.trim(); if (q) { searchKeyword.value = q; loadProducts() } }
const clearSearch = () => { searchKeyword.value = ''; searchQuery.value = ''; activeCategory.value = 'all'; loadProducts() }

// 监听路由 search 参数
watch(() => route.query.keyword, (kw) => {
  if (kw) { searchKeyword.value = kw; searchQuery.value = kw } else { searchKeyword.value = ''; searchQuery.value = '' }
  loadProducts()
})

onMounted(() => { loadCategories()
  const kw = route.query.keyword
  if (kw) { searchKeyword.value = kw; searchQuery.value = kw }
  loadProducts() })
</script>

<style scoped>
.home { padding: 0; }
.container { max-width: 1200px; margin: 0 auto; padding: 0 24px; }

/* Hero */
.hero {
  background: linear-gradient(160deg, #faf9f7 0%, #f5f0eb 40%, #ede4d8 100%);
  padding: 64px 0 56px;
  margin-bottom: 36px;
}
.hero-inner {
  max-width: 1200px; margin: 0 auto; padding: 0 24px;
  display: flex; align-items: center; justify-content: space-between;
}
.hero-title { font-size: 42px; font-weight: 800; color: var(--text); margin-bottom: 8px; }
.hero-accent { color: var(--accent); }
.hero-sub { font-size: 16px; color: var(--text-secondary); margin-bottom: 24px; }
.hero-search { max-width: 400px; }
.search-inp :deep(.el-input__wrapper) {
  border-radius: 24px; border: 1px solid var(--border);
  box-shadow: var(--shadow-xs);
  height: 48px;
}
.hero-deco { display: flex; gap: 24px; }
.deco-item { font-size: 48px; color: var(--accent-light); opacity: .6; }
.deco-2 { font-size: 36px; margin-top: 16px; }
.deco-3 { font-size: 40px; margin-top: -8px; }

/* Categories */
.cats-row { margin-bottom: 28px; }
.cat-tab { font-size: 14px; font-weight: 500; }

/* Product Grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}
.product-card {
  background: var(--surface);
  border-radius: var(--radius);
  overflow: hidden;
  border: 1px solid var(--border-light);
  transition: all .3s cubic-bezier(.4,0,.2,1);
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: transparent;
}
.card-img-wrap {
  display: block; position: relative;
  height: 240px; overflow: hidden; background: #f8f6f3;
}
.card-img-wrap img {
  width: 100%; height: 100%; object-fit: cover;
  transition: transform .5s ease;
}
.product-card:hover .card-img-wrap img { transform: scale(1.06); }
.card-badge {
  position: absolute; top: 12px; right: 12px;
  background: rgba(0,0,0,.6); color: #fff;
  font-size: 12px; padding: 4px 10px; border-radius: 12px;
  font-weight: 500;
}
.card-info { padding: 16px; }
.card-name {
  font-size: 15px; font-weight: 500; color: var(--text);
  text-decoration: none; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; line-height: 1.5; min-height: 45px; margin-bottom: 10px;
}
.card-name:hover { color: var(--accent); }
.card-row { display: flex; justify-content: space-between; align-items: center; }
.card-price { font-size: 20px; font-weight: 700; color: var(--danger); }
.card-stock { font-size: 12px; color: var(--success); }
.card-stock.out { color: var(--danger); }

.search-hint { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
.search-hint h3 { font-size: 20px; font-weight: 600; }
.empty-state { text-align: center; padding: 80px 0; color: var(--text-secondary); }
</style>
