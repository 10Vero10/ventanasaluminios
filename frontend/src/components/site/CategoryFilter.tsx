import { CATEGORIES, type ProductCategory } from '@/types/catalog'
import { cn } from '@/lib/utils'

/**
 * Filtro por línea como control segmentado. Es un grupo de botones con {@code aria-pressed}: cada uno filtra la misma
 * lista, no navega a otra página.
 */
export function CategoryFilter({
  value,
  onChange,
  allLabel,
  label,
}: {
  value: ProductCategory | null
  onChange: (category: ProductCategory | null) => void
  allLabel: string
  label: string
}) {
  const options: { value: ProductCategory | null; label: string }[] = [
    { value: null, label: allLabel },
    ...CATEGORIES.map((c) => ({ value: c.value, label: c.label })),
  ]
  return (
    <div role="group" aria-label={label} className="-mx-5 overflow-x-auto px-5 sm:-mx-6 sm:px-6">
      <div className="inline-flex gap-1 rounded-full bg-secondary p-1">
        {options.map((option) => {
          const active = option.value === value
          return (
            <button
              key={option.label}
              type="button"
              aria-pressed={active}
              onClick={() => onChange(option.value)}
              className={cn(
                'min-h-10 rounded-full px-4 text-sm whitespace-nowrap transition-colors focus-visible:outline-2 focus-visible:outline-ring',
                active ? 'bg-background font-semibold text-foreground shadow-[0_0_0_1px_rgba(0,0,0,0.06)]' : 'text-foreground/70 hover:text-foreground',
              )}
            >
              {option.label}
            </button>
          )
        })}
      </div>
    </div>
  )
}
