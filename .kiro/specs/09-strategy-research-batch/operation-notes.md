# Operation Notes — 09-strategy-research-batch

This document is an operation note that cumulatively records, by date, the work results actually performed by the operator / Kiro during 09-strategy-research-batch.

- Application environment: `aws-paper` / `ap-northeast-2`
- First validation target: first preparation of the Docker image / ECR push for the Strategy Research MS (`port_strategy_research`) AWS Batch (as of 2026-06-13)
- Strategy Research final compute first priority: AWS Batch (OD-MS-008 alignment) / ECS Fargate Task is second priority / used only as an alternative means for this date's image smoke

## Timeline Dashboard

Summary of this spec's operator execution history. See each dated section for details.

| Date | Key result | Main decision / mitigation |
|---|---|---|
| 2026-06-13 | Research Docker image first preparation (Dockerfile / requirements.txt new · py_compile · import smoke · ECR push `portfolio-strategy-research:paper-20260613`) | OD-MS-018 (dependency boundary) · 3 new Research adapters |
| 2026-06-15 (skeleton) | AWS Batch CE / Queue / JD rev1 new · Log Group / Secret / IAM Role formal creation · py_compile smoke + DB smoke SubmitJob SUCCEEDED | Strategy Common first consistency confirmation |
| 2026-06-15 (full) | BACKTEST_RESEARCH full + internal extended analysis SUCCEEDED · BACKTEST_REPORT 4-report generation SUCCEEDED | OD-MS-019 new (Research Batch porting targets = RESEARCH + REPORT 2 types / `run_extended_analysis.py` heavy classification) · R-AUTO-015 mitigation |
| 2026-06-15 (S3) | BACKTEST_REPORT S3 upload reinforcement (4 files confirmed · prefix `strategy-research/reports/`) · JD rev3 registration · boto3 dependency addition | OD-MS-019 S3 prefix first validation |
| 2026-06-16 | BACKTEST_RESEARCH re-run + BACKTEST_REPORT formal JD `portfolio-paper-strategy-report` rev1~rev3 correction · rev3 SUCCEEDED · S3 4 files confirmed | on top of the 08 raw freshness recovery, restarted the Research → Decision safe subset dry-run |
| 2026-06-17 | Daily AWS 17-step E2E Step 4 `BACKTEST_RESEARCH` + Step 5 `BACKTEST_REPORT` passed | heavy classification SubmitJob 0 retained (OD-MS-019 / R-AUTO-015) |
| 2026-06-22 | Daily wrapper second live operation run · Step 4 / Step 5 0 regressions | heavy classification SubmitJob 0 retained |

## Open Risks & Next Checks

Latest status summary below the Timeline Dashboard. See each dated section for detailed basis.

### Currently remaining risks (Open)

