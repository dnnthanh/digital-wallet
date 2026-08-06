# DW-002 private_key_jwt and Kafka System Context Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Harden DW-002 service authentication by replacing shared client secrets with RFC 7523 signed JWT client authentication, and make Kafka/scheduler technical execution context explicit while retaining M2M tokens for downstream calls.

**Architecture:** Keep a single Keycloak client `be-auth-api`. The client remains a service-account client, but authenticates to the token endpoint with `private_key_jwt` (`clientAuthenticatorType=client-jwt`) and exposes only its public JWK through a well-known endpoint so Keycloak can verify assertions. Kafka consumers and schedulers create explicit technical `UserContext` values (`KAFKA_CONSUMER` / `SCHEDULER`) rather than depending on HTTP request scope; when they call another protected service they use the same service-token provider. Business provenance such as initiating username may travel as event metadata, but is not used as the machine credential.

**Tech Stack:** Java 25, Spring Boot 4.1, Spring Security OAuth2/Jose, Nimbus JOSE JWT, Keycloak 26.7, Kafka, Docker Compose, Maven, JUnit 5, GitHub Actions.

## Global Constraints

- Target branch is `develop`; work remains on `feature/platform-foundation`.
- Final feature history must be squash-compressed to one commit on top of `develop`.
- Do not create a separate `be-auth-api-admin` client.
- Do not commit a private key, client secret, demo password, or other production credential.
- `be-auth-api` uses RFC 7523 signed JWT client authentication to obtain `client_credentials` tokens.
- The assertion uses `iss=sub=client_id`, `aud=token endpoint`, short expiration, unique `jti`, and RS256.
- Keycloak verifies client assertions with the public key/JWKS; only public key material may be exposed.
- Kafka consumers/schedulers use explicit technical actors and do not manufacture an end-user JWT merely to establish local context.
- Kafka/job code uses an M2M token only when a protected downstream HTTP/gRPC boundary requires one.
- Keep Spotless enforcement and the existing 1-commit PR invariant.
- Add the generated Keycloak/authentication deep-dive DOCX under project documentation.

---

### Task 1: RED - signed JWT service token contract

**Files:**
- Modify test: `backend/platform/be-platform-starter/src/test/java/com/dnnthanh/wallet/be/platform/security/ServiceTokenProviderTest.java`
- Modify later: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/security/ServiceTokenProvider.java`
- Modify later: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/config/InternalSecurityProperties.java`
- Create later: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/security/ClientAssertionProvider.java`
- Create later: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/security/RsaPrivateKeyClientAssertionProvider.java`

**Interfaces:**
- `ClientAssertionProvider.assertion(): String`
- `ServiceTokenProvider.token(): String`
- properties: `token-uri`, `client-id`, `private-key-location`, optional `key-id`, `assertion-ttl`.

- [ ] Write a failing test proving token requests contain `grant_type=client_credentials`, `client_id`, `client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer`, and `client_assertion`, with no `client_secret`.
- [ ] Write a failing test for a signed assertion whose `iss` and `sub` are the client id, `aud` is the exact token URI, expiration is short-lived, `jti` is present, and the JWT verifies with the paired RSA public key.
- [ ] Run the platform test workflow and observe RED before implementation.
- [ ] Implement the minimal assertion provider and update `ServiceTokenProvider`.
- [ ] Verify GREEN and retain token caching/expiry behavior.

### Task 2: RED - public JWKS and Keycloak realm contract

**Files:**
- Create test: `backend/services/be-auth-api/src/test/java/com/dnnthanh/wallet/be/auth/adapter/in/web/ClientJwksControllerTest.java`
- Modify test: `backend/services/be-auth-api/src/test/java/com/dnnthanh/wallet/be/auth/infrastructure/KeycloakRealmContractTest.java`
- Create later: `backend/services/be-auth-api/src/main/java/com/dnnthanh/wallet/be/auth/adapter/in/web/ClientJwksController.java`
- Create later: `backend/services/be-auth-api/src/main/java/com/dnnthanh/wallet/be/auth/infrastructure/config/ClientJwksConfiguration.java`
- Modify later: `infrastructure/keycloak/realm-digital-wallet.json`
- Modify later: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/security/SecurityConfiguration.java`

**Interfaces:**
- public endpoint: `GET /.well-known/wallet-client-jwks.json`
- Keycloak client: `clientAuthenticatorType=client-jwt`, `use.jwks.url=true`, `jwks.url=http://be-auth-api:8080/.well-known/wallet-client-jwks.json`, no `secret`.

