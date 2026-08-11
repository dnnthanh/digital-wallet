# DW-004 Wallet Account Verification Report

## Scope

Feature: DW-004 — Wallet Account

Branch: `feature/wallet-account`

Base branch: `develop`

Starting `develop` SHA: `67ec968563c4ef73f19e5157cfa761e478de7e3a`

Verified code head before documentation-only closure: `34488077799909e0b72a3df1893bed639787bc32`

The feature intentionally does not implement Ledger, Balance Projection, Transfer, Hold/Capture/Reversal, Funding/Payout, Limits, Fraud, Reconciliation, Settlement, or Statement business behavior.

## Implemented behavior

- Hexagonal `be-wallet-account-api` bounded context.
- One wallet per `(userId, currency)` with database-enforced uniqueness.
- Opaque UUID wallet identity, immutable owner/scope/currency, initial `ACTIVE` lifecycle state, timestamps, and optimistic version metadata.
- No balance state or balance mutation in the Wallet Account model or migrations.
- KYC prerequisite resolved through DW-003 `GET /private/api/v1/kyc/me` before the local wallet write transaction.
- KYC failure/malformed response/subject mismatch fails closed with stable wallet errors.
- Effective permissions/scopes resolved through the DW-002 Auth API with the current bearer token; authorization dependency failure fails closed.
- Keycloak wallet permissions use `permission:WALLET_SELF_CREATE` and `permission:WALLET_SELF_READ`, composed into `role:wallet-user`.
- Public service contracts:
  - `POST /private/api/v1/wallets` — `WALLET_SELF_CREATE`;
  - `GET /private/api/v1/wallets/me` — `WALLET_SELF_READ`;
  - `GET /private/api/v1/wallets/{walletId}` — `WALLET_SELF_READ`, owner-scoped.
- PostgreSQL + Liquibase-owned `wallet_account` and `wallet_outbox_event` tables.
- Atomic wallet row + `WALLET_ACCOUNT_CREATED` transactional outbox row.
- Typed outbox payload contains wallet id, user id, currency, status, and created time only; it excludes balance, KYC details, authorization data, bearer tokens, and trace context.
- No direct Kafka publication inside the wallet transaction.
- Spring-managed HTTP clients use explicit configurable deadlines: connect `2s`, read `3s` by default.
- No blind retry was introduced for Auth or KYC calls.
- Wallet database bootstrap, root/split Compose wiring, Docker service build, and runtime smoke on host port `8083`.

## TDD evidence

### Core domain/application

RED: GitHub Actions run `31468954083` (`#437`). Repository/format/Compose/Docker gates were clean and `backend-test` failed at test compilation because the DW-004 production contracts did not yet exist.

GREEN: GitHub Actions run `31469339758` (`#438`) passed the core domain/application tests.

Covered behavior includes wallet creation invariants, currency normalization/allow-listing, KYC-before-write ordering, KYC failure preventing the transactional writer, ownership-scoped reads, not-found semantics, and deterministic list ordering.

### Persistence, KYC, REST, and outbox

RED: GitHub Actions run `31469972048` (`#440`) failed at test compilation for the intentionally missing persistence/outbox/KYC/controller adapters after all preceding repository gates were clean.

GREEN: GitHub Actions run `31470435071` (`#442`) passed after the minimum adapters/migrations were implemented.

Covered behavior includes PostgreSQL persistence, owner lookup, unique `(userId,currency)` conflict mapping, KYC fail-closed behavior, bearer propagation, controller mapping, required permission annotations, and minimal typed outbox creation.

### Effective authorization and Keycloak

RED: GitHub Actions run `31470771320` (`#444`) failed at test compilation for the intentionally missing Auth API authorization adapter.

GREEN: GitHub Actions run `31471210350` (`#446`) passed after Auth API effective-permission/scopes integration and Keycloak wallet permissions were implemented.