| ID | Summary | Status | Detection / Mitigation summary |
|---|---|---|---|
| R-AUTO-015 | risk of a heavy-classification (`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) SubmitJob accidentally leaking in | 🟢 Mitigated (2026-06-17 / 2026-06-22 0 regressions) | `smoke` / `full` job name prefix + operator approval + JD timeout 600s / vCPU 1 / memory 2048 cap + `attempts=1` no automatic retry |
| R-AUTO-001 | double execution due to AWS Batch automatic retry | 🟢 Mitigated | Job Definition `attempts = 1` retained |
| R-DATA-008 | possibility that the 3 Research adapters contain their own judgment logic | 🟢 Mitigated | the adapters perform only `port_strategy_common` calls / no own judgment included |
| R-COST-003 | S3 report cumulative cost (lifecycle not set) | 🟠 Open | S3 lifecycle decision follow-up (06 spec) / KMS encryption decision follow-up |
| OD-MS-018 provisional status | formal move of the 3 Research adapters to `port_strategy_common` | 🟠 Open (🟡 provisional decision) | a Decision / Research result comparison test is needed at the follow-up move |
| `port_strategy_common` formal packaging | vendoring retained | 🟠 Open | review wheel / CodeArtifact at the 07 spec / Strategy Common stage |
| BACKTEST_REPORT rev1 / rev2 cleanup | Inactive handling or deregister follow-up | 🟠 Open | use only the final operational rev3 / rev1 (local-only) · rev2 (missing path) are reference history |

### Next Checks

- [ ] View Daily Batch's `BACKTEST_RESEARCH` / `BACKTEST_REPORT` step ProcessBuilder → AWS Batch SubmitJob mapping (05 / 04 spec follow-up phase)
- [ ] Step Functions state machine (enforce BACKTEST_RESEARCH → BACKTEST_REPORT order + `attempts=1`) + EventBridge Scheduler periodic trigger (04 spec follow-up phase)
- [ ] S3 lifecycle policy decision (R-COST-003) / KMS encryption decision
- [ ] formal adapter move of the 3 Research adapters to `port_strategy_common` (R-DATA-008 / OD-MS-018 formalization)
- [ ] `port_strategy_common` formal packaging (wheel / CodeArtifact / git submodule) — 07 spec / Strategy Common stage
- [ ] codify the operational procedure for the heavy-classification entrypoint manual assist tool (R-AUTO-015)
- [ ] BACKTEST_REPORT rev1 / rev2 Inactive handling or deregister

### Key Fact Preservation (must retain when editing)

Do not delete / abridge the following evidence when editing.

- **smoke run criteria**: `smoke` job name prefix + `python -m py_compile` / import smoke / DB smoke / timeout 600s / vCPU 1 / memory 2048 cap. 0 RDS DDL / DML.
- **full run criteria**: `full` job name prefix + operator direct approval + same timeout / vCPU / memory cap. BACKTEST_RESEARCH performs internal extended analysis together.
- **attempt = 1 (no automatic retry)**: Job Definition `attempts = 1` maintained. The 0 AWS Batch automatic retry policy is OD-SAFE-004 / R-AUTO-001 alignment.
- **cost guard (heavy classification blocking)**: `run_extended_analysis.py` / `block_watch_*` / `block_exception_buy_*` are not View Daily Batch execution targets. Excluded from AWS Batch porting targets. Classified as manual auxiliary tools only when needed.
- **S3 prefix contract**: `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/` (OD-MS-019 alignment). The Job Role `s3:PutObject` Resource is limited to `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*`. 0 public read / 0 wildcards.
- **key evidence figures**: 2026-06-15 full run `run_id a39b0b0c-cfe9-474e-8a4a-4ddb33f09567` / total_return 4.55930879 / mdd -0.08941942 / sharpe 2.65561307 / trade_count 308. 2026-06-16 re-run `run_id 439d78e7-41fd-4bb7-b455-18564ddff758` / total_return 4.66534417 / sharpe 2.68071466 / trade_count 310. The figures are maintained as an audit trail.
- **BACKTEST_REPORT formal Job Definition**: `portfolio-paper-strategy-report:3` (rev3 module call method). rev1 local-only / rev2 path absence are preserved only as correction history.
- **BACKTEST_RESEARCH formal Job Definition**: `portfolio-paper-strategy-research:5` (reused since 2026-06-16). The previous revision 1 is smoke / initial full run history.
- **Research image boundary**: includes `port_strategy_research` + `port_strategy_common`, excludes `port_strategy_decision` (OD-MS-018). Docker build context `C:\Workspaces` maintained.

## Record format

- Accumulate under per-date `## YYYY-MM-DD <summary>` headers (same as the 02 / 03 / 04 / 06 / 08 spec operation-notes).
- Record results briefly as only 성공 / 실패 / 보류 / 해당 없음 / carried-over. For failure cases, summarize only 1 line of cause + 1 line of action + result.
- Do not quote the full text of AWS CLI / Console / docker build / ECR push / CloudWatch logs / Task event message / heavy backtest output in this document (security / length reduction).
- Record IAM Role / Policy / secret changes in only 4 lines: change date / changer / change reason / before-and-after item summary (full JSON body quotation prohibited).
- Full-body quotation of the 8 MS source / Dockerfile / requirements.txt / adapter is prohibited. Record only the fact that the operator newly wrote / modified them.

## Safety principles

- Do not record actual secret value, password, RDS endpoint hostname, RDS password, token, IAM access key id, account-id, actual secret ARN, actual KMS Key ARN, instance-id, image digest, or Batch job ARN in plaintext in this document. Use `[REDACTED]` or a placeholder (`<account-id>` / `<region>` / `<rds-endpoint>` / `<image-tag>` / `<image-digest>` / `<job-arn>`) for all of them.
- Do not record secret lookup results (value). Only 성공 / 실패 + last update time.
- Actual AWS resource creation / modification / deletion is performed directly by the operator. Kiro only performs document authoring / procedure organization / validation item organization.
- The README / AGENTS.md / CHANGELOG / docs / worklog of the 8 MS (`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) are not changed by this spec work. Dockerfile / requirements.txt / adapter files the operator directly wrote / modified are the operator's direct work, and only the facts are recorded in this note.
- Actual `secretsmanager:GetSecretValue` calls are by the operator only. Kiro automatic validation is `secretsmanager:DescribeSecret` metadata only.
- Maintain the same principle in follow-up work so that a secret value is not exposed in plaintext in the work chat / command output / console capture / docker build log / ECR push log / CloudWatch Logs body / operator note (R-DOCS-001 alignment).
- Full backtest / long-running research / report generation / RDS DDL/DML / broker / KIS / order / fill / Daily Batch entrypoint calls are outside this date's validation scope.

## 2026-06-13 Strategy Research Batch image first preparation

**Summary** — First preparation of Docker / ECR for AWS Batch of `port_strategy_research`.

Work scope:

- As-Is analysis / heavy·light classification / new Research adapter creation / new Dockerfile · requirements.txt creation / local docker build / py_compile · import smoke / ECR push

Outside this date's scope (follow-up separation):

- AWS Batch Compute Environment / Job Queue / Job Definition / SubmitJob
- CloudWatch Log Group (for Research) / Secrets Manager (`/portfolio/paper/rds/research-app`) / IAM Role (execution / job)

Role split: Kiro only organizes documents / procedures / validation items. The operator directly performs the actual As-Is analysis / adapter / Dockerfile / build / ECR push.

### 1. Confirm AWS execution structure

1. Confirm As-Is entrypoint: complete
   1) Confirm local repository path: complete (`C:\Workspaces\port_strategy_research`)
   2) Confirm main entrypoint candidates: complete
       - `backtest_research_run.py`
       - `backtest_report_run.py`
       - `run_extended_analysis.py`
       - `block_watch_analysis_run.py`
       - `block_watch_backtest_run.py`
       - `block_exception_buy_backtest_run.py`
       - `block_exception_buy_engine_run.py`
   3) Confirm Research steps in the existing Daily Batch: complete
       - `BACKTEST_RESEARCH` (about 45 seconds per port-view DailyBatchService)
       - `BACKTEST_REPORT` (about 2 seconds per port-view DailyBatchService)
2. Separate heavy / light commands: complete
   1) heavy candidates (excluded from this date's execution):
       - full backtest
       - long-running research
       - report generation
       - extended analysis
       - block / watch backtest family
       - block exception engine backtest family
   2) light / smoke candidates (used this date):
       - `python -m py_compile`
       - import smoke
       - Confirm MS boundary inside the Docker image (confirm absence of `/app/port_strategy_decision`)
3. Confirm requirements / dependency: complete
   1) External dependency:
       - `psycopg2-binary`
       - `pandas`
       - `numpy`
   2) Internal dependency:
       - `port_strategy_research` (this MS)
       - `port_strategy_common`
       - `port_strategy_decision` is excluded from the final image (aligned with §2 below)
4. Confirm `research_app` DB environment variables: complete (per `db_config.py`)
   1) `INTEREST_DB_PASSWORD` required (required at import time)
   2) `INTEREST_DB_HOST`
   3) `INTEREST_DB_PORT`
   4) `INTEREST_DB_NAME`
   5) `INTEREST_DB_USER`
   6) search_path: `research, preprocessor, interest, reference, legacy, public` (OD-DB-006 / 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) §3 alignment)
       - Note: `legacy` is included in search_path but actual access is blocked because USAGE is not granted (OD-DB-007 alignment)

### 2. Remove direct Research → Decision runtime dependency

1. Decision background: complete (OD-MS-018 alignment)
   1) `port_strategy_research` previously had a structure that directly imported 3 backtest adapters of `port_strategy_decision`
       - `port_strategy_decision.backtest_market`
       - `port_strategy_decision.backtest_filter`
       - `port_strategy_decision.backtest_sizing`
   2) Those 3 files are thin adapters that call `port_strategy_common` logic rather than actual core logic
   3) Vendoring the entire Decision MS into the Research Batch image would blur the MS boundary, so it was decided to migrate them to Research-internal adapters
2. Create 3 new Research adapters: complete (operator directly wrote)
   1) `research_backtest_market_adapter.py`
   2) `research_backtest_filter_adapter.py`
   3) `research_backtest_sizing_adapter.py`
   4) All adapters only call `port_strategy_common` — no own decision logic included (R-DATA-008 mitigation alignment)
   5) 0 full-body quotations of the adapters — only the fact that the operator directly wrote them is recorded
3. import-changed files: complete (operator directly wrote)
   1) `backtest_engine.py`
   2) `backtest_buy_logic.py`
   3) `block_exception_buy_engine_run.py`
4. Before / after import mapping: complete
   1) Before:
       - `from port_strategy_decision.backtest_filter import filter_buy_candidates`
       - `from port_strategy_decision.backtest_market import evaluate_market`
       - `from port_strategy_decision.backtest_sizing import allocate_positions`
   2) After:
       - `from port_strategy_research.research_backtest_filter_adapter import filter_buy_candidates`
       - `from port_strategy_research.research_backtest_market_adapter import evaluate_market`
       - `from port_strategy_research.research_backtest_sizing_adapter import allocate_positions`
5. Change method / validation: complete
   1) Modified via a separate Python patch script method (aligned with the 08 spec Korean encoding reinforcement decision — no direct replace via PowerShell `Get-Content` / `Set-Content`)
   2) Used `encoding="utf-8-sig"` for source reading / `encoding="utf-8"` for saving
   3) Korean preview output normal
   4) `python -m py_compile` passed
   5) 0 residual `from port_strategy_decision` / `import port_strategy_decision` (`grep` check result)
   6) Confirmed absence of `/app/port_strategy_decision` in the Docker image (§3-3 alignment)
6. Follow-up separation: complete
   1) This date's decision is provisional (OD-MS-018 🟡) — in the long term, the candidate of moving the adapters to the `port_strategy_common` formal package is maintained (R-DATA-008 mitigation alignment)

### 3. Docker / ECR organization for Batch

1. New Dockerfile / requirements.txt creation: complete (operator directly wrote)
   1) `port_strategy_research/requirements.txt` new
   2) `port_strategy_research/Dockerfile` new
   3) Docker build context: `C:\Workspaces` (aligned with the Strategy Decision Dockerfile pattern — 04 spec 2026-06-13 §1 / OD-MS-014 vendoring pattern alignment)
   4) image inclusion targets:
       - `port_strategy_research`
       - `port_strategy_common`
   5) image exclusion targets:
       - `port_strategy_decision` (OD-MS-018 alignment — per this date's decision, not included in the image)
   6) The default CMD is set to a safe `py_compile` basis — the actual operational entrypoint is decided in a follow-up phase's AWS Batch Job Definition `command` or SubmitJob `--container-overrides`
   7) 0 full-body quotations of Dockerfile / requirements.txt — only the fact that the operator newly wrote them is recorded (R-DOCS-001 alignment)
2. local docker build: complete
   1) image tag: `portfolio-strategy-research:paper-20260613`
   2) build success
   3) dependency install success (`psycopg2-binary` / `pandas` / `numpy`)
3. py_compile / import smoke: complete
   1) default CMD `py_compile` execution success
   2) container import smoke passed:
       - `port_strategy_research.db_config`
       - `port_strategy_research.backtest_engine`
       - `port_strategy_research.backtest_buy_logic`
       - `port_strategy_research.block_exception_buy_engine_run`
   3) Docker image MS boundary check success:
       - `/app/port_strategy_decision` existence: False
       - i.e. first validation of the OD-MS-018 image exclusion target policy passed
   4) This date's import smoke passed via dummy env or import-time environment variable bypass — in actual AWS Batch execution it is resolved with Secrets Manager-based environment injection (follow-up separation)
4. ECR push: complete
   1) ECR repository `portfolio-strategy-research` newly created
       - Environment separation policy: repositories are not separated per paper / live environment (08 / 04 spec alignment)
       - paper / live distinction is handled in 6 items: image tag / Job Definition / Secrets path / IAM Job Role / environment variables / RDS settings
   2) tag `paper-20260613` push complete
   3) tag `paper-latest` push complete
   4) ECR image check: image size about 90MB
   5) 0 plaintext records of image digest / actual ECR URI / account-id / repository ARN in this note / spec deliverables (`<image-digest>` placeholder)

### 4. AWS Batch execution structure (follow-up)

Outside this date's work scope — all follow-up separation.

1. Confirm Compute Environment candidate: follow-up
   1) Fargate compute environment vs EC2 managed compute environment comparison follow-up
   2) NAT-free public subnet + assignPublicIp policy, or private subnet policy via VPC Endpoint, review follow-up (R-NET-002 / R-NET-003 alignment)
2. Confirm Job Queue candidate: follow-up
3. Job Definition draft or first creation: follow-up
   1) family / revision / cpu / memory / timeout / job role / execution role / log configuration follow-up
4. Confirm or create CloudWatch Log Group: follow-up
   1) Candidate name: `/portfolio/paper/strategy-research`
   2) retention 14-day policy alignment follow-up (OD-OBS-002)
5. Confirm or create Secrets Manager `/portfolio/paper/rds/research-app`: follow-up
   1) JSON multi-key method (`host` / `port` / `dbname` / `username` / `password`) — 08 / 04 spec alignment follow-up
6. Confirm IAM Role / execution role / job role: follow-up
   1) Add research-app secret read inline policy to the Execution Role follow-up (secret ARN limited / 0 wildcards / OD-SEC-006 alignment)
   2) New Job Role creation follow-up (`portfolio-paper-research-job-role` candidate)
7. Batch Job timeout / vCPU / memory criteria: follow-up
   1) heavy backtest cost / time / timeout criteria decided separately follow-up (R-AUTO-015 mitigation alignment)

### 5. Actual Batch SubmitJob (follow-up)

1. If a short no-op / import smoke command exists, review in follow-up: follow-up
   1) job name prefix `smoke` / `full` distinction policy follow-up (R-AUTO-015 mitigation alignment)
2. full backtest / long-running research execution: outside this date's scope — executed after separate cost / time / timeout / operator approval criteria are finalized
3. AWS Batch-based SubmitJob validation: follow-up (separated as the next work)
4. research schema row creation validation: follow-up

### 6. First validation completion criteria

1. Preparation before creating the Strategy Research AWS Batch body complete
2. As-Is entrypoint / heavy-light classification complete
3. Direct Research → Decision runtime dependency removal complete (OD-MS-018 alignment)
4. Research Batch image organized on the basis of `port_strategy_research` + `port_strategy_common` complete
5. local Docker build / py_compile / import smoke complete
6. ECR repository `portfolio-strategy-research` creation and push complete (`paper-20260613` / `paper-latest`)
7. AWS Batch Compute Environment / Job Queue / Job Definition / SubmitJob is follow-up separation
8. 0 full backtest / long-running research executions
9. 0 RDS DDL / DML
10. 0 broker / KIS calls
11. Judgment: Strategy Research AWS Batch image first preparation complete. Batch CE / Queue / JD / SubmitJob can proceed in a follow-up phase.

### 7. Outside this date's scope / follow-up handover

1. AWS Batch Compute Environment / Job Queue / Job Definition / SubmitJob first creation: follow-up
2. Formal creation of CloudWatch Log Group / Secrets Manager / IAM Role: follow-up
3. Short no-op / import smoke SubmitJob validation — based on `smoke` job name prefix + light command allowlist: follow-up (R-AUTO-015 mitigation alignment)
4. full backtest / long-running research / report generation — executed after separate cost / time / timeout / operator approval criteria are finalized: follow-up
5. Formal organization of the `research_app` per-schema GRANT matrix — 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) follow-up update (R-DATA-005 alignment)
6. Long-term organization of the 3 Research-internal adapters — move to `port_strategy_common` formal package / adapter (R-DATA-008 mitigation alignment / OD-MS-005 / OD-MS-014 follow-up alignment)
7. Step Functions integration / EventBridge Scheduler linkage: follow-up (04 spec follow-up phase / OD-MS-008 auxiliary policy alignment)
8. CI/CD OIDC / GitHub Actions automatic build / push: follow-up (07 spec responsibility)
9. aws-live cutover: follow-up (10 spec responsibility)

### 8. Safety / security check results

1. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog from this date's work. The `port_strategy_research` Dockerfile / requirements.txt / 3 adapters / 3 import-changed files the operator directly wrote are recorded as facts only in §2 / §3 of this note (0 full-body quotations).
2. 0 plaintext records in this note of actual secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / account number / token / account-id / actual ARN / image digest / IAM access key id / job ARN.
3. 0 actual `secretsmanager:GetSecretValue` calls (Secrets Manager `/portfolio/paper/rds/research-app` itself was not created this date). 0 secret value calls in the Kiro automatic validation / this note authoring process (R-DOCS-001 alignment).
4. AWS / Docker / ECR / IAM / Secrets Manager / RDS work is performed directly by the operator. Kiro only performs document authoring / procedure organization / validation item organization.
5. 0 broker / KIS / order / fill / Daily Batch entrypoint calls. This date's validation covers only Strategy Research image first preparation (local build + py_compile / import smoke + ECR push).
6. 0 full backtest / long-running research / report generation / extended analysis / block-family backtests. All heavy classification follow-up separation.
7. 0 RDS DDL / DML. 0 AWS Batch SubmitJob.
8. live automatic batch / report generation is still prohibited until follow-up validation / approval per OD-SAFE-002 / OD-SAFE-003 policy. This date is paper image first preparation.
9. Operational identifiers (instance id / private IP / SSM session id / RDS endpoint hostname from prior date's work) reused as-is. Additional operational identifiers in this section — all fact records / not secrets:
   - image tag: `paper-20260613` / `paper-latest`
   - ECR repository: `portfolio-strategy-research`
   - Dockerfile / requirements.txt: `port_strategy_research/Dockerfile` / `port_strategy_research/requirements.txt`
   - 3 Research adapters: `research_backtest_market_adapter.py` / `research_backtest_filter_adapter.py` / `research_backtest_sizing_adapter.py`
   - image size about 90MB

## IAM change record template (add per date as needed)

```
## YYYY-MM-DD IAM 변경
- 변경 일자: YYYY-MM-DDTHH:MM:SS+09:00
- 변경자: [운영자 식별자]
- 변경 사유: [한 줄]
- 변경 전 / 후 항목 요약: [추가·삭제 statement 수, Action·Resource 변경 요약 — JSON 본문 전체 인용 금지]
```

## 2026-06-15 Strategy Research AWS Batch execution skeleton complete

**Summary** — AWS Batch execution skeleton newly created + smoke SubmitJob first validation + Strategy Common first consistency confirmation.

Work scope (recovered from 2026-06-13 follow-up):

- (a) AWS Batch CE / Queue / JD first creation
- (b) Formal creation of CloudWatch Log Group / Secrets Manager / IAM Role
- (c) Short no-op / import smoke SubmitJob validation

Kiro only organizes documents / procedures / validation items.

### 1. Create AWS Batch Compute Environment

1. CE creation: complete
   1) name: `portfolio-paper-strategy-research-ce`
   2) type: `MANAGED`
   3) compute resources type: `FARGATE`
   4) maxvCpus: 4
   5) state: `ENABLED`
   6) status: `VALID`
2. Confirm NAT-free public subnet + assignPublicIp policy alignment: complete (OD-NET-004 alignment / same pattern as Strategy Decision / Strategy Execution / preprocessor / crawler smoke)
3. Confirm use of Service-linked role (`AWSServiceRoleForBatch`): complete (no separate new IAM Role creation)

### 2. Create Job Queue

1. Queue creation: complete
   1) name: `portfolio-paper-strategy-research-queue`
   2) priority: 10
   3) state: `ENABLED`
   4) status: `VALID`
   5) compute environment order: `portfolio-paper-strategy-research-ce` single

### 3. Register Job Definition revision 1

1. JD registration: complete
   1) family: `portfolio-paper-strategy-research`
   2) revision: 1
   3) image tag: `paper-latest` (reused as-is from the 2026-06-13 §3 push)
   4) platformCapabilities: `FARGATE`
   5) assignPublicIp: `ENABLED`
   6) vCPU: 1
   7) memory: 2048
   8) timeout (`attemptDurationSeconds`): 600 seconds
   9) default `command`: safe `py_compile`-family smoke (the operational entrypoint is decided in SubmitJob `--container-overrides` / a follow-up revision)
   10) execution role / job role / log configuration: §6 / §4 alignment

### 4. Create CloudWatch Log Group

1. Log Group creation: complete
   1) name: `/portfolio/paper/strategy-research`
   2) retention: 14 days (OD-OBS-002 alignment)
2. Job Definition `logConfiguration` = `awslogs` connection: complete (0 full-body quotations of the log driver)
3. log delivery errors: 0 (§7 / §8 / this date's follow-up §11 all confirmed normal receipt)

### 5. Prepare Secrets Manager research-app

1. secret newly created: complete
   1) name: `/portfolio/paper/rds/research-app`
   2) JSON multi-key structure: `host` / `port` / `dbname` / `username` / `password` (08 / 04 spec alignment)
2. 0 secret value exposures — 0 plaintext records in this note / operation note / work chat / CloudWatch Logs / docker build / ECR push / Console capture (R-DOCS-001 alignment)
3. key presence validation complete — first confirmation of 5 keys' existence via `secretsmanager:DescribeSecret` metadata output. The `GetSecretValue` result value was called by the operator only, with 0 plaintext records in this note. 0 `GetSecretValue` calls in the Kiro automatic validation / this note authoring process.

### 6. Prepare IAM Role

1. Execution Role: complete
   1) name: `portfolio-paper-research-batch-execution-role`
   2) trust: `ecs-tasks.amazonaws.com` (AWS Batch on Fargate standard)
   3) attached managed policy: `AmazonECSTaskExecutionRolePolicy`
   4) inline policy: research-app secret read (`secretsmanager:GetSecretValue` / `secretsmanager:DescribeSecret`) — Resource is limited to the single `/portfolio/paper/rds/research-app` secret ARN. 0 wildcards (OD-SEC-006 / 03 §13 alignment)
2. Job Role: complete
   1) name: `portfolio-paper-research-job-role`
   2) trust: `ecs-tasks.amazonaws.com`
   3) Initial smoke-stage permissions: least privilege (secret read is the Execution Role's responsibility / the Job Role itself uses only environment-variable-based + in-container direct access when connecting to RDS)
   4) S3 PutObject permission addition: added separately at this date's §11 stage (BACKTEST_REPORT S3 upload reinforcement) — Resource limited to `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` / Action limited to `s3:PutObject` / 0 public read permission grants
3. IAM change 4-line summary (aligned with the operation-notes IAM change record template):
   - 변경 일자: 2026-06-15(KST)
   - 변경자: 운영자(Kiro 직접 수행 0건)
   - 변경 사유: AWS Batch Strategy Research smoke / full / report 실행에 필요한 Execution / Job Role 신규 생성, research-app secret read inline policy 부여, 본 일자 후반부에 Job Role 에 S3 PutObject 한정 권한 추가
   - 변경 전 / 후 항목 요약: Execution Role 신규 / managed policy 1개 + inline policy 1개 / Job Role 신규 / inline policy 1개(S3 PutObject Resource 한정). 모든 정책 본문 전체 인용 0건. account-id / 실제 IAM Role ARN / 실제 secret ARN 본 노트 평문 기록 0건(`<account-id>` / `<region>` / `<role-arn>` / `<secret-arn>` placeholder).

### 7. py_compile smoke SubmitJob

1. SubmitJob execution: complete
   1) jobName: `smoke-strategy-research-import-20260615`
   2) jobId: `81ec3581-0204-43ea-8238-a2a6d22f3f28`
   3) jobDefinition: `portfolio-paper-strategy-research:1`
   4) container override command: `python -m py_compile`-family smoke
   5) status: `SUCCEEDED`
   6) exitCode: 0
2. CloudWatch Log Stream creation confirmed complete. 0 log delivery errors. 0 full-body quotations of the Log.
3. 0 image pull errors / 0 secret injection errors (this SubmitJob has no secret injected / `py_compile` simple import validation / 0 RDS access).
4. First application of the `smoke` job name prefix policy (R-AUTO-015 mitigation alignment).

### 8. DB smoke SubmitJob

1. SubmitJob execution: complete
   1) jobName: `smoke-strategy-research-db-20260615`
   2) jobId: `5399aa10-0fdd-466b-8079-236d3b7e7e37`
   3) jobDefinition: `portfolio-paper-strategy-research:1`
   4) container override command: DB smoke validation (import + Secrets Manager environment-variable-injection-based `psycopg2.connect` + `current_user` / `current_database` / `current_schema` output)
   5) status: `SUCCEEDED`
   6) exitCode: 0
2. DB smoke result:
   1) `db smoke ok`
   2) `current_user = research_app`
   3) `current_database = portfolio`
   4) first schema output = `research` (first search_path item)
   5) search_path alignment (`research, preprocessor, interest, reference, legacy, public` / OD-DB-006 / OD-DB-007 alignment)
3. Safety check:
   1) 0 secret value exposures (0 plaintext password output in the CloudWatch Logs body)
   2) 0 image pull errors
   3) 0 secret injection errors (normally injected via the Execution Role's inline policy)
   4) 0 log delivery errors
4. 0 RDS DDL / DML / only SELECT-limited metadata calls used.

### 9. Strategy Common first consistency confirmation

1. Confirm current role: complete
   1) `port_strategy_common` has no separate compute
   2) Included in the Decision / Execution / Research image via vendoring (OD-MS-014 alignment / 04 spec 2026-06-13 §1 alignment)
   3) Provides the strategy config / signal / sizing / result contract
2. First validation: complete
   1) `port_strategy_common` import smoke passed
   2) `port_strategy_decision`'s common import smoke passed
   3) `port_strategy_execution`'s common import smoke passed
   4) `port_strategy_research`'s common import smoke passed
   5) common main modules `python -m py_compile` passed:
       - `common_block_watch.py`
       - `common_buy_decision.py`
       - `common_buy_filter.py`
       - `common_buy_guard.py`
       - `common_buy_sizing.py`
       - `common_context.py`
       - `common_market.py`
       - `common_result.py`
       - `common_sell_decision.py`
       - `common_sell_guard.py`
       - `common_types.py`
       - `common_version.py`
       - `config.py`
       - `utils.py`
       - `__init__.py`
   6) 3 Research adapters (`research_backtest_market_adapter.py` / `research_backtest_filter_adapter.py` / `research_backtest_sizing_adapter.py`) `python -m py_compile` passed
3. packaging decision: complete
   1) For now, maintain Docker build context-based vendoring (OD-MS-014 / also aligned with this date's §11 image rebuild)
   2) Maintain the Docker build context `C:\Workspaces` basis
   3) git submodule is kept only as a candidate and not applied this date
   4) Recording the `port_strategy_common` git-sha / commit basis included per image is follow-up (07 spec / Strategy Common stage)
4. Expression / caution:
   1) This item is not Strategy Common formal packaging completion.
   2) The expression is recorded as "Strategy Common first consistency confirmation complete / formal package management is follow-up".
5. Follow-up separation:
   1) wheel / sdist generation
   2) CodeArtifact or internal artifact repo review
   3) package / version management review at the 07 spec (CI/CD) stage
   4) The candidate of moving the 3 Research adapters → `port_strategy_common` formal adapter is maintained (OD-MS-005 / OD-MS-014 / OD-MS-018 follow-up alignment)
   5) When moving the adapters, a Decision / Research result comparison test is needed (R-DATA-008 mitigation alignment)
   6) Formal documentation of the R-DATA-008 mitigation-based smoke / sample comparison procedure is needed

### 10. First validation completion criteria

1. AWS Batch Compute Environment / Job Queue / Job Definition revision 1 all ACTIVE / VALID
2. CloudWatch Log Group `/portfolio/paper/strategy-research` (retention 14 days) creation complete
3. Secrets Manager `/portfolio/paper/rds/research-app` (JSON multi-key) new creation complete
4. IAM Execution Role / Job Role new creation complete (secret ARN limited / 0 wildcards)
5. py_compile smoke SubmitJob `SUCCEEDED` / exitCode 0
6. DB smoke SubmitJob `SUCCEEDED` / exitCode 0 / `db smoke ok` / `research_app` / `portfolio` / `research` schema / search_path alignment
7. Strategy Common first consistency confirmation complete (import smoke / py_compile / vendoring maintenance decision)
8. All AWS / Docker / ECR / IAM / Secrets Manager / CloudWatch / Batch / RDS work performed directly by the operator (0 Kiro direct execution)
9. 0 broker / KIS / order / fill / Daily Batch entrypoint calls
10. 0 RDS DDL / DML
11. Judgment: Strategy Research AWS Batch execution skeleton + smoke first validation complete. Entry into the follow-up phase's BACKTEST_RESEARCH / BACKTEST_REPORT full execution is possible.

### 11. Outside this date's scope / follow-up handover

1. AWS Batch SubmitJob linkage from View Daily Batch — currently operator manual SubmitJob. The View `DailyBatchService`-side step mapping is follow-up (05 spec / 04 spec follow-up phase alignment)
2. Step Functions state machine definition / EventBridge Scheduler periodic trigger — 04 spec follow-up phase responsibility
3. Judgment on whether to port the block-family research entrypoints (`block_watch_analysis_run` / `block_watch_backtest_run` / `block_exception_buy_backtest_run` / `block_exception_buy_engine_run`) to AWS Batch — per this date's OD-MS-019 decision alignment, excluded from porting targets on the View Daily Batch basis, classified as manual auxiliary tools only when needed. heavy classification follow-up.
4. Moving the 3 Research-internal adapters to the `port_strategy_common` formal adapter — R-DATA-008 mitigation alignment / OD-MS-005 / OD-MS-014 / OD-MS-018 follow-up alignment
5. `port_strategy_common` formal package / version management (wheel / CodeArtifact / git submodule) — 07 spec / Strategy Common stage
6. CI/CD OIDC build / push automation — 07 spec responsibility
7. aws-live cutover — 10 spec responsibility

### 12. Safety / security check results

1. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog due to this date's work. The `port_strategy_research` Dockerfile / requirements.txt / 3 adapters / 3 import-changed files the operator directly modified / wrote are recorded as facts only in this spec operation-notes (2026-06-13 §2 / §3 / this section §9 / this date's §11) / 0 full-body quotations.
2. 0 plaintext records in this note of actual secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / account number / token / IAM access key id.
3. 0 plaintext records in this note of account-id / actual secret ARN / actual IAM Role ARN / image digest. Placeholders (`<account-id>` / `<region>` / `<secret-arn>` / `<role-arn>` / `<image-digest>`) used. jobId (`81ec3581-...` / `5399aa10-...`) / Job Definition family / revision / Compute Environment name / Job Queue name / Log Group name / Secret name / Role name / image tag (`paper-latest`) are operational identifier fact records per the user-specified policy alignment — not secrets.
4. AWS Batch / IAM / Secrets Manager / CloudWatch / RDS work performed directly by the operator. Kiro only performs document authoring / procedure organization / validation item organization.
5. 0 broker / KIS / order / fill / Daily Batch entrypoint calls. 0 RDS DDL / DML.
6. Both SubmitJobs in this section were executed within the `smoke` job name prefix (R-AUTO-015 mitigation alignment) / timeout 600 seconds / vCPU 1 / memory 2048 cap. 0 heavy-classification SubmitJobs.
7. live automatic batch / report generation is still prohibited until follow-up validation / approval per OD-SAFE-002 / OD-SAFE-003 policy. This date is paper environment validation.

### 13. Items recovered this date among 6/13 follow-ups (complete)

Among the items separated as follow-up in this spec 2026-06-13 §7 / §8 / `_common/followups-overview.md` 2026-06-13 09 spec follow-up memo, the items recovered at this date's skeleton / smoke stage are as follows. The 6/13 body is preserved as-is, and this section records only the completion recovery.

1. AWS Batch Compute Environment / Job Queue / Job Definition revision 1 first creation: complete (this date's §1 / §2 / §3)
2. CloudWatch Log Group `/portfolio/paper/strategy-research` creation + retention 14 days: complete (this date's §4)
3. Secrets Manager `/portfolio/paper/rds/research-app` JSON multi-key new creation: complete (this date's §5)
4. IAM Role reinforcement — Execution Role research-app secret read inline policy addition (secret ARN limited / 0 wildcards) + Job Role `portfolio-paper-research-job-role` new creation: complete (this date's §6 / the S3 PutObject permission addition is recovered in this date's later §11 BACKTEST_REPORT S3 upload reinforcement)
5. Short no-op / import smoke SubmitJob validation — based on `smoke` job name prefix + light command allowlist (R-AUTO-015 mitigation alignment): complete (this date's §7 / §8)

Items other than the above (BACKTEST_RESEARCH / BACKTEST_REPORT full execution / S3 upload reinforcement / View Daily Batch linkage / Step Functions / EventBridge / adapter formal move / common packaging / CI/CD / aws-live cutover) remain in other 2026-06-15 sections of this spec or as follow-up separation.

## 2026-06-15 Strategy Research AWS Batch full / report execution validation

**Summary** — As a follow-up to the same date's earlier section (smoke passed), enter single-run execution validation of BACKTEST_RESEARCH / BACKTEST_REPORT AWS Batch.

Work scope:

- BACKTEST_RESEARCH: full backtest + internal extended analysis
- BACKTEST_REPORT: 4 report generation

Role:

- First empirical demonstration of OD-MS-008 (Strategy Research compute first priority = AWS Batch) / OD-MS-018 (Research Batch image dependency boundary)
- OD-MS-019 new decision lock: porting target entrypoint = BACKTEST_RESEARCH + BACKTEST_REPORT 2 types / `run_extended_analysis.py` classified as manual auxiliary tool / report artifact S3 prefix `strategy-research/reports/`

### 1. BACKTEST_RESEARCH AWS Batch execution validation

1. Execution result: complete
   1) jobDefinition: `portfolio-paper-strategy-research:1` (used as-is from this date's §3 registration)
   2) container override command: `python -m port_strategy_research.backtest_research_run` (or equivalent entrypoint)
   3) status: `SUCCEEDED`
   4) exitCode: 0
   5) full backtest execution success — extended analysis also performed together inside `backtest_research_run`
2. backtest run result metadata:
   1) `run_id`: `a39b0b0c-cfe9-474e-8a4a-4ddb33f09567`
   2) `total_return`: 4.55930879
   3) `mdd`: -0.08941942
   4) `sharpe`: 2.65561307
   5) `trade_count`: 308
3. Deliverables:
   1) `research.strategy_backtest_run` 1 row / `research.strategy_backtest_daily` / `research.strategy_trade_log` / `research.strategy_backtest_*_analysis` accumulation — 0 row count / analysis full-body quotations in this note. 0 secret / plaintext data exposures.
4. Safety check:
   1) 0 image pull errors / 0 secret injection errors / 0 log delivery errors
   2) 0 RDS DDL. backtest run result / extended analysis insert / upsert is the normal `port_strategy_research` repository helper flow (2026-06-13 §1 alignment)
   3) 0 broker / KIS / order / fill / Daily Batch entrypoint calls
   4) Normal termination within the Job Definition timeout (`attemptDurationSeconds = 600`) — `full` job name prefix + operator direct approval flow (R-AUTO-015 mitigation alignment)

### 2. BACKTEST_REPORT AWS Batch execution validation

1. Execution result: complete
   1) jobDefinition: `portfolio-paper-strategy-research:1`
   2) container override command: `python -m port_strategy_research.backtest_report_run` (or equivalent entrypoint) — based on `BACKTEST_RESEARCH`'s latest `run_id`
   3) `REPORT_OUTPUT_DIR` environment variable: `/tmp/portfolio-reports` (formally introduced in this date's §11 BACKTEST_REPORT S3 upload reinforcement — Fargate ephemeral area alignment / auto-cleaned on container termination)
   4) status: `SUCCEEDED`
   5) exitCode: 0
   6) 4 report file generation success
       - 01_요약 리포트
       - 02_일자별 매매 리포트
       - 03_거래 상세 리포트
       - 04_추천 리포트
2. At this §2 point, S3 upload not applied (local file generation only). S3 upload reinforcement + validation is separated into this date's §11 ~ §17.
3. Safety check:
   1) RDS read limited. 0 RDS DDL / DML.
   2) 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog due to this date's work.
   3) 0 broker / KIS calls.

### 3. Finalize AWS Batch porting targets on the View Daily Batch basis

1. Decision: complete (OD-MS-019 new decision alignment)
   1) Among the existing local View Batch steps, only 2 types `BACKTEST_RESEARCH` / `BACKTEST_REPORT` are finalized as Strategy Research AWS Batch porting targets
   2) `run_extended_analysis.py` is excluded from separate AWS Batch porting targets (aligned with §4 below)
   3) `block_watch_analysis_run.py` / `block_watch_backtest_run.py` / `block_exception_buy_backtest_run.py` / `block_exception_buy_engine_run.py` are not View Daily Batch execution targets — excluded from AWS Batch porting targets at this date, classified as operator manual auxiliary tools only when needed (09 spec follow-up phase responsibility / R-AUTO-015 alignment)
2. Application scope:
   1) The same policy is used when entering Step Functions state machine definition / EventBridge Scheduler periodic trigger (04 spec follow-up phase)
   2) The same policy is used when entering the View Daily Batch ProcessBuilder → AWS Batch SubmitJob mapping (05 spec follow-up phase)

### 4. Finalize run_extended_analysis.py handling criteria

1. Handling criteria: complete
   1) `run_extended_analysis.py` is not a View Daily Batch execution target
   2) extended analysis is already performed inside `BACKTEST_RESEARCH` (`backtest_research_run`) (this date's §1 alignment)
   3) Excluded from separate AWS Batch porting targets
   4) Classified as a manual auxiliary script for regenerating the analysis tables (`strategy_backtest_*_analysis`) of an existing `run_id` when needed
   5) heavy classification — 0 SubmitJobs this date / on follow-up SubmitJob, proceeds only within `full` job name prefix + separate operator approval + Batch Job timeout / vCPU / memory cap (R-AUTO-015 mitigation alignment)

### 5. First validation completion criteria

1. BACKTEST_RESEARCH full execution `SUCCEEDED` / exitCode 0 / `run_id a39b0b0c-...` / total_return / mdd / sharpe / trade_count output confirmed
2. BACKTEST_REPORT 4 report generation `SUCCEEDED` / exitCode 0 / `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` application confirmed
3. AWS Batch porting targets 2 types (BACKTEST_RESEARCH / BACKTEST_REPORT) finalized on the View Daily Batch basis
4. `run_extended_analysis.py` handling criteria finalized (BACKTEST_RESEARCH internal handling + manual auxiliary tool classification)
5. The 4 block-family types are excluded from AWS Batch porting targets / classified as follow-up manual auxiliary tools only
6. Judgment: BACKTEST_RESEARCH / BACKTEST_REPORT first this-phase validation complete. The follow-up is separated into S3 upload reinforcement (§11 ~ §17) and View / Step Functions / EventBridge linkage (follow-up spec responsibility).

### 6. This phase's follow-up handover

1. Map the View Daily Batch `BACKTEST_RESEARCH` / `BACKTEST_REPORT` steps from ProcessBuilder direct execution to AWS Batch SubmitJob calls — 05 spec follow-up phase or 04 spec follow-up phase responsibility
2. Force the BACKTEST_RESEARCH → BACKTEST_REPORT order inside the Step Functions state machine + EventBridge Scheduler periodic trigger — 04 spec follow-up phase
3. Separate isolated operational mode for the block-family research entrypoints (`block_watch_*` / `block_exception_buy_*`) — 09 spec follow-up phase responsibility / heavy classification
4. `port_strategy_common` formal package / version management — 07 spec / Strategy Common stage
5. 3 Research-internal adapters → `port_strategy_common` formal adapter move — R-DATA-008 mitigation alignment / OD-MS-005 / OD-MS-014 / OD-MS-018 follow-up alignment
6. CI/CD OIDC build / push automation — 07 spec responsibility
7. aws-live cutover — 10 spec responsibility

### 7. Safety / security check results

1. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog due to this phase's work.
2. 0 plaintext records in this note of actual secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / account number / token / IAM access key id.
3. 0 plaintext records in this note of account-id / actual secret ARN / actual IAM Role ARN / image digest. Operational identifiers (jobDefinition family / revision / `run_id` / `REPORT_OUTPUT_DIR` path / job name prefix `full` / metric values) are fact records per the user-specified policy alignment — not secrets.
4. 0 broker / KIS / order / fill / Daily Batch entrypoint calls. 0 RDS DDL / DML. In this phase, AWS Batch SubmitJob is executed directly by the operator only, one run each of BACKTEST_RESEARCH / BACKTEST_REPORT — `full` job name prefix (R-AUTO-015 mitigation alignment).
5. live automatic batch / report generation is still prohibited until follow-up validation / approval per OD-SAFE-002 / OD-SAFE-003 policy. This date is paper environment validation.

## 2026-06-15 BACKTEST_REPORT S3 upload reinforcement

**Summary** — As a follow-up to the same date's §1 ~ §13 (skeleton) / §1 ~ §7 (RESEARCH / REPORT this-phase validation), REPORT S3 upload reinforcement.

- First empirical demonstration of the OD-MS-019 "report artifact S3 prefix `strategy-research/reports/`" policy
- Flow: reuse existing S3 bucket → add boto3 dependency → add wrapper S3 upload option → Docker rebuild + ECR push → register Job Definition revision 3 → validate S3 upload SubmitJob → confirm 4 S3 files exist
- The `port_strategy_research` requirements.txt addition / report wrapper change the operator directly wrote / modified are recorded as facts only in this note (0 full-body quotations)

### 11. Reuse existing S3 bucket

1. bucket reuse: complete
   1) name: `portfolio-paper-migration-yukiever`
   2) 0 new Strategy Research bucket creations — existing operational bucket reused
   3) prefix used: `strategy-research/reports/` (OD-MS-019 alignment)

### 12. Add TaskRole S3 PutObject permission

1. IAM change: complete
   1) target Role: `portfolio-paper-research-job-role` (permission added to this date's §6 Job Role body)
   2) permission scope: `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*`
   3) allowed Action: `s3:PutObject` limited
   4) 0 public read permission additions (`s3:PutObjectAcl` / `public-read` etc. not granted)
   5) 0 other prefix or other bucket access (0 Resource wildcards / 03 §13 / OD-SEC-006 alignment)
2. IAM change 4-line summary (aligned with the IAM change record template):
   - 변경 일자: 2026-06-15(KST)
   - 변경자: 운영자(Kiro 직접 수행 0건)
   - 변경 사유: BACKTEST_REPORT 산출물 S3 업로드를 위해 Job Role 에 prefix 한정 PutObject 권한 1건 추가
   - 변경 전 / 후 항목 요약: Job Role inline policy 1개 추가(Resource = `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` / Action = `s3:PutObject` 한정 / public read 0건 / wildcard 0건). 정책 본문 전체 인용 0건. 실제 IAM Role ARN / account-id 본 노트 평문 기록 0건.

### 13. Add boto3 dependency

1. Change: complete (operator directly wrote)
   1) Added 1 line `boto3` to `port_strategy_research/requirements.txt` (0 full-body quotations)
   2) In-Docker-image import smoke passed — `python -c "import boto3"` normal
   3) Existing dependencies (`psycopg2-binary` / `pandas` / `numpy`) maintained as-is
2. R-DATA-008 / OD-MS-018 alignment — 0 changes to the adapter no-own-decision-logic policy. boto3 is used only in the wrapper's S3 upload stage.

### 14. Add report wrapper S3 upload option

1. Change: complete (operator directly wrote / 0 full-body quotations in this note)
   1) When the `REPORT_S3_BUCKET` environment variable is set → perform S3 upload
   2) When `REPORT_S3_BUCKET` is unset → local file generation as before, then upload skip (0 functional regression)
   3) `REPORT_S3_PREFIX` environment variable default = `strategy-research/reports`
   4) Sub-path separation based on `AWS_BATCH_JOB_ID` (environment variable AWS Batch automatically injects) — the actual path pattern the operator validated in this date's §16 / §17 is `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/...`
   5) `REPORT_OUTPUT_DIR` environment variable default = `/tmp/portfolio-reports` maintained (Fargate ephemeral area alignment / auto-cleaned on container termination)
2. Safety check:
   1) This wrapper performs only SELECT / local file generation / S3 PutObject / 0 RDS DDL / DML
   2) 0 public read grants / KMS encryption decided separately follow-up (this date uses the bucket's default SSE)

### 15. Docker rebuild / ECR push

1. rebuild + push: complete
   1) imageTag: `paper-20260615-report-s3`
   2) digest: `<image-digest>` placeholder (`sha256:7d28...4c4a7` truncated form / 09 spec 2026-06-13 §3 / R-DOCS-001 alignment — 0 full digest plaintext records)
   3) pushedAt: 2026-06-15T17:00:10+09:00(KST)
   4) size: about 106MB (106315984 bytes) — reflects the boto3 addition + cache difference versus about 90MB in 2026-06-13 §3
2. 0 changes to the Docker build context `C:\Workspaces` / image inclusion = `port_strategy_research` + `port_strategy_common` / image exclusion = `port_strategy_decision` (OD-MS-018 alignment) policy.
3. 0 new ECR repository `portfolio-strategy-research` creations — reused as-is from 2026-06-13 §3.

### 16. Register Job Definition revision 3

1. JD registration: complete
   1) family: `portfolio-paper-strategy-research`
   2) revision: 3 (this date's §3 revision 1 + intermediate-stage revision 2 follow-up)
   3) image: `paper-20260615-report-s3`
   4) TaskRole: `portfolio-paper-research-job-role` (as-is with this date's §12 permission addition)
   5) No change to the Execution Role / log configuration / network mode / vCPU / memory / timeout / assignPublicIp policy (this date's §3 / §4 / §6 alignment)
   6) Environment variables: `REPORT_OUTPUT_DIR=/tmp/portfolio-reports`, `REPORT_S3_BUCKET=portfolio-paper-migration-yukiever`, `REPORT_S3_PREFIX=strategy-research/reports` (`AWS_BATCH_JOB_ID` is auto-injected by AWS Batch)

### 17. S3 upload Batch execution validation

1. SubmitJob execution: complete
   1) jobName: `strategy-research-backtest-report-s3-20260615`
   2) jobId: `112f5fe4-02f3-4614-a88c-60a9842e1447`
   3) jobDefinition: `portfolio-paper-strategy-research:3`
   4) container override command: BACKTEST_REPORT execution + S3 upload (this date's §14 wrapper option applied)
   5) status: `SUCCEEDED`
   6) exitCode: 0
   7) logStreamName: `strategy-research/default/b18e548d46764cd791028088e1d32a6d`
2. Safety check:
   1) 0 image pull errors / 0 secret injection errors / 0 log delivery errors
   2) RDS read limited. 0 RDS DDL / DML.
   3) 0 broker / KIS calls.
   4) Although a `full`-classification SubmitJob, normal termination within the timeout 600 seconds / vCPU 1 / memory 2048 cap (R-AUTO-015 mitigation alignment).
   5) 0 AWS Batch automatic retries (OD-SAFE-004 / R-AUTO-001 alignment — Job Definition `attempts = 1`).

### 18. Confirm 4 S3 files exist

1. Confirm 4 S3 objects: complete
   1) `s3://portfolio-paper-migration-yukiever/strategy-research/reports/20260615/112f5fe4-02f3-4614-a88c-60a9842e1447/01_요약_리포트_20260615_v1.txt`
   2) `s3://portfolio-paper-migration-yukiever/strategy-research/reports/20260615/112f5fe4-02f3-4614-a88c-60a9842e1447/02_일자별_매매_리포트_20260615_v1.txt`
   3) `s3://portfolio-paper-migration-yukiever/strategy-research/reports/20260615/112f5fe4-02f3-4614-a88c-60a9842e1447/03_거래_상세_리포트_20260615_v1.txt`
   4) `s3://portfolio-paper-migration-yukiever/strategy-research/reports/20260615/112f5fe4-02f3-4614-a88c-60a9842e1447/04_추천_리포트_20260615_v1.txt`
