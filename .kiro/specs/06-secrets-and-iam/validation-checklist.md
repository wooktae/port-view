# Validation Checklist — 06-secrets-and-iam

This document is the minimal edition that organizes the [확인] / completion criteria of [`./runbook.md`](./runbook.md) into a 4-label-based checklist.
This spec's (06) first application environment is `aws-paper`, the region is `ap-northeast-2`, and the first application target is the MarketConnector EC2.

Label rules (do not use labels other than those in this document):

- `[O]` pass / confirmed met.
- `[X]` not met or failure. Operator / Kiro follow-up action needed.
- `[Kiro 후속 작업 필요]` an item on hold because there is a deliverable that Kiro must additionally create or fill within this spec.
- `[운영자 확인 필요]` an item that only the operator can confirm directly on the AWS Console / EC2 shell. Not a Kiro automatic-validation target.

Security / safety principles

- The items below use only `[REDACTED]` or a placeholder. Do not record them in plaintext in this document.
  - secret value / KIS app key / KIS app secret / account number
  - RDS endpoint hostname / RDS password / account-id
  - actual secret ARN / IAM access key id / Slack webhook URL
- Only the operator issues an actual `secretsmanager:GetSecretValue` call. Kiro automatic validation is limited to `secretsmanager:DescribeSecret` metadata.
- The 8 MS code / README / AGENTS.md / CHANGELOG / docs / worklog unmodified.
- Actual AWS resources not created / not modified / not deleted. This checklist only inspects / records results.

## 1. Document creation status

- [O] [`./requirements.md`](./requirements.md) created
- [O] [`./README.md`](./README.md) created
- [O] [`./design.md`](./design.md) created
- [O] [`./tasks.md`](./tasks.md) created
- [O] [`./runbook.md`](./runbook.md) created
- [Kiro 후속 작업 필요] [`./operation-notes.md`](./operation-notes.md) creation planned

## 2. Secrets Manager

- [운영자 확인 필요] confirm `/portfolio/paper/marketconnector/kis-app-key` creation (including Description / Tag)
- [운영자 확인 필요] confirm `/portfolio/paper/marketconnector/kis-app-secret` creation
- [운영자 확인 필요] confirm `/portfolio/paper/marketconnector/paper-account` creation (JSON multi-key: `PAPER_ACNT`, `ACNT_PRDT_CD`)
- [운영자 확인 필요] confirm `/portfolio/paper/rds/marketconnector-app` creation (JSON multi-key: `host`, `port`, `dbname`, `username`, `password`)
- [운영자 확인 필요] confirm the existing `/portfolio/paper/rds/master` is retained (name / KMS / last modified time match the 02 spec point in time)
- [운영자 확인 필요] 0 additional registrations of other `/portfolio/paper/marketconnector/*` or `/portfolio/paper/rds/*` secrets beyond the 5 secrets above
- [운영자 확인 필요] confirm the secret value body is not recorded in plaintext in this document / runbook.md / operation-notes.md / console captures / operator notes

## 3. SSM Parameter Store

- [운영자 확인 필요] confirm `/portfolio/paper/marketconnector/kis-base-url` creation
- [운영자 확인 필요] confirm `/portfolio/paper/marketconnector/connector-host` creation
- [운영자 확인 필요] confirm `/portfolio/paper/marketconnector/connector-port` creation
- [운영자 확인 필요] confirm `/portfolio/paper/marketconnector/connector-debug` creation
- [운영자 확인 필요] confirm `/portfolio/paper/marketconnector/environment` creation (Value=`paper`)
- [운영자 확인 필요] confirm `/portfolio/paper/marketconnector/broker-name` creation
- [운영자 확인 필요] 0 additional registrations of other `/portfolio/paper/marketconnector/*` parameters beyond the 6 above
- [운영자 확인 필요] confirm the parameter names themselves do not contain an actual endpoint hostname / account number / secret value / password / token

## 4. IAM Role / Policy

- [운영자 확인 필요] confirm IAM Role `portfolio-paper-marketconnector-ec2-role` exists / Trust Policy `Service: ec2.amazonaws.com` + `sts:AssumeRole`
- [운영자 확인 필요] confirm Instance Profile `portfolio-paper-marketconnector-ec2-profile` exists / Role attach
- [운영자 확인 필요] confirm the above Instance Profile is attached to the EC2 instance (the ARN in the `describe-iam-instance-profile-associations` result matches)
- [운영자 확인 필요] Permission Policy allows only `secretsmanager:GetSecretValue`, `secretsmanager:DescribeSecret`. 0 other secretsmanager Actions
- [운영자 확인 필요] Permission Policy allows only `ssm:GetParameter`, `ssm:GetParameters`, `ssm:GetParametersByPath`. 0 other ssm Actions
- [운영자 확인 필요] 0 `"*"` in the Permission Policy Resource
- [운영자 확인 필요] 0 `Action: "*"` / `secretsmanager:*` / `ssm:*` in the Permission Policy
- [운영자 확인 필요] 0 other service prefixes in the Permission Policy Resource (the 5 below)
  - `/portfolio/paper/view/*`
  - `/portfolio/paper/crawler/*`
  - `/portfolio/paper/preprocessor/*`
  - `/portfolio/paper/strategy/*`
  - `/portfolio/paper/research/*`
- [운영자 확인 필요] 0 other environment prefix (`/portfolio/live/...`) in the Permission Policy Resource
- [운영자 확인 필요] the 4 secret ARNs / 1 parameter ARN prefix in the Permission Policy Resource match the design §4.2 matrix
- [운영자 확인 필요] 0 `kms:Decrypt` statements in the Policy when there is no CMK-use decision. Limit to that KMS Key ARN only when CMK use is decided

