import { useState } from 'react'

import { useAdminOutlet } from '@/components/admin/adminOutlet'
import { ConfirmDialog } from '@/components/admin/ConfirmDialog'
import { Panel } from '@/components/admin/Panel'
import { Button } from '@/components/ui/button'
import { Loadable } from '@/components/ui/Loadable'
import { OptionSelect } from '@/components/ui/OptionSelect'
import { InlineError, MessageState } from '@/components/ui/states'
import { phoneHref } from '@/lib/contact'
import { useLoad } from '@/lib/useLoad'
import { getAdminGlassOptions } from '@/services/api/adminCatalogService'
import { changeQuoteStatus, deleteQuote, getQuote, getQuotes } from '@/services/api/adminQuoteService'
import { getErrorMessage } from '@/services/api/httpClient'
import type { GlassOptions } from '@/types/catalog'
import { type Quote, QUOTE_STATUSES, type QuoteStatus, quoteStatusLabel } from '@/types/quote'

const PAGE_SIZE = 20
const DATE_FORMAT = new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'short' })
const ORDERS = [
  { value: 'asc', label: 'Más antiguas primero' },
  { value: 'desc', label: 'Más recientes primero' },
] as const

/** Bandeja de cotizaciones (briefing §3.7 y §5.7.6). */
export default function QuotesTab() {
  const { refreshDashboard } = useAdminOutlet()
  const [status, setStatus] = useState<QuoteStatus | null>(null)
  const [order, setOrder] = useState<'asc' | 'desc'>('asc')
  const [page, setPage] = useState(0)
  const newestFirst = order === 'desc'
  const quotes = useLoad(() => getQuotes({ status, page, size: PAGE_SIZE, newestFirst }), [status, page, newestFirst])
  const glass = useLoad(getAdminGlassOptions, [])
  const [toDelete, setToDelete] = useState<Quote | null>(null)

  function reload() {
    quotes.reload()
    refreshDashboard()
  }

  function updated(quote: Quote) {
    quotes.setData((current) => ({ ...current, content: current.content.map((q) => (q.id === quote.id ? quote : q)) }))
    refreshDashboard()
  }

  return (
    <Panel
      title="Cotizaciones"
      description="Solicitudes enviadas desde el formulario de cotización del sitio."
      actions={
        <Button size="sm" variant="secondary" onClick={reload}>
          Actualizar
        </Button>
      }
    >
      <div className="flex flex-wrap gap-4">
        <label className="flex flex-col gap-1 text-sm font-semibold">
          Estado
          <OptionSelect
            className="py-2 text-sm"
            options={QUOTE_STATUSES}
            noneLabel="Todos los estados"
            value={status}
            onChange={(value) => {
              setStatus(value)
              setPage(0)
            }}
          />
        </label>
        <label className="flex flex-col gap-1 text-sm font-semibold">
          Orden
          <OptionSelect
            className="py-2 text-sm"
            options={ORDERS}
            value={order}
            onChange={(value) => {
              setOrder(value ?? 'asc')
              setPage(0)
            }}
          />
        </label>
      </div>

      <div className="mt-6">
        <Loadable
          state={quotes}
          loadingLabel="Cargando cotizaciones…"
          errorTitle="No se pudieron cargar las cotizaciones."
          isEmpty={(data) => data.content.length === 0}
          empty={<MessageState title={status ? 'No hay cotizaciones con este estado.' : 'Aún no hay cotizaciones.'} />}
        >
          {(data) => (
            <>
              <p className="mb-4 text-sm text-muted-foreground">{data.totalElements} en total</p>
              <ul className="flex flex-col gap-4">
                {data.content.map((quote, i) => {
                  const offset = page * PAGE_SIZE + i
                  return (
                    <li key={quote.id}>
                      <QuoteCard
                        quote={quote}
                        number={newestFirst ? data.totalElements - offset : offset + 1}
                        glass={glass.status === 'ready' ? glass.data : null}
                        onUpdated={updated}
                        onDelete={() => setToDelete(quote)}
                      />
                    </li>
                  )
                })}
              </ul>
              {data.totalPages > 1 && (
                <nav aria-label="Páginas" className="mt-6 flex items-center justify-between gap-4">
                  <Button size="sm" variant="secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>
                    Anterior
                  </Button>
                  <span className="text-sm text-muted-foreground">
                    Página {page + 1} de {data.totalPages}
                  </span>
                  <Button size="sm" variant="secondary" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>
                    Siguiente
                  </Button>
                </nav>
              )}
            </>
          )}
        </Loadable>
      </div>

      <ConfirmDialog
        open={toDelete !== null}
        onOpenChange={(open) => !open && setToDelete(null)}
        title={toDelete ? `¿Eliminar la cotización de ${toDelete.name}?` : ''}
        description="Dejará de aparecer en la bandeja."
        confirmLabel="Eliminar"
        onConfirm={async () => {
          if (!toDelete) return
          await deleteQuote(toDelete.id)
          // Si era la última de la página, se vuelve a la anterior.
          if (quotes.status === 'ready' && quotes.data.content.length === 1 && page > 0) setPage(page - 1)
          reload()
        }}
      />
    </Panel>
  )
}

