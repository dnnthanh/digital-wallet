# Project Brief — Digital Wallet / Banking Platform

This repository implements a Digital Wallet / Banking Platform.

**The E-commerce repository is a REFERENCE project only.** Reuse its engineering discipline, hexagonal structure, testing/verification philosophy, Docker/CI patterns, and documentation style. Do not reuse marketplace domain concepts unless explicitly required by wallet/merchant use cases.

## Product goals

Build a realistic wallet platform that demonstrates:
1. strong financial correctness through double-entry bookkeeping;
2. secure identity/authorization with Keycloak;
3. robust distributed-system boundaries using REST, gRPC, and Kafka appropriately;
4. idempotency, reconciliation, concurrency control, audit, and recoverability;
5. measurable performance/resilience trade-offs rather than cargo-cult architecture.

## Capability map

`platform-foundation`, `customer-kyc`, `wallet-account`, `double-entry-ledger`, `balance-projection`, `transfer`, `funding-payout`, `hold-capture-reversal`, `limit-fraud`, `reconciliation`, `scheduled-settlement`, `statement-export`, `notification`, `operations`, `performance-resilience-comparison`.

## Non-goals

- No fake microservices split solely for service count.
- No duplicate custom IAM database competing with Keycloak.
- No direct balance mutation without ledger journals.
- No "exactly once" claims based solely on Kafka configuration.
- No blind retries of ambiguous financial operations.
