import { Link } from 'react-router-dom'
import OutageCard from '../../components/OutageCard'
import Button from '../../components/ui/Button'
import Icon from '../../components/ui/Icon'
import { ErrorState, Skeleton } from '../../components/ui/States'
import { Surface, SurfaceHeader } from '../../components/ui/Surface'
import { areaLabel } from '../../lib/enums'
import { formatBillMonth, formatTime, taka } from '../../lib/format'
import { useAsync } from '../../lib/useAsync'
import { getDashboard, healthSummary } from '../../services/admin.service'
import { normalizeOutage } from '../../services/outages.service'

export default function AdminDashboard() {
  const dashboard = useAsync(getDashboard, [])
  const data = dashboard.data

  if (dashboard.error) {
    return (
      <Surface>
        <ErrorState error={dashboard.error} onRetry={dashboard.reload} />
      </Surface>
    )
  }

  const health = healthSummary(data?.services)

  return (
    <div className="grid gap-6">
      <header className="animate-rise flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-[1.7rem] font-semibold tracking-[-0.024em] text-ink">
            System overview
          </h1>
          <p className="mt-1 text-[14px] text-muted">
            {data?.generatedAt
              ? `Snapshot taken at ${formatTime(data.generatedAt)}`
              : 'Live figures across every service.'}
          </p>
        </div>
        <Button
          variant="secondary"
          size="sm"
          loading={dashboard.loading}
          onClick={dashboard.reload}
        >
          <Icon name="refresh" size={16} />
          Refresh
        </Button>
      </header>

      {/* Health first. On this page it is the only panel that can tell an
          operator something is wrong right now, so it outranks the counts. */}
      <ServiceHealth services={data?.services} summary={health} loading={dashboard.loading} />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatBlock
          loading={dashboard.loading}
          label="Accounts"
          value={data?.users?.total}
          to="/admin/users"
          lines={[
            [`${data?.users?.active ?? 0} active`, 'positive'],
            [`${data?.users?.inactive ?? 0} deactivated`, 'neutral'],
            [`${data?.users?.admins ?? 0} admin`, 'neutral'],
          ]}
        />
        <StatBlock
          loading={dashboard.loading}
          label="Revenue collected"
          value={taka(data?.payments?.totalRevenue)}
          raw
          to="/admin/payments"
          lines={[
            [
              `${taka(data?.payments?.currentMonthRevenue)} in ${formatBillMonth(data?.payments?.currentBillMonth)}`,
              'neutral',
            ],
            [`${data?.payments?.successful ?? 0} settled`, 'positive'],
            [`${data?.payments?.failed ?? 0} failed`, (data?.payments?.failed ?? 0) > 0 ? 'critical' : 'neutral'],
          ]}
        />
        <StatBlock
          loading={dashboard.loading}
          label="Complaints"
          value={data?.complaints?.total}
          to="/admin/complaints"
          lines={[
            [
              `${data?.complaints?.pending ?? 0} awaiting triage`,
              (data?.complaints?.pending ?? 0) > 0 ? 'warning' : 'neutral',
            ],
            [`${data?.complaints?.inProgress ?? 0} in progress`, 'info'],
            [`${data?.complaints?.resolved ?? 0} resolved`, 'positive'],
          ]}
        />
        <StatBlock
          loading={dashboard.loading}
          label="Outages"
          value={data?.outages?.total}
          to="/admin/outages"
          lines={[
            [
              `${data?.outages?.ongoing ?? 0} ongoing`,
              (data?.outages?.ongoing ?? 0) > 0 ? 'critical' : 'neutral',
            ],
            [`${data?.outages?.scheduled ?? 0} scheduled`, 'info'],
            [`${data?.outages?.resolved ?? 0} resolved`, 'positive'],
          ]}
        />
      </div>

      <div className="grid gap-4 lg:grid-cols-[1.15fr_1fr]">
        <Surface>
          <SurfaceHeader
            title="Upcoming outages"
            action={
              <Button as={Link} to="/admin/outages" variant="ghost" size="sm">
                Manage
              </Button>
            }
          />
          <div className="p-4">
            {dashboard.loading ? (
              <div className="grid gap-3">
                <Skeleton className="h-24" />
                <Skeleton className="h-24" />
              </div>
            ) : data?.outages?.upcoming?.length ? (
              <div className="stagger grid gap-3">
                {data.outages.upcoming.slice(0, 3).map((raw) => (
                  <OutageCard key={raw.id} outage={normalizeOutage(raw)} />
                ))}
              </div>
            ) : (
              <p className="py-6 text-center text-[13px] text-muted">
                Nothing scheduled.
              </p>
            )}
          </div>
        </Surface>

        <div className="grid gap-4">
          <Distribution
            title="Accounts by area"
            data={data?.users?.byArea}
            loading={dashboard.loading}
            labelFn={areaLabel}
          />
          <Surface>
            <SurfaceHeader title="Revenue by month" />
            <div className="p-4">
              {dashboard.loading ? (
                <Skeleton className="h-24" />
              ) : data?.payments?.recentMonths?.length ? (
                <RevenueBars months={data.payments.recentMonths} />
              ) : (
                <p className="py-4 text-center text-[13px] text-muted">No payments yet.</p>
              )}
            </div>
          </Surface>
        </div>
      </div>
    </div>
  )
}

