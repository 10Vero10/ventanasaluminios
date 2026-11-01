import { useState } from 'react'

type KeysOf<T, V> = { [K in keyof T]-?: T[K] extends V ? (V extends T[K] ? K : never) : never }[keyof T]

/**
 * Borrador editable de un objeto que viene del servidor.
 *
 * Cuando llega una versión nueva del objeto (otra referencia: se recargó la lista, se guardó otra fila que lo cambió),
 * los campos que el usuario no tocó toman el valor del servidor y los que sí tocó se conservan. Así una recarga no
 * borra lo escrito y, por ejemplo, una opción que el servidor dejó de marcar "por defecto" se ve desmarcada.
 *
 * Con {@code source} null (todavía cargando) el borrador también es null.
 */
export function useDraft<T extends object>(source: T): DraftState<T, T>
export function useDraft<T extends object>(source: T | null): DraftState<T, T | null>
export function useDraft<T extends object>(source: T | null): DraftState<T, T | null> {
  const [base, setBase] = useState(source)
  const [draft, setDraft] = useState(source)

  if (base !== source) {
    setBase(source)
    setDraft((current) => (current === null || base === null || source === null ? source : rebase(current, base, source)))
  }

  function set<K extends keyof T>(key: K, value: T[K]) {
    setDraft((current) => current && { ...current, [key]: value })
  }

  return {
    draft,
    set,
    /** Props de un campo de texto: valor y cambio. */
    bind: (key: KeysOf<T, string>) => ({
      value: (draft?.[key] as string | undefined) ?? '',
      onChange: (value: string) => set(key, value as T[typeof key]),
    }),
    /** Props de un campo de texto opcional: vacío se guarda como null. */
    bindOptional: (key: KeysOf<T, string | null>) => ({
      value: (draft?.[key] as string | null | undefined) ?? '',
      onChange: (value: string) => set(key, (value || null) as T[typeof key]),
    }),
    /** Reemplaza el borrador (por ejemplo, con lo que devolvió el servidor al guardar). */
    reset: (value: T) => setDraft(value),
  }
}

export interface DraftState<T, D> {
  draft: D
  set: <K extends keyof T>(key: K, value: T[K]) => void
  bind: (key: KeysOf<T, string>) => { value: string; onChange: (value: string) => void }
  bindOptional: (key: KeysOf<T, string | null>) => { value: string; onChange: (value: string) => void }
  reset: (value: T) => void
}

/** Toma los valores nuevos del servidor salvo en los campos que el usuario cambió respecto de la versión anterior. */
export function rebase<T extends object>(draft: T, previous: T, next: T): T {
  const result = { ...next }
  for (const key of Object.keys(next) as (keyof T)[]) {
    if (!Object.is(draft[key], previous[key])) result[key] = draft[key]
  }
  return result
}
