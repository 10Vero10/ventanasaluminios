import { type ReactNode, useCallback, useEffect, useMemo, useState } from 'react'

import * as authService from '@/services/api/authService'
import { onSessionExpired } from '@/services/api/httpClient'
import type { AdminUser } from '@/types/auth'

import { AuthContext, type AuthState } from './authContext'

/**
 * Sesión del panel. El token vive en una cookie httpOnly que JavaScript no puede leer: el estado se obtiene de
 * {@code GET /auth/me} y se borra cuando el backend responde 401 en el panel.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AdminUser | null | undefined>(undefined)
  const [expired, setExpired] = useState(false)

  useEffect(() => {
    let active = true
    authService
      .getCurrentAdmin()
      .then((admin) => active && setUser(admin))
      .catch(() => active && setUser(null))
    const stop = onSessionExpired(() => {
      setUser(null)
      setExpired(true)
    })
    return () => {
      active = false
      stop()
    }
  }, [])

  const signIn = useCallback(async (email: string, password: string) => {
    const admin = await authService.login(email, password)
    setExpired(false)
    setUser(admin)
    return admin
  }, [])

  const signOut = useCallback(async () => {
    try {
      await authService.logout()
    } finally {
      setExpired(false)
      setUser(null)
    }
  }, [])

  const changePassword = useCallback(async (currentPassword: string, newPassword: string) => {
    const admin = await authService.changePassword(currentPassword, newPassword)
    setUser(admin)
    return admin
  }, [])

  const value = useMemo<AuthState>(
    () => ({ user, expired, signIn, signOut, changePassword }),
    [user, expired, signIn, signOut, changePassword],
  )
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
