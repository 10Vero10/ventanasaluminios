import { act, renderHook, waitFor } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { useLoad } from './useLoad'

describe('useLoad', () => {
  it('al recargar mantiene los datos anteriores hasta que llegan los nuevos', async () => {
    let value = 1
    let resolve: (n: number) => void = () => {}
    const loader = () => (value === 1 ? Promise.resolve(value) : new Promise<number>((r) => (resolve = r)))
    const { result } = renderHook(() => useLoad(loader, []))
    await waitFor(() => expect(result.current.status).toBe('ready'))

    value = 2
    act(() => result.current.reload())
    expect(result.current).toMatchObject({ status: 'ready', data: 1, refreshing: true })

    await act(async () => resolve(2))
    expect(result.current).toMatchObject({ status: 'ready', data: 2, refreshing: false })
  })

  it('si cambian las dependencias vuelve a "loading"', async () => {
    const { result, rerender } = renderHook(({ n }) => useLoad(() => new Promise<number>(() => {}), [n]), {
      initialProps: { n: 1 },
    })
    expect(result.current.status).toBe('loading')
    rerender({ n: 2 })
    expect(result.current.status).toBe('loading')
  })
})
