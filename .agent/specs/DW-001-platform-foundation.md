# DW-001: Platform Foundation Specification

## Status
APPROVED BASELINE — implementation details may be refined by ADR/spec review before coding.

## Goal
Provide stable technical conventions and reusable platform starters without leaking business-domain coupling.

## Structural baseline
The uploaded E-commerce project is the canonical engineering-structure reference for DW-001. Digital Wallet must preserve the same family of repository patterns unless a banking ADR explicitly documents a deviation:
- backend shared modules under `backend/platform/`;
- runnable deployables under `backend/services/be-*`;
- common Maven parent/tooling baseline;
- generic backend runtime/service Dockerfiles and entrypoints;
- root plus split Compose layout under `compose/`;
- Hexagonal/DDD package conventions, testing style and CI quality gates.

The E-commerce business domain is not copied. Seller/cart/catalog/checkout concepts stay out of Digital Wallet unless independently required by a banking use case.

## Use cases
- Standard REST API response/error envelope and exception mapping
- Request/correlation/trace propagation for HTTP, gRPC, Kafka, schedulers and supported async thread hand-offs
- Keycloak resource-server baseline and service-identity support
- Request-scoped execution identity (`UserContext`) without leaking Spring Security into application code
- Idempotency infrastructure primitives without domain-specific persistence
- Transactional outbox technical support
- Typed Kafka event/envelope and base producer/consumer conventions
- MapStruct mapping contracts and technical stereotypes (`UseCase`, `Adapter`, `Persistence`)
- gRPC client/server baseline with deadlines/interceptors
- Redis cache starter with typed serializers, explicit TTL and fail-open cache behavior
- PostgreSQL/Testcontainers test infrastructure
- Health, metrics, structured logging, i18n and OpenTelemetry baseline
- Dockerfile and Docker Compose foundations for later Digital Wallet services

## Invariants / non-negotiable rules
- No business-domain model in platform shared modules
- No source-controlled production secrets/default passwords
- No runtime REST-vs-gRPC transport switch
- Platform failures must not silently change financial semantics
- Redis/cache failure cannot become an authoritative financial-data decision
- One platform convention per concern; do not keep parallel response/error/event/security abstractions after the reference base is adopted
- Structural deviations from the E-commerce engineering skeleton require an ADR
- Agents must prefer JDK/Spring/existing `backend/platform/*`/managed dependencies before writing new utility/helper abstractions
- Pure reference-null predicates use `Objects.isNull/nonNull`; string/array/collection/map nullability uses the matching `StringUtils`/`ArrayUtils`/collection utility instead of repeated manual null checks
- Lombok, MapStruct, Jackson and existing platform helpers must be reused when they already solve the problem cleanly
- `Validate`/`Objects.requireNonNull` are internal programmer/configuration preconditions only; normal client-invalid input uses Bean Validation or typed `BusinessException`/`ErrorCode`
- Internal unchecked precondition/invariant messages use named constants; they are diagnostic-only and never replace i18n client messages
- `IllegalArgumentException`, `IllegalStateException`, and `NullPointerException` that escape an internal invariant are explicitly mapped to localized `INTERNAL_ERROR` / HTTP 500, not blanket HTTP 400
- `CodeEnum`/`I18nCodeEnum` use structural `name()`/`getDeclaringClass()` contracts and do not runtime-check `instanceof Enum` followed by hard-coded exceptions
- Malformed/unknown request enums follow request-deserialization BAD_REQUEST/i18n handling; raw Java enum exception text is never returned to clients
- Redis object values are typed classes/records serialized centrally by the cache starter; `StringRedisTemplate`/`RedisTemplate<String, String>` is not an object-cache shortcut
- Redis cache namespace is derived from `spring.application.name`, not a Java hard-coded project prefix
- Every runnable backend service defines `spring.application.name` in `src/main/resources/application.yml`
- Kafka business messages are typed classes/records/envelopes; ordinary JSON events are not manually converted to `String`, `byte[]`, or `Map<String, Object>` payloads
- Outbox JSON/JSONB conversion is an infrastructure boundary centralized in `OutboxPayloadCodec`
- Operational/environment values belong in YAML/environment-backed configuration rather than repeated Java literals; stable protocol constants may stay in code
- Distributed tracing is platform-owned. Feature services/jobs/consumers must not manually create or propagate W3C trace headers when platform instrumentation can do it centrally
- W3C `traceparent`/`tracestate` is authoritative; `trace-id` is a platform compatibility/diagnostic header only
- Kafka producer/listener observation is auto-enabled from `be-platform-starter`; service modules do not repeat observation wiring
- `@Scheduled` jobs receive the shared `ObservationRegistry` automatically and create a root trace when there is no upstream parent
- Boot-managed async executors receive `ContextPropagatingTaskDecorator` so trace context survives supported thread hand-offs
- Spring-managed `RestClient.Builder`, Spring Boot gRPC clients/stubs, Spring-managed Kafka clients and Boot-managed executors are the default; bypassing them requires an ADR because it can break tracing
- Trace context stays in transport/instrumentation metadata, not ordinary typed business event payloads
- New cross-cutting tracing boundaries should be solved once in `backend/platform/*` and inherited automatically by later services

