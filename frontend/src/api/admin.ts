import { del, get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

export interface Knowledge {
  id?: string
  category?: string
  question: string
  answer: string
  keywords?: string
  hitCount?: number
}

export interface Ticket {
  id: string
  ticketNo: string
  userId?: string
  sessionId?: string
  orderId?: string
  category?: string
  question: string
  status: number
  handlerId?: string
  reply?: string
  createTime?: string
  closeTime?: string
}

/** 数据看板 */
export function dashboardOverview() {
  return get<Record<string, number>>('/admin/dashboard/overview')
}

export function dashboardTrend(days = 7) {
  return get<Array<Record<string, unknown>>>('/admin/dashboard/trend', { days })
}

export function dashboardShopRanking(limit = 10) {
  return get<Array<Record<string, unknown>>>('/admin/dashboard/shop-ranking', { limit })
}

export function dashboardDishRanking(limit = 10) {
  return get<Array<Record<string, unknown>>>('/admin/dashboard/dish-ranking', { limit })
}

export function dashboardCategoryShare() {
  return get<Array<Record<string, unknown>>>('/admin/dashboard/category-share')
}

export function dashboardHourDistribution() {
  return get<Array<Record<string, unknown>>>('/admin/dashboard/hour-distribution')
}

export function dashboardTagCloud(limit = 30) {
  return get<Array<Record<string, unknown>>>('/admin/dashboard/tag-cloud', { limit })
}

/** 审核与处置 */
export function auditMerchant(id: string, pass: boolean, remark?: string) {
  return post<void>(`/admin/audit/merchant/${id}`, null, { params: { pass, remark } })
}

export function auditRider(id: string, pass: boolean, remark?: string) {
  return post<void>(`/admin/audit/rider/${id}`, null, { params: { pass, remark } })
}

export function banShop(id: string, ban: boolean) {
  return post<void>(`/admin/audit/shop/${id}/ban`, null, { params: { ban } })
}

export function banRider(id: string, ban: boolean) {
  return post<void>(`/admin/audit/rider/${id}/ban`, null, { params: { ban } })
}

/** 知识库 */
export function listKnowledge(category?: string) {
  return get<Knowledge[]>('/admin/knowledge', { category })
}

export function saveKnowledge(data: Knowledge) {
  return post<void>('/admin/knowledge', data)
}

export function removeKnowledge(id: string) {
  return del<void>(`/admin/knowledge/${id}`)
}

/** 工单 */
export function listTickets(params?: { status?: number; pageNum?: number; pageSize?: number }) {
  return get<PageResult<Ticket>>('/tickets', params)
}

export function handleTicket(id: string, reply: string, close = false) {
  return post<void>(`/tickets/${id}/handle`, null, { params: { reply, close } })
}
