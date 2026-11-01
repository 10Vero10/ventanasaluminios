import { useEffect, type ReactNode } from 'react'

import { Button } from '@/components/ui/button'
import { LoadingState, MessageState } from '@/components/ui/states'
import { getSite } from '@/services/api/siteService'
import { useLoad } from '@/lib/useLoad'

import { SiteContext } from './siteContext'

/**
 * Carga la configuración pública una vez y mantiene el título, la meta descripción y el favicon de la página
 * (briefing §3.12). Sin configuración no hay sitio que mostrar: muestra carga o error con reintento.
 */
export function SiteProvider({ children }: { children: ReactNode }) {
  const site = useLoad(getSite, [])

  useEffect(() => {
    if (site.status !== 'ready') return
    const { heroTitle, heroSubtitle, faviconUrl } = site.data
    document.title = `${heroTitle} | Fabricación e Instalación a Medida`
    document.querySelector('meta[name="description"]')?.setAttribute('content', heroSubtitle)
    const icon = document.querySelector<HTMLLinkElement>('link[rel="icon"]')
    if (faviconUrl) {
      icon?.setAttribute('href', faviconUrl)
    } else {
      icon?.remove()
    }
  }, [site])

  if (site.status === 'loading') {
    return <LoadingState label="Cargando el sitio…" className="justify-center" />
  }
  if (site.status === 'error') {
    return (
      <main className="mx-auto max-w-xl px-6 py-24">
        <MessageState
          tone="error"
          title="No pudimos cargar el sitio"
          description={site.message}
          action={<Button onClick={site.reload}>Intentar de nuevo</Button>}
        />
      </main>
    )
  }
  return <SiteContext.Provider value={site.data}>{children}</SiteContext.Provider>
}
