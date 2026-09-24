import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/utils/request'

export interface Dish {
  id: string
  shopId?: string
  categoryId?: string
  dishName: string
  description?: string
  image?: string
  price: number
  stock?: number
  monthlySales?: number
  score?: number
  tags?: string
  ingredients?: string
  shelfStatus?: number
}

export interface DishCategory {
  id: string
  name: string
  sort?: number
}

/** 学生端 */
export function getDishDetail(id: string) {
  return get<Dish>(`/dishes/${id}`)
}

export function searchDishes(params: { keyword: string; shopId?: string; pageNum?: number; pageSize?: number }) {
  return get<PageResult<Dish>>('/dishes/search', params)
}

/** 商户端：菜品管理 */
export function listMyDishes(params?: { categoryId?: string; shelfStatus?: number; pageNum?: number; pageSize?: number }) {
  return get<PageResult<Dish>>('/merchant/dishes', params)
}

export function createDish(data: Partial<Dish>) {
  return post<void>('/merchant/dishes', data)
}

export function updateDishStatus(id: string, shelfStatus: number) {
  return put<void>(`/merchant/dishes/${id}/status`, { shelfStatus })
}

export function updateDishStock(id: string, stock: number) {
  return put<void>(`/merchant/dishes/${id}/stock`, { stock })
}

export function listDishCategories() {
  return get<DishCategory[]>('/merchant/dish-categories')
}

export function removeDish(id: string) {
  return del<void>(`/merchant/dishes/${id}`)
}
