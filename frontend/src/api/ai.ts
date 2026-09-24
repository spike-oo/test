import { get, post } from '@/utils/request'

export interface AiChatParams {
  sessionId?: string
  message: string
  shopId?: string
  limit?: number
}

export interface AiOrderResult {
  sessionId?: string
  reply: string
  dishes: unknown[]
  conditions?: Record<string, string>
  fallback: boolean
  modelVersion?: string
}

export interface AiChatResult {
  sessionId: string
  answer: string
  needTicket: boolean
  fallback: boolean
  knowledge?: string[]
  modelVersion?: string
}

/** AI 自然语言点餐 */
export function aiOrder(data: AiChatParams) {
  return post<AiOrderResult>('/ai/order', data)
}

/** 语义搜索 */
export function semanticSearch(data: AiChatParams) {
  return post<AiOrderResult>('/ai/search', data)
}

/** 个性化推荐：scene 可选 HOME / AI_ORDER / SEARCH */
export function recommend(scene = 'HOME', limit = 6) {
  return get<Record<string, unknown>>('/ai/recommend', { scene, limit })
}

/** 饮食分析报告 */
export function dietReport(days = 7) {
  return get<Record<string, unknown>>('/ai/diet-report', { days })
}

/** 智能客服 */
export function chat(data: AiChatParams) {
  return post<AiChatResult>('/ai/chat', data)
}

export function clearChatContext(sessionId: string) {
  return post<void>('/ai/chat/clear', null, { params: { sessionId } })
}

/** 商户端 AI 能力 */
export function reviewReport() {
  return get<Record<string, unknown>>('/merchant/ai/review-report')
}

export function businessReport() {
  return get<Record<string, unknown>>('/merchant/ai/business-report')
}

export function analyzeReview(reviewId: string) {
  return post<Record<string, unknown>>(`/merchant/ai/reviews/${reviewId}/analyze`)
}
