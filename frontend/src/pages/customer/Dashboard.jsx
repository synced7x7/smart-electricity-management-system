import { Link } from 'react-router-dom'
import OutageCard from '../../components/OutageCard'
import Badge from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import Icon from '../../components/ui/Icon'
import { EmptyState, ErrorState, SkeletonRows } from '../../components/ui/States'
import { Surface, SurfaceHeader } from '../../components/ui/Surface'
import { useAuth } from '../../context/AuthContext'
import { areaLabel } from '../../lib/enums'
import {
  currentBillMonth,
  formatBillMonth,
  formatDate,
  relativeTime,
  taka,
} from '../../lib/format'
import { useAsync } from '../../lib/useAsync'
import { getUnreadCount } from '../../services/notifications.service'
import { getHistory } from '../../services/payments.service'
import { getActive, getByArea } from '../../services/outages.service'
import { displayName } from '../../services/users.service'

export default function Dashboard() {
  const { userId, profile, area, profileComplete } = useAuth()

  // With an area set, show that area's outages; without one, fall back to
  // everything currently active rather than an empty panel that looks broken.
  const outages = useAsync(
    () => (area ? getByArea(area) : getActive()),
    [area],
    { enabled: Boolean(userId) },
  )
  const payments = useAsync(() => getHistory(userId), [userId], { enabled: Boolean(userId) })
  const unread = useAsync(() => getUnreadCount(userId), [userId], { enabled: Boolean(userId) })

  const thisMonth = currentBillMonth()
  const history = payments.data || []
  const paidThisMonth = history.find(
    (p) => p.billMonth === thisMonth && p.status === 'SUCCESS',
  )
  const lastPayment = history.find((p) => p.status === 'SUCCESS')
  const now = new Date()
  const ranked = (outages.data || []).filter((o) => !o.endTime || new Date(o.endTime) > now)
  
  const liveNow = ranked.filter((o) => o.status === 'ONGOING')

  return (
    <div className="grid gap-6">
      <header className="animate-rise">
        <h1 className="text-[1.7rem] font-semibold tracking-[-0.024em] text-ink">
          Good day, {displayName(profile)}
        </h1>
        <p className="mt-1 text-[14px] text-muted">
          {area ? `Supply and billing for ${areaLabel(area)}.` : 'Your account at a glance.'}
        </p>
      </header>

      {/* The one thing a first-time user must do before anything else works
          properly. Placed above the fold, and it disappears the moment it is
          satisfied rather than nagging forever. */}
      {!profileComplete && (
        <Surface className="animate-rise border-amber/30 bg-amber-soft p-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-start gap-3">
              <span className="mt-0.5 text-amber">
                <Icon name="user" size={18} />
              </span>
              <div>
                <p className="text-[14px] font-semibold text-amber">
                  Finish setting up your profile
                </p>
                <p className="mt-0.5 max-w-lg text-[13px] text-amber/85">
                  We need your full name and meter number before we can match your
                  account to a connection.
                </p>
              </div>
            </div>
            <Button as={Link} to="/profile" size="sm" className="shrink-0">
              Complete profile
            </Button>
          </div>
        </Surface>
      )}

      {/* Supply status first: whether the lights are on outranks everything
          else on this page. */}
      <section className="grid gap-3">
        <div className="flex items-baseline justify-between gap-3">
          <h2 className="text-[15px] font-semibold text-ink">Supply status</h2>
          {area && <span className="text-[12.5px] text-faint">{areaLabel(area)}</span>}
        </div>

        {outages.loading ? (
          <SkeletonRows rows={2} />
        ) : outages.error ? (
          <Surface>
            <ErrorState error={outages.error} onRetry={outages.reload} />
          </Surface>
        ) : ranked.length === 0 ? (
          <Surface className="flex items-center gap-3 p-4">
            <span className="grid size-9 shrink-0 place-items-center rounded-full bg-brand-soft text-brand">
              <Icon name="check" size={18} />
            </span>
            <div>
              <p className="text-[14px] font-medium text-ink">Supply is normal</p>
              <p className="text-[13px] text-muted">
                No outages are scheduled or ongoing
                {area ? ` in ${areaLabel(area)}` : ''}.
              </p>
            </div>
          </Surface>
        ) : (
          <div className="stagger grid gap-3">
            {ranked.slice(0, 4).map((outage) => (
              <OutageCard key={outage.id} outage={outage} />
            ))}
          </div>
        )}

        {ranked.length > 4 && (
          <p className="text-[12.5px] text-muted">
            +{ranked.length - 4} more scheduled in your area.
          </p>
        )}
      </section>

      {/* Money and messages, side by side below the supply state. */}
      <div className="grid gap-4 md:grid-cols-2">
        <Surface className="flex flex-col">
          <SurfaceHeader title="This month’s bill" description={formatBillMonth(thisMonth)} />
          <div className="flex flex-1 flex-col justify-between gap-4 p-5">
            {payments.loading ? (
              <SkeletonRows rows={1} />
            ) : paidThisMonth ? (
              <>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="grid size-6 place-items-center rounded-full bg-brand-soft text-brand">
                      <Icon name="check" size={14} strokeWidth={2} />
                    </span>
                    <p className="text-[14px] font-medium text-ink">Paid</p>
                  </div>
                  <p className="tnum mt-3 text-[1.75rem] leading-none font-semibold tracking-[-0.02em] text-ink">
                    {taka(paidThisMonth.amount)}
                  </p>
                  <p className="mt-2 text-[12.5px] text-muted">
                    Settled {relativeTime(paidThisMonth.paidAt || paidThisMonth.createdAt)} ·{' '}
                    {paidThisMonth.transactionId}
                  </p>
                </div>
                <Button
                  as={Link}
                  to={`/pay?receipt=${paidThisMonth.id}`}
                  variant="secondary"
                  size="sm"
                  className="self-start"
                >
                  View receipt
                </Button>
              </>
            ) : (
              <>
                <div>
                  <Badge tone="warning" dot>
                    Not yet paid
                  </Badge>
                  <p className="mt-3 text-[13.5px] leading-relaxed text-muted">
                    {lastPayment
                      ? `Your last payment of ${taka(lastPayment.amount)} covered ${formatBillMonth(lastPayment.billMonth)}.`
                      : 'You have not recorded a payment yet.'}
                  </p>
                </div>
                <Button as={Link} to="/pay" size="md" className="self-start">
                  <Icon name="wallet" size={17} />
                  Pay this bill
                </Button>
              </>
            )}
          </div>
        </Surface>

        <Surface className="flex flex-col">
          <SurfaceHeader title="Alerts" description="Outage notices and announcements" />
          <div className="flex flex-1 flex-col justify-between gap-4 p-5">
            <div>
              <p className="tnum text-[1.75rem] leading-none font-semibold tracking-[-0.02em] text-ink">
                {unread.loading ? '—' : (unread.data ?? 0)}
              </p>
              <p className="mt-2 text-[13.5px] text-muted">
                {unread.data === 1 ? 'unread message' : 'unread messages'}
                {liveNow.length > 0 && (
                  <>
                    {' · '}
                    <span className="font-medium text-danger">
                      {liveNow.length} outage{liveNow.length === 1 ? '' : 's'} live now
                    </span>
                  </>
                )}
              </p>
            </div>
            <Button as={Link} to="/alerts" variant="secondary" size="sm" className="self-start">
              Open alerts
            </Button>
          </div>
        </Surface>
      </div>

      <Surface>
        <SurfaceHeader
          title="Recent payments"
          action={
            <Button as={Link} to="/pay" variant="ghost" size="sm">
              See all
            </Button>
          }
        />
        {payments.loading ? (
          <div className="p-5">
            <SkeletonRows rows={3} />
          </div>
        ) : payments.error ? (
          <ErrorState error={payments.error} onRetry={payments.reload} />
        ) : history.length === 0 ? (
          <EmptyState
            title="No payments yet"
            description="Once you pay a bill it will appear here with a downloadable receipt."
            action={
              <Button as={Link} to="/pay" size="sm">
                Pay a bill
              </Button>
            }
          />
        ) : (
          <ul className="divide-y divide-line">
            {history.slice(0, 4).map((payment) => (
              <li key={payment.id} className="flex items-center gap-4 px-5 py-3">
                <div className="min-w-0 flex-1">
                  <p className="text-[13.5px] font-medium text-ink">
                    {formatBillMonth(payment.billMonth)}
                  </p>
                  <p className="text-[12px] text-faint">
                    {formatDate(payment.paidAt || payment.createdAt)} ·{' '}
                    {payment.paymentMethod}
                  </p>
                </div>
                <Badge value={payment.status} />
                <p className="tnum w-24 text-right text-[13.5px] font-semibold text-ink">
                  {taka(payment.amount)}
                </p>
              </li>
            ))}
          </ul>
        )}
      </Surface>
    </div>
  )
}