- [ ] Write failing tests proving the JWKS endpoint exposes only RSA public material and contains the configured `kid`.
- [ ] Write failing realm-contract assertions for `client-jwt`, JWKS URL, service account enabled, and absence of client secret.
- [ ] Run tests and observe RED.
- [ ] Implement JWKS publication and realm configuration.
- [ ] Verify GREEN.

### Task 3: RED - Kafka and scheduler technical context

**Files:**
- Create test: `backend/platform/be-platform-starter/src/test/java/com/dnnthanh/wallet/be/platform/context/SystemUserContextFactoryTest.java`
- Modify test: `backend/platform/be-platform-starter/src/test/java/com/dnnthanh/wallet/be/platform/kafka/BaseDomainEventConsumerTest.java`
- Create later: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/context/SystemUserContextFactory.java`
- Modify later: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/kafka/BaseDomainEventConsumer.java`

**Interfaces:**
- `SystemUserContextFactory.forKafkaConsumer(String serviceName): UserContext`
- `SystemUserContextFactory.forScheduler(String serviceName): UserContext`
- `SystemUserContextFactory.forService(String serviceName): UserContext`
- `BaseDomainEventConsumer.systemUserContext(String serviceName): UserContext`

- [ ] Write failing tests for `KAFKA_CONSUMER`, `SCHEDULER`, and `SERVICE` actor creation.
- [ ] Write a failing consumer test proving Kafka code can explicitly obtain technical context without `SecurityContextHolder` or request scope.
- [ ] Run tests and observe RED.
- [ ] Implement the factory and consumer helper using `UserContext.system(...)`.
- [ ] Verify GREEN.

### Task 4: runtime key generation and deployment wiring

**Files:**
- Create: `infrastructure/keycloak/generate-auth-client-key.sh`
- Modify: `.env.example`
- Modify: `docker-compose.yml`
- Modify: `compose/backend/auth.yml`
- Modify: `compose/infrastructure.yml`
- Modify: `backend/services/be-auth-api/src/main/resources/application.yml`
- Modify: `.github/workflows/ci.yml`

**Interfaces:**
- generated private key path is mounted read-only into `be-auth-api`.
- Keycloak retrieves only public JWK from `be-auth-api`.
- CI runtime smoke generates an ephemeral RSA key and verifies client-credentials token acquisition with signed JWT.

- [ ] Add runtime-only RSA key generation; never commit the resulting key.
- [ ] Remove `AUTH_KEYCLOAK_CLIENT_SECRET` from application/Compose/CI requirements.
- [ ] Wire private-key path/key id and public JWKS URL.
- [ ] Run Compose/Docker/runtime smoke checks.

### Task 5: documentation and final verification

**Files:**
- Add: `docs/security/Keycloak_Authentication_Authorization_Deep_Dive_VI.docx`
- Modify: `.agent/specs/DW-002-auth-keycloak.md`
- Modify: `.agent/plans/2026-08-06-DW-002-auth-keycloak.md`
- Modify: `.agent/reports/DW-002-auth-keycloak-verification.md`
- Modify: PR #3 description.

- [ ] Add the generated deep-dive DOCX unchanged and record its SHA-256.
- [ ] Update DW-002 docs from client-secret terminology to signed JWT/private-key terminology and document Kafka/scheduler technical contexts.
- [ ] Run Maven verify, explicit Spotless gate, Compose validation, Dockerfile validation, and runtime smoke.
- [ ] Rebuild a single final commit with parent equal to current `develop` SHA and force-update `feature/platform-foundation`.
- [ ] Verify PR #3 remains open against `develop`, is exactly one commit ahead, and all final CI jobs are green.
