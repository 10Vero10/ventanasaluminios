import { categoryLabel, type Product, type ProductCategory } from '@/types/catalog'

/** Rutas del sitio público. Las páginas y enlaces las toman de aquí en vez de escribirlas a mano. */
export const ROUTES = {
  home: '/',
  catalog: '/catalogo',
  gallery: '/trabajos',
  about: '/nosotros',
  quote: '/cotizar',
} as const

/** Parámetro de la línea en Catálogo y Trabajos: ?linea=ventanas. */
export const CATEGORY_PARAM = 'linea'

export function categoryToParam(category: ProductCategory): string {
  return category.toLowerCase()
}

export function catalogHref(category: ProductCategory): string {
  return `${ROUTES.catalog}?${CATEGORY_PARAM}=${categoryToParam(category)}`
}

export function galleryHref(category: ProductCategory): string {
  return `${ROUTES.gallery}?${CATEGORY_PARAM}=${categoryToParam(category)}`
}

/** Tipo de producto que se precarga al cotizar desde el catálogo: "Corredizas (Ventanas)" (briefing §3.4). */
export function productTypeFor(product: Pick<Product, 'title' | 'category'>): string {
  return `${product.title} (${categoryLabel(product.category)})`
}

/** Enlace a Cotizar con el producto ya elegido. */
export function quoteHref(product: Pick<Product, 'title' | 'category'>): string {
  return `${ROUTES.quote}?producto=${encodeURIComponent(productTypeFor(product))}`
}
