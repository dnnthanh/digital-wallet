# ADR-001: REST, gRPC, and Kafka Responsibilities

## Status
Accepted

## Decision
Use REST/JSON at public/admin/external boundaries, gRPC for selected internal synchronous core-money calls, and Kafka for asynchronous domain events/projections/recovery.

## Why
REST maximizes interoperability/debuggability. gRPC provides compact typed contracts, HTTP/2 multiplexing, deadlines, and well-defined status semantics for internal calls. Kafka decouples durable asynchronous event consumers and supports replay/recovery patterns.

## Rejected
- REST-only: simpler, but misses useful gRPC learning/performance comparison for internal critical paths.
- gRPC-everywhere: poor fit for browser/public/provider/webhook boundaries.
- Kafka-as-RPC: adds correlation/timeout/operational complexity for naturally synchronous decisions.
