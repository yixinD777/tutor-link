import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '../api/auth'
import router from '../router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('accessToken') || '',
    userId: '',
    nickname: '',
    avatarUrl: '',
    role: 0
  }),
  actions: {
    async login(account, password) {
      const data = await loginApi({ account, password })
      if (!(data.role & 4)) {
        throw new Error('该账号不是管理员')
      }
      this.token = data.accessToken
      this.userId = data.userId
      this.nickname = data.nickname
      this.avatarUrl = data.avatarUrl
      this.role = data.role
      localStorage.setItem('accessToken', data.accessToken)
      localStorage.setItem('refreshToken', data.refreshToken)
      return data
    },
    async logout() {
      try { await logoutApi() } catch (e) { /* ignore */ }
      this.token = ''
      this.userId = ''
      this.nickname = ''
      this.avatarUrl = ''
      this.role = 0
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      router.push('/login')
    }
  }
})
