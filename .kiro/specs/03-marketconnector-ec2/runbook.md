# Runbook — 03-marketconnector-ec2

A procedure for the operator to follow directly on the EC2 shell / AWS Console. First application environment: `aws-paper`, region `ap-northeast-2`. See [`./design.md`](./design.md) for detailed decision rationale.

Labels

- `[실행]` perform an actual command / action
- `[확인]` inspect state / result only
- `[준비]` agree / record only before follow-up work
- `[복구]` roll back on failure / incident

Principles

- Do not record actual secret values, KIS app key / app secret, account numbers, the RDS endpoint hostname, the RDS password, account-id, actual secret ARNs, IAM access key ids, instance-ids, or EIPs in plaintext. Use `[REDACTED]` or a placeholder for all of them.
- Only the operator issues an actual `secretsmanager:GetSecretValue` call. Kiro automatic validation is limited to `DescribeSecret` / `GetParameter` metadata.
- Zero calls to new-order / buy / sell / cancel / modify entrypoints (`connector_buy.py` / `connector_sell.py` / `connector_cancel.py` / `connector_modify.py` must never be executed).
- Actual creation / change / deletion of AWS resources is performed by the operator directly.

## 1. Pre-check [확인]

1. Confirm the EC2 region is `ap-northeast-2`. In EC2 Instances, confirm the MarketConnector EC2 is in the `running` state.
2. On the EC2 → Modify IAM role screen, confirm the Instance Profile is `portfolio-paper-marketconnector-ec2-profile`.
3. Confirm the Security Group matrix: allow only `sg-marketconnector-ec2` → `sg-rds-postgres` 5432 inbound. RDS Public access = `No`.
4. Confirm metadata only that the 4 Secrets Manager entries / 6 SSM Parameters are registered exactly per the 06 spec naming.

## 2. env injection [실행] [확인]

> **2026-06-22 reinforcement (OD-MS-027 new alignment / R-AUTO-021 new mitigation first validation)**
>
> In Daily AWS Paper Wrapper operation (`.kiro/scripts/run-daily-aws-paper.ps1`), Step 1 / Step 12 / Step 13 / Step 17 (the 4 MarketConnector EC2 SSM steps) call the common **MarketConnector env bootstrap function** in `daily-aws-paper.functions.ps1` right before entry, which **regenerates** `/tmp/inject-env.sh` **at step execution time**.
>
> That is, it does not assume the prior existence of `/tmp/inject-env.sh` on the EC2. After an EC2 stop / start, the `/tmp` directory may be lost as ephemeral (R-AUTO-021 alignment).
>
> The manual export script authoring in this procedure is used only as a manual fallback when the operator performs a one-off verification directly while `/tmp/inject-env.sh` is absent. Formal systemd unit + `EnvironmentFile` registration remains separated as the responsibility of this spec's follow-up tasks (7 / 26 / 27).

1. Connect to the EC2 via SSH or SSM Session Manager.
2. Author the temporary export script — the `/tmp/inject-env.sh` pattern (permission 700, no plaintext secret storage, in-memory export only). See [`../06-secrets-and-iam/runbook.md`](../06-secrets-and-iam/runbook.md) §6-1 for the detailed pattern.
3. The environment-variable mapping is exactly as in the [`./design.md`](./design.md) §8.2 / §8.2.1 / §8.2.2 tables:
   - Secrets Manager: `APP_KEY`, `APP_SECRET`, `PAPER_ACNT`, `ACNT_PRDT_CD`, `INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD`
   - SSM Parameter: `BASE_URL`, `PORT_ENVIRONMENT`, `PORT_BROKER_NAME`, `CONNECTOR_HOST`, `CONNECTOR_PORT`, `CONNECTOR_DEBUG`
   - **KIS_* alias (actual code execution alignment)**: `KIS_APP_KEY`, `KIS_APP_SECRET`, `KIS_PAPER_ACNT`, `KIS_ACNT_PRDT_CD`, `KIS_BASE_URL` — export the same value simultaneously with the compatibility key (`./design.md` §8.2.1 alignment / 2026-06-17 reinforcement)
