# Requirements Document — 02-aws-network-and-rds

## Introduction

이 spec은 PORT-STRATEGY-AI 8개 MS가 사용할 AWS 네트워크 기반(VPC / Subnet / Route Table / NAT-free 전략 / VPC Endpoint / Security Group)과 단일 PostgreSQL `portfolio` 데이터베이스를 RDS for PostgreSQL로 옮기는 전환 절차를 확정한다.

운영자 결정 사항이 본 spec 시점에 변경되었으므로 본 문서는 다음 환경 모델을 전제로 한다.

- `local-dev`: 기존 로컬 PostgreSQL 환경 유지. AWS 리소스 없음. 개발 / 단위 검증은 local-dev에서 수행.
- `aws-paper`: 1차 AWS 구축 환경. KIS 모의투자 기반 실전 리허설. RDS / ECS / EC2 marketconnector / CloudWatch / Slack / Secrets 구성 포함. 초기 주문 차단, 이후 자동 BUY/SELL E2E 검증.
- `aws-live`: paper 검증 후 후속 구축. 실제 계좌 연결. 초기 자동주문 금지, 후보 생성 + View 수동 승인 중심 운영. 충분한 검증 후 제한적 자동화 검토.

본 spec(02 폴더) 내부 산출물:

- [`requirements.md`](./requirements.md)
- [`design.md`](./design.md)
- [`tasks.md`](./tasks.md)
- [`decision-matrix.md`](./decision-matrix.md)

루트 공통 참조 문서:

- [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md)
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md)
- [`../_common/ms-aws-service-decision-matrix.md`](../_common/ms-aws-service-decision-matrix.md)
- [`../_common/cost-simulation.md`](../_common/cost-simulation.md)

본 spec은 위 문서를 만들거나 참조하는 작업만 다룬다. 실제 VPC / RDS / Secrets 리소스 생성, IaC 작성, 8개 MS 코드 / 기존 README / AGENTS.md / docs / CHANGELOG / worklog 수정은 본 spec 범위가 아니다.

선행 spec: `01-aws-migration-foundation`.

본 spec의 결정은 아래 후속 spec 의 입력으로 사용된다.

- `06-secrets-and-iam`
- `03-marketconnector-ec2`
- `08-interest-crawler-and-preprocessor-ecs`
- `04-strategy-batch-stepfunctions`
- `05-port-view-ecs-and-runbook`
- `09-strategy-research-batch`
- `10-cutover-and-validation-runbook`

## Glossary

- 단일 portfolio DB: 모든 MS가 공유하는 PostgreSQL 데이터베이스 `portfolio`.
- schema-per-domain: 10개 도메인 schema(`reference, interest, preprocessor, research, decision, execution, connector, ops, legacy, public`).
- search_path 정책: MS별 README에 정의된 schema 우선순위. 본 spec은 변경하지 않는다.
- cutover: 로컬 PostgreSQL → AWS RDS로 운영 데이터를 옮기는 전환 작업.
- NAT-free 전략: NAT Gateway / NAT Instance 모두 사용하지 않고 인터넷 outbound가 필요한 워크로드만 public subnet 또는 EC2+EIP에 배치하고, AWS 서비스 접근은 VPC Endpoint로 처리하는 네트워크 정책.

## Requirements

### Requirement 1: 단일 VPC와 환경 구분

**Objective**: As 운영자, I want 단일 VPC 안에서 local-dev / aws-paper / aws-live를 안전하게 구분하기, so that 비용과 운영 단순성을 유지하면서 환경 사이의 영향이 차단된다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL 단일 region(서울 ap-northeast-2)에 단일 VPC를 두는 권고를 명시해야 한다.
2. WHEN 환경 구분을 다루는 경우 THEN design.md SHALL local-dev, aws-paper, aws-live 3개 환경 모델을 명시하고 local-dev는 AWS 리소스를 갖지 않음을 표로 명시해야 한다.
3. WHEN AWS 환경별 RDS 분리를 다루는 경우 THEN design.md SHALL aws-paper와 aws-live 각각 별도 RDS 인스턴스를 권고해야 한다.
4. IF 운영자가 prod / non-prod VPC 분리 옵션을 검토하는 경우 THEN decision-matrix.md SHALL 단일 VPC 유지(현 권고)와 VPC 분리안의 비용 / 운영 부담 비교 표를 포함해야 한다.

### Requirement 2: Subnet과 Route Table 구조 (NAT-free 전제)

