import { CATEGORIES, categoryLabel, type Product, type ProductCategory } from '@/types/catalog'

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

/** Producto precargado en Cotizar: ?producto=Corredizas (Ventanas). */
export const QUOTE_PRODUCT_PARAM = 'producto'

/** VENTANAS en la API ↔ "ventanas" en la URL. */
export function categoryToParam(category: ProductCategory): string {
  return category.toLowerCase()
}

/** "ventanas" en la URL ↔ VENTANAS en la API. Un valor desconocido equivale a "todas". */
export function categoryFromParam(value: string | null): ProductCategory | null {
  return CATEGORIES.find((c) => c.value.toLowerCase() === value?.toLowerCase())?.value ?? null
}

export function catalogHref(category: ProductCategory): string {
  return `${ROUTES.catalog}?${CATEGORY_PARAM}=${categoryToParam(category)}`
}

export function galleryHref(category: ProductCategory): string {
  return `${ROUTES.gallery}?${CATEGORY_PARAM}=${categoryToParam(category)}`
}

/** Tipo de producto que se precarga al cotizar desde el catálogo: "Corredizas (Ventanas)" (briefing §3.4). */
function productTypeFor(product: Pick<Product, 'title' | 'category'>): string {
  return `${product.title} (${categoryLabel(product.category)})`
}

/** Enlace a Cotizar con el producto ya elegido. */
export function quoteHref(product: Pick<Product, 'title' | 'category'>): string {
  return `${ROUTES.quote}?${QUOTE_PRODUCT_PARAM}=${encodeURIComponent(productTypeFor(product))}`
}
