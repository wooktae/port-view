# Tasks — 03-marketconnector-ec2

These tasks are the minimal checklist that decomposes the [`./design.md`](./design.md) / [`./requirements.md`](./requirements.md) decisions into progress units. The operator performs the actual AWS work and EC2 shell work directly, and Kiro handles only document / procedure / validation-item organization.

## 1. Preliminary Status Check

- [ ] 1. Confirm EC2 / SG / Instance Profile attach status (§2)
- [ ] 2. Confirm the result of Python 3.9.25 / 1 venv / 5 dependencies installed (§4)
- [ ] 3. Confirm PostgreSQL client / pg_restore major 18 / full 18.4 (§5)
- [ ] 4. Confirm the registration status of the 06 spec's 4 Secrets + 6 SSM Parameters (§8)

## 2. Organize the env Injection Procedure

- [x] 5. Organize the temporary export script (`/tmp/inject-env.sh`) pattern (§8.3)
  - [2026-06-17] The v5 pattern (JSON SecretString internal key extraction + simultaneous `KIS_*` alias export) reinforcement complete.
    - Consistent with operation-notes 2026-06-17 §1 / runbook §2 / design §8.2.2
  - [2026-06-22 reinforcement] Handling `/tmp` volatility after EC2 stop / start — reinforcement complete with the pattern where Step 1 / Step 12 / Step 13 / Step 17 of the Daily AWS Paper Wrapper regenerate `/tmp/inject-env.sh` at step execution time via the `daily-aws-paper.functions.ps1` common MarketConnector env bootstrap function.
    - OD-MS-027 new / R-AUTO-021 new mitigation first demonstration
    - Consistent with operation-notes 2026-06-22 §1 / runbook §2.1
- [x] 6. Confirm the environment-variable mapping (KIS / RDS / CONNECTOR_*) table (§8.2) — [2026-06-17] Completed with the `APP_*` compatibility key + `KIS_*` alias simultaneous export policy (consistent with design §8.2.1). [2026-06-22 reinforcement] The same mapping is maintained in the Daily wrapper bootstrap function / 0 regressions of the 0-secret-value-plaintext-output policy
- [ ] 7. The normal operating mode switch (systemd / startup script) is separated into a follow-up task / separate phase (§7, §8.3) — [2026-06-22 reinforcement] While the wrapper bootstrap function acts as the single source of truth, the formal systemd unit + `EnvironmentFile` registration remains the responsibility of follow-up tasks (26 / 27) as-is / re-reviewed at the Step Functions switch point (consistent with followups-overview 2026-06-22 §1)

## 3. Organize the Query-Type Smoke Test

- [ ] 8. Organize the RDS connection procedure on the `marketconnector_app` basis (§6)
- [x] 9. Organize the query-type execution procedure of `connector_balance.py` / `connector_order_check.py` (§7.3, §10) — [2026-06-17] SSM RunCommand single re-validation complete (consistent with operation-notes 2026-06-17 §1 / §2 / runbook §4.1 / §4.2)
- [ ] 10. Organize the Flask internal smoke test (query-type endpoint) procedure (§7.3, §10)
- [ ] 11. Validation of the operable candidate entrypoints (`connector_quote_realtime.py` / `connector_quote_closed.py` / `connector_view_service.py`) is separated into a follow-up phase (§7.3, §7.4)
- [x] 12. Specify the 0-call policy for new order / buy / sell / cancel / modify entrypoints (§7.3, R14) — [2026-06-17] Re-validated 0 new order / `--execute` / aws-live work on this date (consistent with operation-notes 2026-06-17 §3)

## 4. Organize the Instance Role / Access Key Non-Use Validation

- [ ] 13. Organize the credential-normal-state inspection items (`aws sts get-caller-identity` / `aws configure list`) (§9.4)
- [ ] 14. Organize the inspection items for 0 access keys in EC2 access key files / environment variables / dotfiles / systemd (§9.5)
- [ ] 15. Organize the validation items for the 03 auxiliary permissions (`AmazonSSMManagedInstanceCore` + CloudWatchLogsWrite recommended option = pre-create the log group) (§9.2)

## 5. Follow-Up Phase Deliverable Creation Plan

- [ ] 16. Create runbook.md — 8 stages + [실행]/[확인]/[준비]/[복구] labels (§11.1)
- [ ] 17. Create validation-checklist.md — 7 inspection areas + 4 labels (§11.2, §11.3)
- [ ] 18. Create operation-notes.md — per-date accumulation + 6/9 / 6/10 facts + 8 results + IAM change template (§3, §10, §11.4)

