package com.ebanking.mapper;

import com.ebanking.domain.Account;
import com.ebanking.domain.Transaction;
import com.ebanking.dto.TransactionRequestDto;
import com.ebanking.dto.TransactionResponseDto;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequestDto dto, Account account) {
        return Transaction.builder()
                .account(account)
                .amount(dto.getAmount())
                .currency(account.getCurrency())
                .description(dto.getDescription())
                .valueDate(dto.getValueDate())
                .build();
    }

    public TransactionResponseDto toDto(Transaction transaction) {
        TransactionResponseDto dto = new TransactionResponseDto();
        dto.setId(transaction.getId());
        dto.setAccountIban(transaction.getAccount().getIban());
        dto.setCurrency(transaction.getCurrency());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setValueDate(transaction.getValueDate());
        return dto;
    }
}