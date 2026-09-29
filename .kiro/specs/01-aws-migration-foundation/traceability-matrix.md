# Traceability Matrix — 01-aws-migration-foundation

This document presents, in a single table, the mapping of the `01-aws-migration-foundation` spec from requirements (Requirement N) → design (design.md section) → tasks (tasks.md task number) → validation / operator decision. This document is mapping material only; it does not change any decision value.

## Column Definitions

- Requirement ID: `Requirement N` in `01-aws-migration-foundation/requirements.md`.
- Requirement Summary: One-line summary. See the original text for details.
- Design Section: The section title in `01-aws-migration-foundation/design.md`.
- Task ID: The task number in `01-aws-migration-foundation/tasks.md`.
- Validation / Evidence: Because this spec is a document-artifact spec, evidence is document authoring / table finalization / `git status`. Validation executed in follow-up specs is delegated to that spec's validation-checklist.
- Related Decision ID: The Decision ID in `../_common/operator-decisions.md`.

## Mapping Table

| Requirement ID | Requirement Summary | Design Section | Task ID | Validation / Evidence | Related Decision ID |
|---|---|---|---|---|---|
| Requirement 1 | Per-MS AWS compute candidate comparison and recommendation | `Per-MS Compute Candidate Comparison and Recommendation` | 1, 2 | design.md tables complete, Appendix A inventory table | OD-MS-001 ~ OD-MS-009 |
| Requirement 2 | RDS for PostgreSQL migration strategy for the single portfolio DB | `RDS for PostgreSQL Migration Design` | 3, 4 | design.md per-environment RDS table, role matrix table | OD-DB-001, OD-DB-002, OD-DB-003, OD-DB-004, OD-DB-006, OD-RDS-001, OD-RDS-004, OD-RDS-008, OD-CUT-001 |
| Requirement 3 | Externalizing sensitive information (Secrets Manager / SSM) | `Secrets / Environment Variable Design` | 5, 6 | design.md mapping table, `[REDACTED]` consistency grep review | OD-SEC-001, OD-SEC-002, OD-SEC-003, OD-DB-003 |
| Requirement 4 | Network / external access path design (VPC / SG / EIP / operator access) | `Network Design` | 7 | design.md VPC diagram / SG / EIP recommendation / SSM policy stated | OD-ENV-005, OD-NET-003, OD-NET-009 |
| Requirement 5 | Container image / ECR / CI/CD standards | `Container Images and CI/CD` | 8 | design.md ECR naming / CI/CD comparison / build-time secret policy | OD-MS-005 (packaging), OD-CICD-* (added in 07) |
| Requirement 6 | Observability / alerting / operational Runbook skeleton | `Observability / Alerting / Runbook`, `Runbook Items` | 9, 10, 11 | design.md log group / metrics / Slack / Runbook 5 scenarios / automatic retry policy table | OD-OBS-001, OD-OBS-002, OD-OBS-003, OD-OBS-004, OD-MS-010, OD-SAFE-004 |
| Requirement 7 | Environment separation / cost estimation / 6-stage cutover roadmap | `Environment Separation / Cost / Phased Cutover` | 12, 13 | design.md environment table / cost items / stage table (entry/exit conditions) / follow-up spec candidates | OD-ENV-001, OD-ENV-002, OD-ENV-003, OD-ENV-004, OD-CUT-003, OD-CUT-004 |
| Requirement 8 | Safety constraints and artifact scope of this spec | `Safety Constraints of This Spec` | 14 | `git status --short` shows no changes to the 8 MS, secret pattern grep result finds only `[REDACTED]`, MS entrypoints not executed | (safety constraint, no separate Decision ID) |

## Supplementary Mapping at the Acceptance Criteria Level

This spec has many acceptance criteria, so some Requirements are scattered across design / task as detailed items. To let the operator trace quickly, the key acceptance criteria are supplemented in a separate table.

| Requirement ID | Acceptance Criteria | Design Location | Task | Decision |
|---|---|---|---|---|
| 1.2 | Evaluate port-marketconnector EC2 as a top-tier candidate | `port-marketconnector` paragraph | 2 | OD-MS-001, OD-NET-003 |
| 1.4 | Compare strategy decision/execution/research across ECS Fargate / AWS Batch / Step Functions | `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research` paragraphs | 2 | OD-MS-006, OD-MS-007, OD-MS-008 |
| 1.5 | State the port-interest-crawler Selenium/Chrome limitations | `port-interest-crawler` paragraph | 2 | OD-MS-003 |
| 1.7 | port_strategy_common has no separate compute + packaging recommendation | `port_strategy_common` paragraph | 2 | OD-MS-005 |
| 2.5 | pg_dump / pg_restore as first choice, AWS DMS as second | `Data Migration` paragraph | 3 | OD-CUT-001, OD-CUT-002 |
| 2.7 | Recommend live multi-AZ + allow dev/paper single-AZ | `RDS for PostgreSQL Migration Design` per-environment instance table | 3 | OD-RDS-002, OD-RDS-003 |
| 3.6 | Preserve environment variable key compatibility (`INTEREST_DB_*`, `PORT_*`, etc.) | `Environment Variable Compatibility` paragraph | 5 | OD-DB-003 |
| 3.7 | All secret positions use `[REDACTED]` | Entire body | 5, 14 | (safety constraint) |
| 3.8 | Compare access_token.txt storage candidates | `access_token.txt Storage Location` paragraph | 6 | OD-SEC-003 |
| 4.2 | EC2 + EIP as first choice, NAT Gateway EIP as second | `Outbound IP Policy` paragraph | 7 | OD-NET-003 |
| 4.6 | Prohibit SSH exposure + use SSM Session Manager | `Network Design` operator access paragraph | 7 | OD-NET-009, OD-SEC-004 |
| 6.5 | Runbook 5 scenarios | `Runbook Items` paragraph | 11 | OD-MS-010 |
| 6.6 | Distinguish automatic retry allowed / prohibited | `Automatic Retry Policy` paragraph | 11 | OD-SAFE-004 |
| 7.3 | 6-stage cutover roadmap entry/exit conditions | `Phased Cutover Roadmap` paragraph | 12 | OD-ENV-003, OD-ENV-004 |
| 8.4 | No actual AWS resources created | Safety constraints in body | 14 | (safety constraint) |

## Handover to Follow-up Specs

Because this spec is the foundation stage, it does not perform Requirement-level actual validation (for example, VPC creation results or RDS endpoint confirmation). Follow-up specs take over as follows.

- Requirement 1, 2, 4, 7 → `02-aws-network-and-rds`'s [`./traceability-matrix.md`](../02-aws-network-and-rds/traceability-matrix.md), [`./validation-checklist.md`](../02-aws-network-and-rds/validation-checklist.md).
- Requirement 3 → 06-secrets-and-iam (planned).
- Requirement 5, 8 → 07-cicd-pipelines (planned); the safety constraints of this spec apply as-is to all follow-up specs.
- Requirement 6 → 05-port-view-ecs-and-runbook (planned), 10-cutover-and-validation-runbook (planned).

## Safety Constraints for This Document's Work

- Authoring this document does not change any decision value.
- Do not modify the code / README / AGENTS.md / CHANGELOG / docs / worklog of the 8 MS.
- No actual AWS resources created / changed.
- No actual secret values output (all `[REDACTED]`).
