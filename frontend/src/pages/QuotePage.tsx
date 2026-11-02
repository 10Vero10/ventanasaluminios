import { useState } from 'react'
import { Navigate, useSearchParams } from 'react-router'

import { type Configuration, INITIAL_CONFIGURATION, isDrawable, resolveConfiguration } from '@/components/quote/configuration'
import { type ApplyFeedback, GlassConfigurator } from '@/components/quote/GlassConfigurator'
import { QuoteAdvisory } from '@/components/quote/QuoteAdvisory'
import { QuoteContactCard } from '@/components/quote/QuoteContactCard'
import { afterSent, applyConfiguration, emptyDraft, syncConfiguration, writeDimensions } from '@/components/quote/quoteDraft'
import { QuoteForm } from '@/components/quote/QuoteForm'
import { PageHeader, Section, SectionTitle } from '@/components/site/Section'
import { PRODUCT_TYPE_OPTIONS } from '@/lib/productType'
import { useLoad } from '@/lib/useLoad'
import { getGlassOptions } from '@/services/api/catalogService'
import { useSite } from '@/site/siteContext'

const MAX_PRODUCT_TYPE = 120

/** Cotizar (briefing §5.6). Sin cotización activa no existe: vuelve al inicio. */
export default function QuotePage() {
  const site = useSite()
  if (!site.quotingEnabled) return <Navigate to="/" replace />
  return <QuoteContent />
}

/**
 * En el celular el formulario queda debajo del configurador: después de "Aplicar" se baja hasta él para que se vean
 * las medidas puestas. En pantallas anchas están lado a lado y no hace falta.
 */
function showFormOnSmallScreens() {
  if (window.matchMedia('(min-width: 1024px)').matches) return
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  requestAnimationFrame(() =>
    document.getElementById('solicitud')?.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth', block: 'start' }),
  )
}

function QuoteContent() {
  const [params] = useSearchParams()
  const preselected = params.get('producto')?.trim().slice(0, MAX_PRODUCT_TYPE) || null
  const productOptions =
    preselected && !PRODUCT_TYPE_OPTIONS.some((o) => o.value === preselected)
      ? [{ label: preselected, value: preselected }, ...PRODUCT_TYPE_OPTIONS]
      : PRODUCT_TYPE_OPTIONS

  const glassOptions = useLoad(getGlassOptions, [])
  const [config, setConfig] = useState<Configuration>(INITIAL_CONFIGURATION)
  const [feedback, setFeedback] = useState<ApplyFeedback>('idle')
  const [draft, setDraft] = useState(() => emptyDraft(preselected ?? PRODUCT_TYPE_OPTIONS[0].value))

  /** Configuración resuelta contra las opciones activas; null si falta algo o una medida está en 0. */
  function resolveValid(next: Configuration) {
    if (glassOptions.status !== 'ready' || !isDrawable(next)) return null
    return resolveConfiguration(next, glassOptions.data)
  }

  // Las actualizaciones son funcionales: el envío de la cotización termina en otro momento y no debe pisar con un
  // borrador viejo lo que el visitante escribió mientras tanto.
  function changeConfig(patch: Partial<Configuration>) {
    const next = { ...config, ...patch }
    const resolved = resolveValid(next)
    setConfig(next)
    setDraft((current) => syncConfiguration(current, resolved))
  }

  function apply() {
    const resolved = resolveValid(config)
    if (resolved) {
      setDraft((current) => applyConfiguration(current, resolved))
      showFormOnSmallScreens()
    }
    setFeedback(resolved ? 'applied' : 'invalid')
  }

  function changeDimensions(text: string) {
    setDraft((current) => writeDimensions(current, text))
    setFeedback('idle')
  }

  function changeProductType(productType: string) {
    // Con una medida en 0 se conservan las últimas medidas válidas, pero el tipo de producto sí cambia.
    const resolved = resolveValid(config)
    setDraft((current) => syncConfiguration({ ...current, productType }, resolved))
  }

  return (
    <>
      <Section className="pb-8 md:pb-10">
        <PageHeader
          kicker="Cotizar"
          title="Hablemos de tu proyecto"
          lead="Escríbenos o llámanos: te asesoramos en materiales, medidas y presupuesto con tolerancias exactas y garantía de fábrica."
        />
      </Section>

      <QuoteContactCard />

      <Section>
        <div className="grid gap-12 lg:grid-cols-2 lg:gap-16">
          <section aria-labelledby="configurador">
            <p className="text-sm font-semibold text-status">Configurador dimensional a escala</p>
            <SectionTitle id="configurador" className="text-[24px] md:text-[32px]">
              Plano Esquemático Dinámico
            </SectionTitle>
            <div className="mt-8">
              <GlassConfigurator options={glassOptions} config={config} onChange={changeConfig} onApply={apply} feedback={feedback} />
            </div>
          </section>

          <section id="solicitud" aria-labelledby="formulario" className="scroll-mt-20 lg:sticky lg:top-24 lg:self-start">
            <p className="text-sm font-semibold text-status">Formulario directo</p>
            <SectionTitle id="formulario" className="text-[24px] md:text-[32px]">
              Solicitud de Cotización Rápida
            </SectionTitle>
            <p className="mt-3 text-muted-foreground">
              Completa tus datos y recibirás asesoría técnica detallada con cálculo de materiales.
            </p>
            <QuoteForm
              draft={draft}
              productOptions={productOptions}
              onProductType={changeProductType}
              onDimensions={changeDimensions}
              onMessage={(message) => setDraft((current) => ({ ...current, message }))}
              onSent={() => setDraft(afterSent)}
            />
          </section>
        </div>
      </Section>

      <QuoteAdvisory />
    </>
  )
}