2. ACL / public setting: all 4 kept private (0 public read grants). 0 full-body quotations.
3. prefix alignment: `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/...` (OD-MS-019 alignment / this date's §14 wrapper option result).

### 19. First validation completion criteria

1. Reuse existing S3 bucket `portfolio-paper-migration-yukiever` / 0 new bucket creations
2. Add prefix-limited `s3:PutObject` permission to the Job Role (0 wildcards / 0 public read)
3. boto3 dependency addition + import smoke success
4. Add the report wrapper's `REPORT_S3_BUCKET` / `REPORT_S3_PREFIX` / `AWS_BATCH_JOB_ID`-based sub-path separation / `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` default option
5. Docker rebuild (`paper-20260615-report-s3`) + ECR push complete (image size about 106MB / image digest placeholder used)
6. Job Definition revision 3 registration (`portfolio-paper-strategy-research:3`, image `paper-20260615-report-s3`, TaskRole `portfolio-paper-research-job-role`)
7. SubmitJob `strategy-research-backtest-report-s3-20260615` (jobId `112f5fe4-...`) `SUCCEEDED` / exitCode 0 / logStreamName confirmed
8. Confirm 4 S3 objects exist (prefix `strategy-research/reports/20260615/112f5fe4-.../` / file names `01_요약 리포트` ~ `04_추천 리포트`)
9. 0 RDS DDL / DML / 0 broker / KIS calls / 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog
10. Judgment: BACKTEST_REPORT S3 upload reinforcement first validation complete. The follow-up is separated into View / Step Functions / EventBridge linkage + S3 lifecycle policy / KMS encryption decision + adapter formal move + common packaging + CI/CD + aws-live cutover.

### 20. This phase's follow-up handover (S3 upload reinforcement standalone)

1. S3 lifecycle policy — applied after operator decision (storage cost accumulation from report accumulation / R-COST-003 alignment). lifecycle not set this date.
2. S3 KMS encryption — currently uses bucket default SSE / CMK application is a follow-up spec (06 / 09 follow-up phase) decision.
3. Map the View Daily Batch `BACKTEST_REPORT` step's ProcessBuilder → AWS Batch SubmitJob call + pass S3 prefix consistency — 05 / 04 spec follow-up phase responsibility
4. Force the `BACKTEST_RESEARCH` → `BACKTEST_REPORT` order inside the Step Functions state machine + EventBridge Scheduler periodic trigger — 04 spec follow-up phase
5. Formalize the manual auxiliary tool operational procedure for heavy jobs (`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) — 09 spec follow-up phase / R-AUTO-015 mitigation alignment

### 21. Safety / security check results (S3 upload reinforcement standalone)

1. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog due to this phase's work. The `port_strategy_research` requirements.txt (boto3 1-line addition) / report wrapper change / Dockerfile change (if any) the operator directly wrote / modified are recorded as facts only in this note's §13 / §14 / §15 / 0 full-body quotations.
2. 0 plaintext records in this note of actual secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / account number / token / IAM access key id.
3. 0 plaintext records in this note of account-id / actual secret ARN / actual IAM Role ARN / actual ECR URI / image digest (full sha256). Only the following operational identifiers are fact records per the user-specified policy alignment — not secrets:
   - image tag: `paper-20260615-report-s3` / image size about 106MB / pushedAt 2026-06-15T17:00:10+09:00
   - jobName: `strategy-research-backtest-report-s3-20260615`
   - jobId: `112f5fe4-02f3-4614-a88c-60a9842e1447`
   - logStreamName: `strategy-research/default/b18e548d46764cd791028088e1d32a6d`
   - S3 bucket: `portfolio-paper-migration-yukiever`
   - S3 prefix: `strategy-research/reports/20260615/112f5fe4-...`
   - file names: `01_요약 리포트` ~ `04_추천 리포트`
4. AWS / IAM / Secrets Manager / S3 / Docker / ECR / Batch / RDS / GRANT work all performed directly by the operator. Kiro only performs document authoring / procedure organization / validation item organization. 0 plaintext records of `secretsmanager:GetSecretValue` result values. 0 plaintext secret value output in the CloudWatch Logs body. 0 full-body quotations of S3 objects.
5. 0 broker / KIS / order / fill / Daily Batch entrypoint calls. 0 RDS DDL / DML. In this phase, AWS Batch SubmitJob is a single run for BACKTEST_REPORT S3 upload validation — `full` job name prefix (R-AUTO-015 mitigation alignment).
6. 0 public read grants / 0 wrong bucket uploads / 0 wrong prefix uploads. First empirical demonstration of the S3 prefix `strategy-research/reports/`-limited policy (R-COST-003 / OD-MS-019 alignment).
7. live automatic batch / report generation is still prohibited until follow-up validation / approval per OD-SAFE-002 / OD-SAFE-003 policy.

### 22. Items recovered this phase among 6/13 follow-ups (complete)

Among the items separated as follow-up in this spec 2026-06-13 §7 / `_common/followups-overview.md` 2026-06-13 09 spec follow-up memo, the items recovered in this phase (S3 upload reinforcement) are as follows. The 6/13 body is preserved as-is, and this section records only the completion recovery.

1. full backtest / long-running research / report generation — executed after separate cost / time / timeout / operator approval criteria are finalized: complete (this date's §1 / §2 BACKTEST_RESEARCH + BACKTEST_REPORT single-run validation)
2. BACKTEST_REPORT's `REPORT_OUTPUT_DIR` separation / S3 upload option / S3 prefix alignment: complete (this date's §14 ~ §18)
3. Formal decision of per-heavy-classification-candidate (`backtest_research_run` / `backtest_report_run` / `run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) timeout / vCPU / memory — in this phase, only BACKTEST_RESEARCH + BACKTEST_REPORT are first finalized at vCPU 1 / memory 2048 / timeout 600 seconds. The rest (`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) remain, per the OD-MS-019 decision alignment, excluded from AWS Batch porting targets / classified as manual auxiliary tools / a separate follow-up phase responsibility.

Items other than the above (View Daily Batch SubmitJob linkage / Step Functions / EventBridge / adapter formal move / common packaging / CI/CD / aws-live cutover) remain in this spec's follow-up separation.


## 2026-06-16 Strategy Research AWS Batch Backend dry-run re-validation

**Summary** — On top of the 08 spec 2026-06-16 raw freshness recovery, first validation of restarting the Research → Decision safe subset dry-run.

Work scope:

- (a) BACKTEST_RESEARCH AWS Batch single re-run
- (b) BACKTEST_REPORT formal Job Definition `portfolio-paper-strategy-report` new registration + rev1 ~ rev3 correction + final rev3 single-run success + confirm 4 S3 uploads exist

Safety:

- Kiro: document / procedure / validation item organization only. Operator: directly performs the actual AWS Batch / IAM / S3 / Docker / ECR work
- 0 actual BUY / SELL orders / `--execute` / fill · position sync automatic retries / 0 aws-live work (OD-SAFE-001 ~ OD-SAFE-004 / OD-MS-021 alignment)

### 1. BACKTEST_RESEARCH AWS Batch re-run

1. SubmitJob execution: complete
   1) Job Definition: `portfolio-paper-strategy-research:5`
   2) cluster: `portfolio-paper-strategy-research-ce` single / queue `portfolio-paper-strategy-research-queue`
   3) execution status: `SUCCEEDED`
   4) container exitCode: `0`
   5) 0 Batch automatic retries (`attempts = 1` / OD-SAFE-004 / R-AUTO-001 / R-AUTO-015 alignment)
2. Confirm latest run result: complete
   1) `research.strategy_backtest_run` latest row run_id: `439d78e7-41fd-4bb7-b455-18564ddff758`
   2) backtest_end_date: `2026-06-15` confirmed
   3) `strategy_trade_log` row_count: `310`
   4) `strategy_backtest_daily` row_count: `822`
   5) `strategy_backtest_daily_position` row_count: `2375`
3. Performance metrics: complete
   1) total_return: `4.66534417`
   2) mdd: `-0.08941942`
   3) sharpe: `2.68071466`
   4) trade_count: `310`
4. Safety check: complete
   1) 0 RDS DDL / DML is limited to the normal backtest run flow
   2) 0 broker / KIS calls / 0 Daily Batch entrypoint direct calls
   3) 0 heavy-classification (`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) SubmitJobs (OD-MS-019 alignment)
   4) 0 plaintext records in this note of image digest / job ARN / account-id (`<image-digest>` / `<job-arn>` / `<account-id>` placeholder)

