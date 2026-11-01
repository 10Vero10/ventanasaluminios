import { type FormEvent, useState } from 'react'

import { Button } from '@/components/ui/button'
import { Field, TextAreaField, TextField } from '@/components/ui/field'
import { OptionSelect } from '@/components/ui/OptionSelect'
import { InlineError } from '@/components/ui/states'
import { mailHref, phoneHref } from '@/lib/contact'
import { getErrorMessage } from '@/services/api/httpClient'
import { createQuote } from '@/services/api/quoteService'
import { useSite } from '@/site/siteContext'

import { MAX_MESSAGE, type QuoteDraft } from './quoteDraft'

type SubmitState = { status: 'idle' } | { status: 'sending' } | { status: 'sent' } | { status: 'error'; message: string }

/**
 * Solicitud de Cotización Rápida (briefing §5.6.4). Los datos de contacto y el envío viven aquí; tipo, medidas y
 * mensaje vienen de la página porque el configurador también los llena.
 */
export function QuoteForm({
  draft,
  productOptions,
  onProductType,
  onDimensions,
  onMessage,
  onSent,
}: {
  draft: QuoteDraft
  productOptions: readonly { value: string; label: string }[]
  onProductType: (productType: string) => void
  onDimensions: (text: string) => void
  onMessage: (message: string) => void
  onSent: () => void
}) {
  const site = useSite()
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [website, setWebsite] = useState('')
  const [missingContact, setMissingContact] = useState(false)
  const [submit, setSubmit] = useState<SubmitState>({ status: 'idle' })

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!name.trim() || !phone.trim()) return setMissingContact(true)
    setMissingContact(false)
    setSubmit({ status: 'sending' })
    const structured = draft.applied
    try {
      await createQuote({
        name: name.trim(),
        phone: phone.trim(),
        productType: draft.productType,
        dimensionsText: draft.dimensionsText.trim(),
        message: draft.message.trim(),
        website,
        ...(structured && {
          widthCm: structured.widthCm,
          heightCm: structured.heightCm,
          panes: structured.panes,
          glassTypeCode: structured.type.code,
          glassColorCode: structured.color.code,
          thicknessMm: structured.thickness.mm,
        }),
      })
      setName('')
      setPhone('')
      onSent()
      setSubmit({ status: 'sent' })
    } catch (error) {
      setSubmit({ status: 'error', message: getErrorMessage(error) })
    }
  }

  if (submit.status === 'sent') {
    return (
      <div role="status" className="mt-8 rounded-[28px] bg-secondary p-8">
        <p className="font-display text-[28px] font-semibold">¡Cotización enviada!</p>
        <p className="mt-2 text-[17px] text-muted-foreground">
          Recibimos tu solicitud. Nos pondremos en contacto contigo a la brevedad al número que indicaste.
        </p>
        <Button variant="secondary" className="mt-6" onClick={() => setSubmit({ status: 'idle' })}>
          Enviar otra cotización
        </Button>
      </div>
    )
  }

  const sending = submit.status === 'sending'
  return (
    <form noValidate onSubmit={handleSubmit} className="mt-8 flex flex-col gap-5">
      <TextField
        label="Nombre y apellido"
        required
        error={missingContact && !name.trim() ? 'Escribe tu nombre.' : null}
        name="name"
        autoComplete="name"
        maxLength={100}
        placeholder="Ej. Carlos Mendoza"
        value={name}
        onChange={setName}
      />
      <TextField
        label="Teléfono / WhatsApp"
        required
        error={missingContact && !phone.trim() ? 'Escribe tu teléfono.' : null}
        name="phone"
        type="tel"
        autoComplete="tel"
        maxLength={40}
        placeholder="+57 300 000 0000"
        value={phone}
        onChange={setPhone}
      />
      <Field label="Tipo de producto">
        {(props) => (
          <OptionSelect {...props} options={productOptions} value={draft.productType} onChange={(v) => v && onProductType(v)} />
        )}
      </Field>
      <TextField
        label="Medidas aproximadas (alto x ancho en cm)"
        hint="Puedes usar el configurador para fijar medidas exactas."
        name="dimensionsText"
        maxLength={200}
        placeholder="Ej. 120 alto x 150 ancho"
        value={draft.dimensionsText}
        onChange={onDimensions}
      />
      <TextAreaField
        label="Mensaje o detalles del proyecto"
        name="message"
        maxLength={MAX_MESSAGE}
        placeholder="Tipo de vidrio (templado, laminado), color de perfil o dirección aproximada…"
        value={draft.message}
        onChange={onMessage}
      />
      {/* Campo trampa: las personas no lo ven ni llegan con el teclado; un bot que lo llene no se guarda. */}
      <div aria-hidden="true" className="absolute -left-[9999px] h-px w-px overflow-hidden">
        <label>
          Sitio web
          <input name="website" tabIndex={-1} autoComplete="off" value={website} onChange={(e) => setWebsite(e.target.value)} />
        </label>
      </div>

      <InlineError message={missingContact ? 'Por favor ingresa tu nombre y teléfono para recibir la cotización.' : null} />
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
  )
}
