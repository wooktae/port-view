# Validation Checklist — 02-aws-network-and-rds

This checklist is a checkpoint list for the operator to confirm pass/fail one line at a time after progressing through each Step of [`./runbook.md`](./runbook.md). All items must be checked for this spec's first (aws-paper) progress to be finalized.

Do not write actual secret / password / token / app key / app secret / account number / webhook URL values in this document. All use only `[REDACTED]`.

## Status notation rules

Do not use the existing Markdown checkboxes (`- [ ]`, `- [x]`). This checklist uses the following 4 labels.

- `<span style="color:red">[O]</span>` — confirmation complete or success.
- `<span style="color:blue">[X]</span>` — failure or expected-value mismatch. Record `mismatch: actual = ..., expected = ...` at the end of the item.
- `<span style="color:green">[Kiro 후속 작업 필요]</span>` — an item Kiro can confirm via additional file reads / grep / git status / AWS ReadOnly queries / cross-document comparison. Also note the reason it was not performed in this session or a lack of privilege.
- `<span style="color:black">[운영자 확인 필요]</span>` — an item requiring operator judgment, cost approval, whether externally exposed, or human decision / cognition.

Automatic validation updates via AWS API call results using the ReadOnly IAM privileges of [`../../docs/kiro-readonly-validator-iam.md`](../../docs/kiro-readonly-validator-iam.md). The Secret value is never queried (metadata only).

## 1. Pre-flight Checklist

- <span style="color:red">[O]</span> Confirm the AWS Region is `ap-northeast-2` — all AWS API calls of this validation respond normally in the `ap-northeast-2` region
- <span style="color:red">[O]</span> Confirm this is aws-paper-targeted work (OD-ENV-003) — OD-ENV-003 = `aws-paper` 🟢 확정 is registered in the `../_common/operator-decisions.md` At a Glance, and this spec's (02) requirements / design / runbook all consistently specify aws-paper as the first application environment (0 mismatches)
- <span style="color:red">[O]</span> Confirm this work does not modify the 8 MS code / README / AGENTS.md / CHANGELOG / docs / worklog — the 8 workspaces' `git status --short` results are all unchanged. Inside port-view too, no changes outside `.kiro/`
- <span style="color:red">[O]</span> Confirm actual secret values are not exposed in this spec / operator notes / console captures — repo grep is covered in §9. External areas such as console captures / external notes / personal PC files can be inspected only by the operator -> confirmation complete
- <span style="color:red">[O]</span> Confirm operator approval before creating cost-incurring resources (VPC Endpoint, RDS, EIP) (R-COST-001) -> confirmation complete
- <span style="color:red">[O]</span> Confirm the core-decision lock state in the `At a Glance` of [`../_common/operator-decisions.md`](../_common/operator-decisions.md) (OD-NET-001 / OD-NET-005 / OD-NET-009 / OD-RDS-001 / OD-CUT-001) — human cognition itself is operator direct confirmation -> confirmation complete
- <span style="color:red">[O]</span> A cost profile line (low / realistic / stable) decision record exists in [`./decision-matrix.md`](./decision-matrix.md) — confirmed by the `aws-paper low 적용` line in operation-notes
- <span style="color:red">[O]</span> Awareness of the R-NET-001 ~ R-COST-002 items in [`../_common/risk-register.md`](../_common/risk-register.md) — human cognition item -> confirmation complete

## 2. Network Validation

