# Product Event Service

A Spring Boot microservice that captures and stores all product transaction events from Kafka.

## Overview

This microservice listens to the `product-events` Kafka topic and automatically stores all product CRUD operations (Create, Update, Delete) in a PostgreSQL database. It provides a REST API to query the stored events for audit, analytics, and reporting purposes.

## Features

- ✅ **Kafka Consumer**: Listens to `product-events` topic
- ✅ **Event Storage**: Stores all events in PostgreSQL database
- ✅ **REST API**: Query events by product ID, type, or time range
- ✅ **Event Statistics**: Get counts and metrics of captured events
- ✅ **Swagger UI**: Interactive API documentation
- ✅ **Backward Compatible**: Handles both JSON and string messages
- ✅ **Audit Trail**: Complete history of all product transactions

## Technology Stack

- **Spring Boot** 3.2.5
- **Spring Kafka** - Kafka consumer
- **Spring Data JPA** - Database access
- **PostgreSQL** - Event storage (can use H2 for testing)
- **Swagger/OpenAPI** 3.0 - API documentation
- **Lombok** - Reduced boilerplate code

## Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose (for PostgreSQL and Kafka)

## Running the Service

### 1. Start Dependencies

From the root `poc-project` directory, start Kafka and PostgreSQL:

```bash
docker compose up -d
```

This starts:
- Kafka (localhost:9092)
- Zookeeper (localhost:2181)
- PostgreSQL (localhost:5432)

### 2. Build the Service

```bash
cd product-event-service
mvn clean install
```

### 3. Run the Service

```bash
mvn spring-boot:run
```

The service will start on **port 8081**.

## API Endpoints

### Swagger UI
Access the interactive API documentation at:
**http://localhost:8081/swagger-ui.html**

### REST Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/events` | GET | Get all events |
| `/api/events/recent` | GET | Get last 100 events |
| `/api/events/product/{id}` | GET | Get events for specific product |
| `/api/events/type/{type}` | GET | Get events by type (CREATE/UPDATE/DELETE) |
| `/api/events/stats` | GET | Get event statistics |

## Configuration

### Database Configuration

**PostgreSQL (Production):**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/product_events_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

**H2 (Development/Testing):**
Uncomment these lines in `application.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:eventdb
spring.h2.console.enabled=true
```

### Kafka Configuration

```properties
spring.kafka.bootstrap-servers=localhost:9092
kafka.topic.product-events=product-events
spring.kafka.consumer.group-id=product-event-consumer-group
```

## Testing the Service

1. **Start the main POC service** (on port 8080)
2. **Create a product** using the main service API
3. **Check captured events**:
   ```bash
   curl http://localhost:8081/api/events/recent
   ```
4. **View in Swagger UI**: http://localhost:8081/swagger-ui.html

## Database Schema

The `product_events` table stores:
- Event ID (auto-generated)
- Event Type (CREATE, UPDATE, DELETE)
- Product ID
- Product Name, Description, Price, Quantity
- Event Timestamp
- Received Timestamp
- Raw Message (for debugging)

## Monitoring

- **Health Check**: http://localhost:8081/actuator/health
- **Metrics**: http://localhost:8081/actuator/metrics
