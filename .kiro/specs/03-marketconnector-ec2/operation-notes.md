# Operation Notes — 03-marketconnector-ec2

Accumulates the 03-marketconnector-ec2 progress results by date. Facts / results / follow-up actions only. First applied environment `aws-paper`, region `ap-northeast-2`.

Principles

- No plaintext recording of actual secret value, KIS app key / app secret, account number, RDS endpoint hostname, RDS password, S3 bucket name, dump file path, account-id, actual secret ARN, IAM access key id, instance-id, EIP. All use `[REDACTED]` or a placeholder.
- No pasting of raw stdout / stderr. The result is one of three values: success / failure / follow-up needed.
- 0 new order / buy / sell / cancel / modify calls.
- Actual AWS resource creation / modification / deletion is performed directly by the operator. Kiro only writes documentation / organizes procedures / organizes validation items.

## 2026-06-09 Used as RDS restore runner

- Usage form: this EC2 is used as an RDS restore runner.
- Flow: local PostgreSQL `pg_dump` → S3 temporary bucket → EC2 → private RDS `pg_restore` (client 18.4).
- Result:
  - 10 schemas created = success
  - 7 roles created = success
  - core table row count match = success
  - `pg_restore` exit code 0 = success
- Actual dump file path / S3 bucket name / RDS endpoint hostname / instance-id / EIP / account-id not recorded.
- AWS resource changes: performed directly by the operator (not performed by Kiro).

## 2026-06-10 MarketConnector EC2 operational transition validation

- Usage form: transition the same EC2 from RDS restore runner → MarketConnector formal operational EC2.
- Input: 06 spec first-application result (Secrets 4 + SSM Parameter 6 + Instance Role + read-only Policy).
- 8 validations:
  - (a) Python 3.9.25 / 1 venv configured = success
  - (b) `requests` / `flask` / `psycopg2-binary` / `psycopg` / `pandas` 5 packages installed = success
  - (c) RDS connection based on `marketconnector_app` = success
  - (d) `connector_balance.py` execution = success
  - (e) `connector_order_check.py` execution = success
  - (f) Flask internal smoke test (query-only endpoint) = success
  - (g) env injection based on Secrets Manager / SSM Parameter Store = success
  - (h) execution without an Access Key based on the EC2 Instance Role = success
- 0 new order / buy / sell / cancel / modify entrypoint calls (`connector_buy.py` / `connector_sell.py` / `connector_cancel.py` / `connector_modify.py` not executed).
- 0 RDS DDL/DML. Query-only SELECT only.
- `secretsmanager:GetSecretValue` calls are performed only by the operator. Kiro automatic validation is limited to the `DescribeSecret` / `DescribeParameters` level.
- Actual secret value / account number / RDS endpoint / RDS password / account-id / actual ARN / IAM access key id / instance-id / EIP not recorded.
- AWS resource changes: performed directly by the operator (not performed by Kiro).

## 2026-06-13 Strategy order execution executor added

Accumulates the result of the MarketConnector new executor (`connector_strategy_order_execute.py`) addition that the operator performed directly on 2026-06-13. On this date Kiro only performed documentation / procedure organization, and the actual code authoring / static validation / `python -m py_compile` execution was carried out directly by the operator.

### 1. Background of the new executor addition

1. Responsibility separation decision: complete
   1) The auto buy / sell entrypoints of Strategy Execution (`port_strategy_execution`) had been directly calling `connector_buy.buy_stock()` / `connector_sell.sell_stock()`.
   2) As a follow-up responsibility-separation decision, the meaning of Strategy Execution's `--execute` was narrowed to handle only the `READY -> REQUESTED` state transition.
   3) A new executor on the MarketConnector side was needed to actually submit `REQUESTED`-state strategy orders to the broker / KIS API.
2. Result on this date: complete
   1) New executor added — `connector_strategy_order_execute.py`
   2) The existing validated paper order entrypoints are unchanged
   3) The fact of the responsibility separation on the Strategy Execution side is accumulated separately in [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 §2

### 2. Changed / unchanged file inventory

1. Newly added: complete
   1) `connector_strategy_order_execute.py`
       - 489 insertions
       - New file, with no existing tracked changes
2. Guarantee that existing validated entrypoints are unchanged: complete
   1) `connector_buy.py`: unchanged
   2) `connector_sell.py`: unchanged
   3) `connector_order_common.py`: unchanged
   4) `connector_order_check.py`: unchanged
   5) `connector_balance.py`: unchanged
   6) `db_config.py`: unchanged
   7) `config.py`: unchanged
   8) `token_manager.py`: unchanged

### 3. New executor behavior definition

1. Target order query: complete
   1) Only orders in `strategy_execution_order` with `status = 'REQUESTED'` + `connector_order_request_id IS NULL` are targeted.
   2) Sort: SELL first, BUY second.
2. Default execution mode = dry run: complete
   1) Print the target order list to stdout only.
   2) 0 KIS / broker API calls.
   3) 0 DB updates.
3. `--execute` execution mode: complete
   1) Enter real ordering only when the `PORT_ENVIRONMENT=paper` and `PORT_DB_TARGET=aws-paper` guards pass.
   2) Lazy import then call `buy_stock()` / `sell_stock()` of `connector_buy` / `connector_sell`.
   3) On success, update `strategy_execution_order` to `SUBMITTED`.
   4) On failure, update `strategy_execution_order` to `FAILED`.
   5) On SELL success, call the position `SELL_ORDERED` update helper.

### 4. Static validation

1. Compile check: complete
   1) `python -m py_compile connector_strategy_order_execute.py` passed.
2. Encoding check: complete
   1) Confirmed UTF-8 Korean literals display normally.
3. dry run single execution result: complete
   1) Output: `[NO_TARGET] REQUESTED strategy order 없음`
   2) Reason: at this date's point in time there were 0 rows matching `REQUESTED` + `connector_order_request_id IS NULL`, so it was judged a normal dry run result.

### 5. Out of scope for this date / follow-up handover

1. Actual `--execute` execution: on hold
   1) Not included in this date's validation scope.
   2) Reason: 0 REQUESTED target order rows + SSM Port Forwarding / AWS Paper RDS connection policy needs organizing.
2. EC2 deployment: follow-up
   1) EC2 deployment of the new executor is a follow-up task.
   2) Produce a zip or tag candidate per the operator's decision, then deploy.
3. Weekday or safe test data validation: follow-up
   1) `READY -> REQUESTED -> SUBMITTED` end-to-end dry / integration validation is a follow-up phase.
   2) Because of the weekend guard (WEEKEND) blocking effect, only the dry run stage was possible on this date.

### 6. Safety / security check result

1. Within this date's work scope, 0 KIS / broker API calls. 0 actual `--execute` calls.
2. 0 RDS DDL/DML. The dry run can use query-only SELECT only.
3. 0 plaintext records in this note of actual secret value / account number / RDS endpoint hostname / RDS password / KIS app key / KIS app secret / token / account-id / actual ARN / instance-id / EIP.
4. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog due to this date's work. The `connector_strategy_order_execute.py` that the operator added directly is recorded in this note only as a fact, with 0 full-body quotations.

## 2026-06-13 SSM Port Forwarding standard waypoint role first validation

Accumulates the fact that this EC2 was first validated as the SSM Port Forwarding standard waypoint for Local-to-AWS Paper RDS connection. For the detailed validation procedure / Runbook body, refer to the [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding connection validation + Runbook section.

### 1. Additional role of this EC2

1. SSM Port Forwarding standard waypoint: complete
   1) Decision lock: OD-NET-010 (SSM Port Forwarding standard waypoint = `portfolio-paper-marketconnector-ec2`).
   2) instance id: `i-0fce77927b7397b88`
   3) private ip: `10.0.0.181`
   4) state: `running` (at this date's check point)
   5) SSM Managed Node status: ping `Online` / platform `Linux` / agent `3.3.4515.0`
2. Usage flow: complete
   1) Local PC `localhost:15433` → SSM Session Manager tunnel → this EC2 → AWS Paper RDS `portfolio-paper-rds:5432`
   2) Connection to AWS Paper RDS from local is possible without RDS Public exposure (`PubliclyAccessible = False`).
   3) `portfolio-paper-crawler-worker` (instance id `i-0ff768ea639a91355`) is maintained in the KRX GUI / Windows worker role (consistent with 08 spec) — it is not used as an SSM Port Forwarding waypoint.

### 2. 03 spec impact

1. This EC2's formal operational role: complete
   1) This EC2 maintains as-is the EC2 operational pattern of 03 spec design §2 / §3 (MarketConnector formal operation + Instance Role + Secrets Manager / SSM Parameter Store env injection).
   2) The only role added on this date is the SSM Port Forwarding waypoint; 0 EC2 new creation / instance type change / EBS recreation / public subnet change / EIP detach (consistent with 03 design §2.3).
2. 0 SG / IAM changes: complete
   1) 0 changes to `sg-marketconnector-ec2` inbound / outbound rules.
   2) 0 changes to Instance Role / Instance Profile permissions. This EC2's SSM Managed Node permission is used as-is, from the `AmazonSSMManagedInstanceCore` managed policy attachment result of 03 spec §9.

### 3. This date's validation result summary

1. SSM Port Forwarding tunnel open: complete (session id `terraform-vjp3fv3nz73konetcevdzjh9de` / local port 15433 / remote 5432).
2. `portfolio_admin` connection: complete (`current_user` = `portfolio_admin`, `inet_server_addr` = `10.0.20.165`, `inet_server_port` = `5432`).
3. `execution_app` connection: complete (Strategy Execution porting pre-validation passed / search_path = `execution, decision, research, connector, preprocessor, interest, reference, legacy, public`).
4. 0 broker / KIS / order / fill entrypoint calls. 0 RDS DDL/DML. Limited to SELECT queries.

### 4. Out of scope for this date / follow-up handover

1. SSM Port Forwarding session automatic keep-alive / reconnect: follow-up (R-AUTO-012).
2. psql client formal install / PATH registration (operator local PC): follow-up (R-AUTO-013).
3. MarketConnector new executor (`connector_strategy_order_execute.py`) EC2 deployment candidate zip / tag production: follow-up (03 spec follow-up phase or 07 spec).
4. Transition of this EC2 to a normal operational mode based on a systemd unit / startup script: responsibility of a 03 spec follow-up task or a separate phase.

### 5. Safety / security check

1. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source from this date's work. 0 AWS resource changes.
2. 0 plaintext records in this note of password / secret value / KIS app key / KIS app secret / account number / token / account-id / actual IAM access key id / actual secret ARN / EIP. All use `[REDACTED]` or a placeholder.
3. instance id / private IP / SSM session id / local port / RDS endpoint hostname are recorded as facts as operational identifiers (consistent with the user-specified policy — not a secret).

## 2026-06-13 SSM Port Forwarding standard waypoint role — psql 18 + pgAdmin4 reinforcement

This section is a follow-up to §1 ~ §5 of the earlier section of the same date (`## 2026-06-13 SSM Port Forwarding standard waypoint role first validation`). Accumulates the fact that, while this EC2 serves as the SSM Port Forwarding standard waypoint, connection to AWS Paper RDS was also first demonstrated with 2 additional clients (local PostgreSQL 18 `psql.exe` + pgAdmin4).

For detailed results, refer to the [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding reinforcement (psql 18 client + pgAdmin4 connection validation) section.

### 1. Additional client compatibility confirmation of this EC2

1. Local PostgreSQL 18 psql client: complete
   1) Direct path execution — `C:\Program Files\PostgreSQL\18\bin\psql.exe`
   2) client `18.1` / server `18.4` / SSL `TLSv1.3` consistent.
   3) Consistent with the client major 18 / full 18.4 policy (03 spec design §5 / R-DATA-003 mitigation).
2. pgAdmin4: complete
   1) `localhost:15433` registered — direct RDS endpoint registration prohibited (consistent with OD-NET-011).
   2) Connection is established only when this EC2's SSM Port Forwarding tunnel is open.
   3) On tunnel termination the pgAdmin4 connection is immediately severed (consistent with R-AUTO-012).

### 2. This EC2's operational impact

1. 0 changes to SG / Instance Role / Instance Profile permissions — no changes on this EC2's side.
2. This EC2's SSM Managed Node permission is used as-is, from the `AmazonSSMManagedInstanceCore` managed policy attachment result of 03 spec §9 (consistent with the earlier section §1 ~ §2).
3. Expansion of the client types used over the SSM Port Forwarding tunnel (Python `psycopg2` / psql 18 / pgAdmin4) is outside this EC2's area of responsibility — it is the responsibility of the client on the operator's local PC.

### 3. Safety / security check

1. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source from this date's work. 0 AWS resource changes.
2. 0 plaintext records of password / secret value. All use `[REDACTED]` or a placeholder. The passwords of pgAdmin4 / psql 18 are used only in the operator's local PC environment, and plaintext recording in this note / console captures / logs is prohibited (consistent with R-DOCS-001).
3. This EC2's operational identifiers (the instance id / private IP / SSM session id in §1 of the earlier section) are reused as-is. No additional operational identifiers.

## 2026-06-17 MarketConnector query-only dry-run re-validation

Accumulates the result of the MarketConnector EC2 query-only dry-run re-validation that the operator performed directly on 2026-06-17. On this date Kiro only performed documentation / procedure organization, and the SSM RunCommand / Secrets Manager / SSM Parameter Store / Instance Role-based execution and RDS access were all carried out directly by the operator. This date is limited to `aws-paper` / 0 aws-live work / 0 new orders / 0 `--execute`.

### 1. `CONNECTOR_BALANCE` re-validation

1. Execution form: complete
   1) Execute `connector_balance.py` in the venv environment inside the MarketConnector EC2 via SSM RunCommand.
   2) Environment variables are injected with the `/tmp/inject-env.sh` pattern (permission 700 / no plaintext secret storage / memory export only).
