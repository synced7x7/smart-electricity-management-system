/**
 * The mark: a meter dial with a bolt through it. Drawn rather than imported so
 * it inherits `currentColor` and works on paper, on the brand block, and in
 * dark mode without three variants.
 */
export default function Brand({ size = 28, className = '' }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 28 28"
      fill="none"
      aria-hidden="true"
      className={className}
    >
      <rect x="1.75" y="1.75" width="24.5" height="24.5" rx="7" fill="currentColor" opacity="0.12" />
      <rect
        x="1.75"
        y="1.75"
        width="24.5"
        height="24.5"
        rx="7"
        stroke="currentColor"
        strokeOpacity="0.28"
        strokeWidth="1.2"
      />
      <path
        d="M15.4 6.5 9 15.1h4.1l-.5 6.4L19 12.9h-4.1l.5-6.4Z"
        fill="currentColor"
      />
    </svg>
  )
}

export function Wordmark({ className = '' }) {
  return (
    <span className={`flex items-baseline gap-1.5 ${className}`}>
      <span className="text-[15px] font-semibold tracking-[-0.01em] text-ink">DESCO</span>
      <span className="text-[11px] font-medium tracking-[0.08em] text-faint uppercase">
        Portal
      </span>
    </span>
  )
}
