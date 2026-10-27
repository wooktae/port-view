# Risk Register — AWS Migration

## Purpose

This document is the single source of truth managing the operational·security·data·cost risks of the PORT-STRATEGY-AI AWS Migration.

A Risk ID, once assigned, is not reused or renumbered. Each Risk manages the items below in an `Item / Value` 2-column table.

| Item | Value |
| --- | --- |
| Status | Open · Mitigated · Accepted · Closed |
| Impact | High · Medium · Low |
| Likelihood | High · Medium · Low |
| Risk | A problem that can occur |
| Response | The measure currently applied or to be performed |
| Related spec | Responsible spec number |

Per-date mitigation history, executionName, ARN, commandId, revision, SHA256, DB after-check figures, and raw log are not accumulated in this document.

Detailed execution evidence is managed in each spec's `operation-notes.md`, work history in `WORKLOG.md`, and decision changes in `operator-decisions.md`.

Sensitive information originals are not recorded; only `[REDACTED]`-family placeholders are used.

## Risk Dashboard

### Status summary

| Item | Value |
| --- | --- |
| Total | 76 |
| 🔴·🟠 Open | 29 |
| 🟢 Mitigated | 45 |
| 🔵 Accepted | 2 |
| ⚫ Closed | 0 |
| Open High | 19 |

### Status by area

| Item | Value |
| --- | --- |
| Automation | 38 |
| Data | 17 |
| Security | 7 |
| Broker | 5 |
| Network | 4 |
| Cost | 3 |
| Docs | 2 |

### Impact status

| Item | Value |
| --- | --- |
| High | 46 |
| Medium | 28 |
| Low | 2 |

### 🔴 Open High — priority action

| Item | Value |
| --- | --- |
| R-NET-001 | Public subnet ECS Task SG inbound opened incorrectly, allowing external exposure |
| R-NET-003 | Missing·inconsistent AWS API access path or Endpoint set needed by the current network structure |
| R-SEC-001 | RDS SG inbound broad CIDR · RDS externally exposed |
| R-DATA-001 | RDS role search_path unset · wrong schema queried |
| R-DATA-002 | pg_dump / pg_restore cutover consistency broken · sequence · default privilege missing |
| R-BROKER-001 | broker registered IP mismatch from EIP detach / EC2 replacement · broker call failure |
| R-BROKER-002 | single access_token multi-renewal conflict (EC2 multiplication · Fargate multiple Tasks) |
| R-AUTO-001 | Step Functions Retry wrongly enabled · BUY / SELL / fill sync / position duplicate order |
| R-AUTO-002 | aws-live automatic BUY/SELL enabled early without approval · risk of wrong orders on the real account |
| R-DOCS-001 | risk of plaintext recording of secret / token / account number / webhook in spec deliverables |
| R-SEC-002 | Root credential (password / MFA) lost · exposed · emergency recovery impossible · external breach risk |
| R-SEC-003 | portadmin credential lost · AWS Console / IAM work halted |
| R-SEC-004 | Flask debug mode, when entering the operational environment, externally exposes stack trace · env · internal paths |
| R-DATA-007 | local PostgreSQL ↔ AWS Paper RDS merge · duplicate orders · wrong fills · source-of-truth collapse |
| R-AUTO-014 | single Task Definition + command override mismapping · risk of running the wrong entrypoint |
| R-AUTO-016 | Windows worker Administrator interactive session absent · KRX GUI collection failure risk |
| R-AUTO-030 | 3-stage Intraday Stop Sell auto-ENABLE early entry · risk of broker stop-loss without an approval gate |
| R-SEC-010 | risk of unauthorized access when operating without rotate after an app role DB password plaintext-exposure history |
| R-AUTO-034 | Fargate Task Role states:StartExecution broadly granted · risk of gate default ENABLE regression |

## Risk Inventory

Risks are organized by Area. For items with `Open` status, after the response completes, leave the evidence in the relevant spec `operation-notes.md` and update only the status in this document.

