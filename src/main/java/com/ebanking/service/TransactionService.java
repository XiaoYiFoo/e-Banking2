//package com.ebanking.service;
//
//import com.ebanking.domain.Transaction;
//import com.ebanking.dto.TransactionRequest;
//import com.ebanking.dto.TransactionResponse;
//import com.ebanking.repository.TransactionRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.YearMonth;
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class TransactionService {
//
//    private final TransactionRepository transactionRepository;
//    private final ExchangeRateService exchangeRateService;
//
//    public void processTransaction(Transaction transaction) {
//        log.debug("Saving transaction to DB: {}", transaction.getId());
//        transactionRepository.save(transaction);
//    }
//
//    public TransactionResponse getTransactions(String customerId, TransactionRequest request) {
//        YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());
//        LocalDate start = ym.atDay(1);
//        LocalDate end = ym.atEndOfMonth();
//
//        var page = transactionRepository.findByCustomerIdAndValueDateBetween(
//                customerId, start, end, PageRequest.of(request.getPage(), request.getSize())
//        );
//
//        List<Transaction> transactions = page.getContent();
//
//        // Calculate totals
//        BigDecimal totalCredit = BigDecimal.ZERO;
//        BigDecimal totalDebit = BigDecimal.ZERO;
//        for (Transaction transaction : transactions) {
//            BigDecimal convertedAmount = exchangeRateService.convertToBaseCurrency(
//                    transaction.getAmount(),
//                    transaction.getCurrency(),
//                    request.getBaseCurrency(),
//                    transaction.getValueDate()
//            );
//            // Handle null converted amount gracefully
//            if (convertedAmount == null) {
//                log.warn("Exchange rate service returned null for transaction {}, using original amount",
//                        transaction.getId());
//                convertedAmount = transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO;
//            }
//            if (transaction.isCredit()) {
//                totalCredit = totalCredit.add(convertedAmount);
//            } else {
//                totalDebit = totalDebit.add(convertedAmount.abs());
//            }
//        }
//
//        return TransactionResponse.builder()
//                .transactions(transactions)
//                .totalCredit(totalCredit)
//                .totalDebit(totalDebit)
//                .baseCurrency(request.getBaseCurrency())
//                .page(request.getPage())
//                .size(request.getSize())
//                .totalPages(page.getTotalPages())
//                .totalElements((int) page.getTotalElements())
//                .first(page.isFirst())
//                .last(page.isLast())
//                .build();
//    }
//}

package com.ebanking.service;

import com.ebanking.domain.Account;
import com.ebanking.domain.Transaction;
import com.ebanking.repository.AccountRepository;
import com.ebanking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public Transaction createTransaction(String accountIban, BigDecimal amount, String description, LocalDate valueDate) {
        Account account = accountRepository.findById(accountIban)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountIban));
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .account(account)
                .amount(amount)
                .currency(account.getCurrency())
                .description(description)
                .valueDate(valueDate)
                .build();
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsByAccount(String accountIban) {
        return transactionRepository.findByAccount_Iban(accountIban);
    }

    public List<Transaction> getTransactionsByCustomer(String customerId) {
        return transactionRepository.findByAccount_Customer_Id(customerId);
    }
}