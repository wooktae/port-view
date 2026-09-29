# Operation Notes — 04-strategy-batch-stepfunctions

This document is an operation note that accumulates, by date, the results of work actually performed by the operator / Kiro during 04-strategy-batch-stepfunctions.

- First application environment = `aws-paper` / region = `ap-northeast-2`.
- First validation target = ECS / Fargate single RunTask validation of the Strategy Decision MS (`port_strategy_decision`).
- Outside this date's work scope = EventBridge Scheduler / Step Functions / Strategy Execution (`port_strategy_execution`) integration → responsibility of a 04 spec follow-up phase or a separate spec (execution / orchestration).

## Record Format

- Accumulate under a `## YYYY-MM-DD <summary>` header per date (same as the 02 / 03 / 06 / 08 spec operation-notes).
- Record results only briefly as success / failure / on hold / not applicable / carried over. For failure cases, summarize only 1 line of cause + 1 line of action + result.
- Do not quote the full text of AWS CLI / Console / CloudWatch logs / Task event message / docker build logs in this document (security / volume reduction).
- Record IAM Role / Policy / secret changes only as a 4-line summary of change date / changer / change reason / before·after item summary (full JSON body quoting prohibited).
- Full-text quoting of the source / Dockerfile / requirements.txt of the 8 MS is prohibited. Record only the fact that the operator newly authored them directly.

## Safety Principles

- The following values are prohibited from plaintext recording in this document — all use `[REDACTED]` or a placeholder (`<account-id>` / `<region>` / `<rds-endpoint>` / `<image-tag>` / `<image-digest>` / `<task-arn>`):
  - secret value / password / KIS app key / KIS app secret / account number.
  - RDS endpoint hostname / RDS password / token / IAM access key id / account-id.
  - actual secret ARN / actual KMS Key ARN / instance-id / image digest / task ARN.
- Recording secret query results (value) is prohibited — only success / failure + last refresh time (if needed, ISO 8601 `YYYY-MM-DDTHH:MM:SS+09:00`).
- Actual AWS resource creation / modification / deletion = performed directly by the operator / Kiro = performs only document authoring / procedure organization / validation item organization.
- The README / AGENTS.md / CHANGELOG / docs / worklog of the 8 MS are not changed by this spec work:
  - MS = `port-view` · `port-marketconnector` · `port-interest-crawler` · `port-interest-preprocessor`.
  - MS = `port_strategy_common` · `port_strategy_decision` · `port_strategy_execution` · `port_strategy_research`.
  - The Dockerfile / requirements.txt authored directly by the operator = operator's direct work / record only the fact in this note.
- Actual `secretsmanager:GetSecretValue` calls are for the operator only. Kiro automatic validation uses only `secretsmanager:DescribeSecret` metadata.
- Maintain the same principle in follow-up work so that secret value is not exposed in plaintext in the work chat / command output / console capture / CloudWatch Logs body / operator note (R-DOCS-001 consistency).

## Current Automation Lineup State (Automation Lineup Dashboard)

Summary of the 7 aws-paper Daily automation lineup states as of 2026-07-01. See the per-date sections below for detailed evidence. No change to the aws-live automatic BUY / SELL policy (OD-SAFE-002 / OD-SAFE-003 consistency).

| # | Scheduler / Component | Cron (Asia/Seoul) | State | Related Decision |
|---|---|---|---|---|
| 1 | `portfolio-paper-ec2-start-0750-kst` | 07:50 MON-FRI | 🟢 ENABLED | OD-MS-034 |
| 2 | `portfolio-daily-brief-morning-slack-0750-kst` | 07:50 MON-FRI | 🟢 ENABLED | OD-MS-038 |
| 3 | `portfolio-paper-daily-step1-11-approval-0800-kst` | 08:00 MON-FRI | 🟢 ENABLED | OD-MS-032 |
| 4 | `portfolio-paper-daily-step12-17-order-0901-kst` | 09:01 MON-FRI | 🟢 ENABLED (transitioned 2026-07-01) | OD-MS-033 |
| 5 | `portfolio-paper-intraday-snapshot-evaluate-10min-kst` | 09:10~15:50/10min | 🟢 ENABLED | OD-MS-035 |
| 6 | `portfolio-daily-brief-evening-slack-1550-kst` | 15:50 MON-FRI | 🟢 ENABLED | OD-MS-038 |
| 7 | `portfolio-paper-marketconnector-stop-1550-kst` | 15:50 MON-FRI | 🟢 ENABLED | OD-MS-034 |

3 State Machines (kept ACTIVE):

- `portfolio-paper-daily-step1-17-approval` — Step 1~11 approval workflow + `BuildApprovalSlackPayload → SendApprovalRequiredSlack` (updated 2026-06-30 afternoon).
- `portfolio-paper-daily-step12-17-approval` — Step 12~17 approval workflow (external caller consistency).
- `portfolio-paper-intraday-stop-sell-approval` — intraday stop-loss approval gate (automatic ENABLE entry is a follow-up phase responsibility).

3 auxiliary Lambdas (kept ACTIVE):

- `portfolio-event-notifier` — AWS common Slack notifier (OD-MS-030 / new 2026-06-23).
- `portfolio-approval-slack-summary-builder` — Approval Required Slack builder (new 2026-06-30 afternoon).
- `portfolio-daily-brief-slack-summary-builder` — Daily Brief Slack builder (new 2026-06-30 afternoon / OD-MS-038).
- `portfolio-paper-daily-scheduler-dispatcher` — Scheduler → Step Functions dispatch (OD-MS-032).

## Chronological Index

Core result summary of each per-date section. See the body of the relevant section for detailed execution / DB / GRANT / decision / safety-check evidence.

| Date | Core Result | New Decision / Risk |
|---|---|---|
| 2026-06-13 (1) | Strategy Decision buy-signal / position-signal ECS RunTask passed (2 Task Definitions separated) | OD-MS-013 |
| 2026-06-13 (2) | Strategy Execution `--execute` responsibility separation + View Daily Batch 17-step restructuring | OD-MS-016 / OD-MS-021 |
| 2026-06-13 (3) | Strategy Execution `execution_app` AWS Paper RDS connection pre-validation (Python psycopg2) | — |
| 2026-06-13 (4) | psql 18 + pgAdmin4 client compatibility reinforcement | — |
| 2026-06-13 (5) | Strategy Execution ECS Fargate 8-kind command override validation | OD-MS-017 |
| 2026-06-16 | Strategy Decision safe step ECS dry-run re-validation | — |
| 2026-06-17 | Daily AWS 17-step E2E first completion (positions OPEN 4) | R-DATA-005 reinforcement |
| 2026-06-18 | Wrapper 17-step live operation + additional-buy unique constraint patch | OD-MS-024 / R-DATA-012 |
| 2026-06-22 | Wrapper 2nd live operation + `execution_app` decision UPDATE GRANT correction | OD-DB-011 / R-DATA-013 |
| 2026-06-23 | Step Functions approval workflow live pass + 3-kind Slack notifier | OD-MS-028 / OD-MS-029 / OD-MS-030 / OD-MS-031 / R-AUTO-023 / R-AUTO-024 |
| 2026-06-29 (2) | port-view external caller first validation (Step 1~11) | R-AUTO-034 new |
| 2026-06-29 (3) | port-view Step 12~17 approval external caller second validation | R-AUTO-033 reinforcement |
| 2026-06-30 (morning) | 4-kind View operation path organization + Daily Batch gate fix + DB validation principle | — |
| 2026-06-30 (afternoon, ECS) | ECS View external caller third validation (Step 12~17 approval) | R-AUTO-033 reinforcement |
| 2026-06-30 (afternoon, Slack) | Approval Required Slack Builder integration + Daily Brief Slack | OD-MS-038 / R-AUTO-035 |
| 2026-06-30 (afternoon, stop-loss) | intraday stop-loss Slack live integration cross-reference | R-AUTO-036 |
| 2026-07-01 | Step 12~17 Scheduler ENABLED + 7-kind Daily automation lineup ENABLED | R-AUTO-037 new |

## 2026-06-13 Strategy Decision ECS / Fargate first porting validation

Accumulates the result of the ECS / Fargate first porting validation of `port_strategy_decision` performed directly by the operator on 2026-06-13. On this date, Kiro performed only document authoring / procedure organization / validation item organization, and the actual AWS / Docker / ECR / ECS / IAM / Secrets / RDS / GRANT work was carried out directly by the operator.

### 1. AWS Execution Structure Confirmation

1. As-Is execution method confirmation: complete
   1) Local repository path confirmation: complete (`C:\Workspaces\port_strategy_decision`)
   2) Main entrypoint candidate confirmation: complete
       - `daily_buy_signal_run.py`
       - `daily_position_signal_run.py`
   3) `port_strategy_common` import possibility confirmation: complete (based on local `PYTHONPATH=C:\Workspaces`)
   4) DB connection environment variable confirmation: complete
       - `INTEREST_DB_HOST`
       - `INTEREST_DB_PORT`
       - `INTEREST_DB_NAME`
       - `INTEREST_DB_USER`
       - `INTEREST_DB_PASSWORD`
   5) Existing Daily Batch execution structure confirmation: complete
       - `port-view` Daily Batch step 6 = `DAILY_BUY_SIGNAL`, step 7 = `DAILY_POSITION_SIGNAL`
       - Java `DailyBatchService` runs the two entrypoints sequentially as separate ProcessBuilder processes
2. Data dependency confirmation: complete
   1) preprocessor schema read: complete (input feature)
   2) research schema write: complete (`research.strategy_block_watch_candidate`)
   3) execution schema read: complete (`execution.strategy_position_state`)
   4) decision schema write: complete (`decision.strategy_daily_position_decision`)
   5) connector / reference / legacy reference confirmation: complete (no direct dependency within this date's validation scope)
3. To-Be execution method finalization: complete
   1) Plan to newly author a unified `daily_decision_run.py`: on hold
       - Inherits the structure where the existing Daily Batch runs buy-signal / position-signal as separate steps
       - Finalized in AWS too as separating the two entrypoints into separate Task Definitions
   2) First execution candidate: finalized as `daily_buy_signal_run.py`
   3) Second execution candidate: finalized as `daily_position_signal_run.py`
   4) Docker build context: finalized based on `C:\Workspaces`
   5) Source bundled in image: vendoring of the two sources `port_strategy_common` + `port_strategy_decision`
   6) `port_strategy_common` formal package / version management: follow-up (Strategy Common or DevOps enhancement stage)
   7) Task Definition separation policy: finalized this date (see OD-MS-013)
       - buy-signal: `portfolio-paper-strategy-decision-buy-signal`
       - position-signal: `portfolio-paper-strategy-decision-position-signal`
   8) EventBridge Scheduler integration / Step Functions orchestration: follow-up separation

### 2. Dockerfile / requirements.txt New Creation

1. `port_strategy_decision` first Dockerfile / requirements.txt newly created directly by the operator: complete
   1) Docker build context: `C:\Workspaces`
   2) Reflects the vendoring method of the two sources `port_strategy_common` and `port_strategy_decision` inside the image
   3) The first operational entrypoint is decided in the ECS Task Definition `command` (the image itself can run both entrypoints)
2. `port_strategy_common` formal package / version management: on hold
   1) The first ECS smoke image uses the vendoring method
   2) Formal package / version management is separated into a follow-up Strategy Common or DevOps enhancement stage

### 3. Local Image Build / Smoke

1. Local Docker build: complete
   1) `portfolio-strategy-decision:paper-20260613`
2. container import smoke success: complete
   1) `port_strategy_common` import: success
   2) `port_strategy_decision.daily_buy_signal_run` import: success
   3) `port_strategy_decision.daily_position_signal_run` import: success

### 4. ECR Repository / Push

1. ECR repository creation / confirmation: complete
   1) `portfolio-strategy-decision` repository newly created
   2) Environment separation policy: repository not separated per paper / live environment (08 spec policy consistency)
       - paper / live distinction is handled in the 6 items image tag / Task Definition / Secrets·SSM path / IAM Task Role / environment variables / RDS settings
2. ECR push: complete
   1) tag `paper-20260613`
   2) tag `paper-latest`
3. image digest: confirmed directly by the operator. The actual digest value is not recorded in this note / spec artifacts (`<image-digest>` placeholder)

### 5. CloudWatch Log Group / Secrets Manager / IAM Role

1. CloudWatch Log Group creation / confirmation: complete
   1) `/portfolio/paper/strategy-decision`
   2) retention: 14 days (08 spec consistency / OD-OBS-002 consistency)
2. Secrets Manager secret creation / confirmation: complete
   1) `/portfolio/paper/rds/decision-app`
   2) JSON multi-key method (`host` / `port` / `dbname` / `username` / `password`) — consistent with the 08 preprocessor secret pattern
   3) actual secret value / endpoint / password / ARN / account-id not recorded in this note (R-DOCS-001 / R-DATA-006 consistency)
3. ECS Task Execution Role confirmation: complete
   1) name: `portfolio-paper-ecs-task-execution-role` (first created in the 08 spec, reused this date)
   2) decision-app DB Secret read inline policy added: complete
       - secret ARN scoped / Resource·Action wildcard 0 / 03 §13 / OD-SEC-006 consistency
       - change date / changer / change reason / before·after item summary is a 4-line summary only in this note (full JSON body quoting prohibited)
4. ECS Task Role creation / confirmation: complete
   1) name: `portfolio-paper-decision-task-role`
   2) trust: `ecs-tasks.amazonaws.com`
   3) decision-app runtime-point SDK / runtime permissions were not added this date (RDS connection is handled by the Execution Role's secret injection + RDS credentials)

### 6. ECS Task Definition

1. Task Definition separated registration: complete
   1) buy-signal:
       - family: `portfolio-paper-strategy-decision-buy-signal`
       - command: `python -m port_strategy_decision.daily_buy_signal_run`
   2) position-signal:
       - family: `portfolio-paper-strategy-decision-position-signal`
       - command: `python -m port_strategy_decision.daily_position_signal_run`
2. Common items: complete
   1) network mode: `awsvpc`
   2) launch type: Fargate
   3) cpu: 512
   4) memory: 1024
   5) ECS cluster: `portfolio-paper-cluster` (first created in the 08 spec, reused this date)
   6) network: reuses the existing public subnet + assignPublicIp ENABLED + RDS-accessible ECS SG structure (OD-NET-004 consistency)
   7) log group connection: `/portfolio/paper/strategy-decision`
   8) Secret environment variable connection: 5 kinds (`INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD`)
3. Environment variable compatibility policy: uses the `INTEREST_DB_*` environment variable names as-is for compatibility with existing code (OD-DB-003 consistency)

### 7. RunTask Single-run Validation

#### 7-1. `daily_buy_signal_run` Validation

1. ECS RunTask first run: failed
   1) cause: insufficient permission on `research.strategy_block_watch_candidate`
   2) `decision_app` did not hold INSERT / UPDATE permission on the candidate table of the research schema — even if research is included in search_path, it fails without a GRANT
2. Permission correction: complete
   1) granted `decision_app` permission on `research.strategy_block_watch_candidate`
   2) corrected research schema sequence usage / select permission
3. ECS RunTask re-run: complete
   1) ECR image pull: success
   2) Secret injection: success
   3) RDS connection: success
   4) CloudWatch Logs output: success
   5) exitCode: 0
4. CloudWatch result summary: complete
   1) `daily_run_id`: 44
   2) `run_date`: 2026-06-13
   3) `data_date`: 2026-06-08
   4) `market_signal`: BLOCK
   5) `base_exposure`: 0.0
   6) `max_positions`: 0
   7) `candidates`: 0
   8) `signals`: 0
   9) `block_watch`: 1

#### 7-2. `daily_position_signal_run` Validation

1. ECS RunTask first run: failed
   1) cause: `relation "strategy_position_state" does not exist`
   2) reason: the actual table location is `execution.strategy_position_state`, but due to `decision_app`'s insufficient execution schema / table permission, the lookup fails even within search_path
2. RDS table location confirmation: complete
   1) `decision.strategy_daily_position_decision`
   2) `execution.strategy_position_state`
   3) `legacy.strategy_daily_position_decision_test_backup` (not a reference target / OD-DB-007 consistency)
3. Permission correction: complete
   1) granted `decision_app` permission on `execution.strategy_position_state`
   2) granted `decision_app` permission on `decision.strategy_daily_position_decision`
   3) corrected the relevant schema sequence usage / select permission
4. ECS RunTask re-run: complete
   1) ECR image pull: success
   2) Secret injection: success
   3) RDS connection: success
   4) CloudWatch Logs output: success
   5) exitCode: 0
5. CloudWatch result summary: complete
   1) `daily_run_id`: 44
   2) `run_date`: 2026-06-13
   3) `data_date`: 2026-06-08
   4) `market_signal`: BLOCK
   5) `evaluator_version`: v1
   6) `positions`: 0
   7) `decision_count`: 0
   8) `sell_count`: 0
   9) `hold_count`: 0
   10) `skip_count`: 0
   11) `decision_ids`: []

### 8. First Validation Completion Criteria

1. Strategy Decision ECS Task single-run success: complete
   1) `daily_buy_signal_run` success
   2) `daily_position_signal_run` success
2. Required DB access success with `decision_app` permission: complete
   1) confirmed permission consistency for the 4 schemas preprocessor / research / execution / decision
   2) maintained the legacy schema USAGE not-granted policy (OD-DB-007 consistency)
3. Execution log confirmable: complete
   1) confirmed CloudWatch Logs output
4. Follow-up automation item separation: complete
   1) EventBridge Scheduler integration is a follow-up
   2) Step Functions orchestration integration is to be reviewed at the strategy execution / batch orchestration stage
   3) `port_strategy_common` formal package / version management is separated into a follow-up Strategy Common or DevOps enhancement stage
   4) Formal organization of the DB permission matrix is separated into a follow-up update of the 06 / 02 spec db-roles-and-grants
5. Judgment: Strategy Decision AWS ECS / Fargate porting core validation complete

### 9. Outside This Date's Scope / Follow-up Handover

1. EventBridge Scheduler → ECS RunTask integration: follow-up
2. Step Functions Standard / Express selection + state machine definition: follow-up (04 spec follow-up phase)
3. Strategy Execution (`port_strategy_execution`) ECS / Fargate porting validation: follow-up (separate spec / 04 spec follow-up phase responsibility)
4. `port_strategy_common` formal package / version management (wheel + CodeArtifact or git submodule packaging): follow-up (07 / Strategy Common stage)
5. Formal organization of the DB permission matrix: follow-up (formally reflect `decision_app`'s research / execution / decision schema permissions into the 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) §4 GRANT / §5 validation SQL)
6. aws-live cutover: follow-up (10 spec responsibility)
7. CI/CD OIDC / GitHub Actions automatic build / push: follow-up (07 spec responsibility)

### 10. Safety / Security Check Results

1. This date's work resulted in 0 changes to the README / AGENTS.md / CHANGELOG / docs / worklog / source of the 8 MS (the `port_strategy_decision` Dockerfile / requirements.txt authored directly by the operator is recorded only as a fact in §2 of this note).
2. 0 plaintext records in this note of actual secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / account number / token / account-id / actual ARN / image digest / IAM access key id / task ARN.
3. Actual `secretsmanager:GetSecretValue` calls are for the operator only. 0 secret value calls during Kiro automatic validation / this note authoring (R-DOCS-001 consistency).
4. AWS / Docker / ECR / ECS / IAM / Secrets Manager / RDS / GRANT work was all performed directly by the operator. Kiro performed only document authoring / procedure organization / validation item organization.
5. 0 broker / KIS / order / fill / Daily Batch entrypoint calls. This date's validation covered only the ECS RunTask single-run of the two entrypoints `daily_buy_signal_run` / `daily_position_signal_run`.


## 2026-06-13 Strategy Execution Responsibility Separation + View Daily Batch 17-step Change

Accumulates the results of the responsibility separation of Strategy Execution (`port_strategy_execution`), handover to the new MarketConnector executor, and the View Daily Batch 17-step restructuring performed directly by the operator on 2026-06-13. On this date, Kiro performed only document authoring / procedure organization / validation item organization, and the actual code / static validation / Maven compile was carried out directly by the operator.

This section is a second work result accumulated separately from the same date's Strategy Decision ECS / Fargate first porting validation (§1 ~ §10).

### 1. Responsibility Separation Decision

1. Decision background: complete
   1) Separates the structure where Strategy Execution's automatic buy / sell entrypoints had been directly calling `connector_buy.buy_stock()` / `connector_sell.sell_stock()` into Strategy Execution = responsible only for order candidate creation / status transition, and MarketConnector = responsible for the actual broker order submission.
   2) Narrows the meaning of Strategy Execution's `--execute` so that it handles only the `READY -> REQUESTED` status transition rather than the actual broker order submission.
   3) The responsibility for actually submitting `REQUESTED`-state strategy orders to the broker / KIS API is handled by MarketConnector's new executor `connector_strategy_order_execute.py` (consistent with the 03 spec first validation result [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-13 section).
2. Post-separation operation policy: complete
   1) Strategy Execution's `--execute` = handles only the `READY -> REQUESTED` transition.
   2) MarketConnector executor `--execute` = handles only the `REQUESTED -> SUBMITTED` or `FAILED` transition.
   3) `connector_order_request_id` is not created / updated by Strategy Execution.
   4) The `mark_position_sell_ordered()` call for the SELL position is transferred to the MarketConnector executor side (executed only at the point of SELL success).

### 2. Strategy Execution (`port_strategy_execution`) Changed / Unchanged Inventory

1. Changed files: complete
   1) `execution_repository.py`
   2) `daily_auto_buy_execute_run.py`
   3) `daily_auto_sell_execute_run.py`
2. Main change summary: complete
   1) newly added repository function `mark_execution_order_requested(conn, execution_order_id, result_payload)`
   2) removed hardcoded MarketConnector path from the BUY / SELL automatic execution entrypoints
   3) removed `sys.path` insertion code
   4) removed `connector_buy` / `connector_sell` import
   5) removed direct `buy_stock()` / `sell_stock()` calls
   6) changed the meaning of `--execute` to `READY -> REQUESTED`
   7) removed the `mark_position_sell_ordered()` call from the SELL path (transferred to the MarketConnector side)
   8) kept `connector_order_request_id` so that it is not created / updated on the Strategy Execution side
3. Static validation: complete
   1) `python -m py_compile execution_repository.py daily_auto_buy_execute_run.py daily_auto_sell_execute_run.py` passed
   2) 0 results in the direct import / call search (`connector_buy` / `connector_sell` / `buy_stock` / `sell_stock`)
   3) confirmed UTF-8 Korean literals display normally
4. This date's validation limitation: on hold
   1) Due to the weekend guard (WEEKEND) blocking, the DB candidate query stage was not confirmed during the dry run.
   2) 0 `--execute` actual calls.
   3) Execution validation of the `READY -> REQUESTED` transition in a weekday or safe test data environment is separated as a follow-up.

### 3. MarketConnector (`port-marketconnector`) New Executor Handover

1. Handover result: complete
   1) The addition of the new executor `connector_strategy_order_execute.py` on the MarketConnector side is accumulated as the 03 spec first validation result (consistent with [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-13 section §1 ~ §6).
   2) No change to the existing validated paper order entrypoints (`connector_buy.py` / `connector_sell.py` / `connector_order_common.py` / `connector_order_check.py` / `connector_balance.py` / `db_config.py` / `config.py` / `token_manager.py`).
2. New executor operation summary: complete
   1) queries `REQUESTED` + `connector_order_request_id IS NULL` strategy orders.
   2) sorts SELL first, BUY second.
   3) the default dry run performs only list output.
   4) applies the `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` guard only when `--execute`.
   5) on `--execute`, calls `connector_buy.buy_stock()` / `connector_sell.sell_stock()` after lazy import.
   6) on success updates `strategy_execution_order` to `SUBMITTED`, on failure to `FAILED`.
   7) on SELL success, calls the position `SELL_ORDERED` update helper.
3. This date's dry run result: complete
   1) output: `[NO_TARGET] REQUESTED strategy order 없음`
   2) since the `REQUESTED` target row count is 0, judged as a normal dry run result.
   3) the actual REQUESTED order row processing output is unvalidated (R-AUTO-009 consistency follow-up addition).

### 4. View Daily Batch 17-step Change (`port-view`)

1. Changed files: complete
   1) `src/main/java/my/portfolio/port_view/service/DailyBatchService.java`
   2) `src/main/java/my/portfolio/port_view/util/DailyBatchLabelUtils.java`
