//package com.ebanking.dto;
//
//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.Data;
//
//@Data
//public class AccountDto {
//    private String iban;
//    private String currency;
//}
//

package com.ebanking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Account information")
public class AccountDto {

    @Schema(
            description = "International Bank Account Number (IBAN)",
            example = "GB29NWBK60161331926819"
    )
    private String iban;

    @Schema(
            description = "Currency code (ISO 4217 3-letter code)",
            example = "GBP",
            allowableValues = {"GBP", "EUR", "USD", "CHF", "JPY", "CAD", "AUD", "NZD"}
    )
    private String currency;

}