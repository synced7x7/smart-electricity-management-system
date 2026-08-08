package com.desco.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown for business-level auth failures:
 * duplicate email, bad credentials, account inactive, etc.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AuthException extends RuntimeException {

    private final HttpStatus status;

    public AuthException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public AuthException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
