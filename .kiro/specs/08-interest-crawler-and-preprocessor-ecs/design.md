# Design Document — 08-interest-crawler-and-preprocessor-ecs

## Introduction

This summarizes the core of this spec in one line. **It fixes the criteria for the procedures to enable a first-round execution validation of the two Python MS `port-interest-crawler` / `port-interest-preprocessor` on the ECR / ECS of the `aws-paper` environment: ECR repository / Dockerfile inspection / local build / ECR push / ECS Cluster·Role·Log Group / single Preprocessor run / separation of Crawler outbound risk.**

- First-round target environment: `aws-paper`, region `ap-northeast-2`. The integrated cutover of `aws-live` with the 10 spec is out of scope for this spec.
- Inputs: [`./requirements.md`](./requirements.md) R1 ~ R11, [`../02-aws-network-and-rds`](../02-aws-network-and-rds) (VPC / Subnet / SG / VPC Endpoint / RDS), [`../06-secrets-and-iam`](../06-secrets-and-iam) (Secrets / SSM / Role policy), [`../03-marketconnector-ec2`](../03-marketconnector-ec2) (EC2 → ECS operational pattern §13).
- The deliverables of this 08 initial-document phase are limited to the 3 files requirements.md / design.md / tasks.md. runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md are authored separately after operator execution (consistent with R11).

This document does not record actual secret values, account-id, RDS endpoint hostname, image digest, actual ARNs, instance-id, or IAM access key id in plaintext. It uses only `[REDACTED]` or placeholders (`<account-id>`, `<region>`, `<ecr-repo-uri>`, `<image-tag>`, `<task-arn>`, `<rds-endpoint>`, `<cluster-name>`) (consistent with R10.4).

## Runtime Role Split at a Glance

The runtime boundary of the hybrid execution model is determined by whether GUI is required. For detailed rationale, see §12 (hybrid classification) / §13 (SSM automation) / §15 (rev7 non-GUI operational path + Autologon) / §17 (Step 2 success-judgment strengthening).

