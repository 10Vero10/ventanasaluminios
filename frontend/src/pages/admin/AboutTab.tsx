import { SettingsForm } from '@/components/admin/SettingsForm'
import { useSettingsDraft } from '@/components/admin/useSettingsDraft'
import { TextAreaField, TextField } from '@/components/ui/field'

const MAX_YEARS = 150

/** Pestaña Quiénes somos (briefing §5.7.5). Los años se usan en todo el sitio. */
export default function AboutTab() {
  const settings = useSettingsDraft()
  return (
    <SettingsForm
      settings={settings}
      title="Quiénes somos"
      description="Textos de la historia y trayectoria de la empresa."
      className="flex max-w-3xl flex-col gap-5"
    >
      {(draft) => (
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
              settings.set('experienceYears', Math.min(MAX_YEARS, Math.max(0, Math.round(Number(value) || 0))))
            }
          />
          <TextAreaField label="Párrafo 1 de la historia" maxLength={2000} {...settings.bind('aboutParagraph1')} />
          <TextAreaField label="Párrafo 2 de la historia" maxLength={2000} {...settings.bind('aboutParagraph2')} />
          <TextAreaField label="Texto destacado" maxLength={1000} {...settings.bind('aboutHighlight')} />
        </>
      )}
    </SettingsForm>
  )
}
