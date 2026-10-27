# Design Document — 03-marketconnector-ec2

## Introduction

The core of this spec is summarized in one line.

> **The already-created MarketConnector EC2 takes the first-application results of the 02 / 06 specs as input, and this spec fixes the criteria for authoring the procedure / validation / operation notes so that it is formally transitioned into an operating form based on the KIS Connector (Flask) / `marketconnector_app`-based RDS connection / Secrets Manager and SSM Parameter Store env injection / Instance Role-based no-Access-Key operation; it also accumulates the 8 validation results from 2026-06-10 into the deliverables.**

Related specs: [`../02-aws-network-and-rds`](../02-aws-network-and-rds) / [`../06-secrets-and-iam`](../06-secrets-and-iam)

- First-application environment: `aws-paper` / region `ap-northeast-2` / first-application target: 1 MarketConnector EC2.
- Inputs of this spec:
  - [`./requirements.md`](./requirements.md) R1 ~ R14
  - [`../02-aws-network-and-rds/`](../02-aws-network-and-rds/) first-application results (VPC / Subnet / SG / 5 VPC Endpoints / RDS PostgreSQL / 7 DB roles)
  - [`../06-secrets-and-iam/`](../06-secrets-and-iam/) first-application results (4 Secrets Manager entries / 6 SSM Parameter Store entries / Instance Role + Profile + read-only Policy / no-Access-Key principle)
- Deliverable of this spec: in this phase, only [`./design.md`](./design.md).
  - [`./tasks.md`](./tasks.md) / [`./runbook.md`](./runbook.md) / [`./validation-checklist.md`](./validation-checklist.md) / [`./operation-notes.md`](./operation-notes.md) are follow-up phases. Basis: R14.7.
- Out of scope for this spec (must be stated explicitly). Basis: R14.
  - EC2 new creation
  - New order / buy / sell / cancel / modify calls
  - live rotation automation
  - GitHub Actions OIDC / CI/CD Role (07 spec)
  - Full IAM matrix across all 8 MS (split across 04 / 05 / 08 / 09)
  - aws-live IAM (10 spec)
  - Modifying the README / AGENTS.md / CHANGELOG / docs / worklog and source code / packaging files of the 8 MS

This document never records the actual values below. Everywhere it uses only `[REDACTED]` or placeholders (`<account-id>`, `<region>`, `<instance-id>`, `<eip>`, `<rds-endpoint>`, `<venv-path>`, `<venv-name>`, `<ebs-size>`, `<instance-type>`). Basis: R14.4.

- Actual secret value / RDS password / token / Slack webhook URL
- KIS app key / KIS app secret / account number
- RDS endpoint hostname / account-id
- Actual secret ARN / IAM access key id
- instance-id / EIP / EBS volume id

## 1. Scope / Out of Scope (R1)

### 1.1 In Scope (basis: R1.2)

| Item | Description |
|------|------|
| EC2 OS | Amazon Linux 2023 (already complete) |
| Python runtime | Python 3.9.25 + 1 venv |
| First-application dependency libraries | `requests`, `flask`, `psycopg2-binary`, `psycopg`, `pandas` (5 items) |
| PostgreSQL client | client major 18 / full 18.4 |
| pg_restore | major 18 / full 18.4 |
| KIS Connector (Flask) | Validate the feasibility of entering a normal operating form based on `connector_app.py` |
| RDS connection | Private RDS connection based on the `marketconnector_app` user |
| Secrets / SSM env injection | Map environment variables using the 06 §1 / §2 / §3 results as input (handed over by 06) |
| No Access Key | Use only IMDSv2 + Instance Role credentials (handed over by 06) |
| 8 validation results from 6/10 | Accumulated record in the deliverables |
| Operator procedure / validation / note authoring criteria | runbook / validation-checklist / operation-notes are follow-up phases |

### 1.2 Out of Scope (basis: R1.3)

| Item | Split spec / reason |
|------|------------------|
| EC2 new creation | Already created as the RDS restore runner at the 02 spec point (already complete) |
| New order / buy / sell / cancel / modify calls | 0 in every phase of this spec. Only read-only smoke tests |
| live rotation automation | Same as out of scope for the 06 spec |
| GitHub Actions OIDC / CI/CD Role | 07 spec |
| Full IAM matrix across all 8 MS | Split across 04 / 05 / 08 / 09 / 10 specs |
| aws-live IAM matrix | Consolidated in the 10 spec |
| Modifying 8 MS code / docs | R14.1 safety constraint |

### 1.3 Hand-off Items to Follow-up Specs (basis: R1.4)

| Follow-up spec | Hand-off item | Form |
|-----------|-----------|------|
| 04 strategy-batch-stepfunctions | EC2 operating pattern → ECS Task Role pattern mapping (§13) | Task Role skeleton + `/portfolio/paper/strategy/*` service prefix |
| 05 port-view-ecs-and-runbook | Same as above | Task Role skeleton + `/portfolio/paper/view/*` service prefix |
| 08 interest-crawler-and-preprocessor-ecs | Same as above | Task Role skeleton + `/portfolio/paper/crawler/*`, `/portfolio/paper/preprocessor/*` |
| 09 strategy-research-batch | Same as above | Task Role skeleton + `/portfolio/paper/research/*` |
| 10 cutover-and-validation-runbook | Consolidation of the aws-live `/portfolio/live/...` prefix | env prefix separation + Role permission matrix lock |

