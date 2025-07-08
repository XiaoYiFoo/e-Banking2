package com.ebanking.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    private String iban;

    @Column(nullable = false, length = 3)
    private String currency; // ISO 4217, e.g. "GBP", "EUR", "CHF"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Add other account fields as needed (balance, type, etc.)
}