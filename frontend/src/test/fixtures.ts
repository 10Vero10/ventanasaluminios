import type { GlassOptions } from '@/types/catalog'
import type { PublicSite } from '@/types/site'

export const SITE: PublicSite = {
  heroTitle: 'Ventanas y Aluminios',
  heroSubtitle: 'Fabricación e instalación a medida.',
  heroImageUrl: null,
  heroBadge: null,
  phone: '+57 317 846 3260',
  email: 'contacto@ejemplo.com',
  whatsappNumber: '573178463260',
  experienceYears: 25,
  quotingEnabled: true,
  faviconUrl: null,
  aboutParagraph1: 'Párrafo uno.',
  aboutParagraph2: 'Párrafo dos.',
  aboutHighlight: 'Destacado.',
  featuredLines: [],
}

export const GLASS_OPTIONS: GlassOptions = {
  types: [
    { id: 1, position: 1, code: 'CRUDO', name: 'Crudo', active: true, defaultOption: true },
    { id: 2, position: 2, code: 'LAMINADO_TEMPLADO', name: 'Laminado templado', active: true, defaultOption: false },
  ],
  colors: [
    { id: 1, position: 1, code: 'TRANSPARENTE', name: 'Transparente', hex: '#E0F2FE', reflective: false, note: null, active: true, defaultOption: true },
    { id: 2, position: 2, code: 'BRONCE', name: 'Bronce', hex: '#966969', reflective: false, note: null, active: true, defaultOption: false },
  ],
  thicknesses: [
    { id: 1, position: 1, mm: 6, active: true, defaultOption: true },
    { id: 2, position: 2, mm: 8, active: true, defaultOption: false },
  ],
}
