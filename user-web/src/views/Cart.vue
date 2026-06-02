<template>
  <div class="cart-page">
    <div class="container">
      <div class="cart-head">
        <h2>购物车</h2>
        <span class="count" v-if="cartItems.length">{{ cartItems.length }} 件商品</span>
      </div>

      <div v-if="cartItems.length">
        <TransitionGroup name="list" tag="div" class="cart-items">
          <div class="cart-card" v-for="item in cartItems" :key="item.productId">
            <el-checkbox :model-value="selectedIds.has(item.productId)" @change="toggle(item)" />
            <img :src="item.imageUrl || 'https://via.placeholder.com/120'" class="card-img" />
            <div class="card-body">
              <router-link :to="`/product/${item.productId}`" class="card-name">{{ item.productName }}</router-link>
              <span class="card-warn" v-if="item.status == 0">已下架</span>
              <span class="card-warn" v-else-if="item.stock <= 0">缺货</span>
              <div class="card-row">
                <span class="card-price">&yen;{{ item.price }}</span>
                <el-input-number v-model="item.quantity" :min="1" :max="Math.max(item.stock,999)" size="small" :disabled="item.status == 0" @change="(v) => { if(v>0) update(item); else remove(item.productId); }" />
                <span class="card-sub">&yen;{{ (item.quantity*item.price).toFixed(2) }}</span>
              </div>
            </div>
            <el-button link class="del-btn" @click="remove(item.productId)"><el-icon :size="18"><Delete /></el-icon></el-button>
          </div>
        </TransitionGroup>

        <div class="cart-bar">
          <div class="bar-left">
            <el-checkbox :model-value="selectedIds.size === cartItems.length" :indeterminate="selectedIds.size > 0 && selectedIds.size < cartItems.length" @change="toggleAll">全选</el-checkbox>
            <el-button link type="danger" @click="clearSelected" v-if="selectedIds.size">删除选中</el-button>
          </div>
          <div class="bar-right">
            <span class="bar-label">合计</span>
            <span class="bar-total">&yen;{{ total.toFixed(2) }}</span>
            <el-button type="primary" size="large" :disabled="!selectedIds.size" @click="checkout">去结算</el-button>
          </div>
        </div>
      </div>

      <div v-else class="empty-cart">
        <el-empty description="购物车是空的" />
        <router-link to="/home" class="go-shop">去逛逛</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCartList, updateCartItem, removeCartItem } from '@/api/cart'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const cartItems = ref([]); const selectedIds = ref(new Set())
const total = computed(() => cartItems.value.filter(i => selectedIds.value.has(i.productId)).reduce((s,i) => s+i.quantity*i.price, 0))

const load = async () => { try { cartItems.value = await getCartList() } catch { /* */ } }
const toggle = (item) => { const s = new Set(selectedIds.value); s.has(item.productId)?s.delete(item.productId):s.add(item.productId); selectedIds.value = s }
const toggleAll = (v) => { selectedIds.value = v ? new Set(cartItems.value.map(i=>i.productId)) : new Set() }
const update = async (item) => { try { await updateCartItem({productId:item.productId,quantity:item.quantity}) } catch { ElMessage.error('更新失败'); load() } }
const remove = async (id) => { try { await removeCartItem(id); const s = new Set(selectedIds.value); s.delete(id); selectedIds.value = s; ElMessage.success('已移除') } catch { /* */ }; load() }
const clearSelected = async () => {
  try { await ElMessageBox.confirm('确定删除选中的商品？','提示',{type:'warning'})
    for (const id of selectedIds.value) { await removeCartItem(id) }
    selectedIds.value = new Set(); load()
  } catch { /* cancelled */ }
}
const checkout = () => {
  const items = cartItems.value.filter(i => selectedIds.value.has(i.productId))
  if (items.some(i=>i.status==0||i.stock<=0)) return ElMessage.warning('有商品已下架或缺货')
  localStorage.setItem('selectedCartItems', JSON.stringify(items)); router.push('/order-confirm')
}
onMounted(load)
</script>

<style scoped>
.cart-page { padding: 0; }
.container { max-width: 900px; margin: 0 auto; padding: 0 24px; }
.cart-head { display: flex; align-items: baseline; gap: 12px; margin-bottom: 24px; }
.cart-head h2 { font-size: 24px; font-weight: 700; }
.count { font-size: 14px; color: var(--text-muted); }

.cart-items { display: flex; flex-direction: column; gap: 12px; margin-bottom: 100px; }
.cart-card {
  display: flex; align-items: center; gap: 16px;
  background: var(--surface); border-radius: var(--radius); padding: 16px 20px;
  border: 1px solid var(--border-light); transition: box-shadow .2s;
}
.cart-card:hover { box-shadow: var(--shadow); }
.card-img { width: 90px; height: 90px; object-fit: cover; border-radius: var(--radius-xs); }
.card-body { flex: 1; min-width: 0; }
.card-name { font-size: 15px; font-weight: 500; color: var(--text); text-decoration: none; display: block; }
.card-name:hover { color: var(--accent); }
.card-warn { font-size: 12px; color: var(--danger); }
.card-row { display: flex; align-items: center; gap: 16px; margin-top: 8px; }
.card-price { font-size: 14px; color: var(--text); width: 60px; }
.card-sub { font-size: 16px; font-weight: 600; color: var(--danger); margin-left: auto; }
.del-btn { flex-shrink: 0; color: var(--text-muted); }

.cart-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: var(--surface); border-top: 1px solid var(--border-light);
  padding: 16px 20px; display: flex; justify-content: space-between; align-items: center;
  z-index: 50; max-width: 900px; margin: 0 auto;
  border-radius: var(--radius) var(--radius) 0 0;
  box-shadow: 0 -4px 16px rgba(0,0,0,.06);
}
.bar-left { display: flex; align-items: center; gap: 20px; }
.bar-right { display: flex; align-items: center; gap: 12px; }
.bar-label { font-size: 14px; color: var(--text-secondary); }
.bar-total { font-size: 24px; font-weight: 700; color: var(--danger); }
.empty-cart { text-align: center; padding: 80px 0; }
.go-shop { display: inline-block; margin-top: 12px; background: var(--primary); color: #fff; padding: 10px 32px; border-radius: 24px; text-decoration: none; font-weight: 500; }

/* List transitions */
.list-enter-active { transition: all .4s ease; }
.list-leave-active { transition: all .3s ease; }
.list-enter-from { opacity: 0; transform: translateX(-20px); }
.list-leave-to   { opacity: 0; transform: translateX(20px); }
</style>
