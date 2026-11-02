import { useState } from 'react'
import { Link } from 'react-router'

import { CategoryFilter } from '@/components/site/CategoryFilter'
import { QuoteCallout } from '@/components/site/QuoteCallout'
import { PageHeader, Section } from '@/components/site/Section'
import { Button } from '@/components/ui/button'
import { Dialog } from '@/components/ui/dialog'
import { Loadable } from '@/components/ui/Loadable'
import { MessageState } from '@/components/ui/states'
import { useCategoryParam } from '@/lib/categoryParam'
import { galleryHref, quoteHref } from '@/lib/routes'
import { useLoad } from '@/lib/useLoad'
import { getProducts } from '@/services/api/catalogService'
import { useSite } from '@/site/siteContext'
import { categoryLabel, type Product } from '@/types/catalog'

/** Catálogo (briefing §5.4): filtro por línea en la URL, tarjetas y detalle en diálogo. */
export default function CatalogPage() {
  const site = useSite()
  const [category, selectCategory] = useCategoryParam()
  const products = useLoad(() => getProducts(category ?? undefined), [category])
  const [selected, setSelected] = useState<Product | null>(null)

  return (
    <>
      <Section className="pb-10 md:pb-12">
        <PageHeader
          kicker="Catálogo arquitectónico"
          title="Cuatro líneas de trabajo con medida exacta"
          lead="Cada producto se fabrica para la medida de tu obra. Explora nuestros sistemas, tipos y acabados diseñados para durabilidad estructural y elegancia."
        />
        <dl className="mt-8 grid max-w-3xl grid-cols-3 gap-3 sm:gap-6 md:mt-10">
          <div>
            <dt className="text-xs text-muted-foreground sm:text-sm">Precisión</dt>
            <dd className="font-display text-intro leading-tight font-semibold sm:text-title-2">± 1.0 mm</dd>
            <dd className="text-xs text-muted-foreground sm:text-sm">Ajuste perimetral</dd>
          </div>
          <div>
            <dt className="text-xs text-muted-foreground sm:text-sm">Acabados</dt>
            <dd className="font-display text-intro leading-tight font-semibold sm:text-title-2">Anodizado</dd>
            <dd className="text-xs text-muted-foreground sm:text-sm">Y pintura electrostática</dd>
          </div>
          <div>
            <dt className="text-xs text-muted-foreground sm:text-sm">Garantía</dt>
            <dd className="font-display text-intro leading-tight font-semibold sm:text-title-2">+{site.experienceYears} años</dd>
            <dd className="text-xs text-muted-foreground sm:text-sm">De experiencia real</dd>
          </div>
        </dl>
        <div className="mt-8 md:mt-12">
          <CategoryFilter value={category} onChange={selectCategory} allLabel="Todos los sistemas" label="Filtrar por línea" />
        </div>
      </Section>

      <section aria-label="Productos" className="page-container pb-12 md:pb-24">
        <Loadable
          state={products}
          loadingLabel="Cargando productos…"
          errorTitle="No pudimos cargar el catálogo"
          empty={(data) =>
            data.length === 0 && (
              <MessageState
                title="No hay productos en esta línea"
                description="Prueba con otra línea o mira todos los sistemas."
                action={
                  <Button variant="secondary" onClick={() => selectCategory(null)}>
                    Ver todos los sistemas
                  </Button>
                }
              />
            )
          }
        >
          {(data) => (
            <ul className="grid gap-4 md:grid-cols-2 md:gap-6">
              {data.map((product) => (
                <li key={product.id}>
                  <ProductCard product={product} onDetails={() => setSelected(product)} />
                </li>
              ))}
            </ul>
          )}
        </Loadable>
      </section>

      <QuoteCallout
        kicker="Fabricación especializada"
        title="¿Necesitas un sistema específico? Te asesoramos."
        text="Cuéntanos el espacio y las medidas, y te recomendamos el producto y calibre indicado."
      />

      <Dialog
        open={selected !== null}
        onOpenChange={(open) => !open && setSelected(null)}
        title={selected ? `${selected.title} · ${selected.subtitle}` : ''}
        description={selected ? categoryLabel(selected.category) : undefined}
      >
        {selected && <ProductDetail product={selected} />}
      </Dialog>
    </>
  )
}

