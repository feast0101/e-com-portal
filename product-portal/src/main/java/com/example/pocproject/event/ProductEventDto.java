package com.example.pocproject.event;

import java.math.BigDecimal;

/** DTO for Product Events sent to Kafka. */
public class ProductEventDto {

  private String eventType; // CREATE, UPDATE, DELETE
  private Long productId;
  private String productName;
  private String description;
  private BigDecimal price;
  private Integer quantity;
  private Long timestamp;

  public ProductEventDto() {}

  public ProductEventDto(
      String eventType,
      Long productId,
      String productName,
      String description,
      BigDecimal price,
      Integer quantity) {
    this.eventType = eventType;
    this.productId = productId;
    this.productName = productName;
    this.description = description;
    this.price = price;
    this.quantity = quantity;
    this.timestamp = System.currentTimeMillis();
  }

  // Getters and Setters
  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public String toString() {
    return "ProductEventDto{"
        + "eventType='"
        + eventType
        + '\''
        + ", productId="
        + productId
        + ", productName='"
        + productName
        + '\''
        + ", description='"
        + description
        + '\''
        + ", price="
        + price
        + ", quantity="
        + quantity
        + ", timestamp="
        + timestamp
        + '}';
  }
}
