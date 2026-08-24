import { useState } from 'react'
import Badge, { LiveBadge } from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import { Input, Select, Textarea } from '../../components/ui/Field'
import Icon from '../../components/ui/Icon'
import Modal from '../../components/ui/Modal'
import { EmptyState, ErrorState, FormError, SkeletonRows } from '../../components/ui/States'
import { Surface } from '../../components/ui/Surface'
import { Pagination, TableWrap, Td, Th, Tr } from '../../components/ui/Table'
import AreaPicker from '../../components/ui/AreaPicker'
import { useToast } from '../../context/ToastContext'
import { OUTAGE_STATUSES, OUTAGE_TYPES, areaLabel, label } from '../../lib/enums'
import { formatDateTime, toLocalInput } from '../../lib/format'
import { useAsync } from '../../lib/useAsync'
import {
  createOutage,
  listOutagesForAdmin,
  updateOutageStatus,
} from '../../services/outages.service'

/** Two hours from now, rounded to the hour — a sane default window start. */
function defaultWindow() {
  const start = new Date()
  start.setHours(start.getHours() + 2, 0, 0, 0)
  const end = new Date(start)
  end.setHours(end.getHours() + 4)
  return { start: toLocalInput(start.toISOString()), end: toLocalInput(end.toISOString()) }
}

export default function AdminOutages() {
  const toast = useToast()
  const [status, setStatus] = useState('')
  const [area, setArea] = useState('')
  const [page, setPage] = useState(0)
  const [composing, setComposing] = useState(false)
  const [pending, setPending] = useState(null)

  const outages = useAsync(
    () => listOutagesForAdmin({ status, area, page, size: 10 }),
    [status, area, page],
  )
  const rows = outages.data?.content || []

  async function onStatusChange(outage, next) {
    setPending(outage.id)
    try {
      const updated = await updateOutageStatus(outage.id, next)
      outages.setData((current) => ({
        ...current,
        content: (current?.content || []).map((o) => (o.id === updated.id ? updated : o)),
      }))
      toast.success(`“${updated.title}” is now ${label(updated.status).toLowerCase()}.`)
    } catch (error) {
      toast.error(error.message)
    } finally {
      setPending(null)
    }
  }

  return (
    <div className="grid gap-5">
      <header className="animate-rise flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-[1.55rem] font-semibold tracking-[-0.022em] text-ink">
            Outage scheduling
          </h1>
          <p className="mt-1 text-[13.5px] text-muted">
            Schedule maintenance, declare emergencies, and move outages through
            their lifecycle.
          </p>
        </div>
        <Button onClick={() => setComposing(true)}>
          <Icon name="plus" size={17} />
          Schedule outage
        </Button>
      </header>

      <div className="flex flex-wrap gap-3">
        <Select
          value={status}
          onChange={(event) => {
            setStatus(event.target.value)
            setPage(0)
          }}
          className="w-44"
          aria-label="Filter by status"
        >
          <option value="">All statuses</option>
          {OUTAGE_STATUSES.map((value) => (
            <option key={value} value={value}>
              {label(value)}
            </option>
          ))}
        </Select>
        <AreaPicker
          label={null}
          value={area}
          onChange={(event) => {
            setArea(event.target.value)
            setPage(0)
          }}
          placeholder="All areas"
          emptyOption="All areas"
          className="w-56"
        />
      </div>

      <Surface className="overflow-hidden">
        {outages.loading ? (
          <div className="p-5">
            <SkeletonRows rows={5} />
          </div>
        ) : outages.error ? (
          <ErrorState error={outages.error} onRetry={outages.reload} />
        ) : rows.length === 0 ? (
          <EmptyState
            title="No outages match"
            description="Clear the filters, or schedule one."
          />
        ) : (
          <>
            <TableWrap>
              <thead>
                <tr>
                  <Th>Outage</Th>
                  <Th>Area</Th>
                  <Th>Window</Th>
                  <Th>Status</Th>
                  <Th align="right">Move to</Th>
                </tr>
              </thead>
              <tbody>
                {rows.map((outage) => (
                  <Tr key={outage.id}>
                    <Td>
                      <div className="flex items-center gap-2">
                        <span className="font-medium text-ink">{outage.title}</span>
                        {outage.type === 'EMERGENCY' && (
                          <Badge tone="critical">Emergency</Badge>
                        )}
                      </div>
                      <span className="line-clamp-1 block max-w-md text-[11.5px] text-faint">
                        {outage.detail}
                      </span>
                    </Td>
                    <Td>{areaLabel(outage.area)}</Td>
                    <Td mono className="whitespace-nowrap text-[12.5px]">
                      {formatDateTime(outage.startTime)}
                      <span className="block text-faint">
                        → {formatDateTime(outage.endTime)}
                      </span>
                    </Td>
                    <Td>
                      {outage.status === 'ONGOING' ? (
                        <LiveBadge />
                      ) : (
                        <Badge value={outage.status} />
                      )}
                    </Td>
                    <Td align="right">
                      {/* Terminal states are terminal — offering a control
                          that would only ever fail is worse than no control. */}
                      {outage.status === 'RESOLVED' || outage.status === 'CANCELLED' ? (
                        <span className="text-[12.5px] text-faint">Closed</span>
                      ) : (
                        <Select
                          value=""
                          disabled={pending === outage.id}
                          onChange={(event) =>
                            event.target.value && onStatusChange(outage, event.target.value)
                          }
                          className="ml-auto w-36"
                          aria-label={`Change status of ${outage.title}`}
                        >
                          <option value="">Change…</option>
                          {OUTAGE_STATUSES.filter((s) => s !== outage.status).map((value) => (
                            <option key={value} value={value}>
                              {label(value)}
                            </option>
                          ))}
                        </Select>
                      )}
                    </Td>
                  </Tr>
                ))}
              </tbody>
            </TableWrap>
            <Pagination {...outages.data} onPage={setPage} />
          </>
        )}
      </Surface>

      <ScheduleModal
        open={composing}
        onClose={() => setComposing(false)}
        onCreated={() => {
          setComposing(false)
          setPage(0)
          outages.reload()
        }}
      />
    </div>
  )
}

