import { buildAutoMessage, mergeAutoMessage } from '@/lib/configurator'
import type { QuoteCreateRequest } from '@/types/quote'

import { dimensionsText, glassDescription, type ResolvedConfiguration } from './configuration'

/** Máximo de caracteres del mensaje que acepta el servidor. */
export const MAX_MESSAGE = 2000

/**
 * La parte del formulario de cotización que el configurador puede llenar (briefing §3.5.2), con lo necesario para
 * sincronizarla: qué configuración está aplicada y cuál fue el último mensaje sugerido.
 */
export interface QuoteDraft {
  productType: string
  dimensionsText: string
  message: string
  /** Configuración que está en el formulario y se envía; null antes de "Aplicar" o si se escribieron medidas a mano. */
  applied: ResolvedConfiguration | null
  /** Último mensaje sugerido, para reemplazarlo en vez de repetirlo. */
  lastAutoMessage: string | null
}

export function emptyDraft(productType: string): QuoteDraft {
  return { productType, dimensionsText: '', message: '', applied: null, lastAutoMessage: null }
}

/** Pone en el formulario las medidas y el mensaje de una configuración válida, sin perder lo que escribió el cliente. */
export function applyConfiguration(draft: QuoteDraft, resolved: ResolvedConfiguration): QuoteDraft {
  const autoMessage = buildAutoMessage(draft.productType, resolved.widthCm, resolved.heightCm, glassDescription(resolved))
  return {
    ...draft,
    dimensionsText: dimensionsText(resolved),
    // El texto sugerido va al inicio; se recorta para no pasar el máximo del servidor.
    message: mergeAutoMessage(draft.message, draft.lastAutoMessage, autoMessage).slice(0, MAX_MESSAGE),
    applied: resolved,
    lastAutoMessage: autoMessage,
  }
}

/**
 * Después del primer "Aplicar", cada cambio vuelve a llenar el formulario. Con una medida en 0 ({@code resolved} null)
 * se conservan los últimos valores válidos.
 */
export function syncConfiguration(draft: QuoteDraft, resolved: ResolvedConfiguration | null): QuoteDraft {
  return draft.applied && resolved ? applyConfiguration(draft, resolved) : draft
}

/** Escribir las medidas a mano desconecta el configurador: el servidor guardaría su texto en vez del escrito. */
export function writeDimensions(draft: QuoteDraft, text: string): QuoteDraft {
  return { ...draft, dimensionsText: text, applied: null }
}

/** Tras enviar: el mensaje enviado se vacía, así que no hay sugerencia anterior que reemplazar. */
export function afterSent(draft: QuoteDraft, sentMessage: string): QuoteDraft {
  // Si el visitante siguió escribiendo mientras se enviaba, el mensaje nuevo se conserva.
  if (draft.message !== sentMessage) return draft
  return { ...draft, message: '', lastAutoMessage: null }
}

/** Datos de contacto del formulario, más el campo trampa. */
export interface QuoteContact {
  name: string
  phone: string
  website: string
}

/** Solicitud que se envía: textos recortados y, si hay una configuración aplicada, sus datos estructurados. */
export function toQuoteRequest(draft: QuoteDraft, contact: QuoteContact): QuoteCreateRequest {
  const applied = draft.applied
  return {
    name: contact.name.trim(),
    phone: contact.phone.trim(),
    productType: draft.productType,
    dimensionsText: draft.dimensionsText.trim(),
    message: draft.message.trim(),
    website: contact.website,
    ...(applied && {
      widthCm: applied.widthCm,
      heightCm: applied.heightCm,
      panes: applied.panes,
      glassTypeCode: applied.type.code,
      glassColorCode: applied.color.code,
      thicknessMm: applied.thickness.mm,
    }),
  }
}
