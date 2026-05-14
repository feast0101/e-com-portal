package com.example.pocproject.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a product in the system. This entity is used for JPA persistence and Redis caching.
 */
@Entity
@Table(name = "products")
public class Product implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;
  private BigDecimal price;
  private int quantity;

  public Product() {}

  public Product(String name, String description, BigDecimal price, int quantity) {
    this.name = name;
    this.description = description;
    this.price = price;
    this.quantity = quantity;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Product product = (Product) o;
    return quantity == product.quantity
        && Objects.equals(id, product.id)
        && Objects.equals(name, product.name)
        && Objects.equals(description, product.description)
        && Objects.equals(price, product.price);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, price, quantity);
  }

  @Override
  public String toString() {
    return "Product{"
        + "id="
        + id
        + ", name='"
        + name
        + "'"
        + ", description='"
        + description
        + "'"
        + ", price="
        + price
        + ", quantity="
        + quantity
        + "}";
  }
}