4. **JSON SecretString handling** — the secrets below are JSON SecretStrings, not plain strings.
   - `/portfolio/paper/marketconnector/kis-app-key` — inner key `APP_KEY`
   - `/portfolio/paper/marketconnector/kis-app-secret` — inner key `APP_SECRET`
   - `/portfolio/paper/marketconnector/paper-account` — inner keys `PAPER_ACNT` / `ACNT_PRDT_CD`

   Handle them with the following procedure (`./design.md` §8.2.2 alignment).
   - Extract only the inner key value from the result of `aws secretsmanager get-secret-value --secret-id <name> --query SecretString --output text` using `python -c 'import json,sys; d=json.loads(sys.stdin.read()); print(d["APP_KEY"])'` or `jq -r .APP_KEY`.
   - Do not export the entire JSON dict as the environment-variable value (the cause of the 2026-06-17 first failure).
   - Export the extracted value into both the `APP_KEY` and `KIS_APP_KEY` environment variables simultaneously. Do the same for `APP_SECRET` / `KIS_APP_SECRET`, `PAPER_ACNT` / `KIS_PAPER_ACNT`, `ACNT_PRDT_CD` / `KIS_ACNT_PRDT_CD`, `BASE_URL` / `KIS_BASE_URL`.
   - Do not print the secret values themselves to stdout / logs / console capture / operator notes. Confirm only value length or key presence.
5. After `source /tmp/inject-env.sh`, confirm only the environment-variable keys (do not leave the values in output / logs).

   ```bash
   env | grep -E '^(APP_KEY|APP_SECRET|PAPER_ACNT|ACNT_PRDT_CD|KIS_APP_KEY|KIS_APP_SECRET|KIS_PAPER_ACNT|KIS_ACNT_PRDT_CD|KIS_BASE_URL|INTEREST_DB_|BASE_URL|PORT_|CONNECTOR_)' | cut -d= -f1
   ```
6. Transition to the normal operation mode (systemd unit / startup script) is the responsibility of this spec's follow-up task or a separate phase. Out of scope for this runbook. [준비]

### 2.1 Recovery procedure for Daily wrapper bootstrap failure (2026-06-22 reinforcement)

This section is the recovery procedure for when a `/tmp/inject-env.sh not found` or equivalent env-missing error occurs at Step 1 / Step 12 / Step 13 / Step 17 during Daily AWS Paper Wrapper operation (OD-MS-027 / R-AUTO-021 alignment).

1. Confirm whether the wrapper bootstrap function is applied [확인]
   1) Confirm the existence of the MarketConnector env bootstrap function inside `daily-aws-paper.functions.ps1`.
   2) Confirm that the bootstrap function is called right before step entry in the Step 1 / Step 12 / Step 13 / Step 17 step files (`.kiro/scripts/steps/`).
   3) Confirm whether PowerShell parser validation passes (0 parser errors across all `.ps1` files).
2. If the wrapper-side patch is aligned → re-run the Step standalone [실행]
   1) `.\run-daily-aws-paper.ps1 -Environment aws-paper -RunDate <RunDate> -StartStep 1 -EndStep 1` (Step 1 standalone) or re-run only the affected step standalone.
   2) Use only the `-StartStep` / `-EndStep` parameters (`-FromStep` / `-ToStep` are undefined — a mistake risks entering the default 1 / 17 range).
   3) Confirm the step result in the wrapper run summary is SUCCESS / SSM ResponseCode 0 / step failure exit status 0.
