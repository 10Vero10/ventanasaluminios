import type { ReactNode } from 'react'

import { Loadable } from '@/components/ui/Loadable'
import { useLoad } from '@/lib/useLoad'
import { getSettings, updateSettings } from '@/services/api/adminSiteService'
import { toSettingsRequest } from '@/types/admin'
import type { SiteSettings } from '@/types/site'

import { Panel } from './Panel'
import { SaveRow } from './SaveRow'
import { type DraftState, useDraft } from './useDraft'
import { useSaveState } from './useSaveState'

/**
 * Formulario de una parte de la configuración del sitio: carga, error, campos y "Guardar cambios". Guarda la
 * configuración completa ({@code PUT /admin/settings} reemplaza todo). Cambiar {@code reloadKey} la vuelve a leer
 * (por ejemplo, después de importar un respaldo).
 */
export function SettingsForm({
  title,
  description,
  className,
  reloadKey = 0,
  children,
}: {
  title: string
  description: string
  className?: string
  reloadKey?: number
  children: (form: DraftState<SiteSettings>) => ReactNode
}) {
  const loaded = useLoad(getSettings, [reloadKey])
  return (
    <Panel title={title} description={description}>
      <Loadable state={loaded} loadingLabel="Cargando configuración…" errorTitle="No se pudo cargar la configuración">
        {(settings) => (
          <SettingsFormBody source={settings} onSaved={(saved) => loaded.setData(() => saved)} className={className}>
            {children}
          </SettingsFormBody>
        )}
      </Loadable>
    </Panel>
  )
}

function SettingsFormBody({
  source,
  onSaved,
  className,
  children,
}: {
  source: SiteSettings
  onSaved: (saved: SiteSettings) => void
  className?: string
  children: (form: DraftState<SiteSettings>) => ReactNode
}) {
  const form = useDraft(source)
  const save = useSaveState()

  async function submit() {
    const sent = form.draft
    const saved = await save.run(() => updateSettings(toSettingsRequest(sent)))
    if (saved) {
      onSaved(saved)
      // Lo guardado puede venir normalizado (espacios, WhatsApp derivado del teléfono); lo escrito mientras tanto queda.
      form.commit(sent, saved)
    }
  }

  return (
    <form
      noValidate
      className="flex flex-col gap-6"
      onSubmit={(event) => {
        event.preventDefault()
        void submit()
      }}
    >
      <div className={className}>{children(form)}</div>
      <SaveRow save={save} />
    </form>
  )
}
