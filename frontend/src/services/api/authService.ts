import type { AdminUser } from '@/types/auth'

import { httpClient } from './httpClient'

export async function login(email: string, password: string): Promise<AdminUser> {
  const { data } = await httpClient.post<AdminUser>('/auth/login', { email, password })
  return data
}

export async function logout(): Promise<void> {
  await httpClient.post('/auth/logout')
}

/** Administrador de la sesión actual; responde 401 si no hay sesión. */
export async function getCurrentAdmin(): Promise<AdminUser> {
  const { data } = await httpClient.get<AdminUser>('/auth/me')
  return data
}

export async function changePassword(currentPassword: string, newPassword: string): Promise<AdminUser> {
  const { data } = await httpClient.post<AdminUser>('/auth/change-password', { currentPassword, newPassword })
  return data
}
