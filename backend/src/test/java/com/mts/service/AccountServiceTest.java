package com.mts.service;

import com.mts.domain.dto.AccountResponse;
import com.mts.domain.dto.BalanceResponse;
import com.mts.domain.entity.Account;
import com.mts.domain.entity.TransactionLog;
import com.mts.domain.enums.AccountStatus;
import com.mts.domain.exception.AccountNotFoundException;
import com.mts.repository.AccountRepository;
import com.mts.repository.TransactionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionLogRepository txnRepo;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .holderName("Test User")
                .balance(new BigDecimal("100.00"))
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void testGetAccount_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccount(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBalance()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void testGetAccount_NotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(99L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void testGetBalance_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BalanceResponse response = accountService.getBalance(1L);

        assertThat(response.getAccountId()).isEqualTo(1L);
        assertThat(response.getBalance()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void testGetTransactions_Success() {
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(txnRepo.findByFromAccountIdOrToAccountId(1L, 1L))
                .thenReturn(List.of(new TransactionLog(), new TransactionLog()));

        List<com.mts.domain.dto.TransactionHistoryResponse> txns = accountService.getTransactions(1L);

        assertThat(txns).hasSize(2);
    }
}
