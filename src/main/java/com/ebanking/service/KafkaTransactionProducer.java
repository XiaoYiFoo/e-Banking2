package com.ebanking.service;

import com.ebanking.domain.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Kafka Producer Service for sending transaction messages.
 *
 * Used for testing and demonstration purposes to send
 * transaction messages to the Kafka topic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaTransactionProducer {

    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    @Value("${app.kafka.topic.transactions}")
    private String topicName;

    /**
     * Sends a transaction message to Kafka.
     *
     * @param transaction Transaction to send
     * @return CompletableFuture with send result
     */
    public CompletableFuture<SendResult<String, Transaction>> sendTransaction(Transaction transaction) {
        log.info("Sending transaction to Kafka: {}", transaction.getId());

        return kafkaTemplate.send(topicName, transaction.getId(), transaction)
            .whenComplete((result, throwable) -> {
                if (throwable == null) {
                    log.info("Transaction sent successfully to Kafka: {} at offset: {}",
                            transaction.getId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send transaction to Kafka: {}", throwable.getMessage());
                }
            });
    }

    /**
     * Sends a transaction message to Kafka synchronously.
     *
     * @param transaction Transaction to send
     * @return SendResult with metadata
     * @throws RuntimeException if sending fails
     */
    public SendResult<String, Transaction> sendTransactionSync(Transaction transaction) {
        log.info("Sending transaction to Kafka: {}", transaction.getId());

        try {
            // Use get() to wait for completion
            SendResult<String, Transaction> result = kafkaTemplate.send(topicName, transaction.getId(), transaction)
                    .get(6, TimeUnit.SECONDS);

            log.info("Transaction sent successfully to Kafka: {} at offset: {}",
                    transaction.getId(), result.getRecordMetadata().offset());

            return result;

        } catch (Exception e) {
            log.error("Failed to send transaction to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send transaction to Kafka: " + e.getMessage(), e);
        }
    }

    /**
     * Sends multiple test transactions to Kafka.
     *
     * @param customerId Customer ID for the transactions
     * @param count Number of transactions to send
     */

}