## Network
### R-NET-001

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Medium |
| Risk | Public subnet ECS Task SG inbound opened incorrectly, allowing external exposure |
| Response | Prohibit Task SG inbound 0.0.0.0/0 · allow only outbound-needed domains. |
| Related spec | 02, 08 |

### R-NET-002

| Item | Value |
| --- | --- |
| Status | 🟠 Open |
| Impact | Medium |
| Likelihood | Medium |
| Risk | In the NAT-free structure, placing an external outbound workload in a private subnet incorrectly causes external-call failure |
| Response | Place external outbound workloads in public subnet + assignPublicIp=ENABLED. |
| Related spec | 02, 08 |

### R-NET-003

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Medium |
| Risk | Missing·inconsistent AWS API access path or Endpoint set needed by the current network structure |
| Response | Confirm ECR API / ECR DKR / Secrets Manager / Logs Endpoint |
| Response | Confirm S3 Gateway Endpoint |
| Response | For SSM, confirm the actual operational path between Public outbound or SSM Endpoint |
| Response | Confirm Interface Endpoint SG inbound 443 |
| Related spec | 02, 06, 07 |

### R-NET-004

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | RDS Public No · local PC cannot connect directly · restore / validation SQL cannot be performed |
| Response | Use a same-VPC EC2 as the restore runner · use an SSM Port Forwarding tunnel. |
| Related spec | 02, 03, 10 |

## Security
### R-SEC-001

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Low |
| Risk | RDS SG inbound broad CIDR · RDS externally exposed |
| Response | sg-rds-postgres inbound allows only other-SG references · publicly accessible false. |
| Related spec | 02 |

### R-SEC-002

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Low |
| Risk | Root credential (password / MFA) lost · exposed · emergency recovery impossible · external breach risk |
| Response | Root MFA · back up recovery codes safely · use portadmin for daily work. |
| Related spec | 02 |

### R-SEC-003

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Low |
| Risk | portadmin credential lost · AWS Console / IAM work halted |
| Response | portadmin MFA · back up recovery codes safely · keep the reset path via Root. |
| Related spec | 02, 06 |

### R-SEC-004

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Low |
| Risk | Flask debug mode, when entering the operational environment, externally exposes stack trace · env · internal paths |
| Response | Force CONNECTOR_DEBUG false in operation · unset immediately after validation · manage systemd env. |
| Related spec | 03, 05 |

### R-SEC-005

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | worker IAM Role missing KRX login secret read permission · AccessDenied · startup failure |
| Response | Grant secret ARN-limited GetSecretValue in the worker IAM Role inline policy. |
| Related spec | 06, 08 |

### R-SEC-009

| Item | Value |
| --- | --- |
| Status | 🟠 Open |
| Impact | Medium |
| Likelihood | Medium |
| Risk | Windows worker Autologon credential · risk of simultaneous KRX worker + EC2 OS breach from RDP exposure |
| Response | Autologon limited to the paper worker · RDP limited to operator IP · SSM first. |
| Related spec | 08, 05, 10 |

### R-SEC-010

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Medium |
| Risk | risk of unauthorized access when operating without rotate after an app role DB password plaintext-exposure history |
| Response | Rotate the exposed app role password and re-validate Secrets Manager·loader·environment-variable injection. |
| Related spec | 02, 03, 04, 05, 06, 08, 09, 10 |

## Data
### R-DATA-001

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Medium |
| Risk | RDS role search_path unset · wrong schema queried |
| Response | Apply ALTER ROLE ... SET search_path to the 7 roles · validate per-role SHOW search_path. |
| Related spec | 02 |

### R-DATA-002

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Medium |
| Risk | pg_dump / pg_restore cutover consistency broken · sequence · default privilege missing |
| Response | Fix dump options + post-restore validation SQL · compare row count local-dev ±0. |
| Related spec | 02, 10 |

### R-DATA-003

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | dump source · RDS engine major version mismatch · pg_restore failure |
| Response | Keep client / RDS at a major version at least the dump source · prohibit downgrade. |
| Related spec | 02, 03, 10 |

### R-DATA-004

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | restore failure due to owner role absence in the dump · inaccurate permission setup |
| Response | pg_restore --no-owner --no-privileges + post-hoc GRANT reconfiguration. |
| Related spec | 02, 10 |

