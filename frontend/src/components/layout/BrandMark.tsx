/** Logo de 4 paneles de la empresa (el mismo del favicon), como marca de la cabecera y el pie. */
export function BrandMark({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 64 64" className={className} aria-hidden="true">
      <rect width="64" height="64" rx="12" fill="#00236f" />
      <g fill="#ffffff" fillOpacity=".3" stroke="#ffffff" strokeOpacity=".8" strokeWidth="1.5">
        <rect x="18" y="18" width="12" height="12" rx="1" />
        <rect x="34" y="18" width="12" height="12" rx="1" />
        <rect x="18" y="34" width="12" height="12" rx="1" />
        <rect x="34" y="34" width="12" height="12" rx="1" />
      </g>
    </svg>
  )
}
