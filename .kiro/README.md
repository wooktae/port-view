# PORT-STRATEGY-AI AWS Migration Specs

## Purpose

This `.kiro` workspace is the space for authoring and managing the AWS Migration specs of the PORT-STRATEGY-AI portfolio.

## Scope

Although the directory itself lives inside `port-view`, the scope covered here is not the single `port-view` MS but all 8 MS of PORT-STRATEGY-AI. This workspace therefore operates as a cross-service spec repository.

- Each spec focuses only on its own scope.
- Rather than copying large content from the root common documents verbatim, summarize only the necessary parts and then reference the root common documents.
- Keep all decisions consistent with `operator-decisions.md`. When a decision changes, do not change it arbitrarily in a spec; update `operator-decisions.md` first.
- This workspace does not perform actual implementation (code changes, AWS resource creation, running operational entrypoints). Procedures that require implementation are written only as a spec or runbook.

## Current Status Dashboard

This section is a short progress summary for the operator to see at a glance.

- Detailed per-date results and follow-up handoffs are accumulated in `specs/_common/followups-overview.md` and `WORKLOG.md`.
- The operational results of each spec are accumulated in that spec's `operation-notes.md`.
- "Complete" is used only when actual data load/freshness or an end-to-end state transition has been confirmed.
- Alignment decisions: `OD-MS-020` · `OD-MS-021` · `OD-MS-023` · `OD-MS-026` · `OD-MS-027` · `OD-MS-028` · `OD-MS-029`.

### Paper Daily Step 1~11

