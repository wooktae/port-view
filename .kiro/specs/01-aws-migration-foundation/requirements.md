# Requirements Document

## Introduction

이 문서는 PORT-STRATEGY-AI 포트폴리오 자동매매 시스템을 구성하는 8개 마이크로서비스(이하 8개 MS)를 AWS 환경으로 이전하기 위한 기반(foundation) 단계의 요구사항을 정의한다.

대상 8개 MS는 다음과 같다.

- `port-marketconnector` (Python/Flask, KIS 국내 주식 API 연동, 주문/잔고/시세, 단일 access token 파일 기반 세션)
- `port-view` (Spring Boot 4.1 / Thymeleaf, 통합 운영 콘솔, Daily Batch orchestration, Slack 알림)
- `port-interest-crawler` (Python, Naver / yfinance / KRX 수집, Selenium/Chrome 의존)
- `port-interest-preprocessor` (Python, raw → pre feature 가공, 외부 holiday API)
- `port_strategy_common` (Python 라이브러리, 순수 함수형 전략 코어, DB/HTTP/IO 없음)
- `port_strategy_decision` (Python, daily BUY signal 및 daily position HOLD/SELL decision)
- `port_strategy_research` (Python, backtest run / analysis / 텍스트 리포트 생성)
- `port_strategy_execution` (Python, execution order 생성, connector 주문 호출, fill/position sync, intraday monitor)

8개 MS는 모두 단일 PostgreSQL `portfolio` 데이터베이스를 공유하며, 도메인별 schema 와 MS별 `search_path` 우선순위로 동작한다.

- 도메인 schema: `reference`, `interest`, `preprocessor`, `research`, `decision`, `execution`, `connector`, `ops`, `legacy`, `public`
- 각 MS는 `INTEREST_DB_*` 환경변수와 broker / Slack / 외부 API credential을 사용한다.

이번 spec은 다음을 산출물로 한다.

- 8개 MS 각각의 AWS 배포 후보 비교와 권고
- 공통 기반(네트워크, RDS, Secrets, ECR, 관측, CI/CD, 알림, Runbook)의 권고 구조
- 후속 spec에서 실제 IaC 작성과 배포로 이어질 수 있는 작업 계획

이번 spec은 **문서 산출물(requirements.md, design.md, tasks.md)만** 만든다. 실제 AWS 리소스 생성, 코드 수정, 기존 README/AGENTS.md/docs/CHANGELOG/worklog 수정은 본 spec 범위에서 수행하지 않는다.

## Glossary

- **Foundation 단계**: 실제 배포/리소스 생성 전, 8개 MS의 AWS 매핑/네트워크/데이터/관측/CI/CD/Runbook 권고 구조를 문서로 확정하는 단계.
- **MS**: 마이크로서비스. 본 시스템에서는 8개 저장소 단위.
- **Daily Batch**: `port-view`에서 시작하는 일일 파이프라인. 현재 외부 MS의 Python 절대경로를 subprocess로 호출하는 구조.
- **단일 portfolio DB / schema-per-domain**: 모든 MS가 동일한 PostgreSQL 데이터베이스 `portfolio`를 공유하고, 도메인 schema와 MS별 `search_path`로 테이블을 해석하는 구조.

## Requirements

### Requirement 1: 8개 MS별 AWS 배포 후보 매핑

**Objective**: As 운영자, I want 각 MS의 워크로드 특성에 맞는 AWS 컴퓨트 후보를 비교해 받기, so that 무리한 통일 없이 합리적인 컴퓨트 선택을 결정할 수 있다.

#### Acceptance Criteria

