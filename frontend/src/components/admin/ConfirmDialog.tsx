import { useState } from 'react'

import { Button } from '@/components/ui/button'
import { Dialog } from '@/components/ui/dialog'
import { getErrorMessage } from '@/services/api/httpClient'

/** Confirmación para acciones que no se pueden deshacer desde el panel (apple-design: solo para lo destructivo). */
export function ConfirmDialog({
  open,
  onOpenChange,
  title,
  description,
  confirmLabel,
  onConfirm,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  description?: string
  confirmLabel: string
  onConfirm: () => Promise<void>
}) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function confirm() {
    setBusy(true)
    setError(null)
    try {
      await onConfirm()
      onOpenChange(false)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(next) => {
        if (!busy) {
          setError(null)
          onOpenChange(next)
        }
      }}
      title={title}
      description={description}
      className="sm:w-[min(480px,calc(100vw-48px))]"
    >
      {error && (
        <p role="alert" className="mb-4 text-sm text-destructive">
          {error}
        </p>
      )}
      <div className="flex flex-wrap justify-end gap-3">
        <Button variant="secondary" onClick={() => onOpenChange(false)} disabled={busy}>
          Cancelar
        </Button>
        <Button variant="destructive" onClick={confirm} disabled={busy}>
          {busy ? 'Procesando…' : confirmLabel}
        </Button>
      </div>
    </Dialog>
  )
}