2. DailyBatchService new step addition: complete
   1) location: after `DAILY_AUTO_BUY`, before `CONNECTOR_ORDER_CHECK`.
   2) order: 12
   3) code: `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`
   4) name: `Strategy 주문 실행`
   5) workDir: `properties.getMarketconnectorDir()`
   6) command: `python connector_strategy_order_execute.py --execute`
3. Follow-up step order adjustment: complete
   1) `CONNECTOR_ORDER_CHECK`: 13
   2) `SYNC_SELL_FILL`: 14
   3) `SYNC_BUY_FILL`: 15
   4) `SYNC_BUY_POSITION`: 16
   5) `BALANCE_REFRESH`: 17
4. Daily Batch 17-step order after change: complete
   1) 1. `CONNECTOR_BALANCE`
   2) 2. `INTEREST_CRAWLER`
   3) 3. `PREPROCESSOR`
   4) 4. `BACKTEST_RESEARCH`
   5) 5. `BACKTEST_REPORT`
   6) 6. `DAILY_BUY_SIGNAL`
   7) 7. `DAILY_POSITION_SIGNAL`
   8) 8. `DAILY_BUY_EXECUTION`
   9) 9. `DAILY_SELL_EXECUTION`
   10) 10. `DAILY_AUTO_SELL`
   11) 11. `DAILY_AUTO_BUY`
   12) 12. `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`
   13) 13. `CONNECTOR_ORDER_CHECK`
   14) 14. `SYNC_SELL_FILL`
   15) 15. `SYNC_BUY_FILL`
   16) 16. `SYNC_BUY_POSITION`
   17) 17. `BALANCE_REFRESH`
5. isNoTarget recognition phrase addition: complete
   1) `REQUESTED strategy order 없음`
   2) `strategy execution requested 주문이 없음`
   3) `[NO_TARGET] REQUESTED strategy order 없음`
   4) `no requested`
   5) `no target`
6. DailyBatchLabelUtils change: complete
   1) `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE -> Strategy 주문 실행`
   2) added to both `stepCodeLabel()` and `stepCodeShortLabel()`.
7. Static validation: complete
   1) `.\mvnw.cmd clean compile` success.
   2) 0 actual Daily Batch runs / Python order script runs / DB / AWS access for the new step.

### 5. Local Development / AWS Paper RDS Operation Principles (Local-to-AWS Paper RDS)

1. Paper environment source of truth: complete
   1) The source of truth of the Paper environment is fixed to a single AWS Paper RDS.
   2) Even when running locally, if `PORT_ENVIRONMENT=paper`, it looks at the AWS Paper RDS.
   3) Even when running on AWS, it uses the same AWS Paper RDS.
   4) Local PostgreSQL is used only for `LOCAL_DEV` fixture / experiment / backup reference.
   5) Order / fill / position data is not merged or synchronized between the local DB and the AWS Paper RDS.
2. Environment distinction: complete
   1) `LOCAL_DEV`
       - local PostgreSQL usable
       - development / experiment / fixture only
       - not actual paper operation
       - order execution prohibited
   2) `PAPER`
       - uses AWS Paper RDS
       - local execution also uses AWS Paper RDS
       - AWS execution also uses AWS Paper RDS
       - paper order / fill / position source of truth
   3) `LIVE`
       - follow-up design target (10 spec integration)
       - the live operation source of truth must be separated from paper
3. SSM Port Forwarding direction: complete
   1) RDS is kept Private (02 spec decision consistency / R-SEC-001 consistency).
   2) RDS Public access is prohibited.
   3) When connecting to AWS Paper RDS from local, SSM Port Forwarding is used.
   4) From local, connection is via a port like `localhost:15433`, but the actual target is AWS Paper RDS.
   5) You must not judge it as a local DB unconditionally just because the DB host is `localhost`.
   6) The actual paper order execution guard is judged by the combination `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` (consistent with the MarketConnector executor `--execute` guard).
4. Example environment variables (with Local PC + SSM Port Forwarding): complete
   1) `PORT_ENVIRONMENT=paper`
   2) `PORT_DB_TARGET=aws-paper`
   3) `INTEREST_DB_HOST=localhost`
   4) `INTEREST_DB_PORT=15433`
   5) `INTEREST_DB_NAME=portfolio`
   6) plaintext recording of password / account / actual RDS endpoint prohibited (R-DOCS-001 consistency).
5. Prohibitions: complete
   1) paper order execution on local PostgreSQL prohibited.
   2) `connector_order_request` merge between local DB and AWS Paper RDS prohibited.
   3) `connector_fill` merge between local DB and AWS Paper RDS prohibited.
   4) `strategy_execution_order` merge between local DB and AWS Paper RDS prohibited.
   5) `strategy_position_state` merge between local DB and AWS Paper RDS prohibited.

### 6. Outside This Date's Scope / Follow-up Handover

1. Actual `--execute` end-to-end validation: follow-up (operator confirmation)
   1) Weekday / safe-test-data-based dry / integration validation of Strategy Execution `--execute` (`READY -> REQUESTED`) + MarketConnector executor `--execute` (`REQUESTED -> SUBMITTED`).
   2) Proceed under separate approval after organizing the SSM Port Forwarding / AWS Paper RDS connection policy.
2. SSM Port Forwarding runbook organization: follow-up
   1) Formally document the SSM Port Forwarding → AWS Paper RDS connection procedure in the 02 spec runbook or a separate operation note.
3. `PORT_ENVIRONMENT` / `PORT_DB_TARGET` check of all MS: follow-up
   1) Check the `PORT_ENVIRONMENT` / `PORT_DB_TARGET` consistency in the environment variable inventory of the 8 MS.
4. Strategy Execution AWS porting: follow-up
   1) `port_strategy_execution` ECS / Fargate porting validation (reflecting automatic BUY / SELL E2E OD-SAFE-001 ~ OD-SAFE-004).
5. MarketConnector new executor EC2 deployment: follow-up
   1) EC2 deployment candidate zip or tag artifact for `connector_strategy_order_execute.py`.
   2) Separated into the 03 spec follow-up phase or 07 spec (CI/CD) responsibility.
6. End-to-end validation before operating the new View 17-step: follow-up
   1) After the Java `DailyBatchService` change, validate in a weekday / safe environment whether the 12 ~ 17 segment of the actual Daily Batch sequence runs smoothly in the operational environment (R-AUTO-011 consistency).

### 7. Safety / Security Check Results

1. Within this date's work scope, 0 KIS / broker API calls. 0 `--execute` actual calls. 0 broker / KIS / order / fill / Daily Batch entrypoint calls.
2. 0 RDS DDL/DML. 0 AWS resource creation / change.
3. 0 plaintext records in this note of actual secret value / account number / RDS endpoint hostname / RDS password / KIS app key / KIS app secret / token / account-id / actual ARN / instance-id / image digest / IAM access key id.
4. The 3 changed files on the Strategy Execution side (`execution_repository.py` / `daily_auto_buy_execute_run.py` / `daily_auto_sell_execute_run.py`), the 1 new file on the MarketConnector side (`connector_strategy_order_execute.py`), and the 2 changed files on the View side (`DailyBatchService.java` / `DailyBatchLabelUtils.java`) are recorded only as facts in this note with 0 full-text quotations.
5. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog due to this date's work.


## 2026-06-13 Strategy Execution AWS Porting Pre-validation (`execution_app` AWS Paper RDS Connection)

### Summary

- First validation of the `execution_app`-based AWS Paper RDS connection at the stage before entering the Strategy Execution (`port_strategy_execution`) AWS porting.
- Separate work from the 2 preceding sections of the same date (Strategy Decision ECS / Fargate first §1~§10 + Strategy Execution responsibility separation + View 17-step §1~§7).
- See the [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding section for the SSM Port Forwarding Runbook / procedure.

### 1. Validation Background / Input

1. Responsibility separation result (preceding section §1 ~ §3) input: complete
   1) Strategy Execution `--execute` = handles only the `READY -> REQUESTED` status transition.
   2) MarketConnector new executor `connector_strategy_order_execute.py` `--execute` = handles only the `REQUESTED -> SUBMITTED` / `FAILED` transition.
   3) `connector_order_request_id` is not created / updated on the Strategy Execution side.
2. SSM Port Forwarding standard waypoint decision input: complete
   1) OD-NET-010 = `portfolio-paper-marketconnector-ec2` (instance id `i-0fce77927b7397b88`).
   2) Local PC `localhost:15433` → SSM tunnel → this EC2 → AWS Paper RDS `portfolio-paper-rds:5432`.
   3) RDS `PubliclyAccessible = False` maintained.

### 2. `execution_app` Connection First Validation

1. Connection parameters: complete
   1) host: `localhost`
   2) port: `15433`
   3) dbname: `portfolio`
   4) user: `execution_app`
   5) password: uses environment variable `PGPASSWORD` (0 plaintext exposure, R-DOCS-001 consistency)
2. Python `psycopg2` connection result: complete
   1) `current_user`: `execution_app`
   2) `current_database`: `portfolio`
   3) `search_path`: `execution, decision, research, connector, preprocessor, interest, reference, legacy, public`
3. Consistency judgment: complete
   1) `execution_app`'s search_path is consistent with the 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) §3 / OD-DB-006 / OD-DB-007 (legacy is included but actual access is blocked by not granting USAGE).
   2) First demonstration of AWS Paper RDS connection possibility at the stage before entering Strategy Execution porting.
   3) SELECT query only — 0 INSERT / UPDATE / DELETE / DDL / 0 order / fill entrypoint calls.

### 3. Strategy Execution Porting Follow-up Handover

1. AWS Paper RDS app role connection confirmation: complete
   1) The precondition that Strategy Execution's entrypoints (`daily_auto_buy_execute_run.py` / `daily_auto_sell_execute_run.py` / `execution_repository.py`) can connect to AWS Paper RDS as `execution_app` is satisfied.
2. Outside this date's scope: follow-up
   1) Strategy Execution Dockerfile / requirements.txt new creation: follow-up (consistent with the `port_strategy_decision` Dockerfile pattern — 04 spec 2026-06-13 §2 input).
   2) Local build + container import smoke + ECR push: follow-up.
   3) ECS Task Definition registration (awsvpc / Fargate / cpu-memory / Secrets Manager `/portfolio/paper/rds/execution-app` new creation + Execution Role inline policy addition + Task Role new creation): follow-up.
   4) ECS RunTask single-run validation: follow-up.
   5) Formal organization of `execution_app`'s per-schema GRANT matrix is separated into a follow-up update of the 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) (R-DATA-005 consistency).
3. `READY -> REQUESTED -> SUBMITTED` end-to-end dry / integration validation with weekday or safe test data: follow-up (R-AUTO-009 / R-AUTO-010 consistency).
4. EventBridge Scheduler / Step Functions state machine definition (enforcing the buy-signal → position-signal → strategy-execution order + prohibiting automatic retry, reflecting OD-SAFE-004): 04 spec follow-up phase responsibility.

### 4. Safety / Security Check

1. This date's work resulted in 0 changes to the README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging of the 8 MS. 0 AWS resource creation / modification / deletion.
2. 0 plaintext records in this note of password / secret value / KIS app key / KIS app secret / account number / token / account-id / actual IAM access key id / actual secret ARN.
3. 0 broker / KIS / order / fill entrypoint calls. 0 RDS DDL/DML. 0 `--execute` actual calls.
4. instance id (`i-0fce77927b7397b88`) / private IP (`10.0.20.165`) / local port (`15433`) / RDS endpoint hostname / SSM session id (`terraform-vjp3fv3nz73konetcevdzjh9de`) are recorded as facts as operational identifiers (consistent with the user-specified policy — not secrets).


## 2026-06-13 Strategy Execution AWS Porting Pre-validation — psql 18 + pgAdmin4 Reinforcement

### Summary

- Follow-up to §1~§4 of the preceding section (`## 2026-06-13 Strategy Execution AWS 포팅 사전 검증 (execution_app AWS Paper RDS 접속)`).
- Reinforces the pre-porting client compatibility check before entering the Strategy Execution AWS porting, with local psql 18 + pgAdmin4.
- See the [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 SSM Port Forwarding reinforcement section for detailed results.

### 1. Additional Client Compatibility First Demonstration

1. Local PostgreSQL 18 psql client: complete
   1) direct-path execution — `& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -p 15433 -U portfolio_admin -d portfolio`
   2) confirmed output of client `18.1` / server `18.4` / `SSL TLSv1.3` / `inet_server_addr = 10.0.20.165` / `inet_server_port = 5432`.
   3) A separate `execution_app` psql connection is outside this date's work scope — already passed via the Python `psycopg2` validation (preceding section §2).
2. pgAdmin4: complete
   1) Server registration — Name `AWS Paper RDS - portfolio` / Host `localhost` / Port `15433` / Maintenance database `portfolio` / Username `portfolio_admin` / SSL mode `Prefer`.
   2) confirmed output of `current_user` / `current_database` / `inet_server_addr` / `inet_server_port` — `portfolio_admin` / `portfolio` / `10.0.20.165` / `5432`.
   3) confirmed the core schema / table that Strategy Execution will handle are queryable:
       - `execution.strategy_execution_order` (responsibility separation result `READY -> REQUESTED -> SUBMITTED`/`FAILED` status transition target)
       - `execution.strategy_execution_plan`
       - `execution.strategy_position_state`
       - `execution.connector_signal_order_map`
       - `connector.connector_account` / `connector.connector_order_request` / `connector.connector_order_event` / `connector.connector_fill`
       - `decision.strategy_daily_position_decision` / `decision.strategy_daily_run`
   4) This date's validation performed only up to confirming SELECT possibility — 0 INSERT / UPDATE / DELETE / DDL.

### 2. Strategy Execution Porting Pre-validation Synthesis

1. Python `psycopg2`-based `execution_app` connection (preceding section §2): passed.
2. Local PostgreSQL 18 `psql.exe` direct-path-based `portfolio_admin` connection: passed.
3. pgAdmin4-based `portfolio_admin` connection + Strategy Execution core schema / table query: passed.
4. All 3 clients this date (Python `psycopg2` / psql 18 / pgAdmin4) operate over the same SSM Port Forwarding tunnel (local port `15433`).
5. First multi-client demonstration of pre-connection possibility at the stage before entering the Strategy Execution AWS porting main phase.

### 3. Outside This Date's Scope / Follow-up Handover

1. Strategy Execution Dockerfile / requirements.txt new creation / ECR push / Secrets Manager `/portfolio/paper/rds/execution-app` new / Execution Role inline policy addition / Task Role new / ECS Task Definition registration / RunTask single-run validation: follow-up (same as preceding section §3).
2. Formal organization of `execution_app`'s per-schema GRANT matrix is separated into a follow-up update of the 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) (R-DATA-005 consistency).
3. `READY -> REQUESTED -> SUBMITTED` end-to-end dry / integration validation with weekday or safe test data: follow-up (R-AUTO-009 / R-AUTO-010 consistency).
4. Per-environment (paper / live) server separated-registration policy for pgAdmin4: follow-up (10 spec — at the point of entering the live environment).

### 4. Safety / Security Check

1. This date's work resulted in 0 changes to the README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging of the 8 MS. 0 AWS resource changes.
2. 0 plaintext records of password / secret value / KIS app key / KIS app secret / account number / token / account-id. The passwords of pgAdmin4 / psql 18 are used only in the operator's local PC environment and are prohibited from plaintext recording in this note / console capture / logs (R-DOCS-001 consistency).
3. 0 broker / KIS / order / fill entrypoint calls. 0 RDS DDL/DML. 0 `--execute` actual calls.


## 2026-06-13 Strategy Execution ECS / Fargate First Porting Validation

### Summary

- ECS / Fargate main phase first porting validation of `port_strategy_execution` (follow-up to the 4 preceding sections of the same date).
- Preceding sections = Strategy Decision ECS first / Strategy Execution responsibility separation + View 17-step / AWS Paper RDS pre-validation / psql 18 + pgAdmin4 reinforcement.
- environment `aws-paper` / region `ap-northeast-2`.
- 0 broker / KIS calls / 0 `connector_order_request` creation / 0 actual `READY -> REQUESTED` transitions.
- live automatic order prohibited until follow-up validation / approval (OD-SAFE-002 / OD-SAFE-003 / OD-SAFE-004 consistency).
- Kiro performs only document authoring / procedure organization / the actual Docker / ECR / ECS / IAM / Secrets Manager / RDS / GRANT work is carried out directly by the operator.

### 1. AWS Execution Structure Confirmation / Docker Decision

1. As-Is execution method confirmation: complete
   1) Local repository path confirmation: complete (`C:\Workspaces\port_strategy_execution`)
   2) Responsibility separation input: complete (preceding section §1 consistency)
       - Strategy Execution `--execute` = handles only the `READY -> REQUESTED` status transition
       - MarketConnector executor `connector_strategy_order_execute.py --execute` = `REQUESTED -> SUBMITTED` / `FAILED` transition (separate responsibility / separate EC2 / not included in this Task Definition)
       - the SELL position `mark_position_sell_ordered()` call is on the MarketConnector side
   3) 7 Daily Batch step entrypoint candidate confirmation: complete
       - `daily_buy_execution_run.py`
       - `daily_sell_execution_run.py`
       - `daily_auto_buy_execute_run.py`
       - `daily_auto_sell_execute_run.py`
       - `execution_sync_buy_fill.py`
       - `execution_sync_sell_fill.py`
       - `execution_sync_buy_position.py`
   4) Confirmation of the structure where `execution_config.py` requires the `INTEREST_DB_PASSWORD` environment variable at import time: complete (passed during smoke with a dummy env / in ECS execution resolved by Secrets Manager-based environment injection)
2. Task Definition operation method finalization: complete (OD-MS-017 consistency)
   1) single Task Definition + command override method finalized
   2) A pattern intentionally different from the Strategy Decision (2026-06-13 §6 / OD-MS-013) 2-Task-Definition separation method
   3) Selection reasons:
       - the 7 entrypoints share the same image / role / secret / log group / cpu / memory
       - separating into 7 Task Definitions is excessive from an operational complexity standpoint
       - suitable for per-Step-Functions-state command override mapping
   4) The 7 steps of the View Daily Batch can be logically preserved — in AWS orchestration they are mapped by per-Step-Functions-state command override (follow-up separation)
   5) The MarketConnector executor `connector_strategy_order_execute.py --execute` is not included in this Task Definition

### 2. Dockerfile / requirements.txt New Creation

1. `port_strategy_execution` first Dockerfile / requirements.txt newly created directly by the operator: complete
   1) Docker build context: `C:\Workspaces`
   2) `port_strategy_execution` source vendoring inside the image (consistent with the Strategy Decision Dockerfile pattern)
   3) first dependency: `psycopg2-binary`
   4) the default CMD is a safe `py_compile`-family — the actual operational entrypoint is decided by the command override in the ECS Task Definition `command` or the RunTask `--overrides` command
2. 0 full-text quotations of the Dockerfile / requirements.txt — only the fact that the operator newly authored them directly is recorded in this note (R-DOCS-001 consistency).

### 3. Local Image Build / Smoke

1. Local Docker build: complete
   1) image tag: `portfolio-strategy-execution:paper-20260613`
   2) local latest tag: `portfolio-strategy-execution:paper-latest`
2. container smoke: complete
   1) `python -m py_compile` passed
   2) import smoke passed (passed by injecting a dummy env when importing `execution_config.py`)
   3) confirmed 0 broker / KIS / RDS calls when importing the 7 entrypoints of `port_strategy_execution`
   4) In actual ECS execution, not a dummy env but the `/portfolio/paper/rds/execution-app` JSON multi-key of Secrets Manager is injected as environment

### 4. ECR Repository / Push

1. ECR repository creation / confirmation: complete
   1) repository name: `portfolio-strategy-execution`
   2) Environment separation policy: repository not separated per paper / live environment (08 / 04 spec consistency)
       - paper / live distinction is handled in the 6 items image tag / Task Definition / Secrets·SSM path / IAM Task Role / environment variables / RDS settings
2. ECR push: complete
   1) tag `paper-20260613`
   2) tag `paper-latest`
3. image digest: confirmed directly by the operator. 0 plaintext records of actual digest / actual ECR URI / account-id in this note / spec artifacts (`<image-digest>` placeholder).

### 5. CloudWatch Log Group / Secrets Manager / IAM Role

1. CloudWatch Log Group creation / confirmation: complete
   1) `/portfolio/paper/strategy-execution`
   2) retention: 14 days (08 / 04 spec consistency / OD-OBS-002 consistency)
2. Secrets Manager secret creation / confirmation: complete
   1) `/portfolio/paper/rds/execution-app`
   2) JSON multi-key method (`host` / `port` / `dbname` / `username` / `password`) — consistent with the 08 / 04 spec preprocessor·decision-app secret pattern
   3) actual secret value / endpoint / password / ARN / account-id not recorded in this note (R-DOCS-001 / R-DATA-006 consistency)
3. ECS Task Execution Role reinforcement: complete
   1) name: `portfolio-paper-ecs-task-execution-role` (first created in the 08 / 04 spec, reused this date)
   2) execution-app DB Secret read inline policy added: complete
       - secret ARN scoped / Resource·Action wildcard 0 / 03 §13 / OD-SEC-006 consistency
       - change date / changer / change reason / before·after item summary is a 4-line summary only in this note (full JSON body quoting prohibited)
4. ECS Task Role confirmation: complete
   1) name: `portfolio-paper-execution-task-role`
   2) trust: `ecs-tasks.amazonaws.com`
   3) execution-app runtime-point SDK / runtime permissions were not added this date (RDS connection is handled by the Execution Role's secret injection + RDS credentials)

### 6. ECS Network Confirmation

1. ECS Cluster reuse: complete
   1) cluster: `portfolio-paper-cluster` (first created in the 08 / 04 spec, reused this date)
2. Network structure: complete
   1) 2 public subnets used — `public-a` / `public-b` (OD-NET-004 consistency)
   2) security group: `sgroup-strategy-tasks`
   3) confirmed RDS SG inbound 5432 allows `sgroup-strategy-tasks` source (R-SEC-001 / 02 spec design §4 consistency)
   4) confirmed the VPC Endpoint SG 443 source also allows `sgroup-strategy-tasks` (NAT-free policy consistency / R-NET-003 mitigation consistency)
   5) RunTask `assignPublicIp = ENABLED` method (OD-NET-004 first validation consistency — reuses 08 / 04 spec result)
3. 0 plaintext records in this note of actual subnet id / sg id / VPC Endpoint id — consistent with the identifier notation policy of the 02 spec design.md / operation-notes.md.

### 7. ECS Task Definition Registration

1. Task Definition registration: complete
   1) family: `portfolio-paper-strategy-execution`
   2) revision: 1
   3) status: ACTIVE
   4) networkMode: `awsvpc`
   5) launch type: Fargate
   6) cpu: 512
   7) memory: 1024
   8) container name: `strategy-execution`
   9) default command: safe `py_compile`-family — the operator command-overrides via RunTask `--overrides` to selectively run one of the 7 entrypoints
2. environment: complete
   1) `PORT_ENVIRONMENT=paper` (OD-ENV-007 / OD-MS-016 guard consistency)
   2) `PORT_DB_TARGET=aws-paper` (same)
3. secrets (Secrets Manager `/portfolio/paper/rds/execution-app` JSON multi-key mapping): complete
   1) `INTEREST_DB_HOST`
   2) `INTEREST_DB_PORT`
   3) `INTEREST_DB_NAME`
   4) `INTEREST_DB_USER`
   5) `INTEREST_DB_PASSWORD`
4. log group connection: `/portfolio/paper/strategy-execution`
5. Environment variable compatibility policy: uses the `INTEREST_DB_*` environment variable names as-is for compatibility with existing code (OD-DB-003 consistency / same pattern as 04 spec Strategy Decision)
6. 0 plaintext records in this note of actual image digest / task ARN / account-id / actual secret ARN.

### 8. ECS RunTask command override Validation (8 kinds)

This date's validation was all performed directly by the operator as RunTask single runs. All RunTasks select the entrypoint via `containerOverrides[].command` of `--overrides`. 0 broker / KIS calls / 0 `connector_order_request` creation / 0 actual `READY -> REQUESTED` transitions.

#### 8-1. Default command (`py_compile`) Validation

1. RunTask run: complete
   1) command override: default `py_compile`-family
   2) lastStatus: STOPPED
   3) exitCode: 0
   4) ECR pull: success
   5) Secret injection: success
   6) Task startup: success
   7) CloudWatch log stream creation: success
