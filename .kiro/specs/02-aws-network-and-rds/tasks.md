# Implementation Plan — 02-aws-network-and-rds

This work plan is a step-by-step procedure for the operator to proceed directly via the AWS Console or IaC. The environment model is 3: local-dev / aws-paper / aws-live. The first environment this spec applies to is aws-paper; the aws-live application is carried out integrally in `10-cutover-and-validation-runbook`.

Each task is decomposed into a small unit that can be reviewed and approved in a single session. Tasks with high cost impact are marked "approval required".

Within this spec's scope, actual AWS resource creation, modifying the 8 MS code / existing README / AGENTS.md / docs / CHANGELOG / worklog, executing the 8 MS entrypoints, and the actual cutover are not performed. All secrets use only `[REDACTED]`.

## Phase 0 — Pre-Decisions (documentation)

- [ ] 1. Decide the cost-saving option vs stability-first option line
  - Review the cost profiles (low / realistic / stable) in `decision-matrix.md` and decide the line for each of aws-paper and aws-live.
  - Record the result next to the corresponding Decision ID in the root common [`../_common/operator-decisions.md`](../_common/operator-decisions.md).
  - This task is documentation work.
  - _Requirements: 1, 11_

- [ ] 2. Choose CIDR / AZ
  - Confirm the VPC CIDR (recommended `10.0.0.0/16`) does not conflict with other in-house networks.
  - Decide the 2 AZs to use (recommended `ap-northeast-2a`, `ap-northeast-2c`).
  - _Requirements: 1, 2_

