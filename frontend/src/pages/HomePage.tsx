import { Link } from 'react-router'

import { Kicker, Section, SectionTitle } from '@/components/site/Section'
import { QuoteCallout } from '@/components/site/QuoteCallout'
import { Button } from '@/components/ui/button'
import { useSite } from '@/site/siteContext'
import { catalogHref, ROUTES } from '@/lib/routes'

/** Inicio (briefing §5.3): portada editable, trayectoria, líneas destacadas y llamado a cotizar. */
export default function HomePage() {
  const site = useSite()
  return (
    <>
      <section aria-labelledby="portada" className="bg-background">
        <div className="page-container pt-10 text-center md:pt-20">
          <Kicker>Fábrica · Instalación · A medida</Kicker>
          <h1
            id="portada"
            className="mx-auto mt-3 max-w-4xl font-display text-hero-sm leading-[1.05] font-semibold tracking-[-0.025em] text-balance sm:text-hero md:text-display-xl md:tracking-[-0.015em]"
          >
            {site.heroTitle}
          </h1>
          <p className="mx-auto mt-4 max-w-2xl text-body leading-snug text-muted-foreground md:mt-5 md:text-intro-lg">
            {site.heroSubtitle}
          </p>
          <div className="mt-7 flex flex-wrap justify-center gap-3 md:mt-8">
            {site.quotingEnabled && (
              <Button asChild size="lg">
                <Link to={ROUTES.quote}>Cotizar</Link>
              </Button>
            )}
            <Button asChild size="lg" variant="secondary">
              <Link to={ROUTES.catalog}>Ver catálogo</Link>
            </Button>
          </div>
          <ul className="mt-6 flex flex-wrap justify-center gap-x-6 gap-y-1 text-sm text-muted-foreground">
            <li>Perfiles de alta resistencia</li>
            <li>Vidrio de seguridad templado</li>
          </ul>
        </div>
        {site.heroImageUrl && (
          <div className="mx-auto mt-8 max-w-[1240px] px-4 pb-12 md:mt-12 md:px-6 md:pb-24">
            <figure className="relative overflow-hidden rounded-[28px] bg-secondary">
              <img
                src={site.heroImageUrl}
                alt="Ventanas y fachadas de aluminio y vidrio"
                width={1600}
                height={900}
                fetchPriority="high"
                className="aspect-[4/3] w-full object-cover sm:aspect-[16/10] md:aspect-[16/9]"
              />
              {site.heroBadge && (
                <figcaption className="absolute top-3 left-3 max-w-[calc(100%-1.5rem)] truncate rounded-full sm:top-4 sm:left-4 bg-white/85 px-4 py-2 text-xs font-semibold text-foreground backdrop-blur-md">
                  {site.heroBadge}
                </figcaption>
              )}
              <div className="absolute right-3 bottom-3 left-3 rounded-[16px] bg-white/90 p-3 backdrop-blur-md sm:right-auto sm:bottom-4 sm:left-4 sm:max-w-xs sm:rounded-[20px] sm:p-4">
                <p className="text-xs font-semibold text-status">Ingeniería estructural</p>
                <p className="mt-1 text-callout font-semibold sm:text-body">Calibre europeo y sellado térmico</p>
              </div>
            </figure>
          </div>
        )}
      </section>

      <Section tone="mist" labelledBy="trayectoria">
        <div className="grid gap-12 md:grid-cols-[1fr_1.4fr] md:items-end">
          <div>
            <Kicker>Nuestra trayectoria</Kicker>
            <SectionTitle id="trayectoria">{site.experienceYears} años fabricando confianza</SectionTitle>
            <p className="mt-4 text-body text-muted-foreground">Desde nuestro taller, a la medida de cada obra.</p>
            <Button asChild variant="link" className="mt-4 text-body">
              <Link to={ROUTES.about}>Conócenos</Link>
            </Button>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <article className="tile bg-background">
              <p className="text-xs font-semibold text-muted-foreground">Ajuste técnico</p>
              <h3 className="mt-2 font-display text-2xl font-semibold tracking-[-0.01em]">Medida milimétrica</h3>
              <p className="mt-2 text-muted-foreground">Precisión milimétrica.</p>
            </article>
            <article className="tile bg-background">
              <p className="text-xs font-semibold text-muted-foreground">Garantía</p>
              <h3 className="mt-2 font-display text-2xl font-semibold tracking-[-0.01em]">Instalación limpia</h3>
              <p className="mt-2 text-muted-foreground">
                Personal técnico especializado que respeta tu obra, protegiendo pisos, acabados y dejando el área
                impecable.
              </p>
            </article>
          </div>
        </div>
      </Section>

      <Section labelledBy="lineas">
        <div className="max-w-2xl">
          <Kicker>Líneas especializadas</Kicker>
          <SectionTitle id="lineas">Lo que fabricamos</SectionTitle>
          <p className="mt-4 text-body text-muted-foreground">
            Cuatro líneas de trabajo y una sola obsesión: medidas exactas, instalación limpia y acabados que duren.
          </p>
        </div>
        <ul className="mt-8 grid gap-4 md:mt-12 md:grid-cols-2 md:gap-5">
          {site.featuredLines.map((line) => (
            <li key={line.id}>
              <Link
                to={catalogHref(line.targetCategory)}
                className="group block overflow-hidden rounded-[28px] bg-secondary transition-transform duration-100 active:scale-[0.99] motion-reduce:active:scale-100"
              >
                {line.imageUrl && (
                  <img
                    src={line.imageUrl}
                    alt=""
                    width={1000}
                    height={625}
                    loading="lazy"
                    className="aspect-[16/10] w-full object-cover transition-transform duration-500 group-hover:scale-[1.02] motion-reduce:transition-none"
                  />
                )}
                <div className="p-5 md:p-7">
                  <p className="text-xs font-semibold text-muted-foreground">
                    {line.number} · {line.categoryTag}
                  </p>
                  <h3 className="mt-2 font-display text-title-3 font-semibold tracking-[-0.015em] md:text-title-2">{line.title}</h3>
                  <p className="mt-2 text-muted-foreground">{line.description}</p>
                  <p className="mt-4 text-link group-hover:underline">{line.linkText}</p>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      </Section>

      <QuoteCallout
        kicker="Atención personalizada"
        title="¿Listo para tu proyecto? Recibimos tu cotización."
        text="Medidas, obras y proyectos residenciales, comerciales y empresariales."
        note="Respuesta técnica en menos de 24 horas"
      />
    </>
  )
}