2. Judgment: container baseline (image / role / secret / log group / cpu / memory / network) first validation passed.

#### 8-2. `execution_sync_buy_fill.py` Validation

1. RunTask run: complete
   1) command override: `python -m execution_sync_buy_fill`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch result summary: complete
   1) `submitted_buy_orders`: 0
   2) `synced_count`: 0
   3) `skipped_count`: 0
3. Judgment: SUBMITTED BUY fill sync terminated normally with a sync target count of 0 (NO_TARGET normal operation).

#### 8-3. `execution_sync_sell_fill.py` Validation

1. RunTask run: complete
   1) command override: `python -m execution_sync_sell_fill`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch result summary: complete
   1) `submitted_position_sell_orders`: 0
   2) `synced_count`: 0
   3) `skipped_count`: 0
3. Judgment: SUBMITTED SELL fill sync terminated normally with a sync target count of 0 (NO_TARGET normal operation).

#### 8-4. `execution_sync_buy_position.py` Validation

1. RunTask run: complete
   1) command override: `python -m execution_sync_buy_position`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch result summary: complete
   1) `filled_buy_orders_without_position`: 0
   2) `synced_count`: 0
   3) `skipped_count`: 0
3. Judgment: FILLED BUY → position creation sync terminated normally with a target count of 0 (NO_TARGET normal operation).

#### 8-5. `daily_buy_execution_run.py` Validation

1. RunTask run: complete
   1) command override: `python -m daily_buy_execution_run`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch result summary: complete
   1) WEEKEND guard block — not a Korean business day
   2) NO_TARGET output
   3) 0 `connector_order_request` creation
3. Judgment: weekend safety guard normally blocked — broker / KIS call possibility 0.

#### 8-6. `daily_sell_execution_run.py` Validation

1. RunTask run: complete
   1) command override: `python -m daily_sell_execution_run`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch result summary: complete
   1) WEEKEND guard block — not a Korean business day
   2) NO_TARGET output
   3) 0 `connector_order_request` creation
3. Judgment: weekend safety guard normally blocked — broker / KIS call possibility 0.

#### 8-7. `daily_auto_sell_execute_run.py --execute` Validation

1. RunTask run: complete
   1) command override: `python -m daily_auto_sell_execute_run --execute`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch result summary: complete
   1) WEEKEND guard block — not a Korean business day
   2) NO_TARGET output
   3) 0 actual `READY -> REQUESTED` transitions (OD-MS-016 consistency — the meaning of `--execute` was narrowed to `READY -> REQUESTED` after responsibility separation)
   4) 0 broker / KIS order calls
   5) 0 `connector_order_request` creation
3. Judgment: the weekend safety guard also normally blocked the `--execute`-included entrypoint — despite the `--execute` actual call occurring, 0 actual orders / status transitions.

#### 8-8. `daily_auto_buy_execute_run.py --execute` Validation

1. RunTask run: complete
   1) command override: `python -m daily_auto_buy_execute_run --execute`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch result summary: complete
   1) WEEKEND guard block — not a Korean business day
   2) NO_TARGET output
   3) 0 actual `READY -> REQUESTED` transitions (OD-MS-016 consistency)
   4) 0 broker / KIS order calls
   5) 0 `connector_order_request` creation
3. Judgment: the weekend safety guard also normally blocked the `--execute`-included entrypoint — despite the `--execute` actual call occurring, 0 actual orders / status transitions.

### 9. First Validation Completion Criteria

1. Strategy Execution ECS Task single-run success: complete (all 8 kinds exitCode 0)
2. single Task Definition + command override operation method validation complete
3. AWS Paper RDS `execution_app` connection validation complete (preceding section pre-validation + this section actual RunTask run)
4. Secrets Manager-based DB environment variable injection validation complete
5. CloudWatch Logs output validation complete
6. weekend safety guard validation complete — the `--execute`-included entrypoint is also safely blocked
7. 0 broker / KIS order calls
8. 0 actual `READY -> REQUESTED` transitions
9. 0 `connector_order_request` creation
10. `READY -> REQUESTED -> SUBMITTED` end-to-end validation based on weekday or safe test data is separated as a follow-up (R-AUTO-009 / R-AUTO-010 / R-AUTO-011 consistency)
11. Judgment: Strategy Execution AWS ECS / Fargate main phase core validation complete. live automatic order is still prohibited per the OD-SAFE-002 / OD-SAFE-003 policy until follow-up validation / approval.

### 10. Outside This Date's Scope / Follow-up Handover

1. Step Functions Standard / Express selection + state machine definition: follow-up (04 spec follow-up phase)
   1) Connect the 2 Strategy Decision Task Definitions (buy-signal / position-signal, OD-MS-013 consistency) + the single Strategy Execution Task Definition + command override (OD-MS-017) within one state machine
   2) The BUY / SELL / fill sync / position change steps prohibit automatic retry (OD-SAFE-004 / R-AUTO-001 consistency)
   3) Only idempotent steps (sync-family NO_TARGET termination) allow automatic retry
2. EventBridge Scheduler → Step Functions / ECS RunTask periodic trigger integration: follow-up
3. MarketConnector executor handover validation: follow-up
   1) end-to-end validation of MarketConnector executor `connector_strategy_order_execute.py --execute` (`REQUESTED -> SUBMITTED`/`FAILED`) after Strategy Execution `--execute` (`READY -> REQUESTED`)
   2) The MarketConnector executor is not included in this Task Definition — it is a separate executor responsibility on the MarketConnector EC2 side (03 spec 2026-06-13 §1 ~ §5 consistency)
4. `READY -> REQUESTED -> SUBMITTED` end-to-end dry / integration validation based on weekday or safe test data: follow-up (R-AUTO-009 / R-AUTO-010 consistency)
5. View Daily Batch ProcessBuilder direct execution → control UI upgrade: follow-up (long-term / 05 spec)
   1) The 17-step (2026-06-13 §4 consistency) implementation change of the View Daily Batch itself is outside this date's work scope
   2) This date's work validated that ECS RunTask command override is possible
6. Formal organization of the per-Step-Functions-state command override mapping table: follow-up
   1) state name ↔ command override allowlist 1:1 table (R-AUTO-014 mitigation consistency)
   2) Specify the policy prohibiting arbitrary command input from View or Step Functions
7. Formal organization of Strategy Execution `execution_app`'s per-schema GRANT matrix: follow-up (02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) follow-up update / R-DATA-005 consistency)
8. aws-live cutover: follow-up (10 spec responsibility)
9. CI/CD OIDC / GitHub Actions automatic build / push: follow-up (07 spec responsibility)

### 11. Safety / Security Check Results

1. This date's work resulted in 0 changes to the README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging of the 8 MS. The `port_strategy_execution` Dockerfile / requirements.txt authored directly by the operator is recorded only as a fact in §2 of this note (0 full-text quotations).
2. 0 plaintext records in this note of actual secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / account number / token / account-id / actual ARN / image digest / IAM access key id / task ARN.
3. 0 plaintext records of `secretsmanager:GetSecretValue` result values. 0 secret value calls during Kiro automatic validation / this note authoring (R-DOCS-001 consistency).
4. AWS / Docker / ECR / ECS / IAM / Secrets Manager / RDS / GRANT work was all performed directly by the operator. Kiro performed only document authoring / procedure organization / validation item organization.
5. 0 plaintext output of secret value in the CloudWatch Logs body — the Task Definition `secrets` field is injected only as environment variables and is masked in stdout output.
6. 0 broker / KIS / order / fill entrypoint calls. This date's validation covered only the ECS RunTask single-run of the 7 entrypoints — all terminated normally as NO_TARGET due to the weekend guard or a sync target count of 0.
7. The 2 `--execute`-included entrypoints (`daily_auto_sell_execute_run.py --execute` / `daily_auto_buy_execute_run.py --execute`) were also blocked by the weekend guard — 0 actual `READY -> REQUESTED` transitions / 0 `connector_order_request` creation.
8. live automatic order is still prohibited until follow-up validation / approval (OD-SAFE-002 / OD-SAFE-003 / OD-SAFE-004 consistency). This date is a paper first validation.
9. Operational identifiers (fact record / not secrets):
   1) The instance id / private IP / SSM session id / RDS endpoint hostname of the preceding section are reused as-is.
   2) image tag = `paper-20260613` / `paper-latest`.
   3) Task Definition family `portfolio-paper-strategy-execution` / revision 1 / container name `strategy-execution`.
   4) security group `sgroup-strategy-tasks`.
   5) Log Group `/portfolio/paper/strategy-execution`.
   6) Secret name `/portfolio/paper/rds/execution-app`.
   7) Task Role `portfolio-paper-execution-task-role` / Execution Role `portfolio-paper-ecs-task-execution-role`.


## 2026-06-16 Strategy Decision safe step ECS dry-run Re-validation

### Summary

- ECS / Fargate single re-run of the 2 Strategy Decision safe steps (`DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL`).
- Follow-up to the 2026-06-13 Strategy Decision ECS / Fargate first porting validation (§1~§10).
- Input state consistency:
  - 08 spec 2026-06-16 Crawler data non-collection resolution + KRX EC2 automation success restored raw freshness.
  - 09 spec 2026-06-16 Strategy Research AWS Batch Backend dry-run re-validation updated the backtest run row (run_id `439d78e7-...` / backtest_end_date `2026-06-15`).
- purpose = first validation of restarting the backend AWS E2E dry-run safe subset (Research → Decision).
- 0 actual BUY / SELL / `--execute` / fill · position sync automatic retry / 0 aws-live work (OD-SAFE-001~004 / OD-MS-021 / OD-MS-013 consistency).
- Kiro performs only document authoring / procedure organization / the actual ECS RunTask / IAM / RDS work is carried out directly by the operator.

### 1. DAILY_BUY_SIGNAL Single ECS / Fargate Re-run

1. RunTask run: complete
   1) Task Definition: `portfolio-paper-strategy-decision-buy-signal:1`
   2) launch type: FARGATE / awsvpc
   3) command override: `python -m port_strategy_decision.daily_buy_signal_run`
   4) image tag: `paper-20260613` (reused as-is from 2026-06-13 §3 / §4)
   5) decision-app DB secret environment variable injection (`/portfolio/paper/rds/decision-app` JSON multi-key 5 kinds / `INTEREST_DB_*` environment variable compatibility policy / OD-DB-003 consistency)
   6) log group: `/portfolio/paper/strategy-decision`
   7) log stream prefix: `buy-signal`
2. Run result: success
   1) lastStatus: `STOPPED`
   2) stopCode: `EssentialContainerExited`
   3) container exitCode: `0`
   4) 0 Batch / automatic retry (OD-SAFE-004 / R-AUTO-001 consistency — Strategy Decision is ECS RunTask single-run / before introducing the Step Functions automatic retry policy)
   5) no actual order submission option — 0 broker / KIS calls / 0 `connector_order_request` creation
3. `decision.strategy_daily_signal` result confirmation: complete
   1) run_date: `2026-06-16`
   2) data_date: `2026-06-15` (2026-06-16 §3 consistency — same date as the backtest_end_date of the 09 spec backtest run)
   3) signal_date: `2026-06-16`
   4) signal_type: `BUY`
   5) signal_status: `READY`
   6) row_count: `4`
   7) rank range: `1 ~ 4`
   8) created_at / updated_at: `2026-06-16 07:28:56 UTC`
4. Safety check: complete
   1) only BUY signal `READY`-state rows created — actual order submission is the responsibility of follow-up steps (`DAILY_AUTO_BUY` / `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) and was not executed this date
   2) 0 `connector_order_request` / `connector_fill` row changes
   3) 0 plaintext records in this note of image digest / task ARN / account-id (`<image-digest>` / `<task-arn>` / `<account-id>` placeholder)

### 2. DAILY_POSITION_SIGNAL Single ECS / Fargate Re-run

1. RunTask run: complete
   1) Task Definition: `portfolio-paper-strategy-decision-position-signal:1`
   2) launch type: FARGATE / awsvpc
   3) command override: `python -m port_strategy_decision.daily_position_signal_run`
   4) image tag: `paper-20260613`
   5) decision-app DB secret environment variable injection
   6) log group: `/portfolio/paper/strategy-decision`
   7) log stream prefix: `position-signal`
2. Run result: success
   1) lastStatus: `STOPPED`
   2) stopCode: `EssentialContainerExited`
   3) container exitCode: `0`
   4) 0 Batch / automatic retry
   5) no actual order submission option — 0 broker / KIS calls
3. position signal run result confirmation: complete
   1) daily_run_id: `45`
   2) run_date: `2026-06-16`
   3) data_date: `2026-06-15`
   4) market_signal: `AGGRESSIVE`
   5) positions: `0`
   6) decision_count: `0`
   7) sell_count: `0`
   8) hold_count: `0`
   9) skip_count: `0`
   10) decision_ids: `[]`
4. `decision.strategy_daily_position_decision` state: normal skip
   1) max_decision_date: `2026-05-29` maintained (no new row created)
   2) cause: current held positions 0 — there is no position decision target itself (a BUY signal `READY`-state row has never been converted into an actual buy / 0 `connector_order_request` / 0 `strategy_position_state` OPEN)
   3) Judgment: a normal skip, not an error — a normal operation case of Strategy Decision position-signal, and this date's result is not a violation of R-AUTO-001 / R-AUTO-002
5. Safety check: complete
   1) 0 `connector_order_request` / `connector_fill` / `strategy_position_state` row changes
   2) 0 SELL position `mark_position_sell_ordered()` calls (OD-MS-016 consistency — MarketConnector executor-side responsibility)
   3) 0 intraday stop SELL creation

### 3. Backend AWS E2E dry-run safe subset Progress State

1. safe subset completed items (as of 2026-06-16, OD-MS-021 consistency — 17-step order as-is):
   1) #1 `CONNECTOR_BALANCE`: complete (2026-06-15 §2 / 03 spec / `connector_balance_snapshot` stored)
   2) #2 `INTEREST_CRAWLER`: complete (2026-06-16 / 08 spec — non-GUI rev7 + KRX EC2 worker hybrid structure complete / raw freshness restored)
   3) #3 `PREPROCESSOR`: run complete (2026-06-15 §4 / 08 spec — first up to reaching a re-runnable state after raw input data restoration / re-run based on new raw input is follow-up task 77)
   4) #4 `BACKTEST_RESEARCH`: complete (2026-06-16 / 09 spec §1 — `portfolio-paper-strategy-research:5` / run_id `439d78e7-...`)
   5) #5 `BACKTEST_REPORT`: complete (2026-06-16 / 09 spec §2 / §3 / §4 — `portfolio-paper-strategy-report:3` / 4 S3 objects)
   6) #6 `DAILY_BUY_SIGNAL`: complete (this section §1 — row_count `4` / `READY`)
   7) #7 `DAILY_POSITION_SIGNAL`: complete (this section §2 — positions 0 normal skip)
2. safe subset on-hold / follow-up items (order / fill / sync / execution family):
   1) #8 `DAILY_BUY_EXECUTION` ~ #11 `DAILY_AUTO_BUY`: not proceeded / dry-run skip planned — `--execute` order submission family execution prohibited (R-AUTO-009 / R-AUTO-010 / OD-SAFE-002 consistency)
   2) #12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`: not proceeded / dry-run skip planned — paper operational environment activation on hold (R-AUTO-011 consistency)
   3) #13 `CONNECTOR_ORDER_CHECK` ~ #16 `SYNC_BUY_POSITION`: not proceeded / dry-run skip planned — fill · position sync automatic retry prohibited (OD-SAFE-004 consistency)
   4) #17 `BALANCE_REFRESH`: not proceeded
3. Creation of a View AWS execution mapping table / safe step priority connection / separate judgment of whether Execution family dry-run is possible: follow-up separation (05 spec / 04 spec follow-up phase responsibility).

### 4. First Validation Completion Criteria

1. DAILY_BUY_SIGNAL ECS / Fargate single run: complete (§1 — exitCode 0 / 4 `decision.strategy_daily_signal` rows created / signal_date `2026-06-16`)
2. DAILY_POSITION_SIGNAL ECS / Fargate single run: complete (§2 — exitCode 0 / positions 0 normal skip / `decision.strategy_daily_position_decision` no new row created)
3. Stale data risk resolved with data_date `2026-06-15`-based input (R-DATA-009 / R-DATA-010 mitigation first demonstration)
4. 0 actual order submission / `--execute` / fill · position sync automatic retry / SELL position change — OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 ~ R-AUTO-011 consistency
5. 0 aws-live work — this date is `aws-paper` only
6. Judgment: Strategy Decision safe step ECS dry-run re-validation complete. The follow-up proceeds under separate approval per the safety criteria of the order / fill / sync family + in a weekday or safe test data environment.

### 5. Follow-up Handover

1. View Daily Batch `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL` step ProcessBuilder → ECS RunTask call mapping — 05 spec follow-up phase responsibility
2. Step Functions state machine definition (enforcing the buy-signal → position-signal order + reflecting the automatic retry prohibition policy OD-SAFE-004) + EventBridge Scheduler periodic trigger — 04 spec follow-up phase responsibility
3. paper operational environment activation of the order / fill / sync family (`DAILY_BUY_EXECUTION` ~ `BALANCE_REFRESH`): proceed after separate operator approval + validation in a weekday / safe test data environment (R-AUTO-009 / R-AUTO-010 / R-AUTO-011 / OD-SAFE-002 / OD-SAFE-003 consistency)
4. Separate judgment of whether Execution family dry-run is possible (safety guard check before entering `READY -> REQUESTED -> SUBMITTED` end-to-end validation) — 04 spec follow-up phase
5. aws-live cutover — 10 spec responsibility

### 6. Task Completion Processing (this spec)

Since this spec has no separate tasks.md, task-unit completion processing is recorded directly in this section.

1. DAILY_BUY_SIGNAL single ECS / Fargate run validation: complete (§1 consistency / passed re-run this date as a follow-up to 2026-06-13 §7)
2. DAILY_POSITION_SIGNAL single ECS / Fargate run validation: complete (§2 consistency / first demonstration of a normal operation case with positions 0 normal skip)
3. Strategy Decision safe step Backend dry-run re-validation: complete (§3 / safe subset #1 ~ #7 all complete)
4. order / fill / sync / execution stage: not run or follow-up on hold (§3 / §5)
5. View Daily Batch `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL` mapping: follow-up (§5 / 05 spec)
6. Step Functions + EventBridge Scheduler periodic trigger: follow-up (§5 / 04 spec follow-up phase)

### 7. Safety / Security Check Results

1. This date's work resulted in 0 changes to the README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging of the 8 MS. 0 files modified directly by the operator (this date's work performed only ECS RunTask single-run validation + DB result query).
2. 0 plaintext records in this note of actual secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / account number / token / account-id / actual secret ARN / actual IAM Role ARN / image digest full sha256 / IAM access key id / task ARN. All `[REDACTED]` or placeholder.
3. ECS / IAM / Secrets Manager / RDS work was all performed directly by the operator. Kiro performed only document authoring / procedure organization / validation item organization. 0 plaintext records of `secretsmanager:GetSecretValue` result values.
4. 0 plaintext quotations in this note of CloudWatch Logs body / ECS Task event / SSM response body. Only facts (Task Definition family·revision / image tag / lastStatus / exitCode / row count / data_date / signal_date / signal_status) recorded.
5. 0 direct broker / KIS / order / fill / Daily Batch entrypoint calls. 0 new BUY / SELL / cancel / modify / `--execute`. 0 fill / position sync automatic retry. 0 SELL position `mark_position_sell_ordered()` calls.
6. 0 RDS DDL. DML is limited to `decision.strategy_daily_run` / `decision.strategy_daily_signal` insert (4 BUY signal `READY` rows). 0 new `decision.strategy_daily_position_decision` rows (positions 0 normal skip).
7. live automatic BUY / SELL E2E validation is still prohibited until follow-up validation / approval per the OD-SAFE-002 / OD-SAFE-003 policy. This date is the paper environment only.


## 2026-06-17 Daily AWS 17-step E2E Complete (Strategy Decision · Strategy Execution)

### Summary

- The Daily AWS 17-step E2E flow was connected end-to-end this date (follow-up to the MarketConnector query-oriented dry-run re-validation in the first session of the same date).
- This spec's responsible steps 9 kinds = Step 6 / 7 / 8 / 9 / 10 / 11 / 14 / 15 / 16.
- 03 spec's responsible steps 4 kinds = 1 / 12 / 13 / 17 (operation-notes 2026-06-17 §1~§5 consistency).
- 08 spec (2 / 3) / 09 spec (4 / 5) operation-notes 2026-06-17 consistency.
- environment `aws-paper` only / 0 aws-live work.
- 0 broker / KIS calls within this spec scope (order submission responsibility is 03 spec Step 12).
- Strategy Execution `--execute` handles only the `READY -> REQUESTED` status transition (OD-MS-016 consistency).
- Kiro performs only document authoring / procedure organization / the actual ECS RunTask / IAM / RDS / GRANT work is carried out directly by the operator.

### 1. Step 6 `DAILY_BUY_SIGNAL`

1. RunTask run: complete
   1) Task Definition: `portfolio-paper-strategy-decision-buy-signal:1`
   2) launch type: FARGATE / awsvpc / image tag `paper-20260613`
   3) command override: `python -m port_strategy_decision.daily_buy_signal_run`
2. Run result: success
   1) lastStatus: `STOPPED` / stopCode: `EssentialContainerExited` / exitCode: `0`
   2) `decision.strategy_daily_run` new row created / run_date `2026-06-17` / data_date `2026-06-16`
3. `decision.strategy_daily_signal` BUY READY result: confirmed
   1) row_count: 4
   2) signal_type: BUY / signal_status: READY
   3) 4 candidates:
      - `282330` BGF리테일
      - `004990` 롯데지주
      - `003490` 대한항공
      - `088350` 한화생명
   4) rank range: `1 ~ 4`
4. Safety check: complete
   1) only BUY signal `READY`-state rows created — actual order submission is the responsibility of follow-up steps (`DAILY_AUTO_BUY` / `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`)
   2) 0 `connector_order_request` / `connector_fill` row changes / 0 broker · KIS calls

### 2. Step 7 `DAILY_POSITION_SIGNAL`

1. RunTask run: complete
   1) Task Definition: `portfolio-paper-strategy-decision-position-signal:1`
   2) command override: `python -m port_strategy_decision.daily_position_signal_run`
2. Run result: success
   1) lastStatus: `STOPPED` / exitCode: `0`
3. position signal result: normal skip
   1) positions: 0
   2) decision_count: 0
   3) `decision.strategy_daily_position_decision` no new row created — normal skip (held positions 0 / at the point of `strategy_position_state` OPEN 0 / at the point of `connector_order_request` 0)
4. Judgment: a normal operation case (0 R-AUTO-001 / R-AUTO-002 violations / 2026-06-16 §2 / §4 consistency)

### 3. Step 8 `DAILY_BUY_EXECUTION`

1. First blocker / operator action: complete
   1) first failure cause = missing `interest` schema USAGE / table SELECT / sequence / default privileges of `execution_app` (R-DATA-005 [2026-06-17 reinforcement] consistency)
   2) the operator directly corrected the GRANT — `interest` schema USAGE + `interest.*` table SELECT + sequence + default privileges (auto-applied to future objects) — consistent with the 02 spec / 06 spec operation-notes 2026-06-17 fact record
   3) formal matrix update of 02 spec db-roles-and-grants is a follow-up phase
2. Re-run result: complete
   1) RunTask exitCode 0 / lastStatus STOPPED
   2) `execution.strategy_execution_plan` new row created — `execution_plan_id 92`
   3) 4 BUY READY created (execution_order)
   4) all 4 `connector_order_request_id` are NULL — Strategy Execution handles only up to `READY -> REQUESTED` / `connector_order_request_id` mapping is MarketConnector executor `--execute` responsibility (OD-MS-016 consistency)
   5) 0 actual broker / KIS order calls

### 4. Step 9 `DAILY_SELL_EXECUTION`

1. RunTask run: complete
2. Run result: normal skip
   1) sell_decisions: 0
   2) orders_to_upsert: 0
   3) 0 actual broker / KIS order calls

### 5. Step 10 `DAILY_AUTO_SELL`

1. RunTask run: complete
2. Run result: normal skip
   1) READY SELL orders: 0
   2) 0 actual broker / KIS order calls
   3) 0 SELL position `mark_position_sell_ordered()` calls (OD-MS-016 consistency — MarketConnector-side responsibility)

