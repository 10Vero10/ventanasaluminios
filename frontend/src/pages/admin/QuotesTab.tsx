import { useState } from 'react'

import { useAdminOutlet } from '@/components/admin/adminOutlet'
import { ConfirmDialog } from '@/components/admin/ConfirmDialog'
import { Panel } from '@/components/admin/Panel'
import { Button } from '@/components/ui/button'
import { Select } from '@/components/ui/field'
import { LoadingState, MessageState } from '@/components/ui/states'
import { phoneHref } from '@/lib/contact'
import { useLoad } from '@/lib/useLoad'
import { getAdminGlassOptions } from '@/services/api/adminCatalogService'
import { changeQuoteStatus, deleteQuote, getQuote, getQuotes } from '@/services/api/adminQuoteService'
import { getErrorMessage } from '@/services/api/httpClient'
import type { GlassOptions } from '@/types/catalog'
import { type Quote, QUOTE_STATUSES, type QuoteStatus } from '@/types/quote'

const PAGE_SIZE = 20
const DATE_FORMAT = new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'short' })

/** Bandeja de cotizaciones (briefing §3.7 y §5.7.6). */
export default function QuotesTab() {
  const { refreshDashboard } = useAdminOutlet()
  const [status, setStatus] = useState<QuoteStatus | null>(null)
  const [newestFirst, setNewestFirst] = useState(false)
  const [page, setPage] = useState(0)
  const [refresh, setRefresh] = useState(0)
  const quotes = useLoad(() => getQuotes({ status, page, size: PAGE_SIZE, newestFirst }), [status, page, newestFirst, refresh])
  const glass = useLoad(getAdminGlassOptions, [])
  const [toDelete, setToDelete] = useState<Quote | null>(null)

  function reload() {
    setRefresh((n) => n + 1)
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
          <Select
            className="py-2 text-sm"
            value={status ?? ''}
            onChange={(e) => {
              setStatus((e.target.value || null) as QuoteStatus | null)
              setPage(0)
            }}
          >
            <option value="">Todos los estados</option>
            {QUOTE_STATUSES.map((s) => (
              <option key={s.value} value={s.value}>
                {s.label}
              </option>
            ))}
          </Select>
        </label>
        <label className="flex flex-col gap-1 text-sm font-semibold">
          Orden
          <Select
            className="py-2 text-sm"
            value={newestFirst ? 'desc' : 'asc'}
            onChange={(e) => {
              setNewestFirst(e.target.value === 'desc')
              setPage(0)
            }}
          >
            <option value="asc">Más antiguas primero</option>
            <option value="desc">Más recientes primero</option>
          </Select>
        </label>
      </div>

      <div className="mt-6">
        {quotes.status === 'loading' && <LoadingState label="Cargando cotizaciones…" />}
        {quotes.status === 'error' && (
          <MessageState
            tone="error"
            title="No se pudieron cargar las cotizaciones."
            description={quotes.message}
            action={<Button onClick={quotes.reload}>Intentar de nuevo</Button>}
          />
        )}
        {quotes.status === 'ready' && quotes.data.content.length === 0 && (
          <MessageState title={status ? 'No hay cotizaciones con este estado.' : 'Aún no hay cotizaciones.'} />
        )}
        {quotes.status === 'ready' && quotes.data.content.length > 0 && (
          <>
            <p className="mb-4 text-sm text-muted-foreground">{quotes.data.totalElements} en total</p>
            <ul className="flex flex-col gap-4">
              {quotes.data.content.map((quote, i) => {
                const offset = page * PAGE_SIZE + i
                const number = newestFirst ? quotes.data.totalElements - offset : offset + 1
                return (
                  <li key={`${quote.id}-${quote.updatedAt}`}>
                    <QuoteCard
                      quote={quote}
                      number={number}
                      glass={glass.status === 'ready' ? glass.data : null}
                      onChanged={refreshDashboard}
                      onDelete={() => setToDelete(quote)}
                    />
                  </li>
                )
              })}
            </ul>
            {quotes.data.totalPages > 1 && (
              <nav aria-label="Páginas" className="mt-6 flex items-center justify-between gap-4">
                <Button size="sm" variant="secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>
                  Anterior
                </Button>
                <span className="text-sm text-muted-foreground">
                  Página {page + 1} de {quotes.data.totalPages}
                </span>
                <Button
                  size="sm"
                  variant="secondary"
                  disabled={page + 1 >= quotes.data.totalPages}
                  onClick={() => setPage(page + 1)}
                >
                  Siguiente
                </Button>
              </nav>
            )}
          </>
        )}
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
          const lastOnPage = quotes.status === 'ready' && quotes.data.content.length === 1 && page > 0
          if (lastOnPage) setPage(page - 1)
          reload()
        }}
      />
    </Panel>
  )
}

