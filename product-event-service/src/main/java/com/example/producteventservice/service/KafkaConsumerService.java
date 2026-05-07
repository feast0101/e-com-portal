package com.example.producteventservice.service;

import com.example.producteventservice.dto.ProductEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer service that listens to product-events topic
 * and stores all events in the database.
 */
@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final ProductEventService productEventService;
    private final ObjectMapper objectMapper;

    public KafkaConsumerService(ProductEventService productEventService, ObjectMapper objectMapper) {
        this.productEventService = productEventService;
        this.objectMapper = objectMapper;
    }

    /**
     * Listens to product-events topic and processes incoming messages as Strings.
     * Manually deserializes to handle both JSON objects and String messages.
     */
    @KafkaListener(
        topics = "${kafka.topic.product-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeProductEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received message from topic: {}, partition: {}, offset: {}", topic, partition, offset);
        log.info("Message content: {}", message);

        try {
            // Remove extra quotes if present (handles double-serialization)
            String cleanMessage = message;
            if (message.startsWith("\"") && message.endsWith("\"")) {
                cleanMessage = message.substring(1, message.length() - 1);
                // Unescape the inner JSON
                cleanMessage = cleanMessage.replace("\\\"", "\"");
                log.info("Cleaned message: {}", cleanMessage);
            }

            // Try to parse as ProductEventDto JSON
            ProductEventDto eventDto = objectMapper.readValue(cleanMessage, ProductEventDto.class);
            productEventService.processEvent(eventDto, cleanMessage);
            log.info("Successfully processed {} event for product ID: {}, Name: {}",
                    eventDto.getEventType(), eventDto.getProductId(), eventDto.getProductName());

        } catch (Exception e) {
            // If JSON parsing fails, store as raw string message
            log.warn("Could not parse message as ProductEventDto JSON, storing as raw string: {}", e.getMessage());
            productEventService.processStringMessage(message);
        }
    }

    /**
     * Consumer for any other general events (optional).
     */
    @KafkaListener(
        topics = "general-events",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeGeneralEvent(@Payload String message) {
        log.info("Received general event: {}", message);
        // You can add processing logic for general events here if needed
    }
}
