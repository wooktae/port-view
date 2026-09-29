# Requirements Document — 08-interest-crawler-and-preprocessor-ecs

## Introduction

This spec confirms the procedure / validation items / failure-cause candidate authoring criteria so that the two already-operating Python MS `port-interest-crawler` and `port-interest-preprocessor` can be first-execution-validated on ECS / ECR in the `aws-paper` environment.

Core one line: create 2 ECR repositories → inspect the Dockerfile → build the local image (preprocessor first) → ECR push → prepare the ECS Cluster·Role → validate one preprocessor ECS Task run → manage the crawler's Selenium / Chrome / KRX / Naver / yfinance outbound risk separately.

Prerequisite input: [`../02-aws-network-and-rds`](../02-aws-network-and-rds) (VPC / Subnet / SG / VPC Endpoint / RDS), [`../06-secrets-and-iam`](../06-secrets-and-iam) (Secrets / SSM / Role policy), [`../03-marketconnector-ec2`](../03-marketconnector-ec2) (EC2 → ECS operation pattern mapping §13). First application environment = `aws-paper`, region `ap-northeast-2`.

The deliverables of this 08 initial-document phase are limited to the 3: [`./requirements.md`](./requirements.md) / [`./design.md`](./design.md) / [`./tasks.md`](./tasks.md). runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md are authored separately after operator execution.

This document does not record in plaintext the actual secret value, account-id, RDS endpoint, KIS app key, KIS app secret, account number, IAM access key id, actual ARN, the account-id part of the ECR repository URI, the actual image digest, the actual ARN of the ECS Cluster / Task / Service, or the instance-id. All use only `[REDACTED]` or a placeholder (`<account-id>`, `<region>`, `<ecr-repo-uri>`, `<image-tag>`, `<task-arn>`, `<rds-endpoint>`).

## Glossary

- **Crawler MS**: `port-interest-crawler`. The Python MS that collects raw data from external sources such as Naver / yfinance / KRX. May depend on Selenium / Chrome.
- **Preprocessor MS**: `port-interest-preprocessor`. The Python MS that reads the collected raw and processes it into pre feature tables. No external API / Selenium dependency. Centered on RDS read / write.
- **ECR Repository**: AWS Elastic Container Registry. At the time of this spec, a separate repository is created per MS. Names: `portfolio-interest-crawler`, `portfolio-interest-preprocessor`.
- **ECS Cluster**: The Fargate-based ECS Cluster newly created at the time of this spec. A single aws-paper one.
- **Task Execution Role**: The ECS shared execution role used for ECR pull / CloudWatch Logs write / Secret injection.
- **Task Role**: The permissions the application inside the container uses (Secrets / SSM read, SG policy for RDS connection). Separated per MS.
- **NAT-free structure**: No NAT Gateway. The ECS Fargate Task handles external outbound with public subnet + assignPublicIp.
- **Single-run validation**: A validation stage where the operator directly performs only one RunTask, without ECS Service / Step Functions automatic start.

## Role Split at a Glance (Hybrid Execution Model)

This spec combines two runtimes depending on whether GUI is required, not a single runtime. For the detailed basis, see R7 / R8 / R12 / R13 / design.md §12 ~ §15.

| Workload | Runtime | Success judgment criterion | Decision lock |
|---|---|---|---|
| Preprocessor MS (`port-interest-preprocessor`) | ECS Fargate Task (single-run) | `awsvpc` + public subnet + `assignPublicIp=ENABLED`, exit code 0, CloudWatch Logs confirmed, `preprocessor_app` RDS connection success | OD-MS-011 |
| non-GUI crawler (news / agency / foreignindex / commodity / macroeconomic / price / investorflow / marketbreadth) | ECS Fargate Task (Task Definition `portfolio-paper-interest-crawler:7` = daily operation) | RunTask exit code 0, 8 non-GUI steps SUCCESS, raw `max(trade_date)` consistency | OD-MS-011 / OD-MS-020 |
| KRX GUI crawler (`interest_program` / `interest_shortsell`) | Windows EC2 worker (Autologon + Administrator console + Scheduled Task) | SSM RunCommand → `schtasks /Run` → Running→Ready + Last Result 0 + latest worker log + KRX raw DB validation (`interest_program_raw` / `interest_shortsell_raw` `max(trade_date)` ≥ ExpectedKrxRawDate) | OD-MS-022 / OD-MS-026 |
| ECS Task Definition rev6 (smoke-only) | ECS Fargate (reference history) | Kept as-is at the Selenium Chrome smoke pass point / not an operation target | Consistent with 2026-06-13 §5 |

