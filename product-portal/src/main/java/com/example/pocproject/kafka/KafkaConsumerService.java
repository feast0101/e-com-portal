package com.example.pocproject.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Service for consuming Kafka messages.
 * Demonstrates basic Kafka integration.
 */
@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    /**
     * Listens for messages on the "product-events" Kafka topic.
     *
     * @param message The consumed message.
     */
    @KafkaListener(topics = "product-events", groupId = "poc-group")
    public void listenProductEvents(String message) {
        log.info("Consumed product event message: {}", message);
        // Here you would typically process the event, e.g., update a read model, send notifications, etc.
    }

    /**
     * Listens for messages on the "general-events" Kafka topic.
     *
     * @param message The consumed message.
     */
    @KafkaListener(topics = "general-events", groupId = "poc-group")
    public void listenGeneralEvents(String message) {
        log.info("Consumed general event message: {}", message);
    }
}
