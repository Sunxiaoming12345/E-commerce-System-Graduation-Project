import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const username = ref(localStorage.getItem('admin_username') || '')

  function setToken(t) {
    token.value = t
    localStorage.setItem('admin_token', t)
  }

  function setUsername(name) {
    username.value = name
    localStorage.setItem('admin_username', name)
  }

  function logout() {
    token.value = ''
    username.value = ''
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_username')
  }

  return { token, username, setToken, setUsername, logout }
})
