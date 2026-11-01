import type { ComponentProps, ReactNode } from 'react'
import { useId } from 'react'

import { cn } from '@/lib/utils'

const controlClass =
  'w-full rounded-[12px] border border-input bg-background px-4 py-3 text-[17px] tracking-[-0.022em] text-foreground placeholder:text-muted-foreground transition-colors focus-visible:border-ring focus-visible:outline-2 focus-visible:outline-offset-0 focus-visible:outline-ring/40 disabled:bg-muted aria-invalid:border-destructive'

interface FieldProps {
  label: string
  hint?: ReactNode
  error?: string | null
  required?: boolean
  children: (props: { id: string; 'aria-describedby'?: string; 'aria-invalid'?: boolean }) => ReactNode
  className?: string
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
      {children({ id, 'aria-describedby': describedBy, 'aria-invalid': error ? true : undefined })}
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

export function Select({ className, ...props }: ComponentProps<'select'>) {
  return <select className={cn(controlClass, 'appearance-auto pr-10', className)} {...props} />
}
