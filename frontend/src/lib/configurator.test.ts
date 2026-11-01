import { describe, expect, it } from 'vitest'

import {
  buildAutoMessage,
  clampCm,
  describeDimensions,
  describeGlass,
  drawingScale,
  formatArea,
  MAX_HEIGHT_CM,
  MAX_WIDTH_CM,
  mergeAutoMessage,
  pxPerCm,
  suggestedPanes,
} from './configurator'

// Mismos casos que GlassConfiguratorRulesTest en el backend.
describe('medidas', () => {
  it('limita los valores a los rangos 0–600 y 0–260', () => {
    expect(clampCm(-5, MAX_WIDTH_CM)).toBe(0)
    expect(clampCm(700, MAX_WIDTH_CM)).toBe(600)
    expect(clampCm(300, MAX_HEIGHT_CM)).toBe(260)
    expect(clampCm(123.6, MAX_HEIGHT_CM)).toBe(124)
    expect(clampCm(Number.NaN, MAX_WIDTH_CM)).toBe(0)
  })

  it('calcula el área con dos decimales', () => {
    expect(formatArea(150, 120)).toBe('1.80')
    expect(formatArea(600, 260)).toBe('15.60')
    expect(formatArea(33, 33)).toBe('0.11')
    expect(formatArea(1, 1)).toBe('0.00')
  })

  it('propone una hoja por cada 150 cm de ancho', () => {
    expect(suggestedPanes(1)).toBe(1)
    expect(suggestedPanes(150)).toBe(1)
    expect(suggestedPanes(151)).toBe(2)
    expect(suggestedPanes(300)).toBe(2)
    expect(suggestedPanes(600)).toBe(4)
  })
})

describe('textos', () => {
  it('describe el vidrio en minúsculas', () => {
    expect(describeGlass('Laminado templado', 'Bronce', 8)).toBe('Vidrio laminado templado bronce 8 mm')
    expect(describeGlass('Acústico', 'Verde Frence', 6)).toBe('Vidrio acústico verde frence 6 mm')
  })

  it('describe las medidas y agrega las hojas solo si hay más de una', () => {
    const glass = 'Vidrio laminado templado bronce 8 mm'
    expect(describeDimensions(150, 120, 1, glass)).toBe(
      '120 cm alto x 150 cm ancho (1.80 m²) · Vidrio laminado templado bronce 8 mm',
    )
    expect(describeDimensions(450, 200, 3, glass)).toBe(
      '200 cm alto x 450 cm ancho (9.00 m²) · Vidrio laminado templado bronce 8 mm · 3 hojas',
    )
  })
})

describe('mensaje sugerido', () => {
  const a = buildAutoMessage('Ventanas', 150, 120, 'Vidrio crudo transparente 6 mm')
  const b = buildAutoMessage('Ventanas', 200, 100, 'Vidrio templado bronce 8 mm')

  it('arma el mensaje con medidas y vidrio', () => {
    expect(a).toBe(
      'Hola, me interesa una cotización para Ventanas de 150 cm x 120 cm en vidrio crudo transparente 6 mm.',
    )
  })

  it('llena el mensaje vacío', () => {
    expect(mergeAutoMessage('', null, a)).toBe(a)
  })

  it('reemplaza el mensaje sugerido anterior al cambiar las medidas', () => {
    expect(mergeAutoMessage(a, a, b)).toBe(b)
  })

  it('conserva lo que escribió el cliente', () => {
    expect(mergeAutoMessage(`${a}\nEs para un segundo piso.`, a, b)).toBe(`${b}\nEs para un segundo piso.`)
    expect(mergeAutoMessage('Es para un segundo piso.', a, b)).toBe(`${b}\nEs para un segundo piso.`)
  })

  it('no duplica el mensaje si ya está', () => {
    expect(mergeAutoMessage(b, null, b)).toBe(b)
  })
})

describe('escala del dibujo', () => {
  it('usa la misma escala en ancho y alto para que la medida máxima quepa', () => {
    const ppc = pxPerCm(300, 200)
    expect(ppc).toBeCloseTo(0.5)
    expect(600 * ppc).toBeLessThanOrEqual(300)
    expect(260 * ppc).toBeLessThanOrEqual(200)
  })

  it('calcula la escala aproximada 1:N', () => {
    expect(drawingScale(0.5)).toBe(75)
    expect(drawingScale(0)).toBe(0)
  })
})
