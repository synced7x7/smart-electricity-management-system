/**
 * Service areas — GENERATED. Do not hand-edit.
 *
 * Regenerate together with the Java enums and the SQL migration; the three
 * must agree exactly, because these strings are a database enum contract.
 * See db/02_area_nationwide.sql.
 *
 * A service area is any of the 64 districts of Bangladesh. The 8 original
 * DESCO zones are Dhaka neighbourhoods rather than districts; they are kept
 * because existing accounts still use them and the database cannot drop an
 * enum value, but they are listed separately so new users pick a district.
 */

/** The 8 original DESCO zones. Legacy — still valid, still selectable. */
export const LEGACY_ZONES = [
  'UTTARA',
  'GULSHAN',
  'BANANI',
  'DHANMONDI',
  'BASHUNDHARA',
  'MIRPUR',
  'BANASREE',
  'BARIDHARA',
]

/** The 64 districts, grouped by division — the grouping the picker renders. */
export const DISTRICTS_BY_DIVISION = {
  'Barishal': [
    'BARGUNA',
    'BARISHAL',
    'BHOLA',
    'JHALOKATI',
    'PATUAKHALI',
    'PIROJPUR',
  ],
  'Chattogram': [
    'BANDARBAN',
    'BRAHMANBARIA',
    'CHANDPUR',
    'CHATTOGRAM',
    'CUMILLA',
    'COXS_BAZAR',
    'FENI',
    'KHAGRACHHARI',
    'LAKSHMIPUR',
    'NOAKHALI',
    'RANGAMATI',
  ],
  'Dhaka': [
    'DHAKA',
    'FARIDPUR',
    'GAZIPUR',
    'GOPALGANJ',
    'KISHOREGANJ',
    'MADARIPUR',
    'MANIKGANJ',
    'MUNSHIGANJ',
    'NARAYANGANJ',
    'NARSINGDI',
    'RAJBARI',
    'SHARIATPUR',
    'TANGAIL',
  ],
  'Khulna': [
    'BAGERHAT',
    'CHUADANGA',
    'JASHORE',
    'JHENAIDAH',
    'KHULNA',
    'KUSHTIA',
    'MAGURA',
    'MEHERPUR',
    'NARAIL',
    'SATKHIRA',
  ],
  'Mymensingh': [
    'JAMALPUR',
    'MYMENSINGH',
    'NETROKONA',
    'SHERPUR',
  ],
  'Rajshahi': [
    'BOGURA',
    'CHAPAINAWABGANJ',
    'JOYPURHAT',
    'NAOGAON',
    'NATORE',
    'PABNA',
    'RAJSHAHI',
    'SIRAJGANJ',
  ],
  'Rangpur': [
    'DINAJPUR',
    'GAIBANDHA',
    'KURIGRAM',
    'LALMONIRHAT',
    'NILPHAMARI',
    'PANCHAGARH',
    'RANGPUR',
    'THAKURGAON',
  ],
  'Sylhet': [
    'HABIGANJ',
    'MOULVIBAZAR',
    'SUNAMGANJ',
    'SYLHET',
  ],
}

export const DISTRICTS = Object.values(DISTRICTS_BY_DIVISION).flat()

/** Every accepted value. Order matters only for the ungrouped fallback. */
export const ALL_AREAS = [...DISTRICTS, ...LEGACY_ZONES]

/** Enum label -> human label, for the cases title-casing gets wrong. */
const DISPLAY = {
  COXS_BAZAR: "Cox's Bazar",
  BRAHMANBARIA: "Brahmanbaria",
  CHAPAINAWABGANJ: "Chapainawabganj",
}

const DIVISION_OF = (() => {
  const map = {}
  for (const [division, districts] of Object.entries(DISTRICTS_BY_DIVISION)) {
    for (const d of districts) map[d] = division
  }
  return map
})()

/** Which division a district belongs to; null for the legacy zones. */
export function divisionOf(area) {
  return DIVISION_OF[area] || null
}

export function isLegacyZone(area) {
  return LEGACY_ZONES.includes(area)
}

/** Display name for an area value. Unknown values pass through readably. */
export function areaLabel(area) {
  if (!area) return 'No area set'
  if (DISPLAY[area]) return DISPLAY[area]
  return String(area)
    .split('_')
    .map((w) => w.charAt(0) + w.slice(1).toLowerCase())
    .join(' ')
}

/**
 * Fuzzy-ish search used by the area picker: matches on the display name, the
 * raw enum value, and the division, so typing "chatto", "COXS" or "sylhet"
 * all find something sensible.
 */
export function searchAreas(query) {
  const q = query.trim().toLowerCase()
  if (!q) return null
  return ALL_AREAS.filter((a) => {
    const division = DIVISION_OF[a] || ""
    return (
      areaLabel(a).toLowerCase().includes(q) ||
      a.toLowerCase().includes(q) ||
      division.toLowerCase().includes(q)
    )
  })
}