function glassText(quote: Quote, glass: GlassOptions | null): string | null {
  if (!quote.glassTypeCode || !quote.glassColorCode || !quote.thicknessMm) return null
  const type = glass?.types.find((t) => t.code === quote.glassTypeCode)?.name ?? quote.glassTypeCode
  const color = glass?.colors.find((c) => c.code === quote.glassColorCode)?.name ?? quote.glassColorCode
  return `${type} · ${color} · ${quote.thicknessMm} mm`
}

function QuoteCard({
  quote: initial,
  number,
  glass,
  onChanged,
  onDelete,
}: {
  quote: Quote
  number: number
  glass: GlassOptions | null
  onChanged: () => void
  onDelete: () => void
}) {
  const [quote, setQuote] = useState(initial)
  const [statusError, setStatusError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [history, setHistory] = useState<string[] | null>(null)

  async function updateStatus(next: QuoteStatus) {
    setBusy(true)
    setStatusError(null)
    try {
      setQuote(await changeQuoteStatus(quote.id, next))
      setHistory(null)
      onChanged()
    } catch (error) {
      setStatusError(getErrorMessage(error))
    } finally {
      setBusy(false)
    }
  }

  async function toggleHistory() {
    if (history) {
      setHistory(null)
      return
    }
    try {
      const detail = await getQuote(quote.id)
      setHistory(
        detail.history.length === 0
          ? ['Sin cambios de estado.']
          : detail.history.map((h) => {
              const from = QUOTE_STATUSES.find((s) => s.value === h.fromStatus)?.label
              const to = QUOTE_STATUSES.find((s) => s.value === h.toStatus)?.label
              return `${DATE_FORMAT.format(new Date(h.changedAt))}: ${from} → ${to}`
            }),
      )
    } catch (error) {
      setHistory([getErrorMessage(error)])
    }
  }

  const rows: { label: string; value: string | null }[] = [
    { label: 'Tipo de producto', value: quote.productType },
    { label: 'Medidas aproximadas', value: quote.dimensionsText },
    { label: 'Vidrio', value: glassText(quote, glass) },
    { label: 'Mensaje o detalles', value: quote.message },
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
          <Select
            className="py-2 text-sm"
            value={quote.status}
            disabled={busy}
            onChange={(e) => void updateStatus(e.target.value as QuoteStatus)}
          >
            {QUOTE_STATUSES.map((s) => (
              <option key={s.value} value={s.value}>
                {s.label}
              </option>
            ))}
          </Select>
        </label>
      </div>
      {statusError && (
        <p role="alert" className="mt-2 text-sm text-destructive">
          {statusError}
        </p>
      )}
      <dl className="mt-4 grid gap-3 sm:grid-cols-2">
        {rows.map((row) => (
          <div key={row.label} className={row.label === 'Mensaje o detalles' ? 'sm:col-span-2' : undefined}>
            <dt className="text-xs text-muted-foreground">{row.label}</dt>
            <dd className="break-words whitespace-pre-line">{row.value || 'Sin dato'}</dd>
          </div>
        ))}
      </dl>
      <div className="mt-4 flex flex-wrap gap-2">
        <Button size="sm" variant="ghost" aria-expanded={history !== null} onClick={() => void toggleHistory()}>
          {history ? 'Ocultar historial' : 'Ver historial'}
        </Button>
        <Button size="sm" variant="destructive" onClick={onDelete}>
          Eliminar
        </Button>
      </div>
      {history && (
        <ul className="mt-3 list-disc pl-5 text-sm text-muted-foreground">
          {history.map((line) => (
            <li key={line}>{line}</li>
          ))}
        </ul>
      )}
    </article>
  )
}
