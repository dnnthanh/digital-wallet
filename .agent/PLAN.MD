# Digital Wallet Delivery Plan

| ID | Capability | Status | Depends on |
|---|---|---|---|
| DW-000 | Project bootstrap | DONE | - |
| DW-001 | Platform foundation | IN_REVIEW | DW-000 |
| DW-002 | Keycloak authentication/authorization | IN_REVIEW | DW-001 |
| DW-003 | Customer KYC | IN_REVIEW | DW-002 |
| DW-004 | Wallet account | PLANNED | DW-002, DW-003 |
| DW-005 | Double-entry ledger | PLANNED | DW-001 |
| DW-006 | Balance projection | PLANNED | DW-005 |
| DW-007 | Transfer | PLANNED | DW-004, DW-005, DW-010, DW-011 |
| DW-008 | Hold/capture/reversal | PLANNED | DW-004, DW-005 |
| DW-009 | Funding/payout | PLANNED | DW-004, DW-005, DW-008 |
| DW-010 | Limits | PLANNED | DW-004 |
| DW-011 | Fraud | PLANNED | DW-002, DW-004 |
| DW-012 | Reconciliation | PLANNED | DW-005, DW-009 |
| DW-013 | Scheduled settlement | PLANNED | DW-005, DW-012 |
| DW-014 | Statement export | PLANNED | DW-005, DW-006 |
| DW-015 | Notification | PLANNED | DW-001 |
| DW-016 | Operations | PLANNED | DW-002 + target domains |
| DW-017 | Performance/resilience comparison | PLANNED | Core flows |
| DW-018 | Database/concurrency labs | PLANNED | DW-005, DW-007 |
| DW-019 | Full-system verification | PLANNED | DW-001..018 |

Ledger correctness is implemented before deep transfer/funding flows. Performance work must never weaken financial invariants.
