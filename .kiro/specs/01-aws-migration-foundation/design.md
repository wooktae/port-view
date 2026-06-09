# Design Document

## Overview

본 design은 PORT-STRATEGY-AI 8개 MS를 AWS로 이전하기 위한 **foundation 구조**를 정의한다. 실제 IaC 코드, AWS 리소스 생성, 8개 MS의 코드 변경은 본 spec 범위가 아니다. 본 문서는 후속 spec(예: `02-aws-network-and-rds`, `03-marketconnector-ec2`)에서 구체적인 IaC와 배포가 작성될 수 있도록 권고 구조와 결정 근거를 정리한다.

본 design의 핵심 결정은 다음과 같다.

- 8개 MS를 워크로드 특성별로 EC2 / ECS Fargate / AWS Batch / Step Functions + EventBridge Scheduler / Lambda / Beanstalk 중 적합한 후보로 매핑한다. 모두 동일 컴퓨트로 통일하지 않는다.
- 단일 PostgreSQL `portfolio` DB와 schema-per-domain 구조는 그대로 유지하면서 RDS for PostgreSQL로 옮긴다. 기존 `INTEREST_DB_*` 환경변수 키와 `search_path` 정책을 변경하지 않는다.
- 모든 secret은 Secrets Manager / SSM Parameter Store로 외부화한다. broker 토큰 파일과 broker IP 등록 같은 운영 제약을 EC2 + EIP 권고에 반영한다.
- 관측은 CloudWatch Logs / Metric / Alarm을 표준으로 하되, 기존 `port-view`의 `SlackNotificationService`를 유지하면서 인프라 알람은 별도 SNS → Lambda → Slack 경로로 보강한다.
- 단계적 cutover 로드맵을 6 stage로 정의해 한 번에 모든 MS를 옮기지 않는다.

## Architecture

### 현재 (로컬) 구조 요약

- 8개 MS가 같은 호스트의 PostgreSQL `portfolio` DB를 공유한다.
- `port-view`가 운영 콘솔이며 Daily Batch는 다른 MS Python 절대경로(예: `C:\Workspaces\...`)를 subprocess로 호출한다.
- `port-marketconnector` Flask가 KIS broker에 직접 outbound HTTPS로 호출하고 `access_token.txt` 단일 파일로 토큰을 보관한다.
- `port-interest-crawler`가 Selenium/Chrome 기반으로 KRX/Naver를 수집하고, yfinance로 해외 데이터를 수집한다.
- 각 Python MS는 `INTEREST_DB_*` 환경변수로 동일 DB에 연결하며 MS별 `search_path` 우선순위가 다르다.
- Slack 알림은 `port-view`의 `SlackNotificationService`가 webhook URL로 전송한다.

### AWS 목표 구조 (논리)

- **Region**: 단일 region에 단일 VPC. live와 non-prod는 같은 VPC 안에서 환경 tag와 subnet 그룹으로 구분.
- **Networking**: public subnet(ALB, NAT Gateway, marketconnector EC2 옵션), private subnet(ECS Fargate Task, AWS Batch, Lambda VPC, RDS).
- **Data**: 단일 RDS for PostgreSQL `portfolio`. 환경 분리는 인스턴스 또는 DB 분리(권고는 environment별 RDS 인스턴스).
- **Compute**:
  - `port-marketconnector`: EC2 + EIP (또는 NAT Gateway EIP) — broker IP 정책과 단일 토큰 세션 보호.
  - `port-view`: ECS Fargate 1순위, Beanstalk 2순위. Daily Batch orchestration은 EventBridge + Step Functions로 분리 권고.
  - `port-interest-crawler`: Selenium/Chrome 의존 부분은 ECS Fargate Task(EventBridge Scheduler 트리거), yfinance/Naver REST 호출 부분만 분리해 Lambda 후보.
  - `port-interest-preprocessor`: ECS Fargate Task. 짧은 단일 step만 Lambda 후보로 검토.
  - `port_strategy_common`: 별도 컴퓨트 없음. 다른 MS 컨테이너 이미지에 packaging.
  - `port_strategy_decision`: ECS Fargate Task + EventBridge Scheduler.
  - `port_strategy_execution`: ECS Fargate Task + EventBridge Scheduler. intraday monitor는 paper/live 환경에서 짧은 주기 ECS Service 또는 Step Functions Map state 후보.
  - `port_strategy_research`: AWS Batch 1순위(긴 backtest), ECS Fargate 2순위.