2. First failure cause: confirmed
   1) The `kis-app-key` / `kis-app-secret` / `paper-account` in Secrets Manager are a JSON SecretString, not a plain string.
   2) In the first attempt the entire SecretString (JSON dict form) was exported as the environment variable value as-is — its form differed from the plain value the code expected, so authentication failed.
3. Correction (v5 pattern): complete
   1) JSON-parse the result of `aws secretsmanager get-secret-value --secret-id <name> --query SecretString --output text`, then extract only the value per internal key.
   2) Export the `APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD` / `BASE_URL` extraction result to both the compatibility keys and the `KIS_*` aliases simultaneously (`KIS_APP_KEY` / `KIS_APP_SECRET` / `KIS_PAPER_ACNT` / `KIS_ACNT_PRDT_CD` / `KIS_BASE_URL`).
   3) 0 plaintext records of the secret value itself in stdout / logs / console captures / operator notes. Only value length / key presence confirmed.
4. Result confirmation: complete
   1) 1 latest row in `connector.connector_balance_snapshot` confirmed.
   2) `as_of_date` `2026-06-17`
   3) `as_of_ts` `2026-06-17 00:46:17 UTC`
   4) `created_at` `2026-06-17 00:46:17 UTC`
   5) `source_api` `inquire-balance`
   6) `source_version` `connector-balance-1.0.0`
   7) 0 held stocks is treated as a normal 0 based on the same `as_of_date` in `connector.connector_position_snapshot` (consistent with legacy holdings 0).
   8) The KIS API call is limited to balance query. 0 new order / buy / sell / cancel / modify calls.

### 2. `CONNECTOR_ORDER_CHECK` re-validation (MarketConnector query-series preliminary validation)

This stage is the result of the operator preliminarily validating a single step `CONNECTOR_ORDER_CHECK` (order 13), located in the latter half of the Daily 17 steps. On this date it was executed only as a MarketConnector query-series preliminary validation, not the full Daily 17-step flow.

1. Execution form: complete
   1) Execute `connector_order_check.py` in the venv environment inside the MarketConnector EC2 via SSM RunCommand.
   2) v5 pattern reused — JSON SecretString internal key extraction + simultaneous `APP_*` / `KIS_*` export. No first failure.
3. KIS API call result: complete
   1) `api_name` `inquire-daily-ccld`
   2) `response_status` `200`
   3) `response_code` `0`
   4) `is_success` `true`
   5) `called_at` `2026-06-17 00:51:03 UTC`
   6) The call is limited to query. 0 new order / buy / sell / cancel / modify calls.
4. row count inventory: complete
   1) `connector.connector_order_request` cumulative `33`
   2) `connector.connector_order_event` cumulative `18`
   3) `connector.connector_fill` cumulative `13`
   4) 0 new `connector_order_event` / `connector_fill` rows at this execution point is judged normal for this date's non-occurrence of new orders / fills (consistent with 0 KIS new orders / fills / OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004).
5. PowerShell variable loss (minor issue): confirmed
   1) The operator-side local PowerShell variables were temporarily lost, so command reconstruction was carried out.
   2) No impact on the AWS / EC2 / RDS / KIS side — the execution result inside the MarketConnector EC2 is normal.

### 3. Safety / security check result

1. Within this date's work scope, 0 KIS / broker / new order calls. 0 actual `--execute` calls. 0 aws-live work.
2. 0 RDS DDL/DML. 1 `connector_balance_snapshot` insert / 0 `connector_position_snapshot` inserts (0 held stocks). Otherwise limited to SELECT.
3. 0 plaintext records in this note of actual secret value / KIS app key / KIS app secret / account number / token / RDS password / RDS endpoint hostname / account-id / actual IAM Role ARN / actual secret ARN / instance-id / EIP / image digest. All use `[REDACTED]` or a placeholder.
4. The fact of JSON SecretString internal key parsing is recorded only as a mapping fact. 0 plaintext records of value. First demonstration of the raw SecretString export prohibition policy (consistent with R-DOCS-001).
5. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to this date's work. Kiro modified only this spec's documents (design.md §8.2 / §8.2.1 / §8.2.2 / §8.2.3 / §8.3 / runbook.md §2 / §4 / part of validation-checklist / this note / tasks.md / root common documents).


- [`./runbook.md`](./runbook.md) §3 ~ §6 result reflection → accumulate per-date update of this note at the operator's EC2 shell work point.
- [`./validation-checklist.md`](./validation-checklist.md) §2 / §4 / §5 / §6 reflect the result after checking the `[운영자 확인 필요]` items.
- [`./validation-checklist.md`](./validation-checklist.md) §4 operationally viable candidate entrypoints (`connector_quote_realtime.py` / `connector_quote_closed.py` / `connector_view_service.py`) validation → follow-up phase or a separate phase.
- [`./validation-checklist.md`](./validation-checklist.md) §6 `CloudWatchLogsWrite` recommended option (pre-create log group + exclude `CreateLogGroup`) application → update after the operator's direct work.
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md) update candidates: OD-SEC-005 / OD-SEC-006 provisional → confirmed candidates, new candidate for the `AmazonSSMManagedInstanceCore` usage policy (upon operator approval).
- [`../_common/risk-register.md`](../_common/risk-register.md) update candidates: R-DATA / R-CAP / R-BROKER / R-SEC / R-AUTO 5 items (upon assigning the next available ID and operator approval).
- [`../_common/followups-overview.md`](../_common/followups-overview.md) update candidates: 03 first applied environment / first scope / out of scope / 04 / 05 / 08 / 09 / 10 spec handover.
- Transition to a normal operational mode (systemd unit or startup script) = responsibility of a follow-up task of this spec or a separate phase. At this point the temporary validation stage (shell + temporary export script) is maintained.

## 2026-06-17 (Daily AWS 17-step E2E complete)

The Daily AWS 17-step E2E flow that the operator performed directly as a follow-up to the same date's first session (MarketConnector query-only dry-run re-validation) was connected end-to-end on this date.

There are 4 steps within this spec's scope.

- Step 1 `CONNECTOR_BALANCE` (uses the first session's result as-is / consistent with §1 of the earlier section of this note)
- Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`
- Step 13 `CONNECTOR_ORDER_CHECK`
- Step 17 `BALANCE_REFRESH`

The remaining steps (2 / 3 / 4 / 5 / 6 / 7 / 8 / 9 / 10 / 11 / 14 / 15 / 16) are consistent with the 2026-06-17 §1 ~ §6 / §1 ~ §3 accumulation of the 04 / 08 / 09 spec operation-notes.

Kiro only performed documentation / procedure organization. The actual SSM RunCommand / KIS API / RDS work was carried out directly by the operator. This date is limited to `aws-paper` / 0 aws-live work. The actual broker calls were only 4 KIS paper BUY / 0 SELL · cancel · modify calls.

### 1. Step 1 `CONNECTOR_BALANCE` (uses the first session's result)

1. Uses the first session's result as-is: complete
   1) Consistent with the earlier section of this note, 2026-06-17 MarketConnector query-only dry-run re-validation §1.
   2) `connector.connector_balance_snapshot` latest row `as_of_date 2026-06-17` / `as_of_ts 2026-06-17 00:46:17 UTC` / `source_api inquire-balance` / `source_version connector-balance-1.0.0` / 0 held stocks.
   3) Reuse the v5 env injection result (JSON SecretString internal key extraction + `APP_*` compatibility key + `KIS_*` alias simultaneous export) as-is across the entire 17-step span.

### 2. Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`

1. First attempt / failure cause: confirmed
   1) `connector_strategy_order_execute.py` not deployed to the MarketConnector EC2 — the new executor was not on the EC2 local at the initial dry-run point.
   2) After EC2 deployment, import failed with `psycopg` absent on entering system python — confirmed the venv python must be used.
   3) `execution` table UPDATE permission missing — consistent with the OD-DB-008 R-only policy of `marketconnector_app` / the operator's direct GRANT correction was needed for the SUBMITTED transition.
   4) Issue that `source_run_id` fallback patch is needed — follow-up review of the `source_daily_signal_id null` correction in `result_payload` (consistent with R-AUTO-009 / R-DATA-005 [2026-06-17 reinforcement]).
2. Correction / formal deployment: complete
   1) `connector_strategy_order_execute.py` formally deployed to the MarketConnector EC2 venv python environment (operator direct work).
   2) `execution` table UPDATE permission GRANT correction (operator direct work / consistent with 02 spec operation-notes 2026-06-17 §1 fact record).
   3) `source_run_id` fallback patch applied (operator direct work / the diff is a fact record in this note only).
   4) Entering the paper-environment-limited path per the `--execute` guard (`PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper`) consistency (consistent with R-AUTO-010 [2026-06-17 reinforcement]).
3. Result of submitting 4 KIS paper BUY: success
   1) `282330` BGF리테일 — `execution_order_id 26` SUBMITTED / `connector_order_request_id 34` created / `broker_order_no 0000035906`.
   2) `004990` 롯데지주 — `execution_order_id 27` SUBMITTED / `connector_order_request_id 35` created / `broker_order_no 0000035912`.
   3) `003490` 대한항공 — `execution_order_id 28` SUBMITTED / `connector_order_request_id 36` created / `broker_order_no 0000035918`.
   4) `088350` 한화생명 — `execution_order_id 29` SUBMITTED / `connector_order_request_id 37` created / `broker_order_no 0000035932`.
   5) All 4 paper responses normal / 0 SELL / cancel / modify calls / 0 aws-live work (consistent with OD-MS-016 / OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004).
   6) The Strategy Execution `READY -> REQUESTED` transition is the responsibility of 04 spec Step 11 `DAILY_AUTO_BUY` / this step handles only the `REQUESTED -> SUBMITTED` transition (first demonstration of the OD-MS-016 responsibility separation).
4. Follow-up supplement candidates: follow-up
   1) Review of the `source_daily_signal_id null` correction within `connector_strategy_order_execute.py` `result_payload` (OPTIONAL_COLUMNS or fallback pattern) — tasks.md task 30 follow-up phase.
   2) Formal matrix update of the `execution` table UPDATE permission is a 02 spec db-roles-and-grants follow-up phase.
   3) MarketConnector EC2 single-file manual deployment recurrence prevention / deployment checklist / patch deployment procedure — 07 spec follow-up phase.

### 3. Step 13 `CONNECTOR_ORDER_CHECK` (this execution)

This step is a separate real execution from the same date's first-session preliminary single validation (the earlier section §2 of this note). This execution is the fill synchronization flow immediately after the Step 12 submission of 4 KIS paper BUY.

1. KIS `inquire-daily-ccld` call: success
   1) The API call itself responded normally.
   2) The first response form was `output1 empty` + `output2 summary only` — `output2` was the aggregate summary of all 4 orders.
2. First fallback mapping contamination identified: confirmed
   1) The existing summary fallback logic incorrectly mapped the summary value to the last order id `37` / `088350` 한화생명.
   2) The incorrectly created `connector.connector_order_event` / `connector.connector_fill` rows were identified (consistent with the new R-AUTO-018).
   3) The operator immediately deleted the incorrectly created rows + recovered the `connector_order_request` status.
3. summary fallback guard patch: complete
   1) `connector_order_check.py` summary fallback guard patch applied (operator direct work).
   2) If there are 2 or more active order candidates, changing event / fill / status via summary fallback is prohibited.
   3) summary fallback is allowed only when there is exactly 1 active order candidate.
   4) The patch diff is a fact record in this note only — 0 full-body quotations (first demonstration of R-AUTO-018 mitigation / consistent with R-DOCS-001).
4. Fill synchronization via per-`broker_order_no` single query: success
   1) All 4 proceeded with fill synchronization via KIS single query based on the `broker_order_no` input parameter.
   2) `connector.connector_order_request 34 ~ 37` all confirmed transitioning to FILLED.
   3) `connector.connector_fill 26 ~ 29` new rows confirmed created.
   4) 0 patterns of only the last row being updated under a multi active-order situation (first demonstration of R-AUTO-018 mitigation).
5. Follow-up supplement candidates: follow-up
   1) Add summary fallback guard test cases (per active-candidate 0 / 1 / 2 / many + `output1` / `output2` input forms) — tasks.md task 29 follow-up phase.

### 4. Step 17 `BALANCE_REFRESH`

1. First failure cause: confirmed
   1) Due to a `legacy.holdings` search_path / permission problem, the SSM RunCommand result was a `relation "holdings" does not exist` pattern.
   2) In the AWS DB, bare `holdings` could not be resolved within the search_path of `marketconnector_app`.
   3) `marketconnector_app`'s `legacy` schema USAGE not granted (consistent with OD-DB-007 — all app roles ungranted for the legacy schema) + `legacy.holdings` DML not granted + sequence not granted + database search_path missing — unsuitable for the bare-table-name-dependent legacy path call.
2. Operator action: complete (consistent with 02 spec / 06 spec operation-notes fact record / consistent with the new R-DATA-011)
   1) Corrected `marketconnector_app`'s database search_path to `connector, execution, legacy, reference, public`.
   2) Granted `legacy` schema USAGE permission (1 exceptional GRANT needed for legacy operational data access — a candidate for follow-up re-review of OD-DB-007).
   3) Granted `legacy.holdings` DML (SELECT / INSERT / UPDATE / DELETE) permission (limited to the minimum permission for legacy operational data update).
   4) Granted `legacy` schema sequence permission + corrected future default privileges.
3. Re-execution result: success
   1) SSM command Status `Success`.
   2) ResponseCode `0`.
   3) StdErr empty.
   4) `connector.connector_position_snapshot` 4-stock latest rows confirmed created.
