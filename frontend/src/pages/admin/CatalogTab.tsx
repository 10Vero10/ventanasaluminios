import { useState } from 'react'

import { useAdminOutlet } from '@/components/admin/adminOutlet'
import { ConfirmDialog } from '@/components/admin/ConfirmDialog'
import { MoveButtons } from '@/components/admin/MoveButtons'
import { Panel } from '@/components/admin/Panel'
import { ProductEditor } from '@/components/admin/ProductEditor'
import { useOrderedList } from '@/components/admin/useOrderedList'
import { CategoryFilter } from '@/components/site/CategoryFilter'
import { Button } from '@/components/ui/button'
import { Dialog } from '@/components/ui/dialog'
import { Loadable } from '@/components/ui/Loadable'
import { InlineError, MessageState } from '@/components/ui/states'
import { deleteProduct, duplicateProduct, getAdminProducts, reorderProducts } from '@/services/api/adminCatalogService'
import { getErrorMessage } from '@/services/api/httpClient'
import { categoryLabel, type Product, type ProductCategory } from '@/types/catalog'

type Editing = { product: Product | null }

/** Pestaña Catálogo (briefing §5.7.4). */
export default function CatalogTab() {
  const { refreshDashboard } = useAdminOutlet()
  const list = useOrderedList(getAdminProducts, reorderProducts)
  const [filter, setFilter] = useState<ProductCategory | null>(null)
  const [editing, setEditing] = useState<Editing | null>(null)
  const [toDelete, setToDelete] = useState<Product | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const all = list.items ?? []
  const visible = filter ? all.filter((p) => p.category === filter) : all

  /** Mover dentro de la vista filtrada equivale a intercambiar con el vecino visible en la lista completa. */
  function moveVisible(from: number, to: number) {
    const fullFrom = all.indexOf(visible[from])
    const fullTo = all.indexOf(visible[to])
    void list.move(fullFrom, fullTo)
  }

  async function duplicate(product: Product) {
    setActionError(null)
    try {
      await duplicateProduct(product.id)
      list.reload()
      refreshDashboard()
    } catch (error) {
      setActionError(getErrorMessage(error))
    }
  }

  return (
    <Panel
      title="Catálogo de productos"
      description="Agrega, edita, duplica o elimina productos. Los ocultos no aparecen en el sitio."
      actions={
        <Button size="sm" onClick={() => setEditing({ product: null })}>
          {filter ? `Agregar en ${categoryLabel(filter)}` : 'Agregar producto'}
        </Button>
      }
    >
      <CategoryFilter value={filter} onChange={setFilter} allLabel="Todos" label="Filtrar productos por línea" />
      <div className="mt-6">
        <InlineError message={list.error} className="mb-4" />
        <InlineError message={actionError} className="mb-4" />
        <Loadable state={list.loaded} loadingLabel="Cargando productos…" errorTitle="No se pudieron cargar los productos">
          {() => (
            <>
              <p className="mb-3 text-sm text-muted-foreground">
                {visible.length} {visible.length === 1 ? 'producto' : 'productos'}
              </p>
              {visible.length === 0 ? (
                <MessageState title="No hay productos en esta línea." />
              ) : (
                <ul className="flex flex-col gap-3">
                  {visible.map((product, index) => (
                    <li key={product.id} className="grid grid-cols-[72px_1fr] items-center gap-x-3 gap-y-3 rounded-[20px] border border-border p-3 sm:grid-cols-[96px_1fr_auto] sm:gap-4 sm:p-4">
                      {product.imageUrl ? (
                        <img src={product.imageUrl} alt="" className="aspect-[16/10] w-full rounded-[12px] bg-secondary object-cover" loading="lazy" />
                      ) : (
                        <div className="aspect-[16/10] w-full rounded-[12px] bg-secondary" />
                      )}
                      <div>
                        <p className="text-xs text-muted-foreground">
                          {categoryLabel(product.category)} · {product.specs.length} filas de ficha técnica
                          {!product.active && <span className="ml-2 font-semibold text-status">Oculto</span>}
                        </p>
                        <p className="text-[17px] font-semibold">{product.title}</p>
                        <p className="text-sm text-muted-foreground">{product.subtitle}</p>
                      </div>
                      <div className="col-span-2 flex flex-wrap gap-2 sm:col-span-1">
                        <MoveButtons label={product.title} index={index} count={visible.length} onMove={moveVisible} disabled={list.busy} />
                        <Button size="sm" variant="secondary" onClick={() => setEditing({ product })}>
                          Editar
                        </Button>
                        <Button size="sm" variant="secondary" onClick={() => void duplicate(product)}>
                          Duplicar
                        </Button>
                        <Button size="sm" variant="destructive" onClick={() => setToDelete(product)}>
                          Eliminar
                        </Button>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </>
          )}
        </Loadable>
      </div>

      <Dialog
        open={editing !== null}
        onOpenChange={(open) => !open && setEditing(null)}
        title={editing?.product ? `Editar ${editing.product.title}` : 'Nuevo producto'}
        className="sm:w-[min(860px,calc(100vw-48px))]"
      >
        {editing && (
          <ProductEditor
            product={editing.product}
            newCategory={filter ?? 'VENTANAS'}
            onCancel={() => setEditing(null)}
            onSaved={() => {
              setEditing(null)
              list.reload()
              refreshDashboard()
            }}
          />
        )}
      </Dialog>

      <ConfirmDialog
        open={toDelete !== null}
        onOpenChange={(open) => !open && setToDelete(null)}
        title="¿Eliminar este producto del catálogo?"
        description={toDelete?.title}
        confirmLabel="Eliminar producto"
        onConfirm={async () => {
          if (!toDelete) return
          await deleteProduct(toDelete.id)
          list.reload()
          refreshDashboard()
        }}
      />
    </Panel>
  )
}
