import { type FormEvent, type ReactNode, useState } from 'react'

import { MoveButtons } from '@/components/admin/MoveButtons'
import { moveItem } from '@/components/admin/moveItem'
import { Panel } from '@/components/admin/Panel'
import { SaveButton } from '@/components/admin/SaveButton'
import { useSaveState } from '@/components/admin/useSaveState'
import { Checkbox } from '@/components/ui/checkbox'
import { Field, TextField } from '@/components/ui/field'
import { Loadable } from '@/components/ui/Loadable'
import { InlineError } from '@/components/ui/states'
import { GLASS_CODE_PATTERN, glassCodeFrom } from '@/lib/glassCode'
import { useLoad } from '@/lib/useLoad'
import {
  getAdminGlassOptions,
  type GlassKind,
  type GlassKinds,
  reorderGlass,
  saveGlassOption,
} from '@/services/api/adminCatalogService'
import { getErrorMessage } from '@/services/api/httpClient'
import type { GlassColor, GlassOptions, GlassThickness, GlassType } from '@/types/catalog'

type Item<K extends GlassKind> = GlassKinds[K]['item']
type Request<K extends GlassKind> = GlassKinds[K]['request']

/**
 * Lo que distingue a cada lista: cómo se arma la solicitud (también sirve para saber si una fila cambió, comparando
 * solo lo editable), cómo se titula una fila y qué campos propios tiene.
 */
interface KindConfig<K extends GlassKind> {
  kind: K
  title: string
  toRequest: (item: Item<K>) => Request<K>
  rowLabel: (item: Item<K>) => string
  fields: (draft: Item<K>, set: (patch: Partial<Item<K>>) => void) => ReactNode
  newForm: (onCreated: () => void) => ReactNode
}

const TYPES: KindConfig<'types'> = {
  kind: 'types',
  title: 'Tipos de vidrio',
  toRequest: (t: GlassType) => ({ code: t.code, name: t.name.trim(), active: t.active, defaultOption: t.defaultOption }),
  rowLabel: (t) => t.name,
  fields: (draft, set) => <NameField code={draft.code} value={draft.name} onChange={(name) => set({ name })} />,
  newForm: (onCreated) => <NewNamedOption kind="types" onCreated={onCreated} />,
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
  fields: (draft, set) => (
    <>
      <NameField code={draft.code} value={draft.name} onChange={(name) => set({ name })} />
      <ColorField value={draft.hex} onChange={(hex) => set({ hex })} />
      <Checkbox className="pb-3 text-sm" checked={draft.reflective} onChange={(reflective) => set({ reflective })}>
        Reflectivo
      </Checkbox>
      <TextField
        label="Nota"
        hint="Opcional. Se muestra al elegir este color."
        className="basis-full"
        maxLength={200}
        value={draft.note ?? ''}
        onChange={(note) => set({ note })}
      />
    </>
  ),
  newForm: (onCreated) => <NewNamedOption kind="colors" onCreated={onCreated} />,
}

const THICKNESSES: KindConfig<'thicknesses'> = {
  kind: 'thicknesses',
  title: 'Espesores',
  toRequest: (t: GlassThickness) => ({ mm: t.mm, active: t.active, defaultOption: t.defaultOption }),
  rowLabel: (t) => `${t.mm} mm`,
  fields: (draft) => <p className="w-20 pb-3 font-display text-xl font-semibold tabular-nums">{draft.mm} mm</p>,
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
  options: { reload: () => void; setData: (update: (data: GlassOptions) => GlassOptions) => void }
}) {
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const setItems = (next: Item<K>[]) => options.setData((data) => ({ ...data, [config.kind]: next }))

  async function move(from: number, to: number) {
    const next = moveItem(items, from, to)
    setItems(next)
    setBusy(true)
    setError(null)
    try {
      await reorderGlass(config.kind, next.map((i) => i.id))
    } catch (err) {
      setItems(items)
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Panel title={config.title} description="Desactiva una opción para ocultarla del configurador sin perder las cotizaciones que la usan.">
      <InlineError message={error} className="mb-4" />
      <ul className="flex flex-col gap-3">
        {items.map((item, index) => (
          <li key={item.id} className="flex flex-wrap items-end gap-3 rounded-[20px] border border-border p-4">
            <GlassRow config={config} item={item} onSaved={options.reload} />
            <div className="flex gap-1">
              <MoveButtons label={config.rowLabel(item)} index={index} count={items.length} onMove={move} disabled={busy} />
            </div>
          </li>
        ))}
      </ul>
      <div className="mt-6">{config.newForm(options.reload)}</div>
    </Panel>
  )
}

function GlassRow<K extends GlassKind>({ config, item, onSaved }: { config: KindConfig<K>; item: Item<K>; onSaved: () => void }) {
  const [draft, setDraft] = useState(item)
  const save = useSaveState()
  const set = (patch: Partial<Item<K>>) => setDraft((d) => ({ ...d, ...patch }))
  const changed = JSON.stringify(config.toRequest(draft)) !== JSON.stringify(config.toRequest(item))

  async function submit(event: FormEvent) {
    event.preventDefault()
    // Al guardar una opción por defecto, el servidor desmarca la anterior: se recarga la lista entera.
    if (await save.run(() => saveGlassOption(config.kind, item.id, config.toRequest(draft)))) onSaved()
  }

  return (
    <form noValidate onSubmit={submit} className="flex min-w-0 flex-1 flex-wrap items-end gap-4">
      {config.fields(draft, set)}
      <div className="flex flex-wrap gap-5 pb-3 text-sm">
        <Checkbox checked={draft.active} onChange={(active) => set({ active } as Partial<Item<K>>)}>
          Activa
        </Checkbox>
        <Checkbox checked={draft.defaultOption} onChange={(defaultOption) => set({ defaultOption } as Partial<Item<K>>)}>
          Por defecto
        </Checkbox>
      </div>
      <SaveButton save={save} label="Guardar" size="sm" disabled={!changed} />
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

function NewNamedOption({ kind, onCreated }: { kind: 'types' | 'colors'; onCreated: () => void }) {
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
    const base = { code: effectiveCode, name: name.trim(), active: true, defaultOption: false }
    void run(() =>
      kind === 'types'
        ? saveGlassOption('types', null, base)
        : saveGlassOption('colors', null, { ...base, hex, reflective: false, note: null }),
    )
  }

  return (
    <form noValidate onSubmit={submit} className="flex flex-col gap-4 rounded-[20px] bg-secondary p-5">
      <p className="font-semibold">{kind === 'types' ? 'Nuevo tipo' : 'Nuevo color'}</p>
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
        {kind === 'colors' && <ColorField value={hex} onChange={setHex} />}
      </div>
      <InlineError message={error} />
      <SaveButton save={save} label="Agregar" busyLabel="Agregando…" size="sm" />
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
      <SaveButton save={save} label="Agregar" busyLabel="Agregando…" size="sm" />
    </form>
  )
}
