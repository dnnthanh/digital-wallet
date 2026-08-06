# Digital Wallet Engineering Conventions

## Structural reference rule

The uploaded E-commerce project is the canonical **engineering-structure and reusable technical-code reference** for this repository. New work should preserve its repository topology, Maven conventions, platform starter separation, Hexagonal package layout, Docker patterns, Compose organization, testing style and CI philosophy unless a Digital Wallet ADR explicitly justifies a difference.

E-commerce remains domain-isolated: do not introduce seller/cart/catalog/checkout/product concepts into Digital Wallet unless a banking use case independently requires an analogous merchant capability.

Expected backend shape:

```text
backend/
  platform/
    be-platform-starter/
    be-platform-cache-starter/
  services/
    be-<bounded-context>-api/
    be-<bounded-context>-worker/
    be-<bounded-context>-job/
  Dockerfile.service
  Dockerfile.runtime
  docker/
compose/
  infrastructure.yml
  backend/
docker-compose.yml
```

## Technology baseline

- Java 25
- Spring Boot 4.1.x
- Spring Cloud 2025.1.x
- Maven Wrapper
- PostgreSQL
- Redis
- Kafka (KRaft for local development)
- Keycloak
- gRPC + Protobuf for selected internal synchronous contracts
- OpenTelemetry
- Testcontainers
- MapStruct / Lombok
- Apache Commons Lang / Commons Collections / Commons Codec
- Spotless / ArchUnit / JaCoCo / Maven Enforcer quality gates

## Library-first rule

Before implementing a helper, mapper, serializer, logging wrapper, cache abstraction, retry helper, codec, validation helper, or utility, check in this order:

1. JDK / standard library;
2. Spring / Spring Boot;
3. existing Digital Wallet `backend/platform/*` code;
4. dependencies already managed by `backend/pom.xml`;
5. only then write custom code or propose a new dependency.

Concrete conventions:

- constructor injection -> Lombok `@RequiredArgsConstructor` when suitable;
- logging -> Lombok `@Slf4j`;
- utility holder -> Lombok `@UtilityClass` only when a stateless holder is genuinely needed;
- repetitive DTO/model/entity conversion -> MapStruct + platform mapper contracts;
- JSON -> inject the Spring/platform `ObjectMapper`; never create ad-hoc production `new ObjectMapper()`/`new JsonMapper()` instances;
- pure reference null predicate -> `Objects.isNull` / `Objects.nonNull`; use `Objects.requireNonNullElse*` for fallback selection;
- strings -> Apache Commons Lang `StringUtils` for null-safe blank/default/trim operations where clearer than raw JDK code;
- arrays -> Apache Commons Lang `ArrayUtils`, especially `ArrayUtils.isEmpty`, rather than null + `length == 0` boilerplate;
- collections -> JDK/Spring `CollectionUtils` first when sufficient, otherwise Commons Collections algorithms;
- maps -> Spring collection utilities or Commons Collections `MapUtils` when they make null-safe intent explicit;
- assertions/invariants -> Apache Commons Lang `Validate` or JDK `Objects.requireNonNull` is allowed for programmer/configuration/internal invariants, not normal API validation;
- invariant diagnostic text -> named constants, not repeated inline literals in `Validate`, `Objects.requireNonNull`, or unchecked exception constructors;
- encoding/digest/hex/Base64 -> JDK first, Commons Codec when it is the clearer existing implementation;
- do not create duplicate project-local `StringUtils`, `JsonUtils`, `CollectionUtils`, `MapUtils`, codecs, mapper frameworks, cache wrappers, Kafka wrappers, retry helpers, or logging helpers when an existing facility already solves the problem.

Adding a dependency for one trivial helper is discouraged. Prefer an already managed library or standard API.

### Utility examples

Prefer:

```java
String correlationId =
        StringUtils.defaultIfBlank(StringUtils.trim(provided), UUID.randomUUID().toString());
```

over a manual null + blank expression.

For a pure nullable reference use:

```java
if (Objects.isNull(value)) {
    ...
}
```

For arrays use `ArrayUtils.isEmpty(values)`. For collections/maps use `CollectionUtils.isEmpty(values)` or `MapUtils.isEmpty(details)` when those utilities express the intent directly.

## Exception semantics

Exception type must match the responsibility of the failure.

