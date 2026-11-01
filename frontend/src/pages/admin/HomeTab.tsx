import { type FormEvent, useState } from 'react'

import { useAdminOutlet } from '@/components/admin/adminOutlet'
import { CategorySelect } from '@/components/admin/CategorySelect'
import { ConfirmDialog } from '@/components/admin/ConfirmDialog'
import { ImagePicker } from '@/components/admin/ImagePicker'
import { MoveButtons } from '@/components/admin/MoveButtons'
import { Panel } from '@/components/admin/Panel'
import { SaveFeedback } from '@/components/admin/SaveFeedback'
import { useOrderedList } from '@/components/admin/useOrderedList'
import { useSaveState } from '@/components/admin/useSaveState'
import { useSettingsDraft } from '@/components/admin/useSettingsDraft'
import { Button } from '@/components/ui/button'
import { Field, Input, Textarea } from '@/components/ui/field'
import { LoadingState, MessageState } from '@/components/ui/states'
import {
  createGalleryImage,
  deleteGalleryImage,
  getAdminGallery,
  getFeaturedLines,
  reorderFeaturedLines,
  reorderGallery,
  updateFeaturedLine,
  updateGalleryImage,
} from '@/services/api/adminSiteService'
import { categoryLabel, type ProductCategory } from '@/types/catalog'
import type { FeaturedLine, GalleryImage } from '@/types/site'

/** Pestaña Inicio (briefing §5.7.3): portada, galería y líneas destacadas. */
export default function HomeTab() {
  return (
    <div className="flex flex-col gap-6">
      <HeroPanel />
      <GalleryPanel />
      <FeaturedLinesPanel />
    </div>
  )
}

function HeroPanel() {
  const { loaded, draft, set, submit, save } = useSettingsDraft()

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    void submit()
  }

  return (
    <Panel title="Portada" description="Textos e imagen que ven las personas al entrar al sitio.">
      {loaded.status === 'loading' && <LoadingState label="Cargando configuración…" />}
      {loaded.status === 'error' && (
        <MessageState tone="error" title="No se pudo cargar la configuración" description={loaded.message} action={<Button onClick={loaded.reload}>Intentar de nuevo</Button>} />
      )}
      {draft && (
        <form onSubmit={handleSubmit} className="grid gap-6 lg:grid-cols-2">
          <div className="flex flex-col gap-5">
            <Field label="Título de portada" required>
              {(props) => <Input {...props} maxLength={120} value={draft.heroTitle} onChange={(e) => set('heroTitle', e.target.value)} />}
            </Field>
            <Field label="Subtítulo">
              {(props) => <Textarea {...props} maxLength={300} value={draft.heroSubtitle} onChange={(e) => set('heroSubtitle', e.target.value)} />}
            </Field>
            <Field label="Etiqueta sobre la imagen" hint="Opcional. Ejemplo: Vidrio templado y aluminio.">
              {(props) => <Input {...props} maxLength={80} value={draft.heroBadge ?? ''} onChange={(e) => set('heroBadge', e.target.value || null)} />}
            </Field>
            <div className="flex flex-wrap items-center gap-4">
              <Button type="submit" disabled={save.saving}>
                {save.saving ? 'Guardando…' : 'Guardar cambios'}
              </Button>
              <SaveFeedback state={save.state} />
            </div>
          </div>
          <ImagePicker label="Imagen principal (recomendado 16:9)" value={draft.heroImageUrl} onChange={(url) => set('heroImageUrl', url)} />
        </form>
      )}
    </Panel>
  )
}

