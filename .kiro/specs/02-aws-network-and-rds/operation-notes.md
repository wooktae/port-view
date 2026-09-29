## 2026-06-08 AWS Foundation Execution Record

- Step 2 cost / environment decision confirmation complete: aws-paper low applied. No secret value recorded.
- CIDR: 10.0.0.0/16 (recommended)
- 2 AZs: ap-northeast-2a, ap-northeast-2c (recommended)
- 6 Subnets: public-a, public-b, app-a, app-b, data-a, data-b
- Internet Gateway: portfolio-igw
- NAT Gateways list is empty
- No EC2 instance serving the NAT role
- Route tables: rt-public, rt-app, rt-data
- Security group creation
 1) sgroup-marketconnector-ec2 (cannot start with sg)
 2) sgroup-port-view-ecs
 3) sgroup-strategy-tasks
 4) sgroup-crawler-tasks 
 5) sgroup-preprocessor-tasks
 6) sgroup-research-batch
 7) sgroup-rds-postgres
 8) sgroup-vpc-endpoints
 - Security Group rule population: complete
 - VPC Endpoint creation: complete
 - RDS Subnet Group creation: complete
 - RDS Parameter Group creation: complete
 - Secret name: /portfolio/paper/rds/master
 - Step 14-1 RDS creation in progress: PostgreSQL 16.14-R1, portfolio-paper-rds, portfolio_admin, self-managed password method selected. No actual password value recorded.
 - KMS Key ID: alias/aws/rds
 - RDS creation: complete
 - INTEREST_DB_HOST:
 - DB Endpoint: portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com
 - DB programming language: changed to PSQL (Windows)
 - Step 15 RDS access security / Parameter / Option confirmation complete: Publicly accessible = No, pg-portfolio-paper applied, backup retention 7 days, deletion protection enabled confirmed. No secret value recorded.


## 2026-06-08 Kiro ReadOnly Validation IAM Design and Automated Validation Result

- Kiro ReadOnly validation IAM design complete.
  - User name `portfolio-kiro-readonly-validator` / policy name `PortfolioKiroReadOnlyValidatorPolicy`
  - Detailed design reference: [`../../docs/kiro-readonly-validator-iam.md`](../../docs/kiro-readonly-validator-iam.md)
  - Policy includes: ec2 / rds / secretsmanager (metadata only) / iam / cloudwatch / logs / tag ReadOnly
  - Explicit Deny: `secretsmanager:GetSecretValue`, KMS Decrypt
- Automated validation run: uses only AWS CLI ReadOnly calls. No resource creation/modification/deletion. SecretsManager calls only DescribeSecret and does not call GetSecretValue.
- Automatically verifiable items — all match expected values.
  - VPC / 6 Subnets / IGW attach
  - NAT not created / NAT-role EC2 not created
  - 3 Route Tables (rt-public 0/0→IGW, rt-app/rt-data no external route)
  - 8 SG inventory / sg-rds-postgres inbound SG reference only (no 0/0:5432) / SSH 22 0/0 inbound 0 count
  - sg-vpc-endpoints inbound / operational SG outbound table match check
  - 6 VPC Endpoints available + Private DNS + sgroup-vpc-endpoints
  - RDS subnet group / parameter group / instance (class / storage / public access / backup retention / deletion protection / encryption / parameter group in-sync)
- Number of completed automated validation items: approximately 38 (Network 13 + SG 16 + VPC Endpoint 8 + RDS 11, including the automatable portions, some sub-items estimated by summation).
- Number of items requiring manual confirmation: approximately 31 (Pre-flight operator awareness / cost profile decision awareness / RDS PITR Console confirmation / Cost Validation Billing Dashboard / DB SQL not executed / Cutover agreement / Rollback not performed / Documentation human review, etc.).
- Number of mismatched items: 0. The existing `sgroup-marketconnector-ec2` outbound was a [X] candidate due to a list notation difference in the AWS CLI output structure, but the actual rule satisfies HTTPS 0.0.0.0/0 + sgroup-vpc-endpoints (443) + sgroup-rds-postgres (5432) all, so there is no operational mismatch.
- No secret value lookup (only DescribeSecret metadata called).
- No AWS resource creation / modification / deletion (only read-only API called).
- Validation artifact update targets: `.kiro/docs/kiro-readonly-validator-iam.md`, `.kiro/specs/02-aws-network-and-rds/validation-checklist.md`, `.kiro/specs/02-aws-network-and-rds/operation-notes.md` 3 files. No modification to the 8 MS code / README / AGENTS.md / CHANGELOG / docs / worklog.
- Follow-up work recommendation
  - The operator creates the `portfolio-kiro-readonly-validator` IAM User as portadmin + attaches the policy + issues an access key, then registers it in a separate profile in the Kiro environment.
  - The access key is not written in this document / repo / plaintext file (`[REDACTED]`).
  - At the time of 03 / 06 spec progress, after executing DB / schema / role SQL, update the Section 6 manual confirmation items to [O].
  - After registering the Cost Anomaly Detection alert, update the Section 8 items to [O].


## 2026-06-08 validation-checklist Status Label 4-Type Reclassification

- Reclassified the validation-checklist.md status labels from 3 types to 4 types. Removed all existing <span style="color:black">[확인 필요]</span> and split them into <span style="color:green">[Kiro 후속 작업 필요]</span> or <span style="color:black">[운영자 확인 필요]</span>.
- Since Rollback Validation is currently in a normal state of rollback not performed, all were organized in the form `[O] Rollback 미수행 — 현재 대상 아님`. At the actual future rollback time, [O]/[X] will be updated via AWS API re-validation.
- Among the Cost / Documentation items, those that Kiro can confirm via additional ReadOnly calls / file grep / document comparison were classified as `[Kiro 후속 작업 필요]`. In this session, ALB 0 count (elbv2 / classic elb), Cost Anomaly Detection alert 0 count, repo grep (AKIA / Slack webhook / JWT / plaintext secret 0 count) were additionally auto-validated and immediately confirmed as [O] or [운영자 확인 필요].
- Actual operator judgment / cost approval / external exposure / human awareness / future agreement were separated as `[운영자 확인 필요]` (operator-decisions / risk-register awareness, cost line decision, cutover agreement, public document masking, Cost Anomaly Detection registration decision, DB SQL agreement, etc.).
- Correction of an existing [X] item: `sgroup-marketconnector-ec2` outbound satisfies all the requirements of the design.md table (0.0.0.0/0 broker, sg-rds-postgres 5432, VPC Endpoints 443) and only the notation order differs. Therefore it was reclassified as [O] rather than an [X] mismatch.
- Change summary (label counts, excluding label-rule description lines): [O] 74 / [X] 0 / [Kiro 후속 작업 필요] 6 / [운영자 확인 필요] 20. (Includes Rollback Validation 10 [O].)
- No secret value lookup. SecretsManager uses metadata only.
- No AWS resource creation / modification / deletion (only read-only API called).
- No modification to the 8 MS code / README / AGENTS.md / CHANGELOG / docs / worklog (`git status --short` result shows no changes outside .kiro).
- Work artifact update targets: `.kiro/specs/02-aws-network-and-rds/validation-checklist.md`, `.kiro/specs/02-aws-network-and-rds/operation-notes.md`. account-id / RDS endpoint hostname / secret ARN / access key id are not newly output by this work. Internal identifiers the operator already wrote at the top of this file are recommended to be masked before external disclosure.


