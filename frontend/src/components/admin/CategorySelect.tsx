import type { FieldControlProps } from '@/components/ui/field'
import { OptionSelect } from '@/components/ui/OptionSelect'
import { CATEGORIES, type ProductCategory } from '@/types/catalog'

/** Lista de líneas. */
export function CategorySelect(
  props: Partial<FieldControlProps> & { value: ProductCategory; onChange: (value: ProductCategory) => void },
) {
  return <OptionSelect {...props} options={CATEGORIES} />
}

/** Lista de líneas con "Sin línea" (null). */
export function OptionalCategorySelect(
  props: Partial<FieldControlProps> & { value: ProductCategory | null; onChange: (value: ProductCategory | null) => void },
) {
  return <OptionSelect {...props} options={CATEGORIES} noneLabel="Sin línea" />
}
