import { get, put } from '@/utils/request'
import type { PageResult } from '@/utils/request'

export interface Shop {
  id: string
  shopName: string
  logo?: string
  description?: string
  address?: string
  categoryId?: string
  businessStatus?: number
  deliveryFee?: number
  minAmount?: number
  score?: number
  monthlySales?: number
  notice?: string
}

/** 学生端：店铺列表 / 详情 */
export function listShops(params?: { keyword?: string; categoryId?: string; pageNum?: number; pageSize?: number }) {
  return get<PageResult<Shop>>('/shops', params)
}

export function getShopDetail(id: string) {
  return get<Shop>(`/shops/${id}`)
}

/** 商户端：我的店铺 */
export function getMyShop() {
  return get<Shop>('/merchant/shop')
}

export function updateMyShop(data: Partial<Shop>) {
  return put<void>('/merchant/shop', data)
}

/** 营业状态：1 营业中 / 0 休息中 */
export function updateBusinessStatus(status: number) {
  return put<void>('/merchant/shop/status', { status })
}
