# Design Document — 06-secrets-and-iam

## Introduction

The core of this spec is summarized in one line. **First confirm the operational structure where the MarketConnector EC2 does not store an IAM Access Key inside the EC2 and reads Secrets Manager / SSM Parameter Store values with least privilege using only the EC2 Instance Role.**

- First application environment: `aws-paper`. region `ap-northeast-2`. First application target workload: MarketConnector EC2.
- Input of this spec
  - [`./requirements.md`](./requirements.md) R1 ~ R14, [`./README.md`](./README.md)
  - [`../02-aws-network-and-rds/`](../02-aws-network-and-rds/) first-application result
    (VPC / Subnet / SG / 5 VPC Endpoints / RDS PostgreSQL / 7 DB roles)
- Deliverables of this spec: in this phase, only [`./design.md`](./design.md). 4 follow-up-phase documents. Basis R14.7.
  - [`./tasks.md`](./tasks.md) / [`./runbook.md`](./runbook.md)
  - [`./validation-checklist.md`](./validation-checklist.md) / [`./operation-notes.md`](./operation-notes.md)
- Out of this spec's scope (must be specified). Basis R14.
  - live rotation automation (Lambda rotation, scheduled rotation)
  - GitHub Actions OIDC / CI/CD Role (07 spec)
  - Full IAM matrix for all 8 MS (03 / 08 / 04 / 05 / 09 spec division)
  - aws-live IAM (10 spec integration)
  - Actual AWS resource creation / modification / deletion

This document does not write the following items. All use only `[REDACTED]` or a placeholder (`<account-id>`, `<region>`, etc.).
Basis R14.4.

- actual secret value / KIS app key / KIS app secret / account number
- RDS endpoint hostname / account-id
- actual secret ARN / IAM access key id

## 1. Secret / Parameter Classification Criteria

### 1.0 Classification Principle (basis R1.5 / R1.6 / R1.7)

- "A value where exposure enables external transmission / external authentication / external fund movement" → **Secrets Manager**.
- "A value that differs by environment but by itself cannot directly cause external action" → **SSM Parameter Store**.
- The classification criterion is exposure impact, not cost. The candidates that can be moved to SSM SecureString for cost reduction and those that must not be moved are separated in §1.4.

### 1.1 Secrets Manager Storage Candidates (basis R1.1 / R1.2)

| Item | Environment-variable key | Classification reason | Note |
|------|------------|----------|------|
| KIS app key | `APP_KEY` (port-marketconnector `config.py`) | Exposure enables KIS API call / external authentication | Per-environment separation (`paper` / `live`). This spec (06) first-locks only `paper` |
| KIS app secret | `APP_SECRET` (同) | Exposure enables KIS API call / external authentication | same as above |
| KIS paper account number | `PAPER_ACNT` | Exposure discloses the broker-side account identifier | Recommended as a standalone secret or a JSON multi-key secret. See the §2 matrix |
| KIS account product code | `ACNT_PRDT_CD` | Needed as a pair with the account number for broker calls | Recommended in the same secret as `PAPER_ACNT` (JSON multi-key) |
| RDS `marketconnector_app` connection info | 5 `INTEREST_DB_*` (see mapping below) | Password exposure enables RDS connection | Recommended to bundle into a single JSON multi-key secret |
| RDS master password (`portfolio_admin`) | (no application environment variable, operator-only) | A secret already registered in the 02 spec. Compatibility maintained | Keep the existing name `/portfolio/paper/rds/master` as-is (§2.5) |

#### Row Notes — RDS `marketconnector_app` connection-info environment-variable mapping

- `INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD`

### 1.2 SSM Parameter Store Storage Candidates (basis R1.3)

| Item | Environment-variable key | Classification reason | Note |
|------|------------|----------|------|
| KIS base URL | `BASE_URL` (port-marketconnector `config.py`) | The endpoint differs by environment but the value itself is not a secret | A different parameter per `paper` / `live` |
| Connector Flask host | (operator-decided key) | General setting value | May differ by environment |
| Connector Flask port | (同) | General setting value | |
| Connector Flask debug flag | (同) | General setting value | Can be temporarily ON in `paper` validation mode |
| `PORT_ENVIRONMENT` | `PORT_ENVIRONMENT` | Environment identifier (`paper` / `live`). Not a secret | Used for live-call branching |
| `PORT_BROKER_NAME` | `PORT_BROKER_NAME` | broker name. Not a secret | |
| `PORT_STRATEGY_NAME` | `PORT_STRATEGY_NAME` | strategy name. Not a secret | |
| `PORT_STRATEGY_VERSION` | `PORT_STRATEGY_VERSION` | strategy version. Not a secret | |

