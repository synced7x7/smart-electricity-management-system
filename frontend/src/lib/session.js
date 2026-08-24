/**
 * Token storage.
 *
 * localStorage, deliberately: this is a simulation with no backend cookie
 * support (every service is a stateless bearer-token resource server, and the
 * gateway sets no session), so httpOnly cookies aren't an option without
 * changing all eight services. Kept in one module so the tradeoff is
 * documented in exactly one place rather than smeared across the app.
 */

const KEY = 'desco.session'

/** Refresh this many seconds before the access token actually expires. */
export const REFRESH_SKEW_SECONDS = 60

const empty = {
  accessToken: null,
  refreshToken: null,
  expiresAt: null,
  userId: null,
  email: null,
}

export function readSession() {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return { ...empty }
    return { ...empty, ...JSON.parse(raw) }
  } catch {
    return { ...empty }
  }
}

function persist(next) {
  try {
    localStorage.setItem(KEY, JSON.stringify(next))
  } catch {
    /* Private mode / quota — the session simply won't survive a reload. */
  }
  return next
}

/** Called on login and register, where identity arrives with the tokens. */
export function writeSession({ userId, email, accessToken, refreshToken, expiresIn }) {
  return persist({
    userId: userId ?? null,
    email: email ?? null,
    accessToken: accessToken ?? null,
    refreshToken: refreshToken ?? null,
    expiresAt: expiresIn ? Date.now() + Number(expiresIn) * 1000 : null,
  })
}

/** Called by the refresh interceptor — must not disturb the stored identity. */
export function writeTokens({ accessToken, refreshToken, expiresIn }) {
  const current = readSession()
  return persist({
    ...current,
    accessToken: accessToken ?? current.accessToken,
    refreshToken: refreshToken ?? current.refreshToken,
    expiresAt: expiresIn ? Date.now() + Number(expiresIn) * 1000 : current.expiresAt,
  })
}

export function clearSession() {
  try {
    localStorage.removeItem(KEY)
  } catch {
    /* ignore */
  }
}

export function hasSession() {
  return Boolean(readSession().accessToken)
}

/**
 * The JWT carries only `sub` (email) and `userId` — there is no `role` claim
 * anywhere in this system, which is why role always comes from
 * `GET /api/users/me` and never from the token. This decoder exists purely to
 * recover `userId` if it is ever missing from storage; it does not verify the
 * signature and nothing security-relevant may depend on it.
 */
export function decodeTokenPayload(token) {
  if (!token) return null
  try {
    const [, payload] = token.split('.')
    if (!payload) return null
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(json)
  } catch {
    return null
  }
}
