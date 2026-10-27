# Operation Notes — 06-secrets-and-iam

This document is an operation note that cumulatively records, by date, the work results actually performed by the operator / Kiro during 06-secrets-and-iam.
The first application environment is `aws-paper`, the region is `ap-northeast-2`, and the first application target is the MarketConnector EC2.

Record format

- Accumulate with per-date `## YYYY-MM-DD <summary>` headers (same as 02 spec operation-notes).
- The `[운영자 기록]` slot at the end of an item is briefly filled by the operator with the result (`성공` / `실패` / `보류` / `해당 없음`) after the actual work.
- On failure / on-hold, write only a 1-line reason. Full quotation of JSON body / error message is prohibited (security / volume reduction).
- An IAM Role / Policy change is summarized in 4 lines: change date / changer / change reason / before·after item summary (full JSON body quotation prohibited).

Safety principles

- Actual secret value, KIS app key, KIS app secret, account number, RDS endpoint hostname, RDS password, token, Slack webhook URL,
  IAM access key id, account-id, actual secret ARN, actual KMS Key ARN are prohibited from plaintext recording in this document.
  All `[REDACTED]` or a placeholder.
- The secret lookup result itself (value) is prohibited from recording. Record only `성공 / 실패` and the last update time (if needed, ISO 8601 `YYYY-MM-DDTHH:MM:SS+09:00`).
- Actual AWS resource creation / modification / deletion is performed directly by the operator. Kiro performs only document authoring / procedure organization / validation item organization.
- The 8 MS (`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`,
  `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`)
  README / AGENTS.md / CHANGELOG / docs / worklog / source code unmodified.
- Only the operator issues an actual `secretsmanager:GetSecretValue` call. Kiro automatic validation is limited to `secretsmanager:DescribeSecret` metadata.

### Placeholder Coverage

This is the list of target items and placeholders referenced by the "0 plaintext records of sensitive information" confirmation block at the end of each session in this document.
Each session refers to the below and records only a short confirmation without duplicate enumeration.

Target items (full plaintext recording prohibited in this document)

- secret value / KIS app key / KIS app secret
- raw 12-digit account number / raw broker_order_no
- token / RDS password / RDS endpoint hostname
- raw 12-digit account-id / IAM access key id
- actual IAM Role ARN / actual secret ARN / actual KMS Key ARN / actual state machine ARN / actual Lambda ARN
- Slack webhook URL / DB password / Administrator password
- image digest full sha256 / actual public IP / actual EIP / task ARN / ENI ID

Allowed placeholders

- `[REDACTED]`, `[REDACTED_ARN]`, `[REDACTED_SECRET_ARN]`, `[REDACTED_ACCOUNT_NO]`,
  `[REDACTED_PUBLIC_IP]`, `[REDACTED_TASK_ARN]`, `[REDACTED_BROKER_ORDER_NO]`
- `<account-id>`, `<region>`, `<kms-key-id>`, `<instance-id>`

Allowed factual records (not secrets)

- secret name path (`/portfolio/paper/marketconnector/kis-app-key`, etc.)
- SSM Parameter name path
- environment-variable key names (`APP_KEY` / `KIS_APP_KEY` / `PORT_ENVIRONMENT` / `INTEREST_DB_*`, etc.)
- IAM Role / Instance Profile / Policy name labels
- DB role · schema names
- ECS Task Definition · state machine · Scheduler · Lambda label names
- executionName / commandId / SHA256 / source_version / Spring profile / Spring properties key labels

## 2026-06-10 06-secrets-and-iam documentation progress

- [`./requirements.md`](./requirements.md) creation complete
- [`./README.md`](./README.md) creation complete
- [`./design.md`](./design.md) creation complete
- [`./tasks.md`](./tasks.md) creation complete
- [`./runbook.md`](./runbook.md) creation complete
- [`./validation-checklist.md`](./validation-checklist.md) creation complete
- [`./operation-notes.md`](./operation-notes.md) creation complete

On this date, 0 AWS resource creation / modification / deletion. 0 changes to the 8 MS code / documents. 0 external API calls.
Only 7 documents newly created inside this spec folder.

## 2026-06-10 Actual AWS work record template

The operator fills this section after actually performing the [`./runbook.md`](./runbook.md) steps.
Write only the result in the `[운영자 기록]` slot at the end of each item
(no recording of secret value / account number / endpoint hostname / account-id / actual ARN).

### 1. Secrets Manager

- whether `/portfolio/paper/marketconnector/kis-app-key` is created: [운영자 기록]
- whether `/portfolio/paper/marketconnector/kis-app-secret` is created: [운영자 기록]
- whether `/portfolio/paper/marketconnector/paper-account` is created
  (JSON multi-key `PAPER_ACNT` / `ACNT_PRDT_CD`): [운영자 기록]
- whether `/portfolio/paper/rds/marketconnector-app` is created
  (JSON multi-key `host` / `port` / `dbname` / `username` / `password`): [운영자 기록]
- whether the existing `/portfolio/paper/rds/master` is retained (name / KMS / last modified time match the 02 spec point in time): [운영자 기록]
- whether any new secret beyond the above 5 is additionally registered (0 is normal if any): [운영자 기록]
- confirm 0 plaintext records of the actual secret value in this document / console captures / operator notes: [운영자 기록]

### 2. SSM Parameter Store

- whether `/portfolio/paper/marketconnector/kis-base-url` is created: [운영자 기록]
- whether `/portfolio/paper/marketconnector/connector-host` is created: [운영자 기록]
- whether `/portfolio/paper/marketconnector/connector-port` is created: [운영자 기록]
- whether `/portfolio/paper/marketconnector/connector-debug` is created: [운영자 기록]
- whether `/portfolio/paper/marketconnector/environment` is created (Value=`paper`): [운영자 기록]
- whether `/portfolio/paper/marketconnector/broker-name` is created: [운영자 기록]
- confirm the parameter names themselves do not contain endpoint hostname / account number / secret value / password / token: [운영자 기록]

### 3. IAM Role / Policy / Instance Profile

- whether IAM Role `portfolio-paper-marketconnector-ec2-role` is created or confirmed
  (Trust Policy `Service: ec2.amazonaws.com`): [운영자 기록]
- whether Instance Profile `portfolio-paper-marketconnector-ec2-profile` is created or confirmed (including Role attach): [운영자 기록]
- whether the minimal read policy `portfolio-paper-marketconnector-ec2-readonly` is authored / attached: [운영자 기록]
- whether the Instance Profile is attached to the EC2 instance
  (`describe-iam-instance-profile-associations` result matches): [운영자 기록]
- confirm 0 use of Resource wildcard `"*"`: [운영자 기록]
- confirm 0 Action wildcards (`secretsmanager:*` / `ssm:*` / `*`): [운영자 기록]
- confirm 0 other service prefix (`/portfolio/paper/view/*`, `/portfolio/paper/crawler/*`, etc.) Resource: [운영자 기록]
- confirm 0 other environment prefix (`/portfolio/live/...`) Resource: [운영자 기록]
- whether a KMS Decrypt statement is applied (0 is normal when CMK is unused): [운영자 기록]

### 4. EC2 validation

- whether the `aws sts get-caller-identity` result `Arn` is confirmed to be of the form
  `assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>`
  (recording the actual account-id / instance-id in this document is prohibited): [운영자 기록]
