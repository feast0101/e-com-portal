package com.example.producteventservice.service;

import com.example.producteventservice.dto.ProductEventDto;
import com.example.producteventservice.model.ProductEventEntity;
import com.example.producteventservice.repository.ProductEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for processing and storing product events. */
@Service
public class ProductEventService {

  private static final Logger log = LoggerFactory.getLogger(ProductEventService.class);

  private final ProductEventRepository eventRepository;
  private final ObjectMapper objectMapper;

  public ProductEventService(ProductEventRepository eventRepository, ObjectMapper objectMapper) {
    this.eventRepository = eventRepository;
    this.objectMapper = objectMapper;
  }

  /** Process and store a product event from Kafka. */
  @Transactional
  public ProductEventEntity processEvent(ProductEventDto eventDto, String rawMessage) {
    log.info("Processing product event: {}", eventDto.getEventType());

    ProductEventEntity entity = new ProductEventEntity();
    entity.setEventType(eventDto.getEventType());
    entity.setProductId(eventDto.getProductId());
    entity.setProductName(eventDto.getProductName());
    entity.setDescription(eventDto.getDescription());
    entity.setPrice(eventDto.getPrice());
    entity.setQuantity(eventDto.getQuantity());

    // Convert timestamp from epoch to LocalDateTime
    if (eventDto.getTimestamp() != null) {
      entity.setEventTimestamp(
          LocalDateTime.ofInstant(
              Instant.ofEpochMilli(eventDto.getTimestamp()), ZoneId.systemDefault()));
    } else {
      entity.setEventTimestamp(LocalDateTime.now());
    }

    entity.setRawMessage(rawMessage);

    ProductEventEntity saved = eventRepository.save(entity);
    log.info("Stored product event with ID: {}", saved.getId());

    return saved;
  }

  /** Process a raw string message (for backward compatibility). */
  @Transactional
  public ProductEventEntity processStringMessage(String message) {
    log.info("Processing raw string message: {}", message);

    ProductEventEntity entity = new ProductEventEntity();
    entity.setEventType("UNKNOWN");
    entity.setRawMessage(message);
    entity.setEventTimestamp(LocalDateTime.now());

    return eventRepository.save(entity);
  }

  /** Get all events with pagination and sorting. */
  public Page<ProductEventEntity> getAllEvents(Pageable pageable) {
    return eventRepository.findAll(pageable);
  }

  /** Get all events (non-paginated). */
  public List<ProductEventEntity> getAllEvents() {
    return eventRepository.findAll();
  }

  /** Get recent events (last 100). */
  public List<ProductEventEntity> getRecentEvents() {
    return eventRepository.findTop100ByOrderByReceivedTimestampDesc();
  }

  /** Get events for a specific product with pagination. */
  public Page<ProductEventEntity> getEventsByProductId(Long productId, Pageable pageable) {
    return eventRepository.findByProductId(productId, pageable);
  }

  /** Get events for a specific product (non-paginated). */
  public List<ProductEventEntity> getEventsByProductId(Long productId) {
    return eventRepository.findByProductIdOrderByEventTimestampDesc(productId);
  }

  /** Get events by type with pagination. */
  public Page<ProductEventEntity> getEventsByType(String eventType, Pageable pageable) {
    return eventRepository.findByEventType(eventType.toUpperCase(), pageable);
  }

  /** Get events by type (non-paginated). */
  public List<ProductEventEntity> getEventsByType(String eventType) {
    return eventRepository.findByEventTypeOrderByEventTimestampDesc(eventType);
  }

  /** Get total event count. */
  public long getTotalEventCount() {
    return eventRepository.count();
  }
}
