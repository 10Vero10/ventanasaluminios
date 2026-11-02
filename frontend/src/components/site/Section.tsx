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
      <div className={cn('page-container py-12 md:py-24', className)}>{children}</div>
    </section>
  )
}

/** Antetítulo corto sobre un título de sección. */
export function Kicker({ children, className }: { children: ReactNode; className?: string }) {
  return <p className={cn('text-callout font-semibold text-status md:text-body', className)}>{children}</p>
}

const SECTION_TITLE_SIZES = {
  /** Título de una banda de página. */
  lg: 'text-title-2 sm:text-title-1 md:text-display',
  /** Título dentro de una columna (configurador, formulario). */
  md: 'text-title-3 md:text-title-1',
  /** Título de un bloque destacado a todo el ancho (asesoría). */
  wide: 'text-title-2 md:text-display-sm',
} as const

/** Título de sección; el tamaño se elige con {@code size} en vez de pisar clases. */
export function SectionTitle({
  id,
  children,
  className,
  size = 'lg',
}: {
  id?: string
  children: ReactNode
  className?: string
  size?: keyof typeof SECTION_TITLE_SIZES
}) {
  return (
    <h2
      id={id}
      className={cn(
        'mt-2 font-display leading-[1.1] font-semibold tracking-[-0.02em] text-balance',
        SECTION_TITLE_SIZES[size],
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
      <Lead className="mt-4 max-w-2xl md:mt-5">{lead}</Lead>
    </>
  )
}

/** Título principal (h1) de una página del sitio. */
export function PageTitle({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <h1
      className={cn(
        'mt-2 font-display text-display-xs leading-[1.08] font-semibold tracking-[-0.02em] text-balance sm:text-display-sm md:text-display-lg',
        className,
      )}
    >
      {children}
    </h1>
  )
}

/** Bajada de un título: texto secundario un poco más grande que el cuerpo en pantallas anchas. */
export function Lead({ children, className }: { children: ReactNode; className?: string }) {
  return <p className={cn('text-body text-muted-foreground md:text-intro', className)}>{children}</p>
}
