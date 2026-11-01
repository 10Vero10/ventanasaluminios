import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { describe, expect, it } from 'vitest'

import AppRouter from './AppRouter'

describe('AppRouter', () => {
  it('muestra la página provisional en cualquier ruta', () => {
    render(
      <MemoryRouter initialEntries={['/catalogo']}>
        <AppRouter />
      </MemoryRouter>,
    )
    expect(screen.getByRole('heading', { level: 1, name: 'Ventanas y Aluminios SAS' })).toBeInTheDocument()
  })
})
