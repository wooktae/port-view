# Design Document

## Overview

This design defines the **foundation structure** for migrating the 8 PORT-STRATEGY-AI MS to AWS. Actual IaC code, AWS resource creation, and code changes to the 8 MS are out of scope for this spec. This document organizes the recommended structure and decision rationale so that concrete IaC and deployment can be authored in follow-up specs (e.g., `02-aws-network-and-rds`, `03-marketconnector-ec2`).

The key decisions of this design are as follows.

- The 8 MS are mapped, by workload characteristics, to the appropriate candidate among EC2 / ECS Fargate / AWS Batch / Step Functions + EventBridge Scheduler / Lambda / Beanstalk. They are not unified onto the same compute.
- The single PostgreSQL `portfolio` DB and schema-per-domain structure are preserved as-is while moving to RDS for PostgreSQL. The existing `INTEREST_DB_*` environment variable keys and `search_path` policy are not changed.
- All secrets are externalized to Secrets Manager / SSM Parameter Store. Operational constraints such as the broker token file and broker IP registration are reflected in the EC2 + EIP recommendation.
- Observability standardizes on CloudWatch Logs / Metric / Alarm, while preserving the existing `port-view` `SlackNotificationService`; infrastructure alarms are reinforced via a separate SNS → Lambda → Slack path.
- A phased cutover roadmap is defined in 6 stages so that not all MS are moved at once.

## Architecture

### Current (Local) Architecture Summary

- The 8 MS share the PostgreSQL `portfolio` DB on the same host.
- `port-view` is the operations console, and Daily Batch calls the Python absolute paths of other MS (e.g., `C:\Workspaces\...`) via subprocess.
- The `port-marketconnector` Flask calls the KIS broker directly over outbound HTTPS and keeps the token in the single file `access_token.txt`.
- `port-interest-crawler` collects KRX/Naver via Selenium/Chrome and collects overseas data via yfinance.
- Each Python MS connects to the same DB via the `INTEREST_DB_*` environment variables, and the per-MS `search_path` priority differs.
- Slack notifications are sent by the `port-view` `SlackNotificationService` to a webhook URL.

### Target AWS Architecture (Logical)

- **Region**: A single VPC in a single region. Live and non-prod are distinguished within the same VPC by environment tags and subnet groups.
- **Networking**: public subnet (ALB, NAT Gateway, marketconnector EC2 option), private subnet (ECS Fargate Task, AWS Batch, Lambda VPC, RDS).
- **Data**: A single RDS for PostgreSQL `portfolio`. Environment separation is by instance or DB separation (the recommendation is a per-environment RDS instance).
- **Compute**:
  - `port-marketconnector`: EC2 + EIP (or NAT Gateway EIP) — to protect the broker IP policy and the single-token session.
  - `port-view`: ECS Fargate as first choice, Beanstalk as second. Daily Batch orchestration is recommended to be separated into EventBridge + Step Functions.
  - `port-interest-crawler`: the Selenium/Chrome-dependent part as an ECS Fargate Task (triggered by EventBridge Scheduler); only the yfinance/Naver REST call part is separated as a Lambda candidate.
  - `port-interest-preprocessor`: ECS Fargate Task. Only short single steps are reviewed as Lambda candidates.
  - `port_strategy_common`: no separate compute. Packaged into the other MS container images.
  - `port_strategy_decision`: ECS Fargate Task + EventBridge Scheduler.
  - `port_strategy_execution`: ECS Fargate Task + EventBridge Scheduler. The intraday monitor is a short-interval ECS Service or Step Functions Map state candidate in the paper/live environments.
  - `port_strategy_research`: AWS Batch as first choice (long backtests), ECS Fargate as second.
- **Secrets**: Secrets Manager (highly sensitive, rotation candidates), SSM Parameter Store (low-sensitivity environment-dependent values).
- **Image registry**: ECR per MS.
- **CI/CD**: GitHub Actions → ECR → ECS / EC2 deployment as first choice, CodePipeline + CodeBuild + CodeDeploy as second.
- **Observability**: CloudWatch Logs / Metrics / Alarms / EventBridge. Infrastructure alarms via SNS → Lambda → Slack webhook. Domain notifications (Daily Batch, balance, etc.) preserve the existing `SlackNotificationService`.

### Component Flow

