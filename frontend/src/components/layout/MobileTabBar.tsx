import { NavLink } from 'react-router'

import { cn } from '@/lib/utils'
import { useSite } from '@/site/siteContext'

import { visibleNav } from './navigation'

/** Barra inferior en el celular (briefing §5.2.3), al alcance del pulgar y respetando el área segura. */
export function MobileTabBar() {
  const site = useSite()
  return (
    <nav
      aria-label="Secciones"
      className="fixed inset-x-0 bottom-0 z-40 border-t border-black/5 bg-white/85 pb-[env(safe-area-inset-bottom)] backdrop-blur-xl md:hidden"
    >
      <ul className="flex">
        {visibleNav(site.quotingEnabled).map((item) => (
          <li key={item.to} className="flex-1">
            <NavLink
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                cn(
                  'flex min-h-14 items-center justify-center text-xs',
                  isActive ? 'font-semibold text-link' : 'text-foreground/70',
                )
              }
            >
              {item.label}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  )
}
