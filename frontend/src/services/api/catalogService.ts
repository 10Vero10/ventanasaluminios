import type { GlassOptions, Product, ProductCategory } from '@/types/catalog'

import { httpClient } from './httpClient'

export async function getProducts(category?: ProductCategory): Promise<Product[]> {
  const { data } = await httpClient.get<Product[]>('/products', { params: category ? { category } : {} })
  return data
}

export async function getGlassOptions(): Promise<GlassOptions> {
  const { data } = await httpClient.get<GlassOptions>('/glass-options')
  return data
}
