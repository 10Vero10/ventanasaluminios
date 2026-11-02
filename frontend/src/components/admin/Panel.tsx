import type { ReactNode } from 'react'

import { cn } from '@/lib/utils'

/** Bloque del panel: título, ayuda corta y contenido sobre fondo blanco. */
export function Panel({
  title,
  description,
  actions,
  children,
  className,
}: {
  title: string
  description?: string
  actions?: ReactNode
  children: ReactNode
  className?: string
}) {
  return (
    <section className={cn('rounded-[24px] bg-background p-4 sm:rounded-[28px] sm:p-8', className)}>
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2 className="font-display text-title-3 leading-tight font-semibold tracking-[-0.01em]">{title}</h2>
          {description && <p className="mt-1 text-muted-foreground">{description}</p>}
        </div>
        {actions && <div className="flex flex-wrap gap-2">{actions}</div>}
      </div>
      <div className="mt-6">{children}</div>
    </section>
  )
}
