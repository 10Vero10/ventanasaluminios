import { useOutletContext } from 'react-router'

export interface AdminOutletContext {
  /** Vuelve a pedir los contadores (después de crear, borrar o cambiar el estado de algo). */
  refreshDashboard: () => void
}

export function useAdminOutlet(): AdminOutletContext {
  return useOutletContext<AdminOutletContext>()
}