- whether the `aws configure list` result access_key Source is confirmed as `iam-role` or `Ec2InstanceMetadata`: [운영자 기록]
- whether `~/.aws/credentials` non-existence is confirmed: [운영자 기록]
- whether 0 `aws_access_key_id` / `aws_secret_access_key` lines inside `~/.aws/config` is confirmed: [운영자 기록]
- whether the `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` / `AWS_SESSION_TOKEN` environment variables in the current shell are confirmed unset: [운영자 기록]
- whether 0 access key exports inside dotfiles (`~/.bashrc` / `~/.profile` / `~/.bash_profile`) is confirmed: [운영자 기록]
- whether Secrets Manager `describe-secret` 4 entries return metadata normally (no value lookup): [운영자 기록]
- whether SSM `get-parameters-by-path /portfolio/paper/marketconnector` 6 entries return normally: [운영자 기록]
- whether querying another service prefix gives AccessDenied / NotFound (privilege isolation normal): [운영자 기록]
- whether querying another environment prefix (`/portfolio/live/...`) gives AccessDenied: [운영자 기록]

### 5. Connector smoke test (read-only only)

- whether the environment variables below are injected after running the temporary export script (value display prohibited): [운영자 기록]
  - `INTEREST_DB_*` / `APP_KEY` / `APP_SECRET` / `BASE_URL`
  - `PAPER_ACNT` / `ACNT_PRDT_CD` / `PORT_ENVIRONMENT` / `PORT_BROKER_NAME`
- whether 0 plaintext storage of the temporary export script / environment-variable value file / logs / console captures is confirmed: [운영자 기록]
- whether RDS `marketconnector_app` connection succeeds (no DDL/DML, query only): [운영자 기록]
- whether KIS token issuance or reuse of the existing `access_token.txt` is confirmed (token value display prohibited): [운영자 기록]
- whether `connector_balance.py` balance query succeeds: [운영자 기록]
- whether `connector_order_check.py` order / fill query succeeds: [운영자 기록]
- whether the Flask read-only endpoint smoke test passes (balance / holdings / order history, etc.): [운영자 기록]
- whether 0 new-order / buy / sell / cancel / modify API calls is confirmed: [운영자 기록]
  - target: `connector_buy.py`, `connector_sell.py`, `connector_cancel.py`, `connector_modify.py` not executed

### 6. Incomplete / carried over

- incomplete items: [운영자 기록]
- carried-over items (moved to the next date): [운영자 기록]
- items to move to the 03-marketconnector-ec2 spec: [운영자 기록]
  - e.g.: `AmazonSSMManagedInstanceCore` attach, CloudWatch Logs write privilege, systemd normal operation mode transition
- whether the `_common` update candidates proceeded: [운영자 기록]
  - operator-decisions.md OD-SEC-001 / OD-OBS-004 / OD-SEC-005 / OD-SEC-006
  - risk-register.md 4 R-SEC candidates
  - followups-overview.md 06 section

## IAM change record template (add per-date as needed)

When this spec's Permission Policy / Trust Policy / Resource ARN list changes, accumulate a record in this section.
1 change = a 4-line summary format.

```
## YYYY-MM-DD IAM change
- change date: YYYY-MM-DDTHH:MM:SS+09:00
- changer: [operator identifier (nickname / role)]  # actual IAM user / email plaintext prohibited
- change reason: [one-line summary]
- before / after item summary: [number of Resource added / removed items, number of Action added / removed items, whether a KMS statement was added, etc. —
  full JSON body quotation prohibited]
```

## Follow-up handover

- On this date, all 7 documents of the 06 spec (`requirements.md`, `README.md`, `design.md`, `tasks.md`, `runbook.md`,
  `validation-checklist.md`, `operation-notes.md`) were created.
  This spec's documentation closure condition ([`./tasks.md`](./tasks.md) task 22) is satisfied.
  However, the actual AWS resource work and validation pass are operator follow-up work.
- On entering the 03-marketconnector-ec2 spec, take this spec's §4 Instance Role matrix, §5 Access Key non-use principle,
  §6 ECS Task Role skeleton, §7 OD candidates as input ([`./tasks.md`](./tasks.md) task 23).
- Updates to [`../_common/operator-decisions.md`](../_common/operator-decisions.md),
  [`../_common/risk-register.md`](../_common/risk-register.md),
  [`../_common/followups-overview.md`](../_common/followups-overview.md) proceed separately upon operator approval
  in [`./tasks.md`](./tasks.md) task 16 / 17 / 18.


## 2026-06-17 Factual record of Secrets / IAM / DB Role permissions during the Daily AWS 17-step E2E flow

The operator cumulatively records facts within this spec's scope during the same date's second session (Daily AWS 17-step E2E completion).
Scope = MarketConnector EC2 Instance Role + Secrets Manager + SSM Parameter Store + DB role correction connection.

Session overview

- 0 additional decisions of this spec itself / 0 body changes.
- Records only the facts of decision-alignment validation and the connection to the 02 spec's DB role / grants correction result.
- For the full 17-step progress state, see the 2026-06-17 section of the 03 / 04 / 02 / 08 / 09 spec operation-notes.
- Kiro performs only document authoring / procedure organization. The actual IAM / Secrets Manager / SSM / GRANT work is performed directly by the operator.
- This date is limited to `aws-paper` / 0 aws-live actions.
- 0 plaintext records of sensitive information — see Placeholder Coverage.

### 1. MarketConnector EC2 Instance Role-based Secrets Manager / SSM Parameter Store first validation

