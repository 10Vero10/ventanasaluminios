import { describeDimensions, describeGlass, panesSuffix, suggestedPanes } from '@/lib/configurator'
import type { GlassColor, GlassOptions, GlassThickness, GlassType } from '@/types/catalog'

/** Lo que el visitante eligió en el configurador. Un código en null significa "la opción por defecto". */
export interface Configuration {
  widthCm: number
  heightCm: number
  /** Hojas elegidas a mano; null sigue la sugerencia por ancho. */
  panes: number | null
  typeCode: string | null
  colorCode: string | null
  thicknessMm: number | null
}

export const INITIAL_CONFIGURATION: Configuration = {
  widthCm: 150,
  heightCm: 120,
  panes: null,
  typeCode: null,
  colorCode: null,
  thicknessMm: null,
}

/** Configuración con cada opción ya resuelta contra la lista activa. */
export interface ResolvedConfiguration {
  widthCm: number
  heightCm: number
  panes: number
  type: GlassType
  color: GlassColor
  thickness: GlassThickness
}

function pick<T extends { defaultOption: boolean }>(items: T[], matches: (item: T) => boolean): T | undefined {
  return items.find(matches) ?? items.find((item) => item.defaultOption) ?? items[0]
}

/** Null si falta alguna lista de opciones (por ejemplo, todas desactivadas en el panel). */
export function resolveConfiguration(config: Configuration, options: GlassOptions): ResolvedConfiguration | null {
  const type = pick(options.types, (t) => t.code === config.typeCode)
  const color = pick(options.colors, (c) => c.code === config.colorCode)
  const thickness = pick(options.thicknesses, (t) => t.mm === config.thicknessMm)
  if (!type || !color || !thickness) return null
  return {
    widthCm: config.widthCm,
    heightCm: config.heightCm,
    panes: config.panes ?? suggestedPanes(config.widthCm),
    type,
    color,
    thickness,
  }
}

export function isDrawable(config: { widthCm: number; heightCm: number }): boolean {
  return config.widthCm > 0 && config.heightCm > 0
}

export function glassDescription(resolved: ResolvedConfiguration): string {
  return describeGlass(resolved.type.name, resolved.color.name, resolved.thickness.mm)
}

export function dimensionsText(resolved: ResolvedConfiguration): string {
  return describeDimensions(resolved.widthCm, resolved.heightCm, resolved.panes, glassDescription(resolved))
}

/** "Vidrio crudo · Transparente · 6 mm", más " · 3 hojas" si hay más de una (briefing §5.6.3). */
export function glassSummary(resolved: ResolvedConfiguration): string {
  const text = `Vidrio ${resolved.type.name.toLocaleLowerCase('es-CO')} · ${resolved.color.name} · ${resolved.thickness.mm} mm`
  return text + panesSuffix(resolved.panes)
}
