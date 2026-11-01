import { useState } from 'react'
import { Link, NavLink, Outlet, useLocation } from 'react-router'

import { useAuth } from '@/auth/authContext'
import { BrandMark } from '@/components/layout/BrandMark'
import { Button } from '@/components/ui/button'
import { useLoad } from '@/lib/useLoad'
import { cn } from '@/lib/utils'
import { getDashboard } from '@/services/api/adminSiteService'

import type { AdminOutletContext } from './adminOutlet'

/** Encabezado del panel con contadores y pestañas (briefing §5.7.2). */
export function AdminLayout() {
  const { user, signOut } = useAuth()
  const location = useLocation()
  const [refresh, setRefresh] = useState(0)
  const dashboard = useLoad(getDashboard, [refresh])
  const counts = dashboard.status === 'ready' ? dashboard.data : null
  const passwordChanged = (location.state as { passwordChanged?: boolean } | null)?.passwordChanged

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
        <div className="mx-auto flex max-w-[1180px] flex-wrap items-center justify-between gap-4 px-4 py-4 sm:px-6">
          <div className="flex items-center gap-3">
            <BrandMark className="size-9 shrink-0" />
            <div className="leading-tight">
              <p className="text-[15px] font-semibold whitespace-nowrap">Ventanas y Aluminios</p>
              <p className="text-xs whitespace-nowrap text-muted-foreground">Panel de control</p>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <span className="hidden text-sm text-muted-foreground md:inline">{user?.email}</span>
            <Button asChild size="sm" variant="ghost">
              <Link to="/admin/cambiar-clave">Cambiar contraseña</Link>
            </Button>
            <Button asChild size="sm" variant="secondary">
              <a href="/" target="_blank" rel="noopener noreferrer">
                Ver sitio
              </a>
            </Button>
            <Button size="sm" variant="secondary" onClick={() => void signOut()}>
              Cerrar sesión
            </Button>
          </div>
        </div>
        <div className="mx-auto max-w-[1180px] px-4 sm:px-6">
          <dl className="grid grid-cols-3 gap-3 pb-5">
            {[
              { label: 'Productos en catálogo', value: counts?.products },
              { label: 'Imágenes en galería', value: counts?.galleryImages },
              { label: 'Cotizaciones nuevas', value: counts?.newQuotes },
            ].map((item) => (
              <div key={item.label} className="rounded-[16px] bg-secondary px-4 py-3">
                <dt className="text-xs text-muted-foreground">{item.label}</dt>
                <dd className="font-display text-2xl font-semibold tabular-nums">{item.value ?? '·'}</dd>
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
      <main id="contenido" className="mx-auto max-w-[1180px] px-4 py-8 sm:px-6">
        {passwordChanged && (
          <p role="status" className="mb-6 rounded-[16px] bg-background p-4 text-sm">
            Contraseña actualizada. Las demás sesiones se cerraron.
          </p>
        )}
        <Outlet context={{ refreshDashboard: () => setRefresh((n) => n + 1) } satisfies AdminOutletContext} />
      </main>
    </div>
  )
}
