# Design Document — 06-secrets-and-iam

## Introduction

본 spec의 핵심은 한 줄로 요약한다. **MarketConnector EC2가 IAM Access Key를 EC2 안에 저장하지 않고, EC2 Instance Role 만으로 Secrets Manager / SSM Parameter Store 값을 최소 권한으로 read 하는 운영 구조를 1차 확정한다.**

- 1차 적용 환경: `aws-paper`. region `ap-northeast-2`. 1차 적용 대상 워크로드: MarketConnector EC2.
- 본 spec 의 입력
  - [`./requirements.md`](./requirements.md) R1 ~ R14, [`./README.md`](./README.md)
  - [`../02-aws-network-and-rds/`](../02-aws-network-and-rds/) 1차 적용 결과
    (VPC / Subnet / SG / VPC Endpoint 5종 / RDS PostgreSQL / DB role 7종)
- 본 spec 의 산출물: 본 phase 에서는 [`./design.md`](./design.md) 한 개. 후속 phase 4종. R14.7 근거.
  - [`./tasks.md`](./tasks.md) / [`./runbook.md`](./runbook.md)
  - [`./validation-checklist.md`](./validation-checklist.md) / [`./operation-notes.md`](./operation-notes.md)
- 본 spec 의 범위 밖(반드시 명시). R14 근거.
  - live rotation 자동화(Lambda rotation, scheduled rotation)
  - GitHub Actions OIDC / CI/CD Role (07 spec)
  - 8개 MS 전체 full IAM 매트릭스 (03 / 08 / 04 / 05 / 09 spec 분담)
  - aws-live IAM (10 spec 통합)
  - 실제 AWS 리소스 생성 / 수정 / 삭제

본 문서에는 아래 항목을 적지 않는다. 모두 `[REDACTED]` 또는 placeholder(`<account-id>`, `<region>` 등) 만 사용한다.
R14.4 근거.

- 실제 secret value / KIS app key / KIS app secret / 계좌번호
- RDS endpoint hostname / account-id
- 실제 secret ARN / IAM access key id

## 1. Secret / Parameter 분류 기준

### 1.0 분류 원칙 (R1.5 / R1.6 / R1.7 근거)

- "노출되면 외부 발신 / 외부 인증 / 외부 자금 이동이 가능한 값" → **Secrets Manager**.
- "환경에 따라 값이 달라지지만 그 값 자체로는 외부 행위를 직접 일으키지 못하는 값" → **SSM Parameter Store**.
- 분류 기준은 비용이 아니라 노출 시 영향이다. 비용 절감을 위해 SSM SecureString 으로 옮길 수 있는 후보와 옮기면 안 되는 후보는 §1.4 에서 분리한다.

### 1.1 Secrets Manager 보관 후보 (R1.1 / R1.2 근거)

| 항목 | 환경변수 키 | 분류 사유 | 비고 |
|------|------------|----------|------|
| KIS app key | `APP_KEY` (port-marketconnector `config.py`) | 노출 시 KIS API 호출 / 외부 인증 가능 | 환경별 분리(`paper` / `live`). 본 spec(06)은 `paper` 만 1차 락 |
| KIS app secret | `APP_SECRET` (동) | 노출 시 KIS API 호출 / 외부 인증 가능 | 동상 |
| KIS paper 계좌번호 | `PAPER_ACNT` | 노출 시 broker 측 계좌 식별 노출 | 단독 secret 또는 JSON multi-key secret 권고. §2 매트릭스 참조 |
| KIS 계좌상품코드 | `ACNT_PRDT_CD` | 계좌번호와 짝으로 broker 호출에 필요 | `PAPER_ACNT` 와 동일 secret(JSON multi-key) 권고 |
| RDS `marketconnector_app` 접속정보 | `INTEREST_DB_*` 5종 (매핑 아래 참조) | password 노출 시 RDS 접속 가능 | JSON multi-key secret 1개로 묶어 보관 권고 |
| RDS master 비밀번호(`portfolio_admin`) | (application 환경변수 없음, 운영자 전용) | 02 spec 에서 이미 등록된 secret. 호환 유지 | 기존 이름 `/portfolio/paper/rds/master` 그대로(§2.5) |

#### Row Notes — RDS `marketconnector_app` 접속정보 환경변수 매핑

- `INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD`

### 1.2 SSM Parameter Store 보관 후보 (R1.3 근거)

| 항목 | 환경변수 키 | 분류 사유 | 비고 |
|------|------------|----------|------|
| KIS base URL | `BASE_URL` (port-marketconnector `config.py`) | 환경별 endpoint 가 다르지만 그 값 자체는 비밀이 아님 | `paper` / `live` 별 다른 parameter |
| Connector Flask host | (운영자 결정 키) | 일반 설정값 | 환경별 다를 수 있음 |
| Connector Flask port | (동) | 일반 설정값 | |
| Connector Flask debug flag | (동) | 일반 설정값 | `paper` 검증 모드에서 임시 ON 가능 |
| `PORT_ENVIRONMENT` | `PORT_ENVIRONMENT` | 환경 식별자(`paper` / `live`). 비밀 아님 | live 호출 분기에 사용 |
| `PORT_BROKER_NAME` | `PORT_BROKER_NAME` | broker 이름. 비밀 아님 | |
| `PORT_STRATEGY_NAME` | `PORT_STRATEGY_NAME` | 전략 이름. 비밀 아님 | |
| `PORT_STRATEGY_VERSION` | `PORT_STRATEGY_VERSION` | 전략 버전. 비밀 아님 | |

