import { Navigate, Route, Routes } from 'react-router'

import { AuthProvider } from '@/auth/AuthProvider'
import { AdminLayout } from '@/components/admin/AdminLayout'
import { RequireAdmin } from '@/routes/RequireAdmin'

import AboutTab from './AboutTab'
import CatalogTab from './CatalogTab'
import ChangePasswordPage from './ChangePasswordPage'
import GlassTab from './GlassTab'
import HomeTab from './HomeTab'
import LoginPage from './LoginPage'
import QuotesTab from './QuotesTab'
import SettingsTab from './SettingsTab'

/** Panel de administración. Se carga aparte del sitio público para que las visitas no descarguen su código. */
export default function AdminApp() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="login" element={<LoginPage />} />
        <Route
          path="cambiar-clave"
          element={
            <RequireAdmin allowPasswordChange>
              <ChangePasswordPage />
            </RequireAdmin>
          }
        />
        <Route
          element={
            <RequireAdmin>
              <AdminLayout />
            </RequireAdmin>
          }
        >
          <Route index element={<Navigate to="cotizaciones" replace />} />
          <Route path="cotizaciones" element={<QuotesTab />} />
          <Route path="inicio" element={<HomeTab />} />
          <Route path="catalogo" element={<CatalogTab />} />
          <Route path="vidrio" element={<GlassTab />} />
          <Route path="nosotros" element={<AboutTab />} />
          <Route path="configuracion" element={<SettingsTab />} />
          <Route path="*" element={<Navigate to="cotizaciones" replace />} />
        </Route>
      </Routes>
    </AuthProvider>
  )
}