### 6. Step 11 `DAILY_AUTO_BUY`

1. RunTask run: complete
   1) command override: `python -m daily_auto_buy_execute_run --execute`
   2) `--execute` guard (business day / environment) passed
2. Run result: success
   1) 4 BUY execution_orders `READY -> REQUESTED` transition complete
   2) `execution_plan_id 92`
   3) 4 BUY REQUESTED
   4) total_qty: `378`
   5) total_target_amount: `6908189.40`
   6) all 4 `connector_order_request_id` are NULL — the `REQUESTED -> SUBMITTED` transition and `connector_order_request_id` mapping are MarketConnector executor responsibility (OD-MS-016 consistency / 03 spec Step 12 responsibility boundary)
   7) 0 actual broker / KIS order calls — Strategy Execution `--execute` is status transition only
3. Responsibility boundary first demonstration: complete
   1) Strategy Execution = handles only the `READY -> REQUESTED` status transition
   2) MarketConnector = actual KIS paper order submission + `REQUESTED -> SUBMITTED` transition (03 spec Step 12)
   3) First demonstration of the flow from #11 (`DAILY_AUTO_BUY`) → #12 (`MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) of the View Daily Batch 17-step (R-AUTO-009 / R-AUTO-010 / R-AUTO-011 [2026-06-17 reinforcement] consistency)

### 7. Step 14 `SYNC_SELL_FILL`

1. RunTask run: complete
   1) command override: `python -m execution_sync_sell_fill`
2. Run result: normal skip
   1) SELL fill 0 — no SELL order occurred this date
   2) 0 `execution.strategy_execution_order` SELL row changes

### 8. Step 15 `SYNC_BUY_FILL`

1. RunTask run: complete
   1) command override: `python -m execution_sync_buy_fill`
2. Run result: success
   1) BUY fill sync complete based on `connector.connector_fill 26 ~ 29` (consistent with the 03 spec Step 13 result)
   2) `execution.strategy_execution_order 26 ~ 29` SUBMITTED → FILLED transition complete
   3) sync_result reflection (`filled_qty` / `filled_avg_price` etc.) — mapping of the broker response value of `connector_fill`

### 9. Step 16 `SYNC_BUY_POSITION`

1. RunTask run: complete
   1) command override: `python -m execution_sync_buy_position`
2. Run result: success
   1) `execution.strategy_position_state` 4 OPEN newly created
   2) position_state_id mapping:
      - `282330` BGF리테일: `position_state_id 6`
      - `004990` 롯데지주: `position_state_id 7`
      - `003490` 대한항공: `position_state_id 8`
      - `088350` 한화생명: `position_state_id 9`
   3) OPEN rows created based on BUY fill — Strategy Execution position status first OPEN transition complete

### 10. Backend AWS E2E 17-step This Date's Progress State

1. All 17 steps complete or normal skip (OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004 consistency):
   1) #1 `CONNECTOR_BALANCE`: complete (03 spec §1)
   2) #2 `INTEREST_CRAWLER`: complete (08 spec §1)
   3) #3 `PREPROCESSOR`: complete (08 spec §2)
   4) #4 `BACKTEST_RESEARCH`: complete (09 spec §1)
   5) #5 `BACKTEST_REPORT`: complete (09 spec §2)
   6) #6 `DAILY_BUY_SIGNAL`: complete (this section §1)
   7) #7 `DAILY_POSITION_SIGNAL`: complete normal skip (this section §2)
   8) #8 `DAILY_BUY_EXECUTION`: complete (this section §3)
   9) #9 `DAILY_SELL_EXECUTION`: complete normal skip (this section §4)
   10) #10 `DAILY_AUTO_SELL`: complete normal skip (this section §5)
   11) #11 `DAILY_AUTO_BUY`: complete (this section §6)
   12) #12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`: complete (03 spec §2)
   13) #13 `CONNECTOR_ORDER_CHECK`: complete (03 spec §3)
   14) #14 `SYNC_SELL_FILL`: complete normal skip (this section §7)
   15) #15 `SYNC_BUY_FILL`: complete (this section §8)
   16) #16 `SYNC_BUY_POSITION`: complete (this section §9)
   17) #17 `BALANCE_REFRESH`: complete (03 spec §4)
2. ECS RunTask + command override pattern first validation complete (OD-MS-013 / OD-MS-017 consistency) — mitigation first demonstration via this date's validation.
3. Due to the PowerShell AWS CLI `--overrides` inline JSON quoting problem, use of the UTF-8 no BOM JSON file + `--overrides file://...` pattern is required — re-demonstrated this date too (R-AUTO-014 mitigation consistency / for details see 2026-06-13 §3.1 / §4.5 consistency).
4. Strategy Execution = responsible for execution candidate / status transition, not actual broker order submission / actual KIS order submission is 03 spec Step 12 responsibility — responsibility boundary clarified this date via 17-step E2E first demonstration (OD-MS-016 / R-AUTO-009 / R-AUTO-010 / R-AUTO-011 [2026-06-17 reinforcement] consistency).

### 11. Follow-up Handover

1. View Daily Batch 9-step ProcessBuilder → ECS RunTask call mapping — 05 spec follow-up phase responsibility
2. Step Functions state machine definition (enforcing the buy-signal → position-signal order + reflecting the automatic retry prohibition policy OD-SAFE-004 for the sell-execution / auto-buy / sync family) + EventBridge Scheduler periodic trigger — 04 spec follow-up phase responsibility
3. `execution_app` interest permission formal matrix update — 02 spec db-roles-and-grants follow-up phase
4. Maintain the 0-SubmitJob policy for the heavy classification (`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) (R-AUTO-015 consistency) — 09 spec follow-up phase
5. AWS paper automation orchestrator candidate organization (View- or Step Functions-based) — 04 / 05 spec follow-up phase
6. aws-live cutover — 10 spec responsibility

### 12. Safety / Security Check Results

1. 0 document / source changes (spec area) — the `execution_app` interest permission change directly corrected by the operator via GRANT is recorded as a fact in the 02 · 06 spec operation-notes (0 full-text quotations).
2. 0 plaintext records of sensitive information — secret value / RDS password · endpoint / KIS app key · secret / account number / token / account-id / actual secret · IAM Role ARN / image digest / IAM access key id / task ARN all `[REDACTED]` or placeholder.
3. AWS work and log quotation:
   1) ECS / IAM / Secrets Manager / RDS / GRANT = all performed directly by the operator.
   2) Kiro = performs only document authoring / procedure organization / 0 plaintext records of `secretsmanager:GetSecretValue` result values.
   3) 0 quotations = CloudWatch Logs body / ECS Task event / SSM response body.
   4) Operational identifiers (fact record): `execution_plan_id 92` / `execution_order id 26~29` / `connector_order_request id 34~37` / `position_state_id 6~9` / ticker · company name · quantity / `total_qty 378` / `total_target_amount 6908189.40` / data_date `2026-06-16` / signal_date · run_date `2026-06-17`.
4. Order / fill safety:
   1) 0 direct broker / KIS / order / fill / Daily Batch entrypoint calls in this spec (order submission is 03 spec Step 12 responsibility).
   2) Strategy Execution `--execute` = limited to #11 (`DAILY_AUTO_BUY`) / `READY -> REQUESTED` status transition only / 0 broker · KIS calls.
   3) 0 SELL position `mark_position_sell_ordered()` calls.
   4) 0 fill / position sync automatic retry.
5. RDS change scope (0 DDL / DML limited to this spec scope):
   1) `decision.strategy_daily_run` / `decision.strategy_daily_signal` insert.
   2) `execution.strategy_execution_plan` insert (`id 92`).
   3) `execution.strategy_execution_order` insert (4 BUY READY) + update (4 REQUESTED → SUBMITTED → FILLED).
   4) `execution.strategy_position_state` insert (4 OPEN / `id 6~9`).
   5) 0 new `decision.strategy_daily_position_decision` rows (positions 0 normal skip).
6. aws-live / mitigation:
   1) live automatic BUY / SELL E2E validation prohibited until OD-SAFE-002 / OD-SAFE-003 follow-up approval / this date is `aws-paper` only / 0 aws-live work.
   2) With the paper 17-step E2E first pass, R-AUTO-009 / R-AUTO-010 / R-AUTO-011 mitigation first demonstrated / Status `Mitigated` update consistency.


## 2026-06-18 Daily AWS Paper Wrapper 17-step Live Operation Validation (Strategy Decision · Strategy Execution)

### Summary

- Actual execution of Daily AWS Paper Wrapper (`.kiro/scripts/run-daily-aws-paper.ps1`) Step 1~17.
- This spec's responsible steps 9 kinds = Step 6 / 7 / 8 / 9 / 10 / 11 / 14 / 15 / 16.
- 03 spec (1 / 12 / 13 / 17) / 08 spec (2 / 3) / 09 spec (4 / 5) operation-notes 2026-06-18 consistency.
- environment `aws-paper` / RunDate `2026-06-18` / 0 aws-live work.
- 0 direct broker / KIS calls within 04 spec scope (KIS paper BUY is 03 spec Step 12 responsibility).
- notable point = at Step 16 SYNC_BUY_POSITION an additional-buy unique constraint conflict → operator direct merge patch + Docker rebuild + ECR push + ECS re-run (§5 detailed consistency).
- Kiro performs only document authoring / procedure organization / the actual wrapper execution / ECS RunTask / patch / Docker rebuild / ECR push / ECS re-run are all carried out directly by the operator.

### 1. Step 6 `DAILY_BUY_SIGNAL` / Step 7 `DAILY_POSITION_SIGNAL` / Step 8 `DAILY_BUY_EXECUTION`

1. Step 6: complete
   1) wrapper Step 6 call → ECS RunTask `portfolio-paper-strategy-decision-buy-signal:1` (image `paper-20260613` / OD-MS-013 consistency)
   2) exitCode 0 / lastStatus STOPPED
   3) `decision.strategy_daily_signal` BUY READY 4 / signal_date `2026-06-18` / data_date `2026-06-17`
   4) 4 candidates — `004990` 롯데지주 / `023530` 롯데쇼핑 / `003490` 대한항공 / `042660` 한화오션
2. Step 7: complete
   1) wrapper Step 7 call → ECS RunTask `portfolio-paper-strategy-decision-position-signal:1` (image `paper-20260613`)
   2) exitCode 0 / lastStatus STOPPED
   3) HOLD decision normally created based on held positions (4 stocks OPEN from the 2026-06-17 17-step E2E result: `282330` / `004990` / `003490` / `088350`)
   4) `decision.strategy_daily_position_decision` new row creation consistency
3. Step 8: complete
   1) wrapper Step 8 call → ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python daily_buy_execution_run.py` (OD-MS-017 consistency)
   2) exitCode 0 / lastStatus STOPPED
   3) `execution.strategy_execution_plan` new row created / `execution_plan_id 94`
   4) 4 BUY READY orders created / blocked_order_count 0 / skipped_order_count 0
   5) per the additional-buy allow policy, existing held stocks (`004990` / `003490`) are also created as BUY candidate orders — OD-MS-024 new consistency
   6) order quantities:
       - `004990` 롯데지주 4 shares
       - `023530` 롯데쇼핑 8 shares
       - `003490` 대한항공 6 shares
       - `042660` 한화오션 11 shares
4. Safety check: complete
   1) all 4 `connector_order_request_id` are NULL — Strategy Execution handles only up to `READY -> REQUESTED` / `connector_order_request_id` mapping is MarketConnector executor responsibility (OD-MS-016 consistency)
   2) 0 direct broker / KIS calls

### 2. Step 9 `DAILY_SELL_EXECUTION` / Step 10 `DAILY_AUTO_SELL`

1. Step 9: complete normal skip
   1) wrapper Step 9 call → ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python daily_sell_execution_run.py`
   2) exitCode 0 / lastStatus STOPPED
   3) no SELL target / sell_decisions 0 / orders_to_upsert 0
   4) 0 direct broker / KIS calls
2. Step 10: complete normal skip
   1) wrapper Step 10 call → ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python daily_auto_sell_execute_run.py --execute`
   2) exitCode 0 / lastStatus STOPPED
   3) 0 READY SELL orders / `--execute` means only strategy execution internal state creation·update (OD-MS-016 responsibility separation consistency)
   4) 0 direct broker / KIS calls

### 3. Step 11 `DAILY_AUTO_BUY` — 4 `READY -> REQUESTED`

1. wrapper Step 11 call: complete
   1) ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python daily_auto_buy_execute_run.py --execute`
   2) `--execute` means only strategy execution internal state creation·update / not a direct broker · KIS submission (OD-MS-016 responsibility separation consistency)
2. Run result: complete
   1) exitCode 0 / lastStatus STOPPED
   2) 4 BUY `READY -> REQUESTED` transition — `strategy_execution_order id 30 ~ 33` REQUESTED
   3) `execution_plan_id 94`
   4) all 4 `connector_order_request_id` are NULL — Step 12 MarketConnector executor handles the `REQUESTED -> SUBMITTED` transition + `connector_order_request_id` mapping responsibility (OD-MS-016 consistency / 03 spec 2026-06-18 §2 consistency)
   5) 0 direct broker / KIS calls

### 4. Step 14 `SYNC_SELL_FILL` / Step 15 `SYNC_BUY_FILL`

1. Step 14: complete normal skip
   1) wrapper Step 14 call → ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python execution_sync_sell_fill.py`
   2) exitCode 0 / lastStatus STOPPED
   3) SELL fill 0 / no SELL order occurred consistency
2. Step 15: complete
   1) wrapper Step 15 call → ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_fill.py`
   2) exitCode 0 / lastStatus STOPPED
   3) BUY fill sync based on `connector.connector_fill` — strategy_execution_order id `30 ~ 33` REQUESTED → FILLED transition (consistent with the single-fill synchronization result of 03 spec 2026-06-18 §3)
   4) `result_payload.sync_result` created / all 4 FILLED consistency

### 5. Step 16 `SYNC_BUY_POSITION` — Additional-buy unique constraint Conflict + merge Patch

This step is the largest operational exception in this date's actual wrapper execution. Per the additional-buy allow policy consistency, attempting a new INSERT for stocks that already have an OPEN position (`004990` / `003490`) causes a `strategy_position_state` `unique(account_id, ticker_code, status='OPEN')` conflict. The result of the operator resolving it directly via patch is accumulated with OD-MS-024 new / R-DATA-012 new mitigation consistency.

1. First attempt: failed (R-DATA-012 new consistency)
   1) wrapper Step 16 call → ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_position.py`
   2) ECS task failed — `duplicate key value violates unique constraint` (`strategy_position_state` unique constraint on `account_id` / `ticker_code` / OPEN status)
   3) affected stocks — `004990` 롯데지주 (existing `position_state_id 7` OPEN) / `003490` 대한항공 (existing `position_state_id 8` OPEN) — 2 additional-buy cases
   4) 2 new OPEN (`023530` / `042660`) had 0 unique violations due to absence of an existing OPEN row
2. patch content: complete
   1) `port_strategy_execution/execution_sync_buy_position.py` patched directly by the operator
   2) added logic to query the existing position based on the same `account_id` / `ticker_code` / OPEN status
   3) when an existing OPEN position exists, calls `merge_open_position_state()` instead of a new INSERT — quantity simple summation / entry_price weighted average / status `OPEN` maintained
   4) accumulates the additional-buy history in `buy_info.additional_buys` (`execution_order_id` / `connector_order_request_id` / buy date / quantity / unit price)
   5) idempotency check — if the same `execution_order_id` or `connector_order_request_id` already exists in `additional_buys`, does not accumulate again
   6) proceeds with a new INSERT only when there is no existing OPEN position
   7) the patch change is recorded only as a fact in this note (0 full-text quotations / R-DOCS-001 consistency)
3. Docker build and ECR push: complete
   1) `portfolio-strategy-execution:paper-20260613` rebuilt
   2) Docker build context caution — since the Dockerfile COPYs the `port_strategy_execution/...` path, the build proceeds based on `C:\Workspaces`
   3) ECR push complete (image digest kept by the operator / plaintext recording in this spec artifact prohibited / R-DOCS-001 consistency)
   4) On PowerShell pipe-based `docker login`, a 400 response occurred → resolved by the `cmd /c` pipe bypass method (operator local PC environment operation memo)
4. Re-run result: success
   1) wrapper Step 16 re-entry → ECS task exitCode 0 / Step 16 SUCCESS
   2) `position_sync_result` created / all 4 processed consistently
5. Position reflection result: confirmed (OD-MS-024 / R-DATA-012 mitigation first demonstration)
   1) `004990` 롯데지주 — existing `position_state_id 7` maintained / 65 shares → 69 shares / entry_price `27008.6956` (weighted average updated) / `execution_order_id 30` recorded in `additional_buys`
   2) `003490` 대한항공 — existing `position_state_id 8` maintained / 52 shares → 58 shares / entry_price `28979.3103` / `execution_order_id 32` recorded in `additional_buys`
   3) `023530` 롯데쇼핑 — new `position_state_id 11` created / 8 shares / entry_price `194225.0000`
   4) `042660` 한화오션 — new `position_state_id 12` created / 11 shares / entry_price `126118.1818`
   5) 2 additional-buy merges + 2 new OPEN all processed consistently
6. Follow-up handover: follow-up
   1) formal patch commit + 07 spec CI/CD integration — followups-overview 2026-06-18 §3
   2) idempotency regression check automation — periodic SQL check of whether the same `execution_order_id` is not accumulated twice
   3) validation of `additional_buys` handling during the SELL closing flow / partial liquidation / full liquidation — followups-overview 2026-06-18 §10

### 6. Safety / Security Check Results (Strategy Decision · Strategy Execution)

1. 0 document / source changes (spec area) — the `port_strategy_execution/execution_sync_buy_position.py` change the operator directly patched / rebuilt Docker / ECR pushed is only fact-recorded in §5 (0 full-text quotations / R-DOCS-001 consistency).
2. 0 plaintext records of sensitive information — secret value / RDS password · endpoint / KIS app key · secret / account number · account password / token / account-id / actual secret ARN · IAM Role ARN / image digest / IAM access key id / task ARN all `[REDACTED]` or placeholder.
3. AWS work and log quotation:
   1) ECS / IAM / Secrets Manager / RDS / Docker / ECR / GRANT = all performed directly by the operator.
   2) Kiro = performs only document authoring / procedure organization / 0 plaintext records of `secretsmanager:GetSecretValue` result values.
   3) 0 quotations = CloudWatch Logs body / ECS Task event / SSM response body / Docker build · push logs.
   4) Operational identifiers (fact record): `execution_plan_id 94` / `strategy_execution_order id 30~33` / `connector_order_request_id 4 kinds` (03 spec Step 12 responsibility) / `position_state_id 7 · 8 · 11 · 12` / ticker · company name · quantity · entry_price / data_date `2026-06-17` / signal_date · run_date `2026-06-18`.
4. Order / fill safety:
   1) 0 direct broker / KIS / order / fill / Daily Batch entrypoint calls within this spec scope (order submission is 03 spec Step 12 responsibility).
   2) Strategy Execution `--execute` = limited to Step 10 / 11 / 14 / 15 / 16 / all strategy execution internal state creation·update / not a direct broker · KIS submission (OD-MS-016 consistency).
   3) 0 SELL position `mark_position_sell_ordered()` calls.
   4) 0 fill / position sync automatic retry (operator direct patch then re-run / wrapper automatic retry not used).
5. RDS change scope (0 DDL / DML limited to this spec scope):
   1) `decision.strategy_daily_run` / `decision.strategy_daily_signal` insert.
   2) `decision.strategy_daily_position_decision` insert.
   3) `execution.strategy_execution_plan` insert (`id 94`).
   4) `execution.strategy_execution_order` insert · update (4 BUY READY → REQUESTED → SUBMITTED → FILLED).
   5) `execution.strategy_position_state` insert · merge (2 additional-buy merges + 2 new INSERT).
   6) 0 SELL row changes / 0 `mark_position_sell_ordered()` calls.
6. aws-live / mitigation:
   1) live automatic BUY / SELL E2E validation prohibited until OD-SAFE-002 / OD-SAFE-003 follow-up approval / this date is `aws-paper` only / 0 aws-live work.
   2) With the paper 17-step wrapper actual execution + additional-buy merge E2E first pass, OD-MS-024 / R-DATA-012 mitigation first demonstrated / Status `Mitigated` update consistency.


## 2026-06-22 Daily AWS Paper execution steps 6~17 Operation Validation

### Summary

- Second actual operation execution of Daily AWS Paper Wrapper (`.kiro/scripts/run-daily-aws-paper.ps1`) 1~17.
- This spec's responsible steps 9 kinds = Step 6 / 7 / 8 / 9 / 10 / 11 / 14 / 15 / 16.
- expression = "Daily wrapper-based manual orchestration validation" / the Step Functions implementation itself is kept as a follow-up orchestration target.
- 03 spec (1 / 12 / 13 / 17) / 08 spec (2 / 3) / 09 spec (4 / 5) operation-notes 2026-06-22 consistency.
- environment `aws-paper` / RunDate `2026-06-22` / 0 aws-live work.
- 0 broker / KIS calls within this spec scope (order submission responsibility 03 spec Step 12 / the 1 KIS paper SELL is 03 spec responsibility).
- `--execute` means strategy execution internal state creation·update (OD-MS-016 consistency).
- Kiro performs only document authoring / procedure organization / the actual ECS RunTask / IAM / RDS / GRANT work is carried out directly by the operator.

### 1. Step 6 ~ Step 8 (Daily Decision · Buy Execution Plan)

1. Step 6 `DAILY_BUY_SIGNAL`: complete
   1) ECS RunTask `portfolio-paper-strategy-decision-buy-signal:1` exitCode 0
   2) `decision.strategy_daily_signal` new row consistency
2. Step 7 `DAILY_POSITION_SIGNAL`: complete
   1) ECS RunTask `portfolio-paper-strategy-decision-position-signal:1` exitCode 0
   2) position decision created for the 6 stocks held this date (including `088350`) — including a `088350` 한화생명 SELL_HARD_STOP decision
3. Step 8 `DAILY_BUY_EXECUTION`: complete
   1) ECS RunTask (`portfolio-paper-strategy-execution:1` + command override `python daily_buy_execution_run.py`) exitCode 0
   2) `execution.strategy_execution_plan id 96` created — `plan_date 2026-06-22` / `strategy_name strategy_ai` / `market_signal DEFENSIVE` / `risk_regime DEFENSIVE` / `plan_status PARTIALLY_BLOCKED` / total_candidate 3 / ready 1 / blocked 2 / skipped 0 / total_target_amount `1,237,080` / available_cash `-62,763` / max_order_amount `-31,381.50`
   3) 2 BUY BLOCKED — cash shortage reason consistency / 0 safety criteria violations (OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004 consistency)

### 2. Step 9 `DAILY_SELL_EXECUTION` — First Permission-missing Failure + GRANT Correction

1. First failure cause identification: confirmed
   1) ECS RunTask failed — `permission denied for table strategy_daily_position_decision` pattern (or an equivalent permission error)
   2) `execution_app` previously held only `decision.strategy_daily_position_decision` SELECT / UPDATE not granted
   3) Step 9 flow — after creating a SELL execution_order, must UPDATE the `execution_order_id` of the daily position decision row to link the SELL decision with the execution order → UPDATE permission required (R-DATA-005 [2026-06-22 reinforcement] consistency / R-DATA-013 new)
2. Operator direct GRANT correction: complete (02 spec / 06 spec operation-notes follow-up update target)
   1) executed `GRANT USAGE ON SCHEMA decision TO execution_app`
   2) executed `GRANT UPDATE ON TABLE decision.strategy_daily_position_decision TO execution_app`
   3) permission confirmation — confirmed both SELECT + UPDATE permissions on `execution_app`
3. Step 9 re-run: complete
   1) ECS RunTask exitCode 0
   2) `execution.strategy_execution_order id 37` (SELL `088350` 244 shares MARKET) created / `execution_status READY`
   3) `execution_order_id` UPDATE consistency of the `decision.strategy_daily_position_decision` row

### 3. Step 10 / Step 11 (Daily Auto SELL / BUY)

1. Step 10 `DAILY_AUTO_SELL`: complete
   1) ECS RunTask (`portfolio-paper-strategy-execution:1` + command override `python daily_auto_sell_execute_run.py --execute`) exitCode 0
   2) `execution_order id 37` status `READY -> REQUESTED` transition (OD-MS-016 responsibility separation consistency / 0 direct broker · KIS calls)
