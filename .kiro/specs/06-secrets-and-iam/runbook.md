# Runbook — 06-secrets-and-iam

This document is an execution procedure that the operator can follow directly, in order, on the AWS Console / EC2 shell.
The first application environment is `aws-paper`, the region is `ap-northeast-2`, and the first application target is the MarketConnector EC2.
`aws-live` is the integrated responsibility of [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook) (planned).

This runbook does not automatically create actual AWS resources. The operator clicks / inputs directly one step at a time, and proceeds to the next Step after passing the [확인] of each Step.

Prerequisite inputs

- [`./requirements.md`](./requirements.md), [`./README.md`](./README.md), [`./design.md`](./design.md), [`./tasks.md`](./tasks.md)
- [`../02-aws-network-and-rds/runbook.md`](../02-aws-network-and-rds/runbook.md)
- [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md)
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/risk-register.md`](../_common/risk-register.md)

Security / safety principles

- The items below use only `[REDACTED]` or a placeholder. Do not write them in plaintext anywhere in this document / console captures / operator notes.
  - secret value / KIS app key / KIS app secret / account number
  - RDS endpoint hostname / RDS password / account-id
  - actual secret ARN / IAM access key id / Slack webhook URL
- Do not modify the 8 MS source code / README / AGENTS.md / CHANGELOG / docs / worklog.
- Do not call broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / order / fill / Daily Batch / intraday monitor. This runbook proceeds only up to the read-only smoke test.
- Only the operator issues an actual `secretsmanager:GetSecretValue` call. Kiro automatic validation is limited to `secretsmanager:DescribeSecret` metadata.

## Step labels

- `[실행]` a step that creates / changes a resource or actually executes a command on the AWS Console / EC2 shell.
- `[확인]` a step that confirms only state / result without creating a resource.
- `[준비]` a step that only agrees / records before entering follow-up work (validation-checklist.md / operation-notes.md / 03 spec).
- `[복구]` a step performed only at the point of validation failure / incident / rollback.

## 1. Pre-check

### 1-1. Confirm environment / target [확인]

1. In the top-right of the AWS Console, confirm the region is `ap-northeast-2` (Seoul).
2. Confirm that the account alias or account-id belongs to the aws-paper environment. Record the account-id only in the operator notes (in this document, `[REDACTED]`).
3. In EC2 → Instances, confirm the MarketConnector EC2 exists. Record the instance ID only in the operator notes (in this document, the `<instance-id>` placeholder).
4. Confirm the EC2 state is `running` and an EIP is attached.

### 1-2. Current EC2 credential state [확인]

While connected to the EC2:

```text
aws sts get-caller-identity
aws configure list
ls -la ~/.aws 2>/dev/null
```

- If a `~/.aws/credentials` file exists, go to Step 7-5 ([복구]) and then re-run this runbook from the beginning. At this spec's point in time, no long-lived access key should remain inside the EC2.
- If the `Arn` in the `aws sts get-caller-identity` result is of the `user/...` form rather than the `assumed-role/...` form, it means the EC2 is operating with IAM user credentials. Go to Step 7-5.

### 1-3. Existing RDS master secret compatibility [확인]

1. In the Secrets Manager → Secrets list, confirm that `/portfolio/paper/rds/master` exists.
2. This runbook keeps this secret as is. Do not create / delete / rename it.

### 1-4. Awareness of the temporary operation mode [준비]

The shell `export`-based environment-variable injection method used in the morning validation is a temporary operation mode.
After this runbook completes, transition to the normal operation mode (injection via Secrets Manager / SSM Parameter Store).
See [`./design.md`](./design.md) §3.4 for the detailed definition.

## 2. Secrets Manager registration

Cost impact: a monthly unit price is incurred per secret (small amount). This step will newly register 4 entries.
Assumes KMS uses the AWS managed default key (`aws/secretsmanager`).
When using a CMK, add the KmsDecrypt condition of [`./design.md`](./design.md) §4.2.

### 2-1. Create KIS app key secret [실행]

1. Secrets Manager → `Store a new secret`.
2. Secret type: `Other type of secret`.
3. The operator enters the KIS app key value directly in the Console at Key/value or Plaintext. In this document, only `[REDACTED]`.
4. Encryption key: `aws/secretsmanager` (default).
5. Secret name: `/portfolio/paper/marketconnector/kis-app-key`.
6. Description: `MarketConnector KIS app key (paper). Value redacted.`
7. Tags (recommended): `env=paper`, `service=marketconnector`, `kind=kis-app-key`, `project=portfolio`.
8. Rotation: out of scope for this spec. `Disable automatic rotation`.
9. Review → Store. Record the resulting ARN in the operator notes with the placeholder below.
   - `arn:aws:secretsmanager:<region>:<account-id>:secret:/portfolio/paper/marketconnector/kis-app-key-XXXXXX`

### 2-2. Create KIS app secret secret [실행]

- Same order as 2-1. Only the differences:
  - Secret name: `/portfolio/paper/marketconnector/kis-app-secret`.
  - Tags: `kind=kis-app-secret`.
  - Value: KIS app secret (entered directly by the operator, `[REDACTED]` in this document).

### 2-3. Create KIS paper account secret [실행]

1. Secret type: `Other type of secret` → select Key/value (JSON multi-key).
2. Add keys:
   - `PAPER_ACNT` = `[REDACTED]` (entered directly by the operator)
   - `ACNT_PRDT_CD` = `[REDACTED]` (entered directly by the operator)
3. Secret name: `/portfolio/paper/marketconnector/paper-account`.
4. Tags: `kind=paper-account`.
5. Do not write the actual account number / product code in plaintext in this document / operator notes / console captures.

### 2-4. Create RDS marketconnector_app secret [실행]

1. Secret type: `Other type of secret` → Key/value (JSON multi-key).
2. Add keys (values entered directly by the operator, `[REDACTED]` in this document):
   - `host`
   - `port`
   - `dbname`
   - `username`
   - `password`
3. Secret name: `/portfolio/paper/rds/marketconnector-app`.
4. Tags: `env=paper`, `service=rds`, `kind=marketconnector-app`, `project=portfolio`.
5. Do not write the actual RDS endpoint hostname / password in this document.

### 2-5. Registration result [확인]

1. In the Secrets Manager → Secrets list, confirm the following 4 entries exist:
   - `/portfolio/paper/marketconnector/kis-app-key`
   - `/portfolio/paper/marketconnector/kis-app-secret`
   - `/portfolio/paper/marketconnector/paper-account`
   - `/portfolio/paper/rds/marketconnector-app`
2. Confirm the existing `/portfolio/paper/rds/master` is kept as is.
3. Confirm each secret's Description / Tag matches [`./design.md`](./design.md) §1.1 / §2.4.

## 3. SSM Parameter Store registration

Cost impact: Standard tier (4KB or less) parameters are free. This step will register 6 entries.

### 3-1. Create KIS base URL parameter [실행]

1. Systems Manager → Parameter Store → `Create parameter`.
2. Name: `/portfolio/paper/marketconnector/kis-base-url`.
3. Tier: `Standard`.
4. Type: `String` (plaintext).
5. Value: KIS paper endpoint URL (entered directly by the operator, e.g., `https://openapivts.koreainvestment.com:29443`).
6. Tags: `env=paper`, `service=marketconnector`, `kind=kis-base-url`, `project=portfolio`.
7. Create parameter.

