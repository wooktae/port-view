# Requirements Document — 06-secrets-and-iam

## Introduction

이 spec은 PORT-STRATEGY-AI AWS Migration의 6번째 단계로, EC2 / ECS Task가 KIS 모의투자 API와 RDS `portfolio` DB에 접속할 때 사용하는 secret / parameter의 보관 위치, IAM Role / Policy 기반 최소 권한 read 정책, 그리고 운영자 절차 / 검증 / 운영 노트의 작성 기준을 확정한다.

선행 spec `02-aws-network-and-rds`의 1차 적용은 완료된 상태다. VPC / Subnet / SG / VPC Endpoint(Secrets Manager / SSM / CloudWatch Logs / ECR / S3) / RDS PostgreSQL / 7개 DB role(`marketconnector_app`, `view_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `research_app`, `execution_app`)이 운영자 직접 작업으로 적용되어 있다. MarketConnector EC2(Amazon Linux 2023, public subnet, EIP attach)도 이미 생성되어 있고, 본 spec 작업 시점 오전에 운영자는 shell 환경변수 기반으로 KIS paper API 조회 / RDS `marketconnector_app` 접속 / `connector_balance.py`, `connector_order_check.py` 실행 / Flask 조회성 endpoint smoke test를 통과시켰다.

본 spec의 1차 적용 환경은 `aws-paper`다. 본 spec이 다루는 핵심은 다음 한 가지로 좁힌다.

- MarketConnector EC2가 Access Key를 저장하지 않고, EC2 Instance Role 만으로 Secrets Manager / SSM Parameter Store에서 KIS / RDS / 일반 설정값을 read 하도록 보관 위치 / 권한 / 절차를 1차 확정한다.
- 03-marketconnector-ec2 / 08-interest-crawler-and-preprocessor-ecs가 이후 ECS Fargate Task Role로 동일 secret / parameter 패턴을 재사용 가능하도록 최소 골격만 남긴다.

본 spec은 다음을 포함하지 않는다(범위 외).

- live rotation 자동화(Lambda rotation, scheduled rotation).
- CI/CD OIDC role(GitHub Actions OIDC) 설계 — 07 spec.
- 8개 MS 전체에 대한 full IAM 매트릭스 — 08 / 04 / 05 / 09 spec에서 패턴 재사용 형태로 진행.
- aws-live 환경의 secret 보관 / IAM 매트릭스 — 10-cutover-and-validation-runbook(예정)에서 통합.
- 8개 MS 소스 코드, README, AGENTS.md, CHANGELOG, docs, worklog 수정.

본 spec(06 폴더) 안 산출물은 [`requirements.md`](./requirements.md), [`design.md`](./design.md), [`tasks.md`](./tasks.md), [`runbook.md`](./runbook.md), [`validation-checklist.md`](./validation-checklist.md), [`operation-notes.md`](./operation-notes.md)와 보조 문서(필요 시 `traceability-matrix.md`)를 포함한다. 본 phase에서는 `requirements.md`만 작성하고, 나머지 문서는 후속 phase에서 만든다. 루트 공통 참조 / 갱신 후보 문서는 [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/followups-overview.md`](../_common/followups-overview.md), [`../_common/risk-register.md`](../_common/risk-register.md), [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md)이며 실제 갱신은 본 spec의 tasks 단계에서 수행한다.

선행 spec: [`../01-aws-migration-foundation`](../01-aws-migration-foundation), [`../02-aws-network-and-rds`](../02-aws-network-and-rds). 본 spec의 결정은 [`../03-marketconnector-ec2`](../03-marketconnector-ec2)(예정), [`../08-interest-crawler-and-preprocessor-ecs`](../08-interest-crawler-and-preprocessor-ecs)(예정), [`../04-strategy-batch-stepfunctions`](../04-strategy-batch-stepfunctions)(예정), [`../05-port-view-ecs-and-runbook`](../05-port-view-ecs-and-runbook)(예정), [`../09-strategy-research-batch`](../09-strategy-research-batch)(예정), [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook)(예정)의 입력으로 사용된다.

본 문서에는 실제 secret / password / token / KIS app key / KIS app secret / 계좌번호 / webhook URL / access key / DB endpoint hostname / account-id 값을 적지 않는다. 모두 `[REDACTED]` 또는 placeholder만 사용한다.

## Glossary

