# DW-004 Wallet Account Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a Hexagonal Wallet Account service that opens one ACTIVE wallet per verified customer/currency, provides ownership-scoped reads, and atomically records a typed account-created outbox event without implementing balances, Ledger, Transfer, or later financial behavior.

**Architecture:** Add `be-wallet-account-api`. A non-transactional orchestration service resolves current identity, validates currency, and verifies KYC through DW-003 before invoking a separate transactional writer. PostgreSQL owns wallet identity/state and the local transactional outbox. REST adapters use the existing platform response/security conventions, and Keycloak remains the permission source of truth.

**Tech Stack:** Java 25, Spring Boot 4.1.0, Spring Security, Spring Data JPA, PostgreSQL 16, Liquibase, Spring `RestClient`, DW-003 KYC REST contract, MapStruct/Lombok, JUnit 5, AssertJ, MockMvc, Testcontainers PostgreSQL, Docker Compose, GitHub Actions.

## Global Constraints

- Work only on `feature/wallet-account`, created from the current `develop` head.
- Service artifact: `be-wallet-account-api`; package root: `com.dnnthanh.wallet.be.walletaccount`.
- Controllers call application ports, never repositories.
- Application/domain do not import JPA entities, Spring Security, KYC REST DTOs, or adapter classes.
- KYC is verified through DW-003; never query the KYC database.
- KYC network resolution completes before the wallet write transaction starts.
- One wallet per `(userId, currency)`; the database unique constraint is the race-safety boundary.
- Wallet row + account-created outbox row commit atomically.
- No balance fields, Ledger journals, Transfer/Hold/Funding/Limits/Fraud/Reconciliation/Settlement/Statement behavior.
- No direct Kafka publication in the wallet transaction.
- No Redis authority or wallet object cache in DW-004.
- Keycloak remains IAM/permission source of truth; no IAM tables.
- Supported currencies are YAML/environment-backed and normalized with JDK currency semantics.
- Inherit platform tracing/correlation; no manual W3C trace propagation.
- Java formatting remains Spotless `2.44.5` + google-java-format `1.28.0` AOSP.

---

### Task 1: Lock specification and branch

**Files:**
- Create: `.agent/specs/DW-004-wallet-account.md`
- Modify: `.agent/PLAN.MD`
- Create: `.agent/plans/2026-08-11-DW-004-wallet-account.md`

**Interfaces:**
- Branch: `feature/wallet-account`
- Base: current `develop`
- Service: `be-wallet-account-api`

- [x] **Step 1:** Create `feature/wallet-account` from current `develop` and verify the starting SHA matches `develop`.
- [x] **Step 2:** Commit the DW-004 specification before production code.
- [ ] **Step 3:** Commit this implementation plan before production code.
- [ ] **Step 4:** Self-review spec/plan for placeholders, balance leakage, cross-context DB access, network-inside-transaction wording, and incorrect branch/service names.

### Task 2: RED — module, domain, application, and KYC-gate contracts

**Files:**
- Modify: `backend/pom.xml`
- Create: `backend/services/be-wallet-account-api/pom.xml`
- Create tests:
  - `backend/services/be-wallet-account-api/src/test/java/com/dnnthanh/wallet/be/walletaccount/domain/WalletAccountTest.java`
  - `backend/services/be-wallet-account-api/src/test/java/com/dnnthanh/wallet/be/walletaccount/application/service/WalletAccountServiceImplementTest.java`
  - `backend/services/be-wallet-account-api/src/test/java/com/dnnthanh/wallet/be/walletaccount/application/service/WalletCurrencyPolicyTest.java`
- Create later production types:
  - `domain/WalletAccount.java`
  - `domain/WalletStatus.java`
  - `application/model/OpenWalletCommand.java`
  - `application/model/WalletAccountView.java`
  - `application/model/VerifiedKyc.java`
  - `application/port/in/OpenWalletAccountUseCase.java`
  - `application/port/in/GetMyWalletQuery.java`
  - `application/port/in/ListMyWalletsQuery.java`
  - `application/port/out/CurrentActorPort.java`
  - `application/port/out/KycVerificationPort.java`
  - `application/port/out/WalletAccountRepositoryPort.java`
  - `application/service/WalletCurrencyPolicy.java`
  - `application/service/WalletAccountServiceImplement.java`
  - `application/service/WalletAccountWriteTransaction.java`