- **Secrets**: Secrets Manager(고민감, rotation 후보), SSM Parameter Store(저민감 환경 의존 값).
- **Image registry**: ECR per MS.
- **CI/CD**: GitHub Actions → ECR → ECS / EC2 배포 1순위, CodePipeline + CodeBuild + CodeDeploy 2순위.
- **Observability**: CloudWatch Logs / Metrics / Alarms / EventBridge. 인프라 알람은 SNS → Lambda → Slack webhook. 도메인 알림(Daily Batch, 잔고 등)은 기존 `SlackNotificationService` 유지.

### 컴포넌트 간 흐름

논리 흐름(텍스트 다이어그램):

```
[EventBridge Scheduler]
    ├─> Step Functions: daily-pipeline
    │     ├─> ECS Task: interest-crawler-daily
    │     ├─> ECS Task: interest-preprocessor-daily
    │     ├─> ECS Task: strategy-decision-daily-buy
    │     ├─> ECS Task: strategy-decision-daily-position
    │     ├─> ECS Task: strategy-execution-daily-buy/sell
    │     └─> Lambda: post-batch-summary -> port-view (또는 SlackNotificationService)
    └─> Step Functions: intraday-monitor
          └─> ECS Task: strategy-execution-intraday (loop with wait)

[port-view ECS Service]
    ├─> RDS PostgreSQL (read/write 운영 콘솔 조회 + Daily Batch 메타)
    ├─> port-marketconnector EC2 (HTTP, Service Discovery 또는 ALB internal)
    └─> SlackNotificationService -> Slack webhook (Secrets Manager)

[port-marketconnector EC2 (EIP)]
    ├─> KIS broker (outbound HTTPS, EIP 등록 필요)
    └─> RDS PostgreSQL (connector schema 우선 search_path)

[Secrets Manager / SSM Parameter Store]
    └─> IAM Role 통해 EC2 / ECS Task / Lambda에 주입
```

## MS별 컴퓨트 후보 비교 및 권고

비교 항목: 워크로드 패턴, 외부 의존성, 컴퓨트 후보, 권고, 사유.

### `port-marketconnector`

- 워크로드 패턴: Flask API + 시간 민감한 broker 주문/시세. 단일 access token 파일 공유 필요. broker 측에서 outbound IP 등록 필요 가능성 높음.
- 외부 의존성: KIS API (HTTPS), RDS, Slack 없음.
- 후보 비교
  - EC2 + EIP: 고정 outbound IP, 단일 토큰 파일 보존 용이, broker rate limit 단일성 확보. 운영 부담 약간 있음.
  - ECS Fargate + NAT Gateway EIP: 컨테이너 표준화 가능. 단일 토큰 파일은 EFS 또는 Secrets Manager로 외부화 필요. NAT Gateway EIP 한도 관리 필요.
  - App Runner: 외부 outbound EIP 고정 어려움. 비권고.
- **권고**: EC2 + EIP 1순위, ECS Fargate + NAT Gateway EIP 2순위.
- 근거(EC2 권고 사유): broker IP 등록 정책, `access_token.txt` 단일 세션 제약, 주문 시점 latency 안정성.

### `port-view`

- 워크로드 패턴: Spring Boot 4.1 / Thymeleaf 운영 콘솔. JVM 장기 실행. Daily Batch orchestration 포함.
- 외부 의존성: RDS, marketconnector HTTP, Slack webhook, Daily Batch가 호출하는 외부 MS 실행 경로(현재는 Python 로컬 경로).
- 후보 비교
  - ECS Fargate: 컨테이너 표준화, 무중단 배포 용이.
  - Elastic Beanstalk Tomcat: Spring Boot 친화. 다만 컨테이너 표준화에서 멀어짐.
  - App Runner: 단일 컨테이너 단순 배포. JVM 큰 메모리 freedom 제한 가능.