4. Final held snapshot: confirmed
   1) `282330` BGF리테일 — `position_snapshot_id 123` / `quantity 17` / `avg_buy_price 120182.35`.
   2) `004990` 롯데지주 — `position_snapshot_id 121` / `quantity 65` / `avg_buy_price 27043.08`.
   3) `003490` 대한항공 — `position_snapshot_id 120` / `quantity 52` / `avg_buy_price 28980.77`.
   4) `088350` 한화생명 — `position_snapshot_id 122` / `quantity 244` / `avg_buy_price 5744.41`.
5. Follow-up supplement candidates: follow-up
   1) Organize the `connector.connector_balance_snapshot` freshness validation SQL — distinguish the meaning of `as_of_ts` / `max(created_at)` rather than `created_at` alone (`as_of_ts` is the broker response reference point / `created_at` is the row insert point) — tasks.md task 31 follow-up phase.
   2) Formal documentation of the `legacy.holdings` permission / search_path correction — 02 spec db-roles-and-grants follow-up phase.
   3) The fact of 1 marketconnector_app-limited exception to the policy that the legacy schema is ungranted for all app roles (OD-DB-007) is accumulated in the 02 spec / 06 spec operation-notes / formal matrix update follow-up.

### 5. Safety / security check result (Daily AWS 17-step E2E)

1. Within this date's work scope, broker / KIS calls are limited to 4 KIS paper BUY (Step 12) + balance / order check query-only calls (Step 1 / Step 13). 0 SELL / cancel / modify / additional `--execute` calls. 0 SELL position `mark_position_sell_ordered()` calls (consistent with OD-MS-016 — no SELL flow occurred on this date). 0 aws-live work.
2. 0 RDS DDL. DML is limited to this date's normal 17-step flow.
   - `connector.connector_order_request` insert 4 (`id 34 ~ 37`)
   - `connector.connector_order_event` insert (delete the incorrectly created rows + recreate the normal rows)
   - `connector.connector_fill` insert 4 (`id 26 ~ 29`)
   - `execution.strategy_execution_order` update (`id 26 ~ 29` SUBMITTED → FILLED)
   - `connector.connector_balance_snapshot` insert 1
   - `connector.connector_position_snapshot` insert 4 (`id 120 ~ 123`)

   All 4 rows affected by `--execute` (KIS paper BUY) transition consistently SUBMITTED → FILLED → position OPEN.
3. The sensitive information below has 0 plaintext records in this note — all use `[REDACTED]` or a placeholder.
   - actual secret value / KIS app key / KIS app secret / token
   - account number
   - RDS password / RDS endpoint hostname
   - account-id / actual IAM Role ARN / actual secret ARN / IAM access key id
   - instance-id / EIP / image digest full sha256 / task ARN / job ARN
4. The 4 KIS paper `broker_order_no` (`0000035906` / `0000035912` / `0000035918` / `0000035932`) are broker response values that are operational identifiers — not real-account order numbers / consistent with this note's fact record (consistent with R-DOCS-001 [2026-06-17 reinforcement (17-step E2E)]).
5. The diffs of `connector_strategy_order_execute.py` / `connector_order_check.py` that the operator patched / formally deployed directly are recorded in this note only as facts — 0 full-body quotations. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to this date's work.
6. AWS / SSM / EC2 / RDS / Secrets Manager / SSM Parameter Store / KIS API calls are all performed directly by the operator. Kiro only performed documentation / procedure organization / validation item organization.
   - 0 AWS CLI executions
   - 0 AWS resource creation / modification / deletion
   - 0 plaintext records of `secretsmanager:GetSecretValue` result values
   - 0 plaintext quotations of CloudWatch Logs body / SSM response body / KIS API response body / full operator PowerShell stdout
7. The operational identifiers below are recorded as facts consistent with the user-specified policy — not secrets.
   - execution_plan_id `92` / execution_order id `26 ~ 29`
   - connector_order_request id `34 ~ 37` / broker_order_no 4 kinds
   - position_state_id `6 ~ 9` / connector_position_snapshot id `120 ~ 123`
   - stock codes `282330` / `004990` / `003490` / `088350` / stock names / quantities / avg_buy_price
   - total_qty `378` / total_target_amount `6908189.40`
   - data_date `2026-06-16` / signal_date · run_date `2026-06-17`

## 2026-06-17 (Daily AWS PowerShell wrapper implementation)

Accumulates the result of the operator-facing Windows PowerShell wrapper implementation of the Daily AWS 17-step that the operator performed directly as a follow-up to the same date's second session (Daily AWS 17-step E2E complete).

This wrapper work is not the sole responsibility of the 03 spec (MarketConnector EC2), but for the reasons below it is adopted as the cross-cutting accumulation location in the 03 spec operation-notes.

- (a) Step 12 PAPER_ORDER_GATE directly handles the 03 spec's `connector_strategy_order_execute.py --execute`.
- (b) Step 1 / Step 13 / Step 17 are all directly tied to the 03 spec's MarketConnector EC2 SSM RunCommand flow.
- The 04 / 06 / 08 / 09 spec are referenced only as the third follow-up memo in followups-overview.md 2026-06-17 / not accumulated separately (to prevent duplicate accumulation).

Safety facts within this wrapper work scope:

- 0 actual broker / KIS / new BUY · SELL · cancel · modify / `--execute` order submissions
- 0 aws-live work
- 0 wrapper-based full 1~17 actual re-executions
- 0 uses of Step 12 `-AllowPaperOrderExecute`
- bundled wrapper not created

Kiro only performed documentation / procedure organization. The actual wrapper code authoring / parser validation / DryRun / Step 1 SSM execution / Step 12 gate validation were all carried out directly by the operator.

### 1. wrapper deliverable inventory

1. main wrapper: complete
   1) `.kiro/scripts/run-daily-aws-paper.ps1`
   2) Parameter handling — `RunDate` / `Region` / `Environment` / `StartStep` / `EndStep` / `DryRun` / `AllowPaperOrderExecute`
   3) Environment input = only `aws-paper` allowed (consistent with the R-AUTO-002 [2026-06-17 wrapper reinforcement] mitigation)
   4) Step 12 PAPER_ORDER_GATE central blocking
   5) Overall execution result summary generation — `summary/run-summary.txt`
2. config: complete
   1) `.kiro/scripts/daily-aws-paper.config.ps1`
   2) Manages region / cluster / instance id / subnet / SG / task definition / job definition / log group / output path
   3) Manages ECS public subnet / Preprocessor TD / Interest Crawler TD / Strategy Decision TD / Strategy Execution TD / AWS Batch job queue / job definition / MarketConnector EC2 instance id / Windows KRX crawler worker instance id
3. functions: complete
   1) `.kiro/scripts/daily-aws-paper.functions.ps1`
   2) Step registry management
   3) SSM execution common function (`AWS-RunShellScript` Linux default + added `AWS-RunPowerShellScript` Windows support) — added documentName output to the execution log
   4) ECS RunTask common function (`Invoke-DailyAwsPaperEcsTask` / generate UTF-8 no BOM JSON via a Python patch script / `--overrides file://...` pattern / taskArn collection / `describe-tasks` polling / `STOPPED` wait / container exitCode confirmation / log group · stream prefix supplement / log storage based on `describe-log-streams` / `get-log-events`)
   5) AWS Batch SubmitJob common function (`Invoke-DailyAwsPaperBatchJob`)
       - Python patch script + `aws batch submit-job`
       - jobId collection / `describe-jobs` polling / `SUCCEEDED` · `FAILED` decision
       - Batch CloudWatch log stream confirmation / log storage based on `aws logs get-log-events`
       - On failure, throw including jobId / status / statusReason / exitCode / cloudWatchLog
   6) CloudWatch log collection / SSM stdout · stderr storage / summary recording / success·failure·blocker decision / PowerShell UTF-8 correction
4. 17 step files: complete
   1) `.kiro/scripts/steps/step-01-connector-balance.ps1` (MarketConnector EC2 SSM / `connector_balance.py`)
   2) `.kiro/scripts/steps/step-02-interest-crawler.ps1` (non-GUI ECS RunTask + Windows KRX worker `Portfolio-KRX-Worker-Daily` Scheduled Task trigger / skip trigger if worker not `running`)
   3) `.kiro/scripts/steps/step-03-preprocessor.ps1` (`portfolio-paper-interest-preprocessor:1` ECS RunTask)
   4) `.kiro/scripts/steps/step-04-backtest-research.ps1` (`portfolio-paper-strategy-research:5` AWS Batch SubmitJob)
   5) `.kiro/scripts/steps/step-05-backtest-report.ps1` (`portfolio-paper-strategy-report:3` AWS Batch SubmitJob / S3 upload wrapper)
   6) `.kiro/scripts/steps/step-06-daily-buy-signal.ps1` (`portfolio-paper-strategy-decision-buy-signal:1` ECS RunTask)
   7) `.kiro/scripts/steps/step-07-daily-position-signal.ps1` (`portfolio-paper-strategy-decision-position-signal:1` ECS RunTask)
   8) `.kiro/scripts/steps/step-08-daily-buy-execution.ps1` (`portfolio-paper-strategy-execution:1` + command override `python daily_buy_execution_run.py`)
   9) `.kiro/scripts/steps/step-09-daily-sell-execution.ps1` (`portfolio-paper-strategy-execution:1` + command override `python daily_sell_execution_run.py`)
   10) `.kiro/scripts/steps/step-10-daily-auto-sell.ps1` (`portfolio-paper-strategy-execution:1` + command override `python daily_auto_sell_execute_run.py --execute`) — `--execute` means strategy execution internal state creation·update / not direct broker · KIS submission (consistent with OD-MS-016 responsibility separation)
   11) `.kiro/scripts/steps/step-11-daily-auto-buy.ps1` (`portfolio-paper-strategy-execution:1` + command override `python daily_auto_buy_execute_run.py --execute`) — `--execute` means strategy execution internal state creation·update / not direct broker · KIS submission (consistent with OD-MS-016 responsibility separation)
   12) `.kiro/scripts/steps/step-12-marketconnector-strategy-order-execute.ps1` (MarketConnector EC2 SSM / `connector_strategy_order_execute.py --execute`) — **step capable of actual KIS paper order submission / blocked by default / allowed only when `-AllowPaperOrderExecute` is specified**
   13) `.kiro/scripts/steps/step-13-connector-order-check.ps1` (MarketConnector EC2 SSM / `connector_order_check.py`) — order status query / DB status update / no new order submission
   14) `.kiro/scripts/steps/step-14-sync-sell-fill.ps1` (`portfolio-paper-strategy-execution:1` + command override `python execution_sync_sell_fill.py`) — SELL fill / status DB update / no broker · KIS calls
   15) `.kiro/scripts/steps/step-15-sync-buy-fill.ps1` (`portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_fill.py`) — BUY fill / status DB update / no broker · KIS calls
   16) `.kiro/scripts/steps/step-16-sync-buy-position.ps1` (`portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_position.py`) — BUY position DB update / no broker · KIS calls
   17) `.kiro/scripts/steps/step-17-balance-refresh.ps1` (MarketConnector EC2 SSM / `connector_balance.py`) — balance / position snapshot refresh / no new order submission
5. bundled wrapper: not created
   1) `.kiro/scripts/run-daily-aws-paper-bundled.ps1` not created on this date — a follow-up optional task
   2) Created if single-file execution is needed at a point when per-step file validation for the 17 steps is sufficiently stabilized
   3) The default development · validation · operation baseline maintains the separated file structure (consistent with OD-MS-023)

### 2. Step 1 standalone SSM first validation

1. Execution form: complete
   1) `-StartStep 1 -EndStep 1` standalone execution
   2) Execute `connector_balance.py` in the venv python environment via MarketConnector EC2 SSM RunCommand
   3) `/tmp/inject-env.sh` v5 env injection used (JSON SecretString internal key extraction + `APP_*` compatibility key + `KIS_*` alias simultaneous export / consistent with R-DOCS-001)
