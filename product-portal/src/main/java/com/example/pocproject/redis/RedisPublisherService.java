package com.example.pocproject.redis;

import com.example.pocproject.event.ProductEventDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis publisher service for publishing product events to Redis channels.
 */
@Service
public class RedisPublisherService {
    
    private static final Logger log = LoggerFactory.getLogger(RedisPublisherService.class);
    private static final String PRODUCT_EVENTS_CHANNEL = "product-events-channel";
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public RedisPublisherService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Publishes a product event to Redis channel.
     *
     * @param event The ProductEventDto to publish.
     */
    public void publishProductEvent(ProductEventDto event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(PRODUCT_EVENTS_CHANNEL, jsonMessage);
            log.info("Published ProductEvent to Redis channel {}: eventType={}, productId={}, productName={}", 
                    PRODUCT_EVENTS_CHANNEL, event.getEventType(), event.getProductId(), event.getProductName());
        } catch (JsonProcessingException e) {
            log.error("Error serializing ProductEventDto to JSON for Redis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish product event to Redis", e);
        }
    }
}