Logical flow (text diagram):

```
[EventBridge Scheduler]
    ├─> Step Functions: daily-pipeline
    │     ├─> ECS Task: interest-crawler-daily
    │     ├─> ECS Task: interest-preprocessor-daily
    │     ├─> ECS Task: strategy-decision-daily-buy
    │     ├─> ECS Task: strategy-decision-daily-position
    │     ├─> ECS Task: strategy-execution-daily-buy/sell
    │     └─> Lambda: post-batch-summary -> port-view (or SlackNotificationService)
    └─> Step Functions: intraday-monitor
          └─> ECS Task: strategy-execution-intraday (loop with wait)

[port-view ECS Service]
    ├─> RDS PostgreSQL (read/write operations console queries + Daily Batch metadata)
    ├─> port-marketconnector EC2 (HTTP, Service Discovery or ALB internal)
    └─> SlackNotificationService -> Slack webhook (Secrets Manager)

[port-marketconnector EC2 (EIP)]
    ├─> KIS broker (outbound HTTPS, EIP registration required)
    └─> RDS PostgreSQL (connector schema priority search_path)

[Secrets Manager / SSM Parameter Store]
    └─> injected into EC2 / ECS Task / Lambda via IAM Role
```

## Per-MS Compute Candidate Comparison and Recommendation

Comparison items: workload pattern, external dependencies, compute candidates, recommendation, rationale.

### `port-marketconnector`

- Workload pattern: Flask API + time-sensitive broker orders/quotes. Requires sharing a single access token file. High likelihood the broker requires outbound IP registration.
- External dependencies: KIS API (HTTPS), RDS, no Slack.
- Candidate comparison
  - EC2 + EIP: fixed outbound IP, easy to preserve the single token file, ensures broker rate-limit singularity. Slightly higher operational burden.
  - ECS Fargate + NAT Gateway EIP: container standardization possible. The single token file must be externalized to EFS or Secrets Manager. NAT Gateway EIP limit management needed.
  - App Runner: hard to fix a dedicated outbound EIP. Not recommended.
- **Recommendation**: EC2 + EIP as first choice, ECS Fargate + NAT Gateway EIP as second.
- Rationale (reason for EC2 recommendation): broker IP registration policy, the `access_token.txt` single-session constraint, and latency stability at order time.

### `port-view`

- Workload pattern: Spring Boot 4.1 / Thymeleaf operations console. Long-running JVM. Includes Daily Batch orchestration.
- External dependencies: RDS, marketconnector HTTP, Slack webhook, and the external MS execution paths that Daily Batch calls (currently local Python paths).
- Candidate comparison
  - ECS Fargate: container standardization, easy zero-downtime deployment.
  - Elastic Beanstalk Tomcat: Spring Boot friendly. However, it moves away from container standardization.
  - App Runner: simple single-container deployment. May limit the JVM's large-memory freedom.
- **Recommendation**: ECS Fargate as first choice, Beanstalk as second.
- Daily Batch impact: the structure of directly calling external MS Python paths breaks on AWS. In this foundation, only the recommendation to move the call method to Step Functions + EventBridge + ECS RunTask is stated. The actual Daily Batch code change is a follow-up spec.

### `port-interest-crawler`

- Workload pattern: daily collection + history backfill. Selenium/Chrome dependency (KRX, some Naver). yfinance/Naver REST calls.
- External dependencies: KRX (web/Selenium), Naver, yfinance.
- Candidate comparison
  - Lambda: Selenium/Chrome packaging is possible but has limits such as concurrency, cold start, and /tmp caps. The KRX login flow is not recommended.
  - ECS Fargate Task + EventBridge Scheduler: stable Selenium/Chrome container. Recommended.
  - EC2: possible, but 24/7 is not needed. Not recommended on cost grounds.
- **Recommendation**: ECS Fargate Task as first choice. Only the yfinance/Naver REST calls can be separated to use Lambda.
- Rationale: the Selenium/Chrome dependency and the stateful nature of KRX login.

### `port-interest-preprocessor`

- Workload pattern: raw → pre feature processing. Large proportion of DB upsert. External holiday API calls.
- External dependencies: RDS, holiday API.
- Candidate comparison
  - Lambda: suitable for short steps. Timeout risk on some long-running steps.
  - ECS Fargate Task: stable for long upsert. Recommended.
