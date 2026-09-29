# Operation Notes — 08-interest-crawler-and-preprocessor-ecs

This document is an operation note that accumulates, by date, the results of the work actually performed by the operator / Kiro during 08-interest-crawler-and-preprocessor-ecs. The first application environment is `aws-paper`, the region is `ap-northeast-2`, and the first validation target is the Preprocessor MS (`port-interest-preprocessor`).

## Timeline Dashboard

A summary of the operator execution history for this spec. See each date section for details.

| Date | Key result | Key decision / mitigation |
|---|---|---|
| 2026-06-10 | 2 ECR repos · Dockerfile · local build · ECR push · ECS Cluster · Task Role · Log Group · Preprocessor RunTask exitCode 0 | OD-MS-011(hybrid) premise confirmed |
| 2026-06-12 | KRX GUI collection = Windows EC2 worker separation (RDP + wrapper manual execution first pass) | KRX Secret new · download path junction · Korean literal policy |
| 2026-06-13 | SSM RunCommand → `schtasks /Run` → Scheduled Task automation entrypoint + ECS crawler rev6 Selenium Chrome smoke pass | OD-MS-015(SSM direct execution unsuitable) · hybrid first completion judgment |
| 2026-06-15 | Backend AWS E2E dry-run first · CONNECTOR_BALANCE pass · Preprocessor ECS execution success (data freshness constraint) · stale raw data issue discovered | OD-MS-020(expression correction) · OD-MS-021(dry-run safety criteria) · R-DATA-009 / R-DATA-010 new |
| 2026-06-16 | non-GUI ECS rev7(`paper-20260616-nongui`) new · RunTask exitCode 0 · 8 raw types freshness recovered · KRX GUI Autologon + Administrator interactive session + Scheduled Task automation demonstrated | OD-MS-022 new · R-SEC-009 · R-AUTO-017 new · hybrid structure complete |
| 2026-06-17 | Daily AWS 17-step E2E complete (Step 2 crawler + Step 3 preprocessor pass · feature freshness `2026-06-16`) | R-DATA-009 / R-DATA-010 mitigation first demonstration |
| 2026-06-20 | Daily wrapper final check · 6/18 duplicate execution attempt safely halted · Step 12 not executed · 6/19 KRX raw freshness recovery confirmed | R-AUTO-007 identified (Scheduled Task trigger success → Step 2 SUCCESS risk) |
| 2026-06-21 | Step 2 success determination strengthened · `interest_krx_raw_validate_daily.py` new · DB validation SSM step linkage · worker stopped fail-closed | OD-MS-026 new · R-AUTO-020 new mitigation |
| 2026-06-22 | Daily wrapper 2nd real operational run · Step 2 / Step 3 regression 0 cases | R-AUTO-007 / R-AUTO-020 mitigation regression 0 cases |

## Open Risks & Next Checks

A summary of the latest status below the Timeline Dashboard. See each date section for detailed basis.

### Currently remaining risks (Open)

| ID | Summary | Status | Detection / Mitigation summary |
|---|---|---|---|
| R-AUTO-020 | When Step 2 is treated as SUCCESS based only on Scheduled Task trigger success, KRX raw non-load propagates past Step 3 | 🟢 Mitigated (2026-06-22 regression 0 cases) | Chrome / chromedriver best-effort reset + Running→Ready wait + Last Result 0 confirmation + latest worker log output + KRX raw DB validation + worker stopped fail-closed |
| R-AUTO-016 | Entering Step 2 SUCCESS while crawler worker EC2 is stopped | 🟢 Mitigated | fail-closed handling from 2026-06-21 / skip abolished |
| R-AUTO-017 | Chrome / chromedriver stale process residue | 🟢 Mitigated | best-effort reset on Step 2 entry / failure kept as warning |
| R-AUTO-007 | wrapper success exit does not guarantee actual DB load | 🟢 Mitigated | task 57 reclaimed / `KrxDbValidationCommandId` step result recorded |
| R-DATA-009 / R-DATA-010 | non-GUI raw freshness insufficient → Preprocessor new feature date not generated | 🟢 Mitigated | 2026-06-16 rev7 operational path created + 2026-06-17 17-step E2E passed |
| R-SEC-009 | Windows worker Autologon credential exposure time | 🟠 Open (paper-limited exception) | EC2 stop after work ends / 0 cases of credential values recorded in plaintext in documents |
| interest_foreignindex_raw HANGSENG/NIKKEI225/SHANGHAI NULL | Some index NULL Data (non-blocker) | 🟠 Open | Not a Preprocessor blocker / follow-up check |
| interest_ticker_value_raw stale | latest date 2026-03-09 (separate domain) | 🟠 Open | Excluded from this dry-run blocking factor / separate follow-up |
| EC2 lifecycle automation | `/tmp/inject-env.sh` lost after MarketConnector EC2 stop / start | 🟠 Open | task 59 follow-up separation / 03 spec responsibility |

### Next Checks

- [ ] task 54: EventBridge Scheduler → SSM RunCommand → `schtasks /Run` regular trigger linkage
- [ ] task 55: ECS RunTask + SSM RunCommand hybrid orchestration in Step Functions
- [ ] task 56: EC2 worker log collection based on CloudWatch Logs Agent or SSM output
- [ ] task 59: Specify stop procedure after EC2 worker work completion (idle cost reduction)
- [ ] task 78: Automate raw / feature freshness validation SQL after preprocessor execution
- [ ] task 111: Review whether to display `KrxDbValidationCommandId` / latest worker log / Step 2 validation results in View Daily Batch

### Key Fact Preservation (must be maintained when editing)

When editing, do not delete / condense the following basis. Defend against documentation readability improvement leading to fact loss.

- KRX GUI worker Last Result `0` (or `0x0`) = SUCCESS condition. Do not treat as SUCCESS based only on the `SUCCESS: Attempted to run the scheduled task` marker.
- Scheduled Task trigger success ≠ Step 2 SUCCESS. All 6 success conditions (non-GUI ECS exitCode 0 / worker EC2 running / Running→Ready return / Last Result 0 / latest log output / DB validation pass) are required.
- KRX raw DB validation: `interest_program_raw` / `interest_shortsell_raw` require `max(trade_date)` ≥ ExpectedKrxRawDate, row_count > 0. On failure, exit code 30.
- worker EC2 stopped → fail-closed, not skip. Output instanceId / state in the failure message.
- Autologon use is a security exception limited to the paper-only Windows worker. Recording credential values in plaintext is prohibited.
- SSM direct Python execution is rejected as unsuitable for SYSTEM Session 0. SSM serves only the `schtasks /Run` trigger role.
- non-GUI ECS Task Definition revision 6 = smoke only, revision 7 = daily operation. Do not confuse.
- raw freshness recovery basis numbers (2026-06-16 §3): `interest_price_raw` 1,207,904 → 1,209,624, `interest_investorflow_raw` 274,204 → 275,949, `interest_shortsell_raw` +349 row, etc. Maintain the row_count / max_date numbers.

## Record format

- Accumulate with `## YYYY-MM-DD <summary>` headers per date (same as 02 / 06 spec operation-notes).
- Record results only briefly as success / failure / on hold / not applicable / carried over. For failure cases, summarize only up to 1 line reason + 1 line action + result.
- Do not quote full AWS CLI / Console / CloudWatch logs / Task event message / docker build logs in this document (security / volume reduction).
- Record IAM Role / Policy / secret changes only as a 4-line summary of change date / changer / change reason / before·after item summary (full JSON body quotation prohibited).

## Safety principles

