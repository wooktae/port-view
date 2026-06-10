# PORT-STRATEGY-AI AWS Migration 후속 Spec 개요

본 문서는 `01-aws-migration-foundation` spec 이후의 후속 spec 9개의 큰 그림을 정리한다. 운영자 결정 변경(2026-06 갱신)에 맞춰 환경 모델은 local-dev / aws-paper / aws-live 3개로 정리되었고, NAT Gateway는 paper / live 모두 기본 미사용이다. AWS dev 환경은 별도 구축하지 않는다.

각 spec은 별도 폴더(`02-` ~ `10-`)로 만들고 본 문서는 spec 사이의 의존성과 진행 순서를 한눈에 보기 위한 인덱스다.

본 문서는 문서일 뿐이며 실제 AWS 리소스 생성, IaC 작성, 8개 MS 코드 / README / AGENTS.md / docs / CHANGELOG / worklog 수정은 본 작업 범위가 아니다. 모든 secret은 `[REDACTED]`로만 표기한다.

## 환경 모델 (전 spec 공통)

- `local-dev`: 기존 로컬 PostgreSQL 환경 유지. AWS 리소스 없음. 개발 / 단위 검증.
- `aws-paper`: 1차 AWS 구축, KIS 모의투자 기반 실전 리허설. 초기 주문 차단 → 검증 후 자동 BUY/SELL E2E 단계적 허용.
- `aws-live`: paper 검증 후 후속 구축. 실계좌. 초기 자동주문 금지, 후보 + 수동 승인 중심. BUY/SELL/fill sync/position 변경/intraday stop SELL 생성 자동 재시도 금지. idempotent step만 자동 재시도 허용.

## 진행 순서 (운영자 결정 우선순위 기준)

1. `02-aws-network-and-rds`
2. `06-secrets-and-iam`
3. `03-marketconnector-ec2`
4. `08-interest-crawler-and-preprocessor-ecs`
5. `04-strategy-batch-stepfunctions`
6. `05-port-view-ecs-and-runbook`
7. `09-strategy-research-batch`
8. `07-cicd-pipelines`
9. `10-cutover-and-validation-runbook`

### 2026-06-10 후속 메모 (이월 / 우선순위 보정)