### 3-2. Create Connector Flask host parameter [실행]

- Same as 3-1. Only the differences:
  - Name: `/portfolio/paper/marketconnector/connector-host`.
  - Type: `String`.
  - Value: Connector Flask bind host (entered directly by the operator).

### 3-3. Create Connector Flask port parameter [실행]

- Name: `/portfolio/paper/marketconnector/connector-port`.
- Type: `String` or `String`+numeric notation. Value: Connector Flask port (entered directly by the operator).

### 3-4. Create Connector debug parameter [실행]

- Name: `/portfolio/paper/marketconnector/connector-debug`.
- Type: `String`. Value: `true` / `false`, decided by the operator.

### 3-5. Create environment / broker-name parameters [실행]

1. `/portfolio/paper/marketconnector/environment` — Value: `paper`.
2. `/portfolio/paper/marketconnector/broker-name` — Value: `kis-paper` or an operator-decided identifier.

### 3-6. Registration result [확인]

1. In the Parameter Store list, confirm 6 entries with the `/portfolio/paper/marketconnector/*` prefix.
2. Each parameter's Tier / Type / Tag matches [`./design.md`](./design.md) §1.2 / §2.4.
3. Confirm that no parameter name contains an actual endpoint hostname / account number / secret value. Non-inclusion is normal.

