# Runbook — 06-secrets-and-iam

본 문서는 운영자가 AWS Console / EC2 shell에서 순서대로 직접 따라 할 수 있는 실행 절차서다. 1차 적용 환경은 `aws-paper`, region은 `ap-northeast-2`, 1차 적용 대상은 MarketConnector EC2다. `aws-live`는 [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook)(예정) 통합 책임.

본 runbook은 실제 AWS 리소스를 자동으로 만들지 않는다. 운영자가 한 단계씩 직접 클릭 / 입력하고, 각 Step의 [확인]까지 통과한 다음 Step으로 진행한다.

선행 입력

- [`./requirements.md`](./requirements.md), [`./README.md`](./README.md), [`./design.md`](./design.md), [`./tasks.md`](./tasks.md)
- [`../02-aws-network-and-rds/runbook.md`](../02-aws-network-and-rds/runbook.md), [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md)
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/risk-register.md`](../_common/risk-register.md)

보안 / 안전 원칙

- 모든 secret value, KIS app key, KIS app secret, 계좌번호, RDS endpoint hostname, RDS password, account-id, 실제 secret ARN, IAM access key id, Slack webhook URL은 `[REDACTED]` 또는 placeholder만 사용한다. 본 문서 / 콘솔 화면 캡처 / 운영자 노트 어디에도 평문으로 적지 않는다.
- 8개 MS 소스 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 금지.
- broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / 주문 / 체결 / Daily Batch / intraday monitor 호출 금지. 본 runbook은 조회성 smoke test까지만 진행한다.
- `secretsmanager:GetSecretValue` 실호출은 운영자만. Kiro 자동 검증은 `secretsmanager:DescribeSecret` metadata만.

## Step 라벨

- `[실행]` AWS Console / EC2 shell에서 리소스를 생성 / 변경하거나 명령을 실제로 실행하는 단계.
- `[확인]` 리소스 생성 없이 상태 / 결과만 확인하는 단계.
- `[준비]` 후속 작업(validation-checklist.md / operation-notes.md / 03 spec) 진입 전 합의 / 기록만 하는 단계.
- `[복구]` 검증 실패 / 장애 / 되돌리기 시점에만 수행하는 단계.

## 1. 사전 확인

### 1-1. 환경 / 대상 확인 [확인]

1. AWS Console 우측 상단에서 region이 `ap-northeast-2`(서울)인지 확인.
2. account alias 또는 account-id가 aws-paper 환경의 것인지 확인. account-id는 운영자 노트에만 기록(본 문서에는 `[REDACTED]`).
3. EC2 → Instances에서 MarketConnector EC2가 존재하는지 확인. 인스턴스 ID는 운영자 노트에만 기록(본 문서에는 `<instance-id>` placeholder).
4. EC2 상태가 `running`이고 EIP가 attach 되어 있는지 확인.

### 1-2. 현재 EC2 자격증명 상태 [확인]

EC2에 접속한 상태에서:

```text
aws sts get-caller-identity
aws configure list
ls -la ~/.aws 2>/dev/null
```

- `~/.aws/credentials` 파일이 있으면 Step 7-5([복구])로 이동 후 본 runbook을 처음부터 다시 진행한다. 본 spec 시점에 EC2 안에 long-lived access key가 남아있으면 안 된다.
- `aws sts get-caller-identity` 결과의 `Arn`이 `assumed-role/...` 형태가 아니라 `user/...` 형태이면 EC2가 IAM user 자격증명으로 동작 중이라는 뜻이다. Step 7-5로 이동한다.

### 1-3. 기존 RDS master secret 호환 [확인]

1. Secrets Manager → Secrets 목록에서 `/portfolio/paper/rds/master`가 존재하는지 확인.
2. 본 runbook은 이 secret을 그대로 유지한다. 새로 생성 / 삭제 / 이름 변경 금지.

### 1-4. 임시 운영 모드 인지 [준비]

오전 검증에서 사용한 shell `export` 기반 환경변수 주입 방식은 임시 운영 모드다. 본 runbook 완료 후에는 정상 운영 모드(Secrets Manager / SSM Parameter Store 경유 주입)로 전환한다. 자세한 정의는 [`./design.md`](./design.md) §3.4 참조.

## 2. Secrets Manager 등록

비용 영향: secret 1건당 월 단가 발생(소액). 본 단계에서 4건 신규 등록 예정. KMS는 AWS managed default key(`aws/secretsmanager`) 사용 가정. CMK 사용 시 [`./design.md`](./design.md) §4.2 KmsDecrypt 조건 추가.

### 2-1. KIS app key secret 생성 [실행]

1. Secrets Manager → `Store a new secret`.
2. Secret type: `Other type of secret`.
3. Key/value 또는 Plaintext에 KIS app key 값을 운영자가 Console에서 직접 입력. 본 문서에는 `[REDACTED]`만.
4. Encryption key: `aws/secretsmanager` (default).
5. Secret name: `/portfolio/paper/marketconnector/kis-app-key`.
6. Description: `MarketConnector KIS app key (paper). Value redacted.`
7. Tags(권고): `env=paper`, `service=marketconnector`, `kind=kis-app-key`, `project=portfolio`.
8. Rotation: 본 spec 범위 밖. `Disable automatic rotation`.
9. Review → Store. 결과 ARN은 placeholder(`arn:aws:secretsmanager:<region>:<account-id>:secret:/portfolio/paper/marketconnector/kis-app-key-XXXXXX`)로 운영자 노트에 기록.

### 2-2. KIS app secret secret 생성 [실행]

- 2-1과 동일 순서. 차이점만:
  - Secret name: `/portfolio/paper/marketconnector/kis-app-secret`.
  - Tags: `kind=kis-app-secret`.
  - 값: KIS app secret(운영자 직접 입력, 본 문서에는 `[REDACTED]`).

### 2-3. KIS paper account secret 생성 [실행]

1. Secret type: `Other type of secret` → Key/value 선택(JSON multi-key).
2. Key 추가:
   - `PAPER_ACNT` = `[REDACTED]` (운영자 직접 입력)
   - `ACNT_PRDT_CD` = `[REDACTED]` (운영자 직접 입력)
3. Secret name: `/portfolio/paper/marketconnector/paper-account`.
4. Tags: `kind=paper-account`.
5. 실제 계좌번호 / 상품코드는 본 문서 / 운영자 노트 / 콘솔 캡처에 평문으로 적지 않는다.

### 2-4. RDS marketconnector_app secret 생성 [실행]

1. Secret type: `Other type of secret` → Key/value (JSON multi-key).
2. Key 추가(값은 운영자 직접 입력, 본 문서에는 `[REDACTED]`):
   - `host`
   - `port`
   - `dbname`
   - `username`
   - `password`
3. Secret name: `/portfolio/paper/rds/marketconnector-app`.
4. Tags: `env=paper`, `service=rds`, `kind=marketconnector-app`, `project=portfolio`.
5. 실제 RDS endpoint hostname / password는 본 문서에 적지 않는다.

### 2-5. 등록 결과 [확인]

1. Secrets Manager → Secrets 목록에서 다음 4건이 존재하는지 확인:
   - `/portfolio/paper/marketconnector/kis-app-key`
   - `/portfolio/paper/marketconnector/kis-app-secret`
   - `/portfolio/paper/marketconnector/paper-account`
   - `/portfolio/paper/rds/marketconnector-app`
2. 기존 `/portfolio/paper/rds/master`가 그대로 유지되는지 확인.
3. 각 secret의 Description / Tag 가 [`./design.md`](./design.md) §1.1 / §2.4 와 일치하는지 확인.

## 3. SSM Parameter Store 등록

비용 영향: Standard tier(4KB 이하) parameter는 무료. 본 단계에서 6건 등록 예정.

### 3-1. KIS base URL parameter 생성 [실행]

1. Systems Manager → Parameter Store → `Create parameter`.
2. Name: `/portfolio/paper/marketconnector/kis-base-url`.
3. Tier: `Standard`.
4. Type: `String`(평문).
5. Value: KIS paper endpoint URL(운영자 직접 입력, 예: `https://openapivts.koreainvestment.com:29443`).
6. Tags: `env=paper`, `service=marketconnector`, `kind=kis-base-url`, `project=portfolio`.
7. Create parameter.