- 단일 portfolio DB: 모든 MS가 공유하는 PostgreSQL 데이터베이스 `portfolio`. 02 spec에서 RDS for PostgreSQL로 1차 적용 완료.
- MarketConnector EC2: aws-paper의 KIS Connector를 실행하는 EC2 인스턴스. 02 spec 시점에 RDS restore runner 용도로 먼저 생성되었고, 03 spec에서 KIS Connector / Flask app 운영 형태로 정식 사용한다.
- Instance Role: EC2 인스턴스에 attach되는 IAM Role. EC2 안에서 동작하는 프로세스가 access key / secret key 없이도 IAM Role 권한으로 AWS API를 호출할 수 있게 한다.
- Task Role: ECS Task 정의에 attach되는 IAM Role. ECS Fargate / EC2 Task가 access key 없이 IAM Role 권한으로 AWS API를 호출할 수 있게 한다. 본 spec에서는 ECS Task가 아직 없으므로 패턴 정의만 남긴다.
- Secrets Manager: AWS Secrets Manager. 본 spec에서는 KIS app key / app secret / paper 계좌번호 / RDS DB role 비밀번호 / RDS master 비밀번호 / Slack webhook(결정 후) 같은 값 단위 secret 보관소로 사용한다.
- SSM Parameter Store: AWS Systems Manager Parameter Store. 본 spec에서는 KIS base URL / Connector host / port / debug / account product code / 환경 식별자 같은 일반 설정값 보관소로 사용한다.
- SSM SecureString: SSM Parameter Store의 KMS 암호화 parameter. 비용을 줄이고 싶은 일부 secret 후보 보관소로 사용 가능. 본 spec에서는 Slack webhook 후보로 평가한다.
- 최소 권한 read: IAM Policy의 Resource 절을 정확한 secret ARN / parameter ARN로 한정하고, `*` 와일드카드를 쓰지 않는 정책. `secretsmanager:GetSecretValue` / `ssm:GetParameter` / `ssm:GetParameters` 외 다른 action을 부여하지 않는다.
- Access Key 미사용 원칙: EC2 / ECS Task가 자기 안에 IAM access key / secret access key를 파일 / 환경변수 / config 형태로 저장하지 않고, Instance Role 또는 Task Role 만으로 AWS API 호출을 수행해야 한다는 정책.
- naming 규칙: secret / parameter 이름이 환경(`paper` / `live`), 서비스(`marketconnector` / `crawler` / `view` 등), 항목(`kis-app-key` / `db-password` 등)을 prefix / 경로로 일관되게 표현해야 한다는 규칙. 본 spec에서 1차 확정한다.

## Requirements

### Requirement 1: Secrets / Parameters 분류 기준 확정 (OD-SEC-001)

**Objective**: As 운영자, I want KIS / RDS / 일반 설정 / Slack webhook 값별로 Secrets Manager 또는 SSM Parameter Store 중 어디에 보관할지 분류 기준을 확정하기, so that 운영자가 어떤 항목을 어디에 넣어야 하는지 spec / runbook 만 보고 결정할 수 있다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면, THE design.md SHALL Secrets Manager 보관 후보 목록에 KIS app key, KIS app secret, KIS paper 계좌번호 관련 값(`PAPER_ACNT`, `ACNT_PRDT_CD`)을 포함해야 한다.
2. WHEN design.md가 작성되면, THE design.md SHALL Secrets Manager 보관 후보 목록에 RDS `marketconnector_app` 접속정보(host, port, db name, user, password)를 포함하고, RDS master(`portfolio_admin`) 비밀번호도 별도 항목으로 명시해야 한다.
3. WHEN design.md가 작성되면, THE design.md SHALL SSM Parameter Store 보관 후보 목록에 KIS base URL(`BASE_URL` / `https://openapivts.koreainvestment.com:29443` 같은 환경별 endpoint), Connector Flask host / port / debug, `PORT_ENVIRONMENT`, `PORT_BROKER_NAME`, `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION` 같은 일반 설정 항목을 포함해야 한다.
4. WHEN Slack webhook 보관 위치를 다루는 경우, THE design.md SHALL Secrets Manager(권고)와 SSM SecureString(절감안) 두 후보를 비교하고, OD-OBS-004 결정과 본 spec에서 최종 락할 결정값을 한 곳에서 명시해야 한다.
5. WHEN 분류 기준을 다루는 경우, THE design.md SHALL "비밀이 노출되면 외부 발신 / 외부 인증 / 외부 자금 이동이 가능한 값"은 Secrets Manager로 보낸다는 1차 분류 기준을 명문화해야 한다.
6. WHEN 분류 기준을 다루는 경우, THE design.md SHALL "노출되어도 외부 행위는 직접 일으키지 않지만 환경에 따라 값이 달라지는 일반 설정값"은 SSM Parameter Store로 보낸다는 1차 분류 기준을 명문화해야 한다.
7. WHERE 운영자가 비용 절감을 위해 일부 secret을 SSM SecureString으로 옮기길 원하는 경우, THE design.md SHALL 옮길 수 있는 후보(예: Slack webhook)와 옮기면 안 되는 후보(KIS app secret, KIS app key, RDS 비밀번호)를 명시 분리해야 한다.

