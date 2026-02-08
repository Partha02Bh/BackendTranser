package com.mts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mts.domain.dto.LoginRequest;
import com.mts.domain.entity.Account;
import com.mts.domain.enums.AccountStatus;
import com.mts.repository.AccountRepository;
import com.mts.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AccountRepository accountRepository;

        @MockBean
        private JwtService jwtService;

        @MockBean
        private UserDetailsService userDetailsService;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Test
        void testLoginSuccess() throws Exception {
                // Prepare test account
                Account account = Account.builder()
                                .id(1L)
                                .username("user")
                                .holderName("Test User")
                                .balance(new BigDecimal("100.00"))
                                .status(AccountStatus.ACTIVE)
                                .build();

                // Mock UserDetailsService for authentication
                UserDetails userDetails = User.builder()
                                .username("user")
                                .password(passwordEncoder.encode("user123"))
                                .roles("USER")
                                .build();

                when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
                when(accountRepository.findByUsername("user")).thenReturn(Optional.of(account));
                when(jwtService.generateToken(any(UserDetails.class))).thenReturn("test-jwt-token");

                LoginRequest loginRequest = new LoginRequest("user", "user123", "USER");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username").value("user"))
                                .andExpect(jsonPath("$.message").value("Login successful"))
                                .andExpect(jsonPath("$.token").value("test-jwt-token"));
        }

        @Test
        void testLoginInvalidCredentials() throws Exception {
                // Mock UserDetailsService but with wrong password
                UserDetails userDetails = User.builder()
                                .username("user")
                                .password(passwordEncoder.encode("correctpassword"))
                                .roles("USER")
                                .build();

                when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);

                LoginRequest loginRequest = new LoginRequest("user", "wrongpassword", "USER");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message").value("Invalid credentials"));
        }
}
