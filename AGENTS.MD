# AGENTS.MD

## Purpose

This repository implements a production-oriented Digital Wallet / Banking Platform. Optimize for financial correctness, security, auditability, operability, maintainability, and learning value rather than code volume.

**The E-commerce repository is reference-only for business semantics but canonical for reusable engineering structure and technical conventions. Never introduce marketplace concepts such as catalog/cart/checkout/seller/shop unless a wallet requirement explicitly needs an analogous merchant capability.**

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

## Library-first and typed-data rules

Before writing custom helpers or infrastructure code, search in this order: JDK/standard library -> Spring/Spring Boot -> existing `backend/platform/*` modules -> dependencies already managed by the parent POM -> only then create project code or propose a dependency.

- Prefer Lombok for boilerplate when it preserves clarity: `@RequiredArgsConstructor`, `@Slf4j`, `@UtilityClass`, builders/value annotations when appropriate.
- Prefer MapStruct and existing platform mapper contracts for repetitive DTO/model/entity mapping.
- Reuse the Spring-managed/platform `ObjectMapper`; never create ad-hoc production `new ObjectMapper()`/`new JsonMapper()` instances.
- Apache Commons Lang, Apache Commons Collections and Commons Codec are shared utilities in the backend parent. Reuse them when they make intent clearer instead of writing duplicate helpers.
- Pure reference-null predicates use `Objects.isNull` / `Objects.nonNull` (or `Objects.requireNonNullElse*` when selecting a fallback) rather than repeated `x == null` / `x != null` production checks.
- Strings: prefer `StringUtils.isBlank/isNotBlank`, `trim`, `trimToNull`, `defaultIfBlank`, etc. Do not repeat null + blank logic manually.
- Arrays: prefer `ArrayUtils.isEmpty` and other null-safe `ArrayUtils` operations instead of `array == null || array.length == 0`.
- Collections/maps: use existing Spring `CollectionUtils` or Apache Commons Collections/`MapUtils` for null-safe emptiness and collection operations. Pick the utility matching the data type and intent; do not create project-local `CollectionUtils`/`MapUtils` clones.
- Encoding/digest/hex/Base64 helpers: prefer JDK APIs first, then Commons Codec where it provides the clearer/supported operation; do not hand-roll codecs.
- `Validate` and `Objects.requireNonNull` are allowed for programmer/configuration/internal invariant checks. They are **not** the API validation strategy for user/client input.
- Diagnostic messages passed to `Validate`, `Objects.requireNonNull`, or unchecked invariant exceptions must use named constants; do not scatter repeated inline exception strings through production code.
- Do not add a new dependency just to save a trivial line when an existing JDK/Spring/platform/managed dependency already solves the requirement.
- Redis object caching must use typed objects through `WalletCacheManager`/Spring Cache/`RedisCacheManager` and a typed Jackson serializer. Do not use `StringRedisTemplate` or `RedisTemplate<String, String>` as an object-cache shortcut and do not manually convert cached objects to JSON strings.
- Redis keys stay readable strings. Redis values are typed objects. Raw/scalar Redis access is allowed only when the feature's Redis data model is inherently scalar/raw and the exception is explicit in its spec/ADR.
- Redis cache namespace comes from `spring.application.name`; never hard-code a project prefix such as `wallet::` in Java.
- Every runnable backend service must define `spring.application.name` in `src/main/resources/application.yml`.
- Kafka producers publish typed objects/envelopes. Ordinary JSON events must use centralized Spring Kafka Jackson serialization; business code must not manually create JSON `String`/`byte[]` payloads.
- Kafka business event payloads use explicit classes/records such as `EventEnvelope<TransferCompletedPayload>`, not `Map<String, Object>` as the normal contract.
- A runnable service that uses `KafkaTemplate` or `@KafkaListener` must import `classpath:platform-kafka.yml` from its `application.yml`; do not duplicate serializer/deserializer class names in service code/config.
- Raw `String`, `byte[]`, or map Kafka boundaries require an explicit protocol/legacy reason in the feature spec/ADR.
- Outbox JSON/JSONB is an infrastructure storage format; conversion belongs in the centralized outbox codec, not duplicated throughout domain/application code.
- Operational/environment values (service names, URLs, timeouts, credentials, environment prefixes, provider endpoints, configurable topic names) belong in YAML/environment-backed configuration, preferably via `@ConfigurationProperties`. Stable protocol constants may remain code constants when genuinely invariant.

## Null, invariant, enum, and i18n rules

