import { useCallback, useEffect, useState } from 'react'

import { getErrorMessage } from '@/services/api/httpClient'

export type LoadState<T> =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'ready'; data: T; refreshing: boolean }

type Settled<T> = { depsKey: string; key: string } & (
  | { status: 'error'; message: string }
  | { status: 'ready'; data: T }
)

/**
 * Carga datos al montar o cuando cambian las dependencias (valores simples), y expone los tres estados que toda vista
 * con datos debe mostrar (antislop R-27).
 *
 * - Si cambian las dependencias (otra línea, otra página), el estado es "loading" hasta que llega la respuesta.
 * - Si solo se pide {@code reload()}, se siguen mostrando los datos anteriores con {@code refreshing: true}: la vista
 *   no se desmonta y no se pierden ediciones sin guardar ni mensajes de "guardado".
 */
export function useLoad<T>(loader: () => Promise<T>, deps: readonly unknown[]): LoadState<T> & { reload: () => void } {
  const [attempt, setAttempt] = useState(0)
  const [settled, setSettled] = useState<Settled<T> | null>(null)
  const depsKey = JSON.stringify(deps)
  const key = JSON.stringify([depsKey, attempt])

  useEffect(() => {
    let active = true
    loader()
      .then((data) => active && setSettled({ depsKey, key, status: 'ready', data }))
      .catch((error: unknown) => active && setSettled({ depsKey, key, status: 'error', message: getErrorMessage(error) }))
    return () => {
      active = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key])

  const reload = useCallback(() => setAttempt((n) => n + 1), [])
  if (!settled || settled.depsKey !== depsKey) return { status: 'loading', reload }
  const refreshing = settled.key !== key
  if (settled.status === 'ready') return { status: 'ready', data: settled.data, refreshing, reload }
  return refreshing ? { status: 'loading', reload } : { status: 'error', message: settled.message, reload }
}
