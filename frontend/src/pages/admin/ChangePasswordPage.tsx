import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router'

import { useAuth } from '@/auth/authContext'
import { AuthCard } from '@/components/admin/AuthCard'
import { Button } from '@/components/ui/button'
import { Field, Input } from '@/components/ui/field'
import { getErrorMessage } from '@/services/api/httpClient'

const MIN_LENGTH = 12
const MAX_BYTES = 72

/** Cambio de contraseña; obligatorio en el primer ingreso con la contraseña temporal (ADR-005). */
export default function ChangePasswordPage() {
  const { user, changePassword } = useAuth()
  const navigate = useNavigate()
  const [current, setCurrent] = useState('')
  const [next, setNext] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [sending, setSending] = useState(false)
  const required = user?.mustChangePassword ?? false

  function validate(): string | null {
    if (!current || !next) return 'Escribe la contraseña actual y la nueva.'
    if (next.length < MIN_LENGTH) return `La nueva contraseña debe tener al menos ${MIN_LENGTH} caracteres.`
    if (new TextEncoder().encode(next).length > MAX_BYTES) return 'La nueva contraseña es demasiado larga.'
    if (next === current) return 'La nueva contraseña debe ser distinta de la actual.'
    if (next !== confirm) return 'La confirmación no coincide con la nueva contraseña.'
    return null
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const problem = validate()
    if (problem) {
      setError(problem)
      return
    }
    setSending(true)
    setError(null)
    try {
      await changePassword(current, next)
      navigate('/admin', { replace: true, state: { passwordChanged: true } })
    } catch (err) {
      setError(getErrorMessage(err))
      setSending(false)
    }
  }

  return (
    <AuthCard
      title={required ? 'Crea tu contraseña' : 'Cambiar contraseña'}
      description={
        required
          ? 'Estás usando una contraseña temporal. Elige una nueva para entrar al panel.'
          : 'Al cambiarla se cierran las demás sesiones abiertas.'
      }
    >
      <form noValidate onSubmit={handleSubmit} className="flex flex-col gap-5">
        <Field label={required ? 'Contraseña temporal' : 'Contraseña actual'}>
          {(props) => (
            <Input {...props} type="password" autoComplete="current-password" value={current} onChange={(e) => setCurrent(e.target.value)} />
          )}
        </Field>
        <Field label="Nueva contraseña" hint={`Mínimo ${MIN_LENGTH} caracteres.`}>
          {(props) => (
            <Input {...props} type="password" autoComplete="new-password" value={next} onChange={(e) => setNext(e.target.value)} />
          )}
        </Field>
        <Field label="Confirmar nueva contraseña">
          {(props) => (
            <Input {...props} type="password" autoComplete="new-password" value={confirm} onChange={(e) => setConfirm(e.target.value)} />
          )}
        </Field>
        {error && (
          <p role="alert" className="text-sm text-destructive">
            {error}
          </p>
        )}
        <Button type="submit" size="lg" disabled={sending}>
          {sending ? 'Guardando…' : 'Guardar contraseña'}
        </Button>
      </form>
    </AuthCard>
  )
}