function ProductCard({ product, onDetails }: { product: Product; onDetails: () => void }) {
  const site = useSite()
  return (
    <article className="flex h-full flex-col overflow-hidden rounded-[28px] bg-secondary">
      {product.imageUrl && (
        <img
          src={product.imageUrl}
          alt=""
          width={1200}
          height={750}
          loading="lazy"
          className="aspect-[16/10] w-full object-cover"
        />
      )}
      <div className="flex flex-1 flex-col p-5 md:p-7">
        <p className="flex flex-wrap items-center gap-x-3 text-xs font-semibold text-muted-foreground">
          <span>
            {product.categoryNumber ? `${product.categoryNumber} · ` : ''}
            {categoryLabel(product.category)}
          </span>
          {product.badge && <span className="text-status">{product.badge}</span>}
        </p>
        <h2 className="mt-2 font-display text-title-3 leading-tight font-semibold tracking-[-0.015em] md:text-title-2">{product.title}</h2>
        <p className="text-body text-muted-foreground">{product.subtitle}</p>
        <p className="mt-3">{product.description}</p>
        {product.specs.length > 0 && (
          <dl className="mt-5 flex flex-col gap-1.5 text-sm">
            {product.specs.slice(0, 3).map((spec) => (
              <div key={spec.label} className="flex gap-2">
                <dt className="font-semibold">{spec.label}:</dt>
                <dd className="text-muted-foreground">{spec.value}</dd>
              </div>
            ))}
          </dl>
        )}
        <div className="mt-auto flex flex-wrap items-center gap-3 pt-6">
          {site.quotingEnabled && (
            <Button asChild size="sm">
              <Link to={quoteHref(product)}>
                {product.ctaText ?? 'Cotizar este sistema'}
              </Link>
            </Button>
          )}
          <Button size="sm" variant="secondary" onClick={onDetails}>
            Detalles
          </Button>
          <Link to={galleryHref(product.category)} className="text-sm text-link hover:underline">
            Ver trabajos de esta línea
          </Link>
        </div>
      </div>
    </article>
  )
}

function ProductDetail({ product }: { product: Product }) {
  const site = useSite()
  return (
    <div className="flex flex-col gap-6">
      <p className="text-body">{product.description}</p>
      {product.imageUrl && (
        <img
          src={product.imageUrl}
          alt={product.title}
          width={1200}
          height={750}
          className="aspect-[16/10] w-full rounded-[20px] object-cover"
        />
      )}
      {product.specs.length > 0 && (
        <div>
          <h3 className="font-display text-xl font-semibold">Ficha técnica y características</h3>
          <dl className="mt-3 divide-y divide-border">
            {product.specs.map((spec) => (
              <div key={spec.label} className="grid grid-cols-[minmax(0,1fr)_minmax(0,2fr)] gap-4 py-3">
                <dt className="font-semibold">{spec.label}</dt>
                <dd className="text-muted-foreground">{spec.value}</dd>
              </div>
            ))}
          </dl>
        </div>
      )}
      {product.features.length > 0 && (
        <div>
          <h3 className="font-display text-xl font-semibold">Ventajas del sistema</h3>
          <ul className="mt-3 list-disc space-y-1.5 pl-5">
            {product.features.map((feature) => (
              <li key={feature}>{feature}</li>
            ))}
          </ul>
        </div>
      )}
      {site.quotingEnabled && (
        <Button asChild size="lg" className="self-start">
          <Link to={quoteHref(product)}>
            Solicitar cotización de este sistema
          </Link>
        </Button>
      )}
    </div>
  )
}
