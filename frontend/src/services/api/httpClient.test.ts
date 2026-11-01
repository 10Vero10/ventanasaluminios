import { AxiosError, AxiosHeaders } from 'axios'
import { describe, expect, it } from 'vitest'

import { getErrorMessage } from './httpClient'

function apiError(status: number, data: unknown): AxiosError {
  const config = { headers: new AxiosHeaders() }
  return new AxiosError('fallo', 'ERR_BAD_REQUEST', config, null, {
    status,
    statusText: '',
    headers: {},
    config,
    data,
  })
}

describe('getErrorMessage', () => {
  it('usa el mensaje del backend cuando viene en el formato uniforme', () => {
    const error = apiError(400, { error: 'Nombre y teléfono son obligatorios.', code: 'VALIDATION_ERROR' })
    expect(getErrorMessage(error)).toBe('Nombre y teléfono son obligatorios.')
  })

  it('avisa de falta de conexión cuando no hubo respuesta', () => {
    const error = new AxiosError('Network Error', 'ERR_NETWORK')
    expect(getErrorMessage(error)).toBe('No hay conexión con el servidor. Revisa tu internet e intenta de nuevo.')
  })

  it('nunca expone mensajes técnicos en inglés', () => {
    expect(getErrorMessage(new TypeError("Cannot read properties of null (reading 'quotes')"))).toBe(
      'Ocurrió un error inesperado. Intenta de nuevo.',
    )
  })
})
