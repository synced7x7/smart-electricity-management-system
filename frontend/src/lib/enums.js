/**
 * Enum values are a contract with the database — these must match `pg_enum`
 * exactly (see DOCUMENTATION_BACKEND.md, "Key schema rules"). Never send a
 * prettified label to the API; use `label()` for display only.
 */

/**
 * Service areas moved to ./areas.js when they grew from the 8 original DESCO
 * zones to all 64 districts of Bangladesh. Re-exported here so the many
 * `import { AREAS, areaLabel } from '../lib/enums'` call sites keep working and
 * there is still one import path for "enum values the backend accepts".
 */
export {
  ALL_AREAS as AREAS,
  DISTRICTS,
  DISTRICTS_BY_DIVISION,
  LEGACY_ZONES,
  areaLabel,
  divisionOf,
  isLegacyZone,
  searchAreas,
} from './areas'

export const PAYMENT_STATUSES = ['PENDING', 'SUCCESS', 'FAILED']

export const OUTAGE_TYPES = ['SCHEDULED', 'EMERGENCY']
export const OUTAGE_STATUSES = ['SCHEDULED', 'ONGOING', 'RESOLVED', 'CANCELLED']

export const COMPLAINT_CATEGORIES = [
  'BILLING',
  'POWER_CUT',
  'VOLTAGE_FLUCTUATION',
  'METER_FAULT',
  'OTHER',
]
export const COMPLAINT_STATUSES = ['PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED']

export const NOTIFICATION_TYPES = [
  'OUTAGE_ALERT',
  'COMPLAINT_UPDATE',
  'GENERAL_ANNOUNCEMENT',
  'BILLING_ALERT',
]

/** UPPER_SNAKE_CASE -> "Upper snake case", for display only. */
export function label(value) {
  if (!value) return '—'
  const words = String(value).toLowerCase().split('_')
  return words[0].charAt(0).toUpperCase() + words[0].slice(1) + (words.length > 1 ? ' ' + words.slice(1).join(' ') : '')
}

/**
 * Semantic tone per state. Only three tones carry colour in this system —
 * everything else is neutral — so a coloured badge always means something.
 */
export const TONE = {
  // payments
  SUCCESS: 'positive',
  PENDING: 'warning',
  FAILED: 'critical',
  // outages
  SCHEDULED: 'info',
  ONGOING: 'critical',
  RESOLVED: 'positive',
  CANCELLED: 'neutral',
  // complaints
  IN_PROGRESS: 'info',
  REJECTED: 'neutral',
  // service health
  UP: 'positive',
  DOWN: 'critical',
  UNKNOWN: 'neutral',
  // outage type
  EMERGENCY: 'critical',
}

export function toneFor(value) {
  return TONE[value] || 'neutral'
}
