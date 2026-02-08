package com.mts.service;

import com.mts.domain.dto.AccountResponse;
import com.mts.domain.dto.BalanceResponse;
import com.mts.domain.entity.Account;
import com.mts.domain.entity.TransactionLog;
import com.mts.domain.exception.AccountNotFoundException;
import com.mts.repository.AccountRepository;
import com.mts.repository.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Service layer for account operations - mostly read-only stuff
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accounts;
    private final TransactionLogRepository transactions;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Transactional
    public void createAccount(com.mts.domain.dto.RegisterRequest request) {
        log.info("Creating new account for user: {}", request.getUsername());

        // Check if username exists
        if (accounts.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Create new account
        Account account = Account.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .holderName(request.getHolderName())
                // Default to 0 if null, otherwise use requested balance (for demo purposes)
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : java.math.BigDecimal.ZERO)
                .status(com.mts.domain.enums.AccountStatus.ACTIVE)
                .role(com.mts.domain.enums.Role.USER)
                .build();

        accounts.save(account);
        log.info("Account created successfully with ID: {}", account.getId());
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long id) {
        log.debug("Looking up account #{}", id);

        Account acct = findAccountOrThrow(id);

        return AccountResponse.builder()
                .id(acct.getId())
                .holderName(acct.getHolderName())
                .balance(acct.getBalance())
                .status(acct.getStatus())
                .lastUpdated(acct.getLastUpdated())
                .build();
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long id) {
        log.debug("Checking balance for account #{}", id);

        Account acct = findAccountOrThrow(id);

        // just return the essentials
        return new BalanceResponse(acct.getId(), acct.getBalance());
    }

    @Transactional(readOnly = true)
    public List<com.mts.domain.dto.TransactionHistoryResponse> getTransactions(Long id) {
        log.debug("Fetching txn history for account #{}", id);

        // make sure the account actually exists first
        if (!accounts.existsById(id)) {
            throw new AccountNotFoundException(id);
        }

        // grab all transactions where this account is involved
        List<TransactionLog> logs = transactions.findByFromAccountIdOrToAccountId(id, id);

        return logs.stream()
                .map(log -> {
                    Account fromAccount = accounts.findById(log.getFromAccountId()).orElse(null);
                    Account toAccount = accounts.findById(log.getToAccountId()).orElse(null);

                    String fromUser = (fromAccount != null) ? fromAccount.getUsername() : "Unknown";
                    String fromHolder = (fromAccount != null) ? fromAccount.getHolderName() : "Unknown";
                    String toUser = (toAccount != null) ? toAccount.getUsername() : "Unknown";
                    String toHolder = (toAccount != null) ? toAccount.getHolderName() : "Unknown";

                    return com.mts.domain.dto.TransactionHistoryResponse.builder()
                            .transactionId(log.getId())
                            .fromAccountId(log.getFromAccountId())
                            .fromAccountUsername(fromUser)
                            .fromAccountHolderName(fromHolder)
                            .toAccountId(log.getToAccountId())
                            .toAccountUsername(toUser)
                            .toAccountHolderName(toHolder)
                            .amount(log.getAmount())
                            .status(log.getStatus())
                            .timestamp(log.getCreatedOn())
                            .build();
                })
                .toList();
    }

    // helper to avoid repeating the same lookup pattern
    private Account findAccountOrThrow(Long id) {
        return accounts.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
