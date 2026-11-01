/** Formato de código que acepta el backend para tipos y colores de vidrio. */
export const GLASS_CODE_PATTERN = /^[A-Z0-9_]{2,30}$/

/** Propone el código a partir del nombre: "Laminado templado" → "LAMINADO_TEMPLADO". */
export function glassCodeFrom(name: string): string {
  return name
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toUpperCase()
    .replace(/[^A-Z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .slice(0, 30)
}
