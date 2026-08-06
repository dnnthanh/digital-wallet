# DW-002: Keycloak Authentication & Authorization Specification

## Status
APPROVED FOR IMPLEMENTATION — derived from accepted ADR-002, AGENTS.MD, DW-001 platform contracts, and the explicit DW-002 delivery request.

## Goal
Provide production-oriented authentication and authorization foundations centered on Keycloak without introducing a competing IAM database or implementing Ledger/Transfer/KYC business behavior.

## Scope
DW-002 owns:
- Keycloak realm/client/role/group configuration for local development and automated contract verification;
- compact authenticated identity extraction into the platform `UserContext`;
- effective permission resolution from Keycloak composite realm roles;
- hierarchical authorization scope resolution from Keycloak group paths;
- bounded-TTL Redis caching for resolved authorization metadata with fail-open-to-Keycloak behavior;
- reusable backend permission/scope enforcement primitives suitable for `@PreAuthorize`;
- self-service REST APIs:
  - `GET /api/v1/me/profile`
  - `GET /api/v1/me/permissions`
  - `GET /api/v1/me/scopes`;
- service-to-service client-credentials support already established by DW-001, with DW-002 wiring/configuration proving its use against Keycloak;
- audit actor classification continuity for USER, SERVICE, SCHEDULER and KAFKA_CONSUMER identities.

DW-002 does not own:
- Ledger, Transfer, KYC, wallet-account, fraud, limit or settlement business rules;
- business-domain persistence tables;
- a custom user/credential/role database;
- authorization decisions performed only by frontend code;
- large permission/scope payloads embedded into JWTs.

## Architecture
### Platform layer
`be-platform-starter` remains the reusable security boundary. It owns compact principal extraction, common authorization value types, and a method-security bean that can evaluate permission/scope data exposed through ports without leaking Spring Security into future application/domain code.

### Auth bounded context
Add runnable `backend/services/be-auth-api` using the repository Hexagonal layout:

```text
adapter/in/web -> application/port/in -> application/service -> application/port/out -> adapter/out/external/keycloak/rest
```

The service exposes only current-principal self-service metadata. It owns no IAM database.

### Keycloak
Keycloak remains source of truth for authentication and authorization metadata:
- composite realm roles represent `role -> permissions`;
- permission roles use the prefix `permission:` and are not assigned directly by wallet business code;
- Keycloak groups represent hierarchical authorization scope; APIs expose stable group `path` values rather than internal database state;
- the backend obtains an administrative service token through client credentials and calls Keycloak Admin REST APIs;
- client secrets are environment-backed placeholders in realm import/configuration, never committed literal secrets.

### Cache
Redis role: `CACHE` only. Effective authorization is cached by user id with a bounded TTL. Cache failure must not grant access and must not become an authority; the service resolves from Keycloak when cache is absent/unavailable. Invalidation is exposed as an explicit application operation for future admin/event integration.

## Public contracts
All endpoints are authenticated and live under `/api/v1`.

### GET `/api/v1/me/profile`
Returns stable identity data derived from the JWT/request context:
- `userId`
- `username`
- `actorType`
- compact `roles`

No token, credential or Keycloak secret is returned.

### GET `/api/v1/me/permissions`
Returns effective permission codes resolved from Keycloak composite realm roles. Only role names beginning with `permission:` are exposed, with the prefix removed in the API response. Results are distinct and sorted.

### GET `/api/v1/me/scopes`
Returns hierarchical Keycloak group paths assigned to the current user. Results are distinct and sorted.

## Authorization primitives
The platform must provide a bean addressable from method security expressions:
- `@walletAuthorization.hasPermission('code')`
- `@walletAuthorization.hasScope('/scope/path')`
- `@walletAuthorization.hasPermissionInScope('code', '/scope/path')`

Rules:
- missing authentication -> false;
- missing permission/scope -> false;
- scope match is exact path or descendant path separated by `/`; string-prefix collisions such as `/bank/a` vs `/bank/abc` must not authorize;
- service actors do not bypass permissions/scopes unless a future feature explicitly specifies such a policy.

## Keycloak Admin REST adapter
For current `userId`, resolve:
- effective realm roles from `GET /admin/realms/{realm}/users/{user-id}/role-mappings/realm/composite`;
- groups from `GET /admin/realms/{realm}/users/{user-id}/groups`.

The adapter uses the DW-001 `ServiceTokenProvider`; base URL, realm, client id/secret, token URI and timeouts are configuration/environment-backed.

Keycloak 26 supports environment-variable placeholders in realm import files, so local client secrets remain placeholders supplied by Compose/runtime.

## Failure semantics
- invalid/missing bearer token -> platform authentication response (401);
- authenticated principal without required permission/scope -> 403;
- Keycloak authorization metadata unavailable and no usable cached value -> stable internal dependency failure (5xx), never silent authorization success;
- Redis unavailable -> log/cache miss semantics, then resolve from Keycloak;
- malformed Keycloak response -> typed infrastructure failure, no raw provider body/secret exposed;
- token/client secret values must never be logged.

## Observability and correlation
Reuse DW-001 platform tracing/correlation. The auth service must:
- define `spring.application.name=be-auth-api`;
- use Spring-managed `RestClient.Builder` so W3C tracing propagates automatically;
- never manually write `traceparent`/`tracestate`;
- log authorization/provider failures without JWTs, passwords, tokens or secrets.

## Docker / Compose
- Add `be-auth-api` to the backend Maven reactor and generic `backend/Dockerfile.service` pattern.
- Add `compose/backend/auth.yml` and aggregate it from `compose/backend/all.yml`.
- Keycloak client secret is required via environment variable; `.env.example` contains blank secret placeholders only.
- Existing PostgreSQL, Redis, Kafka, Keycloak, gRPC and observability foundations from DW-001 remain the single platform implementation; DW-002 must not duplicate them.

## Required tests / TDD evidence
1. `UserContext` maps JWT identity and compact role claims, and classifies configured service-account roles as SERVICE.
2. Permission mapper accepts only `permission:` effective role names, strips prefix, deduplicates and sorts.
3. Scope mapper deduplicates and sorts Keycloak group paths.
4. Authorization policy denies missing values and handles exact/descendant scope boundaries correctly.
5. Self-service controller requires authentication and returns the platform response contract.
6. Keycloak adapter sends service bearer token and maps composite roles/groups from provider responses.
7. Redis authorization cache is typed, bounded TTL, namespaced by `spring.application.name`, and cache failure falls through to provider resolution.
8. Realm contract test proves client secret placeholder, service account/client, composite role, permission role and hierarchical group fixtures exist without committed secret literals.
9. Maven reactor, Spotless, repository verification, Dockerfile checks and root/split Compose validation pass.

## Definition of done
- Spec and implementation plan are committed before production code.
- RED tests are committed and observed failing for the missing DW-002 implementation.
- Minimal implementation makes those tests green without Ledger/Transfer/KYC code.
- Verification evidence is stored in `.agent/reports/DW-002-auth-keycloak-verification.md`.
- Branch targets `develop`; GitHub Actions are green.
- No merge into `master`.
