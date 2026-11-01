import type { GlassColorRequest, GlassThicknessRequest, GlassTypeRequest } from '@/types/admin'
import type { GlassColor, GlassOptions, GlassThickness, GlassType, Product, ProductRequest } from '@/types/catalog'

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

export type GlassKind = 'types' | 'colors' | 'thicknesses'

const GLASS_PATH: Record<GlassKind, string> = {
  types: '/admin/glass-types',
  colors: '/admin/glass-colors',
  thicknesses: '/admin/glass-thicknesses',
}

export async function saveGlassType(id: number | null, request: GlassTypeRequest): Promise<GlassType> {
  return saveGlass('types', id, request)
}

export async function saveGlassColor(id: number | null, request: GlassColorRequest): Promise<GlassColor> {
  return saveGlass('colors', id, request)
}

export async function saveGlassThickness(id: number | null, request: GlassThicknessRequest): Promise<GlassThickness> {
  return saveGlass('thicknesses', id, request)
}

async function saveGlass<T>(kind: GlassKind, id: number | null, request: unknown): Promise<T> {
  const { data } =
    id === null
      ? await httpClient.post<T>(GLASS_PATH[kind], request)
      : await httpClient.put<T>(`${GLASS_PATH[kind]}/${id}`, request)
  return data
}

export async function reorderGlass(kind: GlassKind, ids: number[]): Promise<void> {
  await httpClient.put(`${GLASS_PATH[kind]}/order`, { ids })
}