- <span style="color:red">[O]</span> Confirm VPC `portfolio-vpc` creation. State = `Available`. CIDR = `10.0.0.0/16`
- <span style="color:red">[O]</span> `env=paper`, `project=portfolio` applied to VPC Tags
- <span style="color:red">[O]</span> 2 Public subnets (`public-a` 10.0.0.0/24, `public-b` 10.0.1.0/24) created
- <span style="color:red">[O]</span> 2 Private app subnets (`app-a` 10.0.10.0/24, `app-b` 10.0.11.0/24) created
- <span style="color:red">[O]</span> 2 Private data subnets (`data-a` 10.0.20.0/24, `data-b` 10.0.21.0/24) created
- <span style="color:red">[O]</span> The AZ mapping (a / c) of the 6 subnets matches the intent — public-a/app-a/data-a = ap-northeast-2a, public-b/app-b/data-b = ap-northeast-2c
- <span style="color:red">[O]</span> Internet Gateway `portfolio-igw` created + attached to `portfolio-vpc` (State = `available`)
- <span style="color:red">[O]</span> Confirm NAT Gateway non-creation (NAT Gateways menu empty, R-COST-002)
- <span style="color:red">[O]</span> Confirm NAT-role EC2 non-creation (no `nat-*` Name instance, 0 EC2 instances in the VPC)
- <span style="color:red">[O]</span> 3 Route Tables created: `rt-public`, `rt-app`, `rt-data`
- <span style="color:red">[O]</span> `0.0.0.0/0 → portfolio-igw` exists in `rt-public` Routes
- <span style="color:red">[O]</span> `rt-app`, `rt-data` Routes have no external route other than VPC local + S3 Gateway endpoint (prefix list) (no 0.0.0.0/0 route, R-NET-002)
- <span style="color:red">[O]</span> Subnet associations: `rt-public` ↔ public-a/b, `rt-app` ↔ app-a/b, `rt-data` ↔ data-a/b

## 3. Security Group Validation

- <span style="color:red">[O]</span> 8 SGs created (the name prefix is `sgroup-` per operator decision):
  - `sgroup-marketconnector-ec2`, `sgroup-port-view-ecs`
  - `sgroup-strategy-tasks`, `sgroup-crawler-tasks`, `sgroup-preprocessor-tasks`, `sgroup-research-batch`
  - `sgroup-rds-postgres`, `sgroup-vpc-endpoints`
- <span style="color:red">[O]</span> `sgroup-rds-postgres` inbound = only the 6 app/EC2 SG references (5432). **No 0.0.0.0/0 inbound** (R-SEC-001)
- <span style="color:red">[O]</span> `sgroup-rds-postgres` outbound is empty
- <span style="color:red">[O]</span> `sgroup-vpc-endpoints` inbound = only the 6 app/EC2 SG references (443)
- <span style="color:red">[O]</span> `sgroup-marketconnector-ec2` inbound = `sgroup-port-view-ecs` (TCP 5000 or an operator-decided port)
- <span style="color:red">[O]</span> `sgroup-marketconnector-ec2` outbound requirements met.
  - actual value: HTTPS(443) → 0.0.0.0/0(broker) + sgroup-vpc-endpoints(443) + sgroup-rds-postgres(5432)
  - all design.md table requirements (0.0.0.0/0 broker, sg-rds-postgres 5432, VPC Endpoints 443) match (only the notation order differs)
- <span style="color:red">[O]</span> `sgroup-port-view-ecs` inbound is empty (or only an operator IP allowlist)
- <span style="color:red">[O]</span> `sgroup-port-view-ecs` outbound = HTTPS 0.0.0.0/0 + sgroup-marketconnector-ec2(5000) + sgroup-rds-postgres(5432) + sgroup-vpc-endpoints(443)
- <span style="color:red">[O]</span> `sgroup-crawler-tasks` inbound is empty (R-NET-001)
- <span style="color:red">[O]</span> `sgroup-crawler-tasks` outbound = HTTPS 0.0.0.0/0 + sgroup-rds-postgres + sgroup-vpc-endpoints
- <span style="color:red">[O]</span> `sgroup-preprocessor-tasks` inbound is empty
- <span style="color:red">[O]</span> `sgroup-preprocessor-tasks` outbound = HTTPS 0.0.0.0/0 + sgroup-rds-postgres + sgroup-vpc-endpoints
- <span style="color:red">[O]</span> `sgroup-strategy-tasks` outbound = sgroup-marketconnector-ec2(5000) + sgroup-rds-postgres(5432) + sgroup-vpc-endpoints(443) + HTTPS 0.0.0.0/0
- <span style="color:red">[O]</span> `sgroup-research-batch` outbound = sgroup-rds-postgres + sgroup-vpc-endpoints + HTTPS 0.0.0.0/0
- <span style="color:red">[O]</span> EC2 SSH 22 inbound 0.0.0.0/0 is nowhere in any SG (OD-NET-009) — 0 results for a VPC-wide port 22 0/0 inbound search

## 4. VPC Endpoint Validation