### R-DATA-005

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | On applying DB Role least privilege, per-scenario permission shortage · overage possible |
| Response | Periodic check of 02 db-roles-and-grants §5 validation SQL · accumulate individual GRANT corrections. |
| Related spec | 02, 04, 05, 06, 08 |

### R-DATA-006

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | Secrets Manager JSON multi-key missing · ECS Task connects to wrong DB host, fails |
| Response | Include the 5 JSON keys · pre-check Task Definition secrets 1:1 mapping. |
| Related spec | 06, 08, 04, 05, 09 |

### R-DATA-007

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Medium |
| Risk | local PostgreSQL ↔ AWS Paper RDS merge · duplicate orders · wrong fills · source-of-truth collapse |
| Response | Codify OD-ENV-006/007/008 · PORT_ENVIRONMENT + PORT_DB_TARGET guard. |
| Related spec | 02, 03, 04, 05, 10 |

### R-DATA-008

| Item | Value |
| --- | --- |
| Status | 🟠 Open |
| Impact | Medium |
| Likelihood | Medium |
| Risk | The 3 Research-internal adapters do not follow port_strategy_common contract changes · result mismatch |
| Response | py_compile / import smoke + Daily Decision vs Research sample comparison. |
| Related spec | 09, 07, 10 |

### R-DATA-009

| Item | Value |
| --- | --- |
| Status | 🟠 Open |
| Impact | Medium |
| Likelihood | Medium |
| Risk | risk of the smoke-pass expression being mistaken for raw-freshness completion, entering stale-data-based operation |
| Response | Unify the expression as "partial complete" · check MAX(trade_date) SQL for the 7 non-GUI raw types. |
| Related spec | 08, 04, 09 |

### R-DATA-010

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | non-GUI raw freshness not recovered · risk of stale raw propagation to preprocessor · Research · Decision |
| Response | Re-run preprocessor after raw freshness recovery · validate new feature date. |
| Related spec | 08, 04, 09 |

### R-DATA-011

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | marketconnector_app legacy schema USAGE/DML/search_path missing · BALANCE_REFRESH failure |
| Response | legacy schema USAGE/DML + search_path correction + default privileges update. |
| Related spec | 02, 03, 06 |

### R-DATA-012

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | additional-buy case unique-constraint conflict · position_state update failure · consistency risk |
| Response | execution_sync_buy_position INSERT/UPDATE merge patch. |
| Related spec | 04, 10 |

### R-DATA-013

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | execution_app missing decision.strategy_daily_position_decision UPDATE permission |
| Response | GRANT USAGE ON SCHEMA decision + limited UPDATE ON TABLE. |
| Related spec | 02, 04 |

### R-DATA-014

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | On intraday Snapshot Refresh failure/delay, risk of entering Evaluate in a stale snapshot state |
| Response | OD-MS-035 4 safeguards · snapshot as_of_ts validation · fail-closed. |
| Related spec | 03, 04, 10 |

### R-DATA-015

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | Recognizing a stop-loss candidate in a sellable_qty 0 / insufficient state · Daily SELL duplicate stop-loss risk |
| Response | Pre-compare sellable_qty at the evaluate stage · avoid when an existing SELL is in progress. |
| Related spec | 03, 04, 10 |

### R-DATA-016

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | Evaluate in a Snapshot current_price null / 0 / stale state · wrong stop-loss judgment |
| Response | Block evaluate entry when current_price is null·0·past·out of range. |
| Related spec | 03, 04, 10 |

### R-DATA-017

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | judging Step2B SUCCESS on KRX worker exit 0 alone risks raw N-business-day lag stale propagation |
| Response | Maintain the Python-result-based non-zero exit and the expected trade date·row count validator. |
| Related spec | 04, 08, 09, 10 |

## Broker
### R-BROKER-001

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Medium |
| Risk | broker registered IP mismatch from EIP detach / EC2 replacement · broker call failure |
| Response | Keep EC2+EIP first priority · keep EIP running attach · detach·attach procedure on replacement. |
| Related spec | 03 |

