package com.mts.controller;

import com.mts.domain.dto.TransactionHistoryResponse;
import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import com.mts.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Transfer endpoints - the heart of the money transfer system.
 * Users can send money, and view their transaction history.
 */
@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class TransferController {

    private final TransferService transferService;

    // POST /api/v1/transfers - only regular users can transfer (not admins)
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("Transfer: {} -> {} for ${}",
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        TransferResponse result = transferService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // GET /api/v1/transfers - returns transactions based on who's asking
    @GetMapping
    public ResponseEntity<List<TransactionHistoryResponse>> getTransactions(Authentication auth) {
        String user = auth.getName();

        // check if they're an admin
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        log.info("Fetching transactions for {} (admin={})", user, isAdmin);

        List<TransactionHistoryResponse> results;
        if (isAdmin) {
            results = transferService.getAllTransactions();
        } else {
            results = transferService.getTransactionsByUsername(user);
        }

        return ResponseEntity.ok(results);
    }

    // GET /api/v1/transfers/all - admin-only endpoint for all transactions
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransactionHistoryResponse>> getAllTransactions() {
        log.info("Admin: fetching all transactions");
        return ResponseEntity.ok(transferService.getAllTransactions());
    }
}