## 2026-06-08 [Kiro 후속 작업 필요] Item Additional Validation and [O] Promotion

- Target: the 6 items left as [Kiro 후속 작업 필요] in the previous session.
- Result: 4 items promoted to [O], 2 items kept as [Kiro 후속 작업 필요] (stating the reason to retry after Cost Explorer data accumulates).
- Promoted items
  - §1 Confirm whether it is an aws-paper target task — [O] by consistent comparison of operator-decisions.md At a Glance OD-ENV-003 = `aws-paper` 🟢 확정 with this spec.
  - §9 operator-decisions.md matches this spec's decision states — [O] as all core decisions in the OD-ENV / OD-NET / OD-RDS / OD-DB / OD-CUT / OD-SAFE categories are consistent with this spec's design / runbook / decision-matrix (0 mismatches).
  - §9 risk-register.md mitigation check — [O] as the mitigation text of R-NET-001~003 / R-SEC-001 / R-DATA-001~002 / R-AUTO-001~002 / R-DOCS-001 / R-COST-001~002 / R-SEC-002~003 is consistent with this spec's design / runbook / validation-checklist controls.
  - §9 secret-position [REDACTED] consistency — grep of the following patterns over the `.kiro/**/*.md` tree all 0 count. No [REDACTED-CANDIDATE] output. Promoted to [O]. Outside the repo (console captures / personal notes / external PC files) is kept separated as §1 [운영자 확인 필요].
    - `(AKIA|ASIA)` access key id
    - Slack incoming webhook URL / JWT
    - `(PASSWORD|SECRET|TOKEN|APP_KEY|APP_SECRET)=값`
    - `aws_secret_access_key=값`
    - `KIS*KEY|SECRET|TOKEN=값`
    - account number plaintext pattern
- Retained items (Kiro 후속 작업 필요)
  - §8 Endpoint-Hours billing matching — Cost Explorer ReadOnly (`ce get-cost-and-usage`) USAGE_TYPE group query result is 0 count at this point (presumed data accumulation lag). Will retry in a few days and update to [O] / [X].
  - §8 RDS-InstanceUsage billing matching — same reason. RDS instance class / Single-AZ are in [O] state by automated validation, but billing line matching is retried after data accumulates.
- Change summary (label counts, excluding label-rule description lines): [O] 78 / [X] 0 / [Kiro 후속 작업 필요] 2 / [운영자 확인 필요] 20.
- Summary of newly discovered risks or items requiring operator confirmation
  - 0 new risks identified. No additional row needed in risk-register.md.
  - The one line of RDS endpoint hostname at the top of operation-notes.md and the secret name `/portfolio/paper/rds/master` are not secret values but carry the possibility of identifier exposure upon external disclosure. As a reinforcement of the §1 [운영자 확인 필요] item "Confirm that no actual secret value is exposed in this spec / operator notes / console captures", the operator needs to mask them before external disclosure (within R-DOCS-001 mitigation scope).
- No secret value lookup (uses only DescribeSecret metadata. GetSecretValue 0 times).
- No AWS resource creation / modification / deletion (only read-only describe / list / get / cost explorer get-cost-and-usage called).
- No modification to the 8 MS code / README / AGENTS.md / CHANGELOG / docs / worklog.
- Work artifact update targets: `.kiro/specs/02-aws-network-and-rds/validation-checklist.md`, `.kiro/specs/02-aws-network-and-rds/operation-notes.md`. account-id / RDS endpoint hostname / secret ARN / access key id are not newly output by this work.

- Operational decision: Creation of the `portfolio-kiro-readonly-validator` IAM User is deferred for now, and Kiro validation proceeds with the `terraform` IAM User, which is the existing AWS CLI default credential. However, since `terraform` has AdministratorAccess permission, AWS resource creation/modification/deletion commands are prohibited during Kiro work and only ReadOnly queries are allowed. Separation of a ReadOnly-only IAM will be reconsidered in a future security cleanup phase.


## 2026-06-09 DB Role / Privilege Separation First Application

The operator directly executed the §4 SQL of this spec [`./db-roles-and-grants.md`](./db-roles-and-grants.md), and confirmed the result with the §5 validation SQL. The SQL body / actual password / endpoint / secret value are not recorded in this document.

### owner / membership

- `portfolio_owner` creation complete (NOLOGIN).
- `portfolio_owner` membership grant to `portfolio_admin` complete. Subsequently entered a state where `ALTER SCHEMA ... OWNER TO portfolio_owner` can be executed.
- Ownership transfer of the 9 domain schemas (`reference`, `interest`, `preprocessor`, `research`, `decision`, `execution`, `connector`, `ops`, `legacy`) to `portfolio_owner` complete.
- The `public` schema is not changed (RDS policy / compatibility retained).
- The owner of existing table / sequence / index remains as `portfolio_admin` (the restore execution account).
  - `REASSIGN OWNED BY portfolio_admin TO portfolio_owner` was not executed in this session.
  - §4.5 default privileges are not automatically applied to existing objects.
  - In this session, the privilege matrix is applied only via §4.4 explicit GRANT (objects newly created afterward are subject to default privileges).
  - Whether to bulk-transfer ownership in a follow-up session is managed separately by operator decision.

### app role 7 types

