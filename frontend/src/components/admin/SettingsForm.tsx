import type { ReactNode } from 'react'

import { Loadable } from '@/components/ui/Loadable'
import type { SiteSettings } from '@/types/site'

import { Panel } from './Panel'
import { SaveRow } from './SaveButton'
import type { SettingsDraft } from './useSettingsDraft'

/** Lo que reciben los campos: el borrador ya cargado y las funciones para editarlo. */
export type SettingsFormState = SettingsDraft & { draft: SiteSettings }

/** Formulario de una parte de la configuración del sitio: carga, error, campos y "Guardar cambios". */
export function SettingsForm({
  settings,
  title,
  description,
  className,
  children,
}: {
  settings: SettingsDraft
  title: string
  description: string
  className?: string
  children: (form: SettingsFormState) => ReactNode
}) {
  return (
    <Panel title={title} description={description}>
      <Loadable state={settings.loaded} loadingLabel="Cargando configuración…" errorTitle="No se pudo cargar la configuración">
        {() =>
          settings.draft && (
            <form
              noValidate
              className="flex flex-col gap-6"
              onSubmit={(event) => {
                event.preventDefault()
                void settings.submit()
              }}
            >
              <div className={className}>{children({ ...settings, draft: settings.draft })}</div>
              <SaveRow save={settings.save} />
            </form>
          )
        }
      </Loadable>
    </Panel>
  )
}
