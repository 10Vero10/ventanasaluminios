import { type FormEvent, type ReactNode, useState } from 'react'

import { MoveButtons } from '@/components/admin/MoveButtons'
import { Panel } from '@/components/admin/Panel'
import { SaveRow } from '@/components/admin/SaveRow'
import { type DraftState, useDraft } from '@/components/admin/useDraft'
import { useReorder } from '@/components/admin/useReorder'
import { useSaveState } from '@/components/admin/useSaveState'
import { Checkbox } from '@/components/ui/checkbox'
import { Field, TextField } from '@/components/ui/field'
import { Loadable } from '@/components/ui/Loadable'
import { InlineError } from '@/components/ui/states'
import { GLASS_CODE_PATTERN, glassCodeFrom } from '@/lib/glassCode'
import { type Loaded, useLoad } from '@/lib/useLoad'
import { getAdminGlassOptions, reorderGlass, saveGlassOption } from '@/services/api/adminCatalogService'
import type { GlassKind, GlassKinds } from '@/types/admin'
import type { GlassColor, GlassOptions, GlassThickness, GlassType } from '@/types/catalog'

type Item<K extends GlassKind> = GlassOptions[K][number]
type Request<K extends GlassKind> = GlassKinds[K]['request']
type Draft<K extends GlassKind> = DraftState<Item<K>>

/**
 * Lo que distingue a cada lista: cómo se arma la solicitud (también sirve para saber si una fila cambió, comparando
 * solo lo editable), cómo se nombra una fila, sus campos propios y su formulario para agregar.
 */
interface KindConfig<K extends GlassKind> {
  kind: K
  title: string
  toRequest: (item: Item<K>) => Request<K>
  rowLabel: (item: Item<K>) => string
  fields: (draft: Draft<K>) => ReactNode
  newForm: (onCreated: () => void) => ReactNode
}

const TYPES: KindConfig<'types'> = {
  kind: 'types',
  title: 'Tipos de vidrio',
  toRequest: (t: GlassType) => ({ code: t.code, name: t.name.trim(), active: t.active, defaultOption: t.defaultOption }),
  rowLabel: (t) => t.name,
  fields: ({ draft, bind }) => <NameField code={draft.code} {...bind('name')} />,
  newForm: (onCreated) => (
    <NewNamedOption
      title="Nuevo tipo"
      onCreated={onCreated}
      create={(base) => saveGlassOption('types', null, { ...base, active: true, defaultOption: false })}
    />
  ),
}

const COLORS: KindConfig<'colors'> = {
  kind: 'colors',
  title: 'Colores',
  toRequest: (c: GlassColor) => ({
    code: c.code,
    name: c.name.trim(),
    hex: c.hex.toUpperCase(),
    reflective: c.reflective,
    note: c.note?.trim() || null,
    active: c.active,
    defaultOption: c.defaultOption,
  }),
  rowLabel: (c) => c.name,
  fields: ({ draft, set, bind, bindOptional }) => (
    <>
      <NameField code={draft.code} {...bind('name')} />
      <ColorField value={draft.hex} onChange={(hex) => set('hex', hex)} />
      <Checkbox className="pb-3 text-sm" checked={draft.reflective} onChange={(reflective) => set('reflective', reflective)}>
        Reflectivo
      </Checkbox>
      <TextField
        label="Nota"
        hint="Opcional. Se muestra al elegir este color."
        className="basis-full"
        maxLength={200}
        {...bindOptional('note')}
      />
    </>
  ),
  newForm: (onCreated) => (
    <NewNamedOption
      title="Nuevo color"
      withColor
      onCreated={onCreated}
      create={(base, hex) =>
        saveGlassOption('colors', null, { ...base, hex, reflective: false, note: null, active: true, defaultOption: false })
      }
    />
  ),
}

