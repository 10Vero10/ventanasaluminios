import type { GlassColor, GlassThickness, GlassType, Product, ProductCategory, ProductRequest } from './catalog'
import type { FeaturedLine, SiteContent, SiteSettings } from './site'

export type SiteSettingsRequest = SiteContent

export type FeaturedLineRequest = Omit<FeaturedLine, 'id' | 'position'>

export interface GalleryImageRequest {
  title: string
  url: string
  category: ProductCategory | null
}

export type GlassTypeRequest = Omit<GlassType, 'id' | 'position'>
export type GlassColorRequest = Omit<GlassColor, 'id' | 'position'>
export type GlassThicknessRequest = Omit<GlassThickness, 'id' | 'position'>

/** Cada lista de opciones de vidrio con su forma de respuesta y de solicitud. */
export interface GlassKinds {
  types: { item: GlassType; request: GlassTypeRequest }
  colors: { item: GlassColor; request: GlassColorRequest }
  thicknesses: { item: GlassThickness; request: GlassThicknessRequest }
}

export type GlassKind = keyof GlassKinds

export interface BackupImportResult {
  featuredLines: number
  galleryImages: number
  products: number
  glassTypes: number
  glassColors: number
  glassThicknesses: number
}

/** Lo editable de cada respuesta, listo para enviarlo de vuelta. */
export function toSettingsRequest({ updatedAt, ...request }: SiteSettings): SiteSettingsRequest {
  return request
}

export function toFeaturedLineRequest({ id, position, ...request }: FeaturedLine): FeaturedLineRequest {
  return request
}

export function toProductRequest({ id, position, ...request }: Product): ProductRequest {
  return request
}
