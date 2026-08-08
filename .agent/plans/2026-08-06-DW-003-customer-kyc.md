# DW-003 Customer KYC Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the Customer KYC bounded context required by later Wallet Account flows, with secure identity-data handling, explicit lifecycle invariants, Keycloak permission/scope enforcement, PostgreSQL persistence, and transactional outbox evidence.

**Architecture:** Add `be-customer-kyc-api` as a Hexagonal Spring Boot service. Domain/application own the KYC state machine; PostgreSQL adapters own JPA/Liquibase persistence; a request-scoped auth adapter reuses DW-002 `/api/v1/me/permissions` and `/scopes` to build platform `WalletAuthorization`; sensitive document numbers are HMAC-fingerprinted before persistence; status changes atomically persist a local outbox row.

**Tech Stack:** Java 25, Spring Boot 4.1.0, Spring Security, Spring Data JPA, PostgreSQL, Liquibase, Spring `RestClient`, Keycloak/DW-002 Auth API, JUnit 5, AssertJ, MockMvc, Testcontainers PostgreSQL, MapStruct/Lombok, Docker Compose, GitHub Actions.

## Global Constraints

- Start from current `develop`; work on `feature/customer-kyc` from the feature index.
- Do not reuse the historical `feature/platform-foundation` branch for DW-003.
- Keycloak remains IAM/authorization source of truth; no IAM tables in KYC.
- Raw government document numbers are request-only and must never be persisted, logged, cached, returned, or emitted to Kafka/outbox payloads.
- Persist HMAC-SHA256 fingerprint + last four normalized characters; `KYC_DOCUMENT_HMAC_SECRET` has no source default.
- PostgreSQL is KYC source of truth; Liquibase migrations are append-only.
- Controllers call application ports, never repositories.
- Application/domain do not import JPA entities, Spring Security, Keycloak representations, or REST DTOs.
- Permission checks use DW-002 effective permissions; reviewer data access additionally enforces hierarchical scope.
- Do not duplicate the DW-002 Redis authorization cache in KYC.
- Network authorization resolution occurs before transactional KYC writes; no network call is intentionally introduced inside the KYC database transaction.
- Status transition + KYC row + outbox row are atomic.
- No direct in-transaction Kafka publish.
- Inherit platform tracing; no manual `traceparent`/`tracestate` or custom trace spans.
- Java formatting remains Spotless `2.44.5` + google-java-format `1.28.0` AOSP.
- No Ledger, Transfer, Wallet Account, OCR, liveness, AML/sanctions, third-party KYC provider, or document-binary storage implementation.

---

### Task 1: Lock specification, branch, and module boundary

**Files:**
- Create: `.agent/specs/DW-003-customer-kyc.md`
- Create: `.agent/plans/2026-08-06-DW-003-customer-kyc.md`
- Later modify: `backend/pom.xml`
- Later create: `backend/services/be-customer-kyc-api/pom.xml`

**Interfaces:**
- Feature branch: `feature/customer-kyc`
- Service artifact: `be-customer-kyc-api`
- Package root: `com.dnnthanh.wallet.be.kyc`

- [ ] **Step 1:** Verify `feature/customer-kyc` points to the current `develop` head before feature commits.
- [ ] **Step 2:** Commit the spec before production code.
- [ ] **Step 3:** Commit this implementation plan before production code.
- [ ] **Step 4:** Self-review spec and plan for placeholders, cross-context DB access, raw-document leakage, inconsistent status names, and incorrect branch references.

### Task 2: RED — domain lifecycle and sensitive-document contracts

**Files:**
- Create test: `backend/services/be-customer-kyc-api/src/test/java/com/dnnthanh/wallet/be/kyc/domain/CustomerKycTest.java`
- Create test: `backend/services/be-customer-kyc-api/src/test/java/com/dnnthanh/wallet/be/kyc/adapter/out/security/HmacDocumentFingerprintAdapterTest.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/domain/CustomerKyc.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/domain/KycStatus.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/domain/KycDocumentType.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/domain/KycReviewDecision.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/model/DocumentFingerprint.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/port/out/DocumentFingerprintPort.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/adapter/out/security/HmacDocumentFingerprintAdapter.java`

