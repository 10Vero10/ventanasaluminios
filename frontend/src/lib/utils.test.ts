import { describe, expect, it } from 'vitest'

import { cn } from './utils'

describe('cn', () => {
  it('distingue los tamaños propios de los colores', () => {
    expect(cn('text-body text-muted-foreground')).toBe('text-body text-muted-foreground')
  })

  it('un tamaño posterior reemplaza al anterior en el mismo punto de quiebre', () => {
    expect(cn('text-title-2 sm:text-title-1', 'text-title-3 sm:text-title-3')).toBe('text-title-3 sm:text-title-3')
  })
})
