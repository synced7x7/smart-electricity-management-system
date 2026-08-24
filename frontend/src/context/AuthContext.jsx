import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react'
import { SESSION_EXPIRED_EVENT } from '../lib/api'
import {
  clearSession,
  decodeTokenPayload,
  readSession,
  writeSession,
} from '../lib/session'
import * as authApi from '../services/auth.service'
import * as usersApi from '../services/users.service'

const AuthContext = createContext(null)

/**
 * One source of truth for "who is signed in".
 *
 * The JWT in this system carries only `sub` and `userId` — there is no role
 * claim anywhere — so the role that drives route guarding is read from
 * `GET /api/users/me` and refreshed whenever the session changes. That also
 * means a user demoted in the database loses the admin shell on their next
 * profile load, not when their token happens to expire.
 */
export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => readSession())
  const [profile, setProfile] = useState(null)
  // 'loading' until we know whether the stored token still resolves a profile.
  const [status, setStatus] = useState(() =>
    readSession().accessToken ? 'loading' : 'anonymous',
  )
  const [expiredNotice, setExpiredNotice] = useState(false)
  // Whether this session began with a registration rather than a sign-in. Only
  // used to decide whether the profile page shows its "Account created" banner
  // — a returning user with an incomplete profile must not be told they just
  // signed up.
  const [justRegistered, setJustRegistered] = useState(false)
  const mounted = useRef(true)

  useEffect(() => {
    mounted.current = true
    return () => {
      mounted.current = false
    }
  }, [])

  const loadProfile = useCallback(async () => {
    try {
      const me = await usersApi.getMe()
      if (!mounted.current) return me
      setProfile(me)
      setStatus('authenticated')
      return me
    } catch (error) {
      // A 401/403 here means the refresh interceptor already gave up; the
      // session-expired event below handles the teardown. Any other failure
      // (gateway down, user-service down) must not sign the user out — they
      // are still authenticated, we just cannot describe them yet.
      if (!mounted.current) return null
      if (error?.status === 401 || error?.status === 403) {
        clearSession()
        setSession(readSession())
        setProfile(null)
        setStatus('anonymous')
        return null
      }
      setStatus('authenticated')
      return null
    }
  }, [])

  // Resolve the stored token once on boot.
  useEffect(() => {
    if (status === 'loading') loadProfile()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // The interceptor fires this when even the refresh token is spent.
  useEffect(() => {
    function onExpired() {
      clearSession()
      setSession(readSession())
      setProfile(null)
      setStatus('anonymous')
      setExpiredNotice(true)
    }
    window.addEventListener(SESSION_EXPIRED_EVENT, onExpired)
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, onExpired)
  }, [])

  // Signing out in one tab should not leave another tab holding a live shell.
  useEffect(() => {
    function onStorage(event) {
      if (event.key !== 'desco.session') return
      const next = readSession()
      setSession(next)
      if (!next.accessToken) {
        setProfile(null)
        setStatus('anonymous')
      }
    }
    window.addEventListener('storage', onStorage)
    return () => window.removeEventListener('storage', onStorage)
  }, [])

  const startSession = useCallback(
    async (auth) => {
      const userId = auth.userId || decodeTokenPayload(auth.accessToken)?.userId || null
      writeSession({ ...auth, userId })
      setSession(readSession())
      setExpiredNotice(false)
      setStatus('loading')
      return loadProfile()
    },
    [loadProfile],
  )

  const signIn = useCallback(
    async (credentials) => {
      setJustRegistered(false)
      return startSession(await authApi.login(credentials))
    },
    [startSession],
  )

  const signUp = useCallback(
    async (details) => {
      const session = await startSession(await authApi.register(details))
      setJustRegistered(true)
      return session
    },
    [startSession],
  )

  const signOut = useCallback(() => {
    clearSession()
    setSession(readSession())
    setProfile(null)
    setStatus('anonymous')
    setExpiredNotice(false)
    setJustRegistered(false)
  }, [])

  const value = useMemo(
    () => ({
      status,
      isAuthenticated: status === 'authenticated',
      userId: session.userId,
      email: profile?.email || session.email,
      profile,
      role: profile?.role || null,
      isAdmin: profile?.role === 'ADMIN',
      /** The account-level area; a profile area overrides it once set. */
      area: profile?.area || null,
      profileComplete: usersApi.isProfileComplete(profile),
      expiredNotice,
      dismissExpiredNotice: () => setExpiredNotice(false),
      justRegistered,
      clearJustRegistered: () => setJustRegistered(false),
      signIn,
      signUp,
      signOut,
      refreshProfile: loadProfile,
      setProfile,
    }),
    [
      status,
      session,
      profile,
      expiredNotice,
      justRegistered,
      signIn,
      signUp,
      signOut,
      loadProfile,
    ],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>')
  return ctx
}
