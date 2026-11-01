import { type FormEvent, useRef, useState } from 'react'

import { useAdminOutlet } from '@/components/admin/adminOutlet'
import { ConfirmDialog } from '@/components/admin/ConfirmDialog'
import { ImagePicker } from '@/components/admin/ImagePicker'
import { Panel } from '@/components/admin/Panel'
import { SaveFeedback } from '@/components/admin/SaveFeedback'
import { useSaveState } from '@/components/admin/useSaveState'
import { useSettingsDraft } from '@/components/admin/useSettingsDraft'
import { Button } from '@/components/ui/button'
import { Field, Input } from '@/components/ui/field'
import { LoadingState, MessageState } from '@/components/ui/states'
import { exportBackup, importBackup } from '@/services/api/backupService'
import type { BackupImportResult } from '@/types/admin'

/** Pestaña Configuración (briefing §5.7.7): cotización, contacto, favicon y respaldo. */
export default function SettingsTab() {
  const { loaded, draft, set, submit, save, reload } = useSettingsDraft()

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    void submit()
  }

  return (
    <div className="flex flex-col gap-6">
      <Panel title="Configuración general" description="Controla lo que se muestra a las personas en el sitio.">
        {loaded.status === 'loading' && <LoadingState label="Cargando configuración…" />}
        {loaded.status === 'error' && (
          <MessageState tone="error" title="No se pudo cargar la configuración" description={loaded.message} action={<Button onClick={loaded.reload}>Intentar de nuevo</Button>} />
        )}
        {draft && (
          <form onSubmit={handleSubmit} className="flex flex-col gap-8">
            <div className="flex items-start justify-between gap-6 rounded-[20px] bg-secondary p-5">
              <div>
                <p id="cotizacion-label" className="font-semibold">
                  Activar opción de cotización
                </p>
                <p id="cotizacion-ayuda" className="mt-1 max-w-2xl text-sm text-muted-foreground">
                  Al desactivarla se ocultan el menú Cotizar, los botones de cotización, el formulario, el configurador y el
                  acceso directo por WhatsApp. Teléfono y correo siguen visibles.
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
              <Field label="Teléfono">
                {(props) => <Input {...props} type="tel" maxLength={40} placeholder="+57 300 000 0000" value={draft.phone} onChange={(e) => set('phone', e.target.value)} />}
              </Field>
              <Field label="Correo">
                {(props) => <Input {...props} type="email" maxLength={254} placeholder="correo@empresa.com" value={draft.email} onChange={(e) => set('email', e.target.value)} />}
              </Field>
              <Field label="WhatsApp" hint="Con indicativo de país y sin espacios (ej. 573178463260). Vacío usa el teléfono.">
                {(props) => (
                  <Input
                    {...props}
                    inputMode="numeric"
                    maxLength={20}
                    placeholder="573000000000"
                    value={draft.whatsappNumber}
                    onChange={(e) => set('whatsappNumber', e.target.value)}
                  />
                )}
              </Field>
            </fieldset>

            <div className="grid gap-5 md:grid-cols-[1fr_1fr]">
              <div>
                <h3 className="font-display text-xl font-semibold">Icono de pestaña</h3>
                <p className="mt-1 text-sm text-muted-foreground">
                  Es el logo que aparece en la pestaña del navegador. Sin imagen se usa el logo de 4 paneles del sitio.
                </p>
                <div className="mt-4 flex items-center gap-3">
                  <img src={draft.faviconUrl ?? '/favicon.svg'} alt="Icono actual" width={40} height={40} className="size-10 rounded-[8px] bg-secondary object-contain" />
                  {draft.faviconUrl && (
                    <Button size="sm" variant="secondary" onClick={() => set('faviconUrl', null)}>
                      Usar el logo de 4 paneles
                    </Button>
                  )}
                </div>
              </div>
              <ImagePicker label="Imagen del icono" value={draft.faviconUrl} onChange={(url) => set('faviconUrl', url)} aspect="1/1" allowClear={false} />
            </div>

            <div className="flex flex-wrap items-center gap-4">
              <Button type="submit" disabled={save.saving}>
                {save.saving ? 'Guardando…' : 'Guardar cambios'}
              </Button>
              <SaveFeedback state={save.state} />
            </div>
          </form>
        )}
      </Panel>
      <BackupPanel onImported={reload} />
    </div>
  )
}

