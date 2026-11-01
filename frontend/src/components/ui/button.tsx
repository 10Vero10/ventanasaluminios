import { Slot } from 'radix-ui'
import { cva, type VariantProps } from 'class-variance-authority'
import type { ComponentProps } from 'react'

import { cn } from '@/lib/utils'

/**
 * Botón en píldora (apple-style-reference). El azul se reserva para la acción principal de cada pantalla; responde al
 * presionar (escala 0.97) y no al soltar.
 */
const buttonVariants = cva(
  'inline-flex items-center justify-center gap-2 rounded-full font-normal whitespace-nowrap select-none transition-[transform,background-color,color,border-color] duration-100 ease-out active:scale-[0.97] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring disabled:opacity-50 disabled:active:scale-100 motion-reduce:transition-none motion-reduce:active:scale-100',
  {
    variants: {
      variant: {
        primary: 'bg-primary text-primary-foreground hover:bg-[#0077ed]',
        secondary: 'border border-input bg-transparent text-foreground hover:bg-secondary',
        ghost: 'bg-transparent text-foreground hover:bg-secondary',
        link: 'rounded-md bg-transparent px-0 text-link underline-offset-4 hover:underline',
        destructive: 'border border-destructive bg-transparent text-destructive hover:bg-destructive hover:text-white',
      },
      size: {
        sm: 'min-h-9 px-4 text-sm',
        md: 'min-h-11 px-5 text-[17px] tracking-[-0.022em]',
        lg: 'min-h-12 px-7 text-[17px] tracking-[-0.022em]',
        icon: 'size-11',
      },
    },
    defaultVariants: { variant: 'primary', size: 'md' },
  },
)

type ButtonProps = ComponentProps<'button'> & VariantProps<typeof buttonVariants> & { asChild?: boolean }

export function Button({ className, variant, size, asChild = false, type, ...props }: ButtonProps) {
  const Component = asChild ? Slot.Root : 'button'
  return (
    <Component
      className={cn(buttonVariants({ variant, size }), className)}
      type={asChild ? undefined : (type ?? 'button')}
      {...props}
    />
  )
}
