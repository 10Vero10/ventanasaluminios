import { Link } from 'react-router'

import { Section } from '@/components/site/Section'
import { Button } from '@/components/ui/button'
import { ROUTES } from '@/lib/routes'

export default function NotFoundPage() {
  return (
    <Section className="text-center">
      <p className="text-[17px] font-semibold text-status">Error 404</p>
      <h1 className="mt-2 font-display text-[40px] leading-tight font-semibold tracking-[-0.02em]">
        Esta página no existe
      </h1>
      <p className="mx-auto mt-4 max-w-md text-[17px] text-muted-foreground">
        Puede que el enlace esté mal escrito o que la página se haya movido.
      </p>
      <div className="mt-8 flex flex-wrap justify-center gap-3">
        <Button asChild>
          <Link to="/">Ir al inicio</Link>
        </Button>
        <Button asChild variant="secondary">
          <Link to={ROUTES.catalog}>Ver catálogo</Link>
        </Button>
      </div>
    </Section>
  )
}
