package com.mts.service;

import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import com.mts.domain.entity.Account;
import com.mts.domain.entity.TransactionLog;
import com.mts.domain.enums.AccountStatus;
import com.mts.domain.enums.TransactionStatus;
import com.mts.domain.exception.AccountNotActiveException;
import com.mts.domain.exception.AccountNotFoundException;
import com.mts.domain.exception.DuplicateTransferException;
import com.mts.domain.exception.InsufficientBalanceException;
import com.mts.repository.AccountRepository;
import com.mts.repository.TransactionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionLogRepository txnRepo;

    @InjectMocks
    private TransferService transferService;

    private Account alice;
    private Account bob;
    private TransferRequest request;

    @BeforeEach
    void setUp() {
        alice = Account.builder()
                .id(1L)
                .username("alice")
                .balance(new BigDecimal("100.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        bob = Account.builder()
                .id(2L)
                .username("bob")
                .balance(new BigDecimal("50.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        request = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("20.00"))
                .idempotencyKey("test-key")
                .build();
    }

    @Test
    @DisplayName("Should execute valid transfer successfully")
    void testSuccessfulTransfer() {
        when(txnRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(txnRepo.save(any(TransactionLog.class))).thenAnswer(i -> {
            TransactionLog t = i.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCreatedOn(LocalDateTime.now());
            return t;
        });

        TransferResponse response = transferService.transfer(request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(alice.getBalance()).isEqualByComparingTo("80.00");
        assertThat(bob.getBalance()).isEqualByComparingTo("70.00");

        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("Should fail if sufficient funds are missing")
    void testInsufficientFunds() {
        request.setAmount(new BigDecimal("200.00")); // more than Alice has

        when(txnRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(bob));

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should fail if account not found")
    void testAccountNotFound() {
        when(txnRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("Should fail if source account inactive")
    void testInactiveSource() {
        alice.setStatus(AccountStatus.LOCKED);

        when(txnRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(alice));

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(AccountNotActiveException.class);
    }

    @Test
    @DisplayName("Should fail for self transfer")
    void testSelfTransfer() {
        request.setToAccountId(1L);

        when(txnRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should detect duplicate idempotency key")
    void testDuplicateTransfer() {
        when(txnRepo.findByIdempotencyKey("test-key"))
                .thenReturn(Optional.of(new TransactionLog()));

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(DuplicateTransferException.class);

        verify(accountRepository, never()).findById(any());
    }
}