## 4. IAM Role / Policy preparation

### 4-1. Confirm or create the IAM Role [실행]

1. IAM → Roles → search: `portfolio-paper-marketconnector-ec2-role`.
2. If it exists, open the Trust Policy and confirm the Principal is `Service: ec2.amazonaws.com` and the Action is `sts:AssumeRole`.
3. If it does not exist, `Create role`:
   - Trusted entity type: `AWS service`.
   - Use case: `EC2`.
   - Role name: `portfolio-paper-marketconnector-ec2-role`.
   - Description: `MarketConnector EC2 instance role for paper. Read-only Secrets/Parameters.`
   - Tags: `env=paper`, `service=marketconnector`, `kind=ec2-role`, `project=portfolio`.

### 4-2. Confirm or create the Instance Profile [실행]

- On the AWS Console, when you create an EC2 Role, a same-named Instance Profile is often auto-created. In IAM → Roles → the Role details, confirm the Instance Profile ARN exists.
- If it does not exist, the operator creates it with the CLI:
  ```text
  aws iam create-instance-profile --instance-profile-name portfolio-paper-marketconnector-ec2-profile
  aws iam add-role-to-instance-profile \
    --instance-profile-name portfolio-paper-marketconnector-ec2-profile \
    --role-name portfolio-paper-marketconnector-ec2-role
  ```

### 4-3. Author the minimal read policy [실행]

1. IAM → Policies → `Create policy` → JSON tab.
2. Configure the Statement (exactly as the [`./design.md`](./design.md) §4.2 matrix). Use only placeholders for Resource, and the operator replaces them with the actual ARNs in the Console:

   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Sid": "SecretsManagerRead",
         "Effect": "Allow",
         "Action": [
           "secretsmanager:GetSecretValue",
           "secretsmanager:DescribeSecret"
         ],
         "Resource": [
           "arn:aws:secretsmanager:<region>:<account-id>:secret:/portfolio/paper/marketconnector/kis-app-key-*",
           "arn:aws:secretsmanager:<region>:<account-id>:secret:/portfolio/paper/marketconnector/kis-app-secret-*",
           "arn:aws:secretsmanager:<region>:<account-id>:secret:/portfolio/paper/marketconnector/paper-account-*",
           "arn:aws:secretsmanager:<region>:<account-id>:secret:/portfolio/paper/rds/marketconnector-app-*"
         ]
       },
       {
         "Sid": "SsmParameterRead",
         "Effect": "Allow",
         "Action": [
           "ssm:GetParameter",
           "ssm:GetParameters",
           "ssm:GetParametersByPath"
         ],
         "Resource": [
           "arn:aws:ssm:<region>:<account-id>:parameter/portfolio/paper/marketconnector/*"
         ]
       }
     ]
   }
   ```

3. Add a KmsDecrypt statement only when CMK use is decided ([`./design.md`](./design.md) §4.2). Do not add this statement when using the AWS managed `aws/secretsmanager`.
4. Policy name: `portfolio-paper-marketconnector-ec2-readonly`.
5. Description: `Read-only Secrets/Parameters for MarketConnector EC2 (paper). No wildcards.`
6. Review once more that Resource does not contain `*` or another service prefix, then Create.

### 4-4. policy attach [실행]

1. IAM → Roles → `portfolio-paper-marketconnector-ec2-role` → Permissions → `Add permissions` → `Attach policies`.
2. Select `portfolio-paper-marketconnector-ec2-readonly` and attach.
3. Whether to attach auxiliary managed policies such as `AmazonSSMManagedInstanceCore` for SSM Session Manager access is the 03 spec's responsibility. Out of scope for this runbook.

### 4-5. Attach the Instance Profile to the EC2 instance [실행]

1. EC2 → Instances → select the MarketConnector EC2 → Actions → Security → Modify IAM role.
2. Select IAM role: `portfolio-paper-marketconnector-ec2-profile` and Update.
3. Right after attach, it may take several seconds to several minutes for the credential cache to refresh. There is no need to restart the EC2.

Cost impact: no cost for the IAM Role / Policy / Instance Profile themselves.

## 5. EC2 validation

### 5-1. EC2 connection [확인]

- Connect to the MarketConnector EC2 via SSH or SSM Session Manager (the SSM Session Manager privilege is to be attached in the 03 spec. Connect via SSH at this point in time).

### 5-2. Confirm assumed-role [확인]

```text
aws sts get-caller-identity
```

- Normal if the `Arn` in the result is of the form `arn:aws:sts::<account-id>:assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>`.
- If it is of the `user/...` or `iam-user/...` form, go to Step 7-5 ([복구]).
- The actual account-id / instance-id go only into the operator notes. In this document, the `<account-id>` / `<instance-id>` placeholders.

### 5-3. Confirm credential source [확인]

```text
aws configure list
```

- Confirm the Source column of `access_key` is `iam-role` or `Ec2InstanceMetadata`.
- If the Source is `env`, `shared-credentials-file`, or `config-file`, go to Step 7-5 ([복구]).

### 5-4. Access key file non-existence [확인]

```text
ls -la ~/.aws 2>/dev/null
test -f ~/.aws/credentials && echo "FOUND" || echo "OK"
grep -E "^aws_access_key_id|^aws_secret_access_key" ~/.aws/credentials ~/.aws/config 2>/dev/null
```

- Non-existence of `~/.aws/credentials` is normal. If it exists, Step 7-5.
- 0 grep results is normal.

### 5-5. Query Secrets Manager metadata [확인]

```text
aws secretsmanager describe-secret \
  --secret-id /portfolio/paper/marketconnector/kis-app-key \
  --region ap-northeast-2
