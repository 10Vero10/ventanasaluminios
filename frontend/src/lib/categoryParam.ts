import { useSearchParams } from 'react-router'

import { CATEGORIES, type ProductCategory } from '@/types/catalog'

import { CATEGORY_PARAM, categoryToParam } from './routes'

/** "ventanas" en la URL ↔ VENTANAS en la API. Un valor desconocido equivale a "todas". */
export function categoryFromParam(value: string | null): ProductCategory | null {
  return CATEGORIES.find((c) => c.value.toLowerCase() === value?.toLowerCase())?.value ?? null
}

/** Línea elegida en la URL (?linea=) y la función para cambiarla sin agregar una entrada al historial. */
export function useCategoryParam(): [ProductCategory | null, (category: ProductCategory | null) => void] {
  const [params, setParams] = useSearchParams()
  const category = categoryFromParam(params.get(CATEGORY_PARAM))
  const setCategory = (next: ProductCategory | null) =>
    setParams(next ? { [CATEGORY_PARAM]: categoryToParam(next) } : {}, { replace: true })
  return [category, setCategory]
}