- `marketconnector_app`, `view_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `research_app`, `execution_app` creation complete.
- All 7 roles confirmed `LOGIN = true`, `SUPERUSER / CREATEDB / CREATEROLE / REPLICATION / BYPASSRLS = false`.
- All 7 roles granted `GRANT CONNECT ON DATABASE portfolio` + `GRANT USAGE ON SCHEMA public` complete.

### GRANT Matrix / DEFAULT PRIVILEGES / search_path

- §4.4 schema-level GRANT (USAGE / SELECT / INSERT-UPDATE-DELETE / sequence USAGE-SELECT) application complete. The legacy schema is intentionally omitted.
- §4.5 `ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA <9개>` application complete. legacy excluded. Subsequently, when new objects are created under `portfolio_owner`, the privilege matrix is automatically applied.
- §4.6 `ALTER ROLE ... SET search_path` 7 applications complete. Matches each MS README definition (the legacy entry is included in search_path for compatibility retention but, since USAGE is not granted, actual access is blocked).

### Validation SQL Result Summary (§5)

- §5.1 role / attribute: existence of 7 app roles + `portfolio_owner` + `portfolio_admin` all confirmed. superuser / createdb / createrole / replication / bypassrls of the 7 app roles all false.
- §5.1 search_path: all 7 roles match the §3 table of this document.
- §5.2 schema USAGE / CREATE matrix: 0 mismatches with the §2 table. `legacy` USAGE = false confirmed for all app roles.
- §5.3 table privilege summary / sequence privilege summary: 0 mismatches with the §2 table.
- §5.4 actual connection validation
  - `marketconnector_app`: connection success. `connector` query success, `execution` query success, `legacy` USAGE = false confirmed. INSERT / UPDATE / DELETE attempt on `execution` schema → permission denied (matches expected value).
  - `execution_app`: connection success. `execution` / `decision` / `connector` query success, `legacy` USAGE = false confirmed. INSERT / UPDATE / DELETE possible on `execution` schema (matches expected value).
  - `view_app`: connection success. `execution` / `connector` / `decision` query success, `legacy` USAGE = false confirmed. INSERT / UPDATE / DELETE attempt on `execution` schema → permission denied (matches expected value, the view write scope is currently limited to `ops`).

### This Session's Safety Constraint Check

- No secret value lookup. No actual password / endpoint / account-id / account number / token / app key / app secret recorded. Only `[REDACTED]` or placeholder used throughout.
- No AWS resource creation / modification / deletion. This work performs only SQL execution inside RDS and recording of SQL results.
- No modification to the 8 MS code / README / AGENTS.md / CHANGELOG / docs / worklog.
- Work artifact update targets: this file and [`./validation-checklist.md`](./validation-checklist.md), 2 files.

### Follow-up Work Recommendation

- Formal storage of the 7 app role passwords (Secrets Manager / SSM SecureString) and the IAM matrix are organized in the follow-up spec `06-secrets-and-iam`.
- Decide whether to bulk-transfer ownership of existing objects (`portfolio_admin`-owned). Depending on the decision, choose between §4.2.1 option A / option B of [`./db-roles-and-grants.md`](./db-roles-and-grants.md).
- Matrix changes (legacy not granted, `marketconnector_app` execution reduced to R only) are proposed for update in the OD-DB category of [`../_common/operator-decisions.md`](../_common/operator-decisions.md).


## 2026-06-09 Local → RDS Migration & RDS Recreation Execution Record

This section records the result of the Local PostgreSQL → aws-paper RDS migration the operator directly performed on 2026-06-09. This document does not write the actual endpoint hostname / password / secret value / account-id / account number (only `[REDACTED]` or placeholder used).

### Execution Summary

- Execution flow: Local Windows PC → S3 temporary migration bucket → aws-paper MarketConnector EC2 → private RDS PostgreSQL.
- Restore Runner: aws-paper MarketConnector EC2 (Amazon Linux 2023, public subnet, EIP attach). Kiro performed only ReadOnly validation and documentation in this work.
- Result: schema / table / index / sequence / FK / trigger / per-table row count all match the local baseline (diff 0).

### 1. Local Backup and Baseline Establishment

- pg_dump result: `portfolio_full_20260609.dump` (format custom + gzip), size 422,334,494 bytes.
- Storage location: `C:\Workspaces\db-backup\portfolio_20260609\` (local PC).
- Baseline: per-schema table count, per-table row count snapshot, major object count (index / trigger / sequence / FK), total table count 81.

### 2. private RDS Direct Connection Attempt and Decision

- Timeout when connecting directly to the RDS endpoint from the local PC. Since the RDS endpoint resolves to a private IP and RDS Publicly accessible = No, judged as expected behavior.
- Decision: use the MarketConnector EC2 as the RDS restore runner. Do not directly allow the local PC IP in the RDS Security Group. Keep RDS Public access = No.

### 3. MarketConnector EC2 Preparation (restore runner)

- Newly created 1 EC2 for aws-paper: Amazon Linux 2023, placed in public subnet, EIP attach.
- Security Group: allow RDS PostgreSQL 5432 inbound source as the MarketConnector EC2 SG. No 0.0.0.0/0 5432 allowance.
- Connection: EC2 Instance Connect success. Direct local SSH connection deferred due to possible outbound 22 restriction.
- IAM Role: attached the default managed policy for SSM Session Manager transition + granted temporary migration bucket read permission. The Access Key is not stored inside the EC2.
- Packages: OS update complete, AWS CLI confirmed provided by default. For the PostgreSQL client, after first installing 15.18, a dump archive header version mismatch was confirmed → removed the 15 client and switched to the 18.4 client (psql 18.4 / pg_restore 18.4). private RDS psql connection from the EC2 success.

### 4. dump File Transfer and Metadata Confirmation

- Local dump → S3 temporary bucket upload → download to MarketConnector EC2.
- Integrity: local / S3 / EC2 dump file size 422,334,494 bytes match.
- pg_restore --list result: TOC Entries 812, line count 823, dump source PostgreSQL 18.1, dump format CUSTOM + gzip compression confirmed.

### 5. Major version mismatch Discovery and RDS Recreation

- Existing RDS engine PostgreSQL 16.14 confirmed. Restoring dump source 18.1 to 16.14 is a downgrade major version restore, so judged as risky.
- Decision: delete the existing PostgreSQL 16.14 RDS and recreate on a PostgreSQL 18.4 basis. Keep RDS Public access = No, specify initial database name `portfolio`.
- Result: new RDS `portfolio` DB connection success, version PostgreSQL 18.4 confirmed.

### 6. First restore Failure and Option Correction

- First attempt result: `role "postgres" does not exist` error. The cause is judged to be that the local dump's object owner is `postgres` but the RDS has no `postgres` role.
- Correction: after drop / recreate of the `portfolio` DB, re-executed with the `pg_restore --no-owner --no-privileges` option.
- Result: completed without error. The RDS object owner was organized on the basis of the restore execution account (`portfolio_admin`).

### 7. Consistency Validation

- per-schema table count 81 match. table / index / sequence / FK (33) / trigger (23) / per-table row count clean CSV 82 lines all diff 0 with the local baseline.
- CRLF / LF differences were normalized with the `--strip-trailing-cr` option before comparison.

### 8. This Session's Safety Constraint / Sensitive Information Check

- No new recording of actual password / secret value / endpoint hostname / account-id / account number / token / app key / app secret / webhook URL. Only `[REDACTED]` or placeholder used throughout.
- AWS resource changes: EC2 new creation and existing RDS deletion / recreation the operator directly performed. Kiro did not change AWS resources by this work.
- No modification to the 8 MS code / README / AGENTS.md / CHANGELOG / docs / worklog.
- Work artifact update targets: this file + [`./validation-checklist.md`](./validation-checklist.md). Decision changes (legacy not granted / marketconnector_app execution R-only / view_app execution R-only / REASSIGN OWNED not executed) were separately accumulated in OD-DB-007 ~ OD-DB-010 of [`../_common/operator-decisions.md`](../_common/operator-decisions.md).

### 9. Lessons / Follow-up Recommendation

- Prevent PostgreSQL major version mismatch in advance at the starting stage by comparing the dump's `pg_restore --list` output with the RDS engine version (R-DATA-003).
- private RDS is not connected directly from local. Always connect via EC2 + SSM Session Manager or an EIP-based restore runner (R-NET-004).
- If the dump owner role and the RDS role differ, bypass with `--no-owner --no-privileges`, and perform privilege / owner cleanup with separate SQL (this spec [`./db-roles-and-grants.md`](./db-roles-and-grants.md)) (R-DATA-004).
- Since `REASSIGN OWNED BY portfolio_admin TO portfolio_owner` was not executed in the first application, default privileges are automatically applied to new objects but not to existing objects. Whether to additionally transfer is managed separately as a follow-up by operator decision.


## 2026-06-13 Local-to-AWS Paper RDS Operation Mode Organization

This section accumulates in the 02 spec operation notes the Local development / AWS Paper RDS operation principles the operator decided on 2026-06-13.

- No change to the RDS Public access not-allowed policy (02 spec first application result / R-SEC-001 / R-NET-004 consistent).
- Codify the flow and guard combination when connecting to AWS Paper RDS from the local development environment.
- The same principle is synchronized to 04 spec 2026-06-13 §5 (Local-to-AWS Paper RDS) and OD-ENV-006 / OD-ENV-007 / OD-ENV-008 of [`../_common/operator-decisions.md`](../_common/operator-decisions.md).

### 1. Paper Environment source of truth

1. Paper environment source of truth: complete
   1) Fixed as the single AWS Paper RDS source of truth.
   2) Even when running locally, if `PORT_ENVIRONMENT=paper`, it looks at AWS Paper RDS.
   3) Even when running on AWS, the same AWS Paper RDS is used.
   4) Local PostgreSQL is used only for `LOCAL_DEV` fixture / experiment / backup reference.
   5) Order / fill / position data merge or synchronization between the local DB and AWS Paper RDS is not performed (R-DATA-007 consistent, added as follow-up).

### 2. Environment Distinction Labels

1. `LOCAL_DEV`: complete
   1) local PostgreSQL usable.
   2) Development / experiment / fixture only.
   3) Not actual paper operation.
   4) Order execution prohibited.
2. `PAPER`: complete
   1) AWS Paper RDS used.
   2) Local execution also uses AWS Paper RDS.
   3) AWS execution also uses AWS Paper RDS.
   4) paper order / fill / position source of truth.
3. `LIVE`: follow-up
   1) Follow-up design target (10 spec integration).
   2) The live operation source of truth needs to be separated from paper.

### 3. SSM Port Forwarding Direction

1. RDS exposure policy: complete
   1) RDS kept Private (02 spec first application result / OD-NET-009 / R-SEC-001 consistent).
   2) RDS Public access allowance prohibited.
2. When connecting to AWS Paper RDS from local: complete
   1) Use SSM Port Forwarding.
   2) Locally connect via a port like `localhost:15433`, but the actual target is AWS Paper RDS.
   3) The DB host being `localhost` must not be unconditionally judged as the local DB.
   4) The actual paper order execution guard is judged by the `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` combination (MarketConnector executor `--execute` guard consistent).

### 4. Example Environment Variables (Local PC + SSM Port Forwarding)

1. Environment variable example: complete
   1) `PORT_ENVIRONMENT=paper`
   2) `PORT_DB_TARGET=aws-paper`
   3) `INTEREST_DB_HOST=localhost`
   4) `INTEREST_DB_PORT=15433`
   5) `INTEREST_DB_NAME=portfolio`
2. Notation policy: complete
   1) Plaintext recording of password / account / actual RDS endpoint hostname / actual SSM Port Forwarding session id prohibited (R-DOCS-001 consistent).
   2) Only placeholder is used in this note / follow-up spec artifacts.

### 5. Prohibited Items

1. paper order execution in the local environment prohibited: complete
   1) paper order execution on local PostgreSQL prohibited.
2. synchronization between local DB and AWS Paper RDS prohibited: complete
   1) `connector_order_request` merge prohibited.
   2) `connector_fill` merge prohibited.
   3) `strategy_execution_order` merge prohibited.
   4) `strategy_position_state` merge prohibited.

### 6. Follow-up Handover

1. SSM Port Forwarding runbook organization: follow-up
   1) Formally document the SSM Port Forwarding → AWS Paper RDS connection procedure in the 02 spec runbook or a separate operation note.
2. Environment variable check of all MS: follow-up
   1) Check `PORT_ENVIRONMENT` / `PORT_DB_TARGET` consistency in the environment variable inventory of the 8 MS.
3. aws-live integration timing: follow-up
   1) The source of truth of the `LIVE` label is separated into an RDS distinct from paper (decided at the 10 spec integration timing).


## 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding Connection Validation + Runbook

This section, separately from the same-date (2026-06-13) Local-to-AWS Paper RDS Operation Mode Organization (previous section), accumulates the result of the first empirical validation of the SSM Port Forwarding-based local → AWS Paper RDS connection the operator directly performed.

- This section is simultaneously used as the first body of the Local-to-AWS Paper RDS SSM Port Forwarding Runbook.
- No change to the RDS Public access not-allowed policy (R-SEC-001 / R-NET-004).
- 0 AWS / RDS / IAM changes — on this date, only read-only AWS API calls, an SSM Port Forwarding session + Python `psycopg2` connection validation were performed.

### 1. Prerequisite Tool Confirmation

1. AWS CLI confirmation: complete
   1) Command: `aws --version`
   2) Result: `aws-cli/2.27.50 Python/3.13.4 Windows/11 exe/AMD64`
2. Session Manager Plugin confirmation: complete
   1) On first run, `session-manager-plugin` PowerShell recognition failed → reconfirmed after Plugin installation.
   2) Command: `session-manager-plugin --version`
   3) Result: `1.2.814.0`
   4) Judgment: SSM Port Forwarding session can be started.

### 2. SSM Port Forwarding Standard Waypoint Decision

1. Running EC2 check: complete
   1) Command: `aws ec2 describe-instances`
   2) `portfolio-paper-marketconnector-ec2`
       - instance id: `i-0fce77927b7397b88`
       - private ip: `10.0.0.181`
       - state: `running`
   3) `portfolio-paper-crawler-worker`
       - instance id: `i-0ff768ea639a91355`
       - private ip: `10.0.0.169`
       - state: `running`
2. Standard waypoint decision: complete
   1) SSM Port Forwarding waypoint = `portfolio-paper-marketconnector-ec2` (instance id `i-0fce77927b7397b88`).
   2) Decision reason:
       - This EC2 holds a history of validating access to the same RDS as the 2026-06-09 RDS restore runner (previous section §3 consistent).
       - The operational waypoint role for the Paper DB that MarketConnector / Strategy Execution / View will look at is natural (03 spec consistent).
       - `portfolio-paper-crawler-worker` is kept in the KRX GUI / Windows worker role (08 spec consistent — this EC2 is not used as an SSM Port Forwarding waypoint).
   3) Decision lock: updated [`../_common/operator-decisions.md`](../_common/operator-decisions.md) with OD-NET-010 (SSM Port Forwarding standard waypoint).

### 3. Target EC2 SSM Managed Node Check

1. SSM Online status confirmation: complete
   1) Command: `aws ssm describe-instance-information --filters "Key=InstanceIds,Values=i-0fce77927b7397b88"`
   2) Result:
       - instance id: `i-0fce77927b7397b88`
       - ping status: `Online`
       - platform type: `Linux`
       - agent version: `3.3.4515.0`
   3) Judgment: usable as an SSM Port Forwarding target.

### 4. AWS Paper RDS endpoint Confirmation

1. RDS metadata query: complete
   1) Command: `aws rds describe-db-instances --db-instance-identifier portfolio-paper-rds`
   2) Result:
       - DB name: `portfolio`
       - endpoint hostname: `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com`
       - port: `5432`
       - publicly accessible: `False`
       - status: `available`
   3) Judgment: RDS kept Private (OD-NET-009 / R-SEC-001 / R-NET-004 consistent). Local connection possible via SSM Port Forwarding without Public exposure.

### 5. SSM Port Forwarding Tunnel Open

1. Standard command: complete
   1) Command: `aws ssm start-session --target i-0fce77927b7397b88 --document-name AWS-StartPortForwardingSessionToRemoteHost --parameters host="portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com",portNumber="5432",localPortNumber="15433"`
2. Session result: complete
   1) session id: `terraform-vjp3fv3nz73konetcevdzjh9de`
   2) local port: `15433`
   3) remote RDS port: `5432`
   4) message: `Port 15433 opened`
   5) message: `Waiting for connections...`
3. Connection structure: complete
   1) Local PC `localhost:15433`
   2) → SSM Session Manager tunnel
   3) → `portfolio-paper-marketconnector-ec2` (instance id `i-0fce77927b7397b88`)
   4) → AWS Paper RDS `portfolio-paper-rds:5432`

### 6. Local psql client Not Installed / PATH Not Registered Confirmation

1. First attempt: failed
   1) Command: `psql -h localhost -p 15433 -U portfolio_admin -d portfolio`
   2) Result: `psql` command recognition failed in PowerShell.
   3) Judgment: not an AWS / SSM / RDS consistency problem. A local PostgreSQL client PATH not-registered problem (R-AUTO-013 consistent, added as follow-up).
2. Correction direction: complete (immediate action deferred)
   1) Do not proceed with immediate psql installation.
   2) Proceed with connection confirmation using Python `psycopg2` (§7 / §8 / §9).
   3) Formal psql client installation / PATH registration is a follow-up (`_common/followups-overview.md` 2026-06-13 §1).

### 7. Python `psycopg2` Availability Confirmation

1. Module import check: complete
   1) Command: `python -c "import psycopg2; print('psycopg2 OK')"`
   2) Result: `psycopg2 OK`
   3) Judgment: Python-based DB connection test possible.

### 8. `portfolio_admin` Connection Confirmation

1. Connection parameters: complete
   1) host: `localhost`
   2) port: `15433`
   3) dbname: `portfolio`
   4) user: `portfolio_admin`
   5) password: uses the `PGPASSWORD` environment variable (0 plaintext exposure, R-DOCS-001 consistent)
2. Connection result: complete
   1) Output summary (`current_user`, `current_database`, `inet_server_addr`, `inet_server_port`):
       - `('portfolio_admin', 'portfolio', '10.0.20.165', 5432)`
   2) Judgment:
       - AWS Paper RDS connection success from the local PC via the SSM tunnel.
       - actual RDS private IP = `10.0.20.165` (RDS endpoint resolve result).
       - actual RDS port = `5432`.
       - It was first empirically shown that the actual target of `localhost:15433` is AWS Paper RDS (OD-ENV-007 consistent).

### 9. `execution_app` Connection Confirmation (Strategy Execution Porting Prerequisite Validation)

1. Connection parameters: complete
   1) host: `localhost`
   2) port: `15433`
   3) dbname: `portfolio`
   4) user: `execution_app`
   5) password: uses the `PGPASSWORD` environment variable (0 plaintext exposure)
2. Connection result: complete
   1) `current_user`: `execution_app`
   2) `current_database`: `portfolio`
   3) `search_path`: `execution, decision, research, connector, preprocessor, interest, reference, legacy, public`
3. Judgment: complete
   1) The first validation of AWS Paper RDS app role connection at the stage before Strategy Execution (`port_strategy_execution`) AWS porting passed.
   2) The search_path of `execution_app` is consistent with [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §3 / OD-DB-006 / OD-DB-007 (legacy is included in search_path but actual access is blocked since USAGE is not granted).
   3) This date's validation is limited to SELECT queries (`current_user` / `current_database` / `inet_server_addr` / `inet_server_port` / `search_path`) — INSERT / UPDATE / DELETE / DDL 0 count.

### 10. Local-to-AWS Paper RDS SSM Port Forwarding Runbook (First Body)

1. Prerequisite check stage: [확인]
   1) Local AWS CLI confirmation — `aws --version`
   2) Session Manager Plugin confirmation — `session-manager-plugin --version`
   3) Target EC2 running status confirmation — `aws ec2 describe-instances --instance-ids i-0fce77927b7397b88`
   4) Target EC2 SSM Online status confirmation — `aws ssm describe-instance-information --filters "Key=InstanceIds,Values=i-0fce77927b7397b88"`
   5) AWS Paper RDS endpoint / status confirmation — `aws rds describe-db-instances --db-instance-identifier portfolio-paper-rds`
2. SSM Port Forwarding tunnel open stage: [실행]
   1) Command:
       - `aws ssm start-session --target i-0fce77927b7397b88 --document-name AWS-StartPortForwardingSessionToRemoteHost --parameters host="portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com",portNumber="5432",localPortNumber="15433"`
   2) Success output:
       - `Port 15433 opened`
       - `Waiting for connections...`
   3) tunnel maintenance conditions:
       - Do not close this PowerShell / terminal window (R-AUTO-012 consistent).
       - Additional work is performed in a separate PowerShell window.
3. Local environment variable standard export stage: [준비]
   1) Plaintext recording of password / secret in this spec / other artifact bodies prohibited (R-DOCS-001 consistent).
   2) Standard environment variable keys:
       - `PORT_ENVIRONMENT=paper`
       - `PORT_DB_TARGET=aws-paper`
       - `INTEREST_DB_HOST=localhost`
       - `INTEREST_DB_PORT=15433`
       - `INTEREST_DB_NAME=portfolio`
   3) `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD` are separated by per-MS app role (§10.5).
4. Connection validation stage: [확인]
   1) Python `psycopg2` check — `python -c "import psycopg2; print('psycopg2 OK')"`
   2) `portfolio_admin` connection confirmation:
       - `current_user` = `portfolio_admin`
       - `current_database` = `portfolio`
       - `inet_server_addr` = `10.0.20.165`
       - `inet_server_port` = `5432`
   3) Per-MS app role connection confirmation (§10.5 table) — validate `current_user` / `current_database` / `search_path` output.
   4) Only when the psql client is in PATH, `psql -h localhost -p 15433 -U <app_role> -d portfolio` is also usable. At this date's point, PATH not registered (R-AUTO-013 consistent).
5. Per-MS app role mapping: [준비]
   1) Strategy Execution: `execution_app`
   2) MarketConnector: `marketconnector_app`
   3) Interest Crawler: `crawler_app`
   4) Interest Preprocessor: `preprocessor_app`
   5) View: `view_app`
   6) Plaintext recording of password / secret value in this runbook body / operator notes / console captures / logs prohibited (R-DOCS-001 consistent).
6. Success criteria: [확인]
   1) The SSM tunnel message is maintained in the `Port 15433 opened` state.
   2) AWS Paper RDS connection possible with `portfolio_admin`.
   3) AWS Paper RDS connection possible with `execution_app` (this date's first validation complete).
   4) `execution_app` search_path = `execution, decision, research, connector, preprocessor, interest, reference, legacy, public` match.
   5) The RDS `PubliclyAccessible` value is maintained as `False`.
7. Failure / recovery: [복구]
   1) If the `Port 15433 opened` message does not appear, retry after checking SSM Plugin installation / EC2 SSM Online / IAM Role / the 5 VPC Endpoints (`com.amazonaws.<region>.ssm` / `ssmmessages` / `ec2messages`).
   2) If the tunnel window closed, restart a new session with the same command (R-AUTO-012 consistent).
   3) When `password authentication failed` occurs at the connection stage, check the consistency of the environment's `PGPASSWORD` value / app role password (plaintext output prohibited).
   4) When `relation does not exist` / `permission denied` occurs, recheck 02 spec [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4 GRANT matrix / §5 validation SQL (R-DATA-005 consistent).

### 11. Safety / Security Check Result

1. 0 AWS resource creation / modification / deletion. Uses only read-only AWS API (`aws ec2 describe-instances` / `aws ssm describe-instance-information` / `aws rds describe-db-instances`) + an SSM Port Forwarding session (`aws ssm start-session` only) + Python `psycopg2` SELECT queries.
2. 0 RDS DDL/DML. 0 INSERT / UPDATE / DELETE / DDL. 0 broker / KIS / order / fill / Daily Batch entrypoint calls.
3. 0 plaintext recording in this note of actual password / secret value / KIS app key / KIS app secret / account number / token / account-id / actual IAM access key id / actual secret ARN / EIP. All `[REDACTED]` or placeholder.
4. Operational identifiers included in plaintext in this note / runbook body:
   - instance id: `i-0fce77927b7397b88` / `i-0ff768ea639a91355`
   - private IP: `10.0.0.181` / `10.0.0.169` / `10.0.20.165`
   - local port: `15433`
   - SSM session id: `terraform-vjp3fv3nz73konetcevdzjh9de`
   - RDS endpoint hostname: `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com`

   Per the user-specified policy, operational identifiers may be recorded in work logs / runbook, but sensitive information (secret value / password / token / account-id / KIS credentials) must never be recorded in plaintext.
5. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog due to this date's work.
6. The session id is the identifier (temporary value) of this date's validation session, and a different identifier is generated when the same session is restarted. The session id in this note is merely a factual record for reproduction / tracking purposes and is not a secret.

### 12. Outside This Date's Scope / Follow-up Handover

1. Formal psql client installation / PATH registration: follow-up (R-AUTO-013).
2. Paper mode DB environment variable inventory check of all MS: follow-up (`_common/followups-overview.md` 2026-06-13 §2).
3. Strategy Execution (`port_strategy_execution`) AWS porting main phase: follow-up (04 spec follow-up phase).
4. MarketConnector new executor (`connector_strategy_order_execute.py`) EC2 deployment candidate zip / tag production: follow-up (03 spec follow-up phase or 07 spec).
5. `READY -> REQUESTED -> SUBMITTED` end-to-end dry / integration validation with weekday or safe test data: follow-up (R-AUTO-009 / R-AUTO-010 / R-AUTO-011 consistent).
6. EventBridge Scheduler regular trigger / Step Functions hybrid orchestration: 04 / 08 spec follow-up phase.
7. SSM Port Forwarding session automatic keep-alive / reconnect: operator confirmation (currently a manual restart policy, R-AUTO-012 consistent).


## 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding Reinforcement (psql 18 client + pgAdmin4 Connection Validation)

This section is a follow-up to §1 ~ §12 of the same-date previous section (`## 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook`).

