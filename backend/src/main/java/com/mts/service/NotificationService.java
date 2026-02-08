package com.mts.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling async notifications and audit logging.
 * Methods run in background threads to avoid blocking main transfer operations.
 */
@Service
@Slf4j
public class NotificationService {

    /**
     * Send async notification after successful transfer.
     * Runs in background thread pool.
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> notifyTransferSuccess(
            UUID transactionId,
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount) {

        log.info("🔔 [ASYNC] Sending transfer success notification for transaction: {}", transactionId);
        log.info("   Transfer: Account {} → Account {} (Amount: ${})", fromAccountId, toAccountId, amount);

        try {
            // Simulate notification processing (email, SMS, push notification, etc.)
            Thread.sleep(1000);
            log.info("✅ [ASYNC] Notification sent successfully for transaction: {}", transactionId);
        } catch (InterruptedException e) {
            log.error("❌ [ASYNC] Notification failed for transaction: {}", transactionId, e);
            Thread.currentThread().interrupt();
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Async audit logging for compliance and monitoring.
     * Runs in background to avoid impacting response time.
     */
    @Async("taskExecutor")
    public void logTransactionAudit(
            UUID transactionId,
            String action,
            String username,
            String details) {

        log.info("📝 [ASYNC] Audit log - Transaction: {}, Action: {}, User: {}",
                transactionId, action, username);
        log.info("   Details: {}", details);

        try {
            // Simulate writing to external audit system
            Thread.sleep(500);
            log.info("✅ [ASYNC] Audit log persisted for transaction: {}", transactionId);
        } catch (InterruptedException e) {
            log.error("❌ [ASYNC] Audit logging failed for transaction: {}", transactionId, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Send async email notification (simulated).
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> sendEmailNotification(String recipientEmail, String subject, String body) {
        log.info("📧 [ASYNC] Sending email to: {} - Subject: {}", recipientEmail, subject);

        try {
            // Simulate email sending
            Thread.sleep(800);
            log.info("✅ [ASYNC] Email sent successfully to: {}", recipientEmail);
        } catch (InterruptedException e) {
            log.error("❌ [ASYNC] Email sending failed to: {}", recipientEmail, e);
            Thread.currentThread().interrupt();
        }

        return CompletableFuture.completedFuture(null);
    }
}