1. EC2 Instance Role read result: success
   1) MarketConnector EC2 Instance Role-based `secretsmanager:GetSecretValue` call success
      - Step 1 `CONNECTOR_BALANCE`
      - Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`
      - Step 13 `CONNECTOR_ORDER_CHECK`
      - reused in the Step 17 `BALANCE_REFRESH` flow
   2) JSON SecretString inner-key extraction policy alignment (03 spec design.md §8.2.2 alignment)
      - the SecretString of `kis-app-key` / `kis-app-secret` / `paper-account`, etc. is JSON, not a plain string
      - export after extracting the inner keys `APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD`
   3) Access Key non-use principle (OD-SEC-005) alignment — IMDSv2 + Instance Role only / 0 static credentials.
2. 0 value plaintext output / key presence validation: complete
   1) when the operator exports the environment variables, only `length` / `key presence` / `alias presence` are confirmed.
   2) 0 value plaintext in stdout / logs / console captures / operator notes
      (R-DOCS-001 [2026-06-17 reinforcement] / [2026-06-17 reinforcement (17-step E2E)] alignment).
   3) `KIS_*` alias simultaneous export policy (03 spec runbook §2 alignment) — the 5 below
      - `KIS_APP_KEY` / `KIS_APP_SECRET` / `KIS_PAPER_ACNT` / `KIS_ACNT_PRDT_CD` / `KIS_BASE_URL`
3. OD-SEC-006 first-validation memo reinforcement: complete
   1) the MarketConnector EC2 Instance Role's secret read privilege is applied limited to KIS Secrets / DB Secrets
      - operated normally across the full 17-step on this date
   2) per-MS Secret access separation policy alignment (2026-06-15 §6 alignment)
      - preprocessor secret access is still `AccessDeniedException` / normal operation
   3) the JSON SecretString inner-key parsing policy reinforces only the first-validation memo without changing the OD-SEC-006 body
      (2026-06-17 first session decision alignment).

### 2. Connection between the 02 spec DB Role privilege correction and this spec

The 2 DB role privilege corrections discovered in this date's 17-step flow are in the 02 spec's responsibility area but are related to this spec's IAM / Secrets / Role policy.
Records only the facts of alignment validation and the subsequent formal matrix update.

1. `execution_app`'s `interest` schema privilege correction (Step 8 impact): factual record
   1) 02 spec operation-notes 2026-06-17 §1 alignment — `DAILY_BUY_EXECUTION` passed via the operator's direct GRANT correction.
   2) this spec's `decision-app` / `execution-app` Secrets Manager policy alignment validation
      - the Secret access privilege was aligned on this date
      - the DB role's schema · table · sequence GRANT shortage was the first-failure cause
   3) the 02 spec db-roles-and-grants formal matrix update is a follow-up phase.
2. `marketconnector_app`'s `legacy` schema · `legacy.holdings` · search_path correction (Step 17 impact): factual record
   1) 02 spec operation-notes 2026-06-17 §2 alignment — `BALANCE_REFRESH` passed via the operator's direct correction
      - search_path correction + USAGE / DML / sequence GRANT + default privileges correction
   2) this spec's `marketconnector-ec2-role` Secrets Manager policy alignment validation
      - the Secret access privilege was aligned on this date
      - the DB role's legacy schema USAGE / DML / sequence / database search_path shortage was the first-failure cause
   3) reference to the marketconnector_app-limited 1 exception of OD-DB-007 (legacy schema not granted to any app role)
      - [`../_common/operator-decisions.md`](../_common/operator-decisions.md) Change Log 2026-06-17 second item
      - [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-17 §2
      - [`../_common/risk-register.md`](../_common/risk-register.md) R-DATA-011 new alignment
      - the 02 spec body decision-value change is separated as a follow-up
3. Follow-up formal matrix / default privileges / sequence privilege validation candidates:
   1) 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) formal update
      - the `execution_app` / `marketconnector_app` rows of §4 GRANT / §5 validation SQL
   2) formal organization of future default privileges (`ALTER DEFAULT PRIVILEGES IN SCHEMA <name> GRANT ...`).
   3) formal organization of sequence privileges.

### 3. Safety / security inspection result

1. This date's work made 0 changes to the 8 MS README / AGENTS.md / CHANGELOG / docs / worklog / source / packaging.
   0 changes to the 06 spec body decision values / the operator's direct IAM / Secrets / GRANT changes are recorded in this note as facts only.
2. Confirm 0 plaintext records of sensitive information — see Placeholder Coverage.
   Only operational identifiers are recorded as facts:
   - secret name path `/portfolio/paper/kis/marketconnector` / `/portfolio/paper/rds/marketconnector-app` / `/portfolio/paper/rds/execution-app`
   - SSM Parameter name path `/portfolio/paper/kis/...`
   - environment-variable key names `APP_KEY` / `KIS_APP_KEY` / `PORT_ENVIRONMENT` / `PORT_DB_TARGET`
   - role names `execution_app` / `marketconnector_app`
   - schema names `interest` / `legacy`
3. All AWS / IAM / Secrets Manager / SSM Parameter Store / RDS / GRANT work was performed directly by the operator.
   Kiro performs only document authoring / procedure organization / validation item organization.
   - 0 AWS CLI / boto3 executions (Kiro side)
   - 0 plaintext records of the `secretsmanager:GetSecretValue` result value
   - 0 plaintext quotations of the CloudWatch Logs body / SSM response body / KIS API response body / operator PowerShell stdout full text
4. The JSON SecretString inner-key parsing fact is recorded only as a mapping fact (0 value plaintext).
   first validation of the raw SecretString export prohibition policy
   (R-DOCS-001 [2026-06-17 reinforcement] / [2026-06-17 reinforcement (17-step E2E)] alignment).
5. broker / KIS calls are limited to 4 KIS paper BUY (03 spec Step 12) + balance / order check read-only.
   - 0 SELL / cancel / modify / additional `--execute` calls
   - the live automatic BUY / SELL E2E validation is still prohibited until follow-up validation / approval, per the OD-SAFE-002 / OD-SAFE-003 policy
   - this date is limited to `aws-paper` / 0 aws-live actions
6. First validation that this spec's policy-backed AWS E2E flow operates normally, via the Daily AWS 17-step E2E paper first pass
   - OD-SEC-005 / OD-SEC-006 / OD-SEC-007 mitigation alignment
   - Status retained at the existing value (🟡 잠정)


## 2026-06-29 (2) — Fargate port-view Task Role / env injection follow-up addition

As a result of this date's port-view-side commit `e72de6f` (`feat(view): add Step Functions daily batch trigger`),
this leaves as a follow-up the IAM / Secrets / env injection policy that the 06 spec side must additionally lock at Fargate entry.
This note has 0 plaintext quotations of the port-view-side code body / IAM Policy / ASL / response body (R-DOCS-001 alignment).

§1. Fargate port-view Task Role follow-up

1) `states:StartExecution` minimal privilege
   (1) Resource pattern limitation
       - the `states:StartExecution` privilege scope of the Fargate port-view Task Role is
         recommended limited to the `portfolio-paper-daily-step1-17-approval` state machine ARN
       - retain 0 Resource · Action wildcards (OD-SEC-005 / OD-SEC-006 / R-AUTO-034 new mitigation alignment)
       - if a Step 12~17 approval-type state machine is added separately, limit it with the same pattern
   (2) 0 plaintext records in this spec body
       - the actual IAM Role ARN / full IAM Policy body / the account-id part of the state machine ARN (`[REDACTED]` handling)
2) Other Task Role privileges
   (1) grant minimal privilege for ECR pull / Secrets Manager (specific ARN) / SSM Parameter Store (specific ARN) /
       CloudWatch Logs `CreateLogStream` + `PutLogEvents`
   (2) narrow the Resource pattern with a per-environment prefix (`/portfolio/paper/...`)
   (3) retain the separation of ECS Task Definition `executionRoleArn` and `taskRoleArn`

§2. Secrets injection policy

1) RDS connection info
   (1) prohibit direct recording in image / properties
       - prohibit plaintext recording of RDS host · port · user · password in
         Dockerfile / `application.properties` / `application-aws-paper.properties`
       - inject via Secrets Manager (or SSM SecureString)
       - inject environment variables via the ECS Task Definition `secrets` block or a startup hook
       - retain the `INTEREST_DB_*` environment-variable pattern as is (port-view README alignment)
   (2) Exposure control
       - expose environment variables only inside the Fargate Task
       - prohibit plaintext exposure in external audit log / operator notes / console captures
         (R-DOCS-001 / R-DOCS-002 / R-SEC-010 alignment)
2) KIS secret / Slack webhook
   (1) since the Fargate View side has no direct KIS broker call, KIS secret injection is out of this spec's first application scope
       - the KIS secret is retained as the 03 spec MarketConnector EC2 side's responsibility (OD-SEC-003 / OD-MS-027 alignment)
   (2) the Slack webhook URL is first operated as an environment variable on the `portfolio-event-notifier` Lambda side
       - migration to Secrets Manager or SSM SecureString is a follow-up (R-AUTO-024 alignment)
       - no change to the port-view-side Slack webhook usage policy

§3. Step Functions identifier env injection

1) `stateMachineArn` / region / `executionNamePrefix`
   (1) prohibit fixing directly in `application.properties`
       - the 3 properties below of port-view `aws-stepfunctions` mode are injected via env or config
         - `portfolio.batch.aws-stepfunctions-state-machine-arn`
         - `portfolio.batch.aws-stepfunctions-region`
         - `portfolio.batch.aws-stepfunctions-execution-name-prefix`
       - inject via the ECS Task Definition `environment` or `secrets` block
       - 0 plaintext records in this spec body (`[REDACTED]` or placeholder)
2) Safe defaults
   (1) `portfolio.batch.local-file-execution-enabled=false`
   (2) `portfolio.batch.paper-order-enabled=false`
   (3) `portfolio.batch.full-pipeline-execution-enabled=false`
   (4) `portfolio.batch.max-executable-step-order=11`
       (Step 12 and above are ENABLEd after introducing a separate approval-type phase)
   (5) R-AUTO-034 new mitigation alignment / 05 spec follow-up phase responsibility

§4. Decision / risk mapping

| Category | Item | Status |
| --- | --- | --- |
| Decision | OD-SEC-005 / OD-SEC-006 / OD-SEC-007 / OD-MS-002 / OD-MS-009 / OD-MS-037 | first-validation memo reinforced without body changes |
| Decision | detailed change history | see the `../_common/operator-decisions.md` Change Log 2026-06-29 (2) item |
| Risk | R-AUTO-034 (new) | Open — Fargate View `states:StartExecution` excessive privilege + Step 12 gate bypass risk / 06 spec follow-up phase responsibility |
| Risk | R-DOCS-001 / R-DOCS-002 / R-SEC-010 | alignment retained — the secret value / DB password / Slack webhook / Administrator password plaintext recording prohibition policy as is |

§5. Follow-up (06 spec follow-up phase responsibility)

1) Formal definition of the Fargate port-view Task Role + IAM Policy authoring
   (1) `states:StartExecution` Resource limitation + ECR pull + Secrets Manager (`/portfolio/paper/...`) + CloudWatch Logs minimal privilege
   (2) `simulate-principal-policy` validation + 0 Resource · Action wildcard audit
2) Formal authoring of the ECS Task Definition `secrets` / `environment` blocks
   (1) RDS connection info Secrets Manager injection / `INTEREST_DB_*` environment-variable mapping
   (2) `portfolio.batch.aws-stepfunctions-*` env injection mapping
3) Re-validate the Fargate port-view-side secret at the DB password rotate (R-SEC-010 new) entry time
4) When entering the Slack webhook URL migration to Secrets Manager or SSM Parameter Store (R-AUTO-024),
   cross-spec audit the impact on the port-view-side secret loader

§6. This date's factual recording scope

1) Kiro work = performed only the accumulation of this section in 06 spec `operation-notes.md`
2) the operator's direct commit `e72de6f` code changes are in the port-view MS area (0 changes to the 06 spec area)
3) 0 changes on this date to AWS CLI / boto3 / psql / Spring Boot execution / external API calls
   - 0 AWS resource creation · modification · deletion
4) 0 full plaintext quotations of the responses · bodies below
   - full IAM Policy body / `simulate-principal-policy` response body
   - Secrets Manager `GetSecretValue` response body / SSM Parameter Store response body
   - full Spring Boot application log
5) Confirm 0 plaintext records of sensitive information — see Placeholder Coverage.
6) Only operational identifiers recorded as facts — not secrets:
   - commit hash `e72de6f`
   - Class name `StepFunctionsDailyBatchExecutionService`
   - Spring properties key labels
   - environment-variable pattern `INTEREST_DB_*`
   - Spring profile `aws-paper`


## 2026-06-30 (afternoon) — First validation of port-view ECS Fargate Task Role / Task Execution Role separation

Cumulatively records the facts within the 06 spec (secrets / IAM) scope, from the results of the port-view ECS Fargate first-porting pass + ECS View → AWS Step Functions
Step 12~17 approval execution first validation that the operator performed directly on this date's afternoon.

Session overview

- Scope = ECS Task Role / Task Execution Role separation validation.
- 0 full plaintext quotations of the responses · bodies below (R-DOCS-001 alignment)
  - port-view code body / full IAM Policy body / `simulate-principal-policy` response body
  - Secrets Manager `GetSecretValue` response body / full Task Definition JSON body
  - Step Functions execution history body / Slack message body / full CloudWatch Logs

§1. First validation of Task Role / Task Execution Role separation

1) Task Role: complete
   (1) name `portfolio-paper-view-task-role`
       - actual ARN `[REDACTED_ARN]` placeholder / 0 plaintext records in this note
   (2) responsibility = application-side privileges
       - the `states:StartExecution` privilege is granted limited to the Step 12~17 approval state machine ARN
         (`portfolio-paper-daily-step12-17-approval`)
       - the grant limited to the general workflow ARN (`portfolio-paper-daily-step1-17-approval`) remains a 06 spec follow-up phase responsibility
       - 0 Action / Resource wildcards
   (3) first-validation evidence
       - ECS View → Step Functions Step 12~17 approval execution passed
         (executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / status `SUCCEEDED`)
       - first-validation evidence of (a) Task Role `states:StartExecution` minimal privilege of the R-AUTO-034 mitigation
2) Task Execution Role: complete
   (1) name `portfolio-paper-ecs-task-execution-role`
       - actual ARN `[REDACTED_ARN]` placeholder / 0 plaintext records in this note
   (2) responsibility = ECS task startup-time privileges
       - ECR repository `portfolio-view` pull
       - CloudWatch Logs group `/ecs/portfolio-view` `PutLogEvents`
       - Secrets Manager `GetSecretValue`
         (Resource = `/portfolio/paper/...` prefix-limited grant policy retained / 06 spec follow-up phase responsibility)
   (3) first-validation evidence
       - ECR pull success + ECS task RUNNING
       - Spring Boot started confirmed in CloudWatch Logs
         (log group `/ecs/portfolio-view` / 0 plaintext quotations of the raw body)
       - HikariPool RDS connection success + default schema `ops` confirmed
         (RDS connection info Secrets Manager injection first validation / actual secret ARN `[REDACTED_SECRET_ARN]` placeholder)
3) First validation of the separation policy: complete
   (1) first validation of the separate grant so that the Task Role and Task Execution Role are not merged into the same IAM Role
       - the Task Role is application responsibility (e.g. `states:StartExecution`)
       - the Task Execution Role is startup-time responsibility (e.g. ECR pull / CloudWatch Logs / Secrets Manager)
       - OD-SEC-005 / OD-SEC-006 / R-AUTO-034 mitigation alignment

§2. First validation of the Fargate task definition `environment` / `secrets` blocks

1) `environment` block: complete
   (1) Spring profile `aws-paper` injection
   (2) `portfolio.batch.local-file-execution-enabled=false` (Fargate safe default / R-AUTO-034 mitigation alignment)
   (3) 0 loosening regression of gate values such as `paperOrderEnabled` · `fullPipelineExecutionEnabled` · `maxExecutableStepOrder` versus default
   (4) default account-number env correction (limited to the revision 1 → 2 correction)
       - `PORTFOLIO_BATCH_DEFAULT_ACCOUNT_NO` + `PORTFOLIO_VIEW_ACCOUNT_DEFAULT_ACCOUNT_NO`
       - 0 plaintext records of the raw 12-digit account number in this note / `[REDACTED_ACCOUNT_NO]` placeholder
   (5) `portfolio.batch.aws-stepfunctions-approval-state-machine-arn` env injection
       - actual ARN `[REDACTED_ARN]` placeholder / 0 plaintext records in this note
2) `secrets` block: complete
   (1) RDS connection info Secrets Manager injection first validation
   (2) the Resource ARN pattern retains the `/portfolio/paper/...` prefix-limited policy
       (R-SEC-005 / R-SEC-008 / R-DOCS-001 alignment)
   (3) 0 plaintext records of KIS app key · app secret · account number · DB password · Slack webhook URL
       - all `[REDACTED]` or placeholder

§3. Decision / risk mapping

1) No decision body change
   (1) OD-SEC-005 / OD-SEC-006 / OD-SEC-007 / OD-MS-002 / OD-MS-009 / OD-MS-037 first-validation memo reinforced without body changes
       - for detailed decision changes, see the `../_common/operator-decisions.md` Change Log 2026-06-30 (afternoon) item
2) Risk mapping

| Risk ID | This session's impact (summary) | Status |
| --- | --- | --- |
| R-AUTO-034 [2026-06-30 afternoon reinforcement] | first validation of the Fargate Task Role `states:StartExecution` grant limited to the approval ARN (details below) | Open (promotion conditions below) |
| R-AUTO-033 [2026-06-30 afternoon reinforcement] | operator IP/32 SG inbound + Public IP direct access + ECS View → Step 12~17 approval third validation passed | Mitigated retained (05 spec responsibility) |
| R-DOCS-001 / R-DOCS-002 / R-SEC-010 | sensitive-information plaintext recording prohibition policy alignment (details below) | alignment retained |

#### Details — R-AUTO-034 [2026-06-30 afternoon reinforcement]

- first validation of the Fargate Task Role `states:StartExecution` grant limited to the Step 12~17 approval state machine ARN.
- first validation of Task Role + Task Execution Role separation.
- 0 gate regression inside the Task Definition `environment`.
- Status Open retained — a `Mitigated` promotion candidate when both conditions below are met
  - both general + approval ARN limited grants
  - default ENABLE regression audit pass at the Fargate external-exposure time

#### Details — R-DOCS-001 / R-DOCS-002 / R-SEC-010

- the plaintext recording prohibition policy for the items below is retained as is
  - secret value / DB password / Slack webhook URL / Administrator password
  - actual ARN / image digest full sha256 / public IP / task ARN / ENI ID / raw broker_order_no

§4. Follow-up (06 spec follow-up phase responsibility)

1) Organize the `states:StartExecution` Resource pattern of the Fargate port-view Task Role
   - grant limited to both the general workflow ARN + approval workflow ARN
   (1) `simulate-principal-policy` validation + 0 Resource · Action wildcard audit
   (2) 0 plaintext records of the full IAM Policy body (R-DOCS-001 alignment)
2) Formal authoring of the Fargate task definition `secrets` block
   - separate RDS connection info + Slack webhook URL (R-AUTO-024 alignment) + KIS app key · app secret
3) Cross-spec audit of adding ACM / Route53 IAM privileges at the time of ALB / HTTPS / Route53 / Cloudflare Tunnel introduction
4) Re-validate the Fargate port-view-side secret at the DB password rotate (R-SEC-010 new) entry time
5) Cross-spec audit of the 06 spec-side IAM privileges when inspecting CloudWatch Logs alarms / DLQ / retry / failure Slack integration

§5. This date's factual recording scope

1) Kiro work = performed only the accumulation of this section in 06 spec `operation-notes.md`
2) Operator direct-performed = ECS task role / execution role creation · IAM Policy authoring · ECR pull · CloudWatch Logs connection ·
   Secrets Manager `GetSecretValue` work (operator area / 0 changes to this date's cross-service AWS Migration spec area)
3) 0 Kiro-side changes on this date to AWS CLI / boto3 / psql / Spring Boot execution / external API calls
   - 0 Kiro-side changes on this date to AWS resource creation · modification · deletion
4) 0 full plaintext quotations of the responses · bodies below
   - full IAM Policy body / `simulate-principal-policy` response body
   - Secrets Manager `GetSecretValue` response body / SSM Parameter Store response body
   - full Task Definition JSON body / Step Functions execution history body
   - Slack message body / full CloudWatch Logs / full Spring Boot application log
5) Confirm 0 plaintext records of sensitive information — see Placeholder Coverage.
6) Only operational identifiers recorded as facts — not secrets:
   - Task Role name `portfolio-paper-view-task-role`
   - Task Execution Role name `portfolio-paper-ecs-task-execution-role`
   - ECR `portfolio-view` / CloudWatch Logs `/ecs/portfolio-view`
   - Security Group `sgroup-port-view-ecs`
   - ECS cluster `portfolio-paper-cluster` / ECS service `portfolio-view-service`
   - task definition `portfolio-view:2`
   - Spring profile `aws-paper`
   - state machine `portfolio-paper-daily-step12-17-approval`
   - executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / status `SUCCEEDED`
   - Spring properties env labels / Secrets Manager prefix `/portfolio/paper/...`


## 2026-06-30 (afternoon) Slack — Daily Brief Builder Lambda Secrets Manager `valueFrom` + separation validation of 2 notification-dedicated IAM Roles

Separately from the preceding section (port-view ECS Fargate Task Role / Task Execution Role separation), this cumulatively records the facts of the new IAM Role separation on the Daily Brief Slack automation side + the Builder Lambda's DB password injection method, which the operator performed directly on the same date's afternoon.

Session overview

- Scope = 2 new IAM Role separations for Daily Brief Slack automation + Builder Lambda Secrets Manager `valueFrom`.
- 0 full plaintext quotations of the responses · bodies below (R-DOCS-001 alignment)
  - Lambda code body / full IAM Policy body / `simulate-principal-policy` response body
  - Secrets Manager `GetSecretValue` response body / Step Functions ASL body / Slack message body

§1. First validation of 2 new notification-dedicated IAM Role separations

1) `portfolio-daily-brief-sfn-role`: complete
   (1) responsibility = Builder Lambda + Notifier Lambda invoke privilege inside the Daily Brief mini Step Functions
       - Resource = limited to the Daily Brief Builder `portfolio-daily-brief-slack-summary-builder`
         + Notifier `portfolio-event-notifier`
       - separate from the Builder Lambda `portfolio-approval-slack-summary-builder`
       - Action = limited to `lambda:InvokeFunction`
       - 0 Action / Resource wildcards
   (2) 0 plaintext records of the actual ARN (`[REDACTED_ARN]` placeholder)
   (3) separated from the main Daily execution IAM Role (`portfolio-paper-stepfunctions-execution-role`)
       - isolated so that a notification-side privilege extension does not affect the main execution privilege (OD-SEC-005 / OD-SEC-006 alignment)
2) `portfolio-daily-brief-scheduler-role`: complete
   (1) responsibility = the mini state machine StartExecution privilege of the 2 Daily Brief Schedulers
       - Resource = limited to the `portfolio-daily-brief-slack-notification` state machine ARN
       - Action = limited to `states:StartExecution`
       - 0 Action / Resource wildcards
   (2) 0 plaintext records of the actual ARN (`[REDACTED_ARN]` placeholder)
   (3) separated from the main Daily execution Scheduler IAM Role (`portfolio-paper-eventbridge-scheduler-role`)
       / EC2 lifecycle Scheduler IAM Role

§2. First validation of the Builder Lambda DB password injection method

1) `portfolio-daily-brief-slack-summary-builder` Lambda configuration: complete
   (1) Lambda runtime
       - Python 3.12
       - DB driver `pg8000` (`psycopg2` not used)
   (2) DB password injection method
       - map Secrets Manager `valueFrom` to the Lambda environment variable `DB_PASSWORD_SECRET_VALUE_FROM`
       - runtime lookup of `secretsmanager:GetSecretValue` inside the Lambda code
       - 0 plaintext records of the Lambda environment variable / Secrets Manager `valueFrom` mapping / `GetSecretValue` response body
       - actual secret ARN `[REDACTED_SECRET_ARN]` placeholder
       - the secret value itself is used only in memory after the RDS connection time
       - 0 plaintext records in the Lambda CloudWatch Logs
       - OD-SEC-002 / R-DOCS-001 / R-SEC-010 alignment
2) RDS read responsibility
   (1) the Lambda connects directly to the RDS endpoint without depending on an SSM Port Forwarding waypoint (a Lambda inside the VPC)
       - 0 plaintext records of the RDS endpoint hostname in this spec note
       - limited to `connector_balance_snapshot` + `connector_position_snapshot` SELECT
       - 0 DDL / DML / 0 `executemany`
   (2) 0 plaintext quotations of the SELECT result — the operator notes record only the summary result as facts

§3. Builder Lambda-side IAM Policy (responsibility separation)

1) The Execution Role of the `portfolio-daily-brief-slack-summary-builder` Lambda
   (a separate Lambda's own Execution Role / 0 plaintext records of the IAM Role name in this note)
   (1) `secretsmanager:GetSecretValue` Resource = limited to the Daily Brief RDS app role secret ARN
   (2) RDS connection privilege = limited to RDS endpoint TCP 5432 outbound inside the VPC
   (3) `logs:CreateLogGroup` / `logs:CreateLogStream` / `logs:PutLogEvents`
       Resource = limited to `/aws/lambda/portfolio-daily-brief-slack-summary-builder`
   (4) 0 Action / Resource wildcards / formal matrix update as a 06 spec follow-up phase responsibility
2) 0 plaintext records of the actual IAM Policy body / `simulate-principal-policy` response body

§4. Decision / risk mapping

1) No decision body change
   - OD-SEC-002 (RDS master password Secrets Manager storage)
   - OD-SEC-005 (EC2 / 8 MS Access Key non-use principle)
   - OD-SEC-006 (per-MS Secret access separation)
   - OD-MS-030 (AWS common Slack notifier)
   - first-validation memo reinforced without body changes
   - for detailed decision changes, see the `../_common/operator-decisions.md` Change Log
     `2026-06-30 (오후) Slack` item + OD-MS-038 new
2) Risk mapping

| Risk ID | This session's impact | Status |
| --- | --- | --- |
| R-AUTO-035 (new) | risk of Daily Brief Slack automatic delivery failure or duplicate delivery | Mitigated (details below) |
| R-DOCS-001 / R-SEC-010 | prohibit plaintext recording of Lambda code body / Secrets Manager secret value / DB password / actual ARN | alignment retained / 0 additions on this date |
| R-AUTO-024 | risk of Slack webhook URL plaintext exposure (`Accepted`) | mitigation retained (details below) |

#### Details — R-AUTO-035 (new) mitigation

- mini Step Functions separation
- 2 Schedulers ENABLED
- manual smoke `SUCCEEDED`
- notification-dedicated IAM Role separation (0 privilege broadening)

#### Details — R-AUTO-024 mitigation

- keep the Notifier Lambda `portfolio-event-notifier` environment variable `SLACK_WEBHOOK_URL` plaintext
- migrate to Secrets Manager or SSM SecureString after operation stabilization
- 06 spec follow-up phase responsibility

§5. Follow-up (06 spec follow-up phase responsibility)

1) Formal definition of the Daily Brief Builder Lambda's IAM Policy
   - `simulate-principal-policy` validation + 0 Resource · Action wildcard audit
2) Migrate the Slack webhook URL from the Lambda environment variable to Secrets Manager or SSM SecureString (R-AUTO-024 alignment)
3) Periodic audit of the Resource pattern of `portfolio-daily-brief-sfn-role` / `portfolio-daily-brief-scheduler-role`
   - confirm the Daily Brief-dedicated ARN limitation
4) Audit of the Daily Brief Builder Lambda's RDS connection pooling / reuse policy
   - audit the number of Secrets Manager `GetSecretValue` calls at Lambda cold start
   - 2 per weekday = about 44 per month / the `GetSecretValue` unit price is very small, retained as is
5) Re-validate the Daily Brief Builder Lambda-side Secrets Manager secret at the DB password rotate (R-SEC-010) entry time

§6. This date's factual recording scope

1) Kiro work = performed only the accumulation of this section in 06 spec `operation-notes.md`
2) Operator direct-performed area
   - 2 new Builder Lambdas created + 1 new mini state machine created
   - 2 Schedulers ENABLED + 2 new IAM Roles created + IAM Policy authoring
   - Notifier formatter improvement + `portfolio-paper-daily-step1-17-approval` ASL update
3) 0 Kiro-side changes on this date to AWS CLI / boto3 / psql / Spring Boot execution / external API calls
   - 0 Kiro-side changes on this date to AWS resource creation · modification · deletion
4) 0 full plaintext quotations of the responses · bodies below
   - full IAM Policy body / `simulate-principal-policy` response body
   - Secrets Manager `GetSecretValue` response body / Step Functions ASL body
   - Lambda code body / Slack message body
   - full Builder output / full Notifier input / full CloudWatch Logs
5) Confirm 0 plaintext records of sensitive information — see Placeholder Coverage.
6) Only operational identifiers recorded as facts — not secrets:
   - IAM Role names `portfolio-daily-brief-sfn-role` · `portfolio-daily-brief-scheduler-role`
   - Builder Lambda names `portfolio-approval-slack-summary-builder` · `portfolio-daily-brief-slack-summary-builder`
   - Notifier Lambda name `portfolio-event-notifier`
   - state machine name `portfolio-daily-brief-slack-notification`
   - 2 Scheduler names
   - Lambda runtime `Python 3.12` / DB driver `pg8000`
   - DB password injection method `DB_PASSWORD_SECRET_VALUE_FROM`
   - Slack webhook environment-variable name `SLACK_WEBHOOK_URL`
   - Lambda log group `/aws/lambda/portfolio-daily-brief-slack-summary-builder`


## 2026-06-30 (afternoon) intraday stop-loss Slack — MarketConnector EC2 Instance Role inline policy (`lambda:InvokeFunction` Resource-limited) separation validation

As additional work on this date's afternoon, this cumulatively records the fact of the MarketConnector EC2 Instance Role inline policy grant within the 06 spec scope, during the intraday stop-loss Slack live integration that the operator performed directly.

Session overview

- Scope = new MarketConnector EC2 Instance Role inline policy grant (only operational identifiers recorded as facts).
- 0 full plaintext quotations of the responses · bodies below (R-DOCS-001 alignment)
  - full IAM Policy body / `simulate-principal-policy` response body
  - Lambda code body / `connector_intraday_position_evaluate.py` body / runner ps1 body
  - SSM response body / Slack message body / full CloudWatch Logs

§1. First validation of the MarketConnector EC2 Instance Role inline policy separation

1) Instance Role
   (1) name `portfolio-paper-marketconnector-ec2-role`
       - the 03 spec's formal operational EC2 (MarketConnector EC2) Instance Role
       - 0 plaintext records of the actual ARN (`[REDACTED_ARN]` placeholder)
   (2) the existing privilege scope before this inline policy grant (no change on this date)
       - SSM Session Manager / `AmazonSSMManagedInstanceCore` managed policy
       - KIS Secrets Manager / SSM Parameter Store read
       - RDS connection (based on the `marketconnector_app` DB role)
       - CloudWatch Logs write
       - S3 access_token backup
2) New inline policy grant
   (1) policy name `portfolio-paper-marketconnector-event-notifier-invoke`
   (2) action = `lambda:InvokeFunction`
   (3) Resource
       - limited to the `portfolio-event-notifier` Lambda
       - 0 plaintext records of the actual Lambda ARN (`[REDACTED_ARN]` placeholder)
       - 0 Resource · Action wildcards
       - OD-SEC-005 / OD-SEC-006 alignment
   (4) this inline policy is separated from all of the IAM Roles below
       - Daily Batch state machine / Step Functions execution role / EventBridge Scheduler role
       - the 2 Daily Brief notification-dedicated IAM Roles
         (`portfolio-daily-brief-sfn-role` · `portfolio-daily-brief-scheduler-role`)
3) EC2 invoke smoke passed
   (1) Notifier Lambda invoke smoke success from the EC2 side
   (2) 0 plaintext quotations of the Lambda response body
   (3) this smoke is for the purpose of pre-validating the IAM privilege of the `connector_intraday_position_evaluate.py --notify-slack` runtime call path

§2. MarketConnector evaluate's `DB_PASSWORD_SECRET_VALUE_FROM` policy alignment

1) DB password injection method
   (1) no change to the MarketConnector EC2-side DB password injection method in this date's addition either
   (2) MarketConnector evaluate retains the existing EC2 Instance Role-based Secrets Manager `GetSecretValue` runtime lookup policy
       (OD-SEC-002 / R-DOCS-001 / R-SEC-010 alignment)
   (3) separate from the Daily Brief Builder Lambda's `DB_PASSWORD_SECRET_VALUE_FROM` environment-variable valueFrom method
       - MarketConnector evaluate runs as EC2 venv Python, not a Lambda
       - the environment-variable injection method differs by the EC2-side operator setup / no change in this date's addition
2) Slack webhook URL-side policy alignment
   (1) keep the Notifier Lambda `portfolio-event-notifier` environment variable `SLACK_WEBHOOK_URL` plaintext (R-AUTO-024 `Accepted`)
   (2) MarketConnector evaluate does not directly know the Slack webhook URL
       - performs only Notifier Lambda invoke
       - the webhook URL plaintext exposure risk is separated as the Notifier Lambda side's responsibility
   (3) Slack webhook URL migration to Secrets Manager or SSM SecureString is a follow-up
       (R-AUTO-024 / 06 spec follow-up phase responsibility retained)

§3. Decision / risk mapping

1) No decision body change
   - OD-SEC-002 / OD-SEC-005 / OD-SEC-006 / OD-SEC-007 / OD-MS-001 / OD-MS-016 / OD-MS-030 / OD-MS-035 / OD-MS-036 / OD-MS-038
   - for detailed decision changes, see the `../_common/operator-decisions.md` Change Log `2026-06-30 (오후) 장중 손절 Slack` item
   - no new decision / no change to the Decision Summary count (total 97 / confirmed 52 / provisional 42 retained)
2) Risk mapping

| Risk ID | This session's impact | Status |
| --- | --- | --- |
| R-AUTO-036 (new) | risk of the side effect of a no-rollback policy when Slack delivery fails after intraday stop-loss READY creation | Mitigated (details below) |
| R-AUTO-035 [2026-06-30 afternoon additional reinforcement] | the Notifier Lambda call entry point is extended to the MarketConnector EC2 runner | Mitigated retained |
| R-AUTO-024 | risk of Slack webhook URL plaintext exposure (`Accepted`) | mitigation retained (details below) |
| R-SEC-010 | DB password plaintext exposure follow-up rotation (`Open`) | mitigation retained (details below) |
| R-DOCS-001 / R-DOCS-002 | body plaintext recording prohibition policy alignment (details below) | alignment retained |

#### Details — R-AUTO-036 mitigation core

- IAM privilege limited grant
- EC2 invoke smoke
- DB after-check reinforcement
- first pass on this date

#### Details — R-AUTO-024 mitigation (this session)

- migrate to Secrets Manager or SSM SecureString after operation stabilization
- 06 spec follow-up phase responsibility

#### Details — R-SEC-010 mitigation (this session)

- the fact of the operator's local session accidental exposure is recorded without the password value, only at the "credential rotation / history cleanup recommendation" level
- R-DOCS-001 / R-DOCS-002 alignment

#### Details — R-DOCS-001 / R-DOCS-002 targets (this session)

- prohibit plaintext recording of the IAM Policy body / Lambda code body
- `connector_intraday_position_evaluate.py` body / runner ps1 body
- SSM response body / `simulate-principal-policy` response body

§4. Follow-up (06 spec follow-up phase responsibility)

1) Formal definition of the MarketConnector EC2 Instance Role's inline policy matrix
   - `simulate-principal-policy` validation + 0 Resource · Action wildcard audit
   - including a unified-matrix alignment audit of the Daily Brief Builder Lambda Execution Role + 2 Daily Brief mini Step Functions IAM Roles +
     MarketConnector EC2 inline policy
2) Migrate the Slack webhook URL from the Notifier Lambda environment variable to Secrets Manager or SSM SecureString (R-AUTO-024 alignment)
3) Re-validate the MarketConnector EC2-side Secrets Manager secret at the DB password rotate (R-SEC-010) entry time
4) R-AUTO-036 operational detection automation — 05 · 06 spec follow-up phase responsibility
   - Lambda CloudWatch Logs metric filter
   - Slack delivery failure alarm
   - DB after-check ↔ Slack reception cross-reference automation
5) Cross-spec audit so that the Daily Brief Builder Lambda Execution Role + 2 Daily Brief mini Step Functions IAM Roles +
   MarketConnector EC2 inline policy all invoke only the `portfolio-event-notifier` Lambda
   (retain 0 privilege-broadening regression)

§5. This date's factual recording scope

1) Kiro work = performed only the accumulation of this section in 06 spec `operation-notes.md`
2) Operator direct-performed area
   - new inline policy grant to the MarketConnector EC2 Instance Role
   - EC2-side Lambda invoke smoke + 1 live runner safety validation
3) 0 Kiro-side changes on this date to AWS CLI / boto3 / psql / Spring Boot execution / external API calls / Lambda execution / Step Functions execution / SSM RunCommand
   / 0 Kiro-side changes on this date to AWS resource creation · modification · deletion
4) 0 full plaintext quotations of the responses · bodies below
   - full IAM Policy body / `simulate-principal-policy` response body
   - Secrets Manager `GetSecretValue` response body
   - Lambda code body / `connector_intraday_position_evaluate.py` body / runner ps1 body
   - SSM response body / Slack message body / full CloudWatch Logs
5) Confirm 0 plaintext records of sensitive information — see Placeholder Coverage.
6) Only operational identifiers recorded as facts — not secrets:
   - IAM Role name `portfolio-paper-marketconnector-ec2-role`
   - inline policy name `portfolio-paper-marketconnector-event-notifier-invoke`
   - Lambda name `portfolio-event-notifier`
   - state machine name `portfolio-paper-intraday-stop-sell-approval`
   - SSM commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131`
   - MarketConnector evaluate file path / deployment SHA256 / deployment version
   - runner file path / runner SHA256 / source_version