**Interfaces:**
- `DocumentFingerprintPort.fingerprint(String rawDocumentNumber): DocumentFingerprint`
- `DocumentFingerprint(String fingerprint, String last4)`
- `CustomerKyc.createDraft(UUID kycId, String userId, String scopePath, KycDraft draft, Instant now)`
- `CustomerKyc.updateDraft(KycDraft draft, Instant now)`
- `CustomerKyc.submit(Instant now)`
- `CustomerKyc.review(KycReviewDecision decision, String reviewerUserId, String rejectionReasonCode, Instant now)`

- [ ] **Step 1: Write failing lifecycle tests** proving creation starts at `DRAFT`, submit changes to `PENDING_REVIEW`, pending/verified cannot be edited, rejected edit returns to `DRAFT` and clears review metadata, only pending can be reviewed, reject requires reason, and self-review fails.
- [ ] **Step 2: Write failing sensitive-data tests** proving the HMAC result is deterministic, different secrets produce different fingerprints, output exposes last four only, and blank secret configuration is rejected.
- [ ] **Step 3: Push RED commit** with module test scaffolding/tests and observe `backend-test` fail because production types are absent.
- [ ] **Step 4: Implement minimal domain enums/aggregate, fingerprint port/model, HMAC adapter, and `wallet.kyc.security.document-hmac-secret` configuration.
- [ ] **Step 5: Verify GREEN** for the targeted module tests in GitHub Actions before adding persistence behavior.

### Task 3: RED — PostgreSQL model, Liquibase, repository, and optimistic concurrency

**Files:**
- Create test: `backend/services/be-customer-kyc-api/src/test/java/com/dnnthanh/wallet/be/kyc/adapter/out/persistence/CustomerKycPersistenceAdapterIT.java`
- Create: `backend/services/be-customer-kyc-api/src/main/resources/db/changelog/db.changelog-master.yaml`
- Create: `backend/services/be-customer-kyc-api/src/main/resources/db/changelog/changes/001-create-customer-kyc.yaml`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/port/out/CustomerKycRepositoryPort.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/adapter/out/persistence/entity/CustomerKycEntity.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/adapter/out/persistence/repository/CustomerKycJpaRepository.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/adapter/out/persistence/CustomerKycPersistenceMapper.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/adapter/out/persistence/CustomerKycPersistenceAdapter.java`

**Interfaces:**
- `CustomerKycRepositoryPort.findByUserId(String): Optional<CustomerKyc>`
- `CustomerKycRepositoryPort.findById(UUID): Optional<CustomerKyc>`
- `CustomerKycRepositoryPort.save(CustomerKyc): CustomerKyc`
- unique DB constraints: `uk_customer_kyc_user_id`, `uk_customer_kyc_document_fingerprint`
- optimistic version column: `version bigint not null`

- [ ] **Step 1: Write failing Testcontainers integration tests** for Liquibase startup, save/load round-trip, unique `user_id`, unique `document_fingerprint`, and stale optimistic version conflict.
- [ ] **Step 2: Push RED and inspect the expected persistence failures.**
- [ ] **Step 3: Implement Liquibase migration, JPA entity/repository, MapStruct mapper, and adapter.**
- [ ] **Step 4: Map duplicate document fingerprint to `KYC_DOCUMENT_ALREADY_EXISTS`; do not relabel unrelated persistence failures as client errors.**
- [ ] **Step 5: Verify GREEN** with PostgreSQL Testcontainers.

### Task 4: RED — transactional application use cases and outbox atomicity

**Files:**
- Create test: `backend/services/be-customer-kyc-api/src/test/java/com/dnnthanh/wallet/be/kyc/application/service/CustomerKycServiceImplementTest.java`
- Create integration test: `backend/services/be-customer-kyc-api/src/test/java/com/dnnthanh/wallet/be/kyc/adapter/out/persistence/KycOutboxAtomicityIT.java`
- Create: `backend/services/be-customer-kyc-api/src/main/resources/db/changelog/changes/002-create-kyc-outbox.yaml`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/model/KycDraft.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/model/KycView.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/model/KycDecisionCommand.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/event/KycStatusChangedPayload.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/port/in/GetMyKycQuery.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/port/in/UpsertMyKycDraftUseCase.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/port/in/SubmitMyKycUseCase.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/port/in/GetKycReviewQuery.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/port/in/ReviewKycUseCase.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/port/out/KycAuthorizationPort.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/port/out/KycOutboxPort.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/application/service/CustomerKycServiceImplement.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/adapter/out/persistence/entity/KycOutboxEventEntity.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/adapter/out/persistence/repository/KycOutboxJpaRepository.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/adapter/out/persistence/KycOutboxPersistenceAdapter.java`

