# DW-002: Keycloak Authentication & Authorization Specification

## Status

IMPLEMENTED / IN REVIEW on `feature/platform-foundation`, targeting `develop`.

## Goal

Provide a production-oriented authentication and authorization foundation centered on Keycloak without introducing a competing IAM database or implementing Ledger, Transfer, KYC, wallet-account, fraud, limits, settlement, or other future-domain behavior.

## Scope

DW-002 owns:

- Spring Security Resource Server authentication and JWT identity extraction into platform `UserContext`;
- Keycloak realm/client/role/group contracts;
- effective permissions from composite realm roles prefixed with `permission:`;
- hierarchical authorization scopes from Keycloak group paths;
- reusable `WalletAuthorization` permission/scope primitives for method security;
- bounded-TTL Redis authorization snapshots with Keycloak fallback and fail-closed semantics;
- `GET /api/v1/me/profile`, `/permissions`, and `/scopes`;
- service-to-Keycloak Client Credentials authentication using RFC 7523 `private_key_jwt`;
- explicit technical contexts for `SERVICE`, `SCHEDULER`, and `KAFKA_CONSUMER` actors;
- Docker Compose, tests, runtime contracts, CI quality gates, and security reference documentation.

## Architecture

### Platform layer

`be-platform-starter` owns reusable security mechanics:

- resource-server and method-security configuration;
- request `UserContext` and system-context factory;
- permission and hierarchical-scope checks;
- signed client assertions and service-token acquisition;
- raw-response support for public JWKS;
- common API errors, correlation, audit, and observability conventions.

The M2M token provider is not servlet/request scoped. Kafka workers and scheduled jobs can therefore request a service token only when calling a protected downstream service.

### Auth bounded context

`backend/services/be-auth-api` follows the repository Hexagonal convention:

```text
adapter/in/web
  -> application/port/in
  -> application/service
  -> application/port/out
  -> adapter/out/external/keycloak/rest
```

It owns no IAM persistence tables. Keycloak remains the source of truth.

## Keycloak authorization model

- Composite realm roles model `role -> permissions`.
- Permission roles use the `permission:` prefix.
- Keycloak group paths model hierarchical authorization scope.
- The single client `be-auth-api` remains the service-account client; DW-002 does not introduce a separate admin client.
- Keycloak Admin REST is called with the service-account access token.

## Client authentication: `private_key_jwt`

`be-auth-api` does not send or store a shared `client_secret` for its machine-authentication path.

The token request uses OAuth 2.0 Client Credentials plus RFC 7523 signed JWT client authentication:

- Keycloak authenticator: `client-jwt`;
- signing algorithm: RS256;
- `iss = sub = client_id`;
- `aud` is the configured Keycloak token endpoint;
- short-lived `iat`/`exp`;
- unique `jti`;
- configured `kid`;
- form parameters: `grant_type`, `client_id`, `client_assertion_type`, and `client_assertion`.

### Key ownership and JWKS

The RSA private key belongs to the service runtime and must never be committed or logged.

Local Compose uses a one-shot `auth-keygen` container to create the key in a Docker volume. `be-auth-api` mounts it read-only and exposes only its derived public JWK at:

```text
GET /.well-known/wallet-client-jwks.json
```

The endpoint is public so Keycloak can authenticate the client before issuing a token. It must never expose private RSA parameters such as `d`.

Keycloak realm requirements:

- `clientAuthenticatorType = client-jwt`;
- `use.jwks.url = true`;
- `jwks.url = http://be-auth-api:8080/.well-known/wallet-client-jwks.json`;
- service account enabled;
- no client-secret literal.

## HTTP, Kafka, jobs, and technical identity

### HTTP request

Spring Security validates the incoming bearer JWT and builds request `UserContext`. A JWT containing `SERVICE_ACCOUNT` maps to `ActorType.SERVICE`.

### Kafka consumer

