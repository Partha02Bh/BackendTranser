package com.mts.domain.exception;

/**
 * Exception thrown when account has insufficient balance for transaction.
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
