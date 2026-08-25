import { api, ApiError, toApiError } from '../lib/api'

/**
 * user-service returns `UserProfileResponse` raw — a merge of the account
 * (owned by auth-service) and the profile row. Before the profile exists,
 * every profile field comes back null while the account fields are populated;
 * `isProfileComplete` is what the UI branches on.
 */

export async function getMe() {
  try {
    const { data } = await api.get('/api/users/me')
    return data
  } catch (error) {
    throw toApiError(error, 'Could not load your profile.')
  }
}

export async function getUser(userId) {
  try {
    const { data } = await api.get(`/api/users/${userId}`)
    return data
  } catch (error) {
    throw toApiError(error, 'Could not load that user.')
  }
}

/**
 * Partial update: a field omitted from the body is left unchanged, not
 * cleared. Empty strings from the form are therefore stripped rather than
 * sent — sending "" would either fail validation or blank a stored value.
 *
 * `fullName` is required on the very first save because the column is NOT
 * NULL and no row exists yet; subsequent saves don't re-enforce it.
 */
export async function updateMe(patch) {
  const body = {}
  for (const [key, value] of Object.entries(patch)) {
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      body[key] = typeof value === 'string' ? value.trim() : value
    }
  }

  try {
    const { data } = await api.put('/api/users/me', body)
    return data
  } catch (error) {
    const apiError = toApiError(error, 'Could not save your profile.')
    if (apiError.status === 409) {
      throw new ApiError(
        'That meter number is already registered to another account.',
        { status: 409, code: 'DUPLICATE_METER', raw: apiError.raw },
      )
    }
    throw apiError
  }
}

export function isProfileComplete(profile) {
  return Boolean(profile?.fullName)
}

/** First name for greetings; falls back to the email local part. */
export function displayName(profile) {
  if (profile?.fullName) return profile.fullName.split(' ')[0]
  if (profile?.email) return profile.email.split('@')[0]
  return 'there'
}
