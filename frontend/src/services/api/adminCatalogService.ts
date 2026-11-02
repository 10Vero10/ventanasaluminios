import type { GlassKind, GlassKinds } from '@/types/admin'
import type { GlassOptions, Product, ProductRequest } from '@/types/catalog'

import { httpClient } from './httpClient'

export async function getAdminProducts(): Promise<Product[]> {
  const { data } = await httpClient.get<Product[]>('/admin/products')
  return data
}

export async function createProduct(request: ProductRequest): Promise<Product> {
  const { data } = await httpClient.post<Product>('/admin/products', request)
  return data
}

export async function updateProduct(id: number, request: ProductRequest): Promise<Product> {
  const { data } = await httpClient.put<Product>(`/admin/products/${id}`, request)
  return data
}

export async function duplicateProduct(id: number): Promise<Product> {
  const { data } = await httpClient.post<Product>(`/admin/products/${id}/duplicate`)
  return data
}

export async function deleteProduct(id: number): Promise<void> {
  await httpClient.delete(`/admin/products/${id}`)
}

export async function reorderProducts(ids: number[]): Promise<Product[]> {
  const { data } = await httpClient.put<Product[]>('/admin/products/order', { ids })
  return data
}

export async function getAdminGlassOptions(): Promise<GlassOptions> {
  const { data } = await httpClient.get<GlassOptions>('/admin/glass-options')
  return data
}

const GLASS_PATH: Record<GlassKind, string> = {
  types: '/admin/glass-types',
  colors: '/admin/glass-colors',
  thicknesses: '/admin/glass-thicknesses',
}

/** Crea ({@code id} null) o edita una opción de vidrio. */
export async function saveGlassOption<K extends GlassKind>(
  kind: K,
  id: number | null,
  request: GlassKinds[K]['request'],
): Promise<GlassKinds[K]['item']> {
  const { data } =
    id === null
      ? await httpClient.post<GlassKinds[K]['item']>(GLASS_PATH[kind], request)
      : await httpClient.put<GlassKinds[K]['item']>(`${GLASS_PATH[kind]}/${id}`, request)
  return data
}

export async function reorderGlass(kind: GlassKind, ids: number[]): Promise<void> {
  await httpClient.put(`${GLASS_PATH[kind]}/order`, { ids })
}