- **권고**: ECS Fargate 1순위, Beanstalk 2순위.
- Daily Batch 영향: 외부 MS Python 경로 직접 호출 구조는 AWS에서 깨진다. 본 foundation에서는 호출 방식을 Step Functions + EventBridge + ECS RunTask로 옮기는 권고만 명시한다. 실제 Daily Batch 코드 수정은 후속 spec.

### `port-interest-crawler`

- 워크로드 패턴: 일일 수집 + history backfill. Selenium/Chrome 의존(KRX, 일부 Naver). yfinance/Naver REST 호출.
- 외부 의존성: KRX(웹/Selenium), Naver, yfinance.
- 후보 비교
  - Lambda: Selenium/Chrome 패키징 가능하지만 동시 실행, cold start, /tmp 한도 등 한계. KRX 로그인 흐름은 비권고.
  - ECS Fargate Task + EventBridge Scheduler: Selenium/Chrome 컨테이너 안정. 권고.
  - EC2: 가능하지만 24/7일 필요 없음. 비용 비권고.
- **권고**: ECS Fargate Task 1순위. yfinance/Naver REST 호출만 분리해 Lambda 사용 가능.
- 근거: Selenium/Chrome 의존성, KRX 로그인의 stateful 특성.

### `port-interest-preprocessor`

- 워크로드 패턴: raw → pre feature 가공. DB upsert 비중 큼. 외부 holiday API 호출.
- 외부 의존성: RDS, holiday API.
- 후보 비교
  - Lambda: 짧은 step에 적합. 일부 long-running step에서 timeout 위험.
  - ECS Fargate Task: long upsert 안정. 권고.
- **권고**: ECS Fargate Task 1순위. holiday API 같은 짧은 step만 Lambda 분리 검토.

### `port_strategy_common`

- 워크로드 패턴: 순수 라이브러리. DB / HTTP / IO 없음.
- 후보 비교: 별도 컴퓨트 배포 대상 아님.
- **권고**: 다른 MS의 컨테이너 이미지에 packaging.
  - 1순위: git submodule + `pip install ./port_strategy_common`을 Dockerfile 빌드 단계에서 수행.
  - 2순위: wheel/sdist로 빌드 후 CodeArtifact 또는 S3 사설 index에 게시.
- 근거: 순수 함수 코어이므로 별도 컴퓨트 비용을 만들지 않는다.

### `port_strategy_decision`

- 워크로드 패턴: 일일 batch. daily BUY signal과 daily position decision. RDS 연결.
- 후보 비교
  - ECS Fargate Task + EventBridge Scheduler: 권고.
  - AWS Batch: 동시 다수 job 필요 없으면 과한 옵션.
  - Step Functions: 다른 MS와 함께 daily pipeline 묶을 때 유용.
- **권고**: ECS Fargate Task 1순위. Step Functions에 step으로 포함.

### `port_strategy_execution`

- 워크로드 패턴: 일일 + 장중. BUY/SELL execution order 생성, connector 호출, fill sync, position sync, intraday monitor.
- 후보 비교
  - ECS Fargate Task + EventBridge Scheduler: 일일 step에 권고.
  - intraday monitor: ECS Service(상시) 또는 Step Functions Map + Wait 기반 폴링 모델.
- **권고**: 일일은 ECS Fargate Task 1순위. intraday는 Step Functions + ECS RunTask 폴링 모델 1순위, ECS Service 2순위.
- 안전 제약: live BUY/SELL은 자동 재시도 금지. 재시도 정책은 Runbook에서 정의.

### `port_strategy_research`

- 워크로드 패턴: backtest run, analysis, 텍스트 리포트 생성. 장시간 실행 가능.
- 후보 비교
  - AWS Batch: 장시간 / 가변 자원 적합. 권고.
  - ECS Fargate Task: 짧은 backtest에 충분.
  - Step Functions: backtest → analysis → report 흐름 묶기 좋음.
- **권고**: AWS Batch 1순위, ECS Fargate Task 2순위. 리포트 파일은 S3 버킷에 저장 권고.

## RDS for PostgreSQL 전환 설계

### 인스턴스 구성

- 단일 region 단일 VPC.
- 환경별 RDS 인스턴스를 별도로 두는 방식 권고: `portfolio-dev`, `portfolio-paper`, `portfolio-live`.
- live: PostgreSQL 16 이상, multi-AZ, 자동 백업 7~14일, encryption at rest, performance insights on.
- paper: single-AZ 허용, 자동 백업 7일.
- dev: single-AZ 허용, 자동 백업 1~3일.

