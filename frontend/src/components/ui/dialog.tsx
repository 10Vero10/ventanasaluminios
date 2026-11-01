import { AnimatePresence, motion, useReducedMotion } from 'motion/react'
import { Dialog as DialogPrimitive } from 'radix-ui'
import type { ReactNode } from 'react'

import { cn } from '@/lib/utils'

/** Spring sin rebote (apple-design): el diálogo entra y sale por el mismo camino y se puede interrumpir. */
const SPRING = { type: 'spring', bounce: 0, duration: 0.35 } as const

interface DialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  description?: string
  children: ReactNode
  className?: string
}

/** Diálogo accesible (Radix: foco atrapado, Escape, aria) con fondo que atenúa para enfocar la tarea. */
export function Dialog({ open, onOpenChange, title, description, children, className }: DialogProps) {
  const reduceMotion = useReducedMotion()
  return (
    <DialogPrimitive.Root open={open} onOpenChange={onOpenChange}>
      <AnimatePresence>
        {open && (
          <DialogPrimitive.Portal forceMount>
            <DialogPrimitive.Overlay asChild forceMount>
              <motion.div
                className="fixed inset-0 z-50 bg-black/40"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.2 }}
              />
            </DialogPrimitive.Overlay>
            <DialogPrimitive.Content asChild forceMount aria-describedby={description ? undefined : undefined}>
              <motion.div
                className={cn(
                  'fixed inset-x-0 bottom-0 z-50 max-h-[92dvh] overflow-y-auto rounded-t-[28px] bg-background p-6 shadow-[0_-1px_0_rgba(0,0,0,0.04)] focus:outline-none sm:inset-x-auto sm:top-1/2 sm:bottom-auto sm:left-1/2 sm:w-[min(720px,calc(100vw-48px))] sm:-translate-x-1/2 sm:-translate-y-1/2 sm:rounded-[28px] sm:p-10',
                  className,
                )}
                initial={reduceMotion ? { opacity: 0 } : { opacity: 0, y: 40 }}
                animate={{ opacity: 1, y: 0 }}
                exit={reduceMotion ? { opacity: 0 } : { opacity: 0, y: 40 }}
                transition={reduceMotion ? { duration: 0.15 } : SPRING}
              >
                <DialogPrimitive.Title className="pr-12 font-display text-[28px] leading-tight font-semibold tracking-[-0.015em]">
                  {title}
                </DialogPrimitive.Title>
                {description ? (
                  <DialogPrimitive.Description className="mt-2 text-muted-foreground">{description}</DialogPrimitive.Description>
                ) : (
                  <DialogPrimitive.Description className="sr-only">{title}</DialogPrimitive.Description>
                )}
                <DialogPrimitive.Close
                  className="absolute top-5 right-5 flex size-11 items-center justify-center rounded-full bg-secondary text-foreground transition-transform duration-100 active:scale-95 focus-visible:outline-2 focus-visible:outline-ring"
                  aria-label="Cerrar"
                >
                  <span aria-hidden="true" className="text-xl leading-none">×</span>
                </DialogPrimitive.Close>
                <div className="mt-6">{children}</div>
              </motion.div>
            </DialogPrimitive.Content>
          </DialogPrimitive.Portal>
        )}
      </AnimatePresence>
    </DialogPrimitive.Root>
  )
}