- Recording actual secret value, password, KIS app key, KIS app secret, account number, RDS endpoint hostname, RDS password, token, IAM access key id, account-id, actual secret ARN, actual KMS Key ARN, instance-id, image digest in plaintext in this document is prohibited. All are `[REDACTED]` or placeholder.
- Recording secret lookup results (value) is prohibited. Only success / failure + last refresh time (if needed, ISO 8601 `YYYY-MM-DDTHH:MM:SS+09:00`).
- The operator directly performs actual AWS resource creation / modification / deletion. Kiro performs only documentation writing / procedure organization / validation item organization.
- The README / AGENTS.md / CHANGELOG / docs / worklog of the 8 MS (`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) are not changed by this spec work. The Dockerfile / requirements.txt written directly by the operator are the operator's direct work and only facts are recorded in this note.
- Actual `secretsmanager:GetSecretValue` calls are by the operator only. Kiro automatic validation is `secretsmanager:DescribeSecret` metadata only.
- Maintain the same principle in follow-up work so that secret value is not exposed in plaintext in work chat / command output / console captures / operator notes (aligned with R-DOCS-001).

## 2026-06-10 08-interest-crawler-and-preprocessor-ecs operator execution record

**Summary** — ECS / ECR basic porting first result.

- Kiro: documentation / procedure organization only
- Operator: performs actual AWS / IAM / Secrets / RDS work directly

### 1. ECR Repository creation

1. Create 2 ECR repositories: complete
   1) `portfolio-interest-crawler`: creation complete
   2) `portfolio-interest-preprocessor`: creation complete
2. Environment separation policy: confirmed not to separate ECR repository per paper / live environment
   1) The same image artifact is not pushed to a duplicate repository per environment
   2) The paper / live distinction is handled in 6 items: image tag / ECS Task Definition / Secrets·SSM path / IAM Task Role / environment variables / RDS·broker settings
3. Common base image candidate: kept for follow-up review

### 2. Dockerfile / requirements newly created

1. port-interest-preprocessor: Dockerfile initially absent → operator directly created new
   1) base image: `python:3.13-slim`
   2) command: `python pre_daily.py`
   3) requirements.txt newly created: main dependencies `psycopg2-binary`, `requests`
2. port-interest-crawler: Dockerfile initially absent → operator directly created new
   1) base image: `python:3.13-slim`
   2) command: `python interest_crawler_daily.py`
   3) Chromium / chromedriver included
   4) requirements.txt newly created: main dependencies `beautifulsoup4`, `pandas`, `psycopg2-binary`, `requests`, `selenium`, `yfinance`
3. Confirmed the need for the Crawler's Selenium / Chrome / chromedriver
4. First found·fixed 1 case of a Dockerfile line-continuation character (`\`) error at the build stage (Dockerfile's `\` use, not a PowerShell backtick)

### 3. Local image build

1. preprocessor built first: complete
   1) `portfolio-interest-preprocessor:paper-20260610`
   2) `portfolio-interest-preprocessor:paper-latest`
2. crawler build: complete
   1) `portfolio-interest-crawler:paper-20260610`
   2) `portfolio-interest-crawler:paper-latest`
   3) larger image than preprocessor due to Chromium / chromedriver inclusion
3. crawler Selenium dependency build-stage risk first resolved (build success). Runtime stabilization is outside this spec's scope

### 4. ECR Push

1. preprocessor push: complete
   1) tag `paper-20260610`
   2) tag `paper-latest`
2. crawler push: complete
   1) tag `paper-20260610`
   2) tag `paper-latest`
3. image digest: confirmed directly by the operator. The actual digest value is not recorded in this note / spec deliverables (`<image-digest>` placeholder)

### 5. ECS Cluster / Task Execution Role / Task Role / Log Group preparation

1. ECS Cluster creation / confirmation: complete
   1) name: `portfolio-paper-cluster`
   2) Fargate capacity provider confirmed
   3) ACTIVE status confirmed
2. Task Execution Role creation: complete
   1) name: `portfolio-paper-ecs-task-execution-role`
   2) trust: `ecs-tasks.amazonaws.com`
   3) managed policy: `AmazonECSTaskExecutionRolePolicy` (secured basic ECR pull / CloudWatch Logs write permissions)
   4) preprocessor DB secret read inline policy added (limited to a single secret ARN basis / 0 Resource·Action wildcards)
3. Task Role creation / confirmation: complete
   1) `portfolio-paper-preprocessor-task-role`
   2) `portfolio-paper-crawler-task-role`
4. CloudWatch Log Group creation + retention 14 days: complete
   1) `/portfolio/paper/preprocessor`
   2) `/portfolio/paper/crawler`
5. RDS connection SG confirmation: complete
   1) preprocessor task SG → RDS PostgreSQL SG 5432 inbound allowance confirmed
   2) Resource wildcard / Action wildcard prohibition principle maintained
   3) actual ARN / account-id not recorded in this note

### 6. Secrets Manager / Task Definition / Preprocessor RunTask validation

1. Secrets Manager `/portfolio/paper/rds/preprocessor-app` newly created: complete
   1) newly created after confirming initial non-existence
   2) JSON multi-key method used (`host` / `port` / `dbname` / `username` / `password`)
   3) actual endpoint / password / ARN / account-id not recorded in this note
2. preprocessor Task Definition registration: complete
   1) family: `portfolio-paper-interest-preprocessor`
   2) revision: 1
   3) networkMode: `awsvpc`
   4) cpu: 512 / memory: 1024
   5) image tag: `paper-20260610`
   6) inject Secrets Manager JSON keys as env via the ECS `secrets` field
3. preprocessor ECS RunTask first validation: complete
   1) public subnet + `assignPublicIp = ENABLED` method used
   2) CloudWatch Logs output confirmed
   3) final lastStatus: `STOPPED`
   4) final exitCode: `0`
   5) `PREPROCESSOR PIPELINE END` confirmed

### 7. Issues found / handled during Preprocessor RunTask validation

1. First failure: `host` key missing in the Secrets Manager JSON secret
   1) symptom: psycopg2 attempts to connect via Unix socket `/var/run/postgresql/.s.PGSQL.5432`
   2) action: recreate `/portfolio/paper/rds/preprocessor-app` JSON secret (include all of `host` / `port` / `dbname` / `username` / `password`)
   3) result: resolved
2. Second failure: insufficient DB sequence permission
   1) symptom: `permission denied for sequence pre_marketbreadth_daily_feature_id_seq`
   2) cause: some preprocessor-related sequences remained in the `public` schema
   3) action: operator directly executed GRANT
       - grant `preprocessor_app` USAGE / SELECT on `public.pre_marketbreadth_daily_feature_id_seq`
       - grant `preprocessor_app` USAGE / SELECT on `public.pre_macroeconomic_daily_feature_id_seq`
   4) confirmation: `preprocessor_app` USAGE / SELECT permission is true for the above 2 sequences
3. Final re-execution: preprocessor ECS Task exitCode `0` confirmed

### 8. Crawler status (carry-over organization)

1. Completed items
   1) crawler Dockerfile / requirements newly created
   2) image build including Selenium / Chromium / chromedriver succeeded
   3) ECR push complete (`paper-20260610` / `paper-latest`)
2. Carried-over items
   1) crawler Task Definition registration
   2) crawler RunTask runtime validation
   3) validation of whether KRX / Naver / yfinance outbound runtime is reachable
   4) 100% crawler stabilization is outside this spec's scope (follow-up spec / follow-up phase responsibility)

### 9. This date's safety / documentation record check

1. 0 cases of actual password / secret value / endpoint hostname / account-id / actual ARN / image digest / IAM access key id recorded in plaintext in this note. All are `[REDACTED]` or placeholder.
2. 0 AWS / IAM / Secrets Manager / RDS changes due to Kiro's work on this spec. All actual work was performed directly by the operator, and Kiro organized only the procedure / result / failure reason / action in documents.
3. No modification of the 8 MS code / packaging due to Kiro's work on this spec. The Dockerfile / requirements.txt of port-interest-preprocessor / port-interest-crawler were newly created by the operator's direct work (see this note §2).
4. 0 external calls / crawling / orders / RDS DDL·DML due to Kiro's work on this spec. See this note §6 / §7 for the 2 RDS sequence GRANTs performed directly by the operator and the preprocessor pipeline RunTask result.
5. Maintain the same principle in follow-up work so that secret value is not exposed in plaintext in work chat / command output / console captures / operator notes (aligned with R-DOCS-001).

### 10. Follow-up handover

1. crawler Task Definition / RunTask runtime validation — proceed as follow-up work
2. crawler outbound (KRX / Naver / yfinance / Selenium) operational stabilization — follow-up spec / follow-up phase responsibility
3. additional check of remaining `public` schema sequences — follow-up check of whether domain sequences other than preprocessor-related remain (carried over)
4. 0 updates to this spec's deliverables (requirements.md / design.md / tasks.md). 1 new creation of this note. runbook.md / validation-checklist.md are written separately after operator execution

## IAM change record template (add per date as needed)

When this spec's Task Execution Role / Task Role / Permission Policy / Resource ARN list changes, accumulate the record in this section. 1 change = 4-line summary format.

```
## YYYY-MM-DD IAM 변경
- 변경 일자: YYYY-MM-DDTHH:MM:SS+09:00
- 변경자: [운영자 식별자(닉네임 / 직무)]  # 실제 IAM user / email 평문 금지
- 변경 사유: [한 줄 요약]
- 변경 전 / 후 항목 요약: [Resource 추가 / 삭제 항목 수, Action 추가 / 삭제 항목 수, KMS statement 추가 여부 등 — JSON 본문 전체 인용 금지]
```

## 2026-06-12 First validation of KRX GUI-dependent collection based on Windows EC2 worker

**Summary** — First validation of KRX program / shortsell collection based on Windows EC2 worker.

- Classification decision: KRX GUI collection = Windows EC2 worker / non-GUI crawler = ECS Fargate Task candidate maintained / preprocessor = ECS Fargate Task maintained
- Kiro: documentation / procedure / validation item organization only
- Operator: performs actual AWS / IAM / Secrets Manager / EC2 / RDS work directly

### 1. EC2 worker environment confirmation

1. RDP connection after EC2 worker start: success
2. Work directory `C:\portfolio\port-interest-crawler` confirmation: normal
3. venv `C:\portfolio\venvs\interest-crawler` activate: normal
4. Python interpreter version: 3.13.5
5. EC2 IAM Role recognition: `portfolio-paper-crawler-worker-role` recognition confirmed (actual ARN / account-id not recorded in this note)
6. Pre-check: confirmed failure via `localhost:5432` connection attempt while DB environment variables not injected (expected behavior)

### 2. RDS Secret-based DB environment variable injection

1. DB environment variable injection based on Secrets Manager after running `C:\portfolio\load-crawler-db-env.ps1`: success
2. Secret used: `/portfolio/paper/rds/crawler-app` (actual ARN / account-id not recorded in this note)
3. Injected environment variable confirmation
   1) `INTEREST_DB_HOST`: injection confirmed
   2) `INTEREST_DB_PORT`: injection confirmed
   3) `INTEREST_DB_NAME`: injection confirmed
   4) `INTEREST_DB_USER`: injection confirmed
   5) `INTEREST_DB_PASSWORD`: injection confirmed (value not output / not recorded)
4. 0 cases of secret value plaintext exposure in work chat / command output / note (aligned with R-DOCS-001)

### 3. Download path mismatch action

1. Code-expected path: `interest_program.py` assumes use of `C:\Users\USER\Downloads`
2. Actual Chrome download path: `C:\Users\Administrator\Downloads`
3. Action
   1) remove the empty folder `C:\Users\USER\Downloads`
   2) junction `C:\Users\USER\Downloads` → `C:\Users\Administrator\Downloads`
4. Result: after applying the junction, `interest_program.py` normally recognizes the CSV file and collection succeeds
5. Follow-up: automatically confirm junction existence / consistency check within the wrapper on every execution

### 4. KRX program standalone collection result

1. Prerequisite execution: `interest_krx_login_new.py`
   1) `KRX ID/PW Login Success`
   2) `KRX Login Ready`
2. `interest_program.py` standalone execution: success
3. Processing result per collection date
   1) 2026-06-09: Collected
   2) 2026-06-10: Collected
   3) 2026-06-11: Collected
4. DB validation — `interest_program_raw`
   1) row count change: 543 → 546 (+3)
   2) row count per date: 2026-06-09 / 2026-06-10 / 2026-06-11, 1 each
   3) sample column confirmation: `trade_date`, quantity / amount column group, `source`, `source_version`, `created_at`, `updated_at`, `collected_at` all normal

### 5. KRX shortsell standalone collection result

1. `interest_shortsell.py` standalone execution: success
2. Processing result per collection date
   1) 2026-06-09: 349 Company Collected
   2) 2026-06-10: 349 Company Collected
   3) 2026-06-11: 349 Company Collected
3. DB validation — `interest_shortsell_raw`
   1) row count change: 189158 → 190205 (+1047)
   2) row count per date: 2026-06-09 / 2026-06-10 / 2026-06-11, 349 each
   3) number of tickers × number of days: 349 × 3 = 1047 match
   4) sample column confirmation: `trade_date`, `ticker_code`, `ticker_name`, `short_volume`, `short_amount`, `total_volume`, `short_ratio`, `source`, `source_version`, `collected_at` all normal

### 6. Korean literal / encoding validation

1. Creation and execution of `encoding_test.py` containing Korean: normal operation confirmed
2. Korean string modification via the Python patch script method: normal (BOM removal + utf-8 save)
3. Validation method
   1) confirm automatic BOM removal with `read_text(encoding="utf-8-sig")`
   2) confirm utf-8 save with `write_text(encoding="utf-8")`
4. Decision: in the future, when Python source modification is needed inside the EC2, use only the Python patch script method instead of PowerShell's `Get-Content` / `Set-Content` method (to prevent Korean literal corruption)

### 7. KRX Secret Manager integration

1. New creation of a KRX login-only Secret: complete
   1) Secret path: `/portfolio/paper/krx/crawler-login`
   2) JSON key: `username`, `password`
   3) 0 cases of Secret value plaintext recorded in this note (only `[REDACTED]` / "injected from Secrets Manager")
2. First permission-missing case
   1) symptom: `AccessDeniedException` occurs because `portfolio-paper-crawler-worker-role` has no new KRX secret read permission
   2) operator IAM change — 4-line summary
       - change date: 2026-06-12
       - changer: operator (job identifier only)
       - change reason: resolve missing KRX login secret read
       - before / after item summary: added inline policy to `portfolio-paper-crawler-worker-role`, allowed actions `secretsmanager:DescribeSecret` / `secretsmanager:GetSecretValue`, target Resource limited to the KRX crawler login secret (0 Resource·Action wildcards)
   3) result: after adding permission, secret lookup succeeds on the EC2 worker
3. KRX environment variable injection
   1) `C:\portfolio\load-krx-env.ps1` newly created
   2) `KRX_USER_ID` injection confirmed
   3) `KRX_USER_PASSWORD` injection confirmed (value not output / length only checked)
4. Re-execution validation: `interest_krx_login_new.py` succeeds at KRX login with Secrets Manager-based credentials

### 8. EC2 worker daily wrapper

1. `C:\portfolio\run_krx_worker_daily.ps1` newly created (the actual full ps1 body is not recorded in this note / only facts recorded)
2. wrapper role (summary)
   1) venv activate
   2) DB environment variable injection based on RDS Secret
   3) KRX environment variable injection based on KRX Secret
   4) confirm download path junction existence
   5) sequential execution of `interest_krx_login_new.py` → `interest_program.py` → `interest_shortsell.py`
   6) save execution logs under `C:\portfolio\logs`
3. Log file format confirmation: `krx_worker_daily_yyyyMMdd_HHmmss.log`
4. wrapper first execution result
   1) `KRX already logged in`
   2) `KRX Login Ready`
   3) program execution success
   4) shortsell execution success
5. wrapper re-execution result: `[Collected Date] None`
   1) cause: 2026-06-09 ~ 2026-06-11 already collected → 0 new collection targets
   2) judgment: normal completion of an idempotent / no-op nature, not a failure
6. DB re-confirmation (`check_program_rows.py`, `check_shortsell_rows.py`)
   1) program: 2026-06-09 / 2026-06-10 / 2026-06-11, 1 each maintained
   2) shortsell: 2026-06-09 / 2026-06-10 / 2026-06-11, 349 each maintained

### 9. Final judgment / classification decision

1. KRX GUI-dependent collection (KRX program / KRX shortsell): use Windows EC2 worker — confirmed
2. non-GUI crawler (Naver / yfinance / KRX non-GUI path): ECS Fargate Task candidate maintained
3. preprocessor: ECS Fargate Task maintained (2026-06-10 first validation result as-is)
4. Overall Interest Crawler / Preprocessor flow: organized as a hybrid execution model (EC2 worker + ECS Fargate Task mix)
5. EC2 worker-based KRX program / shortsell first operable state: reached
6. Full automation: outside this date's scope
7. Automation follow-up (SSM RunCommand / EventBridge Scheduler / Step Functions hybrid orchestration / CloudWatch Logs Agent / automatic addition of DB validation output within the wrapper) is separated as follow-up

### 10. This date's safety / documentation record check

1. 0 cases of actual secret value / password / KRX login password / KIS app key / KIS app secret / account number / RDS endpoint hostname / IAM access key id / account-id / actual ARN / image digest / instance-id recorded in plaintext in this note. All are `[REDACTED]` or placeholder.
2. KRX login ID / password are marked only as "injected from Secrets Manager". Value not recorded.
3. 0 AWS / IAM / Secrets Manager / EC2 / RDS changes due to Kiro's work on this date. All actual work performed directly by the operator. Kiro organized only the procedure / result / failure reason / action in documents.
4. No modification of the 8 MS (`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to Kiro's work on this date.
5. The ps1 / py test / wrapper files the operator created directly inside the EC2 are recorded in this note only as facts (full file body not recorded).
6. 0 external calls / crawling / orders / RDS DDL·DML due to Kiro's work on this date. See this note §3 ~ §8 for the KRX collection results / sequence GRANT / wrapper execution performed directly by the operator.
7. Maintain the same principle in follow-up work so that secret value is not exposed in plaintext in work chat / command output / console captures / operator notes (aligned with R-DOCS-001).

### 11. Follow-up handover

1. Unattended EC2 worker execution based on SSM RunCommand: follow-up separation
2. EventBridge Scheduler → SSM RunCommand linkage: follow-up separation
3. ECS Task + EC2 worker mixed orchestration in Step Functions: follow-up separation
4. EC2 worker log collection based on CloudWatch Logs Agent or SSM output: follow-up separation
5. Automatic addition of DB validation output within the wrapper (output row count per date of `interest_program_raw` / `interest_shortsell_raw`): follow-up separation
6. Re-organization of ECS / Fargate non-GUI crawler scope (finalize the inventory of which crawler goes to the EC2 worker / which crawler goes to the ECS Fargate Task): follow-up separation
7. Specify stop procedure after EC2 worker work completion (idle time cost reduction): follow-up separation
8. KRX GUI collection headless refactoring: kept only as a long-term candidate (the current decision is to use the EC2 worker)

## 2026-06-13 SSM RunCommand automation + ECS crawler smoke first validation

**Summary** — KRX GUI automation entrypoint confirmed + ECS crawler smoke pass.

- Automation method confirmed: SSM RunCommand → `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"` → Windows Scheduled Task → Administrator interactive session → wrapper. SSM direct wrapper execution is rejected (SYSTEM Session 0 unsuitable)
- ECS crawler Task Definition revision 6 = Selenium / Chrome / outbound smoke only
- Kiro: documentation / procedure / validation item organization only
- Operator: performs actual AWS / SSM / EC2 / ECS / CloudWatch / RDS confirmation work directly
- Decision lock: [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-MS-012 / OD-MS-015

### 1. SSM management status confirmation

1. Reviewing Systems Manager nodes: complete
   1) `portfolio-paper-crawler-worker` Managed Node registration confirmed
   2) RunCommand target designation possible confirmed
   3) `AWS-RunPowerShellScript` execution possible confirmed
