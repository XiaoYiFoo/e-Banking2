package com.ebanking.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Data
public class LoginRequest {
    @NotBlank(message = "Customer ID is required")
    @NotEmpty(message = "Customer ID cannot be empty")
    @Size(min = 1, message = "Customer ID must not be empty")
    private String customerId;

    @NotBlank(message = "Password is required")
    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 1, message = "Password must not be empty")
    private String password;
}