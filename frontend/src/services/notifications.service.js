import { api, toApiError, unwrap } from '../lib/api'

/**
 * notification-service (8084) — every response is wrapped in
 * { success, message, data, timestamp }.
 *
 * A row is either direct (`targetUserId`) or an area broadcast (`targetArea`),
 * never both. `GET /user/{id}` already merges the two, optionally filtered to
 * one area.
 */

export async function getFeed(userId, area) {
  try {
    const { data } = await api.get(`/api/notifications/user/${userId}`, {
      params: area ? { area } : undefined,
    })
    const list = unwrap(data)
    return Array.isArray(list) ? list : []
  } catch (error) {
    throw toApiError(error, 'Could not load your notifications.')
  }
}

export async function getUnreadCount(userId) {
  try {
    const { data } = await api.get(`/api/notifications/user/${userId}/unread-count`)
    return unwrap(data)?.unreadCount ?? 0
  } catch (error) {
    throw toApiError(error, 'Could not load your unread count.')
  }
}

export async function markRead(id) {
  try {
    const { data } = await api.patch(`/api/notifications/${id}/read`)
    return unwrap(data)
  } catch (error) {
    throw toApiError(error, 'Could not mark that as read.')
  }
}

export async function markAllRead(userId) {
  try {
    await api.patch(`/api/notifications/user/${userId}/read-all`)
  } catch (error) {
    throw toApiError(error, 'Could not mark everything as read.')
  }
}

/**
 * An emergency outage alert is not the same kind of message as a monthly
 * announcement, and the feed should not flatten them into one row style.
 */
export function isUrgent(notification) {
  return notification?.type === 'OUTAGE_ALERT'
}
