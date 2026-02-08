package com.mts.controller;

import com.mts.domain.dto.LoginRequest;
import com.mts.domain.dto.LoginResponse;
import com.mts.domain.dto.OtpRequest;
import com.mts.repository.AccountRepository;
import com.mts.security.JwtService;
import com.mts.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles OTP + JWT two-factor authentication.
 * Step 1: /login - validates credentials, sends OTP
 * Step 2: /verify-otp - validates OTP, returns JWT
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:4300" })
public class AuthController {

        private final AccountRepository accountRepo;
        private final AuthenticationManager authManager;
        private final UserDetailsService userDetailsService;
        private final JwtService jwtService;
        private final OtpService otpService;
        private final com.mts.service.AccountService accountService;

        @PostMapping("/register")
        public ResponseEntity<?> register(@RequestBody com.mts.domain.dto.RegisterRequest request) {
                log.info("Registration attempt for: {}", request.getUsername());
                try {
                        accountService.createAccount(request);
                        return ResponseEntity.status(201).body(Map.of("message", "User registered successfully"));
                } catch (RuntimeException e) {
                        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
                }
        }

        /**
         * Step 1: Validate credentials and send OTP.
         */
        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginRequest request) {
                log.info("Login attempt for: {}", request.getUsername());

                try {
                        // Authenticate credentials
                        authManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getUsername(),
                                                        request.getPassword()));
                } catch (BadCredentialsException e) {
                        log.warn("Invalid credentials for username: {}", request.getUsername());
                        return ResponseEntity.status(401).body(
                                        Map.of("message", "Invalid credentials"));
                }

                // Check if account exists and validate role
                var accountOpt = accountRepo.findByUsername(request.getUsername());
                if (accountOpt.isPresent()) {
                        var account = accountOpt.get();
                        String requestedRole = request.getRole(); // "USER" or "OWNER"
                        String actualRole = account.getRole().name(); // "USER" or "ADMIN"

                        // If user tries to login as ADMIN/OWNER but is actually a USER
                        if ("OWNER".equalsIgnoreCase(requestedRole) && "USER".equalsIgnoreCase(actualRole)) {
                                log.warn("Role mismatch for user: {}. Requested: {}, Actual: {}", request.getUsername(),
                                                requestedRole, actualRole);
                                return ResponseEntity.status(403).body(
                                                Map.of("message",
                                                                "You are not an admin. Please login through the User form."));
                        }

                        // If admin tries to login as USER (Reverse restriction)
                        if ("USER".equalsIgnoreCase(requestedRole) && ("OWNER".equalsIgnoreCase(actualRole)
                                        || "ADMIN".equalsIgnoreCase(actualRole))) {
                                log.warn("Role mismatch for user: {}. Requested: {}, Actual: {}", request.getUsername(),
                                                requestedRole, actualRole);
                                return ResponseEntity.status(403).body(
                                                Map.of("message",
                                                                "You are an admin. Please login through the Admin form."));
                        }
                }

                // Generate and store OTP (printed to console for demo)
                String otp = otpService.generateOtp(request.getUsername());

                log.info("OTP generated for: {} - check console for OTP", request.getUsername());
                return ResponseEntity.ok(Map.of(
                                "message", "OTP sent successfully",
                                "username", request.getUsername(),
                                "otp", otp // Include OTP in response for demo (remove in production!)
                ));
        }

        /**
         * Step 2: Validate OTP and return JWT token.
         */
        @PostMapping("/verify-otp")
        public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest request) {
                log.info("OTP verification for: {}", request.getUsername());

                // Validate OTP
                if (!otpService.validateOtp(request.getUsername(), request.getOtp())) {
                        log.warn("Invalid or expired OTP for: {}", request.getUsername());
                        return ResponseEntity.status(401).body(
                                        Map.of("message", "Invalid or expired OTP"));
                }

                // OTP valid - generate JWT
                UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
                String token = jwtService.generateToken(userDetails);

                // Get account details
                var maybeAccount = accountRepo.findByUsername(request.getUsername());

                if (maybeAccount.isEmpty()) {
                        log.warn("No account found for username: {}", request.getUsername());
                        return ResponseEntity.badRequest().body(
                                        Map.of("message", "Account not found for username: " + request.getUsername()));
                }

                var account = maybeAccount.get();

                LoginResponse response = LoginResponse.builder()
                                .accountId(account.getId())
                                .username(account.getUsername())
                                .holderName(account.getHolderName())
                                .balance(account.getBalance())
                                .status(account.getStatus())
                                .role(account.getRole().name())
                                .message("Login successful")
                                .token(token)
                                .build();

                log.info("Login successful for: {}", request.getUsername());
                return ResponseEntity.ok(response);
        }
}
