# Kiro ReadOnly Validator IAM — Design

This document is a design spec for the IAM user / policy / validation procedure that lets Kiro validate — **read-only only** — the PORT-STRATEGY-AI Foundation (VPC / Subnet / Route Table / SG / VPC Endpoint / RDS / Secrets metadata) that the operator built directly in the AWS Console. This IAM neither creates nor modifies any resource, and never reads secret values.

This document does not record actual secret / password / access key / token / account-id / endpoint hostname values. It uses only `[REDACTED]` or placeholders.

## 1. Purpose and Scope

- Purpose
  - Enable Kiro to validate the Foundation results the operator built, using ReadOnly permissions, so it can update the results in [`../specs/02-aws-network-and-rds/validation-checklist.md`](../specs/02-aws-network-and-rds/validation-checklist.md).
  - Items such as operator decisions / cost / DB SQL / cutover / external exposure are hard to validate automatically. With these IAM permissions we handle only the items that can be validated automatically and leave the rest to manual operator confirmation.
- Scope
  - VPC, Subnet, Route Table, IGW, NAT Gateway, EC2 instance (for NAT-candidate inspection), Security Group, VPC Endpoint, RDS instance / subnet group / parameter group, Secrets Manager metadata, IAM inventory, CloudWatch / CloudWatch Logs metadata, Resource Groups Tagging.
- Out of scope
  - Resource creation / modification / deletion.
  - Secret value retrieval (`secretsmanager:GetSecretValue`).
  - Decrypt / KMS Decrypt / data-plane calls.
  - DB DDL/DML / SQL execution.
  - broker / KIS / Slack / external API calls.

## 2. Naming Conventions

- IAM User name: `portfolio-kiro-readonly-validator`
- IAM Policy name: `PortfolioKiroReadOnlyValidatorPolicy`
- (Optional) IAM Role name: `portfolio-kiro-readonly-validator-role` — when integrating with an external system / OIDC.
- Environment mapping: Start in aws-paper. When applying to aws-live, whether to add a `-live` suffix to the same name or split into a separate IAM User is left to the operator's decision.

## 3. Least-Privilege IAM Policy (JSON)