- Accumulates the result of the operator first empirically validating AWS Paper RDS connection with 2 additional clients (local PostgreSQL 18 `psql.exe` direct path execution + pgAdmin4) over the same SSM Port Forwarding tunnel.
- The SSM tunnel itself is reused (same command / same local port `15433` / a new session id possible).
- No change to the RDS Public access not-allowed policy / OD-NET-009 / R-SEC-001 / R-NET-004 body.
- 0 AWS / RDS / IAM changes — uses only read-only AWS API + an SSM Port Forwarding session + psql / pgAdmin4 SELECT queries.

### 1. Local PostgreSQL 18 psql client Connection Validation

1. Local PostgreSQL installation inventory: complete
   1) Path: `C:\Program Files\PostgreSQL`
   2) Version folders: `17`, `18`
2. Direct path execution command: complete
   1) Command: `& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -p 15433 -U portfolio_admin -d portfolio`
   2) General `psql` PATH registration is still incomplete — work proceeds with full path direct execution (R-AUTO-013 mitigation reinforcement consistent).
3. Connection result: complete
   1) psql client: `18.1`
   2) server: `18.4`
   3) SSL connection: `TLSv1.3`
   4) `current_user`: `portfolio_admin`
   5) `current_database`: `portfolio`
   6) `inet_server_addr`: `10.0.20.165`
   7) `inet_server_port`: `5432`
