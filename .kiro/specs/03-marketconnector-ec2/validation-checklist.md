# Validation Checklist — 03-marketconnector-ec2

A minimal checklist that records the [확인] results of [`./runbook.md`](./runbook.md) with the 4 labels.

Labels

- `[O]` pass
- `[X]` not met / failure
- `[Kiro 후속 작업 필요]` deliverable that Kiro must additionally create or fill, on hold
- `[운영자 확인 필요]` can be confirmed directly only by the operator

Principles

- Do not record actual secret values, KIS app key / app secret, account numbers, the RDS endpoint hostname, the RDS password, account-id, actual secret ARNs, IAM access key ids, instance-ids, or EIPs in plaintext. Use `[REDACTED]` or a placeholder for all of them.
- Only the operator issues an actual `GetSecretValue` call. Kiro automatic validation is limited to `DescribeSecret` / `GetParameter` metadata.

## 1. Python / venv / dependency libraries

| Item | Result | Basis | Note |
|------|------|------|------|
| Python `3.9.25` installed | [O] | 2026-06-10 operator direct confirmation | (§4.1) |
| 1 venv configured | [O] | 2026-06-10 operator direct confirmation | path / name placeholder |
| `requests` / `flask` / `psycopg2-binary` / `psycopg` / `pandas` (5 items) installed | [O] | 2026-06-10 operator direct confirmation | (§4.2) |
| The 8 MS packaging files (`requirements.txt` etc.) unmodified | [O] | this spec's safety constraint | (R14.1) |

## 2. PostgreSQL client / pg_restore / psql 18.4

| Item | Result | Basis | Note |
|------|------|------|------|
| `psql --version` output major 18 / full 18.4 or higher | [운영자 확인 필요] | operator EC2 shell | (§5) |
| `pg_dump --version` output major 18 / full 18.4 or higher | [운영자 확인 필요] | same as above | |
| `pg_restore --version` output major 18 / full 18.4 or higher | [운영자 확인 필요] | same as above | the client used for the 6/9 RDS restore, unchanged |
| 0 downgrades below client 18.4 | [O] | this spec's no-downgrade policy | (§5.2) |

## 3. `marketconnector_app` RDS connection

| Item | Result | Basis | Note |
|------|------|------|------|
| `marketconnector_app` user SELECT 1 pass | [O] | 2026-06-10 operator direct confirmation | endpoint / password not recorded |
| RDS Public access = `No` | [운영자 확인 필요] | RDS Console | 02 spec alignment |
| SG inbound: allow only `sg-marketconnector-ec2` → `sg-rds-postgres` 5432 | [운영자 확인 필요] | EC2 Console | (§6.1) |
| 0 RDS DDL/DML calls at this spec's point in time | [O] | this spec's safety constraint | (R14, §6.5) |

## 4. Connector / Flask read-only smoke test

| Item | Result | Basis | Note |
|------|------|------|------|
| `connector_balance.py` balance query pass | [O] | 2026-06-10 operator direct confirmation / [2026-06-17 re-validation] pass | (§10 (d)) |
| `connector_order_check.py` order / fill query pass | [O] | 2026-06-10 operator direct confirmation / [2026-06-17 re-validation] pass | (§10 (e)) |
| Flask internal smoke test (`/api/v1/view/...` read-only endpoint) pass | [O] | 2026-06-10 operator direct confirmation | (§10 (f)) |
| Validation of operation-ready candidates (`connector_quote_realtime.py` / `connector_quote_closed.py` / `connector_view_service.py`) | [Kiro 후속 작업 필요] | follow-up phase responsibility | (§7.3, §7.4) |
| 0 `CONNECTOR_DEBUG=true` operational exposure | [운영자 확인 필요] | Flask startup log | normal operation mode forces `false` (§8.2) |
| [2026-06-17] `CONNECTOR_BALANCE` SSM RunCommand re-validation | [O] | 2026-06-17 operator direct confirmation (SSM RunCommand) | `connector_balance_snapshot` latest `as_of_date 2026-06-17` / `source_api inquire-balance` / `source_version connector-balance-1.0.0` / 0 holdings normal (runbook §4.1) |
| [2026-06-17] `CONNECTOR_ORDER_CHECK` SSM RunCommand re-validation (MarketConnector read-only-series precedent validation) | [O] | 2026-06-17 operator direct confirmation (SSM RunCommand) | KIS `inquire-daily-ccld` normal response. See details §7E |

