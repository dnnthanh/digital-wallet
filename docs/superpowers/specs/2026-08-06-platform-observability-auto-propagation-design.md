# Platform Observability Auto-Propagation Design

## Goal

Make tracing a platform-owned cross-cutting concern so every Digital Wallet backend service gets HTTP, gRPC, Kafka, scheduled-job, and async-thread trace propagation by depending on `be-platform-starter`, without feature code manually creating or forwarding trace identifiers.

## Decisions

- Micrometer Observation is the platform tracing abstraction.
- W3C `traceparent` / `tracestate` is the authoritative distributed tracing context.
- The existing `trace-id` HTTP header remains a compatibility/diagnostic header only; it is not the propagation protocol.
- Kafka producer and listener observation is enabled centrally for every Spring-managed `KafkaTemplate` and every `AbstractKafkaListenerContainerFactory`.
- Batch listeners enable per-record observations so records from different traces do not collapse into one ambiguous parent context.
- `@Scheduled` methods receive an Observation automatically through a platform `SchedulingConfigurer`; an execution without an upstream parent becomes a root trace.
- Boot-managed async executors receive `ContextPropagatingTaskDecorator` centrally so a scheduled/request/Kafka trace survives thread hand-off.
- Spring-managed `RestClient.Builder`, Spring Boot gRPC clients, and Spring Kafka clients are required so their built-in observation instrumentation can propagate the current W3C context.
- Feature code must not manually write `traceparent`, `tracestate`, or synthetic trace IDs. A protocol exception requires an ADR.

## Data flows

### Kafka

`HTTP/gRPC/job span -> KafkaTemplate observation -> W3C headers -> Kafka listener observation -> consumer span -> downstream REST/gRPC/Kafka`

The business payload remains a typed object; tracing lives in transport headers and never in `EventEnvelope<T>` business payload fields.

### Scheduled jobs

`@Scheduled execution -> scheduled-task observation/root trace -> service logic -> REST/gRPC/Kafka`

Job code does not call `Tracer.nextSpan()` and does not create UUID trace IDs.

### Async hand-off

`current observation -> Boot-managed TaskExecutor + ContextPropagatingTaskDecorator -> worker thread restores context -> downstream client instrumentation continues the same trace`

## Platform components

- `PlatformObservabilityAutoConfiguration`: publishes the cross-cutting beans.
- `PlatformKafkaObservationBeanPostProcessor`: enables observation on all Spring-managed Kafka templates/listener factories without replacing application container customizers.
- `TraceContextAccessor`: remains the read-only bridge for diagnostics/logging and the compatibility `trace-id` HTTP header.
- Starter auto-configuration metadata imports the observability auto-configuration automatically.

## Failure and compatibility behavior

- If no active span exists, `TraceContextAccessor.currentTraceId()` returns `null`; business behavior must not fail because tracing is unavailable.
- Trace propagation must never affect transaction correctness, retries, or message payload schemas.
- Custom executors/clients that bypass Spring Boot instrumentation are forbidden by default because they silently break propagation.
- Observability remains infrastructure-only; bounded contexts do not depend on tracing implementation classes.

## Verification

Tests must prove:

1. every `KafkaTemplate` is observation-enabled and receives the shared `ObservationRegistry`;
2. every listener factory is observation-enabled, uses the shared registry, and enables per-record batch observation;
3. scheduled-task registration receives the shared registry;
4. Boot-managed async executor customizers install `ContextPropagatingTaskDecorator`;
5. starter metadata auto-loads the observability configuration;
6. repository rules forbid manual distributed-trace propagation in feature/service code.
