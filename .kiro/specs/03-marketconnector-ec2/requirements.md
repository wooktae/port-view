# Requirements Document — 03-marketconnector-ec2

## Introduction

This spec is the 3rd stage of the PORT-STRATEGY-AI AWS Migration. It fixes the criteria for authoring the procedure / validation / operation notes that transition the already-created MarketConnector EC2 into a formal operating form (Connector / Flask / KIS API / RDS connection).

The first application of the preceding specs is all complete.

Applied as operator direct work in [`../02-aws-network-and-rds`](../02-aws-network-and-rds):

- VPC / Subnet / SG
- VPC Endpoint (Secrets Manager / SSM / CloudWatch Logs / ECR / S3)
- RDS PostgreSQL
- 7 DB roles (`marketconnector_app`, `view_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `research_app`, `execution_app`)

First-locked in [`../06-secrets-and-iam`](../06-secrets-and-iam):

- Secrets Manager / SSM Parameter Store classification criteria
- naming rule `/portfolio/{env}/{service}/{item}`
- MarketConnector EC2 Instance Role least-privilege read policy
- No-Access-Key principle inside the EC2

At the time of this spec (03) work, the MarketConnector EC2 has already gone through the following two stages.

- 2026-06-09: the aws-paper MarketConnector EC2 (Amazon Linux 2023, public subnet, EIP attach) was newly created as the RDS restore runner.
  - Moved the dump file along the path local PostgreSQL → S3 temporary bucket → EC2 → private RDS.
  - Completed the restore and consistency validation to aws-paper RDS PostgreSQL 18.4 with PostgreSQL client / pg_restore 18.4.
  - Reference: [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-09 section, [`../02-aws-network-and-rds/runbook.md`](../02-aws-network-and-rds/runbook.md) appendix A.
- 2026-06-10: the same EC2 was transitioned into the MarketConnector formal operating EC2. The operator directly validated the following 8 items, and all were confirmed as success.
  - Python 3.9.25 / venv configuration
  - Dependency library (`requests`, `flask`, `psycopg2-binary`, `psycopg`, `pandas`) install
  - RDS connection based on the `marketconnector_app` user
  - `connector_balance.py` / `connector_order_check.py` execution
  - Flask internal smoke test
  - Secrets Manager / SSM Parameter Store-based env injection
  - Execution without an Access Key based on the EC2 Instance Role

The first-application environment of this spec is `aws-paper`. The core is narrowed to the one line below.

- The already-created MarketConnector EC2 takes the first-application results of 02 / 06 as input, and this spec fixes the criteria for authoring the procedure / validation / operation notes so that it is formally transitioned into an operating form based on the KIS Connector (Flask) / `marketconnector_app`-based RDS connection / Secrets Manager and SSM Parameter Store env injection / Instance Role-based no-Access-Key operation; it also accumulates the 8 validation results from 2026-06-10 into the deliverables.

This spec does not include the following (out of scope).

- EC2 new creation (already complete, handled as an already-complete item).
- New order / buy / sell / cancel / modify calls. Validation at this spec point performs only read-only smoke tests.
- live rotation automation (same as out of scope for the 06 spec).
- CI/CD OIDC / GitHub Actions Role design (07 spec).
- Full IAM matrix across all 8 MS (split across 04 / 05 / 08 / 09 specs).
- aws-live IAM (consolidated in the 10 spec).
- Modifying the README / AGENTS.md / CHANGELOG / docs / worklog and source code of the 8 MS.

Deliverables inside this spec (the 03 folder):

- [`requirements.md`](./requirements.md)
- [`design.md`](./design.md)
- [`tasks.md`](./tasks.md)
- [`runbook.md`](./runbook.md)
- [`validation-checklist.md`](./validation-checklist.md)
- [`operation-notes.md`](./operation-notes.md)
- Auxiliary document (if needed, `traceability-matrix.md`)

In this phase, only `requirements.md` is authored; the remaining documents are created in follow-up phases.

Root common reference / update-candidate documents:

- [`../_common/operator-decisions.md`](../_common/operator-decisions.md)
- [`../_common/followups-overview.md`](../_common/followups-overview.md)
- [`../_common/risk-register.md`](../_common/risk-register.md)
- [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md)

The actual update is performed at the tasks stage of this spec.

Preceding specs:

- [`../01-aws-migration-foundation`](../01-aws-migration-foundation)
- [`../02-aws-network-and-rds`](../02-aws-network-and-rds)
- [`../06-secrets-and-iam`](../06-secrets-and-iam)

The decisions of this spec are used as input for the following follow-up specs.

- [`../04-strategy-batch-stepfunctions`](../04-strategy-batch-stepfunctions) (planned)
- [`../05-port-view-ecs-and-runbook`](../05-port-view-ecs-and-runbook) (planned)
- [`../08-interest-crawler-and-preprocessor-ecs`](../08-interest-crawler-and-preprocessor-ecs) (planned)
- [`../09-strategy-research-batch`](../09-strategy-research-batch) (planned)
- [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook) (planned)

This document never records the actual values of the items below. Everywhere it uses only `[REDACTED]` or placeholders (`<account-id>`, `<region>`, `<instance-id>`, `<eip>`, `<rds-endpoint>`).

- Actual secret value / RDS password / token / Slack webhook URL
- KIS app key / KIS app secret / account number
- RDS endpoint hostname / account-id
- Actual secret ARN / IAM access key id
- instance-id / EIP

## Glossary

- MarketConnector EC2: the EC2 instance that runs the aws-paper KIS Connector. It was created first as the RDS restore runner at the 02 spec point, and in this spec (03) it is transitioned into the formal operating form of the KIS Connector / Flask app.
- Already-complete item: an item that the operator already applied directly at the start of this spec (the EC2 instance itself, the Amazon Linux 2023 OS, public subnet placement, EIP attach, the default Security Group connection, etc.). This spec takes these as input and does not re-create or change them.
- Formal operating form: the state on the MarketConnector EC2 where all of the following are provisioned.
  - Python 3.9.25 venv
  - Dependency libraries
  - KIS Connector entrypoint (Flask `connector_app.py`, `connector_balance.py`, `connector_order_check.py`, `connector_quote_realtime.py`, `connector_quote_closed.py`, `connector_view_service.py`)
  - RDS connection based on `marketconnector_app`
  - Secrets Manager / SSM Parameter Store env injection
  - Instance Role-based no-Access-Key operation
- Temporary validation stage: the operating mode at this spec point. A stage where environment variables are injected in the form of a shell or a temporary export script and the operator manually runs the entrypoint. The transition to systemd unit-based or startup script-based normal operating mode is left as the responsibility of a follow-up task of this spec or a separate phase.
- Normal operating mode: the mode where a systemd unit or startup script automatically starts the Connector / Flask process at EC2 boot / restart time, memory-exports the env injection, and then maintains the KIS API / RDS connection. This spec states only the form / responsibility scope; the actual systemd unit authoring is the responsibility of a follow-up task of this spec or a separate phase.
- Read-only smoke test: validation that does not trigger any broker-side new order / buy / sell / cancel / modify call. It performs only balance queries, held-stock queries, order / fill queries, and Flask read-only endpoint (`/api/v1/view/...`) calls.
- IMDSv2 + Instance Role credentials: the method of using the temporary credentials issued by the EC2 metadata service (IMDSv2) with Instance Role permissions. No long-lived access key / secret access key is stored inside the EC2. Consistent with the 06 spec OD-SEC-005 / OD-SEC-006 candidate decisions.
- 8 validation results from 6/10: the pass results of the 8 validation items the operator performed directly on 2026-06-10 (Python 3.9.25 / venv configuration / dependency library install / RDS connection / `connector_balance.py` execution / `connector_order_check.py` execution / Flask internal smoke test / Secrets Manager · SSM env injection / Instance Role-based no-Access-Key execution). All are targets for accumulated recording in this spec's deliverables.

## Requirements

### Requirement 1: Scope and Out of Scope of This Spec

**Objective**: As the operator, I want to explicitly receive the scope and out of scope of this spec (03), so that a follow-up spec (04 / 05 / 08 / 09 / 10) or the operator can grasp the work boundary of this spec at a glance.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL state the first-application environment of this spec as `aws-paper` and state that `aws-live` is out of scope for this spec.
2. WHEN design.md is authored, THE design.md SHALL state the following items as in scope for this spec.
   - The Amazon Linux 2023 OS of the already-created MarketConnector EC2
   - Python 3.9.25 venv
   - Dependency libraries (`requests`, `flask`, `psycopg2-binary`, `psycopg`, `pandas`)
   - PostgreSQL client (18.4) / pg_restore (18.4)
   - KIS Connector (Flask)
   - RDS connection based on `marketconnector_app`
   - Secrets Manager · SSM Parameter Store env injection (handed over by 06)
   - Instance Role-based no Access Key (handed over by 06)
   - Documentation of the 8 validation results from 2026-06-10
3. WHEN design.md is authored, THE design.md SHALL state the following items as out of scope for this spec.
   - EC2 new creation (an already-complete preceding item)
   - New order / buy / sell / cancel / modify calls (read-only only)
   - live rotation automation (same as out of scope for 06)
   - CI/CD OIDC / GitHub Actions Role (07 spec)
   - Full IAM matrix across all 8 MS (split across 04 / 05 / 08 / 09)
   - aws-live IAM matrix (10 spec)
   - Modifying the code / README / AGENTS.md / CHANGELOG / docs / worklog of the 8 MS
4. WHERE a follow-up spec (04 / 05 / 08 / 09 / 10) uses the decisions of this spec as input, THE design.md SHALL state the items that this spec hands over to follow-up specs (EC2 operating pattern / Instance Role permissions / env injection flow / validation label policy).
5. WHEN design.md is authored, THE design.md SHALL state the phase separation policy that, at this phase (requirements), no deliverable other than `requirements.md` (`design.md`, `tasks.md`, `runbook.md`, `validation-checklist.md`, `operation-notes.md`) is created.

### Requirement 2: Stating EC2 Already-Complete Items

**Objective**: As the operator, I want to receive, in one place, the EC2 already-complete items that are already provisioned at the start of this spec, so that this spec's work is forced not to re-attempt EC2 new creation or change an already-complete item.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL state the following as the already-complete items of the MarketConnector EC2: Amazon Linux 2023, public subnet placement, EIP attach, default Security Group connection, and completed application of the 02 spec SG matrix (in particular allowing `sg-marketconnector-ec2` ↔ `sg-rds-postgres` 5432 inbound).
2. WHEN design.md deals with the identifiers of the already-complete items (instance type, EBS volume size, instance-id, EIP value, etc.), THE design.md SHALL not record the actual values and SHALL use only placeholders (`<instance-type>`, `<ebs-size>`, `<instance-id>`, `<eip>`) or `[REDACTED]`.
3. WHEN design.md deals with the already-complete items of the IAM Role / Instance Profile, THE design.md SHALL state that it assumes as input that `portfolio-paper-marketconnector-ec2-role` and `portfolio-paper-marketconnector-ec2-profile` fixed in the 06 spec are attached to this EC2.
4. WHEN design.md deals with the already-complete items, THE design.md SHALL state the policy that this spec's work does not perform EC2 new creation / instance type change / EBS re-creation / public subnet change / EIP detach or replacement.
5. IF one of the already-complete items is found by operator inspection to differ from this spec's assumptions, THEN THE design.md SHALL state the policy that, instead of an arbitrary change within this spec, only a change proposal is recorded in [`../_common/operator-decisions.md`](../_common/operator-decisions.md) and applied only after operator approval.

### Requirement 3: Operator Hand-over Facts (2026-06-09 RDS restore runner → 2026-06-10 MarketConnector operating transition)

**Objective**: As the operator, I want to receive the criteria for accumulating in the deliverables the fact that the same EC2 was transitioned from the 2026-06-09 RDS restore runner to the 2026-06-10 MarketConnector formal operating EC2, so that a follow-up spec / operations retrospective can trace the EC2 usage history at a glance.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL state the fact that at the 2026-06-09 point this EC2 was used as the RDS restore runner and that the restore and consistency validation to aws-paper RDS PostgreSQL 18.4 was completed based on PostgreSQL client / pg_restore 18.4.
2. WHEN design.md is authored, THE design.md SHALL state the policy that this RDS is operated with a private endpoint and is reachable only from EC2 within the same VPC (input for R-NET-004 mitigation).
3. WHEN design.md is authored, THE design.md SHALL state that at the 2026-06-10 point the same EC2 was transitioned into the MarketConnector formal operating EC2 and that the 8 validation results of this spec must all be recorded as success.
4. WHEN operation-notes.md is authored in a follow-up phase, THE operation-notes.md SHALL include, as a per-date accumulated record, one line for the 2026-06-09 RDS restore runner usage fact, one line for the 2026-06-10 MarketConnector operating transition fact, and one line per item for the 8 validation results.
5. WHEN operation-notes.md records the above facts, THE operation-notes.md SHALL not record the actual dump file path / S3 bucket name / RDS endpoint hostname / actual EIP / actual instance-id in plaintext and SHALL use only a placeholder or `[REDACTED]`.

### Requirement 4: Python / venv / Dependency Library Configuration

**Objective**: As the operator, I want to receive the configuration criteria for the MarketConnector EC2 Python runtime (Python 3.9.25 / venv / dependency libraries), so that all follow-up phase deliverables of this spec author validation / procedures under the same runtime assumptions.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL state the Python version on the MarketConnector EC2 as `3.9.25` and SHALL recommend a 1-venv configuration.
2. WHEN design.md deals with the venv location / name, THE design.md SHALL not force an actual absolute path (e.g., `/opt/portfolio/...`) or the operator home path itself, and SHALL use only placeholders (`<venv-path>`, `<venv-name>`).
3. WHEN design.md deals with the dependency libraries, THE design.md SHALL state the 5 items `requests`, `flask`, `psycopg2-binary`, `psycopg`, `pandas` as the first-application dependency libraries at this spec point.
4. WHEN design.md deals with additional dependency libraries, THE design.md SHALL state that any library beyond the 5 above (e.g., an auxiliary package to be added by operator decision) is separated as the responsibility of a follow-up phase of this spec or a separate spec.
5. WHEN design.md deals with the relationship to the 8 MS source / requirements.txt, THE design.md SHALL state the policy that this spec's work does not modify the source code / `requirements.txt` / `setup.py` / `pyproject.toml` and other packaging files of the 8 MS.
6. WHEN design.md deals with the dependency library install result, THE design.md SHALL state the policy that only the install success / failure result is recorded in the deliverables and that the actual PyPI mirror used, the pip cache path, and the operator user home absolute path are not recorded in this document.

### Requirement 5: PostgreSQL client / pg_restore 18.4 Usage Criteria

**Objective**: As the operator, I want to receive the criteria that the PostgreSQL client / pg_restore 18.4 used in the 2026-06-09 RDS restore is maintained as-is at this spec point, so that the client / engine major version mismatch risk (R-DATA-003) does not recur at this spec point.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL state that the major version of the PostgreSQL client / pg_restore on the MarketConnector EC2 is maintained at `18.4`.
2. WHEN design.md deals with compatibility with the RDS engine version, THE design.md SHALL include a one-line note that the aws-paper RDS engine is operating on PostgreSQL 18.4 (see 02 spec appendix A) and that client 18 is also generally compatible with RDS PostgreSQL 16 (or a higher minor version) (citing the R-DATA-003 mitigation).
3. WHEN design.md deals with the client downgrade scenario, THE design.md SHALL state the policy of not downgrading the client below 18.4 at this spec point.
4. IF during operation the dump source major version is changed or the RDS engine major version is changed, THEN THE design.md SHALL state the policy that it is handled as a decision of a follow-up spec (02 or 10) instead of an arbitrary change within this spec.
   - Reference: [`../02-aws-network-and-rds/runbook.md`](../02-aws-network-and-rds/runbook.md) appendix A
   - Reference: re-confirm the [`../_common/risk-register.md`](../_common/risk-register.md) R-DATA-003 mitigation
5. WHEN runbook.md is authored in a follow-up phase, THE runbook.md SHALL include a step that confirms client major version 18 from the output of `pg_dump --version`, `pg_restore --version`, `psql --version`.

### Requirement 6: RDS `marketconnector_app` Connection Operating Criteria

**Objective**: As the operator, I want to receive the operating criteria for the MarketConnector EC2 connecting to the private RDS with the `marketconnector_app` DB role, so that the application's RDS access operates normally while no direct RDS connection occurs from outside the EC2.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL state that the aws-paper RDS exposes only a private endpoint and that only this EC2's SG (`sg-marketconnector-ec2`) → RDS SG (`sg-rds-postgres`) 5432 inbound is allowed (02 spec SG matrix input).
2. WHEN design.md deals with the application connection user, THE design.md SHALL state that it connects only with the `marketconnector_app` DB role, that it does not perform day-to-day application connections with the `portfolio_admin` identity, and that this is consistent with the OD-DB-008 (execution R-only) decision and the OD-DB-007 (legacy not granted) decision.
3. WHEN design.md deals with connection info injection, THE design.md SHALL take the 06 spec `/portfolio/paper/rds/marketconnector-app` JSON multi-key secret as input for this spec and SHALL state the following environment variable mapping.
   - `host` → `INTEREST_DB_HOST`
   - `port` → `INTEREST_DB_PORT`
   - `dbname` → `INTEREST_DB_NAME`
   - `username` → `INTEREST_DB_USER`
   - `password` → `INTEREST_DB_PASSWORD`
4. WHEN design.md deals with hostname notation in this spec's deliverables, THE design.md SHALL state the policy that the actual RDS endpoint hostname is not recorded in plaintext anywhere in this document / runbook / validation-checklist / operation-notes and that only a placeholder (`<rds-endpoint>`) or `[REDACTED]` is used.
5. IF an RDS connection request occurs from outside (outside the VPC, the operator's local PC), THEN THE design.md SHALL, per the R-NET-004 mitigation, force the operating criteria to access only via EC2 + SSM Session Manager or EC2 Instance Connect, and SHALL state that an external IP is not allowed directly on the RDS SG (adjacent to the R-SEC-001 mitigation).
6. WHEN design.md deals with RDS DDL/DML at this spec point, THE design.md SHALL state the policy that in every phase of this spec no additional DDL/DML is performed beyond the read-only SELECT that the application triggers (consistent with R14).

### Requirement 7: KIS Connector / Flask Operating Form

**Objective**: As the operator, I want to receive the KIS Connector (Flask) operating form on the MarketConnector EC2 separated into the two stages of temporary validation and normal operating mode, so that the responsibility between validation at this spec point and the normal operating mode to be carried into a follow-up / separate phase of this spec becomes clear.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL state that the following entrypoints of the `port-marketconnector` repo must be operable in the normal operating form on the EC2: `connector_app.py`, `connector_balance.py`, `connector_order_check.py`, `connector_quote_realtime.py`, `connector_quote_closed.py`, `connector_view_service.py`.
2. WHEN design.md deals with the operating mode at this spec point, THE design.md SHALL state that this spec point is a temporary validation stage (environment variable injection based on a shell session or a temporary export script + operator manual entrypoint execution), and SHALL state that the transition to systemd unit-based or startup script-based normal operating mode is the responsibility of a follow-up task of this spec or a separate phase.
3. WHEN design.md deals with the responsibility boundary of the transition to normal operating mode, THE design.md SHALL state that the following items are the responsibility of a follow-up task of this spec or a separate phase: authoring the systemd unit / authoring the startup script / forcing Flask debug mode OFF / process restart policy / log location decision.
4. WHEN design.md deals with the new order / buy / sell / cancel / modify entrypoints (`connector_buy.py`, `connector_sell.py`, `connector_cancel.py`, `connector_modify.py`), THE design.md SHALL state the policy that the above entrypoints are not executed in any phase of this spec.
5. WHEN runbook.md is authored in a follow-up phase, THE runbook.md SHALL include the Flask startup command of the temporary validation stage (e.g., `python connector_app.py`) and read-only endpoint call patterns on a placeholder basis, but SHALL never include new-order endpoint call examples.
6. IF the operator wants to attempt the transition to systemd unit-based normal operating mode at this spec point, THEN THE design.md SHALL state the policy of separating it as a follow-up task of this spec and applying it only at the entry point of a separate phase (e.g., a follow-up 03 phase) or a separate spec.

### Requirement 8: Secrets Manager / SSM Parameter Store env Injection

**Objective**: As the operator, I want to receive as input to this spec the Secrets Manager / SSM Parameter Store classification / naming / injection flow first-locked in the 06 spec, so that the MarketConnector EC2 reads the KIS / RDS / general config values with only environment variables and no code change.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL state that it takes the 06 spec §1 (classification criteria) / §2 (naming matrix) / §3 (environment variable compatibility) tables as input for this spec as-is.
2. WHEN design.md deals with environment variable mapping, THE design.md SHALL include the following key flow.
   - KIS-related: `APP_KEY`, `APP_SECRET`, `BASE_URL`, `PAPER_ACNT`, `ACNT_PRDT_CD`
   - RDS-related: `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`
   - General config: `PORT_ENVIRONMENT`, `PORT_BROKER_NAME`
3. WHEN design.md deals with the temporary export script pattern (`/tmp/inject-env.sh` etc.), THE design.md SHALL take the pattern of the 06 spec runbook §6-1 as input for the temporary validation stage of this spec, and SHALL state that this spec holds the responsibility to transition to normal operating mode (systemd `EnvironmentFile=` or startup script memory export) in a follow-up task of this spec or a separate phase.
4. WHEN design.md deals with the security policy of the temporary export script, THE design.md SHALL state the policy that no secret value is stored in plaintext anywhere in the script / `EnvironmentFile=` body, that only a memory export is performed, and that the script permission is recommended to be a restricted permission such as 700 / 600 by operator decision (adjacent to the R-SEC candidate in §12).
5. WHEN design.md deals with the environment variable key compatibility of this spec, THE design.md SHALL state that the KIS / RDS identifiers are externalized with only environment variable injection and no modification of the 8 MS code / `port-marketconnector` `config.py` (OD-DB-003 input).
6. WHEN runbook.md is authored in a follow-up phase, THE runbook.md SHALL separate the temporary export script authoring, secret / parameter read, env injection, and the KIS / RDS read-only smoke test pass step after Connector restart into the [실행] / [확인] / [준비] / [복구] labels.
7. WHEN this spec's deliverable deals with a secret / parameter value, THE deliverable SHALL never record the actual secret value, KIS app key / app secret, account number, RDS password, or RDS endpoint hostname in plaintext and SHALL use only `[REDACTED]` or a placeholder everywhere.

### Requirement 9: Instance Role-Based No-Access-Key Operation

**Objective**: As the operator, I want to enforce once more at this spec point the operating criteria that no IAM access key / secret access key is stored inside the MarketConnector EC2 and only IMDSv2 + Instance Role credentials are used, so that the 06 spec OD-SEC-005 / OD-SEC-006 candidate decisions and the R-SEC candidate risk are consistently controlled at this spec point as well.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL again state the policy that no IAM access key id and secret access key is stored in any file / environment variable / dotfile / systemd unit / `EnvironmentFile=` / application config inside this EC2 (06 spec §5 input).
2. WHEN design.md deals with the credential flow at this spec point, THE design.md SHALL state the following as the normal state at this spec point.
   - The `Arn` of the `aws sts get-caller-identity` result is in the form `arn:aws:sts::<account-id>:assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>`
   - The `access_key` Source of `aws configure list` is `iam-role` or `Ec2InstanceMetadata`
3. WHEN design.md deals with the Instance Role auxiliary permissions at this spec point, THE design.md SHALL state the following items as the responsibility scope of this spec: `AmazonSSMManagedInstanceCore` managed policy attach for SSM Session Manager access, and granting CloudWatch Logs write permission (sending application logs / Connector logs).
4. WHEN design.md deals with the responsibility boundary between 06 spec permissions and this spec's permissions, THE design.md SHALL state the split that the 06 spec is responsible for the secret / parameter read permission and this spec (03) is responsible for EC2 operating auxiliary permissions such as SSM Session Manager / CloudWatch Logs write (06 spec §4.4 input).
5. WHEN runbook.md is authored in a follow-up phase, THE runbook.md SHALL include steps to check the absence of `~/.aws/credentials` inside the EC2, to check for 0 matches of the `aws_access_key_id` / `aws_secret_access_key` pattern via grep, and to check for 0 access key patterns inside dotfiles / systemd `EnvironmentFile=`.
6. IF an access key is found inside the EC2 during this spec's operation, THEN THE design.md SHALL state that a [복구] procedure to immediately discard the access key in the IAM Console, move the EC2's `~/.aws/credentials` to a backup, and revert to the mode using only IMDSv2 + Instance Role credentials must be included in runbook.md.
7. WHEN this spec's deliverable deals with an access key, THE deliverable SHALL never record the actual access key id / secret access key value and SHALL use only `[REDACTED]` everywhere.

### Requirement 10: Recording Requirement for the 8 Validation Results from 2026-06-10

**Objective**: As the operator, I want to receive the criteria so that the 8 validation results passed at the 2026-06-10 point are accumulated in this spec's deliverables, so that a follow-up spec or operations retrospective can trace at a glance the formal operating transition point of this EC2.

#### Acceptance Criteria

1. WHEN operation-notes.md is authored in a follow-up phase, THE operation-notes.md SHALL record the following 8 items as a per-date accumulation (`## 2026-06-10 ...`).
   - (a) Python 3.9.25 / venv configuration complete = success
   - (b) `requests` / `flask` / `psycopg2-binary` / `psycopg` / `pandas` install complete = success
   - (c) RDS connection based on `marketconnector_app` success
   - (d) `connector_balance.py` execution success
   - (e) `connector_order_check.py` execution success
   - (f) Flask internal smoke test success
   - (g) Secrets Manager / SSM Parameter Store-based env injection success
   - (h) Execution without an Access Key based on the EC2 Instance Role success
2. WHEN validation-checklist.md is authored in a follow-up phase, THE validation-checklist.md SHALL separate each of the 8 items above into a distinct check item and mark it with one of the 4 labels (`[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]`). At this spec point, all 8 items are marked as `[O]` or as an operator-directly-confirmed result.
3. WHEN operation-notes.md records the results of the 8 items above, THE operation-notes.md SHALL not record the actual RDS endpoint hostname / account number / token / KIS app key / KIS app secret / actual secret ARN / instance-id / EIP / actual account-id in plaintext and SHALL use only `[REDACTED]` or a placeholder everywhere.
4. WHEN validation-checklist.md deals with the 8 check items, THE validation-checklist.md SHALL follow the label / format policy of the 02 / 06 specs as-is (4 labels, per-date accumulated format, secret `[REDACTED]`).
5. IF one of the 8 items turns to failure at a future check point, THEN THE design.md SHALL state the policy that, instead of an arbitrary decision within this spec, the label is updated to `[X]` or `[Kiro 후속 작업 필요]`, the cause is recorded as an R-* candidate in [`../_common/risk-register.md`](../_common/risk-register.md), and then the operator approval process is followed.
6. WHEN this spec's deliverable cites the 8 results, THE deliverable SHALL record only the result itself (success / failure), the check date, and the fact that the operator confirmed it directly, and SHALL not paste the stdout / stderr content itself of the validation commands or the secret value into the body as-is.

### Requirement 11: Operator Procedure / Validation / Operation Notes Deliverable Requirement

**Objective**: As the operator, I want to first-fix the format and label rules of this spec's follow-up phase deliverables (`runbook.md`, `validation-checklist.md`, `operation-notes.md`), so that the operator can proceed with the work / checks / accumulated recording using the same check flow / label rules as the 02 / 06 specs.

#### Acceptance Criteria

1. WHEN this spec's follow-up phase proceeds, THE follow-up phase SHALL create the `runbook.md`, `validation-checklist.md`, `operation-notes.md` deliverables inside this spec folder.
2. WHEN runbook.md is authored, THE runbook.md SHALL attach the [실행] / [확인] / [준비] / [복구] labels to each step and follow as-is the security principles of the 02 / 06 spec runbooks (secret `[REDACTED]`, no modification of 8 MS code / docs, no external calls).
3. WHEN runbook.md deals with the step decomposition, THE runbook.md SHALL include the following items step by step.
   - (a) Pre-check (EC2 / SG / Instance Profile attach state / Python / venv / client 18 version)
   - (b) Confirm dependency library install result
   - (c) Author temporary export script / inject environment variables after source
   - (d) Confirm RDS connection based on `marketconnector_app`
   - (e) Execute `connector_balance.py` / `connector_order_check.py`
   - (f) Flask internal smoke test
   - (g) Check Instance Role credentials / absence of Access Key
   - (h) Accumulate the 8 results into operator notes
4. WHEN validation-checklist.md is authored, THE validation-checklist.md SHALL use only the following 4 labels: `[O]`, `[X]`, `[Kiro 후속 작업 필요]`, `[운영자 확인 필요]`. No other label is used.
5. WHEN validation-checklist.md deals with the check areas, THE validation-checklist.md SHALL include all of the following areas.
   - (a) Python / venv / dependency library inventory
   - (b) PostgreSQL client / pg_restore version
   - (c) `marketconnector_app` RDS connection result
   - (d) Connector / Flask read-only smoke test result
   - (e) Secrets Manager / SSM Parameter Store env injection result
   - (f) Instance Role credentials / absence of Access Key result
   - (g) Validation of 0 new order / buy / sell / cancel / modify calls
6. WHEN operation-notes.md is authored, THE operation-notes.md SHALL follow as-is the per-date accumulated record format (`## YYYY-MM-DD ...`) of the 02 / 06 spec operation-notes, and SHALL never record the actual secret / token / account number / RDS hostname / account-id / actual ARN / actual instance-id / EIP.
7. WHEN operation-notes.md is authored, THE operation-notes.md SHALL include in the body the work-split principle that actual AWS resource creation / change / deletion is performed directly by the operator and Kiro performs only document authoring / procedure organization / validation item organization.

### Requirement 12: Common Decision / Risk / Follow-up Document Update Candidates

**Objective**: As the operator, I want to explicitly receive in this requirements.md body the following common document candidates to update at this spec point, so that the follow-up phase tasks stage knows at a glance which rows to update.

- [`../_common/operator-decisions.md`](../_common/operator-decisions.md)
- [`../_common/risk-register.md`](../_common/risk-register.md)
- [`../_common/followups-overview.md`](../_common/followups-overview.md)

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL state the update candidates at this spec point for the following decisions in [`../_common/operator-decisions.md`](../_common/operator-decisions.md).
   - Update the OD-SEC-005 (no-Access-Key principle) status at this spec point from tentative → confirmed candidate
   - Update the OD-SEC-006 (IAM Role-based read principle) status at this spec point from tentative → confirmed candidate
   - State the `AmazonSSMManagedInstanceCore` usage policy as a new OD-SEC-* candidate or an OD-NET-009 auxiliary decision
2. WHEN design.md is authored, THE design.md SHALL state the following items as new risk candidates in [`../_common/risk-register.md`](../_common/risk-register.md).
   - Recurrence of pg_restore client / RDS engine major version mismatch (reinforces R-DATA-003)
   - EC2 disk shortage (EBS usage / log rotation)
   - Query failure due to exceeding the KIS API rate limit
   - Flask debug mode exposure (on carelessness in the temporary validation stage)
   - Unintended termination of Connector / Flask due to no systemd configuration (restart missed after EC2 reboot)
3. WHEN design.md deals with [`../_common/followups-overview.md`](../_common/followups-overview.md) update candidates, THE design.md SHALL state the following as update candidates.
   - The first-application environment of the 03 spec (`aws-paper`)
   - The first scope (MarketConnector EC2 formal operating transition + 8 validations)
   - Out of scope (EC2 new creation / new order / live rotation / OIDC / full IAM matrix)
   - The hand-off items to the 04 / 05 / 08 / 09 / 10 specs
4. WHEN design.md deals with the new risk candidates, THE design.md SHALL state one line each of mitigation, detection, rollback for each risk, in the same format as the [`../_common/risk-register.md`](../_common/risk-register.md) columns.
5. WHEN tasks.md is authored in a follow-up phase, THE tasks.md SHALL separate the update work for [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/risk-register.md`](../_common/risk-register.md), [`../_common/followups-overview.md`](../_common/followups-overview.md) into distinct tasks.
6. WHEN this spec's deliverable assigns a new decision ID / new risk ID, THE deliverable SHALL follow as-is the ID assignment rule of the 02 / 06 specs (an ID once assigned is not reused / re-numbered; assign the next available number).

### Requirement 13: Follow-up Spec Reusability (EC2 → ECS Operating Pattern Mapping)

**Objective**: As the operator, I want to receive a one-line skeleton of how this spec (03)'s EC2 operating pattern (systemd / startup script / log location / how the Instance Role permissions are attached) maps to the ECS operation of the follow-up 04 / 05 / 08 / 09 specs, so that the same pattern is not re-created for each spec.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL state a one-line skeleton that this spec's EC2 operating pattern maps to the ECS Fargate Task operating pattern of the 04 / 05 / 08 / 09 specs.
   - EC2 side: Instance Role-based secret / parameter read, temporary export → transition to normal operating mode, CloudWatch Logs write, SSM Session Manager access
   - ECS side mapping: Task Role-based secret / parameter read, ECS `secrets` field-based environment variable injection, awslogs driver-based CloudWatch Logs, SSM ECS Exec
2. WHEN design.md deals with the responsibility split, THE design.md SHALL state that this spec (03) holds the first-lock responsibility for the EC2 operating pattern itself and that filling in the ECS Task Role service prefixes (`/portfolio/paper/strategy/*` / `/portfolio/paper/view/*` / `/portfolio/paper/crawler/*`, etc.) is the responsibility of the 04 / 05 / 08 / 09 specs.
3. WHEN design.md deals with decisions that follow-up specs must not change, THE design.md SHALL state one line each for the following items: no-Resource-wildcard policy, no-Action-wildcard policy, env prefix `paper` / `live` separation, no assigning another service prefix, no-Access-Key principle (IMDSv2 + Role only).
4. WHERE this spec's systemd unit or startup script form is not carried over as-is to a follow-up spec's ECS Task operating form, THE design.md SHALL state a one-line mapping that the EC2 systemd maps to the ECS Task Definition + ECS Service / Step Functions.

### Requirement 14: Safety Constraints of This Spec

**Objective**: As the operator, I want to explicitly restrict this spec's work from changing code / operational data / AWS resources / external calls / new orders.

#### Acceptance Criteria

1. WHILE all phases of this spec proceed, THE work SHALL not modify the README / AGENTS.md / CHANGELOG / docs / worklog and source code / packaging files (`requirements.txt`, `setup.py`, `pyproject.toml`) of the 8 MS below.
   - `port-view`
   - `port-marketconnector`
   - `port-interest-crawler`
   - `port-interest-preprocessor`
   - `port_strategy_common`
   - `port_strategy_decision`
   - `port_strategy_execution`
   - `port_strategy_research`
2. WHILE all phases of this spec proceed, THE work SHALL not create or change / delete the actual AWS resources below. All actual creation / change / deletion is performed directly by the operator.
   - The EC2 instance itself / EBS volume / EIP attach change
   - Security Group rules
   - IAM Role / Policy / Instance Profile
   - Secrets Manager secret / SSM Parameter
   - RDS instance / parameter group
   - CloudWatch Logs Group, etc.
3. WHILE all phases of this spec proceed, THE work SHALL not execute external calls (broker / KIS API / Selenium / KRX / Naver / yfinance), new order / buy / sell / cancel / modify calls, Daily Batch / intraday monitor calls, or RDS DDL/DML calls, and SHALL perform only read-only smoke tests as operator direct work.
4. WHEN this spec's deliverable deals with a secret / identifier, THE deliverable SHALL use only `[REDACTED]` in place of the following items and SHALL not record the actual values.
   - Actual secret value / password / token / webhook URL
   - KIS app key / KIS app secret / account number
   - access key id / secret access key
   - RDS endpoint hostname / account-id
   - Actual secret ARN / actual KMS Key ARN
   - instance-id / EIP / EBS volume id
5. WHEN dealing with secret retrieval, THE deliverable SHALL state that the `secretsmanager:GetSecretValue` call is performed only by the operator and Kiro automated validation uses only `secretsmanager:DescribeSecret` metadata (consistent with 06 spec §11).
6. WHEN this spec deals with the separation policy from follow-up specs, THE deliverable SHALL state that EC2 new creation / live rotation automation / CI/CD OIDC / full IAM matrix / aws-live environment IAM are out of scope for this spec.
7. WHILE this phase (requirements) proceeds, THE work SHALL not create any deliverable other than `requirements.md` (`design.md`, `tasks.md`, `runbook.md`, `validation-checklist.md`, `operation-notes.md`) inside this spec folder. These are the responsibility of follow-up phases.
