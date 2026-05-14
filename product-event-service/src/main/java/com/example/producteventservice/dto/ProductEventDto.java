package com.example.producteventservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for Product Event received from Kafka. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductEventDto {

  @JsonProperty("eventType")
  private String eventType; // CREATE, UPDATE, DELETE

  @JsonProperty("productId")
  private Long productId;

  @JsonProperty("productName")
  private String productName;

  @JsonProperty("description")
  private String description;

  @JsonProperty("price")
  private BigDecimal price;

  @JsonProperty("quantity")
  private Integer quantity;

  @JsonProperty("timestamp")
  private Long timestamp;
}