### Requirement 2: Naming / 경로 규칙 1차 확정

**Objective**: As 운영자, I want secret / parameter 이름과 경로 규칙을 1차 확정하기, so that 환경(`paper` / `live`)이나 서비스가 늘어나도 naming 충돌 없이 secret을 찾을 수 있고, IAM Resource 절을 ARN prefix로 깔끔하게 한정할 수 있다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면, THE design.md SHALL Secrets Manager 이름 규칙 `/portfolio/{env}/{service}/{item}` 형식을 1차 권고로 명시해야 한다(예: `/portfolio/paper/marketconnector/kis-app-key`, `/portfolio/paper/marketconnector/kis-app-secret`, `/portfolio/paper/rds/master`, `/portfolio/paper/rds/marketconnector-app`).
2. WHEN design.md가 작성되면, THE design.md SHALL SSM Parameter Store 이름 규칙 `/portfolio/{env}/{service}/{item}` 형식을 1차 권고로 명시해야 한다(예: `/portfolio/paper/marketconnector/kis-base-url`, `/portfolio/paper/marketconnector/connector-host`, `/portfolio/paper/marketconnector/connector-port`).
3. WHEN env 표기를 다루는 경우, THE design.md SHALL `paper` / `live` 두 값을 사용하고 `dev` / `test` 같은 추가 env는 본 spec 범위 밖으로 명시해야 한다(OD-ENV-001 / OD-ENV-003 입력).
4. WHEN service 표기를 다루는 경우, THE design.md SHALL 본 spec 시점에서 `marketconnector`, `rds`, `view`, `strategy`, `crawler`, `preprocessor`, `research`, `ops` 명칭을 service prefix 후보로 명시하고 본 spec(06)이 1차 확정 대상으로 보는 것은 `marketconnector` 와 `rds` 두 개로 한정해야 한다.
5. IF 기존 02 spec / `operation-notes.md`에 이미 등록된 secret 이름이 본 규칙과 다른 경우, THEN THE design.md SHALL 기존 이름(예: `/portfolio/paper/rds/master`)을 그대로 유지하고 본 규칙은 신규 항목부터 적용한다는 호환 정책을 명시해야 한다.
6. WHEN naming 규칙을 다루는 경우, THE design.md SHALL 실제 secret 값 / endpoint hostname / 계좌번호를 secret / parameter 이름 자체에 절대 포함하지 않는다는 정책을 명문화해야 한다.

### Requirement 3: 환경변수 키 호환성 유지 (OD-DB-003)

**Objective**: As 운영자, I want secret / parameter 도입 후에도 8개 MS의 기존 환경변수 키 이름이 그대로 유지되기, so that 코드 / README / AGENTS.md 수정 없이 secret 값만 외부화할 수 있다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면, THE design.md SHALL 다음 환경변수 키가 그대로 유지된다는 정책을 명시해야 한다: `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`, `PORTFOLIO_DB_NAME`, `PORT_ACCOUNT_NO`, `PORT_BROKER_NAME`, `PORT_ENVIRONMENT`, `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION`, `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT`.
2. WHEN design.md가 작성되면, THE design.md SHALL `port-marketconnector` 의 `config.py`가 사용하는 KIS 관련 식별자(`APP_KEY`, `APP_SECRET`, `BASE_URL`, `PAPER_ACNT`, `ACNT_PRDT_CD`)에 대해, 코드 변경 없이 환경변수로 주입 가능하도록 외부화 후보 키 이름과 매핑 표를 1차 권고로 명시해야 한다.
3. WHEN design.md가 작성되면, THE design.md SHALL secret / parameter 값이 EC2 / ECS 컨테이너의 환경변수로 주입되는 시점이 process 시작 시점 이라는 점, 런타임 중 자동 rotation 반영은 본 spec 범위 외라는 점을 명시해야 한다.
4. IF 운영자가 본 spec 시점 이후에도 shell 환경변수에 KIS app key / app secret / 계좌번호를 직접 export 한 채 운영하길 원하는 경우, THEN THE design.md SHALL 이를 임시 운영 모드로 분류하고 정상 운영에서는 Secrets Manager 경유 주입을 표준으로 한다는 정책을 명시해야 한다.

### Requirement 4: MarketConnector EC2 Instance Role 최소 권한 (OD-SEC-001 / R-SEC-001 연계)

