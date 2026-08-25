import { createPortal } from 'react-dom'
import { useToast } from '../../context/ToastContext'

const tones = {
  neutral: 'border-line bg-surface text-ink',
  positive: 'border-brand/30 bg-surface text-ink',
  critical: 'border-danger/30 bg-surface text-ink',
  info: 'border-info/30 bg-surface text-ink',
}

const accents = {
  neutral: 'bg-faint',
  positive: 'bg-brand',
  critical: 'bg-danger',
  info: 'bg-info',
}

/**
 * Toasts enter and leave along the same path (up from the bottom edge), so
 * the direction of travel matches where they live. `@starting-style` gives
 * the entrance without a mount effect, and a transition rather than a
 * keyframe means a rapid second toast retargets instead of restarting.
 */
export default function Toaster() {
  const { toasts, dismiss } = useToast()
  if (!toasts.length) return null

  return createPortal(
    <div
      className="pointer-events-none fixed inset-x-0 bottom-0 z-[60] flex flex-col items-center gap-2 p-4 sm:items-end sm:p-6"
      role="region"
      aria-label="Notifications"
    >
      {toasts.map((toast) => (
        <div
          key={toast.id}
          role="status"
          className={`animate-rise pointer-events-auto flex w-full max-w-sm items-start gap-3 overflow-hidden rounded-xl border py-3 pr-3 pl-0 shadow-[0_10px_30px_-12px_rgba(0,0,0,0.35)] ${tones[toast.tone]}`}
        >
          <span className={`h-full w-[3px] shrink-0 self-stretch ${accents[toast.tone]}`} />
          <p className="flex-1 py-0.5 text-[13.5px] leading-snug">{toast.message}</p>
          <button
            type="button"
            onClick={() => dismiss(toast.id)}
            aria-label="Dismiss"
            className="-mr-1 shrink-0 rounded-md p-1 text-faint transition-colors duration-150 hover:bg-sunken hover:text-ink"
          >
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
              <path
                d="m3.5 3.5 7 7m0-7-7 7"
                stroke="currentColor"
                strokeWidth="1.6"
                strokeLinecap="round"
              />
            </svg>
          </button>
        </div>
      ))}
    </div>,
    document.body,
  )
}
