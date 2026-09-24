import { get, post } from '@/utils/request'
import type { PageResult } from '@/utils/request'

export interface Review {
  id: string
  orderId?: string
  shopId?: string
  userId?: string
  score?: number
  content?: string
  images?: string
  anonymous?: number
  aiTags?: string
  sentiment?: number
  reply?: string
  createTime?: string
}

export interface ReviewSubmitParams {
  orderId: string
  score: number
  content?: string
  images?: string
  anonymous?: number
}

/** 学生端：提交 / 查看评价 */
export function submitReview(data: ReviewSubmitParams) {
  return post<void>('/reviews', data)
}

export function listShopReviews(params: { shopId: string; pageNum?: number; pageSize?: number }) {
  return get<PageResult<Review>>('/reviews', params)
}

/** 商户端 */
export function listMyShopReviews(params?: { pageNum?: number; pageSize?: number }) {
  return get<PageResult<Review>>('/merchant/reviews', params)
}

export function replyReview(id: string, reply: string) {
  return post<void>(`/merchant/reviews/${id}/reply`, { reply })
}

export function reviewTags(limit = 20) {
  return get<Array<{ tagName: string; tagCount: number }>>('/merchant/reviews/tags', { limit })
}

export function reviewOverview() {
  return get<Record<string, unknown>>('/merchant/reviews/overview')
}
