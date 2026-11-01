import { Button } from '@/components/ui/button'
import { LoadingState, MessageState } from '@/components/ui/states'
import { formatArea, MAX_HEIGHT_CM, MAX_PANES, MAX_WIDTH_CM, suggestedPanes } from '@/lib/configurator'
import type { LoadState } from '@/lib/useLoad'
import type { GlassOptions } from '@/types/catalog'

import { BlueprintDrawing } from './BlueprintDrawing'
import { type Configuration, glassSummary, isDrawable, resolveConfiguration } from './configuration'
import { MeasureInput } from './MeasureInput'
import { OptionChips } from './OptionChips'

export type ApplyFeedback = 'idle' | 'applied' | 'invalid'

/** Plano Esquemático Dinámico (briefing §5.6.3). El estado vive en la página para sincronizarlo con el formulario. */
export function GlassConfigurator({
  options,
  config,
  onChange,
  onApply,
  feedback,
}: {
  options: LoadState<GlassOptions> & { reload: () => void }
  config: Configuration
  onChange: (patch: Partial<Configuration>) => void
  onApply: () => void
  feedback: ApplyFeedback
}) {
  if (options.status === 'loading') return <LoadingState label="Cargando opciones de vidrio…" />
  if (options.status === 'error') {
    return (
      <MessageState
        tone="error"
        title="No pudimos cargar el configurador"
        description={`${options.message} Puedes escribir las medidas a mano en el formulario.`}
        action={<Button onClick={options.reload}>Intentar de nuevo</Button>}
      />
    )
  }
  const resolved = resolveConfiguration(config, options.data)
  if (!resolved) {
    return (
      <MessageState
        title="El configurador no está disponible"
        description="Escribe las medidas a mano en el formulario y te asesoramos con el vidrio."
      />
    )
  }
  const drawable = isDrawable(config)
  const suggested = suggestedPanes(config.widthCm)

  return (
    <div className="flex flex-col gap-8">
      <div aria-live="polite">
        {drawable ? (
          <BlueprintDrawing config={resolved} />
        ) : (
          <p className="flex h-[296px] items-center justify-center rounded-[20px] bg-secondary px-6 text-center text-muted-foreground">
            Ajusta el ancho y el alto (mayores a 0 cm) para ver el plano.
          </p>
        )}
      </div>

      <dl className="grid grid-cols-3 gap-3">
        {[
          { label: 'Ancho', value: `${config.widthCm} cm` },
          { label: 'Alto', value: `${config.heightCm} cm` },
          { label: 'Área estimada', value: `${formatArea(config.widthCm, config.heightCm)} m²` },
        ].map((item) => (
          <div key={item.label} className="rounded-[16px] bg-secondary px-4 py-3">
            <dt className="text-xs text-muted-foreground">{item.label}</dt>
            <dd className="font-display text-xl font-semibold tabular-nums">{item.value}</dd>
          </div>
        ))}
      </dl>
      <p className="-mt-4 text-sm text-muted-foreground">{glassSummary(resolved)}</p>

      <div className="grid gap-6 sm:grid-cols-2">
        <MeasureInput
          label="Ancho de ventana (cm)"
          name="ancho"
          value={config.widthCm}
          max={MAX_WIDTH_CM}
          onChange={(widthCm) => onChange({ widthCm })}
        />
        <MeasureInput
          label="Alto de ventana (cm)"
          name="alto"
          value={config.heightCm}
          max={MAX_HEIGHT_CM}
          onChange={(heightCm) => onChange({ heightCm })}
        />
      </div>

      <OptionChips
        legend="Hojas"
        name="hojas"
        value={resolved.panes}
        onChange={(panes) => onChange({ panes })}
        options={Array.from({ length: MAX_PANES }, (_, i) => ({
          value: i + 1,
          label: i + 1 === suggested ? `${i + 1} (sugerido)` : String(i + 1),
        }))}
        hint="Sugerimos una hoja por cada 150 cm de ancho."
      />
      <OptionChips
        legend="Tipo de vidrio"
        name="tipo-vidrio"
        value={resolved.type.code}
        onChange={(typeCode) => onChange({ typeCode })}
        options={options.data.types.map((t) => ({ value: t.code, label: t.name }))}
      />
      <OptionChips
        legend="Color del vidrio"
        name="color-vidrio"
        value={resolved.color.code}
        onChange={(colorCode) => onChange({ colorCode })}
        options={options.data.colors.map((c) => ({ value: c.code, label: c.name, swatch: c.hex }))}
        hint={resolved.color.note ?? undefined}
      />
      <OptionChips
        legend="Espesor"
        name="espesor"
        value={resolved.thickness.mm}
        onChange={(thicknessMm) => onChange({ thicknessMm })}
        options={options.data.thicknesses.map((t) => ({ value: t.mm, label: `${t.mm} mm` }))}
      />

      <div className="flex flex-col items-start gap-3">
        <Button onClick={onApply}>Aplicar medidas a mi cotización</Button>
        <p role="status" className="min-h-6 text-sm">
          {feedback === 'applied' && <span className="text-foreground">✓ Medidas aplicadas al formulario de cotización.</span>}
          {feedback === 'invalid' && (
            <span className="text-destructive">El ancho y el alto deben ser mayores a 0 cm para aplicarlos.</span>
          )}
        </p>
      </div>
    </div>
  )
}