### 3-2. Connector Flask host parameter 생성 [실행]

- 3-1과 동일. 차이점만:
  - Name: `/portfolio/paper/marketconnector/connector-host`.
  - Type: `String`.
  - Value: Connector Flask bind host(운영자 직접 입력).

### 3-3. Connector Flask port parameter 생성 [실행]

- Name: `/portfolio/paper/marketconnector/connector-port`.
- Type: `String` 또는 `String`+숫자 표기. Value: Connector Flask 포트(운영자 직접 입력).

### 3-4. Connector debug parameter 생성 [실행]

- Name: `/portfolio/paper/marketconnector/connector-debug`.
- Type: `String`. Value: `true` / `false` 중 운영자 결정.

### 3-5. environment / broker-name parameter 생성 [실행]

1. `/portfolio/paper/marketconnector/environment` — Value: `paper`.
2. `/portfolio/paper/marketconnector/broker-name` — Value: `kis-paper` 또는 운영자 결정 식별자.

### 3-6. 등록 결과 [확인]

1. Parameter Store 목록에서 `/portfolio/paper/marketconnector/*` prefix로 6건 확인.
2. 각 parameter의 Tier / Type / Tag가 [`./design.md`](./design.md) §1.2 / §2.4 와 일치.
3. 어떤 parameter 이름에도 실제 endpoint hostname / 계좌번호 / secret value가 포함되지 않는지 확인. 미포함이어야 정상.

