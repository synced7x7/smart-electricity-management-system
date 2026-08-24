import { api, ApiError, toApiError } from '../lib/api'

/**
 * admin-service (8087) returns DTOs raw. Lists use a `PageResponse` envelope:
 * { content, page, size, totalElements, totalPages, first, last }.
 *
 * Every endpoint here needs a JWT whose *account row* has role = ADMIN — the
 * role is resolved from the database on each request, not from a token claim,
 * so a demoted or deactivated admin loses access immediately.
 */

export async function getDashboard() {
  try {
    const { data } = await api.get('/api/admin/dashboard')
    return data
  } catch (error) {
    throw toApiError(error, 'Could not load the dashboard.')
  }
}

export async function getServiceHealth() {
  try {
    const { data } = await api.get('/api/admin/services/health')
    return Array.isArray(data) ? data : []
  } catch (error) {
    throw toApiError(error, 'Could not reach the health panel.')
  }
}

export async function listUsers({ isActive, page = 0, size = 10 } = {}) {
  try {
    const { data } = await api.get('/api/admin/users', {
      params: {
        isActive: isActive === undefined || isActive === '' ? undefined : isActive,
        page,
        size,
      },
    })
    return data
  } catch (error) {
    throw toApiError(error, 'Could not load users.')
  }
}

/**
 * Deactivating the last active ADMIN is refused with a 400 whose message is
 * the actual reason — the system protecting itself from lockout, not a
 * generic failure. It is given its own code so the UI can present it as a
 * guardrail rather than an error.
 */
export async function setUserActive(id, isActive) {
  try {
    const { data } = await api.patch(`/api/admin/users/${id}/status`, { isActive })
    return data
  } catch (error) {
    const apiError = toApiError(error, 'Could not change that account.')
    if (apiError.status === 400 && /only remaining active admin/i.test(apiError.message)) {
      throw new ApiError(
        'This is the only active admin account. Promote another admin before deactivating it.',
        { status: 400, code: 'LAST_ADMIN', raw: apiError.raw },
      )
    }
    throw apiError
  }
}

export async function getUserPayments(id) {
  try {
    const { data } = await api.get(`/api/admin/users/${id}/payments`)
    return Array.isArray(data) ? data : []
  } catch (error) {
    throw toApiError(error, 'Could not load that user’s payments.')
  }
}

export async function listPayments({ page = 0, size = 10 } = {}) {
  try {
    const { data } = await api.get('/api/admin/payments', { params: { page, size } })
    return data
  } catch (error) {
    throw toApiError(error, 'Could not load the payment ledger.')
  }
}

/** The six services admin-service probes, in the order it reports them. */
export function healthSummary(services) {
  const list = services || []
  return {
    total: list.length,
    up: list.filter((s) => s.status === 'UP').length,
    down: list.filter((s) => s.status === 'DOWN').length,
    unknown: list.filter((s) => s.status !== 'UP' && s.status !== 'DOWN').length,
  }
}