### R-BROKER-002

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Low |
| Risk | single access_token multi-renewal conflict (EC2 multiplication · Fargate multiple Tasks) |
| Response | Single instance operation · desiredCount=1 with the Fargate option · token backup policy. |
| Related spec | 03 |

### R-BROKER-003

| Item | Value |
| --- | --- |
| Status | 🟠 Open |
| Impact | Medium |
| Likelihood | Medium |
| Risk | balance / order query / view API failure from exceeding the KIS API rate limit |
| Response | Minimize call frequency at the temporary validation stage · token reuse · Restart cap. |
| Related spec | 03 |

### R-BROKER-004

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | Simple re-run after a KIS paper timeout · risk of the same order being duplicate-accepted by the broker |
| Response | Pre-check broker_order_no before re-run · restore REQUESTED control only when absent. |
| Related spec | 03, 04, 10 |

### R-BROKER-005

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | On a real-order test in the no-holdings state, REJECTED · wrong row creation due to no OPEN position |
| Response | output1_count + strategy_position_state OPEN + sellable_qty pre-check (4 items). |
| Related spec | 03, 04, 10 |

## Automation
### R-AUTO-001

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Medium |
| Risk | Step Functions Retry wrongly enabled · BUY / SELL / fill sync / position duplicate order |
| Response | Do not place Retry on order-submission·Fill Sync·Position-change States. Periodically audit per-State Retry and command mapping. |
| Related spec | 04, 10 |

### R-AUTO-002

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Medium |
| Risk | aws-live automatic BUY/SELL enabled early without approval · risk of wrong orders on the real account |
| Response | aws-live BUY/SELL separate Decision + enter after passing paper N business days. |
| Related spec | 04, 05, 10 |

### R-AUTO-003

| Item | Value |
| --- | --- |
| Status | 🟠 Open |
| Impact | Medium |
| Likelihood | Medium |
| Risk | EC2 EBS shortage · venv / logs / access_token / Connector artifact save failure |
| Response | EBS utilization alarm · log rotate · access_token backup policy. |
| Related spec | 03, 04, 05, 08, 09 |

### R-AUTO-004

| Item | Value |
| --- | --- |
| Status | 🟠 Open |
| Impact | Medium |
| Likelihood | Medium |
| Risk | systemd unit / startup not set · Connector · Flask auto-restart failure after EC2 reboot |
| Response | Introduce systemd unit + Restart=on-failure · manual restart note for the temporary stage. |
| Related spec | 03 |

### R-AUTO-005

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | risk of entering operation without crawler ECS Task Selenium / Chromium / KRX outbound validation |
| Response | ECS smoke RunTask pass · KRX GUI separated to the Windows EC2 worker (OD-MS-011/012). |
| Related spec | 08, 02 |

### R-AUTO-006

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | CSV recognition failure from Windows worker download-path assumption vs actual Chrome path mismatch |
| Response | Keep the C:\Users\USER\Downloads · Administrator\Downloads junction + check every run. |
| Related spec | 08 |

### R-AUTO-007

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | EC2 worker wrapper exit 0 does not guarantee actual DB loading success · stale data propagation |
| Response | Auto-invoke the KRX raw DB validation SSM step within the wrapper · fail Step 2 on failure. |
| Related spec | 08 |

### R-AUTO-008

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | risk of KRX GUI login failure when running the wrapper in the SSM SYSTEM SessionId 0 context |
| Response | SSM only triggers schtasks /Run · wrapper execution is an Administrator interactive Task. |
| Related spec | 08 |

### R-AUTO-009

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | risk of entering operation with MarketConnector executor --execute actual REQUESTED handling unvalidated |
| Response | 4 KIS paper BUY SUBMITTED passed via 17-step E2E · additional validation before aws-live entry. |
| Related spec | 03, 04, 05 |

### R-AUTO-010

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | executor --execute guard bypass · risk of flowing to the wrong DB / broker on missing environment variables |
| Response | Force the PORT_ENVIRONMENT=paper + PORT_DB_TARGET=aws-paper combination · deny entry on missing env. |
| Related spec | 03, 04, 06 |

