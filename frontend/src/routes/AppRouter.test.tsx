import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { getSite } from '@/services/api/siteService'
import { SITE } from '@/test/fixtures'

import AppRouter from './AppRouter'

vi.mock('@/services/api/siteService', () => ({ getSite: vi.fn(), getGallery: vi.fn().mockResolvedValue([]) }))
vi.mock('@/services/api/catalogService', () => ({
  getProducts: vi.fn().mockResolvedValue([]),
  getGlassOptions: vi.fn().mockResolvedValue({ types: [], colors: [], thicknesses: [] }),
}))

function renderAt(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <AppRouter />
    </MemoryRouter>,
  )
}

describe('AppRouter', () => {
  beforeEach(() => {
    vi.mocked(getSite).mockResolvedValue(SITE)
  })

  it('muestra en la portada el título editable del sitio y lo pone en la pestaña', async () => {
    renderAt('/')
    expect(await screen.findByRole('heading', { level: 1, name: 'Ventanas y Aluminios' })).toBeInTheDocument()
    expect(document.title).toBe('Ventanas y Aluminios | Fabricación e Instalación a Medida')
  })

  it('usa los años de experiencia de la configuración', async () => {
    vi.mocked(getSite).mockResolvedValue({ ...SITE, experienceYears: 30 })
    renderAt('/nosotros')
    expect(await screen.findByRole('heading', { name: 'Más de 30 años fortaleciendo procesos' })).toBeInTheDocument()
  })

  it('con la cotización apagada /cotizar vuelve al inicio y no hay botones de cotizar', async () => {
    vi.mocked(getSite).mockResolvedValue({ ...SITE, quotingEnabled: false })
    renderAt('/cotizar')
    expect(await screen.findByRole('heading', { level: 1, name: 'Ventanas y Aluminios' })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /cotizar/i })).not.toBeInTheDocument()
  })

  it('una ruta desconocida muestra la página 404', async () => {
    renderAt('/no-existe')
    expect(await screen.findByRole('heading', { name: 'Esta página no existe' })).toBeInTheDocument()
  })

  it('si la API falla muestra el error con reintento', async () => {
    vi.mocked(getSite).mockRejectedValue(new Error('caída'))
    renderAt('/')
    expect(await screen.findByText('No pudimos cargar el sitio')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Intentar de nuevo' })).toBeInTheDocument()
  })
})
