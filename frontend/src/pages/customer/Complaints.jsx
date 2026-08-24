import { useEffect, useState } from 'react'
import Badge from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import { Input, Select, Textarea } from '../../components/ui/Field'
import Icon from '../../components/ui/Icon'
import { EmptyState, ErrorState, FormError, SkeletonRows } from '../../components/ui/States'
import { Surface, SurfaceHeader } from '../../components/ui/Surface'
import AreaPicker from '../../components/ui/AreaPicker'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { COMPLAINT_CATEGORIES, areaLabel, label } from '../../lib/enums'
import { formatDateTime, relativeTime } from '../../lib/format'
import { useAsync } from '../../lib/useAsync'
import { getMyComplaints, submitComplaint } from '../../services/complaints.service'

const EMPTY = { category: 'POWER_CUT', title: '', description: '', area: '' }

export default function Complaints() {
  const { userId, area: accountArea } = useAuth()
  const toast = useToast()

  const complaints = useAsync(() => getMyComplaints(userId), [userId], {
    enabled: Boolean(userId),
  })

  const [form, setForm] = useState({ ...EMPTY, area: accountArea || '' })
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const [open, setOpen] = useState(false)

  // On a cold load of this page the profile has not resolved yet, so the
  // initialiser above sees no area. Adopt it once it arrives — but only while
  // the field is untouched, so this never overwrites a deliberate choice.
  useEffect(() => {
    if (accountArea) setForm((f) => (f.area ? f : { ...f, area: accountArea }))
  }, [accountArea])

  const update = (key) => (event) => {
    setForm((f) => ({ ...f, [key]: event.target.value }))
    setFieldErrors((e) => ({ ...e, [key]: undefined }))
    if (error) setError(null)
  }

  function validate() {
    const next = {}
    if (!form.title.trim()) next.title = 'Give this a short title.'
    if (!form.description.trim()) next.description = 'Describe what happened.'
    if (!form.area) next.area = 'Select the area this affects.'
    setFieldErrors(next)
    return Object.keys(next).length === 0
  }

  async function onSubmit(event) {
    event.preventDefault()
    if (!validate()) return

    setBusy(true)
    setError(null)
    try {
      // `userId` comes from the signed-in session, never from a form field —
      // complaint-service trusts the body for identity (backend issue #21),
      // so this is the one place that decides whose name a complaint carries.
      await submitComplaint({ userId, ...form })
      toast.success('Complaint submitted. You’ll see updates here.')
      setForm({ ...EMPTY, area: accountArea || '' })
      setOpen(false)
      complaints.reload()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const rows = complaints.data || []

  return (
    <div className="grid gap-6">
      <header className="animate-rise flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-[1.7rem] font-semibold tracking-[-0.024em] text-ink">
            Complaints
          </h1>
          <p className="mt-1 text-[14px] text-muted">
            Report a problem and follow it through to a resolution.
          </p>
        </div>
        {!open && (
          <Button onClick={() => setOpen(true)}>
            <Icon name="plus" size={17} />
            New complaint
          </Button>
        )}
      </header>

      {open && (
        <Surface className="animate-rise">
          <SurfaceHeader
            title="Report a problem"
            action={
              <Button variant="ghost" size="sm" onClick={() => setOpen(false)} disabled={busy}>
                Cancel
              </Button>
            }
          />
          <form onSubmit={onSubmit} className="grid gap-4 p-5" noValidate>
            <FormError>{error}</FormError>

            <div className="grid gap-4 sm:grid-cols-2">
              <Select label="Category" value={form.category} onChange={update('category')}>
                {COMPLAINT_CATEGORIES.map((value) => (
                  <option key={value} value={value}>
                    {label(value)}
                  </option>
                ))}
              </Select>
              <AreaPicker
                label="Area"
                value={form.area}
                onChange={update('area')}
                error={fieldErrors.area}
                placeholder="Select an area"
                required
              />
            </div>

            <Input
              label="Title"
              value={form.title}
              onChange={update('title')}
              error={fieldErrors.title}
              placeholder="e.g. Repeated voltage drops in the evening"
              required
            />

            <Textarea
              label="What happened?"
              value={form.description}
              onChange={update('description')}
              error={fieldErrors.description}
              placeholder="When it started, how often it happens, and anything already tried."
              rows={5}
              required
            />

            <Button type="submit" loading={busy} className="justify-self-start">
              Submit complaint
            </Button>
          </form>
        </Surface>
      )}

      <Surface>
        <SurfaceHeader title="Your complaints" />
        {complaints.loading ? (
          <div className="p-5">
            <SkeletonRows rows={3} />
          </div>
        ) : complaints.error ? (
          <ErrorState error={complaints.error} onRetry={complaints.reload} />
        ) : rows.length === 0 ? (
          <EmptyState
            title="Nothing reported"
            description="When you report a problem it appears here with its current status and any reply from the operations team."
            action={
              !open && (
                <Button size="sm" onClick={() => setOpen(true)}>
                  Report a problem
                </Button>
              )
            }
          />
        ) : (
          <ul className="divide-y divide-line">
            {rows.map((complaint) => (
              <li key={complaint.id} className="px-5 py-4">
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <div className="min-w-0">
                    <h3 className="text-[14.5px] font-semibold text-ink">{complaint.title}</h3>
                    <p className="mt-0.5 text-[12px] text-faint">
                      {complaint.category ? `${label(complaint.category)} · ` : ''}
                      {areaLabel(complaint.area)} · raised {relativeTime(complaint.createdAt)}
                    </p>
                  </div>
                  <Badge value={complaint.status} />
                </div>

                <p className="mt-2 text-[13px] leading-relaxed text-muted">
                  {complaint.description}
                </p>

                {/* The reply is the whole reason the user came back to this
                    page — it gets its own block, not a metadata line. */}
                {complaint.remark && (
                  <div className="mt-3 rounded-lg border-l-[3px] border-l-brand bg-sunken px-3.5 py-3">
                    <p className="text-[11.5px] font-semibold tracking-[0.04em] text-muted uppercase">
                      Response from operations
                    </p>
                    <p className="mt-1 text-[13px] leading-relaxed text-ink">
                      {complaint.remark}
                    </p>
                    <p className="mt-1.5 text-[11.5px] text-faint">
                      Updated {formatDateTime(complaint.updatedAt)}
                    </p>
                  </div>
                )}
              </li>
            ))}
          </ul>
        )}
      </Surface>
    </div>
  )
}