- <span style="color:red">[O]</span> S3 Gateway endpoint (`vpce-s3-gw`) created. Connected to `rt-app`, `rt-data`
- <span style="color:red">[O]</span> ECR API Interface endpoint (`vpce-ecr-api`) State = `available`. app-a, app-b included in AZ
- <span style="color:red">[O]</span> ECR DKR Interface endpoint (`vpce-ecr-dkr`) State = `available`
- <span style="color:red">[O]</span> Secrets Manager Interface endpoint (`vpce-secretsmanager`) State = `available`
- <span style="color:red">[O]</span> SSM Interface endpoint (`vpce-ssm`) State = `available`
- <span style="color:red">[O]</span> CloudWatch Logs Interface endpoint (`vpce-logs`) State = `available`
- <span style="color:red">[O]</span> All Interface endpoints Private DNS = `enabled`
- <span style="color:red">[O]</span> All Interface endpoints SG = `sgroup-vpc-endpoints`
- <span style="color:black">[운영자 확인 필요]</span> (option) STS / KMS endpoints are active / inactive per the OD-NET-006 decision — currently not created. Whether to activate is an operator decision

## 5. RDS Validation

- <span style="color:red">[O]</span> RDS Subnet Group `portfolio-paper-subnet-group` Status = `Complete`. data-a, data-b included
- <span style="color:red">[O]</span> RDS Parameter Group `pg-portfolio-paper` created. family = `postgres16`
- Parameter group applied values:
  - <span style="color:red">[O]</span> `rds.force_ssl = 1` (system source. effective value = 1)
  - <span style="color:red">[O]</span> `log_min_duration_statement = 1000` (user source)
  - <span style="color:red">[O]</span> `log_lock_waits = 1` (user source)
  - <span style="color:red">[O]</span> `idle_in_transaction_session_timeout = 60000` (user source)
- <span style="color:red">[O]</span> (prerequisite) Secrets Manager `/portfolio/paper/rds/master` registered. Value is `[REDACTED]` — confirm metadata only with DescribeSecret. No value query
- <span style="color:red">[O]</span> RDS instance `portfolio-paper-rds` Status = `available`
- <span style="color:red">[O]</span> RDS engine = PostgreSQL 16.x (actual value = 16.14)
- <span style="color:red">[O]</span> RDS instance class = `db.t4g.small` (OD-RDS-001)
- <span style="color:red">[O]</span> RDS deployment = Single-AZ (MultiAZ = false)
- <span style="color:red">[O]</span> RDS storage = gp3 50 GB (storage autoscaling max = 100 GB)
- <span style="color:red">[O]</span> RDS encryption at rest = Enabled (KMS default)
- <span style="color:red">[O]</span> RDS Performance Insights = Enabled
- <span style="color:red">[O]</span> RDS VPC = `portfolio-vpc`, subnet group = `portfolio-paper-subnet-group`, SG = `sgroup-rds-postgres`
- <span style="color:red">[O]</span> RDS Publicly accessible = `No` (R-SEC-001)
- <span style="color:red">[O]</span> RDS Backup retention = 7 days (OD-RDS-006)
- <span style="color:red">[O]</span> RDS PITR = Enabled (OD-RDS-008) — PITR active with BackupRetentionPeriod=7 + automated backups enabled. The operator is recommended to confirm the additional time in the Console
- <span style="color:red">[O]</span> RDS Deletion protection = Enabled
- <span style="color:red">[O]</span> RDS DB parameter group = `pg-portfolio-paper` (Status `in-sync`)
- <span style="color:red">[O]</span> The RDS Endpoint URL is recorded in the operator notes (`INTEREST_DB_HOST`) — see the DB Endpoint line in operation-notes. The hostname is not recorded in this checklist (public exposure prohibited)

## 6. DB / schema / role preparation Validation

