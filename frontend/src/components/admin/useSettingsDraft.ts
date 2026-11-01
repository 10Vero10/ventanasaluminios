import { useState } from 'react'

import { useLoad } from '@/lib/useLoad'
import { getSettings, updateSettings } from '@/services/api/adminSiteService'
import type { SiteSettingsRequest } from '@/types/admin'
import type { SiteSettings } from '@/types/site'

import { useSaveState } from './useSaveState'

function toRequest(settings: SiteSettings): SiteSettingsRequest {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { updatedAt, ...request } = settings
  return request
}

/**
 * Borrador de la configuración del sitio. Cada pestaña edita sus campos y guarda la configuración completa
 * ({@code PUT /admin/settings} reemplaza todo), partiendo de lo que devolvió el servidor al abrirla.
 */
export function useSettingsDraft() {
  const loaded = useLoad(getSettings, [])
  const [draft, setDraft] = useState<SiteSettingsRequest | null>(null)
  const save = useSaveState()
  const current = draft ?? (loaded.status === 'ready' ? toRequest(loaded.data) : null)

  /** Actualiza sobre el borrador más reciente: una subida de imagen que termina tarde no borra lo escrito mientras. */
  function set<K extends keyof SiteSettingsRequest>(key: K, value: SiteSettingsRequest[K]) {
    if (loaded.status !== 'ready') return
    const base = toRequest(loaded.data)
    setDraft((previous) => ({ ...(previous ?? base), [key]: value }))
  }

  async function submit() {
    if (!current) return
    const saved = await save.run(() => updateSettings(current))
    if (saved) setDraft(toRequest(saved))
  }

  /** Descarta el borrador y vuelve a leer del servidor (por ejemplo, después de importar un respaldo). */
  function reload() {
    setDraft(null)
    loaded.reload()
  }

  return { loaded, draft: current, set, submit, save, reload }
}
