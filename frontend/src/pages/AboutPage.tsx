import { QuoteCallout } from '@/components/site/QuoteCallout'
import { Kicker, PageTitle, Section, SectionTitle } from '@/components/site/Section'
import { useSite } from '@/site/siteContext'

const EXPERIENCE = [
  {
    title: 'Trayectoria consolidada',
    subtitle: null,
    text: 'Fabricando e instalando soluciones arquitectónicas de alta gama con precisión europea.',
  },
  {
    title: 'Proyectos integrales',
    subtitle: 'Alcance versátil',
    text: 'Atención a proyectos residenciales, comerciales y empresariales con garantías reales.',
  },
  {
    title: 'Equipo especializado',
    subtitle: 'Sin intermediarios',
    text: 'Fabricación e instalación con personal técnico propio certificado en seguridad de obra.',
  },
]

const COMMITMENT = [
  {
    title: 'Medidas exactas',
    text: 'Precisión milimétrica en vanos, perfiles y vidrios templados para un acople hermético y perfecto.',
  },
  {
    title: 'Instalación limpia',
    text: 'Procedimientos rigurosos en obra que cuidan tu espacio, con sellados pulcros y protección de áreas.',
  },
  {
    title: 'Garantía comprobada',
    text: 'Soporte continuo y materiales certificados que aseguran máxima resistencia acústica y climática.',
  },
]

/** Quiénes somos (briefing §5.5): los tres párrafos son editables desde el panel. */
export default function AboutPage() {
  const site = useSite()
  const years = site.experienceYears
  return (
    <>
      <Section>
        <div className="grid gap-12 md:grid-cols-[1.2fr_1fr] md:items-center">
          <div>
            <Kicker>¿Quiénes somos?</Kicker>
            <PageTitle>Una empresa que mide todo, menos su dedicación</PageTitle>
            <p className="mt-4 max-w-xl text-[17px] text-muted-foreground md:mt-5 md:text-[19px]">
              Fabricación e instalación de soluciones en vidrio, aluminio y otros materiales desde hace más de {years}{' '}
              años.
            </p>
          </div>
          <div className="rounded-[28px] bg-secondary p-8">
            <p className="font-display text-[56px] leading-none font-semibold tracking-[-0.02em]">{years}+ años</p>
            <p className="mt-3 text-sm font-semibold text-status">Solidez y confianza</p>
            <p className="text-[17px]">Garantía en obra</p>
          </div>
        </div>
      </Section>

      <Section tone="mist" labelledBy="historia">
        <div className={site.aboutImageUrl ? 'grid gap-10 md:grid-cols-2 md:items-center md:gap-14' : undefined}>
          {site.aboutImageUrl && (
            <figure>
              <img
                src={site.aboutImageUrl}
                alt="Taller de carpintería y ensamble a medida de Ventanas y Aluminios"
                width={1200}
                height={900}
                loading="lazy"
                className="aspect-[4/3] w-full rounded-[28px] bg-background object-cover"
              />
              <figcaption className="mt-3 text-sm text-muted-foreground">Taller de carpintería y ensamble a medida</figcaption>
            </figure>
          )}
          <div>
            <Kicker>Nuestra historia</Kicker>
            <SectionTitle id="historia">¿Quiénes somos?</SectionTitle>
            <div className="mt-6 grid max-w-3xl gap-4 text-[17px] leading-relaxed md:mt-8 md:gap-5 md:text-[19px]">
              <p>{site.aboutParagraph1}</p>
              <p>{site.aboutParagraph2}</p>
              <p className="border-l-2 border-status pl-5 font-semibold">{site.aboutHighlight}</p>
            </div>
          </div>
        </div>
      </Section>

      <Section labelledBy="experiencia">
        <div className="grid gap-10 md:grid-cols-[1fr_auto] md:items-end">
          <div>
            <Kicker>Nuestra experiencia</Kicker>
            <SectionTitle id="experiencia">Más de {years} años fortaleciendo procesos</SectionTitle>
            <p className="mt-4 max-w-2xl text-[17px] text-muted-foreground">
              Más de {years} años de trayectoria nos han permitido fortalecer nuestros conocimientos, mejorar nuestros
              procesos y ofrecer trabajos confiables y duraderos.
            </p>
          </div>
          <p className="text-right">
            <span className="block font-display text-[64px] leading-none font-semibold tracking-[-0.02em]">{years}</span>
            <span className="text-sm text-muted-foreground">Años de experiencia</span>
          </p>
        </div>
        <ul className="mt-8 grid gap-4 md:mt-12 md:grid-cols-3">
          {EXPERIENCE.map((item) => (
            <li key={item.title} className="rounded-[24px] bg-secondary p-5 md:rounded-[28px] md:p-7">
              {item.subtitle && <p className="text-xs font-semibold text-muted-foreground">{item.subtitle}</p>}
              <h3 className="mt-1 font-display text-2xl font-semibold tracking-[-0.01em]">{item.title}</h3>
              <p className="mt-2 text-muted-foreground">{item.text}</p>
            </li>
          ))}
        </ul>
      </Section>

      <Section tone="mist" labelledBy="compromiso">
        <Kicker>Nuestro compromiso</Kicker>
        <SectionTitle id="compromiso">Calidad en cada entrega</SectionTitle>
        <p className="mt-4 max-w-2xl text-[17px] text-muted-foreground">
          Nos comprometemos con cada proyecto, buscando siempre entregar calidad, responsabilidad, cumplimiento y
          excelentes acabados.
        </p>
        <ul className="mt-10 grid gap-4 md:grid-cols-3">
          {COMMITMENT.map((item) => (
            <li key={item.title} className="rounded-[24px] bg-background p-5 md:rounded-[28px] md:p-7">
              <h3 className="font-display text-2xl font-semibold tracking-[-0.01em]">{item.title}</h3>
              <p className="mt-2 text-muted-foreground">{item.text}</p>
            </li>
          ))}
        </ul>
      </Section>

      <QuoteCallout
        kicker="Inicia tu proyecto"
        title="¿Quieres un trabajo así de cuidado? Hablemos."
        text="Te contamos cómo fabricamos e instalamos cada proyecto a la medida exacta de tu hogar u oficina."
      />
    </>
  )
}
