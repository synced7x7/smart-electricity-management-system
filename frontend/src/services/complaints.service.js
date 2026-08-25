import { api, toApiError, unwrap } from '../lib/api'

/**
 * Like outages, the `complaints` table is served by two services with two
 * shapes — and here the column names genuinely diverged in the database until
 * they were reconciled (backend log #31: `subject` and `title` were two
 * columns, as were `admin_remark` and `resolution_notes`).
 *
 *   complaint-service (8085) { title,   category, resolutionNotes, ... }  + envelope
 *   admin-service     (8087) { subject, —,        adminRemark,     ... }  raw
 *
 * internal                   { id, userId, area, category, title,
 *                              description, status, remark, createdAt, updatedAt }
 *
 * Note admin-service exposes no `category` at all, so it is null on the admin
 * side — the admin table must not rely on it.
 */
export function normalizeComplaint(raw) {
  if (!raw) return null
  return {
    id: raw.id,
    userId: raw.userId,
    area: raw.area,
    category: raw.category ?? null,
    title: raw.title ?? raw.subject ?? '',
    description: raw.description ?? '',
    status: raw.status,
    remark: raw.resolutionNotes ?? raw.adminRemark ?? '',
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

const normalizeList = (data) => (Array.isArray(data) ? data.map(normalizeComplaint) : [])

/* -------------------------------- customer -------------------------------- */

/**
 * complaint-service reads `userId` from the request body rather than from the
 * JWT (a known backend limitation, issue #21 — any authenticated user could
 * file in someone else's name). The caller's own id is passed explicitly and
 * is never taken from a form field.
 */
export async function submitComplaint({ userId, area, category, title, description }) {
  try {
    const { data } = await api.post('/api/complaints', {
      userId,
      area,
      category,
      title,
      description,
    })
    return normalizeComplaint(unwrap(data))
  } catch (error) {
    throw toApiError(error, 'Could not submit your complaint.')
  }
}

export async function getMyComplaints(userId) {
  try {
    const { data } = await api.get(`/api/complaints/user/${userId}`)
    return normalizeList(unwrap(data))
  } catch (error) {
    throw toApiError(error, 'Could not load your complaints.')
  }
}

/* --------------------------------- admin ---------------------------------- */

export async function listComplaintsForAdmin({ status, area, page = 0, size = 10 } = {}) {
  try {
    const { data } = await api.get('/api/admin/complaints', {
      params: { status: status || undefined, area: area || undefined, page, size },
    })
    return { ...data, content: (data.content || []).map(normalizeComplaint) }
  } catch (error) {
    throw toApiError(error, 'Could not load complaints.')
  }
}

/**
 * Triage goes through admin-service so the ADMIN role is actually enforced.
 * It writes `admin_remark`, which complaint-service now reads back as
 * `resolutionNotes` — one column, both services (backend log #31).
 */
export async function updateComplaint(id, { status, adminRemark }) {
  try {
    const { data } = await api.patch(`/api/admin/complaints/${id}`, {
      status,
      adminRemark: adminRemark || null,
    })
    return normalizeComplaint(data)
  } catch (error) {
    throw toApiError(error, 'Could not update that complaint.')
  }
}

export function isOpen(complaint) {
  return complaint?.status === 'PENDING' || complaint?.status === 'IN_PROGRESS'
}