**Interfaces:**
- `KycAuthorizationPort.requireCustomerScope(): String`
- `KycAuthorizationPort.hasScope(String requiredScope): boolean`
- `KycOutboxPort.appendStatusChanged(UUID kycId, KycStatusChangedPayload payload, Instant createdAt): void`
- `KycStatusChangedPayload(UUID kycId, String userId, KycStatus previousStatus, KycStatus currentStatus, Instant changedAt)`
- outbox event type: `CUSTOMER_KYC_STATUS_CHANGED`

- [ ] **Step 1: Write failing application tests** for owner-only self operations, first draft creation, rejected-profile edit reset, submit, reviewer scope denial, self-review denial, verify/reject transitions, and status-change outbox calls.
- [ ] **Step 2: Write failing integration test** proving a status transition rollback removes both KYC state and outbox row, while success commits both.
- [ ] **Step 3: Push RED and inspect expected failures.**
- [ ] **Step 4: Implement application service and outbox persistence using platform `OutboxPayloadCodec`; the outbox payload contains only identifiers/status/timestamp.**
- [ ] **Step 5: Annotate write methods with local `@Transactional`; resolve request authorization before entering those writes through the inbound/request-scoped adapter path.**
- [ ] **Step 6: Verify GREEN** for unit + Postgres atomicity tests.

### Task 5: RED — reuse DW-002 effective authorization in the KYC service

**Files:**
- Create test: `backend/services/be-customer-kyc-api/src/test/java/com/dnnthanh/wallet/be/kyc/adapter/out/auth/AuthApiCurrentAuthorizationAdapterTest.java`
- Create test: `backend/services/be-customer-kyc-api/src/test/java/com/dnnthanh/wallet/be/kyc/infrastructure/KycAuthorizationConfigurationTest.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/adapter/out/auth/AuthApiCurrentAuthorizationAdapter.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/adapter/out/auth/AuthPermissionsResponse.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/adapter/out/auth/AuthScopesResponse.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/infrastructure/KycAuthorizationConfiguration.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/infrastructure/KycAuthProperties.java`

**Interfaces:**
- Auth API base URL property: `wallet.kyc.auth-api-base-url`
- adapter calls `GET /api/v1/me/permissions` and `GET /api/v1/me/scopes`
- inbound bearer token is relayed only in the HTTP Authorization header and never logged/stored
- request-scoped bean name: `walletAuthorization`

- [ ] **Step 1: Write failing HTTP adapter tests** proving bearer relay, both endpoint paths, distinct/sorted snapshot mapping, and dependency failures mapping to `KYC_AUTHORIZATION_UNAVAILABLE`.
- [ ] **Step 2: Write failing request-scope tests** proving one effective snapshot supplies both permission checks and `KycAuthorizationPort` scope checks, `/bank` authorizes `/bank/demo-branch`, and `/bank/a` does not authorize `/bank/abc`.
- [ ] **Step 3: Push RED and inspect expected failures.**
- [ ] **Step 4: Implement with Spring-managed `RestClient.Builder`; do not instantiate `RestClient.create` and do not add manual trace headers.**
- [ ] **Step 5: Verify GREEN** and ensure KYC owns no Redis auth cache.

### Task 6: RED — REST contracts, error codes, and method security

**Files:**
- Create test: `backend/services/be-customer-kyc-api/src/test/java/com/dnnthanh/wallet/be/kyc/adapter/in/web/KycControllerTest.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/adapter/in/web/KycController.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/api/request/UpsertKycDraftRequest.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/api/request/KycReviewDecisionRequest.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/api/response/KycResponse.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/exception/KycErrorCode.java`
- Create later: `backend/services/be-customer-kyc-api/src/main/resources/messages.properties`