**Core principle** — SSM direct Python execution is rejected as unsuitable for SYSTEM Session 0. SSM handles only the `schtasks /Run` trigger role. Scheduled Task trigger success ≠ Step 2 SUCCESS.

## Requirements

### Requirement 1: This spec's scope and out-of-scope

**User Story:** As an operator, I want to explicitly receive this spec's scope and out-of-scope, so that a follow-up spec or follow-up phase can grasp the work boundary at a glance.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL specify the first application environment as `aws-paper` / `ap-northeast-2`, and specify that `aws-live` and the 10 spec integrated cutover are out of this spec's scope.
2. WHEN design.md is authored, THE design.md SHALL specify the following as in this spec's scope: 2 ECR repository creation criteria / Dockerfile inspection / local image build / ECR push / ECS Cluster·Role / preprocessor single-run validation / separate management of the crawler outbound·Selenium risk.
3. WHEN design.md is authored, THE design.md SHALL specify the following as out of this spec's scope: ECS Service / Step Functions automatic start, aws-live application (10 spec), CI/CD OIDC / GitHub Actions Role (07 spec), modifying the two MS's README / AGENTS.md / source / `requirements.txt`, 100% guarantee of crawler Selenium·Chrome operational stabilization.
4. WHEN this 08 initial-document phase proceeds, THE this phase SHALL limit the deliverables to the 3 requirements.md / design.md / tasks.md, and author runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md separately after operator execution.

### Requirement 2: ECR Repository creation criteria

**User Story:** As an operator, I want to receive the ECR repository creation criteria for the two MS, so that the image push / pull path is consistently determined.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL specify the ECR repositories as the following 2: `portfolio-interest-crawler`, `portfolio-interest-preprocessor`.
2. THE design.md SHALL specify the region of the two repositories as `<region>` (`ap-northeast-2`) and recommend enabling image scan on push.
3. THE design.md SHALL use only the repository URI notation placeholders `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:<image-tag>`, `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-preprocessor:<image-tag>`, and not record the actual account-id in plaintext.
4. WHERE the two MS may share a common base image, THE design.md SHALL specify whether to separate the common base image repository as out of this spec's scope (follow-up review).
5. THE design.md SHALL specify that the ECR repository is not separated per paper / live environment and the same image artifact is not pushed to per-environment duplicate repositories, and that the paper / live distinction is handled by the 6 items image tag / ECS Task Definition / Secrets Manager·SSM Parameter Store path / IAM Task Role / environment variables / RDS·broker settings.

### Requirement 3: Dockerfile baseline inspection

**User Story:** As an operator, I want to receive the Dockerfile baseline inspection items for the two MS, so that defects found at build / execution time can be identified in advance.

#### Acceptance Criteria

1. THE design.md SHALL specify, as inspection items, whether a Dockerfile exists inside the two MS repos, and the base image, the requirements install method, the entrypoint / CMD and the environment-variable injection method.
2. WHERE the Crawler MS needs a Selenium / Chrome dependency, THE design.md SHALL specify, as an inspection item, whether the Chrome / chromedriver install step exists in the Dockerfile.
3. THE design.md SHALL specify, as an inspection item, the RDS connection environment-variable (`INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`) injection compatibility of the Preprocessor MS Dockerfile.
4. IF the Dockerfile is absent or the entrypoint is incorrect, THEN THE design.md SHALL specify that this spec's work does not directly modify the two MS's source / Dockerfile and separates it into the responsibility of a follow-up spec or operator stage.

### Requirement 4: Local image build (preprocessor first)

**User Story:** As an operator, I want to receive the local image build procedure and priority, so that defects are blocked at the build stage before ECS execution validation.

#### Acceptance Criteria

1. THE design.md SHALL specify the local image build order as (1) Preprocessor MS → (2) Crawler MS.
2. WHEN the local build fails, THE design.md SHALL specify the following as failure-cause candidates: requirements.txt compatibility / Python version mismatch / import path / insufficient system package / the Crawler MS's Selenium·Chrome dependency.
3. THE design.md SHALL specify that on a Crawler MS build failure, the Selenium / Chrome dependency defect is separated as a Crawler-only risk and does not block the Preprocessor build.
4. THE design.md SHALL specify the policy that only the build result (success / failure / whether an image id exists) is recorded in the deliverable, and the build log stdout / stderr body is not quoted in plaintext in this spec's deliverables.

