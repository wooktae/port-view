# Kiro ReadOnly Validator IAM — Design

본 문서는 운영자가 AWS Console에서 직접 구축한 PORT-STRATEGY-AI Foundation(VPC / Subnet / Route Table / SG / VPC Endpoint / RDS / Secrets metadata)을 Kiro가 **읽기 전용으로만** 검증할 수 있도록 만드는 IAM 사용자 / 정책 / 검증 절차 설계서다. 본 IAM은 어떤 리소스도 만들거나 변경하지 않고, secret 값은 절대 조회하지 않는다.

본 문서에는 실제 secret / password / access key / token / account-id / endpoint hostname 값을 적지 않는다. 모두 `[REDACTED]` 또는 placeholder만 사용한다.

## 1. 목적과 범위

- 목적
  - Kiro가 운영자가 만든 Foundation 결과를 ReadOnly 권한으로 검증해 [`../specs/02-aws-network-and-rds/validation-checklist.md`](../specs/02-aws-network-and-rds/validation-checklist.md)에 결과를 갱신할 수 있도록 한다.
  - 운영자 결정 / 비용 / DB SQL / cutover / 외부 노출 같은 항목은 자동 검증이 어렵다. 본 IAM 권한으로는 자동 검증 가능한 항목만 다루고, 나머지는 운영자 수동 확인으로 남긴다.
- 범위
  - VPC, Subnet, Route Table, IGW, NAT Gateway, EC2 instance(NAT 후보 점검 용도), Security Group, VPC Endpoint, RDS instance / subnet group / parameter group, Secrets Manager metadata, IAM 인벤토리, CloudWatch / CloudWatch Logs metadata, Resource Groups Tagging.
- 범위 밖
  - 리소스 생성 / 변경 / 삭제.
  - Secret value 조회(`secretsmanager:GetSecretValue`).
  - Decrypt / KMS Decrypt / 데이터 plane 호출.
  - DB DDL/DML / SQL 실행.
  - broker / KIS / Slack / 외부 API 호출.

## 2. 명명 규칙

- IAM User 이름: `portfolio-kiro-readonly-validator`
- IAM Policy 이름: `PortfolioKiroReadOnlyValidatorPolicy`
- (옵션) IAM Role 이름: `portfolio-kiro-readonly-validator-role` — 외부 system / OIDC 연동 시.
- 환경 매핑: aws-paper에서 시작. aws-live 적용 시 동일 이름에 `-live` suffix를 추가하거나 별도 IAM User로 분리하는 것을 운영자 결정으로 둔다.

## 3. 최소 권한 IAM Policy (JSON)

사용자가 지정한 권한 목록 그대로 ReadOnly만 부여한다. 쓰기 / 변경 / 삭제 / `GetSecretValue` / 데이터 plane은 절대 포함하지 않는다.

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

설계 메모

- `Allow` 블록은 사용자가 지정한 ReadOnly 권한을 그대로 포함한다. `iam:GenerateCredentialReport` / `GenerateServiceLastAccessedDetails`는 Get/List 계열의 ReadOnly 보강 항목으로만 사용.
- 마지막 `DenyAnyWrite` 블록은 운영자가 IAM 정책을 추가로 attach해 권한이 확대되는 사고를 방지하기 위한 명시 deny. AWS IAM의 `Deny` 우선 순위로 인해 다른 attached policy가 write 권한을 부여하더라도 실제 호출은 차단된다.
- `secretsmanager:GetSecretValue`와 `secretsmanager:GetSecret*` 패턴은 `Deny` 측에 명시한다. ReadOnly 정책에 우연히 GetSecret API가 들어가는 사고를 차단.
- 데이터 plane(KMS Decrypt 등) 호출도 명시 차단. Secret value를 우회 복호화하는 경로를 막는다.

## 4. AWS Console 생성 절차

운영자(Step 0의 `portadmin` 사용자)가 직접 수행한다.

1. Console → IAM → Policies → Create policy → JSON 선택.
2. 위 § 3 JSON을 그대로 붙여 넣는다.
3. Next → Tags 추가:
   - `env=paper`
   - `kind=readonly-validator`
   - `project=portfolio`
4. Policy name: `PortfolioKiroReadOnlyValidatorPolicy`. Description 짧게 기록.
5. Create policy.
6. IAM → Users → Create user.
7. User name: `portfolio-kiro-readonly-validator`.
8. Provide user access to the AWS Management Console: 비활성(콘솔 로그인 비허용. CLI 전용).
9. Permissions options: `Attach policies directly` → `PortfolioKiroReadOnlyValidatorPolicy` 선택 → Next → Tags → Create user.
10. 생성된 사용자 → Security credentials → Create access key:
    - Use case: `Command Line Interface (CLI)`.
    - Confirmation 체크 후 Next → Description tag(예: `kiro-cli`) → Create access key.
    - Access key / Secret access key는 본 문서 / repo / 노트에 평문 기록 금지(`[REDACTED]`).