```

- Using the same pattern, confirm all 4 entries return metadata (name / KMS / last modified time) normally.
- Do not call `secretsmanager:GetSecretValue` during the validation step. The operator calls it only at the normal operation (Step 6) point in time.

### 5-6. Query SSM Parameter [확인]

```text
aws ssm get-parameters-by-path \
  --path /portfolio/paper/marketconnector \
  --region ap-northeast-2
```

- Confirm the 6 `/portfolio/paper/marketconnector/*` entries are returned.
- AccessDenied is normal for queries to another service prefix (`/portfolio/paper/view/...`, `/portfolio/paper/crawler/...`). Confirm privilege isolation with the following command:
  ```text
  aws ssm get-parameter --name /portfolio/paper/view/dummy --region ap-northeast-2 || echo "DENIED OR NOT_FOUND (OK)"
  ```

## 6. Connector smoke test (read-only only)

The goal of this step is to confirm that the existing read-only flow passes as is with Instance Role credentials + secret/parameter injection. Never make a new-order / buy / sell / cancel / modify call.

### 6-1. Temporary environment-variable injection script [실행]

1. In the operator home on the EC2 or a directory such as `/opt/portfolio/marketconnector/`,
   create a temporary export script (e.g., `/tmp/inject-env.sh`).
   The script injects secret/parameter values into environment variables
   (do not write secret values in plaintext in the file — the pattern below only does in-memory export).
2. Script pattern (placeholders only):
   ```bash
   #!/usr/bin/env bash
   set -euo pipefail
   REGION=ap-northeast-2

   # Parameters (평문 가능)
   export BASE_URL=$(aws ssm get-parameter --region "$REGION" --name /portfolio/paper/marketconnector/kis-base-url --query 'Parameter.Value' --output text)
   export PORT_ENVIRONMENT=$(aws ssm get-parameter --region "$REGION" --name /portfolio/paper/marketconnector/environment --query 'Parameter.Value' --output text)
   export PORT_BROKER_NAME=$(aws ssm get-parameter --region "$REGION" --name /portfolio/paper/marketconnector/broker-name --query 'Parameter.Value' --output text)

   # Secrets (단일값)
   export APP_KEY=$(aws secretsmanager get-secret-value --region "$REGION" --secret-id /portfolio/paper/marketconnector/kis-app-key --query 'SecretString' --output text)
   export APP_SECRET=$(aws secretsmanager get-secret-value --region "$REGION" --secret-id /portfolio/paper/marketconnector/kis-app-secret --query 'SecretString' --output text)

   # Secrets (JSON multi-key)
   ACCT_JSON=$(aws secretsmanager get-secret-value --region "$REGION" --secret-id /portfolio/paper/marketconnector/paper-account --query 'SecretString' --output text)
   export PAPER_ACNT=$(echo "$ACCT_JSON" | jq -r '.PAPER_ACNT')
   export ACNT_PRDT_CD=$(echo "$ACCT_JSON" | jq -r '.ACNT_PRDT_CD')

   DB_JSON=$(aws secretsmanager get-secret-value --region "$REGION" --secret-id /portfolio/paper/rds/marketconnector-app --query 'SecretString' --output text)
   export INTEREST_DB_HOST=$(echo "$DB_JSON" | jq -r '.host')
   export INTEREST_DB_PORT=$(echo "$DB_JSON" | jq -r '.port')
   export INTEREST_DB_NAME=$(echo "$DB_JSON" | jq -r '.dbname')
   export INTEREST_DB_USER=$(echo "$DB_JSON" | jq -r '.username')
   export INTEREST_DB_PASSWORD=$(echo "$DB_JSON" | jq -r '.password')
   ```
3. Script permission: `chmod 700 /tmp/inject-env.sh`. Run with `source /tmp/inject-env.sh`.
4. The script itself does not contain secret values. The environment variables are exported only within the current shell and are not saved to the file.

### 6-2. Confirm RDS connection [확인]

- Confirm only the `marketconnector_app` user connection with `psql` or the existing operation command. No DDL/DML.

### 6-3. Confirm KIS token issuance / reuse [확인]

- Confirm only whether the `port-marketconnector` token manager behavior reads the access_token file or leads to a new issuance. Do not print the token value in plaintext.

### 6-4. Read-only entrypoint smoke test [확인]

- Run `connector_balance.py` → confirm the balance-query result is returned.
- Run `connector_order_check.py` → confirm the order/fill query result is returned.
- Confirm the Flask read-only endpoint smoke test (balance / holdings / order history query) passes.

### 6-5. Prohibit new-order API calls [확인]

- Do not run `connector_buy.py`, `connector_sell.py`, `connector_cancel.py`, `connector_modify.py`.
- In this runbook's scope, buy / sell / cancel / modify calls must be 0.

### 6-6. Transition to the normal operation mode [준비]

- Once the temporary export script validation is done, the transition to the normal operation mode is handed over to the 03 spec (MarketConnector EC2 operation) responsibility.
  - normal operation mode = systemd unit + Instance Role-based startup script.
  - This runbook goes only up to temporary validation.

## 7. Inspection / recovery on failure

### 7-1. secret name typo [복구]

- If AccessDenied or NotFound occurs, confirm the secret name exactly.
  - Format: `/portfolio/paper/marketconnector/kis-app-key`.
  - All 4 entries must match the [`./design.md`](./design.md) §2.4 table.
- If the name is wrong, do not delete the secret; handle it with one of the following.
  - Match the Resource ARN of the IAM policy, or
  - Recreate the secret with the exact name
- Handle a wrongly named secret by automatic deletion after the 30-day recovery period or by the operator's explicit deletion
  (do not use `force-delete-without-recovery`).

### 7-2. IAM policy Resource scope [복구]

- If AccessDenied occurs, confirm the policy's Resource ARN matches this spec's naming (`/portfolio/paper/marketconnector/*`, `/portfolio/paper/rds/marketconnector-app*`).
- If Resource contains `*` or another service prefix (`/portfolio/paper/view/*`), detach it immediately and rewrite it per the §4-3 matrix of this runbook.

### 7-3. Instance Profile attach state [복구]

```text
aws ec2 describe-iam-instance-profile-associations \
  --filters Name=instance-id,Values=<instance-id> \
  --region ap-northeast-2
```

- Confirm the `IamInstanceProfile.Arn` in the result is of the `portfolio-paper-marketconnector-ec2-profile` form.
- If another Profile is attached, replace it in EC2 → Modify IAM role.

### 7-4. VPC Endpoint state [복구]

- Confirm the Secrets Manager / SSM Interface Endpoint is connected to the EC2's subnet and the SG allows inbound 443 of the EC2 SG. See the VPC Endpoint Step of the 02 spec runbook.
- Confirm the DNS response returns the endpoint private IP (`nslookup secretsmanager.ap-northeast-2.amazonaws.com`).

### 7-5. Access Key environment-variable / file contamination [복구]

```text
unset AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY AWS_SESSION_TOKEN
test -f ~/.aws/credentials && mv ~/.aws/credentials ~/.aws/credentials.bak.$(date +%s)
test -f ~/.aws/config && grep -E "^aws_access_key_id|^aws_secret_access_key" ~/.aws/config
```

- Back up and move `~/.aws/credentials` (instead of deleting). Then re-validate §1-2 / §5-2 / §5-3 in a new shell.
- If an access key that someone exported is found, revoke it immediately in the IAM Console (`Make inactive` → `Delete`).

### 7-6. shell environment-variable temporary method rollback [복구]

- If secret/parameter injection fails, temporarily fall back to the shell `export` direct injection method used in the morning (temporary operation mode) and continue operation.
- However, this is a temporary mode; after resolving the defect in this runbook, transition back to the normal operation mode. See [`./design.md`](./design.md) §3.4.

## 8. Completion criteria [확인]

- [확인] `~/.aws/credentials` does not exist inside the EC2. 0 `aws_access_key_id` / `aws_secret_access_key` patterns.
- [확인] The `aws sts get-caller-identity` result is of the `assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>` form.
- [확인] The access_key Source of `aws configure list` is `iam-role` or `Ec2InstanceMetadata`.
- [확인] Secrets Manager 4 entries (`kis-app-key` / `kis-app-secret` / `paper-account` / `marketconnector-app`) + existing `rds/master` retained. Describe possible.
- [확인] SSM Parameter 6 entries (`kis-base-url` / `connector-host` / `connector-port` / `connector-debug` / `environment` / `broker-name`) Get possible.
- [확인] 0 `*` or other service prefix in the IAM policy's Resource. 0 `secretsmanager:*` / `ssm:*` / `*` in Action.
- [확인] Connector read-only smoke test passes (`connector_balance.py`, `connector_order_check.py`, Flask read-only endpoint). 0 new-order / buy / sell / cancel / modify calls.
- [확인] None of the items below are recorded in plaintext anywhere in this runbook. All use `[REDACTED]` or a placeholder.
  - secret value / KIS app key / KIS app secret / account number
  - RDS endpoint hostname / RDS password / account-id
  - actual secret ARN / IAM access key id / Slack webhook URL

## 9. Follow-up handover [준비]

- [`./validation-checklist.md`](./validation-checklist.md): itemize the [확인] results of this runbook into inspection items with the 4 labels (`[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]`).
- [`./operation-notes.md`](./operation-notes.md): cumulatively record the date this runbook was performed.
  Leave only the secret/parameter registration result (success/failure only) and the IAM Role / Policy change summary (before/after item summary).
  Do not record the actual secret value / account-id / RDS endpoint / actual ARN.
- [`../03-marketconnector-ec2`](../03-marketconnector-ec2) (planned): 03 spec responsibility items.
  Take this runbook's Instance Role policy as is as input.
  - `AmazonSSMManagedInstanceCore` attach for SSM Session Manager access
  - CloudWatch Logs write privilege
  - systemd-based normal operation mode transition
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md): reflect upon operator approval.
  See [`./design.md`](./design.md) §7 for the detailed candidate values.
  - OD-SEC-001 / OD-OBS-004 update candidates
  - new OD-SEC-005 / OD-SEC-006 candidates
- [`../_common/risk-register.md`](../_common/risk-register.md): register under the next available IDs upon operator approval.
  See [`./design.md`](./design.md) §8 for the detailed candidate values.
  - 4 R-SEC candidates: excessive privilege / EC2 secret plaintext exposure / EC2 access key file / naming mismatch
