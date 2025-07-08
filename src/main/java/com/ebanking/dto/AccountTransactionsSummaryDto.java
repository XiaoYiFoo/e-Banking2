// src/main/java/com/ebanking/dto/AccountTransactionsSummaryDto.java
package com.ebanking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AccountTransactionsSummaryDto {
    @Schema(description = "List of transactions for the account")
    private List<TransactionResponseDto> transactions;

    @Schema(description = "Total debit amount", example = "100.00")
    private BigDecimal totalDebit;

    @Schema(description = "Total credit amount", example = "200.00")
    private BigDecimal totalCredit;
}