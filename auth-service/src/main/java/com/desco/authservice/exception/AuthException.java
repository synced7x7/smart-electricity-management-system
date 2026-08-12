package com.desco.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/*
 business-level auth failures duplicate email, bad credentials, account inactive, etc.
 */
@Getter
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

}
