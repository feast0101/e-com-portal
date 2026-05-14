package com.example.pocproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Welcome controller to provide basic API information. */
@RestController
@Tag(name = "Welcome", description = "API information and available endpoints")
public class WelcomeController {

  @Operation(
      summary = "API Welcome",
      description = "Returns basic information about the API and available endpoints")
  @ApiResponse(responseCode = "200", description = "API information retrieved successfully")
  @GetMapping("/")
  public Map<String, Object> welcome() {
    Map<String, Object> response = new HashMap<>();
    response.put("application", "POC Project - Spring Boot Application");
    response.put("status", "running");
    response.put("version", "0.0.1-SNAPSHOT");

    Map<String, String> endpoints = new HashMap<>();
    endpoints.put("Swagger UI", "http://localhost:8080/swagger-ui.html");
    endpoints.put("OpenAPI Docs", "http://localhost:8080/v3/api-docs");
    endpoints.put("H2 Console", "http://localhost:8080/h2-console");
    endpoints.put("Health Check", "http://localhost:8080/actuator/health");
    endpoints.put("API Documentation", "All API endpoints are prefixed with /api");
    endpoints.put("Authentication", "POST http://localhost:8080/api/auth/signin");
    endpoints.put("Registration", "POST http://localhost:8080/api/auth/signup");
    endpoints.put("Products API", "GET http://localhost:8080/api/products");
    endpoints.put("Retry Demo", "GET http://localhost:8080/api/products/retry-demo");
    endpoints.put("Async Demo", "GET http://localhost:8080/api/products/async-demo?taskId=test");

    response.put("endpoints", endpoints);

    Map<String, String> authentication = new HashMap<>();
    authentication.put("note", "Most endpoints require JWT authentication");
    authentication.put("header", "Authorization: Bearer <your-jwt-token>");
    authentication.put("login", "Use /api/auth/signin to get a token");

    response.put("authentication", authentication);

    return response;
  }
}
