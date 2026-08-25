import { useEffect, useRef } from 'react'
import { createPortal } from 'react-dom'

/**
 * A modal is not anchored to a trigger, so it keeps `transform-origin: center`
 * and scales from 0.97 rather than 0 — nothing in the physical world appears
 * out of nothing. The scrim dims the page because this is a blocking task;
 * a non-blocking panel would use translucency and no scrim instead.
 */
export default function Modal({ open, onClose, title, description, children, footer, size = 'md' }) {
  const panelRef = useRef(null)
  const restoreFocus = useRef(null)

  useEffect(() => {
    if (!open) return undefined

    restoreFocus.current = document.activeElement
    const { overflow } = document.body.style
    document.body.style.overflow = 'hidden'

    function onKeyDown(event) {
      if (event.key === 'Escape') {
        onClose?.()
        return
      }
      if (event.key !== 'Tab') return
      // Keep Tab inside the dialog — escaping into the page behind a scrim
      // strands keyboard users on controls they cannot see.
      const focusables = panelRef.current?.querySelectorAll(
        'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
      )
      if (!focusables?.length) return
      const first = focusables[0]
      const last = focusables[focusables.length - 1]
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault()
        last.focus()
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault()
        first.focus()
      }
    }

    document.addEventListener('keydown', onKeyDown)
    const raf = requestAnimationFrame(() => {
      const target = panelRef.current?.querySelector(
        'input:not([type="hidden"]), select, textarea, button',
      )
      target?.focus()
    })

    return () => {
      document.removeEventListener('keydown', onKeyDown)
      cancelAnimationFrame(raf)
      document.body.style.overflow = overflow
      if (restoreFocus.current instanceof HTMLElement) restoreFocus.current.focus()
    }
  }, [open, onClose])

  if (!open) return null

  const widths = { sm: 'max-w-sm', md: 'max-w-md', lg: 'max-w-lg', xl: 'max-w-2xl' }

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-end justify-center p-0 sm:items-center sm:p-6">
      <button
        type="button"
        aria-label="Close dialog"
        onClick={onClose}
        className="absolute inset-0 cursor-default bg-ink/25 backdrop-blur-[2px] motion-safe:animate-[desco-scale-in_160ms_var(--ease-out-strong)]"
      />
      <div
        ref={panelRef}
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className={`animate-scale-in relative w-full ${widths[size]} rounded-t-2xl border border-line bg-surface shadow-[0_18px_50px_-12px_rgba(0,0,0,0.35)] sm:rounded-2xl`}
      >
        {(title || description) && (
          <div className="border-b border-line px-5 py-4">
            {title && <h2 className="text-[15px] font-semibold text-ink">{title}</h2>}
            {description && <p className="mt-1 text-[13px] text-muted">{description}</p>}
          </div>
        )}
        <div className="max-h-[70vh] overflow-y-auto px-5 py-4">{children}</div>
        {footer && (
          <div className="flex flex-wrap justify-end gap-2 border-t border-line px-5 py-3.5">
            {footer}
          </div>
        )}
      </div>
    </div>,
    document.body,
  )
}
