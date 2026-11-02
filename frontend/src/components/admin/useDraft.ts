import { useState } from 'react'

type KeysOf<T, V> = { [K in keyof T]-?: T[K] extends V ? (V extends T[K] ? K : never) : never }[keyof T]

export interface DraftState<T> {
  draft: T
  set: <K extends keyof T>(key: K, value: T[K]) => void
  /** Props de un campo de texto: valor y cambio. */
  bind: (key: KeysOf<T, string>) => { value: string; onChange: (value: string) => void }
  /** Props de un campo de texto opcional: vacío se guarda como null. */
  bindOptional: (key: KeysOf<T, string | null>) => { value: string; onChange: (value: string) => void }
  /** Reemplaza el borrador entero (por ejemplo, para vaciar un formulario de alta). */
  reset: (value: T) => void
  /**
   * Después de guardar {@code sent} y recibir {@code saved}: toma lo guardado (que puede venir normalizado) salvo en
   * los campos que el usuario siguió editando mientras se guardaba.
   */
  commit: (sent: T, saved: T) => void
}

/**
 * Borrador editable de un objeto que viene del servidor.
 *
 * Cuando llega una versión nueva del objeto (otra referencia: se recargó la lista, se guardó otra fila que lo cambió),
 * los campos que el usuario no tocó toman el valor del servidor y los que sí tocó se conservan.
 *
 * {@code source} debe ser estable entre renders: un estado, datos cargados o una constante. Un objeto armado en cada
 * render se tomaría como versión nueva cada vez.
 */
export function useDraft<T extends object>(source: T): DraftState<T> {
  const [base, setBase] = useState(source)
  const [draft, setDraft] = useState(source)

  if (base !== source) {
    setBase(source)
    setDraft((current) => rebase(current, base, source))
  }

  function set<K extends keyof T>(key: K, value: T[K]) {
    setDraft((current) => ({ ...current, [key]: value }))
  }

  return {
    draft,
    set,
    bind: (key) => ({ value: draft[key] as string, onChange: (value) => set(key, value as T[typeof key]) }),
    bindOptional: (key) => ({
      value: (draft[key] as string | null) ?? '',
      onChange: (value) => set(key, (value || null) as T[typeof key]),
    }),
    reset: (value) => setDraft(value),
    commit: (sent, saved) => setDraft((current) => rebase(current, sent, saved)),
  }
}

/** Toma los valores nuevos salvo en los campos que el usuario cambió respecto de la versión anterior. */
export function rebase<T extends object>(draft: T, previous: T, next: T): T {
  const result = { ...next }
  for (const key of Object.keys(next) as (keyof T)[]) {
    if (!Object.is(draft[key], previous[key])) result[key] = draft[key]
  }
  return result
}