1. WHEN 본 spec이 완료되면 THEN design.md SHALL 8개 MS 각각에 대해 최소 2개 AWS 컴퓨트 후보(예: EC2 / ECS Fargate / AWS Batch / Lambda / Elastic Beanstalk / App Runner / Step Functions + EventBridge Scheduler)를 비교한 표와 권고를 포함해야 한다.
2. WHEN `port-marketconnector` MS를 평가하는 경우 THEN design.md SHALL KIS broker 세션, 단일 access token 파일, 고정 outbound IP 가능성을 근거로 EC2 후보를 1순위 후보 중 하나로 평가해야 한다.
3. WHEN `port-view` MS를 평가하는 경우 THEN design.md SHALL Elastic Beanstalk, ECS Fargate, App Runner를 후보로 비교하고 Daily Batch가 외부 MS Python 경로를 직접 호출하는 현 구조의 영향을 명시해야 한다.
4. WHEN `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research` MS를 평가하는 경우 THEN design.md SHALL 아래 후보를 비교하고 daily / intraday / backtest의 실행 빈도와 지속 시간을 근거로 권고를 명시해야 한다.
   - ECS Fargate Task
   - AWS Batch
   - Step Functions + EventBridge Scheduler
5. WHEN `port-interest-crawler` MS를 평가하는 경우 THEN design.md SHALL Lambda, ECS Task, EC2 후보를 비교하고 Selenium/Chrome 의존성과 KRX 로그인 흐름을 근거로 Lambda 사용 한계를 명시해야 한다.
6. WHEN `port-interest-preprocessor` MS를 평가하는 경우 THEN design.md SHALL Lambda, ECS Task 후보를 비교하고 holiday API 호출 및 long-running upsert 가능성을 근거로 권고를 명시해야 한다.
7. WHEN `port_strategy_common` MS를 평가하는 경우 THEN design.md SHALL 순수 라이브러리 특성과 별도 컴퓨트 배포 대상이 아니라는 결론, 그리고 다른 MS 컨테이너 이미지에 포함시키는 packaging 권고를 명시해야 한다.
8. IF 어떤 MS를 컨테이너 서비스가 아닌 EC2로 권고하는 경우 THEN design.md SHALL 그 사유를 broker IP, 세션, 라이선스, 운영 비용 중 어느 항목에서 비롯된 것인지 명확히 적어야 한다.

### Requirement 2: 단일 PostgreSQL `portfolio` DB의 RDS 전환 전략

**Objective**: As 운영자, I want 단일 portfolio DB와 schema-per-domain 구조를 유지하면서 AWS RDS for PostgreSQL로 옮기는 전략, so that 기존 SQL과 `search_path` 기반 동작이 깨지지 않는다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL RDS for PostgreSQL 단일 인스턴스를 기본으로 한 권고와, dev / paper / live 환경 분리 방식을 포함해야 한다.
2. WHEN schema 구성을 다루는 경우 THEN design.md SHALL `reference, interest, preprocessor, research, decision, execution, connector, ops, legacy, public` 10개 schema가 그대로 유지되어야 함을 명시해야 한다.
3. WHEN 각 MS의 DB 사용자 권한을 다루는 경우 THEN design.md SHALL MS별 DB role(예: `marketconnector_app`, `crawler_app`, ...)과 schema-level 최소 권한 권고를 포함하되, 기존 `INTEREST_DB_USER` 환경변수 키는 유지하는 방식으로 적어야 한다.
4. WHEN 각 MS의 `search_path` 정책을 다루는 경우 THEN design.md SHALL 8개 MS README에 정의된 `search_path` 순서를 변경하지 않고 그대로 유지하는 방식임을 명시해야 한다.
5. WHEN 데이터 이전 절차를 다루는 경우 THEN design.md SHALL `pg_dump` / `pg_restore` 또는 AWS DMS 후보를 비교하고, 1순위 권고와 cutover 단계 개요를 포함해야 한다.
6. WHEN 백업과 복원을 다루는 경우 THEN design.md SHALL RDS automated backup, manual snapshot, point-in-time recovery 사용 권고와 보존 기간 권고를 포함해야 한다.
7. IF 비용 최적화를 위해 multi-AZ를 비활성화하는 경우 THEN design.md SHALL live 환경에서는 multi-AZ를 권고하고 dev / paper에서만 single-AZ 허용 가능을 명시해야 한다.

