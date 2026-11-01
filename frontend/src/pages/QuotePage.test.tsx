import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { getGlassOptions } from '@/services/api/catalogService'
import { createQuote } from '@/services/api/quoteService'
import { SiteContext } from '@/site/siteContext'
import { GLASS_OPTIONS, SITE } from '@/test/fixtures'

import QuotePage from './QuotePage'

vi.mock('@/services/api/catalogService', () => ({ getGlassOptions: vi.fn() }))
vi.mock('@/services/api/quoteService', () => ({ createQuote: vi.fn() }))

function renderPage(path = '/cotizar') {
  return render(
    <SiteContext.Provider value={SITE}>
      <MemoryRouter initialEntries={[path]}>
        <QuotePage />
      </MemoryRouter>
    </SiteContext.Provider>,
  )
}

describe('QuotePage', () => {
  beforeEach(() => {
    vi.mocked(getGlassOptions).mockResolvedValue(GLASS_OPTIONS)
    vi.mocked(createQuote).mockReset()
  })

  it('aplica las medidas al formulario y las mantiene sincronizadas', async () => {
    const user = userEvent.setup()
    renderPage()
    await user.click(await screen.findByRole('button', { name: 'Aplicar medidas a mi cotización' }))

    expect(screen.getByText('✓ Medidas aplicadas al formulario de cotización.')).toBeInTheDocument()
    expect(screen.getByLabelText('Medidas aproximadas (alto x ancho en cm)')).toHaveValue(
      '120 cm alto x 150 cm ancho (1.80 m²) · Vidrio crudo transparente 6 mm',
    )
    const message = screen.getByLabelText('Mensaje o detalles del proyecto')
    expect(message).toHaveValue(
      'Hola, me interesa una cotización para Ventanas arquitectónicas de 150 cm x 120 cm en vidrio crudo transparente 6 mm.',
    )

    await user.click(screen.getByRole('radio', { name: 'Bronce' }))
    await user.click(screen.getByRole('button', { name: 'Aumentar ancho' }))
    expect(screen.getByLabelText('Medidas aproximadas (alto x ancho en cm)')).toHaveValue(
      '120 cm alto x 155 cm ancho (1.86 m²) · Vidrio crudo bronce 6 mm · 2 hojas',
    )
    expect(message).toHaveValue(
      'Hola, me interesa una cotización para Ventanas arquitectónicas de 155 cm x 120 cm en vidrio crudo bronce 6 mm.',
    )
  })

  it('conserva lo que escribió el cliente al cambiar el tipo de producto', async () => {
    const user = userEvent.setup()
    renderPage()
    const message = screen.getByLabelText('Mensaje o detalles del proyecto')
    await user.type(message, 'Es para un segundo piso.')
    await user.click(await screen.findByRole('button', { name: 'Aplicar medidas a mi cotización' }))
    await user.selectOptions(screen.getByLabelText('Tipo de producto'), 'Portones tipo americano')

    expect(message).toHaveValue(
      'Hola, me interesa una cotización para Portones tipo americano de 150 cm x 120 cm en vidrio crudo transparente 6 mm.\nEs para un segundo piso.',
    )
  })

  it('con una medida en 0 no aplica y lo explica', async () => {
    const user = userEvent.setup()
    renderPage()
    const width = await screen.findByLabelText('ancho en centímetros')
    await user.clear(width)
    await user.click(screen.getByRole('button', { name: 'Aplicar medidas a mi cotización' }))

    expect(screen.getByText('El ancho y el alto deben ser mayores a 0 cm para aplicarlos.')).toBeInTheDocument()
    expect(screen.getByText('Ajusta el ancho y el alto (mayores a 0 cm) para ver el plano.')).toBeInTheDocument()
  })

  it('precarga el producto elegido en el catálogo', async () => {
    renderPage('/cotizar?producto=Corredizas%20(Ventanas)')
    expect(await screen.findByLabelText('Tipo de producto')).toHaveValue('Corredizas (Ventanas)')
  })

  it('pide nombre y teléfono antes de enviar', async () => {
    const user = userEvent.setup()
    renderPage()
    await user.click(screen.getByRole('button', { name: 'Enviar cotización' }))

    expect(screen.getByText('Por favor ingresa tu nombre y teléfono para recibir la cotización.')).toBeInTheDocument()
    expect(createQuote).not.toHaveBeenCalled()
  })

  it('envía los datos del configurador y muestra la confirmación', async () => {
    const user = userEvent.setup()
    vi.mocked(createQuote).mockResolvedValue('id-1')
    renderPage()
    await user.click(await screen.findByRole('button', { name: 'Aplicar medidas a mi cotización' }))
    await user.type(screen.getByLabelText(/Nombre y apellido/), '  Carlos Mendoza ')
    await user.type(screen.getByLabelText(/Teléfono/), '3001234567')
    await user.click(screen.getByRole('button', { name: 'Enviar cotización' }))

    expect(await screen.findByText('¡Cotización enviada!')).toBeInTheDocument()
    expect(createQuote).toHaveBeenCalledWith(
      expect.objectContaining({
        name: 'Carlos Mendoza',
        phone: '3001234567',
        productType: 'Ventanas arquitectónicas',
        widthCm: 150,
        heightCm: 120,
        panes: 1,
        glassTypeCode: 'CRUDO',
        glassColorCode: 'TRANSPARENTE',
        thicknessMm: 6,
        website: '',
      }),
    )
    await user.click(screen.getByRole('button', { name: 'Enviar otra cotización' }))
    expect(screen.getByLabelText(/Nombre y apellido/)).toHaveValue('')
  })

  it('si el envío falla muestra el error del servidor con el teléfono y el correo', async () => {
    const user = userEvent.setup()
    vi.mocked(createQuote).mockRejectedValue(new Error('x'))
    renderPage()
    await user.type(screen.getByLabelText(/Nombre y apellido/), 'Ana')
    await user.type(screen.getByLabelText(/Teléfono/), '300')
    await user.click(screen.getByRole('button', { name: 'Enviar cotización' }))

    const alert = await screen.findByText(/También puedes llamarnos al/)
    await waitFor(() => expect(alert).toHaveTextContent(SITE.phone))
    expect(alert).toHaveTextContent(SITE.email)
  })
})

