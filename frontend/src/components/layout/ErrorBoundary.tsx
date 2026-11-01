import { Component, type ErrorInfo, type ReactNode } from 'react'

import { Button } from '@/components/ui/button'
import { MessageState } from '@/components/ui/states'

/**
 * Última red: si algo falla al pintar, se muestra un mensaje con "Recargar" en vez de una página en blanco.
 * React solo permite capturar errores de render con un componente de clase.
 */
export class ErrorBoundary extends Component<{ children: ReactNode }, { failed: boolean }> {
  state = { failed: false }

  static getDerivedStateFromError() {
    return { failed: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Error al mostrar la página', error, info.componentStack)
  }

  render() {
    if (!this.state.failed) return this.props.children
    return (
      <main className="mx-auto max-w-xl px-6 py-24">
        <MessageState
          tone="error"
          title="Algo salió mal al mostrar esta página"
          description="Recarga la página. Si el problema sigue, intenta más tarde."
          action={<Button onClick={() => window.location.reload()}>Recargar</Button>}
        />
      </main>
    )
  }
}
