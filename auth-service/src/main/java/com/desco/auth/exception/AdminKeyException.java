package com.desco.auth.exception;

/*
  A registration supplied an administrator key that was wrong, 
  or that the server is not configured to accept.
 */
public class AdminKeyException extends RuntimeException {
    public AdminKeyException(String message) {
        super(message);
    }
}