## Synchronous boundaries
- Keycloak/OIDC discovery/JWK validation where required
- Keycloak client-credentials token acquisition for service identities
- Selected internal service-to-service calls may use gRPC; public/admin/external HTTP remains REST
- REST/gRPC outbound clients use framework-managed instrumentation so the current trace continues automatically

## Asynchronous boundaries
- Kafka technical envelope conventions only; no global business topic owner
- Kafka object serialization/deserialization is centralized through Spring Kafka Jackson serializers/deserializers
- Kafka trace propagation is transport-header instrumentation owned by the platform, not part of the business payload contract
- Scheduled jobs are observed at execution entry and downstream REST/gRPC/Kafka calls inherit that context

## Failure and recovery scenarios
- Keycloak unavailable after JWT key cache warm/cold
- Kafka unavailable during outbox publication
- Redis unavailable for optional cache
- Trace exporter unavailable
- Programmer/configuration invariant escapes to an API boundary and must be logged while returning localized `INTERNAL_ERROR`
- Tracing/observation infrastructure must never change business correctness or financial transaction outcomes

## Required verification
- Architecture dependency and stereotype tests
- REST API/error contract test
- Explicit invariant exception handler test proving internal NPE/IAE/ISE returns localized HTTP 500 rather than a raw message or HTTP 400
- Enum contract test proving `CodeEnum`/`I18nCodeEnum` do not require runtime enum-type checks
- gRPC interceptor/deadline test
- Kafka typed event-envelope serialization test
- Kafka typed consumer contract test
- Kafka template/listener-factory observation auto-configuration test
- Scheduled-task `ObservationRegistry` auto-configuration test
- Async task-executor context-propagation auto-configuration test
- Starter metadata test proving observability auto-configuration loads without per-service configuration
- Redis typed cache integration test
- Redis key-prefix assertion using `spring.application.name`
- Testcontainers PostgreSQL smoke test
- Maven reactor verification on Java 25
- Root and split Docker Compose config validation
- Generic Dockerfile BuildKit validation
- Repository structural gate that rejects the legacy `backend/be-platform-foundation` layout
- Repository convention gate that rejects object-cache `StringRedisTemplate`, hard-coded `wallet::` prefix, ordinary Kafka raw payload patterns, runnable services missing `spring.application.name`, service-level manual distributed-trace propagation, manual production `== null`/`!= null`, array `length == 0` checks, runtime enum guards, and hard-coded unchecked-precondition messages

## Security / authorization
- All protected entry points use Keycloak-authenticated identity.
- Platform exposes compact identity/roles only; DW-002 owns effective permissions and Keycloak group/scope-tree resolution.
- Backend enforces permission and data scope; client checks never replace server enforcement.
- Sensitive values are excluded from logs/events unless explicitly required and protected.

## Observability
- `be-platform-starter` owns distributed tracing auto-configuration.
- W3C trace context propagates across HTTP, gRPC, Kafka, schedulers and supported async thread hand-offs without feature-level propagation code.
- Kafka producer/listener observations share the application `ObservationRegistry`; batch listeners use per-record observations.
- Scheduled jobs are observed automatically and downstream framework-managed clients inherit the active context.
- `correlationId` and distributed `traceId` remain separate concepts.
- Emit technical and business metrics needed to diagnose latency, errors, retries and recovery.
- `spring.application.name` is the canonical service identifier for namespace/logging/observability conventions where a service name is required.

## Docker / Compose scope
DW-001 owns the container foundation, not a later business feature:
- `backend/Dockerfile.service` and `backend/Dockerfile.runtime`;
- root `Dockerfile.backend`;
- non-root backend entrypoints;
- root `docker-compose.yml`;
- `compose/infrastructure.yml` for PostgreSQL, Redis, Kafka KRaft and Keycloak;
- `compose/backend/all.yml` as the aggregation point for service fragments added by later DW features;
- `.env.example` with blank secret values.

## Definition of done
- Implementation plan exists and maps every use case/invariant to code/tests.
- Unit/integration/recovery tests required above pass.
- Maven, typed serialization/tracing/null-invariant convention gates, Dockerfile and Compose verification pass.
- Verification evidence is stored under `.agent/reports/`.
- Feature branch PR targets `develop` and GitHub Actions are green.
