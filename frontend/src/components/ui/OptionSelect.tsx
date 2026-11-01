import type { FieldControlProps } from './field'
import { Select } from './field'

interface BaseProps<T extends string> extends Partial<FieldControlProps> {
  options: readonly { value: T; label: string }[]
  className?: string
  disabled?: boolean
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
export function OptionSelect<T extends string>(props: OptionSelectProps<T>) {
  const { options, className, disabled, id } = props
  return (
    <Select
      id={id}
      aria-describedby={props['aria-describedby']}
      aria-invalid={props['aria-invalid']}
      aria-required={props['aria-required']}
      className={className}
      disabled={disabled}
      value={props.value ?? ''}
      onChange={(e) => {
        const chosen = options.find((o) => o.value === e.target.value)?.value
        if (props.noneLabel !== undefined) props.onChange(chosen ?? null)
        else if (chosen !== undefined) props.onChange(chosen)
      }}
    >
      {props.noneLabel !== undefined && <option value="">{props.noneLabel}</option>}
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </Select>
  )
}
