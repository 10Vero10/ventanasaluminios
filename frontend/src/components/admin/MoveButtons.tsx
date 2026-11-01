import { Button } from '@/components/ui/button'

/** Subir y bajar un elemento de una lista ordenada. */
export function MoveButtons({
  label,
  index,
  count,
  onMove,
  disabled,
}: {
  /** Nombre del elemento para las etiquetas accesibles. */
  label: string
  index: number
  count: number
  onMove: (from: number, to: number) => void
  disabled?: boolean
}) {
  return (
    <>
      <Button
        size="sm"
        variant="ghost"
        aria-label={`Subir ${label}`}
        disabled={disabled || index === 0}
        onClick={() => onMove(index, index - 1)}
      >
        Subir
      </Button>
      <Button
        size="sm"
        variant="ghost"
        aria-label={`Bajar ${label}`}
        disabled={disabled || index === count - 1}
        onClick={() => onMove(index, index + 1)}
      >
        Bajar
      </Button>
    </>
  )
}
