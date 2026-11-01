export type QuoteStatus = 'NUEVA' | 'CONTACTADA' | 'COTIZADA' | 'CERRADA' | 'DESCARTADA'

export const QUOTE_STATUSES: { value: QuoteStatus; label: string }[] = [
  { value: 'NUEVA', label: 'Nueva' },
  { value: 'CONTACTADA', label: 'Contactada' },
  { value: 'COTIZADA', label: 'Cotizada' },
  { value: 'CERRADA', label: 'Cerrada' },
  { value: 'DESCARTADA', label: 'Descartada' },
]

export function quoteStatusLabel(status: QuoteStatus): string {
  return QUOTE_STATUSES.find((s) => s.value === status)?.label ?? status
}

export interface QuoteCreateRequest {
  name: string
  phone: string
  productType: string
  dimensionsText: string
  message: string
  widthCm?: number
  heightCm?: number
  panes?: number
  glassTypeCode?: string
  glassColorCode?: string
  thicknessMm?: number
  website: string
}

export interface Quote {
  id: string
  createdAt: string
  updatedAt: string
  status: QuoteStatus
  name: string
  phone: string
  productType: string | null
  dimensionsText: string | null
  message: string | null
  widthCm: number | null
  heightCm: number | null
  panes: number | null
  areaM2: number | null
  glassTypeCode: string | null
  glassColorCode: string | null
  thicknessMm: number | null
}

export interface QuoteStatusChange {
  fromStatus: QuoteStatus
  toStatus: QuoteStatus
  changedAt: string
}

export interface QuoteDetail {
  quote: Quote
  history: QuoteStatusChange[]
}

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface Dashboard {
  products: number
  galleryImages: number
  newQuotes: number
}