- Choose utilities by semantics, not mechanically: pure reference-null checks -> `Objects.isNull/nonNull`; strings -> `StringUtils`; arrays -> `ArrayUtils`; collections/maps -> Spring/Commons collection utilities.
- `CodeEnum`/`I18nCodeEnum` define structural contracts (`name()`, `getDeclaringClass()`) and must not runtime-check `instanceof Enum` then throw hard-coded exceptions.
- Client/request/domain-invalid data that is expected during normal API usage uses Bean Validation or a typed `BusinessException`/`ErrorCode`; its response message is resolved through i18n.
- Programmer/configuration/infrastructure invariant exceptions from `Validate`, `Objects.requireNonNull`, `IllegalArgumentException`, `IllegalStateException`, or `NullPointerException` are not client errors. If they escape to HTTP, the platform handler logs the technical cause and returns localized `INTERNAL_ERROR` / HTTP 500.
- Internal precondition text is diagnostic only and belongs in named constants such as `PlatformInvariantMessages` / cache-specific invariant constants; it must not become the client-facing response message.
- Unknown/malformed request enums are request-deserialization failures and follow the platform BAD_REQUEST/i18n path; never expose a raw Java enum exception message to clients.

## Java formatting rules

- Java formatting follows the E-commerce reference: Spotless Maven Plugin `2.44.5` with `google-java-format 1.28.0` and `AOSP` style.
- `mvn verify` must execute `spotless:check`; CI must fail on unformatted Java.
- Before committing Java changes, run `./mvnw -f backend/pom.xml spotless:apply` and then the normal verification suite.
- Do not manually invent a competing formatter/style in a feature module.

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
- Client/request validation that can reach an API boundary must use the platform validation flow or a typed `BusinessException`/`ErrorCode` so HTTP/gRPC semantics are explicit.
- Do **not** add a blanket `IllegalArgumentException -> HTTP 400` handler. `IllegalArgumentException` may represent a programmer/configuration invariant and masking it as a client error hides defects.
- Programmer/configuration invariant exceptions (`IllegalArgumentException`, `IllegalStateException`, `NullPointerException`) have an explicit platform handler that returns localized `INTERNAL_ERROR` / HTTP 500 while logging the cause server-side.
- Internal constructor/configuration invariants may use `Validate`/`Objects.requireNonNull` when they cannot represent normal client input, but diagnostic text uses named constants.
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

## Platform-owned tracing rules

Distributed tracing is a `be-platform-starter` responsibility. Feature services, jobs, workers and consumers must inherit it instead of rebuilding propagation locally.

- W3C `traceparent`/`tracestate` is the authoritative distributed trace context. The compatibility `trace-id` header is platform-only diagnostics and is not a replacement propagation protocol.
- Kafka tracing is automatic: the platform enables Micrometer Observation on every Spring-managed `KafkaTemplate` and Kafka listener container factory. Business code must not add trace headers to Kafka records or enable observation per service.
- `@Scheduled` jobs are automatically observed by the platform. A job execution without an upstream parent starts a root trace; job code must not call `Tracer.nextSpan()`, generate UUID trace IDs, or manually manage tracing scopes for normal scheduling.
- Boot-managed async executors receive `ContextPropagatingTaskDecorator` from the platform so request/job/Kafka context survives thread hand-off. Prefer Boot-managed executors; a custom executor requires explicit justification and must preserve context.
- Use Spring-managed `RestClient.Builder`, Spring Boot gRPC clients/stubs, Spring-managed `KafkaTemplate`, and Boot-managed task executors. Do not instantiate clients/executors in a way that bypasses observation instrumentation unless an ADR explicitly documents the exception.
- gRPC and REST downstream calls rely on framework observation/tracing instrumentation. Feature code must not manually write `traceparent`, `tracestate`, or synthetic trace IDs.
- Trace context belongs in transport/instrumentation metadata, never in normal business event payloads such as `EventEnvelope<T>`.
- `correlationId` and distributed `traceId` are different concerns. Correlation IDs may remain business/request identifiers; they do not replace W3C tracing.
- New cross-cutting propagation requirements must be implemented once in `backend/platform/*` and auto-configured for all services whenever technically possible.

## Observability and security

- Propagate W3C trace context across HTTP, gRPC, Kafka, schedulers and supported async thread hand-offs through the platform starter.
- Structured logs include service, traceId, spanId, request/event/job identifiers, outcome, and latency.
- Never log passwords, tokens, PINs, full bank account/card data, or KYC documents.
- Secrets are never committed.
