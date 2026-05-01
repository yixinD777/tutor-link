import { defineStore } from 'pinia'
import { ref } from 'vue'
import { setToken, clearToken } from '../api/request'

export const useUserStore = defineStore('user', () => {
  const userId = ref(null)
  const nickname = ref('')
  const avatarUrl = ref('')
  const role = ref(0)
  const isLoggedIn = ref(false)

  function setLoginInfo(data) {
    userId.value = data.userId
    nickname.value = data.nickname
    avatarUrl.value = data.avatarUrl
    role.value = data.role
    isLoggedIn.value = true
    setToken(data.accessToken)
    uni.setStorageSync('refreshToken', data.refreshToken)
    uni.setStorageSync('userInfo', JSON.stringify(data))
  }

  function loadFromStorage() {
    const info = uni.getStorageSync('userInfo')
    if (info) {
      const data = JSON.parse(info)
      userId.value = data.userId
      nickname.value = data.nickname
      avatarUrl.value = data.avatarUrl
      role.value = data.role
      isLoggedIn.value = true
    }
  }

  function logout() {
    userId.value = null
    nickname.value = ''
    avatarUrl.value = ''
    role.value = 0
    isLoggedIn.value = false
    clearToken()
    uni.removeStorageSync('userInfo')
  }

  function isTutor() {
    return (role.value & 2) !== 0
  }

  function isParent() {
    return (role.value & 1) !== 0
  }

  return { userId, nickname, avatarUrl, role, isLoggedIn, setLoginInfo, loadFromStorage, logout, isTutor, isParent }
})
