package com.example.purchase.exception;

/**
 * Thrown when business rules for currency conversion are violated 
 * (e.g., no exchange rate exists within the required 6-month historical window).
 * Maps to an HTTP 400 Bad Request status via the GlobalExceptionHandler.
 */
public class CurrencyConversionException extends RuntimeException {
    
    public CurrencyConversionException(String message) {
        super(message);
    }
}