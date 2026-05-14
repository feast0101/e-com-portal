# e-com-portal — Code Quality & Best Practices

## Project Overview

Two Spring Boot 3.2.5 / Java 17 services communicating over Kafka and Redis:

- **product-portal** (`com.example.pocproject`) — REST API, Spring Security (JWT), JPA, Redis cache, Kafka producer, Resilience4j circuit breaker, Spring Retry
- **product-event-service** (`com.example.producteventservice`) — Kafka consumer, JPA persistence of product events

Infrastructure: PostgreSQL (prod), H2 (test), Kafka + Zookeeper, Redis — all defined in `docker-compose.yml`.

---

## Architecture Rules

- **No cross-service JPA calls.** Services communicate exclusively via Kafka topics or Redis pub/sub. Never share a database between `product-portal` and `product-event-service`.
- **Layer boundaries are strict.** Controllers call services. Services call repositories, Kafka producers, and Redis. Repositories talk to the database only. No skipping layers.
- **DTOs cross boundaries; entities do not.** Never return a JPA `@Entity` directly from a controller. Use a dedicated DTO or payload class (see `ProductEventDto`, `JwtResponse`).

---

## Clean Code Standards

### Naming
- Classes: `PascalCase`, descriptive nouns (`ProductService`, `KafkaConsumerService`).
- Methods: `camelCase`, verb-first (`createProduct`, `sendProductEvent`, `findById`).
- Constants: `UPPER_SNAKE_CASE` (`PRODUCTS_CACHE`, `PRODUCT_EVENTS_TOPIC`).
- No abbreviations unless universally understood (`dto`, `id`, `jwt`).

### Comments
- Write **no comments** that restate what the code already says.
- Javadoc on public API methods is acceptable; do not put Javadoc on private methods or internal-only classes.
- Acceptable comment: a non-obvious invariant, a known framework quirk, or a security rationale (e.g., why CSRF is disabled).
- Never leave `TODO`, `FIXME`, or commented-out code in a PR.

### Method length and complexity
- Aim for methods under 30 lines. Extract private helpers if a method grows beyond that.
- Cyclomatic complexity > 5 in a single method is a refactor signal.
- Avoid deeply nested `if/else`; prefer early returns and guard clauses.

---

## Layer-by-layer Standards

### Controllers (`controller/`)
- One controller per resource (`ProductController`, `AuthController`).
- Return `ResponseEntity<T>` with explicit HTTP status codes:
  - `201 Created` for POST that creates a resource
  - `204 No Content` for DELETE
  - `404 Not Found` when a resource is absent — do not return `200` with a null body
- Annotate every endpoint with `@Operation`, `@ApiResponses`, and `@SecurityRequirement` (if protected).
- Use `@Valid` on every `@RequestBody`. Validation failures must produce `400`, not `500`.
- No business logic in controllers. If you find yourself writing an `if` that touches data, move it to the service.
- Do not call the service twice in a single handler to check-then-act (e.g., `getById` + `delete`). Make the service operation idempotent or return a result that conveys existence.

### Services (`service/`)
- Annotate write operations with `@Transactional`; read-only operations with `@Transactional(readOnly = true)`.
- Do **not** place `@Transactional` on methods that call Kafka producers — the JPA transaction and Kafka send are in different transactional systems. Publish events *after* the DB commit completes (or use an outbox pattern).
- Resilience4j `@CircuitBreaker` fallback methods must have the same return type and an added `Throwable` parameter; always log the reason in the fallback.
- `@Retryable` — only retry on genuinely transient exceptions. Never retry on `IllegalArgumentException` or validation failures.
- `@Async` methods must return `CompletableFuture<T>`. Handle `InterruptedException` by restoring the interrupt flag (`Thread.currentThread().interrupt()`).
- Wrap checked exceptions that callers cannot handle into specific unchecked exceptions (not raw `RuntimeException`).

