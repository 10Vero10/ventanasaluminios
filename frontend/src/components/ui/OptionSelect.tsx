import { Select } from './field'

/**
 * Lista desplegable sobre un conjunto cerrado de valores. Devuelve el valor ya tipado (busca la opción elegida en vez de
 * convertir el texto), y con {@code noneLabel} admite "ninguno" (null).
 */
export function OptionSelect<T extends string>({
  options,
  value,
  onChange,
  noneLabel,
  className,
  disabled,
  ...props
}: {
  options: readonly { value: T; label: string }[]
  value: T | null
  onChange: (value: T | null) => void
  noneLabel?: string
  className?: string
  disabled?: boolean
  id?: string
  'aria-describedby'?: string
  'aria-invalid'?: boolean
}) {
  return (
    <Select
      {...props}
      className={className}
      disabled={disabled}
      value={value ?? ''}
      onChange={(e) => onChange(options.find((o) => o.value === e.target.value)?.value ?? null)}
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