**Objective**: As 운영자, I want public / private (app) / private (data) subnet 구조와 NAT-free 라우팅을 받기, so that 비용을 줄이면서 outbound가 필요한 워크로드 위치를 명확히 정의할 수 있다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL 다중 AZ 기준 public, private (app), private (data) 3종 subnet 구조와 각 subnet에 들어갈 리소스 매핑을 포함해야 한다.
2. WHEN Route Table 정책을 다루는 경우 THEN design.md SHALL public Route Table은 IGW로, private (app) Route Table은 외부 라우트 없이 VPC Endpoint로만 AWS 서비스에 접근하는 NAT-free 라우팅을 명시해야 한다.
3. WHEN RDS subnet group을 다루는 경우 THEN design.md SHALL 최소 2개 AZ의 private (data) subnet으로 구성하는 권고를 포함해야 한다.
4. WHEN 인터넷 outbound가 필요한 워크로드를 다루는 경우 THEN design.md SHALL 해당 워크로드를 public subnet + assignPublicIp 또는 EC2 + EIP 또는 ECS on EC2 또는 NAT Instance 후보 중 하나에 배치하는 정책을 옵션별로 명시해야 한다.

### Requirement 3: NAT-free 전략과 VPC Endpoint 구성

**Objective**: As 운영자, I want NAT Gateway 미사용을 기본안으로 둔 외부 접근 정책과 VPC Endpoint 구성을 받기, so that 비용을 줄이면서 워크로드별로 외부 접근 경로를 통제할 수 있다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL aws-paper와 aws-live 모두 NAT Gateway 미사용을 기본안으로 명시해야 한다.
2. WHEN 인터넷 outbound 워크로드 옵션을 다루는 경우 THEN design.md SHALL 다음 옵션을 비교해야 한다:
   - public subnet + assignPublicIp 활성 ECS Fargate
   - 마켓커넥터 EC2 + EIP
   - crawler 전용 EC2 또는 ECS on EC2
   - NAT Instance(`t4g.nano`)
   - 비교 항목은 비용, 보안 리스크, SG 통제, 운영 난이도, 권고 사용처를 포함해야 한다.
3. WHEN VPC Endpoint를 다루는 경우 THEN design.md SHALL S3 (Gateway), ECR (api+dkr), Secrets Manager, SSM, CloudWatch Logs Endpoint를 활성으로 권고하고 STS / KMS는 옵션으로 명시해야 한다.
4. WHEN 비용 영향을 다루는 경우 THEN decision-matrix.md SHALL aws-paper와 aws-live 각각의 NAT 미사용 + VPC Endpoint 조합 월 비용을 추정 표로 포함해야 한다.
5. IF 마켓커넥터가 broker 측 IP 등록을 요구하는 경우 THEN design.md SHALL 마켓커넥터 EC2에 EIP 직접 부여를 1순위로 두는 정책을 명시해야 한다.

### Requirement 4: Security Group 설계

**Objective**: As 운영자, I want MS별 Security Group을 최소 권한으로 받기, so that 의도한 inbound / outbound만 허용된다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL 다음 SG를 정의해야 한다: `sg-marketconnector-ec2`, `sg-port-view-ecs`, `sg-strategy-tasks`, `sg-crawler-tasks`, `sg-preprocessor-tasks`, `sg-research-batch`, `sg-rds-postgres`, `sg-vpc-endpoints`. ALB SG는 ALB 도입 결정 시 추가.
2. WHEN RDS SG를 다루는 경우 THEN design.md SHALL `sg-rds-postgres` inbound는 위 app/EC2 SG 참조만 허용하고 0.0.0.0/0 inbound를 허용하지 않는다는 정책을 명시해야 한다.
3. WHEN 운영자 접근 SG를 다루는 경우 THEN design.md SHALL EC2 SSH 22 inbound 0.0.0.0/0을 금지하고 SSM Session Manager만 사용하는 권고를 명시해야 한다.
4. WHEN public subnet에 배치되는 ECS Task를 다루는 경우 THEN design.md SHALL 해당 Task SG는 inbound 미허용(필요 시 운영자 IP allowlist)과 outbound 외부 인터넷만 허용하는 정책을 명시해야 한다.

### Requirement 5: RDS for PostgreSQL 인스턴스 구성 (local-dev 제외)

