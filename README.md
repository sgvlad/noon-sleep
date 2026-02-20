# Sleep Logger API

A REST API for logging and analyzing sleep data, built as part of the Noom fullstack interview.

## Tech Stack

- **Java 17** + Spring Boot 2.7
- **PostgreSQL 13** (via Docker)
- **Flyway** for DB migrations
- **JDBC** (`NamedParameterJdbcTemplate`) — no ORM
- **Testcontainers** for integration tests
- **Docker Compose** for local development

## Architecture

[BCE](https://bce.design/) (Boundary–Control–Entity) pattern, single component:

```
src/main/java/com/noom/interview/fullstack/sleep/
├── boundary/    ← Controllers, DTOs, exception handlers
├── control/     ← Service layer (business logic)
└── entity/      ← Domain model (SleepLog, MorningFeeling), repository
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/sleep-log` | Log last night's sleep |
| `GET`  | `/api/sleep-log/last-night` | Fetch last night's sleep data |
| `GET`  | `/api/sleep-log/averages` | Get 30-day sleep averages |

All endpoints require `X-User-Id` header (simulates user identity, no real auth).

## Running Locally

### Prerequisites
- Docker & Docker Compose

### Start the database
```bash
cd noon
docker-compose up db
```

### Run the application
```bash
cd sleep
./gradlew bootRun
```

The API will be available at `http://localhost:8080`.

### Run with Docker Compose (full stack)
```bash
cd noon
docker-compose up
```

## Running Tests

```bash
cd sleep
./gradlew test
```

Tests use Testcontainers — Docker must be running.

## Database

Migrations are managed by Flyway in `src/main/resources/db/migration/`:

- `V1.0__test_db_reachable.sql` — connectivity check
- `V2.0__create_sleep_log_table.sql` — sleep_log table

