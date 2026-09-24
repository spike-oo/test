import { get, post, put } from '@/utils/request'
import type { PageResult } from '@/utils/request'
import type { Order } from '@/api/order'

export interface Rider {
  id: string
  name?: string
  phone?: string
  workStatus?: number
  deliveringCount?: number
  totalOrders?: number
  score?: number
  auditStatus?: number
}

export interface RiderIncome {
  todayIncome?: number
  monthIncome?: number
  totalIncome?: number
  todayOrders?: number
}

/** 抢单大厅：待取餐且未被抢的订单 */
export function listGrabHall(params?: { pageNum?: number; pageSize?: number }) {
  return get<PageResult<Order>>('/rider/hall', params)
}

export function grabOrder(orderId: string) {
  return post<void>(`/rider/grab/${orderId}`)
}

export function pickupOrder(orderId: string) {
  return post<void>(`/rider/pickup/${orderId}`)
}

export function deliverOrder(orderId: string) {
  return post<void>(`/rider/deliver/${orderId}`)
}

export function listRiderOrders(params?: { status?: number; pageNum?: number; pageSize?: number }) {
  return get<PageResult<Order>>('/rider/orders', params)
}

export function getIncome() {
  return get<RiderIncome>('/rider/income')
}

/** 工作状态：1 在线 / 0 离线 */
export function updateWorkStatus(workStatus: number) {
  return put<void>('/rider/work-status', { workStatus })
}

export function getRiderProfile() {
  return get<Rider>('/rider/profile')
}
