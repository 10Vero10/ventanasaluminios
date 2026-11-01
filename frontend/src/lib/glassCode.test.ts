import { describe, expect, it } from 'vitest'

import { GLASS_CODE_PATTERN, glassCodeFrom } from './glassCode'

describe('glassCodeFrom', () => {
  it('quita tildes y espacios y deja un código válido', () => {
    expect(glassCodeFrom('Termo acústico')).toBe('TERMO_ACUSTICO')
    expect(glassCodeFrom('  Verde  Frence ')).toBe('VERDE_FRENCE')
    expect(GLASS_CODE_PATTERN.test(glassCodeFrom('Laminado templado'))).toBe(true)
  })
})