### 2. BACKTEST_REPORT formal Job Definition progression flow (rev1 ~ rev3)

1. rev1 registration: complete (correction history)
   1) family: `portfolio-paper-strategy-report:1`
   2) command: `python -m port_strategy_research.backtest_report_run` (direct execution structure)
   3) Result: local report 4-file generation success / S3 upload wrapper not performed
   4) Judgment: unsuitable as the formal BACKTEST_REPORT operational path — the wrapper call is missing, so it does not proceed to the S3 upload stage
2. rev2 registration: complete (correction history)
   1) family: `portfolio-paper-strategy-report:2`
   2) command: `python aws_batch_backtest_report_wrapper.py` (direct file execution structure)
   3) Result: failed due to absence of the `/app/aws_batch_backtest_report_wrapper.py` path in the container
   4) Cause: the current image's module placement (`port_strategy_research/aws_batch_backtest_report_wrapper.py`) does not align with the direct file path call method
   5) Action: corrected to the module call method (`python -m`) in rev3
3. rev3 registration: complete (final operational path)
   1) family: `portfolio-paper-strategy-report:3`
   2) command: `python -m port_strategy_research.aws_batch_backtest_report_wrapper`
   3) image tag: `paper-20260615-report-s3` (reused as-is from the 2026-06-15 §15 reinforcement)
   4) Environment variables
       - `REPORT_OUTPUT_DIR=/tmp/portfolio-reports`
       - `REPORT_S3_BUCKET=portfolio-paper-migration-yukiever`
       - `REPORT_S3_PREFIX=strategy-research/reports`
   5) Job Role: `portfolio-paper-research-job-role`'s S3 PutObject Resource = `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` limited (2026-06-15 §12 reinforcement reused / 0 public read / 0 wildcards)
