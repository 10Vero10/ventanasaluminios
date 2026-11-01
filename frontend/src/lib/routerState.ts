/** Lee un campo del estado de navegación (location.state), que llega sin tipo. */
export function routerStateValue<T>(state: unknown, key: string, isValid: (value: unknown) => value is T): T | undefined {
  if (typeof state !== 'object' || state === null) return undefined
  const value = (state as Record<string, unknown>)[key]
  return isValid(value) ? value : undefined
}

export const isString = (value: unknown): value is string => typeof value === 'string'
export const isTrue = (value: unknown): value is true => value === true
