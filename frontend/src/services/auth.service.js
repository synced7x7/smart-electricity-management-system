import { api, ApiError, toApiError } from '../lib/api'

/**
 * auth-service returns `AuthResponse` raw (no envelope):
 * { userId, email, accessToken, refreshToken, tokenType, expiresIn }
 */

export async function login({ email, password }) {
  try {
    const { data } = await api.post(
      '/api/auth/login',
      { email, password },
      { skipAuth: true },
    )
    return data
  } catch (error) {
    throw toApiError(error, 'Could not sign in.')
  }
}

export async function register({ email, password, area, adminKey }) {
  try {
    const body = { email, password }
    // `area` is optional at registration; sending null would fail enum parsing.
    if (area) body.area = area
    // Only sent when the caller actually typed one. An empty string would be
    // treated as "I am claiming admin" and rejected.
    if (adminKey && adminKey.trim()) body.adminKey = adminKey.trim()
    const { data } = await api.post('/api/auth/register', body, { skipAuth: true })
    return data
  } catch (error) {
    const apiError = toApiError(error, 'Could not create your account.')
    // 403 here is always the administrator key being wrong or not enabled —
    // it is the only privileged claim this endpoint accepts.
    if (apiError.status === 403) {
      throw new ApiError(apiError.message, {
        status: 403,
        code: 'BAD_ADMIN_KEY',
        raw: apiError.raw,
      })
    }
    // auth-service answers a duplicate email with 401 + "Email already
    // registered" rather than 409. Surfacing that verbatim on a sign-*up* form
    // reads as "your credentials are wrong", the opposite of what happened.
    if (apiError.status === 401 && /already registered/i.test(apiError.message)) {
      throw new ApiError('An account with this email already exists. Sign in instead.', {
        status: 409,
        code: 'EMAIL_TAKEN',
        raw: apiError.raw,
      })
    }
    throw apiError
  }
}
