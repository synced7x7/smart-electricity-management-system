import { forwardRef, useId } from 'react'

const control =
  'w-full rounded-lg border bg-surface px-3 text-sm text-ink placeholder:text-faint ' +
  'transition-[border-color,box-shadow] duration-150 [transition-timing-function:var(--ease-out-strong)] ' +
  'focus:outline-none focus:border-brand focus:ring-2 focus:ring-brand/15 ' +
  'disabled:bg-sunken disabled:text-muted'

/**
 * Label, control, hint and error travel together so the error can never be
 * rendered next to the wrong input, and so validation appears inline rather
 * than as a summary the user has to map back to a field themselves.
 */
export function Field({ label, hint, error, required, htmlFor, children, className = '' }) {
  return (
    <div className={`grid gap-1.5 ${className}`}>
      {label && (
        <label htmlFor={htmlFor} className="text-[13px] font-medium text-ink">
          {label}
          {required && <span className="ml-0.5 text-danger">*</span>}
        </label>
      )}
      {children}
      {error ? (
        <p className="text-[12.5px] text-danger">{error}</p>
      ) : hint ? (
        <p className="text-[12.5px] text-muted">{hint}</p>
      ) : null}
    </div>
  )
}

export const Input = forwardRef(function Input(
  { label, hint, error, required, className = '', id, ...props },
  ref,
) {
  const autoId = useId()
  const inputId = id || autoId
  return (
    <Field label={label} hint={hint} error={error} required={required} htmlFor={inputId}>
      <input
        ref={ref}
        id={inputId}
        required={required}
        aria-invalid={error ? true : undefined}
        className={`${control} h-10 ${error ? 'border-danger' : 'border-line'} ${className}`}
        {...props}
      />
    </Field>
  )
})

export const Select = forwardRef(function Select(
  { label, hint, error, required, className = '', id, children, ...props },
  ref,
) {
  const autoId = useId()
  const selectId = id || autoId
  return (
    <Field label={label} hint={hint} error={error} required={required} htmlFor={selectId}>
      <select
        ref={ref}
        id={selectId}
        required={required}
        aria-invalid={error ? true : undefined}
        className={`${control} h-10 appearance-none bg-[length:16px] bg-[right_0.65rem_center] bg-no-repeat pr-9 ${
          error ? 'border-danger' : 'border-line'
        } ${className}`}
        style={{
          backgroundImage:
            "url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 16 16' fill='none'%3E%3Cpath d='M4 6.5 8 10.5 12 6.5' stroke='%236a6e68' stroke-width='1.6' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E\")",
        }}
        {...props}
      >
        {children}
      </select>
    </Field>
  )
})

export const Textarea = forwardRef(function Textarea(
  { label, hint, error, required, className = '', id, rows = 4, ...props },
  ref,
) {
  const autoId = useId()
  const textareaId = id || autoId
  return (
    <Field label={label} hint={hint} error={error} required={required} htmlFor={textareaId}>
      <textarea
        ref={ref}
        id={textareaId}
        rows={rows}
        required={required}
        aria-invalid={error ? true : undefined}
        className={`${control} resize-y py-2.5 leading-relaxed ${
          error ? 'border-danger' : 'border-line'
        } ${className}`}
        {...props}
      />
    </Field>
  )
})