### R-AUTO-011

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | risk of entering operation with the View Daily Batch 12~17 range paper end-to-end unvalidated |
| Response | 12~17 paper passed via 17-step E2E · additional weekday / safe-data validation before live cutover. |
| Related spec | 04, 05 |

### R-AUTO-012

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | On SSM Port Forwarding tunnel disconnection, local paper validation · app role connection immediately impossible |
| Response | Keep the OD-NET-010 standard waypoint · do not close the tunnel window · restart procedure on failure. |
| Related spec | 02, 03, 04 |

### R-AUTO-013

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Low |
| Likelihood | Medium |
| Risk | SQL check over the SSM tunnel impossible due to local PC psql client not installed / PATH not registered |
| Response | Install PostgreSQL 18 client + register PATH or use full path · psycopg2 workaround. |
| Related spec | 02, 03 |

### R-AUTO-014

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Medium |
| Risk | single Task Definition + command override mismapping · risk of running the wrong entrypoint |
| Response | Formally organize the SF state name ↔ command override mapping table · review checklist. |
| Related spec | 04, 05, 10 |

### R-AUTO-015

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | Research heavy backtest / report full execution mistake · Batch cost · vCPU · RDS load |
| Response | smoke / full prefix + attempt=1 · keep SubmitJob manual. |
| Related spec | 09, 10 |

### R-AUTO-016

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Medium |
| Risk | Windows worker Administrator interactive session absent · KRX GUI collection failure risk |
| Response | Autologon bootstrap · query user Active pre-check · monitor wrapper log login. |
| Related spec | 08 |

### R-AUTO-017

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Low |
| Likelihood | Medium |
| Risk | Chrome process residual after wrapper termination · memory leak · next Selenium attach conflict |
| Response | step-02 wrapper does a chrome/chromedriver best-effort reset just before KRX execution. |
| Related spec | 08 |

### R-AUTO-018

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | KIS output1 empty + summary fallback mismapping · order/fill contamination · wrong propagation risk |
| Response | summary fallback guard patch + single direct-only default (OD-MS-025). |
| Related spec | 03, 04 |

### R-AUTO-019

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Low |
| Risk | Step 12 wrapper running unintentionally with -AllowPaperOrderExecute · KIS real-order risk |
| Response | wrapper central + internal double gate · default OFF · record the summary PaperOrder field. |
| Related spec | 03, 04, 10 |

### R-AUTO-020

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | On Step 2 SUCCESS from Scheduled Task trigger success alone, stale raw · KRX login failure undetected |
| Response | chrome reset + Running→Ready wait + Last Result 0 + KRX raw DB validation. |
| Related spec | 08, 04, 05, 09 |

### R-AUTO-021

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | /tmp/inject-env.sh volatility after EC2 stop/start · Daily wrapper Step 1/12/13/17 failure |
| Response | wrapper bootstrap function regenerates env just before Step 1/12/13/17. |
| Related spec | 03, 04, 06 |

### R-AUTO-022

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | On REJECTED rejection_code=40580000, not in the Step 12 default filter · automatic re-submission cut off |
| Response | Step 12 retry-normalizer built-in · restore REQUESTED only when the 6 conditions are satisfied. |
| Related spec | 03, 04, 10 |

### R-AUTO-023

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | On Slack notifier omission in the SF Catch/Fail state, delayed failure awareness · consistency risk |
| Response | SF Catch · DAILY_EXECUTION_FAILED Slack notifier · 3 event labels. |
| Related spec | 04, 05, 10 |

### R-AUTO-024

| Item | Value |
| --- | --- |
| Status | 🔵 Accepted |
| Impact | Medium |
| Likelihood | Medium |
| Risk | secret exposure risk from long-term storage of the Slack notifier Lambda's webhook URL environment variable |
| Response | Re-review moving to Secrets Manager / SSM SecureString after operational stabilization. |
| Related spec | 04, 06, 10 |

