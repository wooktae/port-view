# Requirements Document

## Introduction

This document defines the requirements for the foundation stage of migrating the 8 microservices (hereafter the 8 MS) that make up the PORT-STRATEGY-AI portfolio automated trading system to the AWS environment.

The 8 target MS are as follows.

- `port-marketconnector` (Python/Flask, KIS domestic stock API integration, orders/balance/quotes, single access token file-based session)
- `port-view` (Spring Boot 4.1 / Thymeleaf, integrated operations console, Daily Batch orchestration, Slack notifications)
- `port-interest-crawler` (Python, Naver / yfinance / KRX collection, Selenium/Chrome dependency)
- `port-interest-preprocessor` (Python, raw → pre feature processing, external holiday API)
- `port_strategy_common` (Python library, pure functional strategy core, no DB/HTTP/IO)
- `port_strategy_decision` (Python, daily BUY signal and daily position HOLD/SELL decision)
- `port_strategy_research` (Python, backtest run / analysis / text report generation)
- `port_strategy_execution` (Python, execution order creation, connector order calls, fill/position sync, intraday monitor)

All 8 MS share a single PostgreSQL `portfolio` database and operate with per-domain schemas and per-MS `search_path` priority.

- Domain schemas: `reference`, `interest`, `preprocessor`, `research`, `decision`, `execution`, `connector`, `ops`, `legacy`, `public`
- Each MS uses the `INTEREST_DB_*` environment variables and broker / Slack / external API credentials.

The artifacts of this spec are as follows.

- A comparison and recommendation of AWS deployment candidates for each of the 8 MS
- A recommended structure for the common foundation (network, RDS, Secrets, ECR, observability, CI/CD, alerting, Runbook)
- A work plan that can lead to actual IaC authoring and deployment in follow-up specs

This spec produces **only document artifacts (requirements.md, design.md, tasks.md)**. Actual AWS resource creation, code changes, and modification of existing README/AGENTS.md/docs/CHANGELOG/worklog are not performed within the scope of this spec.

## Glossary

- **Foundation stage**: The stage before actual deployment/resource creation, in which the AWS mapping/network/data/observability/CI/CD/Runbook recommended structure for the 8 MS is finalized as documents.
- **MS**: Microservice. In this system, the unit of the 8 repositories.
- **Daily Batch**: The daily pipeline that starts in `port-view`. Currently a structure that calls the Python absolute paths of external MS via subprocess.
- **Single portfolio DB / schema-per-domain**: A structure in which all MS share the same PostgreSQL database `portfolio` and resolve tables via domain schemas and per-MS `search_path`.

## Requirements

### Requirement 1: Per-MS AWS deployment candidate mapping

**Objective**: As an operator, I want to receive a comparison of AWS compute candidates suited to each MS's workload characteristics, so that I can decide on a reasonable compute choice without forced uniformity.

#### Acceptance Criteria

1. WHEN this spec is complete THEN design.md SHALL include, for each of the 8 MS, a table comparing at least 2 AWS compute candidates (e.g., EC2 / ECS Fargate / AWS Batch / Lambda / Elastic Beanstalk / App Runner / Step Functions + EventBridge Scheduler) and a recommendation.
2. WHEN evaluating the `port-marketconnector` MS THEN design.md SHALL evaluate EC2 as one of the top-tier candidates, based on the KIS broker session, the single access token file, and the possibility of a fixed outbound IP.
3. WHEN evaluating the `port-view` MS THEN design.md SHALL compare Elastic Beanstalk, ECS Fargate, and App Runner as candidates and state the impact of the current structure in which Daily Batch directly calls external MS Python paths.
4. WHEN evaluating the `port_strategy_decision`, `port_strategy_execution`, and `port_strategy_research` MS THEN design.md SHALL compare the candidates below and state a recommendation based on the execution frequency and duration of daily / intraday / backtest.
   - ECS Fargate Task
   - AWS Batch
   - Step Functions + EventBridge Scheduler
