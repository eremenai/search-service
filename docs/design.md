
# Backend Search Service – Design (Updated, JDBC-based)

## 1. Overview

This document describes the design of a backend search service that:

- Manages **clients** and their **documents**.
- Provides:
    - Search for clients by name/email/company (via email domain).
    - Semantic search for documents by content using embeddings over document chunks.
- Exposes a **single /search endpoint** that can return both client and document results.
- Uses a realistic stack: **Java (Spring Boot)**, **PostgreSQL**, **Liquibase**, **JDBC** (via Spring’s facilities), and a pluggable embedding provider.

For simplicity (per clarification from the reviewers):

- There is **no advisor-level isolation** in this implementation.
- All advisors (users of the system) are assumed to see the **same dataset**.
- **Documents are immutable**:
    - No updates and no versioning.
    - If a document with the same title already exists for a given client, the API returns a conflict.

Potential future improvements (such as per-advisor row-level security and versioning) can be added later but are out of scope for this iteration.

---

## 2. Requirements

### 2.1 Functional

- Maintain **clients**:
    - Each client has email, first/last/full name, and derived email-domain fields for search.
    - Clients are globally unique by email.

- Maintain **documents** associated with clients:
    - Each document:
        - Belongs to exactly one client.
        - Has a title and full text content.
        - Is **immutable** once created (no updates).
        - Is unique per `(client, title)`.

- Search:
    - **Client search**:
        - By email.
        - By company name via email domain (e.g. `Nevis Wealth` → `neviswealth.com`).
        - By first name, last name, or full name.
    - **Document search**:
        - Query by text (e.g. `address proof`) and find documents whose content uses related terms (e.g. contains `utility bill`).
        - Document search can be scoped to a specific client when `clientId` is provided; otherwise it searches across all documents.

- **Single aggregated search endpoint**:
    - `/search` returns both clients and documents:
        - When `clientId` is omitted: both client and document results are returned (documents across all clients).
        - When `clientId` is provided: results include clients plus documents for that client.

- API:
    - Endpoints to create/fetch clients and documents.
    - `/search` endpoint that returns a typed list of results (clients and/or documents).
    - API documentation via OpenAPI / Swagger.

### 2.2 Non-functional

- **Simplicity**:
    - No multi-tenant security in DB schema; all data is visible to all callers.
- **Extensibility**:
    - Design leaves room to add:
        - Row-level security (RLS) for per-advisor isolation.
        - Document versioning.
        - Admin views for full dataset.
- **Testability**:
    - Unit tests for domain and services.
    - Integration tests with Testcontainers for Postgres and semantic search.
- **Deployability**:
    - Docker-based local setup using docker-compose (Postgres + backend + optional embeddings service).

---

## 3. Architecture Overview

### 3.1 Components

- **search-service** (Spring Boot, Java)
    - Exposes REST API.
    - Implements domain logic for clients, documents, and search.
    - Uses Spring’s JDBC support (`JdbcTemplate` / `NamedParameterJdbcTemplate`) to interact with Postgres.
    - Integrates with an `EmbeddingProvider` to compute embeddings.

- **PostgreSQL**
    - Primary data store for clients, documents, and document chunks.
    - Provides:
        - Text search via `pg_trgm`.
        - Vector search via `pgvector`.

- **embeddings-calculation-service** (FastAPI, Python)
    - Loads a local embedding model (e.g. `BAAI/bge-small-en`).
    - Exposes an HTTP `/embed` endpoint for text → vector embeddings.
    - Used for local development and testing.
    - In production, a third-party embedding / LLM provider is used behind the same `EmbeddingProvider` interface.

### 3.2 Layers inside search-service

- **API layer** (controllers)
    - Spring MVC controllers exposing REST endpoints.
    - OpenAPI annotations for Swagger.
    - Validation of request bodies and query parameters.

- **Service layer**
    - `ClientService`, `DocumentService`, `SearchService`.
    - Implements business rules:
        - Uniqueness constraints (e.g. document title per client).
        - Mapping between DTOs and domain objects.
        - Orchestration of chunking + embeddings for documents.

- **Persistence layer**
    - Repository classes using Spring’s JDBC (`JdbcTemplate` or `NamedParameterJdbcTemplate`).
    - SQL is defined explicitly (no ORM), but encapsulated in repositories.

- **Integration layer**
    - `EmbeddingProvider` interface with:
        - Mock implementation.
        - HTTP implementation calling the embeddings service or external provider.

---

## 4. Domain Model