**Interfaces:**
- `OpenWalletAccountUseCase.open(OpenWalletCommand): WalletAccountView`
- `GetMyWalletQuery.getMyWallet(UUID walletId): WalletAccountView`
- `ListMyWalletsQuery.listMyWallets(): List<WalletAccountView>`
- `CurrentActorPort.userId(): String`
- `KycVerificationPort.requireVerified(String expectedUserId): VerifiedKyc`
- `WalletAccountWriteTransaction.open(String userId, String scopePath, String currency): WalletAccount`
- `WalletAccountRepositoryPort.findByIdAndUserId(UUID walletId, String userId): Optional<WalletAccount>`
- `WalletAccountRepositoryPort.findAllByUserId(String userId): List<WalletAccount>`
- `WalletAccountRepositoryPort.save(WalletAccount wallet): WalletAccount`

- [ ] **Step 1: Write failing domain tests** proving a created account preserves wallet id/owner/scope/currency, starts `ACTIVE`, and contains no balance API.
- [ ] **Step 2: Write failing currency-policy tests** proving lowercase/whitespace normalizes to uppercase ISO code, malformed ISO codes fail, and configured-but-unsupported currencies map to `WALLET_UNSUPPORTED_CURRENCY`.
- [ ] **Step 3: Write failing application tests** proving KYC verification is required before the writer is invoked, verified KYC scope is passed into the writer, reads are owner scoped, and deterministic list results are returned.
- [ ] **Step 4: Push RED commit** with module/test scaffolding and confirm `backend-test` fails because production types/behavior do not exist.
- [ ] **Step 5: Do not add production behavior until RED is observed.**

### Task 3: GREEN — minimal domain/application behavior

**Files:** production types listed in Task 2 plus:
- Create: `backend/services/be-wallet-account-api/src/main/java/com/dnnthanh/wallet/be/walletaccount/exception/WalletAccountErrorCode.java`
- Create: `backend/services/be-wallet-account-api/src/main/java/com/dnnthanh/wallet/be/walletaccount/constant/WalletAccountInvariantMessages.java`
- Create: `backend/services/be-wallet-account-api/src/main/java/com/dnnthanh/wallet/be/walletaccount/infrastructure/WalletAccountProperties.java`

**Interfaces:**
- `WalletAccount.create(UUID walletId, String userId, String scopePath, String currency, Instant now)`
- `WalletCurrencyPolicy.normalizeSupported(String rawCurrency): String`
- Error codes exactly: `WALLET_NOT_FOUND`, `WALLET_KYC_REQUIRED`, `WALLET_KYC_UNAVAILABLE`, `WALLET_UNSUPPORTED_CURRENCY`, `WALLET_ALREADY_EXISTS`.

- [ ] **Step 1:** Implement the smallest aggregate/model/ports/error enum required by RED tests.
- [ ] **Step 2:** Implement `WalletCurrencyPolicy` using `StringUtils.trimToNull`, `Currency.getInstance`, and the configured supported-currency set.
- [ ] **Step 3:** Implement `WalletAccountServiceImplement.open` as non-transactional orchestration: resolve user -> normalize currency -> require verified KYC -> invoke transactional writer.
- [ ] **Step 4:** Implement ownership-scoped get/list application behavior.
- [ ] **Step 5:** Run/push and verify targeted unit tests become GREEN before persistence work.

### Task 4: RED/GREEN — PostgreSQL, Liquibase, duplicate race, and transactional outbox

**Files:**
- Create migrations:
  - `src/main/resources/db/changelog/db.changelog-master.yaml`
  - `src/main/resources/db/changelog/changes/001-create-wallet-account.yaml`
  - `src/main/resources/db/changelog/changes/002-create-wallet-outbox.yaml`