### 1.3 Slack Webhook Storage Location Comparison (basis R1.4 / OD-OBS-004)

| Candidate | Advantage | Disadvantage | Recommendation |
|------|------|------|------|
| Secrets Manager | rotation / audit / per-environment KMS policy consistency | Per-secret monthly unit price (small) | **Recommended (1st)** |
| SSM SecureString | Free (only the KMS call unit price when using a KMS Key) | Weak rotation API standard, audit format differs from Secrets Manager | An acceptable cost-saving option when reducing cost (2nd) |

This spec's (06) first-lock decision value (tentative): **Secrets Manager storage (recommended)**.

- If cost reduction is needed, record a change proposal to move to SSM SecureString below and apply upon approval.
  - [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-OBS-004
- For this decision's [`../_common/operator-decisions.md`](../_common/operator-decisions.md) update candidate, see §7.

### 1.4 Movable Candidates vs Must-Not-Move Candidates (basis R1.7)

| Item | Can it be moved to SSM SecureString | Reason |
|------|------------------------------------|------|
| Slack webhook URL | Can be moved (cost-saving option) | The exposure impact is limited to sending an external channel message. KMS encryption is sufficient |
| KIS app key | Must not be moved | The core broker authentication value. Applying Secrets Manager's rotation / audit standard is recommended |
| KIS app secret | Must not be moved | same as above |
| RDS `marketconnector_app` password | Must not be moved | Direct RDS connection possible. Keep consistency with the master password policy of Secrets Manager (OD-SEC-002) |
| RDS master (`portfolio_admin`) password | Must not be moved | Already registered in Secrets Manager in the 02 spec. Compatibility maintained (§2.5) |

## 2. Naming / Path Rules

### 2.1 Basic Rule (basis R2.1 / R2.2 / R2.3 / R2.4)

- Basic format: `/portfolio/{env}/{service}/{item}`.
- `{env}` value: `paper` or `live`. Others (`dev`, `test`) are out of this spec's scope. OD-ENV-001 / OD-ENV-003 input.
- `{service}` value (candidates at the time of this spec): `marketconnector`, `rds`, `view`, `strategy`, `crawler`, `preprocessor`, `research`, `ops`. What this spec (06) treats as the first-confirmation target is limited to the two `marketconnector` and `rds`. Basis R2.4.
- `{item}` uses only lowercase alphabet + digits + `-`. Spaces / Korean / uppercase / `_` / actual values are prohibited.

### 2.2 Compatibility Policy (basis R2.5)

- The existing name (`/portfolio/paper/rds/master`) already registered in the 02 spec / [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) is the same form as this rule and is kept as-is. This rule is applied from new items onward.

### 2.3 Name-Itself Prohibition Rule (basis R2.6)

- Do not include the actual RDS endpoint hostname, account number, secret value, password or token in the secret / parameter name itself. Use only a placeholder or an item name (`kis-app-key`, `marketconnector-app`) for all identifiers / account numbers / hostnames.

### 2.4 secret / parameter Name Matrix (basis R2.1 / R2.2 / R3.2)

| Store | Name (example) | Mapped environment variable (placeholder) | Note |
|--------|-----------|----------------------------|------|
| Secrets Manager | `/portfolio/paper/marketconnector/kis-app-key` | `APP_KEY` | Single-value secret |
| Secrets Manager | `/portfolio/paper/marketconnector/kis-app-secret` | `APP_SECRET` | Single-value secret |
| Secrets Manager | `/portfolio/paper/marketconnector/paper-account` | `PAPER_ACNT`, `ACNT_PRDT_CD` | JSON multi-key recommended |
| Secrets Manager | `/portfolio/paper/rds/marketconnector-app` | `INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD` | JSON multi-key |
| Secrets Manager | `/portfolio/paper/rds/master` | (operator-only) | 02 spec-registered secret. Kept as-is |
| SSM Parameter | `/portfolio/paper/marketconnector/kis-base-url` | `BASE_URL` | General setting value |
| SSM Parameter | `/portfolio/paper/marketconnector/connector-host` | (operator-decided key) | Flask host |
| SSM Parameter | `/portfolio/paper/marketconnector/connector-port` | (同) | Flask port |
| SSM Parameter | `/portfolio/paper/marketconnector/connector-debug` | (同) | Flask debug flag |
| SSM Parameter | `/portfolio/paper/marketconnector/environment` | `PORT_ENVIRONMENT` | Environment identifier |
| SSM Parameter | `/portfolio/paper/marketconnector/broker-name` | `PORT_BROKER_NAME` | broker name |

The Slack webhook name is recommended as `/portfolio/paper/ops/slack-webhook` (Secrets Manager) per the §1.3 decision. Actual registration is out of this spec's scope and is direct operator work.

### 2.5 RDS master Compatibility (basis R2.5)

- The `/portfolio/paper/rds/master` already registered in the 02 spec is the same format as this rule and is used as-is. This spec's `/portfolio/paper/rds/marketconnector-app` is added as a new item. For the permission separation of the two secrets, see the §4.2 matrix.

## 3. Environment-Variable Compatibility

### 3.1 Maintain the 8 MS Environment-Variable Keys (basis R3.1 / OD-DB-003)

Even after introducing this spec, the existing environment-variable key names of the 8 MS are not changed. The following keys are kept as-is.

- DB connection series: `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`, `PORTFOLIO_DB_NAME`
- Account / environment series: `PORT_ACCOUNT_NO`, `PORT_BROKER_NAME`, `PORT_ENVIRONMENT`
- Strategy / order series: `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION`, `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT`

### 3.2 port-marketconnector `config.py` Externalization (basis R3.2)

- Externalize the KIS identifiers used by `port-marketconnector`'s `c:\Workspaces\port-marketconnector\config.py`.
  - Targets: `APP_KEY`, `APP_SECRET`, `BASE_URL`, `PAPER_ACNT`, `ACNT_PRDT_CD`
  - Map them so they can be injected as environment variables without code changes. For the mapping, see the §2.4 table.
- This spec (06) does not handle modifying `config.py` itself (8 MS code no-modification policy, R14.1). Externalization proceeds only in the form of environment-variable injection.

### 3.3 Injection Timing (basis R3.3)

- The secret / parameter value is injected as an environment variable at process start.
- Reflecting automatic rotation during runtime (a running process automatically reading a new value) is out of this spec's scope.

### 3.4 Temporary Operating Mode vs Normal Operation (basis R3.4)

| Mode | Form | Location | Policy |
|------|------|------|------|
| Temporary operating mode | Direct injection via shell `export` | Operator shell session inside EC2 | The mode used in this spec's morning validation. Going forward, not recommended except for one-off validation / debugging |
| Normal operating mode | Injection via Secrets Manager / SSM Parameter Store | EC2 startup script or systemd `EnvironmentFile` (the secret value itself is not stored) | Standard |

The secret value plaintext must not be stored in `EnvironmentFile`. Detailed inspection items are handled in §5 and the follow-up phase's [`./validation-checklist.md`](./validation-checklist.md).

## 4. MarketConnector EC2 Instance Role Design (basis R4)

### 4.1 Role / Instance Profile Name Candidates (basis R4.1)

| Item | Name candidate |
|------|-----------|
| IAM Role | `portfolio-paper-marketconnector-ec2-role` |
| IAM Instance Profile | `portfolio-paper-marketconnector-ec2-profile` |
| Trust Policy Principal | `Service: ec2.amazonaws.com` |
| Trust Policy Action | `sts:AssumeRole` |
| Trust Policy Effect | `Allow` |

### 4.2 Permission Policy Matrix (basis R4.2 / R4.3 / R4.6)

| Statement | Effect | Action | Resource | Note |
|-----------|--------|--------|----------|------|
| `SecretsManagerRead` | Allow | `secretsmanager:GetSecretValue`, `secretsmanager:DescribeSecret` | 4 secret ARNs (KIS 3 + RDS 1). See §2.4 for the detailed list | No permission granted for other-service secrets. The actual ARN suffix `-XXXXXX` is recorded at operator registration |
| `SsmParameterRead` | Allow | `ssm:GetParameter`, `ssm:GetParameters`, `ssm:GetParametersByPath` | `arn:aws:ssm:<region>:<account-id>:parameter/portfolio/paper/marketconnector/*` | Prefix only. Including another service prefix is prohibited |
| `KmsDecrypt` (conditional) | Allow | `kms:Decrypt` | `arn:aws:kms:<region>:<account-id>:key/<kms-key-id>` | See the Row Notes for detailed conditions |

#### Row Notes — SecretsManagerRead Resource details

- The 4 secret ARNs that go in the Resource position follow the secret names below.
  - `/portfolio/paper/marketconnector/kis-app-key`
  - `/portfolio/paper/marketconnector/kis-app-secret`
  - `/portfolio/paper/marketconnector/paper-account`
  - `/portfolio/paper/rds/marketconnector-app`
- ARN form: `arn:aws:secretsmanager:<region>:<account-id>:secret:<name>-*`

#### Row Notes — KmsDecrypt conditions

- This statement applies only when using a Secrets Manager CMK or SSM SecureString.
- If only the AWS managed `aws/secretsmanager` default key is used, this statement is not applicable.

Per basis R4.5, this matrix denotes all ARN positions only as a placeholder or `[REDACTED]`. The actual account-id / actual secret ARN / actual KMS Key ARN are never recorded in this document.

### 4.3 Prohibition Policy Matrix (basis R4.4)

| Pattern | Prohibition reason |
|------|----------|
| `Resource: "*"` | Over-permission. R-SEC candidate (§8). Able to read even other-service / other-environment secrets |
| `Action: "*"` | Over-permission. Grants write / delete / rotate beyond read |
| `secretsmanager:*` | Over-permission. Grants `CreateSecret` / `DeleteSecret` / `PutSecretValue` |
| `ssm:*` | Over-permission. Grants `PutParameter` / `DeleteParameter` |
| Including other-service-prefix (`/portfolio/paper/view/*`, `/portfolio/paper/crawler/*`, `/portfolio/paper/strategy/*`, `/portfolio/paper/research/*`) Resource | Out of 06's responsibility scope. 03 / 08 / 04 / 05 / 09 spec division |
| Including other-environment-prefix (`/portfolio/live/...`) Resource | aws-live IAM is the 10 spec integration responsibility |

### 4.4 Responsibility Scope Separation (basis R4.7)

| spec | Permissions handled |
|------|-------------|
| 06 (this spec) | secret / parameter read permission (Secrets Manager, SSM Parameter Store, conditional KMS Decrypt) |
| 03 (marketconnector-ec2) | `AmazonSSMManagedInstanceCore` for SSM Session Manager connection (managed policy attach), CloudWatch Logs write, EC2 operational auxiliary permissions |

This spec's Permission Policy and 03's managed policy attach are both attached to the same IAM Role, but their responsibility / lock timing is separated.

### 4.5 Document Notation Rule (basis R4.5)

- All ARNs use only the placeholder below or `[REDACTED]`.
  - `arn:aws:iam::<account-id>:role/portfolio-paper-marketconnector-ec2-role`
  - `arn:aws:secretsmanager:<region>:<account-id>:secret:/portfolio/paper/...`
  - `arn:aws:ssm:<region>:<account-id>:parameter/portfolio/paper/...`
  - `arn:aws:kms:<region>:<account-id>:key/<kms-key-id>`
- The actual account-id / actual secret ARN / actual KMS Key ARN are never recorded in this document / follow-up-phase deliverables.

## 5. Access Key Non-Use Principle (basis R5)

### 5.1 Prohibition / Standard Matrix (basis R5.1 / R5.2)

| Item | Policy |
|------|------|
| `~/.aws/credentials` long-lived access key file | **Prohibited** |
| access key item in `~/.aws/config` | **Prohibited** |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` environment variables (export / storage inside EC2) | **Prohibited** |
| systemd unit's `Environment=AWS_ACCESS_KEY_ID=...` | **Prohibited** |
| storing an access key in a systemd unit's `EnvironmentFile=` | **Prohibited** |
| exporting an access key in a user home dotfile (`~/.bashrc`, `~/.profile`, `~/.bash_profile`) | **Prohibited** |
| storing an access key in an application config file / `.env` file | **Prohibited** |
| IMDSv2 + Instance Role credentials | **Standard use** |

### 5.2 Validation Location (basis R5.3)

| Deliverable | Validation item |
|--------|-----------|
| [`./runbook.md`](./runbook.md) (follow-up phase) | Credential validation command (details below) |
| [`./validation-checklist.md`](./validation-checklist.md) (follow-up phase) | `~/.aws/credentials` non-existence check, assumed-role ARN match check, 0 access key patterns in `EnvironmentFile=` check |

#### Row Notes — runbook.md credential validation command

- Run `aws sts get-caller-identity` inside the EC2 → confirm the assumed-role ARN.
  - Expected form: `arn:aws:sts::<account-id>:assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>`
- Confirm the `access_key` source of the `aws configure list` result is `iam-role`.

The actual validation is the follow-up phase's responsibility. This design specifies only the location.

### 5.3 Emergency Scenario Handling (basis R5.4)

- If the operator finds an emergency scenario where an access key must temporarily be placed inside the EC2, an arbitrary decision in this spec is prohibited.
- Record only a change proposal in the OD-SEC category of [`../_common/operator-decisions.md`](../_common/operator-decisions.md) and apply upon operator approval.
- After emergency application, separate the procedure of immediately discarding the access key + returning to this spec's standard (IMDSv2 + Instance Role) into the [복구] stage of [`./runbook.md`](./runbook.md).

### 5.4 access key Value Notation Policy (basis R5.5)

- Never record the actual access key id or secret access key value in any deliverable of this spec (design / tasks / runbook / validation-checklist / operation-notes). Use only `[REDACTED]`.

## 6. ECS Task Role Reuse Pattern (actual creation is 03 / 08 / 04 / 05 / 09)

This spec leaves only a pattern template, and filling in the actual Role / Resource ARN is the responsibility of follow-up specs. Basis R6 / R13.

### 6.1 Role Separation (basis R6.2)

| Name | Responsibility | Permission type | Usage timing |
|------|------|----------|-----------|
| Task Execution Role | ECR image pull, CloudWatch Logs write, Secrets Manager environment-variable injection | AWS managed `AmazonECSTaskExecutionRolePolicy` + Secrets Manager Resource ARN limited | At Task start, used by the AWS Service (ECS Agent) side |
| Task Role | Application-runtime secret / parameter read | The same skeleton as 06's Instance Role permission matrix (§4.2) | Used by the container process |

#### Row Notes — Task Execution Role vs Task Role Action

- Task Execution Role Action: `secretsmanager:GetSecretValue` (for ECS `secrets` field injection).
- Task Role Action: `secretsmanager:GetSecretValue`, `secretsmanager:DescribeSecret`,
  `ssm:GetParameter`, `ssm:GetParameters`, `ssm:GetParametersByPath`.

### 6.2 ECS `secrets` Field Pattern (basis R6.3)

- The `containerDefinitions[*].secrets[*]` item of the ECS Task Definition follows the pattern below (placeholder).
  - `name=<ENV_KEY>, valueFrom=arn:aws:secretsmanager:<region>:<account-id>:secret:/portfolio/{env}/{service}/{item}-XXXXXX`
- When using a JSON multi-key secret (e.g., `/portfolio/paper/rds/marketconnector-app`), map it by appending the `:json-key::` suffix to the end of `valueFrom` (placeholder).
  - Example: `arn:aws:secretsmanager:<region>:<account-id>:secret:/portfolio/paper/rds/marketconnector-app-XXXXXX:host::`
- The actual ARN suffix / json-key branching is filled in by follow-up specs.

### 6.3 Division Matrix (basis R6.4 / R6.5 / R13)

| spec | Responsibility | Use of this spec 06's input |
|------|------|---------------------|
| 03 marketconnector-ec2 | Instance Role formal operation, SSM Session Manager attach, CloudWatch Logs | Receives 06's Instance Role policy (§4.2) as-is as input |
| 08 interest-crawler-and-preprocessor-ecs | Create crawler / preprocessor-dedicated Task Roles | 06's Task Role skeleton (§6.1, §6.2) + change only the service prefix (`crawler` / `preprocessor`) |
| 04 strategy-batch-stepfunctions | strategy decision / execution Task Role, Step Functions IAM | Uses 06's Task Role skeleton |
| 05 port-view-ecs-and-runbook | port-view Task Role | Uses 06's Task Role skeleton |
| 09 strategy-research-batch | research (AWS Batch + Step Functions) Task Role / Job Role | Uses 06's Task Role skeleton |

### 6.4 Decisions Follow-Up Specs Must Not Change (basis R13.4)

- Keep the naming `/portfolio/{env}/{service}/{item}` format.
- Use only `paper` / `live` for the `{env}` value.
- Resource wildcard (`Resource: "*"`) prohibited.
- Action wildcard (`secretsmanager:*` / `ssm:*` / `Action: "*"`) prohibited.
- Granting other-service-prefix Resource prohibited.

## 7. operator-decisions.md Update Candidates (actual update is the tasks stage)

Candidates to reflect in [`../_common/operator-decisions.md`](../_common/operator-decisions.md) after this spec is locked. Basis R10. The actual update is this spec's [`./tasks.md`](./tasks.md) stage.

### 7.1 This Spec's Lock / New Candidates

| ID | Item | This spec's candidate decision value | Status (English / Korean) |
|----|------|--------------------|---------------------|
| OD-SEC-001 | Secrets storage location | Separate Secrets Manager items and SSM Parameter Store items (details below) | TENTATIVE 🟡 잠정 (CONFIRMED 🟢 확정 upon operator approval) |
| OD-OBS-004 | Slack webhook storage location | **Secrets Manager recommended**. SSM SecureString allowed when reducing cost | TENTATIVE 🟡 잠정 |
| OD-SEC-005 (new candidate) | MarketConnector EC2 / 8 MS Access Key non-use principle | Prohibit storing an access key file / environment variable / dotfile inside EC2. Use only IMDSv2 + Instance Role credentials | TENTATIVE 🟡 잠정 |
| OD-SEC-006 (new candidate) | EC2 / ECS IAM Role-based secret / parameter read principle | Least privilege (Action whitelist). Resource wildcard prohibited. Action wildcard prohibited. service prefix separation | TENTATIVE 🟡 잠정 |

#### Row Notes — OD-SEC-001 candidate decision value

- Secrets Manager storage
  - KIS app key / app secret / paper account / RDS connection info / RDS master
- SSM Parameter Store storage
  - KIS base URL / Connector host·port·debug
  - General settings such as `PORT_ENVIRONMENT` / `PORT_BROKER_NAME`

### 7.2 Existing Decision Consistency (basis R10.5)

| ID | Item | Consistency with this spec's decision | This spec's update recommendation |
|----|------|----------------------|------------------|
| OD-SEC-002 | RDS master password = Secrets Manager | Consistent | Keep status (can lock TENTATIVE 🟡 → CONFIRMED 🟢 at operator approval time) |
| OD-SEC-003 | KIS access_token storage location = EC2 local file + S3 backup 1st | Consistent (details below) | Keep status (TENTATIVE 🟡) |

#### Row Notes — OD-SEC-003 consistency details

- In the paper environment, maintaining the token file (`access_token.txt`) is the responsibility of the 03 spec.
- The option to store the token itself in Secrets Manager is re-reviewed in the follow-up spec (03).

### 7.3 Status Label Rule (basis R10.7)

- English canonical values: CONFIRMED / TENTATIVE / TBD / DEFERRED.
- Display-purpose Korean / color: 🟢 확정 / 🟡 잠정 / 🔴 미정 / 🔵 보류.
- Same rule as the 02 spec. All deliverables of this spec use the same rule.

## 8. risk-register.md Update Candidates (actual update is the tasks stage)

Candidates to reflect in [`../_common/risk-register.md`](../_common/risk-register.md) after this spec is locked. Basis R12.

- The actual update is this spec's [`./tasks.md`](./tasks.md) stage.
- The ID is assigned the next number in the risk-register at the time of addition (e.g., `R-SEC-004` ~ `R-SEC-007`).
- This design only reserves the slots, and the final IDs are locked at the tasks stage.

| ID (tentative) | Risk summary |
|-----------|-----------|
| R-SEC-XXX-a | IAM policy over-permission able to read via Resource wildcard or even other-service secrets |
| R-SEC-XXX-b | secret value plaintext exposure in EC2 environment variables / process logs / systemd `EnvironmentFile` |
| R-SEC-XXX-c | Operational mistake of storing an IAM access key id / secret access key file inside EC2 |
| R-SEC-XXX-d | Read failure at application runtime or reading a wrong environment value due to Parameter / Secret naming mismatch |

#### Details — R-SEC-XXX-a IAM policy over-permission

- Mitigation: exact Resource ARN limiting (§4.2) + service prefix whitelist + Action whitelist.
- Detection: IAM Access Analyzer / static policy inspection / [`./validation-checklist.md`](./validation-checklist.md) 0-Resource-wildcard check.
- Rollback: policy detach + restore the previous policy.

#### Details — R-SEC-XXX-b EC2 secret value plaintext exposure

- Mitigation: prohibit recording the secret value itself in EnvironmentFile / log / dotfile. Allow only runtime read. log mask policy.
- Detection: log grep check (0 `APP_SECRET=`, `PASSWORD=` patterns).
  Check whether the `aws sts get-caller-identity` flow is Instance Role-based.
- Rollback: immediately rotate the exposed secret (KIS app secret / RDS password / Slack webhook, etc.).

#### Details — R-SEC-XXX-c EC2 access key file storage mistake

- Mitigation: forced check that `~/.aws/credentials` does not exist, enforce IMDSv2, 0 dotfile / EnvironmentFile access key patterns check.
- Detection: [`./validation-checklist.md`](./validation-checklist.md) label / periodic check.
- Rollback: immediately discard the access key (IAM Console) + return to Instance-Role-credentials-only mode.

#### Details — R-SEC-XXX-d naming mismatch

- Example: `paper` reads a `live` parameter.
- Mitigation: enforce this spec's naming rule (§2) + specify in IaC / runbook / validation-checklist + enforce `{env}` separation.
- Detection: alert on container / EC2 startup health check failure.
  Check that the environment identifier (`PORT_ENVIRONMENT`) matches the secret path prefix.
- Rollback: correct the name then redeploy. If read with a wrong environment value, immediately stop the process + review secret rotation.

Each risk adds a row per the column format below at the tasks stage. Basis R12.5.

- Reference: [`../02-aws-network-and-rds/risk-register.md`](../_common/risk-register.md) column format
- Columns: Area / Risk / Impact / Probability / Mitigation / Detection / Rollback / Affected Spec / Status

## 9. followups-overview.md Update Candidates (actual update is the tasks stage)

Candidates to reflect in [`../_common/followups-overview.md`](../_common/followups-overview.md) after this spec is locked. Basis R11. The actual update is this spec's [`./tasks.md`](./tasks.md) stage.

### 9.1 Input Items of This Spec's (06) Decisions (basis R11.2)

| Follow-up spec | Input items of this spec 06's decisions |
|-----------|---------------------------|
| 03 marketconnector-ec2 | Use the Instance Role policy matrix (§4.2) as-is. SSM Session Manager / CloudWatch Logs / token handoff procedures are 03's responsibility |
| 08 interest-crawler-and-preprocessor-ecs | Task Role skeleton (§6.1 / §6.2) + crawler / preprocessor service prefix (`/portfolio/paper/crawler/*`, `/portfolio/paper/preprocessor/*`) |
| 04 strategy-batch-stepfunctions | Task Role skeleton + strategy service prefix (`/portfolio/paper/strategy/*`) + Step Functions IAM policy (enforcing no-auto-retry steps is 04's responsibility) |
| 05 port-view-ecs-and-runbook | Task Role skeleton + view service prefix (`/portfolio/paper/view/*`) |
| 09 strategy-research-batch | Task Role skeleton / AWS Batch Job Role + research service prefix (`/portfolio/paper/research/*`) |
| 10 cutover-and-validation-runbook | The aws-live environment IAM integration point. Register `/portfolio/live/...` prefix secret / parameter and lock the Role permission matrix |

### 9.2 This Spec's (06) First Application Environment / Scope / Out-of-Scope (basis R11.1)

- First application environment: `aws-paper`.
- First scope: MarketConnector EC2 Instance Role + Secrets Manager / SSM Parameter Store registration (`marketconnector` / `rds` service prefix).
- Out-of-scope: live rotation automation, GitHub Actions OIDC, 8 MS full IAM matrix, aws-live IAM.

### 9.3 06 ↔ 03 Order (basis R11.4)

- 06 locks the secret / parameter / IAM skeleton first → 03 receives it as input for the EC2 formal operation (Connector / Flask / KIS API / RDS connection). The 03 EC2 formal operation stage does not enter before 06 is locked.

## 10. Follow-Up Phase Plan

Deliverable responsibility division after this spec design is locked. Basis R7 / R8 / R9.

| Deliverable | Responsibility (summary) | This spec's input |
|--------|-------------|--------------|
| [`./tasks.md`](./tasks.md) | Decompose the design decisions and _common update candidates (§7 / §8 / §9) into task units | §7 / §8 / §9 / [`./requirements.md`](./requirements.md) R10 / R11 / R12 |
| [`./runbook.md`](./runbook.md) | Operator direct-work procedure (details below) | §1 / §2 / §4 / §5 / [`./requirements.md`](./requirements.md) R7 |
| [`./validation-checklist.md`](./validation-checklist.md) | 4-label + 8-inspection-area checklist (details below) | §4 / §5 / [`./requirements.md`](./requirements.md) R8 |
| [`./operation-notes.md`](./operation-notes.md) | Per-date accumulative records (details below) | §11 / [`./requirements.md`](./requirements.md) R9 |

#### Row Notes — runbook.md stages

- Labels: [실행] / [확인] / [준비] / [복구]
- Stage order
  - (a) Secrets Manager registration
  - (b) SSM Parameter Store registration
  - (c) Instance Role / Profile creation
  - (d) policy attach
  - (e) Profile attach to the EC2 instance
  - (f) EC2 internal secret / parameter read validation
  - (g) KIS / RDS smoke test after Connector restart

#### Row Notes — validation-checklist.md labels / inspection areas

- 4 labels: `[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]`
- 8 inspection areas
  - (1) Secrets Manager registration inventory
  - (2) SSM Parameter Store registration inventory
  - (3) Role / Instance Profile existence / attach status
  - (4) policy matrix (§4.2) match
  - (5) 0 Resource wildcards
  - (6) EC2 access key file non-existence
  - (7) `aws sts get-caller-identity` assumed-role ARN match
  - (8) Connector smoke test pass

#### Row Notes — operation-notes.md recording policy

- Per-date accumulative records.
- No secret value recorded — record only success / failure.
- IAM Role / Policy change record template
  - change date / changer / change reason / before · after item summary
  - Quoting the full JSON body is prohibited

Each deliverable follows the same-deliverable conventions of the 02 spec (4 labels, per-date `## YYYY-MM-DD ...` accumulative format, secret `[REDACTED]` policy) as-is.

## 11. Safety Constraints (summary)

Applies to all phases of this spec. Basis R14.

- Do not modify the README / AGENTS.md / CHANGELOG / docs / worklog and source code of the following 8 MS.
  - `port-view`, `port-marketconnector`
  - `port-interest-crawler`, `port-interest-preprocessor`
  - `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`
- Do not create, change or delete actual AWS resources (Secrets Manager secret, SSM Parameter, IAM Role, IAM Policy, IAM Instance Profile, EC2 Instance Profile attach change, KMS Key, etc.). The operator performs all actual creation / modification / deletion directly.
- Running the 8 MS entrypoints is prohibited. broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / order / fill / Daily Batch / intraday monitor calls are prohibited.
- Do not write the following items anywhere in this spec's deliverables (design / tasks / runbook / validation-checklist / operation-notes).
  - actual secret value / KIS app key / KIS app secret / account number
  - RDS password / RDS endpoint hostname / account-id
  - actual secret ARN / actual KMS Key ARN
  - IAM access key id / secret access key / Slack webhook URL
- Use only `[REDACTED]` or a placeholder (`<account-id>`, `<region>`, `<kms-key-id>`).
- The `secretsmanager:GetSecretValue` call is performed only by the operator. Kiro's automatic validation uses only `secretsmanager:DescribeSecret` metadata. Basis R14.5.
- live rotation automation / CI/CD OIDC / full IAM matrix / aws-live environment IAM are out of this spec's scope. Division to follow-up specs (07 / 03 / 08 / 04 / 05 / 09 / 10). Basis R14.6.

## Testing Strategy (reference)

This spec is a deliverable of IaC / IAM policy / secret storage-location decisions / operator procedures. There is no code / pure function / algorithm whose behavior changes with input variation. Therefore property-based testing (PBT) does not apply. This design does not include a Correctness Properties section.

This spec's validation is performed only in the following two forms.

- Static policy inspection: check whether the policy JSON's Action / Resource matches the §4.2 matrix (including 0 wildcard usages). The responsibility of [`./validation-checklist.md`](./validation-checklist.md) (follow-up phase).
- smoke test: check along the following two axes.
  - Check the EC2 internal metadata call results: `aws sts get-caller-identity` / `aws secretsmanager describe-secret` / `aws ssm get-parameter`
  - Check whether KIS / RDS calls pass after Connector restart
- Responsibility: [`./runbook.md`](./runbook.md) / [`./validation-checklist.md`](./validation-checklist.md) (follow-up phase).
- The `secretsmanager:GetSecretValue` call is performed only by the operator.
