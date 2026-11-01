import { useCallback, useEffect, useRef, useState } from 'react'

import { getErrorMessage } from '@/services/api/httpClient'

export type SaveState = { status: 'idle' } | { status: 'saving' } | { status: 'saved' } | { status: 'error'; message: string }

/** Estado de un guardado: "Guardando…", "¡Guardado con éxito!" por unos segundos, o el error en español. */
export function useSaveState() {
  const [state, setState] = useState<SaveState>({ status: 'idle' })
  const timer = useRef<number | undefined>(undefined)
  useEffect(() => () => window.clearTimeout(timer.current), [])

  const run = useCallback(async <T,>(action: () => Promise<T>): Promise<T | undefined> => {
    window.clearTimeout(timer.current)
    setState({ status: 'saving' })
    try {
      const result = await action()
      setState({ status: 'saved' })
      timer.current = window.setTimeout(() => setState({ status: 'idle' }), 3000)
      return result
    } catch (error) {
      setState({ status: 'error', message: getErrorMessage(error) })
      return undefined
    }
  }, [])

  return { state, run, saving: state.status === 'saving' }
}
