import { Section, SectionTitle } from '@/components/site/Section'

/** Asesoría técnica y horario (briefing §5.6.5). */
export function QuoteAdvisory() {
  return (
    <Section tone="mist" labelledBy="asesoria">
      <div className="grid gap-8 md:grid-cols-[1.4fr_1fr] md:items-center">
        <div>
          <SectionTitle id="asesoria" className="mt-0 text-[28px] md:text-[40px]">
            Asesoría técnica y visita a domicilio
          </SectionTitle>
          <p className="mt-4 text-[17px] text-muted-foreground">
            Realizamos visitas técnicas para toma de medidas exactas, muestreo físico de perfiles de aluminio y revisión
            estructural en sitio sin compromiso adicional.
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
  )
}