## 4. IAM Role / Policy 준비

### 4-1. IAM Role 확인 또는 생성 [실행]

1. IAM → Roles → 검색: `portfolio-paper-marketconnector-ec2-role`.
2. 존재하면 Trust Policy를 열어 Principal이 `Service: ec2.amazonaws.com`, Action이 `sts:AssumeRole`인지 확인.
3. 존재하지 않으면 `Create role`:
   - Trusted entity type: `AWS service`.
   - Use case: `EC2`.
   - Role name: `portfolio-paper-marketconnector-ec2-role`.
   - Description: `MarketConnector EC2 instance role for paper. Read-only Secrets/Parameters.`
   - Tags: `env=paper`, `service=marketconnector`, `kind=ec2-role`, `project=portfolio`.

### 4-2. Instance Profile 확인 또는 생성 [실행]

- AWS Console에서 EC2용 Role을 만들면 동명 Instance Profile이 자동 생성되는 경우가 많다. IAM → Roles → 해당 Role 상세에서 Instance Profile ARN이 존재하는지 확인.
- 미존재 시 운영자가 CLI로 생성:
  ```text
  aws iam create-instance-profile --instance-profile-name portfolio-paper-marketconnector-ec2-profile
  aws iam add-role-to-instance-profile \
    --instance-profile-name portfolio-paper-marketconnector-ec2-profile \
    --role-name portfolio-paper-marketconnector-ec2-role
  ```

### 4-3. 최소 read policy 작성 [실행]

1. IAM → Policies → `Create policy` → JSON 탭.
2. Statement 구성([`./design.md`](./design.md) §4.2 매트릭스 그대로). Resource는 placeholder만 사용하고, 운영자가 Console에서 실제 ARN으로 교체:

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

3. CMK 사용이 결정된 경우에만 KmsDecrypt statement 추가([`./design.md`](./design.md) §4.2). AWS managed `aws/secretsmanager` 사용 시 본 statement 추가 금지.
4. Policy name: `portfolio-paper-marketconnector-ec2-readonly`.
5. Description: `Read-only Secrets/Parameters for MarketConnector EC2 (paper). No wildcards.`
6. Resource에 `*` 또는 다른 service prefix가 포함되지 않는지 한 번 더 검토 후 Create.

### 4-4. policy attach [실행]

