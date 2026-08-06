# DW-002 Keycloak Authentication & Authorization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add reusable Keycloak-centered authentication/authorization foundations and an authenticated `be-auth-api` self-service boundary without introducing business-domain logic or a competing IAM database.

**Architecture:** Extend DW-001 platform security only with stable reusable identity/policy primitives. Add `be-auth-api` as a Hexagonal service that resolves effective composite-role permissions and hierarchical group scopes from Keycloak through an output port, caches the typed read model in Redis with bounded TTL, and exposes `/api/v1/me/*` REST endpoints. Reuse existing platform response/error/tracing, client-credentials, Redis, Compose and Docker conventions.

**Tech Stack:** Java 25, Spring Boot 4.1.0, Spring Security OAuth2 Resource Server, Spring `RestClient`, Keycloak 26.7.0, Redis 7.4, Maven, JUnit 5, AssertJ, MockMvc, Testcontainers/platform cache starter, Docker Compose, GitHub Actions.

## Global Constraints

- Start from `develop`; work only on `feature/platform-foundation` as explicitly requested.
- Keycloak is the authority for authentication and authorization metadata; no custom IAM tables.
- JWT stays compact; effective permissions and scope trees are resolved server-side.
- Roles aggregate permissions through Keycloak composite roles; scopes use Keycloak group paths.
- Backend enforces permission + scope; frontend checks are UX only.
- Client/request errors use stable platform errors; dependency failures never silently authorize.
- Redis is CACHE only, typed, bounded TTL, namespaced by `spring.application.name`, and fail-open means retry source-of-truth Keycloak, not grant access.
- Use Spring-managed `RestClient.Builder`, platform tracing/correlation, platform `ObjectMapper`, and existing cache/security abstractions.
- No manual W3C trace header propagation and no token/secret logging.
- No Ledger/Transfer/KYC/wallet business implementation.
- Java formatting: Spotless 2.44.5 + google-java-format 1.28.0 AOSP.

---

### Task 1: Lock DW-002 specification and plan

**Files:**
- Create: `.agent/specs/DW-002-auth-keycloak.md`
- Create: `.agent/plans/2026-08-06-DW-002-auth-keycloak.md`

**Interfaces:**
- Consumes: accepted `ADR-002`, DW-001 platform rules.
- Produces: exact DW-002 scope, API contracts, failure rules, TDD checklist.

- [ ] **Step 1:** Commit this spec and plan before production code.
- [ ] **Step 2:** Re-read both files for placeholders, contradictions, scope leaks, and type-name inconsistencies.
- [ ] **Step 3:** Verify the branch head starts from current `develop`.

### Task 2: RED — platform identity and authorization policy contracts

