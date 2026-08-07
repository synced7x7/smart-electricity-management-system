package com.desco.authservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Access denied at {}", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(build(403, "Forbidden",
                        "Admin access required.", req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(500, "Internal Server Error",
                        "An unexpected error occurred.", req.getRequestURI()));
    }

    private ErrorResponse build(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .status(status).error(error).message(message).path(path).build();
    }
}
