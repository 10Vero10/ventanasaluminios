import { act, renderHook } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { rebase, useDraft } from './useDraft'

describe('useDraft', () => {
  it('rebase conserva lo editado y toma del servidor lo demás', () => {
    const previous = { name: 'Crudo', defaultOption: true }
    const draft = { name: 'Crudo claro', defaultOption: true }
    expect(rebase(draft, previous, { name: 'Crudo', defaultOption: false })).toEqual({ name: 'Crudo claro', defaultOption: false })
  })

  it('al llegar una versión nueva del servidor actualiza los campos sin tocar', () => {
    const first = { title: 'A', active: true }
    const { result, rerender } = renderHook(({ source }) => useDraft(source), { initialProps: { source: first } })
    act(() => result.current.set('title', 'A editado'))
    rerender({ source: { title: 'A', active: false } })
    expect(result.current.draft).toEqual({ title: 'A editado', active: false })
  })

  it('commit toma lo guardado salvo lo que se siguió escribiendo mientras se guardaba', () => {
    const source = { title: '', category: 'A' }
    const { result } = renderHook(() => useDraft(source))
    act(() => result.current.set('title', ''))
    const sent = result.current.draft
    act(() => result.current.set('category', 'B'))
    act(() => result.current.commit(sent, { title: 'Imagen', category: 'A' }))
    expect(result.current.draft).toEqual({ title: 'Imagen', category: 'B' })
  })
})
