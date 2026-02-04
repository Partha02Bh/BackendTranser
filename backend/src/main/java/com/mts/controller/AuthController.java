package com.mts.controller;

import com.mts.domain.dto.LoginResponse;
import com.mts.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Handles login - Spring Security does the actual auth, 
// we just return account info for the frontend
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AccountRepository accountRepo;

    @GetMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestParam String username) {
        log.info("Login attempt for: {}", username);

        var maybeAccount = accountRepo.findByUsername(username);

        if (maybeAccount.isEmpty()) {
            log.warn("No account found for username: {}", username);
            LoginResponse notFound = LoginResponse.builder()
                    .message("Account not found for username: " + username)
                    .build();
            return ResponseEntity.badRequest().body(notFound);
        }

        var account = maybeAccount.get();

        LoginResponse response = LoginResponse.builder()
                .accountId(account.getId())
                .username(account.getUsername())
                .holderName(account.getHolderName())
                .balance(account.getBalance())
                .status(account.getStatus())
                .message("Login successful")
                .build();

        return ResponseEntity.ok(response);
    }
}
