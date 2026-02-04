package com.mts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import com.mts.domain.enums.TransactionStatus;
import com.mts.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferService transferService;

    @Test
    @WithMockUser(roles = "USER")
    void testTransfer_Success() throws Exception {
        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("10.00"))
                .idempotencyKey("uuid-key")
                .build();

        TransferResponse res = TransferResponse.builder()
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("10.00"))
                .build();

        when(transferService.transfer(any(TransferRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/transfers")
                .with(csrf()) // needed for non-GET tests unless CSRF disabled
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testTransfer_ForbiddenForAdmin() throws Exception {
        TransferRequest req = TransferRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("10.00"))
                .idempotencyKey("uuid-key")
                .build();

        mockMvc.perform(post("/api/v1/transfers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testGetAllTransactions_Admin() throws Exception {
        when(transferService.getAllTransactions()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/transfers/all"))
                .andExpect(status().isOk());
    }
}
