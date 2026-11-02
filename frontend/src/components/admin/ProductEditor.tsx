import { type FormEvent, type ReactNode, useState } from 'react'

import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { DialogFooter } from '@/components/ui/dialog'
import { Field, TextAreaField, TextField } from '@/components/ui/field'
import { createProduct, updateProduct } from '@/services/api/adminCatalogService'
import { toProductRequest } from '@/types/admin'
import { CATEGORIES, type Product, type ProductCategory, type ProductRequest } from '@/types/catalog'

import { CategorySelect } from './CategorySelect'
import { ImagePicker } from './ImagePicker'
import { SaveRow } from './SaveRow'
import { useDraft } from './useDraft'
import { useSaveState } from './useSaveState'

const MAX_ROWS = 12

function emptyProduct(category: ProductCategory): ProductRequest {
  return {
    category,
    categoryNumber: CATEGORIES.find((c) => c.value === category)?.number ?? null,
    title: '',
    subtitle: '',
    description: '',
    imageUrl: null,
    badge: null,
    ctaText: null,
    active: true,
    specs: [],
    features: [],
  }
}

/**
 * Editor de un producto (briefing §5.7.4): edita {@code product} o, si es null, crea uno nuevo en
 * {@code newCategory}. La ficha técnica y las ventajas se guardan en el orden de la pantalla; las filas vacías se
 * descartan.
 */
export function ProductEditor({
  product,
  newCategory,
  onSaved,
  onCancel,
}: {
  product: Product | null
  newCategory: ProductCategory
  onSaved: () => void
  onCancel: () => void
}) {
  const [initial] = useState<ProductRequest>(() => (product ? toProductRequest(product) : emptyProduct(newCategory)))
  const { draft, set, bind, bindOptional } = useDraft(initial)
  const save = useSaveState()

  async function submit(event: FormEvent) {
    event.preventDefault()
    const request: ProductRequest = {
      ...draft,
      specs: draft.specs.filter((s) => s.label.trim() || s.value.trim()),
      features: draft.features.filter((f) => f.trim()),
    }
    const saved = await save.run(() => (product ? updateProduct(product.id, request) : createProduct(request)))
    if (saved) onSaved()
  }

  return (
    <form noValidate onSubmit={submit} className="flex flex-col gap-6">
      <div className="grid gap-5 md:grid-cols-2">
        <TextField label="Título" required maxLength={80} {...bind('title')} />
        <TextField label="Subtítulo" required maxLength={120} {...bind('subtitle')} />
        <Field label="Línea">
          {(props) => <CategorySelect {...props} value={draft.category} onChange={(category) => set('category', category)} />}
        </Field>
        <TextField label="Número de la línea" hint="Ejemplo: 01." maxLength={4} {...bindOptional('categoryNumber')} />
        <TextField label="Distintivo" hint="Opcional. Ejemplo: 4 obras instaladas." maxLength={60} {...bindOptional('badge')} />
        <TextField label="Texto del botón" hint='Opcional. Por defecto "Cotizar este sistema".' maxLength={60} {...bindOptional('ctaText')} />
      </div>
      <TextAreaField label="Descripción" required maxLength={600} {...bind('description')} />
      <Checkbox className="text-body" checked={draft.active} onChange={(active) => set('active', active)}>
        Visible en el catálogo
      </Checkbox>
      <ImagePicker label="Imagen del producto" value={draft.imageUrl} onChange={(url) => set('imageUrl', url)} aspect="16/10" />

      <EditableRows
        legend="Ficha técnica"
        addLabel="Agregar fila"
        rows={draft.specs}
        onChange={(specs) => set('specs', specs)}
        newRow={{ label: '', value: '' }}
        removeLabel={(i) => `Quitar fila ${i + 1} de la ficha técnica`}
        className="sm:grid-cols-[1fr_2fr_auto]"
        renderRow={(spec, i, update) => (
          <>
            <TextField label={`Etiqueta ${i + 1}`} maxLength={60} value={spec.label} onChange={(label) => update({ ...spec, label })} />
            <TextField label={`Valor ${i + 1}`} maxLength={150} value={spec.value} onChange={(value) => update({ ...spec, value })} />
          </>
        )}
      />
      <EditableRows
        legend="Ventajas"
        addLabel="Agregar ventaja"
        rows={draft.features}
        onChange={(features) => set('features', features)}
        newRow=""
        removeLabel={(i) => `Quitar ventaja ${i + 1}`}
        className="sm:grid-cols-[1fr_auto]"
        renderRow={(feature, i, update) => (
          <TextField label={`Ventaja ${i + 1}`} maxLength={150} value={feature} onChange={update} />
        )}
      />

      <DialogFooter>
        <SaveRow save={save} label="Guardar producto" className="gap-2 sm:gap-3">
          <Button variant="secondary" onClick={onCancel} disabled={save.saving}>
            Descartar cambios
          </Button>
        </SaveRow>
      </DialogFooter>
    </form>
  )
}

/** Lista editable de filas (ficha técnica o ventajas) con "Quitar" por fila y "Agregar" hasta 12. */
function EditableRows<T>({
  legend,
  addLabel,
  rows,
  onChange,
  newRow,
  removeLabel,
  className,
  renderRow,
}: {
  legend: string
  addLabel: string
  rows: T[]
  onChange: (rows: T[]) => void
  newRow: T
  removeLabel: (index: number) => string
  className: string
  renderRow: (row: T, index: number, update: (row: T) => void) => ReactNode
}) {
  return (
    <fieldset>
      <legend className="font-display text-xl font-semibold">{legend}</legend>
      <ul className="mt-3 flex flex-col gap-3">
        {rows.map((row, i) => (
          <li key={i} className={`grid items-end gap-3 ${className}`}>
            {renderRow(row, i, (next) => onChange(rows.map((r, j) => (j === i ? next : r))))}
            <Button size="sm" variant="ghost" aria-label={removeLabel(i)} onClick={() => onChange(rows.filter((_, j) => j !== i))}>
              Quitar
            </Button>
          </li>
        ))}
      </ul>
      <Button size="sm" variant="secondary" className="mt-3" disabled={rows.length >= MAX_ROWS} onClick={() => onChange([...rows, newRow])}>
        {addLabel}
      </Button>
    </fieldset>
  )
}
