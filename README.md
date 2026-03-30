# Transaction Reconciliation Engine

Spring Boot service for reconciling transactions between an internal platform and an external provider. It supports:

- deterministic reconciliation by `txn_id`
- configurable timestamp tolerance
- persisted, queryable reconciliation runs in H2
- idempotent reruns for identical inputs
- minimal REST API

## Tech Stack

- Java 21
- Spring Boot 3.3
- Spring Web
- Spring Data JPA
- H2 Database
- JUnit 5 / Spring Boot Test
- Maven

## Project Structure

- `src/main/java/com/assignment/reconciliation/service` contains ingestion, hashing, reconciliation, and persistence orchestration
- `src/main/java/com/assignment/reconciliation/api` exposes the REST endpoints
- `src/main/java/com/assignment/reconciliation/persistence` contains the H2 entities and repositories
- `src/main/resources/data` contains bundled sample input datasets
- `data` is created locally to store the file-based H2 database
- `sample-output.json` shows an example result payload

## Reconciliation Approach

The reconciliation engine loads both datasets, validates them, normalizes transaction values, and indexes them by `txn_id` using hash maps. This gives near linear-time comparison across both datasets and keeps the approach easy to explain in an interview.

Each internal transaction is matched against the provider dataset and categorized into one of:

- `matched`
- `amount_mismatch`
- `status_mismatch`
- `missing_in_provider`
- `missing_in_internal`

### Comparison Rules

- `txn_id` is the primary join key
- `amount` is compared using `BigDecimal`
- `status` is trimmed and normalized to uppercase before comparison
- `timestamp` is parsed as `Instant`
- timestamp tolerance is configurable and defaults to `5 seconds`

If amount and status match but the timestamp falls outside tolerance, the record is stored in the `status_mismatch` bucket with reason `TIMESTAMP_OUTSIDE_TOLERANCE`. This keeps the required output categories stable while still preserving the timestamp-specific discrepancy.

### Validation Rules

- duplicate `txn_id` values are rejected
- null or missing `txn_id`, `amount`, `status`, or `timestamp` are rejected
- malformed timestamps are rejected
- unreadable input files return explicit validation errors

## Idempotency Strategy

Each run computes:

- a normalized hash of the internal dataset
- a normalized hash of the provider dataset
- an idempotency key derived from both hashes plus the configured tolerance

If the same effective input is submitted again, the service returns the previously stored result instead of inserting a duplicate reconciliation job.

## Storage Design

Runs are stored in H2 with:

- job metadata: job id, idempotency key, run time, hashes, tolerance, and summary counts
- result rows: category, transaction id, internal/provider values, timestamps, timestamp drift, and mismatch reason

This is intentionally simple for the assignment while remaining easy to migrate later to Postgres or another relational database.

## Running Locally

### Prerequisites

- Java 21 installed and available on PATH
- Maven installed and available on PATH

### Start the application

```bash
mvn clean spring-boot:run
```

The application starts on `http://localhost:8080` and automatically creates the H2 database files inside the local `data` directory.

### API endpoints

- `POST /api/reconciliations/run`
- `GET /api/reconciliations/{jobId}`
- `GET /api/reconciliations/{jobId}/summary`

### Run the reconciliation API with sample data

If you call the run endpoint without file paths, the application falls back to the bundled sample datasets.

```bash
curl -X POST "http://localhost:8080/api/reconciliations/run" \
  -H "Content-Type: application/json" \
  -d '{"use_sample_data":true}'
```

You can also send an empty body:

```bash
curl -X POST "http://localhost:8080/api/reconciliations/run"
```

### Run the reconciliation API with custom files

```bash
curl -X POST "http://localhost:8080/api/reconciliations/run" \
  -H "Content-Type: application/json" \
  -d '{
    "internal_file_path":"/absolute/path/to/internal.json",
    "provider_file_path":"/absolute/path/to/provider.json"
  }'
```

The response contains `summary.job_id`. Use that value in the next two API calls.

### Get the full reconciliation result by job id

```bash
curl "http://localhost:8080/api/reconciliations/<jobId>"
```

### Get the reconciliation summary by job id

```bash
curl "http://localhost:8080/api/reconciliations/<jobId>/summary"
```

### Access the H2 database

Open `http://localhost:8080/h2-console` in your browser and connect with:

- JDBC URL: `jdbc:h2:file:./data/reconciliation-db;AUTO_SERVER=TRUE`
- Username: `sa`
- Password: leave blank

The database stores reconciliation data in:

- `reconciliation_jobs`
- `reconciliation_results`

The physical H2 files are created under the repo-local `data` directory.

## Sample Input

Bundled sample datasets live at:

- `src/main/resources/data/internal-transactions.json`
- `src/main/resources/data/provider-transactions.json`

Sample output is included at:

- `sample-output.json`

## Running Tests

```bash
mvn test
```

Test coverage includes:

- matching and mismatch behavior
- timestamp tolerance behavior
- duplicate and malformed input validation
- API execution and summary retrieval
- idempotent reruns returning the same stored job

## Design Discussion

### Reprocessing

Reprocessing is safe because the idempotency key prevents duplicate results for the same normalized input. If upstream systems resend identical datasets, the existing stored result is reused.

### Batch vs Real-Time

This submission uses a batch-style reconciliation flow because it is faster to deliver and easier to test for a take-home assignment. In production, the reconciliation engine can stay the same while ingestion moves to event-driven or streaming inputs.

### Scaling to 1 Million Transactions Per Day

- keep matching logic map-based for linear scans
- partition jobs and results by reconciliation date
- move from H2 to Postgres or another production database
- process files in chunks or stream inputs if memory becomes a concern
- run reconciliation asynchronously behind a queue if the API needs to stay low-latency

## Tradeoffs

- H2 keeps setup easy, but it is not intended as the final production database
- the REST API is intentionally minimal because the reconciliation engine is the core deliverable
- timestamp-only discrepancies are represented inside the existing mismatch category rather than introducing a new output bucket
