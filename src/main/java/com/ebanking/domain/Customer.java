package com.ebanking.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    private String id; // e.g. "P-0123456789"

    @Column(nullable = false)
    private String password; // Will be hashed

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;

    // Add other customer fields as needed (name, email, etc.)
}