4. Final decision: the formal BACKTEST_REPORT Job Definition is finalized as `portfolio-paper-strategy-report:3`. rev1 / rev2 are recorded only as closed-nature correction history (R-AUTO-015 mitigation·detection reinforcement).

### 3. BACKTEST_REPORT rev3 SubmitJob execution validation

1. SubmitJob execution: complete
   1) jobName: `portfolio-paper-backtest-report-s3-20260616-rev3`
   2) Job Definition: `portfolio-paper-strategy-report:3`
   3) status: `SUCCEEDED`
   4) container exitCode: `0`
   5) 0 Batch automatic retries (`attempts = 1`)
2. Input run_id: `439d78e7-41fd-4bb7-b455-18564ddff758` (§1 alignment)
3. Confirm local report 4-file generation: complete
   1) `01_요약_리포트_20260616_v1.txt`
   2) `02_일자별_매매_리포트_20260616_v1.txt`
   3) `03_거래_상세_리포트_20260616_v1.txt`
   4) `04_추천_리포트_20260616_v1.txt`
4. wrapper S3 upload completed count: `4` confirmed

### 4. Confirm S3 upload result

1. S3 prefix: complete
   1) `strategy-research/reports/20260616/67522706-9b5f-4770-a312-ceb1987c4655/`
   2) bucket: `portfolio-paper-migration-yukiever` (existing reused / OD-MS-019 alignment)
