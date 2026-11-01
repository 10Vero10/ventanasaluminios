import type { ReactNode } from 'react'

import { cn } from '@/lib/utils'

/** Casilla nativa con su texto; el color sale del token primario. */
export function Checkbox({
  checked,
  onChange,
  children,
  className,
}: {
  checked: boolean
  onChange: (checked: boolean) => void
  children: ReactNode
  className?: string
}) {
  return (
    <label className={cn('flex items-center gap-2', className)}>
      <input type="checkbox" className="size-4 accent-primary" checked={checked} onChange={(e) => onChange(e.target.checked)} />
      {children}
    </label>
  )
}
