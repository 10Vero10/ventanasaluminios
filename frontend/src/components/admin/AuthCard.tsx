import type { ReactNode } from 'react'
import { Link } from 'react-router'

import { BrandMark } from '@/components/layout/BrandMark'
import { ROUTES } from '@/lib/routes'

/** Marco centrado de las pantallas de acceso al panel. */
export function AuthCard({ title, description, children }: { title: string; description: string; children: ReactNode }) {
  return (
    <main className="flex min-h-dvh items-center justify-center bg-secondary px-4 py-12">
      <div className="w-full max-w-[420px] rounded-[28px] bg-background p-8 sm:p-10">
        <div className="flex items-center gap-3">
          <BrandMark className="size-10" />
          <span className="text-body font-semibold">Ventanas y Aluminios</span>
        </div>
        <h1 className="mt-8 font-display text-title-2 leading-tight font-semibold tracking-[-0.015em]">{title}</h1>
        <p className="mt-2 text-muted-foreground">{description}</p>
        <div className="mt-8">{children}</div>
        <Link to={ROUTES.home} className="mt-8 inline-block text-sm text-link hover:underline">
          Volver al sitio
        </Link>
      </div>
    </main>
  )
}