5. WHEN evaluating the `port-interest-crawler` MS THEN design.md SHALL compare Lambda, ECS Task, and EC2 candidates and state the limits of using Lambda, based on the Selenium/Chrome dependency and the KRX login flow.
6. WHEN evaluating the `port-interest-preprocessor` MS THEN design.md SHALL compare Lambda and ECS Task candidates and state a recommendation based on the holiday API calls and the possibility of long-running upsert.
7. WHEN evaluating the `port_strategy_common` MS THEN design.md SHALL state the conclusion that, given its pure-library nature, it is not a separate compute deployment target, along with a packaging recommendation to include it in the other MS container images.
8. IF a given MS is recommended for EC2 rather than a container service THEN design.md SHALL clearly state which of broker IP, session, license, or operating cost the rationale stems from.

### Requirement 2: RDS migration strategy for the single PostgreSQL `portfolio` DB

**Objective**: As an operator, I want a strategy to move to AWS RDS for PostgreSQL while preserving the single portfolio DB and schema-per-domain structure, so that the existing SQL and `search_path`-based behavior does not break.

#### Acceptance Criteria

1. WHEN design.md is authored THEN design.md SHALL include a recommendation based on a single RDS for PostgreSQL instance, and a method for separating the dev / paper / live environments.
2. WHEN addressing the schema configuration THEN design.md SHALL state that the 10 schemas `reference, interest, preprocessor, research, decision, execution, connector, ops, legacy, public` must be preserved as-is.
3. WHEN addressing each MS's DB user permissions THEN design.md SHALL include per-MS DB roles (e.g., `marketconnector_app`, `crawler_app`, ...) and a schema-level least-privilege recommendation, written in a way that preserves the existing `INTEREST_DB_USER` environment variable key.
4. WHEN addressing each MS's `search_path` policy THEN design.md SHALL state that the `search_path` order defined in the 8 MS READMEs is preserved as-is without change.
5. WHEN addressing the data migration procedure THEN design.md SHALL compare `pg_dump` / `pg_restore` and AWS DMS candidates and include a first-choice recommendation and a cutover-stage outline.
6. WHEN addressing backup and restore THEN design.md SHALL include recommendations for using RDS automated backup, manual snapshot, and point-in-time recovery, along with a retention-period recommendation.
7. IF multi-AZ is disabled for cost optimization THEN design.md SHALL recommend multi-AZ for the live environment and state that single-AZ may be allowed only for dev / paper.

### Requirement 3: Externalizing sensitive information

**Objective**: As an operator, I want to separate broker / DB / Slack / external API credentials out of code and documents, so that in all 8 MS no secret remains in source control or logs.

#### Acceptance Criteria

1. WHEN design.md is authored THEN design.md SHALL define the usage criteria for Secrets Manager and SSM Parameter Store and include a mapping table of which item is stored where.
2. WHEN addressing the KIS app key, app secret, base URL, account number, account product code, and access token file THEN design.md SHALL recommend storing them in Secrets Manager and injecting them via the IAM Role of the EC2 or ECS Task.
3. WHEN addressing the PostgreSQL connection password THEN design.md SHALL recommend storing it in Secrets Manager and mark automatic rotation as a possibility recommendation.
4. WHEN addressing the Slack webhook URL THEN design.md SHALL include a recommendation to use Secrets Manager or SSM Parameter Store SecureString.
5. WHEN addressing environment-dependent values such as the Naver API client id/secret and Chrome / ChromeDriver paths THEN design.md SHALL include a recommendation to use SSM Parameter Store.
6. WHEN addressing existing environment variable key compatibility THEN design.md SHALL state a policy of preserving the following key names as-is.
   - RDS: `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`, `PORTFOLIO_DB_NAME`
   - Broker / account: `PORT_ACCOUNT_NO`, `PORT_BROKER_NAME`
   - Environment: `PORT_ENVIRONMENT`
   - Strategy: `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION`, `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT`
