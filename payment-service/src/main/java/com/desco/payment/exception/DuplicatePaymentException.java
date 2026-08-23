package com.desco.payment.exception;

/**
 * Raised when a user tries to pay a bill month that is already settled.
 */
public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException(String message) {
        super(message);
    }
}
