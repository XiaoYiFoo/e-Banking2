package com.ebanking.repository;

import com.ebanking.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByCustomer_Id(String customerId);
    List<Account> findByCurrency(String currency);
}