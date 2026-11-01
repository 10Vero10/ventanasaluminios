import { Outlet } from 'react-router'

import { MobileTabBar } from './MobileTabBar'
import { SiteFooter } from './SiteFooter'
import { SiteHeader } from './SiteHeader'

/** Estructura común del sitio público: cabecera, contenido, pie y barra inferior en el celular. */
export function PublicLayout() {
  return (
    <>
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-50 focus:rounded-full focus:bg-background focus:px-4 focus:py-2"
      >
        Saltar al contenido
      </a>
      <SiteHeader />
      <main id="contenido">
        <Outlet />
      </main>
      <SiteFooter />
      <MobileTabBar />
    </>
  )
}