## 6. Organize _common Update Candidates

- [ ] 19. Hand off the operator-decisions.md update candidates (OD-SEC-005 / OD-SEC-006 tentative→confirmed candidates, `AmazonSSMManagedInstanceCore` usage policy new candidate) (§12.1)
- [ ] 20. Hand off the risk-register.md update candidates (5 items R-DATA / R-CAP / R-BROKER / R-SEC / R-AUTO) (§12.2)
- [ ] 21. Hand off the followups-overview.md update candidates (03 first application environment / first scope / out-of-scope / 04·05·08·09·10 handoff) (§12.3)

## 7. Completion Criteria

- [ ] 22. All 4 + 2 = 6 deliverables requirements.md / design.md / tasks.md / runbook.md / validation-checklist.md / operation-notes.md exist
- [ ] 23. All 8 validation results accumulated as [O] or an operator-direct-confirmation result
- [ ] 24. No actual secret value / account number / RDS endpoint / account-id / actual ARN / IAM access key id / instance-id / EIP recorded anywhere in this spec's deliverables (R14)
- [ ] 25. A state where the 04 / 05 / 08 / 09 / 10 specs can receive this spec's §13 mapping as input at entry (§13)

## 8. New Follow-Up Tasks (2026-06-17 reinforcement)

- [ ] 26. When switching to systemd / startup script normal operating mode, reflect the v5 env mapping pattern (JSON SecretString internal key extraction + `APP_*` compatibility key + `KIS_*` alias simultaneous export) — follow-up to task 7
- [ ] 27. Judge whether to promote `/tmp/inject-env.sh` to an operational script (maintain the permission 700 / memory export only / no-secret-plaintext-storage policy / compare the formal systemd EnvironmentFile vs runtime AWS CLI call pattern)
- [ ] 28. Decide unification / keeping both of the `APP_*` compatibility key vs `KIS_*` alias (currently safe with simultaneous export / review the unification candidate at the follow-up code cleanup point)

## 9. New Follow-Up Tasks (2026-06-17 17-step E2E reinforcement)

This section is the follow-up tasks at the Daily AWS 17-step E2E completion point. Consistent with this note / operation-notes.md 2026-06-17 (Daily AWS 17-step E2E complete) §1 ~ §5. The fact of the `connector_strategy_order_execute.py` / `connector_order_check.py` changes the operator directly patched / formally deployed on this date is recorded only as a fact in operation-notes / 0 full-body quotations.

- [ ] 29. Add test cases for the `connector_order_check.py` summary fallback guard.
  - Behavior when active candidates are 0 / 1 / 2 / multiple
  - `output1 empty` + `output2 aggregate summary` input value / `output1` normal row input value
  - 0-event · fill · status-change validation / KIS API response fixture
  - Basis: R-AUTO-018 mitigation reinforcement follow-up
- [ ] 30. Review the `source_daily_signal_id null` correction in the `result_payload` of `connector_strategy_order_execute.py` (OPTIONAL_COLUMNS or fallback pattern / consistent with R-AUTO-009 [2026-06-17 reinforcement]) — paper environment-only weekday / safe-data additional validation follow-up
- [ ] 31. Organize the `connector_balance_snapshot` freshness validation SQL.
  - Distinguish the meaning of `as_of_ts` / `max(created_at)` rather than `created_at` alone.
  - `as_of_ts` = the broker-response reference time / `created_at` = the row insert time.
  - Organize the candidate separated inspection SQL for `connector_balance_snapshot` / `connector_position_snapshot` / `connector_api_call_log`.
  - Basis: consistent with R-DOCS-001 [2026-06-17 reinforcement] / [2026-06-17 reinforcement (17-step E2E)].

## Task Dependency Graph (simple)

```text
1~4 (preliminary status)
  └─> 5~7 (env injection)
        └─> 8~12 (query-type smoke test)
              └─> 13~15 (Access Key non-use validation)
                    └─> 16~18 (follow-up phase deliverables)
                          └─> 19~21 (_common update candidates)
                                └─> 22~25 (completion criteria)
                                      └─> 26~28 (2026-06-17 v5 env mapping follow-up)
                                            └─> 29~31 (2026-06-17 17-step E2E reinforcement)
```


## 10. New Follow-Up Tasks (2026-06-22 reinforcement — Daily AWS Paper 1~17 second live full run + first actual SELL E2E)

