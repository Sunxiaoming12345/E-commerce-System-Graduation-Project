import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/',
  timeout: 10000
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      // 网关直接拿 authorization 整段做 JWT 解析，只传 token 不带 Bearer 前缀
      config.headers.Authorization = token
    }
    return config
  },
  (err) => Promise.reject(err)
)

const statusMessages = {
  401: '登录已过期，请重新登录',
  403: '没有权限访问',
  404: '请求的资源不存在',
  500: '服务器内部错误'
}

request.interceptors.response.use(
  (res) => {
    const { code, data, msg } = res.data
    if (code === 1) {
      return data !== undefined ? data : res.data
    }
    // 后端业务错误：只 reject，由调用方决定是否弹提示
    return Promise.reject(new Error(msg || '请求失败'))
  },
  (err) => {
    const status = err.response?.status
    if (status === 401) {
      localStorage.removeItem('admin_token')
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(new Error('登录已过期'))
    }
    // 优先用后端 msg，其次用状态码对应文案，最后兜底
    const message = err.response?.data?.msg
      || statusMessages[status]
      || err.message
      || '网络错误'
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  }
)

export default request
