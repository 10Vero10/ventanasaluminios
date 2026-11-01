import { motion, useReducedMotion } from 'motion/react'
import { useEffect, useRef, useState } from 'react'

import { drawingScale, pxPerCm } from '@/lib/configurator'

import type { ResolvedConfiguration } from './configuration'

const MAX_DRAWING_HEIGHT_PX = 240
/** Espacio para las cotas: a la izquierda la del alto y abajo la del ancho. */
const MARGIN_LEFT = 68
const MARGIN_BOTTOM = 44
const MARGIN_TOP = 12
const MARGIN_RIGHT = 12
const SPRING = { type: 'spring', bounce: 0, duration: 0.4 } as const

/**
 * Plano a escala real (briefing §3.5.2): la misma escala en ancho y alto, calculada para que 600 × 260 cm quepa en el
 * ancho disponible. Las medidas cambian con un spring sin rebote que parte del tamaño actual, así se puede escribir o
 * pulsar − / + sin esperar a que termine la animación anterior.
 */
export function BlueprintDrawing({ config }: { config: ResolvedConfiguration }) {
  const containerRef = useRef<HTMLDivElement>(null)
  const [availableWidth, setAvailableWidth] = useState(0)
  const reduceMotion = useReducedMotion()

  useEffect(() => {
    const element = containerRef.current
    if (!element) return
    const observer = new ResizeObserver(([entry]) => setAvailableWidth(entry.contentRect.width))
    observer.observe(element)
    return () => observer.disconnect()
  }, [])

  const ppc = pxPerCm(Math.max(0, availableWidth - MARGIN_LEFT - MARGIN_RIGHT), MAX_DRAWING_HEIGHT_PX)
  const width = config.widthCm * ppc
  const height = config.heightCm * ppc
  const svgHeight = MARGIN_TOP + MAX_DRAWING_HEIGHT_PX + MARGIN_BOTTOM
  const top = MARGIN_TOP + MAX_DRAWING_HEIGHT_PX - height
  const left = MARGIN_LEFT
  const transition = reduceMotion ? { duration: 0 } : SPRING
  const label = `${config.type.name.toLocaleUpperCase('es-CO')} · ${config.thickness.mm} MM`
  const labelFits = width > label.length * 7 + 16 && height > 28
  const scale = drawingScale(ppc)
  const paneWidth = width / config.panes

  return (
    <figure className="flex flex-col gap-3">
      <div ref={containerRef} className="w-full">
        {availableWidth > 0 && (
          <svg
            width={availableWidth}
            height={svgHeight}
            viewBox={`0 0 ${availableWidth} ${svgHeight}`}
            role="img"
            aria-label={`Plano de la ventana: ${config.widthCm} cm de ancho por ${config.heightCm} cm de alto, ${config.panes} ${config.panes === 1 ? 'hoja' : 'hojas'}`}
            className="block overflow-visible"
          >
            <defs>
              <linearGradient id="reflejo" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0" stopColor="#ffffff" stopOpacity="0.55" />
                <stop offset="0.45" stopColor="#ffffff" stopOpacity="0" />
                <stop offset="1" stopColor="#ffffff" stopOpacity="0.25" />
              </linearGradient>
            </defs>
            {/* Marco y vidrio */}
            <motion.rect
              initial={false}
              animate={{ x: left, y: top, width, height }}
              transition={transition}
              fill={config.color.hex}
              fillOpacity={0.55}
              stroke="#1d1d1f"
              strokeWidth={2}
            />
            {config.color.reflective && (
              <motion.rect
                initial={false}
                animate={{ x: left, y: top, width, height }}
                transition={transition}
                fill="url(#reflejo)"
              />
            )}
            {Array.from({ length: config.panes - 1 }, (_, i) => (
              <motion.line
                key={i}
                initial={false}
                animate={{ x1: left + paneWidth * (i + 1), x2: left + paneWidth * (i + 1), y1: top, y2: top + height }}
                transition={transition}
                stroke="#1d1d1f"
                strokeWidth={1.5}
              />
            ))}
            {labelFits && (
              <motion.text
                initial={false}
                animate={{ x: left + width / 2, y: top + height / 2 }}
                transition={transition}
                textAnchor="middle"
                dominantBaseline="middle"
                className="fill-foreground text-[11px] font-semibold tracking-[0.04em]"
              >
                {label}
              </motion.text>
            )}
            {/* Cota del ancho */}
            <motion.line
              initial={false}
              animate={{ x1: left, x2: left + width, y1: MARGIN_TOP + MAX_DRAWING_HEIGHT_PX + 18, y2: MARGIN_TOP + MAX_DRAWING_HEIGHT_PX + 18 }}
              transition={transition}
              stroke="#707070"
              strokeWidth={1}
            />
            <motion.text
              initial={false}
              animate={{ x: left + width / 2, y: MARGIN_TOP + MAX_DRAWING_HEIGHT_PX + 36 }}
              transition={transition}
              textAnchor="middle"
              className="fill-muted-foreground text-[12px]"
            >
              {config.widthCm} cm
            </motion.text>
            {/* Cota del alto */}
            <motion.line
              initial={false}
              animate={{ x1: left - 14, x2: left - 14, y1: top, y2: top + height }}
              transition={transition}
              stroke="#707070"
              strokeWidth={1}
            />
            <motion.text
              initial={false}
              animate={{ x: left - 20, y: top + height / 2 }}
              transition={transition}
              textAnchor="end"
              dominantBaseline="middle"
              className="fill-muted-foreground text-[12px]"
            >
              {config.heightCm} cm
            </motion.text>
          </svg>
        )}
      </div>
      <figcaption className="flex items-center justify-between text-xs text-muted-foreground">
        <span>Dibujo a escala real, la misma en ancho y alto.</span>
        {scale > 0 && <span title="Escala aproximada del dibujo en esta pantalla">ESC ≈ 1:{scale}</span>}
      </figcaption>
    </figure>
  )
}
