import { useState } from 'react'

import { getErrorMessage } from '@/services/api/httpClient'

import { moveItem } from './moveItem'

/**
 * Subir y bajar en una lista: la pantalla cambia al instante y luego se guarda el orden completo. Si el servidor lo
 * rechaza, vuelve el orden anterior, salvo que mientras tanto hayan llegado datos nuevos (esos se respetan).
 *
 * {@code locked} indica cuándo no se puede mover: mientras se guarda un orden o mientras la lista se recarga
 * ({@code refreshing}), para no ordenar sobre datos viejos.
 */
export function useReorder<T extends { id: number }>(
  items: T[] | null,
  setItems: (update: (current: T[]) => T[]) => void,
  saveOrder: (ids: number[]) => Promise<unknown>,
  refreshing = false,
) {
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const locked = busy || refreshing

  async function move(from: number, to: number) {
    if (!items || locked) return
    const previous = items
    const next = moveItem(items, from, to)
    setItems(() => next)
    setError(null)
    setBusy(true)
    try {
      await saveOrder(next.map((item) => item.id))
    } catch (err) {
      setItems((current) => (current === next ? previous : current))
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return { move, locked, error }
}
