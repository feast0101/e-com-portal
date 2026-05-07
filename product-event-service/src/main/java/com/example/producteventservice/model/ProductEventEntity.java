package com.example.producteventservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity to store product transaction events.
 * Captures all product CRUD operations received from Kafka.
 */
@Entity
@Table(name = "product_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductEventEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_type", nullable = false)
    private String eventType; // CREATE, UPDATE, DELETE
    
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "product_name")
    private String productName;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "price")
    private BigDecimal price;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Column(name = "event_timestamp")
    private LocalDateTime eventTimestamp;
    
    @Column(name = "received_timestamp")
    private LocalDateTime receivedTimestamp;
    
    @Column(name = "raw_message", length = 2000)
    private String rawMessage; // Store raw Kafka message for debugging
    
    @PrePersist
    public void prePersist() {
        if (receivedTimestamp == null) {
            receivedTimestamp = LocalDateTime.now();
        }
    }
}
