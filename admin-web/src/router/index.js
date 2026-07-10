import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import Layout from '@/views/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'products', name: 'Products', component: () => import('@/views/Products.vue'), meta: { title: '商品管理' } },
      { path: 'orders', name: 'Orders', component: () => import('@/views/Orders.vue'), meta: { title: '订单管理' } },
      { path: 'categories', name: 'Categories', component: () => import('@/views/Categories.vue'), meta: { title: '分类管理' } },
      { path: 'payments', name: 'Payments', component: () => import('@/views/Payments.vue'), meta: { title: '支付记录' } },
      { path: 'reviews', name: 'Reviews', component: () => import('@/views/Reviews.vue'), meta: { title: '评价管理' } },
      { path: 'coupons', name: 'Coupons', component: () => import('@/views/Coupons.vue'), meta: { title: '优惠券管理' } },
      { path: 'refunds', name: 'Refunds', component: () => import('@/views/Refunds.vue'), meta: { title: '退款管理' } },
      { path: 'lottery', name: 'Lottery', component: () => import('@/views/Lottery.vue'), meta: { title: '抽奖管理' } },
      { path: 'seckill', name: 'Seckill', component: () => import('@/views/Seckill.vue'), meta: { title: '秒杀管理' } },
      { path: 'chat', name: 'Chat', component: () => import('@/views/ChatManagement.vue'), meta: { title: '客服消息' } }
    ]
  },
  // 兼容旧路径 /home 和未匹配路由 → 重定向到工作台
  { path: '/home', redirect: '/dashboard' },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = useUserStore()
  if (to.meta.guest) {
    if (user.token) next({ path: '/' })
    else next()
  } else {
    if (!user.token) next({ path: '/login', query: { redirect: to.fullPath } })
    else next()
  }
})

export default router
