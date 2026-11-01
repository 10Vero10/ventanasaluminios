import { CATEGORIES, type ProductCategory } from '@/types/catalog'

/** "ventanas" en la URL ↔ VENTANAS en la API. Un valor desconocido equivale a "todas". */
export function categoryFromParam(value: string | null): ProductCategory | null {
  const upper = value?.toUpperCase()
  return CATEGORIES.some((c) => c.value === upper) ? (upper as ProductCategory) : null
}

export function categoryToParam(category: ProductCategory | null): string | null {
  return category ? category.toLowerCase() : null
}
