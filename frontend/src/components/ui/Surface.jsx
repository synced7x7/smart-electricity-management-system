/**
 * The one container in the system. Everything sits on paper; a Surface is a
 * sheet laid on it. There is no second card style and no nesting of surfaces —
 * depth is used to separate regions, not to decorate them.
 */
export function Surface({ as: Tag = 'div', className = '', children, ...props }) {
  return (
    <Tag
      className={`rounded-xl border border-line bg-surface ${className}`}
      {...props}
    >
      {children}
    </Tag>
  )
}

export function SurfaceHeader({ title, description, action, className = '' }) {
  return (
    <div
      className={`flex flex-wrap items-start justify-between gap-3 border-b border-line px-5 py-4 ${className}`}
    >
      <div className="min-w-0">
        <h2 className="text-[15px] font-semibold text-ink">{title}</h2>
        {description && <p className="mt-0.5 text-[13px] text-muted">{description}</p>}
      </div>
      {action && <div className="shrink-0">{action}</div>}
    </div>
  )
}

/** A labelled value pair — the unit the profile and receipt views are built from. */
export function DataRow({ label, value, mono = false, className = '' }) {
  return (
    <div className={`flex items-baseline justify-between gap-6 py-2.5 ${className}`}>
      <dt className="shrink-0 text-[13px] text-muted">{label}</dt>
      <dd
        className={`min-w-0 text-right text-[13.5px] font-medium text-ink ${
          mono ? 'tnum' : ''
        }`}
      >
        {value ?? '—'}
      </dd>
    </div>
  )
}
