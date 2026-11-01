import { type FormEvent, useState } from 'react'

import { Button } from '@/components/ui/button'
import { Field, Input, Textarea } from '@/components/ui/field'
import { createProduct, updateProduct } from '@/services/api/adminCatalogService'
import type { Product, ProductRequest } from '@/types/catalog'

import { CategorySelect } from './CategorySelect'
import { ImagePicker } from './ImagePicker'
import { SaveFeedback } from './SaveFeedback'
import { useSaveState } from './useSaveState'

const MAX_ROWS = 12

/** Editor de un producto (briefing §5.7.4). La ficha técnica y las ventajas se guardan en el orden de la pantalla. */
export function ProductEditor({
  product,
  initial,
  onSaved,
  onCancel,
}: {
  /** null para crear uno nuevo. */
  product: Product | null
  initial: ProductRequest
  onSaved: (product: Product) => void
  onCancel: () => void
}) {
  const [draft, setDraft] = useState<ProductRequest>(initial)
  const save = useSaveState()
  const set = <K extends keyof ProductRequest>(key: K, value: ProductRequest[K]) => setDraft((d) => ({ ...d, [key]: value }))

  async function submit(event: FormEvent) {
    event.preventDefault()
    const request: ProductRequest = {
      ...draft,
      specs: draft.specs.filter((s) => s.label.trim() || s.value.trim()),
      features: draft.features.filter((f) => f.trim()),
    }
    const saved = await save.run(() => (product ? updateProduct(product.id, request) : createProduct(request)))
    if (saved) onSaved(saved)
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-6">
      <div className="grid gap-5 md:grid-cols-2">
        <Field label="Título" required>
          {(props) => <Input {...props} maxLength={80} value={draft.title} onChange={(e) => set('title', e.target.value)} />}
        </Field>
        <Field label="Subtítulo" required>
          {(props) => <Input {...props} maxLength={120} value={draft.subtitle} onChange={(e) => set('subtitle', e.target.value)} />}
        </Field>
        <Field label="Línea">
          {(props) => <CategorySelect {...props} value={draft.category} onChange={(v) => v && set('category', v)} />}
        </Field>
        <Field label="Número de la línea" hint="Ejemplo: 01.">
          {(props) => <Input {...props} maxLength={4} value={draft.categoryNumber ?? ''} onChange={(e) => set('categoryNumber', e.target.value || null)} />}
        </Field>
        <Field label="Distintivo" hint="Opcional. Ejemplo: 4 obras instaladas.">
          {(props) => <Input {...props} maxLength={60} value={draft.badge ?? ''} onChange={(e) => set('badge', e.target.value || null)} />}
        </Field>
        <Field label="Texto del botón" hint='Opcional. Por defecto "Cotizar este sistema".'>
          {(props) => <Input {...props} maxLength={60} value={draft.ctaText ?? ''} onChange={(e) => set('ctaText', e.target.value || null)} />}
        </Field>
      </div>
      <Field label="Descripción" required>
        {(props) => <Textarea {...props} maxLength={600} value={draft.description} onChange={(e) => set('description', e.target.value)} />}
      </Field>
      <label className="flex items-center gap-3 text-[17px]">
        <input type="checkbox" className="size-5 accent-[#0071e3]" checked={draft.active} onChange={(e) => set('active', e.target.checked)} />
        Visible en el catálogo
      </label>
      <ImagePicker label="Imagen del producto" value={draft.imageUrl} onChange={(url) => set('imageUrl', url)} aspect="16/10" />

      <fieldset>
        <legend className="font-display text-xl font-semibold">Ficha técnica</legend>
        <ul className="mt-3 flex flex-col gap-3">
          {draft.specs.map((spec, i) => (
            <li key={i} className="grid items-end gap-3 sm:grid-cols-[1fr_2fr_auto]">
              <Field label={`Etiqueta ${i + 1}`}>
                {(props) => (
                  <Input
                    {...props}
                    maxLength={60}
                    value={spec.label}
                    onChange={(e) => set('specs', draft.specs.map((s, j) => (j === i ? { ...s, label: e.target.value } : s)))}
                  />
                )}
              </Field>
              <Field label={`Valor ${i + 1}`}>
                {(props) => (
                  <Input
                    {...props}
                    maxLength={150}
                    value={spec.value}
                    onChange={(e) => set('specs', draft.specs.map((s, j) => (j === i ? { ...s, value: e.target.value } : s)))}
                  />
                )}
              </Field>
              <Button size="sm" variant="ghost" aria-label={`Quitar fila ${i + 1} de la ficha técnica`} onClick={() => set('specs', draft.specs.filter((_, j) => j !== i))}>
                Quitar
              </Button>
            </li>
          ))}
        </ul>
        <Button
          size="sm"
          variant="secondary"
          className="mt-3"
          disabled={draft.specs.length >= MAX_ROWS}
          onClick={() => set('specs', [...draft.specs, { label: '', value: '' }])}
        >
          Agregar fila
        </Button>
      </fieldset>

      <fieldset>
        <legend className="font-display text-xl font-semibold">Ventajas</legend>
        <ul className="mt-3 flex flex-col gap-3">
          {draft.features.map((feature, i) => (
            <li key={i} className="grid items-end gap-3 sm:grid-cols-[1fr_auto]">
              <Field label={`Ventaja ${i + 1}`}>
                {(props) => (
                  <Input
                    {...props}
                    maxLength={150}
                    value={feature}
                    onChange={(e) => set('features', draft.features.map((f, j) => (j === i ? e.target.value : f)))}
                  />
                )}
              </Field>
              <Button size="sm" variant="ghost" aria-label={`Quitar ventaja ${i + 1}`} onClick={() => set('features', draft.features.filter((_, j) => j !== i))}>
                Quitar
              </Button>
            </li>
          ))}
        </ul>
        <Button
          size="sm"
          variant="secondary"
          className="mt-3"
          disabled={draft.features.length >= MAX_ROWS}
          onClick={() => set('features', [...draft.features, ''])}
        >
          Agregar ventaja
        </Button>
      </fieldset>

      <div className="sticky -bottom-6 -mx-6 -mb-6 flex flex-wrap items-center gap-3 border-t border-border bg-background px-6 py-4 sm:-bottom-10 sm:-mx-10 sm:-mb-10 sm:px-10">
        <Button type="submit" disabled={save.saving}>
          {save.saving ? 'Guardando…' : 'Guardar producto'}
        </Button>
        <Button variant="secondary" onClick={onCancel} disabled={save.saving}>
          Descartar cambios
        </Button>
        <SaveFeedback state={save.state} />
      </div>
    </form>
  )
}