4. Password input-related memo: operator confirmation
   1) No password prompt on connection — judged to be because psql used the `PGPASSWORD` environment variable of the PowerShell session.
   2) If needed, the in-session password environment variable can be removed with `Remove-Item Env:PGPASSWORD`.
   3) 0 plaintext recording in this note / console captures / logs (R-DOCS-001 consistent).
5. Judgment: complete
   1) Local PostgreSQL 18 psql client → SSM tunnel → AWS Paper RDS 18.4 connection first empirically shown.
   2) client major 18 / full 18.4 / server 18.4 consistent (R-DATA-003 mitigation consistent / 03 spec design §5 consistent).

### 2. pgAdmin4 Connection Validation

1. Server registration parameters: complete
   1) Host name/address: `localhost`
   2) Port: `15433`
   3) Maintenance database: `portfolio`
   4) Username: `portfolio_admin`
   5) Password: `portfolio_admin` password — 0 plaintext recording in this note / console captures / logs (R-DOCS-001 consistent).
   6) SSL mode: `Prefer` or default TLS automatic connection.
2. SSM tunnel-side confirmation: complete
   1) When pgAdmin4 connects, confirmed the `Connection accepted for session [...]` message output in the tunnel window of §5 of the same-date previous section.
   2) On tunnel window termination, the pgAdmin4 connection is also immediately severed (R-AUTO-012 consistent).
