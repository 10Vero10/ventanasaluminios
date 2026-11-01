import { type FormEvent, type ReactNode, useState } from 'react'

import { MoveButtons } from '@/components/admin/MoveButtons'
import { Panel } from '@/components/admin/Panel'
import { SaveFeedback } from '@/components/admin/SaveFeedback'
import { useSaveState } from '@/components/admin/useSaveState'
import { Button } from '@/components/ui/button'
import { Field, Input } from '@/components/ui/field'
import { LoadingState, MessageState } from '@/components/ui/states'
import {
  getAdminGlassOptions,
  type GlassKind,
  reorderGlass,
  saveGlassColor,
  saveGlassThickness,
  saveGlassType,
} from '@/services/api/adminCatalogService'
import { getErrorMessage } from '@/services/api/httpClient'
import { useLoad } from '@/lib/useLoad'
import type { GlassColor, GlassThickness, GlassType } from '@/types/catalog'

const CODE_PATTERN = /^[A-Z0-9_]{2,30}$/

/** Convierte "Laminado templado" en "LAMINADO_TEMPLADO" para proponer el código. */
function codeFrom(name: string): string {
  return name
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toUpperCase()
    .replace(/[^A-Z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .slice(0, 30)
}

/**
 * Opciones del configurador de vidrio. No se borran (hay cotizaciones que las usan): se desactivan. La opción por
 * defecto es la que aparece elegida al abrir el configurador.
 */
export default function GlassTab() {
  const [refresh, setRefresh] = useState(0)
  const options = useLoad(getAdminGlassOptions, [refresh])
  const reload = () => setRefresh((n) => n + 1)

  if (options.status === 'loading') return <LoadingState label="Cargando opciones de vidrio…" />
  if (options.status === 'error') {
    return <MessageState tone="error" title="No se pudieron cargar las opciones de vidrio" description={options.message} action={<Button onClick={options.reload}>Intentar de nuevo</Button>} />
  }
  return (
    <div className="flex flex-col gap-6">
      <GlassList
        kind="types"
        title="Tipos de vidrio"
        items={options.data.types}
        onChanged={reload}
        renderRow={(item, done) => <TypeRow item={item} onSaved={done} />}
        newForm={<NewNamed kind="types" onCreated={reload} />}
      />
      <GlassList
        kind="colors"
        title="Colores"
        items={options.data.colors}
        onChanged={reload}
        renderRow={(item, done) => <ColorRow item={item} onSaved={done} />}
        newForm={<NewNamed kind="colors" onCreated={reload} />}
      />
      <GlassList
        kind="thicknesses"
        title="Espesores"
        items={options.data.thicknesses}
        onChanged={reload}
        renderRow={(item, done) => <ThicknessRow item={item} onSaved={done} />}
        newForm={<NewThickness onCreated={reload} />}
      />
    </div>
  )
}

function GlassList<T extends { id: number }>({
  kind,
  title,
  items,
  onChanged,
  renderRow,
  newForm,
}: {
  kind: GlassKind
  title: string
  items: T[]
  onChanged: () => void
  renderRow: (item: T, onSaved: () => void) => ReactNode
  newForm: ReactNode
}) {
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function move(from: number, to: number) {
    const ids = items.map((i) => i.id)
    const [id] = ids.splice(from, 1)
    ids.splice(to, 0, id)
    setBusy(true)
    setError(null)
    try {
      await reorderGlass(kind, ids)
      onChanged()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Panel title={title} description="Desactiva una opción para ocultarla del configurador sin perder las cotizaciones que la usan.">
      {error && (
        <p role="alert" className="mb-4 text-sm text-destructive">
          {error}
        </p>
      )}
      <ul className="flex flex-col gap-3">
        {items.map((item, index) => (
          <li key={JSON.stringify(item)} className="flex flex-wrap items-end gap-3 rounded-[20px] border border-border p-4">
            <div className="min-w-0 flex-1">{renderRow(item, onChanged)}</div>
            <div className="flex gap-1">
              <MoveButtons label={`opción ${index + 1}`} index={index} count={items.length} onMove={move} disabled={busy} />
            </div>
          </li>
        ))}
      </ul>
      <div className="mt-6">{newForm}</div>
    </Panel>
  )
}

function Flags({
  active,
  defaultOption,
  onActive,
  onDefault,
}: {
  active: boolean
  defaultOption: boolean
  onActive: (value: boolean) => void
  onDefault: (value: boolean) => void
}) {
  return (
    <div className="flex flex-wrap gap-5 text-sm">
      <label className="flex items-center gap-2">
        <input type="checkbox" className="size-4 accent-[#0071e3]" checked={active} onChange={(e) => onActive(e.target.checked)} />
        Activa
      </label>
      <label className="flex items-center gap-2">
        <input type="checkbox" className="size-4 accent-[#0071e3]" checked={defaultOption} onChange={(e) => onDefault(e.target.checked)} />
        Por defecto
      </label>
    </div>
  )
}

function useRowSave(onSaved: () => void) {
  const save = useSaveState()
  async function run(action: () => Promise<unknown>) {
    const result = await save.run(action)
    if (result) onSaved()
  }
  return { save, run }
}

function TypeRow({ item, onSaved }: { item: GlassType; onSaved: () => void }) {
  const [draft, setDraft] = useState(item)
  const { save, run } = useRowSave(onSaved)
  const changed = JSON.stringify(draft) !== JSON.stringify(item)
  function submit(event: FormEvent) {
    event.preventDefault()
    void run(() => saveGlassType(item.id, { code: item.code, name: draft.name, active: draft.active, defaultOption: draft.defaultOption }))
  }
  return (
    <form onSubmit={submit} className="flex flex-wrap items-end gap-4">
      <Field label={`Nombre (${item.code})`} className="min-w-48 flex-1">
        {(props) => <Input {...props} maxLength={60} value={draft.name} onChange={(e) => setDraft({ ...draft, name: e.target.value })} />}
      </Field>
      <Flags
        active={draft.active}
        defaultOption={draft.defaultOption}
        onActive={(active) => setDraft({ ...draft, active })}
        onDefault={(defaultOption) => setDraft({ ...draft, defaultOption })}
      />
      <Button type="submit" size="sm" disabled={!changed || save.saving}>
        {save.saving ? 'Guardando…' : 'Guardar'}
      </Button>
      <SaveFeedback state={save.state} />
    </form>
  )
}

function ColorRow({ item, onSaved }: { item: GlassColor; onSaved: () => void }) {
  const [draft, setDraft] = useState(item)
  const { save, run } = useRowSave(onSaved)
  const changed = JSON.stringify(draft) !== JSON.stringify(item)
  function submit(event: FormEvent) {
    event.preventDefault()
    void run(() =>
      saveGlassColor(item.id, {
        code: item.code,
        name: draft.name,
        hex: draft.hex.toUpperCase(),
        reflective: draft.reflective,
        note: draft.note?.trim() || null,
        active: draft.active,
        defaultOption: draft.defaultOption,
      }),
    )
  }
  return (
    <form onSubmit={submit} className="flex flex-col gap-4">
      <div className="flex flex-wrap items-end gap-4">
        <Field label={`Nombre (${item.code})`} className="min-w-48 flex-1">
          {(props) => <Input {...props} maxLength={60} value={draft.name} onChange={(e) => setDraft({ ...draft, name: e.target.value })} />}
        </Field>
        <Field label="Color de referencia">
          {(props) => (
            <input
              {...props}
              type="color"
              value={draft.hex}
              onChange={(e) => setDraft({ ...draft, hex: e.target.value.toUpperCase() })}
              className="h-12 w-20 cursor-pointer rounded-[12px] border border-input bg-background p-1"
            />
          )}
        </Field>
        <label className="flex items-center gap-2 pb-3 text-sm">
          <input type="checkbox" className="size-4 accent-[#0071e3]" checked={draft.reflective} onChange={(e) => setDraft({ ...draft, reflective: e.target.checked })} />
          Reflectivo
        </label>
      </div>
      <Field label="Nota" hint="Opcional. Se muestra al elegir este color.">
        {(props) => <Input {...props} maxLength={200} value={draft.note ?? ''} onChange={(e) => setDraft({ ...draft, note: e.target.value })} />}
      </Field>
      <div className="flex flex-wrap items-center gap-4">
        <Flags
          active={draft.active}
          defaultOption={draft.defaultOption}
          onActive={(active) => setDraft({ ...draft, active })}
          onDefault={(defaultOption) => setDraft({ ...draft, defaultOption })}
        />
        <Button type="submit" size="sm" disabled={!changed || save.saving}>
          {save.saving ? 'Guardando…' : 'Guardar'}
        </Button>
        <SaveFeedback state={save.state} />
      </div>
    </form>
  )
}

function ThicknessRow({ item, onSaved }: { item: GlassThickness; onSaved: () => void }) {
  const [draft, setDraft] = useState(item)
  const { save, run } = useRowSave(onSaved)
  const changed = JSON.stringify(draft) !== JSON.stringify(item)
  function submit(event: FormEvent) {
    event.preventDefault()
    void run(() => saveGlassThickness(item.id, { mm: item.mm, active: draft.active, defaultOption: draft.defaultOption }))
  }
  return (
    <form onSubmit={submit} className="flex flex-wrap items-center gap-5">
      <p className="w-20 font-display text-xl font-semibold tabular-nums">{item.mm} mm</p>
      <Flags
        active={draft.active}
        defaultOption={draft.defaultOption}
        onActive={(active) => setDraft({ ...draft, active })}
        onDefault={(defaultOption) => setDraft({ ...draft, defaultOption })}
      />
      <Button type="submit" size="sm" disabled={!changed || save.saving}>
        {save.saving ? 'Guardando…' : 'Guardar'}
      </Button>
      <SaveFeedback state={save.state} />
    </form>
  )
}

function NewNamed({ kind, onCreated }: { kind: 'types' | 'colors'; onCreated: () => void }) {
  const [name, setName] = useState('')
  const [code, setCode] = useState<string | null>(null)
  const [hex, setHex] = useState('#E0F2FE')
  const [error, setError] = useState<string | null>(null)
  const { save, run } = useRowSave(() => {
    setName('')
    setCode(null)
    onCreated()
  })
  const effectiveCode = code ?? codeFrom(name)

  function submit(event: FormEvent) {
    event.preventDefault()
    if (!name.trim()) {
      setError('Escribe el nombre.')
      return
    }
    if (!CODE_PATTERN.test(effectiveCode)) {
      setError('El código debe tener de 2 a 30 caracteres: letras mayúsculas sin tilde, números o guion bajo.')
      return
    }
    setError(null)
    const base = { code: effectiveCode, name: name.trim(), active: true, defaultOption: false }
    void run(() => (kind === 'types' ? saveGlassType(null, base) : saveGlassColor(null, { ...base, hex, reflective: false, note: null })))
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-4 rounded-[20px] bg-secondary p-5">
      <p className="font-semibold">{kind === 'types' ? 'Nuevo tipo' : 'Nuevo color'}</p>
      <div className="flex flex-wrap items-end gap-4">
        <Field label="Nombre" className="min-w-48 flex-1">
          {(props) => <Input {...props} maxLength={60} value={name} onChange={(e) => setName(e.target.value)} />}
        </Field>
        <Field label="Código" hint="No se puede cambiar después." className="min-w-48 flex-1">
          {(props) => <Input {...props} maxLength={30} value={effectiveCode} onChange={(e) => setCode(e.target.value.toUpperCase())} />}
        </Field>
        {kind === 'colors' && (
          <Field label="Color de referencia">
            {(props) => (
              <input {...props} type="color" value={hex} onChange={(e) => setHex(e.target.value.toUpperCase())} className="h-12 w-20 cursor-pointer rounded-[12px] border border-input bg-background p-1" />
            )}
          </Field>
        )}
      </div>
      {error && (
        <p role="alert" className="text-sm text-destructive">
          {error}
        </p>
      )}
      <div className="flex flex-wrap items-center gap-4">
        <Button type="submit" size="sm" disabled={save.saving}>
          {save.saving ? 'Agregando…' : 'Agregar'}
        </Button>
        <SaveFeedback state={save.state} />
      </div>
    </form>
  )
}

function NewThickness({ onCreated }: { onCreated: () => void }) {
  const [mm, setMm] = useState('')
  const [error, setError] = useState<string | null>(null)
  const { save, run } = useRowSave(() => {
    setMm('')
    onCreated()
  })
  function submit(event: FormEvent) {
    event.preventDefault()
    const value = Number(mm)
    if (!Number.isInteger(value) || value < 1 || value > 50) {
      setError('El espesor debe ser un número entero entre 1 y 50 mm.')
      return
    }
    setError(null)
    void run(() => saveGlassThickness(null, { mm: value, active: true, defaultOption: false }))
  }
  return (
    <form onSubmit={submit} className="flex flex-wrap items-end gap-4 rounded-[20px] bg-secondary p-5">
      <Field label="Nuevo espesor (mm)">
        {(props) => <Input {...props} type="number" inputMode="numeric" min={1} max={50} className="w-32" value={mm} onChange={(e) => setMm(e.target.value)} />}
      </Field>
      <Button type="submit" size="sm" disabled={save.saving}>
        {save.saving ? 'Agregando…' : 'Agregar'}
      </Button>
      {error && (
        <p role="alert" className="text-sm text-destructive">
          {error}
        </p>
      )}
      <SaveFeedback state={save.state} />
    </form>
  )
}