| Workload | Runtime | Entrypoint / trigger | Success judgment |
|---|---|---|---|
| Preprocessor MS | ECS Fargate Task (single-run) | `aws ecs run-task` once | exit code 0 / CloudWatch Logs `PREPROCESSOR PIPELINE END` / feature `updated_at` refreshed |
| non-GUI crawler | ECS Fargate Task (`portfolio-paper-interest-crawler:7`, for daily operations) | `aws ecs run-task` once (inside wrapper Step 2) | exit code 0 / 8 non-GUI steps SUCCESS / raw table `max(trade_date)` consistent |
| KRX GUI crawler | Windows EC2 worker (Autologon → Administrator console → Scheduled Task) | SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` | Running→Ready return + Last Result 0 + latest log tail + KRX raw DB validation exit code 0 |
| ECS Task Definition rev6 (reference history) | ECS Fargate (smoke only) | Not an operational entrypoint | State retained as of the 2026-06-13 §5 pass point / not an operational target |

**Entrypoint principles**

- SSM direct Python / wrapper execution is unsuitable for the SYSTEM Session 0. Adoption rejected (§13.2 / §15.5).
- SSM RunCommand serves only the `schtasks /Run` trigger role.
- Scheduled Task trigger success ≠ Step 2 SUCCESS. All 6 success conditions (§17.1) must pass.
- Headless / non-interactive KRX collection is excluded from the operational method based on local validation (§15.6).
- Autologon is a security exception limited to the paper-only Windows worker (R-SEC-009 / §12 · §15.4).

## 1. Scope / Out of Scope (R1)

### 1.1 In Scope / Out of Scope

| Category | Item |
|------|------|
| In scope | Criteria for creating 2 ECR repositories / Dockerfile inspection / local image build (preprocessor first) / ECR push / preparation of ECS Cluster·Task Role·Task Execution Role·Log Group / single-run validation of the Preprocessor ECS Task / separate management of Crawler outbound·Selenium risk |
| Out of scope | Always-on ECS Service / EventBridge Scheduler / Step Functions automatic startup / aws-live application (10 spec) / GitHub Actions OIDC·CI/CD Role (07 spec) / modification of the 8 MS README·AGENTS.md·source·`requirements.txt`·Dockerfile / 100% guarantee of Crawler Selenium·Chrome operational stabilization |

### 1.2 Limitation of this phase's deliverables (R11 basis)

The deliverables of this 08 initial-document phase are limited to the 3 files requirements.md / design.md / tasks.md. runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md are authored separately after operator execution. The actual update of [`../_common/operator-decisions.md`](../_common/operator-decisions.md) / [`../_common/risk-register.md`](../_common/risk-register.md) / [`../_common/followups-overview.md`](../_common/followups-overview.md) is the responsibility of a subsequent phase; this phase performs candidate identification only.

## 2. ECR Repository (R2)

### 2.1 Repository matrix

| Repository name | region | URI placeholder | Target MS | image scan on push |
|----------------|--------|----------------|--------|--------------------|
| `portfolio-interest-crawler` | `<region>` (`ap-northeast-2`) | `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:<image-tag>` | `port-interest-crawler` (Crawler MS) | enabled (recommended) |
| `portfolio-interest-preprocessor` | `<region>` (`ap-northeast-2`) | `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-preprocessor:<image-tag>` | `port-interest-preprocessor` (Preprocessor MS) | enabled (recommended) |

The actual account-id is not recorded in plaintext anywhere in this design (consistent with R10.4). The actual repository creation is a direct operator task and is out of scope for this spec.

### 2.2 Common base image separation

The two MS may share a common Python base image / Selenium base image, but whether to separate a common base image repository is out of scope for this spec and is deferred to a subsequent review (R2.4 basis).

### 2.3 Environment non-separation policy (R2.5 basis)

The ECR repository is not separated per paper / live environment. The same image artifact is not pushed to duplicate repositories per environment, and the paper / live distinction is handled in the following 6 items: (1) image tag, (2) ECS Task Definition, (3) Secrets Manager / SSM Parameter Store path, (4) IAM Task Role, (5) environment variables, (6) RDS / broker configuration.

## 3. Dockerfile inspection (R3)

The work of this spec does not directly modify the Dockerfile / source / `requirements.txt` of the two MS. If a missing Dockerfile or an entrypoint defect is found, it is separated as the responsibility of a subsequent spec or operator stage (consistent with R3.4).

### 3.1 Inspection items — Preprocessor MS

| Inspection item | Expected value / inspection method |
|----------|--------------------|
| Dockerfile existence | 1 `Dockerfile` at the repo root |
| base image | Python 3.x slim family recommended. The actual tag decision is the operator's |
| requirements install | `pip install -r requirements.txt` or an equivalent method |
| entrypoint / CMD | Specify the preprocessor execution entrypoint (e.g., `python pre_daily.py`) |
| RDS env compatibility | Compatible with injecting the `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD` environment variables |
| Selenium / Chrome dependency | **Not required** (no external API / Selenium used) |

### 3.2 Inspection items — Crawler MS

| Inspection item | Expected value / inspection method |
|----------|--------------------|
| Dockerfile existence | 1 `Dockerfile` at the repo root |
| base image | Python 3.x slim or selenium-capable family |
| requirements install | `pip install -r requirements.txt` or an equivalent method |
| entrypoint / CMD | Specify the crawler execution entrypoint |
| Chrome / chromedriver install | First-round confirmation of whether an install step exists inside the Dockerfile when Selenium is depended on |
| RDS env compatibility | Compatible with injecting the same 5 environment variable keys (for raw loading) |

## 4. Local image build (R4)

### 4.1 Build order / policy

| Order | Target | Policy |
|------|------|------|
| 1 | Preprocessor MS | Build first. On build success, proceed to the ECR push stage |
| 2 | Crawler MS | Proceed after the Preprocessor build succeeds. **Do not block the Preprocessor flow on failure** (consistent with R4.3) |

Only build success / failure / whether an image id exists are recorded in subsequent deliverables. The build log stdout / stderr body is not quoted in plaintext (consistent with R4.4).

### 4.2 Build failure cause candidates

| # | Cause candidate | First-round inspection location |
|---|----------|--------------|
| 1 | `requirements.txt` compatibility (package conflict / missing build wheel) | `pip install` stage log |
| 2 | Python version mismatch (3.9 / 3.10 / 3.11, etc.) | base image tag |
| 3 | import path / missing module | entrypoint execution stage |
| 4 | Insufficient system packages (`build-essential`, `libpq-dev`, etc.) | `apt-get install` stage of the Dockerfile |
| 5 | Selenium / Chrome / chromedriver install failure of the Crawler MS | Limited to the Crawler Dockerfile. Unrelated to Preprocessor |

## 5. ECR Push (R5)

### 5.1 Tag / order policy

| Item | Policy |
|------|------|
| image tag format | Placeholder `<image-tag>` in the form `paper-<yyyymmdd>` or `paper-latest`. The actual tag decision is a direct operator stage |
| push order | (1) Preprocessor → (2) Crawler |
| digest confirmation | On push success, confirm `sha256:...`. The actual value must not be recorded in plaintext in this design / subsequent deliverables. Use only the placeholder `<image-digest>` |

### 5.2 image tag strategy (R5.5 basis)

| Stage | tag format | Note |
|------|---------|------|
| aws-paper first-round validation | `paper-<yyyymmdd>` / `paper-latest` | Used at the time of this spec |
| aws-live application | `live-<yyyymmdd>` / `live-latest` | After the 10 spec cutover |
| CI/CD maturity stage | `git-<sha>` may be added | After a subsequent spec (07 OIDC) |

The push target URI uses only the placeholders `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:<image-tag>`, `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-preprocessor:<image-tag>`.

### 5.3 Push failure cause candidates

| # | Cause candidate | First-round inspection location |
|---|----------|--------------|
| 1 | ECR login token expired (`aws ecr get-login-password` reissue needed) | `docker login` response |
| 2 | Task Execution Role / operator IAM permission missing (`ecr:PutImage`, etc.) | IAM policy |
| 3 | repository not created | ECR Console / `aws ecr describe-repositories` |
| 4 | Docker daemon not started / local environment defect | `docker info` |
| 5 | region mismatch (other than `ap-northeast-2`) | login URI / push target URI |

## 6. ECS Cluster / Role / Log Group preparation (R6)

### 6.1 Infrastructure deliverable matrix

| Kind | Count | Name placeholder | Policy |
|------|------|-----------------|------|
| ECS Cluster (Fargate) | 1 | `<cluster-name>` | aws-paper single. New-creation basis at the time of this spec |
| Task Execution Role | 1 | `portfolio-paper-ecs-task-execution-role` | ECR pull / CloudWatch Logs write / Secrets·SSM read permissions |
| Task Role — Crawler | 1 | `portfolio-paper-crawler-task-role` | Limited to `/portfolio/paper/crawler/*` Secrets·SSM read |
| Task Role — Preprocessor | 1 | `portfolio-paper-preprocessor-task-role` | Limited to `/portfolio/paper/preprocessor/*` Secrets·SSM read |
| CloudWatch Log Group | 2 | `/portfolio/paper/crawler`, `/portfolio/paper/preprocessor` | Pre-created by the operator. Arbitrary log group creation by the application is prohibited |

The actual Role / Cluster / Log Group creation is a direct operator task and is out of scope for this spec (consistent with R10.1).

### 6.2 Task Execution Role — permission skeleton

| Statement | Action candidate | Resource scope |
|-----------|-------------|--------------|
| ECR Pull | `ecr:GetAuthorizationToken`, `ecr:BatchGetImage`, `ecr:GetDownloadUrlForLayer` | Limited to the repository ARN (`<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:<image-tag>`, `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-preprocessor:<image-tag>`) |
| CloudWatch Logs Write | `logs:CreateLogStream`, `logs:PutLogEvents`, `logs:DescribeLogStreams` | Limited to the `/portfolio/paper/crawler*`, `/portfolio/paper/preprocessor*` log group ARN |
| Secrets / SSM Read | `secretsmanager:GetSecretValue`, `ssm:GetParameters` | Limited to the service prefix Resource ARN |

### 6.3 wildcard prohibition policy (consistent with 03 §13, R6.5 basis)

| Pattern | Policy |
|------|------|
| Resource `*` | **Prohibited** |
| Action wildcard (`secretsmanager:*`, `ssm:*`, `ecs:*`, `Action: "*"`) | **Prohibited** |
| service prefix separation | Grant only each independent prefix to crawler / preprocessor. Granting a different service prefix is prohibited |

### 6.4 Task Execution Role / Task Role responsibility separation (R6.6 basis)

The Secrets Manager / SSM read permission for injecting the Task Definition `secrets` field is held by the **Task Execution Role**. The permission for the application runtime to query Secrets Manager / SSM Parameter Store directly via the AWS SDK is held by the **Task Role**. This first-round validation preferentially uses Task Definition `secrets` field injection.

## 7. Preprocessor single-run validation (R7)

### 7.1 Task execution form

| Item | Decision value |
|------|--------|
| networkMode | `awsvpc` |
| Subnet placement | public subnet (02 spec `public-a` or `public-b`) |
| `assignPublicIp` | `ENABLED` (NAT-free outbound) |
| Execution method | `aws ecs run-task` once, single-run. No ECS Service / EventBridge / Step Functions |
| Out of scope for this spec | Always-on Service / periodic Scheduler startup (consistent with R7.3) |

### 7.2 Validation items

| # | Inspection item | Expected result |
|---|----------|----------|
| 1 | RDS connection based on `preprocessor_app` | Success (private endpoint, `sg-preprocessor-task` → `sg-rds-postgres` 5432 passing) |
| 2 | CloudWatch Logs output | Stream created inside the `/portfolio/paper/preprocessor` log group / body output |
| 3 | Task exit code | `0` |
| 4 | image pull / Secret injection | Task Execution Role log / environment variable injection normal |

### 7.3 RDS environment variable mapping (cited from 06 / 03 §6)

| Secret path | JSON key | environment variable key |
|------------|----------|-------------|
| `/portfolio/paper/rds/preprocessor-app` | `host` | `INTEREST_DB_HOST` |
| same as above | `port` | `INTEREST_DB_PORT` |
| same as above | `dbname` | `INTEREST_DB_NAME` |
| same as above | `username` | `INTEREST_DB_USER` |
| same as above | `password` | `INTEREST_DB_PASSWORD` |

The actual RDS endpoint hostname / password must not be recorded in plaintext anywhere in this design. Use only the placeholders `<rds-endpoint>` / `[REDACTED]`.

### 7.4 Execution failure cause candidates

| # | Cause candidate | First-round inspection location |
|---|----------|--------------|
| 1 | Environment variable injection missing (Task Definition `secrets` field defect) | Task Definition JSON / CloudWatch Logs |
| 2 | Secret read permission missing (Task Role or Task Execution Role) | IAM policy / Task event message |
| 3 | RDS SG inbound not allowed (`sg-preprocessor-task` not registered) | RDS SG inbound rule |
| 4 | VPC Endpoint missing or public subnet placement missing | Subnet / Route Table / VPC Endpoint |
| 5 | image entrypoint defect / requirements not installed | Regress to the image build stage |

## 8. Separate management of Crawler external outbound risk (R8)

At the time of this spec, the Crawler performs only the first-round review (Dockerfile / whether it can build / whether outbound is reachable). A 100% guarantee of operational stabilization is out of scope for this spec, and when a Selenium / Chrome dependency defect is found, it does not block the Preprocessor validation flow (consistent with R8.3 / R8.4).

### 8.1 External dependency / outbound domain candidates

| Dependency / domain | Use | First-round review |
|--------------|------|---------|
| Selenium / Chrome / chromedriver | Dynamic page collection | Confirm only whether an install step exists inside the Dockerfile. Stability validation is subsequent |
| `data.krx.co.kr`, `open.krx.co.kr` | KRX OTP / quote / flow / short selling | First-round confirmation of DNS / 443 outbound reachability |
| `finance.naver.com` | Naver quote / news / report | same as above |
| `query1.finance.yahoo.com` | yfinance API | same as above |

### 8.2 Risk candidates (separate management)

| ID candidate | Risk | Handling in this spec |
|---------|-------|-------------|
| R-CRAWL-XXX | KRX login / OTP expiration / rate limit | First-round review only. Operational stabilization is a subsequent spec |
| R-CRAWL-XXX | Selenium / Chrome abnormal termination / headless compatibility | same as above |
| R-CRAWL-XXX | Naver / yfinance response schema change | same as above |
| R-CRAWL-XXX | Outbound reachability failure in the NAT-free environment (public subnet IP blocked) | Re-confirm the §9 NAT-free policy |

## 9. NAT-free policy (R9)

### 9.1 Policy matrix

| Item | Policy |
|------|------|
| NAT Gateway | **Prohibited** (block cost incurrence) |
| This spec's ECS Fargate Task outbound | 100% public subnet + `assignPublicIp = ENABLED` |
| External API outbound (KRX / Naver / yfinance / holiday) | same as above |
| Handling when a NAT Gateway is found | Arbitrary decision by this spec is prohibited. Handle only by an operator decision after re-confirming [`../_common/operator-decisions.md`](../_common/operator-decisions.md) **OD-NET-001** / **OD-NET-002** (consistent with R9.3) |

## 10. EC2 → ECS operational pattern handover (03 spec §13 input)

### 10.1 Mapping table (cited from 03 §13.1 + filled in for crawler / preprocessor)

| Area | EC2 (03 spec) | ECS Fargate (this spec, 08) |
|------|--------------|--------------------------|
| Credentials | Instance Role + IMDSv2 | Task Role (crawler / preprocessor separated) |
| secret / parameter read | Environment variable injection (`/tmp/inject-env.sh` or systemd `EnvironmentFile=`) | Task Definition `secrets` field + Task Role |
| Logs | CloudWatch Logs Agent or `PutLogEvents` | awslogs driver → `/portfolio/paper/crawler`, `/portfolio/paper/preprocessor` |
| Access / debugging | SSM Session Manager | ECS Exec |
| Automatic startup | systemd unit or startup script | **Out of scope for this spec** (Service / EventBridge / Step Functions subsequent spec) |

### 10.2 service prefix separation (consistent with 03 §13.2)

| MS | service prefix |
|----|---------------|
| Crawler | `/portfolio/paper/crawler/*` |
| Preprocessor | `/portfolio/paper/preprocessor/*` |

A subsequent spec does not change the principles of prohibiting granting a different service prefix Resource / prohibiting Resource wildcard / prohibiting Action wildcard / using only the env prefix `paper` / `live` / not using an Access Key (IMDSv2 + Role only) (consistent with 03 §13.3).

## 11. Safety constraints (R10, R11)

### 11.1 Policy by area (R10)

| Area | Policy |
|------|------|
| Actual AWS resources | The creation·modification·deletion of ECR repository / ECS Cluster / Task Definition / Service / IAM Role / Policy / CloudWatch Log Group / Secrets / SSM Parameter / RDS is **only by direct operator work**. Direct execution is prohibited in all phases of this spec |
| 8 MS code / docs / packaging | The README / AGENTS.md / CHANGELOG / docs / worklog / source / `requirements.txt` / `Dockerfile` / `setup.py` / `pyproject.toml` of `port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research` are not modified |
| External calls | 0 calls to KRX / Naver / yfinance / Selenium / Chrome / KIS API |
| Crawling / orders | 0 crawls. 0 buy / sell / cancel / modify |
| RDS DDL/DML | 0. Even query-only SELECT is 0 at the time of this phase |
| secret / identifier notation | The actual secret value, password, KIS app key·app secret, account number, token, RDS endpoint hostname, account-id, actual ARN, image digest, IAM access key id, instance-id must not be recorded in plaintext. Only `[REDACTED]` or a placeholder |
| `GetSecretValue` call | Performed by the operator only. Kiro automatic validation uses only `secretsmanager:DescribeSecret` metadata (consistent with 06 §11) |

### 11.2 phase separation (R11)

The deliverables of this 08 initial-document phase are limited to the 3 files requirements.md / design.md / tasks.md. runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md are authored separately after operator execution.

| Deliverable | Handling in this phase |
|--------|--------------|
| `requirements.md` / `design.md` / `tasks.md` | Limited to the 3 deliverables of this 08 initial-document phase |
| `runbook.md` / `validation-checklist.md` / `operation-notes.md` | Authored separately after operator execution |
| `CHANGELOG.md` / `WORKLOG.md` (`docs/worklog/YYYY-MM-DD.md`) | Authored separately after operator execution |
| `_common/*.md` update | Actual update is prohibited in this phase. Only candidates are identified in a subsequent phase |

This call-unit policy: this call updates only design.md, and requirements.md / tasks.md are separated into a separate call responsibility.

## 12. 2026-06-12 operator validation result / Hybrid execution model

This section is a supplement reflecting the first-round validation result of the 2026-06-12 Windows EC2 worker-based KRX GUI-dependent collection. It does not fully rewrite the existing ECS-centric design; it additionally specifies only the classification decision and Secrets Manager integration method updated from the validation result. For detailed operator execution results, see the 2026-06-12 section of [`./operation-notes.md`](./operation-notes.md).

### 12.1 Hybrid execution model classification

The crawler / preprocessor runtime of this spec is not a single ECS Fargate structure; it separates roles as follows.

| Workload | runtime | First-round operational-ready state | Note |
|---------|---------|-------------------|------|
| Preprocessor MS (`port-interest-preprocessor`) | ECS Fargate Task (NAT-free public subnet + `assignPublicIp=ENABLED`) | Reached 2026-06-10 | §7 kept as is |
| non-GUI crawler (Naver / yfinance / KRX non-GUI path candidate) | ECS Fargate Task candidate kept | Not reached (carried over) | runtime validation / no Selenium used / outbound reachability validation subsequent |
| KRX GUI-dependent crawler (KRX program / KRX shortsell, Selenium / Chrome) | Windows EC2 worker | Reached 2026-06-12 (first-round operational-ready / full automation is subsequent) | wrapper-based manual execution |

### 12.2 Reason the KRX GUI-dependent collection was separated to the EC2 worker

| Reason | Explanation |
|------|------|
| Chrome GUI / Download dependency | After KRX OTP, the CSV download drops into the Chrome download folder. The download path is OS user / session dependent |
| Login session maintenance | After KRX login, the OTP / session token is statefully coupled to the browser context |
| Ease of debug attach / operator inspection | RDP entry allows immediate inspection of the GUI state / download folder / Chrome devtools |
| KRX site characteristics | Possibility of insufficient headless stability due to non-standard JavaScript / dynamic elements / OTP, etc. |
| ECS Fargate Task lacks GUI / Display support | Fargate effectively has no GUI / X11 / Display support. Compatibility burden with the KRX OTP / Chrome download flow |

### 12.3 Secrets Manager integration method (reflected 2026-06-12)

| Secret path | Use | JSON key | First-round use time | Note |
|-------------|------|---------|--------------|------|
| `/portfolio/paper/rds/preprocessor-app` | preprocessor RDS connection | `host` / `port` / `dbname` / `username` / `password` | 2026-06-10 | §7.3 |
| `/portfolio/paper/rds/crawler-app` | RDS connection of the EC2 worker (crawler_app role) | `host` / `port` / `dbname` / `username` / `password` | 2026-06-12 | read from EC2 IAM Role `portfolio-paper-crawler-worker-role` |
| `/portfolio/paper/krx/crawler-login` | KRX login credential | `username` / `password` | 2026-06-12 | Added `secretsmanager:DescribeSecret` / `secretsmanager:GetSecretValue` to the EC2 IAM Role inline policy (Resource-limited / 0 wildcards) |

The actual value / RDS endpoint hostname / KRX login password / actual ARN / account-id of the above Secrets must not be recorded in plaintext anywhere in this design (consistent with R10.4 / R-DOCS-001). The KRX login ID / password is noted only as "injected from Secrets Manager".

### 12.4 EC2 worker operational mode separation

| Mode | Explanation | At the time of this spec |
|------|------|-------------|
| Manual execution (wrapper-based) | After the operator connects via RDP, `run_krx_worker_daily.ps1` is run once. venv activate / DB Secret / KRX Secret / download path junction / KRX login / KRX program / KRX shortsell / log saving are handled as a single flow | **First-round operational-ready state (reached 2026-06-12)** |
| Automatic execution (SSM RunCommand + EventBridge Scheduler + optional Step Functions hybrid orchestration) | Unattended execution of the EC2 worker. Out of scope for this spec / separated subsequently | Not reached |
| Idle cost reduction | Specify the EC2 stop procedure after work ends. Out of scope for this spec / separated subsequently | Not reached |

### 12.5 Update principles of this section

- The existing §1 ~ §11 decision values (ECR repository / Dockerfile inspection / local build / ECR push / ECS Cluster·Role·Log Group / Preprocessor single run / NAT-free policy / safety constraints) are not changed.
- The ECS single-conversion assumption of the crawler-related §3.2 / §4 / §5 is supplemented by the §12.1 hybrid execution model classification (not a full rewrite).
- When the non-GUI crawler is operated as an ECS Fargate Task, the §7 preprocessor single-run validation pattern is reused as is.
- Even while the KRX GUI-dependent collection is operated on the EC2 worker, the NAT-free policy (§9) applies equally to the EC2 worker's outbound path (public subnet + EIP or an equivalent method / operator decision).
- The subsequent spec / subsequent phase responsibility separation principle is kept as in §11.

## 13. 2026-06-13 operator validation result / Hybrid execution model first-round automation completed

This section is a supplement reflecting the first-round validation result of the 2026-06-13 SSM RunCommand automation + the Selenium / Chrome / outbound smoke of ECS crawler revision 6. The §12 hybrid execution model classification is kept as is, and only the first-round automation structure of the KRX GUI path and the meaning of the ECS crawler smoke are additionally specified. For detailed operator execution results, see the 2026-06-13 section of [`./operation-notes.md`](./operation-notes.md).

### 13.1 KRX GUI path first-round automation structure

| Stage | Component | Decision value |
|------|----------|--------|
| Trigger | EventBridge Scheduler | **Separated subsequently** (currently an operator or SSM manual trigger) |
| Entrypoint | SSM RunCommand (`AWS-RunPowerShellScript`) | Used as the first-round automation entrypoint |
| Trigger command | `schtasks /Run /TN Portfolio-KRX-Worker-Daily` | Performs only the Scheduled Task one-time run trigger, not direct wrapper execution |
| Execution container | Windows Scheduled Task `Portfolio-KRX-Worker-Daily` | Runs in the Administrator interactive session. SYSTEM Session 0 use is prohibited |
| Execution script | `C:\portfolio\run_krx_worker_daily.ps1` | venv activate / RDS Secret injection / KRX Secret injection / download path junction check / sequential run of `interest_krx_login_new.py` → `interest_program.py` → `interest_shortsell.py` / log saving |
| Log location | `C:\portfolio\logs\krx_worker_daily_yyyyMMdd_HHmmss.log` | The full file body must not be quoted in plaintext in this design / operation-notes |

This first-round automation structure is in a first-round validation-complete state in the paper environment (reached 2026-06-13), and the EventBridge Scheduler periodic trigger linkage is separated subsequently.

### 13.2 Judgment that direct SYSTEM Session 0 execution is unsuitable

| Item | Result |
|------|------|
| Execution context of SSM RunCommand | SessionId 0 / `nt authority\system` |
| RDP user session | SessionId 2 (Administrator) |
| Chrome process | When run directly in SessionId 0, the KRX login stage may fail due to separation of the GUI / Display / Chrome download folder / OTP session context |
| Decision | The method where SSM RunCommand directly runs the wrapper in SYSTEM Session 0 is judged **unsuitable for KRX GUI login**. The direct-execution method is not adopted |
| Workaround | SSM RunCommand handles only the `schtasks /Run` trigger, and the actual wrapper execution is handled by the Scheduled Task inside the Administrator interactive session |

### 13.3 Meaning of ECS crawler Task Definition revision 6

| Item | Decision value |
|------|--------|
| family | `portfolio-paper-interest-crawler` |
| revision | 1 ~ 6 (latest revision = 6) |
| image | `portfolio-interest-crawler:paper-20260611` |
| cpu / memory | 1024 / 2048 |
| network mode | `awsvpc` |
| Task Role | `portfolio-paper-crawler-task-role` |
| Task Execution Role | `portfolio-paper-ecs-task-execution-role` |
| Log Group | `/portfolio/paper/crawler` |
| Log stream prefix | `ecs-selenium-chrome-smoke` |
| Meaning of `command` | **Not the actual daily crawler entrypoint, but for Selenium / Chrome / outbound smoke validation** |
| Operational Task Definition separation | **Separated subsequently** (the actual daily crawler's Task Definition / image / command is separated into a separate revision or separate family — task 58) |

The RunTask smoke result of revision 6 (2026-06-13) first-round validates the following.

- ECS / Fargate Selenium 4.40.0 / Chromium / chromedriver runtime operation
- outbound reachability based on public-a / public-b subnet + `sgroup-crawler-tasks` SG + `assignPublicIp = ENABLED`
- `example.com` HTTPS reachability / Naver Finance page loading (TITLE `Npay 증권` confirmed)
- exitCode 0 / `SELENIUM CHROME SMOKE SUCCESS` / `DRIVER QUIT` / `SELENIUM CHROME SMOKE END`

Therefore, revision 6 is interpreted only as a smoke guarantee of ECS / Fargate / Chromium runtime availability. The operational stabilization of the actual daily crawler is out of scope for this spec, and confirming the non-GUI crawler inventory and separating the operational Task Definition (task 58) are subsequent spec / subsequent phase responsibilities.

### 13.4 Hybrid execution model first-round completion judgment

| Workload | Execution location | First-round operational-ready state |
|---------|----------|-------------------|
| Preprocessor MS | ECS Fargate Task | Reached 2026-06-10 |
| KRX GUI-dependent crawler (KRX program / KRX shortsell) | Windows EC2 worker (Scheduled Task + wrapper) | Reached 2026-06-12 |
| KRX GUI path automation trigger | SSM RunCommand → `schtasks /Run` | Reached 2026-06-13 |
| non-GUI crawler runtime availability (Selenium / Chrome / outbound) | ECS Fargate Task (smoke revision 6) | First-round passed 2026-06-13 |
| EventBridge Scheduler / Step Functions periodic trigger | — | Not reached (separated subsequently) |
| EC2 worker unattended stop / cost reduction | — | Not reached (separated subsequently) |

The first-round completion judgment of the hybrid execution model at this point is based on the first-round validation passing of the above 6 dimensions + reaching stage 1 of the automation trigger. The EventBridge Scheduler periodic trigger / Step Functions hybrid orchestration / separation of the non-GUI crawler's operational Task Definition / automatic DB validation output within the wrapper / EC2 worker stop procedure are all subsequent spec / subsequent phase responsibilities.

### 13.5 Update principles of this section

- The existing §1 ~ §12 decision values are not changed.
- §13 is a supplement that updates the `Automatic execution (SSM RunCommand + EventBridge Scheduler + optional Step Functions hybrid orchestration)` row of the §12.4 EC2 worker operational mode separation table to the state of having reached the first-round automation entrypoint (SSM RunCommand → Scheduled Task trigger).
- The EventBridge Scheduler periodic trigger linkage is out of scope for this spec, and the subsequent separation principle (§11 / §12.5) is kept as is.
- The actual operational Task Definition separation of the non-GUI crawler is distinguished as subsequent work different from this §13's smoke result (revision 6) (task 58).

## Testing Strategy (reference)

This spec is an ECR / ECS / IAM / procedure-document deliverable, and there is no code / pure function / algorithm whose behavior changes with input variation. Therefore property-based testing (PBT) does not apply, and this design does not include a Correctness Properties section (same policy as the 03 spec Testing Strategy).

The validation of this spec is performed only in the following two forms, both of which are subsequent phase responsibilities.

- **Static inspection**: ECR repository existence / repository scan setting / image tag·digest existence / Task Definition `secrets` mapping / Task Role·Task Execution Role permission skeleton / Log Group existence / NAT Gateway absence / wildcard absence / Access Key absence. Responsibility of validation-checklist.md (subsequent phase).
- **Single-run validation**: Preprocessor ECS Task once `aws ecs run-task` → RDS connection success / CloudWatch Logs output / exit code 0. Responsibility of runbook.md (subsequent phase). Crawler outbound validation is out of scope for this spec and is the responsibility of a separate phase or subsequent spec.

## 14. 2026-06-15 operator validation result / Interest Crawler status re-judgment

This section is a supplement reflecting the first-round inspection result of the Backend AWS E2E dry-run performed directly by the operator on 2026-06-15. It does not change the §12 Hybrid execution model classification and the §13 first-round automation completion judgment themselves; it corrects the crawler runtime status expression of this spec from "fully complete" to "hybrid first-round / partially complete". For detailed operator execution results, see the 2026-06-15 section of [`./operation-notes.md`](./operation-notes.md) / for this date's decision lock, see OD-MS-020 / OD-MS-021 of [`../_common/operator-decisions.md`](../_common/operator-decisions.md).

### 14.1 Expression correction (consistent with OD-MS-020)

The existing expression could be interpreted as the whole Interest Crawler being complete by bundling the operational-ready reach of the KRX GUI worker and the ECS / Fargate Selenium Chrome smoke pass, so it is corrected.

| Existing expression | Corrected expression |
|----------|----------|
| Interest Crawler complete: done | Interest Crawler hybrid first-round implementation: partially complete |
| Interest Crawler is first-round complete based on the hybrid execution model | KRX GUI worker is first-round complete in an operational-ready state / ECS · Fargate crawler is smoke-validation complete / the non-GUI daily raw collection operational path and full raw freshness validation are subsequent |

This expression correction follows the following principles.

- The "complete" notation is used only when actual data loading / freshness validation is confirmed.
- Do not confuse KRX GUI worker completion with full Interest Crawler completion.
- Separate smoke success from daily raw freshness success.

### 14.2 Per-workload status on this date (2026-06-15)

The §12.1 hybrid execution model classification table is updated to this date's status (the classification itself unchanged).

| Workload | runtime | This date's status | Note |
|---------|---------|-------------|------|
| Preprocessor MS (`port-interest-preprocessor`) | ECS Fargate Task | Single RunTask success on this date (exitCode 0 / `updated_at` refreshed) — data freshness constraint | §7 kept as is |
| KRX GUI-dependent crawler (KRX program / KRX shortsell, Selenium / Chrome) | Windows EC2 worker | wrapper re-run success on this date — `[Collected Date] None` idempotent / `interest_program_raw` · `interest_shortsell_raw` latest date 2026-06-12 | KRX trading day normal (loaded up to the prior trading day as of Monday 2026-06-15) |
| non-GUI crawler (Naver / yfinance / KRX non-GUI path candidate) | ECS Fargate Task candidate kept | **Operational execution not reached** — the Selenium Chrome smoke (2026-06-13 §13.3 revision 6) passed, but the actual daily raw collection operational path / Task Definition / command separation / outbound reachability validation is incomplete | task 58 / task 72 subsequent separation |

### 14.3 non-GUI crawler inventory classification (operational path separation subsequent)

This date's first-round classification is as follows. The actual operational Task Definition / command separation is subsequent to task 58 / task 72.

- ECS Fargate candidates (high likelihood of non-GUI): `interest_news.py` / `interest_agency.py` / `interest_foreignindex.py` / `interest_commodity.py` / `interest_macroeconomic.py` / `interest_price.py` / `interest_investorflow.py` / `interest_marketbreadth.py`
- ECS Fargate excluded (KRX GUI-dependent or separately separated): `interest_krx_login_new.py` / `interest_program.py` / `interest_shortsell.py` / `interest_ticker_value.py`

The above classification is re-inspected as a 1:1 mapping at the time of the operational Task Definition separation. `interest_crawler_daily.py` itself calls both KRX GUI-dependent files and non-GUI files simultaneously, so it is excluded from the ECS Fargate standalone-execution target (consistent with 2026-06-13 §13.3).

### 14.4 Insufficient raw freshness validation and downstream impact

Result of this date's SQL inspection (as of 2026-06-15):

- `interest_program_raw` / `interest_shortsell_raw` latest date = 2026-06-12 (KRX GUI worker path normal)
- non-GUI raw 7 types (`interest_agency_raw` / `interest_news_raw` / `interest_commodity_raw` / `interest_foreignindex_raw` / `interest_investorflow_raw` / `interest_marketbreadth_raw` / `interest_price_raw`) — not loaded up to the prior trading day
- `interest_ticker_value_raw` latest date = 2026-03-09 (excluded from this dry-run's core blocking factor / separated subsequently)

In this state, even if the preprocessor ECS RunTask succeeds, new feature date generation is limited (consistent with this date's §4). In the Backend AWS E2E dry-run flow, raw freshness recovery is a precondition before moving on to `BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL`, and it acts as a stale data input risk in a subsequent spec (04 / 09) (consistent with R-DATA-009 / R-DATA-010).

### 14.5 Backend AWS E2E dry-run entry consistency

The §12 / §13 hybrid execution model first-round completion judgment is kept as is, but the result of organizing this date's backend AWS execution status based on the View Daily Batch 17-step order is as follows (for the detailed table, see 2026-06-15 §6 of [`./operation-notes.md`](./operation-notes.md)).

| Category | This date's status |
|------|-------------|
| Complete | #1 `CONNECTOR_BALANCE` / #3 `PREPROCESSOR` (data freshness constraint) |
| Partially complete / promoted to follow-up | #2 `INTEREST_CRAWLER` |
| Not progressed | #4 ~ #7 (`BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL`) |
| Not progressed / dry-run skip planned | #8 ~ #17 (`DAILY_BUY_EXECUTION` / `DAILY_SELL_EXECUTION` / `DAILY_AUTO_SELL` / `DAILY_AUTO_BUY` / `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` / `CONNECTOR_ORDER_CHECK` / `SYNC_SELL_FILL` / `SYNC_BUY_FILL` / `SYNC_BUY_POSITION` / `BALANCE_REFRESH`) |

This table is used as input for the OD-MS-021 decision lock. The actual BUY / SELL / `--execute` / fill·position sync automatic retry / aws-live work are all 0 (consistent with §11.1 / OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 ~ R-AUTO-011).

### 14.6 Update principles of this section

- The existing §1 ~ §13 decision values are not changed.
- §14 adds only expression correction / this date's status update / explicit statement of non-GUI operational non-reach / explicit statement of the impact of insufficient raw freshness on top of the §12 hybrid execution model classification and the §13 first-round automation completion judgment.
- The actual operational Task Definition separation of the non-GUI crawler, the raw freshness recovery work, the preprocessor re-run, and the raw / feature freshness validation SQL automation are all subsequent spec / subsequent phase responsibilities (consistent with task 72 / 73 / 74 / 77 / 78 / 58).

## 15. 2026-06-16 operator validation result / Hybrid execution model update (Crawler data non-collection resolved + KRX EC2 automation success)

This section is a supplement reflecting the results performed directly by the operator on 2026-06-16. The §1 ~ §14 decision values are not changed; only the following are supplemented.

Work scope:

- (a) Resolution of the Crawler data non-collection cause (identification of the absence of a non-GUI-only orchestration / Task Definition)
- (b) New `interest_crawler_daily_nongui.py` + Docker rebuild + ECR push + ECS Task Definition revision 7 registration + RunTask exitCode 0
- (c) Recovery of non-GUI raw 6 types + KRX raw 2 types + news / agency 2026-06-16 loading
- (d) Windows EC2 worker Autologon bootstrap + confirmation of Administrator console session Active + SSM RunCommand → `schtasks /Run` → Scheduled Task flow re-validation

Supplement targets: the operational status expression of the hybrid execution model / the meaning of non-GUI rev7 / the first-round demonstration of the KRX GUI automatic-login operational method / the unsuitability of SSM direct Python · wrapper execution / the decision to exclude the headless · non-interactive KRX collection operational method.

Reference: [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §1 ~ §5 / decision lock [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-MS-022 + OD-MS-011 / OD-MS-015 / OD-MS-020.

### 15.1 Expression correction (consistent with OD-MS-020 / OD-MS-022)

The §14.1 corrected expression is further supplemented with this date's result. The principle that the "complete" notation is used only when actual data loading / freshness validation is confirmed is kept as is.

| Existing expression | Corrected expression |
|----------|----------|
| Interest Crawler hybrid first-round implementation: partially complete | Interest Crawler hybrid structure complete (non-GUI rev7 operational path created + RunTask success + raw freshness recovery) |
| The non-GUI daily raw collection operational path and full raw freshness validation are subsequent | Creation of the non-GUI ECS / Fargate rev7 operational path and RunTask success (all steps SUCCESS / raw freshness recovery) |
| KRX EC2 collection: planned | KRX GUI crawler = Windows EC2 worker + Autologon + Administrator interactive session + Scheduled Task + SSM trigger success |
| KRX headless long-term candidate | Excluded from the operational method based on local validation (KRX login / nos_setup / keyboard security / iframe constraints) |
| Preprocessor execution complete (data freshness constraint) | Preprocessor = reached the ECS re-run-ready state after raw input data recovery (re-run is subsequent) |

### 15.2 Per-workload status on this date (2026-06-16)

The §12.1 / §14.2 hybrid execution model classification table is updated to this date's status (the classification itself unchanged).

| Workload | runtime | This date's status | Note |
|---------|---------|-------------|------|
| Preprocessor MS (`port-interest-preprocessor`) | ECS Fargate Task | Reached the re-run-ready state after raw input data recovery / re-run is subsequent (task 77) | §7 / §14.2 kept as is |
| non-GUI crawler | ECS Fargate Task Definition revision 7 | Operational path created and RunTask success (failures 0 / exitCode 0 / about 9 min 51 sec / all steps SUCCESS) | §15.3 new |
| KRX GUI crawler (KRX program / KRX shortsell) | Windows EC2 worker + Autologon + Administrator interactive session + Scheduled Task + SSM trigger | KRX login / program / shortsell 2026-06-15 loading success on this date (`interest_program_raw` 547 → 548 / `interest_shortsell_raw` 190,554 → 190,903) | §15.4 new |

### 15.3 ECS Task Definition revision 6 / revision 7 meaning separation

| Item | revision 6 (Selenium / Chrome smoke only) | revision 7 (for non-GUI daily operations) |
|------|-----------------------------------------|---------------------------------|
| family | `portfolio-paper-interest-crawler` | `portfolio-paper-interest-crawler` |
| revision | 6 | 7 |
| image tag | `paper-20260611` | `paper-20260616-nongui` |
| `command` | Selenium Chrome smoke command | `["python", "interest_crawler_daily_nongui.py"]` |
| log stream prefix | `ecs-selenium-chrome-smoke` | `ecs-crawler-nongui-daily` |
| Meaning | smoke guarantee of ECS / Fargate / Chromium runtime availability | non-GUI 8 types (`interest_news` / `interest_agency` / `interest_foreignindex` / `interest_commodity` / `interest_macroeconomic` / `interest_price` / `interest_investorflow` / `interest_marketbreadth`) daily raw operational entrypoint |
| Whether used at operational time | Not used (smoke validation ended) | Used as the daily operational entrypoint from this date |

The RunTask smoke result of revision 6 (2026-06-13 §13.3) remains valid as is, but the daily operational Task Definition was separated to revision 7 on this date. revision 7 does not import the 3 KRX GUI-family types (`interest_krx_login_new` / `interest_program` / `interest_shortsell`), and the KRX GUI stages excluded from the ECS / Fargate standalone-execution target are separated to the §15.4 Windows EC2 worker path.

### 15.4 KRX GUI automatic-login-based operational method (consistent with OD-MS-022)

The first-round automation flow of the KRX GUI-dependent crawler (2026-06-13 §13.1) is kept as is, but with this date's Autologon bootstrap first-round demonstration, the following flow is confirmed as the operational method.

| Stage | Component | This date's status |
|------|----------|-------------|
| Precondition | Administrator automatic login via Microsoft Sysinternals Autologon / after EC2 reboot, confirm SSM Online + `query user` Administrator console session Active | First-round demonstration passed |
| Trigger | SSM RunCommand (`AWS-RunPowerShellScript`) | First-round demonstration passed |
| Trigger command | `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"` | First-round demonstration passed |
| Execution container | Windows Scheduled Task `Portfolio-KRX-Worker-Daily` (Logon Mode `Interactive only` / Run As User `Administrator`) | First-round demonstration passed |
| Execution script | `powershell.exe -ExecutionPolicy Bypass -File C:\portfolio\run_krx_worker_daily.ps1` | First-round demonstration passed |
| Child calls | `python interest_krx_login_new.py` → `python interest_program.py` → `python interest_shortsell.py` | First-round demonstration passed |
| Result | Last Result `0` / wrapper `DONE :: KRX worker daily` / `interest_program_raw` 2026-06-15 / `interest_shortsell_raw` 2026-06-15 | First-round demonstration passed |

With this date's result, the operational mode of the KRX GUI crawler completed first-round automation entry from "wrapper-based manual execution" (OD-MS-012) to "Autologon + Administrator interactive session + Scheduled Task + SSM trigger automation" (OD-MS-022). The EventBridge Scheduler periodic trigger linkage is separated subsequently as in §11 / §12.5 / §13.5.

### 15.5 Explicit statement of SSM direct Python / wrapper execution unsuitability

The §13.2 judgment that direct SYSTEM Session 0 execution is unsuitable is supplemented with this date's result.

- The method where SSM RunCommand directly runs the wrapper (`run_krx_worker_daily.ps1`) or Python (`interest_krx_login_new.py`) is **excluded from the operational method**.
- Reason — SSM RunCommand itself runs as SYSTEM and cannot handle the KRX GUI / Chrome download / nos_setup / keyboard security / iframe flow in the Session 0 / non-interactive context.
- Normal behavior — the `whoami` result of SSM RunCommand being output as `nt authority\system` is normal, and the actual KRX GUI execution context is handled by the Scheduled Task inside a separate Administrator console interactive session.

### 15.6 Explicit statement of excluding the headless / non-interactive KRX collection operational method

Due to the behavioral characteristics of the KRX site (KRX login / nos_setup / keyboard security / iframe constraints / OTP, etc.), headless or non-interactive KRX collection is judged to fall short of operational stability based on local validation up to this date.

- Headless KRX collection: excluded from the operational method based on local validation
- Non-interactive KRX collection: excluded from the operational method
- This decision is used, consistent with OD-MS-022, to unify the KRX GUI crawler operational mode of this spec. It can be re-examined in the long term according to KRX site changes / changes in local validation results — at this point, it is not separated subsequently and only the exclusion-from-operational-method decision is locked.

### 15.7 Backend AWS E2E dry-run entry consistency

The §14.5 table is updated to this date's status (the 17-step order and safety criteria are kept as in OD-MS-021).

| Category | This date's status |
|------|-------------|
| Complete | #1 `CONNECTOR_BALANCE` / #2 `INTEREST_CRAWLER` (Crawler data non-collection resolution complete / hybrid structure complete) |
| Reached the Preprocessor re-run-ready state / re-run is subsequent | #3 `PREPROCESSOR` |
| Subsequent resumption planned | #4 ~ #7 (`BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL`) |
| Not progressed / dry-run skip planned | #8 ~ #17 (`DAILY_BUY_EXECUTION` / `DAILY_SELL_EXECUTION` / `DAILY_AUTO_SELL` / `DAILY_AUTO_BUY` / `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` / `CONNECTOR_ORDER_CHECK` / `SYNC_SELL_FILL` / `SYNC_BUY_FILL` / `SYNC_BUY_POSITION` / `BALANCE_REFRESH`) |

The actual BUY / SELL / `--execute` / fill · position sync automatic retry / aws-live work are all 0 (consistent with §11.1 / OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 ~ R-AUTO-011 / OD-MS-021).

### 15.8 Update principles of this section

- The existing §1 ~ §14 decision values are not changed.
- §15 adds only the expression update reflecting this date's first-round demonstration result (non-GUI rev7 operational path creation + RunTask success + raw freshness recovery + demonstration of the KRX EC2 automatic-login-based operational method) and the decision lock (OD-MS-022) on top of §12 / §13 / §14.
- The Preprocessor ECS re-run / Backend AWS E2E dry-run resumption (`BACKTEST_RESEARCH` → `BACKTEST_REPORT` → `DAILY_BUY_SIGNAL` → `DAILY_POSITION_SIGNAL` order) / follow-up inspection of `interest_foreignindex_raw` HANGSENG · NIKKEI225 · SHANGHAI NULL Data / EventBridge Scheduler periodic trigger / Step Functions hybrid orchestration / automatic addition of DB validation output within the wrapper / EC2 worker stop procedure / Chrome process cleanup option / View implementation are all subsequent spec / subsequent phase responsibilities.

## 16. 2026-06-20 operator validation result / Daily AWS Paper Wrapper final inspection + EC2 lifecycle follow-up necessity

This section is a supplement reflecting the results performed directly by the operator on 2026-06-20: (a) final inspection of the Daily AWS Paper Wrapper structure / safety criteria / EC2 startup criteria, (b) safe halt of the wrapper re-run attempt for the RunDate `2026-06-18` / Step 1 ~ Step 11 scope, (c) inspection of the 6/19 KRX raw freshness recovery status. It does not change the §12 ~ §15 decision values; it additionally specifies only the EC2 lifecycle follow-up necessity and the basis for entering Step 2 wrapper success-judgment strengthening. For detailed operator execution results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-20 §1 ~ §5.

### 16.1 EC2 startup criteria (per-Step dependency)

Specifies the Step 2 INTEREST_CRAWLER responsibility scope of this spec and the EC2 dependency of adjacent steps.

| Step | Dependent EC2 | This date's operational memo |
|------|----------|------------------|
| Step 1 `CONNECTOR_BALANCE` | MarketConnector EC2 | `/tmp/inject-env.sh` may be lost after EC2 stop · start / first-round demonstration on this date (passed after regeneration). 03 spec responsibility |
| Step 2 `INTEREST_CRAWLER` | (a) non-GUI ECS Fargate Task / (b) Crawler Worker EC2 (KRX GUI) | non-GUI has no EC2 dependency / KRX GUI requires the Crawler Worker EC2 in `running` state + an Administrator interactive session |
| Step 12 / 13 / 17 | MarketConnector EC2 | broker / KIS call or BALANCE_REFRESH / 03 spec responsibility |

### 16.2 EC2 lifecycle follow-up necessity (08 spec input)

- If both EC2 are stopped, the operator must directly start them before the wrapper run / must stop them after completion (consistent with R-AUTO-016 / R-AUTO-017 mitigation).
- The automation of the MarketConnector EC2 `/tmp/inject-env.sh` loss handling / SSM Online wait / KRX worker EC2 stop procedure is out of scope for this spec / separated subsequently (consistent with task 59).
- Result of this date's 6/18 wrapper duplicate-run attempt — Step 12 not executed / 0 new broker orders / 0 new execution_plan / residual Crawler Worker chrome processes cleaned up by EC2 stop / consistent with R-AUTO-002 / R-AUTO-019 mitigation.

### 16.3 Update principles of this section

- The §12 ~ §15 decision values are not changed.
- §16 supplements only the EC2 startup criteria / EC2 lifecycle follow-up necessity / 6/18 duplicate-run attempt result.
- The KRX GUI path automatic login (OD-MS-022) / non-GUI ECS Fargate operational path (OD-MS-011) / expression correction (OD-MS-020) policies are kept as is on this date as well.

## 17. 2026-06-21 operator validation result / Step 2 INTEREST_CRAWLER success-judgment strengthening (OD-MS-026 new)

This section is a supplement reflecting the results performed directly by the operator on 2026-06-21. It does not change the §12 ~ §16 decision values; it additionally specifies only the Step 2 success conditions and the worker stopped fail-closed design change / the Step 2 guard role of the KRX raw validation script.

Work scope:

- (a) `step-02-interest-crawler.ps1` success-judgment strengthening
- (b) non-GUI ECS crawler env supplement
- (c) New `interest_krx_raw_validate_daily.py` + EC2 deployment + EC2 standalone validation
- (d) `step-02-interest-crawler.ps1` DB validation integration
- (e) crawler worker stopped fail-closed handling
- (f) Step 2 standalone execution validation

Reference: [`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1 ~ §8 / decision lock OD-MS-026 new / R-AUTO-020 new mitigation first-round demonstration / R-AUTO-007 / R-AUTO-016 / R-AUTO-017 supplement.

### 17.1 Step 2 success conditions (strengthened)

The SUCCESS condition of Step 2 `INTEREST_CRAWLER` must pass all of the following 6 (consistent with OD-MS-026 / consistent with R-AUTO-020 mitigation).

1. **non-GUI ECS crawler exitCode 0** — RunTask of Task Definition `portfolio-paper-interest-crawler:7` with lastStatus `STOPPED` / container exitCode 0 / stoppedReason `Essential container in task exited` / CloudWatch log saving confirmed.
2. **Crawler Worker EC2 running** — if the instance state is not `running`, Step 2 is fail-closed (see §17.2 below).
3. **Windows Scheduled Task `Portfolio-KRX-Worker-Daily` Running → Ready return** — trigger via `schtasks /Run` / poll for `Running` state → wait for `Ready` return / output `sawRunning` log / Step 2 fails on timeout.
4. **Last Result 0 or 0x0** — confirm Last Result after the Scheduled Task ends / do not treat as SUCCESS by Scheduled Task trigger success alone (core of R-AUTO-020 mitigation).
5. **latest worker log path / tail output** — latest file path / last write time / size / tail output of `C:\portfolio\logs\krx_worker_daily_*.log`. Confirm the `KRX login SUCCESS` / `KRX program SUCCESS` / `KRX shortsell SUCCESS` / `DONE :: KRX worker daily` labels (0 plaintext body quotes / consistent with R-DOCS-001).
6. **KRX raw DB validation pass** — `INTEREST_CRAWLER_KRX_DB_VALIDATE` SSM step → run `interest_krx_raw_validate_daily.py --expected-date <ExpectedKrxRawDate>` → validate the row_count + `max(trade_date)` of `interest_program_raw` / `interest_shortsell_raw` based on the expected trade_date / Step 2 fails on non-zero exit or row_count 0. Include `KrxDbValidationCommandId` in the step result.

### 17.2 worker stopped handling (skip → fail-closed)

| Time | Handling method | Result |
|------|-----------|------|
| ~ 2026-06-20 (previous) | If the crawler worker EC2 is not `running`, the KRX GUI Scheduled Task trigger is automatically skipped inside the wrapper | Step 2 SUCCESS is possible even in the skip state / risk that KRX raw non-loading propagates to Step 3 onward (consistent with R-AUTO-016 / R-AUTO-020) |
| 2026-06-21 (current) | If the crawler worker EC2 is not `running`, Step 2 fails immediately (fail-closed) / output instanceId / state | Block Step 2 SUCCESS entry when the KRX GUI worker · DB validation is not performed |

This change is the core design change of the R-AUTO-016 mitigation update and the R-AUTO-020 new mitigation, and in this date's standalone execution validation the worker state `running` was first-round demonstrated, so the fail-closed branch itself was not entered (`consistent with §17.4`).

### 17.3 Step 2 guard role of the KRX raw validation script

- `interest_krx_raw_validate_daily.py` is used as the Step 2 guard and is the last checkpoint of the stage before Step 2 is treated as SUCCESS.
- The validation targets are the two raw tables `interest_program_raw` / `interest_shortsell_raw`, and it confirms that the row_count based on the expected trade_date + `max(trade_date)` is at or above expected. It returns exit code 30 on failure.
- DB session consistency — user `crawler_app` / schema `interest` / search_path `interest, reference, legacy, public` (consistent with the 2026-06-21 EC2 standalone validation result).
- By placing this script at the Step 2 guard position, the limitation where the wrapper treated Step 2 as SUCCESS by seeing only the Scheduled Task trigger success (R-AUTO-007 / R-AUTO-020) is first-round blocked.

### 17.4 Step 2 standalone execution validation result (2026-06-21)

| Item | Value / result |
|------|-----------|
| RunId | `daily-aws-paper-20260621-204017` |
| Environment / RunDate / execution scope | `aws-paper` / `2026-06-20` / `-StartStep 2 -EndStep 2` |
| ExpectedKrxRawDate | `2026-06-19` |
| StepCode / Status / Runner | `INTEREST_CRAWLER` / `SUCCESS` / `ECS+SSM` |
| non-GUI ECS taskDefinition / taskId / exitCode | `portfolio-paper-interest-crawler:7` / `78979b5cbb714d0eb94f5946e15a14ce` / `0` |
| Crawler Worker EC2 state | `running` |
| KRX worker SSM commandId | `f9d82fcc-1e26-4710-87c3-1d20483b63ef` |
| Scheduled Task | elapsedSeconds=`111` / sawRunning=True / FinalStatus=`Ready` / FinalLastResult=`0` |
| latest worker log | `C:\portfolio\logs\krx_worker_daily_20260621_114154.log` |
| KRX raw DB validation SSM commandId | `2279c6d7-2da6-4317-9c10-7cc77374b317` |
| `interest_program_raw` validation | expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`1` / OK |
| `interest_shortsell_raw` validation | expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`349` / OK |
| validation exit code | `0` (stderr empty) |

### 17.5 Update principles of this section

- The §12 ~ §16 decision values are not changed.
- §17 additionally specifies only (a) the Step 2 success-condition strengthening, (b) the worker stopped handling change (skip → fail-closed), (c) the Step 2 guard role of the KRX raw validation script, and (d) the Step 2 standalone execution validation result.
- The KRX GUI path automatic login (OD-MS-022) / non-GUI ECS Fargate operational path (OD-MS-011) / expression correction (OD-MS-020) / wrapper operational policy (OD-MS-023) decisions are kept as is.
- Progression of stages after Step 3 PREPROCESSOR / EC2 lifecycle automation / Step Functions mixed orchestration / View display integration / worker log centralized collection are all kept as subsequent spec / subsequent phase responsibilities.