/* -------------------------------------------------------------------------- */

const lineTones = {
  positive: 'text-brand',
  warning: 'text-amber',
  critical: 'text-danger',
  info: 'text-info',
  neutral: 'text-muted',
}

function StatBlock({ label, value, lines, loading, to, raw = false }) {
  return (
    <Surface
      as={Link}
      to={to}
      className="animate-rise block p-4 transition-[border-color,transform] duration-150 [transition-timing-function:var(--ease-out-strong)] hover:border-line-strong active:scale-[0.99]"
    >
      <p className="text-[11.5px] font-medium tracking-[0.05em] text-muted uppercase">
        {label}
      </p>
      {loading ? (
        <Skeleton className="mt-2 h-8 w-24" />
      ) : (
        <p className="tnum mt-1.5 text-[1.75rem] leading-none font-semibold tracking-[-0.022em] text-ink">
          {raw ? value : (value ?? 0)}
        </p>
      )}
      <ul className="mt-3 grid gap-0.5">
        {lines.map(([text, tone]) => (
          <li key={text} className={`text-[12.5px] ${lineTones[tone]}`}>
            {loading ? <Skeleton className="h-3 w-20" /> : text}
          </li>
        ))}
      </ul>
    </Surface>
  )
}

/**
 * Six services, genuinely UP or DOWN. This is a real signal, not decoration:
 * the summary line states the count outright so an operator does not have to
 * count green dots, and a DOWN service carries its reason.
 */
