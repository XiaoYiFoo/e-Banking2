package com.ebanking.dto;

import com.ebanking.validation.ValidCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Account creation request")
public class AccountRequestDto {

    @Schema(
            description = "Currency code (ISO 4217 3-letter code)",
            example = "GBP",
            allowableValues = {"GBP", "EUR", "USD", "CHF", "JPY", "CAD", "AUD", "NZD"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Currency is required")
    @ValidCurrency(message = "Currency must be a valid 3-letter ISO 4217 code")
    private String currency;
}