2. Step 11 `DAILY_AUTO_BUY`: complete
   1) ECS RunTask (`portfolio-paper-strategy-execution:1` + command override `python daily_auto_buy_execute_run.py --execute`) exitCode 0
   2) the 2 BUYs are already BLOCKED in Step 8 / 0 new `READY -> REQUESTED` transition targets in this step
   3) all 4 `connector_order_request_id` are NULL — 0 direct broker · KIS calls (03 spec Step 12 responsibility)

### 4. Step 12 ~ Step 17 (03 spec Step 12·13·17 / 04 spec Step 14·15·16)

1. Step 12 ~ Step 13: 03 spec operation-notes 2026-06-22 §2 / §3 consistency. Outside this spec scope.
2. Step 14 `SYNC_SELL_FILL`: complete
   1) ECS RunTask (`portfolio-paper-strategy-execution:1` + command override `python execution_sync_sell_fill.py`) exitCode 0
   2) SELL fill sync based on `connector_fill id 34` — `execution_order id 37` execution_status `REQUESTED -> SUBMITTED -> FILLED` transition consistency
3. Step 15 `SYNC_BUY_FILL`: complete
   1) ECS RunTask exitCode 0
   2) BUY fill 0 (0 BUY orders this date) — `[NO_TARGET]` normal processing
4. Step 16 `SYNC_BUY_POSITION`: complete
   1) ECS RunTask (`portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_position.py`) exitCode 0 — 0 regression of the 2026-06-18 additional-buy merge patch (OD-MS-024 consistency)
   2) 0 BUY position changes / only SELL liquidation occurred this date / separate sync responsibility is `mark_position_sell_ordered()` + Step 17 BALANCE_REFRESH responsibility
5. Step 17 `BALANCE_REFRESH`: 03 spec operation-notes 2026-06-22 §4 consistency. The transition of `strategy_position_state id 9` to remaining_qty 0 / position_status `CLOSED` / latest_sell_reason `SELL_HARD_STOP` is the consistent endpoint of this spec's SELL fill sync result + the `mark_position_sell_ordered()` call result at the point of the MarketConnector executor SELL success.

### 5. Decision / Risk Change Summary

1. New decision: complete
   1) OD-DB-011 (`execution_app`'s limited UPDATE permission on `decision.strategy_daily_position_decision`, 🟢 확정 / formal matrix update of 02 spec db-roles-and-grants is a follow-up responsibility)
2. Decision without body change: first demonstration memo reinforcement
   1) OD-MS-016 — the responsibility separation of Strategy Execution `READY -> REQUESTED` (Step 10) / MarketConnector `REQUESTED -> SUBMITTED` (03 spec Step 12) is first demonstrated in the SELL flow too
   2) OD-MS-021 — 17-step safety criteria consistency / 0 safety criteria violations other than the Step 9 permission correction
   3) OD-MS-017 — 0 regression of the single Task Definition + command override pattern (all 7 entrypoints called normally)
3. New risk: complete
   1) R-DATA-013 (risk of Step 9 SELL execution link update failure due to missing `execution_app` `decision` schema UPDATE permission, Status `Mitigated`)
4. Risk without body change: reinforcement memo
   1) R-DATA-005 — [2026-06-22 reinforcement] added the case of missing `execution_app`'s `decision` schema USAGE / `decision.strategy_daily_position_decision` UPDATE / formal matrix update of 02 spec db-roles-and-grants maintained as follow-up

### 6. Safety / Security Check Results (2026-06-22)

1. Order / execution safety:
   1) 0 broker / KIS / new order calls within this spec scope.
   2) `--execute` used only to mean strategy execution internal state update / independent of the 03 spec Step 12 broker call.
   3) SELL position `mark_position_sell_ordered()` is MarketConnector executor responsibility.
   4) 0 fill · position sync automatic retry / 0 aws-live work.
2. RDS change scope (0 DDL / DML limited to the 17-step normal flow):
   1) `decision.strategy_daily_signal` / `decision.strategy_daily_run`.
   2) `decision.strategy_daily_position_decision` (SELL HOLD decision + `execution_order_id` UPDATE).
   3) `execution.strategy_execution_plan` insert (`id 96`).
   4) `execution.strategy_execution_order` insert · update (`id 37` READY → REQUESTED → SUBMITTED → FILLED).
   5) `execution.strategy_position_state` update (`id 9` remaining_qty 244 → 0 / OPEN → CLOSED / latest_sell_reason `SELL_HARD_STOP`).
   6) GRANT = performed directly by the operator (USAGE ON SCHEMA decision + UPDATE ON decision.strategy_daily_position_decision to execution_app / 02 · 06 spec operation-notes follow-up update).
3. 0 plaintext records of sensitive information — secret value / KIS app key / app secret / account number / token / RDS password / RDS endpoint hostname / account-id / actual IAM Role ARN / secret ARN / IAM access key id / instance-id / image digest / task ARN / job ARN all `[REDACTED]` or placeholder.
4. Operational identifiers (fact record / not secrets):
   1) `execution_plan_id 96` / execution_order `id 37` / connector_order_request `id 46` / connector_fill `id 34` / position_state `id 9`.
   2) ticker · company name · quantity · price · ratio.
   3) signal_date · run_date `2026-06-22`.
5. 0 changes to the 8 MS documents / source this date (spec area). The Step Functions state machine definition / EventBridge Scheduler periodic trigger / View orchestration mapping are kept as follow-up orchestration targets.

## 2026-06-23 Step Functions approval workflow Live Validation + Strategy Execution SELL E2E

### Summary

- Live validation pass of the Step Functions state machine `portfolio-paper-daily-step1-17-approval` + first actual SELL E2E pass of the Step 12~17 `allowPaperOrderExecute=true` approval true path.
- This spec's responsible steps = a total of 9: Step 6 / 7 / 8 / 9 / 10 / 11 / 14 / 15 / 16 + 04 spec consistency on the Step Functions orchestration overall flow side.
- 03 spec (1 / 12 / 13 / 17), 08 spec (2 / 3), 09 spec (4 / 5) are consistent with each spec's operation-notes 2026-06-23 / this note is a simple quotation.
- environment `aws-paper` / RunDate `2026-06-23` / region `ap-northeast-2` / 0 aws-live work.
- 0 direct broker / KIS calls (the 1 KIS paper SELL is 03 spec Step 12 responsibility / OD-MS-016 responsibility separation consistency).
- OD-MS-029 new consistency — the first actual SELL E2E round based on the Step Functions approval workflow (follow-up to the 2026-06-22 wrapper-based round).
- Keeps the "Step Functions follow-up orchestration target" expression of the ms-aws-service-decision-matrix body as-is.
- Kiro performs only document authoring / procedure organization / the actual Step Functions definition · execution / ECS RunTask / SSM / IAM / RDS / connector patch · deployment are carried out directly by the operator.

### 1. Step Functions state machine `portfolio-paper-daily-step1-17-approval` orchestration Live Validation

1. state machine operation policy (OD-MS-029 new consistency): confirmed
   1) defined directly by the operator / this note records only fact identifiers + operation procedure consistency / 0 quotations of the state machine definition body / full Amazon States Language (ASL) (R-DOCS-001 consistency)
   2) **single state machine + input parameter branching** — the false path and true path are not separated into distinct state machines / branches only by the input parameter (`allowPaperOrderExecute`) and the start step (`StartStep` / input corresponding to `EndStep`) of the same `portfolio-paper-daily-step1-17-approval`
   3) approval gate = `allowPaperOrderExecute` input value. Entry into the true path is performed by the operator only after checking the false path result (R-AUTO-019 mitigation Step Functions-side consistency / post-verification of the `PaperOrder: True` label in Step 12 stdout)
2. false path pre-validation: passed
   1) `allowPaperOrderExecute=false` / Step 1 ~ Step 17 approval blocked path
   2) Step 10 / Step 11 / Step 12 input values (READY / REQUESTED candidates / ticker / quantity / reason) paper environment dry-run confirmation
   3) 0 direct broker · KIS calls blocked before entering Step 12 / on the 04 spec side the `READY -> REQUESTED` transition of `execution.strategy_execution_order` proceeds normally (the Step 10 / Step 11-side `--execute` means strategy execution internal state update / OD-MS-016 responsibility separation consistency / 0 broker calls)
   4) operator result check — this date's Step 7 DAILY_POSITION_SIGNAL BGF리테일 `282330` SELL_HARD_STOP decision / Step 9 DAILY_SELL_EXECUTION `execution.strategy_execution_order id 40` creation / 04 spec-side flow consistency pre-validation passed
3. true path approval execution: passed
   1) `allowPaperOrderExecute=true` / Step 12 ~ Step 17 approval true path
   2) Step 12 KIS paper SELL 1 submission = 03 spec 2026-06-23 §2 consistency (`connector.connector_order_request id 48` / `broker_order_no 0000006143` / 04 spec-side `execution.strategy_execution_order id 40` SUBMITTED → FILLED)
   3) Step 14 SYNC_SELL_FILL / Step 17 BALANCE_REFRESH normal pass — this spec §5 / 03 spec 2026-06-23 §4 consistency
4. Step Functions-side ECS RunTask / SSM RunCommand / AWS Batch call consistency: confirmed
   1) The ECS RunTask Task Definitions of this spec's responsible steps (6 / 7 / 8 / 9 / 10 / 11 / 14 / 15 / 16) are identical to the wrapper-based ones (2026-06-17 / 2026-06-18 / 2026-06-22) — `portfolio-paper-strategy-decision-buy-signal:1` / `portfolio-paper-strategy-decision-position-signal:1` / `portfolio-paper-strategy-execution:1` + command override (OD-MS-013 / OD-MS-017 consistency / 0 regression)
   2) The SSM RunCommand of the 03 spec's responsible steps (1 / 12 / 13 / 17) targets the MarketConnector EC2 / the flow of calling the wrapper-common MarketConnector env bootstrap function right before Step Functions entry is maintained (R-AUTO-021 [2026-06-23 reinforcement] consistency)
   3) The AWS Batch SubmitJob of the 09 spec's responsible steps (4 / 5) targets `portfolio-paper-strategy-research-queue` (OD-MS-019 consistency / 0 regression)
   4) Since the Step Functions state definition calls the same entrypoints (ECS Task Definition / SSM Document / Batch Job Definition), the existing operation policy bodies (OD-MS-013 / OD-MS-016 / OD-MS-017 / OD-MS-019 / OD-MS-021 / OD-MS-023 / OD-MS-024 / OD-MS-025 / OD-MS-026 / OD-MS-027 / OD-MS-028) are unchanged during the wrapper-based → Step Functions-based transition

### 2. Step 6 ~ Step 8 (Daily Decision · Buy Execution Plan)

1. Step 6 `DAILY_BUY_SIGNAL`: complete
   1) ECS RunTask `portfolio-paper-strategy-decision-buy-signal:1` exitCode 0
   2) `decision.strategy_daily_signal` new row consistency / this date's BUY READY candidates 0 or BLOCKED (cash situation consistency) — the exact candidate count / ticker is not specified in the _common meta / consistency confirmed as 0 new `READY -> REQUESTED` transition targets in Step 11 DAILY_AUTO_BUY (fact-reinforce the BUY candidate count of this § at the point of follow-up spec confirmation)
2. Step 7 `DAILY_POSITION_SIGNAL`: complete
   1) ECS RunTask `portfolio-paper-strategy-decision-position-signal:1` exitCode 0
   2) position decision created for the 5 stocks held this date (2026-06-22 §4 balance 5 stocks consistency / `003490` / `004990` / `023530` / `042660` / `282330`)
   3) including the `282330` BGF리테일 SELL_HARD_STOP decision — the exact entry_date / entry_price / current_price / expected_pnl_rate / hard_stop_loss_rate / holding_days / snapshot_qty / sellable_qty / remaining_qty 17 / expected_pnl_amount is not specified in the _common meta / reinforce the SELL_HARD_STOP decision metadata of this § at the point of follow-up spec confirmation
3. Step 8 `DAILY_BUY_EXECUTION`: complete
   1) ECS RunTask (`portfolio-paper-strategy-execution:1` + command override `python daily_buy_execution_run.py`) exitCode 0
   2) `execution.strategy_execution_plan` new row insert consistency — this date's plan id is not specified in the _common meta / presumed to be the follow-up id of the 2026-06-22 plan id `96` (fact-reinforce the plan id of this § at the point of follow-up spec confirmation)
   3) 0 safety criteria violations (OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004 consistency) / BUY candidates 0 or BLOCKED consistency / 0 duplicate plan creation

### 3. Step 9 `DAILY_SELL_EXECUTION` — execution_order id 40 Creation (R-DATA-013 mitigation 0 regression)

1. Permission regression check: passed
   1) The 2026-06-22 §2 `execution_app` GRANT correction (`USAGE ON SCHEMA decision` + `UPDATE ON decision.strategy_daily_position_decision`) is maintained as-is this date too / 0 R-DATA-013 mitigation regression
   2) 0 occurrences of the `permission denied for table strategy_daily_position_decision` pattern
2. Step 9 run result: complete
   1) ECS RunTask (`portfolio-paper-strategy-execution:1` + command override `python daily_sell_execution_run.py`) exitCode 0
   2) `execution.strategy_execution_order id 40` newly created — action_type SELL / ticker `282330` / company name BGF리테일 / quantity 17 / order_method MARKET / `execution_status READY` / `connector_order_request_id IS NULL` (no connector-side link before entering Step 12 — 03 spec Step 12 responsibility / OD-MS-016 responsibility separation consistency)
   3) `execution_order_id` UPDATE consistency of the `decision.strategy_daily_position_decision` row — SELL decision and execution_order id 40 link passed (R-DATA-013 mitigation consistency / R-DATA-005 [2026-06-22 reinforcement] consistency)
3. Side facts: confirmed
   1) The Step 9 SELL execution_order creation + daily_position_decision link UPDATE flow is the responsibility of this spec's `daily_sell_execution_run.py` entrypoint / 1 of the 7 command overrides of the 04 spec Task Definition `portfolio-paper-strategy-execution:1` (OD-MS-017 consistency / 0 regression)
   2) The SELL liquidation status change of this spec's `execution.strategy_position_state` is the responsibility of Step 14 SYNC_SELL_FILL + the MarketConnector executor's `mark_position_sell_ordered()`, not this step (OD-MS-016 consistency / this note §5 consistency)

### 4. Step 10 / Step 11 (Daily Auto SELL / BUY)

1. Step 10 `DAILY_AUTO_SELL`: complete
   1) ECS RunTask (`portfolio-paper-strategy-execution:1` + command override `python daily_auto_sell_execute_run.py --execute`) exitCode 0
   2) `execution.strategy_execution_order id 40` status `READY -> REQUESTED` transition (OD-MS-016 responsibility separation consistency / 0 direct broker · KIS calls)
   3) `connector_order_request_id` kept NULL in this step (MarketConnector-side Step 12 responsibility / OD-MS-016 consistency)
   4) `--execute` means strategy execution internal state update / not a direct broker submission (R-AUTO-014 mitigation consistency / 0 regression of state ↔ command 1:1 mapping)
2. Step 11 `DAILY_AUTO_BUY`: complete
   1) ECS RunTask (`portfolio-paper-strategy-execution:1` + command override `python daily_auto_buy_execute_run.py --execute`) exitCode 0
   2) BUY candidates 0 or BLOCKED — 0 new `READY -> REQUESTED` transition targets in this step / consistency
   3) 0 direct broker · KIS calls (03 spec Step 12 responsibility / 0 BUY orders this date)
3. retry-normalizer entry review (OD-MS-028 consistency): confirmed
   1) 0 rows with the combination `execution_status IN (READY, FAILED)` + `connector_order_request_id IS NOT NULL` of this date's `execution.strategy_execution_order` → 0 application of the retry-normalizer at the start of 03 spec Step 12 (R-AUTO-022 mitigation consistency / 0 application of `strategy_execution_order` UPDATE on the 04 spec side)
   2) Right before entering Step 12, the 04 spec-side `execution.strategy_execution_order id 40` status = REQUESTED + `connector_order_request_id IS NULL` / default filter consistency / normal broker submission proceeds without bypassing the retry-normalizer

### 5. Step 14 / Step 15 / Step 16 (Strategy Execution sync)

1. Step 12 ~ Step 13 / Step 17: 03 spec operation-notes 2026-06-23 §2 / §3 / §4 consistency. Outside this spec scope. broker_order_no `0000006143` / `connector.connector_order_request id 48` / after Step 17 BALANCE_REFRESH the 4 held stocks consistency — only fact quotation on this spec side.
2. Step 14 `SYNC_SELL_FILL`: complete
   1) ECS RunTask (`portfolio-paper-strategy-execution:1` + command override `python execution_sync_sell_fill.py`) exitCode 0
   2) SELL fill sync based on the `connector.connector_fill` row (03 spec 2026-06-23 §3 consistency / SELL side / ticker `282330` BGF리테일) — `execution.strategy_execution_order id 40` execution_status `REQUESTED -> SUBMITTED -> FILLED` transition consistency
   3) 1:1 link consistency between `connector.connector_order_request id 48` and `execution.strategy_execution_order id 40` / 0 duplicate sync / 0 idempotency regression
3. Step 15 `SYNC_BUY_FILL`: complete
   1) ECS RunTask (`portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_fill.py`) exitCode 0
   2) BUY fill 0 (0 BUY orders this date) — `[NO_TARGET]` normal processing
4. Step 16 `SYNC_BUY_POSITION`: complete
   1) ECS RunTask (`portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_position.py`) exitCode 0
   2) 0 BUY position changes / only SELL liquidation occurred this date / 0 regression of the additional-buy merge patch (OD-MS-024 consistency) / 0 new OPEN INSERT of `strategy_position_state` / 0 additional-buy merge path entry
   3) SELL liquidation-side `execution.strategy_position_state` BGF리테일 OPEN → CLOSED transition:
       - consistent endpoint = the point of the MarketConnector executor `mark_position_sell_ordered()` call + the `connector.connector_position_snapshot` update of Step 17 BALANCE_REFRESH (OD-MS-016 responsibility separation consistency).
       - this date's BGF리테일 position_state row = OPEN → CLOSED / remaining_qty 17 → 0 / latest_sell_reason `SELL_HARD_STOP`.
       - the exact position_state row id is not specified in the _common meta / fact-reinforce at the point of follow-up spec confirmation.

### 6. Step 12 retry-normalizer 04 spec-side Consistency (OD-MS-028 new)

1. This spec-side impact scope: confirmed
   1) OD-MS-028 affected spec = 03 · 04 · 10 / the 04 spec-side impact is the possibility of retry-normalizer UPDATE application to `execution.strategy_execution_order`
   2) When the Step Functions Step 12 state calls `port-marketconnector/connector_strategy_order_execute.py`, using `.venv/bin/python` consistency — even after the Step Functions transition, the same entrypoint is called and the retry-normalizer operates as-is
   3) The 04 spec-side `execution.strategy_execution_order` is the target of the retry-normalizer's recovery processing (`execution_status = REQUESTED` + `connector_order_request_id = NULL` + `result_payload.retry_normalizer` metadata storage) / 0 retry candidates this date / 0 recovery application / 0 UPDATE application on the 04 spec side
2. Responsibility boundary consistency: confirmed
   1) OD-MS-016 body unchanged — the Strategy Execution (04 spec) `READY -> REQUESTED` / MarketConnector (03 spec) `REQUESTED -> SUBMITTED` responsibility separation is maintained as-is
   2) The retry-normalizer is an internal safety supplement at the start of MarketConnector-side Step 12 / the entrypoint bodies of 04 spec (`daily_auto_sell_execute_run.py` / `daily_auto_buy_execute_run.py` / `execution_sync_sell_fill.py` etc.) are unchanged this date
   3) Unless a 04 spec-side follow-up phase Step Functions state definition adds a broker call / `connector.connector_order_request` creation flow outside Step 12, the retry-normalizer application scope is maintained as-is (OD-MS-028 body risk memo consistency)
3. 6-condition recovery consistency quotation: confirmed (03 spec §1 consistency / body quotation in this note is limited to fact mapping)
   1) `execution_mode = PAPER_STRATEGY` + `action_type IN (BUY, SELL)` + `execution_status IN (READY, FAILED)` + `connector_order_request_id IS NOT NULL` + linked `connector_order_request.request_status = REJECTED` + `rejection_code = 40580000` + `broker_order_no IS NULL` + no `connector_fill`
   2) 0 rows satisfying all 6 conditions above in this date's 04 spec-side `execution.strategy_execution_order` inventory / 0 retry-normalizer entry

### 7. Decision / Risk Change Summary

1. New decision: complete
   1) OD-MS-028 (Step 12 retry-normalizer built-in policy / 🟡 잠정 / affected spec 03 · 04 · 10 / the this spec-side impact is the `execution.strategy_execution_order` UPDATE application scope)
   2) OD-MS-029 (Daily AWS Paper Step Functions approval workflow false / true path operation procedure / 🟢 확정 / affected spec 04 · 10 / the this spec-side impact is the Step Functions state machine orchestration responsibility / the ECS RunTask Task Definition · command override mapping of Step 6 ~ Step 11 / Step 14 ~ Step 16 is identical to the wrapper-based one)
2. Decision without body change: first demonstration memo reinforcement
   1) OD-MS-009 — the Step Functions side of Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask enters the demonstration stage this date (approval workflow validation pass) / EventBridge Scheduler periodic trigger / formal production automation entry is a follow-up phase
   2) OD-MS-013 / OD-MS-017 — the Strategy Decision 2-Task-Definition separation / Strategy Execution single Task Definition + command override pattern is called identically on the Step Functions side too / 0 regression
   3) OD-MS-016 — the Strategy Execution / MarketConnector responsibility separation is first demonstrated on the Step Functions side too, limited to 1 SELL
   4) OD-MS-021 / OD-MS-023 — the 17-step safety criteria / wrapper operation policy body is maintained as-is / the Step Functions approval gate plays a role equivalent to the wrapper's `-AllowPaperOrderExecute` PAPER_ORDER_GATE (R-AUTO-019 mitigation Step Functions-side consistency)
   5) OD-MS-024 — 0 regression of the `execution_sync_buy_position.py` additional-buy merge patch / 0 BUY this date / 0 merge path entry
   6) OD-DB-011 — `execution_app`'s limited UPDATE permission on `decision.strategy_daily_position_decision` maintained / 0 Step 9 regression
3. New risk: complete
   1) R-AUTO-022 (risk of a broken automatic re-submission path after a market-close REJECTED · `40580000`, Status `Mitigated` — Step 12 start built-in retry-normalizer + 6 recovery conditions / this date's dry-run pass / 0 application of `execution.strategy_execution_order` UPDATE on the 04 spec side / the first-round validation with an actual retry candidate occurrence is followups-overview 2026-06-23 §3 follow-up)
4. Risk without body change: reinforcement memo
   1) R-AUTO-021 — [2026-06-23 reinforcement] even after the Step Functions transition, the Step 1 / 12 / 13 / 17 wrapper-common bootstrap call flow must be maintained / Status `Mitigated` maintained
   2) R-DATA-005 — [2026-06-22 reinforcement] 0 regression of the missing `execution_app` decision schema USAGE / UPDATE / this date's permission consistency / formal matrix update of 02 spec db-roles-and-grants maintained as follow-up
   3) R-DATA-013 — risk of Step 9 SELL execution link update failure due to missing `execution_app`'s `decision` schema UPDATE permission / Status `Mitigated` maintained / 0 regression this date
   4) R-AUTO-001 / R-AUTO-014 / R-AUTO-015 — automatic retry / command override mismapping / Strategy Research heavy job accidental execution risk / all 0 mitigation regression this date
   5) R-BROKER-004 — risk of duplicate orders after a KIS paper API timeout / 0 timeout occurrences this date / 0 mitigation regression

### 8. Safety / Security Check Results (2026-06-23)

1. Order / execution safety:
   1) 0 broker / KIS / new order calls within this spec scope.
   2) `--execute` used only to mean strategy execution internal state update / independent of the 03 spec Step 12 broker call.
   3) SELL position `mark_position_sell_ordered()` is MarketConnector executor responsibility (OD-MS-016 consistency).
   4) 0 fill · position sync automatic retry / 0 aws-live work.
