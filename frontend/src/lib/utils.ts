import { clsx, type ClassValue } from 'clsx'
import { extendTailwindMerge } from 'tailwind-merge'

/**
 * Tamaños de texto propios (ver @theme en index.css). Sin registrarlos, tailwind-merge los tomaría por colores y, al
 * combinar "text-body text-muted-foreground", borraría uno de los dos.
 */
export const TEXT_SIZES = [
  'caption',
  'callout',
  'body',
  'intro',
  'intro-lg',
  'title-3',
  'title-2',
  'title-1',
  'display-xs',
  'hero-sm',
  'display-sm',
  'hero',
  'display',
  'display-md',
  'display-lg',
  'display-xl',
]

const twMerge = extendTailwindMerge({ extend: { theme: { text: TEXT_SIZES } } })

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
