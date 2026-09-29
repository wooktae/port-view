# Requirements Document — 06-secrets-and-iam

## Introduction

Core of this spec

- The 6th stage of the PORT-STRATEGY-AI AWS Migration.
- Target: the secret / parameter used when EC2 / ECS Task connects to the KIS paper-trading API and the RDS `portfolio` DB.
- 3 confirmed scopes
  - the storage location of the secret / parameter
  - the IAM Role / Policy-based least-privilege read policy
  - the authoring criteria for the operator procedure / validation / operation notes

Prerequisite spec status

- `02-aws-network-and-rds` first-application complete (direct operator work).
  - VPC / Subnet / SG / 5 VPC Endpoints (Secrets Manager / SSM / CloudWatch Logs / ECR / S3)
  - RDS PostgreSQL
  - 7 DB roles: `marketconnector_app`, `view_app`, `crawler_app`, `preprocessor_app`,
    `decision_app`, `research_app`, `execution_app`
- The MarketConnector EC2 (Amazon Linux 2023, public subnet, EIP attach) is already created.
- On the morning of this spec's work, the operator passed the following flows based on shell environment variables.
  - KIS paper API query
  - RDS `marketconnector_app` connection
  - `connector_balance.py` / `connector_order_check.py` execution
  - Flask query-type endpoint smoke test

First application environment = `aws-paper`. The core this spec covers is narrowed to the following single item.

- First confirm the storage location / permission / procedure so the MarketConnector EC2 reads the KIS / RDS / general setting values from
  Secrets Manager / SSM Parameter Store using only the EC2 Instance Role, without storing an Access Key.
- Leave only a minimal skeleton so that 03-marketconnector-ec2 / 08-interest-crawler-and-preprocessor-ecs can later reuse the same secret / parameter
  pattern with an ECS Fargate Task Role.

What is not included (out of scope)

| Out-of-scope item | Transferred spec |
| --- | --- |
| live rotation automation (Lambda rotation / scheduled rotation) | (after this spec) |
| CI/CD OIDC role (GitHub Actions OIDC) design | 07 |
| Full IAM matrix for all 8 MS | 08 / 04 / 05 / 09 (pattern reuse) |
| aws-live environment secret storage / IAM matrix | 10-cutover-and-validation-runbook (planned) |
| Modifying the 8 MS source code / README / AGENTS.md / CHANGELOG / docs / worklog | (modification prohibited) |

Deliverables of this spec (06 folder)

- `requirements.md`, `design.md`, `tasks.md`, `runbook.md`, `validation-checklist.md`, `operation-notes.md`
- Auxiliary document (if needed): `traceability-matrix.md`
- In this phase, only `requirements.md` is authored. The remaining documents are created in follow-up phases.