1. IAM → Roles → `portfolio-paper-marketconnector-ec2-role` → Permissions → `Add permissions` → `Attach policies`.
2. `portfolio-paper-marketconnector-ec2-readonly` 선택 후 attach.
3. SSM Session Manager 접속용 `AmazonSSMManagedInstanceCore` 등 보조 managed policy attach 여부는 03 spec 책임. 본 runbook 범위 밖.

### 4-5. EC2 instance에 Instance Profile attach [실행]

1. EC2 → Instances → MarketConnector EC2 선택 → Actions → Security → Modify IAM role.
2. IAM role: `portfolio-paper-marketconnector-ec2-profile` 선택 후 Update.
3. attach 직후에는 자격증명 캐시가 갱신되기까지 수 초~수 분 소요될 수 있다. EC2를 재기동할 필요는 없다.

비용 영향: IAM Role / Policy / Instance Profile 자체 비용 없음.

## 5. EC2 검증

### 5-1. EC2 접속 [확인]

- SSH 또는 SSM Session Manager로 MarketConnector EC2에 접속(SSM Session Manager 권한은 03 spec에서 attach 예정. 본 시점에 SSH로 접속한다).

### 5-2. assumed-role 확인 [확인]

```text
aws sts get-caller-identity
```

- 결과의 `Arn`이 `arn:aws:sts::<account-id>:assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>` 형태면 정상.
- `user/...` 또는 `iam-user/...` 형태이면 Step 7-5([복구])로 이동.
- 실제 account-id / instance-id는 운영자 노트에만. 본 문서에는 `<account-id>` / `<instance-id>` placeholder.

### 5-3. credential source 확인 [확인]

```text
aws configure list
```

- `access_key`의 Source 컬럼이 `iam-role` 또는 `Ec2InstanceMetadata`인지 확인.
- Source가 `env`, `shared-credentials-file`, `config-file` 이면 Step 7-5([복구])로 이동.

### 5-4. access key 파일 미존재 [확인]

```text
ls -la ~/.aws 2>/dev/null
test -f ~/.aws/credentials && echo "FOUND" || echo "OK"
grep -E "^aws_access_key_id|^aws_secret_access_key" ~/.aws/credentials ~/.aws/config 2>/dev/null
```

- `~/.aws/credentials` 미존재가 정상. 존재하면 Step 7-5.
- grep 결과 0건이 정상.

### 5-5. Secrets Manager metadata 조회 [확인]

```text
aws secretsmanager describe-secret \
  --secret-id /portfolio/paper/marketconnector/kis-app-key \
  --region ap-northeast-2
```

- 같은 패턴으로 4건 모두 메타데이터(이름 / KMS / 마지막 수정 시각) 정상 반환 확인.
- `secretsmanager:GetSecretValue`를 검증 단계에서 호출하지 않는다. 운영자가 정상 운영(Step 6) 시점에만 호출한다.

### 5-6. SSM Parameter 조회 [확인]

```text
aws ssm get-parameters-by-path \
  --path /portfolio/paper/marketconnector \
  --region ap-northeast-2
```

- `/portfolio/paper/marketconnector/*` 6건이 반환되는지 확인.
- 다른 service prefix(`/portfolio/paper/view/...`, `/portfolio/paper/crawler/...`)에 대한 조회는 AccessDenied가 정상. 다음 명령으로 권한 격리 확인:
  ```text
  aws ssm get-parameter --name /portfolio/paper/view/dummy --region ap-northeast-2 || echo "DENIED OR NOT_FOUND (OK)"
  ```

## 6. Connector smoke test (조회성만)

본 단계의 목표는 Instance Role 자격증명 + secret/parameter 주입으로 기존 조회성 흐름이 그대로 통과하는지 확인하는 것이다. 신규 주문 / 매수 / 매도 / 취소 / 정정 호출은 절대 하지 않는다.

### 6-1. 임시 환경변수 주입 스크립트 [실행]