## 5. EC2 Access Key non-use

- [운영자 확인 필요] `~/.aws/credentials` does not exist (`test -f ~/.aws/credentials` → not found)
- [운영자 확인 필요] 0 `aws_access_key_id` / `aws_secret_access_key` lines inside `~/.aws/config`
- [운영자 확인 필요] the `AWS_ACCESS_KEY_ID` environment variable is unset in the current shell
- [운영자 확인 필요] the `AWS_SECRET_ACCESS_KEY` environment variable is unset in the current shell
- [운영자 확인 필요] the `AWS_SESSION_TOKEN` environment variable is unset in the current shell (the Instance Role credentials are handled automatically by the metadata service, so export is unnecessary)
- [운영자 확인 필요] 0 access-key-related export lines inside `~/.bashrc`, `~/.profile`, `~/.bash_profile`
- [운영자 확인 필요] 0 access keys inside the systemd unit's `Environment=` / `EnvironmentFile=`
- [운영자 확인 필요] 0 access keys inside the application `.env` / config files
- [운영자 확인 필요] the `aws sts get-caller-identity` result `Arn` is of the form `arn:aws:sts::<account-id>:assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>`
- [운영자 확인 필요] the `access_key` Source column of the `aws configure list` result is `iam-role` or `Ec2InstanceMetadata`

## 6. EC2 query validation

- [운영자 확인 필요] `aws secretsmanager describe-secret --secret-id /portfolio/paper/marketconnector/kis-app-key` succeeds on the EC2
- [운영자 확인 필요] `aws secretsmanager describe-secret` returns metadata normally for all 4 of this spec's secrets on the EC2
- [운영자 확인 필요] `aws ssm get-parameters-by-path --path /portfolio/paper/marketconnector` returns all 6 parameters on the EC2
- [운영자 확인 필요] querying another service prefix (`/portfolio/paper/view/*`, `/portfolio/paper/crawler/*`) on the EC2 gives AccessDenied or NotFound (privilege isolation normal)
- [운영자 확인 필요] 0 `secretsmanager:GetSecretValue` automatic-validation calls on the EC2 (called only in the operator's normal operation flow)
- [운영자 확인 필요] querying another environment prefix (`/portfolio/live/...`) on the EC2 gives AccessDenied (privilege isolation normal)

## 7. Connector smoke test (read-only only)

- [운영자 확인 필요] after running the temporary export script, the DB environment variables below are injected into the current shell (do not display values / record in logs)
  - `INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD`
- [운영자 확인 필요] after running the temporary export script, the KIS / configuration environment variables below are injected
  - `APP_KEY` / `APP_SECRET` / `BASE_URL` / `PAPER_ACNT` / `ACNT_PRDT_CD` / `PORT_ENVIRONMENT` / `PORT_BROKER_NAME`
- [운영자 확인 필요] the temporary export script / environment-variable values are not stored in plaintext in files / logs / console captures
- [운영자 확인 필요] RDS `marketconnector_app` connection success (no DDL/DML, query only)
- [운영자 확인 필요] KIS token issuance or reuse of the existing `access_token.txt` works normally
- [운영자 확인 필요] `connector_balance.py` balance query success
- [운영자 확인 필요] `connector_order_check.py` order / fill query success
- [운영자 확인 필요] Flask read-only endpoint smoke test passes (balance / holdings / order history, etc.)
- [운영자 확인 필요] 0 new-order / buy / sell / cancel / modify API calls during this validation
  - not-executed targets: `connector_buy.py`, `connector_sell.py`, `connector_cancel.py`, `connector_modify.py`

## 8. Completion criteria

- [운영자 확인 필요] 0 IAM access key files / environment variables / dotfiles / systemd EnvironmentFile / `.env` inside the EC2
- [운영자 확인 필요] the 4 secrets + 6 parameters of this spec are readable with Instance Role credentials alone
- [운영자 확인 필요] 0 Resource wildcard / Action wildcard / other service prefix / other environment prefix in the IAM Permission Policy
- [운영자 확인 필요] RDS `marketconnector_app` query + KIS read-only smoke test pass
- [O] 0 plaintext records of the items below anywhere in this document / `requirements.md` / `README.md` / `design.md` / `tasks.md` / `runbook.md`
  - secret value / KIS app key / KIS app secret / account number
  - RDS endpoint hostname / RDS password / account-id
  - actual secret ARN / IAM access key id / Slack webhook URL

## 9. Follow-up handover

- [Kiro 후속 작업 필요] Author a template in [`./operation-notes.md`](./operation-notes.md) to record this checklist's results (dated cumulative). Do not record secret values, record only success / failure.
- [Kiro 후속 작업 필요] Update [`../_common/operator-decisions.md`](../_common/operator-decisions.md) (upon operator approval)
  - update OD-SEC-001 / OD-OBS-004
  - reflect new OD-SEC-005 / OD-SEC-006 candidates
- [Kiro 후속 작업 필요] Register in [`../_common/risk-register.md`](../_common/risk-register.md) (upon operator approval)
  - 4 R-SEC candidates: excessive privilege / EC2 secret plaintext exposure / EC2 access key file / naming mismatch
  - register under the next available IDs
- [Kiro 후속 작업 필요] Update the 06 section of [`../_common/followups-overview.md`](../_common/followups-overview.md) (first application environment / first scope / out of scope / 03 handover).
- [운영자 확인 필요] Hand over this spec's decisions and this checklist's pass results as entry input to the 03-marketconnector-ec2 spec (Instance Role policy / Access Key non-use principle / Task Role skeleton).