- Create persistence:
  - `adapter/out/persistence/entity/WalletAccountEntity.java`
  - `adapter/out/persistence/entity/WalletOutboxEntity.java`
  - `adapter/out/persistence/repository/WalletAccountJpaRepository.java`
  - `adapter/out/persistence/repository/WalletOutboxJpaRepository.java`
  - `adapter/out/persistence/WalletAccountPersistenceMapper.java`
  - `adapter/out/persistence/WalletAccountPersistenceAdapter.java`
  - `adapter/out/persistence/WalletOutboxPersistenceAdapter.java`
- Create event contract:
  - `application/event/WalletAccountCreatedPayload.java`
  - `application/port/out/WalletOutboxPort.java`
  - `constant/WalletAccountEventTypes.java`
- Create tests:
  - `adapter/out/persistence/WalletAccountPersistenceAdapterIntegrationTest.java`
  - `adapter/out/persistence/WalletAccountOutboxAtomicityIntegrationTest.java`

**Interfaces:**
- table `wallet_account(wallet_id,user_id,scope_path,currency,status,created_at,updated_at,version)` with unique `(user_id,currency)`;
- table `wallet_outbox_event(event_id,aggregate_id,event_type,payload,created_at,published_at,attempt_count)`;
- `WalletOutboxPort.appendCreated(WalletAccountCreatedPayload payload): void`.

- [ ] **Step 1: Write failing Testcontainers tests** proving migrations create both tables, no balance columns exist, save/load works, and duplicate `(user_id,currency)` maps to `WALLET_ALREADY_EXISTS`.
- [ ] **Step 2: Write failing atomicity test** proving successful open writes exactly one wallet and one `WALLET_ACCOUNT_CREATED` outbox row with no balance/KYC/auth fields.
- [ ] **Step 3:** Implement JPA/Liquibase adapters and MapStruct mapper.
- [ ] **Step 4:** Map database unique-constraint failure to `WALLET_ALREADY_EXISTS`; do not infer duplicate safety only in memory.
- [ ] **Step 5:** Implement `WalletAccountWriteTransaction` with `@Transactional`, repository save, and outbox append in the same transaction.
- [ ] **Step 6:** Verify Testcontainers tests GREEN.

### Task 5: RED/GREEN — KYC REST adapter, bearer/current actor adapters, REST API, and Spring Security

**Files:**
- Create KYC adapter:
  - `adapter/out/kyc/KycVerificationRestAdapter.java`
  - `adapter/out/kyc/KycApiResponse.java`
  - `adapter/out/kyc/KycResponseData.java`
- Create auth/context adapters:
  - `application/port/out/CurrentBearerTokenPort.java`
  - `adapter/out/auth/SecurityContextBearerTokenAdapter.java`
  - `adapter/out/auth/UserContextCurrentActorAdapter.java`
- Create API:
  - `api/request/OpenWalletRequest.java`
  - `api/response/WalletAccountResponse.java`
  - `adapter/in/web/WalletAccountController.java`
  - `adapter/in/web/mapper/WalletAccountApiMapper.java`
- Create configuration/application:
  - `WalletAccountApiApplication.java`
  - `infrastructure/WalletAccountConfiguration.java`
  - `src/main/resources/application.yml`
  - `src/main/resources/messages.properties`
  - `src/main/resources/messages_vi.properties`
- Create tests:
  - `adapter/out/kyc/KycVerificationRestAdapterTest.java`
  - `adapter/in/web/WalletAccountControllerTest.java`
  - `adapter/in/web/WalletAccountSecurityIntegrationTest.java`

**Interfaces:**
- KYC call: `GET /private/api/v1/kyc/me` with unchanged incoming `Authorization` header;
- response must have `data.userId`, `data.scopePath`, `data.status`;
- statuses other than `VERIFIED` -> `WALLET_KYC_REQUIRED`;
- transport/5xx/malformed/subject mismatch -> `WALLET_KYC_UNAVAILABLE`;
- POST `/private/api/v1/wallets` -> `WALLET_SELF_CREATE`;
- GET `/private/api/v1/wallets/me` and `/private/api/v1/wallets/{walletId}` -> `WALLET_SELF_READ`.

