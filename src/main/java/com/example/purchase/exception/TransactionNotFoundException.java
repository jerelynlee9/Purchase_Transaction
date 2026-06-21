package com.example.purchase.exception;

/**
 * Thrown when a requested transaction UUID cannot be located in the database.
 * Maps to an HTTP 404 Not Found status via the GlobalExceptionHandler.
 */
public class TransactionNotFoundException extends RuntimeException {
    
    public TransactionNotFoundException(String message) {
        super(message);
    }
}