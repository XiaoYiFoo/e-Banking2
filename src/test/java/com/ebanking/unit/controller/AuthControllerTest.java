package com.ebanking.unit.controller;

import com.ebanking.controller.AuthController;
import com.ebanking.domain.Customer;
import com.ebanking.dto.LoginRequest;
import com.ebanking.dto.LoginResponse;
import com.ebanking.dto.RegisterRequest;
import com.ebanking.exception.GlobalExceptionHandler;
import com.ebanking.repository.CustomerRepository;
import com.ebanking.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Create a standalone setup with GlobalExceptionHandler
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(
                        authenticationManager,
                        jwtService,
                        customerRepository,
                        passwordEncoder))
                .setControllerAdvice(new GlobalExceptionHandler()) // Add this line
                .build();
    }

    @Test
    @DisplayName("Should return 400 when login request is missing required fields")
    void shouldReturn400WhenLoginRequestIsMissingRequiredFields() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setCustomerId("sherry");
        // password is null

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("Should return 400 when login request has empty customerId")
    void shouldReturn400WhenLoginRequestHasEmptyCustomerId() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setCustomerId(""); // empty string
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("Should return 400 when login request has empty password")
    void shouldReturn400WhenLoginRequestHasEmptyPassword() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setCustomerId("sherry");
        request.setPassword(""); // empty string

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfullyWithValidCredentials() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setCustomerId("sherry");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("sherry")).thenReturn("jwt-token");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.customerId").value("sherry"))
                .andExpect(jsonPath("$.message").value("Login successful"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken("sherry");
    }

    @Test
    @DisplayName("Should return 401 when credentials are invalid")
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setCustomerId("sherry");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Should register successfully with valid data")
    void shouldRegisterSuccessfullyWithValidData() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setCustomerId("newuser");
        request.setPassword("password123");

        when(customerRepository.existsById("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            return customer;
        });

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("newuser"));

        verify(customerRepository).existsById("newuser");
        verify(passwordEncoder).encode("password123");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should return 400 when register request is missing required fields")
    void shouldReturn400WhenRegisterRequestIsMissingRequiredFields() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setCustomerId("newuser");
        // password is null

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(customerRepository, never()).existsById(anyString());
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return 409 when customer already exists")
    void shouldReturn409WhenCustomerAlreadyExists() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setCustomerId("existinguser");
        request.setPassword("password123");

        when(customerRepository.existsById("existinguser")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()); // Changed from isBadRequest() to isConflict()

        verify(customerRepository).existsById("existinguser");
        verify(customerRepository, never()).save(any());
    }
}