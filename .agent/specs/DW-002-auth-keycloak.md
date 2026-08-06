# DW-002: Keycloak Authentication & Authorization Specification

## Status
IMPLEMENTED / IN REVIEW — finalized for the current `feature/platform-foundation` delivery against `develop`.

## Goal
Provide production-oriented authentication and authorization foundations centered on Keycloak without introducing a competing IAM database or implementing Ledger/Transfer/KYC business behavior.

## Scope
DW-002 owns:
- Keycloak realm/client/role/group configuration for local development and contract verification;
- Spring Security Resource Server authentication and compact JWT identity extraction into platform `UserContext`;
- effective permission resolution from Keycloak composite realm roles;
- hierarchical authorization scope resolution from Keycloak group paths;
- bounded-TTL Redis caching for resolved authorization metadata with fail-closed authorization semantics;
- reusable permission/scope primitives suitable for `@PreAuthorize`;
- self-service REST APIs `GET /api/v1/me/profile`, `/permissions`, and `/scopes`;
- service-to-Keycloak machine authentication using OAuth 2.0 Client Credentials with RFC 7523 `private_key_jwt` client authentication;
- explicit technical execution contexts for `SERVICE`, `SCHEDULER`, and `KAFKA_CONSUMER` actors.

DW-002 does not own Ledger, Transfer, KYC, wallet-account, fraud, limits, settlement, a custom IAM database, frontend-only authorization, or future business-domain policies.

## Architecture
### Platform layer
`be-platform-starter` owns reusable security mechanics: resource-server configuration, request `UserContext`, technical actor factories, method-security primitives, signed client assertions, service-token acquisition, common API errors, and correlation/observability integration.

The M2M token provider is not servlet/request scoped so Kafka workers and scheduled jobs can use it when they call a protected downstream service.

### Auth bounded context
`backend/services/be-auth-api` follows the repository Hexagonal layout:

```text
adapter/in/web -> application/port/in -> application/service -> application/port/out -> adapter/out/external/keycloak/rest
```

It exposes current-principal metadata and owns no IAM persistence tables.

### Keycloak source of truth
Keycloak remains the authentication/authorization authority:
- composite realm roles represent `role -> permissions`;
- permission roles use the `permission:` prefix;
- group paths represent hierarchical authorization scope;
- the existing single client `be-auth-api` remains the service-account client; DW-002 does not introduce a separate admin client;
- Keycloak Admin REST is called with a service-account access token obtained through Client Credentials.

## Client authentication: `private_key_jwt`
`be-auth-api` no longer authenticates the token request with a shared `client_secret`.

The client uses RFC 7523 signed JWT client authentication:
- Keycloak client authenticator: `client-jwt`;
- signing algorithm: RS256;
- client assertion `iss` = `sub` = `be-auth-api`;
- assertion `aud` is the configured Keycloak token endpoint;
- assertion has short `exp`, `iat`, unique `jti`, and configured `kid`;
- token request contains `grant_type=client_credentials`, `client_id`, `client_assertion_type`, and `client_assertion`;
- no `client_secret` is sent or stored for this service authentication path.

### Key ownership and JWKS
The private RSA key belongs to the service runtime and must never be committed to Git.

Local Compose generates the private key into a Docker volume and mounts it read-only into `be-auth-api`. The service publishes only the derived public JWK at:

```text
GET /.well-known/wallet-client-jwks.json
```

This endpoint is intentionally unauthenticated because Keycloak must fetch the public key before it can authenticate the client. It must never expose RSA private parameters such as `d`.

Keycloak realm configuration uses:
- `clientAuthenticatorType=client-jwt`;
- `use.jwks.url=true`;
- `jwks.url=http://be-auth-api:8080/.well-known/wallet-client-jwks.json`;
- service account enabled;
- no literal client secret.

Production key material must come from an appropriate secret/key-management mechanism; the repository only defines runtime location, key id, assertion TTL, and public-key discovery contracts.

## HTTP, Kafka, jobs and technical identity
### HTTP request
For a normal authenticated request, Spring Security validates the bearer JWT and builds request `UserContext`. A service-account JWT with `SERVICE_ACCOUNT` maps to `ActorType.SERVICE`.

### Kafka consumer
Kafka execution has no servlet request and therefore must not depend on request-scoped `SecurityContext`/`UserContext`.

A consumer creates an explicit technical context:

```text
ActorType = KAFKA_CONSUMER
userId/username = service name
role = SERVICE_ACCOUNT
```

This local context does not require minting a JWT. If the consumer later calls another protected service, it obtains an M2M access token through `ServiceTokenProvider`.