Grant only ReadOnly, exactly as the user-specified permission list. Write / modify / delete / `GetSecretValue` / data-plane are never included.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "Ec2NetworkReadOnly",
      "Effect": "Allow",
      "Action": [
        "ec2:Describe*",
        "ec2:Get*",
        "ec2:List*"
      ],
      "Resource": "*"
    },
    {
      "Sid": "RdsReadOnly",
      "Effect": "Allow",
      "Action": [
        "rds:Describe*",
        "rds:ListTagsForResource"
      ],
      "Resource": "*"
    },
    {
      "Sid": "SecretsManagerMetadataOnly",
      "Effect": "Allow",
      "Action": [
        "secretsmanager:ListSecrets",
        "secretsmanager:DescribeSecret",
        "secretsmanager:ListSecretVersionIds"
      ],
      "Resource": "*"
    },
    {
      "Sid": "IamReadOnly",
      "Effect": "Allow",
      "Action": [
        "iam:Get*",
        "iam:List*",
        "iam:GenerateCredentialReport",
        "iam:GenerateServiceLastAccessedDetails"
      ],
      "Resource": "*"
    },
    {
      "Sid": "CloudWatchReadOnly",
      "Effect": "Allow",
      "Action": [
        "cloudwatch:Describe*",
        "cloudwatch:List*",
        "cloudwatch:Get*"
      ],
      "Resource": "*"
    },
    {
      "Sid": "CloudWatchLogsReadOnly",
      "Effect": "Allow",
      "Action": [
        "logs:Describe*",
        "logs:ListTagsLogGroup",
        "logs:ListTagsForResource"
      ],
      "Resource": "*"
    },
    {
      "Sid": "TagReadOnly",
      "Effect": "Allow",
      "Action": [
        "tag:GetResources",
        "tag:GetTagKeys",
        "tag:GetTagValues"
      ],
      "Resource": "*"
    },
    {
      "Sid": "DenyAnyWrite",
      "Effect": "Deny",
      "Action": [
        "secretsmanager:GetSecretValue",
        "secretsmanager:GetSecret*",
        "kms:Decrypt",
        "kms:GenerateDataKey",
        "ec2:Create*",
        "ec2:Modify*",
        "ec2:Delete*",
        "ec2:Authorize*",
        "ec2:Revoke*",
        "ec2:Run*",
        "ec2:Start*",
        "ec2:Stop*",
        "ec2:Terminate*",
        "ec2:Associate*",
        "ec2:Disassociate*",
        "ec2:Attach*",
        "ec2:Detach*",
        "rds:Create*",
        "rds:Modify*",
        "rds:Delete*",
        "rds:Reboot*",
        "rds:Restore*",
        "iam:Create*",
        "iam:Update*",
        "iam:Delete*",
        "iam:Attach*",
        "iam:Detach*",
        "iam:Put*",
        "iam:Add*",
        "iam:Remove*",
        "logs:Put*",
        "logs:Create*",
        "logs:Delete*"
      ],
      "Resource": "*"
    }
  ]
}
```

Design notes

- The `Allow` block includes the ReadOnly permissions the user specified, as-is. `iam:GenerateCredentialReport` / `GenerateServiceLastAccessedDetails` are used only as ReadOnly reinforcements in the Get/List family.
- The final `DenyAnyWrite` block is an explicit deny to prevent the accident of privilege escalation should the operator attach an additional IAM policy. Because of the `Deny` precedence in AWS IAM, even if another attached policy grants a write permission, the actual call is blocked.
- The `secretsmanager:GetSecretValue` and `secretsmanager:GetSecret*` patterns are stated on the `Deny` side. This blocks the accident of a GetSecret API accidentally entering the ReadOnly policy.
- Data-plane calls (KMS Decrypt, etc.) are also explicitly blocked. This closes the path of decrypting a secret value by a workaround.

## 4. AWS Console Creation Procedure

The operator (the `portadmin` user from Step 0) performs this directly.

1. Console → IAM → Policies → Create policy → select JSON.
2. Paste the § 3 JSON above as-is.
3. Next → add Tags:
   - `env=paper`
   - `kind=readonly-validator`
   - `project=portfolio`
4. Policy name: `PortfolioKiroReadOnlyValidatorPolicy`. Record a short Description.
5. Create policy.
6. IAM → Users → Create user.
7. User name: `portfolio-kiro-readonly-validator`.
8. Provide user access to the AWS Management Console: disabled (no console sign-in allowed. CLI only).
9. Permissions options: `Attach policies directly` → select `PortfolioKiroReadOnlyValidatorPolicy` → Next → Tags → Create user.
10. Created user → Security credentials → Create access key:
    - Use case: `Command Line Interface (CLI)`.
    - After checking the Confirmation, Next → Description tag (e.g., `kiro-cli`) → Create access key.
    - The Access key / Secret access key must not be recorded in plaintext in this document / repo / notes (`[REDACTED]`).
11. (Optional) IAM → Users → `portfolio-kiro-readonly-validator` → Security credentials → MFA: generally not applied since there is no human use. However, per operator policy, a hardware key / Authenticator registration is possible.

## 5. AWS CLI Creation Procedure

The operator runs the following commands in order with `portadmin` credentials (Region is irrelevant since IAM is global).

```powershell
# 1) Policy 파일 준비 (위 § 3 JSON을 portfolio-kiro-readonly.json으로 저장)
aws iam create-policy `
  --policy-name PortfolioKiroReadOnlyValidatorPolicy `
  --policy-document file://portfolio-kiro-readonly.json `
  --tags Key=env,Value=paper Key=kind,Value=readonly-validator Key=project,Value=portfolio

# 2) IAM User 생성
aws iam create-user `
  --user-name portfolio-kiro-readonly-validator `
  --tags Key=env,Value=paper Key=kind,Value=readonly-validator Key=project,Value=portfolio

# 3) Policy attach (account-id는 운영자 직접 치환)
aws iam attach-user-policy `
  --user-name portfolio-kiro-readonly-validator `
  --policy-arn arn:aws:iam::[REDACTED-ACCOUNT-ID]:policy/PortfolioKiroReadOnlyValidatorPolicy

# 4) Access key 발급 (출력은 운영자만 보고 안전한 위치에 보관. repo / 본 문서에 적지 않는다)
aws iam create-access-key `
  --user-name portfolio-kiro-readonly-validator
```

