import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import Badge from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import { Input } from '../../components/ui/Field'
import Icon from '../../components/ui/Icon'
import { FormError } from '../../components/ui/States'
import { DataRow, Surface, SurfaceHeader } from '../../components/ui/Surface'
import AreaPicker from '../../components/ui/AreaPicker'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { areaLabel } from '../../lib/enums'
import { formatDate } from '../../lib/format'
import { updateMe } from '../../services/users.service'

const EMPTY = {
  fullName: '',
  phoneNumber: '',
  area: '',
  address: '',
  meterNumber: '',
  avatarUrl: '',
}

/**
 * One profile page, two audiences.
 *
 * An admin needs the same underlying record — `user_profiles` is per-account,
 * not per-role — but none of the billing framing. Meter number is meaningless
 * to them, and the "you must complete this before the app works" pressure is
 * wrong: an admin is never blocked by a missing profile (see `RequireAnonymous`,
 * which routes on `isAdmin` before it ever looks at `profileComplete`).
 *
 * Branching here rather than forking the file keeps the partial-update rules,
 * the duplicate-meter handling and the area fallback in exactly one place.
 */
export default function Profile({ variant = 'customer' }) {
  const { profile, setProfile, area: accountArea, profileComplete } = useAuth()
  const toast = useToast()
  const [params, setParams] = useSearchParams()

  const isAdminView = variant === 'admin'
  // Admins are never pushed through a completion flow.
  const isFirstTime = !isAdminView && !profileComplete
  const [editing, setEditing] = useState(isFirstTime)
  const [form, setForm] = useState(EMPTY)
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  // The form mirrors the server every time the profile changes, so a
  // successful save leaves the inputs holding what was actually stored rather
  // than what was typed.
  useEffect(() => {
    if (!profile) return
    setForm({
      fullName: profile.fullName || '',
      phoneNumber: profile.phoneNumber || '',
      area: profile.area || '',
      address: profile.address || '',
      meterNumber: profile.meterNumber || '',
      avatarUrl: profile.avatarUrl || '',
    })
    setEditing((current) => current || (!isAdminView && !profile.fullName))
  }, [profile, isAdminView])

  const welcome = params.get('welcome') === '1'

  const update = (key) => (event) => {
    setForm((f) => ({ ...f, [key]: event.target.value }))
    setFieldErrors((e) => ({ ...e, [key]: undefined }))
    if (error) setError(null)
  }

  /** Only the rules the server actually enforces — no invented ones. */
  function validate() {
    const next = {}
    if (!form.fullName.trim()) {
      next.fullName = 'Required — your profile cannot be created without it.'
    } else if (form.fullName.length > 150) {
      next.fullName = 'Must be at most 150 characters.'
    }
    if (form.phoneNumber && !/^\+?[0-9]{7,15}$/.test(form.phoneNumber.trim())) {
      next.phoneNumber = '7–15 digits, optionally starting with +.'
    }
    if (form.meterNumber.length > 50) next.meterNumber = 'Must be at most 50 characters.'
    if (form.address.length > 500) next.address = 'Must be at most 500 characters.'
    // Deliberately NOT validating `area` here. The profile row requires one at
    // the database level, but the server falls back to the account's own area
    // when the request omits it — and `GET /api/users/me` reports `area` from
    // the *profile* row only, so before a profile exists the client cannot see
    // whether that fallback is available. Guessing "missing" here would block a
    // save the server would have accepted; the server's own 400 is accurate.
    setFieldErrors(next)
    return Object.keys(next).length === 0
  }

  async function onSubmit(event) {
    event.preventDefault()
    if (!validate()) return

    setBusy(true)
    setError(null)
    try {
      const saved = await updateMe(form)
      setProfile(saved)
      setEditing(false)
      if (welcome) {
        const next = new URLSearchParams(params)
        next.delete('welcome')
        setParams(next, { replace: true })
      }
      toast.success(isFirstTime ? 'Profile created.' : 'Profile updated.')
    } catch (err) {
      // A duplicate meter number belongs on the field that caused it, not in
      // a banner the user has to map back to an input themselves.
      if (err.code === 'DUPLICATE_METER') {
        setFieldErrors((e) => ({ ...e, meterNumber: err.message }))
      } else {
        setError(err.message)
      }
    } finally {
      setBusy(false)
    }
  }

  function onCancel() {
    setForm({
      fullName: profile?.fullName || '',
      phoneNumber: profile?.phoneNumber || '',
      area: profile?.area || '',
      address: profile?.address || '',
      meterNumber: profile?.meterNumber || '',
      avatarUrl: profile?.avatarUrl || '',
    })
    setFieldErrors({})
    setError(null)
    setEditing(false)
  }

  const initials = useMemo(() => {
    const source = profile?.fullName || profile?.email || ''
    return source
      .split(/[\s@.]+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join('')
  }, [profile])

  return (
    <div className="grid max-w-2xl gap-6">
      <header className="animate-rise">
        <h1 className="text-[1.7rem] font-semibold tracking-[-0.024em] text-ink">
          {isFirstTime ? 'Complete your profile' : 'My profile'}
        </h1>
        <p className="mt-1 text-[14px] text-muted">
          {isAdminView
            ? 'Your administrator account. The name here is what appears across the operations console.'
            : isFirstTime
              ? 'Your full name is required before a profile can be created. Everything else can wait.'
              : 'Account details and the connection this account is matched to.'}
        </p>
      </header>

      {welcome && isFirstTime && (
        <Surface className="animate-rise flex items-start gap-3 border-brand/25 bg-brand-soft p-4">
          <span className="mt-0.5 text-brand">
            <Icon name="check" size={18} />
          </span>
          <div>
            <p className="text-[14px] font-semibold text-brand-ink dark:text-brand">
              Account created
            </p>
            <p className="mt-0.5 text-[13px] text-brand-ink/80 dark:text-brand/80">
              One more step and you’re set up.
            </p>
          </div>
        </Surface>
      )}

      <Surface>
        <SurfaceHeader
          title="Details"
          description={
            profile?.accountCreatedAt
              ? `Member since ${formatDate(profile.accountCreatedAt)}`
              : undefined
          }
          action={
            !editing && (
              <Button variant="secondary" size="sm" onClick={() => setEditing(true)}>
                Edit
              </Button>
            )
          }
        />

        {editing ? (
          <form onSubmit={onSubmit} className="grid gap-4 p-5" noValidate>
            <FormError>{error}</FormError>

            <Input
              label="Full name"
              value={form.fullName}
              onChange={update('fullName')}
              error={fieldErrors.fullName}
              placeholder="e.g. Sohan Nur"
              autoComplete="name"
              required
            />

            <div className="grid gap-4 sm:grid-cols-2">
              <Input
                label="Phone number"
                value={form.phoneNumber}
                onChange={update('phoneNumber')}
                error={fieldErrors.phoneNumber}
                placeholder="01700000000"
                inputMode="tel"
                autoComplete="tel"
              />
              <AreaPicker
                value={form.area}
                onChange={update('area')}
                error={fieldErrors.area}
                placeholder={
                  accountArea
                    ? `Keep current (${areaLabel(accountArea)})`
                    : 'Use my registered area'
                }
                emptyOption={
                  accountArea
                    ? `Keep current (${areaLabel(accountArea)})`
                    : 'Use my registered area'
                }
                hint={
                  isAdminView
                    ? 'Required by the profile record. It does not limit what you can administer.'
                    : accountArea
                      ? undefined
                      : 'Leave this alone to use the area you registered with.'
                }
              />
            </div>

            {/* A meter number identifies a billed connection. An admin account
                is not one, so the field would only invite a pointless unique
                constraint collision. */}
            {!isAdminView && (
              <Input
                label="Meter number"
                value={form.meterNumber}
                onChange={update('meterNumber')}
                error={fieldErrors.meterNumber}
                placeholder="e.g. DESCO-4471902"
                hint="Printed on your bill. Must be unique across all accounts."
                className="tnum"
              />
            )}

            <Input
              label="Address"
              value={form.address}
              onChange={update('address')}
              error={fieldErrors.address}
              placeholder="House, road, block, area"
              autoComplete="street-address"
            />

            <Input
              label="Avatar URL"
              value={form.avatarUrl}
              onChange={update('avatarUrl')}
              error={fieldErrors.avatarUrl}
              placeholder="https://…"
              type="url"
            />

            <div className="mt-1 flex flex-wrap gap-2">
              <Button type="submit" loading={busy}>
                {isFirstTime ? 'Create profile' : 'Save changes'}
              </Button>
              {!isFirstTime && (
                <Button type="button" variant="ghost" onClick={onCancel} disabled={busy}>
                  Cancel
                </Button>
              )}
            </div>

            {/* Partial-update semantics are surprising enough to state
                outright — a blank field here does not erase what is stored. */}
            <p className="text-[12px] text-faint">
              Fields left blank keep their current value rather than being cleared.
            </p>
          </form>
        ) : (
          <div className="p-5">
            <div className="mb-4 flex items-center gap-3.5">
              {profile?.avatarUrl ? (
                <img
                  src={profile.avatarUrl}
                  alt=""
                  className="size-12 rounded-full border border-line object-cover"
                  onError={(event) => {
                    event.currentTarget.style.display = 'none'
                  }}
                />
              ) : (
                <span className="grid size-12 place-items-center rounded-full bg-brand-soft text-[15px] font-semibold text-brand-ink dark:text-brand">
                  {initials || '—'}
                </span>
              )}
              <div className="min-w-0">
                <p className="truncate text-[15px] font-semibold text-ink">
                  {profile?.fullName || '—'}
                </p>
                <p className="truncate text-[13px] text-muted">{profile?.email}</p>
              </div>
              <div className="ml-auto flex shrink-0 gap-1.5">
                {profile?.role === 'ADMIN' && <Badge tone="info">Admin</Badge>}
                <Badge tone={profile?.isActive === false ? 'critical' : 'positive'}>
                  {profile?.isActive === false ? 'Deactivated' : 'Active'}
                </Badge>
              </div>
            </div>

            <dl className="divide-y divide-line border-t border-line">
              <DataRow label="Phone" value={profile?.phoneNumber} mono />
              <DataRow label="Service area" value={areaLabel(profile?.area)} />
              {!isAdminView && (
                <DataRow label="Meter number" value={profile?.meterNumber} mono />
              )}
              <DataRow label="Address" value={profile?.address} />
              <DataRow
                label="Profile last updated"
                value={profile?.profileUpdatedAt ? formatDate(profile.profileUpdatedAt) : null}
              />
            </dl>
          </div>
        )}
      </Surface>
    </div>
  )
}
