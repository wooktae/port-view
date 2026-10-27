# Traceability Matrix — 02-aws-network-and-rds

This document shows the mapping of requirements (Requirement N) → design (design.md section) → tasks (tasks.md task number) → validation (validation-checklist.md section / runbook.md Step) → operator decision of the `02-aws-network-and-rds` spec in a single table. This document is mapping material and does not change decision values.

## Column Definitions

- Requirement ID: `Requirement N` in `02-aws-network-and-rds/requirements.md`.
- Requirement Summary: A one-line summary. See the source for details.
- Design Section: The section title in `02-aws-network-and-rds/design.md`.
- Task ID: The task number in `02-aws-network-and-rds/tasks.md`.
- Validation / Evidence: `02-aws-network-and-rds/validation-checklist.md` section + `02-aws-network-and-rds/runbook.md` Step.
- Related Decision ID: The Decision ID in `../_common/operator-decisions.md`.

## Mapping Table

| Requirement ID | Requirement Summary | Design Section | Task ID | Validation / Evidence | Related Decision ID |
|---|---|---|---|---|---|
| Requirement 1 | Single VPC + local-dev / aws-paper / aws-live environment separation | `Architecture` environment model / `Single VPC vs prod / non-prod Separation` | 1, 2, 6 | validation §1, §2 / runbook Step 2, 3, 4 | OD-ENV-001, OD-ENV-002, OD-ENV-003, OD-ENV-005 |
| Requirement 2 | public / private (app) / private (data) subnet + NAT-free Route Table | `VPC Diagram`, `Resource Placement Policy`, `Route Table Policy (NAT-free)` | 7, 10 | validation §2 / runbook Step 5, 8 | OD-NET-001, OD-NET-002, OD-NET-004 |
| Requirement 3 | NAT-free strategy + enabled VPC Endpoint items | `NAT-free Strategy and Internet Outbound Options`, `VPC Endpoint Recommendation` | 4, 5, 13 | validation §2 (NAT not created), §4 / runbook Step 7, 11 | OD-NET-001, OD-NET-002, OD-NET-003, OD-NET-004, OD-NET-005, OD-NET-006 |
| Requirement 4 | Per-MS Security Group least privilege | `Security Group Design` | 11, 12 | validation §3 / runbook Step 9, 10 | OD-NET-009, OD-SEC-004 |
| Requirement 5 | aws-paper / aws-live RDS instance configuration | `RDS for PostgreSQL Configuration` per-environment instance table, Parameter Group Recommendation | 3, 14, 15, 17 | validation §5 / runbook Step 12, 13, 14, 15 | OD-RDS-001, OD-RDS-002, OD-RDS-003, OD-RDS-004, OD-RDS-005, OD-RDS-006, OD-RDS-007, OD-RDS-008 |
| Requirement 6 | portfolio DB + 10 schemas + search_path kept | `Keeping the portfolio DB and 10 schemas`, `Environment-Variable Compatibility` | 18, 19, 22, 23 | validation §6 / runbook Step 16 | OD-DB-001, OD-DB-002, OD-DB-003, OD-DB-006 |
| Requirement 7 | Per-MS DB Role permission matrix | `DB Role Permission Matrix` Role Definition / permission matrix table | 4 (see 01 spec), 20, 21 | validation §6 / runbook Step 16 | OD-DB-004, OD-DB-005, OD-SEC-001, OD-SEC-002 |
| Requirement 8 | local-dev → aws-paper → aws-live cutover procedure | `Data cutover Procedure` stage overview / validation SQL / Rollback Criteria | 27, 28 | validation §7 / runbook Step 17 | OD-CUT-001, OD-CUT-002, OD-CUT-003, OD-CUT-004 |
| Requirement 9 | RDS backup / snapshot / PITR policy | `RDS backup / snapshot / PITR` table | 25 | validation §5 (PITR / retention) / runbook Step 14, 15 | OD-RDS-006, OD-RDS-007, OD-RDS-008, OD-RDS-009 |
| Requirement 10 | aws-paper / aws-live automatic BUY/SELL / automatic retry policy | `Safety Mechanisms and Automatic Retry Policy` paper / live / environment separation safety mechanisms | 29 | validation §7 (automatic retry / Secret prefix separation) | OD-SAFE-001, OD-SAFE-002, OD-SAFE-003, OD-SAFE-004 |
| Requirement 11 | Cost profiles (low / realistic / stable) | `Cost Impact Summary` + decision-matrix.md §11, §12, §13 | 1 | validation §1, §8 / runbook Step 2 | OD-NET-001, OD-NET-002, OD-NET-005, OD-NET-007, OD-NET-008, OD-RDS-002, OD-RDS-003, OD-OBS-002, OD-OBS-003 |
| Requirement 12 | Step-by-step AWS Console procedure | `AWS Console Step-by-step Flow` summary 16 steps | 6 ~ 26 | validation §2 ~ §6 / runbook Step 0 ~ 18 | (decisions throughout) |
| Requirement 13 | AWS Resource glossary + operator decision record | (separate root common documents) [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md), [`../_common/operator-decisions.md`](../_common/operator-decisions.md) | (common across all specs) | validation §1, §9 | (global) |
| Requirement 14 | This spec's safety constraints | `Safety Constraints of This Spec` | all | validation §9 (`[REDACTED]` consistency, MS code / docs unmodified) | (safety constraints) |