Immediately after issuance, register the access key / secret access key in the environment Kiro will use (for example, `aws configure --profile portfolio-kiro-readonly`). After registration, remove the access key value from memory immediately and do not leave it in a plaintext file.

(Optional) To operate as an IAM Role, use the policy above as the Role's inline or attached policy, and restrict the trust policy's Principal to the external system's OIDC provider or portadmin. For operational simplicity, this spec puts the IAM User + access key approach as the first choice.

## 6. Validation Commands — AWS CLI ReadOnly

This section is the set of read-only AWS CLI commands Kiro calls during automatic validation. Each command is reproducible identically in the operator environment. Identifiers such as account-id, RDS endpoint hostname, and secret ARN are masked directly by the operator in the output.

```powershell
# Region 고정
$Env:AWS_REGION = "ap-northeast-2"
$Env:AWS_PROFILE = "portfolio-kiro-readonly"  # 운영자 정의

# 6-1. VPC
aws ec2 describe-vpcs `
  --filters "Name=tag:Name,Values=portfolio-vpc" `
  --query "Vpcs[].{VpcId:VpcId,Cidr:CidrBlock,State:State,Tags:Tags}" --output json

# 6-2. Subnet 6개
aws ec2 describe-subnets `
  --filters "Name=vpc-id,Values=<vpc-id>" `
  --query "Subnets[].{Name:Tags[?Key=='Name']|[0].Value,Cidr:CidrBlock,AZ:AvailabilityZone,MapPublicIpOnLaunch:MapPublicIpOnLaunch}" --output json

# 6-3. Internet Gateway
aws ec2 describe-internet-gateways `
  --filters "Name=attachment.vpc-id,Values=<vpc-id>" `
  --query "InternetGateways[].{IgwId:InternetGatewayId,Name:Tags[?Key=='Name']|[0].Value,Attachments:Attachments}" --output json

# 6-4. NAT Gateway 미생성
aws ec2 describe-nat-gateways `
  --filter "Name=vpc-id,Values=<vpc-id>" `
  --query "NatGateways[]" --output json

# 6-5. NAT 역할 EC2 후보 미생성 (VPC 내 EC2 일반 점검)
aws ec2 describe-instances `
  --filters "Name=vpc-id,Values=<vpc-id>" `
  --query "Reservations[].Instances[].{InstanceId:InstanceId,Name:Tags[?Key=='Name']|[0].Value,State:State.Name}" --output json

# 6-6. Route Table
aws ec2 describe-route-tables `
  --filters "Name=vpc-id,Values=<vpc-id>" `
  --query "RouteTables[].{Name:Tags[?Key=='Name']|[0].Value,Routes:Routes,Assoc:Associations}" --output json

# 6-7. Security Group 인벤토리
aws ec2 describe-security-groups `
  --filters "Name=vpc-id,Values=<vpc-id>" `
  --query "SecurityGroups[].{Name:GroupName,Id:GroupId}" --output json

# 6-8. RDS SG inbound 0/0:5432 점검 (결과는 비어 있어야 한다)
aws ec2 describe-security-groups `
  --filters "Name=vpc-id,Values=<vpc-id>" "Name=ip-permission.cidr,Values=0.0.0.0/0" "Name=ip-permission.from-port,Values=5432" `
  --query "SecurityGroups[].{Name:GroupName,Id:GroupId}" --output json

# 6-9. SSH 22 0/0 점검 (결과는 비어 있어야 한다)
aws ec2 describe-security-groups `
  --filters "Name=vpc-id,Values=<vpc-id>" "Name=ip-permission.cidr,Values=0.0.0.0/0" "Name=ip-permission.from-port,Values=22" `
  --query "SecurityGroups[].{Name:GroupName,Id:GroupId}" --output json

# 6-10. VPC Endpoint
aws ec2 describe-vpc-endpoints `
  --filters "Name=vpc-id,Values=<vpc-id>" `
  --query "VpcEndpoints[].{Name:Tags[?Key=='Name']|[0].Value,Service:ServiceName,Type:VpcEndpointType,State:State,PrivateDns:PrivateDnsEnabled,Subnets:SubnetIds,SGs:Groups[].GroupId,Routes:RouteTableIds}" --output json

# 6-11. RDS subnet group / parameter group / instance
aws rds describe-db-subnet-groups --db-subnet-group-name portfolio-paper-subnet-group --output json
aws rds describe-db-parameter-groups --db-parameter-group-name pg-portfolio-paper --output json
aws rds describe-db-parameters --db-parameter-group-name pg-portfolio-paper --source user --output json
aws rds describe-db-instances --db-instance-identifier portfolio-paper-rds --output json

# 6-12. Secrets Manager metadata (절대 GetSecretValue 호출 금지)
aws secretsmanager describe-secret --secret-id /portfolio/paper/rds/master `
  --query "{Name:Name,KmsKeyId:KmsKeyId,RotationEnabled:RotationEnabled,LastChangedDate:LastChangedDate,DeletedDate:DeletedDate,Tags:Tags}" --output json