**Objective**: As 운영자, I want MarketConnector EC2 Instance Role에 필요한 secret / parameter read 권한만 부여받기, so that EC2 안에 access key를 저장하지 않고도 운영 가능하면서 권한 과다 위험(R-SEC-001 인접)을 차단한다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면, THE design.md SHALL MarketConnector EC2 Instance Role 이름 후보(예: `portfolio-paper-marketconnector-ec2-role`)와 그에 attach되는 Instance Profile 이름 후보를 명시해야 한다.
2. WHEN Instance Role의 Secrets Manager 권한을 다루는 경우, THE design.md SHALL Action을 `secretsmanager:GetSecretValue` 와 `secretsmanager:DescribeSecret` 두 개로만 한정하고, Resource 절은 다음 secret ARN 만 포함해야 한다: KIS app key, KIS app secret, KIS paper 계좌번호 관련(`PAPER_ACNT` / `ACNT_PRDT_CD` 가 secret으로 분류된 경우), RDS `marketconnector_app` 접속정보 secret. 다른 secret(다른 MS의 DB role 비밀번호 등)에 대한 권한은 부여하지 않는다.
3. WHEN Instance Role의 SSM Parameter Store 권한을 다루는 경우, THE design.md SHALL Action을 `ssm:GetParameter`, `ssm:GetParameters`, `ssm:GetParametersByPath` 만으로 한정하고, Resource 절은 `/portfolio/paper/marketconnector/*` 경로 prefix만 포함하도록 명시해야 한다.
4. WHEN Resource 절을 다루는 경우, THE design.md SHALL `Resource: "*"` 형태의 wildcard 정책을 절대 사용하지 않는다는 정책을 명문화해야 한다.
5. WHEN 문서에 IAM Role / Policy ARN이 등장하는 경우, THE design.md SHALL ARN 자리를 placeholder(`arn:aws:iam::<account-id>:role/portfolio-paper-marketconnector-ec2-role`) 또는 `[REDACTED]`로만 표기해야 하고, 실제 account-id / 실제 secret ARN을 그대로 적지 않아야 한다.
6. IF Instance Role에 KMS Decrypt 권한이 필요한 경우(SSM SecureString 또는 Secrets Manager CMK 사용 시), THEN THE design.md SHALL Action을 `kms:Decrypt` 로 한정하고 Resource 절은 본 spec에서 사용 결정된 KMS Key ARN만 포함하도록 명시해야 한다. CMK 미사용 / `aws/secretsmanager` default key 만 사용하는 경우 본 항목은 적용 외 임을 명시한다.
7. WHEN CloudWatch Logs / SSM Session Manager 등 보조 권한을 다루는 경우, THE design.md SHALL 본 spec(06)의 책임 범위는 secret / parameter read 권한이며, SSM Session Manager 접속용 managed policy(예: `AmazonSSMManagedInstanceCore`)는 03 spec(MarketConnector EC2 운영) 영역으로 분리해 명시해야 한다.

### Requirement 5: EC2 내부 Access Key 미사용 원칙

**Objective**: As 운영자, I want MarketConnector EC2 안에 IAM access key / secret access key가 절대 저장되지 않도록 정책을 명문화하기, so that EC2가 탈취되더라도 access key 자체가 외부로 빠져나가지 않는다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면, THE design.md SHALL "MarketConnector EC2 안의 어떤 파일 / 환경변수 / 사용자 home 디렉터리 / systemd unit / config 파일에도 IAM access key id 와 secret access key를 저장하지 않는다"는 원칙을 명문화해야 한다.
2. WHEN design.md가 작성되면, THE design.md SHALL EC2 위 어떤 프로세스도 `~/.aws/credentials` 형식의 long-lived access key 파일에 의존하지 않고, EC2 metadata service(IMDSv2) 기반 Instance Role 자격증명만 사용해야 한다는 정책을 명시해야 한다.
3. WHEN runbook.md가 후속 phase에서 작성되면, THE runbook.md SHALL EC2 안에서 `aws sts get-caller-identity` 와 `aws configure list` 결과로 Instance Role 자격증명만 사용 중인지 검증하는 절차를 포함해야 한다.
4. IF 운영자가 임시로 EC2 안에 access key를 둬야 하는 비상 시나리오를 발견한 경우, THEN THE design.md SHALL 본 spec 임의 결정 대신 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-SEC 카테고리에 변경 제안만 기록하고 운영자 승인 후에만 적용한다는 정책을 명시해야 한다.
5. WHEN 본 spec 산출물이 access key 와 관련된 어떤 값을 다루는 경우, THE 산출물 SHALL 실제 access key id, secret access key 값을 절대 적지 않고 `[REDACTED]` 만 사용해야 한다.

### Requirement 6: ECS Task Role 재사용 패턴 (Connector / Crawler / Preprocessor / Strategy / View / Research 공통)

