import { type FormEvent, useState } from 'react'
import { Navigate, useSearchParams } from 'react-router'

import {
  type Configuration,
  dimensionsText,
  glassDescription,
  INITIAL_CONFIGURATION,
  isDrawable,
  type ResolvedConfiguration,
  resolveConfiguration,
} from '@/components/quote/configuration'
import { type ApplyFeedback, GlassConfigurator } from '@/components/quote/GlassConfigurator'
import { PageHeader, Section, SectionTitle } from '@/components/site/Section'
import { Button } from '@/components/ui/button'
import { Field, Input, Select, Textarea } from '@/components/ui/field'
import { buildAutoMessage, mergeAutoMessage } from '@/lib/configurator'
import { mailHref, phoneHref, whatsappHref } from '@/lib/contact'
import { PRODUCT_TYPE_OPTIONS } from '@/lib/productType'
import { useLoad } from '@/lib/useLoad'
import { getGlassOptions } from '@/services/api/catalogService'
import { getErrorMessage } from '@/services/api/httpClient'
import { createQuote } from '@/services/api/quoteService'
import { useSite } from '@/site/siteContext'
import type { GlassOptions } from '@/types/catalog'

interface FormValues {
  name: string
  phone: string
  productType: string
  dimensionsText: string
  message: string
  website: string
}

type SubmitState = { status: 'idle' } | { status: 'sending' } | { status: 'sent' } | { status: 'error'; message: string }

const MAX_PRODUCT_TYPE = 120
const MAX_MESSAGE = 2000

/** Cotizar (briefing §5.6). Sin cotización activa no existe: vuelve al inicio. */
export default function QuotePage() {
  const site = useSite()
  if (!site.quotingEnabled) return <Navigate to="/" replace />
  return <QuoteContent />
}

