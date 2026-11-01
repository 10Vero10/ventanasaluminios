import { Link } from 'react-router'

import { Button } from '@/components/ui/button'
import { useSite } from '@/site/siteContext'
import { ROUTES } from '@/lib/routes'

import { Kicker, Section, SectionTitle } from './Section'

/** Llamado final a cotizar; desaparece con la cotización apagada (briefing §3.1.1). */
export function QuoteCallout({ kicker, title, text, note }: { kicker: string; title: string; text: string; note?: string }) {
  const site = useSite()
  if (!site.quotingEnabled) return null
  return (
    <Section labelledBy="llamado-cotizar" className="text-center">
      <Kicker>{kicker}</Kicker>
      <SectionTitle id="llamado-cotizar" className="mx-auto max-w-2xl">
        {title}
      </SectionTitle>
      <p className="mx-auto mt-4 max-w-xl text-[17px] text-muted-foreground">{text}</p>
      <Button asChild size="lg" className="mt-8">
        <Link to={ROUTES.quote}>Cotizar ahora</Link>
      </Button>
      {note && <p className="mt-4 text-sm text-muted-foreground">{note}</p>}
    </Section>
  )
}
