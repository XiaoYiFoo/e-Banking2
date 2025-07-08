package com.ebanking.controller;

import com.ebanking.domain.Customer;
import com.ebanking.dto.LoginRequest;
import com.ebanking.dto.LoginResponse;
import com.ebanking.dto.RegisterRequest;
import com.ebanking.repository.CustomerRepository;
import com.ebanking.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/v1/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final AuthenticationManager authenticationManager;
//    private final JwtService jwtService;
//    private final CustomerRepository customerRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Operation(
//            summary = "Customer Login",
//            description = "Authenticate a customer and return a JWT token for API access",
//            operationId = "loginCustomer"
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Login successful",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = LoginResponse.class),
//                            examples = @ExampleObject(
//                                    name = "Successful Login",
//                                    value = """
//                        {
//                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
//                          "customerId": "CUST123456",
//                          "message": "Login successful"
//                        }
//                        """
//                            )
//                    )
//            ),
//            @ApiResponse(
//                    responseCode = "400",
//                    description = "Invalid request format",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = com.ebanking.dto.ErrorResponse.class),
//                            examples = @ExampleObject(
//                                    name = "Validation Error",
//                                    value = """
//                        {
//                          "status": 400,
//                          "error": "Validation Error",
//                          "message": "Request validation failed",
//                          "timestamp": "2024-01-15T10:30:00",
//                          "validationErrors": [
//                            {
//                              "field": "customerId",
//                              "message": "Customer ID is required"
//                            }
//                          ]
//                        }
//                        """
//                            )
//                    )
//            ),
//            @ApiResponse(
//                    responseCode = "401",
//                    description = "Invalid credentials",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = com.ebanking.dto.ErrorResponse.class),
//                            examples = @ExampleObject(
//                                    name = "Bad Credentials",
//                                    value = """
//                        {
//                          "status": 401,
//                          "error": "Bad Credentials",
//                          "message": "Invalid customer ID or password",
//                          "timestamp": "2024-01-15T10:30:00"
//                        }
//                        """
//                            )
//                    )
//            )
//    })
//    @PostMapping("/login")
//    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
//        try {
//            log.info("Attempting login for customer: {}", request.getCustomerId());
//
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(request.getCustomerId(), request.getPassword())
//            );
//
//            String token = jwtService.generateToken(request.getCustomerId());
//
//            LoginResponse response = new LoginResponse(
//                    token,
//                    request.getCustomerId(),
//                    "Login successful"
//            );
//
//            return ResponseEntity.ok(response);
//        } catch (BadCredentialsException e) {
//            log.warn("Bad credentials for customer: {}", request.getCustomerId());
//            throw e; // Let GlobalExceptionHandler handle it
//        } catch (UsernameNotFoundException e) {
//            log.warn("User not found: {}", request.getCustomerId());
//            throw e; // Let GlobalExceptionHandler handle it
//        }
//    }
//
//    @PostMapping("/register")
//    public ResponseEntity<Customer> register(@Valid @RequestBody RegisterRequest request) {
//        log.info("Attempting registration for customer: {}", request.getCustomerId());
//
//        if (customerRepository.existsById(request.getCustomerId())) {
//            throw new IllegalArgumentException("Customer with ID '" + request.getCustomerId() + "' already exists");
//        }
//
//        Customer customer = Customer.builder()
//                .id(request.getCustomerId())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .build();
//
//        Customer saved = customerRepository.save(customer);
//        log.info("Successfully registered customer: {}", request.getCustomerId());
//        return ResponseEntity.ok(saved);
//    }
//}


@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(
            summary = "Customer Login",
            description = "Authenticate a customer and return a JWT token for API access",
            operationId = "loginCustomer"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Login",
                                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "customerId": "CUST123456",
                          "message": "Login successful"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.ebanking.dto.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                        {
                          "status": 400,
                          "error": "Validation Error",
                          "message": "Request validation failed",
                          "timestamp": "2024-01-15T10:30:00",
                          "validationErrors": [
                            {
                              "field": "customerId",
                              "message": "Customer ID is required"
                            }
                          ]
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.ebanking.dto.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Bad Credentials",
                                    value = """
                        {
                          "status": 401,
                          "error": "Bad Credentials",
                          "message": "Invalid customer ID or password",
                          "timestamp": "2024-01-15T10:30:00"
                        }
                        """
                            )
                    )
            )
    })
    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LoginResponse> login(
            @Parameter(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Login Request",
                                    value = """
                            {
                              "customerId": "CUST123456",
                              "password": "securePassword123"
                            }
                            """
                            )
                    )
            )
            @Valid @RequestBody LoginRequest request) {

        try {
            log.info("Attempting login for customer: {}", request.getCustomerId());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCustomerId(), request.getPassword())
            );

            String token = jwtService.generateToken(request.getCustomerId());

            LoginResponse response = new LoginResponse(
                    token,
                    request.getCustomerId(),
                    "Login successful"
            );

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Bad credentials for customer: {}", request.getCustomerId());
            throw e;
        } catch (UsernameNotFoundException e) {
            log.warn("User not found: {}", request.getCustomerId());
            throw e;
        }
    }

    @Operation(
            summary = "Customer Registration",
            description = "Register a new customer account with the provided credentials",
            operationId = "registerCustomer"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Registration successful",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Customer.class),
                            examples = @ExampleObject(
                                    name = "Successful Registration",
                                    value = """
                        {
                          "id": "CUST123456",
                          "password": "$2a$10$encryptedPasswordHash",
                          "accounts": []
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format or validation error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.ebanking.dto.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                        {
                          "status": 400,
                          "error": "Validation Error",
                          "message": "Request validation failed",
                          "timestamp": "2024-01-15T10:30:00",
                          "validationErrors": [
                            {
                              "field": "password",
                              "message": "Password must be at least 6 characters"
                            }
                          ]
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Customer already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.ebanking.dto.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Customer Already Exists",
                                    value = """
                        {
                          "status": 409,
                          "error": "Customer Already Exists",
                          "message": "Customer with ID 'CUST123456' already exists",
                          "timestamp": "2024-01-15T10:30:00"
                        }
                        """
                            )
                    )
            )
    })
    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Customer> register(
            @Parameter(
                    description = "Registration details",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Registration Request",
                                    value = """
                            {
                              "customerId": "CUST123456",
                              "password": "securePassword123"
                            }
                            """
                            )
                    )
            )
            @Valid @RequestBody RegisterRequest request) {

        log.info("Attempting registration for customer: {}", request.getCustomerId());

        if (customerRepository.existsById(request.getCustomerId())) {
            throw new IllegalArgumentException("Customer with ID '" + request.getCustomerId() + "' already exists");
        }

        Customer customer = Customer.builder()
                .id(request.getCustomerId())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Successfully registered customer: {}", request.getCustomerId());
        return ResponseEntity.ok(saved);
    }

    @Operation(
            summary = "Health Check",
            description = "Check if the authentication service is running",
            operationId = "healthCheck"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Service is healthy",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Health Check Response",
                                    value = """
                        {
                          "status": "UP",
                          "service": "Authentication Service",
                          "timestamp": "2024-01-15T10:30:00"
                        }
                        """
                            )
                    )
            )
    })
    @GetMapping(
            value = "/health",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Object> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Authentication Service",
                "timestamp", java.time.LocalDateTime.now()
        ));
    }
}