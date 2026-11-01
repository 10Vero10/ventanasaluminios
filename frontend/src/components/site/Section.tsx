import type { ReactNode } from 'react'

import { cn } from '@/lib/utils'

/** Bloque de página con el ancho y el aire de apple-style-reference; {@code tone="mist"} es la banda gris. */
export function Section({
  children,
  tone = 'white',
  className,
  id,
  labelledBy,
}: {
  children: ReactNode
  tone?: 'white' | 'mist'
  className?: string
  id?: string
  labelledBy?: string
}) {
  return (
    <section id={id} aria-labelledby={labelledBy} className={cn(tone === 'mist' ? 'bg-secondary' : 'bg-background')}>
      <div className={cn('mx-auto max-w-[1080px] px-6 py-16 md:py-24', className)}>{children}</div>
    </section>
  )
}

/** Antetítulo corto sobre un título de sección. */
export function Kicker({ children, className }: { children: ReactNode; className?: string }) {
  return <p className={cn('text-[17px] font-semibold text-status', className)}>{children}</p>
}

export function SectionTitle({ id, children, className }: { id?: string; children: ReactNode; className?: string }) {
  return (
    <h2
      id={id}
      className={cn(
        'mt-2 font-display text-[32px] leading-[1.1] font-semibold tracking-[-0.02em] text-balance md:text-[48px]',
        className,
      )}
    >
      {children}
    </h2>
  )
}
