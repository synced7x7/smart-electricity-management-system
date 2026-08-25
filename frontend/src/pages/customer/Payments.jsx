import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import Badge from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import { Input, Select } from '../../components/ui/Field'
import Icon from '../../components/ui/Icon'
import Modal from '../../components/ui/Modal'
import { EmptyState, ErrorState, FormError, SkeletonRows } from '../../components/ui/States'
import { DataRow, Surface, SurfaceHeader } from '../../components/ui/Surface'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import {
  currentBillMonth,
  formatBillMonth,
  formatDateTime,
  recentBillMonths,
  taka,
} from '../../lib/format'
import { useAsync } from '../../lib/useAsync'
import {
  PAYMENT_METHODS,
  getHistory,
  getReceipt,
  payBill,
  settledMonths,
} from '../../services/payments.service'

export default function Payments() {
  const { userId } = useAuth()
  const toast = useToast()
  const [params, setParams] = useSearchParams()

  const history = useAsync(() => getHistory(userId), [userId], { enabled: Boolean(userId) })
  const rows = history.data || []
  // Keyed on the fetched array, not the `|| []` fallback — that literal is
  // a new reference every render and would defeat the memo entirely.
  const settled = useMemo(() => settledMonths(history.data), [history.data])

  const [form, setForm] = useState({
    billMonth: currentBillMonth(),
    amount: '',
    paymentMethod: 'BKASH',
  })
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState(null)
  /** Distinct from `error`: "already paid" is information, not a failure. */
  const [conflict, setConflict] = useState(null)
  const [busy, setBusy] = useState(false)
  const [receiptId, setReceiptId] = useState(params.get('receipt'))

  const months = useMemo(() => recentBillMonths(18), [])
  const alreadySettled = settled.has(form.billMonth)

  // Selecting a month that is already paid is a normal thing to do by
  // accident, so it is answered before the request rather than by a 409.
  useEffect(() => {
    setConflict(alreadySettled ? form.billMonth : null)
  }, [alreadySettled, form.billMonth])

  const update = (key) => (event) => {
    setForm((f) => ({ ...f, [key]: event.target.value }))
    setFieldErrors((e) => ({ ...e, [key]: undefined }))
    if (error) setError(null)
  }

  function validate() {
    const next = {}
    if (!/^\d{4}-\d{2}$/.test(form.billMonth)) next.billMonth = 'Choose a bill month.'
    const amount = Number(form.amount)
    if (!form.amount || Number.isNaN(amount)) next.amount = 'Enter the amount on your bill.'
    else if (amount < 1) next.amount = 'Must be at least ৳1.00.'
    setFieldErrors(next)
    return Object.keys(next).length === 0
  }

  async function onSubmit(event) {
    event.preventDefault()
    if (!validate()) return

    setBusy(true)
    setError(null)
    try {
      const payment = await payBill({ userId, ...form })
      toast.success(`${formatBillMonth(payment.billMonth)} paid — ${taka(payment.amount)}.`)
      setForm((f) => ({ ...f, amount: '' }))
      await history.reload()
      setReceiptId(payment.id)
    } catch (err) {
      if (err.code === 'ALREADY_PAID') {
        // The server saw a duplicate our local history did not know about
        // (paid in another tab, say). Reload so the list agrees with reality.
        setConflict(form.billMonth)
        history.reload()
      } else {
        setError(err.message)
      }
    } finally {
      setBusy(false)
    }
  }

  function closeReceipt() {
    setReceiptId(null)
    if (params.get('receipt')) {
      const next = new URLSearchParams(params)
      next.delete('receipt')
      setParams(next, { replace: true })
    }
  }

  return (
    <div className="grid gap-6">
      <header className="animate-rise">
        <h1 className="text-[1.7rem] font-semibold tracking-[-0.024em] text-ink">Pay a bill</h1>
        <p className="mt-1 text-[14px] text-muted">
          Settle a month, then keep the receipt. One payment per bill month.
        </p>
      </header>

      <Surface className="animate-rise">
        <SurfaceHeader title="New payment" />
        <form onSubmit={onSubmit} className="grid gap-4 p-5" noValidate>
          <FormError>{error}</FormError>

          <div className="grid gap-4 sm:grid-cols-2">
            <Select
              label="Bill month"
              value={form.billMonth}
              onChange={update('billMonth')}
              error={fieldErrors.billMonth}
            >
              {months.map((month) => (
                <option key={month} value={month}>
                  {formatBillMonth(month)}
                  {settled.has(month) ? ' — paid' : ''}
                </option>
              ))}
            </Select>

            <Input
              label="Amount"
              value={form.amount}
              onChange={update('amount')}
              error={fieldErrors.amount}
              placeholder="0.00"
              inputMode="decimal"
              type="number"
              step="0.01"
              min="1"
              className="tnum"
            />
          </div>

          <Select
            label="Pay with"
            value={form.paymentMethod}
            onChange={update('paymentMethod')}
          >
            {PAYMENT_METHODS.map((method) => (
              <option key={method.value} value={method.value}>
                {method.label}
              </option>
            ))}
          </Select>

          {/* Not an error. The bill is settled — that is good news, and it
              should not look like something went wrong. */}
          {conflict && (
            <div className="animate-rise flex items-start gap-3 rounded-lg border border-brand/25 bg-brand-soft px-3.5 py-3">
              <span className="mt-0.5 shrink-0 text-brand">
                <Icon name="check" size={17} strokeWidth={2} />
              </span>
              <div className="min-w-0">
                <p className="text-[13.5px] font-semibold text-brand-ink dark:text-brand">
                  {formatBillMonth(conflict)} is already paid
                </p>
                <p className="mt-0.5 text-[12.5px] text-brand-ink/80 dark:text-brand/80">
                  Nothing further is owed for this month. Pick a different month to
                  make another payment.
                </p>
              </div>
            </div>
          )}

          <Button
            type="submit"
            size="lg"
            loading={busy}
            disabled={Boolean(conflict)}
            className="mt-1 justify-self-start"
          >
            {form.amount ? `Pay ${taka(form.amount)}` : 'Pay bill'}
          </Button>

          <p className="text-[12px] text-faint">
            Simulated gateway — no real funds move. A transaction reference is issued
            on success.
          </p>
        </form>
      </Surface>

      <Surface>
        <SurfaceHeader title="Payment history" description="Newest first" />
        {history.loading ? (
          <div className="p-5">
            <SkeletonRows rows={4} />
          </div>
        ) : history.error ? (
          <ErrorState error={history.error} onRetry={history.reload} />
        ) : rows.length === 0 ? (
          <EmptyState
            title="No payments yet"
            description="Your first payment will appear here, with a receipt you can open at any time."
          />
        ) : (
          <ul className="divide-y divide-line">
            {rows.map((payment) => (
              <li
                key={payment.id}
                className="flex flex-wrap items-center gap-x-4 gap-y-2 px-5 py-3.5"
              >
                <div className="min-w-0 flex-1">
                  <p className="text-[14px] font-medium text-ink">
                    {formatBillMonth(payment.billMonth)}
                  </p>
                  <p className="tnum truncate text-[12px] text-faint">
                    {payment.transactionId} · {payment.paymentMethod}
                  </p>
                </div>

                <Badge value={payment.status} />

                <p className="tnum w-28 text-right text-[14px] font-semibold text-ink">
                  {taka(payment.amount)}
                </p>

                {/* A receipt only exists for a settled payment — offering the
                    button on a FAILED row would promise a 400. */}
                {payment.status === 'SUCCESS' && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setReceiptId(payment.id)}
                    aria-label={`Receipt for ${formatBillMonth(payment.billMonth)}`}
                  >
                    <Icon name="receipt" size={16} />
                    Receipt
                  </Button>
                )}
              </li>
            ))}
          </ul>
        )}
      </Surface>

      <ReceiptModal id={receiptId} onClose={closeReceipt} />
    </div>
  )
}

