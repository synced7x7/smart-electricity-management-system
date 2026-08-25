import axios from 'axios'
import { clearSession, readSession, writeTokens } from './session'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 20_000,
})

/**
 * Raised when the session can no longer be recovered. `AuthProvider` listens
 * for this rather than every page having to handle "the refresh token died".
 */
export const SESSION_EXPIRED_EVENT = 'desco:session-expired'

/* -------------------------------------------------------------------------- */
/* Request: attach the access token                                            */
/* -------------------------------------------------------------------------- */

api.interceptors.request.use((config) => {
  if (config.skipAuth) return config
  const { accessToken } = readSession()
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

/* -------------------------------------------------------------------------- */
/* Response: silent refresh on expiry                                          */
/* -------------------------------------------------------------------------- */

/**
 * Access tokens live 15 minutes. A half-filled complaint form must never be
 * lost to that, so a failed call is retried once behind a single shared
 * refresh — concurrent 401s wait on the same promise instead of each minting
 * their own token.
 */
let refreshInFlight = null

function refreshAccessToken() {
  if (refreshInFlight) return refreshInFlight

  const { refreshToken } = readSession()
  if (!refreshToken) return Promise.reject(new Error('No refresh token'))

  refreshInFlight = axios
    .post(
      `${API_BASE_URL}/api/auth/refresh`,
      { refreshToken },
      {
        headers: {
          'Content-Type': 'application/json',
          // The gateway only permits /api/auth/login and /api/auth/register
          // unauthenticated — `anyExchange().authenticated()` covers /refresh,
          // so an unauthenticated refresh is rejected at the edge before it
          // ever reaches auth-service. The refresh token is signed with the
          // same HMAC secret and lives 7 days, so presenting it as the bearer
          // satisfies the gateway's validity check and auth-service (which
          // does permitAll on /refresh) then handles the body normally.
          Authorization: `Bearer ${refreshToken}`,
        },
        timeout: 20_000,
      },
    )
    .then((res) => {
      const data = res.data || {}
      if (!data.accessToken) throw new Error('Refresh returned no access token')
      // auth-service reissues both tokens; keep the old refresh token if it
      // ever stops doing so rather than dropping the session.
      writeTokens({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken || refreshToken,
        expiresIn: data.expiresIn,
      })
      return data.accessToken
    })
    .finally(() => {
      refreshInFlight = null
    })

  return refreshInFlight
}

/**
 * Which failures mean "your token expired" rather than "you may not do this".
 *
 * - 401 always means expired/absent credentials in this system.
 * - 403 is ambiguous: admin-service returns 403 for a valid USER token hitting
 *   an admin route (a real authorization decision — refreshing would not help),
 *   while payment-service still returns a bodiless 403 for a missing or expired
 *   token (documented backend issue #19). So 403 is only treated as expiry on
 *   payment-service paths.
 */
function isRecoverableAuthFailure(status, url) {
  if (status === 401) return true
  if (status === 403) return String(url || '').includes('/api/payments')
  return false
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { config, response } = error
    if (!config || !response) return Promise.reject(error)

    const isAuthEndpoint = String(config.url || '').includes('/api/auth/')

    if (
      !config._retried &&
      !config.skipAuth &&
      !isAuthEndpoint &&
      isRecoverableAuthFailure(response.status, config.url)
    ) {
      config._retried = true
      try {
        const token = await refreshAccessToken()
        config.headers = { ...config.headers, Authorization: `Bearer ${token}` }
        return api(config)
      } catch {
        clearSession()
        window.dispatchEvent(new CustomEvent(SESSION_EXPIRED_EVENT))
        return Promise.reject(error)
      }
    }

    return Promise.reject(error)
  },
)

/* -------------------------------------------------------------------------- */
/* Response shape normalisation                                                */
/* -------------------------------------------------------------------------- */

/**
 * The system has two response conventions and no way to tell them apart from
 * the URL alone at the call site:
 *
 *   auth / user / payment / admin  ->  the DTO, raw
 *   outage / notification / complaint -> { success, message, data, timestamp }
 *
 * Unwrapping on the shape rather than the path means a service switching
 * convention later doesn't break the caller.
 */
export function unwrap(payload) {
  if (
    payload &&
    typeof payload === 'object' &&
    !Array.isArray(payload) &&
    'success' in payload &&
    'data' in payload
  ) {
    return payload.data
  }
  return payload
}

/* -------------------------------------------------------------------------- */
/* Errors                                                                      */
/* -------------------------------------------------------------------------- */

export class ApiError extends Error {
  constructor(message, { status, code, raw } = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status ?? 0
    /** A stable string the UI can branch on, e.g. 'DUPLICATE_PAYMENT'. */
    this.code = code
    this.raw = raw
  }
}

const NETWORK_MESSAGE =
  'Cannot reach the API gateway. Check that it is running on ' + API_BASE_URL + '.'

/**
 * Both error conventions carry `message`, so one extractor covers the system.
 * A 403 with an empty body (payment-service, backend issue #19) has no message
 * at all — hence the status-keyed fallbacks.
 */
export function toApiError(error, fallback = 'Something went wrong.') {
  if (error instanceof ApiError) return error

  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return new ApiError(
        error.code === 'ECONNABORTED' ? 'The request timed out.' : NETWORK_MESSAGE,
        { status: 0, code: 'NETWORK', raw: error },
      )
    }

    const { status, data } = error.response
    const message =
      (data && typeof data === 'object' && (data.message || data.error)) ||
      (typeof data === 'string' && data.trim() ? data : null) ||
      statusFallback(status, fallback)

    return new ApiError(message, { status, raw: data })
  }

  return new ApiError(error?.message || fallback, { raw: error })
}

function statusFallback(status, fallback) {
  switch (status) {
    case 400:
      return 'That request was rejected. Please check the fields and try again.'
    case 401:
      return 'Your session has expired. Please sign in again.'
    case 403:
      return 'You do not have permission to do that.'
    case 404:
      return 'Not found.'
    case 409:
      return 'That conflicts with something that already exists.'
    case 429:
      return 'Too many requests. Please wait a moment.'
    case 500:
    case 502:
    case 503:
      return 'The service is having trouble right now. Please try again shortly.'
    default:
      return fallback
  }
}
