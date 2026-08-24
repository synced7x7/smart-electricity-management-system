/**
 * A small hand-drawn set on a 20px grid with a consistent 1.6 stroke, rather
 * than an icon package. Eight icons do not justify a dependency, and a
 * consistent stroke weight across every glyph is the thing that actually
 * makes an icon set read as one set.
 */
const paths = {
  home: 'M3.5 8.5 10 3.5l6.5 5V16a1 1 0 0 1-1 1h-3v-4.5h-5V17h-3a1 1 0 0 1-1-1V8.5Z',
  bolt: 'M11 2.5 4.5 11h4l-.5 6.5L15 9h-4l.5-6.5Z',
  wallet:
    'M3 6.5A1.5 1.5 0 0 1 4.5 5H15a1.5 1.5 0 0 1 1.5 1.5v7A1.5 1.5 0 0 1 15 15H4.5A1.5 1.5 0 0 1 3 13.5v-7Zm10 3.5h2.5',
  bell: 'M7 15a3 3 0 0 0 6 0M5 15h10l-1-1.5v-4a4 4 0 1 0-8 0v4L5 15Z',
  message: 'M4 5.5h12v8H9.5L6 16.5V13.5H4v-8Z',
  user: 'M10 10.5a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM4.5 16.5a5.5 5.5 0 0 1 11 0',
  users: 'M8 10a2.75 2.75 0 1 0 0-5.5A2.75 2.75 0 0 0 8 10Zm-4.5 6.5a4.5 4.5 0 0 1 9 0M13.5 10.5a4.5 4.5 0 0 1 3 6',
  grid: 'M3.5 3.5h5.5v5.5H3.5V3.5Zm7.5 0h5.5v5.5H11V3.5ZM3.5 11h5.5v5.5H3.5V11Zm7.5 0h5.5v5.5H11V11Z',
  receipt: 'M5 3.5h10v14l-2-1.5-1.5 1.5L10 16l-1.5 1.5L7 16l-2 1.5v-14Zm2.5 4h5m-5 3.5h5',
  logout: 'M12.5 6V4.5a1 1 0 0 0-1-1h-6a1 1 0 0 0-1 1v11a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V14M9 10h7.5m0 0-2.5-2.5M16.5 10 14 12.5',
  check: 'm4.5 10.5 3.5 3.5 7.5-8',
  plus: 'M10 4.5v11M4.5 10h11',
  clock: 'M10 5.5V10l3 1.5M17 10a7 7 0 1 1-14 0 7 7 0 0 1 14 0Z',
  alert: 'M10 7v4m0 2.5v.2M10 3 2.5 16.5h15L10 3Z',
  menu: 'M3.5 6h13m-13 4h13m-13 4h13',
  external: 'M8 4.5H5.5a1 1 0 0 0-1 1v9a1 1 0 0 0 1 1h9a1 1 0 0 0 1-1V12M11 4.5h4.5V9M9 11l6.5-6.5',
  refresh: 'M15.5 6.5A6 6 0 1 0 16 10m-.5-3.5V3m0 3.5H12',
}

export default function Icon({ name, size = 20, className = '', strokeWidth = 1.6 }) {
  const d = paths[name]
  if (!d) return null
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 20 20"
      fill="none"
      aria-hidden="true"
      className={className}
    >
      <path
        d={d}
        stroke="currentColor"
        strokeWidth={strokeWidth}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}