function GalleryPanel() {
  const { refreshDashboard } = useAdminOutlet()
  const list = useOrderedList(getAdminGallery, reorderGallery)
  const [toDelete, setToDelete] = useState<GalleryImage | null>(null)

  return (
    <Panel title="Galería de trabajos" description="Imágenes de la página Trabajos. También se pueden usar en cualquier selector de imagen.">
      <NewGalleryImage
        onCreated={() => {
          list.reload()
          refreshDashboard()
        }}
      />
      <div className="mt-8">
        {list.loaded.status === 'loading' && <LoadingState label="Cargando galería…" />}
        {list.loaded.status === 'error' && (
          <MessageState tone="error" title="No se pudo cargar la galería" description={list.loaded.message} action={<Button onClick={list.reload}>Intentar de nuevo</Button>} />
        )}
        {list.items?.length === 0 && <MessageState title="La galería está vacía." />}
        {list.error && (
          <p role="alert" className="mb-4 text-sm text-destructive">
            {list.error}
          </p>
        )}
        {list.items && list.items.length > 0 && (
          <ul className="flex flex-col gap-3">
            {list.items.map((image, index) => (
              <li key={`${image.id}-${image.title}-${image.category}`}>
                <GalleryRow
                  image={image}
                  index={index}
                  count={list.items!.length}
                  onMove={list.move}
                  moving={list.busy}
                  onSaved={list.replace}
                  onDelete={() => setToDelete(image)}
                />
              </li>
            ))}
          </ul>
        )}
      </div>
      <ConfirmDialog
        open={toDelete !== null}
        onOpenChange={(open) => !open && setToDelete(null)}
        title="¿Eliminar esta imagen de la galería?"
        description={toDelete?.title}
        confirmLabel="Eliminar imagen"
        onConfirm={async () => {
          if (!toDelete) return
          await deleteGalleryImage(toDelete.id)
          list.reload()
          refreshDashboard()
        }}
      />
    </Panel>
  )
}

function NewGalleryImage({ onCreated }: { onCreated: () => void }) {
  const [title, setTitle] = useState('')
  const [url, setUrl] = useState<string | null>(null)
  const [category, setCategory] = useState<ProductCategory | null>(null)
  const [error, setError] = useState<string | null>(null)
  const save = useSaveState()

  async function add(event: FormEvent) {
    event.preventDefault()
    if (!url) {
      setError('Pega la URL o sube un archivo antes de agregar.')
      return
    }
    setError(null)
    const created = await save.run(() => createGalleryImage({ title: title.trim(), url, category }))
    if (created) {
      setTitle('')
      setUrl(null)
      setCategory(null)
      onCreated()
    }
  }

  return (
    <form onSubmit={add} className="grid gap-5 rounded-[20px] bg-secondary p-5 lg:grid-cols-[minmax(0,360px)_1fr]">
      <ImagePicker label="Nueva imagen" value={url} onChange={setUrl} aspect="4/3" />
      <div className="flex flex-col gap-5">
        <Field label="Título" hint='Sin título queda "Imagen". Ejemplo: Fachada comercial.'>
          {(props) => <Input {...props} maxLength={120} value={title} onChange={(e) => setTitle(e.target.value)} />}
        </Field>
        <Field label="Línea">{(props) => <CategorySelect {...props} allowNone value={category} onChange={setCategory} />}</Field>
        {error && (
          <p role="alert" className="text-sm text-destructive">
            {error}
          </p>
        )}
        <div className="flex flex-wrap items-center gap-4">
          <Button type="submit" disabled={save.saving}>
            {save.saving ? 'Agregando…' : 'Agregar a la galería'}
          </Button>
          <SaveFeedback state={save.state} />
        </div>
      </div>
    </form>
  )
}

function GalleryRow({
  image,
  index,
  count,
  onMove,
  moving,
  onSaved,
  onDelete,
}: {
  image: GalleryImage
  index: number
  count: number
  onMove: (from: number, to: number) => void
  moving: boolean
  onSaved: (image: GalleryImage) => void
  onDelete: () => void
}) {
  const [title, setTitle] = useState(image.title)
  const [category, setCategory] = useState(image.category)
  const save = useSaveState()
  const changed = title !== image.title || category !== image.category

  async function submit(event: FormEvent) {
    event.preventDefault()
    const saved = await save.run(() => updateGalleryImage(image.id, { title: title.trim(), url: image.url, category }))
    if (saved) onSaved(saved)
  }

  return (
    <form onSubmit={submit} className="grid items-center gap-4 rounded-[20px] border border-border p-4 sm:grid-cols-[120px_1fr]">
      <img src={image.url} alt={image.title} className="aspect-[4/3] w-full rounded-[12px] bg-secondary object-cover" loading="lazy" />
      <div className="flex flex-col gap-3">
        <div className="grid gap-3 sm:grid-cols-2">
          <Field label="Título">{(props) => <Input {...props} maxLength={120} value={title} onChange={(e) => setTitle(e.target.value)} />}</Field>
          <Field label="Línea">{(props) => <CategorySelect {...props} allowNone value={category} onChange={setCategory} />}</Field>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <Button type="submit" size="sm" disabled={!changed || save.saving}>
            {save.saving ? 'Guardando…' : 'Guardar'}
          </Button>
          <MoveButtons label={image.title} index={index} count={count} onMove={onMove} disabled={moving} />
          <Button size="sm" variant="destructive" onClick={onDelete}>
            Eliminar
          </Button>
          <SaveFeedback state={save.state} />
        </div>
      </div>
    </form>
  )
}