## Per-Acceptance-Criteria Augmentation Mapping

| Requirement ID | Acceptance Criteria | Design Location | Task | Validation / Runbook | Decision |
|---|---|---|---|---|---|
| 1.1 | Single region Seoul ap-northeast-2 | `Architecture` environment model | 1, 2 | validation §1 / runbook Step 1 | OD-ENV-005 |
| 2.2 | NAT-free Route Table policy | `Route Table Policy (NAT-free)` | 10 | validation §2 (rt-app, rt-data no external route) / runbook Step 8 | OD-NET-001, OD-NET-002 |
| 3.1 | NAT GW not-used default for both aws-paper / aws-live | `NAT-free Strategy and Internet Outbound Options` | 9 | validation §2 (NAT not created) / runbook Step 7 | OD-NET-001, OD-NET-002 |
| 3.3 | 5 recommended VPC Endpoints + STS/KMS optional | `VPC Endpoint Recommendation` table | 5, 13 | validation §4 / runbook Step 11 | OD-NET-005, OD-NET-006 |
| 3.5 | marketconnector EIP top priority | `NAT-free Strategy and Internet Outbound Options` recommendation | 4 | validation §3 (`sg-marketconnector-ec2` outbound) / runbook Step 10 | OD-NET-003 |
| 4.2 | RDS SG inbound = SG reference only | `Security Group Design` table | 12 | validation §3 (no 0.0.0.0/0) / runbook Step 10-1 | OD-SEC-004 |
| 4.3 | Prohibit EC2 SSH 22 inbound 0/0, SSM Session Manager | `Security Group Design` operator access policy | 12 | validation §3 (no SSH 22 0/0 SG) / runbook Step 10 | OD-NET-009, OD-SEC-004 |
| 4.4 | public subnet ECS Task SG inbound not allowed | `Security Group Design` table | 12 | validation §3 (`sg-crawler-tasks`, `sg-preprocessor-tasks` inbound empty) / runbook Step 10-6, 10-7 | (R-NET-001) |
| 5.2 | aws-paper RDS = `db.t4g.small` single-AZ + retention 7 days + PITR | `RDS for PostgreSQL Configuration` per-environment instance table | 17 | validation §5 / runbook Step 14 | OD-RDS-001, OD-RDS-006, OD-RDS-008 |
| 5.6 | parameter group `rds.force_ssl=1` etc. | `Parameter Group Recommendation` | 15 | validation §5 / runbook Step 13 | OD-RDS-004 |
| 6.4 | Environment-variable key compatibility kept | `Environment-Variable Compatibility` | 23 | validation §6 / runbook Step 16-3 | OD-DB-003 |
| 7.1 | 7 roles + per-schema permission matrix | `DB Role Permission Matrix` | 20, 21 | validation §6 / runbook Step 16 | OD-DB-004 |
| 7.5 | DB password is a placeholder. Final decision in 06 spec | `DB Role Permission Matrix` body + Step 16 placeholder | 16, 20 | validation §6 (`[REDACTED]` notation) / runbook Step 14-0 | OD-SEC-001, OD-SEC-002 |
| 8.1 | pg_dump+pg_restore top priority, AWS DMS deferred | `Data cutover Procedure` | 27 | validation §7 / runbook Step 17 | OD-CUT-001, OD-CUT-002 |
| 8.4 | On validation failure, rollback returning to local-dev | `Rollback Criteria` | 28 | validation §7 / runbook Step 17, 19 | OD-CUT-001, OD-CUT-004 |
| 9.1 | backup retention paper 7 days / live 14 days + manual snapshot timing + PITR | `RDS backup / snapshot / PITR` table | 25 | validation §5 (retention 7 days, PITR on) / runbook Step 14, 15 | OD-RDS-006, OD-RDS-007, OD-RDS-009, OD-CUT-003 |
| 10.3 | automatic-retry-prohibited step table | `Safety Mechanisms and Automatic Retry Policy` table | 29 | validation §7 (automatic retry policy agreed) | OD-SAFE-004 |
| 10.4 | aws-paper / aws-live environment separation (Secret prefix etc.) | `Safety Mechanisms and Automatic Retry Policy` environment separation safety mechanisms | 29 | validation §7 (Secret prefix separation agreed) | OD-SAFE-002, OD-SAFE-003, OD-SEC-001 |
| 11.1 | 3 cost profiles for aws-paper / aws-live | decision-matrix.md §11, §12 | 1 | validation §1, §8 | (sum of all decisions) |
| 11.2 | Per-item impact of cost-saving items | decision-matrix.md §13 | 1 | validation §8 | OD-NET-001, OD-NET-007, OD-NET-008, OD-RDS-002, OD-OBS-002, OD-OBS-003 |
| 12.1 | tasks.md Console step decomposition | `AWS Console Step-by-step Flow` 16 steps | 6 ~ 26 | validation §2 ~ §6 / runbook Step 0 ~ 18 | (throughout) |
| 13.1 | aws-resource-glossary includes all items | (root common) [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md) | (common across all specs) | validation §9 | (global) |
| 14.5 | secret positions `[REDACTED]` consistency | body safety constraints | all | validation §9 (R-DOCS-001 mitigation) | (safety constraints) |

