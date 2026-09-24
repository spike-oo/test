import { del, get, post, put } from '@/utils/request'

export interface CartItem {
  id: string
  dishId: string
  dishName?: string
  dishImage?: string
  price?: number
  quantity: number
  selected?: number
  shopId?: string
  stock?: number
}

export interface AddCartParams {
  dishId: string
  quantity: number
}

export function listCart() {
  return get<CartItem[]>('/cart')
}

export function addToCart(data: AddCartParams) {
  return post<void>('/cart', data)
}

export function updateQuantity(id: string, quantity: number) {
  return put<void>(`/cart/${id}/quantity`, { quantity })
}

export function updateSelected(id: string, selected: number) {
  return put<void>(`/cart/${id}/selected`, { selected })
}

export function removeCartItem(id: string) {
  return del<void>(`/cart/${id}`)
}

export function clearCart() {
  return del<void>('/cart')
}