## 2026-07-01 — IAM · Secrets alignment at the Step 12~17 Scheduler ENABLE transition time (0 new resources)

At the 2026-07-01 aws-paper Daily automation first full ON entry time (Step 12~17 Scheduler
`portfolio-paper-daily-step12-17-order-0901-kst` DISABLED → ENABLED transition),
no new resource creation · policy extension from the IAM · Secrets perspective occurred.
This section is a short evidence reinforcement from the 06 spec perspective.

### 1) 0 new IAM Role · Policy · Secret creation

- 0 new IAM Roles
  - the Scheduler target IAM Role `portfolio-paper-eventbridge-scheduler-role`
  - the Dispatcher Lambda target IAM Role `portfolio-paper-daily-scheduler-dispatcher-role`
  - all reused as is, the roles already created at the 2026-06-23 EventBridge automation implementation time (OD-MS-032 alignment)
- 0 new inline policy · managed policy
  - Scheduler → Dispatcher Lambda invoke privilege
    (`lambda:InvokeFunction` Resource limited to `portfolio-paper-daily-scheduler-dispatcher`)
  - Dispatcher Lambda → Step Functions `StartExecution` privilege
    (Resource limited to the 2 target state machine ARNs / 0 Resource · Action wildcards)
  - confirmed the existing policy operates as is / 0 new statement additions on this date
