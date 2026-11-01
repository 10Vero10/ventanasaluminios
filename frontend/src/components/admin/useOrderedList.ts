import { useState } from 'react'

import { useLoad } from '@/lib/useLoad'
import { getErrorMessage } from '@/services/api/httpClient'

import { moveItem } from './moveItem'

/**
 * Lista ordenable del panel. Mover cambia la pantalla al instante y luego guarda el orden completo; si el servidor lo
 * rechaza, vuelve al orden anterior y muestra el error.
 */
export function useOrderedList<T extends { id: number }>(
  loader: () => Promise<T[]>,
  saveOrder: (ids: number[]) => Promise<unknown>,
) {
  const loaded = useLoad(loader, [])
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const items = loaded.status === 'ready' ? loaded.data : null

  async function move(from: number, to: number) {
    if (!items) return
    const next = moveItem(items, from, to)
    loaded.setData(() => next)
    setError(null)
    setBusy(true)
    try {
      await saveOrder(next.map((item) => item.id))
    } catch (err) {
      loaded.setData(() => items)
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  /** Reemplaza un elemento en pantalla después de guardarlo. */
  function replace(item: T) {
    loaded.setData((current) => current.map((existing) => (existing.id === item.id ? item : existing)))
  }

  return { loaded, items, move, replace, reload: loaded.reload, error, busy }
}