**Interfaces:**
- `GET /api/v1/kyc/me`
- `PUT /api/v1/kyc/me/draft`
- `POST /api/v1/kyc/me/submit`
- `GET /api/v1/kyc/reviews/{kycId}`
- `POST /api/v1/kyc/reviews/{kycId}/decision`
- permissions: `kyc:self:read`, `kyc:self:write`, `kyc:self:submit`, `kyc:review`

- [ ] **Step 1: Write failing MockMvc tests** for 401 unauthenticated access, 403 missing permission, validation errors, self draft response masking, submit contract, review permission, and stable KYC error responses.
- [ ] **Step 2: Push RED and inspect expected failures.**
- [ ] **Step 3: Implement request/response DTOs, controller, `@PreAuthorize` expressions against `walletAuthorization`, KYC error codes, and message keys.**
- [ ] **Step 4: Verify responses never contain raw document number, fingerprint, HMAC secret, or full authorization token.**
- [ ] **Step 5: Verify GREEN** for controller/security tests.

### Task 7: RED — runtime wiring, Keycloak realm, Compose, and Docker/CI topology

**Files:**
- Create test: `backend/services/be-customer-kyc-api/src/test/java/com/dnnthanh/wallet/be/kyc/infrastructure/KeycloakKycRealmContractTest.java`
- Create: `backend/services/be-customer-kyc-api/src/main/java/com/dnnthanh/wallet/be/kyc/CustomerKycApiApplication.java`
- Create: `backend/services/be-customer-kyc-api/src/main/resources/application.yml`
- Create: `compose/backend/customer-kyc.yml`
- Modify: `compose/backend/all.yml`
- Modify: `docker-compose.yml`
- Modify: `.env.example`
- Modify: `backend/pom.xml`
- Modify: `infrastructure/keycloak/realm-digital-wallet.json`
- Modify: `.github/workflows/ci.yml`

**Realm contract:**
- add `permission:kyc:self:read`, `permission:kyc:self:write`, `permission:kyc:self:submit`, `permission:kyc:review`;
- `role:wallet-user` composites the three self permissions;
- add composite `role:kyc-reviewer` containing `permission:kyc:review`;
- existing `/bank` hierarchy remains the review scope model;
- no new password/client secret literal is committed.

**Runtime contract:**
- service name `be-customer-kyc-api`;
- container internal port `8080`, host port `8082`;
- PostgreSQL connection from environment;
- `AUTH_API_BASE_URL=http://be-auth-api:8080` in Compose;
- `KYC_DOCUMENT_HMAC_SECRET` required from environment;
- resource-server issuer/JWK configuration follows existing auth service pattern.

- [ ] **Step 1: Write failing realm contract test** for permissions/composite roles and absence of raw secret literals.
- [ ] **Step 2: Push RED and inspect expected failure.**
- [ ] **Step 3: Wire module POM, application config, datasource/Liquibase, Keycloak realm, Compose service, root aggregation, and `.env.example`.**
- [ ] **Step 4: Extend Dockerfile CI validation with `MODULE=be-customer-kyc-api`; keep existing auth validation.**
- [ ] **Step 5: Verify GREEN** for repository-quality, backend-format, backend-test, compose-validation, and dockerfile-validation.

### Task 8: Final verification evidence and PR

**Files:**
- Create: `.agent/reports/DW-003-customer-kyc-verification.md`
- Modify: `.agent/PLAN.MD` (`DW-003` -> `IN_REVIEW` only after implementation gates pass)

- [ ] **Step 1: Inspect a fresh GitHub Actions run for the final branch head.**
- [ ] **Step 2: If CI fails, fetch failed job steps/logs, identify root cause, add a regression test when behavior-related, implement the smallest fix, and push again.**
- [ ] **Step 3: Record branch base/head SHA, commits, exact test/check results, Docker/Compose validation, external blockers if any, and non-goals in the verification report.**
- [ ] **Step 4: Open PR `feature/customer-kyc -> develop`; do not target or merge `master`.**
- [ ] **Step 5: Re-read final PR/Actions status and report only verified outcomes.