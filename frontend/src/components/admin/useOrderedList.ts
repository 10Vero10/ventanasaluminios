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
  const [refresh, setRefresh] = useState(0)
  const loaded = useLoad(loader, [refresh])
  const [local, setLocal] = useState<{ source: T[]; items: T[] } | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const source = loaded.status === 'ready' ? loaded.data : null
  const items = local && local.source === source ? local.items : source

  async function move(from: number, to: number) {
    if (!items || !source) return
    const previous = items
    const next = moveItem(items, from, to)
    setLocal({ source, items: next })
    setError(null)
    setBusy(true)
    try {
      await saveOrder(next.map((item) => item.id))
    } catch (err) {
      setLocal({ source, items: previous })
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  /** Reemplaza un elemento en pantalla después de guardarlo. */
  function replace(item: T) {
    if (!items || !source) return
    setLocal({ source, items: items.map((current) => (current.id === item.id ? item : current)) })
  }

  return { loaded, items, move, replace, reload: () => setRefresh((n) => n + 1), error, busy }
}
