import type { ReactNode } from 'react'

import type { Loaded } from '@/lib/useLoad'

import { Button } from './button'
import { LoadingState, MessageState } from './states'

/**
 * Pinta los tres estados de una carga (antislop R-27): cargando, error con "Intentar de nuevo", y los datos. Con
 * {@code isEmpty} y {@code empty} también el estado vacío.
 */
export function Loadable<T>({
  state,
  loadingLabel,
  errorTitle,
  isEmpty,
  empty,
  children,
}: {
  state: Loaded<T>
  loadingLabel: string
  errorTitle: string
  isEmpty?: (data: T) => boolean
  empty?: ReactNode
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
  if (isEmpty?.(state.data) && empty) return <>{empty}</>
  return <>{children(state.data)}</>
}