2. Confirm 4 S3 objects exist: complete
   1) `01_요약_리포트_20260616_v1.txt` — 7,258 bytes
   2) `02_일자별_매매_리포트_20260616_v1.txt` — 552,540 bytes
   3) `03_거래_상세_리포트_20260616_v1.txt` — 329,088 bytes
   4) `04_추천_리포트_20260616_v1.txt` — 11,847 bytes
3. kept private / 0 public read grants / KMS encryption decided separately follow-up (R-COST-003 alignment)
4. 0 plaintext records in this note of actual account-id / actual IAM Role ARN / actual secret ARN / actual S3 object ARN / image digest full sha256 (`<account-id>` / `<role-arn>` / `<secret-arn>` / `<image-digest>` placeholder)

### 5. First validation completion criteria

1. BACKTEST_RESEARCH AWS Batch execution validation: complete (2026-06-16 §1 alignment / `portfolio-paper-strategy-research:5` `SUCCEEDED` / run_id `439d78e7-...`)
2. BACKTEST_REPORT AWS Batch execution validation: complete (2026-06-16 §2 / §3 alignment / final Job Definition `portfolio-paper-strategy-report:3` / `SUCCEEDED` / exitCode 0)
3. BACKTEST_REPORT S3 upload reinforcement first empirical demonstration: complete (2026-06-16 §4 alignment / 4 objects exist in S3 prefix `strategy-research/reports/20260616/67522706-9b5f-4770-a312-ceb1987c4655/`)
4. BACKTEST_REPORT operational path correction: complete (rev1 local-only / rev2 path absence / rev3 finalized to the module call method)
5. 0 heavy-classification SubmitJobs / 0 Batch automatic retries / 0 RDS DDL / 0 broker · KIS calls / 0 aws-live work
6. Judgment: Strategy Research AWS Batch formal BACKTEST_REPORT Job Definition finalized + safe subset re-run validation complete

