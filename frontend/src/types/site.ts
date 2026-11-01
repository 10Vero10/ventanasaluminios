import type { ProductCategory } from './catalog'

export interface FeaturedLine {
  id: number
  position: number
  number: string
  title: string
  categoryTag: string
  description: string
  imageUrl: string | null
  linkText: string
  targetCategory: ProductCategory
}

/** Campos editables del sitio (mismos en la respuesta pública y en el panel). */
export interface SiteContent {
  heroTitle: string
  heroSubtitle: string
  heroImageUrl: string | null
  heroBadge: string | null
  phone: string
  email: string
  whatsappNumber: string
  experienceYears: number
  quotingEnabled: boolean
  faviconUrl: string | null
  aboutParagraph1: string
  aboutParagraph2: string
  aboutHighlight: string
}

export interface PublicSite extends SiteContent {
  featuredLines: FeaturedLine[]
}

export interface SiteSettings extends SiteContent {
  updatedAt: string
}

export interface GalleryImage {
  id: number
  position: number
  title: string
  url: string
  category: ProductCategory | null
}
