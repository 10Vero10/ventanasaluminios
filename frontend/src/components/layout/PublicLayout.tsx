import { useEffect } from 'react'
import { Outlet, useLocation } from 'react-router'

import { MobileTabBar } from './MobileTabBar'
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
        className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-50 focus:rounded-full focus:bg-background focus:px-4 focus:py-2"
      >
        Saltar al contenido
      </a>
      <SiteHeader />
      {/* En el celular la barra inferior es fija: este margen evita que tape el final de la página. */}
      <div className="pb-[calc(3.5rem+env(safe-area-inset-bottom))] md:pb-0">
        <main id="contenido">
          <Outlet />
        </main>
        <SiteFooter />
      </div>
      <MobileTabBar />
    </>
  )
}
