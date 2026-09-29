# Design Document — 02-aws-network-and-rds

## Overview

This design finalizes the AWS network foundation and the RDS for PostgreSQL single portfolio DB structure that the 8 PORT-STRATEGY-AI MS will use. In line with the changed operator decisions, the environment model is organized into 3 (local-dev / aws-paper / aws-live), and the network uses the NAT-free strategy as the default.

Key decisions

- Single region (Seoul ap-northeast-2), single VPC.
- Environment separation: local-dev (existing local PostgreSQL, no AWS resources) / aws-paper (first AWS build) / aws-live (follow-up build after paper validation).
- 3 subnet types: public / private (app) / private (data), multi-AZ.
- NAT Gateway not used for both aws-paper / aws-live. Workloads requiring internet outbound are separated into the candidates public subnet + assignPublicIp, EC2 + EIP, or NAT Instance (auxiliary).
- VPC Endpoints enabled: ECR (api+dkr), S3 (Gateway), Secrets Manager, SSM, CloudWatch Logs. STS / KMS are optional.
- The marketconnector uses EC2 + EIP as top priority. Broker IP registration is based on the EIP.
- The aws-paper RDS starts with `db.t4g.small` single-AZ. The aws-live RDS is chosen after comparing the two options cost-saving (single-AZ) / stability-first (multi-AZ).
- The portfolio DB and 10 schemas are kept. Per-MS search_path / environment-variable key compatibility is kept.
- cutover uses `pg_dump` + `pg_restore` as top priority. AWS DMS is deferred.
- aws-live BUY/SELL / fill sync / position change / intraday stop SELL creation are automatic-retry prohibited. Only idempotent steps are allowed automatic retry.

## Architecture

### Environment Model

| Environment | Location | Role | broker connection | Automatic BUY/SELL | RDS | NAT | ALB |
|------|------|------|--------------|---------------|-----|-----|-----|
| local-dev | Operator local | Development, unit validation, source changes | Prohibited | Prohibited | Existing local PostgreSQL | N/A | N/A |
| aws-paper | AWS single VPC | Live rehearsal (KIS paper trading) | broker paper / validation mode only | Blocked initially → stepwise allowance after validation | `db.t4g.small` single-AZ, 7-day backup, PITR | Not used | Not used initially |
| aws-live | AWS single VPC | Real account operation | Allowed (automatic ordering prohibited initially) | Candidate generation + manual approval in View → limited after validation | Cost-saving option single-AZ / stability-first option multi-AZ, 14-day backup, PITR | Not used | Cost-saving option defers initially, stability-first option uses internal ALB |

### Single VPC vs prod / non-prod Separation

| Option | Cost | Operational burden | Recommendation |
|------|------|-----------|------|
| Single VPC + per-environment SG/Subnet tags | Low | Low | Top priority (current decision) |
| prod / non-prod VPC separation | Additional NAT/Endpoint cost possible, peering or separate build burden | High | Second priority. Re-review when regulatory / audit requirements arise |

### VPC Diagram (text)

```
VPC: portfolio-vpc (10.0.0.0/16, 서울 ap-northeast-2, AZ a/b)

Public Subnets
  ├─ public-a 10.0.0.0/24  (AZ a) [IGW] [marketconnector EC2 + EIP]
  └─ public-b 10.0.1.0/24  (AZ b) [(예비)]

Private (app) Subnets — NAT-free, 외부 라우트 없음
  ├─ app-a    10.0.10.0/24 (AZ a) [ECS Fargate Task]
  └─ app-b    10.0.11.0/24 (AZ b) [ECS Fargate Task]

Private (data) Subnets
  ├─ data-a   10.0.20.0/24 (AZ a) [RDS]
  └─ data-b   10.0.21.0/24 (AZ b) [RDS standby (multi-AZ 시)]

VPC Endpoints (Interface, app subnet)
  - com.amazonaws.ap-northeast-2.ecr.api
  - com.amazonaws.ap-northeast-2.ecr.dkr
  - com.amazonaws.ap-northeast-2.secretsmanager
  - com.amazonaws.ap-northeast-2.ssm
  - com.amazonaws.ap-northeast-2.logs
  (옵션) com.amazonaws.ap-northeast-2.sts
  (옵션) com.amazonaws.ap-northeast-2.kms

VPC Endpoints (Gateway)
  - com.amazonaws.ap-northeast-2.s3
```

