package com.mts.domain.dto;

import com.mts.domain.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for transaction history response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponse {
    private UUID transactionId;
    private Long fromAccountId;
    private String fromAccountUsername;
    private Long toAccountId;
    private String toAccountUsername;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime timestamp;
}
