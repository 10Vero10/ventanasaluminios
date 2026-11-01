import type { ReactNode } from 'react'

import { Loadable } from '@/components/ui/Loadable'
import type { SiteSettingsRequest } from '@/types/admin'

import { Panel } from './Panel'
import { SaveButton } from './SaveButton'
import { useSettingsDraft } from './useSettingsDraft'

type SettingsDraft = ReturnType<typeof useSettingsDraft>

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
  children: (draft: SiteSettingsRequest) => ReactNode
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
              <div className={className}>{children(settings.draft)}</div>
              <SaveButton save={settings.save} />
            </form>
          )
        }
      </Loadable>
    </Panel>
  )
}
