import axios from 'axios'

import type { ApiError } from '@/types/api'

/**
 * Cliente HTTP único: las páginas nunca llaman a axios directo, siempre a los servicios de esta carpeta.
 * La sesión viaja en cookie httpOnly del mismo dominio; axios reenvía solo la cookie XSRF-TOKEN de Spring
 * en el encabezado X-XSRF-TOKEN.
 */
/**
 * Tiempo para la primera solicitud de una visita: en el plan gratis de Render el servidor se duerme y tarda cerca de
 * un minuto en despertar. Las demás solicitudes usan el tiempo normal.
 */
export const WAKE_UP_TIMEOUT_MS = 120_000

export const httpClient = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15_000,
})

/** Mensaje cuando lo que responde no es la API (por ejemplo, el hosting devuelve la página en vez del backend). */
const API_UNAVAILABLE_MESSAGE = 'El servidor del sitio no está disponible en este momento. Intenta de nuevo más tarde.'

/** El servidor respondió, pero no con JSON de la API. */
export class ApiUnavailableError extends Error {
  constructor() {
    super(API_UNAVAILABLE_MESSAGE)
    this.name = 'ApiUnavailableError'
  }
}

/** Una respuesta con cuerpo debe ser JSON; un 204 o un cuerpo vacío no lo necesitan. */
export function isApiResponse(contentType: unknown, data: unknown): boolean {
  const empty = data === undefined || data === null || data === ''
  return empty || (typeof contentType === 'string' && contentType.includes('json'))
}

httpClient.interceptors.response.use((response) => {
  if (!isApiResponse(response.headers['content-type'], response.data)) throw new ApiUnavailableError()
  return response
})

type SessionListener = () => void
const sessionListeners = new Set<SessionListener>()

/** Avisa cuando el backend rechaza la sesión (401) en una ruta del panel; devuelve la función para dejar de escuchar. */
export function onSessionExpired(listener: SessionListener): () => void {
  sessionListeners.add(listener)
  return () => sessionListeners.delete(listener)
}

httpClient.interceptors.response.use(undefined, (error: unknown) => {
  if (axios.isAxiosError(error) && error.response?.status === 401) {
    const url = error.config?.url ?? ''
    // El login y la consulta inicial de sesión responden 401 como parte normal del flujo.
    if (url.startsWith('/admin') || url === '/auth/change-password' || url === '/auth/logout') {
      sessionListeners.forEach((listener) => listener())
    }
  }
  return Promise.reject(error)
})

/** Si el error es una respuesta HTTP con ese estado. */
export function isHttpStatus(error: unknown, status: number): boolean {
  return axios.isAxiosError(error) && error.response?.status === status
}

/** Mensaje legible de un error de la API; si el backend no respondió, un mensaje genérico en español. */
export function getErrorMessage(error: unknown): string {
  if (error instanceof ApiUnavailableError) return error.message
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
