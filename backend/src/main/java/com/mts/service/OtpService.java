package com.mts.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for OTP generation and validation.
 * Stores OTPs in-memory with 5-minute expiry.
 */
@Service
@Slf4j
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    /**
     * Generates a new OTP for the given username.
     * 
     * @param username the username to generate OTP for
     * @return the generated OTP
     */
    public String generateOtp(String username) {
        // Generate 6-digit OTP
        int otp = 100000 + random.nextInt(900000);
        String otpString = String.valueOf(otp);

        // Store with expiry time
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        otpStore.put(username, new OtpData(otpString, expiryTime));

        log.info("========================================");
        log.info("OTP for user '{}': {}", username, otpString);
        log.info("Valid until: {}", expiryTime);
        log.info("========================================");

        return otpString;
    }

    /**
     * Validates the OTP for the given username.
     * 
     * @param username the username
     * @param otp      the OTP to validate
     * @return true if valid, false otherwise
     */
    public boolean validateOtp(String username, String otp) {
        OtpData otpData = otpStore.get(username);

        if (otpData == null) {
            log.warn("No OTP found for user: {}", username);
            return false;
        }

        if (LocalDateTime.now().isAfter(otpData.expiryTime())) {
            log.warn("OTP expired for user: {}", username);
            otpStore.remove(username);
            return false;
        }

        if (!otpData.otp().equals(otp)) {
            log.warn("Invalid OTP for user: {}", username);
            return false;
        }

        // OTP is valid - remove it (one-time use)
        otpStore.remove(username);
        log.info("OTP validated successfully for user: {}", username);
        return true;
    }

    /**
     * Checks if a pending OTP exists for the user.
     */
    public boolean hasPendingOtp(String username) {
        OtpData otpData = otpStore.get(username);
        if (otpData == null)
            return false;
        if (LocalDateTime.now().isAfter(otpData.expiryTime())) {
            otpStore.remove(username);
            return false;
        }
        return true;
    }

    /**
     * Internal record to store OTP with expiry.
     */
    private record OtpData(String otp, LocalDateTime expiryTime) {
    }
}