function Switch({
  checked,
  onChange,
  labelledBy,
  describedBy,
}: {
  checked: boolean
  onChange: (value: boolean) => void
  labelledBy: string
  describedBy: string
}) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-labelledby={labelledBy}
      aria-describedby={describedBy}
      onClick={() => onChange(!checked)}
      className={`relative h-8 w-[52px] shrink-0 rounded-full transition-colors duration-200 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring ${checked ? 'bg-[#34c759]' : 'bg-[#d6d6d6]'}`}
    >
      <span
        aria-hidden="true"
        className={`absolute top-0.5 left-0.5 size-7 rounded-full bg-white shadow-[0_2px_4px_rgba(0,0,0,0.2)] transition-transform duration-200 motion-reduce:transition-none ${checked ? 'translate-x-5' : ''}`}
      />
    </button>
  )
}

function BackupPanel({ onImported }: { onImported: () => void }) {
  const { refreshDashboard } = useAdminOutlet()
  const fileInput = useRef<HTMLInputElement>(null)
  const exporting = useSaveState()
  const [pending, setPending] = useState<{ name: string; text: string } | null>(null)
  const [result, setResult] = useState<BackupImportResult | null>(null)
  const [fileError, setFileError] = useState<string | null>(null)

  async function download() {
    await exporting.run(async () => {
      const data = await exportBackup()
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `respaldo-ventanas-y-aluminios-${new Date().toISOString().slice(0, 10)}.json`
      link.click()
      URL.revokeObjectURL(url)
    })
  }

  async function pick(file: File | undefined) {
    setFileError(null)
    setResult(null)
    if (!file) return
    if (file.size > 10 * 1024 * 1024) {
      setFileError('El archivo es demasiado grande para ser un respaldo del sitio.')
      return
    }
    setPending({ name: file.name, text: await file.text() })
    if (fileInput.current) fileInput.current.value = ''
  }

  return (
    <Panel
      title="Respaldo"
      description="Descarga el contenido del sitio (configuración, galería, líneas, catálogo y opciones de vidrio) o restáuralo desde un archivo. Las cotizaciones no se incluyen."
    >
      <div className="flex flex-wrap items-center gap-3">
        <Button variant="secondary" disabled={exporting.saving} onClick={() => void download()}>
          {exporting.saving ? 'Preparando…' : 'Exportar respaldo'}
        </Button>
        <input
          ref={fileInput}
          type="file"
          accept="application/json,.json"
          className="sr-only"
          tabIndex={-1}
          aria-hidden="true"
          onChange={(e) => void pick(e.target.files?.[0])}
        />
        <Button variant="secondary" onClick={() => fileInput.current?.click()}>
          Importar respaldo
        </Button>
      </div>
      <div className="mt-3">
        {exporting.state.status === 'error' && (
          <p role="alert" className="text-sm text-destructive">
            {exporting.state.message}
          </p>
        )}
        {fileError && (
          <p role="alert" className="text-sm text-destructive">
            {fileError}
          </p>
        )}
        {result && (
          <p role="status" className="text-sm">
            Respaldo restaurado: {result.products} productos, {result.galleryImages} imágenes, {result.featuredLines} líneas
            destacadas y {result.glassTypes + result.glassColors + result.glassThicknesses} opciones de vidrio.
          </p>
        )}
      </div>
      <ConfirmDialog
        open={pending !== null}
        onOpenChange={(open) => !open && setPending(null)}
        title="¿Restaurar este respaldo?"
        description={`Esto reemplazará el contenido actual por el de "${pending?.name ?? ''}". ¿Continuar?`}
        confirmLabel="Restaurar"
        onConfirm={async () => {
          if (!pending) return
          setResult(await importBackup(pending.text))
          onImported()
          refreshDashboard()
        }}
      />
    </Panel>
  )
}
