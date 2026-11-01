import { categoryLabel, type Product } from '@/types/catalog'

/** Tipo de producto que se precarga al cotizar desde el catálogo: "Corredizas (Ventanas)" (briefing §3.4). */
export function productTypeFor(product: Pick<Product, 'title' | 'category'>): string {
  return `${product.title} (${categoryLabel(product.category)})`
}

/** Enlace a Cotizar con el producto ya elegido. */
export function quoteHref(product: Pick<Product, 'title' | 'category'>): string {
  return `/cotizar?producto=${encodeURIComponent(productTypeFor(product))}`
}

/** Opciones del formulario de cotización (briefing §5.6.4): texto visible y valor guardado. */
export const PRODUCT_TYPE_OPTIONS = [
  { label: 'Ventanas arquitectónicas (corredizas o batientes)', value: 'Ventanas arquitectónicas' },
  { label: 'Puertas en aluminio, vidrio o PVC', value: 'Puertas en aluminio o vidrio' },
  { label: 'Portones tipo americano automatizados', value: 'Portones tipo americano' },
  { label: 'Divisiones en vidrio templado o acrílico', value: 'Divisiones de baño o oficina' },
  { label: 'Fachadas flotantes o vitrinas comerciales', value: 'Fachadas flotantes o vitrinas' },
]