function ServiceHealth({ services, summary, loading }) {
  const allUp = summary.total > 0 && summary.up === summary.total

  return (
    <Surface className="animate-rise overflow-hidden">
      <div
        className={`flex flex-wrap items-center justify-between gap-3 border-b px-5 py-3.5 ${
          loading
            ? 'border-line'
            : allUp
              ? 'border-brand/20 bg-brand-soft'
              : 'border-danger/20 bg-danger-soft'
        }`}
      >
        <div className="flex items-center gap-2.5">
          <span className={loading ? 'text-muted' : allUp ? 'text-brand' : 'text-danger'}>
            <Icon name={allUp && !loading ? 'check' : 'alert'} size={18} strokeWidth={2} />
          </span>
          <p
            className={`text-[14px] font-semibold ${
              loading ? 'text-ink' : allUp ? 'text-brand-ink dark:text-brand' : 'text-danger'
            }`}
          >
            {loading
              ? 'Probing services…'
              : allUp
                ? `All ${summary.total} services responding`
                : `${summary.down + summary.unknown} of ${summary.total} services not responding`}
          </p>
        </div>
        <p className="text-[12px] text-muted">Probed in parallel, 4s timeout</p>
      </div>

      <div className="grid gap-px bg-line sm:grid-cols-2 lg:grid-cols-3">
        {loading
          ? Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="bg-surface p-4">
                <Skeleton className="h-4 w-32" />
                <Skeleton className="mt-2 h-3 w-20" />
              </div>
            ))
          : (services || []).map((service) => {
              const up = service.status === 'UP'
              const down = service.status === 'DOWN'
              return (
                <div key={service.name} className="bg-surface p-4">
                  <div className="flex items-center gap-2">
                    <span
                      className={`size-2 shrink-0 rounded-full ${
                        up ? 'bg-brand' : down ? 'bg-danger' : 'bg-faint'
                      }`}
                    />
                    <p className="truncate text-[13.5px] font-medium text-ink">
                      {service.name}
                    </p>
                    <span
                      className={`tnum ml-auto shrink-0 text-[11.5px] ${
                        up ? 'text-muted' : 'text-danger'
                      }`}
                    >
                      {up && service.responseTimeMs != null
                        ? `${service.responseTimeMs} ms`
                        : service.status}
                    </span>
                  </div>
                  <p className="mt-1 truncate text-[11.5px] text-faint" title={service.detail || service.url}>
                    {down && service.detail ? service.detail : service.url}
                  </p>
                </div>
              )
            })}
      </div>
    </Surface>
  )
}

function Distribution({ title, data, loading, labelFn = (v) => v }) {
  const entries = Object.entries(data || {}).sort((a, b) => b[1] - a[1])
  const max = Math.max(1, ...entries.map(([, count]) => count))

  return (
    <Surface>
      <SurfaceHeader title={title} />
      <div className="grid gap-2 p-4">
        {loading ? (
          Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-5" />)
        ) : entries.length === 0 ? (
          <p className="py-4 text-center text-[13px] text-muted">No data yet.</p>
        ) : (
          entries.map(([key, count]) => (
            <div key={key} className="flex items-center gap-3">
              <span className="w-24 shrink-0 truncate text-[12.5px] text-muted">
                {labelFn(key)}
              </span>
              <span className="h-1.5 flex-1 overflow-hidden rounded-full bg-sunken">
                <span
                  className="block h-full rounded-full bg-brand transition-[width] duration-500 [transition-timing-function:var(--ease-out-strong)]"
                  style={{ width: `${(count / max) * 100}%` }}
                />
              </span>
              <span className="tnum w-8 shrink-0 text-right text-[12.5px] font-medium text-ink">
                {count}
              </span>
            </div>
          ))
        )}
      </div>
    </Surface>
  )
}

function RevenueBars({ months }) {
  const ordered = [...months].reverse()
  const max = Math.max(1, ...ordered.map((m) => Number(m.revenue) || 0))

  return (
    <div>
      <div className="flex h-24 items-end gap-1.5">
        {ordered.map((month) => {
          const value = Number(month.revenue) || 0
          return (
            <div
              key={month.billMonth}
              className="group flex flex-1 flex-col items-center justify-end gap-1"
              title={`${formatBillMonth(month.billMonth)} — ${taka(value)} across ${month.payments} payment${month.payments === 1 ? '' : 's'}`}
            >
              <span
                className="w-full rounded-t-sm bg-brand/75 transition-[height,background-color] duration-300 [transition-timing-function:var(--ease-out-strong)] group-hover:bg-brand"
                style={{ height: `${Math.max(3, (value / max) * 100)}%` }}
              />
            </div>
          )
        })}
      </div>
      <div className="mt-2 flex justify-between text-[11px] text-faint">
        <span>{formatBillMonth(ordered[0]?.billMonth)}</span>
        <span>{formatBillMonth(ordered[ordered.length - 1]?.billMonth)}</span>
      </div>
    </div>
  )
}
