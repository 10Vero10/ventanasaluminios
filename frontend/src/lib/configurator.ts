/**
 * Reglas del configurador de vidrio (briefing §3.5 y ADR-007). Son las mismas que GlassConfiguratorRules del backend,
 * que recalcula todo al recibir la cotización; aquí sirven para dibujar el plano y armar los textos al instante.
 * Ambos lados se prueban con los mismos casos.
 */

export const MAX_WIDTH_CM = 600
export const MAX_HEIGHT_CM = 260
export const MAX_PANE_WIDTH_CM = 150
export const MAX_PANES = 4
/** Lo que cambian los botones − y + del configurador. */
export const STEP_CM = 5

const SPANISH = 'es-CO'

/** Redondea al entero y limita a [0, máximo]; lo que no es número pasa a 0. */
export function clampCm(value: number, max: number): number {
  if (!Number.isFinite(value)) return 0
  return Math.min(max, Math.max(0, Math.round(value)))
}

/** Área en m² con dos decimales, redondeo hacia arriba en .5 como el backend. */
export function areaM2(widthCm: number, heightCm: number): number {
  return Math.round((widthCm * heightCm) / 100) / 100
}

export function formatArea(widthCm: number, heightCm: number): string {
  return areaM2(widthCm, heightCm).toFixed(2)
}

/** Hojas que propone el configurador: una por cada 150 cm de ancho, entre 1 y 4. */
export function suggestedPanes(widthCm: number): number {
  return Math.min(MAX_PANES, Math.max(1, Math.ceil(widthCm / MAX_PANE_WIDTH_CM)))
}

/** "Vidrio laminado templado bronce 8 mm". */
export function describeGlass(typeName: string, colorName: string, thicknessMm: number): string {
  return `Vidrio ${typeName.toLocaleLowerCase(SPANISH)} ${colorName.toLocaleLowerCase(SPANISH)} ${thicknessMm} mm`
}

/** " · 3 hojas" cuando hay más de una; vacío con una sola. */
export function panesSuffix(panes: number): string {
  return panes > 1 ? ` · ${panes} hojas` : ''
}

/** "120 cm alto x 150 cm ancho (1.80 m²) · Vidrio crudo transparente 6 mm", más " · 3 hojas" si hay más de una. */
export function describeDimensions(widthCm: number, heightCm: number, panes: number, glass: string): string {
  const text = `${heightCm} cm alto x ${widthCm} cm ancho (${formatArea(widthCm, heightCm)} m²) · ${glass}`
  return text + panesSuffix(panes)
}

/** Mensaje sugerido para el formulario. */
export function buildAutoMessage(productType: string, widthCm: number, heightCm: number, glass: string): string {
  const glassText = glass.charAt(0).toLocaleLowerCase(SPANISH) + glass.slice(1)
  return `Hola, me interesa una cotización para ${productType} de ${widthCm} cm x ${heightCm} cm en ${glassText}.`
}

/**
 * Pone el mensaje sugerido sin perder lo que escribió el cliente: llena un mensaje vacío, reemplaza el sugerido
 * anterior, no duplica el nuevo y, si no, lo agrega al inicio.
 */
export function mergeAutoMessage(current: string, previousAuto: string | null, nextAuto: string): string {
  if (!current.trim()) return nextAuto
  if (previousAuto && current.includes(previousAuto)) return current.replace(previousAuto, nextAuto)
  if (current.includes(nextAuto)) return current
  return `${nextAuto}\n${current}`
}

/** Píxeles por centímetro para que la medida máxima (600 × 260 cm) quepa, con la misma escala en ancho y alto. */
export function pxPerCm(availableWidthPx: number, maxHeightPx: number): number {
  return Math.max(0, Math.min(availableWidthPx / MAX_WIDTH_CM, maxHeightPx / MAX_HEIGHT_CM))
}

const CM_PER_CSS_PX = 2.54 / 96

/** Escala aproximada del dibujo en pantalla (1:N), redondeada a múltiplos de 5. */
export function drawingScale(ppc: number): number {
  if (ppc <= 0) return 0
  return Math.max(5, Math.round(1 / (ppc * CM_PER_CSS_PX) / 5) * 5)
}