### 1.4 Phase Separation Policy (basis: R1.5)

At this phase (design), only [`./design.md`](./design.md) is authored. tasks / runbook / validation-checklist / operation-notes are the responsibility of follow-up phases and are not created in this phase.

## 2. EC2 Already Complete (R2)

### 2.1 Already-Complete Items (basis: R2.1 / R2.3)

| Item | Assumed value | Notation in this design |
|------|--------|---------------|
| EC2 OS | Amazon Linux 2023 | Stated |
| Subnet placement | public subnet | 02 spec decision (`public-a`) as input |
| EIP | attach + running | placeholder `<eip>` |
| Default SG | `sg-marketconnector-ec2` ↔ `sg-rds-postgres` 5432 inbound (02 spec matrix) | Stated |
| IAM Role | `portfolio-paper-marketconnector-ec2-role` (06 spec §4.1) | Name stated |
| IAM Instance Profile | `portfolio-paper-marketconnector-ec2-profile` (06 spec §4.1) attach complete | Name stated |

### 2.2 Identifier Placeholder Policy (basis: R2.2)

This design does not record the EC2 instance type, EBS volume size, instance-id, EIP value, or actual account-id. Every place uses only a placeholder (`<instance-type>`, `<ebs-size>`, `<instance-id>`, `<eip>`, `<account-id>`) or `[REDACTED]`.

### 2.3 Unchanged Items (basis: R2.4)

| Item | Policy |
|------|------|
| EC2 new creation | **Prohibited** |
| Instance type change | **Prohibited** |
| EBS re-creation / volume replacement | **Prohibited** |
| public subnet change | **Prohibited** |
| EIP detach or replacement | **Prohibited** |

### 2.4 Handling Flow on Mismatch (basis: R2.5)

If, at the operator inspection point, one of the already-complete items is found to differ from the assumptions of this design, arbitrary change of this spec is prohibited. The handling flow is as follows.

Suspend the work → record only a change proposal under the OD-MC or OD-NET category of [`../_common/operator-decisions.md`](../_common/operator-decisions.md) → operator approval / rejection → update this design and then resume the work.

## 3. Operator Hand-over Facts (R3)

### 3.1 Hand-over Facts by Date (basis: R3.1 / R3.3)

| Date | Usage form | Input | Result |
|------|-----------|------|------|
| 2026-06-09 | RDS restore runner | local PostgreSQL `pg_dump` → S3 temporary bucket → EC2 → private RDS `pg_restore` 18.4 | 10 schemas created / 7 roles created / core table row count match / `pg_restore` exit code 0 |
| 2026-06-10 | MarketConnector operating transition | 06 spec first-application results (4 Secrets + 6 SSM + Instance Role + read-only Policy) | All 8 validations succeeded (§10) |

### 3.2 RDS private endpoint Policy (basis: R3.2)

This RDS is operated with `Publicly accessible = No` and is reachable only from EC2 within the same VPC (input for R-NET-004 mitigation). A connection is established only when this EC2 is placed in a public subnet of the same VPC and the SG matrix (`sg-marketconnector-ec2` → `sg-rds-postgres` 5432) is applied.

### 3.3 Notation Policy (basis: R3.4 / R3.5)

The body of the hand-over-facts-by-date in this design does not record the actual dump file path / S3 bucket name / RDS endpoint hostname / actual EIP / actual instance-id / actual account-id / password / token in plaintext. Everywhere it uses only a placeholder or `[REDACTED]`. operation-notes.md (follow-up phase) follows the same policy when it records the accumulated per-date entries.

## 4. Python / venv / Dependencies (R4)

### 4.1 Python Runtime (basis: R4.1 / R4.2)

| Item | Decision of this spec |
|------|--------------|
| Python version | `3.9.25` |
| Number of venvs | 1 |
| venv path | placeholder `<venv-path>` |
| venv name | placeholder `<venv-name>` |

### 4.2 First-Application Dependency Libraries (basis: R4.3)

| Package | Purpose | Result at this spec point |
|--------|------|------------------|
| `requests` | KIS HTTP client | Install success |
| `flask` | Connector API | Install success |
| `psycopg2-binary` | RDS connection (primary use) | Install success |
| `psycopg` | RDS connection (secondary) | Install success |
| `pandas` | Quote / balance processing | Install success |

### 4.3 Additional Dependencies / No MS Code Modification (basis: R4.4 / R4.5)

Any library beyond the 5 above (e.g., an auxiliary package to be added by operator decision) is the responsibility of a follow-up phase of this spec or a separate spec and is not first-locked in this design. The work of this spec does not modify the source code / `requirements.txt` / `setup.py` / `pyproject.toml` of the 8 MS (consistent with R14.1).

### 4.4 Install Result Recording Policy (basis: R4.6)

Only the two values success / failure are recorded in the deliverables for the install result. The actual PyPI mirror used, the pip cache path, and the operator user home absolute path are recorded nowhere in this design or in follow-up phase deliverables.

## 5. PostgreSQL client / pg_restore 18.4 (R5)

### 5.1 Recommended Values (basis: R5.1 / R5.2)