### 6. Follow-up handover

1. Map the View Daily Batch `BACKTEST_RESEARCH` / `BACKTEST_REPORT` steps from ProcessBuilder direct execution to AWS Batch SubmitJob calls — 05 spec / 04 spec follow-up phase responsibility
2. Step Functions state machine definition (force the BACKTEST_RESEARCH → BACKTEST_REPORT order + prohibit automatic retry) + EventBridge Scheduler periodic trigger — 04 spec follow-up phase responsibility
3. S3 lifecycle policy + KMS encryption decision — R-COST-003 alignment / 06 spec follow-up phase
4. Cleanup of `portfolio-paper-strategy-report:1` / `portfolio-paper-strategy-report:2` (prohibit operational use / Inactive handling or deregister) is separated as a follow-up operator decision
5. Formalize the manual auxiliary tool operational procedure for heavy-classification entrypoints (`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) — 09 spec follow-up phase / R-AUTO-015 mitigation alignment
6. aws-live cutover — 10 spec responsibility

### 7. Safety / security check results

1. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog due to this date's work. The `port_strategy_research/aws_batch_backtest_report_wrapper.py` / Dockerfile / requirements.txt changes the operator directly wrote / modified are recorded as facts only in this note's §2 / §3 (0 full-body quotations).
2. 0 plaintext records in this note of actual secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / account number / token / account-id / actual secret ARN / actual IAM Role ARN / image digest full sha256 / IAM access key id / job ARN. All `[REDACTED]` or placeholder.
3. AWS Batch / IAM / S3 / Docker / ECR / RDS work all performed directly by the operator. Kiro only performs document authoring / procedure organization / validation item organization. 0 plaintext records of `secretsmanager:GetSecretValue` result values.
4. 0 plaintext quotations in this note of the CloudWatch Logs body / Batch console response body / Docker build log / S3 upload client log. Only facts (jobName / status / exitCode / row count / file name / object size / S3 prefix) are recorded.
5. 0 broker / KIS / order / fill / Daily Batch entrypoint direct calls. 0 new BUY / SELL / cancel / modify / `--execute`. 0 fill / position sync automatic retries.
6. 0 RDS DDL / DML is limited to BACKTEST_RESEARCH's normal backtest run flow + BACKTEST_REPORT's read-only lookup.
7. live automatic batch / report generation is still prohibited until follow-up validation / approval per OD-SAFE-002 / OD-SAFE-003 policy. This date is paper environment limited.

### 8. Task completion handling (this spec)

This spec has no separate tasks.md, so this noted per-task completion handling is recorded directly in this section.

1. BACKTEST_RESEARCH AWS Batch execution validation: complete (§1 / `portfolio-paper-strategy-research:5` `SUCCEEDED` / run_id `439d78e7-...`)
2. BACKTEST_REPORT AWS Batch execution validation: complete (§2 / §3 / final `portfolio-paper-strategy-report:3` `SUCCEEDED`)
3. BACKTEST_REPORT S3 upload reinforcement: complete (§4 / confirmed 4 S3 objects exist)
4. BACKTEST_REPORT formal Job Definition operational path correction: complete (§2 / rev1 local-only / rev2 path absence / rev3 finalized to the module call method)
5. View Daily Batch `BACKTEST_RESEARCH` / `BACKTEST_REPORT` step → AWS Batch SubmitJob mapping: follow-up (§6 / 05 spec / 04 spec follow-up phase)
6. Step Functions state machine + EventBridge Scheduler: follow-up (§6 / 04 spec follow-up phase)
7. S3 lifecycle / KMS encryption: follow-up (§6 / R-COST-003 / 06 spec)
8. heavy classification operational procedure formalization: follow-up (§6 / R-AUTO-015)
9. aws-live cutover: follow-up (§6 / 10 spec)


## 2026-06-17 Daily AWS 17-step E2E complete (Strategy Research)

**Summary** — The Daily AWS 17-step E2E flow was connected end-to-end this date.

- This spec's scope steps: step 4 `BACKTEST_RESEARCH` / step 5 `BACKTEST_REPORT`
- Full 17-step progress status: see the 2026-06-17 section of the 03 / 04 / 08 spec operation-notes
- Kiro: document / procedure organization only. Operator: directly performs the actual AWS Batch SubmitJob / IAM / RDS / S3 work
- Safety: `aws-paper` limited / 0 aws-live work / 0 heavy-classification (`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) SubmitJobs maintained (OD-MS-019 / R-AUTO-015 alignment)