3. If the wrapper-side patch is not applied / a bypass is needed right before operation → operator manual fallback [실행] [확인]
   1) The operator connects to the MarketConnector EC2 via SSM Session Manager and manually regenerates `/tmp/inject-env.sh` exactly per this §2 procedure (permission 700 / in-memory export only).
   2) Maintain the JSON SecretString inner-key extraction flow.
       - Parse the result of `aws secretsmanager get-secret-value --secret-id <name> --query SecretString --output text` as JSON.
       - Extract only the inner keys (`APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD` / `BASE_URL`).
       - Export the `APP_*` compatibility keys + `KIS_*` aliases simultaneously.
   3) No plaintext secret value output — zero plaintext records in stdout / stderr / console capture / operator notes / wrapper logs (R-DOCS-001 alignment). Output only length / key presence.
   4) After the manual fallback, re-run the wrapper Step standalone. The permanent resolution transitions to formal application of the wrapper-side bootstrap function / subsequent PowerShell parser validation pass.
4. Re-emphasis of the Step 12 actual-order safety gate [확인]
   1) Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` can submit an actual KIS paper order only when the operator explicitly enters the `-AllowPaperOrderExecute` option (R-AUTO-019 mitigation alignment).
   2) Even during bootstrap failure recovery, avoid an unintended `-AllowPaperOrderExecute` input when re-running Step 12 standalone — verify afterward that the `PaperOrder: True` label is printed in the wrapper summary.
   3) No automatic activation of BUY / SELL in the live environment (OD-SAFE-002 / OD-SAFE-003 / R-AUTO-002 alignment) — the wrapper environment input allows only `aws-paper` / no aws-live branch exists at the code level.

## 3. RDS connection confirmation [확인]

1. Confirm `psql` is major version 18: `psql --version`. Same for `pg_dump --version`, `pg_restore --version`.
2. Confirm only the RDS connection as the `marketconnector_app` user:
   ```text
   psql "host=$INTEREST_DB_HOST port=$INTEREST_DB_PORT dbname=$INTEREST_DB_NAME user=$INTEREST_DB_USER" -c "SELECT 1;"
   ```
3. Pass if the result = `1`. Do not run DDL/DML. Zero calls other than SELECT.
4. On failure, go to §8 [복구].

## 4. connector read-only execution [실행] [확인]

1. Activate the venv: `source <venv-path>/bin/activate`.
2. Run `connector_balance.py` — confirm the balance-query result is returned. exit code 0.
3. Run `connector_order_check.py` — confirm the order / fill query result is returned. exit code 0.
4. Re-confirm that neither script triggers a KIS-side new-order / buy / sell / cancel / modify call.

### 4.1. `connector_balance.py` validation SQL candidates [확인]

1. Confirm the latest row of the BALANCE category in `connector.connector_api_call_log`.
   - `category = 'BALANCE'` / `api_name = 'inquire-balance'` / latest `called_at`
   - `response_status = 200` / `response_code = 0` / `is_success = true`
   - Alignment with the no-plaintext-recording policy for secret value / account number / token / response body (R-DOCS-001).
2. Confirm the latest row of `connector.connector_balance_snapshot`.
   - latest `as_of_date` / `as_of_ts` / `created_at`
   - factual record of `source_api = 'inquire-balance'` / `source_version`
   - 0 holdings is confirmed as 0 rows in `connector.connector_position_snapshot` for the same `as_of_date`, or as a normal 0-count row.
3. SELECT only. 0 DDL/DML. Accumulate results as facts only in the dated section of [`./operation-notes.md`](./operation-notes.md).

### 4.2. `connector_order_check.py` validation SQL candidates [확인]

1. Confirm the latest row of the ORDER category in `connector.connector_api_call_log`.
   - `category = 'ORDER'` / `api_name = 'inquire-daily-ccld'` / latest `called_at`
   - `response_status = 200` / `response_code = 0` / `is_success = true`
   - No plaintext body recording.
2. Confirm the row count inventory.
   - Compare the cumulative row count of `connector.connector_order_request` / `connector.connector_order_event` / `connector.connector_fill` against the number of new rows at this execution time.
   - If there are 0 new orders / fills for the day, 0 new rows in `connector_order_event` / `connector_fill` is judged normal (aligned with no KIS new order / fill occurring).
   - If there is a row-count increase, record only the facts of call time / ticker / quantity cumulatively (no plaintext recording of order number / account number).
3. SELECT only. 0 DDL/DML. Accumulate results as facts only in the dated section of [`./operation-notes.md`](./operation-notes.md).

## 5. Flask read-only smoke test [실행] [확인]

1. Start Flask (temporary validation stage): `python connector_app.py`. Code defaults: host `127.0.0.1`, port `5000`, debug `False`.
2. Confirm there is no trace of `CONNECTOR_DEBUG=True` in the startup log. In normal operation mode it must be forced to `false` ([`./design.md`](./design.md) §8.2).
3. Within the same EC2, call only read-only endpoints:
   - `curl -s http://127.0.0.1:5000/api/v1/view/<placeholder-path>`
   - Confirm a 200 response from read-only endpoints such as balance / holdings / order history.
