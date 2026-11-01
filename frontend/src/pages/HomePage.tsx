import { Link } from 'react-router'

import { Kicker, Section, SectionTitle } from '@/components/site/Section'
import { QuoteCallout } from '@/components/site/QuoteCallout'
import { Button } from '@/components/ui/button'
import { catalogHref } from '@/lib/categoryParam'
import { useSite } from '@/site/siteContext'

/** Inicio (briefing §5.3): portada editable, trayectoria, líneas destacadas y llamado a cotizar. */
export default function HomePage() {
  const site = useSite()
  return (
    <>
      <section aria-labelledby="portada" className="bg-background">
        <div className="mx-auto max-w-[1080px] px-6 pt-14 text-center md:pt-20">
          <Kicker>Fábrica · Instalación · A medida</Kicker>
          <h1
            id="portada"
            className="mx-auto mt-3 max-w-4xl font-display text-[44px] leading-[1.05] font-semibold tracking-[-0.025em] text-balance md:text-[80px] md:tracking-[-0.015em]"
          >
            {site.heroTitle}
          </h1>
          <p className="mx-auto mt-5 max-w-2xl text-[19px] leading-snug text-muted-foreground md:text-[21px]">
            {site.heroSubtitle}
          </p>
          <div className="mt-8 flex flex-wrap justify-center gap-3">
            {site.quotingEnabled && (
              <Button asChild size="lg">
                <Link to="/cotizar">Cotizar</Link>
              </Button>
            )}
            <Button asChild size="lg" variant="secondary">
              <Link to="/catalogo">Ver catálogo</Link>
            </Button>
          </div>
          <ul className="mt-6 flex flex-wrap justify-center gap-x-6 gap-y-1 text-sm text-muted-foreground">
            <li>Perfiles de alta resistencia</li>
            <li>Vidrio de seguridad templado</li>
          </ul>
        </div>
        {site.heroImageUrl && (
          <div className="mx-auto mt-12 max-w-[1240px] px-4 pb-16 md:px-6 md:pb-24">
            <figure className="relative overflow-hidden rounded-[28px] bg-secondary">
              <img
                src={site.heroImageUrl}
                alt="Ventanas y fachadas de aluminio y vidrio"
                width={1600}
                height={900}
                fetchPriority="high"
                className="aspect-[4/3] w-full object-cover md:aspect-[16/9]"
              />
              {site.heroBadge && (
                <figcaption className="absolute top-4 left-4 rounded-full bg-white/85 px-4 py-2 text-xs font-semibold text-foreground backdrop-blur-md">
                  {site.heroBadge}
                </figcaption>
              )}
              <div className="absolute right-4 bottom-4 left-4 rounded-[20px] bg-white/90 p-4 backdrop-blur-md sm:right-auto sm:max-w-xs">
                <p className="text-xs font-semibold text-status">Ingeniería estructural</p>
                <p className="mt-1 text-[17px] font-semibold">Calibre europeo y sellado térmico</p>
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
            <p className="mt-4 text-[17px] text-muted-foreground">Desde nuestro taller, a la medida de cada obra.</p>
            <Button asChild variant="link" className="mt-4 text-[17px]">
              <Link to="/nosotros">Conócenos</Link>
            </Button>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <article className="rounded-[28px] bg-background p-7">
              <p className="text-xs font-semibold text-muted-foreground">Ajuste técnico</p>
              <h3 className="mt-2 font-display text-2xl font-semibold tracking-[-0.01em]">Medida milimétrica</h3>
              <p className="mt-2 text-muted-foreground">Precisión milimétrica.</p>
            </article>
            <article className="rounded-[28px] bg-background p-7">
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
          <p className="mt-4 text-[17px] text-muted-foreground">
            Cuatro líneas de trabajo y una sola obsesión: medidas exactas, instalación limpia y acabados que duren.
          </p>
        </div>
        <ul className="mt-12 grid gap-5 md:grid-cols-2">
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
                <div className="p-7">
                  <p className="text-xs font-semibold text-muted-foreground">
                    {line.number} · {line.categoryTag}
                  </p>
                  <h3 className="mt-2 font-display text-[28px] font-semibold tracking-[-0.015em]">{line.title}</h3>
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
