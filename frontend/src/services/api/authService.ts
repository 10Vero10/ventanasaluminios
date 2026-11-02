import type { AdminUser } from '@/types/auth'

import { httpClient, isHttpStatus, WAKE_UP_TIMEOUT_MS } from './httpClient'

export async function login(email: string, password: string): Promise<AdminUser> {
  const { data } = await httpClient.post<AdminUser>('/auth/login', { email, password })
  return data
}

/** Cierra la sesión en el servidor. Un 401 cuenta como cerrada: ya no había sesión. */
export async function logout(): Promise<void> {
  try {
    await httpClient.post('/auth/logout')
  } catch (error) {
    if (!isHttpStatus(error, 401)) throw error
  }
}

/** Administrador de la sesión actual; responde 401 si no hay sesión. */
export async function getCurrentAdmin(): Promise<AdminUser> {
  // Primera solicitud al abrir el panel: puede tocar despertar el servidor.
  const { data } = await httpClient.get<AdminUser>('/auth/me', { timeout: WAKE_UP_TIMEOUT_MS })
  return data
}

export async function changePassword(currentPassword: string, newPassword: string): Promise<AdminUser> {
  const { data } = await httpClient.post<AdminUser>('/auth/change-password', { currentPassword, newPassword })
  return data
}