**Objective**: As 운영자, I want 03-marketconnector-ec2 / 08-interest-crawler-and-preprocessor-ecs / 04-strategy-batch-stepfunctions / 05-port-view-ecs-and-runbook / 09-strategy-research-batch가 동일 패턴(Task Role 기반 secret / parameter read)을 재사용하도록 골격을 받기, so that 후속 spec에서 IAM 매트릭스 설계를 처음부터 다시 하지 않아도 된다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면, THE design.md SHALL ECS Fargate / EC2 Task가 사용하는 Task Role 골격(Action 목록과 Resource ARN 패턴)을 명시하고, MarketConnector EC2 Instance Role과 동일한 최소 권한 read 정책 형식을 따른다는 점을 명시해야 한다.
2. WHEN design.md가 작성되면, THE design.md SHALL ECS Task Execution Role(이미지 pull / Secrets Manager 환경변수 주입용)과 Task Role(애플리케이션 런타임 secret / parameter read용)을 분리해 정의하고, 각각의 책임을 한 줄씩 명시해야 한다.
3. WHEN design.md가 작성되면, THE design.md SHALL 향후 ECS Task가 Secrets Manager의 secret 값을 환경변수로 주입할 때 사용하는 ECS `secrets` 필드 패턴(`name=ENV_KEY, valueFrom=<secret ARN>`)을 1차 권고로 명시하고, 실제 ARN 자리를 placeholder로만 표기해야 한다.
4. WHEN 본 spec이 후속 spec과의 책임 분리를 다루는 경우, THE design.md SHALL 03 / 08 / 04 / 05 / 09 spec이 본 spec의 IAM 골격을 입력으로 받아 service-별 Resource ARN 만 채운다는 분담 원칙을 명시해야 한다.
5. WHERE 본 spec 시점(06)에 ECS Task Role 자체를 실제 생성하지 않는 경우, THE design.md SHALL Task Role 정의는 패턴(템플릿)으로만 남기고 실제 생성은 03 / 08 spec의 책임으로 명시해야 한다.

### Requirement 7: 운영자 절차(runbook.md) 산출물 요구

**Objective**: As 운영자, I want 본 spec(06) design / tasks 단계에서 AWS Console 직접 진행이 가능한 절차서를 받기, so that IaC 작성 없이도 secret 등록 / IAM Role 생성 / EC2 attach / 검증을 한 손으로 따라갈 수 있다.

#### Acceptance Criteria

1. WHEN 본 spec의 후속 phase가 진행되면, THE 후속 phase SHALL `runbook.md` 산출물을 본 spec 폴더 안에 생성해야 한다.
2. WHEN runbook.md가 작성되면, THE runbook.md SHALL 다음 단계를 단계별로 분해해야 한다: (a) Secrets Manager에 KIS / RDS secret 등록, (b) SSM Parameter Store에 KIS base URL / Connector 일반 설정 등록, (c) MarketConnector EC2 Instance Role / Instance Profile 생성, (d) Instance Role에 최소 권한 정책 attach, (e) EC2 instance에 Instance Profile attach, (f) EC2 안에서 secret / parameter read 검증, (g) Connector 재기동 후 KIS / RDS smoke test.
3. WHEN runbook.md의 각 단계를 다루는 경우, THE runbook.md SHALL 각 단계에 [실행] / [확인] / [준비] / [복구] 라벨을 붙이고, 운영자가 AWS Console에서 누를 항목, 입력값(민감정보는 `[REDACTED]`), 검증 방법, 실패 시 조치를 포함해야 한다.
4. WHEN runbook.md가 secret 등록 단계를 다루는 경우, THE runbook.md SHALL 실제 KIS app key / app secret / 계좌번호 / DB password 값을 본 문서에 적지 않고 운영자가 Console에 직접 입력하도록 안내해야 한다.
5. WHEN runbook.md가 비용 영향이 있는 단계(Secrets Manager 항목 추가, KMS CMK 도입 등)를 다루는 경우, THE runbook.md SHALL 해당 단계에 "approval required" 표시와 비용 영향 한 줄 요약을 추가해야 한다.
6. WHEN runbook.md가 작성되면, THE runbook.md SHALL 02 spec runbook 의 라벨 / 형식 / 보안 원칙(secret `[REDACTED]`, 8개 MS 코드 / docs 미수정, 외부 호출 금지)을 그대로 따라야 한다.

### Requirement 8: 검증 항목(validation-checklist.md) 산출물 요구

**Objective**: As 운영자, I want 본 spec design / tasks 단계에서 4종 라벨 기반 검증 체크리스트를 받기, so that 02 spec 과 동일한 점검 흐름을 그대로 사용할 수 있다.

#### Acceptance Criteria

