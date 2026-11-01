import { ROUTES } from '@/lib/routes'

/** Secciones públicas del sitio. "Cotizar" solo existe con la cotización activa (briefing §3.1.1). */
export interface NavItem {
  to: string
  label: string
  quoting?: boolean
}

export const NAV_ITEMS: NavItem[] = [
  { to: '/', label: 'Inicio' },
  { to: ROUTES.catalog, label: 'Catálogo' },
  { to: ROUTES.gallery, label: 'Trabajos' },
  { to: ROUTES.about, label: 'Nosotros' },
  { to: ROUTES.quote, label: 'Cotizar', quoting: true },
]

export function visibleNav(quotingEnabled: boolean): NavItem[] {
  return NAV_ITEMS.filter((item) => !item.quoting || quotingEnabled)
}