2. RDS change scope (0 DDL / DML limited to the Step Functions approval true path normal flow):
   1) `decision.strategy_daily_signal` insert.
   2) `decision.strategy_daily_run` insert · update.
   3) `decision.strategy_daily_position_decision` insert (including 282330 BGF리테일 SELL_HARD_STOP) + `execution_order_id` UPDATE (`id 40` link).
   4) `execution.strategy_execution_plan` insert (this date's plan id not specified in the _common meta / follow-up fact reinforcement).
   5) `execution.strategy_execution_order` insert · update (`id 40` READY → REQUESTED → SUBMITTED → FILLED).
   6) `execution.strategy_position_state` update (BGF리테일 OPEN → CLOSED / remaining_qty 17 → 0 / latest_sell_reason `SELL_HARD_STOP` / row id not specified in the _common meta).
   7) 0 new GRANT this date (0 regression of the 2026-06-22 §2 GRANT).
   8) 0 application of the retry-normalizer's `strategy_execution_order` UPDATE (0 retry candidates / OD-MS-028 consistency).
3. 0 plaintext records of sensitive information — secret value / KIS app key / KIS app secret / account number / account password / token / RDS password / RDS endpoint hostname / account-id / actual IAM Role ARN / actual secret ARN / IAM access key id / instance-id / image digest full sha256 / task ARN / job ARN / Step Functions execution ARN / full PowerShell stdout all `[REDACTED]` or placeholder.
4. AWS / boto3 execution:
   1) AWS / Step Functions / ECS / SSM / EC2 / RDS / Secrets Manager / KIS API / S3 / CloudWatch calls are all performed directly by the operator.
   2) Kiro performs only document authoring / procedure organization / 0 AWS CLI · boto3 executions / 0 AWS resource creation · modification · deletion.
   3) 0 plaintext records of `secretsmanager:GetSecretValue` result values.
   4) 0 plaintext quotations of CloudWatch Logs body / Step Functions execution history body / ASL body / SSM response body / KIS API response body / ECS Task describe body / operator patch body.
5. 0 changes to the 8 MS documents / source this date (spec area):
   1) The `port-marketconnector/connector_strategy_order_execute.py` full replacement + Step 12 start built-in retry-normalizer change the operator directly patched is recorded as a fact in the 03 spec operation-notes 2026-06-23 §1 (0 this spec body quotations / R-DOCS-001 consistency).
   2) The Step Functions state machine definition / EventBridge Scheduler periodic trigger / View orchestration mapping enter the demonstration stage this date / formal production automation entry and formal accumulation of the ASL body are a follow-up orchestration phase responsibility.
6. Operational identifiers (not secrets / fact record):
   1) Step Functions state machine `portfolio-paper-daily-step1-17-approval`.
   2) `execution.strategy_execution_order id 40` / `connector.connector_order_request id 48` / `broker_order_no 0000006143`.
   3) ticker `282330` BGF리테일 / quantity 17 / sell MARKET / SELL reason `SELL_HARD_STOP`.
   4) approval gate label `allowPaperOrderExecute=false` · `=true` / wrapper stdout label `PaperOrder: True`.
   5) Task Definition `portfolio-paper-strategy-decision-buy-signal:1` · `portfolio-paper-strategy-decision-position-signal:1` · `portfolio-paper-strategy-execution:1`.
   6) signal_date · run_date `2026-06-23`.


### 9. AWS Common Slack notifier First Validation + Step Functions 3-kind Slack Receipt Validation (OD-MS-030 / OD-MS-031 new)

#### Summary

- Follow-up to §1~§8 of the same date (2026-06-23) / AWS common Slack notifier Lambda implementation + Step Functions approval workflow 3-kind Slack receipt validation.
- 04 spec responsibility area = Step Functions orchestration + operational observability auxiliary layer only.
- 0 plaintext quotations (R-DOCS-001 consistency):
  - Lambda code / IAM Role inline policy / environment variable value / Slack webhook URL.
  - Slack message body / Step Functions Catch state ASL body.
- fact record targets = Lambda name / Role name / Runtime / environment variable key / Slack event label / completion marker / bot name.

1. New AWS common Slack notifier (OD-MS-030 new / 🟡 잠정): complete
   1) Lambda name: `portfolio-event-notifier`
   2) Runtime: Python 3.12
   3) IAM Role: `portfolio-event-notifier-lambda-role`
   4) role = a common Slack notification auxiliary layer for PORT-STRATEGY-AI-wide operational events — a single entrypoint callable from anywhere: Step Functions / EventBridge / EC2 SSM / Batch / Lambda
   5) Slack webhook URL = first validated via Lambda environment variable `SLACK_WEBHOOK_URL` (value not recorded / R-DOCS-001 consistency / 0 plaintext output in this note)
   6) to be migrated to Secrets Manager or SSM Parameter Store (SecureString) after operational stabilization (R-AUTO-024 new / Status `Accepted` / 06 spec follow-up)
   7) **This Lambda is not the main compute of this spec's Strategy Execution / Strategy Decision MS** — the Lambda-not-recommended policy (ms-aws-service-decision-matrix body) is maintained as-is / the Slack notifier is used only as an operational event notification auxiliary layer
2. Smoke / template validation: complete
   1) `hello wook` manual invoke success — consistency of the Lambda console / CLI invoke path
   2) Slack receipt confirmed — message reached the operator Slack channel normally
   3) Portfolio Daily Bot message received — Slack bot name consistency
   4) completion marker: `SLACK_LAMBDA_SMOKE_TEST=SUCCESS`
   5) 6 common message templates first composed — (a) pre-market balance / held stock status / (b) Daily validation complete · approval required / (c) Daily execution success / (d) Daily execution failure / (e) intraday stop-loss candidate / (f) post-market balance / held stock status
   6) profit / loss display = 🔴 / 🔵 / ⚪ emoji applied / Slack attachment color bar applied
   7) completion marker: `PORTFOLIO_EVENT_NOTIFIER_TEMPLATE_TEST=SUCCESS`
3. Step Functions 3-kind Slack receipt validation (OD-MS-031 new / 🟢 확정 / R-AUTO-023 new mitigation first demonstration): complete
   1) `APPROVAL_REQUIRED` — sent when entering the approval gate after Step 1~11 completion. Role grant complete / approval-required Slack receipt confirmed after Step 1~11 completion / validated the flow where the operator can recognize the approval-required state via Slack upon approval gate entry (consistent with the false path pre-validation + true path approval execution flow of this § §2).
   2) `DAILY_EXECUTION_SUCCESS` — sent on overall success after Step 17 completion. Daily success Slack receipt confirmed after Step 17 completion / validated the Slack receipt path on Step Functions overall success termination (consistent with the Step 17 BALANCE_REFRESH pass of this § §1 true path validation — 03 spec operation-notes 2026-06-23 §4 consistency).
   3) `DAILY_EXECUTION_FAILED` — sent on failure during Step Functions execution:
       - 12~17 test-only failure Slack receipt confirmed.
       - 1~17 full workflow failure Slack ASL applied.
       - 1~17 full workflow test-only failure Slack receipt confirmed.
       - validated the `DAILY_EXECUTION_FAILED` notification send path in an operational failure case.
       - **The failure validation is a test-only failure injection and does not intentionally cause an actual broker order failure** — 0 additional broker calls / 0 aws-live work / R-AUTO-001 · R-AUTO-002 · OD-SAFE-001~004 consistency.
4. EventBridge automation handover (OD-MS-031 consistency): handover
   1) #7 EventBridge automation applies only the 3 Slack kinds (`APPROVAL_REQUIRED` + `DAILY_EXECUTION_SUCCESS` + `DAILY_EXECUTION_FAILED`) first / the EventBridge Scheduler periodic trigger cron time / timezone / holiday guard decisions are a 04 / 10 spec follow-up phase responsibility (OD-MS-009 body unchanged).
   2) pre-market balance / post-market balance / intraday stop-loss notifications are separated as a follow-up stage — this date's 6 templates are only first composed / the actual send flow is outside this application scope.
   3) The first goal is not message wording enhancement but validating whether Slack is received in the Step Functions execution flow — the first goal passed as the result of this § §3.
   4) **6. Slack implementation: complete / 7. EventBridge automation is the next stage** — the formal Step Functions state machine definition / EventBridge Scheduler periodic trigger entry / full application of the 6 Slack kinds / introduction of DLQ · retry · CloudWatch Alarm in this spec's follow-up phase is a follow-up separation.
5. Decision / Risk Change Summary (this § §9 only): complete
   1) New decision — OD-MS-030 (AWS common Slack notifier Lambda introduction policy, 🟡 잠정 / affected spec 04 · 05 · 10) + OD-MS-031 (Step Functions / EventBridge first Slack integration scope limited to 3 kinds policy, 🟢 확정 / affected spec 04 · 05 · 10).
   2) Decision without body change — OD-MS-009 (Daily Batch orchestration) / OD-MS-010 (infra alarm channel = keep port-view SlackNotificationService + SNS·Lambda·Slack fan-out auxiliary) / OD-MS-029 (Step Functions approval workflow) first demonstration memo reinforced without body change.
   3) New risk — R-AUTO-023 (risk of Slack omission on the Step Functions failure path, Status `Mitigated` — Catch path `DAILY_EXECUTION_FAILED` send + test-only failure injection receipt validation pass). R-AUTO-024 (risk of weakened secret management from long-term storage of the Slack webhook URL in a Lambda environment variable, Status `Accepted` — proceed with migration to Secrets Manager · SSM Parameter Store after operational stabilization).
6. Safety / Security Check (this § §9 only): complete
   1) 0 plaintext records of the Slack webhook URL — only the Lambda environment variable key name and meaning recorded / 0 plaintext quotations of value plaintext / Lambda code body / Slack message body / Step Functions Catch state ASL body / Slack webhook response body (R-DOCS-001 consistency / all `[REDACTED]` or placeholder)
   2) 0 secret value records — all operational identifiers of this § §9 (Lambda name / Role name / Runtime / environment variable key / Slack event label / completion marker / bot name) are fact identifiers, not secrets
   3) The failure validation is a test-only failure injection and causes 0 actual broker order failures — validated only the flow where the Step Functions Catch state receives a test-only failure signal and sends the `DAILY_EXECUTION_FAILED` Slack / 0 actual broker / KIS calls / 0 additional `--execute` calls / 0 aws-live work
   4) AWS Lambda / IAM / Step Functions / Slack webhook calls are all performed directly by the operator — Kiro only document authoring / procedure organization. 0 AWS CLI / boto3 executions. 0 AWS resource creation / modification / deletion. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to this § work (spec area / port-view SlackNotificationService body unchanged)
   5) The existing port-view Slack is maintained — the responsibility of port-view SlackNotificationService for View Daily Batch manual execution result notification is unchanged / not removed · replaced in this work / only the possibility of future migration to the common notifier is recorded (05 spec follow-up phase responsibility)



## 2026-06-29 (2) — First local Validation of port-view Attaching as a Step Functions StartExecution external caller

Accumulates the result of the port-view-side commit `e72de6f` (`feat(view): add Step Functions daily batch trigger`) the operator directly performed this date, from the perspective of the 04 spec Step Functions state machine operation. This note has 0 plaintext quotations of port-view-side code body / IAM Policy / ASL / response body (R-DOCS-001 consistency).

§1. port-view added as an external caller
 1) Existing automatic trigger path (OD-MS-032)
   (1) EventBridge Scheduler → Dispatcher Lambda → Step Functions `StartExecution`
       - Scheduler `portfolio-paper-daily-step1-11-approval-0800-kst` (`ENABLED`) / 08:00 KST
       - Scheduler `portfolio-paper-daily-step12-17-order-0901-kst` (`DISABLED`) / 09:01 KST automatic ENABLE on hold (OD-MS-033)
 2) New operator manual trigger path (OD-MS-002 / OD-MS-009 / OD-MS-037 memo reinforcement)
   (1) View `/daily-batch` screen AWS Step 1~11 safe trigger button
       - port-view's `StepFunctionsDailyBatchExecutionService` calls `StartExecution` with the AWS SDK v2 Step Functions client
       - Controller endpoint `POST /daily-batch/aws-stepfunctions/start-range`
       - on success displays `executionName` + account-id redaction `executionArn` flash message
       - in `aws-stepfunctions` mode, View does not directly run a Python subprocess · `C:/Workspaces` local source
   (2) This path is limited to **operator manual trigger**, not an automatic trigger — no change to the Daily Batch automatic trigger policy (OD-MS-032 / OD-MS-033).

§2. The fact that `runDate` is required in the View input
 1) ASL state referencing `runDate.$=$.runDate`
   (1) `StopCrawlerEc2AfterStep11Success` (OD-MS-034 consistency) directly references the input's `runDate`
       - if there is no `runDate` in the external caller's `StartExecution` input, `States.Runtime` occurs
       - identified and supplemented in the View first validation
 2) Supplementation policy
   (1) the external `StartExecution` caller must always include `runDate` (Asia/Seoul yyyy-MM-dd) in the input JSON
       - port-view `StepFunctionsDailyBatchExecutionService` supplemented to automatically include an Asia/Seoul-based `runDate` in the input
       - the EventBridge Scheduler + Dispatcher Lambda path has the Lambda generate a KST `runDate` and include it in the input per OD-MS-032 consistency / no change to the existing policy
   (2) ASL-side follow-up review
       - review introducing a fail-fast branch or a default `runDate` supplement branch when `runDate` is missing from the external caller input (04 spec follow-up phase responsibility / outside this date's work scope)

§3. Step 1~11 → StopCrawlerEc2AfterStep11Success → SendApprovalRequiredSlack Flow Validation
 1) end-to-end pass
   (1) first demonstration via View operator manual trigger
       - Step 1~11 workflow execution passed
       - `StopCrawlerEc2AfterStep11Success` task state (OD-MS-034) passed after `runDate` supplementation
       - reached `SendApprovalRequiredSlack`
       - Slack `APPROVAL_REQUIRED` receipt confirmed
       - 0 new `connector_order_request` / 0 broker order submission
 2) Step 12~17 policy maintained
   (1) blocking maintained based on `allowPaperOrderExecute=false`
       - Step 12 approval gate blocking consistency
       - no change to the paper-order gate policy
       - the 09:01 schedule automatic ENABLE on-hold policy (OD-MS-033) is maintained as-is

§4. View-side safety gate consistency (external caller responsibility separation)
 1) `StepFunctionsDailyBatchExecutionService` service-level safety gate
   (1) service-level blocking conditions
       - `canStartAwsStepfunctions=false`
       - `hasRunningBatch=true`
       - `stateMachineArn` empty value
       - outside the `minExecutableStepOrder` · `maxExecutableStepOrder` range
       - Step 12 or higher in the `allowPaperOrderExecute=false` state
       - approval requests are all blocked except `paperOrderEnabled=true`
   (2) 04 spec-side consistency
       - the state machine's own approval gate (`Step12_CheckApproval`) and the View-side service-level gate are separate layers
       - both must be satisfied to enter Step 12 or higher broker calls / R-AUTO-033 [2026-06-29 reinforcement (2)] / R-AUTO-034 new consistency

§5. Decision / Risk Mapping
 1) Decision body unchanged
   (1) OD-MS-009 / OD-MS-029 / OD-MS-031 / OD-MS-032 / OD-MS-033 / OD-MS-034 / OD-SAFE-001 ~ OD-SAFE-004 first demonstration memo reinforced without body change
       - see `../_common/operator-decisions.md` Change Log 2026-06-29 (2) item for detailed decision changes
 2) Risk mapping
   (1) R-AUTO-033 [2026-06-29 reinforcement (2)] / R-AUTO-034 new — see `../_common/risk-register.md`
       - port-view external caller first validation pass / Status `Mitigated` maintained
       - risk of excessive Fargate View permission + Step 12 gate bypass / Status `Open` / 06 spec follow-up phase responsibility

§6. Follow-up (04 spec follow-up phase responsibility)
 1) ASL `runDate` missing handling review
   (1) review introducing a fail-fast or default supplement branch when the external caller's input `runDate` is missing
 2) Step 12~17 external caller responsibility separation
   (1) if a Step 12~17 approval-type trigger is newly added, cross-spec audit of the consistency between the View-side preflight + approval gate + paper-order gate and the 04 spec state machine's approval gate
 3) Improvement of the Slack `APPROVAL_REQUIRED` summary 0/0 display (R-AUTO-027 mitigation extension) follow-up maintained as-is
 4) Whether to automatically ENABLE the 09:01 schedule (OD-MS-033 on-hold policy) maintained as-is

§7. This Date's Fact Record Scope
 1) Kiro work = accumulated only this section of the 04 spec `operation-notes.md`
 2) The operator's direct commit `e72de6f` code change is in the port-view MS area (0 changes to the 04 spec area)
 3) 0 changes this date to AWS / EventBridge Scheduler / Lambda / Step Functions / SSM / EC2 / RDS / S3 / KIS API calls / 0 AWS resource new creation · modification · deletion
 4) 0 plaintext quotations of `StartExecution` response body / Step Functions execution history body / Slack message body / Lambda response body / KIS API response body / full Spring Boot application log
 5) 0 plaintext records of sensitive information — all `[REDACTED]` or placeholder:
   (a) secret value / KIS app key · KIS app secret / account number / token.
   (b) RDS password / RDS endpoint hostname / raw 12-digit account-id.
   (c) actual IAM Role · secret · state machine ARN / IAM access key id / instance-id / Slack webhook URL.
 6) Operational identifiers (fact record consistent with the user-specified policy / not secrets):
   (a) commit hash `e72de6f` / commit message / Class name `StepFunctionsDailyBatchExecutionService` / Controller endpoint `/daily-batch/aws-stepfunctions/start-range`.
   (b) 4 state names = `StopCrawlerEc2AfterStep11Success` · `SendApprovalRequiredSlack` · `Step6ToStep11_Succeeded` · `Step12_CheckApproval`.
   (c) Slack event label `APPROVAL_REQUIRED` / error label `States.Runtime` / payload field label / Spring profile `aws-paper`.


## 2026-06-29 (3) — Second phase Validation of port-view Attaching as the external caller of the Step 12~17 approval state machine

### Summary

- Operator direct performance = 5 additional changes on the port-view side (`DailyBatchProperties.java` · `StepFunctionsDailyBatchExecutionService.java` · `application-aws-paper.properties` · `DailyBatchController.java` · `daily_batch.html`).
- Accumulated from the perspective of the 04 spec Step Functions state machine operation.
- 0 plaintext quotations (R-DOCS-001 consistency) — port-view code body / IAM Policy / ASL / response body / `StartExecution` input JSON body.

§1. Step 12~17 approval state machine ARN separation
 1) Separation of the general workflow and the approval workflow: complete
   (1) 2 target state machines
       - general workflow: `portfolio-paper-daily-step1-17-approval`
       - approval workflow: `portfolio-paper-daily-step12-17-approval`
   (2) View-side mapping
       - `startSafeRange` = uses the general ARN
       - `startApprovalRange` = uses the approval-dedicated ARN
       - if the approval ARN is empty, blocked at the View service level
   (3) Fargate Task Role follow-up (06 spec responsibility)
       - the `states:StartExecution` Resource pattern needs to be scoped to both the general ARN + approval ARN
       - the Resource · Action wildcard 0 maintenance policy is kept as-is

§2. `Step12_CheckApproval` Choice `BooleanEquals` condition and external caller payload consistency
 1) payload type consistency first demonstration: complete
   (1) boolean fields
       - `allowPaperOrderExecute=true` (boolean JSON)
       - `paperOrderEnabled=true` (boolean JSON)
       - identified a case where being passed as the string `"true"` is blocked at `Step12_CheckApproval`
   (2) numeric fields
       - `fromStepOrder` · `toStepOrder` · `startStep` · `endStep` are passed as numeric JSON
       - string numerics may also cause abnormal branching in some states → numeric passing recommended
   (3) ASL-side follow-up review
       - introducing a fail-fast branch or default coercion branch to enforce external caller payload type consistency is a 04 spec follow-up phase responsibility (outside this date's work scope)
       - if the external caller passes the payload consistent with this note §2.1.1 / §2.1.2, the current ASL operates as-is consistently

§3. Step 12~17 → 13~17 Flow Validation
 1) end-to-end pass: complete
   (1) state progression consistency
       - `Step12_CheckApproval` passed
       - `Step12_RunMarketConnectorStrategyOrderExecute` executed
       - `Step12_GetCommandInvocation` success
       - Step 13~17 fully progressed
       - `ExecutionSucceeded` confirmed
   (2) DB post-verification
       - operational marker `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS`
       - 0 new `connector_order_request` today
       - 0 READY / REQUESTED `strategy_execution_order`
       - no new broker order submission
       - recent `connector_order_request` shows only existing orders from 2026-06-22 ~ 2026-06-24
   (3) validation identifiers
       - executionName `port-view-step12-17-step12-17-20260629-194314-ba5edaf8`
       - status `SUCCEEDED`
       - start `2026-06-29T19:43:15.673+09:00`
       - stop `2026-06-29T19:46:06.546+09:00`
       - 0 plaintext quotations in this note of Step Functions execution history body / SSM stdout body

§4. View-side safety gate consistency (external caller responsibility separation)
 1) `StepFunctionsDailyBatchExecutionService` service-level safety gate
   (1) service-level blocking conditions (approval entry added)
       - `canStartAwsStepfunctions=false`
       - `hasRunningBatch=true`
       - general `stateMachineArn` empty value or approval `stateMachineArn` empty value
       - outside the `minExecutableStepOrder` · `maxExecutableStepOrder` range
       - Step 12 or higher in the `allowPaperOrderExecute=false` state
       - approval requests are all blocked except `paperOrderEnabled=true`
       - immediately blocked on `startApprovalRange` entry if the approval ARN is absent
   (2) 04 spec-side consistency
       - the state machine's own approval gate (`Step12_CheckApproval`) and the View-side service-level gate are separate layers
       - both must be satisfied to enter Step 12 or higher broker calls / R-AUTO-033 [2026-06-29 reinforcement (3)] / R-AUTO-034 [2026-06-29 reinforcement] consistency

§5. Decision / Risk Mapping
 1) Decision body unchanged
   (1) OD-MS-009 / OD-MS-029 / OD-MS-031 / OD-MS-032 / OD-MS-033 / OD-MS-034 / OD-SAFE-001 ~ OD-SAFE-004 first demonstration memo reinforced without body change
       - see `../_common/operator-decisions.md` Change Log 2026-06-29 (3) item for detailed decision changes
 2) Risk mapping
   (1) R-AUTO-033 [2026-06-29 reinforcement (3)] / R-AUTO-034 [2026-06-29 reinforcement] — see `../_common/risk-register.md`
       - port-view external caller approval phase validation pass / Status `Mitigated` maintained / Fargate Task Role permission separation is a 06 spec follow-up phase responsibility

§6. Follow-up (04 spec follow-up phase responsibility)
 1) ASL payload type consistency enforcement review
   (1) review introducing a branch that enforces the external caller payload's boolean / numeric type consistency via fail-fast or default coercion before entering `Step12_CheckApproval`
 2) approval workflow Step 12~17 external caller responsibility separation
   (1) if a new external caller (e.g., Fargate View / another operator tool) is added, cross-spec audit of the boolean / numeric payload type consistency
 3) Improvement of the Slack `APPROVAL_REQUIRED` summary 0/0 display (R-AUTO-027 mitigation extension) follow-up maintained as-is
 4) Whether to automatically ENABLE the 09:01 schedule (OD-MS-033 on-hold policy) maintained as-is
 5) Step Functions execution history Catch state audit + introduction of DLQ · retry · CloudWatch Alarm (R-AUTO-023) maintained as-is

§7. This Date's Fact Record Scope
 1) Kiro work = accumulated only this section of the 04 spec `operation-notes.md`
 2) The operator's direct changes (port-view MS area) are 0 changes to the 04 spec area
 3) 0 new changes this date to AWS / EventBridge Scheduler / Lambda / Step Functions / SSM / EC2 / RDS / S3 / KIS API calls / 0 AWS resource new creation · modification · deletion
 4) 0 plaintext quotations of `StartExecution` response body / Step Functions execution history body / Slack message body / Lambda response body / KIS API response body / full Spring Boot application log / SSM stdout body / commit diff body / PowerShell wrapper body
 5) 0 plaintext records in this note of sensitive information (secret value / KIS app key / KIS app secret / account number / token / RDS password / RDS endpoint hostname / raw 12-digit account-id / actual IAM Role ARN / actual secret ARN / IAM access key id / instance-id / actual state machine ARN / Slack webhook URL / DB password) — all `[REDACTED]` or placeholder
 6) Operational identifiers (fact record consistent with the user-specified policy / not secrets):
   (a) executionName `port-view-step12-17-step12-17-20260629-194314-ba5edaf8` / operational marker `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS`.
   (b) 2 state machines = `portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval`.
   (c) 4 state names = `Step12_CheckApproval` · `Step12_RunMarketConnectorStrategyOrderExecute` · `Step12_GetCommandInvocation` · `ExecutionSucceeded`.
   (d) 2 Controller endpoints = `/daily-batch/aws-stepfunctions/start-range` · `/daily-batch/aws-stepfunctions/start-approval-range`.
   (e) 7 Spring properties keys / environment variable label / payload field label + boolean / numeric type / `requestedBy=VIEW_APPROVAL_BUTTON` label.
   (f) Spring profile `aws-paper` / start · stop timestamp.


