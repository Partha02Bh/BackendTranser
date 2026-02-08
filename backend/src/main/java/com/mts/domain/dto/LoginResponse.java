package com.mts.domain.dto;

import com.mts.domain.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for login response containing account details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long accountId;
    private String username;
    private String holderName;
    private BigDecimal balance;
    private AccountStatus status;
    private String message;
    private String token;
    private String role;
}
