# User Analytics + Semantic Search Backend

A Spring Boot 3 / Java 21 backend that tracks user behavioral events, computes
analytics over them, and supports lightweight embedding-based "semantic"
search and user-similarity lookups — built to be interview-ready and to
demonstrate clean, layered architecture.

> **Note on AI:** no external AI API is called. A deterministic, in-process
> `FakeEmbeddingService` stands in for a real embedding model. It implements
> the same `EmbeddingService` interface that an `OpenAIEmbeddingService` or
> `HuggingFaceEmbeddingService` would, so swapping in a real provider later
> requires zero changes to controllers, services, or repositories.

---

## 1. Project Overview

| Capability | Endpoint |
|---|---|
| Track a user event | `POST /track` |
| Aggregate analytics with filters | `GET /analytics` |
| Semantic search over events | `GET /search` |
| Find behaviorally similar users | `GET /similar-users` |
| Health check | `GET /actuator/health` |
| API docs | `GET /swagger-ui.html` |

Every successful response is wrapped as:

```json
{ "success": true, "message": "...", "data": { } }
```

Every error response has the same shape:

```json
{ "timestamp": "...", "status": 400, "message": "...", "path": "/track" }
```

---

## 2. Architecture

```
controller → service (interface) → service.impl → repository → database
```

- **controller** — thin REST layer; validates input via Bean Validation, delegates to services, never contains business logic.
- **service / service.impl** — interfaces + implementations (DIP). `EmbeddingService` is the key abstraction that makes the AI provider swappable.
- **repository** — Spring Data JPA repositories with optimized JPQL aggregate queries (no N+1 patterns, filtering pushed into SQL).
- **entity** — JPA entities (`User`, `Event`, `Embedding`).
- **dto** — request/response payloads, decoupled from entities.
- **mapper** — entity ↔ DTO translation (`EventMapper`).
- **util** — `VectorUtils` (cosine similarity from scratch) and `JsonVectorConverter`.
- **exception / GlobalExceptionHandler** — consistent error contract across the API.
- **config** — Swagger/OpenAPI configuration.
- **constants** — `AppConstants` for magic numbers/strings.

### Architecture diagram

```
                ┌─────────────┐
 Client  ─────▶ │ Controllers │  (Tracking / Analytics / Search / SimilarUsers)
                └──────┬──────┘
                       │  DTOs (validated)
                ┌──────▼──────┐
                │  Services   │  (interfaces + impl, business logic, logging)
                └──────┬──────┘
            ┌──────────┼───────────┐
     ┌──────▼─────┐ ┌──▼───────┐ ┌─▼────────────┐
     │ UserRepo   │ │ EventRepo│ │ EmbeddingRepo│
     └──────┬─────┘ └──┬───────┘ └─┬────────────┘
            └──────────┼───────────┘
                        ▼
                   MySQL Database
                 (users / events / embeddings)
```

### Folder structure

```
user-analytics-system/
├── src/main/java/com/example/analytics/
│   ├── controller/        TrackingController, AnalyticsController, SearchController, SimilarUsersController
│   ├── service/            interfaces (EmbeddingService, TrackingService, AnalyticsService, SearchService, SimilarUsersService)
│   ├── service/impl/       FakeEmbeddingService, TrackingServiceImpl, AnalyticsServiceImpl, SearchServiceImpl, SimilarUsersServiceImpl
│   ├── repository/        UserRepository, EventRepository, EmbeddingRepository
│   ├── entity/             User, Event, Embedding
│   ├── dto/                 TrackEventRequest, ApiResponse, EventResponse, AnalyticsResponse, SearchResultResponse, SimilarUserResponse
│   ├── mapper/              EventMapper
│   ├── util/                 VectorUtils, JsonVectorConverter
│   ├── config/               OpenApiConfig
│   ├── exception/           GlobalExceptionHandler, BadRequestException, EntityNotFoundException, InternalServerException, ErrorResponse
│   └── constants/           AppConstants
├── src/main/resources/
│   ├── application.yml      mysql / dev (H2) profiles
│   ├── schema.sql            DDL for users / events / embeddings
│   └── data.sql               20 users, 200 events, 200 embeddings
├── src/test/java/...         JUnit 5 + Mockito tests
├── postman_collection.json
├── Dockerfile / docker-compose.yml
└── pom.xml
```

