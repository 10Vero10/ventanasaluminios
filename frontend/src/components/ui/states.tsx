import type { ReactNode } from 'react'

import { cn } from '@/lib/utils'

/** Carga: dice qué se está cargando y lo anuncia a lectores de pantalla. */
export function LoadingState({ label, className }: { label: string; className?: string }) {
  return (
    <div role="status" className={cn('flex items-center gap-3 py-16 text-muted-foreground', className)}>
      <span
        aria-hidden="true"
        className="size-5 animate-spin rounded-full border-2 border-border border-t-foreground motion-reduce:animate-none"
      />
      {label}
    </div>
  )
}

/** Vacío o error: la causa y, si existe, la acción que lo resuelve. */
export function MessageState({
  title,
  description,
  action,
  tone = 'neutral',
  className,
}: {
  title: string
  description?: ReactNode
  action?: ReactNode
  tone?: 'neutral' | 'error'
  className?: string
}) {
  return (
    <div
      role={tone === 'error' ? 'alert' : undefined}
      className={cn('rounded-[28px] bg-secondary px-6 py-12 text-center', className)}
    >
      <p className={cn('font-display text-xl font-semibold', tone === 'error' && 'text-destructive')}>{title}</p>
      {description && <div className="mx-auto mt-2 max-w-md text-muted-foreground">{description}</div>}
      {action && <div className="mt-6 flex justify-center">{action}</div>}
    </div>
  )
}

/** Error en línea bajo un formulario o una acción; no pinta nada si no hay mensaje. */
export function InlineError({ message, className }: { message: string | null | undefined; className?: string }) {
  if (!message) return null
  return (
    <p role="alert" className={cn('text-sm text-destructive', className)}>
      {message}
    </p>
  )
}
