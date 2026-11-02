import { useEffect } from 'react'
import { Outlet, useLocation } from 'react-router'

import { MobileTabBar } from './MobileTabBar'
import { PAGE_CONTENT_ID } from './navigation'
import { SiteFooter } from './SiteFooter'
import { SiteHeader } from './SiteHeader'

/** Estructura común del sitio público: cabecera, contenido, pie y barra inferior en el celular. */
export function PublicLayout() {
  const { pathname } = useLocation()
  // Al cambiar de página se empieza arriba; los cambios de filtro (?linea=) no mueven el scroll.
  useEffect(() => {
    window.scrollTo({ top: 0 })
  }, [pathname])

  return (
    <>
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-[60] focus:rounded-full focus:bg-background focus:px-4 focus:py-2"
      >
        Saltar al contenido
      </a>
      <SiteHeader />
      {/* En el celular la barra inferior es fija: este margen evita que tape el final de la página. */}
      <div id={PAGE_CONTENT_ID} className="pb-[calc(var(--spacing-tabbar)+env(safe-area-inset-bottom))] md:pb-0">
        <main id="contenido">
          <Outlet />
        </main>
        <SiteFooter />
        <MobileTabBar />
      </div>
    </>
  )
}
