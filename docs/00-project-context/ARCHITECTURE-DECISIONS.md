# Architecture Decisions Summary

Read the detailed ADRs under `docs/adr/`.

## Transport
- REST/JSON: public/mobile/admin APIs, provider APIs, webhooks.
- gRPC: internal synchronous core-money calls when typed contracts and deadline semantics are useful.
- Kafka: durable domain events, projections, notifications, asynchronous workflow/recovery.

## Identity
Keycloak owns authentication, users, credentials, roles, composite permissions, service identities, and authorization scope/group trees. JWTs are compact. Effective permissions/scopes are resolved server-side and may be cached with explicit invalidation/TTL.

## Financial source of truth
The double-entry ledger is authoritative for posted money movement. Balance projection is derived/read-optimized and may lag.

## Consistency
Use local ACID transactions inside a bounded context. Use outbox/events/Saga/reconciliation across contexts rather than distributed XA transactions.