### Repositories (`repository/`)
- Extend `JpaRepository<T, ID>` — no `EntityManager` injection unless a native query is unavoidable.
- Return `Optional<T>` for single-entity lookups; unwrap with `.orElseThrow(() -> new ResourceNotFoundException(...))`, never `.get()`.
- Use `Pageable` on any method that can return an unbounded list.
- Named parameters in JPQL: `:paramName`, never string concatenation.
- Check for N+1 queries on collections — use `@EntityGraph` or `JOIN FETCH`.

### Kafka (`kafka/`)
- Topic names go in constants, not inline strings:
  ```java
  public static final String PRODUCT_EVENTS_TOPIC = "product-events";
  ```
- Producers must set a message key when ordering matters (e.g., product ID as key).
- Consumers must handle deserialization errors explicitly — a poison pill must not block the partition.
- Use `@RetryableTopic` for at-least-once delivery with dead-letter handling; do not swallow consumer exceptions.
- DTOs sent over Kafka must be backwards-compatible — add fields with defaults; never remove or rename fields without a migration plan.

### Redis (`redis/`)
- All cache value names are namespaced: `"products"`, `"users"` — never a bare ID.
- Every cached value must have a TTL configured in `RedisConfig`. Unbounded caches cause memory exhaustion.
- `@CacheEvict` on all write operations that invalidate a cached entity.
- Objects stored in Redis must be JSON-serializable via the `RedisConfig` serializer — do not rely on Java serialization.

### Security (`security/`)
- JWT secret comes from `application.yml` / environment variable only. Never hardcode it.
- Passwords hashed with `BCryptPasswordEncoder` exclusively. Do not accept plain text or weaker hashes.
- CSRF is disabled for stateless JWT APIs — this is intentional and documented in `SecurityConfig`.
- New protected endpoints: add `@PreAuthorize("hasRole('...')")` at the method level, not just `authenticated()` in the filter chain, so role checks are co-located with the method.
- `permitAll()` rules in `SecurityFilterChain` are allow-listed explicitly. Never use a wildcard `/**` permit.
- Never log JWT tokens or raw passwords — not even at `DEBUG` level.

---

## Logging

Use SLF4J via the static logger pattern already established in the project:

```java
private static final Logger log = LoggerFactory.getLogger(MyClass.class);
```

- `log.info` — normal flow milestones (entity created, event published).
- `log.warn` — recoverable anomalies (fallback triggered, retry attempt).
- `log.error` — failures that need investigation; always include the exception: `log.error("msg", e)`.
- No `System.out.println` anywhere in production code.
- Do not log entire request/response bodies containing PII (email, password, token).

---

## Testing

| What to test | Which slice | Key annotations |
|---|---|---|
| Controller request/response | Web slice | `@WebMvcTest`, `MockMvc` |
| Service logic | Unit | JUnit 5 + Mockito |
| Repository queries | JPA slice | `@DataJpaTest`, H2 |
| Kafka consumer | Integration | `@EmbeddedKafka`, `@SpringBootTest` |
| Security rules | Web slice | `@WithMockUser`, `MockMvc` |

- Every new public service method needs at least one unit test covering the happy path and one for the failure/fallback path.
- Assertions must verify the actual value, not just `assertNotNull`. Prefer AssertJ (`assertThat(...).isEqualTo(...)`).
- No `Thread.sleep` in tests — use `Awaitility` for async assertions.
- Test method names: `methodName_condition_expectedResult` (e.g., `getProductById_whenNotFound_returnEmpty`).

---

## What Claude Should Never Do in This Project

- Introduce field injection (`@Autowired` on a field). Always use constructor injection.
- Add `@Transactional` to repository interface methods — Spring Data already manages transactions there.
- Expose JPA entities directly in REST responses.
- Catch and silently swallow exceptions without at least a `log.warn`.
- Hardcode Kafka topic names, JWT secrets, or database credentials inline.
- Add `System.out.println` or `e.printStackTrace()`.
- Return `Optional` from a controller method — unwrap it in the service or controller and return the appropriate `ResponseEntity`.
- Use `@SpringBootTest` for tests that only need `@WebMvcTest` or `@DataJpaTest` — full context startup is slow and unnecessary.
