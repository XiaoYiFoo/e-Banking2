package com.ebanking.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequestDto {
    @NotBlank(message = "Account IBAN is required")
    private String accountIban;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Value date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate valueDate;
}