- **Recommendation**: ECS Fargate Task as first choice. Consider separating only short steps such as the holiday API to Lambda.

### `port_strategy_common`

- Workload pattern: pure library. No DB / HTTP / IO.
- Candidate comparison: not a separate compute deployment target.
- **Recommendation**: package it into the other MS container images.
  - First choice: git submodule + running `pip install ./port_strategy_common` in the Dockerfile build stage.
  - Second choice: build a wheel/sdist and publish to CodeArtifact or a private S3 index.
- Rationale: since it is a pure-function core, it should not incur separate compute cost.

### `port_strategy_decision`

- Workload pattern: daily batch. daily BUY signal and daily position decision. RDS connection.
- Candidate comparison
  - ECS Fargate Task + EventBridge Scheduler: recommended.
  - AWS Batch: an over-provisioned option unless many concurrent jobs are needed.
  - Step Functions: useful when bundling a daily pipeline with the other MS.
- **Recommendation**: ECS Fargate Task as first choice. Included as a step in Step Functions.

### `port_strategy_execution`

- Workload pattern: daily + intraday. BUY/SELL execution order creation, connector calls, fill sync, position sync, intraday monitor.
- Candidate comparison
  - ECS Fargate Task + EventBridge Scheduler: recommended for daily steps.
  - intraday monitor: an ECS Service (always-on) or a Step Functions Map + Wait-based polling model.
- **Recommendation**: daily as ECS Fargate Task first choice. intraday as Step Functions + ECS RunTask polling model first choice, ECS Service second.
- Safety constraint: live BUY/SELL must not be automatically retried. The retry policy is defined in the Runbook.

### `port_strategy_research`

- Workload pattern: backtest run, analysis, text report generation. May run for a long time.
- Candidate comparison
  - AWS Batch: suitable for long-running / variable resources. Recommended.
  - ECS Fargate Task: sufficient for short backtests.
  - Step Functions: good for bundling the backtest → analysis → report flow.
- **Recommendation**: AWS Batch as first choice, ECS Fargate Task as second. Report files are recommended to be stored in an S3 bucket.

## RDS for PostgreSQL Migration Design

### Instance Configuration

- Single VPC in a single region.
- Recommendation to place a separate RDS instance per environment: `portfolio-dev`, `portfolio-paper`, `portfolio-live`.
- live: PostgreSQL 16 or higher, multi-AZ, automated backup 7~14 days, encryption at rest, performance insights on.
- paper: single-AZ allowed, automated backup 7 days.
- dev: single-AZ allowed, automated backup 1~3 days.

### Schema Policy

- Preserve the 10 schemas: `reference, interest, preprocessor, research, decision, execution, connector, ops, legacy, public`.
- Each MS's `search_path` priority is preserved exactly as defined in its README. It is not changed in this spec.

### DB Role / Permissions

- Per-MS role recommendation (example names):
  - `marketconnector_app`: read/write on `connector`, `execution`, `legacy`, `reference`, `public` + RDS read-only on others.
  - `crawler_app`: read/write on `interest`, `reference`.
  - `preprocessor_app`: read/write on `preprocessor`, read on `interest`, `reference`.
  - `decision_app`: read/write on `decision`, read on `research`, `preprocessor`, `connector`, `execution`, `reference`.
  - `execution_app`: read/write on `execution`, read on `connector`, `decision`, `research`, `reference`.
  - `research_app`: read/write on `research`, read on `preprocessor`, `interest`, `reference`.
  - `view_app`: read on all schemas + read/write on `ops`.
- Environment variable keys: preserve the existing `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`. Only the values branch per MS role.

### Data Migration

- First choice: `pg_dump` + `pg_restore` (schema-only → data) cutover method.
  - Rationale: single instance, single DB. Simplicity is prioritized over the cost of introducing AWS DMS.
- Second choice: AWS DMS (when zero-downtime becomes necessary).
- Cutover-stage outline
  1. The operator stops all MS execution (especially broker orders, fill sync, intraday monitor).
  2. Perform `pg_dump` (schema + data) against the local PostgreSQL `portfolio` DB.
  3. Perform `pg_restore` to the RDS portfolio-target.
  4. Run `search_path` / role permission verification SQL (read-only).
  5. Bring up the AWS-side MS in the paper environment.
  6. After validation passes, live cutover.

