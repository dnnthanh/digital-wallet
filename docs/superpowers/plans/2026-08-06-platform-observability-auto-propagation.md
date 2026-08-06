# Platform Observability Auto-Propagation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Kafka, scheduled jobs, and async hand-offs preserve tracing automatically from `be-platform-starter` with no feature-level tracing configuration.

**Architecture:** Add one Spring Boot auto-configuration that wires Micrometer Observation into Kafka templates/listener factories and scheduled-task registration, plus Boot task-executor customizers for thread context propagation. Keep W3C trace context authoritative and keep tracing out of business payloads.

**Tech Stack:** Java 25, Spring Boot 4.1, Spring Framework 7, Spring Kafka 4.1, Micrometer Observation/Tracing, OpenTelemetry, JUnit 5, AssertJ, Mockito.

## Global Constraints

- `be-platform-starter` owns cross-cutting tracing behavior.
- Feature code must not manually propagate `traceparent`, `tracestate`, or synthetic trace IDs.
- Kafka business payloads remain typed objects; tracing belongs to headers.
- Scheduled jobs create/restore observations through Spring infrastructure, not business code.
- Existing application/container customizers must not be overwritten.
- Spotless AOSP formatting and repository verification remain mandatory.

---

### Task 1: Define platform observability contracts

**Files:**
- Test: `backend/platform/be-platform-starter/src/test/java/com/dnnthanh/wallet/be/platform/autoconfigure/PlatformObservabilityAutoConfigurationTest.java`

**Interfaces:**
- Consumes: Spring Kafka `KafkaTemplate`, `ConcurrentKafkaListenerContainerFactory`; Micrometer `ObservationRegistry`; Spring scheduling/task executor APIs.
- Produces: executable behavior contracts for the auto-configuration.

- [ ] **Step 1: Write failing tests** for Kafka template observation, listener observation, scheduled registry wiring, async task decorators, and starter metadata.
- [ ] **Step 2: Run** `./mvnw -B -ntp -f backend/pom.xml -pl platform/be-platform-starter -am test` and confirm test compilation fails because `PlatformObservabilityAutoConfiguration` does not exist.

### Task 2: Implement platform auto-configuration

**Files:**
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/autoconfigure/PlatformObservabilityAutoConfiguration.java`
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/trace/PlatformKafkaObservationBeanPostProcessor.java`
- Modify: `backend/platform/be-platform-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

**Interfaces:**
- Produces: `SchedulingConfigurer`, Kafka observation bean post-processor, `ThreadPoolTaskExecutorCustomizer`, `SimpleAsyncTaskExecutorCustomizer`.

- [ ] **Step 1: Implement** Kafka observation enabling without replacing listener container customizers.
- [ ] **Step 2: Implement** scheduled-task registry observation wiring.
- [ ] **Step 3: Implement** Boot executor context-propagation customizers using `ContextPropagatingTaskDecorator`.
- [ ] **Step 4: Publish** the auto-configuration through starter metadata.
- [ ] **Step 5: Run** the focused Maven tests and confirm they pass.

### Task 3: Consolidate trace compatibility wiring

**Files:**
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/trace/TraceContextAccessor.java`
- Delete: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/trace/TracePropagationConfiguration.java`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/autoconfigure/PlatformObservabilityAutoConfiguration.java`

**Interfaces:**
- Produces: auto-configured `TraceContextAccessor` and `RestClientCustomizer` compatibility `trace-id` header while W3C tracing remains authoritative.

- [ ] **Step 1: Remove** component-scanning dependence from `TraceContextAccessor`.
- [ ] **Step 2: Register** the accessor and existing `trace-id` RestClient compatibility customizer from platform auto-configuration.
- [ ] **Step 3: Delete** the non-auto-loaded trace configuration to prevent two configuration paths.
- [ ] **Step 4: Run** focused tests and Spotless check.

### Task 4: Make the rule permanent for future AI sessions

**Files:**
- Modify: `AGENTS.MD`
- Modify: `.agent/CONVENTIONS.MD`
- Modify: `.agent/specs/DW-001-platform-foundation.md`
- Modify: `verification/verify_repository.py`

**Interfaces:**
- Produces: enforceable rule that distributed tracing is platform-owned and feature code cannot manually recreate it.

- [ ] **Step 1: Document** automatic Kafka/job/async propagation and Spring-managed client requirements.
- [ ] **Step 2: Add repository verification** that rejects manual `traceparent`/`tracestate` propagation and feature-level synthetic trace IDs outside the platform tracing package.
- [ ] **Step 3: Run** `python3 verification/verify_repository.py` and confirm it passes.

### Task 5: Verify and squash DW-001

**Files:**
- Modify: `.agent/reports/DW-001-platform-foundation-verification.md`

**Interfaces:**
- Produces: one clean feature commit based directly on `develop` with the full verified DW-001 tree.

- [ ] **Step 1: Run** `./mvnw -B -ntp -f backend/pom.xml verify` and repository/Docker/Compose quality gates through GitHub Actions.
- [ ] **Step 2: Record** the final verification evidence.
- [ ] **Step 3: Create** a new commit whose parent is current `develop` and whose tree equals the verified feature HEAD.
- [ ] **Step 4: Force-update** `feature/platform-foundation` to that squash commit.
- [ ] **Step 5: Run fresh GitHub Actions** on the squash HEAD and require every PR gate to be green before completion.
