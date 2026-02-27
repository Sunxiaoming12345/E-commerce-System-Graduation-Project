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
        redirect: '/home',
        children: [
            { path: 'home', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
            { path: 'product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue'), meta: { title: '商品详情' } },
            { path: 'cart', name: 'Cart', component: () => import('@/views/Cart.vue'), meta: { title: '购物车' } },
            { path: 'order-confirm', name: 'OrderConfirm', component: () => import('@/views/OrderConfirm.vue'), meta: { title: '确认订单' } },
            { path: 'orders', name: 'Orders', component: () => import('@/views/Orders.vue'), meta: { title: '我的订单' } },
            { path: 'order-detail/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetail.vue'), meta: { title: '订单详情' } },
            { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人中心' } }
        ]
    }
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