1. EC2의 운영자 home 또는 `/opt/portfolio/marketconnector/` 같은 디렉터리에 임시 export 스크립트(예: `/tmp/inject-env.sh`)를 만든다. 스크립트는 secret/parameter 값을 환경변수에 주입한다(파일에 secret 값을 평문으로 적지 않는다 — 아래 패턴은 메모리 export만 한다).
2. 스크립트 패턴(placeholder만):
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
3. 스크립트 권한: `chmod 700 /tmp/inject-env.sh`. 실행은 `source /tmp/inject-env.sh`.
4. 스크립트 자체에는 secret 값이 들어가지 않는다. 환경변수도 현재 shell 안에만 export 되며 파일에 저장되지 않는다.

### 6-2. RDS 접속 확인 [확인]

- `psql` 또는 기존 운영 명령으로 `marketconnector_app` 사용자 접속만 확인. DDL/DML 금지.

### 6-3. KIS token 발급 / 재사용 확인 [확인]

- `port-marketconnector` 의 token manager 동작이 access_token 파일을 읽거나 신규 발급으로 이어지는지만 확인. 토큰 값 평문 출력 금지.

### 6-4. 조회성 entrypoint smoke test [확인]

- `connector_balance.py` 실행 → 잔고 조회 결과 반환 확인.
- `connector_order_check.py` 실행 → 주문/체결 조회 결과 반환 확인.
- Flask 조회성 endpoint smoke test(잔고 / 보유 / 주문 내역 조회) 통과 확인.

### 6-5. 신규 주문 API 호출 금지 [확인]

- `connector_buy.py`, `connector_sell.py`, `connector_cancel.py`, `connector_modify.py` 실행 금지.
- 본 runbook 범위에서는 매수 / 매도 / 취소 / 정정 호출이 0건이어야 한다.

### 6-6. 정상 운영 모드 전환 [준비]

- 임시 export 스크립트 검증이 끝나면, 정상 운영 모드(systemd unit + Instance Role 기반 startup script)로의 전환은 03 spec(MarketConnector EC2 운영) 책임으로 인계한다. 본 runbook은 임시 검증까지만.

## 7. 실패 시 점검 / 복구

### 7-1. secret 이름 오타 [복구]

- AccessDenied 또는 NotFound가 발생하면 secret 이름을 정확히 확인한다(`/portfolio/paper/marketconnector/kis-app-key` 형식). 4건 모두 [`./design.md`](./design.md) §2.4 표와 일치해야 한다.
- 이름이 틀렸으면 secret을 삭제하지 말고 IAM policy의 Resource ARN을 일치시키거나 secret을 정확한 이름으로 재생성. 잘못된 이름 secret은 30일 복구 기간 후 자동 삭제 또는 운영자 명시적 삭제(`force-delete-without-recovery`는 사용하지 않는다).

### 7-2. IAM policy Resource 범위 [복구]

- AccessDenied가 발생하면 policy의 Resource ARN이 본 spec naming(`/portfolio/paper/marketconnector/*`, `/portfolio/paper/rds/marketconnector-app*`)과 일치하는지 확인.
- Resource에 `*` 또는 다른 service prefix(`/portfolio/paper/view/*`)가 포함되어 있으면 즉시 detach 후 본 runbook §4-3 매트릭스로 재작성.

### 7-3. Instance Profile attach 상태 [복구]

```text
aws ec2 describe-iam-instance-profile-associations \
  --filters Name=instance-id,Values=<instance-id> \
  --region ap-northeast-2
```

- 결과의 `IamInstanceProfile.Arn`이 `portfolio-paper-marketconnector-ec2-profile` 형태인지 확인.
- 다른 Profile이 attach 되어 있으면 EC2 → Modify IAM role에서 교체.

### 7-4. VPC Endpoint 상태 [복구]

- Secrets Manager / SSM Interface Endpoint가 EC2의 subnet에 연결되어 있고 SG가 EC2 SG의 inbound 443을 허용하는지 확인. 02 spec runbook의 VPC Endpoint Step 참조.
- DNS 응답이 endpoint private IP로 반환되는지 확인(`nslookup secretsmanager.ap-northeast-2.amazonaws.com`).