- [ ] **Step 1: Write failing KYC adapter tests** for verified response, non-verified response, downstream failure, malformed body, and subject mismatch.
- [ ] **Step 2: Write failing controller tests** for request mapping/response envelope and use-case delegation.
- [ ] **Step 3: Write failing security tests** proving unauthenticated requests are 401 and authenticated requests missing permission are 403.
- [ ] **Step 4:** Implement adapters/config/controller with Spring-managed `RestClient` and platform security/user context.
- [ ] **Step 5:** Verify targeted tests GREEN.

### Task 6: Realm, database bootstrap, Compose, Docker, and CI integration

**Files:**
- Modify: `infrastructure/keycloak/realm-digital-wallet.json`
- Create: `infrastructure/postgres/init/003-wallet-account.sh`
- Create: `compose/backend/wallet-account.yml`
- Modify: `compose/backend/all.yml`
- Modify: `compose/infrastructure.yml` only if database bootstrap wiring requires it
- Modify: `docker-compose.yml`
- Modify: `.env.example`
- Modify: `.github/workflows/ci.yml`
- Modify: `verification/verify_repository.py`
- Create test: `src/test/java/com/dnnthanh/wallet/be/walletaccount/infrastructure/KeycloakWalletRealmContractTest.java`

**Interfaces:**
- DB defaults: host `postgres`, database `wallet_account_db`, user `wallet_account`, password from `WALLET_DB_PASSWORD`;
- service port: host `8083` -> container `8080`;
- KYC base URL in Compose: `http://be-customer-kyc-api:8080`;
- permissions: `permission:wallet:self:create`, `permission:wallet:self:read` composed into `role:wallet-user`.

- [ ] **Step 1: Write failing realm contract test** proving both permissions exist and are composites of `role:wallet-user`.
- [ ] **Step 2:** Add wallet database bootstrap and service Compose fragment using blank/example secrets only.
- [ ] **Step 3:** Add runtime env/config and root/split Compose aggregation.
- [ ] **Step 4:** Extend Dockerfile CI checks with `MODULE=be-wallet-account-api`.
- [ ] **Step 5:** Extend runtime smoke to build/start Wallet Account and wait for `http://localhost:8083/actuator/health`.
- [ ] **Step 6:** Extend repository verification so the new runnable service is subject to existing scan/package/application-name guardrails.

### Task 7: Full verification, evidence, PR, and CI repair loop

**Files:**
- Create: `.agent/reports/DW-004-wallet-account-verification.md`
- Modify: `.agent/PLAN.MD` only after verification evidence supports the final status.

**Verification commands represented by CI:**
- `python3 verification/verify_repository.py`
- `./mvnw -B -ntp -f backend/pom.xml spotless:apply` followed by clean diff requirement
- `./mvnw -B -ntp -f backend/pom.xml verify`
- `docker compose -f docker-compose.yml config`
- `docker compose --project-directory . -f compose/infrastructure.yml -f compose/backend/all.yml config`
- BuildKit checks for runtime, auth, KYC, wallet-account, and root Dockerfiles
- runtime smoke for PostgreSQL, Redis, Keycloak, Auth, KYC, and Wallet Account

- [ ] **Step 1:** Push implementation head and inspect every GitHub Actions job.
- [ ] **Step 2:** For any failure, read the failing job steps/logs, identify root cause, add/revise a regression test when behavior is involved, and push the smallest fix.
- [ ] **Step 3:** Repeat until the final head has all required CI jobs green or an external infrastructure blocker is proven and documented accurately.
- [ ] **Step 4:** Write verification evidence with exact commit SHA, test counts, Compose/Docker results, and workflow run/job status.
- [ ] **Step 5:** Open PR `feature/wallet-account` -> `develop`; never target or merge `master`.
