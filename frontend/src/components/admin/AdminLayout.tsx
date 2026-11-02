import { useState } from 'react'
import { Link, NavLink, Outlet, useLocation } from 'react-router'

import { useAuth } from '@/auth/authContext'
import { BrandMark } from '@/components/layout/BrandMark'
import { Button } from '@/components/ui/button'
import { isTrue, routerStateValue } from '@/lib/routerState'
import { ROUTES } from '@/lib/routes'
import { useLoad } from '@/lib/useLoad'
import { cn } from '@/lib/utils'
import { getDashboard } from '@/services/api/adminSiteService'
import { getErrorMessage } from '@/services/api/httpClient'

import type { AdminOutletContext } from './adminOutlet'

/** Encabezado del panel con contadores y pestañas (briefing §5.7.2). */
export function AdminLayout() {
  const { user, signOut } = useAuth()
  const location = useLocation()
  const [signOutError, setSignOutError] = useState<string | null>(null)
  const dashboard = useLoad(getDashboard, [])
  const counts = dashboard.status === 'ready' ? dashboard.data : null
  const passwordChanged = routerStateValue(location.state, 'passwordChanged', isTrue)

  const tabs = [
    { to: '/admin/cotizaciones', label: counts ? `Cotizaciones (${counts.newQuotes})` : 'Cotizaciones' },
    { to: '/admin/inicio', label: 'Inicio' },
    { to: '/admin/catalogo', label: 'Catálogo' },
    { to: '/admin/vidrio', label: 'Vidrio' },
    { to: '/admin/nosotros', label: 'Quiénes somos' },
    { to: '/admin/configuracion', label: 'Configuración' },
  ]

  return (
    <div className="min-h-dvh bg-secondary">
      <header className="bg-background">
        <div className="mx-auto flex max-w-[1180px] flex-wrap items-center justify-between gap-x-4 gap-y-3 px-4 py-3 sm:px-6 sm:py-4">
          <div className="flex items-center gap-3">
            <BrandMark className="size-9 shrink-0" />
            <div className="leading-tight">
              <p className="text-callout font-semibold whitespace-nowrap">Ventanas y Aluminios</p>
              <p className="text-xs whitespace-nowrap text-muted-foreground">Panel de control</p>
            </div>
          </div>
          <div className="flex items-center gap-1 sm:gap-2">
            <span className="hidden text-sm text-muted-foreground md:inline">{user?.email}</span>
            <Button asChild size="sm" variant="ghost">
              <Link to="/admin/cambiar-clave">
                <span className="sm:hidden">Contraseña</span>
                <span className="hidden sm:inline">Cambiar contraseña</span>
              </Link>
            </Button>
            <Button asChild size="sm" variant="secondary">
              <a href={ROUTES.home} target="_blank" rel="noopener noreferrer">
                Ver sitio
              </a>
            </Button>
            <Button
              size="sm"
              variant="secondary"
              onClick={() => {
                setSignOutError(null)
                signOut().catch((error: unknown) =>
                  setSignOutError(`No se pudo cerrar la sesión. ${getErrorMessage(error)}`),
                )
              }}
            >
              <span className="sm:hidden">Salir</span>
              <span className="hidden sm:inline">Cerrar sesión</span>
            </Button>
          </div>
        </div>
        <div className="mx-auto max-w-[1180px] px-4 sm:px-6">
          <dl className="grid grid-cols-3 gap-2 pb-3 sm:gap-3 sm:pb-5">
            {[
              { label: 'Productos en catálogo', value: counts?.products },
              { label: 'Imágenes en galería', value: counts?.galleryImages },
              { label: 'Cotizaciones nuevas', value: counts?.newQuotes },
            ].map((item) => (
              <div key={item.label} className="rounded-[14px] bg-secondary px-3 py-2 sm:rounded-[16px] sm:px-4 sm:py-3">
                <dt className="text-caption leading-tight text-muted-foreground sm:text-xs">{item.label}</dt>
                <dd className="font-display text-xl font-semibold tabular-nums sm:text-2xl">{item.value ?? '·'}</dd>
              </div>
            ))}
          </dl>
          <nav aria-label="Secciones del panel" className="-mx-4 overflow-x-auto px-4 sm:-mx-6 sm:px-6">
            <ul className="flex gap-1">
              {tabs.map((tab) => (
                <li key={tab.to}>
                  <NavLink
                    to={tab.to}
                    className={({ isActive }) =>
                      cn(
                        'inline-flex min-h-11 items-center border-b-2 px-3 text-sm whitespace-nowrap transition-colors focus-visible:outline-2 focus-visible:outline-ring',
                        isActive ? 'border-foreground font-semibold text-foreground' : 'border-transparent text-muted-foreground hover:text-foreground',
                      )
                    }
                  >
                    {tab.label}
                  </NavLink>
                </li>
              ))}
            </ul>
          </nav>
        </div>
      </header>
      <main id="contenido" className="mx-auto max-w-[1180px] px-3 py-4 sm:px-6 sm:py-8">
        {signOutError && (
          <p role="alert" className="mb-6 rounded-[16px] bg-background p-4 text-sm text-destructive">
            {signOutError}
          </p>
        )}
        {passwordChanged && (
          <p role="status" className="mb-6 rounded-[16px] bg-background p-4 text-sm">
            Contraseña actualizada. Las demás sesiones se cerraron.
          </p>
        )}
        <Outlet context={{ refreshDashboard: dashboard.reload } satisfies AdminOutletContext} />
      </main>
    </div>
  )
}
