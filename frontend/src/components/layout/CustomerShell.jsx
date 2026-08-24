import { useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { areaLabel } from '../../lib/enums'
import { getUnreadCount } from '../../services/notifications.service'
import Icon from '../ui/Icon'
import Brand, { Wordmark } from './Brand'
import SessionNotice from './SessionNotice'

const NAV = [
  { to: '/', label: 'Overview', icon: 'home', end: true },
  { to: '/pay', label: 'Pay bill', icon: 'wallet' },
  { to: '/complaints', label: 'Complaints', icon: 'message' },
  { to: '/alerts', label: 'Alerts', icon: 'bell' },
  { to: '/profile', label: 'Profile', icon: 'user' },
]

/**
 * The customer shell is a single centred column, not a dashboard grid. This is
 * a bill-pay flow: one task at a time, wide margins, and no sidebar competing
 * with the content for attention.
 */
export default function CustomerShell() {
  const { profile, email, signOut, userId } = useAuth()
  const location = useLocation()
  const [unread, setUnread] = useState(0)

  // The badge is the only reason the shell touches the network. It refreshes
  // on navigation rather than on a timer — a poll would keep a token alive
  // and hammer the gateway for a number nobody is watching.
  useEffect(() => {
    if (!userId) return undefined
    let cancelled = false
    getUnreadCount(userId)
      .then((count) => {
        if (!cancelled) setUnread(count)
      })
      .catch(() => {
        /* The badge is not worth an error state. */
      })
    return () => {
      cancelled = true
    }
  }, [userId, location.pathname])

  return (
    <div className="flex min-h-dvh flex-col bg-paper">
      <SessionNotice />

      {/* Translucent chrome with content scrolling beneath it, rather than an
          opaque strip that permanently consumes the top of the viewport. */}
      <header className="sticky top-0 z-30 border-b border-line bg-paper/80 backdrop-blur-xl supports-[not(backdrop-filter:blur(0))]:bg-paper">
        <div className="mx-auto flex h-14 w-full max-w-5xl items-center gap-3 px-4 sm:px-6">
          <div className="flex items-center gap-2 text-brand">
            <Brand size={26} />
            <Wordmark className="hidden sm:flex" />
          </div>

          <nav className="ml-4 hidden items-center gap-0.5 md:flex" aria-label="Main">
            {NAV.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  `relative rounded-lg px-3 py-1.5 text-[13.5px] font-medium transition-colors duration-150 ${
                    isActive ? 'bg-sunken text-ink' : 'text-muted hover:text-ink'
                  }`
                }
              >
                {item.label}
                {item.to === '/alerts' && unread > 0 && (
                  <span className="tnum ml-1.5 rounded-full bg-danger px-1.5 py-px text-[10.5px] font-semibold text-white dark:text-[#2a0d07]">
                    {unread > 99 ? '99+' : unread}
                  </span>
                )}
              </NavLink>
            ))}
          </nav>

          <div className="ml-auto flex items-center gap-3">
            <div className="hidden text-right sm:block">
              <p className="max-w-[14rem] truncate text-[13px] font-medium text-ink">
                {profile?.fullName || email}
              </p>
              <p className="text-[11.5px] text-faint">{areaLabel(profile?.area)}</p>
            </div>
            <button
              type="button"
              onClick={signOut}
              title="Sign out"
              className="rounded-lg p-2 text-muted transition-colors duration-150 hover:bg-sunken hover:text-ink active:scale-[0.97]"
            >
              <Icon name="logout" size={18} />
              <span className="sr-only">Sign out</span>
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto w-full max-w-5xl flex-1 px-4 pt-6 pb-24 sm:px-6 md:pb-12">
        <Outlet />
      </main>

      {/* On small screens the nav moves to the thumb, where it belongs. */}
      <nav
        className="fixed inset-x-0 bottom-0 z-30 border-t border-line bg-paper/90 backdrop-blur-xl md:hidden"
        aria-label="Main"
      >
        <div className="mx-auto grid max-w-lg grid-cols-5">
          {NAV.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `relative flex flex-col items-center gap-0.5 py-2.5 text-[10.5px] font-medium transition-colors duration-150 ${
                  isActive ? 'text-brand' : 'text-faint'
                }`
              }
            >
              <span className="relative">
                <Icon name={item.icon} size={19} />
                {item.to === '/alerts' && unread > 0 && (
                  <span className="absolute -top-0.5 -right-1 size-2 rounded-full bg-danger ring-2 ring-paper" />
                )}
              </span>
              {item.label}
            </NavLink>
          ))}
        </div>
      </nav>
    </div>
  )
}