2. Execution result: success
   1) SSM RunCommand `Status: Success`
   2) `ResponseCode: 0`
   3) stdout / stderr file storage (under `C:\Temp\portfolio-daily-aws-paper\<run-id>\logs\`)
   4) `connector.connector_balance_snapshot` stored — consistent with the 03 spec body responsibility flow
   5) `connector.connector_position_snapshot` deleted then re-stored — consistent with the 03 spec body responsibility flow
   6) `legacy.holdings` stored — consistent with the 03 spec body responsibility flow (consistent with the state where the marketconnector_app legacy schema USAGE / DML / search_path correction was completed via R-DATA-011 mitigation)
   7) 0 new order / buy / sell / cancel / modify calls / broker · KIS API response limited to balance query

### 3. Step 12 PAPER_ORDER_GATE safety blocking validation

This validation is the wrapper's most core safety check. It first validates whether the wrapper central PAPER_ORDER_GATE + Step 12 internal double gate simultaneously block Step 12 — the step capable of actual KIS paper order submission — from executing at an unintended point.

1. Execution form: complete
   1) `-StartStep 12 -EndStep 12` execution mode (not DryRun)
   2) Option input — `DryRun: False` / `PaperOrder: False` defaults
   3) 0 explicit `-AllowPaperOrderExecute` options
2. First blocking — wrapper central PAPER_ORDER_GATE: complete
   1) The main wrapper's step registry identifies Step 12's Risk classification as `PAPER_ORDER_GATE`
   2) Because `$PaperOrder` is false, it is immediately blocked at the wrapper central gate / the blocking message is output normally
   3) 0 Step 12 SSM command submissions themselves
3. Second blocking — Step 12 internal double gate: complete
   1) The step file itself also re-validates the `-AllowPaperOrderExecute` input value
   2) On non-specification, immediate termination / 0 unintended bypass entries
4. Safety check result: complete
   1) No actual SSM command submission
   2) 0 actual KIS order submissions
   3) 0 new `connector.connector_order_request` rows
   4) 0 plaintext outputs of secret value / KIS app key / KIS app secret / account number / token in the Step 12 stdout / stderr files (consistent with the R-DOCS-001 [2026-06-17 wrapper reinforcement] mitigation)
   5) First demonstration of R-AUTO-019 mitigation

### 4. Step 13 / Step 17 wrapper flow organization

Step 13 / Step 17 use the 03 spec's MarketConnector EC2 SSM RunCommand flow as-is, but both are classified as steps with no new broker · KIS order submission.

1. Step 13 `CONNECTOR_ORDER_CHECK`: complete (DryRun FOUND confirmed)
   1) MarketConnector EC2 SSM RunCommand structure connected
   2) `/tmp/inject-env.sh` loading + `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` validation
   3) `connector_order_check.py` execution
   4) **Classification: order status query / DB status update step / no new broker · KIS order submission**
   5) Uses the summary fallback guard patch (consistent with 2026-06-17 17-step E2E §3) when there are many active orders in the `output1 empty` + `output2 aggregate summary` form — the wrapper calls the `connector_order_check.py` formally deployed in 03 spec as-is / the wrapper does not have separate fallback logic (consistent with R-AUTO-018 mitigation)
2. Step 17 `BALANCE_REFRESH`: complete (DryRun FOUND confirmed)
   1) MarketConnector EC2 SSM RunCommand structure connected
   2) `/tmp/inject-env.sh` loading + environment validation
   3) `connector_balance.py` execution (same entrypoint as Step 1 / only the step name differs within the wrapper)
   4) **Classification: balance / position snapshot refresh step / no new broker · KIS order submission**
   5) Consistent with the state where `marketconnector_app`'s legacy schema USAGE / `legacy.holdings` DML / sequence / database search_path was corrected in 17-step E2E §4 (first demonstration of R-DATA-011 mitigation)

### 5. Full 1~17 DryRun result / parser validation / safety grep

1. Full 1~17 DryRun: complete
   1) `-DryRun -StartStep 1 -EndStep 17` execution
   2) All 17 steps confirmed `FOUND`
   3) 0 actual ECS RunTask submissions
   4) 0 actual Batch SubmitJob submissions
   5) 0 actual SSM command submissions
2. PowerShell parser validation: complete
   1) main wrapper / config / functions / 17 step files = 20 files total
   2) All parser OK
   3) 0 parser errors
3. Dangerous keyword safety grep: complete
   1) Checked `--execute` / order / buy / sell keywords
   2) Step 10 / Step 11's `--execute` is classified for strategy execution internal state creation·update / not direct broker · KIS submission (consistent with OD-MS-016 responsibility separation)
   3) Step 12's `--execute` is classified as a step capable of actual KIS paper order submission + registered with Risk `PAPER_ORDER_GATE` + passed the central + internal double gate check
   4) Step 13 = order status query / DB status update classification
   5) Step 14 / Step 15 / Step 16 = fill / position sync DB update classification
   6) 0 additions of unintended broker · KIS order submission commands

### 6. Safety / security check result (Daily AWS PowerShell wrapper implementation)

1. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging from this date's wrapper work. The `.kiro/scripts/` new folder / files are the operator's local PC tooling / outside Kiro spec deliverables.
2. The sensitive information below has 0 plaintext records in this note — all use `[REDACTED]` or a placeholder.
   - actual secret value / KIS app key / KIS app secret / token
   - account number
   - RDS password / RDS endpoint hostname
   - account-id / actual IAM Role ARN / actual secret ARN / IAM access key id
   - instance-id / EIP / image digest full sha256 / task ARN / job ARN
3. AWS / SSM / EC2 / ECS / Batch / IAM / Secrets Manager / SSM Parameter Store / RDS / KIS calls are all performed directly by the operator. Kiro only performed documentation / procedure organization / validation item organization.
   - 0 AWS CLI / boto3 executions
   - 0 AWS resource creation / modification / deletion
   - 0 plaintext records of `secretsmanager:GetSecretValue` result values
   - 0 plaintext quotations of CloudWatch Logs body / SSM response body / KIS API response body / full operator PowerShell stdout
4. 0 plaintext secret outputs in the wrapper summary / overrides JSON / SSM stdout · stderr files (consistent with the R-DOCS-001 [2026-06-17 wrapper reinforcement] mitigation / uses only `/tmp/inject-env.sh` v5 env injection / 0 plaintext value outputs / checks only key presence / length).
5. 0 broker / KIS calls (during this wrapper work). 0 new BUY / SELL / cancel / modify / `--execute`. 0 SELL position `mark_position_sell_ordered()` calls. 0 fill · position sync automatic retries.
   - live automatic BUY / SELL E2E validation is still prohibited until follow-up validation / approval per the OD-SAFE-002 / OD-SAFE-003 policy.
   - This wrapper work is limited to `aws-paper` / 0 aws-live work / the wrapper environment input itself allows only `aws-paper`.
6. 0 RDS DDL. DML is limited to the Step 1 standalone SSM first validation.
   - `connector.connector_balance_snapshot` insert 1
   - `connector.connector_position_snapshot` 0 held stocks handling
   - `legacy.holdings` storage is limited to consistency with the 03 spec Step 1 flow

   Otherwise the 17-step DryRun has 0 ECS RunTask / Batch SubmitJob / SSM command submissions, so 0 RDS writes. Not-executed items:
   - 0 wrapper-based full 1~17 actual re-executions
   - 0 uses of Step 12 `-AllowPaperOrderExecute`
   - 0 validations of Step 2 actual execution when the Windows KRX crawler worker is stopped (skip behavior code exists / actual scenario validation is a follow-up)
   - bundled wrapper not created
7. Only the operational identifiers below are recorded as facts consistent with the user-specified policy — not secrets.
   - wrapper file names / step file names
   - cluster `portfolio-paper-cluster`
   - Task Definition family·revision
   - Job Queue · Job Definition family · revision
   - Log Group names
   - run folders under `C:\Temp\portfolio-daily-aws-paper`
   - the 20 parser validation files

## 2026-06-18 (Daily AWS Paper Wrapper 17-step live operational validation — 03 spec responsibility steps)

Accumulates, among the Step 1 ~ Step 17 real executions of the Daily AWS Paper Wrapper (`.kiro/scripts/run-daily-aws-paper.ps1`) that the operator performed directly on the same date, the results of the 03 spec (MarketConnector EC2) responsibility steps Step 1 / Step 12 / Step 13 / Step 17.

- Step 2 / Step 3 = 08 spec
- Step 4 / Step 5 = 09 spec
- Step 6 ~ Step 11 / Step 14 ~ Step 16 = consistent with the 2026-06-18 section of the 04 spec operation-notes

Environment `aws-paper` / RunDate `2026-06-18` / region `ap-northeast-2` / first use of Step 12 `-AllowPaperOrderExecute`.

Kiro only performed documentation / procedure organization. The actual wrapper execution / SSM RunCommand / KIS API calls / operator direct patch / Docker rebuild / ECR push / ECS re-execution were all carried out directly by the operator. The actual broker / KIS calls are limited to 4 KIS paper BUY (Step 12) + balance · order check query-only / 0 SELL · cancel · modify calls / 0 aws-live work.

### 1. Step 1 `CONNECTOR_BALANCE`

1. Execution form: complete
   1) Entered via wrapper `-StartStep 1 -EndStep 1` or as part of the integrated 1~17 execution (operator direct decision)
   2) Execute `connector_balance.py` in the venv python environment via MarketConnector EC2 SSM RunCommand
   3) `/tmp/inject-env.sh` v5 env injection used (JSON SecretString internal key extraction + `APP_*` compatibility key + `KIS_*` alias simultaneous export)
2. Execution result: success
   1) SSM RunCommand `Status: Success` / `ResponseCode: 0`
   2) `connector.connector_balance_snapshot` new row stored (consistent with the 03 spec body responsibility flow)
   3) `connector.connector_position_snapshot` update / `legacy.holdings` update are recorded consistent with the Step 17 real execution result (§4)
   4) 0 new broker / KIS order calls / limited to balance query

### 2. Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` — first use of `-AllowPaperOrderExecute`

This step is the first case where the operator explicitly used the `-AllowPaperOrderExecute` option after the wrapper structure first establishment (2026-06-17 OD-MS-023). Accumulates the fact of use / affected rows / the controlled recovery procedure in a form consistent with the R-AUTO-019 mitigation.

1. Execution form: complete
   1) Entered explicitly via wrapper `-StartStep 12 -EndStep 12 -AllowPaperOrderExecute` (consistent with R-AUTO-019 mitigation — option default OFF / entered only when the operator explicitly specifies the option)
   2) Passed both the wrapper central PAPER_ORDER_GATE + Step 12 internal double gate
   3) Passed the `aws-paper` environment + `PORT_DB_TARGET=aws-paper` guard
   4) Called `connector_strategy_order_execute.py --execute` in the venv python environment via MarketConnector EC2 SSM RunCommand
2. First attempt — KIS paper API read timeout: confirmed
   1) Basic network / DNS / TCP / HTTPS connection confirmed normal — judged a transient latency fault of the KIS paper endpoint (consistent with the new R-BROKER-004)
   2) `connector.connector_order_request` id `38 ~ 41` created then FAILED
   3) `execution.strategy_execution_order` id `30 ~ 33` handled as FAILED
   4) Confirmed the `response_status` / response timeout pattern / absence of `broker_order_no` in `connector.connector_api_call_log`
3. Controlled REQUESTED recovery: complete (first demonstration of R-BROKER-004 mitigation)
   1) Consistent with the no-simple-re-execution policy — pre-check the presence of `connector_order_request` / `connector_api_call_log` / `broker_order_no`
   2) After confirming the absence of `broker_order_no`, the operator directly SQL-corrected the affected strategy_execution_order to REQUESTED, the state just before SUBMITTED
   3) No wrapper bypass / uses only operator direct SQL
4. Retry result: success
   1) Re-entered wrapper Step 12 (with the `-AllowPaperOrderExecute` option as-is)
   2) 4 KIS paper BUY submissions succeeded
   3) Stock / mapping:
       - `004990` 롯데지주 — `strategy_execution_order id 30` SUBMITTED / `connector_order_request id 42` ACCEPTED / `broker_order_no 0000025576`
       - `023530` 롯데쇼핑 — `strategy_execution_order id 31` SUBMITTED / `connector_order_request id 43` ACCEPTED / `broker_order_no 0000025740`
       - `003490` 대한항공 — `strategy_execution_order id 32` SUBMITTED / `connector_order_request id 44` ACCEPTED / `broker_order_no 0000025744`
       - `042660` 한화오션 — `strategy_execution_order id 33` SUBMITTED / `connector_order_request id 45` ACCEPTED / `broker_order_no 0000025747`
   4) All 4 paper responses normal / 0 SELL · cancel · modify calls / 0 aws-live work
   5) 0 duplicate broker orders — all 4 broker_order_no unique / consistent normal per R-BROKER-004 mitigation
5. Follow-up handover: follow-up
   1) Formal documentation of the KIS paper API timeout retry policy (03 spec runbook §4.2 or a follow-up phase deliverable / followups-overview 2026-06-18 §2)
   2) Automatic recording of the timeout / recovery / retry labels in the wrapper Step 12 stdout (wrapper run summary reinforcement / followups-overview 2026-06-18 §5)

### 3. Step 13 `CONNECTOR_ORDER_CHECK` — broad → single fallback skip first demonstration

This step is the result of first demonstrating whether the 2026-06-17 17-step E2E summary fallback guard patch (R-AUTO-018 mitigation) automatically operates under this date's multi active-candidate situation.

1. broad query result: confirmed
   1) wrapper Step 13 `connector_order_check.py` call (MarketConnector EC2 SSM)
   2) KIS `inquire-daily-ccld` response — `output1 empty` + `output2 summary-only`
   3) active_order_candidates = 4 (consistent with Step 12's connector_order_request id `42 ~ 45` ACCEPTED)
2. summary fallback automatic skip: complete (consistent with R-AUTO-018 [2026-06-18 reinforcement])
   1) The summary fallback guard automatically skips under the active_order_candidates >= 2 condition
   2) 0 incorrect mappings of `connector.connector_order_event` / `connector.connector_fill` / `connector_order_request status`
   3) The wrapper Step 13 stdout outputs the `summary fallback skipped (active_order_candidates=4)` label
3. Single re-query: complete
   1) Proceeded with `--code` / `--order-no` / `--no-broad` single re-query 4 times
   2) All 4 reflected fills normally
   3) Fill results:
       - `004990` 롯데지주 4 shares filled → `connector_order_event` / `connector_fill` created
       - `023530` 롯데쇼핑 8 shares filled → same as above
       - `003490` 대한항공 6 shares filled → same as above
       - `042660` 한화오션 11 shares filled → same as above
   4) `connector_order_request 42 ~ 45` all transitioned to FILLED
4. Follow-up handover: follow-up
   1) Add an automatic single-query loop call within the wrapper when active_order_candidates >= 2 is detected after a broad query — followups-overview 2026-06-18 §1 follow-up

### 4. Step 17 `BALANCE_REFRESH` — view_app legacy schema permission bypass case

1. Execution result: success
   1) wrapper Step 17 `connector_balance.py` call (MarketConnector EC2 SSM)
   2) SSM commandId `66ec8831-74b9-469c-8410-6ccb11cb3400`
   3) responseCode `0`
   4) Status `Success`
   5) `connector.connector_position_snapshot` row_count `36` / max_created_at `2026-06-18 04:33:07.456056+00`
   6) `legacy.holdings` row_count `41` / max_created_at `2026-06-18 04:33:07.420226`
