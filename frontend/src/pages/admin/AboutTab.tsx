import { ImagePicker } from '@/components/admin/ImagePicker'
import { SettingsForm } from '@/components/admin/SettingsForm'
import { TextAreaField, TextField } from '@/components/ui/field'

const MAX_YEARS = 150

/** Pestaña Quiénes somos (briefing §5.7.5). Los años se usan en todo el sitio. */
export default function AboutTab() {
  return (
    <SettingsForm
      title="Quiénes somos"
      description="Textos de la historia y trayectoria de la empresa."
      className="flex max-w-3xl flex-col gap-5"
    >
      {({ draft, bind, set }) => (
        <>
          <TextField
            label="Años de experiencia"
            hint="Aparece en la cabecera, el pie, el catálogo y esta página."
            type="number"
            inputMode="numeric"
            min={0}
            max={MAX_YEARS}
            inputClassName="max-w-40"
            value={String(draft.experienceYears)}
            onChange={(value) =>
              set('experienceYears', Math.min(MAX_YEARS, Math.max(0, Math.round(Number(value) || 0))))
            }
          />
          <TextAreaField label="Párrafo 1 de la historia" maxLength={2000} {...bind('aboutParagraph1')} />
          <TextAreaField label="Párrafo 2 de la historia" maxLength={2000} {...bind('aboutParagraph2')} />
          <TextAreaField label="Texto destacado" maxLength={1000} {...bind('aboutHighlight')} />
          <ImagePicker
            label="Foto del taller (recomendado 4:3)"
            value={draft.aboutImageUrl}
            onChange={(url) => set('aboutImageUrl', url)}
            aspect="4/3"
            previewClassName="max-w-md"
          />
        </>
      )}
    </SettingsForm>
  )
}
