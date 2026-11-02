import { ROUTES } from '@/lib/routes'

/** Secciones públicas del sitio. "Cotizar" solo existe con la cotización activa (briefing §3.1.1). */
export interface NavItem {
  to: string
  label: string
  /** Solo se marca activo en esa ruta exacta (Inicio), no en las que empiezan igual. */
  end?: boolean
  quoting?: boolean
}

export const NAV_ITEMS: NavItem[] = [
  { to: ROUTES.home, label: 'Inicio', end: true },
  { to: ROUTES.catalog, label: 'Catálogo' },
  { to: ROUTES.gallery, label: 'Trabajos' },
  { to: ROUTES.about, label: 'Nosotros' },
  { to: ROUTES.quote, label: 'Cotizar', quoting: true },
]

export function visibleNav(quotingEnabled: boolean): NavItem[] {
  return NAV_ITEMS.filter((item) => !item.quoting || quotingEnabled)
}

/** Id del contenedor de la página (todo menos la cabecera): se vuelve inerte mientras el menú del celular está abierto. */
export const PAGE_CONTENT_ID = 'pagina'
