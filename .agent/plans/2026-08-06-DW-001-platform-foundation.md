# DW-001 Platform Foundation Implementation Plan

**Goal:** Build the Digital Wallet technical foundation by adapting the proven E-commerce repository skeleton, while keeping the domain banking-specific and adding gRPC as a first-class internal synchronous transport.

**Architecture:** Preserve the reference project's repository shape and conventions instead of inventing a parallel skeleton. Shared backend capabilities live under `backend/platform/` and are split into `be-platform-starter` for web/security/messaging/gRPC/idempotency/observability primitives and `be-platform-cache-starter` for Redis/cache primitives. Future runnable deployables live under `backend/services/be-*`. Docker uses the same generic service/runtime image pattern as the reference project, and Compose is split into infrastructure plus backend fragments.

**Tech Stack:** Java 25, Spring Boot 4.1.0, Spring Cloud 2025.1.2, Spring Security OAuth2, Spring Kafka, Spring Data Redis/Lettuce, Spring gRPC, OpenTelemetry, PostgreSQL, Keycloak, Testcontainers, JUnit/AssertJ, ArchUnit, MapStruct, Lombok, Resilience4j, JaCoCo, Maven Enforcer.

## Reference-derived structure

```text
backend/
  pom.xml
  platform/
    be-platform-starter/
    be-platform-cache-starter/
  services/                 # populated by later DW features
  Dockerfile.service
  Dockerfile.runtime
  docker/
    service-entrypoint.sh
    runtime-entrypoint.sh
compose/
  infrastructure.yml
  backend/
    all.yml
docker-compose.yml
Dockerfile.backend
compose-up.sh
infrastructure/
  postgres/
  keycloak/
```

## Constraints
- E-commerce is a structural/engineering reference only; marketplace domain concepts do not enter Digital Wallet.
- No business-domain model in shared platform starters.
- No source-controlled production secrets/default passwords.
- No runtime REST-vs-gRPC switch.
- REST remains public/admin/external HTTP; gRPC is selected internal synchronous communication; Kafka is asynchronous/event communication.
- Platform failures never silently change financial semantics.
- Dockerfile and Compose foundations are part of DW-001, not deferred to later business features.

## Tasks
1. Mirror the reference Maven parent baseline and `backend/platform/*` module organization on Java 25/Spring Boot 4.1.
2. Split generic platform primitives into `be-platform-starter` and Redis/cache primitives into `be-platform-cache-starter`.
3. Keep REST error/correlation, idempotency, outbox/event-envelope, OAuth2 service identity and architecture tests in the platform starter.
4. Add Digital Wallet-specific gRPC deadline/correlation infrastructure without changing the reference's overall repository shape.
5. Keep Redis integration tests in the cache starter and PostgreSQL Testcontainers smoke tests in the core platform starter.
6. Add generic backend `Dockerfile.service`, `Dockerfile.runtime`, root `Dockerfile.backend`, entrypoints and non-root Java 25 runtime conventions based on the reference project.
7. Add root and split Compose topology for PostgreSQL, Redis, Kafka KRaft and Keycloak, leaving backend service fragments ready for later DW features.
8. Add CI gates for repository structure, Maven/Testcontainers, Compose config and Dockerfile syntax/build checks.
9. Store fresh verification evidence and keep PR targeting `develop`.

## Quality gate
- `python3 verification/verify_repository.py` must enforce the reference-shaped platform/Docker/Compose layout and reject legacy `backend/be-platform-foundation`.
- `./mvnw -B -ntp -f backend/pom.xml verify` must pass on Java 25 with Docker/Testcontainers.
- `docker compose -f docker-compose.yml config` must pass with CI-provided ephemeral credentials.
- Split compose validation (`compose/infrastructure.yml` + `compose/backend/all.yml`) must pass.
- All backend Dockerfiles must pass BuildKit `--check` validation.
- PR targets `develop` and all required GitHub Actions jobs are green.