- Client/request/business validation that can reach REST/gRPC boundaries uses bean validation or a typed `BusinessException` with a stable `ErrorCode`.
- Do not throw plain `IllegalArgumentException` for expected client-invalid values and then let it fall into an internal-error handler.
- Do not translate all `IllegalArgumentException` to HTTP 400; it may signal programmer/configuration defects.
- Internal constructors/configuration/infrastructure preconditions may use `Validate`, `Objects.requireNonNull`, or unchecked invariant exceptions when the input is not normal client data.
- Programmer/configuration invariant exceptions (`IllegalArgumentException`, `IllegalStateException`, `NullPointerException`) have an explicit platform handler: log the technical cause, return localized `PlatformErrorCode.INTERNAL_ERROR`, HTTP 500.
- Internal invariant messages are diagnostic-only and live in named constants such as `PlatformInvariantMessages` or module-specific constants. They are never exposed as the client response message.
- Serialization/provider/infrastructure failures retain infrastructure semantics and are not relabeled as client validation errors.
- Every externally visible error needs stable REST/gRPC mapping and a test covering the mapping or typed error code.
- Malformed/unknown request enums follow request-deserialization BAD_REQUEST/i18n handling. Raw `Enum.valueOf`/Java exception text is never the API message.

Example: invalid `Idempotency-Key` uses `BusinessException(PlatformErrorCode.INVALID_IDEMPOTENCY_KEY)` with HTTP 400 semantics rather than a plain `IllegalArgumentException`.

## Enum contracts

`CodeEnum` and `I18nCodeEnum` are structural contracts. Implementations provide `name()` and, for i18n enums, `getDeclaringClass()`; the interfaces must not use runtime `instanceof Enum` checks followed by hard-coded `IllegalStateException` messages. Enum message keys are built from centralized i18n constants.

## Java formatting

Digital Wallet mirrors the E-commerce formatter exactly:

```xml
<googleJavaFormat>
  <version>1.28.0</version>
  <style>AOSP</style>
</googleJavaFormat>
```

with Spotless Maven Plugin `2.44.5`, unused-import cleanup, trailing-whitespace cleanup, and final newline enforcement.

Rules:

- `mvn verify` runs `spotless:check` and fails CI on unformatted Java;
- before committing Java changes, run `./mvnw -f backend/pom.xml spotless:apply` then the relevant tests/`verify`;
- do not hand-format around Spotless or introduce a feature-specific formatter;
- generated sources, when introduced, must be explicitly excluded rather than silently changing formatter behavior.

## Typed Redis rule

Business/application/adapters must not manually serialize object caches to JSON strings.

Approved object-cache flow:

```text
typed object
  -> WalletCacheManager / Spring Cache
  -> RedisCacheManager
  -> JacksonJsonRedisSerializer<T>
  -> Redis
```

Rules:
- do not use `StringRedisTemplate` or `RedisTemplate<String, String>` as an object-cache shortcut;
- do not call `ObjectMapper.writeValueAsString/readValue` around ordinary Redis object caching;
- Redis keys remain readable strings;
- Redis values are typed classes/records;
- each cache has explicit name, TTL and value type;
- raw/scalar Redis access is allowed only when Redis is intentionally used as COUNTER, LOCK, RATE_LIMITER, interoperability/raw protocol state, etc., and that exception is stated in the feature spec/ADR.

Redis cache keys use this namespace convention:

```text
<spring.application.name>::<cache-name>::<business-key>
```

Never hard-code a Java prefix such as `wallet::`. Every runnable backend service must define:

```yaml
spring:
  application:
    name: be-<service-name>
```

in `src/main/resources/application.yml`.

## Typed Kafka rule

Business code publishes and consumes typed Java event objects. Broker values become bytes through the centralized Spring Kafka serializer; business code must not manually build JSON strings or `byte[]` for normal JSON events.

Preferred flow:

```text
EventEnvelope<TransferCompletedPayload>
  -> KafkaTemplate<String, Object>
  -> JacksonJsonSerializer
  -> Kafka bytes
  -> JacksonJsonDeserializer
  -> typed EventEnvelope / payload
  -> consumer
```

Rules:
- event payloads use explicit record/class contracts;
- `Map<String, Object>` is not the normal event payload model;
- Kafka listeners should not normally receive raw `String`, `byte[]`, or maps;
- raw/legacy protocol boundaries require explicit feature spec/ADR justification;
- Kafka serializer/deserializer configuration is centralized in `classpath:platform-kafka.yml`, not duplicated in every producer/consumer;
- any runnable service that uses `KafkaTemplate` or `@KafkaListener` must import `classpath:platform-kafka.yml` from its `application.yml`;
- outbox JSON/JSONB conversion is allowed only as an infrastructure storage boundary and is centralized in `OutboxPayloadCodec`.

## Platform-owned distributed tracing

