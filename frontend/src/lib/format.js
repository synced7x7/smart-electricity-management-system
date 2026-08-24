/**
 * The backend serialises `LocalDateTime` — no timezone, no offset. Parsing
 * "2026-08-24T14:00:00" with `new Date()` is treated as *local* time by every
 * modern engine, which is what we want: the server clock and the user are both
 * in Dhaka in this simulation. What we must never do is append a "Z".
 */
export function parseServerDate(value) {
  if (!value) return null
  const d = new Date(value)
  return Number.isNaN(d.getTime()) ? null : d
}

/**
 * Serialise back to the shape Spring's `LocalDateTime` binder accepts.
 * outage/complaint/notification services pin the pattern to
 * `yyyy-MM-dd'T'HH:mm:ss` explicitly, so millis and "Z" are both rejected.
 */
export function toServerDateTime(value) {
  if (!value) return null
  // <input type="datetime-local"> yields "YYYY-MM-DDTHH:mm" (no seconds).
  return value.length === 16 ? `${value}:00` : value
}

/** Server datetime -> the value a <input type="datetime-local"> expects. */
export function toLocalInput(value) {
  const d = parseServerDate(value)
  if (!d) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const currency = new Intl.NumberFormat('en-BD', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

/** Money is always shown to two decimals with the unit — never a bare number. */
export function taka(amount) {
  if (amount == null || amount === '') return '—'
  const n = typeof amount === 'string' ? Number(amount) : amount
  if (Number.isNaN(n)) return '—'
  return `৳${currency.format(n)}`
}

const dateFmt = new Intl.DateTimeFormat('en-GB', {
  day: 'numeric',
  month: 'short',
  year: 'numeric',
})

const dateTimeFmt = new Intl.DateTimeFormat('en-GB', {
  day: 'numeric',
  month: 'short',
  year: 'numeric',
  hour: 'numeric',
  minute: '2-digit',
})

const timeFmt = new Intl.DateTimeFormat('en-GB', {
  hour: 'numeric',
  minute: '2-digit',
})

export function formatDate(value) {
  const d = parseServerDate(value)
  return d ? dateFmt.format(d) : '—'
}

export function formatDateTime(value) {
  const d = parseServerDate(value)
  return d ? dateTimeFmt.format(d) : '—'
}

export function formatTime(value) {
  const d = parseServerDate(value)
  return d ? timeFmt.format(d) : '—'
}

/** "2026-08" -> "August 2026". Bill months are the customer's mental model. */
export function formatBillMonth(billMonth) {
  if (!billMonth || !/^\d{4}-\d{2}$/.test(billMonth)) return billMonth || '—'
  const [year, month] = billMonth.split('-')
  const d = new Date(Number(year), Number(month) - 1, 1)
  return new Intl.DateTimeFormat('en-GB', { month: 'long', year: 'numeric' }).format(d)
}

/** Current month as YYYY-MM — the default the bill-pay form opens on. */
export function currentBillMonth() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

/** The N most recent bill months, newest first, for the month picker. */
export function recentBillMonths(count = 18) {
  const out = []
  const now = new Date()
  for (let i = 0; i < count; i += 1) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    out.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`)
  }
  return out
}

/** "3 hours ago" / "in 2 days" — used sparingly, never for money. */
const rel = new Intl.RelativeTimeFormat('en', { numeric: 'auto' })

export function relativeTime(value) {
  const d = parseServerDate(value)
  if (!d) return '—'
  const diffMs = d.getTime() - Date.now()
  const abs = Math.abs(diffMs)
  const units = [
    ['minute', 60_000],
    ['hour', 3_600_000],
    ['day', 86_400_000],
    ['week', 604_800_000],
    ['month', 2_592_000_000],
    ['year', 31_536_000_000],
  ]
  if (abs < 60_000) return 'just now'
  let chosen = units[0]
  for (const unit of units) {
    if (abs >= unit[1]) chosen = unit
  }
  return rel.format(Math.round(diffMs / chosen[1]), chosen[0])
}

/** Short, stable identity for a UUID in dense tables. */
export function shortId(id) {
  return id ? String(id).slice(0, 8) : '—'
}
