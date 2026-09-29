# PORT-STRATEGY-AI AWS Migration Follow-up Spec Overview

## Purpose

This document is the single source of truth that organizes the big picture of the 9 follow-up specs after the `01-aws-migration-foundation` spec. It serves as an index for seeing the dependencies and progression order between specs at a glance.

Document flow: **Purpose → Current Follow-up Dashboard (Now · Next · Later · Blocked · Done Recently) → Spec Roadmap → History Management Principles → Update Rules → Security Notes**.

Long background and date-by-date completion history are managed in `WORKLOG.md`, detailed execution evidence in each spec's `operation-notes.md`, decisions in `operator-decisions.md`, and risks in `risk-register.md`. This document keeps only the current follow-up work and the spec progression order.

In line with the operator decision change (2026-06 update), the environment model has been consolidated to 3 — `local-dev` · `aws-paper` · `aws-live` — and the NAT Gateway is not used by default in both paper and live. No separate AWS dev environment is built.

Each spec is managed in a separate folder (`02-` ~ `10-`).

This document is a follow-up work index and does not cover actual AWS resource creation, IaC authoring, or application code changes.

Sensitive information is never recorded verbatim; only `[REDACTED]`-style placeholders are used.

## Current Follow-up Dashboard

A follow-up work summary the operator reviews daily. Long background and date-by-date completion history are separated into `WORKLOG.md`, and detailed execution evidence into each spec's `operation-notes.md`.

### 🟠 Now — start immediately

#### OPS Mirror detailing

| Item | Value |
| --- | --- |
| Current | run-level + representative workflow step mirror complete |
| Follow-up | Expand to all detailed steps |
| Distinction needed | Automatic execution run · manual recovery run |
| Safeguard | Review a CloudWatch Alarm for Recorder Lambda failure |
| Related spec | 04 |

#### Operating data·Slack follow-ups

| Item | Value |
| --- | --- |
| Balance consistency | Review linking the 10-minute balance snapshot with order·fill·Position reconciliation |
| Daily Brief | Confirm next-weekday 07:50 pre-market Slack reception |
| Ticker display | Re-confirm per-ticker display when held tickers exist |
| Holiday Guard | Decide the API fallback policy |
| stale data | Clean up `connector_position_snapshot` or supplement judgment based on the latest balance |
| Stop-loss Slack | Confirm the real `INTRADAY_STOP_LOSS` event under an actual hard stop condition |
| Approval Slack | Confirm real `APPROVAL_REQUIRED` reception in the next regular Step 1~11 cycle |
| Related spec | 03, 04, 05 |

#### Order validation remaining axis

| Item | Value |
| --- | --- |
| Complete | Plan → Order → Connector Request → Fill → Position validator |
| Remaining | Signal Order Map · Broker acceptance segment |
| NO_TARGET | A normal no-order case is handled as explicit success |
| fail-closed | Cycles with actual orders keep failure on a chain mismatch |
| Related spec | 03, 04 |

### 🟠 Next — start after preparation is complete

| Item | Value |
| --- | --- |
| View deployment | Formalize Dockerfile · ECR tag · Task Definition · Service |
| Daily orchestration | State Machine · Scheduler built·ENABLED complete · structure failure propagation (ECS ExitCode · SFN Fail · failure Slack) consistently |
| Intraday | 1-week Stop Sell order test after an actual held position occurs |
| KRX | Observe Python failure propagation · DB validator in live operation during normal automatic cycles |
| Approval Slack | Show real values instead of 0/0 · display KRX reference date |
| Daily Batch UI | payload·account·ARN redaction · UTC→KST · operator-friendly wording |
| Dispatcher | Structured logs for runDate · scheduleType · holiday skip |
| stale order | Clean up the 6 ACCEPTED requests from 2026-04-27 |
| View backend | Decide to exclude Local File execution on ECS or keep it operator-only |
| Related spec | 03, 04, 05, 06, 07, 08, 10 |

#### P2 — View operations security·display enhancement (out-of-scope decision)

ALB · HTTPS · Route53 · authentication · Auto Scaling · Blue/Green · UI display enhancement · external public operation are excluded from the current active follow-up work. This is not a failure·incomplete state but the operator's deliberate out-of-scope decision.

| Item | Value |
| --- | --- |
| P2 status | Not performed |
| Current scope | Retain the ECS Fargate initial validation state |
| Reason | Personal operation·portfolio demonstration purpose |
| Re-review | When external exposure or multi-user operation is needed |

### 🟠 Later — long-term candidates

