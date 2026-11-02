import type { ProductCategory } from '@/types/catalog'
import type { GalleryImage, PublicSite } from '@/types/site'

import { httpClient, WAKE_UP_TIMEOUT_MS } from './httpClient'

/** Configuración del sitio. Es lo primero que se pide, así que espera a que el servidor despierte. */
export async function getSite(): Promise<PublicSite> {
  const { data } = await httpClient.get<PublicSite>('/site', { timeout: WAKE_UP_TIMEOUT_MS })
  return data
}

export async function getGallery(category?: ProductCategory): Promise<GalleryImage[]> {
  const { data } = await httpClient.get<GalleryImage[]>('/gallery', { params: category ? { category } : {} })
  return data
}
