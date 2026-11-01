import { useRef, useState } from 'react'

import { Button } from '@/components/ui/button'
import { InlineError } from '@/components/ui/states'
import { downloadJson } from '@/lib/download'
import { exportBackup, importBackup } from '@/services/api/backupService'
import type { BackupImportResult } from '@/types/admin'

import { useAdminOutlet } from './adminOutlet'
import { ConfirmDialog } from './ConfirmDialog'
import { Panel } from './Panel'
import { useSaveState } from './useSaveState'

/** Por debajo del límite de nginx (6 MB) para que el error sea este y no uno genérico. */
const MAX_BACKUP_BYTES = 5 * 1024 * 1024

/** Exportar e importar el respaldo del contenido (briefing §3.10). */
export function BackupPanel({ onImported }: { onImported: () => void }) {
  const { refreshDashboard } = useAdminOutlet()
  const fileInput = useRef<HTMLInputElement>(null)
  const exporting = useSaveState()
  const [pending, setPending] = useState<{ name: string; text: string } | null>(null)
  const [result, setResult] = useState<BackupImportResult | null>(null)
  const [fileError, setFileError] = useState<string | null>(null)

  async function download() {
    await exporting.run(async () =>
      downloadJson(`respaldo-ventanas-y-aluminios-${new Date().toISOString().slice(0, 10)}.json`, await exportBackup()),
    )
  }

  async function pick(file: File | undefined) {
    setFileError(null)
    setResult(null)
    // Se limpia siempre, para poder elegir otra vez el mismo archivo después de un error.
    if (fileInput.current) fileInput.current.value = ''
    if (!file) return
    if (file.size > MAX_BACKUP_BYTES) {
      setFileError('El archivo pesa más de 5 MB: no parece un respaldo de este sitio.')
      return
    }
    setPending({ name: file.name, text: await file.text() })
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
        <InlineError message={exporting.state.status === 'error' ? exporting.state.message : null} />
        <InlineError message={fileError} />
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
