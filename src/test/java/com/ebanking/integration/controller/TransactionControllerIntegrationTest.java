package com.ebanking.integration.controller;

import com.ebanking.domain.Account;
import com.ebanking.domain.Customer;
import com.ebanking.domain.Transaction;
import com.ebanking.dto.TransactionRequestDto;
import com.ebanking.repository.AccountRepository;
import com.ebanking.repository.CustomerRepository;
import com.ebanking.repository.TransactionRepository;
import com.ebanking.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("TransactionController Integration Tests")
class TransactionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Customer testCustomer;
    private Account testAccount;
    private String authToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create test customer
        testCustomer = Customer.builder()
                .id("test-customer")
                .password(passwordEncoder.encode("password123"))
                .build();
        testCustomer = customerRepository.save(testCustomer);

        // Create test account
        testAccount = Account.builder()
                .iban("TEST123456")
                .currency("MYR")
                .customer(testCustomer)
                .build();
        testAccount = accountRepository.save(testAccount);

        // Generate JWT token for authentication
        authToken = jwtService.generateToken(testCustomer.getId());
    }

    @Test
    @DisplayName("Should create transaction successfully with valid request and authentication")
    void shouldCreateTransactionSuccessfully() throws Exception {
        // Given
        TransactionRequestDto request = new TransactionRequestDto();
        request.setAccountIban("TEST123456");
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setDescription("Test transaction");
        request.setValueDate(LocalDate.now());

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountIban").value("TEST123456"))
                .andExpect(jsonPath("$.currency").value("MYR"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.description").value("Test transaction"));

        // Verify transaction was saved
        assertThat(transactionRepository.findAll()).hasSize(1);
        Transaction savedTransaction = transactionRepository.findAll().get(0);
        assertThat(savedTransaction.getAccount().getIban()).isEqualTo("TEST123456");
        assertThat(savedTransaction.getAmount()).isEqualTo(BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("Should return 401 when creating transaction without authentication")
    void shouldReturn401WhenCreatingTransactionWithoutAuthentication() throws Exception {
        // Given
        TransactionRequestDto request = new TransactionRequestDto();
        request.setAccountIban("TEST123456");
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setDescription("Test transaction");
        request.setValueDate(LocalDate.now());

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 when account not found")
    void shouldReturn400WhenAccountNotFound() throws Exception {
        // Given
        TransactionRequestDto request = new TransactionRequestDto();
        request.setAccountIban("NONEXISTENT");
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setDescription("Test transaction");
        request.setValueDate(LocalDate.now());

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get transactions by account")
    void shouldGetTransactionsByAccount() throws Exception {
        // Given
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .account(testAccount)
                .amount(BigDecimal.valueOf(100.00))
                .currency("MYR")
                .valueDate(LocalDate.now())
                .description("Test transaction")
                .build();
        transactionRepository.save(transaction);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/account/TEST123456")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions[0].id").value(transaction.getId()))
                .andExpect(jsonPath("$.transactions[0].accountIban").value("TEST123456"))
                .andExpect(jsonPath("$.transactions[0].currency").value("MYR"))
                .andExpect(jsonPath("$.transactions[0].amount").value(100.00))
                .andExpect(jsonPath("$.totalCredit").value(100.00))
                .andExpect(jsonPath("$.totalDebit").value(0));
    }

    @Test
    @DisplayName("Should get transactions by customer")
    void shouldGetTransactionsByCustomer() throws Exception {
        // Given
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .account(testAccount)
                .amount(BigDecimal.valueOf(100.00))
                .currency("MYR")
                .valueDate(LocalDate.now())
                .description("Test transaction")
                .build();
        transactionRepository.save(transaction);
        transactionRepository.save(transaction);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/customer/test-customer")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(transaction.getId()))
                .andExpect(jsonPath("$[0].accountIban").value("TEST123456"))
                .andExpect(jsonPath("$[0].currency").value("MYR"))
                .andExpect(jsonPath("$[0].amount").value(100.00));
    }

    @Test
    @DisplayName("Should return empty list when no transactions found")
    void shouldReturnEmptyListWhenNoTransactionsFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/transactions/account/TEST123456")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions").isEmpty())
                .andExpect(jsonPath("$.totalCredit").value(0))
                .andExpect(jsonPath("$.totalDebit").value(0));
    }

    @Test
    @DisplayName("Should return 401 when getting transactions without authentication")
    void shouldReturn401WhenGettingTransactionsWithoutAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/transactions/account/TEST123456"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle invalid request body")
    void shouldHandleInvalidRequestBody() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle missing required fields")
    void shouldHandleMissingRequiredFields() throws Exception {
        // Given
        TransactionRequestDto invalidRequest = new TransactionRequestDto();
        invalidRequest.setAccountIban("TEST123456");
        // Missing amount, description, valueDate

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle negative amount")
    void shouldHandleNegativeAmount() throws Exception {
        // Given
        TransactionRequestDto request = new TransactionRequestDto();
        request.setAccountIban("TEST123456");
        request.setAmount(BigDecimal.valueOf(-100.00));
        request.setDescription("Test transaction");
        request.setValueDate(LocalDate.now());

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Should allow negative amounts for debits
    }

    @Test
    @DisplayName("Should handle multiple transactions for same account")
    void shouldHandleMultipleTransactionsForSameAccount() throws Exception {
        // Given
        TransactionRequestDto request1 = new TransactionRequestDto();
        request1.setAccountIban("TEST123456");
        request1.setAmount(BigDecimal.valueOf(100.00));
        request1.setDescription("First transaction");
        request1.setValueDate(LocalDate.now());

        TransactionRequestDto request2 = new TransactionRequestDto();
        request2.setAccountIban("TEST123456");
        request2.setAmount(BigDecimal.valueOf(200.00));
        request2.setDescription("Second transaction");
        request2.setValueDate(LocalDate.now());

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        // Verify both transactions were saved
        assertThat(transactionRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Should use account currency for transaction")
    void shouldUseAccountCurrencyForTransaction() throws Exception {
        // Given
        TransactionRequestDto request = new TransactionRequestDto();
        request.setAccountIban("TEST123456");
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setDescription("Test transaction");
        request.setValueDate(LocalDate.now());

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("MYR")); // Should use account currency

        // Verify transaction currency matches account currency
        Transaction savedTransaction = transactionRepository.findAll().get(0);
        assertThat(savedTransaction.getCurrency()).isEqualTo("MYR");
    }
}