- 0 new Secrets Manager secret
  - the Slack webhook URL is retained for first validation in the Notifier Lambda `portfolio-event-notifier` environment variable (`SLACK_WEBHOOK_URL`)
    (R-AUTO-024 `Accepted` alignment)
  - migration to Secrets Manager or SSM SecureString after operation stabilization remains a 06 spec follow-up phase responsibility
  - 0 new secret grants at any of the Dispatcher Lambda / Scheduler / Step Functions layers
- 0 new KMS key · rotation policy changes — the existing KMS · rotation policy retained as is

### 2) Evidence that the existing Scheduler role · Dispatcher Lambda privilege path operates

- Step 12~17 Scheduler ENABLE transition privilege path
  - the Scheduler role `portfolio-paper-eventbridge-scheduler-role`
    invokes the Dispatcher Lambda `portfolio-paper-daily-scheduler-dispatcher`
  - the Dispatcher Lambda calls Step Functions `portfolio-paper-daily-step12-17-approval` `StartExecution`
    with the IAM Role `portfolio-paper-daily-scheduler-dispatcher-role`
  - the existing privilege path operates as is
- Manual execution (not via the Dispatcher Lambda)
  - executionName `port-manual-daily-step12-17-20260701-043747` / status `SUCCEEDED`
  - a round where the operator directly called `start-execution`
  - Step Functions execution role existing privilege path alignment confirmed — the 3 below
    - (a) MarketConnector EC2 SSM RunCommand call privilege
    - (b) Notifier Lambda `portfolio-event-notifier` invoke privilege
    - (c) EC2 lifecycle Lambda `portfolio-paper-ec2-lifecycle-dispatcher` invoke privilege
  - 0 new statement additions / 0 Resource · Action wildcards policy retained as is (OD-SEC-005 / OD-SEC-006 alignment)