| Tool | Recommended value | Compatibility note |
|------|--------|-----------|
| `pg_dump` | Maintain at least client major 18 / full 18.4 | Consistent with 02 spec appendix A |
| `pg_restore` | Maintain at least client major 18 / full 18.4 | Consistent with 02 spec appendix A |
| `psql` | Maintain at least client major 18 / full 18.4 | Consistent with 02 spec appendix A |

The aws-paper RDS engine is operating on PostgreSQL 18.4. At this spec point, client major 18 (full 18.4) is compatible with RDS engine major 18 (full 18.4) in the same-major consistency state (citing the R-DATA-003 mitigation). The compatibility matrix for a future change of the RDS engine major is out of scope for this spec and is handled as a follow-up spec decision after re-confirming 02 spec appendix A and the R-DATA-003 mitigation.

### 5.2 No-Downgrade Policy (basis: R5.3 / R5.4)

At this spec point, do not downgrade the client below 18.4.

If a change of the dump source major version or the RDS engine major version occurs, arbitrary decision within this spec is prohibited. Handle it as a decision of a follow-up spec (02 or 10) after consulting the references below.

- [`../02-aws-network-and-rds/runbook.md`](../02-aws-network-and-rds/runbook.md) appendix A
- [`../_common/risk-register.md`](../_common/risk-register.md) R-DATA-003 mitigation

### 5.3 Follow-up Phase Responsibility (basis: R5.5)

[`./runbook.md`](./runbook.md) (follow-up phase) includes a step that confirms client major version 18 from the output of `pg_dump --version`, `pg_restore --version`, `psql --version`.

## 6. RDS `marketconnector_app` Connection (R6)

### 6.1 SG / Access Policy (basis: R6.1)

| Item | Policy |
|------|------|
| RDS endpoint | private endpoint only |
| RDS SG inbound | Allow only `sg-marketconnector-ec2` → `sg-rds-postgres` 5432 |
| Public access | `No` |
| External CIDR inbound | Strictly prohibited (consistent with R-SEC-001 mitigation) |

### 6.2 DB role / Usage Policy (basis: R6.2)

| Role | When used | Policy |
|------|-----------|------|
| `marketconnector_app` | application runtime | Used for the day-to-day application connection of this spec |
| `portfolio_admin` | Only at DDL / administration time | No day-to-day application connection. Consistent with OD-DB-007 / OD-DB-008 |

### 6.3 secret Mapping (basis: R6.3 / R6.4)

| Secret path (06 spec §2.4) | JSON key | Environment variable key (8 MS compatible) |
|----------------------------|----------|--------------------------|
| `/portfolio/paper/rds/marketconnector-app` | `host` | `INTEREST_DB_HOST` |
| Same as above | `port` | `INTEREST_DB_PORT` |
| Same as above | `dbname` | `INTEREST_DB_NAME` |
| Same as above | `username` | `INTEREST_DB_USER` |
| Same as above | `password` | `INTEREST_DB_PASSWORD` |

Nowhere in this design or the follow-up phase deliverables is the actual RDS endpoint hostname recorded in plaintext. Only the placeholder `<rds-endpoint>` or `[REDACTED]` is used.

### 6.4 External Connection Handling (basis: R6.5)