3. Confirmation SQL: complete
   1) `select current_user, current_database(), inet_server_addr(), inet_server_port(), current_setting('search_path');`
4. Confirmation result: complete
   1) `current_user`: `portfolio_admin`
   2) `current_database`: `portfolio`
   3) `inet_server_addr`: `10.0.20.165`
   4) `inet_server_port`: `5432`
   5) `search_path`: `"$user", public`
       - Note: `portfolio_admin` is not a target of `ALTER ROLE ... SET search_path` at this date's point — only the app roles (7 types) have search_path applied (OD-DB-006 consistent / [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §3 consistent). Therefore the `"$user", public` output of `portfolio_admin` is normal.
5. schema / table query availability confirmation: complete
   1) `connector.connector_account`
   2) `connector.connector_order_request`
   3) `connector.connector_order_event`
   4) `connector.connector_fill`
   5) `decision.strategy_daily_position_decision`
   6) `decision.strategy_daily_run`
   7) `execution.connector_signal_order_map`
   8) `execution.strategy_execution_order`
   9) `execution.strategy_execution_plan`
   10) `execution.strategy_position_state`
   11) `interest.interest_pool`
   12) `ops.strategy_daily_batch_run`
   13) `ops.strategy_daily_batch_step_log`
   14) `preprocessor.pre_agency_analysis`
   15) `preprocessor.pre_news_daily_feature`
   16) This date's validation only performs confirmation of SELECT availability — INSERT / UPDATE / DELETE / DDL 0 count.
