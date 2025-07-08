package com.ebanking.controller;

import com.ebanking.domain.Account;
import com.ebanking.dto.AccountDto;
import com.ebanking.repository.AccountRepository;
import com.ebanking.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountDto>> getAccountsByCustomer(@PathVariable String customerId) {
        List<Account> accounts = accountService.getAccountsByCustomerId(customerId);
        List<AccountDto> dtos = accounts.stream().map(account -> {
            AccountDto dto = new AccountDto();
            dto.setIban(account.getIban());
            dto.setCurrency(account.getCurrency());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@RequestBody AccountDto accountDto, @RequestParam String customerId) {
        Account account = accountService.createAccount(customerId, accountDto.getIban(), accountDto.getCurrency());
        AccountDto dto = new AccountDto();
        dto.setIban(account.getIban());
        dto.setCurrency(account.getCurrency());
        return ResponseEntity.ok(dto);
    }
}