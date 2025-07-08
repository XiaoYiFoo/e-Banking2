package com.ebanking.repository;

import com.ebanking.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    // You can add custom queries if needed
}