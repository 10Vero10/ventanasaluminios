import { Route, Routes } from 'react-router'

import { PublicLayout } from '@/components/layout/PublicLayout'
import AboutPage from '@/pages/AboutPage'
import CatalogPage from '@/pages/CatalogPage'
import GalleryPage from '@/pages/GalleryPage'
import HomePage from '@/pages/HomePage'
import NotFoundPage from '@/pages/NotFoundPage'
import QuotePage from '@/pages/QuotePage'
import { SiteProvider } from '@/site/SiteProvider'

/** Rutas del sitio público. Todas comparten la configuración del sitio y el layout. */
export default function AppRouter() {
  return (
    <Routes>
      <Route
        element={
          <SiteProvider>
            <PublicLayout />
          </SiteProvider>
        }
      >
        <Route index element={<HomePage />} />
        <Route path="catalogo" element={<CatalogPage />} />
        <Route path="trabajos" element={<GalleryPage />} />
        <Route path="nosotros" element={<AboutPage />} />
        <Route path="cotizar" element={<QuotePage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