### Backup / Restore

- automated backup retention: live 14 days, paper 7 days, dev 1~3 days.
- manual snapshot: just before every cutover, and every quarter.
- point-in-time recovery: active for live, active for paper.

## Secrets / Environment Variable Design

### Item-by-Item Mapping

- Secrets Manager (highly sensitive, rotation candidates)
  - KIS app key, app secret, base URL
  - account number, account product code
  - contents of access_token.txt (or EFS path as a preferred candidate — see the separate item below)
  - RDS password (`INTEREST_DB_PASSWORD`)
  - Slack webhook URL
  - Naver API client secret
- SSM Parameter Store (low sensitivity)
  - `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `PORTFOLIO_DB_NAME`, `INTEREST_DB_USER`
  - `PORT_ACCOUNT_NO` (SecureString recommended, per operator policy)
  - `PORT_BROKER_NAME`, `PORT_ENVIRONMENT`, `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION`
  - `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT`
  - Naver API client id (standard Parameter Store possible if low sensitivity)
  - Chrome / ChromeDriver paths (fixed at container build time; may be unnecessary)

### `access_token.txt` Storage Location

- Candidate comparison
  - EFS: mountable by both EC2/ECS under the single broker session premise. Concurrent-write conflict risk is avoided operationally by guaranteeing a single instance.
  - S3: simple storage. However, latency / consistency must be considered for frequent read/write.
  - Secrets Manager: can store the token itself. However, on refresh, the Secrets API call cost / latency / rate limit must be considered.
- **Recommendation**: as long as the market connector runs as a single EC2 instance, EC2 local disk + periodic backup to S3 as first choice, EFS as second.
- Rationale: under the single-broker-session, single-instance premise, there is no need to incur the cost/latency of EFS / Secrets Manager. However, the token backup/restore procedure on EC2 replacement is required in the Runbook.

### Environment Variable Compatibility

- The existing key names (`INTEREST_DB_*`, `PORT_*`) are preserved as-is.
- On the AWS side, the ECS Task Definition or EC2 user data injects the Secrets Manager / Parameter Store values under the same key names.
- No secret is recorded with an actual value in the Dockerfile, build artifact, log, or this document. All are marked as `[REDACTED]`.

## Network Design

### VPC / Subnet

- Single VPC, multiple AZs.
- Public subnet: ALB, NAT Gateway, marketconnector EC2 (when using EIP).
- Private subnet (app): ECS Fargate Task, AWS Batch compute env, Lambda (VPC-attached).
- Private subnet (data): dedicated to the RDS subnet group.
- VPC endpoint recommendation: S3, ECR, Secrets Manager, SSM, CloudWatch Logs.

### Outbound IP Policy

- First choice: assign an EIP directly to the marketconnector EC2 and register it with the broker.
- Second choice: register the NAT Gateway EIP with the broker (but note that if many workloads share the NAT Gateway EIP, other outbound traffic is also exposed together).
- This spec recommends marketconnector as EC2 + its own EIP.

### Internal Calls

- The `port-view` → `port-marketconnector` call is an internal call within the same VPC.
- First choice: ECS Service Discovery (Cloud Map) or Internal ALB.
- Second choice: fixed private IP + Security Group whitelist.
- Operator access: no direct SSH exposure. Use SSM Session Manager.

### External Outbound Paths

- KRX, Naver, yfinance, Slack, the holiday API, and the KIS broker all require only outbound HTTPS. No MS requires external inbound exposure.
- ALB inbound, absent operator (authenticated) access and Slack/external webhook callbacks, effectively needs only operations console access. Only `port-view` is placed behind an internal ALB or an operator-only public ALB (authenticated).

## Container Images and CI/CD

### ECR Naming Convention

- `port-view`, `port-interest-crawler`, `port-interest-preprocessor`, `port-strategy-decision`, `port-strategy-execution`, `port-strategy-research`, `port-marketconnector` (kept for the container option).
- Tags: `:{git-sha}` + movable `:{env}` alias (e.g., `:dev`, `:paper`, `:live`).

### `port_strategy_common` packaging

- First choice: git submodule + `pip install ./port_strategy_common` (or `pip install -e .`) in the Dockerfile build stage.
- Second choice: build a wheel/sdist and publish to CodeArtifact or a private S3 index, then `pip install port-strategy-common==X.Y.Z` in the other MS Dockerfiles.
- This spec recommends the first choice. Introducing CodeArtifact is a follow-up spec.

### CI/CD Tooling

- First choice: GitHub Actions → ECR push → ECS RunTask / Service deployment or EC2 CodeDeploy hook.
- Second choice: CodePipeline + CodeBuild + CodeDeploy.
- This spec recommends the first choice. Rationale: the 8 MS repositories are distributed externally and GitHub workflows are simple. However, CodeBuild is kept as a reinforcing tool for when in-VPC builds are needed (e.g., an RDS migration job).

### Build-time Secret Handling

- Build-time secret injection is prohibited. Secrets are not baked into the image.
- At runtime, inject via IAM Role + Secrets Manager / Parameter Store.

### Per-Environment Promotion

- `dev` auto deploy, `paper` manual approval, `live` manual approval + condition of passing N business days of paper validation.
- The value of N is decided by the operator. This spec keeps only a placeholder.

### Deployment of EC2-Recommended MS

- When `port-marketconnector` is EC2 first choice, non-container deployment options
  - First choice: AMI baseline + systemd unit + CodeDeploy (or GitHub Actions + S3 + AWS CLI).
  - Second choice: install a docker engine on the same EC2 and run a single container (the EIP stays on the EC2).
- This spec recommends the first choice. Broker token singularity and operational simplicity are prioritized.

## Observability / Alerting / Runbook

### Logs

- log group naming convention: `/portfolio/{env}/{ms}` (e.g., `/portfolio/live/marketconnector`).
- log retention: live 90 days, paper 30 days, dev 7~14 days.
- Containers use the awslogs driver; EC2 uses the CloudWatch agent.

### Metrics / Alarm

- Standard metrics
  - CPU / Memory (infrastructure side)
  - RDS connection count, deadlocks, replica lag (excluded if absent)
  - ECS Task failure count, exit code distribution
  - EventBridge / Step Functions failure count
- Domain metrics (custom)
  - broker API error rate (marketconnector)
  - Daily Batch step success/failure count
  - intraday monitor heartbeat
  - fill sync lag (delay from fill occurrence → sync reflection)
- alarm threshold recommendation — needs fine-tuning with operator data. This spec provides only placeholder recommendations.

### Slack Alerts

- Domain notifications: preserve the existing `port-view` `SlackNotificationService`. The webhook URL is injected from Secrets Manager.
- Infrastructure alarms: CloudWatch Alarm → SNS → Lambda → Slack webhook.
  - Candidate comparison: AWS Chatbot is usable. However, Chatbot requires IAM/Slack workspace registration. A simple webhook has lower operational burden.
- **Recommendation**: infrastructure alarms via SNS → Lambda → Slack as first choice, AWS Chatbot as second.

### Daily Batch Integration

- The `strategy_daily_batch_run` and `strategy_daily_batch_step_log` tables keep their schema as-is.
- The AWS-side trigger is EventBridge Scheduler → Step Functions → ECS RunTask.
- On step failure, Step Functions failure → CloudWatch Alarm → SNS → Slack.
- The `port-view` Daily Batch screen is preserved, but the "manual run" and "re-run failed step" actions are replaced with ECS RunTask API calls in a follow-up spec.

### Runbook Items (Foundation-Stage Skeleton)

- broker token expiry / reissue
  - Symptom: KIS API authentication failure.
  - Procedure: operator manual refresh → Secrets Manager update → marketconnector restart → broker test call.
  - Automatic retry: prohibited (operator manual only).
- KIS order failure
  - Symptom: failure recorded in the `connector_order_request` status. Broker error codes vary.
  - Procedure: classify error code → resubmit only transient errors after operator approval → notify the user for permanent errors.
  - Automatic retry: allowed only for some transient errors. In the live environment, automatic retry is prohibited by default.
- RDS failover
  - Symptom: connection failure across all MS.
  - Procedure: RDS multi-AZ automatic failover, then endpoint unchanged → restart each MS connection pool → consistency check.
- Daily Batch failure re-execution
  - Symptom: Step Functions execution failure / partial step failure.
  - Procedure: identify the failed step → check whether the input is idempotent → re-run only idempotent steps → resume subsequent steps after operator approval.
- intraday monitor interruption
  - Symptom: heartbeat metric stops.
  - Procedure: check Step Functions / ECS Task state → restart → reprocessing the missed interval requires operator approval (order impact).

### Automatic Retry Policy

- Automatic retry allowed: quote lookup, idempotent preprocessing steps, holiday API, transient yfinance / Naver REST errors.
- Automatic retry prohibited: BUY / SELL orders, reflecting fill sync results, position state changes, intraday stop SELL creation.

## Environment Separation / Cost / Phased Cutover

### Environment Separation

- dev: broker live connection prohibited. Only paper or sandbox mode allowed. Separate RDS instance.
- paper: only broker paper / validation mode allowed. Actual orders prohibited. Separate RDS instance.
- live: broker live connection allowed. multi-AZ RDS, all alarms active.

### Cost Items

- EC2 (marketconnector + optional operational bastion): low ~ medium.
- ECS Fargate Task: per-execution. low if daily batch is a short run.
- RDS: low (dev/paper) ~ medium (live multi-AZ).
- NAT Gateway: hourly + data transfer cost. Could be medium. A cost optimization candidate.
- CloudWatch Logs / Metrics / Alarms: low ~ medium (depending on retention).
- Secrets Manager: secret count × monthly unit price, low.
- This spec presents only low / medium / high estimates rather than actual prices. Precise estimation is a follow-up spec.
- For the per-environment (dev / paper / live) monthly cost simulation, the comparison of whether to use NAT Gateway, and the two migration recommendation options (minimum-cost paper-validation type / operational-stability-first type), see the root common document [cost-simulation.md](../_common/cost-simulation.md).

### Phased Cutover Roadmap

- Stage 1: Network / RDS / Secrets / ECR foundation
  - Entry condition: this spec approved.
  - Exit condition: a VPC or RDS design flaw is found.
- Stage 2: `port_strategy_common` packaging + one MS pilot
  - Recommended pilot: `port-interest-preprocessor` or `port_strategy_research` (no broker impact).
  - Entry condition: Stage 1 complete, ECR / IAM Role ready.
  - Exit condition: packaging conflict, RDS permission issue.
- Stage 3: `port-marketconnector` EC2 + EIP
  - Entry condition: Stage 2 complete, broker IP registration procedure agreed, access_token Runbook agreed.
  - Exit condition: broker-side IP policy mismatch, token singularity broken.
- Stage 4: `port_strategy_decision` + `port_strategy_execution` (paper environment first)
  - Entry condition: Stage 3 passes N business days of validation (paper).
  - Exit condition: fill sync or position sync consistency mismatch in the paper environment.
- Stage 5: `port-view` operations console + Daily Batch integration (EventBridge + Step Functions)
  - Entry condition: Stage 4 paper validation passes.
  - Exit condition: Daily Batch step mapping omission.
- Stage 6: live cutover and shutdown of legacy local operation
  - Entry condition: N business days of paper-environment integrity + Runbook rehearsal complete.
  - Exit condition: broker / order / consistency issues on the first live business day.

## Safety Constraints of This Spec

- The artifacts are only the 3 files `requirements.md`, `design.md`, and `tasks.md`.
- The existing README, AGENTS.md, CHANGELOG, docs, and worklog of the 8 MS are not modified.
- The Python / Java source code of the 8 MS is not modified.
- No actual AWS resources are created. No IaC is authored in this spec either.
- No actual secret values are written in this document (all `[REDACTED]`).
- No entrypoint of the 8 MS is executed.

## Follow-up Spec Candidates

Because this spec is the foundation stage, the following follow-up spec candidates are only identified.

- `02-aws-network-and-rds`: VPC, subnet, SG, RDS instance IaC.
- `03-marketconnector-ec2`: marketconnector EC2 + EIP + access_token operational procedure + broker registration IaC.
- `04-strategy-batch-stepfunctions`: Step Functions / EventBridge / ECS RunTask design + code changes for Daily Batch and the intraday monitor.
- `05-port-view-ecs-and-runbook`: port-view ECS deployment, Runbook v1 finalization, Slack alert integration.
- `06-secrets-and-iam`: IAM Role granularity + Secrets rotation policy.
- `07-cicd-pipelines`: GitHub Actions standard workflow + ECR + deployment automation.