### 4.1 Clients

A **client**:

- Represents an end-customer in the system.
- Is globally unique by email.
- Has name fields and derived email-domain fields to support flexible search.

Fields:

- `id` (UUID)
- `email` (TEXT)
- `email_domain` (TEXT) – part after `@`, e.g. `neviswealth.com`.
- `email_domain_slug` (TEXT) – normalized domain without dots/punctuation, e.g. `neviswealth`.
- `first_name` (TEXT)
- `last_name` (TEXT)
- `full_name` (TEXT) – denormalised combined name.
- `country_of_residence` (TEXT, optional)
- Timestamp: `created_at`.

### 4.2 Documents

A **document**:

- Belongs to exactly one client.
- Is immutable (no updates, no versioning).
- Contains the full text content used for search.
- Is unique per `(client_id, title)`.

Fields:

- `id` (UUID)
- `client_id` (UUID) – FK to clients.
- `title` (TEXT, required, unique per client).
- `content` (TEXT, required).
- Optional:
- `content_hash` (e.g. SHA-256 of content, required).
    - `summary` (TEXT, optional, for cached summary if implemented).
- Timestamp: `created_at`.

### 4.3 Document chunks

Each document is split into multiple **chunks** for vector search:

- Each chunk holds:
    - A piece of the document text.
    - Its embedding vector.

Fields:

- `document_id` (UUID)
- `chunk_index` (INTEGER) – 0, 1, 2, …
- `content` (TEXT) – chunk text.
- `embedding` (VECTOR) – pgvector column.

Primary key: `(document_id, chunk_index)`.

Chunks are created only at document creation time (no updates).

---

## 5. Persistence Design

### 5.1 Extensions

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS vector;
```

Embedding vector dimension is configured to match the chosen embedding model (e.g. `VECTOR(768)`).

### 5.2 Tables

#### 5.2.1 clients

```sql
CREATE TABLE clients (
  id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  email                 TEXT NOT NULL,
  email_domain          TEXT NOT NULL,
  email_domain_slug     TEXT NOT NULL,
  first_name            TEXT NOT NULL,
  last_name             TEXT NOT NULL,
  full_name             TEXT NOT NULL,
  country_of_residence  TEXT NULL,
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (email)
);
```

Indexes:

```sql
CREATE INDEX idx_clients_email_trgm
  ON clients USING gin (email gin_trgm_ops);

CREATE INDEX idx_clients_email_domain_trgm
  ON clients USING gin (email_domain gin_trgm_ops);

CREATE INDEX idx_clients_email_domain_slug_trgm
  ON clients USING gin (email_domain_slug gin_trgm_ops);

CREATE INDEX idx_clients_full_name_trgm
  ON clients USING gin (full_name gin_trgm_ops);
```

#### 5.2.2 documents

```sql
CREATE TABLE documents (
  id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  client_id      UUID NOT NULL REFERENCES clients(id),
  title          TEXT NOT NULL,
  content        TEXT NOT NULL,
  content_hash   CHAR(64) NOT NULL,
  summary        TEXT NULL,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (client_id, title)
);
```

The unique constraint enforces:

- For a given client, there cannot be two documents with the same `title`.
- If the API attempts to create another document with the same `(client_id, title)`, it should respond with `409 Conflict`.

#### 5.2.3 document_chunks

```sql
CREATE TABLE document_chunks (
  document_id   UUID NOT NULL REFERENCES documents(id),
  chunk_index   INTEGER NOT NULL,
  content       TEXT NOT NULL,
  embedding     vector NOT NULL,
  PRIMARY KEY (document_id, chunk_index)
);
```

Vector index for ANN search:

```sql
CREATE INDEX idx_document_chunks_embedding
  ON document_chunks
  USING ivfflat (embedding vector_l2_ops)
  WITH (lists = 100);