2. RunCommand basic execution validation: complete
   1) `hostname` execution success
   2) `whoami` result: `nt authority\system` confirmed
   3) PowerShell 5.1 execution confirmed
   4) `C:\portfolio` file access possible confirmed

### 2. SSM RunCommand direct execution attempt / failure judgment

1. wrapper direct execution attempt (SSM RunCommand → `C:\portfolio\run_krx_worker_daily.ps1`): partial complete
   1) wrapper execution start: confirmed
   2) DB Secret loading: success
   3) KRX Secret loading: success
   4) download junction confirmation: success
   5) KRX login stage: failure
2. Failure cause judgment: complete
   1) SSM RunCommand runs under Session 0 / SYSTEM account
   2) confirmed Chrome process runs under SessionId 0
   3) confirmed the RDP user session is separated as SessionId 2
   4) KRX GUI-dependent login has insufficient compatibility with the SYSTEM Session 0 direct execution method (GUI / Display / Chrome download folder / OTP session dependency)
3. Conclusion: the method where SSM RunCommand directly runs the wrapper under SYSTEM Session 0 is unsuitable for KRX GUI login. The direct execution method is not adopted.

### 3. Scheduled Task supplementary method applied

1. Scheduled Task registration: complete
   1) Task name: `Portfolio-KRX-Worker-Daily`
   2) execution user: Administrator (interactive session basis)
   3) execution target: `C:\portfolio\run_krx_worker_daily.ps1`
2. First trigger validation (Administrator RDP session basis): complete
   1) Task execution success with `Start-ScheduledTask`
   2) Chrome / Selenium GUI execution possible confirmed
3. wrapper first execution result (Scheduled Task basis): complete
   1) `KRX ID/PW Login Success` confirmed
   2) `KRX Login Ready` confirmed
   3) `interest_program` 2026-06-12 collection success
   4) `interest_shortsell` 2026-06-12 349 Company Collected confirmed
   5) `KRX worker daily wrapper DONE` normal termination confirmed

### 4. SSM-based Scheduled Task trigger validation (RDP closed)

1. Trigger the Scheduled Task via SSM RunCommand with the RDP window closed and the EC2 kept only in Running state: complete
   1) execution command: `schtasks /Run /TN Portfolio-KRX-Worker-Daily`
   2) SSM response: `SUCCESS: Attempted to run the scheduled task` confirmed
2. Scheduled Task status confirmation: complete
   1) immediately after trigger, State = `Running`
   2) after RDP reconnection, confirmed the KRX worker daily wrapper is operating on top of the Administrator interactive session
   3) final State = `Ready` (execution ended)
3. wrapper latest log confirmation: complete
   1) log file: `krx_worker_daily_20260613_021904.log`
   2) `KRX login` success confirmed
   3) `interest_program [Collected Date] None` confirmed
   4) `interest_shortsell [Collected Date] None` confirmed
   5) `DONE :: KRX worker daily` confirmed
4. `[Collected Date] None` judgment: complete
   1) cause: already collected up to 2026-06-12 → 0 new collection targets
   2) judgment: normal completion of idempotent / no-op, not a failure (aligned with 2026-06-12 §8)
5. Conclusion: the method where SSM RunCommand triggers the Scheduled Task is confirmed as the first automation method. Confirmed that automatic execution is possible with EC2 Running alone without RDP connection.

### 5. ECS crawler Task Definition / RunTask smoke validation

1. non-GUI crawler scope confirmation: complete
   1) crawler source file list confirmed
   2) Selenium / WebDriver / Chrome / `debuggerAddress` dependent files identified
   3) KRX GUI-dependent files = Windows EC2 worker separation maintained (as-is from 2026-06-12)
   4) `requests` / `yfinance`-centric files = ECS Fargate candidate maintained
   5) `interest_crawler_daily.py` calls both KRX GUI-dependent files and non-GUI files → excluded from ECS standalone execution targets
   6) crawler task and EC2 worker role boundary organization complete
2. Task Definition confirmation: complete
   1) family: `portfolio-paper-interest-crawler` revision 1 ~ 6
   2) latest revision: `portfolio-paper-interest-crawler:6`
   3) image: `portfolio-interest-crawler:paper-20260611`
   4) compatibility: Fargate
   5) cpu: 1024 / memory: 2048
   6) network mode: `awsvpc`
   7) `taskRoleArn`: `portfolio-paper-crawler-task-role`
   8) `executionRoleArn`: `portfolio-paper-ecs-task-execution-role`
   9) crawler DB Secret environment variable connection confirmed
   10) CloudWatch Logs group: `/portfolio/paper/crawler`
   11) log stream prefix: `ecs-selenium-chrome-smoke`
   12) revision 6's `command`: not the actual daily crawler entrypoint but a Selenium Chrome smoke command (for ECS / Chrome / network smoke validation)
3. RunTask smoke execution: complete
   1) Subnet: public-a / public-b confirmed
   2) Security Group: `sgroup-crawler-tasks` confirmed
   3) RunTask execution on `assignPublicIp = ENABLED` basis
   4) Task final lastStatus: `STOPPED`
   5) `stoppedReason`: `Essential container in task exited`
   6) container exitCode: `0`
4. CloudWatch Logs confirmation: complete
   1) Log group: `/portfolio/paper/crawler`
   2) Log stream: `ecs-selenium-chrome-smoke/interest-crawler/<task-id>`
   3) Selenium 4.40.0 confirmed
   4) `CHROME_BIN=/usr/bin/chromium` confirmed
   5) `CHROMEDRIVER_PATH=/usr/bin/chromedriver` confirmed
   6) `example.com` connection success
   7) Naver Finance connection success
   8) TITLE naver finance: `Npay 증권` confirmed
   9) `SELENIUM CHROME SMOKE SUCCESS` confirmed
   10) `DRIVER QUIT` confirmed
   11) `SELENIUM CHROME SMOKE END` confirmed
5. Conclusion: ECS / Fargate Selenium Chrome smoke first pass. revision 6 is for smoke validation and is separated from the actual daily crawler Task Definition registration. The actual Task Definition separation of the non-GUI crawler is the responsibility of follow-up task (58).

### 6. Hybrid execution model first completion judgment

1. KRX program / KRX shortsell: complete on EC2 worker basis
   1) confirmed the SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1` execution structure
2. non-GUI crawler candidate: separation complete on ECS Fargate basis
3. ECS crawler Task Definition basic skeleton: confirmation complete (revision 6, smoke use)
4. ECS / Fargate Selenium Chrome smoke: success
5. public subnet + `assignPublicIp = ENABLED` outbound access possible: confirmed
6. CloudWatch Logs confirmation possible: confirmed
7. Data load structure of stages before preprocessor: maintained on hybrid execution model basis
8. Judgment: Interest Crawler hybrid execution model first completion

### 7. This date's safety / documentation record check

1. 0 cases of actual secret value / password / KRX login password / KIS app key / KIS app secret / account number / RDS endpoint hostname / IAM access key id / account-id / actual ARN / image digest / instance-id / task ARN recorded in plaintext in this note. All are `[REDACTED]` or placeholder.
2. KRX login ID / password are marked only as "injected from Secrets Manager". Value not recorded.
3. 0 AWS / SSM / EC2 / ECS / CloudWatch / IAM / Secrets Manager / RDS changes due to Kiro's work on this date. All actual work performed directly by the operator.
4. No modification of the 8 MS (`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to Kiro's work on this date.
5. The Scheduled Task / wrapper / environment variable injection ps1 files the operator registered directly inside the EC2 are recorded in this note only as facts (full file body not recorded).
6. 0 external calls / crawling / orders / RDS DDL·DML due to Kiro's work on this date. See this note §1 ~ §5 for the SSM RunCommand / Scheduled Task trigger / ECS RunTask smoke / CloudWatch Logs confirmation performed directly by the operator.
7. wrapper log body / SSM response body / full CloudWatch Logs / full stdout / stderr of Selenium / Chrome are not quoted in this note (volume reduction + aligned with R-DOCS-001 / R-DOCS-002).
8. Actual `secretsmanager:GetSecretValue` calls are by the operator only. 0 secret value calls in Kiro automatic validation / this note's writing process.

### 8. Follow-up handover

1. EventBridge Scheduler → SSM RunCommand linkage (regular execution of the `schtasks /Run` trigger): follow-up separation
2. ECS Task + EC2 worker mixed orchestration in Step Functions: follow-up separation
3. EC2 worker log collection based on CloudWatch Logs Agent or SSM output: follow-up separation
4. Automatic addition of DB validation output within the wrapper (output row count per date of `interest_program_raw` / `interest_shortsell_raw`) — aligned with R-AUTO-007: follow-up separation
5. Actual Task Definition separation of the ECS / Fargate non-GUI crawler (finalize inventory + separate the smoke revision 6 from the operational Task Definition): follow-up separation
6. Specify stop procedure after EC2 worker work completion (idle time cost reduction): follow-up separation
7. Updates to this spec's deliverables (requirements.md / design.md / tasks.md) are limited to reflecting the results of this note §1 ~ §6. runbook.md / validation-checklist.md retain the responsibility to be written separately after operator execution.

## 2026-06-15 Backend AWS E2E dry-run first / Interest Crawler status re-judgment

**Summary** — First check of the Backend AWS E2E dry-run in 17-step order. Early discovery of a stale raw data issue before View entry.

Work scope:

- (a) AWS account / region / EC2 / ECS / AWS Batch pre-check
- (b) `CONNECTOR_BALANCE` first execution based on MarketConnector EC2
- (c) KRX worker re-execution based on Windows EC2 worker + confirm latest date of `interest_program_raw` / `interest_shortsell_raw`
- (d) SQL check of the latest date of non-GUI crawler `interest_*_raw`
- (e) 1 single preprocessor ECS RunTask execution + confirm DB `updated_at` refresh

Safety:

- Kiro: documentation / procedure / validation item organization only. Operator: performs actual AWS / SSM / EC2 / ECS / Batch / RDS work directly
- 0 cases of actual BUY / SELL / `--execute` order-submission series (aligned with OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 ~ R-AUTO-011)

### 1. Pre-check (AWS account / region / EC2 / ECS / AWS Batch)

1. AWS account / region confirmation: complete
   1) region: `ap-northeast-2`
   2) account confirmation complete (0 cases of account-id recorded in plaintext — only `<account-id>` placeholder)
   3) local AWS CLI basic execution possible
2. EC2 status confirmation: complete
   1) MarketConnector EC2 running confirmed
   2) Windows crawler worker running confirmed
   3) SSM managed instance Online confirmed
3. ECS status confirmation: complete
   1) `portfolio-paper-cluster` ACTIVE confirmed
   2) preprocessor task definition (family `portfolio-paper-interest-preprocessor`) confirmed
   3) Strategy Decision buy-signal task definition (family `portfolio-paper-strategy-decision-buy-signal`) confirmed
   4) Strategy Decision position-signal task definition (family `portfolio-paper-strategy-decision-position-signal`) confirmed
4. AWS Batch Research status confirmation: complete
   1) Compute Environment `portfolio-paper-strategy-research-ce`: ENABLED / VALID / Healthy confirmed
   2) Job Queue `portfolio-paper-strategy-research-queue`: ENABLED / VALID / Healthy confirmed
   3) Research Job Definition `portfolio-paper-strategy-research` active revision confirmed
   4) latest revision including Backtest report S3 upload (`portfolio-paper-strategy-research:3`) confirmed

