package com.example.producteventservice.config;

import com.example.producteventservice.dto.ProductEventDto;
import com.example.producteventservice.service.ProductEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

/**
 * Redis subscriber service that listens to product-events-channel and stores events in the
 * database.
 */
@Service
public class RedisSubscriberService implements MessageListener {

  private static final Logger log = LoggerFactory.getLogger(RedisSubscriberService.class);

  private final ProductEventService productEventService;
  private final ObjectMapper objectMapper;

  public RedisSubscriberService(
      ProductEventService productEventService, ObjectMapper objectMapper) {
    this.productEventService = productEventService;
    this.objectMapper = objectMapper;
  }

  /** Callback method invoked when a message is received from Redis channel. */
  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String messageBody = new String(message.getBody());
      log.info("Received message from Redis channel: {}", messageBody);

      // Remove extra quotes if present (handles double-serialization)
      String cleanMessage = messageBody;
      if (messageBody.startsWith("\"") && messageBody.endsWith("\"")) {
        cleanMessage = messageBody.substring(1, messageBody.length() - 1);
        cleanMessage = cleanMessage.replace("\\\"", "\"");
        log.info("Cleaned Redis message: {}", cleanMessage);
      }

      // Try to parse as ProductEventDto JSON
      ProductEventDto eventDto = objectMapper.readValue(cleanMessage, ProductEventDto.class);
      productEventService.processEvent(eventDto, cleanMessage);
      log.info(
          "Successfully processed {} event from Redis for product ID: {}, Name: {}",
          eventDto.getEventType(),
          eventDto.getProductId(),
          eventDto.getProductName());

    } catch (Exception e) {
      log.error("Error processing Redis message: {}", e.getMessage(), e);
      // Store as string message if processing fails
      try {
        String messageBody = new String(message.getBody());
        productEventService.processStringMessage("Redis message: " + messageBody);
      } catch (Exception ex) {
        log.error("Failed to store Redis message as string: {}", ex.getMessage());
      }
    }
  }
}
