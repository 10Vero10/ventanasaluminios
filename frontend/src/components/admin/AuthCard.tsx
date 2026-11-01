import type { ReactNode } from 'react'
import { Link } from 'react-router'

import { BrandMark } from '@/components/layout/BrandMark'

/** Marco centrado de las pantallas de acceso al panel. */
export function AuthCard({ title, description, children }: { title: string; description: string; children: ReactNode }) {
  return (
    <main className="flex min-h-dvh items-center justify-center bg-secondary px-4 py-12">
      <div className="w-full max-w-[420px] rounded-[28px] bg-background p-8 sm:p-10">
        <BrandMark />
        <h1 className="mt-8 font-display text-[28px] leading-tight font-semibold tracking-[-0.015em]">{title}</h1>
        <p className="mt-2 text-muted-foreground">{description}</p>
        <div className="mt-8">{children}</div>
        <Link to="/" className="mt-8 inline-block text-sm text-link hover:underline">
          Volver al sitio
        </Link>
      </div>
    </main>
  )
}
