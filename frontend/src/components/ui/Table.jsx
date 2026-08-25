import Button from './Button'

/**
 * Wide tables scroll inside their own container. The page body must never
 * scroll horizontally — that breaks the reading position of everything else
 * on screen, not just the table.
 */
export function TableWrap({ children, className = '' }) {
  return (
    <div className={`w-full overflow-x-auto ${className}`}>
      <table className="w-full min-w-[36rem] border-collapse text-left text-[13.5px]">
        {children}
      </table>
    </div>
  )
}

export function Th({ children, align = 'left', className = '' }) {
  return (
    <th
      scope="col"
      className={`whitespace-nowrap border-b border-line px-4 py-2.5 text-[11.5px] font-semibold tracking-[0.04em] text-muted uppercase ${
        align === 'right' ? 'text-right' : ''
      } ${className}`}
    >
      {children}
    </th>
  )
}

export function Td({ children, align = 'left', mono = false, className = '' }) {
  return (
    <td
      className={`border-b border-line px-4 py-3 align-middle text-ink ${
        align === 'right' ? 'text-right' : ''
      } ${mono ? 'tnum' : ''} ${className}`}
    >
      {children}
    </td>
  )
}

export function Tr({ children, className = '' }) {
  return (
    <tr className={`transition-colors duration-150 hover:bg-sunken/60 ${className}`}>
      {children}
    </tr>
  )
}

/**
 * Pagination states the actual range, not just a page number — "showing 11–20
 * of 47" answers the question the page number only implies.
 */
export function Pagination({ page, size, totalElements, totalPages, first, last, onPage }) {
  if (!totalPages || totalPages <= 1) {
    return totalElements ? (
      <p className="px-4 py-3 text-[12.5px] text-muted">
        {totalElements} {totalElements === 1 ? 'record' : 'records'}
      </p>
    ) : null
  }

  const from = page * size + 1
  const to = Math.min((page + 1) * size, totalElements)

  return (
    <div className="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
      <p className="tnum text-[12.5px] text-muted">
        {from}–{to} of {totalElements}
      </p>
      <div className="flex items-center gap-2">
        <Button
          variant="secondary"
          size="sm"
          disabled={first}
          onClick={() => onPage(page - 1)}
        >
          Previous
        </Button>
        <span className="tnum px-1 text-[12.5px] text-muted">
          {page + 1} / {totalPages}
        </span>
        <Button variant="secondary" size="sm" disabled={last} onClick={() => onPage(page + 1)}>
          Next
        </Button>
      </div>
    </div>
  )
}
