import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo, updateUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('user_token') || '')
  const username = ref(localStorage.getItem('user_username') || '')
  const userInfo = ref(null)

  function setToken(t) {
    token.value = t
    localStorage.setItem('user_token', t)
  }

  function setUsername(name) {
    username.value = name
    localStorage.setItem('user_username', name)
  }

  function logout() {
    token.value = ''
    username.value = ''
    userInfo.value = null
    localStorage.removeItem('user_token')
    localStorage.removeItem('user_username')
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      userInfo.value = res
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  async function updateInfo(info) {
    try {
      await updateUserInfo(info)
      await fetchUserInfo()
      return true
    } catch (error) {
      console.error('更新用户信息失败:', error)
      return false
    }
  }

  return { token, username, userInfo, setToken, setUsername, logout, fetchUserInfo, updateInfo }
})