### 1. Step 4 `BACKTEST_RESEARCH`

1. AWS Batch SubmitJob execution: complete
   1) Job Definition: `portfolio-paper-strategy-research:5` (2026-06-16 §1 alignment / same revision reused this date)
   2) launch type: FARGATE / awsvpc / image tag `paper-20260616` or the latest paper image at that time (operator directly decides)
2. Execution result: success
   1) job status: `SUCCEEDED`
   2) container exitCode: `0`
   3) 0 Batch automatic retries (OD-SAFE-004 / R-AUTO-001 alignment)
3. Result confirmation: complete
   1) latest result date: `2026-06-16`
   2) Sharpe Ratio: `2.68`
   3) `research.strategy_backtest_daily` freshness: `2026-06-16` confirmed
   4) `research.strategy_backtest_daily_position` freshness: `2026-06-16` confirmed
4. Safety check: complete
   1) extended analysis is already performed inside BACKTEST_RESEARCH (OD-MS-019 alignment) — no separate SubmitJob
   2) 0 heavy-classification SubmitJobs maintained
   3) 0 RDS DDL / 0 broker · KIS calls / 0 `--execute` calls

### 2. Step 5 `BACKTEST_REPORT`

1. AWS Batch SubmitJob execution: complete
   1) Job Definition: `portfolio-paper-strategy-report:3` (rev3 module call method / 2026-06-16 §2 / §3 alignment)
   2) image tag `paper-20260615-report-s3` or a paper image the operator directly updated
   3) env: `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` / `REPORT_S3_BUCKET=portfolio-paper-migration-yukiever` / `REPORT_S3_PREFIX=strategy-research/reports`
2. Execution result: success
   1) job status: `SUCCEEDED`
   2) container exitCode: `0`
3. Confirm 4 S3 object generation: complete
   1) prefix: `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/` alignment (OD-MS-019 alignment)
   2) `01_요약_리포트` / `02_일자별_매매_리포트` / `03_거래_상세_리포트` / `04_추천_리포트` all 4 types confirmed to exist
   3) kept private / 0 public read grants / 0 wildcards (R-COST-003 mitigation alignment)
4. report upload prefix / object count validation: complete
   1) The Job Role's `s3:PutObject` Resource is `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*`-limited policy alignment (OD-MS-019 / R-COST-003 alignment)
   2) 0 PutObject patterns to other prefixes / other buckets

### 3. Safety / security check results

1. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to this date's work. If there is a `port_strategy_research/aws_batch_backtest_report_wrapper.py` / related Dockerfile / requirements.txt the operator directly updated, it is recorded as facts only (0 full-body quotations).
2. 0 plaintext records of the following sensitive information — all `[REDACTED]` or placeholder:
   - secret value / RDS password / RDS endpoint hostname / token
   - KIS app key / KIS app secret / account number
   - account-id / actual secret ARN / actual IAM Role ARN / image digest full sha256 / IAM access key id / job ARN / task ARN / S3 bucket ARN

   Only operational identifiers as fact records: Job Definition family `portfolio-paper-strategy-research` / `portfolio-paper-strategy-report` / S3 prefix `strategy-research/reports/` / Sharpe Ratio `2.68` / latest result date `2026-06-16` / 4 report file names.
3. AWS Batch / ECS / IAM / S3 / Docker / ECR / RDS work all performed directly by the operator. Kiro only performs document authoring / procedure organization / validation item organization. 0 plaintext records of `secretsmanager:GetSecretValue` result values. 0 plaintext quotations in this note of the CloudWatch Logs body / Batch console response / S3 client log.
4. 0 broker / KIS / order / fill / Daily Batch entrypoint direct calls. 0 heavy-classification (`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) SubmitJobs maintained (R-AUTO-015 alignment).
5. 0 RDS DDL. DML is limited to BACKTEST_RESEARCH's normal backtest run flow — `research.strategy_backtest_run` / `research.strategy_backtest_daily` / `research.strategy_backtest_daily_position` / `research.strategy_trade_log` / `research.strategy_backtest_*_analysis` normal insert / upsert.
6. live automatic batch / report generation is still prohibited until follow-up validation / approval per OD-SAFE-002 / OD-SAFE-003 policy. This date is `aws-paper` limited / 0 aws-live work. S3 lifecycle policy / KMS encryption decision is follow-up (R-COST-003 alignment).
7. With the Daily AWS 17-step E2E paper first pass, backend AWS E2E steps 4 / 5 are aligned — OD-MS-008 / OD-MS-019 mitigation first empirical demonstration / Status kept at existing values as-is.


## 2026-06-22 Daily AWS Paper Step 4 / Step 5 re-validation (Strategy Research)

**Summary** — In the 2nd Daily wrapper actual operational run, re-validated 0 Step 4 / Step 5 regressions.

- Environment: `aws-paper` / RunDate `2026-06-22` / region `ap-northeast-2`
- This spec's scope steps: step 4 `BACKTEST_RESEARCH` / step 5 `BACKTEST_REPORT`
- Full 17-step progress status / Daily wrapper full-run result: 03 / 04 / 08 spec operation-notes 2026-06-22 / [`../_common/operator-decisions.md`](../_common/operator-decisions.md) Change Log 2026-06-22
- Kiro: document / procedure organization only. Operator: directly performs the actual AWS Batch SubmitJob / IAM / RDS / S3 / CloudWatch work
- Safety: `aws-paper` limited / 0 aws-live work / 0 heavy-classification (`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) SubmitJobs maintained (OD-MS-019 / R-AUTO-015 alignment)

### 1. Step 4 `BACKTEST_RESEARCH`

1. AWS Batch SubmitJob: complete
   1) Job Queue `portfolio-paper-strategy-research-queue` / Job Definition `portfolio-paper-strategy-research:5` (prior date alignment / no revision change)
   2) Job Status `SUCCEEDED` / exit code 0 — 0 plaintext records in this note of jobId / job ARN / placeholder handling
2. Result alignment: confirmed
   1) `research.strategy_backtest_run` new row — RunDate alignment
   2) `research.strategy_backtest_daily` / `research.strategy_backtest_daily_position` normal accumulation
3. 0 heavy-classification SubmitJobs maintained (R-AUTO-015 alignment)

### 2. Step 5 `BACKTEST_REPORT`

1. AWS Batch SubmitJob: complete
   1) Same Job Queue / Job Definition `portfolio-paper-strategy-report:3` (prior date alignment / no revision change)
   2) Job Status `SUCCEEDED` / exit code 0
2. Result alignment: confirmed
   1) 4 report objects aligned in S3 prefix `s3://portfolio-paper-migration-yukiever/strategy-research/reports/20260622/<aws-batch-job-id>/` (jobId / job ARN placeholder handling)
   2) 0 public read / 0 regression of the Job Role inline policy Resource-limited policy (OD-MS-019 alignment / R-COST-003 mitigation alignment)

### 3. Daily wrapper 1 ~ 17 second actual full-run alignment

1. The Daily wrapper 1 ~ 17 second actual full-run result is outside this spec's scope, and this date's Step 4 / Step 5 passed with 0 regressions in the same flow as the first full-run (2026-06-18).
2. The body decision values of the Strategy Research AWS Batch first-priority decision (OD-MS-008) / Research Batch image dependency boundary (OD-MS-018) / porting targets + S3 prefix (OD-MS-019) are unchanged.
3. 0 regression of the policy blocking heavy-classification entrypoints from flowing into SubmitJob / 0 Compute Environment vCPU / Job Queue depth / Cost Explorer cost spike.

### 4. Decision / risk change summary

1. New decisions: 0. New risks: 0.
2. Decisions with no body change: first empirical demonstration memo reinforcement
   1) OD-MS-008 — Research AWS Batch first priority first empirically demonstrated with 0 Daily run actual regressions
   2) OD-MS-019 — `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2 types limited + S3 prefix `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/` 0 regressions / 0 heavy-classification SubmitJobs maintained
3. Risks with no body change: reinforcement memo
   1) R-AUTO-015 — 0 regression of the heavy-classification SubmitJob blocking policy / Status `Mitigated` maintained
   2) R-COST-003 — the S3 report accumulation cost / lifecycle-unset risk is unchanged this date / follow-up lifecycle decision remains follow-up

### 5. Safety / security check results (2026-06-22)

1. Within this spec's scope, 0 broker / KIS calls. 0 `--execute` calls. 0 fill · position sync automatic retries. 0 aws-live work.
2. 0 RDS DDL. DML is limited to `research.strategy_backtest_run` / `research.strategy_backtest_daily` / `research.strategy_backtest_daily_position` / `research.strategy_backtest_*_analysis` normal accumulation. S3 PutObject is Job Role inline policy Resource-limited (OD-MS-019 alignment).
3. 0 plaintext records in this note of actual secret value / RDS password / RDS endpoint hostname / account-id / actual secret ARN / actual IAM Role ARN / image digest full sha256 / IAM access key id / jobId / job ARN. All `[REDACTED]` or placeholder.
4. AWS / Batch / IAM / Secrets Manager / RDS / S3 / CloudWatch work all performed directly by the operator — Kiro only performs document authoring / procedure organization. 0 AWS CLI / boto3 executions. 0 AWS resource creation / modification / deletion. 0 plaintext records of `secretsmanager:GetSecretValue` result values. 0 plaintext quotations of the CloudWatch Logs body / Batch job describe body / S3 object body.
5. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to this date's work (spec area). Only operational identifiers (Job Queue / Job Definition family · revision / Log Group name / S3 prefix pattern / RunDate `2026-06-22`) are fact records per the user-specified policy alignment — not secrets.