const THICKNESSES: KindConfig<'thicknesses'> = {
  kind: 'thicknesses',
  title: 'Espesores',
  toRequest: (t: GlassThickness) => ({ mm: t.mm, active: t.active, defaultOption: t.defaultOption }),
  rowLabel: (t) => `${t.mm} mm`,
  fields: ({ draft }) => <p className="w-20 pb-3 font-display text-xl font-semibold tabular-nums">{draft.mm} mm</p>,
  newForm: (onCreated) => <NewThickness onCreated={onCreated} />,
}

/**
 * Opciones del configurador de vidrio. No se borran (hay cotizaciones que las usan): se desactivan. La opción por
 * defecto es la que aparece elegida al abrir el configurador.
 */
export default function GlassTab() {
  const options = useLoad(getAdminGlassOptions, [])
  return (
    <Loadable state={options} loadingLabel="Cargando opciones de vidrio…" errorTitle="No se pudieron cargar las opciones de vidrio">
      {(data) => (
        <div className="flex flex-col gap-6">
          <GlassList config={TYPES} items={data.types} options={options} />
          <GlassList config={COLORS} items={data.colors} options={options} />
          <GlassList config={THICKNESSES} items={data.thicknesses} options={options} />
        </div>
      )}
    </Loadable>
  )
}

function GlassList<K extends GlassKind>({
  config,
  items,
  options,
}: {
  config: KindConfig<K>
  items: Item<K>[]
  options: Loaded<GlassOptions>
}) {
  const setItems = (update: (current: Item<K>[]) => Item<K>[]) =>
    options.setData((data) => ({ ...data, [config.kind]: update(data[config.kind]) }))
  // Mientras se recarga no se ordena sobre datos viejos.
  const refreshing = options.status === 'ready' && options.refreshing
  const reorder = useReorder(items, setItems, (ids) => reorderGlass(config.kind, ids), refreshing)

  return (
    <Panel title={config.title} description="Desactiva una opción para ocultarla del configurador sin perder las cotizaciones que la usan.">
      <InlineError message={reorder.error} className="mb-4" />
      <ul className="flex flex-col gap-3">
        {items.map((item, index) => (
          <li key={item.id} className="flex flex-wrap items-end gap-3 rounded-[20px] border border-border p-4">
            <GlassRow config={config} item={item} onSaved={options.reload} />
            <div className="flex gap-1">
              <MoveButtons label={config.rowLabel(item)} index={index} count={items.length} onMove={reorder.move} disabled={reorder.locked} />
            </div>
          </li>
        ))}
      </ul>
      <div className="mt-6">{config.newForm(options.reload)}</div>
    </Panel>
  )
}

function GlassRow<K extends GlassKind>({ config, item, onSaved }: { config: KindConfig<K>; item: Item<K>; onSaved: () => void }) {
  // El borrador sigue al servidor en lo que no se tocó: si otra fila pasa a ser la opción por defecto, esta se desmarca.
  const draftState = useDraft<Item<K>>(item)
  const { draft } = draftState
  const save = useSaveState()
  const changed = JSON.stringify(config.toRequest(draft)) !== JSON.stringify(config.toRequest(item))
  const setFlag = (key: 'active' | 'defaultOption', value: boolean) => draftState.set(key, value as Item<K>[typeof key])

  async function submit(event: FormEvent) {
    event.preventDefault()
    // Al guardar una opción por defecto, el servidor desmarca la anterior: se recarga la lista entera.
    if (await save.run(() => saveGlassOption(config.kind, item.id, config.toRequest(draft)))) onSaved()
  }

  return (
    <form noValidate onSubmit={submit} className="flex min-w-0 flex-1 flex-wrap items-end gap-4">
      {config.fields(draftState)}
      <div className="flex flex-wrap gap-5 pb-3 text-sm">
        <Checkbox checked={draft.active} onChange={(value) => setFlag('active', value)}>
          Activa
        </Checkbox>
        <Checkbox checked={draft.defaultOption} onChange={(value) => setFlag('defaultOption', value)}>
          Por defecto
        </Checkbox>
      </div>
      <SaveRow save={save} label="Guardar" size="sm" disabled={!changed} />
    </form>
  )
}