---

## 3. Database Design (ER Diagram)

```
┌────────────────┐        ┌─────────────────────┐        ┌────────────────────┐
│ users           │        │ events                │        │ embeddings           │
├────────────────┤        ├─────────────────────┤        ├────────────────────┤
│ id (PK)         │        │ id (PK)               │        │ id (PK)              │
│ user_id (UQ)    │◀──────▶│ user_id (FK*, string) │───────▶│ event_id (FK)        │
│ created_at      │  1   N │ event                  │  1   1 │ vector (JSON)        │
└────────────────┘        │ metadata (JSON)        │        │ model_name           │
                            │ timestamp              │        │ created_at           │
                            │ embedding_id (FK)      │        └────────────────────┘
                            │ created_at             │
                            └─────────────────────┘
```

`events.user_id` is a logical reference to `users.user_id` (string business
key, not a hard FK) so events can be tracked even if user creation and event
insertion race — `TrackingServiceImpl` creates the user record first within
the same transaction. `events.embedding_id` and `embeddings.event_id` form a
1:1 link between an event and its generated vector.

---

## 4. Setup Instructions

### Prerequisites
- Java 21
- Maven 3.8+
- MySQL 8+ (or Docker)

### MySQL configuration

```bash
mysql -u root -p < src/main/resources/schema.sql
mysql -u root -p < src/main/resources/data.sql
```

Update credentials in `application.yml` under the `mysql` profile if they
differ from `root`/`root`.

### Running the application

