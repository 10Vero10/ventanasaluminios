import { useId } from 'react'

import { clampCm, STEP_CM } from '@/lib/configurator'

/** Medida en cm con − / + de 5 en 5 y escritura directa; siempre se ajusta al rango (briefing §3.5.2). */
export function MeasureInput({
  label,
  name,
  value,
  max,
  onChange,
}: {
  label: string
  /** "ancho" o "alto", para las etiquetas accesibles. */
  name: string
  value: number
  max: number
  onChange: (value: number) => void
}) {
  const id = useId()
  const hintId = `${id}-rango`
  const set = (next: number) => onChange(clampCm(next, max))
  const stepButton =
    'flex size-11 shrink-0 items-center justify-center rounded-full bg-secondary text-xl transition-transform duration-100 active:scale-95 disabled:opacity-40 focus-visible:outline-2 focus-visible:outline-ring motion-reduce:transition-none'
  return (
    <div className="flex flex-col gap-2">
      <label htmlFor={id} className="text-sm font-semibold">
        {label}
      </label>
      <div className="flex items-center gap-2">
        <button
          type="button"
          className={stepButton}
          aria-label={`Disminuir ${name}`}
          disabled={value <= 0}
          onClick={() => set(value - STEP_CM)}
        >
          <span aria-hidden="true">−</span>
        </button>
        <input
          id={id}
          type="number"
          inputMode="numeric"
          min={0}
          max={max}
          step={1}
          value={value}
          aria-label={`${name} en centímetros`}
          aria-describedby={hintId}
          onChange={(event) => set(event.target.value === '' ? 0 : Number(event.target.value))}
          className="h-11 w-full min-w-0 rounded-[12px] border border-input bg-background text-center text-body tabular-nums focus-visible:border-ring focus-visible:outline-2 focus-visible:outline-ring/40"
        />
        <button
          type="button"
          className={stepButton}
          aria-label={`Aumentar ${name}`}
          disabled={value >= max}
          onClick={() => set(value + STEP_CM)}
        >
          <span aria-hidden="true">+</span>
        </button>
      </div>
      <p id={hintId} className="text-xs text-muted-foreground">
        Rango: 0 – {max} cm
      </p>
    </div>
  )
}
