import { type FormEvent, type ReactNode, useState } from 'react'

import { useAdminOutlet } from '@/components/admin/adminOutlet'
import { CategorySelect } from '@/components/admin/CategorySelect'
import { ConfirmDialog } from '@/components/admin/ConfirmDialog'
import { ImagePicker } from '@/components/admin/ImagePicker'
import { MoveButtons } from '@/components/admin/MoveButtons'
import { Panel } from '@/components/admin/Panel'
import { SaveButton } from '@/components/admin/SaveButton'
import { useOrderedList } from '@/components/admin/useOrderedList'
import { useSaveState } from '@/components/admin/useSaveState'
import { Button } from '@/components/ui/button'
import { Field, TextField } from '@/components/ui/field'
import { Loadable } from '@/components/ui/Loadable'
import { InlineError, MessageState } from '@/components/ui/states'
import {
  createGalleryImage,
  deleteGalleryImage,
  getAdminGallery,
  reorderGallery,
  updateGalleryImage,
} from '@/services/api/adminSiteService'
import type { ProductCategory } from '@/types/catalog'
import type { GalleryImage } from '@/types/site'

/** Galería de trabajos: agregar, editar título y línea, ordenar y eliminar. */
export function GalleryPanel() {
  const { refreshDashboard } = useAdminOutlet()
  const list = useOrderedList(getAdminGallery, reorderGallery)
  const [toDelete, setToDelete] = useState<GalleryImage | null>(null)
  const changed = () => {
    list.reload()
    refreshDashboard()
  }

  return (
    <Panel title="Galería de trabajos" description="Imágenes de la página Trabajos. También se pueden usar en cualquier selector de imagen.">
      <NewGalleryImage onCreated={changed} />
      <div className="mt-8">
        <InlineError message={list.error} className="mb-4" />
        <Loadable
          state={list.loaded}
          loadingLabel="Cargando galería…"
          errorTitle="No se pudo cargar la galería"
          isEmpty={(images) => images.length === 0}
          empty={<MessageState title="La galería está vacía." />}
        >
          {(images) => (
            <ul className="flex flex-col gap-3">
              {images.map((image, index) => (
                <li key={image.id}>
                  <GalleryRow
                    image={image}
                    onSaved={list.replace}
                    onDelete={() => setToDelete(image)}
                    moveButtons={
                      <MoveButtons label={image.title} index={index} count={images.length} onMove={list.move} disabled={list.busy} />
                    }
                  />
                </li>
              ))}
            </ul>
          )}
        </Loadable>
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
          changed()
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
    <form noValidate onSubmit={add} className="grid gap-5 rounded-[20px] bg-secondary p-5 lg:grid-cols-[minmax(0,360px)_1fr]">
      <ImagePicker label="Nueva imagen" value={url} onChange={setUrl} aspect="4/3" />
      <div className="flex flex-col gap-5">
        <TextField
          label="Título"
          hint='Sin título queda "Imagen". Ejemplo: Fachada comercial.'
          maxLength={120}
          value={title}
          onChange={setTitle}
        />
        <Field label="Línea">{(props) => <CategorySelect {...props} allowNone value={category} onChange={setCategory} />}</Field>
        <InlineError message={error} />
        <SaveButton save={save} label="Agregar a la galería" busyLabel="Agregando…" />
      </div>
    </form>
  )
}

function GalleryRow({
  image,
  onSaved,
  onDelete,
  moveButtons,
}: {
  image: GalleryImage
  onSaved: (image: GalleryImage) => void
  onDelete: () => void
  moveButtons: ReactNode
}) {
  const [title, setTitle] = useState(image.title)
  const [category, setCategory] = useState(image.category)
  const save = useSaveState()
  const changed = title.trim() !== image.title || category !== image.category

  async function submit(event: FormEvent) {
    event.preventDefault()
    const saved = await save.run(() => updateGalleryImage(image.id, { title: title.trim(), url: image.url, category }))
    if (saved) onSaved(saved)
  }

  return (
    <form noValidate onSubmit={submit} className="grid items-center gap-4 rounded-[20px] border border-border p-4 sm:grid-cols-[120px_1fr]">
      <img src={image.url} alt={image.title} className="aspect-[4/3] w-full rounded-[12px] bg-secondary object-cover" loading="lazy" />
      <div className="flex flex-col gap-3">
        <div className="grid gap-3 sm:grid-cols-2">
          <TextField label="Título" maxLength={120} value={title} onChange={setTitle} />
          <Field label="Línea">{(props) => <CategorySelect {...props} allowNone value={category} onChange={setCategory} />}</Field>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <SaveButton save={save} label="Guardar" size="sm" disabled={!changed} />
          {moveButtons}
          <Button size="sm" variant="destructive" onClick={onDelete}>
            Eliminar
          </Button>
        </div>
      </div>
    </form>
  )
}
