# Backend Search Service – Agent Instructions

## 1. Role and scope

You are the coding agent for a backend search service.

Your job is to implement and evolve a backend system that:
- Manages clients and their documents.
- Provides search capabilities over this data (including semantic search via embeddings).
- Follows the architecture and design decisions described in `docs/design.md`.

This file focuses on tools, repository layout, and conventions. For functional requirements and deeper design details, always consult `docs/design.md`.

---

## 2. Tech stack and tools

The project is developed and run locally with the following tools:

- Java: 25.0.1  
- Spring Boot: 3.5.8  
- Build tool: Gradle (latest stable version)  
- Python: 3.13.9  
- Python web framework: FastAPI (for the embeddings service)  
- Database: PostgreSQL (via Docker)  
- Container tooling:
  - Docker: 29.0.1
  - docker-compose: 2.40.3-desktop.1

Do not introduce additional languages, frameworks, or databases unless explicitly instructed.

---

## 3. Repository layout

At the root of the repository:

- `search-service/`  
  Java + Spring Boot backend service.

- `embeddings-calculation-service/`  
  Python FastAPI service that loads an embedding model (e.g. `BAAI/bge-small-en`) and exposes an HTTP API to compute text embeddings.  
  This is used for local development and testing. In production, the system is expected to use a third-party LLM/embedding provider; see `docs/design.md` for how that integration is modeled.

- `devops/`
  - `devops/local/`  
    Local infrastructure for development. Contains `docker-compose` files to start PostgreSQL, the embeddings service, and any other local dependencies.
  - `devops/remote/`  
    Optional directory reserved for remote infrastructure definitions (e.g. `terraform/`, `helm/`) for future deployment work. It is not required to be implemented.

- `docs/`
  - `docs/design.md`  
    Main architecture and design document for this project. It describes the domain, API contracts, search approach, and design trade-offs.

- `AGENTS.md` (this file)  
  Project-level instructions and conventions for the coding agent.

Keep this structure stable unless explicitly asked to change it.

---

## 4. Design and behaviour reference

Do not treat this file as the full specification of the system.

- For **requirements, domain rules, API shapes, search behaviour, and edge cases**, always refer to:
  - `docs/design.md`

If you change the design or behaviour in a meaningful way (e.g. new endpoints, new entities, updated search semantics), update `docs/design.md` accordingly and, where relevant, adjust this `AGENTS.md` to stay in sync.

---

## 5. Development conventions

When modifying or creating code, follow these conventions:

- Prefer **small, incremental changes** that could correspond to realistic pull requests.
- Aim for **test-first or test-driven development** where practical:
  - Add or update tests whenever behaviour changes.
  - Cover critical edge cases and error paths.
- Maintain clear separation of concerns in the Java backend:
  - Domain / application logic.
  - Persistence layer (repositories).
  - HTTP/API layer (controllers).
  - Integration with external services (e.g. embeddings service, future LLM provider).
- Keep code readable and straightforward; avoid premature abstraction and over-generalisation.

Do not introduce large refactors or architectural changes without a clear reason that is consistent with `docs/design.md`.

---

## 6. Testing strategy

The project should have:

- **Unit tests** for core domain and service logic.
- **Integration tests** using Testcontainers (or a similar approach) to exercise:
  - PostgreSQL integration.
  - Search behaviour.
  - Any cross-service interactions (e.g. with the embeddings service, using a test or mock implementation if necessary).

General expectations:

- Place Java tests under `search-service/src/test/java/...`.
- Name tests clearly and keep them deterministic and repeatable.
- Use realistic test data that reflects the kinds of queries and documents described in `docs/design.md`.

When implementing or modifying functionality, ensure tests are updated or added accordingly.

---

## 7. Running the system locally

Assume the following workflows (exact commands may be adjusted to match the final Gradle and compose setup):

- Start local infrastructure (PostgreSQL, embeddings service, etc.):
  - Use docker-compose files from `devops/local/`.  
  - Example (subject to actual filenames):
    - `docker compose -f devops/local/docker-compose.yml up -d`

- Build the Java service:
  - From `search-service/`:
    - `./gradlew build`  
  - The resulting JAR should be runnable via:
    - `java -jar build/libs/<artifact-name>.jar`

- Run the Java service:
  - Prefer running via the IDE (e.g. IntelliJ IDEA) using the Spring Boot run configuration during development.
  - For containerized runs, use the JAR built by Gradle and run it with `java -jar` inside the Docker image.

- Run tests:
  - From `search-service/`:
    - `./gradlew test`

- Run the embeddings service:
  - From `embeddings-calculation-service/`:
    - Start the FastAPI app (e.g. `uvicorn main:app --reload` or via a helper script defined in that directory).
  - Ensure it is reachable from the Java service at the configured URL (see `docs/design.md` / configuration files).

If you change how the app is built, started, or tested, update this section so the instructions remain accurate.

---

## 8. Keeping documentation in sync

Whenever you introduce or change:

- The overall system design or architecture,
- API contracts or significant behaviour,
- Repository layout or core tooling,

you should:

1. Update `docs/design.md` to reflect the new design decisions or behaviour.
2. Update this `AGENTS.md` if any of its sections (tech stack, layout, workflows, conventions) are affected.

Treat `AGENTS.md` and `docs/design.md` as living documents that must stay aligned with the actual codebase.

---

## 9. Guardrails

- Do not change:
  - Tool versions listed in Section 2, unless explicitly instructed.
  - The high-level repository layout described in Section 3, unless explicitly instructed.
- Do not add new external services or databases without a clear, documented rationale in `docs/design.md`.
- When modifying behaviour that affects data access, search semantics, or any form of isolation, ensure tests clearly verify the intended behaviour.
- Keep configuration, secrets, and environment-specific settings out of the repository, following good security practices.

If something is unclear, first check `docs/design.md` and the existing code and tests. Only introduce new behaviour or patterns when they are consistent with that design.
