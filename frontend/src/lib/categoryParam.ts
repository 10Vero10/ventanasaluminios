import { useSearchParams } from 'react-router'

import type { ProductCategory } from '@/types/catalog'

import { CATEGORY_PARAM, categoryFromParam, categoryToParam } from './routes'

/** Línea elegida en la URL (?linea=) y la función para cambiarla sin agregar una entrada al historial. */
export function useCategoryParam(): [ProductCategory | null, (category: ProductCategory | null) => void] {
  const [params, setParams] = useSearchParams()
  const category = categoryFromParam(params.get(CATEGORY_PARAM))
  const setCategory = (next: ProductCategory | null) =>
    setParams(next ? { [CATEGORY_PARAM]: categoryToParam(next) } : {}, { replace: true })
  return [category, setCategory]
}
