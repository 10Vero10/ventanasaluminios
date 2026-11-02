import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'

import { useAuth } from '@/auth/authContext'
import { WakingLoadingState } from '@/components/ui/states'

/**
 * Protege el panel: sin sesión va al login; con la contraseña temporal sin cambiar, a cambiarla. El backend aplica las
 * mismas reglas; esto solo evita mostrar pantallas que fallarían.
 */
export function RequireAdmin({ children, allowPasswordChange = false }: { children: ReactNode; allowPasswordChange?: boolean }) {
  const { user } = useAuth()
  const location = useLocation()
  if (user === undefined) return <WakingLoadingState label="Comprobando la sesión…" />
  if (user === null) return <Navigate to="/admin/login" replace state={{ from: location.pathname }} />
  if (user.mustChangePassword && !allowPasswordChange) return <Navigate to="/admin/cambiar-clave" replace />
  return children
}