```

No views are required for versioning, since documents are immutable.

---

## 6. Embeddings and Chunking

### 6.1 Chunking strategy

Define a `ChunkingStrategy` interface:

```java
public interface ChunkingStrategy {
    List<Chunk> chunk(String fullContent);
}
```

Default implementation: paragraph/word-based chunking:

- Split by paragraphs (`\n\n`).
- Group paragraphs into chunks up to a configurable maximum number of characters (e.g. 1000–1500).
- If a paragraph exceeds the max, split it into smaller word-based pieces.
- Optional overlap between chunks can be added later if needed.

Configuration:

- Max chunk size in characters (config property).
- Initial implementation is simple; token-based chunking can be added later via a different `ChunkingStrategy` implementation.

### 6.2 Embedding provider

Define an `EmbeddingProvider` interface:

```java
public interface EmbeddingProvider {
    float[] embed(String text);
}
```

Implementations:

1. **MockEmbeddingProvider**
    - Used for unit tests and when backend runs without external dependencies.
    - Returns deterministic pseudo-random vectors based on input.

2. **HttpEmbeddingProvider**
    - Calls the embeddings-calculation-service (FastAPI) in local dev, or a third-party embedding/LLM API in production.
    - Configured via:
        - Base URL.
        - API token (optional in local dev).
    - Sends JSON payload `{ "text": "..." }` and expects a JSON array of floats in response.

### 6.3 Document ingestion flow

On **document creation**:

1. Validate request (title, content not null).
2. Check that:
    - `client_id` exists.
    - No existing document with same `(client_id, title)` exists.
    - If such a document exists, return `409 Conflict`.
3. Insert into `documents`:
    - Generate `id`.
    - Compute `content_hash`.
4. Chunk content via `ChunkingStrategy` into `[Chunk0, Chunk1, …]`.
5. For each chunk:
    - Call `EmbeddingProvider` to compute embedding.
    - Insert into `document_chunks` with `(document_id, chunk_index, content, embedding)`.
6. Optionally, summaries can be computed on-demand and stored in `documents.summary`.

Documents are immutable: no update endpoint is provided; if an updated version is needed, a new document (with a different title) must be created.

---

## 7. Search Design

Search is exposed as a single endpoint `/search` that returns both clients and documents. Internally, client search and document search are implemented separately and combined in the response.

### 7.1 Client search

Input:

- Query string `q` (required).

Normalisation:

- `q_normalized`:
    - Lowercased, trimmed.
- `q_slug`:
    - Lowercased, spaces and punctuation removed (e.g. `Nevis Wealth!` → `neviswealth`).

Strategy:

1. If `q` contains `@`:
    - Treat as email search.
    - Use trigram similarity on `email`.

2. Else:
    - **Name search**:
        - Trigram similarity on `full_name` against `q_normalized`.
    - **Domain/company search**:
        - Trigram similarity on `email_domain_slug` against `q_slug`
          so that `"Nevis Wealth"` matches `neviswealth.com`.

3. Combine the results:
    - Use similarity scores to rank.
    - Return up to a fixed maximum number of clients (e.g. 20).
    - No explicit `limit` query param is exposed.

Implementation-wise, repositories will execute SQL via `JdbcTemplate` / `NamedParameterJdbcTemplate`, mapping rows to DTOs.

Result type for client hits:

```json
{
  "type": "client",
  "score": 0.95,
  "client": {
    "id": "uuid",
    "first_name": "John",
    "last_name": "Smith",
    "email": "john.smith@neviswealth.com",
    "countryOfResidence": "PT"
  }
}
```

### 7.2 Document search

Document search can be scoped to a specific client when `clientId` is provided; if omitted, search runs across all documents.

Input:

- Query string `q` (required).
- `clientId` (UUID, optional).

Strategy:

1. Compute query embedding:
    - `v_q = EmbeddingProvider.embed(q)`.

2. Query chunks (optionally scoped) using JDBC:

```sql
SELECT dc.document_id,
       dc.chunk_index,
       dc.content,
       d.client_id,
       d.title
