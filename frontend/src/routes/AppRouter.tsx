import { Route, Routes } from 'react-router'

import ConstructionPage from '@/pages/ConstructionPage'

/** Rutas de la aplicación. Las rutas reales (/, /catalogo, /trabajos, /nosotros, /cotizar, /admin/...) llegan en el paso 5. */
export default function AppRouter() {
  return (
    <Routes>
      <Route path="*" element={<ConstructionPage />} />
    </Routes>
  )
}