6. Judgment: complete
   1) pgAdmin4 → SSM tunnel → AWS Paper RDS connection first empirically shown.
   2) pgAdmin4 connects via `localhost:15433` but the actual target is AWS Private RDS (`10.0.20.165:5432`).
   3) While the SSM tunnel is maintained, AWS Paper RDS operational data can be queried from pgAdmin4.
   4) On tunnel termination, the pgAdmin4 connection is immediately severed (R-AUTO-012 detection reinforcement consistent).

### 3. Runbook §10 Reinforcement (Auxiliary Procedure for §10 of the Previous Section)

This section reinforces the auxiliary procedure for the 2 additional clients without changing the first body of the Runbook in §10 of the previous section.

1. Connection validation stage (auxiliary): [확인]
   1) Local PostgreSQL 18 psql client direct path execution — `& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -p 15433 -U portfolio_admin -d portfolio`
   2) Result confirmation items:
       - psql client major / full / server major / full match (client `18.1` / server `18.4`)
       - `SSL connection: TLSv1.3`
       - `current_user` / `current_database` / `inet_server_addr` / `inet_server_port` match
   3) General `psql` PATH not registered is normal — full path direct execution or PATH registration follow-up (R-AUTO-013 consistent).
2. pgAdmin4 server registration stage: [준비]
   1) Register Server
       - Name: `AWS Paper RDS - portfolio`
   2) Connection
       - Host name/address: `localhost`
       - Port: `15433`
       - Maintenance database: `portfolio`
       - Username: `portfolio_admin` or per-MS app role
       - Password: the relevant DB password — plaintext recording in this runbook body / operator notes / console captures / logs prohibited (R-DOCS-001 consistent).
   3) SSL
       - SSL mode: `Prefer`
   4) Caution:
       - Connection is possible only if the SSM Port Forwarding PowerShell window is open (`Port 15433 opened` maintained).
       - On tunnel termination / `Ctrl + C`, the pgAdmin4 connection is also immediately severed (R-AUTO-012).
       - pgAdmin4 server registration is set to `localhost:15433`, not the RDS endpoint (OD-NET-011 consistent).
3. Success criteria reinforcement: [확인]
   1) In the psql / pgAdmin4 output, `inet_server_addr = 10.0.20.165` / `inet_server_port = 5432` match (previous section §10.6 success criteria reinforcement).
   2) RDS `PubliclyAccessible` value maintained as `False` (as in previous section §10.6).

### 4. Safety / Security Check Result

1. 0 AWS / RDS / IAM / Secrets Manager / SSM changes. Uses only read-only AWS API + reuse of the same SSM Port Forwarding tunnel + psql / pgAdmin4 SELECT queries.
2. 0 RDS DDL/DML. 0 INSERT / UPDATE / DELETE / DDL. 0 broker / KIS / order / fill / Daily Batch entrypoint calls. 0 `--execute` actual calls.
3. 0 plaintext recording in this note of actual password / secret value / KIS app key / KIS app secret / account number / token / account-id / actual IAM access key id / actual secret ARN / EIP. All `[REDACTED]` or placeholder.
4. Operational identifiers (instance id / private IP / local port / SSM session id / RDS endpoint hostname of previous section §11) are reused as is. Additional operational identifier of this section: local PostgreSQL installation path (`C:\Program Files\PostgreSQL\18\bin\psql.exe`) — a tool path of the local PC and not a secret.
5. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog due to this date's work. pgAdmin4 / psql 18 client are operator local PC tools with no impact on separate spec artifacts.

### 5. Outside This Date's Scope / Follow-up Handover

1. General `psql` command PATH registration — register `C:\Program Files\PostgreSQL\18\bin` in the system / user PATH (R-AUTO-013 consistent / `_common/followups-overview.md` 2026-06-13 SSM Port Forwarding follow-up memo reinforcement).
2. pgAdmin4 per-environment (paper / live) server separate registration policy — standardize a server name prefix policy (e.g., `AWS Paper RDS - portfolio`) so as not to mistakenly register the live RDS in the paper environment. The live environment is separated as a follow-up (10 spec).
3. pgAdmin4 / psql per-app-role password storage — the responsibility of the operator's local PC environment. Not injected directly from Secrets Manager. To prevent an R-DOCS-001 violation on operator mistake, the principle of prohibiting plaintext recording in screen captures / chat / notes is maintained.
4. Strategy Execution (`port_strategy_execution`) AWS porting main phase: follow-up (04 spec follow-up phase). On this date, prerequisite connection availability was first empirically shown complete with 3 clients (Python `psycopg2` / psql 18 / pgAdmin4).


## 2026-06-17 DB Role / Privilege / search_path Correction Discovered During the Daily AWS 17-step E2E Flow

Accumulates the facts of DB Role / privilege / search_path correction within this spec's scope that the operator discovered during the same-date second session (Daily AWS 17-step E2E complete).