## Mapping to Risks

The key risks of this spec are managed in [`../_common/risk-register.md`](../_common/risk-register.md). Requirement → Risk mapping augmentation.

| Requirement ID | Related Risk ID | mitigation location |
|---|---|---|
| Requirement 2 (NAT-free Route Table) | R-NET-002 | runbook Step 5, 8 / validation §2 |
| Requirement 3 (NAT-free + Endpoint) | R-NET-003, R-COST-002 | runbook Step 7, 11 / validation §2 (NAT), §4 (Endpoint) |
| Requirement 4 (SG least privilege) | R-NET-001, R-SEC-001 | runbook Step 9, 10 / validation §3 |
| Requirement 5 (RDS configuration) | R-SEC-001, R-COST-001 | runbook Step 14, 15 / validation §5, §8 |
| Requirement 6 (search_path kept) | R-DATA-001 | runbook Step 16 / validation §6 |
| Requirement 8 (cutover) | R-DATA-002 | runbook Step 17 / validation §7 |
| Requirement 10 (automatic retry policy) | R-AUTO-001, R-AUTO-002 | runbook Step 17 (paper Secret separation) / validation §7 |
| Requirement 11 (cost profiles) | R-COST-001, R-COST-002 | decision-matrix §11, §12, §13 / validation §8 |
| Requirement 14 (safety constraints) | R-DOCS-001 | body safety constraints / validation §9 |

## Handoff to Follow-up Specs

This spec's decisions / deliverables are handed off to the following follow-up specs.

- 06-secrets-and-iam (planned): Final decision on Secrets storage / the IAM matrix. Lock the secret placeholders of Requirements 7, 10.
- 03-marketconnector-ec2 (planned): EIP / SSM operator access / access_token procedure. Apply Requirements 3, 4.
- 08-interest-crawler-and-preprocessor-ecs (planned): public subnet placement + OPT-1 / OPT-3. Apply Requirements 3, 4.
- 04-strategy-batch-stepfunctions (planned): Step Functions automatic retry policy. Apply Requirement 10.
- 05-port-view-ecs-and-runbook (planned): ALB adoption decision + view_app execution write re-review. Augment Requirement 7.
- 10-cutover-and-validation-runbook (planned): Integrate the actual cutover. Execute Requirements 8, 9.

## Safety Constraints for This Document's Work

- Writing this document does not change decision values.
- Do not modify the code / README / AGENTS.md / CHANGELOG / docs / worklog of the 8 MS.
- No actual AWS resource creation / change.
- No actual secret value output (all `[REDACTED]`).
