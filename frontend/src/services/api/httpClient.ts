import axios from 'axios'

import type { ApiError } from '@/types/api'

/**
 * Cliente HTTP único: las páginas nunca llaman a axios directo, siempre a los servicios de esta carpeta.
 * La sesión viaja en cookie httpOnly del mismo dominio; axios reenvía solo la cookie XSRF-TOKEN de Spring
 * en el encabezado X-XSRF-TOKEN.
 */
export const httpClient = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15_000,
})

/** Mensaje legible de un error de la API; si el backend no respondió, un mensaje genérico en español. */
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ApiError>(error)) {
    const message = error.response?.data?.error
    if (message) {
      return message
    }
    if (!error.response) {
      return 'No hay conexión con el servidor. Revisa tu internet e intenta de nuevo.'
    }
  }
  return 'Ocurrió un error inesperado. Intenta de nuevo.'
}
