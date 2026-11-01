import type { FeaturedLineRequest, GalleryImageRequest, SiteSettingsRequest } from '@/types/admin'
import type { Dashboard } from '@/types/quote'
import type { FeaturedLine, GalleryImage, SiteSettings } from '@/types/site'

import { httpClient } from './httpClient'

export async function getDashboard(): Promise<Dashboard> {
  const { data } = await httpClient.get<Dashboard>('/admin/dashboard')
  return data
}

export async function getSettings(): Promise<SiteSettings> {
  const { data } = await httpClient.get<SiteSettings>('/admin/settings')
  return data
}

export async function updateSettings(request: SiteSettingsRequest): Promise<SiteSettings> {
  const { data } = await httpClient.put<SiteSettings>('/admin/settings', request)
  return data
}

export async function getFeaturedLines(): Promise<FeaturedLine[]> {
  const { data } = await httpClient.get<FeaturedLine[]>('/admin/featured-lines')
  return data
}

export async function updateFeaturedLine(id: number, request: FeaturedLineRequest): Promise<FeaturedLine> {
  const { data } = await httpClient.put<FeaturedLine>(`/admin/featured-lines/${id}`, request)
  return data
}

export async function reorderFeaturedLines(ids: number[]): Promise<FeaturedLine[]> {
  const { data } = await httpClient.put<FeaturedLine[]>('/admin/featured-lines/order', { ids })
  return data
}

export async function getAdminGallery(): Promise<GalleryImage[]> {
  const { data } = await httpClient.get<GalleryImage[]>('/admin/gallery')
  return data
}

export async function createGalleryImage(request: GalleryImageRequest): Promise<GalleryImage> {
  const { data } = await httpClient.post<GalleryImage>('/admin/gallery', request)
  return data
}

export async function updateGalleryImage(id: number, request: GalleryImageRequest): Promise<GalleryImage> {
  const { data } = await httpClient.put<GalleryImage>(`/admin/gallery/${id}`, request)
  return data
}

export async function deleteGalleryImage(id: number): Promise<void> {
  await httpClient.delete(`/admin/gallery/${id}`)
}

export async function reorderGallery(ids: number[]): Promise<GalleryImage[]> {
  const { data } = await httpClient.put<GalleryImage[]>('/admin/gallery/order', { ids })
  return data
}

/** Sube una imagen (JPG, PNG o WebP de máximo 5 MB) y devuelve su URL pública "/media/…". */
export async function uploadImage(file: File): Promise<string> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await httpClient.post<{ url: string }>('/admin/media', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60_000,
  })
  return data.url
}