2. Permission bypass case at result confirmation: confirmed
   1) view_app lacks `legacy` schema USAGE / `legacy.holdings` SELECT permission (consistent with OD-DB-007 — the policy that all app roles are ungranted for the legacy schema / only `marketconnector_app` holds 1 exceptional GRANT via R-DATA-011 mitigation)
   2) At Step 17 result confirmation, `permission denied` occurred when querying `legacy.holdings` with view_app
   3) The operator bypass-queried with `portfolio_admin` — operational validation passed / but whether view_app's formal GRANT is needed is an operator decision follow-up (consistent with R-DATA-005 [2026-06-18 reinforcement])
3. Safety check: complete
   1) The Step 17 execution itself succeeded — this case is a result-confirmation permission problem and has no impact on the wrapper / this step's behavior itself
   2) Whether to add view_app's `legacy.holdings` SELECT permission is separated as a 02 spec db-roles-and-grants follow-up phase responsibility (followups-overview 2026-06-18 §4)
4. Final OPEN positions (consistent with the Step 16 result + Step 17 snapshot): confirmed
   1) `003490` 대한항공 — 58 shares / entry_price `28979.3103` / `position_state_id 8`
   2) `004990` 롯데지주 — 69 shares / entry_price `27008.6956` / `position_state_id 7`
   3) `023530` 롯데쇼핑 — 8 shares / entry_price `194225.0000` / `position_state_id 11` (new)
   4) `042660` 한화오션 — 11 shares / entry_price `126118.1818` / `position_state_id 12` (new)
   5) `088350` 한화생명 — 244 shares / entry_price `5744.4057` / existing OPEN maintained
   6) `282330` BGF리테일 — 17 shares / entry_price `120182.3529` / existing OPEN maintained
   7) 2 additional-buy merges (004990 / 003490) + 2 new OPEN (023530 / 042660) — new OD-MS-024 / first demonstration of R-DATA-012 mitigation

### 5. Safety / security check result (Daily AWS Paper Wrapper 17-step live operational validation)

1. Within this date's work scope, broker / KIS calls are limited to 4 KIS paper BUY (Step 12 real execution) + balance · order check query-only calls (Step 1 / Step 13 / Step 17). 0 SELL / cancel / modify / additional `--execute` calls. 0 SELL position `mark_position_sell_ordered()` calls (consistent with OD-MS-016 — no SELL flow occurred on this date). 0 aws-live work.
2. 0 RDS DDL. DML is limited to this date's normal 17-step flow.
   - `connector.connector_order_request` insert (id `38 ~ 41` FAILED + `42 ~ 45` ACCEPTED)
   - `connector.connector_order_event` insert (4 fills)
   - `connector.connector_fill` insert (4 fills)
   - `execution.strategy_execution_order` update (id `30 ~ 33` FAILED → REQUESTED → SUBMITTED → FILLED)
   - `connector.connector_balance_snapshot` insert
   - `connector.connector_position_snapshot` insert (row_count 36)
   - `legacy.holdings` insert (row_count 41)

   Operator direct SQL correction — strategy_execution_order id `30 ~ 33` FAILED → REQUESTED recovery (consistent with R-BROKER-004 mitigation / no wrapper bypass / uses only operator direct SQL).
3. The sensitive information below has 0 plaintext records in this note — all use `[REDACTED]` or a placeholder.
   - actual secret value / KIS app key / KIS app secret / token / KIS paper login credential
   - account number / account password
   - RDS password / RDS endpoint hostname
   - account-id / actual IAM Role ARN / actual secret ARN / IAM access key id
   - instance-id / EIP / image digest full sha256 / task ARN / job ARN
4. The 4 KIS paper `broker_order_no` (`0000025576` / `0000025740` / `0000025744` / `0000025747`) are broker response values that are operational identifiers — not real-account order numbers / consistent with this note's fact record. SSM commandId `66ec8831-74b9-469c-8410-6ccb11cb3400` is also recorded as a fact as an operational identifier.
5. The diff of `port_strategy_execution/execution_sync_buy_position.py` that the operator patched / formally deployed directly is recorded in the 04 spec operation-notes only as a fact (0 full-body quotations / consistent with R-DOCS-001). 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to this date's work (spec area).
6. AWS / SSM / EC2 / ECS / Batch / IAM / Secrets Manager / SSM Parameter Store / RDS / GRANT / KIS / S3 / CloudWatch / Docker / ECR calls are all performed directly by the operator. Kiro only performed documentation / procedure organization / validation item organization.
   - 0 AWS CLI / boto3 executions
   - 0 AWS resource creation / modification / deletion
   - 0 plaintext records of `secretsmanager:GetSecretValue` result values
   - 0 plaintext quotations of CloudWatch Logs body / SSM response body / KIS API response body / Docker build · push logs / full operator PowerShell stdout
7. The operational identifiers below are recorded as facts consistent with the user-specified policy — not secrets.
   - connector_order_request id `38 ~ 41` (FAILED) + `42 ~ 45` (ACCEPTED)
   - strategy_execution_order id `30 ~ 33` / broker_order_no 4 kinds
   - position_state_id `7` · `8` · `11` · `12` / SSM commandId
   - `connector.connector_position_snapshot` row_count `36` / `legacy.holdings` row_count `41`
   - stock codes `004990` · `023530` · `003490` · `042660` · `088350` · `282330` / stock names / quantities / entry_price
   - data_date `2026-06-17` / signal_date · run_date `2026-06-18`

## 2026-06-18 (Step 13 `connector_order_check.py` per-order sequential query default — operational safety strengthening)

This section accumulates, in this spec's operation notes, the result of the change to the default execution mode of MarketConnector `connector_order_check.py` (broad batch fill query → per-active-order single direct-only sequential query) that the operator performed directly as a follow-up to the same date's (2026-06-18) first session (Daily AWS Paper Wrapper 17-step live operational validation complete).

Not an operational failure but a design change to strengthen operational safety. Reflects the first session's broad-query `output1 empty` + `output2 summary-only` + 4 active candidate response pattern (first demonstration of the R-AUTO-018 [2026-06-18 reinforcement] mitigation) result into a safer default behavior at the code level.

This work falls under the following and is consistent with the new decision lock OD-MS-025.

- Operational safety strengthening of the 03 spec MarketConnector EC2
- Clarifying the standalone execution responsibility boundary of Step 13 of the 17-step Daily AWS Paper Wrapper

### 1. Changed target file + default execution mode change: complete

1. Changed target file: confirmed
   1) `port-marketconnector/connector_order_check.py`
   2) EC2 live operational path: `/home/ec2-user/apps/port-marketconnector/src/connector_order_check.py`
2. Default execution mode: change complete
   1) Before — broad-fill-query centered (a risky structure that, when there are many active order candidates and the response is `output1 empty` + `output2 summary-only`, could incorrectly map the summary value to the last order row or an arbitrary single order / the R-AUTO-018 first-demonstration pattern)
   2) After — after querying the active order list, per-order single direct-only sequential query based on order number / stock code (fill synchronization only via per-broker_order_no single query even in a multi active-order state / reduced summary fallback exposure surface)
3. Distinguishing operational failure / operational safety strengthening: confirmed
   1) This change is not operational failure recovery but a design change to strengthen operational safety
   2) In addition to the first session's in-wrapper automatic-skip first demonstration on the same date, the default behavior of `connector_order_check.py` itself is changed one step further in a safer direction
   3) The wrapper ps1 responsibility is orchestration only (`.kiro/scripts/steps/step-13-connector-order-check.ps1` = SSM RunCommand call / environment validation / log storage) / fill-query method control (active query / single direct-only / broad mode separation) = internal responsibility of `connector_order_check.py` — consistent with OD-MS-025

### 2. New options + operational mode policy: complete

1. New options: addition complete
   1) `--broad` — legacy broad query mode / not used by default / limited to diagnostic · legacy use / the operational default mode is active-order single sequential query
   2) `--active-limit` — maximum processing count of active-order single sequential query (`--active-limit` default value decision follow-up / followups-overview 2026-06-18 §6)
   3) `--code` / `--order-no` / `--no-broad` — an explicit order-query combination that queries only that order without broad fallback
2. Operational mode policy: confirmed
   1) Default execution (`python connector_order_check.py`) — after querying the active order list, per-order single direct-only sequential query based on order number / stock code
   2) legacy broad batch query (`python connector_order_check.py --broad`) — limited to diagnostic · legacy use
   3) Explicit order query (`python connector_order_check.py --code {ticker_code} --order-no {broker_order_no} --no-broad`) — single direct-only / queries only that order without broad fallback
   4) summary fallback allow condition — allowed only when the `connector_order_request` candidate is confirmed as 1 by order number / stock code / does not use the `output1 empty` + `output2 summary-only` response as a DB-reflection basis in a multi active state (consistent with the R-AUTO-018 [2026-06-18 additional reinforcement] mitigation)

### 3. Validation milestone (2026-06-18 second session): complete

1. Local validation: passed
   1) `python -m py_compile connector_order_check.py` passed
   2) Confirmed the `--broad` / `--active-limit` options display in `python connector_order_check.py --help`
2. Local commit: complete
   1) commit hash `75cb804`
   2) commit message `fix(connector): run order checks sequentially per active order`
   3) The current repository's remote push destination is unset → git push not performed
   4) Git remote setup + formal push of commit `75cb804` is separated as a follow-up (followups-overview 2026-06-18 §1)
3. EC2 reflection (via S3): complete
   1) S3 bucket `portfolio-paper-migration-yukiever`
   2) S3 key `deploy/marketconnector/connector_order_check.py`
   3) File size `39159 bytes`
   4) EC2 instance id `i-0fce77927b7397b88` (OD-NET-010 standard SSM Port Forwarding standard waypoint / 03 spec formal operational EC2)
   5) EC2 backup file name `connector_order_check.py.bak-20260618-step13-per-order` (consistent with the original-preservation policy)
   6) SSM commandId `b344d404-07a4-4bb6-9d63-34151e648bab` (SSM RunCommand response identifier)
4. Wrapper standalone execution validation: passed
   1) Command `-StartStep 13 -EndStep 13` (StartStep / EndStep — risk of entering default 1/17 on a FromStep / ToStep mistake / explicit wrapper parameters)
   2) `connector_order_event` / `connector_fill` created normally based on 1 single direct-only query (`004990` / broker_order_no `0000025576`)
   3) 0 new broker · KIS order submissions / all query-only calls

### 4. Decision / risk change summary: complete

1. New decision: complete
   1) OD-MS-025 (MarketConnector `connector_order_check.py` operational mode = active-order single sequential query default + broad option isolation + `connector_order_check.py` internal branching, 🟡 잠정)
2. Decisions with no body change: only first-demonstration memo reinforced
   1) OD-MS-016 (Strategy Execution / MarketConnector responsibility separation) — MarketConnector internal operational mode change / Strategy Execution-side responsibility boundary as-is / Status keeps its existing value
   2) OD-MS-021 (Backend AWS E2E dry-run 17-step order + safety baseline) — no change to the 17-step order / safety baseline / behavior change validated via Step 13 standalone execution / Status keeps its existing value
   3) OD-MS-023 (Daily AWS wrapper operational policy) — the wrapper ps1 responsibility is orchestration only / fill-query method control is clarified as internal responsibility of `connector_order_check.py` / Status keeps its existing value
3. New risks: 0
4. Risks with no body change: detection / mitigation memo reinforced
   1) R-AUTO-018 (KIS `inquire-daily-ccld` summary-only fallback mismapping) — [2026-06-18 additional reinforcement] memo added.
       - In addition to the first session's in-wrapper automatic-skip first demonstration on the same date, the default execution mode of `connector_order_check.py` itself is changed to active-order single sequential query.
       - The broad-fill-query call frequency itself decreases, and the summary fallback exposure surface in a multi active state decreases.
       - Status `Mitigated` maintained (verified on the 2026-06-18 second session).

### 5. Safety / security check result (Step 13 `connector_order_check.py` per-order sequential query default): complete

1. broker / KIS call scope: 1 single direct-only query (`004990` / `0000025576`) + 1 wrapper Step 13 standalone execution (`-StartStep 13 -EndStep 13`) — all query-only. 0 new BUY · SELL · cancel · modify · `--execute` calls. 0 SELL position `mark_position_sell_ordered()` calls. 0 fill · position sync automatic retries. 0 aws-live work.
2. 0 RDS DDL. DML is limited to the Step 13 normal flow — the new `connector.connector_order_event` / `connector.connector_fill` rows are consistent with the single direct-only query result.
3. The sensitive information below has 0 plaintext records in this note — all use `[REDACTED]` or a placeholder.
   - actual secret value / KIS app key / KIS app secret / token / KIS paper login credential
   - account number / account password
   - RDS password / RDS endpoint hostname
   - account-id / actual IAM Role ARN / actual secret ARN / IAM access key id
   - EIP / image digest full sha256 / task ARN / job ARN
4. AWS / SSM / EC2 / S3 / IAM / Secrets Manager / SSM Parameter Store / RDS / KIS calls are all performed directly by the operator. Kiro only performed documentation / procedure organization / validation item organization.
   - 0 AWS CLI / boto3 executions
   - 0 AWS resource creation / modification / deletion
   - 0 plaintext records of `secretsmanager:GetSecretValue` result values
   - 0 plaintext quotations of CloudWatch Logs body / SSM response body / KIS API response body / full operator PowerShell stdout