CIDRs are recommended examples. The operator decides so there is no conflict with other in-house networks.

### Resource Placement Policy

- public-a / public-b
  - IGW
  - marketconnector EC2 (with EIP)
  - ECS Fargate Tasks requiring internet outbound (e.g., crawler / preprocessor) or ECS on EC2 (crawler-dedicated EC2)
- app-a / app-b
  - ECS Fargate Tasks not requiring external internet (port-view, decision, execution, research)
- data-a / data-b
  - RDS instance and multi-AZ standby

### Route Table Policy (NAT-free)

- public Route Table → 0.0.0.0/0 IGW
- app Route Table → no external outbound route. AWS services reachable only through VPC Endpoints.
- data Route Table → no external outbound route.

## NAT-free Strategy and Internet Outbound Options

With NAT Gateway not-used set as the default option, how to place workloads requiring internet outbound is the key choice of this spec.

### Option Comparison

| Option | Description | Cost impact | Security risk | SG control basis | Operational difficulty | Recommended use case |
|------|------|----------|------------|--------------|-------------|--------------|
| OPT-1: ECS Fargate + public subnet + assignPublicIp | Public IP auto-assigned to the Task | No NAT. Slight IPv4 usage cost | If the Task SG is misconfigured, inbound from outside is possible | Never allow inbound 0.0.0.0/0. Allow outbound only to the required external domains | Low | crawler / preprocessor / research |
| OPT-2: marketconnector EC2 + EIP | Fixed EIP on EC2, broker IP registration | EC2 unit cost + EIP attach free | If the EC2 SG is wrong, SSH/ports exposed externally | inbound only SSM access, block port 22/external ports | Medium | marketconnector top priority |
| OPT-3: crawler-dedicated EC2 or ECS on EC2 | Operate the crawler as EC2 or ECS on EC2 (public subnet) | EC2 24/7 cost | Burden of operating EC2 directly | Same as OPT-2 | Medium | When KRX login / Selenium stable operation is difficult on ECS Fargate |
| OPT-4: NAT Instance (`t4g.nano`) | Outbound from the private app subnet through a self-managed NAT EC2 | EC2 ~$3.5/month + EBS + data processing | EC2 SPOF. Burden of implementing multi-AZ HA directly | NAT EC2 SG is outbound only, inbound only from inside the VPC | High | Auxiliary for some workloads where NAT-free is difficult |

Recommendation

- marketconnector: OPT-2 (EC2 + EIP). Best fit for the broker IP registration policy.
- crawler / preprocessor / research: OPT-1 as top priority. Promote to OPT-3 if KRX login / Selenium stability is insufficient.
- For both aws-paper and aws-live, keep OPT-4 (NAT Instance) only as an auxiliary option. The default is the OPT-1 and OPT-2 combination.

### VPC Endpoint Recommendation

| Endpoint | Type | Reason needed | Enabled environments |
|----------|------|----------|-----------|
| `s3` | Gateway | ECR layer download / S3 backup / research report storage. Gateway is free. | aws-paper, aws-live |
| `ecr.api` | Interface | ECR API calls (authorization, manifest). | aws-paper, aws-live |
| `ecr.dkr` | Interface | Image layer pull. | aws-paper, aws-live |
| `secretsmanager` | Interface | Secret injection at ECS Task / EC2 startup. | aws-paper, aws-live |
| `ssm` | Interface | Operator access via SSM Session Manager, Parameter Store lookup. | aws-paper, aws-live |
| `logs` | Interface | Log shipping for ECS / EC2 / RDS (awslogs driver, CW agent). | aws-paper, aws-live |
| `sts` | Interface | Some SDK flows such as OIDC role assume. | Optional. Enable when the SDK needs to reach the STS public endpoint under NAT-free. |
| `kms` | Interface | Encryption/decryption calls when using a CMK. | Optional. Enable when a CMK is adopted. |

Cost impact (Seoul-based approximation)

