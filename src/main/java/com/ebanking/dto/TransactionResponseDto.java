package com.ebanking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionResponseDto {
    private String id;
    private String accountIban;
    private String currency;
    private BigDecimal amount;
    private String description;
    private LocalDate valueDate;
}