**Objective**: As 운영자, I want aws-paper와 aws-live RDS 구성과 multi-AZ 결정을 받기, so that 비용과 가용성을 균형 있게 선택할 수 있다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL local-dev는 기존 로컬 PostgreSQL 유지로 명시하고, aws-paper와 aws-live 각각의 인스턴스 클래스, single-AZ vs multi-AZ, storage, IOPS, encryption at rest, performance insights, backup retention, PITR을 표로 포함해야 한다.
2. WHEN aws-paper RDS를 다루는 경우 THEN design.md SHALL `db.t4g.small` single-AZ 시작을 권고하고 retention 7일 / PITR on을 명시해야 한다.
3. WHEN aws-live RDS 비용 절감안을 다루는 경우 THEN decision-matrix.md SHALL `db.t4g.medium` single-AZ 시작 옵션을 비용 / 다운타임 리스크와 함께 표로 포함해야 한다.
4. WHEN aws-live RDS 안정성 우선안을 다루는 경우 THEN design.md SHALL `db.t4g.medium` 또는 `db.m6g.large` multi-AZ 권고와 retention 14일 / PITR on을 명시해야 한다.
5. WHEN PostgreSQL 버전을 다루는 경우 THEN design.md SHALL PostgreSQL 16 이상을 권고하고 minor version auto upgrade 정책(paper / live 활성)을 명시해야 한다.
6. WHEN parameter group을 다루는 경우 THEN design.md SHALL `rds.force_ssl=1`, `log_min_duration_statement`, `log_lock_waits=1`, `idle_in_transaction_session_timeout` 권고를 명시해야 한다.

### Requirement 6: portfolio DB와 schema-per-domain 유지

**Objective**: As 운영자, I want 단일 portfolio DB와 10개 schema 구조를 그대로 옮기기, so that 8개 MS의 기존 SQL과 search_path 정책이 그대로 동작한다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL aws-paper / aws-live 각각의 RDS 안에 데이터베이스 이름 `portfolio`를 사용하는 정책을 명시해야 한다.
2. WHEN schema 정책을 다루는 경우 THEN design.md SHALL 10개 schema(`reference, interest, preprocessor, research, decision, execution, connector, ops, legacy, public`)를 그대로 유지하는 정책을 명시해야 한다.
3. WHEN MS별 search_path 정책을 다루는 경우 THEN design.md SHALL 8개 MS README에 정의된 search_path 순서를 변경하지 않고 RDS connection에 동일하게 적용하는 방식을 명시해야 한다.
4. WHEN 환경변수 키 호환성을 다루는 경우 THEN design.md SHALL 아래 키를 그대로 유지하는 정책을 명시해야 한다.
   - RDS: `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`, `PORTFOLIO_DB_NAME`
   - 브로커 / 계정: `PORT_ACCOUNT_NO`, `PORT_BROKER_NAME`
   - 환경: `PORT_ENVIRONMENT`
   - 전략: `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION`, `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT`

### Requirement 7: MS별 DB Role 권한 매트릭스

