import { useCallback, useEffect, useId, useLayoutEffect, useMemo, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import {
  DISTRICTS_BY_DIVISION,
  LEGACY_ZONES,
  areaLabel,
  divisionOf,
  isLegacyZone,
  searchAreas,
} from '../../lib/areas'
import { Field } from './Field'
import Icon from './Icon'

/**
 * Area selector.
 *
 * A plain <select> was fine for 8 DESCO zones; with 72 values (64 districts plus
 * the 8 legacy zones) it stops being usable — you cannot scan it, and native
 * select type-ahead only matches a leading prefix. So this is a real combobox:
 * type to filter, arrows to move, Enter to pick.
 *
 * Districts are grouped by division because that is how people locate their own
 * district, and the legacy Dhaka zones are pushed to their own group at the end
 * so a new user naturally lands on a district instead.
 */
export default function AreaPicker({
  label = 'Service area',
  value,
  onChange,
  error,
  hint,
  required,
  disabled,
  /** Text for the "nothing selected" state — differs per form. */
  placeholder = 'Select a district',
  /** Renders a clear/no-selection option with this label. */
  emptyOption,
  id,
  name,
  className = '',
}) {
  const autoId = useId()
  const buttonId = id || autoId
  const listId = `${buttonId}-listbox`

  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')
  const [activeIndex, setActiveIndex] = useState(0)

  const rootRef = useRef(null)
  const inputRef = useRef(null)
  const listRef = useRef(null)
  const popoverRef = useRef(null)
  const buttonRef = useRef(null)

  /**
   * The popover is rendered into document.body and positioned in viewport
   * coordinates.
   *
   * As a normal absolutely-positioned child it was cropped by the nearest
   * scrolling ancestor: inside the "Schedule outage" dialog — whose body is
   * `max-h-[70vh] overflow-y-auto` — 196px of the 256px list was cut off,
   * leaving about two options reachable. No z-index fixes that; the only way
   * out of a clipping container is to not be inside it.
   */
  const [rect, setRect] = useState(null)

  const position = useCallback(() => {
    const trigger = buttonRef.current
    if (!trigger) return
    const r = trigger.getBoundingClientRect()
    const MAX_H = 320
    const GAP = 4
    const below = window.innerHeight - r.bottom - GAP
    const above = r.top - GAP
    // Flip up only when below is genuinely cramped and above is roomier.
    const flip = below < 200 && above > below
    setRect({
      left: r.left,
      width: r.width,
      top: flip ? undefined : r.bottom + GAP,
      bottom: flip ? window.innerHeight - r.top + GAP : undefined,
      maxHeight: Math.min(MAX_H, Math.max(160, flip ? above : below)),
    })
  }, [])

  /**
   * One flat list of selectable values drives keyboard navigation, alongside a
   * grouped view for rendering. Keeping them derived from the same source means
   * the highlighted row and the Enter key can never disagree.
   */
  const { groups, flat } = useMemo(() => {
    const matches = searchAreas(query)
    const allow = matches ? new Set(matches) : null

    const built = []
    for (const [division, districts] of Object.entries(DISTRICTS_BY_DIVISION)) {
      const items = allow ? districts.filter((d) => allow.has(d)) : districts
      if (items.length) built.push({ heading: division, items })
    }
    const zones = allow ? LEGACY_ZONES.filter((z) => allow.has(z)) : LEGACY_ZONES
    if (zones.length) built.push({ heading: 'DESCO zones (Dhaka)', items: zones })

    const flattened = []
    if (emptyOption && !query) flattened.push('')
    for (const g of built) flattened.push(...g.items)
    return { groups: built, flat: flattened }
  }, [query, emptyOption])

  // Any change to the result set invalidates the old highlight position.
  useEffect(() => {
    setActiveIndex(0)
  }, [query])

  // Open on the current value so the list starts where the user left it.
  useEffect(() => {
    if (!open) return
    const index = flat.indexOf(value || '')
    setActiveIndex(index >= 0 ? index : 0)
    const raf = requestAnimationFrame(() => inputRef.current?.focus())
    return () => cancelAnimationFrame(raf)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open])

  // Keep the highlighted row in view during keyboard navigation.
  useEffect(() => {
    if (!open) return
    listRef.current
      ?.querySelector('[data-active="true"]')
      ?.scrollIntoView({ block: 'nearest' })
  }, [activeIndex, open])

  useLayoutEffect(() => {
    if (!open) return undefined
    position()
    // `true` captures scrolls on any ancestor, not just the window — the
    // dialog body scrolls, and the popover has to follow its trigger.
    window.addEventListener('scroll', position, true)
    window.addEventListener('resize', position)
    return () => {
      window.removeEventListener('scroll', position, true)
      window.removeEventListener('resize', position)
    }
  }, [open, position])

  useEffect(() => {
    if (!open) return undefined
    function onPointerDown(event) {
      // The popover is portaled out, so rootRef no longer contains it.
      if (rootRef.current?.contains(event.target)) return
      if (popoverRef.current?.contains(event.target)) return
      close()
    }
    document.addEventListener('mousedown', onPointerDown)
    return () => document.removeEventListener('mousedown', onPointerDown)
  }, [open])

  function close() {
    setOpen(false)
    setQuery('')
  }

  function commit(next) {
    onChange?.({ target: { name, value: next } })
    close()
  }

  function onKeyDown(event) {
    if (!open) {
      if (event.key === 'ArrowDown' || event.key === 'Enter' || event.key === ' ') {
        event.preventDefault()
        setOpen(true)
      }
      return
    }
    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault()
        setActiveIndex((i) => (flat.length ? (i + 1) % flat.length : 0))
        break
      case 'ArrowUp':
        event.preventDefault()
        setActiveIndex((i) => (flat.length ? (i - 1 + flat.length) % flat.length : 0))
        break
      case 'Home':
        event.preventDefault()
        setActiveIndex(0)
        break
      case 'End':
        event.preventDefault()
        setActiveIndex(Math.max(0, flat.length - 1))
        break
      case 'Enter':
        event.preventDefault()
        if (flat.length) commit(flat[activeIndex])
        break
      case 'Escape':
        event.preventDefault()
        close()
        break
      case 'Tab':
        close()
        break
      default:
        break
    }
  }

  const selectedLabel = value ? areaLabel(value) : null
  const division = value ? divisionOf(value) : null

  return (
    <Field label={label} hint={hint} error={error} required={required} htmlFor={buttonId}>
      <div ref={rootRef} className={`relative ${className}`}>
        {/* The real value, so this works inside an uncontrolled form too. */}
        <input type="hidden" name={name} value={value || ''} />

        <button
          ref={buttonRef}
          type="button"
          id={buttonId}
          role="combobox"
          aria-expanded={open}
          aria-controls={open ? listId : undefined}
          aria-haspopup="listbox"
          aria-invalid={error ? true : undefined}
          disabled={disabled}
          onClick={() => (open ? close() : setOpen(true))}
          onKeyDown={onKeyDown}
          className={`flex h-10 w-full items-center gap-2 rounded-lg border bg-surface px-3 text-left text-sm transition-[border-color,box-shadow] duration-150 [transition-timing-function:var(--ease-out-strong)] focus:border-brand focus:ring-2 focus:ring-brand/15 focus:outline-none disabled:bg-sunken disabled:text-muted ${
            error ? 'border-danger' : 'border-line'
          }`}
        >
          <span className={`min-w-0 flex-1 truncate ${value ? 'text-ink' : 'text-faint'}`}>
            {selectedLabel || placeholder}
          </span>
          {division && (
            <span className="shrink-0 text-[11.5px] text-faint">{division}</span>
          )}
          {value && isLegacyZone(value) && (
            <span className="shrink-0 text-[11.5px] text-faint">Dhaka</span>
          )}
          <svg
            width="16"
            height="16"
            viewBox="0 0 16 16"
            fill="none"
            aria-hidden="true"
            className={`shrink-0 text-muted transition-transform duration-150 ${
              open ? 'rotate-180' : ''
            }`}
          >
            <path
              d="M4 6.5 8 10.5 12 6.5"
              stroke="currentColor"
              strokeWidth="1.6"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </button>

        {open &&
          rect &&
          createPortal(
            <div
              ref={popoverRef}
              style={{
                position: 'fixed',
                left: rect.left,
                width: rect.width,
                top: rect.top,
                bottom: rect.bottom,
              }}
              // Above the modal (z-50), below the toasts (z-60).
              className="animate-scale-in z-[55] origin-top overflow-hidden rounded-xl border border-line bg-surface shadow-[0_14px_40px_-12px_rgba(0,0,0,0.35)]"
            >
            <div className="border-b border-line p-2">
              <input
                ref={inputRef}
                type="text"
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                onKeyDown={onKeyDown}
                placeholder="Search district or division…"
                aria-label="Search areas"
                aria-controls={listId}
                aria-autocomplete="list"
                className="h-8 w-full rounded-md border border-line bg-paper px-2.5 text-[13px] text-ink placeholder:text-faint focus:border-brand focus:outline-none"
              />
            </div>

            <div
              ref={listRef}
              id={listId}
              role="listbox"
              style={{ maxHeight: rect.maxHeight - 52 }}
              className="overflow-y-auto py-1"
            >
              {flat.length === 0 ? (
                <p className="px-3 py-6 text-center text-[13px] text-muted">
                  No area matches “{query}”.
                </p>
              ) : (
                <>
                  {emptyOption && !query && (
                    <Option
                      value=""
                      display={emptyOption}
                      selected={!value}
                      active={flat[activeIndex] === ''}
                      onPick={commit}
                      onHover={() => setActiveIndex(flat.indexOf(''))}
                    />
                  )}
                  {groups.map((group) => (
                    <div key={group.heading}>
                      <p className="px-3 pt-2 pb-1 text-[10.5px] font-semibold tracking-[0.06em] text-faint uppercase">
                        {group.heading}
                      </p>
                      {group.items.map((item) => (
                        <Option
                          key={item}
                          value={item}
                          display={areaLabel(item)}
                          selected={item === value}
                          active={flat[activeIndex] === item}
                          onPick={commit}
                          onHover={() => setActiveIndex(flat.indexOf(item))}
                        />
                      ))}
                    </div>
                  ))}
                </>
              )}
            </div>
            </div>,
            document.body,
          )}
      </div>
    </Field>
  )
}

function Option({ value, display, selected, active, onPick, onHover }) {
  return (
    <button
      type="button"
      role="option"
      aria-selected={selected}
      data-active={active}
      // Pointer-down rather than click: the search input has focus, and a click
      // would blur it first and race the outside-click handler that closes us.
      onMouseDown={(event) => {
        event.preventDefault()
        onPick(value)
      }}
      onMouseEnter={onHover}
      className={`flex w-full items-center gap-2 px-3 py-1.5 text-left text-[13.5px] transition-colors duration-100 ${
        active ? 'bg-sunken text-ink' : 'text-muted'
      } ${selected ? 'font-semibold text-ink' : ''}`}
    >
      <span className="min-w-0 flex-1 truncate">{display}</span>
      {selected && (
        <span className="shrink-0 text-brand">
          <Icon name="check" size={14} strokeWidth={2.2} />
        </span>
      )}
    </button>
  )
}