### R-AUTO-025

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | On Scheduler · Dispatcher · SF chain failure, Daily automatic validation entry omitted |
| Response | Scheduler IAM least privilege + Dispatcher dryRun validation + phased activation + Catch Slack. |
| Related spec | 04, 05, 10 |

### R-AUTO-028

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | 07:50 start · 15:50 stop · Crawler stop execution failure from an EC2 lifecycle Scheduler error |
| Response | 2 Schedulers at 07:50 · 15:50 ENABLED + Lambda dryRun pass + weekend skip. |
| Related spec | 03, 04, 05, 08, 10 |

### R-AUTO-029

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | duplicate READY creation from repeated Intraday Evaluate calls · signal_type filter missing |
| Response | evaluate pre-duplicate check · Submit & Refresh signal_type-dedicated filter. |
| Related spec | 03, 04, 10 |

### R-AUTO-030

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Medium |
| Risk | 3-stage Intraday Stop Sell auto-ENABLE early entry · risk of broker stop-loss without an approval gate |
| Response | Keep initially DISABLED · enter after separate operator approval · Slack safety gate. |
| Related spec | 03, 04, 10 |

### R-AUTO-031

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Low |
| Risk | risk of entering the true-path even in the false state due to Intraday Stop Sell approval gate misconfiguration |
| Response | blocked gate safety test · separate from Daily Step 12 · signal_type filter. |
| Related spec | 03, 04, 05, 10 |

### R-AUTO-032

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Low |
| Risk | When Daily SELL · Intraday call the same executor, duplicate broker calls due to a missing signal_type filter |
| Response | --intraday-stop-only option · handle only signal_type=INTRADAY_STOP_SELL. |
| Related spec | 03, 04, 10 |

### R-AUTO-026

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Medium |
| Risk | KIS EGW00201 REJECTED automatic retry unsupported · order carried over · buy/sell opportunity lost |
| Response | Step 12 retry-normalizer EGW00201 expansion · 4 automatic retries passed. |
| Related spec | 03, 04, 10 |

### R-AUTO-027

| Item | Value |
| --- | --- |
| Status | 🔵 Accepted |
| Impact | Medium |
| Likelihood | Medium |
| Risk | APPROVAL_REQUIRED Slack shows 0/0 · operator misunderstanding · risk of wrong next-step decision |
| Response | Candidate for Mitigated promotion after improving the Slack payload builder. |
| Related spec | 04, 05, 10 |

### R-AUTO-033

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Low |
| Risk | port-view Local Spring Boot execution-type call default ENABLE regression · POST bypass risk |
| Response | 4 feature flags default false · view_app read-only · Fargate-entry follow-up audit. |
| Related spec | 04, 05, 10 |

### R-AUTO-034

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Low |
| Risk | Fargate Task Role states:StartExecution broadly granted · risk of gate default ENABLE regression |
| Response | Limit the Task Role's `states:StartExecution` Resource to allowed State Machine ARNs and keep the execution gate default false. |
| Related spec | 04, 05, 06, 10 |

### R-AUTO-035

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | Daily Brief Slack auto-send failure · operator awareness omission from duplicate sends · misunderstanding risk |
| Response | Daily Brief separates Dispatcher·Builder·Notifier responsibilities and checks Scheduler·Step Functions·Slack receipt together. |
| Related spec | 04, 05, 10 |

### R-AUTO-036

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Low |
| Risk | On Slack failure after an Intraday hard stop, risk of outputting only a warning without READY rollback |
| Response | broker submission only after passing the state machine approval gate · Notifier IAM limited. |
| Related spec | 03, 04, 06, 10 |

### R-AUTO-037

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Low |
| Risk | risk of Step 12~17 auto-execution running in a Step 1~11 failure · data-not-ready state |
| Response | Keep the Dispatcher market-holiday guard and the Step Functions readiness-state gate, and continue observing normal automatic cycles. |
| Related spec | 04, 05, 10 |

### R-AUTO-038

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | Medium |
| Likelihood | Medium |
| Risk | risk that the View Daily Batch screen cannot directly show AWS Step Functions execution history because it is not mirrored to `ops.strategy_daily_batch_run` |
| Response | Keep run-level·representative-step mirroring with the Recorder Lambda and a dedicated DB Role, and review detailed-step expansion. |
| Related spec | 04, 05, 10 |

