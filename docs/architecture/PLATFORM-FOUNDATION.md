# DW-001 Platform Foundation

DW-001 deliberately follows the repository and backend skeleton from the E-commerce reference project while keeping all domain concepts Digital Wallet/Banking-specific.

## Backend platform modules

### `backend/platform/be-platform-starter`

Business-neutral technical primitives shared by backend deployables:
- stable REST error envelopes and correlation IDs;
- idempotency-key validation primitives;
- Kafka event-envelope and transactional-outbox contracts;
- gRPC deadline and correlation metadata interceptors for selected internal synchronous calls;
- OAuth2 client-credentials token-provider abstraction for Keycloak service identities;
- Spring Boot auto-configuration hooks;
- PostgreSQL Testcontainers smoke tests and architecture rules.

### `backend/platform/be-platform-cache-starter`

Redis/cache-specific primitives are isolated from the general starter:
- typed Redis JSON cache helpers;
- explicit cache TTL specifications;
- Redis Testcontainers integration verification.

Future runnable applications belong under `backend/services/be-*`, matching the reference project's module topology.

## Container and local infrastructure baseline

Docker/container support is part of DW-001:
- `backend/Dockerfile.service`: generic per-service build/runtime image;
- `backend/Dockerfile.runtime`: shared runtime image capable of selecting a built `SERVICE_MODULE`;
- `Dockerfile.backend`: root backend runtime build entry point;
- `backend/docker/*-entrypoint.sh`: non-root Java runtime entry points;
- `docker-compose.yml`: root local infrastructure entry point;
- `compose/infrastructure.yml`: PostgreSQL, Redis, Kafka KRaft and Keycloak;
- `compose/backend/*.yml`: backend deployable fragments added by subsequent DW features;
- `compose-up.sh`: consistent local Compose launcher.

The platform starters deliberately do not contain wallet, ledger, transfer, KYC, merchant, customer, settlement, seller, cart, catalog or checkout domain models.
