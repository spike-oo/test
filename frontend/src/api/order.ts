import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/** 订单状态：0 待接单 1 备餐中 2 待取餐 3 配送中 4 已完成 5 已取消 6 申诉中 */
export interface Order {
  id: string
  orderNo: string
  userId?: string
  shopId?: string
  riderId?: string
  goodsAmount?: number
  deliveryFee?: number
  totalAmount: number
  orderStatus: number
  payStatus?: number
  payType?: number
  deliveryType?: number
  receiver?: string
  receiverPhone?: string
  address?: string
  remark?: string
  pickupCode?: string
  createTime?: string
}

export interface OrderItem {
  id?: string
  dishId: string
  dishName: string
  dishImage?: string
  price: number
  quantity: number
  amount: number
}

export interface OrderVO {
  order: Order
  items: OrderItem[]
  statusDesc?: string
}

export interface CreateOrderParams {
  deliveryType?: number
  payType?: number
  receiver: string
  receiverPhone: string
  address?: string
  remark?: string
}

/** 学生端 */
export function createOrder(data: CreateOrderParams) {
  return post<OrderVO>('/orders', data)
}

export function listMyOrders(params?: { status?: number; pageNum?: number; pageSize?: number }) {
  return get<PageResult<OrderVO>>('/orders', params)
}

export function getOrderDetail(id: string) {
  return get<OrderVO>(`/orders/${id}`)
}

export function cancelOrder(id: string, reason?: string) {
  return post<void>(`/orders/${id}/cancel`, { reason })
}

/** 商户端 */
export function listShopOrders(params?: { status?: number; pageNum?: number; pageSize?: number }) {
  return get<PageResult<OrderVO>>('/merchant/orders', params)
}

export function acceptOrder(id: string) {
  return post<Order>(`/merchant/orders/${id}/accept`)
}

export function readyOrder(id: string) {
  return post<Order>(`/merchant/orders/${id}/ready`)
}

export function rejectOrder(id: string, reason?: string) {
  return post<Order>(`/merchant/orders/${id}/reject`, { reason })
}
