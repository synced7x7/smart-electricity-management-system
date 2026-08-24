import { useAuth } from '../../context/AuthContext'

/**
 * Access tokens live 15 minutes and refresh silently. This banner only appears
 * on the rare occasion the *refresh* token is also spent — the one case the
 * user genuinely has to act on. Saying so plainly is better than a redirect
 * that looks like the app forgot them.
 */
export default function SessionNotice() {
  const { expiredNotice, dismissExpiredNotice } = useAuth()
  if (!expiredNotice) return null

  return (
    <div
      role="status"
      className="animate-rise border-b border-amber/25 bg-amber-soft px-4 py-2.5 text-center text-[13px] text-amber"
    >
      Your session expired and you were signed out.
      <button
        type="button"
        onClick={dismissExpiredNotice}
        className="ml-2 font-semibold underline underline-offset-2"
      >
        Dismiss
      </button>
    </div>
  )
}