### 1.3 Slack webhook 보관 위치 비교 (R1.4 / OD-OBS-004 근거)

| 후보 | 장점 | 단점 | 권고 |
|------|------|------|------|
| Secrets Manager | rotation / audit / 환경별 KMS 정책 일관성 | secret 개당 월 단가(소액) | **권고(1순위)** |
| SSM SecureString | 무료(KMS Key 사용 시 KMS 호출 단가만) | rotation API 표준이 약함, audit 형식이 Secrets Manager 와 다름 | 비용 절감 시 허용 가능한 절감안(2순위) |

본 spec(06) 1차 락 결정값(잠정): **Secrets Manager 보관(권고)**.

- 비용 절감이 필요하면 SSM SecureString 으로 옮기는 변경 제안을 아래에 기록 후 승인 시 적용.
  - [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-OBS-004
- 본 결정의 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 갱신 후보는 §7 참조.

### 1.4 옮겨도 되는 후보 vs 옮기면 안 되는 후보 (R1.7 근거)

| 항목 | SSM SecureString 으로 옮겨도 되는가 | 사유 |
|------|------------------------------------|------|
| Slack webhook URL | 옮겨도 됨(절감안) | 노출 영향이 외부 채널 메시지 발신에 한정. KMS 암호화로 충분 |
| KIS app key | 옮기면 안 됨 | broker 인증 핵심 값. Secrets Manager 의 rotation / audit 표준 적용 권고 |
| KIS app secret | 옮기면 안 됨 | 동상 |
| RDS `marketconnector_app` password | 옮기면 안 됨 | RDS 직접 접속 가능. Secrets Manager 의 master password 정책(OD-SEC-002)과 일관성 유지 |
| RDS master(`portfolio_admin`) password | 옮기면 안 됨 | 02 spec 에서 이미 Secrets Manager 등록. 호환 유지(§2.5) |

## 2. Naming / 경로 규칙

### 2.1 기본 규칙 (R2.1 / R2.2 / R2.3 / R2.4 근거)

- 기본 형식: `/portfolio/{env}/{service}/{item}`.
- `{env}` 값: `paper` 또는 `live`. 그 외(`dev`, `test`)는 본 spec 범위 밖. OD-ENV-001 / OD-ENV-003 입력.
- `{service}` 값(본 spec 시점 후보): `marketconnector`, `rds`, `view`, `strategy`, `crawler`, `preprocessor`, `research`, `ops`. 본 spec(06)이 1차 확정 대상으로 보는 것은 `marketconnector` 와 `rds` 두 개로 한정. R2.4 근거.
- `{item}` 은 lowercase 알파벳 + 숫자 + `-` 만 사용. 띄어쓰기 / 한글 / 대문자 / `_` / 실제 값 금지.

### 2.2 호환 정책 (R2.5 근거)

- 02 spec / [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 에 이미 등록된 기존 이름(`/portfolio/paper/rds/master`)은 본 규칙과 동일 형태이며 그대로 유지한다. 본 규칙은 신규 항목부터 적용한다.

### 2.3 이름 자체 금지 규칙 (R2.6 근거)

- 실제 RDS endpoint hostname, 계좌번호, secret value, password, token 을 secret / parameter 이름 자체에 포함하지 않는다. 식별자 / 계좌번호 / hostname 모두 placeholder 또는 항목명(`kis-app-key`, `marketconnector-app`)만 사용한다.

### 2.4 secret / parameter 이름 매트릭스 (R2.1 / R2.2 / R3.2 근거)

| 보관소 | 이름 (예) | 매핑 환경변수 (placeholder) | 비고 |
|--------|-----------|----------------------------|------|
| Secrets Manager | `/portfolio/paper/marketconnector/kis-app-key` | `APP_KEY` | 단일 값 secret |
| Secrets Manager | `/portfolio/paper/marketconnector/kis-app-secret` | `APP_SECRET` | 단일 값 secret |
| Secrets Manager | `/portfolio/paper/marketconnector/paper-account` | `PAPER_ACNT`, `ACNT_PRDT_CD` | JSON multi-key 권고 |
| Secrets Manager | `/portfolio/paper/rds/marketconnector-app` | `INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD` | JSON multi-key |
| Secrets Manager | `/portfolio/paper/rds/master` | (운영자 전용) | 02 spec 등록 secret. 그대로 유지 |
| SSM Parameter | `/portfolio/paper/marketconnector/kis-base-url` | `BASE_URL` | 일반 설정값 |
| SSM Parameter | `/portfolio/paper/marketconnector/connector-host` | (운영자 결정 키) | Flask host |
| SSM Parameter | `/portfolio/paper/marketconnector/connector-port` | (동) | Flask port |
| SSM Parameter | `/portfolio/paper/marketconnector/connector-debug` | (동) | Flask debug flag |
| SSM Parameter | `/portfolio/paper/marketconnector/environment` | `PORT_ENVIRONMENT` | 환경 식별자 |
| SSM Parameter | `/portfolio/paper/marketconnector/broker-name` | `PORT_BROKER_NAME` | broker 이름 |

Slack webhook 의 이름은 §1.3 결정에 따라 `/portfolio/paper/ops/slack-webhook` (Secrets Manager) 으로 권고한다. 실제 등록은 본 spec 범위 밖, 운영자 직접 작업.

### 2.5 RDS master 호환 (R2.5 근거)

- 02 spec 에서 이미 등록된 `/portfolio/paper/rds/master` 는 본 규칙과 동일 형식이며 그대로 사용한다. 본 spec 의 `/portfolio/paper/rds/marketconnector-app` 은 신규 항목으로 추가된다. 두 secret 의 권한 분리는 §4.2 매트릭스 참조.

## 3. 환경변수 호환성

### 3.1 8개 MS 환경변수 키 유지 (R3.1 / OD-DB-003 근거)

본 spec 도입 후에도 8개 MS 의 기존 환경변수 키 이름은 변경하지 않는다. 다음 키는 그대로 유지한다.

- DB 접속 계열: `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`, `PORTFOLIO_DB_NAME`
- 계좌 / 환경 계열: `PORT_ACCOUNT_NO`, `PORT_BROKER_NAME`, `PORT_ENVIRONMENT`
- 전략 / 주문 계열: `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION`, `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT`

### 3.2 port-marketconnector `config.py` 외부화 (R3.2 근거)

- `port-marketconnector` 의 `c:\Workspaces\port-marketconnector\config.py` 가 사용하는 KIS 식별자를 외부화한다.
  - 대상: `APP_KEY`, `APP_SECRET`, `BASE_URL`, `PAPER_ACNT`, `ACNT_PRDT_CD`
  - 코드 변경 없이 환경변수로 주입 가능하도록 매핑한다. 매핑은 §2.4 표 참조.
- 본 spec(06)은 `config.py` 수정 자체를 다루지 않는다(8개 MS 코드 미수정 정책, R14.1). 외부화는 환경변수 주입 형태로만 진행한다.

### 3.3 주입 시점 (R3.3 근거)

- secret / parameter 값은 process 시작 시점에 환경변수로 주입된다.
- 런타임 중 자동 rotation 반영(running process 가 새 값을 자동 read) 은 본 spec 범위 밖.

### 3.4 임시 운영 모드 vs 정상 운영 (R3.4 근거)

| 모드 | 형태 | 위치 | 정책 |
|------|------|------|------|
| 임시 운영 모드 | shell `export` 직접 주입 | EC2 안 운영자 shell session | 본 spec 시점 오전 검증에서 사용된 모드. 향후에는 단발 검증 / 디버깅 외 사용 비권고 |
| 정상 운영 모드 | Secrets Manager / SSM Parameter Store 경유 주입 | EC2 startup script 또는 systemd `EnvironmentFile`(secret value 자체는 미저장) | 표준 |

`EnvironmentFile` 에 secret value 평문이 저장되면 안 된다. 자세한 점검 항목은 §5 와 후속 phase 의 [`./validation-checklist.md`](./validation-checklist.md) 에서 다룬다.

## 4. MarketConnector EC2 Instance Role 설계 (R4 근거)

### 4.1 Role / Instance Profile 이름 후보 (R4.1 근거)

| 항목 | 이름 후보 |
|------|-----------|
| IAM Role | `portfolio-paper-marketconnector-ec2-role` |
| IAM Instance Profile | `portfolio-paper-marketconnector-ec2-profile` |
| Trust Policy Principal | `Service: ec2.amazonaws.com` |
| Trust Policy Action | `sts:AssumeRole` |
| Trust Policy Effect | `Allow` |

### 4.2 Permission Policy 매트릭스 (R4.2 / R4.3 / R4.6 근거)

| Statement | Effect | Action | Resource | 비고 |
|-----------|--------|--------|----------|------|
| `SecretsManagerRead` | Allow | `secretsmanager:GetSecretValue`, `secretsmanager:DescribeSecret` | 4건 secret ARN (KIS 3건 + RDS 1건). 상세 목록은 §2.4 참조 | 다른 service secret 권한 부여 금지. 실제 ARN suffix `-XXXXXX` 는 운영자 등록 시 기록 |
| `SsmParameterRead` | Allow | `ssm:GetParameter`, `ssm:GetParameters`, `ssm:GetParametersByPath` | `arn:aws:ssm:<region>:<account-id>:parameter/portfolio/paper/marketconnector/*` | prefix 만 한정. 다른 service prefix 포함 금지 |
| `KmsDecrypt` (조건부) | Allow | `kms:Decrypt` | `arn:aws:kms:<region>:<account-id>:key/<kms-key-id>` | 상세 조건은 Row Notes 참조 |

#### Row Notes — SecretsManagerRead Resource 상세

- Resource 자리에 들어가는 4건 secret ARN 은 아래 secret 이름을 따른다.
  - `/portfolio/paper/marketconnector/kis-app-key`
  - `/portfolio/paper/marketconnector/kis-app-secret`
  - `/portfolio/paper/marketconnector/paper-account`
  - `/portfolio/paper/rds/marketconnector-app`
- ARN 형태: `arn:aws:secretsmanager:<region>:<account-id>:secret:<name>-*`

#### Row Notes — KmsDecrypt 조건

- Secrets Manager CMK 또는 SSM SecureString 사용 시에만 본 statement 적용.
- AWS managed `aws/secretsmanager` default key 만 사용하는 경우 본 statement 적용 외.

본 매트릭스는 R4.5 근거에 따라 모든 ARN 자리를 placeholder 또는 `[REDACTED]` 로만 표기한다. 실제 account-id / 실제 secret ARN / 실제 KMS Key ARN 은 본 문서에 절대 기록하지 않는다.

### 4.3 금지 정책 매트릭스 (R4.4 근거)

| 패턴 | 금지 사유 |
|------|----------|
| `Resource: "*"` | 권한 과다. R-SEC 후보(§8). 다른 service / 다른 환경 secret 까지 read 가능 |
| `Action: "*"` | 권한 과다. read 외 write / delete / rotate 까지 부여됨 |
| `secretsmanager:*` | 권한 과다. `CreateSecret` / `DeleteSecret` / `PutSecretValue` 까지 부여됨 |
| `ssm:*` | 권한 과다. `PutParameter` / `DeleteParameter` 까지 부여됨 |
| 다른 service prefix(`/portfolio/paper/view/*`, `/portfolio/paper/crawler/*`, `/portfolio/paper/strategy/*`, `/portfolio/paper/research/*`) Resource 포함 | 06 책임 범위 밖. 03 / 08 / 04 / 05 / 09 spec 분담 |
| 다른 환경 prefix(`/portfolio/live/...`) Resource 포함 | aws-live IAM 은 10 spec 통합 책임 |

### 4.4 책임 범위 분리 (R4.7 근거)

| spec | 다루는 권한 |
|------|-------------|
| 06 (본 spec) | secret / parameter read 권한(Secrets Manager, SSM Parameter Store, 조건부 KMS Decrypt) |
| 03 (marketconnector-ec2) | SSM Session Manager 접속용 `AmazonSSMManagedInstanceCore` (managed policy attach), CloudWatch Logs write, EC2 운영 보조 권한 |

본 spec 의 Permission Policy 와 03 의 managed policy attach 는 같은 IAM Role 에 함께 attach 되지만, 책임 / 락 시점은 분리한다.

### 4.5 문서 표기 규칙 (R4.5 근거)

- 모든 ARN 은 아래 placeholder 또는 `[REDACTED]` 만 사용한다.
  - `arn:aws:iam::<account-id>:role/portfolio-paper-marketconnector-ec2-role`
  - `arn:aws:secretsmanager:<region>:<account-id>:secret:/portfolio/paper/...`
  - `arn:aws:ssm:<region>:<account-id>:parameter/portfolio/paper/...`
  - `arn:aws:kms:<region>:<account-id>:key/<kms-key-id>`
- 실제 account-id / 실제 secret ARN / 실제 KMS Key ARN 은 본 문서 / 후속 phase 산출물 에 절대 기록하지 않는다.

## 5. Access Key 미사용 원칙 (R5 근거)

### 5.1 금지 / 표준 매트릭스 (R5.1 / R5.2 근거)

| 항목 | 정책 |
|------|------|
| `~/.aws/credentials` long-lived access key 파일 | **금지** |
| `~/.aws/config` 의 access key 항목 | **금지** |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` 환경변수 (EC2 안에서 export / 저장) | **금지** |
| systemd unit 의 `Environment=AWS_ACCESS_KEY_ID=...` | **금지** |
| systemd unit 의 `EnvironmentFile=` 에 access key 저장 | **금지** |
| 사용자 home 디렉터리 dotfile(`~/.bashrc`, `~/.profile`, `~/.bash_profile`) 에 access key export | **금지** |
| application config 파일 / `.env` 파일에 access key 저장 | **금지** |
| IMDSv2 + Instance Role 자격증명 | **표준 사용** |

### 5.2 검증 위치 (R5.3 근거)

| 산출물 | 검증 항목 |
|--------|-----------|
| [`./runbook.md`](./runbook.md) (후속 phase) | 자격증명 검증 명령 (상세 아래) |
| [`./validation-checklist.md`](./validation-checklist.md) (후속 phase) | `~/.aws/credentials` 미존재 점검, assumed-role ARN 일치 점검, `EnvironmentFile=` 안 access key 패턴 0건 점검 |

#### Row Notes — runbook.md 자격증명 검증 명령

- EC2 안에서 `aws sts get-caller-identity` 실행 → assumed-role ARN 확인.
  - 기대 형태: `arn:aws:sts::<account-id>:assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>`
- `aws configure list` 결과의 `access_key` source 가 `iam-role` 인지 확인.

실제 검증은 후속 phase 의 책임. 본 design 은 위치만 명시.

### 5.3 비상 시나리오 처리 (R5.4 근거)

- 운영자가 임시로 EC2 안에 access key 를 둬야 하는 비상 시나리오를 발견한 경우, 본 spec 임의 결정 금지.
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-SEC 카테고리에 변경 제안만 기록 후 운영자 승인 시 적용한다.
- 비상 적용 후에는 즉시 access key 폐기 + 본 spec 표준(IMDSv2 + Instance Role) 으로 복귀 절차를 [`./runbook.md`](./runbook.md) [복구] 단계로 분리한다.

### 5.4 access key 값 표기 정책 (R5.5 근거)

- 본 spec 의 모든 산출물(design / tasks / runbook / validation-checklist / operation-notes) 에 실제 access key id, secret access key 값을 절대 기록하지 않는다. 모두 `[REDACTED]` 만 사용.

## 6. ECS Task Role 재사용 패턴 (실제 생성은 03 / 08 / 04 / 05 / 09)

본 spec 은 패턴 템플릿만 남기고, 실제 Role / Resource ARN 채움은 후속 spec 의 책임이다. R6 / R13 근거.

### 6.1 Role 분리 (R6.2 근거)

| 이름 | 책임 | 권한 종류 | 사용 시점 |
|------|------|----------|-----------|
| Task Execution Role | ECR 이미지 pull, CloudWatch Logs write, Secrets Manager 환경변수 주입 | AWS managed `AmazonECSTaskExecutionRolePolicy` + Secrets Manager Resource ARN 한정 | Task 시작 시점, AWS Service(ECS Agent) 측이 사용 |
| Task Role | 애플리케이션 런타임 secret / parameter read | 06 의 Instance Role 권한 매트릭스(§4.2) 와 동일 골격 | 컨테이너 프로세스가 사용 |

#### Row Notes — Task Execution Role vs Task Role Action

- Task Execution Role Action: `secretsmanager:GetSecretValue` (ECS `secrets` 필드 주입 목적).
- Task Role Action: `secretsmanager:GetSecretValue`, `secretsmanager:DescribeSecret`,
  `ssm:GetParameter`, `ssm:GetParameters`, `ssm:GetParametersByPath`.

### 6.2 ECS `secrets` 필드 패턴 (R6.3 근거)

- ECS Task Definition 의 `containerDefinitions[*].secrets[*]` 항목은 다음 패턴을 따른다(placeholder).
  - `name=<ENV_KEY>, valueFrom=arn:aws:secretsmanager:<region>:<account-id>:secret:/portfolio/{env}/{service}/{item}-XXXXXX`
- JSON multi-key secret(예: `/portfolio/paper/rds/marketconnector-app`) 사용 시 `valueFrom` 끝에 `:json-key::` 접미를 붙여 매핑한다(placeholder).
  - 예: `arn:aws:secretsmanager:<region>:<account-id>:secret:/portfolio/paper/rds/marketconnector-app-XXXXXX:host::`
- 실제 ARN suffix / json-key 분기 는 후속 spec 에서 채운다.

### 6.3 분담 매트릭스 (R6.4 / R6.5 / R13 근거)

| spec | 책임 | 본 spec 06 입력 활용 |
|------|------|---------------------|
| 03 marketconnector-ec2 | Instance Role 정식 운영, SSM Session Manager attach, CloudWatch Logs | 06 의 Instance Role 정책(§4.2)을 그대로 입력으로 받음 |
| 08 interest-crawler-and-preprocessor-ecs | crawler / preprocessor 전용 Task Role 생성 | 06 의 Task Role 골격(§6.1, §6.2) + service prefix 만 변경 (`crawler` / `preprocessor`) |
| 04 strategy-batch-stepfunctions | strategy decision / execution Task Role, Step Functions IAM | 06 의 Task Role 골격 사용 |
| 05 port-view-ecs-and-runbook | port-view Task Role | 06 의 Task Role 골격 사용 |
| 09 strategy-research-batch | research(AWS Batch + Step Functions) Task Role / Job Role | 06 의 Task Role 골격 사용 |

### 6.4 후속 spec 이 변경하지 말아야 할 결정 (R13.4 근거)

- naming `/portfolio/{env}/{service}/{item}` 형식 유지.
- `{env}` 값 `paper` / `live` 만 사용.
- Resource wildcard(`Resource: "*"`) 금지.
- Action wildcard(`secretsmanager:*` / `ssm:*` / `Action: "*"`) 금지.
- 다른 service prefix Resource 부여 금지.

## 7. operator-decisions.md 갱신 후보 (실제 갱신은 tasks 단계)

본 spec 락 후 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 에 반영할 후보. R10 근거. 실제 갱신은 본 spec 의 [`./tasks.md`](./tasks.md) 단계.

### 7.1 본 spec 락 / 신규 후보

| ID | 항목 | 본 spec 후보 결정값 | Status (영문 / 한글) |
|----|------|--------------------|---------------------|
| OD-SEC-001 | Secrets 보관 위치 | Secrets Manager 항목과 SSM Parameter Store 항목 분리 (상세 아래) | TENTATIVE 🟡 잠정 (운영자 승인 시 CONFIRMED 🟢 확정) |
| OD-OBS-004 | Slack webhook 보관 위치 | **Secrets Manager 권고**. 비용 절감 시 SSM SecureString 허용 | TENTATIVE 🟡 잠정 |
| OD-SEC-005 (신규 후보) | MarketConnector EC2 / 8개 MS Access Key 미사용 원칙 | EC2 안에 access key 파일 / 환경변수 / dotfile 저장 금지. IMDSv2 + Instance Role 자격증명만 사용 | TENTATIVE 🟡 잠정 |
| OD-SEC-006 (신규 후보) | EC2 / ECS IAM Role 기반 secret / parameter read 원칙 | 최소 권한(Action 화이트리스트). Resource wildcard 금지. Action wildcard 금지. service prefix 분리 | TENTATIVE 🟡 잠정 |

#### Row Notes — OD-SEC-001 후보 결정값

- Secrets Manager 보관
  - KIS app key / app secret / paper 계좌 / RDS 접속정보 / RDS master
- SSM Parameter Store 보관
  - KIS base URL / Connector host·port·debug
  - `PORT_ENVIRONMENT` / `PORT_BROKER_NAME` 등 일반 설정

### 7.2 기존 결정 정합성 (R10.5 근거)

| ID | 항목 | 본 spec 결정과 정합성 | 본 spec 갱신 권고 |
|----|------|----------------------|------------------|
| OD-SEC-002 | RDS master password = Secrets Manager | 정합 | 상태 유지(TENTATIVE 🟡 → CONFIRMED 🟢 운영자 승인 시점에 락 가능) |
| OD-SEC-003 | KIS access_token 보관 위치 = EC2 로컬 파일 + S3 backup 1순위 | 정합 (상세 아래) | 상태 유지(TENTATIVE 🟡) |

#### Row Notes — OD-SEC-003 정합 상세

- paper 환경에서는 token 파일 유지(`access_token.txt`) 는 03 spec 의 책임.
- token 자체를 Secrets Manager 로 보관하는 옵션은 후속 spec(03) 에서 재검토.

### 7.3 Status 라벨 규칙 (R10.7 근거)

- 영문 기준값: CONFIRMED / TENTATIVE / TBD / DEFERRED.
- 표시용 한글 / 색상: 🟢 확정 / 🟡 잠정 / 🔴 미정 / 🔵 보류.
- 02 spec 과 동일 규칙. 본 spec 산출물 전부 동일 규칙 사용.

## 8. risk-register.md 갱신 후보 (실제 갱신은 tasks 단계)

본 spec 락 후 [`../_common/risk-register.md`](../_common/risk-register.md) 에 반영할 후보. R12 근거.

- 실제 갱신은 본 spec 의 [`./tasks.md`](./tasks.md) 단계.
- ID 는 추가 시점에 risk-register 의 다음 번호를 부여한다(예: `R-SEC-004` ~ `R-SEC-007`).
- 본 design 에서는 자리만 잡고 최종 ID 는 tasks 단계에서 락한다.

| ID (잠정) | Risk 요약 |
|-----------|-----------|
| R-SEC-XXX-a | Resource wildcard 또는 다른 service secret 까지 read 가능한 IAM 정책 권한 과다 |
| R-SEC-XXX-b | EC2 환경변수 / process 로그 / systemd `EnvironmentFile` 에 secret value 평문 노출 |
| R-SEC-XXX-c | EC2 안에 IAM access key id / secret access key 파일 저장 운영 실수 |
| R-SEC-XXX-d | Parameter / Secret naming 불일치로 application 런타임 시 read 실패 또는 잘못된 환경값 read |

#### Details — R-SEC-XXX-a IAM 정책 권한 과다

- Mitigation: Resource ARN 정확 한정(§4.2) + service prefix 화이트리스트 + Action 화이트리스트.
- Detection: IAM Access Analyzer / 정책 정적 검사 / [`./validation-checklist.md`](./validation-checklist.md) Resource wildcard 0건 점검.
- Rollback: 정책 detach + 이전 정책 복구.

#### Details — R-SEC-XXX-b EC2 secret value 평문 노출

- Mitigation: secret value 자체를 EnvironmentFile / log / dotfile 에 기록 금지. runtime read 만 허용. log mask 정책.
- Detection: log grep 점검(`APP_SECRET=`, `PASSWORD=` 패턴 0건).
  `aws sts get-caller-identity` 흐름이 Instance Role 기반 인지 점검.
- Rollback: 노출된 secret 즉시 rotate (KIS app secret / RDS password / Slack webhook 등).

#### Details — R-SEC-XXX-c EC2 access key 파일 저장 실수

- Mitigation: `~/.aws/credentials` 미존재 강제 점검, IMDSv2 강제, dotfile / EnvironmentFile access key 패턴 0건 점검.
- Detection: [`./validation-checklist.md`](./validation-checklist.md) 라벨 / 정기 점검.
- Rollback: 즉시 access key 폐기(IAM Console) + Instance Role 자격증명 만 사용 모드로 복귀.

#### Details — R-SEC-XXX-d naming 불일치

- 예: `paper` 가 `live` parameter 를 read.
- Mitigation: 본 spec naming 규칙(§2) 강제 + IaC / runbook / validation-checklist 에 명시 + `{env}` 분리 강제.
- Detection: 컨테이너 / EC2 startup health check 실패 시 알림.
  환경 식별자(`PORT_ENVIRONMENT`) 와 secret 경로 prefix 일치 점검.
- Rollback: 이름 정정 후 재배포. 잘못된 환경값으로 read 된 경우 즉시 process 정지 + secret rotation 검토.

각 리스크는 tasks 단계에서 아래 컬럼 형식에 맞춰 row 를 추가한다. R12.5 근거.

- 참조: [`../02-aws-network-and-rds/risk-register.md`](../_common/risk-register.md) 컬럼 형식
- 컬럼: Area / Risk / Impact / Probability / Mitigation / Detection / Rollback / Affected Spec / Status

## 9. followups-overview.md 갱신 후보 (실제 갱신은 tasks 단계)

본 spec 락 후 [`../_common/followups-overview.md`](../_common/followups-overview.md) 에 반영할 후보. R11 근거. 실제 갱신은 본 spec 의 [`./tasks.md`](./tasks.md) 단계.

### 9.1 본 spec(06) 결정의 입력 항목 (R11.2 근거)

| 후속 spec | 본 spec 06 결정의 입력 항목 |
|-----------|---------------------------|
| 03 marketconnector-ec2 | Instance Role 정책 매트릭스(§4.2) 그대로 사용. SSM Session Manager / CloudWatch Logs / 토큰 인계 절차는 03 책임 |
| 08 interest-crawler-and-preprocessor-ecs | Task Role 골격(§6.1 / §6.2) + crawler / preprocessor service prefix(`/portfolio/paper/crawler/*`, `/portfolio/paper/preprocessor/*`) |
| 04 strategy-batch-stepfunctions | Task Role 골격 + strategy service prefix(`/portfolio/paper/strategy/*`) + Step Functions IAM 정책(자동 재시도 금지 step 강제는 04 책임) |
| 05 port-view-ecs-and-runbook | Task Role 골격 + view service prefix(`/portfolio/paper/view/*`) |
| 09 strategy-research-batch | Task Role 골격 / AWS Batch Job Role + research service prefix(`/portfolio/paper/research/*`) |
| 10 cutover-and-validation-runbook | aws-live 환경 IAM 통합 시점. `/portfolio/live/...` prefix secret / parameter 등록과 Role 권한 매트릭스 락 |

### 9.2 본 spec(06) 1차 적용 환경 / 범위 / 범위 밖 (R11.1 근거)

- 1차 적용 환경: `aws-paper`.
- 1차 범위: MarketConnector EC2 Instance Role + Secrets Manager / SSM Parameter Store 등록(`marketconnector` / `rds` service prefix).
- 범위 밖: live rotation 자동화, GitHub Actions OIDC, 8개 MS full IAM 매트릭스, aws-live IAM.

### 9.3 06 ↔ 03 순서 (R11.4 근거)

- 06 이 secret / parameter / IAM 골격을 먼저 락 → 03 이 EC2 정식 운영(Connector / Flask / KIS API / RDS 접속) 입력으로 받음. 06 락 전에 03 의 EC2 정식 운영 단계는 진입하지 않는다.

## 10. 후속 phase 계획

본 spec design 락 이후의 산출물 책임 분담. R7 / R8 / R9 근거.

| 산출물 | 책임 (요약) | 본 spec 입력 |
|--------|-------------|--------------|
| [`./tasks.md`](./tasks.md) | design 결정과 _common 갱신 후보(§7 / §8 / §9) 를 task 단위로 분해 | §7 / §8 / §9 / [`./requirements.md`](./requirements.md) R10 / R11 / R12 |
| [`./runbook.md`](./runbook.md) | 운영자 직접 작업 절차서 (상세 아래) | §1 / §2 / §4 / §5 / [`./requirements.md`](./requirements.md) R7 |
| [`./validation-checklist.md`](./validation-checklist.md) | 4종 라벨 + 8개 점검 영역 체크리스트 (상세 아래) | §4 / §5 / [`./requirements.md`](./requirements.md) R8 |
| [`./operation-notes.md`](./operation-notes.md) | 일자별 누적 기록 (상세 아래) | §11 / [`./requirements.md`](./requirements.md) R9 |

#### Row Notes — runbook.md 단계

- 라벨: [실행] / [확인] / [준비] / [복구]
- 단계 순서
  - (a) Secrets Manager 등록
  - (b) SSM Parameter Store 등록
  - (c) Instance Role / Profile 생성
  - (d) 정책 attach
  - (e) EC2 instance 에 Profile attach
  - (f) EC2 안 secret / parameter read 검증
  - (g) Connector 재기동 후 KIS / RDS smoke test

#### Row Notes — validation-checklist.md 라벨 / 점검 영역

- 4종 라벨: `[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]`
- 8개 점검 영역
  - (1) Secrets Manager 등록 인벤토리
  - (2) SSM Parameter Store 등록 인벤토리
  - (3) Role / Instance Profile 존재 / attach 상태
  - (4) 정책 매트릭스(§4.2) 일치
  - (5) Resource wildcard 0건
  - (6) EC2 access key 파일 미존재
  - (7) `aws sts get-caller-identity` assumed-role ARN 일치
  - (8) Connector smoke test 통과

#### Row Notes — operation-notes.md 기록 정책

- 일자별 누적 기록.
- secret value 미기록 — 성공 / 실패만 기록.
- IAM Role / Policy 변경 기록 템플릿
  - 변경 일자 / 변경자 / 변경 사유 / 변경 전 · 후 항목 요약
  - JSON 본문 전체 인용 금지

각 산출물은 02 spec 의 동일 산출물 컨벤션(라벨 4종, 일자별 `## YYYY-MM-DD ...` 누적 형식, secret `[REDACTED]` 정책)을 그대로 따른다.

## 11. 안전 제약 (요약)

본 spec 의 모든 phase 에 적용. R14 근거.

- 아래 8개 MS 의 README / AGENTS.md / CHANGELOG / docs / worklog 와 소스 코드를 수정하지 않는다.
  - `port-view`, `port-marketconnector`
  - `port-interest-crawler`, `port-interest-preprocessor`
  - `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`
- 실제 AWS 리소스(Secrets Manager secret, SSM Parameter, IAM Role, IAM Policy, IAM Instance Profile, EC2 Instance Profile attach 변경, KMS Key 등) 를 만들거나 변경 / 삭제하지 않는다. 모든 실제 생성 / 수정 / 삭제는 운영자가 직접 수행한다.
- 8개 MS entrypoint 실행 금지. broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / 주문 / 체결 / Daily Batch / intraday monitor 호출 금지.
- 본 spec 산출물(design / tasks / runbook / validation-checklist / operation-notes) 어디에도 아래 항목을 적지 않는다.
  - 실제 secret value / KIS app key / KIS app secret / 계좌번호
  - RDS password / RDS endpoint hostname / account-id
  - 실제 secret ARN / 실제 KMS Key ARN
  - IAM access key id / secret access key / Slack webhook URL
- 모두 `[REDACTED]` 또는 placeholder(`<account-id>`, `<region>`, `<kms-key-id>`) 만 사용.
- `secretsmanager:GetSecretValue` 호출은 운영자만 수행한다. Kiro 자동 검증은 `secretsmanager:DescribeSecret` metadata 만 사용한다. R14.5 근거.
- live rotation 자동화 / CI/CD OIDC / full IAM 매트릭스 / aws-live 환경 IAM 은 본 spec 범위 밖. 후속 spec(07 / 03 / 08 / 04 / 05 / 09 / 10) 분담. R14.6 근거.

## Testing Strategy (참고)

본 spec 은 IaC / IAM 정책 / secret 보관 위치 결정 / 운영 절차서 산출물이다. 코드 / 순수 함수 / 입력 변동에 따라 행위가 달라지는 알고리즘이 없다. 따라서 property-based testing(PBT) 은 적용되지 않는다. 본 design 은 Correctness Properties 섹션을 포함하지 않는다.

본 spec 의 검증은 다음 두 형태로만 수행된다.

- 정책 정적 검사: 정책 JSON 의 Action / Resource 가 §4.2 매트릭스와 일치하는지(wildcard 사용 0건 포함) 점검. [`./validation-checklist.md`](./validation-checklist.md) (후속 phase) 책임.
- smoke test: 아래 두 축으로 점검.
  - EC2 안 metadata 호출 결과 점검: `aws sts get-caller-identity` / `aws secretsmanager describe-secret` / `aws ssm get-parameter`
  - Connector 재기동 후 KIS / RDS 호출 통과 여부 점검
- 책임: [`./runbook.md`](./runbook.md) / [`./validation-checklist.md`](./validation-checklist.md) (후속 phase).
- `secretsmanager:GetSecretValue` 호출은 운영자만 수행한다.