## 5. Secrets Manager / SSM env injection

| Item | Result | Basis | Note |
|------|------|------|------|
| Secrets Manager 4 entries metadata normal (`describe-secret`) | [운영자 확인 필요] | operator EC2 shell | KIS app key / secret / paper-account / rds/marketconnector-app |
| Existing `/portfolio/paper/rds/master` retained | [운영자 확인 필요] | 02 spec alignment | 0 renames / deletions |
| SSM Parameter 6 entries normal (`get-parameters-by-path /portfolio/paper/marketconnector`) | [운영자 확인 필요] | operator EC2 shell | base-url / connector-host·port·debug / environment / broker-name |
| Environment-variable mapping (`APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD` / `INTEREST_DB_*` / `BASE_URL` / `PORT_*` / `CONNECTOR_*`) injection pass | [O] | 2026-06-10 operator direct confirmation / [2026-06-17 re-validation] pass (v5 pattern) | (§10 (g), §8.2) |
| 0 plaintext secret storage in the temporary export script | [운영자 확인 필요] | EC2 shell file inspection | permission 700 recommended |
| 0 automatic `GetSecretValue` calls (Kiro side) | [O] | this spec's safety constraint | operator only |
| [2026-06-17] JSON SecretString inner-key extraction + `KIS_*` alias simultaneous export validation (v5 pattern) | [O] | 2026-06-17 operator direct confirmation | passed after v5 pattern correction. See details §7F |
| [2026-06-17] 0 plaintext records of secret value / account number / token | [O] | 2026-06-17 operator direct confirmation | record only secret name path / JSON shape / value length / key presence (R-DOCS-001 alignment / security policy) |

## 6. Instance Role / Access Key non-use

| Item | Result | Basis | Note |
|------|------|------|------|
| `aws sts get-caller-identity` result `assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>` | [O] | 2026-06-10 operator direct confirmation | (§9.4) |
| `aws configure list` access_key Source = `iam-role` or `Ec2InstanceMetadata` | [O] | 2026-06-10 operator direct confirmation | |
| `~/.aws/credentials` does not exist | [O] | 2026-06-10 operator direct confirmation | |
| 0 access-key patterns inside `~/.aws/config`, dotfiles, systemd `EnvironmentFile=` | [운영자 확인 필요] | EC2 shell grep | (§9.5) |
| `AmazonSSMManagedInstanceCore` attach status | [운영자 확인 필요] | IAM Console | (§9.2) |
| `CloudWatchLogsWrite` recommended option (pre-create log group, excluding `CreateLogGroup`) applied | [Kiro 후속 작업 필요] | update after operator direct work | (§9.2) |

## 7. 0 new-order / buy / sell / cancel / modify calls

| Item | Result | Basis | Note |
|------|------|------|------|
| `connector_buy.py` not executed | [O] | this spec's safety constraint | (R14, §7.3) |
| `connector_sell.py` not executed | [O] | same as above | |
| `connector_cancel.py` not executed | [O] | same as above | |
| `connector_modify.py` not executed | [O] | same as above | |
| 0 Flask new-order endpoint (`/api/v1/buy|sell|cancel|modify/...`) calls | [O] | this spec's safety constraint | runbook §5 alignment |
| 0 RDS DDL/DML | [O] | this spec's safety constraint | (§6.5) |
| [2026-06-17] Re-validation of 0 new-order / `--execute` / aws-live actions | [O] | 2026-06-17 operator direct confirmation | only single read-only runs executed. See details §7A |
| [2026-06-17] (Daily AWS 17-step E2E) Step 12 KIS paper BUY 4 submissions success | [O] | 2026-06-17 operator direct confirmation / operation-notes 2026-06-17 (Daily AWS 17-step E2E complete) §2 alignment | See details §7B |
| [2026-06-17] (Daily AWS 17-step E2E) Step 13 single-item query pass after summary fallback guard patch | [O] | 2026-06-17 operator direct confirmation / operation-notes 2026-06-17 (Daily AWS 17-step E2E complete) §3 alignment | See details §7C |
| [2026-06-17] (Daily AWS 17-step E2E) Step 17 re-run pass after legacy privilege / search_path correction | [O] | 2026-06-17 operator direct confirmation / operation-notes 2026-06-17 (Daily AWS 17-step E2E complete) §4 alignment | See details §7D |

