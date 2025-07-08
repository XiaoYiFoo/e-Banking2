//package com.ebanking.service;
//
//import com.ebanking.domain.Transaction;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class KafkaTransactionConsumer {
//
//    private final TransactionService transactionService;
//
//    @KafkaListener(
//            topics = "${app.kafka.topic.transactions}",
//            groupId = "${spring.kafka.consumer.group-id}",
//            containerFactory = "kafkaListenerContainerFactory"
//    )
//    public void consumeTransaction(
//            @Payload Transaction transaction,
//            Acknowledgment acknowledgment,
//            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
//            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
//            @Header(KafkaHeaders.OFFSET) long offset) {
//        try {
//            log.debug("Received transaction from Kafka - Topic: {}, Partition: {}, Offset: {}, Transaction ID: {}",
//                    topic, partition, offset, transaction.getId());
//            // Save to DB
//            transactionService.processTransaction(transaction);
//            acknowledgment.acknowledge();
//            log.debug("Successfully processed transaction: {}", transaction.getId());
//        } catch (Exception e) {
//            log.error("Error processing transaction from Kafka - Transaction ID: {}, Error: {}",
//                    transaction.getId(), e.getMessage(), e);
//            acknowledgment.acknowledge();
//        }
//    }
//}

package com.ebanking.service;

import com.ebanking.domain.Account;
import com.ebanking.domain.Transaction;
import com.ebanking.dto.TransactionKafkaDto;
import com.ebanking.repository.AccountRepository;
import com.ebanking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaTransactionConsumer {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @KafkaListener(
            topics = "transactions",
            groupId = "transaction-group",
            containerFactory = "transactionKafkaListenerContainerFactory"
    )
    public void consume(TransactionKafkaDto dto) {
        log.info("Received transaction from Kafka: {}", dto);

        Optional<Account> accountOpt = accountRepository.findById(dto.getAccountIban());
        if (accountOpt.isEmpty()) {
            log.error("Account with IBAN {} not found. Transaction will not be saved.", dto.getAccountIban());
            return;
        }

        Account account = accountOpt.get();

        Transaction transaction = Transaction.builder()
                .id(dto.getId() != null ? dto.getId() : UUID.randomUUID().toString())
                .account(account)
                .amount(dto.getAmount())
                .currency(account.getCurrency()) // Always use the account's currency
                .valueDate(dto.getValueDate())
                .description(dto.getDescription())
                .build();

        transactionRepository.save(transaction);
        log.info("Transaction saved to database: {}", transaction.getId());
    }
}