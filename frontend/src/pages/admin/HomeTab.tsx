import { FeaturedLinesPanel } from './home/FeaturedLinesPanel'
import { GalleryPanel } from './home/GalleryPanel'
import { HeroPanel } from './home/HeroPanel'

/** Pestaña Inicio (briefing §5.7.3): portada, galería y líneas destacadas. */
export default function HomeTab() {
  return (
    <div className="flex flex-col gap-6">
      <HeroPanel />
      <GalleryPanel />
      <FeaturedLinesPanel />
    </div>
  )
}
