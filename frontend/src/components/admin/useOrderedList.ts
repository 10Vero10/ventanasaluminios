import { useLoad } from '@/lib/useLoad'

import { useReorder } from './useReorder'

/**
 * Lista ordenable del panel: carga, reordenamiento optimista y reemplazo de un elemento guardado. Mientras se recarga,
 * {@code locked} es true para no ordenar sobre datos viejos.
 */
export function useOrderedList<T extends { id: number }>(
  loader: () => Promise<T[]>,
  saveOrder: (ids: number[]) => Promise<unknown>,
) {
  const loaded = useLoad(loader, [])
  const items = loaded.status === 'ready' ? loaded.data : null
  const reorder = useReorder(items, loaded.setData, saveOrder)
  const refreshing = loaded.status === 'ready' && loaded.refreshing

  /** Reemplaza un elemento en pantalla después de guardarlo. */
  function replace(item: T) {
    loaded.setData((current) => current.map((existing) => (existing.id === item.id ? item : existing)))
  }

  return {
    loaded,
    items,
    move: reorder.move,
    replace,
    reload: loaded.reload,
    error: reorder.error,
    busy: reorder.busy || refreshing,
  }
}
