import { type FormEvent, useState } from 'react'

import { CategorySelect } from '@/components/admin/CategorySelect'
import { ImagePicker } from '@/components/admin/ImagePicker'
import { MoveButtons } from '@/components/admin/MoveButtons'
import { Panel } from '@/components/admin/Panel'
import { SaveRow } from '@/components/admin/SaveRow'
import { useDraft } from '@/components/admin/useDraft'
import { useOrderedList } from '@/components/admin/useOrderedList'
import { useSaveState } from '@/components/admin/useSaveState'
import { Button } from '@/components/ui/button'
import { Field, TextAreaField, TextField } from '@/components/ui/field'
import { Loadable } from '@/components/ui/Loadable'
import { InlineError } from '@/components/ui/states'
import { getFeaturedLines, reorderFeaturedLines, updateFeaturedLine } from '@/services/api/adminSiteService'
import { toFeaturedLineRequest } from '@/types/admin'
import { categoryLabel } from '@/types/catalog'
import type { FeaturedLine } from '@/types/site'

/** Tarjetas de líneas de la portada: editar textos e imagen, y ordenar. */
export function FeaturedLinesPanel() {
  const list = useOrderedList(getFeaturedLines, reorderFeaturedLines)
  const [editing, setEditing] = useState<number | null>(null)

  return (
    <Panel title="Líneas destacadas" description="Tarjetas de la portada. Cambia sus textos, su imagen y su orden.">
      <InlineError message={list.error} className="mb-4" />
      <Loadable state={list.loaded} loadingLabel="Cargando líneas…" errorTitle="No se pudieron cargar las líneas">
        {(lines) => (
          <ul className="flex flex-col gap-3">
            {lines.map((line, index) => (
              <li key={line.id} className="rounded-[20px] border border-border p-4">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <p className="text-xs text-muted-foreground">
                      {line.number} · {categoryLabel(line.targetCategory)}
                    </p>
                    <p className="text-body font-semibold">{line.title}</p>
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
                    <MoveButtons label={line.title} index={index} count={lines.length} onMove={list.move} disabled={list.locked} />
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
      </Loadable>
    </Panel>
  )
}

function FeaturedLineEditor({ line, onSaved }: { line: FeaturedLine; onSaved: (line: FeaturedLine) => void }) {
  const { draft, set, bind } = useDraft(line)
  const save = useSaveState()

  async function submit(event: FormEvent) {
    event.preventDefault()
    const saved = await save.run(() => updateFeaturedLine(line.id, toFeaturedLineRequest(draft)))
    if (saved) onSaved(saved)
  }

  return (
    <form noValidate onSubmit={submit} className="mt-5 grid gap-5 lg:grid-cols-2">
      <div className="flex flex-col gap-4">
        <div className="grid gap-4 sm:grid-cols-[100px_1fr]">
          <TextField label="Número" maxLength={4} {...bind('number')} />
          <TextField label="Título" maxLength={80} {...bind('title')} />
        </div>
        <TextField label="Etiqueta de categoría" maxLength={80} {...bind('categoryTag')} />
        <TextAreaField label="Descripción" maxLength={400} {...bind('description')} />
        <div className="grid gap-4 sm:grid-cols-2">
          <TextField label="Texto del enlace" maxLength={40} {...bind('linkText')} />
          <Field label="Categoría destino">
            {(props) => (
              <CategorySelect {...props} value={draft.targetCategory} onChange={(value) => set('targetCategory', value)} />
            )}
          </Field>
        </div>
        <SaveRow save={save} label="Guardar línea" />
      </div>
      <ImagePicker label="Imagen de la tarjeta" value={draft.imageUrl} onChange={(url) => set('imageUrl', url)} aspect="16/10" />
    </form>
  )
}
