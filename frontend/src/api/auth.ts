import { post } from '@/utils/request'

/** 登录入参（对应后端 LoginRequest） */
export interface LoginParams {
  username: string
  password: string
}

/** 登录返回（对应后端 LoginResponse） */
export interface LoginResult {
  token: string
  userId: string
  role: string
  nickname?: string
  avatar?: string
}

export interface RegisterParams {
  username: string
  password: string
  phone: string
  nickname?: string
  role: string
}

/** 登录 */
export function login(data: LoginParams) {
  return post<LoginResult>('/auth/login', data)
}

/** 注册 */
export function register(data: RegisterParams) {
  return post<void>('/auth/register', data)
}

/** 退出登录 */
export function logout() {
  return post<void>('/auth/logout')
}