function ScheduleModal({ open, onClose, onCreated }) {
  const toast = useToast()
  const initial = defaultWindow()
  const [form, setForm] = useState({
    title: '',
    description: '',
    area: '',
    type: 'SCHEDULED',
    startTime: initial.start,
    endTime: initial.end,
  })
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  const update = (key) => (event) => {
    setForm((f) => ({ ...f, [key]: event.target.value }))
    setFieldErrors((e) => ({ ...e, [key]: undefined }))
    if (error) setError(null)
  }

  function validate() {
    const next = {}
    if (!form.title.trim()) next.title = 'Required.'
    if (!form.description.trim()) next.description = 'Required.'
    if (!form.area) next.area = 'Select the affected area.'
    if (!form.startTime) next.startTime = 'Required.'
    if (!form.endTime) next.endTime = 'Required.'
    else if (new Date(form.endTime) <= new Date(form.startTime)) {
      next.endTime = 'Must be after the start time.'
    } else if (new Date(form.endTime) <= new Date()) {
      // The server enforces @Future on endTime; catching it here saves a
      // round trip and explains the rule at the field that broke it.
      next.endTime = 'Must be in the future.'
    }
    setFieldErrors(next)
    return Object.keys(next).length === 0
  }

  async function onSubmit(event) {
    event.preventDefault()
    if (!validate()) return

    setBusy(true)
    setError(null)
    try {
      const created = await createOutage(form)
      toast.success(`“${created.title}” scheduled for ${areaLabel(created.area)}.`)
      const fresh = defaultWindow()
      setForm({
        title: '',
        description: '',
        area: '',
        type: 'SCHEDULED',
        startTime: fresh.start,
        endTime: fresh.end,
      })
      onCreated()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      size="lg"
      title="Schedule an outage"
      description="Customers in the selected area are notified when this is created."
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={busy}>
            Cancel
          </Button>
          <Button onClick={onSubmit} loading={busy}>
            Schedule
          </Button>
        </>
      }
    >
      <form onSubmit={onSubmit} className="grid gap-4" noValidate>
        <FormError>{error}</FormError>

        <Input
          label="Title"
          value={form.title}
          onChange={update('title')}
          error={fieldErrors.title}
          placeholder="e.g. Feeder maintenance — Sector 7"
          required
        />

        <Textarea
          label="Description"
          value={form.description}
          onChange={update('description')}
          error={fieldErrors.description}
          placeholder="What is happening and what customers should expect."
          rows={3}
          required
        />

        <div className="grid gap-4 sm:grid-cols-2">
          <AreaPicker
            label="Area"
            value={form.area}
            onChange={update('area')}
            error={fieldErrors.area}
            placeholder="Select an area"
            required
          />
          <Select label="Type" value={form.type} onChange={update('type')} required>
            {OUTAGE_TYPES.map((value) => (
              <option key={value} value={value}>
                {label(value)}
              </option>
            ))}
          </Select>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Starts"
            type="datetime-local"
            value={form.startTime}
            onChange={update('startTime')}
            error={fieldErrors.startTime}
            required
          />
          <Input
            label="Ends"
            type="datetime-local"
            value={form.endTime}
            onChange={update('endTime')}
            error={fieldErrors.endTime}
            required
          />
        </div>

        {form.type === 'EMERGENCY' && (
          <p className="rounded-lg border border-danger/25 bg-danger-soft px-3 py-2.5 text-[12.5px] text-danger">
            Emergency outages are shown to customers with the highest urgency in
            the app. Use only for unplanned interruptions.
          </p>
        )}
      </form>
    </Modal>
  )
}
