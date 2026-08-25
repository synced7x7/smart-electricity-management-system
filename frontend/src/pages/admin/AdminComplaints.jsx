import { useEffect, useState } from 'react'
import Badge from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import { Select, Textarea } from '../../components/ui/Field'
import Modal from '../../components/ui/Modal'
import { EmptyState, ErrorState, FormError, SkeletonRows } from '../../components/ui/States'
import { Surface } from '../../components/ui/Surface'
import { Pagination, TableWrap, Td, Th, Tr } from '../../components/ui/Table'
import AreaPicker from '../../components/ui/AreaPicker'
import { useToast } from '../../context/ToastContext'
import { COMPLAINT_STATUSES, areaLabel, label } from '../../lib/enums'
import { formatDate, relativeTime, shortId } from '../../lib/format'
import { useAsync } from '../../lib/useAsync'
import { listComplaintsForAdmin, updateComplaint } from '../../services/complaints.service'

export default function AdminComplaints() {
  const [status, setStatus] = useState('')
  const [area, setArea] = useState('')
  const [page, setPage] = useState(0)
  const [triaging, setTriaging] = useState(null)

  const complaints = useAsync(
    () => listComplaintsForAdmin({ status, area, page, size: 10 }),
    [status, area, page],
  )
  const rows = complaints.data?.content || []

  return (
    <div className="grid gap-5">
      <header className="animate-rise">
        <h1 className="text-[1.55rem] font-semibold tracking-[-0.022em] text-ink">
          Complaint triage
        </h1>
        <p className="mt-1 text-[13.5px] text-muted">
          Set a status and leave a remark — the customer sees your remark on
          their own complaint.
        </p>
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
          {COMPLAINT_STATUSES.map((value) => (
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
        {complaints.loading ? (
          <div className="p-5">
            <SkeletonRows rows={5} />
          </div>
        ) : complaints.error ? (
          <ErrorState error={complaints.error} onRetry={complaints.reload} />
        ) : rows.length === 0 ? (
          <EmptyState title="No complaints match" description="Try a different filter." />
        ) : (
          <>
            <TableWrap>
              <thead>
                <tr>
                  <Th>Complaint</Th>
                  <Th>Area</Th>
                  <Th>Raised</Th>
                  <Th>Status</Th>
                  <Th align="right">Action</Th>
                </tr>
              </thead>
              <tbody>
                {rows.map((complaint) => (
                  <Tr key={complaint.id}>
                    <Td>
                      <span className="font-medium text-ink">{complaint.title}</span>
                      <span className="tnum block text-[11.5px] text-faint">
                        from {shortId(complaint.userId)}
                      </span>
                    </Td>
                    <Td>{areaLabel(complaint.area)}</Td>
                    <Td mono className="whitespace-nowrap">
                      {formatDate(complaint.createdAt)}
                      <span className="block text-[11.5px] text-faint">
                        {relativeTime(complaint.createdAt)}
                      </span>
                    </Td>
                    <Td>
                      <Badge value={complaint.status} />
                    </Td>
                    <Td align="right">
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => setTriaging(complaint)}
                      >
                        {complaint.remark ? 'Update' : 'Respond'}
                      </Button>
                    </Td>
                  </Tr>
                ))}
              </tbody>
            </TableWrap>
            <Pagination {...complaints.data} onPage={setPage} />
          </>
        )}
      </Surface>

      <TriageModal
        complaint={triaging}
        onClose={() => setTriaging(null)}
        onSaved={(updated) => {
          complaints.setData((current) => ({
            ...current,
            content: (current?.content || []).map((c) =>
              c.id === updated.id ? { ...c, ...updated } : c,
            ),
          }))
          setTriaging(null)
        }}
      />
    </div>
  )
}

function TriageModal({ complaint, onClose, onSaved }) {
  const toast = useToast()
  const [status, setStatus] = useState('PENDING')
  const [remark, setRemark] = useState('')
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  // Reset to the complaint being opened, not to whatever was last typed —
  // carrying a previous remark into a different complaint would be a serious
  // mistake to make silently.
  useEffect(() => {
    if (!complaint) return
    setStatus(complaint.status || 'PENDING')
    setRemark(complaint.remark || '')
    setError(null)
  }, [complaint])

  async function onSubmit(event) {
    event.preventDefault()
    setBusy(true)
    setError(null)
    try {
      const updated = await updateComplaint(complaint.id, { status, adminRemark: remark })
      toast.success('Complaint updated. The customer can see your response.')
      onSaved(updated)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <Modal
      open={Boolean(complaint)}
      onClose={onClose}
      size="lg"
      title={complaint?.title}
      description={
        complaint
          ? `${areaLabel(complaint.area)} · raised ${relativeTime(complaint.createdAt)}`
          : undefined
      }
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={busy}>
            Cancel
          </Button>
          <Button onClick={onSubmit} loading={busy}>
            Save response
          </Button>
        </>
      }
    >
      <form onSubmit={onSubmit} className="grid gap-4" noValidate>
        <FormError>{error}</FormError>

        <div className="rounded-lg bg-sunken px-3.5 py-3">
          <p className="text-[11.5px] font-semibold tracking-[0.04em] text-muted uppercase">
            What the customer reported
          </p>
          <p className="mt-1 text-[13px] leading-relaxed text-ink">
            {complaint?.description || '—'}
          </p>
        </div>

        <Select
          label="Status"
          value={status}
          onChange={(event) => setStatus(event.target.value)}
          required
        >
          {COMPLAINT_STATUSES.map((value) => (
            <option key={value} value={value}>
              {label(value)}
            </option>
          ))}
        </Select>

        <Textarea
          label="Remark"
          value={remark}
          onChange={(event) => setRemark(event.target.value)}
          rows={4}
          maxLength={2000}
          placeholder="What was found, what was done, and what happens next."
          hint="Visible to the customer on their complaint. Max 2000 characters."
        />
      </form>
    </Modal>
  )
}
