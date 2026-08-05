# AGENTS.MD

## Purpose

This repository implements a production-oriented Digital Wallet / Banking Platform. Optimize for financial correctness, security, auditability, operability, maintainability, and learning value rather than code volume.

**The E-commerce repository is reference-only. Never introduce marketplace concepts such as catalog/cart/checkout/seller/shop unless a wallet requirement explicitly needs an analogous merchant capability.**

## Branch policy

- `master` is production-ready only.
- `develop` is the integration/core branch.
- Every change starts from `develop` on `feature/*`, `fix/*`, or `chore/*`.
- No direct feature implementation on `master` or `develop`.
- A feature may merge into `develop` only after its spec, implementation plan, tests, verification evidence, review, and CI checks are complete.

## Mandatory workflow

1. Read `AGENTS.MD`.
2. Read `.agent/PLAN.MD`.
3. Read `.agent/CONVENTIONS.MD`.
4. Read the relevant `.agent/specs/DW-xxx-*.md`.
5. Read applicable ADRs under `docs/adr/`.
6. If the spec is incomplete, update/review the spec before implementation.
7. Create `feature/<feature-name>` from `develop`.
8. Write/update tests first for behavior changes.
9. Implement the smallest coherent design satisfying the spec.
10. Run the feature quality gate and store evidence under `.agent/reports/`.
11. Update docs/ADR when implementation changes an architectural decision.
12. Push branch and open a PR into `develop`.
13. Inspect GitHub Actions; fix failures before declaring the feature complete.

## Architecture rules

- Use DDD where domain invariants justify rich models.
- Follow Clean/Hexagonal boundaries: inbound adapter -> application/use case -> domain -> port -> outbound adapter.
- Domain/application must not depend on persistence entities or adapter classes.
- Controllers must not call repositories directly.
- A bounded context must never access another bounded context's database.
- Output ports hide protocol details from application code.
- Internal synchronous core-money calls prefer gRPC when the call is typed, latency-sensitive, and service-to-service.
- Public/mobile/admin APIs and third-party HTTP integrations use REST/JSON.
- Domain events and asynchronous projections/recovery use Kafka.
- Never implement runtime `transport: REST|GRPC` switching for production architecture.
- Do not create a shared business-domain library coupling bounded contexts. Shared libraries may contain stable technical conventions/contracts only.
- Backend deployables/artifacts start with `be-`.
- Java package roots use `com.dnnthanh.wallet.be.<context>...`.

## Naming and hexagonal conventions

- Input interfaces: `application/port/in/*UseCase` or `*Query`.
- Implementations: `application/service/*ServiceImplement`.
- Output interfaces: `application/port/out/*Port`.
- Outbound internal adapters: `adapter/out/internal/<target>/grpc|rest/*Adapter`.
- External providers: `adapter/out/external/<provider>/<protocol>/*Adapter`.
- Inbound service calls: `adapter/in/internal/grpc|rest/...`.
- Webhooks: `adapter/in/webhook/...`.
- Kafka consumers: `adapter/in/messaging/kafka/...`.
- Web request/response DTOs stay under `api/request` and `api/response`; application code must not import them.

## Financial correctness rules

- Double-entry ledger is the source of truth for posted money movement.
- Every posted journal must satisfy `sum(debit) == sum(credit)` in one atomic database transaction.
- Never directly mutate a wallet balance as the sole representation of a financial transaction.
- Balance projection is a read model; it must not authorize a critical debit when stale data can cause overspend.
- Every money-changing command requires idempotency semantics.
- Ambiguous external outcomes are reconciled before unsafe retries.
- Manual operations affecting money create ledger entries plus immutable audit evidence; operators never "fix" money by direct SQL updates.

## Authentication / authorization rules

- Keycloak/OIDC is the authority for authentication and authorization metadata.
- Roles aggregate permissions via Keycloak composite roles.
- Authorization scope is modeled using a Keycloak group/tree hierarchy appropriate to bank/organization/branch/merchant scopes.
- JWT remains intentionally compact; do not embed complete permission lists or large scope trees.
- Expose self-service APIs for profile, effective permissions, and scopes.
- Backend always enforces permission + scope; frontend checks are UX only.
- Service-to-service authentication uses Keycloak client credentials/service identities.
- Audit distinguishes USER, SERVICE, SCHEDULER, and KAFKA_CONSUMER actors.

## Reliability rules

- No blind retry.
- Each dependency defines deadline/timeout, retryability, backoff+jitter, circuit breaker/bulkhead when appropriate, and ambiguous-outcome handling.
- gRPC callers set deadlines.
- DB state + event publication uses Transactional Outbox where atomicity is required.
- Kafka consumers are idempotent; duplicate delivery is normal.
- Async failure must have retry/DLT/replay/reconciliation/manual recovery where required.

## API and error rules

- Public HTTP contracts are versioned under `/api/v1/**`.
- Current-user APIs include `/api/v1/me/profile`, `/api/v1/me/permissions`, `/api/v1/me/scopes`.
- Use stable error codes; never force clients to parse free-form messages.
- Map errors consistently between REST and gRPC status semantics.

## Data ownership

- Each bounded context owns its schema/tables and migrations.
- Cross-context reads go through contracts/events, never direct SQL.
- PostgreSQL is the authoritative relational store unless a feature spec says otherwise.
- Redis must declare its exact role (CACHE, STATE_STORE, LOCK, COUNTER, RATE_LIMITER).

## Testing and evidence

- Unit tests are mandatory for domain/application behavior.
- Integration tests use real technology with Testcontainers when practical.
- Critical flows require concurrency, idempotency, transaction, outbox/inbox, retry/DLT, and reconciliation tests where relevant.
- Never claim completion without fresh verification output.

## Observability and security

- Propagate W3C trace context across HTTP, gRPC, Kafka, and schedulers.
- Structured logs include service, traceId, spanId, request/event/job identifiers, outcome, and latency.
- Never log passwords, tokens, PINs, full bank account/card data, or KYC documents.
- Secrets are never committed.
