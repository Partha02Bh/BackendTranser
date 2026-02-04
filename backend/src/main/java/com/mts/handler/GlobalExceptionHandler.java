package com.mts.handler;

import com.mts.domain.dto.ErrorResponse;
import com.mts.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Central place to handle all exceptions thrown by controllers.
 * Maps each exception type to an appropriate HTTP status.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        // 404 - account doesn't exist
        @ExceptionHandler(AccountNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(AccountNotFoundException ex, HttpServletRequest req) {
                log.error("Account not found: {}", ex.getMessage());
                return buildError(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
        }

        // 403 - account exists but is frozen/suspended
        @ExceptionHandler(AccountNotActiveException.class)
        public ResponseEntity<ErrorResponse> handleInactive(AccountNotActiveException ex, HttpServletRequest req) {
                log.error("Account inactive: {}", ex.getMessage());
                return buildError(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), req);
        }

        // 400 - not enough money
        @ExceptionHandler(InsufficientBalanceException.class)
        public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientBalanceException ex,
                        HttpServletRequest req) {
                log.error("Insufficient balance: {}", ex.getMessage());
                return buildError(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req);
        }

        // 409 - trying to process the same transfer twice
        @ExceptionHandler(DuplicateTransferException.class)
        public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateTransferException ex, HttpServletRequest req) {
                log.error("Duplicate transfer attempt: {}", ex.getMessage());
                return buildError(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req);
        }

        // 422 - request body failed validation
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                        HttpServletRequest req) {
                log.error("Validation failed: {}", ex.getMessage());

                List<String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                                .toList();

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                                .error("Validation Failed")
                                .message("Request validation failed")
                                .path(req.getRequestURI())
                                .timestamp(LocalDateTime.now())
                                .details(fieldErrors)
                                .build();

                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
        }

        // 400 - generic bad input
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleBadArgument(IllegalArgumentException ex, HttpServletRequest req) {
                log.error("Bad argument: {}", ex.getMessage());
                return buildError(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req);
        }

        // 403 - user doesn't have permission (from @PreAuthorize)
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
                log.error("Access denied: {}", ex.getMessage());
                return buildError(HttpStatus.FORBIDDEN, "Forbidden", "You are not authorized to perform this action",
                                req);
        }

        // 500 - something unexpected broke
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
                log.error("Unexpected error: ", ex); // full stack trace for debugging
                return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                                "An unexpected error occurred", req);
        }

        // helper to reduce boilerplate
        private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String error, String message,
                        HttpServletRequest req) {
                ErrorResponse body = ErrorResponse.builder()
                                .status(status.value())
                                .error(error)
                                .message(message)
                                .path(req.getRequestURI())
                                .timestamp(LocalDateTime.now())
                                .build();
                return ResponseEntity.status(status).body(body);
        }
}