function NameField({ code, value, onChange }: { code: string; value: string; onChange: (value: string) => void }) {
  return <TextField label={`Nombre (${code})`} className="min-w-48 flex-1" maxLength={60} value={value} onChange={onChange} />
}

function ColorField({ value, onChange }: { value: string; onChange: (hex: string) => void }) {
  return (
    <Field label="Color de referencia">
      {(props) => (
        <input
          {...props}
          type="color"
          value={value}
          onChange={(e) => onChange(e.target.value.toUpperCase())}
          className="h-12 w-20 cursor-pointer rounded-[12px] border border-input bg-background p-1"
        />
      )}
    </Field>
  )
}

function useCreate(onCreated: () => void, reset: () => void) {
  const save = useSaveState()
  async function run(action: () => Promise<unknown>) {
    if (await save.run(action)) {
      reset()
      onCreated()
    }
  }
  return { save, run }
}

/** Formulario para agregar un tipo o un color: nombre, código propuesto desde el nombre y, en colores, el color. */
function NewNamedOption({
  title,
  withColor = false,
  create,
  onCreated,
}: {
  title: string
  withColor?: boolean
  create: (base: { code: string; name: string }, hex: string) => Promise<unknown>
  onCreated: () => void
}) {
  const [name, setName] = useState('')
  const [code, setCode] = useState<string | null>(null)
  const [hex, setHex] = useState('#E0F2FE')
  const [error, setError] = useState<string | null>(null)
  const { save, run } = useCreate(onCreated, () => {
    setName('')
    setCode(null)
  })
  const effectiveCode = code ?? glassCodeFrom(name)

  function submit(event: FormEvent) {
    event.preventDefault()
    if (!name.trim()) return setError('Escribe el nombre.')
    if (!GLASS_CODE_PATTERN.test(effectiveCode)) {
      return setError('El código debe tener de 2 a 30 caracteres: letras mayúsculas sin tilde, números o guion bajo.')
    }
    setError(null)
    void run(() => create({ code: effectiveCode, name: name.trim() }, hex))
  }

  return (
    <form noValidate onSubmit={submit} className="flex flex-col gap-4 rounded-[20px] bg-secondary p-5">
      <p className="font-semibold">{title}</p>
      <div className="flex flex-wrap items-end gap-4">
        <TextField label="Nombre" className="min-w-48 flex-1" maxLength={60} value={name} onChange={setName} />
        <TextField
          label="Código"
          hint="No se puede cambiar después."
          className="min-w-48 flex-1"
          maxLength={30}
          value={effectiveCode}
          onChange={(value) => setCode(value.toUpperCase())}
        />
        {withColor && <ColorField value={hex} onChange={setHex} />}
      </div>
      <InlineError message={error} />
      <SaveRow save={save} label="Agregar" busyLabel="Agregando…" size="sm" />
    </form>
  )
}

function NewThickness({ onCreated }: { onCreated: () => void }) {
  const [mm, setMm] = useState('')
  const [error, setError] = useState<string | null>(null)
  const { save, run } = useCreate(onCreated, () => setMm(''))

  function submit(event: FormEvent) {
    event.preventDefault()
    const value = Number(mm)
    if (!Number.isInteger(value) || value < 1 || value > 50) return setError('El espesor debe ser un número entero entre 1 y 50 mm.')
    setError(null)
    void run(() => saveGlassOption('thicknesses', null, { mm: value, active: true, defaultOption: false }))
  }

  return (
    <form noValidate onSubmit={submit} className="flex flex-col gap-4 rounded-[20px] bg-secondary p-5">
      <TextField
        label="Nuevo espesor (mm)"
        type="number"
        inputMode="numeric"
        min={1}
        max={50}
        inputClassName="w-32"
        value={mm}
        onChange={setMm}
      />
      <InlineError message={error} />
      <SaveRow save={save} label="Agregar" busyLabel="Agregando…" size="sm" />
    </form>
  )
}
