import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import Icon from '../ui/Icon'
import Brand from './Brand'
import SessionNotice from './SessionNotice'

const NAV = [
  { to: '/admin', label: 'Dashboard', icon: 'grid', end: true },
  { to: '/admin/users', label: 'Users', icon: 'users' },
  { to: '/admin/outages', label: 'Outages', icon: 'bolt' },
  { to: '/admin/complaints', label: 'Complaints', icon: 'message' },
  { to: '/admin/payments', label: 'Payments', icon: 'wallet' },
  { to: '/admin/profile', label: 'My profile', icon: 'user' },
]

/**
 * The admin shell is the opposite brief to the customer one: persistent
 * navigation, full-width content, and density. An operator is scanning across
 * records, not completing a single task.
 */
export default function AdminShell() {
  const { profile, email, signOut } = useAuth()
  const location = useLocation()
  const [navOpen, setNavOpen] = useState(false)

  // Navigating should close the drawer; leaving it open over the new page
  // makes the tap feel like it did not register.
  useEffect(() => setNavOpen(false), [location.pathname])

  const navList = (
    <nav className="grid gap-0.5" aria-label="Admin">
      {NAV.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          end={item.end}
          className={({ isActive }) =>
            `flex items-center gap-2.5 rounded-lg px-3 py-2 text-[13.5px] font-medium transition-colors duration-150 ${
              isActive
                ? 'bg-brand-soft text-brand-ink dark:text-brand'
                : 'text-muted hover:bg-sunken hover:text-ink'
            }`
          }
        >
          <Icon name={item.icon} size={18} />
          {item.label}
        </NavLink>
      ))}
    </nav>
  )

  return (
    <div className="min-h-dvh bg-paper">
      <SessionNotice />

      <div className="flex">
        {/* Desktop rail */}
        <aside className="sticky top-0 hidden h-dvh w-60 shrink-0 flex-col border-r border-line bg-surface px-3 py-4 lg:flex">
          <Link to="/admin" className="mb-6 flex items-center gap-2 px-2 text-brand">
            <Brand size={26} />
            <span className="grid">
              <span className="text-[14px] font-semibold text-ink">DESCO</span>
              <span className="text-[10.5px] font-medium tracking-[0.08em] text-faint uppercase">
                Operations
              </span>
            </span>
          </Link>

          {navList}

          <div className="mt-auto border-t border-line pt-3">
            {/* The identity block is the obvious place to click to edit
                yourself, so make it actually do that. */}
            <Link
              to="/admin/profile"
              className="block rounded-lg px-3 py-1 transition-colors duration-150 hover:bg-sunken"
            >
              <p className="truncate text-[12.5px] font-medium text-ink">
                {profile?.fullName || email}
              </p>
              <p className="text-[11px] tracking-[0.06em] text-faint uppercase">
                Administrator
              </p>
            </Link>
            <button
              type="button"
              onClick={signOut}
              className="mt-2 flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-[13px] font-medium text-muted transition-colors duration-150 hover:bg-sunken hover:text-ink active:scale-[0.98]"
            >
              <Icon name="logout" size={17} />
              Sign out
            </button>
          </div>
        </aside>

        <div className="min-w-0 flex-1">
          {/* Mobile bar */}
          <header className="sticky top-0 z-30 flex h-14 items-center gap-3 border-b border-line bg-paper/85 px-4 backdrop-blur-xl lg:hidden">
            <button
              type="button"
              onClick={() => setNavOpen((v) => !v)}
              aria-expanded={navOpen}
              aria-label="Toggle navigation"
              className="rounded-lg p-2 text-muted transition-colors duration-150 hover:bg-sunken hover:text-ink active:scale-[0.97]"
            >
              <Icon name="menu" size={18} />
            </button>
            <span className="flex items-center gap-2 text-brand">
              <Brand size={24} />
              <span className="text-[14px] font-semibold text-ink">Operations</span>
            </span>
            <button
              type="button"
              onClick={signOut}
              aria-label="Sign out"
              className="ml-auto rounded-lg p-2 text-muted transition-colors duration-150 hover:bg-sunken hover:text-ink active:scale-[0.97]"
            >
              <Icon name="logout" size={18} />
            </button>
          </header>

          {navOpen && (
            <div className="animate-rise border-b border-line bg-surface px-3 py-3 lg:hidden">
              {navList}
            </div>
          )}

          <main className="px-4 py-6 sm:px-6 lg:px-8">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  )
}
