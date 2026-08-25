import { useState } from 'react'
import { Link } from 'react-router-dom'
import Button from '../../components/ui/Button'
import { Input } from '../../components/ui/Field'
import { FormError } from '../../components/ui/States'
import { useAuth } from '../../context/AuthContext'

export default function Login() {
  const { signIn } = useAuth()

  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  const update = (key) => (event) => {
    setForm((f) => ({ ...f, [key]: event.target.value }))
    // Clearing on edit means the error describes the current attempt, never
    // the previous one.
    if (error) setError(null)
  }

  async function onSubmit(event) {
    event.preventDefault()
    setBusy(true)
    setError(null)
    try {
      // No navigation here: RequireAnonymous sends the user on once the
      // profile (and therefore the role) has resolved. Navigating from this
      // component as well would race that redirect and lose.
      await signIn(form)
    } catch (err) {
      setError(err.message)
      setBusy(false)
    }
  }

  return (
    <div className="animate-rise">
      <h1 className="text-[1.6rem] font-semibold tracking-[-0.022em] text-ink">Sign in</h1>
      <p className="mt-1.5 text-[14px] text-muted">
        Access your bills, outages and complaints.
      </p>

      <form onSubmit={onSubmit} className="mt-7 grid gap-4" noValidate>
        <FormError>{error}</FormError>

        <Input
          label="Email"
          type="email"
          name="email"
          autoComplete="email"
          placeholder="you@example.com"
          value={form.email}
          onChange={update('email')}
          required
        />
        <Input
          label="Password"
          type="password"
          name="password"
          autoComplete="current-password"
          placeholder="••••••••"
          value={form.password}
          onChange={update('password')}
          required
        />

        <Button type="submit" size="lg" loading={busy} className="mt-1 w-full">
          Sign in
        </Button>
      </form>

      <p className="mt-6 text-[13.5px] text-muted">
        No account yet?{' '}
        <Link
          to="/register"
          className="font-medium text-brand underline-offset-2 hover:underline"
        >
          Create one
        </Link>
      </p>
    </div>
  )
}