## Cost
### R-COST-001

| Item | Value |
| --- | --- |
| Status | 🟠 Open |
| Impact | Medium |
| Likelihood | Medium |
| Risk | monthly cost surge from simultaneously activating ALB / NAT / VPC Endpoint multi-AZ / RDS multi-AZ |
| Response | NAT not used · ALB initially not used · keep paper single-AZ · Cost Explorer check. |
| Related spec | 02, 05, 10 |

### R-COST-002

| Item | Value |
| --- | --- |
| Status | 🟠 Open |
| Impact | Medium |
| Likelihood | Low |
| Risk | NAT Gateway creation from IaC / Console mistake · ~$0.06 per hour + data processing cost |
| Response | Confirm NAT not used in 02 runbook · include as a validation-checklist item. |
| Related spec | 02 |

### R-COST-003

| Item | Value |
| --- | --- |
| Status | 🟠 Open |
| Impact | Medium |
| Likelihood | Low |
| Risk | S3 report lifecycle unset · storage cost accumulation · careless public read exposure risk |
| Response | Job Role PutObject prefix-limited · S3 lifecycle policy introduction follow-up. |
| Related spec | 09, 06, 10 |

## Docs
### R-DOCS-001

| Item | Value |
| --- | --- |
| Status | 🔴 Open |
| Impact | High |
| Likelihood | Medium |
| Risk | risk of plaintext recording of secret / token / account number / webhook in spec deliverables |
| Response | Record sensitive information only with the `[REDACTED]` family. Periodically scan documents and git history. |
| Related spec | all specs |

### R-DOCS-002

| Item | Value |
| --- | --- |
| Status | 🟢 Mitigated |
| Impact | High |
| Likelihood | Low |
| Risk | risk of Windows worker KRX / RDS password plaintext exposure in PowerShell · wrapper · log files |
| Response | Prohibit wrapper password Write-Host · periodic log secret grep check. |
| Related spec | 06, 08 |

## Accepted Risks

| Item | Value |
| --- | --- |
| R-AUTO-024 | Slack webhook environment-variable storage · review moving to Secrets Manager or SSM SecureString after operational stabilization |
| R-AUTO-027 | APPROVAL_REQUIRED Slack shows 0/0 · review Mitigated promotion after Builder improvement |
| Operating principle | Accepted is a state where the risk is recognized and accepted under current conditions, and is subject to periodic re-review |

## Evidence Management

| Item | Value |
| --- | --- |
| This document | Risk ID · status · impact · likelihood · risk · response · related spec |
| Detailed execution evidence | each spec `operation-notes.md` |
| Per-date work | `WORKLOG.md` |
| Decision changes | `operator-decisions.md` |
| Cost details | `cost-simulation.md` |
| raw log · SQL · AWS JSON | Not recorded in this document |
| executionName · ARN · SHA256 | Not recorded in this document |
| Risk Details | Does not use a separate long-form section |

## Risk Update Rules

| Item | Value |
| --- | --- |
| New Risk | Assign the ID after the last number |
| ID reuse | Prohibited |
| Status change | Update after confirming validation evidence |
| Open | State where the response is not complete or periodic empirical demonstration is needed |
| Mitigated | State where response application and validation are complete |
| Accepted | Risk recognized and accepted under current conditions |
| Closed | State where the risk cause is removed and there is no recurrence possibility |
| Detailed history | Recorded in operation-notes and WORKLOG |
| Body length | Keep the risk and response to one or two sentences each |
| Table format | standalone tables use 2 columns `Item / Value` |
| Sensitive information | Use only `[REDACTED]`-family placeholders |

## Security Notes

| Item | Value |
| --- | --- |
| Actual AWS execution | None |
| Application code modification | None |
| AWS resource change | None |
| broker · KIS · DB execution | None |
| Sensitive information originals | Recording prohibited |
| Allowed notation | `[REDACTED]`-family placeholder |
| Document role | AWS Migration common Risk Register |
