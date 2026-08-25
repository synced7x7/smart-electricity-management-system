import Button from './Button'
import Spinner from './Spinner'

/**
 * Loading placeholders match the shape of what is arriving, so the layout
 * does not jump when data lands.
 */
export function Skeleton({ className = '' }) {
  return (
    <div
      className={`animate-pulse rounded-md bg-sunken motion-reduce:animate-none ${className}`}
      aria-hidden="true"
    />
  )
}

export function SkeletonRows({ rows = 3, className = '' }) {
  return (
    <div className={`grid gap-3 ${className}`} aria-hidden="true">
      {Array.from({ length: rows }).map((_, i) => (
        <Skeleton key={i} className="h-14" />
      ))}
    </div>
  )
}

export function Loading({ label = 'Loading', className = '' }) {
  return (
    <div className={`flex items-center gap-2 py-10 text-sm text-muted ${className}`}>
      <Spinner />
      <span>{label}…</span>
    </div>
  )
}

/**
 * An empty state names what would be here and how to make one appear. "No
 * data" tells the user nothing they did not already know.
 */
export function EmptyState({ title, description, action, icon, className = '' }) {
  return (
    <div className={`grid justify-items-center gap-2 px-6 py-12 text-center ${className}`}>
      {icon && <div className="mb-1 text-faint">{icon}</div>}
      <p className="text-[14.5px] font-medium text-ink">{title}</p>
      {description && <p className="max-w-sm text-[13px] text-muted">{description}</p>}
      {action && <div className="mt-3">{action}</div>}
    </div>
  )
}

/**
 * A failed read is recoverable — always offer the retry rather than leaving
 * the user with a dead panel and a refresh key.
 */
export function ErrorState({ error, onRetry, className = '' }) {
  return (
    <div className={`grid justify-items-center gap-2 px-6 py-12 text-center ${className}`}>
      <p className="text-[14.5px] font-medium text-ink">That didn’t load</p>
      <p className="max-w-md text-[13px] text-muted">
        {error?.message || 'Something went wrong.'}
      </p>
      {onRetry && (
        <Button variant="secondary" size="sm" className="mt-3" onClick={onRetry}>
          Try again
        </Button>
      )}
    </div>
  )
}

/** Inline form-level error — validation the field-level errors can't express. */
export function FormError({ children, className = '' }) {
  if (!children) return null
  return (
    <p
      role="alert"
      className={`animate-rise rounded-lg border border-danger/25 bg-danger-soft px-3 py-2.5 text-[13px] text-danger ${className}`}
    >
      {children}
    </p>
  )
}
