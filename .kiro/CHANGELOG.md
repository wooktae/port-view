# Kiro AWS Migration Changelog

This document briefly records only the change history of AWS Migration spec documents within the `.kiro` workspace.

## Writing Principles

- Record changes to the spec structure, root common documents, creation of new specs, and major document restructuring.
- For operational code changes, summarize only the results required for document consistency, and separate detailed implementation history into `WORKLOG.md` or the relevant `operation-notes.md`.
- Organize change results in a two-column `항목 / 값` table wherever possible.
- Split long cells across multiple rows, and do not record raw logs, full SQL, or full AWS responses.
- Standardize item categories as `Added`, `Changed`, `Removed`, and `Security`.
- Use the work date in Korea as the date.

## 2026-07-22 (Paper Daily normal automated run acceptance · first stabilization complete · P2 View enhancement out of scope)

### Changed

- 🟢 Reflected completion of end-to-end acceptance for a normal automated Paper Daily run (operator directly confirmed · Kiro documentation changes only)

  | Item | Value |
  | --- | --- |
  | Step 1~11 | Normal Scheduler automated execution SUCCEEDED (08:00 → 08:21 KST) |
  | Step 12~17 | Normal Scheduler automated execution SUCCEEDED (09:01 → 09:06 KST) |
  | Crawler validation | Program latest trading date 2026-07-21: 1 row · Shortsell: 349 rows |
  | Daily Run | 2026-07-22 COMPLETED · reference date 2026-07-21 · AGGRESSIVE · 1 candidate |
  | Execution Plan | READY · 1 order-ready target · 0 blocked/skipped |
  | validator | READY Plan → Order · Order Chain passed automatically |
  | Order · Fill | 한국전력 83 shares BUY · Execution Order · Request FILLED · 1 Fill of 83 shares |
  | Position · Balance | Position 83 shares OPEN · Balance Snapshot created |
  | Slack · OPS | Success Slack received automatically · Workflow/Batch success records confirmed |

- 🟢 Marked P1 complete · declared first-stage Paper Daily stabilization complete

  | Item | Value |
  | --- | --- |
  | P1 follow-up | Marked observation of the next normal automated end-to-end run complete |
  | Declaration | First-stage Paper Daily stabilization complete |
  | Do not overinterpret | Does not mean aws-live readiness, long-term fault-free operation, or completion of the entire AWS Migration |
  | Failure history | Preserve 2026-07-20 · 2026-07-21 FAILED history (do not overwrite with success) |

- 🟢 Reflected decision to exclude P2 View operational security/display enhancements from scope

  | Item | Value |
  | --- | --- |
  | P2 status | Not performed (intentional scope exclusion · not feature removal or AWS resource deletion) |
  | Targets | ALB · HTTPS · Route53 · authentication · Auto Scaling · Blue/Green · UI enhancement · external exposure |
  | Current scope | Maintain first-stage ECS Fargate proof state |
  | Revisit condition | When external exposure or multi-user operation becomes necessary |
  | Actual AWS resource changes | None |

- 🟢 Reflected updates in `_common` · README documentation

  | Item | Value |
  | --- | --- |
  | README | Paper Daily Step 1~11 · Step 12~17 · port-view row · latest validation date 2026-07-22 |
  | followups-overview | Now P1 → Done recently 2026-07-22 · removed active Next/Later P2 items and moved them to the decision table |
  | operator-decisions | Reflected OD-SAFE-001 · OD-MS-002 · no new Decision ID · no Dashboard count change |
  | ms-aws-service-decision-matrix | port_strategy_execution · port-view operational notes · no change to service selection conclusions |
  | aws-resource-glossary | Added operational proof note dated 2026-07-22 · no change to 5-field template |
  | risk-register | Review only · no status/count change · no new Risk ID |
  | New Risk ID · Decision ID | None |

### Security

| Item | Result |
| --- | --- |
| AWS · DB · broker · KIS · Slack · ECS · EC2 · Lambda · Step Functions execution | 0 Kiro executions · performed directly by operator |
| Newly recorded raw State Machine ARN · execution ARN · ECS Task ARN · account-id · account number · broker order number · SHA256 | 0 |
| git add · commit · push · reset · restore | 0 |
| Placeholder policy | Use only the `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_BROKER_ORDER_NO]` families |
| Storage encoding | UTF-8 No BOM maintained |

## 2026-07-21 (Final consistency supplement for common AWS Migration documents)

### Changed

- 🟢 Aligned SSM Endpoint documentation with the actual operating structure (documentation consistency only · not an actual AWS resource change)

  | Item | Value |
  | --- | --- |
  | operator-decisions.md | OD-NET-005 baseline set (S3 GW + ECR api/dkr + Secrets + Logs) + optional SSM |
  | operator-decisions.md | OD-SEC-007 SSM outbound path aligned to Public or Endpoint |
  | risk-register.md | Aligned R-NET-003 risk/mitigation to access-path/Endpoint-set mismatch |
  | aws-resource-glossary.md | Distinguished VPC Endpoint Gateway/Interface · SSM optional |
  | ms-aws-service-decision-matrix.md | Separated Network and Data VPC Endpoint roles |
  | New Risk ID · Decision ID | None · status counts preserved |

- 🟢 Standardized expression of Crawler current operation / target architecture

  | Item | Value |
  | --- | --- |
  | operator-decisions.md | Distinguished OD-MS-003 target perspective · OD-MS-011 current Hybrid perspective |
  | ms-aws-service-decision-matrix.md | Split Crawler card into current operation and long-term target rows |

- 🟢 Updated follow-up orchestration wording

  | Item | Value |
  | --- | --- |
  | followups-overview.md | Updated Next Daily orchestration to built/ENABLED complete + failure-propagation aligned |

### Removed

  | Item | Value |
  | --- | --- |
  | operator-decisions.md | Removed dead `See details` references from OD-MS-027~040 |
  | followups-overview.md | Removed leftover Historical Notes references/rows |

### Security

  | Item | Value |
  | --- | --- |
  | Actual AWS · DB · broker · KIS · Slack execution | 0 |
  | Newly recorded raw sensitive information | 0 |
  | git command execution | 0 |

## 2026-07-21 (Validator failure on normal no-order run · `--allow-no-target` supplement)

### Changed

- 🟢 Fixed `P0_ValidateOrderChain` handling for a normal no-order run (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | Background | The 2026-07-21 09:01 automated execution passed Step 12~16 and then FAILED at the final `P0_ValidateOrderChain` because of a missing setting that treated a normal no-order run with 0 actual orders as `NO_EXECUTION_ORDER_TARGET` failure |
  | State Machine ECS command | Added `--allow-no-target` |
  | Python source | No change |
  | ECS image | No change |
  | Task Definition | No change |
  | ASL validation | validate-state-machine-definition OK · re-query after deployment confirmed |
  | validator standalone smoke | `ORDER_CHAIN_VALIDATION=SUCCESS target=0 errors=0` · ExitCode 0 |
  | Full automated execution rerun | None |
  | 09:01 automated execution history | FAILED preserved |
  | New Risk ID · Decision ID | None |

- 🟢 Briefly supplemented `_common` document Details / Evidence

  | Item | Value |
  | --- | --- |
  | risk-register.md | Supplemented R-AUTO-001 · R-AUTO-037 Mitigation history for 2026-07-21 · no new Risk ID · no Dashboard count change |
  | operator-decisions.md | Added 2026-07-21 operational notes to OD-SAFE-004 · OD-MS-032 Details · no new Decision ID · no Status count change |
  | followups-overview.md | Updated Now P1 · updated `NO_TARGET` distinction item · added Done recently 2026-07-21 item |
  | ms-aws-service-decision-matrix.md | 4.7 port_strategy_execution operational note · no change to service selection conclusion |
  | aws-resource-glossary.md | Added Usage Notes 2026-07-21 · no change to 5-field template |
  | README.md | Added Current Status Dashboard Paper Daily Step 12-17 row · latest validation date 2026-07-21 · footnote |

- 🟢 Strengthened P3 KRX Crawler failure propagation · added runner DB validator (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | Background | Windows Scheduled Task `Portfolio-KRX-Worker-Daily` runs `C:\portfolio\run_krx_worker_daily.ps1` · `interest_program.py` and `interest_shortsell.py` could end with exit 0 even on internal failure results, creating a false-success path (R-DATA-017) |
  | Python exit-code propagation | `interest_program.py` · `interest_shortsell.py` — exit 0 only for `SUCCESS` or normal `NO_CHANGE` with `error_count=0` · exit 1 for `FAILED`, partial errors, or unknown states · added `PROCESS_EXIT_DECISION` log (only these two files changed · login/Chrome helper files unchanged) |
  | runner DB validator | Added after `run_krx_worker_daily.ps1` · based on expected trade date, require `interest_program_raw` latest trading date match + exactly 1 row · `interest_shortsell_raw` latest trading date match + at least 300 rows · execution order KRX login → program → shortsell → Validate crawler DB → DONE |
  | Failure propagation | On validation failure, runner non-zero → SSM → Step Functions failure propagation |
  | Validation | Local import/exit-code unit tests passed · deployed after backing up existing files · local/remote SHA256 matched · production venv py_compile · remote PowerShell parse passed · standalone validator run expected trade date 2026-07-20 · Program 1 row · Shortsell 349 rows · `CRAWLER_DB_VALIDATION=SUCCESS` · ExitCode 0 |
  | Actual rerun | No crawler recollection or full State Machine rerun |
  | Risk / Decision change | R-DATA-017 promoted `Open` → `Mitigated` · R-AUTO-020 remains `Mitigated` · supplemented OD-MS-026 Details · no new Risk ID or Decision ID |

### Security

| Item | Result |
| --- | --- |
| AWS · DB · broker · KIS · Slack · ECS · EC2 · Lambda · Step Functions execution | 0 Kiro executions · performed directly by operator |
| Newly recorded raw State Machine ARN · execution ARN · ECS Task ARN · account-id · broker order number · SHA256 | 0 |
| git add · commit · push · reset · restore | 0 |
| Placeholder policy | Use only the `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_ACCOUNT_NO]` families |
| Storage encoding | UTF-8 No BOM maintained |

## 2026-07-20 (Step 13 EGW00201 recovery · rate-limit supplement · Step 14~17 manual completion · State Machine failure Slack and runDate alignment)

### Changed

- 🔴 Step 12~17 automated execution Step 13 incident and manual recovery (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | State Machine | `portfolio-paper-daily-step12-17-approval` |
  | Failure point | Step 13 (order/fill lookup) · 09:01 automated execution history remains FAILED |
  | Step 12 order submission | Normal · 2 sell orders accepted by broker · first order fully filled |
  | Second-order lookup | KIS `EGW00201` (per-second transaction count exceeded) |
  | Recovery | Did not rerun Step 12 (duplicate prevention) · manually ran Step 13 so second order was fully filled for 13 shares as `FILLED` · both orders `FILLED` |
  | Step 14~17 | Executed manually in sequence without temporary State Machine · Step 14·15·16 ECS ExitCode 0 · Step 17 balance API 200 · snapshot stored |
  | Success Slack | Not an automated result · after manual recovery, manually invoked `portfolio-event-notifier` (`DAILY_EXECUTION_SUCCESS` · runDate=2026-07-20) · StatusCode 200 · actually received |

- 🟢 MarketConnector Step 13 rate-limit supplement (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | Target file | Full replacement of `connector_order_check.py` · deployed to EC2 via S3 |
  | Version | connector-order-check-2.0.1 → 2.0.2 |
  | Change | Added 5-second delay between order lookups · max 2 retries only for `EGW00201` (first 1.5 sec · second 5 sec) |
  | Scope | Lookup API rate-limit retries · not broker order-submission retries · not applied to other error codes |
  | Static validation | Original backed up · SHA validation · Python compile · AST validation passed |

- 🟢 Made State Machine runDate dynamic · fixed failure Slack path (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | Step1_SendConnectorBalanceCommand | Removed hard-coded `--run-date 2026-06-22` · dynamically passes `$.runDate` (States.Array · States.Format) |
  | Result-failure bypass | Issue where Choice Default went directly to Fail State when Task exited normally but result was abnormal, bypassing failure Slack |
  | Fix | Changed Step13·14·15·16·Step1 Default to failure-context Pass State · store `$.dailyExecutionFailure`, then failure Slack · OPS failure record · final status remains FAILED |
  | Step12_Failed | Excluded from this change scope |
  | Validation | ASL validation OK · re-query after deployment confirmed |

- 🟢 Automated display of actual fill details in Daily execution success Slack (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | New Builder Lambda | `portfolio-daily-execution-slack-summary-builder` · Python 3.12 |
  | Builder role | Query `connector.connector_fill` · `reference.stock_master` · `view_app` · generate success Slack payload |
  | Notifier change | `portfolio-event-notifier` `DAILY_EXECUTION_SUCCESS` formatter displays buy/sell fill lists · if none, `- 없음` |
  | State Machine | Added `BuildDailyExecutionSuccessSlackSummary` State · success path Builder → Notifier → `RecordWorkflowStepSuccess` |
  | IAM | Dedicated minimum-permission Builder execution Role · `lambda:InvokeFunction` inline permission on State Machine Role (`portfolio-daily-execution-slack-builder-invoke`) |
  | Manual smoke | 2026-07-20 data · 0 buys · sells 엔씨소프트 13 shares · 코오롱생명과학 78 shares · Slack actually received |
  | Automated execution distinction | Full execution not started · 09:01 automated execution remains Step 13 `EGW00201` FAILED |

- 🟢 P0 operational safety strengthening (performed after incident recovery · directly by operator)

  | Item | Value |
  | --- | --- |
  | Step 13 bounded polling | `connector_order_check.py` 2.0.2 → 2.0.3 · up to 3 additional polls for active orders (ACCEPTED · SUBMITTED · PENDING · PARTIAL_FILLED · PARTIALLY_FILLED), default 10 seconds · exit 1 if still active at end · repeated lookup API queries, not order-submission retries · deployed to EC2 through temporary S3 object then deleted |
  | Step14~16 fail-closed | Step14 SELL Fill · Step15 BUY Fill target/processed-count validation · Step16 BUY Position link/quantity validation · rollback then exit 1 on missing/mismatch · commit only if everything matches |
  | Execution Task Definition | Built new immutable tag `paper-20260720-p0-integrity-v1` (did not overwrite existing tag) · `portfolio-paper-strategy-execution` revision 3 · connected Step14·15·16 · ECS Fargate smoke passed |
  | State Machine validator integration | Connected `execution_validate_ready_plan_order.py` (immediately before Step12) · `execution_validate_order_chain.py` (immediately after Step16) read-only validation · failure routes to failure Slack/OPS failure path |
  | Step12 failure Slack path | Removed direct Fail from `Step12_Failed` · save `dailyExecutionFailure` then `SendDailyExecutionFailedSlack` → `RecordBatchFailure` → final Fail · ASL validation OK |
  | Failure formatter smoke | Invoked Notifier directly with `DAILY_EXECUTION_FAILED` test event without a real order failure · confirmed failure-item display · Portfolio Daily Bot actually received |
  | Completed scope · incomplete | P0 implementation/production deployment/standalone smoke/State Machine integration complete · not the next normal automated-run success · full automated-run acceptance test not performed (P1 remains) · 09:01 automated execution FAILED preserved · no new Risk / Decision ID |

- 🟢 Briefly supplemented `_common` document Details / Evidence

  | Item | Value |
  | --- | --- |
  | risk-register.md | Supplemented R-AUTO-001 · R-AUTO-023 · R-AUTO-037 · R-AUTO-038 · R-BROKER-004 Mitigation history for 2026-07-20 · no new Risk ID · no Dashboard count change |
  | operator-decisions.md | Added brief operational notes to OD-SAFE-001 · OD-SAFE-004 · OD-MS-009 · OD-MS-030 · OD-MS-031 · OD-MS-032 Details · no new Decision ID · no Status count change |
  | followups-overview.md | Added Done recently 2026-07-20 item · updated Now |
  | ms-aws-service-decision-matrix.md | 4.1 port-marketconnector · 4.7 port_strategy_execution operational notes · no change to service selection conclusion |
  | aws-resource-glossary.md | Added Usage Notes 2026-07-20 · no change to 5-field template |
  | README.md | Current Status Dashboard Paper Daily Step 12-17 row · latest validation date 2026-07-20 |

### Added

- 🟢 Added 2 new read-only validators (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | `execution_validate_ready_plan_order.py` | Compare READY state of latest Execution Plan by plan_date and `ready_order_count` against actual PAPER_STRATEGY Order count · exit 1 on missing/count mismatch/wrong quantity or ticker · validated successfully against real data for 2026-07-20 Plan 143 · 2 Orders · connected immediately before Step12 |
  | `execution_validate_order_chain.py` | Read-only Plan→Order→Request→Fill→Position chain validation · exit 1 on mismatch · successfully validated real data for 2 SELLs on 2026-07-20 · connected immediately after Step16 |

### Security

| Item | Result |
| --- | --- |
| AWS · DB · broker · KIS · Slack · SSM · ECS · EC2 · Lambda · Step Functions execution | 0 Kiro executions · performed directly by operator |
| Newly recorded raw broker order number · execution ARN · State Machine ARN · SSM Command ID · ECS Task ARN · account-id · SHA256 | 0 |
| git add · commit · push · reset · restore · checkout · stash | 0 |
| Placeholder policy | Use only the `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_BROKER_ORDER_NO]` · `[REDACTED_ACCOUNT_NO]` families |
| Storage encoding | UTF-8 No BOM maintained |

## 2026-07-16 (daily-buy-e2e-first-run · Step 12 wait time 60 seconds)

### Changed

- 🟢 Reflected first E2E proof of real Daily buy · fill · Fill · Position (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | daily_run_id | 76 |
  | run_date · data_date | 2026-07-16 · 2026-07-15 |
  | market_signal | AGGRESSIVE |
  | BUY candidates | 엔씨소프트 036570 · 코오롱생명과학 102940 |
  | Order result | Market buys of 13 shares · 78 shares · fully filled |
  | New Position | ID 13(엔씨소프트) · ID 14(코오롱생명과학) · both `OPEN` |
  | Related decisions · risks | OD-MS-032 · OD-SAFE-001 · R-AUTO-037 · R-AUTO-038 · R-BROKER-004 |

- 🟠 Initial order/fill lookup issue and manual recovery

  | Item | Value |
  | --- | --- |
  | Initial Step 13 lookup time | About 10 seconds after order submission |
  | Initial lookup result | 엔씨소프트 9 shares partially filled · 코오롱생명과학 in accepted state |
  | Actual market orders | Continued filling afterward · only internal status remained stuck at first lookup result |
  | Full Step Functions | Treated as SUCCESS based on ExitCode 0 |
  | Recovery sequence | Reran Step 13 → Step 15 → Step 16 |
  | Final data consistency | Confirmed consistency among Order Request · Fill · Execution Order · Position |

- 🟢 Changed fill-lookup wait time after Step 12 order submission (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | Target State Machine | `portfolio-paper-daily-step12-17-approval` |
  | Target Wait State | `Step12_WaitBeforeCheck` |
  | Before · after | `Seconds=10` → `Seconds=60` |
  | Next State | `Step12_GetCommandInvocation` unchanged |
  | Validation | Passed after State Machine update and re-query |
  | Raw actual ARN · revision ID | Not recorded in document |

- 🟢 Briefly supplemented `_common/risk-register.md` Mitigation history evidence

  | Item | Value |
  | --- | --- |
  | R-AUTO-037 | Remains Mitigated · first real-run proof + polling · processed-count failure propagation · OPS Mirror expansion remains |
  | R-AUTO-038 | Remains Mitigated · reconfirmed need to expand OPS Mirror detailed Steps |
  | R-BROKER-004 | Remains Mitigated · broker duplicate-order count remains 0 |
  | New Risk ID | None |
  | Risk Dashboard count | No change |

- 🟢 Brief operational notes in `_common/operator-decisions.md` Details

  | Item | Value |
  | --- | --- |
  | Target Decision ID (1) | OD-MS-032 (first real-buy proof on Dispatcher · Scheduler · State Machine chain) |
  | Target Decision ID (2) | OD-SAFE-001 (first proof of paper automated BUY/SELL line from initial block → allowed after validation) |
  | Status change | None (existing values preserved) |
  | New Decision ID | None |
  | Decision Summary count | No change |

- 🟢 Moved `_common/followups-overview.md` Now → Done recently

  | Item | Value |
  | --- | --- |
  | Moved item | First proof of automatic order submission · fills · balance refresh · Slack end-to-end on a day with candidates |
  | Completion date | 2026-07-16 |
  | New Now items | ACCEPTED / PARTIAL_FILLED polling · Step 13·15·16 processed-count failure propagation · Workflow FAIL on Execution Order · Fill · Position mismatch · integration with 10-minute balance snapshot |
  | Now items retained | Strengthen order validation chain · distinguish `NO_TARGET` success · ExitCode / SF failure propagation · expand OPS Mirror detailed Steps |

- 🟢 Brief operational note in `_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Target section | 4.7 `port_strategy_execution` |
  | Service selection conclusion | No change (1st choice · 2nd choice · not-recommended strings preserved) |
  | Additional fact | Lookup 10 seconds after order submission was earlier than paper-trading fill reflection · supplemented with 60-second Wait · do not judge consistency success by ExitCode 0 alone · polling and consistency-validation follow-ups remain |

- 🟢 Added `_common/aws-resource-glossary.md` Usage Notes for 2026-07-16

  | Item | Value |
  | --- | --- |
  | Target terms | Step Functions · Wait State |
  | 5-field template | No change |
  | Additional fact | Post-order Wait 10 sec → 60 sec · longer Wait mitigates API reflection delay · not a replacement for polling |

- 🟢 Updated `README.md` Current Status Dashboard row

  | Item | Value |
  | --- | --- |
  | Paper Daily Step 12-17 | Automated ENABLED · 2026-07-16 real BUY · fill · Fill · Position E2E complete · Wait 60 sec · latest validation date 2026-07-16 |
  | Paper Daily Step 1-11 | 2026-07-16 `daily_run_id=76` recovery run · reflected creation of 2 BUY candidates · latest validation date 2026-07-16 |
  | Other Dashboard rows | No change |

### Security

- 🟢 Checked Kiro execution · newly recorded raw sensitive information

  | Item | Value |
  | --- | --- |
  | AWS · DB · broker · KIS · Slack · Scheduler · Step Functions · ECS · EC2 commands | 0 Kiro executions |
  | git add · commit · push | 0 |
  | Newly recorded raw broker order number · executionArn · ARN · account-id · secret · webhook URL · payload · account number | 0 |
  | Placeholder policy | Use only the `[REDACTED_BROKER_ORDER_NO]` · `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_ACCOUNT_NO]` families |
  | Storage encoding | UTF-8 No BOM maintained |


## 2026-07-15 (daily-brief-slack-recovery · transition to shared Dispatcher Holiday Guard)

### Changed

- 🟢 Reflected transition to a shared Holiday Guard structure in the Daily Scheduler Dispatcher (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | Dispatcher-supported eventType | Added `MORNING_BRIEF` · `EVENING_BRIEF` |
  | Dispatcher environment variables | Added Daily Brief State Machine ARN (raw value not recorded) |
  | Dispatcher IAM | Added Daily Brief State Machine `states:StartExecution` permission · Resource-scoped · no wildcard |
  | Integrated result | 08:00 Step 1~11 · 09:01 Step 12~17 · 07:50 pre-market · 15:50 post-market all use the same Dispatcher · same Holiday Guard |
  | Related decisions · risks | OD-MS-032 · OD-MS-038 · R-AUTO-035 |

- 🟢 Reflected normalization of Daily Brief Builder Lambda

  | Item | Value |
  | --- | --- |
  | Internal Holiday Guard | Environment-variable-controlled approach applied · disabled in production environment |
  | Builder responsibility | Retains only message generation · DB query responsibilities |
  | Normal response fields | Added `skipped=false` · `skipReason=null` |
  | Dependency packaging | Includes `pg8000` and related dependencies |
  | Initial failure cause | No `skipped` field in normal response → `CheckHolidaySkip` Choice `States.Runtime` |

- 🟢 Switched pre-market · post-market Scheduler Targets

  | Item | Value |
  | --- | --- |
  | Pre-market `portfolio-daily-brief-morning-slack-0750-kst` | Target changed from direct Daily Brief State Machine invocation → Dispatcher Lambda invocation |
  | Post-market `portfolio-daily-brief-evening-slack-1550-kst` | Target changed from direct Daily Brief State Machine invocation → Dispatcher Lambda invocation |
  | Scheduler time · status | Existing times preserved · ENABLED preserved |
  | Timezone · Flexible · cron | Asia/Seoul · OFF · MON-FRI preserved |
  | EC2 stop Scheduler 15:50 | No change · remains executed regardless of holidays |

- 🟢 2026-07-15 validation results

  | Item | Value |
  | --- | --- |
  | Existing Dispatcher Step 1~11 · Step 12~17 paths | Normal |
  | Dispatcher pre-market · post-market Daily Brief paths | Normal |
  | Weekend actual-execution-mode smoke | Confirmed Dispatcher Holiday Guard block |
  | Builder DB query | Normal |
  | 15:50 automated post-market run | Initially failed due to `CheckHolidaySkip` error |
  | 16:02 post-market real smoke | Step Functions `SUCCEEDED` · Notifier `statusCode=200` · Slack actually received |

- 🟢 Briefly supplemented `_common/risk-register.md` R-AUTO-035 Mitigation history evidence

  | Item | Value |
  | --- | --- |
  | Target Risk ID | R-AUTO-035 |
  | Status change | None (`Mitigated` remains) |
  | New Risk ID | None |
  | Additional evidence | Shared Dispatcher Holiday Guard · normal Builder skip fields · `pg8000` included · actual post-market Slack received |

- 🟢 Moved `_common/followups-overview.md` Now → Done recently

  | Item | Value |
  | --- | --- |
  | Moved item | Confirm 15:50 automated post-market Slack reception |
  | Completion date | 2026-07-15 |
  | Now item retained | Confirm 07:50 automated pre-market Slack reception |
  | Follow-up items retained | Reconfirm per-ticker display when positions exist · Holiday API fallback policy |

- 🟢 Brief operational notes in `_common/operator-decisions.md` Details

  | Item | Value |
  | --- | --- |
  | Target Decision ID (1) | OD-MS-032 (shared Dispatcher Holiday Guard integration · consistency across 4 paths) |
  | Target Decision ID (2) | OD-MS-038 (retain Builder responsibility · normal skip fields · `pg8000` packaging) |
  | Status change | None (existing values preserved) |
  | New Decision ID | None |
  | Decision Summary count | No change |

- 🟢 Updated `README.md` Current Status Dashboard row

  | Item | Value |
  | --- | --- |
  | Daily Brief Slack status | Automated ENABLED + shared Dispatcher Holiday Guard integrated + actual post-market reception confirmed |
  | Latest validation date | 2026-07-08 → 2026-07-15 |
  | Other rows | No change |

- 🟢 Reflected unification of Daily execution business date to KST (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | Principle | Keep DB timestamp storage in UTC · standardize business-date determination on Asia/Seoul |
  | Daily BUY / SELL execution ECS Task Definition | Added `TZ=Asia/Seoul` |
  | Market EC2 server timezone | Changed to Korea Standard Time |
  | Step Functions execution path | Connected new Task Definition revision |
  | Related decisions · risks | OD-MS-040 · R-DATA-010 · R-DATA-017 |
  | Recurrence in next automated run | Continue real-run observation (not complete) |

- 🟢 Aligned Approval Slack query criteria to a single `daily_run_id`

  | Item | Value |
  | --- | --- |
  | Query criterion | Fix one `daily_run_id`, then use the same Run for status · reference date · signal · candidates |
  | Independent latest Plan · Run queries | Removed |
  | Order query | Orders linked by `source_daily_run_id` only |
  | Plan query | Only the Plan referenced by that Order · if absent, `latestPlanId=null` |
  | Related decision | OD-MS-031 |

- 🟢 Improved Approval Slack display of candidate names and 5 scores

  | Item | Value |
  | --- | --- |
  | Stock name | Use `company_name` or `ticker_code` |
  | Score fields | Structured by Builder · Notifier displays to three decimal places |
  | Korean labels | Overall · Investor Flow · Information · Trend · Short Selling |
  | Raw `buy_info` dict exposure in Slack | Removed |

- 🟢 Builder · Notifier Lambda deployment and actual Slack E2E success

  | Item | Value |
  | --- | --- |
  | `portfolio-approval-slack-summary-builder` | `Active` · `LastUpdateStatus=Successful` · Runtime · Handler unchanged |
  | `portfolio-event-notifier` | `Active` · `LastUpdateStatus=Successful` · Runtime · Handler unchanged |
  | Previous-version rollback ZIP | Secured |
  | Builder DB query status · reference date · signal · candidates | AGGRESSIVE · 2026-07-10 · 4 · 4 |
  | Candidate stocks | DL · 대주전자재료 · 삼성SDI · 한국피아이엠 |
  | `latestPlanId` | null (confirmed no arbitrary historical Plan use) |
  | Builder → Notifier → Slack message-display E2E | Complete (not actual automated order-fill E2E) |

- 🟢 Briefly supplemented `_common/risk-register.md` R-DATA-010 · R-DATA-017 Mitigation history evidence

  | Item | Value |
  | --- | --- |
  | Target Risk ID | R-DATA-010 · R-DATA-017 |
  | Status change | None (R-DATA-010 remains `Mitigated` · R-DATA-017 remains `Open`) |
  | New Risk ID | None |
  | Risk Dashboard count | No change |
  | Additional evidence | Daily BUY execution-container TZ misjudgment path · NO_TARGET / ExitCode 0 false-success path |

- 🟢 Brief operational notes in `_common/operator-decisions.md` Details

  | Item | Value |
  | --- | --- |
  | Target Decision ID | OD-MS-031 · OD-MS-040 |
  | Status change | None |
  | New Decision ID | None |
  | Decision Summary count | No change |

- 🟢 Split `_common/followups-overview.md` Now · moved item to Done recently

  | Item | Value |
  | --- | --- |
  | Split item | `APPROVAL_REQUIRED` Slack Builder improvement — Builder → Notifier → Slack E2E `Done recently 2026-07-15` · automated Scheduler real-run reception remains `Now` |
  | New Now follow-ups | Observe next-business-day KST automated run · fail if Plan / Order is not created · distinguish `NO_TARGET` success · ExitCode / SF failure propagation · strengthen order validation chain |

- 🟢 Minimal reflection in `README.md` Current Status Dashboard

  | Item | Value |
  | --- | --- |
  | Paper Daily Step 1-11 row | Briefly reflected completion of KST date basis for Daily execution · latest validation date 2026-07-15 |
  | Paper Daily Step 12-17 row | Briefly reflected Approval Slack data consistency · candidate-display E2E completion · latest validation date 2026-07-15 |
  | Next automated-run real observation | Retained as follow-up |
  | Other rows | No change |

### Security

| Item | Result |
| --- | --- |
| AWS · Lambda · Step Functions · Scheduler · IAM · DB · Slack · ECS · EC2 execution | 0 Kiro executions · performed directly by operator |
| broker · KIS · crawler · automated buy · automated sell · fill sync · position sync · intraday monitor execution | 0 |
| Newly recorded raw secret · password · token · webhook URL · account number · actual ARN · executionArn · RequestId · SHA256 · Slack payload | 0 |
| Newly recorded full Lambda code · IAM Policy · Step Functions history · PowerShell output · SSM response · raw SQL output | 0 |
| git add · commit · push execution | 0 |
| Placeholder policy | Use only the `[REDACTED_ARN]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_LAMBDA_ARN]` families |

## 2026-07-09 (krx-crawler-ec2-timezone-fix-and-recollect)

### Changed

- 🟢 Reflected completed KRX CRAWLER Windows EC2 timezone fix and recollection (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | Target server | KRX CRAWLER Windows EC2 |
  | Previous timezone | UTC |
  | New timezone | Korea Standard Time |
  | Validation tools | Get-Date · Get-TimeZone · Python datetime |
  | KRX Scheduled Task | Manually ran Portfolio-KRX-Worker-Daily |
  | `interest_program_raw` latest_date | 2026-07-08 (rows 1) |
  | `interest_shortsell_raw` latest_date | 2026-07-08 (rows 349) |
  | program `created_at` (KST display) | 2026-07-09 11:23:33 |
  | shortsell `created_at` (KST display) | 2026-07-09 11:24:08 |
  | DB session timezone | Recommend keeping UTC · convert for display using `at time zone 'Asia/Seoul'` |

- 🟢 Promoted `_common/risk-register.md` R-DATA-010 Status

  | Item | Value |
  | --- | --- |
  | Target Risk ID | R-DATA-010 |
  | Status change | 🔴 Open → 🟢 Mitigated |
  | Evidence | 2026-07-08 ECS Fargate TZ patch + 2026-07-09 CRAWLER Windows EC2 KST change + KRX recollection confirmed recovery of raw-data freshness |
  | Cause-path distinction | ECS Fargate UTC · CRAWLER Windows EC2 UTC documented as separate paths |
  | Detection retained regularly | `interest_*_raw` `MAX(trade_date)` vs previous trading day SQL · verify preprocessor feature date |

- 🔴 `_common/risk-register.md` R-DATA-017 remains Open · false-success supplement

  | Item | Value |
  | --- | --- |
  | Target Risk ID | R-DATA-017 |
  | Status change | None (`Open` remains) |
  | Remaining issue | `interest_program.py` / `interest_shortsell.py` `[Error]` + exit 0 · `run_krx_worker_daily.ps1` misjudgment · LastTaskResult 0 · SSM ResponseCode 0 · Step Functions SUCCEEDED ≠ DB raw freshness |
  | Follow-up | Continue strengthening Step2B success criteria |

- 🟠 Moved `_common/followups-overview.md` Now → Done recently

  | Item | Value |
  | --- | --- |
  | Moved item | Observe 2026-07-09 08:00 KST Daily Step1~11 automated run |
  | Reason for move | Freshness recovery confirmed (KRX program / shortsell 2026-07-08 recollection complete) |
  | Completion date | 2026-07-09 |

- 🟠 Added OD-MS-040 Details note in `_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Target Decision ID | OD-MS-040 |
  | Status change | None (🟢 확정 retained) |
  | Additional note | CRAWLER Windows EC2 also runs in KST timezone for KRX GUI target-date calculation consistency · confirmed recovery of KRX recollection freshness after 2026-07-09 UTC → KST change |
  | New Decision ID | None (extension within existing OD-MS-040 scope) |

- 🟠 Added KST note to section 4.3 port-interest-crawler in `_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Target section | 4.3 port-interest-crawler |
  | Service selection conclusion | No change (Windows EC2 worker 1st choice · ECS Fargate 1st choice preserved) |
  | Additional note | Windows EC2 worker needs KST timezone or timezone-aware code for KRX GUI collection target-date calculation consistency |

### Added

- 🟠 Registered new follow-ups · `_common/followups-overview.md` Next

  | Item | Value |
  | --- | --- |
  | New follow-up (1) | Exit 1 when `run_krx_worker_daily.ps1` detects `[Error]` |
  | New follow-up (2) | `sys.exit(1)` when `interest_program.py` / `interest_shortsell.py` fail |
  | New follow-up (3) | Add DB validation after Step2B (program >= 1 · shortsell = 349 based on expected trade_date) |
  | On validation failure | Process as Step Functions Fail |
  | Related specs | 04, 08 |

### Security

| Item | Result |
| --- | --- |
| AWS · DB · psql · Spring Boot · Lambda · Step Functions · ECS · SSM · EC2 · IAM · RDS execution | 0 Kiro executions · performed directly by operator |
| broker · KIS · crawler · automated buy · automated sell · fill sync · position sync · intraday monitor execution | 0 |
| Newly recorded raw secret · password · token · webhook URL | 0 |
| Newly recorded raw accountNo · account-id · actual ARN · public IP · broker_order_no · CommandId · executionArn · instance-id | 0 |
| Newly recorded full raw SQL output · full CloudWatch logs · full Step Functions history | 0 |
| git add · commit · push execution | 0 |
| Placeholder policy | Use only the `[REDACTED_ARN]` · `[REDACTED_ACCOUNT]` · `[REDACTED_INSTANCE_ID]` · `[REDACTED_COMMAND_ID]` families |

## 2026-07-08 (view-daily-batch-ops-mirror-ui-consumption-confirmed)

### Changed

- 🟢 Reflected completion of View Daily Batch / OPS Mirror UI consumption verification

  | Item | Value |
  | --- | --- |
  | Target role | `view_app` |
  | `ops` schema USAGE check | Complete |
  | `ops.strategy_daily_batch_run` SELECT | Confirmed |
  | `ops.strategy_daily_batch_step_log` SELECT | Confirmed |
  | `AWS_STEPFUNCTIONS` run-history storage | Confirmed |
  | Representative run history | #61 Step 12~17 · #60 Step 1~11 |
  | Representative step_log | #61 `SFN_STEP12_17_WORKFLOW` / SUCCESS · #60 `APPROVAL_BLOCKED` / SKIPPED |
  | `/daily-batch` execution-mode display | aws-stepfunctions |
  | Selected-run detail · step-log rendering | Confirmed (#61) |
  | AWS Step 1~11 start button · AWS Step 12~17 approval-execution button | Display confirmed |
  | Local File execution mode | OFF |
  | aws-stepfunctions execution mode | ON |
  | Related decisions · risks | OD-DB-012 · OD-MS-039 · R-AUTO-038 |

- 🟢 Marked existing follow-up "Confirm that the View Daily Batch screen actually renders `AWS_STEPFUNCTIONS` run history" complete

  | Item | Value |
  | --- | --- |
  | Target document | `_common/followups-overview.md` |
  | Moved from Now | Done recently (2026-07-08) |
  | Status change | None (planned → complete) |

- 🟢 Briefly supplemented `_common/risk-register.md` R-AUTO-038 Mitigation history evidence

  | Item | Value |
  | --- | --- |
  | Target Risk ID | R-AUTO-038 |
  | Status change | None (`Mitigated` remains) |
  | Additional evidence | Confirmed `/daily-batch` UI consumption · run-level + representative workflow-step screen rendering |
  | Expansion to all detailed step mirrors | Retained as follow-up |

- 🟢 Added 2026-07-08 UI consumption note to OD-MS-039 Details in `_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Target Decision ID | OD-MS-039 |
  | Status change | None (🟢 확정 retained) |
  | First-scope decision retained | Not a mirror of all detailed steps · run-level + representative workflow step |
  | Additional note | 2026-07-08 UI consumption confirmation complete (view_app permissions · `/daily-batch` screen rendering) |

### Added

- 🟠 Registered new follow-up for redaction of `/daily-batch` display fields

  | Item | Value |
  | --- | --- |
  | Review display redaction for `requestPayload` / `resultPayload` | Newly registered |
  | Review masking of raw `accountNo` | Newly registered |
  | Review redaction of raw `executionArn` · `stateMachineArn` | Newly registered |
  | Review enhancement from raw UTC `timestamp` → KST display | Newly registered |
  | Review operator-friendly improvement of #60-series `APPROVAL_BLOCKED` / SKIPPED wording | Newly registered |
  | Target spec | 05 |

### Security

| Item | Result |
| --- | --- |
| AWS · DB · psql · Spring Boot · Lambda · Step Functions · ECS · SSM · EC2 · IAM · RDS execution | 0 Kiro executions · performed directly by operator |
| broker · KIS · crawler · automated buy · automated sell · fill sync · position sync · intraday monitor execution | 0 |
| Newly recorded raw secret · password · token · webhook URL | 0 |
| Newly recorded raw accountNo · account-id · actual ARN · public IP · broker_order_no | 0 |
| Newly recorded full raw payload · raw HTML · full SQL output | 0 |
| git add · commit · push execution | 0 |
| Placeholder policy | Use only the `[REDACTED_ACCOUNT_NO]` · `[REDACTED_ARN]` · `[REDACTED_PUBLIC_IP]` · `[REDACTED_SECRET_ARN]` families |

## 2026-07-08 (daily-step1-11-ecs-timezone-tz-patch · stale data root cause confirmed)

### Added

- 🟠 Added Daily Step1~11 stale-data root-cause analysis record

  | Item | Value |
  | --- | --- |
  | Background | 2026-07-08 08:00 KST Daily Step1~11 SUCCEEDED · DB latest raw / feature / decision `data_date` stuck at 2026-07-06 |
  | Direct cause | ECS Fargate container UTC timezone + naive `datetime.now().date()` usage → when run at 08:00 KST, UTC was around 23:00 on the previous day → target date slipped one additional day |
  | Excluded cause | Incorrect 2026-07-07 market-holiday judgment (confirmed `is_holiday(2026-07-07,"KR")==False`) |
  | Target file (1) | `.kiro/WORKLOG.md` |
  | Target file (2) | `.kiro/README.md` (Current Status Dashboard row status wording) |
  | Target file (3) | `.kiro/specs/_common/followups-overview.md` (Now · Done recently) |
  | Target file (4) | `.kiro/specs/_common/risk-register.md` (R-DATA-010 Mitigation history) |
  | Target file (5) | `.kiro/specs/_common/operator-decisions.md` (new decision) |

- 🟠 Added record of short-term ECS crawler / preprocessor timezone patch (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | New TaskDefinition (1) | `portfolio-paper-interest-crawler:8` |
  | New TaskDefinition (2) | `portfolio-paper-interest-preprocessor:2` |
  | Added env | `TZ=Asia/Seoul` |
  | State Machine update target | `portfolio-paper-daily-step1-17-approval` |
  | Step2A_RunInterestCrawlerNongui | crawler `:7` → `:8` |
  | Step3_RunPreprocessor | preprocessor `:1` → `:2` |
  | State Machine revisionId | `[REDACTED_REVISION_ID]` (raw value not recorded) |
  | one-off TZ smoke | crawler:8 · preprocessor:2 each passed `TZ_ENV=Asia/Seoul` · `time.tzname=('KST','KST')` · `naive_yesterday=2026-07-07` |

- 🟠 Added follow-up for next automated-run validation

  | Item | Value |
  | --- | --- |
  | Planned validation time | 2026-07-09 08:00 KST Daily Step1~11 automated run · 09:01 Step 12~17 automated run |
  | Observation item (1) | Whether Step2A crawler log collects `interest_price` · `interest_investorflow` for 2026-07-07 |
  | Observation item (2) | Whether preprocessor latest date advances to 2026-07-07 |
  | Observation item (3) | Whether `decision.strategy_daily_run.data_date` becomes 2026-07-07 |
  | Observation item (4) | Whether Step 12~17 automated execution flows normally without stale-data impact |
  | Action today | No manual Step1~11 rerun |

### Changed

- 🟠 Briefly supplemented `_common/risk-register.md` R-DATA-010 Mitigation history evidence

  | Item | Value |
  | --- | --- |
  | Target Risk ID | R-DATA-010 |
  | Status change | None (`Open` remains · partial mitigation applied · next auto-run verification pending) |
  | Additional cause | ECS Fargate UTC timezone + naive `datetime.now()` usage |
  | Additional mitigation | crawler / preprocessor TaskDefinition `TZ=Asia/Seoul` · State Machine Step2A / Step3 revision updated · one-off TZ smoke passed |
  | Follow-up validation | After 2026-07-09 08:00 KST automated run, verify raw / preprocessor / decision `data_date` |

- 🟠 Added new decision in `_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | New Decision ID | OD-MS-040 |
  | Decision summary | ECS batch containers that calculate Daily market dates explicitly use Asia/Seoul timezone instead of relying on the UTC default |
  | Short-term action | Add TaskDefinition `TZ=Asia/Seoul` |
  | Fundamental improvement | Replace direct `datetime.now()` usage in code with a timezone-aware helper (follow-up) |
  | Status | 🟢 CONFIRMED |

- 🟠 Adjusted Current Status Dashboard row wording in `README.md`

  | Item | Value |
  | --- | --- |
  | Paper Daily Step 1-11 status | 🟢 automated ENABLED → 🟠 automated ENABLED · awaiting 2026-07-09 freshness validation |
  | Latest validation date | 2026-07-01 → 2026-07-08 |
  | Nearby note | DB freshness recovery to be confirmed in 2026-07-09 automated run |
  | Daily Brief Slack row | Existing 2026-07-08 holiday-guard supplement retained |

### Security

| Item | Result |
| --- | --- |
| AWS · DB · psql · Spring Boot execution | 0 |
| ECS RegisterTaskDefinition · State Machine UpdateStateMachine · one-off TZ smoke | 0 Kiro executions · performed directly by operator |
| Slack webhook · Step Functions · Lambda · SSM · EC2 execution | 0 |
| broker · KIS · crawler · automated buy · automated sell · fill sync · position sync · intraday monitor execution | 0 |
| Newly recorded raw secret · password · token · webhook URL | 0 |
| Newly recorded full account-id · actual ARN · public IP · broker_order_no · image digest · execution ARN | 0 |
| Raw State Machine revisionId recorded | 0 (`[REDACTED_REVISION_ID]` used) |
| Newly recorded full AWS CLI output | 0 |
| git add · commit · push execution | 0 |
| Placeholder policy | Use only the `[REDACTED*]` family |

## 2026-07-08 (scheduler-inventory-and-holiday-guard-boost · operating-status-table preparation run)

### Added

- 🟢 Reflected Scheduler inventory check results in documentation

  | Item | Value |
  | --- | --- |
  | Target document (1) | `.kiro/WORKLOG.md` |
  | Target document (2) | `.kiro/README.md` (Daily Brief Slack row validation date) |
  | Target document (3) | `.kiro/specs/_common/followups-overview.md` (new Done recently row) |
  | Total Scheduler count | 7 |
  | ENABLED / DISABLED | 7 / 0 |
  | Timezone · Flexible · weekdays | Asia/Seoul · OFF · MON-FRI |

- 🟢 Reflected Intraday · Daily Brief holiday-guard supplements in documentation

  | Item | Value |
  | --- | --- |
  | Intraday scheduler dispatcher | Newly deployed guard · added `HOLIDAY_COUNTRY` · `FAIL_CLOSED_ON_HOLIDAY_ERROR` env · dryRun validation complete |
  | Daily Brief Summary Builder | Newly deployed guard · added the same 2 env values |
  | Daily Brief state machine | Inserted `CheckHolidaySkip` Choice · added `SkipDailyBriefSlack` terminal path |
  | dryRun validation result | `runDate=2026-07-08 skipped=false` · `runDate=2026-08-15 skipped=true` |
  | Actual execution actor | Operator directly (0 Kiro executions) |

### Changed

- 🟢 Briefly supplemented R-AUTO-035 Details Mitigation history evidence (`_common/risk-register.md`)

  | Item | Value |
  | --- | --- |
  | Target Risk ID | R-AUTO-035 |
  | New Risk ID | 0 |
  | Additional content | 2026-07-08 Summary Builder guard deployment · inserted state machine `CheckHolidaySkip` Choice · confirmed holiday dryRun |
  | Status change | None (`Mitigated` remains) |

- 🟢 Updated README Current Status Dashboard

  | Item | Value |
  | --- | --- |
  | Daily Brief Slack validation date | 2026-06-30 → 2026-07-08 |
  | Status description | Automated ENABLED + holiday-guard supplement complete |
  | Other rows | No change |

### Security

| Item | Result |
| --- | --- |
| AWS · DB · psql · Spring Boot execution | 0 |
| Lambda deployment · env addition · Step Functions definition update | 0 Kiro executions · performed directly by operator |
| Slack webhook · Step Functions · Lambda · ECS · SSM · EC2 execution | 0 |
| broker · KIS · crawler · automated buy · automated sell · fill sync · position sync · intraday monitor execution | 0 |
| Newly recorded raw secret · password · token · webhook URL | 0 |
| Newly recorded full account no · actual ARN · public IP · broker_order_no · Lambda zip SHA256 · image digest · execution ARN | 0 |
| Newly recorded full AWS CLI output | 0 |
| git add · commit · push execution | 0 |
| Placeholder policy | Use only the `[REDACTED*]` family |

## 2026-07-07 (root-docs-readability · Kiro execution-overhead improvement · _common 10th-pass result summary)

### Added

- 🟢 Recorded `_common` 10th-pass result summary (appended to top of this CHANGELOG · WORKLOG)

  | Item | Value |
  | --- | --- |
  | Target file (1) | `_common/risk-register.md` |
  | Target file (2) | `_common/followups-overview.md` |
  | Target file (3) | `_common/operator-decisions.md` |
  | risk-register.md over300 | 134 → 9 |
  | followups-overview.md over300 | 210 → 70 |
  | operator-decisions.md over300 | 240 → 2 |
  | tableO300 | 0 |
  | Encoding | UTF-8 No BOM maintained |
  | Newly introduced raw sensitive information | 0 |
  | Judgment | Long Details migration complete |

- 🟢 Recorded readability-cleanup goal for the 3 root documents

  | Item | Value |
  | --- | --- |
  | Targets | `.kiro/README.md` · `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` |
  | Reference style | 2026-07-01 CHANGELOG section |
  | Formatting direction | Table-centered · short bullets · Security table |
  | Not targets | `.kiro/specs/**` · documents in the 8 MS repositories |
  | README actual edit | Not performed |

### Changed

- 🟠 Recorded improvement direction for Kiro documentation workflow

  | Item | Value |
  | --- | --- |
  | Delay point | Violation-matrix organization step for the 3 root files |
  | Root-cause summary | ViolationRecord matrix · content_hash · safety_exception classification |
  | Additional causes | sub-agent calls · scans outside workspace · long PowerShell one-liners |
  | Judgment | Audit pipeline was excessive relative to local documentation edits |
  | Improvement direction | Apply rule 0 only to newly written/directly modified sections |

- 🟠 Recorded need to supplement AGENTS.md (no actual change in this pass)

  | Item | Value |
  | --- | --- |
  | Principle retained | Rule 0 highest-priority document-readability rule |
  | Default-scope clause | Limit to newly written/directly modified sections (needs addition) |
  | Exception clause | ViolationRecord matrix · content_hash · full before/after audit only when explicitly requested |
  | Existing whole-document violations | Record only as follow-up candidates unless explicitly requested |
  | Timing | Separate into dedicated AGENTS.md edit pass |

### Security

| Item | Result |
| --- | --- |
| AWS · DB · psql · Spring Boot execution | 0 |
| Slack webhook · Step Functions · Lambda · ECS execution | 0 |
| SSM · EC2 · broker · KIS · crawler execution | 0 |
| automated buy · automated sell · fill sync · position sync · intraday monitor execution | 0 |
| Raw secret · password · token · webhook URL recorded | 0 |
| Raw actual ARN · account-id · public IP · broker_order_no recorded | 0 |
| Git writes (`git add` · `git commit` · `git push`) | 0 |
| Placeholder policy | Use only the `[REDACTED*]` family |

## 2026-07-06 (spec-docs-readability-sessions · 4 parallel readability-improvement Sessions across 01~09 spec subfolders)

### Added

- 🟢 Session A — readability improvements for specs 01~03

  | Item | Value |
  | --- | --- |
  | Scope front | 01-aws-migration-foundation |
  | Scope middle | 02-aws-network-and-rds |
  | Scope end | 03-marketconnector-ec2 |
  | Modified file count | 13 |
  | Original-preserved file count | 5 |
  | >300 · >500 lines | 0 across all 18 files |
  | Extra-long table rows · bullets | 0 |

- 🟢 Session B — readability improvements for specs 04~05

  | Item | Value |
  | --- | --- |
  | Scope front | 04-strategy-batch-stepfunctions / operation-notes.md |
  | Scope middle | 05-port-view-ecs-and-runbook / operation-notes.md |
  | Scope end | 05 runbook.md · validation-checklist.md |
  | New in 04 | Automation Lineup Dashboard · date-based index |
  | New in 05 | View execution-location backend matrix |
  | >500 lines | 0 in key 04 / 05 documents |
  | Table rows · Bullets >300 · >500 | 0 |

- 🟢 Session C — readability improvements for spec 06 secrets-and-iam

  | Item | Value |
  | --- | --- |
  | Modified file count | 6 |
  | Modified files front | requirements.md · design.md · tasks.md |
  | Modified files end | operation-notes.md · runbook.md · validation-checklist.md |
  | gt300 (>300 lines) | 39 → 0 |
  | gt500 (>500 lines) | 9 → 0 |
  | Placeholder Coverage | Cleanup complete |
  | Security-policy weakening | None |

- 🟢 Session D — readability improvements for specs 08 / 09 data · research

  | Item | Value |
  | --- | --- |
  | Scope front | 08 requirements.md · design.md · tasks.md · operation-notes.md |
  | Scope end | 09 operation-notes.md |
  | Modified file count | 5 |
  | Editing method | additive-only |
  | New blocks front | Role Split · Runtime Role Split · Task Section Overview |
  | New blocks end | Open Risks & Next Checks · Key Fact Preservation |
  | Lines over 500 characters | 0 |
  | Critical-fact preservation | stale data · latest_trade_date · KRX worker · research batch fact-loss 0 |

### Security

| Item | Result |
| --- | --- |
| AWS · DB · psql · Spring Boot execution | 0 |
| Slack webhook · Step Functions · Lambda · ECS execution | 0 |
| SSM · EC2 · broker · KIS · crawler execution | 0 |
| Raw secret · password · token · webhook URL recorded | 0 |
| Raw actual ARN · account-id · public IP · broker_order_no recorded | 0 |
| Placeholder policy | Use only the `[REDACTED*]` family |

## 2026-07-06 (kiro-common-docs-readability-6-7 · recovery-oriented passes 6~7)

### Changed

- 🟢 `_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Processing | Restored mojibake in display wording |
  | fact-loss | None |
  | Decision ID · Status · [REDACTED*] count | Maintained at or above baseline |

- 🟢 `_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | Processing | Recovered some wrap artifacts |
  | Checked items front | single-character bullets · broken Korean |
  | Checked items end | identified broken-wrap candidates |
  | fact-loss | None |
  | Risk ID · Status · [REDACTED*] count | Maintained at or above baseline |

- 🟢 Documents not edited (this pass)

  | Item | Value |
  | --- | --- |
  | No-edit front | `_common/followups-overview.md` · `_common/ms-aws-service-decision-matrix.md` |
  | No-edit middle | `_common/cost-simulation.md` · `_common/aws-resource-glossary.md` |
  | Status | Not targets of passes 6~7 or retained as follow-up candidates |

### Security

| Item | Result |
| --- | --- |
| AWS · DB · Slack · crawler · broker · KIS execution | 0 |
| Step Functions · Lambda · SSM · psql · Spring Boot execution | 0 |
| Raw secret · password · token · webhook recorded | 0 |
| Raw actual ARN · account-id · public IP · broker_order_no recorded | 0 |
| Storage encoding for 6 target documents | UTF-8 No BOM maintained |
| Placeholder policy | Use only the `[REDACTED*]` family |

## 2026-07-06 (kiro-common-docs-readability-9 · 9th pass · restructuring 6 _common documents)

### Added

- 🟢 `_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Added at top | `## Purpose` H2 heading |
  | Document flow front | Purpose → Review Needed → Status Legend → Decision Dashboard |
  | Document flow middle | At a Glance → Decision Index → Open/Tentative/Deferred Decisions |
  | Document flow end | Decision Details → Change Log → Decision Change Log Details |
  | Document final | Decision Update Rules → Security Notes |
  | Principle | Preserve original content · explicitly state `[REDACTED*]` placeholder policy |

- 🟢 `_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | Added at top | `## Purpose` H2 heading |
  | Document flow front | Purpose → Risk Dashboard → Immediate Action Risks → Risk Index |
  | Document flow end | Risk Details → Accepted/Closed Risks → Risk Update Rules → Security Notes |
  | Principle | Do not reuse Risk IDs · do not quote raw evidence · `[REDACTED*]` placeholder policy |
  | New H2 | `## Accepted/Closed Risks` (summary table for R-AUTO-024 · R-AUTO-027) |

- 🟢 `_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | Added at top | `## Purpose` H2 heading |
  | New H2 | `## Spec Roadmap` |
  | H3 under Spec Roadmap | environment model · sequence · summary of each follow-up Spec · dependency diagram |
  | Additional new H2 | `## Update Rules` |

- 🟢 `_common/aws-resource-glossary.md`

  | Item | Value |
  | --- | --- |
  | Added at top | `## Purpose` H2 heading |
  | New H2 | `## Glossary` (demoted 38 service items to H3) |
  | Additional new H2 | `## Usage Notes` |
  | Usage Notes targets front | EventBridge Scheduler · Lambda · IAM Role |
  | Usage Notes targets end | ECS/Fargate · Step Functions (migration priority explicitly stated) |
  | Additional new H2 | `## Update Rules` |

- 🟢 `_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Added at top | `## Purpose` H2 (explicitly states service-selection conclusions remain unchanged) |
  | New H2 | `## Rejected/Deferred Services` |
  | H3 under Rejected/Deferred | chapter 7 (EKS) · chapter 8 (Lambda) · chapter 9 (Elastic Beanstalk / App Runner) |
  | Additional new H2 | `## Appendix` |
  | H3 under Appendix | chapter 10 final conclusion · Evidence Details |

- 🟢 `_common/cost-simulation.md`

  | Item | Value |
  | --- | --- |
  | Added at top | `## Purpose` H2 heading |
  | Policy | Minimal changes · avoid adding new cost figures |

### Changed

- 🟢 `_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Rename | `## Decision Summary` → `## Decision Dashboard` (explicit warning that recount is required) |
  | Rename | `## Detailed Decisions` → `## Decision Index` (added guidance that it serves as a table index) |
  | H2 → H3 demotion | Duplicate H2 `## Decision Details — Compute / Service Placement` |
  | Move | `Deferred Decisions` → before `Decision Details` |
  | Move | `Decision Update Rules` → after `Change Log Details` |
  | Rename + move | `본 spec 작업 안전 제약` → `Security Notes` (end of file) |

- 🟢 `_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | Rename | `## Risk Table` → `## Risk Index` |
  | H2 → H3 demotion | `## 컬럼 정의` · `## 상태 정의` |
  | Rename | `추가 식별 시 갱신 규칙` → `Risk Update Rules` |
  | Rename | `본 문서 작업 안전 제약` → `Security Notes` |

- 🟢 `_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | H2 → H3 demotion front | `## 환경 모델` · `## 진행 순서` |
  | H2 → H3 demotion end | `## 각 후속 Spec 요약` · `## 의존성 다이어그램` |
  | Integrated location | Under `Spec Roadmap` |
  | Rename | `공통 작업 범위 제한` → `Security Notes` |

- 🟢 `_common/aws-resource-glossary.md`

  | Item | Value |
  | --- | --- |
  | Rename | `## 카테고리별 목차` → `## Category TOC` |
  | H2 → H3 demotion | 38 individual service H2 headings |
  | Demotion example A | VPC · Subnet · NAT Gateway · EC2 |
  | Demotion example B | ECS · Fargate · RDS · Secrets Manager |
  | Demotion example C | IAM Role · EventBridge Scheduler · Step Functions · Lambda |
  | Demotion example D | S3 · AWS Batch · KMS etc. |
  | Rename | `본 spec 작업 안전 제약` → `Security Notes` |

- 🟢 `_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Rename | `## 5. MS별 최종 권고안` → `## MS Decision Cards` |
  | Rename | `## 6. 포트폴리오 어필 관점의 보강안` → `## Portfolio Appeal Notes` |
  | Absorbed targets front | `## 7. EKS 검토 섹션` · `## 8. Lambda 검토 섹션` |
  | Absorbed targets end | `## 9. Elastic Beanstalk / App Runner 검토 섹션` |
  | Absorbed location | As H3 under `Rejected/Deferred Services` |
  | Absorbed targets (Appendix) | `## 10. 최종 결론` · `## Evidence Details` |
  | Absorbed location | As H3 under `Appendix` |
  | Added | `## Security Notes` |

- 🟢 `_common/cost-simulation.md`

  | Item | Value |
  | --- | --- |
  | Rename | `## 8. 안전 제약` → `## Security Notes` |
  | No change | Major structure · cost figures · scenario labels · USD count |

### Security

- 🟢 Newly introduced raw sensitive information (common to 6 `_common` documents)

  | Item | Value |
  | --- | --- |
  | secret · password · token · webhook URL | 0 |
  | KIS app key · KIS app secret | 0 |
  | account number · account-id · actual ARN | 0 |
  | public IP · broker_order_no · full image digest sha256 | 0 |

- 🟢 `[REDACTED*]` placeholder count (maintained at or above baseline)

  | Document | Value |
  | --- | --- |
  | operator-decisions | 37 → 42 |
  | risk-register | 7 → 13 |
  | followups-overview | 23 → 29 |
  | aws-resource-glossary | 11 → 17 |
  | ms-aws-service-decision-matrix | 2 → 9 |
  | cost-simulation | 3 → 3 |

- 🟢 Encoding handling

  | Item | Value |
  | --- | --- |
  | `risk-register.md` pre-existing UTF-8 BOM | Option A (removed) |
  | Other 5 documents | UTF-8 No BOM from baseline |
  | Final storage encoding for all 6 target documents | Standardized to UTF-8 No BOM |

- 🟢 Execution categories (0 each)

  | Item | Value |
  | --- | --- |
  | AWS · DB · psql · Spring Boot execution | 0 |
  | Slack webhook · Step Functions · Lambda · ECS execution | 0 |
  | SSM · EC2 · KIS API · broker execution | 0 |
  | crawler · Selenium execution | 0 |
  | automated buy · automated sell · fill sync · position sync execution | 0 |
  | intraday monitor execution | 0 |
  | write-type · rollback-type git command execution | 0 |

## 2026-07-03 (Step Functions execution-history OPS mirror complete)

### Added

- 🟢 Expanded use of `ops.strategy_daily_batch_run`

  | Item | Value |
  | --- | --- |
  | New run_type value | `AWS_STEPFUNCTIONS` |
  | Recording actor | Lambda `portfolio-daily-batch-ops-recorder` |
  | Minimum-permission role | `ops_recorder_app` |

- 🟢 Smoke passed

  | Item | Value |
  | --- | --- |
  | RECORD_START / RECORD_STEP / RECORD_SUCCESS | All 3 smoke tests passed |
  | commit smoke | `batchRunId=53` |
  | Step 12 approval-blocked smoke | OPS table record confirmed |

### Changed

- 🟢 `_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | New decision | `OD-DB-012` (OPS mirror schema) |
  | New decision | `OD-MS-039` (recording actor · minimum-permission role) |

- 🟢 `_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | New risk | `R-AUTO-038` |
  | Initial status | 🟢 Mitigated (smoke passed) |

- 🟢 `_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | 2026-07-03 follow-up note | Added |
  | Remaining follow-up front | Reflect in View Daily Batch screen |
  | Remaining follow-up end | Observe consistency in a real automated execution run |

### Security

| Item | Result |
| --- | --- |
| AWS CLI · boto3 · psql · Spring Boot · Lambda · SFN · Slack webhook · KIS execution | 0 |
| broker order submission · aws-live work | 0 |
| Raw secret · password · token · webhook URL · account number · actual ARN · public IP · broker_order_no recorded | 0 |
| Placeholder policy | Use only the `[REDACTED*]` family |

## 2026-07-02 (portfolio-event-notifier Slack wording improvements + View read-screen stabilization smoke)

### Added

- 🟢 Improved `portfolio-event-notifier` success formatter

  | Item | Value |
  | --- | --- |
  | APPROVAL_REQUIRED title | `[Daily 검증] 성공` |
  | APPROVAL_REQUIRED change | Removed "Awaiting approval" line |
  | DAILY_EXECUTION_SUCCESS title | `[Daily 실행] 성공` |
  | DAILY_EXECUTION_SUCCESS change | Standardized `SUCCESS` → `성공` |
  | Smoke | Both success formatter variants passed |

- 🟢 View read-screen baseline smoke passed

  | Item | Value |
  | --- | --- |
  | Screens front | `/dashboard` · `/balance-summary` · `/positions` · `/orders` |
  | Screens end | `/strategy/execution/plans` · `/strategy/reports/latest` · `/daily-batch` |
  | Latest balance snapshot | `id=357` · `as_of_date=2026-07-02` |
  | Amounts | `total_eval_amount=8,706,505` · `cash_balance=8,706,505` |
  | Positions | 0 |
  | Historical positions exposed on /positions | None |

### Changed

- 🟢 `_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | 2026-07-02 follow-up note | Added (Slack · View) |
  | Cause of Daily Batch screen history mismatch | Not a View query bug · identified as absence of Step Functions run mirror |

- 🟢 `_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | Slack formatter-related risk | Progress on success-formatter mitigation · FAILED formatter remains |

### Security

| Item | Result |
| --- | --- |
| AWS CLI · boto3 · psql · Spring Boot · Lambda · SFN · Slack webhook · KIS execution | 0 |
| broker order submission · aws-live work | 0 |
| Raw secret · password · token · webhook URL · account number · actual ARN · public IP · broker_order_no recorded | 0 |
| Placeholder policy | Use only the `[REDACTED*]` family |

## 2026-07-02 (kiro-common-docs-readability · 8th pass · reducing line density in _common documents)

### Changed

- 🟢 `_common/cost-simulation.md`

  | Item | Value |
  | --- | --- |
  | Restructure | paper / live realistic key-configuration inline bullets → 3-column table |
  | Table columns | Category · Item · Approx. USD/month |
  | Intro | Split 3 guidance lines into 2 paragraphs |
  | >300 lines | 1 → 0 |
  | Max line | 320 → 296 |

- 🟢 `_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Split target front | Evidence Details 4.2 (2026-06-30 afternoon) |
  | Split target end | Evidence Details 4.3 (2026-06-23 · 2026-06-24) |
  | Processing | Narrative over 500 chars → multi-line bullets |
  | Recomposition target | Dispatcher Lambda specs · Slack pass-validation enumeration → table/bullets |
  | >500 lines | 2 → 0 |
  | Max line | 514 → 488 |

- 🟢 `_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | Split target front | 2026-06-13 (Strategy Execution responsibility split) |
  | Split target middle | 2026-06-17 (Daily wrapper decision lock) |
  | Split target end | 2026-06-18 (connector_order_check decision lock) |
  | Processing | 3+ sentences · 500+ chars → split into bullets |
  | >500 lines | 19 → 17 |
  | Max line | 544 → 530 |

- 🟢 `_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Split target front | Change Log Details 2026-06-16 (safe subset safety criteria) |
  | Split target middle | 2026-06-23 (OD-SAFE-* proof) |
  | Split target end | 2026-06-29 (2) (new R-AUTO-034 mitigation) |
  | Processing | Evidence over 500 chars → split into bullets |
  | >500 lines | 6 → 3 |
  | Max line | 557 → 529 |

### Security

- 🟢 Newly introduced raw sensitive information (common to 6 `_common` documents)

  | Item | Value |
  | --- | --- |
  | secret · password · token · webhook URL | 0 |
  | actual ARN · actual IP · broker_order_no | 0 |
  | `[REDACTED*]` placeholder count | before ≤ after maintained |
  | `risk-register.md` pre-existing BOM (1) | State preserved |
  | Newly introduced BOM | 0 |

## 2026-07-01 (June actual AWS cost analysis · VPC Endpoint savings alignment)

### Added

- 🟢 Confirmed actual June aws-paper cost

  | Item | Value |
  | --- | --- |
  | Before tax | `135.40 USD` |
  | Tax | `13.55 USD` |
  | Including tax | approx. `148.95 USD` |
  | Month-end estimate alignment | `180 USD` is conservative but reasonable for July full automation |

- 🟢 Aligned June cost drivers

  | Item | Value |
  | --- | --- |
  | VPC cost | `74.24 USD` |
  | Of which VPC Endpoint | `70.69 USD` |
  | Judgment | VPC Endpoint is the primary cost driver |

### Changed

- 🟢 Aligned VPC Endpoint reduction

  | Item | Value |
  | --- | --- |
  | SSM endpoint | Removal complete |
  | 4 endpoints | `ecr.api` · `ecr.dkr` · `logs` · `secretsmanager` reduced from 2 AZ → 1 AZ |
  | Expected monthly savings | approx. `56.16 USD` / month |
  | Additional endpoint deletion | Deferred due to operational risk |

- 🟢 `_common/cost-simulation.md`

  | Item | Value |
  | --- | --- |
  | June actual result | Reflected consistently |
  | Cost driver Top | Reflected VPC Endpoint |
  | Savings alignment front | SSM endpoint removal |
  | Savings alignment end | Reduced 4 endpoints to 1 AZ |

### Security

| Item | Result |
| --- | --- |
| AWS CLI · boto3 · psql · Billing Console automated-query execution | 0 |
| broker order submission · aws-live work | 0 |
| Raw secret · password · token · billing account id · payment method recorded | 0 |
| Placeholder policy | Use only the `[REDACTED*]` family |

## 2026-07-01 (kiro-common-docs-readability spec execution · Dashboard-first restructuring of _common documents)

### Added

- 🟢 Added new `Risk Dashboard` section at the top of `_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | Group front | Open High risks · Mitigated High risks |
  | Group end | Newly added risks · Risks needing operator action |
  | Format | Table format (for daily operator review) |
  | Status badges front | 🔴 Open High · 🟠 Open · 🟢 Mitigated |
  | Status badges end | 🔵 Accepted · ⚫ Closed |

- 🟢 Added new `Current Follow-up Dashboard` section at the top of `_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | Group front | Now · Next · Later |
  | Group end | Blocked · Done recently |
  | Now/Next/Later format | `- [ ]` checklist |
  | Blocked/Done recently format | Table format |
  | Links | Relative paths to related spec `operation-notes.md` |

- 🟢 Added new `Final Recommendation Summary` section at the top of `_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Table size | Fixed order of 8 MS · 5 columns |
  | 5-column front | MS · 1st-choice service · 2nd choice/deferred |
  | 5-column end | Adoption reason · cost/operational risk |
  | Added | Summary of not-recommended services |

- 🟢 Added new `Cost Dashboard` section at the top of `_common/cost-simulation.md`

  | Item | Value |
  | --- | --- |
  | Cost labels | paper realistic · live realistic estimated monthly cost |
  | Additional summary front | cost driver Top 5 |
  | Additional summary end | cost-saving decisions Top 5 |
  | Repeated caveat | `AWS Pricing Calculator 확인 필요` |

- 🟢 Added category-based TOC section at the top of `_common/aws-resource-glossary.md`

  | Item | Value |
  | --- | --- |
  | Category front | Network · Compute · Database |
  | Category middle | Security/IAM/Secrets · Orchestration |
  | Category end | Observability · Storage/Artifact |
  | Link format | Anchor links |

### Changed

- 🟢 `_common/operator-decisions.md` (no file changes)

  | Item | Value |
  | --- | --- |
  | Reason | Already satisfies required structure |
  | Structure front | Status Legend → Decision Summary → At a Glance |
  | Structure end | Detailed Decisions → Change Log |

- 🟢 Placed Dashboard-first structure at top

  | Item | Value |
  | --- | --- |
  | Applied documents | Other 5 `_common` documents |
  | Policy | Detail-behind (existing detail preserved below) |
  | Preserved targets front | Risk Table · Risk Details · execution sequence |
  | Preserved targets end | date-based follow-up notes · chapters 1~10 · Change Log |

### Security

- 🟢 Execution categories (0 each)

  | Item | Value |
  | --- | --- |
  | Kiro commands executed | 0 |
  | AWS CLI · boto3 · psql · Spring Boot execution | 0 |
  | broker · KIS · crawler execution | 0 |
  | automated buy · automated sell · fill sync · position sync execution | 0 |
  | intraday monitor execution | 0 |

- 🟢 Change-scope restriction

  | Item | Value |
  | --- | --- |
  | Changes to 8 MS repositories | 0 |
  | Changes outside `_common/` spec folder | 0 |
  | Newly recorded plaintext sensitive information | 0 |
  | Placeholder policy | Maintain `[REDACTED]` family |

- 🟢 git work

  | Item | Value |
  | --- | --- |
  | Automatic rollback | Not performed |
  | Write-type git front | `git add` · `git commit` |
  | Write-type git end | `git rm` · `git mv` · `git push` |
  | Write-type execution | 0 |

## 2026-07-01 (paper Daily automation first full ON + Step 12~17 Scheduler ENABLED)

### Added

- 🟢 **Added Step 12~17 manual execution SUCCEEDED result**

  | Item | Value |
  | --- | --- |
  | executionName | `port-manual-daily-step12-17-20260701-043747` |
  | State machine | `portfolio-paper-daily-step12-17-approval` |
  | Status | 🟢 **SUCCEEDED** |
  | Start | `2026-07-01T13:37:47.856+09:00` |
  | Stop | `2026-07-01T13:40:41.212+09:00` |
  | Execution history | `ExecutionSucceeded` |
  | Slack | 3 messages received (07:50 pre-market · 08:24 approval required · Step 12~17 success) |

  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Added DB after-check pass result**

  | Item | Value |
  | --- | --- |
  | `strategy_execution_order` (2026-07-01) | 0 |
  | REQUESTED strategy orders | 0 |
  | active `connector_order_request` | 0 |
  | today's connector orders | 0 |
  | latest `connector_balance_snapshot` | `id=321` |
  | `as_of_date` | `2026-07-01` |
  | `total_eval_amount` | `8,706,505` |
  | `cash_balance` | `8,706,505` |
  | `eval_profit` | `0` |
  | Step 12~17 judgment | 🔵 **NO_TARGET** safe exit |

  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Added confirmation that all 7 paper Daily automation lineup items are ENABLED**

| Item | Value |
| --- | --- |
| 07:50 | **Task**: EC2 start<br>**Status**: 🟢 **ENABLED** |
| 07:50 | **Task**: pre-market Slack<br>**Status**: 🟢 **ENABLED** |
| 08:00 | **Task**: Step 1~11<br>**Status**: 🟢 **ENABLED** |
| **09:01** | **Task**: **Step 12~17 (ENABLED on this date)**<br>**Status**: 🟢 **ENABLED** |
| 09:10~15:50 | **Task**: 10-minute intraday stop-loss<br>**Status**: 🟢 **ENABLED** |
| 15:50 | **Task**: post-market Slack<br>**Status**: 🟢 **ENABLED** |
| 15:50 | **Task**: MarketConnector stop<br>**Status**: 🟢 **ENABLED** |

  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Added new R-AUTO-037 row**

  | Item | Value |
  | --- | --- |
  | Risk | Step 12~17 automated execution could run even when Step 1~11 failed or data is not ready |
  | Impact | `High` |
  | Probability | `Low` |
  | Status | 🟢 **Mitigated** |
  | Affected Spec | `04, 05, 10` |

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | R-AUTO-001 | Added [2026-07-01 supplement] mitigation note |
  | Core policy | Continue prohibiting automatic retries for BUY / SELL / fill sync / position-changing steps |
  | Additional observation | Step Functions Retry policy remains a review target even after Step 12~17 Scheduler ENABLED |
  | Status | 🟠 **Open** |
  | R-AUTO-025 | Added [2026-07-01 automatic ENABLE entry] mitigation note |
  | Change summary | Step 12~17 Scheduler 🟠 **DISABLED** → 🟢 **ENABLED** |
  | Entry condition | 09:01 schedule hold policy passed ENABLE after operator approval |
  | Status | 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Change Log | Added 2026-07-01 row |
  | Evidence supplement | `OD-SAFE-001` · `OD-SAFE-002` · `OD-SAFE-003` · `OD-MS-009` · `OD-MS-032` · `OD-MS-033` |
  | New decisions | None |
  | Decision Summary | Total 97 · confirmed 52 · tentative 42 preserved |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | 2026-07-01 follow-up note | Added |
  | Completed | 3 |
  | Remaining follow-ups | 5 |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Supplement scope | port_strategy_execution · Daily Batch orchestration · EventBridge Scheduler |
  | Core evidence | Both paper Step 1~11 Scheduler + Step 12~17 Scheduler are 🟢 **ENABLED** |
  | Step 12~17 manual execution | 🟢 **SUCCEEDED** |
  | Automated execution lineup | Confirmed |
  | Service selection | No change |

- 🟢 `.kiro/specs/_common/cost-simulation.md`

  | Item | Value |
  | --- | --- |
  | Trigger | Step 12~17 Scheduler 🟠 **DISABLED** → 🟢 **ENABLED** |
  | New always-on compute cost | None |
  | EventBridge Scheduler | Within free tier |
  | Step Functions transitions | Very small |
  | RDS · EC2 · Fargate · NAT · ALB | No cost change |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | Item | Value |
  | --- | --- |
  | Target item | EventBridge Scheduler |
  | Operational example supplement | `portfolio-paper-daily-step12-17-order-0901-kst` |
  | Start date | 🟢 **ENABLED** from 2026-07-01 |
  | Role | Responsible for automated paper Step 12~17 execution |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | Appended section | `2026-07-01 — paper Daily Step 12~17 자동 실행 ENABLED` |
  | Step 12~17 manual execution | 🟢 **SUCCEEDED** |
  | DB after-check | Passed |
  | Scheduler | 🟢 **ENABLED** |
  | Target Input | `{"scheduleType":"STEP12_17_ORDER","dryRun":false}` |
  | Full Daily lineup | Confirmation complete |
  | Step Functions structure | No change |
  | aws-live policy | No change |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-30 ECS View manual-approval path | Already passed |
  | 2026-07-01 execution path | Step 12~17 automated Scheduler 🟢 **ENABLED** without View manual approval |
  | port-view role | Retains read · approval · operations UI |
  | paper automated ordering | Switched to 09:01 Scheduler path |
  | View manual execution | Retained as operator fallback |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/runbook.md`

  | Item | Value |
  | --- | --- |
  | Added procedure | Step 12~17 Scheduler enable / disable operation |
  | Procedure items | Confirm current state · confirm Target.Input · DISABLED → ENABLED · ENABLED → DISABLED rollback |
  | Confirmation commands | `list-schedules` · `get-schedule` |
  | Lineup confirmation | Full Daily lineup |
  | Caution | Duplicate new start-execution |
  | Security | Plaintext ARN · account id · secret <span style="color:#D1242F">**prohibited**</span> |
  | Marker policy | SUCCESS marker after failure <span style="color:#D1242F">**prohibited**</span> |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/validation-checklist.md`

  | Item | Value |
  | --- | --- |
  | 2026-07-01 validation | append |
  | Step 12~17 manual execution | 🟢 **SUCCEEDED** |
  | Slack | received |
  | DB after-check | passed |
  | Step 12~17 Scheduler | 🟢 **ENABLED** |
  | Full Daily automation lineup | checked |
  | active connector order | none |
  | today connector order | none |
  | latest balance snapshot | 2026-07-01 |

- 🟢 `.kiro/specs/06-secrets-and-iam/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | IAM Role change | None |
  | Evidence | Existing Scheduler role · Dispatcher Lambda permission path operational evidence |
  | New secret created | 0 |
  | New IAM policy created | 0 |
  | New Role created | 0 |

### Security

| Item | Result |
| --- | --- |
| AWS CLI · boto3 · psql · Spring Boot · external API execution | 0 |
| broker order submission | 0 |
| aws-live work | 0 |
| Raw secret recorded | 0 |

> Kiro modified documentation only in this task.

## 2026-06-30 (afternoon) (port-view ECS Fargate Public IP first port complete + ECS View → AWS Step Functions Step 12~17 approval execution passed + desiredCount 0 shutdown)

### Added

- 🟢 **Added first successful port-view ECS Fargate Public IP porting result**

  | Item | Value |
  | --- | --- |
  | Performed by | Operator directly |
  | Network | No ALB · public subnet · `assignPublicIp=ENABLED` · no NAT Gateway |
  | SG inbound | TCP 8080 restricted to operator IP/32 |
  | CloudWatch Logs | retention 7 days |
  | ECR | Created repository `portfolio-view` · image push |
  | Task definition | `portfolio-view:1` → `portfolio-view:2` (corrected missing default account-number env) |
  | Service | `portfolio-view-service` 🟢 **RUNNING** |
  | Screen checks | Direct Fargate public-IP access · Dashboard · Balance · Positions · Orders · Reports · Daily passed |

  - Details: [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md)

- 🟢 **Added successful ECS View → AWS Step Functions Step 12~17 approval execution result**

  | Item | Value |
  | --- | --- |
  | executionName | `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` |
  | State machine | `portfolio-paper-daily-step12-17-approval` |
  | Status | 🟢 **SUCCEEDED** |
  | Start | `2026-06-30T14:15:42.899+09:00` |
  | Stop | `2026-06-30T14:18:48.358+09:00` |
  | Slack | `DAILY_EXECUTION_SUCCESS` received |
  | Final judgment | 🔵 **NO_TARGET** safe exit |

  - Details: [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md)

- 🟢 **Added DB after-check pass result**

  | Item | Value |
  | --- | --- |
  | REQUESTED `strategy_execution_order` | 0 |
  | retryable rejected | 0 |
  | active `connector_order_request` | 0 |
  | today connector orders | 0 |
  | latest `connector_balance_snapshot` | `id=281` |
  | `as_of_date` | `2026-06-30` |
  | `total_eval_amount` | `8,706,505` |
  | `cash_balance` | `8,706,505` |

  - Identified 6 historical stale `connector_order_request` rows (remaining ACCEPTED from 2026-04-27 · follow-up cleanup candidates)
  - Details: [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md)
- 🟢 **Aligned 5 View operation paths** — added ECS View → AWS Step Functions Step 12~17 approval execution (this afternoon) to the existing 4.
- 🟢 **Created new `.kiro/specs/05-port-view-ecs-and-runbook/runbook.md`** — ECS service desiredCount 0/1 operation commands + automatic public-IP query pattern + browser URL output + AWS CLI `list/describe → 변수 추출 → 후속 검증` pattern + SG inbound update procedure when operator IP changes + Step 12~17 approval execution procedure.
- 🟢 **Created new `.kiro/specs/05-port-view-ecs-and-runbook/validation-checklist.md`** — 13 checklist items + pass results for this date.
- 🟢 **Added 3 operational-command authoring rules to `.kiro/AGENTS.md`** — (1) AWS CLI commands use `list/describe → 변수 추출 → 후속 검증` instead of manually replacing ARN / task ARN / ENI ID / LOG_STREAM, (2) psql validation queries first confirm actual column names using `information_schema.columns` (do not assume column names), (3) do not place SUCCESS/DONE markers after failed SQL/commands.

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | R-AUTO-033 | Added [2026-06-30 afternoon supplement] mitigation note |
  | Evidence | Public IP direct access + operator IP/32 SG + third proof of ECS View → Step 12~17 approval |
  | Status | 🟢 **Mitigated** |
  | R-AUTO-034 | Added [2026-06-30 afternoon supplement] mitigation note |
  | Evidence | First proof that Fargate task role `portfolio-paper-view-task-role` has `states:StartExecution` permission scoped to the Step 12~17 approval state machine ARN |
  | Remaining | Fargate Task Role permission separation remains responsibility of a follow-up phase in spec 06 |
  | Status | 🟠 **Open** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-30 (afternoon) follow-up note | Added |
  | Completed | 4 |
  | Follow-ups | 6 |
  | New risk notes | Public IP · desiredCount · assumed column names · stale `connector_order_request` |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Change Log | Added 2026-06-30 (afternoon) item |
  | Evidence supplement | `OD-MS-002` · `OD-MS-009` · `OD-MS-037` · `OD-SAFE-001` ~ `OD-SAFE-004` |
  | Body change | None |
  | New decisions on this date | None |
  | Decision Summary | Total 96 · confirmed 51 · tentative 42 preserved |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Target row | port-view 4.2 |
  | Additional note | 2026-06-30 (afternoon) first ECS Fargate Service proof complete |
  | Deferred | ALB · HTTPS · Route53 · Cloudflare Tunnel |
  | Still not first choices | Elastic Beanstalk · App Runner · Lambda |
  | Operational note | Manual desiredCount 0/1 operation |

- 🟢 `.kiro/specs/_common/cost-simulation.md`

  | Item | Value |
  | --- | --- |
  | Target | port-view Fargate cost assumption |
  | 2026-06-30 (afternoon) note | Added |
  | Compute assumption | 0.5 vCPU · 1 GB always-on vs temporary desiredCount 1 operation |
  | Public IPv4 | Additional cost |
  | Excluded | ALB · NAT |
  | Guidance | Keep wording to recheck with AWS Pricing Calculator |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | Item | Value |
  | --- | --- |
  | Supplement targets | ECS Fargate · IAM Role · IAM Policy · public subnet · CloudWatch Logs |
  | Operational examples | `portfolio-view-service` · `portfolio-view:2` |
  | Network | `assignPublicIp=ENABLED` |
  | Scale | desiredCount 0/1 |
  | Access control | operator IP/32 SG |
  | Logs retention | 7 days |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | Appended section | Full block 3. ECS Fargate porting: complete |
  | Detail blocks | 1)~6) + operating procedure + validation checklist + decision/risk mapping + factual-record scope |
  | Basis | Single reference block for afternoon 2026-06-30 |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-30 (afternoon) cross-reference | append |
  | Execution flow | ECS View → `portfolio-paper-daily-step12-17-approval` `StartExecution` |
  | Status | 🟢 **SUCCEEDED** |
  | Slack | received |
  | Step Functions structure | No change |

- 🟢 `.kiro/specs/06-secrets-and-iam/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-30 (afternoon) result | append |
  | Task role | `portfolio-paper-view-task-role` |
  | Execution role | `portfolio-paper-ecs-task-execution-role` |
  | Validation | Separation validation passed |

- 🟢 `.kiro/README.md`

  | Item | Value |
  | --- | --- |
  | Updated section | port-view / 05 spec lines in "Current Progress Summary" |
  | Reflected fact | 2026-06-30 (afternoon) first ECS Fargate validation complete |
  | Reflected identifiers | executionName + desiredCount 0 shutdown |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | Update location | Prepended to 2026-06-30 (afternoon) section |
  | Length | 5~10-line summary |
  | Distinction | Separate section from morning Local View validation |
  | Exit condition | Includes ECS service desiredCount 0 shutdown |

### Security

| Item | Result |
| --- | --- |
| Raw secrets (KIS app key/secret · account number · token · RDS password · IAM/state machine ARN · Slack webhook URL, etc.) | 0 |
| AWS CLI · boto3 · psql · Spring Boot · external API execution | 0 |
| Raw CloudWatch · SFN execution history · Lambda / KIS response body · Spring log · Slack payload · DB after-check · ENI/TASK/LOG_STREAM quoted | 0 |
| New broker orders (BUY / SELL / cancel / modify) | 0 |
| Automatic fill · position sync retries | 0 |
| aws-live work | 0 |
| commit · add · reset · checkout · stash | 0 |

> broker · KIS calls were limited to one morning Step 1~11 auto-trigger run + one morning Local View → Step 12~17 approval + one afternoon ECS View → Step 12~17 approval on this date (all `SUCCEEDED` / NO_TARGET / 0 broker order submissions) + balance refresh only.

<details>
<summary>🔵 Summary of operational identifiers recorded as facts</summary>

- ECS Fargate — cluster `portfolio-paper-cluster` · service `portfolio-view-service` · task def `portfolio-view:2` · ECR `portfolio-view` · Logs `/ecs/portfolio-view` · SG `sgroup-port-view-ecs`.
- IAM — task execution role `portfolio-paper-ecs-task-execution-role` · task role `portfolio-paper-view-task-role`.
- Task parameters — launch type `FARGATE` · awsvpc · cpu 512 · memory 1024 · container port 8080.
- Step Functions — executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` · state machine `portfolio-paper-daily-step12-17-approval` · status `SUCCEEDED` · start `2026-06-30T14:15:42.899+09:00` · stop `2026-06-30T14:18:48.358+09:00` · Slack `DAILY_EXECUTION_SUCCESS`.
- balance snapshot — `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505`.
- placeholders — `[REDACTED]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_PUBLIC_IP]` · `[REDACTED_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_SECRET_ARN]` · `[REDACTED_BROKER_ORDER_NO]`.

</details>

## 2026-06-30 (afternoon) (Slack wording improvements fully completed — Approval Required Builder + Daily Brief Builder + notifier formatter + mini Step Functions + pre/post-market Schedulers)

### Added

- 🟢 **Added completed Approval Required Slack Builder integration**

  | Item | Value |
  | --- | --- |
  | Builder Lambda | `portfolio-approval-slack-summary-builder` (new) |
  | State machine | `portfolio-paper-daily-step1-17-approval` |
  | Flow integration | `StopCrawlerEc2AfterStep11Success → BuildApprovalSlackPayload → SendApprovalRequiredSlack → Step12_CheckApproval` |
  | revisionId | `da8642c6-8409-41b6-ad57-e066ff672332` |
  | Smoke | Builder output → Notifier Slack 🟢 **Success** |
  | Slack content | `APPROVAL_REQUIRED` · color `#ECB22E` · `marketStatusCode=BLOCK` · `marketStatusLabel=차단` |
  | Display items | Block reason · Daily buy signal 0/0 · no Daily position decision · no buy/sell candidates |

  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Added Daily Brief Slack Builder Lambda**

  | Item | Value |
  | --- | --- |
  | Builder Lambda | `portfolio-daily-brief-slack-summary-builder` (new) |
  | Runtime | Python 3.12 + `pg8000` + Secrets Manager `valueFrom` (does not use `psycopg2`) |
  | Events | Two types: `MORNING_BRIEF` · `EVENING_BRIEF` |
  | Query basis | Latest `connector_balance_snapshot` + same-`as_of_date` `connector_position_snapshot` + `quantity > 0` |
  | Sort | evaluation amount descending · ticker code ascending · null/blank handled as `-` |
  | Smoke | `MORNING_BRIEF` + `EVENING_BRIEF` invoke 🟢 **Success** |
  | Balance snapshot | `id=281` · `as_of_date=2026-06-30` |
  | Amounts | `total_eval_amount=8,706,505원` · `cash_balance=8,706,505원` |
  | P/L | `cumulativeProfitRate=-12.94%` · `cumulativeProfitAmount=-1,293,495원` · `positionCount=0` |
  | Evening delta | `0원` |

  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Added Daily Brief mini Step Functions**

  | Item | Value |
  | --- | --- |
  | State machine | `portfolio-daily-brief-slack-notification` 🟢 **ACTIVE** |
  | Flow | `BuildDailyBriefPayload → SendSlackNotifier` |
  | IAM Roles (new) | `portfolio-daily-brief-sfn-role` · `portfolio-daily-brief-scheduler-role` |
  | Date | 2026-06-30 |

  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Daily Brief morning smoke passed**

  | Item | Value |
  | --- | --- |
  | executionName | `daily-brief-morning-smoke-safe-20260630-193255-68f50aeb` |
  | Status | 🟢 **SUCCEEDED** |
  | Date | 2026-06-30 |
  | Pre-market Slack notification | Receipt confirmed |

  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Daily Brief evening smoke passed**

  | Item | Value |
  | --- | --- |
  | executionName | `daily-brief-evening-smoke-safe-20260630-193300-aa2b9a12` |
  | Status | 🟢 **SUCCEEDED** |
  | Date | 2026-06-30 |
  | Post-market Slack notification | Receipt confirmed |

  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Added and ENABLED two pre/post-market EventBridge Schedulers**

| Item | Value |
| --- | --- |
| `portfolio-daily-brief-morning-slack-0750-kst` | **cron**: `cron(50 7 ? * MON-FRI *)`<br>**Timezone**: Asia/Seoul<br>**Flexible**: OFF<br>**Input eventType**: `MORNING_BRIEF`<br>**State**: 🟢 **ENABLED** |
| `portfolio-daily-brief-evening-slack-1550-kst` | **cron**: `cron(50 15 ? * MON-FRI *)`<br>**Timezone**: Asia/Seoul<br>**Flexible**: OFF<br>**Input eventType**: `EVENING_BRIEF`<br>**State**: 🟢 **ENABLED** |

  - No fixed `runDate` injected into Scheduler input · Builder processes based on KST at execution time.
  - Operates independently of MarketConnector EC2 start · stop.
  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Added completed live-event integration result for intraday stop-loss `INTRADAY_STOP_LOSS` Slack (additional afternoon work)**

  | Item | Value |
  | --- | --- |
  | Notifier Lambda | `portfolio-event-notifier` |
  | Formatter | Confirmed support for `INTRADAY_STOP_LOSS` |
  | Slack wording | Confirmed receipt of `🚨 [장중 손절]` |
  | `evalProfitRate` display | `%` suffix applied |
  | Local smoke | Confirmed display `🔵 -4.2%` |
  | EC2 IAM invoke smoke | Receipt confirmed |

  - Details: [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md)

- 🟢 **Deployed replacement MarketConnector evaluate script**

  | Item | Value |
  | --- | --- |
  | File | `/home/ec2-user/apps/port-marketconnector/src/connector_intraday_position_evaluate.py` |
  | Version | `connector-intraday-position-evaluate-1.1.1-slack-notify` |
  | New CLI options | `--notify-slack` · `--slack-function-name` · `--slack-region` |
  | SHA256 hash validation | Passed |
  | Static validation | `py_compile` + `--help` passed |
  | Date | 2026-06-30 |

  - Details: [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md)

- 🟢 **Updated intraday runner**

  | Item | Value |
  | --- | --- |
  | File | `/home/ec2-user/apps/port-marketconnector/scripts/run_intraday_snapshot_and_evaluate.sh` |
  | Connected options | `--create-order --notify-slack` |
  | Backup | `.bak.20260630T112255Z.create-order-notify-slack` |
  | SHA256 hash validation | Passed |
  | Syntax validation | Passed |
  | Date | 2026-06-30 |

  - Details: [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md)

- 🟢 **One safe validation run of the intraday runner**

  | Item | Value |
  | --- | --- |
  | commandId | `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` |
  | Result | `EMPTY_NORMAL` |
  | Marker | `INTRADAY_SNAPSHOT_AND_EVALUATE=SUCCESS` |
  | Exit code | `0` |
  | `open_position_count` | `0` |
  | Completion | Normal termination with 0 OPEN positions |
  | Date | 2026-06-30 |

  - Details: [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md)

- 🟢 **Added MarketConnector EC2 IAM permission result**

  | Item | Value |
  | --- | --- |
  | Role | `portfolio-paper-marketconnector-ec2-role` |
  | Inline policy | `portfolio-paper-marketconnector-event-notifier-invoke` |
  | Action | `lambda:InvokeFunction` |
  | Resource | Restricted to `portfolio-event-notifier` |
  | Wildcard | Resource · Action 🟢 **0** |
  | EC2 invoke smoke | 🟢 **Success** |

  - Details: [06 operation-notes](specs/06-secrets-and-iam/operation-notes.md)

- 🟢 **Added successful intraday stop-loss DB after-check result**

  | Item | Value |
  | --- | --- |
  | Marker | `STEP19C_INTRADAY_STOP_FINAL_DB_AFTER_CHECK=SUCCESS` |
  | `TODAY_INTRADAY_CHECKS` | 0 |
  | `TODAY_INTRADAY_STOP_EXECUTION_ORDERS` | 0 |
  | `ACTIVE_INTRADAY_STOP_EXECUTION_ORDERS` | 0 |
  | `TODAY_INTRADAY_STOP_CONNECTOR_ORDERS` | 0 |
  | `TODAY_INTRADAY_STOP_CONNECTOR_ORDER_ROWS` | 0 rows |
  | Latest `connector_balance_snapshot` | `id=281` |
  | `as_of_date` | `2026-06-30` |
  | `as_of_ts` | `2026-06-30 11:23:34.973842+00` |
  | `total_eval_amount` | `8,706,505` |
  | `cash_balance` | `8,706,505` |
  | `source_version` | `connector-intraday-snapshot-refresh-1.0.0` |
  | PSQL exit code | `0` |

  - DB validation queries used only columns confirmed in advance through `information_schema.columns` (0 guessed column names).
  - Details: [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md)

### Changed

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | OD-MS-030 | Evidence reinforced with no body change |
  | Existing policy | Three `portfolio-event-notifier` types (`APPROVAL_REQUIRED` · `DAILY_EXECUTION_SUCCESS` · `DAILY_EXECUTION_FAILED`) |
  | Expansion on this date | Approval Required + two Daily Brief aliases + nested adapter + P/L prefix rule |
  | Existing View SlackNotificationService | Retained |
  | OD-MS-038 | New (Daily Brief Slack mini workflow) |
  | Status | 🟢 **확정** |
  | Affected specs | 04 · 05 · 10 |
  | Decision Summary | total 96 → 97 · 확정 51 → 52 · 잠정 42 unchanged |
  | Change Log | Added 2026-06-30 (afternoon) Slack entry |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | R-AUTO-035 | New (risk of Daily Brief Slack automatic-send failure or duplicate delivery) |
  | Impact | Medium |
  | Probability | Medium |
  | Mitigation | Separate mini Step Functions · 2 Schedulers ENABLED · manual smoke 🟢 **SUCCEEDED** · confirmed Slack receipt · CloudWatch / SFN audit |
  | Detection | 07:50/15:50 Slack receipt · SFN execution status · Lambda CloudWatch Logs |
  | Rollback | Scheduler DISABLED or manual SFN execution |
  | Affected Spec | 04, 05, 10 |
  | Status | 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | Existing "separate pre-market / post-market Slack follow-up" | Moved to completed on this date |
  | New follow-ups | 7 registered |
  | Follow-up 1 | Confirm automatic 07:50 pre-market Slack receipt on the next weekday |
  | Follow-up 2 | Confirm automatic 15:50 post-market Slack receipt on the next weekday |
  | Follow-up 3 | Confirm automatic rich `APPROVAL_REQUIRED` Slack receipt on the next Step1~11 run |
  | Follow-up 4 | Reconfirm per-position P/L · return display when holdings exist |
  | Follow-up 5 | Reinforce `DAILY_EXECUTION_SUCCESS` summary |
  | Follow-up 6 | Define cause-truncation policy for `DAILY_EXECUTION_FAILED` |
  | Follow-up 7 | Validate `INTRADAY_STOP_LOSS` live-event integration / Daily Brief Slack live validation in spec 10 |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | Item | Reinforcement target |
  | --- | --- |
  | Lambda | `portfolio-event-notifier` · `portfolio-approval-slack-summary-builder` · `portfolio-daily-brief-slack-summary-builder` |
  | Step Functions | `portfolio-daily-brief-slack-notification` mini workflow |
  | EventBridge Scheduler | Two Daily Brief Schedulers at 07:50 / 15:50 |
  | Secrets Manager | password-injection method based on `DB_PASSWORD_SECRET_VALUE_FROM` valueFrom |
  | Plaintext secret recording | 0 (aligned with R-DOCS-001) |

- 🟢 `.kiro/specs/_common/cost-simulation.md`

  | Item | Value |
  | --- | --- |
  | Daily Brief Slack automation cost | 2 Lambdas + small number of SFN transitions + 2 Schedulers |
  | Monthly cost | Very small |
  | Fargate · ALB · NAT cost | Not related |
  | Exact amount | Retain note to recheck with AWS Pricing Calculator |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Target row | port-view 4.2 |
  | Notification helper-layer expansion | Lambda notifier + Builder Lambda + mini Step Functions |
  | Daily Batch orchestration | Step Functions + EventBridge decision retained |
  | Daily Brief path | Separated into dedicated mini Step Functions (not coupled to order-execution path) |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | Approval Builder integration | Complete |
  | State machine | `portfolio-paper-daily-step1-17-approval` |
  | ASL flow integration | `BuildApprovalSlackPayload → SendApprovalRequiredSlack` |
  | revisionId | Recorded as fact |
  | Slack smoke | 🟢 **Passed** |
  | Follow-up | Confirm automatic Approval Required Slack receipt on the next actual Step1~11 run |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | Daily Brief Slack automation | Separate phase from the afternoon ECS Fargate first port + Step 12~17 approval |
  | MarketConnector EC2 start · stop | Operates independently from Daily Brief Slack |

- 🟢 `.kiro/specs/06-secrets-and-iam/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | Two IAM Roles | `portfolio-daily-brief-sfn-role` · `portfolio-daily-brief-scheduler-role` |
  | Builder Lambda injection method | `DB_PASSWORD_SECRET_VALUE_FROM` + Secrets Manager `get_secret_value` |
  | Plaintext secret recording | 0 |
  | Additional afternoon Role | `portfolio-paper-marketconnector-ec2-role` |
  | Additional inline policy | `portfolio-paper-marketconnector-event-notifier-invoke` |
  | action | `lambda:InvokeFunction` |
  | Resource | Restricted to `portfolio-event-notifier` |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (additional afternoon work)

  | Item | Value |
  | --- | --- |
  | Responsibility for creating intraday stop-loss READY | Delegated to MarketConnector EC2 runner |
  | Runner path | `/home/ec2-user/apps/port-marketconnector/scripts/run_intraday_snapshot_and_evaluate.sh` |
  | Actual broker order submission | Remains responsibility of `portfolio-paper-intraday-stop-sell-approval` SFN approval gate |

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md` (additional afternoon work)

  | Item | Value |
  | --- | --- |
  | evaluate replacement deployment | `connector-intraday-position-evaluate-1.1.1-slack-notify` |
  | runner update | `--create-order --notify-slack` |
  | SHA256 hash validation | Passed |
  | EC2 invoke smoke | Passed |
  | One safe runner validation | commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` · `EMPTY_NORMAL` |

- 🟢 `.kiro/specs/_common/operator-decisions.md` (additional afternoon work)

  | Item | Value |
  | --- | --- |
  | Change Log | Added `2026-06-30 (오후) 장중 손절 Slack` entry |
  | Body changes | None (evidence reinforcement only for OD-MS-030 · OD-MS-035 · OD-MS-036 · OD-MS-038) |
  | New decisions | None |
  | Decision Summary | total 97 · 확정 52 · 잠정 42 unchanged |

- 🟢 `.kiro/specs/_common/risk-register.md` (additional afternoon work)

  | Item | Value |
  | --- | --- |
  | R-AUTO-035 | Added [2026-06-30 afternoon additional reinforcement] note to mitigation |
  | Reinforcement | MarketConnector EC2 runner is also an entry point for Notifier Lambda invoke |
  | Real automatic-fire validation | Follow-up |
  | Status | 🟢 **Mitigated** unchanged |
  | R-AUTO-036 | New (risk of no rollback if Slack fails after intraday stop-loss READY creation) |
  | Impact | Medium |
  | Probability | Low |
  | Mitigation | Lambda CloudWatch Logs · SFN execution audit · DB after-check |
  | Affected Spec | 03, 04, 06, 10 |
  | Status | 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/followups-overview.md` (additional afternoon work)

  | Item | Value |
  | --- | --- |
  | `INTRADAY_STOP_LOSS` actual intraday-position event integration | Moved to complete |
  | New follow-ups | 4 |
  | Follow-up 1 | Confirm live-event receipt after an actual holding meets the hard-stop condition |
  | Follow-up 2 | Reconfirm READY creation + approval-gate blocked state |
  | Follow-up 3 | Review whether to add account · current price · entry price · expected P/L amount to Slack message |
  | Follow-up 4 | Review whether to add a separate approval-summary Slack after intraday stop-loss READY creation |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md` (additional afternoon work)

  | Item | Reinforcement target |
  | --- | --- |
  | Lambda | MarketConnector EC2 → `portfolio-event-notifier` invoke path · `INTRADAY_STOP_LOSS` formatter |
  | IAM Role | `portfolio-paper-marketconnector-ec2-role` inline policy |
  | Step Functions | Separate `portfolio-paper-intraday-stop-sell-approval` approval gate |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md` (additional afternoon work)

  | Item | Value |
  | --- | --- |
  | Target rows | port-marketconnector / `port_strategy_execution` |
  | MarketConnector EC2 runner responsibility | intraday snapshot refresh · position evaluate · READY creation · Slack notify |
  | broker order submission | Maintain separation through SFN approval gate |
  | Lambda location | Maintain Slack notifier / summary builder helper layer |

- 🟢 `.kiro/specs/_common/cost-simulation.md` (additional afternoon work)

  | Item | Value |
  | --- | --- |
  | Lambda invoke · SFN transitions · Slack notify | All low-frequency |
  | EC2 MarketConnector | Reuse existing resource |
  | New ongoing cost | 0 meaningful increase |
  | Fargate · ALB · NAT cost | Not related |
  | Exact amount | Retain note to recheck with AWS Pricing Calculator |

- 🟢 `.kiro/AGENTS.md`

  | Item | Value |
  | --- | --- |
  | New rules | 4 types |
  | Rule 1 | Strengthen requirement to check psql `information_schema.columns` in advance (guessing nonexistent column names <span style="color:#D1242F">**Prohibited**</span>) |
  | Rule 2 | Printing a SUCCESS marker after a failed command / SQL <span style="color:#D1242F">**Prohibited**</span> |
  | Rule 3 | When a PowerShell native command is not found / psql fails, distinguish `$LASTEXITCODE` from `$?` |
  | Rule 4 | Handle cp949 encoding errors for emoji / special characters in Windows AWS CLI stdout (prefer EC2-side sanitization or status-only queries) |
  | Rule 5 | Use UTF-8 No BOM JSON + `--parameters file://...` pattern for SSM multiline commands |
  | Rule 6 | Including DB password / secret values in chat / docs / logs / command examples <span style="color:#D1242F">**Prohibited**</span> |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | Update location | End of 2026-06-30 (afternoon) section |
  | Content | 5~10-line final completion summary of intraday stop-loss Slack live-event integration |
  | Accumulated | decisions / risks / follow-ups |

- 🟢 `.kiro/README.md`

  | Item | Value |
  | --- | --- |
  | Update location | "Current progress summary" section |
  | Reflected fact | Intraday stop-loss integration complete in Slack-improvement row |
  | Validation logs | Not included |

### Security

| Item | Result |
| --- | --- |
| Plaintext secrets (Slack webhook URL · DB password · IAM ARN · state machine ARN · KIS app key/secret · account number · token) | 0 |
| AWS · Lambda · SFN · Scheduler · IAM · RDS · Secrets Manager · Slack webhook · KIS API execution | 0 |
| Full plaintext quotation of Lambda code · SFN ASL · Scheduler target JSON · Slack payload · Builder output · Notifier input · CloudWatch Logs · IAM Policy body | 0 |
| New broker · KIS calls | 0 |
| Slack live-event sends other than smoke | 0 |
| commit · add · reset · checkout · stash | 0 |

> <span style="color:#D1242F">A history of a DB password being accidentally exposed in an operator-local session is retained only at the level of "credential rotation / history cleanup recommended," without recording the password value (R-SEC-010 / R-DOCS-001 / R-DOCS-002 · follow-up in spec 06).</span>

For the Slack webhook URL, only the environment-variable name (`SLACK_WEBHOOK_URL`) is recorded. Migration to Secrets Manager or SSM SecureString is planned after operational stabilization (R-AUTO-024 · follow-up in spec 06).

<details>
<summary>🔵 Recorded operational identifiers</summary>

- Slack improvements — Builder Lambdas `portfolio-approval-slack-summary-builder` · `portfolio-daily-brief-slack-summary-builder` · Notifier Lambda `portfolio-event-notifier`.
- mini SFN — `portfolio-daily-brief-slack-notification` (revisionId `da8642c6-8409-41b6-ad57-e066ff672332`).
- IAM Roles — `portfolio-daily-brief-sfn-role` · `portfolio-daily-brief-scheduler-role`.
- Schedulers — `portfolio-daily-brief-morning-slack-0750-kst` · `portfolio-daily-brief-evening-slack-1550-kst` · cron `cron(50 7 ? * MON-FRI *)` / `cron(50 15 ? * MON-FRI *)` · Asia/Seoul.
- eventType labels — `MORNING_BRIEF` · `EVENING_BRIEF` · `PRE_MARKET_STATUS` · `POST_MARKET_STATUS` · `APPROVAL_REQUIRED` · `DAILY_EXECUTION_SUCCESS` · `DAILY_EXECUTION_FAILED` · `INTRADAY_STOP_LOSS`.
- Intraday stop-loss · SSM commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` · IAM Role `portfolio-paper-marketconnector-ec2-role` · inline policy `portfolio-paper-marketconnector-event-notifier-invoke`
  state machine `portfolio-paper-intraday-stop-sell-approval` · 3 markers `STEP19C_INTRADAY_STOP_FINAL_DB_AFTER_CHECK=SUCCESS` · `INTRADAY_SNAPSHOT_AND_EVALUATE=SUCCESS` · `EMPTY_NORMAL`.
- balance snapshot — `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505` · `cumulativeProfitRate=-12.94%` · `cumulativeProfitAmount=-1,293,495` · `positionCount=0`.
- Detailed evidence — [03](specs/03-marketconnector-ec2/operation-notes.md) · [04](specs/04-strategy-batch-stepfunctions/operation-notes.md) · [05](specs/05-port-view-ecs-and-runbook/operation-notes.md) · [06](specs/06-secrets-and-iam/operation-notes.md).

</details>

## 2026-06-30 (Local View → AWS Step Functions Step 12~17 approval execution validation passed + Daily Batch gate correction + four View operating paths organized)

### Added

- 🟢 **Added successful result of the morning scheduled AWS Step Functions Step 1~11 run**

  | Item | Value |
  | --- | --- |
  | Trigger | Existing EventBridge Scheduler · Dispatcher Lambda automatic trigger path (aligned with OD-MS-032) |
  | Step 12~17 policy | Keep separate approval state machine + paper-order gate |
  | 09:01 automatic ENABLE | Deferred (OD-MS-033) |

  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Added successful Local View → AWS Step Functions Step 12~17 approval execution result**

  | Item | Value |
  | --- | --- |
  | Precondition | Enabled in alignment with operator intent after correcting the Daily Batch gate (`DailyBatchController.java`) |
  | executionName | `port-view-daily-step12-17-20260630-095111-aae2595c` |
  | State machine | `portfolio-paper-daily-step12-17-approval` |
  | Trigger | Local View · AWS Step Functions approval-range button |
  | Status | 🟢 **SUCCEEDED** |
  | Start | `2026-06-30T09:51:11.903+09:00` |
  | Stop | `2026-06-30T09:54:16.484+09:00` |
  | Completion judgment | Safely terminated with no order target |

  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Added successful DB post-validation result**

  | Item | Value |
  | --- | --- |
  | New `connector_order_request` | 0 |
  | Remaining REQUESTED `strategy_execution_order` | 0 |
  | Active `connector_order_request` | 0 |
  | Latest `connector_balance_snapshot` | `id=281` |
  | `as_of_date` | `2026-06-30` |
  | `total_eval_amount` | `8,706,505` |
  | `cash_balance` | `8,706,505` |
  | `eval_profit` | `0` |
  | `source_version` | `connector-intraday-snapshot-refresh-1.0.0` |
  | Holdings | 0 |

  - `connector_position_snapshot` was validated by `account_no` + `as_of_date` because the `balance_snapshot_id` column does not exist.
  - Details: [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md)

- 🟢 **Added result of organizing the four View operating paths**

  | Path | Date · Run |
  | --- | --- |
  | (a) Local View → Local File Step 1 only | 2026-06-28 Run #46 |
  | (b) Local View → Local File Step 1~11 | 2026-06-28 Run #47 |
  | (c) Local View → Local File Step 12~17 | 2026-06-29 Run #48 |
  | (d) **Local View → AWS Step Functions Step 12~17 approval execution** | **This date** |

  - Confirmed that Local File execution and AWS Step Functions execution operate separately.
  - Confirmed that the order-capable Step 12~17 range executes through a separate approval state machine + paper-order gate.
  - Details: [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md)

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | R-AUTO-033 | Added [2026-06-30 reinforcement] mitigation note |
  | Evidence | First empirical validation of Daily Batch gate alignment with operator intent + AWS Step Functions approval execution |
  | Gate combination | `fullPipelineExecutionEnabled=true` + `paperOrderEnabled=true` |
  | Alignment fact | Separate activation conditions for the AWS Step 1~11 + AWS Step 12~17 approval buttons and the Local File gate |
  | Status | 🟢 **Mitigated** |
  | R-AUTO-034 | Added [2026-06-30 reinforcement] mitigation note |
  | Evidence | First empirical validation of four View operating paths + reinforced DB-validation query-authoring principles |
  | Remaining | Separation of Fargate Task Role permissions remains the responsibility of a follow-up phase in spec 06 |
  | Status | 🟠 **Open** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-30 follow-up note | Added |
  | Completed | 5 items |
  | Follow-ups | 7 items |
  | New record | DB-validation query-authoring principles |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | Append scope | 2026-06-30 §1~§6 |
  | Morning run | Step 1~11 automatic trigger passed |
  | Morning run (View) | Second empirical validation of Local View → approval workflow manual operator trigger |
  | Gate alignment | Confirmed Daily Batch gate alignment with operator intent |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | Append 8) | Completed validation of four View operating paths |
  | Append 9) | Recorded Daily Batch gate correction aligned with operator intent |
  | Append 10) | Added DB-validation query-authoring principles |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | Update location | Prepended to the 2026-06-30 section |
  | Length | 5~10-line summary |
  | Included facts | Daily Batch gate correction · executionName · DB post-validation · four operating paths organized |

### Security

| Item | Result |
| --- | --- |
| Plaintext secrets (KIS app key/secret · account number · token · RDS password · IAM/state machine ARN · Slack webhook URL, etc.) | 0 |
| AWS CLI · boto3 · psql · Spring Boot · external API execution | 0 |
| New AWS resource creation · modification · deletion | 0 |
| Full plaintext quotation of CloudWatch · SFN execution history · Lambda / KIS response body · Spring log · Slack payload · DB post-validation raw output | 0 |
| New broker · KIS BUY / SELL / cancel / modify | 0 |
| Automatic fill · position-sync retry | 0 |
| aws-live work | 0 |
| commit · add · reset · checkout · stash | 0 |

> broker · KIS calls = limited to the morning Step 1~11 automatic trigger (`connector_order_request` new rows 0) + one Local View → AWS Step Functions Step 12~17 approval execution (`SUCCEEDED` / NO_TARGET / broker order submissions 0) + balance refresh.

<details>
<summary>🔵 Recorded operational identifiers</summary>

- Step Functions — executionName `port-view-daily-step12-17-20260630-095111-aae2595c` · state machine `portfolio-paper-daily-step12-17-approval`.
- Spring — Controller `DailyBatchController` · 6 gate labels (`executionEnabled` · `localFileExecutionEnabled` · `fullPipelineExecutionEnabled` · `paperOrderEnabled` / allowed range `1~17` / screen labels `Execution ON` · `Local File OFF` · `Full Pipeline ON` · `Paper Order ON`).
- Balance snapshot — `id=281`.
- DB column names — `balance_snapshot_id` (absent) · `account_no` · `as_of_date`.
- Pipeline runs — `#46` · `#47` · `#48` · four operating-path labels.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-29 (3) (port-view Step 12~17 approval validation completed + Local View wrappers organized + Approval state machine ARN separated)

### Added

- 🟢 Local View → AWS Step Functions Step 12~17 approval execution validation

  | Item | Value |
  | --- | --- |
  | Validation phase | Second phase in which Local View connects directly to Step Functions `StartExecution` as an external caller, rather than through the Daily Pipeline |
  | executionName | `port-view-step12-17-step12-17-20260629-194314-ba5edaf8` |
  | State Machine | `portfolio-paper-daily-step12-17-approval` |
  | Execution status | `SUCCEEDED` |
  | Start time | `2026-06-29T19:43:15.673+09:00` |
  | Stop time | `2026-06-29T19:46:06.546+09:00` |
  | Start gate | Passed `Step12_CheckApproval` |
  | Step 12 execution | `Step12_RunMarketConnectorStrategyOrderExecute` |
  | Step 12 confirmation | `Step12_GetCommandInvocation` |
  | Subsequent flow | Full Step 13~17 flow executed |
  | Final State | `ExecutionSucceeded` |
  | DB post-validation marker | `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS` |
  | New `connector_order_request` | 0 |
  | READY·REQUESTED `strategy_execution_order` | 0 |
  | New broker orders | 0 |
  | Details | [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |

- 🟢 Added AWS Step 12~17 approval execution endpoint and button

  | Item | Value |
  | --- | --- |
  | Controller endpoint | `POST /daily-batch/aws-stepfunctions/start-approval-range` |
  | View file | `daily_batch.html` |
  | Button structure | Separate safe-execution and approval-execution buttons |
  | Activation condition | Apply separate safe · approval gates |
  | `requestedBy` | `VIEW_APPROVAL_BUTTON` |
  | `allowPaperOrderExecute` | `true` |
  | `paperOrderEnabled` | `true` |
  | payload type | Preserve boolean |
  | Success display | Show `executionName` |
  | ARN display | Show redacted `executionArn` |

- 🟢 Separated dedicated State Machine ARN for Step 12~17

  | Item | Value |
  | --- | --- |
  | General workflow | `portfolio-paper-daily-step1-17-approval` |
  | Approval workflow | `portfolio-paper-daily-step12-17-approval` |
  | properties key | `portfolio.batch.aws-stepfunctions-approval-state-machine-arn` |
  | Environment variable | `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_APPROVAL_STATE_MACHINE_ARN` |
  | Configuration class | `DailyBatchProperties` |
  | New field | `awsStepfunctionsApprovalStateMachineArn` |
  | accessor | Added getter · setter |
  | `startSafeRange` | Uses general workflow ARN |
  | `startApprovalRange` | Uses approval-specific ARN |
  | fail-closed | Block approval execution when approval ARN is empty |

- 🟢 Organized two local View startup wrappers

  | Item | Value |
  | --- | --- |
  | Local File wrapper | `Start-PortfolioViewAwsPaperLocalFile.ps1` |
  | Local File mode | `PORTFOLIO_BATCH_EXECUTION_MODE=local-file` |
  | Local File gate | Local-file-related gates ON |
  | Step Functions gate | OFF |
  | Step Functions wrapper | `Start-PortfolioViewAwsPaperStepFunctions.ps1` |
  | Step Functions mode | `PORTFOLIO_BATCH_EXECUTION_MODE=aws-stepfunctions` |
  | Step Functions gate | ON |
  | ARN injection | Set both general ARN and approval ARN |
  | Common profile | `aws-paper` |
  | Executable range | Full Step 1~17 |
  | Local File env loader | `Load-PortfolioViewAwsPaperLocalFileEnv.ps1` |
  | Step Functions env loader | `Load-PortfolioViewAwsPaperStepFunctionsEnv.ps1` |
  | Wrapper location | `C:\Workspaces\portfolio-local-env\` |
  | Spec scope | Wrapper bodies are operator-local tools and outside this spec scope |

- 🟢 Updated 05 spec operation notes

  | Item | Value |
  | --- | --- |
  | Target document | `specs/05-port-view-ecs-and-runbook/operation-notes.md` |
  | New completed section 4 | Step 12~17 approval validation completed |
  | New completed section 5 | Local View startup wrappers organized |
  | Existing incomplete items | Updated to complete |
  | renumber | Moved Docker·ECR·Task Definition·Fargate validation to 6)·7) |

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | R-AUTO-033 | Added [2026-06-29 reinforcement (3)] mitigation note |
  | Evidence | Separate Step 12~17 approval state machine + first successful empirical validation of boolean / numeric payload |
  | Status | 🟢 **Mitigated** |
  | R-AUTO-034 | Added [2026-06-29 reinforcement] mitigation note |
  | Evidence | Separate approval ARN + first empirical validation of service-level safety gate |
  | Remaining | Separation of Fargate Task Role permissions remains responsibility of a follow-up phase in spec 06 |
  | Status | 🟠 **Open** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-29 (3) follow-up note | Added |
  | Completed | 5 items |
  | Completed 1 | Step 12~17 approval validation passed |
  | Completed 2 | Approval ARN separated |
  | Completed 3 | Payload boolean / numeric alignment |
  | Completed 4 | Two local View wrappers organized |
  | Completed 5 | 05 spec operation-notes updated |
  | Resolved | 3 items (blocked string boolean payload · wrong ARN call · safe-only gate left incorrectly enabled) |
  | New follow-ups | 4 items (2026-04-27 삼성전자 stale ACCEPTED cleanup · initial Fargate `paperOrderEnabled` default · ALB source IP · minimum authentication · CloudWatch Logs · alarms · failure Slack) |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Body changes | None |
  | Evidence reinforcement | `OD-MS-002` · `OD-MS-009` · `OD-MS-037` · `OD-SAFE-001` ~ `OD-SAFE-004` |
  | Change Log | Added 2026-06-29 (3) entry |
  | Decision Summary | Remains total 96 · 확정 51 · 잠정 42 |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Target row | port-view 4.2 |
  | Step 12~17 approval | Separate state machine ARN |
  | payload | Align boolean / numeric types |
  | Local wrappers | Two wrappers organized |
  | Fargate Task Role `states:StartExecution` | Policy grants both general + approval ARNs with Resource scoping |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | Item | Value |
  | --- | --- |
  | Supported Step Functions entry paths | Two paths: Step 1~11 safe + Step 12~17 approval |
  | Choice condition | `Step12_CheckApproval` `BooleanEquals` |
  | boolean payload | `allowPaperOrderExecute` · `paperOrderEnabled` |
  | numeric payload | `fromStepOrder` · `toStepOrder` · `startStep` · `endStep` |
  | Fargate Task Role permission separation | Both general + approval ARNs are Resource-scoped |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | "7. ECS Fargate 포팅 구현" 4) | Step 12~17 approval validation completed |
  | 5) | Local View startup wrappers organized |
  | 6) · 7) | Docker · ECR · Task Definition · Fargate validation renumbered |
  | Conclusion section | Added |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | Append scope | 2026-06-29 (3) §1~§5 |
  | Empirical fact | Second phase in which View connects to `portfolio-paper-daily-step12-17-approval` as an external caller |
  | payload alignment | Boolean type aligned with `Step12_CheckApproval` `BooleanEquals` Choice condition |
  | Policy changes | None |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | Update location | Prepended to the 2026-06-29 (3) section |
  | Length | 5~10-line summary |
  | Included facts | Step 12~17 approval + ARN separation + payload boolean reinforcement + wrapper organization + 3 resolved items |

### Security

| Item | Result |
| --- | --- |
| Plaintext secrets (KIS app key/secret · account number · token · RDS password · IAM/state machine ARN · Slack webhook URL, etc.) | 0 |
| AWS · Lambda · SFN · EventBridge · SSM · EC2 · RDS · S3 execution | 0 |
| AWS CLI · boto3 · psql · Spring Boot · external API execution | 0 |
| New AWS resource creation · modification · deletion | 0 |
| Full plaintext quotation of CloudWatch · SFN execution history · Lambda / KIS response body · Spring log · Slack payload · commit diff · wrapper body | 0 |
| New broker · KIS BUY / SELL / cancel / modify | 0 |
| Automatic fill · position-sync retry | 0 |
| aws-live work | 0 |
| commit · add · reset · checkout · stash | 0 |

> broker · KIS calls = one Step 12 `--execute` (<span style="color:#0969DA">**NO_TARGET**</span>) + one Step 13 active connector-order query (targets 0) + Step 17 balance refresh only. `connector_order_request` new rows 0 / broker order submissions 0.

<details>
<summary>🔵 Recorded operational identifiers</summary>

- port-view executionName `port-view-step12-17-step12-17-20260629-194314-ba5edaf8`.
- Operational marker `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS`.
- Two state machines — `portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval`.
- Four state names — `Step12_CheckApproval` · `Step12_RunMarketConnectorStrategyOrderExecute` · `Step12_GetCommandInvocation` · `ExecutionSucceeded`.
- Two Controller endpoints — `/daily-batch/aws-stepfunctions/start-range` · `/daily-batch/aws-stepfunctions/start-approval-range`.
- Seven Spring properties keys — existing 6 + `portfolio.batch.aws-stepfunctions-approval-state-machine-arn`.
- Four PowerShell wrappers — `Start-PortfolioViewAwsPaperLocalFile.ps1` · `Load-PortfolioViewAwsPaperLocalFileEnv.ps1` · `Start-PortfolioViewAwsPaperStepFunctions.ps1` · `Load-PortfolioViewAwsPaperStepFunctionsEnv.ps1`.
- DB URL `jdbc:postgresql://127.0.0.1:15433/portfolio` · View DB user `view_app` · Tomcat port `8080` · Spring profile `aws-paper`.
- start · stop timestamp `2026-06-29T19:43:15.673+09:00` ~ `2026-06-29T19:46:06.546+09:00`.
- placeholder — `[REDACTED]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_ARN]` · `[REDACTED_SECRET_ARN]` · `[REDACTED_BROKER_ORDER_NO]`.

</details>

## 2026-06-29 (2) (implemented port-view aws-stepfunctions Daily Batch trigger + local Step 1~11 StartExecution validation passed)

### Added

- 🟢 First implementation of port-view aws-stepfunctions Daily Batch trigger

  | Item | Value |
  | --- | --- |
  | Documentation reflection | Kiro documentation updated |
  | Actual code basis | port-view commit `e72de6f` |
  | commit message | `feat(view): add Step Functions daily batch trigger` |
  | Modified file | `pom.xml` |
  | Modified file | `DailyBatchProperties.java` |
  | Modified file | `DailyBatchController.java` |
  | Modified file | `StepFunctionsDailyBatchExecutionService.java` |
  | Modified file | `application-aws-paper.properties` |
  | Modified file | `daily_batch.html` |
  | AWS SDK | Added AWS SDK v2 Step Functions client |
  | backend | Split into `local-file` and `aws-stepfunctions` |
  | View execution responsibility | Does not directly execute Python subprocess |
  | Local source execution | Not executed directly |
  | AWS call | Performs Step Functions `StartExecution` only |
  | env injection | `stateMachineArn` |
  | env injection | region |
  | env injection | `executionNamePrefix` |
  | Screen button | AWS Step 1~11 safe trigger |
  | Controller endpoint | `POST /daily-batch/aws-stepfunctions/start-range` |
  | Success display | `executionName` |
  | ARN display | account-id-redacted `executionArn` flash message |

- 🟢 Validated local Step 1~11 StartExecution

  | Item | Value |
  | --- | --- |
  | View profile | `aws-paper` |
  | Execution backend | `aws-stepfunctions` |
  | Start method | Clicked AWS Step 1~11 safe trigger on screen |
  | StartExecution | Success |
  | workflow | Step 1~11 executed |
  | Step 12~17 | Order-related section remained blocked |
  | Block criterion | `allowPaperOrderExecute=false` |
  | Slack | `APPROVAL_REQUIRED` received |
  | E2E result | Passed |
  | broker order submissions | 0 |
  | New `connector_order_request` | 0 |

- 🟠 Missing `runDate` issue and fix

  | Item | Value |
  | --- | --- |
  | Initial failure location | `StopCrawlerEc2AfterStep11Success` |
  | Error | `States.Runtime` |
  | ASL reference | `runDate.$=$.runDate` |
  | Direct cause | `runDate` missing from View `StartExecution` input |
  | Modified class | `StepFunctionsDailyBatchExecutionService` |
  | Added value | `runDate` based on Asia/Seoul |
  | Format | `yyyy-MM-dd` |
  | Revalidation | Passed `StopCrawlerEc2AfterStep11Success` |
  | Final check | Passed through `SendApprovalRequiredSlack` |

- 🟠 Added new Risk `R-AUTO-034`

  | Item | Value |
  | --- | --- |
  | Risk ID | `R-AUTO-034` |
  | Risk | Excessive `states:StartExecution` permission for Fargate View |
  | Additional risk | Bypassing order-related gates for Step 12+ |
  | Status | `Open` |
  | IAM mitigation | Restrict Resource to a specific State Machine ARN |
  | Default gate | `paperOrderEnabled=false` |
  | Initial Step upper bound | `maxExecutableStepOrder=11` |
  | Step 12~17 condition | Enable only after preflight |
  | Step 12~17 condition | Enable only after approval gate |
  | Step 12~17 condition | Enable only after paper-order gate |
  | Screen security | Redact `executionArn` |
  | Screen security | Redact account-id |
  | Follow-up specs | 05·06·10 |

- 🟢 Created new 05 spec folder

  | Item | Value |
  | --- | --- |
  | Folder | `.kiro/specs/05-port-view-ecs-and-runbook/` |
  | New document | `operation-notes.md` |
  | Section 6 | ECS Fargate porting plan complete |
  | Section 7 | ECS Fargate porting implementation |
  | Implementation complete | Step Functions backend |
  | Implementation complete | `aws-stepfunctions` mode |
  | Implementation complete | Local Step 1~11 validation |
  | Implementation incomplete | Step 12~17 approval validation |
  | Implementation incomplete | Docker |
  | Implementation incomplete | ECR |
  | Implementation incomplete | ECS Task Definition |
  | Implementation incomplete | Fargate validation |

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | R-AUTO-033 | Added [2026-06-29 supplement] mitigation note |
  | Evidence | First local validation passed with port-view attached as a Step Functions `StartExecution` client |
  | Policy | Separate Step 1~11 safe trigger · keep Step 12~17 blocked |
  | Status | 🟢 **Mitigated** |
  | R-AUTO-034 | Added new row |
  | Risk | Excessive Fargate View `states:StartExecution` permission + Step 12 gate bypass |
  | Status | 🟠 **Open** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | Second 2026-06-29 follow-up note | Added |
  | Completed | port-view local aws-stepfunctions Step 1~11 validation |
  | New follow-ups | 7 |
  | Follow-up items | Dockerfile · ECR push · ECS Task Definition · ECS Service smoke · Fargate Step 1~11 StartExecution · Step 12~17 approval path · Task Role least privilege |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Body change | None |
  | Evidence supplement | `OD-MS-002` · `OD-MS-009` · `OD-MS-037` |
  | First proof | Added `StepFunctionsDailyBatchExecutionService` · first aws-stepfunctions backend implementation complete · local Step Functions trigger validated before Fargate entry |
  | Change Log | Added 2026-06-29 (2) item |
  | Decision Summary | No change |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Target row | port-view |
  | Operational-stability 1st choice | Keep ECS Fargate Service |
  | Execution orchestration | Move View internal subprocess → Step Functions `StartExecution` |
  | Preliminary validation | Local aws-stepfunctions mode complete |
  | Not-recommended judgment | Elastic Beanstalk · App Runner · Lambda unchanged |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | Item | Value |
  | --- | --- |
  | Supplement targets | Step Functions · ECS Fargate · IAM Role · IAM Policy |
  | View Fargate role | Always-on web console |
  | Actual Daily Batch execution | Step Functions `StartExecution` |
  | Task Role permission | `states:StartExecution` restricted to a specific state machine ARN |
  | `aws-stepfunctions` mode | No subprocess execution inside View |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | Fact | First local validation with port-view attached as an external SFN `StartExecution` caller |
  | Required View input | `runDate` (Asia/Seoul yyyy-MM-dd) |
  | ASL reference | `runDate.$=$.runDate` |
  | Flow validation | Step 1~11 → `StopCrawlerEc2AfterStep11Success` → `SendApprovalRequiredSlack` |
  | Step 12~17 | Approval gate / paper-order gate policy preserved |

- 🟢 `.kiro/specs/06-secrets-and-iam/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | Fargate port-view Task Role | Follow-up needed for least-privilege `states:StartExecution` (recommend restricting to state machine ARN) |
  | RDS connection information | Directly recording in image · properties <span style="color:#D1242F">**prohibited**</span> |
  | Injection method | Secrets Manager or SSM SecureString |
  | env / config injection targets | `stateMachineArn` · region · `executionNamePrefix` |
  | Secret values | Plaintext recording in documentation <span style="color:#D1242F">**prohibited**</span> |

- 🟢 Spec 10 `cutover-and-validation-runbook` folder

  | Item | Value |
  | --- | --- |
  | As of this date | Does not exist |
  | Delegation | Separated as follow-up phase responsibility |
  | Recording location | followups-overview 2026-06-29 (2) follow-up note |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | Update location | Prepended to 2026-06-29 (2) section |
  | Length | 5~10-line summary |
  | Included facts | First implementation complete · Step 1~11 validation · runDate fix · new follow-ups |

### Security

| Item | Result |
| --- | --- |
| Raw secrets (KIS app key/secret · account number · token · RDS password · IAM/state machine ARN · Slack webhook URL · DB password, etc.) | 0 |
| AWS · Lambda · SFN · EventBridge · SSM · EC2 · RDS · S3 execution | 0 |
| AWS CLI · boto3 · psql · Spring Boot · external API execution | 0 |
| New/modified/deleted AWS resources | 0 |
| Plaintext Lambda code · IAM Policy · SFN ASL · SFN execution history · CloudWatch · KIS response · Spring log · Slack payload quoted | 0 |
| New broker · KIS calls | 0 |
| aws-live work | 0 |
| commit · add · reset · checkout · stash | 0 |

> Validation was limited to direct local execution by the operator after applying port-view commit `e72de6f`. New broker order submissions: 0 · Step 12~17 approval gate remained blocked.

<details>
<summary>🔵 Summary of operational identifiers recorded as facts</summary>

- port-view commit hash `e72de6f` · commit message `feat(view): add Step Functions daily batch trigger`.
- Controller endpoint path `/daily-batch/aws-stepfunctions/start-range`.
- 6 Spring Boot properties keys · `portfolio.batch.execution-mode` · `portfolio.batch.aws-stepfunctions-region` · `portfolio.batch.aws-stepfunctions-state-machine-arn` · `portfolio.batch.aws-stepfunctions-execution-name-prefix`
  `portfolio.batch.aws-stepfunctions-start-enabled` · `portfolio.batch.aws-stepfunctions-step-start-enabled`.
- StartExecution payload — 11 items + `runDate` (Asia/Seoul yyyy-MM-dd).
- State names — `StopCrawlerEc2AfterStep11Success` · `SendApprovalRequiredSlack` · `Step6ToStep11_Succeeded` · `Step12_CheckApproval`.
- Slack event label `APPROVAL_REQUIRED` · error label `States.Runtime` · class `StepFunctionsDailyBatchExecutionService`.
- placeholders — `[REDACTED]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_ARN]` · `[REDACTED_SECRET_ARN]`.

</details>

## 2026-06-29 (View Local Batch Step 12~17 execution passed + View AWS Paper Batch common launcher organized / DB password rotation follow-up registered)

### Added

- 🟢 **Added full View Local Batch Step 12~17 execution pass result**

  | Item | Value |
  | --- | --- |
  | Daily Pipeline run | `#48` |
  | Result | 🟢 **SUCCESS** |
  | Execution type | `MANUAL_PARTIAL` |
  | Requested by | `VIEW_BUTTON` |
  | Execution range | Step 12~17 |
  | total · success · no_target · failed · skipped | `6 · 5 · 1 · 0 · 0` |
  | duration | `22,336ms` |
  | DB preflight before entry | 0 REQUESTED `strategy_execution_order` · 0 retryable rejected · 0 active `connector_order_request` |
  | `connector_strategy_order_execute.py` SHA256 hash | Matches official EC2 deployment (2026-06-25 patch · aligned with OD-MS-036) |
  | 6 View gates | Aligned |

  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` safe exit as <span style="color:#0969DA">NO_TARGET</span>**

  | Item | Value |
  | --- | --- |
  | subprocess | `marketconnector_app` |
  | command | `python connector_strategy_order_execute.py --execute` |
  | New `connector_order_request` | 0 |
  | broker order submissions | 0 |
  | Mitigation regressions | OD-MS-016 · OD-MS-028 · OD-MS-033 · OD-MS-036 · OD-SAFE-001~004 · R-AUTO-001 · R-AUTO-019 · R-AUTO-031 · R-AUTO-032 · R-BROKER-004 · R-AUTO-033 all 0 |

  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Step 13~16 downstream synchronization passed normally**

| Item | Value |
| --- | --- |
| `CONNECTOR_ORDER_CHECK` | **Result**: 🟢 **SUCCESS**<br>**Processed count**: 0 active connector orders |
| `SYNC_SELL_FILL` | **Result**: 🟢 **SUCCESS**<br>**Processed count**: `submitted_position_sell_orders=0` |
| `SYNC_BUY_FILL` | **Result**: 🟢 **SUCCESS**<br>**Processed count**: `submitted_buy_orders=0` |
| `SYNC_BUY_POSITION` | **Result**: 🟢 **SUCCESS**<br>**Processed count**: `filled_buy_orders_without_position=0` |

  - Mitigation regressions — OD-MS-016 · OD-MS-021 · OD-MS-024 · OD-MS-025 · R-AUTO-018 · R-DATA-012 all 0
  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Step 17 `BALANCE_REFRESH` passed KIS token reissue + balance storage**

  | Item | Value |
  | --- | --- |
  | After token expiry detection | Reissued |
  | Balance query Status | `200` |
  | `connector_balance_snapshot id` | `239` |
  | `as_of_date` | `2026-06-29` |
  | `cash_balance` | `8,706,505` |
  | `total_eval_amount` | `8,706,505` |
  | `eval_profit` | `0` |
  | `source_version` | `connector-balance-1.0.0` |
  | `connector_position_snapshot` | Empty |
  | Positions | 0 |
  | `NO_ORDER_SUBMITTED` post-validation | Passed |
  | Mitigation regressions | R-DATA-005 · R-DATA-011 0 |

  - Details: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Added 2 common View AWS Paper Batch launchers** — operator-local PowerShell tools

  | Launcher | Role |
  | --- | --- |
  | `C:/Workspaces/portfolio-local-env/Load-PortfolioViewAwsPaperBatchEnv.ps1` | env loader (`view_app` DB user · tunnel `127.0.0.1:15433` · Batch/local-file ON · full pipeline ON · paper order ON · `1~17` range) |
  | `C:/Workspaces/portfolio-local-env/Start-PortfolioViewAwsPaperBatch.ps1` | starter (invoke env loader → `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=aws-paper`) |

  - Starter validation passed — Tomcat 8080 started · DB `127.0.0.1:15433/portfolio` connected · Default schema `ops` · screen allowed range `1~17` · Execution · Local File · Full Pipeline · Paper Order all ON · execution buttons enabled

- 🟢 **Added new R-SEC-010 row**

  | Item | Value |
  | --- | --- |
  | Risk | Historical plaintext DB password exposure → follow-up password rotation needed for affected app role |
  | Status | 🟠 **Open** |
  | Promotion condition | After rotation complete + secret loader / env revalidation passes → 🟢 **Mitigated** |
  | Follow-up owner | 06 spec |
  | Coupled with | R-DOCS-001 · R-DOCS-002 · R-SEC-001~003 · OD-SEC-002 |

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | R-AUTO-033 | Added [2026-06-29 reinforcement] mitigation note |
  | Evidence | Full Step 12~17 range · paper order-capable range safely ended as 🔵 **NO_TARGET** |
  | New `connector_order_request` | 0 |
  | broker order submissions | 0 |
  | Step 17 balance refresh | Passed |
  | Policy documentation | Shared launcher allows paper Steps 10/11/12 → preflight required after starter execution |
  | Status | 🟢 **Mitigated** |
  | R-DOCS-002 | Added [2026-06-29 reinforcement] mitigation note |
  | Evidence | Identified history of DB password exposure during conversation · follow-up separated into new R-SEC-010 |
  | Status | Remains 🟢 **Mitigated** |
  | R-SEC-010 | New row |
  | mitigation | (a) Rotate app roles (`view_app` · `marketconnector_app` · `crawler_app` · `preprocessor_app` · `research_app` · `decision_app` |
  | mitigation (continued) | exposed role among `execution_app`) (b) update Secrets Manager / SSM SecureString (c) revalidate local PowerShell secret loader · env (d) carefully check starter use before rotation is complete |
  | detection | Exposure-history audit + operator note at rotation completion |
  | rollback | Promote Status → 🟢 **Mitigated** when rotation is complete |
  | Status | 🟠 **Open** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-29 follow-up note | Added |
  | Completed | 5 items (Step 12~17 safety check · Step 12~17 execution · DB after-check · shared launcher organization · second validation conclusion) |
  | Follow-up | 5 items (document 4 preflight items · DB password rotation · secret loader revalidation · ECS entry design · update 05 spec subordinate documents) |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | Update location | Prepend to 2026-06-29 section |
  | Length | 5~10-line summary |
  | Included facts | Run #48 · 2 shared launchers · conclusion · 3 caution/follow-up items · safety/security alignment |

- 🟢 Formal updates to operation-notes · validation-checklist · safety-gate-note under 05-port-view-ecs-and-runbook · 04-strategy-batch-stepfunctions are outside this date's work scope. Formal reflection of the Step 12~17 NO_TARGET safe termination, six View gates, and shared launcher organization is separated as follow-up responsibility for specs 04 · 05.

### Security

| Item | Result |
| --- | --- |
| Raw secrets (KIS app key/secret · account number · token · RDS password · IAM/state machine ARN · Slack webhook URL · DB password · Administrator password, etc.) | 0 |
| AWS · EventBridge · Lambda · SFN · SSM · EC2 · RDS · S3 executions | 0 |
| AWS CLI · boto3 executions | 0 |
| AWS resource creations · modifications · deletions | 0 |
| Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
| Plaintext quotation of CloudWatch · SFN · Lambda · KIS · Spring log · Pipeline stdout · patch body · launcher body | 0 |
| New broker · KIS BUY / SELL / cancel / modify | 0 |
| Automatic fill · position sync retries | 0 |
| aws-live work | 0 |

> broker · KIS calls = 1 Step 17 balance refresh (KIS token reissue + balance query Status `200`) + 1 Step 12 `--execute` (<span style="color:#0969DA">**NO_TARGET**</span> · 0 broker calls) + 1 Step 13 active connector order query (0 targets).
> 0 new `connector_order_request` · `NO_ORDER_SUBMITTED` after-check passed. · mitigation regressions (R-AUTO-001 · R-AUTO-002 · R-AUTO-019 · R-AUTO-031 · R-AUTO-032 · R-BROKER-004 · R-AUTO-033) all 0.

<details>
<summary>🟠 DB password plaintext exposure history registration</summary>

- A history of DB password plaintext exposure during the operator conversation on this date was identified.
- New R-SEC-010 / Status 🟠 **Open** / R-DOCS-002 [2026-06-29 reinforcement].
- App-role rotation targets — the exposed role among `view_app` · `marketconnector_app` · `crawler_app` · `preprocessor_app` · `research_app` · `decision_app` · `execution_app`.
- Promotion condition — rotation complete + Secrets Manager / SSM SecureString update + operator-local PowerShell secret loader · env revalidation passed → 🟢 **Mitigated**.
- Follow-up owner — 06 spec phase.

</details>

<details>
<summary>🔵 Show summary of factual operational identifiers recorded</summary>

- Pipeline run id `#48` · execution type `MANUAL_PARTIAL` · requester `VIEW_BUTTON` · execution range `Step 12~17` · step count `6/5/1/0/0` · duration `22,336ms`.
- Six Step codes — `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` · `CONNECTOR_ORDER_CHECK` · `SYNC_SELL_FILL` · `SYNC_BUY_FILL` · `SYNC_BUY_POSITION` · `BALANCE_REFRESH`.
- Result labels — `SUCCESS` · `NO_TARGET`.
- Two DB roles — `view_app` (JVM) · `marketconnector_app` (subprocess).
- balance snapshot — `id=239` · `as_of_date=2026-06-29` · `cash_balance=8,706,505` · `total_eval_amount=8,706,505` · `eval_profit=0` · `source_version=connector-balance-1.0.0`.
- launcher file paths — `C:/Workspaces/portfolio-local-env/Load-PortfolioViewAwsPaperBatchEnv.ps1` · `C:/Workspaces/portfolio-local-env/Start-PortfolioViewAwsPaperBatch.ps1`.
- Spring profile `aws-paper` · Tomcat `8080` · DB `127.0.0.1:15433/portfolio` · default schema `ops` · screen allowed range `1~17`.
- All recorded factually in alignment with explicit user policy — not secrets.

</details>

## 2026-06-28 (View Local Batch Step 1 + Step 1~11 execution validation passed + decisive evidence of 6/26 KRX reference-date lag secured)

### Added

- 🟢 **View Local Batch Step 1 standalone execution passed**

  | Item | Value |
  | --- | --- |
  | Daily Pipeline run | `#46` |
  | Result | 🟢 **SUCCESS** |
  | Execution type | `MANUAL_PARTIAL` |
  | Requester | `VIEW_BUTTON` |
  | Execution range | Step 1~1 |
  | Step 1 `CONNECTOR_BALANCE` | 🟢 **SUCCESS** |
  | workDir | `C:/Workspaces/port-marketconnector` |
  | command | `python connector_balance.py` |
  | exit code | `0` |
  | subprocess | `dbUser=marketconnector_app` |
  | legacy `balance_summary` write | Disabled |
  | `connector_balance_snapshot` storage | Normal |
  | `connector_position_snapshot` empty | Normal |
  | Positions | 0 |
  | mitigation regressions | R-DATA-005 · R-DATA-011 0 |

- 🟢 **View Local Batch full Step 1~11 execution passed**

  | Item | Value |
  | --- | --- |
  | Daily Pipeline run | `#47` |
  | Result | 🟢 **SUCCESS** |
  | Execution type | `MANUAL_PARTIAL` |
  | Requester | `VIEW_BUTTON` |
  | Execution range | Step 1~11 |
  | total · success · no_target · failed · skipped | `11 · 7 · 4 · 0 · 0` |
  | duration | `1,362,039ms` |
  | Step 1~7 (CONNECTOR_BALANCE · INTEREST_CRAWLER · PREPROCESSOR · BACKTEST_RESEARCH · BACKTEST_REPORT · DAILY_BUY_SIGNAL · DAILY_POSITION_SIGNAL) | All 🟢 **SUCCESS** |
  | Step 8~11 (DAILY_BUY_EXECUTION · DAILY_SELL_EXECUTION · DAILY_AUTO_SELL · DAILY_AUTO_BUY) | 🔵 **NO_TARGET** |
  | broker order submissions | 0 |
  | `--execute` | 0 |

- 🟢 **Per-step DB role override validation passed**

  | View / Step | DB Role |
  | --- | --- |
  | View JVM | Keep `view_app` |
  | Step 1 (subprocess) | `marketconnector_app` |
  | Step 2 (subprocess) | `crawler_app` |
  | Step 3 (subprocess) | `preprocessor_app` |
  | Step 4~5 (subprocess) | `research_app` |
  | Step 6~7 (subprocess) | `decision_app` |
  | Step 8~11 (subprocess) | `execution_app` |

  - First empirical validation — OD-DB-007 · OD-DB-008 · OD-DB-009 · OD-DB-011 · R-DATA-005 mitigation regressions 0.

- 🟢 **Decisive evidence of 6/26 KRX reference-date lag secured**

  | Item | Value |
  | --- | --- |
  | First-created raw | `2026-06-25` · `2026-06-26` rows in `interest_program_raw` · `interest_shortsell_raw` |
  | 6/26 AWS Step Function | `step1-11-approval-20260626-080004-0904111b` 🟢 **SUCCEEDED** |
  | Step2B Windows KRX GUI worker | `LastTaskResult=0` |
  | Actual worker collection date | Only through previous trading day `2026-06-24` |
  | Combined alignment | R-DATA-017 [2026-06-28 decisive evidence] · R-AUTO-020 [2026-06-26 reinforcement] |

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | R-DATA-017 | Added [2026-06-28 decisive evidence] mitigation note |
  | Evidence | First creation of 6/25 · 6/26 rows during Local Step 2 execution |
  | Status | Remains 🟠 **Open** |
  | Promotion condition | Strengthen Step2B success criteria + display Slack KRX reference date + document feature lag policy → 🟢 **Mitigated** |
  | R-AUTO-033 | Added [2026-06-28 reinforcement] mitigation note |
  | Evidence | Four Spring feature flags default false + server-side POST block + per-step DB role override + 0 broker calls + no entry into Step 12 or later |
  | Status | 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-28 follow-up note | Added |
  | Completed | 4 items (Step 1 standalone · Step 1~11 · DB role override · decisive KRX lag evidence) |
  | Follow-up | 4 items (Step 12~17 execution preflight · strengthen Step2B success criteria · display Slack KRX reference date · document feature lag policy) |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | Update location | Prepend to 2026-06-28 section |
  | Length | 5~10-line summary |
  | Included facts | Run #46 · Run #47 · DB role separation · decisive KRX lag evidence · next step |

- 🟢 OD-MS-037 (View Local AWS Paper read-only first scope)

  | Item | Value |
  | --- | --- |
  | Body change | None (operator-decisions.md) |
  | Evidence reinforcement | After read-only entry, Step 1~11 validation entered the second local validation stage |
  | Step 12 or later | Policy of entering only after separate preflight remains |
  | Formal note reflection | Responsibility separated to follow-up Change Log entry |

- 🟢 Formal updates to operation-notes under 04-strategy-batch-stepfunctions / 05-port-view-ecs-and-runbook are outside this date's scope — formal reflection of Step 1~11 results · DB role override · KRX lag · Step 12~17 preflight is follow-up responsibility for specs 04 · 05.

### Security

| Item | Result |
| --- | --- |
| Raw secrets (KIS app key/secret · account number · token · RDS password · IAM/state machine ARN · Slack webhook URL · DB password · Administrator password, etc.) | 0 |
| AWS · EventBridge · Lambda · SFN · SSM · EC2 · RDS · S3 · KIS executions | 0 |
| AWS CLI · boto3 executions | 0 |
| AWS resource creations · modifications · deletions | 0 |
| Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
| Plaintext quotation of CloudWatch · SFN execution history · Lambda / KIS response · Spring log · Pipeline stdout | 0 |
| New broker · KIS BUY / SELL / cancel / modify · `--execute` | 0 |
| Automatic fill · position sync retries | 0 |
| aws-live work | 0 |
| Slack deliveries (during validation) | 0 |

> broker · KIS calls = limited to read-oriented Step 1 CONNECTOR_BALANCE + normal Step 2 KRX worker execution (`LastTaskResult=0`). Step 8~11 <span style="color:#0969DA">**NO_TARGET**</span>.

<details>
<summary>🔵 Show summary of factual operational identifiers recorded</summary>

- Pipeline run id `#46` · `#47` · execution type `MANUAL_PARTIAL` · requester `VIEW_BUTTON`.
- Execution-range labels `Step 1~1` · `Step 1~11`.
- Eleven Step codes — CONNECTOR_BALANCE · INTEREST_CRAWLER · PREPROCESSOR · BACKTEST_RESEARCH · BACKTEST_REPORT · DAILY_BUY_SIGNAL · DAILY_POSITION_SIGNAL · DAILY_BUY_EXECUTION · DAILY_SELL_EXECUTION · DAILY_AUTO_SELL · DAILY_AUTO_BUY.
- Result labels — `SUCCESS` · `NO_TARGET`.
- DB roles — `view_app` · `marketconnector_app` · `crawler_app` · `preprocessor_app` · `research_app` · `decision_app` · `execution_app`.
- duration `1,362,039ms` · count distribution `11/7/4/0/0`.
- workDir `C:/Workspaces/port-marketconnector` · command `python connector_balance.py` · exit code `0`.
- KRX raw previous load date `2026-06-24` · newly created raw dates on this date `2026-06-25` · `2026-06-26`.
- 6/26 Step Function execution name `step1-11-approval-20260626-080004-0904111b`.
- placeholder — `[REDACTED]` · `[REDACTED_ACCOUNT_NO]`.

</details>

## 2026-06-27 (View Local AWS Paper read-only first scope complete / local validation passed before ECS Fargate migration)

### Added

- 🟢 **View Local AWS Paper read-only first scope complete**

  | Item | Value |
  | --- | --- |
  | Connection method | Local Spring Boot → SSM Port Forwarding (`127.0.0.1:15433`) → AWS Paper RDS |
  | Mode | read-only operations console |
  | Alignment | OD-ENV-006 · OD-ENV-007 · OD-NET-010 · OD-NET-011 · OD-DB-009 |
  | Spring profile | aws-paper (new) |
  | DB user | Keep `view_app` |
  | Four feature flags (`snapshot-refresh` · `order-refresh` · `strategy.execution.submit` · `connector-refresh`) | All default false |
  | KIS · Connector · Slack · Daily Batch triggers | 0 |

- 🟢 **New decision OD-MS-037**

  | Item | Value |
  | --- | --- |
  | Content | Confirm View Local AWS Paper read-only first scope |
  | Prerequisite before ECS / Fargate entry | Batch second validation (View Local Batch Step 1~17) first |
  | Status | 🟢 **확정** |
  | Affected specs | 04 · 05 · 10 |
  | Decision Summary | Total 95 → 96 · confirmed 50 → 51 · provisional 42 unchanged |

- 🟢 **Balance / Positions screens**

  | Item | Value |
  | --- | --- |
  | `BalanceService` query source | legacy `balance_summary` → latest `connector_balance_snapshot` |
  | Positions | Based on `connector_position_snapshot` |
  | `/balance-summary` · `/positions` | Screen validation passed |
  | 0 positions | Empty card normal |
  | KIS · Connector · Slack · Batch calls during screen entry | 0 |

- 🟢 **Orders / Order Detail screens**

  | Item | Value |
  | --- | --- |
  | `/orders` list · `/orders/56` detail | Normal |
  | Display source | `connector_order_request` · `connector_order_event` · `connector_fill` |
  | `portfolio.order-refresh.enabled` | false |
  | KIS · Connector refresh · order API calls | 0 |

- 🟢 **Strategy / Report screens**

  | Item | Value |
  | --- | --- |
  | `/strategy/execution/plans` | plan #113 displayed (new buys blocked · no execution candidates) |
  | `portfolio.strategy.execution.submit-enabled` | false |
  | Server-side POST submit block | Confirmed |
  | `/strategy/reports/latest` period | `2023-01-27 ~ 2026-06-24` |
  | Cumulative return | `427.69%` |
  | MDD | `-8.94%` |
  | Sharpe | `2.48` |

- 🟢 **Daily Batch / Dashboard screens**

  | Item | Value |
  | --- | --- |
  | run · step log | Read-only configuration |
  | Execution buttons | disabled |
  | Server-side POST bypass | Blocked |
  | Order-capable path Step 12 or later | Blocked by default |
  | `/dashboard` wording | "AWS Paper / Snapshot-based / read-only" |

- 🟢 **Added new R-AUTO-033 row**

  | Item | Value |
  | --- | --- |
  | Risk | Unintended broker · batch · Slack trigger through View Local execution-capable calls / POST bypass |
  | Status | 🟢 **Mitigated** |
  | Mitigation | Four Spring feature flags + server-side POST block + staged entry only after explicit operator ENABLE |

### Changed

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | OD-MS-037 | New row |
  | Change Log | Added 2026-06-27 entry |
  | Body change | None (only evidence reinforcement for OD-ENV-006 · OD-ENV-007 · OD-NET-010 · OD-NET-011 · OD-DB-009 · OD-MS-010) |
  | First empirical validation | Local Spring Boot AWS Paper RDS read-only connection first startup passed |
  | Decision Summary | Total 95 → 96 · confirmed 50 → 51 · provisional 42 unchanged |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | R-AUTO-033 | New row |
  | Risk | Bypass of execution-capable calls on View Local |
  | Mitigation | Four Spring feature flags default false + server-side POST block + staged ENABLE |
  | Status | 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-27 follow-up note | Added |
  | Completed | 5 items (aws-paper profile · balance/positions · orders/order detail · strategy/reports · Daily Batch/dashboard) |
  | Follow-up | 4 items (View Local Batch Step 1~17 execution validation · Slack notifier integration · ECS/Fargate deployment design · 05 spec subordinate updates) |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | Update location | Prepend to 2026-06-27 section |
  | Length | 5~10-line summary |
  | Included facts | read-only scope complete + screen validation + conclusion + follow-up + safety/security alignment |

- 🟢 Formal subordinate updates under 05-port-view-ecs-and-runbook are outside this date's scope. Spring Boot source changes on the port-view MS side (`application-aws-paper.properties` · `BalanceService.java` · controller · template) belong to the port-view MS area; 0 cross-service AWS Migration spec-area changes resulted from this date's work.

### Security

| Item | Result |
| --- | --- |
| Raw secrets (KIS app key/secret · account number · token · RDS password · Slack webhook · DB password · Administrator password, etc.) | 0 |
| AWS · Lambda · SFN · SSM · EC2 · RDS · S3 · KIS executions | 0 |
| AWS CLI · boto3 executions | 0 |
| AWS resource creations · modifications · deletions | 0 |
| Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
| Plaintext quotation of CloudWatch · SFN · Lambda · KIS · Spring log | 0 |
| Execution-capable calls from View Local (broker · batch · Slack) | 0 |
| KIS · Connector refresh · order API · Daily Batch run · Slack delivery | 0 |

> Four Spring Boot feature flags + server-side POST block prevented unintended triggers (aligned with new R-AUTO-033).

<details>
<summary>🔵 Show summary of factual operational identifiers recorded</summary>

- `view_app` DB user · `127.0.0.1:15433` local port · SSM Port Forwarding waypoint pattern.
- plan id `113` · order id `56`.
- Backtest period `2023-01-27 ~ 2026-06-24` · cumulative return `427.69%` · MDD `-8.94%` · Sharpe `2.48`.
- Four Spring Boot feature-flag labels.
- route prefixes — `/balance-summary` · `/positions` · `/orders` · `/orders/{id}` · `/strategy/execution/plans` · `/strategy/reports/latest` · `/dashboard` · `/daily-batch`.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-26 (AWS Paper Step 1~11 approval workflow scheduled execution passed + first identification of Step2B KRX reference-date lag)

### Added

- 🟢 **08:00 EventBridge Scheduler scheduled execution result**

  | Item | Value |
  | --- | --- |
  | Scheduler | `portfolio-paper-daily-step1-11-approval-0800-kst` 🟢 **ENABLED** |
  | Dispatcher Lambda | `portfolio-paper-daily-scheduler-dispatcher` |
  | State machine | `portfolio-paper-daily-step1-17-approval` |
  | execution name | `step1-11-approval-20260626-080004-0904111b` |
  | Status | 🟢 **SUCCEEDED** |
  | Step 12~17 default | false (approval gate remains blocked) |
  | Slack | `APPROVAL_REQUIRED` received |
  | Live order submissions | 0 |
  | New `connector_order_request` | 0 |
  | Alignment | R-AUTO-025 [2026-06-24 reinforcement] · OD-MS-029 · OD-MS-031 · OD-MS-032 · OD-MS-033 |

- 🟢 **Step2B Windows KRX GUI worker success handling**

  | Item | Value |
  | --- | --- |
  | Scheduled Task | `Portfolio-KRX-Worker-Daily` |
  | `LastTaskResult` | `0` |
  | State | Running → returned to Ready |
  | Call path | KRX login / program / shortsell entered normally |
  | mitigation regressions | R-AUTO-007 · R-AUTO-008 · R-AUTO-020 all 0 |

- 🟢 **New R-DATA-017 row**

  | Item | Value |
  | --- | --- |
  | Risk | Step2B worker process exit 0 / `LastTaskResult=0` alone does not guarantee KRX raw freshness |
  | Impact | stale collection date propagates to preprocessor / decision / research |
  | First identified case | Collection date `2026-06-24` in 2026-06-26 worker log |
  | Status | 🟠 **Open** |

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | R-AUTO-020 | Added [2026-06-26 reinforcement] mitigation note |
  | Evidence | Even with worker `LastTaskResult=0` + Step Function `SUCCEEDED`, KRX raw collection date lagged the previous trading day |
  | `ExpectedKrxRawDate` calculation basis | Case where actual worker collection-date distribution was not reflected |
  | Status | Remains 🟢 **Mitigated** (worker execution itself is aligned) |
  | R-DATA-017 | New row (candidate for promotion after strengthening Step2B success criteria) |
  | Status | 🟠 **Open** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-26 follow-up note | Added |
  | Completed | 2 items (08:00 Scheduler scheduled execution · Step2B worker success handling) |
  | Follow-up | 3 items (strengthen Step2B success criteria · display Slack KRX reference date · revisit feature-lag acceptance policy) |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | Update location | Prepend to 2026-06-26 section |
  | Length | 5~10-line summary |
  | Included facts | 08:00 Scheduler `SUCCEEDED` · Step2B success handling · first identification of KRX lag during 2026-06-28 local validation · 3 follow-ups |

- 🟢 Formal updates to 04 spec operation-notes / validation documents are outside this date's scope — formal reflection of stronger Step2B success criteria is follow-up responsibility for specs 04 / 08.

### Security

| Item | Result |
| --- | --- |
| Raw secrets (KIS · account · token · RDS password · IAM ARN · Slack webhook · Administrator password, etc.) | 0 |
| AWS · Lambda · SFN · SSM · EC2 · RDS · S3 · KIS executions | 0 |
| AWS CLI · boto3 executions | 0 |
| AWS resource creations · modifications · deletions | 0 |
| Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
| Plaintext quotation of CloudWatch · SFN · Lambda · Slack payload · KIS response · SSM stdout | 0 |
| New broker · KIS BUY / SELL / cancel / modify · `--execute` | 0 |
| Automatic fill · position sync retries | 0 |
| aws-live work | 0 |

> broker · KIS calls = limited to Steps 1~11 from this date's 08:00 schedule. 0 order submissions after Step 12.

<details>
<summary>🔵 Show summary of factual operational identifiers recorded</summary>

- Scheduler `portfolio-paper-daily-step1-11-approval-0800-kst`.
- Dispatcher Lambda `portfolio-paper-daily-scheduler-dispatcher`.
- State machine `portfolio-paper-daily-step1-17-approval`.
- execution name `step1-11-approval-20260626-080004-0904111b`.
- Slack event label `APPROVAL_REQUIRED`.
- Windows Scheduled Task `Portfolio-KRX-Worker-Daily` · `LastTaskResult=0`.
- KRX raw collection date `2026-06-24` · worker-log reference date `2026-06-26` · run_date `2026-06-26`.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-25 (Intraday-position Step Function implementation complete + blocked-gate / no-target true-path validation passed / live order deferred)

### Added

- 🟢 **Partial implementation of stages 1·2·3 of the three-stage intraday-position check structure complete** (performed directly by operator)

  | Item | Value |
  | --- | --- |
  | Kiro AWS CLI · boto3 · SFN · SSM · Lambda · EC2 · RDS · S3 · KIS API executions | 0 |
  | AWS resource creations · modifications · deletions | 0 |
  | Changes to 8 MS README · AGENTS · CHANGELOG · docs · worklog | 0 |
  | Entrypoint changes | Follow-up update responsibility for 03 spec operation-notes |
  | New OD-MS-036 | 🟢 **확정** |
  | Affected specs | 03 · 04 · 05 · 10 |

- 🟢 `connector_intraday_snapshot_refresh.py` — new MarketConnector stage-1 entrypoint

  | Item | Value |
  | --- | --- |
  | source_version | `connector-intraday-snapshot-refresh-1.0.0` |
  | SHA256 hash validation | Passed |
  | KIS balance · position snapshot | Refreshed |
  | Upsert targets | `connector_balance_snapshot` · `connector_position_snapshot` |
  | broker order submissions | 0 |
  | idempotent | Confirmed |
  | Deployment | MarketConnector EC2 venv (SSM commandId `b44d7c4e-c21c-48c0-a3c0-3a5572935577`) |

- 🟢 `connector_intraday_position_evaluate.py` — new StrategyExecution stage-2 entrypoint

  | Item | Value |
  | --- | --- |
  | source_version | `connector-intraday-position-evaluate-1.1.0` |
  | SHA256 hash validation | Passed |
  | Execution method | MarketConnector EC2 venv python via SSM |
  | Safety guards | 4 (stale snapshot · duplicate order · `sellable_qty` · `current_price`) |
  | Storage | Create `strategy_intraday_position_check` + `INTRADAY_STOP_SELL` READY order only |
  | broker order submissions | 0 |
  | `daily_intraday_position_monitor_run.py` modifications | 0 |
  | Alignment | OD-MS-035 |

- 🟢 **New wrapper `run_intraday_snapshot_and_evaluate.sh`** — operational wrapper for running stage-1 snapshot refresh + stage-2 evaluate in a single SSM RunCommand. Calls MarketConnector EC2 venv python. Authored · deployed directly by operator.

- 🟢 **One new EventBridge Scheduler ENABLED**

  | Item | Value |
  | --- | --- |
  | Scheduler | `portfolio-paper-intraday-snapshot-evaluate-10min-kst` |
  | cron | `cron(10/10 9-15 ? * MON-FRI *)` |
  | Timezone | Asia/Seoul |
  | Flexible | OFF |
  | Target | dispatcher invoking SSM RunCommand |
  | Validation | Automatic tick every 10 minutes during weekday market hours passed |

- 🟢 **One new Step Functions stage-3 State Machine created**

  | Item | Value |
  | --- | --- |
  | State machine | `portfolio-paper-intraday-stop-sell-approval` |
  | Type | STANDARD |
  | Status | 🟢 **ACTIVE** |
  | State count | 18 |
  | Created at | `2026-06-25T14:58:45+09:00` |
  | Separation | Fully separated from Daily Step 1~17 state machine |
  | approval gate | `CheckIntradayStopApproval` → `BlockedByIntradayStopApprovalGate` |
  | true-path states | 9 |
  | broker-call delegation | SFN → SSM RunCommand + ECS RunTask.sync → MarketConnector EC2 |
  | Lambda role | dispatcher only (0 direct broker calls) |

  - Nine true-path states · `RunIntradayStopOrderExecute` · `GetIntradayStopOrderExecuteInvocation` · `RunConnectorOrderCheck` · `GetConnectorOrderCheckInvocation` · `RunSyncSellFill` · `RunConnectorBalanceRefresh`
    `GetConnectorBalanceRefreshInvocation` · `IntradayStopWorkflowSucceeded` · `IntradayStopWorkflowFailed`.

- 🟢 `connector_strategy_order_execute.py` — dedicated `signal_type=INTRADAY_STOP_SELL` filter patch

  | Item | Value |
  | --- | --- |
  | SHA256 hash validation | Passed |
  | Operational marker | `DEPLOY_CONNECTOR_STRATEGY_ORDER_EXECUTE_INTRADAY_STOP_FILTER=SUCCESS` |
  | Separation alignment | Daily SELL (Step 12 `--execute`) ↔ Intraday Stop Sell |
  | R-AUTO-032 | First empirical validation of new mitigation |
  | OD-MS-028 · OD-MS-033 | No body changes |

- 🟢 **Safety test (a) blocked gate**

  | Item | Value |
  | --- | --- |
  | executionName | `intraday-stop-blocked-gate-20260625-145928` |
  | input | `allowIntradayStopOrderExecute=false` |
  | Status | 🟢 **SUCCEEDED** |
  | approval gate | Block alignment confirmed |
  | true-path entries | 0 |
  | marker | `INTRADAY_STOP_SELL_BLOCKED_GATE_TEST=SUCCESS` |

- 🟢 **Safety test (b) true-path no-target**

  | Item | Value |
  | --- | --- |
  | executionName | `intraday-stop-truepath-notarget-20260625-150146` |
  | input | `allowIntradayStopOrderExecute=true` |
  | Status | 🟢 **SUCCEEDED** |
  | Start | `2026-06-25T15:01:46+09:00` |
  | Stop | `15:02:53+09:00` |
  | true-path state entry | Aligned |
  | DB comparison `INTRADAY_STOP_SELL` | 0 rows |
  | `READY/FAILED SELL` | 0 rows |
  | `max_connector_order_request_id` | 56 (no change) |
  | `created_after_truepath_count` | 0 |
  | New `connector_order_request` | 0 |
  | KIS broker order submissions | 0 |

- 🟢 **Recorded 5 additional operational markers**

  - `INTRADAY_STOP_SELL_ASL_DRAFT_VALIDATE=SUCCESS`
  - `INTRADAY_STOP_SELL_IAM_INSPECT=SUCCESS`
  - `INTRADAY_STOP_SELL_STATE_MACHINE_CREATE=SUCCESS`
  - `INTRADAY_STOP_SELL_BLOCKED_GATE_TEST=SUCCESS`
  - `DEPLOY_CONNECTOR_STRATEGY_ORDER_EXECUTE_INTRADAY_STOP_FILTER=SUCCESS`

- 🟢 **3 new risks + 1 reinforcement**

| Item | Value |
| --- | --- |
| R-AUTO-030 | **Status**: remains 🟠 **Open**<br>**Note**: [2026-06-25 reinforcement] State Machine creation + blocked gate + no-target validation passed. Policy blocking automatic ENABLE entry remains |
| R-AUTO-031 | **Status**: 🟢 **Mitigated**<br>**Note**: Risk of Intraday Stop Sell approval-gate misconfiguration. First empirical blocked-gate test |
| R-AUTO-032 | **Status**: 🟢 **Mitigated**<br>**Note**: Risk from missing Daily SELL ↔ Intraday Stop Sell `signal_type` filter. Dedicated filter patch deployment aligned |
| R-BROKER-005 | **Status**: 🟢 **Mitigated**<br>**Note**: Risk of attempting a live order with zero positions. Correctly stopped at pre-validation stage |

### Changed

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | OD-MS-036 | New row (Intraday Stop Sell Submit Workflow) |
  | Status | 🟢 **확정** |
  | Affected specs | 03 · 04 · 05 · 10 |
  | Change Log | Added 2026-06-25 entry |
  | OD-MS-035 | Added [2026-06-25 reinforcement] note · no body change |
  | Decision Summary | Total 94 → 95 · confirmed 49 → 50 · provisional 42 unchanged |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | Item | Value |
  | --- | --- |
  | R-AUTO-030 | Added [2026-06-25 reinforcement] mitigation note · Status remains 🟠 **Open** |
  | R-AUTO-031 | New (approval-gate misconfiguration risk) · Status 🟢 **Mitigated** |
  | R-AUTO-032 | New (missing Daily SELL ↔ Intraday Stop Sell `signal_type` filter) · Status 🟢 **Mitigated** |
  | R-BROKER-005 | New (risk of attempting live order with zero positions) · Status 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-25 follow-up note | Added |
  | Completed | 8 items (stage-1 entrypoint · stage-2 entrypoint · Scheduler automatic tick · `signal_type` filter patch · State Machine creation · blocked gate · no-target true-path · Step 17 run-date dynamicization) |
  | Follow-up | 5 items (resume real 1-share live-order test · Slack notification integration · automatic ENABLE-entry policy · Dispatcher Lambda log reinforcement · 03/04/05/06/10 spec follow-up) |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Fourth 2026-06-25 note | Added |
  | Primary / secondary / not-recommended decision values | No change |
  | Crawler · Preprocessor · Decision · Execution · Research | Existing decisions retained |
  | Operations-note update | MarketConnector stages 1·3 · StrategyExecution stage 2 · SFN stage 3 · Scheduler · Lambda dispatcher role |
  | Nature | Factual record of results performed directly by operator |
  | Kiro AWS CLI · boto3 executions | 0 |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md` — no additions on this date. EventBridge Scheduler · Lambda · SSM · SFN · ECS entries already exist, and existing entries are not modified by policy.

- 🟢 `.kiro/WORKLOG.md` — prepend 2026-06-25 section (newest-first · 5~10-line summary).

- 🟢 **Updated dedicated intraday-position-check path status**

  | Item | Value |
  | --- | --- |
  | Previous state | 2026-06-24 final design confirmed / documented |
  | Progress on this date | Partial implementation of stages 1 + 2 + 3 |
  | Progress details | Scheduler 10-minute tick · State Machine creation · two safety tests passed |
  | Live broker orders | 0 |
  | Real 1-share order test | Resume after a position exists |
  | Automatic ENABLE entry | Follow-up phase + separate operator approval |

- 🟢 **Reinforced dynamic run-date for Daily Step 17 balance-refresh task `RunConnectorBalanceRefresh`**

  | Item | Value |
  | --- | --- |
  | Before | Fixed `2026-06-22` |
  | Changed to | Dynamic KST `$(TZ=Asia/Seoul date +%F)` |
  | Daily ASL reflection | Follow-up update responsibility for 03 spec operation-notes |
  | OD-MS-034 | No body change |

### Security

| Item | Result |
| --- | --- |
| Raw secrets (KIS · account · token · RDS password · IAM ARN · Slack webhook · Administrator password, etc.) | 0 |
| AWS · Lambda · SFN · SSM · EC2 · RDS · S3 · KIS executions | 0 |
| AWS CLI · boto3 executions | 0 |
| AWS resource creations · modifications · deletions | 0 |
| Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
| Plaintext quotation of patch body · SFN ASL · IAM Policy · Lambda · CloudWatch · KIS response · SSM stdout | 0 |
| Additional BUY / SELL / cancel / modify · `--execute` | 0 |
| New `connector_order_request` | 0 |
| aws-live work | 0 |
| account-id portion (inside ARN) | Replaced with `[REDACTED]` |

> broker · KIS calls = one forced KIS balance refresh (`EMPTY_NORMAL` · SSM commandId `5b1e5757-ea93-4eb6-a7a0-6d5a38d87840` · `rt_cd=0` · `output1_count=0` · `output2_count=1`) + snapshot refresh from Scheduler automatic tick only.

<details>
<summary>🟠 Real 1-share `INTRADAY_STOP_SELL` order test stopped · deferred</summary>

- Operator stopped at the pre-validation stage immediately before broker call because positions = 0 and `sellable_qty=0`.
- 0 sell orders created for nonexistent positions.
- Verified default gate block with `allowIntradayStopOrderExecute=false`.
- Verified 0 new orders on no-target true-path.
- Alignment — first empirical validation of new R-BROKER-005 · R-AUTO-031 mitigation.

</details>

<details>
<summary>🔵 Show summary of factual operational identifiers recorded</summary>

- SSM command_id — `b44d7c4e-c21c-48c0-a3c0-3a5572935577` · `5b1e5757-ea93-4eb6-a7a0-6d5a38d87840`.
- Two execution_name values — blocked gate · true-path no-target (see specs for details).
- Scheduler `portfolio-paper-intraday-snapshot-evaluate-10min-kst` · cron `cron(10/10 9-15 ? * MON-FRI *)`.
- State Machine `portfolio-paper-intraday-stop-sell-approval` · 11 state names.
- Five operational markers.
- source_version `connector-intraday-snapshot-refresh-1.0.0` · `connector-intraday-position-evaluate-1.1.0`.
- Three SHA256 hashes validated successfully.
- `as_of_date=2026-06-25` · `as_of_ts=2026-06-25T06:07:27.438088` · `max_connector_order_request_id=56` · `output1_count=0` · `output2_count=1` · `EMPTY_NORMAL`.
- signal_type label `INTRADAY_STOP_SELL` · run_date `2026-06-25`.
- All recorded factually in alignment with explicit user policy — not secrets.

</details>

## 2026-06-24 (08:00 Scheduler live execution validation + Step 12 retry-normalizer EGW00201 expansion + manual Step 12~17 execution with 4 FILLED)

### Added

- 🟢 **08:00 EventBridge Scheduler live execution validation** (OD-MS-032 follow-up)

  | Item | Value |
  | --- | --- |
  | Scheduler | `portfolio-paper-daily-step1-11-approval-0800-kst` (State 🟢 **ENABLED**) |
  | Execution path | Scheduler → Dispatcher Lambda → SFN `portfolio-paper-daily-step1-17-approval` |
  | Step 1~11 | Completed (approval-required flow) |
  | Slack | `APPROVAL_REQUIRED` received |
  | Orders after Step 12 | Blocked |
  | New orders | 0 |
  | 09:01 Scheduler | Remains 🟠 **DISABLED** |
  | First empirical validation | R-AUTO-025 [2026-06-24 reinforcement] mitigation |

- 🟢 **Expanded Step 12 retry-normalizer retry for `EGW00201`**

  | Item | Value |
  | --- | --- |
  | Targets | 4 remaining from 6/23 (2 `40580000` + 2 `EGW00201`) |
  | Reinforcement | Default sleep between orders + `EGW00201` backoff retry + `submit_attempts` · `result_payload` recording |
  | Target file | `port-marketconnector/connector_strategy_order_execute.py` (full replacement performed directly by operator) |
  | Deployment path | S3 upload + formal deployment to MarketConnector EC2 |
  | Validation | `py_compile` + operational marker passed |
  | Body quotation | 0 (follow-up update responsibility for 03 spec operation-notes) |
  | dry-run `candidate_count` | 4 |
  | DB changes | 0 |
  | New OD-MS-033 | 🟢 **확정** · affected specs 03/04/05/10 |
  | New R-AUTO-026 | First empirical mitigation validation |

- 🟢 **Manual Step 12~17 execution succeeded + all 4 ultimately FILLED**

  | Item | Value |
  | --- | --- |
  | Execution method | Manual invoke of Dispatcher Lambda `STEP12_17_ORDER` |
  | Slack | `DAILY_EXECUTION_SUCCESS` received |
  | Result for 4 items | New `connector_order_request` created + `broker_order_no` created + final fill |

| Item | Value |
| --- | --- |
| `042660` | **Stock name**: 한화오션<br>**Result**: 🟢 **FILLED** |
| `004990` | **Stock name**: 롯데지주<br>**Result**: 🟢 **FILLED** |
| `003490` | **Stock name**: 대한항공<br>**Result**: 🟢 **FILLED** |
| `023530` | **Stock name**: 롯데쇼핑<br>**Result**: 🟢 **FILLED** |

- 🟢 **Confirmed full fill by re-querying single-item order-check after partial fill for `004990`**

  | Item | Value |
  | --- | --- |
  | Initial Step 13 query | `PARTIAL_FILLED` |
  | Re-query command | `--code 004990 --order-no <broker_order_no> --no-broad` |
  | Re-query result | `tot_ccld_qty=69` fully filled |
  | Alignment | OD-MS-025 · R-AUTO-018 mitigation regressions 0 |

- 🟢 **New decisions · risks**

| Item | Value |
| --- | --- |
| OD-MS-033 | **Status**: 🟢 **확정**<br>**Note**: Expanded Step 12 retry-normalizer targets + KIS rate-limit backoff retry + deferred automatic ENABLE at 09:01 |
| R-AUTO-026 | **Status**: 🟢 **Mitigated**<br>**Note**: Risk of missing `EGW00201` rate-limit retry |
| R-AUTO-027 | **Status**: 🔵 **Accepted**<br>**Note**: Risk of 0/0 display in `APPROVAL_REQUIRED` Slack summary |

- 🟢 **EC2 automatic execution implementation complete**

  | Item | Value |
  | --- | --- |
  | 07:50 MarketConnector + Crawler start Scheduler | `portfolio-paper-ec2-start-0750-kst` 🟢 **ENABLED** |
  | 07:50 cron | `cron(50 7 ? * MON-FRI *)` · Asia/Seoul |
  | 07:50 Target input | `{"action":"start","target":"BOTH","holidayCheck":true,"reason":"PRE_DAILY_STEP1_11","dryRun":false}` |
  | 15:50 MarketConnector stop Scheduler | `portfolio-paper-marketconnector-stop-1550-kst` 🟢 **ENABLED** |
  | 15:50 cron | `cron(50 15 ? * MON-FRI *)` · Asia/Seoul |
  | 15:50 Target input | `{"action":"stop","target":"MARKETCONNECTOR","holidayCheck":false,"reason":"POST_MARKET_CLOSE","dryRun":false}` |
  | Crawler stop after successful Step 1~11 | New SFN task state `StopCrawlerEc2AfterStep11Success` |
  | State machine | `portfolio-paper-daily-step1-17-approval` |
  | RevisionId | `edd92cc9-1d94-4752-9a43-b7eb5b2f3c2c` |
  | Permission added | EC2 lifecycle Lambda invoke added to SFN execution role |
  | New OD-MS-034 | 🟢 **확정** · affected specs 03/04/05/08/10 |

- 🟢 **New EC2 lifecycle Lambda configured**

  | Item | Value |
  | --- | --- |
  | Lambda | `portfolio-paper-ec2-lifecycle-dispatcher` |
  | Runtime | Python 3.12 |
  | Handler | `lambda_function.lambda_handler` |
  | Timeout | 30s |
  | Memory | 256MB |
  | State | 🟢 **Active** |
  | IAM Role | `portfolio-paper-ec2-lifecycle-dispatcher` (new dedicated role) |
  | Permissions | EC2 start · stop |
  | Environment variables | `TIMEZONE=Asia/Seoul` · `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true` |
  | Holiday-check policy | Applies only to start · not to stop (post-market stop is independent of holidays · idempotent if already stopped) |

- 🟢 **EC2 lifecycle validation results**

  | Item | Value |
  | --- | --- |
  | 3 dryRun cases (`start BOTH` · `stop CRAWLER` · `stop MARKETCONNECTOR`) | Passed |
  | Weekend skip (`runDate=2026-06-27` Saturday) | fail-closed passed |
  | `get-schedule` for both Schedulers | State 🟢 **ENABLED** · Flexible OFF · Target Lambda · Target input aligned |
  | SFN ASL update after backup | Passed · State Machine 🟢 **ACTIVE** · RevisionId `edd92cc9-1d94-4752-9a43-b7eb5b2f3c2c` |
  | Live trading-day 07:50 / 15:50 execution validation | Planned for next trading day |
  | New R-AUTO-028 | 🟢 **Mitigated** |

- 🟢 **Final three-stage intraday-position-check design confirmed / documented** — this is the final design confirmation/documentation stage, not completion of actual implementation

  | Item | Value |
  | --- | --- |
  | Actual AWS · Lambda · SSM · SFN · RDS · KIS API executions | 0 |
  | AWS resource creations · modifications · deletions | 0 |
  | Application source modifications | 0 |
  | Stage 1 (MarketConnector 10-minute Snapshot Refresh) | Scheduler → Lambda → SSM → MC EC2 · refresh balance/position snapshots · 0 decisions/orders · idempotent |
  | Stage 2 (StrategyExecution Intraday Evaluate) | SSM immediately after stage 1 · store `strategy_intraday_position_check` + create READY order only · 0 broker orders · 4 safety guards · new file implementation · no SFN Retry automatic-order linkage |
  | Stage 3 (Intraday Stop Sell Submit & Refresh) | Separate SFN · dedicated `source_type=INTRADAY_STOP_SELL` filter · execute after initial manual approval · separated from Daily BUY/SELL flow |
  | New OD-MS-035 | 🟢 **확정** · affected specs 03/04/05/10 |

- 🟢 **Specified 4 intraday safety guards**

  | Item | Validation |
  | --- | --- |
  | (a) stale snapshot | Stage-1 `as_of_ts` + freshness validation before entering stage 2 |
  | (b) duplicate order | Pre-check `account_id` + `ticker_code` + `source_type=INTRADAY_STOP_SELL` row + idempotency-key follow-up |
  | (c) `sellable_qty` | Pre-compare snapshot `sellable_qty` with candidate quantity · block READY creation if insufficient |
  | (d) `current_price` | sanity check null · 0 · old timestamp · abnormal ±X% range · block evaluate entry on failure |

- 🟢 **5 new risks**

  | ID | Status |
  | --- | --- |
  | R-DATA-014 | 🟢 **Mitigated** |
  | R-AUTO-029 | 🟢 **Mitigated** |
  | R-DATA-015 | 🟢 **Mitigated** |
  | R-DATA-016 | 🟢 **Mitigated** |
  | R-AUTO-030 | 🟠 **Open** (stage-3 automatic ENABLE deferred · promote to 🟢 **Mitigated** after formal implementation + validation pass) |

### Changed

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Change type | Expanded decision lock + added 3 Change Log entries |
  | OD-MS-033 expansion | Step 12 retry-normalizer targets = `40580000` + `EGW00201` (only rejection_code-limiting condition updated among six recovery conditions · retain `broker_order_no IS NULL` + no `connector_fill` conditions · 0 R-AUTO-001 / R-BROKER-004 duplicate-order bypasses) |
  | `EGW00201` backoff operations note | No immediate repeated submission · retry with expanding sleep interval · retain KIS-recommended backoff defaults |
  | New OD-MS-034 row + 2nd Change Log entry | EC2 lifecycle automatic execution |
  | New OD-MS-035 row + 3rd Change Log entry | Final three-stage intraday-position-check design · 🟢 **확정** · affected specs 03/04/05/10 |
  | Decision Summary | Total 92 → 93 → 94 / confirmed 47 → 48 → 49 / provisional 42 unchanged |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | Change |
  | --- | --- |
  | R-AUTO-025 mitigation | [2026-06-24 reinforcement] First live 08:00 execution passed · Scheduler · Dispatcher Lambda · Step Functions chain normal · Status 🟢 **Mitigated** · separate operator approval remains required before 09:01 automatic ENABLE |
  | R-AUTO-025 mitigation | [2026-06-24 second reinforcement] EC2 lifecycle automation chain ENABLED |
  | R-AUTO-026 / R-AUTO-027 | Reflected |
  | New R-AUTO-028 | Risk of EC2 lifecycle Scheduler · Lambda configuration error · Status 🟢 **Mitigated** |
  | New R-DATA-014 | Incorrect stop-loss decision based on stale snapshot · 🟢 **Mitigated** |
  | New R-AUTO-029 | Duplicate `INTRADAY_STOP_SELL` `READY` order creation · 🟢 **Mitigated** |
  | New R-DATA-015 | Stop-loss order submission with insufficient `sellable_qty` · 🟢 **Mitigated** |
  | New R-DATA-016 | Decision based on missing `current_price` or abnormal price · 🟢 **Mitigated** |
  | New R-AUTO-030 | Stage-3 Submit & Refresh Step Functions enabled too early, submitting orders without approval · 🟠 **Open** |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | Item | Value |
  | --- | --- |
  | EventBridge Scheduler / Lambda / Step Functions — 3 entries | Added [2026-06-24 first live execution validation passed] note · Scheduler calls Dispatcher Lambda rather than SFN directly · Dispatcher branches workflow by scheduleType · staged-automation safeguard keeps 09:01 Scheduler `DISABLED` |
  | EventBridge Scheduler second reinforcement | Two EC2 lifecycle Schedulers ENABLED (07:50 EC2 start · 15:50 MarketConnector stop) · Scheduler is holiday-unaware · Lambda holiday guard applies to start request |
  | Lambda second reinforcement | Separate Slack notifier · scheduler dispatcher · EC2 lifecycle dispatcher · retain orchestration support layer |
  | Step Functions second reinforcement | Crawler-stop Lambda task (`StopCrawlerEc2AfterStep11Success`) in Step 1~11 success path · stop Crawler before Approval Slack · RevisionId `edd92cc9-1d94-4752-9a43-b7eb5b2f3c2c` |
  | New EC2 entry | MarketConnector EC2 start 07:50 · stop 15:50 · Crawler EC2 start 07:50 · stop on Step 1~11 success · keep running for debugging on failure |
  | Third reinforcement (intraday-position check) | Added 10-minute Snapshot Refresh Scheduler candidate `portfolio-paper-intraday-snapshot-refresh-10min-kst` · added intraday snapshot refresh orchestration-helper Lambda candidate · added snapshot refresh + intraday evaluate control path to SSM RunCommand entry |
  | Third reinforcement (intraday-position check) (continued) | Added separate state-machine candidate for Intraday Stop Sell Submit & Refresh · added MarketConnector EC2 note as 10-minute intraday refresh target |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-24 follow-up note | 5 completed + 5 follow-ups |
  | 2026-06-24 second follow-up | 3 completed + 9 follow-ups = observe next trading-day 07:50 / Crawler stop after Step 1~11 success / live 15:50 execution · defer 09:01 ENABLE · improve Slack summary · reinforce Dispatcher logs · migrate webhook URL · EGW00215 backoff · Holiday API backup |
  | 2026-06-24 third follow-up | Final three-stage intraday-position-check design confirmed · 5 completed (lock three-stage structure · decide new file implementation · separate dedicated intraday path · new OD-MS-035 · 5 new risks including R-DATA-014) + implementation planned tomorrow (stage-1 Scheduler · Lambda · SSM · new stage-2 evaluate file · separate stage-3 Step Functions · Slack notification linkage) + subordinate spec (03 · 04 · 05 · 06 · 10) modification candidates |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-24 note | First empirical validation of 5-layer Daily Batch orchestration · automatic 08:00 Step 1~11 approval-required execution · 4 FILLED from manual Step 12~17 Dispatcher invoke · Step 12 retry-normalizer + KIS rate-limit backoff · Lambda retained as orchestration dispatcher/notifier support layer |
  | Second 2026-06-24 note | EC2 lifecycle automatic execution implementation complete · orchestration expanded to 6-layer combination |
  | Third 2026-06-24 note | Final three-stage intraday-position-check design · MarketConnector 10-minute Snapshot Refresh centered on MC EC2 + SSM · Intraday Evaluate candidate as new-file SSM execution · only create READY order · Intraday Stop Sell Submit & Refresh as separate SFN candidate |
  | Third 2026-06-24 note (continued) | Lambda serves Scheduler/lifecycle/notifier/orchestration-helper roles · no change to primary decision values for all 8 MS |

- 🟢 **Updated Daily Batch orchestration documentation status**

  | Item | Value |
  | --- | --- |
  | 6-layer combination | SFN + EventBridge Scheduler + Dispatcher Lambda + EC2 lifecycle Lambda + ECS RunTask + SSM RunCommand + AWS Batch |
  | Dedicated intraday-position-check path | Stage 1 Scheduler + Lambda + SSM / Stage 2 SSM + new Execution entrypoint / Stage 3 separate SFN + Slack (follow-up implementation planned) |
  | Three-way Lambda role separation | `portfolio-paper-daily-scheduler-dispatcher` (scheduler dispatcher) · `portfolio-paper-ec2-lifecycle-dispatcher` (EC2 lifecycle dispatcher) · `portfolio-event-notifier` (Slack notifier) · intraday snapshot refresh dispatcher to be added in a follow-up phase |

- 🟢 **Updated automation status**

  | Item | Value |
  | --- | --- |
  | EventBridge automation | Implementation complete · dryRun validation complete · 08:00 live execution validation complete · 09:01 automatic execution remains 🟠 **DISABLED** |
  | 09:01 automatic ENABLE deferred | Aligned with OD-MS-033 · improve `APPROVAL_REQUIRED` Slack summary 0/0 (R-AUTO-027) · reinforce Dispatcher Lambda application logs · decide after reviewing accumulated operational runs |
  | EC2 lifecycle automation | Implementation complete · 3 Lambda dryRuns + weekend-skip validation complete · 07:50 🟢 **ENABLED** · 15:50 🟢 **ENABLED** · Crawler stop after Step 1~11 success linked · first live trading-day execution validation planned |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | Added bullet to 2026-06-24 section | EC2 lifecycle automatic execution complete · 07:50 · 15:50 Scheduler ENABLED · SFN success-path change · three-way Lambda role separation · new R-AUTO-028 · second R-AUTO-025 reinforcement · Decision Summary updated 92 → 93 |

- 🟢 `.kiro/README.md`

  | Item | Value |
  | --- | --- |
  | Current progress summary | Added "EC2 lifecycle automatic execution implementation complete (2026-06-24)" · reflected three-way Lambda role separation · no change to reference date |

### Security

  | Item | Result |
  | --- | --- |
  | Plaintext recording of secret · token · password · ARN · IAM · webhook URL · Step Functions ARN · Lambda ARN | 0 (all `[REDACTED]` or placeholders) |
  | Quotation of full replacement body for operator-patched `port-marketconnector/connector_strategy_order_execute.py` | 0 (R-DOCS-001 · follow-up update responsibility for 03 spec operation-notes) |
  | Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging spec area | 0 |
  | AWS · EventBridge · Lambda · SFN · SSM · EC2 · RDS · S3 · Slack · KIS API | All performed directly by operator · Kiro updated root / `_common` documents only |
  | AWS CLI / boto3 executions | 0 |
  | AWS resource creations · modifications · deletions | 0 |
  | Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
  | Plaintext quotation of CloudWatch Logs · SFN history · KIS response · Lambda response · Slack message · Scheduler `get-schedule` · `simulate-principal-policy` · dryRun response · patch body | 0 |
  | broker / KIS calls | Resubmitted 4 Step 12 paper orders (`042660` · `004990` · `003490` · `023530` BUY MARKET · four 6/23 remnants generated new `connector_order_request` after retry-normalizer) + Step 13 fill query |
  | Additional BUY / SELL / cancel / modify / `--execute` | 0 |
  | Automatic fill · position sync retries | 0 |
  | aws-live work | 0 |
  | SELL position `mark_position_sell_ordered()` calls | 0 (0 SELL flow) |

<details><summary>🔵 Operational identifier summary</summary>

  | Item | Value |
  | --- | --- |
  | rejection_code | `40580000` · `EGW00201` |
  | dry-run | `candidate_count=4` |
  | Ticker codes | `042660` · `004990` · `003490` · `023530` |
  | `004990` transition | PARTIAL_FILLED → FILLED · `tot_ccld_qty=69` |
  | Scheduler | `portfolio-paper-daily-step1-11-approval-0800-kst` · `portfolio-paper-daily-step12-17-order-0901-kst` · `portfolio-paper-ec2-start-0750-kst` · `portfolio-paper-marketconnector-stop-1550-kst` |
  | Dispatcher Lambda | `portfolio-paper-daily-scheduler-dispatcher` · `portfolio-paper-ec2-lifecycle-dispatcher` |
  | SFN state machine | `portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval` |
  | SFN state names | `StopCrawlerEc2AfterStep11Success` · `SendApprovalRequiredSlack` · `SendDailyExecutionFailedSlack` · `Step6ToStep11_Succeeded` · `Step12_CheckApproval` |
  | SFN RevisionId | `edd92cc9-1d94-4752-9a43-b7eb5b2f3c2c` |
  | cron expressions | `cron(0 8 ? * MON-FRI *)` · `cron(1 9 ? * MON-FRI *)` · `cron(50 7 ? * MON-FRI *)` · `cron(50 15 ? * MON-FRI *)` |
  | scheduleType | `STEP12_17_ORDER` |
  | EC2 lifecycle action | `start` · `stop` |
  | target labels | `BOTH` · `CRAWLER` · `MARKETCONNECTOR` |
  | reason labels | `PRE_DAILY_STEP1_11` · `STEP1_11_SUCCESS` · `POST_MARKET_CLOSE` · `STEP1_11_FAILED` |
  | Slack events | `APPROVAL_REQUIRED` · `DAILY_EXECUTION_SUCCESS` · `DAILY_EXECUTION_FAILED` |
  | run_date | `2026-06-24` |
  | Weekend skip validation | `runDate=2026-06-27` |

</details>

## 2026-06-23 (Step Functions approval live validation + Step 12 retry-normalizer + confirmed normal Korean DB storage)

### Added

- 🟢 **Live validation of Step Functions state machine `portfolio-paper-daily-step1-17-approval`**

  | Item | Value |
  | --- | --- |
  | Step 1~17 `allowPaperOrderExecute=false` blocked path | Passed |
  | Step 12~17 `allowPaperOrderExecute=true` true path | Passed |
  | First real SELL order | BGF리테일 `282330` 17 shares MARKET |
  | `connector_order_request id 48` | 🟢 **FILLED** |
  | `strategy_execution_order id 40` | 🟢 **FILLED** |
  | `broker_order_no` | `0000006143` |
  | Latest holdings after Step 17 balance refresh | 4 holdings reflected normally |
  | Step Functions implementation status | Entered operator empirical-validation stage · ms-aws-service-decision-matrix body retained |
  | Accumulation into 03 / 04 spec operation-notes | Separated as follow-up responsibility (second / third pass) |

- 🟢 **Recorded Step 12 retry-normalizer operational reinforcement**

  | Item | Value |
  | --- | --- |
  | Target file | `port-marketconnector/connector_strategy_order_execute.py` (full replacement) |
  | Applied location | Beginning of Step 12 · existing safety reinforcement inside Step 12 · not a separate Step 11.5 |
  | Recovery condition | `execution_mode = PAPER_STRATEGY` + `action_type IN (BUY, SELL)` + `execution_status IN (READY, FAILED)` + `connector_order_request_id IS NOT NULL` + linked `connector_order_request.request_status = REJECTED` + |
  | Recovery condition (continued) | `rejection_code = 40580000` + `broker_order_no IS NULL` + no `connector_fill` |
  | Recovery action | `execution_status = REQUESTED` + `connector_order_request_id = NULL` + store old-request history in `result_payload.retry_normalizer` |
  | Formal EC2 deployment | Complete |
  | `.venv/bin/python` dry-run | Passed (retry candidates 0 · REQUESTED orders 0) |
  | Step Functions Step 12 | Aligned to use `.venv/bin/python` |

- 🟢 **Implemented common AWS Slack notifier Lambda + first validation**

  | Item | Value |
  | --- | --- |
  | Lambda | `portfolio-event-notifier` |
  | Runtime | Python 3.12 |
  | IAM Role | `portfolio-event-notifier-lambda-role` |
  | Responsibility | Single entry point for common PORT-STRATEGY-AI operational events (SFN · EventBridge · EC2 SSM · Batch · Lambda) |
  | Validation | Manual `hello wook` invoke succeeded · Slack received · Portfolio Daily Bot message received |
  | Completion marker | `SLACK_LAMBDA_SMOKE_TEST=SUCCESS` |
  | Plaintext Lambda code / environment-variable values / webhook URL records | 0 (aligned with R-DOCS-001 · all `[REDACTED]` or placeholders) |

- 🟢 **Initial set of six common Slack message templates**

  | No. | Message |
  | --- | --- |
  | (1) | Pre-market balance / holdings status |
  | (2) | Daily validation complete · approval required |
  | (3) | Daily execution success |
  | (4) | Daily execution failure |
  | (5) | Intraday stop-loss candidates |
  | (6) | Post-market balance / holdings status |
  | P/L emoji | 🔴 · 🔵 · ⚪ + Slack attachment color bar |
  | Completion marker | `PORTFOLIO_EVENT_NOTIFIER_TEMPLATE_TEST=SUCCESS` |

- 🟢 **Validated receipt of three Slack events in Step Functions approval workflow**

  | Event | Result |
  | --- | --- |
  | (1) `APPROVAL_REQUIRED` | Sent when entering approval gate after Step 1~11 completion · approval-required awareness flow 🟢 **Passed** |
  | (2) `DAILY_EXECUTION_SUCCESS` | Sent after Step 17 when the full flow succeeds · 🟢 **Passed** |
  | (3) `DAILY_EXECUTION_FAILED` | SFN Catch path · received 12~17 test-only failure + applied ASL for 1~17 full test-only failure · 🟢 **Passed** · 0 intentionally induced real broker-order failures |

- 🟢 **7. EventBridge automation implementation: complete** — new OD-MS-032 · 🟢 **확정** · affected specs 04 · 05 · 10

  | Item | Value |
  | --- | --- |
  | Scheduler `portfolio-paper-daily-step1-11-approval-0800-kst` | cron `cron(0 8 ? * MON-FRI *)` · Asia/Seoul · Flexible OFF · Target Lambda `portfolio-paper-daily-scheduler-dispatcher` · Target Role `portfolio-paper-eventbridge-scheduler-role` |
  | Scheduler `portfolio-paper-daily-step1-11-approval-0800-kst` (continued) | Target input `{"scheduleType":"STEP1_11_APPROVAL","dryRun":false}` · State 🟢 **ENABLED** · `allowPaperOrderExecute=false` |
  | Scheduler `portfolio-paper-daily-step12-17-order-0901-kst` | cron `cron(1 9 ? * MON-FRI *)` · Asia/Seoul · Flexible OFF · same Target Lambda · Target input `{"scheduleType":"STEP12_17_ORDER","dryRun":false}` · State 🟠 **DISABLED** · `allowPaperOrderExecute=true` |
  | Dispatcher Lambda `portfolio-paper-daily-scheduler-dispatcher` | Runtime Python 3.12 · Handler `lambda_function.lambda_handler` · Timeout 30s · Memory 256MB · State 🟢 **Active** · IAM Role `portfolio-paper-daily-scheduler-dispatcher-role` · environment variable `TIMEZONE=Asia/Seoul` |
  | Dispatcher Lambda `portfolio-paper-daily-scheduler-dispatcher` (continued) | `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true` · Step1~17 / Step12~17 State Machine ARNs not recorded |
  | Dispatcher Lambda responsibility | Generate KST `runDate` (YYYY-MM-DD) · skip weekends · market holidays · branch payload by scheduleType · call SFN `StartExecution` · return immediately (does not wait for SFN completion) |
  | IAM Role / Inline policy | 4 types + 3 types |

- 🟢 **EventBridge automation validation complete**

  | Validation | Result |
  | --- | --- |
  | IAM `simulate-principal-policy` | both `states:StartExecution` + `lambda:InvokeFunction` 🟢 **allowed** |
  | Lambda dryRun (08:00 `STEP1_11_APPROVAL`) | Passed · `started=false` · `reason=DRY_RUN_NO_START_EXECUTION` |
  | Lambda dryRun (09:01 `STEP12_17_ORDER`) | Passed · target state machine + `allowPaperOrderExecute` branch aligned |
  | Scheduler `get-schedule` | 08:00 🟢 **ENABLED** / 09:01 🟠 **DISABLED** (aligned with staged activation) |
  | Target Step Functions ACTIVE | `portfolio-paper-daily-step1-17-approval` + `portfolio-paper-daily-step12-17-approval` |
  | New R-AUTO-025 | Risk of Scheduler · Dispatcher Lambda · SFN integration failure · Status 🟢 **Mitigated** |

### Changed

- 🟢 `.kiro/README.md`

  | Item | Value |
  | --- | --- |
  | Reference date | `2026-06-22` → `2026-06-23` |
  | Backend AWS E2E / SFN status row reinforcement | Daily AWS Paper wrapper 1~17 live run completed · SFN approval false / true paths live-validated · Step 12 retry-normalizer deployed to EC2 + dry-run succeeded · confirmed normal Korean DB storage |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | New OD-MS-028 | Embedded Step 12 retry-normalizer policy · 🟠 **잠정** · affected specs 03/04/10 |
  | New OD-MS-029 | SFN approval false / true path operating procedure · 🟢 **확정** · affected specs 04/10 |
  | Decision Summary | total 86 → 88 / 확정 43 → 44 / 잠정 40 → 41 |
  | First empirical validation reinforcement | OD-MS-009 · OD-MS-027 · OD-SAFE-001 · OD-SAFE-004 |
  | Change Log | Added 2026-06-23 entry |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | Change |
  | --- | --- |
  | New R-AUTO-022 | Risk that an execution_order still linked to a rejected `connector_order_request_id` after market-close REJECTED · `40580000` is not automatically resubmitted the next day · 🟢 **Mitigated** |
  | R-AUTO-021 mitigation | [2026-06-23 reinforcement] Step 1 · 12 · 13 · 17 bootstrap must remain even after SFN transition |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-23 follow-up note)

  | Category | Item |
  | --- | --- |
  | Completed | SFN approval false / true paths live-validated · Step 12 retry-normalizer deployed to EC2 + dry-run validated · confirmed normal Korean DB storage |
  | Follow-ups retained | EGW00215 balance-refresh rate-limit backoff · retry policy · Korean output display in PowerShell · SSM · AWS CLI · operational validation when an actual Step 12 retry-normalizer candidate occurs · review View Daily Batch display of retry-normalizer · approval-gate result |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Note above port-interest-crawler row | Added one paragraph: "2026-06-23 SFN approval workflow false · true paths live-validated · Step 12~17 true path completed with MarketConnector EC2 + SSM + SFN · confirmed REQUESTED → broker submit → fill sync → balance refresh path between Strategy Execution |
  | Note above port-interest-crawler row (continued) | and MarketConnector · retry-normalizer applied as an internal Step 12 safety reinforcement" |
  | Decision values (first choice / second choice / not recommended) | No change |

- 🟢 **Restricted first EventBridge automation scope to three Slack types**

  | Item | Value |
  | --- | --- |
  | New OD-MS-031 | 🟢 **확정** · affected specs 04 · 05 · 10 |
  | Initial Slack events | `APPROVAL_REQUIRED` + `DAILY_EXECUTION_SUCCESS` + `DAILY_EXECUTION_FAILED` |
  | Separated follow-ups | Pre-market balance · post-market balance · intraday stop-loss Slack |
  | Initial objective | Validate Slack receipt within SFN execution flow, not wording refinement |

- 🟢 **Retain existing port-view Slack**

  | Item | Value |
  | --- | --- |
  | Responsibility | port-view SlackNotificationService · notifications for View Daily Batch manual execution results |
  | This work | No removal · replacement |
  | Future | Only record possibility of migration to common notifier (no body change to OD-MS-010 · aligned with new OD-MS-030) |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | Item | Value |
  | --- | --- |
  | First reinforcement | 4 items: Lambda · EventBridge Scheduler · SFN · Secrets Manager · SSM Parameter Store · Slack notifier `portfolio-event-notifier` note · SFN Catch-path alignment · Slack webhook migration candidate · aligned with OD-MS-030 · OD-MS-031 · R-AUTO-023 · R-AUTO-024 |
  | Second reinforcement (Lambda / EventBridge Scheduler) | Dispatcher Lambda `portfolio-paper-daily-scheduler-dispatcher` as automation dispatcher · returns immediately after SFN StartExecution · Scheduler does not know KRX market holidays · Lambda guard is single responsibility · 2 schedules + staged activation (aligned with OD-MS-032 / R-AUTO-025) |
  | Existing body / decision values / cost model | No change |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md` (second · third notes)

  | Note | Content |
  | --- | --- |
  | Second 2026-06-23 note | Common AWS Slack notifier implemented + three SFN Slack events validated · new OD-MS-030 · OD-MS-031 · new R-AUTO-023 · R-AUTO-024 · Lambda remains not recommended for primary workloads · reinforced rationale for using it only as an operational-event notification helper service · recorded SFN + Scheduler + Lambda notifier from observability / portfolio-value perspective |
  | Second 2026-06-23 note (continued) | Initial Daily Batch Slack scope = 3 events · retain port-view Slack · no decision-value changes |
  | Third 2026-06-23 note | Scheduler + Dispatcher Lambda-based Daily automation complete · new OD-MS-032 · new R-AUTO-025 · Daily Batch orchestration 5-layer combination (SFN + Scheduler + Dispatcher Lambda + ECS RunTask + SSM RunCommand + AWS Batch) |
  | Third 2026-06-23 note (continued) | Lambda = orchestration-input adjustment · market-holiday guard · StartExecution dispatcher · no decision-value changes · staged activation aligned |

- 🟢 `.kiro/specs/_common/operator-decisions.md` (second · third entries)

  | Item | Value |
  | --- | --- |
  | Second 2026-06-23 entry | New OD-MS-030 · OD-MS-031 · first empirical reinforcement of OD-MS-009 · OD-MS-010 · OD-MS-029 |
  | Decision Summary update | total 88 → 90 / 확정 44 → 45 / 잠정 41 → 42 |
  | Third 2026-06-23 entry | New OD-MS-032 · first empirical reinforcement of OD-MS-009 · OD-MS-029 · OD-MS-031 |
  | Decision Summary update | total 90 → 91 / 확정 45 → 46 / 잠정 42 unchanged |

- 🟢 `.kiro/specs/_common/risk-register.md` (3 new items)

  | ID | Content |
  | --- | --- |
  | R-AUTO-023 | Risk of missing Slack notification on SFN failure path |
  | R-AUTO-024 | Risk of weakened secret management if Slack webhook URL remains long-term in Lambda environment variables |
  | R-AUTO-025 | Risk of Scheduler · Dispatcher Lambda · SFN integration failure · Status 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/followups-overview.md` (second · third follow-up notes)

  | Note | Content |
  | --- | --- |
  | Second 2026-06-23 (completed) | Slack notifier Lambda smoke test / template test / receipt validation of three SFN Slack events |
  | Second 2026-06-23 (follow-up) | Scheduled trigger entry · separate implementation of pre-market · post-market · intraday stop-loss Slack · migrate Slack webhook URL to Secrets Manager · SSM Parameter Store(SecureString) · review DLQ · retry · CloudWatch Alarm |
  | Third 2026-06-23 (completed) | Created 2 Schedulers · implemented Dispatcher Lambda · validated Scheduler → Lambda permissions · Lambda dryRun · Scheduler `get-schedule` status · retain 08:00 🟢 **ENABLED** / 09:01 🟠 **DISABLED** |
  | Third 2026-06-23 (follow-up) | Check tomorrow's 08:00 execution result |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | Added 2026-06-23 §9 | Results of validating three SFN Slack notifications |

- 🟢 `.kiro/README.md`

  | Item | Value |
  | --- | --- |
  | Added items | "Common AWS Slack notifier first validation complete (2026-06-23)" + "EventBridge automation resources implemented (2026-06-23)" |
  | Reference date | No change |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | Accumulated bullets in 2026-06-23 section | Slack notifier implementation complete · three Slack notifications validated · initial EventBridge automation scope limited to 3 events · retain port-view Slack |
  | Second accumulation | Created 2 Schedulers · implemented Dispatcher Lambda · IAM `simulate-principal-policy` + Lambda dryRun + Scheduler `get-schedule` status validation · staged activation with 08:00 ENABLED + 09:01 DISABLED · first real `StartExecution` validation planned for tomorrow at 08:00 |
  | 2026-06-23 section | Newly prepended |

### Security

  | Item | Result |
  | --- | --- |
  | Plaintext sensitive information recorded | secret · KIS app key · secret · account number · password · token · RDS password · RDS endpoint · account-id · IAM Role ARN · secret ARN · access key id · instance-id · EIP · image digest |
  | Plaintext sensitive information recorded (continued) | task ARN · job ARN · full broker response · webhook URL · KIS paper login credential · Administrator password plaintext records · 0 (all `[REDACTED]` or placeholders) |
  | DB Korean validation SQL result | confirmed both `server_encoding` · `client_encoding` are `UTF8` · Korean text normal across 4 tables (summarized without sensitive information) |
  | Plaintext Slack webhook URL recording | 0 (`SLACK_WEBHOOK_URL` is key name · meaning only · value · Lambda code body · Slack message · SFN Catch state ASL · Slack webhook response quotation 0 · aligned with R-DOCS-001) |
  | Webhook URL migration plan | initial-validation only → Secrets Manager / SSM Parameter Store(SecureString) · new R-AUTO-024 · Status 🔵 **Accepted** · follow-up in spec 06 · Lambda runtime retrieval · IAM Role Resource ARN-scoped read (wildcard 0 · aligned with OD-SEC-006) |
  | Webhook-exposure grep check | Lambda console · `get-function-configuration` response · CloudFormation · SAM · Terraform state · git history may be checked · value set directly by operator · Kiro records only key name · meaning |
  | `-AllowPaperOrderExecute` use | limited to approval point on SFN approval true path · one `282330` SELL 17-share order on this date |
  | Additional BUY · cancel · modify · `--execute` | 0 |
  | SELL position `mark_position_sell_ordered()` responsibility | At successful MarketConnector executor SELL point (aligned with OD-MS-016) |
  | Automatic fill · position-sync retry | 0 |
  | aws-live work | 0 |
  | `DAILY_EXECUTION_FAILED` validation | test-only failure injection · 0 intentionally induced real broker-order failures (R-AUTO-023 mitigation · aligned with OD-SAFE-001 ~ 004 · 0 additional broker calls) |
  | AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · RDS · GRANT · KIS · SFN · S3 · CloudWatch · Lambda · Slack · Scheduler work | all performed directly by operator · Kiro only updated root / `_common` / 04 spec operation-notes documentation |
  | AWS CLI · boto3 execution | 0 |
  | AWS resource creation · modification · deletion | 0 |
  | Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
  | Full plaintext quotation of CloudWatch Logs · SSM responses · KIS API · SFN history · Slack messages · Lambda code · get-schedule · `simulate-principal-policy` · dryRun responses · Docker · PowerShell stdout | 0 |
  | Full replacement body / retry-normalizer body of `connector_strategy_order_execute.py` quoted | 0 (R-DOCS-001 · follow-up responsibility for `_common` metadata / 03 spec update) |
  | Lambda `portfolio-event-notifier` · Dispatcher Lambda code · environment variables · IAM inline policy · SFN Catch ASL · Target Role · Target Lambda ARN body quoted | 0 |
  | Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging spec areas | 0 |

<details><summary>🔵 Operational identifier summary</summary>

  | Item | Value |
  | --- | --- |
  | SFN state machine | `portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval` |
  | `strategy_execution_order id` | `40` |
  | `connector_order_request id` | `48` |
  | `broker_order_no` | `0000006143` |
  | Ticker code | `282330` (stock name · quantity 17) |
  | approval gate | true · false labels |
  | DB session table name | 4 types |
  | Lambda names | `portfolio-event-notifier` · `portfolio-paper-daily-scheduler-dispatcher` |
  | IAM Roles | `portfolio-event-notifier-lambda-role` · `portfolio-paper-eventbridge-scheduler-role` · `portfolio-paper-daily-scheduler-dispatcher-role` |
  | Inline policies | `portfolio-paper-scheduler-start-execution-policy` · `portfolio-paper-scheduler-invoke-dispatcher-policy` · `portfolio-paper-daily-scheduler-dispatcher-policy` |
  | Schedulers | `portfolio-paper-daily-step1-11-approval-0800-kst` · `portfolio-paper-daily-step12-17-order-0901-kst` |
  | cron expressions | `cron(0 8 ? * MON-FRI *)` · `cron(1 9 ? * MON-FRI *)` |
  | Timezone · Runtime · Handler | `Asia/Seoul` · `Python 3.12` · `lambda_function.lambda_handler` |
  | Timeout / Memory | `30s` / `256MB` |
  | Environment-variable keys | `SLACK_WEBHOOK_URL` · `TIMEZONE` · `HOLIDAY_COUNTRY` · `FAIL_CLOSED_ON_HOLIDAY_ERROR` (values not recorded) |
  | Slack events | `APPROVAL_REQUIRED` · `DAILY_EXECUTION_SUCCESS` · `DAILY_EXECUTION_FAILED` |
  | scheduleType | `STEP1_11_APPROVAL` · `STEP12_17_ORDER` |
  | Target input JSON | `{"scheduleType":"...","dryRun":false}` |
  | Completion markers | `SLACK_LAMBDA_SMOKE_TEST=SUCCESS` · `PORTFOLIO_EVENT_NOTIFIER_TEMPLATE_TEST=SUCCESS` |
  | dryRun response | `started=false` · `reason=DRY_RUN_NO_START_EXECUTION` |
  | Smoke message / bot name | `hello wook` / `Portfolio Daily Bot` |

</details>

## 2026-06-22 (Daily AWS Paper 1~17 complete + MarketConnector env bootstrap + Step 9 permission correction + SELL E2E validation)

### Added

- 🟢 **Daily AWS Paper Wrapper Step 1~17 live-operation completion**

  | Item | Value |
  | --- | --- |
  | wrapper | `.kiro/scripts/run-daily-aws-paper.ps1` |
  | Range | All steps Step 1 ~ Step 17 in live operation |
  | Final state | Actual Paper SELL order submission · KIS acceptance · fill query · SELL fill sync · position CLOSED · balance refresh end-to-end passed |
  | Record meaning | First complete operational run of AWS operations wrapper |
  | Environment · RunDate · region | `aws-paper` · `2026-06-22` · `ap-northeast-2` |
  | 1 KIS paper SELL | `088350` 한화생명 244 shares MARKET |
  | `execution_order id 37` | 🟢 **SUBMITTED** |
  | `connector_order_request id 46` | 🟢 **ACCEPTED** |
  | Plaintext broker response recording | 0 (aligned with R-DOCS-001) |

- 🟢 **MarketConnector env bootstrap regeneration pattern**

  | Item | Value |
  | --- | --- |
  | Target file | `daily-aws-paper.functions.ps1` (patched directly by operator) |
  | Function role | Added new MarketConnector env bootstrap · extract keys inside Secrets Manager JSON SecretString · export APP_* / KIS_* aliases · do not print secret values · chmod 700 `/tmp/inject-env.sh` · regenerate at step execution time |
  | Call sites | Common to Steps 1 · 12 · 13 · 17 |
  | Validation | PowerShell parser validation passed · Step 1 rerun succeeded · handles `/tmp` volatility after EC2 stop / start |
  | Body quotation | 0 (aligned with R-DOCS-001 · new OD-MS-027 · new R-AUTO-021 mitigation) |

- 🟢 **Step 9 `execution_app` permission correction**

  | Item | Value |
  | --- | --- |
  | GRANT (operator direct) | `GRANT USAGE ON SCHEMA decision TO execution_app` + `GRANT UPDATE ON TABLE decision.strategy_daily_position_decision TO execution_app` |
  | Permission confirmation | SELECT + UPDATE |
  | Result | After Step 9 creates SELL execution_order, daily position decision `execution_order_id` can be UPDATEd |
  | Step 9 ~ Step 11 rerun | 🟢 **Passed** |
  | Decisions · risks | New OD-DB-011 · first empirical validation of new R-DATA-013 mitigation · aligned with R-DATA-005 [2026-06-22 reinforcement] |
  | Formal 02 spec db-roles-and-grants matrix update | Follow-up retained |

- 🟢 **Actual Paper SELL order E2E validation results**

  | Item | Value |
  | --- | --- |
  | execution plan id | `96` |
  | `plan_date` | `2026-06-22` |
  | `plan_status` | `PARTIALLY_BLOCKED` |
  | total_candidate · ready · blocked · skipped | 3 · 1 · 2 · 0 |
  | total_target_amount | `1,237,080` |
  | available_cash | `-62,763` |
  | max_order_amount | `-31,381.50` |
  | Step 12 target | 1 `088350` 한화생명 SELL 244 shares MARKET (2 BUY BLOCKED for insufficient cash · duplicate orders 0 rows) |
  | Sell reason | `SELL_HARD_STOP` |
  | entry_date / entry_price | `2026-06-17` / `5,744.4057` |
  | current_price | `5,070` |
  | expected_pnl_rate | approx. `-11.7402%` |
  | hard_stop_loss_rate | `-10%` |
  | holding_days | 5 |
  | snapshot_qty · sellable_qty · remaining_qty | all 244 |
  | expected_pnl_amount | approx. `-164,554.9908` |
  | Step 13 fill query | connector_fill id `34` · fill_qty 244 · fill_price `5,075.8607` · fill_amount `1,238,510.01` · side SELL · fill_ts `2026-06-22 00:46:58 UTC` |
  | Step 14 · 15 · 16 ECS exitCode | 0 |
  | Step 17 SSM | 🟢 **Success** |
  | `strategy_position_state id 9` | remaining_qty 0 · position_status 🟢 **CLOSED** · latest_sell_reason `SELL_HARD_STOP` |
  | Latest `connector_position_snapshot` created_at | `2026-06-22 00:50:50 UTC` |
  | 5 positions | `003490` 58 shares · `004990` 69 shares · `023530` 8 shares · `042660` 11 shares · `282330` 17 shares |
  | `088350` 한화생명 | Confirmed removed from balance snapshot |

- 🟢 **New decisions · risks**

| Item | Value |
| --- | --- |
| New OD-MS-027 | **Status**: 🟠 **잠정**<br>**Note**: MarketConnector env-bootstrap regeneration policy · remove assumption that `/tmp/inject-env.sh` preexists · regenerate at step execution time · shared wrapper function · no secret-value output · chmod 700 |
| New OD-DB-011 | **Status**: 🟢 **확정**<br>**Note**: Limited UPDATE permission for `execution_app` on `decision.strategy_daily_position_decision` · limited to responsibility for SELL execution-link update |
| New R-AUTO-021 | **Status**: 🟢 **Mitigated**<br>**Note**: Risk of Steps 1 · 12 · 13 · 17 failing due to `/tmp` volatility after MC EC2 stop · start |
| New R-DATA-013 | **Status**: 🟢 **Mitigated**<br>**Note**: Risk of Step 9 SELL execution-link update failure due to missing `decision` schema UPDATE permission for `execution_app` |

### Changed

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-22 §1~§6 | MarketConnector env-bootstrap regeneration + Paper SELL submission validation |
  | (a) Initial Step 1 failure cause | `/tmp/inject-env.sh not found` (`/tmp` volatility after EC2 stop / start + SSM step assuming env file preexists) |
  | (b) Function addition | `daily-aws-paper.functions.ps1` MarketConnector env bootstrap · called by Steps 1 · 12 · 13 · 17 |
  | (c) Value handling | Extract keys from JSON SecretString · export APP_* / KIS_* aliases · do not print secret values |
  | (d) Step 1 rerun | Succeeded |
  | (e) Step 12 | 한화생명 244-share SELL MARKET submission succeeded · broker_order_no / broker_branch_code created · 0 plaintext broker responses in body · `[REDACTED]` aligned |
  | (f) Step 13 | Fill query succeeded + safety / security checks |

- 🟢 `.kiro/specs/03-marketconnector-ec2/runbook.md` §2 (env injection)

  | Item | Value |
  | --- | --- |
  | Explicit note | During Daily wrapper operation, Steps 1 · 12 · 13 · 17 regenerate `/tmp/inject-env.sh` at step execution time using the wrapper's common bootstrap function |
  | Explicit note (additional) | `/tmp` may be volatile after stop / start · if `/tmp/inject-env.sh not found` or env missing occurs, check bootstrap-function application · keep secret values out of logs · retain chmod 700 |
  | Step 12 live order | Allowed only when `-AllowPaperOrderExecute` is explicitly provided (re-emphasize safety gate) |

- 🟢 `.kiro/specs/03-marketconnector-ec2/tasks.md`

  | Item | Value |
  | --- | --- |
  | tasks 7 · 26 · 27 | Added 2026-06-22 reinforcement note · MarketConnector Daily wrapper SSM-step env bootstrap regeneration reflected · handling for `/tmp` volatility after stop · start complete · Step 12 Paper SELL submission validation complete |
  | Formalize systemd / startup script | Follow-up retained |

- 🟢 `.kiro/specs/03-marketconnector-ec2/validation-checklist.md`

  | Item | Value |
  | --- | --- |
  | Accumulated 2026-06-22 validation | MarketConnector env-bootstrap regeneration validation · Step 1 CONNECTOR_BALANCE rerun succeeded · Step 12 Paper SELL submission succeeded · Step 13 fill query succeeded · no secret-value log exposure |
  | Confirmation actor | Marked as passed directly by operator |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-22 section | Daily AWS Paper execution Steps 6 ~ 17 operational validation |
  | Step 6 · 7 · 8 · 10 · 11 · 14 · 15 · 16 · `--execute` | First pass of internal strategy-execution state updates |
  | Step 9 | Initial missing-permission failure → passed after GRANT correction |
  | Step 12 ~ 17 | Actual SELL order · fill · synchronization · balance refresh passed |
  | Wording | "Manual orchestration validation based on Daily wrapper" · not SFN implementation itself · retain as follow-up orchestration target |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-22 section | Daily AWS Paper Step 2 / Step 3 revalidation |
  | Step 2 INTEREST_CRAWLER | 🟢 **Success** · non-GUI ECS exitCode 0 · Windows KRX worker SSM Success · KRX raw DB validation Success · ExpectedKrxRawDate `2026-06-19` passed · 2026-06-21 OD-MS-026-aligned Daily run revalidation |
  | Step 3 PREPROCESSOR | ECS exitCode 0 |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md`

  | Item | Value |
  | --- | --- |
  | tasks 57 · 105 · 107 · 110 | 2026-06-22 Daily run revalidation complete · Step 2 KRX raw DB validation · Scheduled Task wait · Last Result · worker-stopped fail-closed · Step 3 Preprocessor Daily wrapper passed again |
  | EC2 lifecycle · CloudWatch Logs Agent · SFN hybrid orchestration · View display linkage | Follow-up retained |

- 🟢 `.kiro/specs/09-strategy-research-batch/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-22 section | Daily AWS Paper Step 4 / Step 5 revalidation |
  | Step 4 BACKTEST_RESEARCH AWS Batch | 🟢 **SUCCEEDED** |
  | Step 5 BACKTEST_REPORT AWS Batch | 🟢 **SUCCEEDED** |
  | State | Research · report passed normally during Daily wrapper 1 ~ 17 operation · heavy-classification SubmitJob remains 0 (aligned with OD-MS-019 / R-AUTO-015) |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Added Change Log 2026-06-22 entry | Complete |
  | New decisions | OD-MS-027 · OD-DB-011 |
  | First empirical reinforcement | OD-MS-016 · OD-MS-021 · OD-MS-023 |
  | Decision Summary | Total 84 → 86 / provisional 39 → 40 / confirmed 42 → 43 |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | Change |
  | --- | --- |
  | New R-AUTO-021 | Registered |
  | New R-DATA-013 | Registered |
  | R-DATA-005 detection · mitigation | [2026-06-22 reinforcement] Added case of missing execution_app decision-schema USAGE / table UPDATE · formal matrix update follow-up |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-22 follow-up note)

  | Item | Value |
  | --- | --- |
  | On SFN migration | Include regeneration logic for MC env bootstrap in Steps 1 · 12 · 13 · 17 |
  | Step 9 permission requirement | Reflect in DB-role bootstrap / grant documentation |
  | Step 12 | Reinforce pre-order DB preflight view or wrapper-summary output |
  | Step 13 | Reinforce automatic summary of connector_fill · order_request · execution_order states |
  | Step 17 | Include latest balance-snapshot confirmation that liquidated ticker was removed in wrapper summary |
  | Other | Follow-up for PGPASSWORD / psql operational convenience runbook |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Target rows | port-interest-crawler · Strategy Decision · Execution · MarketConnector · Research Batch |
  | Added note | One paragraph: "2026-06-22 Daily AWS Paper 1~17 second full live run (first actual SELL E2E)" |
  | Combination | PowerShell wrapper + ECS RunTask + AWS Batch + SSM RunCommand · second E2E rehearsal succeeded |
  | SFN | Retained as follow-up orchestration target |
  | Decision values (primary / secondary / not recommended) | No change |

- 🟢 `.kiro/README.md`

  | Item | Value |
  | --- | --- |
  | Reference date | `2026-06-21` → `2026-06-22` |
  | Progress-summary rows 03 · 04 · 08 · 09 + Backend AWS E2E 17-step progress row | First live SELL E2E completed · Step 9 permission correction · env bootstrap regeneration |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-22 section | Newly prepended (Daily AWS Paper 1~17 complete + MC env bootstrap + Step 9 permission correction + SELL E2E validation) |

### Security

  | Item | Result |
  | --- | --- |
  | Plaintext recording of secret / KIS · account · token · RDS · account-id · IAM Role ARN · secret ARN · access key · instance-id · EIP · image digest · task ARN · job ARN · raw broker_order_no · raw broker_branch_code · KIS paper credential · Administrator password | 0 (all `[REDACTED]` or placeholders) |
  | `-AllowPaperOrderExecute` use | Limited to standalone Step 12 approval point · 1 한화생명 SELL · option explicitly specified directly by operator (R-AUTO-019 mitigation · wrapper summary `PaperOrder : True` can be verified afterward) |
  | Additional BUY · cancel · modify · `--execute` | 0 |
  | SELL position `mark_position_sell_ordered()` calls | Responsibility at MarketConnector executor SELL-success point (aligned with OD-MS-016) |
  | AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · SSM Parameter Store · RDS · GRANT · KIS · S3 · CloudWatch · Docker · ECR work | All performed directly by operator · Kiro only organized documentation · procedures · validation items |
  | AWS CLI · boto3 executions | 0 |
  | AWS resource creations · modifications · deletions | 0 |
  | Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
  | Plaintext quotation of full CloudWatch Logs · SSM response · KIS API · Docker build/push · PowerShell stdout | 0 |
  | RDS DDL | 0 |
  | RDS DML (limited to normal 17-step flow) | See rows below |
  | · `decision.strategy_daily_signal` | Recorded |
  | · `decision.strategy_daily_run` | Recorded |
  | · `decision.strategy_daily_position_decision` | SELL HOLD decision + execution_order_id link UPDATE |
  | · `execution.strategy_execution_plan` | insert (`id 96`) |
  | · `execution.strategy_execution_order` | insert · update (`id 37` READY → REQUESTED → SUBMITTED → FILLED) |
  | · `execution.strategy_position_state` | update (`id 9` remaining_qty 244 → 0 / OPEN → CLOSED / latest_sell_reason `SELL_HARD_STOP`) |
  | · `connector.connector_order_request` | insert (`id 46` ACCEPTED → FILLED) |
  | · `connector.connector_order_event` | Recorded |
  | · `connector.connector_fill` | insert (`id 34`) |
  | · `connector.connector_balance_snapshot` | insert |
  | · `connector.connector_position_snapshot` | insert (5 positions) |
  | GRANT (operator direct) | `USAGE ON SCHEMA decision` + `UPDATE ON decision.strategy_daily_position_decision` to `execution_app` |
  | Quotation of operator patch body (`daily-aws-paper.functions.ps1` MC env bootstrap · GRANT SQL) | 0 (facts only recorded) |
  | Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging spec area | 0 |

<details><summary>🔵 Operational identifier summary</summary>

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

## 2026-06-21 (Strengthened Step 2 `INTEREST_CRAWLER` success criteria + integrated KRX raw DB validation)

### Added

- 🟢 `port-interest-crawler/interest_krx_raw_validate_daily.py` (new · written directly by operator)

  | Item | Value |
  | --- | --- |
  | Targets | `interest_program_raw` · `interest_shortsell_raw` |
  | Validation | row_count + max(trade_date) based on expected trade_date |
  | Failure handling | Return exit code 30 |
  | Local validation | py_compile · UTF-8 read · program · shortsell · exit30 marker passed |
  | S3 deployment | `s3://portfolio-paper-migration-yukiever/tmp/krx/interest_krx_raw_validate_daily.py` → `C:\portfolio\port-interest-crawler\interest_krx_raw_validate_daily.py` |
  | Standalone EC2 validation | crawler_app · interest schema · search_path `interest, reference, legacy, public` |
  | interest_program_raw | expected=`2026-06-19` · max_date=`2026-06-19` · expected_count=`1` |
  | interest_shortsell_raw | expected=`2026-06-19` · max_date=`2026-06-19` · expected_count=`349` |
  | exit code | 0 |

- 🟢 Integrated KRX raw DB validation SSM step into `step-02-interest-crawler.ps1`

  | Item | Value |
  | --- | --- |
  | SSM step | `INTEREST_CRAWLER_KRX_DB_VALIDATE` |
  | wrapper handling | Calculate `ExpectedKrxRawDate` (previous trading day based on RunDate) → call Windows crawler-worker EC2 via SSM |
  | Invocation command | `load-crawler-db-env.ps1` + `venvs/interest-crawler` venv + `interest_krx_raw_validate_daily.py --expected-date <yyyy-mm-dd>` |
  | Failure conditions | row_count 0 or non-zero exit → Step 2 fail |
  | step result | Includes `KrxDbValidationCommandId` |

- 🟢 **New decisions · risks**

| Item | Value |
| --- | --- |
| New OD-MS-026 | **Status**: 🟠 **잠정** · **Note**: Step 2 success criterion = KRX raw DB validation, not Scheduled Task trigger · KRX GUI path retains Windows Administrator interactive Scheduled Task · wrapper executes · waits for completion · Last Result · latest log |
| New OD-MS-026 (continued) | DB-validation orchestration · validation script validates raw freshness · worker stopped is fail-closed |
| New R-AUTO-020 | **Status**: 🟢 **Mitigated** · **Note**: Risk that KRX raw non-load propagates past Step 3 when Step 2 SUCCESS is determined only from successful Scheduled Task trigger |
| New R-AUTO-020 (continued) | mitigation = Chrome / chromedriver best-effort reset + Running → Ready wait + Last Result + latest log + DB validation + worker-stopped fail-closed |

### Changed

- 🟢 Strengthened success determination in `step-02-interest-crawler.ps1`

  | Item | Value |
  | --- | --- |
  | (a) Pre-execution reset | Chrome / chromedriver stale-process best-effort reset · reset failure is warning |
  | (b) Execution path | Retain Administrator interactive Scheduled Task `Portfolio-KRX-Worker-Daily` `schtasks /Run` · reject SSM direct python · KRX GUI-dependent structure · based on Windows Administrator interactive session |
  | (c) State wait | After Scheduled Task Running polling, wait for return to Ready · `sawRunning` log · Step 2 fails on timeout |
  | (d) Result determination | Only Last Result 0 or 0x0 is <span style="color:#1A7F37">**SUCCESS**</span> · do not treat trigger success alone as SUCCESS |
  | (e) Log output | Latest `C:\portfolio\logs\krx_worker_daily_*.log` path · last write time · size · tail |
  | (f) worker fail-closed | Fail immediately unless `running` · print instanceId · state · block Step 2 SUCCESS when KRX GUI worker · DB validation has not run · remove previous automatic skip · update R-AUTO-016 mitigation |

- 🟢 Reinforced non-GUI ECS RunTask overrides in `daily-aws-paper.functions.ps1`

  | Item | Value |
  | --- | --- |
  | Environment variables | `TEMP=/tmp` · `TMP=/tmp` · `PYTHONUTF8=1` · `PYTHONIOENCODING=utf-8` |
  | Common function | `Invoke-DailyAwsPaperEcsTask` `EnvironmentVariables` parameter · pass ECS RunTask `containerOverrides.environment` |
  | Common function | `New-SsmParameterFile` `ExecutionTimeoutSeconds` parameter · pass `ExecutionTimeoutSeconds` to `Invoke-SsmCommandAndWait` |
  | Purpose | Reduce Windows / Linux encoding differences · enforce UTF-8 for crawler logs · file processing · specify temporary-file path · control SSM timeout for long-running KRX worker · DB validation |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md`

  | Item | Value |
  | --- | --- |
  | Accumulated 2026-06-21 section | Strengthened Step 2 success determination · Chrome reset · Scheduled Task wait + Last Result · latest worker log · validation-script creation + S3 presigned URL deployment + standalone EC2 validation · step-02 DB validation integration · worker-stopped fail-closed · standalone Step 2 execution validation succeeded |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md`

  | Item | Value |
  | --- | --- |
  | task 57 (automatically add DB validation within wrapper) | Complete |
  | task 92 (clean up remaining Chrome processes) | First pass complete |
  | New task items | KRX GUI worker Scheduled Task wait + Last Result · create KRX raw DB-validation script + S3 deployment + standalone EC2 validation · step-02 DB-validation integration · worker-stopped fail-closed · standalone Step 2 execution validation success |
  | Follow-up retained | EC2 lifecycle · SFN hybrid orchestration · CloudWatch Logs Agent · View display linkage |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/design.md`

  | Item | Value |
  | --- | --- |
  | Hybrid execution model Step 2 success conditions | non-GUI ECS exitCode 0 · Crawler Worker EC2 running · Windows Scheduled Task Running → Ready · Last Result 0 or 0x0 · latest worker log path · tail · KRX raw DB validation passed |
  | Explicit note | worker-stopped fail-closed · KRX raw validation script used as Step 2 guard |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/requirements.md`

  | Item | Value |
  | --- | --- |
  | Acceptance Criteria reinforcement | Successful Scheduled Task trigger alone cannot make Step 2 succeed · successful `interest_program_raw` · `interest_shortsell_raw` validation against `ExpectedKrxRawDate` is success condition · worker stopped is fail-closed, not skip |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Change Log 2026-06-21 | Added |
  | OD-MS-026 | New |
  | Decision Summary | Updated |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | Change |
  | --- | --- |
  | R-AUTO-020 | New |
  | R-AUTO-007 / R-AUTO-016 / R-AUTO-017 | mitigation · detection reinforced |
  | R-AUTO-016 wording "automatic skip inside wrapper" | Updated to fail-closed on this date |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-21 follow-up note)

  | Category | Item |
  | --- | --- |
  | Completed | Strengthened Step 2 success determination · integrated KRX raw DB validation into wrapper |
  | Follow-up retained | EC2 lifecycle automatic start · stop · SFN mixed orchestration · View display linkage · centralized worker-log collection |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Added note to port-interest-crawler row | Step 2 success conditions (non-GUI ECS exitCode 0 + KRX GUI worker Scheduled Task wait + Last Result + latest log + KRX raw DB validation) + worker-stopped fail-closed |
  | Decision values (primary / secondary / not recommended) | No change |

- 🟢 `.kiro/README.md`

  | Item | Value |
  | --- | --- |
  | "Windows KRX crawler worker and Step 2 behavior" section | Removed previous wording "automatically skips KRX GUI Scheduled Task trigger inside wrapper" · now fail-closed |
  | Standalone Step 2 execution example | `.\run-daily-aws-paper.ps1 -Environment aws-paper -RunDate 2026-06-20 -StartStep 2 -EndStep 2` |
  | Explicit Step 2 success conditions | non-GUI ECS exitCode 0 · Crawler Worker EC2 running · Scheduled Task Running → Ready · Last Result 0 · latest worker log · KRX raw DB validation |
  | "Current progress summary" row 08 | Updated |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-21 section | Newly accumulated (strengthened Step 2 success determination + integrated KRX raw DB validation) |

### Security

  | Item | Result |
  | --- | --- |
  | Plaintext recording of secret / KIS · account · token · RDS · account-id · IAM Role ARN · secret ARN · access key · EIP · image digest · task ARN · job ARN · KIS paper credential · Administrator password · actual S3 presigned URL | 0 (all `[REDACTED]` or placeholders) |
  | broker / KIS calls · new BUY / SELL / cancel / modify / `--execute` | 0 |
  | SELL position `mark_position_sell_ordered()` calls | 0 |
  | Automatic fill · position sync retries · aws-live work | 0 (this date limited to `aws-paper`) |
  | AWS · SSM · EC2 · S3 · ECS · RDS · KRX calls | All performed directly by operator · Kiro only organized documentation · procedures · validation items |
  | AWS CLI · boto3 executions · AWS resource creations · modifications · deletions | 0 |
  | Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
  | Plaintext quotation of full CloudWatch Logs · SSM response · PowerShell stdout | 0 |
  | RDS DDL | 0 |
  | RDS DML | Limited to validation SQL SELECT · new rows in `interest_program_raw` · `interest_shortsell_raw` are aligned with KRX worker load results · this date's new Step 2 wrapper execution was idempotent · no-op |
  | Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · packaging spec area | 0 |
  | Quotation of operator patch bodies (`interest_krx_raw_validate_daily.py` · `step-02-interest-crawler.ps1` · `daily-aws-paper.functions.ps1`) | 0 (aligned with R-DOCS-001 · facts only recorded) |

<details><summary>🔵 Operational identifier summary</summary>

  | Item | Value |
  | --- | --- |
  | SSM commandId (4) | `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3` · `c844aea5-1429-430a-9510-39fc99f17f05` · `2279c6d7-2da6-4317-9c10-7cc77374b317` · `f9d82fcc-1e26-4710-87c3-1d20483b63ef` |
  | RunId | `daily-aws-paper-20260621-204017` |
  | taskDefinition | `portfolio-paper-interest-crawler:7` |
  | taskId | `78979b5cbb714d0eb94f5946e15a14ce` |
  | S3 key | `tmp/krx/interest_krx_raw_validate_daily.py` |
  | Scheduled Task | `Portfolio-KRX-Worker-Daily` |
  | latest worker log | `krx_worker_daily_20260621_114154.log` |
  | wrapper options | `-StartStep` · `-EndStep` |
  | DB session | user · search_path · row_count |

</details>

## 2026-06-20 (Final AWS automatic-wrapper review + safe stop of duplicate 6/18 execution attempt + identified EC2 lifecycle follow-up need)

### Changed

- 🟢 **Documentation updates**

  | Target | Content |
  | --- | --- |
  | `.kiro/WORKLOG.md` 2026-06-20 section | Accumulated |
  | `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` 2026-06-20 | Accumulated |
  | `.kiro/specs/_common/followups-overview.md` 2026-06-20 follow-up note | Reinforce automatic EC2 lifecycle start / stop · handle loss of MC EC2 `/tmp/inject-env.sh` after stop · start · prohibit Step 12 order submission until separate approval |

- 🟢 **Final review of Daily AWS Paper Wrapper structure · safety criteria · EC2 startup criteria**

  | Item | Value |
  | --- | --- |
  | wrapper structure | `run-daily-aws-paper.ps1` + `daily-aws-paper.config.ps1` + `daily-aws-paper.functions.ps1` + `steps/step-01 ~ step-17` |
  | Safety criteria | Step 1 ~ Step 11 before broker · KIS order submission · only Step 12 submits actual KIS paper orders · blocked unless `-AllowPaperOrderExecute` specified · Step 12 excluded on weekends · market holidays |
  | EC2 startup criteria | MarketConnector EC2 required for Steps 1 · 12 · 13 · 17 · Crawler Worker EC2 required for Step 2 KRX GUI worker · both EC2s must be started before wrapper execution if stopped · MC `/tmp/inject-env.sh` may be lost after EC2 stop · start · lifecycle reinforcement follow-up |

- 🟢 **Safely stopped duplicate 6/18 wrapper execution attempt**

  | Item | Value |
  | --- | --- |
  | RunDate | `2026-06-18` |
  | Range | Attempted rerun of Step 1 ~ Step 11 |
  | Initial Step 1 failure | `/tmp/inject-env.sh` lost → passed after regeneration |
  | Step 2 | non-GUI ECS exitCode 0 · KRX worker Scheduled Task trigger succeeded |
  | Step 3 | Stopped with Ctrl+C immediately after entering PREPROCESSOR |
  | State after rerun | 0 ECS RUNNING tasks · 0 AWS Batch RUNNING · SUBMITTED · PENDING · RUNNABLE · Scheduled Task State `Ready` · LastTaskResult 0 · Step 12 not run |
  | New DB rows | execution_plan 0 · strategy_execution_order 0 · connector_order_request 0 · new KIS order submissions 0 |
  | Existing history | `execution_plan_id 94` BUY 4 · 🟢 **FILLED** 4 · 4 connector-linked items completed normally |
  | Shutdown handling | Stopped Crawler Worker · MarketConnector EC2 |

- 🟢 **6/19 KRX raw freshness recovery state**

  | Table | Value |
  | --- | --- |
  | `interest_program_raw` max_date | `2026-06-19` |
  | `interest_program_raw` 2026-06-18 row_count | `1` |
  | `interest_program_raw` 2026-06-19 row_count | `1` |
  | `interest_shortsell_raw` max_date | `2026-06-19` |
  | `interest_shortsell_raw` 2026-06-18 row_count | `349` |
  | `interest_shortsell_raw` 2026-06-19 row_count | `349` |
  | Conclusion | KRX raw reference freshness recovery complete |
  | Follow-up | Identified limitation of success determination centered on Scheduled Task trigger / LASTEXITCODE → separated Step 2 wrapper success-criteria reinforcement to 2026-06-21 |

### Security

  | Item | Result |
  | --- | --- |
  | Plaintext recording of secret · account · token · RDS · account-id · ARN · access key · EIP · image digest · task ARN · job ARN · KIS paper credential · Administrator password | 0 (all `[REDACTED]` or placeholders) |
  | Step 12 execution · broker · new KIS order submissions · aws-live work · automatic fill · position sync retry · `mark_position_sell_ordered()` calls | 0 (this date limited to `aws-paper`) |
  | AWS · SSM · EC2 · RDS calls | All performed directly by operator · Kiro only organized documentation · procedures |
  | AWS CLI · boto3 executions · AWS resource creations · modifications · deletions | 0 |
  | RDS DDL · DML | 0 (SELECT only) |
  | Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging spec area | 0 |

<details><summary>🔵 Operational identifier summary</summary>

  | Item | Value |
  | --- | --- |
  | `execution_plan_id` | `94` |
  | RunDate | `2026-06-18` |
  | Validation targets | `interest_program_raw` · `interest_shortsell_raw` row_count · max_date |

</details>

## 2026-06-18 (Defaulted Step 13 `connector_order_check.py` to sequential per-order queries — operational safety reinforcement)

### Changed

- 🟢 `port-marketconnector/connector_order_check.py` (patched directly by operator)

  | Item | Value |
  | --- | --- |
  | Default mode change | Broad batch fill query → sequential query for active orders |
  | local commit | `75cb804` (remote push destination unset · no git push · separated as follow-up) |
  | EC2 deployment | Via S3 |
  | Body quotation | 0 (aligned with R-DOCS-001) |
  | New options | `--broad` (legacy broad mode · not used by default) · `--active-limit` (max sequential active-order query count) |
  | Explicit order query | `--code {ticker_code} --order-no {broker_order_no} --no-broad` (without broad fallback) |
  | Safety criteria | Prohibit use of `output2 summary-only` as DB-update evidence when multiple active orders exist · allow summary fallback only when exactly one `connector_order_request` candidate is confirmed by order number / ticker code · prevent incorrect mapping to `connector_order_event` · `connector_fill` (aligned with R-AUTO-018 [2026-06-18 additional reinforcement] mitigation) |

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md` (2026-06-18 §1~§6)

  | Section | Content |
  | --- | --- |
  | (a) Background | Operational-safety reinforcement for KIS paper fill-query response pattern (`output1 empty` + `output2 summary-only`) · not an operational failure · design change · block propagation of incorrect connector_fill into Steps 15 / 16 |
  | (b) Change | Changed default execution mode · added `--broad` · `--active-limit` options · explicit order-query combination · wrapper ps1 handles execution orchestration only · fill-query method controlled inside `connector_order_check.py` (aligned with new OD-MS-025) |
  | (c) Local validation | `python -m py_compile` passed · confirmed `--help` options · commit `75cb804` |
  | (d) S3 upload + EC2 deployment | bucket `portfolio-paper-migration-yukiever` · key `deploy/marketconnector/connector_order_check.py` · `39159 bytes` · EC2 backup `connector_order_check.py.bak-20260618-step13-per-order` · ownership restored to `ec2-user:ec2-user` |
  | (e) EC2 validation + single direct-only query | `python3 connector_order_check.py --code 004990 --order-no 0000025576 --no-broad` · SSM Status 🟢 **Success** · ResponseCode `0` · StdErr empty |
  | (f) Standalone wrapper Step 13 execution | `.\run-daily-aws-paper.ps1 -Environment aws-paper -RunDate 2026-06-18 -StartStep 13 -EndStep 13` (not `FromStep` / `ToStep`) · SSM commandId `b344d404-07a4-4bb6-9d63-34151e648bab` · status 🟢 **Success** · responseCode `0` · Step 13 🟢 **COMPLETED** |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Second Change Log 2026-06-18 entry | Added |
  | New OD-MS-025 | <span style="color:#BF8700">**잠정**</span> · `connector_order_check.py` operations mode = sequential active-order query by default · broad batch query is legacy · only when `--broad` specified · explicit order query uses `--code` · `--order-no` · `--no-broad` |
  | New OD-MS-025 (continued) | Prohibit summary fallback DB updates with multiple active orders · permit only when one candidate is confirmed · wrapper ps1 handles orchestration only · fill-query method controlled inside `connector_order_check.py` · retain OD-MS-016 · OD-MS-021 · OD-MS-023 body |
  | Decision Summary | Total 82 → 83 / provisional 37 → 38 |
  | OD-MS-016 · OD-MS-021 · OD-MS-023 | Reinforced first empirical validation notes without body changes |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | Change |
  | --- | --- |
  | R-AUTO-018 detection / mitigation | [2026-06-18 additional reinforcement] `connector_order_check.py` default execution changed to sequential per-order queries · reduced broad-call frequency · reduced exposure surface for summary fallback · Status remains 🟢 **Mitigated** · both single direct-only query + standalone wrapper Step 13 execution passed |

- 🟢 `.kiro/specs/_common/followups-overview.md` (second 2026-06-18 follow-up note)

  | Follow-up | Value |
  | --- | --- |
  | Configure Git remote push destination | Push commit `75cb804` |
  | Reinforce Step 13 summary output | active_order_count · single_check_success_count · single_check_failed_count · broad_mode_used |
  | View Daily Batch screen linkage | Display Step 13 result per order |
  | Windows PowerShell · AWS CLI SSM emoji StdOut | Document cp949 encoding-error avoidance pattern |

- 🟢 `.kiro/README.md`

  | Item | Value |
  | --- | --- |
  | "Operator local PowerShell wrapper" section | Added **Step 13 safety caution** · sequential active-order query by default · `--broad` legacy · explicit order query uses `--code` · `--order-no` · `--no-broad` · wrapper ps1 handles orchestration only · fill-query method controlled inside `connector_order_check.py` |
  | Main `-StartStep` / `-EndStep` option description | `-FromStep` / `-ToStep` are nonexistent parameters · typo may cause default `-StartStep 1 -EndStep 17` behavior |
  | "Current progress summary" row 03 | Added one-line result from second session on this date |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-18 section | Newly accumulated (Step 13 sequential per-order queries by default — operational safety reinforcement) |

### Security

  | Item | Result |
  | --- | --- |
  | Plaintext recording of secret · KIS · account · token · RDS · account-id · ARN · access key · EIP · image digest · task ARN · job ARN · KIS paper credential | 0 (all `[REDACTED]` or `<account-id>` · `<region>` · `<rds-endpoint>` · `<role-arn>` · `<secret-arn>`) |
  | Quotation of `port-marketconnector/connector_order_check.py` patch body | 0 (aligned with R-DOCS-001 · facts only recorded) |
  | broker / KIS calls | 1 single direct-only query (`004990` · broker_order_no `0000025576`) + 1 standalone wrapper Step 13 execution (both read-oriented) |
  | New BUY · SELL · cancel · modify · `--execute` | 0 |
  | `mark_position_sell_ordered()` · automatic fill · position sync retries | 0 |
  | Session environment | Limited to `aws-paper` · aws-live work 0 |
  | AWS · SSM · EC2 · S3 · IAM · Secrets Manager · SSM Parameter Store · RDS · KIS | All performed directly by operator · Kiro only organized documentation · procedures · validation items |
  | AWS CLI · boto3 executions · AWS resource creations · modifications · deletions | 0 |
  | Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
  | Plaintext quotation of full CloudWatch Logs · SSM response · KIS API response body · PowerShell stdout | 0 |
  | RDS DDL | 0 |
  | RDS DML | Limited to normal Step 13 single direct-only query flow · new `connector.connector_order_event` · `connector.connector_fill` rows aligned with broker response values · one single direct-only + one standalone wrapper Step 13 · 0 incorrectly linked rows |
  | Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · packaging spec area | 0 |

<details><summary>🔵 Operational identifier summary</summary>

  | Item | Value |
  | --- | --- |
  | commit | `75cb804` |
  | S3 bucket · key · file size | `portfolio-paper-migration-yukiever` · `deploy/marketconnector/connector_order_check.py` · `39159 bytes` |
  | EC2 instance id | `i-0fce77927b7397b88` |
  | EC2 backup filename | `connector_order_check.py.bak-20260618-step13-per-order` |
  | SSM commandId | `b344d404-07a4-4bb6-9d63-34151e648bab` |
  | Ticker code | `004990` |
  | broker_order_no | `0000025576` |
  | wrapper options / parameters | `-StartStep` · `-EndStep` |

</details>

## 2026-06-18 (Daily AWS Paper Wrapper 17-step live-operation validation complete)

### Changed

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md` (2026-06-18 §1~§4)

  | Section | Content |
  | --- | --- |
  | (a) Step 1 CONNECTOR_BALANCE | Operator-direct SSM RunCommand 🟢 **Passed** |
  | (b) Step 12 MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE (`-AllowPaperOrderExecute` specified) | First KIS paper API read timeout · connector_order_request ids `38~41` <span style="color:#D1242F">**FAILED**</span> · strategy_execution_order ids `30~33` <span style="color:#D1242F">**FAILED**</span> |
  | (b) Step 12 MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE (`-AllowPaperOrderExecute` specified) (continued) | Network · DNS · TCP · HTTPS normal → judged temporary KIS paper endpoint latency issue |
  | (b) Controlled REQUESTED recovery | Performed directly by operator after pre-checking existence of `connector_order_request` · `connector_api_call_log` · `broker_order_no` |
  | (b) Retry submission of 4 KIS paper BUY orders | connector_order_request ids `42~45` 🟢 **ACCEPTED** · broker_order_no `0000025576` 004990 · `0000025740` 023530 · `0000025744` 003490 · `0000025747` 042660 · strategy_execution_order ids `30~33` 🟢 **SUBMITTED** |
  | (c) Step 13 CONNECTOR_ORDER_CHECK | broad response `output1 empty` + `output2 summary-only` · 4 active candidates · summary fallback guard automatically skipped (first empirical R-AUTO-018 mitigation) → all 4 fills reflected via per-order re-query using `--code` · `--order-no` · `--no-broad` · 4 shares · 8 shares · 6 shares · 11 shares |
  | (d) Step 17 BALANCE_REFRESH | SSM commandId `66ec8831-74b9-469c-8410-6ccb11cb3400` · responseCode 0 · Status Success · `connector.connector_position_snapshot` row_count `36` · max_created_at `2026-06-18 04:33:07.456056+00` |
  | (d) Step 17 BALANCE_REFRESH (continued) | `legacy.holdings` row_count `41` · max_created_at `2026-06-18 04:33:07.420226` · view_app lacked SELECT permission on legacy schema → used `portfolio_admin` as workaround (R-DATA-005 reinforcement · followups retained) |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (2026-06-18 §1~§6)

  | Step | Result |
  | --- | --- |
  | Step 6 DAILY_BUY_SIGNAL | 4 BUY READY · `004990` 롯데지주 · `023530` 롯데쇼핑 · `003490` 대한항공 · `042660` 한화오션 |
  | Step 7 DAILY_POSITION_SIGNAL | HOLD decisions for positions created normally |
  | Step 8 DAILY_BUY_EXECUTION | `execution_plan_id 94` · 4 BUY READY · blocked 0 · skipped 0 · existing holdings (`004990` · `003490`) also BUY candidates because additional buys allowed · 004990 4 shares · 023530 8 shares · 003490 6 shares · 042660 11 shares |
  | Step 9 DAILY_SELL_EXECUTION | Normal skip (no SELL target · exitCode 0) |
  | Step 10 DAILY_AUTO_SELL | Normal skip (no READY SELL · exitCode 0) |
  | Step 11 DAILY_AUTO_BUY | 4 BUY READY → 🟢 **REQUESTED** |
  | Step 14 SYNC_SELL_FILL | Normal skip (no SELL fill targets) |
  | Step 15 SYNC_BUY_FILL | strategy_execution_order ids `30~33` 🟢 **FILLED** based on connector_fill · `result_payload.sync_result` created |
  | Step 16 SYNC_BUY_POSITION | See 4 phases below |
  | · phase 1 collision | Initial additional-buy unique-constraint collision (aligned with new R-DATA-012) |
  | · phase 2 patch | `execution_sync_buy_position.py` — query OPEN by `account_id` · `ticker_code` · use `merge_open_position_state()` if OPEN exists · `buy_info.additional_buys` history · deduplicate by `execution_order_id` · `connector_order_request_id` |
  | · phase 3 rebuild | Docker rebuild (`portfolio-strategy-execution:paper-20260613`) + ECR push (image digest retained by operator · plaintext 0) |
  | · phase 4 rerun | ECS task rerun exitCode 0 · 🟢 **SUCCESS** · position_sync_result created |
  | Step 16 position reflection | 004990 65 → 69 shares (`position_state_id 7` retained · additional_buys 30) · 003490 52 → 58 shares (`position_state_id 8` retained · additional_buys 32) · new `position_state_id 11` for 023530, 8 shares entry_price `194225.0000` |
  | Step 16 position reflection (continued) | New `position_state_id 12` for 042660, 11 shares entry_price `126118.1818` |
  | Docker build caution | Dockerfile `port_strategy_execution/...` COPY → build from `C:\Workspaces` · if PowerShell pipe-based `docker login` returns 400, use `cmd /c` pipe workaround |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Change Log 2026-06-18 | Added |
  | New OD-MS-024 | 🟠 **잠정** · additional-buy policy · when existing OPEN exists, use `merge_open_position_state()` rather than new INSERT · `buy_info.additional_buys` history · idempotency · patch `port_strategy_execution/execution_sync_buy_position.py` · retain alignment with OD-MS-016 · OD-MS-017 |
  | Decision Summary | Total 81 → 82 / provisional 36 → 37 |
  | First empirical validation notes reinforced | OD-MS-016 (Step 11 · 12 · 15 · 16 end-to-end validation) · OD-MS-021 (17-step wrapper safety criteria) · OD-MS-023 (wrapper live execution passed · first Step 12 use) |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | Change |
  | --- | --- |
  | New R-BROKER-004 | Risk of duplicate broker orders from simple rerun after KIS paper API read timeout · mitigation = controlled REQUESTED recovery after pre-check · first empirical validation · Status 🟢 **Mitigated** |
  | New R-DATA-012 | strategy_position_state `unique(account_id, ticker_code, status='OPEN')` collision on additional buy · mitigation = `execution_sync_buy_position.py` merge patch + idempotency · Status 🟢 **Mitigated** |
  | R-AUTO-018 detection / mitigation | [2026-06-18 reinforcement] first empirical automatic skip of summary fallback in wrapper Step 13 broad query · allow fallback only for single-item `--code` · `--order-no` · `--no-broad` query |
  | R-AUTO-009 · R-AUTO-010 · R-AUTO-011 · R-AUTO-019 | First Step 12 `-AllowPaperOrderExecute` use · initial timeout → controlled recovery → 4 KIS paper BUY end-to-end passed · Status remains 🟢 **Mitigated** · additional weekday · safe-data validation before live cutover remains follow-up |
  | R-DATA-005 detection / mitigation | [2026-06-18 reinforcement] view_app lacked `legacy.holdings` SELECT permission · `portfolio_admin` workaround used to confirm Step 17 result · formal GRANT follow-up (02 spec) |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-18 follow-up note)

  | Follow-up | Value |
  | --- | --- |
  | Step 13 per-order fill query | Reinforce wrapper automation |
  | KIS API timeout retry policy | Document |
  | Step 16 additional-buy merge patch | Formal commit + 07 spec CI/CD linkage |
  | view_app `legacy.holdings` SELECT permission | 02 spec db-roles-and-grants follow-up |
  | wrapper run-summary output | Reinforce |
  | Before View Daily Batch screen linkage | Improve final operational summary |
  | bundled wrapper | Decide whether to create |
  | Other | Scheduled EventBridge trigger · SFN migration · aws-live cutover |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Decision values (compute primary / secondary / not recommended) | No change |
  | First empirical note reinforcement | MC (EC2) Steps 12·13·17 · Strategy Execution (ECS Fargate + command override) Steps 15·16 · Strategy Decision (ECS Fargate) Steps 6·7 · Strategy Research (AWS Batch) Steps 4·5 · Interest Crawler (hybrid) Step 2 · Preprocessor (ECS Fargate) Step 3 |
  | State | Wrapper-based 17-step E2E operational validation complete · Daily live run · 4 actual paper orders · fills · sync · balance refresh aligned |

- 🟢 `.kiro/README.md`

  | Item | Value |
  | --- | --- |
  | "Current progress summary" | Reference date 2026-06-18 · wrapper 1~17 live run complete · first Step 12 use · identified/recovered 3 operational exceptions |
  | "Operator local PowerShell wrapper" | Reinforced first empirical use note for `-AllowPaperOrderExecute` on this date under "Step 12 safety caution" |
  | bundled-wrapper not-created policy | Retained |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-18 section | Newly accumulated (Daily AWS Paper Wrapper 17-step live-operation validation complete) |

### Security

  | Item | Result |
  | --- | --- |
  | Plaintext sensitive-data recording | secret · KIS · account · token · RDS · account-id · IAM Role ARN · secret ARN · access key · instance-id · EIP · image digest · task ARN · job ARN |
  | Plaintext sensitive-data recording (continued) | 0 (all `[REDACTED]` or `<account-id>` · `<region>` · `<rds-endpoint>` · `<role-arn>` · `<secret-arn>` · `<instance-id>` · `<eip>` · `<image-digest>` · `<task-arn>` · `<job-arn>`) |
  | ECR push image digest | Retained by operator · prohibited plaintext in spec artifacts (aligned with R-DOCS-001) |
  | broker / KIS calls | 4 KIS paper BUY (Step 12 live execution) + read-oriented balance · order check |
  | SELL · cancel · modify · additional `--execute` · `mark_position_sell_ordered()` | 0 |
  | Automatic fill · position sync retries | 0 (rerun after direct operator patch) |
  | Session environment | Limited to `aws-paper` · aws-live work 0 |
  | KIS paper `broker_order_no` (`0000025576` · `0000025740` · `0000025744` · `0000025747`) | Operational broker-response identifiers · not live-account identifiers |
  | AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · SSM Parameter Store · RDS · GRANT · KIS · S3 · CloudWatch · Docker · ECR | All performed directly by operator · Kiro only organized documentation · procedures · validation |
  | AWS CLI · boto3 · AWS resource creations · modifications · deletions | 0 |
  | Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
  | Plaintext quotation of full CloudWatch Logs · SSM responses · KIS API · Docker build · push · PowerShell stdout | 0 |
  | RDS DDL | 0 |
  | RDS DML (normal 17-step flow) | See rows below |
  | · `decision.strategy_daily_signal` | Recorded |
  | · `decision.strategy_daily_run` | Recorded |
  | · `decision.strategy_daily_position_decision` | HOLD |
  | · `execution.strategy_execution_plan` | insert (`id 94`) |
  | · `execution.strategy_execution_order` | insert · update (4 BUY READY → REQUESTED → first FAILED → second SUBMITTED → FILLED) |
  | · `execution.strategy_position_state` | insert · merge (2 additional-buy merges + 2 new INSERTs) |
  | · `connector.connector_order_request` | insert (ids `38~41` FAILED + `42~45` ACCEPTED) |
  | · `connector.connector_order_event` | Recorded |
  | · `connector.connector_fill` | insert (4 fills) |
  | · `connector.connector_balance_snapshot` | insert |
  | · `connector.connector_position_snapshot` | insert (row_count 36) |
  | · `legacy.holdings` | insert (row_count 41) |
  | Quotation of `port_strategy_execution/execution_sync_buy_position.py` changes | 0 (facts only recorded in 04 spec operation-notes) |
  | Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging spec area | 0 |

<details><summary>🔵 Operational identifier summary</summary>

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
  | Ticker codes | `004990` · `023530` · `003490` · `042660` · `088350` · `282330` |
  | data_date | `2026-06-17` |
  | signal_date · run_date | `2026-06-18` |

</details>
## 2026-06-17 (Daily AWS PowerShell wrapper implementation)

### Added

- 🟢 `.kiro/scripts/` (new · operator-local Windows PowerShell wrapper structure)

  | Item | Value |
  | --- | --- |
  | Location | Operator-local tool · outside Kiro spec artifacts · separate from 8 MS source trees |
  | main wrapper | `run-daily-aws-paper.ps1` · parameters `RunDate` · `Region` · `Environment` · `StartStep` · `EndStep` · `DryRun` · `AllowPaperOrderExecute` · central Step 12 PAPER_ORDER_GATE block · summary generation |
  | config | `daily-aws-paper.config.ps1` · manages region · cluster · instance id · subnet · SG · task definition · job definition · log group · output path |
  | functions | `daily-aws-paper.functions.ps1` · Step registry · common SSM (`AWS-RunShellScript` Linux + `AWS-RunPowerShellScript` Windows) · common ECS RunTask (UTF-8 no BOM JSON `--overrides file://...`) · common AWS Batch SubmitJob |
  | functions (continued) | Collect CloudWatch logs · store SSM stdout · stderr · record summary · determine success/failure/blocker · PowerShell UTF-8 correction |
  | step files | `steps/step-01-connector-balance.ps1` ~ `steps/step-17-balance-refresh.ps1` (17 files) |
  | bundled wrapper | `run-daily-aws-paper-bundled.ps1` not created on this date · optional follow-up |

- 🟢 **New decisions · risks**

| Item | Value |
| --- | --- |
| New OD-MS-023 | **Status**: 🟠 **잠정**<br>**Note**: Daily AWS wrapper policy · Windows PowerShell baseline · split-file structure · bundled wrapper only if needed · Step 12 blocked without `-AllowPaperOrderExecute` · inspectable CLI baseline before full automation · environment input only `aws-paper` · wrapper itself is not an execution target inside EC2 · ECS · Batch |
| New R-AUTO-019 | **Status**: 🟢 **Mitigated** · **Note**: Risk of unintended live KIS paper order submission if wrapper Step 12 uses `-AllowPaperOrderExecute` unintentionally · mitigation = central PAPER_ORDER_GATE + Step 12 internal double gate + explicit option · default OFF · recommend operator note before use |
| New R-AUTO-019 (continued) | detection = summary `PaperOrder : True` · frequency of Step 12 option use · `connector_order_request` insert audit · rollback = immediately stop Step 12 · stop SSM RunCommand · broker cancel |

### Changed

- 🟢 `.kiro/README.md`

  | Item | Value |
  | --- | --- |
  | "Operator local PowerShell wrapper" section | Newly added · default path `.kiro/scripts/` · main options `-DryRun` · `-StartStep` · `-EndStep` · `-AllowPaperOrderExecute` · Step 12 safety caution · bundled wrapper not created · environment input only `aws-paper` |
  | "Current progress summary" 03 / progress row | Reinforced first milestone note establishing wrapper baseline |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Third Change Log 2026-06-17 entry | Added |
  | New OD-MS-023 | Classified under Compute Decisions / service placement as follow-up Daily Batch orchestration decision · operator-tool stage 1 of OD-MS-009 · OD-MS-021 |
  | OD-MS-009 · OD-MS-021 | Reinforced first empirical notes without body changes · wrapper enforces same 17-step mapping · same safety criteria (prohibit BUY · SELL execution · block `--execute` family · prohibit aws-live · Research → Decision · Connector Balance at 1 · Balance Refresh at 17) at code level |
  | OD-SAFE-002 · OD-SAFE-003 · OD-SAFE-004 | Reinforced first empirical notes · Step 12 PAPER_ORDER_GATE block · Step 10 · Step 11 `--execute` perform internal strategy-execution state updates (not direct broker / KIS submission) · no new automatic retry introduced |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | Change |
  | --- | --- |
  | New R-AUTO-019 | Step 12 wrapper-gate bypass risk · Status 🟢 **Mitigated** |
  | R-AUTO-016 detection / mitigation | [2026-06-17 wrapper reinforcement] wrapper Step 2 pre-checks Windows KRX crawler-worker EC2 state `running` · first empirical automatic skip of KRX GUI Scheduled Task trigger if not `running` |
  | R-AUTO-002 mitigation | [2026-06-17 wrapper reinforcement] wrapper environment input is only `aws-paper` · aws-live branch does not exist at code level |
  | R-DOCS-001 detection | [2026-06-17 wrapper reinforcement] checked wrapper summary · overrides JSON · SSM stdout · stderr files for plaintext secret output: 0 · use `/tmp/inject-env.sh` v5 env injection only |

- 🟢 `.kiro/specs/_common/followups-overview.md` (third 2026-06-17 follow-up note)

  | Follow-up | Value |
  | --- | --- |
  | bundled wrapper | Decide whether to create |
  | Step 1~7 safe subset | Validate actual execution |
  | Step 8~11 execution-side | Validate state creation/update range separately |
  | Step 12 live order submission | Only by explicitly specifying `-AllowPaperOrderExecute` after operator confirmation |
  | Step 13~17 | Link after confirming order / fill results |
  | Step 2 skip when Windows KRX worker stopped | Validate behavior |
  | Other | Review View backend orchestration or SFN migration · scheduled EventBridge trigger · aws-live cutover (10 spec) |

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md` (2026-06-17 §1~§6)

  | Section | Content |
  | --- | --- |
  | (a) wrapper artifact inventory | main · config · functions · 17 step files · bundled not created |

  | (b) Standalone Step 1 SSM validation | MC EC2 · `connector_balance.py` · 🟢 **Success** · ResponseCode 0 · `connector_balance_snapshot` saved · 0 holdings |
  | (c) Step 12 PAPER_ORDER_GATE safety block | `-StartStep 12 -EndStep 12` · default `DryRun: False` · `PaperOrder: False` · central wrapper gate block · internal double gate · 0 SSM commands submitted without `-AllowPaperOrderExecute` · 0 live KIS order submissions |
  | (d) Step 13 / Step 17 wrapper flow | Step 13 = query order status · update DB status · Step 17 = balance · position snapshot refresh · neither submits new broker · KIS orders |
  | (e) Full Step 1~17 DryRun | 17 FOUND checks passed · 20 parser validations OK · safety grep for risky keywords passed · Step 10 / 11 `--execute` perform internal execution-state updates · only Step 12 `--execute` classified as capable of submitting KIS paper orders |
  | (f) Safety / security checks | 0 plaintext records of secret · KIS · account · token · RDS · account-id · ARN · access key · EIP · image digest · 0 live broker · KIS · `--execute` order submissions · 0 aws-live operations · 0 full wrapper-based Step 1~17 live re-runs · bundled wrapper not created |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-17 section | Newly accumulated (Daily AWS PowerShell wrapper implementation) |

### Security

  | Item | Result |
  | --- | --- |
  | Plaintext sensitive information recorded | secret · KIS · account · token · RDS · account-id · IAM Role ARN · secret ARN · access key · instance-id · EIP · image digest · task ARN · job ARN plaintext records |
  | Plaintext sensitive information recorded (continued) | 0 (`[REDACTED]` or `<account-id>` · `<region>` · `<rds-endpoint>` · `<role-arn>` · `<secret-arn>` · `<instance-id>` · `<eip>` · `<image-digest>` · `<task-arn>` · `<job-arn>`) |
  | Plaintext secrets in wrapper summary · overrides JSON · SSM stdout · stderr files | 0 (`/tmp/inject-env.sh` v5 env injection only · 0 plaintext values printed · only key presence / length checked) |
  | Automatic retries of live broker · KIS · new BUY · SELL · cancel · modify · `--execute` · `mark_position_sell_ordered()` · fill · position sync | 0 |
  | Session environment | `aws-paper` only · 0 aws-live work · wrapper itself accepts only `aws-paper` as environment input |
  | Step 12 PAPER_ORDER_GATE | blocked unless `-AllowPaperOrderExecute` is explicitly provided · option used 0 times on this date · 0 live KIS paper order submissions |
  | AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · SSM Parameter Store · RDS · KIS | all performed directly by the operator · Kiro only organized documentation · procedures · validation |
  | AWS CLI · boto3 · AWS resource creation · modification · deletion | 0 |
  | Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
  | Full plaintext quotation of CloudWatch Logs · SSM responses · KIS API · PowerShell stdout | 0 |
  | wrapper summary · step stdout · stderr storage location | run folders under `C:\Temp\portfolio-daily-aws-paper` · operator-local PC tool · outside Kiro spec artifacts |
  | RDS DDL | 0 |
  | RDS DML | 1 `connector.connector_balance_snapshot` insert from the standalone Step 1 SSM first validation · 0 `connector.connector_position_snapshot` holdings · `legacy.holdings` write limited to consistency with the 03 spec Step 1 flow · all other 17-step DryRun paths submitted 0 ECS RunTask / Batch SubmitJob / SSM commands, therefore 0 writes |
  | `.kiro/scripts/` location | operator-local Windows PC tool · outside Kiro spec artifacts · separated from the 8 MS source · packaging · docs · worklog · README · AGENTS.md · CHANGELOG areas |
  | Changes to 04 · 06 · 08 · 09 spec operation-notes caused by wrapper work on this date | 0 · wrapper is a cross-cutting operator tool, so accumulated only in 03 spec operation-notes · other specs referenced only through followups-overview notes |
## 2026-06-17 (Daily AWS 17-step E2E complete)

### Changed

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md` (2026-06-17 §1~§5)

  | Section | Content |
  | --- | --- |
  | (a) Step 1 CONNECTOR_BALANCE linkage | Factually linked the first session (v5 env injection · `as_of_date 2026-06-17`) as the starting state of the 17-step flow |
  | (b) Step 12 MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE final success | 4 KIS paper BUY orders · `execution_order` id `26~29` 🟢 **SUBMITTED** · `connector_order_request` id `34~37` · `broker_order_no 0000035906` · `0000035912` · `0000035918` · `0000035932` |
  | (b) Initial failure · correction reason | `connector_strategy_order_execute.py` not deployed to MC EC2 · system Python lacked `psycopg` → venv Python required · missing UPDATE permission on `execution` table → operator directly corrected GRANT · formally deployed after `source_run_id` fallback patch |
  | (c) Step 13 CONNECTOR_ORDER_CHECK | Initial response `output1 empty` + `output2 aggregate summary` caused multi-active fallback mapping contamination → deleted incorrectly created `connector_order_event` / `connector_fill` rows + restored `connector_order_request` status + patched summary fallback guard (allowed only when exactly one candidate exists |
  | (c) Step 13 CONNECTOR_ORDER_CHECK (continued) | prohibit event / fill / status changes when multiple candidates exist) + fill synchronization succeeded through per-`broker_order_no` single-order queries (`connector_order_request 34~37` FILLED · `connector_fill 26~29` created) |
  | (d) Step 17 BALANCE_REFRESH | Initial failure = missing `marketconnector_app` legacy schema USAGE · `legacy.holdings` DML · sequence · search_path → corrected search_path + USAGE · DML · sequence GRANT + default privileges → re-run 🟢 **Success** · ResponseCode 0 |
  | (d) Step 17 BALANCE_REFRESH (continued) | `connector_position_snapshot` 4 symbols · `position_snapshot_id 120~123` · quantity `52 · 65 · 244 · 17` · avg_buy_price `28980.77 · 27043.08 · 5744.41 · 120182.35` |
  | (e) Safety / security checks | 0 plaintext records of secret · KIS · account · token · RDS · account-id · ARN · access key · EIP · image digest · 0 SELL · cancel · modify calls · 0 aws-live work · 0 quoted patch bodies |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (2026-06-17 §1~§6)

  | Step | Result |
  | --- | --- |
  | Step 6 DAILY_BUY_SIGNAL | ECS RunTask exitCode 0 · run_date `2026-06-17` · data_date `2026-06-16` · 4 BUY READY rows in `decision.strategy_daily_signal` · 4 candidates `282330` · `004990` · `003490` · `088350` |
  | Step 7 DAILY_POSITION_SIGNAL | positions 0 · decision_count 0 normal skip · 0 new `decision.strategy_daily_position_decision` rows |
  | Step 8 DAILY_BUY_EXECUTION | Initial blocker = missing `execution_app` `interest` schema USAGE · table SELECT · sequence · default privileges → operator corrected GRANT → re-run `execution_plan_id 92` · 4 BUY READY rows · all 4 `connector_order_request_id` values NULL · 0 broker / KIS calls |
  | Step 9 DAILY_SELL_EXECUTION | Normal skip · sell_decisions 0 · orders_to_upsert 0 · 0 broker calls |
  | Step 10 DAILY_AUTO_SELL | Normal skip · READY SELL 0 · 0 broker calls |
  | Step 11 DAILY_AUTO_BUY | 4 BUY rows `READY -> REQUESTED` · `execution_plan_id 92` · `total_qty 378` · `total_target_amount 6908189.40` · all `connector_order_request_id` values NULL · 0 broker / KIS calls · first empirical validation of OD-MS-016 responsibility separation |
  | Step 14 SYNC_SELL_FILL | Normal skip · 0 SELL fills |
  | Step 15 SYNC_BUY_FILL | BUY fill sync based on `connector_fill 26~29` · `execution_order 26~29` 🟢 **FILLED** |
  | Step 16 SYNC_BUY_POSITION | 4 `strategy_position_state` rows OPEN · `position_state_id 6~9` |
  | Safety / security | 0 broker / KIS calls (order submission is 03 spec Step 12) · `--execute` limited to Step 11 / 03 spec Step 12 · 0 aws-live work · first empirical responsibility boundary: Strategy Execution = candidate / state transition, MC = live KIS order submission · operator-applied `execution_app` interest GRANT recorded as fact in 02 · 06 specs |

- 🟢 `.kiro/specs/09-strategy-research-batch/operation-notes.md` (2026-06-17 §1~§3)

  | Step | Result |
  | --- | --- |
  | Step 4 BACKTEST_RESEARCH | AWS Batch 🟢 **SUCCEEDED** · latest result date `2026-06-16` · Sharpe Ratio `2.68` · latest `research.strategy_backtest_daily` · `research.strategy_backtest_daily_position` date `2026-06-16` |
  | Step 5 BACKTEST_REPORT | AWS Batch 🟢 **SUCCEEDED** · 4 S3 report objects created · upload prefix aligned · consistent with OD-MS-019 |
  | Safety / security | RDS DDL 0 · DML limited to normal backtest run flow · Job Role S3 PutObject Resource scoped · public read 0 · 0 plaintext image digest · job ARN · 0 SubmitJob for heavy classification (`run_extended_analysis` · `block_watch_*` · `block_exception_buy_*`) (consistent with R-AUTO-015) |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` (2026-06-17 §1~§3)

  | Step | Result |
  | --- | --- |
  | Step 2 INTEREST_CRAWLER | non-GUI ECS Fargate Task `portfolio-paper-interest-crawler:7` succeeded · KRX Windows EC2 worker Scheduled Task succeeded · `interest_program_raw` · `interest_shortsell_raw` loaded for 2026-06-16 · crawler worker stop request completed |
  | Step 2 INTEREST_CRAWLER (continued) | KRX GUI = Windows interactive desktop session · first empirical validation of hybrid structure: non-GUI + KRX GUI |
  | Step 3 PREPROCESSOR | ECS RunTask exitCode 0 · `PREPROCESSOR PIPELINE END` · latest `pre_total_market_daily_feature` · `pre_total_stock_daily_feature` date `2026-06-16` |
  | Initial failure issue | Missing `execution_app` interest schema · table SELECT permission (affected Step 8 · preprocessor itself normal · fact recorded in 02 · 06 specs) · aws-live 0 · RDS DDL 0 |

- 🟢 `.kiro/specs/02-aws-network-and-rds/operation-notes.md` (2026-06-17 §1~§3)

  | Section | Content |
  | --- | --- |
  | (a) `execution_app` `interest` schema | Corrected USAGE + table SELECT + sequence + default privileges (Step 8 initial failure → passed after GRANT re-run · consistent with R-DATA-005 [2026-06-17 reinforcement]) |
  | (b) `marketconnector_app` `legacy` | Corrected schema USAGE + `legacy.holdings` DML + sequence + database search_path (Step 17 initial failure → passed after search_path `connector, execution, legacy, reference, public` + USAGE · DML · sequence GRANT + default privileges correction |
  | (b) `marketconnector_app` `legacy` (continued) | consistent with R-DATA-005 · R-DATA-011) |
  | (c) Candidate validation SQL | check `current_setting('search_path')` per app role · correct future default privileges · validate legacy path dependencies that use bare table names |
  | Formal 02 spec db-roles-and-grants update | separated into follow-up task · 0 plaintext password · endpoint · account-id · ARN · account number records on this date |

- 🟢 `.kiro/specs/06-secrets-and-iam/operation-notes.md` (2026-06-17 §1~§3)

  | Section | Content |
  | --- | --- |
  | (a) Secrets Manager / SSM Parameter read through MC EC2 Instance Role | first successful empirical validation · JSON SecretString internal-key extraction policy (`kis-app-key` · `kis-app-secret` · `paper-account` · `kis-paper-base-url`, etc.) |
  | (b) Validation method | 0 plaintext values printed · focused on key presence · length · alias presence |
  | (c) Link to 02 spec DB roles / grants | correction of `execution_app` interest SELECT · correction of `marketconnector_app` legacy · `legacy.holdings` permissions · default privileges · sequence validation candidates · 0 plaintext values recorded · consistent with first empirical OD-SEC-006 validation |

- 🟢 4 documents under 03-marketconnector-ec2

  | File | Change |
  | --- | --- |
  | `runbook.md` §4 | Reinforced candidate validation SQL (Step 12 consistency between `connector_order_request` `SUBMITTED` + `execution_order` `SUBMITTED` · Step 13 summary fallback only when exactly one active candidate exists · Step 17 `connector_position_snapshot` |
  | `runbook.md` §4 (continued) | distinguish freshness of `connector_balance_snapshot` using `as_of_ts` / `max(created_at)`) · note in §2 env injection that venv Python is required (system Python lacks `psycopg`) · no decision-value change |
  | `validation-checklist.md` §4/§5/§7 | Added 2026-06-17 17-step E2E row · Step 12 KIS paper BUY 4 orders <span style="color:#1A7F37">**[O]**</span> · first empirical validation of Step 13 summary fallback guard <span style="color:#1A7F37">**[O]**</span> |
  | `validation-checklist.md` §4/§5/§7 (continued) | Step 17 passed after legacy.holdings permission / search_path correction and re-run <span style="color:#1A7F37">**[O]**</span> · 0 SELL · cancel · modify · aws-live <span style="color:#1A7F37">**[O]**</span> |
  | `tasks.md` | Reinforced 17-step E2E facts in tasks 5 · 6 · 9 · 12 · added §8 tasks 29 · 30 · 31 (`inquire-daily-ccld` summary fallback test · `connector_strategy_order_execute.py` `source_daily_signal_id` null correction · `connector_balance_snapshot` freshness SQL organization) · no decision-value change |
  | `design.md` §8.3 | first empirical validation of Step 12 / 13 responsibility boundary · `inquire-daily-ccld` `output1` / `output2` handling policy · summary fallback guard policy · no decision-value change |

- 🟢 4 documents under 06-secrets-and-iam · 08-interest · 02-aws

  | File | Change |
  | --- | --- |
  | `06/runbook.md` §4/§5 | `execution_app` interest SELECT · `marketconnector_app` legacy.holdings permission · default-privileges follow-up · formal 02 spec update follow-up · no decision-value change |
  | `06/validation-checklist.md` §3/§4 | 2026-06-17 first empirical row (MC EC2 Instance Role + Secrets Manager + SSM Parameter Store + internal-key extraction from JSON SecretString) 🟢 **[O]** |
  | `08/tasks.md` | Step 2 INTEREST_CRAWLER 17-step E2E consistency · crawler worker stop-request result · Step 3 PREPROCESSOR result · no decision-value change |
  | `02/db-roles-and-grants.md` §4/§5 | follow-up note · formal matrix follow-up for `execution_app` interest USAGE · SELECT · sequence · default privileges · formal matrix follow-up for `marketconnector_app` legacy schema · `legacy.holdings` DML · sequence · search_path · consistent with R-DATA-005 · R-DATA-011 · no decision-value change |
  | `02/validation-checklist.md` | 2026-06-17 first empirical row · `execution_app` interest SELECT passed 🟢 **[O]** · `marketconnector_app` legacy · `legacy.holdings` permission / search_path passed 🟢 **[O]** |

- 🟢 `.kiro/README.md` "Current progress summary" update

  | Row | Content |
  | --- | --- |
  | 03 | MC EC2 KIS paper BUY 4 orders (execution_order id `26~29` SUBMITTED · connector_order_request id `34~37` · first empirical summary fallback guard · BALANCE_REFRESH passed after legacy permission correction and re-run) |
  | 04 | First Strategy Decision / Execution AWS E2E paper validation (execution_plan_id `92` · READY → REQUESTED → SUBMITTED → FILLED end-to-end · 4 `strategy_position_state` rows OPEN) |
  | 06 | corrected `execution_app` interest permission + corrected `marketconnector_app` legacy permission · facts recorded in both 02 · 06 specs |
  | 08 | first Interest Crawler hybrid + Preprocessor ECS restart validation (2026-06-16 load · `pre_total_*` 2026-06-16 · crawler worker stop completed) |
  | 09 | AWS Batch BACKTEST_RESEARCH + BACKTEST_REPORT (Sharpe Ratio `2.68` · 4 S3 objects) |
  | 02 | corrected `execution_app` interest + `marketconnector_app` legacy schema · `legacy.holdings` permission / search_path |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Second Change Log 2026-06-17 entry | Added |
  | New OD | 0 |
  | OD-MS-016 · OD-MS-021 · OD-DB-008 · OD-MS-008 · OD-MS-019 | Reinforced first empirical notes without body changes |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | Change |
  | --- | --- |
  | New R-AUTO-018 | Risk of incorrect mapping in `inquire-daily-ccld` summary fallback · mitigation = guard patch (allow only when exactly one candidate exists) + per-`broker_order_no` single-order query · detection = mapping consistency · rollback = delete incorrect rows + re-run · Status 🟢 **Mitigated** |
  | New R-DATA-011 | BALANCE_REFRESH failure risk due to missing `marketconnector_app` legacy schema USAGE · `legacy.holdings` DML · sequence · search_path |
  | New R-DATA-011 (continued) | mitigation = search_path `connector, execution, legacy, reference, public` + GRANT + default privileges · detection = `relation "holdings" does not exist` · Status 🟢 **Mitigated** |
  | R-DOCS-001 detection / mitigation | Re-validated [2026-06-17 reinforcement (17-step E2E)] |
  | R-DATA-005 detection / mitigation | [2026-06-17 reinforcement] missing-permission cases for `execution_app` interest / `marketconnector_app` legacy.holdings + GRANT resolution · formal 02 spec update follow-up |
  | R-AUTO-009 · R-AUTO-010 · R-AUTO-011 | Status 🟢 **Mitigated** · end-to-end passed limited to 4 KIS paper BUY orders · additional weekday · safe-data validation required before live cutover |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-17 17-step E2E follow-up note)

  | Follow-up | Content |
  | --- | --- |
  | Completed scope | 17-step |
  | Follow-ups retained | `connector_order_check.py` summary fallback test · `connector_strategy_order_execute.py` `source_daily_signal_id` null correction · formal documentation of `legacy.holdings` permission / search_path · organize `connector_balance_snapshot` freshness `as_of_ts` SQL |
  | Follow-ups retained (continued) | avoid emoji in Windows cp949 console stdout · AWS paper automation orchestrator candidates · View AWS execution mapping (05) · Step Functions + EventBridge Scheduler (04) · aws-live cutover (10) · CI/CD OIDC (07) |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | Item | Value |
  | --- | --- |
  | Target | 4.1 · 4.3 · 4.4 · 4.6 · 4.7 · 4.8 · Section 5 · 6.1 recommendation |
  | Decision value (first choice) | No change |
  | First empirical note reinforcement | validated by MC EC2 · crawler hybrid (non-GUI ECS Fargate + KRX Windows EC2) · preprocessor ECS Fargate · research AWS Batch · decision · execution ECS Fargate · first full Daily AWS 17-step E2E paper pass |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-17 section | Newly accumulated (Daily AWS 17-step E2E complete) |
  | First session on same date (MC read-only dry-run revalidation) | No body change |

### Security

  | Item | Result |
  | --- | --- |
  | Plaintext sensitive information recorded | secret · KIS · account · token · RDS · account-id · IAM Role ARN · secret ARN · access key · instance-id · EIP · image digest · task ARN · job ARN plaintext records |
  | Plaintext sensitive information recorded (continued) | 0 (all `[REDACTED]` or `<account-id>` · `<region>` · `<rds-endpoint>` · `<role-arn>` · `<secret-arn>` · `<instance-id>` · `<eip>` · `<image-digest>` · `<task-arn>` · `<job-arn>`) |
  | broker / KIS calls | 4 KIS paper BUY orders (`MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` live execution) + read-only `inquire-balance` / `inquire-daily-ccld` |
  | SELL · cancel · modify · additional `--execute` · `mark_position_sell_ordered()` | 0 |
  | Session environment | `aws-paper` only · 0 aws-live work |
  | KIS paper `broker_order_no` | broker response values · not live-account data |
  | AWS · SSM · EC2 · ECS · AWS Batch · IAM · Secrets Manager · SSM Parameter Store · RDS · GRANT · KIS · S3 · CloudWatch | all performed directly by the operator · Kiro only organized documentation · procedures · validation |
  | AWS CLI · boto3 · AWS resource creation · modification · deletion | 0 |
  | Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
  | Full plaintext quotation of CloudWatch Logs · SSM responses · KIS API · Docker build · PowerShell stdout | 0 |
  | RDS DDL | 0 |
  | RDS DML (normal 17-step flow) | see listed rows below |
  | · `connector.connector_order_request` | insert · update · upsert |
  | · `connector.connector_order_event` | insert · update · upsert |
  | · `connector.connector_fill` | insert · update · upsert |
  | · `execution.strategy_execution_order` | insert · update · upsert |
  | · `execution.strategy_execution_plan` | insert · update · upsert |
  | · `execution.strategy_position_state` | insert · update · upsert |
  | · `decision.strategy_daily_signal` | insert · update · upsert |
  | · `decision.strategy_daily_run` | insert · update · upsert |
  | · `decision.strategy_daily_position_decision` | insert · update · upsert |
  | · `connector.connector_balance_snapshot` | insert · update · upsert |
  | · `connector.connector_position_snapshot` | insert · update · upsert |
  | · `interest.*_raw` | insert · update · upsert |
  | · `pre_total_*_feature` | insert · update · upsert |
  | · `research.strategy_backtest_*` | insert · update · upsert |
  | · 4 rows affected by `--execute` (KIS paper BUY) | all transitioned consistently SUBMITTED → FILLED → position OPEN |
  | Quoted operator patch body (`connector_strategy_order_execute.py` · `connector_order_check.py`) | 0 (facts recorded only in 03 spec operation-notes) |
  | Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging spec areas | 0 |

<details><summary>🔵 Operational identifier summary</summary>

  | Item | Value |
  | --- | --- |
  | execution_plan_id | `92` |
  | execution_order id | `26~29` |
  | connector_order_request id | `34~37` |
  | broker_order_no | `0000035906` · `0000035912` · `0000035918` · `0000035932` |
  | position_state_id | `6~9` |
  | connector_position_snapshot id | `120~123` |
  | Ticker codes | `282330` · `004990` · `003490` · `088350` |
  | total_qty · total_target_amount | `378` · `6908189.40` |
  | Sharpe Ratio | `2.68` |
  | data_date | `2026-06-16` |
  | signal_date · run_date | `2026-06-17` |

</details>
## 2026-06-17 (MarketConnector read-only dry-run revalidation)

### Changed

- 🟢 `.kiro/specs/03-marketconnector-ec2/design.md`

  | Item | Value |
  | --- | --- |
  | Reinforcement target | §8.2 · §8.2.1 · §8.2.2 · §8.2.3 · §8.3 |
  | Policy clarification | Simultaneously export `APP_*` compatibility keys + `KIS_*` aliases (`KIS_APP_KEY` · `KIS_APP_SECRET` · `KIS_PAPER_ACNT` · `KIS_ACNT_PRDT_CD` · `KIS_BASE_URL`) |
  | Extract internal keys from JSON SecretString | SecretString for `/portfolio/paper/marketconnector/kis-app-key` · `kis-app-secret` · `paper-account` is JSON · extract internal keys `APP_KEY` · `APP_SECRET` · `PAPER_ACNT` · `ACNT_PRDT_CD`, then export |
  | v5 correction case | Mapping error from exporting the entire JSON dict → resolved by parsing JSON, extracting only internal key values, then exporting compatibility keys + aliases together |
  | Decision values (4 env-injection categories · EC2 Instance Role as sole credentials · force `CONNECTOR_DEBUG=false` · 0 external exposure on SG inbound 5000) | No change |

- 🟢 `.kiro/specs/03-marketconnector-ec2/runbook.md`

  | Item | Value |
  | --- | --- |
  | §2 env-injection procedure reinforcement | Added KIS_* env keys · JSON parsing procedure · extract internal values from `aws secretsmanager get-secret-value` result using `python -c` or `jq -r .APP_KEY` · prohibit exporting entire JSON dict · verify only value length or key presence |
  | New §4.1 (`connector_balance.py` validation SQL) | consistency among latest `connector_api_call_log` BALANCE `inquire-balance` row + latest `connector_balance_snapshot` row + 0 holdings in `connector_position_snapshot` |
  | New §4.2 (`connector_order_check.py` validation SQL) | `connector_api_call_log` ORDER `inquire-daily-ccld` + row-count inventory for `connector_order_request` · `connector_order_event` · `connector_fill` + treat 0 new rows as normal |

- 🟢 `.kiro/specs/03-marketconnector-ec2/validation-checklist.md`

  | Table | Added 2026-06-17 row |
  | --- | --- |
  | §4 Connector / Flask read-only smoke test | `CONNECTOR_BALANCE` 🟢 **[O]** · `CONNECTOR_ORDER_CHECK` 🟢 **[O]** (pre-validation of MC read-only paths) |
  | §5 Secrets Manager / SSM env injection | v5 pattern (extract internal JSON SecretString keys + export `APP_*` compatibility keys + `KIS_*` aliases together) 🟢 **[O]** · 0 plaintext secret · account · token records 🟢 **[O]** |
  | §7 0 new order · buy · sell · cancel · modify calls | 2026-06-17 revalidation 🟢 **[O]** |

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md` (2026-06-17 §1~§3)

  | Section | Content |
  | --- | --- |
  | (a) CONNECTOR_BALANCE | Initial failure cause = mapping error from exporting entire JSON SecretString → corrected with v5 pattern → final success · latest `connector_balance_snapshot` row (`as_of_date 2026-06-17` · `as_of_ts 2026-06-17 00:46:17 UTC` · `created_at 2026-06-17 00:46:17 UTC` |
  | (a) CONNECTOR_BALANCE (continued) | `source_api inquire-balance` · `source_version connector-balance-1.0.0` · 0 holdings normal) |
  | (b) CONNECTOR_ORDER_CHECK | Reused v5 (pre-validation of MC read-only paths) · `inquire-daily-ccld` `response_status=200` · `response_code=0` · `is_success=true` · `called_at 2026-06-17 00:51:03 UTC` · row count `connector_order_request 33` · `connector_order_event 18` |
  | (b) CONNECTOR_ORDER_CHECK (continued) | `connector_fill 13` · 0 new rows normal · temporary PowerShell variable loss was a minor operator-side issue |
  | (c) Safety / security checks | 0 new orders · 0 `--execute` · 0 aws-live · RDS DDL 0 · only 1 `connector_balance_snapshot` insert · 0 plaintext secret · account · token · RDS · account-id · ARN · access key · instance-id · EIP · image digest records |
  | (c) Safety / security checks (continued) | 0 changes to 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source |

- 🟢 `.kiro/specs/03-marketconnector-ec2/tasks.md`

  | Item | Value |
  | --- | --- |
  | task 5 (organize temporary export-script pattern) | 🟢 **Complete** |
  | task 6 (finalize environment-variable mapping table) | 🟢 **Complete** |
  | task 9 (organize read-only execution procedure for `connector_balance.py` / `connector_order_check.py`) | 🟢 **Complete** |
  | task 12 (document zero-new-order-call policy) | 🟢 **Complete** |
  | New §8 tasks 26 / 27 / 28 | apply v5 env-mapping pattern when transitioning systemd · startup script to normal operating mode · decide whether to promote `/tmp/inject-env.sh` to an operational script · decide unification of `APP_*` compatibility keys vs `KIS_*` aliases |

- 🟢 `.kiro/README.md`

  | Item | Value |
  | --- | --- |
  | Updated current-progress summary row 03 `marketconnector-ec2` | 2026-06-17 `CONNECTOR_BALANCE` · `CONNECTOR_ORDER_CHECK` read-only paths revalidated · explicitly recorded 0 new order · buy · sell · cancel · modify calls |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | Item | Value |
  | --- | --- |
  | Reinforced 03-marketconnector-ec2 follow-up note | Revalidation based on SSM RunCommand + Secrets Manager + SSM Parameter Store + Instance Role · formalize v5 env injection when transitioning systemd · startup script to normal operating mode · organize safe command wrapper before View · SFN integration |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | Change Log 2026-06-17 | Added |
  | New OD | 0 |
  | OD-SEC-006 first empirical validation | MC EC2 Instance Role-based secret read succeeded · 0 plaintext secret records · first empirical confirmation that internal JSON SecretString key parsing is required |
  | OD-MS-001 · OD-MS-009 first empirical validation | MC EC2 read-only paths revalidated · `CONNECTOR_ORDER_CHECK` belongs to the later part of Daily 17-step, but this execution is recorded as an earlier standalone validation |
  | OD-SAFE-001 ~ OD-SAFE-004 | Consistency maintained · 0 new-order · `--execute` calls |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | Change |
  | --- | --- |
  | R-DOCS-001 detection / mitigation | [2026-06-17 reinforcement] extract internal values from JSON SecretString, then export environment variables · 0 plaintext value output · check based on runbook §4.1 · §4.2 validation SQL · `connector_api_call_log` BALANCE · ORDER `response_status` · `response_code` · `is_success` |
  | R-DOCS-001 detection / mitigation (continued) | latest `connector_balance_snapshot` row · 0 new rows in `CONNECTOR_ORDER_CHECK` also treated as normal |
  | New R | 0 |

- 🟢 `.kiro/WORKLOG.md`

  | Item | Value |
  | --- | --- |
  | 2026-06-17 section | Newly accumulated (MC read-only dry-run revalidation) |

### Security

  | Item | Result |
  | --- | --- |
  | Plaintext sensitive information recorded | secret · KIS · account · token · RDS · account-id · IAM Role ARN · secret ARN · access key · instance-id · EIP · image digest · task ARN · job ARN plaintext records |
  | Plaintext sensitive information recorded (continued) | 0 (all `[REDACTED]` or `<account-id>` · `<region>` · `<rds-endpoint>` · `<role-arn>` · `<secret-arn>` · `<instance-id>` · `<eip>` · `<venv-path>`) |
  | JSON SecretString internal-key parsing | recorded mapping facts only · 0 plaintext values · first failure case that exported the entire raw SecretString summarized only by cause · action · result (consistent with R-DOCS-001 [2026-06-17 reinforcement]) |
  | AWS · SSM · EC2 · RDS · Secrets Manager · SSM Parameter Store · KIS API | all performed directly by the operator · Kiro only organized documentation · procedures · validation |
  | AWS CLI execution · AWS resource creation · modification · deletion | 0 |
  | Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
  | Full plaintext quotation of CloudWatch Logs · SSM response · PowerShell stdout · KIS API response body | 0 |
  | broker / KIS · new order · buy · sell · cancel · modify · `--execute` · `mark_position_sell_ordered()` · fill · position-sync automatic retry | 0 |
  | Session environment | `aws-paper` only · 0 aws-live work |
  | RDS DDL | 0 |
  | RDS DML | limited to 1 `connector.connector_balance_snapshot` insert · 0 new `connector.connector_position_snapshot` rows (0 holdings normal) · all other operations SELECT-only |
  | Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging | 0 |

<details><summary>🔵 Operational identifier summary</summary>

  | Item | Value |
  | --- | --- |
  | secret name path · SSM Parameter name path | consistent with user-specified policy |
  | Environment-variable key names · KIS API category · api_name | recorded |
  | response_status · response_code · is_success | recorded |
  | DB table name · row count | recorded |
  | `as_of_date` · `as_of_ts` · `called_at` · `source_api` · `source_version` | recorded |

</details>
## 2026-06-16 (Backend AWS E2E dry-run safe subset resumed)

### Changed

- 🟢 `.kiro/specs/09-strategy-research-batch/operation-notes.md` (2026-06-16 §1~§8)

  | Item | Value |
  | --- | --- |
  | (a) BACKTEST_RESEARCH AWS Batch | `portfolio-paper-strategy-research:5` 🟢 **SUCCEEDED** · exitCode 0 · run_id `439d78e7-41fd-4bb7-b455-18564ddff758` · backtest_end_date `2026-06-15` · `strategy_trade_log` 310 · `strategy_backtest_daily` 822 |
  | (a) BACKTEST_RESEARCH AWS Batch (continued) | `strategy_backtest_daily_position` 2375 · total_return `4.66534417` · mdd `-0.08941942` · sharpe `2.68071466` · trade_count `310` |
  | (b) BACKTEST_REPORT rev1~rev3 correction | rev1 local-only · rev2 missing wrapper path · rev3 finalized with module-call method |
  | (c) `portfolio-paper-strategy-report:3` SubmitJob | 🟢 **SUCCEEDED** · 4 objects under S3 prefix `strategy-research/reports/20260616/67522706-9b5f-4770-a312-ceb1987c4655/` (`01_요약_리포트` 7,258 bytes · `02_일자별_매매_리포트` 552,540 bytes · `03_거래_상세_리포트` 329,088 bytes |
  | (c) `portfolio-paper-strategy-report:3` SubmitJob (continued) | `04_추천_리포트` 11,847 bytes · remained private · public read 0) |
  | (d) Task completion | BACKTEST_RESEARCH · BACKTEST_REPORT AWS Batch execution validation + S3 upload reinforcement + operational-path correction complete (rev1 / rev2 retained as closed correction history) |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (2026-06-16 §1~§7)

  | Item | Value |
  | --- | --- |
  | (a) DAILY_BUY_SIGNAL standalone ECS / Fargate | `portfolio-paper-strategy-decision-buy-signal:1` exitCode 0 · image `paper-20260613` · 4 rows in `decision.strategy_daily_signal` 🟢 **READY** · signal_date `2026-06-16` · data_date `2026-06-15` · rank `1~4` |
  | (b) DAILY_POSITION_SIGNAL standalone ECS / Fargate | `portfolio-paper-strategy-decision-position-signal:1` exitCode 0 · daily_run_id `45` · market_signal `AGGRESSIVE` · positions 0 normal skip · 0 new `decision.strategy_daily_position_decision` rows · max_decision_date remained `2026-05-29` |
  | (c) Backend AWS E2E dry-run safe-subset status | Steps 1~7 complete · Steps 8~17 not executed or planned as dry-run skip |
  | (d) Task completion | DAILY_BUY_SIGNAL / DAILY_POSITION_SIGNAL validation complete · order · fill · sync · execution stages deferred as follow-up |

- 🟢 6 documentation updates

  | File | Change |
  | --- | --- |
  | `_common/followups-overview.md` | second 2026-06-16 follow-up note · safe subset complete (Preprocessor · BACKTEST_RESEARCH · BACKTEST_REPORT · DAILY_BUY_SIGNAL · DAILY_POSITION_SIGNAL) · follow-ups (View AWS mapping · safe steps first · order/fill/sync family held pending approval · separate dry-run decision for Execution family) |
  | `_common/risk-register.md` | reinforced R-AUTO-015 detection with BACKTEST_REPORT rev1 · rev2 path-mismatch cases + finalized rev3 module path · Status remains 🟢 **Mitigated** · 0 new risks |
  | `_common/operator-decisions.md` | second Change Log 2026-06-16 entry · 0 new ODs · reinforced first empirical notes for OD-MS-008 · OD-MS-013 · OD-MS-019 · OD-MS-021 |
  | `_common/ms-aws-service-decision-matrix.md` | 4.7 · 4.8 · Section 5 · 6.1 recommendation · first empirical completion notes for Research (AWS Batch first choice) · Decision (ECS Fargate first choice) · no decision-value change |
  | `.kiro/WORKLOG.md` | newly accumulated second 2026-06-16 session · no body change to first session |

### Security

  | Item | Result |
  | --- | --- |
  | Plaintext records of secret · RDS · KIS · account · token · account-id · IAM Role · secret ARN · image digest · access key · job ARN · task ARN | 0 (all `[REDACTED]` or `<account-id>` · `<region>` · `<task-arn>` · `<job-arn>` · `<image-digest>` · `<role-arn>` · `<secret-arn>`) |
  | AWS Batch · ECS · IAM · S3 · Docker · ECR · RDS work | all performed directly by operator · Kiro only organized documentation · procedures · validation |
  | Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
  | Plaintext quotation of CloudWatch · Batch console · ECS event · Docker build · S3 client logs | 0 |
  | Direct broker / KIS · order · fill · Daily Batch entrypoint calls · BUY · SELL · cancel · modify · `--execute` · fill · position-sync automatic retry · `mark_position_sell_ordered()` | 0 |
  | Session environment | `aws-paper` only · 0 aws-live work |
  | RDS DDL | 0 |
  | RDS DML | limited to normal BACKTEST_RESEARCH backtest run + 4 inserts into `decision.strategy_daily_signal` by DAILY_BUY_SIGNAL · 0 new `strategy_daily_position_decision` rows (positions 0 normal skip) |
  | Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog · source · packaging | 0 |
  | Quoted changes from `port_strategy_research/aws_batch_backtest_report_wrapper.py` · Dockerfile · requirements.txt | 0 (facts recorded in 09 spec operation-notes) |

<details><summary>🔵 Operational identifier summary</summary>

  | Item | Value |
  | --- | --- |
  | Job Definition family · revision | recorded |
  | image tag · Compute Environment · Job Queue | recorded |
  | S3 bucket · prefix · filename · file size | recorded |
  | run_id · metric values · signal_status · signal_date | recorded |

</details>
## 2026-06-16 (Crawler missing-data issue resolved + KRX EC2 automation successful)

### Added

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | Item | Value |
  | --- | --- |
  | New OD-MS-022 | 🟠 **잠정** · KRX GUI crawler automated-login operating method = Windows Autologon + Administrator interactive session + Scheduled Task + SSM trigger · directly executing the wrapper · Python under SYSTEM Session 0 through SSM RunCommand is unsuitable for KRX GUI login |
  | New OD-MS-022 (continued) | Headless · non-interactive KRX collection excluded from the operating method based on local validation · Autologon is a paper-only Windows-worker security exception · may fail if Administrator session is absent · Chrome processes may remain |
  | Decision Summary | total 79 → 80 / 잠정 34 → 35 |
  | Change Log 2026-06-16 | Added |
  | OD-MS-011 · OD-MS-015 · OD-MS-020 | Reinforced first empirical notes without body changes · first empirical validation: KRX GUI = Windows EC2 worker + Autologon + Scheduled Task + SSM trigger · non-GUI = ECS Fargate Task Definition `portfolio-paper-interest-crawler:7` + RunTask exitCode 0 · Preprocessor = ready for ECS re-run after raw-data recovery |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` (2026-06-16 §1~§5)

  | Section | Content |
  | --- | --- |
  | (a) Root-cause confirmation | rev6 = Selenium / Chrome smoke command · original `interest_crawler_daily.py` includes KRX GUI stage · non-GUI orchestration missing |
  | (b) New non-GUI Task Definition | see rows below |
  | · script | new `interest_crawler_daily_nongui.py` · Python patch · `encoding="utf-8"` · py_compile / AST import validation |
  | · Docker · ECR | built `portfolio-interest-crawler:paper-20260616-nongui` · pushed to ECR |
  | · Task Definition | `portfolio-paper-interest-crawler:7` · FARGATE · awsvpc · cpu 1024 · memory 2048 · log group `/portfolio/paper/crawler` · log stream prefix `ecs-crawler-nongui-daily` · crawler-app DB secret injection retained |
  | · RunTask | cluster `portfolio-paper-cluster` · failures 0 · lastStatus STOPPED · stopCode `EssentialContainerExited` · exitCode 0 · about 9m 51s |
  | · All CloudWatch Logs steps 🟢 **SUCCESS** | `interest_news` · `interest_agency` · `interest_foreignindex` · `interest_commodity` · `interest_macroeconomic` · `interest_price` · `interest_investorflow` · `interest_marketbreadth` |
  | (c) Raw-data freshness recovery | see rows below |
  | · `interest_price_raw` | 2026-06-08 → 2026-06-15 (1,207,904 → 1,209,624 · 324 Price) |
  | · `interest_investorflow_raw` | 274,204 → 275,949 · 349 Investor |
  | · `interest_marketbreadth_raw` | 4,788 → 4,793 |
  | · `interest_commodity_raw` | 29,132 → 29,162 · 6 Commodity |
  | · `interest_foreignindex_raw` | 33,738 → 33,766 · some HANGSENG · NIKKEI225 · SHANGHAI NULL values separated as non-blocker |
  | · `interest_news_raw` | 2026-06-11 → 2026-06-16 (68,881 → 71,614 · 349 News) |
  | · `interest_agency_raw` | 2026-06-11 → 2026-06-16 (49,018 → 49,044 · 15 Agency Reports) |
  | · `interest_macroeconomic_raw` | 75,742 → 75,777 · 7 Macro |
  | (d) KRX EC2 worker automation revalidation | see phases below |
  | · SSM direct wrapper · Python execution unsuitable | Session 0 · SYSTEM non-interactive GUI |
  | · Headless · non-interactive KRX collection | excluded based on local validation |
  | · Autologon | Microsoft Sysinternals Autologon applied · after EC2 reboot, SSM Online + `query user` showed Administrator console session Active |
  | · trigger chain | SSM RunCommand → `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"` → Scheduled Task → Administrator console → `powershell.exe -ExecutionPolicy Bypass -File C:\portfolio\run_krx_worker_daily.ps1` |
  | · KRX login | succeeded (elapsed 92.83s) · `interest_program` 2026-06-15 · `interest_shortsell` 2026-06-15 349 Company |
  | · wrapper execution result | `DONE :: KRX worker daily` · Last Result 0 · Last Run Time 2026-06-16 04:55:49 · log `C:\portfolio\logs\krx_worker_daily_20260616_045550.log` |
  | · DB row count | `interest_program_raw` 547 → 548 · `interest_shortsell_raw` 190,554 → 190,903 |
  | · Remaining Chrome processes | separated as cleanup follow-up |
  | (e) Final judgment | Crawler hybrid structure complete · Preprocessor ECS ready for re-run · follow-ups = re-run Preprocessor · resume Backend AWS E2E dry-run · implement View |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md`

  | Item | Value |
  | --- | --- |
  | New §15 tasks 82~91 | separate non-GUI Task Definition rev 7 · recover non-GUI raw freshness · KRX EC2 Autologon bootstrap · Administrator console session Active · revalidate SSM → schtasks → Scheduled Task · KRX program · shortsell 2026-06-15 DB freshness |
  | New §15 tasks 82~91 (continued) | determine Preprocessor ready-for-rerun status · lock decision that SSM direct Python · wrapper execution is unsuitable · lock decision to exclude Headless · non-interactive KRX collection · separate remaining Chrome-process cleanup |
  | Completed carry-over items from 2026-06-15 | task 72 (separate non-GUI Task Definition) · task 74 (recover raw freshness) |
  | Task Dependency Graph | added 82~91 branch |
  | 2026-06-16 carry-over summary | Added |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | Change |
  | --- | --- |
  | New R-SEC-009 | Windows Autologon worker security exception · paper-only · restrict RDP inbound · prohibit credential documentation · do not record Administrator password · EC2 stop procedure · consider dedicated local user · Status <span style="color:#BF8700">**Open**</span> |
  | New R-AUTO-016 | KRX GUI collection failure if Administrator interactive session is absent · mitigation = Autologon bootstrap · `query user` pre-check · verify session before schtasks trigger · Status 🟠 **Open** |
  | New R-AUTO-017 | Remaining Chrome processes · mitigation = clean Chrome when wrapper exits · cleanup before next run · Status 🟠 **Open** |
  | R-AUTO-008 | Reinforced detection · mitigation (first empirical validation of 2026-06-16 Autologon bootstrap + Administrator session Active + Scheduled Task trigger flow) |
  | R-DATA-009 | Reinforced mitigation (first empirical validation through raw-freshness recovery on 2026-06-16) |
  | R-DATA-010 | Reinforced mitigation (recovered 6 non-GUI raw sets + 2 KRX raw sets · news · agency 2026-06-16 · Preprocessor can be re-run · foreignindex NULL separated as non-blocker) |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-16 follow-up note)

  | Category | Item |
  | --- | --- |
  | First-stage complete | separated non-GUI Task Definition rev 7 · RunTask exitCode 0 · 6 non-GUI raw sets + 2 KRX raw sets + news · agency 2026-06-16 · KRX EC2 Autologon + Administrator session Active + Scheduled Task trigger revalidated |
  | Decision lock | New OD-MS-022 · reinforced first empirical notes for OD-MS-011 · OD-MS-015 · OD-MS-020 |
  | Reinforced · new risks | New R-SEC-009 · R-AUTO-016 · R-AUTO-017 · reinforced R-AUTO-008 · reinforced R-DATA-009 · R-DATA-010 |
  | Remaining follow-ups | re-run Preprocessor ECS · resume Backend AWS E2E dry-run · EventBridge Scheduler · SFN hybrid orchestration · CloudWatch Logs Agent · collect EC2 worker logs from SSM output · automatically add DB validation output to wrapper · stop EC2 worker after task completes · clean Chrome processes · implement View |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md` (3 new terms)

  | Term | Description |
  | --- | --- |
  | Windows Autologon | Microsoft Sysinternals tool · cost 0 · paper-only worker security exception · prohibit credential documentation · administrator-level decryption risk · related spec 08 |
  | Windows Scheduled Task | Built-in Windows scheduler · cost 0 · execution trigger for KRX GUI worker Administrator interactive session · triggered from SSM RunCommand with `schtasks /Run` · related spec 08 |
  | SSM RunCommand | AWS Systems Manager remote command execution · cost 0 · documents such as `AWS-RunPowerShellScript` · entry point for KRX worker Scheduled Task trigger and MC EC2 `CONNECTOR_BALANCE` · direct wrapper · Python execution under SYSTEM Session 0 / non-interactive GUI is unsuitable for KRX GUI login · related specs 03 · 08 |

### Changed

- 🟢 4 documents under 08 spec · 2 common docs · README · WORKLOG

  | File | Change |
  | --- | --- |
  | `08/design.md` §15 | updated 2026-06-16 Hybrid execution model · separated meaning of rev 6 / rev 7 · SSM direct Python · wrapper execution unsuitable · excluded Headless · non-interactive KRX collection · no decision-value changes in §12~§14 · workload states (Preprocessor MS remains ECS Fargate · non-GUI crawler ECS rev 7 |
  | `08/design.md` §15 (continued) | KRX GUI crawler Windows EC2 + Autologon + Scheduled Task + SSM) |
  | `08/requirements.md` R8 · R9 | KRX GUI requires Windows interactive session · SSM triggers Scheduled Task rather than executing Python directly · Autologon is paper-only exception · completion criteria for KRX program · shortsell = DB max date + row count |
  | `08/requirements.md` R8 · R9 (continued) | non-GUI crawler = exclude KRX GUI import on ECS Fargate · per-step SUCCESS log · validate raw-table freshness |
  | `_common/cost-simulation.md` 6.1 | KRX Windows worker cost note · Autologon · Scheduled Task · SSM RunCommand itself cost 0 · costs centered on Windows EC2 running · EIP · storage · log volume · follow-up cost saving by stopping after work · no paper / live unit-price change |
  | `_common/ms-aws-service-decision-matrix.md` 4.3 · Section 5 | updated hybrid execution-model note (2026-06-16 result) · KRX GUI = Windows EC2 worker · non-GUI = ECS Fargate · preprocessor = ECS Fargate · rationale that KRX GUI headless · Lambda · ECS-only · SSM direct are unsuitable · non-GUI rev 7 empirical validation complete |
  | `_common/ms-aws-service-decision-matrix.md` 4.3 · Section 5 (continued) | No decision-value change |
  | `.kiro/README.md` | "Current progress summary" · Interest Crawler "hybrid first-stage partially complete" → "hybrid structure complete (non-GUI = ECS rev 7 · KRX GUI = Windows EC2 + Autologon + Scheduled Task + SSM · raw freshness recovered · Preprocessor ready for re-run)" · View / Backend AWS E2E dry-run follow-up remains |
  | `.kiro/WORKLOG.md` | newly accumulated 2026-06-16 (Crawler missing-data issue resolved + KRX EC2 automation successful) section · no changes to other sessions |

### Security

  | Item | Result |
  | --- | --- |
  | Plaintext sensitive information recorded | secret · Administrator password · KRX login password · RDS · KIS · account · token · account-id · IAM · secret ARN · ECR URI · image digest · instance-id · task ARN plaintext records |
  | Plaintext sensitive information recorded (continued) | 0 (all `[REDACTED]` or `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` · `<role-arn>` · `<secret-arn>`) |
  | Autologon handling | paper-only Windows-worker security exception (R-SEC-009) · automatically creates Administrator interactive session via Autologon · prohibit credential documentation · 0 plaintext Administrator password records · explicitly note administrator-level decryption risk |
  | SSM direct Python · wrapper execution | excluded from operating method (SYSTEM Session 0 / non-interactive GUI · unsuitable for KRX GUI login · consistent with OD-MS-022) |
  | Headless · non-interactive KRX collection | excluded from operating method (KRX login · nos_setup · keyboard security · iframe constraints · consistent with OD-MS-022) |
  | AWS · SSM · EC2 · ECS · ECR · Docker · IAM · Secrets Manager · RDS · GRANT | all performed directly by operator · Kiro only organized documentation · procedures · validation |
  | Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
  | Plaintext quotation of CloudWatch Logs · wrapper logs · SSM response · Selenium · Chrome stdout · stderr · Docker build logs | 0 |
  | broker / KIS calls · live BUY · SELL · `--execute` order submission · fill · position-sync automatic retry | 0 |
  | RDS DDL · aws-live work | 0 (this date limited to `aws-paper`) |
  | Changes by Kiro to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog | 0 |
  | Quoted bodies of `port-interest-crawler` / `port-interest-preprocessor` Dockerfile · requirements.txt · `interest_crawler_daily_nongui.py` | 0 (facts recorded in 08 spec operation-notes) |

<details><summary>🔵 Operational identifier summary</summary>

  | Item | Value |
  | --- | --- |
  | image tag | `paper-20260616-nongui` |
  | Task Definition | `portfolio-paper-interest-crawler:7` |
  | Scheduled Task | `Portfolio-KRX-Worker-Daily` |
  | wrapper log | `krx_worker_daily_20260616_045550.log` |
  | KRX load date | 2026-06-15 |
  | news · agency load date | 2026-06-16 |
  | non-GUI raw 6-set load date | 2026-06-15 |

</details>
## 2026-06-15 (Backend AWS E2E dry-run first pass + Interest Crawler status reassessment)

### Added

- 🟡 **New decision locks OD-MS-020 · OD-MS-021**

  | Item | Value |
  | --- | --- |
  | OD-MS-020 | `port-interest-crawler` status reassessment — Interest Crawler hybrid first-stage implementation partially complete · KRX GUI worker operationally usable · ECS Fargate crawler smoke validated · operational path for non-GUI daily raw collection and full raw-data freshness validation remain follow-up |
  | OD-MS-020 judgment rule | Use "complete" only after actual data loading · freshness validation are confirmed |
  | OD-MS-021 | Backend AWS E2E dry-run 17-step order + safety criteria |
  | 17-step order | 1 `CONNECTOR_BALANCE` · 2 `INTEREST_CRAWLER` · 3 `PREPROCESSOR` · 4 `BACKTEST_RESEARCH` · 5 `BACKTEST_REPORT` · 6 `DAILY_BUY_SIGNAL` · 7 `DAILY_POSITION_SIGNAL` · 8~11 BUY/SELL/AUTO |
  | 17-step order (continued) | 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` · 13 `CONNECTOR_ORDER_CHECK` · 14~16 fill·position sync · 17 `BALANCE_REFRESH` |
  | Safety criteria | live BUY / SELL execution <span style="color:#D1242F">**Prohibited**</span> · `--execute` order submission <span style="color:#D1242F">**Prohibited**</span> · automatic fill·position-sync retry <span style="color:#D1242F">**Prohibited**</span> |
  | Safety criteria (continued) | aws-live work <span style="color:#D1242F">**Prohibited**</span> · Research precedes Decision · Execution·MarketConnector family dry-run or skip |
  | Status | both 🟠 **잠정** |
  | Decision Summary | total 77 → 79 · 잠정 32 → 34 |
  | Change Log | second 2026-06-15 entry |
  | OD-SEC-006 | No body change · reinforced first empirical note for `AccessDeniedException` when MarketConnector EC2 role accesses preprocessor secret · Status remains 잠정 |
  | OD-DB-008 | No body change |

- 🟢 **New `.kiro/README.md` "Current progress summary" section**

  | Item | Value |
  | --- | --- |
  | Interest Crawler | hybrid first-stage · partially complete |
  | Preprocessor | ECS execution successful · input raw-data freshness constraint |
  | Connector Balance | first execution successful |
  | Strategy Research | first AWS Batch validation passed |
  | Strategy Decision · Execution | first RunTask validation passed |
  | aws-live | not started |
  | Detailed references | `followups-overview.md` · `WORKLOG.md` |

### Changed

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` (2026-06-15 §1~§8)**

  | Section | Content |
  | --- | --- |
  | (a) Pre-check | AWS account · region · EC2 · ECS · AWS Batch |
  | (b) `CONNECTOR_BALANCE` | MarketConnector EC2 · venv Python · SSM RunCommand · corrected KIS Secrets JSON-key parsing (`APP_KEY` → `KIS_APP_KEY`, etc. mapping) · backed up existing `access_token.txt` · issued new token · KIS balance API status 200 · paper-account balance query |
  | (b) `CONNECTOR_BALANCE` (continued) | `connector_balance_snapshot` saved · 0 holdings in `connector_position_snapshot` · 0 legacy holdings |
  | (c) KRX GUI worker re-run | `KRX already logged in` · `KRX Login Ready` · `[Collected Date] None` idempotent · latest `interest_program_raw` · `interest_shortsell_raw` date 2026-06-12 |
  | (d) SQL check of 7 non-GUI raw datasets | `interest_agency_raw` 2026-06-11 · `interest_news_raw` 2026-06-11 · `interest_commodity_raw` · `interest_foreignindex_raw` · `interest_investorflow_raw` · `interest_marketbreadth_raw` · `interest_price_raw` all 2026-06-08 |
  | (d) SQL check of 7 non-GUI raw datasets (continued) | `interest_ticker_value_raw` 2026-03-09 (excluded as a core dry-run blocker) |
  | (e) preprocessor ECS RunTask | cluster `portfolio-paper-cluster` · TD `portfolio-paper-interest-preprocessor:1` · FARGATE · awsvpc · public-a+public-b · `assignPublicIp=ENABLED` · SG `sgroup-preprocessor-tasks` · lastStatus `STOPPED` |
  | (e) preprocessor ECS RunTask (continued) | stopCode `EssentialContainerExited` · container `interest-preprocessor` · exitCode 0 · about 3m 43s · DB `updated_at` updated to 2026-06-15 11:03:55+00 (KST 20:03:55) · 0 new 2026-06-15 feature-date rows |
  | (f) Secret / IAM permission-separation validation | MarketConnector EC2 role → preprocessor secret `GetSecretValue` `AccessDeniedException` · policy consistency behaved correctly · 0 added permissions · preprocessor DB confirmation via ECS Task or SSM Port Forwarding · 0 IAM changes |
  | (g) 17-step progress checklist | complete 1·3 · partially complete 2 · not started 4~7 · not started / planned dry-run skip 8~17 · 0 live BUY·SELL·`--execute`·automatic fill·position-sync retry · 0 aws-live |
  | (h) Follow-up handoff | separate non-GUI daily operating Task · recover raw freshness · rerun preprocessor + inspect feature date · resume dry-run · review wording consistency |
  | Wording-correction decision lock | aligned with OD-MS-020 · "Interest Crawler complete: complete" → "Interest Crawler hybrid first-stage implementation: partially complete" |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md` new §14 tasks 70~81**

  | Item | Value |
  | --- | --- |
  | New task scope | reconfirm KRX GUI worker operability · KRX raw latest-date SQL · non-GUI Task Definition · follow-up command separation · periodic non-GUI raw latest-date SQL · recover raw freshness · preprocessor RunTask procedure · confirm `updated_at` update · preprocessor rerun procedure · automate raw·feature freshness-validation SQL |
  | New task scope (continued) | wording-correction decision lock · 17-step checklist · Secret·IAM permission-separation validation |
  | Task Dependency Graph | added 70~81 branch |
  | 2026-06-15 carry-over items | added 7-item summary |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/design.md` §14 reinforcement**

  | Item | Value |
  | --- | --- |
  | Reinforcement scope | 2026-06-15 operator validation results · Interest Crawler status reassessment |
  | Keep §12 · §13 | hybrid execution model · no body changes to first-stage automation-completion judgment |
  | Status by workload | Preprocessor MS = one-off RunTask success + data-freshness constraint · KRX GUI worker = re-run idempotent · non-GUI crawler = operational execution not reached |
  | Initial non-GUI inventory classification | 8 ECS Fargate candidates · 4 excluded |
  | Wording correction | aligned with OD-MS-020 · raw-freshness validation gap → downstream-impact table · Backend AWS E2E dry-run entry-consistency table |
  | Update rule | no changes to §1~§13 · explicitly state non-GUI operation not reached · separate follow-ups |

- 🟢 **New R-DATA-009 · R-DATA-010 in `.kiro/specs/_common/risk-register.md`**

  | ID | Content |
  | --- | --- |
  | New R-DATA-009 | Risk of misinterpreting smoke validation as completion of daily-data freshness · mitigation = "complete" wording rule (aligned with OD-MS-020) · separate smoke pass from daily-raw freshness pass · review wording consistency across docs·reports·slides · confusing KRX GUI worker completion with overall completion <span style="color:#D1242F">**Prohibited**</span> |
  | New R-DATA-009 (continued) | detection = grep residual wording · rollback = immediately correct to approved wording · Affected Spec 08·04·09·all · Status <span style="color:#BF8700">**Open**</span> |
  | New R-DATA-010 | see rows below |
  | · Risk | preprocessor / Research / Decision consume stale raw while raw freshness is insufficient · no new feature date generated · Daily Decision produced against historical trading date · latest trading date not reflected immediately before Research backtest |
  | · mitigation | aligned with OD-MS-020 · OD-MS-021 · run raw-freshness SQL before dry-run entry · periodically check latest dates for 7 non-GUI raw datasets · after preprocessor rerun validate feature max date · `updated_at` · task 78 automation follow-up |
  | · detection | compare `MAX(trade_date)` across `interest_*_raw` · compare `data_date` |
  | · rollback | restore raw freshness, rerun preprocessor + inspect feature date |
  | · Affected Spec | 08·04·09 |
  | · Status | 🟠 **Open** |
  | R-DATA-005 · R-DATA-006 · R-DATA-007 · R-DATA-008 · R-COST-003 | No body change |

- 🟢 **Second 2026-06-15 follow-up note in `.kiro/specs/_common/followups-overview.md`**

  | Category | Content |
  | --- | --- |
  | First-stage complete | pre-check · first `CONNECTOR_BALANCE` · KRX GUI worker re-run idempotent · KRX raw latest date · non-GUI raw latest-date SQL · preprocessor ECS RunTask success · Secret·IAM permission-separation validation · 17-step progress summary |
  | Decision lock | New OD-MS-020 · OD-MS-021 · first empirical note for OD-SEC-006 |
  | Reinforced·new risks | New R-DATA-009 · R-DATA-010 |
  | Remaining follow-ups | separate non-GUI crawler operational TD·command · automate interest raw-freshness validation · automate raw·feature freshness-validation SQL after preprocessor execution · recover raw freshness · rerun preprocessor + inspect new feature date · resume Backend E2E dry-run · review wording consistency |

- 🟢 **`.kiro/WORKLOG.md`**

  | Item | Value |
  | --- | --- |
  | New session | second session 2026-06-15 (Backend AWS E2E dry-run first pass + Interest Crawler status reassessment) |
  | First session | Strategy Research AWS Batch skeleton + full/report + S3 upload · no body change |

### Security

| Item | Result |
| --- | --- |
| Plaintext records of secret · token · password · RDS · KIS · account · KRX login password · account-id · IAM access key · secret ARN · IAM Role ARN · ECR URI · full sha256 image digest · instance-id · task ARN · EIP | 0 |
| Placeholders used | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` · `<role-arn>` · `<secret-arn>` |
| AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · RDS · GRANT work | all performed directly by operator · Kiro only organized documentation·procedures·validation |
| Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
| KIS Secrets JSON key → environment-variable mapping | recorded mapping facts only · 0 plaintext values · first empirical validation of policy that exporting raw SecretString is <span style="color:#D1242F">**Prohibited**</span> |
| Plaintext quotation of CloudWatch · wrapper logs (`krx_worker_daily_*.log`) · SSM response · docker build · Selenium·Chrome stdout·stderr | 0 |
| broker / KIS calls | limited to read-only `CONNECTOR_BALANCE` (status 200 · paper-account balance) · `connector_balance_snapshot` · `connector_position_snapshot` inserts |
| New BUY · SELL · cancel · modify · `--execute` | 0 (aligned with OD-SAFE-001~004 · R-AUTO-009~011) |
| Automatic fill · position-sync retry | 0 |
| RDS DDL | 0 |
| aws-live work | 0 (this date limited to `aws-paper`) |
| MarketConnector EC2 role `AccessDeniedException` | recorded only as first policy-consistency validation · 0 added permissions · 0 IAM changes |
| Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog | 0 |

<details><summary>🔵 Operational identifier summary</summary>

- MarketConnector EC2 role name · preprocessor TD family·revision
- cluster `portfolio-paper-cluster` · SG `sgroup-preprocessor-tasks`
- Log Group `/portfolio/paper/preprocessor`
- Secret `/portfolio/paper/rds/preprocessor-app` · `/portfolio/paper/kis/marketconnector`
- Compute Environment · Job Queue · Job Definition revision 3
- preprocessor `updated_at` 2026-06-15 11:03:55+00
- KRX raw latest date 2026-06-12 · latest-date inventory for 7 non-GUI raw datasets

</details>
## 2026-06-15 (Strategy Research AWS Batch execution validation complete)

### Added

- 🟡 **New OD-MS-019 decision lock · first empirical validation of OD-MS-008 · OD-MS-018**

  | Item | Value |
  | --- | --- |
  | New OD-MS-019 | `port_strategy_research` AWS Batch porting targets = 2 flows: `BACKTEST_RESEARCH` + `BACKTEST_REPORT` (based on View Daily Batch) |
  | OD-MS-019 exclusion | `run_extended_analysis.py` = already executed inside BACKTEST_RESEARCH · excluded as separate AWS Batch porting target + classified as manual auxiliary tool |
  | OD-MS-019 exclusion | 4 `block_watch_*` · `block_exception_buy_*` flows = excluded from AWS Batch porting + heavy-classification follow-up |
  | Preserve report artifacts | reuse S3 bucket `portfolio-paper-migration-yukiever` · prefix `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/` |
  | Job Role S3 PutObject | Resource limited to `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` · public read 0 · wildcard 0 |
  | Status | 🟠 **잠정** |
  | Decision Summary | total 76 → 77 · 잠정 31 → 32 |
  | OD-MS-008 | No body change · Strategy Research compute first choice remains AWS Batch · first empirical validation: both BACKTEST_RESEARCH + BACKTEST_REPORT SubmitJob `SUCCEEDED` / exitCode 0 |
  | OD-MS-018 | No body change · Research Batch image dependency boundary = Research internal adapter migration · image includes `port_strategy_research` + `port_strategy_common` · excludes `port_strategy_decision` |
  | image rebuild | `paper-20260615-report-s3` · image digest sha256 placeholder · size about 106MB · pushedAt 2026-06-15T17:00:10+09:00 |
  | Strategy Common confirmation | 3 Research adapters · major common modules passed py_compile · common-import smoke passed for each MS · "first consistency confirmation complete / formal package management is follow-up" |

### Changed

- 🟢 **`.kiro/specs/09-strategy-research-batch/operation-notes.md` (3 sections on 2026-06-15)**

  <details><summary>(a) AWS Batch execution skeleton complete (§1~§13)</summary>

  | Item | Value |
  | --- | --- |
  | Compute Environment | `portfolio-paper-strategy-research-ce` · MANAGED · FARGATE · maxvCpus 4 · state ENABLED · status VALID |
  | Job Queue | `portfolio-paper-strategy-research-queue` · priority 10 · state ENABLED · status VALID |
  | Job Definition rev1 | `portfolio-paper-strategy-research:1` · image `paper-latest` · vCPU 1 · memory 2048 · timeout 600s · FARGATE · assignPublicIp ENABLED · safe `py_compile` smoke as default command |
  | CloudWatch Log Group | `/portfolio/paper/strategy-research` · retention 14 days |
  | Secrets Manager | `/portfolio/paper/rds/research-app` · JSON multi-key `host`/`port`/`dbname`/`username`/`password` · 0 secret-value exposure |
  | Execution Role | `portfolio-paper-research-batch-execution-role` · `AmazonECSTaskExecutionRolePolicy` + research-app secret-read inline · ARN-scoped · wildcard 0 |
  | Job Role | `portfolio-paper-research-job-role` · minimal permissions for initial smoke stage · added prefix-scoped `s3:PutObject` in §11 · public read 0 |
  | py_compile smoke SubmitJob | `smoke-strategy-research-import-20260615` · jobId `81ec3581-0204-43ea-8238-a2a6d22f3f28` · 🟢 **SUCCEEDED** · exitCode 0 |
  | DB smoke SubmitJob | `smoke-strategy-research-db-20260615` · jobId `5399aa10-0fdd-466b-8079-236d3b7e7e37` · 🟢 **SUCCEEDED** · exitCode 0 · `db smoke ok` · `research_app` · `portfolio` · schema `research` · search_path consistent · image pull |
  | DB smoke SubmitJob (continued) | secret injection · log-delivery errors 0 · secret exposure 0 |
  | Strategy Common | no separate compute · vendoring retained · formal package management remains follow-up |
  | 6/13 follow-up closure | (i) first creation of AWS Batch CE · Queue · JD (ii) formal creation of Log Group · Secrets Manager · IAM Role (iii) no-op · import-smoke SubmitJob validation |

  </details>

  <details><summary>(b) full / report execution validation (§1~§7)</summary>

  | Item | Value |
  | --- | --- |
  | BACKTEST_RESEARCH full | 🟢 **SUCCEEDED** · exitCode 0 |
  | run_id | `a39b0b0c-cfe9-474e-8a4a-4ddb33f09567` |
  | total_return | `4.55930879` |
  | mdd | `-0.08941942` |
  | sharpe | `2.65561307` |
  | trade_count | `308` |
  | extended analysis | included as internal execution |
  | BACKTEST_REPORT | 4 reports generated 🟢 **SUCCEEDED** · exitCode 0 · `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` applied |
  | Final AWS Batch porting targets | BACKTEST_RESEARCH + BACKTEST_REPORT |
  | `run_extended_analysis.py` | handled inside BACKTEST_RESEARCH + classified as manual auxiliary tool |
  | heavy-classification SubmitJob | 0 |
  | RDS DDL · DML | 0 |
  | broker · KIS calls | 0 |
  | Batch automatic retry | 0 |

  </details>

  <details><summary>(c) BACKTEST_REPORT S3 upload reinforcement (§11~§22)</summary>

  | Item | Value |
  | --- | --- |
  | S3 bucket | reuse `portfolio-paper-migration-yukiever` |
  | Job Role permission addition | `s3:PutObject` · Resource `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` · public read 0 · wildcard 0 |
  | requirements.txt | added `boto3` · Docker internal import smoke successful |
  | report-wrapper options | skip if `REPORT_S3_BUCKET` unset · default `REPORT_S3_PREFIX` `strategy-research/reports` · separate subpath by `AWS_BATCH_JOB_ID` · default `REPORT_OUTPUT_DIR` remains `/tmp/portfolio-reports` |
  | Docker rebuild + ECR push | image tag `paper-20260615-report-s3` · digest sha256 placeholder · size about 106MB · pushedAt 2026-06-15T17:00:10+09:00 |
  | Job Definition rev3 | image `paper-20260615-report-s3` · TaskRole `portfolio-paper-research-job-role` |
  | S3 upload SubmitJob | `strategy-research-backtest-report-s3-20260615` · jobId `112f5fe4-02f3-4614-a88c-60a9842e1447` · 🟢 **SUCCEEDED** · exitCode 0 · logStreamName `strategy-research/default/b18e548d46764cd791028088e1d32a6d` |
  | 4 S3 objects | prefix `strategy-research/reports/20260615/112f5fe4-.../` · `01_요약 리포트` ~ `04_추천 리포트` · remained private · public read 0 |
  | 6/13 follow-up closure | full backtest · long-running research · report generation · `REPORT_OUTPUT_DIR` separation · S3 upload reinforcement |

  </details>

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | Change |
  | --- | --- |
  | R-AUTO-015 | reinforced mitigation·detection · promoted Status `Open` → 🟢 **Mitigated** |
  | R-AUTO-015 reinforcement evidence | (i) separate `smoke`/`full` job-name prefixes · 2 smoke jobs use prefix `smoke-strategy-research-*-20260615` · standalone BACKTEST_RESEARCH·REPORT use `full` (ii) JD `attemptDurationSeconds=600` · vCPU 1 · memory 2048 |
  | R-AUTO-015 reinforcement evidence (continued) | `attempts=1` (automatic retry 0 · OD-SAFE-004) (iii) first SubmitJob limited to no-op / import smoke (iv) Batch status · exitCode · Log Stream normal · pull · secret injection · log-delivery errors 0 (v) report S3 prefix scoped · public read 0 |
  | R-AUTO-015 reinforcement evidence (continued) | Resource-scoped PutObject |
  | R-DATA-008 | reinforced detection · added first Strategy Common consistency-confirmation procedure · Status remains `Open` |
  | New R-COST-003 | see rows below |
  | · Risk | without BACKTEST_REPORT S3 lifecycle, storage · PUT costs may accumulate · wrong prefix / bucket · accidental public read |
  | · mitigation | OD-MS-019 prefix scope · Job Role Resource scope · public read 0 · skip if `REPORT_S3_BUCKET` unset · lifecycle follow-up · 06 spec KMS |
  | · detection | periodic storage-usage checks · `s3:PutObject` audit · public-exposure alarm · enforce prefix |
  | · rollback | introduce lifecycle · operator cleans incorrect uploads · immediately correct ACL·policy if public exposure occurs |
  | · Affected Spec | 09·06·10 |
  | · Status | 🟠 **Open** |
  | R-DOCS-001 · R-SEC-001 · R-COST-001 · R-COST-002 | No body change |

- 🟢 **2026-06-15 09-spec follow-up note in `.kiro/specs/_common/followups-overview.md`**

  | Category | Content |
  | --- | --- |
  | First-stage complete scope | AWS Batch CE / JQ / JD rev1 · CloudWatch Log Group · Secrets Manager · IAM Execution + Job Role · py_compile smoke + DB smoke · first Strategy Common consistency confirmation · BACKTEST_RESEARCH full + internal extended analysis |
  | First-stage complete scope (continued) | BACKTEST_REPORT 4 reports · `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` · S3 upload reinforcement + Docker rebuild + ECR push + JD rev3 + SubmitJob + 4 S3 objects confirmed |
  | Decision lock | New OD-MS-019 · first empirical notes for OD-MS-008 · OD-MS-018 |
  | Reinforced·new risks | R-AUTO-015 promoted 🟢 **Mitigated** · reinforced R-DATA-008 detection · new R-COST-003 |
  | 6/13 follow-up closure | 5 items (completed on this date) |
  | Remaining follow-ups | map View Daily Batch BACKTEST_RESEARCH / BACKTEST_REPORT → AWS Batch SubmitJob · Step Functions state machine (enforce order + prohibit automatic retry) + EventBridge Scheduler · procedures for `block_watch_*` · `block_exception_buy_*` manual auxiliary tools |
  | Remaining follow-ups (continued) | formally move 3 Research adapters → `port_strategy_common` · formal common package · CI/CD OIDC · aws-live cutover |
  | Execution order | 08 → 04 → 05 → 09 → 07 → 10 · no change · first execution validation of 09 BACKTEST_RESEARCH / REPORT completed on this date |

- 🟢 **`.kiro/specs/_common/ms-aws-service-decision-matrix.md`**

  | Item | Value |
  | --- | --- |
  | Reinforcement scope | 4.8 port_strategy_research table · final recommendation table in Section 5 · Strategy Research row in 6.1 reinforcement matrix |
  | Reinforcement | first empirical AWS Batch + S3 note |
  | Recommendation change | None (AWS Batch first choice · ECS Fargate Task second choice · Lambda not recommended) |
  | 1.4 · Section 2 · Section 3 · other MS in Section 5 | 0 changes on this date |

- 🟢 **`.kiro/WORKLOG.md`**

  | Item | Value |
  | --- | --- |
  | New session | 2026-06-15 (Strategy Research AWS Batch execution skeleton + full / report validation + S3 upload reinforcement) |
  | Other sessions on this date | no body change (single session on this date) |

### Security

| Item | Result |
| --- | --- |
| Plaintext records of secret · token · password · RDS · KIS · account · KRX password · account-id · IAM access key · secret ARN · IAM Role ARN · ECR URI · full sha256 image digest · EIP · Batch job ARN | 0 |
| Placeholders used | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<job-arn>` · `<role-arn>` · `<secret-arn>` |
| AWS · Docker · ECR · IAM · Secrets Manager · CloudWatch · Batch · S3 · RDS work | all performed directly by operator · Kiro only organized documentation·procedures·validation |
| Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
| Plaintext secret values in CloudWatch Logs body | 0 (only 5-key environment-variable injection · stdout masked) |
| AWS Batch SubmitJob | 3 jobs (py_compile smoke · DB smoke · S3-upload validation) + one BACKTEST_RESEARCH + one BACKTEST_REPORT · all run directly by operator · 0 automatic triggers · 0 automatic retries (OD-SAFE-004 · R-AUTO-001 · R-AUTO-015) |
| heavy-classification SubmitJob | 0 (aligned with OD-MS-019 · classified as manual auxiliary tools) |
| RDS DDL / DML | 0 |
| broker · KIS · order · fill · Daily Batch entrypoint calls | 0 |
| live automatic batch / report generation | <span style="color:#D1242F">**Prohibited**</span> until follow-up approval (OD-SAFE-002 · OD-SAFE-003) · this date is paper environment |
| Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog | 0 |
| Quoted change bodies from `port_strategy_research` requirements.txt · report wrapper · Dockerfile | 0 (facts recorded in 09 spec operation-notes) |

<details><summary>🔵 Operational identifier summary</summary>

- image tags `paper-latest` · `paper-20260615-report-s3` · size about 106MB · pushedAt 2026-06-15T17:00:10+09:00
- Compute Environment `portfolio-paper-strategy-research-ce`
- Job Queue `portfolio-paper-strategy-research-queue`
- Job Definition family `portfolio-paper-strategy-research` revision 1 · 3
- Log Group `/portfolio/paper/strategy-research`
- Secret `/portfolio/paper/rds/research-app`
- Execution Role `portfolio-paper-research-batch-execution-role`
- Job Role `portfolio-paper-research-job-role`
- smoke jobId `81ec3581-...` · `5399aa10-...`
- S3 upload jobId `112f5fe4-...`
- logStreamName `strategy-research/default/b18e548d46764cd791028088e1d32a6d`
- S3 bucket `portfolio-paper-migration-yukiever` · prefix `strategy-research/reports/20260615/112f5fe4-.../`
- 4 filenames
- metric values `run_id a39b0b0c-...` · `total_return 4.55930879` · `mdd -0.08941942` · `sharpe 2.65561307` · `trade_count 308`
- full image digest sha256 → placeholder (`<image-digest>` · truncated form)

</details>
## 2026-06-13 (Strategy Research Batch image first-stage preparation)

### Added

- 🟢 **New `.kiro/specs/09-strategy-research-batch/operation-notes.md`**

  | Item | Value |
  | --- | --- |
  | New file | first artifact of 09 spec · cumulative daily operation notes |
  | Recording format · safety principles | same as operation-notes in 02·03·04·06·08 specs · accumulate under `## YYYY-MM-DD <요약>` headers · result values = success·failure·hold·not applicable·carry-over · quoting docker build · ECR push · full CloudWatch content <span style="color:#D1242F">**Prohibited**</span> · summarize IAM changes in 4 lines · Dockerfile |
  | Recording format · safety principles (continued) | quoting requirements · adapter bodies <span style="color:#D1242F">**Prohibited**</span> · 0 plaintext secret · account-id · actual ARN · image digest · `secretsmanager:GetSecretValue` limited to operator · Kiro automatic validation limited to `DescribeSecret` metadata |
  | IAM-change recording template | Included |

  <details><summary>Details for this date §1~§8</summary>

  | Section | Content |
  | --- | --- |
  | §1 Confirm AWS execution structure | 7 As-Is entrypoints · heavy/light split · external/internal dependencies · `research_app` environment variables + search_path |
  | §2 Remove direct Research → Decision dependency | aligned with OD-MS-018 · added 3 internal Research adapters · import changes in 3 files · Python patch script (`encoding="utf-8-sig"` read + `encoding="utf-8"` write) · 0 remaining `from port_strategy_decision` imports |
  | §3 Batch Docker / ECR | new Dockerfile + requirements.txt · Docker build context `C:\Workspaces` · image includes `port_strategy_research` + `port_strategy_common` · image excludes `port_strategy_decision` |
  | §3 Batch Docker / ECR (continued) | local build `portfolio-strategy-research:paper-20260613` · `py_compile` smoke + import smoke passed · confirmed `/app/port_strategy_decision` absent · new ECR repository `portfolio-strategy-research` + pushed `paper-20260613` |
  | §3 Batch Docker / ECR (continued) | `paper-latest` · image size about 90MB |
  | §4 ~ §5 AWS Batch follow-up | separated follow-ups for Compute Environment · Job Queue · Job Definition · CloudWatch Log Group · Secrets Manager · IAM Role · timeout · vCPU · memory · no-op smoke SubmitJob · full backtest |
  | §6 First-stage completion criteria | 11 items |
  | §7 Out of scope / follow-up for this date | 9 items |
  | §8 Safety·security checks | 0 plaintext secret · KIS · account · token · account-id · ARN · image digest · IAM access key · Batch job ARN · `secretsmanager:GetSecretValue` 0 · AWS Batch SubmitJob 0 · full backtest · long-running research · report · extended |
  | §8 Safety·security checks (continued) | block-family 0 · RDS DDL/DML 0 · broker · KIS calls 0 · 8 MS changes 0 · quoted Dockerfile · requirements.txt · 3 adapters · 3 import-change bodies 0 |

  </details>

### Changed

- 🟡 **New OD-MS-018 in `.kiro/specs/_common/operator-decisions.md`**

  | Item | Value |
  | --- | --- |
  | New OD-MS-018 | port_strategy_research Batch image dependency boundary = move to Research internal adapters |
  | Image includes | `port_strategy_research` + `port_strategy_common` |
  | Image excludes | `port_strategy_decision` |
  | 3 new Research internal adapters | `research_backtest_market_adapter.py` · `research_backtest_filter_adapter.py` · `research_backtest_sizing_adapter.py` |
  | Replaced targets | remove direct imports from existing `port_strategy_decision.backtest_market` · `backtest_filter` · `backtest_sizing` |
  | Import changes in 3 files | `backtest_engine.py` · `backtest_buy_logic.py` · `block_exception_buy_engine_run.py` |
  | Adapter principle | call `port_strategy_common` only · contain no independent decision logic (aligned with R-DATA-008 mitigation) |
  | Long-term candidate | formally move adapters → `port_strategy_common` package (follow-up to OD-MS-005 · OD-MS-014) |
  | Status | 🟠 **잠정** |
  | Decision Summary | total 75 → 76 · 잠정 30 → 31 |
  | Change Log | sixth 2026-06-13 entry |
  | OD-MS-008 | No body change · AWS Batch first choice · ECS Fargate Task second choice · Lambda remains not recommended |

- 🟢 **2026-06-13 09-spec follow-up note in `.kiro/specs/_common/followups-overview.md`**

  | Category | Content |
  | --- | --- |
  | First-stage complete scope | As-Is entrypoint · heavy-light split · dependencies · environment variables · removed direct Research → Decision dependency · Dockerfile · requirements.txt · local build · py_compile · import smoke · ECR push |
  | Decision lock | OD-MS-018 |
  | Reinforced·new risks | notes on R-NET-002 · R-NET-003 · R-DATA-005 · new R-AUTO-015 · R-DATA-008 |
  | Remaining follow-ups (10 items) | AWS Batch CE · Queue · JD · CloudWatch Log Group · Secret · IAM Role · smoke SubmitJob · cost/time/timeout criteria for full backtest · adapter → common package migration · Step Functions integration·EventBridge · CI/CD OIDC · aws-live cutover |
  | Execution order | 08 → 04 → 05 → 09 → 07 → 10 · no change · 09 Batch image preparation completed early on 2026-06-13 |

- 🟢 **New R-AUTO-015 · R-DATA-008 in `.kiro/specs/_common/risk-register.md`**

  | ID | Content |
  | --- | --- |
  | New R-AUTO-015 | see rows below |
  | · Risk | accidental full execution of Strategy Research heavy backtest / report · Batch cost / long occupancy · CE vCPU consumption · RDS read load · heavy-report artifacts incorrectly update operating data |
  | · mitigation | aligned with OD-MS-018 · this date limited to first image preparation · py_compile · import smoke · operator approval for heavy jobs · timeout · vCPU · memory upper bounds · first SubmitJob limited to no-op / import smoke · `smoke`/`full` prefixes · heavy-entrypoint allowlist · JD · SubmitJob runbook follow-up |
  | · detection | Batch job duration · minute-scale smoke / hour-scale full alarms · CloudWatch banner · Cost Explorer · `research.strategy_backtest_run` row · CE desired vCPU · Job Queue depth |
  | · rollback | `aws batch terminate-job` · disable Job Queue · CE desired vCPU 0 · operator cleanup of incorrectly created rows · Cost Anomaly Detection |
  | · Affected Spec | 09·10 |
  | · Status | 🟠 **Open** |
  | New R-DATA-008 | see rows below |
  | · Risk | 3 Research internal adapters fail to follow `port_strategy_common` contract changes (dataclass · enum · reason), causing backtest ↔ Daily Decision inconsistency · undermines trust in `strategy_backtest_run` |
  | · mitigation | aligned with OD-MS-018 · adapters call common only · on common changes, require Research py_compile · import smoke · small-sample test · compare Daily Decision ↔ Research backtest samples · long-term formal adapter → common migration · same pattern as Decision backtest_* |
  | · detection | ImportError · AttributeError · config key missing · dataclass mismatch · reason·signal inconsistency · sizing-result inconsistency |
  | · rollback | fix adapter · previous image tag · manual rerun |
  | · Affected Spec | 09·07·10 |
  | · Status | 🟠 **Open** |
  | R-NET-002 · R-NET-003 · R-DATA-005 | No body change |

- 🟢 **`.kiro/WORKLOG.md`**

  | Item | Value |
  | --- | --- |
  | New session | sixth section for 2026-06-13 Strategy Research Batch image first-stage preparation |
  | Same-date sections 1·2·3·4·5 | No body change |

### Security

| Item | Result |
| --- | --- |
| Plaintext records of secret · token · password · RDS · KIS · account · KRX password · account-id · IAM access key · secret ARN · image digest · EIP · Batch job ARN | 0 |
| Placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<job-arn>` |
| AWS · Docker · ECR · IAM · Secrets Manager · RDS work | all performed directly by operator · Kiro only organized documentation·procedures·validation |
| Live `secretsmanager:GetSecretValue` call | 0 (Secret `/portfolio/paper/rds/research-app` not yet created) |
| AWS Batch SubmitJob | 0 |
| Compute Environment · Job Queue · Job Definition | not created on this date |
| CloudWatch Log Group · IAM Role (execution / job) | not created on this date · all separated as follow-up |
| full backtest · long-running research · report generation · extended analysis · block-family | 0 |
| RDS DDL / DML | 0 |
| broker · KIS · order · fill · Daily Batch entrypoint | 0 |
| live automatic batch / report generation | <span style="color:#D1242F">**Prohibited**</span> until follow-up approval (OD-SAFE-002 · OD-SAFE-003) · this date limited to first paper-image preparation |
| Quoted body of operator-authored Dockerfile · requirements.txt · 3 adapters · 3 import-change files | 0 (facts recorded in 09 spec operation-notes) |
| Changes to 8 MS README · AGENTS.md · CHANGELOG · docs · worklog | 0 |

<details><summary>🔵 Operational identifier summary</summary>

- image tags `paper-20260613` · `paper-latest`
- ECR repository `portfolio-strategy-research`
- Dockerfile · requirements.txt · adapter file paths
- image size about 90MB

</details>
## 2026-06-13 (Strategy Execution ECS / Fargate first porting validation)

### Changed

- 🟢 **`.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (2026-06-13 §1~§11)**

  <details><summary>§1~§7 infrastructure details</summary>

  | Item | Value |
  | --- | --- |
  | §1 AWS execution structure · Docker decision | responsibility separation (OD-MS-016) · inventory of 7 entrypoints · `execution_config.py` requires `INTEREST_DB_PASSWORD` · finalized single Task Definition + command override approach |
  | §2 Dockerfile · requirements.txt | newly authored directly by operator · Docker build context `C:\Workspaces` · source vendoring · first dependency `psycopg2-binary` · safe default CMD `py_compile` · 0 quoted body content |
  | §3 Local build + smoke | `portfolio-strategy-execution:paper-20260613` · `paper-latest` · container `py_compile` smoke + import smoke passed (dummy env) |
  | §4 ECR push | repository `portfolio-strategy-execution` · `paper-20260613` · `paper-latest` |
  | §5 CloudWatch · Secret · IAM | Log Group `/portfolio/paper/strategy-execution` retention 14 days · Secret `/portfolio/paper/rds/execution-app` JSON multi-key |
  | §5 CloudWatch · Secret · IAM (continued) | Execution Role `portfolio-paper-ecs-task-execution-role` + execution-app secret-read inline (ARN-scoped · wildcard 0) · confirmed Task Role `portfolio-paper-execution-task-role` |
  | §6 Network | cluster `portfolio-paper-cluster` · public-a + public-b · SG `sgroup-strategy-tasks` · RDS SG inbound + allow source to VPC Endpoint SG 443 · `assignPublicIp=ENABLED` |
  | §7 Task Definition | `portfolio-paper-strategy-execution` revision 1 ACTIVE · awsvpc · Fargate · cpu 512 · memory 1024 · container `strategy-execution` · 5 `INTEREST_DB_*` secret injections · `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` |

  </details>

  <details><summary>§8 RunTask validation: 8 cases</summary>

  | Case | Result |
  | --- | --- |
  | (a) default `py_compile` | exitCode 0 · lastStatus STOPPED |
  | (b) `execution_sync_buy_fill.py` | NO_TARGET · submitted·synced·skipped all 0 · exitCode 0 |
  | (c) `execution_sync_sell_fill.py` | NO_TARGET · exitCode 0 |
  | (d) `execution_sync_buy_position.py` | filled_buy_orders_without_position 0 · exitCode 0 |
  | (e) `daily_buy_execution_run.py` | blocked by WEEKEND guard · NO_TARGET · `connector_order_request` 0 · exitCode 0 |
  | (f) `daily_sell_execution_run.py` | blocked by WEEKEND guard · exitCode 0 |
  | (g) `daily_auto_sell_execute_run.py --execute` | blocked by WEEKEND guard · 0 `READY -> REQUESTED` transitions · 0 broker · KIS calls · exitCode 0 |
  | (h) `daily_auto_buy_execute_run.py --execute` | blocked by WEEKEND guard · same as above · exitCode 0 |

  </details>

  | Item | Value |
  | --- | --- |
  | §9 First-stage validation completion criteria | 11 items |
  | §10 Out of scope / follow-up for this date (9 items) | Step Functions state machine · EventBridge Scheduler · handoff to MarketConnector executor · `READY -> REQUESTED -> SUBMITTED` end-to-end · promote View monitoring UI · state-command allowlist table · `execution_app` GRANT matrix · aws-live cutover · CI/CD OIDC |
  | §11 Safety·security checks | 0 plaintext secret · KIS · account · token · account-id · ARN · image digest · task ARN · 0 plaintext `secretsmanager:GetSecretValue` result values · 0 plaintext CloudWatch secret values · 0 changes to 8 MS |

- 🟡 **New OD-MS-017 in `.kiro/specs/_common/operator-decisions.md`**

  | Item | Value |
  | --- | --- |
  | New OD-MS-017 | port_strategy_execution Task Definition operating model = single Task Definition + command override |
  | family · revision | `portfolio-paper-strategy-execution` revision 1 ACTIVE · awsvpc · Fargate · cpu 512 · memory 1024 · container `strategy-execution` |
  | 7-entrypoint allowlist | `daily_buy_execution_run` · `daily_sell_execution_run` · `daily_auto_buy_execute_run` · `daily_auto_sell_execute_run` · `execution_sync_buy_fill` · `execution_sync_sell_fill` · `execution_sync_buy_position` |
  | override location | `containerOverrides[].command` in RunTask `--overrides` |
  | MarketConnector executor | `connector_strategy_order_execute.py --execute` not included in this Task Definition (aligned with OD-MS-016) |
  | Status | 🟠 **잠정** |
  | Decision Summary | total 74 → 75 · 잠정 29 → 30 |
  | OD-MS-007 · OD-MS-009 · OD-SAFE-004 | no body change · reinforced first empirical notes |
  | Compared with OD-MS-013 | unlike Decision, which is split into 2 TDs · all 7 entrypoints share the same image · role · secret · log group · cpu · memory |

- 🟢 **2026-06-13 Strategy Execution follow-up note in `.kiro/specs/_common/followups-overview.md`**

  | Category | Content |
  | --- | --- |
  | First-stage complete scope (8 items) | Dockerfile · ECR · Log Group · Secret · IAM · Network · Task Definition · RunTask |
  | Decision lock | OD-MS-017 |
  | Reinforced·new risks | R-AUTO-001 · R-AUTO-014 |
  | Remaining follow-ups (8 items) | Step Functions state machine · EventBridge Scheduler · handoff to MarketConnector executor · `READY -> REQUESTED -> SUBMITTED` end-to-end · promote View ProcessBuilder → monitoring UI · state ↔ command allowlist 1:1 table |
  | Remaining follow-ups (8 items) (continued) | `execution_app` GRANT matrix · failure·skip·manual approval gate · CI/CD OIDC |
  | Execution order | 08 → 04 → 05 → 09 → 07 → 10 · no change |

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | Change |
  | --- | --- |
  | R-AUTO-001 | reinforced mitigation·detection · state-machine review checklist: (a) disable Retry policy on each state (b) command override name must be in the 7-entrypoint allowlist (c) no Retry blocks on BUY / SELL / fill-sync / position-change steps |
  | R-AUTO-001 (continued) | detection adds SFN execution-history retry attempts · state ↔ command override audit · duplicate-row check for `idempotency_key` / `client_order_id` in `connector_order_request` |
  | New R-AUTO-014 | see rows below |
  | · Risk | incorrect mapping under single Task Definition + command override · wrong mapping may execute an unintended entrypoint |
  | · mitigation | OD-MS-017 · formally define state ↔ command override 1:1 table · 7-entrypoint allowlist · automatic retry <span style="color:#D1242F">**Prohibited**</span> · reject arbitrary commands from View / SFN callers · including MarketConnector executor in this TD <span style="color:#D1242F">**Prohibited**</span> |
  | · detection | compare task startup banner ↔ state name · audit ECS RunTask `overrides` (CloudTrail) · validate row counts in `execution.strategy_execution_order` / `connector.connector_order_request` · SFN task input/output consistency |
  | · rollback | SFN `StopExecution` · ECS `StopTask` · operator direct SQL inspection · if broker call occurred, KIS cancel/manual cleanup |
  | · Status | 🟠 **Open** |

- 🟢 **`.kiro/WORKLOG.md`**

  | Item | Value |
  | --- | --- |
  | New session | fifth 2026-06-13 session: Strategy Execution ECS / Fargate first porting validation |
  | Same-date sections 1·2·3·4 | No body change |

### Security

| Item | Result |
| --- | --- |
| Plaintext records of secret · token · password · RDS · KIS · account · KRX · account-id · IAM access key · secret ARN · image digest · EIP · task ARN | 0 |
| Placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · RDS work | all performed directly by operator · Kiro only organized documentation·procedures·validation |
| Plaintext recording of `secretsmanager:GetSecretValue` result values | 0 |
| Plaintext secret values in CloudWatch Logs body | 0 (TD `secrets` field = environment variables · stdout masked) |
| Quoted Dockerfile · requirements.txt body | 0 (facts recorded in this spec operation-notes) |
| broker · KIS · order · fill · Daily Batch entrypoint calls | 0 |
| Live `--execute` calls | 2 (`daily_auto_sell_execute_run.py` · `daily_auto_buy_execute_run.py`) · not a Korean business day · blocked by WEEKEND guard · 0 `READY -> REQUESTED` transitions · 0 `connector_order_request` rows created · 0 broker · KIS calls (aligned with R-AUTO-009·010·011) |
| live automatic orders | <span style="color:#D1242F">**Prohibited**</span> until follow-up approval (OD-SAFE-002 · OD-SAFE-003) |
| Changes to 8 MS | 0 |

<details><summary>🔵 Operational identifier summary</summary>

- image tags `paper-20260613` · `paper-latest`
- Task Definition family `portfolio-paper-strategy-execution` revision 1
- container `strategy-execution` · SG `sgroup-strategy-tasks`
- Log Group `/portfolio/paper/strategy-execution`
- Secret `/portfolio/paper/rds/execution-app`
- Task Role `portfolio-paper-execution-task-role`
- Execution Role `portfolio-paper-ecs-task-execution-role`

</details>
## 2026-06-13 (Local-to-AWS Paper RDS SSM Port Forwarding reinforcement — psql 18 client + pgAdmin4 connection validation)

### Changed

- 🟢 **`.kiro/specs/02-aws-network-and-rds/operation-notes.md` (2026-06-13 SSM Port Forwarding reinforcement §1~§5)**

  | Section | Content |
  | --- | --- |
  | §1 Direct-path execution of psql 18 client | client `18.1` · server `18.4` · SSL `TLSv1.3` · `inet_server_addr=10.0.20.165` · `inet_server_port=5432` · narrowed the remaining R-AUTO-013 issue to general `psql` PATH not being registered |
  | §2 pgAdmin4 Server registration | Name `AWS Paper RDS - portfolio` · Host `localhost` · Port `15433` · Maintenance database `portfolio` · Username `portfolio_admin` · SSL mode `Prefer` |
  | §2 Validation SQL | `current_user` · `current_database` · `inet_server_addr` · `inet_server_port` · `search_path` |
  | §2 Query access confirmation | SELECT available across 6 schemas (connector · decision · execution · interest · ops · preprocessor) · 16 core tables |
  | §2 `portfolio_admin` search_path | output `"$user", public` · not a target of `ALTER ROLE ... SET search_path` · normal (aligned with OD-DB-006) |
  | §3 Runbook reinforcement | added auxiliary procedure (direct psql 18 path · pgAdmin4 Server registration · success criteria) without changing the same-date §10 Runbook body |
  | §4 Safety·security checks | 0 AWS · RDS · IAM · Secrets Manager · SSM changes · RDS DDL/DML 0 · 0 broker · KIS · order · fill entrypoint calls · 0 plaintext password records |
  | §5 Out of scope · follow-up for this date (4 items) | register general `psql` PATH · separate pgAdmin4 servers by environment · pgAdmin4 connection-pool reconnect · Strategy Execution main phase |

- 🟢 **`.kiro/specs/03-marketconnector-ec2/operation-notes.md` (SSM Port Forwarding standard waypoint §1~§3)**

  | Item | Value |
  | --- | --- |
  | EC2 used | `portfolio-paper-marketconnector-ec2` · instance id `i-0fce77927b7397b88` |
  | Role | SSM Port Forwarding waypoint from the earlier same-date section · 2 additional clients (psql 18 · pgAdmin4) pass through the same tunnel |
  | SG · Instance Role · Instance Profile permission changes | 0 |
  | New EC2 creation · type change · EBS recreation · public-subnet change · EIP detach | 0 (aligned with 03 design §2.3) |

- 🟢 **`.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (Strategy Execution pre-validation §1~§4)**

  | Item | Value |
  | --- | --- |
  | Follow-up nature | reinforcement with 2 additional clients on top of the earlier same-date section (first `execution_app` Python `psycopg2` connection validation) |
  | pgAdmin4 query-access confirmation | Strategy Execution core schemas/tables (`execution.strategy_execution_order` · `execution.strategy_execution_plan` · `execution.strategy_position_state` · `execution.connector_signal_order_map` |
  | pgAdmin4 query-access confirmation (continued) | `connector.connector_account` · `connector.connector_order_request` · `connector.connector_order_event` · `connector.connector_fill` · `decision.strategy_daily_position_decision` · `decision.strategy_daily_run`) |
  | Query scope | SELECT only · INSERT · UPDATE · DELETE · DDL 0 |
  | Strategy Execution main phase | Dockerfile · requirements.txt · ECR push · `/portfolio/paper/rds/execution-app` · Task Role · Task Definition · standalone RunTask validation all separated as follow-up |

- 🟡 **New OD-NET-011 in `.kiro/specs/_common/operator-decisions.md`**

  | Item | Value |
  | --- | --- |
  | New OD-NET-011 | Local-to-AWS Paper RDS pgAdmin4 usage principle |
  | Registration principle | register only SSM tunnel `localhost:15433` · direct RDS endpoint registration <span style="color:#D1242F">**Prohibited**</span> |
  | Standard Server registration | Name `AWS Paper RDS - portfolio` · Host `localhost` · Port `15433` · Maintenance database `portfolio` · Username `portfolio_admin` or MS-specific app role · SSL mode `Prefer` |
  | Status | 🟠 **잠정** |
  | Decision Summary | total 73 → 74 · 잠정 28 → 29 |
  | OD-NET-010 | No body change · expanded clients on the same tunnel (Python `psycopg2` · psql 18 · pgAdmin4 all passed) |
  | OD-ENV-006 · OD-ENV-007 · OD-ENV-008 | added first multi-client empirical note without body changes · Status all remain 🟠 **잠정** |
  | OD-NET-009 · R-SEC-001 · R-NET-004 | No body change · retain RDS `PubliclyAccessible=False` |

- 🟢 **2026-06-13 SSM Port Forwarding reinforcement follow-up note in `.kiro/specs/_common/followups-overview.md`**

  | Category | Content |
  | --- | --- |
  | Additional first validation complete | direct psql 18 path execution · pgAdmin4 Server registration·validation SQL·SELECT · Runbook reinforcement |
  | Decision lock | OD-NET-011 |
  | Reinforced risks | R-AUTO-012 detection · R-AUTO-013 mitigation |
  | Remaining follow-ups (5 items) | register general `psql` PATH · environment-specific pgAdmin4 server-prefix policy · password storage by app role for pgAdmin4/psql · pgAdmin4 connection-pool reconnect · client connection validation by MS app role |
  | Execution order | 08 → 04 → 05 → 09 → 07 → 10 · no change |

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | Change |
  | --- | --- |
  | R-AUTO-012 | reinforced mitigation·detection·rollback · pgAdmin4 also uses the same SSM tunnel · pgAdmin4 disconnects immediately when tunnel ends · monitor `Connection terminated` · `server closed the connection unexpectedly` patterns · automatic recovery after tunnel restart · Status remains 🟢 **Mitigated** |
  | R-AUTO-013 | reinforced mitigation·detection·rollback · PostgreSQL 18 client `C:\Program Files\PostgreSQL\18\bin\psql.exe` already installed · first validation passed through direct full-path execution (client 18.1 · server 18.4) · risk narrowed from client absence to general `psql` PATH not being registered |
  | R-AUTO-013 (continued) | detection adds `Get-Item` · `$env:Path` checks · rollback explicitly documents `setx PATH ...` or GUI method · Status remains 🟢 **Mitigated** |

- 🟢 **`.kiro/WORKLOG.md`**

  | Item | Value |
  | --- | --- |
  | New session | fourth 2026-06-13 session: SSM Port Forwarding reinforcement — psql 18 client + pgAdmin4 connection validation |
  | Same-date third section · prior two sections | No body change |

### Security

| Item | Result |
| --- | --- |
| Plaintext records of secret · token · password · RDS · KIS · account · KRX · account-id · IAM access key · secret ARN · image digest · EIP | 0 |
| Placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · SSM · EC2 · RDS · GRANT changes | 0 |
| Live `secretsmanager:GetSecretValue` call | 0 |
| broker · KIS · order · fill · Daily Batch entrypoint calls | 0 |
| Live `--execute` calls | 0 |
| RDS DDL/DML | 0 |
| Tools used | read-only AWS API · reuse of same SSM Port Forwarding tunnel · SELECT-only queries via psql 18 / pgAdmin4 / Python `psycopg2` |
| pgAdmin4 · psql 18 passwords | limited to operator-local PC environment · plaintext recording in spec artifacts · operation notes · console captures · logs <span style="color:#D1242F">**Prohibited**</span> (aligned with R-DOCS-001) |

<details><summary>🔵 Operational identifier summary</summary>

- Reused identifiers from prior section: instance id · private IP · local port `15433` · SSM session id `terraform-vjp3fv3nz73konetcevdzjh9de` · RDS endpoint hostname `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com`
- Local PostgreSQL installation path: `C:\Program Files\PostgreSQL\18\bin\psql.exe` (operator-local PC tool path · not a secret)

</details>
## 2026-06-13 (Local-to-AWS Paper RDS SSM Port Forwarding connection validation + initial Runbook body)

### Changed

- 🟢 **`.kiro/specs/02-aws-network-and-rds/operation-notes.md` (2026-06-13 SSM Port Forwarding §1~§12)**

  | Item | Value |
  | --- | --- |
  | Pre-check tools | AWS CLI `2.27.50` · Session Manager Plugin `1.2.814.0` |
  | Standard waypoint decision | OD-NET-010 = `portfolio-paper-marketconnector-ec2` · instance id `i-0fce77927b7397b88` · keep `portfolio-paper-crawler-worker` separated as KRX-only |
  | Target EC2 SSM Online | ping `Online` · agent `3.3.4515.0` |
  | RDS endpoint | retain `PubliclyAccessible=False` |
  | SSM Port Forwarding tunnel | local port `15433` · session id `terraform-vjp3fv3nz73konetcevdzjh9de` · `Port 15433 opened` |
  | Local psql client | discovered PATH not registered (R-AUTO-013) |
  | Python `psycopg2` check | `psycopg2 OK` |
  | `portfolio_admin` connection validation | `inet_server_addr=10.0.20.165` · `inet_server_port=5432` |
  | `execution_app` connection validation | search_path = `execution, decision, research, connector, preprocessor, interest, reference, legacy, public` (aligned with OD-DB-006 · OD-DB-007) |
  | Initial Runbook body | 7 steps + `[실행]`/`[확인]`/`[준비]`/`[복구]` labels + standard environment variables + app-role mapping by MS + success criteria + failure·recovery |
  | Follow-up handoff | 7 items |
  | Safety·security checks | 0 plaintext password · secret · account · KIS · token · account-id · IAM access key · secret ARN · EIP · operational identifiers recorded as facts (not secrets) |

- 🟢 **`.kiro/specs/03-marketconnector-ec2/operation-notes.md` (SSM Port Forwarding waypoint role §1~§5)**

  | Item | Value |
  | --- | --- |
  | EC2 used | same as the formal operating EC2 in 03 spec (validated 2026-06-10) |
  | Added role | first validation as standard SSM Port Forwarding waypoint for Local-to-AWS Paper RDS access |
  | New EC2 creation · type change · EBS recreation · public-subnet change · EIP detach | 0 (aligned with 03 design §2.3) |
  | SG · Instance Role · Instance Profile permission changes | 0 (`AmazonSSMManagedInstanceCore` managed policy unchanged) |
  | Validation summary for this date | SSM Port Forwarding tunnel · `portfolio_admin` connection · `execution_app` connection · RDS DDL/DML 0 · SELECT-only queries |

- 🟢 **`.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (Strategy Execution pre-validation §1~§4)**

  | Item | Value |
  | --- | --- |
  | Nature | separate from the same-date Strategy Decision ECS / Fargate first-porting validation and responsibility-separation section |
  | Validation target | first `execution_app`-based connection validation before entering Strategy Execution AWS porting |
  | Validation scope | Python `psycopg2` SELECT-only queries |
  | Strategy Execution main phase | Dockerfile · requirements.txt · ECR push · `/portfolio/paper/rds/execution-app` · Execution Role inline policy · Task Role · Task Definition · standalone RunTask validation all separated as follow-up |

- 🟡 **New OD-NET-010 in `.kiro/specs/_common/operator-decisions.md`**

  | Item | Value |
  | --- | --- |
  | New OD-NET-010 | Standard Local-to-AWS Paper RDS SSM Port Forwarding waypoint |
  | Waypoint | single `portfolio-paper-marketconnector-ec2` · instance id `i-0fce77927b7397b88` |
  | local port | `15433` |
  | RDS target | `portfolio-paper-rds:5432` |
  | Status | 🟠 **잠정** |
  | Decision Summary | total 72 → 73 · 잠정 27 → 28 |
  | OD-ENV-006 · OD-ENV-007 · OD-ENV-008 | reinforced empirical notes without body changes · all Status remain 잠정 · candidates for promotion to confirmed at aws-live cutover |
  | OD-NET-009 · R-SEC-001 · R-NET-004 · OD-DB-006 · OD-DB-007 | No body change |

- 🟢 **2026-06-13 SSM Port Forwarding follow-up note in `.kiro/specs/_common/followups-overview.md`**

  | Category | Content |
  | --- | --- |
  | First-stage complete scope | local tool checks · standard waypoint decision · SSM Online · RDS endpoint · tunnel opened · `portfolio_admin` / `execution_app` connection · initial Runbook body |
  | Decision lock | OD-NET-010 |
  | New·reinforced risks | R-AUTO-012 · R-AUTO-013 · reinforced R-DATA-007 detection |
  | Remaining follow-ups (6 items) | formally install psql client · inspect all MS Paper-mode DB environment variables · Strategy Execution AWS porting main phase · MarketConnector executor EC2 deployment candidate · `READY → REQUESTED → SUBMITTED` end-to-end · automatic SSM Port Forwarding session keep-alive |
  | Execution order | 08 → 04 → 05 → 09 → 07 → 10 · no change |

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | Change |
  | --- | --- |
  | R-DATA-007 | reinforced detection · 2026-06-13 SSM Port Forwarding empirical validation · added environment-identification consistency check based on `inet_server_addr` / `inet_server_port` output · empirically confirmed host alone cannot identify environment |
  | New R-AUTO-012 | dependency on Local-to-AWS Paper RDS SSM Port Forwarding tunnel · DB connection immediately drops if tunnel window closes · SSM session times out · target EC2 stops · SSM Online is lost · mitigation = OD-NET-010 · Runbook §10.2 / §10.7 · restart new session · Status 🟢 **Mitigated** |
  | New R-AUTO-013 | operator-local PostgreSQL `psql` client not installed / PATH not registered · use Python `psycopg2` as fallback · formal correction in 02 spec runbook Appendix A · Status 🟢 **Mitigated** |

- 🟢 **`.kiro/WORKLOG.md`**

  | Item | Value |
  | --- | --- |
  | New session | third 2026-06-13 session: SSM Port Forwarding connection validation + initial Runbook body |
  | Same-date Strategy Decision · responsibility-separation sections | No body change |

### Security

| Item | Result |
| --- | --- |
| Plaintext records of secret · token · password · RDS · KIS · account · KRX · account-id · IAM access key · secret ARN · image digest · EIP | 0 |
| Placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · SSM · EC2 · RDS · GRANT changes | 0 |
| Live `secretsmanager:GetSecretValue` call | 0 |
| broker · KIS · order · fill · Daily Batch entrypoint calls | 0 |
| Live `--execute` calls | 0 |
| RDS DDL/DML | 0 |
| Tools used | read-only AWS API + SSM Port Forwarding session + Python `psycopg2` SELECT-only queries |

<details><summary>🔵 Operational identifier summary</summary>

- instance id `i-0fce77927b7397b88` · `i-0ff768ea639a91355`
- private IP `10.0.0.181` · `10.0.0.169` · `10.0.20.165`
- local port `15433`
- SSM session id `terraform-vjp3fv3nz73konetcevdzjh9de` (temporary value · reissued on next session restart)
- RDS endpoint hostname `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com`

</details>
## 2026-06-13 (Strategy Execution / MarketConnector responsibility separation + View Daily Batch 17 steps + Local-to-AWS Paper RDS operating principles)

### Changed

- 🟢 **`.kiro/specs/03-marketconnector-ec2/operation-notes.md` (Strategy order-execution executor added §1~§6)**

  | Item | Value |
  | --- | --- |
  | Responsibility-separation background | separate responsibility boundary from Strategy Execution's direct connector calls · `sys.path` insertion structure |
  | New file | `connector_strategy_order_execute.py` (489 insertions) |
  | Existing 8 entrypoints | `connector_buy.py` · `connector_sell.py` · `connector_order_common.py` · `connector_order_check.py` · `connector_balance.py` · `db_config.py` · `config.py` · `token_manager.py` · guaranteed unchanged |
  | New executor behavior | query REQUESTED + `connector_order_request_id IS NULL` · SELL first, BUY second · default dry run |
  | `--execute` behavior | guard `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` · lazy import `connector_buy` / `connector_sell` · update `SUBMITTED` / `FAILED` · update SELL position to `SELL_ORDERED` |
  | Static validation | `python -m py_compile` passed · UTF-8 Korean text intact · dry-run result `[NO_TARGET] REQUESTED strategy order 없음` |
  | Out of scope for this date | actual `--execute` execution · EC2 deployment · weekday/safe-test-data end-to-end |
  | Safety·security | 0 KIS / broker API calls · RDS DDL/DML 0 · 0 plaintext secret · account · RDS · KIS · token · account-id · ARN · instance-id · EIP |

- 🟢 **`.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (responsibility separation + View 17 steps §1~§7)**

  | Section | Content |
  | --- | --- |
  | §1 Responsibility-separation decision | changed meaning of `--execute` · `connector_order_request_id` policy · transferred SELL `mark_position_sell_ordered()` |
  | §2 Strategy Execution changes | 3 files: `execution_repository.py` · `daily_auto_buy_execute_run.py` · `daily_auto_sell_execute_run.py` · 8 change points · `py_compile` passed · 0 direct import / call search hits · candidate dry-run DB query not confirmed because weekend guard blocked first |
  | §3 Handoff to new MarketConnector executor | aligned with 03 spec |
  | §4 View 17-step changes | 2 files: `DailyBatchService.java` · `DailyBatchLabelUtils.java` · new step `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` (order 12) · subsequent step orders adjusted to 13~17 · 5 isNoTarget cases · labels added · `.\mvnw.cmd clean compile` succeeded |
  | §5 Local development / AWS Paper RDS operating principles | Paper source of truth = single AWS Paper RDS · environment labels LOCAL_DEV·PAPER·LIVE · SSM Port Forwarding direction · example environment variables · 5 synchronization items <span style="color:#D1242F">**Prohibited**</span> |
  | §6 Follow-up handoff | 6 items |
  | §7 Safety·security checks | results accumulated |

- 🟢 **`.kiro/specs/02-aws-network-and-rds/operation-notes.md` (Local-to-AWS Paper RDS operating mode §1~§6)**

  | Section | Content |
  | --- | --- |
  | §1 Paper source of truth | single AWS Paper RDS |
  | §2 Environment labels | LOCAL_DEV · PAPER · LIVE |
  | §3 SSM Port Forwarding direction | formally documented |
  | §4 Example environment variables | formally documented |
  | §5 <span style="color:#D1242F">**Prohibited**</span> items | 5 items |
  | §6 Follow-up handoff | formally documented |
  | RDS Public access · OD-NET-009 · R-SEC-001 · R-NET-004 | No body change |
  | 04 spec §5 | synchronized with Local-to-AWS Paper RDS |
  | Plaintext RDS endpoint · SSM session id · password records | 0 |

- 🟡 **New OD-MS-016 · OD-ENV-006 · OD-ENV-007 · OD-ENV-008 in `.kiro/specs/_common/operator-decisions.md`**

  | ID | Value |
  | --- | --- |
  | OD-MS-016 | responsibility separation for Strategy Execution / MarketConnector order execution · Strategy Execution `--execute` = `READY -> REQUESTED` · MarketConnector executor `--execute` = `REQUESTED -> SUBMITTED`/`FAILED` |
  | OD-MS-016 (continued) | SELL `mark_position_sell_ordered()` belongs to MarketConnector · Strategy Execution does not create/update `connector_order_request_id` · 🟠 **잠정** |
  | OD-ENV-006 | Paper environment DB source of truth = single AWS Paper RDS · if `PORT_ENVIRONMENT=paper`, local execution also uses AWS Paper RDS · 🟠 **잠정** |
  | OD-ENV-007 | Local PC → AWS Paper RDS access = SSM Port Forwarding only · keep RDS private · paper-order execution guard = `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` · 🟠 **잠정** |
  | OD-ENV-008 | no Local DB ↔ AWS Paper RDS synchronization · merging `connector_order_request` · `connector_fill` · `strategy_execution_order` · `strategy_position_state` <span style="color:#D1242F">**Prohibited**</span> · <span style="color:#BF8700">**잠정**</span> |
  | Decision Summary | total 68 → 72 · 잠정 23 → 27 |
  | Change Log | second 2026-06-13 entry |
  | Existing decision values (OD-NET-009 · OD-DB-003 · OD-MS-009 · OD-MS-013, etc.) | No body change |

- 🟢 **2026-06-13 responsibility-separation + Local-to-AWS Paper RDS follow-up note in `.kiro/specs/_common/followups-overview.md`**

  | Category | Content |
  | --- | --- |
  | First-stage complete scope | 3 Strategy Execution files · 1 new MarketConnector file · 2 View files · 5 Local-to-AWS Paper RDS operating principles |
  | Decision lock | OD-MS-016 · OD-ENV-006 · OD-ENV-007 · OD-ENV-008 |
  | Remaining follow-ups (6 items) | organize SSM Port Forwarding runbook · inspect environment variables across 8 MS · Strategy Execution AWS porting · MarketConnector executor EC2 deployment candidate · `READY → REQUESTED → SUBMITTED` end-to-end · new View 17-step pre-operation end-to-end |
  | Execution order | 08 → 04 → 05 → 09 → 07 → 10 · no change |

- 🟢 **4 new risks in `.kiro/specs/_common/risk-register.md`**

  | ID | Content |
  | --- | --- |
  | New R-DATA-007 | merging/synchronizing order·fill·position·strategy-execution state data between local PostgreSQL ↔ AWS Paper RDS can cause duplicate orders · accumulated fills · position-state conflicts · source-of-truth inconsistency · mitigation = OD-ENV-006 · OD-ENV-007 |
  | New R-DATA-007 (continued) | formalized OD-ENV-008 + paper guard + identifying environment by DB host alone <span style="color:#D1242F">**Prohibited**</span> · Status <span style="color:#BF8700">**Open**</span> |
  | New R-AUTO-009 | entering operation without validating MarketConnector executor `--execute` against actual REQUESTED order rows after Strategy Execution `--execute` · mitigation = this date limited to static/dry-run · weekday/safe-test-data end-to-end follow-up |
  | New R-AUTO-009 (continued) | live `--execute` before validation <span style="color:#D1242F">**Prohibited**</span> · hold activation of new View step in paper operation · Status <span style="color:#BF8700">**Open**</span> |
  | New R-AUTO-010 | MarketConnector executor `--execute` guard could be bypassed in wrong environment or pass when environment variables are missing · risk of incorrect broker order · mitigation = guard defaults to reject `--execute` when environment variables are missing · operator directly verifies environment variables |
  | New R-AUTO-010 (continued) | plaintext password·account·endpoint output <span style="color:#D1242F">**Prohibited**</span> · Status <span style="color:#BF8700">**Open**</span> |
  | New R-AUTO-011 | inserted new View step `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` (order 12) · 12~17 range not validated end-to-end · mitigation = only `.\mvnw.cmd clean compile` · 0 Daily Batch execution · Python order scripts · DB · AWS access |
  | New R-AUTO-011 (continued) | weekday/safe-environment end-to-end follow-up before operation · preserve pre-change shape in git · Status 🟠 **Open** |

- 🟢 **`.kiro/WORKLOG.md`**

  | Item | Value |
  | --- | --- |
  | New session | second 2026-06-13 session: responsibility separation / View 17 steps / Local-to-AWS Paper RDS |
  | Strategy Decision ECS / Fargate first porting validation section | No body change |

### Security

| Item | Result |
| --- | --- |
| Plaintext records of secret · token · password · RDS · KIS · account · SSM session id · KRX · account-id · ARN · image digest · IAM access key · instance-id · EIP · task ARN | 0 |
| Placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · SSM · EC2 · RDS · GRANT work | 0 |
| Live `secretsmanager:GetSecretValue` call | 0 |
| broker · KIS · order · fill · Daily Batch entrypoint calls | 0 |
| Live `--execute` calls | 0 |

<details><summary>🔵 Operator-authored change list (0 quoted body content)</summary>

- 3 Strategy Execution files: `execution_repository.py` · `daily_auto_buy_execute_run.py` · `daily_auto_sell_execute_run.py`
- 1 new MarketConnector file: `connector_strategy_order_execute.py`
- 2 View files: `DailyBatchService.java` · `DailyBatchLabelUtils.java`
- only facts recorded in this spec artifacts · operation notes · changelog

</details>
## 2026-06-13 (Strategy Decision ECS / Fargate first porting validation + 08 spec SSM automation + ECS crawler smoke)

### Added

- 🟢 **`.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (Strategy Decision ECS §1~§10)**

  | Item | Value |
  | --- | --- |
  | Confirm AWS execution structure | As-Is analysis · data dependencies · finalized To-Be (hold integrated `daily_decision_run.py` · split into 2 Task Definitions · Docker build context `C:\Workspaces` · vendor `port_strategy_common` + `port_strategy_decision` inside image) |
  | Dockerfile · requirements.txt | newly authored directly by operator |
  | Local build + container smoke | `portfolio-strategy-decision:paper-20260613` |
  | ECR push | repository `portfolio-strategy-decision` · `paper-20260613` · `paper-latest` |
  | CloudWatch Log Group | `/portfolio/paper/strategy-decision` · retention 14 days |
  | Secrets Manager | new `/portfolio/paper/rds/decision-app` JSON multi-key |
  | Execution Role · Task Role | decision-app secret-read inline (ARN-scoped · wildcard 0) + new `portfolio-paper-decision-task-role` |
  | 2 Task Definitions | `portfolio-paper-strategy-decision-buy-signal` · `portfolio-paper-strategy-decision-position-signal` · awsvpc · Fargate · cpu 512 · memory 1024 · 5 `INTEREST_DB_*` values |
  | RunTask execution result | exitCode 0 · CloudWatch logs · RDS connection succeeded · `daily_run_id` 44 · `data_date` 2026-06-08 · `market_signal` BLOCK · block_watch 1 · position decision 0 |
  | Initial failures · actions | (a) buy-signal lacked permission on `research.strategy_block_watch_candidate` → corrected (b) position-signal `relation "strategy_position_state" does not exist` (`execution.strategy_position_state` · insufficient execution + decision schema permissions for `decision_app`) → corrected |
  | Follow-up handoff (7 items) | EventBridge Scheduler · Step Functions state machine · Strategy Execution ECS validation · formal `port_strategy_common` package·version · `decision_app` permission matrix · aws-live cutover · CI/CD OIDC |
  | Plaintext records of actual secret · RDS · KIS · account · token · account-id · ARN · image digest · IAM access key · task ARN | 0 |

### Changed

- 🟡 **New OD-MS-013 · OD-MS-014 · OD-MS-015 in `.kiro/specs/_common/operator-decisions.md`**

  | ID | Value |
  | --- | --- |
  | OD-MS-013 | `port_strategy_decision` Task Definition split policy = hold integrated `daily_decision_run.py` · inherit Daily Batch structure · 2 separate TDs for buy-signal / position-signal · family `portfolio-paper-strategy-decision-buy-signal` |
  | OD-MS-013 (continued) | `portfolio-paper-strategy-decision-position-signal` · command `python -m port_strategy_decision.daily_buy_signal_run` · `python -m port_strategy_decision.daily_position_signal_run` · 🟠 **잠정** |
  | OD-MS-014 | first deployment of `port_strategy_common` = vendoring in first ECS smoke image · formal package/version management deferred to Strategy Common or DevOps hardening stage · 🟠 **잠정** |
  | OD-MS-015 | first KRX GUI crawler automation = SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1` |
  | OD-MS-015 (continued) | approach where SSM directly executes wrapper under SYSTEM Session 0 / SessionId 0 adopted as <span style="color:#D1242F">**Rejected**</span> because it is unsuitable for KRX GUI login · <span style="color:#BF8700">**잠정**</span> |
  | Decision Summary | total 65 → 68 · 잠정 20 → 23 |
  | Change Log | 2 entries for 2026-06-13 |
  | OD-MS-009 · OD-MS-012 | No body change · first automation entrypoint separated into OD-MS-015 |
  | Plaintext secret · endpoint · account-id · ARN · image digest records | 0 |

- 🟢 **`.kiro/specs/_common/followups-overview.md`**

  | Item | Value |
  | --- | --- |
  | 2 follow-up notes for 2026-06-13 | 04 spec (Strategy Decision ECS / Fargate first validation) · 08 spec (SSM RunCommand automation + ECS crawler smoke) |
  | 04 · 08 spec body format | added same "2026-06-13 first application result" section style used in 03 · 08 specs |
  | Execution order | 08 → 04 → 05 → 09 → 07 → 10 · no change |

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | Change |
  | --- | --- |
  | R-DATA-005 | reinforced mitigation·detection·rollback·Affected Spec·Status (2026-06-13 04 spec first application result) |
  | R-AUTO-005 · R-AUTO-007 | reinforced mitigation·detection (2026-06-13 ECS crawler smoke + SSM automation result) |
  | New R-AUTO-008 | KRX login failure when SSM RunCommand directly executes KRX GUI wrapper under SYSTEM Session 0 / SessionId 0 · mitigation = Scheduled Task trigger workaround in OD-MS-015 · Status 🟢 **Mitigated** |
  | Formal organization of `decision_app` permission matrix | separated as follow-up update to 02 spec [db-roles-and-grants.md](../02-aws-network-and-rds/db-roles-and-grants.md) |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` (SSM automation + ECS crawler smoke §1~§8)**

  | Item | Value |
  | --- | --- |
  | SSM Managed Node check | passed |
  | Judgment that direct SSM wrapper execution is unsuitable | SessionId 0 vs 2 separation |
  | Windows Scheduled Task | registered `Portfolio-KRX-Worker-Daily` + first trigger validation |
  | Trigger with RDP closed | SSM RunCommand → `schtasks /Run` · `SUCCESS: Attempted to run the scheduled task` · Task State Running → Ready · latest log `krx_worker_daily_20260613_021904.log` · `[Collected Date] None` idempotent normal completion |
  | ECS crawler smoke | `portfolio-paper-interest-crawler:6` · Selenium Chrome smoke RunTask · public-a/public-b + `sgroup-crawler-tasks` + `assignPublicIp=ENABLED` · exitCode 0 · `SELENIUM CHROME SMOKE SUCCESS` · `example.com` |
  | ECS crawler smoke (continued) | reached Naver Finance (`Npay 증권`) · `DRIVER QUIT` · `SELENIUM CHROME SMOKE END` |
  | Hybrid execution model | judged first-stage complete |
  | Follow-up handoff | 7 items |
  | Plaintext records | 0 secret · KRX login · RDS endpoint · account-id · ARN · image digest · task ARN · instance-id · KRX login ID / password described only as "injected from Secrets Manager" · 0 full plaintext quotation of wrapper · SSM response · CloudWatch · Selenium · Chrome stdout·stderr |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md`**

  | Item | Value |
  | --- | --- |
  | task 30 · 31 partially complete | first ECS crawler smoke passed · separation of operational TD for non-GUI crawler remains task 58 follow-up |
  | task 53 partially complete | rejected direct SSM wrapper execution · adopted Scheduled Task trigger |
  | New §13 tasks 61~69 | SSM Managed Node check · case showing direct SSM execution unsuitable · Windows Scheduled Task registration · RDP-closed SSM trigger · separate ECS crawler rev6 smoke command · Selenium Chrome smoke RunTask · non-GUI crawler inventory · first-stage completion of Hybrid execution model · safety/documentation-recording checks |
  | Task Dependency Graph · carry-over summary | updated 2026-06-10 · 2026-06-12 · 2026-06-13 |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/design.md`**

  | Section | Content |
  | --- | --- |
  | New §13 | "2026-06-13 operator validation result / first-stage Hybrid execution model automation complete" |
  | §13.1 | first-stage KRX GUI automation structure · SSM RunCommand → `schtasks /Run` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1` · EventBridge Scheduler follow-up |
  | §13.2 | judgment that direct execution under SYSTEM Session 0 is unsuitable |
  | §13.3 | ECS crawler TD revision 6 = Selenium / Chrome / outbound smoke only · operational TD separation remains task 58 |
  | §13.4 | first-stage Hybrid execution model completion judgment across 6 dimensions |
  | §13.5 | update principles |
  | Existing decision values in §1~§12 | No change |

### Security

| Item | Result |
| --- | --- |
| Plaintext records of secret · token · password · RDS · KRX · KIS · account · RDS endpoint · account-id · ARN · image digest · IAM access key · instance-id · task ARN | 0 |
| Placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · SSM · EC2 · RDS · GRANT work | all performed directly by operator · Kiro only organized documentation·procedures·validation |
| Live `secretsmanager:GetSecretValue` call | 0 |
| broker · KIS · order · fill · Daily Batch entrypoint calls | 0 |
| KRX login ID / password | described only as "injected from Secrets Manager" |
| Quoted operator-authored Dockerfile · requirements.txt · Scheduled Task · wrapper · environment-variable injection ps1 bodies | 0 (facts recorded in this spec operation-notes) |
## 2026-06-12

### Changed

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` (Windows EC2 worker §1~§11)**

  | Item | Value |
  | --- | --- |
  | Confirm EC2 worker environment | complete |
  | Inject RDS Secret DB env | `/portfolio/paper/rds/crawler-app` |
  | Download-path junction adjustment | complete |
  | KRX program collection | 1 row each for 2026-06-09 · 2026-06-10 · 2026-06-11 · `interest_program_raw` 543 → 546 |
  | KRX shortsell collection | 349 rows each · `interest_shortsell_raw` 189158 → 190205 |
  | Korean literal · encoding validation | decided on Python patch-script approach |
  | KRX Secrets Manager integration | `/portfolio/paper/krx/crawler-login` + IAM permission added |
  | EC2 worker daily wrapper | first execution + idempotent no-op rerun of `run_krx_worker_daily.ps1` |
  | Hybrid execution model classification | KRX GUI = Windows EC2 worker · non-GUI = keep ECS Fargate Task candidate · preprocessor = keep ECS Fargate Task |
  | Follow-up handoff | 8 items |
  | Plaintext records | 0 secret · KRX password · RDS endpoint · account-id · ARN · instance-id · image digest · KRX login ID/password described only as "injected from Secrets Manager" |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md`**

  | Item | Value |
  | --- | --- |
  | crawler runtime tasks 29 · 30 · 31 · 32 | confirmed KRX GUI separation to Windows EC2 worker · non-GUI runtime validation partially complete or deferred · workload reclassification |
  | New §11 tasks 44~52 | EC2 worker checks · DB env injection · download-path junction · KRX program collection · KRX shortsell collection · Korean encoding validation · KRX Secrets Manager integration · first wrapper execution · Hybrid execution model classification decision |
  | §12 follow-up tasks 53~60 | SSM RunCommand · EventBridge Scheduler · Step Functions hybrid orchestration · CloudWatch Logs Agent · automatically add DB validation output inside wrapper · reorganize non-GUI crawler scope · EC2 worker stop procedure · long-term headless KRX collection refactor |
  | Task Dependency Graph · carry-over summary | updated 2026-06-10 · 2026-06-12 |

- 🟢 **New §12 in `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/design.md`**

  | Section | Content |
  | --- | --- |
  | §12.1 Hybrid execution model classification table | New |
  | §12.2 Why KRX GUI is separated to EC2 worker | Chrome GUI · download · login session · debug attach · KRX-site characteristics · Fargate lacks GUI support |
  | §12.3 Secrets Manager integration | RDS preprocessor-app · RDS crawler-app · KRX crawler-login · plaintext secret-value recording <span style="color:#D1242F">**Prohibited**</span> |
  | §12.4 EC2 worker operating mode | first-stage manual wrapper execution operationally available · automation follow-up |
  | §12.5 Update principles | formally documented |
  | Existing decision values in §1~§11 | No change |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/requirements.md`**

  | Item | Value |
  | --- | --- |
  | R8 acceptance criterion 5 | Added · if KRX GUI-dependent collection is incompatible with ECS Fargate GUI/Chrome download/OTP flow, execute on Windows EC2 worker · explicitly separate non-GUI crawler / KRX GUI crawler runtime (hybrid execution model) |
  | R1~R11 body | No change |

- 🟡 **New OD-MS-011 · OD-MS-012 · OD-SEC-008 in `.kiro/specs/_common/operator-decisions.md`**

  | ID | Value |
  | --- | --- |
  | OD-MS-011 | port-interest-crawler runtime = Hybrid · KRX GUI = Windows EC2 worker · non-GUI = keep ECS Fargate Task candidate · preprocessor = keep ECS Fargate Task · 🟠 **잠정** |
  | OD-MS-012 | first-stage KRX GUI crawler operating mode = manual wrapper execution · 🟠 **잠정** |
  | OD-SEC-008 | KRX login credentials = Secrets Manager `/portfolio/{env}/krx/crawler-login` JSON `username`/`password` · 🟠 **잠정** |
  | Decision Summary | total 62 → 65 · 잠정 17 → 20 |
  | Change Log | 2026-06-12 entry |
  | Existing decision values (OD-NET-004, etc.) | No body change |

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | Change |
  | --- | --- |
  | R-AUTO-005 | reinforced mitigation/detection/rollback (EC2 worker first-stage operation available · monitor wrapper-log patterns · immediate recovery through wrapper rerun) · Status `Open` → 🟢 **Mitigated** |
  | New R-AUTO-006 | EC2 worker download-path mismatch · junction adjustment · Status 🟢 **Mitigated** |
  | New R-SEC-005 | worker startup failure if KRX login secret-read permission is missing · IAM Role inline policy + Resource·Action wildcard 0 · Status 🟢 **Mitigated** |
  | New R-DOCS-002 | risk of plaintext password exposure inside EC2 worker · length checks + grep checks · Status 🟢 **Mitigated** |
  | New R-AUTO-007 | wrapper success does not guarantee actual DB-load success · follow-up to automatically add DB-validation output inside wrapper · Status 🟠 **Open** |

- 🟢 **`.kiro/specs/_common/followups-overview.md`**

  | Category | Content |
  | --- | --- |
  | 2026-06-12 follow-up note | 08 spec reached first-stage operational availability for Hybrid execution model (KRX program/shortsell collection on EC2 worker succeeded + download-path junction + RDS/KRX Secret injection + wrapper) |
  | Classification decision | KRX GUI = Windows EC2 worker · non-GUI = keep ECS Fargate Task candidate · preprocessor = keep ECS Fargate Task |
  | Separated follow-ups (8 items) | SSM RunCommand · EventBridge Scheduler · Step Functions hybrid orchestration · CloudWatch Logs Agent · automatically add DB validation output inside wrapper · reorganize non-GUI crawler scope · EC2 worker stop procedure · long-term headless KRX collection refactor |
  | Execution-order table | No change |

### Security

| Item | Result |
| --- | --- |
| Plaintext records of secret · token · password · KRX login · KIS · account · RDS endpoint · account-id · ARN · image digest · IAM access key · instance-id | 0 |
| Placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| KRX login ID / password | recorded only as "injected from Secrets Manager" |
| password rotate | outside scope for this date |
| Quoted body of ps1 / py / wrapper files directly created by operator inside EC2 | 0 (facts recorded in operation-notes) |

## 2026-06-10

### Added

- 🟢 **New `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md`**

  | Item | Value |
  | --- | --- |
  | ECR repository | 2 created |
  | Dockerfile · requirements.txt | new for port-interest-preprocessor · port-interest-crawler |
  | Local build | `paper-20260610` · `paper-latest` |
  | ECR push | complete |
  | ECS Cluster | `portfolio-paper-cluster` |
  | Task Execution Role · Task Role | 2 types |
  | CloudWatch Log Group | 2 · retention 14 days |
  | Secrets Manager | new `/portfolio/paper/rds/preprocessor-app` JSON multi-key |
  | preprocessor Task Definition | family `portfolio-paper-interest-preprocessor` · awsvpc · cpu 512 · memory 1024 · image `paper-20260610` |
  | preprocessor RunTask first validation | lastStatus `STOPPED` · exitCode 0 · `PREPROCESSOR PIPELINE END` |
  | Initial failure | missing `host` in Secrets JSON → attempted Unix socket |
  | Second failure | insufficient permissions on 2 remaining public-schema sequences |
  | crawler runtime validation | carried over |
  | Plaintext records | 0 secret · endpoint · account-id · ARN · image digest |

### Changed

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md`**

  | Item | Value |
  | --- | --- |
  | Updated as complete | ECR repository creation · Dockerfile · requirements · local build · ECR push · ECS Cluster · Role · Log Group · Secrets Manager · Task Definition · Preprocessor RunTask · CloudWatch Logs · exit code 0 |
  | crawler build / push | marked complete |
  | task 30 (crawler runtime outbound validation) · task 31 (crawler runtime reachability validation) | marked carry-over |
  | Failure causes reflected | task 28 (missing `host` in Secrets JSON) · task 26 (insufficient permission on 2 remaining public-schema sequences) |
  | Safety constraints for this spec (35~40) | remain 0 based on Kiro work in this spec · direct operator work referenced in operation-notes |

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | Change |
  | --- | --- |
  | R-DATA-005 | reinforced mitigation/detection · remaining public-schema sequence permission-gap case + monitor ECS Task event pattern `permission denied for sequence` |
  | R-DOCS-001 | reinforced detection · operational check to prevent secret values appearing in work chat · command output · console captures · CloudWatch Logs |
  | New R-DATA-006 | missing `host` key in Secrets Manager JSON multi-key causes ECS Task to attempt Unix socket `/var/run/postgresql/.s.PGSQL.5432` |
  | New R-AUTO-005 | entering operation without validating crawler Selenium · Chromium · chromedriver runtime · KRX · Naver · yfinance outbound reachability |
  | Principle | new items do not force password rotate as mitigation · no secret-value exposure · runtime validation separated as follow-up |

- 🟢 **`.kiro/specs/_common/followups-overview.md`**

  | Item | Value |
  | --- | --- |
  | 2026-06-10 follow-up note | recorded one successful 08 preprocessor ECS Task execution (lastStatus `STOPPED` · exitCode 0) |
  | 08 spec section | added 2026-06-10 first application result (completed scope / out of scope / follow-up spec handoff) |
  | Carry-over items | crawler Task Definition / RunTask runtime validation · cleanup of remaining public-schema sequences |

- 🟡 **`.kiro/specs/_common/operator-decisions.md`**

  | Item | Value |
  | --- | --- |
  | Change Log OD-NET-004 note | crawler / preprocessor outbound = public subnet + assignPublicIp · 2026-06-10 preprocessor RunTask first validation |
  | Status | remains 🟠 **잠정** (crawler runtime validation carried over) |
  | Decision values (options·selected value·cost·risk·follow-up spec impact) | No change |

### Security

| Item | Result |
| --- | --- |
| Plaintext records of secret · token · password · KIS · account · webhook URL · RDS endpoint · account-id · ARN · image digest · IAM access key | 0 |
| Placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| password rotate | outside documentation scope for this date |
| Secret-value exposure principle | remains identical for follow-up work (aligned with R-DOCS-001) |

## 2026-06-09

### Added

- 🟢 **2 new auxiliary documents for 02 spec**

  | File | Role |
  | --- | --- |
  | `.kiro/specs/02-aws-network-and-rds/README.md` | one-page operator summary · current status · next work · completed AWS resources · related-document links · automatic-update criteria · decision values · no RDS endpoint hostname · secret value recorded |
  | `.kiro/specs/02-aws-network-and-rds/db-roles-and-grants.md` | DB Role · permission-separation aid · 7 app-role design · permission matrix · search_path strategy · GRANT · ALTER ROLE · DEFAULT PRIVILEGES SQL draft · validation SQL · rollback procedure · explicitly no legacy grants · marketconnector_app execution R-only reduction |

### Changed

- 🟢 **Added 2026-06-09 section to `.kiro/specs/02-aws-network-and-rds/operation-notes.md`**

  | Item | Value |
  | --- | --- |
  | RDS Restore path | local → S3 → EC2 → RDS |
  | dump size | 422,334,494 bytes · TOC 812 |
  | dump source | PostgreSQL 18.1 |
  | RDS version change | recreated 16.14 → 18.4 |
  | Initial error | `role "postgres" does not exist` |
  | Re-run | `--no-owner --no-privileges` · consistency diff 0 |
  | First DB Role / permission-separation application | 7 app roles · introduced `portfolio_owner` · transferred ownership of 9 domain schemas · existing table/sequence/index owners remain `portfolio_admin` · `REASSIGN OWNED` not executed · GRANT matrix · DEFAULT PRIVILEGES · search_path applied · connection validation succeeded for 3 roles |

- 🟢 **`.kiro/specs/02-aws-network-and-rds/validation-checklist.md`**

  | Item | Value |
  | --- | --- |
  | 5 items in §6 DB/schema/role preparation | promoted `[운영자 확인 필요]` → `[O]` |
  | §11 DB Role permission-matrix Validation | new · 13 labels · `[O]` 11 · `[운영자 확인 필요]` 2 |
  | RDS Restore validation (schema · table · index · sequence · FK · trigger · row-count match) | organized as `[O]` |

- 🟢 **`.kiro/specs/02-aws-network-and-rds/runbook.md`**

  | Item | Value |
  | --- | --- |
  | Reinforced section | "RDS Restore Runner & DB Role / permission-application lessons" |
  | Content | avoid major-version mismatch · private RDS through EC2 / SSM · if dump-role mismatch use `--no-owner --no-privileges` · separate schema-only ownership transfer + REASSIGN |
  | Decision values · existing Step numbers | No change |

- 🟢 **4 new risks in `.kiro/specs/_common/risk-register.md`**

  | ID | Content |
  | --- | --- |
  | R-DATA-003 | restore failure due to PostgreSQL major-version mismatch |
  | R-DATA-004 | restore failure due to mismatch between dump owner role and RDS role |
  | R-NET-004 | private RDS cannot be accessed directly · dependency on EC2 / SSM restore runner |
  | R-DATA-005 | need to validate view / execution / marketconnector permission boundaries after applying minimum app-role permissions |

- 🟢 **New OD-DB-007 ~ OD-DB-010 in `.kiro/specs/_common/operator-decisions.md`**

  | ID | Content |
  | --- | --- |
  | OD-DB-007 | legacy schema not granted to any app role |
  | OD-DB-008 | marketconnector_app execution R-only |
  | OD-DB-009 | view_app execution R-only (write to be reconsidered in follow-up spec) · OD-DB-005 separated into OD-DB-009 · body retained · Status retained · no duplicate row created |
  | OD-DB-010 | `REASSIGN OWNED BY portfolio_admin TO portfolio_owner` not executed in first application |

- 🟢 **`.kiro/specs/_common/followups-overview.md`**

  | Item | Value |
  | --- | --- |
  | Execution order | added 2026-06-10 follow-up note |
  | `03-marketconnector-ec2` item | carried-over MarketConnector baseline-porting work · documented dependency on baseline ECS porting |
  | Link | RDS first-application result to use as input for 03 spec |

- 🔵 **Out of scope · no execution**

  | Item | Value |
  | --- | --- |
  | Code / AWS resource / SQL execution changes | None |
  | New EC2 creation · existing RDS deletion · recreation · SQL application | performed directly by operator · this change only records results |
  | Changes to 8 MS code · README · AGENTS.md · CHANGELOG · docs · worklog | None |

- 🟢 **Earlier same-date changes (folder move + link correction)**

  | Item | Value |
  | --- | --- |
  | Moved 6 root common docs | `operator-decisions.md` · `ms-aws-service-decision-matrix.md` · `cost-simulation.md` · `followups-overview.md` · `aws-resource-glossary.md` · `risk-register.md` → `.kiro/specs/_common/` · no filename/body changes |
  | Archive move | `.kiro/specs/note-aws-landscape-2021-vs-2026.md` → `.kiro/specs/_archive/` · no filename/body changes |
  | `.kiro/README.md` update | folder-structure example · root common-doc links changed to `_common/` · `_archive/` |
  | `.kiro/AGENTS.md` update | root reference-document section paths · no semantic change to work rules |
  | Link correction | `.kiro/CHANGELOG.md` · `.kiro/WORKLOG.md` · `.kiro/docs/kiro-readonly-validator-iam.md` · `01-aws-migration-foundation/*.md` · `02-aws-network-and-rds/*.md` · `../X.md` → `../_common/X.md` · `specs/X.md` → `specs/_common/X.md` |

### Security

| Item | Result |
| --- | --- |
| Plaintext records of secret · token · password · app key · app secret · account number · webhook URL · RDS endpoint hostname · account-id · access key id | 0 |
| Placeholder | `[REDACTED]` only |

## 2026-06-06

### Added

- 🟢 **4 first-application auxiliary documents for 02 spec + risk·glossary reinforcement**

  | File | Role |
  | --- | --- |
  | `.kiro/specs/_common/risk-register.md` | single cumulative table for AWS Migration operational·security·cost risks · registered 12 risks R-NET-001 ~ R-COST-002 · same-date follow-up added R-SEC-002 (Root security) · R-SEC-003 (loss of portadmin credentials) |
  | `.kiro/specs/01-aws-migration-foundation/traceability-matrix.md` | 01 spec Requirement → Design → Task → Decision mapping |
  | `.kiro/specs/02-aws-network-and-rds/runbook.md` | operator execution procedure · first version = 18 Steps (Region ~ Rollback) · same-date follow-up added new Step 0 `IAM 관리자 사용자 portadmin 생성` → reorganized to total 19 Steps (Step 0 ~ Step 19) |
  | `.kiro/specs/02-aws-network-and-rds/validation-checklist.md` | checkbox for 02 spec runbook pass/fail · 10 sections (Pre-flight · Network · SG · Endpoint · RDS · DB · Cutover · Cost · Docs · Rollback) |
  | `.kiro/specs/02-aws-network-and-rds/traceability-matrix.md` | 02 spec Requirement → Design → Task → Validation → Decision mapping + Risk mapping |
  | `.kiro/specs/_common/aws-resource-glossary.md` | new IAM User term · reflects first substantive use of IAM administrator-user concept such as portadmin in 02 runbook |

### Changed

- 🟢 **`.kiro/README.md`**

  | Item | Value |
  | --- | --- |
  | Guidance for new auxiliary-document roles | runbook · validation-checklist · traceability-matrix · risk-register |
  | Folder structure | Updated |

- 🟢 **`.kiro/specs/02-aws-network-and-rds/runbook.md`**

  | Item | Value |
  | --- | --- |
  | Execution labels | added `[실행]` · `[확인]` · `[준비]` · `[복구]` to every Step title |
  | Label legend | added at top of body |
  | Step renumbering | after adding Step 0 (IAM portadmin), previous Step 0 ~ 18 → renumbered to Step 1 ~ 19 |
  | cross-reference | all `Step N 완료` · `Step N 에서 다시 결정`, etc. incremented by +1 |
  | Decision values · Console click paths · input values · validation · rollback order | No change |

- 🟢 **`.kiro/specs/02-aws-network-and-rds/validation-checklist.md` · `traceability-matrix.md`**

  | File | Change |
  | --- | --- |
  | validation-checklist.md | aligned to runbook renumbering: `Step 18 rollback` → `Step 19 rollback` |
  | traceability-matrix.md | incremented all runbook Step references in mapping tables (Requirement · Acceptance Criteria reinforcement · Risk mapping) by +1 |

### Security

| Item | Result |
| --- | --- |
| Plaintext records of secret · token · password · app key · app secret · account number · webhook URL | 0 |
| Placeholder | `[REDACTED]` only |
| portadmin password · MFA serial · backup codes · Root MFA serial | <span style="color:#D1242F">**Prohibited**</span> in every document in this workspace · stored separately by operator in external secure location · docs use `[REDACTED]` |

## 2026-06-05

### Added

- Added `.kiro/README.md` to document the purpose and document structure of the AWS Migration spec workspace.
- Added `.kiro/CHANGELOG.md` to establish the standard for managing Kiro spec-document change history.
- Added `.kiro/WORKLOG.md` to accumulate brief work logs in a single file.

### Changed

- Added README / CHANGELOG / WORKLOG management rules to `.kiro/AGENTS.md`. However, the meaning of existing work rules (scope, single-source-of-truth document, per-MS AGENTS.md references, spec authoring, security, execution rules) was not changed.
- Restructured `.kiro/specs/_common/operator-decisions.md` for operator readability. · Actual decision values (Decision ID, options, selected value, cost impact, operational risk, follow-up spec impact) were not changed, and only Status display was improved to show Korean/color labels together (🟢 확정 · 🟡 잠정 · 🔴 미정 · 🔵 보류).
  Added Status Legend, Decision Summary, At a Glance, category-level detailed decision tables, Open · Tentative · Deferred decision collections, Decision Update Rules, and Change Log sections.

### Security

- Reconfirmed the principle that secret, token, password, app key, app secret, account number, and webhook URL must never be recorded in this workspace documents and must be represented only as `[REDACTED]`.
