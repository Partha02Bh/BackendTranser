package com.mts.domain.dto;

import com.mts.domain.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for account response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private String holderName;
    private BigDecimal balance;
    private AccountStatus status;
    private LocalDateTime lastUpdated;
}