**Files:**
- Create test: `backend/platform/be-platform-starter/src/test/java/com/dnnthanh/wallet/be/platform/security/WalletAuthorizationTest.java`
- Create test: `backend/platform/be-platform-starter/src/test/java/com/dnnthanh/wallet/be/platform/context/UserContextConfigurationTest.java`
- Modify later: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/context/UserContextConfiguration.java`
- Create later: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/security/CurrentAuthorization.java`
- Create later: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/security/WalletAuthorization.java`

**Interfaces:**
- `CurrentAuthorization.permissions(): Set<String>`
- `CurrentAuthorization.scopes(): Set<String>`
- `WalletAuthorization.hasPermission(String): boolean`
- `WalletAuthorization.hasScope(String): boolean`
- `WalletAuthorization.hasPermissionInScope(String, String): boolean`

- [ ] **Step 1: Write failing tests** proving missing permission/scope denies, exact scope matches, descendants match, `/bank/a` does not match `/bank/abc`, and configured service-account role maps actor type to `SERVICE`.
- [ ] **Step 2: Push RED commit** containing tests only.
- [ ] **Step 3: Verify RED in GitHub Actions**; backend-test must fail because the new production contracts do not exist, while repository/Compose/Docker checks remain unaffected.
- [ ] **Step 4: Add minimal production code** for `CurrentAuthorization`, `WalletAuthorization`, and service actor classification.
- [ ] **Step 5: Verify GREEN** with backend-test and existing platform tests.

### Task 3: RED — Hexagonal authorization resolution service

**Files:**
- Create module: `backend/services/be-auth-api/pom.xml`
- Create: `backend/services/be-auth-api/src/main/java/com/dnnthanh/wallet/be/auth/AuthApiApplication.java`
- Create: `.../application/port/in/GetMyProfileQuery.java`
- Create: `.../application/port/in/GetMyPermissionsQuery.java`
- Create: `.../application/port/in/GetMyScopesQuery.java`
- Create: `.../application/port/out/AuthorizationDirectoryPort.java`
- Create: `.../application/port/out/AuthorizationCachePort.java`
- Create: `.../application/model/EffectiveAuthorization.java`
- Create: `.../application/service/CurrentIdentityServiceImplement.java`
- Test: `.../application/service/CurrentIdentityServiceImplementTest.java`

**Interfaces:**
- `AuthorizationDirectoryPort.resolve(String userId): EffectiveAuthorization`
- `AuthorizationCachePort.find(String userId): Optional<EffectiveAuthorization>`
- `AuthorizationCachePort.put(String userId, EffectiveAuthorization value): void`
- `AuthorizationCachePort.evict(String userId): void`
- `EffectiveAuthorization(String userId, Set<String> permissions, Set<String> scopes)` with immutable sets.

- [ ] **Step 1: Write failing application tests** for cache-hit, cache-miss-provider-read-cache-write, Redis/cache exception fall-through, deterministic permission/scope ordering at API boundary, and no silent authorization success when provider fails.
- [ ] **Step 2: Push RED and observe expected failure.**
- [ ] **Step 3: Implement the minimal ports/model/service.** The application service depends only on `UserContext`, `AuthorizationDirectoryPort`, and `AuthorizationCachePort`; it imports no Keycloak/Redis adapter classes.
- [ ] **Step 4: Verify GREEN.**

### Task 4: RED — Keycloak Admin REST adapter and mapping

**Files:**
- Create: `.../infrastructure/config/KeycloakAuthorizationProperties.java`
- Create: `.../adapter/out/external/keycloak/rest/KeycloakAuthorizationAdapter.java`
- Create: `.../adapter/out/external/keycloak/rest/KeycloakRoleRepresentation.java`
- Create: `.../adapter/out/external/keycloak/rest/KeycloakGroupRepresentation.java`
- Test: `.../adapter/out/external/keycloak/rest/KeycloakAuthorizationAdapterTest.java`

**Interfaces:**
- Properties prefix: `wallet.auth.keycloak`
- Required values: `base-url`, `realm`; service token configuration continues to use `wallet.internal-security.*` from DW-001.
- Composite roles endpoint: `/admin/realms/{realm}/users/{userId}/role-mappings/realm/composite`
- Groups endpoint: `/admin/realms/{realm}/users/{userId}/groups`
- Permission role format: `permission:<code>`; expose `<code>` only.

- [ ] **Step 1: Write failing adapter tests** using Spring mock HTTP infrastructure bound to `RestClient.Builder`; assert bearer service token header, endpoint paths, permission prefix filtering, de-duplication/sorting, group path de-duplication/sorting, and malformed/null provider response failure.
- [ ] **Step 2: Push RED and observe expected failure.**
- [ ] **Step 3: Implement the adapter** with Spring-managed `RestClient.Builder` and existing `ServiceTokenProvider`; never log token/provider response bodies.
- [ ] **Step 4: Verify GREEN.**

### Task 5: RED — typed Redis authorization cache

**Files:**
- Create: `.../adapter/out/cache/RedisAuthorizationCacheAdapter.java`
- Create: `.../infrastructure/config/AuthCacheConfiguration.java`
- Test: `.../adapter/out/cache/RedisAuthorizationCacheAdapterTest.java`
- Modify: `backend/platform/be-platform-cache-starter/src/main/java/com/dnnthanh/wallet/be/platform/cache/WalletCacheNames.java` only if a stable technical auth-cache constant belongs in the shared cache starter; otherwise keep the auth cache name in the auth module configuration.

**Interfaces:**
- Cache name: `effective-authorization`
- Key: Keycloak user id
- Value type: `EffectiveAuthorization`
- TTL property: `wallet.auth.cache-ttl`, default `PT2M`.

- [ ] **Step 1: Write failing tests** asserting typed value use, get/put/evict mapping and TTL registration.
- [ ] **Step 2: Push RED and observe expected failure.**
- [ ] **Step 3: Implement adapter/config** using `WalletCacheManager` + `RedisCacheSpec`; never use `StringRedisTemplate` or manual JSON conversion.
- [ ] **Step 4: Verify GREEN including existing cache Testcontainers tests.**

### Task 6: RED — authenticated self-service REST APIs and reusable method security

**Files:**
- Create: `.../adapter/in/web/MeController.java`
- Create: `.../api/response/MyProfileResponse.java`
- Create: `.../api/response/MyPermissionsResponse.java`
- Create: `.../api/response/MyScopesResponse.java`
- Test: `.../adapter/in/web/MeControllerTest.java`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/security/SecurityConfiguration.java`