### Requirement 3: 민감정보 외부화

**Objective**: As 운영자, I want broker / DB / Slack / 외부 API credential을 코드와 문서 밖으로 분리하기, so that 8개 MS 모두에서 secret이 source control 또는 로그에 남지 않는다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL Secrets Manager와 SSM Parameter Store의 사용 기준을 정의하고 어떤 항목을 어디에 저장하는지 매핑 표를 포함해야 한다.
2. WHEN KIS app key, app secret, base URL, 계좌번호, 계좌 상품 코드, access token 파일을 다루는 경우 THEN design.md SHALL Secrets Manager에 저장하고 EC2 또는 ECS Task의 IAM Role을 통해 주입하는 방식을 권고해야 한다.
3. WHEN PostgreSQL 접속 password를 다루는 경우 THEN design.md SHALL Secrets Manager에 저장하고 자동 rotation 가능성을 권고로 표시해야 한다.
4. WHEN Slack webhook URL을 다루는 경우 THEN design.md SHALL Secrets Manager 또는 SSM Parameter Store SecureString 사용 권고를 포함해야 한다.
5. WHEN Naver API client id/secret, Chrome / ChromeDriver 경로 같은 환경 의존 값을 다루는 경우 THEN design.md SHALL SSM Parameter Store 사용 권고를 포함해야 한다.
6. WHEN 기존 환경변수 키 호환성을 다루는 경우 THEN design.md SHALL 아래 키 이름을 그대로 유지하는 정책을 명시해야 한다.
   - RDS: `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`, `PORTFOLIO_DB_NAME`
   - 브로커 / 계정: `PORT_ACCOUNT_NO`, `PORT_BROKER_NAME`
   - 환경: `PORT_ENVIRONMENT`
   - 전략: `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION`, `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT`
7. WHEN 문서에 secret을 인용해야 하는 경우 THEN design.md와 tasks.md SHALL 모든 secret 자리에 `[REDACTED]`만 사용하고 실제 값을 절대 적지 않아야 한다.
8. WHEN `access_token.txt`를 다루는 경우 THEN design.md SHALL 단일 파일 토큰을 EFS, S3, 또는 Secrets Manager 중 어디에 보관할지 비교하고 broker 세션 단일성 제약을 근거로 1순위 권고를 명시해야 한다.

### Requirement 4: 네트워크 및 외부 접근 경로 설계

**Objective**: As 운영자, I want VPC / Subnet / Security Group 구조와 broker, KRX, Naver, yfinance, Slack 같은 외부 접근 경로를 정의하기, so that 보안 경계가 명확하고 broker IP 정책에 맞는 outbound가 보장된다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL 단일 VPC와 public / private subnet 구분, NAT Gateway 또는 NAT Instance 권고, RDS 전용 subnet group을 포함해야 한다.
2. WHEN `port-marketconnector`의 outbound IP를 다루는 경우 THEN design.md SHALL Elastic IP를 EC2에 부여하거나 NAT Gateway의 EIP를 broker에 등록하는 후보를 비교하고 1순위 권고를 명시해야 한다.
3. WHEN `port-view`에서 `port-marketconnector`로 호출하는 경로를 다루는 경우 THEN design.md SHALL 동일 VPC 내부 호출, ALB 경유, Service Discovery 후보 중 권고와 그 사유를 포함해야 한다.
4. WHEN `port-interest-crawler`의 KRX / Naver / yfinance 접근을 다루는 경우 THEN design.md SHALL outbound 전용이고 inbound 노출이 없어야 함을 명시해야 한다.
5. WHEN Slack webhook 호출을 다루는 경우 THEN design.md SHALL outbound HTTPS만 필요하며 별도 inbound 노출이 없어야 함을 명시해야 한다.
6. WHEN 운영자 접속을 다루는 경우 THEN design.md SHALL EC2 SSH 직접 노출 대신 SSM Session Manager 사용을 권고해야 한다.
7. IF design.md가 production / non-production VPC 분리를 권고하지 않는 경우 THEN design.md SHALL 단일 VPC 내에서 environment tag와 subnet 분리로 환경을 구분하는 정책을 명시해야 한다.

