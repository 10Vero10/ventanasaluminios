import type { ReactNode } from 'react'

import type { Loaded } from '@/lib/useLoad'

import { Button } from './button'
import { LoadingState, MessageState } from './states'

/**
 * Pinta los tres estados de una carga (antislop R-27): cargando, error con "Intentar de nuevo", y los datos. Con
 * {@code empty} también el estado vacío: devuelve qué mostrar cuando no hay datos, o false si sí los hay.
 */
export function Loadable<T>({
  state,
  loadingLabel,
  errorTitle,
  empty,
  children,
}: {
  state: Loaded<T>
  loadingLabel: string
  errorTitle: string
  empty?: (data: T) => ReactNode | false
  children: (data: T) => ReactNode
}) {
  if (state.status === 'loading') return <LoadingState label={loadingLabel} />
  if (state.status === 'error') {
    return (
      <MessageState
        tone="error"
        title={errorTitle}
        description={state.message}
        action={<Button onClick={state.reload}>Intentar de nuevo</Button>}
      />
    )
  }
  const emptyView = empty?.(state.data)
  if (emptyView) return <>{emptyView}</>
  return <>{children(state.data)}</>
}