- 0 additional decisions of this spec itself / 0 body changes.
- Records only the facts of decision consistency validation and follow-up formal matrix update.
- For the detailed full 17 step progress status, refer to the 2026-06-17 section of the 03 / 04 / 06 / 08 / 09 spec operation-notes.
- Kiro performs only document authoring / procedure organization. The actual GRANT / search_path changes are performed directly by the operator.
- This date is limited to `aws-paper` / 0 aws-live work.
- 0 plaintext recording in this note of password / endpoint hostname / account-id / actual ARN / account number.

### 1. `execution_app` `interest` schema Privilege Correction (Step 8 Impact)

1. First failure fact: confirmed
   1) The Daily AWS 17-step's #8 `DAILY_BUY_EXECUTION` ECS RunTask failed on first execution due to `execution_app`'s missing `interest` schema / table SELECT permission (R-DATA-005 [2026-06-17 reinforcement] consistent).
   2) Impact scope: 04 spec operation-notes 2026-06-17 §3 consistent. preprocessor itself (step #3) completed normally / this permission omission is limited to step #8 impact.
2. Operator action fact: complete (2026-06-17 §3 operator direct GRANT correction)
   1) Grant `interest` schema USAGE permission to `execution_app`.
   2) Grant `interest.*` table SELECT permission (execution_app needs to read the interest schema in the buy execution flow).
   3) Grant sequence permission (limited to necessary sequences).
   4) future default privileges correction (`ALTER DEFAULT PRIVILEGES IN SCHEMA interest GRANT SELECT ON TABLES TO execution_app` etc.) — automatically applied when follow-up objects are newly created.
3. 02 spec formal matrix update: follow-up
   1) Reflecting the facts of `interest` schema USAGE / table SELECT / sequence / default privileges in the `execution_app` row of §4 GRANT / §5 validation SQL of [`./db-roles-and-grants.md`](./db-roles-and-grants.md) is a follow-up phase.
   2) The operator direct GRANT result is recorded only as fact in this note — 02 spec body is updated at the formal matrix update time / no body change on this date.

### 2. `marketconnector_app` `legacy` schema / `legacy.holdings` / search_path Correction (Step 17 Impact)

1. First failure fact: confirmed
   1) The Daily AWS 17-step's #17 `BALANCE_REFRESH` SSM RunCommand failed on first execution due to a bare `holdings` `relation does not exist` error (R-DATA-011 new consistent).
   2) Cause: `marketconnector_app`'s `legacy` schema USAGE not granted (OD-DB-007 consistent — 1 exception occurred to the policy of not granting the legacy schema to all app roles) + `legacy.holdings` DML not granted + sequence not granted + database search_path omission.
   3) Impact scope: 03 spec operation-notes 2026-06-17 §4 consistent.
2. Operator action fact: complete (03 spec §4 operator direct work consistent)
   1) Correct `marketconnector_app`'s database search_path to `connector, execution, legacy, reference, public` (`ALTER ROLE marketconnector_app IN DATABASE portfolio SET search_path = ...`).
   2) Grant `legacy` schema USAGE permission (legacy operational data access needed — 1 exception to OD-DB-007 / follow-up reconsideration candidate).
   3) Grant `legacy.holdings` DML (SELECT / INSERT / UPDATE / DELETE) permission (limited to minimum permission for legacy operational data update).
   4) Grant `legacy` schema sequence permission + future default privileges correction.
3. 02 spec formal matrix update: follow-up
   1) Reflecting the facts of `legacy` schema USAGE / `legacy.holdings` DML / sequence / database search_path in the `marketconnector_app` row of §4 GRANT / §5 validation SQL of [`./db-roles-and-grants.md`](./db-roles-and-grants.md) is a follow-up phase.
   2) The fact of the marketconnector_app-limited 1 exception to the OD-DB-007 (legacy schema not granted to all app roles) policy is recorded as fact in the second item of the [`../_common/operator-decisions.md`](../_common/operator-decisions.md) Change Log 2026-06-17 / body decision value change separated as follow-up.

### 3. Validation SQL Reinforcement Candidates / Per-app-role search_path / grants Validation SQL

1. Per-app-role search_path check SQL candidate:
   1) `SELECT rolname, rolconfig FROM pg_roles WHERE rolname IN ('execution_app', 'marketconnector_app', 'decision_app', 'preprocessor_app', 'crawler_app', 'research_app', 'view_app');` — extract the `search_path=...` item from `rolconfig`.
   2) Or connect with each role and directly execute `SHOW search_path;`.
   3) Add as a regular validation item that this date's `marketconnector_app` search_path was corrected to `connector, execution, legacy, reference, public`.
2. future default privileges check SQL candidate:
   1) `SELECT * FROM pg_default_acl WHERE defaclnamespace = 'legacy'::regnamespace;` — confirm the default ACL of the legacy schema.
   2) `SELECT * FROM pg_default_acl WHERE defaclnamespace = 'interest'::regnamespace;` — confirm the default ACL of the interest schema.
   3) Regularly validate that, as a result of this date's correction, default privileges for `execution_app` interest / `marketconnector_app` legacy were applied.
3. bare table name-dependent legacy path validation SQL candidate:
   1) `SET ROLE marketconnector_app;` `SELECT 1 FROM holdings LIMIT 1;` — confirm whether bare `holdings` is resolved within search_path.
   2) Add as a prerequisite check SQL before BALANCE_REFRESH entry.
4. By R-DATA-011 detection consistency, add these validation SQLs as regular items at the 17-step entry stage — formal matrix update is a follow-up phase.

### 4. Safety / Security Check Result

1. 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging due to this date's work. 0 changes to 02 spec body decision values — formal matrix update is a follow-up.
2. The following sensitive information has 0 plaintext recording in this note — all `[REDACTED]` or placeholder.
   - actual password / RDS endpoint hostname / RDS port / database name / username
   - account-id / actual IAM Role ARN / actual secret ARN

   Only operational identifiers recorded as fact:
   - role names `execution_app` / `marketconnector_app`
   - schema names `interest` / `legacy` / table name `holdings`
   - search_path value `connector, execution, legacy, reference, public`
3. RDS / GRANT / ALTER ROLE work is all performed directly by the operator. Kiro performs only document authoring / procedure organization / validation item organization. 0 plaintext recording of `secretsmanager:GetSecretValue` result values. 0 plaintext quotation in this note of CloudWatch Logs body / SSM response body / operator PowerShell stdout full text.
4. 0 RDS DDL (this spec's scope — 0 schema creation / drop / table creation / drop). 0 DML (this spec's scope — direct change of `legacy.holdings` is 03 spec Step 17 responsibility). GRANT / REVOKE / ALTER DEFAULT PRIVILEGES / ALTER ROLE work occurred on this date and this is recorded only as fact in this note.
5. 0 direct broker / KIS / order / fill / Daily Batch entrypoint calls. live automatic GRANT / DDL / DML is still prohibited until follow-up validation / approval per the OD-SAFE-002 / OD-SAFE-003 policy. This date is limited to `aws-paper` / 0 aws-live work. R-DATA-005 / R-DATA-011 mitigation first empirically shown / Status `Mitigated` update consistent.