### 7-5. Access Key 환경변수 / 파일 오염 [복구]

```text
unset AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY AWS_SESSION_TOKEN
test -f ~/.aws/credentials && mv ~/.aws/credentials ~/.aws/credentials.bak.$(date +%s)
test -f ~/.aws/config && grep -E "^aws_access_key_id|^aws_secret_access_key" ~/.aws/config
```

- `~/.aws/credentials`를 백업 이동(삭제 대신). 이후 새 shell에서 §1-2 / §5-2 / §5-3 재검증.
- 누군가가 export 한 access key가 발견되면 즉시 IAM Console에서 폐기(`Make inactive` → `Delete`).

### 7-6. shell 환경변수 임시 방식 rollback [복구]

- secret/parameter 주입이 실패하면, 오전에 사용한 shell `export` 직접 주입 방식(임시 운영 모드)으로 일시 복귀해 운영을 계속한다. 단 이는 임시 모드이며, 본 runbook 결함 해결 후 정상 운영 모드로 다시 전환한다. [`./design.md`](./design.md) §3.4 참조.

## 8. 완료 기준 [확인]

- [확인] EC2 안 `~/.aws/credentials` 미존재. `aws_access_key_id` / `aws_secret_access_key` 패턴 0건.
- [확인] `aws sts get-caller-identity` 결과가 `assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>` 형태.
- [확인] `aws configure list`의 access_key Source가 `iam-role` 또는 `Ec2InstanceMetadata`.
- [확인] Secrets Manager 4건(`kis-app-key` / `kis-app-secret` / `paper-account` / `marketconnector-app`) + 기존 `rds/master` 유지. Describe 가능.
- [확인] SSM Parameter 6건(`kis-base-url` / `connector-host` / `connector-port` / `connector-debug` / `environment` / `broker-name`) Get 가능.
- [확인] IAM policy의 Resource에 `*` 또는 다른 service prefix 0건. Action에 `secretsmanager:*` / `ssm:*` / `*` 0건.
- [확인] Connector 조회성 smoke test 통과(`connector_balance.py`, `connector_order_check.py`, Flask 조회 endpoint). 신규 주문 / 매수 / 매도 / 취소 / 정정 호출 0건.
- [확인] 본 runbook 어디에도 실제 secret value, KIS app key, KIS app secret, 계좌번호, RDS endpoint hostname, RDS password, account-id, 실제 secret ARN, IAM access key id, Slack webhook URL이 평문으로 기록되지 않음. 모두 `[REDACTED]` 또는 placeholder 사용.

## 9. 후속 인계 [준비]

- [`./validation-checklist.md`](./validation-checklist.md): 본 runbook의 [확인] 결과를 4종 라벨(`[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]`)로 점검 항목화.
- [`./operation-notes.md`](./operation-notes.md): 본 runbook 수행 일자, secret/parameter 등록 결과(성공/실패만), IAM Role / Policy 변경 요약(전후 항목 요약)을 누적 기록. 실제 secret 값 / account-id / RDS endpoint / 실제 ARN은 미기록.
- [`../03-marketconnector-ec2`](../03-marketconnector-ec2)(예정): SSM Session Manager 접속용 `AmazonSSMManagedInstanceCore` attach, CloudWatch Logs write 권한, systemd 기반 정상 운영 모드 전환은 03 spec 책임. 본 runbook의 Instance Role 정책을 그대로 입력으로 받는다.
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md): OD-SEC-001 / OD-OBS-004 갱신 후보 + 신규 OD-SEC-005 / OD-SEC-006 후보를 운영자 승인 시 반영. 자세한 후보값은 [`./design.md`](./design.md) §7.
- [`../_common/risk-register.md`](../_common/risk-register.md): R-SEC 후보 4건(권한 과다 / EC2 secret 평문 노출 / EC2 access key 파일 / naming 불일치)을 운영자 승인 시 다음 가용 ID로 등록. 자세한 후보값은 [`./design.md`](./design.md) §8.