```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Or, for a quick spin without MySQL (in-memory H2):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker

```bash
docker compose up --build
```

This starts MySQL (pre-seeded with `schema.sql`/`data.sql`) and the app
together.

### Swagger UI

Once running: **http://localhost:8080/swagger-ui.html**

### Health check

**http://localhost:8080/actuator/health**

---

## 5. API Documentation & Sample Requests

### `POST /track`

```json
{
  "userId": "123",
  "event": "user viewed pricing page",
  "metadata": { "page": "/pricing" },
  "timestamp": "2026-01-01T12:00:00"
}
```

Response `201 Created`:

```json
{
  "success": true,
  "message": "Event tracked successfully",
  "data": {
    "id": 201,
    "userId": "123",
    "event": "user viewed pricing page",
    "metadata": "{\"page\":\"/pricing\"}",
    "timestamp": "2026-01-01T12:00:00",
    "embeddingId": 201
  }
}
```

### `GET /analytics`, `/analytics?event=login`, `/analytics?from=2026-01-01&to=2026-01-31`

Response `200 OK`:

```json
{
  "success": true,
  "message": "Analytics fetched successfully",
  "data": {
    "totalEvents": 200,
    "totalUsers": 20,
    "eventsPerUser": { "user001": 12, "user002": 9, "...": "..." },
    "topActiveUsers": [{ "userId": "user001", "totalEvents": 12 }],
    "eventCounts": { "user logged in": 30, "user viewed pricing page": 25 },
    "latestEvents": [ { "id": 200, "userId": "user020", "event": "...", "timestamp": "..." } ]
  }
}
```

### `GET /search?query=user visited pricing&topK=5`

```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": [
    { "eventId": 1, "userId": "user001", "event": "user viewed pricing page", "score": 0.87 }
  ]
}
```

### `GET /similar-users?userId=user001&topK=5`

```json
{
  "success": true,
  "message": "Similar users fetched successfully",
  "data": [
    { "userId": "user005", "similarityScore": 0.91, "totalEvents": 11 }
  ]
}
```

---

## 6. Embedding Generation (design)

`EmbeddingService` is the abstraction; `FakeEmbeddingService` is the current
implementation. It hashes each lowercased word into one of 128 buckets
(feature hashing / "hashing trick" — the same idea classic bag-of-words NLP
pipelines use before neural embeddings), increments that bucket, then
L2-normalizes the resulting vector. Phrases sharing vocabulary land closer
together in vector space, giving a reasonable, fully deterministic stand-in
for a real semantic embedding — and it requires no network calls, API keys,
or GPU.

Swapping to a real provider later is a one-class change:

```java
@Service
@Primary
public class OpenAIEmbeddingService implements EmbeddingService {
    // call OpenAI / Hugging Face here, same method signature
}
```

No controller, service, or repository code changes.

---

## 7. Similarity Search (`VectorUtils`)

Implemented from scratch (no linear-algebra library) in
`util/VectorUtils.java`:

- `dotProduct(a, b)` — sum of element-wise products.
- `magnitude(v)` — Euclidean length, `sqrt(sum(x_i^2))`.
- `normalizeVector(v)` — scales a vector to unit length.
- `cosineSimilarity(a, b)` — `(A·B) / (||A|| * ||B||)`, the standard
  similarity metric for comparing embedding directions regardless of scale.

`/search` embeds the query, then brute-force scores every stored embedding
and returns the top-K by score (descending).

`/similar-users` builds a **centroid** embedding per user (the average of
all their event embeddings) and ranks other users by cosine similarity of
centroids against the target user.

---

## 8. Validation & Error Handling

- Bean Validation (`@NotBlank`) on `TrackEventRequest`.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) handles
  `MethodArgumentNotValidException`, `BadRequestException`,
  `EntityNotFoundException`, `InternalServerException`, and a catch-all,
  always returning the same `ErrorResponse` shape.

---

## 9. Logging

SLF4J (`@Slf4j`) logs: incoming requests at each controller, event
tracking/user-creation in `TrackingServiceImpl`, and execution time for
`/search` and `/analytics` (`System.currentTimeMillis()` deltas), plus
warnings/errors in the exception handler.

---

## 10. Testing

```bash
mvn test
```

Covers:
- `VectorUtilsTest` — cosine similarity, normalization, dot product correctness.
- `FakeEmbeddingServiceTest` — dimensionality, determinism, and that related
  phrases score higher than unrelated ones.
- `AnalyticsServiceImplTest` — aggregate totals wired correctly via Mockito.
- `SearchServiceImplTest` — blank-query rejection, empty-result handling,
  and correct top-result ranking.
- `TrackingServiceImplTest` (Track API) — new-user creation path vs.
  existing-user path, embedding persistence.

> **Build note:** this sandbox environment could not reach Maven Central to
> run a full `mvn test`/`mvn package` (outbound network is restricted to a
> small allowlist that doesn't include `repo.maven.apache.org`). The code
> was written and reviewed carefully for correctness and compiles against
> standard Spring Boot 3.3 / Java 21 APIs, but please run `mvn clean test`
> in an environment with normal internet access to get a green build
> confirmation before relying on it.

---

## 11. Postman Collection

See `postman_collection.json` — import directly into Postman. Includes all
five endpoints with example bodies/params and a `baseUrl` variable
(`http://localhost:8080`).

---

## 12. Scalability Discussion

**Millions of events.** The `events` table would be partitioned (e.g. by
`timestamp` range, monthly) so old partitions can be archived or pruned
independently, keeping hot-path queries fast. Write throughput would move
from synchronous HTTP-request-triggered inserts to an async pipeline.

**Async ingestion (Kafka/RabbitMQ).** `POST /track` would publish to a
`user-events` Kafka topic (or RabbitMQ queue) instead of writing
synchronously; a consumer group persists events, generates embeddings, and
writes to storage. This decouples ingestion latency from embedding
generation latency and absorbs traffic spikes via the queue.

**Vector database.** The `embeddings` JSON column is a teaching
simplification. At scale, vectors would live in a purpose-built ANN index —
Pinecone, Milvus, or Weaviate (managed/self-hosted vector DBs supporting
HNSW/IVF indexes) or `pgvector` if staying on Postgres — turning `/search`
from an O(n) brute-force scan into O(log n)-ish approximate nearest-neighbor
lookups.

