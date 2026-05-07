package com.example.pocproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entry point for the Spring Boot POC application.
 * This class enables various features like caching, retries, and asynchronous execution.
 */
@SpringBootApplication
@EnableCaching // Enables Spring's cache management
@EnableRetry // Enables Spring's retry functionality
@EnableAsync // Enables Spring's @Async annotation for asynchronous method execution
public class PocProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocProjectApplication.class, args);
	}

}
