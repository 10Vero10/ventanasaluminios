import { useSearchParams } from 'react-router'

import { CATEGORIES, type ProductCategory } from '@/types/catalog'

const PARAM = 'linea'

/** "ventanas" en la URL ↔ VENTANAS en la API. Un valor desconocido equivale a "todas". */
export function categoryFromParam(value: string | null): ProductCategory | null {
  return CATEGORIES.find((c) => c.value.toLowerCase() === value?.toLowerCase())?.value ?? null
}

function categoryToParam(category: ProductCategory): string {
  return category.toLowerCase()
}

/** Línea elegida en la URL (?linea=) y la función para cambiarla sin agregar una entrada al historial. */
export function useCategoryParam(): [ProductCategory | null, (category: ProductCategory | null) => void] {
  const [params, setParams] = useSearchParams()
  const category = categoryFromParam(params.get(PARAM))
  const setCategory = (next: ProductCategory | null) =>
    setParams(next ? { [PARAM]: categoryToParam(next) } : {}, { replace: true })
  return [category, setCategory]
}

export function catalogHref(category: ProductCategory): string {
  return `/catalogo?${PARAM}=${categoryToParam(category)}`
}

export function galleryHref(category: ProductCategory): string {
  return `/trabajos?${PARAM}=${categoryToParam(category)}`
}
