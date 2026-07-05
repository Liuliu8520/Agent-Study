import { defineStore } from 'pinia'
import { adminApi } from '../api/admin'

const STORAGE_KEY = 'agent-study-admin-token'

export const useAdminAuthStore = defineStore('adminAuth', {
  state: () => ({
    token: localStorage.getItem(STORAGE_KEY) || '',
    username: localStorage.getItem(`${STORAGE_KEY}:username`) || ''
  }),
  getters: {
    loggedIn: (state) => Boolean(state.token)
  },
  actions: {
    async login(username, password) {
      const data = await adminApi.login(username, password)
      this.token = data.accessToken
      this.username = data.username
      localStorage.setItem(STORAGE_KEY, this.token)
      localStorage.setItem(`${STORAGE_KEY}:username`, this.username)
    },
    logout() {
      this.token = ''
      this.username = ''
      localStorage.removeItem(STORAGE_KEY)
      localStorage.removeItem(`${STORAGE_KEY}:username`)
    }
  }
})
