import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'

import { ImagePicker } from './ImagePicker'

vi.mock('@/services/api/adminSiteService', () => ({ getAdminGallery: vi.fn(), uploadImage: vi.fn() }))

describe('ImagePicker', () => {
  it('las rutas del sitio (/media/…, /favicon.svg) no bloquean el envío del formulario', () => {
    render(
      <form data-testid="form">
        <ImagePicker label="Imagen" value="/media/a.webp" onChange={() => {}} />
        <ImagePicker label="Icono" value="/favicon.svg" onChange={() => {}} />
      </form>,
    )
    expect((screen.getByTestId('form') as HTMLFormElement).checkValidity()).toBe(true)
  })
})
