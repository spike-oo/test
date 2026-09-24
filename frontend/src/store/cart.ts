import { defineStore } from 'pinia'

import { listCart, type CartItem } from '@/api/cart'

/**
 * 购物车状态。
 *
 * <p>购物车数据以服务端为准（同一订单只能包含同一店铺的菜品），
 * 前端只缓存列表与勾选态，避免多端不一致。
 */
export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [] as CartItem[],
    loading: false
  }),

  getters: {
    /** 已勾选条数 */
    selectedCount: (state) => state.items.filter((item) => item.selected === 1).length,
    /** 已勾选金额合计 */
    selectedAmount: (state) =>
      state.items
        .filter((item) => item.selected === 1)
        .reduce((sum, item) => sum + (item.price || 0) * item.quantity, 0),
    /** 当前购物车所属店铺（用于同店约束提示） */
    shopId: (state) => state.items[0]?.shopId || ''
  },

  actions: {
    async refresh() {
      this.loading = true
      try {
        this.items = await listCart()
      } finally {
        this.loading = false
      }
    },

    clearLocal() {
      this.items = []
    }
  }
})