**Interfaces:**
- `GET /api/v1/me/profile`
- `GET /api/v1/me/permissions`
- `GET /api/v1/me/scopes`
- Return values through the existing platform API response envelope.

- [ ] **Step 1: Write failing MockMvc tests** for 401 without JWT, successful authenticated profile, permissions and scopes, and no credential/token fields in responses.
- [ ] **Step 2: Write failing method-security test** proving `@PreAuthorize("@walletAuthorization.hasPermission('self:read')")` returns 403 without effective permission and permits with it.
- [ ] **Step 3: Push RED and observe expected failure.**
- [ ] **Step 4: Implement controller/DTO wiring and tighten the platform filter chain** so `/api/v1/**`, `/private/**`, `/internal/**` are authenticated while health/error endpoints remain public.
- [ ] **Step 5: Verify GREEN.**

### Task 7: RED — Keycloak realm, service configuration and container topology

**Files:**
- Modify: `infrastructure/keycloak/realm-digital-wallet.json`
- Create: `backend/services/be-auth-api/src/main/resources/application.yml`
- Create: `backend/services/be-auth-api/src/test/java/com/dnnthanh/wallet/be/auth/infrastructure/KeycloakRealmContractTest.java`
- Create: `compose/backend/auth.yml`
- Modify: `compose/backend/all.yml`
- Modify: `docker-compose.yml`
- Modify: `.env.example`
- Modify: `backend/pom.xml`

**Realm contract:**
- confidential service-account client `be-auth-api` with secret `${AUTH_KEYCLOAK_CLIENT_SECRET}`;
- service account granted only the Keycloak management roles needed to view users, realm roles and groups;
- permission role `permission:self:read`;
- composite role `role:wallet-user` containing `permission:self:read`;
- hierarchical groups `/bank` -> `/bank/demo-branch`;
- local demo user assigned `role:wallet-user` and `/bank/demo-branch`; any local demo password is an environment placeholder, not a literal source secret.

- [ ] **Step 1: Write failing realm contract test** that parses the JSON and asserts placeholder-backed secrets/passwords, composite role mapping, hierarchy and client service-account settings.
- [ ] **Step 2: Push RED and observe expected failure.**
- [ ] **Step 3: Update realm/config/Compose/Maven wiring** using generic `backend/Dockerfile.service` and required environment-backed secrets.
- [ ] **Step 4: Verify GREEN** for Maven, repository-quality, root/split `docker compose config`, and Dockerfile BuildKit checks.

### Task 8: Full verification evidence and PR

**Files:**
- Create: `.agent/reports/DW-002-auth-keycloak-verification.md`
- Modify: `.agent/PLAN.MD` (`DW-002` -> `IN_REVIEW` after all gates pass).

- [ ] **Step 1: Run/inspect fresh full GitHub Actions** for repository-quality, backend-test, compose-validation, dockerfile-validation on the final branch head.
- [ ] **Step 2: If any check fails, read the failed job logs, identify root cause, add a failing regression test when behavior-related, implement the smallest fix, push and re-run.**
- [ ] **Step 3: Record exact final commit SHA, test/check results, Docker/Compose status and known non-goals in the verification report.**
- [ ] **Step 4: Open a PR from `feature/platform-foundation` to `develop`; never target or merge `master`.**
- [ ] **Step 5: Re-read the PR Actions status and only report completion when all required checks are green.**