### Requirement 5: 컨테이너 이미지와 CI/CD 파이프라인

**Objective**: As 운영자, I want 8개 MS에 대해 일관된 컨테이너 이미지 빌드와 배포 파이프라인을 정의하기, so that 코드 변경 → 빌드 → 배포 흐름이 표준화된다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL 8개 MS 중 컨테이너 배포 대상 MS의 ECR repository 명명 규칙(예: `port-marketconnector`, `port-view`, ...)을 포함해야 한다.
2. WHEN `port_strategy_common`의 배포를 다루는 경우 THEN design.md SHALL 자체 컨테이너가 아니라 다른 MS Dockerfile에서 git submodule 또는 wheel/sdist로 packaging되는 방식 중 1순위 권고를 명시해야 한다.
3. WHEN CI/CD 도구를 다루는 경우 THEN design.md SHALL CodePipeline + CodeBuild + CodeDeploy 조합과 GitHub Actions + ECR + 배포 스크립트 조합을 비교하고 1순위 권고와 그 사유를 명시해야 한다.
4. WHEN 빌드 단계의 secret 처리를 다루는 경우 THEN design.md SHALL build 시점에 secret을 이미지에 굽지 않고 runtime에 IAM Role과 Secrets Manager / Parameter Store로 주입하는 정책을 명시해야 한다.
5. WHEN 환경별 배포를 다루는 경우 THEN design.md SHALL dev / paper / live 환경에 대한 promotion 흐름과 manual approval 권고를 포함해야 한다.
6. IF `port-marketconnector` 또는 다른 MS가 EC2 기반으로 권고되는 경우 THEN design.md SHALL 해당 MS에 대해서는 컨테이너 배포가 아닌 AMI 또는 systemd unit + S3 / CodeDeploy 기반 배포 후보를 별도로 명시해야 한다.

### Requirement 6: 관측, 알림, 운영 Runbook

**Objective**: As 운영자, I want 로그 / 메트릭 / 알람 / Slack 알림 / Runbook 표준을 받기, so that 8개 MS의 장애 감지와 대응 절차가 일관된다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL 8개 MS의 로그를 CloudWatch Logs로 수집하는 표준 log group 명명 규칙을 포함해야 한다.
2. WHEN 메트릭을 다루는 경우 THEN design.md SHALL CPU, memory, RDS connection, broker API error rate, Daily Batch step 실패 등 핵심 메트릭과 권고 alarm threshold를 포함해야 한다.
3. WHEN Slack 통합을 다루는 경우 THEN design.md SHALL 기존 `port-view`의 `SlackNotificationService` 흐름을 유지하면서, AWS 측에서는 CloudWatch Alarm → SNS → Lambda → Slack webhook 또는 AWS Chatbot 후보를 비교하고 1순위 권고를 명시해야 한다.
4. WHEN Daily Batch 실패 처리를 다루는 경우 THEN design.md SHALL `strategy_daily_batch_run`, `strategy_daily_batch_step_log` 테이블과 EventBridge / CloudWatch Alarm 연계 권고를 포함해야 한다.
5. WHEN 운영 Runbook을 다루는 경우 THEN design.md SHALL 최소한 broker 토큰 만료 / 재발급, KIS 주문 실패, RDS failover, Daily Batch 실패 재실행, intraday monitor 중단 4가지 시나리오에 대한 절차 항목을 포함해야 한다.
6. WHEN Runbook이 다루는 위험을 다루는 경우 THEN design.md SHALL live 환경에서 자동 재시도 금지가 필요한 항목(예: BUY/SELL 주문, fill sync)과 안전한 자동 재시도가 가능한 항목(예: 시세 조회, 전처리 idempotent step)을 구분해 명시해야 한다.

### Requirement 7: 환경 분리, 비용, 단계적 마이그레이션 로드맵

