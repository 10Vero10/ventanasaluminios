import { type FieldControlProps, Select, type SelectProps } from './field'

interface BaseProps<T extends string> extends Partial<FieldControlProps>, Pick<SelectProps, 'className' | 'disabled' | 'compact'> {
  options: readonly { value: T; label: string }[]
}

/** Con {@code noneLabel} hay una opción "ninguno" y el valor puede ser null; sin ella siempre hay un valor. */
type OptionSelectProps<T extends string> = BaseProps<T> &
  (
    | { noneLabel: string; value: T | null; onChange: (value: T | null) => void }
    | { noneLabel?: undefined; value: T; onChange: (value: T) => void }
  )

/**
 * Lista desplegable sobre un conjunto cerrado de valores. Devuelve el valor ya tipado: busca la opción elegida en vez de
 * convertir el texto.
 */
export function OptionSelect<T extends string>({ options, noneLabel, value, onChange, ...control }: OptionSelectProps<T>) {
  // null solo llega cuando hay opción "ninguno": sin ella todas las opciones tienen valor.
  const pick = onChange as (value: T | null) => void
  return (
    <Select
      {...control}
      value={value ?? ''}
      onChange={(e) => {
        const chosen = options.find((o) => o.value === e.target.value)?.value ?? null
        if (chosen !== null || noneLabel !== undefined) pick(chosen)
      }}
    >
      {noneLabel !== undefined && <option value="">{noneLabel}</option>}
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </Select>
  )
}