## 2026-06-30 — 4-kind View operation path organization complete + Daily Batch gate operation-intent consistency fix + DB validation query authoring principle addition

Accumulates, from the perspective of the 05 spec ECS Fargate porting, the result of the `DailyBatchController.java` Daily Batch gate operation-intent consistency fix + the second demonstration pass of Local View → AWS Step Functions Step 12~17 approval execution the operator directly performed this date. This note has 0 plaintext quotations of port-view-side code body / IAM Policy / ASL / response body / `StartExecution` input JSON body / full DB post-verification raw output (R-DOCS-001 consistency).

8. 4-kind View operation path organization: complete
 1) 4 kinds of operator manual trigger separation on the Local View side: complete
   (1) Local View → Local File Step 1 single run: complete (2026-06-28 Run #46)
   (2) Local View → Local File Step 1~11 run: complete (2026-06-28 Run #47)
   (3) Local View → Local File Step 12~17 run: complete (2026-06-29 (1) Run #48)
   (4) Local View → AWS Step Functions Step 12~17 approval run: complete (this date)
       - executionName `port-view-daily-step12-17-20260630-095111-aae2595c`
       - state machine `portfolio-paper-daily-step12-17-approval`
       - status `SUCCEEDED`
       - start `2026-06-30T09:51:11.903+09:00`
       - stop `2026-06-30T09:54:16.484+09:00`
       - safe termination with no order target
       - DB post-verification passed (0 new `connector_order_request` / 0 new broker order)
 2) Operation path separation consistency: complete
   (1) Local File execution and AWS Step Functions execution separated-operation consistency
       - Local File execution gate: `localFileExecutionEnabled`
       - AWS Step Functions execution gate: `awsStepfunctionsStartEnabled` / `awsStepfunctionsStepStartEnabled`
       - approval range gate: `paperOrderEnabled=true` + approval ARN set
   (2) Step 12~17 order-related segment runs only via a separate approval-type state machine + on passing the paper-order gate consistency

9. Daily Batch gate operation-intent consistency fix: complete
 1) `DailyBatchController.java` fix: complete
   (1) AWS Step Functions button activation condition fix
       - AWS Step 12~17 approval button active even in the `fullPipelineExecutionEnabled=true` state
       - avoid conflict with the AWS Step 1~11 button condition even in the `paperOrderEnabled=true` state
       - keep the Local File execution gate and the AWS Step Functions execution gate separated
       - Step 12~17 runs only on passing `paperOrderEnabled=true` + approval range gate
       - 0 plaintext quotations in this note of the Java body / commit diff (R-DOCS-001 consistency)
   (2) Validation
       - mvn compile success
       - Local View `aws-paper` profile restart success
       - screen display pass (Execution ON / Local File OFF / Full Pipeline ON / Paper Order ON / allowed range `1~17` / AWS Step 12~17 approval execution button active)

10. DB validation query authoring principle addition: incomplete (formal reflection is a follow-up phase responsibility)
 1) Operation principles identified this date: complete (fact record)
   (1) Column name pre-confirmation obligation
       - SELECT after pre-confirming the target column with `information_schema.columns`
       - identified the case of the missing `connector_position_snapshot.balance_snapshot_id` column this date
   (2) SELECT only confirmed columns
       - relation columns (e.g., `balance_snapshot_id`) also prohibited from expected use
       - when absent, validate with a natural key like `account_no` + `as_of_date`
   (3) Result exposure pattern
       - post-verification queries prohibited from hiding results with `DO` / `EXECUTE`
       - author so that the final SELECT result appears directly on screen
   (4) Windows / PowerShell / psql environment consistency
       - for Korean SQL, keep the UTF-8 No BOM `.sql` file + `psql -f` pattern instead of direct `psql -c` execution
       - for SSM multiline commands, keep the UTF-8 No BOM JSON file + `--parameters file://...` pattern
 2) Formal reflection follow-up (05 spec follow-up phase responsibility)
   (1) formally reflect this principle when newly creating `validation-checklist.md` or an equivalent document
   (2) register as a cross-spec audit item at the Fargate cutover point
   (3) consistency with this principle when organizing the DB post-verification query collection

### Decision / Risk Mapping

- OD-MS-002 / OD-MS-009 / OD-MS-037 / OD-SAFE-001 ~ OD-SAFE-004 first demonstration memo reinforced without body change (2026-06-29 (3) Change Log item consistency maintained as-is / no new decision this date / no Decision Summary count change).
- R-AUTO-033 [2026-06-30 reinforcement] — Daily Batch gate operation-intent consistency + AWS Step Functions Step 12~17 approval execution second demonstration / Status `Mitigated` maintained.
- R-AUTO-034 [2026-06-30 reinforcement] — View-side 4 operation paths separation first demonstration + DB validation query authoring principle reinforcement / Status `Open` maintained / Fargate Task Role permission separation is a 06 spec follow-up phase responsibility maintained as-is.

### This Date's Fact Record Scope

- This date's Kiro work = accumulated only this section (items 8 · 9 · 10) of the 05 spec `operation-notes.md`.
- The operator's direct change (`DailyBatchController.java` Daily Batch gate fix) is in the port-view MS area, resulting in 0 changes due to this date's work of the cross-service AWS Migration spec (spec area).
- 0 changes this date to AWS CLI / boto3 / psql / Spring Boot execution / external API calls.
- 0 changes this date to AWS resource new creation · modification · deletion.
- broker / KIS call scope:
  - limited to the morning Step 1~11 automatic trigger (0 new `connector_order_request`).
  - 1 Local View → AWS Step Functions Step 12~17 approval execution (`SUCCEEDED` / NO_TARGET / 0 broker order submission).
  - balance refresh only / 0 additional BUY · SELL · cancel · modify.
  - 0 fill · position sync automatic retry / 0 aws-live work.
- 0 plaintext records of sensitive information — all `[REDACTED]` or placeholder:
  - secret value / KIS app key · KIS app secret / account number · account password / token.
  - RDS password / RDS endpoint hostname / raw 12-digit account-id.
  - actual IAM Role · secret · state machine ARN / IAM access key id / instance-id / EIP.
  - image digest full sha256 / task ARN / job ARN / raw broker_order_no.
  - Slack webhook URL / DB password / Administrator password.
- Operational identifiers (fact record consistent with the user-specified policy / not secrets):
  - executionName `port-view-daily-step12-17-20260630-095111-aae2595c` / state machine `portfolio-paper-daily-step12-17-approval`.
  - Controller class `DailyBatchController` / 6 Daily Batch gate labels / screen display labels.
  - balance snapshot `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505` · `eval_profit=0` · `source_version=connector-intraday-snapshot-refresh-1.0.0`.
  - DB column name `balance_snapshot_id` (absent) · `account_no` · `as_of_date`.
  - Run id `#46` · `#47` · `#48` / Spring profile `aws-paper` / start · stop timestamp.


## 2026-06-30 (afternoon) — Third phase Validation of ECS View Attaching as the external caller of the Step 12~17 approval state machine (cross-reference)

### Summary

- ECS Fargate first porting pass + ECS View → Step 12~17 approval StartExecution first demonstration.
- **0 changes to the Step Functions structure itself** — 0 changes to state machine ASL · IAM Role · EventBridge Scheduler · Dispatcher Lambda · ECS RunTask · SSM RunCommand · AWS Batch.
- The operator's direct performance area is 05 spec (ECS Fargate) / 06 spec (Fargate Task Role IAM) responsibility.
- This note has 0 plaintext quotations of ASL / IAM Policy / execution history body / Slack message body / full CloudWatch Logs / `StartExecution` response body (R-DOCS-001 consistency).

### 1. ECS View → approval state machine StartExecution First Demonstration

 1) external caller first demonstration: complete
   (1) ECS View → `portfolio-paper-daily-step12-17-approval` StartExecution
       - executionName: `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8`
       - state machine: `portfolio-paper-daily-step12-17-approval` (kept as-is from the 2026-06-29 (3) separation complete / 0 ASL changes this date)
       - status: `SUCCEEDED`
       - start: `2026-06-30T14:15:42.899+09:00`
       - stop: `2026-06-30T14:18:48.358+09:00`
       - last state: `ExecutionSucceeded`
       - external caller = port-view Fargate task / the `states:StartExecution` Resource of Task Role `portfolio-paper-view-task-role` = scoped to the approval state machine ARN (`[REDACTED_ARN]` placeholder / 0 plaintext records in this note)
   (2) Slack `DAILY_EXECUTION_SUCCESS` receipt
       - limited to the fact of receipt via this date's Slack notifier Lambda `portfolio-event-notifier` / 0 plaintext records of the message body
   (3) NO_TARGET safe termination
       - Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` NO_TARGET / 0 new `connector_order_request` / 0 broker order submission / 0 READY · REQUESTED `strategy_execution_order` / 0 active `connector_order_request`

### 2. 04 spec Scope Change Facts

 1) No change to the Step Functions structure: complete
   (1) 0 state machine ASL changes
       - 0 changes this date to the approval workflow `portfolio-paper-daily-step12-17-approval` ASL / the 2026-06-29 (3) boolean / numeric payload consistency maintained as-is
       - 0 changes this date to the general workflow `portfolio-paper-daily-step1-17-approval` ASL
   (2) 0 EventBridge Scheduler / Dispatcher Lambda changes
       - 08:00 KST `portfolio-paper-daily-step1-11-approval-0800-kst` ENABLED kept as-is
       - 09:01 KST `portfolio-paper-daily-step12-17-order-0901-kst` DISABLED kept as-is (OD-MS-033 09:01 automatic ENABLE on-hold policy consistency)
       - 0 changes this date to the 07:50 / 15:50 EC2 lifecycle Schedulers
   (3) 0 IAM Role / Policy changes
       - 0 changes this date to `portfolio-paper-stepfunctions-execution-role` / the Fargate Task Role-side IAM change is a 06 spec follow-up phase responsibility maintained as-is
 2) This date's cross-spec responsibility separation: complete
   (1) 05 spec — port-view ECS Fargate first porting / Public IP direct access / SG inbound / CloudWatch Logs retention / ECS task definition `portfolio-view:2` / ECS service `portfolio-view-service` / desiredCount 0 termination
   (2) 06 spec — port-view ECS task role (`portfolio-paper-view-task-role`) + execution role (`portfolio-paper-ecs-task-execution-role`) separation validation
   (3) 04 spec — this note (limited to a cross-reference of the fact that the external caller expanded to ECS View / 0 changes to Step Functions itself)

### 3. Decision / Risk Mapping

 1) Decision body unchanged
   (1) OD-MS-009 (Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) evidence reinforced without body change
       - evidence of the third-phase first demonstration of ECS View attaching as a Step Functions `StartExecution` external caller
   (2) OD-MS-032 (EventBridge Scheduler + Dispatcher Lambda) / OD-MS-033 (Step 12 retry-normalizer + 09:01 on-hold) / OD-MS-034 (EC2 lifecycle automatic execution) body unchanged
   (3) OD-SAFE-001 ~ OD-SAFE-004 (automatic BUY · SELL E2E phased introduction / automatic retry prohibited idempotent only) body unchanged
       - the Step 12~17 order-related segment is active only after explicit operator approval / NO_TARGET safe termination / 0 broker order submission
 2) Risk mapping
   (1) R-AUTO-033 [2026-06-30 afternoon reinforcement] — ECS View → Step 12~17 approval third demonstration pass (Local View → Step 1~11 / Local View → Step 12~17 approval / ECS View → Step 12~17 approval) / Status `Mitigated` maintained
   (2) R-AUTO-034 [2026-06-30 afternoon reinforcement] — first demonstration that the Fargate Task Role `states:StartExecution` permission is scoped to the Step 12~17 approval state machine ARN / Status `Open` maintained (promote to `Mitigated` at the point when both the general + approval ARN are scoped + the default ENABLE regression audit at the Fargate external exposure point passes / 06 spec follow-up phase responsibility)

### 4. This Date's Fact Record Scope

- 0 Kiro-side execution / changes:
  - 0 changes on the 04 spec side to AWS / EventBridge Scheduler / Lambda / Step Functions / SSM / EC2 / IAM / RDS / S3 / KIS API calls.
  - 0 AWS CLI / boto3 / psql / Spring Boot execution / external API calls.
  - 0 changes on the 04 spec side to AWS resource new creation · modification · deletion.
- broker / KIS call scope:
  - morning Step 1~11 automatic trigger + Local View → Step 12~17 approval execution (2026-06-30 morning).
  - 1 ECS View → Step 12~17 approval execution (`SUCCEEDED` / NO_TARGET / 0 broker order submission).
  - balance refresh only / 0 additional BUY · SELL · cancel · modify.
  - 0 fill · position sync automatic retry / 0 aws-live work.
- 0 plaintext records of sensitive information — all `[REDACTED]` / `[REDACTED_ARN]` / `[REDACTED_ACCOUNT_NO]` placeholder:
  - secret value / KIS app key · KIS app secret / raw 12-digit account number / RDS password / RDS endpoint hostname / raw 12-digit account-id.
  - actual IAM Role · secret · state machine ARN / IAM access key id / instance-id / public IP.
  - image digest full sha256 / task ARN / ENI ID / raw broker_order_no.
  - Slack webhook URL / DB password / Administrator password.
- Operational identifiers (fact record consistent with the user-specified policy / not secrets):
  - executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / state machine `portfolio-paper-daily-step12-17-approval` / status `SUCCEEDED`.
  - start · stop timestamp / Slack event `DAILY_EXECUTION_SUCCESS`.
  - Task Role `portfolio-paper-view-task-role` / Task Execution Role `portfolio-paper-ecs-task-execution-role` / state name `ExecutionSucceeded`.


## 2026-06-30 (afternoon) — Approval Required Slack Builder Integration Complete

### Summary

- Separate from the same date's afternoon port-view ECS Fargate first porting cross-reference (§ above section).
- target = Slack wording improvement on the Daily Batch state machine side (`BuildApprovalSlackPayload → SendApprovalRequiredSlack`).
- Accumulated from the perspective of this spec's Step Functions state machine operation.
- 0 plaintext quotations (R-DOCS-001 consistency) — Lambda code / Step Functions ASL / Slack message / Builder output / Notifier input / CloudWatch Logs / IAM Policy body.

### 1. Approval Required Builder Integration First Demonstration

 1) New Builder Lambda: complete
   (1) Lambda name `portfolio-approval-slack-summary-builder` (newly created directly by the operator)
       - responsibility = RDS read + Daily Batch strategy status summary + Notifier Slack input payload creation
       - call target = the `BuildApprovalSlackPayload` state of the `portfolio-paper-daily-step1-17-approval` ASL
       - 0 plaintext records of the actual Lambda code body / IAM Role ARN (`[REDACTED_ARN]` placeholder)
 2) state machine ASL update: complete
   (1) success path change of `portfolio-paper-daily-step1-17-approval`
       - `StopCrawlerEc2AfterStep11Success → BuildApprovalSlackPayload → SendApprovalRequiredSlack → Step12_CheckApproval`
       - operator direct update + confirmed `BuildApprovalSlackPayload` reflected in the deployment definition complete
       - revisionId `da8642c6-8409-41b6-ad57-e066ff672332`
       - State Machine `ACTIVE` maintained / OD-MS-029 (false / true path operation procedure) body unchanged
   (2) 0 plaintext quotations of the ASL body / Catch path body / actual state machine ARN (R-DOCS-001 consistency)
 3) Slack smoke pass: complete
   (1) Builder output → Notifier Lambda (`portfolio-event-notifier`) Slack smoke success
       - eventType `APPROVAL_REQUIRED`
       - Slack attachment color `#ECB22E`
       - `marketStatusCode=BLOCK`
       - `marketStatusLabel=차단`
       - block reason: `시장 수급 압력이 약해서 신규 매수를 차단했음`
       - Daily buy signal: `신호 0 / 후보 0`
       - Daily position decision: `없음`
       - confirmed display of no buy candidate + no sell candidate
   (2) 0 plaintext quotations of the Slack message body

### 2. This Date's 04 spec Scope Change Facts

 1) Step Functions structure change: partial
   (1) only the `portfolio-paper-daily-step1-17-approval` ASL adds the `BuildApprovalSlackPayload` state (operator direct update / revisionId updated)
   (2) 0 changes this date to the `portfolio-paper-daily-step12-17-approval` ASL
   (3) 0 changes this date to the `portfolio-paper-intraday-stop-sell-approval` ASL
 2) IAM Role / Policy change: partial
   (1) the Approval Builder Lambda's IAM Role is scoped to RDS read + the Step Functions-side `lambda:InvokeFunction` mapping (operator direct authoring / 0 plaintext records of the IAM Policy body)
   (2) the existing `portfolio-paper-stepfunctions-execution-role`'s Resource pattern is scoped to the Builder Lambda ARN + Resource · Action wildcard 0 maintained (06 spec follow-up phase responsibility)
 3) EventBridge Scheduler change: none
   (1) 0 changes this date to the 08:00 / 09:01 2 Schedulers (OD-MS-032 / OD-MS-033 consistency)
   (2) 0 changes this date to the 07:50 / 15:50 2 EC2 lifecycle Schedulers (OD-MS-034 consistency)

### 3. Decision / Risk Mapping

 1) Decision body unchanged
   (1) OD-MS-009 (Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) evidence reinforced without body change
       - only a Slack notification builder added inside the Daily Batch state machine (no change to the state machine's own responsibility)
   (2) OD-MS-029 (Step Functions approval workflow false / true path operation procedure) evidence reinforced without body change
       - the `allowPaperOrderExecute` input policy maintained as-is / adding the Slack notification builder has no effect on the entry policy
   (3) OD-MS-030 (AWS common Slack notifier Lambda) evidence reinforced without body change
       - operation scope expanded this date to Approval Required + Daily Brief
   (4) OD-MS-031 (first Slack integration scope limited to 3 kinds) evidence reinforced without body change
       - applied rich messages of the 3 kinds (`APPROVAL_REQUIRED` / `DAILY_EXECUTION_SUCCESS` / `DAILY_EXECUTION_FAILED`)
       - pre-market balance + post-market balance are separated into a separate mini workflow as OD-MS-038 new
   (5) OD-SAFE-001 ~ OD-SAFE-004 body unchanged — 0 impact on broker order / automatic retry / aws-live policy
 2) Risk mapping
   (1) R-AUTO-023 (risk of Slack omission on the Step Functions failure path) mitigation maintained as-is
   (2) R-AUTO-024 (risk of Slack webhook URL plaintext exposure / `Accepted`) mitigation maintained as-is
   (3) R-AUTO-027 (risk of the `APPROVAL_REQUIRED` Slack summary 0/0 display / `Accepted`) — if rich messages are provided via this date's Builder integration, separately audit whether the 0/0 pattern regresses in future Step1~11 actual executions / the 09:01 schedule automatic ENABLE on-hold policy (OD-MS-033) maintained as-is

### 4. This Date's Fact Record Scope

- This date's Kiro work = accumulated only this section of the 04 spec `operation-notes.md`
- Operator direct performance area = Builder Lambda new creation + `portfolio-paper-daily-step1-17-approval` ASL update + deployment definition confirmation + Slack smoke confirmation + IAM Role / Policy change
- 0 Kiro-side changes this date to AWS CLI / boto3 / psql / Lambda execution / Step Functions execution / Slack webhook / KIS API calls / 0 Kiro-side changes this date to AWS resource new creation · modification · deletion
- 0 plaintext quotations of Lambda code body / Step Functions ASL body / Slack message body / full Builder output / full Notifier input / full CloudWatch Logs / full IAM Policy body / Slack webhook URL / actual IAM Role ARN / actual state machine ARN / raw 12-digit account number / DB password (R-DOCS-001 consistency)
- Operational identifiers (fact record consistent with the user-specified policy / not secrets):
  - Builder Lambda name `portfolio-approval-slack-summary-builder` / Notifier Lambda name `portfolio-event-notifier`.
  - state machine name `portfolio-paper-daily-step1-17-approval`.
  - state names = `StopCrawlerEc2AfterStep11Success` · `BuildApprovalSlackPayload` · `SendApprovalRequiredSlack` · `Step12_CheckApproval`.
  - revisionId `da8642c6-8409-41b6-ad57-e066ff672332`.
  - Slack event label `APPROVAL_REQUIRED` / Slack color `#ECB22E`.
  - `marketStatusCode` · `marketStatusLabel` / block reason text (market supply-demand pressure block reason).
  - Daily buy signal `0/0` / Daily position decision `없음`.

### 5. Follow-up

 1) Confirmation of automatic `APPROVAL_REQUIRED` Slack receipt on the next actual Step1~11 execution: follow-up
   (1) audit whether rich messages are automatically received in the Slack channel after the first actual call of `portfolio-paper-daily-step1-11-approval-0800-kst` (ENABLED)
   (2) audit reflection of real data in the Builder output's `marketStatusCode` / buy·sell candidates
   (3) audit 0 RDS read failure / exception patterns in the Approval Builder Lambda CloudWatch Logs
 2) Enhancement of the `DAILY_EXECUTION_SUCCESS` order / fill / balance refresh summary: follow-up
 3) `DAILY_EXECUTION_FAILED` cause truncation policy: follow-up
 4) `INTRADAY_STOP_LOSS` actual intraday position event integration: follow-up (OD-MS-035 / OD-MS-036 / R-AUTO-030 ~ R-AUTO-032 / R-BROKER-005 consistency follow-up phase responsibility)


## 2026-06-30 (afternoon) — Intraday stop-loss Slack Live Integration cross-reference

### Summary

- Responsibility boundary (unchanged):
  - `INTRADAY_STOP_LOSS` Slack send = MarketConnector EC2 runner responsibility.
  - broker order submission = `portfolio-paper-intraday-stop-sell-approval` Step Functions approval gate follow-up responsibility.
- **0 changes to the 04 spec scope** — no changes to state machine ASL · IAM Role · EventBridge Scheduler · Dispatcher Lambda.
- The operator's direct performance area is 03 spec (MarketConnector EC2) / 06 spec (IAM permissions).
- This note has 0 plaintext quotations (R-DOCS-001 consistency):
  - Lambda code body / Step Functions ASL body / SSM response body / IAM Policy body.
  - `connector_intraday_position_evaluate.py` body / runner ps1 body / Slack message body.

### 1. Intraday stop-loss Responsibility Separation cross-reference

 1) `INTRADAY_STOP_LOSS` Slack send responsibility: MarketConnector EC2 runner
   (1) actual flow
       - the intraday runner `/home/ec2-user/apps/port-marketconnector/scripts/run_intraday_snapshot_and_evaluate.sh` (SHA256 `8fe7657a75b5d7637ec645b6d8a55bf75c993d46c1c71e8a5e88bded29baa8a0`) performs snapshot refresh + position evaluate
       - `connector_intraday_position_evaluate.py` (version `connector-intraday-position-evaluate-1.1.1-slack-notify` / SHA256 `5ec6914853ab34e200682f256de53693f972b3e5d337a5bd8ab8ebf5296230ed`) creates a `strategy_execution_order` `INTRADAY_STOP_SELL` `READY` + invokes the Notifier Lambda `portfolio-event-notifier` when the hard stop condition is met
       - the `INTRADAY_STOP_LOSS` Slack send is the MarketConnector EC2 side's direct responsibility / not the Step Functions state machine side's responsibility
   (2) Slack failure policy
       - the READY creation itself is not rolled back and a warning is output
       - broker order submission is still impossible in this flow
       - R-AUTO-036 new / Status `Mitigated`
 2) broker order submission responsibility: `portfolio-paper-intraday-stop-sell-approval` state machine
   (1) approval gate responsibility separation maintained as-is
       - broker order submission possible only after passing the `CheckIntradayStopApproval` Choice `allowIntradayStopOrderExecute=true`
       - 0 state machine ASL changes due to this date's afternoon additional work
       - state machine automatic ENABLE entry is still follow-up (OD-MS-036 / R-AUTO-030 ~ R-AUTO-032 / R-BROKER-005 consistency maintained as-is)
   (2) actual usage validation facts
       - in this date's afternoon actual runner 1 safe validation (SSM commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131`), in the `open_position_count=0` / `EMPTY_NORMAL` state, 0 READY creation + 0 Slack send + 0 state machine calls
       - whether the state machine entry + broker order submission + Slack `DAILY_EXECUTION_SUCCESS` / `DAILY_EXECUTION_FAILED` send occurs in a round where an actual held stock's hard stop condition is met is a follow-up phase responsibility

