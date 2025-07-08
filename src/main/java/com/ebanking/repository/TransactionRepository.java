package com.ebanking.repository;

import com.ebanking.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Page<Transaction> findByCustomerIdAndValueDateBetween(
            String customerId, LocalDate start, LocalDate end, Pageable pageable
    );

    List<Transaction> findByAccount_Iban(String iban);
    List<Transaction> findByAccount_Customer_Id(String customerId);
    List<Transaction> findByAccount_Currency(String currency);
}