### Requirement 5: ECR Push criteria

**User Story:** As an operator, I want to receive the ECR push criteria and failure-cause candidates, so that the aws-paper image tag and digest are consistently managed.

#### Acceptance Criteria

1. THE design.md SHALL specify the tag used at ECR push as the placeholder `<image-tag>` of the form `paper-<yyyymmdd>` or `paper-latest`, and separate the actual tag decision into a direct operator stage.
2. THE design.md SHALL specify the push order as (1) Preprocessor MS push → (2) Crawler MS push (or record the failure-cause candidate).
3. WHEN push succeeds, THE design.md SHALL specify image digest (`sha256:...`) confirmation as an inspection item, but not record the actual digest value in plaintext in this document and use only a placeholder.
4. IF push fails, THEN THE design.md SHALL specify the following as failure-cause candidates: ECR login token expiration / Task Execution Role not granted / repository not created / Docker daemon not started / region mismatch.
5. THE design.md SHALL specify the image tag strategy and the push target URI notation as follows: aws-paper first-validation tag = `paper-<yyyymmdd>` / `paper-latest`, future aws-live tag = `live-<yyyymmdd>` / `live-latest`, future CI/CD maturity-stage tag = `git-<sha>` addable, push target URI = `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:<image-tag>` and `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-preprocessor:<image-tag>`.

### Requirement 6: ECS Cluster / Role / Log Group preparation

**User Story:** As an operator, I want to receive the ECS Cluster and Role / Log Group preparation criteria, so that permission / log channel omissions do not occur at Task execution time.

#### Acceptance Criteria

1. THE design.md SHALL specify the criteria for creating 1 ECS Cluster for aws-paper (name `<cluster-name>`, Fargate-based).
2. THE design.md SHALL specify 1 Task Execution Role and include the following permissions: ECR pull, CloudWatch Logs write, Secrets Manager / SSM read (injection path).
3. THE design.md SHALL specify 2 Task Roles separated per Crawler / Preprocessor, and specify that each Role has only the Secrets / SSM read permission for the service prefix (`/portfolio/paper/crawler/*`, `/portfolio/paper/preprocessor/*`) and the RDS-connection SG-pass permission.
4. THE design.md SHALL specify 2 CloudWatch Log Groups: `/portfolio/paper/crawler`, `/portfolio/paper/preprocessor`. Log group pre-creation is separated into a direct operator stage.
5. WHERE a Resource / Action wildcard (`*`) appears in a policy, THE design.md SHALL specify that it follows the 03 spec §13 wildcard prohibition policy (Resource wildcard prohibited / Action wildcard prohibited) as-is.
6. THE design.md SHALL distinguish the Secrets / SSM read permission for Task Definition `secrets` field injection as the Task Execution Role, and the AWS SDK direct-query permission of the application runtime as the Task Role, and specify that this first validation preferentially uses Task Definition `secrets` injection.

### Requirement 7: Preprocessor single Task execution validation

**User Story:** As an operator, I want to receive the ECS Task single-run validation criteria of the Preprocessor MS, so that the first-validation target at the time of this spec becomes clear.

#### Acceptance Criteria

1. THE design.md SHALL specify the Preprocessor Task's networkMode as `awsvpc`, and specify that NAT-free outbound is handled with public subnet + `assignPublicIp = ENABLED`.
2. WHEN the Preprocessor Task is executed with one RunTask, THE validation SHALL include, as inspection items, whether the RDS connection succeeds on the `preprocessor_app` basis (not `marketconnector_app`), whether CloudWatch Logs output occurs, and the Task exit code (0).
3. THE design.md SHALL specify that the Preprocessor execution at the time of this spec performs only a single run (`aws ecs run-task` once), and that ECS Service always-on operation / EventBridge Scheduler periodic start are out of this spec's scope.
4. IF the Preprocessor Task fails, THEN THE design.md SHALL specify the following as failure-cause candidates: missing environment-variable injection / missing Secret read permission / RDS SG inbound not allowed / missing VPC Endpoint / image entrypoint defect.

### Requirement 8: Separate management of the crawler Selenium / Chrome / external outbound risk