### Schema 정책

- 10개 schema 유지: `reference, interest, preprocessor, research, decision, execution, connector, ops, legacy, public`.
- 각 MS의 `search_path` 우선순위는 README에 정의된 그대로 유지. 본 spec에서 변경하지 않는다.

### DB Role / 권한

- MS별 role 권고(예시 이름):
  - `marketconnector_app`: `connector`, `execution`, `legacy`, `reference`, `public` 읽기/쓰기 + RDS read-only on others.
  - `crawler_app`: `interest`, `reference` 읽기/쓰기.
  - `preprocessor_app`: `preprocessor` 읽기/쓰기, `interest`, `reference` 읽기.
  - `decision_app`: `decision` 읽기/쓰기, `research`, `preprocessor`, `connector`, `execution`, `reference` 읽기.
  - `execution_app`: `execution` 읽기/쓰기, `connector`, `decision`, `research`, `reference` 읽기.
  - `research_app`: `research` 읽기/쓰기, `preprocessor`, `interest`, `reference` 읽기.
  - `view_app`: 모든 schema 읽기 + `ops` 읽기/쓰기.
- 환경변수 키: 기존 `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD` 유지. 값만 MS별 role로 분기.

### 데이터 이전

- 1순위: `pg_dump` + `pg_restore`(schema-only → data) cutover 방식.
  - 사유: 단일 instance, 단일 DB. AWS DMS 도입 비용 대비 단순성 우선.
- 2순위: AWS DMS(zero-downtime 필요해질 때).
- cutover 단계 개요
  1. 운영자가 모든 MS 실행 정지(특히 broker 주문, fill sync, intraday monitor).
  2. 로컬 PostgreSQL `portfolio` DB에 대한 `pg_dump`(schema + data) 수행.
  3. RDS portfolio-target에 `pg_restore` 수행.
  4. `search_path` / role 권한 검증 SQL 실행(읽기만).
  5. AWS 측 MS를 paper 환경에서 가동.
  6. 검증 통과 후 live cutover.

### 백업 / 복원

- automated backup retention: live 14일, paper 7일, dev 1~3일.
- manual snapshot: 매 cutover 직전, 매 분기.
- point-in-time recovery: live 활성, paper 활성.

## Secrets / 환경변수 설계

### 항목별 매핑

- Secrets Manager (고민감, rotation 후보)
  - KIS app key, app secret, base URL
  - 계좌번호, 계좌 상품 코드
  - access_token.txt 내용(또는 EFS path 우선 후보 — 아래 별도 항목 참조)
  - RDS password (`INTEREST_DB_PASSWORD`)
  - Slack webhook URL
  - Naver API client secret