| Item | Value |
| --- | --- |
| Common package | `port_strategy_common` wheel · CodeArtifact · version management |
| Research adapter | Move the 3 internal adapters to the common package |
| Heavy job | `block_watch_*` · `block_exception_buy_*` operating procedures |
| S3 | lifecycle · KMS encryption policy |
| Holiday | Backup path · fallback policy |
| Slack | DLQ · retry · CloudWatch Alarm |
| Intraday | Enter automatic `INTRADAY_STOP_SELL` ENABLE |
| DB operations | Standardize pgAdmin4 paper/live separate registration |
| KRX | Headless refactoring of GUI collection |
| Live | Re-review the automation cutover phase |

### 🔴 Blocked — entry blocked

#### aws-live automatic BUY / SELL

| Item | Value |
| --- | --- |
| Status | 🔴 Blocked |
| Blocking reason | live automatic retry prohibition policy · insufficient paper stable cycles |
| Release condition | Accumulate stable cycles for the 7 paper automations · enter the 10 spec phase |
| Basis | OD-SAFE-002 · OD-SAFE-003 |

#### DB password rotate

| Item | Value |
| --- | --- |
| Status | 🔴 Blocked |
| Blocking reason | Awaiting 06 spec approval |
| Prerequisite | secret loader · environment-variable re-validation |
| Release condition | Rotation after operator approval |
| Risk | R-SEC-010 |

#### Slack webhook secret migration

| Item | Value |
| --- | --- |
| Status | 🔴 Blocked |
| Current | Lambda environment variable in plaintext |
| Release condition | 06 spec phase · add Notifier IAM permission |
| Risk | R-AUTO-024 |

#### Real-candidate-based validation·local environment

| Item | Value |
| --- | --- |
| retry-normalizer | Blocked until a REQUESTED · retry real candidate occurs |
| Real-candidate condition | Post-close REJECTED · `40580000` · `EGW00201` |
| psql PATH | Not registered on the operator's local PC |
| Release condition | `setx PATH` or GUI registration |
| Risk | R-AUTO-013 |

### 🟢 Done recently — recently completed

#### 2026-07-22 — Paper Daily normal automatic cycle success · 1st stabilization complete