Kafka execution has no servlet request and must not depend on request-scoped `SecurityContext` or `UserContext`.

A consumer creates an explicit local context:

```text
actorType = KAFKA_CONSUMER
userId/username = service name
roles = [SERVICE_ACCOUNT]
```

No JWT is needed for local processing. When the consumer calls a protected downstream service, it obtains an M2M access token through `ServiceTokenProvider`.

### Scheduled job

A scheduled job follows the same rule with `ActorType.SCHEDULER`.

### Business initiator vs technical executor

Event provenance such as `initiatedByUserId` or `username` is business/audit metadata, not a machine credential. The background executor authenticates downstream as its service account unless a future requirement explicitly defines delegation or on-behalf-of behavior.

## Public contracts

- `GET /api/v1/me/profile`: stable `userId`, `username`, `actorType`, and compact roles.
- `GET /api/v1/me/permissions`: distinct sorted effective permissions, with `permission:` removed.
- `GET /api/v1/me/scopes`: distinct sorted Keycloak group paths.

No token, password, private key, assertion, or Keycloak credential is returned.

## Authorization primitives

The platform exposes:

- `hasPermission(code)`;
- `hasScope(path)`;
- `hasPermissionInScope(code, path)`.

Rules:

- missing authentication/permission/scope denies;
- scope match is exact or descendant-by-`/` boundary;
- `/bank/a` must not authorize `/bank/abc`;
- service/system actors do not implicitly bypass business permissions.

## Failure and cache semantics

Redis is a cache, never an authorization authority.

- invalid or missing bearer token -> 401;
- authenticated principal without required authorization -> 403;
- Redis unavailable -> cache miss and Keycloak fallback;
- Keycloak unavailable with no usable cached snapshot -> dependency failure;
- malformed provider response -> typed infrastructure failure;
- dependency failure never becomes authorization success.

Default authorization snapshot TTL: `PT2M`.

## Required verification

The delivery must cover:

1. request identity and service-actor classification;
2. permission/scope mapping and hierarchical-boundary checks;
3. `/me/*` API contracts;
4. Keycloak Admin REST bearer-token behavior;
5. typed Redis cache and provider fallback;
6. client assertion request without `client_secret`;
7. RS256 assertion claims/signature verification;
8. public-only JWKS and configured `kid`;
9. Keycloak realm `client-jwt` contract;
10. SERVICE/SCHEDULER/KAFKA_CONSUMER contexts;
11. Kafka local context without HTTP SecurityContext;
12. real Spring internal-security auto-configuration startup;
13. Maven verify, Spotless, repository, Dockerfile, and Compose checks.

## Documentation artifact

The Vietnamese security reference is versioned as a real binary OOXML document at:

```text
docs/security/Keycloak_Authentication_Authorization_Deep_Dive_VI.docx
```

Repository evidence:

- Git blob SHA: `506e0ce3075f7b09763516cb5ee592cbeae5631c`;
- binary upload/fix commit: `a8a2ccd6c14732d539a3ec5a918723df16d20da4`;
- source artifact SHA-256 before upload: `f14bbc9b4b11a2ce9091eb200c0bcfaa8a7ad878cd9b9f80c153a6784916c4b2`;
- source artifact size: 7,876 bytes;
- rendered visual QA: 18 pages inspected.

The document covers Keycloak, OAuth/OIDC, cookie/session/JWT, `private_key_jwt`, mTLS/workload identity, RBAC/ABAC, Spring Security, Kafka/job/M2M, security threats, testing, and implementation checklists.

## Definition of done

- Implementation and TDD evidence are recorded in `.agent/reports/DW-002-auth-keycloak-verification.md`.
- No future Digital Wallet business behavior is implemented prematurely.
- Executable CI gates are reported accurately; externally blocked runtime checks are not represented as passing.
- PR targets `develop` and is not merged into `master`.
- TDD/intermediate commits may remain unsquashed for the repository owner to squash later.
