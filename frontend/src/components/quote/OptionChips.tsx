import type { ReactNode } from 'react'

import { cn } from '@/lib/utils'

/**
 * Opciones excluyentes como píldoras. Por dentro son radios nativos: el teclado (flechas, Tab) y los lectores de
 * pantalla funcionan sin código extra.
 */
export function OptionChips<T extends string | number>({
  legend,
  name,
  options,
  value,
  onChange,
  hint,
}: {
  legend: string
  name: string
  options: { value: T; label: string; swatch?: string }[]
  value: T
  onChange: (value: T) => void
  hint?: ReactNode
}) {
  return (
    <fieldset>
      <legend className="text-sm font-semibold">{legend}</legend>
      <div className="mt-3 flex flex-wrap gap-2">
        {options.map((option) => {
          const checked = option.value === value
          return (
            <label
              key={option.value}
              className={cn(
                'relative inline-flex min-h-10 cursor-pointer items-center gap-2 rounded-full border px-4 text-sm transition-[transform,background-color,border-color] duration-100 select-none active:scale-[0.97] has-[:focus-visible]:outline-2 has-[:focus-visible]:outline-offset-2 has-[:focus-visible]:outline-ring motion-reduce:transition-none',
                checked ? 'border-foreground bg-foreground text-background' : 'border-border bg-background hover:border-input',
              )}
            >
              <input
                type="radio"
                name={name}
                value={String(option.value)}
                checked={checked}
                onChange={() => onChange(option.value)}
                className="sr-only"
              />
              {option.swatch && (
                <span
                  aria-hidden="true"
                  className="size-4 rounded-full border border-black/15"
                  style={{ backgroundColor: option.swatch }}
                />
              )}
              {option.label}
            </label>
          )
        })}
      </div>
      {hint && <p className="mt-2 text-sm text-muted-foreground">{hint}</p>}
    </fieldset>
  )
}
