import { del, get, post, put } from '@/utils/request'

export interface UserProfile {
  userId: string
  username: string
  nickname?: string
  phone?: string
  avatar?: string
  role?: string
  balance?: number
}

/** 饮食档案（对应后端 DietProfileDTO） */
export interface DietProfile {
  dietGoal?: number
  tastePreference?: string
  allergyFoods?: string
  dislikeFoods?: string
  dailyBudget?: number
}

export interface UserAddress {
  id?: string
  receiver: string
  phone: string
  address: string
  isDefault?: number
}

export function getProfile() {
  return get<UserProfile>('/user/profile')
}

export function getDietProfile() {
  return get<DietProfile>('/user/diet-profile')
}

export function saveDietProfile(data: DietProfile) {
  return put<void>('/user/diet-profile', data)
}

export function listAddress() {
  return get<UserAddress[]>('/user/address')
}

export function addAddress(data: UserAddress) {
  return post<void>('/user/address', data)
}

export function removeAddress(id: string) {
  return del<void>(`/user/address/${id}`)
}
