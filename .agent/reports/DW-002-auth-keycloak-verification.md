# DW-002 Keycloak Authentication & Authorization Verification

## Scope verified

DW-002 delivers only the shared authentication and authorization foundation. It does not implement Ledger, Transfer, KYC, wallet-account, limits, fraud, settlement, or other future business-domain behavior.

Verified architecture:

- Keycloak remains the authentication and authorization source of truth.
- JWT supplies compact identity/realm-role data.
- Effective permissions are resolved from Keycloak composite realm roles.
- Hierarchical scopes are resolved from Keycloak group paths.
- `be-auth-api` follows Hexagonal boundaries and owns no IAM database.
- Redis is a bounded-TTL typed cache, never an authorization authority.
- One Keycloak service-account client, `be-auth-api`, is retained; no separate admin client was introduced.
- Machine authentication uses Client Credentials plus RFC 7523 `private_key_jwt`, not a shared client secret.
- Kafka consumers and scheduled jobs use explicit technical actors and request an M2M token only when crossing a protected downstream boundary.

## Branch and delivery baseline

- Base branch: `develop`.
- Base commit: `2f29e5bb9fc5981d96d813027d1a5a060373677f`.
- Feature branch: `feature/platform-foundation`.
- Private-key-JWT implementation head before final documentation: `caac53f422013ac129b983168f429ebd1bd1d708`.
- Binary DOCX fix commit: `a8a2ccd6c14732d539a3ec5a918723df16d20da4`.
- Final specification content SHA: `a0719702d3cc686ce9eb133a822a8f4ea2c541e3`.
- TDD/intermediate commits intentionally remain unsquashed because the repository owner will perform the final squash.
- No merge into `develop` or `master` is part of this verification.

## TDD evidence

### Service actor classification

RED:

- commit `55861fb1f5d124134edcbe87af7e9355965aee82`;
- workflow run `31081888947`;
- service-account JWT was incorrectly classified as USER.

GREEN:

- JWTs containing `SERVICE_ACCOUNT` map to `UserContext.ActorType.SERVICE`;
- regression is covered by `UserContextConfigurationTest`.

### Permission and scope authorization

RED:

- commit `e3eed99dc0a6d84a989ee81648faefbb3b2ce82b`;
- workflow run `31082955668`;
- contracts existed before `CurrentAuthorization` and `WalletAuthorization` production classes.

GREEN:

- permission checks fail closed;
- scopes support exact/descendant paths;
- prefix collision such as `/bank/a` versus `/bank/abc` is rejected;
- Keycloak mapping, Redis cache, application resolution, and `/me/*` endpoints are covered.

### Cache starter auto-configuration

Runtime discovery:

- workflow run `31085372346` exposed missing `WalletCacheManager` due to absent Spring Boot auto-configuration metadata.

RED regression:

- commit `ef88945fbdc478b7c39871c362457ca4172ccb87`;
- workflow run `31086215453`.

GREEN:

- `be-platform-cache-starter` publishes `WalletRedisCacheAutoConfiguration` through `AutoConfiguration.imports`.

### `private_key_jwt`

RED:

- commit `954271315effa0a293d056e854da2e353034c9d1`;
- workflow run `31093667934`;
- signed assertion, JWKS, and system-context tests failed before production implementation existed.

GREEN contracts:

- token request contains `grant_type=client_credentials`, `client_id`, `client_assertion_type`, and `client_assertion`;
- token request does not contain `client_secret`;
- assertion uses RS256 and contains `iss=sub=client_id`, exact configured audience, short expiration, issue time, unique `jti`, and `kid`;
- assertion verifies against the paired RSA public key;
- public JWKS contains the configured `kid` and no private RSA `d` parameter;
- Keycloak realm requires `clientAuthenticatorType=client-jwt`, `use.jwks.url=true`, service account, and the auth API JWKS URL;
- Compose generates and mounts private key material without committing it to Git.

### Spring auto-configuration startup

A real runtime attempt exposed constructor ambiguity in `RsaPrivateKeyClientAssertionProvider`.

RED regression:

- commit `9145f526b041a7ba05bf3e9abd573ad14ebc304c`;
- workflow run `31096263118`;
- `PlatformInternalSecurityAutoConfigurationTest` reproduced `No default constructor found` using `ApplicationContextRunner`.

GREEN:

- the production constructor is explicitly selected for Spring dependency injection;
- full Maven verify passes at implementation head `caac53f422013ac129b983168f429ebd1bd1d708`.

### Kafka and scheduler context

Tests verify:

- `forKafkaConsumer(serviceName)` creates `KAFKA_CONSUMER`;
- `forScheduler(serviceName)` creates `SCHEDULER`;
- `forService(serviceName)` creates `SERVICE`;
- technical contexts carry `SERVICE_ACCOUNT` and do not require HTTP request scope;
- `BaseDomainEventConsumer` exposes explicit Kafka technical context.

Security semantics:

- initiating user id/username is provenance metadata, not a credential;
- local Kafka/job execution does not require creating an end-user JWT;
- protected downstream calls use the executor's M2M service token;
- on-behalf-of/delegation remains an explicit future requirement.

## Implementation verification

### Maven and tests

CI command:

```bash
./mvnw -B -ntp -f backend/pom.xml verify
```

Verified result:

- `be-platform-starter`: 35 tests passed;
- `be-platform-cache-starter`: 5 tests passed;
- `be-auth-api`: 12 tests passed;
- total: **52 passed**, 0 failures, 0 errors, 0 skipped;
- Maven reactor BUILD SUCCESS;
- Spring Boot executable JAR repackaging succeeds;
- Temurin JDK 25;
- PostgreSQL and Redis Testcontainers coverage passes.

### Formatting and repository quality

- Spotless uses google-java-format.
- `spotless:check` remains part of Maven verify.
- `backend-format` runs `spotless:apply` and fails on a tracked diff.
- repository context/spec verification passes.

### Docker and Compose

Static checks pass for:

- root `docker-compose.yml`;
- split `compose/infrastructure.yml` plus `compose/backend/all.yml`;
- runtime Dockerfile;
- generic service Dockerfile with `MODULE=be-auth-api`;
- root backend Dockerfile.

Compose includes:

- one-shot `auth-keygen`;
- RSA private key Docker volume;
- read-only key mount into `be-auth-api`;
- public JWKS endpoint for Keycloak;
- no `AUTH_KEYCLOAK_CLIENT_SECRET` requirement for service client authentication.

## GitHub Actions status

Workflow run `31096424782` for implementation head `caac53f422013ac129b983168f429ebd1bd1d708` verified:

- `repository-quality`: success;
- `backend-format`: success;
- `backend-test`: success;
- `compose-validation`: success;
- `dockerfile-validation`: success.

The final `runtime-smoke` was not allocated by GitHub Actions because the account was blocked by a billing/spending-limit condition. This is an external CI infrastructure blocker and is **not** reported as a passing runtime test.

An earlier runtime attempt did execute far enough to expose the Spring constructor issue. That issue was reproduced by a deterministic RED context test and fixed. The full signed-JWT token exchange/runtime smoke must be rerun when Actions runner availability is restored.

## Security contracts

- `/api/v1/**`, `/private/**`, and `/internal/**` require authentication.
- Health/error/public-JWKS endpoints have explicit public semantics.
- `SERVICE_ACCOUNT` maps to `ActorType.SERVICE`.
- Effective permissions derive only from Keycloak effective roles prefixed `permission:`.
- Scope authorization uses path boundaries, not unsafe raw-prefix checks.
- Keycloak Admin REST uses a Client Credentials service token.
- Client authentication uses short-lived `private_key_jwt` instead of a shared secret.
- Private key material is runtime-mounted and must not be committed or logged.
- JWKS exposes public key material only.
- Redis has bounded TTL and fail-closed source-of-truth fallback.
- Kafka/job technical identity and business provenance remain separate concepts.

## Security study document

The repository contains a real binary OOXML/DOCX artifact at:

```text
docs/security/Keycloak_Authentication_Authorization_Deep_Dive_VI.docx
```

Publication evidence:

- Git blob SHA: `506e0ce3075f7b09763516cb5ee592cbeae5631c`;
- binary-fix commit: `a8a2ccd6c14732d539a3ec5a918723df16d20da4`;
- source artifact SHA-256 before upload: `f14bbc9b4b11a2ce9091eb200c0bcfaa8a7ad878cd9b9f80c153a6784916c4b2`;
- source artifact size: 7,876 bytes;
- the Git connector cannot decode the blob as UTF-8, confirming that it is stored as binary rather than base64 text;
- the source DOCX was opened as an OOXML ZIP and all 18 rendered pages were visually inspected before publication.

The reference covers Keycloak, OAuth/OIDC, session/cookie/JWT, `private_key_jwt`, mTLS/workload identity, RBAC/ABAC, Spring Security, Kafka/job/M2M, threats, tests, and implementation checklists.

## Delivery status

DW-002 remains in review through PR #3 against `develop`. The repository owner will squash the branch later. No merge into `master` is part of this delivery.