| Item | Value |
| --- | --- |
| Step 1~11 | Normal Scheduler automatic execution SUCCEEDED |
| Step 12~17 | Normal Scheduler automatic execution SUCCEEDED |
| DB after-check | Daily Run COMPLETED · Execution Plan READY |
| validator | READY Plan → Order · Order Chain passed automatically |
| Order·fill | 한국전력 83 shares BUY FILLED · Position 83 shares OPEN |
| Slack·OPS | Success Slack received automatically · success record confirmed |
| P1 acceptance | Next normal automatic cycle end-to-end observation complete |
| Declaration | Paper Daily 1st stabilization complete |
| P2 decision | View operations security·display enhancement not performed (out of scope) |
| Evidence | [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

#### 2026-07-21 — normal no-order validator fix

| Item | Value |
| --- | --- |
| Problem | A normal 0-order cycle was FAILED as `NO_EXECUTION_ORDER_TARGET` |
| Fix | Added `--allow-no-target` to the State Machine command |
| Validation | ASL OK · deployment re-query · validator standalone ECS smoke ExitCode 0 |
| Preserved | Retained the 09:01 automatic execution FAILED history |
| Not performed | No full Step 12~17 re-execution |
| Evidence | [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

#### 2026-07-21 — KRX failure propagation·DB validator

| Item | Value |
| --- | --- |
| Python | non-zero exit on failure·partial error·undetermined state |
| runner | Validate expected trade date · Program 1 record · Shortsell at least 300 records |
| Standalone result | Program 1 record · Shortsell 349 records · ExitCode 0 |
| Risk | R-DATA-017 Open → Mitigated |
| Not performed | No crawler re-collection · no full State Machine re-execution |
| Evidence | [08 operation-notes](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) |

#### 2026-07-20 — Step 13 recovery·P0 hardening

| Item | Value |
| --- | --- |
| Incident | `EGW00201` rate-limit |
| Recovery | Step 12 not re-executed · Step 13~17 completed manually |
| Fill | 2 sell orders FILLED |
| Improvement | Query interval · limited polling · Step 14~16 fail-closed |
| Slack | Failure path · display of successful fill list |
| Evidence | [03 operation-notes](../03-marketconnector-ec2/operation-notes.md) · [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

#### 2026-07-15 ~ 2026-07-16 — KST·Daily automation validation

| Item | Value |
| --- | --- |
| KST fix | Removed naive date misjudgment · ECS `TZ=Asia/Seoul` |
| BUY E2E | 2 candidates · order submission · fill · Fill · Position · balance refresh |
| Slack | Approval · Daily Brief received in live operation |
| Evidence | [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

#### 2026-07-01 ~ 2026-07-09 — automation baseline

| Item | Value |
| --- | --- |
| Scheduler | 7 ENABLED · Asia/Seoul · weekdays |
| OPS Mirror | Recorder Lambda · run-level · representative step |
| View | Daily Batch / OPS Mirror query confirmed |
| KRX | Windows EC2 timezone changed to KST · freshness restored |
| Evidence | [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) · [05 operation-notes](../05-port-view-ecs-and-runbook/operation-notes.md) · [08 operation-notes](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) |

## Spec Roadmap

This section is an index for quickly checking the role and next entry point of specs 02~10. Date-by-date execution history and detailed validation results are managed in each spec's `operation-notes.md`.

### Environment model

| Item | Value |
| --- | --- |
| `local-dev` | Local development·unit validation environment · no AWS resources |
| `aws-paper` | AWS operating validation environment based on KIS paper trading |
| `aws-live` | The real-account environment entered after paper validation |
| AWS dev | Not built separately |
| Automatic retry | Only idempotent steps allowed |
| Order-related steps | No automatic retry for BUY · SELL · Fill Sync · Position change · Intraday Stop SELL |
| NAT Gateway | Not used by default in both paper and live |

### Progression order

| Order | Spec |
| --- | --- |
| 1 | `02-aws-network-and-rds` |
| 2 | `06-secrets-and-iam` |
| 3 | `03-marketconnector-ec2` |
| 4 | `08-interest-crawler-and-preprocessor-ecs` |
| 5 | `04-strategy-batch-stepfunctions` |
| 6 | `05-port-view-ecs-and-runbook` |
| 7 | `09-strategy-research-batch` |
| 8 | `07-cicd-pipelines` |
| 9 | `10-cutover-and-validation-runbook` |

### 02-aws-network-and-rds

| Item | Value |
| --- | --- |
| Purpose | Finalize the shared infrastructure based on VPC · Subnet · Route Table · Security Group · RDS |
| Target | Shared across the 8 MS |
| Key decisions | NAT-free · Private RDS · schema-per-domain · paper/live separation |
| Current status | aws-paper-based construction and operating validation in progress |
| Key follow-ups | DB password rotation · app role permission cleanup · live RDS design |
| Prerequisite | `01-aws-migration-foundation` |
| Next links | 06 · 03 · 08 · 04 · 05 · 09 · 10 |
| Detail | [operation-notes](../02-aws-network-and-rds/operation-notes.md) |

### 06-secrets-and-iam

| Item | Value |
| --- | --- |
| Purpose | Finalize the standard for Secrets Manager · SSM Parameter Store · IAM Role/Policy |
| Target | Shared across the 8 MS |
| Key decisions | Role-based access · no Access Key · Resource/Action least privilege |
| Current status | Main paper Roles·Secrets applied |
| Key follow-ups | DB password rotate · Slack webhook secret migration · permission audit |
| Prerequisite | 02 |
| Next links | 03 · 04 · 05 · 07 · 08 · 09 · 10 |
| Detail | [operation-notes](../06-secrets-and-iam/operation-notes.md) |

### 03-marketconnector-ec2

| Item | Value |
| --- | --- |
| Purpose | Operate MarketConnector as EC2 + EIP |
| Target | `port-marketconnector` |
| Key decisions | Fixed broker-registered IP · single token/session · SSM operation |
| Current status | Validated the operating path for query · order submission · fill query · balance refresh |
| Key follow-ups | Intraday Stop Sell validation with real positions · stale request cleanup |
| Prerequisite | 02 · 06 |
| Next links | 04 · 05 · 10 |
| Detail | [operation-notes](../03-marketconnector-ec2/operation-notes.md) |

### 08-interest-crawler-and-preprocessor-ecs

| Item | Value |
| --- | --- |
| Purpose | Finalize the AWS execution model for the Crawler and Preprocessor |
| Target | `port-interest-crawler` · `port-interest-preprocessor` |
| Key decisions | non-GUI on ECS Fargate · KRX GUI on a Windows EC2 worker |
| Current status | Hybrid execution · KRX failure propagation · DB validator applied |
| Key follow-ups | Observe normal automatic cycles in live operation · long-term review of headless refactoring |
| Prerequisite | 02 · 06 |
| Next links | 04 · 05 · 10 |
| Detail | [operation-notes](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) |

### 04-strategy-batch-stepfunctions

| Item | Value |
| --- | --- |
| Purpose | Operate the Daily Batch and Intraday flow with Step Functions + ECS |
| Target | `port_strategy_decision` · `port_strategy_execution` |
| Key decisions | Standard workflow · no automatic Retry on order steps · separate approval/failure paths |
| Current status | Step 1~11 · Step 12~17 normal automatic cycle success · Paper Daily 1st stabilization complete |
| Key follow-ups | OPS Mirror detailing · long-term incident-free cycle observation |
| Prerequisite | 02 · 06 · 03 · 08 |
| Next links | 05 · 10 |
| Detail | [operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

### 05-port-view-ecs-and-runbook

| Item | Value |
| --- | --- |
| Purpose | Migrate port-view to an ECS Fargate operations console |
| Target | `port-view` |
| Key decisions | View has a display·control role · the actual Batch runs on Step Functions |
| Current status | Retain the ECS Fargate initial validation state · Dashboard/OPS Mirror query validated |
| Key follow-ups | P2 operations security·display enhancement out of scope (re-review at external exposure·multi-user) |
| Prerequisite | 02 · 04 · 06 |
| Next links | 07 · 10 |
| Detail | [operation-notes](../05-port-view-ecs-and-runbook/operation-notes.md) |

### 09-strategy-research-batch

| Item | Value |
| --- | --- |
| Purpose | Operate long-running backtests and report generation with AWS Batch |
| Target | `port_strategy_research` |
| Key decisions | AWS Batch + S3 · Step Functions auxiliary |
| Current status | AWS Batch 1st execution validation complete |
| Key follow-ups | S3 lifecycle · KMS · heavy job operating procedures |
| Prerequisite | 02 · 06 |
| Next links | 07 · 10 |
| Detail | [operation-notes](../09-strategy-research-batch/operation-notes.md) |

### 07-cicd-pipelines

| Item | Value |
| --- | --- |
| Purpose | Standardize GitHub Actions → ECR → ECS/EC2 deployment |
| Target | Shared across the 8 MS |
| Key decisions | paper automatic deployment · live manual approval · OIDC Role |
| Current status | Awaiting follow-up entry |
| Key follow-ups | Common workflow · image promotion · rollback · package version management |
| Prerequisite | 02 · 06 · at least 1 deploy-target MS stabilized |
| Next links | 10 |
| Detail | [spec folder](../07-cicd-pipelines/) |

### 10-cutover-and-validation-runbook

| Item | Value |
| --- | --- |
| Purpose | Finalize the local-dev → aws-paper → aws-live transition and rollback procedure |
| Target | Shared across the 8 MS |
| Key decisions | paper validation period · live entry criteria · rollback trigger |
| Current status | aws-live entry remains blocked |
| Key follow-ups | Design the live cutover phase after accumulating paper stable cycles |
| Prerequisite | paper validation complete for the live-target items of 02~09 |
| Next links | Final operations transition |
| Detail | [spec folder](../10-cutover-and-validation-runbook/) |

### Dependencies

```text
01 Foundation
  ├─ 02 Network & RDS
  └─ 06 Secrets & IAM
        ↓
03 MarketConnector
        ↓
08 Crawler & Preprocessor
        ↓
04 Strategy Batch
        ↓
05 View
        ↓
09 Research
        ↓
07 CI/CD
        ↓
10 Cutover
```

## History Management Principles

| Item | Value |
| --- | --- |
| This document | Keeps only current follow-up work · blocked items · recent completions · spec roadmap |
| Date-by-date work | Recorded in the root `WORKLOG.md` |
| Document changes | Recorded in the root `CHANGELOG.md` |
| Detailed execution evidence | Recorded in each spec's `operation-notes.md` |
| Decision changes | Recorded in `operator-decisions.md` |
| Risk changes | Recorded in `risk-register.md` |
| Old completions | Not accumulated in the body; replaced with WORKLOG and operation-notes links |

## Update Rules

| Item | Value |
| --- | --- |
| New follow-up | Add to one of Now · Next · Later · Blocked |
| Completed item | Add to Done Recently |
| Done Recently scope | Keep only the most recent 5~10 key completions |
| Detailed description | Write as short factual sentences in a 2-column table |
| Long execution history | Separate into the relevant spec's `operation-notes.md` |
| No duplication | Do not repeat the same execution history in the Dashboard and Spec Roadmap |
| Status change | Update the status and next action of an existing item |
| Sensitive information | Only `[REDACTED]`-style placeholders are used |

## Security Notes

| Item | Value |
| --- | --- |
| Actual AWS execution | Out of scope of this document |
| Code changes | Out of scope of this document |
| broker · KIS · DB execution | Prohibited |
| aws-live automatic orders | Prohibited until separate approval apart from paper validation |
| Automatic retry | Only idempotent steps allowed |
| Verbatim sensitive information | Recording prohibited |
| AWS dev environment | Not built |
