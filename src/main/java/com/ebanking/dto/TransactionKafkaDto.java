package com.ebanking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionKafkaDto {
    private String id;
    private String accountIban;
    private BigDecimal amount;
    private String currency;
    private LocalDate valueDate;
    private String description;
}