7. WHEN a secret must be cited in a document THEN design.md and tasks.md SHALL use only `[REDACTED]` in every secret position and never write actual values.
8. WHEN addressing `access_token.txt` THEN design.md SHALL compare whether to keep the single-file token in EFS, S3, or Secrets Manager and state a first-choice recommendation based on the broker session singularity constraint.

### Requirement 4: Network and external access path design

**Objective**: As an operator, I want to define the VPC / Subnet / Security Group structure and external access paths such as broker, KRX, Naver, yfinance, and Slack, so that the security boundary is clear and outbound conforming to the broker IP policy is guaranteed.

#### Acceptance Criteria

1. WHEN design.md is authored THEN design.md SHALL include a single VPC, a public / private subnet distinction, a NAT Gateway or NAT Instance recommendation, and a dedicated RDS subnet group.
2. WHEN addressing the outbound IP of `port-marketconnector` THEN design.md SHALL compare the candidates of assigning an Elastic IP to the EC2 or registering the NAT Gateway EIP with the broker, and state a first-choice recommendation.
3. WHEN addressing the call path from `port-view` to `port-marketconnector` THEN design.md SHALL include a recommendation and its rationale among the candidates of same-VPC internal call, ALB routing, and Service Discovery.
4. WHEN addressing `port-interest-crawler`'s KRX / Naver / yfinance access THEN design.md SHALL state that it must be outbound-only with no inbound exposure.
5. WHEN addressing the Slack webhook call THEN design.md SHALL state that only outbound HTTPS is required and that there must be no separate inbound exposure.
6. WHEN addressing operator access THEN design.md SHALL recommend using SSM Session Manager instead of directly exposing EC2 SSH.
7. IF design.md does not recommend separating production / non-production VPCs THEN design.md SHALL state a policy of distinguishing environments within a single VPC via environment tags and subnet separation.

### Requirement 5: Container images and CI/CD pipeline

**Objective**: As an operator, I want to define consistent container image build and deployment pipelines across the 8 MS, so that the code change → build → deploy flow is standardized.

#### Acceptance Criteria

1. WHEN design.md is authored THEN design.md SHALL include the ECR repository naming convention (e.g., `port-marketconnector`, `port-view`, ...) for the MS among the 8 MS that are container-deployment targets.
2. WHEN addressing the deployment of `port_strategy_common` THEN design.md SHALL state a first-choice recommendation between being packaged via git submodule or wheel/sdist in the other MS Dockerfiles, rather than as its own container.
3. WHEN addressing CI/CD tools THEN design.md SHALL compare the CodePipeline + CodeBuild + CodeDeploy combination with the GitHub Actions + ECR + deployment script combination and state a first-choice recommendation with its rationale.
4. WHEN addressing secret handling in the build stage THEN design.md SHALL state a policy of not baking secrets into the image at build time but injecting them at runtime via the IAM Role and Secrets Manager / Parameter Store.
5. WHEN addressing per-environment deployment THEN design.md SHALL include a promotion flow for the dev / paper / live environments and a manual approval recommendation.
6. IF `port-marketconnector` or another MS is recommended on an EC2 basis THEN design.md SHALL separately state, for that MS, deployment candidates based on AMI or systemd unit + S3 / CodeDeploy rather than container deployment.

### Requirement 6: Observability, alerting, and operational Runbook

**Objective**: As an operator, I want to receive log / metric / alarm / Slack alert / Runbook standards, so that fault detection and response procedures across the 8 MS are consistent.

#### Acceptance Criteria