5. The diff of `port-marketconnector/connector_order_check.py` that the operator patched directly is recorded in this note only as a fact (0 full-body quotations / consistent with R-DOCS-001). 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to this date's second-session work (spec area).
6. Uses wrapper parameters `StartStep` / `EndStep` (risk of entering default 1/17 on a FromStep / ToStep mistake) — this date's standalone execution proceeded with `-StartStep 13 -EndStep 13` / consistent with the explicit-wrapper-parameter policy.
7. The operational identifiers below are recorded as facts consistent with the user-specified policy — not secrets.
   - commit `75cb804`
   - S3 bucket `portfolio-paper-migration-yukiever`
   - S3 key `deploy/marketconnector/connector_order_check.py`
   - file size `39159 bytes`
   - EC2 instance id `i-0fce77927b7397b88`
   - EC2 backup file name `connector_order_check.py.bak-20260618-step13-per-order`
   - SSM commandId `b344d404-07a4-4bb6-9d63-34151e648bab`
   - stock code `004990` / broker_order_no `0000025576`
   - wrapper options `--broad` · `--active-limit` · `--code` · `--order-no` · `--no-broad`
   - wrapper parameters `StartStep` · `EndStep`

   instance id is the standard SSM Port Forwarding standard waypoint identifier already recorded as a fact in OD-NET-010.

### 6. Follow-up work (carried over from the 2026-06-18 second session): planned

1. Git remote push destination setup + formal push of commit `75cb804`: planned
   1) The current `port-marketconnector` repository's remote push destination is unset
   2) Register origin + push after the operator's decision
   3) Organize together with the formal origin policy at the point of 07 spec CI/CD pipelines integration
2. Step 13 wrapper standalone execution summary output reinforcement: planned
   1) Automatically record `active_order_count` / `single_check_success_count` / `single_check_failed_count` / `broad_mode_used` in `summary/run-summary.txt`
   2) On failure, automatically record stock code / order number / reason (consistent with R-AUTO-018 detection / post-hoc verification possible with the operator PowerShell summary alone)
3. Per-order display when integrating the View Daily Batch screen: planned
   1) At the point of entering 05 spec, decide whether to display the single direct-only query result on the screen at the per-order row unit
   2) Review the consistency between the wrapper summary format and the screen format
4. Organize the Windows PowerShell · AWS CLI SSM output cp949 encoding avoidance pattern: planned
   1) Possibility of cp949 vs UTF-8 encoding conflict when the wrapper Step 13 stdout / SSM response body includes Korean labels / file names
   2) Maintain the 0 cp949 corruption policy in operator notes / spec deliverable bodies (consistent with R-DOCS-001)
5. Codify the KIS paper API timeout retry policy (combined with the 2026-06-18 first session §2 followups §2): planned
   1) Because the single direct-only query pattern is controlled by `--no-broad` + `--code` + `--order-no`, synchronize only via per-broker_order_no single query even at the timeout retry point
   2) Formal procedure entry in 03 spec runbook §4.2 follow-up
6. `--active-limit` default value decision follow-up: planned
   1) Apply the default value at this date's first-demonstration point
   2) Decide the processing-count ceiling policy when the number of active orders accumulates in the operational environment

## IAM change record template (add per date as needed)

```
## YYYY-MM-DD IAM 변경
- 변경 일자: YYYY-MM-DDTHH:MM:SS+09:00
- 변경자: [운영자 식별자]
- 변경 사유: [한 줄]
- 변경 전 / 후 항목 요약: [추가·삭제 statement 수, Action·Resource 변경 요약 — JSON 본문 전체 인용 금지]
```


## 2026-06-22 MarketConnector env bootstrap regeneration + Paper SELL order submission validation

Accumulates, among the second live operational execution of Daily AWS Paper Wrapper 1 ~ 17 that the operator performed directly on 2026-06-22, the results of this spec's responsibility steps (Step 1 / Step 12 / Step 13 / Step 17).

Flow summary:

- Step 1 first failure cause identified (`/tmp/inject-env.sh not found`)
- After adding the `daily-aws-paper.functions.ps1` MarketConnector env bootstrap function, Step 1 re-execution succeeded
- Step 12 한화생명 244 shares SELL_HARD_STOP MARKET submission succeeded
- Step 13 fill query succeeded
- Step 17 BALANCE_REFRESH Success

On this date Kiro only performed documentation / procedure organization. The actual wrapper execution / EC2 / SSM / KIS / RDS / GRANT work was all carried out directly by the operator. This date is limited to `aws-paper` / 0 aws-live work.

This work is not a simple dry-run but the **first end-to-end operational result** that passed all the way from actual Paper SELL order submission / KIS acceptance / fill query / SELL fill sync / position CLOSED / balance refresh.

### 1. Step 1 `CONNECTOR_BALANCE` first failure cause analysis + bootstrap patch

1. First failure pattern: confirmed
   1) The SSM RunCommand response has a `/tmp/inject-env.sh: not found` or equivalent env missing message
   2) The MarketConnector EC2's `/tmp` directory is ephemeral / volatilized after EC2 stop · start
   3) The existing SSM steps were written on the assumption that the env file pre-exists on the EC2 (on previous dates it was maintained by the operator's direct authoring of the v5 env injection)
2. Operator direct patch: complete
   1) Target file = operator local wrapper `daily-aws-paper.functions.ps1` (`.kiro/scripts/` area / operator tooling / outside this spec's source area)
   2) Newly added a MarketConnector env bootstrap function / common call from Step 1 / Step 12 / Step 13 / Step 17 (consistent with the new OD-MS-027)
   3) bootstrap flow — `secretsmanager:GetSecretValue` call → JSON SecretString parse → internal key (`APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD` / `BASE_URL`) extraction → `APP_*` compatibility key + `KIS_*` alias simultaneous export → `/tmp/inject-env.sh` chmod 700 / memory export only / 0 plaintext outputs of the secret value (consistent with R-DOCS-001)
   4) After PowerShell parser validation passed, Step 1 re-execution — passed with just one re-execution after the first failure / 0 plaintext quotations of stdout body in this note
3. This patch's operational policy consistency: confirmed
   1) OD-MS-027 new — abolish the `/tmp/inject-env.sh` pre-existence assumption / regenerate at step execution time / wrapper common function call / no secret value output
   2) Consistent with the new R-AUTO-021 mitigation — first operational-stage blocking of the Step 1 / 12 / 13 / 17 failure risk due to `/tmp` volatilization after MarketConnector EC2 stop · start
   3) 0 changes to this spec's source code / operational entrypoints (`connector_balance.py` / `connector_order_check.py` / `connector_strategy_order_execute.py`) — the operator's direct patch is limited to the wrapper area

### 2. Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` Paper SELL submission

1. Pre-check before Step 12 entry (consistent with 03 spec runbook / safety gate): complete
   1) execution plan id `96` creation confirmed.
       - `plan_date 2026-06-22` / `strategy_name strategy_ai`
       - `market_signal DEFENSIVE` / `risk_regime DEFENSIVE`
       - `plan_status PARTIALLY_BLOCKED`
       - total_candidate 3 / ready 1 / blocked 2 / skipped 0
       - total_target_amount `1,237,080` / available_cash `-62,763` / max_order_amount `-31,381.50`
   2) Step 12 target = 1 `088350` 한화생명 SELL 244 shares MARKET (the 2 BUYs were handled as BLOCKED due to insufficient cash)
   3) Duplicate order check — 0 rows in `connector.connector_order_request` targeting the same strategy_execution_order
2. Sell reason (SELL_HARD_STOP) consistency confirmation: complete
   1) entry_date `2026-06-17` / entry_price `5,744.4057` / current_price `5,070`
   2) expected_pnl_rate approx `-11.7402%` / hard_stop_loss_rate `-10%` / holding_days 5
   3) snapshot_qty 244 / sellable_qty 244 / remaining_qty 244
   4) expected_pnl_amount approx `-164,554.9908`
   5) The entry_qty 244 / remaining_qty 244 of the 한화생명 OPEN position based on `strategy_position_state id 9` matches the Step 12 SELL quantity 244
3. `-AllowPaperOrderExecute` explicit execution + result: complete
   1) wrapper PAPER_ORDER_GATE passed — `PaperOrder: True` label output (consistent with R-AUTO-019 mitigation)
   2) `execution_order id 37` status `SUBMITTED`
   3) `connector_order_request id 46` created / request_type SELL / order_method MARKET / order_qty 244 / request_status `ACCEPTED`
   4) broker_order_no created / broker_branch_code created — 0 plaintext records of the broker response value in this note / consistent with `[REDACTED]` handling (consistent with R-DOCS-001)
   5) No rejection / KIS paper API normal response / first attempt passed (0 regressions of the 2026-06-18 KIS paper read timeout / consistent with R-BROKER-004 mitigation)

### 3. Step 13 `CONNECTOR_ORDER_CHECK` fill synchronization

1. KIS `inquire-daily-ccld` call: success
   1) Uses the `connector_order_check.py` 2026-06-18 patch (`--code` / `--order-no` / `--no-broad` single direct-only query default / consistent with OD-MS-025) as-is
   2) Single active candidate (`088350` SELL 1) consistent — no summary fallback guard entry
2. Fill result: complete
   1) `connector_order_request id 46` request_status `FILLED`
   2) `connector_fill id 34` newly created / fill_qty 244 / fill_price `5,075.8607` / fill_amount `1,238,510.01` / side SELL / fill_ts `2026-06-22 00:46:58 UTC`
   3) `connector_order_event` new row consistent

### 4. Step 17 `BALANCE_REFRESH`

1. SSM execution result: complete
   1) SSM Status `Success` / ResponseCode `0` / StdErr empty
   2) On this date, 0 regressions after the `marketconnector_app`'s `legacy.holdings` search_path / permission correction (2026-06-17 §4) / R-DATA-011 mitigation maintained as-is
2. Final balance snapshot: confirmed
   1) `connector.connector_position_snapshot` latest `created_at 2026-06-22 00:50:50 UTC`
   2) 5 held stocks — `003490` 대한항공 58 shares / `004990` 롯데지주 69 shares / `023530` 롯데쇼핑 8 shares / `042660` 한화오션 11 shares / `282330` BGF리테일 17 shares
   3) `088350` 한화생명 confirmed removed from the latest balance snapshot (consistent with the Step 12 SELL liquidation)
   4) `legacy.holdings` result confirmation was bypass-queried by the operator with `portfolio_admin` due to view_app permission absence (consistent with R-DATA-005 [2026-06-22 reinforcement] / formal GRANT follow-up)

### 5. Decision / risk change summary

1. New decisions: complete
   1) OD-MS-027 (MarketConnector env bootstrap regeneration operational policy, 🟡 잠정)
   2) OD-DB-011 (`execution_app`'s `decision.strategy_daily_position_decision` limited UPDATE permission, 🟢 확정)
2. Decisions with no body change: first-demonstration memo reinforced
   1) OD-MS-016 — the Strategy Execution `READY -> REQUESTED` / MarketConnector `REQUESTED -> SUBMITTED` responsibility separation is first demonstrated even limited to 1 SELL
   2) OD-MS-021 — consistent with the 17-step safety baseline / 0 safety-baseline violations other than the Step 9 permission correction
   3) OD-MS-023 — the wrapper 1 ~ 17 second real full run / second use of `-AllowPaperOrderExecute` (2026-06-18 BUY 4 / 2026-06-22 SELL 1)
3. New risks: complete
   1) R-AUTO-021 (Step 1 / 12 / 13 / 17 failure risk due to `/tmp` volatilization after MarketConnector EC2 stop / start, Status `Mitigated` — first blocking via the wrapper bootstrap function)
   2) R-DATA-013 (Step 9 SELL execution link update failure risk due to `execution_app`'s missing `decision` schema UPDATE permission, Status `Mitigated` — operator direct GRANT correction)
4. Risks with no body change: reinforcement memo
   1) R-DATA-005 — [2026-06-22 reinforcement] the `execution_app`'s missing `decision` schema USAGE / `decision.strategy_daily_position_decision` UPDATE was additionally identified as the Step 9 first failure cause / 02 spec db-roles-and-grants formal matrix update follow-up maintained

### 6. Safety / security check result (2026-06-22)

1. broker / KIS calls = 1 KIS paper SELL (Step 12 real execution) + balance · order check query-only. 0 BUY / cancel / modify / additional `--execute` calls. The SELL position `mark_position_sell_ordered()` call is the responsibility of the MarketConnector executor SELL success point (consistent with OD-MS-016). 0 fill · position sync automatic retries. 0 aws-live work.
2. 0 RDS DDL. DML is limited to the normal 17-step flow — `connector.connector_order_request` insert (`id 46` ACCEPTED → FILLED) / `connector.connector_order_event` / `connector.connector_fill` insert (`id 34`) / `connector.connector_balance_snapshot` insert / `connector.connector_position_snapshot` insert (5 held stocks).
3. The sensitive information below has 0 plaintext records in this note — all use `[REDACTED]` or a placeholder.
   - actual secret value / KIS app key / KIS app secret / token / KIS paper login credential
   - account number / account password
   - RDS password / RDS endpoint hostname
   - account-id / actual IAM Role ARN / actual secret ARN / IAM access key id
   - instance-id / EIP / image digest full sha256 / task ARN / job ARN
   - broker_order_no raw / broker_branch_code raw
4. AWS / SSM / EC2 / RDS / Secrets Manager / SSM Parameter Store / KIS API calls are all performed directly by the operator. Kiro only performed documentation / procedure organization / validation item organization.
   - 0 AWS CLI / boto3 executions
   - 0 AWS resource creation / modification / deletion
   - 0 plaintext records of `secretsmanager:GetSecretValue` result values
   - 0 plaintext quotations of CloudWatch Logs body / SSM response body / KIS API response body / full operator PowerShell stdout
5. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to this date's work (spec area). The diff of the `daily-aws-paper.functions.ps1` MarketConnector env bootstrap function that the operator patched directly is recorded in this note § 1 only as a fact (0 full-body quotations / consistent with R-DOCS-001 / `.kiro/scripts/` area).
6. The operational identifiers below are recorded as facts consistent with the user-specified policy — not secrets.
   - `execution_plan_id 96` / execution_order id `37`
   - connector_order_request id `46` / connector_fill id `34`
   - position_state_id `9`
   - stock codes / stock names / quantities · prices · ratios
   - data_date / signal_date · run_date `2026-06-22`
   - `fill_ts 2026-06-22 00:46:58 UTC` / `created_at 2026-06-22 00:50:50 UTC`