### §7G — Step 12 Paper SELL submission details

- Explicit `-AllowPaperOrderExecute` execution.
- `088350` 한화생명 244 shares MARKET.
- `execution_order id 37` SUBMITTED / `connector_order_request id 46` ACCEPTED.
- broker_order_no · broker_branch_code generated (0 plaintext records in this document / R-DOCS-001 alignment).
- No rejection.

### §7H — Step 13 fill-query details

- `connector_order_request id 46` FILLED / `connector_fill id 34` generated.
- fill_qty 244 / fill_price `5,075.8607` / fill_amount `1,238,510.01` / fill_ts `2026-06-22 00:46:58 UTC`.
- OD-MS-025 alignment (single-item direct-only query).

### §7F — JSON SecretString v5 pattern details

- After extracting the JSON SecretString inner keys (`APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD`) of `kis-app-key` / `kis-app-secret` / `paper-account`, export the `APP_*` compatibility keys + `KIS_*` aliases simultaneously.
- The first failure of exporting the entire JSON dict as the environment-variable value was resolved by the v5 pattern correction.
- Basis: design.md §8.2.1 / §8.2.2 / runbook.md §2 alignment.

### §7E — CONNECTOR_ORDER_CHECK SSM RunCommand re-validation details

- KIS `inquire-daily-ccld` response: `response_status=200` / `response_code=0` / `is_success=true`.
- `called_at 2026-06-17 00:51:03 UTC`.
- Row count: `connector_order_request 33` / `connector_order_event 18` / `connector_fill 13`.
- 0 new is judged normal as no new order / fill occurred on this date.
- Basis: runbook §4.2.

### §7A — 0 new-order re-validation details

- This date's execution is only single read-only runs of `connector_balance.py` / `connector_order_check.py`.
- `connector_buy.py` / `connector_sell.py` / `connector_cancel.py` / `connector_modify.py` not executed.
- 0 `--execute` / 0 aws-live actions.
- 0 new rows in `connector_order_event` / `connector_fill` = normal.
- Basis: runbook §4.2 / OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004 alignment.

### §7B — Step 12 KIS paper BUY 4 submissions details

- `execution_order` id `26 ~ 29` SUBMITTED / `connector_order_request` id `34 ~ 37` generated.
- `broker_order_no`: `0000035906` / `0000035912` / `0000035918` / `0000035932`.
- 0 SELL / cancel / modify calls / 0 aws-live actions.
- Correction: passed after formal deployment of `connector_strategy_order_execute.py` to the MarketConnector EC2 + using venv python + correcting `execution` table UPDATE privileges + the `source_run_id` fallback patch.
- Basis: R-AUTO-009 / R-AUTO-010 [2026-06-17 reinforcement] alignment.

### §7C — Step 13 summary fallback guard details

- Immediately identified a case where the first response, being `output1 empty` + `output2 summary`, was incorrectly mapped to the last order row.
- Deleted the incorrectly created `connector_order_event` / `connector_fill` + restored the `connector_order_request` status.
- Applied the `connector_order_check.py` summary fallback guard patch:
  - allow fallback only when there is exactly 1 active candidate
  - if multiple, prohibit event · fill · status changes
- All 4 synchronized normally via per-`broker_order_no` single-item query (`connector_order_request 34 ~ 37` FILLED / `connector_fill 26 ~ 29` generated).
- R-AUTO-018 new mitigation first validation.

### §7D — Step 17 legacy privilege / search_path correction details

