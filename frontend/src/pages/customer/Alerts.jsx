import { useMemo, useState } from 'react'
import Button from '../../components/ui/Button'
import Icon from '../../components/ui/Icon'
import { EmptyState, ErrorState, SkeletonRows } from '../../components/ui/States'
import { Surface, SurfaceHeader } from '../../components/ui/Surface'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { areaLabel } from '../../lib/enums'
import { formatDateTime, relativeTime } from '../../lib/format'
import { useAsync } from '../../lib/useAsync'
import { getFeed, markAllRead, markRead } from '../../services/notifications.service'

/**
 * An emergency outage alert and a monthly announcement are not the same
 * message. The feed encodes that: only OUTAGE_ALERT gets the danger accent
 * and the alert glyph, so urgency stays meaningful.
 */
const KIND = {
  OUTAGE_ALERT: {
    icon: 'alert',
    accent: 'border-l-danger',
    chip: 'bg-danger-soft text-danger',
    caption: 'Outage',
  },
  BILLING_ALERT: {
    icon: 'wallet',
    accent: 'border-l-amber',
    chip: 'bg-amber-soft text-amber',
    caption: 'Billing',
  },
  COMPLAINT_UPDATE: {
    icon: 'message',
    accent: 'border-l-info',
    chip: 'bg-info-soft text-info',
    caption: 'Complaint',
  },
  GENERAL_ANNOUNCEMENT: {
    icon: 'bell',
    accent: 'border-l-line-strong',
    chip: 'bg-sunken text-muted',
    caption: 'Announcement',
  },
}

export default function Alerts() {
  const { userId, area } = useAuth()
  const toast = useToast()
  const [busy, setBusy] = useState(false)

  const feed = useAsync(() => getFeed(userId), [userId], { enabled: Boolean(userId) })
  const items = feed.data || []

  const unreadCount = useMemo(
    () => (feed.data || []).filter((n) => !n.isRead).length,
    [feed.data],
  )

  async function onMarkRead(id) {
    // Optimistic: the row greys out on the click, not on the round trip. If
    // the call fails the state is put back and the user is told.
    feed.setData((current) =>
      (current || []).map((n) => (n.id === id ? { ...n, isRead: true } : n)),
    )
    try {
      await markRead(id)
    } catch (error) {
      feed.setData((current) =>
        (current || []).map((n) => (n.id === id ? { ...n, isRead: false } : n)),
      )
      toast.error(error.message)
    }
  }

  async function onMarkAll() {
    setBusy(true)
    try {
      await markAllRead(userId)
      feed.setData((current) => (current || []).map((n) => ({ ...n, isRead: true })))
      toast.success('All alerts marked as read.')
    } catch (error) {
      toast.error(error.message)
      feed.reload()
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="grid gap-6">
      <header className="animate-rise">
        <h1 className="text-[1.7rem] font-semibold tracking-[-0.024em] text-ink">Alerts</h1>
        <p className="mt-1 text-[14px] text-muted">
          Outage notices for {areaLabel(area)} and messages sent directly to you.
        </p>
      </header>

      <Surface>
        <SurfaceHeader
          title={unreadCount > 0 ? `${unreadCount} unread` : 'All caught up'}
          description={items.length ? `${items.length} in total` : undefined}
          action={
            unreadCount > 0 && (
              <Button variant="secondary" size="sm" loading={busy} onClick={onMarkAll}>
                Mark all read
              </Button>
            )
          }
        />

        {feed.loading ? (
          <div className="p-5">
            <SkeletonRows rows={4} />
          </div>
        ) : feed.error ? (
          <ErrorState error={feed.error} onRetry={feed.reload} />
        ) : items.length === 0 ? (
          <EmptyState
            title="No alerts yet"
            description="Outage notices for your area and updates on your complaints will arrive here."
          />
        ) : (
          <ul className="divide-y divide-line">
            {items.map((item) => {
              const kind = KIND[item.type] || KIND.GENERAL_ANNOUNCEMENT
              return (
                <li
                  key={item.id}
                  className={`flex gap-3.5 border-l-[3px] px-5 py-4 transition-colors duration-150 ${kind.accent} ${
                    item.isRead ? 'bg-transparent' : 'bg-sunken/45'
                  }`}
                >
                  <span
                    className={`mt-0.5 grid size-8 shrink-0 place-items-center rounded-full ${kind.chip}`}
                  >
                    <Icon name={kind.icon} size={16} />
                  </span>

                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-baseline gap-x-2 gap-y-0.5">
                      <h3
                        className={`text-[14px] leading-snug ${
                          item.isRead ? 'font-medium text-muted' : 'font-semibold text-ink'
                        }`}
                      >
                        {item.title}
                      </h3>
                      {!item.isRead && (
                        <span className="size-1.5 shrink-0 rounded-full bg-brand" aria-label="Unread" />
                      )}
                    </div>

                    <p className="mt-1 text-[13px] leading-relaxed text-muted">{item.message}</p>

                    <p className="mt-1.5 text-[11.5px] text-faint">
                      {kind.caption}
                      {item.targetArea ? ` · ${areaLabel(item.targetArea)}` : ' · direct'} ·{' '}
                      <time dateTime={item.createdAt} title={formatDateTime(item.createdAt)}>
                        {relativeTime(item.createdAt)}
                      </time>
                    </p>
                  </div>

                  {!item.isRead && (
                    <Button
                      variant="ghost"
                      size="sm"
                      className="shrink-0 self-start"
                      onClick={() => onMarkRead(item.id)}
                    >
                      Mark read
                    </Button>
                  )}
                </li>
              )
            })}
          </ul>
        )}
      </Surface>
    </div>
  )
}