### 2. 04 spec Scope Change Facts

 1) No change to the Step Functions state machine: complete
   (1) 0 changes to `portfolio-paper-daily-step1-17-approval`
   (2) 0 changes to `portfolio-paper-daily-step12-17-approval`
   (3) 0 changes to `portfolio-paper-intraday-stop-sell-approval`
   (4) 0 changes to `portfolio-daily-brief-slack-notification`
 2) No change to EventBridge Scheduler / Dispatcher Lambda: complete
   (1) 0 changes to all of the 08:00 / 09:01 / 07:50 / 15:50 / 10-minute Snapshot Refresh Schedulers
   (2) 0 changes to the 3 Dispatcher Lambdas
 3) No change to IAM Role / Policy: complete
   (1) 0 changes this date to the Step Functions execution role / EventBridge Scheduler role
   (2) MarketConnector EC2 IAM inline policy grant is a 06 spec responsibility area

### 3. Decision / Risk Mapping

 1) Decision body unchanged
   (1) OD-MS-009 / OD-MS-030 / OD-MS-035 / OD-MS-036 / OD-MS-038 evidence reinforced without body change
   (2) see `../_common/operator-decisions.md` Change Log `2026-06-30 (오후) 장중 손절 Slack` item for detailed decision changes
   (3) no new decision / no Decision Summary count change (total 97 / 확정 52 / 잠정 42 maintained)
 2) Risk mapping
   (1) R-AUTO-036 new / Status `Mitigated` (03 / 04 / 06 / 10 spec affected)
   (2) R-AUTO-035 [2026-06-30 afternoon additional reinforcement] / Status `Mitigated` maintained as-is
   (3) R-AUTO-024 / Status `Accepted` mitigation maintained as-is
   (4) R-AUTO-030 / R-AUTO-031 / R-AUTO-032 / R-BROKER-005 mitigation maintained as-is — the `portfolio-paper-intraday-stop-sell-approval` state machine automatic ENABLE entry is still follow-up

### 4. Follow-up

 1) cross-reference integration audit of `INTRADAY_STOP_LOSS` Slack live event receipt + state machine entry when a hard stop condition is met after an actual held stock occurs
 2) review whether to add a separate approval summary Slack after intraday stop-loss READY creation (a separate summary Slack similar to `APPROVAL_REQUIRED`)
 3) `portfolio-paper-intraday-stop-sell-approval` state machine automatic ENABLE entry follow-up after separate operator approval

### 5. This Date's Fact Record Scope

- This date's Kiro work = accumulated only this section's cross-reference of the 04 spec `operation-notes.md`
- Operator direct performance area = MarketConnector EC2 side (03 spec responsibility) + IAM permission grant (06 spec responsibility)
- 0 Kiro-side changes this date to AWS CLI / boto3 / psql / Lambda execution / Step Functions execution / SSM RunCommand / external API calls / 0 Kiro-side changes this date to AWS resource new creation · modification · deletion
- 0 plaintext quotations of Lambda code body / Step Functions ASL body / SSM response body / full IAM Policy body / `connector_intraday_position_evaluate.py` body / runner ps1 body / Slack message body (R-DOCS-001 consistency)
- 0 plaintext records in this note of sensitive information (secret value / KIS app key / KIS app secret / raw 12-digit account number / RDS password / RDS endpoint hostname / raw 12-digit account-id / actual IAM Role ARN / actual secret ARN / IAM access key id / actual state machine ARN / Slack webhook URL / DB password / Administrator password / raw broker_order_no) — all `[REDACTED]` or placeholder
- Operational identifiers (4 state machine names / Lambda name / SSM commandId / runner SHA256 / evaluate deployment SHA256 / source_version / Slack event label / marker label / IAM Role name / inline policy name) are fact-recorded only consistent with the user-specified policy — not secrets


## 2026-07-01 — paper Daily Step 12~17 Automatic Execution ENABLED

As of 2026-07-01, aws-paper Daily automation reached the first full-ON state. Both the Step 1~11 Scheduler and the Step 12~17 Scheduler are ENABLED, and when there are candidates, the 09:01 KST Step 12~17 path proceeds automatically without View manual approval.

### 1) Step 12~17 Manual Execution SUCCEEDED (this date's pass evidence)

- Fact record of 1 Step 12~17 manual execution pass the operator directly performed.
  - executionName `port-manual-daily-step12-17-20260701-043747`
  - state machine `portfolio-paper-daily-step12-17-approval`
  - status `SUCCEEDED`
  - start `2026-07-01T13:37:47.856+09:00`
  - stop `2026-07-01T13:40:41.212+09:00`
  - execution history `ExecutionSucceeded`
  - 3 Slack kinds received — 07:50 pre-market Slack / 08:24 approval-required Slack / Step 12~17 success Slack
- Consistency of the flow inside the Step 12~17 approval workflow: after passing the `Step12_CheckApproval` Choice, `Step12_RunMarketConnectorStrategyOrderExecute` → `Step12_GetCommandInvocation` → Step 13~17 sequential progression → `ExecutionSucceeded`.
- This date's no-candidate state (NO_TARGET) — with no REQUESTED strategy order existing, Step 12 terminates safely with the `[NO_TARGET] REQUESTED strategy order 없음` response / Step 13~17 perform only status query · fill sync · balance refresh with no new broker order.

### 2) DB pre-check / after-check Pass

- 2026-07-01 `strategy_execution_order` 0 / REQUESTED strategy orders 0.
- active `connector_order_request` 0 / today's `connector_order_request` 0.
- latest `connector_balance_snapshot id=321` / `as_of_date=2026-07-01` / `total_eval_amount=8,706,505` / `cash_balance=8,706,505` / `eval_profit=0`.
- stale `connector_position_snapshot` (4 remaining from 2026-06-23) confirmed as a stale snapshot separated from the latest balance base date 2026-07-01 / the judgment is a full-cash / NO_TARGET safe termination judgment based on the latest balance + 0 orders that day.
- follow-up cleanup candidate — cleaning up the stale `connector_position_snapshot` or supplementing the latest-balance-based judgment query (followups-overview 2026-07-01 follow-up memo consistency).

### 3) Step 12~17 Scheduler ENABLE Transition

- Scheduler `portfolio-paper-daily-step12-17-order-0901-kst` DISABLED → ENABLED transition complete.
  - LastModificationDate `2026-07-01T13:53:57.160+09:00`
  - ScheduleExpression `cron(1 9 ? * MON-FRI *)`
  - Timezone `Asia/Seoul`
  - FlexibleTimeWindow `OFF`
  - Target Lambda `portfolio-paper-daily-scheduler-dispatcher`
  - Target Input `{"scheduleType":"STEP12_17_ORDER","dryRun":false}`
  - State `ENABLED`
- 09:01 KST automatic execution triggers the Step 12~17 approval workflow (`portfolio-paper-daily-step12-17-approval`) StartExecution without View manual approval.
- The promotion condition in the OD-MS-033 (Step 12 retry-normalizer + 09:01 on-hold) decision body (ENABLE entry after separate operator judgment) passed this date → proceed only with evidence reinforcement without body change (operator-decisions.md Change Log 2026-07-01 item).
- No change to the Step Functions structure itself — 0 changes this date to the `portfolio-paper-daily-step12-17-approval` state machine ASL / IAM execution role / Retry policy / R-AUTO-001 [2026-07-01 reinforcement] consistency (BUY / SELL / fill sync / position change step Retry deactivation maintained).

### 4) Full Daily Schedule Lineup 7-kind ENABLED Confirmation

- 07:50 EC2 start — `portfolio-paper-ec2-start-0750-kst` (cron `cron(50 7 ? * MON-FRI *)` / Asia/Seoul / MarketConnector · Crawler EC2 start / OD-MS-034 consistency).
- 07:50 pre-market Slack — `portfolio-daily-brief-morning-slack-0750-kst` (cron `cron(50 7 ? * MON-FRI *)` / Asia/Seoul / eventType `MORNING_BRIEF` / OD-MS-038 consistency).
- 08:00 Step 1~11 — `portfolio-paper-daily-step1-11-approval-0800-kst` (cron `cron(0 8 ? * MON-FRI *)` / Asia/Seoul / Target Input `{"scheduleType":"STEP1_11_APPROVAL","dryRun":false}` / OD-MS-032 consistency).
- **09:01 Step 12~17 — `portfolio-paper-daily-step12-17-order-0901-kst` (cron `cron(1 9 ? * MON-FRI *)` / Asia/Seoul / Target Input `{"scheduleType":"STEP12_17_ORDER","dryRun":false}` / ENABLED this date).**
- 09:10~15:50 10-minute intraday stop-loss — `portfolio-paper-intraday-snapshot-evaluate-10min-kst` (cron `cron(10/10 9-15 ? * MON-FRI *)` / Asia/Seoul / OD-MS-035 consistency).
- 15:50 post-market Slack — `portfolio-daily-brief-evening-slack-1550-kst` (cron `cron(50 15 ? * MON-FRI *)` / Asia/Seoul / eventType `EVENING_BRIEF` / OD-MS-038 consistency).
- 15:50 MarketConnector stop — `portfolio-paper-marketconnector-stop-1550-kst` (cron `cron(50 15 ? * MON-FRI *)` / Asia/Seoul / OD-MS-034 consistency).

### 5) Decision / Risk Change Summary

- No new decision / no Decision Summary count change (total 97 / 확정 52 / 잠정 42 maintained).
- The decisions below are evidence-reinforced without body change:
  - OD-SAFE-001 (paper automatic BUY / SELL E2E initial block → allow after validation).
  - OD-SAFE-002 (live automatic BUY policy candidate + manual approval priority).
  - OD-SAFE-003 (live automatic SELL policy candidate + manual approval priority).
  - OD-MS-009 (Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask).
  - OD-MS-032 (EventBridge Scheduler + Dispatcher Lambda).
  - OD-MS-033 (Step 12 retry-normalizer + 09:01 on-hold).
- [2026-07-01 reinforcement] added to R-AUTO-001 mitigation — the automatic retry prohibition policy is maintained even after the Step 12~17 Scheduler is ENABLED.
- [2026-07-01 automatic ENABLE entry] added to R-AUTO-025 mitigation — the 09:01 schedule on-hold policy passed ENABLE entry after operator approval / Status `Mitigated` maintained as-is.
- **R-AUTO-037 new** — risk that Step 12~17 automatic execution runs even in a Step 1~11 failure or data-unready state / Impact `High` / Probability `Low` / Status `Mitigated`.

### 6) Safety Policy Reconfirmation

- **This change is limited to aws-paper. The aws-live automatic BUY / SELL policy is not changed, and live maintains the candidate + manual approval priority policy.** (OD-SAFE-002 / OD-SAFE-003 consistency)
- The automatic retry prohibition policy for the BUY / SELL / fill sync / position change / intraday stop SELL creation steps is maintained (OD-SAFE-004 / R-AUTO-001 consistency).
- The Step 12 retry-normalizer (built into the start of Step 12 of `connector_strategy_order_execute.py` / OD-MS-028) is confirmed to be consistent with the re-submission path only when a stale REQUESTED exists after a market-close REJECTED · `40580000` / 0 retry candidates in this date's round.

### 7) Follow-up

1. Live observation of the next business day's 09:01 automatic execution — Scheduler invocation log · Dispatcher Lambda CloudWatch Logs · Step Functions execution creation · Slack receipt · DB after-check consistency audit.
2. Automatic order submission · fill · balance refresh · Slack confirmation on a day with candidates — this date has no candidates / the first demonstration of the end-to-end broker order → `connector_order_request` ACCEPTED → `connector_fill` reflection → `connector_balance_snapshot` refresh → `DAILY_EXECUTION_SUCCESS` Slack in an actual-candidate round.
3. Cleaning up the stale `connector_position_snapshot` or supplementing the latest-balance-based judgment query.
4. cleanup of the 2026-04-27 Samsung Electronics stale ACCEPTED `connector_order_request` 6 (03 · 04 spec follow-up phase responsibility maintained as-is).
5. Re-review the aws-live automation policy in a separate cutover phase (10 spec follow-up phase responsibility).

### 8) Fact Record Scope (this date's Kiro side)

0 Kiro-side execution / changes:

- 0 AWS CLI / boto3 / psql / Lambda execution / Step Functions execution / SSM RunCommand / Slack webhook / KIS API calls.
- 0 AWS resource new creation · modification · deletion.
- 0 broker order submission / 0 new `connector_order_request` · `connector_fill` · `strategy_execution_order` / 0 aws-live work.
- 0 commit / add / reset / checkout / stash.
- 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging.

Operator direct performance area:

- Step 12~17 Scheduler `update-schedule --state ENABLED`.
- Step 12~17 manual execution `start-execution`.
- DB after-check SELECT.

0 plaintext records of sensitive information — all `[REDACTED]`-family placeholder:

- secret value / KIS app key · app secret / raw 12-digit account number / account password / token.
- RDS password / RDS endpoint hostname / raw 12-digit account-id.
- actual IAM Role ARN / actual secret ARN / IAM access key id / instance-id / EIP / public IP.
- image digest full sha256 / task ARN / ENI ID / raw broker_order_no.
- Slack webhook URL / DB password / actual state machine ARN / actual Lambda ARN.

Operational identifiers (fact record consistent with the user-specified policy):

- Scheduler name · cron · Asia/Seoul · Target Input · Dispatcher Lambda name · state machine name.
- executionName · status · start · stop timestamp · Slack event label.
- balance snapshot id · as_of_date · amount · count · 7-kind lineup names.


## 2026-07-15 (afternoon) — Daily BUY KST Date Misjudgment Resolution + Approval Slack Data Consistency and Candidate Display Improvement

Result the operator directly performed on 2026-07-15 afternoon. This note records two different faults together. (a) The cause of the 4 buy candidates on 2026-07-13 (Mon) not being executed was that the execution container's business date judgment was UTC-based, while the Daily strategy calculation itself was normal. (b) As a separate matter, corrected the problem where the Approval Required Slack was mixing and displaying different Daily Runs and Execution Plans and exposing the candidate stock payload truncated in raw form. This is distinguished as a fault different from the Daily Brief Slack non-send recovery (see the 2026-07-15 daily-brief-slack-recovery section at the top of this document · WORKLOG 2026-07-15).

This note contains only the Kiro document record; the actual code · Lambda deployment · ECS Task Definition update · Market EC2 timezone change · Step Functions execution path connection · DB validation were all performed directly by the operator. Kiro performed 0 AWS · Lambda · ECS · EC2 · Step Functions · Scheduler · IAM · DB · Slack · broker · KIS executions. 0 plaintext records in this note of Lambda code body · IAM Policy body · SQL raw output · Slack payload · actual ARN · executionArn · RequestId · SHA256 · webhook URL · account-id · broker_order_no · raw account number · all `[REDACTED_*]`-family placeholders.

### 1) Symptoms

Two different issues were identified on the same date.

- (A) Daily BUY non-execution — even though 2026-07-13 (Mon) `daily_run_id=73` was calculated normally and 4 BUY signals · 4 candidates were created, the Execution Plan and Execution Order were not created, so the 4 buy candidates did not make it onto the execution target. Step Functions terminated as SUCCESS, making automatic detection difficult.
- (B) Approval Slack data mixing — in the Approval Required Slack message, different Daily Runs and Execution Plans were combined, displaying a status/base-date/signal/candidate combination that does not actually exist in the DB. The candidate stock name was displayed as `-`, and the `buy_info` raw dictionary was rendered in an exposed·truncated state, lowering operational readability.

### 2) Cause

**(A) Daily BUY non-execution · business date UTC misjudgment**

| Item | Value |
| --- | --- |
| actual execution time | 2026-07-13 08:00 KST |
| execution container UTC time | 2026-07-12 23:00 UTC |
| business date calculation function | `date.today()` · `datetime.today()` (naive) |
| container internal computed result | 2026-07-12 (Sun) |
| Step8 result | `WEEKEND / NO_TARGET` |
| Execution Plan creation | none |
| Execution Order creation | none |
| Container ExitCode | 0 |
| Step Functions overall | SUCCESS |

**(B) Approval Slack data mixing · independent latest query**

| Item | Value |
| --- | --- |
| Builder latest query (Plan) | `latestPlanId=133` · `latestPlanDate=2026-07-09` |
| Builder latest query (Run) | `latestDailyRunId=73` |
| combined result (displayed) | status `DEFENSIVE` · base date `2026-07-09` · 4 signals · 4 candidates |
| actual DB combination existence | none |
| candidate name rendering | `-` |
| score display | `buy_info` raw dict + truncation |

### 3) Actual DB Result (Daily strategy calculation normal)

| Item | Value |
| --- | --- |
| `daily_run_id` | 73 |
| `run_date` (execution-time KST date) | 2026-07-12 |
| `data_date` (market-based data date) | 2026-07-10 |
| `market_signal` | AGGRESSIVE |
| BUY signals | 4 |
| candidates | 4 |
| candidate stocks | DL · 대주전자재료 · 삼성SDI · 한국피아이엠 |

The Daily strategy calculation was normal. `Execution Plan / Order non-creation` is a separate defect that occurred later at the execution container business date misjudgment stage.

### 4) KST Date-basis Fix

Principle: keep the DB timestamp storage basis as UTC, and unify the business date judgment on an `Asia/Seoul` basis.

| Target | Action |
| --- | --- |
| Daily BUY / SELL execution-related ECS Task Definition | added `TZ=Asia/Seoul` environment variable |
| Market EC2 server timezone | changed to Korea Standard Time |
| related execution scripts | supplemented to use the same KST business date on local and ECS |
| Step Functions execution path | connected the new ECS Task Definition revision to the actual execution path |
| DB session timezone | kept as UTC |

R-DATA-010 [2026-07-15 reinforcement] · OD-MS-040 [2026-07-15 reinforcement] consistency.

### 5) Approval Slack Single Daily Run Query Structure

Fixed the Builder query basis to a single `daily_run_id` and removed the independent latest query.

| Item | After change |
| --- | --- |
| query basis | first fix a single `daily_run_id` |
| status · base date · signal count · candidate count | queried based on the same Daily Run |
| independent latest query | removed the method of selecting Plan · Run each separately |
| signal query | Daily Signal query based on the Daily Run |
| Order query | query only Execution Orders connected via `source_daily_run_id` |
| Plan query | use only the Execution Plan the relevant Order references |
| when the connected Plan is absent | handled as `latestPlanId=null` · does not use an arbitrary past Plan |
| message status · base date | use the selected Daily Run's `market_signal` · `data_date` |
| signal count · candidate count | the same Daily Run value or the actual query count |
| buy candidate list | use only the Daily Signal of the same `daily_run_id` |

### 6) Candidate Display Format Improvement

| Item | Change |
| --- | --- |
| stock name | use the actual `company_name` or `ticker_code` |
| score display | the Builder structures the per-candidate score fields and passes them to the Notifier · the Notifier displays to three decimal places |
| raw `buy_info` dict exposure | removed |
| Korean labels | `final_score` → 종합 · `flow_score` → 수급 · `info_score` → 정보 · `tape_score` → 추세 · `short_score` → 공매도 |

Final format example (Slack render · actual values not recorded):

```
[매수 후보]
- DL
  · 종합 0.433 / 수급 0.636 / 정보 0.000 / 추세 0.034 / 공매도 0.066
```

### 7) Deployment Targets

| Lambda | Deployment status |
| --- | --- |
| `portfolio-approval-slack-summary-builder` | `Active` · `LastUpdateStatus=Successful` · Runtime · Handler unchanged |
| `portfolio-event-notifier` | `Active` · `LastUpdateStatus=Successful` · Runtime · Handler unchanged |

Previous-version rollback ZIP secured (operator direct performance · raw SHA256 not recorded).

### 8) Validation Result

Builder actual DB query result:

| Item | Value |
| --- | --- |
| status | AGGRESSIVE |
| base date | 2026-07-10 |
| signal count | 4 |
| candidate count | 4 |
| `latestPlanId` | null |
| candidate stocks | DL · 대주전자재료 · 삼성SDI · 한국피아이엠 |
| per-candidate fields | 종합 · 수급 · 정보 · 추세 · 공매도 normal |

`latestPlanId=null` means there is no Execution Order · Plan connected to the selected Daily Run, and is recorded as a validation result that no arbitrary past Plan was used.

Builder → Notifier → Slack E2E validation (test-execution-based, not an actual automatic order fill E2E):

| Item | Result |
| --- | --- |
| actual Slack message receipt | complete |
| status vs DB `market_signal` | match |
| base date vs DB `data_date` | match |
| signal count vs actual Signal count | match |
| candidate count vs actual candidate count | match |
| candidate stocks vs the same `daily_run_id` stock list | match |
| different Daily Run · past Plan mixing | none |
| candidate stock name normal display | confirmed |
| 5 scores Korean display | confirmed |
| raw dict exposure | removal confirmed |

### 9) Remaining Follow-up

- [ ] Live observation of whether the next business day's KST-based Daily BUY automatic execution recurs (Step Functions periodic round)
- [ ] Step Functions failure handling when there are READY candidates but no Plan / Order (failure propagation reinforcement)
- [ ] Reinforce the distinction between `NO_TARGET` and an actual success state
- [ ] Reinforce failure propagation so that internal order failures are not buried as ExitCode 0 and Step Functions SUCCESS
- [ ] Reinforce the order validation chain leading through Plan / Order / Connector Request / Signal Order Map / Broker acceptance / Fill / Position

See `_common/followups-overview.md` Now · `_common/risk-register.md` R-DATA-010 [2026-07-15 reinforcement] · R-DATA-017 [2026-07-15 reinforcement] · `_common/operator-decisions.md` OD-MS-031 [2026-07-15 reinforcement] · OD-MS-040 [2026-07-15 reinforcement] for related items.

### 10) Fact Record Scope (this date's Kiro side)

0 Kiro-side execution / changes:

- 0 AWS CLI · boto3 · psql · Lambda execution · Step Functions execution · SSM RunCommand · Slack webhook · KIS API · broker calls
- 0 AWS resource new creation · modification · deletion
- 0 Lambda deployment · ECS Task Definition update · EC2 timezone change · Step Functions execution path connection
- 0 broker order submission · fill sync · position sync · aws-live work
- 0 git add · commit · push executions
- 0 changes to the 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging

Operator direct performance area:

- Daily BUY / SELL execution-related ECS Task Definition new revision registration · `TZ=Asia/Seoul` addition
- Market EC2 server timezone Korea Standard Time change
- execution script KST business date computation supplementation
- connection of the new Task Definition revision to the Step Functions execution path
- Builder Lambda `portfolio-approval-slack-summary-builder` redeployment (single `daily_run_id` query · stock name · score field structuring)
- Notifier Lambda `portfolio-event-notifier` redeployment (Korean labels · score to three decimal places · raw dict not exposed)
- Builder → Notifier → Slack message display E2E validation

0 new records of sensitive information · actual identifier raw text — all subject to `[REDACTED_*]` placeholder use:

- secret value · KIS app key · KIS app secret · raw 12-digit account number
- RDS password · RDS endpoint hostname · raw 12-digit account-id
- actual IAM Role ARN · actual secret ARN · IAM access key id · instance-id · EIP · public IP
- image digest full sha256 · task ARN · ENI ID · raw broker_order_no
- Slack webhook URL · DB password · actual state machine ARN · actual Lambda ARN · executionArn · RequestId · SHA256 · Slack payload raw

Operational identifiers (fact record consistent with the user-specified policy):

- `daily_run_id=73` · `run_date=2026-07-12` · `data_date=2026-07-10` · `market_signal=AGGRESSIVE`
- 2 Lambda names · Deployment status · Runtime · Handler unchanged fact
- candidate stock names · 5 score labels (Korean · English key mapping)