### Scheduled job
A scheduled job follows the same rule with `ActorType.SCHEDULER`: local technical execution context first; M2M credential only at an outbound protected service boundary.

### Business initiator vs technical executor
Business provenance and technical authentication are independent concerns. An event may carry `initiatedByUserId`/`username` or equivalent metadata for audit/history while the background consumer authenticates downstream as its service account. A background worker must not manufacture or impersonate an end-user token merely because a username exists in event metadata. On-behalf-of/delegation requires an explicit future security requirement.

## Public contracts
All `/api/v1/**` endpoints require authentication.

### GET `/api/v1/me/profile`
Returns stable identity data derived from JWT/request context: `userId`, `username`, `actorType`, and compact roles. No token, private key, credential, or Keycloak secret is returned.

### GET `/api/v1/me/permissions`
Returns effective permission codes resolved from Keycloak composite realm roles. Only effective role names beginning with `permission:` are exposed, with the prefix removed. Results are distinct and sorted.

### GET `/api/v1/me/scopes`
Returns distinct, sorted Keycloak group paths assigned to the current user.

## Authorization primitives
The platform exposes `walletAuthorization` for method security:
- `hasPermission(code)`;
- `hasScope(path)`;
- `hasPermissionInScope(code, path)`.

Rules:
- missing authentication/permission/scope denies;
- scope match is exact or descendant-by-`/` boundary;
- raw string-prefix collisions such as `/bank/a` vs `/bank/abc` do not authorize;
- service/system actors do not implicitly bypass business permissions.

## Keycloak Admin REST adapter
For the current user id, resolve:
- effective realm roles from `/admin/realms/{realm}/users/{user-id}/role-mappings/realm/composite`;
- groups from `/admin/realms/{realm}/users/{user-id}/groups`.

The adapter uses Spring-managed `RestClient.Builder` and the shared `ServiceTokenProvider`. Client authentication details remain inside the platform security adapter rather than application/domain code.

## Cache and failure semantics
Redis is a cache, never an authorization authority. The default authorization snapshot TTL is bounded (`PT2M`).

- invalid/missing bearer token -> 401;
- authenticated principal without required permission/scope -> 403;
- Redis unavailable -> treat as cache miss and resolve from Keycloak;
- Keycloak unavailable and no usable cached value -> dependency failure, never authorization success;
- malformed provider response -> typed infrastructure failure;
- JWTs, access tokens, private keys, passwords and assertions must not be logged.

## Docker / Compose
- `be-auth-api` uses the generic service Dockerfile.
- `auth-keygen` creates an ephemeral/local RSA private key in the `auth-client-key` volume when absent.
- `be-auth-api` mounts that volume read-only.
- Keycloak receives only the public JWKS URL; it does not receive the private key.
- `.env.example` contains configuration identifiers/TTLs but no `AUTH_KEYCLOAK_CLIENT_SECRET`.
- private keys and local `.env` files must remain outside Git.

## Required tests / TDD contracts
1. JWT request identity and service actor classification.
2. Permission and scope mapping/authorization boundaries.
3. Self-service controller authentication/API contracts.
4. Keycloak Admin adapter bearer-token behavior.
5. Typed bounded-TTL Redis authorization cache and provider fallback.
6. `ServiceTokenProvider` sends `client_assertion` and never `client_secret`.
7. RS256 assertion verifies with the paired public key and contains required RFC 7523 claims.
8. Public JWKS contains the configured `kid` and no private RSA parameter.
9. Realm contract requires `client-jwt`, JWKS URL and service account without a client secret.
10. `SystemUserContextFactory` creates SERVICE/SCHEDULER/KAFKA_CONSUMER technical actors.
11. Kafka consumer can establish local technical context without an HTTP SecurityContext.
12. Internal-security Spring auto-configuration creates the signed-JWT/token-provider beans in a real application context.
13. Maven verify, Spotless, repository verification, Dockerfile and Compose checks pass.

## Documentation
The security study/reference document is versioned at:

```text
docs/security/Keycloak_Authentication_Authorization_Deep_Dive_VI.docx
```

It covers Keycloak, OAuth/OIDC, cookie/session/JWT, `private_key_jwt`, mTLS, RBAC/ABAC, Spring Security, Kafka/job/M2M, threat modeling and implementation checklists.

## Definition of done
- Implementation and TDD evidence are recorded in `.agent/reports/DW-002-auth-keycloak-verification.md`.
- No Ledger/Transfer/KYC/future-domain implementation is introduced.
- All CI gates that can execute are green; any externally blocked gate is documented rather than reported as passed.
- PR targets `develop`; no merge into `master` is part of DW-002.
