package com.desco.auth.exception;

/**
 * A registration supplied an administrator key that was wrong, or that the
 * server is not configured to accept.
 *
 * Deliberately distinct from AuthException (401): the caller's credentials are
 * not in question, they are asking for a privilege they may not have. That maps
 * to 403.
 */
public class AdminKeyException extends RuntimeException {
    public AdminKeyException(String message) {
        super(message);
    }
}