This section is the follow-up tasks from the 2026-06-22 Daily AWS Paper Wrapper 1 ~ 17 second live operational execution result. See detailed results:

- [`./operation-notes.md`](./operation-notes.md) 2026-06-22 §1 ~ §6
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md) Change Log 2026-06-22 (OD-MS-027 / OD-DB-011 new)
- [`../_common/risk-register.md`](../_common/risk-register.md) R-AUTO-021 / R-DATA-013 new + R-DATA-005 [2026-06-22 reinforcement]

- [x] 32. Reflection of the env bootstrap regeneration pattern for the MarketConnector Daily wrapper SSM steps (Step 1 / Step 12 / Step 13 / Step 17) complete.
  - Added the MarketConnector env bootstrap function inside `daily-aws-paper.functions.ps1`.
  - JSON SecretString internal key extraction + `APP_*` / `KIS_*` alias simultaneous export + chmod 700 + no secret value output / called right before Step entry.
  - Passed PowerShell parser validation + first demonstration of Step 1 re-run pass (OD-MS-027 new / R-AUTO-021 new mitigation alignment).
  - Reference: [`./operation-notes.md`](./operation-notes.md) 2026-06-22 §1
- [x] 33. Handling `/tmp` volatility after EC2 stop / start complete.
  - Since the wrapper bootstrap function regenerates `/tmp/inject-env.sh` at step execution time, Step 1 / 12 / 13 / 17 can be entered even right after EC2 stop / start.
  - The operator manual fallback procedure is organized in [`./runbook.md`](./runbook.md) §2.1.
  - Reference: [`./operation-notes.md`](./operation-notes.md) 2026-06-22 §1 / [`./runbook.md`](./runbook.md) §2 / §2.1
- [x] 34. Step 12 Paper SELL order submission validation complete.
  - `088350` 한화생명 244 shares MARKET 1 order / `SELL_HARD_STOP` reason / `-AllowPaperOrderExecute` explicit execution.
  - `execution_order id 37` SUBMITTED / `connector_order_request id 46` ACCEPTED → FILLED.
  - `connector_fill id 34` created (fill_qty 244 / fill_price 5,075.8607 / fill_amount 1,238,510.01).
  - 0 broker_order_no · broker_branch_code plaintext records in this spec's deliverables.
  - 03 spec-responsibility Steps (1 / 12 / 13 / 17) passed the Daily wrapper second live full run (R-AUTO-019 mitigation alignment).
  - Reference: [`./operation-notes.md`](./operation-notes.md) 2026-06-22 §2 ~ §4
- [x] 35. Step 17 BALANCE_REFRESH result consistency validation complete.
  - `connector.connector_position_snapshot` latest `created_at 2026-06-22 00:50:50 UTC`.
  - 5 held tickers (`003490` 58 shares / `004990` 69 shares / `023530` 8 shares / `042660` 11 shares / `282330` 17 shares).
  - Confirmed `088350` 한화생명 removed from the balance snapshot.
  - 0 R-DATA-011 mitigation regressions (the `legacy.holdings` permission of `marketconnector_app` / database search_path correction kept as-is).
  - Reference: [`./operation-notes.md`](./operation-notes.md) 2026-06-22 §4
- [ ] 36. The formal systemd / startup script normal operating mode switch remains a follow-up.
  - While the wrapper bootstrap function acts as the single source of truth, task 7 / task 26 / task 27 remain the follow-up responsibility.
  - Re-reviewed together with the formal systemd unit + `EnvironmentFile` registration at the Step Functions switch (consistent with followups-overview 2026-06-22 §1).
- [ ] 37. Reinforce the pre-Step-12-order DB preflight view or wrapper summary output.
  - `execution_plan` plan_status / ready · blocked distribution
  - SELL target ticker code · quantity · reason
  - Expose the `position_state` quantity match validation as a wrapper run summary or a separate preflight view
  - Basis: R-AUTO-019 mitigation reinforcement follow-up / combined with the 05 spec View Daily Batch screen integration / consistent with followups-overview 2026-06-22 §3
- [ ] 38. Reinforce the automatic summary output of Step 13 connector_fill / order_request / execution_order status.
  - Automatically record the `active_order_count` / `single_check_success_count` / `fill_qty` / `fill_price` / `fill_amount` labels in the wrapper run summary.
  - Basis: R-AUTO-018 / R-AUTO-020 detection reinforcement / combined with the 2026-06-18 second follow-up memo / consistent with followups-overview 2026-06-22 §4
