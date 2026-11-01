import { createContext, useContext } from 'react'

import type { PublicSite } from '@/types/site'

export const SiteContext = createContext<PublicSite | null>(null)

/** Configuración pública del sitio. Solo se usa debajo de SiteProvider, que no renderiza nada hasta tenerla. */
export function useSite(): PublicSite {
  const site = useContext(SiteContext)
  if (!site) {
    throw new Error('useSite debe usarse dentro de SiteProvider')
  }
  return site
}