1. WHEN design.md is authored THEN design.md SHALL include a standard log group naming convention for collecting the logs of the 8 MS into CloudWatch Logs.
2. WHEN addressing metrics THEN design.md SHALL include core metrics such as CPU, memory, RDS connection, broker API error rate, and Daily Batch step failure, along with recommended alarm thresholds.
3. WHEN addressing Slack integration THEN design.md SHALL, while preserving the existing `port-view` `SlackNotificationService` flow, compare the candidates of CloudWatch Alarm → SNS → Lambda → Slack webhook and AWS Chatbot on the AWS side and state a first-choice recommendation.
4. WHEN addressing Daily Batch failure handling THEN design.md SHALL include a recommendation to link the `strategy_daily_batch_run` and `strategy_daily_batch_step_log` tables with EventBridge / CloudWatch Alarm.
5. WHEN addressing the operational Runbook THEN design.md SHALL include procedure items for at least 4 scenarios: broker token expiry / reissue, KIS order failure, RDS failover, Daily Batch failure re-execution, and intraday monitor interruption.
6. WHEN addressing the risks covered by the Runbook THEN design.md SHALL distinguish and state the items that require prohibiting automatic retry in the live environment (e.g., BUY/SELL orders, fill sync) from the items where safe automatic retry is possible (e.g., quote lookup, idempotent preprocessing steps).

### Requirement 7: Environment separation, cost, and phased migration roadmap

**Objective**: As an operator, I want to receive dev / paper / live environment separation, cost estimation, and a phased cutover roadmap, so that I can avoid the risk of moving all MS at once.

#### Acceptance Criteria

1. WHEN design.md is authored THEN design.md SHALL define the environment distinctions (dev, paper, live) and state whether broker live connection is allowed in each environment.
2. WHEN addressing cost THEN design.md SHALL include a candidate list of monthly cost items for EC2, ECS Fargate, RDS, NAT Gateway, CloudWatch, and Secrets Manager, but SHALL express actual exchange rates/prices only as estimate ranges (low/high).
3. WHEN addressing the phased cutover roadmap THEN design.md SHALL recommend at least the following stage order:
   - Stage 1: Network / RDS / Secrets / ECR foundation
   - Stage 2: `port_strategy_common` packaging + one MS pilot (e.g., `port-interest-preprocessor` or `port_strategy_research`)
   - Stage 3: `port-marketconnector` (including broker IP registration)
   - Stage 4: `port_strategy_decision` + `port_strategy_execution` (paper environment first)
   - Stage 5: `port-view` operations console + Daily Batch integration
   - Stage 6: live cutover and shutdown of legacy local operation
4. WHEN addressing each stage THEN design.md SHALL include entry conditions and exit (rollback) conditions.
5. WHEN addressing the live environment cutover THEN design.md SHALL recommend at least N business days of validation in the paper environment, but the value of N SHALL be left as a placeholder for the operator to decide.

### Requirement 8: Safety constraints and scope of this spec's artifacts

**Objective**: As an operator, I want to explicitly restrict this spec's work from changing code / operational data / existing documents, so that it does not conflict with the AGENTS.md work rules of the 8 MS.

#### Acceptance Criteria

1. WHEN this spec produces artifacts THEN the artifacts SHALL be limited to only `requirements.md`, `design.md`, and `tasks.md` under `C:\Workspaces\port-view\.kiro\specs\01-aws-migration-foundation\`.
2. WHILE this spec's work is in progress THE work SHALL NOT modify the existing `README.md`, `AGENTS.md`, `CHANGELOG.md`, `docs/**`, or `docs/worklog/**` files of the 8 MS.
3. WHILE this spec's work is in progress THE work SHALL NOT modify the Python / Java source code of the 8 MS.
4. WHILE this spec's work is in progress THE work SHALL NOT create or change any actual AWS resource.
5. WHILE this spec's work is in progress THE work SHALL NOT execute any entrypoint of the 8 MS (in particular, calling broker APIs, Selenium/Chrome, KRX/Naver/yfinance, RDS/PostgreSQL DDL/DML, Daily Batch, or the intraday monitor is prohibited).
6. WHEN this spec addresses a secret THE artifacts SHALL NOT include actual password, token, app key, app secret, account number, or webhook URL values, and SHALL mark them all as `[REDACTED]`.
7. WHEN tasks.md is authored THEN tasks.md SHALL divide each task into a small unit that a single operator can review and approve in a single session, and any task that entails AWS resource creation SHALL be explicitly marked "approval required".
