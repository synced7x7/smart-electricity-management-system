package com.desco.auth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                "Authentication Error",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AdminKeyException.class)
    public ResponseEntity<ErrorResponse> handleAdminKey(AdminKeyException ex, WebRequest request) {
        // 403, not 401: the caller is authenticated enough to create an account,
        // they simply may not claim the privilege they asked for.
        return new ResponseEntity<>(new ErrorResponse(
                HttpStatus.FORBIDDEN.value(), ex.getMessage(), "Forbidden", LocalDateTime.now()
        ), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication failed",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((s1, s2) -> s1 + ", " + s2)
                .orElse("Validation failed");

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                "Validation Error",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                "Validation Error",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * A value that the Java enum accepts but the database has never heard of.
     *
     * Postgres answers with SQLSTATE 22P02, "invalid input value for enum
     * area_name: ...", which otherwise falls through to the catch-all below and
     * reaches the caller as an opaque 500. That is exactly what happens when the
     * application ships ahead of its migration — the Java enum knows all 64
     * districts, the database still only knows the 8 original zones.
     *
     * Turning it into a 400 that names the offending value and the migration is
     * the difference between "the server is broken" and "run this script".
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(DataAccessException ex, WebRequest request) {
        String unsupported = unsupportedEnumValue(ex);
        if (unsupported != null) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    unsupported,
                    "Validation Error",
                    LocalDateTime.now()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        log.error("Data access failure", ex);
        return new ResponseEntity<>(new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                "Internal Server Error",
                LocalDateTime.now()
        ), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static final Pattern INVALID_ENUM =
            Pattern.compile("invalid input value for enum (\\w+): \"([^\"]*)\"");

    /** Non-null when the cause chain is Postgres rejecting an unknown enum label. */
    static String unsupportedEnumValue(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t.getMessage() == null) continue;
            Matcher m = INVALID_ENUM.matcher(t.getMessage());
            if (m.find()) {
                return "'" + m.group(2) + "' is not a value the database accepts for "
                        + m.group(1) + " yet. The database schema is behind the application — "
                        + "apply db/02_area_nationwide.sql, then retry.";
            }
            if (t.getCause() == t) break;
        }
        return null;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        String unsupported = unsupportedEnumValue(ex);
        if (unsupported != null) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(), unsupported, "Validation Error", LocalDateTime.now()
            ), HttpStatus.BAD_REQUEST);
        }
        // Never echo ex.getMessage() back to the caller: for a SQL failure that
        // is the generated statement plus constraint names (backend log #33).
        log.error("Unhandled exception", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                "Internal Server Error",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