### 2. CONNECTOR_BALANCE execution (Backend E2E dry-run No. 1)

1. Execute on MarketConnector EC2: complete
   1) work directory: `/home/ec2-user/apps/port-marketconnector`
   2) execution environment: venv python used
   3) entrypoint: SSM RunCommand
2. Initial failure cause confirmation / action: complete
   1) first failure — import smoke failure while environment variables such as `KIS_APP_KEY` not injected
   2) cause — confirmed that KIS Secrets is in JSON form (`/portfolio/paper/kis/marketconnector` JSON multi-key). The SecretString original must not be exported as-is; the JSON keys must be parsed
   3) action — corrected the Secret JSON key → environment variable mapping
3. Confirm KIS Secret key mapping: complete
   1) `APP_KEY` → `KIS_APP_KEY`
   2) `APP_SECRET` → `KIS_APP_SECRET`
   3) `PAPER_ACNT` → `KIS_PAPER_ACNT`
   4) `ACNT_PRDT_CD` → `KIS_ACNT_PRDT_CD`
4. Execution success: complete
   1) Backed up the existing `access_token.txt`
   2) New token issuance success (0 plaintext records of the token value)
   3) KIS balance API status 200
   4) paper trading balance lookup success
   5) `connector.connector_balance_snapshot` save success
   6) `connector.connector_position_snapshot` — normally handled on the basis of 0 current holdings
   7) Confirmed no legacy holdings data
5. Documentation-needed items (R-DOCS-001 alignment):
   1) KIS Secrets requires JSON key parsing (raw SecretString export prohibited)
   2) 0 plaintext records in this note of secret value / KIS app key / KIS app secret / account number / token
6. Safety check: this stage is limited to lookup-type calls + balance / position snapshot saving. 0 broker BUY / SELL order calls. 0 `--execute`.

### 3. INTEREST_CRAWLER status re-judgment (Backend E2E dry-run No. 2)

1. KRX GUI worker status: complete (2026-06-12 §1 ~ §9 / 2026-06-13 §1 ~ §6 accumulation maintained as-is)
   1) Windows EC2 worker-based KRX login success
   2) `interest_program.py` single collection success
   3) `interest_shortsell.py` single collection success
   4) `run_krx_worker_daily.ps1` wrapper creation / execution success
   5) Confirmed DB Secret (`/portfolio/paper/rds/crawler-app`) / KRX Secret (`/portfolio/paper/krx/crawler-login`) loading
   6) Download path junction handling complete (`C:\Users\USER\Downloads` → `C:\Users\Administrator\Downloads`)
   7) Confirmed program / shortsell log file creation (`C:\portfolio\logs\krx_worker_daily_*.log`)
2. 2026-06-15 KRX worker re-run result: complete
   1) `KRX already logged in` confirmed
   2) `KRX Login Ready` confirmed
   3) program execution success
   4) shortsell execution success
   5) `[Collected Date] None` output — 0 new collection targets (no trading days accumulated during 2026-06-13 / 14 + as of Monday 2026-06-15, already loaded through the prior trading day 2026-06-12)
   6) Re-confirmed DB status with `check_program_rows.py` / `check_shortsell_rows.py`
3. KRX raw latest date (as of 2026-06-15):
   1) `interest_program_raw` latest date: 2026-06-12
   2) `interest_shortsell_raw` latest date: 2026-06-12
   3) As of Monday 2026-06-15, loaded through the prior trading day Friday 2026-06-12 (KRX trading day normal)
4. non-GUI crawler status: incomplete / promoted to follow-up
   1) ECS / Fargate Selenium Chrome smoke validation complete (2026-06-13 §5)
   2) Not full actual daily raw collection complete — non-GUI daily operational Task Definition / command separation needed (task 58 follow-up)
   3) Full raw freshness validation failed — the 7 non-GUI raw types are not loaded through the prior trading day
5. non-GUI raw latest date SQL check result (as of 2026-06-15):
   1) `interest_agency_raw` latest date: 2026-06-11
   2) `interest_news_raw` latest date: 2026-06-11
   3) `interest_commodity_raw` latest date: 2026-06-08
   4) `interest_foreignindex_raw` latest date: 2026-06-08
   5) `interest_investorflow_raw` latest date: 2026-06-08
   6) `interest_marketbreadth_raw` latest date: 2026-06-08
   7) `interest_price_raw` latest date: 2026-06-08
   8) `interest_ticker_value_raw` latest date: 2026-03-09 — excluded from this dry-run's core blocking factors (separate follow-up separation)
6. Impact analysis:
   1) Even if the Preprocessor runs, new feature date generation is limited due to insufficient input raw freshness (aligned with §4 below)
   2) The data freshness basis before moving to Research / Decision is weak — this phase pass of the Backend E2E dry-run needs reinforcement
   3) On the Backend E2E dry-run basis, No. 2 `INTEREST_CRAWLER` is re-classified as **not complete but partial complete / promoted to follow-up**
7. Expression correction (R-DOCS-001 / this date's record alignment):
   1) The existing expressions "Interest Crawler completion: complete" / "Interest Crawler is first-completed on the hybrid execution model basis" are overstatements
   2) Corrected expressions — "Interest Crawler hybrid first implementation: partial complete" / "the KRX GUI worker is first-completed to an operable state" / "the ECS / Fargate crawler is smoke-validation complete" / "the non-GUI daily raw collection operational path and full raw freshness validation are follow-up"
   3) First empirical demonstration of the principle that the "complete" notation is used only when confirmed through actual data loading / freshness validation

### 4. PREPROCESSOR ECS Task single run (Backend E2E dry-run No. 3)

1. ECS RunTask execution: complete
   1) cluster: `portfolio-paper-cluster`
   2) task definition: `portfolio-paper-interest-preprocessor:1`
   3) launch type: `FARGATE`
   4) networkMode: `awsvpc`
   5) subnet: public-a / public-b used
   6) `assignPublicIp = ENABLED`
   7) Security Group: `sgroup-preprocessor-tasks`
2. Execution result: success
   1) lastStatus: `STOPPED`
   2) desiredStatus: `STOPPED`
   3) stopCode: `EssentialContainerExited`
   4) stoppedReason: `Essential container in task exited`
   5) container: `interest-preprocessor`
   6) exitCode: 0
   7) execution time about 3 minutes 43 seconds
3. CloudWatch Logs check:
   1) Confirmed `/portfolio/paper/preprocessor` log stream creation
   2) Latest log stream `storedBytes = 0` — log-body-based validation is limited
   3) Judged the Task successful on the exitCode 0 basis
   4) 0 full-body log quotations (R-DOCS-001 / this note's safety principle alignment)
4. DB reflection check:
   1) The preprocessor table `updated_at` was updated to 2026-06-15 11:03:55+00 (KST 2026-06-15 20:03:55)
   2) Matches the ECS Task execution time
   3) Judged that the DB write path operated
5. New feature date check:
   1) A new 2026-06-15 feature date was not confirmed
   2) Appears to be an existing feature row update / recalculation form
   3) The cause is not a preprocessor failure but the raw freshness shortage in §3 (especially `interest_price_raw` / `interest_marketbreadth_raw` / `interest_investorflow_raw` / `interest_commodity_raw` / `interest_foreignindex_raw` / `interest_news_raw` / `interest_agency_raw` not loaded through the prior trading day)
6. Conclusion:
   1) The PREPROCESSOR ECS execution path is a success — exitCode 0 / DB write operated
   2) However, data update is limited due to insufficient input raw freshness
   3) On the Backend E2E dry-run basis, PREPROCESSOR is marked as **execution complete** but with a **data freshness constraint** recorded together

### 5. First empirical demonstration of Secret / IAM permission separation

1. preprocessor secret lookup attempt from MarketConnector EC2: failure (expected behavior)
   1) call — `/portfolio/paper/rds/preprocessor-app` `GetSecretValue`
   2) response — `AccessDeniedException` confirmed
   3) principal — `portfolio-paper-marketconnector-ec2-role`
   4) cause — because the MarketConnector EC2 role has no preprocessor secret read permission (OD-SEC-006 alignment — service prefix separation)
2. Judgment:
   1) Judged not a failure but the per-MS Secret access separation (OD-SEC-006 / 03 §13) operating normally
   2) **Do not add** preprocessor secret read permission to the MarketConnector EC2 role
   3) preprocessor DB checks are performed via (a) the preprocessor ECS Task (`portfolio-paper-preprocessor-task-role`) or (b) the operator's local PC SSM Port Forwarding (OD-NET-010 / OD-NET-011)
3. Additional record (operator local PC tool environment):
   1) RDS port-forwarding is possible via SSM `StartPortForwardingSessionToRemoteHost`
   2) Pasting the PowerShell prompt `>>` together into the command causes a `StreamAlreadyRedirected` error — the actual command must not include `>>` (operator local PC tip)
4. 0 IAM changes: no permission change to the MarketConnector EC2 role / preprocessor task role / crawler task role this date. The AccessDenied occurrence is recorded only as first empirical demonstration of policy consistency.

### 6. Backend AWS E2E dry-run 17-step progress status (as of 2026-06-15)

This table is a dry-run checklist organizing this date's backend AWS-side execution status on the basis of the local View Daily Batch 17-step order. All actual BUY / SELL / `--execute` order-submission-family are 0 (OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 / R-AUTO-010 / R-AUTO-011 alignment).

