import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { changeQuoteStatus, getQuotes } from '@/services/api/adminQuoteService'
import { getDashboard } from '@/services/api/adminSiteService'
import { changePassword, getCurrentAdmin, login, logout } from '@/services/api/authService'
import { GLASS_OPTIONS } from '@/test/fixtures'
import type { AdminUser } from '@/types/auth'
import type { Quote } from '@/types/quote'

import AdminApp from './AdminApp'

vi.mock('@/services/api/authService', () => ({
  getCurrentAdmin: vi.fn(),
  login: vi.fn(),
  logout: vi.fn(),
  changePassword: vi.fn(),
}))
vi.mock('@/services/api/adminSiteService', () => ({ getDashboard: vi.fn() }))
vi.mock('@/services/api/adminCatalogService', () => ({ getAdminGlassOptions: vi.fn(() => Promise.resolve(GLASS_OPTIONS)) }))
vi.mock('@/services/api/adminQuoteService', () => ({
  getQuotes: vi.fn(),
  getQuote: vi.fn(),
  changeQuoteStatus: vi.fn(),
  deleteQuote: vi.fn(),
}))

const ADMIN: AdminUser = { id: 1, email: 'admin@ejemplo.com', role: 'ADMIN', mustChangePassword: false, lastLoginAt: null }

const QUOTE: Quote = {
  id: 'q-1',
  createdAt: '2026-11-01T12:00:00Z',
  updatedAt: '2026-11-01T12:00:00Z',
  status: 'NUEVA',
  name: '<b>Carlos</b>',
  phone: '3001234567',
  productType: 'Ventanas arquitectónicas',
  dimensionsText: '120 cm alto x 150 cm ancho (1.80 m²) · Vidrio crudo bronce 6 mm',
  message: null,
  widthCm: 150,
  heightCm: 120,
  panes: 1,
  areaM2: 1.8,
  glassTypeCode: 'CRUDO',
  glassColorCode: 'BRONCE',
  thicknessMm: 6,
}

function renderAdmin(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/admin/*" element={<AdminApp />} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('Panel de administración', () => {
  beforeEach(() => {
    vi.mocked(getDashboard).mockResolvedValue({ products: 8, galleryImages: 5, newQuotes: 1 })
    vi.mocked(getQuotes).mockResolvedValue({ content: [QUOTE], page: 0, size: 20, totalElements: 1, totalPages: 1 })
  })

  it('sin sesión lleva al login y muestra el error del servidor', async () => {
    const user = userEvent.setup()
    vi.mocked(getCurrentAdmin).mockRejectedValue(new Error('401'))
    vi.mocked(login).mockRejectedValue(new Error('x'))
    renderAdmin('/admin/cotizaciones')

    await user.type(await screen.findByLabelText('Correo'), 'admin@ejemplo.com')
    await user.type(screen.getByLabelText('Contraseña'), 'mala')
    await user.click(screen.getByRole('button', { name: 'Entrar al panel' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Ocurrió un error inesperado')
  })

  it('con la contraseña temporal obliga a cambiarla antes de entrar', async () => {
    const user = userEvent.setup()
    vi.mocked(getCurrentAdmin).mockRejectedValue(new Error('401'))
    vi.mocked(login).mockResolvedValue({ ...ADMIN, mustChangePassword: true })
    vi.mocked(changePassword).mockResolvedValue(ADMIN)
    renderAdmin('/admin')

    await user.type(await screen.findByLabelText('Correo'), 'admin@ejemplo.com')
    await user.type(screen.getByLabelText('Contraseña'), 'Temporal-2026')
    await user.click(screen.getByRole('button', { name: 'Entrar al panel' }))
    expect(await screen.findByRole('heading', { name: 'Crea tu contraseña' })).toBeInTheDocument()

    await user.type(screen.getByLabelText('Contraseña temporal'), 'Temporal-2026')
    await user.type(screen.getByLabelText('Nueva contraseña'), 'corta')
    await user.click(screen.getByRole('button', { name: 'Guardar contraseña' }))
    expect(screen.getByRole('alert')).toHaveTextContent('al menos 12 caracteres')

    await user.clear(screen.getByLabelText('Nueva contraseña'))
    await user.type(screen.getByLabelText('Nueva contraseña'), 'Nueva-Clave-2026')
    await user.type(screen.getByLabelText('Confirmar nueva contraseña'), 'Nueva-Clave-2026')
    await user.click(screen.getByRole('button', { name: 'Guardar contraseña' }))
    expect(changePassword).toHaveBeenCalledWith('Temporal-2026', 'Nueva-Clave-2026')
    expect(await screen.findByRole('heading', { name: 'Cotizaciones' })).toBeInTheDocument()
  })

  it('muestra la bandeja como texto y cambia el estado', async () => {
    const user = userEvent.setup()
    vi.mocked(getCurrentAdmin).mockResolvedValue(ADMIN)
    vi.mocked(changeQuoteStatus).mockResolvedValue({ ...QUOTE, status: 'CONTACTADA', updatedAt: '2026-11-01T13:00:00Z' })
    renderAdmin('/admin')

    const card = (await screen.findByText('<b>Carlos</b>')).closest('article')!
    expect(within(card).getByText('Crudo · Bronce · 6 mm')).toBeInTheDocument()
    expect(within(card).getByText('Sin dato')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Cotizaciones (1)' })).toBeInTheDocument()

    await user.selectOptions(within(card).getByLabelText('Estado'), 'CONTACTADA')
    expect(changeQuoteStatus).toHaveBeenCalledWith('q-1', 'CONTACTADA')
    expect(getDashboard).toHaveBeenCalledTimes(2)
  })

  it('si cerrar sesión falla lo avisa y no aparenta haberla cerrado', async () => {
    const user = userEvent.setup()
    vi.mocked(getCurrentAdmin).mockResolvedValue(ADMIN)
    vi.mocked(logout).mockRejectedValue(new Error('sin red'))
    renderAdmin('/admin')

    await user.click(await screen.findByRole('button', { name: 'Cerrar sesión' }))
    expect(await screen.findByText(/No se pudo cerrar la sesión/)).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Cotizaciones' })).toBeInTheDocument()
  })
})