Root common reference / update candidates (actual updates are performed in this spec's tasks stage)

- [`../_common/operator-decisions.md`](../_common/operator-decisions.md)
- [`../_common/followups-overview.md`](../_common/followups-overview.md)
- [`../_common/risk-register.md`](../_common/risk-register.md)
- [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md)

Prerequisite / follow-up specs

- Prerequisite: [`../01-aws-migration-foundation`](../01-aws-migration-foundation),
  [`../02-aws-network-and-rds`](../02-aws-network-and-rds)
- Follow-up specs where this spec's decisions are used as input (all planned)
  - [`../03-marketconnector-ec2`](../03-marketconnector-ec2)
  - [`../08-interest-crawler-and-preprocessor-ecs`](../08-interest-crawler-and-preprocessor-ecs)
  - [`../04-strategy-batch-stepfunctions`](../04-strategy-batch-stepfunctions)
  - [`../05-port-view-ecs-and-runbook`](../05-port-view-ecs-and-runbook)
  - [`../09-strategy-research-batch`](../09-strategy-research-batch)
  - [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook)

This document does not write the following items. All use only `[REDACTED]` or a placeholder.

- actual secret / password / token / KIS app key / KIS app secret
- account number / webhook URL / access key
- DB endpoint hostname / account-id

## Glossary

- Single portfolio DB: the PostgreSQL database `portfolio` shared by all MS. First-applied as RDS for PostgreSQL in the 02 spec.
- MarketConnector EC2: the EC2 instance that runs the aws-paper KIS Connector. It was first created as an RDS restore runner at the time of the 02 spec, and is formally used in the 03 spec as the KIS Connector / Flask app operating form.
- Instance Role: the IAM Role attached to an EC2 instance. It lets processes running inside EC2 call the AWS API with IAM Role permissions without an access key / secret key.
- Task Role: the IAM Role attached to an ECS Task definition. It lets an ECS Fargate / EC2 Task call the AWS API with IAM Role permissions without an access key. Since there is no ECS Task yet in this spec, only the pattern definition is left.
- Secrets Manager: AWS Secrets Manager. In this spec it is used as a per-value secret store for values such as KIS app key / app secret / paper account number / RDS DB role password / RDS master password / Slack webhook (after decision).
- SSM Parameter Store: AWS Systems Manager Parameter Store. In this spec it is used as a general setting-value store for values such as KIS base URL / Connector host / port / debug / account product code / environment identifier.
- SSM SecureString: a KMS-encrypted parameter of the SSM Parameter Store. It can be used as a store for some secret candidates when cost reduction is desired. In this spec it is evaluated as a Slack webhook candidate.
- Least-privilege read: a policy that limits the IAM Policy Resource clause to the exact secret ARN / parameter ARN and does not use the `*` wildcard. It grants no action other than `secretsmanager:GetSecretValue` / `ssm:GetParameter` / `ssm:GetParameters`.
- Access Key non-use principle: the policy that an EC2 / ECS Task must not store an IAM access key / secret access key inside itself as a file / environment variable / config, and must perform AWS API calls using only the Instance Role or Task Role.
- naming rule: the rule that a secret / parameter name must consistently express the environment (`paper` / `live`), service (`marketconnector` / `crawler` / `view`, etc.) and item (`kis-app-key` / `db-password`, etc.) as a prefix / path. This is first confirmed in this spec.

## Requirements

### Requirement 1: Confirm the Secrets / Parameters classification criteria (OD-SEC-001)

**Objective**: As an operator, I want to confirm the classification criteria for whether each KIS / RDS / general setting / Slack webhook value is stored in Secrets Manager or SSM Parameter Store, so that the operator can decide what item goes where by looking only at the spec / runbook.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL include KIS app key, KIS app secret, and KIS paper account-number-related values (`PAPER_ACNT`, `ACNT_PRDT_CD`) in the Secrets Manager storage candidate list.
2. WHEN design.md is authored, THE design.md SHALL include the RDS `marketconnector_app` connection info (host, port, db name, user, password) in the Secrets Manager storage candidate list, and SHALL also specify the RDS master (`portfolio_admin`) password as a separate item.
3. WHEN design.md is authored, THE design.md SHALL include in the SSM Parameter Store storage candidate list
   general setting items such as KIS base URL (`BASE_URL` / a per-environment endpoint like `https://openapivts.koreainvestment.com:29443`),
   Connector Flask host / port / debug, `PORT_ENVIRONMENT`, `PORT_BROKER_NAME`,
   `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION`.
4. WHEN handling the Slack webhook storage location, THE design.md SHALL compare the two candidates Secrets Manager (recommended) and SSM SecureString (cost-saving option), and specify the OD-OBS-004 decision and the decision value to be finally locked in this spec in one place.
5. WHEN handling the classification criteria, THE design.md SHALL codify the first classification criterion that "a value where exposure enables external transmission / external authentication / external fund movement" is sent to Secrets Manager.
6. WHEN handling the classification criteria, THE design.md SHALL codify the first classification criterion that "a general setting value that does not directly cause external action even if exposed but whose value differs by environment" is sent to SSM Parameter Store.
7. WHERE the operator wants to move some secrets to SSM SecureString for cost reduction, THE design.md SHALL explicitly separate the movable candidates (e.g., Slack webhook) from the must-not-move candidates (KIS app secret, KIS app key, RDS password).

### Requirement 2: First confirm the naming / path rules

**Objective**: As an operator, I want to first confirm the secret / parameter name and path rules, so that even if environments (`paper` / `live`) or services increase, secrets can be found without naming conflicts, and the IAM Resource clause can be cleanly limited by an ARN prefix.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL specify the Secrets Manager name rule
   `/portfolio/{env}/{service}/{item}` format as a first recommendation
   (e.g., `/portfolio/paper/marketconnector/kis-app-key`, `/portfolio/paper/marketconnector/kis-app-secret`,
   `/portfolio/paper/rds/master`, `/portfolio/paper/rds/marketconnector-app`).
2. WHEN design.md is authored, THE design.md SHALL specify the SSM Parameter Store name rule
   `/portfolio/{env}/{service}/{item}` format as a first recommendation
   (e.g., `/portfolio/paper/marketconnector/kis-base-url`, `/portfolio/paper/marketconnector/connector-host`,
   `/portfolio/paper/marketconnector/connector-port`).
3. WHEN handling the env notation, THE design.md SHALL use the two values `paper` / `live` and specify that additional envs such as `dev` / `test` are out of this spec's scope (OD-ENV-001 / OD-ENV-003 input).
4. WHEN handling the service notation, THE design.md SHALL, at the time of this spec, specify `marketconnector`, `rds`, `view`, `strategy`, `crawler`, `preprocessor`, `research`, `ops` as service prefix candidates, and SHALL limit what this spec (06) treats as the first-confirmation target to the two `marketconnector` and `rds`.
5. IF an already-registered secret name in the existing 02 spec / `operation-notes.md` differs from this rule, THEN THE design.md SHALL specify the compatibility policy that the existing name (e.g., `/portfolio/paper/rds/master`) is kept as-is and this rule is applied from new items onward.
6. WHEN handling the naming rule, THE design.md SHALL codify the policy of never including an actual secret value / endpoint hostname / account number in the secret / parameter name itself.

### Requirement 3: Maintain environment-variable key compatibility (OD-DB-003)

**Objective**: As an operator, I want the existing environment-variable key names of the 8 MS to remain unchanged even after introducing secret / parameter, so that only the secret values can be externalized without modifying code / README / AGENTS.md.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL specify the policy that the following environment-variable keys remain unchanged:
   `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`,
   `PORTFOLIO_DB_NAME`, `PORT_ACCOUNT_NO`, `PORT_BROKER_NAME`, `PORT_ENVIRONMENT`,
   `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION`, `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT`.
2. WHEN design.md is authored, THE design.md SHALL specify, as a first recommendation, the externalization candidate key names and mapping table for the KIS-related identifiers (`APP_KEY`, `APP_SECRET`, `BASE_URL`, `PAPER_ACNT`, `ACNT_PRDT_CD`) used by `port-marketconnector`'s `config.py`, so they can be injected via environment variables without code changes.
3. WHEN design.md is authored, THE design.md SHALL specify that the point at which the secret / parameter value is injected as an EC2 / ECS container environment variable is at process start, and that reflecting automatic rotation during runtime is out of this spec's scope.
4. IF the operator wants to keep operating with KIS app key / app secret / account number directly exported to shell environment variables even after this spec, THEN THE design.md SHALL classify this as a temporary operating mode and specify the policy that Secrets Manager-based injection is the standard in normal operation.

### Requirement 4: MarketConnector EC2 Instance Role least privilege (linked to OD-SEC-001 / R-SEC-001)

**Objective**: As an operator, I want the MarketConnector EC2 Instance Role to be granted only the necessary secret / parameter read permissions, so that it can operate without storing an access key inside EC2 while blocking the over-permission risk (adjacent to R-SEC-001).

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL specify the MarketConnector EC2 Instance Role name candidate (e.g., `portfolio-paper-marketconnector-ec2-role`) and the Instance Profile name candidate attached to it.
2. WHEN handling the Instance Role's Secrets Manager permission, THE design.md SHALL
   limit the Action to only the two `secretsmanager:GetSecretValue` and `secretsmanager:DescribeSecret`,
   and the Resource clause SHALL include only the following secret ARNs:
   KIS app key, KIS app secret, KIS paper account-number-related (when `PAPER_ACNT` / `ACNT_PRDT_CD` are classified as secrets),
   the RDS `marketconnector_app` connection-info secret.
   No permission is granted for other secrets (such as other MS's DB role passwords).
3. WHEN handling the Instance Role's SSM Parameter Store permission, THE design.md SHALL limit the Action to only `ssm:GetParameter`, `ssm:GetParameters`, `ssm:GetParametersByPath`, and specify that the Resource clause includes only the `/portfolio/paper/marketconnector/*` path prefix.
4. WHEN handling the Resource clause, THE design.md SHALL codify the policy of never using a wildcard policy of the form `Resource: "*"`.
5. WHEN an IAM Role / Policy ARN appears in the document, THE design.md SHALL denote the ARN position only as a placeholder (`arn:aws:iam::<account-id>:role/portfolio-paper-marketconnector-ec2-role`) or `[REDACTED]`, and SHALL not write the actual account-id / actual secret ARN as-is.
6. IF the Instance Role needs KMS Decrypt permission (when using SSM SecureString or a Secrets Manager CMK), THEN THE design.md SHALL limit the Action to `kms:Decrypt` and specify that the Resource clause includes only the KMS Key ARN decided for use in this spec. If a CMK is not used / only the `aws/secretsmanager` default key is used, it specifies that this item is not applicable.
7. WHEN handling auxiliary permissions such as CloudWatch Logs / SSM Session Manager, THE design.md SHALL specify that the responsibility scope of this spec (06) is the secret / parameter read permission, and that the managed policy for SSM Session Manager connection (e.g., `AmazonSSMManagedInstanceCore`) is separated into the 03 spec (MarketConnector EC2 operation) area.

### Requirement 5: EC2 internal Access Key non-use principle

**Objective**: As an operator, I want to codify the policy that an IAM access key / secret access key is never stored inside the MarketConnector EC2, so that even if the EC2 is compromised, the access key itself does not leak externally.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL codify the principle that "no IAM access key id and secret access key is stored in any file / environment variable / user home directory / systemd unit / config file inside the MarketConnector EC2".
2. WHEN design.md is authored, THE design.md SHALL specify the policy that no process on the EC2 depends on a long-lived access key file of the `~/.aws/credentials` form, and uses only the EC2 metadata service (IMDSv2)-based Instance Role credentials.
3. WHEN runbook.md is authored in a follow-up phase, THE runbook.md SHALL include a procedure that verifies only the Instance Role credentials are in use via the `aws sts get-caller-identity` and `aws configure list` results inside the EC2.
4. IF the operator finds an emergency scenario where an access key must temporarily be placed inside the EC2, THEN THE design.md SHALL specify the policy of, instead of an arbitrary decision in this spec, recording only a change proposal in the OD-SEC category of [`../_common/operator-decisions.md`](../_common/operator-decisions.md) and applying it only after operator approval.
5. WHEN this spec's deliverables handle any value related to an access key, THE deliverable SHALL never write the actual access key id or secret access key value and use only `[REDACTED]`.

### Requirement 6: ECS Task Role reuse pattern (common to Connector / Crawler / Preprocessor / Strategy / View / Research)

**Objective**: As an operator,
I want 03-marketconnector-ec2 / 08-interest-crawler-and-preprocessor-ecs / 04-strategy-batch-stepfunctions /
05-port-view-ecs-and-runbook / 09-strategy-research-batch to receive a skeleton so they reuse the same pattern (Task Role-based secret / parameter read),
so that follow-up specs do not redo the IAM matrix design from scratch.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL specify the Task Role skeleton (Action list and Resource ARN pattern) used by an ECS Fargate / EC2 Task, and specify that it follows the same least-privilege read policy format as the MarketConnector EC2 Instance Role.
2. WHEN design.md is authored, THE design.md SHALL define separately the ECS Task Execution Role (for image pull / Secrets Manager environment-variable injection) and the Task Role (for application-runtime secret / parameter read), and specify each responsibility in one line.
3. WHEN design.md is authored, THE design.md SHALL specify, as a first recommendation, the ECS `secrets` field pattern (`name=ENV_KEY, valueFrom=<secret ARN>`) used when an ECS Task later injects a Secrets Manager secret value as an environment variable, and denote the actual ARN position only as a placeholder.
4. WHEN this spec handles the responsibility separation with follow-up specs, THE design.md SHALL specify the division principle that the 03 / 08 / 04 / 05 / 09 specs receive this spec's IAM skeleton as input and fill in only the per-service Resource ARN.
5. WHERE the ECS Task Role itself is not actually created at the time of this spec (06), THE design.md SHALL leave the Task Role definition only as a pattern (template) and specify that actual creation is the responsibility of the 03 / 08 specs.

### Requirement 7: Operator procedure (runbook.md) deliverable requirement

**Objective**: As an operator, I want to receive, in this spec's (06) design / tasks stage, a procedure that can be done directly in the AWS Console, so that I can follow secret registration / IAM Role creation / EC2 attach / validation by hand without writing IaC.

#### Acceptance Criteria

1. WHEN this spec's follow-up phase proceeds, THE follow-up phase SHALL create the `runbook.md` deliverable inside this spec folder.
2. WHEN runbook.md is authored, THE runbook.md SHALL decompose the following stages step by step:
   (a) register the KIS / RDS secret in Secrets Manager,
   (b) register the KIS base URL / Connector general settings in SSM Parameter Store,
   (c) create the MarketConnector EC2 Instance Role / Instance Profile,
   (d) attach the least-privilege policy to the Instance Role,
   (e) attach the Instance Profile to the EC2 instance,
   (f) verify secret / parameter read inside the EC2,
   (g) KIS / RDS smoke test after Connector restart.
3. WHEN handling each stage of runbook.md, THE runbook.md SHALL attach the `[실행]` / `[확인]` / `[준비]` / `[복구]` label to each stage, and include the items the operator clicks in the AWS Console, the input values (sensitive information as `[REDACTED]`), the validation method and the action on failure.
4. WHEN runbook.md handles the secret registration stage, THE runbook.md SHALL not write the actual KIS app key / app secret / account number / DB password value in this document and guide the operator to enter it directly in the Console.
5. WHEN runbook.md handles a cost-impacting stage (adding a Secrets Manager item, introducing a KMS CMK, etc.), THE runbook.md SHALL add an "approval required" mark and a one-line cost-impact summary to that stage.
6. WHEN runbook.md is authored, THE runbook.md SHALL follow the label / format / security principles of the 02 spec runbook as-is (secret `[REDACTED]`, no modification of the 8 MS code / docs, no external calls).

### Requirement 8: Validation items (validation-checklist.md) deliverable requirement

**Objective**: As an operator, I want to receive, in this spec's design / tasks stage, a 4-label-based validation checklist, so that I can use the same inspection flow as the 02 spec as-is.

#### Acceptance Criteria

1. WHEN this spec's follow-up phase proceeds, THE follow-up phase SHALL create the `validation-checklist.md` deliverable inside this spec folder.
2. WHEN validation-checklist.md is authored, THE validation-checklist.md SHALL use only the following 4 labels: `[O]`, `[X]`, `[Kiro 후속 작업 필요]`, `[운영자 확인 필요]`. Other labels (e.g., `[확인 필요]`) are not used.
3. WHEN validation-checklist.md handles inspection items, THE validation-checklist.md SHALL include all of the following areas:
   (a) Secrets Manager registration inventory (name / KMS / metadata, value query absolutely prohibited),
   (b) SSM Parameter Store registration inventory,
   (c) MarketConnector EC2 Instance Role / Instance Profile existence / attach status,
   (d) whether the Instance Role policy's Action / Resource matches this spec's design matrix,
   (e) verify 0 Resource wildcard usages,
   (f) verify the EC2 access key files (`~/.aws/credentials`, access key in `~/.aws/config`) do not exist,
   (g) verify the `aws sts get-caller-identity` result is the Instance Role assumed-role ARN,
   (h) whether the Connector smoke test passes.
4. WHEN validation-checklist.md handles a secret item, THE validation-checklist.md SHALL specify that the `secretsmanager:GetSecretValue` call is performed only by the operator and Kiro's automatic validation uses only `secretsmanager:DescribeSecret` metadata.
5. WHEN validation-checklist.md handles an IAM inspection item, THE validation-checklist.md SHALL separate items so that this spec design's Action / Resource matrix and the actual policy JSON are comparable.

### Requirement 9: Operation notes (operation-notes.md) template requirement

**Objective**: As an operator, I want to receive, in this spec's design / tasks stage, an operation-notes template that can accumulate execution results, so that actual secret values are never recorded and only the results are safely accumulated.

#### Acceptance Criteria

1. WHEN this spec's follow-up phase proceeds, THE follow-up phase SHALL create the `operation-notes.md` deliverable inside this spec folder.
2. WHEN operation-notes.md is authored, THE operation-notes.md SHALL never record the actual secret value, KIS app key / app secret, account number, RDS password, RDS endpoint hostname, account-id, actual secret ARN or IAM access key id, and denote all of them only as `[REDACTED]` or a placeholder.
3. WHEN operation-notes.md handles secret query results, THE operation-notes.md SHALL be a template that can record only "query success / failure", "last update time" and "the fact that the operator confirmed it directly", not the secret value itself.
4. WHEN operation-notes.md handles an IAM Role / Policy change, THE operation-notes.md SHALL be a template that can record the change date, changer, change reason and a before / after policy item summary (quoting the full JSON body is prohibited).
5. WHEN operation-notes.md is authored, THE operation-notes.md SHALL include in the body the work-division principle that actual AWS resource creation / modification / deletion is performed directly by the operator, and Kiro performs only document authoring / procedure organization / validation-item organization.
6. WHEN operation-notes.md is authored, THE operation-notes.md SHALL follow the per-date accumulative record format (`## YYYY-MM-DD ...`) of the 02 spec operation-notes as-is.

### Requirement 10: Common decision document update candidate — operator-decisions.md

**Objective**: As an operator, I want the decisions this spec locks and the new decision candidates it adds to be specified as update candidates in [`../_common/operator-decisions.md`](../_common/operator-decisions.md), so that follow-up specs (03 / 08 / 04 / 05 / 09 / 10) can safely use these decisions as input.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL specify the candidate decision value that locks OD-SEC-001 (Secrets storage location) from 🔴 미정 → 🟢 확정 or 🟡 잠정 in this spec (06).
2. WHEN design.md is authored, THE design.md SHALL specify the candidate decision value that locks OD-OBS-004 (Slack webhook storage location) from 🔴 미정 → 🟢 확정 or 🟡 잠정 in this spec (06).
3. WHEN design.md is authored, THE design.md SHALL specify, as a new decision candidate OD-SEC-XXX (e.g., OD-SEC-005), the "Access Key non-use principle for the MarketConnector EC2 / all 8 MS".
4. WHEN design.md is authored, THE design.md SHALL specify, as a new decision candidate OD-SEC-XXX (e.g., OD-SEC-006), the "EC2 / ECS IAM Role-based secret / parameter read principle (least privilege, Resource wildcard prohibited)".
5. WHERE OD-SEC-002 (RDS master password storage = Secrets Manager) and OD-SEC-003 (KIS access_token storage location) are consistent with this spec's decisions, THE design.md SHALL specify in one line each how the status of the two decisions should be updated in this spec (whether 잠정 → 확정, or kept as-is).
6. WHEN tasks.md is authored in a follow-up phase, THE tasks.md SHALL separate the [`../_common/operator-decisions.md`](../_common/operator-decisions.md) update work as a separate task and perform it only within this spec phase.
7. WHEN this spec's deliverables record a decision item, THE deliverable SHALL use the Status label English canonical values (CONFIRMED / TENTATIVE / TBD / DEFERRED) and the display-purpose Korean/color (🟢 확정 / 🟡 잠정 / 🔴 미정 / 🔵 보류) rules the same as the 02 spec.

### Requirement 11: Common follow-up document update candidate — followups-overview.md

**Objective**: As an operator, I want to receive update candidates that reflect this spec's (06) progress status in [`../_common/followups-overview.md`](../_common/followups-overview.md), so that at the entry point of other follow-up specs (03 / 08 / 04 / 05 / 09 / 10), it can be grasped at a glance how far 06 has progressed.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL specify update candidates that add, to the 06-secrets-and-iam section of [`../_common/followups-overview.md`](../_common/followups-overview.md),
   this spec's first application environment (`aws-paper`),
   first scope (MarketConnector EC2 Instance Role + Secrets Manager / SSM registration),
   and out-of-scope (live rotation automation, OIDC, full IAM matrix).
2. WHEN design.md is authored, THE design.md SHALL specify, as followups-overview.md update candidates, the items where this spec's decisions are used as input for the 03 / 08 / 04 / 05 / 09 specs (Task Role pattern, secret naming rule, Resource wildcard prohibition policy).
3. WHEN tasks.md is authored in a follow-up phase, THE tasks.md SHALL separate the [`../_common/followups-overview.md`](../_common/followups-overview.md) update work as a separate task.
4. WHEN handling the followups-overview.md update candidates, THE design.md SHALL specify in one line how 06 is grouped in order with 03 (03's full EC2 operation receives the secret / parameter / IAM skeleton that 06 locked first as input).

### Requirement 12: Common follow-up document update candidate — risk-register.md

**Objective**: As an operator, I want the risks this spec newly identifies / reinforces to be received as update candidates in [`../_common/risk-register.md`](../_common/risk-register.md), so that risks such as secret permission / EC2 environment-variable exposure / Access Key storage mistake / naming mismatch are tracked in the accumulative table.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL specify, as an update candidate, the new risk candidate `R-SEC-XXX` "Secrets Manager / SSM IAM over-permission (Resource wildcard, able to read even other services' secrets)".
2. WHEN design.md is authored, THE design.md SHALL specify, as an update candidate, the new risk candidate `R-SEC-XXX` "secret value exposed in plaintext in EC2 environment variables or process logs".
3. WHEN design.md is authored, THE design.md SHALL specify, as an update candidate, the new risk candidate `R-SEC-XXX` "operational mistake where an IAM access key id / secret access key file is stored inside EC2".
4. WHEN design.md is authored, THE design.md SHALL specify, as an update candidate, the new risk candidate `R-SEC-XXX` "read failure at application runtime or reading a wrong environment value due to Parameter / Secret naming mismatch".
5. WHEN handling a new risk candidate, THE design.md SHALL specify one line each of mitigation, detection and rollback for each risk (the same as the 02 spec risk-register column format).
6. WHEN tasks.md is authored in a follow-up phase, THE tasks.md SHALL separate the [`../_common/risk-register.md`](../_common/risk-register.md) update work as a separate task.

### Requirement 13: Follow-up spec reusability (03 / 08 first)

**Objective**: As an operator, I want 03-marketconnector-ec2 and 08-interest-crawler-and-preprocessor-ecs (future ECS porting) to reuse the same pattern (Instance Role / Task Role-based secret read) as-is, so that the same IAM / secret naming decision is not remade per spec.

#### Acceptance Criteria

1. WHEN design.md is authored, THE design.md SHALL specify the responsibility division that 03-marketconnector-ec2 receives this spec's Instance Role policy as-is as input and applies it to the EC2 formal operation (Connector / Flask / KIS API / RDS connection) stage.
2. WHEN design.md is authored, THE design.md SHALL specify the responsibility division that 08-interest-crawler-and-preprocessor-ecs receives this spec's Task Role skeleton as-is as input and applies it to the ECS Fargate Task Role (crawler-dedicated / preprocessor-dedicated).
3. WHEN design.md is authored, THE design.md SHALL specify that the 04 / 05 / 09 specs also use the same Task Role skeleton as input, but specify the division that filling in each spec's secret / parameter Resource ARN proceeds in that spec's phase.
4. WHEN design.md is authored, THE design.md SHALL specify in one line each the decisions that follow-up specs after this spec (06) must not change (naming rule, env prefix `paper` / `live`, Resource wildcard prohibition principle).

### Requirement 14: Safety constraints of this spec

**Objective**: As an operator, I want to explicitly restrict this spec's work so that it does not change code / operational data / AWS resources / external calls.

#### Acceptance Criteria

1. WHILE all phases of this spec are in progress, THE work SHALL not modify the README / AGENTS.md / CHANGELOG / docs / worklog and source code of the 8 MS.
2. WHILE all phases of this spec are in progress, THE work SHALL not create or change actual AWS resources (Secrets Manager secret, SSM Parameter, IAM Role, IAM Policy, IAM Instance Profile, EC2 attach change, etc.). The operator performs all actual creation / modification / deletion directly.
3. WHILE all phases of this spec are in progress, THE work SHALL not execute the 8 MS entrypoints, broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / orders / fills / Daily Batch / intraday monitor calls.
4. WHEN this spec's deliverables handle a secret, THE deliverable SHALL use only `[REDACTED]` in place of all actual secret value, password, KIS app key, KIS app secret, account number, token, webhook URL, access key id, secret access key, RDS endpoint hostname, account-id and actual secret ARN, and SHALL not write the actual values.
5. WHEN handling secret query, THE deliverable SHALL specify that the `secretsmanager:GetSecretValue` call is performed only by the operator and Kiro's automatic validation uses only `DescribeSecret` metadata.
6. WHEN this spec handles the separation policy with follow-up specs, THE deliverable SHALL specify that live rotation automation / CI/CD OIDC / full IAM matrix / aws-live environment IAM are out of this spec's scope.
7. WHILE this phase (requirements) is in progress, THE work SHALL not create deliverables other than `requirements.md` (`design.md`, `tasks.md`, `runbook.md`, `validation-checklist.md`, `operation-notes.md`) inside this spec folder. These are the responsibility of follow-up phases.
