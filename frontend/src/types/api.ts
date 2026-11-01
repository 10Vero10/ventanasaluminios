/** Formato uniforme de error del backend: `{ "error": "mensaje en español", "code": "CODIGO" }`. */
export interface ApiError {
  error: string
  code: string
}
