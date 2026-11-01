import { Button } from '@/components/ui/button'
import { mailHref, phoneHref, whatsappHref } from '@/lib/contact'
import { useSite } from '@/site/siteContext'

/** Canales de contacto de la página Cotizar (briefing §5.6.2). */
export function QuoteContactCard() {
  const site = useSite()
  return (
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
  )
}