A later contract review found that the existing DW-002 mapper returns the exact suffix after `permission:`. Realm names were therefore corrected to `permission:WALLET_SELF_CREATE` / `permission:WALLET_SELF_READ`, and a regression test rejects the earlier lowercase naming shape.

### Transactional atomicity

`WalletAccountOutboxAtomicityIntegrationTest` uses PostgreSQL Testcontainers and verifies both sides of the local transaction:

- success persists one wallet row and one `WALLET_ACCOUNT_CREATED` outbox row;
- rollback removes both rows;
- serialized payload excludes scope, balance, permission, and scope-list leakage.

### HTTP dependency deadlines

A final reliability self-review identified that Auth/KYC calls were fail-closed but did not yet declare explicit HTTP deadlines.

RED: GitHub Actions run `31472494047` (`#452`) passed repository/format/Compose/Docker and failed `backend-test` on the new timeout configuration contract.

GREEN: the service now configures Spring-managed HTTP clients with environment-overridable connect/read deadlines and still performs no blind retry.

## Test coverage executed by Maven

Final Maven reactor verification includes the existing platform, cache, Auth, and Customer KYC suites plus the DW-004 suite. The DW-004 suite includes:

- `WalletAccountTest`;
- `WalletCurrencyPolicyTest`;
- `WalletAccountServiceImplementTest`;
- `WalletAccountWriteTransactionImplementTest`;
- `WalletAccountPersistenceAdapterIntegrationTest`;
- `WalletAccountOutboxAtomicityIntegrationTest`;
- `KycVerificationRestAdapterTest`;
- `AuthApiCurrentAuthorizationAdapterTest`;
- `WalletAccountControllerTest`;
- `WalletAccountSecurityIntegrationTest`;
- `KeycloakWalletRealmContractTest`;
- `WalletHttpClientConfigurationContractTest`.

The final code verification job completed successfully with zero Maven test failures/errors. The current GitHub connector does not expose a stable aggregate Surefire count in the normalized job result, so this report does not invent an aggregate number.

## Final code verification

GitHub Actions run `31472920548` (`#454`) on exact code head `34488077799909e0b72a3df1893bed639787bc32` completed successfully.

Jobs:

- `repository-quality`: SUCCESS;
- `backend-format`: SUCCESS;
- `backend-test`: SUCCESS;
- `compose-validation`: SUCCESS;
- `dockerfile-validation`: SUCCESS;
- `runtime-smoke`: SUCCESS.

The repository gate validates the DW-004 spec/plan/service/Compose presence, service scan boundaries, existing platform conventions, and explicitly rejects balance state in Wallet Account production code and migrations.

## Docker and runtime evidence

The final runtime smoke builds and starts:

- PostgreSQL;
- Redis;
- Keycloak;
- `be-auth-api`;
- `be-customer-kyc-api`;
- `be-wallet-account-api`.

It waits for health endpoints on:

- Auth API: `8081`;
- Customer KYC API: `8082`;
- Wallet Account API: `8083`.

The same runtime smoke also verifies Keycloak `private_key_jwt` service authentication and confirms the public JWKS contains the expected RSA public material without private-key data.

Compose validation covers both root `docker-compose.yml` and split `compose/infrastructure.yml` + `compose/backend/all.yml`. Dockerfile validation includes `MODULE=be-wallet-account-api` using the generic backend Docker pattern.

## Secrets and configuration

- `WALLET_DB_PASSWORD` remains blank in `.env.example` and required at runtime.
- Auth/KYC URLs, supported currencies, Keycloak issuer/JWK URLs, datasource settings, and HTTP deadlines are environment/YAML-backed.
- No wallet password/token/private key is committed.

## Environment note

The execution sandbox could not clone the repository over HTTPS because outbound DNS/network access was unavailable. No local Maven or Docker result is claimed. Branch writes, TDD commits, verification, runtime Docker builds, Testcontainers, and final evidence were performed through the connected GitHub repository and GitHub Actions runners.

## Merge policy

This feature targets `develop` only. It must not be merged directly into `master`.
