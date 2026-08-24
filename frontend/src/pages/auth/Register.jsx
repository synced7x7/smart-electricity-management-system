import { useState } from 'react'
import { Link } from 'react-router-dom'
import Button from '../../components/ui/Button'
import { Input } from '../../components/ui/Field'
import { FormError } from '../../components/ui/States'
import AreaPicker from '../../components/ui/AreaPicker'
import { useAuth } from '../../context/AuthContext'

export default function Register() {
  const { signUp } = useAuth()

  const [form, setForm] = useState({
    email: '',
    password: '',
    confirm: '',
    area: '',
    adminKey: '',
  })
  // Kept behind a disclosure so the ordinary signup stays a four-field form.
  // Hiding it is not a security measure — the key is the security measure —
  // it just keeps an option irrelevant to almost every visitor out of the way.
  const [claimingAdmin, setClaimingAdmin] = useState(false)
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  const update = (key) => (event) => {
    setForm((f) => ({ ...f, [key]: event.target.value }))
    setFieldErrors((e) => ({ ...e, [key]: undefined }))
    if (error) setError(null)
  }

  /**
   * Validated inline before the request, not after: the password mismatch and
   * length rules are things the browser already knows, and a round trip to
   * learn them wastes the user's time.
   */
  function validate() {
    const next = {}
    if (!form.email.includes('@')) next.email = 'Enter a valid email address.'
    if (form.password.length < 8) next.password = 'Use at least 8 characters.'
    if (form.confirm !== form.password) next.confirm = 'Passwords do not match.'
    setFieldErrors(next)
    return Object.keys(next).length === 0
  }

  async function onSubmit(event) {
    event.preventDefault()
    if (!validate()) return

    setBusy(true)
    setError(null)
    try {
      // A new account has no profile row yet, so RequireAnonymous routes to
      // the completion flow rather than to an overview with nothing in it.
      await signUp({
        email: form.email,
        password: form.password,
        area: form.area,
        adminKey: claimingAdmin ? form.adminKey : '',
      })
    } catch (err) {
      // A rejected key belongs on the key field, not in a banner at the top of
      // a form whose other fields were all fine.
      if (err.code === 'BAD_ADMIN_KEY') {
        setFieldErrors((e) => ({ ...e, adminKey: err.message }))
      } else {
        setError(err.message)
      }
      setBusy(false)
    }
  }

  return (
    <div className="animate-rise">
      <h1 className="text-[1.6rem] font-semibold tracking-[-0.022em] text-ink">
        Create your account
      </h1>
      <p className="mt-1.5 text-[14px] text-muted">
        {claimingAdmin
          ? 'An administrator key is required. Without it, use the customer form.'
          : 'Takes a minute. You can add your meter details afterwards.'}
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
          error={fieldErrors.email}
          required
        />

        {/* A database enum, so this is a picker rather than free text — but it
            now spans all 64 districts, which is too many to scan in a plain
            dropdown. */}
        <AreaPicker
          name="area"
          value={form.area}
          onChange={update('area')}
          placeholder="Select later"
          emptyOption="Select later"
          hint="Determines which outage alerts you receive. You can change it later."
        />

        <Input
          label="Password"
          type="password"
          name="password"
          autoComplete="new-password"
          placeholder="At least 8 characters"
          value={form.password}
          onChange={update('password')}
          error={fieldErrors.password}
          required
        />
        <Input
          label="Confirm password"
          type="password"
          name="confirm"
          autoComplete="new-password"
          value={form.confirm}
          onChange={update('confirm')}
          error={fieldErrors.confirm}
          required
        />

        {claimingAdmin ? (
          <Input
            label="Administrator key"
            type="password"
            name="adminKey"
            autoComplete="off"
            placeholder="Provided by whoever runs this deployment"
            value={form.adminKey}
            onChange={update('adminKey')}
            error={fieldErrors.adminKey}
            hint="Creates an administrator account instead of a customer one."
          />
        ) : (
          <button
            type="button"
            onClick={() => setClaimingAdmin(true)}
            className="justify-self-start text-[13px] text-muted underline-offset-2 transition-colors duration-150 hover:text-ink hover:underline"
          >
            Registering as an administrator?
          </button>
        )}

        <Button type="submit" size="lg" loading={busy} className="mt-1 w-full">
          {claimingAdmin ? 'Create administrator account' : 'Create account'}
        </Button>
      </form>

      <p className="mt-6 text-[13.5px] text-muted">
        Already registered?{' '}
        <Link to="/login" className="font-medium text-brand underline-offset-2 hover:underline">
          Sign in
        </Link>
      </p>
    </div>
  )
}
