# Backend Search Service

Java Spring Boot service that manages clients and their documents and exposes a single `/search` endpoint that returns both client and document matches (including semantic search over document chunks). Design details live in `docs/design.md`.

## Repository layout
- `search-service/` — Spring Boot application code and Gradle build.
- `devops/local/` — local infra (Postgres via docker-compose).
- `docs/` — architecture and design reference.

## Prerequisites
- Java 25 (uses the Gradle wrapper, no local Gradle needed).
- Docker + docker-compose for local Postgres.

## Quickstart (local)
1) Start Postgres:
```
docker compose -f devops/local/docker-compose.yml up -d
```
2) Run the app (Liquibase applies migrations on startup):
```
cd search-service
./gradlew bootRun
```
3) API docs: `http://localhost:8080/swagger-ui/index.html`

## Configuration
Key environment variables (defaults are defined in `src/main/resources/application.yml`):
- `DB_URL` (default `jdbc:postgresql://localhost:6000/search_service`)
- `DB_USERNAME` / `DB_PASSWORD`
- `DB_ADMIN_USERNAME` / `DB_ADMIN_PASSWORD` (used by Liquibase)
- Embeddings: `EMBEDDING_BASE_URL`, `EMBEDDING_API_TOKEN`, `EMBEDDING_PROVIDER` (default `mock`)
- Chunking: `CHUNKING_MAX_CHARS` (default `1200`)

When using the provided Postgres container, set:
```
DB_USERNAME=search_service
DB_PASSWORD=search_service
DB_ADMIN_USERNAME=search_service
DB_ADMIN_PASSWORD=search_service
```

## Build and test
From `search-service/`:
```
./gradlew test
./gradlew bootJar
```

## Docker
Build the service image (from `search-service/`):
```
docker build -t search-service .
```
Run it against the local Postgres (Windows/macOS example using host.docker.internal):
```
docker run -p 8080:8080 ^
  -e DB_URL=jdbc:postgresql://host.docker.internal:6000/search_service ^
  -e DB_USERNAME=search_service ^
  -e DB_PASSWORD=search_service ^
  -e DB_ADMIN_USERNAME=search_service ^
  -e DB_ADMIN_PASSWORD=search_service ^
  search-service
```

## Docker Compose options
- Database only (default): `docker compose -f devops/local/docker-compose.yml up -d` (or target explicitly with `... up -d postgres`)
- Database + Java service: `docker compose -f devops/local/docker-compose.yml --profile app up -d`
- Database + Java service + embeddings provider: `docker compose -f devops/local/docker-compose.yml --profile app --profile embed up -d`  
  The embeddings service block is currently commented out—uncomment it when the embeddings provider is available.

## Useful links
- Design document: `docs/design.md`
- Local compose: `devops/local/docker-compose.yml`