| # | Step | This date's status | Note |
|---|------|-------------|------|
| 1 | CONNECTOR_BALANCE | complete | §2 / MarketConnector EC2 / KIS balance API 200 / snapshot saved |
| 2 | INTEREST_CRAWLER | partial complete / promoted to follow-up | §3 / KRX GUI worker complete / non-GUI raw freshness insufficient |
| 3 | PREPROCESSOR | execution complete (data freshness constraint) | §4 / ECS Task exitCode 0 / DB `updated_at` updated / 0 new feature dates |
| 4 | BACKTEST_RESEARCH | not proceeded | Separate from 2026-06-15 §1 ~ §7 (09 spec) — in this dry-run flow, re-run planned after raw freshness recovery |
| 5 | BACKTEST_REPORT | not proceeded | same as above |
| 6 | DAILY_BUY_SIGNAL | not proceeded | Maintain order so Research runs before Decision (this date's safety criterion) |
| 7 | DAILY_POSITION_SIGNAL | not proceeded | same as above |
| 8 | DAILY_BUY_EXECUTION | not proceeded / dry-run skip planned | Safety criterion — `--execute` order-submission-family execution prohibited |
| 9 | DAILY_SELL_EXECUTION | not proceeded / dry-run skip planned | same as above |
| 10 | DAILY_AUTO_SELL | not proceeded / dry-run skip planned | same as above |
| 11 | DAILY_AUTO_BUY | not proceeded / dry-run skip planned | same as above |
| 12 | MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE | not proceeded / dry-run skip planned | R-AUTO-009 / R-AUTO-010 / R-AUTO-011 alignment — paper operational environment activation on hold |
| 13 | CONNECTOR_ORDER_CHECK | not proceeded / dry-run skip planned | fill / position sync automatic retry prohibited (OD-SAFE-004) |
| 14 | SYNC_SELL_FILL | not proceeded / dry-run skip planned | same as above |
| 15 | SYNC_BUY_FILL | not proceeded / dry-run skip planned | same as above |
| 16 | SYNC_BUY_POSITION | not proceeded / dry-run skip planned | same as above |
| 17 | BALANCE_REFRESH | not proceeded | step 17 position maintained (View Daily Batch 17-step order alignment) |

### 7. This date's safety / document record check

1. 0 plaintext records in this note of actual secret value / KIS app key / KIS app secret / account number / token / RDS endpoint hostname / RDS password / IAM access key id / account-id / actual ARN / image digest / instance-id / task ARN. All `[REDACTED]` or placeholder.
2. The KIS Secret JSON key → environment variable mapping (`APP_KEY` → `KIS_APP_KEY` / `APP_SECRET` → `KIS_APP_SECRET` / `PAPER_ACNT` → `KIS_PAPER_ACNT` / `ACNT_PRDT_CD` → `KIS_ACNT_PRDT_CD`) records only the mapping fact / does not record values.
3. 0 AWS / SSM / EC2 / ECS / Batch / IAM / Secrets Manager / RDS / GRANT changes due to Kiro's work this date. All actual work was performed directly by the operator, and Kiro only organized the procedure / result / failure cause / action into documents.
4. No modifications to the 8 MS (`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to Kiro's work this date.
5. broker / KIS calls — only `CONNECTOR_BALANCE`-limited lookup-type calls occurred (KIS balance API status 200 / paper trading balance lookup). 0 new orders / buys / sells / cancels / modifies / `--execute`. 0 fill / position sync automatic retries.
6. 0 RDS DDL / DML is limited to the normal flow of `connector_balance_snapshot` / `connector_position_snapshot` insert + preprocessor pipeline. 0 direct SQL changes.
7. 0 aws-live work — this date is `aws-paper` limited.
8. 0 quotations in this note of the CloudWatch Logs body / Selenium / Chrome stdout / stderr / SSM response body / full docker build log.
9. The full body of the wrapper log (`krx_worker_daily_*.log`) is not recorded in this note — only facts (log file name / `[Collected Date] None` output / `KRX login` success or not) are recorded.

### 8. Follow-up handover

1. Organize the non-GUI Interest Crawler operational execution path — separate an execution path excluding the KRX GUI stage from `interest_crawler_daily.py` + separate the non-GUI crawler command for ECS Fargate (task 58 follow-up)
   1) Target candidates — `interest_news.py` / `interest_agency.py` / `interest_foreignindex.py` / `interest_commodity.py` / `interest_macroeconomic.py` / `interest_price.py` / `interest_investorflow.py` / `interest_marketbreadth.py`
   2) Exclusion candidates — `interest_krx_login_new.py` / `interest_program.py` / `interest_shortsell.py` / `interest_ticker_value.py`
2. raw freshness recovery — load `interest_price_raw` / `interest_investorflow_raw` / `interest_marketbreadth_raw` / `interest_commodity_raw` / `interest_foreignindex_raw` / `interest_news_raw` / `interest_agency_raw` through the prior trading day
3. preprocessor re-run — after raw freshness recovery, re-run `portfolio-paper-interest-preprocessor` → confirm exitCode 0 → confirm feature table max date / `updated_at` → confirm whether a new feature date is generated
4. Resume Backend E2E dry-run — proceed in the order `BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL`. Research runs before Decision. The order-submission / execution family only skips or performs dry-run per the safety criterion
5. Expression unification — the existing document expressions "Interest Crawler completion: complete" / "Interest Crawler is first-completed on the hybrid execution model basis" are corrected per this date's decision alignment (WORKLOG.md / followups-overview.md / operator-decisions.md OD-MS-020 alignment)
6. This date's dry-run result is recorded not as a failure but as a **validation success that discovered the stale raw data issue early** — Connector Balance / Preprocessor ECS execution path success + Interest Crawler re-classification + non-GUI daily operational follow-up separation

## 2026-06-16 Crawler data non-collection resolution + KRX EC2 automation success

**Summary** — Resolved the 2026-06-15 stale raw data issue + reinforced the first automation entry point of the Hybrid execution model.

Work scope:

- (a) Diagnose the Crawler data non-collection cause (rev6 = Selenium / Chrome smoke command / original `interest_crawler_daily.py` includes the KRX GUI stage / non-GUI orchestration absent)
- (b) Newly create the non-GUI crawler ECS / Fargate operational path + RunTask success (`portfolio-paper-interest-crawler:7` / `paper-20260616-nongui`)
- (c) raw freshness recovery (6 non-GUI types + 2 KRX types + news / agency)
- (d) Windows EC2 worker Autologon bootstrap + Administrator interactive session empirical demonstration
- (e) KRX login / program / shortsell 2026-06-15 loading success via the SSM RunCommand → `schtasks /Run` → Scheduled Task flow

Safety:

- Kiro: document / procedure / validation item organization only. Operator: directly performs the actual AWS / SSM / EC2 / ECS / ECR / Docker / IAM / Secrets Manager / RDS work
- 0 actual BUY / SELL / `--execute` order-submission-family (OD-SAFE-001 ~ OD-SAFE-004 / OD-MS-021 / R-AUTO-009 ~ R-AUTO-011 alignment)

### 1. Confirm Crawler data non-collection cause

1. Re-confirm the actual behavior of ECS / Fargate revision 6: complete
   1) `portfolio-paper-interest-crawler:6`'s `command` was a Selenium / Chrome / outbound smoke command, not actual daily raw collection (2026-06-13 §5 alignment)
   2) Even if revision 6 RunTask passes, it does not lead to `interest_*_raw` prior-trading-day loading
   3) Expression correction alignment — first empirical demonstration that "ECS · Fargate crawler smoke validation complete" and "reaching the non-GUI daily raw collection operational path" are different stages
2. Confirm the ECS / Fargate direct-execution unsuitability of the original `interest_crawler_daily.py`: complete
   1) Includes the KRX GUI stage (`interest_krx_login_new` / `interest_program` / `interest_shortsell`)
   2) Unsuitable for standalone execution due to the ECS / Fargate Task's Display non-support / Chrome download folder / KRX OTP / session stateful dependency
   3) Conclusion: maintain the decision to exclude from ECS / Fargate standalone execution targets (2026-06-13 §5 / 2026-06-15 §3 alignment)
3. Organized the absence of non-GUI orchestration / Task Definition as the core cause: complete
   1) revision 6 = smoke-only
   2) An operational Task Definition for actual daily raw collection did not exist
   3) Therefore this date's first work = newly create non-GUI-dedicated orchestration + register an operational Task Definition + validate RunTask

### 2. Create the non-GUI crawler ECS / Fargate operational path

1. New creation of `interest_crawler_daily_nongui.py`: complete
   1) Creation method: Python patch script method (`encoding="utf-8"` save / prevent Korean literal corruption / 2026-06-12 §6 alignment)
   2) Static validation: `py_compile` passed + AST import validation passed
   3) In-Docker-image file inclusion check passed
   4) The full body of this file is not quoted in plaintext in this note / spec deliverables (R-DOCS-001 alignment — fact records only)
2. Exclude KRX GUI family (3 types): complete
   1) `interest_krx_login_new`
   2) `interest_program`
   3) `interest_shortsell`
3. Include non-GUI family (8 types): complete
   1) `interest_news`
   2) `interest_agency`
   3) `interest_foreignindex`
   4) `interest_commodity`
   5) `interest_macroeconomic`
   6) `interest_price`
   7) `interest_investorflow`
   8) `interest_marketbreadth`
4. Docker rebuild + ECR push: complete
   1) image tag: `paper-20260616-nongui`
   2) image digest: operator directly confirmed (the actual value is not recorded in this note — `<image-digest>` placeholder)
   3) ECR repository: `portfolio-interest-crawler` (reused as-is from 2026-06-10 §1 / environment-non-separation policy alignment)
5. ECS Task Definition registration: complete
   1) family: `portfolio-paper-interest-crawler`
   2) revision: 7 (6 = Selenium Chrome smoke-only / 7 = separated for non-GUI daily operation)
   3) image: `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:paper-20260616-nongui`
   4) launch type: FARGATE / awsvpc
   5) cpu: 1024 / memory: 2048
   6) command: `python` / `interest_crawler_daily_nongui.py`
   7) Log group: `/portfolio/paper/crawler`
   8) Log stream prefix: `ecs-crawler-nongui-daily`
   9) crawler-app DB Secret environment variable injection maintained (as-is from 2026-06-10 §6)
6. RunTask result: complete
   1) cluster: `portfolio-paper-cluster`
   2) Task Definition: `portfolio-paper-interest-crawler:7`
   3) RunTask submit: success
   4) failures: 0
   5) final task status: `STOPPED`
   6) stopCode: `EssentialContainerExited`
   7) container exitCode: `0`
   8) execution time: about 9 minutes 51 seconds
   9) Confirmed CloudWatch log stream creation — 0 full-body plaintext quotations (R-DOCS-001 alignment)
   10) Confirmed all steps SUCCESS (all 8 steps): `interest_news` / `interest_agency` / `interest_foreignindex` / `interest_commodity` / `interest_macroeconomic` / `interest_price` / `interest_investorflow` / `interest_marketbreadth`

### 3. raw freshness recovery (DB validation)

1. `interest_price_raw`: complete
   1) max date change: 2026-06-08 → 2026-06-15
   2) row count change: 1,207,904 → 1,209,624
   3) 2026-06-15 = 324 Price Collected
2. `interest_investorflow_raw`: complete
   1) max date change: 2026-06-08 → 2026-06-15
   2) row count change: 274,204 → 275,949
   3) 2026-06-15 = 349 Investor Collected
3. `interest_marketbreadth_raw`: complete
   1) max date change: 2026-06-08 → 2026-06-15
   2) row count change: 4,788 → 4,793
4. `interest_commodity_raw`: complete
   1) max date change: 2026-06-08 → 2026-06-15
   2) row count change: 29,132 → 29,162
   3) 2026-06-15 = 6 Commodity Collected
5. `interest_foreignindex_raw`: partial complete (non-blocker candidate)
   1) max date change: 2026-06-08 → 2026-06-15
   2) row count change: 33,738 → 33,766
   3) Confirmed some indices NULL Data on 2026-06-15 — `HANGSENG` / `NIKKEI225` / `SHANGHAI`
   4) Separated as some indices' data gap rather than a full failure — recorded as a follow-up check candidate, not a Preprocessor blocker
6. `interest_news_raw`: complete
   1) max date change: 2026-06-11 → 2026-06-16
   2) row count change: 68,881 → 71,614
   3) 2026-06-16 = 349 News Collected
7. `interest_agency_raw`: complete
   1) max date change: 2026-06-11 → 2026-06-16
   2) row count change: 49,018 → 49,044
   3) 2026-06-16 = 15 Agency Reports Collected
8. `interest_macroeconomic_raw`: complete
   1) max date change: 2026-06-08 → 2026-06-15
   2) row count change: 75,742 → 75,777
   3) 2026-06-15 = 7 Macro Collected
9. Completion judgment:
   1) news / agency: updated to 2026-06-16
   2) price / investorflow / marketbreadth / commodity / foreignindex / macro: updated to 2026-06-15
   3) Main cause of stale raw data resolved (2026-06-15 §3 / R-DATA-009 / R-DATA-010 mitigation alignment)
   4) Reached a level usable as Preprocessor input data (re-run is follow-up §5 / task 77)

### 4. KRX EC2 worker automation re-validation

1. Re-confirm SSM direct wrapper / Python execution unsuitability: complete
   1) As-is from 2026-06-13 §2 — SSM RunCommand is Session 0 / SYSTEM account non-interactive execution
   2) The Chrome GUI is not displayed on the Administrator RDP screen
   3) Unsuitable for headless / non-interactive collection due to KRX login / nos_setup / keyboard security / iframe constraints
   4) Decision: maintain excluding SSM direct Python execution from the operational method (2026-06-13 OD-MS-015 alignment)
2. Exclude headless / non-interactive KRX collection: complete
   1) Judged that KRX headless / non-interactive collection is in an impossible direction per local validation
   2) Excluded from the operational method (the previous "long-term candidate" expression is corrected this date to "excluded per local validation / excluded from the operational method")
3. Autologon bootstrap: complete
   1) Used Microsoft Sysinternals Autologon
   2) Administrator auto-login setup complete
   3) Confirmed SSM managed instance Online after EC2 reboot
   4) Confirmed Administrator console session Active via `query user` output
       - USERNAME: `administrator`
       - SESSIONNAME: `console`
       - ID: `1`
       - STATE: `Active`
   5) `whoami` output is `nt authority\system` — normal because SSM RunCommand itself runs as SYSTEM
   6) The required condition was the existence of a separate Administrator console interactive session, and the Active confirmation passed
   7) 0 plaintext records in this note of the Autologon credentials (DefaultUserName / DefaultPassword) (R-DOCS-001 / R-SEC-009 alignment)
   8) Autologon use is a paper-only Windows worker-limited security exception (R-SEC-009 new)
   9) 0 plaintext records in this note of Administrator / KRX / DB password
4. First empirical demonstration of the final automation execution path: complete
   1) SSM RunCommand
   2) `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"`
   3) Windows Scheduled Task
   4) Administrator console interactive session
   5) `powershell.exe -ExecutionPolicy Bypass -File C:\portfolio\run_krx_worker_daily.ps1`
   6) `python interest_krx_login_new.py`
   7) `python interest_program.py`
   8) `python interest_shortsell.py`
5. Scheduled Task result: complete
   1) TaskName: `Portfolio-KRX-Worker-Daily`
   2) Logon Mode: `Interactive only`
   3) Run As User: `Administrator`
   4) `schtasks /Run` response: `SUCCESS: Attempted to run the scheduled task "Portfolio-KRX-Worker-Daily"`
   5) running status: Status `Running` / Last Result `267009`
   6) execution complete status: Status `Ready` / Last Result `0`
   7) Last Run Time: 2026-06-16 04:55:49
   8) wrapper local log: `C:\portfolio\logs\krx_worker_daily_20260616_045550.log`
6. KRX worker log result: complete (0 full-body plaintext quotations of the file)
   1) KRX login success: `KRX ID/PW Login Success` / `KRX Login Ready` (elapsed 92.83s)
   2) `interest_program` success: Collected Date 2026-06-15 / 2026-06-15 Collected
   3) `interest_shortsell` success: Collected Date 2026-06-15 / 2026-06-15 349 Company Collected
   4) wrapper complete: `DONE :: KRX worker daily`
   5) Confirmed Python process termination
   6) Confirmed Chrome process residual — may remain due to the KRX debug attach structure / follow-up cleanup option separated (R-AUTO-017 new)
7. Confirm KRX DB freshness: complete
   1) `interest_program_raw`: max date 2026-06-15 / row count 547 → 548
   2) `interest_shortsell_raw`: max date 2026-06-15 / row count 190,554 → 190,903
   3) Confirmed 2026-06-15 data new creation
   4) Judged duplicate insert / upsert behavior within the normal range