4. Never call the new-order endpoints (`/api/v1/buy/...`, `/api/v1/sell/...`, `/api/v1/cancel/...`, `/api/v1/modify/...`).
5. Validation of operation-ready candidates (`connector_quote_realtime.py` / `connector_quote_closed.py` / `connector_view_service.py`) is the responsibility of a follow-up phase. Out of scope for this runbook. [준비]

## 6. Confirm Instance Role / Access Key non-use [확인]

1. `aws sts get-caller-identity` — confirm the `Arn` is of the form `arn:aws:sts::<account-id>:assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>`.
2. `aws configure list` — confirm the `access_key` Source is `iam-role` or `Ec2InstanceMetadata`.
3. Confirm `~/.aws/credentials` does not exist:
   ```text
   test -f ~/.aws/credentials && echo "FOUND" || echo "OK"
   ```
4. Zero access-key patterns inside `~/.aws/config`, dotfiles, systemd EnvironmentFile:
   ```text
   grep -rEi "^aws_access_key_id|^aws_secret_access_key|AWS_ACCESS_KEY_ID=|AWS_SECRET_ACCESS_KEY=" ~/.aws/config ~/.bashrc ~/.profile ~/.bash_profile /etc/systemd/system 2>/dev/null
   ```
5. Confirm the 4 Secrets Manager `describe-secret` entries return metadata normally. 0 automatic `GetSecretValue` calls (operator only).
6. Confirm the 6 SSM `get-parameters-by-path /portfolio/paper/marketconnector` entries return normally.

## 7. Result recording [준비]

1. Record the §3 ~ §6 results in the dated cumulative section (`## YYYY-MM-DD ...`) of [`./operation-notes.md`](./operation-notes.md) as only one of the two values success / failure.
2. Do not record actual secret value / account number / RDS endpoint / account-id / actual ARN / IAM access key id / instance-id / EIP.
3. Mark each of the 7 inspection areas in [`./validation-checklist.md`](./validation-checklist.md) with one of the 4 labels.
4. Update all 8 validation results to `[O]` or to the operator's direct confirmation result ([`./design.md`](./design.md) §10).

## 8. Recovery flow on failure [복구]

1. RDS connection failure → confirm the SG matrix / confirm the Instance Profile attach / confirm the Secret name spelling / confirm the VPC Endpoint status.
2. Connector read-only failure → confirm KIS API rate limit (R-BROKER) / inspect the token issuance / reuse flow (do not print the token value) / check the Connector log (re-confirm 0 grep hits for a Flask debug flag plaintext secret exposure pattern).
3. Instance Role validation failure → if an access key is found, immediately revoke it in the IAM Console → move `~/.aws/credentials` to a backup (timestamp suffix) → re-validate the IMDSv2 + Role-only mode ([`./design.md`](./design.md) §9.6).
4. Because systemd / startup script is out of scope for this runbook, failure to enter the normal operation mode is handled in this spec's follow-up task or a separate phase. Operation at this runbook's point in time can temporarily fall back to the temporary export mode.
5. Record all [복구] results in [`./operation-notes.md`](./operation-notes.md) accumulating only date / facts / follow-up actions (no recording of the root-cause stdout body / secret value).
</content>