Distributed tracing is infrastructure behavior owned by `be-platform-starter`. A feature module should gain tracing simply by using the normal Spring-managed clients/executors; it must not reimplement propagation.

Authoritative flow:

```text
HTTP / gRPC / scheduled job / Kafka listener
  -> current Micrometer Observation
  -> W3C traceparent + tracestate at instrumented transport boundaries
  -> downstream child span
```

Rules:

- W3C `traceparent`/`tracestate` is the authoritative distributed context. `trace-id` is only a platform compatibility/diagnostic header.
- The platform automatically enables observation for all Spring-managed `KafkaTemplate` instances and Kafka listener container factories. Do not add trace headers to business records and do not repeat `setObservationEnabled`/`setObservationRegistry` in a service.
- Batch Kafka listeners use per-record observations so unrelated records do not share one ambiguous parent trace.
- The platform wires the shared `ObservationRegistry` into Spring scheduling. Ordinary `@Scheduled` code gets a root observation automatically when no parent exists; do not call `Tracer.nextSpan()`, synthesize a trace ID, or manually open/close trace scopes in the job.
- The platform installs `ContextPropagatingTaskDecorator` on Boot-managed async executors. Prefer those executors for `@Async`/background work so trace context survives thread switches.
- Use Spring-managed `RestClient.Builder`, Spring Boot gRPC clients/stubs, Spring-managed Kafka clients, and Boot-managed task executors. Bypassing these with ad-hoc clients/executors requires an ADR because it can silently break propagation.
- Feature code must not manually write `traceparent`, `tracestate`, or the compatibility `trace-id` header. Cross-cutting propagation belongs in `backend/platform/*`.
- Trace metadata stays out of ordinary typed business payloads. `EventEnvelope<T>` contains business/event metadata, while trace context is transport instrumentation metadata.
- `correlationId` is not `traceId`: preserve a business/request correlation identifier when useful, but never use it as a substitute for W3C distributed tracing.
- If a new protocol/boundary is introduced later, first extend the platform auto-configuration so all services inherit propagation automatically; only use feature-local propagation when the protocol makes a shared solution impossible and an ADR documents it.

## Configuration vs hard-coding

Avoid operational literals in Java when they vary by service/environment. Service names, URLs, credentials, external endpoints, timeouts, environment prefixes and configurable topic names belong in `application.yml` / environment variables and should be bound with `@ConfigurationProperties` when they form a coherent configuration group.

Stable protocol constants (for example an invariant header name or event schema version) may remain code constants. Do not externalize constants merely for the sake of configuration.

`spring.application.name` is the canonical service namespace for cache/logging/observability conventions where a service identifier is needed.

## Package layout

```text
com.dnnthanh.wallet.be.<context>
  api/
  application/port/in/
  application/port/out/
  application/service/
  domain/
  adapter/in/
  adapter/out/
  infrastructure/
```

- Controllers/inbound adapters call application ports, never repositories directly.
- Application/domain cannot import adapter or persistence implementation classes.
- Internal outbound adapters are grouped by target and protocol, e.g. `adapter/out/internal/ledger/grpc`.
- Public/mobile/admin and provider HTTP use REST; selected internal core-money synchronous calls use gRPC; Kafka handles events/projections/recovery.

## Contract ownership

- REST: `docs/contracts/rest/` plus generated OpenAPI in each API deployable.
- gRPC: `contracts/grpc/<context>/v1/*.proto`.
- Kafka: `docs/contracts/kafka/` and typed schemas/contracts owned by the producing bounded context.

## Containers and Compose

Dockerfiles and Compose are first-class repository infrastructure, not an end-of-project task. New runnable services must plug into the generic backend Docker pattern and add their Compose fragment under `compose/backend/` following the reference project.

## Money types

Never use `double`/`float` for money. Java monetary values use `BigDecimal` with explicit currency/scale. PostgreSQL uses `numeric` with explicit precision/scale.

## IDs and time

Externally exposed identifiers are opaque. Persist UTC timestamps and emit ISO-8601. Financial cutoffs never depend on the local JVM timezone.

## Database migrations

Use Liquibase. Migrations are append-only after merge. Business idempotency constraints are enforced at database level as well as application level.

## Testing

JUnit + AssertJ. Testcontainers for real infrastructure integrations. External HTTP providers may use WireMock; gRPC integration should use an ephemeral real server where practical. CI must validate Maven, Spotless formatting, repository architecture, utility-reuse guardrails, null/invariant-message guardrails, typed serialization conventions, platform-owned tracing rules, Dockerfile definitions and Compose topology for affected features.
