import type { FieldControlProps } from '@/components/ui/field'
import { OptionSelect } from '@/components/ui/OptionSelect'
import { CATEGORIES, type ProductCategory } from '@/types/catalog'

/** Lista de líneas; con {@code allowNone} agrega "Sin línea". */
export function CategorySelect({
  value,
  onChange,
  allowNone = false,
  ...props
}: Partial<FieldControlProps> & {
  value: ProductCategory | null
  onChange: (value: ProductCategory | null) => void
  allowNone?: boolean
}) {
  return <OptionSelect {...props} options={CATEGORIES} value={value} onChange={onChange} noneLabel={allowNone ? 'Sin línea' : undefined} />
}
