package com.example.pocproject.kafka;

import com.example.pocproject.event.ProductEventDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for producing and consuming Kafka messages.
 * Demonstrates basic Kafka integration.
 */
@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends a message to the specified Kafka topic.
     *
     * @param topic The Kafka topic to send the message to.
     * @param message The message content.
     */
    public void sendMessage(String topic, String message) {
        log.info("Producing message to topic {}: {}", topic, message);
        kafkaTemplate.send(topic, message);
    }

    /**
     * Sends a ProductEvent as JSON to the specified Kafka topic.
     *
     * @param topic The Kafka topic to send the message to.
     * @param event The ProductEventDto to send.
     */
    public void sendProductEvent(String topic, ProductEventDto event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            log.info("Producing ProductEvent to topic {}: {}", topic, jsonMessage);
            kafkaTemplate.send(topic, jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("Error serializing ProductEventDto to JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send product event", e);
        }
    }
}
