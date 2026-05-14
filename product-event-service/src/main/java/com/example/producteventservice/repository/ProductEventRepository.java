package com.example.producteventservice.repository;

import com.example.producteventservice.model.ProductEventEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for Product Event entities. */
@Repository
public interface ProductEventRepository extends JpaRepository<ProductEventEntity, Long> {

  /** Find all events for a specific product with pagination. */
  Page<ProductEventEntity> findByProductId(Long productId, Pageable pageable);

  /** Find all events for a specific product. */
  List<ProductEventEntity> findByProductIdOrderByEventTimestampDesc(Long productId);

  /** Find all events of a specific type with pagination. */
  Page<ProductEventEntity> findByEventType(String eventType, Pageable pageable);

  /** Find all events of a specific type. */
  List<ProductEventEntity> findByEventTypeOrderByEventTimestampDesc(String eventType);

  /** Find events within a time range. */
  List<ProductEventEntity> findByEventTimestampBetweenOrderByEventTimestampDesc(
      LocalDateTime start, LocalDateTime end);

  /** Find recent events (limit by count). */
  List<ProductEventEntity> findTop100ByOrderByReceivedTimestampDesc();
}
