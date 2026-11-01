import { lazy, Suspense } from 'react'
import { Route, Routes } from 'react-router'

import { PublicLayout } from '@/components/layout/PublicLayout'
import { LoadingState } from '@/components/ui/states'
import AboutPage from '@/pages/AboutPage'
import CatalogPage from '@/pages/CatalogPage'
import GalleryPage from '@/pages/GalleryPage'
import HomePage from '@/pages/HomePage'
import NotFoundPage from '@/pages/NotFoundPage'
import QuotePage from '@/pages/QuotePage'
import { SiteProvider } from '@/site/SiteProvider'

const AdminApp = lazy(() => import('@/pages/admin/AdminApp'))

/** Rutas del sitio público (comparten configuración y layout) y del panel, que se carga aparte. */
export default function AppRouter() {
  return (
    <Routes>
      <Route
        path="admin/*"
        element={
          <Suspense fallback={<LoadingState label="Cargando el panel…" className="justify-center" />}>
            <AdminApp />
          </Suspense>
        }
      />
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