## 2026-06-23 Step 12 retry-normalizer embedded + Step Functions approval true path Paper SELL E2E

Accumulates, among the 4 tasks below that the operator performed directly on 2026-06-23, the results of this spec's responsibility steps (Step 12 / Step 13 / Step 17).

- (a) Daily AWS Paper Step Functions state machine `portfolio-paper-daily-step1-17-approval` live validation
- (b) Step 12 `connector_strategy_order_execute.py` full replacement + Step 12 head retry-normalizer embedded
- (c) Step 12 ~ Step 17 `allowPaperOrderExecute=true` approval true path first real SELL E2E pass
- (d) connector-side DB Korean storage normal confirmation

Following the 2026-06-17 first BUY 4 full run (first real BUY E2E) / 2026-06-22 SELL 1 full run (first real SELL E2E / wrapper-based), this date is classified as the first real SELL E2E round via the Step Functions approval workflow.

On this date Kiro only performed documentation / procedure organization. The actual Step Functions / EC2 / SSM / KIS / RDS / Secrets Manager / Step 12 entrypoint patch / EC2 formal deployment work was all carried out directly by the operator. This date is limited to `aws-paper` / 0 aws-live work.

The detailed accumulation of the 04 spec-side steps (Step 6 ~ Step 11 Strategy Decision · Execution / Step 14 ~ Step 16 fill sync · position sync) is separated as third-work responsibility — this note is limited to the MarketConnector EC2 responsibility area (Step 12 / Step 13 / Step 17).

### 1. Step 12 retry-normalizer embedded (`connector_strategy_order_execute.py` full replacement + EC2 formal deployment + dry-run pass)

1. First identified pattern (new R-AUTO-022): confirmed
   1) When the KIS broker returns REJECTED + `rejection_code = 40580000` for a `connector.connector_order_request` after market close, that `connector_order_request_id` remains linked to `execution.strategy_execution_order` as-is until the next business day
   2) Step 12's default filter (`execution_status = REQUESTED` + `connector_order_request_id IS NULL`) does not catch that row, so the automatic re-submission path is broken
   3) Without operator direct SQL recovery (`execution_status = REQUESTED` + `connector_order_request_id = NULL`), a state that cannot enter operational automation may occur
   4) Same pattern for both SELL · BUY / because the same entrypoint is called even after the Step Functions transition, this risk applies identically to both wrapper-based / Step Functions-based
2. Operator direct patch: complete
   1) Target file = `port-marketconnector/connector_strategy_order_execute.py` full replacement (outside `.kiro/scripts/` / port-marketconnector area / operator direct patch of this spec's source area — this note is a fact record only / 0 full-body quotations / consistent with R-DOCS-001)
   2) Application location = Step 12 (`MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) head — **not a separate Step 11.5 split / an internal safety supplement of the existing Step 12**
   3) Applied only when all 6 recovery conditions are satisfied.
       - `execution_mode = PAPER_STRATEGY`
       - `action_type IN (BUY, SELL)`
       - `execution_status IN (READY, FAILED)`
       - `connector_order_request_id IS NOT NULL` + linked `connector_order_request.request_status = REJECTED`
       - `rejection_code = 40580000`
       - `broker_order_no IS NULL` + no `connector_fill`
   4) Cases where `broker_order_no IS NOT NULL` or a `connector_fill` already exists are excluded from the recovery target — ensures no conflict with the duplicate order risk of R-BROKER-004 / R-AUTO-001
   5) Recovery handling — `execution_status = REQUESTED` + `connector_order_request_id = NULL` + store the old request history in `result_payload.retry_normalizer` (`old_connector_order_request_id` / `request_status` / `rejection_code` / `recovered_at`, etc., post-hoc-traceable metadata)
3. EC2 formal deployment + dry-run pass: complete
   1) EC2 = `portfolio-paper-marketconnector-ec2` (instance id `i-0fce77927b7397b88` / consistent with OD-NET-010 / this note's operational identifier fact record — not a secret)
   2) Formal deployment flow = operator direct patch → EC2 deployment (same flow as 2026-06-18 §2 — 0 full-body quotations in this note)
   3) `.venv/bin/python` dry-run pass — currently 0 retry candidates / 0 REQUESTED orders / 0 actual R-AUTO-022 candidate occurrences on this date
   4) Step Functions Step 12 also uses `.venv/bin/python` consistently — because the same entrypoint is called even after the Step Functions transition, the retry-normalizer operates as-is
4. Operational policy consistency: confirmed
   1) OD-MS-028 new (Step 12 retry-normalizer embedded policy / 🟡 잠정 / affected spec 03 · 04 · 10 / explicitly not a separate Step 11.5 split)
   2) OD-MS-016 no body change — the Strategy Execution `READY -> REQUESTED` / MarketConnector `REQUESTED -> SUBMITTED` responsibility separation is maintained as-is / the retry-normalizer is classified as a MarketConnector-side Step 12 internal safety supplement
   3) OD-MS-009 / OD-MS-021 / OD-MS-023 no body change — the Daily Batch orchestration / 17-step safety baseline / wrapper operational policy body maintained as-is
   4) R-AUTO-022 new (risk of the automatic re-submission path being broken after post-close REJECTED · `40580000`, Status `Mitigated` / first blocking via this date's dry-run pass — validation of the first round of actual retry candidate occurrence is followups-overview 2026-06-23 §3 follow-up)
   5) This mitigation's automatic recovery target is limited to `40580000` (market close) — other rejection_code (trading suspension / rejection / tick-unit error, etc.) maintain the operator direct-check responsibility (consistent with R-AUTO-001 / R-BROKER-004)

### 2. Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` Step Functions approval true path Paper SELL submission

1. Step Functions approval workflow pre-validation (consistent with the new OD-MS-029): complete
   1) state machine = `portfolio-paper-daily-step1-17-approval` (operator direct definition / this note is a fact identifier record only / 0 full-body quotations of the state definition)
   2) Step 1 ~ Step 17 `allowPaperOrderExecute=false` approval blocked path pre-passed — paper environment dry-run confirmation of the input values (READY / REQUESTED candidates / stock / quantity / reason) of Step 10 / Step 11 / Step 12
   3) After the operator checked the pre-validation result, entered the approved execution of the Step 12 ~ Step 17 `allowPaperOrderExecute=true` approval true path — separated only by the input parameter (`allowPaperOrderExecute`) of the same state machine and the start step branch / no separate state machine split
   4) Maintains the wrapper common MarketConnector env bootstrap function call flow immediately before Step Functions Step 12 entry (consistent with R-AUTO-021 [2026-06-23 reinforcement] / maintains the `/tmp/inject-env.sh` regeneration pattern even after the Step Functions transition)
2. Pre-check before Step 12 entry (consistent with 03 spec runbook §2 / safety gate): complete
   1) Step 12 target = 1 `282330` BGF리테일 SELL 17 shares MARKET — as a result of the 04 spec-side Step 9 ~ Step 11 flow, `execution.strategy_execution_order id 40` entered the REQUESTED state (detailed accumulation is third-work 04 spec operation-notes responsibility)
   2) Duplicate order check — 0 existing rows in `connector.connector_order_request` targeting the same `strategy_execution_order_id 40`
   3) retry-normalizer entry review — no `40580000` REJECTED history / 6 recovery conditions unmet / 0 retry-normalizer applications (consistent with R-AUTO-022 mitigation)
3. `allowPaperOrderExecute=true` approval true path execution + result: complete
   1) Step Functions approval gate passed — consistent with the `PaperOrder: True` label of the Step 12 stdout (consistent with R-AUTO-019 mitigation on the Step Functions side / consistent with the new OD-MS-029)
   2) `execution.strategy_execution_order id 40` status `SUBMITTED` → `FILLED`
   3) `connector.connector_order_request id 48` newly created / request_type SELL / order_method MARKET / order_qty 17 / request_status `ACCEPTED` → `FILLED`
   4) broker_order_no `0000006143` created / broker_branch_code created — 0 plaintext records of the full broker response in this note / consistent with `[REDACTED]` handling (consistent with R-DOCS-001)
   5) No rejection / KIS paper API normal response / first attempt passed (0 regressions of the 2026-06-18 KIS paper read timeout / R-BROKER-004 / 0 retry-normalizer entries on this date)

### 3. Step 13 `CONNECTOR_ORDER_CHECK` fill synchronization

1. KIS `inquire-daily-ccld` call: success
   1) Uses the `connector_order_check.py` 2026-06-18 §2 patch (`--code` / `--order-no` / `--no-broad` single direct-only query default / consistent with OD-MS-025) as-is
   2) Single active candidate (`282330` SELL 1) consistent — no summary fallback guard entry (blocking of the `output2 summary-only` mismapping risk in a multi active state / consistent with R-AUTO-018 mitigation)
2. Fill result: complete
   1) `connector.connector_order_request id 48` request_status `FILLED`
   2) `connector.connector_fill` new row / side SELL / stock `282330` BGF리테일 / normal mapping (detailed fill_qty / fill_price / fill_amount / fill_ts accumulation is separated as third-work 04 spec operation-notes responsibility — this note is limited to the 03 spec MarketConnector EC2 responsibility area)
   3) `connector.connector_order_event` new row consistent

### 4. Step 17 `BALANCE_REFRESH`

1. SSM execution result: complete
   1) SSM Status `Success` / ResponseCode `0` / StdErr empty
   2) Consistent with the wrapper common MarketConnector env bootstrap function call (2026-06-22 §1 / consistent with OD-MS-027 / maintains the bootstrap call flow immediately before Step 17 entry even after the Step Functions transition — consistent with R-AUTO-021 [2026-06-23 reinforcement])
   3) 0 regressions after the `marketconnector_app`'s `legacy.holdings` search_path / permission correction (2026-06-17 §4) / R-DATA-011 mitigation maintained as-is
2. Final balance snapshot: confirmed
   1) `connector.connector_position_snapshot` latest row insert / 4 held stocks reflected normally (consistent with the Step 12 SELL liquidation)
   2) `282330` BGF리테일 confirmed removed from the latest balance snapshot (Step 12 SELL 17 shares liquidation → decreased to 4 held stocks in the `connector_position_snapshot` latest row / consistent with the difference from 2026-06-22 balance 5 stocks → this date 4 stocks)
   3) `legacy.holdings` result confirmation was bypass-queried by the operator with `portfolio_admin` due to view_app permission absence (consistent with R-DATA-005 [2026-06-22 reinforcement] / formal GRANT follow-up maintained)
3. KIS call-side events: identified
   1) EGW00123 token expiry then reissue behavior pass confirmed — token automatic reissue path operates normally in this date's Step 17 flow
   2) EGW00215 rate limit occurrence case identified — new R assignment on hold this date.
       - Consistent with R-AUTO-001 / R-AUTO-015 / OD-SAFE-004.
       - The automatic retry policy is maintained as-is, limited to idempotent steps.
       - Step 17 BALANCE_REFRESH is classified as idempotent.
       - The backoff · retry policy decision on the Step 17 wrapper · Step Functions Step 17 state side is separated as a followups-overview 2026-06-23 §1 follow-up phase responsibility.

### 5. DB Korean storage normal confirmation (limited to connector-side responsibility tables)

1. Check target: complete
   1) `connector.connector_order_request` — Korean columns such as stock name / reason store / query normal
   2) `connector.connector_api_call_log` — KIS API call message Korean column store / query normal
   3) `connector.connector_position_snapshot` — held stock name Korean column store / query normal
   4) `execution.strategy_execution_order` Korean store / query normal — however, this table is 04 spec responsibility / this note is a fact citation only / detailed accumulation is separated as third-work 04 spec operation-notes responsibility
2. encoding check: confirmed
   1) `SHOW server_encoding` = `UTF8`
   2) `SHOW client_encoding` = `UTF8`
   3) Korean normal query of the above 4 tables based on the PGAdmin4 / `portfolio_admin` session (limited to SELECT / 0 DDL · DML)
3. Conclusion: correction
   1) Not "DB Korean corruption" but judged an encoding issue of some PowerShell / SSM / AWS CLI console output display paths
   2) Not newly registered as a DB risk / 0 new R assignments in the R-DATA series
   3) Organizing the console output display path is separated as a followups-overview 2026-06-23 §2 follow-up (unify the wrapper run summary output encoding / Korean label display in the SSM RunCommand response body / Korean column display in AWS CLI `--output text` / combined with the 2026-06-18 second session follow-up memo) responsibility

### 6. Decision / risk change summary

1. New decisions: complete
   1) OD-MS-028 (Step 12 retry-normalizer embedded policy / 🟡 잠정 / affected spec 03 · 04 · 10 / not a separate Step 11.5 split / embedded at the existing Step 12 head)
   2) OD-MS-029 (Daily AWS Paper Step Functions approval workflow false / true path operational procedure / 🟢 확정 / affected spec 04 · 10 / same state machine input parameter branching)
2. Decisions with no body change: first-demonstration memo reinforced
   1) OD-MS-009 — the Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask enters the Step Functions-side operator demonstration stage (EventBridge Scheduler periodic trigger / formal production entry is still a follow-up phase)
   2) OD-MS-027 — the point that the MarketConnector env bootstrap regeneration operational policy must be called identically immediately before Step Functions Step 1 / 12 / 13 / 17 entry is first demonstrated as R-AUTO-021 [2026-06-23 reinforcement]
   3) OD-MS-016 / OD-MS-021 / OD-MS-023 — the Strategy Execution / MarketConnector responsibility separation / 17-step safety baseline / wrapper operational policy body maintained as-is / the Step 12 retry-normalizer is classified only as a MarketConnector-side Step 12 internal safety supplement / maintains the same responsibility boundary even after the Step Functions transition
   4) OD-SAFE-001 / OD-SAFE-004 — the automatic BUY · SELL E2E phased introduction / automatic-retry-prohibited step body maintained as-is / entry into the Step Functions approval true path only at the operator's explicit approval point / the Step 12 retry-normalizer's automatic recovery operates only within the idempotent step's controlled recovery scope (the 6 conditions)
