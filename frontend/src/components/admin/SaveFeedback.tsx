import type { SaveState } from './useSaveState'

/** Resultado del último guardado, anunciado a lectores de pantalla. */
export function SaveFeedback({ state }: { state: SaveState }) {
  return (
    <p role="status" className="min-h-5 text-sm">
      {state.status === 'saved' && <span>¡Guardado con éxito!</span>}
      {state.status === 'error' && <span className="text-destructive">{state.message}</span>}
    </p>
  )
}
