package com.example.producteventservice.controller;

import com.example.producteventservice.model.ProductEventEntity;
import com.example.producteventservice.service.ProductEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for querying stored product events. */
@RestController
@RequestMapping("/api/events")
@Tag(name = "Product Events", description = "Query product transaction events captured from Kafka")
public class ProductEventController {

  private final ProductEventService productEventService;

  public ProductEventController(ProductEventService productEventService) {
    this.productEventService = productEventService;
  }

  /** Get all product events with pagination and sorting. */
  @Operation(
      summary = "Get all events with pagination",
      description =
          "Retrieves all product transaction events with pagination and sorting by most recent"
              + " timestamp")
  @ApiResponses(
      value = {@ApiResponse(responseCode = "200", description = "Events retrieved successfully")})
  @GetMapping("/paginated")
  public ResponseEntity<Page<ProductEventEntity>> getAllEventsPaginated(
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size (number of records per page)", example = "10")
          @RequestParam(defaultValue = "10")
          int size,
      @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
          @RequestParam(defaultValue = "DESC")
          String sortDirection) {

    Sort.Direction direction =
        sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;

    // Sort by receivedTimestamp (most recent first by default)
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "receivedTimestamp"));

    Page<ProductEventEntity> events = productEventService.getAllEvents(pageable);
    return ResponseEntity.ok(events);
  }

  /** Get all product events (non-paginated - use with caution for large datasets). */
  @Operation(
      summary = "Get all events",
      description = "Retrieves all product transaction events from the database (non-paginated)")
  @ApiResponses(
      value = {@ApiResponse(responseCode = "200", description = "Events retrieved successfully")})
  @GetMapping
  public ResponseEntity<List<ProductEventEntity>> getAllEvents() {
    return ResponseEntity.ok(productEventService.getAllEvents());
  }

  /** Get recent events (last 100). */
  @Operation(
      summary = "Get recent events",
      description = "Retrieves the most recent 100 product events")
  @ApiResponse(responseCode = "200", description = "Recent events retrieved successfully")
  @GetMapping("/recent")
  public ResponseEntity<List<ProductEventEntity>> getRecentEvents() {
    return ResponseEntity.ok(productEventService.getRecentEvents());
  }

  /** Get events by product ID with pagination. */
  @Operation(
      summary = "Get events by product ID (paginated)",
      description = "Retrieves all events for a specific product with pagination and sorting")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Events found"),
        @ApiResponse(responseCode = "404", description = "No events found for this product")
      })
  @GetMapping("/product/{productId}/paginated")
  public ResponseEntity<Page<ProductEventEntity>> getEventsByProductIdPaginated(
      @Parameter(description = "Product ID to filter events", required = true) @PathVariable
          Long productId,
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10")
          int size,
      @Parameter(description = "Sort direction", example = "DESC")
          @RequestParam(defaultValue = "DESC")
          String sortDirection) {

    Sort.Direction direction =
        sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "receivedTimestamp"));

    Page<ProductEventEntity> events = productEventService.getEventsByProductId(productId, pageable);
    return ResponseEntity.ok(events);
  }

  /** Get events by product ID (non-paginated). */
  @Operation(
      summary = "Get events by product ID",
      description = "Retrieves all events for a specific product")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Events found"),
        @ApiResponse(responseCode = "404", description = "No events found for this product")
      })
  @GetMapping("/product/{productId}")
  public ResponseEntity<List<ProductEventEntity>> getEventsByProductId(
      @Parameter(description = "Product ID to filter events", required = true) @PathVariable
          Long productId) {
    List<ProductEventEntity> events = productEventService.getEventsByProductId(productId);
    return ResponseEntity.ok(events);
  }

  /** Get events by type with pagination. */
  @Operation(
      summary = "Get events by type (paginated)",
      description = "Retrieves events filtered by type (CREATE, UPDATE, DELETE) with pagination")
  @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
  @GetMapping("/type/{eventType}/paginated")
  public ResponseEntity<Page<ProductEventEntity>> getEventsByTypePaginated(
      @Parameter(description = "Event type (CREATE, UPDATE, DELETE)", required = true) @PathVariable
          String eventType,
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10")
          int size,
      @Parameter(description = "Sort direction", example = "DESC")
          @RequestParam(defaultValue = "DESC")
          String sortDirection) {

    Sort.Direction direction =
        sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "receivedTimestamp"));

    return ResponseEntity.ok(productEventService.getEventsByType(eventType, pageable));
  }

  /** Get events by type (CREATE, UPDATE, DELETE) - non-paginated. */
  @Operation(
      summary = "Get events by type",
      description = "Retrieves events filtered by type (CREATE, UPDATE, DELETE)")
  @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
  @GetMapping("/type/{eventType}")
  public ResponseEntity<List<ProductEventEntity>> getEventsByType(
      @Parameter(description = "Event type (CREATE, UPDATE, DELETE)", required = true) @PathVariable
          String eventType) {
    return ResponseEntity.ok(productEventService.getEventsByType(eventType.toUpperCase()));
  }

  /** Get event statistics. */
  @Operation(
      summary = "Get event statistics",
      description = "Returns statistics about captured events")
  @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> getEventStats() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalEvents", productEventService.getTotalEventCount());
    stats.put("createEvents", productEventService.getEventsByType("CREATE").size());
    stats.put("updateEvents", productEventService.getEventsByType("UPDATE").size());
    stats.put("deleteEvents", productEventService.getEventsByType("DELETE").size());
    stats.put("unknownEvents", productEventService.getEventsByType("UNKNOWN").size());

    return ResponseEntity.ok(stats);
  }
}
