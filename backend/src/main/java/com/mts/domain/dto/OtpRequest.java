package com.mts.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OTP verification request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequest {
    private String username;
    private String otp;
}
