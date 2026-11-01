import type { QuoteCreateRequest } from '@/types/quote'

import { httpClient } from './httpClient'

export async function createQuote(request: QuoteCreateRequest): Promise<string> {
  const { data } = await httpClient.post<{ id: string }>('/quotes', request)
  return data.id
}
