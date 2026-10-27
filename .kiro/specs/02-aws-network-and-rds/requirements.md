# Requirements Document — 02-aws-network-and-rds

## Introduction

This spec finalizes the AWS network foundation (VPC / Subnet / Route Table / NAT-free strategy / VPC Endpoint / Security Group) that the 8 PORT-STRATEGY-AI MS will use, along with the transition procedure for moving the single PostgreSQL `portfolio` database to RDS for PostgreSQL.

Because the operator decisions changed as of this spec, this document assumes the following environment model.

- `local-dev`: Retains the existing local PostgreSQL environment. No AWS resources. Development / unit validation is performed in local-dev.
- `aws-paper`: The first AWS build environment. A live rehearsal based on KIS paper trading. Includes RDS / ECS / EC2 marketconnector / CloudWatch / Slack / Secrets. Orders are blocked initially, followed by automatic BUY/SELL E2E validation.
- `aws-live`: A follow-up build after paper validation. Connects to a real account. Automatic ordering is prohibited initially, operating primarily around candidate generation + manual approval in the View. Limited automation is reviewed after sufficient validation.

Deliverables inside this spec (the 02 folder):

- [`requirements.md`](./requirements.md)
- [`design.md`](./design.md)
- [`tasks.md`](./tasks.md)
- [`decision-matrix.md`](./decision-matrix.md)

Root common reference documents:

- [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md)
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md)
- [`../_common/ms-aws-service-decision-matrix.md`](../_common/ms-aws-service-decision-matrix.md)
- [`../_common/cost-simulation.md`](../_common/cost-simulation.md)

This spec covers only the work of creating or referencing the documents above. Actual creation of VPC / RDS / Secrets resources, writing IaC, and modifying the 8 MS code / existing README / AGENTS.md / docs / CHANGELOG / worklog are out of scope for this spec.

Preceding spec: `01-aws-migration-foundation`.

The decisions of this spec are used as input to the following follow-up specs.

- `06-secrets-and-iam`
- `03-marketconnector-ec2`
- `08-interest-crawler-and-preprocessor-ecs`
- `04-strategy-batch-stepfunctions`
- `05-port-view-ecs-and-runbook`
- `09-strategy-research-batch`
- `10-cutover-and-validation-runbook`

## Glossary

- Single portfolio DB: The PostgreSQL database `portfolio` shared by all MS.
- schema-per-domain: 10 domain schemas (`reference, interest, preprocessor, research, decision, execution, connector, ops, legacy, public`).
- search_path policy: The schema priority order defined in each MS README. This spec does not change it.
- cutover: The transition work of moving operational data from local PostgreSQL → AWS RDS.
- NAT-free strategy: A network policy that uses neither NAT Gateway nor NAT Instance, places only workloads that require internet outbound in a public subnet or on EC2+EIP, and handles AWS service access through VPC Endpoints.

## Requirements

### Requirement 1: Single VPC and Environment Separation

**Objective**: As an operator, I want to safely separate local-dev / aws-paper / aws-live within a single VPC, so that the impact between environments is isolated while keeping cost and operational simplicity.

#### Acceptance Criteria

1. WHEN design.md is written THEN design.md SHALL specify the recommendation to place a single VPC in a single region (Seoul ap-northeast-2).
2. WHEN environment separation is addressed THEN design.md SHALL specify the 3 environment models local-dev, aws-paper, aws-live, and specify in a table that local-dev has no AWS resources.
3. WHEN RDS separation per AWS environment is addressed THEN design.md SHALL recommend a separate RDS instance for each of aws-paper and aws-live.
4. IF the operator reviews the prod / non-prod VPC separation option THEN decision-matrix.md SHALL include a comparison table of the cost / operational burden of keeping a single VPC (current recommendation) versus the VPC separation option.

### Requirement 2: Subnet and Route Table Structure (NAT-free premise)

**Objective**: As an operator, I want a public / private (app) / private (data) subnet structure and NAT-free routing, so that I can clearly define the location of workloads requiring outbound while reducing cost.

#### Acceptance Criteria

1. WHEN design.md is written THEN design.md SHALL include a 3-type subnet structure of public, private (app), private (data) on a multi-AZ basis, and a mapping of the resources that go into each subnet.
2. WHEN the Route Table policy is addressed THEN design.md SHALL specify NAT-free routing where the public Route Table uses the IGW, and the private (app) Route Table has no external route and accesses AWS services only through VPC Endpoints.
3. WHEN the RDS subnet group is addressed THEN design.md SHALL include the recommendation to compose it from private (data) subnets in at least 2 AZs.
4. WHEN workloads requiring internet outbound are addressed THEN design.md SHALL specify, per option, a policy that places such workloads in one of the candidates public subnet + assignPublicIp, EC2 + EIP, ECS on EC2, or NAT Instance.

