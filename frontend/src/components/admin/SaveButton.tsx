import { Button } from '@/components/ui/button'

import { SaveFeedback } from './SaveFeedback'
import type { SaveState } from './useSaveState'

/** Botón de guardar con su texto de espera y el resultado al lado ("¡Guardado con éxito!" o el error). */
export function SaveButton({
  save,
  label = 'Guardar cambios',
  busyLabel = 'Guardando…',
  disabled,
  size,
  type = 'submit',
  onClick,
}: {
  save: { state: SaveState; saving: boolean }
  label?: string
  busyLabel?: string
  disabled?: boolean
  size?: 'sm' | 'md'
  type?: 'submit' | 'button'
  onClick?: () => void
}) {
  return (
    <div className="flex flex-wrap items-center gap-4">
      <Button type={type} size={size} disabled={disabled || save.saving} onClick={onClick}>
        {save.saving ? busyLabel : label}
      </Button>
      <SaveFeedback state={save.state} />
    </div>
  )
}
