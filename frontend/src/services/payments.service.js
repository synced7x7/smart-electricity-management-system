import { api, ApiError, toApiError } from '../lib/api'

/**
 * payment-service returns `PaymentResponse` raw:
 * { id, userId, billMonth, amount, paymentMethod, transactionId, status,
 *   paidAt, createdAt }
 */

export const PAYMENT_METHODS = [
  { value: 'BKASH', label: 'bKash' },
  { value: 'NAGAD', label: 'Nagad' },
  { value: 'ROCKET', label: 'Rocket' },
  { value: 'CARD', label: 'Debit / credit card' },
  { value: 'BANK_TRANSFER', label: 'Bank transfer' },
]

export async function payBill({ userId, billMonth, amount, paymentMethod }) {
  try {
    const body = { userId, billMonth, amount: Number(amount) }
    // Omitted rather than nulled — the service defaults it to DUMMY_GATEWAY.
    if (paymentMethod) body.paymentMethod = paymentMethod
    const { data } = await api.post('/api/payments', body)
    return data
  } catch (error) {
    const apiError = toApiError(error, 'The payment could not be completed.')
    // A 409 here is not a failure — it means this bill month is already
    // settled. It gets its own code so the UI can say so plainly instead of
    // showing a red error toast for something that is actually good news.
    if (apiError.status === 409) {
      throw new ApiError('This bill month has already been paid.', {
        status: 409,
        code: 'ALREADY_PAID',
        raw: apiError.raw,
      })
    }
    throw apiError
  }
}

export async function getPayment(id) {
  try {
    const { data } = await api.get(`/api/payments/${id}`)
    return data
  } catch (error) {
    throw toApiError(error, 'Could not load that payment.')
  }
}

/** Newest first, per the service contract. */
export async function getHistory(userId) {
  try {
    const { data } = await api.get(`/api/payments/history/${userId}`)
    return Array.isArray(data) ? data : []
  } catch (error) {
    throw toApiError(error, 'Could not load your payment history.')
  }
}

/** Only issued for SUCCESS payments — anything else is a 400 by design. */
export async function getReceipt(id) {
  try {
    const { data } = await api.get(`/api/payments/${id}/receipt`)
    return data
  } catch (error) {
    const apiError = toApiError(error, 'Could not load that receipt.')
    if (apiError.status === 400) {
      throw new ApiError('A receipt is only issued once a payment has succeeded.', {
        status: 400,
        code: 'RECEIPT_UNAVAILABLE',
        raw: apiError.raw,
      })
    }
    throw apiError
  }
}

/** The set of bill months this user has already settled. */
export function settledMonths(history) {
  return new Set(
    (history || []).filter((p) => p.status === 'SUCCESS').map((p) => p.billMonth),
  )
}