### 5. Final judgment / follow-up handover

1. Crawler hybrid structure completion judgment: complete
   1) non-GUI = ECS / Fargate Task Definition revision 7 operational path creation + RunTask success + raw freshness recovery
   2) KRX GUI = Windows EC2 worker + Autologon + Administrator interactive session + Scheduled Task + SSM trigger first automation flow empirical demonstration
   3) Preprocessor = ECS / Fargate Task maintained (2026-06-10 §6 / 2026-06-15 §4 alignment)
   4) Crawler data non-collection resolution complete
2. KRX GUI-dependent collection finalized as separated to the Windows EC2 worker path: complete (OD-MS-011 / OD-MS-022 alignment)
3. Role separation of non-GUI ECS / Fargate rev7 and the KRX EC2 worker finalized: complete
4. Reached the Preprocessor ECS re-runnable state: complete (the re-run itself is task 77 follow-up — only up to "re-runnable state" is recorded this date)
5. Expression unification (OD-MS-020 alignment):
   1) "Crawler data non-collection resolution: complete"
   2) "Interest Crawler hybrid structure complete" (non-GUI rev7 operational path creation + RunTask success + raw freshness recovery / KRX GUI worker operable + automation entry point first empirical demonstration)
   3) "Preprocessor: ECS re-runnable state after raw input data recovery"
   4) "Backend AWS E2E dry-run: follow-up resume planned"
   5) "View implementation: follow-up planned"
6. Follow-up handover:
   1) Preprocessor ECS re-run (based on raw freshness recovery input / confirm feature table max date / `updated_at` / whether a new feature date is generated / R-DATA-010 mitigation alignment / task 77)
   2) Resume Backend AWS E2E dry-run (order `BACKTEST_RESEARCH` → `BACKTEST_REPORT` → `DAILY_BUY_SIGNAL` → `DAILY_POSITION_SIGNAL` / Research first / order·execution family skip or dry-run / OD-MS-021 alignment)
   3) Follow-up check of `interest_foreignindex_raw`'s HANGSENG / NIKKEI225 / SHANGHAI NULL Data (non-blocker candidate)
   4) EventBridge Scheduler → SSM RunCommand → `schtasks /Run` periodic trigger linkage (task 54)
   5) Step Functions hybrid orchestration (task 55)
   6) CloudWatch Logs Agent or SSM output-based EC2 worker log collection (task 56)
   7) Automatically add DB validation output within the wrapper (task 57 / R-AUTO-007 alignment)
   8) Specify the stop procedure after EC2 worker work completion (task 59)
   9) Chrome process cleanup option (R-AUTO-017 mitigation / task 90 new)
   10) View implementation (05 spec follow-up separation)

### 6. This date's safety / document record check

1. 0 plaintext records in this note of actual secret value / Administrator password / Autologon DefaultPassword / KRX login password / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / account number / token / account-id / actual secret ARN / actual IAM Role ARN / image digest full sha256 / IAM access key id / instance-id / task ARN / Task Definition ARN. All `[REDACTED]` or placeholder.
2. KRX login ID / password / Administrator credentials / Autologon credentials are noted only as "injected via Secrets Manager or Sysinternals Autologon". Values not recorded.
3. 0 AWS / SSM / EC2 / ECS / ECR / Docker / IAM / Secrets Manager / RDS / GRANT changes due to Kiro's work this date. All actual work performed directly by the operator.
4. No modifications to the 8 MS (`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to Kiro's work this date.
5. The `port-interest-crawler/interest_crawler_daily_nongui.py` new file / Dockerfile change / requirements.txt change (if any) the operator directly wrote / modified are recorded as facts only in this note — 0 full-body quotations.
6. 0 broker / KIS / order / fill / Daily Batch entrypoint calls. 0 new BUY / SELL / cancel / modify / `--execute`. 0 fill / position sync automatic retries (OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 / R-AUTO-010 / R-AUTO-011 alignment).
7. 0 RDS DDL / DML is limited to the normal flow of crawler raw insert / KRX program / shortsell insert / preprocessor pipeline. 0 direct SQL changes.
8. 0 aws-live work — this date is `aws-paper` limited.
9. 0 plaintext quotations of the CloudWatch Logs body / wrapper log (`krx_worker_daily_20260616_045550.log`) / SSM response body / docker build log / full Selenium · Chrome stdout · stderr.
10. Autologon use is a paper-only Windows worker-limited security exception (R-SEC-009) — first empirical demonstration of the policy prohibiting plaintext recording of auto-login credentials in the operator note / spec deliverables.

## 2026-06-17 Daily AWS 17-step E2E complete (Interest Crawler + Preprocessor)

**Summary** — The Daily AWS 17-step E2E flow was connected end-to-end this date.

- This spec's scope steps: step 2 `INTEREST_CRAWLER` / step 3 `PREPROCESSOR`
- Full 17-step progress status: see the 2026-06-17 section of the 03 / 04 / 09 spec operation-notes
- Kiro: document / procedure organization only. Operator: directly performs the actual ECS RunTask / SSM RunCommand / Windows EC2 worker work
- Safety: `aws-paper` limited / 0 aws-live work / hybrid structure first empirical demonstration alignment (OD-MS-011 / OD-MS-022 alignment)

### 1. Step 2 `INTEREST_CRAWLER`

1. non-GUI ECS Fargate crawler RunTask: success
   1) Task Definition: `portfolio-paper-interest-crawler:7` (2026-06-16 §1 alignment / same revision reused this date)
   2) launch type: FARGATE / awsvpc / public subnet + `assignPublicIp = ENABLED` (OD-NET-004 alignment)
   3) command: `interest_crawler_daily_nongui.py`
   4) lastStatus: `STOPPED` / stopCode: `EssentialContainerExited` / exitCode: `0`
   5) All non-GUI steps SUCCESS — `interest_news` / `interest_agency` / `interest_foreignindex` / `interest_commodity` / `interest_macroeconomic` / `interest_price` / `interest_investorflow` / `interest_marketbreadth`
2. KRX Windows EC2 worker Scheduled Task: success
   1) SSM RunCommand → `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"` flow (OD-MS-022 alignment)
   2) Windows Scheduled Task → Administrator console interactive session → `run_krx_worker_daily.ps1` execution
   3) KRX login success / confirmed `interest_program_raw` / `interest_shortsell_raw` 2026-06-16 loading
3. Confirm raw freshness: complete
   1) `interest.interest_program_raw` latest date `2026-06-16` confirmed
   2) `interest.interest_shortsell_raw` latest date `2026-06-16` confirmed
   3) The 6 non-GUI raw types + news / agency also loaded through the prior trading day (R-DATA-009 / R-DATA-010 mitigation first empirical demonstration / 2026-06-16 §3 alignment)
4. crawler worker stop request: complete
   1) Stop request after Windows EC2 worker work completion performed directly by the operator — idle cost reduction (R-SEC-009 mitigation alignment / minimize Autologon exposure time)
   2) The next run is a schtasks trigger after an SSM Online + Administrator session Active pre-check (OD-MS-022 / R-AUTO-016 mitigation alignment)
5. Maintain the KRX GUI collection operational method: confirmed
   1) KRX GUI = maintained on the Windows interactive desktop session basis (OD-MS-022 alignment)
   2) Headless / non-interactive KRX collection is excluded from the operational method (OD-MS-022 alignment)
   3) The non-GUI crawler and the KRX GUI worker are documented as a Hybrid structure (OD-MS-011 / OD-MS-020 alignment)

### 2. Step 3 `PREPROCESSOR`

1. ECS Fargate RunTask: success
   1) Task Definition: `portfolio-paper-interest-preprocessor:1` (2026-06-10 §6 alignment / same revision reused this date)
   2) launch type: FARGATE / awsvpc / public subnet + `assignPublicIp = ENABLED`
   3) command: `pre_daily.py` orchestration
   4) lastStatus: `STOPPED` / stopCode: `EssentialContainerExited` / exitCode: `0`
   5) Confirmed `PREPROCESSOR PIPELINE END` CloudWatch Logs output
2. Confirm feature freshness: complete
   1) `pre_total.pre_total_market_daily_feature` latest date `2026-06-16` confirmed
   2) `pre_total.pre_total_stock_daily_feature` latest date `2026-06-16` confirmed
3. First failure issue and this step alignment:
   1) One of this date's 17-step E2E flow's first blockers was the `execution_app`'s interest schema/table SELECT permission missing (R-DATA-005 [2026-06-17 reinforcement] alignment) — that issue affects **step 8 `DAILY_BUY_EXECUTION`**, and this step `PREPROCESSOR` itself completed normally (the causal relationship between R-DATA-005 and this step is separated)
   2) The `preprocessor_app` permission is aligned as of this date — preprocessor pipeline execution / DB write operate normally
   3) Aligned with the fact records in 02 spec / 06 spec operation-notes 2026-06-17 §1 ~ §3 of `execution_app` interest permission correction / `marketconnector_app` legacy permission correction

### 3. Safety / security check results

1. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to this date's work. The SSM RunCommand / Windows EC2 worker stop / KRX wrapper execution results the operator directly performed are recorded as facts only in this note.
2. 0 plaintext records of the following sensitive information — all `[REDACTED]` or placeholder:
   - secret value / Administrator password / KRX login password / RDS password / RDS endpoint hostname
   - KIS app key / KIS app secret / account number / token
   - account-id / actual secret ARN / actual IAM Role ARN / image digest full sha256 / IAM access key id / instance-id / task ARN

   Only operational identifiers as fact records: Task Definition family `portfolio-paper-interest-crawler` / `portfolio-paper-interest-preprocessor` / Scheduled Task `Portfolio-KRX-Worker-Daily` / loading date 2026-06-16 / feature latest date 2026-06-16.
3. AWS / SSM / EC2 / ECS / ECR / Docker / IAM / Secrets Manager / RDS / GRANT work all performed directly by the operator. Kiro only performs document authoring / procedure organization / validation item organization. 0 plaintext records of `secretsmanager:GetSecretValue` result values. 0 plaintext quotations in this note of the CloudWatch Logs body / wrapper log (`krx_worker_daily_*.log`) / SSM response body / Selenium · Chrome stdout.
4. 0 broker / KIS / order / fill / Daily Batch entrypoint direct calls within this spec's scope. 0 RDS DDL. DML is limited to this date's 17-step normal flow — `interest.interest_*_raw` insert / `pre_total.pre_total_*_feature` insert / `pre_*.*_feature` insert.
5. The crawler worker stop request was performed as the operator's direct work — minimize Autologon exposure time (R-SEC-009 mitigation alignment). On the next run, the flow of Administrator console session Active pre-check after EC2 start is aligned (R-AUTO-016 mitigation alignment).
6. live automatic crawler / preprocessor execution is still prohibited until follow-up validation / approval. This date is `aws-paper` limited / 0 aws-live work. With the Daily AWS 17-step E2E paper first pass, backend AWS E2E steps 2 / 3 are aligned — OD-MS-011 / OD-MS-020 / OD-MS-022 mitigation first empirical demonstration / Status kept at existing values as-is.

## 2026-06-20 AWS automatic Wrapper final confirmation + 6/18 duplicate-run attempt safe abort + 6/19 KRX raw freshness recovery status

**Summary** — Final check of the Daily wrapper structure / safety criteria / EC2 startup criteria + safe abort of the 6/18 duplicate-run attempt.

- (a) Final check of the Daily AWS Paper Wrapper structure / safety criteria / EC2 startup criteria
- (b) Safe abort of the RunDate `2026-06-18` / Step 1 ~ Step 11 range wrapper re-run attempt (confirmed Step 12 not executed)
- (c) Check of the 6/19 KRX raw DB freshness recovery status
- Kiro: document / procedure organization only. Operator: directly performs the actual AWS / SSM / EC2 / RDS work
- Safety: `aws-paper` limited / 0 aws-live work / 0 new broker · KIS order submissions / 0 `--execute` calls

### 1. Daily AWS Paper Wrapper final check (structure / safety criteria / EC2 startup criteria): complete

1. Structure confirmation: complete
   1) main wrapper `C:\Workspaces\port-view\.kiro\scripts\run-daily-aws-paper.ps1`
   2) config `C:\Workspaces\port-view\.kiro\scripts\daily-aws-paper.config.ps1`
   3) functions `C:\Workspaces\port-view\.kiro\scripts\daily-aws-paper.functions.ps1`
   4) 17 step files `C:\Workspaces\port-view\.kiro\scripts\steps\step-01 ~ step-17`
