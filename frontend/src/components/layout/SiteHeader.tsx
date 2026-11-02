import { AnimatePresence, motion, useReducedMotion } from 'motion/react'
import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, NavLink, useLocation } from 'react-router'

import { Button } from '@/components/ui/button'
import { mailHref, phoneHref, whatsappHref } from '@/lib/contact'
import { ROUTES } from '@/lib/routes'
import { cn } from '@/lib/utils'
import { useSite } from '@/site/siteContext'

import { BrandMark } from './BrandMark'
import { type NavItem, PAGE_CONTENT_ID, visibleNav } from './navigation'

/**
 * Cabecera translúcida (el contenido pasa por debajo, apple-design §12). En pantallas chicas los enlaces pasan al
 * menú y a la barra inferior.
 */
export function SiteHeader() {
  const site = useSite()
  const [menuOpen, setMenuOpen] = useState(false)
  const toggleRef = useRef<HTMLButtonElement>(null)
  const location = useLocation()
  const items = visibleNav(site.quotingEnabled)

  // Cambiar de página cierra el menú.
  const [lastPath, setLastPath] = useState(location.pathname)
  if (lastPath !== location.pathname) {
    setLastPath(location.pathname)
    setMenuOpen(false)
  }

  const closeMenu = useCallback(() => {
    setMenuOpen(false)
    toggleRef.current?.focus()
  }, [])

  return (
    // El desenfoque va en una capa (::before) y no en <header>: con backdrop-filter en el propio header, el navegador lo
    // vuelve el marco de los elementos "fixed" y el menú del celular quedaba recortado a la altura de la cabecera.
    <header className="sticky top-0 z-50 isolate border-b border-black/5 before:absolute before:inset-0 before:-z-10 before:bg-white/80 before:backdrop-blur-xl before:backdrop-saturate-150 supports-[backdrop-filter]:before:bg-white/70">
      <div className="hidden border-b border-black/5 md:block">
        <div className="page-container flex items-center justify-between py-1.5 text-xs text-muted-foreground">
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
      <nav aria-label="Principal" className="page-container flex h-header items-center justify-between gap-4">
        <Link to={ROUTES.home} className="flex items-center gap-2.5 rounded-md font-display text-body font-semibold tracking-[-0.01em]">
          <BrandMark className="size-7" />
          Ventanas y Aluminios
        </Link>
        <ul className="hidden items-center gap-1 md:flex">
          {items.map((item) => (
            <li key={item.to}>
              <NavLink
                to={item.to}
                end={item.end}
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
            ref={toggleRef}
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
      <MobileMenu open={menuOpen} items={items} onClose={closeMenu} />
    </header>
  )
}

/**
 * Menú del celular, justo después de su botón en el documento (orden de lectura y de Tab correcto). Al abrirse lleva el
 * foco al primer enlace, se cierra con Escape devolviendo el foco al botón, y deja inerte el resto de la página para que
 * Tab no llegue a lo que queda tapado.
 */
function MobileMenu({ open, items, onClose }: { open: boolean; items: NavItem[]; onClose: () => void }) {
  const site = useSite()
  const reduceMotion = useReducedMotion()
  const firstLinkRef = useRef<HTMLAnchorElement>(null)

  useEffect(() => {
    if (!open) return
    const page = document.getElementById(PAGE_CONTENT_ID)
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    page?.setAttribute('inert', '')
    firstLinkRef.current?.focus()
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.body.style.overflow = previousOverflow
      page?.removeAttribute('inert')
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [open, onClose])

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          id="menu-movil"
          className="fixed inset-x-0 top-header bottom-0 overflow-y-auto bg-white px-6 pt-4 pb-28 md:hidden"
          initial={reduceMotion ? { opacity: 0 } : { opacity: 0, y: -12 }}
          animate={{ opacity: 1, y: 0 }}
          exit={reduceMotion ? { opacity: 0 } : { opacity: 0, y: -12 }}
          transition={{ type: 'spring', bounce: 0, duration: 0.3 }}
        >
          <ul className="flex flex-col">
            {items.map((item, index) => (
              <li key={item.to}>
                <NavLink
                  ref={index === 0 ? firstLinkRef : undefined}
                  to={item.to}
                  end={item.end}
                  className={({ isActive }) =>
                    cn(
                      'block border-b border-border py-4 font-display text-title-2 font-semibold tracking-[-0.015em]',
                      !isActive && 'text-foreground/70',
                    )
                  }
                >
                  {item.label}
                </NavLink>
              </li>
            ))}
          </ul>
          <div className="mt-8 flex flex-col gap-3 text-body">
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