**User Story:** As an operator, I want to receive the crawler MS's Selenium / Chrome / KRX / Naver / yfinance outbound risk as a separate section, so that the Preprocessor validation and the crawler stabilization responsibility are separated.

#### Acceptance Criteria

1. THE design.md SHALL specify whether the Crawler MS needs Selenium / Chrome as a first-review item.
2. THE design.md SHALL specify that the KRX, Naver, yfinance outbound access paths are handled with public subnet + `assignPublicIp` in the NAT-free structure, and separate, as risk candidates, the possibility of KRX login / rate limit / insufficient Selenium stability.
3. THE design.md SHALL specify that a 100% guarantee of crawler operational stabilization is out of this spec's scope, and that at the time of this spec only the first review (Dockerfile / whether it can build / whether outbound is reachable) is performed.
4. WHERE a Selenium / Chrome dependency defect is found in the Crawler first review, THE design.md SHALL separate it into the responsibility of a follow-up spec or follow-up phase and specify that it does not block the Preprocessor validation flow.
5. WHERE KRX GUI-dependent collection (e.g., KRX program / KRX shortsell) is incompatible with the ECS Fargate Task's GUI / Chrome download / OTP session flow, THE design.md SHALL specify that that KRX GUI-dependent crawler can run on a Windows EC2 worker instead of an ECS Fargate Task, and specify the runtime separation (hybrid execution model) of the non-GUI crawler and the KRX GUI-dependent crawler.
6. WHEN KRX GUI collection automation enters the operation stage, THE design.md SHALL specify the following 4 conditions (consistent with the 2026-06-16 result / OD-MS-022): (a) KRX GUI collection requires a Windows interactive session / (b) SSM is used only as a Scheduled Task trigger role, not direct Python execution / (c) Autologon is a paper-only operational exception (security exception) / (d) the KRX program · shortsell completion criterion is validated by the DB max date and row count increase.
7. WHEN the non-GUI crawler's ECS Fargate operation path is separated, THE design.md SHALL specify the following 4 conditions (consistent with the 2026-06-16 result / OD-MS-011 / OD-MS-022): (a) exclude the KRX GUI-dependent modules from the ECS / Fargate Task's entrypoint / Python module import / (b) confirm the per-non-GUI-step SUCCESS log via CloudWatch Logs / (c) validate the freshness per raw table with SQL / (d) judge whether the Preprocessor can be re-run with the recovered raw input.

### Requirement 9: Enforce the NAT-free policy

**User Story:** As an operator, I want to re-enforce the NAT-free policy at the time of this spec, so that no NAT Gateway cost is incurred.

#### Acceptance Criteria

1. THE design.md SHALL prohibit NAT Gateway usage and specify that all ECS Fargate Tasks of this spec handle outbound with public subnet + `assignPublicIp = ENABLED`.
2. THE design.md SHALL specify that external API (Naver / yfinance / KRX / holiday) outbound is also handled by the same path.
3. IF a NAT Gateway is found during this spec's operation, THEN THE design.md SHALL specify that, instead of an arbitrary decision in this spec, it is handled only as an operator decision after re-confirming [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-NET-001 / OD-NET-002.

### Requirement 10: Safety constraints (work division / sensitive information / external calls)

**User Story:** As an operator, I want to explicitly receive this spec's work safety constraints, so that code / operational data / AWS resources / external calls are not changed due to this spec's work.

#### Acceptance Criteria

1. WHILE all phases of this spec are in progress, THE work SHALL not directly perform creation / change / deletion of actual AWS resources (ECR repository, ECS Cluster / Task Definition / Service, IAM Role / Policy, CloudWatch Log Group, Secrets / SSM Parameter, RDS) and handle it only as direct operator work.
2. WHILE all phases of this spec are in progress, THE work SHALL not modify the README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging files of the 8 MS (`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`).
3. WHILE all phases of this spec are in progress, THE work SHALL maintain 0 external calls (KRX / Naver / yfinance / Selenium / Chrome / KIS API), 0 crawls, 0 orders / buys / sells / cancels / modifies, 0 RDS DDL/DML.
4. WHEN this spec's deliverables handle a secret / identifier, THE deliverable SHALL not write the actual secret value / password / KIS app key / app secret / account number / token / RDS endpoint hostname / account-id / actual ARN / image digest / IAM access key id / instance-id in plaintext, and use only `[REDACTED]` or a placeholder (`<account-id>`, `<region>`, `<ecr-repo-uri>`, `<image-tag>`, `<task-arn>`, `<rds-endpoint>`).
5. WHEN handling secret query, THE deliverable SHALL specify that the `secretsmanager:GetSecretValue` call is performed only by the operator and Kiro's automatic validation uses only `secretsmanager:DescribeSecret` metadata (consistent with 06 spec §11).