### Requirement 3: NAT-free Strategy and VPC Endpoint Configuration

**Objective**: As an operator, I want an external access policy with NAT Gateway not used as the default option, and a VPC Endpoint configuration, so that I can control the external access path per workload while reducing cost.

#### Acceptance Criteria

1. WHEN design.md is written THEN design.md SHALL specify not using a NAT Gateway as the default option for both aws-paper and aws-live.
2. WHEN internet outbound workload options are addressed THEN design.md SHALL compare the following options:
   - ECS Fargate with public subnet + assignPublicIp enabled
   - marketconnector EC2 + EIP
   - crawler-dedicated EC2 or ECS on EC2
   - NAT Instance (`t4g.nano`)
   - The comparison items SHALL include cost, security risk, SG control, operational difficulty, and recommended use case.
3. WHEN VPC Endpoints are addressed THEN design.md SHALL recommend enabling the S3 (Gateway), ECR (api+dkr), Secrets Manager, SSM, and CloudWatch Logs Endpoints, and specify STS / KMS as optional.
4. WHEN the cost impact is addressed THEN decision-matrix.md SHALL include an estimate table of the monthly cost of the NAT-not-used + VPC Endpoint combination for each of aws-paper and aws-live.
5. IF the marketconnector requires IP registration on the broker side THEN design.md SHALL specify a policy that gives assigning an EIP directly to the marketconnector EC2 the top priority.

### Requirement 4: Security Group Design

**Objective**: As an operator, I want per-MS Security Groups with least privilege, so that only the intended inbound / outbound is allowed.

#### Acceptance Criteria

1. WHEN design.md is written THEN design.md SHALL define the following SGs: `sg-marketconnector-ec2`, `sg-port-view-ecs`, `sg-strategy-tasks`, `sg-crawler-tasks`, `sg-preprocessor-tasks`, `sg-research-batch`, `sg-rds-postgres`, `sg-vpc-endpoints`. The ALB SG is added when the ALB adoption decision is made.
2. WHEN the RDS SG is addressed THEN design.md SHALL specify a policy where `sg-rds-postgres` inbound allows only references to the app/EC2 SGs above and does not allow 0.0.0.0/0 inbound.
3. WHEN the operator access SG is addressed THEN design.md SHALL specify a recommendation that prohibits EC2 SSH 22 inbound from 0.0.0.0/0 and uses only SSM Session Manager.
4. WHEN an ECS Task placed in a public subnet is addressed THEN design.md SHALL specify a policy where that Task SG allows no inbound (an operator IP allowlist if needed) and allows only external internet for outbound.

### Requirement 5: RDS for PostgreSQL Instance Configuration (excluding local-dev)

**Objective**: As an operator, I want the aws-paper and aws-live RDS configuration and the multi-AZ decision, so that I can balance cost and availability.

#### Acceptance Criteria

1. WHEN design.md is written THEN design.md SHALL specify that local-dev retains the existing local PostgreSQL, and include in a table, for each of aws-paper and aws-live, the instance class, single-AZ vs multi-AZ, storage, IOPS, encryption at rest, performance insights, backup retention, and PITR.
2. WHEN the aws-paper RDS is addressed THEN design.md SHALL recommend starting with `db.t4g.small` single-AZ and specify retention 7 days / PITR on.
3. WHEN the aws-live RDS cost-saving option is addressed THEN decision-matrix.md SHALL include, in a table, the `db.t4g.medium` single-AZ starting option along with its cost / downtime risk.
4. WHEN the aws-live RDS stability-first option is addressed THEN design.md SHALL specify the `db.t4g.medium` or `db.m6g.large` multi-AZ recommendation with retention 14 days / PITR on.
5. WHEN the PostgreSQL version is addressed THEN design.md SHALL recommend PostgreSQL 16 or higher and specify the minor version auto upgrade policy (enabled for paper / live).
6. WHEN the parameter group is addressed THEN design.md SHALL specify the recommendations `rds.force_ssl=1`, `log_min_duration_statement`, `log_lock_waits=1`, `idle_in_transaction_session_timeout`.

### Requirement 6: Keeping the portfolio DB and schema-per-domain

**Objective**: As an operator, I want to move the single portfolio DB and the 10-schema structure as-is, so that the existing SQL and search_path policy of the 8 MS work unchanged.

#### Acceptance Criteria

1. WHEN design.md is written THEN design.md SHALL specify a policy of using the database name `portfolio` inside the RDS of each of aws-paper / aws-live.
2. WHEN the schema policy is addressed THEN design.md SHALL specify a policy of keeping the 10 schemas (`reference, interest, preprocessor, research, decision, execution, connector, ops, legacy, public`) as-is.
3. WHEN the per-MS search_path policy is addressed THEN design.md SHALL specify a way of applying the search_path order defined in the 8 MS READMEs to the RDS connection identically, without changing it.
4. WHEN environment-variable key compatibility is addressed THEN design.md SHALL specify a policy of keeping the following keys as-is.
   - RDS: `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`, `PORTFOLIO_DB_NAME`
   - Broker / account: `PORT_ACCOUNT_NO`, `PORT_BROKER_NAME`
   - Environment: `PORT_ENVIRONMENT`
   - Strategy: `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION`, `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT`

