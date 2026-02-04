package com.mts.domain.entity;

import com.mts.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Immutable record of a money transfer attempt
@Entity
@Table(name = "transaction_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLog {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id; // using UUID for globally unique txn IDs

    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;

    @Column(name = "to_account_id", nullable = false)
    private Long toAccountId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "failure_reason")
    private String failureReason; // only populated if status is FAILED

    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String idempotencyKey; // prevents duplicate transactions

    @CreationTimestamp
    @Column(name = "created_on")
    private LocalDateTime createdOn;
}
