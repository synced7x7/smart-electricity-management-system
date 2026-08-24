import { label as prettify, toneFor } from '../../lib/enums'

/**
 * Colour is scarce on purpose. Neutral is the default so that a coloured
 * badge always signals something the reader should act on — money that
 * failed, supply that is out, a service that is down.
 */
const tones = {
  neutral: 'bg-sunken text-muted border-line',
  positive: 'bg-brand-soft text-brand-ink border-brand/25 dark:text-brand',
  warning: 'bg-amber-soft text-amber border-amber/25',
  critical: 'bg-danger-soft text-danger border-danger/25',
  info: 'bg-info-soft text-info border-info/25',
}

export default function Badge({ tone, value, children, dot = false, className = '' }) {
  const resolved = tone || toneFor(value)
  return (
    <span
      className={`inline-flex items-center gap-1.5 whitespace-nowrap rounded-full border px-2 py-0.5 text-[11.5px] font-medium tracking-[0.01em] ${tones[resolved]} ${className}`}
    >
      {dot && <span className="size-1.5 rounded-full bg-current" aria-hidden="true" />}
      {children ?? prettify(value)}
    </span>
  )
}

/**
 * A live outage is the one state in the customer app that should feel
 * genuinely alarming, so it gets a pulse. Nothing else in the system does —
 * if everything pulses, nothing does.
 */
export function LiveBadge({ className = '' }) {
  return (
    <span
      className={`inline-flex items-center gap-1.5 whitespace-nowrap rounded-full border border-danger/30 bg-danger-soft px-2 py-0.5 text-[11.5px] font-semibold text-danger ${className}`}
    >
      <span className="relative flex size-1.5">
        <span className="absolute inline-flex size-full animate-ping rounded-full bg-danger opacity-70 motion-reduce:hidden" />
        <span className="relative inline-flex size-1.5 rounded-full bg-danger" />
      </span>
      Live now
    </span>
  )
}
