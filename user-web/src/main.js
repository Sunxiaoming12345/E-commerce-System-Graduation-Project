import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// 全局时间格式化：2026-05-30T17:17:19 → 2026-05-30 17:17
app.config.globalProperties.$fmt = (val) => {
  if (!val) return ''
  const s = String(val)
  return s.replace('T', ' ').substring(0, 19)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')