- SSM Parameter Store (저민감)
  - `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `PORTFOLIO_DB_NAME`, `INTEREST_DB_USER`
  - `PORT_ACCOUNT_NO` (운영자 정책에 따라 SecureString 권고)
  - `PORT_BROKER_NAME`, `PORT_ENVIRONMENT`, `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION`
  - `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT`
  - Naver API client id (저민감 시 일반 Parameter Store 가능)
  - Chrome / ChromeDriver 경로(컨테이너 빌드 시 고정, 불필요할 수 있음)

### `access_token.txt` 보관 위치

- 후보 비교
  - EFS: 단일 broker 세션 전제에서 EC2/ECS 모두 mount 가능. 동시 쓰기 충돌 위험은 운영적으로 단일 instance 보장으로 회피.
  - S3: 단순 보관. 다만 빈번한 read/write에서 latency / consistency 고려 필요.
  - Secrets Manager: 토큰 자체 저장 가능. 다만 갱신 시 Secrets API 호출 비용 / latency / rate limit 고려.
- **권고**: 마켓커넥터를 EC2 단일 instance로 운영하는 한 EC2 로컬 디스크 + 정기 backup to S3 1순위, EFS 2순위.
- 근거: 단일 broker 세션 단일 instance 전제이면 EFS / Secrets Manager 비용/지연을 굳이 들일 필요가 없다. 다만 EC2 교체 시 토큰 백업/복원 절차가 Runbook에 필요하다.

### 환경변수 호환성

- 기존 키 이름(`INTEREST_DB_*`, `PORT_*`)은 그대로 유지한다.
- AWS 측에서는 ECS Task Definition 또는 EC2 user data에서 Secrets Manager / Parameter Store 값을 같은 키 이름으로 주입한다.
- 어떤 secret도 Dockerfile, build artifact, log, 본 문서에 실값으로 기록하지 않는다. 모두 `[REDACTED]`로 표기한다.

## 네트워크 설계

### VPC / Subnet

- 단일 VPC, 다중 AZ.
- Public subnet: ALB, NAT Gateway, marketconnector EC2(EIP 사용 시).
- Private subnet (app): ECS Fargate Task, AWS Batch compute env, Lambda(VPC 연결).
- Private subnet (data): RDS subnet group 전용.
- VPC endpoint 권고: S3, ECR, Secrets Manager, SSM, CloudWatch Logs.

### Outbound IP 정책

- 1순위: marketconnector EC2에 EIP 직접 부여하고 broker에 등록.
- 2순위: NAT Gateway EIP를 broker에 등록(다만 NAT Gateway EIP를 다수 워크로드가 공유하면 다른 outbound도 함께 노출되는 점 주의).
- 본 spec은 marketconnector를 EC2 + 자체 EIP로 권고한다.

### 내부 호출

- `port-view` → `port-marketconnector` 호출은 동일 VPC 내부 호출.
- 1순위: ECS Service Discovery(Cloud Map) 또는 Internal ALB.
- 2순위: 고정 private IP + Security Group 화이트리스트.
- 운영자 접속: SSH 직접 노출 금지. SSM Session Manager 사용.

### 외부 outbound 경로

- KRX, Naver, yfinance, Slack, holiday API, KIS broker는 모두 outbound HTTPS만 필요. 어떤 MS도 외부 inbound 노출이 필요하지 않다.
- ALB inbound는 운영자(인증)와 Slack/외부 webhook callback이 없으면 사실상 운영자 콘솔 접근만 필요. `port-view`만 internal ALB 또는 운영자 전용 public ALB(인증) 뒤에 둔다.

## 컨테이너 이미지와 CI/CD

### ECR 명명 규칙

- `port-view`, `port-interest-crawler`, `port-interest-preprocessor`, `port-strategy-decision`, `port-strategy-execution`, `port-strategy-research`, `port-marketconnector`(컨테이너 옵션 보관용).
- 태그: `:{git-sha}` + `:{env}` 이동 가능 alias(예: `:dev`, `:paper`, `:live`).

### `port_strategy_common` packaging

- 1순위: git submodule + Dockerfile 빌드 단계에서 `pip install ./port_strategy_common`(또는 `pip install -e .`).
- 2순위: wheel/sdist 빌드 후 CodeArtifact 또는 S3 사설 index에 publish, 다른 MS Dockerfile에서 `pip install port-strategy-common==X.Y.Z`.
- 본 spec에서 1순위 권고. CodeArtifact 도입은 후속 spec.

### CI/CD 도구

- 1순위: GitHub Actions → ECR push → ECS RunTask / Service 배포 또는 EC2 CodeDeploy hook.
- 2순위: CodePipeline + CodeBuild + CodeDeploy.
- 본 spec 권고는 1순위. 사유: 8개 MS 저장소가 외부에 분산되어 있고 GitHub workflow가 단순. 다만 CodeBuild는 VPC 내부 빌드(예: RDS migration job)가 필요할 때 보강 도구로 유지.

### 빌드 시 secret 처리

- build-time secret 주입 금지. 이미지에 secret을 굽지 않는다.
- runtime에 IAM Role + Secrets Manager / Parameter Store 주입.

### 환경별 promotion

- `dev` 자동 배포, `paper` manual approval, `live` manual approval + paper 검증 N영업일 통과 조건.
- N 값은 운영자가 결정. 본 spec은 placeholder만 유지.

### EC2 권고 MS의 배포

- `port-marketconnector`가 EC2 1순위인 경우, 컨테이너 외 배포 옵션
  - 1순위: AMI baseline + systemd unit + CodeDeploy(또는 GitHub Actions + S3 + AWS CLI).
  - 2순위: 같은 EC2에 docker engine 설치 후 컨테이너 단일 실행(EIP는 EC2에 유지).
- 본 spec은 1순위 권고. broker 토큰 단일성과 운영 단순성 우선.

## Observability / 알림 / Runbook

### 로그

- log group 명명 규칙: `/portfolio/{env}/{ms}`(예: `/portfolio/live/marketconnector`).
- log retention: live 90일, paper 30일, dev 7~14일.
- 컨테이너는 awslogs driver, EC2는 CloudWatch agent.

### 메트릭 / Alarm

- 표준 메트릭
  - CPU / Memory(인프라 측)
  - RDS connection count, deadlocks, replica lag(없는 경우 제외)
  - ECS Task failure count, exit code 분포
  - EventBridge / Step Functions 실패 count
- 도메인 메트릭(custom)
  - broker API error rate(marketconnector)
  - Daily Batch step 성공/실패 count
  - intraday monitor heartbeat
  - fill sync lag(체결 발생 → sync 반영 지연)
- alarm threshold 권고 — 운영자 데이터로 fine-tune 필요. 본 spec은 placeholder 권고만.

### Slack 알림

- 도메인 알림: 기존 `port-view`의 `SlackNotificationService` 유지. webhook URL은 Secrets Manager에서 주입.
- 인프라 알람: CloudWatch Alarm → SNS → Lambda → Slack webhook.
  - 후보 비교: AWS Chatbot 사용 가능. 다만 Chatbot은 IAM/Slack workspace 등록 필요. 단순 webhook이 운영 부담 적음.
- **권고**: 인프라 알람은 SNS → Lambda → Slack 1순위, AWS Chatbot 2순위.

### Daily Batch 통합

- `strategy_daily_batch_run`, `strategy_daily_batch_step_log` 테이블은 schema 그대로 유지.
- AWS 측 트리거는 EventBridge Scheduler → Step Functions → ECS RunTask.
- 단계 실패 시 Step Functions 실패 → CloudWatch Alarm → SNS → Slack.
- `port-view`의 Daily Batch 화면은 그대로 유지하되 "수동 실행", "실패 step 재실행" 액션은 후속 spec에서 ECS RunTask API 호출로 대체.

### Runbook 항목 (foundation 단계 골격)

- broker 토큰 만료 / 재발급
  - 증상: KIS API 인증 실패.
  - 절차: 운영자 수동 갱신 → Secrets Manager 갱신 → marketconnector restart → broker test call.
  - 자동 재시도: 금지(운영자 수동만).
- KIS 주문 실패
  - 증상: `connector_order_request` 상태에 실패 기록. broker error code 다양.
  - 절차: error code 분류 → 일시적 오류만 운영자 승인 후 재제출 → 영구 오류는 사용자 통보.
  - 자동 재시도: 일부 일시적 오류만 허용. live 환경에서는 자동 재시도 기본 금지.
- RDS failover
  - 증상: 모든 MS connection 실패.
  - 절차: RDS multi-AZ 자동 failover 후 endpoint 동일 → 각 MS connection pool 재시작 → 정합성 검사.
- Daily Batch 실패 재실행
  - 증상: Step Functions execution 실패 / step 일부 실패.
  - 절차: 실패 step 식별 → 입력 idempotent 여부 확인 → idempotent step만 재실행 → 운영자 승인 후 후속 step 재개.
- intraday monitor 중단
  - 증상: heartbeat 메트릭 끊김.
  - 절차: Step Functions / ECS Task 상태 확인 → 재기동 → 누락 구간 재처리는 운영자 승인 필요(주문 영향).

### 자동 재시도 정책

- 자동 재시도 허용: 시세 조회, 전처리 idempotent step, 휴일 API, yfinance / Naver REST 일시 오류.
- 자동 재시도 금지: BUY / SELL 주문, fill sync 결과 반영, position state 변경, intraday stop SELL 생성.

## 환경 분리 / 비용 / 단계적 cutover

### 환경 분리

- dev: broker live 연결 금지. paper or sandbox 모드만 허용. RDS 별도 인스턴스.
- paper: broker paper / 검증 모드만 허용. 실제 주문 금지. RDS 별도 인스턴스.
- live: broker live 연결 허용. multi-AZ RDS, 모든 알람 활성.

### 비용 항목

- EC2 (marketconnector + 옵션 운영용 bastion): low ~ medium.
- ECS Fargate Task: per-execution. daily batch 짧은 실행이면 low.
- RDS: low(dev/paper) ~ medium(live multi-AZ).
- NAT Gateway: 시간당 + 데이터 전송 비용. medium 가능. cost optimization 후보.
- CloudWatch Logs / Metrics / Alarms: low ~ medium(retention에 따라).
- Secrets Manager: secret 수 × 월 단가, low.
- 본 spec은 실제 가격 대신 low / medium / high 추정만 제시. 정확 추정은 후속 spec.
- 환경별(dev / paper / live) 월 비용 시뮬레이션과 NAT Gateway 사용 여부 비교, 두 가지 마이그레이션 권고안(최소 비용 paper 검증형 / 운영 안정성 우선형)은 루트 공통 문서 [cost-simulation.md](../_common/cost-simulation.md) 참고.

### 단계적 cutover 로드맵

- Stage 1: 네트워크 / RDS / Secrets / ECR foundation
  - 진입 조건: 본 spec 승인.
  - 이탈 조건: VPC 또는 RDS 설계 결함 발견.
- Stage 2: `port_strategy_common` packaging + 1개 MS 파일럿
  - 권고 파일럿: `port-interest-preprocessor` 또는 `port_strategy_research`(broker 영향 없음).
  - 진입 조건: Stage 1 완료, ECR / IAM Role 준비.
  - 이탈 조건: packaging 충돌, RDS 권한 문제.
- Stage 3: `port-marketconnector` EC2 + EIP
  - 진입 조건: Stage 2 완료, broker IP 등록 절차 합의, access_token Runbook 합의.
  - 이탈 조건: broker 측 IP 정책 불일치, 토큰 단일성 깨짐.
- Stage 4: `port_strategy_decision` + `port_strategy_execution` (paper 환경 우선)
  - 진입 조건: Stage 3 검증 N영업일 통과(paper).
  - 이탈 조건: paper 환경에서 fill sync 또는 position sync 정합성 불일치.
- Stage 5: `port-view` 운영 콘솔 + Daily Batch 통합(EventBridge + Step Functions)
  - 진입 조건: Stage 4 paper 검증 통과.
  - 이탈 조건: Daily Batch step 매핑 누락.
- Stage 6: live cutover 및 legacy 로컬 운영 종료
  - 진입 조건: paper 환경 N영업일 무결성 + Runbook 리허설 완료.
  - 이탈 조건: live 첫 영업일 broker / 주문 / 정합성 이슈.

## 본 spec의 안전 제약

- 산출물은 `requirements.md`, `design.md`, `tasks.md` 3개 파일만.
- 8개 MS의 기존 README, AGENTS.md, CHANGELOG, docs, worklog는 수정하지 않는다.
- 8개 MS의 Python / Java 소스 코드는 수정하지 않는다.
- 실제 AWS 리소스는 만들지 않는다. IaC도 본 spec에서는 작성하지 않는다.
- 실제 secret 값을 본 문서에 적지 않는다(모두 `[REDACTED]`).
- 8개 MS의 어떤 entrypoint도 실행하지 않는다.

## 후속 spec 후보

본 spec은 foundation 단계이므로 다음 spec 후보를 식별만 해 둔다.

- `02-aws-network-and-rds`: VPC, subnet, SG, RDS 인스턴스 IaC.
- `03-marketconnector-ec2`: marketconnector EC2 + EIP + access_token 운영 절차 + broker 등록 IaC.
- `04-strategy-batch-stepfunctions`: Daily Batch와 intraday monitor의 Step Functions / EventBridge / ECS RunTask 설계 + 코드 변경.
- `05-port-view-ecs-and-runbook`: port-view ECS 배포, Runbook v1 확정, Slack 알림 통합.
- `06-secrets-and-iam`: IAM Role 세분화 + Secrets rotation 정책.
- `07-cicd-pipelines`: GitHub Actions 표준 워크플로 + ECR + 배포 자동화.
