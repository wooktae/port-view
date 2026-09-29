# 06 Secrets and IAM — Operator Summary

This is a one-page summary the operator reads quickly every day. For details, see `requirements.md`. `design.md` / `tasks.md` / `runbook.md` / `validation-checklist.md` / `operation-notes.md` are created in follow-up phases. Decision-value changes are made only in `../_common/operator-decisions.md`.

## 1. Current Status

- `requirements.md` creation complete. `design.md` / `tasks.md` / `runbook.md` / `validation-checklist.md` / `operation-notes.md` are still follow-up phases.
- The first application environment is `aws-paper`, the region is `ap-northeast-2`, and the first application target is the MarketConnector EC2.
- The prerequisite spec `02-aws-network-and-rds` is first-application complete. VPC / Subnet / SG / VPC Endpoint (Secrets Manager / SSM / CloudWatch Logs / ECR / S3) / RDS PostgreSQL / 7 DB roles have been applied by direct operator work.
- The MarketConnector EC2 (Amazon Linux 2023, public subnet, EIP attach) is already created.
- On the morning of this spec's work, the operator passed KIS paper API query / RDS `marketconnector_app` connection / `connector_balance.py` / `connector_order_check.py` execution / Flask query-type endpoint smoke test based on shell environment variables.

## 2. Purpose of This Spec

- Never store an IAM access key id / secret access key inside the MarketConnector EC2.
- Read only the necessary values from Secrets Manager / SSM Parameter Store using the EC2 Instance Role credentials alone.
- Separate KIS / RDS sensitive information from code and documents.
- Leave a skeleton so the ECS Task Roles of the 03 / 08 / 04 / 05 / 09 specs can reuse the same pattern (Task Role-based secret / parameter read).

## 3. What to Do This Time

- Organize the Secrets Manager storage candidates for KIS app key / app secret / paper account-number-related values (`PAPER_ACNT`, `ACNT_PRDT_CD`).
- Organize the Secrets Manager storage candidates for the RDS `marketconnector_app` connection info (host / port / db name / user / password).
- Organize the SSM Parameter Store storage criteria for general settings such as KIS base URL / Connector Flask host / port / debug / `PORT_ENVIRONMENT` / `PORT_BROKER_NAME`.
- Design the MarketConnector EC2 Instance Role / Instance Profile least privilege (allowed actions: `secretsmanager:GetSecretValue`, `secretsmanager:DescribeSecret`, `ssm:GetParameter`, `ssm:GetParameters`, `ssm:GetParametersByPath`. The Resource is only the exact ARN or the `/portfolio/paper/marketconnector/*` prefix; `Resource: "*"` is prohibited).
- Codify the EC2 internal Access Key non-use principle (use only IMDSv2 + Instance Role credentials; prohibit long-lived access key files such as `~/.aws/credentials`).
- Provide a first recommendation of the naming rule `/portfolio/{env}/{service}/{item}` (env is `paper` / `live`, and the first-confirmed services in this spec are `marketconnector` / `rds`).

## 4. What Not to Do This Time

- live rotation automation (Lambda rotation, scheduled rotation).
- GitHub Actions OIDC / CI/CD Role design (07 spec).
- The full IAM matrix for all 8 MS (proceeds as pattern reuse in the 03 / 08 / 04 / 05 / 09 specs).
- aws-live IAM design (integrated into 10-cutover-and-validation-runbook).
- Creation / modification / deletion of actual AWS resources (Secrets Manager secret, SSM Parameter, IAM Role / Policy / Instance Profile, EC2 attach change). The operator performs all of these directly.
- Modifying the README / AGENTS.md / CHANGELOG / docs / worklog / source code of the 8 MS.

## 5. Operator Decision Candidates

- `OD-SEC-001` Secrets storage location — to be locked from 🔴 미정 → 🟢 확정 or 🟡 잠정 in this spec's design stage.
- `OD-OBS-004` Slack webhook storage location — to be finally locked in this spec after comparing Secrets Manager (recommended) vs SSM SecureString (cost-saving option).
- `OD-SEC-XXX` Access Key non-use principle for the MarketConnector EC2 / all 8 MS (new candidate).
- `OD-SEC-XXX` EC2 / ECS IAM Role-based secret / parameter read principle, Resource wildcard prohibited (new candidate).

## 6. Next Work

- Author `design.md` — classification criteria / naming rules / Instance Role·Task Role matrix / OD·R·follow-up spec division.
- Author `tasks.md` — decompose the _common document update work and the 03 / 08 input handoff work.
- Author `runbook.md` — Secrets / Parameter registration → Instance Role / Profile creation → attach → EC2 validation → Connector smoke test stages, with `[실행]` / `[확인]` / `[준비]` / `[복구]` labels.
- Author `validation-checklist.md` — the 4 labels `[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]`, verify 0 Resource wildcards, verify the EC2 access key file does not exist, verify the `aws sts get-caller-identity` result is the Instance Role assumed-role ARN.
- Author `operation-notes.md` — per-date accumulative records, actual secret value / account number / account-id / RDS endpoint hostname / actual secret ARN not recorded (all `[REDACTED]`), record only success/failure for secret query results.

## 7. One-Line Summary

The core of 06 is to first confirm the operational structure where the MarketConnector EC2 reads the KIS / RDS secrets and general setting values from Secrets Manager / SSM Parameter Store with least privilege, using only the Instance Role without an Access Key.

## 8. Related Document Links

### Documents the operator reads often

- [Requirements](./requirements.md)
- [Operator Decisions](../_common/operator-decisions.md)
- [Followups Overview](../_common/followups-overview.md)
- [Risk Register](../_common/risk-register.md)

### Documents to be created in follow-up phases

- `design.md`
- `tasks.md`
- `runbook.md`
- `validation-checklist.md`
- `operation-notes.md`

### Prerequisite specs

- [01 AWS Migration Foundation — Requirements](../01-aws-migration-foundation/requirements.md)
- [02 AWS Network and RDS — Operator Summary](../02-aws-network-and-rds/README.md)

This README does not write actual secret value, KIS app key / app secret, account number, RDS endpoint hostname, account-id, actual secret ARN or IAM access key id. All are shown only as `[REDACTED]` or a placeholder.
