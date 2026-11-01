import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { getAdminGlassOptions, saveGlassType } from '@/services/api/adminCatalogService'
import { GLASS_OPTIONS } from '@/test/fixtures'

import GlassTab from './GlassTab'

vi.mock('@/services/api/adminCatalogService', () => ({
  getAdminGlassOptions: vi.fn(),
  reorderGlass: vi.fn(),
  saveGlassType: vi.fn(),
  saveGlassColor: vi.fn(),
  saveGlassThickness: vi.fn(),
}))

describe('GlassTab', () => {
  beforeEach(() => {
    vi.mocked(getAdminGlassOptions).mockResolvedValue(GLASS_OPTIONS)
  })

  it('al guardar una fila muestra la confirmación y no borra lo escrito en las demás', async () => {
    const user = userEvent.setup()
    vi.mocked(saveGlassType).mockImplementation(async (_id, request) => ({ ...GLASS_OPTIONS.types[0], ...request }))
    render(<GlassTab />)

    const crudo = await screen.findByLabelText('Nombre (CRUDO)')
    const laminado = screen.getByLabelText('Nombre (LAMINADO_TEMPLADO)')
    await user.type(laminado, ' 2')
    await user.type(crudo, ' claro')
    vi.mocked(getAdminGlassOptions).mockResolvedValue({
      ...GLASS_OPTIONS,
      types: [{ ...GLASS_OPTIONS.types[0], name: 'Crudo claro' }, GLASS_OPTIONS.types[1]],
    })
    await user.click(screen.getAllByRole('button', { name: 'Guardar' })[0])

    expect(await screen.findByText('¡Guardado con éxito!')).toBeInTheDocument()
    expect(saveGlassType).toHaveBeenCalledWith(1, { code: 'CRUDO', name: 'Crudo claro', active: true, defaultOption: true })
    expect(screen.getByLabelText('Nombre (LAMINADO_TEMPLADO)')).toHaveValue('Laminado templado 2')
  })
})