**Objective**: As 운영자, I want dev / paper / live 환경 분리와 비용 추정, 단계적 cutover 로드맵을 받기, so that 한 번에 모든 MS를 옮기는 위험을 피할 수 있다.

#### Acceptance Criteria

1. WHEN design.md가 작성되면 THEN design.md SHALL 환경 구분(dev, paper, live)을 정의하고 각 환경에서 broker live 연결 허용 여부를 명시해야 한다.
2. WHEN 비용을 다루는 경우 THEN design.md SHALL EC2, ECS Fargate, RDS, NAT Gateway, CloudWatch, Secrets Manager의 월간 비용 항목 후보 목록을 포함하되 실제 환율/가격은 추정 범위(low/high)로만 표기해야 한다.
3. WHEN 단계적 cutover 로드맵을 다루는 경우 THEN design.md SHALL 최소한 다음 단계 순서를 권고해야 한다:
   - Stage 1: 네트워크 / RDS / Secrets / ECR foundation
   - Stage 2: `port_strategy_common` packaging + 1개 MS 파일럿(예: `port-interest-preprocessor` 또는 `port_strategy_research`)
   - Stage 3: `port-marketconnector` (broker IP 등록 포함)
   - Stage 4: `port_strategy_decision` + `port_strategy_execution` (paper 환경 우선)
   - Stage 5: `port-view` 운영 콘솔 + Daily Batch 통합
   - Stage 6: live cutover 및 legacy 로컬 운영 종료
4. WHEN 각 stage를 다루는 경우 THEN design.md SHALL 진입 조건과 이탈(rollback) 조건을 포함해야 한다.
5. WHEN live 환경 cutover를 다루는 경우 THEN design.md SHALL paper 환경에서 최소 N영업일 이상 검증을 권고하되 N 값은 운영자가 결정하는 placeholder로 둬야 한다.

### Requirement 8: 본 spec 산출물의 안전 제약과 범위

**Objective**: As 운영자, I want 본 spec 작업이 코드 / 운영 데이터 / 기존 문서를 변경하지 않도록 명시적으로 제한하기, so that 8개 MS의 AGENTS.md 작업 규칙과 충돌하지 않는다.

#### Acceptance Criteria

1. WHEN 본 spec이 산출물을 만드는 경우 THEN 산출물 SHALL `C:\Workspaces\port-view\.kiro\specs\01-aws-migration-foundation\` 하위의 `requirements.md`, `design.md`, `tasks.md`로만 한정되어야 한다.
2. WHEN 본 spec 작업이 진행되는 동안 THE 작업 SHALL 8개 MS의 기존 `README.md`, `AGENTS.md`, `CHANGELOG.md`, `docs/**`, `docs/worklog/**` 파일을 수정하지 않아야 한다.
3. WHEN 본 spec 작업이 진행되는 동안 THE 작업 SHALL 8개 MS의 Python / Java 소스 코드를 수정하지 않아야 한다.
4. WHEN 본 spec 작업이 진행되는 동안 THE 작업 SHALL 실제 AWS 리소스를 생성하거나 변경하지 않아야 한다.
5. WHEN 본 spec 작업이 진행되는 동안 THE 작업 SHALL 8개 MS의 어떤 entrypoint도 실행하지 않아야 한다(특히 broker API, Selenium/Chrome, KRX/Naver/yfinance, RDS/PostgreSQL DDL/DML, Daily Batch, intraday monitor 호출 금지).
6. WHEN 본 spec이 secret을 다루는 경우 THE 산출물 SHALL 실제 password, token, app key, app secret, 계좌번호, webhook URL 값을 포함하지 않고 모두 `[REDACTED]`로 표기해야 한다.
7. WHEN tasks.md가 작성되는 경우 THEN tasks.md SHALL 각 task가 운영자 1인이 단일 세션에서 검토하고 승인할 수 있는 작은 단위로 나뉘어야 하며, AWS 리소스 생성을 동반하는 task는 명시적으로 "approval required"로 표기되어야 한다.
