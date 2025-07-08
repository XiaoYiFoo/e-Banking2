package com.ebanking.service;

import com.ebanking.domain.Account;
import com.ebanking.domain.Customer;
import com.ebanking.repository.AccountRepository;
import com.ebanking.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public List<Account> getAccountsByCustomerId(String customerId) {
        return accountRepository.findByCustomer_Id(customerId);
    }

    public Account createAccount(String customerId, String iban, String currency) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        Account account = Account.builder()
                .iban(iban)
                .currency(currency)
                .customer(customer)
                .build();
        return accountRepository.save(account);
    }

    private String generateIBAN(String currency) {
        // Simple IBAN generation for demo purposes
        // In production, use proper IBAN generation algorithm
        String countryCode = switch (currency) {
            case "GBP" -> "GB";
            case "EUR" -> "DE";
            case "USD" -> "US";
            case "CHF" -> "CH";
            default -> "XX";
        };

        String randomPart = String.format("%016d", (long) (Math.random() * 10000000000000000L));
        return countryCode + "00" + randomPart;
    }
}