# DW-002 Keycloak Authentication & Authorization Verification

## Scope verified

DW-002 delivers the authentication/authorization foundation only. It does not implement Ledger, Transfer, KYC, wallet-account, limits, fraud, settlement, or other future business-domain behavior.

Verified architecture:
- Keycloak remains the authentication and authorization source of truth.
- JWT carries compact identity/realm-role information; effective permissions and authorization scopes are resolved server-side.
- Keycloak composite realm roles model `role -> permission` aggregation.
- Keycloak group paths model hierarchical authorization scope.
- `be-auth-api` follows Hexagonal boundaries and owns no IAM database.
- Redis is a typed bounded-TTL cache only; cache failure falls through to Keycloak and never grants access.
- `be-auth-api` keeps one Keycloak service-account client; no separate admin client was introduced.
- machine authentication uses Client Credentials plus RFC 7523 `private_key_jwt`, not a shared client secret.
- Kafka consumers and scheduled jobs establish explicit local technical actors and obtain an M2M token only when crossing a protected downstream boundary.

## Branch and baseline

- Base branch: `develop`
- Base commit: `2f29e5bb9fc5981d96d813027d1a5a060373677f` (`feat: add DW-001 platform foundation`)
- Feature branch: `feature/platform-foundation`
- Current private-key-JWT implementation head before final documentation commits: `caac53f422013ac129b983168f429ebd1bd1d708`.
- TDD/intermediate commits intentionally remain on the branch because the owner requested to perform the final squash personally.
- No merge into `develop` or `master` is part of this verification.

## TDD evidence

### Service actor classification

RED:
- Commit: `55861fb1f5d124134edcbe87af7e9355965aee82`
- Workflow run: `31081888947`
- Expected failure: service-account JWT was classified as USER rather than SERVICE.

GREEN:
- Service-account JWT identity maps to `UserContext.ActorType.SERVICE` while preserving authenticated JWT semantics.
- Regression remains covered by `UserContextConfigurationTest`.

### Permission and scope authorization contracts

RED:
- Commit: `e3eed99dc0a6d84a989ee81648faefbb3b2ce82b`
- Workflow run: `31082955668`
- Expected failure occurred before `CurrentAuthorization` / `WalletAuthorization` existed.

GREEN:
- Permission checks fail closed.
- Scope checks support exact/descendant paths while rejecting prefix collisions such as `/bank/a` vs `/bank/abc`.
- Keycloak mapping/adapter, Redis cache, application resolution and self-service REST contracts are covered by tests.

### Cache starter auto-configuration regression

Runtime failure evidence:
- Workflow run: `31085372346`
- Real application startup exposed a missing `WalletCacheManager` because the cache starter did not publish Spring Boot `AutoConfiguration.imports` metadata.

RED regression:
- Commit: `ef88945fbdc478b7c39871c362457ca4172ccb87`
- Workflow run: `31086215453`.

