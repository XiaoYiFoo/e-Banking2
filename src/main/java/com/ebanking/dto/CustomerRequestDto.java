package com.ebanking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CustomerRequestDto {
    @Schema(description = "Customer ID", example = "P-1234567890")
    private String id;
    // Add other fields as needed, e.g. name, email, etc.
}