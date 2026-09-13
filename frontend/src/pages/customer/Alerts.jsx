import OutageCard from '../../components/OutageCard'
import { EmptyState, ErrorState, SkeletonRows } from '../../components/ui/States'
import { Surface, SurfaceHeader } from '../../components/ui/Surface'
import { useAuth } from '../../context/AuthContext'
import { areaLabel } from '../../lib/enums'
import { useAsync } from '../../lib/useAsync'
import { getByArea } from '../../services/outages.service'

export default function Alerts() {
  const { userId, area } = useAuth()
  const outages = useAsync(() => getByArea(area), [area], {
    enabled: Boolean(userId && area),
  })
  const items = outages.data || []

  return (
    <div className="grid gap-6">
      <header className="animate-rise">
        <h1 className="text-[1.7rem] font-semibold tracking-[-0.024em] text-ink">Alerts</h1>
        <p className="mt-1 text-[14px] text-muted">
          Scheduled and ongoing outages for {area ? areaLabel(area) : 'your area'}.
        </p>
      </header>

      <Surface>
        <SurfaceHeader
          title={items.length ? `${items.length} outages` : 'No upcoming outages'}
          description={items.length ? 'Scheduled and ongoing in your area' : undefined}
        />

        {outages.loading ? (
          <div className="p-5">
            <SkeletonRows rows={4} />
          </div>
        ) : outages.error ? (
          <ErrorState error={outages.error} onRetry={outages.reload} />
        ) : items.length === 0 ? (
          <EmptyState
            title="No upcoming outages"
            description="Scheduled and ongoing outages for your area will appear here."
          />
        ) : (
          <div className="stagger grid gap-3 p-4">
            {items.map((item) => (
              <OutageCard key={item.id} outage={item} />
            ))}
          </div>
        )}
      </Surface>
    </div>
  )
}
