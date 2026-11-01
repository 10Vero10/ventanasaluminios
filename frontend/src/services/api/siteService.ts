import type { ProductCategory } from '@/types/catalog'
import type { GalleryImage, PublicSite } from '@/types/site'

import { httpClient } from './httpClient'

export async function getSite(): Promise<PublicSite> {
  const { data } = await httpClient.get<PublicSite>('/site')
  return data
}

export async function getGallery(category?: ProductCategory): Promise<GalleryImage[]> {
  const { data } = await httpClient.get<GalleryImage[]>('/gallery', { params: category ? { category } : {} })
  return data
}
