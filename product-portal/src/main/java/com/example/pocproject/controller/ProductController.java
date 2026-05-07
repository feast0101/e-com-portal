package com.example.pocproject.controller;

import com.example.pocproject.model.Product;
import com.example.pocproject.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * REST Controller for managing {@link Product} resources.
 * Exposes endpoints for CRUD operations, and demonstrates retry and async capabilities.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management APIs - CRUD operations, retry patterns, and async processing")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Creates a new product.
     *
     * @param product The product details to create.
     * @return The created product with HTTP status 201.
     */
    @Operation(summary = "Create a new product", description = "Creates a new product in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Invalid product data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id The ID of the product to retrieve.
     * @return The product if found, otherwise HTTP status 404.
     */
    @Operation(summary = "Get product by ID", description = "Retrieves a single product by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "ID of the product to retrieve", required = true)
            @PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> new ResponseEntity<>(product, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Retrieves all products.
     *
     * @return A list of all products with HTTP status 200.
     */
    @Operation(summary = "Get all products", description = "Retrieves a list of all products in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of products retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /**
     * Updates an existing product.
     *
     * @param id The ID of the product to update.
     * @param productDetails The new product details.
     * @return The updated product if found, otherwise HTTP status 404.
     */
    @Operation(summary = "Update a product", description = "Updates an existing product by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "400", description = "Invalid product data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "ID of the product to update", required = true)
            @PathVariable Long id,
            @Valid @RequestBody Product productDetails) {
        return productService.updateProduct(id, productDetails)
                .map(product -> new ResponseEntity<>(product, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id The ID of the product to delete.
     * @return HTTP status 204 if deleted, otherwise 404.
     */
    @Operation(summary = "Delete a product", description = "Deletes a product by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID of the product to delete", required = true)
            @PathVariable Long id) {
        if (productService.getProductById(id).isPresent()) {
            productService.deleteProduct(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Demonstrates a retriable operation.
     *
     * @param input The input string for the retriable operation.
     * @return A message indicating the success or failure of the retriable operation.
     */
    @Operation(summary = "Retry pattern demo",
               description = "Demonstrates Spring Retry mechanism with automatic retry on failures. No authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation succeeded after retries"),
            @ApiResponse(responseCode = "500", description = "Operation failed after all retry attempts")
    })
    @GetMapping("/retry-demo")
    public ResponseEntity<String> retryDemo(
            @Parameter(description = "Input string for the retriable operation", required = true)
            @RequestParam String input) {
        try {
            String result = productService.performRetriableOperation(input);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Retriable operation failed after multiple attempts: {}", e.getMessage());
            return new ResponseEntity<>("Retriable operation failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Demonstrates an asynchronous operation.
     *
     * @param taskId The ID for the asynchronous task.
     * @return A message indicating the task has started, and the result when it completes.
     */
    @Operation(summary = "Async processing demo",
               description = "Demonstrates asynchronous task execution using CompletableFuture. No authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Async task completed successfully"),
            @ApiResponse(responseCode = "500", description = "Async task failed")
    })
    @GetMapping("/async-demo")
    public CompletableFuture<ResponseEntity<String>> asyncDemo(
            @Parameter(description = "Task ID for tracking the async operation", required = true)
            @RequestParam String taskId) {
        log.info("Controller received request for async task: {}", taskId);
        return productService.performAsyncTask(taskId)
                .thenApply(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .exceptionally(ex -> {
                    log.error("Async task {} failed: {}", taskId, ex.getMessage());
                    return new ResponseEntity<>("Async task failed: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }
}