function FeaturedLinesPanel() {
  const list = useOrderedList(getFeaturedLines, reorderFeaturedLines)
  const [editing, setEditing] = useState<number | null>(null)

  return (
    <Panel title="Líneas destacadas" description="Tarjetas de la portada. Cambia sus textos, su imagen y su orden.">
      {list.loaded.status === 'loading' && <LoadingState label="Cargando líneas…" />}
      {list.loaded.status === 'error' && (
        <MessageState tone="error" title="No se pudieron cargar las líneas" description={list.loaded.message} action={<Button onClick={list.reload}>Intentar de nuevo</Button>} />
      )}
      {list.error && (
        <p role="alert" className="mb-4 text-sm text-destructive">
          {list.error}
        </p>
      )}
      {list.items && (
        <ul className="flex flex-col gap-3">
          {list.items.map((line, index) => (
            <li key={line.id} className="rounded-[20px] border border-border p-4">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="text-xs text-muted-foreground">
                    {line.number} · {categoryLabel(line.targetCategory)}
                  </p>
                  <p className="text-[17px] font-semibold">{line.title}</p>
                </div>
                <div className="flex flex-wrap gap-2">
                  <Button
                    size="sm"
                    variant="secondary"
                    aria-expanded={editing === line.id}
                    onClick={() => setEditing(editing === line.id ? null : line.id)}
                  >
                    {editing === line.id ? 'Cerrar' : 'Editar'}
                  </Button>
                  <MoveButtons label={line.title} index={index} count={list.items!.length} onMove={list.move} disabled={list.busy} />
                </div>
              </div>
              {editing === line.id && (
                <FeaturedLineEditor
                  line={line}
                  onSaved={(saved) => {
                    list.replace(saved)
                    setEditing(null)
                  }}
                />
              )}
            </li>
          ))}
        </ul>
      )}
    </Panel>
  )
}

function FeaturedLineEditor({ line, onSaved }: { line: FeaturedLine; onSaved: (line: FeaturedLine) => void }) {
  const [draft, setDraft] = useState(line)
  const save = useSaveState()
  const set = <K extends keyof FeaturedLine>(key: K, value: FeaturedLine[K]) => setDraft((d) => ({ ...d, [key]: value }))

  async function submit(event: FormEvent) {
    event.preventDefault()
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { id, position, ...request } = draft
    const saved = await save.run(() => updateFeaturedLine(line.id, request))
    if (saved) onSaved(saved)
  }

  return (
    <form onSubmit={submit} className="mt-5 grid gap-5 lg:grid-cols-2">
      <div className="flex flex-col gap-4">
        <div className="grid gap-4 sm:grid-cols-[100px_1fr]">
          <Field label="Número">{(props) => <Input {...props} maxLength={4} value={draft.number} onChange={(e) => set('number', e.target.value)} />}</Field>
          <Field label="Título">{(props) => <Input {...props} maxLength={80} value={draft.title} onChange={(e) => set('title', e.target.value)} />}</Field>
        </div>
        <Field label="Etiqueta de categoría">
          {(props) => <Input {...props} maxLength={80} value={draft.categoryTag} onChange={(e) => set('categoryTag', e.target.value)} />}
        </Field>
        <Field label="Descripción">
          {(props) => <Textarea {...props} maxLength={400} value={draft.description} onChange={(e) => set('description', e.target.value)} />}
        </Field>
        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Texto del enlace">
            {(props) => <Input {...props} maxLength={40} value={draft.linkText} onChange={(e) => set('linkText', e.target.value)} />}
          </Field>
          <Field label="Categoría destino">
            {(props) => <CategorySelect {...props} value={draft.targetCategory} onChange={(v) => v && set('targetCategory', v)} />}
          </Field>
        </div>
        <div className="flex flex-wrap items-center gap-4">
          <Button type="submit" disabled={save.saving}>
            {save.saving ? 'Guardando…' : 'Guardar línea'}
          </Button>
          <SaveFeedback state={save.state} />
        </div>
      </div>
      <ImagePicker label="Imagen de la tarjeta" value={draft.imageUrl} onChange={(url) => set('imageUrl', url)} aspect="16/10" />
    </form>
  )
}
