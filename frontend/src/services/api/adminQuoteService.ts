import type { Page, Quote, QuoteDetail, QuoteStatus } from '@/types/quote'

import { httpClient } from './httpClient'

export interface QuoteQuery {
  status: QuoteStatus | null
  page: number
  size: number
  newestFirst: boolean
}

export async function getQuotes(query: QuoteQuery): Promise<Page<Quote>> {
  const { data } = await httpClient.get<Page<Quote>>('/admin/quotes', {
    params: {
      ...(query.status && { status: query.status }),
      page: query.page,
      size: query.size,
      sort: query.newestFirst ? 'createdAt,desc' : 'createdAt,asc',
    },
  })
  return data
}

export async function getQuote(id: string): Promise<QuoteDetail> {
  const { data } = await httpClient.get<QuoteDetail>(`/admin/quotes/${id}`)
  return data
}

export async function changeQuoteStatus(id: string, status: QuoteStatus): Promise<Quote> {
  const { data } = await httpClient.patch<Quote>(`/admin/quotes/${id}/status`, { status })
  return data
}

export async function deleteQuote(id: string): Promise<void> {
  await httpClient.delete(`/admin/quotes/${id}`)
}
