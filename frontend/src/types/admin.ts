import type { GlassColor, GlassThickness, GlassType, ProductCategory } from './catalog'
import type { SiteContent } from './site'

export type SiteSettingsRequest = SiteContent

export interface FeaturedLineRequest {
  number: string
  title: string
  categoryTag: string
  description: string
  imageUrl: string | null
  linkText: string
  targetCategory: ProductCategory
}

export interface GalleryImageRequest {
  title: string
  url: string
  category: ProductCategory | null
}

export type GlassTypeRequest = Omit<GlassType, 'id' | 'position'>
export type GlassColorRequest = Omit<GlassColor, 'id' | 'position'>
export type GlassThicknessRequest = Omit<GlassThickness, 'id' | 'position'>

export interface BackupImportResult {
  featuredLines: number
  galleryImages: number
  products: number
  glassTypes: number
  glassColors: number
  glassThicknesses: number
}
