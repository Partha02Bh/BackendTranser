package com.mts.controller;

import com.mts.domain.dto.AccountResponse;
import com.mts.domain.dto.BalanceResponse;
import com.mts.domain.entity.TransactionLog;
import com.mts.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Account endpoints - view account info, balances, and transaction history
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class AccountController {

    private final AccountService svc;

    // GET /api/v1/accounts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long id) {
        log.debug("Account details requested for #{}", id);
        return ResponseEntity.ok(svc.getAccount(id));
    }

    // GET /api/v1/accounts/{id}/balance
    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long id) {
        log.debug("Balance check for #{}", id);
        return ResponseEntity.ok(svc.getBalance(id));
    }

    // GET /api/v1/accounts/{id}/transactions
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionLog>> getTransactions(@PathVariable Long id) {
        log.debug("Txn history requested for #{}", id);
        List<TransactionLog> txns = svc.getTransactions(id);
        return ResponseEntity.ok(txns);
    }
}
