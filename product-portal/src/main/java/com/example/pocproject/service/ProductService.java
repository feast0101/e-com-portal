package com.example.pocproject.service;

import com.example.pocproject.event.ProductEventDto;
import com.example.pocproject.kafka.KafkaProducerService;
import com.example.pocproject.model.Product;
import com.example.pocproject.redis.RedisPublisherService;
import com.example.pocproject.repository.ProductRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service class for managing {@link Product} entities.
 * Demonstrates JPA, Transaction Management, Redis Caching, Resilient4j Circuit Breaker,
 * Spring Retry, and Asynchronous processing.
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final KafkaProducerService kafkaProducerService;
    private final RedisPublisherService redisPublisherService;

    public ProductService(ProductRepository productRepository,
                         KafkaProducerService kafkaProducerService,
                         RedisPublisherService redisPublisherService) {
        this.productRepository = productRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.redisPublisherService = redisPublisherService;
    }

    /**
     * Creates a new product.
     * Demonstrates transactional operation and Kafka event publishing.
     *
     * @param product The product to create.
     * @return The created product.
     */
    @Transactional
    public Product createProduct(Product product) {
        Product savedProduct = productRepository.save(product);

        // Create event DTO
        ProductEventDto event = new ProductEventDto(
            "CREATE",
            savedProduct.getId(),
            savedProduct.getName(),
            savedProduct.getDescription(),
            savedProduct.getPrice(),
            savedProduct.getQuantity()
        );

        // Publish to both Kafka and Redis
        kafkaProducerService.sendProductEvent("product-events", event);
        redisPublisherService.publishProductEvent(event);

        log.info("Product created and events published: {}", savedProduct);
        return savedProduct;
    }

    /**
     * Retrieves a product by its ID.
     * Demonstrates Redis caching using @Cacheable.
     *
     * @param id The ID of the product to retrieve.
     * @return An Optional containing the product if found, otherwise empty.
     */
    @Cacheable(value = "products", key = "#id")
    @CircuitBreaker(name = "backendA", fallbackMethod = "fallbackGetProductById")
    public Optional<Product> getProductById(Long id) {
        log.info("Fetching product by ID: {} from database (or cache miss)", id);
        // Simulate a delay or potential failure for circuit breaker demonstration
        if (id % 2 == 0) { // Simulate failure for even IDs
            // throw new RuntimeException("Simulated database error for even ID: " + id);
        }
        return productRepository.findById(id);
    }

    /**
     * Fallback method for getProductById in case of CircuitBreaker open or exception.
     *
     * @param id The ID of the product.
     * @param t The throwable that caused the fallback.
     * @return An empty Optional, indicating the product could not be retrieved.
     */
    public Optional<Product> fallbackGetProductById(Long id, Throwable t) {
        log.warn("Fallback for getProductById triggered for ID: {}. Reason: {}", id, t.getMessage());
        return Optional.empty();
    }

    /**
     * Retrieves all products.
     *
     * @return A list of all products.
     */
    public List<Product> getAllProducts() {
        log.info("Fetching all products from database.");
        return productRepository.findAll();
    }

    /**
     * Updates an existing product.
     * Demonstrates transactional operation and Redis cache update using @CachePut.
     *
     * @param id The ID of the product to update.
     * @param productDetails The new product details.
     * @return An Optional containing the updated product if found, otherwise empty.
     */
    @Transactional
    @CachePut(value = "products", key = "#id")
    public Optional<Product> updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id).map(product -> {
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setQuantity(productDetails.getQuantity());
            Product updatedProduct = productRepository.save(product);

            // Create event DTO
            ProductEventDto event = new ProductEventDto(
                "UPDATE",
                updatedProduct.getId(),
                updatedProduct.getName(),
                updatedProduct.getDescription(),
                updatedProduct.getPrice(),
                updatedProduct.getQuantity()
            );

            // Publish to both Kafka and Redis
            kafkaProducerService.sendProductEvent("product-events", event);
            redisPublisherService.publishProductEvent(event);

            log.info("Product updated and events published: {}", updatedProduct);
            return updatedProduct;
        });
    }

    /**
     * Deletes a product by its ID.
     * Demonstrates transactional operation and Redis cache eviction using @CacheEvict.
     *
     * @param id The ID of the product to delete.
     */
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        // Get product details before deletion for event
        Optional<Product> productOpt = productRepository.findById(id);

        productRepository.deleteById(id);

        // Create and publish event to both Kafka and Redis
        ProductEventDto event;
        if (productOpt.isPresent()) {
            Product deletedProduct = productOpt.get();
            event = new ProductEventDto(
                "DELETE",
                deletedProduct.getId(),
                deletedProduct.getName(),
                deletedProduct.getDescription(),
                deletedProduct.getPrice(),
                deletedProduct.getQuantity()
            );
        } else {
            // If product not found, send minimal event
            event = new ProductEventDto(
                "DELETE",
                id,
                null,
                null,
                null,
                null
            );
        }

        // Publish to both Kafka and Redis
        kafkaProducerService.sendProductEvent("product-events", event);
        redisPublisherService.publishProductEvent(event);
        log.info("Product deleted and events published for ID: {}", id);
    }

    /**
     * Simulates a potentially failing operation that will be retried.
     * Demonstrates Spring Retry functionality.
     *
     * @param input A string input for the operation.
     * @return A success message if the operation eventually succeeds.
     * @throws RuntimeException if the operation fails after all retries.
     */
    @Retryable(
        value = { RuntimeException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public String performRetriableOperation(String input) {
        log.info("Attempting retriable operation for input: {}", input);
        // Simulate a transient failure
        if (Math.random() > 0.5) {
            log.warn("Simulated failure for retriable operation: {}", input);
            throw new RuntimeException("Transient failure for " + input);
        }
        log.info("Retriable operation succeeded for input: {}", input);
        return "Operation for " + input + " succeeded!";
    }

    /**
     * Performs a long-running task asynchronously.
     * Demonstrates Spring's @Async capability.
     *
     * @param taskId The ID of the task to perform.
     * @return A CompletableFuture that will hold the result of the asynchronous task.
     */
    @Async
    public CompletableFuture<String> performAsyncTask(String taskId) {
        log.info("Starting async task: {}", taskId);
        try {
            Thread.sleep(5000); // Simulate long-running task
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
        log.info("Completed async task: {}", taskId);
        return CompletableFuture.completedFuture("Async task " + taskId + " finished successfully!");
    }
}
