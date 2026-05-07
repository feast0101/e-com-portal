# Spring Boot POC Project

This project is a Proof of Concept (POC) demonstrating various features of Spring Boot, including:

*   **JPA & Spring Data:** Persistence with H2 in-memory database.
*   **Spring Security & JWT:** User authentication and authorization using JSON Web Tokens.
*   **Role-Based Access Control (RBAC):** Admin-only endpoints with `@PreAuthorize` annotations.
*   **Redis Cache:** Caching mechanism for improved performance.
*   **Kafka:** Asynchronous messaging for event-driven architecture.
*   **Resilient4j:** Circuit Breaker pattern for fault tolerance.
*   **Spring Retry:** Automatic retries for transient failures.
*   **Concurrency:** Asynchronous method execution using `@Async`.
*   **Transaction Management:** Declarative transaction handling.
*   **Validation:** Request body validation using `jakarta.validation`.
*   **Actuator:** Production-ready features for monitoring and management.
*   **Swagger/OpenAPI:** Interactive API documentation with Swagger UI.

## Table of Contents

- [Spring Boot POC Project](#spring-boot-poc-project)
  - [Table of Contents](#table-of-contents)
  - [Prerequisites](#prerequisites)
  - [Getting Started](#getting-started)
    - [Build the Project](#build-the-project)
    - [Run the Application](#run-the-application)
  - [External Services Setup](#external-services-setup)
    - [Redis](#redis)
    - [Kafka](#kafka)
  - [API Documentation](#api-documentation)
    - [Swagger UI](#swagger-ui)
    - [How to Use Swagger UI with JWT Authentication:](#how-to-use-swagger-ui-with-jwt-authentication)
  - [API Endpoints](#api-endpoints)
    - [Authentication](#authentication)
    - [Product Management (Requires Authentication)](#product-management-requires-authentication)
    - [User Management (Admin Only - Requires ADMIN Role)](#user-management-admin-only---requires-admin-role)
    - [Resilience \& Concurrency Demos (No Authentication Required)](#resilience--concurrency-demos-no-authentication-required)
  - [H2 Console](#h2-console)
  - [Actuator Endpoints](#actuator-endpoints)
  - [Project Structure](#project-structure)
  - [Javadoc](#javadoc)

## Prerequisites

Before you begin, ensure you have the following installed:

*   **Java 17 or higher**
*   **Maven 3.6.0 or higher**
*   **Docker** (recommended for running Redis and Kafka easily) or direct installations of:
    *   **Redis Server**
    *   **Apache Kafka** (and Zookeeper)

## Getting Started

### Build the Project

Navigate to the `poc-project` directory and build the project using Maven:

```bash
cd poc-project
mvn clean install
```

### Run the Application

After building, you can run the Spring Boot application:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## External Services Setup

### Redis

Redis is used for caching. You can run Redis using Docker:

```bash
docker run -d --name poc-redis -p 6379:6379 redis/redis-stack-server:latest
```

### Kafka

Kafka is used for asynchronous messaging. You can run Kafka and Zookeeper using Docker Compose. Create a `docker-compose.yml` file in a separate directory (e.g., `kafka-docker`) with the following content:

```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.0.1
    hostname: zookeeper
    container_name: zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  broker:
    image: confluentinc/cp-kafka:7.0.1
    hostname: broker
    container_name: broker
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
      - "9101:9101"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://broker:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_JMX_PORT: 9101
      KAFKA_JMX_HOSTNAME: localhost
```

Then, start the services:

```bash
# For Docker Compose V2 (modern Docker installations)
docker compose up -d

# For older Docker Compose V1 installations (if needed)
# docker-compose up -d
```

## API Documentation

### Swagger UI

This project includes comprehensive API documentation using **Swagger/OpenAPI 3.0**.

**Access Swagger UI at:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

**Features:**
- 📋 Interactive API documentation for all endpoints
- 🔐 Built-in authentication support - use the "Authorize" button to add your JWT token
- 🧪 Try-it-out functionality to test APIs directly from the browser
- 📊 Request/Response schemas and examples
- 🏷️ Organized by tags: Authentication, Products, Welcome

**OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### How to Use Swagger UI with JWT Authentication:

1. First, register a user using the `POST /api/auth/signup` endpoint
2. Then, login using the `POST /api/auth/signin` endpoint to get a JWT token
3. Click the **"Authorize"** button at the top of Swagger UI
4. Enter your token in the format: `Bearer <your-jwt-token>`
5. Click "Authorize" and then "Close"
6. Now you can test all protected endpoints!

## API Endpoints

All API endpoints are prefixed with `/api`.

### Authentication

*   **Register a new user:**
    *   `POST /api/auth/signup`
    *   **Body:**
        ```json
        {
            "username": "testuser",
            "password": "password",
            "role": ["user"] // Optional, can be "admin" or "user"
        }
        ```
    *   **Example Response:** `{"message": "User registered successfully!"}`

*   **Login and get JWT token:**
    *   `POST /api/auth/signin`
    *   **Body:**
        ```json
        {
            "username": "testuser",
            "password": "password"
        }
        ```
    *   **Example Response:**
        ```json
        {
            "token": "eyJhbGciOiJIUzUxMi...",
            "type": "Bearer",
            "id": 1,
            "username": "testuser",
            "roles": ["ROLE_USER"]
        }
        ```
    *   **Note:** Use the `token` from this response in the `Authorization` header for protected endpoints (e.g., `Authorization: Bearer <YOUR_JWT_TOKEN>`).

### Product Management (Requires Authentication)

*   **Create Product:**
    *   `POST /api/products`
    *   **Headers:** `Authorization: Bearer <JWT_TOKEN>`
    *   **Body:**
        ```json
        {
            "name": "Laptop",
            "description": "Powerful laptop for work and gaming",
            "price": 1200.00,
            "quantity": 10
        }
        ```
*   **Get All Products:**
    *   `GET /api/products`
    *   **Headers:** `Authorization: Bearer <JWT_TOKEN>`
*   **Get Product by ID:**
    *   `GET /api/products/{id}`
    *   **Headers:** `Authorization: Bearer <JWT_TOKEN>`
    *   **Note:** This endpoint demonstrates Redis caching and Resilient4j Circuit Breaker. The first call will hit the DB, subsequent calls for the same ID will hit the cache (within TTL). If the service is configured to fail for certain IDs (e.g., even IDs as simulated in `ProductService`), the circuit breaker might open.
*   **Update Product:**
    *   `PUT /api/products/{id}`
    *   **Headers:** `Authorization: Bearer <JWT_TOKEN>`
    *   **Body:** (same as create, but include updated fields)
*   **Delete Product:**
    *   `DELETE /api/products/{id}`
    *   **Headers:** `Authorization: Bearer <JWT_TOKEN>`

### User Management (Admin Only - Requires ADMIN Role)

These endpoints are **only accessible to users with ADMIN role**. Attempting to access them with a regular USER role will result in a 403 Forbidden error.

*   **Get All Users:**
    *   `GET /api/admin/users`
    *   **Headers:** `Authorization: Bearer <ADMIN_JWT_TOKEN>`
    *   Returns a list of all users in the system

*   **Get User by ID:**
    *   `GET /api/admin/users/{id}`
    *   **Headers:** `Authorization: Bearer <ADMIN_JWT_TOKEN>`
    *   Returns details of a specific user

*   **Update User:**
    *   `PUT /api/admin/users/{id}`
    *   **Headers:** `Authorization: Bearer <ADMIN_JWT_TOKEN>`
    *   **Body:** (all fields optional)
        ```json
        {
            "username": "newusername",
            "password": "newpassword",
            "roles": ["ROLE_ADMIN", "ROLE_USER"]
        }
        ```

*   **Delete User:**
    *   `DELETE /api/admin/users/{id}`
    *   **Headers:** `Authorization: Bearer <ADMIN_JWT_TOKEN>`
    *   Permanently deletes a user from the system

**Testing Admin Functionality:**

1. **Create an admin user:**
   ```json
   POST /api/auth/signup
   {
       "username": "admin",
       "password": "admin123",
       "role": ["admin"]
   }
   ```

2. **Login as admin:**
   ```json
   POST /api/auth/signin
   {
       "username": "admin",
       "password": "admin123"
   }
   ```
   Copy the JWT token from the response.

3. **Use the admin token** in Swagger UI:
   - Click the "Authorize" button
   - Enter: `Bearer <admin-jwt-token>`
   - Now you can access all admin endpoints!

4. **Try with a regular user** to see the 403 Forbidden error:
   - Create a user with role "user"
   - Login and get their token
   - Try to access `/api/admin/users` - you'll get a 403 error

### Resilience & Concurrency Demos (No Authentication Required)

*   **Retry Demo:**
    *   `GET /api/products/retry-demo?input=test`
    *   This endpoint calls a method in `ProductService` that simulates transient failures and uses `@Retryable` to automatically retry the operation. You might need to call it a few times to see the retry mechanism in action.
*   **Async Demo:**
    *   `GET /api/products/async-demo?taskId=task123`
    *   This endpoint triggers an asynchronous task in `ProductService`. The controller immediately returns a `CompletableFuture` which will resolve when the async task completes (after a simulated 5-second delay).

## H2 Console

The H2 in-memory database console is available at `http://localhost:8080/h2-console`.
*   **JDBC URL:** `jdbc:h2:mem:pocdb`
*   **User Name:** `sa`
*   **Password:** `password`

## Actuator Endpoints

Spring Boot Actuator provides production-ready features. Some useful endpoints:

*   `GET http://localhost:8080/actuator`
*   `GET http://localhost:8080/actuator/health`
*   `GET http://localhost:8080/actuator/info`
*   `GET http://localhost:8080/actuator/metrics`
*   `GET http://localhost:8080/actuator/caches`
*   `GET http://localhost:8080/actuator/circuitbreakers`

## Project Structure

```
poc-project/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/pocproject/
│   │   │       ├── PocProjectApplication.java
│   │   │       ├── controller/
│   │   │       │   └── ProductController.java
│   │   │       ├── kafka/
│   │   │       │   ├── KafkaConsumerService.java
│   │   │       │   └── KafkaProducerService.java
│   │   │       ├── model/
│   │   │       │   └── Product.java
│   │   │       ├── repository/
│   │   │       │   └── ProductRepository.java
│   │   │       ├── security/
│   │   │       │   ├── config/
│   │   │       │   │   └── SecurityConfig.java
│   │   │       │   ├── controller/
│   │   │       │   │   └── AuthController.java
│   │   │       │   ├── filter/
│   │   │       │   │   └── JwtRequestFilter.java
│   │   │       │   ├── model/
│   │   │       │   │   ├── ERole.java
│   │   │       │   │   ├── Role.java
│   │   │       │   │   └── User.java
│   │   │       │   ├── payload/
│   │   │       │   │   ├── JwtResponse.java
│   │   │       │   │   ├── LoginRequest.java
│   │   │       │   │   ├── MessageResponse.java
│   │   │       │   │   └── SignupRequest.java
│   │   │       │   ├── repository/
│   │   │       │   │   ├── RoleRepository.java
│   │   │       │   │   └── UserRepository.java
│   │   │       │   └── service/
│   │   │       │       ├── JwtUtil.java
│   │   │       │       └── UserDetailsServiceImpl.java
│   │   │       ├── service/
│   │   │       │   └── ProductService.java
│   │   │       └── util/
│   │   │           └── DataLoader.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/example/pocproject/
│               └── PocProjectApplicationTests.java
├── pom.xml
└── README.md
```

## Javadoc

Javadoc comments have been added to all relevant classes and methods to explain their purpose, parameters, and return values. You can generate the Javadoc documentation by running:

```bash
mvn javadoc:javadoc
```

The generated documentation will be available in `target/site/apidocs`.
