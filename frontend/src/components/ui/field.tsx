import type { ComponentProps, ReactNode } from 'react'
import { useId } from 'react'

import { cn } from '@/lib/utils'

const controlClass =
  'w-full rounded-[12px] border border-input bg-background px-4 py-3 text-body tracking-[-0.022em] text-foreground placeholder:text-muted-foreground transition-colors focus-visible:border-ring focus-visible:outline-2 focus-visible:outline-offset-0 focus-visible:outline-ring/40 disabled:bg-muted aria-invalid:border-destructive'

/** Lo que Field pasa al control para conectarlo con su etiqueta, su ayuda y su error. */
export interface FieldControlProps {
  id: string
  'aria-describedby'?: string
  'aria-invalid'?: boolean
  'aria-required'?: boolean
}

interface FieldLabelProps {
  label: string
  hint?: ReactNode
  error?: string | null
  required?: boolean
  className?: string
}

interface FieldProps extends FieldLabelProps {
  children: (props: FieldControlProps) => ReactNode
}

/** Campo con etiqueta visible, ayuda y error conectados por id (antislop R-39). */
export function Field({ label, hint, error, required, children, className }: FieldProps) {
  const id = useId()
  const hintId = hint ? `${id}-hint` : undefined
  const errorId = error ? `${id}-error` : undefined
  const describedBy = [hintId, errorId].filter(Boolean).join(' ') || undefined
  return (
    <div className={cn('flex flex-col gap-2', className)}>
      <label htmlFor={id} className="text-sm font-semibold text-foreground">
        {label}
        {required && <span aria-hidden="true"> *</span>}
      </label>
      {children({ id, 'aria-describedby': describedBy, 'aria-invalid': error ? true : undefined, 'aria-required': required || undefined })}
      {hint && (
        <p id={hintId} className="text-sm text-muted-foreground">
          {hint}
        </p>
      )}
      {error && (
        <p id={errorId} className="text-sm text-destructive" role="alert">
          {error}
        </p>
      )}
    </div>
  )
}

export function Input({ className, ...props }: ComponentProps<'input'>) {
  return <input className={cn(controlClass, className)} {...props} />
}

export function Textarea({ className, ...props }: ComponentProps<'textarea'>) {
  return <textarea className={cn(controlClass, 'min-h-32 resize-y', className)} {...props} />
}

export type SelectProps = ComponentProps<'select'> & {
  /** Más baja, para filtros y listas dentro de una fila. */
  compact?: boolean
}

export function Select({ className, compact = false, ...props }: SelectProps) {
  return <select className={cn(controlClass, 'appearance-auto pr-10', compact && 'py-2 text-base sm:text-sm', className)} {...props} />
}

type TextFieldProps = FieldLabelProps & {
  value: string
  onChange: (value: string) => void
} & Omit<ComponentProps<'input'>, 'value' | 'onChange' | 'className' | 'id'> & { inputClassName?: string }

/** Campo de texto completo: etiqueta, control y ayuda; {@code onChange} recibe el texto. */
export function TextField({
  label,
  hint,
  error,
  required,
  className,
  inputClassName,
  value,
  onChange,
  ...inputProps
}: TextFieldProps) {
  return (
    <Field label={label} hint={hint} error={error} required={required} className={className}>
      {(props) => (
        <Input {...inputProps} {...props} className={inputClassName} value={value} onChange={(e) => onChange(e.target.value)} />
      )}
    </Field>
  )
}

/** Texto largo con etiqueta; {@code onChange} recibe el texto. */
export function TextAreaField({
  label,
  hint,
  error,
  required,
  className,
  value,
  onChange,
  ...textareaProps
}: FieldLabelProps & { value: string; onChange: (value: string) => void } & Omit<
    ComponentProps<'textarea'>,
    'value' | 'onChange' | 'className' | 'id'
  >) {
  return (
    <Field label={label} hint={hint} error={error} required={required} className={className}>
      {(props) => <Textarea {...textareaProps} {...props} value={value} onChange={(e) => onChange(e.target.value)} />}
    </Field>
  )
}
