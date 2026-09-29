# Kiro AWS Migration Worklog

This document is a concise work log for the `.kiro` workspace. It is not written in the same level of detail as each MS's `docs/worklog/YYYY-MM-DD.md`; instead of creating a separate file for each date, entries are accumulated in this single file.

## Writing Principles

- Accumulate date-based sections in this single file.
- Each date should generally follow the order `Summary → Completed → Evidence → Risks → Follow-ups → Security`.
- Completed facts and validation results should be organized, where possible, in two-column `항목 / 값` tables.
- Split long cells across multiple rows, and move raw logs · full SQL · full AWS responses to `operation-notes.md`.
- Explicitly state who performed actual AWS execution, whether application changes were made, whether documents in the 8 MS repositories were modified, and whether sensitive information was recorded.

## 2026-07-22 (Paper Daily normal automated end-to-end run succeeded · P1 acceptance complete · Paper Daily first stabilization complete · decision not to perform P2 View enhancement)

### 🧭 Summary

In the normal Scheduler-driven run on 2026-07-22, both Paper Daily Step 1~11 and Step 12~17 completed as SUCCEEDED without forced orders or induced errors.
Crawler DB validation, Daily Run COMPLETED, Execution Plan READY, automatic passage of the READY Plan → Order validator, and automatic passage of the Order Chain validator during a real-order run were confirmed.
An 83-share 한국전력 order and fill, Position reflection, Balance Snapshot creation, automatic receipt of the success Slack, and OPS success records were all confirmed.
Based on this run, the existing P1 follow-up item—observing the next normal automated run end-to-end—was marked complete, and Paper Daily was declared to have completed its first stabilization phase. This does not mean that every operational risk has been resolved, aws-live is ready, long-term fault-free operation has been validated, or the entire AWS Migration is complete.
Separately, the operator decided not to perform P2 View operational-security and presentation enhancements (ALB · HTTPS · Route53 · authentication · Auto Scaling · Blue/Green · View display enhancement · public external operation) within the current portfolio scope. This is an intentional scope exclusion, not a failure or incomplete item, and should be reconsidered if external exposure or multi-user operation becomes necessary.
Kiro modified documentation only in this session; actual operational verification was performed by the operator. Actual AWS · DB · broker · KIS · Slack · ECS · EC2 · Lambda · Step Functions executions by Kiro: 0.

### ✅ Completed

- 🟢 Normal Scheduler execution of Step 1~11 (verified directly by operator)

| Item | Value |
| --- | --- |
| Scheduler status | ENABLED |
| Execution time | 2026-07-22 08:00 KST |
| Execution result | SUCCEEDED |
| Completion time | 2026-07-22 08:21 KST |
| Execution method | Normal run without forced execution or induced errors |

- 🟢 Crawler data validation

| Item | Value |
| --- | --- |
| Program latest trading date | 2026-07-21 |
| Program data | 1 row |
| Shortsell latest trading date | 2026-07-21 |
| Shortsell data | 349 rows |
| Validation criterion | Program exactly 1 row · Shortsell at least 300 rows |
| Result | Normal |

- 🟢 Daily Run · Execution Plan

| Item | Value |
| --- | --- |
| Daily Run date | 2026-07-22 |
| Data as-of date | 2026-07-21 |
| Daily Run status | COMPLETED |
| Market Signal | AGGRESSIVE |
| Candidate count | 1 |
| Signal count | 1 |
| Execution Plan status | READY |
| Order-ready targets | 1 |
| Blocked · skipped targets | 0 |

- 🟢 Normal Scheduler execution of Step 12~17 (verified directly by operator)

| Item | Value |
| --- | --- |
| Execution time | 2026-07-22 09:01 KST |
| Execution result | SUCCEEDED |
| Completion time | 2026-07-22 09:06 KST |
| Execution method | Normal Scheduler automated run |
| READY Plan → Order validator | Passed automatically |
| Order Chain validator | Passed automatically |

- 🟢 Order · Fill · Position · Balance after-check

| Item | Value |
| --- | --- |
| Security | 한국전력 (015760) |
| Order side | BUY |
| Order quantity | 83 shares |
| Execution Order | FILLED |
| Connector Order Request | FILLED |
| Fill quantity | 83 shares |
| Fill count | 1 |
| Position status | OPEN |
| Position quantity | 83 shares |
| Balance Snapshot | Created on 2026-07-22 |

- 🟢 Slack · OPS records

| Item | Value |
| --- | --- |
| Approval Slack | Sent normally through the automatic Step 1~11 path |
| Daily success Slack | Received normally through the automatic Step 12~17 path |
| Fill display | Confirmed display of 한국전력 83-share BUY fill |
| Workflow Step record | Success record confirmed |
| Batch record | Success record confirmed |

- 🟢 Completion judgment · declaration

| Item | Value |
| --- | --- |
| P1 acceptance | Observation of the next normal automated end-to-end run complete |
| Paper Daily | First stabilization declared complete |
| No over-interpretation | Does not mean all risks resolved · aws-live ready · long-term fault-free operation validated · entire Migration complete |

- 🟢 Decision not to perform P2 View operational-security · display enhancement (operator decision)

| Item | Value |
| --- | --- |
| P2 status | Not performed |
| Current scope | Retain first ECS Fargate proof state |
| Reason | Personal operation · portfolio demonstration purpose · not publicly exposed |
| Nature of decision | Intentional scope exclusion (not failure or incompletion) |
| Reconsideration condition | If external exposure or multi-user operation becomes necessary |

### 🔵 Evidence

| Item | Value |
| --- | --- |
| README | Current Status Dashboard Paper Daily Step 1~11 · Step 12~17 · port-view row |
| Follow-up | [followups-overview](specs/_common/followups-overview.md) Now → Done recently 2026-07-22 |
| Decision | [operator-decisions](specs/_common/operator-decisions.md) OD-SAFE-001 · OD-MS-002 |
| Detailed execution evidence | [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |

### ⚠️ Risks

| Item | Value |
| --- | --- |
| New Risk ID | None (Risk 76 · status counts preserved) |
| Status changes | None (successful normal run reconfirms existing mitigation; does not eliminate risk) |
| P2-related Risk | If risk scenario remains, keep Open · no arbitrary Mitigated/Closed change |

### 📌 Follow-ups

- [ ] Continue accumulating observations of long-term fault-free automated runs after first stabilization.
- [ ] Expand OPS Mirror detailed Steps · distinguish automated-run records from manually recovered-run records.
- [ ] Expand Slack exception-case validation (approval · success · failure · stop-loss paths).
- [ ] Reconsider View operational-security · display enhancement when P2 reconsideration conditions are met (external exposure · multi-user operation).

### 🔐 Security

| Item | Result |
| --- | --- |
| AWS · DB · broker · KIS · Slack · ECS · EC2 · Lambda · Step Functions commands | Kiro execution 0 · performed directly by operator |
| Newly recorded raw State Machine ARN · execution ARN · ECS Task ARN · account-id · account number · broker order number · SHA256 | 0 |
| git add · commit · push · reset · restore | 0 |
| Placeholder policy | Use only `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_BROKER_ORDER_NO]` families |
| Save encoding | UTF-8 No BOM retained |

## 2026-07-21 (final alignment reinforcement for common AWS Migration documents)

### 🧭 Summary

Residual inconsistencies across five `_common` common documents were corrected strictly from a document-alignment perspective. SSM VPC Endpoint descriptions were standardized to the actual operational structure (S3 Gateway + ECR/Secrets/Logs Interface baseline set · SSM as either Public outbound or optional Interface Endpoint), current Hybrid Crawler operation was clearly separated from the long-term target, and followups orchestration wording and dead references were cleaned up.
This work was document-alignment correction only and did not change actual AWS resources. Kiro modified documentation only; actual AWS · DB · broker · KIS · Slack executions by Kiro: 0.

### ✅ Completed

| Item | Value |
| --- | --- |
| operator-decisions.md | Aligned OD-NET-005 selected value to baseline set + optional SSM |
| operator-decisions.md | Aligned OD-SEC-007 SSM outbound path (Public or Endpoint) |
| operator-decisions.md | Removed dead `See details` tail references from OD-MS-027~040 |
| operator-decisions.md | Distinguished OD-MS-003 (target) from OD-MS-011 (current Hybrid) perspectives |
| risk-register.md | Aligned R-NET-003 risk · response to mismatch between access path and Endpoint set |
| aws-resource-glossary.md | Distinguished VPC Endpoint Gateway/Interface · optional SSM |
| ms-aws-service-decision-matrix.md | Aligned Network and Data · Crawler cards |
| followups-overview.md | Removed Historical Notes remnants · updated Daily orchestration wording |

### 🔵 Evidence

| Item | Value |
| --- | --- |
| Decision | [operator-decisions](specs/_common/operator-decisions.md) |
| Risk | [risk-register](specs/_common/risk-register.md) |
| Follow-up | [followups-overview](specs/_common/followups-overview.md) |

### ⚠️ Risks

| Item | Value |
| --- | --- |
| New Risk ID | None (Risk 76 · status counts preserved) |
| New Decision ID | None (Decision 100 · status counts preserved) |

### 📌 Follow-ups

- [ ] Operator to finally verify, from the actual network configuration, which outbound path allows SSM Session Manager to work normally in current aws-paper without an SSM Interface Endpoint.

### 🔐 Security

Documentation only; execution 0 · broker orders 0 · aws-live work 0 · raw secret records 0. SSM Endpoint policy changes were document-alignment corrections and did not change actual AWS resources. Git commands executed: 0. Confirmed UTF-8 No BOM · two-column table alignment · no lines over 300 characters.

## 2026-07-21 (normal no-order run validator failure · `--allow-no-target` reinforcement)

### 🧭 Summary

The State Machine `portfolio-paper-daily-step12-17-approval`, automatically executed at 09:01 on 2026-07-21, passed Step 12~16 and then terminated as FAILED at the final `P0_ValidateOrderChain`.
The failure was not caused by an actual order-chain mismatch or order-submission failure. It was a configuration omission in which the downstream Order Chain validator treated a normal no-order run—where the actual number of `strategy_execution_order` rows created for 2026-07-21 was 0—as a failure.
The local `execution_validate_order_chain.py` already supported successful no-order handling through `--allow-no-target`, but that argument was missing from the ECS command in the operational State Machine's `P0_ValidateOrderChain`.
The operator added `--allow-no-target` to the State Machine command, validated the ASL, deployed it, re-read the deployed definition, and confirmed successful no-order handling with a standalone ECS smoke of the validator. There were no Python-source changes, image changes, Task Definition changes, or full Step 12~17 reruns. Kiro modified documentation only; operational work was performed by the operator.

### ✅ Completed

- 🔴 09:01 Step 12~17 automatic-execution failure (verified directly by operator)

| Item | Value |
| --- | --- |
| State Machine | `portfolio-paper-daily-step12-17-approval` |
| Step 12~16 | All succeeded |
| Failed State | `P0_ValidateOrderChain` (final validator) |
| Final automatic-execution status | FAILED |
| ECS container exit | ExitCode 1 |
| Task Definition | `portfolio-paper-strategy-execution:3` |
| Execution image | `paper-20260720-p0-integrity-v1` |
| Failure Slack | `DAILY_EXECUTION_FAILED` received normally |

- 🟠 Confirmed failure cause (verified directly by operator)

| Item | Value |
| --- | --- |
| Actual orders created | `strategy_execution_order` 0 |
| Query result | signal_date_count=0 · created_kst_count=0 · current_validator_target_count=0 |
| Validator log | target_count=0 · error_count=0 · ORDER_CHAIN_VALIDATION=FAILED |
| reason | NO_EXECUTION_ORDER_TARGET |
| Direct cause | Downstream Order Chain validator treated a normal no-order run as failure |
| Missing configuration | Operational State Machine `P0_ValidateOrderChain` command did not pass `--allow-no-target` |

- 🟢 Correction and deployment (performed directly by operator)

| Item | Value |
| --- | --- |
| Before command | `... 'execution_validate_order_chain.py', '--run-date', $.runDate` |
| After command | `... 'execution_validate_order_chain.py', '--run-date', $.runDate, '--allow-no-target'` |
| Python source | No change |
| ECS image rebuild | None |
| Task Definition change | None |
| Existing definition backup | Local backup |
| Modified-definition encoding | UTF-8 No BOM |
| ASL validation | validate-state-machine-definition OK |
| Post-deployment verification | Re-read confirmed `--allow-no-target` applied |

- 🟢 Standalone validator smoke (performed directly by operator)

| Item | Value |
| --- | --- |
| Method | Standalone read-only validator ECS smoke |
| Smoke date | 2026-07-21 |
| Smoke result | ORDER_CHAIN_VALIDATION=SUCCESS target=0 errors=0 |
| Container | ExitCode 0 |
| Fail-closed retained | For runs with actual orders, continue Plan→Order→Request→Fill→Position validation |
| Full Step 12~17 rerun | None |
| Step 12 order-submission rerun | None |
| 09:01 automatic-execution history | FAILED preserved (not overwritten as success) |

### 🔵 Evidence

- 🔵 Raw State Machine ARN · execution ARN · ECS Task ARN · account-id · SHA256 are not pasted into this log. If needed, reference only via `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_ACCOUNT_NO]` placeholders.
- 🔵 Reinforced `_common/risk-register.md` R-AUTO-001 · R-AUTO-037 Mitigation history for 2026-07-21 · promoted R-DATA-017 to `Mitigated` · reinforced R-AUTO-020 · added 2026-07-21 operational-note Details to `_common/operator-decisions.md` OD-SAFE-004 · OD-MS-032 · OD-MS-026.
  Organized completed Next items in `_common/followups-overview.md` · updated Now P1 · added Done recently 2026-07-21 (validator · P3 crawler) · added short operational notes to `_common/ms-aws-service-decision-matrix.md` 4.3 · 4.7 and `_common/aws-resource-glossary.md` Usage Notes 2026-07-21.

### ⚠️ Risks

| Item | Value |
| --- | --- |
| Risk ID | R-AUTO-001 |
| Status change | 🔴 Open retained |
| Summary | Broker-order retries remain prohibited · only State Machine command modified · 0 duplicate broker orders from automatic retry |

| Item | Value |
| --- | --- |
| Risk ID | R-AUTO-037 |
| Status change | 🟢 Mitigated retained |
| Summary | Confirmed validator configuration omission for normal no-order run · added `--allow-no-target` · standalone smoke ExitCode 0 |

| Item | Value |
| --- | --- |
| Risk ID | R-DATA-017 |
| Status change | 🟢 Open → Mitigated promotion |
| Summary | KRX Python failure propagation + runner expected-trade-date · row-count validator · fail-closed · standalone validator Program 1 row · Shortsell 349 rows · ExitCode 0 |

| Item | Value |
| --- | --- |
| Risk ID | R-AUTO-020 |
| Status change | 🟢 Mitigated retained |
| Summary | Reinforced downstream DB validation in runner (Program 1 row · Shortsell at least 300 rows) |

### 📌 Follow-ups

- [ ] [P1] Reobserve full success path and automatic success-Slack entry in the next normal automated run (without forced orders or induced errors) — failure-propagation observation complete · full automatic Step 12~17 success acceptance after correction still unconfirmed.
- [ ] Strengthen distinction between `NO_TARGET` and actual success state (explicit success for normal no-order run · retain fail-closed behavior for runs with real orders).
- [ ] Observe crawler Python failure propagation and runner DB validator in a normal automated KRX run (standalone runner validator succeeded on 2026-07-21 · normal automated-run validation remains separate).

### 🧭 P3 — KRX Crawler failure-propagation reinforcement and DB validator:complete

This is separate work from the normal no-order validator correction above. Windows Scheduled Task `Portfolio-KRX-Worker-Daily` runs `C:\portfolio\run_krx_worker_daily.ps1`; the existing runner already failed when Python `$LASTEXITCODE` was non-zero, but `interest_program.py`
and `interest_shortsell.py` could finish with process exit 0 even after internal failure results, creating a false-success axis (R-DATA-017). The operator performed the work directly; Kiro modified documentation only.

- 🟢 Improved Python exit-code propagation (performed directly by operator)

| Item | Value |
| --- | --- |
| Target files | Only `interest_program.py` · `interest_shortsell.py` |
| exit 0 condition | `SUCCESS` · normal `NO_CHANGE` with `error_count=0` |
| exit 1 condition | `FAILED` · partial error · unknown status |
| Log | Added `PROCESS_EXIT_DECISION` |
| Not modified | Login · Chrome helper files |
| Validation | Local import · exit-code unit tests passed · operational venv py_compile · exit-code unit tests passed |

- 🟢 Added runner DB validator (performed directly by operator)

| Item | Value |
| --- | --- |
| Target | Local `ops/run_krx_worker_daily.ps1` |
| Validation criterion (1) | Latest trading date in `interest_program_raw` = expected · exactly 1 row |
| Validation criterion (2) | Latest trading date in `interest_shortsell_raw` = expected · at least 300 rows |
| Execution order | KRX login → program → shortsell → Validate crawler DB → DONE |
| Temporary validator file | Created under `$AppDir`, then deleted |
| Failure propagation | Validation failure → runner non-zero → SSM → Step Functions |
| Standalone execution result | expected trade date 2026-07-20 · Program 1 row · Shortsell 349 rows · `CRAWLER_DB_VALIDATION=SUCCESS` · ExitCode 0 |

- 🟢 Deployment · alignment (performed directly by operator)

| Item | Value |
| --- | --- |
| Sequence | Obtain local original → modify → validate → operational backup → deploy → remote validation |
| File alignment | Local/remote SHA256 matched · remote PowerShell parse passed |
| Actual rerun | No crawler recollection · no full State Machine rerun |
| Risk / Decision | R-DATA-017 `Open` → `Mitigated` promotion · R-AUTO-020 `Mitigated` retained · OD-MS-026 Details reinforced · no new IDs |

### 🔐 Security

| Item | Result |
| --- | --- |
| AWS · DB · broker · KIS · Slack · ECS · EC2 · Lambda · Step Functions · Scheduled Task commands | Kiro execution 0 · performed directly by operator |
| Newly recorded raw State Machine ARN · execution ARN · ECS Task ARN · account-id · instance-id · Command ID · broker order number · SHA256 | 0 |
| git add · commit · push · reset · restore | 0 |
| Placeholder policy | Use only `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_ACCOUNT_NO]` families |
| Save encoding | UTF-8 No BOM retained |

## 2026-07-20 (Step 13 EGW00201 recovery · rate-limit reinforcement · manual completion of Step 14~17 · State Machine failure Slack and runDate alignment)

### 🧭 Summary

The State Machine `portfolio-paper-daily-step12-17-approval`, automatically executed at 09:01 on 2026-07-20, failed at Step 13 (order/fill query). Step 12 order submission itself completed normally: two SELL orders were accepted by the broker and the first order was reflected as fully filled, but the second order query returned KIS `EGW00201` (transaction rate limit exceeded).
The existing `connector_order_check.py` had no dedicated retry for `EGW00201` and queried multiple orders consecutively with no wait between requests. Because Step 13 failed, Step 14~17 and the success Slack did not run automatically.
The operator directly deployed a query-API-only rate-limit patch (5-second wait between orders + up to 2 retries only for `EGW00201`) and, to avoid duplicate-order risk, did not rerun Step 12. Instead, Step 13 alone was executed manually, bringing both orders to `FILLED`.
The operator then manually executed Step 14~17 sequentially without creating a temporary State Machine. The success Slack was not an automatic result; it was received after manually invoking the Lambda once recovery was complete. The original 09:01 automated execution history remains FAILED to preserve the actual incident. The hardcoded balance runDate (`--run-date 2026-06-22`) in the State Machine was removed, and the failure-Slack bypass path for abnormal task results was also corrected.
As a separate follow-up unrelated to recovery, `DAILY_EXECUTION_SUCCESS` was enhanced to display the actual filled BUY/SELL securities and quantities for the day. A new `portfolio-daily-execution-slack-summary-builder` Lambda queries `connector.connector_fill`
and `reference.stock_master`, builds the payload, and was connected to the State Machine success path Builder → Notifier. A manual smoke using 2026-07-20 data, without starting the full execution, confirmed Slack receipt with 0 BUY fills and 2 SELL securities.
After this incident recovery, separate P0 safety hardening was performed: bounded polling for active Step 13 orders (`connector_order_check.py` 2.0.3), fail-closed consistency validation for Step14~16, integration of two new read-only validators (`execution_validate_ready_plan_order.py`
and `execution_validate_order_chain.py`) into the State Machine, immutable Execution image tag `paper-20260720-p0-integrity-v1`, Task Definition revision 3, `Step12_Failed` failure-Slack routing correction, and standalone smoke of the `DAILY_EXECUTION_FAILED` formatter.
However, this completion scope covers P0 implementation · operational deployment · standalone smoke · State Machine integration only. It does not mean the next normal automated run succeeded. The full automated acceptance test remains unperformed, and the 09:01 automated execution remains recorded as Step 13 `EGW00201` FAILED. Kiro updated documentation only; operational work was performed by the operator.

### ✅ Completed

- 🔴 Step 12~17 automatic-execution incident (verified directly by operator)

| Item | Value |
| --- | --- |
| State Machine | `portfolio-paper-daily-step12-17-approval` |
| Failure point | Step 13 (order/fill query) |
| Step 12 order submission | Completed normally · two SELL orders accepted by broker |
| First order at Step 13 | Reflected as fully filled |
| Second order at Step 13 | KIS `EGW00201` (transaction-rate limit exceeded) |
| Direct cause | No dedicated `EGW00201` retry · no wait between order queries |
| Downstream impact | Step 14~17 · success Slack did not run automatically |

- 🟢 Step 13 rate-limit reinforcement (performed directly by operator)

| Item | Value |
| --- | --- |
| Target file | Full replacement of `connector_order_check.py` · deployed to EC2 via S3 |
| Version | connector-order-check-2.0.1 → 2.0.2 |
| Inter-order wait | Added 5-second wait between queries |
| Retry | Up to 2 retries only for `EGW00201` (first 1.5s · second 5s) |
| Other error codes | No automatic retry |
| Pre-deployment validation | Original backup · SHA validation · Python compile · AST validation passed |
| Broker order-submission logic | No change |

- 🟢 Manual recovery of unresolved order at Step 13 (performed directly by operator)

| Item | Value |
| --- | --- |
| Step 12 rerun | Not performed due to duplicate-order risk |
| Manual Step 13 execution | 1 active order |
| Second SELL order | 13 shares fully filled · reflected as `FILLED` |
| Generated data | `SUMMARY_ONLY_FILLED` order event · fill data |
| Final status | Both orders `FILLED` · active targets 0 |
| New event version | connector-order-check-2.0.2 |

- 🟢 Sequential manual recovery of Step 14~17 (performed directly by operator)

| Item | Value |
| --- | --- |
| Temporary State Machine | Not created · executed Step 14·15·16·17 one by one in order |
| Step 14 `execution_sync_sell_fill.py` | ECS Task ExitCode 0 |
| Step 15 `execution_sync_buy_fill.py` | ECS Task ExitCode 0 |
| Step 16 `execution_sync_buy_position.py` | ECS Task ExitCode 0 |
| Step 17 `run_connector_balance_daily.sh` | run-date=2026-07-20 · balance API Status 200 · `connector_balance_snapshot` saved successfully |
| Holdings | 0 (historical snapshot records preserved) |

- 🟢 Latest balance snapshot (as of 2026-07-20)

| Item | Value |
| --- | --- |
| Cash balance | 8,706,505 KRW |
| Total valuation amount | 8,057,330 KRW |
| Intraday sell amount | 5,298,000 KRW |
| Position rows for today | None (actual holdings 0) |

- 🟢 Manual success-Slack recovery (performed directly by operator)

| Item | Value |
| --- | --- |
| Invoked Lambda | `portfolio-event-notifier` |
| Event | `DAILY_EXECUTION_SUCCESS` · runDate=2026-07-20 · stage=AFTER_STEP_17 |
| Result | StatusCode 200 · ok=true · actually received in Portfolio Daily Bot channel |
| 09:01 automated execution history | FAILED retained (manual recovery not represented as automatic success) |

- 🟢 Removed hardcoded State Machine runDate · corrected failure-Slack path (performed directly by operator)

| Item | Value |
| --- | --- |
| Step1_SendConnectorBalanceCommand | Removed hardcoded `--run-date 2026-06-22` · dynamically passes `$.runDate` (States.Array · States.Format) |
| Failure-bypass problem | When Task ended normally but returned an abnormal result, Choice Default went directly to Fail State and bypassed failure Slack |
| Correction | Changed Step13·14·15·16·Step1 Defaults to failure-context Pass States · store `$.dailyExecutionFailure` → failure Slack · OPS failure record · retain final FAILED state |
| Step12_Failed | Outside scope of this change |
| Validation | ASL validation OK · post-deployment reread confirmed removal of 2026-06-22 · application of $.runDate · corrected failure path |

- 🟢 Improved success Slack to show actual Daily fill details (performed directly by operator)

| Item | Value |
| --- | --- |
| New Builder Lambda | `portfolio-daily-execution-slack-summary-builder` · Python 3.12 |
| Role | Query actual fills for the day and generate success-Slack payload |
| DB user | `view_app` (schema USAGE · table SELECT · column SELECT confirmed) |
| Fill source | `connector.connector_fill` |
| Security-name source | `reference.stock_master` |
| Query as-of date | Convert `connector_fill.created_at` to Asia/Seoul date and compare with runDate |
| Result fields | `buyFills` · `sellFills` · per-security `tickerCode` · `stockName` · `fillQty` |

- 🟢 Changed success-Slack formatter · connected Builder (performed directly by operator)

| Item | Value |
| --- | --- |
| Target Notifier | `portfolio-event-notifier` · `DAILY_EXECUTION_SUCCESS` formatter |
| Success-message structure | Title ✅ [Daily 실행] 성공 · execution date · final status · [매수 체결] · [매도 체결] |
| Fill display | `- 없음` when none · otherwise `- 종목명 / 수량주` |
| Removed lines | Existing Workflow · Execution · order/fill summary lines |
| Before deployment | Existing Lambda code backup · Python compile · import · formatter local test passed |
| After deployment | Status Active · LastUpdateStatus=Successful · marker/hash consistency confirmed (raw SHA256 not recorded) |

- 🟢 Connected State Machine success path · least-privilege IAM (performed directly by operator)

| Item | Value |
| --- | --- |
| Target State Machine | `portfolio-paper-daily-step12-17-approval` |
| New State | `BuildDailyExecutionSuccessSlackSummary` |
| Success path | Step completion → Builder → Notifier → `RecordWorkflowStepSuccess` |
| Builder input · result | runDate input · result stored at `$.dailyExecutionSuccessSummary` |
| Notifier input | `$.dailyExecutionSuccessSummary.Payload` |
| If Builder fails | Connect to existing `SendDailyExecutionFailedSlack` path |
| IAM | Dedicated minimal-execution Role for Builder · `lambda:InvokeFunction` inline on State Machine Role (`portfolio-daily-execution-slack-builder-invoke`) |
| ASL validation | OK · diagnostics 0 · canonical(key-sorted) comparison had 0 semantic differences |

- 🟢 Manual smoke result (performed directly by operator)

| Item | Value |
| --- | --- |
| Method | Sequential manual Builder → Notifier invocation using 2026-07-20 data |
| Full execution | Not started (did not run entire State Machine) |
| BUY fills | 0 |
| SELL fill (1) | 엔씨소프트(036570) 13 shares |
| SELL fill (2) | 코오롱생명과학(102940) 78 shares |
| Notifier | StatusCode 200 · no FunctionError · ok=true · Slack actually received |
| 09:01 automated execution history | Step 13 `EGW00201` FAILED retained (manual smoke not represented as automatic success) |

- 🟢 P0 safety hardening — bounded polling for Step 13 active orders (after incident recovery · performed directly by operator)

| Item | Value |
| --- | --- |
| Target file · version | `connector_order_check.py` connector-order-check-2.0.2 → 2.0.3 |
| Active statuses handled | ACCEPTED · SUBMITTED · PENDING · PARTIAL_FILLED · PARTIALLY_FILLED |
| Polling | Up to 3 additional polls after initial query · default 10-second interval · exit 1 if still active at end |
| Existing behavior retained | 5-second wait between orders · dedicated `EGW00201` retry |
| Deployment | Deploy to EC2 through temporary S3 object · timestamp backup · delete temporary object after deployment |
| Validation | Python compile before/after replacement · local/remote SHA256 matched (raw hash not recorded) |
| Nature | Repeated order/fill query API calls · not an order-submission retry |

- 🟢 P0 safety hardening — Step14~16 data-consistency fail-closed behavior (performed directly by operator)

| Item | Value |
| --- | --- |
| Step14 SELL Fill | Validate target count · processed count · fill · execution order · original position · position-close status together |
| Step15 BUY Fill | Validate target count · processed count · rollback and exit 1 on missing/failure |
| Step16 BUY Position | Validate request · fill · position linkage · quantity · rollback and exit 1 on mismatch |
| Commit condition | Commit · success only when all validations match |

- 🟢 P0 safety hardening — two new validators · Execution image · ECS integration (performed directly by operator)

| Item | Value |
| --- | --- |
| `execution_validate_ready_plan_order.py` | Immediately before Step12 after approval · compare READY Plan `ready_order_count` with actual PAPER_STRATEGY Order count · exit 1 on missing/count mismatch/wrong quantity or security |
| Real-data validation (1) | 2026-07-20 Plan 143 · 2 Orders validation succeeded · ECS Fargate smoke ExitCode 0 |
| `execution_validate_order_chain.py` | Immediately after Step16 · read-only Plan→Order→Request→Fill→Position validation · exit 1 on mismatch |
| Real-data validation (2) | 2026-07-20 two SELL orders validation succeeded · ECS Fargate smoke ExitCode 0 |
| Failure path | Both validators connect failures to common failure Slack · OPS failure-record path |
| ECR tag | Built new immutable tag `paper-20260720-p0-integrity-v1` · did not overwrite existing tag |
| Task Definition | `portfolio-paper-strategy-execution` revision 3 · Step14·15·16 connected |
| New-image validation | Modified files · validator py_compile · new Task Definition ECS Fargate smoke succeeded |

- 🟢 P0 safety hardening — Step12 failure propagation · failure-formatter smoke (performed directly by operator)

| Item | Value |
| --- | --- |
| Step12_Failed | Removed direct Fail termination · store Error·Cause in `dailyExecutionFailure` → `SendDailyExecutionFailedSlack` → `RecordBatchFailure` → final Fail |
| Validation | Local backup of pre-change definition · ASL validation OK · post-deployment reread validation |
| Failure formatter smoke | Directly invoked Notifier Lambda with a `DAILY_EXECUTION_FAILED` test event without causing a real order failure |
| Display confirmation | Execution date · Workflow · failed Step · cause · whether order was submitted · next action · explicit test-event wording in cause · actual Portfolio Daily Bot receipt |

- 🟠 Distinguish P0 completed scope vs incomplete items

| Item | Value |
| --- | --- |
| Complete | ACCEPTED·PARTIAL_FILLED polling · Step14·15·16 processed-count/consistency validation · Plan→Order→Request→Fill→Position mismatch failure propagation · failure on missing/count-mismatched Orders vs READY Plan · Step12_Failed failure-Slack path · failure formatter smoke |
| Nature of completion | P0 implementation · operational deployment · standalone smoke · State Machine integration |
| Incomplete (P1) | Observe next normal automated run success · full automated acceptance test not performed |
| Automated execution history | 09:01 automatic execution retained as Step 13 `EGW00201` FAILED (manual recovery/P0 completion not recorded as automatic success) |

### 🔵 Evidence

- 🔵 Raw State Machine ARN · execution ARN · SSM Command ID · ECS Task ARN · broker order number · account-id · SHA256 are not pasted into this log. If needed, reference only via `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_BROKER_ORDER_NO]` · `[REDACTED_ACCOUNT_NO]` placeholders.
- 🔵 Reinforced `_common/risk-register.md` R-AUTO-001 · R-AUTO-037 · R-AUTO-038 · R-BROKER-004 Mitigation history for 2026-07-20 · added short operational Details to `_common/operator-decisions.md` OD-SAFE-001 · OD-SAFE-004 · OD-MS-009 · OD-MS-030 · OD-MS-031 · OD-MS-032.
  Added Done recently 2026-07-20 and updated Now in `_common/followups-overview.md` · added short operational notes to `_common/ms-aws-service-decision-matrix.md` 4.1 · 4.7 · added Usage Notes for 2026-07-20 to `_common/aws-resource-glossary.md`.

### ⚠️ Risks

| Item | Value |
| --- | --- |
| Risk ID | R-AUTO-001 |
| Status change | 🔴 Open retained |
| Summary | Broker-order retries themselves remain prohibited · limited retry only for query API `EGW00201` · Step 12 was not rerun to prevent duplicates |

| Item | Value |
| --- | --- |
| Risk ID | R-AUTO-037 |
| Status change | 🟢 Mitigated retained |
| Summary | Confirmed Step 13 failure stops downstream Steps · removed hardcoded runDate · reinforced result-failure Slack path |

| Item | Value |
| --- | --- |
| Risk ID | R-AUTO-038 |
| Status change | 🟢 Mitigated retained |
| Summary | Need to distinguish automated-execution failure history from manual-recovery history · follow-up to expand OPS detailed Steps retained |

| Item | Value |
| --- | --- |
| Risk ID | R-BROKER-004 |
| Status change | 🟢 Mitigated retained |
| Summary | Recovered only Step 13 without rerunning Step 12 · 0 duplicate broker orders |

### 📌 Follow-ups

- [ ] [P1] Observe the next normal automated run end-to-end (without forced orders or induced errors) — Step1~11 Scheduler automatic success · Step12~17 automatic execution · automatic Builder→Notifier entry and same-day fill details in success Slack · DB after-check · 5-second inter-order wait logs · no unnecessary retry when `EGW00201` does not occur · both new validators pass in automatic path.
- [ ] [P1] Perform full automated-run acceptance test (P0 is complete only through implementation · deployment · standalone smoke · State Machine integration).
- [ ] In a real multi-order run, verify 5-second inter-order wait · active-order polling · `EGW00201` retry logs in operation (normal if retry path is not entered when the error does not occur).
- [ ] Expand OPS Mirror detailed Steps · distinguish automated runs from manual recovery runs in mirror.
- [ ] Reinforce Signal Order Map · Broker acceptance segment in the order-validation chain (Plan→Order→Request→Fill→Position segment is already validated by `execution_validate_order_chain.py`).

### 🔐 Security

| Item | Result |
| --- | --- |
| AWS · DB · broker · KIS · Slack · SSM · ECS · EC2 · Lambda · Step Functions commands | Kiro execution 0 · performed directly by operator |
| Newly recorded raw broker order number · execution ARN · State Machine ARN · SSM Command ID · ECS Task ARN · account-id · SHA256 | 0 |
| git add · commit · push · reset · restore · checkout · stash | 0 |
| Placeholder policy | Use only `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_BROKER_ORDER_NO]` · `[REDACTED_ACCOUNT_NO]` families |
| Save encoding | UTF-8 No BOM retained |
## 2026-07-16 (daily-buy-e2e-first-run · Step 12 wait increased to 60 seconds · follow-up polling candidate)

### 🧭 Summary

In the 2026-07-16 run, Daily Run 76 was calculated normally and produced two BUY candidates (엔씨소프트 036570 · 코오롱생명과학 102940). The operator manually reran Step 12~17; actual KIS paper-trading market BUY orders were accepted and fully filled, completing the first real run connected through Fill and Position.
An issue was found where the initial Step 13 query occurred about 10 seconds after order submission, causing intermediate fill states to become stuck internally. The operator manually reran Step 13 → Step 15 → Step 16 to restore DB consistency. As a recurrence-prevention measure, the State Machine `portfolio-paper-daily-step12-17-approval` Wait State `Step12_WaitBeforeCheck` `Seconds` was changed from 10 → 60.
Follow-up items remain: polling · stronger Execution Order consistency/failure propagation · detailed OPS Mirror Step expansion. Kiro updates documentation only in this session.

### ✅ Completed

- 🟢 Daily Step 1~11 recovery (performed directly by operator)

| Item | Value |
| --- | --- |
| daily_run_id | 76 |
| run_date | 2026-07-16 |
| data_date | 2026-07-15 |
| market_signal | AGGRESSIVE |
| BUY candidates | 엔씨소프트 036570 · 코오롱생명과학 102940 |

- 🟢 Manual rerun of Step 12~17 · real BUY orders (performed directly by operator)

| Item | Value |
| --- | --- |
| 엔씨소프트 order | 13-share market BUY |
| 코오롱생명과학 order | 78-share market BUY |
| KIS paper-trading acceptance | Both orders succeeded |
| Raw broker order number | Not recorded in document |

- 🟢 Confirmed actual balance

| Item | Value |
| --- | --- |
| 엔씨소프트 final holding quantity | 13 shares |
| 엔씨소프트 average fill price | 223,500 KRW |
| 코오롱생명과학 final holding quantity | 78 shares |
| 코오롱생명과학 average fill price | Approximately 38,839.7436 KRW |
| Fully filled | Both orders complete |

- 🟠 Initial order/fill-query issue

| Item | Value |
| --- | --- |
| Initial Step 13 query timing | About 10 seconds after order submission |
| Initial 엔씨소프트 query | 9 shares partially filled |
| Initial 코오롱생명과학 query | Accepted status |
| Actual market orders | Continued filling afterward |
| Internal status | Stuck at first query result |
| Entire Step Functions | Treated as SUCCESS based on ExitCode 0 |

- 🟢 Manual recovery sequence (performed directly by operator)

| Item | Value |
| --- | --- |
| Step 13 rerun | `connector_order_check.py` · both orders reflected as `FILLED` · latest Connector Order Event · Connector Fill 13 shares · 78 shares reflected |
| Step 15 rerun | `execution_sync_buy_fill.py` · 2 Execution Orders `SUBMITTED` → `FILLED` |
| Step 16 rerun | `execution_sync_buy_position.py` · `filled_buy_orders_without_position=2` · `synced_count=2` · `skipped_count=0` |
| New Position | ID 13(엔씨소프트) · ID 14(코오롱생명과학) · both `OPEN` |

- 🟢 Final data-chain consistency

| Item | Value |
| --- | --- |
| Daily Signal | Normal |
| Execution Plan | Normal |
| Execution Order | `FILLED` |
| Connector Order Request | `FILLED` |
| Connector Fill | Normal |
| Strategy Position State | `OPEN` |
| Actual balance vs internal Position | Consistency confirmed |

- 🟢 Recurrence-prevention change (performed directly by operator)

| Item | Value |
| --- | --- |
| Target State Machine | `portfolio-paper-daily-step12-17-approval` |
| Target Wait State | `Step12_WaitBeforeCheck` |
| Before · after | `Seconds=10` → `Seconds=60` |
| Next State | `Step12_GetCommandInvocation` retained |
| Validation | Passed reread after State Machine update |
| Raw actual ARN · revision ID | Not recorded |

### 🔵 Evidence

- 🔵 Raw broker order numbers · actual State Machine ARN · executionArn · revision ID · account-id · Slack payload · SSM response · psql raw output are not pasted into this log. If needed, reference only via `[REDACTED_BROKER_ORDER_NO]` · `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_ACCOUNT_NO]` placeholders.
- 🔵 Reinforced `_common/risk-register.md` R-AUTO-037 · R-AUTO-038 · R-BROKER-004 Mitigation history for 2026-07-16 · added short operational-validation notes to `_common/operator-decisions.md` OD-MS-032 · OD-SAFE-001 Details.
  Added Done recently 2026-07-16 row in `_common/followups-overview.md` · added short operational note for `port_strategy_execution` in `_common/ms-aws-service-decision-matrix.md` · added Usage Notes 2026-07-16 table in `_common/aws-resource-glossary.md`.

### ⚠️ Risks

| Item | Value |
| --- | --- |
| Risk ID | R-AUTO-037 |
| Status change | 🟢 Mitigated retained |
| Summary | First real BUY · fill · Fill · Position E2E validation · Step 12 Wait changed to 60 seconds · polling · processed-count failure propagation · OPS Mirror detail expansion remain |

| Item | Value |
| --- | --- |
| Risk ID | R-AUTO-038 |
| Status change | 🟢 Mitigated retained |
| Summary | OPS Mirror records the entire Step 12~17 range as one representative Step · reconfirmed need for detailed Step expansion |

| Item | Value |
| --- | --- |
| Risk ID | R-BROKER-004 |
| Status change | 🟢 Mitigated retained |
| Summary | 0 duplicate broker orders in real BUY run · no regression of pre-check policy |

### 📌 Follow-ups

- [ ] Follow-up polling for `ACCEPTED` / `PARTIAL_FILLED` orders — if still incomplete after 60 seconds, requery a bounded number of times at 30~60-second intervals — 04 spec.
- [ ] Strengthen failure propagation based on processed counts in Step 13 · 15 · 16 — 04 spec.
- [ ] Workflow FAIL on mismatch among Execution Order · Connector Request · Fill · Position — 04 spec.
- [ ] Review linkage between 10-minute balance snapshot and order · fill · Position correction — 03, 04 spec.

### 🔐 Security

| Item | Result |
| --- | --- |
| AWS · DB · broker · KIS · Slack · Scheduler · Step Functions · ECS · EC2 commands | Kiro execution 0 · performed directly by operator |
| broker · KIS automated BUY · automated SELL · fill sync · position sync · intraday monitor · crawler execution | 0 |
| Newly recorded raw broker order number · executionArn · State Machine ARN · account-id · secret · webhook URL · raw payload · account number · raw Fill | 0 |
| Newly recorded Lambda · IAM Policy · Step Functions history · SSM response · raw SQL output | 0 |
| git add · commit · push · rebase · reset · restore | 0 |
| Placeholder policy | Use only `[REDACTED_BROKER_ORDER_NO]` · `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_ACCOUNT_NO]` families |
| Save encoding | UTF-8 No BOM retained |

## 2026-07-15 (daily-brief-slack-recovery · migration to common Dispatcher Holiday Guard · Daily BUY KST date-misjudgment fix · Approval Slack data consistency and candidate-display improvement)

### 🧭 Summary

On the afternoon of 2026-07-15, an issue was identified where the automatic post-market Daily Brief Slack was not sent. The causes were: the Builder Lambda failed to call the external Holiday API from inside the VPC and therefore skipped pre/post-market Slack via Fail-Closed behavior;
even after disabling the Builder's internal Holiday Guard, the normal response lacked the `skipped` field, causing the mini state machine `CheckHolidaySkip` Choice to raise `States.Runtime`; and the Builder deployment ZIP did not contain `pg8000`, causing an import error when entering the DB path.
The operator directly added support for `MORNING_BRIEF` and `EVENING_BRIEF` to the Daily Scheduler Dispatcher, added the Daily Brief State Machine ARN + `states:StartExecution` IAM permission, and changed the pre/post-market Scheduler Targets to invoke the Dispatcher Lambda, consolidating Holiday Guard responsibility in the Dispatcher.
The Builder's internal guard was made environment-variable controlled and disabled in the operational environment; `skipped=false` and `skipReason=null` were added to normal responses, and `pg8000` was packaged and redeployed. In the 2026-07-15 16:02 post-market live smoke, Step Functions `SUCCEEDED`, Notifier `statusCode=200`, and actual Slack receipt were confirmed. Kiro updates documentation only in this session.

### ✅ Completed

- 🟠 Confirmed incident causes (verified directly by operator)

| Item | Value |
| --- | --- |
| Primary cause | Builder Lambda failed to call external Holiday API from inside VPC |
| Fail-Closed result | Pre-market · post-market Slack sends were skipped |
| Secondary cause | Normal response lacked `skipped` after Builder internal Holiday Guard was disabled |
| Choice result | `States.Runtime` occurred at `CheckHolidaySkip` |
| Third cause | Builder deployment ZIP lacked `pg8000` |
| Secondary effect | Import error when entering DB path |

- 🟢 Final automation structure (performed directly by operator)

| Item | Value |
| --- | --- |
| Pre-market 07:50 | Scheduler → Daily Scheduler Dispatcher → common Holiday Guard → Daily Brief State Machine |
| Daily validation 08:00 | Scheduler → same Dispatcher → same Holiday Guard → Step 1~11 |
| Daily execution 09:01 | Scheduler → same Dispatcher → same Holiday Guard → Step 12~17 |
| Post-market 15:50 | Scheduler → same Dispatcher → same Holiday Guard → Daily Brief State Machine |
| EC2 Stop 15:50 | Existing Scheduler retained · continues regardless of holiday |

- 🟢 Actual modifications (performed directly by operator)

| Item | Value |
| --- | --- |
| Dispatcher supported eventType | Added `MORNING_BRIEF` · `EVENING_BRIEF` |
| Dispatcher environment variable | Added Daily Brief State Machine ARN (raw value not recorded) |
| Dispatcher IAM | Added Daily Brief State Machine `states:StartExecution` permission |
| Dispatcher IAM scope | Resource-scoped · no wildcard |
| Pre-market Scheduler Target | Direct Daily Brief State Machine invocation → Dispatcher Lambda invocation |
| Post-market Scheduler Target | Direct Daily Brief State Machine invocation → Dispatcher Lambda invocation |
| Scheduler time · status | Existing times retained · ENABLED retained |
| Builder Holiday Guard | Environment-variable controlled · disabled in operational environment |
| Builder normal-response fields | Added `skipped=false` · `skipReason=null` |
| Builder dependency | Packaged `pg8000` and related dependencies |
| Existing 08:00 · 09:01 Daily Schedulers | No change |
| EC2 start · stop Schedulers | No change |

- 🟢 Validation results (performed directly by operator)

| Item | Value |
| --- | --- |
| Existing Dispatcher Step 1~11 · Step 12~17 paths | Normal |
| Dispatcher pre/post-market Daily Brief paths | Normal |
| Weekend real-execution-mode smoke | Confirmed Dispatcher Holiday Guard blocked execution |
| Builder DB query | Normal |
| Builder normal-response fields | Confirmed `skipped=false` · `skipReason=null` |
| 2026-07-15 15:50 automatic post-market execution | Failed initially due to `CheckHolidaySkip` error |
| 2026-07-15 16:02 post-market live smoke | Step Functions `SUCCEEDED` |
| Notifier | `statusCode=200` |
| Slack | Actual receipt confirmed |

- 🟢 Summary of actual received content (2026-07-15 post-market)

| Item | Value |
| --- | --- |
| As-of date | 2026-07-15 |
| Total valuation amount | 8,706,505 KRW |
| Cash | 8,706,505 KRW |
| Cumulative return | -12.94% |
| Valuation P/L | -1,293,495 KRW |
| Day-over-day | 0 KRW |
| Holdings | None |

### 🔵 Evidence

- 🔵 Lambda source · IAM Policy · Step Functions history · PowerShell output · full SSM response · actual State Machine ARN · accountNo · executionArn · RequestId · SHA256 · webhook URL are not pasted into this log. If needed, reference only with `[REDACTED_ARN]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_EXECUTION_ARN]` placeholders.
- 🔵 Reinforced `_common/risk-register.md` R-AUTO-035 Mitigation history for 2026-07-15 · moved the 15:50 post-market Slack item from Now to Done recently in `_common/followups-overview.md` · retained the 07:50 pre-market Slack item in Now · added short operational validation notes to `_common/operator-decisions.md` OD-MS-032
  OD-MS-038 Details · added a short Daily Batch orchestration operational note to `_common/ms-aws-service-decision-matrix.md` · added short EventBridge Scheduler · Lambda Usage Notes to `_common/aws-resource-glossary.md`.

### ⚠️ Risks

| Item | Value |
| --- | --- |
| Risk ID | R-AUTO-035 |
| Status change | 🟢 Mitigated retained |
| Summary | Consolidated Holiday Guard in common Dispatcher · added Builder normal skip fields · packaged `pg8000` · reinforced Mitigation history with actual 2026-07-15 16:02 post-market Slack receipt |

### 📌 Follow-ups

- [ ] Keep follow-up to confirm actual automatic pre-market Slack receipt at 07:50 on the next business day — 04 spec.
- [ ] Reconfirm per-security Daily Brief display when holdings exist — 04 spec.
- [ ] Decide Daily Brief Holiday API fallback policy after consolidating common Holiday Guard in Dispatcher — 04 spec.

### 🧭 Afternoon — resolved Daily BUY KST date misjudgment

This is a separate incident from the Daily Brief send failure. On 2026-07-13 (Mon), `daily_run_id=73` was calculated normally and there were 4 BUY signals / 4 candidates, but no Execution Plan / Execution Order was created, so all 4 BUY candidates failed to reach the execution target set. The execution container used naive `date.today()`
and `datetime.today()`; at 08:00 KST (= 2026-07-12 23:00 UTC), it interpreted the business date as 2026-07-12 (Sun), causing Step8 to finish as `WEEKEND / NO_TARGET` with ExitCode 0. Step Functions therefore displayed overall SUCCESS, making the problem hard to detect automatically. Daily strategy calculation and the 2026-07-10 data were normal (`daily_run_id=73`
`run_date=2026-07-12` `data_date=2026-07-10` `market_signal=AGGRESSIVE`).

- 🟢 Corrected KST date basis (performed directly by operator)

| Item | Value |
| --- | --- |
| DB timestamp storage basis | Retain UTC |
| Business-date judgment basis | Standardized to Asia/Seoul |
| Daily BUY / SELL execution ECS Task Definition | Added `TZ=Asia/Seoul` |
| Market EC2 server timezone | Changed to Korea Standard Time |
| Execution scripts | Reinforced both local · ECS paths to use KST business date |
| Step Functions execution path | Connected new Task Definition revision |

- 🟢 Distinguish completed vs incomplete

| Item | Status |
| --- | --- |
| Direct cause of UTC date misjudgment | 🟢 Resolved |
| ECS · Market EC2 KST application | 🟢 Complete |
| New Task Definition revision connection | 🟢 Complete |
| Recurrence in next automatic execution | 🟠 Continue real-operation observation |
| Strengthen distinction between `NO_TARGET` and success status | 🟠 Follow-up |
| Strengthen internal-order-failure ExitCode and SF failure propagation | 🟠 Follow-up |
| Reinforce order-validation chain (Plan → Order → Connector Request → Signal Order Map → Broker → Fill → Position) | 🟠 Follow-up |

- 🔵 Evidence — 04 operation-notes 2026-07-15 (오후) section · `_common/risk-register.md` R-DATA-010 [2026-07-15 보강] · R-DATA-017 [2026-07-15 보강] · `_common/operator-decisions.md` OD-MS-040 [2026-07-15 보강] · `_common/followups-overview.md` Now.

### 🧭 Afternoon — improved Approval Slack data consistency and candidate display

The Approval Required Slack message was displaying a mixed combination from different Daily Run and Execution Plan records. The Builder independently queried the latest Execution Plan (`latestPlanId=133`
`latestPlanDate=2026-07-09`) and latest Daily Run (`latestDailyRunId=73`), rendering a combination that did not actually exist in the DB: status `DEFENSIVE` · as-of date `2026-07-09` · 4 signals · 4 candidates. The actual result was `daily_run_id=73` · base data 2026-07-10 · market status `AGGRESSIVE`
with 4 BUY signals · 4 candidates.

- 🟢 Corrected Approval Slack query structure (performed directly by operator)

| Item | Value |
| --- | --- |
| Query basis | Fix a single `daily_run_id`, then query status · as-of date · signals · candidates using the same Run |
| Removed independent latest query | Removed separate Plan · Run selection |
| Order query | Only Execution Orders linked through `source_daily_run_id` |
| Plan query | Only the Execution Plan referenced by those Orders |
| If linked Plan does not exist | `latestPlanId=null` · do not use an arbitrary old Plan |
| Message status · as-of date | Use `market_signal` · `data_date` from the selected Daily Run |

- 🟢 Improved candidate display

| Item | Value |
| --- | --- |
| Security name | Use actual `company_name` or `ticker_code` |
| Score fields | Structured by Builder and passed to Notifier |
| Score decimals | Display to three decimal places |
| Korean labels | 종합 · 수급 · 정보 · 추세 · 공매도 |
| Raw `buy_info` dict | Not exposed to Slack |

- 🟢 Deployment · validation results (performed directly by operator)

| Item | Value |
| --- | --- |
| Builder Lambda `portfolio-approval-slack-summary-builder` | `Active` · `LastUpdateStatus=Successful` · Runtime · Handler unchanged |
| Notifier Lambda `portfolio-event-notifier` | `Active` · `LastUpdateStatus=Successful` · Runtime · Handler unchanged |
| Previous-version rollback ZIP | Secured |
| Builder DB query status · as-of date · signal count · candidate count | AGGRESSIVE · 2026-07-10 · 4 · 4 |
| `latestPlanId` | null (no Order · Plan for selected Run · confirmed old Plan not used) |
| Candidate securities | DL · 대주전자재료 · 삼성SDI · 한국피아이엠 |
| Builder → Notifier → Slack message-display E2E | Complete (not an actual automatic-order fill E2E) |

- 🔵 Evidence — 04 operation-notes 2026-07-15 (오후) section · `_common/operator-decisions.md` OD-MS-031 [2026-07-15 보강] · `_common/followups-overview.md` Done recently 2026-07-15 row.

### 🔐 Security

| Item | Result |
| --- | --- |
| AWS · Lambda · Step Functions · Scheduler · IAM · DB · Slack · ECS · EC2 execution | Kiro execution 0 · performed directly by operator |
| broker · KIS · crawler · automated BUY · automated SELL · fill sync · position sync · intraday monitor execution | 0 |
| Newly recorded raw secret · password · token · webhook URL · account number · actual ARN · executionArn · RequestId · SHA256 · Slack payload | 0 |
| Newly recorded Lambda code · IAM Policy · Step Functions history · PowerShell output · full SSM response · raw SQL output | 0 |
| git add · commit · push execution | 0 |
| Placeholder policy | Use only `[REDACTED_ARN]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_LAMBDA_ARN]` families |
| Save encoding | UTF-8 No BOM retained |

## 2026-07-09 (krx-crawler-ec2-timezone-fix-and-recollect)

### 🧭 Summary

After the 2026-07-08 run, the remaining cause of KRX raw stagnation (`interest_program_raw` · `interest_shortsell_raw` `MAX(trade_date)=2026-07-07`) was identified: the KRX CRAWLER Windows EC2 timezone was set to UTC, so during the 08:00 KST execution the server-local time was interpreted as the previous day around 23:00 UTC.
The operator changed the CRAWLER Windows EC2 timezone to Korea Standard Time and manually recollected through the KRX worker, confirming recovery of raw data for 2026-07-08. Kiro updates documentation only in this session.

### ✅ Completed

- 🟠 Confirmed CRAWLER Windows EC2 timezone issue (performed directly by operator)

| Item | Value |
| --- | --- |
| Symptom | Latest date for `interest_program_raw` / `interest_shortsell_raw` stuck at 2026-07-07 |
| Step Functions Step1~11 | SUCCEEDED (does not guarantee KRX Step2B raw freshness) |
| Direct cause | CRAWLER Windows EC2 timezone set to UTC |
| Additional cause | `datetime.today() - 1` · `datetime.now().date() - 1` family logic calculated target date as 2026-07-07 |

- 🟢 CRAWLER Windows EC2 timezone action (performed directly by operator)

| Item | Value |
| --- | --- |
| Timezone change | UTC → Korea Standard Time |
| Get-Date · Get-TimeZone · Python datetime | Confirmed KST-based calculation |
| Server-time issue | Resolved |
| DB `created_at` session timezone | Recommend retaining UTC (convert for display with `at time zone 'Asia/Seoul'`) |

- 🟢 KRX recollection result (performed directly by operator)

| Item | Value |
| --- | --- |
| CRAWLER EC2 start | Complete |
| SSM Online | Confirmed |
| Portfolio-KRX-Worker-Daily Scheduled Task | Manually executed |
| `interest_program_raw` latest_date | 2026-07-08 |
| `interest_program_raw` rows_20260708 | 1 |
| `interest_shortsell_raw` latest_date | 2026-07-08 |
| `interest_shortsell_raw` rows_20260708 | 349 |

- 🟢 Load timestamps (displayed in KST)

| Item | Value |
| --- | --- |
| program `created_at` (UTC) | 2026-07-09 02:23:33 |
| program display (KST) | 2026-07-09 11:23:33 |
| shortsell `created_at` (UTC) | 2026-07-09 02:24:08 |
| shortsell display (KST) | 2026-07-09 11:24:08 |

- 🟢 Final judgment

| Item | Value |
| --- | --- |
| KRX program / shortsell recollection for 2026-07-08 | Complete |
| Effect of CRAWLER EC2 timezone correction | Confirmed |
| Step1~11 freshness validation | Both core ECS raw + KRX GUI worker recovered |

- 🟠 Reconfirmed Step2B automatic-execution structure

| Item | Value |
| --- | --- |
| 07:50 KST | EventBridge Scheduler → EC2 lifecycle dispatcher (`action=start` · `target=BOTH` · `holidayCheck=true` · `reason=PRE_DAILY_STEP1_11` · `dryRun=false`) |
| 08:00 KST | Step1~11 scheduler → daily scheduler dispatcher |
| Step2B flow | Step Functions Step2B_RunKrxGuiWorker → SSM AWS-RunPowerShellScript → Windows Scheduled Task Portfolio-KRX-Worker-Daily → `C:\portfolio\run_krx_worker_daily.ps1` → KRX login / program / shortsell |
| Operational interpretation | Step1~11 itself does not start the CRAWLER EC2; it assumes CRAWLER EC2 running + SSM Online before sending the Step2B command · manual revalidation must also check the prerequisite first |

- 🔴 Remaining false-success issue (keep Open)

| Item | Value |
| --- | --- |
| `interest_program.py` / `interest_shortsell.py` | May output `[Error]` yet still exit code 0 |
| `run_krx_worker_daily.ps1` | May incorrectly interpret the above as success |
| Windows Scheduled Task LastTaskResult | 0 |
| Step2B SSM ResponseCode | 0 |
| Entire Step Functions | SUCCEEDED |
| But DB | Raw data for expected trade_date may be absent |

### 🔵 Evidence

- 🔵 Detailed KRX raw · Scheduled Task · SSM execution details · actual ARN / CommandId / instance-id / execution ARN are not pasted into this log. If needed, reference only via `[REDACTED_ARN]` · `[REDACTED_COMMAND_ID]` · `[REDACTED_INSTANCE_ID]` placeholders.
- 🔵 Reinforced `_common/risk-register.md` R-DATA-010 Mitigation history for 2026-07-09 · keep R-DATA-017 Open (false-success issue remains) · moved item from Now to Done recently in `_common/followups-overview.md` and newly registered `KRX Step2B DB validation 추가` under Next.
  Added CRAWLER Windows EC2 KST note to `_common/operator-decisions.md` OD-MS-040 Details · added short KST note to `_common/ms-aws-service-decision-matrix.md` 4.3 port-interest-crawler section.

### ⚠️ Risks

| Item | Value |
| --- | --- |
| Risk ID | R-DATA-010 |
| Status change | 🔴 Open → 🟢 Mitigated |
| Summary | Freshness recovery confirmed through ECS Fargate TZ patch (2026-07-08) + CRAWLER Windows EC2 KST change + KRX recollection · retain regular audit + detection |

| Item | Value |
| --- | --- |
| Risk ID | R-DATA-017 |
| Status change | 🔴 Open retained |
| Summary | Step2B false-success remains (worker exit 0 · SSM ResponseCode 0 · Step Functions SUCCEEDED ≠ KRX raw freshness) |

### 📌 Follow-ups

- [ ] In `run_krx_worker_daily.ps1`, exit 1 when `[Error]` is detected — 08 spec.
- [ ] `sys.exit(1)` on failure in `interest_program.py` / `interest_shortsell.py` — 08 spec.
- [ ] Add DB validation after Step2B (for expected trade_date: program >= 1 · shortsell = 349 · fail Step Functions if validation fails) — 04, 08 spec.
- [ ] Manage CRAWLER Windows EC2 KST-retention policy through an OD-MS-040 Details note · keep root fix (replace code with timezone-aware helper) for a follow-up 08-spec phase.

### 🔐 Security

| Item | Result |
| --- | --- |
| AWS · DB · psql · Spring Boot · Lambda · Step Functions · ECS · SSM · EC2 · IAM · RDS execution | Kiro execution 0 · performed directly by operator |
| broker · KIS · crawler · automated BUY · automated SELL · fill sync · position sync · intraday monitor execution | 0 |
| Newly recorded raw secret · password · token · webhook URL | 0 |
| Newly recorded raw accountNo · account-id · actual ARN · public IP · broker_order_no · CommandId · executionArn · instance-id | 0 |
| Newly recorded full raw SQL output · CloudWatch content · Step Functions history | 0 |
| git add · commit · push execution | 0 |
| Placeholder policy | Use only `[REDACTED_ARN]` · `[REDACTED_ACCOUNT]` · `[REDACTED_INSTANCE_ID]` · `[REDACTED_COMMAND_ID]` families |

## 2026-07-08 (view-daily-batch-ops-mirror-ui-consumption-confirmed)

### 🧭 Summary

Completed follow-up UI-consumption validation for the Step Functions execution-history OPS Mirror (OD-DB-012 · OD-MS-039 · R-AUTO-038) completed on 2026-07-03. Confirmed `ops` schema permissions for `view_app`, SELECT access to `ops.strategy_daily_batch_run` · `ops.strategy_daily_batch_step_log`,
and actual rendering of `AWS_STEPFUNCTIONS` run history on the `/daily-batch` screen. Kiro updates documentation only in this session.

### ✅ Completed

- 🟢 DB permission confirmation (performed directly by operator)

| Item | Value |
| --- | --- |
| Target role | `view_app` |
| `ops` schema USAGE | Confirmed |
| `ops.strategy_daily_batch_run` SELECT | Confirmed |
| `ops.strategy_daily_batch_step_log` SELECT | Confirmed |

- 🟢 OPS Mirror load confirmation (performed directly by operator)

| Item | Value |
| --- | --- |
| `AWS_STEPFUNCTIONS` run-history load | Confirmed |
| Recent `run_status` flow | SUCCESS |
| run #61 family | Step 12~17 |
| run #60 family | Step 1~11 |

- 🟢 step_log details confirmation (performed directly by operator)

| Item | Value |
| --- | --- |
| Representative #61 step_log item | `SFN_STEP12_17_WORKFLOW` / SUCCESS |
| Representative #60 step_log item | `APPROVAL_BLOCKED` / SKIPPED |
| Run + step detail query | Available |

- 🟢 View-screen confirmation (performed directly by operator)

| Item | Value |
| --- | --- |
| `/daily-batch` execution-mode display | aws-stepfunctions |
| Recent run-history label | `AWS_STEPFUNCTIONS` |
| Selected-run detail rendering | Confirmed (#61) |
| Selected-run step-log rendering | Confirmed (#61) |
| AWS Step 1~11 start button | Displayed |
| AWS Step 12~17 approval execution button | Displayed |
| Local File execution mode | OFF |
| aws-stepfunctions execution mode | ON |

- 🟢 Final judgment

| Item | Value |
| --- | --- |
| View Daily Batch / OPS Mirror functionality | Confirmation complete |
| OPS Mirror DB load | Normal |
| View query permissions | Normal |
| `/daily-batch` run list · selected run · step log rendering | Normal |

### 🔵 Evidence

- 🔵 Full detailed SELECT results · raw HTML · run/step detail JSON · executionArn · stateMachineArn · raw accountNo are not pasted into this log. If needed, separate into each spec's `operation-notes.md`.
- 🔵 Reinforced `_common/operator-decisions.md` OD-MS-039 · `_common/risk-register.md` R-AUTO-038 Mitigation history for 2026-07-08 · added Done recently 2026-07-08 row to `_common/followups-overview.md`.

### 📌 Follow-ups

- [ ] Review redaction for `requestPayload` / `resultPayload` display on `/daily-batch` — 05 spec.
- [ ] Review masking of raw `accountNo` (`[REDACTED_ACCOUNT_NO]` family display) — 05 spec.
- [ ] Review redaction of raw `executionArn` / `stateMachineArn` — 05 spec.
- [ ] Review display of `timestamp` in KST rather than raw UTC — 05 spec.
- [ ] Review operator-friendly wording for #60-family `APPROVAL_BLOCKED` / SKIPPED, such as "approval waiting / normal approval-blocked termination" — 05 spec.

### 🔐 Security

| Item | Result |
| --- | --- |
| AWS · DB · psql · Spring Boot · Lambda · Step Functions · ECS · SSM · EC2 · IAM · RDS execution | Kiro execution 0 · performed directly by operator |
| broker · KIS · crawler · automated BUY · automated SELL · fill sync · position sync · intraday monitor execution | 0 |
| Newly recorded raw secret · password · token · webhook URL | 0 |
| Newly recorded raw accountNo · account-id · actual ARN · public IP · broker_order_no | 0 |
| Newly recorded full raw payload · raw HTML · full SQL output | 0 |
| git add · commit · push execution | 0 |
| Placeholder policy | Use only `[REDACTED_ACCOUNT_NO]` · `[REDACTED_ARN]` · `[REDACTED_PUBLIC_IP]` · `[REDACTED_SECRET_ARN]` families |

## 2026-07-08 (daily-step1-11-ecs-timezone-tz-patch · stale-data root cause confirmed)

### 🧭 Summary

The cause of the DB latest-date stagnation at 2026-07-06 was confirmed as ECS Fargate UTC timezone + use of timezone-naive `datetime.now()`, and a short-term patch adding `TZ=Asia/Seoul` to crawler · preprocessor TaskDefinitions was applied. Step2A
and Step3 task revisions in State Machine `portfolio-paper-daily-step1-17-approval` were also updated to new revisions. Step1~11 was not rerun; freshness recovery would be validated in the next automatic execution at 2026-07-09 08:00 KST. Kiro updates documentation only in this session.

### ✅ Completed

- 🟠 Detected DB freshness anomaly

| Item | Value |
| --- | --- |
| Daily Step1~11 automatic execution (2026-07-08 08:00 KST) | SUCCEEDED |
| interest_price_raw latest | 2026-07-06 |
| interest_investorflow_raw latest | 2026-07-06 |
| interest_program_raw latest | 2026-07-06 |
| interest_shortsell_raw latest | 2026-07-06 |
| pre_total_market_daily_feature latest | 2026-07-06 |
| pre_total_stock_daily_feature latest | 2026-07-06 |
| decision.strategy_daily_run | run_date 2026-07-07 · data_date 2026-07-06 |
| execution.strategy_execution_plan | plan_date 2026-07-07 · NO_CANDIDATE |

- 🟠 Confirmed Step Functions / ECS TaskDefinition configuration

| Item | Value |
| --- | --- |
| State Machine (before change) | `portfolio-paper-daily-step1-17-approval` |
| Step2A task (before change) | `portfolio-paper-interest-crawler:7` |
| Step2A command | `python interest_crawler_daily_nongui.py` |
| Step3 task (before change) | `portfolio-paper-interest-preprocessor:1` |
| Step3 command | `python pre_daily.py` |
| runDate / targetDate / untilDate | No separate parameters passed to Step2A · Step3 |

- 🟠 Confirmed crawler CloudWatch log (Step2A result)

| Item | Value |
| --- | --- |
| interest_news target date | 2026-07-07 |
| interest_agency target date | 2026-07-06 · 2026-07-07 |
| interest_price target date | 2026-07-06 |
| interest_investorflow target date | 2026-07-06 |
| interest_marketbreadth target date | 2026-07-06 |

- 🟢 Excluded holiday misjudgment

| Item | Value |
| --- | --- |
| `interest_get_holidays.is_holiday(2026-07-07, "KR")` | False |
| 2026-07-07 KRX normal open | Confirmed |
| Judgment | Not caused by incorrectly treating 2026-07-07 as a market holiday |

- 🟢 Confirmed UTC timezone as root cause

| Item | Value |
| --- | --- |
| Actual Step2A execution time | 2026-07-08 08:01 KST |
| Same time in UTC | 2026-07-07 23:01 |
| Container default timezone | Observed as UTC |
| `datetime.now().date()` result | 2026-07-07 |
| Target date after applying `-1 day` | 2026-07-06 |
| Judgment | Container UTC + naive `datetime.now()` was the direct cause of stale target date |

- 🟢 Registered crawler:8 / preprocessor:2 (performed directly by operator)

| Item | Value |
| --- | --- |
| New TaskDefinition (1) | `portfolio-paper-interest-crawler:8` |
| New TaskDefinition (2) | `portfolio-paper-interest-preprocessor:2` |
| Added env | `TZ=Asia/Seoul` |
| Image · command · IAM Role · resource specs | No change |

- 🟢 Updated State Machine Step2A / Step3 task revisions (performed directly by operator)

| Item | Value |
| --- | --- |
| Target State Machine | `portfolio-paper-daily-step1-17-approval` |
| Step2A_RunInterestCrawlerNongui | crawler `:7` → `:8` |
| Step3_RunPreprocessor | preprocessor `:1` → `:2` |
| revisionId | `[REDACTED_REVISION_ID]` (raw value not recorded) |

- 🟢 crawler:8 / preprocessor:2 one-off TZ smoke succeeded

| Item | Value |
| --- | --- |
| crawler:8 TZ_ENV | `Asia/Seoul` |
| crawler:8 time.tzname | `('KST', 'KST')` |
| crawler:8 naive_yesterday | 2026-07-07 |
| preprocessor:2 TZ_ENV | `Asia/Seoul` |
| preprocessor:2 time.tzname | `('KST', 'KST')` |
| preprocessor:2 naive_yesterday | 2026-07-07 |

### 🔵 Evidence

- 🔵 Full detailed CloudWatch logs · Step Functions execution history · raw ECS RegisterTaskDefinition response · raw UpdateStateMachine response · raw SSM output are not pasted into this log. If needed, separate into each spec's `operation-notes.md`.
- 🔵 Raw actual Task ARN · execution ARN · account-id · secret ARN · state machine ARN · Task Definition ARN are not recorded. Only `[REDACTED_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_REVISION_ID]` placeholders are used.

### 📌 Follow-ups

- [ ] Observe automatic Daily Step1~11 execution at 2026-07-09 08:00 KST — verify in Step2A crawler logs that `interest_price` · `interest_investorflow` collect 2026-07-07.
- [ ] Verify on 2026-07-09 that preprocessor latest date advances to 2026-07-07 (`pre_total_market_daily_feature` · `pre_total_stock_daily_feature`).
- [ ] Verify on 2026-07-09 that decision `data_date` is 2026-07-07 (`decision.strategy_daily_run`).
- [ ] Verify 2026-07-09 09:01 Step 12~17 automatic execution flows normally without stale-data impact.
- [ ] Keep root fix (replace direct `datetime.now()` with timezone-aware helper) as a follow-up phase in 08 spec.

### ⚠️ Risks

| Item | Value |
| --- | --- |
| Risk ID | R-DATA-010 |
| Status | 🔴 Open (partial mitigation) |
| Summary | Insufficient non-GUI raw freshness. Short-term TZ patch applied on 2026-07-08 · awaiting next automatic-execution validation. |

| Item | Value |
| --- | --- |
| Risk ID | R-DATA-017 |
| Status | 🔴 Open |
| Summary | Separate risk for KRX GUI worker family. Distinct from the cause in this session. |

### 🔐 Security

| Item | Result |
| --- | --- |
| Kiro AWS · DB · psql · ECS · Step Functions · Lambda · SSM · EC2 execution | 0 (documentation update only) |
| ECS RegisterTaskDefinition · State Machine UpdateStateMachine · one-off TZ smoke | Performed directly by operator · Kiro execution 0 |
| broker · KIS · crawler · automated BUY · automated SELL · fill sync · position sync · intraday monitor execution | 0 |
| Newly recorded raw secret · password · token · webhook URL | 0 |
| Newly recorded raw account-id · actual ARN · public IP · broker_order_no · image digest · execution ARN | 0 |
| Raw State Machine revisionId record | 0 (`[REDACTED_REVISION_ID]` used) |
| Newly recorded full AWS CLI output | 0 |
| git add · commit · push execution | 0 |
| Placeholder policy | `[REDACTED*]` family only |

## 2026-07-08 (scheduler-inventory-and-holiday-guard-boost · operations-status-table preparation session)

### 🧭 Summary

This session reviewed Scheduler · Lambda · Step Functions holiday-guard paths while preparing the first operations-status table. The inventory of seven Schedulers was confirmed, and the existing holiday guards in the EC2 lifecycle · Daily scheduler dispatcher were verified only.
New holiday-guard reinforcement was added to the Intraday scheduler dispatcher and Daily Brief Slack Summary Builder. Actual Lambda deployment · env additions · Step Functions definition changes · dryRun validation were completed directly by the operator; Kiro modified documentation only.

### ✅ Completed

- 🟢 Reconfirmed Scheduler inventory

| Item | Value |
| --- | --- |
| Total Scheduler count | 7 |
| ENABLED | 7 |
| DISABLED | 0 |
| Timezone | Asia/Seoul |
| FlexibleTimeWindow | OFF |
| Weekday basis | MON-FRI |
| DLQ | None |

- 🟢 Status of seven automation line items

| Item | Value |
| --- | --- |
| Time | 07:50 |
| Target | Pre-market Slack |
| Status | ENABLED |

| Item | Value |
| --- | --- |
| Time | 07:50 |
| Target | EC2 start |
| Status | ENABLED |

| Item | Value |
| --- | --- |
| Time | 08:00 |
| Target | Step 1~11 |
| Status | ENABLED |

| Item | Value |
| --- | --- |
| Time | 09:01 |
| Target | Step 12~17 |
| Status | ENABLED |

| Item | Value |
| --- | --- |
| Time | 09:10~15:50 |
| Target | Intraday snapshot / evaluate |
| Status | ENABLED |

| Item | Value |
| --- | --- |
| Time | 15:50 |
| Target | Post-market Slack |
| Status | ENABLED |

| Item | Value |
| --- | --- |
| Time | 15:50 |
| Target | MarketConnector stop |
| Status | ENABLED |

- 🟢 Holiday-guard path review results

| Path | Status |
| --- | --- |
| EC2 lifecycle dispatcher (start · holidayCheck=true) | Existing guard confirmed |
| EC2 lifecycle dispatcher (stop) | Retain structure allowing stop even on holidays for safe shutdown |
| Daily scheduler dispatcher (Step 1~11 · Step 12~17) | Existing guard confirmed |
| Intraday scheduler dispatcher | New guard deployed · 2 env values added · dryRun validation complete |
| Daily Brief Slack workflow | Builder guard deployed · inserted state machine `CheckHolidaySkip` Choice · holiday dryRun confirmed |

- 🟢 Final judgment

| Item | Value |
| --- | --- |
| Scheduler itself | MON-FRI cron, not direct holiday exclusion |
| Actual holiday block point | Internal Lambda / State Machine guard |
| Confirmed blocked paths | EC2 start · Daily Step 1~11 · Daily Step 12~17 · Intraday · Daily Brief Slack |
| Structure retained | EC2 stop may run even on holidays |

### 🔵 Evidence

- 🔵 Intraday dispatcher dryRun validation observed `runDate=2026-07-08` as `skipped=false` / `MARKET_DAY_CANDIDATE`, and `runDate=2026-08-15` as `skipped=true` / `WEEKEND`. dryRun only; no actual SSM execution.
- 🔵 Daily Brief Slack workflow holiday dryRun confirmed entry into `SkipDailyBriefSlack` and no entry into `SendSlackNotifier`.
- 🔵 Detailed Lambda source / env values / state machine ASL / execution ARN / full dryRun response are not recorded in this log.

### 📌 Follow-ups

- [ ] Observe actual automatic receipt of 07:50 pre-market Slack · 15:50 post-market Slack on the next business day (Daily Brief mini workflow — real receipt still unconfirmed).
- [ ] Retain Holiday API backup-path / fallback-policy follow-up (after extending R-AUTO-028 mitigation).
- [ ] Continue follow-up for recent Step Functions execution review · DB latest-date query · View Daily Batch · OPS Mirror · first operations-status table.

### 🔐 Security

| Item | Result |
| --- | --- |
| AWS · DB · psql · Spring Boot execution | 0 |
| Lambda code deployment · env addition · Step Functions definition update | Kiro execution 0 · performed directly by operator |
| SSM · EC2 · broker · KIS · crawler execution | 0 |
| Newly recorded raw secret · password · token · webhook URL | 0 |
| Newly recorded raw account no · actual ARN · public IP · broker_order_no · image digest · Lambda zip SHA256 · execution ARN | 0 |
| Newly recorded full AWS CLI output | 0 |
| git add · commit · push execution | 0 |
| Placeholder policy | `[REDACTED*]` family only |
## 2026-07-07 (root-docs-readability · Kiro execution-overhead improvement · _common round-10 result summary)

### 🧭 Summary

This session attempted a readability cleanup of the three `.kiro` root documents (README · WORKLOG · CHANGELOG). No actual AWS operations or feature implementation were performed; the work was limited to document editing and a review of the Kiro working method. The target style was the 2026-07-01 CHANGELOG style (table-centric · short bullets · Security tables). In parallel, the result summary for `_common` round 10 was recorded in this log.

### ✅ Completed

- 🟢 Defined readability targets for the three root documents

| Item | Value |
| --- | --- |
| Targets | `.kiro/README.md` · `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` |
| Target style reference | 2026-07-01 CHANGELOG section |
| Target format | Table-centric · short bullets · Security tables |
| Actual modification scope | Limited to the three root documents |
| Out of scope | `.kiro/specs/**` · README/CHANGELOG/docs/worklog/AGENTS.md of the 8 MS |

- 🟢 Recorded `_common` round-10 result summary

| Item | Value |
| --- | --- |
| Targets | `_common/risk-register.md` · `_common/followups-overview.md` · `_common/operator-decisions.md` |
| risk-register.md over300 | 134 → 9 |
| followups-overview.md over300 | 210 → 70 |
| operator-decisions.md over300 | 240 → 2 |
| tableO300 | 0 |
| Encoding | UTF-8 No BOM retained |
| Newly introduced raw sensitive information | 0 |
| Judgment | Long-form Details migration complete |

- 🟠 Confirmed Kiro execution overhead

| Item | Value |
| --- | --- |
| Delay point | Violation-matrix organization step for the three root files |
| Cause (1) | ViolationRecord matrix generation |
| Cause (2) | Recording content_hash |
| Cause (3) | safety_exception classification |
| Cause (4) | Sub-agent invocation attempts |
| Cause (5) | Scan attempts based outside the workspace / under TEMP |
| Cause (6) | Long PowerShell one-liners · trust prompts |
| Judgment | Audit pipeline was excessive relative to localized document editing |

- 🟠 Response direction (documentation only · rule change handled in a separate session)

| Item | Value |
| --- | --- |
| Principle retained | Rule 0 readability-first principle |
| Default application scope | Limited to newly written / directly modified sentences · tables · bullets |
| Existing-document-wide violations | Unless explicitly requested, do not modify; record only as follow-up candidates |
| Full-audit items | ViolationRecord matrix · content_hash · complete before/after audit only when explicitly requested |
| Actual AGENTS.md modification | Not performed in this session · separate follow-up session |

### 🔵 Evidence

- 🔵 Detailed edits from `_common` round 10 can be referenced in the files themselves and in the round-10-related items of `.kiro/CHANGELOG.md`. This session records only the root-log summary.
- 🔵 No actual AWS resource creation/modification/deletion was performed; only document editing and work-method review.

### ⚠️ Risks

- 🟠 The original readability-cleanup plan for the three root documents was not fully applied because of audit-pipeline overhead. Remaining line-level violations are follow-up targets.
- 🟠 Because the scope of Rule 0 is not explicitly defined, full-document scans may recur in future sessions. A separate AGENTS.md reinforcement session is planned.

### 📌 Follow-ups

- [ ] Add to AGENTS.md Rule 0 that "default application scope = newly written / directly modified portions" (separate session).
- [ ] Add an exception that full scans / ViolationRecord matrix / content_hash are performed only when explicitly requested (separate session).
- [ ] Recalculate the remaining readability-cleanup targets for the three root documents and retry with localized edits.
- [ ] Decide whether to further reduce remaining over300 lines in the three `_common` files (risk-register 9 · followups-overview 70 · operator-decisions 2).

### 🔐 Security

- 🟢 Execution categories (document-only changes · all 0)

| Item | Value |
| --- | --- |
| AWS · DB · psql · Spring Boot execution | 0 |
| Slack webhook · Step Functions · Lambda · ECS execution | 0 |
| SSM · EC2 · broker · KIS · crawler execution | 0 |
| Automated BUY · automated SELL · fill sync · position sync execution | 0 |
| intraday monitor · live cutover execution | 0 |

- 🟢 Git operations

| Item | Value |
| --- | --- |
| Write operations (`git add` · `git commit` · `git push`) | 0 |
| Rollback operations (`git reset` · `git checkout` · `git restore`) | 0 |
| Status-check command | Not executed |

- 🟢 Newly recorded raw sensitive information (all 0)

| Item | Value |
| --- | --- |
| secret · password · token · webhook URL | 0 |
| KIS app key · KIS app secret | 0 |
| Account number · account-id · actual ARN | 0 |
| Actual public IP · broker_order_no · image digest full sha256 | 0 |
| Placeholder policy | `[REDACTED*]` family only |

## 2026-07-06 (spec-docs-readability-sessions · parallel readability improvement across four Sessions for 01~09 spec subfolders)

### 🧭 Summary

Documents under `.kiro/specs/` 01~09 were divided into four groups, Sessions A~D, and readability improvements were performed in parallel. This was a document-editing-only session with no actual AWS operations or feature implementation.

- Each Session used a separate scope and individual goals (reduce >300 · >500-character lines · shorten table rows / bullets · organize placeholder coverage).
- No critical fact loss · 0 newly recorded raw secret · actual ARN · account-id · public IP · broker_order_no values.

### ✅ Completed

- 🟢 Session A — readability improvement for specs 01~03

| Item | Value |
| --- | --- |
| Front scope | 01-aws-migration-foundation · 02-aws-network-and-rds |
| Back scope | 03-marketconnector-ec2 |
| Modified files | 13 |
| Files preserved unchanged | 5 |
| >300 · >500 lines | Achieved 0 across all 18 files |
| Extra-long table rows · bullets | 0 |
| Newly added raw secret · ARN · account-id · public IP | 0 |

- 🟢 Session B — readability improvement for specs 04~05

| Item | Value |
| --- | --- |
| Front scope | 04-strategy-batch-stepfunctions / operation-notes.md |
| Middle scope | 05-port-view-ecs-and-runbook / operation-notes.md |
| Back scope | 05 runbook.md · validation-checklist.md |
| New front block in 04 | Automation Lineup Dashboard |
| New back block in 04 | Date index |
| New block in 05 | Backend matrix by View execution location |
| >500 lines | Achieved 0 in major 04 / 05 documents |
| Table rows >300 · >500 | 0 |
| Bullets >300 · >500 | 0 |
| Newly added raw secret | 0 |

- 🟢 Session C — readability improvement for 06 secrets-and-iam spec documents

| Item | Value |
| --- | --- |
| Front scope | requirements.md · design.md · tasks.md |
| Back scope | operation-notes.md · runbook.md · validation-checklist.md |
| Modified files | 6 |
| gt300 (>300-character lines) | 39 → 0 |
| gt500 (>500-character lines) | 9 → 0 |
| Placeholder Coverage | Cleanup complete |
| Security-policy weakening | None |
| Newly recorded raw sensitive information | 0 |

- 🟢 Session D — readability improvement for 08 / 09 data · research spec documents

| Item | Value |
| --- | --- |
| Front scope | 08 requirements.md · design.md · tasks.md · operation-notes.md |
| Back scope | 09 operation-notes.md |
| Modified files | 5 |
| Editing method | additive-only |
| New front blocks | Role Split · Runtime Role Split · Task Section Overview |
| New back blocks | Open Risks & Next Checks · Key Fact Preservation |
| Lines over 500 characters | 0 |
| Fact loss (stale data · latest_trade_date · KRX worker · research batch) | 0 |
| Newly added raw secret | 0 |

### 🔵 Evidence

- 🔵 Detailed modified-file information for each Session can be found in operation-notes.md · runbook.md · validation-checklist.md within each spec folder (these subdocuments were direct modification targets in this session and should be referenced first).
- 🔵 This session was dedicated to readability improvements for `.kiro/specs`, not to actual AWS operations or feature implementation.

### ⚠️ Risks

- No new risks. Alignment with R-DOCS-001 (prohibition on recording plaintext secrets) retained.

### 📌 Follow-ups

- [ ] Separately decide whether readability improvements are needed for the 07 spec subfolder.
- [ ] Decide on a follow-up session for spec 10 and later specs.
- [ ] Decide whether to append a small baseline-metrics section for Sessions A~D to each spec operation-notes.md.

### 🔐 Security

- 🟢 Execution categories (document-only changes · all 0)

| Item | Value |
| --- | --- |
| AWS · DB · psql · Spring Boot execution | 0 |
| Slack webhook · Step Functions · Lambda · ECS execution | 0 |
| SSM · EC2 · broker · KIS execution | 0 |
| Crawler · Selenium execution | 0 |
| Automated BUY · automated SELL · fill sync · position sync execution | 0 |
| intraday monitor · live cutover execution | 0 |

- 🟢 Newly recorded raw sensitive information (all 0)

| Item | Value |
| --- | --- |
| secret · password · token · webhook URL | 0 |
| KIS app key · KIS app secret | 0 |
| Account number · account-id · actual ARN | 0 |
| Actual public IP · broker_order_no · image digest full sha256 | 0 |
| Placeholder policy | `[REDACTED*]` family only |

## 2026-07-06 (kiro-common-docs-readability-6-7 · recovery-oriented rounds 6~7)

### 🧭 Summary

Recovery-oriented work for rounds 6~7 was performed on documents under `.kiro/specs/_common/`. This is recorded separately as a recovery-only session between round 8 line-density reduction (separate 2026-07-02 section) and round 9 structural reorganization (later 2026-07-06 section).

- Recovered mojibake in display text in `operator-decisions.md`.
- Recovered some wrap artifacts in `risk-register.md`.
- Checked for single-character bullets · broken Korean text · broken-wrap candidates.
- Retained UTF-8 No BOM policy · 0 newly recorded raw secrets.

### ✅ Completed

- 🟢 `_common/operator-decisions.md`

| Item | Value |
| --- | --- |
| Action | Recovered mojibake in display text |
| Fact loss | None |
| Decision ID · Status · [REDACTED*] count | Preserved at or above baseline |

- 🟢 `_common/risk-register.md`

| Item | Value |
| --- | --- |
| Action | Recovered some wrap artifacts |
| Front checks | Single-character bullets · broken Korean text |
| Back checks | Identified broken-wrap candidates |
| Fact loss | None |
| Risk ID · Status · [REDACTED*] count | Preserved at or above baseline |

### 🔵 Evidence

- 🔵 Document metrics from rounds 6 / 7 were absorbed into the later round-9 baseline document (see 2026-07-06 kiro-common-docs-readability-9 section).
- 🔵 This session performed only localized document editing with no actual AWS operations.

### ⚠️ Risks

- No new risks. Broken-wrap candidates remain under observation as follow-up items.

### 📌 Follow-ups

- [ ] If additional broken-wrap candidates are found in future editing sessions, recover them locally.
- [ ] Recheck if mojibake / wrap artifacts recur.

### 🔐 Security

- 🟢 Execution · newly recorded raw sensitive information (all 0)

| Item | Value |
| --- | --- |
| AWS · DB · Slack · crawler · broker · KIS execution | 0 |
| Step Functions · Lambda · SSM · psql · Spring Boot execution | 0 |
| secret · password · token · webhook · actual ARN · account-id | 0 |
| public IP · broker_order_no · image digest full sha256 raw values | 0 |
| Save encoding for the 6 target documents | UTF-8 No BOM retained |

## 2026-07-06 (kiro-common-docs-readability-9 · round 9 · structural reorganization of 6 _common documents)

### 🧭 Summary

Six documents under `.kiro/specs/_common/` were reorganized into a predictable three-level fixed flow similar to `.kiro/WORKLOG.md` and `.kiro/CHANGELOG.md`.

- Flow: top Purpose/Dashboard/Summary · middle Short Index · bottom Details/Appendix/Historical Notes/Usage Notes.
- Success criterion was a predictable structure, not line-count reduction.
- Added a `## Purpose` header at the top of all six documents · renamed safety-constraint sections to `## Security Notes` and moved them to the end of each file.
- The pre-existing UTF-8 BOM in `risk-register.md` was handled with Option A (removal).
- Decision ID · Risk ID · dates · USD · Status · `[REDACTED*]` count were all preserved at or above baseline.

### ✅ Completed

- `_common/operator-decisions.md` — organized the following items.
   - Added Purpose.
   - Renamed Decision Summary → Decision Dashboard (marked as requiring recount).
   - Renamed Detailed Decisions → Decision Index.
   - Moved Deferred Decisions before Decision Details.
   - Demoted duplicate H2 (Compute/Service Placement) to H3.
   - Renamed this-spec safety constraints → Security Notes and moved them to file end.
   - Moved Decision Update Rules after Change Log Details.
   - Aligned H2 order to target.
- `_common/risk-register.md` — organized the following items.
   - Removed pre-existing BOM using Option A (186458→186455 bytes).
   - Added Purpose.
   - Renamed Risk Table → Risk Index.
   - Demoted column-definition/status-definition H2 headings to H3.
   - Added Accepted/Closed Risks summary section (`R-AUTO-024` · `R-AUTO-027`).
   - Renamed rules for updates on additional identification → Risk Update Rules.
   - Renamed this-document safety constraints → Security Notes.
- `_common/followups-overview.md` — organized the following items.
   - Added Purpose.
   - Consolidated environment model / execution order / each follow-up Spec summary / dependency diagram under H3 headings below Spec Roadmap H2.
   - Added Update Rules.
   - Renamed common work-scope restrictions → Security Notes.
- `_common/aws-resource-glossary.md` — organized the following items.
   - Added Purpose.
   - Renamed category-based table of contents → Category TOC.
   - Added Glossary H2.
   - Demoted 38 individual-service H2 headings to H3 (remaining H2: Purpose · Category TOC · Glossary · Usage Notes · Update Rules · Security Notes).
   - Added Usage Notes (documented priority migration policy for EventBridge Scheduler · Lambda · IAM Role · ECS/Fargate · Step Functions).
   - Added Update Rules.
   - Renamed this-spec safety constraints → Security Notes.
- `_common/ms-aws-service-decision-matrix.md` — organized the following items.
   - Added Purpose (explicitly states service-selection conclusions are unchanged).
   - Renamed chapter 5 → MS Decision Cards.
   - Renamed chapter 6 → Portfolio Appeal Notes.
   - Added Rejected/Deferred Services H2 and absorbed chapters 7/8/9 as subordinate H3 sections.
   - Absorbed chapter 10 under Appendix as H3.
   - Demoted Evidence Details H2 to H3.
   - Added Security Notes.
- `_common/cost-simulation.md` — minimal changes. Added Purpose. Renamed chapter 8 safety constraints → Security Notes. Retained 0 lines over 300 characters · no new cost figures · USD count remains 3.

### 🔵 Evidence

- 🔵 Fact-loss comparison before/after restructuring and line-count organization — see Task 9 validation results in `.kiro/specs/kiro-common-docs-readability-9/tasks.md`.
- 🔵 Lines per file · count of lines over 300 characters (Baseline → After)

| File | Value |
| --- | --- |
| operator-decisions.md | 2045 · 238 → 2057 · 240 |
| followups-overview.md | 1443 · 209 → 1463 · 210 |
| risk-register.md | 1062 · 133 (pre-existing BOM) → 1093 · 134 (BOM removed) |
| aws-resource-glossary.md | 493 · 35 → 527 · 36 |
| ms-aws-service-decision-matrix.md | 1065 · 41 → 1100 · 42 |
| cost-simulation.md | 445 · 0 → 447 · 0 |

### ⚠️ Risks

- 🟠 Move table-cell lines over 300 characters into Details

| Item | Value |
| --- | --- |
| Action in this session | Surface-level cleanup only |
| Migration of individual Details content | Deferred to follow-up session |
| Method | Planned sequential move with no fact-loss risk |

- 🟠 Unconsolidated items in `operator-decisions.md`

| Item | Value |
| --- | --- |
| Duplicate OD-MS-028 row | Not consolidated |
| Decision Summary count recount | Carried over as Review Needed |

- 🟠 `ms-aws-service-decision-matrix.md` chapters 1 · 2 · 3 · 4

| Item | Value |
| --- | --- |
| Location | Retained immediately after Final Recommendation Summary |
| Physical move under Appendix | Avoided for anchor stability |

### 📌 Follow-ups

- [ ] Move remaining items with 3+ sentences in a table cell · 120+ characters · 4+ slash chains individually into Decision Details / Risk Details / Usage Notes (next session).
- [ ] Decide whether to consolidate duplicate OD-MS-028 row in `operator-decisions.md`.
- [ ] Recount Decision Summary count in `operator-decisions.md`.
- [ ] Physically move chapters 1 · 2 · 3 · 4 of `ms-aws-service-decision-matrix.md` under Appendix.

### 🔐 Security

- 🟢 Execution categories (document-only changes · all 0)

| Item | Value |
| --- | --- |
| AWS · psql · Spring Boot execution | 0 |
| Slack webhook · Step Functions · Lambda · ECS execution | 0 |
| SSM · EC2 · broker · KIS execution | 0 |
| Crawler · Selenium execution | 0 |
| Automated BUY · automated SELL · fill sync · position sync execution | 0 |
| intraday monitor · live cutover execution | 0 |

- 🟢 Git operations

| Item | Value |
| --- | --- |
| Front write operations | `git add` · `git commit` |
| Back write operations | `git rm` · `git mv` · `git push` |
| Write operations executed | 0 |
| Front rollback operations | `git checkout` · `git reset` |
| Back rollback operations | `git stash` · `git restore` |
| Automatic rollback execution | 0 |
| Status-check command | Used `git status --short` only |

- 🟢 Newly recorded raw sensitive information (all 0)

| Item | Value |
| --- | --- |
| secret · password · token · webhook URL | 0 |
| KIS app key · KIS app secret | 0 |
| Account number · account-id · actual ARN | 0 |
| Actual public IP · broker_order_no · image digest full sha256 | 0 |
| Placeholder policy | `[REDACTED*]` family only |

- 🟢 Encoding

| Item | Value |
| --- | --- |
| Save encoding for the 6 target documents | Standardized on UTF-8 No BOM |
| `risk-register.md` pre-existing BOM | Handled with Option A (removal) |

## 2026-07-03 (Step Functions execution-history OPS mirror foundation complete)

### 🧭 Summary

The foundation for mirroring AWS Step Functions execution history into OPS tables was completed.

- Target tables: `ops.strategy_daily_batch_run` · `ops.strategy_daily_batch_step_log`.
- Added `AWS_STEPFUNCTIONS` to `run_type` · used a dedicated least-privilege role · used Lambda `portfolio-daily-batch-ops-recorder`.
- RECORD_START / RECORD_STEP / RECORD_SUCCESS smoke passed · commit smoke `batchRunId=53` · confirmed OPS table records after Step 12 approval-blocked smoke.
- Established the foundation for the View Daily Batch screen to query AWS Step Functions run history (aligns with the cause of Daily Batch history mismatch confirmed on the 2026-07-02 View screen).

### ✅ Completed

1. OPS mirror foundation complete
   1) DB schema alignment:complete
       - Use `ops.strategy_daily_batch_run` · `ops.strategy_daily_batch_step_log`
       - Added `AWS_STEPFUNCTIONS` to `run_type`
   2) Execution-actor separation:complete
       - Lambda `portfolio-daily-batch-ops-recorder`
       - Adopted dedicated least-privilege role `ops_recorder_app`
   3) Smoke passed:complete
       - Three smokes: RECORD_START · RECORD_STEP · RECORD_SUCCESS
       - commit smoke `batchRunId=53`
       - Confirmed OPS table records after Step 12 approval-blocked smoke

### 🔵 Evidence

- 🔵 Decision/risk/follow-up details

| Document | Location |
| --- | --- |
| operator-decisions | `OD-DB-012` · `OD-MS-039` |
| risk-register | `R-AUTO-038` |
| followups-overview | 2026-07-03 item |

### ⚠️ Risks

- 🟢 New risk `R-AUTO-038` registered · initial status `Mitigated` (smoke passed · real-operation observation follow-up).
- 🟠 View Daily Batch screen ↔ OPS table connection remains a follow-up phase (View query integration required).

### 📌 Follow-ups

- [ ] Update View Daily Batch screen to query `ops.strategy_daily_batch_run` runs with `AWS_STEPFUNCTIONS`.
- [ ] Observe OPS mirror consistency during a real automated Step 1~17 run.
- [ ] Recheck least-privilege alignment for `ops_recorder_app` role (long-term retention decision).

### 🔐 Security

- 🟢 Kiro modified documentation only. 0 executions · 0 broker orders · 0 aws-live work · 0 raw secrets recorded.
- 🟢 AWS CLI · psql · Spring Boot · Slack webhook · Step Functions · Lambda · ECS · SSM · KIS API execution: 0.
- 🟢 Newly recorded raw secret · password · token · webhook URL · account number · actual ARN · public IP · broker_order_no: 0 · `[REDACTED*]` placeholder policy retained.

## 2026-07-02 (portfolio-event-notifier Slack wording improvement + View query-screen stabilization smoke)

### 🧭 Summary

Slack wording improvements for successful `portfolio-event-notifier` messages and the baseline smoke for View query screens both passed.

- APPROVAL_REQUIRED · DAILY_EXECUTION_SUCCESS successful formatter smoke passed.
- Baseline smoke passed for seven View screens (`/dashboard` · `/balance-summary` · `/positions` · `/orders` · `/strategy/execution/plans` · `/strategy/reports/latest` · `/daily-batch`).
- Latest balance snapshot `id=357` · `as_of_date=2026-07-02` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505` · holdings 0.

### ✅ Completed

1. Slack wording improvement
   1) APPROVAL_REQUIRED formatter:complete
       - Standardized title to `[Daily 검증] 성공`
       - Removed "승인 대기" line
   2) DAILY_EXECUTION_SUCCESS formatter:complete
       - Standardized title to `[Daily 실행] 성공`
       - Standardized `SUCCESS` display to `성공`
   3) Smoke passed:complete
       - Confirmed receipt for both successful formatters
       - DAILY_EXECUTION_FAILED formatter requires separate smoke · remains follow-up

2. View query-screen stabilization smoke
   1) Baseline smoke passed for seven screens:complete
       - `/dashboard` · `/balance-summary` · `/positions` · `/orders`
       - `/strategy/execution/plans` · `/strategy/reports/latest` · `/daily-batch`
   2) Latest balance snapshot alignment:complete
       - `id=357` · `as_of_date=2026-07-02`
       - `total_eval_amount=8,706,505` · `cash_balance=8,706,505` · holdings 0
   3) Confirmed old positions not exposed in /positions
       - Even if historical `connector_position_snapshot` rows exist, they are not exposed on screen for the latest as-of date
   4) Daily Batch history-mismatch cause organized:confirmed
       - Not a View query bug
       - Classified as absence of Step Functions execution-history mirror (addressed in 2026-07-03 OPS mirror follow-up)

### 🔵 Evidence

- 🔵 Detailed evidence links

| Document | Location |
| --- | --- |
| followups-overview | 2026-07-02 item (Slack · View) |
| risk-register | Remaining risks in Slack formatter family |

### ⚠️ Risks

- 🟠 DAILY_EXECUTION_FAILED formatter remains without a separate smoke pass · wording validation required when a real failure event is exposed.
- 🟠 Daily Batch history mismatch is not caused by View · requires revalidation after Step Functions run mirror (2026-07-03) is integrated.

### 📌 Follow-ups

- [ ] Run DAILY_EXECUTION_FAILED formatter smoke.
- [ ] Update Daily Batch screen to query OPS mirror `AWS_STEPFUNCTIONS` run history.
- [ ] Maintain regular smoke sessions for the seven View screens.

### 🔐 Security

- 🟢 Kiro modified documentation only. 0 executions · 0 broker orders · 0 aws-live work · 0 raw secrets recorded.
- 🟢 AWS CLI · psql · Spring Boot · Slack webhook · Step Functions · Lambda · ECS · SSM · KIS API execution: 0.
- 🟢 Newly recorded raw sensitive information: 0 · `[REDACTED*]` placeholder policy retained.

## 2026-07-02 (kiro-common-docs-readability · round 8 · reducing line density across 6 _common documents)

### 🧭 Summary

Round 8 readability improvements were performed on six documents under `.kiro/specs/_common/`.

- Dashboard / Summary / At a Glance areas were retained, while long prose inside table cells and paragraphs was split into bullets to reduce maximum line length and counts of >500 / >300-character lines.
- `cost-simulation.md` reached 0 lines over 300 characters.
- Decision ID · Risk ID · dates · status labels · USD figures · `[REDACTED*]` placeholder count were all preserved.

### ✅ Completed

- `_common/cost-simulation.md` — restructured main paper/live realistic configuration bullets into a table, split opening 3 lines. >300=1→0.
- `_common/ms-aws-service-decision-matrix.md` — split multiple 500+ character descriptions in Evidence Details 4.1 · 4.2 · 4.6 (2026-06-23 · 6-24) into multi-line bullets. >500=2→0.
- `_common/followups-overview.md` — split 500+ character descriptions in 2026-06-13 · 2026-06-17 · 2026-06-18 Historical Notes into bullets.
- `_common/operator-decisions.md` — split 500+ character descriptions in Change Log Details 2026-06-16 · 2026-06-23 · 2026-06-29 (2)·(3) into bullets. >500=6→3.
- `_common/risk-register.md` — no edits in this session (other targets prioritized).
- `_common/aws-resource-glossary.md` — no edits in this session (other targets prioritized).

### 🔵 Evidence

- 🔵 Metrics before → after (based on this session)

| File | Value |
| --- | --- |
| `operator-decisions.md` | 2020 → 2045 lines · >500 6 → 3 · max 557 → 529 |
| `followups-overview.md` | 1420 → 1443 lines · >500 19 → 17 · max 544 → 530 |
| `ms-aws-service-decision-matrix.md` | 973 → 1065 lines · >500 2 → 0 · max 514 → 488 |
| `cost-simulation.md` | 413 → 445 lines · >300 1 → 0 · max 320 → 296 |
| `risk-register.md` · `aws-resource-glossary.md` | unchanged |

- 🔵 Fact preservation (all satisfy before ≤ after)

| Item | Value |
| --- | --- |
| Decision ID · Risk ID | before ≤ after |
| date · status | before ≤ after |
| USD · REDACTED placeholder count | before ≤ after |

### ⚠️ Risks

- No new risks. Alignment with R-DOCS-001 (no plaintext secret recording) retained.

### 📌 Follow-ups

- [ ] Shorten table cells in `_common/risk-register.md` and move content into Risk Details (incomplete in round 8 · candidate for round 9).
- [ ] Align 5-field template in `_common/aws-resource-glossary.md` and move date-based evidence (incomplete in round 8 · candidate for round 9).
- [ ] Further move rationale with 3+ sentences inside Detailed Decisions rows in `_common/operator-decisions.md` to Change Log Details (partially complete in round 8 · candidate for round 9).
- [ ] Rebalance Historical Notes in `_common/followups-overview.md` to 3~5 bullets per date (partially complete in round 8 · candidate for round 9).
- [ ] Decide policy for pre-existing BOM in `_common/risk-register.md` (retain as-is / separate follow-up decision).

### 🔐 Security

Documentation only. 0 executions · 0 broker orders · 0 aws-live work · 0 raw secrets recorded. AWS CLI · boto3 · psql · Spring Boot · Slack webhook · Step Functions · Lambda · ECS · KIS API calls: 0. No new expansion of `[REDACTED*]` placeholder values (before/after count identical).

## 2026-07-01 (June actual AWS cost analysis · VPC Endpoint savings alignment)

### 🧭 Summary

June aws-paper actual cost review and VPC Endpoint savings actions were organized.

- June pre-tax `135.40 USD` · tax `13.55 USD` · approximately `148.95 USD` including tax.
- Month-end estimate of `180 USD` was judged conservative but reasonable for July full automation.
- The primary cost driver was VPC Endpoint (`70.69 USD` of total VPC cost `74.24 USD`).
- SSM endpoint removal complete · `ecr.api` · `ecr.dkr` · `logs` · `secretsmanager` endpoints reduced from 2 AZ → 1 AZ.
- Estimated monthly savings approximately `56.16 USD` · further endpoint deletion deferred due to operational risk.

### ✅ Completed

1. Confirm June actual cost
   1) Total-cost alignment:complete
       - Pre-tax `135.40 USD` · tax `13.55 USD` · approximately `148.95 USD` including tax
   2) Month-end estimate alignment:confirmed
       - 180 USD judged conservative but reasonable for July full automation
   3) Top cost-driver alignment:confirmed
       - VPC Endpoint `70.69 USD` of VPC cost `74.24 USD`

2. VPC Endpoint savings actions
   1) Remove SSM endpoint:complete
       - Removed after minimizing dependency on the SSM endpoint for EC2 · Fargate access paths
   2) Reduce 4 endpoint types from 2 AZ → 1 AZ:complete
       - `ecr.api` · `ecr.dkr` · `logs` · `secretsmanager`
   3) Estimated-savings alignment:confirmed
       - Approximately `56.16 USD` / month
   4) Further endpoint deletion deferred:confirmed
       - Retained as follow-up because savings are small relative to operational risk

### 🔵 Evidence

- 🔵 Detailed evidence link

| Document | Location |
| --- | --- |
| cost-simulation | `_common/cost-simulation.md` June actuals · savings alignment |

### ⚠️ Risks

- 🟠 Further VPC Endpoint deletion deferred · reassess operational risk if needed.
- 🟠 After entering July full automation, a measurement session is needed to compare actual monthly cost against the 180 USD estimate.

### 📌 Follow-ups

- [ ] Append July actual cost to `cost-simulation.md` once finalized.
- [ ] Regularly observe normal SSM · ECS · Lambda operation after reducing `ecr.api` · `ecr.dkr` · `logs` · `secretsmanager` to 1 AZ.
- [ ] Define conditions for reevaluating further endpoint deletion (operational risk vs savings).

### 🔐 Security

- 🟢 Kiro modified documentation only. 0 executions · 0 broker orders · 0 aws-live work · 0 raw secrets recorded.
- 🟢 AWS CLI · psql · Spring Boot · Slack webhook · Step Functions · Lambda · ECS · SSM · KIS API execution: 0.
- 🟢 Actual USD totals · endpoint names recorded only as evidence values · 0 raw account number · payment method · billing account id records.

## 2026-07-01 (kiro-common-docs-readability spec execution · Dashboard-first restructuring of 6 _common documents)

### 🧭 Summary

Executed the `kiro-common-docs-readability` spec and added operator-daily-use Dashboard/Summary sections at the top of six single-source-of-truth documents under `.kiro/specs/_common/`.

- Existing detailed content was not deleted and was retained behind the Dashboard.
- `operator-decisions.md` already had the required structure (`Status Legend` → `Decision Summary` → `At a Glance` → `Detailed Decisions` → `Change Log`), so it was preserved without restructuring.
- 0 actual AWS resource · MS source · lower-level spec-folder changes.

### ✅ Completed

- 🟢 `_common/risk-register.md`

| Item | Value |
| --- | --- |
| New addition | 4 groups in `Risk Dashboard` |
| Front groups | Open High · Mitigated High |
| Back groups | Newly added · Operator action required |
| Original retained | `Risk Table` · `Risk Details` |

- 🟢 `_common/followups-overview.md`

| Item | Value |
| --- | --- |
| New addition | 5 groups in `Current Follow-up Dashboard` |
| Front groups | Now · Next · Later |
| Back groups | Blocked · Done recently |
| Original retained | Execution order · date-based follow-up notes |

- 🟢 `_common/ms-aws-service-decision-matrix.md`

| Item | Value |
| --- | --- |
| New addition | `Final Recommendation Summary` 5-column table |
| Table size | Fixed order of 8 MS |
| Additional summary | Summary of non-recommended services |
| Original retained | chapters 1 ~ 10 |

- 🟢 `_common/cost-simulation.md`

| Item | Value |
| --- | --- |
| New addition | `Cost Dashboard` |
| Dashboard front | paper · live realistic monthly estimated cost |
| Dashboard back | Top 5 cost drivers · Top 5 savings decisions |
| Original retained | chapters 1 ~ 8 |

- 🟢 `_common/aws-resource-glossary.md`

| Item | Value |
| --- | --- |
| New addition | 7 category-based TOC groups |
| Front categories | Network · Compute · Database |
| Middle categories | Security/IAM/Secrets · Orchestration |
| Back categories | Observability · Storage/Artifact |
| Original retained | All glossary entries |

- 🟢 `_common/operator-decisions.md`

| Item | Value |
| --- | --- |
| Result | Confirmed existing structure alignment · no file changes |
| Front structure | Status Legend → Decision Summary → At a Glance |
| Back structure | Detailed Decisions → Change Log |

### 🔵 Evidence

- [`_common/risk-register.md`](specs/_common/risk-register.md) `Risk Dashboard` section
- [`_common/followups-overview.md`](specs/_common/followups-overview.md) `Current Follow-up Dashboard` section
- [`_common/ms-aws-service-decision-matrix.md`](specs/_common/ms-aws-service-decision-matrix.md) `Final Recommendation Summary` section
- [`_common/cost-simulation.md`](specs/_common/cost-simulation.md) `Cost Dashboard` section
- [`_common/aws-resource-glossary.md`](specs/_common/aws-resource-glossary.md) category-based TOC section

### ⚠️ Risks

0 new risks. 0 changes to existing risk mitigation / detection / status.

### 📌 Follow-ups

- [ ] Moving detailed content behind Detail sections (long mitigation text · table cells with 3+ sentences · slash chains of 4+) is the responsibility of a follow-up phase. This session completed only the Dashboard-first top placement.
- [ ] Review exposure of links to each `_common` Dashboard when integrating View screens in the follow-up phase of 05 spec port-view-ecs-and-runbook.
- [ ] Abbreviation of Historical Notes / Change Log · separation of Appendix (EKS · Lambda · Beanstalk / App Runner) is the responsibility of a follow-up session.
- [ ] Keep relative-path link alignment for operation-notes (`../<spec>/operation-notes.md`) as a periodic-check item.

### 🔐 Security

- Document modifications limited to 5 md files under `.kiro/specs/_common/` · 1 `.kiro/WORKLOG.md` file.
- AWS CLI · boto3 · psql · Spring Boot execution: 0.
- broker · KIS · Selenium · KRX crawler execution: 0.
- Automated BUY · automated SELL · fill sync · position sync · intraday monitor execution: 0.
- Changes to source · README · AGENTS.md · CHANGELOG · docs/worklog in the 8 MS repositories: 0 (spec area only).
- Changes to lower-level spec folders (outside `_common/`): 0.
- Newly recorded raw secret · password · token · webhook URL · account number · actual ARN · public IP · broker_order_no: 0.
- No automatic rollback · write-type git commands (`git add` · `git commit` · `git rm` · `git mv` · `git push`) executed: 0.

## 2026-07-01 (paper Daily automation first full ON + Step 12~17 automatic execution ENABLED)

### 🧭 Summary

As of 2026-07-01, aws-paper Daily automation reached its first full-ON state. Both the Step 1~11 Scheduler and Step 12~17 Scheduler are ENABLED, and when candidates exist, the 09:01 KST Step 12~17 path proceeds automatically without manual approval from View.

> **Safety note** — This change is limited to aws-paper. **No changes to aws-live automatic BUY · SELL policy**. Live retains the candidate + manual-approval-first policy.

### ✅ Completed

- One operator-triggered manual Step 12~17 execution passed. Status **`SUCCEEDED`** · received all 3 Slack types (07:50 pre-market Slack · 08:24 approval-required Slack · Step 12~17 success Slack).

| Item | Value |
| --- | --- |
| executionName | `port-manual-daily-step12-17-20260701-043747` |
| state machine | `portfolio-paper-daily-step12-17-approval` |
| start | `2026-07-01T13:37:47.856+09:00` |
| stop | `2026-07-01T13:40:41.212+09:00` |
| execution history | `ExecutionSucceeded` |

- DB pre-check · after-check passed. Step 12~17 judgment: **safe NO_TARGET termination**.

| Item | Value |
| --- | --- |
| 2026-07-01 `strategy_execution_order` | 0 |
| REQUESTED strategy orders | 0 |
| active `connector_order_request` | 0 |
| today's `connector_order_request` | 0 |
| latest `connector_balance_snapshot` | `id=321` |
| `as_of_date` | `2026-07-01` |
| `total_eval_amount` | `8,706,505` |
| `cash_balance` | `8,706,505` |
| `eval_profit` | `0` |

- Step 12~17 Scheduler `portfolio-paper-daily-step12-17-order-0901-kst` transition **DISABLED → ENABLED** complete.

| Item | Value |
| --- | --- |
| LastModificationDate | `2026-07-01T13:53:57.160+09:00` |
| ScheduleExpression | `cron(1 9 ? * MON-FRI *)` |
| Timezone | Asia/Seoul |
| FlexibleTimeWindow | OFF |
| Target | `portfolio-paper-daily-scheduler-dispatcher` |
| Target Input | `{"scheduleType":"STEP12_17_ORDER","dryRun":false}` |

- Confirmed all seven Daily scheduling items are **🟢 ENABLED**.

| Item | Value |
| --- | --- |
| `portfolio-paper-ec2-start-0750-kst` | 07:50 KST EC2 start |
| `portfolio-daily-brief-morning-slack-0750-kst` | eventType `MORNING_BRIEF` |
| `portfolio-paper-daily-step1-11-approval-0800-kst` | dryRun false |
| **`portfolio-paper-daily-step12-17-order-0901-kst`** | **dryRun false — ENABLED on this date** |
| `portfolio-paper-intraday-snapshot-evaluate-10min-kst` | cron `cron(10/10 9-15 ? * MON-FRI *)` |
| `portfolio-daily-brief-evening-slack-1550-kst` | eventType `EVENING_BRIEF` |
| `portfolio-paper-marketconnector-stop-1550-kst` | 15:50 KST MC EC2 stop |

- Remaining stale `connector_position_snapshot` rows (4 rows from 2026-06-23) were confirmed as stale snapshots separate from the latest balance as-of date 2026-07-01. Judgment based on latest balance + 0 orders for the day: all-cash **NO_TARGET** safe termination.
- Kiro modified documentation only for this work. See the table below for execution · order · record history.

| Item | Result |
| --- | --- |
| AWS CLI execution | 0 |
| boto3 execution | 0 |
| psql execution | 0 |
| Spring Boot execution | 0 |
| external API execution | 0 |
| broker order submission | 0 |
| aws-live work | 0 |
| raw secret recording | 0 |

### 🔵 Evidence

- Step 12~17 manual execution SUCCEEDED — [04 operation-notes 2026-07-01 §1](specs/04-strategy-batch-stepfunctions/operation-notes.md).
- DB pre-check · after-check passed · NO_TARGET safe termination judgment — [04 operation-notes 2026-07-01 §2](specs/04-strategy-batch-stepfunctions/operation-notes.md).
- Step 12~17 Scheduler DISABLED → ENABLED transition complete — [04 operation-notes 2026-07-01 §3](specs/04-strategy-batch-stepfunctions/operation-notes.md).
- Confirmed all seven Daily scheduling items ENABLED — [04 operation-notes 2026-07-01 §4](specs/04-strategy-batch-stepfunctions/operation-notes.md).

### ⚠️ Risks

| Item | Value |
| --- | --- |
| New Risk ID | R-AUTO-037 |
| Status badge | 🟢 `Mitigated` |
| Impact | High |
| Probability | Low |

- New R-AUTO-037 — risk that Step 12~17 automatic execution may run even when Step 1~11 fails or data is not ready.
- R-AUTO-001 mitigation reinforcement [2026-07-01 reinforcement] — no automatic retry policy retained for BUY · SELL · fill sync · position-change steps.
- R-AUTO-001 mitigation reinforcement [2026-07-01 reinforcement] — even after Step 12~17 Scheduler ENABLED, Step Functions Retry policy remains under review.
- R-AUTO-025 mitigation reinforcement [2026-07-01 automatic ENABLE entry] — 09:01 schedule hold policy passed entry into ENABLE after operator approval. Status remains `Mitigated`.
- Promotion conditions for OD-MS-033 (09:01 hold) are already included in the decision body, so only evidence reinforcement was added.
- No new decisions on this date. Decision Summary count unchanged (total 97 · 확정 52 · 잠정 42).

### 📌 Follow-ups

- [ ] Observe real automatic execution at 09:01 on the next business day.
- [ ] On a day with candidates, confirm automatic order submission · fill · balance refresh · Slack receipt.
- [ ] Clean stale `connector_position_snapshot` or reinforce the judgment query based on latest balance.
- [ ] Clean up 6 stale ACCEPTED `connector_order_request` rows for 삼성전자 from 2026-04-27.
- [ ] Reconsider aws-live automation policy in a separate cutover phase.

### 🔐 Security

| Item | Result |
| --- | --- |
| Raw secret recording | 0 |
| AWS CLI execution | 0 |
| psql execution | 0 |
| broker order submission | 0 |
| aws-live policy change | 0 |

> <span style="color:#D1242F">No change to aws-live BUY / SELL automation policy.</span>

<details>
<summary>🔵 View placeholder / unrecorded raw categories</summary>

- Placeholder policy catalog (not used in this session) — `[REDACTED]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_PUBLIC_IP]` · `[REDACTED_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_SECRET_ARN]` · `[REDACTED_BROKER_ORDER_NO]`.
- Unrecorded raw sensitive-information categories — secret · password · KIS app key · KIS app secret · token · Slack webhook URL · raw account number · RDS endpoint · account-id · actual IAM Role ARN · actual state machine ARN · actual Lambda ARN · raw broker_order_no · public IP · EIP · ENI ID · task ARN · image digest full sha256.
- Only operational identifiers are recorded as facts — state machine name · scheduler name · Lambda function name · execution name · status · timestamp · balance snapshot id · amounts · count.

</details>


## 2026-06-30 (afternoon) (port-view ECS Fargate Public IP first port completed + ECS View → AWS Step Functions Step 12~17 approval execution passed + desiredCount 0 shutdown)

### 🧭 Summary

The afternoon session on 2026-06-30 completed three major workstreams.
(1) The first port of port-view to ECS Fargate with a Public IP was completed, reaching ECS View → AWS Step Functions Step 12~17 approval execution `SUCCEEDED` and shutdown with desiredCount 0. (2) Approval Required + Daily Brief Slack Builder + notifier formatter improvements, mini Step Functions,
and two pre/post-market Schedulers were ENABLED successfully. (3) The MarketConnector EC2 evaluate replacement deployment extended integration through a real `INTRADAY_STOP_LOSS` Slack event path. All three workstreams completed DB after-checks and Slack receipt validation, while the aws-live automation policy remained unchanged.

### ✅ Completed

#### 1. port-view ECS Fargate Public IP first port

| Item | Value |
| --- | --- |
| Overall status | 🟢 Complete |
| ECR | `portfolio-view` push complete |
| Task Definition | `portfolio-view:1` → `portfolio-view:2` |
| ECS Service | `portfolio-view-service` RUNNING |
| Spring profile | `aws-paper` |
| Web server | Tomcat 8080 |
| DB | RDS PostgreSQL |
| Connection pool | HikariPool |
| Default schema | `ops` |
| Screen validation | Dashboard · Balance · Positions · Orders · Reports · Daily query paths passed |

#### 2. ECS View → Step Functions Step 12~17 approval execution

| Item | Value |
| --- | --- |
| Execution result | 🟢 `SUCCEEDED` |
| executionName | `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` |
| State Machine | `portfolio-paper-daily-step12-17-approval` |
| Slack | `DAILY_EXECUTION_SUCCESS` received |
| Order result | Safe `NO_TARGET` termination |
| New broker orders | 0 |

#### 3. DB after-check

| Item | Value |
| --- | --- |
| Validation result | 🟢 Passed |
| REQUESTED `strategy_execution_order` | 0 |
| retryable rejected | 0 |
| active `connector_order_request` | 0 |
| Latest snapshot ID | `281` |
| `as_of_date` | `2026-06-30` |
| `total_eval_amount` | `8,706,505` |
| `cash_balance` | `8,706,505` |

#### 4. ECS shutdown and network operation

| Item | Value |
| --- | --- |
| `desiredCount` | Transitioned to 0 |
| Fargate task | Stopped |
| Next startup | New public IP will be assigned |
| SG inbound | Operator IP `/32` retained |
| Operational-path alignment | 5 paths confirmed |
| Local File paths | Step 1 · Step 1~11 · Step 12~17 |
| Local → AWS SFN | Step 12~17 validated in the morning |
| ECS → AWS SFN | Step 12~17 validated in the afternoon |

#### 5. Approval Required Slack Builder

| Item | Value |
| --- | --- |
| Overall status | 🟢 Complete |
| Builder Lambda | `portfolio-approval-slack-summary-builder` |
| State Machine | `portfolio-paper-daily-step1-17-approval` |
| revisionId | `da8642c6-8409-41b6-ad57-e066ff672332` |
| Notifier smoke | Success |
| eventType | `APPROVAL_REQUIRED` |
| color | `#ECB22E` |
| `marketStatusCode` | `BLOCK` |
| `marketStatusLabel` | `차단` |

#### 6. Daily Brief Slack Builder

| Item | Value |
| --- | --- |
| Overall status | 🟢 Complete |
| Builder Lambda | `portfolio-daily-brief-slack-summary-builder` |
| Runtime | Python 3.12 |
| DB library | `pg8000` |
| Secret injection | Secrets Manager `valueFrom` |
| smoke | Passed |
| snapshot ID | `281` |
| Total valuation amount | `8,706,505` |
| Cash balance | `8,706,505` |
| Cumulative return | `-12.94%` |
| Cumulative P/L | `-1,293,495` |
| Holdings | 0 |
| Post-market delta | 0 KRW |

#### 7. `portfolio-event-notifier` formatter

| Item | Value |
| --- | --- |
| Overall status | 🟢 Complete |
| alias 1 | `MORNING_BRIEF` → `PRE_MARKET_STATUS` |
| alias 2 | `EVENING_BRIEF` → `POST_MARKET_STATUS` |
| P/L prefix | 🔵 · 🔴 · ⚪ |
| Pre-market Slack | `🌅 [장 전] 6/30 (화)` received |
| Post-market Slack | `🌅 [장 후] 6/30 (화)` received |
| Cumulative return display | `🔵 -12.94%` |
| Day-over-day display | `⚪ 0원` |

#### 8. Daily Brief mini Step Functions and IAM

| Item | Value |
| --- | --- |
| Overall status | 🟢 Complete |
| State Machine | `portfolio-daily-brief-slack-notification` |
| State Machine status | ACTIVE |
| Execution Role | `portfolio-daily-brief-sfn-role` |
| Scheduler Role | `portfolio-daily-brief-scheduler-role` |
| morning smoke | `daily-brief-morning-smoke-safe-20260630-193255-68f50aeb` |
| morning result | `SUCCEEDED` |
| evening smoke | `daily-brief-evening-smoke-safe-20260630-193300-aa2b9a12` |
| evening result | `SUCCEEDED` |

#### 9. EventBridge Scheduler

| Item | Value |
| --- | --- |
| Overall status | 🟢 2 ENABLED |
| Pre-market Scheduler | `portfolio-daily-brief-morning-slack-0750-kst` |
| Pre-market cron | `cron(50 7 ? * MON-FRI *)` |
| Pre-market timezone | `Asia/Seoul` |
| Pre-market input | `MORNING_BRIEF` |
| Post-market Scheduler | `portfolio-daily-brief-evening-slack-1550-kst` |
| Post-market cron | `cron(50 15 ? * MON-FRI *)` |
| Post-market timezone | `Asia/Seoul` |
| Post-market input | `EVENING_BRIEF` |

#### 10. Intraday stop-loss Slack real-event integration

| Item | Value |
| --- | --- |
| Overall status | 🟢 Complete |
| Deployment target | MarketConnector evaluate |
| Version | `connector-intraday-position-evaluate-1.1.1-slack-notify` |
| CLI option | `--notify-slack` |
| CLI option | `--slack-function-name` |
| CLI option | `--slack-region` |
| Static validation | `py_compile` passed |
| CLI validation | `--help` passed |

#### 11. MarketConnector EC2 IAM invoke permission

| Item | Value |
| --- | --- |
| Overall status | 🟢 Complete |
| Role | `portfolio-paper-marketconnector-ec2-role` |
| inline policy | `portfolio-paper-marketconnector-event-notifier-invoke` |
| Resource | Restricted to `portfolio-event-notifier` |
| invoke smoke | Success |

#### 12. Intraday runner update

| Item | Value |
| --- | --- |
| Overall status | 🟢 Complete |
| New option | `--create-order` |
| New option | `--notify-slack` |
| backup | `run_intraday_snapshot_and_evaluate.sh.bak.20260630T112255Z.create-order-notify-slack` |
| Syntax validation | Passed |

#### 13. One safe real-runner validation

| Item | Value |
| --- | --- |
| Validation result | 🟢 Passed |
| commandId | `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` |
| `open_position_count` | 0 |
| Status | `EMPTY_NORMAL` |
| marker | `INTRADAY_SNAPSHOT_AND_EVALUATE=SUCCESS` |
| Exit code | 0 |
| Order query | Not executed |
| Order creation | Not executed |
| Slack send | Not executed |
| Normal-termination reason | 0 OPEN positions |

#### 14. Intraday stop-loss DB after-check

| Item | Value |
| --- | --- |
| Validation result | 🟢 Passed |
| marker | `STEP19C_INTRADAY_STOP_FINAL_DB_AFTER_CHECK=SUCCESS` |
| `TODAY_INTRADAY_CHECKS` | 0 |
| `TODAY_INTRADAY_STOP_EXECUTION_ORDERS` | 0 |
| `ACTIVE_INTRADAY_STOP_EXECUTION_ORDERS` | 0 |
| `TODAY_INTRADAY_STOP_CONNECTOR_ORDERS` | 0 |
| `TODAY_INTRADAY_STOP_CONNECTOR_ORDER_ROWS` | 0 |
| Latest snapshot ID | `281` |
| source version | `connector-intraday-snapshot-refresh-1.0.0` |
| psql exit code | 0 |

### 🔵 Evidence

| Item | Value |
| --- | --- |
| Category | ECS Fargate first port |
| Result | 🟢 Passed |
| Detail | [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md) |

| Item | Value |
| --- | --- |
| Category | Step Functions Step 12~17 approval |
| Result | 🟢 SUCCEEDED |
| Detail | [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |

| Item | Value |
| --- | --- |
| Category | DB after-check |
| Result | 🟢 Passed · balance id=281 |
| Detail | [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md) |

| Item | Value |
| --- | --- |
| Category | Three Slack improvements (Approval / Daily Brief / Notifier) |
| Result | 🟢 smoke SUCCEEDED |
| Detail | [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |

| Item | Value |
| --- | --- |
| Category | Daily Brief mini SFN + 2 Schedulers |
| Result | 🟢 ENABLED |
| Detail | [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |

| Item | Value |
| --- | --- |
| Category | Intraday stop-loss evaluate replacement · runner integration |
| Result | 🟢 Safe validation passed |
| Detail | [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md) |

| Item | Value |
| --- | --- |
| Category | Intraday stop-loss DB after-check |
| Result | 🟢 Passed |
| Detail | [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md) |

| Item | Value |
| --- | --- |
| Category | MarketConnector EC2 IAM invoke permission |
| Result | 🟢 Granted · smoke succeeded |
| Detail | [06 operation-notes](specs/06-secrets-and-iam/operation-notes.md) |

- Alignment of five View operating paths (2026-06-28 Run #46 · #47 → 2026-06-29 Run #48 → 2026-06-30 morning `port-view-daily-step12-17-20260630-095111-aae2595c` → **afternoon `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8`**).
- 🟠 Identified 6 stale `connector_order_request` rows (remaining ACCEPTED from 2026-04-27) — unrelated to this day's execution; follow-up cleanup candidate.
- 🟠 Identified one case where a SUCCESS marker was printed after failed SQL — reflected in reinforcement of rule 3 in `.kiro/AGENTS.md`.

### ⚠️ Risks

| Item | Value |
| --- | --- |
| Risk ID | R-AUTO-033 |
| Status | 🟢 `Mitigated` |
| Note | First validation of Public IP direct + operator IP/32 SG [2026-06-30 afternoon reinforcement] |

| Item | Value |
| --- | --- |
| Risk ID | R-AUTO-034 |
| Status | 🟠 `Open` |
| Note | First validation of restricted Fargate Task Role `states:StartExecution` grant (permission separation remains follow-up in spec 06) |

| Item | Value |
| --- | --- |
| Risk ID | R-AUTO-035 |
| Status | 🟢 `Mitigated` |
| Note | Daily Brief mini SFN separation + 2 Schedulers ENABLED + smoke SUCCEEDED (new) |

| Item | Value |
| --- | --- |
| Risk ID | R-AUTO-036 |
| Status | 🟢 `Mitigated` |
| Note | Policy has no rollback if Slack fails after intraday stop-loss READY creation (new · detected through CloudWatch/SFN audit/DB after-check) |

- Decision changes — no body changes to OD-MS-002 · OD-MS-009 · OD-MS-030 · OD-MS-035 · OD-MS-036 · OD-MS-037 · OD-SAFE-001 ~ OD-SAFE-004; evidence reinforced.
- New decision — OD-MS-038 (Daily Brief Slack mini workflow, 🟢 확정 / affected specs 04 · 05 · 10).
- Decision Summary — total 96 → 97 · 확정 51 → 52 · 잠정 42 retained.

### 📌 Follow-ups

ECS / View follow-ups:

- [ ] Align Daily-screen wording with ECS / AWS mode.
- [ ] Decide cleanup for 6 stale `connector_order_request` rows (2026-04-27).
- [ ] Formalize desiredCount 0/1 operating commands in the runbook.
- [ ] Define SG inbound update procedure when operator IP changes.
- [ ] ALB · HTTPS · Route53 · Cloudflare Tunnel · authentication · application-ecs.yml separation · ECS Auto Scaling · Blue/Green remain follow-up phases for specs 05 · 06 · 07 · 10.
- [ ] Reinforce AWS CLI · psql · SUCCESS-marker operating principles in `.kiro/AGENTS.md` (reflected on this date).

Slack follow-ups:

- [ ] Confirm automatic Daily Brief Slack receipt at 07:50 / 15:50 on the next business day.
- [ ] Confirm automatic receipt of rich `APPROVAL_REQUIRED` Slack during the next real Step 1~11 run.
- [ ] Reconfirm per-security P/L · return display when holdings exist.
- [ ] Reinforce `DAILY_EXECUTION_SUCCESS` summary / define `DAILY_EXECUTION_FAILED` cause-truncation policy.
- [ ] Move Slack webhook URL to Secrets Manager or SSM SecureString (R-AUTO-024 / spec 06).

Intraday stop-loss follow-ups:

- [ ] Confirm receipt of a real `INTRADAY_STOP_LOSS` event when a real holding exists (this date was limited to `open_position_count=0`).
- [ ] Reconfirm `INTRADAY_STOP_SELL` READY creation + approval-gate block when a real holding exists.
- [ ] Review whether to add account · current price · entry price · expected P/L amount to Slack message.
- [ ] Review whether to add a separate approval-summary Slack after intraday stop-loss READY creation.
- [ ] Keep automatic ENABLE entry of `portfolio-paper-intraday-stop-sell-approval` as follow-up.

### 🔐 Security

| Item | Result |
| --- | --- |
| Raw secret recorded | 0 |
| AWS CLI · boto3 · psql · Spring Boot · Lambda · SFN · Slack webhook · KIS execution | 0 |
| broker order submission · fill · position-sync automatic retry | 0 |
| Slack real-event sends other than smoke | 0 |
| commit · add · reset · checkout · stash | 0 |
| aws-live policy change | 0 |

> <span style="color:#D1242F">Kiro updated only `.kiro` root + `.kiro/specs` documentation on this date. All AWS resource creation · modification · deletion was performed directly by the operator.</span>

<details>
<summary>🔵 Placeholder and operational-identifier summary</summary>

- Placeholder — `[REDACTED]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_PUBLIC_IP]` · `[REDACTED_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_SECRET_ARN]` · `[REDACTED_BROKER_ORDER_NO]`.
- ECS Fargate — cluster `portfolio-paper-cluster` · service `portfolio-view-service` · task def `portfolio-view:2` · ECR `portfolio-view` · Logs `/ecs/portfolio-view` · SG `sgroup-port-view-ecs`.
- Step Functions — executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` · state machine `portfolio-paper-daily-step12-17-approval` · status `SUCCEEDED` · Slack `DAILY_EXECUTION_SUCCESS`.
- Intraday stop-loss — Lambda `portfolio-event-notifier` · state machine `portfolio-paper-intraday-stop-sell-approval` · SSM commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` · IAM Role `portfolio-paper-marketconnector-ec2-role` · inline policy `portfolio-paper-marketconnector-event-notifier-invoke`.
- Slack improvements — Builder Lambda `portfolio-approval-slack-summary-builder` · `portfolio-daily-brief-slack-summary-builder` · mini SFN `portfolio-daily-brief-slack-notification` · Scheduler `portfolio-daily-brief-morning-slack-0750-kst` · `portfolio-daily-brief-evening-slack-1550-kst`.
- balance snapshot — `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505`.
- Detailed evidence — [04](specs/04-strategy-batch-stepfunctions/operation-notes.md) · [05](specs/05-port-view-ecs-and-runbook/operation-notes.md) · [06](specs/06-secrets-and-iam/operation-notes.md) · [03](specs/03-marketconnector-ec2/operation-notes.md).

</details>

## 2026-06-30 (Local View → AWS Step Functions Step 12~17 approval execution validation passed + Daily Batch gate correction + four View operating paths aligned)

### 🧭 Summary

The morning session on 2026-06-30 produced the first successful Local View → AWS Step Functions Step 12~17 approval execution.
The regular morning AWS Step Functions Step 1~11 run passed through the automatic trigger path. After correcting the Daily Batch gate (`DailyBatchController.java`), Local View launched executionName `port-view-daily-step12-17-20260630-095111-aae2595c`, which completed as `SUCCEEDED`, confirming alignment across four View operating paths.
The order-capable Step 12~17 range retains the separate approval state machine + paper-order gate policy.

### ✅ Completed

1. Morning AWS Step Functions Step 1~11 scheduled run passed:complete
   1) Trigger: automatic path through EventBridge Scheduler · Dispatcher Lambda
   2) Alignment: OD-MS-032

2. Local View → AWS Step Functions Step 12~17 approval execution:complete

| Item | Value |
| --- | --- |
| executionName | `port-view-daily-step12-17-20260630-095111-aae2595c` |
| state machine | `portfolio-paper-daily-step12-17-approval` |
| Status | 🟢 **SUCCEEDED** |
| Start | `2026-06-30T09:51:11.903+09:00` |
| Stop | `2026-06-30T09:54:16.484+09:00` |
| Completion judgment | Safe termination with no order target |

3. Daily Batch gate (`DailyBatchController.java`) correction validation:complete

| Item | Value |
| --- | --- |
| Gate combination | `fullPipelineExecutionEnabled=true` + `paperOrderEnabled=true` |
| Activation condition | AWS Step 12~17 approval button enabled · Local File / AWS Step Functions gates separated · executes only when approval-range gate passes |
| mvn compile | Success |
| Local View restart | `aws-paper` profile passed |
| Screen labels | `Execution ON` · `Local File OFF` · `Full Pipeline ON` · `Paper Order ON` |
| Allowed range | `1~17` |

4. DB post-validation:complete

| Item | Value |
| --- | --- |
| New `connector_order_request` | 0 |
| Remaining REQUESTED `strategy_execution_order` | 0 |
| active `connector_order_request` | 0 |
| Latest `connector_balance_snapshot` | `id=281` |
| `as_of_date` | `2026-06-30` |
| `total_eval_amount` | `8,706,505` |
| `cash_balance` | `8,706,505` |
| `eval_profit` | `0` |
| `source_version` | `connector-intraday-snapshot-refresh-1.0.0` |
| Holdings | 0 |

5. Four View operating paths aligned:complete
   1) Local View → Local File Step 1 (2026-06-28 Run #46)
   2) Local View → Local File Step 1~11 (2026-06-28 Run #47)
   3) Local View → Local File Step 12~17 (2026-06-29 Run #48)
   4) **Local View → AWS Step Functions Step 12~17 (this date)**

### 🔵 Evidence

- Morning AWS Step Functions Step 1~11 scheduled run passed — automatic EventBridge Scheduler · Dispatcher Lambda trigger path · aligned with OD-MS-032. The order-capable Step 12~17 range retains a separate approval state machine + paper-order gate policy (OD-MS-033 09:01 automatic ENABLE remains deferred).
- Local View → AWS Step Functions Step 12~17 approval execution passed — approval button enabled after aligning Daily Batch gate with operator intent. Executes only when `paperOrderEnabled=true` + approval-range gate passes.

| Validation | Result |
| --- | --- |
| mvn compile | Success |
| Local View `aws-paper` profile restart | Passed |
| Screen display (Execution ON · Local File OFF · Full Pipeline ON · Paper Order ON · allowed range `1~17` · approval button enabled) | Passed |

- AWS Step Functions execution facts · executionName `port-view-daily-step12-17-20260630-095111-aae2595c` · state machine `portfolio-paper-daily-step12-17-approval` · trigger = Local View AWS Step Functions approval-range button
  status 🟢 **SUCCEEDED** · start `2026-06-30T09:51:11.903+09:00` · stop `2026-06-30T09:54:16.484+09:00` · safe termination with no order target.
- DB post-validation passed — see Completed item 4 table above. Because `connector_position_snapshot.balance_snapshot_id` does not exist, holdings validation used `account_no` + `as_of_date`.
- Four operating paths aligned — Local File and AWS Step Functions executions operate separately / order-capable Step 12~17 executes through separate approval state machine + paper-order gate / 0 mitigation regressions for R-AUTO-033 · R-AUTO-034.

### ⚠️ Risks

| Item | Value |
| --- | --- |
| ID | OD-MS-002 · OD-MS-009 · OD-MS-037 · OD-SAFE-001 ~ OD-SAFE-004 |
| Status | Body unchanged |
| Note | First-validation evidence reinforced (combined with 2026-06-29 (3) reinforcement) |

| Item | Value |
| --- | --- |
| ID | R-AUTO-033 |
| Status | 🟢 **Mitigated** |
| Note | [2026-06-30 reinforcement] Daily Batch gate aligned with operator intent + first AWS Step Functions approval-execution validation |

| Item | Value |
| --- | --- |
| ID | R-AUTO-034 |
| Status | 🟠 **Open** |
| Note | [2026-06-30 reinforcement] First validation separating four View operating paths (Fargate Task Role permission separation remains follow-up in spec 06) |

| Item | Value |
| --- | --- |
| ID | Decision Summary |
| Status | Unchanged |
| Note | total 96 · 확정 51 · 잠정 42 retained |

| Item | Value |
| --- | --- |
| ID | New decisions on this date |
| Status | None |
| Note | — |

### 📌 Follow-ups

- View / ECS:
  - After stabilizing View Local → AWS Step Functions, enter ECS / Fargate porting design (follow-up phase for specs 05 · 06 · 07).
  - Decide whether Local File execution is excluded from View ECS deployment or retained as a separate operator-only path.
  - Review an AWS Step Functions trigger-centered operating direction from ECS Fargate.
  - Update Daily Batch screen wording so `aws-stepfunctions` mode is as explicit as local-file mode (spec 05 follow-up).
  - Dockerfile · ECR · ECS Task Definition · Fargate read-only smoke test · Fargate Step 1~11 + Step 12~17 validation (follow-up phase for specs 05 · 06 · 07).
- DB:
  - Reinforce DB validation-query authoring principles — check `information_schema.columns` first · prohibit guessed columns · post-validation query must expose SELECT result · align with this date's confirmed absence of `connector_position_snapshot.balance_snapshot_id`.
- Previous follow-ups retained:
  - R-BROKER-005 · R-DATA-017 · R-SEC-010 · R-AUTO-024 · R-AUTO-027 · R-AUTO-028
  - 2026-04-27 삼성전자 stale ACCEPTED cleanup
  - OD-MS-033 09:01 hold

### 🔐 Security

| Item | Result |
| --- | --- |
| New AWS resource creation · modification · deletion | 0 |
| New KIS · Connector · Slack · Daily Batch trigger change on this date | 0 (validation limited to direct operator local execution) |
| New `connector_order_request` | 0 |
| `--execute` execution on this date | 1 (🔵 **NO_TARGET**) |
| broker order submission | 0 |
| aws-live work | 0 |
| Raw secret recording | 0 |

> The port-view MS commit (`DailyBatchController.java` Daily Batch gate correction) belongs to the port-view MS scope. Changes caused by the cross-service AWS Migration spec work on this date: 0 in the spec area.

<details>
<summary>🔵 Unrecorded raw categories and operational-identifier summary</summary>

- Unrecorded raw sensitive information · secret value · KIS app key · KIS app secret · account number · account password · token · RDS password · RDS endpoint hostname · raw 12-digit account-id · actual IAM Role ARN · actual secret ARN · IAM access key id · instance-id · EIP
  image digest full sha256 · task ARN · job ARN · raw broker_order_no · KIS paper login credential · Slack webhook URL · DB password · Administrator password · actual state machine ARN. All represented as `[REDACTED]` or placeholders.
- Operational identifiers recorded as facts · executionName `port-view-daily-step12-17-20260630-095111-aae2595c` · state machine name `portfolio-paper-daily-step12-17-approval` · status `SUCCEEDED` · start · stop timestamp · balance snapshot `id=281`
  `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505` · `eval_profit=0` · `source_version=connector-intraday-snapshot-refresh-1.0.0` · 4 screen-status labels · allowed range `1~17` · Daily Batch gate labels
  - DB column names `balance_snapshot_id` · `account_no` · `as_of_date`. Aligned with explicit user policy — not secrets.

</details>

## 2026-06-29 (3) (port-view Step 12~17 approval validation complete + Local View wrappers organized + Approval state machine ARN separated)

### ✅ Completed

- 🟢 **View Local Batch Step 12~17 approval state machine validation passed**

| Item | Value |
| --- | --- |
| executionName | `port-view-step12-17-step12-17-20260629-194314-ba5edaf8` |
| state machine | `portfolio-paper-daily-step12-17-approval` |
| Status | 🟢 **SUCCEEDED** |
| Start | `2026-06-29T19:43:15.673+09:00` |
| Stop | `2026-06-29T19:46:06.546+09:00` |
| Entry phase | Second phase where Local View connects to SFN `StartExecution` as an external caller |
| Flow | `Step12_CheckApproval` → `Step12_RunMarketConnectorStrategyOrderExecute` → `Step12_GetCommandInvocation` → Step 13~17 → `ExecutionSucceeded` |
| DB post-validation marker | `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS` |
| New `connector_order_request` today | 0 |
| READY · REQUESTED `strategy_execution_order` | 0 |
| New broker orders | 0 |
| mitigation regression | R-AUTO-033 · R-AUTO-034 0 |

- 🟢 **Four preflight checks passed**

| Item | Result |
| --- | --- |
| REQUESTED / READY `strategy_execution_order` | 0 |
| retryable rejected `connector_order_request` | 0 |
| active `connector_order_request` | 0 |
| strategy-linked active `connector_order_request` | None |
| stale ACCEPTED orders (2026-04-27 삼성전자) | Registered in followups-overview as a separate cleanup target |

- 🟢 **Implementation changes**

| Item | Value |
| --- | --- |
| `DailyBatchProperties` field | `awsStepfunctionsApprovalStateMachineArn` + getter/setter |
| `application-aws-paper.properties` key | `portfolio.batch.aws-stepfunctions-approval-state-machine-arn` |
| Environment variable | `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_APPROVAL_STATE_MACHINE_ARN` |
| `startSafeRange` | General ARN (`portfolio-paper-daily-step1-17-approval`) |
| `startApprovalRange` | approval ARN (`portfolio-paper-daily-step12-17-approval`) |
| approval ARN empty | Block approval execution |
| boolean payload | `allowPaperOrderExecute` · `paperOrderEnabled` |
| numeric payload | `fromStepOrder` · `toStepOrder` · `startStep` · `endStep` |
| New `DailyBatchController` endpoint | `POST /daily-batch/aws-stepfunctions/start-approval-range` |
| `daily_batch.html` | Separate AWS Step 12~17 approval-execution button (safe / approval conditions separated) |

- 🟢 **Payload type reinforcement**

| Item | Value |
| --- | --- |
| Initial validation result | Blocked after entering approval state machine because `Step12_CheckApproval` `BooleanEquals` did not match |
| Cause | boolean field passed as string `"true"` + numeric fields passed as strings |
| Action | Corrected to boolean / numeric types |
| Revalidation result | Passed `Step12_CheckApproval` + confirmed 🟢 **ExecutionSucceeded** |

- 🟢 **Local View startup wrappers organized and passed**

| Item | Value |
| --- | --- |
| Local-file wrapper | `Start-PortfolioViewAwsPaperLocalFile.ps1` |
| AWS SFN wrapper | `Start-PortfolioViewAwsPaperStepFunctions.ps1` |
| Common Spring profile | `aws-paper` |
| Execution range | Full Step 1~17 (operator-selected, not safe-only gate) |
| Validation passed | `Unblock-File` + `ExecutionPolicy Bypass` + `VIEW_AWS_PAPER_STEPFUNCTIONS_ENV_READY` + DB `jdbc:postgresql://127.0.0.1:15433/portfolio` + View DB user `view_app` + Tomcat 8080 + `PortViewApplication started` |
| Wrapper location | `C:\Workspaces\portfolio-local-env\` (outside spec scope) |

### ⚠️ Risks

| Item | Value |
| --- | --- |
| ID | OD-MS-002 · OD-MS-009 · OD-MS-037 · OD-SAFE-001 ~ OD-SAFE-004 |
| Status | Body unchanged |
| Note | 05 spec operation-notes 4)·5) Step 12~17 approval + wrapper organization complete / 6)·7) Docker·ECR·Task Def·Fargate validation incomplete and renumbered |

| Item | Value |
| --- | --- |
| ID | R-AUTO-033 |
| Status | 🟢 **Mitigated** |
| Note | [2026-06-29 (3) reinforcement] Separate Step 12~17 approval SFN + first boolean/numeric payload validation |

| Item | Value |
| --- | --- |
| ID | R-AUTO-034 |
| Status | 🟠 **Open** |
| Note | [2026-06-29 (3) reinforcement] Approval ARN separation + first service-level safety-gate validation · Fargate Task Role separation remains spec 06 follow-up |

| Item | Value |
| --- | --- |
| ID | Decision Summary |
| Status | Unchanged |
| Note | total 96 · 확정 51 · 잠정 42 retained |

### 📌 Follow-ups

- [ ] Write Dockerfile.
- [ ] ECR repository · image push.
- [ ] Register ECS Task Definition.
- [ ] ECS Service read-only smoke test.
- [ ] Validate Fargate View Step 1~11 `StartExecution`.
- [ ] Validate Fargate View Step 12~17 approval.
- [ ] Fargate Task Role least privilege for `states:StartExecution` (both general + approval resources explicitly scoped).
- [ ] Clean up stale ACCEPTED `connector_order_request` for 2026-04-27 삼성전자.
- [ ] Decide initial Fargate `paperOrderEnabled` default.
- [ ] Restrict ALB source IP or add minimal authentication.
- [ ] Review CloudWatch Logs · alarms · failure Slack integration.
- Follow-up phase responsibility: specs 05 · 06 · 07 · 10.

### 🔐 Security

| Item | Result |
| --- | --- |
| New AWS resource creation · modification · deletion | 0 |
| New KIS · Connector · Slack · Daily Batch trigger on this date | 0 (validation limited to direct operator local execution) |
| New `connector_order_request` | 0 |
| `--execute` execution on this date | 1 (🔵 **NO_TARGET**) |
| broker order submission | 0 |
| aws-live work | 0 |
| Raw secret recording (KIS · account · token · RDS password · IAM ARN · SFN ARN · Slack webhook · DB password · Administrator password, etc.) | 0 |
| commit · add · reset · checkout · stash | 0 |

> port-view MS changes (`DailyBatchProperties.java` · `StepFunctionsDailyBatchExecutionService.java` · `application-aws-paper.properties` · `DailyBatchController.java` · `daily_batch.html` + 2 PowerShell wrappers + 2 env loaders) belong to the port-view MS scope. Cross-service AWS Migration spec changes on this date: 0.

<details>
<summary>🔵 Recorded operational-identifier summary</summary>

- executionName `port-view-step12-17-step12-17-20260629-194314-ba5edaf8`.
- Operational marker `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS`.
- Two state machines — `portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval`.
- Spring properties key + environment-variable labels.
- Payload field labels + boolean / numeric types.
- Four PowerShell wrapper filenames.
- DB URL `jdbc:postgresql://127.0.0.1:15433/portfolio` · View DB user `view_app` · Tomcat port `8080`.
- start · stop timestamp `2026-06-29T19:43:15.673+09:00` ~ `2026-06-29T19:46:06.546+09:00`.
- Four state names — `Step12_CheckApproval` · `Step12_RunMarketConnectorStrategyOrderExecute` · `Step12_GetCommandInvocation` · `ExecutionSucceeded`.
- `requestedBy=VIEW_APPROVAL_BUTTON` label.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-29 (2) (port-view aws-stepfunctions Daily Batch trigger implemented + local Step 1~11 StartExecution validation passed)

### ✅ Completed

- 🟢 **First port-view aws-stepfunctions Daily Batch trigger implementation passed**

| Item | Value |
| --- | --- |
| New class | `StepFunctionsDailyBatchExecutionService` |
| `DailyBatchProperties` gate | `canStartAwsStepfunctions` · `awsStepfunctionsStartEnabled` · `awsStepfunctionsStepStartEnabled`, etc. |
| Controller endpoint | `POST /daily-batch/aws-stepfunctions/start-range` |
| Screen button | `/daily-batch` AWS Step 1~11 safe trigger |
| `application-aws-paper.properties` | Added env override placeholder |
| AWS SDK | v2 Step Functions client |
| commit | `e72de6f` (`feat(view): add Step Functions daily batch trigger`) |

- 🟢 **Local Step 1~11 `StartExecution` end-to-end validation passed**

| Item | Value |
| --- | --- |
| Execution condition | `aws-paper` profile + `execution-mode=aws-stepfunctions` |
| Trigger | `/daily-batch` AWS Step 1~11 safe-trigger button |
| Result | `StartExecution` succeeded · returned `executionName` · `executionArn` flash message with account-id redacted |
| Step 1~11 workflow | Executed |
| Step 12~17 order-capable range | `allowPaperOrderExecute=false` block retained |
| Slack | `APPROVAL_REQUIRED` received |
| broker order submission | 0 |
| New `connector_order_request` | 0 |
| aws-live work | 0 |

- 🟢 **Resolved missing runDate issue**

| Item | Value |
| --- | --- |
| Initial validation failure point | `StopCrawlerEc2AfterStep11Success` state with `States.Runtime` |
| Cause | View `StartExecution` input did not contain `runDate` required by ASL Payload `runDate.$=$.runDate` |
| Action | `StepFunctionsDailyBatchExecutionService` now includes Asia/Seoul-based `runDate` (yyyy-MM-dd) in input JSON |
| Revalidation result | Passed `StopCrawlerEc2AfterStep11Success`, then `SendApprovalRequiredSlack`; received Slack `APPROVAL_REQUIRED` |

- 🟢 **Safety alignment** — all blocked at service level

| Case | Result |
| --- | --- |
| `canStartAwsStepfunctions=false` | Blocked |
| `hasRunningBatch=true` | Blocked |
| `stateMachineArn` empty | Blocked |
| Outside `minExecutableStepOrder` · `maxExecutableStepOrder` range | Blocked |
| Step 12+ while `allowPaperOrderExecute=false` | Blocked |
| Approval request outside `paperOrderEnabled=true` condition | Blocked |

  - First empirical validation — R-AUTO-033 [2026-06-29 reinforcement] · new R-AUTO-034 mitigation.

### ⚠️ Risks

| Item | Value |
| --- | --- |
| ID | OD-MS-002 · OD-MS-009 · OD-MS-037 |
| Status | Body unchanged |
| Note | First empirical note reinforced (SFN backend first phase complete · local SFN trigger prevalidation complete before Fargate entry) |

| Item | Value |
| --- | --- |
| ID | R-AUTO-033 |
| Status | 🟢 **Mitigated** |
| Note | [2026-06-29 reinforcement] First validation of Step Functions trigger separation |

| Item | Value |
| --- | --- |
| ID | R-AUTO-034 |
| Status | 🟠 **Open** |
| Note | New · risk of excessive Fargate View `states:StartExecution` permission + bypassing order-capable gate for Step 12+ |

| Item | Value |
| --- | --- |
| ID | Decision Summary |
| Status | Unchanged |
| Note | total 96 · 확정 51 · 잠정 42 retained |

### 📌 Follow-ups

- [ ] (a) Write Dockerfile.
- [ ] (b) ECR repository · image push.
- [ ] (c) Register ECS Task Definition.
- [ ] (d) ECS Service read-only smoke test.
- [ ] (e) Validate Step 1~11 `StartExecution` from Fargate View.
- [ ] (f) Implement follow-up Step 12~17 approval trigger / preflight / paper-order gate.
- [ ] (g) Fargate Task Role least privilege for `states:StartExecution` (state-machine ARN scoped).
- Follow-up phase responsibility: specs 05 · 06 · 10.

### 🔐 Security

| Item | Result |
| --- | --- |
| AWS · EventBridge · Lambda · SFN · SSM · EC2 · RDS · S3 · KIS execution | 0 |
| New AWS resource creation · modification · deletion | 0 |
| port-view MS source changes other than commit `e72de6f` | 0 |
| Changes to 8 MS README · AGENTS · CHANGELOG · docs · worklog · packaging | 0 (spec area) |
| Raw secret recording (KIS · account · token · RDS password · IAM ARN · Slack webhook · Administrator password, etc.) | 0 |

<details>
<summary>🔵 Recorded operational-identifier summary</summary>

- port-view commit hash `e72de6f` · commit message `feat(view): add Step Functions daily batch trigger`.
- Controller endpoint `/daily-batch/aws-stepfunctions/start-range`.
- Spring properties key labels · payload-field labels.
- state names — `StopCrawlerEc2AfterStep11Success` · `SendApprovalRequiredSlack`.
- Slack event label `APPROVAL_REQUIRED` · error label `States.Runtime`.
- placeholder — `[REDACTED]`.

</details>
## 2026-06-29 (View Local Batch Step 12~17 execution passed + common View AWS Paper Batch launcher organized / DB password rotation follow-up registered)

### ✅ Completed

- 🟢 **Pre-Step-12 safety checks passed**

| Item | Result |
| --- | --- |
| REQUESTED `strategy_execution_order` | 0 |
| retryable rejected `connector_order_request` (`40580000` · `EGW00201`) | 0 |
| active `connector_order_request` | 0 |
| Real-order submission targets | None |
| `connector_strategy_order_execute.py` SHA256 | `379895709A7FD1AF6E95D41CF85009FF913A5D60D40788C20630F28730A5F5AE` (same as formally deployed EC2 version) |
| Filter / guard alignment | `--intraday-stop-only` · `signal_type` · retry-normalizer (`40580000` + `EGW00201`) · `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` execute guard |
| Alignment | OD-MS-028 · OD-MS-033 · OD-MS-036 · OD-SAFE-004 |

- 🟢 **Full View Local Batch Step 12~17 execution passed**

| Item | Value |
| --- | --- |
| Daily Pipeline run | `#48` |
| Result | 🟢 **SUCCESS** |
| Execution type | `MANUAL_PARTIAL` |
| Requested by | `VIEW_BUTTON` |
| Account | `[REDACTED]` |
| Execution range | Step 12~17 |
| total · success · no_target · failed · skipped | `6 · 5 · 1 · 0 · 0` |
| duration | `22,336ms` |
| View gate | `executionEnabled=true` · `localFileExecutionEnabled=true` · `fullPipelineExecutionEnabled=false` · `paperOrderEnabled=true` · `minExecutableStepOrder=12` · `maxExecutableStepOrder=17` |
| Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` | 🔵 **NO_TARGET** · `marketconnector_app` · new orders 0 |
| Step 13~16 (`CONNECTOR_ORDER_CHECK` · `SYNC_SELL_FILL` · `SYNC_BUY_FILL` · `SYNC_BUY_POSITION`) | 🟢 **SUCCESS** · all target count 0 |
| Step 17 `BALANCE_REFRESH` | KIS token expired → reissue passed · balance API Status `200` |
| `connector_balance_snapshot id` | `239` |
| `as_of_date` | `2026-06-29` |
| `cash_balance` · `total_eval_amount` | `8,706,505` |
| `eval_profit` | `0` |
| `source_version` | `connector-balance-1.0.0` |
| `connector_position_snapshot` | Empty · holdings 0 |
| `NO_ORDER_SUBMITTED` post-validation | Passed |
| mitigation regression | R-AUTO-019 · R-AUTO-031 · R-AUTO-032 · R-BROKER-004 · R-AUTO-033 all 0 |

- 🟢 **Common View AWS Paper Batch launcher organized**

| Launcher | Role |
| --- | --- |
| `C:/Workspaces/portfolio-local-env/Load-PortfolioViewAwsPaperBatchEnv.ps1` | env loader · `view_app` DB user · tunnel `127.0.0.1:15433` · Batch/local-file ON · full pipeline ON · paper order ON · `1~17` range |
| `C:/Workspaces/portfolio-local-env/Start-PortfolioViewAwsPaperBatch.ps1` | starter · calls env loader → `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=aws-paper` |

| Item | Result |
| --- | --- |
| Tomcat | Started on 8080 |
| DB | Connected to `127.0.0.1:15433/portfolio` |
| Default schema | `ops` |
| Screen allowed range | `1~17` |
| Execution · Local File · Full Pipeline · Paper Order | All ON |
| Execution buttons | Step 1 only · 1~11 · 1~17 · selected range enabled |

- 🟢 **Conclusion**

| Item | Value |
| --- | --- |
| View Local Batch second validation | Complete (Step 1 only · Step 1~11 · Step 12~17 all passed) |
| Flow validation | Local View button → local-source ProcessBuilder execution → AWS Paper DB write · query |
| Per-step DB role override | Validation complete (`view_app` JVM + per-step subprocess separation) |
| Order-capable Step 12 | 🔵 **NO_TARGET** safe termination |
| Local execution orchestration before ECS / Fargate | Validation complete |
| OD-MS-037 | Second-validation partial-completion note reinforced · body unchanged |

### 📌 Follow-ups (cautions registered)

- [ ] (a) Because the common launcher permits all Step 1~17 including paper order-capable Step 10 / 11 / 12, formalize policy requiring four preflight checks before execution (REQUESTED `strategy_execution_order` + retryable rejected + active `connector_order_request` + paper-order gate).
- [ ] (b) 🟠 **History of DB password exposure in conversation** — app-role password rotation required as follow-up.

| Item | Value |
| --- | --- |
| New risk | R-SEC-010 |
| R-DOCS-002 | [2026-06-29 reinforcement] |
| Status | 🟠 **Open** |

- [ ] (c) After rotation, revalidate operator-local secret loader (`Load-PortfolioViewAwsPaperBatchEnv.ps1` or equivalent) + env setup / spec 06 follow-up responsibility / perform cautious checks before starter use until operator completes rotation.

### 🔐 Security

| Item | Result |
| --- | --- |
| New AWS resource creation · modification · deletion | 0 |
| New `connector_order_request` | 0 |
| `--execute` execution on this date | 1 (Step 12 · 🔵 **NO_TARGET**) |
| broker order submission | 0 |
| fill · position-sync automatic retry | 0 |
| aws-live work | 0 |
| broker · KIS calls | Limited to one Step 17 balance refresh + token reissue |
| Spring Boot · 8 MS README · AGENTS · CHANGELOG · docs · worklog changes | 0 (spec area) |
| Raw secret recording (account · KIS · token · RDS password · IAM ARN · Slack webhook · DB password · Administrator password) | 0 |

<details>
<summary>🔵 Recorded operational-identifier summary</summary>

- Pipeline run id `#48` · execution type `MANUAL_PARTIAL` · requester `VIEW_BUTTON` · execution range `Step 12~17` · `6/5/1/0/0` · duration `22,336ms`.
- `connector_strategy_order_execute.py` SHA256 `379895709A7FD1AF6E95D41CF85009FF913A5D60D40788C20630F28730A5F5AE`.
- Six View gates.
- balance snapshot — `id=239` · `as_of_date=2026-06-29` · `cash_balance=8,706,505` · `total_eval_amount=8,706,505` · `eval_profit=0` · `source_version=connector-balance-1.0.0`.
- Two launcher file paths.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-28 (View Local Batch Step 1 + Step 1~11 validation passed + decisive evidence of 6/26 KRX date lag)

### ✅ Completed

- 🟢 **View Local Batch Step 1 standalone execution passed**

| Item | Value |
| --- | --- |
| Daily Pipeline run | `#46` |
| Result | 🟢 **SUCCESS** |
| Execution type | `MANUAL_PARTIAL` |
| Requester | `VIEW_BUTTON` |
| Account | `[REDACTED]` |
| Execution range | Step 1~1 |
| Step 1 `CONNECTOR_BALANCE` | 🟢 **SUCCESS** |
| subprocess | `marketconnector_app` |
| legacy `balance_summary` write | Disabled |
| `connector_balance_snapshot` save | Normal |
| `connector_position_snapshot` empty | Normal |
| Holdings | 0 |
| mitigation regression | R-DATA-005 · R-DATA-011 0 |

- 🟢 **Full View Local Batch Step 1~11 execution passed**

| Item | Value |
| --- | --- |
| Daily Pipeline run | `#47` |
| Result | 🟢 **SUCCESS** |
| Execution type | `MANUAL_PARTIAL` |
| Requester | `VIEW_BUTTON` |
| Execution range | Step 1~11 |
| total · success · no_target · failed · skipped | `11 · 7 · 4 · 0 · 0` |
| duration | `1,362,039ms` |
| Step 1~7 | All 🟢 **SUCCESS** |
| Step 8~11 | 🔵 **NO_TARGET** |
| broker order submission | 0 |
| `--execute` | 0 |

- 🟢 **Per-step DB role override validation passed**

| View / Step | DB Role |
| --- | --- |
| View JVM | `view_app` |
| Step 1 (subprocess) | `marketconnector_app` |
| Step 2 (subprocess) | `crawler_app` |
| Step 3 (subprocess) | `preprocessor_app` |
| Step 4~5 (subprocess) | `research_app` |
| Step 6~7 (subprocess) | `decision_app` |
| Step 8~11 (subprocess) | `execution_app` |

  - First empirical validation — OD-DB-007 · OD-DB-008 · OD-DB-009 · OD-DB-011 · 0 regressions in R-DATA-005 mitigation.

- 🟢 **Decisive evidence of 6/26 KRX date lag**

| Item | Value |
| --- | --- |
| Raw data first generated during Local Step 2 execution | `interest_program_raw` · `interest_shortsell_raw` for `2026-06-25` · `2026-06-26` |
| 6/26 AWS Step Function | `step1-11-approval-20260626-080004-0904111b` 🟢 **SUCCEEDED** |
| Step2B Windows KRX GUI worker | `LastTaskResult=0` |
| Actual worker load date | Prior business day `2026-06-24` |
| Alignment | R-DATA-017 [2026-06-28 decisive evidence] · R-AUTO-020 [2026-06-26 reinforcement] |
| Follow-up priority raised | Strengthen Step2B success criteria (`latest_trade_date` + row_count automatic validation) · show KRX as-of date in Slack · formalize feature-lag policy |

### 📌 Follow-ups

- [ ] Proceed to View Local Batch Step 12~17 validation (OD-MS-037 follow-up phase).
- [ ] Enter order-capable Step 12 only after separate preflight (REQUESTED `strategy_execution_order` + retryable rejected `connector_order_request` + active `connector_order_request` + Spring PAPER_ORDER_GATE feature flag).

### 🔐 Security

| Item | Result |
| --- | --- |
| New AWS resource creation · modification · deletion | 0 |
| KIS · Connector · Slack · Daily Batch trigger other than intended Local View manual execution | 0 |
| New `connector_order_request` | 0 |
| `--execute` | 0 |
| aws-live work | 0 |
| Spring Boot · 8 MS README · AGENTS · CHANGELOG · docs · worklog changes | 0 (spec area) |
| Raw secret recording (account · KIS · token · RDS password · IAM ARN · Slack webhook · DB password · Administrator password) | 0 |

<details>
<summary>🔵 Recorded operational-identifier summary</summary>

- Pipeline run ids `#46` · `#47`.
- Execution type `MANUAL_PARTIAL` · requester `VIEW_BUTTON`.
- Six DB roles.
- duration `1,362,039ms` · step count `11/7/4/0/0`.
- KRX raw load date `2026-06-24` · raw dates newly generated on 2026-06-28: `2026-06-25` · `2026-06-26`.
- 6/26 Step Function execution name `step1-11-approval-20260626-080004-0904111b`.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-27 (View Local AWS Paper read-only first scope complete / local validation passed before ECS Fargate)

### ✅ Completed

- 🟢 **View Local AWS Paper read-only first run passed**

| Item | Value |
| --- | --- |
| Access path | Local Spring Boot → SSM Port Forwarding (`127.0.0.1:15433`) → AWS Paper RDS |
| Alignment | OD-ENV-006 · OD-ENV-007 · OD-NET-010 |
| Spring profile | aws-paper (new) |
| DB user | `view_app` retained |
| Four feature-flag groups (`snapshot-refresh` · `order-refresh` · `strategy.execution.submit` · Slack · Connector execution calls) | all false by default |
| KIS · Connector refresh · Slack · Daily Batch triggers | 0 |

- 🟢 **Screen validations passed**

| Case | Result |
| --- | --- |
| (a) Balance / holdings | `BalanceService` legacy `balance_summary` → latest `connector_balance_snapshot` · holdings based on `connector_position_snapshot` · empty card normal with 0 holdings · `/balance-summary` · `/positions` OK |
| (b) Orders / order detail | Based on `connector_order_request` · `event` · `fill` · `/orders` list + `/orders/56` detail OK · order API calls 0 |
| (c) Strategy / report | `/strategy/execution/plans` plan #113 (new BUY blocked) + `/strategy/reports/latest` (period `2023-01-27 ~ 2026-06-24` · cumulative return `427.69%` · MDD `-8.94%` · Sharpe `2.48`) · server-side POST submit blocked |
| (d) Daily Batch · dashboard | run · step-log read-only query · execution buttons disabled · server-side POST bypass blocked · Step 12+ blocked by default · `/dashboard` wording "AWS Paper · Snapshot 기준 · 조회-only" |

- 🟢 **Conclusion · decision / risk**

| Item | Value |
| --- | --- |
| Local View AWS Paper DB-query read-only console | Operation confirmed |
| First local validation before ECS Fargate complete (spec 05) | Limited to read-only scope |
| New OD-MS-037 | 🟢 **확정** · affected specs 04 · 05 · 10 |
| New R-AUTO-033 | 🟢 **Mitigated** (four Spring feature-flag groups + server-side POST block + staged ENABLE) |
| Decision Summary | total 95 → 96 · 확정 50 → 51 · 잠정 42 retained |

### 📌 Follow-ups

- [ ] Before immediately entering ECS / Fargate, first validate View Local Batch Step 1~17 execution (second local validation).
- [ ] Keep read-only validation and batch-execution validation separate because their risk characteristics differ.
- [ ] Enter ECS deployment design only after second validation passes (follow-up phase responsibility of spec 05 port-view-ecs-and-runbook).

### 🔐 Security

| Item | Result |
| --- | --- |
| New AWS resource creation · modification · deletion | 0 |
| KIS · Connector · Slack · Daily Batch · ECS RunTask · SubmitJob calls | 0 |
| New `connector_order_request` · `--execute` | 0 |
| aws-live work | 0 |
| Spring Boot application properties · Java controller · service · Thymeleaf template changes | port-view MS scope (spec changes 0) |
| Raw secret recording (account · KIS · token · RDS password · IAM ARN · Slack webhook · DB password · Administrator password) | 0 |

<details>
<summary>🔵 Recorded operational-identifier summary</summary>

- `view_app` DB user · `127.0.0.1:15433` local port.
- plan id `113` · order id `56`.
- cumulative return `427.69%` · MDD `-8.94%` · Sharpe `2.48` · backtest period `2023-01-27 ~ 2026-06-24`.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-26 (regular AWS Paper Step 1~11 approval workflow passed + first identification of Step2B KRX date lag)

- 🟢 **08:00 EventBridge Scheduler regular execution result added**

| Item | Value |
| --- | --- |
| Scheduler | `portfolio-paper-daily-step1-11-approval-0800-kst` 🟢 **ENABLED** |
| Dispatcher | Dispatcher Lambda |
| State machine | `portfolio-paper-daily-step1-17-approval` |
| Execution | `step1-11-approval-20260626-080004-0904111b` |
| Status | 🟢 **SUCCEEDED** |
| Step 12~17 | default false · approval gate block retained |
| Slack | `APPROVAL_REQUIRED` received |
| Broker order | 0 |
| New `connector_order_request` | 0 |

  - Alignment: R-AUTO-025 [2026-06-24 reinforcement] mitigation · OD-MS-032 · OD-MS-033.

- 🟢 **Step2B Windows KRX GUI worker success handling**

| Item | Value |
| --- | --- |
| Scheduled Task | `Portfolio-KRX-Worker-Daily` |
| `LastTaskResult` | `0` |
| Status | Running → Ready |
| Invocation path | KRX login / program / shortsell entered normally |
| mitigation regression | R-AUTO-007 · R-AUTO-008 · R-AUTO-020 all 0 |

- 🟠 **First identification of 6/26 KRX as-of-date lag** (confirmed during 2026-06-28 Local Step 1~11 validation)

| Item | Value |
| --- | --- |
| `interest_program_raw` · `interest_shortsell_raw` collection date based on 6/26 08:05 KST worker log | `2026-06-24` |
| Step Function SUCCESS | Aligned with worker process exit 0 |
| Strategy-data freshness perspective | Insufficient `latest_trade_date` validation |
| Risk | R-AUTO-020 [2026-06-26 reinforcement] · new R-DATA-017 |

### 📌 Follow-ups

- [ ] (a) Add `latest_trade_date` + row_count validation after KRX program / shortsell collection to Step2B success criteria (do not treat worker exit 0 alone as SUCCESS · re-evaluate `ExpectedKrxRawDate` calculation in `interest_krx_raw_validate_daily.py`).
- [ ] (b) Display KRX as-of date in Slack `APPROVAL_REQUIRED` message (combined with R-AUTO-027 mitigation expansion · specs 04 · 05 follow-up).
- [ ] (c) Re-evaluate accepted feature lag for `interest_program` · `interest_shortsell` in preprocessor / decision / research (combined with R-DATA-010 · specs 08 · 09 follow-up).

### 🔐 Security

| Item | Result |
| --- | --- |
| New AWS resource creation · modification · deletion | 0 |
| Application-source modification | 0 |
| Changes to 8 MS README · AGENTS · CHANGELOG · docs · worklog | 0 (spec area) |
| AWS CLI · boto3 · SSM · Lambda · SFN · RDS · KIS API execution | 0 |
| Raw secret recording (account · KIS · token · RDS password · IAM ARN · Slack webhook · Administrator password) | 0 |

> SFN execution name · Scheduler name · Lambda name · State Machine name · Slack event label · run_date are recorded as facts only, aligned with the explicit user policy.

## 2026-06-25 (intraday-position Step Function implementation complete + blocked gate / no-target true-path validation passed / real order deferred)

### ✅ Completed

- 🟢 **Progress on stages 1 · 2 · 3 of the three-stage intraday-position-check structure** (performed directly by operator)

| Item | Value |
| --- | --- |
| Kiro scope | `.kiro` root / `_common` spec documentation updates |
| Kiro AWS CLI · boto3 execution | 0 |
| AWS resource creation · modification · deletion | 0 |
| New entrypoint (stage 1) | `connector_intraday_snapshot_refresh.py` |
| Stage-1 source_version | `connector-intraday-snapshot-refresh-1.0.0` |
| Stage-1 sha256 | `99f7d1394fcd28dc5e070c072a9cdd244244df6afd9def09e62cd08b829b2269` |
| New entrypoint (stage 2) | `connector_intraday_position_evaluate.py` |
| Stage-2 source_version | `connector-intraday-position-evaluate-1.1.0` |
| Stage-2 sha256 | `B56C35753C47D6FC72D83DE892D7AC6534CDBF93F2628D9B047F5FBC30CD50A1` |
| Wrapper | `run_intraday_snapshot_and_evaluate.sh` |
| Deployment | MarketConnector EC2 venv (SSM commandId `b44d7c4e-c21c-48c0-a3c0-3a5572935577`) |
| `daily_intraday_position_monitor_run.py` modification | 0 (aligned with OD-MS-035) |

- 🟢 **Dedicated `signal_type=INTRADAY_STOP_SELL` filter patch in `connector_strategy_order_execute.py`**

| Item | Value |
| --- | --- |
| patch sha256 | `379895709A7FD1AF6E95D41CF85009FF913A5D60D40788C20630F28730A5F5AE` |
| Deployment | Directly by operator to EC2 |
| Operational marker | `DEPLOY_CONNECTOR_STRATEGY_ORDER_EXECUTE_INTRADAY_STOP_FILTER=SUCCESS` |
| Forced KIS balance refresh | 1 (SSM commandId `5b1e5757-ea93-4eb6-a7a0-6d5a38d87840`) |
| `rt_cd` · `output1_count` · `output2_count` | `0` · `0` · `1` |
| `as_of_date` · `as_of_ts` | `2026-06-25` · `2026-06-25T06:07:27.438088` |
| Status | `EMPTY_NORMAL` |
| Plaintext quotation of patch body | 0 (aligned with R-DOCS-001 · 03 spec operation-notes follow-up responsibility) |

- 🟢 **New EventBridge Scheduler ENABLED**

| Item | Value |
| --- | --- |
| Scheduler | `portfolio-paper-intraday-snapshot-evaluate-10min-kst` |
| cron | `cron(10/10 9-15 ? * MON-FRI *)` |
| Timezone | Asia/Seoul |
| Validation | Weekday market-hours automatic tick passed |
| Execution alignment | Stage 1 snapshot refresh + stage 2 evaluate |
| broker order submission | 0 |
| Snapshot update | Idempotent |
| Four safety guards | stale snapshot · duplicate order · `sellable_qty` · `current_price` |

- 🟢 **Separate stage-3 State Machine created**

| Item | Value |
| --- | --- |
| State Machine | `portfolio-paper-intraday-stop-sell-approval` |
| Type · Status | STANDARD · 🟢 **ACTIVE** |
| state count | 18 |
| Created at | `2026-06-25T14:58:45+09:00` |
| Daily Step 1~17 SFN | Fully separated |
| approval gate | `CheckIntradayStopApproval` → `BlockedByIntradayStopApprovalGate` |
| true-path 9 states | `RunIntradayStopOrderExecute` · `GetIntradayStopOrderExecuteInvocation` · `RunConnectorOrderCheck` · `GetConnectorOrderCheckInvocation` · `RunSyncSellFill` · `RunConnectorBalanceRefresh` |
| true-path 9 states (continued) | `GetConnectorBalanceRefreshInvocation` · `IntradayStopWorkflowSucceeded` · `IntradayStopWorkflowFailed` |
| Operational markers | `INTRADAY_STOP_SELL_ASL_DRAFT_VALIDATE=SUCCESS` · `INTRADAY_STOP_SELL_IAM_INSPECT=SUCCESS` · `INTRADAY_STOP_SELL_STATE_MACHINE_CREATE=SUCCESS` |
| broker-call delegation | SFN → SSM RunCommand + ECS RunTask.sync → MarketConnector EC2 |
| Direct Lambda broker calls | 0 (no body change to OD-MS-030 · OD-MS-032 · OD-MS-034) |

- 🟢 **Safety test (a) blocked gate**

| Item | Value |
| --- | --- |
| execution name | `intraday-stop-blocked-gate-20260625-145928` |
| input | `allowIntradayStopOrderExecute=false` |
| Status | 🟢 **SUCCEEDED** |
| approval gate | Block aligned |
| true-path entries | 0 |
| Operational marker | `INTRADAY_STOP_SELL_BLOCKED_GATE_TEST=SUCCESS` |

- 🟢 **Safety test (b) true-path no-target**

| Item | Value |
| --- | --- |
| execution name | `intraday-stop-truepath-notarget-20260625-150146` |
| input | `allowIntradayStopOrderExecute=true` |
| Status | 🟢 **SUCCEEDED** |
| Start | `2026-06-25T15:01:46+09:00` |
| Stop | `15:02:53+09:00` |
| true-path state entry | Aligned |
| DB comparison `INTRADAY_STOP_SELL` · `READY/FAILED SELL` | 0 rows |
| `max_connector_order_request_id` | `56` (unchanged) |
| `created_after_truepath_count` | 0 |
| New `connector_order_request` | 0 |
| KIS broker order submission | 0 |

- 🟠 **Actual 1-share `INTRADAY_STOP_SELL` order test stopped · deferred**

| Item | Value |
| --- | --- |
| Holdings | 0 |
| `sellable_qty` | 0 |
| Action | Operator stopped before broker call during prevalidation |
| SELL order against nonexistent position | 0 |
| Resume condition | Next time a holding exists (first empirical validation of new R-BROKER-005 mitigation) |

- 🟢 **Daily Step 17 `RunConnectorBalanceRefresh` run-date made dynamic**

| Item | Value |
| --- | --- |
| Before | Fixed `2026-06-22` |
| After | Dynamic KST `$(TZ=Asia/Seoul date +%F)` |
| Daily ASL change | Follow-up responsibility of 03 spec operation-notes |

### ⚠️ Risks

| Item | Value |
| --- | --- |
| ID | OD-MS-036 |
| Status | 🟢 **확정** |
| Note | New (Intraday Stop Sell Submit Workflow · separate SFN + manual approval + dedicated `signal_type` filter + Daily Step 12 separation · affected specs 03/04/05/10) |

| Item | Value |
| --- | --- |
| ID | R-AUTO-030 |
| Status | 🟠 **Open** |
| Note | [2026-06-25 reinforcement] State Machine created + blocked-gate + no-target validation passed · automatic ENABLE remains blocked |

| Item | Value |
| --- | --- |
| ID | R-AUTO-031 |
| Status | 🟢 **Mitigated** |
| Note | New (approval-gate misconfiguration risk · first empirical blocked-gate test) |

| Item | Value |
| --- | --- |
| ID | R-AUTO-032 |
| Status | 🟢 **Mitigated** |
| Note | New (risk of missing Daily SELL ↔ Intraday Stop Sell `signal_type` filter · dedicated filter patch deployed) |

| Item | Value |
| --- | --- |
| ID | R-BROKER-005 |
| Status | 🟢 **Mitigated** |
| Note | New (risk of real order when holdings are 0 · stopped during prevalidation as intended) |

| Item | Value |
| --- | --- |
| ID | Decision Summary |
| Status | Updated |
| Note | total 94 → 95 · 확정 49 → 50 · 잠정 42 retained |

### 🧾 Kiro work outputs

- This section in `.kiro/WORKLOG.md`.
- 2026-06-25 section in `.kiro/CHANGELOG.md`.
- `.kiro/specs/_common/operator-decisions.md` — Change Log 2026-06-25 + new OD-MS-036 + OD-MS-035 [2026-06-25 reinforcement] + Decision Summary.
- `.kiro/specs/_common/risk-register.md` — R-AUTO-030 [2026-06-25 reinforcement] + new R-AUTO-031 · R-AUTO-032 · R-BROKER-005.
- `.kiro/specs/_common/followups-overview.md` — 2026-06-25 follow-up note.
- `.kiro/specs/_common/ms-aws-service-decision-matrix.md` — fourth 2026-06-25 note (no change to first-choice decision values).
- `.kiro/specs/_common/aws-resource-glossary.md` — no addition on this date (policy: do not modify already-existing items).

### 🔐 Security

| Item | Result |
| --- | --- |
| AWS · EventBridge · Lambda · SFN · SSM · EC2 · RDS · S3 · KIS API calls | All performed directly by operator |
| Kiro AWS CLI · boto3 execution | 0 |
| AWS resource creation · modification · deletion | 0 |
| Plaintext recording of `secretsmanager:GetSecretValue` results | 0 |
| Plaintext quotation of CloudWatch · SFN · KIS response · Lambda · IAM Policy · SFN ASL · patch body · entrypoint body · mojibake SSM stdout | 0 |
| Changes to 8 MS README · AGENTS · CHANGELOG · docs · worklog · source · packaging | 0 (spec area) |
| broker · KIS new BUY / SELL / cancel / modify · `--execute` | 0 |
| New `connector_order_request` | 0 |
| aws-live work | 0 |
| Raw secret recording (KIS · account · token · RDS password · IAM ARN · Slack webhook · Administrator password, etc.) | 0 |

> broker · KIS calls = limited to one forced KIS balance refresh (`EMPTY_NORMAL`) + snapshot refresh from Scheduler automatic ticks. Account-id portions are `[REDACTED]`.

<details>
<summary>🔵 Recorded operational-identifier summary</summary>

- SSM command_id — `b44d7c4e-c21c-48c0-a3c0-3a5572935577` · `5b1e5757-ea93-4eb6-a7a0-6d5a38d87840`.
- execution_name — `intraday-stop-blocked-gate-20260625-145928` · `intraday-stop-truepath-notarget-20260625-150146`.
- cron expression `cron(10/10 9-15 ? * MON-FRI *)` · five operational markers.
- two source_version values · three sha256 values.
- `as_of_date=2026-06-25` · `max_connector_order_request_id=56`.
- placeholder — `[REDACTED]`.
- Recorded as facts in alignment with explicit user policy — not secrets.

</details>

## 2026-06-24 (08:00 Scheduler real-run validation + Step 12 retry-normalizer EGW00201 expansion + manual Step 12~17 run with 4 FILLED)

### ✅ Completed

- 🟢 **08:00 EventBridge Scheduler real-run validation complete**

| Item | Value |
| --- | --- |
| Scheduler | `portfolio-paper-daily-step1-11-approval-0800-kst` (State 🟢 **ENABLED**) |
| Execution path | Scheduler → Dispatcher Lambda → SFN `portfolio-paper-daily-step1-17-approval` |
| Step 1~11 | approval-required flow complete |
| Slack | `APPROVAL_REQUIRED` received |
| Order-submission path after Step 12 | Blocked |
| New order submissions (08:00 basis) | 0 |
| 09:01 Scheduler | `portfolio-paper-daily-step12-17-order-0901-kst` 🟠 **DISABLED** retained |
| First empirical validation | R-AUTO-025 [2026-06-24 reinforcement] · OD-MS-032 · OD-MS-033 |

- 🟢 **Corrected Step 12 order-retry error handling (EGW00201 expansion)**

| Item | Value |
| --- | --- |
| Four remaining 6/23 rows | `REJECTED` |
| `rejection_code` distribution | `40580000` 2 + `EGW00201` 2 |
| Existing retry-normalizer target | `40580000` only |
| `EGW00201` judgment | KIS gateway rate limit / requests-per-second exceeded (not broker order-content error) |
| Modified file | Full replacement of `port-marketconnector/connector_strategy_order_execute.py` |
| Reinforcement | Add `EGW00201` to retry-normalizer + default sleep between orders + backoff retry + record `submit_attempts` in `result_payload` |
| Deployment | S3 upload + formal MarketConnector EC2 deployment |
| Validation | `py_compile` + operational marker passed |
| New OD-MS-033 | 🟢 **확정** |
| R-AUTO-026 | First mitigation validation |
| Plaintext body quotation | 0 (aligned with R-DOCS-001 · follow-up to 03 spec operation-notes) |

- 🟢 **Step 12 dry-run + manual execution validation**

| Item | Value |
| --- | --- |
| retry-normalizer `candidate_count` | 4 (all `40580000` 2 + `EGW00201` 2 recognized) |
| dry-run DB changes | 0 |
| Manual invoke | Dispatcher Lambda `STEP12_17_ORDER` |
| Slack | `DAILY_EXECUTION_SUCCESS` received |
| Four results | New `connector_order_request` + `broker_order_no` created + ultimately filled |

| Item | Value |
| --- | --- |
| Ticker code | `042660` |
| Security name | 한화오션 |
| Result | 🟢 **FILLED** |

| Item | Value |
| --- | --- |
| Ticker code | `004990` |
| Security name | 롯데지주 |
| Result | 🟢 **FILLED** (`PARTIAL_FILLED` → single-order requery · `tot_ccld_qty=69`) |

| Item | Value |
| --- | --- |
| Ticker code | `003490` |
| Security name | 대한항공 |
| Result | 🟢 **FILLED** |

| Item | Value |
| --- | --- |
| Ticker code | `023530` |
| Security name | 롯데쇼핑 |
| Result | 🟢 **FILLED** |

- 🟢 **Decision / risk changes**

| Item | Value |
| --- | --- |
| ID | OD-MS-033 |
| Status | 🟢 **확정** |
| Note | New (Step 12 retry-normalizer `40580000` + `EGW00201` + rate-limit backoff + separate approval hold for automatic 09:01 ENABLE · affected specs 03/04/05/10) |

| Item | Value |
| --- | --- |
| ID | R-AUTO-025 |
| Status | 🟢 **Mitigated** |
| Note | [2026-06-24 reinforcement] First 08:00 real run passed |

| Item | Value |
| --- | --- |
| ID | R-AUTO-026 |
| Status | 🟢 **Mitigated** |
| Note | New (risk of missing rate-limit retry for `EGW00201`) |

| Item | Value |
| --- | --- |
| ID | R-AUTO-027 |
| Status | 🔵 **Accepted** |
| Note | New (operator misunderstanding risk from `APPROVAL_REQUIRED` Slack summary showing 0/0) |

| Item | Value |
| --- | --- |
| ID | Decision Summary |
| Status | Updated |
| Note | total 91 → 92 · 확정 46 → 47 · 잠정 42 retained |

- 🟢 **EC2 lifecycle automation implementation complete** (same-date follow-up)

| Item | Value |
| --- | --- |
| Lambda | `portfolio-paper-ec2-lifecycle-dispatcher` |
| Runtime · Handler | Python 3.12 · `lambda_function.lambda_handler` |
| Timeout · Memory | 30s · 256MB |
| State | 🟢 **Active** |
| IAM Role | New · EC2 start·stop permission granted |
| Environment variables | `TIMEZONE=Asia/Seoul` · `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true` |
| Holiday-check policy | Applied only to start · not to stop |
| Three dryRuns (`start BOTH` · `stop CRAWLER` · `stop MARKETCONNECTOR`) | Passed |
| Weekend skip (`runDate=2026-06-27`) | Passed |

- 🟢 **Two additional EventBridge Schedulers ENABLED**

| Item | Value |
| --- | --- |
| Scheduler | `portfolio-paper-ec2-start-0750-kst` 🟢 **ENABLED** |
| cron | `cron(50 7 ? * MON-FRI *)` · Asia/Seoul |
| Target input | `{"action":"start","target":"BOTH","holidayCheck":true,"reason":"PRE_DAILY_STEP1_11","dryRun":false}` |

| Item | Value |
| --- | --- |
| Scheduler | `portfolio-paper-marketconnector-stop-1550-kst` 🟢 **ENABLED** |
| cron | `cron(50 15 ? * MON-FRI *)` · Asia/Seoul |
| Target input | `{"action":"stop","target":"MARKETCONNECTOR","holidayCheck":false,"reason":"POST_MARKET_CLOSE","dryRun":false}` |

  - Confirmed aligned `get-schedule` responses for both Schedulers. Scheduler invoke Role · Lambda invoke permissions have Resource/Action wildcard 0.

- 🟢 **Step Functions success-path change**

| Item | Value |
| --- | --- |
| State machine | `portfolio-paper-daily-step1-17-approval` |
| Flow | `Step6ToStep11_Succeeded` → `StopCrawlerEc2AfterStep11Success` (EC2 lifecycle · Crawler stop) → `SendApprovalRequiredSlack` → `Step12_CheckApproval` |
| On Step 1~11 failure | `SendDailyExecutionFailedSlack` (`portfolio-event-notifier` · keep Crawler EC2 running) |
| Status | 🟢 **ACTIVE** |
| RevisionId | `edd92cc9-1d94-4752-9a43-b7eb5b2f3c2c` |
| SFN execution role | Added EC2 lifecycle Lambda invoke permission |
| New OD-MS-034 | 🟢 **확정** · affected specs 03/04/05/08/10 |

- 🟢 **Three-way Lambda responsibility separation**

| Launcher | Role |
| --- | --- |
| `portfolio-paper-daily-scheduler-dispatcher` | 08:00 / 09:01 SFN schedule dispatcher |
| `portfolio-paper-ec2-lifecycle-dispatcher` | 07:50 / 15:50 EC2 start·stop dispatcher · Crawler stop after successful Step 1~11 |
| `portfolio-event-notifier` | Slack notifications only |

| Item | Value |
| --- | --- |
| Risk | R-AUTO-028 |
| Status | 🟢 **Mitigated** |
| Note | New (risk of EC2 lifecycle Scheduler · Lambda configuration error) |

| Item | Value |
| --- | --- |
| Risk | R-AUTO-025 |
| Status | 🟢 **Mitigated** |
| Note | [second 2026-06-24 reinforcement] EC2 lifecycle automation chain also ENABLED |

| Item | Value |
| --- | --- |
| Risk | Decision Summary |
| Status | Updated |
| Note | total 92 → 93 · 확정 47 → 48 · 잠정 42 retained |

| Item | Value |
| --- | --- |
| Risk | Real business-day validation |
| Status | First validation planned for next business day (07:50 start · Crawler stop · 15:50 stop) |
| Note |  |

- 🟢 **Final intraday-position-check design fixed / implementation planned for next day**

| Item | Value |
| --- | --- |
| Actual AWS · application-source execution | 0 |
| Stage 1 (MarketConnector 10-min Snapshot Refresh) | Scheduler → Lambda → SSM → MC EC2 · refresh balance/position snapshots · 0 judgment/order · idempotent |
| Stage 2 (StrategyExecution Intraday Evaluate) | SSM immediately after stage 1 · persist `strategy_intraday_position_check` + create READY order only · broker orders 0 · four safety guards · new-file implementation · prohibit SFN Retry automatic order connection |
| Stage 3 (Intraday Stop Sell Submit & Refresh) | Separate SFN · dedicated `source_type=INTRADAY_STOP_SELL` filter · initial manual approval before execution · separated from Daily BUY/SELL |
| New OD-MS-035 | 🟢 **확정** · affected specs 03/04/05/10 |

| ID | Status |
| --- | --- |
| R-DATA-014 (incorrect stop-loss based on stale snapshot) | 🟢 **Mitigated** |
| R-AUTO-029 (duplicate `INTRADAY_STOP_SELL` READY) | 🟢 **Mitigated** |
| R-DATA-015 (stop-loss order when `sellable_qty` insufficient) | 🟢 **Mitigated** |
| R-DATA-016 (`current_price` missing · abnormal) | 🟢 **Mitigated** |
| R-AUTO-030 (risk of premature stage-3 ENABLE) | 🟠 **Open** |
| Decision Summary | total 93 → 94 · 확정 48 → 49 · 잠정 42 retained |

### 📌 Follow-ups

- [ ] (a) Improve 0/0 display in `APPROVAL_REQUIRED` Slack summary (R-AUTO-027 mitigation expansion · specs 04 · 05).
- [ ] (b) Reinforce Dispatcher Lambda application logging (spec 04).
- [ ] (c) Keep 09:01 Scheduler automatic ENABLE deferred (aligned with OD-MS-033 · final decision after Slack-summary improvement + Lambda log reinforcement + additional operational runs).
- [ ] (d) Move Slack webhook URL to Secrets Manager / SSM Parameter Store (aligned with R-AUTO-024 · spec 06).
- [ ] (e) balance refresh `EGW00215` rate-limit backoff · retry policy (retain 2026-06-23 follow-up §1).
- [ ] Planned next day — stage 1 Scheduler·Lambda·SSM · stage 2 new evaluate file + four safety guards · stage 3 separate SFN + Slack (automatic ENABLE deferred).
- [ ] Lower-level document updates for specs 03 · 04 · 05 · 06 · 10 remain follow-up-phase responsibility.
- Kiro updated only root · `_common` documentation on this date; actual AWS CLI · boto3 execution 0 · AWS resource creation · modification · deletion 0. No plaintext quotation of Lambda code body · full IAM Policy · full Step Functions ASL · Lambda responses · CloudWatch Logs · KIS API responses · actual ARN
  or instance-id (aligned with R-DOCS-001). Changes caused by this work to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging: 0 (spec area). `daily_intraday_position_monitor_run.py` modification: 0.
- Actual AWS / EventBridge Scheduler / Lambda / Step Functions / SSM / EC2 / RDS / S3 / Slack webhook / KIS API work was performed directly by the operator. Kiro only updated root / `_common` documentation. AWS CLI / boto3 execution 0 / AWS resource creation·modification·deletion 0 / plaintext recording of `secretsmanager:GetSecretValue` results 0 / plaintext quotation of CloudWatch Logs, Step Functions history, KIS API response body, Lambda response body, `connector_strategy_order_execute.py` patch body 0.
  Changes caused by this date's work to 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging: 0 (spec area). The operator's direct full replacement patch of `connector_strategy_order_execute.py` remains follow-up responsibility for 03 spec operation-notes. broker / KIS calls = four Step 12 paper resubmissions (`042660` `004990` `003490`
  `023530` BUY MARKET) + Step 13 fill queries only / additional BUY SELL cancel modify 0 / aws-live work 0.
- No newly recorded sensitive information (secret value · KIS app key · KIS app secret · account number · account password · token · RDS password · RDS endpoint hostname · account-id · actual IAM Role ARN · actual secret ARN · IAM access key id · instance-id · EIP · image digest full sha256 · task ARN
  job ARN · raw broker_order_no · raw broker_branch_code · KIS paper login credential · Slack webhook URL · Step Functions ARN · Lambda ARN). All represented as `[REDACTED]` or placeholders. Only operational identifiers (rejection_code `40580000`
  · `EGW00201` · dry-run `candidate_count=4` · ticker codes `042660` · `004990` · `003490` · `023530` · security names · `004990` PARTIAL_FILLED → FILLED transition · `tot_ccld_qty=69` · two Scheduler names · Dispatcher Lambda name · two Step Functions state-machine names
  · scheduleType label `STEP12_17_ORDER` · Slack event labels `APPROVAL_REQUIRED` · `DAILY_EXECUTION_SUCCESS` · run_date `2026-06-24`) are recorded as facts in alignment with explicit user policy and are not secrets.
## 2026-06-23 (Step Functions approval live validation + Step 12 retry-normalizer + confirmed normal Korean DB storage)

- 🟢 **Live validation of Step Functions state machine `portfolio-paper-daily-step1-17-approval` complete**

| Item | Value |
| --- | --- |
| Step 1~17 `allowPaperOrderExecute=false` blocked | Passed |
| Step 12~17 `allowPaperOrderExecute=true` true path | Passed (after prior false-path validation and REQUESTED-state confirmation → approved true-path execution) |
| First real SELL | BGF리테일 `282330` 17 shares MARKET |
| `connector_order_request id 48` · `strategy_execution_order id 40` | 🟢 **FILLED** |
| `broker_order_no` | `0000006143` |
| Holdings after Step 17 balance refresh | 4 securities reflected normally |
| Decision · risk | New OD-MS-029 · R-AUTO-021 [2026-06-23 reinforcement] |

- 🟢 **Step 12 retry-normalizer operational reinforcement**

| Item | Value |
| --- | --- |
| Background risk (new R-AUTO-022) | After market-close REJECTED / `40580000`, if execution_order still points to rejected `connector_order_request_id`, next-day Step 12 automatic resubmission path (`REQUESTED` + default filter `connector_order_request_id IS NULL`) is broken |
| Target file | Full replacement of `port-marketconnector/connector_strategy_order_execute.py` |
| Applied location | Beginning of Step 12 · not a separate Step 11.5 |
| Recovery condition | `execution_mode = PAPER_STRATEGY` + `action_type IN (BUY, SELL)` + `execution_status IN (READY, FAILED)` + `connector_order_request_id IS NOT NULL` |
| Recovery condition (continued) | linked `connector_order_request.request_status = REJECTED` + `rejection_code = 40580000` + `broker_order_no IS NULL` + no `connector_fill` |
| Recovery action | `execution_status = REQUESTED` + `connector_order_request_id = NULL` + store old request history in `result_payload.retry_normalizer` |
| Formal EC2 deployment | Complete |
| `.venv/bin/python` dry-run | Passed · retry candidates 0 · REQUESTED orders 0 |
| SFN Step 12 | Aligned to use `.venv/bin/python` (new OD-MS-028) |

- 🟢 **Corrected DB Korean-display issue**

| Item | Value |
| --- | --- |
| PGAdmin4 targets | `connector_order_request` · `strategy_execution_order` · `connector_api_call_log` · `connector_position_snapshot` |
| Storage / query | All normal |
| `server_encoding` · `client_encoding` | Both `UTF8` |
| Conclusion | Not a DB Korean-corruption issue · console display encoding issue in PowerShell / SSM / AWS CLI path · no new DB risk · separated as 2026-06-23 follow-up in followups-overview |

- 🟢 **Step 17 balance-refresh follow-up**

| Item | Value |
| --- | --- |
| EGW00123 token expiration → reissue | Passed |
| EGW00215 rate limit | Confirmed |
| balance-refresh rate-limit backoff · retry policy | Retained as follow-up (aligned with followups-overview 2026-06-23 §1 · new Risk ID deferred) |

- 🟢 **Common AWS Slack notifier implementation complete**

| Item | Value |
| --- | --- |
| Lambda | `portfolio-event-notifier` (Runtime Python 3.12 · IAM Role `portfolio-event-notifier-lambda-role`) |
| Role | Operational-event notification helper callable from SFN · EventBridge · EC2 SSM · Batch · Lambda |
| Manual `hello wook` invoke | Success · Portfolio Daily Bot received |
| Completion markers | `SLACK_LAMBDA_SMOKE_TEST=SUCCESS` · `PORTFOLIO_EVENT_NOTIFIER_TEMPLATE_TEST=SUCCESS` |
| Six message templates | pre-market balance · Daily validation complete · Daily execution success · Daily execution failure · intraday stop-loss · post-market balance · 🔴 · 🔵 · ⚪ P/L emoji + Slack attachment color bar |
| port-view SlackNotificationService | Retained · remains responsible for View Daily Batch manual-execution notifications · not removed/replaced · only future migration possibility recorded |
| webhook URL handling | First validation via Lambda env var `SLACK_WEBHOOK_URL` · value not recorded (aligned with R-DOCS-001) · planned move to Secrets Manager or SSM Parameter Store (new OD-MS-030 · new R-AUTO-024 · Status 🔵 **Accepted**) |

- 🟢 **Validated receipt of three Slack events in Step Functions approval workflow**

| Event | Result |
| --- | --- |
| (1) `APPROVAL_REQUIRED` | Slack received when entering approval gate after Step 1~11 completion · manual-approval awareness flow validated |
| (2) `DAILY_EXECUTION_SUCCESS` | Slack received after successful completion through Step 17 |
| (3) `DAILY_EXECUTION_FAILED` | SFN Catch · 12~17 test-only + 1~17 full test-only failure ASL · receipt validation passed · 0 intentionally induced real broker-order failures |
| Decision · risk | New OD-MS-031 (🟢 확정) · new R-AUTO-023 (Status 🟢 **Mitigated**) |

- 🟢 **6. Slack implementation: complete / 7. EventBridge automation is next stage**

| Item | Value |
| --- | --- |
| Initial EventBridge / SFN Slack scope | Limited to `APPROVAL_REQUIRED` + `DAILY_EXECUTION_SUCCESS` + `DAILY_EXECUTION_FAILED` |
| Follow-ups separated | Pre-market balance · post-market balance · intraday stop-loss alert · scheduled trigger · formal production automation · DLQ · retry · CloudWatch Alarm · Slack webhook Secrets Manager migration · follow-up phase for specs 04 · 05 · 06 · 10 |

- 🟢 **7. EventBridge automation implementation: complete** (new OD-MS-032 · 🟢 확정)

| Item | Value |
| --- | --- |
| Two Schedulers | 08:00 KST `portfolio-paper-daily-step1-11-approval-0800-kst` + 09:01 KST `portfolio-paper-daily-step12-17-order-0901-kst` |
| Dispatcher Lambda | `portfolio-paper-daily-scheduler-dispatcher` (Python 3.12 · Handler `lambda_function.lambda_handler` · Timeout 30s · Memory 256MB · State Active) |
| Four IAM Role items | `portfolio-paper-eventbridge-scheduler-role` · `portfolio-paper-daily-scheduler-dispatcher-role` · two ACTIVE SFN state machines aligned |
| Three inline policies | `portfolio-paper-scheduler-start-execution-policy` + `portfolio-paper-scheduler-invoke-dispatcher-policy` + `portfolio-paper-daily-scheduler-dispatcher-policy` · Resource/Action wildcard 0 |
| Dispatcher Lambda responsibility | Generate KST `runDate` (YYYY-MM-DD) + skip weekends / market holidays + branch payload by scheduleType + SFN `StartExecution` + return immediately |

- 🟢 **EventBridge automation validation complete**

| Validation | Result |
| --- | --- |
| `simulate-principal-policy` | both `states:StartExecution` + `lambda:InvokeFunction` 🟢 **allowed** |
| Lambda dryRun (08:00 `STEP1_11_APPROVAL` · 09:01 `STEP12_17_ORDER`) | `started=false` · `reason=DRY_RUN_NO_START_EXECUTION` · target state machine + `allowPaperOrderExecute` branch aligned |
| Scheduler `get-schedule` | 08:00 🟢 **ENABLED** · 09:01 🟠 **DISABLED** staged activation |
| Lambda environment variables | `TIMEZONE=Asia/Seoul` · `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true` · Step1~17 / Step12~17 ARN (plaintext records 0 · aligned with R-DOCS-001) |

- 🟢 **EventBridge staged-activation alignment**

| Item | Value |
| --- | --- |
| 08:00 Scheduler | 🟢 **ENABLED** · next-day 08:00 KST run · Scheduler → Dispatcher Lambda → Step1~17 approval · `allowPaperOrderExecute=false` · Step 12 approval gate block · no order submission |
| 09:01 Scheduler | 🟠 **DISABLED** · next-day 09:01 automatic execution blocked · no automatic Step12~17 execution · final operator decision after last safety checks before order-automation ENABLE |
| Real `StartExecution` (`dryRun=false`) | Not validated on this date · first validation planned at next-day 08:00 (Scheduler invocation · Lambda logs · SFN execution created · `APPROVAL_REQUIRED` Slack receipt · Step 12 approval gate block · no order submission) |
| New R-AUTO-025 | Status 🟢 **Mitigated** |

- 🟢 **Scope · safety · outputs**

| Item | Value |
| --- | --- |
| Actual AWS resources · code changes · DB GRANT | All performed directly by operator · Kiro updated root / `_common` docs only |
| Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging in spec area | 0 |
| Operator direct full replacement of `connector_strategy_order_execute.py` · EC2 deployment · dry-run result | Split into follow-up responsibility for `_common` metadata · 03 spec operation-notes (second/third passes) |
| Plaintext recording of Slack notifier Lambda code · SFN Catch ASL · Slack messages · webhook URL · Dispatcher Lambda code · SFN ARN | 0 (aligned with R-DOCS-001) |

## 2026-06-22 (Daily AWS Paper Wrapper completed 1~17 + MarketConnector env bootstrap + Step 9 permission correction + SELL E2E validation)

- 🟢 **Daily AWS Paper Wrapper Step 1~17 operational completion**

| Item | Value |
| --- | --- |
| wrapper | `.kiro/scripts/run-daily-aws-paper.ps1` |
| Scope | Full operational execution of Step 1~17 |
| Completion | Real Paper SELL order submission · KIS acceptance · fill query · SELL fill sync · position CLOSED · balance refresh end-to-end passed |
| Meaning | First complete operational run of the AWS wrapper |
| Environment · RunDate · region | `aws-paper` · `2026-06-22` · `ap-northeast-2` |
| Reference | `_common/operator-decisions.md` Change Log · 03 · 04 · 08 · 09 spec operation-notes 2026-06-22 |

- 🟢 **MarketConnector `/tmp/inject-env.sh` volatility and bootstrap patch**

| Item | Value |
| --- | --- |
| Initial Step 1 failure cause | Runtime env bootstrap file missing because `/tmp` is volatile after MC EC2 stop / start |
| patch | MC env-bootstrap function in `daily-aws-paper.functions.ps1` · common call from Step 1 · 12 · 13 · 17 |
| Value handling | Extract internal keys from Secrets Manager JSON SecretString (APP_KEY · APP_SECRET · PAPER_ACNT · ACNT_PRDT_CD) + export APP_* · KIS_* aliases · do not log secret values · keep `/tmp/inject-env.sh` permission 700 |
| Validation | PowerShell parser validation passed · Step 1 rerun passed |
| Decision · risk | New OD-MS-027 · first empirical mitigation validation for new R-AUTO-021 |

- 🟢 **Missing Step 9 `execution_app` UPDATE permission and GRANT correction**

| Item | Value |
| --- | --- |
| Initial failure | Step 9 `DAILY_SELL_EXECUTION` lacked UPDATE permission on `decision.strategy_daily_position_decision` |
| Background | `execution_app` previously had SELECT only · Step 9 must update `execution_order_id` in daily-position-decision row after creating SELL execution_order |
| GRANT (performed directly by operator) | `GRANT USAGE ON SCHEMA decision TO execution_app` + `GRANT UPDATE ON TABLE decision.strategy_daily_position_decision TO execution_app` |
| Result | SELECT + UPDATE confirmed · Step 9~11 rerun passed |
| Decision · risk | New OD-DB-011 · first mitigation validation for new R-DATA-013 · R-DATA-005 [2026-06-22 reinforcement] |

- 🟢 **Step 12 한화생명 244-share SELL_HARD_STOP MARKET E2E**

| Item | Value |
| --- | --- |
| execution_plan_id | `96` (`plan_date 2026-06-22` · `strategy_name strategy_ai` · `market_signal DEFENSIVE` · `risk_regime DEFENSIVE` · `plan_status PARTIALLY_BLOCKED`) |
| Plan metrics | total_candidate 3 · ready 1 · blocked 2 · skipped 0 · total_target_amount `1,237,080` · available_cash `-62,763` · max_order_amount `-31,381.50` |
| Step 12 real target | One `088350` 한화생명 SELL 244 MARKET (2 BUY BLOCKED for insufficient cash · duplicate orders 0 rows) |
| SELL reason `SELL_HARD_STOP` | entry_date `2026-06-17` · entry_price `5,744.4057` · current_price `5,070` · expected_pnl_rate approximately `-11.7402%` · hard_stop_loss_rate `-10%` · holding_days 5 · snapshot_qty<br>sellable_qty · remaining_qty all 244 · expected_pnl_amount approximately `-164,554.9908` |
| Precheck | `strategy_position_state id 9` OPEN entry_qty 244 · remaining_qty 244 matched Step 12 SELL qty 244 |
| Step 12 `-AllowPaperOrderExecute` | execution_order id `37` 🟢 **SUBMITTED** · connector_order_request id `46` 🟢 **ACCEPTED** · request_type SELL · order_method MARKET · order_qty 244 · broker_order_no / broker_branch_code created · no rejection |
| Step 12 `-AllowPaperOrderExecute` (continued) | No plaintext broker response recorded here (aligned with R-DOCS-001) |
| Step 13 fill query | connector_order_request id `46` 🟢 **FILLED** · connector_fill id `34` · fill_qty 244 · fill_price `5,075.8607` · fill_amount `1,238,510.01` · side SELL · fill_ts `2026-06-22 00:46:58 UTC` |
| Step 14 / 15 / 16 · Step 17 | ECS exitCode 0 · SSM 🟢 **Success** |
| Final DB validation | `strategy_execution_order id 37` 🟢 **FILLED** · `strategy_position_state id 9` remaining_qty 0 · position_status 🟢 **CLOSED** · latest_sell_reason `SELL_HARD_STOP` |
| Latest balance | `connector_position_snapshot` created_at `2026-06-22 00:50:50 UTC` · 5 holdings (`003490` 58 · `004990` 69 · `023530` 8 · `042660` 11 · `282330` 17) · confirmed `088350` 한화생명 removed from snapshot |

- 🟢 **Decision · risk changes**

| Item | Value |
| --- | --- |
| ID | New OD-MS-027 |
| Status | 🟠 **잠정** |
| Note | MC env-bootstrap regeneration operating policy · discard assumption that `/tmp/inject-env.sh` already exists · regenerate at step execution time · shared wrapper function · do not print secret values |

| Item | Value |
| --- | --- |
| ID | New OD-DB-011 |
| Status | 🟢 **확정** |
| Note | Limited UPDATE permission for `execution_app` on decision.strategy_daily_position_decision · responsibility limited to SELL execution-link update |

| Item | Value |
| --- | --- |
| ID | OD-MS-016 · OD-MS-021 · OD-MS-023 |
| Status | Reinforced |
| Note | First validation of second full wrapper 1~17 operational completion |

| Item | Value |
| --- | --- |
| ID | New R-AUTO-021 |
| Status | 🟢 **Mitigated** |
| Note | Failure of Step 1 · 12 · 13 · 17 because `/tmp` is volatile after MC EC2 stop/start |

| Item | Value |
| --- | --- |
| ID | New R-DATA-013 |
| Status | 🟢 **Mitigated** |
| Note | Missing `execution_app` UPDATE permission on decision schema |

| Item | Value |
| --- | --- |
| ID | R-DATA-005 [2026-06-22 reinforcement] |
| Status | Reinforced |
| Note | Missing `execution_app` decision schema USAGE + table UPDATE caused initial Step 9 failure · formal 02 spec update remains follow-up |

- 🟢 **Kiro outputs**

| File | Change |
| --- | --- |
| `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` 2026-06-22 | New |
| `.kiro/README.md` | Progress summary (reference date 2026-06-22 · second Backend AWS E2E operational completion) |
| `_common/operator-decisions.md` | Change Log · new OD-MS-027 · OD-DB-011 · Summary 84 → 86 · 잠정 39 → 40 · 확정 42 → 43 |
| `_common/risk-register.md` | New R-AUTO-021 · R-DATA-013 + R-DATA-005 reinforcement |
| `_common/followups-overview.md` · `_common/ms-aws-service-decision-matrix.md` | 2026-06-22 follow-up · E2E rehearsal second run |
| 03 · 04 · 08 · 09 spec operation-notes 2026-06-22 | Accumulated |
| 03 spec runbook · tasks · validation-checklist | env injection · tasks 7 · 26 · 27 · validation |
| 08 spec tasks | Reinforced tasks 57 · 105 · 110 |

- 🟢 **Safety · security**

| Item | Result |
| --- | --- |
| AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · RDS · GRANT · KIS | All performed directly by operator · Kiro organized documentation/procedures only |
| AWS CLI · boto3 · AWS resource creation · modification · deletion | 0 |
| Plaintext recording of `secretsmanager:GetSecretValue` results | 0 |
| Full quotation of CloudWatch Logs · SSM responses · KIS API response body | 0 |
| broker / KIS calls | One KIS paper SELL (Step 12) + balance / order-check queries |
| Additional BUY · cancel · modify · `--execute` · fill · position-sync automatic retry · aws-live work | 0 |
| Sensitive information (secret · KIS · account · token · RDS · account-id · IAM · secret ARN · access key · instance-id · EIP · image digest · task ARN · job ARN · broker_order_no · broker_branch_code · KIS paper credential · Administrator password) | No new records · all `[REDACTED]` or placeholders |

<details><summary>🔵 Operational-identifier summary</summary>

| Item | Value |
| --- | --- |
| `execution_plan_id` | `96` |
| execution_order id | `37` |
| connector_order_request id | `46` |
| connector_fill id | `34` |
| position_state_id | `9` |
| Ticker codes | `088350` · `003490` · `004990` · `023530` · `042660` · `282330` |
| data_date / signal_date · run_date | `2026-06-22` |
| fill_ts · created_at | UTC |

</details>

## 2026-06-21 (strengthened Step 2 `INTEREST_CRAWLER` success criteria + KRX raw DB validation integration)

- 🟢 **Background · result summary**

| Item | Value |
| --- | --- |
| Input | 2026-06-20 follow-up note (EC2 lifecycle reinforcement · prohibit Step 12 before separate approval) + 6/19 KRX raw-freshness recovery |
| Strengthened scope | Step 2 wrapper success = (a) Chrome · chromedriver best-effort reset · (b) Scheduled Task Running → Ready wait + Last Result 0 · 0x0 · (c) latest worker log path · size · tail output · (d) KRX raw DB validation passed |
| Actor | Operator directly patched · validated · accumulated only in this spec area |
| 8 MS source · README · docs · worklog changes | 0 |
| Reference | `_common/operator-decisions.md` (new OD-MS-026) · 08 spec operation-notes 2026-06-21 · `_common/followups-overview.md` 2026-06-21 |

- 🟢 **Changes**

| Item | Value |
| --- | --- |
| (a) `step-02-interest-crawler.ps1` | Chrome · chromedriver stale reset · Scheduled Task `Portfolio-KRX-Worker-Daily` `schtasks /Run` · poll Running → Ready · only Last Result 0 / 0x0 is 🟢 **SUCCESS** · latest log path · size · tail |
| (a) `step-02-interest-crawler.ps1` (continued) | <span style="color:#D1242F">**fail-closed**</span> if worker EC2 is not running |
| (b) non-GUI ECS RunTask overrides | `TEMP=/tmp` · `TMP=/tmp` · `PYTHONUTF8=1` · `PYTHONIOENCODING=utf-8` + shared function `Invoke-DailyAwsPaperEcsTask` `EnvironmentVariables` · `New-SsmParameterFile` `ExecutionTimeoutSeconds` · `Invoke-SsmCommandAndWait` `ExecutionTimeoutSeconds` |
| (c) New script | `interest_krx_raw_validate_daily.py`<br>Validate expected trade_date for `interest_program_raw` · `interest_shortsell_raw`<br>row_count + max(trade_date) ≥ expected · exit 30 on failure<br>local py_compile + marker passed |
| (d) S3 presigned-URL deployment | S3 bucket `portfolio-paper-migration-yukiever`<br>key `tmp/krx/interest_krx_raw_validate_daily.py` → EC2<br>SSM commandId `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3` |
| (e) Standalone EC2 validation | user=`crawler_app` · schema=`interest` · search_path `interest, reference, legacy, public` · `interest_program_raw` `2026-06-19` count=`1` OK · `interest_shortsell_raw` `2026-06-19` count=`349` OK · exit 0 |
| (e) Standalone EC2 validation (continued) | SSM commandId `c844aea5-1429-430a-9510-39fc99f17f05` |
| (f) Wrapper integration | `step-02-interest-crawler.ps1` calculates `ExpectedKrxRawDate` (prior business day based on RunDate)<br>→ runs worker-EC2 SSM `INTEREST_CRAWLER_KRX_DB_VALIDATE`<br>Step 2 fails on non-zero exit / row_count 0<br>records `KrxDbValidationCommandId` in step result |

- 🟢 **Validation milestone (2026-06-21)**

| Item | Value |
| --- | --- |
| Standalone wrapper execution | `.\run-daily-aws-paper.ps1 -Environment aws-paper -RunDate 2026-06-20 -StartStep 2 -EndStep 2` |
| RunId · StepCode · Status · Runner · ExpectedKrxRawDate | `daily-aws-paper-20260621-204017` · `INTEREST_CRAWLER` · 🟢 **SUCCESS** · `ECS+SSM` · `2026-06-19` |
| (a) non-GUI ECS | taskDefinition `portfolio-paper-interest-crawler:7` · taskId `78979b5cbb714d0eb94f5946e15a14ce` · exitCode 0 · stoppedReason `Essential container in task exited` · CloudWatch log saved |
| (b) KRX GUI worker | state `running` · commandId `f9d82fcc-1e26-4710-87c3-1d20483b63ef` · 🟢 **Success**<br>`TaskStatus=Running` → `Ready` (elapsedSeconds=111)<br>Logon `Interactive only`<br>Run As `Administrator`<br>latest log `krx_worker_daily_20260621_114154.log` |
| (c) KRX raw DB validation | SSM `INTEREST_CRAWLER_KRX_DB_VALIDATE` · commandId `2279c6d7-2da6-4317-9c10-7cc77374b317` · 🟢 **Success** · DB user=`crawler_app` · schema=`interest` · `interest_program_raw` `2026-06-19` count=`1` OK |
| (c) KRX raw DB validation (continued) | `interest_shortsell_raw` `2026-06-19` count=`349` OK |

- 🟢 **Decision · risk changes**

| Item | Value |
| --- | --- |
| ID | New OD-MS-026 |
| Status | 🟠 **잠정** |
| Note | Step 2 success criterion extends through KRX raw DB validation, not merely Scheduled Task trigger · KRX GUI = Windows Administrator interactive Scheduled Task · wrapper orchestrates run · wait · Last Result · latest log · DB validation |
| Note (continued) | validation script checks program · shortsell freshness · worker stopped is fail-closed |

| Item | Value |
| --- | --- |
| ID | New R-AUTO-020 |
| Status | 🟢 **Mitigated** |
| Note | Risk of propagating missing KRX raw data to Step 3+ if Step 2 SUCCESS is based only on Scheduled Task trigger · mitigation = Chrome/chromedriver best-effort reset + Running → Ready wait + Last Result + latest log + DB validation + worker-stopped fail-closed |

| Item | Value |
| --- | --- |
| ID | R-AUTO-007 mitigation · detection |
| Status | Reinforced |
| Note | First validation of automatic DB-validation output inside wrapper · promoted task-level follow-up completion at task 57 · `KrxDbValidationCommandId` step result · automatic expected-date row_count check |

| Item | Value |
| --- | --- |
| ID | R-AUTO-016 mitigation |
| Status | Reinforced |
| Note | Replaced previous "automatic skip inside wrapper" with fail-closed · immediate failure if crawler worker EC2 stopped · print instanceId · state · block Step 2 SUCCESS if KRX GUI worker / DB validation not executed |

| Item | Value |
| --- | --- |
| ID | R-AUTO-017 mitigation |
| Status | Reinforced |
| Note | First empirical Chrome/chromedriver best-effort reset · failure is warning · follow-up EC2 stop/restart combined with R-AUTO-016 reinforcement |

- 🟢 **Kiro outputs**

| File | Change |
| --- | --- |
| `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` 2026-06-21 | New |
| `.kiro/README.md` | Updated "Windows KRX crawler worker and Step 2 behavior" fail-closed + standalone Step 2 execution example |
| `_common/operator-decisions.md` | Change Log · new OD-MS-026 · Summary update |
| `_common/risk-register.md` | New R-AUTO-020 + reinforced R-AUTO-007 · R-AUTO-016 · R-AUTO-017 |
| `_common/followups-overview.md` · `_common/ms-aws-service-decision-matrix.md` | 2026-06-21 follow-up · Step 2 success criteria + worker-stopped fail-closed |
| 08 spec operation-notes 2026-06-21 | Accumulated |
| 08 spec tasks | tasks 57 · 92 · 94 complete + new task items |
| 08 spec design · requirements | Step 2 success criteria + fail-closed + Acceptance Criteria reinforcement |

- 🟢 **Safety · security**

| Item | Result |
| --- | --- |
| AWS · SSM · EC2 · S3 · ECS · RDS · KRX calls | All performed directly by operator · Kiro organized documentation · procedures · validation only |
| AWS CLI · boto3 · AWS resource creation · modification · deletion | 0 |
| broker / KIS · new BUY · SELL · cancel · modify · `--execute` · fill · position-sync automatic retry · aws-live work | 0 |
| RDS DDL · DML | 0 (validation SQL SELECT only · new `interest_program_raw` · `interest_shortsell_raw` rows are results of KRX worker load · Step 2 wrapper on this date idempotent · no-op) |
| Sensitive information (secret · KIS · account · token · RDS · account-id · IAM · secret ARN · access key · EIP · image digest · task ARN · job ARN · KIS paper credential · Administrator password · actual S3 presigned URL) | No new records · all `[REDACTED]` or placeholders |

- 🟢 **Next work**

| Item | Value |
| --- | --- |
| Close for this date | Step 2 reinforcement complete |
| Separated follow-ups | automated EC2 lifecycle start · SSM Online wait · stop procedure · mixed ECS RunTask + SSM RunCommand orchestration in SFN · display `KrxDbValidationCommandId` · latest worker log · Step 2 validation result in View Daily Batch · centralized worker-log collection |
| Step 12 real paper order submission | Prohibited before separate approval |

<details><summary>🔵 Operational-identifier summary</summary>

| Item | Value |
| --- | --- |
| SSM commandId | `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3` · `c844aea5-1429-430a-9510-39fc99f17f05` · `2279c6d7-2da6-4317-9c10-7cc77374b317` · `f9d82fcc-1e26-4710-87c3-1d20483b63ef` |
| RunId | `daily-aws-paper-20260621-204017` |
| taskDefinition · taskId | `portfolio-paper-interest-crawler:7` · `78979b5cbb714d0eb94f5946e15a14ce` |
| S3 key | `tmp/krx/interest_krx_raw_validate_daily.py` |
| Scheduled Task · wrapper options | `Portfolio-KRX-Worker-Daily` · `-StartStep` · `-EndStep` |
| latest worker log | `krx_worker_daily_20260621_114154.log` |
| DB session | user · search_path · row_count |

</details>

## 2026-06-20 (final AWS automated-wrapper check + safe stop of 6/18 duplicate-execution attempt + 6/19 KRX raw-freshness recovery status)

- 🟢 **Work scope**

| Item | Value |
| --- | --- |
| (a) Final review | Daily AWS Paper Wrapper (`run-daily-aws-paper.ps1` + config + functions + `steps/step-01 ~ 17`) structure · safety criteria · EC2 startup criteria |
| (b) Safe stop of rerun attempt | RunDate `2026-06-18` · Step 1~11 range · duplicate-execution possibility recognized · Ctrl+C immediately after entering Step 3 PREPROCESSOR |
| (c) Check 6/19 KRX raw DB freshness recovery | Accumulated |
| Actual work | Directly by operator (AWS · EC2 · SSM · RDS · KRX) · Kiro documentation/procedure organization only |
| Reference | `_common/followups-overview.md` 2026-06-20 · 08 spec operation-notes 2026-06-20 |

- 🟢 **Reconfirmed wrapper safety criteria · EC2 startup criteria**

| Item | Value |
| --- | --- |
| Step 1~11 | Before broker / KIS order submission |
| Step 12 (`MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) | Can submit real KIS paper orders · blocked unless `-AllowPaperOrderExecute` specified · exclude weekends / market holidays |
| EC2 startup criteria | MC EC2 (Step 1 · 12 · 13 · 17) + Crawler Worker EC2 (Step 2 KRX GUI) · if both stopped, start before wrapper execution |
| `/tmp/inject-env.sh` volatility | Possible after EC2 stop / start · first empirical validation on this date (recreate, then Step 1 rerun passed) · lifecycle reinforcement follow-up |

- 🟢 **Result of 6/18 duplicate-execution attempt**

| Item | Value |
| --- | --- |
| Step 1 CONNECTOR_BALANCE | Initial failure (env lost) → recreate MC EC2 `/tmp/inject-env.sh` → rerun passed |
| Step 2 INTEREST_CRAWLER | non-GUI ECS exitCode 0 · KRX GUI worker Scheduled Task trigger succeeded |
| Confirmed state | 6/18 ECS RUNNING 0 · AWS Batch RUNNING · SUBMITTED · PENDING · RUNNABLE 0 · KRX Scheduled Task `Ready` · LastTaskResult 0 · Step 12 not executed |
| Stop action | Ctrl+C immediately after entering Step 3 PREPROCESSOR · prevented duplicate plan creation |
| DB check | Existing 2026-06-18 `execution_plan_id 94` complete normally (BUY 4 · FILLED 4 · connector linked 4) · after 2026-06-20 04:20 UTC new execution_plan · strategy_execution_order · connector_order_request 0 · new KIS orders 0 |
| Cleanup | Remaining Crawler Worker Chrome processes cleared by EC2 stop · MC · Crawler Worker EC2 stopped |

- 🟢 **6/19 KRX raw freshness recovery**

| Table | Value |
| --- | --- |
| `interest_program_raw` max_date | `2026-06-19` · 2026-06-18 row_count `1` · 2026-06-19 row_count `1` |
| `interest_shortsell_raw` max_date | `2026-06-19` · 2026-06-18 row_count `349` · 2026-06-19 row_count `349` |
| Conclusion | KRX raw freshness recovered |
| Follow-up | Limitation of Scheduled Task trigger / LASTEXITCODE-only success judgment for Windows EC2 worker (aligned with R-AUTO-007) · Step 2 wrapper success-criteria reinforcement separated to 2026-06-21 |

- 🟢 **Decision · risk · outputs · safety**

| Item | Result |
| --- | --- |
| New OD · R | 0 |
| Follow-up handoff | Automated EC2 lifecycle start · stop · `/tmp/inject-env.sh` recreation · limitation of trigger-only Scheduled Task success judgment → Step 2 wrapper reinforcement (2026-06-21) |
| Kiro outputs | `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` 2026-06-20 · 08 spec operation-notes 2026-06-20 accumulation · `_common/followups-overview.md` 2026-06-20 follow-up (EC2 lifecycle · `/tmp/inject-env.sh` · prohibit Step 12 before separate approval) |
| AWS · SSM · EC2 · RDS work | All performed directly by operator · AWS CLI / boto3 · AWS resource creation · modification · deletion · broker · KIS · `--execute` · aws-live work · RDS DDL · DML 0 (SELECT only) |
| Sensitive information | No new records · all `[REDACTED]` or placeholders |
| Operational identifiers | `execution_plan_id 94` · RunDate · `interest_program_raw` · `interest_shortsell_raw` row_count / max_date |

- 🟢 **Next work**

| Item | Value |
| --- | --- |
| Close for this date | Step 2 wrapper success-criteria reinforcement separated to 2026-06-21 |
| Retained follow-ups | EC2 lifecycle · SFN · View display integration · centralized worker-log collection |
## 2026-06-18 (Step 13 `connector_order_check.py` switched to sequential per-order queries by default — operational safety reinforcement)

- 🟢 **Background · nature of work**

| Item | Value |
| --- | --- |
| Background | Follow-up after completing the same-day Daily AWS Paper Wrapper 17-step operational validation |
| Nature | Not an operational failure · design change for stronger operational safety |
| Issue | KIS paper fill-query response (`output1 empty` + `output2 summary-only`) can be returned even when multiple active orders exist (aligned with R-AUTO-018) → add one more default-level block |
| Responsibility separation | wrapper ps1 orchestrates Step 13 execution · fill-query behavior is controlled inside `connector_order_check.py` (aligned with new OD-MS-025) |
| Reference | `_common/operator-decisions.md` Change Log · 03 spec operation-notes 2026-06-18 (Step 13) · `_common/followups-overview.md` second 2026-06-18 follow-up |

- 🟢 **Changes**

| Item | Value |
| --- | --- |
| Target file | `port-marketconnector/connector_order_check.py` (EC2 `/home/ec2-user/apps/port-marketconnector/src/connector_order_check.py`) |
| Default-mode change | broad fill query centered → query active-order list + sequential direct-only query per order number / ticker code |
| New options | `--broad` (legacy · not used by default) · `--active-limit` (maximum count for sequential per-order queries) |
| Explicit order query | `--code {ticker_code} --order-no {broker_order_no} --no-broad` (without broad fallback) |
| Safety criterion | Prohibit using `output2 summary-only` response as DB-update evidence when multiple active orders exist · allow summary fallback only when exactly one `connector_order_request` candidate is identified by order number / ticker code · prevent mis-mapping of `connector_order_event` · `connector_fill` (aligned with R-AUTO-018 [2026-06-18 additional reinforcement] mitigation) |

- 🟢 **Validation milestone (second 2026-06-18 session)**

| Item | Value |
| --- | --- |
| Local validation | `python -m py_compile connector_order_check.py` passed · confirmed `--help` options |
| commit | `75cb804` (`fix(connector): run order checks sequentially per active order` · remote push destination not configured → git push not performed) |
| EC2 deployment | S3 → MC EC2 (`i-0fce77927b7397b88`) deployment · bucket `portfolio-paper-migration-yukiever` · key `deploy/marketconnector/connector_order_check.py` · 39159 bytes |
| EC2 deployment (continued) | Created backup `connector_order_check.py.bak-20260618-step13-per-order`, replaced file, restored ownership `ec2-user:ec2-user`, confirmed `--broad` · `--active-limit` markers |
| EC2 validation | Activate `../.venv` + load `/tmp/inject-env.sh` → `python3 -m py_compile` passed · `--help` options confirmed |
| Single-order direct-only test | `python3 connector_order_check.py --code 004990 --order-no 0000025576 --no-broad` · SSM Status 🟢 **Success** · ResponseCode `0` · StdErr empty |
| Standalone wrapper Step 13 execution | `-StartStep 13 -EndStep 13` (RunDate `2026-06-18`)<br>Selected `Step 13 CONNECTOR_ORDER_CHECK only`<br>SSM commandId `b344d404-07a4-4bb6-9d63-34151e648bab` · 🟢 **Success** · responseCode `0`<br>Step 13 🟢 **COMPLETED** |

- 🟢 **Decision · risk changes**

| Item | Value |
| --- | --- |
| ID | New OD-MS-025 |
| Status | <span style="color:#BF8700">**잠정**</span> |
| Note | `connector_order_check.py` operating mode = sequential per-active-order query by default · broad bulk query is legacy and allowed only with explicit `--broad` · explicit order query uses `--code` · `--order-no` · `--no-broad` · prohibit summary-fallback DB update with multiple active orders · allow only when single candidate confirmed |
| Note (continued) | wrapper ps1 orchestrates only · fill-query behavior is controlled inside `connector_order_check.py` |

| Item | Value |
| --- | --- |
| ID | R-AUTO-018 detection / mitigation |
| Status | [2026-06-18 additional reinforcement] |
| Note | First session validated automatic skip inside wrapper + default `connector_order_check.py` mode changed to sequential per-order query · reduced broad-call frequency · reduced exposure to summary fallback · Status remains 🟢 **Mitigated** |

| Item | Value |
| --- | --- |
| ID | Existing Step 13 automation follow-up |
| Status | First phase resolved |
| Note | Remaining follow-ups (Step 13 summary output reinforcement · View integration · Git remote · cp949 encoding) recorded in second 2026-06-18 follow-up note |

- 🟢 **Kiro outputs**

| File | Change |
| --- | --- |
| `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` second 2026-06-18 section | New |
| `.kiro/README.md` | Added Step 13 safety note to operator-local PowerShell wrapper section + explicitly documented StartStep / EndStep parameters (prevent FromStep / ToStep mistakes) |
| `_common/operator-decisions.md` | Change Log · new OD-MS-025 · Summary updated |
| `_common/risk-register.md` | R-AUTO-018 [2026-06-18 additional reinforcement] |
| `_common/followups-overview.md` | Second 2026-06-18 follow-up (Git remote configuration · Step 13 summary · per-order View display · cp949 encoding) |
| 03 spec operation-notes 2026-06-18 (Step 13) §1~§6 | Accumulated |
| 04 spec · aws-resource-glossary · cost-simulation · ms-aws-service-decision-matrix | No changes (this change belongs to spec 03 · Step 13 only) |

- 🟢 **Safety · security**

| Item | Result |
| --- | --- |
| AWS · SSM · EC2 · S3 · KIS calls | All performed directly by operator · Kiro organized documentation/procedures only |
| AWS CLI · boto3 · AWS resource creation · modification · deletion | 0 |
| broker / KIS calls | One single-order direct-only query + one standalone wrapper Step 13 execution (both read-only/query-type) |
| New BUY · SELL · cancel · modify · `--execute` · fill · position-sync automatic retry · aws-live work | 0 |
| RDS DDL | 0 |
| RDS DML | Limited to normal Step 13 flow · new `connector.connector_order_event` · `connector.connector_fill` rows aligned with single-order direct-only result |
| Sensitive information | No new records · all `[REDACTED]` or placeholders |

- 🟢 **Next work**

| Item | Value |
| --- | --- |
| Configure Git remote push destination | Push commit `75cb804` |
| Reinforce Step 13 summary output | active_order_count · single_check_success_count · single_check_failed_count · broad_mode_used |
| View Daily Batch integration | Display Step 13 results per order |
| Windows PowerShell · AWS CLI SSM emoji stdout | Organize cp949 encoding-avoidance pattern |

<details><summary>🔵 Operational-identifier summary</summary>

| Item | Value |
| --- | --- |
| commit | `75cb804` |
| S3 bucket · key · size | `portfolio-paper-migration-yukiever` · `deploy/marketconnector/connector_order_check.py` · `39159 bytes` |
| EC2 instance · backup file | `i-0fce77927b7397b88` · `connector_order_check.py.bak-20260618-step13-per-order` |
| SSM commandId | `b344d404-07a4-4bb6-9d63-34151e648bab` |
| Ticker · broker_order_no | `004990` · `0000025576` |
| wrapper options | `--broad` · `--active-limit` · `--code` · `--order-no` · `--no-broad` |
| wrapper parameters | `StartStep` · `EndStep` |

</details>

## 2026-06-18 (Daily AWS Paper Wrapper 17-step operational validation complete)

- 🟢 **Daily AWS Paper Wrapper Step 1~17 real execution complete**

| Item | Value |
| --- | --- |
| wrapper | `.kiro/scripts/run-daily-aws-paper.ps1` |
| Meaning | First complete CLI-based Daily execution baseline before View implementation |
| Environment · RunDate · region | `aws-paper` · `2026-06-18` · `ap-northeast-2` |
| Step 12 option | Paper orders allowed only with explicit `-AllowPaperOrderExecute` |
| Result | Four real KIS paper BUY orders submitted · filled · fill sync · position sync · balance refresh connected normally end-to-end |
| aws-live work | 0 |

- 🟢 **17-step result (chronological)**

| Step | Result |
| --- | --- |
| Step 1 CONNECTOR_BALANCE | MC EC2 SSM RunCommand complete |
| Step 2 INTEREST_CRAWLER | non-GUI ECS + Windows KRX worker · interest raw freshness recovered through `2026-06-17` |
| Step 3 PREPROCESSOR | `pre_total_*_feature` freshness `2026-06-17` |
| Step 4 BACKTEST_RESEARCH | AWS Batch 🟢 **SUCCEEDED** · result freshness `2026-06-17` |
| Step 5 BACKTEST_REPORT | 🟢 **SUCCEEDED** · S3 artifacts |
| Step 6 DAILY_BUY_SIGNAL | 4 BUY READY (`004990` 롯데지주 · `023530` 롯데쇼핑 · `003490` 대한항공 · `042660` 한화오션) |
| Step 7 DAILY_POSITION_SIGNAL | Existing holdings HOLD decision normal |
| Step 8 DAILY_BUY_EXECUTION | `execution_plan_id 94` · additional-BUY allowed policy · blocked 0 · skipped 0 |
| Step 9 / Step 10 | Normal skip (no SELL targets) |
| Step 11 DAILY_AUTO_BUY | 4 rows `READY -> REQUESTED` |
| Step 12 MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE | Initial KIS paper API read timeout · requests `38~41` · orders `30~33` <span style="color:#D1242F">**FAILED**</span> · controlled REQUESTED recovery, then retry → 4 BUY 🟢 **ACCEPTED** · requests `42~45` · orders `30~33` 🟢 **SUBMITTED** |
| Step 12 MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE (continued) | broker_order_no `0000025576`, `0000025740`, `0000025744`, `0000025747` |
| Step 13 CONNECTOR_ORDER_CHECK | broad query returned `output1 empty` + `output2 summary-only` · 4 active candidates · summary fallback 🟢 **skipped** (first R-AUTO-018 mitigation validation) → per-order requery with `--code` / `--order-no` / `--no-broad` reflected 4 fills (created connector_order_event · connector_fill) |
| Step 14 | Normal skip |
| Step 15 SYNC_BUY_FILL | strategy_execution_order `30~33` 🟢 **FILLED** |
| Step 16 SYNC_BUY_POSITION | Initial additional-BUY unique-constraint collision · merge patch in `execution_sync_buy_position.py` (`merge_open_position_state()` + `additional_buys` accumulation + idempotency) · Docker rebuild + ECR push (image-digest placeholder) · ECS rerun exitCode 0 |
| Step 16 SYNC_BUY_POSITION (continued) | Reflected: 004990 65→69 shares · 003490 52→58 · new 023530 8 shares id 11 · new 042660 11 shares id 12 |
| Step 17 BALANCE_REFRESH | SSM commandId `66ec8831-74b9-469c-8410-6ccb11cb3400` · responseCode 0 · `connector.connector_position_snapshot` row_count 36 · max_created_at `2026-06-18 04:33:07.456056+00` · `legacy.holdings` row_count 41 |
| Step 17 BALANCE_REFRESH (continued) | max_created_at `2026-06-18 04:33:07.420226` (queried directly with `portfolio_admin` because view_app lacked permission) |

- 🟢 **Decision · risk changes**

| Item | Value |
| --- | --- |
| ID | New OD-MS-024 |
| Status | 🟠 **잠정** |
| Note | Additional-BUY allowed policy + if OPEN already exists use `merge_open_position_state()` instead of new INSERT · keep history in `buy_info.additional_buys` · idempotency based on execution_order_id · connector_order_request_id |

| Item | Value |
| --- | --- |
| ID | New R-BROKER-004 |
| Status | 🟢 **Mitigated** |
| Note | Duplicate-order risk from simple rerun after KIS paper API read timeout · mitigation = precheck + controlled REQUESTED recovery · first validation |

| Item | Value |
| --- | --- |
| ID | New R-DATA-012 |
| Status | 🟢 **Mitigated** |
| Note | strategy_position_state unique-constraint collision on additional BUY · mitigation = merge patch + idempotency |

| Item | Value |
| --- | --- |
| ID | R-AUTO-009 · R-AUTO-010 · R-AUTO-011 · R-AUTO-019 |
| Status | Reinforced · 🟢 **Mitigated** |
| Note | Four KIS paper BUY orders initial timeout → recovery → end-to-end passed · first use of Step 12 `-AllowPaperOrderExecute` |

| Item | Value |
| --- | --- |
| ID | R-AUTO-018 detection |
| Status | Reinforced |
| Note | First validation that summary fallback is automatically skipped in broad-query multi-active state · fallback allowed only for explicit single-order `--code` · `--order-no` · `--no-broad` query |

| Item | Value |
| --- | --- |
| ID | R-DATA-005 follow-up |
| Status | Reinforced |
| Note | view_app lacks `legacy.holdings` SELECT permission · used `portfolio_admin` as workaround · formal GRANT follow-up in spec 02 |

- 🟢 **Kiro outputs**

| File | Change |
| --- | --- |
| `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` 2026-06-18 | New |
| `.kiro/README.md` | Updated progress summary · wrapper section (real 1~17 complete · first Step 12 `-AllowPaperOrderExecute` use · bundled wrapper not created) |
| `_common/operator-decisions.md` | Change Log · new OD-MS-024 · Summary update |
| `_common/risk-register.md` | New R-BROKER-004 · R-DATA-012 + reinforced R-AUTO-009 · R-AUTO-010 · R-AUTO-011 · R-AUTO-018 · R-AUTO-019 · R-DATA-005 |
| `_common/followups-overview.md` | 2026-06-18 follow-up (Step 13 per-order automation · KIS timeout retry policy · Step 16 patch commit · view_app legacy permission · wrapper log/summary · View integration) |
| 03 spec operation-notes 2026-06-18 §1~§4 | Step 1 · Step 12 timeout recovery · Step 13 broad → per-order · Step 17 result + view_app legacy workaround |
| 04 spec operation-notes 2026-06-18 §1~§6 | Step 6~11 · 14~16 results + Step 16 merge patch + Docker build-context caution + ECR push / digest kept by operator |
| `_common/ms-aws-service-decision-matrix.md` | First empirical note with no decision-value change (MC Step 12·13·17 · Execution Step 15·16 · Daily wrapper 17-step E2E) |
| `_common/aws-resource-glossary.md` · `_common/cost-simulation.md` | Body changes 0 |

- 🟢 **Safety · security**

| Item | Result |
| --- | --- |
| AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · RDS · KIS | All performed directly by operator · Kiro organized documentation · procedures · validation only |
| AWS CLI · boto3 · AWS resource creation · modification · deletion | 0 |
| Plaintext quotation of `secretsmanager:GetSecretValue` results · CloudWatch Logs · SSM responses · KIS API response body · PowerShell stdout | 0 |
| broker / KIS calls | 4 KIS paper BUYs (Step 12 real execution) + balance · order-check queries |
| SELL · cancel · modify · additional `--execute` · `mark_position_sell_ordered()` · fill · position-sync automatic retry · aws-live work | 0 |
| Sensitive information | No new records · all `[REDACTED]` or placeholders |

- 🟢 **Next work**

| Item | Value |
| --- | --- |
| Retained follow-ups | Reinforce Step 13 per-order automation · formalize KIS timeout retry policy · formally commit Step 16 merge patch + spec 07 CI/CD · review view_app `legacy.holdings` SELECT permission · reinforce wrapper summary · improve summary before View Daily Batch integration · decide bundled-wrapper creation |
| Retained follow-ups (continued) | EventBridge regular trigger · migration to SFN · aws-live cutover (spec 10) |

<details><summary>🔵 Operational-identifier summary</summary>

| Item | Value |
| --- | --- |
| `execution_plan_id` | `94` |
| connector_order_request id | `38~41` (FAILED) + `42~45` (ACCEPTED) |
| strategy_execution_order id | `30~33` |
| broker_order_no | `0000025576` · `0000025740` · `0000025744` · `0000025747` |
| position_state_id | `7` · `8` · `11` · `12` |
| SSM commandId | `66ec8831-74b9-469c-8410-6ccb11cb3400` |
| `connector.connector_position_snapshot` row_count | `36` |
| `legacy.holdings` row_count | `41` |
| Ticker codes | `004990` · `023530` · `003490` · `042660` |
| data_date · signal_date · run_date | `2026-06-17` · `2026-06-18` |

</details>

# 2026-06-16~17 AWS work records in two-column format

## 2026-06-17 — Daily AWS PowerShell wrapper implementation

### Work objectives

| Item | Value |
| --- | --- |
| Background | Follow-up after same-day 17-step E2E completion |
| Execution location | Operator-local Windows PowerShell |
| Objective 1 | Reduce dependency on manual AWS Console work |
| Objective 2 | Provide reproducible Daily execution unit |
| Objective 3 | Establish CLI execution baseline before View implementation |
| Objective 4 | Provide a safe step-by-step tool before full automation |
| Step 12 policy | Blocked by default |
| Order permission | Only when operator explicitly passes approval option |
| wrapper-based real 1~17 rerun | 0 |
| aws-live execution | 0 |

### Outputs

| File | Role |
| --- | --- |
| `run-daily-aws-paper.ps1` | main wrapper |
| main wrapper parameters | `RunDate` · `Region` · `Environment` · `StartStep` · `EndStep` · `DryRun` · `AllowPaperOrderExecute` |
| main wrapper safety | Central Step 12 `PAPER_ORDER_GATE` |
| main wrapper output | Execution summary generation |
| `daily-aws-paper.config.ps1` | Manage region · cluster · instance ID · subnet · SG · Task Definition · Job Definition · Log Group · output path |
| `daily-aws-paper.functions.ps1` | Step registry and common execution functions |
| SSM common | Linux `AWS-RunShellScript` |
| SSM common | Windows `AWS-RunPowerShellScript` |
| ECS common | RunTask + UTF-8 No BOM JSON + `file://` overrides |
| Batch common | SubmitJob |
| Logs | Save CloudWatch · SSM stdout · stderr |
| Judgment | Success · failure · blocker |
| PowerShell | UTF-8 output correction |
| Step files | `step-01-connector-balance.ps1` ~ `step-17-balance-refresh.ps1` |
| bundled wrapper | Not created on this date |
| bundled wrapper status | Optional follow-up |

### Validation results

| Item | Value |
| --- | --- |
| Full DryRun | Step 1~17 all `FOUND` |
| ECS RunTask submissions | 0 |
| Batch SubmitJob submissions | 0 |
| SSM command submissions | 0 |
| Standalone Step 1 validation | Passed |
| Step 1 execution | MarketConnector EC2 SSM RunCommand |
| Execution script | `connector_balance.py` |
| SSM status | Success |
| ResponseCode | 0 |
| DB result | `connector_balance_snapshot` saved |
| Holdings | 0 |
| Standalone Step 12 validation | Central gate block passed |
| Step 12 default | `PaperOrder=False` |
| Step 12 internal gate | Double block |
| Without approval option | SSM command submissions 0 |
| Actual KIS orders | 0 |
| parser validation | All 20 files OK |
| parser error | 0 |
| safety grep | Passed |
| Step 10·11 `--execute` | Create/update strategy-execution internal state |
| Step 12 `--execute` | Can submit KIS paper orders |
| Unintended order commands | 0 |

### Decisions and Risks

| Item | Value |
| --- | --- |
| New Decision | `OD-MS-023` |
| Decision content | Operator-local PowerShell baseline · split-file structure · explicit Step 12 approval |
| Decision status | 🟡 잠정 |
| New Risk | `R-AUTO-019` |
| Risk | Unintended use of `-AllowPaperOrderExecute` |
| mitigation | Central gate + Step 12 internal gate |
| mitigation | Operator must explicitly specify option |
| Default | OFF |
| Risk status | Mitigated |
| Reinforced Risk | `R-AUTO-016` |
| Reinforcement | Automatically skip Step 2 when Windows KRX worker not running |
| Precheck | EC2 state · SSM Online |
| Reinforced Risk | `R-AUTO-002` |
| Reinforcement | Allow `aws-paper` only · no aws-live branch |
| Reinforced Risk | `R-DOCS-001` |
| Reinforcement | 0 plaintext secrets in summary · overrides JSON · stdout · stderr |

### Documentation updates

| File | Change |
| --- | --- |
| `.kiro/WORKLOG.md` | Added this session |
| `.kiro/CHANGELOG.md` | Third 2026-06-17 section |
| `.kiro/README.md` | PowerShell wrapper execution overview |
| `operator-decisions.md` | Added `OD-MS-023` |
| `followups-overview.md` | Third 2026-06-17 follow-up note |
| `risk-register.md` | Added `R-AUTO-019` |
| `risk-register.md` | Reinforced R-AUTO-002 · R-AUTO-016 · R-DOCS-001 |
| `03 operation-notes.md` | 2026-06-17 §1~§6 |
| Other common documents | No change |
| 04·06·08·09 operation notes | No change |

### Execution · security boundary

| Item | Value |
| --- | --- |
| AWS work actor | Operator |
| Kiro role | Organize documentation · procedures · validation items |
| AWS CLI · boto3 | 0 |
| AWS resource creation·modification·deletion | 0 |
| Direct broker·KIS calls | 0 |
| New BUY·SELL·cancel·modify | 0 |
| fill·position automatic retries | 0 |
| aws-live | 0 |
| Plaintext sensitive information | 0 |
| placeholder | `[REDACTED]` family |
| wrapper execution location | Operator-local Windows PC |
| Internal EC2·ECS·Batch execution | Not in scope |

### Next work

| Follow-up | Value |
| --- | --- |
| bundled wrapper | Decide whether to create |
| safe subset | Real validation of Step 1~7 |
| execution-side subset | Separate validation of Step 8~11 |
| Step 12 | Explicit approval after operator check |
| Step 13~17 | Execute after order/fill-result confirmation |
| KRX worker stopped | Validate Step 2 skip |
| orchestration | Review View or Step Functions |
| scheduled execution | EventBridge Scheduler |
| live | Spec 10 cutover follow-up |

## 2026-06-17 — Daily AWS 17-step E2E complete

### Overall result

| Item | Value |
| --- | --- |
| Overall status | 🟢 Complete |
| Environment | `aws-paper` |
| Full scope | Step 1~17 |
| Actual broker calls | 4 KIS paper BUYs |
| SELL | 0 |
| Cancel·modify | 0 |
| aws-live | 0 |

### Step 1~17 results

| Step | Result |
| --- | --- |
| 1 CONNECTOR_BALANCE | Complete · snapshot as-of date 2026-06-17 |
| 2 INTEREST_CRAWLER | Complete · non-GUI ECS + KRX Windows hybrid |
| 3 PREPROCESSOR | Complete · feature date 2026-06-16 |
| 4 BACKTEST_RESEARCH | AWS Batch SUCCEEDED · Sharpe 2.68 |
| 5 BACKTEST_REPORT | AWS Batch SUCCEEDED · 4 S3 reports |
| 6 DAILY_BUY_SIGNAL | 4 READY |
| 7 DAILY_POSITION_SIGNAL | positions 0 · normal skip |
| 8 DAILY_BUY_EXECUTION | Plan 92 · 4 BUY READY |
| 9 DAILY_SELL_EXECUTION | Normal skip |
| 10 DAILY_AUTO_SELL | Normal skip |
| 11 DAILY_AUTO_BUY | 4 READY → REQUESTED |
| 12 ORDER_EXECUTE | 4 KIS paper BUYs submitted |
| 13 ORDER_CHECK | FILLED synchronized through single-order queries |
| 14 SYNC_SELL_FILL | Normal skip |
| 15 SYNC_BUY_FILL | 4 FILLED |
| 16 SYNC_BUY_POSITION | 4 Positions OPEN |
| 17 BALANCE_REFRESH | Latest position snapshot with 4 securities |

### Key data

| Item | Value |
| --- | --- |
| BUY ticker codes | 282330 · 004990 · 003490 · 088350 |
| Total quantity | 378 |
| Target amount | 6,908,189.40 |
| Execution Order | ID 26~29 |
| Connector Order Request | ID 34~37 |
| Position State | ID 6~9 |
| Final quantities | 17 · 65 · 52 · 244 |

### Decisions and Risks

| Item | Value |
| --- | --- |
| New Decision | 0 |
| New Risks | 2 |
| Reinforced Decision | OD-MS-016 |
| Reinforced Decision | OD-MS-021 |
| Reinforced Decision | OD-DB-008 |
| New Risk | R-AUTO-018 |
| Content | Aggregate-summary mis-mapping risk |
| mitigation | Allow fallback only when exactly one active candidate exists |
| Status | Mitigated |
| New Risk | R-DATA-011 |
| Content | Missing legacy-schema permission · search_path |
| mitigation | Correct GRANT · search_path |
| Status | Mitigated |
| Additional reinforcement | R-DOCS-001 · R-DATA-005 |
| Order-family reinforcement | R-AUTO-009 · R-AUTO-010 · R-AUTO-011 |
| Research Risk | R-AUTO-015 remains Mitigated |

### Execution · security boundary

| Item | Value |
| --- | --- |
| Actual operational work | Performed directly by operator |
| Kiro execution | 0 |
| Plaintext secrets | 0 |
| AWS CLI · boto3 | 0 |
| AWS resource changes | Kiro 0 |
| broker calls | Limited to 4 paper BUYs |
| SELL·cancel·modify | 0 |
| aws-live | 0 |

### Next work

| Follow-up | Value |
| --- | --- |
| source_daily_signal_id | Review null correction |
| summary fallback | Add test cases |
| legacy permission | Formal documentation |
| balance-freshness SQL | Clarify semantic distinction |
| Windows cp949 | Avoid output failures |
| orchestration | View or Step Functions |
| scheduled execution | EventBridge Scheduler |
| package | Formalize Strategy Common |
| live | Spec 10 cutover |

## 2026-06-17 — MarketConnector read-only dry-run revalidation

### Validation results

| Item | Value |
| --- | --- |
| CONNECTOR_BALANCE | Revalidation succeeded |
| Initial failure cause | Exported entire JSON SecretString |
| Correction | Extract internal key values |
| env compatibility | APP_* + KIS_* aliases |
| Latest snapshot date | 2026-06-17 |
| source API | `inquire-balance` |
| Holdings | 0 |
| CONNECTOR_ORDER_CHECK | Revalidation succeeded |
| API | `inquire-daily-ccld` |
| response status | 200 |
| response code | 0 |
| is_success | true |
| New orders/fills | 0 |
| PowerShell variable loss | Minor operator-side issue |
| AWS impact | None |

### Decision · Risk

| Item | Value |
| --- | --- |
| New Decision | 0 |
| New Risk | 0 |
| Reinforced Decision | OD-SEC-006 |
| Reinforced Decision | OD-MS-001 |
| Reinforced Decision | OD-MS-009 |
| Safety alignment | OD-SAFE-001~004 · OD-MS-021 |
| Reinforced Risk | R-DOCS-001 |
| secret-value output | 0 |

### Next work

| Follow-up | Value |
| --- | --- |
| safe subset | Resume Step 2~7 |
| execution range | Separate judgment for Step 8~17 |
| env mapping | Apply to startup script |
| inject env | Review promotion to operational script |
| APP_*·KIS_* | Decide unification |
| View mapping | Spec 05 |
| Step Functions | Spec 04 |
| live | Spec 10 |


## 2026-06-16 — Crawler missing-data issue resolved + KRX EC2 automation

### Cause and action

| Item | Value |
| --- | --- |
| Cause | rev6 was a Selenium smoke command |
| Cause | Original daily script included KRX GUI |
| Cause | Non-GUI orchestration was missing |
| New file | `interest_crawler_daily_nongui.py` |
| Image | Docker rebuild + ECR push |
| Task Definition | `portfolio-paper-interest-crawler:7` |
| RunTask | ExitCode 0 |
| Execution time | About 9m 51s |
| non-GUI steps | All SUCCESS |

### Raw-data freshness

| Item | Value |
| --- | --- |
| price | 2026-06-15 |
| investorflow | 2026-06-15 |
| marketbreadth | 2026-06-15 |
| commodity | 2026-06-15 |
| foreignindex | 2026-06-15 |
| macro | 2026-06-15 |
| news | 2026-06-16 |
| agency | 2026-06-16 |

### Windows KRX worker

| Item | Value |
| --- | --- |
| bootstrap | Autologon |
| session | Administrator Active |
| trigger | SSM RunCommand → Scheduled Task |
| collection | KRX login · program · shortsell |
| load date | 2026-06-15 |
| stop request | Complete |

### Decisions and Risks

| Item | Value |
| --- | --- |
| Reinforced Decision | OD-MS-011 · OD-MS-015 |
| New Decision | OD-MS-022 |
| Content | Autologon + interactive session + Scheduled Task + SSM |
| Status | 🟡 잠정 |
| New Risk | R-SEC-009 |
| New Risk | R-AUTO-016 |
| New Risk | R-AUTO-017 |
| Reinforced Risk | R-AUTO-008 |
| Reinforced Risk | R-DATA-009 · R-DATA-010 |
| BUY · SELL execution | 0 |
| aws-live | 0 |

### Next work

| Follow-up | Value |
| --- | --- |
| Preprocessor | Rerun using recovered raw data |
| Feature validation | max date · updated_at |
| E2E | Safe subset after Research |
| foreignindex NULL | Follow-up review |
| Scheduler | Periodic trigger |
| orchestration | Step Functions hybrid |
| Log collection | CloudWatch Agent · SSM |
| Worker shutdown | stop procedure |
| Chrome | Clean up remaining processes |
| View | Follow-up implementation |

## 2026-06-16 — Backend AWS E2E dry-run safe subset resumed

### Execution results

| Item | Value |
| --- | --- |
| BACKTEST_RESEARCH | AWS Batch SUCCEEDED |
| run_id | `439d78e7-41fd-4bb7-b455-18564ddff758` |
| end date | 2026-06-15 |
| total return | 4.66534417 |
| MDD | -0.08941942 |
| Sharpe | 2.68071466 |
| trade count | 310 |
| BACKTEST_REPORT | rev3 final success |
| report method | `python -m port_strategy_research.aws_batch_backtest_report_wrapper` |
| Job Definition | `portfolio-paper-strategy-report:3` |
| S3 objects | 4 |
| DAILY_BUY_SIGNAL | 4 READY rows |
| signal date | 2026-06-16 |
| DAILY_POSITION_SIGNAL | positions 0 · normal skip |
| New position decisions | 0 |

### Safe-subset status

| Step | Result |
| --- | --- |
| 1 CONNECTOR_BALANCE | Complete |
| 2 INTEREST_CRAWLER | Hybrid complete |
| 3 PREPROCESSOR | Ready for rerun |
| 4 BACKTEST_RESEARCH | Complete |
| 5 BACKTEST_REPORT | Complete |
| 6 DAILY_BUY_SIGNAL | Complete |
| 7 DAILY_POSITION_SIGNAL | Normal skip |
| 8~17 | Not executed |

### BACKTEST_REPORT correction history

| Item | Value |
| --- | --- |
| revision 1 | local-only |
| revision 2 | Failed because wrapper path was missing |
| revision 3 | Succeeded with module-call method |
| Final image | `paper-20260615-report-s3` |
| output dir | `/tmp/portfolio-reports` |
| S3 prefix | `strategy-research/reports` |
| IAM | PutObject Resource restricted to prefix |
| public read | 0 |
| wildcard | 0 |

### Decisions and Risks

| Item | Value |
| --- | --- |
| New Decision | 0 |
| New Risk | 0 |
| Reinforced Decision | OD-MS-008 |
| Reinforced Decision | OD-MS-013 |
| Reinforced Decision | OD-MS-019 |
| Reinforced Decision | OD-MS-021 |
| Reinforced Risk | R-AUTO-015 |
| Risk status | Mitigated retained |
| broker · KIS orders | 0 |
| aws-live | 0 |

### Next work

| Follow-up | Value |
| --- | --- |
| View mapping | 05 spec |
| Step Functions | 04 spec |
| Scheduler | EventBridge |
| Order · fill range | Separate operational approval |
| execution dry-run | Decide feasibility |
| S3 lifecycle | Decision required |
| KMS | Decide encryption policy |
| heavy backtest | Formalize operating procedure |
| live | 10 spec cutover |

## 2026-06-15 (Backend AWS E2E dry-run first pass + Interest Crawler status reassessment)

- 🟢 **Work summary** (second session on the same date · follow-up after Strategy Research AWS Batch skeleton + full / report + S3 upload)

| Item | Value |
| --- | --- |
| (a) Pre-check | AWS account · region · EC2 · ECS · AWS Batch |
| (b) First `CONNECTOR_BALANCE` execution | Based on MC EC2 |
| (c) KRX worker rerun · KRX raw latest-date check | Windows EC2 worker |
| (d) SQL check for latest dates of non-GUI raw data | Complete |
| (e) One-off preprocessor ECS RunTask | + confirmed DB `updated_at` refresh |
| Meaning | First backend-AWS-side dry-run review in the 17-step order before entering View · stale-raw-data issue identified early |
| Reference | 08 spec operation-notes 2026-06-15 |

- 🟢 **Pre-check (complete)**

| Item | Value |
| --- | --- |
| region · account | `ap-northeast-2` · confirmed |
| Local AWS CLI | Basic execution available |
| MarketConnector EC2 · Windows crawler worker | running |
| SSM managed instance | Online |
| `portfolio-paper-cluster` | ACTIVE |
| Task Definitions confirmed | preprocessor · decision buy-signal · decision position-signal |
| Strategy Research | CE · JQ · JD active revision · latest including BACKTEST_REPORT S3 upload = `portfolio-paper-strategy-research:3` · ENABLED · VALID · Healthy |
| Plaintext account-id · actual ARN records | 0 |

- 🟢 **`CONNECTOR_BALANCE` (Backend E2E dry-run step 1) complete**

| Item | Value |
| --- | --- |
| Execution | MarketConnector EC2 · `/home/ec2-user/apps/port-marketconnector` · venv python · SSM RunCommand |
| First failure cause | KIS Secrets were JSON · mistake of exporting the raw SecretString as-is |
| Correction | Secret JSON key → environment-variable mapping (`APP_KEY` → `KIS_APP_KEY` · `APP_SECRET` → `KIS_APP_SECRET` · `PAPER_ACNT` → `KIS_PAPER_ACNT` · `ACNT_PRDT_CD` → `KIS_ACNT_PRDT_CD`) |
| Result | Existing `access_token.txt` backed up · new token issued successfully · KIS balance API status 200 · paper-account balance query succeeded · `connector.connector_balance_snapshot` saved · 0 holdings in `connector.connector_position_snapshot` normal · 0 legacy holdings |
| Plaintext secret · KIS · account · token records | 0 |

- 🟡 **`INTEREST_CRAWLER` (Backend E2E dry-run step 2) partially complete / promoted to follow-up**

| Item | Value |
| --- | --- |
| KRX GUI worker | First operationally usable state complete · `KRX already logged in` · `KRX Login Ready` · `[Collected Date] None` idempotent normal completion |
| Latest KRX raw date | `interest_program_raw` 2026-06-12 · `interest_shortsell_raw` 2026-06-12 (normal through prior trading day) |
| Latest dates for 7 non-GUI raw sets | `interest_agency_raw` 2026-06-11 · `interest_news_raw` 2026-06-11 · `interest_commodity_raw` 2026-06-08 · `interest_foreignindex_raw` 2026-06-08 · `interest_investorflow_raw` 2026-06-08 |
| Latest dates for 7 non-GUI raw sets (continued) | `interest_marketbreadth_raw` 2026-06-08 · `interest_price_raw` 2026-06-08 |
| `interest_ticker_value_raw` | 2026-03-09 · excluded as a core dry-run blocker · separate follow-up |
| ECS · Fargate Selenium Chrome smoke | revision 6 passed on 2026-06-13 · production path / TD / command separation for actual daily raw collection incomplete |
| Conclusion (wording correction) | "Interest Crawler complete: complete" → "Interest Crawler hybrid first-stage implementation: partially complete" · "KRX GUI worker first-stage operationally usable" · "ECS · Fargate crawler smoke validation complete" · "production path for non-GUI daily raw collection and complete raw-data freshness validation remain follow-ups" |

- 🟡 **`PREPROCESSOR` (Backend E2E dry-run step 3) execution complete (data-freshness constraint)**

| Item | Value |
| --- | --- |
| cluster · TD | `portfolio-paper-cluster` · `portfolio-paper-interest-preprocessor:1` |
| Runtime | FARGATE · awsvpc · public-a + public-b · `assignPublicIp=ENABLED` · SG `sgroup-preprocessor-tasks` |
| Final state | lastStatus `STOPPED` · desiredStatus `STOPPED` · stopCode `EssentialContainerExited` · container `interest-preprocessor` · exitCode 0 · about 3m 43s |
| CloudWatch Logs | Confirmed `/portfolio/paper/preprocessor` log stream creation · latest stream `storedBytes=0` · body validation limited · success judged by exitCode 0 |
| DB `updated_at` | Updated to 2026-06-15 11:03:55+00 (KST 20:03:55) |
| New 2026-06-15 feature date | 0 rows · cause = insufficient raw-data freshness, not preprocessor failure (aligned with R-DATA-010) |

- 🟢 **First empirical validation of Secret / IAM permission separation**

| Item | Value |
| --- | --- |
| MarketConnector EC2 role | `portfolio-paper-marketconnector-ec2-role` |
| Attempt | `/portfolio/paper/rds/preprocessor-app` `GetSecretValue` |
| Result | `AccessDeniedException` · not a failure · expected behavior aligned with OD-SEC-006 · OD-DB-008 |
| Added preprocessor secret-read permission | 0 |
| preprocessor DB confirmation path | preprocessor ECS Task or operator-local SSM Port Forwarding (OD-NET-010 · OD-NET-011) only |
| IAM changes | 0 |
| Operator-local PC tip | If PowerShell prompt `>>` is included in command text, `StreamAlreadyRedirected` error occurs |

- 🔵 **17-step progress status**

| Step | Result |
| --- | --- |
| 1 `CONNECTOR_BALANCE` | 🟢 **Complete** |
| 2 `INTEREST_CRAWLER` | 🟠 **Partially complete** |
| 3 `PREPROCESSOR` | Execution complete · data-freshness constraint |
| 4~7 (`BACKTEST_RESEARCH` · `BACKTEST_REPORT` · `DAILY_BUY_SIGNAL` · `DAILY_POSITION_SIGNAL`) | Not executed |
| 8~17 | Not executed or planned dry-run skip |
| Actual BUY · SELL · `--execute` order submission | 0 |
| Automatic fill · position-sync retries | 0 |
| aws-live work | 0 |
| Order preserved | Research before Decision · Connector Balance step 1 · Balance Refresh step 17 |

- 🟡 **Decision locks (second session on 2026-06-15)**

| ID | Content |
| --- | --- |
| OD-MS-020 | Interest Crawler status reassessment · standardized wording to hybrid first-stage implementation partially complete · 🟠 **잠정** |
| OD-MS-021 | Backend AWS E2E dry-run 17-step order + safety criteria · 🟠 **잠정** |
| OD-SEC-006 | Empirical note reinforced without body change · lack of preprocessor secret-read permission on MarketConnector EC2 role behaved correctly · Status remains tentative |
| OD-DB-008 | No body change |
| New R-DATA-009 | Risk of interpreting smoke validation as completion of daily data freshness · Status `Open` |
| New R-DATA-010 | Risk that downstream Research / Decision results are based on stale data because raw freshness is insufficient · Status `Open` |

- 🟢 **Kiro work outputs**

| File | Change |
| --- | --- |
| `08/operation-notes.md` | Accumulated 2026-06-15 §1~§8 |
| `08/tasks.md` | New §14 (70~81) + Task Dependency Graph reinforcement + 2026-06-15 carry-over summary |
| `08/design.md` | §14 reinforcement (wording correction · current-date status · non-GUI path not reached · impact of insufficient raw freshness) |
| `_common/operator-decisions.md` | Added OD-MS-020 · OD-MS-021 + OD-SEC-006 empirical note + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | Second 2026-06-15 follow-up note |
| `_common/risk-register.md` | Added R-DATA-009 · R-DATA-010 |
| `.kiro/README.md` | Added progress-summary section |
| `.kiro/CHANGELOG.md` | Second 2026-06-15 section |
| `.kiro/WORKLOG.md` | This file |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · RDS · GRANT work | All performed directly by operator · Kiro only organized documentation · procedures · validation |
| Plaintext `secretsmanager:GetSecretValue` result values | 0 |
| Full plaintext quotation of CloudWatch Logs · wrapper logs · SSM responses · docker build logs | 0 |
| broker / KIS calls | Read-only `CONNECTOR_BALANCE` only (status 200 · paper-account balance) |
| New orders · `--execute` | 0 |
| RDS DDL | 0 |
| RDS DML | Limited to normal inserts by `connector_balance_snapshot` · `connector_position_snapshot` and normal preprocessor pipeline flow |
| Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging | 0 |
| Newly recorded sensitive information (secret · KIS · account · token · RDS · account-id · IAM · image digest · instance-id · task ARN) | 0 · all `[REDACTED]` or placeholders |

  <details><summary>🔵 Operational identifier summary</summary>

  - MarketConnector EC2 role name
  - preprocessor TD family · revision
  - cluster `portfolio-paper-cluster` · SG `sgroup-preprocessor-tasks`
  - Log Group `/portfolio/paper/preprocessor`
  - Secret `/portfolio/paper/rds/preprocessor-app` · `/portfolio/paper/kis/marketconnector`
  - preprocessor `updated_at` 2026-06-15 11:03:55+00
  - KRX raw latest date 2026-06-12
  - inventory of latest dates for 7 non-GUI raw sets

  </details>

- Next work (tomorrow) · production execution of non-GUI Interest Crawler (separate non-GUI crawler command for ECS Fargate · organize path that excludes KRX GUI stage from `interest_crawler_daily.py`) · restore raw freshness (`interest_price_raw` · `interest_investorflow_raw` · `interest_marketbreadth_raw`
  `interest_commodity_raw` · `interest_foreignindex_raw` · `interest_news_raw`
  - `interest_agency_raw`) · rerun preprocessor + confirm whether a new feature date is created · resume Backend E2E dry-run (`BACKTEST_RESEARCH` → `BACKTEST_REPORT` → `DAILY_BUY_SIGNAL` → `DAILY_POSITION_SIGNAL` order · Research before Decision · order-submission
    execution family only skipped or dry-run according to safety criteria) · all separated as follow-up. · Record this date's dry-run not as a failure, but as successful validation that identified stale raw data early.

## 2026-06-15 (Strategy Research AWS Batch execution skeleton + full / report validation + S3 upload reinforcement)

- 🟢 **Work summary** (follow-up to 2026-06-13 Strategy Research Batch image first-stage preparation)

| Item | Value |
| --- | --- |
| (a) New AWS Batch execution skeleton | Compute Environment · Job Queue · Job Definition revision 1 · CloudWatch Log Group · Secrets Manager `/portfolio/paper/rds/research-app` · Execution Role + Job Role |
| (b) First validation | py_compile smoke + DB smoke SubmitJob |
| (c) full execution · report validation | BACKTEST_RESEARCH full + BACKTEST_REPORT 4 reports generated |
| (d) S3 upload reinforcement | Registered Job Definition revision 3 + confirmed 4 S3 objects exist |
| Reference | 3 sections in 09 spec operation-notes on 2026-06-15 |
| Strategy Research final compute first choice | AWS Batch retained (no body change to OD-MS-008) |

- 🟢 **AWS Batch skeleton resources**

| Item | Value |
| --- | --- |
| Compute Environment | `portfolio-paper-strategy-research-ce` · MANAGED · FARGATE · maxvCpus 4 · state ENABLED · status VALID |
| Job Queue | `portfolio-paper-strategy-research-queue` · priority 10 · state ENABLED · status VALID |
| Job Definition rev1 | `portfolio-paper-strategy-research:1` · image `paper-latest` · vCPU 1 · memory 2048 · timeout 600s · FARGATE · assignPublicIp ENABLED · default command = safe `py_compile` smoke |
| CloudWatch Log Group | `/portfolio/paper/strategy-research` · retention 14 days |
| Secrets Manager | `/portfolio/paper/rds/research-app` · JSON multi-key `host`/`port`/`dbname`/`username`/`password` · 0 secret-value exposure · key-presence validation complete |
| Execution Role | `portfolio-paper-research-batch-execution-role` · `AmazonECSTaskExecutionRolePolicy` + research-app secret-read inline · ARN-scoped · wildcard 0 |
| Job Role | `portfolio-paper-research-job-role` · minimum permissions for initial smoke stage · S3 PutObject permission added at §11 stage |

- 🟢 **Two smoke SubmitJobs both SUCCEEDED / exitCode 0**

| Item | Value |
| --- | --- |
| py_compile smoke | `smoke-strategy-research-import-20260615` · jobId `81ec3581-0204-43ea-8238-a2a6d22f3f28` |
| DB smoke | `smoke-strategy-research-db-20260615` · jobId `5399aa10-0fdd-466b-8079-236d3b7e7e37` · `db smoke ok` · `research_app` · `portfolio` · schema `research` |
| Image pull · secret injection · log-delivery errors | 0 |
| secret-value exposure | 0 |
| `smoke` job-name prefix policy | First application (aligned with R-AUTO-015 mitigation) |

- 🟢 **First Strategy Common consistency check**

| Item | Value |
| --- | --- |
| Separate compute | None (aligned with OD-MS-014) |
| vendoring | Retained in Decision · Execution · Research images |
| py_compile targets passed | 15 major `port_strategy_common` modules (`common_*.py` · `config.py` · `utils.py` · `__init__.py`) + 3 Research adapters · detailed list in [09 spec operation-notes 2026-06-15](specs/09-strategy-research-batch/operation-notes.md) |
| Common-import smoke per MS | Passed |
| Wording | "Strategy Common first consistency check complete / formal package management remains follow-up" |
| Follow-up separation | wheel · sdist · CodeArtifact · 07 CI/CD package · version management · formal Research-adapter move into common (aligned with R-DATA-008 mitigation) |

- 🟢 **BACKTEST_RESEARCH full execution validation**

| Item | Value |
| --- | --- |
| Status | 🟢 **SUCCEEDED** · exitCode 0 |
| run_id | `a39b0b0c-cfe9-474e-8a4a-4ddb33f09567` |
| total_return · mdd · sharpe · trade_count | `4.55930879` · `-0.08941942` · `2.65561307` · `308` |
| extended analysis | Included internally |
| RDS DDL · broker · KIS calls · Batch automatic retry | 0 (aligned with OD-SAFE-004) |

- 🟡 **BACKTEST_REPORT main-phase validation · new OD-MS-019 decision lock**

| Item | Value |
| --- | --- |
| Result | Successfully generated 4 reports from latest `run_id` · applied `REPORT_OUTPUT_DIR`=`/tmp/portfolio-reports` · 🟢 **SUCCEEDED** · exitCode 0 |
| New OD-MS-019 | Research AWS Batch porting targets = 2: BACKTEST_RESEARCH + BACKTEST_REPORT · `run_extended_analysis.py` already runs inside BACKTEST_RESEARCH · excluded as separate AWS Batch porting target + classified as manual auxiliary tool |
| New OD-MS-019 (continued) | Preserve report artifacts under S3 prefix `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/` · 🟠 **잠정** |
| 4 `block_watch_*` · `block_exception_buy_*` items | Excluded from AWS Batch porting targets + heavy-classification follow-up |

- 🟢 **BACKTEST_REPORT S3 upload reinforcement**

| Item | Value |
| --- | --- |
| bucket | Reuse `portfolio-paper-migration-yukiever` |
| Job Role permission addition | `s3:PutObject` · Resource `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` · public read 0 · wildcard 0 |
| requirements.txt | Added `boto3` · Docker internal import smoke succeeded |
| report-wrapper options | Skip if `REPORT_S3_BUCKET` unset · default `REPORT_S3_PREFIX` = `strategy-research/reports` · separate subpath by `AWS_BATCH_JOB_ID` · retain default `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` |
| Docker rebuild + ECR push | `paper-20260615-report-s3` · image digest sha256 placeholder · size about 106MB · pushedAt 2026-06-15T17:00:10+09:00 |
| Job Definition rev3 | image `paper-20260615-report-s3` · TaskRole `portfolio-paper-research-job-role` |

- 🟢 **S3 upload SubmitJob result**

| Item | Value |
| --- | --- |
| jobName · jobId | `strategy-research-backtest-report-s3-20260615` · `112f5fe4-02f3-4614-a88c-60a9842e1447` |
| Status | 🟢 **SUCCEEDED** · exitCode 0 |
| logStreamName | `strategy-research/default/b18e548d46764cd791028088e1d32a6d` |
| 4 S3 objects | `strategy-research/reports/20260615/112f5fe4-.../01_요약_리포트_20260615_v1.txt` · `02_일자별_매매_리포트_20260615_v1.txt` · `03_거래_상세_리포트_20260615_v1.txt` · `04_추천_리포트_20260615_v1.txt` |
| Remained private · public read granted | yes · 0 |

- 🟡 **Decision lock (2026-06-15) · risk updates**

| ID | Value |
| --- | --- |
| OD-MS-019 | New · 🟠 **잠정** |
| OD-MS-008 · OD-MS-018 | Empirical notes reinforced without body changes · Status retained |
| R-AUTO-015 | Mitigation reinforced · Status promoted `Open` → 🟢 **Mitigated** (Mitigated, rather than fully Closed, is appropriate until SFN orchestration) |
| R-DATA-008 | Detection reinforced (Research py_compile / import smoke / sample comparison required whenever common changes) |
| New R-COST-003 | Research S3 report accumulation cost · lifecycle not configured · mitigation = OD-MS-019 prefix scoping + Job Role Resource scoping + public read 0 + S3 lifecycle follow-up · Status 🟠 **Open** |

- 🟢 **Kiro work outputs**

| File | Change |
| --- | --- |
| `09/operation-notes.md` | Accumulated 3 sections for 2026-06-15 |
| `_common/operator-decisions.md` | Added OD-MS-019 + empirical notes for OD-MS-008 · OD-MS-018 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | 2026-06-15 09 spec follow-up note |
| `_common/risk-register.md` | Reinforced R-AUTO-015 + Status `Mitigated` · R-DATA-008 detection · added R-COST-003 |
| `_common/ms-aws-service-decision-matrix.md` | Empirical note in Strategy Research row of 4.8 · chapter 5 · 6.1 reinforcement matrix · 0 recommendation changes |
| `.kiro/CHANGELOG.md` | 2026-06-15 section |
| `.kiro/WORKLOG.md` | This file |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| AWS · Docker · ECR · IAM · Secrets Manager · CloudWatch · Batch · S3 · RDS work | All performed directly by operator · Kiro only organized documentation · procedures · validation |
| Plaintext `secretsmanager:GetSecretValue` result values | 0 |
| Plaintext secret values in CloudWatch Logs body | 0 |
| RDS DDL / DML | 0 |
| broker · KIS · order · fill · Daily Batch entrypoint calls | 0 |
| live automatic batch / report generation | <span style="color:#D1242F">**Prohibited**</span> until follow-up approval (OD-SAFE-002 · OD-SAFE-003) |
| Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging | 0 |
| Quoted body of operator-authored `port_strategy_research` requirements.txt · report-wrapper changes | 0 (facts recorded in 09 spec operation-notes) |
| Newly recorded sensitive information (secret · RDS · KIS · account · token · account-id · secret ARN · IAM Role ARN · image digest full sha256 · IAM access key) | 0 · all `[REDACTED]` or placeholders |

  <details><summary>🔵 Operational identifier summary</summary>

  - image tag `paper-latest` · `paper-20260615-report-s3` · size about 106MB · pushedAt 2026-06-15T17:00:10+09:00
  - Compute Environment · Job Queue · Job Definition family · revision
  - Log Group `/portfolio/paper/strategy-research`
  - Secret · Role names
  - jobName · jobId · logStreamName
  - S3 bucket `portfolio-paper-migration-yukiever` · prefix `strategy-research/reports/20260615/112f5fe4-.../`
  - 4 filenames
  - metric values `run_id a39b0b0c-...` · `total_return` · `mdd` · `sharpe` · `trade_count`

  </details>

- Next work — map View Daily Batch `BACKTEST_RESEARCH` / `BACKTEST_REPORT` steps from direct ProcessBuilder execution → AWS Batch SubmitJob calls (05 / 04 spec follow-up) / Step Functions state machine (enforce BACKTEST_RESEARCH → BACKTEST_REPORT order + EventBridge Scheduler periodic trigger
  aligned with OD-SAFE-004 · 04 spec follow-up) / operating procedure for manual auxiliary tools `block_watch_*` `block_exception_buy_*` (09 follow-up) / move 3 Research internal adapters → formal `port_strategy_common` adapters (follow-up to R-DATA-008 OD-MS-005 OD-MS-014 OD-MS-018) / formal `port_strategy_common` package · version management (07
  Strategy Common) / CI/CD OIDC build-push automation (07 spec) / aws-live cutover (10 spec) / S3 lifecycle policy + KMS encryption (R-COST-003 · 06 follow-up) — all separated as follow-up.
## 2026-06-13 (Strategy Research Batch image first-stage preparation)

- 🟢 **Work summary** (follow-up after same-date sessions 1·2·3·4·5)

| Item | Value |
| --- | --- |
| Nature | Performed directly by operator · first-stage Docker / ECR preparation for `port_strategy_research` AWS Batch |
| Reference | 09 spec operation-notes 2026-06-13 Strategy Research Batch image first-stage preparation |
| Strategy Research final compute first choice | AWS Batch retained (no body change to OD-MS-008) |
| Scope on this date | Prepare container image for AWS Batch · first ECR-push validation |
| Separated follow-up | AWS Batch Compute Environment · Job Queue · Job Definition · SubmitJob |

- 🟢 **AWS execution-structure confirmation**

| Item | Value |
| --- | --- |
| 7 As-Is entrypoints | `backtest_research_run` · `backtest_report_run` · `run_extended_analysis` · `block_watch_analysis_run` · `block_watch_backtest_run` · `block_exception_buy_backtest_run` · `block_exception_buy_engine_run` |
| heavy vs light split | heavy = full backtest · long-running research · report · extended · block family · light = py_compile · import smoke · Docker MS-boundary checks |
| External dependencies | `psycopg2-binary` · `pandas` · `numpy` |
| Internal dependencies | `port_strategy_research` + `port_strategy_common` |
| Environment variables · search_path | `research_app` · `INTEREST_DB_PASSWORD`, etc. · search_path `research, preprocessor, interest, reference, legacy, public` |

- 🟢 **Removed direct Research → Decision runtime dependency**

| Item | Value |
| --- | --- |
| 3 new adapters | `research_backtest_market_adapter.py` · `research_backtest_filter_adapter.py` · `research_backtest_sizing_adapter.py` |
| Import changes in 3 files | `backtest_engine.py` · `backtest_buy_logic.py` · `block_exception_buy_engine_run.py` |
| Before → after | `from port_strategy_decision.backtest_market import evaluate_market`, etc. → `from port_strategy_research.research_backtest_market_adapter import evaluate_market` |
| Method | Python patch script · read with `encoding="utf-8-sig"` · save with `encoding="utf-8"` · Korean preview normal |
| Result | py_compile succeeded · 0 remaining `from port_strategy_decision` / `import port_strategy_decision` |

- 🟢 **Docker / ECR for Batch**

| Item | Value |
| --- | --- |
| Dockerfile · requirements.txt | Newly authored directly by operator |
| Docker build context | `C:\Workspaces` |
| Image included · excluded | included = `port_strategy_research` + `port_strategy_common` · excluded = `port_strategy_decision` |
| Default CMD | Safe `py_compile` family |
| Local build | `portfolio-strategy-research:paper-20260613` |
| Container smoke | `py_compile` + import smoke passed (`port_strategy_research.db_config` · `backtest_engine` · `backtest_buy_logic` · `block_exception_buy_engine_run`) |
| MS-boundary confirmation | `/app/port_strategy_decision` absent · first validation aligned with OD-MS-018 |
| ECR repository | New `portfolio-strategy-research` · pushed `paper-20260613` · `paper-latest` · image size about 90MB |

- 🟡 **Decision locks (sixth session on 2026-06-13)**

| ID | Content |
| --- | --- |
| OD-MS-018 | Research Batch image dependency boundary = move to Research internal adapters · image includes `port_strategy_research` + `port_strategy_common` · excludes `port_strategy_decision` · retain long-term candidate to move adapters → formal `port_strategy_common` package · 🟠 **잠정** |
| OD-MS-008 | No body change · only first empirical note reinforced |

- 🟢 **Kiro work outputs**

| File | Change |
| --- | --- |
| `09/operation-notes.md` | Newly created · 2026-06-13 §1~§8 |
| `_common/operator-decisions.md` | Added OD-MS-018 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | 2026-06-13 09 spec follow-up note |
| `_common/risk-register.md` | Added R-AUTO-015 · R-DATA-008 |
| `.kiro/CHANGELOG.md` | Sixth 2026-06-13 section |
| `.kiro/WORKLOG.md` | This file |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| AWS · Docker · ECR · IAM · Secrets Manager · RDS work | All performed directly by operator · Kiro only organized documentation · procedures · validation |
| AWS Batch SubmitJob | 0 |
| CloudWatch Log Group · Secret · IAM Role created on this date | Not created |
| full backtest · long-running research · report generation · extended analysis · block-family backtest | 0 |
| RDS DDL/DML | 0 |
| broker · KIS calls | 0 |
| Live `secretsmanager:GetSecretValue` call | 0 (secret not created) |
| Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog | 0 |
| Quoted body of operator-authored Dockerfile · requirements.txt · 3 adapters · 3 import-change files | 0 (facts recorded in 09 spec operation-notes) |
| Newly recorded sensitive information (secret · RDS · KIS · account · token · account-id · ARN · image digest · IAM access key · Batch job ARN) | 0 · all `[REDACTED]` or placeholders |

  <details><summary>🔵 Operational identifier summary</summary>

  - image tag `paper-20260613` · `paper-latest`
  - ECR repository `portfolio-strategy-research`
  - Dockerfile · adapter file paths
  - image size about 90MB

  </details>

- Next work · first creation of AWS Batch Compute Environment · Job Queue · Job Definition · CloudWatch Log Group · Secrets Manager `/portfolio/paper/rds/research-app` · IAM Role (execution + job) · short no-op
  import-smoke SubmitJob (`smoke` prefix + allowlist) · full backtest · report generation only after separately defining cost/time/timeout/operator-approval criteria · formal move of Research internal adapters into `port_strategy_common` package · Step Functions integration · CI/CD OIDC · all separated as follow-up.

## 2026-06-13 (Strategy Execution ECS / Fargate first porting validation)

- 🟢 **Work summary** (follow-up after same-date sessions 1·2·3·4)

| Item | Value |
| --- | --- |
| Nature | Performed directly by operator · first main-phase ECS / Fargate porting validation for `port_strategy_execution` |
| Reference | 04 spec operation-notes 2026-06-13 Strategy Execution ECS / Fargate first porting validation |
| broker · KIS calls · `connector_order_request` creation · actual `READY -> REQUESTED` transition | 0 |
| live automatic orders | <span style="color:#D1242F">**Prohibited**</span> until follow-up approval (OD-SAFE-002 · OD-SAFE-003) |

- 🟢 **Docker / ECR**

| Item | Value |
| --- | --- |
| Dockerfile · requirements.txt | Newly authored directly by operator · Docker build context `C:\Workspaces` · source vendoring · initial dependency `psycopg2-binary` · default CMD safe `py_compile` |
| Local build | `portfolio-strategy-execution:paper-20260613` · `paper-latest` |
| Container smoke | `py_compile` + import smoke · confirmed `execution_config.py` requires `INTEREST_DB_PASSWORD` · dummy env passed · actual ECS execution uses Secrets Manager injection |
| ECR push | repository `portfolio-strategy-execution` · `paper-20260613` · `paper-latest` |

- 🟢 **AWS execution resources · Network**

| Item | Value |
| --- | --- |
| CloudWatch Log Group | `/portfolio/paper/strategy-execution` · retention 14 days |
| Secrets Manager | `/portfolio/paper/rds/execution-app` JSON multi-key |
| Execution Role | `portfolio-paper-ecs-task-execution-role` · execution-app secret-read inline (ARN-scoped · wildcard 0) |
| Task Role | confirmed `portfolio-paper-execution-task-role` |
| Network | cluster `portfolio-paper-cluster` · public-a + public-b · SG `sgroup-strategy-tasks` · RDS SG inbound + VPC Endpoint SG 443 source allowed · `assignPublicIp=ENABLED` |

- 🟢 **Task Definition**

| Item | Value |
| --- | --- |
| family · revision | `portfolio-paper-strategy-execution` revision 1 ACTIVE |
| Spec | awsvpc · Fargate · cpu 512 · memory 1024 · container `strategy-execution` · 5 `INTEREST_DB_*` secret injections · `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` |
| Compared with Strategy Decision | Intentionally different from split-two-TD Decision pattern (OD-MS-013) · 7 entrypoints share same image · role · secret · log group · cpu · memory · splitting into 7 TDs judged excessive |
| command-override mapping | Confirmed (OD-MS-017) |

- 🟢 **8 RunTask validations (all exitCode 0 / lastStatus STOPPED)**

| Case | Result |
| --- | --- |
| (a) default `py_compile` | Passed |
| (b) `execution_sync_buy_fill.py` | NO_TARGET · submitted · synced · skipped all 0 |
| (c) `execution_sync_sell_fill.py` | Same |
| (d) `execution_sync_buy_position.py` | filled_buy_orders_without_position 0 |
| (e) `daily_buy_execution_run.py` | Blocked by WEEKEND guard · NO_TARGET · `connector_order_request` 0 |
| (f) `daily_sell_execution_run.py` | Same |
| (g) `daily_auto_sell_execute_run.py --execute` | Blocked by WEEKEND guard · 0 `READY -> REQUESTED` transitions · 0 broker · KIS calls |
| (h) `daily_auto_buy_execute_run.py --execute` | Same |

- 🟡 **Decision locks (fifth session on 2026-06-13)**

| ID | Content |
| --- | --- |
| OD-MS-017 | port_strategy_execution TD operation = single TD + command override · family revision 1 ACTIVE · 7-entrypoint allowlist · 🟠 **잠정** |
| OD-MS-007 · OD-MS-009 · OD-SAFE-004 | First empirical notes reinforced without body changes |
| R-AUTO-001 | Mitigation · detection reinforced |
| New R-AUTO-014 | Risk of incorrect mapping under single TD + command override · Status 🟠 **Open** |

- 🟢 **Kiro work outputs**

| File | Change |
| --- | --- |
| `04/operation-notes.md` | 2026-06-13 Strategy Execution ECS / Fargate section §1~§11 |
| `_common/operator-decisions.md` | Added OD-MS-017 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | Follow-up note after first Strategy Execution validation on 2026-06-13 |
| `_common/risk-register.md` | Reinforced R-AUTO-001 + added R-AUTO-014 |
| `.kiro/CHANGELOG.md` | Fifth 2026-06-13 section |
| `.kiro/WORKLOG.md` | This file |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · RDS · GRANT work | All performed directly by operator · Kiro only organized documentation · procedures · validation |
| Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging | 0 |
| Plaintext `secretsmanager:GetSecretValue` result values | 0 |
| Plaintext secret values in CloudWatch Logs body | 0 |
| Newly recorded sensitive information (secret · RDS · KIS · account · token · account-id · ARN · image digest · IAM access key · task ARN) | 0 · all `[REDACTED]` or placeholders |

  <details><summary>🔵 Operational identifier summary</summary>

  - image tag `paper-20260613` · `paper-latest`
  - Task Definition family · revision · container name
  - SG · Log Group · Secret · Role names

  </details>

- Next work · Step Functions state machine (map 2 Strategy Decision TDs + single Strategy Execution TD + command override) · EventBridge Scheduler integration · end-to-end handoff validation to MarketConnector executor
  weekday or safe-test-data `READY -> REQUESTED -> SUBMITTED` end-to-end validation · promote View Daily Batch ProcessBuilder → monitoring UI (long term) · formally organize `execution_app` schema-permission matrix (02 spec) · CI/CD OIDC (07 spec) · all separated as follow-up.

## 2026-06-13 (Local-to-AWS Paper RDS SSM Port Forwarding reinforcement — psql 18 client + pgAdmin4 connection validation)

- 🟢 **Work summary** (follow-up to same-date third session: SSM Port Forwarding connection validation + initial Runbook body)

| Item | Value |
| --- | --- |
| Nature | Performed directly by operator · first empirical AWS Paper RDS connection validation using two additional clients on the same SSM tunnel (direct path to local PostgreSQL 18 `psql.exe` + pgAdmin4) |
| SSM tunnel | Reuse same command · same local port `15433` |
| RDS Public not enabled · OD-NET-009 · R-SEC-001 · R-NET-004 | No body change |
| AWS · RDS · IAM · Secrets Manager · SSM changes | 0 |
| Tools used | read-only AWS API + SSM Port Forwarding session + psql / pgAdmin4 SELECT only |

- 🟢 **Local PostgreSQL 18 psql-client direct-path validation**

| Item | Value |
| --- | --- |
| Command | `& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -p 15433 -U portfolio_admin -d portfolio` |
| client · server | `18.1` · `18.4` |
| SSL connection | `TLSv1.3` |
| Output | `current_user=portfolio_admin` · `current_database=portfolio` · `inet_server_addr=10.0.20.165` · `inet_server_port=5432` |
| Alignment | client major 18 · full 18.4 (R-DATA-003 · 03 spec design §5) |
| General `psql` PATH registration | Incomplete · work proceeded by direct full-path execution |
| Password handling | `PGPASSWORD` environment variable in PowerShell session · removable with `Remove-Item Env:PGPASSWORD` · 0 plaintext records |

- 🟢 **Local PostgreSQL installation inventory**

| Item | Value |
| --- | --- |
| Path | `C:\Program Files\PostgreSQL` |
| Version folders | `17` · `18` |
| Used for validation on this date | `18` |
| Additional clients missing | 0 |

- 🟢 **pgAdmin4 Server registration · validation**

| Item | Value |
| --- | --- |
| Name | `AWS Paper RDS - portfolio` |
| Host name/address | `localhost` |
| Port | `15433` |
| Maintenance database | `portfolio` |
| Username | `portfolio_admin` |
| Password | `[REDACTED]` |
| SSL mode | `Prefer` or default TLS |
| Tunnel-window confirmation | `Connection accepted for session [...]` output |
| Validation SQL | `select current_user, current_database(), inet_server_addr(), inet_server_port(), current_setting('search_path');` |
| Result | `portfolio_admin` · `portfolio` · `10.0.20.165` · `5432` · `"$user", public` |
| search_path interpretation | `portfolio_admin` is not a target of `ALTER ROLE ... SET search_path` · `"$user", public` is normal (aligned with OD-DB-006) |

- 🟢 **pgAdmin4 SELECT access confirmed · query scope**

| Item | Value |
| --- | --- |
| Schema count · core table count | 6 · 16 |
| Queryable tables | SELECT passed across 6 schemas · 16 tables<br>connector · decision · execution · interest · ops · preprocessor<br>detailed table list in [02 spec operation-notes 2026-06-13](specs/02-aws-network-and-rds/operation-notes.md) |
| Query scope | SELECT only · INSERT · UPDATE · DELETE · DDL 0 |

- 🟢 **pgAdmin4 operating principles**

| Item | Value |
| --- | --- |
| Tunnel required | SSM Port Forwarding PowerShell window must be open before pgAdmin4 accesses AWS Paper RDS · `Port 15433 opened` · `Waiting for connections...` must remain active |
| Tunnel termination | Closing tunnel window or `Ctrl + C` immediately disconnects pgAdmin4 |
| Server registration | Direct RDS endpoint registration <span style="color:#D1242F">**Prohibited**</span> · configure only `localhost:15433` |
| Actual connection target | AWS Private RDS (`10.0.20.165:5432`) |

- 🟡 **Decision lock (fourth session on 2026-06-13)**

| ID | Content |
| --- | --- |
| OD-NET-011 | Local-to-AWS Paper RDS pgAdmin4 principle = register only `localhost:15433` SSM tunnel · direct RDS endpoint registration <span style="color:#D1242F">**Prohibited**</span> · standard Server registration (Name `AWS Paper RDS - portfolio` · Host `localhost` · Port `15433` |
| OD-NET-011 (continued) | Maintenance database `portfolio` · Username `portfolio_admin` or per-MS app role · SSL mode `Prefer`) · <span style="color:#BF8700">**잠정**</span> |

- 🟢 **Runbook reinforcement · Strategy Execution pre-validation summary · risk reinforcement**

| Item | Value |
| --- | --- |
| 02 spec Runbook §10 | No body change · added auxiliary procedure §13 |
| Auxiliary procedure | (a) Direct-path execution of PostgreSQL 18 psql client (b) pgAdmin4 Server registration (Name · Host · Port · DB · Username · SSL mode) (c) reinforce success criteria (`inet_server_addr=10.0.20.165` · `inet_server_port=5432` · RDS `PubliclyAccessible=False`) |
| Strategy Execution pre-validation | First empirical confirmation that all three clients on this date (Python `psycopg2` · psql 18 · pgAdmin4) can access AWS Paper RDS through SSM tunnel · multi-client validation complete before Strategy Execution main phase |
| R-AUTO-012 detection reinforcement | Monitor pgAdmin4 disconnect patterns · `Connection terminated` · `server closed the connection unexpectedly` + automatic recovery after tunnel restart |
| R-AUTO-013 mitigation reinforcement | PostgreSQL 18 client already installed (`C:\Program Files\PostgreSQL\18\bin\psql.exe`) · risk narrowed from missing client to unregistered general `psql` PATH · Status 🟢 **Mitigated** retained |

- 🟢 **Kiro work outputs**

| File | Change |
| --- | --- |
| `02/operation-notes.md` | 2026-06-13 SSM Port Forwarding reinforcement (psql 18 + pgAdmin4) §1~§5 |
| `03/operation-notes.md` | 2026-06-13 SSM Port Forwarding standard-waypoint role · psql 18 + pgAdmin4 reinforcement §1~§3 |
| `04/operation-notes.md` | 2026-06-13 Strategy Execution pre-validation · psql 18 + pgAdmin4 reinforcement §1~§4 |
| `_common/operator-decisions.md` | Added OD-NET-011 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | Follow-up note for SSM Port Forwarding reinforcement on 2026-06-13 |
| `_common/risk-register.md` | Reinforced R-AUTO-012 · R-AUTO-013 mitigation · detection |
| `.kiro/CHANGELOG.md` | Fourth 2026-06-13 section |
| `.kiro/WORKLOG.md` | This file |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| AWS · RDS · IAM · Secrets Manager · SSM changes | 0 |
| Tools used | read-only AWS API + reuse of SSM session + SELECT-only queries via psql 18 / pgAdmin4 / Python `psycopg2` |
| RDS DDL/DML | 0 |
| broker · KIS · order · fill · Daily Batch entrypoint calls | 0 |
| Live `--execute` calls | 0 |
| Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source | 0 |
| pgAdmin4 · psql 18 client | Operator-local PC tools · no separate impact on spec outputs |
| Newly recorded sensitive information (password · secret · KIS · account · token · account-id · IAM access key · secret ARN · EIP) | 0 · all `[REDACTED]` or placeholders |

  <details><summary>🔵 Operational identifier summary</summary>

  - Reuse from prior third session: instance id · private IP · local port · SSM session id `terraform-vjp3fv3nz73konetcevdzjh9de` · RDS endpoint hostname
  - Additional: local PostgreSQL install path `C:\Program Files\PostgreSQL\18\bin\psql.exe` (operator-local PC tool · not a secret)

  </details>

- Next work · register general `psql` PATH (`C:\Program Files\PostgreSQL\18\bin`) · standardize environment-specific pgAdmin4 Server-registration policy (paper · live, server-name prefix) · pgAdmin4 connection-pool automatic reconnect policy · additional client connection validation by MS app role (Strategy Execution `execution_app`
  MarketConnector `marketconnector_app` · Interest Crawler `crawler_app` · Interest Preprocessor `preprocessor_app` · View `view_app`) · weekday or safe-test-data `READY -> REQUESTED -> SUBMITTED` end-to-end dry · integration validation
  all separated as follow-up.

## 2026-06-13 (Local-to-AWS Paper RDS SSM Port Forwarding connection validation + initial Runbook body)

- 🟢 **Work summary** (third session on the same date)

| Item | Value |
| --- | --- |
| Nature | Performed directly by operator · first empirical local → AWS Paper RDS connection validation using SSM Port Forwarding |
| Decision lock | New OD-NET-010 (standard SSM Port Forwarding waypoint) + first empirical-note reinforcement for OD-ENV-006 · OD-ENV-007 · OD-ENV-008 |

- 🟢 **Pre-check tools**

| Item | Value |
| --- | --- |
| `aws --version` | `aws-cli/2.27.50 Python/3.13.4 Windows/11 exe/AMD64` |
| Session Manager Plugin | Initially not recognized → after installation `1.2.814.0` |

- 🟢 **Standard SSM Port Forwarding waypoint decision**

| Item | Value |
| --- | --- |
| Candidate EC2 1 | `portfolio-paper-marketconnector-ec2` · instance id `i-0fce77927b7397b88` · private ip `10.0.0.181` · running |
| Candidate EC2 2 | `portfolio-paper-crawler-worker` · instance id `i-0ff768ea639a91355` · private ip `10.0.0.169` · running |
| Decision | `portfolio-paper-marketconnector-ec2` |
| Reason | Has RDS-access validation history (2026-06-09 RDS restore runner) · natural waypoint for Paper DB access used by MarketConnector / Strategy Execution / View |
| crawler worker | Retain KRX GUI / Windows-worker role · not used as SSM Port Forwarding waypoint |

- 🟢 **Target EC2 SSM Online · AWS Paper RDS endpoint · SSM tunnel opened**

| Item | Value |
| --- | --- |
| `describe-instance-information` | ping `Online` · platform `Linux` · agent `3.3.4515.0` |
| RDS status | DB name `portfolio` · endpoint `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com` · port `5432` · `PubliclyAccessible=False` · status `available` |
| RDS Private policy | No change (R-SEC-001 · R-NET-004 · OD-NET-009) |
| `start-session` command | `AWS-StartPortForwardingSessionToRemoteHost` · host `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com` · portNumber `5432` · localPortNumber `15433` |
| session id · local port | `terraform-vjp3fv3nz73konetcevdzjh9de` · `15433` |
| Output | `Port 15433 opened` · `Waiting for connections...` |

- 🟢 **Local psql not recognized · used Python `psycopg2`**

| Item | Value |
| --- | --- |
| Failed command | `psql -h localhost -p 15433 -U portfolio_admin -d portfolio` (not recognized by PowerShell) |
| Cause | Local PostgreSQL client PATH not registered (not an AWS / SSM / RDS consistency issue) |
| Action | Deferred psql installation · proceeded with Python `psycopg2` (new R-AUTO-013 mitigation) |
| `psycopg2` check | `python -c "import psycopg2; print('psycopg2 OK')"` = `psycopg2 OK` |

- 🟢 **`portfolio_admin` connection validation**

| Item | Value |
| --- | --- |
| host · port · dbname · user | `localhost` · `15433` · `portfolio` · `portfolio_admin` |
| password | Used environment variable `PGPASSWORD` · plaintext exposure 0 |
| Python result | `('portfolio_admin', 'portfolio', '10.0.20.165', 5432)` |
| Meaning | First validation of local PC → SSM tunnel → AWS Paper RDS passed · confirmed actual target behind `localhost:15433` is AWS Paper RDS via `inet_server_addr=10.0.20.165` · `inet_server_port=5432` (reinforced R-DATA-007 detection) |

- 🟢 **`execution_app` connection validation**

| Item | Value |
| --- | --- |
| host · port · dbname · user | `localhost` · `15433` · `portfolio` · `execution_app` |
| password | Used environment variable `PGPASSWORD` |
| current_user · current_database | `execution_app` · `portfolio` |
| search_path | `execution, decision, research, connector, preprocessor, interest, reference, legacy, public` (aligned with OD-DB-006 · OD-DB-007 · access to legacy blocked because USAGE not granted) |
| Meaning | First validation of AWS Paper RDS app-role connectivity passed before Strategy Execution AWS porting |

- 🟢 **Initial body of Local-to-AWS Paper RDS SSM Port Forwarding Runbook**

| Item | Value |
| --- | --- |
| Structure | 7 steps: pre-check · open tunnel · standard environment-variable export · connection validation · per-MS app-role mapping · success criteria · failure/recovery + `[실행]`/`[확인]`/`[준비]`/`[복구]` labels |
| Standard environment variables | `PORT_ENVIRONMENT=paper` · `PORT_DB_TARGET=aws-paper` · `INTEREST_DB_HOST=localhost` · `INTEREST_DB_PORT=15433` · `INTEREST_DB_NAME=portfolio` |
| App role by MS | Strategy Execution `execution_app` · MarketConnector `marketconnector_app` · Interest Crawler `crawler_app` · Interest Preprocessor `preprocessor_app` · View `view_app` |
| Plaintext recording | Password · secret value in body · operator notes · console captures · logs <span style="color:#D1242F">**Prohibited**</span> (aligned with R-DOCS-001) |

- 🟡 **Decision locks (third session on 2026-06-13)**

| ID | Content |
| --- | --- |
| OD-NET-010 | Standard SSM Port Forwarding waypoint = `portfolio-paper-marketconnector-ec2` · local port `15433` · 🟠 **잠정** |
| OD-ENV-006 · OD-ENV-007 · OD-ENV-008 | First empirical notes reinforced without body changes |

- 🟢 **Kiro work outputs**

| File | Change |
| --- | --- |
| `02/operation-notes.md` | 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding connection validation + Runbook §1~§12 |
| `03/operation-notes.md` | 2026-06-13 first validation of standard SSM Port Forwarding waypoint role §1~§5 |
| `04/operation-notes.md` | 2026-06-13 Strategy Execution AWS-porting pre-validation §1~§4 |
| `_common/operator-decisions.md` | Added OD-NET-010 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | 2026-06-13 SSM Port Forwarding follow-up note |
| `_common/risk-register.md` | Reinforced R-DATA-007 detection + added R-AUTO-012 · R-AUTO-013 |
| `.kiro/CHANGELOG.md` | Third 2026-06-13 section |
| `.kiro/WORKLOG.md` | This file |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| AWS · RDS · IAM · Secrets Manager · SSM changes | 0 |
| Tools used | read-only AWS API calls + SSM Port Forwarding session + Python `psycopg2` SELECT only |
| RDS DDL/DML | 0 |
| broker · KIS · order · fill · Daily Batch entrypoint calls | 0 |
| Live `--execute` calls | 0 |
| Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source | 0 |
| Newly recorded sensitive information (password · secret · KIS · account · token · account-id · IAM access key · secret ARN · EIP) | 0 · all `[REDACTED]` or placeholders |

  <details><summary>🔵 Operational identifier summary</summary>

  - instance id `i-0fce77927b7397b88` · `i-0ff768ea639a91355`
  - private IP `10.0.0.181` · `10.0.0.169` · `10.0.20.165`
  - local port `15433`
  - SSM session id `terraform-vjp3fv3nz73konetcevdzjh9de`
  - RDS endpoint hostname `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com`

  </details>

- Next work · formally install psql client · register PATH (R-AUTO-013) · inspect Paper-mode DB environment variables across all MS · Strategy Execution ECS / Fargate main porting phase · MarketConnector new-executor EC2 deployment-candidate zip · tag
  weekday or safe-test-data `READY -> REQUESTED -> SUBMITTED` end-to-end dry · integration validation · decide whether to introduce automatic SSM Port Forwarding session keep-alive · reconnect · all separated as follow-up.

## 2026-06-13 (Strategy Execution / MarketConnector responsibility separation + View Daily Batch 17 steps + Local-to-AWS Paper RDS operating principles)

- 🟢 **Work summary** (second session on the same date · separate from Strategy Decision ECS / Fargate validation section)

| Item | Value |
| --- | --- |
| (a) Strategy Execution responsibility separation | `execution_repository.py` · `daily_auto_buy_execute_run.py` · `daily_auto_sell_execute_run.py` |
| (b) New MarketConnector executor | `connector_strategy_order_execute.py` |
| (c) View Daily Batch 17-step restructuring | `DailyBatchService.java` · `DailyBatchLabelUtils.java` |
| (d) Local development / AWS Paper RDS operating principles | Organized |
| Nature | Performed directly by operator · Kiro only organized documentation · procedures · validation |

- 🟢 **Strategy Execution changes**

| Item | Value |
| --- | --- |
| 3 files | `execution_repository.py` · `daily_auto_buy_execute_run.py` · `daily_auto_sell_execute_run.py` |
| New repository function | `mark_execution_order_requested(conn, execution_order_id, result_payload)` |
| Removed | Hardcoded MarketConnector path · `sys.path` insertion · `connector_buy` / `connector_sell` import · direct `buy_stock()` / `sell_stock()` calls · SELL `mark_position_sell_ordered()` call |
| Narrowed meaning of `--execute` | Actual order submission → `READY -> REQUESTED` state transition |
| `connector_order_request_id` | Not created / updated by Strategy Execution |
| Static validation | `python -m py_compile` passed for 3 files · 0 direct import / call matches · UTF-8 Korean normal |
| Dry-run not confirmed | DB-candidate query stage not reached because of WEEKEND guard |
| Live `--execute` calls | 0 |

- 🟢 **MarketConnector changes**

| Item | Value |
| --- | --- |
| New file | `connector_strategy_order_execute.py` (489 insertions) |
| Query · sort | `REQUESTED` + `connector_order_request_id IS NULL` · SELL first · BUY second |
| Default behavior | Dry run · list only · KIS calls · DB updates 0 |
| `--execute` guard | `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` · lazy import of `connector_buy` / `connector_sell`, then call `buy_stock()` / `sell_stock()` · on success set `strategy_execution_order` `SUBMITTED` · on failure set `FAILED` · on successful SELL set position `SELL_ORDERED` |
| Changes to 8 existing entrypoints | 0 (`connector_buy.py` · `connector_sell.py` · `connector_order_common.py` · `connector_order_check.py` · `connector_balance.py` · `db_config.py` · `config.py` · `token_manager.py`) |
| Static validation | `python -m py_compile` passed · UTF-8 Korean normal · dry-run result `[NO_TARGET] REQUESTED strategy order 없음` (0 REQUESTED targets) |
| Live `--execute` calls | 0 |

- 🟢 **View changes**

| Item | Value |
| --- | --- |
| 2 files | `src/main/java/my/portfolio/port_view/service/DailyBatchService.java` · `src/main/java/my/portfolio/port_view/util/DailyBatchLabelUtils.java` |
| New step | `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` · order 12 · name `Strategy 주문 실행` · command `python connector_strategy_order_execute.py --execute` |
| Insertion point | After `DAILY_AUTO_BUY`(11) · before `CONNECTOR_ORDER_CHECK` |
| Subsequent step-order changes | `CONNECTOR_ORDER_CHECK` 13 · `SYNC_SELL_FILL` 14 · `SYNC_BUY_FILL` 15 · `SYNC_BUY_POSITION` 16 · `BALANCE_REFRESH` 17 |
| 5 isNoTarget phrases | `REQUESTED strategy order 없음` · `strategy execution requested 주문이 없음` · `[NO_TARGET] REQUESTED strategy order 없음` · `no requested` · `no target` |
| Labels added | both `DailyBatchLabelUtils.stepCodeLabel()` · `stepCodeShortLabel()` |
| Compile | `.\mvnw.cmd clean compile` succeeded |
| Daily Batch execution · Python order scripts · DB · AWS access | 0 |

- 🟢 **Local development / AWS Paper RDS operating principles**

| Item | Value |
| --- | --- |
| Paper source of truth | Fixed to single AWS Paper RDS |
| `PORT_ENVIRONMENT=paper` | Local execution also uses AWS Paper RDS · AWS execution uses same |
| Local PostgreSQL | `LOCAL_DEV` fixture · experiment · backup reference only |
| Environment labels | `LOCAL_DEV` · `PAPER` · `LIVE` · `LIVE` is follow-up design target |
| Local → AWS Paper RDS access | SSM Port Forwarding · retain RDS Private (aligned with R-SEC-001 · R-NET-004) |
| Environment identification | Even if endpoint is `localhost:15433`, actual target = AWS Paper RDS · identifying environment by host alone <span style="color:#D1242F">**Prohibited**</span> |
| paper-order execution guard | `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` |
| Local DB ↔ AWS Paper RDS synchronization | Merging `connector_order_request` · `connector_fill` · `strategy_execution_order` · `strategy_position_state` <span style="color:#D1242F">**Prohibited**</span> |

- 🟡 **Decision locks (second session on 2026-06-13)**

| ID | Content |
| --- | --- |
| OD-MS-016 | Strategy Execution / MarketConnector responsibility separation · 🟠 **잠정** |
| OD-ENV-006 | Paper-environment DB source of truth = single AWS Paper RDS · 🟠 **잠정** |
| OD-ENV-007 | Local PC → AWS Paper RDS access method = SSM Port Forwarding only · 🟠 **잠정** |
| OD-ENV-008 | No Local DB ↔ AWS Paper RDS synchronization · 🟠 **잠정** |

- 🟢 **Kiro work outputs**

| File | Change |
| --- | --- |
| `03/operation-notes.md` | 2026-06-13 Strategy-order executor section |
| `04/operation-notes.md` | 2026-06-13 Strategy Execution responsibility separation + View Daily Batch 17-step changes |
| `02/operation-notes.md` | 2026-06-13 Local-to-AWS Paper RDS operating-mode section |
| `_common/operator-decisions.md` | Added OD-MS-016 · OD-ENV-006 · OD-ENV-007 · OD-ENV-008 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | 2026-06-13 responsibility-separation + Local-to-AWS Paper RDS follow-up note |
| `_common/risk-register.md` | Added R-DATA-007 · R-AUTO-009 · R-AUTO-010 · R-AUTO-011 |
| `.kiro/CHANGELOG.md` | Second 2026-06-13 section |
| `.kiro/WORKLOG.md` | This file |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| AWS · RDS · SSM · EC2 · Docker · ECR · ECS · IAM · Secrets Manager work | 0 |
| broker · KIS · order · fill · Daily Batch entrypoint calls | 0 |
| RDS DDL/DML | 0 |
| Live `--execute` calls | 0 |
| Code changes (3 Strategy Execution · 1 new MarketConnector · 2 View) | All performed directly by operator · Kiro only organized facts · procedures · validation |
| Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog | 0 |
| Quoted body of operator-authored Python / Java changes | 0 (facts recorded in this file · spec artifacts) |
| Newly recorded sensitive information (secret · account · RDS · KIS · token · account-id · ARN · instance-id · EIP · IAM access key) | 0 · all `[REDACTED]` or placeholders |

- Next work · organize SSM Port Forwarding runbook · inspect `PORT_ENVIRONMENT` · `PORT_DB_TARGET` across all MS · Strategy Execution AWS porting (`port_strategy_execution` ECS · Fargate) · candidate zip/tag for MarketConnector new-executor EC2 deployment
  weekday or safe-test-data `READY -> REQUESTED -> SUBMITTED` end-to-end dry · integration validation · pre-operation end-to-end validation of new View 17-step flow · all separated as follow-up.

## 2026-06-13 (Strategy Decision ECS / Fargate first porting validation + 08 spec SSM automation + ECS crawler smoke)

- 🟢 **Work summary** (04 spec Strategy Decision + 08 spec SSM automation + ECS crawler smoke)

| Item | Value |
| --- | --- |
| (04) Strategy Decision | Dockerfile · ECR · Log Group · Secret · Role · 2 TDs · 2 RunTasks |
| (08) SSM automation | Managed Node · Scheduled Task · `schtasks /Run` trigger |
| (08) ECS crawler smoke | `portfolio-paper-interest-crawler:6` · Selenium Chrome smoke |
| Nature | Performed directly by operator · Kiro only organized documentation · procedures · validation |

- 🟢 **04 spec — Strategy Decision Docker / ECR**

| Item | Value |
| --- | --- |
| Dockerfile · requirements.txt | Newly authored directly by operator · Docker build context `C:\Workspaces` · image vendoring `port_strategy_common` + `port_strategy_decision` |
| Local build · container smoke | `portfolio-strategy-decision:paper-20260613` · import smoke succeeded |
| ECR push | repository `portfolio-strategy-decision` · `paper-20260613` · `paper-latest` |

- 🟢 **04 spec — AWS resources**

| Item | Value |
| --- | --- |
| CloudWatch Log Group | `/portfolio/paper/strategy-decision` · retention 14 days |
| Secrets Manager | New `/portfolio/paper/rds/decision-app` JSON multi-key |
| Execution Role | decision-app secret-read inline (ARN-scoped · wildcard 0) |
| Task Role | New `portfolio-paper-decision-task-role` |
| ECS Cluster · Task Execution Role | `portfolio-paper-cluster` · `portfolio-paper-ecs-task-execution-role` (initially created in 08 spec · reused on this date) |

- 🟢 **04 spec — 2 Task Definitions · 2 RunTasks**

| Item | Value |
| --- | --- |
| 2 TDs | `portfolio-paper-strategy-decision-buy-signal` · `portfolio-paper-strategy-decision-position-signal` |
| Spec | awsvpc · Fargate · cpu 512 · memory 1024 · 5 env vars: `INTEREST_DB_HOST` · `INTEREST_DB_PORT` · `INTEREST_DB_NAME` · `INTEREST_DB_USER` · `INTEREST_DB_PASSWORD` |
| New integrated `daily_decision_run.py` | Deferred · inherit existing Daily Batch step 6 / step 7 structure · split into 2 TDs (OD-MS-013) |
| RunTask result | exitCode 0 · CloudWatch logs + RDS connection succeeded · `daily_run_id` 44 · `run_date` 2026-06-13 · `data_date` 2026-06-08 · `market_signal` BLOCK · block_watch 1 · position decisions 0 |
| First failures · actions | (a) buy-signal: insufficient permission on `research.strategy_block_watch_candidate` → GRANT corrected (b) position-signal: failed to find `execution.strategy_position_state` (insufficient execution + decision schema permissions) → GRANT corrected · rerun succeeded |

- 🟢 **08 spec — SSM automation · Scheduled Task**

| Item | Value |
| --- | --- |
| SSM Managed Node | Checked `portfolio-paper-crawler-worker` |
| Judgment on direct SSM wrapper execution | Unsuitable for KRX GUI login · SYSTEM Session 0 / SessionId 0 vs SessionId 2 separation |
| New Windows Scheduled Task | `Portfolio-KRX-Worker-Daily` · Administrator interactive · `C:\portfolio\run_krx_worker_daily.ps1` |
| First `Start-ScheduledTask` validation | Success · `KRX ID/PW Login Success` · `interest_program` 1 row for 2026-06-12 · `interest_shortsell` 349 rows for 2026-06-12 · `KRX worker daily wrapper DONE` |
| RDP closed → SSM RunCommand → `schtasks /Run` trigger | Success · `SUCCESS: Attempted to run the scheduled task` · Task State Running → Ready · latest log `krx_worker_daily_20260613_021904.log` · `[Collected Date] None` = already collected through 2026-06-12 · idempotent / no-op normal |
| KRX GUI-path automation lock | OD-MS-015 |

- 🟢 **08 spec — ECS crawler smoke**

| Item | Value |
| --- | --- |
| Task Definition | `portfolio-paper-interest-crawler:6` · image `portfolio-interest-crawler:paper-20260611` · Fargate · cpu 1024 · memory 2048 · `taskRoleArn` `portfolio-paper-crawler-task-role` |
| RunTask condition | public-a / public-b + `sgroup-crawler-tasks` + `assignPublicIp=ENABLED` |
| Result | exitCode 0 · `SELENIUM CHROME SMOKE SUCCESS` · Selenium 4.40.0 · Chromium · chromedriver · reached `example.com` · reached Naver Finance (`Npay 증권`) · `DRIVER QUIT` · `SELENIUM CHROME SMOKE END` |
| Meaning of revision 6 | Selenium / Chrome / outbound smoke only · production TD separation remains follow-up (task 58) |

- 🔵 **First-stage Hybrid execution-model completion judgment**

| Item | Result |
| --- | --- |
| KRX program / shortsell | EC2 worker |
| non-GUI crawler runtime availability | ECS Fargate smoke passed |
| preprocessor | ECS Fargate Task |
| First automation entry point | SSM RunCommand → Scheduled Task trigger |
| Separated follow-ups | EventBridge Scheduler periodic trigger · Step Functions hybrid orchestration · production TD separation for non-GUI crawler · automatically add DB-validation output inside wrapper (R-AUTO-007) · EC2 worker stop procedure |

- 🟡 **Decision locks (2026-06-13)**

| ID | Content |
| --- | --- |
| OD-MS-013 | Strategy Decision TD split policy · 🟠 **잠정** |
| OD-MS-014 | First deployment of `port_strategy_common` = vendoring · 🟠 **잠정** |
| OD-MS-015 | First KRX GUI crawler automation = SSM RunCommand → `schtasks /Run` → Scheduled Task → Administrator interactive · direct SSM execution unsuitable · 🟠 **잠정** |

- 🟢 **Kiro work outputs**

| File | Change |
| --- | --- |
| `04/operation-notes.md` | Newly created (2026-06-13 §1~§10) |
| `08/operation-notes.md` | Added 2026-06-13 section |
| `08/tasks.md` | Updated tasks 30 · 31 · 53 + new §13 tasks 61~69 + Task Dependency Graph · carry-over summary |
| `08/design.md` | New §13 |
| `_common/operator-decisions.md` | Added OD-MS-013 · OD-MS-014 · OD-MS-015 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | Two 2026-06-13 follow-up notes + first-application result reinforcement in 04 / 08 spec bodies |
| `_common/risk-register.md` | Reinforced R-DATA-005 · R-AUTO-005 · R-AUTO-007 + added R-AUTO-008 |
| `.kiro/CHANGELOG.md` | 2026-06-13 section |
| `.kiro/WORKLOG.md` | This file |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · SSM · EC2 · RDS · GRANT work | All performed directly by operator · Kiro only organized documentation · procedures · validation |
| Changes to 8 MS (`port-view` · `port-marketconnector` · `port-interest-crawler` · `port-interest-preprocessor` · `port_strategy_common` · `port_strategy_decision` · `port_strategy_execution`<br>`port_strategy_research`) README · AGENTS.md · CHANGELOG · docs · worklog · source | 0 |
| Quoted body of operator-authored `port_strategy_decision` Dockerfile · requirements.txt · EC2 Scheduled Task · wrapper · env-injection ps1 | 0 (facts recorded in operation-notes) |
| Live `secretsmanager:GetSecretValue` calls | 0 |
| broker · KIS · order · fill · Daily Batch entrypoint calls | 0 |
| KRX login ID / password | Described only as "injected from Secrets Manager" |
| Newly recorded sensitive information (secret · RDS · KRX · KIS · account · token · account-id · ARN · image digest · IAM access key · instance-id · task ARN) | 0 · all `[REDACTED]` or placeholders |

- Next work · EventBridge Scheduler → ECS RunTask (04) · EventBridge Scheduler → SSM RunCommand → `schtasks /Run` (08) periodic trigger integration
  Step Functions state machine (enforce buy-signal → position-signal order + automatic retry <span style="color:#D1242F">**Prohibited**</span> OD-SAFE-004) · hybrid orchestration of ECS Task + EC2 worker from SFN · Strategy Execution ECS / Fargate porting validation
  formal `port_strategy_common` package
  version management (07 / Strategy Common) / formally organize `decision_app` permission matrix (02 spec db-roles-and-grants follow-up) / CloudWatch Logs Agent
  collect EC2 worker logs from SSM output / automatically add DB-validation output inside wrapper (R-AUTO-007) / separate production TD for non-GUI crawler (task 58) / stop EC2 worker after completion — all separated as follow-up.
## 2026-06-12 (First validation of KRX GUI-dependent collection using Windows EC2 worker)

- 🟢 **Work summary**

| Item | Value |
| --- | --- |
| Target | 08 spec KRX GUI-dependent collection (KRX program · KRX shortsell) reached first operationally usable state |
| Environment | Windows EC2 worker · venv `C:\portfolio\venvs\interest-crawler` · Python 3.13.5 |
| RDS Secret DB env injection | `/portfolio/paper/rds/crawler-app` |
| Download-path junction | `C:\Users\USER\Downloads` → `C:\Users\Administrator\Downloads` |
| New KRX Secrets Manager | `/portfolio/paper/krx/crawler-login` + IAM permission added (secret-scoped · wildcard 0) |
| Files executed successfully | Standalone execution of `interest_krx_login_new.py` · `interest_program.py` · `interest_shortsell.py` |

- 🟢 **DB load results**

| Table | Value |
| --- | --- |
| `interest_program_raw` | 543 → 546 (1 row each for 2026-06-09 · 2026-06-10 · 2026-06-11) |
| `interest_shortsell_raw` | 189158 → 190205 (349 rows each) |
| Load-consistency validation | Directly confirmed by operator using `check_program_rows.py` · `check_shortsell_rows.py` |

- 🟢 **EC2 worker daily wrapper**

| Item | Value |
| --- | --- |
| New wrapper | `run_krx_worker_daily.ps1` · created directly by operator |
| First run | KRX login · program · shortsell all succeeded |
| Rerun | No new collection target · `[Collected Date] None` output · idempotent / no-op normal completion |
| Log-file format | `krx_worker_daily_yyyyMMdd_HHmmss.log` |

- 🔵 **Classification decision (Hybrid execution model)**

| Item | Result |
| --- | --- |
| KRX GUI-dependent crawler | Confirmed separation to Windows EC2 worker |
| non-GUI crawler | Retain ECS Fargate Task candidate |
| preprocessor | Retain ECS Fargate Task (same as 2026-06-10) |
| Full automation (SSM RunCommand · EventBridge Scheduler · SFN hybrid orchestration · CloudWatch Logs · automatically add DB validation inside wrapper · EC2 worker stop procedure) | Separated as follow-up |

- 🟢 **Encoding principle**

| Item | Value |
| --- | --- |
| Future EC2 Python source modification | Use Python patch scripts instead of direct PowerShell `Get-Content` / `Set-Content` replace |
| Python patch script | `read_text(encoding="utf-8-sig")` + `write_text(encoding="utf-8")` |

- 🟢 **Kiro work outputs**

| File | Change |
| --- | --- |
| `08/operation-notes.md` | Added 2026-06-12 section |
| `08/tasks.md` | Updated crawler runtime items + §11 Windows EC2 worker tasks 44~52 + §12 follow-up tasks 53~60 |
| `08/design.md` | Reinforced §12 Hybrid execution model |
| `08/requirements.md` | Added R8 acceptance criterion 5 |
| `_common/operator-decisions.md` | Added OD-MS-011 · OD-MS-012 · OD-SEC-008 + Decision Summary · Tentative · Change Log |
| `_common/risk-register.md` | Reinforced R-AUTO-005 mitigation + added R-AUTO-006 · R-SEC-005 · R-DOCS-002 · R-AUTO-007 |
| `_common/followups-overview.md` | 2026-06-12 follow-up note |
| `.kiro/CHANGELOG.md` | 2026-06-12 section |
| `.kiro/WORKLOG.md` | This file |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| AWS · IAM · Secrets Manager · EC2 · RDS work | All performed directly by operator · Kiro only organized documentation · procedures · validation |
| Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging | 0 |
| Quoted body of operator-created EC2 ps1 · py tests · wrapper | 0 (facts recorded in operation-notes) |
| KRX login ID / password | Described only as "injected from Secrets Manager" |
| password rotate | Outside scope for this date |
| Newly recorded sensitive information (secret · KRX password · RDS · KIS · account · token · RDS endpoint · account-id · ARN · image digest · IAM access key · instance-id) | 0 · all `[REDACTED]` or placeholders |

- Next work — unattended EC2-worker execution through SSM RunCommand · EventBridge Scheduler · Step Functions hybrid orchestration · CloudWatch Logs Agent integration · automatically add DB-validation output inside wrapper · reorganize ECS / Fargate non-GUI crawler scope · explicitly define EC2-worker stop procedure — all separated as follow-up.

## 2026-06-10 (afternoon ~ evening session)

- 🟢 **Work summary** (first-stage baseline porting of 08-interest-crawler-and-preprocessor-ecs)

| Item | Value |
| --- | --- |
| 2 new ECR repositories | `portfolio-interest-crawler` · `portfolio-interest-preprocessor` |
| New Dockerfile + requirements.txt | preprocessor: `python:3.13-slim` + `pre_daily.py` · crawler: `python:3.13-slim` + Chromium / chromedriver + `interest_crawler_daily.py` |
| Local build · ECR push | `paper-20260610` · `paper-latest` complete |
| crawler Selenium dependency | First-stage resolution during build |

- 🟢 **ECS resources · Role · Log Group**

| Item | Value |
| --- | --- |
| ECS Cluster | `portfolio-paper-cluster` |
| Task Execution Role | `portfolio-paper-ecs-task-execution-role` · managed `AmazonECSTaskExecutionRolePolicy` + preprocessor DB secret-read inline · restricted to one secret ARN |
| 2 Task Roles | `portfolio-paper-preprocessor-task-role` · `portfolio-paper-crawler-task-role` |
| 2 CloudWatch Log Groups | `/portfolio/paper/preprocessor` · `/portfolio/paper/crawler` · retention 14 days |
| Network | Confirmed preprocessor task SG → RDS PostgreSQL SG inbound 5432 allowed |

- 🟢 **Preprocessor Task Definition · first RunTask validation**

| Item | Value |
| --- | --- |
| New Secrets Manager | `/portfolio/paper/rds/preprocessor-app` JSON multi-key |
| Task Definition | family `portfolio-paper-interest-preprocessor` · revision 1 · awsvpc · cpu 512 · memory 1024 · image `paper-20260610` · ECS `secrets` env injection |
| RunTask result | lastStatus `STOPPED` · exitCode 0 · `PREPROCESSOR PIPELINE END` |

- 🟡 **First · second failures · actions**

| Case | Result |
| --- | --- |
| First failure | Missing Secrets Manager JSON `host` key · psycopg2 attempted Unix socket `/var/run/postgresql/.s.PGSQL.5432` · resolved by recreating secret |
| Second failure | Insufficient `preprocessor_app` USAGE / SELECT permissions on 2 remaining `public` schema sequences (`pre_marketbreadth_daily_feature_id_seq` · `pre_macroeconomic_daily_feature_id_seq`) · operator directly applied GRANT · resolved |
| Handling principle | In both cases, SQL was executed directly by operator · Kiro only organized result · cause · action documentation |

- 🔵 **Crawler progress status**

| Item | Value |
| --- | --- |
| Complete | Dockerfile · requirements · build · push |
| Carry-over | Task Definition registration · RunTask runtime · KRX · Naver · yfinance outbound-reachability validation |
| 100% stabilization | Outside this spec scope |

- 🟢 **Kiro work outputs**

| File | Change |
| --- | --- |
| `08/tasks.md` | Updated |
| `08/operation-notes.md` | Newly created |
| `_common/risk-register.md` | Reinforced R-DATA-005 · R-DOCS-001 + added R-DATA-006 · R-AUTO-005 |
| `_common/followups-overview.md` | 2026-06-10 follow-up note + reinforced 08 spec first-application result |
| `_common/operator-decisions.md` | Change Log OD-NET-004 note |
| `.kiro/CHANGELOG.md` | 2026-06-10 section |
| `.kiro/WORKLOG.md` | This file |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| AWS · ECR · ECS · IAM · Secrets · RDS work | All performed directly by operator · Kiro only organized documentation · procedures · validation |
| Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog in this spec work | 0 (new port-interest-preprocessor · port-interest-crawler Dockerfile · requirements.txt created directly by operator are recorded only in operation notes) |
| Newly recorded sensitive information (secret · password · KIS · account · token · RDS endpoint · account-id · ARN · image digest · IAM access key) | 0 · all `[REDACTED]` or placeholders |
| password rotate | Outside documentation scope for this date |

- Next work — register crawler Task Definition · RunTask runtime · validate KRX · Naver · yfinance outbound reachability at runtime / further inspect remaining `public` schema sequences / decide timing for 08 spec runbook · validation-checklist.

## 2026-06-09 (afternoon ~ evening session)

- 🟢 **Work summary**

| Item | Value |
| --- | --- |
| Daily execution | Status review only · 0 code · automated-trading changes |
| `.kiro` document-structure organization | Consistently organized each document's purpose · auto-editability · need for manual edits · need for retention |
| Follow-up | Plan to correct filename typos such as `_common/operator-decisions.md` (`operator-dicisions.md`, etc.) when found |

- 🟢 **Local PostgreSQL pg_dump backup**

| Item | Value |
| --- | --- |
| File | `portfolio_full_20260609.dump` · 422,334,494 bytes |
| Storage | `C:\Workspaces\db-backup\portfolio_20260609\` (local) |
| Baseline | Table count by schema · row-count snapshot by table · major object counts for index · trigger · sequence · FK |
| Total tables | 81 |

- 🟢 **RDS restore-runner decision**

| Item | Value |
| --- | --- |
| Direct local → RDS connection | timeout · RDS endpoint resolves to private IP · Publicly accessible=No · expected behavior |
| RDS restore runner | Decided on MarketConnector EC2 |

- 🟢 **Created aws-paper MarketConnector EC2**

| Item | Value |
| --- | --- |
| OS · subnet · EIP | Amazon Linux 2023 · public subnet · EIP attached |
| IAM Role | SSM managed policy + read access to temporary S3 migration bucket |
| Access | EC2 Instance Connect succeeded · direct local SSH deferred because outbound 22 may be restricted |
| Basic checks | EC2 OS update · AWS CLI available by default |
| PostgreSQL client | Started at 15.18 · switched to 18.4 after detecting dump archive-header mismatch (psql 18.4 · pg_restore 18.4) |
| Private RDS connection | Succeeded from EC2 |

- 🟢 **RDS Restore**

| Item | Value |
| --- | --- |
| Path | Local dump → temporary S3 bucket → MarketConnector EC2 |
| File-size match | 422,334,494 bytes across local · S3 · EC2 |
| `pg_restore --list` | TOC 812 entries · line count 823 · dump source PostgreSQL 18.1 · format CUSTOM + gzip |
| Major-version mismatch judgment | Restoring 18.1 dump into existing RDS PostgreSQL 16.14 = risk of lower-major restore |
| Action | Deleted existing RDS and recreated on PostgreSQL 18.4 baseline (Public access No · initial DB `portfolio`) |
| First restore error | `role "postgres" does not exist` · cause = dump owner-role ↔ RDS-role mismatch |
| Rerun | Drop/recreate `portfolio` DB then `--no-owner --no-privileges` · completed without error |
| RDS object owner | Organized based on restore-execution account |

- 🟢 **Consistency validation**

| Item | Value |
| --- | --- |
| Table count by schema | 81 matched |
| table · index · sequence · FK · trigger | FK 33 · trigger 23 · all diff 0 versus local baseline |
| row-count CSV | 82 lines diff 0 |
| CRLF · LF difference | Comparison completed after `--strip-trailing-cr` normalization |

- 🟢 **First DB Role / permission-separation application**

| Item | Value |
| --- | --- |
| SQL execution | `db-roles-and-grants.md` §4 · directly by operator |
| `portfolio_owner` | Created NOLOGIN · granted `portfolio_owner` membership to `portfolio_admin` |
| Schema-owner transfer targets (9 domains) | reference · interest · preprocessor · research · decision · execution · connector · ops · legacy (public unchanged) |
| Existing table / sequence / index owners | Retained `portfolio_admin` |
| `REASSIGN OWNED BY portfolio_admin TO portfolio_owner` | Decided not to execute in first application |

- 🟢 **Created 7 app roles · GRANT matrix**

| Item | Value |
| --- | --- |
| 7 app roles | `marketconnector_app` · `view_app` · `crawler_app` · `preprocessor_app` · `decision_app` · `research_app` · `execution_app` |
| Attributes | All LOGIN true · SUPERUSER · CREATEDB · CREATEROLE · REPLICATION · BYPASSRLS false |
| GRANT matrix | legacy not granted · marketconnector_app = connector R/W + execution R-only · view_app = ops R/W + execution R-only |
| DEFAULT PRIVILEGES · search_path | Applied 7 |
| legacy USAGE | false for all roles |
| Sequence permission summary | connector 9 · decision 3 · execution 5 · interest 14 · ops 2 · preprocessor 15 · public 2 · reference 3 · research 19 |

- 🟢 **App-role connection tests**

| Item | Result |
| --- | --- |
| marketconnector_app | Successfully read connector_order_request 33 · strategy_execution_order 17 · execution write blocked · legacy USAGE false |
| execution_app | Read execution · decision · connector + execution write succeeded · legacy USAGE false |
| view_app | Read execution · connector · decision succeeded · execution write blocked · legacy USAGE false |
| Judgment | All matched expected behavior |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| Work in this session | Documentation + SQL execution inside DB only |
| AWS resources (new EC2 · delete/recreate existing RDS) | Performed directly by operator · Kiro recorded results only |
| Changes to 8 MS code · README · AGENTS.md · CHANGELOG · docs · worklog | 0 |
| Newly recorded sensitive information (password · secret · endpoint · account-id · account · token · app key · app secret · webhook URL) | 0 · all `[REDACTED]` or placeholders |

- Next work (planned for 2026-06-10) · baseline MarketConnector porting (EC2 Python venv · requirements · source placement · externalize KIS paper account · RDS access · validate `connector_balance.py` · `connector_order_check.py`) · reflect 03 spec runbook · validation-checklist · operation-notes
  integrated validation · rollback procedure. · Original ECS baseline-porting plan also remains, but carried-over MarketConnector baseline porting takes priority.

## 2026-06-09

- 🟢 **Work summary** (folder move + link correction)

| Item | Value |
| --- | --- |
| Moved 6 root common documents | `operator-decisions.md` · `ms-aws-service-decision-matrix.md` · `cost-simulation.md` · `followups-overview.md` · `aws-resource-glossary.md` · `risk-register.md` → `_common/` |
| Archive move | `note-aws-landscape-2021-vs-2026.md` → `_archive/` · filename/body retained |
| New `02/README.md` | One-page operator summary · current status · next work · completed resources · related-document links · 5 automatic-update-criteria sections |
| `.kiro/README.md` · `.kiro/AGENTS.md` | Updated folder structure · root-reference-document section based on `_common/` · `_archive/` |
| Bulk relative-link correction | All `.md` in 01 / 02 spec folders · `.kiro/docs/kiro-readonly-validator-iam.md` · root meta documents (README · CHANGELOG · WORKLOG) · no body changes except link paths |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| Actual AWS resource creation · modification · deletion | None |
| Changes to 8 MS code · README · AGENTS.md · CHANGELOG · docs · worklog | None |
| Newly recorded sensitive information (secret · token · password · app key · app secret · account · webhook URL · RDS endpoint · account-id · access key id) | 0 |

## 2026-06-06

- 🟢 **Work summary** (4 first-application auxiliary documents for 02 spec)

| Item | Value |
| --- | --- |
| Purpose | Add 4 auxiliary documents that operators can follow step by step in AWS Console |

- 🟢 **New `.kiro/specs/_common/risk-register.md`**

| Item | Value |
| --- | --- |
| Categories | R-NET · R-SEC · R-DATA · R-BROKER · R-AUTO · R-DOCS · R-COST |
| Initial registration | 12 risks |
| Same-date additions | 2 items: R-SEC-002 (Root credentials · MFA loss) · R-SEC-003 (portadmin credential loss) |

- 🟢 **3 auxiliary documents for 02 spec**

| File | Change |
| --- | --- |
| `01/traceability-matrix.md` | Requirement → Design → Task → Decision mapping |
| `02/runbook.md` first version | 18-Step execution procedure (VPC → Subnet → IGW → Route Table → confirm no NAT → SG → VPC Endpoint → RDS → DB preparation → cutover pre-preparation → validation → rollback) |
| `02/runbook.md` same-date follow-up | Added Korean execution-category labels (`[실행]` · `[확인]` · `[준비]` · `[복구]`) to every Step title + new label-legend section at top of body |
| Added Step 0 to `02/runbook.md` | `IAM 관리자 사용자 portadmin 생성` · renumbered previous Step 0~18 → Step 1~19 · incremented all body cross-references by +1 |
| 6 Step 0 sub-steps | Create portadmin user → attach AdministratorAccess policy → Console sign-in URL · account alias → enable MFA → log out Root and sign in as portadmin → harden Root security |
| `02/validation-checklist.md` | 10 checkbox sections · runbook pass/fail review · `Step 18 rollback` → `Step 19 rollback` |
| `02/traceability-matrix.md` | Requirement → Design → Task → Validation → Decision mapping · Risk mapping · incremented all runbook Step references in mapping tables / Acceptance Criteria reinforcement / Risk mapping by +1 |

- 🟢 **`.kiro/specs/_common/aws-resource-glossary.md`**

| Item | Value |
| --- | --- |
| New term | IAM User |
| Basis | IAM administrator-user concept such as portadmin began being used substantively in 02 runbook |

- 🟢 **Document updates · decision values retained**

| Item | Value |
| --- | --- |
| `.kiro/README.md` · `.kiro/CHANGELOG.md` | Updated guidance for new documents · change history |
| Decision values (Decision ID · selected value · cost impact · operational risk · follow-up-spec impact) | No change |
| Console click paths · input values · validation items · rollback order | No change |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| Actual AWS resource creation | None |
| Changes to 8 MS code · README · AGENTS.md · CHANGELOG · docs · worklog | None |
| Records of sensitive information (secret · token · password · app key · app secret · account · webhook URL · portadmin password · Root/portadmin MFA serial · backup codes) | 0 · `[REDACTED]` or placeholders |

## 2026-06-08

- 🟢 **Work summary** (02 spec Foundation · built directly by operator · Kiro = ReadOnly validation + documentation)

| Item | Value |
| --- | --- |
| AWS Foundation build | VPC · Subnet · IGW · Route Table · 8 SGs · 6 VPC Endpoints · RDS subnet group · parameter group · `portfolio-paper-rds` · Secrets Manager `/portfolio/paper/rds/master` |
| Nature | Operator directly used AWS Console · Kiro performed ReadOnly validation + documentation only |

- 🟢 **New · updated documents**

| File | Change |
| --- | --- |
| New `.kiro/docs/kiro-readonly-validator-iam.md` | Designed ReadOnly-dedicated IAM user `portfolio-kiro-readonly-validator` · policy `PortfolioKiroReadOnlyValidatorPolicy` · Allow / Deny policy JSON · Console procedure · AWS CLI procedure · validation-command collection · classification of automatically verifiable / manually confirmed items · `secretsmanager:GetSecretValue` |
| New `.kiro/docs/kiro-readonly-validator-iam.md` (continued) | Explicit Deny for KMS Decrypt |
| New `02/operation-notes.md` | AWS Foundation execution record (SG name prefix `sgroup-` decision · RDS creation result · KMS default, etc.) + accumulated follow-up validation sessions |
| New status-label system in `02/validation-checklist.md` | Checkboxes `- [ ]` → 4 labels `[O]` · `[X]` · `[Kiro 후속 작업 필요]` · `[운영자 확인 필요]` · reflected AWS ReadOnly automatic validation + document comparison + grep · final counts `[O]` 78 · `[X]` 0 · `[Kiro 후속]` 2 · `[운영자]` 20 |

- 🟢 **AWS ReadOnly automatic-validation results · expected values matched**

| Item | Value |
| --- | --- |
| VPC · 6 Subnets · IGW | Matched expected values |
| NAT | Not created |
| Route Table · 8 SGs · 6 VPC Endpoints · RDS instance · Secrets metadata | Matched expected values |
| ALB · ELB · Cost Anomaly Detection | 0 |

- 🟢 **Safety · security · execution principles**

| Item | Result |
| --- | --- |
| secret-value lookup | None (`DescribeSecret` metadata only) |
| KMS Decrypt call | None |
| AWS resource creation / modification / deletion | None |
| Changes to 8 MS code · README · AGENTS.md · CHANGELOG · docs · worklog | None (confirmed all 8 workspaces unchanged via `git status --short`) |
| Sensitive-information records (secret · password · access key · token · webhook URL · account) | 0 |
| Plaintext-pattern grep result for `.kiro/**/*.md` | 0 |

## 2026-06-05

- Created `.kiro/README.md`, `.kiro/CHANGELOG.md`, and `.kiro/WORKLOG.md`.
- Documented in README that the `.kiro` workspace manages AWS Migration specs across all 8 MS.
- Organized in README the roles and source-of-truth criteria for root common documents (`operator-decisions.md`, `ms-aws-service-decision-matrix.md`, `cost-simulation.md`, `followups-overview.md`, `aws-resource-glossary.md`, `note-aws-landscape-2021-vs-2026.md`).
- Decided to accumulate Kiro work logs concisely in a single `.kiro/WORKLOG.md` instead of separate files by date.
- Added README / CHANGELOG / WORKLOG management-rule section to `.kiro/AGENTS.md`. The meaning of existing work rules was not changed.
- Same-date restructuring of `.kiro/specs/_common/operator-decisions.md` (Korean Status labels, At a Glance, Open/Tentative/Deferred collections) was a readability improvement that did not change decision values; detailed changes are recorded in the 2026-06-05 entry of `.kiro/CHANGELOG.md`.
- No actual AWS resources created.
- No application source-code changes.
- No changes to the 8 MS documents (README · CHANGELOG · worklog · AGENTS.md · source).
- No sensitive information recorded.