/** "Laminado templado · Bronce · 8 mm" con los nombres actuales de las opciones (o sus códigos si ya no existen). */
function glassText(quote: Quote, glass: GlassOptions | null): string | null {
  if (!quote.glassTypeCode || !quote.glassColorCode || !quote.thicknessMm) return null
  const type = glass?.types.find((t) => t.code === quote.glassTypeCode)?.name ?? quote.glassTypeCode
  const color = glass?.colors.find((c) => c.code === quote.glassColorCode)?.name ?? quote.glassColorCode
  return `${type} · ${color} · ${quote.thicknessMm} mm`
}

type History = { status: 'hidden' } | { status: 'loading' } | { status: 'error'; message: string } | { status: 'ready'; lines: string[] }

function QuoteCard({
  quote,
  number,
  glass,
  onUpdated,
  onDelete,
}: {
  quote: Quote
  number: number
  glass: GlassOptions | null
  onUpdated: (quote: Quote) => void
  onDelete: () => void
}) {
  const [statusError, setStatusError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [history, setHistory] = useState<History>({ status: 'hidden' })

  async function updateStatus(next: QuoteStatus | null) {
    if (!next) return
    setBusy(true)
    setStatusError(null)
    try {
      onUpdated(await changeQuoteStatus(quote.id, next))
      setHistory({ status: 'hidden' })
    } catch (error) {
      setStatusError(getErrorMessage(error))
    } finally {
      setBusy(false)
    }
  }

  async function toggleHistory() {
    if (history.status !== 'hidden') return setHistory({ status: 'hidden' })
    setHistory({ status: 'loading' })
    try {
      const detail = await getQuote(quote.id)
      setHistory({
        status: 'ready',
        lines: detail.history.map(
          (h) => `${DATE_FORMAT.format(new Date(h.changedAt))}: ${quoteStatusLabel(h.fromStatus)} → ${quoteStatusLabel(h.toStatus)}`,
        ),
      })
    } catch (error) {
      setHistory({ status: 'error', message: getErrorMessage(error) })
    }
  }

  const rows = [
    { label: 'Tipo de producto', value: quote.productType },
    { label: 'Medidas aproximadas', value: quote.dimensionsText },
    { label: 'Vidrio', value: glassText(quote, glass) },
  ]

  return (
    <article className="rounded-[20px] border border-border p-5">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-xs text-muted-foreground">
            #{number} · {DATE_FORMAT.format(new Date(quote.createdAt))}
          </p>
          <h3 className="mt-1 text-[19px] font-semibold">{quote.name}</h3>
          <a href={phoneHref(quote.phone)} className="text-link hover:underline">
            {quote.phone}
          </a>
        </div>
        <label className="flex flex-col gap-1 text-xs font-semibold">
          Estado
          <OptionSelect
            className="py-2 text-sm"
            options={QUOTE_STATUSES}
            value={quote.status}
            disabled={busy}
            onChange={(value) => void updateStatus(value)}
          />
        </label>
      </div>
      <InlineError message={statusError} className="mt-2" />
      <dl className="mt-4 grid gap-3 sm:grid-cols-2">
        {rows.map((row) => (
          <div key={row.label}>
            <dt className="text-xs text-muted-foreground">{row.label}</dt>
            <dd className="break-words">{row.value || 'Sin dato'}</dd>
          </div>
        ))}
        <div className="sm:col-span-2">
          <dt className="text-xs text-muted-foreground">Mensaje o detalles</dt>
          <dd className="break-words whitespace-pre-line">{quote.message || 'Sin dato'}</dd>
        </div>
      </dl>
      <div className="mt-4 flex flex-wrap gap-2">
        <Button size="sm" variant="ghost" aria-expanded={history.status !== 'hidden'} onClick={() => void toggleHistory()}>
          {history.status === 'hidden' ? 'Ver historial' : 'Ocultar historial'}
        </Button>
        <Button size="sm" variant="destructive" onClick={onDelete}>
          Eliminar
        </Button>
      </div>
      {history.status === 'loading' && <p className="mt-3 text-sm text-muted-foreground">Cargando historial…</p>}
      {history.status === 'error' && <InlineError message={history.message} className="mt-3" />}
      {history.status === 'ready' && (
        <ul className="mt-3 list-disc pl-5 text-sm text-muted-foreground">
          {history.lines.length === 0 ? <li>Sin cambios de estado.</li> : history.lines.map((line, i) => <li key={i}>{line}</li>)}
        </ul>
      )}
    </article>
  )
}
