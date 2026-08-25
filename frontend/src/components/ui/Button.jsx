import { forwardRef } from 'react'
import Spinner from './Spinner'

const base =
  'relative inline-flex items-center justify-center gap-2 rounded-lg font-medium ' +
  // Only transform and colour transition — both composite cheaply. `all` would
  // drag layout properties into every state change.
  'transition-[transform,background-color,border-color,color,box-shadow] duration-150 ' +
  '[transition-timing-function:var(--ease-out-strong)] ' +
  // Press feedback is instant and slight: the button must feel like it heard you.
  'active:scale-[0.97] disabled:pointer-events-none disabled:opacity-55 ' +
  'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand'

const variants = {
  primary:
    'bg-brand text-white hover:bg-brand-hover shadow-[0_1px_2px_rgba(0,0,0,0.12)] dark:text-[#08201c]',
  secondary:
    'bg-surface text-ink border border-line hover:border-line-strong hover:bg-sunken',
  ghost: 'text-muted hover:text-ink hover:bg-sunken',
  danger: 'bg-danger text-white hover:opacity-90 dark:text-[#2a0d07]',
  quiet: 'text-brand hover:bg-brand-soft',
}

const sizes = {
  sm: 'h-8 px-3 text-[13px]',
  md: 'h-10 px-4 text-sm',
  lg: 'h-12 px-6 text-[15px]',
}

/**
 * Polymorphic via `as` so a navigation action can be a real anchor (or a
 * router `Link`) while still looking and pressing like a button. Wrapping a
 * link inside a <button> would nest two interactive elements — invalid, and
 * it breaks middle-click, keyboard activation and "open in new tab".
 */
const Button = forwardRef(function Button(
  {
    as: Tag = 'button',
    variant = 'primary',
    size = 'md',
    loading = false,
    disabled,
    className = '',
    children,
    type,
    ...props
  },
  ref,
) {
  const isButton = Tag === 'button'

  return (
    <Tag
      ref={ref}
      {...(isButton ? { type: type || 'button', disabled: disabled || loading } : {})}
      {...(!isButton && (disabled || loading) ? { 'aria-disabled': true, tabIndex: -1 } : {})}
      aria-busy={loading || undefined}
      className={`${base} ${variants[variant]} ${sizes[size]} ${
        !isButton && (disabled || loading) ? 'pointer-events-none opacity-55' : ''
      } ${className}`}
      {...props}
    >
      {/* The label keeps its box while loading so the button never resizes
          mid-action — a width jump reads as a second, unrelated event. */}
      <span
        className={`inline-flex items-center gap-2 transition-opacity duration-150 ${
          loading ? 'opacity-0' : 'opacity-100'
        }`}
      >
        {children}
      </span>
      {loading && (
        <span className="absolute inset-0 grid place-items-center">
          <Spinner size={size === 'sm' ? 13 : 15} />
        </span>
      )}
    </Tag>
  )
})

export default Button
