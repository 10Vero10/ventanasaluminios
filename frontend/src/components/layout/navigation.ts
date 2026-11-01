/** Secciones públicas del sitio. "Cotizar" solo existe con la cotización activa (briefing §3.1.1). */
export interface NavItem {
  to: string
  label: string
  quoting?: boolean
}

export const NAV_ITEMS: NavItem[] = [
  { to: '/', label: 'Inicio' },
  { to: '/catalogo', label: 'Catálogo' },
  { to: '/trabajos', label: 'Trabajos' },
  { to: '/nosotros', label: 'Nosotros' },
  { to: '/cotizar', label: 'Cotizar', quoting: true },
]

export function visibleNav(quotingEnabled: boolean): NavItem[] {
  return NAV_ITEMS.filter((item) => !item.quoting || quotingEnabled)
}
