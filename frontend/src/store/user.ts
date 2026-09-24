import { defineStore } from 'pinia'

import { login as loginApi, logout as logoutApi, type LoginParams } from '@/api/auth'
import { ROLE_KEY, TOKEN_KEY } from '@/utils/request'

/** 角色码，与后端 com.campus.delivery.security.RoleEnum 保持一致 */
export const ROLE = {
  STUDENT: 'STUDENT',
  MERCHANT: 'MERCHANT',
  RIDER: 'RIDER',
  ADMIN: 'ADMIN'
} as const

/** 角色默认首页 */
export const ROLE_HOME: Record<string, string> = {
  [ROLE.STUDENT]: '/student/home',
  [ROLE.MERCHANT]: '/merchant/dashboard',
  [ROLE.RIDER]: '/rider/hall',
  [ROLE.ADMIN]: '/admin/dashboard'
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    role: localStorage.getItem(ROLE_KEY) || '',
    userId: '',
    nickname: ''
  }),

  getters: {
    isLogin: (state) => !!state.token,
    home: (state) => ROLE_HOME[state.role] || '/login'
  },

  actions: {
    async login(params: LoginParams) {
      const result = await loginApi(params)
      this.token = result.token
      this.role = result.role
      this.userId = result.userId
      this.nickname = result.nickname || ''
      localStorage.setItem(TOKEN_KEY, result.token)
      localStorage.setItem(ROLE_KEY, result.role)
      return result
    },

    async logout() {
      try {
        await logoutApi()
      } finally {
        this.reset()
      }
    },

    reset() {
      this.token = ''
      this.role = ''
      this.userId = ''
      this.nickname = ''
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(ROLE_KEY)
    }
  }
})
