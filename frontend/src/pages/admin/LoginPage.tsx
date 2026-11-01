import { type FormEvent, useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router'

import { useAuth } from '@/auth/authContext'
import { AuthCard } from '@/components/admin/AuthCard'
import { Button } from '@/components/ui/button'
import { Field, Input } from '@/components/ui/field'
import { getErrorMessage } from '@/services/api/httpClient'

/** Ingreso al panel con correo y contraseña (briefing §5.7.1). */
export default function LoginPage() {
  const { user, expired, signIn } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from ?? '/admin'
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [sending, setSending] = useState(false)

  if (user) return <Navigate to={user.mustChangePassword ? '/admin/cambiar-clave' : from} replace />

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!email.trim() || !password) {
      setError('Escribe tu correo y tu contraseña.')
      return
    }
    setSending(true)
    setError(null)
    try {
      const admin = await signIn(email.trim(), password)
      navigate(admin.mustChangePassword ? '/admin/cambiar-clave' : from, { replace: true })
    } catch (err) {
      setError(getErrorMessage(err))
      setSending(false)
    }
  }

  return (
    <AuthCard title="Panel de control" description="Ingresa con tu correo y contraseña para editar el sitio.">
      {expired && !error && (
        <p role="status" className="mb-5 rounded-[12px] bg-secondary p-3 text-sm">
          Tu sesión expiró, vuelve a ingresar.
        </p>
      )}
      <form noValidate onSubmit={handleSubmit} className="flex flex-col gap-5">
        <Field label="Correo">
          {(props) => (
            <Input
              {...props}
              type="email"
              autoComplete="username"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          )}
        </Field>
        <Field label="Contraseña">
          {(props) => (
            <Input
              {...props}
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          )}
        </Field>
        {error && (
          <p role="alert" className="text-sm text-destructive">
            {error}
          </p>
        )}
        <Button type="submit" size="lg" disabled={sending}>
          {sending ? 'Ingresando…' : 'Entrar al panel'}
        </Button>
      </form>
    </AuthCard>
  )
}