The operator executed the [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4 SQL of this spec, and confirmed the results with the §5 validation SQL. See the [`./operation-notes.md`](./operation-notes.md) `## 2026-06-09 DB Role / 권한 분리 1차 적용` section for detailed execution records.

- <span style="color:red">[O]</span> Confirm the `portfolio` database exists — exists at restore completion. Target of this session's SQL application
- <span style="color:red">[O]</span> Confirm the 10 schemas exist: `reference, interest, preprocessor, research, decision, execution, connector, ops, legacy, public` — confirmed in the §4.1 pre-check
- <span style="color:red">[O]</span> 7 app roles creation complete — all 7 rows of the §5.1 result exist.
  - `marketconnector_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `execution_app`, `research_app`, `view_app`
  - all confirmed LOGIN = true, SUPERUSER / CREATEDB / CREATEROLE / REPLICATION / BYPASSRLS = false
- <span style="color:red">[O]</span> The per-role schema privilege matrix matches the [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §2 table — §5.2 schema USAGE / CREATE, §5.3 table / sequence privilege summary all 0 mismatches. legacy USAGE confirmed false for all 7 app roles
- <span style="color:red">[O]</span> per-role search_path applied per the 8 MS README definitions (R-DATA-001) — the §5.1 search_path result matches the [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §3 table
- <span style="color:black">[운영자 확인 필요]</span> role passwords are recorded only as Secrets Manager / SSM SecureString placeholders (`[REDACTED]`) — formal storage is registered at the `06-secrets-and-iam` progress point, then this item is updated to [O]

## 7. Cutover preparation Validation

- <span style="color:black">[운영자 확인 필요]</span> pg_dump command format agreed in the operator notes (`--format=custom --no-owner --no-privileges`)
- <span style="color:black">[운영자 확인 필요]</span> pg_restore command format agreed in the operator notes (`--no-owner --no-privileges --jobs=N`)
- <span style="color:black">[운영자 확인 필요]</span> dump file storage location / deletion policy agreed
- <span style="color:black">[운영자 확인 필요]</span> agreement to use the [`./design.md`](./design.md) "Validation SQL Candidates" section for cutover validation
- <span style="color:black">[운영자 확인 필요]</span> [`./design.md`](./design.md) "Rollback Criteria" agreed (matches R-DATA-002 mitigation)
- <span style="color:black">[운영자 확인 필요]</span> agreement to separate `PORT_ENVIRONMENT=paper` / Secret prefix so that no call occurs from aws-paper to the aws-live broker (R-AUTO-002)
- <span style="color:black">[운영자 확인 필요]</span> automatic-retry policy agreed: BUY/SELL/fill sync/position change/intraday stop SELL creation are prohibited from automatic retry (OD-SAFE-004, R-AUTO-001)

## 8. Cost Validation

- <span style="color:black">[운영자 확인 필요]</span> the daily cost related to this spec in the AWS Billing Dashboard is within the [`./decision-matrix.md`](./decision-matrix.md) cost profile line (R-COST-001) — the billing amount itself is confirmed directly in the Billing Dashboard
- <span style="color:red">[O]</span> NAT Gateway-Hours line 0 (R-COST-002) — 0 NAT Gateways automatic validation
- <span style="color:red">[O]</span> ALB-Hours line 0 or matches an operator-decided line — `elbv2 describe-load-balancers` and classic `elb describe-load-balancers` results show 0 LBs inside portfolio-vpc. Matches this spec's decision that ALB is unused
- <span style="color:green">[Kiro 후속 작업 필요]</span> the Endpoint-Hours line matches the active endpoint count × AZ count unit price.
  - The endpoint inventory is confirmed by automatic validation of Interface endpoint 5 types × 2 AZ + S3 Gateway.
  - The Cost Explorer ReadOnly call result shows 0 USAGE_TYPE groups at this time (presumed data-accumulation lag).
  - Retry after a few days once Cost Explorer data accumulates and update to [O] / [X].
- <span style="color:green">[Kiro 후속 작업 필요]</span> the RDS-InstanceUsage line is based on the single-AZ unit price.
  - The RDS instance class / Single-AZ automatic validation is complete.
  - The Cost Explorer ReadOnly call result shows 0 USAGE_TYPE groups at this time (presumed data-accumulation lag).
  - Retry after a few days once Cost Explorer data accumulates and update to [O] / [X].
- <span style="color:black">[운영자 확인 필요]</span> Cost Anomaly Detection alert registration (operator decision) — `ce get-anomaly-monitors` / `ce get-anomaly-subscriptions` results show 0. Whether to register is an operator decision

## 9. Documentation Validation

- <span style="color:red">[O]</span> This spec's 4 files and this runbook / validation-checklist / traceability-matrix are all authored — confirmed directly in the file system
- <span style="color:red">[O]</span> The decision states related to this spec in [`../_common/operator-decisions.md`](../_common/operator-decisions.md) match — as a result of Kiro comparing the two documents, the decisions below are all consistent with this spec design / runbook (0 mismatches).
  - OD-ENV-003 aws-paper / OD-ENV-005 single VPC
  - OD-NET-001 paper NAT unused / OD-NET-005 recommended endpoint 5 types / OD-NET-009 SSM only
  - OD-RDS-001 db.t4g.small single-AZ / OD-RDS-004 PG16 / OD-RDS-006 backup 7 days / OD-RDS-008 PITR on
  - OD-DB-001~006 portfolio + schema-per-domain + 7 roles
  - OD-CUT-001 pg_dump+pg_restore
  - OD-SAFE-001~004
- <span style="color:red">[O]</span> The R-NET / R-SEC / R-DATA / R-COST item mitigations in [`../_common/risk-register.md`](../_common/risk-register.md) are inspected — as a result of Kiro comparing each risk row's mitigation body with this spec's controls, they are consistent.
  - R-NET-001 = sgroup-* inbound is empty
  - R-NET-002 = OPT-1 mapping
  - R-NET-003 = endpoint 5 types available
  - R-SEC-001 = sgroup-rds-postgres SG-reference inbound + RDS public access No
  - R-DATA-001 = role search_path policy / R-DATA-002 = pg_dump options agreement item
  - R-AUTO-001~002 = OD-SAFE-* state machine policy
  - R-DOCS-001 = `[REDACTED]` grep 0
  - R-COST-001~002 = NAT / ALB 0 + Endpoint recommended set
  - R-SEC-002 / R-SEC-003 are consistent with the 02 runbook Step 0 portadmin flow
- <span style="color:red">[O]</span> [`./traceability-matrix.md`](./traceability-matrix.md) Requirement → Design → Task → Decision mapping complete — the mapping was completed in the previous session + synchronized to match the Step renumbering
- <span style="color:red">[O]</span> Use only `[REDACTED]` in all secret slots. No actual value exposure (R-DOCS-001) — the grep result over the `.kiro/**/*.md` tree shows 0 for all patterns below. Console captures / external notes / personal PC files / areas outside the repo are managed separately in [운영자 확인 필요] (§1).
  - `(AKIA|ASIA)` access key id
  - Slack incoming webhook URL / JWT (`eyJ...`)
  - `(PASSWORD|SECRET|TOKEN|APP_KEY|APP_SECRET)=값`
  - `aws_secret_access_key=값`
  - `KIS*KEY|SECRET|TOKEN=값`
  - account number plaintext pattern

## 10. Rollback Validation

This section is a conditional checklist used only when rollback was actually performed. Currently, AWS Foundation creation succeeded and rollback was not performed, so the rollback-not-performed state is normal. If the operator executes the [`./runbook.md`](./runbook.md) Step 19 rollback in the future, re-validate each item with AWS API and update to [O] or [X].

- <span style="color:red">[O]</span> Rollback not performed — since Foundation creation succeeded, RDS instance `portfolio-paper-rds` is currently not a deletion / final snapshot target
- <span style="color:red">[O]</span> Rollback not performed — RDS Subnet Group / Parameter Group are currently not deletion targets
- <span style="color:red">[O]</span> Rollback not performed — the 6 VPC Endpoints are currently not deletion targets
- <span style="color:red">[O]</span> Rollback not performed — the 8 Security Groups are currently not deletion targets
- <span style="color:red">[O]</span> Rollback not performed — the 3 Route Tables are currently not deletion / association-release targets
- <span style="color:red">[O]</span> Rollback not performed — the Internet Gateway is currently not a detach + delete target
- <span style="color:red">[O]</span> Rollback not performed — the 6 Subnets are currently not deletion targets
- <span style="color:red">[O]</span> Rollback not performed — VPC `portfolio-vpc` is currently not a deletion target
- <span style="color:red">[O]</span> Rollback not performed — for the AWS Billing daily cost, it is normal that the normal operation line is currently maintained
- <span style="color:red">[O]</span> Rollback not performed — at this time, recording the rollback reason / date / next attempt plan is unnecessary. Record it in the operator notes when rollback is executed in the future

## 11. DB Role privilege matrix Validation

This section organizes, with labels, the results confirmed with the §5 validation SQL and §5.4 connection validation after applying the [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4 SQL. See the [`./operation-notes.md`](./operation-notes.md) `## 2026-06-09 DB Role / 권한 분리 1차 적용` section for detailed execution records.

- <span style="color:red">[O]</span> `portfolio_owner` creation complete (NOLOGIN). `portfolio_owner` membership granted to `portfolio_admin` complete — §5.1 query result row confirmed to exist
- <span style="color:red">[O]</span> 9 domain schemas (`reference, interest, preprocessor, research, decision, execution, connector, ops, legacy`) owner = `portfolio_owner` transfer complete — `public` is not changed
- <span style="color:black">[운영자 확인 필요]</span> the owner of existing tables / sequences / indexes remains as `portfolio_admin`.
  - `REASSIGN OWNED BY portfolio_admin TO portfolio_owner` was not executed this session.
  - Managed separately by a follow-up operator decision on whether to transfer all at once.
  - At this time, the privilege matrix is applied via the §4.4 explicit GRANT.
  - §4.5 default privileges apply automatically only to new objects afterward.
- <span style="color:red">[O]</span> 7 app roles creation + DB CONNECT + public USAGE grant complete (§4.3 SQL application result)
- <span style="color:red">[O]</span> §5.1 all 7 app roles LOGIN = true, SUPERUSER / CREATEDB / CREATEROLE / REPLICATION / BYPASSRLS = false
- <span style="color:red">[O]</span> the `search_path` of the §5.1 7 app roles matches the [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §3 table
- <span style="color:red">[O]</span> §5.2 schema USAGE / CREATE matrix 0 mismatches with the [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §2 table. All app roles' `legacy` USAGE = false
- <span style="color:red">[O]</span> §5.3 table privilege summary / sequence privilege summary 0 mismatches with the [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §2 table
- <span style="color:red">[O]</span> §5.4 `marketconnector_app` connection validation — connection success. `connector` query success, `execution` query success, `legacy` USAGE = false. `execution` INSERT/UPDATE/DELETE → permission denied normal (matches read-only intent)
- <span style="color:red">[O]</span> §5.4 `execution_app` connection validation — connection success. `execution` / `decision` / `connector` query success, `legacy` USAGE = false. `execution` INSERT/UPDATE/DELETE success (matches R/W intent)
- <span style="color:red">[O]</span> §5.4 `view_app` connection validation — connection success. `execution` / `connector` / `decision` query success, `legacy` USAGE = false. `execution` INSERT/UPDATE/DELETE → permission denied normal (the view write scope is currently limited to `ops`)
- <span style="color:black">[운영자 확인 필요]</span> formal Secrets Manager / SSM SecureString registration of the 7 app role passwords — registered at the `06-secrets-and-iam` progress point, then updated to [O]
- <span style="color:red">[O]</span> The matrix changes (legacy not granted, `marketconnector_app` execution reduced to R only) are updated in the OD-DB category of [`../_common/operator-decisions.md`](../_common/operator-decisions.md) — OD-DB-007 / OD-DB-008 / OD-DB-009 / OD-DB-010 added

## 12. RDS Restore Validation (Local → aws-paper)

This section organizes, with labels, the result of the Local PostgreSQL → aws-paper RDS migration that the operator performed directly on 2026-06-09. See the [`./operation-notes.md`](./operation-notes.md) `## 2026-06-09 Local → RDS Migration & RDS 재생성 실행 기록` section for detailed execution records.

- <span style="color:red">[O]</span> local dump file integrity — `portfolio_full_20260609.dump` (format custom + gzip), all 3 locations local / S3 temporary bucket / MarketConnector EC2 match at 422,334,494 bytes
- <span style="color:red">[O]</span> dump metadata — `pg_restore --list` TOC Entries 812, line count 823, dump source PostgreSQL 18.1
- <span style="color:red">[O]</span> RDS engine version — RDS recreated per the major version mismatch avoidance decision. New RDS engine = PostgreSQL 18.4, initial DB = `portfolio`, Public access = No retained
- <span style="color:red">[O]</span> Restore Runner path — Local Windows PC → S3 temporary bucket → MarketConnector EC2 (Amazon Linux 2023, public subnet, EIP attach) → private RDS. The local PC IP is not allowed directly in the RDS SG
- <span style="color:red">[O]</span> first `role "postgres" does not exist` error handling — re-ran with `pg_restore --no-owner --no-privileges` after DB drop / recreate. Completed without error. The RDS object owner is based on the restore execution account (`portfolio_admin`)
- <span style="color:red">[O]</span> consistency — schema-by-schema table count 81 match, table / index / sequence / FK 33 / trigger 23 / per-table row count clean CSV 82 lines all diff 0 against the local baseline (CRLF / LF compared after `--strip-trailing-cr` normalization)
- <span style="color:red">[O]</span> sensitive information not recorded — no new recording of RDS endpoint hostname / password / secret value / account-id / account number / token in this validation result. Only `[REDACTED]` or placeholders used

## 13. DB Role privilege / search_path reinforcement Validation (2026-06-17 17-step E2E)

This section organizes, with labels, the DB role privilege / search_path facts that the operator discovered / corrected during the 2026-06-17 Daily AWS 17-step E2E flow.

- Detailed execution records: [`./operation-notes.md`](./operation-notes.md) `## 2026-06-17 Daily AWS 17-step E2E 흐름 중 발견된 DB Role / 권한 / search_path 보정` §1 ~ §4.
- Risk reference: [`../_common/risk-register.md`](../_common/risk-register.md) R-DATA-005 [2026-06-17 reinforcement] / R-DATA-011 new.
- Changes to this spec body's decision values (§4 GRANT / §5 validation SQL) are follow-up phase / on this date only factual recording.

- <span style="color:red">[O]</span> `execution_app`'s `interest` schema USAGE privilege correction (Step 8 first failure → passed after the operator directly GRANTed / R-DATA-005 [2026-06-17 reinforcement] alignment)
- <span style="color:red">[O]</span> `execution_app`'s `interest.*` table SELECT privilege correction (execution buy flow alignment)
- <span style="color:red">[O]</span> `execution_app`'s interest sequence privilege / future default privileges correction (`ALTER DEFAULT PRIVILEGES IN SCHEMA interest GRANT SELECT ON TABLES TO execution_app`, etc.)
- <span style="color:red">[O]</span> `marketconnector_app`'s `legacy` schema USAGE privilege correction (Step 17 first failure → passed after the operator directly GRANTed / marketconnector_app-limited 1 exception of OD-DB-007 / R-DATA-011 new alignment)
- <span style="color:red">[O]</span> `marketconnector_app`'s `legacy.holdings` DML (SELECT / INSERT / UPDATE / DELETE) privilege correction (minimal privilege for legacy operation data update)
- <span style="color:red">[O]</span> `marketconnector_app`'s legacy sequence privilege / future default privileges correction
- <span style="color:red">[O]</span> `marketconnector_app`'s database search_path correction — `connector, execution, legacy, reference, public` (`ALTER ROLE marketconnector_app IN DATABASE portfolio SET search_path = ...`)
- <span style="color:red">[O]</span> added a validation SQL candidate for whether a bare-table-name-dependent legacy path (`holdings`) is discovered within search_path (`SET ROLE marketconnector_app; SELECT 1 FROM holdings LIMIT 1;`)
- <span style="color:red">[O]</span> 0 plaintext records of password / RDS endpoint hostname / account-id / actual ARN / account number on this date. R-DOCS-001 [2026-06-17 reinforcement (17-step E2E)] alignment

## This checklist's work safety constraints

- This inspection is confirmed directly by the operator or automatically validated with ReadOnly IAM ([`../../docs/kiro-readonly-validator-iam.md`](../../docs/kiro-readonly-validator-iam.md)). When capturing console output, mask the secret / endpoint host prefix.
- No 8 MS source / document modification.
- Actual AWS resource changes proceed only via [`./runbook.md`](./runbook.md). This checklist is inspection-only.
- When a decision value change is needed, record the change proposal in [`../_common/operator-decisions.md`](../_common/operator-decisions.md), not in this document.
- The secret value is never queried (GetSecretValue prohibited). Only DescribeSecret metadata is used.
- The account-id, RDS endpoint hostname, secret ARN, access key id are not recorded in this document. Internal identifiers already written in the operator notes are masked directly by the operator before external disclosure.
