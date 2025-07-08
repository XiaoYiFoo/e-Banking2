package com.ebanking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
            "GBP", "EUR", "USD", "CHF", "JPY", "CAD", "AUD", "NZD", "SEK", "NOK", "DKK",
            "PLN", "CZK", "HUF", "RON", "BGN", "HRK", "RUB", "TRY", "CNY", "HKD", "SGD",
            "KRW", "INR", "BRL", "MXN", "ZAR", "MYR", "THB", "IDR", "PHP", "VND"
    );

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        if (currency == null || currency.trim().isEmpty()) {
            return false;
        }

        // Check if it's a 3-letter uppercase code
        if (!currency.matches("^[A-Z]{3}$")) {
            return false;
        }

        // Check if it's in our supported currencies list
        return SUPPORTED_CURRENCIES.contains(currency);
    }
}