FROM document_chunks dc
JOIN documents d ON d.id = dc.document_id
WHERE (:clientId IS NULL OR d.client_id = :clientId)
ORDER BY dc.embedding <-> :v_q
LIMIT :limit_chunks;
```

3. Group chunks by `document_id`:
    - For each document:
        - Select the best chunk (minimal distance).
        - Compute a score (e.g. inverse of distance or normalised similarity).
        - Use that chunk’s content as the `matchedSnippet`.

4. Sort documents by score and return top N documents (e.g. 10).

Result type for document hits:

```json
{
  "type": "document",
  "score": 0.92,
  "document": {
    "id": "uuid-document-id",
    "client_id": "uuid-client-id",
    "title": "Utility bill October"
  },
  "matched_snippet": "…recent utility bill as proof of your address…"
}
```

### 7.3 Aggregated search behaviour

Endpoint: `/search`

- Request:
    - Query param `q` (string, required).
    - Query param `clientId` (UUID, optional).
- Behaviour:
    - Run **client search** based on `q`.
    - Run **document search**:
        - If `clientId` is provided, scope documents to that client.
        - If `clientId` is omitted, search across all documents.
    - Combine all results and sort by score descending.

Response:

- JSON array of mixed results:

```json
[
  {
    "type": "client",
    "score": 0.95,
    "client": { ... }
  },
  {
    "type": "document",
    "score": 0.92,
    "document": { ... },
    "matched_snippet": "..."
  }
]
```

If `clientId` is omitted, documents across all clients are included alongside client hits.

---

## 8. API Design

### 8.1 POST /clients

Create a client.

- Request body:

```json
{
  "first_name": "John",
  "last_name": "Smith",
  "email": "john.smith@neviswealth.com",
  "countryOfResidence": "PT"
}
```

- Behaviour:
    - Validate required fields.
    - Extract `email_domain` and `email_domain_slug` from `email`.
    - Build `full_name`.
    - Enforce uniqueness by `email`.
- Responses:
    - `201 Created` + `ClientDto`.
    - `400 Bad Request` – validation error.
    - `409 Conflict` – client with this email already exists.

### 8.2 GET /clients/{id}

Fetch a client by id.

- Path param: `id` (UUID).
- Response:
    - `200 OK` + `ClientDto`.
    - `404 Not Found` – if client does not exist.

### 8.3 POST /clients/{clientId}/documents

Create a document for a client.

- Path param: `clientId` (UUID).
- Request body:

```json
{
  "title": "Utility bill October",
  "content": "Full text content of the document ..."
}
```

- Behaviour:
    - Validate `title` and `content`.
    - Ensure client exists.
    - Ensure no existing document with same `(clientId, title)`:
        - On violation, return `409 Conflict`.
    - Insert into `documents`.
    - Chunk content and store chunks with embeddings in `document_chunks`.
- Responses:
    - `201 Created` + `DocumentDto` (without content).
    - `400 Bad Request` – validation error.
    - `404 Not Found` – client not found.
    - `409 Conflict` – document with the same title already exists for this client.

### 8.4 GET /documents/{id}

Fetch a document and its content.

- Path param: `id` (UUID).
- Response:
    - `200 OK` – `DocumentWithContentDto`:

      ```json
      {
        "id": "uuid-document-id",
        "client_id": "uuid-client-id",
        "title": "Utility bill October",
        "content": "Full text content of the document ..."
      }
      ```

    - `404 Not Found` – if document does not exist.

### 8.5 GET /search

Aggregated search for clients and documents.

- Query params:
    - `q` (string, required).
    - `clientId` (UUID, optional).
- Responses:
    - `200 OK` – list of mixed `client` and `document` results.
    - `400 Bad Request` – missing `q` or invalid `clientId`.

---

## 9. Testing Strategy

### 9.1 Unit tests

- Validate:
    - Client search normalisation and selection logic.
    - Domain rules (e.g. unique email, unique `(clientId, title)`).
    - Chunking strategy behaviour (chunk sizes, splitting rules).
    - Document creation flow (chunks and embeddings invoked).
- Use `MockEmbeddingProvider` for deterministic vectors.

### 9.2 Integration tests (Testcontainers)

Using Testcontainers to spin up Postgres:

- Apply Liquibase migrations at startup.
- Test client search:
    - Insert clients with varied emails/names/domains.
    - Assert that:
        - `q` with `@` matches email.
        - Company-like `q` matches `email_domain_slug`.
        - Name-based queries work as expected.
- Test document search:
    - Insert documents and chunks with known embeddings (via mock provider or saved vectors).
    - Assert:
        - `/search?q=...&clientId=...` returns expected documents.
        - `matched_snippet` contains the relevant text part.

Tests are run via Gradle (`./gradlew test`).

---

## 10. Local Development and Docker

Local stack:

- `devops/local/docker-compose.yml` (or equivalent) starts:
    - Postgres (image with `pg_trgm` + `pgvector` available).
    - embeddings-calculation-service (optional in dev).
    - Java backend (optional, can also run from IDE).

Two modes:

1. **Full mode (with embeddings)**:
    - Start embeddings service container.
    - Configure backend to use `HttpEmbeddingProvider`.

2. **Mock mode**:
    - Do not start embeddings service.
    - Configure backend to use `MockEmbeddingProvider`.

The `README.md` in the repository root explains:

- How to start the environment with docker-compose.
- How to build and run the backend JAR.
- How to run tests.
- How to access Swagger UI.
- Relevant configuration properties (DB URL, embedding provider URL/token, etc.).