- The `states:StartExecution` Resource pattern of the Fargate ECS View Task Role `portfolio-paper-view-task-role`
  - limited to both the general workflow ARN + approval workflow ARN
  - 0 changes on this date
  - the R-AUTO-034 mitigation follow-up (Fargate Task Role privilege separation) remains a 06 spec follow-up phase responsibility

### 3) Re-confirmation of the per-MS Secret access separation policy

- MarketConnector EC2 role `portfolio-paper-marketconnector-ec2-role`
  - existing inline policy retained (KIS Secret · RDS marketconnector-app Secret · Notifier Lambda invoke)
  - 0 new statement additions on this date
  - even in the Step 12 manual execution SSM RunCommand → MarketConnector EC2 → `connector_strategy_order_execute.py` flow,
    the existing Secret access policy operates as is (OD-SEC-006 alignment)
- Strategy Execution ECS Fargate Task Role
  - existing policy as is. The entrypoints after Step 12 operate within the existing policy
    - `daily_auto_buy_execute_run` / `daily_auto_sell_execute_run` / fill sync / position sync
  - existing policy such as execution-app Secret · decision-app Secret retained / 0 new statement additions on this date
- Notifier Lambda `portfolio-event-notifier` role `portfolio-event-notifier-lambda-role`
  - Slack webhook delivery responsibility / 0 new statement additions on this date
  - the Slack webhook URL is retained as a Lambda environment variable (Secrets Manager migration is a R-AUTO-024 `Accepted` follow-up phase)