- Interface Endpoint ~$8/month per AZ + data processing.
- 5 recommended types × 2 AZs ≈ ~$80/month (multi-AZ).
- Enabling in only a single AZ in aws-paper can reduce it to ~$40/month, but Endpoint availability is then vulnerable to a 1 AZ failure.
- Since not using a NAT Gateway saves ~$43~$86/month, the net effect is toward cost savings.

## Security Group Design

| SG | inbound | outbound | Notes |
|----|---------|----------|------|
| sg-marketconnector-ec2 | sg-port-view-ecs (TCP 5000 or an operator-decided port) | 0.0.0.0/0 (broker), sg-rds-postgres (5432), VPC Endpoints (443) | KIS broker outbound, EIP-based IP registration |
| sg-port-view-ecs | (No external allowance. Comes in from the ALB SG when SSM port forwarding or an internal ALB is adopted) | 0.0.0.0/0 (Slack), sg-marketconnector-ec2, sg-rds-postgres, VPC Endpoints | ECS RunTask API on Daily Batch calls |
| sg-strategy-tasks | (none) | sg-marketconnector-ec2, sg-rds-postgres, VPC Endpoints, 0.0.0.0/0 (Slack etc. if needed) | Started via RunTask from Step Functions |
| sg-crawler-tasks | (none) | 0.0.0.0/0 (KRX/Naver/yfinance), sg-rds-postgres, VPC Endpoints | Never allow inbound when placed in a public subnet |
| sg-preprocessor-tasks | (none) | 0.0.0.0/0 (holiday API), sg-rds-postgres, VPC Endpoints | |
| sg-research-batch | (none) | sg-rds-postgres, VPC Endpoints, S3 (Gateway) | Almost no internet outbound. Can be placed in the private app subnet |
| sg-rds-postgres | sg-marketconnector-ec2 (5432), sg-port-view-ecs (5432), sg-strategy-tasks (5432), sg-crawler-tasks (5432), sg-preprocessor-tasks (5432), sg-research-batch (5432) | (none) | Never allow 0.0.0.0/0 inbound |
| sg-vpc-endpoints | The app SGs above (TCP 443) | (none) | Interface Endpoint only |
| sg-port-view-alb (optional) | Operator IP allowlist or auth gate (TCP 443) | sg-port-view-ecs (HTTP) | Added when an ALB is adopted |

Operator access policy

- Prohibit EC2 SSH 22 inbound from 0.0.0.0/0.
- Use only SSM Session Manager. Grant `AmazonSSMManagedInstanceCore` to the EC2 IAM Role.
- For the operator to access the port-view console: (a) SSM port forwarding when no ALB is used, (b) internal ALB + auth gate when an ALB is adopted.

## RDS for PostgreSQL Configuration

### Per-Environment Instance

| Environment | Instance | AZ | storage | IOPS | encryption | PI | backup retention | PITR | Notes |
|------|---------|----|---------|------|------------|----|------------------|------|------|
| local-dev | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | Existing local PostgreSQL kept |
| aws-paper | db.t4g.small | single-AZ | gp3 50 GB | baseline | on (KMS default) | on | 7 days | on | First build. Single-instance downtime tolerated |
| aws-live (cost-saving option) | db.t4g.medium | single-AZ | gp3 100 GB | baseline | on (KMS default) | on | 14 days | on | On AZ failure, recovery only via PITR |
| aws-live (stability-first option) | db.t4g.medium or db.m6g.large | multi-AZ | gp3 100~200 GB | baseline + provisioned if needed | on (CMK recommended) | on | 14 days | on | Automatic failover, no-downtime minor patch |

PostgreSQL version 16 or higher recommended. minor version auto upgrade is enabled for aws-paper / aws-live.

### Parameter Group Recommendation

- `rds.force_ssl = 1` (aws-paper, aws-live)
- `log_min_duration_statement = 1000` (aws-live)
- `log_lock_waits = 1`
- `idle_in_transaction_session_timeout = 60000`
- `statement_timeout` is set in the application. The RDS parameter stays at 0.

### Subnet Group / Network