### Requirement 11: Limit this spec's initial-document phase deliverables

**User Story:** As an operator, I want to receive a limited scope of this spec's initial-document phase deliverables, so that the responsibility boundary between the follow-up phase and the operator execution stage becomes clear.

#### Acceptance Criteria

1. WHILE this 08 initial-document phase is in progress, THE work SHALL limit the deliverables to the 3 requirements.md / design.md / tasks.md.
2. WHILE this 08 initial-document phase is in progress, THE work SHALL not author runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md (or `docs/worklog/YYYY-MM-DD.md`) in this phase and separate it into separate authoring after operator execution.
3. WHILE each phase call is in progress, THE work SHALL update only the single-phase document of that call (requirements call → requirements.md, design call → design.md, tasks call → tasks.md) and separate the remaining phase documents into the responsibility of a follow-up call.
4. WHILE this phase is in progress, THE work SHALL not perform the actual update of [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/risk-register.md`](../_common/risk-register.md), [`../_common/followups-overview.md`](../_common/followups-overview.md), and separate it so that only the update candidates are identified in the follow-up phase (design / tasks).


## 2026-06-16 Reinforcement — Strengthen the KRX GUI collection / non-GUI crawler requirements

This section specifies, as reinforcement memos, additional acceptance criteria for R8 (separate management of the crawler Selenium / Chrome / external outbound risk) and R6 / R7 (ECS Cluster / Role / Log Group / Preprocessor single-run), using the 2026-06-16 operator validation result (`./operation-notes.md` 2026-06-16 §1 ~ §6) as input. The existing R1 ~ R11 decision values (SHALL / SHALL NOT) are not changed, and this section reinforces only the validation criteria arising from the hybrid execution model first-automation entry point and the new creation of the non-GUI operation path. The decision lock is consistent with OD-MS-022 (new) + OD-MS-011 / OD-MS-015 / OD-MS-020 first-demonstration memo reinforcement.

### Requirement 12: KRX GUI collection operation method (R8 reinforcement)

**User Story:** As an operator, I want to receive the operation-method criteria for KRX GUI-dependent collection, so that unsuitable methods such as SSM direct execution / Headless collection do not wrongly enter operation.

#### Acceptance Criteria

1. WHEN KRX GUI-dependent collection (KRX program / KRX shortsell) enters the operation method, THE design.md SHALL specify that an **Administrator console interactive session** on the Windows EC2 worker is a required condition.
2. THE design.md SHALL limit the way SSM RunCommand is used in the KRX GUI path to only the **`schtasks /Run` trigger role**, and specify that the way SSM RunCommand directly executes the wrapper / Python in a SYSTEM Session 0 / non-interactive session is **excluded from the operation method** (consistent with 2026-06-13 §13.2 / 2026-06-16 §15.3).
3. WHERE an Administrator console interactive session is absent, THE operation method SHALL specify using the Administrator console session automatically created after EC2 boot via Autologon bootstrap, and specify that Autologon usage is a paper-only Windows worker-limited security exception (consistent with R-SEC-009).
4. WHEN handling the completion criterion of the KRX GUI collection result, THE validation SHALL pass the SQL check that inspects the DB max date and row count increase together (e.g., `interest_program_raw` max date = previous trade day / row count increase = number of business days, `interest_shortsell_raw` max date = previous trade day / row count increase = number of business days × number of tickers).
5. THE design.md SHALL specify that Headless / non-interactive KRX collection is excluded from local validation / excluded from the operation method (consistent with 2026-06-16 §15.3), and specify that the "long-term candidate" wording is no longer used per this date's correction.

### Requirement 13: non-GUI Interest Crawler operation method (R8 reinforcement)

**User Story:** As an operator, I want to receive the ECS / Fargate operation-method criteria for the non-GUI Interest Crawler, so that the smoke validation and the daily operation Task Definition are separated and raw freshness validation is not missed.

#### Acceptance Criteria

1. WHEN the non-GUI crawler runs as daily operation on ECS / Fargate, THE Task Definition SHALL **exclude** the KRX GUI-dependent imports (`interest_krx_login_new` / `interest_program` / `interest_shortsell`) (consistent with 2026-06-16 §15.2).
2. THE Task Definition SHALL be able to confirm the **SUCCESS message in CloudWatch Logs** or an equivalent step-termination signal per non-GUI step (`interest_news` / `interest_agency` / `interest_foreignindex` / `interest_commodity` / `interest_macroeconomic` / `interest_price` / `interest_investorflow` / `interest_marketbreadth`).
3. THE validation SHALL **separately specify** the ECS / Fargate smoke-only revision (e.g., revision 6 / log stream prefix `ecs-selenium-chrome-smoke`) and the daily operation revision (e.g., revision 7 / log stream prefix `ecs-crawler-nongui-daily`), and not conclude daily operation completion from a smoke pass alone (consistent with R-DATA-009 mitigation).
4. THE validation SHALL confirm with SQL whether each raw table's max date matches the previous trade day (or is within the trade-day N±1 range), and check together whether the row count increase matches number of business days × number of tickers / number of business days × number of indices / number of business days × number of categories.
5. WHEN the raw freshness validation passes, THE validation SHALL judge together whether the Preprocessor ECS RunTask can be input (loaded up to the previous trade day + max date match + new row creation confirmed) (consistent with R-DATA-010 mitigation).

### Requirement 14: hybrid execution model auto-login-based operation-method security (R10 reinforcement)

**User Story:** As an operator, I want to receive the auto-login security-exception criteria for the paper-only Windows worker in the hybrid execution model, so that the auto-login credentials are not recorded in plaintext anywhere in the operator notes / spec deliverables / chat / CloudWatch Logs.

#### Acceptance Criteria

1. WHEN handling the Autologon credentials, THE deliverable SHALL not write DefaultUserName / DefaultPassword / Administrator password in plaintext and use only `[REDACTED]` or a placeholder.
2. THE design.md SHALL classify Autologon usage as a **paper-only Windows worker-limited security exception** and specify that it is not the general operating-environment standard (consistent with R-SEC-009).
3. THE design.md SHALL also specify the **RDP inbound restriction** (no 0.0.0.0/0 / prefer a single operator IP or SSM Session Manager / consistent with OD-NET-009) of the EC2 to which Autologon applies.
4. THE validation SHALL check together whether the Administrator console session is Active in the `query user` result, whether the SSM managed instance is Online, and the Last Result code of the Scheduled Task.
5. WHEN the EC2 worker work ends, THE follow-up work SHALL specify, as follow-up handoff, the EC2 stop procedure (idle cost reduction) / Chrome process cleanup option / a future dedicated local user review candidate (consistent with R-AUTO-017 / R-SEC-009 mitigation).


## Addendum (2026-06-16) — NAT-free policy reinforcement (R9 reinforcement)

This addendum specifies the application scope of R9 (enforce the NAT-free policy) based on the 2026-06-16 operator validation result. Since the KRX GUI collection and non-GUI crawler requirements reinforcement was already covered in §Requirement 12 / §Requirement 13 (the sections above), this addendum keeps only the R9 (NAT-free) item. The R1 ~ R11 body is not changed. For detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-16 § / decision lock [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-MS-022 + OD-NET-001 / OD-NET-002.

#### Acceptance Criteria reinforcement (R9)

1. THE design.md SHALL specify that the non-GUI crawler ECS Task Definition revision 7 handles NAT-free outbound with public subnet + `assignPublicIp = ENABLED` (consistent with the 2026-06-16 RunTask result / 0 NAT Gateways maintained).
2. THE design.md SHALL specify that the outbound of the Windows EC2 worker (KRX GUI crawler) also follows the NAT-free policy (public subnet + EIP or an equivalent method / consistent with OD-NET-001 / OD-NET-002).
3. IF a NAT Gateway is found during this spec's operation, THEN THE design.md SHALL keep the R9.3 policy as-is that, instead of an arbitrary decision in this spec, it is handled only as an operator decision after re-confirming [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-NET-001 / OD-NET-002.



## 2026-06-21 Reinforcement — Strengthen the Step 2 INTEREST_CRAWLER success judgment

This section specifies, as reinforcement memos, additional acceptance criteria for R8 / R12 / R13 (KRX GUI collection operation method / non-GUI Interest Crawler operation method), using the 2026-06-21 operator validation result (`./operation-notes.md` 2026-06-21 §1 ~ §8) as input. The existing R1 ~ R13 decision values (SHALL / SHALL NOT) are not changed, and this section reinforces only the validation criteria arising from the wrapper Step 2 success condition, the worker stopped fail-closed design change, and the introduction of the KRX raw DB validation guard. The decision lock is consistent with OD-MS-026 (new) + OD-MS-022 / OD-MS-023. New R-AUTO-020 + R-AUTO-007 / R-AUTO-016 / R-AUTO-017 mitigation·detection reinforcement.

### Requirement 15: Strengthen the wrapper Step 2 success judgment (R8 / R12 / R13 reinforcement)

**User Story:** As an operator, I want to receive the wrapper Step 2 success judgment criteria, so that the risk of Step 2 being marked SUCCESS on the Scheduled Task trigger success alone and KRX raw non-loading propagating to the Step 3 and later flow is blocked.

#### Acceptance Criteria

1. WHEN wrapper Step 2 (`INTEREST_CRAWLER`) runs, THE wrapper SHALL not mark Step 2 as SUCCESS on the Scheduled Task trigger success alone (consistent with OD-MS-026 / R-AUTO-020 mitigation).
2. WHEN wrapper Step 2 runs, THE wrapper SHALL mark Step 2 SUCCESS only when all of the following 6 conditions pass — (a) non-GUI ECS crawler exitCode 0, (b) Crawler Worker EC2 `running`, (c) Windows Scheduled Task `Running` → `Ready` return (`sawRunning` log output + Step 2 failure on timeout), (d) Last Result 0 or 0x0, (e) latest worker log path / last write time / size / tail output, (f) KRX raw DB validation pass (`interest_program_raw` / `interest_shortsell_raw` row_count + `max(trade_date)` on the `ExpectedKrxRawDate` basis).
3. WHEN the Crawler Worker EC2 instance state is not `running`, THE wrapper SHALL immediately fail-closed Step 2 and print instanceId / state in the failure message (no Step 2 SUCCESS entry after skip / R-AUTO-016 mitigation update).
4. WHEN the KRX raw DB validation step of wrapper Step 2 runs, THE wrapper SHALL run `load-crawler-db-env.ps1` + `venvs/interest-crawler` venv + `interest_krx_raw_validate_daily.py --expected-date <ExpectedKrxRawDate>` inside the Windows crawler worker EC2 via the `INTEREST_CRAWLER_KRX_DB_VALIDATE` SSM step.
5. WHEN `interest_krx_raw_validate_daily.py` runs, THE script SHALL exit with exit code 30 if the row_count of `interest_program_raw` / `interest_shortsell_raw` on the expected trade_date basis is 0 or `max(trade_date)` is below the expected date.
6. WHEN the KRX raw DB validation result of wrapper Step 2 is a non-zero exit or row_count 0, THE wrapper SHALL fail Step 2 and include `KrxDbValidationCommandId` in the step result.
7. WHEN wrapper Step 2 is at the stage right before running the KRX worker, THE wrapper SHALL perform a best-effort reset of stale Chrome / chromedriver processes, and not immediately stop on a reset failure but leave only a warning log and proceed (consistent with R-AUTO-017 mitigation).
8. WHEN the KRX GUI worker of wrapper Step 2 runs, THE wrapper SHALL keep the path that triggers the `Portfolio-KRX-Worker-Daily` Scheduled Task with `schtasks /Run` (SSM direct python execution rejected / consistent with OD-MS-022 / OD-MS-026).
9. WHEN wrapper Step 2 runs a non-GUI ECS RunTask, THE wrapper SHALL inject `TEMP=/tmp` / `TMP=/tmp` / `PYTHONUTF8=1` / `PYTHONIOENCODING=utf-8` via ECS RunTask `containerOverrides.environment` (mitigate Windows / Linux encoding differences / make the temporary-file path dependency explicit).
10. THE wrapper SHALL support passing the `Invoke-DailyAwsPaperEcsTask` `EnvironmentVariables` parameter / `New-SsmParameterFile` `ExecutionTimeoutSeconds` parameter / `Invoke-SsmCommandAndWait` `ExecutionTimeoutSeconds` so it can control the SSM timeout during a long-running KRX worker · DB validation.
