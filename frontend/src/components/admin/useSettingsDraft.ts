import { useState } from 'react'

import { useLoad } from '@/lib/useLoad'
import { getSettings, updateSettings } from '@/services/api/adminSiteService'
import { type SiteSettingsRequest, toSettingsRequest } from '@/types/admin'

import { useSaveState } from './useSaveState'

type Key = keyof SiteSettingsRequest
type KeysOf<V> = { [K in Key]: SiteSettingsRequest[K] extends V ? K : never }[Key]

/**
 * Borrador de la configuración del sitio. Cada pestaña edita sus campos y guarda la configuración completa
 * ({@code PUT /admin/settings} reemplaza todo), partiendo de lo que devolvió el servidor al abrirla.
 */
export function useSettingsDraft() {
  const loaded = useLoad(getSettings, [])
  const [draft, setDraft] = useState<SiteSettingsRequest | null>(null)
  const save = useSaveState()
  const current = draft ?? (loaded.status === 'ready' ? toSettingsRequest(loaded.data) : null)

  /** Actualiza sobre el borrador más reciente: una subida de imagen que termina tarde no borra lo escrito mientras. */
  function set<K extends Key>(key: K, value: SiteSettingsRequest[K]) {
    if (loaded.status !== 'ready') return
    const base = toSettingsRequest(loaded.data)
    setDraft((previous) => ({ ...(previous ?? base), [key]: value }))
  }

  /** Props de un campo de texto obligatorio. */
  function bind(key: KeysOf<string>) {
    return { value: current?.[key] ?? '', onChange: (value: string) => set(key, value) }
  }

  /** Props de un campo de texto opcional: vacío se guarda como null. */
  function bindOptional(key: KeysOf<string | null>) {
    return { value: current?.[key] ?? '', onChange: (value: string) => set(key, value || null) }
  }

  async function submit() {
    if (!current) return
    const saved = await save.run(() => updateSettings(current))
    if (saved) setDraft(toSettingsRequest(saved))
  }

  /** Descarta el borrador y vuelve a leer del servidor (por ejemplo, después de importar un respaldo). */
  function reload() {
    setDraft(null)
    loaded.reload()
  }

  return { loaded, draft: current, set, bind, bindOptional, submit, save, reload }
}