| Item | Value |
| --- | --- |
| Progress status | 🟢 automatic ENABLED |
| 2026-07-22 run | Normal Scheduler automatic execution SUCCEEDED |
| Crawler Program | Latest trade date 2026-07-21 · 1 record |
| Crawler Shortsell | Latest trade date 2026-07-21 · 349 records |
| Daily Run | 2026-07-22 COMPLETED · AGGRESSIVE |
| Execution Plan | READY · 1 order-preparation target |
| Date basis | 2026-07-15 KST applied and complete |
| KRX worker | Python failure propagation · expected trade date validator |
| Stabilization | Paper Daily first stabilization complete |
| Latest validation date | 2026-07-22 |
| Related spec | 04, 08 |
| Details | [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |
| Related evidence | [^tz-patch]<br>[^daily-kst-2026-07-15]<br>[^daily-buy-e2e-2026-07-16]<br>[^krx-validator-2026-07-21]<br>[^paper-daily-stable-2026-07-22] |

### Paper Daily Step 12~17

| Item | Value |
| --- | --- |
| Progress status | 🟢 automatic ENABLED |
| 2026-07-22 run | Normal Scheduler automatic execution SUCCEEDED |
| READY Plan → Order | validator auto-passed |
| Order·fill | 한국전력 83 shares BUY filled |
| Order Chain | validator auto-passed |
| Position | 83 shares OPEN |
| Balance Snapshot | 2026-07-22 created |
| Slack·OPS | Success Slack auto-received · success record confirmed |
| P1 acceptance | 🟢 next normal automatic run end-to-end observation complete |
| Stabilization | Paper Daily first stabilization complete |
| 2026-07-20 history | 2 sells · Step 14~17 manual recovery · P0 reinforcement |
| 2026-07-21 history | Normal no-order automatic run FAILED preserved · `--allow-no-target` supplemented |
| Latest validation date | 2026-07-22 |
| Related spec | 04 |
| Details | [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |
| Related evidence | [^approval-slack-2026-07-15]<br>[^daily-recovery-2026-07-20]<br>[^daily-exec-slack-2026-07-20]<br>[^daily-p0-2026-07-20]<br>[^daily-noorder-2026-07-21]<br>[^paper-daily-stable-2026-07-22] |

### Intraday Stop Loss Slack

| Item | Value |
| --- | --- |
| Progress status | 🟢 integration complete |
| Latest validation date | 2026-06-30 |
| Related spec | 03, 04 |
| Details | [operation-notes](specs/03-marketconnector-ec2/operation-notes.md) |

### Daily Brief Slack

| Item | Value |
| --- | --- |
| Progress status | 🟢 automatic ENABLED |
| Holiday Guard | Applied commonly in the Dispatcher |
| Live confirmation | Post-market Slack received |
| Latest validation date | 2026-07-15 |
| Related spec | 04 |
| Details | [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |
| Related evidence | [^daily-brief-2026-07-15] |

### port-view ECS Fargate

| Item | Value |
| --- | --- |
| Progress status | 🟢 first porting validation complete |
| OPS Mirror UI | `/daily-batch` consumption confirmed |
| Current scope | Keep the ECS Fargate first-demonstration state |
| P2 enhancement | 🟠 operational security·display enhancement not performed (out of scope) |
| Re-review condition | When external exposure or multi-user operation is needed |
| Latest validation date | 2026-07-08 |
| Related spec | 05 |
| Details | [operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md) |
| Related evidence | [^ops-mirror-ui]<br>[^view-p2-2026-07-22] |

### aws-live BUY/SELL automation

| Item | Value |
| --- | --- |
| Progress status | 🔴 not started |
| Latest validation date | — |
| Related spec | 10 |
| Details | [followups-overview](specs/_common/followups-overview.md) |

[^tz-patch]: Corrected the business time zone of the ECS crawler·preprocessor and the Windows KRX worker to KST and confirmed raw-freshness recovery.
    DB timestamp storage remains UTC and operational display is converted to KST.
    The core axis of the Step2B false-success was supplemented by applying the 2026-07-21 runner DB validator.
    Details: `WORKLOG.md` 2026-07-09·2026-07-21, `risk-register.md` R-DATA-010·R-DATA-017·R-AUTO-020.

[^krx-validator-2026-07-21]: Propagated the KRX Program·Shortsell Python failure result as a non-zero exit and added an expected trade date·row count validator at the runner back end.
    The criteria are exactly 1 record for Program and at least 300 records for Shortsell.
    The standalone validation was Shortsell 349 records·ExitCode 0.
    Details: `WORKLOG.md` 2026-07-21, `risk-register.md` R-DATA-017·R-AUTO-020, `operator-decisions.md` OD-MS-026.

[^ops-mirror-ui]: Confirmed the OPS Mirror load of the Step Functions execution history and the `/daily-batch` screen consumption.
    Validated run·step log, execution mode, detail rendering and AWS execution button display.
    payload redaction·account-number masking·UTC→KST display are follow-ups.
    Details: `WORKLOG.md` 2026-07-08, `operator-decisions.md` OD-MS-039, `risk-register.md` R-AUTO-038.

[^daily-brief-2026-07-15]: Integrated the Daily Brief Holiday Guard into the Dispatcher and organized the Builder to focus on message generation·DB query.
    The post-market live smoke confirmed Step Functions SUCCEEDED and Slack reception.
    The 07:50 pre-market automatic reception is a follow-up.
    Details: `WORKLOG.md` 2026-07-15, `risk-register.md` R-AUTO-035, `operator-decisions.md` OD-MS-032·OD-MS-038.

[^daily-kst-2026-07-15]: Confirmed the cause where Monday BUY candidates were processed as WEEKEND·NO_TARGET due to a UTC date misjudgment.
    Unified the business-date judgment of ECS and the MarketConnector EC2 to the Asia/Seoul basis.
    Observing whether it automatically recurs on the next business day is a follow-up.
    Details: `WORKLOG.md` 2026-07-15, `risk-register.md` R-DATA-010·R-DATA-017, `operator-decisions.md` OD-MS-040.

[^daily-buy-e2e-2026-07-16]: Confirmed the order·fill·Fill·Position E2E of 2 Paper Daily BUYs.
    The problem where the early query was too fast and internal state stayed at partial-fill·accepted was recovered to consistency by re-running Step13·15·16.
    The wait time after Step12 was adjusted to 60 seconds.
    polling·processed-count validation is a follow-up.
    Details: `WORKLOG.md` 2026-07-16, `risk-register.md` R-AUTO-037·R-AUTO-038·R-BROKER-004.

[^daily-recovery-2026-07-20]: KIS `EGW00201` occurred during the Step13 multi-order query and the automatic execution ended as FAILED.
    Applied a 5-second wait between queries and a limited retry, and manually recovered Step13~17.
    The 09:01 automatic failure history is preserved and the manual recovery is not recorded as an automatic success.
    Details: `WORKLOG.md` 2026-07-20, `risk-register.md` R-AUTO-001·R-AUTO-037·R-AUTO-038·R-BROKER-004.

[^daily-exec-slack-2026-07-20]: Reinforced the Builder and Notifier to display the day's bought·sold filled tickers and quantities in the Daily execution success Slack.
    Confirmed actual Slack reception in the Builder→Notifier manual smoke.
    This result does not mean the 09:01 automatic execution succeeded.
    Details: `WORKLOG.md` 2026-07-20, `risk-register.md` R-AUTO-023, `operator-decisions.md` OD-MS-009·OD-MS-030·OD-MS-031.

[^daily-p0-2026-07-20]: Applied Step13 active-order polling and Step14~16 processed-count fail-closed to operations.
    Connected the READY Plan·Order and the full order-chain validator.
    Also applied the Step12 failure Slack path to operations.
    Standalone smoke and connection validation were completed, but full normal automatic-run acceptance was left as P1.
    Details: `WORKLOG.md` 2026-07-20, `risk-register.md` R-AUTO-001·R-AUTO-023·R-AUTO-037·R-AUTO-038·R-BROKER-004.

[^daily-noorder-2026-07-21]: A normal no-order run was processed as FAILED due to the missing `--allow-no-target` in `P0_ValidateOrderChain`.
    Supplemented the State Machine command and confirmed target=0·errors=0·ExitCode 0 in the validator standalone smoke.
    The 09:01 automatic failure history is preserved and the full Step12~17 re-run was not performed.
    Details: `WORKLOG.md` 2026-07-21, `risk-register.md` R-AUTO-001·R-AUTO-037, `operator-decisions.md` OD-SAFE-004·OD-MS-032.

[^approval-slack-2026-07-15]: Fixed the problem where the Approval Required Slack mixed different Daily Runs and Execution Plans.
    Aligned status·reference date·signal·candidate·Plan on a single `daily_run_id` basis and structured the candidate scores.
    The Builder→Notifier→Slack E2E is complete and the next automatic Scheduler reception is a follow-up.
    Details: `WORKLOG.md` 2026-07-15, `operator-decisions.md` OD-MS-031.

[^paper-daily-stable-2026-07-22]: In the 2026-07-22 normal Scheduler automatic run, Step 1~11 and Step 12~17 completed as SUCCEEDED without forced orders or induced errors.
    Confirmed Crawler validation·Daily Run COMPLETED·Execution Plan READY·2 validators auto-passed·한국전력 83 shares order·fill·Position 83 shares OPEN·Balance Snapshot·success Slack·OPS success record.
    The P1 next-normal-automatic-run end-to-end observation is marked complete and Paper Daily is declared first-stabilization complete.
    This does not mean aws-live readiness·long-term incident-free operation·full AWS Migration completion, and the 2026-07-20·2026-07-21 FAILED history is preserved.
    Details: `WORKLOG.md` 2026-07-22, `operator-decisions.md` OD-SAFE-001.

[^view-p2-2026-07-22]: The operator decided not to perform P2 View operational security·display enhancement (ALB·HTTPS·Route53·authentication·Auto Scaling·Blue/Green·UI enhancement·external exposure) within the current portfolio scope.
    This is not a failure or incompleteness but an intentional scope exclusion, and port-view keeps the ECS Fargate first-demonstration state.
    It is re-reviewed when external exposure or multi-user operation is needed.
    Details: `WORKLOG.md` 2026-07-22, `operator-decisions.md` OD-MS-002.

## Important Safety Notes

> <span style="color:#D1242F">**aws-live BUY/SELL automation remains in the not-started state.**</span>
>
> This workspace does not perform actual implementation (code changes, AWS resource creation, running operational entrypoints).
>
> Each MS's source code, README, CHANGELOG, worklog and AGENTS.md are not spec-work targets of this workspace and are not modified unless explicitly requested.
>
> The operator-local PowerShell wrappers under `.kiro/scripts/` are operator-local PC tools; they are not Kiro auto-execution targets and are not executed inside EC2 / ECS / Batch either.
>
> Actual secret, password, token, app key, app secret, account number and webhook URL are never written in any document of this workspace, and only `[REDACTED]` is used where sensitive information would be needed.

## Where to Read More

- 04 Strategy Batch StepFunctions — [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)
- 03 MarketConnector EC2 — [operation-notes](specs/03-marketconnector-ec2/operation-notes.md)
- 05 port-view ECS · Runbook — [operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md), [runbook](specs/05-port-view-ecs-and-runbook/runbook.md)
- 02 AWS Network · RDS — [operation-notes](specs/02-aws-network-and-rds/operation-notes.md)
- 06 Secrets · IAM — [operation-notes](specs/06-secrets-and-iam/operation-notes.md)
- Common — [operator-decisions](specs/_common/operator-decisions.md), [risk-register](specs/_common/risk-register.md), [followups-overview](specs/_common/followups-overview.md)

## Target Microservices

The microservices covered by this workspace's specs are the following 8.

- `port-marketconnector`
- `port-view`
- `port-interest-crawler`
- `port-interest-preprocessor`
- `port_strategy_common`
- `port_strategy_decision`
- `port_strategy_research`
- `port_strategy_execution`

Each MS's source code, README, CHANGELOG, worklog and AGENTS.md are not spec-work targets of this workspace and are not modified unless explicitly requested.

## Directory Map

```
.kiro/
├── AGENTS.md           # Kiro working rules (scope, single source-of-truth documents, security/execution rules)
├── README.md           # This document. .kiro workspace guide
├── CHANGELOG.md        # spec document change history
├── WORKLOG.md          # simple work log (accumulative, single file)
└── specs/              # AWS Migration specs and root common reference documents
    ├── 01-aws-migration-foundation/
    │   ├── requirements.md
    │   ├── design.md
    │   ├── tasks.md
    │   └── traceability-matrix.md
    ├── 02-aws-network-and-rds/
    │   ├── README.md
    │   ├── requirements.md
    │   ├── design.md
    │   ├── tasks.md
    │   ├── decision-matrix.md
    │   ├── runbook.md
    │   ├── validation-checklist.md
    │   ├── operation-notes.md
    │   └── traceability-matrix.md
    ├── _common/
    │   ├── operator-decisions.md
    │   ├── ms-aws-service-decision-matrix.md
    │   ├── cost-simulation.md
    │   ├── followups-overview.md
    │   ├── aws-resource-glossary.md
    │   └── risk-register.md
    └── _archive/
        └── note-aws-landscape-2021-vs-2026.md
```

## Key Documents

| File | Role |
| --- | --- |
| `AGENTS.md` | The working rules of the Kiro workspace.<br>Defines the work scope, single source-of-truth documents, per-MS `AGENTS.md` references, spec authoring, security and execution rules. |
| `README.md` | This document.<br>Guides the purpose, scope, folder structure and key document roles of the `.kiro` workspace. |
| `CHANGELOG.md` | Records only the spec document change history inside `.kiro`.<br>Does not record the code·document change history of each MS. |
| `WORKLOG.md` | Accumulates a simple log of Kiro work sessions in a single file.<br>Does not create per-date worklog files. |
| `specs/` | Holds the AWS Migration spec folders and the root common reference documents. |

## Common Reference Documents

Before creating a new spec or modifying an existing spec, first check the following single source-of-truth documents.

| File | Role |
| --- | --- |
| `specs/_common/operator-decisions.md` | The single source-of-truth document for operator decisions.<br>Manages environment, network, RDS, DB schema·role, compute·service placement, security, observability, cutover and safety decisions. |
| `specs/_common/ms-aws-service-decision-matrix.md` | The single source-of-truth document for the per-MS AWS service recommendation and rationale across the 8 MS.<br>Compares the primary compute and orchestration decisions. |
| `specs/_common/cost-simulation.md` | The single source-of-truth document for cost assumptions and expected monthly cost per environment.<br>Organizes RDS size, VPC Endpoint count, ALB·NAT usage and Fargate usage. |
| `specs/_common/followups-overview.md` | Manages the progression order and dependency map of follow-up specs 03~10. |
| `specs/_common/aws-resource-glossary.md` | The single source-of-truth document for AWS terminology explanations. |
| `specs/_common/risk-register.md` | The single accumulative document for AWS Migration operational·security·cost Risks.<br>When a new Risk is identified in a follow-up spec, it is added in the same format. |
| `specs/_archive/note-aws-landscape-2021-vs-2026.md` | A reference note comparing the 2021 AWS configuration with the 2026 recommendation. |

## Spec Document Types

Each spec such as 01-aws-migration-foundation and 02-aws-network-and-rds may have the following auxiliary documents in addition to the body (`requirements.md`, `design.md`, `tasks.md`, and `decision-matrix.md` when needed).

- `runbook.md` — An execution procedure the operator can follow step by step in the AWS Console. Each Step consists of the 5 elements below.

  | Item | Value |
  | --- | --- |
  | Purpose | Required |
  | Preconditions | Required |
  | Console operation order | Required |
  | Post-creation confirmation | Required |
  | Action on failure | Required |
- `validation-checklist.md` — A document that checks pass/fail at the checkbox level after running the runbook.
- `traceability-matrix.md` — A mapping table of requirement → design → task → validation → operator decision.
- Root common `risk-register.md` — Accumulatively manages risks across all specs in a single table.

## Update Rules

- When the workspace's purpose, folder structure or key document list changes, update this `README.md`.
- When a spec structure change, a root common document change, a new spec creation or a major document reorganization occurs, update `CHANGELOG.md`.
- When a meaningful Kiro document work session ends, accumulate it briefly in `WORKLOG.md`.
- When the Kiro working rules (scope, single source-of-truth documents, security/execution policy) change, update `AGENTS.md`.

## Security Rules

- Actual secret, password, token, app key, app secret, account number and webhook URL are never written in any document of this workspace.
- Only `[REDACTED]` is used where sensitive information would be needed.
- Sensitive-information values are not printed anywhere in examples, tables, summaries, logs or generated documents.

## Operator-local PowerShell wrapper

The `.kiro/scripts/` folder of this workspace contains a wrapper for the operator to
reproduce the Daily AWS 17-step step by step in local Windows PowerShell.

The wrapper is an **operator-local PC tool**.

- Not a Kiro auto-execution target
- Not executed inside EC2 · ECS · Batch
- Separated from the source · packaging · docs of the 8 MS repositories
- Separated from the worklog · README · AGENTS.md · CHANGELOG areas

The operational policy · safety criteria · follow-up responsibilities of this wrapper are accumulated in the single source-of-truth documents below. This README section contains only a short guide for the operator to see at a glance.

| File | Role |
|---|---|
| [`specs/_common/operator-decisions.md`](specs/_common/operator-decisions.md) | `OD-MS-023` |
| [`specs/_common/risk-register.md`](specs/_common/risk-register.md) | `R-AUTO-019` |
| [`specs/_common/followups-overview.md`](specs/_common/followups-overview.md) | 2026-06-17 third follow-up memo |
| [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) | 2026-06-17 (Daily AWS PowerShell wrapper implementation) section |

### Folder structure

```
.kiro/scripts/
├── run-daily-aws-paper.ps1                # main wrapper
├── daily-aws-paper.config.ps1             # region / cluster / instance id / subnet / SG / task definition / job definition / log group / output path
├── daily-aws-paper.functions.ps1          # SSM / ECS RunTask / AWS Batch SubmitJob common functions + Step registry + summary
├── run-daily-aws-paper-bundled.ps1        # single-file bundled wrapper (not created on this date / optional follow-up)
└── steps/
    ├── step-01-connector-balance.ps1
    ├── step-02-interest-crawler.ps1
    ├── step-03-preprocessor.ps1
    ├── step-04-backtest-research.ps1
    ├── step-05-backtest-report.ps1
    ├── step-06-daily-buy-signal.ps1
    ├── step-07-daily-position-signal.ps1
    ├── step-08-daily-buy-execution.ps1
    ├── step-09-daily-sell-execution.ps1
    ├── step-10-daily-auto-sell.ps1                                  # --execute = strategy execution internal state update (not direct broker submission)
    ├── step-11-daily-auto-buy.ps1                                   # --execute = strategy execution internal state update (not direct broker submission)
    ├── step-12-marketconnector-strategy-order-execute.ps1           # can submit an actual KIS paper order — blocked by default / allowed only when -AllowPaperOrderExecute is explicit
    ├── step-13-connector-order-check.ps1                            # order status query / DB status update (no new order submission)
    ├── step-14-sync-sell-fill.ps1                                   # SELL fill / status DB update (no broker call)
    ├── step-15-sync-buy-fill.ps1                                    # BUY fill / status DB update (no broker call)
    ├── step-16-sync-buy-position.ps1                                # BUY position DB update (no broker call)
    └── step-17-balance-refresh.ps1                                  # balance / position snapshot refresh (no new order submission)
```

### Key options

| Item | Value |
| --- | --- |
| `-RunDate` | Execution reference date (KST).<br>Uses the current date if unspecified |
| `-Region` | Default `ap-northeast-2` |
| `-Environment` | Only `aws-paper` is allowed<br>No `aws-live` branch exists in the code |
| Policy basis | R-AUTO-002 mitigation<br>OD-SAFE-002 · OD-SAFE-003 |
| `-StartStep` · `-EndStep` | Specify a partial execution range |
| Partial execution example | `-StartStep 1 -EndStep 7` |
| Single Step execution | Always use the `-StartStep N -EndStep N` form |
| Wrong parameters | `-FromStep` · `-ToStep` are not defined in the wrapper |
| Misinput risk | PowerShell may ignore an undefined parameter<br>Risk of entering the default range `1~17` |
| Related basis | 2026-06-18 operation memo<br>OD-MS-023 · OD-MS-025 |
| `-DryRun` | Print the execution plan only |
| DryRun execution impact | 0 ECS RunTask<br>0 Batch SubmitJob<br>0 SSM command |
| DryRun purpose | Confirm the `FOUND` · `MISSING` status of every Step |
| `-AllowPaperOrderExecute` | Explicitly allow the KIS paper order submission of Step 12 |
| Default | OFF |
| Default-block structure | wrapper central `PAPER_ORDER_GATE`<br>Step 12 internal gate |
| Related Risk | R-AUTO-019 mitigation |
| Operating principle | Allowed only when the operator explicitly specifies the option |

### Step 12 safety notes

| Item | Value |
| --- | --- |
| Step | Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` |
| Called script | `connector_strategy_order_execute.py --execute` |
| Order impact | Can submit an actual KIS paper order |
| Default execution state | Order submission blocked |
| Block location | wrapper central `PAPER_ORDER_GATE`<br>Step 12 internal gate |
| Related basis | OD-MS-023<br>R-AUTO-019 |
| Order allowance condition 1 | `-AllowPaperOrderExecute` explicit |
| Order allowance condition 2 | `Environment=aws-paper` |
| Order allowance condition 3 | `PORT_DB_TARGET=aws-paper` guard passed |
| Related Risk | R-AUTO-009<br>R-AUTO-010 |
| live automatic BUY·SELL | Prohibited until follow-up validation and approval |
| live policy | OD-SAFE-002<br>OD-SAFE-003 |

### Step 12 operational demonstration and recovery principle

| Item | Value |
| --- | --- |
| First demonstration date | 2026-06-18 |
| Execution result | The operator submitted 4 paper BUYs |
| Execution condition | `-AllowPaperOrderExecute` explicitly specified |
| First incident | KIS paper API read timeout |
| Recovery method | Retry after a controlled `REQUESTED` recovery |
| Demonstration Risk | R-BROKER-004 mitigation first demonstration |
| Prohibited | A simple re-run without a status check |
| Reason for prohibition | Broker duplicate-order risk |
| Required procedure | Apply the pre-check pattern |
| Detailed basis | [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) |

### Step 10~17 responsibility boundary

| Item | Value |
| --- | --- |
| Step 10 · Step 11 `--execute` | strategy execution internal state creation·update |
| Direct broker submission | None |
| Direct KIS submission | None |
| Related decision | OD-MS-016 |
| Step 13~17 | DB status update·query·snapshot refresh |
| New broker orders | 0 |
| New KIS orders | 0 |
| Step 13 demonstration | broad summary fallback auto-skip |
| Fill reflection method | `--code` · `--order-no` · `--no-broad` single query |
| Related Risk | R-AUTO-018 |
| Step 16 reinforcement | Resolve the unique constraint conflict on additional buys |
| Modified file | `execution_sync_buy_position.py` |
| Modification method | merge handling |
| Related decision·Risk | OD-MS-024<br>R-DATA-012 |
| Detailed basis | [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) |

### Step 13 safety notes

| Item | Value |
| --- | --- |
| Step | Step 13 `CONNECTOR_ORDER_CHECK` |
| Execution method | MarketConnector EC2 SSM RunCommand |
| Called script | `connector_order_check.py` |
| New order submission | None |
| Responsibility | Order status query<br>DB status update |
| Operational reinforcement date | 2026-06-18 |
| Default behavior change | broad bulk query → active-order single sequential query |
| Related decision | OD-MS-025 |
| wrapper responsibility | Step 13 orchestration |
| Query-method responsibility | Inside `connector_order_check.py` |
| Related Risk | R-AUTO-018 |

#### `connector_order_check.py` key options

| Item | Value |
| --- | --- |
| `--broad` | legacy·diagnostic<br>not used by default |
| `--active-limit` | Max count for active-order single sequential query |
| `--code {ticker_code}` | Explicit ticker code |
| `--order-no {broker_order_no}` | Explicit broker order number |
| `--no-broad` | Disable broad query |
| Explicit-order query combination | `--code` + `--order-no` + `--no-broad` |

#### Summary fallback application criteria

| Item | Value |
| --- | --- |
| Multiple active orders | Does not use the KIS `inquire-daily-ccld` summary fallback as a DB-reflection basis |
| Allowance condition | The `connector_order_request` candidate is confirmed as 1 record by order number·ticker code |
| Related Risk | R-AUTO-018 mitigation |
| Detailed response | [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) |

#### Step 13 standalone execution

| Item | Value |
| --- | --- |
| Validation date | 2026-06-18 |
| Execution command | `./run-daily-aws-paper.ps1 -Environment aws-paper -RunDate 2026-06-18 -StartStep 13 -EndStep 13` |
| Result | Step 13 `COMPLETED` |
| Parameters used | `-StartStep` · `-EndStep` |
| Prohibited parameters | `-FromStep` · `-ToStep` |
| Misinput risk | Can enter the default `1~17` range |
| Detailed result | [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) |

### Windows KRX crawler worker and Step 2 behavior

- Step 2 (`INTEREST_CRAWLER`) handles the following 3 flows together.
  - (a) non-GUI ECS · Fargate Task Definition `portfolio-paper-interest-crawler:7` RunTask
  - (b) `Portfolio-KRX-Worker-Daily` Scheduled Task trigger of the Windows KRX crawler worker
  - (c) **KRX raw DB validation** (`interest_krx_raw_validate_daily.py` SSM step `INTEREST_CRAWLER_KRX_DB_VALIDATE`)
- **2026-06-21 success-judgment reinforcement** (OD-MS-026 new · R-AUTO-020 new mitigation alignment) — Step 2 is not marked SUCCESS on the Scheduled Task trigger success alone.
- SUCCESS is marked only when all of the following 5-stage gate pass.

  | Step | Result |
  |---|---|
  | 1 | Scheduled Task status polling |
  | 2 | `Last Result` check |
  | 3 | KRX worker log check |
  | 4 | `interest_program_raw` DB validation on the `ExpectedKrxRawDate` basis |
  | 5 | `interest_shortsell_raw` DB validation on the `ExpectedKrxRawDate` basis |
- For the detailed gate definition and execution result, see [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md).
- **crawler worker EC2 fail-closed** (R-AUTO-016 mitigation update) — If the EC2 instance state of the Windows KRX crawler worker is not `running`, Step 2 fails immediately.
- The previous "auto-skip the KRX GUI Scheduled Task trigger inside the wrapper" behavior was abolished on this date.
- The failure message prints `instanceId` · `state` · blocks Step 2 SUCCESS entry while the KRX GUI worker · DB validation is not performed.
- The KRX GUI path keeps the Windows Administrator interactive Scheduled Task flow rather than direct SSM python execution (OD-MS-022 · OD-MS-026 alignment).
  - `schtasks /Run /TN Portfolio-KRX-Worker-Daily` → `powershell.exe -ExecutionPolicy Bypass -File C:\portfolio\run_krx_worker_daily.ps1`
- The non-GUI ECS RunTask proceeds regardless of worker state, but injects the following 4 environment variables via `containerOverrides.environment` (mitigate Windows · Linux encoding differences · clarify the UTF-8 basis).

  | Item | Value |
  |---|---|
  | `TEMP` | `/tmp` |
  | `TMP` | `/tmp` |
  | `PYTHONUTF8` | `1` |
  | `PYTHONIOENCODING` | `utf-8` |
- **Step 2 standalone execution command** — `.\run-daily-aws-paper.ps1 -Environment aws-paper -RunDate 2026-06-20 -StartStep 2 -EndStep 2`.
- 2026-06-21 standalone execution validation passed (Status `SUCCESS`).
- If mistakenly entered with the undefined parameters `-FromStep` · `-ToStep`, there is a risk of entering the default 1 · 17.
- Always use only the `-StartStep` · `-EndStep` form.
- For the detailed RunId · Runner · DB after-check result, see [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md).

### bundled wrapper

- The `run-daily-aws-paper-bundled.ps1` single-file bundled wrapper was not created on this date.
- It can be created if needed after per-file validation of the 17 steps is complete.
- **The default development · validation · operation basis keeps the split-file structure** (OD-MS-023 alignment).

### Output location

- Per-run-id logs · overrides JSON · summary are stored under `C:\Temp\portfolio-daily-aws-paper\<run-id>\` in `logs/` · `overrides/` · `summary/run-summary.txt`.
- The wrapper summary · overrides JSON · SSM stdout · stderr files have **0** plaintext outputs of the following sensitive-information categories (R-DOCS-001 [2026-06-17 wrapper reinforcement] mitigation alignment).

  | Item | Value |
  | --- | --- |
  | secret value | 0 plaintext outputs |
  | KIS app key | 0 plaintext outputs |
  | KIS app secret | 0 plaintext outputs |
  | account number | 0 plaintext outputs |
  | token | 0 plaintext outputs |
  | RDS password | 0 plaintext outputs |
  | RDS endpoint hostname | 0 plaintext outputs |
