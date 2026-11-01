import { AnimatePresence, motion, useReducedMotion } from 'motion/react'
import { useEffect, useState } from 'react'
import { Link, NavLink, useLocation } from 'react-router'

import { Button } from '@/components/ui/button'
import { mailHref, phoneHref, whatsappHref } from '@/lib/contact'
import { cn } from '@/lib/utils'
import { useSite } from '@/site/siteContext'
import { ROUTES } from '@/lib/routes'

import { BrandMark } from './BrandMark'
import { visibleNav } from './navigation'

/**
 * Cabecera translúcida (el contenido pasa por debajo, apple-design §12). En pantallas chicas los enlaces pasan al
 * menú y a la barra inferior.
 */
export function SiteHeader() {
  const site = useSite()
  const [menuOpen, setMenuOpen] = useState(false)
  const location = useLocation()
  const items = visibleNav(site.quotingEnabled)

  // Cambiar de página cierra el menú.
  const [lastPath, setLastPath] = useState(location.pathname)
  if (lastPath !== location.pathname) {
    setLastPath(location.pathname)
    setMenuOpen(false)
  }

  return (
    <header className="sticky top-0 z-40 border-b border-black/5 bg-white/80 backdrop-blur-xl backdrop-saturate-150 supports-[backdrop-filter]:bg-white/70">
      <div className="hidden border-b border-black/5 md:block">
        <div className="mx-auto flex max-w-[1080px] items-center justify-between px-6 py-1.5 text-xs text-muted-foreground">
          <span>Fábrica · Instalación · A medida · {site.experienceYears}+ años</span>
          <span className="flex gap-5">
            <a href={mailHref(site.email)} className="hover:text-foreground">
              {site.email}
            </a>
            <a href={phoneHref(site.phone)} className="hover:text-foreground">
              {site.phone}
            </a>
          </span>
        </div>
      </div>
      <nav aria-label="Principal" className="mx-auto flex h-14 max-w-[1080px] items-center justify-between gap-4 px-6">
        <Link to="/" className="flex items-center gap-2.5 rounded-md font-display text-[17px] font-semibold tracking-[-0.01em]">
          <BrandMark className="size-7" />
          Ventanas y Aluminios
        </Link>
        <ul className="hidden items-center gap-1 md:flex">
          {items.map((item) => (
            <li key={item.to}>
              <NavLink
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) =>
                  cn(
                    'rounded-full px-3 py-2 text-sm transition-colors hover:text-foreground',
                    isActive ? 'font-semibold text-foreground' : 'text-foreground/70',
                  )
                }
              >
                {item.label}
              </NavLink>
            </li>
          ))}
        </ul>
        <div className="flex items-center gap-2">
          {site.quotingEnabled && (
            <Button asChild size="sm" className="hidden sm:inline-flex">
              <Link to={ROUTES.quote}>Cotizar proyecto</Link>
            </Button>
          )}
          <Button
            variant="ghost"
            size="sm"
            className="md:hidden"
            aria-expanded={menuOpen}
            aria-controls="menu-movil"
            onClick={() => setMenuOpen((open) => !open)}
          >
            {menuOpen ? 'Cerrar' : 'Menú'}
          </Button>
        </div>
      </nav>
      <MobileMenu open={menuOpen} />
    </header>
  )
}

function MobileMenu({ open }: { open: boolean }) {
  const site = useSite()
  const reduceMotion = useReducedMotion()

  useEffect(() => {
    if (!open) return
    const previous = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      document.body.style.overflow = previous
    }
  }, [open])

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          id="menu-movil"
          className="fixed inset-x-0 top-14 bottom-0 overflow-y-auto bg-white px-6 pt-4 pb-28 md:hidden"
          initial={reduceMotion ? { opacity: 0 } : { opacity: 0, y: -12 }}
          animate={{ opacity: 1, y: 0 }}
          exit={reduceMotion ? { opacity: 0 } : { opacity: 0, y: -12 }}
          transition={{ type: 'spring', bounce: 0, duration: 0.3 }}
        >
          <ul className="flex flex-col">
            {visibleNav(site.quotingEnabled).map((item) => (
              <li key={item.to}>
                <NavLink
                  to={item.to}
                  end={item.to === '/'}
                  className={({ isActive }) =>
                    cn(
                      'block border-b border-border py-4 font-display text-[28px] font-semibold tracking-[-0.015em]',
                      !isActive && 'text-foreground/70',
                    )
                  }
                >
                  {item.label}
                </NavLink>
              </li>
            ))}
          </ul>
          <div className="mt-8 flex flex-col gap-3 text-[17px]">
            <a href={phoneHref(site.phone)} className="text-link">
              {site.phone}
            </a>
            <a href={mailHref(site.email)} className="text-link">
              {site.email}
            </a>
            <p className="text-muted-foreground">Taller de carpintería y ensamble</p>
            {site.quotingEnabled && (
              <Button asChild variant="secondary" className="mt-2 self-start">
                <a href={whatsappHref(site.whatsappNumber)} target="_blank" rel="noopener noreferrer">
                  Escribir por WhatsApp
                </a>
              </Button>
            )}
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
