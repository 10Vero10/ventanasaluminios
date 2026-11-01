import { useId, useRef, useState } from 'react'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/field'
import { useLoad } from '@/lib/useLoad'
import { getAdminGallery, uploadImage } from '@/services/api/adminSiteService'
import { getErrorMessage } from '@/services/api/httpClient'

const MAX_BYTES = 5 * 1024 * 1024
const ACCEPTED = ['image/jpeg', 'image/png', 'image/webp']

/**
 * Selector de imagen (briefing §5.7.8): pegar una URL, subir un archivo (JPG, PNG o WebP de máximo 5 MB) o elegir una
 * de la galería. Muestra la vista previa con la proporción indicada.
 */
export function ImagePicker({
  label,
  value,
  onChange,
  aspect = '16/9',
  allowClear = true,
}: {
  label: string
  value: string | null
  onChange: (url: string | null) => void
  aspect?: string
  allowClear?: boolean
}) {
  const id = useId()
  const fileInput = useRef<HTMLInputElement>(null)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [showGallery, setShowGallery] = useState(false)

  async function handleFile(file: File | undefined) {
    if (!file) return
    setError(null)
    if (!ACCEPTED.includes(file.type)) {
      setError(file.type.startsWith('image/') ? 'Solo se aceptan imágenes JPG, PNG o WebP.' : 'El archivo seleccionado no es una imagen.')
      return
    }
    if (file.size > MAX_BYTES) {
      setError(
        `La imagen es demasiado pesada (${Math.round(file.size / 1024)} KB). Usa una imagen de máximo 5 MB o pega la URL de una imagen alojada en internet.`,
      )
      return
    }
    setUploading(true)
    try {
      onChange(await uploadImage(file))
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setUploading(false)
      if (fileInput.current) fileInput.current.value = ''
    }
  }

  return (
    <div className="flex flex-col gap-3">
      <label htmlFor={id} className="text-sm font-semibold">
        {label}
      </label>
      <div className="overflow-hidden rounded-[16px] bg-secondary" style={{ aspectRatio: aspect }}>
        {value ? (
          <img src={value} alt="Vista previa" className="h-full w-full object-cover" />
        ) : (
          <p className="flex h-full items-center justify-center p-4 text-center text-sm text-muted-foreground">Sin imagen</p>
        )}
      </div>
      <Input
        id={id}
        type="url"
        inputMode="url"
        placeholder="Pega la URL de una imagen"
        value={value ?? ''}
        onChange={(e) => onChange(e.target.value.trim() || null)}
      />
      <div className="flex flex-wrap gap-2">
        <input
          ref={fileInput}
          type="file"
          accept={ACCEPTED.join(',')}
          className="sr-only"
          tabIndex={-1}
          aria-hidden="true"
          onChange={(e) => handleFile(e.target.files?.[0])}
        />
        <Button size="sm" variant="secondary" disabled={uploading} onClick={() => fileInput.current?.click()}>
          {uploading ? 'Procesando…' : 'Subir archivo'}
        </Button>
        <Button size="sm" variant="secondary" aria-expanded={showGallery} onClick={() => setShowGallery((v) => !v)}>
          {showGallery ? 'Ocultar galería' : 'Elegir de la galería'}
        </Button>
        {allowClear && value && (
          <Button size="sm" variant="ghost" onClick={() => onChange(null)}>
            Quitar imagen
          </Button>
        )}
      </div>
      {error && (
        <p role="alert" className="text-sm text-destructive">
          {error}
        </p>
      )}
      {showGallery && (
        <GalleryChooser
          onPick={(url) => {
            onChange(url)
            setShowGallery(false)
          }}
        />
      )}
    </div>
  )
}

function GalleryChooser({ onPick }: { onPick: (url: string) => void }) {
  const gallery = useLoad(getAdminGallery, [])
  if (gallery.status === 'loading') return <p className="text-sm text-muted-foreground">Cargando galería…</p>
  if (gallery.status === 'error') {
    return (
      <p role="alert" className="text-sm text-destructive">
        {gallery.message}{' '}
        <button type="button" className="text-link underline" onClick={gallery.reload}>
          Intentar de nuevo
        </button>
      </p>
    )
  }
  if (gallery.data.length === 0) return <p className="text-sm text-muted-foreground">La galería está vacía.</p>
  return (
    <ul className="grid grid-cols-3 gap-2 sm:grid-cols-4">
      {gallery.data.map((image) => (
        <li key={image.id}>
          <button
            type="button"
            onClick={() => onPick(image.url)}
            className="block w-full overflow-hidden rounded-[12px] transition-transform duration-100 active:scale-[0.97] focus-visible:outline-2 focus-visible:outline-ring"
          >
            <img src={image.url} alt={image.title} className="aspect-[4/3] w-full object-cover" loading="lazy" />
          </button>
        </li>
      ))}
    </ul>
  )
}