2. Safety criteria confirmation: complete
   1) Step 1 ~ Step 11 are pre-broker / KIS order submission stages
   2) Only Step 12 (`MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) is a step that can actually submit a KIS paper order
   3) Step 12 is blocked when `-AllowPaperOrderExecute` is not specified (OD-MS-023 / R-AUTO-019 mitigation alignment)
   4) Saturday / market-holiday Step 12 actual order submission is excluded (OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-002 alignment)
3. EC2 startup criteria confirmation: complete
   1) MarketConnector EC2 = needed at Step 1 / Step 12 / Step 13 / Step 17
   2) Crawler Worker EC2 = needed when running the Step 2 KRX GUI worker
   3) If both EC2 are in stopped state, start is needed before wrapper execution
   4) The MarketConnector EC2's `/tmp/inject-env.sh` may be lost after EC2 stop / start (this date's first empirical demonstration)
   5) Need to reinforce a lifecycle that starts only the EC2 the wrapper needs and stops them after completion (follow-up separation)

### 2. 6/18 wrapper duplicate-run attempt safe abort: complete

1. Execution situation: confirmed
   1) On 2026-06-20, attempted a wrapper re-run for RunDate `2026-06-18` / Step 1 ~ Step 11 range
   2) Step 1 `CONNECTOR_BALANCE` initial failure — cause = loss of the MarketConnector EC2's `/tmp/inject-env.sh` (EC2 stop / start impact)
   3) After recreating `/tmp/inject-env.sh` on the MarketConnector EC2, Step 1 re-run passed
   4) Step 2 `INTEREST_CRAWLER` non-GUI ECS task exitCode 0 confirmed / Step 2 KRX GUI worker Scheduled Task trigger success confirmed
   5) After recognizing the possibility of duplicating the existing 6/18 execution history, aborted the wrapper with Ctrl+C right after entering Step 3 `PREPROCESSOR`
2. Confirm safety / whether AWS work remains: complete
   1) 0 ECS RUNNING tasks
   2) 0 AWS Batch RUNNING / SUBMITTED / PENDING / RUNNABLE jobs
   3) KRX `Portfolio-KRX-Worker-Daily` Scheduled Task State `Ready` / LastTaskResult 0
   4) Confirmed Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` not executed
3. Confirm DB duplication: complete
   1) 2026-06-18 existing `execution_plan_id 94` normal completion history (BUY 4 / FILLED 4 / connector linked 4)
   2) 0 new execution_plan after 2026-06-20 04:20 UTC
   3) 0 new strategy_execution_order
   4) No new connector_order_request creation
   5) 0 KIS new order submissions
4. Termination handling: complete
   1) The Crawler Worker chrome residual process is cleaned up by EC2 stop (R-AUTO-017 mitigation alignment)
   2) MarketConnector EC2 / Crawler Worker EC2 stop handling

### 3. 6/19 KRX raw freshness recovery status check: complete

1. `interest_program_raw`: confirmed
   1) max_date `2026-06-19`
   2) 2026-06-18 row_count `1`
   3) 2026-06-19 row_count `1`
2. `interest_shortsell_raw`: confirmed
   1) max_date `2026-06-19`
   2) 2026-06-18 row_count `349`
   3) 2026-06-19 row_count `349`
3. Conclusion: confirmed
   1) On the KRX raw basis, freshness is recovery-complete through `2026-06-19`
   2) Windows EC2 worker automation has a Scheduled Task trigger / LASTEXITCODE-centered success-judgment limitation (R-AUTO-007 alignment)
   3) Judged that Step 2 wrapper success judgment needs reinforcement (separated into 2026-06-21 work)

### 4. Outside this date's scope / follow-up handover: organized

1. EC2 lifecycle automatic start / stop reinforcement (handle `/tmp/inject-env.sh` loss after MarketConnector EC2 stop / start / including SSM Online wait): follow-up separation
2. Step 12 actual paper order submission is not executed until separate approval (OD-SAFE-002 / OD-SAFE-003 / R-AUTO-002 alignment)
3. Step 2 wrapper success judgment reinforcement — Chrome reset / Scheduled Task wait + Last Result / latest worker log / KRX raw DB validation: separated into 2026-06-21 (next section of this note)
4. Mixed ECS RunTask + SSM RunCommand orchestration in Step Functions: follow-up maintained
5. View Daily Batch screen linkage / worker log centralized collection: follow-up maintained

### 5. Safety / security check results (2026-06-20)

1. 0 broker / KIS / new order calls. 0 `--execute` actual calls. 0 SELL / cancel / modify calls. 0 fill · position sync automatic retries. 0 aws-live work.
2. 0 RDS DDL. DML is limited to validation SQL SELECT (`interest_program_raw` / `interest_shortsell_raw` / `strategy_execution_plan` / `strategy_execution_order` / `connector_order_request` row_count + max_date check).
3. 0 plaintext records in this note of actual secret value / KIS app key / KIS app secret / account number / account password / token / RDS password / RDS endpoint hostname / account-id / actual IAM Role ARN / actual secret ARN / IAM access key id / EIP / image digest full sha256 / task ARN / job ARN / KIS paper login credential / Administrator password. All `[REDACTED]` or placeholder.
4. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to this date's work (spec area). AWS / SSM / EC2 / RDS calls all performed directly by the operator.
5. Operational identifiers (`execution_plan_id 94` / RunDate `2026-06-18` / `interest_program_raw` · `interest_shortsell_raw` row_count + max_date / Scheduled Task name `Portfolio-KRX-Worker-Daily` / wrapper parameters `-StartStep` · `-EndStep`) are fact records per the user-specified policy alignment — not secrets.

## 2026-06-21 Step 2 `INTEREST_CRAWLER` success judgment reinforcement + KRX raw DB validation linkage

**Summary** — Step 2 success judgment reinforcement + KRX raw DB validation linkage (OD-MS-026 / R-AUTO-020 new mitigation).

Work scope:

- (a) `step-02-interest-crawler.ps1` success judgment reinforcement
- (b) non-GUI ECS crawler env reinforcement (`TEMP=/tmp` / `TMP=/tmp` / `PYTHONUTF8=1` / `PYTHONIOENCODING=utf-8`)
- (c) `interest_krx_raw_validate_daily.py` new creation + Windows crawler worker EC2 deployment via S3 presigned URL + EC2 standalone validation
- (d) `step-02-interest-crawler.ps1` DB validation linkage (`INTEREST_CRAWLER_KRX_DB_VALIDATE` SSM step)
- (e) crawler worker stopped fail-closed handling (skip → fail-closed)
- (f) Step 2 standalone execution validation

Safety:

- Kiro: document / procedure organization only. Operator: directly performs the actual code authoring / py_compile / S3 upload / SSM RunCommand / EC2 standalone validation / wrapper standalone execution
- `aws-paper` limited / 0 aws-live work / 0 new broker · KIS order submissions / 0 `--execute` calls

### 1. `step-02-interest-crawler.ps1` success judgment reinforcement: complete

1. Chrome / chromedriver best-effort reset before KRX worker execution: complete
   1) Added chrome / chromedriver stale process cleanup (R-AUTO-017 mitigation alignment)
   2) A reset failure does not abort immediately but leaves only a warning log
2. Maintain the Administrator interactive Scheduled Task execution path: complete
   1) Run the `Portfolio-KRX-Worker-Daily` Scheduled Task via `schtasks /Run`
   2) Reject adopting SSM direct python execution / OD-MS-022 / OD-MS-015 alignment
   3) Maintain the KRX GUI-dependent structure on the Windows Administrator interactive session basis
3. Scheduled Task termination wait: complete
   1) Poll `Running` status after execution
   2) wait until it returns to `Ready` status
   3) On timeout, handle Step 2 as failure
   4) Log-output whether `Running` status was observed via `sawRunning`
4. Last Result confirmation: complete
   1) Confirm Last Result after Scheduled Task termination
   2) If Last Result is not `0` or `0x0`, Step 2 failure
   3) Prohibit handling Step 2 SUCCESS on Scheduled Task trigger success alone (R-AUTO-020 new mitigation alignment)
5. Latest worker log path output: complete
   1) Confirm the latest file among `C:\portfolio\logs\krx_worker_daily_*.log`
   2) Output latest log path / last write time / size
   3) Output latest worker log tail
6. crawler worker stopped fail-closed handling: complete
   1) Before-change behavior — if the crawler worker EC2 is not `running`, the KRX GUI worker can be skipped / Step 2 SUCCESS is possible even in the skip state
   2) After-change behavior — if the crawler worker EC2 is not `running`, immediate failure handling / output instanceId / state in the failure message / block Step 2 SUCCESS entry in the KRX GUI worker · DB validation not-performed state (R-AUTO-016 mitigation update)

### 2. non-GUI ECS crawler env reinforcement: complete

1. ECS RunTask overrides environment variable reinforcement: complete
   1) `TEMP=/tmp`
   2) `TMP=/tmp`
   3) `PYTHONUTF8=1`
   4) `PYTHONIOENCODING=utf-8`
2. Common function support reinforcement (`daily-aws-paper.functions.ps1`): complete
   1) Added `Invoke-DailyAwsPaperEcsTask` `EnvironmentVariables` parameter
   2) Added ECS RunTask `containerOverrides.environment` passing
   3) Added `New-SsmParameterFile` `ExecutionTimeoutSeconds` parameter
   4) Added `Invoke-SsmCommandAndWait` `ExecutionTimeoutSeconds` passing
3. Purpose: confirmed
   1) Mitigate encoding differences between Windows / Linux execution environments
   2) Clarify the UTF-8 basis in crawler log / file handling
   3) Make the temporary file path dependency explicit
   4) Enable SSM timeout control during long-running KRX worker and DB validation

### 3. `interest_krx_raw_validate_daily.py` new creation + EC2 deployment + EC2 standalone validation: complete

1. New file creation: complete
   1) `C:\Workspaces\port-interest-crawler\interest_krx_raw_validate_daily.py` (operator directly wrote / this spec deliverable has fact records only / R-DOCS-001 alignment)
2. Validation targets: confirmed
   1) `interest_program_raw`
   2) `interest_shortsell_raw`
3. Validation criteria: confirmed
   1) Confirm row_count based on the expected `trade_date`
   2) Confirm whether `max(trade_date)` is at least the expected date
   3) On failure, return exit code 30
4. Local validation: complete
   1) py_compile passed
   2) UTF-8 read confirmed
   3) program / shortsell / exit30 marker confirmed
5. EC2 deployment (via S3 presigned URL): complete
   1) S3 upload — `s3://portfolio-paper-migration-yukiever/tmp/krx/interest_krx_raw_validate_daily.py`
   2) presigned URL creation (URL actual value plaintext recording in this note prohibited / R-DOCS-001 alignment)
   3) Windows crawler worker EC2 download via SSM RunPowerShellScript — deployment target `C:\portfolio\port-interest-crawler\interest_krx_raw_validate_daily.py`
   4) Remote validation — remote py_compile passed / `interest_program_raw` marker confirmed / `interest_shortsell_raw` marker confirmed / `--expected-date` · `--run-date` args confirmed / exit code 30 failure return logic confirmed / `sys.exit(run(parsed_args))` confirmed
   5) SSM commandId `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3`
6. EC2 standalone validation: complete
   1) Executed inside the Windows crawler worker EC2
   2) Confirmed AWS RDS connection env loaded via `C:\portfolio\load-crawler-db-env.ps1`
   3) Confirmed `C:\portfolio\venvs\interest-crawler` venv activation
   4) Confirmed Python `3.13.5` execution
   5) DB session — user=`crawler_app` / schema=`interest` / search_path=`interest, reference, legacy, public`
   6) Validation result — `interest_program_raw` expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`1` / `interest_shortsell_raw` expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`349`
   7) validation exit code 0 confirmed
   8) SSM commandId `c844aea5-1429-430a-9510-39fc99f17f05`

### 4. `step-02-interest-crawler.ps1` DB validation linkage: complete

1. Added `ExpectedKrxRawDate` calculation: complete
   1) Calculate the prior business day based on RunDate
   2) `ExpectedKrxRawDate` log output
2. Added the DB validation call inside the Windows crawler worker EC2: complete
   1) Load AWS RDS connection env via `C:\portfolio\load-crawler-db-env.ps1`
   2) Activate `C:\portfolio\venvs\interest-crawler` venv
   3) Run `C:\portfolio\port-interest-crawler\interest_krx_raw_validate_daily.py` / pass `--expected-date <ExpectedKrxRawDate>`
3. Failure judgment reinforcement: complete
   1) If `interest_program_raw` expected date row_count is 0, failure
   2) If `interest_shortsell_raw` expected date row_count is 0, failure
   3) If validation exit code is non-zero, Step 2 failure
4. Added validation command tracking: complete
   1) `INTEREST_CRAWLER_KRX_DB_VALIDATE` SSM command id output
   2) stdout / stderr log path output
   3) Include `KrxDbValidationCommandId` in the step result

### 5. Step 2 standalone execution validation: complete

1. Execution conditions: confirmed
   1) Environment `aws-paper`
   2) RunDate `2026-06-20`
   3) StartStep `2` / EndStep `2`
   4) PaperOrder `False`
   5) RunId `daily-aws-paper-20260621-204017`
2. wrapper execution result: complete
   1) StepCode `INTEREST_CRAWLER`
   2) Status `SUCCESS`
   3) Runner `ECS+SSM`
   4) ExpectedKrxRawDate `2026-06-19`
