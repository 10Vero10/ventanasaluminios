import { Button } from '@/components/ui/button'

import { SaveFeedback } from './SaveFeedback'
import type { SaveState } from './useSaveState'

/**
 * Botón de guardar con su texto de espera. El resultado ({@code SaveFeedback}) lo ubica el padre, normalmente al final
 * de su fila de acciones para que aparecer no empuje los demás botones; {@code SaveRow} es la fila estándar.
 */
export function SaveButton({
  save,
  label = 'Guardar cambios',
  busyLabel = 'Guardando…',
  disabled,
  size,
}: {
  save: { state: SaveState; saving: boolean }
  label?: string
  busyLabel?: string
  disabled?: boolean
  size?: 'sm' | 'md'
}) {
  return (
    <Button type="submit" size={size} disabled={disabled || save.saving}>
      {save.saving ? busyLabel : label}
    </Button>
  )
}

/** Fila estándar: botón de guardar y su resultado al lado. */
export function SaveRow({ save, ...props }: Parameters<typeof SaveButton>[0]) {
  return (
    <div className="flex flex-wrap items-center gap-4">
      <SaveButton save={save} {...props} />
      <SaveFeedback state={save.state} />
    </div>
  )
}