- First failure: bare `holdings` `relation does not exist` (missing legacy schema USAGE / `legacy.holdings` DML / sequence / database search_path).
- Correction: the operator corrected search_path to `connector, execution, legacy, reference, public` + USAGE / DML / sequence GRANT + default privileges correction.
- SSM re-run result: `Status Success` / `ResponseCode 0`.
- `connector_position_snapshot` latest created for 4 tickers:
  - `position_snapshot_id 120 ~ 123`
  - quantity `52 / 65 / 244 / 17`
  - avg_buy_price `28980.77 / 27043.08 / 5744.41 / 120182.35`
- R-DATA-011 new mitigation first validation.

## 8. Follow-up handover

- [Kiro 후속 작업 필요] Accumulate this checklist's results (date / success·failure) in [`./operation-notes.md`](./operation-notes.md).
- [Kiro 후속 작업 필요] Reflect the OD-SEC-005 / OD-SEC-006 provisional→confirmed candidates + a new candidate for the `AmazonSSMManagedInstanceCore` usage policy in [`../_common/operator-decisions.md`](../_common/operator-decisions.md) (upon operator approval).
- [Kiro 후속 작업 필요] Register R-DATA / R-CAP / R-BROKER / R-SEC / R-AUTO (5 items) under the next available IDs in [`../_common/risk-register.md`](../_common/risk-register.md) (upon operator approval).
- [Kiro 후속 작업 필요] Reflect the 03 first application environment / first scope / handover to 04·05·08·09·10 in [`../_common/followups-overview.md`](../_common/followups-overview.md).
- [운영자 확인 필요] When entering the 04 / 05 / 08 / 09 specs, take this spec's §13 EC2 → ECS mapping as input.


## 2026-06-22 Daily AWS Paper 1~17 second full-run validation results

This section cumulatively records the validation results of the 03 spec (MarketConnector EC2) responsibility items during the second live operation run of Daily AWS Paper Wrapper 1 ~ 17 on 2026-06-22.

- Detailed results: [`./operation-notes.md`](./operation-notes.md) 2026-06-22 §1 ~ §6.
- All items are based on operator direct confirmation.
- 0 plaintext records of the actual sensitive values below in this document.
  - broker account number / raw broker_order_no / raw broker_branch_code
  - secret value / RDS password
  - instance-id / EIP / actual ARN

| Validation item | Result | Confirmation time / method | Note |
|-----------|------|------------------|------|
| MarketConnector env bootstrap regeneration validation (`daily-aws-paper.functions.ps1` common function call) | [O] | 2026-06-22 operator direct confirmation | OD-MS-027 new / R-AUTO-021 new mitigation first validation / runbook §2.1 alignment |
| Step 1 `CONNECTOR_BALANCE` re-run success (initial failure → passed after bootstrap patch) | [O] | 2026-06-22 wrapper run | `/tmp/inject-env.sh not found` → after adding the bootstrap function, SSM Success / ResponseCode 0 / `connector_balance_snapshot` stored |
| Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` Paper SELL order submission success | [O] | 2026-06-22 wrapper run | `088350` 244 shares MARKET / SUBMITTED · ACCEPTED. See details §7G |
| Step 13 `CONNECTOR_ORDER_CHECK` fill-query success | [O] | 2026-06-22 wrapper run | FILLED / OD-MS-025 alignment (single-item direct-only). See details §7H |
| Step 17 `BALANCE_REFRESH` SSM Success | [O] | 2026-06-22 wrapper run | SSM Status `Success` / ResponseCode 0 / `connector_position_snapshot` latest `created_at 2026-06-22 00:50:50 UTC` / 5 holdings / confirmed `088350` removed from the balance snapshot / 0 R-DATA-011 regressions |
| secret value not exposed in logs | [O] | 2026-06-22 operator direct confirmation | 0 plaintext quotations of wrapper SSM stdout / stderr / SSM response body / KIS API response body / 0 plaintext records of the `secretsmanager:GetSecretValue` result value / only length / key presence output inside the bootstrap function / R-DOCS-001 alignment |
| Step 12 `-AllowPaperOrderExecute` safety gate compliance | [O] | 2026-06-22 wrapper summary | wrapper central PAPER_ORDER_GATE + Step 12 internal double gate / executes only when `-AllowPaperOrderExecute` is explicit / `PaperOrder: True` label output (R-AUTO-019 mitigation alignment) / 0 aws-live actions |