3. non-GUI ECS crawler validation: complete
   1) taskDefinition `portfolio-paper-interest-crawler:7`
   2) taskId `78979b5cbb714d0eb94f5946e15a14ce`
   3) exitCode 0
   4) stoppedReason `Essential container in task exited`
   5) CloudWatch log saved — `C:\Temp\portfolio-daily-aws-paper\daily-aws-paper-20260621-204017\logs\ecs-interest-crawler-nongui-78979b5cbb714d0eb94f5946e15a14ce-cloudwatch.txt` (this note records only the file path / 0 body quotations)
4. KRX GUI worker validation: complete
   1) crawler worker state `running`
   2) SSM commandId `f9d82fcc-1e26-4710-87c3-1d20483b63ef`
   3) SSM result `Success` / ResponseCode 0
   4) Scheduled Task execution flow — `TaskStatus=Running` confirmed → `TaskStatus=Ready` return / elapsedSeconds=`111` / sawRunning=True
   5) Scheduled Task final status — FinalStatus=`Ready` / FinalLastResult=`0` / Last Result=`0`
   6) Scheduled Task execution path — Logon Mode `Interactive only` / Run As `Administrator` / Task To Run `powershell.exe -ExecutionPolicy Bypass -File C:\portfolio\run_krx_worker_daily.ps1`
   7) latest worker log `C:\portfolio\logs\krx_worker_daily_20260621_114154.log` — KRX login SUCCESS / KRX program SUCCESS / KRX shortsell SUCCESS / `DONE :: KRX worker daily` (this note records only the SUCCESS labels / 0 body plaintext quotations)
5. KRX raw DB validation: complete
   1) SSM step code `INTEREST_CRAWLER_KRX_DB_VALIDATE`
   2) SSM commandId `2279c6d7-2da6-4317-9c10-7cc77374b317`
   3) SSM result `Success` / ResponseCode 0
   4) DB session — user=`crawler_app` / schema=`interest` / search_path=`interest, reference, legacy, public`
   5) Validation result — `interest_program_raw` expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`1` / OK / `interest_shortsell_raw` expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`349` / OK
   6) validation exit code 0
   7) stderr empty
6. Final judgment: confirmed
   1) Step 2 standalone execution validation success
   2) Confirmed not only Scheduled Task trigger success but also worker termination / Last Result / DB validation success
   3) Blocks the risk of proceeding past Step 3 in the KRX raw not-loaded state (R-AUTO-020 mitigation first empirical demonstration)
   4) A state where the stages from Step 3 PREPROCESSOR onward can proceed, but not proceeded within this date's work scope

### 6. Decision / risk change summary: complete

1. New decision: complete
   1) OD-MS-026 (Step 2 INTEREST_CRAWLER operational success criterion = up to KRX raw DB validation, not the Scheduled Task trigger / the KRX GUI path is the Windows Administrator interactive Scheduled Task / the wrapper is responsible for execution · termination wait · Last Result · latest log · DB validation orchestration / `interest_krx_raw_validate_daily.py` is responsible for raw freshness validation / crawler worker stopped is fail-closed, 🟡 provisional)
2. New risk: complete
   1) R-AUTO-020 (the risk that KRX raw non-loading propagates past Step 3 when handling Step 2 SUCCESS on Scheduled Task trigger success alone, mitigation = Chrome / chromedriver best-effort reset + Running → Ready wait + Last Result confirmation + latest worker log output + KRX raw DB validation + worker stopped fail-closed, Status `Mitigated`)
3. Risks with no body change: detection / mitigation memo reinforcement
   1) R-AUTO-007 (wrapper success termination does not guarantee actual DB loading success) — the automatic DB validation output within the wrapper is promoted to task-unit completion at the task 57 follow-up as this date's first empirical demonstration (`KrxDbValidationCommandId` step result tracking / automatic confirmation of `interest_program_raw` · `interest_shortsell_raw` expected date row_count)
   2) R-AUTO-016 (KRX GUI collection failure in the absence of an Administrator interactive session) — the previous "automatic skip within the wrapper" expression is updated this date to fail-closed / immediate failure handling when the crawler worker EC2 is stopped / output instanceId / state / block Step 2 SUCCESS entry
   3) R-AUTO-017 (Chrome process residual) — Chrome / chromedriver best-effort reset first empirical demonstration / failure proceeds as warning / the follow-up EC2 stop / restart procedure is combined with the R-AUTO-016 reinforcement

### 7. Safety / security check results (2026-06-21)

1. 0 broker / KIS calls. 0 `--execute` actual calls. 0 SELL / cancel / modify calls. 0 SELL position `mark_position_sell_ordered()` calls. 0 fill · position sync automatic retries. 0 aws-live work.
2. 0 RDS DDL. DML is limited to validation SQL SELECT (`interest_program_raw` / `interest_shortsell_raw` row_count + max_date / DB session user · schema · search_path check). In this date's new Step 2 wrapper execution, idempotent / no-op.
3. 0 plaintext records in this note of actual secret value / KIS app key / KIS app secret / account number / account password / token / RDS password / RDS endpoint hostname / account-id / actual IAM Role ARN / actual secret ARN / IAM access key id / EIP / image digest full sha256 / task ARN / job ARN / KIS paper login credential / Administrator password / S3 presigned URL actual value. All `[REDACTED]` or placeholder.
4. AWS / SSM / EC2 / S3 / ECS / RDS / KRX calls all performed directly by the operator. Kiro only performs document authoring / procedure organization / validation item organization. 0 AWS CLI / boto3 executions. 0 AWS resource creation / modification / deletion. 0 plaintext quotations of the CloudWatch Logs body / SSM response body / full operator PowerShell stdout.
5. The `port-interest-crawler/interest_krx_raw_validate_daily.py` / `step-02-interest-crawler.ps1` / `daily-aws-paper.functions.ps1` changes the operator directly wrote / patched are recorded as facts only in this note (0 full-body quotations / R-DOCS-001 alignment). 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / packaging due to this date's work (spec area).
6. Only operational identifiers are fact records per the user-specified policy alignment — not secrets:
   - 4 SSM commandIds: `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3` / `c844aea5-1429-430a-9510-39fc99f17f05` / `2279c6d7-2da6-4317-9c10-7cc77374b317` / `f9d82fcc-1e26-4710-87c3-1d20483b63ef`
   - RunId: `daily-aws-paper-20260621-204017`
   - Task Definition / taskId: `portfolio-paper-interest-crawler:7` / `78979b5cbb714d0eb94f5946e15a14ce`
   - S3 key: `tmp/krx/interest_krx_raw_validate_daily.py`
   - Scheduled Task: `Portfolio-KRX-Worker-Daily`
   - latest worker log: `krx_worker_daily_20260621_114154.log`
   - wrapper options: `-StartStep` · `-EndStep`
   - DB session: user `crawler_app` / schema `interest` / search_path `interest, reference, legacy, public`
   - DB validation result: row_count 1 · 349 / max_date 2026-06-19

### 8. Outside this date's scope / follow-up handover: organized

1. EC2 lifecycle automatic start / SSM Online wait / stop procedure: follow-up maintained (2026-06-20 §4 alignment)
2. Mixed ECS RunTask + SSM RunCommand orchestration in Step Functions: follow-up maintained
3. Review whether to display `KrxDbValidationCommandId` / latest worker log / Step 2 validation result on the View Daily Batch screen: follow-up maintained
4. worker log centralized collection (CloudWatch Logs Agent or SSM output-based): follow-up maintained
5. Step 12 actual paper order submission is not executed until separate approval (OD-SAFE-002 / OD-SAFE-003 / R-AUTO-002 alignment)
6. This date's work scope is closed with Step 2 reinforcement complete — the stages from Step 3 PREPROCESSOR onward are not forced into this date's next work

## 2026-06-22 Daily AWS Paper Step 2 / Step 3 re-validation (OD-MS-026 first empirical demonstration Daily run regression)

**Summary** — In the 2nd Daily wrapper actual operational run, re-validated 0 Step 2 / Step 3 regressions.

- Environment: `aws-paper` / RunDate `2026-06-22` / region `ap-northeast-2`
- Re-validation pass: 2026-06-21 Step 2 success judgment reinforcement (OD-MS-026 / R-AUTO-020) + KRX raw DB validation linkage
- Kiro: document / procedure organization only. Operator: directly performs the actual ECS RunTask / SSM RunCommand / Windows EC2 worker / RDS work
- Safety: `aws-paper` limited / 0 aws-live work / 0 broker · KIS / order / fill / Daily Batch entrypoint direct calls

### 1. Step 2 `INTEREST_CRAWLER` re-validation (OD-MS-026 alignment)

1. wrapper Step 2 result: complete
   1) `StepCode INTEREST_CRAWLER` / `Status SUCCESS` / `Runner ECS+SSM`
   2) All 6 success conditions (non-GUI ECS exitCode 0 / Crawler Worker EC2 running / Scheduled Task Running → Ready / Last Result 0 or 0x0 / latest worker log / KRX raw DB validation pass) passed (OD-MS-026 alignment)
2. non-GUI ECS crawler validation: complete
   1) taskDefinition `portfolio-paper-interest-crawler:7`
   2) exitCode 0 / stoppedReason `Essential container in task exited`
   3) CloudWatch log saved — 0 file body plaintext quotations in this note
3. Windows KRX worker SSM command validation: complete
   1) crawler worker EC2 state `running` (R-AUTO-016 mitigation update fail-closed branch not entered)
   2) SSM RunCommand Success / ResponseCode 0
   3) Scheduled Task `Portfolio-KRX-Worker-Daily` Running → Ready return / `sawRunning=True` / FinalLastResult 0
   4) latest worker log `C:\portfolio\logs\krx_worker_daily_*.log` (file path / last write time / size / tail output — KRX login / program / shortsell SUCCESS / `DONE :: KRX worker daily` labels confirmed / 0 body plaintext quotations)
4. KRX raw DB validation: complete
   1) SSM step `INTEREST_CRAWLER_KRX_DB_VALIDATE` / Success / ResponseCode 0
   2) DB session user `crawler_app` / schema `interest` / search_path `interest, reference, legacy, public`
   3) Based on `ExpectedKrxRawDate 2026-06-19` — both `interest_program_raw` and `interest_shortsell_raw` row_count > 0 / max_date ≥ expected / validation exit code 0
   4) stderr empty / `KrxDbValidationCommandId` included in the step result (R-AUTO-020 mitigation alignment)

### 2. Step 3 `PREPROCESSOR` re-validation

1. wrapper Step 3 result: complete
   1) ECS RunTask `portfolio-paper-interest-preprocessor:1` exitCode 0
   2) `PREPROCESSOR PIPELINE END` label confirmed — 0 body plaintext quotations in this note
2. raw → feature flow alignment: confirmed
   1) Since entry was after Step 2 KRX raw + non-GUI raw freshness recovery, the stale raw data risk did not occur (R-DATA-009 / R-DATA-010 mitigation alignment)
   2) New feature rows such as `pre_total_market_daily_feature` / `pre_total_stock_daily_feature` were generated normally — secured a state where Step 4 BACKTEST_RESEARCH and Step 6 DAILY_BUY_SIGNAL can be entered

### 3. Decision / risk change summary

1. New decisions: 0. New risks: 0.
2. Decisions with no body change: first empirical demonstration memo reinforcement
   1) OD-MS-026 — the 6 success conditions of the Step 2 success judgment reinforcement first empirically demonstrated with 0 regressions in an actual Daily run pass
   2) OD-MS-011 / OD-MS-022 / OD-MS-023 — hybrid execution model / KRX GUI auto-login / wrapper operational policy 0 regressions
3. Risks with no body change: reinforcement memo
   1) R-AUTO-020 — the limitation of handling Step 2 SUCCESS on Scheduled Task trigger success alone first empirically demonstrated with 0 regressions after introducing the KRX raw DB validation guard
   2) R-AUTO-016 — crawler worker EC2 `running` state / fail-closed branch not entered
   3) R-AUTO-017 — Chrome / chromedriver best-effort reset 0 regressions / the reset label in the wrapper Step 2 stdout normal

### 4. Safety / security check results (2026-06-22)

1. 0 broker / KIS calls. 0 `--execute` calls. 0 new BUY · SELL · cancel · modify calls. 0 fill · position sync automatic retries. 0 aws-live work.
2. 0 RDS DDL. DML is limited to the raw → feature upsert of the KRX worker / non-GUI crawler / Preprocessor.
3. 0 plaintext records in this note of actual secret value / KRX login password / RDS password / RDS endpoint hostname / account-id / actual ARN / IAM access key id / EIP / image digest full sha256 / task ARN / Administrator password. All `[REDACTED]` or placeholder.
4. AWS / SSM / EC2 / ECS / RDS / KRX work all performed directly by the operator. Kiro only performs document authoring / procedure organization. 0 plaintext quotations of the CloudWatch Logs body / SSM response body / full wrapper PowerShell stdout.
5. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to this date's work (spec area). Only operational identifiers (taskDefinition revision / RunDate / ExpectedKrxRawDate / Scheduled Task name / latest worker log file name / DB session user · search_path) are fact records per the user-specified policy alignment — not secrets.
