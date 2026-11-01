/** Enlaces de contacto (briefing §3.8). */

export const WHATSAPP_DEFAULT_MESSAGE = 'Hola Ventanas y Aluminios, quiero cotizar un proyecto a medida.'

/** "+57 317 846 3260" → "tel:+573178463260". */
export function phoneHref(phone: string): string {
  return `tel:${phone.replace(/\s+/g, '')}`
}

export function whatsappHref(whatsappNumber: string, message: string = WHATSAPP_DEFAULT_MESSAGE): string {
  return `https://wa.me/${whatsappNumber.replace(/\D/g, '')}?text=${encodeURIComponent(message)}`
}

export function mailHref(email: string): string {
  return `mailto:${email}`
}
