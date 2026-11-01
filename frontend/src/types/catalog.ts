/** Línea de producto. */
export type ProductCategory = 'VENTANAS' | 'PUERTAS' | 'PORTONES' | 'DIVISIONES'

export const CATEGORIES: { value: ProductCategory; label: string; number: string }[] = [
  { value: 'VENTANAS', label: 'Ventanas', number: '01' },
  { value: 'PUERTAS', label: 'Puertas', number: '02' },
  { value: 'PORTONES', label: 'Portones', number: '03' },
  { value: 'DIVISIONES', label: 'Divisiones', number: '04' },
]

export function categoryLabel(category: ProductCategory): string {
  return CATEGORIES.find((c) => c.value === category)?.label ?? category
}

export interface ProductSpec {
  label: string
  value: string
}

export interface Product {
  id: number
  position: number
  category: ProductCategory
  categoryNumber: string | null
  title: string
  subtitle: string
  description: string
  imageUrl: string | null
  badge: string | null
  ctaText: string | null
  active: boolean
  specs: ProductSpec[]
  features: string[]
}

export type ProductRequest = Omit<Product, 'id' | 'position'>

export interface GlassType {
  id: number
  position: number
  code: string
  name: string
  active: boolean
  defaultOption: boolean
}

export interface GlassColor extends GlassType {
  hex: string
  reflective: boolean
  note: string | null
}

export interface GlassThickness {
  id: number
  position: number
  mm: number
  active: boolean
  defaultOption: boolean
}

export interface GlassOptions {
  types: GlassType[]
  colors: GlassColor[]
  thicknesses: GlassThickness[]
}
