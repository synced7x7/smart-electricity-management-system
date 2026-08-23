package com.desco.payment.enums;

/**
 * Mirrors the native PostgreSQL enum type {@code payment_status}.
 * These three labels are the ONLY values the database accepts.
 */
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED
}
