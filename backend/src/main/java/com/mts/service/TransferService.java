package com.mts.service;

import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import com.mts.domain.dto.TransactionHistoryResponse;
import com.mts.domain.entity.Account;
import com.mts.domain.entity.TransactionLog;
import com.mts.domain.enums.TransactionStatus;
import com.mts.domain.exception.*;
import com.mts.repository.AccountRepository;
import com.mts.repository.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/*
 * Handles all money transfer operations between accounts.
 * This is where the core business logic lives.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

        private final AccountRepository accountRepo;
        private final TransactionLogRepository txnRepo;
        private final NotificationService notificationService;

        // Main transfer method - validates and executes the transfer
        @Transactional
        public TransferResponse transfer(TransferRequest req) {
                log.info("New transfer request: from={} to={} amount={}",
                                req.getFromAccountId(), req.getToAccountId(), req.getAmount());

                // quick check for duplicate requests first
                checkForDuplicate(req.getIdempotencyKey());

                // run all validations
                Account src = fetchAndValidateSource(req);
                Account dest = fetchAndValidateDestination(req);

                // make sure there's enough money
                ensureSufficientFunds(src, req.getAmount());

                // all good - do the transfer
                TransactionLog txn = performTransfer(src, dest, req);

                // Send async notification (non-blocking, runs in background)
                notificationService.notifyTransferSuccess(
                                txn.getId(),
                                req.getFromAccountId(),
                                req.getToAccountId(),
                                req.getAmount());

                return buildResponse(txn);
        }

        private void checkForDuplicate(String idempotencyKey) {
                if (txnRepo.findByIdempotencyKey(idempotencyKey).isPresent()) {
                        throw new DuplicateTransferException("Transfer already processed with key: " + idempotencyKey);
                }
        }

        private Account fetchAndValidateSource(TransferRequest req) {
                // can't transfer to yourself
                if (req.getFromAccountId().equals(req.getToAccountId())) {
                        throw new IllegalArgumentException("Source and destination accounts cannot be the same");
                }

                Account src = accountRepo.findById(req.getFromAccountId())
                                .orElseThrow(() -> new AccountNotFoundException(req.getFromAccountId()));

                if (!src.isActive()) {
                        throw new AccountNotActiveException(req.getFromAccountId());
                }

                // sanity check on amount (DTO validation should catch this but just in case)
                if (req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Transfer amount must be positive");
                }

                return src;
        }

        private Account fetchAndValidateDestination(TransferRequest req) {
                Account dest = accountRepo.findById(req.getToAccountId())
                                .orElseThrow(() -> new AccountNotFoundException(req.getToAccountId()));

                if (!dest.isActive()) {
                        throw new AccountNotActiveException(req.getToAccountId());
                }
                return dest;
        }

        private void ensureSufficientFunds(Account src, BigDecimal amount) {
                BigDecimal available = src.getBalance();
                if (available.compareTo(amount) < 0) {
                        String msg = String.format("Insufficient balance. Available: %s, Requested: %s", available,
                                        amount);
                        throw new InsufficientBalanceException(msg);
                }
        }

        private TransactionLog performTransfer(Account src, Account dest, TransferRequest req) {
                // debit first, then credit (order matters for consistency)
                src.debit(req.getAmount());
                dest.credit(req.getAmount());

                accountRepo.save(src);
                accountRepo.save(dest);

                // create the log entry
                TransactionLog txn = TransactionLog.builder()
                                .fromAccountId(req.getFromAccountId())
                                .toAccountId(req.getToAccountId())
                                .amount(req.getAmount())
                                .status(TransactionStatus.SUCCESS)
                                .idempotencyKey(req.getIdempotencyKey())
                                .build();

                return txnRepo.save(txn);
        }

        private TransferResponse buildResponse(TransactionLog txn) {
                return TransferResponse.builder()
                                .transactionId(txn.getId())
                                .fromAccountId(txn.getFromAccountId())
                                .toAccountId(txn.getToAccountId())
                                .amount(txn.getAmount())
                                .status(txn.getStatus())
                                .message("Transfer completed successfully")
                                .timestamp(txn.getCreatedOn())
                                .build();
        }

        // Admin endpoint - gets everything
        public List<TransactionHistoryResponse> getAllTransactions() {
                List<TransactionHistoryResponse> txns = txnRepo.findAll().stream()
                                .map(this::toHistoryResponse)
                                .toList();
                log.info("Returning all transactions: {}", txns);
                return txns;
        }

        // User endpoint - only their transactions
        public List<TransactionHistoryResponse> getTransactionsByUsername(String username) {
                Account acct = accountRepo.findByUsername(username)
                                .orElseThrow(() -> new AccountNotFoundException(
                                                "Account not found for user: " + username));

                Long id = acct.getId();
                List<TransactionHistoryResponse> txns = txnRepo.findByFromAccountIdOrToAccountId(id, id).stream()
                                .map(this::toHistoryResponse)
                                .toList();
                log.info("Returning transactions for user {}: {}", username, txns);
                return txns;
        }

        private TransactionHistoryResponse toHistoryResponse(TransactionLog txn) {
                // lookup usernames and holder names for display
                Account fromAccount = accountRepo.findById(txn.getFromAccountId())
                                .orElse(null);
                Account toAccount = accountRepo.findById(txn.getToAccountId())
                                .orElse(null);

                String fromUser = (fromAccount != null) ? fromAccount.getUsername() : "Unknown";
                String fromHolder = (fromAccount != null) ? fromAccount.getHolderName() : "Unknown";

                String toUser = (toAccount != null) ? toAccount.getUsername() : "Unknown";
                String toHolder = (toAccount != null) ? toAccount.getHolderName() : "Unknown";

                return TransactionHistoryResponse.builder()
                                .transactionId(txn.getId())
                                .fromAccountId(txn.getFromAccountId())
                                .fromAccountUsername(fromUser)
                                .fromAccountHolderName(fromHolder)
                                .toAccountId(txn.getToAccountId())
                                .toAccountUsername(toUser)
                                .toAccountHolderName(toHolder)
                                .amount(txn.getAmount())
                                .status(txn.getStatus())
                                .timestamp(txn.getCreatedOn())
                                .build();
        }
}