```

## 7. Automatically Validatable / Manual Confirmation Classification

### 7-1. Automatically Validatable (Kiro validates via ReadOnly)

- VPC CIDR / state / tags
- Subnet: 6 names / CIDR / AZ / VPC mapping
- Internet Gateway attach status
- NAT Gateway not created
- NAT-role EC2 instance not created
- Route Table routes (0.0.0.0/0 → IGW for `rt-public`, no external route for `rt-app`/`rt-data`) / subnet association
- Security Group inbound / outbound rules, check for absence of SSH 22 / 5432 0/0
- VPC Endpoint state / subnet / private DNS / SG / route table association
- RDS Subnet Group status / member subnets / VPC
- RDS Parameter Group family / user-set parameter values
- RDS instance status / engine version / class / storage / public access / backup retention / deletion protection / encryption / endpoint existence / SG / parameter group applied
- Secret `/portfolio/paper/rds/master` existence and metadata (KmsKeyId, RotationEnabled, Tags, etc.) — the value is never retrieved

### 7-2. Manual Confirmation Required (Operator Directly)

- Whether operator approval was given (approval required task)
- Whether a secret was exposed in plaintext in this document / captures / operator notes (external file inspection required)
- Awareness of [`../specs/_common/operator-decisions.md`](../specs/_common/operator-decisions.md) / [`../specs/_common/risk-register.md`](../specs/_common/risk-register.md) (human reading)
- The actual billed amount in the AWS Billing Dashboard (readable, but the cost-profile line decision is a human judgment)
- Whether a Cost Anomaly Detection alert is registered, and the operator decision
- Whether DB / schema / role SQL was executed (at the time of 03 / 06 spec progress)
- Whether pg_dump / pg_restore was agreed (operator notes)
- Whether a Rollback was performed and its reason (human decision)
- Public document / repo exposure judgment

## 8. Rules for Reflection into validation-checklist

- Item clearly confirmed via the AWS API → `<span style="color:red">[O]</span>`
- Item not confirmable via the AWS API or requiring operator judgment → `<span style="color:black">[확인 필요]</span> — manual confirmation needed`
- Item that failed validation or does not match the expected value → `<span style="color:blue">[X]</span> — mismatch: actual = ..., expected = ...`
- Expected values / decision values / secret values are not changed arbitrarily even in this document.

## 9. Security / Safety Constraints

- This IAM User / Role / Access Key is ReadOnly. No resource-modifying call occurs.
- `secretsmanager:GetSecretValue` is not in the policy Allow and is explicitly stated in Deny.
- KMS `Decrypt` / `GenerateDataKey` are Deny.
- The access key is not recorded anywhere in this document / repo / plaintext file (`[REDACTED]`).
- Region is fixed to `ap-northeast-2`.
- If an access key exposure is suspected, reissue via `aws iam delete-access-key` then `create-access-key`. Do not record the new value in this document.

## 10. Change / Revocation Procedure

- When automatic validation is no longer needed
  1. Deactivate the access key: `aws iam update-access-key --user-name portfolio-kiro-readonly-validator --access-key-id [REDACTED] --status Inactive`
  2. Delete after a monitoring period: `aws iam delete-access-key`
  3. Policy detach + delete Policy + delete User (if needed).
- If insufficient permissions are found, add only ReadOnly items to Allow. Never add Write / Decrypt items.
