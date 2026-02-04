package com.mts.domain.exception;

/**
 * Exception thrown when an account is not active (locked or closed).
 */
public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(String message) {
        super(message);
    }

    public AccountNotActiveException(Long accountId) {
        super("Account is not active: " + accountId);
    }
}
