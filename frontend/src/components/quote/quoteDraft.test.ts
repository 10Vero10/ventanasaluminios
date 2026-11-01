import { describe, expect, it } from 'vitest'

import { GLASS_OPTIONS } from '@/test/fixtures'

import { INITIAL_CONFIGURATION, resolveConfiguration } from './configuration'
import { afterSent, applyConfiguration, emptyDraft, syncConfiguration, writeDimensions } from './quoteDraft'

const resolved = resolveConfiguration(INITIAL_CONFIGURATION, GLASS_OPTIONS)!
const wider = resolveConfiguration({ ...INITIAL_CONFIGURATION, widthCm: 200 }, GLASS_OPTIONS)!

describe('quoteDraft', () => {
  it('aplicar llena medidas y mensaje y guarda la configuración', () => {
    const draft = applyConfiguration(emptyDraft('Ventanas arquitectónicas'), resolved)
    expect(draft.dimensionsText).toBe('120 cm alto x 150 cm ancho (1.80 m²) · Vidrio crudo transparente 6 mm')
    expect(draft.message).toBe(
      'Hola, me interesa una cotización para Ventanas arquitectónicas de 150 cm x 120 cm en vidrio crudo transparente 6 mm.',
    )
    expect(draft.applied).toBe(resolved)
  })

  it('sincroniza solo después de aplicar y conserva lo válido con una medida en 0', () => {
    const initial = emptyDraft('Ventanas arquitectónicas')
    expect(syncConfiguration(initial, wider)).toBe(initial)
    const applied = applyConfiguration(initial, resolved)
    expect(syncConfiguration(applied, null)).toBe(applied)
    expect(syncConfiguration(applied, wider).message).toContain('200 cm x 120 cm')
  })

  it('reemplaza la sugerencia anterior sin tocar el texto del cliente', () => {
    const draft = applyConfiguration({ ...emptyDraft('Ventanas arquitectónicas'), message: 'Segundo piso.' }, resolved)
    const next = syncConfiguration(draft, wider)
    expect(next.message.split('\n')).toEqual([
      'Hola, me interesa una cotización para Ventanas arquitectónicas de 200 cm x 120 cm en vidrio crudo transparente 6 mm.',
      'Segundo piso.',
    ])
  })

  it('escribir las medidas a mano desconecta el configurador; enviar vacía el mensaje', () => {
    const applied = applyConfiguration(emptyDraft('Ventanas arquitectónicas'), resolved)
    expect(writeDimensions(applied, 'Dos ventanas').applied).toBeNull()
    expect(afterSent(applied)).toMatchObject({ message: '', lastAutoMessage: null, applied: resolved })
  })
})
