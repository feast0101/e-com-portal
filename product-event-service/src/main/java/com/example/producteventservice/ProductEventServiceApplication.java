package com.example.producteventservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Product Event Service. This microservice captures product transaction
 * events from Kafka and stores them in a database for audit and analytics.
 */
@SpringBootApplication
public class ProductEventServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProductEventServiceApplication.class, args);
  }
}
