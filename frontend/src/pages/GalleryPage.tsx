import { CategoryFilter } from '@/components/site/CategoryFilter'
import { QuoteCallout } from '@/components/site/QuoteCallout'
import { PageHeader, Section } from '@/components/site/Section'
import { Button } from '@/components/ui/button'
import { Loadable } from '@/components/ui/Loadable'
import { MessageState } from '@/components/ui/states'
import { useCategoryParam } from '@/lib/categoryParam'
import { useLoad } from '@/lib/useLoad'
import { getGallery } from '@/services/api/siteService'
import { categoryLabel } from '@/types/catalog'

/** Galería de trabajos (decisión de la usuaria): cada imagen con su título y su línea, filtrable por línea. */
export default function GalleryPage() {
  const [category, selectCategory] = useCategoryParam()
  const images = useLoad(() => getGallery(category ?? undefined), [category])

  return (
    <>
      <Section className="pb-10 md:pb-12">
        <PageHeader
          kicker="Galería de trabajos"
          title="Trabajos realizados"
          lead="Proyectos que fabricamos e instalamos en hogares, oficinas y locales comerciales."
        />
        <div className="mt-10">
          <CategoryFilter value={category} onChange={selectCategory} allLabel="Todas las líneas" label="Filtrar por línea" />
        </div>
      </Section>

      <section aria-label="Imágenes de trabajos" className="mx-auto max-w-[1080px] px-6 pb-16 md:pb-24">
        <Loadable
          state={images}
          loadingLabel="Cargando trabajos…"
          errorTitle="No pudimos cargar la galería"
          empty={(data) =>
            data.length === 0 && (
              <MessageState
                title={category ? `Aún no hay trabajos de ${categoryLabel(category).toLowerCase()}` : 'Aún no hay trabajos publicados'}
                description={category ? 'Mira los trabajos de las otras líneas.' : 'Pronto publicaremos fotos de nuestros proyectos.'}
                action={
                  category && (
                    <Button variant="secondary" onClick={() => selectCategory(null)}>
                      Ver todas las líneas
                    </Button>
                  )
                }
              />
            )
          }
        >
          {(data) => (
            <ul className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {data.map((image) => (
                <li key={image.id}>
                  <figure>
                    <img
                      src={image.url}
                      alt={image.title}
                      width={800}
                      height={600}
                      loading="lazy"
                      className="aspect-[4/3] w-full rounded-[20px] bg-secondary object-cover"
                    />
                    <figcaption className="mt-3">
                      <span className="block text-[17px] font-semibold">{image.title}</span>
                      {image.category && <span className="text-sm text-muted-foreground">{categoryLabel(image.category)}</span>}
                    </figcaption>
                  </figure>
                </li>
              ))}
            </ul>
          )}
        </Loadable>
      </section>

      <QuoteCallout
        kicker="Inicia tu proyecto"
        title="¿Quieres un trabajo así? Hablemos."
        text="Fabricamos e instalamos cada proyecto a la medida exacta de tu hogar u oficina."
      />
    </>
  )
}