GREEN:
- `be-platform-cache-starter` now publishes `WalletRedisCacheAutoConfiguration` through `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

### `private_key_jwt` service authentication

RED:
- Commit: `954271315effa0a293d056e854da2e353034c9d1`
- Workflow run: `31093667934`
- Backend tests failed because the signed assertion/JWKS/system-context contracts had been introduced before their production implementations existed.

GREEN contracts now cover:
- Client Credentials request includes `client_id`, `client_assertion_type`, and `client_assertion` and does not contain `client_secret`.
- assertion is RS256 signed with `iss=sub=client_id`, exact configured audience, short expiry, issue time and unique `jti`;
- assertion verifies with the paired RSA public key;
- public JWKS exposes configured `kid` and no private RSA `d` parameter;
- Keycloak realm requires `clientAuthenticatorType=client-jwt`, `use.jwks.url=true`, the auth API JWKS URL and service account, with no client-secret literal;
- local Compose creates/mounts private key material without committing it to Git.

### Spring internal-security startup regression

The first real private-key-JWT runtime attempt exposed a constructor-selection issue in `RsaPrivateKeyClientAssertionProvider`: Spring saw the production constructor plus the deterministic test constructor and attempted default construction.

RED regression:
- Commit: `9145f526b041a7ba05bf3e9abd573ad14ebc304c`
- Workflow run: `31096263118`
- `PlatformInternalSecurityAutoConfigurationTest` reproduced `No default constructor found` in an `ApplicationContextRunner`.

GREEN:
- production constructor is explicitly selected for Spring DI;
- implementation head: `caac53f422013ac129b983168f429ebd1bd1d708`;
- the regression test and full Maven verify pass.

### Kafka / scheduler technical contexts

Tests verify:
- `SystemUserContextFactory.forKafkaConsumer(serviceName)` -> `KAFKA_CONSUMER`;
- `.forScheduler(serviceName)` -> `SCHEDULER`;
- `.forService(serviceName)` -> `SERVICE`;
- technical contexts carry `SERVICE_ACCOUNT` and do not require an HTTP request/SecurityContext;
- `BaseDomainEventConsumer` exposes an explicit Kafka technical-context helper.

Security semantics:
- an event's initiating username/user id is provenance/business metadata, not a credential;
- a consumer/job does not manufacture an end-user token merely to run local logic;
- when a background executor calls a protected downstream service, it uses its M2M service token;
- on-behalf-of/user delegation remains a separate future requirement rather than an implicit fallback.

## Implementation verification

### Maven / tests

Verification command used by CI:

```bash
./mvnw -B -ntp -f backend/pom.xml verify
```

Latest verified results on the private-key-JWT implementation:
- `be-platform-starter`: **35 tests passed**;
- `be-platform-cache-starter`: **5 tests passed**;
- `be-auth-api`: **12 tests passed**;
- total: **52 tests passed**, 0 failures, 0 errors, 0 skipped;
- Maven reactor BUILD SUCCESS;
- executable `be-auth-api` Spring Boot JAR repackaging succeeds;
- Java: Temurin JDK 25.

Container-backed test coverage includes PostgreSQL and Redis Testcontainers.

### Formatting / quality

- Spotless uses google-java-format and `spotless:check` remains part of Maven verify.
- dedicated `backend-format` CI applies Spotless and fails if committed backend sources differ afterward.
- repository context/spec verification passes.

### Docker / Compose

Static checks pass for:
- root `docker-compose.yml`;
- split `compose/infrastructure.yml` + `compose/backend/all.yml`;
- BuildKit Dockerfile checks for runtime, generic `MODULE=be-auth-api`, and root backend Dockerfile.

The current Compose model includes:
- an `auth-keygen` one-shot container that creates RSA private key material into a Docker volume if absent;
- read-only key mount into `be-auth-api`;
- public JWKS endpoint used by Keycloak;
- no `AUTH_KEYCLOAK_CLIENT_SECRET` requirement for the `be-auth-api` service client.

## Latest GitHub Actions status

Workflow run `31096424782` for `caac53f422013ac129b983168f429ebd1bd1d708` verified:
- `repository-quality`: success;
- `backend-format`: success;
- `backend-test`: success;
- `compose-validation`: success;
- `dockerfile-validation`: success.

`runtime-smoke` on the final private-key-JWT head could not provide final E2E evidence because GitHub Actions did not allocate/run the job due to the account billing/spending-limit condition observed during this delivery. This is recorded as an external CI infrastructure blocker, **not** reported as a passing runtime test and **not** hidden as a code success.

Earlier runtime smoke for the pre-hardening client-secret implementation did pass the PostgreSQL/Redis/Keycloak/auth-API stack and authenticated `/me/*` flow. During private-key-JWT hardening, a real runtime attempt also successfully exposed the Spring constructor issue described above, which was converted into a deterministic RED regression test and fixed. A final signed-JWT token exchange/runtime smoke should be rerun when GitHub Actions runner availability is restored.

## Key security contracts verified

- `/api/v1/**`, `/private/**`, and `/internal/**` require authentication; infrastructure health/error/JWKS endpoints have explicit public semantics.
- SERVICE_ACCOUNT JWTs map to `ActorType.SERVICE`.
- Effective permissions derive only from Keycloak effective roles prefixed `permission:`.
- Scope authorization uses exact/descendant path boundaries rather than unsafe raw-prefix matching.
- Keycloak Admin REST uses a service access token acquired by Client Credentials.
- client authentication to the token endpoint uses short-lived signed JWT assertion (`private_key_jwt`) instead of a shared secret.
- private key material is runtime mounted and must not be committed/logged.
- Keycloak can discover only the public JWK; the JWKS response contains no private RSA parameter.
- local `.env` files are ignored from Git.
- Redis cache has bounded TTL (`PT2M` default), typed values, namespacing and source-of-truth fallback.
- cache/provider failure never becomes silent authorization success.
- Kafka/job technical actors are explicit; provenance and credential identity are kept conceptually separate.

## Security study document

The repository includes the Vietnamese deep-dive reference at:

```text
docs/security/Keycloak_Authentication_Authorization_Deep_Dive_VI.docx
```

The repository-optimized DOCX preserves the study content and was validated as a real OOXML ZIP package and rendered for visual QA before commit. Its SHA-256 is recorded after the binary Git object is published.

## Non-goals preserved

No Ledger, Transfer, KYC, wallet-account, business limits, fraud, settlement, or other future-domain implementation was introduced as part of DW-002.

## Delivery status

DW-002 remains in review through PR #3 against `develop`. TDD commits are intentionally left unsquashed for the repository owner to squash later. No merge into `master` is part of this delivery.
