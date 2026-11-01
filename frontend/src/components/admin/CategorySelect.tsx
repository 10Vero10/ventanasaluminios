import { Select } from '@/components/ui/field'
import { CATEGORIES, type ProductCategory } from '@/types/catalog'

/** Lista de líneas; con {@code allowNone} agrega "Sin línea". */
export function CategorySelect({
  value,
  onChange,
  allowNone = false,
  ...props
}: {
  value: ProductCategory | null
  onChange: (value: ProductCategory | null) => void
  allowNone?: boolean
  id?: string
  'aria-describedby'?: string
  'aria-invalid'?: boolean
}) {
  return (
    <Select {...props} value={value ?? ''} onChange={(e) => onChange((e.target.value || null) as ProductCategory | null)}>
      {allowNone && <option value="">Sin línea</option>}
      {CATEGORIES.map((c) => (
        <option key={c.value} value={c.value}>
          {c.label}
        </option>
      ))}
    </Select>
  )
}
