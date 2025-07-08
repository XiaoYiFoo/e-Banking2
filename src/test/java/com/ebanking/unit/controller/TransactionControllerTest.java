package com.ebanking.unit.controller;

import com.ebanking.controller.TransactionController;
import com.ebanking.domain.Account;
import com.ebanking.domain.Customer;
import com.ebanking.domain.Transaction;
import com.ebanking.dto.TransactionRequestDto;
import com.ebanking.dto.TransactionResponseDto;
import com.ebanking.exception.GlobalExceptionHandler;
import com.ebanking.mapper.TransactionMapper;
import com.ebanking.repository.AccountRepository;
import com.ebanking.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionController Unit Tests")
class TransactionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionMapper transactionMapper;

    private Customer testCustomer;
    private Account testAccount;
    private Transaction testTransaction;
    private TransactionRequestDto transactionRequestDto;
    private TransactionResponseDto transactionResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new com.ebanking.controller.TransactionController(
                        transactionService, accountRepository, transactionMapper))
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // Setup test data
        testCustomer = Customer.builder()
                .id("sherry")
                .password("encoded-password")
                .build();

        testAccount = Account.builder()
                .iban("12345")
                .currency("MYR")
                .customer(testCustomer)
                .build();

        testTransaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .account(testAccount)
                .amount(BigDecimal.valueOf(100.00))
                .currency("MYR")
                .valueDate(LocalDate.now())
                .description("Test transaction")
                .build();

        transactionRequestDto = new TransactionRequestDto();
        transactionRequestDto.setAccountIban("12345");
        transactionRequestDto.setAmount(BigDecimal.valueOf(100.00));
        transactionRequestDto.setDescription("Test transaction");
        transactionRequestDto.setValueDate(LocalDate.now());

        transactionResponseDto = new TransactionResponseDto();
        transactionResponseDto.setId(testTransaction.getId());
        transactionResponseDto.setAccountIban("12345");
        transactionResponseDto.setCurrency("MYR");
        transactionResponseDto.setAmount(BigDecimal.valueOf(100.00));
        transactionResponseDto.setDescription("Test transaction");
        transactionResponseDto.setValueDate(LocalDate.now());


        mockMvc = MockMvcBuilders
                .standaloneSetup(new TransactionController(
                        transactionService,
                        accountRepository,
                        transactionMapper))
                .setControllerAdvice(new GlobalExceptionHandler()) // Add this line
                .build();
    }

    @Test
    @DisplayName("Should create transaction successfully")
    void shouldCreateTransactionSuccessfully() throws Exception {
        // Given
        when(accountRepository.findById("12345")).thenReturn(Optional.of(testAccount));
        when(transactionMapper.toEntity(any(TransactionRequestDto.class), any(Account.class)))
                .thenReturn(testTransaction);
        when(transactionService.createTransaction(
                anyString(), any(BigDecimal.class), anyString(), any(LocalDate.class)))
                .thenReturn(testTransaction);
        when(transactionMapper.toDto(testTransaction)).thenReturn(transactionResponseDto);

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionResponseDto.getId())) // Use the DTO ID instead
                .andExpect(jsonPath("$.accountIban").value("12345"))
                .andExpect(jsonPath("$.currency").value("MYR"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.description").value("Test transaction"));

        verify(accountRepository).findById("12345");
        verify(transactionMapper).toEntity(any(TransactionRequestDto.class), any(Account.class));
        verify(transactionService).createTransaction(
                eq("12345"), eq(BigDecimal.valueOf(100.00)), eq("Test transaction"), any(LocalDate.class));
        verify(transactionMapper).toDto(testTransaction);
    }

    @Test
    @DisplayName("Should return 400 when account not found")
    void shouldReturn400WhenAccountNotFound() throws Exception {
        // Given
        when(accountRepository.findById("12345")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequestDto)))
                .andExpect(status().isBadRequest());

        verify(accountRepository).findById("12345");
        verify(transactionService, never()).createTransaction(anyString(), any(), anyString(), any());
    }

    @Test
    @DisplayName("Should get transactions by account")
    void shouldGetTransactionsByAccount() throws Exception {
        // Given
        List<Transaction> transactions = Arrays.asList(testTransaction);
        List<TransactionResponseDto> responseDtos = Arrays.asList(transactionResponseDto);

        when(transactionService.getTransactionsByAccount("12345")).thenReturn(transactions);
        when(transactionMapper.toDto(testTransaction)).thenReturn(transactionResponseDto);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/account/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testTransaction.getId()))
                .andExpect(jsonPath("$[0].accountIban").value("12345"))
                .andExpect(jsonPath("$[0].currency").value("MYR"))
                .andExpect(jsonPath("$[0].amount").value(100.00));

        verify(transactionService).getTransactionsByAccount("12345");
        verify(transactionMapper).toDto(testTransaction);
    }

    @Test
    @DisplayName("Should get transactions by customer")
    void shouldGetTransactionsByCustomer() throws Exception {
        // Given
        List<Transaction> transactions = Arrays.asList(testTransaction);
        List<TransactionResponseDto> responseDtos = Arrays.asList(transactionResponseDto);

        when(transactionService.getTransactionsByCustomer("sherry")).thenReturn(transactions);
        when(transactionMapper.toDto(testTransaction)).thenReturn(transactionResponseDto);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/customer/sherry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testTransaction.getId()))
                .andExpect(jsonPath("$[0].accountIban").value("12345"))
                .andExpect(jsonPath("$[0].currency").value("MYR"))
                .andExpect(jsonPath("$[0].amount").value(100.00));

        verify(transactionService).getTransactionsByCustomer("sherry");
        verify(transactionMapper).toDto(testTransaction);
    }

    @Test
    @DisplayName("Should return empty list when no transactions found")
    void shouldReturnEmptyListWhenNoTransactionsFound() throws Exception {
        // Given
        when(transactionService.getTransactionsByAccount("12345")).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/account/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(transactionService).getTransactionsByAccount("12345");
        verify(transactionMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should handle missing required fields in request")
    void shouldHandleMissingRequiredFieldsInRequest() throws Exception {
        // Given
        TransactionRequestDto invalidRequest = new TransactionRequestDto();
        invalidRequest.setAccountIban("12345");
        // Missing amount, description, valueDate

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(accountRepository, never()).findById(anyString());
        verify(transactionService, never()).createTransaction(anyString(), any(), anyString(), any());
    }

    @Test
    @DisplayName("Should handle null amount in request")
    void shouldHandleNullAmountInRequest() throws Exception {
        // Given
        TransactionRequestDto invalidRequest = new TransactionRequestDto();
        invalidRequest.setAccountIban("12345");
        invalidRequest.setAmount(null);
        invalidRequest.setDescription("Test");
        invalidRequest.setValueDate(LocalDate.now());

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(accountRepository, never()).findById(anyString());
        verify(transactionService, never()).createTransaction(anyString(), any(), anyString(), any());
    }
}