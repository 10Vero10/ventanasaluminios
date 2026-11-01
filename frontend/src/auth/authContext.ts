import { createContext, useContext } from 'react'

import type { AdminUser } from '@/types/auth'

export interface AuthState {
  /** undefined mientras se consulta la sesión; null si no hay sesión. */
  user: AdminUser | null | undefined
  /** true si la sesión se cerró porque el backend la rechazó (vencida o revocada). */
  expired: boolean
  signIn: (email: string, password: string) => Promise<AdminUser>
  signOut: () => Promise<void>
  changePassword: (currentPassword: string, newPassword: string) => Promise<AdminUser>
}

export const AuthContext = createContext<AuthState | null>(null)

export function useAuth(): AuthState {
  const auth = useContext(AuthContext)
  if (!auth) {
    throw new Error('useAuth debe usarse dentro de AuthProvider')
  }
  return auth
}