describe('QuotePage, casos límite', () => {
  beforeEach(() => {
    vi.mocked(getGlassOptions).mockResolvedValue(GLASS_OPTIONS)
    vi.mocked(createQuote).mockReset()
  })

  it('con una medida en 0 después de aplicar, el tipo de producto sí cambia y se conservan las medidas', async () => {
    const user = userEvent.setup()
    renderPage()
    await user.click(await screen.findByRole('button', { name: 'Aplicar medidas a mi cotización' }))
    await user.clear(screen.getByLabelText('ancho en centímetros'))
    await user.selectOptions(screen.getByLabelText('Tipo de producto'), 'Portones tipo americano')

    expect(screen.getByLabelText('Tipo de producto')).toHaveValue('Portones tipo americano')
    expect(screen.getByLabelText('Medidas aproximadas (alto x ancho en cm)')).toHaveValue(
      '120 cm alto x 150 cm ancho (1.80 m²) · Vidrio crudo transparente 6 mm',
    )
  })

  it('si el cliente escribe las medidas a mano, se envían sus medidas y no las del configurador', async () => {
    const user = userEvent.setup()
    vi.mocked(createQuote).mockResolvedValue('id-1')
    renderPage()
    await user.click(await screen.findByRole('button', { name: 'Aplicar medidas a mi cotización' }))
    const dimensions = screen.getByLabelText('Medidas aproximadas (alto x ancho en cm)')
    await user.clear(dimensions)
    await user.type(dimensions, 'Dos ventanas de 120 x 150')
    await user.type(screen.getByLabelText(/Nombre y apellido/), 'Ana')
    await user.type(screen.getByLabelText(/Teléfono/), '300')
    await user.click(screen.getByRole('button', { name: 'Enviar cotización' }))

    const request = vi.mocked(createQuote).mock.calls[0][0]
    expect(request.dimensionsText).toBe('Dos ventanas de 120 x 150')
    expect(request.widthCm).toBeUndefined()
  })

  it('el mensaje con la sugerencia no pasa de 2000 caracteres', async () => {
    const user = userEvent.setup()
    renderPage()
    const message = screen.getByLabelText('Mensaje o detalles del proyecto')
    await user.click(message)
    await user.paste('x'.repeat(1990))
    await user.click(await screen.findByRole('button', { name: 'Aplicar medidas a mi cotización' }))

    expect((message as HTMLTextAreaElement).value.length).toBe(2000)
    expect((message as HTMLTextAreaElement).value.startsWith('Hola, me interesa')).toBe(true)
  })
})
