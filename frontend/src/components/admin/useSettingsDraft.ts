import { useLoad } from '@/lib/useLoad'
import { getSettings, updateSettings } from '@/services/api/adminSiteService'
import { toSettingsRequest } from '@/types/admin'

import { useDraft } from './useDraft'
import { useSaveState } from './useSaveState'

/**
 * Borrador de la configuración del sitio. Cada pestaña edita sus campos y guarda la configuración completa
 * ({@code PUT /admin/settings} reemplaza todo).
 */
export function useSettingsDraft() {
  const loaded = useLoad(getSettings, [])
  const draft = useDraft(loaded.status === 'ready' ? loaded.data : null)
  const save = useSaveState()

  async function submit() {
    if (!draft.draft) return
    const request = toSettingsRequest(draft.draft)
    const saved = await save.run(() => updateSettings(request))
    if (saved) {
      // Lo guardado puede venir normalizado (espacios, WhatsApp derivado del teléfono): se muestra tal cual.
      loaded.setData(() => saved)
      draft.reset(saved)
    }
  }

  return { ...draft, loaded, submit, save, reload: loaded.reload }
}

export type SettingsDraft = ReturnType<typeof useSettingsDraft>
