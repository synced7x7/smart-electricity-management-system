package com.desco.userservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), "Not Found");
    }

    @ExceptionHandler(DuplicateMeterNumberException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateMeter(DuplicateMeterNumberException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), "Duplicate Meter Number");
    }

 
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "No endpoint found for this path", "Not Found");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, message, "Validation Error");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Invalid value for '" + ex.getName() + "': " + ex.getValue(), "Validation Error");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), "Validation Error");
    }

 
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String unsupported = unsupportedEnumValue(ex);
        if (unsupported != null) {
            return build(HttpStatus.BAD_REQUEST, unsupported, "Validation Error");
        }
        log.warn("Rejected by a database constraint: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.BAD_REQUEST,
                "Request violates a database constraint - check that every referenced id exists",
                "Constraint Violation");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(Exception ex) {
        String unsupported = unsupportedEnumValue(ex);
        if (unsupported != null) {
            return build(HttpStatus.BAD_REQUEST, unsupported, "Validation Error");
        }
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "Internal Server Error");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String error) {
        return new ResponseEntity<>(
                new ErrorResponse(status.value(), message, error, LocalDateTime.now()), status);
    }
    private static final Pattern INVALID_ENUM =
            Pattern.compile("invalid input value for enum (\\w+): \"([^\"]*)\"");

   
    private static String unsupportedEnumValue(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t.getMessage() != null) {
                Matcher m = INVALID_ENUM.matcher(t.getMessage());
                if (m.find()) {
                    return "'" + m.group(2) + "' is not a value the database accepts for "
                            + m.group(1) + " yet. The database schema is behind the application — "
                            + "apply db/02_area_nationwide.sql, then retry.";
                }
            }
            if (t.getCause() == t) break;
        }
        return null;
    }
}
