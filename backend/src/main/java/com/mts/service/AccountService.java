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
    public List<TransactionLog> getTransactions(Long id) {
        log.debug("Fetching txn history for account #{}", id);

        // make sure the account actually exists first
        if (!accounts.existsById(id)) {
            throw new AccountNotFoundException(id);
        }

        // grab all transactions where this account is involved
        return transactions.findByFromAccountIdOrToAccountId(id, id);
    }

    // helper to avoid repeating the same lookup pattern
    private Account findAccountOrThrow(Long id) {
        return accounts.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