function ReceiptModal({ id, onClose }) {
  const receipt = useAsync(() => getReceipt(id), [id], { enabled: Boolean(id) })

  return (
    <Modal
      open={Boolean(id)}
      onClose={onClose}
      title="Payment receipt"
      description={receipt.data ? formatBillMonth(receipt.data.billMonth) : undefined}
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Close
          </Button>
          <Button variant="secondary" onClick={() => window.print()} disabled={!receipt.data}>
            Print
          </Button>
        </>
      }
    >
      {receipt.loading ? (
        <SkeletonRows rows={4} />
      ) : receipt.error ? (
        <ErrorState error={receipt.error} onRetry={receipt.reload} />
      ) : receipt.data ? (
        <div>
          <div className="mb-4 flex items-baseline justify-between gap-4 border-b border-line pb-4">
            <div>
              <p className="text-[12px] tracking-[0.06em] text-faint uppercase">Amount paid</p>
              <p className="tnum mt-1 text-[1.75rem] leading-none font-semibold tracking-[-0.02em] text-ink">
                {taka(receipt.data.amount)}
              </p>
            </div>
            <Badge value={receipt.data.status} />
          </div>

          <dl className="divide-y divide-line">
            <DataRow label="Transaction" value={receipt.data.transactionId} mono />
            <DataRow label="Bill month" value={formatBillMonth(receipt.data.billMonth)} />
            <DataRow label="Paid at" value={formatDateTime(receipt.data.paidAt)} mono />
            <DataRow label="Method" value={receipt.data.paymentMethod} />
            <DataRow label="Currency" value={receipt.data.currency} />
            <DataRow label="Issued by" value={receipt.data.issuedBy} />
          </dl>

          {receipt.data.note && (
            <p className="mt-4 rounded-lg bg-sunken px-3 py-2.5 text-[12.5px] text-muted">
              {receipt.data.note}
            </p>
          )}
        </div>
      ) : null}
    </Modal>
  )
}