If an RDS connection is needed from outside the VPC (the operator's local PC), enter only via this EC2 + SSM Session Manager or EC2 Instance Connect. Do not allow an external IP directly on the RDS SG (consistent with R-SEC-001 / R-NET-004 mitigation).

### 6.5 DDL/DML Policy (basis: R6.6)

In every phase of this spec, 0 INSERT / UPDATE / DELETE / CREATE / ALTER / DROP calls beyond the read-only SELECT that the application performs. At this spec point, all RDS-side changes other than operator direct work are prohibited (consistent with R14).

## 7. KIS Connector / Flask Operating Form (R7)

### 7.1 Operating Mode Comparison (basis: R7.2 / R7.3)

| Mode | Form | Responsibility at this spec point |
|------|------|------------------|
| Temporary validation stage | shell session or a temporary export script + operator manual entrypoint execution + Flask debug flag (temporarily ON if needed) | **Responsibility of this spec** |
| Normal operating mode | systemd unit or startup script auto-start + Flask debug OFF + log location decision + restart policy | **Responsibility of a follow-up task of this spec or a separate phase** |

### 7.2 Normal Operating Mode Transition Items (basis: R7.3)

| Item | Responsibility timing |
|------|-----------|
| Author systemd unit | Follow-up phase |
| Author startup script | Follow-up phase |
| Force Flask debug mode OFF | Follow-up phase |
| Process restart policy | Follow-up phase |
| Log file location decision | Follow-up phase |

### 7.3 entrypoint Classification (basis: R7.1 / R7.4)

| Classification | File | Policy at this spec point |
|------|------|------------------|
| Read-only — validated 2026-06-10 | `connector_balance.py`, `connector_order_check.py`, the Flask internal smoke test of `connector_app.py` (read-only endpoints) | Recorded as `[O]` in §10 results (d) / (e) / (f) |
| Read-only — operable candidate (formal validation incomplete at this spec point) | `connector_quote_realtime.py`, `connector_quote_closed.py`, `connector_view_service.py` | Must be startable in the normal operating form; validation is needed at the operating-entry point (a follow-up phase or a separate phase) |
| New order | `connector_buy.py`, `connector_sell.py`, `connector_cancel.py`, `connector_modify.py` | 0 calls in any phase of this spec |

### 7.4 Follow-up Phase Responsibility (basis: R7.5 / R7.6)

[`./runbook.md`](./runbook.md) (follow-up phase) includes the Flask startup command of the temporary validation stage (e.g., `python connector_app.py`) and the read-only endpoint call patterns on a placeholder basis. It never includes new-order endpoint call examples. The transition to systemd unit-based normal operating mode is applied only at the entry point of a follow-up task of this spec or a separate phase.

The operable-candidate entrypoints (`connector_quote_realtime.py`, `connector_quote_closed.py`, `connector_view_service.py`) are validated with read-only smoke tests at the entry point of a follow-up task of this spec or a separate phase, and then added as separate rows to the §10 result table.

## 8. Secrets Manager / SSM Parameter Store env Injection (R8)

### 8.1 06 spec Citation Policy (basis: R8.1)

This spec takes the 06 spec §1 (classification criteria) / §2 (naming matrix) / §3 (environment variable compatibility) tables as input without modification. This design does not first-lock the mapping table again; it cites the 06 spec decision values as-is.

### 8.2 Environment Variable Mapping (basis: R8.2)

| Store | Name (example) | Environment variable key (8 MS compatible) | Note |
|--------|-----------|--------------------------|------|
| Secrets Manager | `/portfolio/paper/marketconnector/kis-app-key` | `APP_KEY` | Single value |
| Secrets Manager | `/portfolio/paper/marketconnector/kis-app-secret` | `APP_SECRET` | Single value |
| Secrets Manager | `/portfolio/paper/marketconnector/paper-account` | `PAPER_ACNT`, `ACNT_PRDT_CD` | JSON multi-key |
| Secrets Manager | `/portfolio/paper/rds/marketconnector-app` | `INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD` | JSON multi-key |
| SSM Parameter | `/portfolio/paper/marketconnector/kis-base-url` | `BASE_URL` | General config value |
| SSM Parameter | `/portfolio/paper/marketconnector/environment` | `PORT_ENVIRONMENT` | Environment identifier |
| SSM Parameter | `/portfolio/paper/marketconnector/broker-name` | `PORT_BROKER_NAME` | broker name |
| SSM Parameter | `/portfolio/paper/marketconnector/connector-host` | `CONNECTOR_HOST` | Flask host. Code default `127.0.0.1` (per port-marketconnector `connector_app.py`) |
| SSM Parameter | `/portfolio/paper/marketconnector/connector-port` | `CONNECTOR_PORT` | Flask port. Code default `5000` |
| SSM Parameter | `/portfolio/paper/marketconnector/connector-debug` | `CONNECTOR_DEBUG` | Flask debug flag. Code default `False`. Must be forced to `false` in normal operating mode |

#### 8.2.1 KIS_* alias Simultaneous Export Policy (2026-06-17 addition)

The environment variable keys of this design (`APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD` / `BASE_URL`) are kept as-is for 8 MS compatibility.

However, it was first demonstrated by operator validation on 2026-06-17 that, at actual `port-marketconnector` code execution time, a `KIS_*` prefix alias is also needed in addition to the compatibility keys of this design.

For details, see the 2026-06-17 section of [`./operation-notes.md`](./operation-notes.md).

| Compatibility key (8 MS consistent) | Alias needed for actual code execution | Source |
|-----------------------|------------------------------|------|
| `APP_KEY` | `KIS_APP_KEY` | The value of the internal key `APP_KEY` inside the Secrets Manager `/portfolio/paper/marketconnector/kis-app-key` JSON |
| `APP_SECRET` | `KIS_APP_SECRET` | The value of the internal key `APP_SECRET` inside the Secrets Manager `/portfolio/paper/marketconnector/kis-app-secret` JSON |
| `PAPER_ACNT` | `KIS_PAPER_ACNT` | The value of the internal key `PAPER_ACNT` inside the Secrets Manager `/portfolio/paper/marketconnector/paper-account` JSON |
| `ACNT_PRDT_CD` | `KIS_ACNT_PRDT_CD` | The value of the internal key `ACNT_PRDT_CD` inside the Secrets Manager `/portfolio/paper/marketconnector/paper-account` JSON |
| `BASE_URL` | `KIS_BASE_URL` | The value of the SSM Parameter `/portfolio/paper/marketconnector/kis-base-url` |

In the temporary validation stage, the compatibility key and the alias are **exported simultaneously** (e.g., the same internal secret value is exported simultaneously into the two environment variables `APP_KEY` and `KIS_APP_KEY`).

The simultaneous-export policy is maintained until the transition to normal operating mode (systemd / startup script). The subsequent alignment (whether to unify on one of the compatibility key / alias, or to keep both exported simultaneously as the operating standard) is separated as the responsibility of a follow-up task of this spec or a separate phase (handed over as-is per §8.5).

#### 8.2.2 JSON SecretString Internal Key Extraction Policy (2026-06-17 addition)

The secrets below are not a plain-string SecretString but a **JSON SecretString**.

- `/portfolio/paper/marketconnector/kis-app-key` — internal key `APP_KEY`
- `/portfolio/paper/marketconnector/kis-app-secret` — internal key `APP_SECRET`
- `/portfolio/paper/marketconnector/paper-account` — internal keys `PAPER_ACNT` / `ACNT_PRDT_CD`

Therefore the temporary export script (§8.3) follows this procedure.

1. Parse the `SecretString` of the Secrets Manager `GetSecretValue` result as JSON.
2. Export only the value of the internal key as an environment variable. Do not export the entire JSON dict as an environment variable value.
3. Export the same value simultaneously to both the compatibility key and the `KIS_*` alias.

`/portfolio/paper/rds/marketconnector-app` is also a JSON multi-key SecretString, and the policy of exporting the mapping below as-is is maintained (consistent with 02 / 06 spec / 0 changes).

- `host` → `INTEREST_DB_HOST`
- `port` → `INTEREST_DB_PORT`
- `dbname` → `INTEREST_DB_NAME`
- `username` → `INTEREST_DB_USER`
- `password` → `INTEREST_DB_PASSWORD`

#### 8.2.3 First-Failure → Correction Case (2026-06-17 addition)

On 2026-06-17, the case in the first `CONNECTOR_BALANCE` run where the KIS balance API call was reached yet `response_status=500` / `response_code=1` / `is_success=false` was returned was first diagnosed not as a discard of the KIS credential itself but as a mapping error that exported the entire JSON SecretString as-is into the env value.

In the v5 pattern that extracted only the value of the internal `APP_KEY` / `APP_SECRET` and exported it simultaneously to both the compatibility key + `KIS_*` alias, the KIS balance API responded normally with `response_status=200` / `response_code=0` / `is_success=true`, and a new `connector_balance_snapshot` row was stored.

This case is accumulated with 0 plaintext records of the secret value / KIS app key / KIS app secret. The secret value is never recorded; only the following levels may be recorded in the deliverables.

- secret name path
- shape (JSON SecretString)
- internal key name
- value length

For details, see the 2026-06-17 section of [`./operation-notes.md`](./operation-notes.md).

### 8.3 Temporary → Normal Transition (basis: R8.3 / R8.4)

| Stage | Form | Security policy | Responsibility timing |
|------|------|-----------|-----------|
| Temporary validation stage | `/tmp/inject-env.sh` source + memory export | No plaintext secret stored in the script body / permission 700 recommended / for a JSON SecretString extract only the internal key value (§8.2.2) / simultaneously export the compatibility key + `KIS_*` alias (§8.2.1) | This spec |
| Normal operating mode | systemd `EnvironmentFile=` or startup script memory export | No plaintext secret stored in the `EnvironmentFile=` body. Permission 600 recommended. After reflecting the v5 mapping pattern (§8.2.1 / §8.2.2), decide by operator decision whether to unify on the compatibility key / alias or keep both | A follow-up task of this spec or a separate phase |

### 8.4 Compatibility Policy (basis: R8.5)

| Item | Policy |
|------|------|
| 8 MS code / `port-marketconnector` `config.py` | Not modified (consistent with OD-DB-003) |
| Environment variable key names | Not changed. Only the secret values are externalized |

### 8.5 Follow-up Phase Responsibility (basis: R8.6)

[`./runbook.md`](./runbook.md) (follow-up phase) separates the temporary export script authoring, secret / parameter read, env injection, and the KIS / RDS read-only smoke test pass step after Connector restart into the [실행] / [확인] / [준비] / [복구] labels.

### 8.6 Sensitive Information Notation Policy (basis: R8.7)

Nowhere in this spec's deliverables is the actual secret value, KIS app key / app secret, account number, RDS password, or RDS endpoint hostname recorded in plaintext. Everywhere uses only `[REDACTED]` or a placeholder.

## 9. Instance Role-Based No Access Key (R9)

### 9.1 06 spec Input (basis: R9.1)

This spec takes the decisions of the 06 spec §4 (Instance Role design) / §5 (no-Access-Key principle) as input as-is. This design does not re-lock the 06 spec decisions; it states only the 03-responsibility auxiliary permissions and the location for checking the credential-normal state.

### 9.2 03-Responsibility Auxiliary Permission Matrix (basis: R9.3 / R9.4)

| Statement | Effect | Action | Resource | Note |
|-----------|--------|--------|----------|------|
| `SsmManagedInstanceCore` | (managed) | AWS managed `AmazonSSMManagedInstanceCore` attach | (managed) | For SSM Session Manager access |
| `CloudWatchLogsWrite (recommended: pre-create log group)` | Allow | `logs:CreateLogStream`, `logs:PutLogEvents`, `logs:DescribeLogStreams` | `arn:aws:logs:<region>:<account-id>:log-group:/portfolio/paper/marketconnector*` | Default recommendation of this spec. The operator pre-creates the log group. See Details §9.1 Notes |
| `CloudWatchLogsWrite (option: include CreateLogGroup)` | Allow | `logs:CreateLogStream`, `logs:PutLogEvents`, `logs:DescribeLogStreams`, `logs:CreateLogGroup` | `arn:aws:logs:<region>:<account-id>:log-group:/portfolio/paper/marketconnector*` | Applied in a limited way only when the recommended option cannot be adopted. See Details §9.1 Notes |

Details §9.1 Notes:

- `CloudWatchLogsWrite (recommended: pre-create log group)`: the default recommended option of this spec. The operator pre-creates the log group. The application / Connector cannot create an arbitrary log group. Do not include other service log-group prefixes.
- `CloudWatchLogsWrite (option: include CreateLogGroup)`: applied in a limited way only when the recommended option cannot be adopted (e.g., a temporary point where operator direct pre-creation is difficult). After application, reclaim it to the pre-create option in a follow-up task of this spec.

Log group name candidates (recommended): `/portfolio/paper/marketconnector/app`, `/portfolio/paper/marketconnector/flask`, `/portfolio/paper/marketconnector/system`. Actual log group creation / filling the Resource ARN is operator direct work and is out of scope for this spec.

### 9.3 06 / 03 Permission Split (basis: R9.4)

| spec | Responsibility permission |
|------|-----------|
| 06 | Secrets Manager / SSM Parameter Store / (conditional) KMS Decrypt — read permission |
| 03 (this spec) | SSM Session Manager access / CloudWatch Logs write — EC2 operating auxiliary permission |

### 9.4 Credential Normal State (basis: R9.2)

| Check item | Normal state |
|-----------|-----------|
| `aws sts get-caller-identity` `Arn` | `arn:aws:sts::<account-id>:assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>` |
| `aws configure list` `access_key` Source | `iam-role` or `Ec2InstanceMetadata` |

### 9.5 Prohibition Policy (basis: R9.1)

| Pattern | Policy |
|------|------|
| `~/.aws/credentials` long-lived access key file | **Prohibited** |
| `~/.aws/config` access key entry | **Prohibited** |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` environment variable export | **Prohibited** |
| dotfile (`~/.bashrc`, `~/.profile`, `~/.bash_profile`) access key export | **Prohibited** |
| systemd unit `Environment=AWS_ACCESS_KEY_ID=...` | **Prohibited** |
| systemd unit `EnvironmentFile=` storing an access key | **Prohibited** |
| application config / `.env` storing an access key | **Prohibited** |

### 9.6 Emergency Scenario Handling (basis: R9.6)

If an access key is found inside the EC2 during this spec's operation, perform the following within 1 hour of discovery.

Discovery → immediately discard the access key in the IAM Console (`Make inactive` → `Delete`) → move the EC2's `~/.aws/credentials` to a backup (timestamp suffix) → revert to the mode that uses only IMDSv2 + Instance Role credentials → re-validate the credential-normal state of 06 / 03 (§9.4) → accumulate the facts into operation-notes.md (follow-up phase).

## 10. 8 Validation Results from 2026-06-10 (R10)

### 10.1 8 Results (basis: R10.1 / R10.2)

| No. | Item | Result | Label candidate |
|------|------|------|-----------|
| (a) | Python 3.9.25 / venv configuration | Success | `[O]` |
| (b) | `requests` / `flask` / `psycopg2-binary` / `psycopg` / `pandas` install | Success | `[O]` |
| (c) | RDS connection based on `marketconnector_app` | Success | `[O]` |
| (d) | `connector_balance.py` execution | Success | `[O]` |
| (e) | `connector_order_check.py` execution | Success | `[O]` |
| (f) | Flask internal smoke test (read-only endpoint) | Success | `[O]` |
| (g) | Secrets Manager / SSM Parameter Store-based env injection | Success | `[O]` |
| (h) | Execution without an Access Key based on the EC2 Instance Role | Success | `[O]` |

### 10.2 Recording Policy (basis: R10.3 / R10.4 / R10.6)

operation-notes.md (follow-up phase) records the 8 items as a per-date accumulation (`## 2026-06-10 ...`).

- Record only the result (success / failure) + the check date + the fact that the operator confirmed it directly.
- Never quote the stdout / stderr body of the validation commands, the secret value, the token value, or the actual endpoint hostname in the body.
- validation-checklist.md (follow-up phase) turns the items into checks with the same 4-label set as the 02 / 06 specs (`[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]`).

### 10.3 Handling on Failure (basis: R10.5)

If one of the 8 items turns to failure at a future check point, arbitrary decision within this spec is prohibited. Update the label to `[X]` or `[Kiro 후속 작업 필요]` → record the cause as an R-* candidate in [`../_common/risk-register.md`](../_common/risk-register.md) → proceed through the operator approval process.

## 11. Deliverable Format / Labels (R11)

### 11.1 [`./runbook.md`](./runbook.md) 8 Steps (basis: R11.2 / R11.3)

| Step | Label candidate | Responsibility |
|------|-----------|------|
| (a) Pre-check (EC2 / SG / Profile attach / Python / venv / client 18) | [확인] | Operator direct |
| (b) Confirm dependency library install result | [확인] | Operator direct |
| (c) Author temporary export script / inject environment variables after source | [실행] | Operator direct |
| (d) Confirm RDS connection based on `marketconnector_app` | [확인] | Operator direct |
| (e) Execute `connector_balance.py` / `connector_order_check.py` | [확인] | Operator direct |
| (f) Flask internal smoke test (read-only only) | [확인] | Operator direct |
| (g) Check Instance Role credentials / absence of Access Key | [확인] | Operator direct |
| (h) Accumulate the 8 results into operator notes | [준비] | Operator direct |

### 11.2 [`./validation-checklist.md`](./validation-checklist.md) 7 Check Areas (basis: R11.5)

| Area | Check item (example) | Label candidate |
|------|---------------|-----------|
| (a) Python / venv / dependency library inventory | Python 3.9.25 / 1 venv / 5 installs | `[O]` candidate |
| (b) PostgreSQL client / pg_restore version | client major 18 / full 18.4 | `[O]` candidate |
| (c) `marketconnector_app` RDS connection result | private endpoint connection pass | `[O]` candidate |
| (d) Connector / Flask read-only smoke test result | balance / order query / Flask `/api/v1/view/...` pass | `[O]` candidate |
| (e) Secrets Manager / SSM env injection result | environment variable mapping (§8.2) pass | `[O]` candidate |
| (f) Instance Role credentials / absence of Access Key result | assumed-role / Source `iam-role` / `~/.aws/credentials` absent | `[O]` candidate |
| (g) 0 new order / buy / sell / cancel / modify calls | 0 calls in every phase of this spec | `[O]` candidate |

### 11.3 4-Label Rule (basis: R11.4)

Use only `[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]`. Use of any other label is prohibited. Same rule as the 02 / 06 specs.

### 11.4 [`./operation-notes.md`](./operation-notes.md) Format (basis: R11.6 / R11.7)

| Item | Policy |
|------|------|
| Accumulated record format | `## YYYY-MM-DD <summary>` (same as 02 / 06 specs) |
| secret / identifier recording | 0. Only `[REDACTED]` or placeholder |
| IAM change record template | 4 lines: change date / changer / change reason / before-and-after item summary. Quoting the full JSON body is prohibited |
| Operator / Kiro work split | Stated at the top of the body: actual AWS resource creation / change / deletion is operator direct; Kiro only organizes documents / procedures / validation items |

## 12. _common Update Candidates (actual update is at the tasks stage, R12)

### 12.1 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) Update Candidates (basis: R12.1)

| ID | Item | Candidate value of this spec | Status |
|----|------|---------------|--------|
| OD-SEC-005 | EC2 / 8 MS no-Access-Key principle | Prohibit storing access key files / environment variables / dotfiles inside the EC2. Only IMDSv2 + Instance Role | TENTATIVE 🟡 → CONFIRMED 🟢 (upon operator approval) |
| OD-SEC-006 | EC2 / ECS IAM Role-based secret / parameter read principle | Least privilege. No Resource wildcard. No Action wildcard. service prefix separation | TENTATIVE 🟡 → CONFIRMED 🟢 (upon operator approval) |
| New OD-SEC-* or OD-NET-009 auxiliary | `AmazonSSMManagedInstanceCore` usage policy | No EC2 SSH 22 inbound + SSM Session Manager + managed policy attach only | TENTATIVE 🟡 |

### 12.2 [`../_common/risk-register.md`](../_common/risk-register.md) Update Candidates (basis: R12.2 / R12.4)

| ID (tentative) | Risk | Mitigation | Detection | Rollback |
|-----------|------|------------|-----------|----------|
| R-DATA-XXX | Recurrence of pg_restore client / RDS engine major version mismatch (reinforces R-DATA-003) | State the policy of maintaining client major 18 / full 18.4. No downgrade | Check `pg_restore --version` / `pg_dump --version` output | Re-run 02 runbook appendix A then reinstall the client |
| R-CAP-XXX | Failure to store venv / logs / token files due to EC2 disk (EBS) shortage | Operator periodic check / log rotation / Connector log location decision (follow-up phase) | `df -h` / EBS CloudWatch metric / Connector startup error | Apply log rotation / expand the EBS volume (operator decision) |
| R-BROKER-XXX | Read-only call failure due to exceeding the KIS API rate limit | Minimize call frequency at this spec point + token reuse policy + limit on Connector restart count | Check KIS API response codes / Connector log pattern | Suspend calls temporarily + resume by operator decision |
| R-SEC-XXX | Flask debug mode exposure (carelessness in the temporary validation stage) | Force `CONNECTOR_DEBUG=false` on transition to normal operating mode. No externally exposed SG during temporary validation | Check EC2 SG inbound / the debug flag in the Flask startup log | Restart after turning the debug flag OFF + audit whether it was externally exposed |
| R-AUTO-XXX | Unintended termination of Connector / Flask due to no systemd configuration (restart missed after EC2 reboot) | State that the responsibility for transition to normal operating mode (systemd unit / startup script) is a follow-up task of this spec or a separate phase | Check Connector restart after EC2 reboot / health check | Restart after applying the systemd unit |

### 12.3 [`../_common/followups-overview.md`](../_common/followups-overview.md) Update Candidates (basis: R12.3)

| Section | Update content |
|------|-----------|
| 03-marketconnector-ec2 first-application environment | `aws-paper`, `ap-northeast-2` |
| First scope | MarketConnector EC2 formal operating transition + accumulated record of 8 validations |
| First out of scope | EC2 new creation / new order / live rotation / OIDC / full IAM matrix |
| Hand-off to 04 / 05 / 08 / 09 / 10 specs | EC2 operating pattern → ECS Task Role pattern mapping (§13) |

### 12.4 ID Assignment Rule (basis: R12.6)

New decision IDs / new risk IDs follow the ID assignment rule of the 02 / 06 specs as-is (an ID once assigned is not reused / re-numbered; assign the next available number).

## 13. EC2 → ECS Operating Pattern Mapping (R13)

### 13.1 Mapping Table (basis: R13.1 / R13.4)

| Area | EC2 (03 this spec) | ECS Fargate (04 / 05 / 08 / 09) |
|------|------------------|--------------------------------|
| Credentials | Instance Role + IMDSv2 | Task Role |
| secret / parameter read | 06 policy + environment variable injection | ECS Task Definition `secrets` field + Task Role |
| Logs | CloudWatch Logs Agent or `PutLogEvents` | awslogs driver |
| Access / debugging | SSM Session Manager | ECS Exec |
| Auto-start | systemd unit or startup script | Task Definition + ECS Service |
| Restart / recovery | systemd `Restart=` | ECS Service `desiredCount` + Step Functions |

### 13.2 Responsibility Split (basis: R13.2)

03 (this spec) = first lock of the EC2 operating pattern. 04 / 05 / 08 / 09 = fill in the service prefix (`/portfolio/paper/strategy/*`, `/portfolio/paper/view/*`, `/portfolio/paper/crawler/*`, `/portfolio/paper/preprocessor/*`, `/portfolio/paper/research/*`).

### 13.3 No-Change Decisions for Follow-up Specs (basis: R13.3)

Follow-up specs do not change the following.

No Resource wildcard (`Resource: "*"`) / no Action wildcard (`secretsmanager:*` / `ssm:*` / `Action: "*"`) / use only env prefix `paper` / `live` / no assigning a Resource for another service prefix / no-Access-Key principle (IMDSv2 + Role only).

## 14. Safety Constraints (R14)

### 14.1 Policy by Area

| Area | Policy |
|------|------|
| 8 MS code / docs / packaging | 8 MS not modified. See Details §14 Notes |
| Actual AWS resources | No work on EC2 / EBS / EIP / SG / IAM Role / Policy / Instance Profile / Secrets Manager secret / SSM Parameter / RDS / parameter group / CloudWatch Logs Group / KMS Key. All actual creation / change / deletion is operator direct |
| External calls | 0 calls to broker / KIS API / Selenium / KRX / Naver / yfinance |
| New orders | 0 calls to buy / sell / cancel / modify / Daily Batch / intraday monitor |
| RDS DDL/DML | 0. Only read-only SELECT is allowed |
| Read-only smoke test | Performed only as operator direct work |
| secret / identifier notation | Use only `[REDACTED]` or placeholder. See Details §14.3 |
| GetSecretValue calls | Performed only by the operator. Kiro automated validation uses only `secretsmanager:DescribeSecret` metadata |
| On violation detection | Immediately halt the work → report to the operator → proceed with the rollback procedure |

### 14.2 Phase Separation (basis: R14.7)

At this phase (design), do not create any deliverable other than [`./design.md`](./design.md) (`tasks.md`, `runbook.md`, `validation-checklist.md`, `operation-notes.md`). Those are the responsibility of follow-up phases.

### 14 Notes — 8 MS No-Modification Targets in Detail

The 8 MS no-modification targets include the README / AGENTS.md / CHANGELOG / docs / worklog / source code / `requirements.txt` / `setup.py` / `pyproject.toml` of the repositories below.

- `port-view`
- `port-marketconnector`
- `port-interest-crawler`
- `port-interest-preprocessor`
- `port_strategy_common`
- `port_strategy_decision`
- `port_strategy_execution`
- `port_strategy_research`

### 14.3 secret / identifier Notation Details

The actual targets of the secret / identifier notation policy are as follows. Everywhere uses only `[REDACTED]` or a placeholder.

- Actual secret value / password / token / webhook URL
- KIS app key / KIS app secret / account number
- access key id / secret access key
- RDS endpoint hostname / account-id
- Actual secret ARN / actual KMS Key ARN
- instance-id / EIP / EBS volume id

## Testing Strategy (reference)

This spec is a deliverable of EC2 operating patterns / IAM auxiliary permissions / environment variable injection flow / procedure documents. It has no code / pure functions / algorithm whose behavior changes with input variation. Therefore property-based testing (PBT) does not apply. This design does not include a Correctness Properties section.

Validation of this spec is performed only in the following two forms.

- Static environment consistency check: check the consistency of Python / venv / dependency libraries / client major version / Instance Role assumed-role ARN / absence of Access Key / Secret · Parameter Describe metadata. Responsibility of [`./validation-checklist.md`](./validation-checklist.md) (follow-up phase).
- Check whether read-only smoke tests pass. Responsibility of [`./runbook.md`](./runbook.md) / [`./validation-checklist.md`](./validation-checklist.md) (follow-up phase).
  - RDS connection based on `marketconnector_app`
  - Execute `connector_balance.py` / `connector_order_check.py`
  - Call the Flask read-only endpoint (`/api/v1/view/...`)
  - The `secretsmanager:GetSecretValue` call is performed only by the operator
  - Kiro automated validation uses only `secretsmanager:DescribeSecret` metadata