- DB subnet group: data-a + data-b (composed in a multi-AZ-capable form. Even a single-AZ instance keeps the group as two subnets).
- VPC SG: sg-rds-postgres.
- Publicly accessible: false.

### Keeping the portfolio DB and 10 schemas

- Database name: `portfolio` (identical in all environments).
- schema: `reference, interest, preprocessor, research, decision, execution, connector, ops, legacy, public`.
- Per-MS search_path is kept as defined in the README. Changing it is prohibited in this spec.

### Environment-Variable Compatibility

- Existing keys kept: `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`, `PORTFOLIO_DB_NAME`, `PORT_ACCOUNT_NO`, `PORT_BROKER_NAME`, `PORT_ENVIRONMENT`, `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION`, `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT`.
- On the AWS side, the ECS Task Definition / EC2 user data inject the RDS endpoint and secret values under the same key names. The final decision on Secrets / Parameter Store mapping is made in `06-secrets-and-iam`.
- All secrets are recorded only as `[REDACTED]`.

## DB Role Permission Matrix

### Role Definition

| Role | MS using it | Primary responsibility |
|------|---------|----------|
| `marketconnector_app` | port-marketconnector | Write broker / order / connector snapshot |
| `crawler_app` | port-interest-crawler | Write interest / reference |
| `preprocessor_app` | port-interest-preprocessor | Write preprocessor, read interest / reference |
| `decision_app` | port_strategy_decision | Write decision, read multiple schemas |
| `execution_app` | port_strategy_execution | Write execution, read connector / decision / research |
| `research_app` | port_strategy_research | Write research, read multiple schemas |
| `view_app` | port-view | READ on all schemas + WRITE on ops by default. execution write need re-reviewed in `05-port-view-ecs-and-runbook` |

### Per-Schema Permission Matrix (R = SELECT, W = INSERT/UPDATE/DELETE)

| schema | marketconnector_app | crawler_app | preprocessor_app | decision_app | execution_app | research_app | view_app |
|--------|---------------------|-------------|------------------|--------------|---------------|--------------|----------|
| reference | R | R/W | R | R | R | R | R |
| interest | - | R/W | R | - | - | R | R |
| preprocessor | - | - | R/W | R | - | R | R |
| research | - | - | - | R | R | R/W | R |
| decision | - | - | - | R/W | R | - | R |
| execution | R/W | - | - | - | R/W | - | R (write re-reviewed in 05) |
| connector | R/W | - | - | R | R | - | R |
| ops | - | - | - | - | - | - | R/W |
| legacy | R | R | - | - | - | - | R |
| public | R/W (compat) | R | R | R | R | R | R |

Permission-granting SQL patterns (examples; actual execution is out of scope for this spec):

- `GRANT USAGE ON SCHEMA <schema> TO <role>;`
- `GRANT SELECT ON ALL TABLES IN SCHEMA <schema> TO <role>;`
- `GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA <schema> TO <role>;` (only on schemas with write permission)
- `ALTER DEFAULT PRIVILEGES IN SCHEMA <schema> GRANT SELECT ON TABLES TO <role>;`

Each role password is a Secrets Manager or SSM SecureString candidate. The final decision is in `06-secrets-and-iam`. This document denotes it only as `[REDACTED]`.

The value going into `INTEREST_DB_USER` differs per MS. The environment-variable key itself is compatible, and only the value branches per role.

## Data cutover Procedure

### Decision

- Top priority: `pg_dump` + `pg_restore`. Assumes a single instance, a single DB, and a tolerable short cutover window.
- Deferred: AWS DMS. Re-reviewed in a separate spec (`10-cutover-and-validation-runbook`) when zero-downtime becomes strictly necessary.

### Stage Overview (local-dev → aws-paper)

1. The operator stops all MS (especially broker orders, fill sync, intraday monitor).
2. Run `pg_dump --format=custom --no-owner --no-privileges` against the local-dev PostgreSQL `portfolio` DB.
3. Create the 10 schemas and 7 roles in the aws-paper RDS in advance.
4. Restore the data with `pg_restore --no-owner --no-privileges --jobs=N`.
5. Run the role permission matrix SQL.
6. Run the validation SQL (next section).
7. Replace the environment variables (`INTEREST_DB_HOST`, `INTEREST_DB_PASSWORD`, etc.) with the RDS endpoint and Secrets (no code changes, only environment injection).
8. Start the MS in aws-paper → validate.
9. After paper validation passes for N business days, proceed to the aws-live cutover (separate spec 10).