1. WHEN 본 spec의 후속 phase가 진행되면, THE 후속 phase SHALL `validation-checklist.md` 산출물을 본 spec 폴더 안에 생성해야 한다.
2. WHEN validation-checklist.md가 작성되면, THE validation-checklist.md SHALL 다음 4종 라벨만 사용해야 한다: `[O]`, `[X]`, `[Kiro 후속 작업 필요]`, `[운영자 확인 필요]`. 그 외 라벨(예: `[확인 필요]`)은 사용하지 않는다.
3. WHEN validation-checklist.md가 점검 항목을 다루는 경우, THE validation-checklist.md SHALL 다음 영역을 모두 포함해야 한다: (a) Secrets Manager 등록 인벤토리(이름 / KMS / metadata, 값은 절대 조회 금지), (b) SSM Parameter Store 등록 인벤토리, (c) MarketConnector EC2 Instance Role / Instance Profile 존재 / attach 상태, (d) Instance Role 정책의 Action / Resource 가 본 spec design 매트릭스와 일치하는지, (e) Resource wildcard 사용 0건 검증, (f) EC2 안 access key 파일(`~/.aws/credentials`, `~/.aws/config` 의 access key) 미존재 검증, (g) `aws sts get-caller-identity` 결과가 Instance Role assumed-role ARN 인지 검증, (h) Connector smoke test 통과 여부.
4. WHEN validation-checklist.md가 secret 항목을 다루는 경우, THE validation-checklist.md SHALL `secretsmanager:GetSecretValue` 호출은 운영자만 수행하고 Kiro 자동 검증은 `secretsmanager:DescribeSecret` metadata 만 사용한다는 점을 명시해야 한다.
5. WHEN validation-checklist.md가 IAM 점검 항목을 다루는 경우, THE validation-checklist.md SHALL 본 spec design 의 Action / Resource 매트릭스와 실제 정책 JSON을 비교 가능한 형태로 항목을 분리해야 한다.

### Requirement 9: 운영 노트(operation-notes.md) 템플릿 요구

**Objective**: As 운영자, I want 본 spec design / tasks 단계에서 실행 결과를 누적 기록할 수 있는 운영 노트 템플릿을 받기, so that 실제 secret 값은 절대 기록하지 않고 결과만 안전하게 누적할 수 있다.

#### Acceptance Criteria

1. WHEN 본 spec의 후속 phase가 진행되면, THE 후속 phase SHALL `operation-notes.md` 산출물을 본 spec 폴더 안에 생성해야 한다.
2. WHEN operation-notes.md가 작성되면, THE operation-notes.md SHALL 실제 secret value, KIS app key / app secret, 계좌번호, RDS password, RDS endpoint hostname, account-id, 실제 secret ARN, IAM access key id 를 절대 기록하지 않고 모두 `[REDACTED]` 또는 placeholder로만 표기해야 한다.
3. WHEN operation-notes.md가 secret 조회 결과를 다루는 경우, THE operation-notes.md SHALL secret value 자체가 아닌 "조회 성공 / 실패", "마지막 갱신 시각", "운영자가 직접 확인했다는 사실"만 기록 가능한 템플릿이어야 한다.
4. WHEN operation-notes.md가 IAM Role / Policy 변경을 다루는 경우, THE operation-notes.md SHALL 변경 일자, 변경자, 변경 사유, 변경 전 / 후 정책 항목 요약(JSON 본문 전체 인용은 금지)을 기록 가능한 템플릿이어야 한다.
5. WHEN operation-notes.md가 작성되면, THE operation-notes.md SHALL 실제 AWS 리소스 생성 / 수정 / 삭제는 운영자가 직접 수행하고 Kiro는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행한다는 작업 분담 원칙을 본문에 포함해야 한다.
6. WHEN operation-notes.md가 작성되면, THE operation-notes.md SHALL 02 spec operation-notes의 일자별 누적 기록 형식(`## YYYY-MM-DD ...`)을 그대로 따라야 한다.

### Requirement 10: 공통 결정 문서 갱신 후보 — operator-decisions.md

