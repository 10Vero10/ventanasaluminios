import { useCallback, useEffect, useState } from 'react'

import { getErrorMessage } from '@/services/api/httpClient'

export type LoadState<T> =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'ready'; data: T }

type Settled<T> = { key: string } & ({ status: 'error'; message: string } | { status: 'ready'; data: T })

/**
 * Carga datos al montar o cuando cambian las dependencias (valores simples), y expone los tres estados que toda vista
 * con datos debe mostrar (antislop R-27). Mientras la última solicitud no responde, el estado es "loading".
 */
export function useLoad<T>(loader: () => Promise<T>, deps: readonly unknown[]): LoadState<T> & { reload: () => void } {
  const [attempt, setAttempt] = useState(0)
  const [settled, setSettled] = useState<Settled<T> | null>(null)
  const key = JSON.stringify([...deps, attempt])

  useEffect(() => {
    let active = true
    loader()
      .then((data) => active && setSettled({ key, status: 'ready', data }))
      .catch((error: unknown) => active && setSettled({ key, status: 'error', message: getErrorMessage(error) }))
    return () => {
      active = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key])

  const reload = useCallback(() => setAttempt((n) => n + 1), [])
  if (!settled || settled.key !== key) return { status: 'loading', reload }
  return settled.status === 'ready'
    ? { status: 'ready', data: settled.data, reload }
    : { status: 'error', message: settled.message, reload }
}