### 4) Validation result

- 0 new IAM Role · Policy · Secret · KMS resource creation.
- the existing Scheduler · Dispatcher Lambda · Step Functions · Notifier Lambda · ECS Task Role · EC2 role privilege paths operate as is.
- no 06 spec decision body change — OD-SEC-005 / OD-SEC-006 / OD-SEC-007 retained as is.
- 06 spec follow-up phase responsibilities retained as is
  - Slack webhook URL Secrets Manager migration
  - Fargate Task Role `states:StartExecution` Resource pattern organization
  - rotation policy / audit automation

### 5) Re-confirmation of the safety policy

- **This change is limited to aws-paper. It does not change the aws-live automatic BUY / SELL policy,
  and live retains the candidate + manual-approval-first policy.** (OD-SAFE-002 / OD-SAFE-003 alignment)
- 0 plaintext records of actual IAM Role · Policy · Secret · KMS-related values in this spec's artifacts — all `[REDACTED]`-family placeholders
  (R-DOCS-001 alignment). Targets: actual ARN · raw 12-digit account-id · Slack webhook URL · Secret ARN · IAM access key id.
- Only operational identifiers recorded as facts — not secrets:
  - IAM Role names / Scheduler names / Dispatcher Lambda name / Notifier Lambda name / state machine names
  - executionName / Slack event label / balance snapshot id / as_of_date / amount / count
