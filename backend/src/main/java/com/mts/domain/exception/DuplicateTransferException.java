package com.mts.domain.exception;

/**
 * Exception thrown when a duplicate transfer is attempted (same idempotency
 * key).
 */
public class DuplicateTransferException extends RuntimeException {

    public DuplicateTransferException(String message) {
        super(message);
    }

    public DuplicateTransferException(String idempotencyKey, boolean isKey) {
        super("Duplicate transfer with idempotency key: " + idempotencyKey);
    }
}
