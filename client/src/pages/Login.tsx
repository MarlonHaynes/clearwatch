import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

export function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('admin@clearwatch.dev')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      await login(email, password)
      navigate('/')
    } catch {
      setError('Invalid email or password')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="login-screen">
      <form className="login-card" onSubmit={handleSubmit}>
        <div className="brand login-brand">
          <span className="brand-mark">◎</span> ClearWatch
        </div>
        <p className="login-sub">Service health, alerting &amp; log search — one product.</p>

        <label>Email</label>
        <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />

        <label>Password</label>
        <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" required />

        {error && <div className="form-error">{error}</div>}

        <button type="submit" disabled={submitting} className="btn-primary">
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>

        <p className="login-hint">
          Seeded admin credentials are in <code>.env.example</code> (<code>ADMIN_EMAIL</code> /{' '}
          <code>ADMIN_PASSWORD</code>).
        </p>
      </form>
    </div>
  )
}
