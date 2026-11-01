import { BackupPanel } from '@/components/admin/BackupPanel'
import { ImagePicker } from '@/components/admin/ImagePicker'
import { SettingsForm } from '@/components/admin/SettingsForm'
import { useSettingsDraft } from '@/components/admin/useSettingsDraft'
import { Button } from '@/components/ui/button'
import { TextField } from '@/components/ui/field'
import { Switch } from '@/components/ui/switch'

const DEFAULT_FAVICON = '/favicon.svg'

/** Pestaña Configuración (briefing §5.7.7): cotización, contacto, favicon y respaldo. */
export default function SettingsTab() {
  const settings = useSettingsDraft()
  return (
    <div className="flex flex-col gap-6">
      <SettingsForm
        settings={settings}
        title="Configuración general"
        description="Controla lo que se muestra a las personas en el sitio."
        className="flex flex-col gap-8"
      >
        {({ draft, bind, set }) => (
          <>
            <div className="flex items-start justify-between gap-6 rounded-[20px] bg-secondary p-5">
              <div>
                <p id="cotizacion-label" className="font-semibold">
                  Activar opción de cotización
                </p>
                <p id="cotizacion-ayuda" className="mt-1 max-w-2xl text-sm text-muted-foreground">
                  Al desactivarla se ocultan el menú Cotizar, los botones de cotización, el formulario, el configurador y
                  el acceso directo por WhatsApp. Teléfono y correo siguen visibles.
                </p>
              </div>
              <Switch
                checked={draft.quotingEnabled}
                onChange={(value) => set('quotingEnabled', value)}
                labelledBy="cotizacion-label"
                describedBy="cotizacion-ayuda"
              />
            </div>

            <fieldset className="grid gap-5 md:grid-cols-3">
              <legend className="mb-4 font-display text-xl font-semibold">Datos de contacto</legend>
              <TextField label="Teléfono" type="tel" maxLength={40} placeholder="+57 300 000 0000" {...bind('phone')} />
              <TextField label="Correo" type="email" maxLength={254} placeholder="correo@empresa.com" {...bind('email')} />
              <TextField
                label="WhatsApp"
                hint="Con indicativo de país y sin espacios (ej. 573178463260). Vacío usa el teléfono."
                inputMode="numeric"
                maxLength={20}
                placeholder="573000000000"
                {...bind('whatsappNumber')}
              />
            </fieldset>

            <div className="grid gap-5 md:grid-cols-2">
              <div>
                <h3 className="font-display text-xl font-semibold">Icono de pestaña</h3>
                <p className="mt-1 text-sm text-muted-foreground">
                  Es el logo que aparece en la pestaña del navegador. Sin imagen se usa el logo de 4 paneles del sitio.
                </p>
                <div className="mt-4 flex items-center gap-3">
                  <img
                    src={draft.faviconUrl ?? DEFAULT_FAVICON}
                    alt="Icono actual"
                    width={40}
                    height={40}
                    className="size-10 rounded-[8px] bg-secondary object-contain"
                  />
                  {draft.faviconUrl && draft.faviconUrl !== DEFAULT_FAVICON && (
                    <Button size="sm" variant="secondary" onClick={() => set('faviconUrl', null)}>
                      Usar el logo de 4 paneles
                    </Button>
                  )}
                </div>
              </div>
              <ImagePicker
                label="Imagen del icono"
                value={draft.faviconUrl}
                onChange={(url) => set('faviconUrl', url)}
                aspect="1/1"
                previewClassName="w-32"
                allowClear={false}
              />
            </div>
          </>
        )}
      </SettingsForm>
      <BackupPanel onImported={settings.reload} />
    </div>
  )
}
