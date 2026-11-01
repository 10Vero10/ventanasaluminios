import { useSearchParams } from 'react-router'

import { CategoryFilter } from '@/components/site/CategoryFilter'
import { QuoteCallout } from '@/components/site/QuoteCallout'
import { Kicker, Section } from '@/components/site/Section'
import { Button } from '@/components/ui/button'
import { LoadingState, MessageState } from '@/components/ui/states'
import { categoryFromParam, categoryToParam } from '@/lib/categoryParam'
import { useLoad } from '@/lib/useLoad'
import { getGallery } from '@/services/api/siteService'
import { categoryLabel, type ProductCategory } from '@/types/catalog'

/** Galería de trabajos (decisión de la usuaria): cada imagen con su título y su línea, filtrable por línea. */
export default function GalleryPage() {
  const [params, setParams] = useSearchParams()
  const category = categoryFromParam(params.get('linea'))
  const images = useLoad(() => getGallery(category ?? undefined), [category])

  function selectCategory(next: ProductCategory | null) {
    const value = categoryToParam(next)
    setParams(value ? { linea: value } : {}, { replace: true })
  }

  return (
    <>
      <Section className="pb-10 md:pb-12">
        <Kicker>Galería de trabajos</Kicker>
        <h1 className="mt-2 max-w-3xl font-display text-[40px] leading-[1.05] font-semibold tracking-[-0.02em] text-balance md:text-[64px]">
          Trabajos realizados
        </h1>
        <p className="mt-5 max-w-2xl text-[19px] text-muted-foreground">
          Proyectos que fabricamos e instalamos en hogares, oficinas y locales comerciales.
        </p>
        <div className="mt-10">
          <CategoryFilter value={category} onChange={selectCategory} allLabel="Todas las líneas" label="Filtrar por línea" />
        </div>
      </Section>

      <section aria-label="Imágenes de trabajos" className="mx-auto max-w-[1080px] px-6 pb-16 md:pb-24">
        {images.status === 'loading' && <LoadingState label="Cargando trabajos…" />}
        {images.status === 'error' && (
          <MessageState
            tone="error"
            title="No pudimos cargar la galería"
            description={images.message}
            action={<Button onClick={images.reload}>Intentar de nuevo</Button>}
          />
        )}
        {images.status === 'ready' && images.data.length === 0 && (
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
        )}
        {images.status === 'ready' && images.data.length > 0 && (
          <ul className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {images.data.map((image) => (
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
      </section>

      <QuoteCallout
        kicker="Inicia tu proyecto"
        title="¿Quieres un trabajo así? Hablemos."
        text="Fabricamos e instalamos cada proyecto a la medida exacta de tu hogar u oficina."
      />
    </>
  )
}