function QuoteContent() {
  const site = useSite()
  const [params] = useSearchParams()
  const preselected = params.get('producto')?.trim().slice(0, MAX_PRODUCT_TYPE) || null
  const productOptions =
    preselected && !PRODUCT_TYPE_OPTIONS.some((o) => o.value === preselected)
      ? [{ label: preselected, value: preselected }, ...PRODUCT_TYPE_OPTIONS]
      : PRODUCT_TYPE_OPTIONS

  const glassOptions = useLoad(getGlassOptions, [])
  const [config, setConfig] = useState<Configuration>(INITIAL_CONFIGURATION)
  const [feedback, setFeedback] = useState<ApplyFeedback>('idle')
  /** Configuración que está en el formulario y se envía; null antes del primer "Aplicar" o si se editan las medidas a mano. */
  const [applied, setApplied] = useState<ResolvedConfiguration | null>(null)
  /** Último mensaje sugerido puesto en el formulario, para reemplazarlo en vez de repetirlo. */
  const [lastAutoMessage, setLastAutoMessage] = useState<string | null>(null)
  const [values, setValues] = useState<FormValues>({
    name: '',
    phone: '',
    productType: preselected ?? PRODUCT_TYPE_OPTIONS[0].value,
    dimensionsText: '',
    message: '',
    website: '',
  })
  const [missingContact, setMissingContact] = useState(false)
  const [submit, setSubmit] = useState<SubmitState>({ status: 'idle' })

  const options: GlassOptions | null = glassOptions.status === 'ready' ? glassOptions.data : null

  /** Pasa la configuración al formulario. Con una medida en 0 conserva los últimos valores válidos. */
  function applyToForm(nextConfig: Configuration, productType: string, current: FormValues) {
    const resolved = options ? resolveConfiguration(nextConfig, options) : null
    if (!resolved || !isDrawable(nextConfig)) return null
    const autoMessage = buildAutoMessage(productType, resolved.widthCm, resolved.heightCm, glassDescription(resolved))
    setValues({
      ...current,
      productType,
      dimensionsText: dimensionsText(resolved),
      // El texto sugerido va al inicio; se recorta para no pasar el máximo que acepta el servidor.
      message: mergeAutoMessage(current.message, lastAutoMessage, autoMessage).slice(0, MAX_MESSAGE),
    })
    setApplied(resolved)
    setLastAutoMessage(autoMessage)
    return resolved
  }

  function changeConfig(patch: Partial<Configuration>) {
    const next = { ...config, ...patch }
    setConfig(next)
    if (applied) applyToForm(next, values.productType, values)
  }

  function apply() {
    const resolved = applyToForm(config, values.productType, values)
    setFeedback(resolved ? 'applied' : 'invalid')
  }

  function changeProductType(productType: string) {
    // Con una medida en 0 se conservan las últimas medidas válidas, pero el tipo de producto sí cambia.
    const synced = applied && applyToForm(config, productType, values)
    if (!synced) setValues({ ...values, productType })
  }

  /** Escribir las medidas a mano desconecta el configurador: el servidor guardaría su texto en vez del escrito. */
  function changeDimensionsText(dimensionsText: string) {
    setValues({ ...values, dimensionsText })
    if (applied) {
      setApplied(null)
      setFeedback('idle')
    }
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!values.name.trim() || !values.phone.trim()) {
      setMissingContact(true)
      return
    }
    setMissingContact(false)
    setSubmit({ status: 'sending' })
    const structured = applied
    try {
      await createQuote({
        name: values.name.trim(),
        phone: values.phone.trim(),
        productType: values.productType,
        dimensionsText: values.dimensionsText.trim(),
        message: values.message.trim(),
        website: values.website,
        ...(structured && {
          widthCm: structured.widthCm,
          heightCm: structured.heightCm,
          panes: structured.panes,
          glassTypeCode: structured.type.code,
          glassColorCode: structured.color.code,
          thicknessMm: structured.thickness.mm,
        }),
      })
      setValues((v) => ({ ...v, name: '', phone: '', message: '' }))
      // El mensaje se vació: no hay sugerencia anterior que reemplazar.
      setLastAutoMessage(null)
      setSubmit({ status: 'sent' })
    } catch (error) {
      setSubmit({ status: 'error', message: getErrorMessage(error) })
    }
  }

  const sending = submit.status === 'sending'

  return (
    <>
      <Section className="pb-8 md:pb-10">
        <PageHeader
          kicker="Cotizar"
          title="Hablemos de tu proyecto"
          lead="Escríbenos o llámanos: te asesoramos en materiales, medidas y presupuesto con tolerancias exactas y garantía de fábrica."
        />
      </Section>

      <section aria-labelledby="contacto" className="mx-auto max-w-[1080px] px-6">
        <div className="grid gap-8 rounded-[28px] bg-secondary p-7 md:grid-cols-[1.3fr_1fr] md:p-10">
          <div>
            <p className="text-sm font-semibold text-status">Contáctanos</p>
            <h2 id="contacto" className="mt-1 font-display text-[28px] leading-tight font-semibold tracking-[-0.015em]">
              Cotiza sin costo y sin compromiso
            </h2>
            <p className="mt-3 text-[17px] text-muted-foreground">
              Cuéntanos el espacio y las medidas, y te recomendamos la mejor opción en vidrio, aluminio o PVC para tu
              hogar, tu oficina o tu negocio.
            </p>
            <Button asChild size="lg" className="mt-6">
              <a href={whatsappHref(site.whatsappNumber)} target="_blank" rel="noopener noreferrer">
                Escribir por WhatsApp
              </a>
            </Button>
            <p className="mt-2 text-xs text-muted-foreground">Respuesta rápida · Canal directo</p>
          </div>
          <dl className="flex flex-col justify-center gap-5">
            <div>
              <dt className="text-xs text-muted-foreground">Correo electrónico</dt>
              <dd>
                <a href={mailHref(site.email)} className="text-[17px] break-all text-link hover:underline">
                  {site.email}
                </a>
              </dd>
            </div>
            <div>
              <dt className="text-xs text-muted-foreground">Teléfono · WhatsApp</dt>
              <dd>
                <a href={phoneHref(site.phone)} className="text-[17px] text-link hover:underline">
                  {site.phone}
                </a>
              </dd>
            </div>
          </dl>
        </div>
      </section>

      <Section>
        <div className="grid gap-12 lg:grid-cols-2 lg:gap-16">
          <section aria-labelledby="configurador">
            <p className="text-sm font-semibold text-status">Configurador dimensional a escala</p>
            <SectionTitle id="configurador" className="text-[28px] md:text-[32px]">
              Plano Esquemático Dinámico
            </SectionTitle>
            <div className="mt-8">
              <GlassConfigurator
                options={glassOptions}
                config={config}
                onChange={changeConfig}
                onApply={apply}
                feedback={feedback}
              />
            </div>
          </section>

          <section aria-labelledby="formulario" className="lg:sticky lg:top-24 lg:self-start">
            <p className="text-sm font-semibold text-status">Formulario directo</p>
            <SectionTitle id="formulario" className="text-[28px] md:text-[32px]">
              Solicitud de Cotización Rápida
            </SectionTitle>
            <p className="mt-3 text-muted-foreground">
              Completa tus datos y recibirás asesoría técnica detallada con cálculo de materiales.
            </p>

            {submit.status === 'sent' ? (
              <div role="status" className="mt-8 rounded-[28px] bg-secondary p-8">
                <p className="font-display text-[28px] font-semibold">¡Cotización enviada!</p>
                <p className="mt-2 text-[17px] text-muted-foreground">
                  Recibimos tu solicitud. Nos pondremos en contacto contigo a la brevedad al número que indicaste.
                </p>
                <Button variant="secondary" className="mt-6" onClick={() => setSubmit({ status: 'idle' })}>
                  Enviar otra cotización
                </Button>
              </div>
            ) : (
              <form noValidate onSubmit={handleSubmit} className="mt-8 flex flex-col gap-5">
                <Field label="Nombre y apellido" required error={missingContact && !values.name.trim() ? 'Escribe tu nombre.' : null}>
                  {(props) => (
                    <Input
                      {...props}
                      name="name"
                      autoComplete="name"
                      maxLength={100}
                      placeholder="Ej. Carlos Mendoza"
                      value={values.name}
                      onChange={(e) => setValues({ ...values, name: e.target.value })}
                    />
                  )}
                </Field>
                <Field label="Teléfono / WhatsApp" required error={missingContact && !values.phone.trim() ? 'Escribe tu teléfono.' : null}>
                  {(props) => (
                    <Input
                      {...props}
                      name="phone"
                      type="tel"
                      autoComplete="tel"
                      maxLength={40}
                      placeholder="+57 300 000 0000"
                      value={values.phone}
                      onChange={(e) => setValues({ ...values, phone: e.target.value })}
                    />
                  )}
                </Field>
                <Field label="Tipo de producto">
                  {(props) => (
                    <Select {...props} name="productType" value={values.productType} onChange={(e) => changeProductType(e.target.value)}>
                      {productOptions.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </Select>
                  )}
                </Field>
                <Field
                  label="Medidas aproximadas (alto x ancho en cm)"
                  hint="Puedes usar el configurador para fijar medidas exactas."
                >
                  {(props) => (
                    <Input
                      {...props}
                      name="dimensionsText"
                      maxLength={200}
                      placeholder="Ej. 120 alto x 150 ancho"
                      value={values.dimensionsText}
                      onChange={(e) => changeDimensionsText(e.target.value)}
                    />
                  )}
                </Field>
                <Field label="Mensaje o detalles del proyecto">
                  {(props) => (
                    <Textarea
                      {...props}
                      name="message"
                      maxLength={2000}
                      placeholder="Tipo de vidrio (templado, laminado), color de perfil o dirección aproximada…"
                      value={values.message}
                      onChange={(e) => setValues({ ...values, message: e.target.value })}
                    />
                  )}
                </Field>
                {/* Campo trampa: las personas no lo ven ni llegan con el teclado; un bot que lo llene no se guarda. */}
                <div aria-hidden="true" className="absolute -left-[9999px] h-px w-px overflow-hidden">
                  <label>
                    Sitio web
                    <input
                      name="website"
                      tabIndex={-1}
                      autoComplete="off"
                      value={values.website}
                      onChange={(e) => setValues({ ...values, website: e.target.value })}
                    />
                  </label>
                </div>

                {missingContact && (
                  <p role="alert" className="text-sm text-destructive">
                    Por favor ingresa tu nombre y teléfono para recibir la cotización.
                  </p>
                )}
                {submit.status === 'error' && (
                  <p role="alert" className="rounded-[16px] bg-secondary p-4 text-sm">
                    <span className="text-destructive">{submit.message}</span> También puedes llamarnos al{' '}
                    <a href={phoneHref(site.phone)} className="text-link hover:underline">
                      {site.phone}
                    </a>{' '}
                    o escribir a{' '}
                    <a href={mailHref(site.email)} className="text-link hover:underline">
                      {site.email}
                    </a>
                    .
                  </p>
                )}
                <Button type="submit" size="lg" disabled={sending} className="self-start">
                  {sending ? 'Enviando…' : 'Enviar cotización'}
                </Button>
              </form>
            )}
          </section>
        </div>
      </Section>

      <Section tone="mist" labelledBy="asesoria">
        <div className="grid gap-8 md:grid-cols-[1.4fr_1fr] md:items-center">
          <div>
            <SectionTitle id="asesoria" className="mt-0 text-[28px] md:text-[40px]">
              Asesoría técnica y visita a domicilio
            </SectionTitle>
            <p className="mt-4 text-[17px] text-muted-foreground">
              Realizamos visitas técnicas para toma de medidas exactas, muestreo físico de perfiles de aluminio y
              revisión estructural en sitio sin compromiso adicional.
            </p>
          </div>
          <ul className="flex flex-col gap-3 text-[17px]">
            <li>
              <span className="font-semibold">Lunes a Viernes:</span> 7:30 AM – 6:00 PM
            </li>
            <li>Instaladores certificados con protocolos de seguridad en obra</li>
          </ul>
        </div>
      </Section>
    </>
  )
}