- 2026-06-09 작업 결과(`02-aws-network-and-rds`의 RDS 재생성 + DB role 1차 적용)는 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md), [`../02-aws-network-and-rds/validation-checklist.md`](../02-aws-network-and-rds/validation-checklist.md), [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md)에 반영 완료. 03 / 06 spec 입력으로 사용한다.
- `03-marketconnector-ec2` 기본 포팅이 2026-06-09에서 2026-06-10으로 이월. EC2 자체는 RDS restore runner로 이미 생성됐으므로 03 spec은 EC2 재생성보다 Python 실행환경 구성 / 설정 외부화 / connector 검증부터 진행한다.
- 원래 계획이던 `08-interest-crawler-and-preprocessor-ecs` 기본 포팅도 2026-06-10에 진행 후보이지만, 03 이월 작업이 06 단계 결정(KIS / RDS 비밀 주입)과 맞물리므로 03을 우선한다. 08은 03이 안정된 뒤 / 또는 06과 병렬로 진입한다.
- `08-interest-crawler-and-preprocessor-ecs` 1차 진행 결과(2026-06-10 오후·저녁): preprocessor ECS Task 1회 실행 성공(lastStatus `STOPPED` / exitCode `0`), 두 MS 의 Dockerfile / requirements.txt 신규 생성, 로컬 빌드 + ECR push 완료, ECS Cluster / Task Execution Role / Task Role 2종 / Log Group 2종 준비 완료. 검증 도중 발견된 1차 이슈(Secrets Manager JSON `host` key 누락)와 2차 이슈(`public` schema 잔존 sequence 권한 부족)는 운영자 직접 조치로 해소. 자세한 결과는 [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-10 섹션 참조.
- 2026-06-10 이월 항목: (1) crawler Task Definition 등록 / RunTask runtime 검증 / KRX·Naver·yfinance outbound 도달 검증, (2) `public` schema 잔존 sequence 추가 점검(preprocessor 외 도메인 sequence 잔존 여부), (3) crawler 운영 안정화(Selenium / Chromium 런타임)는 본 spec 범위 밖으로 후속 spec / 후속 phase 책임. password rotate 는 본 일자 작업 범위 밖.

## 각 후속 Spec 요약

### 02-aws-network-and-rds

- 목적: VPC / Subnet / Route Table / NAT-free 전략 / VPC Endpoint / Security Group / RDS for PostgreSQL의 단일 portfolio DB + schema-per-domain 구조 + aws-paper / aws-live 분리 기준 확정.
- 대상 MS: 8개 MS 모두(공유 인프라).
- 선행 의존성: `01-aws-migration-foundation` 승인.
- 운영자 결정: 인터넷 outbound 워크로드 매핑(OPT-1~OPT-4), VPC Endpoint 활성 항목, RDS 인스턴스 클래스, aws-live single-AZ vs multi-AZ, schema 권한 매트릭스, cutover 방식.
- 예상 난이도: 중상.
- 비용 영향: 가장 크다. NAT-free로 NAT GW 비용은 없지만 VPC Endpoint와 RDS가 고정비의 대부분을 차지.
- 운영 리스크: cutover 시 DB 정합성, public subnet 배치 ECS Task SG 실수 시 외부 노출 위험.

### 06-secrets-and-iam

- 목적: Secrets Manager / SSM Parameter Store 사용 기준, MS별 IAM Role / Policy, 기존 환경변수 키 호환성 유지 정책 확정. rotation / 감사 / Slack webhook 보관 기준 포함.
- 대상 MS: 8개 MS 모두.
- 선행 의존성: `02-aws-network-and-rds`(VPC Endpoint와 SG 결정), `01-aws-migration-foundation`.
- 운영자 결정: 어떤 secret을 Secrets Manager로 보낼지, rotation 주기, IAM Role 분리 단위, Slack webhook 보관 위치(Secrets Manager vs SSM SecureString).
- 예상 난이도: 중. IAM 매트릭스가 길지만 패턴은 단순.
- 비용 영향: 작음 ~ 중. Secrets Manager는 secret 개당 월 단가 누적.
- 운영 리스크: IAM 권한 누락 시 ECS Task / EC2 부팅 실패. 권한 과다 시 감사 위험.

### 03-marketconnector-ec2

- 목적: `port-marketconnector` EC2 + EIP 권고를 실제 진행 절차로 구체화. broker IP 등록, `access_token.txt` 보관 / 백업 / 복구, EC2 교체 시 토큰 인계 절차, 운영 Runbook 포함.
- 대상 MS: `port-marketconnector`.
- 선행 의존성: `02-aws-network-and-rds`(public subnet, EIP, SG), `06-secrets-and-iam`(KIS 비밀 / DB 비밀 주입).
- 운영자 결정: instance type, EBS 크기, EIP 1개 단일 vs 2 EIP active-passive, 토큰 백업 위치(EC2 로컬 + S3 vs EFS).
- 예상 난이도: 중. broker 측 IP 등록 정책에 의존.
- 비용 영향: 중. EC2 24/7 + EIP attach + EBS + 데이터 전송.
- 운영 리스크: 단일 broker 세션 제약, 토큰 만료, EC2 교체 시 다운타임.
- 2026-06-10 이월 작업: 2026-06-09에 RDS restore runner 용도로 이미 aws-paper MarketConnector EC2(Amazon Linux 2023, public subnet, EIP attach, IAM Role + SSM managed policy)를 생성했다. 03 spec 진입 시 EC2 신규 생성은 생략하고, Python venv / requirements 설치 / `port-marketconnector` 소스 배치 / KIS paper 계좌 설정 외부화 / RDS 접속 설정 외부화(`marketconnector_app` 사용) / `connector_balance.py` / `connector_order_check.py` 검증부터 진행한다. 검증 결과는 03 spec의 runbook / validation-checklist / operation-notes에 반영한다. 통합 검증과 rollback 절차는 03 spec과 [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook)(예정)에 분담한다.
- 2026-06-10 1차 적용 결과:
  - 1차 적용 환경: `aws-paper`, `ap-northeast-2`, MarketConnector EC2 1대.
  - 1차 완료 범위: Python 3.9.25 / venv 구성 + 의존 5종(`requests` / `flask` / `psycopg2-binary` / `psycopg` / `pandas`) 설치 + `marketconnector_app` 기준 RDS 접속 + `connector_balance.py` / `connector_order_check.py` 조회성 실행 + Flask 내부 smoke test + Secrets Manager / SSM Parameter Store env 주입 + Instance Role 기반 Access Key 미사용 — 8건 모두 성공.
  - 범위 밖: EC2 신규 생성, 신규 주문 / 매수 / 매도 / 취소 / 정정 호출, live rotation 자동화, GitHub Actions OIDC / CI/CD Role(07), systemd 또는 startup script 정상 운영 모드 전환(03 후속 task 또는 별도 phase).
  - 04 / 05 / 08 / 09 / 10 spec 인계: EC2 Instance Role 패턴을 ECS Task Role 패턴으로 매핑 / service prefix(`/portfolio/{env}/{service}/*`) 분리 유지 / Access Key 미사용 원칙(IMDSv2 + Role only) 유지 / `paper` / `live` env prefix 분리 유지 / Resource·Action wildcard 금지 정책 유지.

### 08-interest-crawler-and-preprocessor-ecs

- 목적: `port-interest-crawler`, `port-interest-preprocessor`를 ECS Fargate + EventBridge Scheduler로 운영. NAT-free 전제에서 OPT-1(public subnet + assignPublicIp)을 기본으로 두고, KRX 로그인 / Selenium 안정성 미달 시 OPT-3(crawler 전용 EC2 또는 ECS on EC2) 승격 절차. Selenium / Chrome 컨테이너 baseline, 외부 holiday API / Naver / yfinance / KRX outbound 정책, history backfill 절차 포함.
- 대상 MS: `port-interest-crawler`, `port-interest-preprocessor`.
- 선행 의존성: `02-aws-network-and-rds`, `06-secrets-and-iam`.
- 운영자 결정: Task 동시성, retry 정책(idempotent step만), KRX 로그인 흐름 처리 방식, OPT-1 vs OPT-3.
- 예상 난이도: 중. Selenium 컨테이너가 가장 까다롭다.
- 비용 영향: 작음 ~ 중. NAT-free라 NAT 데이터 처리 비용은 없음. public subnet IPv4 사용 + Endpoint 비용이 핵심.
- 운영 리스크: KRX 로그인 차단, Selenium 안정성, 외부 API rate limit.
- 2026-06-10 1차 적용 결과:
  - 1차 적용 환경: `aws-paper`, `ap-northeast-2`. 1차 검증 대상: Preprocessor MS(`port-interest-preprocessor`).
  - 1차 완료 범위: ECR repository 2개(`portfolio-interest-crawler`, `portfolio-interest-preprocessor`, paper / live 미분리) 생성 + 두 MS 의 Dockerfile / requirements.txt 운영자 직접 신규 생성 + 로컬 빌드(`paper-20260610` / `paper-latest`) + ECR push + ECS Cluster `portfolio-paper-cluster` + Task Execution Role + Task Role 2종 + CloudWatch Log Group 2종(retention 14일) + Secrets Manager `/portfolio/paper/rds/preprocessor-app` JSON multi-key + preprocessor Task Definition(family `portfolio-paper-interest-preprocessor`, awsvpc, cpu 512 / memory 1024, ECS `secrets` env 주입) + preprocessor RunTask 1회 실행 성공(public subnet + `assignPublicIp=ENABLED`, lastStatus `STOPPED`, exitCode `0`, `PREPROCESSOR PIPELINE END` 확인).
  - 검증 중 발견·조치: (a) Secrets Manager JSON `host` key 누락 → secret 재생성으로 해소(R-DATA-006). (b) `public` schema 잔존 sequence 2건(`pre_marketbreadth_daily_feature_id_seq`, `pre_macroeconomic_daily_feature_id_seq`)의 `preprocessor_app` USAGE / SELECT 부족 → 운영자 직접 GRANT 로 해소(R-DATA-005 보강).
  - 범위 밖 / 이월: ECS Service 상시 가동 / EventBridge Scheduler / Step Functions, aws-live 적용(10 spec), CI/CD OIDC(07 spec), crawler Task Definition / RunTask runtime / KRX·Naver·yfinance outbound 도달 검증(R-AUTO-005), Selenium / Chromium 런타임 안정화 100%, 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 수정.
  - 04 / 05 / 09 / 10 spec 인계: ECR repository 환경 미분리 정책(image tag / Task Definition / Secrets·SSM path / Task Role / env vars / RDS·broker 설정 6개 항목으로 환경 분리), Task Execution Role(secrets 주입) / Task Role(runtime SDK) 책임 분리, NAT-free public subnet + `assignPublicIp=ENABLED` 패턴, paper / live image tag(`paper-<yyyymmdd>` / `live-<yyyymmdd>`) 전략, Resource·Action wildcard 금지 정책(03 §13 정합) 유지.
  - 자세한 운영자 실행 결과 / 실패 사례 / 조치는 [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-10 섹션 참조.

### 04-strategy-batch-stepfunctions

- 목적: `port_strategy_decision`, `port_strategy_execution`의 일일 batch와 intraday monitor를 EventBridge Scheduler + Step Functions + ECS RunTask로 옮기는 설계와 절차. aws-live BUY / SELL 자동 재시도 금지를 state machine 레벨에서 강제. aws-paper의 자동 BUY/SELL E2E 검증 단계 분리(주문 차단 모드 → 검증 후 단계적 허용).
- 대상 MS: `port_strategy_decision`, `port_strategy_execution`.
- 선행 의존성: `02-aws-network-and-rds`, `06-secrets-and-iam`, `03-marketconnector-ec2`(connector 호출 경로 확정), `08-interest-crawler-and-preprocessor-ecs`(입력 데이터 안정).
- 운영자 결정: Step Functions Standard vs Express, intraday polling 주기, 실패 시 운영자 승인 게이트 위치, paper E2E 단계 전환 기준.
- 예상 난이도: 상. orchestration 흐름이 가장 복잡.
- 비용 영향: 작음 ~ 중. Step Functions transitions 저비용. ECS Task 실행 시간이 비용 좌우.
- 운영 리스크: 잘못된 자동 재시도가 중복 BUY / SELL 주문으로 이어질 수 있음.

### 05-port-view-ecs-and-runbook

- 목적: `port-view`를 ECS Fargate로 옮기고 기존 Daily Batch subprocess 구조를 Step Functions / ECS RunTask 호출로 대체. SlackNotificationService와 인프라 알람 채널의 분리 기준 확정. 운영 콘솔 접근 정책(SSM 포트포워딩 vs internal ALB) 포함. `view_app` execution write 필요성 재검토.
- 대상 MS: `port-view`.
- 선행 의존성: `04-strategy-batch-stepfunctions`(Daily Batch 호출 대상 확정), `02`, `06`.
- 운영자 결정: ALB 도입 여부 및 시점, 인증 방식, JVM heap 크기, Slack webhook 채널 분리.
- 예상 난이도: 중상. Spring Boot + Daily Batch 호출 변경.
- 비용 영향: 중. 24/7 ECS Service. ALB 도입 시 +$17.
- 운영 리스크: 기존 Daily Batch 화면의 실행 / 재실행 액션 호환성.

### 09-strategy-research-batch

- 목적: `port_strategy_research` backtest를 AWS Batch(Fargate compute env) + Step Functions로 운영. report 산출물은 S3로 저장. 비정기 / 장시간 backtest 안정 운영 확보.
- 대상 MS: `port_strategy_research`.
- 선행 의존성: `02-aws-network-and-rds`, `06-secrets-and-iam`. (선택) `04-strategy-batch-stepfunctions` 패턴 재사용.
- 운영자 결정: Batch job queue priority, vCPU / 메모리 캡, 동시 실행 수, S3 lifecycle.
- 예상 난이도: 중.
- 비용 영향: 사용량 기반. backtest 실행 빈도에 비례.
- 운영 리스크: 장시간 실행 중 RDS connection 누수, S3 보관 비용 증가.

### 07-cicd-pipelines

- 목적: GitHub Actions → ECR → ECS / EC2 배포 표준 워크플로 확정. aws-paper 자동 배포, aws-live manual approval. `port_strategy_common` packaging(git submodule 또는 wheel) 결정.
- 대상 MS: 8개 MS 모두(저장소 단위).
- 선행 의존성: `02`, `06`, 그리고 적어도 1개 MS의 ECS 배포 spec(03 또는 08 또는 05) 완료.
- 운영자 결정: GitHub Actions vs CodePipeline, 빌드 OIDC role, 환경별 promotion 게이트.
- 예상 난이도: 중. 첫 MS 표준화 후 나머지는 복제.
- 비용 영향: 작음. GitHub Actions 무료 한도 + ECR storage가 주.
- 운영 리스크: build secret 노출, OIDC trust 잘못 설정, 잘못된 환경에 push.

### 10-cutover-and-validation-runbook

- 목적: local-dev → aws-paper, aws-paper 검증 후 → aws-live cutover Runbook과 검증 스크립트 절차. paper N영업일 검증 정의, live 첫 영업일 모니터링 절차, rollback 조건 표 포함.
- 대상 MS: 8개 MS 모두(통합).
- 선행 의존성: `02` ~ `09` 중 live 대상 spec이 모두 paper 환경에서 검증 통과.
- 운영자 결정: 검증 영업일 수 N, cutover 시작 일자, rollback trigger 기준값, local-dev 병행 운영 종료 시점.
- 예상 난이도: 상.
- 비용 영향: 일시적. 이중 운영 기간이 길어지면 paper + live 합산 비용 증가.
- 운영 리스크: 가장 크다. broker live 연결 시 단 한 번의 잘못된 자동 재시도가 큰 손실로 이어질 수 있다.

## 의존성 다이어그램 (텍스트)

```
01-aws-migration-foundation (완료)
   |
   +-> 02-aws-network-and-rds  ----+
   |                                |
   +-> 06-secrets-and-iam ---------+--+
                                   |  |
                                   v  v
                       03-marketconnector-ec2
                                   |
                                   v
                       08-interest-crawler-and-preprocessor-ecs
                                   |
                                   v
                       04-strategy-batch-stepfunctions
                                   |
                                   v
                       05-port-view-ecs-and-runbook
                                   |
                                   v
                       09-strategy-research-batch (병렬 가능)
                                   |
                                   v
                       07-cicd-pipelines (단계별 채택 가능)
                                   |
                                   v
                       10-cutover-and-validation-runbook
```

## 공통 작업 범위 제한 (모든 후속 spec에 적용)

- 실제 AWS 리소스 생성 / 변경 금지.
- 8개 MS의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 금지.
- 8개 MS entrypoint 실행 금지(broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / 주문 / 체결 / Daily Batch / intraday monitor 호출 금지).
- 실제 secret 출력 금지(`[REDACTED]`만 사용).
- aws-live 자동 재시도 금지: BUY / SELL / fill sync 결과 반영 / position 변경 / intraday stop SELL 생성. idempotent step만 자동 재시도 허용.
- AWS dev 환경은 만들지 않는다(local-dev 유지). 모든 후속 spec에서 dev 환경 구성 항목은 제외하거나 local-dev로 표기.
