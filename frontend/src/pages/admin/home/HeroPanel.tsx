import { ImagePicker } from '@/components/admin/ImagePicker'
import { SettingsForm } from '@/components/admin/SettingsForm'
import { useSettingsDraft } from '@/components/admin/useSettingsDraft'
import { TextAreaField, TextField } from '@/components/ui/field'

/** Portada: título, subtítulo, etiqueta e imagen principal. */
export function HeroPanel() {
  const settings = useSettingsDraft()
  return (
    <SettingsForm
      settings={settings}
      title="Portada"
      description="Textos e imagen que ven las personas al entrar al sitio."
      className="grid gap-6 lg:grid-cols-2"
    >
      {(draft) => (
        <>
          <div className="flex flex-col gap-5">
            <TextField label="Título de portada" required maxLength={120} {...settings.bind('heroTitle')} />
            <TextAreaField label="Subtítulo" maxLength={300} {...settings.bind('heroSubtitle')} />
            <TextField
              label="Etiqueta sobre la imagen"
              hint="Opcional. Ejemplo: Vidrio templado y aluminio."
              maxLength={80}
              {...settings.bindOptional('heroBadge')}
            />
          </div>
          <ImagePicker
            label="Imagen principal (recomendado 16:9)"
            value={draft.heroImageUrl}
            onChange={(url) => settings.set('heroImageUrl', url)}
          />
        </>
      )}
    </SettingsForm>
  )
}