**Objective**: As 운영자, I want MS별 PostgreSQL role과 schema-level 권한 매트릭스를 받기, so that 최소 권한 정책으로 보안을 확보할 수 있다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL 7개 role(`marketconnector_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `execution_app`, `research_app`, `view_app`)의 schema별 read/write 권한 매트릭스를 표로 명시해야 한다.
2. WHEN view_app 권한을 다루는 경우 THEN design.md SHALL 모든 schema READ + ops WRITE 기본 권한을 명시하고, execution write 필요성은 `05-port-view-ecs-and-runbook` spec에서 재검토 항목임을 표시해야 한다.
3. WHEN 권한 부여를 다루는 경우 THEN design.md SHALL `USAGE`, `SELECT`, `INSERT/UPDATE/DELETE`, `default privileges` 권고 SQL 패턴을 포함해야 한다.
4. WHEN role과 환경변수 매핑을 다루는 경우 THEN design.md SHALL `INTEREST_DB_USER`에 들어갈 값이 MS별로 다를 수 있다는 정책을 명시해야 한다.
5. WHEN secret 보관을 다루는 경우 THEN design.md SHALL DB 비밀번호가 Secrets Manager 또는 SSM SecureString 항목임을 placeholder로 명시하고 최종 결정은 `06-secrets-and-iam` spec에서 한다는 점을 명시해야 한다. 본 문서에는 실제 값을 절대 출력하지 않고 `[REDACTED]`로만 표기한다.

### Requirement 8: 데이터 cutover 절차 (pg_dump / pg_restore)

**Objective**: As 운영자, I want local-dev → aws-paper, aws-paper 검증 후 → aws-live cutover 절차를 받기, so that 단계별로 안전하게 cutover를 진행할 수 있다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL `pg_dump` + `pg_restore` 1순위, AWS DMS는 보류 결정과 그 사유를 명시해야 한다.
2. WHEN cutover 단계를 다루는 경우 THEN tasks.md SHALL 운영자 정지 → 로컬 dump → RDS restore → schema / search_path / role 검증 → MS aws-paper 가동 → 검증 → aws-live cutover 순서를 단계별 task로 분해해야 한다.
3. WHEN 검증 SQL 후보를 다루는 경우 THEN design.md SHALL schema 목록, role 목록, schema별 table 수, search_path 확인, 핵심 테이블 row count 검증 SQL을 포함해야 한다.
4. IF cutover 도중 검증이 실패하는 경우 THEN tasks.md SHALL 운영자가 즉시 중단하고 local-dev 환경으로 복귀하는 rollback 절차를 명시해야 한다.

### Requirement 9: RDS backup / snapshot / PITR 정책

**Objective**: As 운영자, I want RDS automated backup, manual snapshot, point-in-time recovery 정책을 받기, so that cutover 전후의 복원 옵션이 명확해진다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL automated backup retention(aws-paper 7일, aws-live 14일), manual snapshot 권고 시점(cutover 직전, 분기), PITR 활성 정책(aws-paper / aws-live)을 명시해야 한다.
2. WHEN snapshot 비용을 다루는 경우 THEN decision-matrix.md SHALL backup retention과 single-AZ vs multi-AZ가 비용에 미치는 영향 표를 포함해야 한다.
3. WHEN 복원 절차를 다루는 경우 THEN design.md SHALL snapshot에서 새 RDS 인스턴스로 복원하고 application의 endpoint를 변경하는 절차 개요를 포함해야 한다.

### Requirement 10: 안전장치와 자동 재시도 정책

**Objective**: As 운영자, I want aws-paper / aws-live의 자동 BUY/SELL과 자동 재시도 정책 기준을 받기, so that 잘못된 자동 재시도가 중복 주문 / 자금 손실로 이어지지 않는다.

#### Acceptance Criteria

1. WHEN aws-paper 자동 BUY/SELL E2E 검증을 다루는 경우 THEN design.md SHALL 초기 주문 차단 → 검증 후 자동 BUY/SELL 단계적 허용 절차를 명시해야 한다.
2. WHEN aws-live 자동 BUY / SELL을 다루는 경우 THEN design.md SHALL 초기 자동주문 금지, 후보 생성 + View 수동 승인 중심 운영, 검증 후 제한적 자동화 검토 정책을 명시해야 한다.
3. WHEN 자동 재시도 정책을 다루는 경우 THEN design.md SHALL 자동 재시도 금지(BUY / SELL / fill sync 결과 반영 / position 변경 / intraday stop SELL 생성)와 자동 재시도 허용(idempotent step만)을 표로 명시해야 한다.
4. WHEN paper와 live 환경 분리를 다루는 경우 THEN design.md SHALL aws-paper에서 aws-live broker로 호출이 발생하지 않도록 SG / Secrets / 환경변수 키 분리 정책을 명시해야 한다.

### Requirement 11: 비용 프로파일 (low / realistic / stable)

**Objective**: As 운영자, I want aws-paper와 aws-live 각각의 비용 프로파일을 받기, so that 비용 절감안과 안정성 우선안을 비교하여 선택할 수 있다.

#### Acceptance Criteria

1. WHEN decision-matrix.md가 작성되면 THEN decision-matrix.md SHALL aws-paper의 low / realistic / stable 3개 비용 프로파일과 aws-live의 low / realistic / stable 3개 비용 프로파일을 각각 표로 포함해야 한다.
2. WHEN 비용 절감 항목을 다루는 경우 THEN decision-matrix.md SHALL NAT Gateway 제거, AWS dev 미구축, ALB 미사용 또는 internal ALB 보류, RDS single-AZ 시작, CloudWatch Logs retention 축소, Secrets Manager 항목 최소화 또는 SSM SecureString 활용, VPC Endpoint vs NAT 비용 비교를 항목별 영향과 함께 명시해야 한다.
3. WHEN 비용 절감안의 운영 리스크를 다루는 경우 THEN decision-matrix.md SHALL 절감 옵션별로 발생할 수 있는 운영 리스크(예: AZ 장애 시 다운타임, log 부족으로 회고 불가)를 명시해야 한다.

### Requirement 12: AWS Console 단계별 진행 절차

**Objective**: As 운영자, I want AWS Console에서 직접 진행할 수 있는 단계별 절차를 받기, so that IaC 작성 없이도 aws-paper 환경을 안전하게 구축할 수 있다.

#### Acceptance Criteria

1. WHEN tasks.md가 작성되면 THEN tasks.md SHALL VPC → Subnet → IGW → Route Table → SG → VPC Endpoint → RDS subnet group → RDS parameter group → RDS instance → schema/role 생성 → 검증 SQL → cutover 사전 점검 순서를 작은 단위 task로 분해해야 한다.
2. WHEN 각 task를 다루는 경우 THEN tasks.md SHALL 운영자가 AWS Console에서 눌러야 하는 항목, 입력해야 하는 값(민감정보는 `[REDACTED]`), 검증 방법, rollback 방법을 포함해야 한다.
3. WHEN 비용에 영향이 큰 task를 다루는 경우 THEN tasks.md SHALL 해당 task에 "approval required" 표시와 비용 영향 한 줄 요약을 추가해야 한다.

### Requirement 13: AWS Resource 용어집과 운영자 결정 기록

**Objective**: As 운영자, I want AWS 리소스 용어집과 현재 결정 기록을 별도 파일로 받기, so that AWS 용어가 익숙하지 않아도 후속 spec을 쉽게 따라가고 결정 변경을 추적할 수 있다.

#### Acceptance Criteria

1. WHEN 본 spec 산출물을 만드는 경우 THEN 산출물 SHALL 루트 공통 [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md)를 포함하고 아래 항목을 모두 포함해야 한다.
   - Network: Region / VPC / Subnet / Public Subnet / Private Subnet / Route Table / Internet Gateway / NAT Gateway / NAT Instance / VPC Endpoint / Security Group / Elastic IP / ALB
   - Compute: EC2 / ECS / Fargate / ECR
   - Database: RDS / RDS Subnet Group / RDS Parameter Group
   - Security / Secrets: Secrets Manager / SSM Parameter Store / IAM Role / IAM Policy / Instance Profile / KMS
   - Observability: CloudWatch Logs / CloudWatch Metrics / CloudWatch Alarm
   - Orchestration: EventBridge Scheduler / Step Functions / Lambda / AWS Batch
   - Storage / Discovery: S3 / Cloud Map / Service Discovery
2. WHEN 용어집을 작성하는 경우 THEN 각 항목 SHALL 한 줄 설명, 이 포트폴리오에서의 역할, 비용 발생 여부, 운영자가 조심해야 할 점, 관련 후속 Spec을 포함해야 한다.
3. WHEN 본 spec 산출물을 만드는 경우 THEN 산출물 SHALL 루트 공통 [`../_common/operator-decisions.md`](../_common/operator-decisions.md)를 포함하고 Decision ID / 결정 항목 / 선택지 / 선택값 / 상태 / 비용 영향 / 운영 리스크 / 후속 Spec 영향 컬럼을 가진 표로 현재 운영자 결정을 기록해야 한다.
4. WHEN 운영자 결정 상태를 표시하는 경우 THEN 루트 공통 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) SHALL CONFIRMED / TENTATIVE / TBD / DEFERRED 상태 값을 사용해야 한다.

### Requirement 14: 본 spec의 안전 제약

**Objective**: As 운영자, I want 본 spec 작업이 코드 / 운영 데이터 / 기존 문서를 변경하지 않도록 명시적으로 제한하기.

#### Acceptance Criteria

1. WHEN 본 spec이 산출물을 만드는 경우 THEN 산출물 SHALL 아래 파일과 루트 공통 문서를 참조 / 보강 대상으로만 한정해야 한다.
   - 본 spec 폴더 안: [`requirements.md`](./requirements.md), [`design.md`](./design.md), [`tasks.md`](./tasks.md), [`decision-matrix.md`](./decision-matrix.md)
   - 루트 공통: [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md)
2. WHEN 본 spec 작업이 진행되는 동안 THE 작업 SHALL 8개 MS의 README / AGENTS.md / CHANGELOG / docs / worklog와 소스 코드를 수정하지 않아야 한다.
3. WHEN 본 spec 작업이 진행되는 동안 THE 작업 SHALL 실제 AWS 리소스를 만들거나 변경하지 않아야 한다.
4. WHEN 본 spec 작업이 진행되는 동안 THE 작업 SHALL 8개 MS entrypoint, broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / 주문 / 체결 / Daily Batch / intraday monitor 호출을 실행하지 않아야 한다.
5. WHEN secret을 다루는 경우 THE 산출물 SHALL 모든 secret 자리에 `[REDACTED]`만 사용하고 실제 값을 적지 않아야 한다.
6. WHEN 06-secrets-and-iam과의 분리 정책을 다루는 경우 THEN 본 spec 산출물 SHALL Secrets 보관 / IAM 매트릭스 최종 결정은 `06-secrets-and-iam` spec에서 다룬다는 점을 명시해야 한다.