### Validation SQL Candidates

```
-- schema 목록
SELECT schema_name FROM information_schema.schemata WHERE schema_name NOT LIKE 'pg_%';

-- role 목록
SELECT rolname FROM pg_roles WHERE rolname NOT LIKE 'pg_%';

-- schema별 table 수
SELECT table_schema, count(*) FROM information_schema.tables
WHERE table_schema IN ('reference','interest','preprocessor','research','decision','execution','connector','ops','legacy','public')
GROUP BY table_schema ORDER BY table_schema;

-- 핵심 테이블 row count (정합성 점검)
SELECT 'execution.strategy_execution_order'  AS t, count(*) FROM execution.strategy_execution_order  UNION ALL
SELECT 'connector.connector_order_request'   AS t, count(*) FROM connector.connector_order_request   UNION ALL
SELECT 'decision.strategy_daily_signal'      AS t, count(*) FROM decision.strategy_daily_signal      UNION ALL
SELECT 'preprocessor.pre_total_market_daily_feature' AS t, count(*) FROM preprocessor.pre_total_market_daily_feature;

-- search_path 확인 (해당 role로 접속 후)
SHOW search_path;
```

### Rollback Criteria

- If the validation SQL row count is found to differ from local-dev by anything other than ±0, stop the cutover immediately.
- If a broker call failure or a connector_order_request insert failure is detected after the application starts, immediately revert the environment variables to the local-dev endpoint.
- Do not delete the aws-paper RDS instance; preserve it as evidence. It can be reused on the next attempt.

## RDS backup / snapshot / PITR

| Policy | local-dev | aws-paper | aws-live |
|------|-----------|-----------|----------|
| automated backup | N/A | 7 days | 14 days |
| PITR | N/A | on | on |
| manual snapshot | N/A | right before cutover + quarterly | right before cutover + quarterly |
| restore procedure | N/A | snapshot → new instance → endpoint replacement | Same. Stability-first option when multi-AZ is recommended |

## Safety Mechanisms and Automatic Retry Policy

### aws-paper

- Stage 1 (order-blocking mode): The MS run normally, but connector BUY/SELL calls are blocked (ENV flag or SG stage policy). fill sync / position sync are kept running to validate the flow.
- Stage 2 (automatic BUY/SELL E2E): Turn on the paper-trading broker connection and run automatic BUY/SELL through. Validate duplicate orders / fill sync / position sync errors.
- Transition stages after an operator decision. Apply the operational stability criteria even in paper.

### aws-live

- Automatic ordering prohibited initially. Operate primarily around candidate generation + manual approval in the View.
- Review limited automation after sufficient validation (e.g., only some tickers / some time windows).
- BUY / SELL / fill sync result reflection / position change / intraday stop SELL creation are automatic-retry prohibited.
- Only idempotent steps are allowed automatic retry:
  - quote lookup
  - idempotent preprocessing steps (e.g., upsert)
  - holiday API
  - transient yfinance / Naver REST errors

### Environment Separation Safety Mechanisms

- To ensure no call to the aws-live broker occurs from aws-paper, separate the following.
  - Clearly branch the `PORT_ENVIRONMENT` environment-variable value into `paper` / `live`.
  - Separate the KIS app key / app secret into per-environment Secrets (`/portfolio/paper/...` vs `/portfolio/live/...`).
  - The SG allows inbound only to the environment-specific RDS / EC2 / ECS Task.
  - Separate the Slack webhook per environment as well (recommended). The decision is in 06.

## Cost Impact Summary

For detailed unit prices, see the root common document [`../_common/cost-simulation.md`](../_common/cost-simulation.md) and this spec's [`decision-matrix.md`](./decision-matrix.md).