11. (옵션) IAM → Users → `portfolio-kiro-readonly-validator` → Security credentials → MFA: 인간 사용 없으므로 일반적으로 미적용. 다만 운영자 정책에 따라 하드웨어 키 / Authenticator 등록 가능.

## 5. AWS CLI 생성 절차

운영자가 `portadmin` 자격으로 다음 명령을 순서대로 실행한다(Region은 IAM이 글로벌이므로 무관).

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

발급 직후 Kiro가 사용할 환경(예: `aws configure --profile portfolio-kiro-readonly`)에 access key / secret access key를 등록한다. 등록 후 access key 값은 즉시 메모리에서 제거하고, 평문 파일에 남기지 않는다.

(옵션) IAM Role 형태로 운영하려면 위 정책을 Role의 inline 또는 attached policy로 사용하고, trust policy의 Principal을 외부 system의 OIDC provider 또는 portadmin로 한정한다. 본 spec은 운영 단순성을 위해 IAM User + access key 방식을 1순위로 둔다.

## 6. 검증 명령 — AWS CLI ReadOnly

본 절은 Kiro가 자동 검증 시 호출하는 read-only AWS CLI 명령 모음이다. 각 명령은 운영자 환경에서도 동일하게 재현 가능하다. account-id, RDS endpoint hostname, secret ARN 등 식별자는 출력에서 운영자가 직접 마스킹한다.

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

## 7. 자동 검증 가능 / 수동 확인 분류

### 7-1. 자동 검증 가능 (Kiro가 ReadOnly로 검증)

- VPC CIDR / state / tags
- Subnet 6개 이름 / CIDR / AZ / VPC 매핑
- Internet Gateway attach 상태
- NAT Gateway 미생성
- NAT 역할 EC2 인스턴스 미생성
- Route Table route(0.0.0.0/0 → IGW for `rt-public`, `rt-app`/`rt-data` 외부 라우트 없음) / subnet association
- Security Group inbound / outbound rule, SSH 22 / 5432 0/0 부재 검사
- VPC Endpoint state / subnet / private DNS / SG / route table 연결
- RDS Subnet Group status / member subnets / VPC
- RDS Parameter Group family / 사용자 설정 parameter 값
- RDS instance status / engine version / class / storage / public access / backup retention / deletion protection / encryption / endpoint 존재 여부 / SG / parameter group 적용
- Secret `/portfolio/paper/rds/master` 존재 여부와 metadata(KmsKeyId, RotationEnabled, Tags 등) — value는 절대 조회하지 않음

### 7-2. 수동 확인 필요 (운영자 직접)

- 운영자 승인 여부(approval required task)
- Secret이 본 문서 / 캡처 / 운영자 노트에 평문 노출되지 않았는지(외부 file inspection 필요)
- [`../specs/_common/operator-decisions.md`](../specs/_common/operator-decisions.md) / [`../specs/_common/risk-register.md`](../specs/_common/risk-register.md) 인지 여부(사람의 판독)
- AWS Billing Dashboard 실제 청구 금액(읽기는 가능하나 비용 프로파일 라인 결정은 사람 판단)
- Cost Anomaly Detection alert 등록 여부와 운영자 결정
- DB / schema / role SQL 실행 여부(03 / 06 spec 진행 시점)
- pg_dump / pg_restore 합의 여부(운영자 노트)
- Rollback 수행 여부와 사유(사람 결정)
- Public 문서 / repo 노출 판단

## 8. validation-checklist 반영 규칙

- AWS API로 명확히 확인된 항목 → `<span style="color:red">[O]</span>`
- AWS API로 확인 불가하거나 운영자 판단 필요 항목 → `<span style="color:black">[확인 필요]</span> — 수동 확인 필요`
- 검증 실패 또는 기대값 불일치 항목 → `<span style="color:blue">[X]</span> — 불일치: 실제값 = ..., 기대값 = ...`
- 기대값 / 결정값 / secret value는 본 문서에서도 임의로 변경하지 않는다.

## 9. 보안 / 안전 제약

- 본 IAM User / Role / Access Key는 ReadOnly. 어떤 리소스 변경 호출도 발생하지 않는다.
- `secretsmanager:GetSecretValue`는 정책 Allow에 없고 Deny에 명시된다.
- KMS `Decrypt` / `GenerateDataKey`는 Deny.
- Access key는 본 문서 / repo / 평문 파일 어디에도 기록하지 않는다(`[REDACTED]`).
- Region은 `ap-northeast-2`로 고정.
- access key 노출 의심 시 `aws iam delete-access-key` 후 `create-access-key`로 재발급. 본 문서에는 새 값을 적지 않는다.

## 10. 변경 / 회수 절차

- 더 이상 자동 검증이 필요 없을 때
  1. Access key 비활성: `aws iam update-access-key --user-name portfolio-kiro-readonly-validator --access-key-id [REDACTED] --status Inactive`
  2. 일정 기간 모니터링 후 삭제: `aws iam delete-access-key`
  3. Policy detach + Policy 삭제 + User 삭제(필요 시).
- 권한 부족이 확인되면 Allow에 ReadOnly 항목만 추가한다. Write / Decrypt 항목은 절대 추가하지 않는다.
