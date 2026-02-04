package com.mts.controller;

import com.mts.domain.entity.Account;
import com.mts.domain.enums.AccountStatus;
import com.mts.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountRepository accountRepository;

    @Test
    @WithMockUser
    void testLoginSuccess() throws Exception {
        Account account = Account.builder()
                .id(1L)
                .username("testmsg")
                .holderName("Test User")
                .balance(new BigDecimal("100.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findByUsername("testmsg")).thenReturn(Optional.of(account));

        mockMvc.perform(get("/api/v1/auth/login")
                .param("username", "testmsg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testmsg"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    @WithMockUser
    void testLoginNotFound() throws Exception {
        when(accountRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/auth/login")
                .param("username", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account not found for username: unknown"));
    }
}
