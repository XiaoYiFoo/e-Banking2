package com.ebanking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CurrencyValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {
    String message() default "Currency must be a valid 3-letter ISO 4217 code";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}