**Objective**: As 운영자, I want 본 spec이 락하는 결정과 새로 추가하는 결정 후보를 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 갱신 후보로 명시 받기, so that 후속 spec(03 / 08 / 04 / 05 / 09 / 10)이 본 결정을 입력으로 안전하게 사용할 수 있다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면, THE design.md SHALL OD-SEC-001 (Secrets 보관 위치)을 본 spec(06)에서 🔴 미정 → 🟢 확정 또는 🟡 잠정으로 락하는 후보 결정값을 명시해야 한다.
2. WHEN design.md가 작성되면, THE design.md SHALL OD-OBS-004 (Slack webhook 보관 위치)를 본 spec(06)에서 🔴 미정 → 🟢 확정 또는 🟡 잠정으로 락하는 후보 결정값을 명시해야 한다.
3. WHEN design.md가 작성되면, THE design.md SHALL 신규 결정 후보 OD-SEC-XXX(예: OD-SEC-005)로 "MarketConnector EC2 / 8개 MS 모두 Access Key 미사용 원칙"을 명시해야 한다.
4. WHEN design.md가 작성되면, THE design.md SHALL 신규 결정 후보 OD-SEC-XXX(예: OD-SEC-006)로 "EC2 / ECS IAM Role 기반 secret / parameter read 원칙(최소 권한, Resource wildcard 금지)"을 명시해야 한다.
5. WHERE OD-SEC-002 (RDS master password 보관 = Secrets Manager)와 OD-SEC-003 (KIS access_token 보관 위치)이 본 spec 결정과 정합되는 경우, THE design.md SHALL 두 결정의 상태를 본 spec에서 어떻게 갱신해야 하는지(잠정 → 확정 여부, 또는 그대로 유지) 한 줄씩 명시해야 한다.
6. WHEN tasks.md가 후속 phase에서 작성되면, THE tasks.md SHALL [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 갱신 작업을 별도 task로 분리하고 본 spec phase 안에서만 수행해야 한다.
7. WHEN 본 spec 산출물이 결정 항목을 기록하는 경우, THE 산출물 SHALL Status 라벨 영문 기준값(CONFIRMED / TENTATIVE / TBD / DEFERRED)과 표시용 한글/색상(🟢 확정 / 🟡 잠정 / 🔴 미정 / 🔵 보류) 규칙을 02 spec 과 동일하게 사용해야 한다.

### Requirement 11: 공통 후속 문서 갱신 후보 — followups-overview.md

**Objective**: As 운영자, I want 본 spec(06) 진행 상태를 [`../_common/followups-overview.md`](../_common/followups-overview.md)에 반영하는 갱신 후보를 받기, so that 다른 후속 spec(03 / 08 / 04 / 05 / 09 / 10) 진입 시점에 06이 어디까지 진행됐는지 한눈에 파악할 수 있다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면, THE design.md SHALL [`../_common/followups-overview.md`](../_common/followups-overview.md) 의 06-secrets-and-iam 섹션에 본 spec의 1차 적용 환경(`aws-paper`), 1차 범위(MarketConnector EC2 Instance Role + Secrets Manager / SSM 등록), 범위 외(live rotation 자동화, OIDC, full IAM 매트릭스)를 추가하는 갱신 후보를 명시해야 한다.
2. WHEN design.md가 작성되면, THE design.md SHALL 본 spec 결정이 03 / 08 / 04 / 05 / 09 spec의 입력으로 사용되는 항목(Task Role 패턴, secret naming 규칙, Resource wildcard 금지 정책)을 followups-overview.md 갱신 후보로 명시해야 한다.
3. WHEN tasks.md가 후속 phase에서 작성되면, THE tasks.md SHALL [`../_common/followups-overview.md`](../_common/followups-overview.md) 갱신 작업을 별도 task로 분리해야 한다.
4. WHEN followups-overview.md 갱신 후보를 다루는 경우, THE design.md SHALL 06이 03 와 어떤 순서로 묶이는지(06이 먼저 락된 secret / parameter / IAM 골격을 03 EC2 본격 운영이 입력으로 받음)를 한 줄로 명시해야 한다.

### Requirement 12: 공통 후속 문서 갱신 후보 — risk-register.md

**Objective**: As 운영자, I want 본 spec에서 새로 식별 / 강화하는 리스크를 [`../_common/risk-register.md`](../_common/risk-register.md) 갱신 후보로 받기, so that secret 권한 / EC2 환경변수 노출 / Access Key 저장 실수 / naming 불일치 같은 위험이 누적 표에서 추적된다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면, THE design.md SHALL 신규 리스크 후보 `R-SEC-XXX` "Secrets Manager / SSM IAM 권한 과다(Resource wildcard, 다른 service secret까지 read 가능)"를 갱신 후보로 명시해야 한다.
2. WHEN design.md가 작성되면, THE design.md SHALL 신규 리스크 후보 `R-SEC-XXX` "EC2 환경변수 또는 process 로그에 secret value가 평문 노출"를 갱신 후보로 명시해야 한다.
3. WHEN design.md가 작성되면, THE design.md SHALL 신규 리스크 후보 `R-SEC-XXX` "EC2 안에 IAM access key id / secret access key 파일이 저장되는 운영 실수"를 갱신 후보로 명시해야 한다.
4. WHEN design.md가 작성되면, THE design.md SHALL 신규 리스크 후보 `R-SEC-XXX` "Parameter / Secret naming 불일치로 application 런타임 시 read 실패 또는 잘못된 환경값 read"를 갱신 후보로 명시해야 한다.
5. WHEN 신규 리스크 후보를 다루는 경우, THE design.md SHALL 각 리스크에 대해 mitigation, detection, rollback 한 줄씩을 명시해야 한다(02 spec risk-register 컬럼 형식과 동일).
6. WHEN tasks.md가 후속 phase에서 작성되면, THE tasks.md SHALL [`../_common/risk-register.md`](../_common/risk-register.md) 갱신 작업을 별도 task로 분리해야 한다.

### Requirement 13: 후속 spec 재사용성 (03 / 08 우선)

**Objective**: As 운영자, I want 03-marketconnector-ec2 와 08-interest-crawler-and-preprocessor-ecs(향후 ECS 포팅)에서 동일 패턴(Instance Role / Task Role 기반 secret read)을 그대로 재사용하기, so that 같은 IAM / secret naming 결정을 spec마다 다시 만들지 않는다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면, THE design.md SHALL 03-marketconnector-ec2 가 본 spec 의 Instance Role 정책을 그대로 입력으로 받아 EC2 정식 운영(Connector / Flask / KIS API / RDS 접속) 단계에 적용한다는 책임 분담을 명시해야 한다.
2. WHEN design.md가 작성되면, THE design.md SHALL 08-interest-crawler-and-preprocessor-ecs 가 본 spec 의 Task Role 골격을 그대로 입력으로 받아 ECS Fargate Task Role(crawler 전용 / preprocessor 전용)에 적용한다는 책임 분담을 명시해야 한다.
3. WHEN design.md가 작성되면, THE design.md SHALL 04 / 05 / 09 spec도 같은 Task Role 골격을 입력으로 사용한다는 점을 명시하되, 각 spec의 secret / parameter Resource ARN 채움은 해당 spec phase 에서 진행한다는 분담을 명시해야 한다.
4. WHEN design.md가 작성되면, THE design.md SHALL 본 spec(06) 이후 후속 spec이 변경하지 말아야 할 결정(naming 규칙, env prefix `paper` / `live`, Resource wildcard 금지 원칙)을 한 줄씩 명시해야 한다.

### Requirement 14: 본 spec의 안전 제약

**Objective**: As 운영자, I want 본 spec 작업이 코드 / 운영 데이터 / AWS 리소스 / 외부 호출을 변경하지 않도록 명시적으로 제한하기.

#### Acceptance Criteria

1. WHEN 본 spec의 모든 phase가 진행되는 동안, THE 작업 SHALL 8개 MS의 README / AGENTS.md / CHANGELOG / docs / worklog와 소스 코드를 수정하지 않아야 한다.
2. WHEN 본 spec의 모든 phase가 진행되는 동안, THE 작업 SHALL 실제 AWS 리소스(Secrets Manager secret, SSM Parameter, IAM Role, IAM Policy, IAM Instance Profile, EC2 attach 변경 등)를 만들거나 변경하지 않아야 한다. 모든 실제 생성 / 수정 / 삭제는 운영자가 직접 수행한다.
3. WHEN 본 spec의 모든 phase가 진행되는 동안, THE 작업 SHALL 8개 MS entrypoint, broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / 주문 / 체결 / Daily Batch / intraday monitor 호출을 실행하지 않아야 한다.
4. WHEN 본 spec 산출물이 secret 을 다루는 경우, THE 산출물 SHALL 모든 실제 secret value, password, KIS app key, KIS app secret, 계좌번호, token, webhook URL, access key id, secret access key, RDS endpoint hostname, account-id, 실제 secret ARN 자리에 `[REDACTED]` 만 사용하고 실제 값을 적지 않아야 한다.
5. WHEN secret 조회를 다루는 경우, THE 산출물 SHALL `secretsmanager:GetSecretValue` 호출은 운영자만 수행하고 Kiro 자동 검증은 `DescribeSecret` metadata만 사용한다는 점을 명시해야 한다.
6. WHEN 본 spec이 후속 spec과의 분리 정책을 다루는 경우, THE 산출물 SHALL live rotation 자동화 / CI/CD OIDC / full IAM 매트릭스 / aws-live 환경 IAM 은 본 spec 범위 외임을 명시해야 한다.
7. WHEN 본 phase(requirements) 가 진행되는 동안, THE 작업 SHALL 본 spec 폴더 안에 `requirements.md` 외 다른 산출물(`design.md`, `tasks.md`, `runbook.md`, `validation-checklist.md`, `operation-notes.md`)을 생성하지 않아야 한다. 이들은 후속 phase 의 책임이다.