### Requirement 7: Per-MS DB Role Permission Matrix

**Objective**: As an operator, I want a per-MS PostgreSQL role and schema-level permission matrix, so that I can secure the system with a least-privilege policy.

#### Acceptance Criteria

1. WHEN design.md is written THEN design.md SHALL specify, in a table, the per-schema read/write permission matrix of the 7 roles (`marketconnector_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `execution_app`, `research_app`, `view_app`).
2. WHEN view_app permissions are addressed THEN design.md SHALL specify the default permission of READ on all schemas + WRITE on ops, and indicate that the need for execution write is a re-review item in the `05-port-view-ecs-and-runbook` spec.
3. WHEN permission granting is addressed THEN design.md SHALL include the recommended SQL patterns for `USAGE`, `SELECT`, `INSERT/UPDATE/DELETE`, and `default privileges`.
4. WHEN the role-to-environment-variable mapping is addressed THEN design.md SHALL specify a policy that the value going into `INTEREST_DB_USER` may differ per MS.
5. WHEN secret storage is addressed THEN design.md SHALL specify, as a placeholder, that the DB password is a Secrets Manager or SSM SecureString item, and specify that the final decision is made in the `06-secrets-and-iam` spec. This document SHALL never output the actual value and SHALL denote it only as `[REDACTED]`.

### Requirement 8: Data cutover procedure (pg_dump / pg_restore)

**Objective**: As an operator, I want the cutover procedure of local-dev → aws-paper, then → aws-live after aws-paper validation, so that I can perform the cutover safely step by step.

#### Acceptance Criteria

1. WHEN design.md is written THEN design.md SHALL specify `pg_dump` + `pg_restore` as the top priority, and specify the decision to defer AWS DMS along with its rationale.
2. WHEN the cutover stages are addressed THEN tasks.md SHALL decompose the order operator stop → local dump → RDS restore → schema / search_path / role validation → MS aws-paper startup → validation → aws-live cutover into step-by-step tasks.
3. WHEN validation SQL candidates are addressed THEN design.md SHALL include the SQL for the schema list, the role list, the table count per schema, the search_path check, and the core table row count validation.
4. IF validation fails during cutover THEN tasks.md SHALL specify a rollback procedure where the operator stops immediately and returns to the local-dev environment.

### Requirement 9: RDS backup / snapshot / PITR policy

**Objective**: As an operator, I want the RDS automated backup, manual snapshot, and point-in-time recovery policy, so that the restore options before and after cutover become clear.

#### Acceptance Criteria

1. WHEN design.md is written THEN design.md SHALL specify the automated backup retention (aws-paper 7 days, aws-live 14 days), the recommended manual snapshot timing (right before cutover, quarterly), and the PITR enabled policy (aws-paper / aws-live).
2. WHEN snapshot cost is addressed THEN decision-matrix.md SHALL include a table of the impact of backup retention and single-AZ vs multi-AZ on cost.
3. WHEN the restore procedure is addressed THEN design.md SHALL include an outline of the procedure for restoring from a snapshot to a new RDS instance and changing the application's endpoint.

### Requirement 10: Safety Mechanisms and Automatic Retry Policy

**Objective**: As an operator, I want the criteria for the automatic BUY/SELL and automatic retry policy of aws-paper / aws-live, so that a wrong automatic retry does not lead to duplicate orders / financial loss.

#### Acceptance Criteria

1. WHEN aws-paper automatic BUY/SELL E2E validation is addressed THEN design.md SHALL specify the procedure of order blocking initially → stepwise allowance of automatic BUY/SELL after validation.
2. WHEN aws-live automatic BUY / SELL is addressed THEN design.md SHALL specify the policy of prohibiting automatic ordering initially, operating primarily around candidate generation + manual approval in the View, and reviewing limited automation after validation.
3. WHEN the automatic retry policy is addressed THEN design.md SHALL specify, in a table, automatic retry prohibited (BUY / SELL / fill sync result reflection / position change / intraday stop SELL creation) and automatic retry allowed (idempotent steps only).
4. WHEN paper and live environment separation is addressed THEN design.md SHALL specify an SG / Secrets / environment-variable-key separation policy so that no call to the aws-live broker occurs from aws-paper.

### Requirement 11: Cost Profiles (low / realistic / stable)

**Objective**: As an operator, I want a cost profile for each of aws-paper and aws-live, so that I can compare the cost-saving option and the stability-first option and choose.

#### Acceptance Criteria

1. WHEN decision-matrix.md is written THEN decision-matrix.md SHALL include, each in a table, the 3 cost profiles low / realistic / stable of aws-paper and the 3 cost profiles low / realistic / stable of aws-live.
2. WHEN cost-saving items are addressed THEN decision-matrix.md SHALL specify, with the impact of each item, removing the NAT Gateway, not building AWS dev, not using an ALB or deferring an internal ALB, starting RDS single-AZ, shortening CloudWatch Logs retention, minimizing Secrets Manager items or using SSM SecureString, and the VPC Endpoint vs NAT cost comparison.
3. WHEN the operational risk of cost-saving options is addressed THEN decision-matrix.md SHALL specify the operational risk that may arise per saving option (e.g., downtime on AZ failure, inability to perform a retrospective due to insufficient logs).

### Requirement 12: Step-by-step AWS Console Procedure

**Objective**: As an operator, I want a step-by-step procedure I can perform directly in the AWS Console, so that I can build the aws-paper environment safely even without writing IaC.

#### Acceptance Criteria

1. WHEN tasks.md is written THEN tasks.md SHALL decompose the order VPC → Subnet → IGW → Route Table → SG → VPC Endpoint → RDS subnet group → RDS parameter group → RDS instance → schema/role creation → validation SQL → cutover pre-check into small-unit tasks.
2. WHEN each task is addressed THEN tasks.md SHALL include the items the operator must click in the AWS Console, the values to input (sensitive information as `[REDACTED]`), the validation method, and the rollback method.
3. WHEN a task with high cost impact is addressed THEN tasks.md SHALL add an "approval required" marker and a one-line cost impact summary to that task.

### Requirement 13: AWS Resource Glossary and Operator Decision Record

**Objective**: As an operator, I want the AWS resource glossary and the current decision record as separate files, so that I can easily follow the follow-up specs even without being familiar with AWS terminology and track decision changes.

#### Acceptance Criteria

1. WHEN this spec's deliverables are created THEN the deliverables SHALL include the root common [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md) and SHALL include all of the following items.
   - Network: Region / VPC / Subnet / Public Subnet / Private Subnet / Route Table / Internet Gateway / NAT Gateway / NAT Instance / VPC Endpoint / Security Group / Elastic IP / ALB
   - Compute: EC2 / ECS / Fargate / ECR
   - Database: RDS / RDS Subnet Group / RDS Parameter Group
   - Security / Secrets: Secrets Manager / SSM Parameter Store / IAM Role / IAM Policy / Instance Profile / KMS
   - Observability: CloudWatch Logs / CloudWatch Metrics / CloudWatch Alarm
   - Orchestration: EventBridge Scheduler / Step Functions / Lambda / AWS Batch
   - Storage / Discovery: S3 / Cloud Map / Service Discovery
2. WHEN the glossary is written THEN each item SHALL include a one-line description, its role in this portfolio, whether it incurs cost, points the operator should be careful about, and the related follow-up spec.
3. WHEN this spec's deliverables are created THEN the deliverables SHALL include the root common [`../_common/operator-decisions.md`](../_common/operator-decisions.md) and SHALL record the current operator decisions in a table with the columns Decision ID / decision item / options / chosen value / status / cost impact / operational risk / follow-up spec impact.
4. WHEN the operator decision status is indicated THEN the root common [`../_common/operator-decisions.md`](../_common/operator-decisions.md) SHALL use the status values CONFIRMED / TENTATIVE / TBD / DEFERRED.

### Requirement 14: Safety Constraints of This Spec

**Objective**: As an operator, I want to explicitly restrict this spec's work so that it does not change code / operational data / existing documents.

#### Acceptance Criteria

1. WHEN this spec creates deliverables THEN the deliverables SHALL be limited to referencing / augmenting only the following files and the root common documents.
   - Inside this spec folder: [`requirements.md`](./requirements.md), [`design.md`](./design.md), [`tasks.md`](./tasks.md), [`decision-matrix.md`](./decision-matrix.md)
   - Root common: [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md)
2. WHEN this spec's work is in progress THE work SHALL NOT modify the README / AGENTS.md / CHANGELOG / docs / worklog and source code of the 8 MS.
3. WHEN this spec's work is in progress THE work SHALL NOT create or change actual AWS resources.
4. WHEN this spec's work is in progress THE work SHALL NOT execute the 8 MS entrypoints, or broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / order / fill / Daily Batch / intraday monitor calls.
5. WHEN secrets are addressed THE deliverables SHALL use only `[REDACTED]` in every secret position and SHALL NOT write the actual value.
6. WHEN the separation policy from 06-secrets-and-iam is addressed THEN this spec's deliverables SHALL specify that the final decision on Secrets storage / the IAM matrix is handled in the `06-secrets-and-iam` spec.