- [ ] 3. Decide the aws-paper / aws-live RDS instance class and multi-AZ
  - aws-paper: `db.t4g.small` single-AZ recommended.
  - aws-live: choose between the cost-saving option single-AZ vs the stability-first option multi-AZ.
  - Record the result in the root common [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-RDS-002 / OD-RDS-003.
  - _Requirements: 5_

- [ ] 4. Decide the NAT-free strategy workload mapping
  - Decide which of OPT-1 / OPT-2 / OPT-3 / OPT-4 to place the workloads requiring internet outbound.
  - Recommendation: marketconnector OPT-2, crawler / preprocessor / research OPT-1, promote only the crawler to OPT-3 if needed.
  - Reflect the result in the root common [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-NET-004.
  - _Requirements: 3_

- [ ] 5. Decide the enabled VPC Endpoint items
  - Decide per environment which items to enable among the 5 recommended types (S3, ECR api+dkr, Secrets Manager, SSM, CloudWatch Logs) + the options (STS / KMS).
  - Reflect the result in the root common [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-NET-005, OD-NET-006.
  - _Requirements: 3_

## Phase 1 — VPC / Network (AWS Console, aws-paper)

- [ ] 6. Create VPC (approval required, cost impact 0)
  - Console: VPC → Your VPCs → Create VPC.
  - Input: name `portfolio-vpc`, CIDR `10.0.0.0/16`, IPv6 disabled, tenancy default.
  - Validation: `portfolio-vpc` shown in the VPC list, state `available`.
  - rollback: Delete the VPC. However, deletion is not possible if there are attached resources.
  - _Requirements: 1, 2_

- [ ] 7. Create 6 Subnets
  - Create public-a, public-b, app-a, app-b, data-a, data-b based on the design.md CIDR table.
  - Match each subnet's AZ to the task 2 decision.
  - Validation: Confirm the 6 subnets are mapped to each AZ.
  - rollback: Delete any wrongly created subnet.
  - _Requirements: 2_

- [ ] 8. Create Internet Gateway / attach to VPC
  - Console: VPC → Internet Gateways → Create.
  - Attach to the VPC.
  - Validation: state `attached`.
  - rollback: detach then delete.
  - _Requirements: 2_

- [ ] 9. (NAT-not-used confirmation task) Do not create a NAT Gateway / NAT Instance
  - Per this spec's decision, do not create NAT resources.
  - Record that fact in a CONFIRMED state in the root common [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-NET-001 / OD-NET-002.
  - _Requirements: 3_

- [ ] 10. Create and associate the 3 Route Tables
  - rt-public: 0.0.0.0/0 → IGW. Associate public-a, public-b.
  - rt-app: VPC local only. No external route. Associate app-a, app-b.
  - rt-data: VPC local only. No external route. Associate data-a, data-b.
  - Validation: Confirm the routes / associations of each Route Table.
  - rollback: Remove any wrongly mapped route.
  - _Requirements: 2_

- [ ] 11. Create Security Groups (empty rules)
  - Create all SGs in the design.md SG table: sg-marketconnector-ec2, sg-port-view-ecs, sg-strategy-tasks, sg-crawler-tasks, sg-preprocessor-tasks, sg-research-batch, sg-rds-postgres, sg-vpc-endpoints. (The ALB SG is added when the ALB adoption decision is made.)
  - Rules are filled in task 12.
  - _Requirements: 4_

- [ ] 12. Fill in SG rules (RDS first)
  - sg-rds-postgres inbound: Register the app SGs as 5432 source SGs. Never allow 0.0.0.0/0 inbound.
  - sg-vpc-endpoints inbound: Register the app SGs as 443 source SGs.
  - sg-marketconnector-ec2 outbound: 0.0.0.0/0 (broker) + sg-rds-postgres + sg-vpc-endpoints.
  - sg-crawler-tasks outbound: 0.0.0.0/0 (KRX/Naver/yfinance) + sg-rds-postgres + sg-vpc-endpoints. Never allow inbound.
  - sg-preprocessor-tasks outbound: 0.0.0.0/0 (holiday API) + sg-rds-postgres + sg-vpc-endpoints.
  - sg-port-view-ecs outbound: 0.0.0.0/0 (Slack), sg-marketconnector-ec2, sg-rds-postgres, VPC Endpoints.
  - sg-strategy-tasks / sg-research-batch outbound: based on the design.md table.
  - Validation: Compare the SG rules once more in the Console.
  - _Requirements: 4_

- [ ] 13. Create VPC Endpoints (approval required, cost impact medium)
  - S3 (Gateway) endpoint: associate rt-app, rt-data.
  - ECR (api+dkr), Secrets Manager, SSM, CloudWatch Logs Interface endpoints: placed in the app subnet, attach sg-vpc-endpoints, enable private DNS.
  - (Optional) STS / KMS endpoints: enabled per the task 5 decision.
  - Validation: Each endpoint state `available`. The ECR endpoint `availabilityZones` includes all app subnet AZs.
  - rollback: Delete the endpoint (but under NAT-free, without the endpoint, ECR pull etc. will fail).
  - _Requirements: 3_

## Phase 2 — RDS (aws-paper)

- [ ] 14. Create RDS subnet group
  - Console: RDS → Subnet groups → Create.
  - Include the two subnets data-a, data-b (in a multi-AZ-capable form).
  - Validation: status `Complete`.
  - _Requirements: 5_

- [ ] 15. Create RDS parameter group
  - A parameter group `pg-portfolio-paper` for the PostgreSQL 16 family.
  - `rds.force_ssl = 1`, `log_min_duration_statement = 1000` (adjust if needed), `log_lock_waits = 1`, `idle_in_transaction_session_timeout = 60000`.
  - Validation: Confirm the changes are applied in the parameter group details.
  - _Requirements: 5_

- [ ] 16. (In advance) Register the DB master password in Secrets Manager
  - secret name: `/portfolio/paper/rds/master`.
  - value: `[REDACTED]` (entered directly by the operator).
  - Since this task overlaps with the `06-secrets-and-iam` spec, skip it if that spec has already progressed.
  - _Requirements: 7_

- [ ] 17. Create RDS instance (approval required, high cost impact)
  - Follow the per-environment task 3 decision (this task proceeds with aws-paper).
  - Input: engine PostgreSQL 16, instance class `db.t4g.small`, single-AZ, gp3 50 GB, encryption at rest enabled (KMS default), publicly accessible false, VPC subnet group, sg-rds-postgres, parameter group `pg-portfolio-paper`, backup retention 7 days, PITR on.
  - master username: `portfolio_admin`. password: the Secrets Manager value (`[REDACTED]`).
  - DB name: leave blank at this stage and create the `portfolio` DB in task 18.
  - Validation: instance status `Available`. Obtain the endpoint URL.
  - rollback: Delete the instance (after deciding the snapshot option).
  - _Requirements: 5, 9_

## Phase 3 — DB Initialization (SQL via SSM)

The operator connects to RDS via psql from an ECS Task or EC2 within the same VPC, using SSM Session Manager. Never open SSH 22 inbound.

- [ ] 18. Create the `portfolio` database
  - Connect with the master account via psql, then `CREATE DATABASE portfolio;`.
  - Validation: Confirm the portfolio DB with `\l`.
  - _Requirements: 6_

- [ ] 19. Create the 10 schemas
  - `CREATE SCHEMA IF NOT EXISTS reference;` and 9 others.
  - Validation: `SELECT schema_name FROM information_schema.schemata;`.
  - _Requirements: 6_

- [ ] 20. Create the 7 roles
  - `CREATE ROLE marketconnector_app LOGIN PASSWORD '[REDACTED]';` and 6 others.
  - The password is the value registered in Secrets Manager, entered directly by the operator. Do not write it in this document.
  - Validation: `SELECT rolname FROM pg_roles;`.
  - _Requirements: 7_

- [ ] 21. Apply the per-role schema permission matrix
  - Apply the design.md "Per-Schema Permission Matrix" table as-is.
  - `GRANT USAGE ON SCHEMA <schema> TO <role>;` + `GRANT SELECT ON ALL TABLES IN SCHEMA <schema> TO <role>;` + if needed `GRANT INSERT, UPDATE, DELETE` + `ALTER DEFAULT PRIVILEGES`.
  - Validation: Check the permission matrix SQL one role at a time.
  - _Requirements: 7_

- [ ] 22. Set the per-role search_path default
  - Apply the search_path order from design.md / the 8 MS READMEs as-is, in the form `ALTER ROLE <role> SET search_path = <order>;`.
  - Validation: Connect as each role and `SHOW search_path;`.
  - _Requirements: 6_

- [ ] 23. Organize the RDS endpoint and environment-variable mapping
  - Record the mapping `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME=portfolio`, `INTEREST_DB_USER=<role>`, `INTEREST_DB_PASSWORD=[REDACTED]`, `PORTFOLIO_DB_NAME=portfolio` in the operator notes.
  - Actual value registration is in `06-secrets-and-iam`.
  - _Requirements: 6, 7_

## Phase 4 — Validation (read-only)

- [ ] 24. Run the validation SQL (read-only)
  - Run the SQL in the design.md "Validation SQL Candidates" section in order.
  - Record the result in the operator notes (row counts may be empty at this point).
  - _Requirements: 8_

- [ ] 25. Confirm RDS backup operation
  - Confirm the automated backup setting values and the first backup creation time in the Console.
  - Create one manual snapshot: name `before-cutover-paper`.
  - _Requirements: 9_

- [ ] 26. Confirm CloudWatch RDS logs / metrics
  - Confirm the PostgreSQL log group is enabled (`/aws/rds/instance/{db-id}/postgresql`).
  - Confirm the DB Connections, FreeableMemory, CPUUtilization metric graphs.
  - _Requirements: 9_

## Phase 5 — Cutover Pre-Check (actual cutover in a separate spec)

- [ ] 27. local-dev → aws-paper data cutover pre-check
  - Within this spec's scope, only document the procedure. The actual cutover is integrated in `10-cutover-and-validation-runbook`.
  - Copy stages 1~9 of the design.md "Data cutover Procedure" into the operator notes in advance and decide the cutover schedule separately.
  - _Requirements: 8_

- [ ] 28. Agree on rollback criteria
  - The operator reviews the items in the design.md "Rollback Criteria" and records additions / changes in this task.
  - The rollback target is a return to the local-dev environment (just revert the environment variables).
  - _Requirements: 8_

- [ ] 29. Confirm the pre-safety mechanisms for aws-paper automatic BUY/SELL E2E validation
  - Check the environment-variable / Secret prefix separation so that no call to broker live occurs from aws-paper.
  - Agree on operating the Stage 1 order-blocking mode (no code changes yet, operator notes only).
  - Agree on the automatic retry policy (BUY / SELL / fill sync / position change / intraday stop SELL creation prohibited).
  - _Requirements: 10_

## What Must Not Be Done in This Spec

- Prohibit modifying the 8 MS code / README / AGENTS.md / CHANGELOG / docs / worklog.
- Actual AWS resource creation is not forced in these tasks. The tasks are a procedure guide for when the operator proceeds directly in the Console.
- Do not write actual secret values in the tasks / validation results / operator notes (`[REDACTED]`).
- Prohibit broker / KIS / Selenium / KRX / Naver / yfinance / order / fill / Daily Batch / intraday monitor / auto-trading calls.
- In this spec, the cutover dump / restore is also not actually executed (only the procedure is agreed).
- The aws-live application is carried out integrally in `10-cutover-and-validation-runbook`. This spec only goes up to aws-paper.
- The final decision on the Secrets / IAM matrix is made in `06-secrets-and-iam`. This spec keeps only placeholders.