- Not using a NAT Gateway saves ~$43~$86/month for both paper / live. However, VPC Endpoints accumulate at ~$8/month per AZ.
- Starting the aws-paper RDS as single-AZ saves about 50% versus multi-AZ.
- Starting the aws-live RDS as single-AZ is the cost-saving option. The stability-first option is multi-AZ.
- Not using an ALB saves ~$16.5/month. SSM port forwarding or an internal ALB is an operator decision.
- Shortening CloudWatch Logs retention saves on storage cost.
- The number of Secrets Manager items is decided in `06`. Using SSM SecureString can save some cost.

The concrete table of cost-saving option vs stability-first option is in `decision-matrix.md`.

## AWS Console Step-by-step Flow (summary)

1. Create VPC (`portfolio-vpc`, 10.0.0.0/16, 2 AZs)
2. Create Subnets (public / app / data, 2 each)
3. Create IGW + attach to VPC
4. NAT not used. No step to create a NAT Gateway / NAT Instance.
5. Create / associate Route Tables (public → IGW, app / data → no external route)
6. Create Security Groups + fill in rules
7. Create VPC Endpoints (S3 Gateway + ECR api+dkr / Secrets / SSM / Logs Interface)
8. Create RDS subnet group
9. Create RDS parameter group
10. (In advance) Register the DB master password in Secrets Manager (in collaboration with the `06-secrets-and-iam` spec)
11. Create RDS instance (aws-paper)
12. Confirm the RDS endpoint / confirm the SG connection
13. Run the SQL to create the 10 schemas / 7 roles (psql / pgAdmin via SSM)
14. Run the permission matrix SQL
15. Run the validation SQL
16. cutover pre-check (integrated cutover execution is in `10-cutover-and-validation-runbook`)

For the detailed task and validation / rollback of each step, see `tasks.md`.

## Per-MS AWS Service Candidate Comparison

For the AWS compute / orchestration / auxiliary service candidate comparison of each of the 8 MS and the portfolio-appeal augmentation plan, see the root common document [ms-aws-service-decision-matrix.md](../_common/ms-aws-service-decision-matrix.md).

Services being compared:

- Compute: EC2 / ECS Fargate / ECS on EC2 / AWS Batch / Lambda / EKS / Elastic Beanstalk / App Runner
- Orchestration: Step Functions / EventBridge Scheduler etc.

This design.md addresses only the first cutover operational-stability recommendation. The candidate comparison / Lambda review / EKS review / Beanstalk·App Runner comparison are separated into that document.

The 8 MS compute top-priority decisions (OD-MS-001 ~ OD-MS-010) are recorded in a locked state in chapter 9 of the root common [`../_common/operator-decisions.md`](../_common/operator-decisions.md).

Lambda is not recommended for any of the core batch workloads (decision / execution / research / crawler / preprocessor), and is used only for auxiliary purposes such as infra alarm fan-out / short post-processing / S3 metadata.

Recommendation basis:

- Daily Batch 16-stage measurements
  - crawler about 9 min 53 sec / preprocessor about 5 min 54 sec
  - research about 45 sec / decision · execution single-shot steps under 1 sec
- live BUY/SELL automatic-retry prohibited policy

For details, see chapters 1.4 and 5 of `../_common/ms-aws-service-decision-matrix.md`.

## Safety Constraints of This Spec

- The 4 deliverable files inside this spec (the 02 folder):
  - [`requirements.md`](./requirements.md), [`design.md`](./design.md), [`tasks.md`](./tasks.md), [`decision-matrix.md`](./decision-matrix.md)
- Root common reference documents:
  - [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md)
  - [`../_common/ms-aws-service-decision-matrix.md`](../_common/ms-aws-service-decision-matrix.md), [`../_common/cost-simulation.md`](../_common/cost-simulation.md)
- The README / AGENTS.md / CHANGELOG / docs / worklog and source code of the 8 MS are not modified.
- Actual AWS resource creation / change is not performed in this spec. This document is a procedure guide for the operator to proceed via the Console / IaC.
- The 8 MS entrypoints, and broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / order / fill / Daily Batch / intraday monitor calls are not included in this work.
- All secrets are denoted only as `[REDACTED]`.
- The final decision on the Secrets / IAM matrix is handled in the `06-secrets-and-iam` spec. This spec leaves only placeholders.
