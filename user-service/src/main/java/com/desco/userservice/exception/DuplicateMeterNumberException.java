package com.desco.userservice.exception;

public class DuplicateMeterNumberException extends RuntimeException {
    public DuplicateMeterNumberException(String message) {
        super(message);
    }
}