**Caching.** Redis would cache `/analytics` aggregate results (TTL'd, e.g.
60s) since dashboards are read-heavy and totals don't need millisecond
freshness; cache keys would include the filter params (`event`, `from`,
`to`).

**Search at scale.** For full-text/event-name filtering alongside vector
search, Elasticsearch or OpenSearch would index event metadata, optionally
combined with a vector plugin (k-NN) for hybrid lexical + semantic ranking.

**Horizontal scaling.** The Spring Boot service is stateless (no in-memory
session state), so it scales horizontally behind a load balancer; MySQL
would scale via read replicas for analytics queries, with writes going to a
primary.

**Indexing strategy.** Composite indexes on `(user_id, timestamp)` and
`(event, timestamp)` support the existing analytics filters; a covering
index strategy would be evaluated once query patterns from real usage are
known.

**Connection pooling.** HikariCP (Spring Boot default) is already
configured with bounded pool sizes; pool size would be tuned against DB
`max_connections` and the number of app instances.

**Database optimization.** Periodic `ANALYZE`/index maintenance, query
plan review via `EXPLAIN`, and moving aggregate-heavy analytics to a
read-replica or a separate OLAP store (e.g. ClickHouse) if dashboard load
grows significantly.

---

## 13. Trade-offs

| Decision | Trade-off |
|---|---|
| Fake hashing-based embeddings instead of a real model | Zero cost/latency/API keys, fully deterministic and testable, but lower semantic fidelity than a real transformer embedding. |
| Vectors stored as JSON in MySQL | Simple, no extra infra, but `/search` is O(n) brute-force rather than using an ANN index. |
| `events.user_id` as a string business key (no hard FK to `users.id`) | Decouples insert ordering, but loses referential-integrity enforcement at the DB level (mitigated by always creating the user first in the same transaction). |
| Synchronous embedding generation inside `/track` | Simpler code path and consistent reads immediately after tracking, but adds latency to the write path versus an async queue-based design. |
| `ddl-auto: validate` for MySQL profile | Forces explicit `schema.sql`/migrations (safer for production), but requires running the SQL scripts manually before first boot. |

---

## 14. Future Improvements

- Swap `FakeEmbeddingService` for a real provider (OpenAI/Hugging Face) behind a feature flag.
- Move to a dedicated vector database for ANN search at scale.
- Add Kafka-based async event ingestion.
- Add Redis caching for `/analytics`.
- Add pagination/sorting parameters to `/analytics` and `/search`.
- Add JWT authentication + RBAC for multi-tenant deployments.
- Add rate limiting (e.g. Bucket4j) on `/track`.
- MapStruct for compile-time-generated mappers as the DTO surface grows.

---

## 15. Interview Talking Points (design rationale)

- **Why an interface for embeddings?** Dependency Inversion — business logic
  (`TrackingServiceImpl`, `SearchServiceImpl`) depends on the `EmbeddingService`
  abstraction, not a concrete provider, so swapping fake → real is a config
  change, not a refactor.
- **Why hand-roll cosine similarity?** Demonstrates the underlying math
  rather than hiding it behind a library, and avoids pulling in a heavy
  linear-algebra dependency for a 128-dimension vector.
- **Why centroid vectors for `/similar-users`?** Averaging all of a user's
  event embeddings collapses their entire behavioral history into one
  comparable point, which is the simplest reasonable way to compare "user
  A's behavior" to "user B's behavior" without a more complex sequence
  model.
- **Why JSON columns instead of a vector type?** Keeps the schema portable
  across MySQL versions/engines for this assignment; the README explicitly
  calls out the real-world replacement (pgvector/Pinecone/etc.).
- **Why wrap every response in `ApiResponse<T>`?** Gives API consumers one
  consistent contract to parse (`success`/`message`/`data`) regardless of
  endpoint, which simplifies client-side error handling.
