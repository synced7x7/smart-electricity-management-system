import Badge, { LiveBadge } from './ui/Badge'
import { areaLabel } from '../lib/enums'
import { formatDateTime, formatTime, parseServerDate, relativeTime } from '../lib/format'

/**
 * One outage, at the urgency it actually warrants. An ongoing emergency and a
 * maintenance window three weeks out are not the same message and must not
 * share a visual treatment — that is the whole point of the alert.
 */
export default function OutageCard({ outage, className = '' }) {
  const live = outage.status === 'ONGOING'
  const emergency = outage.type === 'EMERGENCY'
  const accent = live || emergency ? 'border-l-danger' : 'border-l-info'

  const start = parseServerDate(outage.startTime)
  const end = parseServerDate(outage.endTime)
  // Same-day windows read as "24 Aug, 14:00 – 18:00" rather than repeating
  // the date on both ends.
  const sameDay = start && end && start.toDateString() === end.toDateString()

  return (
    <article
      className={`rounded-xl border border-line border-l-[3px] bg-surface p-4 transition-colors duration-150 ${accent} ${className}`}
    >
      <div className="flex flex-wrap items-start justify-between gap-2">
        <h3 className="text-[14.5px] leading-snug font-semibold text-ink">{outage.title}</h3>
        <div className="flex shrink-0 flex-wrap items-center gap-1.5">
          {live ? <LiveBadge /> : <Badge value={outage.status} />}
          {emergency && !live && <Badge tone="critical">Emergency</Badge>}
        </div>
      </div>

      {outage.detail && (
        <p className="mt-1.5 text-[13px] leading-relaxed text-muted">{outage.detail}</p>
      )}

      <dl className="mt-3 flex flex-wrap items-center gap-x-5 gap-y-1 text-[12.5px] text-muted">
        <div className="flex items-center gap-1.5">
          <dt className="sr-only">Area</dt>
          <dd className="font-medium text-ink">{areaLabel(outage.area)}</dd>
        </div>
        <div className="flex items-center gap-1.5">
          <dt className="sr-only">Window</dt>
          <dd className="tnum">
            {sameDay
              ? `${formatDateTime(outage.startTime)} – ${formatTime(outage.endTime)}`
              : `${formatDateTime(outage.startTime)} → ${formatDateTime(outage.endTime)}`}
          </dd>
        </div>
        {outage.status === 'SCHEDULED' && start && start > new Date() && (
          <div>
            <dt className="sr-only">Starts</dt>
            <dd className="text-faint">starts {relativeTime(outage.startTime)}</dd>
          </div>
        )}
        {live && end && (
          <div>
            <dt className="sr-only">Expected back</dt>
            <dd className="font-medium text-danger">
              supply expected back {relativeTime(outage.endTime)}
            </dd>
          </div>
        )}
      </dl>
    </article>
  )
}
