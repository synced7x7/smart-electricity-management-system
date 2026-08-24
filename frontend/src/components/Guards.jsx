import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Spinner from './ui/Spinner'

/**
 * The boot screen. Kept intentionally quiet — this appears for the fraction of
 * a second it takes to resolve a stored token, and anything more elaborate
 * would flash.
 */
function Booting() {
  return (
    <div className="grid min-h-dvh place-items-center bg-paper text-muted">
      <Spinner size={20} />
    </div>
  )
}

/**
 * Where an admin belongs when they land on a customer route.
 *
 * These are not errors — an admin following a bookmark, a stale link, or just
 * typing "localhost:5173" is doing something reasonable. But the customer
 * routes render the *customer shell*, so an admin ends up looking at "Pay bill"
 * and "Alerts" navigation for an account that has no bill and no alerts. Send
 * them to the page that answers the same question for their role.
 */
const ADMIN_EQUIVALENT = {
  '/': '/admin',
  '/pay': '/admin/payments',
  '/complaints': '/admin/complaints',
  '/profile': '/admin/profile',
  // No admin alerts page exists yet, so the console is the closest thing.
  '/alerts': '/admin',
}

/**
 * The customer shell.
 *
 * This is the mirror of `RequireAdmin`: the two experiences are separated by
 * shell, and that separation only holds if it is enforced in BOTH directions.
 * It previously was not — only `/profile` redirected, so an admin visiting `/`
 * was shown the customer dashboard, which looked like the roles shared a
 * dashboard when in fact the admin had simply fallen into the wrong app.
 */
export function RequireCustomer() {
  const { status, isAdmin } = useAuth()
  const location = useLocation()

  if (status === 'loading') return <Booting />
  if (status !== 'authenticated') {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  if (isAdmin) {
    return <Navigate to={ADMIN_EQUIVALENT[location.pathname] || '/admin'} replace />
  }
  return <Outlet />
}

/** Signed in, whatever the role. Remembers where they were headed. */
export function RequireAuth() {
  const { status } = useAuth()
  const location = useLocation()

  if (status === 'loading') return <Booting />
  if (status !== 'authenticated') {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  return <Outlet />
}

/**
 * Role gate. A USER must never render the admin shell — not even briefly —
 * so this redirects rather than hiding nav items inside a shared layout.
 *
 * While the role is still unknown (profile not yet loaded because
 * user-service is down, say) the gate holds rather than guessing. Guessing
 * "not admin" would bounce a real admin out of their own dashboard.
 */
export function RequireAdmin() {
  const { status, role } = useAuth()
  const location = useLocation()

  if (status === 'loading') return <Booting />
  // Record where they were going, exactly as RequireAuth does. Without this,
  // an admin deep-linking to /admin/payments signed in and landed on /admin —
  // the intent was dropped on the floor.
  if (status !== 'authenticated') {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  if (role === null) return <Booting />
  if (role !== 'ADMIN') return <Navigate to="/" replace />
  return <Outlet />
}

/**
 * Login and register: an already-signed-in user has no business here.
 *
 * This guard — not the pages — owns the post-authentication destination. The
 * pages used to navigate themselves, but the moment `signIn`/`signUp`
 * resolved, `status` flipped to 'authenticated' and this redirect rendered
 * first, discarding whatever the page was about to do. That silently broke
 * both "return to the page you were trying to reach" and the new-account
 * profile-completion flow. Deciding it in one place removes the race entirely.
 */
export function RequireAnonymous() {
  const { status, isAdmin, profileComplete, justRegistered } = useAuth()
  const location = useLocation()

  if (status === 'loading') return <Booting />
  if (status !== 'authenticated') return <Outlet />

  // 1. Wherever RequireAuth bounced them from, if anything.
  const intended = location.state?.from?.pathname
  if (intended && intended !== '/login' && intended !== '/register') {
    return <Navigate to={intended} replace />
  }

  // 2. Admins go to their own shell.
  if (isAdmin) return <Navigate to="/admin" replace />

  // 3. A customer with no profile row yet cannot use most of the app, so the
  //    completion flow comes before the dashboard.
  if (!profileComplete) {
    return <Navigate to={justRegistered ? '/profile?welcome=1' : '/profile'} replace />
  }

  return <Navigate to="/" replace />
}