3. New risks: complete
   1) R-AUTO-022 (risk of the automatic re-submission path being broken after post-close REJECTED · `40580000`, Status `Mitigated` — first blocking via the Step 12 retry-normalizer embedding + 6 recovery conditions / this date's dry-run pass / validation of the first round of actual retry candidate occurrence is followups-overview 2026-06-23 §3 follow-up)
4. Risks with no body change: reinforcement memo
   1) R-AUTO-021 — [2026-06-23 reinforcement] the need to maintain the Step 1 / 12 / 13 / 17 wrapper common bootstrap call flow even after the Step Functions transition / until entering formal systemd unit + `EnvironmentFile` registration, the wrapper bootstrap function is the single source of truth / Status `Mitigated` maintained
   2) R-AUTO-018 — KIS `inquire-daily-ccld` summary-only fallback mismapping / this date's Step 13 limited to a single active candidate (`282330` SELL 1) / no multi active state entry / 0 summary fallback guard entries / 0 mitigation-consistency regressions
   3) R-AUTO-019 — `-AllowPaperOrderExecute` unintended-point real order risk / this date's Step Functions approval true path entry is limited to the operator's explicit approval (`allowPaperOrderExecute=true`) point / 0 unintended option uses / post-hoc verification possible with the `PaperOrder: True` label of the Step 12 stdout

### 7. Safety / security check result (2026-06-23)

1. broker / KIS calls = 1 KIS paper SELL (Step 12 Step Functions approval true path real execution) + balance · order check query-only.
   - 0 BUY / cancel / modify / additional `--execute` calls.
   - The SELL position `mark_position_sell_ordered()` call is the responsibility of the MarketConnector executor SELL success point (consistent with OD-MS-016).
   - 0 fill · position sync automatic retries / 0 aws-live work.
2. 0 RDS DDL. DML is limited to the Step Functions approval true path normal flow.
   - `connector.connector_order_request` insert (`id 48` ACCEPTED → FILLED)
   - `connector.connector_order_event` / `connector.connector_fill` insert (detailed row id accumulation is 04 spec responsibility)
   - `connector.connector_balance_snapshot` insert
   - `connector.connector_position_snapshot` insert (4 held stocks)

   0 `execution.strategy_execution_order` UPDATE applications by this date's retry-normalizer (currently 0 retry candidates).
3. The sensitive information below has 0 plaintext records in this note — all use `[REDACTED]` or a placeholder.
   - actual secret value / KIS app key / KIS app secret / token / KIS paper login credential
   - account number / account password
   - RDS password / RDS endpoint hostname
   - account-id / actual IAM Role ARN / actual secret ARN / IAM access key id
   - instance-id plaintext outside the body / EIP / image digest full sha256 / task ARN / job ARN
   - the full broker response other than broker_order_no
   - Administrator password / Step Functions execution ARN
4. AWS / SSM / EC2 / RDS / Secrets Manager / SSM Parameter Store / Step Functions / KIS API calls are all performed directly by the operator. Kiro only performed documentation / procedure organization / validation item organization.
   - 0 AWS CLI / boto3 executions
   - 0 AWS resource creation / modification / deletion
   - 0 plaintext records of `secretsmanager:GetSecretValue` result values
   - 0 plaintext quotations of CloudWatch Logs body / SSM response body / KIS API response body / Step Functions execution history body / full operator PowerShell stdout
   - PGAdmin4 check limited to SELECT / 0 INSERT · UPDATE · DELETE · DDL
5. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to this date's work (spec area).
   - The diff of `port-marketconnector/connector_strategy_order_execute.py` full replacement + Step 12 head retry-normalizer embedding that the operator patched directly is recorded in this note §1 only as a fact.
   - 0 full-body quotations / 0 quotations of function signatures / SQL body / patch diff / consistent with R-DOCS-001 / port-marketconnector area.
6. The operational identifiers below are recorded as facts consistent with the user-specified policy — not secrets.
   - Step Functions state machine `portfolio-paper-daily-step1-17-approval`
   - `execution.strategy_execution_order id 40` / `connector.connector_order_request id 48`
   - `broker_order_no 0000006143`
   - stock code `282330` / stock name BGF리테일 / quantity 17 / sell method MARKET
   - Step Functions approval gate labels `allowPaperOrderExecute=false` · `allowPaperOrderExecute=true`
   - wrapper stdout label `PaperOrder: True`
   - EC2 instance id `i-0fce77927b7397b88` (consistent with OD-NET-010 / already recorded as a fact in spec deliverables prior to this date)
   - signal_date · run_date `2026-06-23`
   - `server_encoding` · `client_encoding` value `UTF8`
   - KIS error code `EGW00123` · `EGW00215`



## 2026-06-30 (afternoon) — Intraday stop-loss Slack live integration (MarketConnector evaluate replacement deployment + intraday runner `--create-order --notify-slack` connection + EC2 IAM `lambda:InvokeFunction` grant + 1 safety validation pass)

Accumulates, from the MarketConnector EC2 operational perspective, the result of the intraday stop-loss Slack live integration that the operator performed directly as this date's afternoon additional work. This note has 0 plaintext quotations of the `connector_intraday_position_evaluate.py` body / runner ps1 body / Lambda code body / IAM Policy full body / SSM response body / Slack message body / full CloudWatch Logs (consistent with R-DOCS-001).

1. MarketConnector evaluate replacement deployment: complete
 1) Target file / deployment identifiers
   (1) File path
       - `/home/ec2-user/apps/port-marketconnector/src/connector_intraday_position_evaluate.py`
       - operator direct replacement deployment
   (2) Deployment version / SHA256
       - deployment version `connector-intraday-position-evaluate-1.1.1-slack-notify`
       - deployment SHA256 `5ec6914853ab34e200682f256de53693f972b3e5d337a5bd8ab8ebf5296230ed`
   (3) 3 new CLI options added
       - `--notify-slack`
       - `--slack-function-name`
       - `--slack-region`
   (4) Static validation
       - `py_compile` passed based on `.venv/bin/python`
       - `--help` output validation passed

2. Intraday runner update: complete
 1) Target file / identifiers
   (1) runner path
       - `/home/ec2-user/apps/port-marketconnector/scripts/run_intraday_snapshot_and_evaluate.sh`
       - operator direct update
   (2) backup / SHA256
       - backup name `run_intraday_snapshot_and_evaluate.sh.bak.20260630T112255Z.create-order-notify-slack`
       - runner SHA256 `8fe7657a75b5d7637ec645b6d8a55bf75c993d46c1c71e8a5e88bded29baa8a0`
   (3) Changes
       - added `--create-order` to the existing evaluate call
       - added `--notify-slack` to the existing evaluate call
       - runner syntax validation passed

3. MarketConnector EC2 IAM permission grant: complete
 1) Instance Role / inline policy
   (1) IAM Role name `portfolio-paper-marketconnector-ec2-role`
   (2) inline policy name `portfolio-paper-marketconnector-event-notifier-invoke`
   (3) action `lambda:InvokeFunction`
   (4) Resource = limited to `portfolio-event-notifier` Lambda / 0 Resource · Action wildcards
   (5) consistent with OD-SEC-005 / OD-SEC-006
 2) EC2 invoke smoke
   (1) Notifier Lambda invoke smoke succeeded from the EC2 side
   (2) 0 plaintext quotations of the Lambda response body

4. Actual runner 1 safety validation: complete
 1) SSM commandId / result
   (1) commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131`
   (2) snapshot refresh succeeded
   (3) evaluate execution succeeded
   (4) source_version `connector-intraday-position-evaluate-1.1.1-slack-notify` confirmed
   (5) `create_order=True` confirmed
   (6) `notify_slack=True` confirmed
   (7) `open_position_count=0` confirmed
   (8) `EMPTY_NORMAL` confirmed
   (9) `INTRADAY_SNAPSHOT_AND_EVALUATE=SUCCESS` confirmed
   (10) runner exit code 0 confirmed
   (11) Because it was a 0 OPEN position state, it terminated normally without check / order / slack

5. DB after-check: complete
 1) marker / count
   (1) marker `STEP19C_INTRADAY_STOP_FINAL_DB_AFTER_CHECK=SUCCESS`
   (2) `TODAY_INTRADAY_CHECKS` count 0
   (3) `TODAY_INTRADAY_STOP_EXECUTION_ORDERS` count 0
   (4) `ACTIVE_INTRADAY_STOP_EXECUTION_ORDERS` count 0
   (5) `TODAY_INTRADAY_STOP_CONNECTOR_ORDERS` count 0
   (6) `TODAY_INTRADAY_STOP_CONNECTOR_ORDER_ROWS` 0 rows
 2) balance snapshot
   (1) latest balance snapshot id `281`
   (2) `as_of_date=2026-06-30`
   (3) `as_of_ts=2026-06-30 11:23:34.973842+00`
   (4) `total_eval_amount=8,706,505` won
   (5) `cash_balance=8,706,505` won
   (6) `source_version=connector-intraday-snapshot-refresh-1.0.0`
   (7) PSQL exit code 0
 3) DB validation query authoring principle
   (1) Use only columns authored after pre-confirming `information_schema.columns`
   (2) 0 uses of assumed column names
   (3) consistent with `.kiro/AGENTS.md` "operational command authoring rules (additional)" rules 2 · 4

6. Decision / risk mapping (2026-06-30 afternoon intraday stop-loss Slack)
 1) No decision body change
   (1) OD-MS-001 (port-marketconnector compute = EC2+EIP) / OD-MS-016 (Strategy Execution / MarketConnector responsibility separation) / OD-MS-035 (intraday position check 3-stage structure) / OD-MS-036 (Intraday Stop Sell Submit Workflow) / OD-MS-030 (AWS common Slack notifier Lambda) / OD-MS-038 (Daily Brief Slack mini workflow) evidence reinforced with no body change
   (2) For detailed decision changes, refer to the `../_common/operator-decisions.md` Change Log `2026-06-30 (오후) 장중 손절 Slack` item
   (3) 0 new decisions / no Decision Summary count change (total 97 / confirmed 52 / provisional 42 maintained)
 2) Risk mapping
   (1) R-AUTO-036 new — risk of the side effect of a no-rollback policy on Slack send failure after intraday stop-loss READY creation / Status `Mitigated`
   (2) R-AUTO-035 [2026-06-30 afternoon additional reinforcement] — the Notifier Lambda call entry point is expanded to the MarketConnector EC2 runner / Status `Mitigated` maintained as-is
   (3) R-AUTO-024 (Slack webhook URL plaintext exposure risk / `Accepted`) mitigation maintained as-is

7. Follow-up (03 spec follow-up phase responsibility or 04 / 06 spec follow-up phase responsibility)
 1) Confirm `INTRADAY_STOP_LOSS` Slack real-event reception when the hard stop condition is satisfied after actual held stocks occur
 2) Re-confirm the `INTRADAY_STOP_SELL` READY creation + approval gate blocked state after actual held stocks occur
 3) Review whether to add account / current price / entry price / expected pnl amount to the Slack message (03 spec follow-up phase responsibility)
 4) Review whether to add a separate approval summary Slack after intraday stop-loss READY creation (04 spec follow-up phase responsibility)
 5) `portfolio-paper-intraday-stop-sell-approval` state machine automatic ENABLE entry follow-up after separate operator approval (04 spec follow-up phase responsibility / consistent with OD-MS-035 / OD-MS-036 / R-AUTO-030 ~ R-AUTO-032 / R-BROKER-005)
 6) Move the Slack webhook URL to Secrets Manager or SSM SecureString (R-AUTO-024 / 06 spec follow-up phase responsibility)
 7) R-AUTO-036 operational detection automation (05 · 06 spec follow-up phase responsibility)

8. This date's fact record scope (2026-06-30 afternoon)
 1) Kiro work = performed only this section accumulation in the 03 spec `operation-notes.md`
 2) Operator direct work area = `connector_intraday_position_evaluate.py` replacement deployment + intraday runner update + EC2 IAM inline policy grant + EC2 invoke smoke + 1 actual runner safety validation
 3) 0 Kiro-side changes to AWS CLI / boto3 / psql / Lambda execution / Step Functions execution / SSM RunCommand / external API calls on this date / 0 Kiro-side AWS resource new creation · modification · deletion on this date
 4) 0 plaintext quotations of the `connector_intraday_position_evaluate.py` body / runner ps1 body / IAM Policy full body / Lambda code body / Step Functions ASL body / SSM response body / Slack message body / full CloudWatch Logs (consistent with R-DOCS-001)
 5) The sensitive information below has 0 plaintext records in this note — all use `[REDACTED]` or a placeholder
       - secret value / KIS app key / KIS app secret / token
       - 12-digit account number raw / DB password / Administrator password
       - RDS password / RDS endpoint hostname
       - 12-digit account-id raw / actual IAM Role ARN / actual secret ARN / IAM access key id / actual state machine ARN
       - Slack webhook URL / EIP / public IP / broker_order_no raw
 6) Only the operational identifiers below are recorded as facts consistent with the user-specified policy — not secrets
       - file path / deployment SHA256 / deployment version / CLI option labels
       - runner SHA256 / runner backup name / SSM commandId
       - IAM Role name / inline policy name / Lambda name / state machine name
       - Slack event labels / phrasing labels
       - 2 source_version / 3 marker names / 5 table count labels
       - balance snapshot id · as_of_date · as_of_ts · amount summary
