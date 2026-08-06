# DW-002 Keycloak Authentication & Authorization Verification

## Scope verified

DW-002 delivers the authentication/authorization foundation only. It does not implement Ledger, Transfer, KYC, wallet-account, limit, fraud, settlement, or other future business-domain behavior.

Verified architecture:
- Keycloak remains the authentication and authorization source of truth.
- JWT carries compact identity/realm-role information; effective permissions and authorization scopes are resolved server-side.
- Keycloak composite realm roles model `role -> permission` aggregation.
- Keycloak group paths model hierarchical authorization scope.
- `be-auth-api` follows Hexagonal boundaries and owns no IAM database.
- Redis is a typed bounded-TTL cache only; cache failure falls through to Keycloak and never grants access.
- Shared resource-server security, request identity, stable 401/403 handling, method-security primitives, correlation/observability, client credentials, PostgreSQL, Redis, Kafka, gRPC, Docker, Compose, and Testcontainers foundations reuse DW-001 platform contracts.

## Branch and baseline

- Base branch: `develop`
- Base commit: `2f29e5bb9fc5981d96d813027d1a5a060373677f` (`feat: add DW-001 platform foundation`)
- Feature branch: `feature/platform-foundation`
- Delivery history is squash-compressed before review.
- The RED/GREEN commit SHAs below are pre-squash TDD evidence from GitHub Actions and are intentionally not retained as separate feature-branch commits.

## TDD evidence

### Service actor classification

RED:
- Pre-squash commit: `55861fb1f5d124134edcbe87af7e9355965aee82`
- Workflow run: `31081888947`
- Expected failure observed: service-account JWT was classified as `USER` instead of `SERVICE`.

GREEN:
- Implemented Keycloak service identity classification while preserving authenticated JWT semantics.
- Regression remains in `UserContextConfigurationTest`.

### Permission and scope authorization contracts

RED:
- Pre-squash commit: `e3eed99dc0a6d84a989ee81648faefbb3b2ce82b`
- Workflow run: `31082955668`
- Expected failure observed during test compilation because the new `CurrentAuthorization` / `WalletAuthorization` contracts did not yet exist.

GREEN:
- Permission checks fail closed.
- Scope checks support exact/descendant paths while rejecting string-prefix collisions.
- Application authorization resolution, Keycloak mapping/adapter, typed Redis cache, self-service REST contracts, and realm fixtures are covered by tests.

### Runtime cache-starter discovery regression

Runtime failure evidence:
- Workflow run: `31085372346`
- Docker image build succeeded, but the real application failed to start because Spring could not find a `WalletCacheManager` bean.
- Root cause: `be-platform-cache-starter` had `WalletRedisCacheAutoConfiguration` but did not publish Spring Boot `AutoConfiguration.imports` metadata.

RED regression:
- Pre-squash commit: `ef88945fbdc478b7c39871c362457ca4172ccb87`
- Workflow run: `31086215453`
- Regression test proved the cache starter did not publish its auto-configuration metadata.

GREEN fix:
- Added `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` for `WalletRedisCacheAutoConfiguration`.
- Regression test now passes and the real Docker runtime stack starts successfully.

## Implementation verification

Pre-squash full verification completed successfully, including the final branch push run `31086960557` and PR run `31087279681`.

The squashed delivery commit is re-verified by the same CI workflow before review completion.

### Maven / tests

Main verification command:

```bash
./mvnw -B -ntp -f backend/pom.xml verify
```

Results:
- `be-platform-starter`: 27 tests, 0 failures, 0 errors, 0 skipped.
- `be-platform-cache-starter`: 5 tests, 0 failures, 0 errors, 0 skipped.
- `be-auth-api`: 11 tests, 0 failures, 0 errors, 0 skipped.
- Total: **43 tests passed**.
- Maven reactor: all four reactor projects SUCCESS.
- `be-auth-api` executable Spring Boot JAR was repackaged successfully.
- Java: Temurin JDK 25.

Container-backed tests passed:
- PostgreSQL Testcontainers smoke test.
- Redis Testcontainers integration test.

Formatting/quality:
- Spotless uses google-java-format 1.28.0 with AOSP style.
- Maven `verify` retains `spotless:check` as a final enforcement gate.
- CI also has a dedicated `backend-format` job that runs `spotless:apply` and then `git diff --exit-code -- backend`; if applying Spotless changes tracked backend sources, CI fails and tells the developer to commit the formatted result.
- This keeps the repository itself formatted without granting CI write access or creating auto-push workflow loops.
- Repository context/spec baseline verification passes.

### Docker and Compose

Static validation passed:
- root `docker-compose.yml` config validation.
- split `compose/infrastructure.yml` + `compose/backend/all.yml` config validation.
- BuildKit Dockerfile checks for platform runtime, generic service image with `MODULE=be-auth-api`, and root backend Dockerfile.

Runtime smoke passed:
- built the `be-auth-api` image using `backend/Dockerfile.service`.
- started PostgreSQL, Redis, Keycloak 26.7.0, and `be-auth-api` through Docker Compose.
- waited for `be-auth-api` `/actuator/health` successfully.
- obtained a Keycloak client-credentials token for `be-auth-api`.
- authenticated `GET /api/v1/me/profile` and verified the principal is `service-account-be-auth-api`, actor type `SERVICE`, and contains `SERVICE_ACCOUNT`.
- authenticated `GET /api/v1/me/permissions` successfully, exercising Keycloak Admin composite-role resolution and the typed authorization cache.
- authenticated `GET /api/v1/me/scopes` successfully, exercising Keycloak group-scope resolution and the typed authorization cache.
- Compose stack teardown completed successfully.

## Key security contracts verified

- `/api/v1/**`, `/private/**`, and `/internal/**` require authentication; health/error endpoints remain available for infrastructure needs.
- Service-account JWT identities map to `UserContext.ActorType.SERVICE`.
- Effective permission names are derived only from Keycloak effective roles prefixed with `permission:`; the prefix is not exposed to API consumers.
- Permission and scope sets are distinct and deterministic.
- Scope authorization is exact-path or descendant-path based, not unsafe raw-string-prefix matching.
- Keycloak Admin requests use a service bearer token supplied by the existing client-credentials provider.
- Keycloak client secret and local demo-user password are environment placeholders rather than committed secret literals.
- Local `.env` files are ignored from Git so developer secrets are not accidentally committed.
- Redis cache uses a bounded TTL (`PT2M` default), typed values, application namespacing, and source-of-truth fallback behavior.
- Backend authorization failure never becomes silent authorization success.

## Non-goals preserved

No Ledger, Transfer, KYC, wallet-account, business limits, fraud, settlement, or other future-domain implementation was added as part of DW-002.

## Delivery status

DW-002 is ready for pull-request review against `develop`. No merge into `master` is part of this feature delivery.
