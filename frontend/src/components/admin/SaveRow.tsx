import type { ReactNode } from 'react'

import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

import { SaveFeedback } from './SaveFeedback'
import type { useSaveState } from './useSaveState'

type SaveProps = {
  save: Pick<ReturnType<typeof useSaveState>, 'saving' | 'state'>
  label?: string
  busyLabel?: string
  disabled?: boolean
  size?: 'sm' | 'md'
}

/**
 * Fila de guardado: el botón (envía el formulario), las acciones extra que se pasen como hijos y, al final, el
 * resultado ("¡Guardado con éxito!" o el error), para que al aparecer no empuje los demás botones.
 */
export function SaveRow({
  save,
  label = 'Guardar cambios',
  busyLabel = 'Guardando…',
  disabled,
  size,
  children,
  className,
}: SaveProps & { children?: ReactNode; className?: string }) {
  return (
    <div className={cn('flex flex-wrap items-center gap-4', className)}>
      <Button type="submit" size={size} disabled={disabled || save.saving}>
        {save.saving ? busyLabel : label}
      </Button>
      {children}
      <SaveFeedback state={save.state} />
    </div>
  )
}
