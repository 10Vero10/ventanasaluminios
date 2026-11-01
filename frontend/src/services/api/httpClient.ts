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
