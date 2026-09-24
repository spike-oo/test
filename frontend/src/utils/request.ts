import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

/** 与后端 com.campus.delivery.common.api.Result 对应 */
export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

/** 与后端 com.campus.delivery.common.api.PageResult 对应 */
export interface PageResult<T> {
  total: number
  pageNum: number
  pageSize: number
  list: T[]
}

/** 成功码（见后端 ResultCode.SUCCESS） */
const SUCCESS_CODE = 200
const UNAUTHORIZED_CODE = 401
export const TOKEN_KEY = 'campus_token'
export const ROLE_KEY = 'campus_role'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 20000
})

service.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const body = response.data as Result
    // 后端统一响应体：{ code, message, data }
    if (body.code === SUCCESS_CODE) {
      return body.data as never
    }
    if (body.code === UNAUTHORIZED_CODE) {
      handleUnauthorized()
      return Promise.reject(new Error(body.message || '登录已过期'))
    }
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  (error) => {
    if (error?.response?.status === 401) {
      handleUnauthorized()
    } else {
      ElMessage.error(error?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

function handleUnauthorized() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ROLE_KEY)
  ElMessage.warning('登录已过期，请重新登录')
  const redirect = encodeURIComponent(window.location.hash.replace(/^#/, '') || '/')
  window.location.hash = `/login?redirect=${redirect}`
}

/** GET：返回值直接是后端 Result.data */
export function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  return service.get(url, { params }) as unknown as Promise<T>
}

export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return service.post(url, data, config) as unknown as Promise<T>
}

export function put<T>(url: string, data?: unknown): Promise<T> {
  return service.put(url, data) as unknown as Promise<T>
}

export function del<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  return service.delete(url, { params }) as unknown as Promise<T>
}

export default service
