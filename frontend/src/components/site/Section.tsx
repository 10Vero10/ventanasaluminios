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
      <div className={cn('mx-auto max-w-[1080px] px-5 py-12 sm:px-6 md:py-24', className)}>{children}</div>
    </section>
  )
}

/** Antetítulo corto sobre un título de sección. */
export function Kicker({ children, className }: { children: ReactNode; className?: string }) {
  return <p className={cn('text-[15px] font-semibold text-status md:text-[17px]', className)}>{children}</p>
}

export function SectionTitle({ id, children, className }: { id?: string; children: ReactNode; className?: string }) {
  return (
    <h2
      id={id}
      className={cn(
        'mt-2 font-display text-[28px] leading-[1.1] font-semibold tracking-[-0.02em] text-balance sm:text-[32px] md:text-[48px]',
        className,
      )}
    >
      {children}
    </h2>
  )
}

/** Encabezado de una página del sitio: antetítulo, título principal y bajada. */
export function PageHeader({ kicker, title, lead }: { kicker: string; title: string; lead: ReactNode }) {
  return (
    <>
      <Kicker>{kicker}</Kicker>
      <PageTitle className="max-w-3xl">{title}</PageTitle>
      <p className="mt-4 max-w-2xl text-[17px] text-muted-foreground md:mt-5 md:text-[19px]">{lead}</p>
    </>
  )
}

/** Título principal (h1) de una página del sitio. */
export function PageTitle({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <h1
      className={cn(
        'mt-2 font-display text-[34px] leading-[1.08] font-semibold tracking-[-0.02em] text-balance sm:text-[40px] md:text-[64px]',
        className,
      )}
    >
      {children}
    </h1>
  )
}
