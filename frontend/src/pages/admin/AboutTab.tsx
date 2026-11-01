import type { FormEvent } from 'react'

import { Panel } from '@/components/admin/Panel'
import { SaveFeedback } from '@/components/admin/SaveFeedback'
import { useSettingsDraft } from '@/components/admin/useSettingsDraft'
import { Button } from '@/components/ui/button'
import { Field, Input, Textarea } from '@/components/ui/field'
import { LoadingState, MessageState } from '@/components/ui/states'

/** Pestaña Quiénes somos (briefing §5.7.5). Los años se usan en todo el sitio. */
export default function AboutTab() {
  const { loaded, draft, set, submit, save } = useSettingsDraft()

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    void submit()
  }

  return (
    <Panel title="Quiénes somos" description="Textos de la historia y trayectoria de la empresa.">
      {loaded.status === 'loading' && <LoadingState label="Cargando configuración…" />}
      {loaded.status === 'error' && (
        <MessageState tone="error" title="No se pudo cargar la configuración" description={loaded.message} action={<Button onClick={loaded.reload}>Intentar de nuevo</Button>} />
      )}
      {draft && (
        <form onSubmit={handleSubmit} className="flex max-w-3xl flex-col gap-5">
          <Field label="Años de experiencia" hint="Aparece en la cabecera, el pie, el catálogo y esta página.">
            {(props) => (
              <Input
                {...props}
                type="number"
                inputMode="numeric"
                min={0}
                max={150}
                className="max-w-40"
                value={draft.experienceYears}
                onChange={(e) => set('experienceYears', Math.min(150, Math.max(0, Math.round(Number(e.target.value) || 0))))}
              />
            )}
          </Field>
          <Field label="Párrafo 1 de la historia">
            {(props) => <Textarea {...props} maxLength={2000} value={draft.aboutParagraph1} onChange={(e) => set('aboutParagraph1', e.target.value)} />}
          </Field>
          <Field label="Párrafo 2 de la historia">
            {(props) => <Textarea {...props} maxLength={2000} value={draft.aboutParagraph2} onChange={(e) => set('aboutParagraph2', e.target.value)} />}
          </Field>
          <Field label="Texto destacado">
            {(props) => <Textarea {...props} maxLength={1000} value={draft.aboutHighlight} onChange={(e) => set('aboutHighlight', e.target.value)} />}
          </Field>
          <div className="flex flex-wrap items-center gap-4">
            <Button type="submit" disabled={save.saving}>
              {save.saving ? 'Guardando…' : 'Guardar cambios'}
            </Button>
            <SaveFeedback state={save.state} />
          </div>
        </form>
      )}
    </Panel>
  )
}
