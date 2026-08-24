import { api, toApiError, unwrap } from '../lib/api'
import { toServerDateTime } from '../lib/format'

/**
 * The same `outages` table is served by two services with two different JSON
 * shapes, so everything here is normalised to one internal shape before it
 * reaches a component:
 *
 *   outage-service (8083)  { title, area, outageType, status, reason,   ... }
 *   admin-service  (8087)  { title, area, type,       status, description, createdBy, ... }
 *
 * internal               { id, title, area, type, status, detail, startTime,
 *                          endTime, createdBy, createdAt, updatedAt }
 *
 * outage-service also wraps every response in { success, message, data }.
 */
export function normalizeOutage(raw) {
  if (!raw) return null
  return {
    id: raw.id,
    title: raw.title,
    area: raw.area,
    type: raw.type ?? raw.outageType ?? null,
    status: raw.status,
    detail: raw.description ?? raw.reason ?? '',
    startTime: raw.startTime,
    endTime: raw.endTime,
    createdBy: raw.createdBy ?? null,
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

const normalizeList = (data) => (Array.isArray(data) ? data.map(normalizeOutage) : [])

/* --------------------------------- reads --------------------------------- */

/** Active + scheduled — what a customer dashboard should show. */
export async function getActive() {
  try {
    const { data } = await api.get('/api/outages/active')
    return normalizeList(unwrap(data))
  } catch (error) {
    throw toApiError(error, 'Could not load current outages.')
  }
}

export async function getByArea(area) {
  try {
    const { data } = await api.get(`/api/outages/area/${area}`)
    return normalizeList(unwrap(data))
  } catch (error) {
    throw toApiError(error, 'Could not load outages for your area.')
  }
}

export async function getAll(status) {
  try {
    const { data } = await api.get('/api/outages', {
      params: status ? { status } : undefined,
    })
    return normalizeList(unwrap(data))
  } catch (error) {
    throw toApiError(error, 'Could not load outages.')
  }
}

/* --------------------------------- writes -------------------------------- */

/**
 * Admin writes go through admin-service, not outage-service: only
 * admin-service enforces the ADMIN role and stamps `createdBy` from the
 * authenticated caller rather than trusting the body.
 */
export async function createOutage({ title, description, area, type, startTime, endTime }) {
  try {
    const { data } = await api.post('/api/admin/outages', {
      title,
      description,
      area,
      type,
      // Spring's LocalDateTime binder rejects a trailing "Z" or millisecond
      // precision; the three team services pin the pattern to
      // yyyy-MM-dd'T'HH:mm:ss outright.
      startTime: toServerDateTime(startTime),
      endTime: toServerDateTime(endTime),
    })
    return normalizeOutage(data)
  } catch (error) {
    throw toApiError(error, 'Could not schedule that outage.')
  }
}

export async function updateOutageStatus(id, status) {
  try {
    const { data } = await api.patch(`/api/admin/outages/${id}/status`, { status })
    return normalizeOutage(data)
  } catch (error) {
    throw toApiError(error, 'Could not update that outage.')
  }
}

/** Paged admin listing — `PageResponse` envelope, not the ApiResponse one. */
export async function listOutagesForAdmin({ status, area, page = 0, size = 10 } = {}) {
  try {
    const { data } = await api.get('/api/admin/outages', {
      params: { status: status || undefined, area: area || undefined, page, size },
    })
    return { ...data, content: (data.content || []).map(normalizeOutage) }
  } catch (error) {
    throw toApiError(error, 'Could not load outages.')
  }
}

/** An outage is "live" when it is currently interrupting supply. */
export function isLive(outage) {
  return outage?.status === 'ONGOING'
}

/** Emergencies and live outages outrank scheduled future work. */
export function outageUrgency(outage) {
  if (outage?.status === 'ONGOING') return 2
  if (outage?.type === 'EMERGENCY') return 1
  return 0
}

export function sortByUrgency(list) {
  return [...(list || [])].sort((a, b) => {
    const diff = outageUrgency(b) - outageUrgency(a)
    if (diff !== 0) return diff
    return new Date(a.startTime || 0) - new Date(b.startTime || 0)
  })
}
