import { Link } from 'react-router'

import { mailHref, phoneHref } from '@/lib/contact'
import { useSite } from '@/site/siteContext'
import { ROUTES } from '@/lib/routes'

import { BrandMark } from './BrandMark'
import { visibleNav } from './navigation'

/** Pie de página (briefing §5.2.4) en la banda gris de apple-style-reference. */
export function SiteFooter() {
  const site = useSite()
  return (
    <footer className="bg-secondary pb-24 md:pb-0">
      <div className="mx-auto grid max-w-[1080px] gap-10 px-6 py-14 text-sm md:grid-cols-[2fr_1fr_1.5fr]">
        <div>
          <p className="flex items-center gap-2.5 font-display text-[17px] font-semibold">
            <BrandMark className="size-7" />
            Ventanas y Aluminios
          </p>
          <p className="mt-4 max-w-sm text-muted-foreground">
            Soluciones en vidrio, aluminio y otros materiales, fabricadas e instaladas con más de {site.experienceYears}{' '}
            años de experiencia. Garantía estructural y cortes milimétricos.
          </p>
        </div>
        <div>
          <p className="font-semibold">Secciones</p>
          <ul className="mt-3 flex flex-col gap-2">
            {visibleNav(site.quotingEnabled).map((item) => (
              <li key={item.to}>
                <Link to={item.to} className="text-muted-foreground hover:text-foreground">
                  {item.to === ROUTES.quote ? 'Configurador y cotización' : item.label}
                </Link>
              </li>
            ))}
          </ul>
        </div>
        <div>
          <p className="font-semibold">Taller y atención</p>
          <ul className="mt-3 flex flex-col gap-2 text-muted-foreground">
            <li>
              <a href={mailHref(site.email)} className="hover:text-foreground">
                {site.email}
              </a>
            </li>
            <li>
              <a href={phoneHref(site.phone)} className="hover:text-foreground">
                {site.phone}
              </a>
            </li>
            <li>Instalaciones en toda el área metropolitana y proyectos a nivel regional.</li>
          </ul>
        </div>
      </div>
      <div className="border-t border-border">
        <p className="mx-auto max-w-[1080px] px-5 py-5 text-xs sm:px-6 text-muted-foreground">
          © {new Date().getFullYear()} Ventanas y Aluminios. Fabricación e instalación a medida. Todos los derechos
          reservados.
        </p>
      </div>
    </footer>
  )
}
