/**
 * A fast spinner makes a load feel shorter than a slow one at identical
 * latency, so this runs at 600ms per revolution rather than the usual ~1s.
 */
export default function Spinner({ size = 16, className = '' }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 16 16"
      fill="none"
      aria-hidden="true"
      className={`animate-spin-fast ${className}`}
    >
      <circle cx="8" cy="8" r="6.5" stroke="currentColor" strokeOpacity="0.22" strokeWidth="2" />
      <path
        d="M14.5 8a6.5 6.5 0 0 0-6.5-6.5"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
    </svg>
  )
}
