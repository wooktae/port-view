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

### 2026-06-12 후속 메모 (08 spec — Hybrid execution model)

- `08-interest-crawler-and-preprocessor-ecs` 가 2026-06-12 Windows EC2 worker 기반 KRX GUI 의존 수집 1차 검증을 마쳤다. 자세한 결과는 [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-12 섹션 참조.
- 1차 운영 가능 상태(2026-06-12 도달):
  - KRX program / KRX shortsell EC2 worker 수집 성공(2026-06-09 / 2026-06-10 / 2026-06-11 각 일자 적재 확인 / `interest_program_raw` 543 → 546 / `interest_shortsell_raw` 189158 → 190205)
  - 다운로드 경로 junction 조치(`C:\Users\USER\Downloads` → `C:\Users\Administrator\Downloads`)
  - RDS Secret(`/portfolio/paper/rds/crawler-app`) + KRX Secret(`/portfolio/paper/krx/crawler-login`) 기반 환경변수 주입
  - EC2 worker daily wrapper(`run_krx_worker_daily.ps1`) 1차 실행 / 재실행 시 idempotent no-op 정상 완료
- runtime 분류 결정(Hybrid execution model):
  - KRX GUI 의존 crawler(KRX program / KRX shortsell): **Windows EC2 worker** 로 분리 확정
  - non-GUI crawler(Naver / yfinance / KRX 비-GUI 경로 후보): **ECS Fargate Task 후보 유지**(범위 재정리는 후속)
  - preprocessor: **ECS Fargate Task 유지**(2026-06-10 1차 검증 결과 그대로)
- 2026-06-12 이월 / 후속 분리:
  1. SSM RunCommand 기반 EC2 worker 무인 실행
  2. EventBridge Scheduler → SSM RunCommand 연계
  3. Step Functions 에서 ECS Task + EC2 worker 혼합 orchestration
  4. CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집
  5. wrapper 내 DB 검증 출력 자동 추가(일자별 row count 출력)
  6. ECS / Fargate non-GUI crawler 범위 재정리(EC2 worker vs ECS Fargate Task 인벤토리 확정)
  7. EC2 worker 작업 완료 후 stop 절차 명시(idle 비용 절감)
  8. KRX GUI 수집 headless 리팩토링은 장기 후보로만 유지(현재 결정은 EC2 worker 사용)
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 04 / 05 / 09 / 10 spec 진입 시 hybrid execution model(EC2 worker + ECS Fargate Task) 전제를 입력으로 사용한다. password rotate 는 본 일자 작업 범위 밖.

### 2026-06-13 후속 메모 (04 spec — Strategy Decision ECS / Fargate 1차 검증)

- `04-strategy-batch-stepfunctions` 가 2026-06-13 `port_strategy_decision` 의 ECS / Fargate 1차 포팅 검증을 마쳤다. 자세한 결과는 [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 섹션 참조.
- 1차 검증 완료 범위(2026-06-13 도달):
  - `port_strategy_decision` Dockerfile / requirements.txt 운영자 직접 신규 생성(이미지 안에 `port_strategy_common` + `port_strategy_decision` vendoring)
  - 로컬 build(`portfolio-strategy-decision:paper-20260613`) + container import smoke 성공(`port_strategy_common` / `daily_buy_signal_run` / `daily_position_signal_run`)
  - ECR repository `portfolio-strategy-decision` 생성 + push(`paper-20260613` / `paper-latest`)
  - CloudWatch Log Group `/portfolio/paper/strategy-decision` (retention 14일) + Secrets Manager `/portfolio/paper/rds/decision-app` JSON multi-key 신규 생성
  - ECS Task Execution Role `portfolio-paper-ecs-task-execution-role` 에 decision-app secret read inline policy 추가(secret ARN 한정 / wildcard 0건) + Task Role `portfolio-paper-decision-task-role` 신규 생성
  - Task Definition 2개 분리 등록(buy-signal / position-signal, awsvpc, Fargate, cpu 512 / memory 1024) — OD-MS-013 정합
  - `daily_buy_signal_run` / `daily_position_signal_run` ECS RunTask 단건 실행 성공(exitCode 0, CloudWatch 로그 출력 + RDS 접속 성공)
  - 검증 결과(`daily_run_id` 44, `run_date` 2026-06-13, `data_date` 2026-06-08, `market_signal` BLOCK, block_watch 1건, position decision 0건)
- 검증 중 발견 / 조치(R-DATA-005 보강):
  - buy-signal 1차 실패 = `research.strategy_block_watch_candidate` 권한 부족 → `decision_app` 권한 보정 후 재실행 성공
  - position-signal 1차 실패 = `relation "strategy_position_state" does not exist` (실제 위치 `execution.strategy_position_state` / `decision_app` 의 execution / decision schema 권한 부족으로 search_path 탐색 실패) → `execution.strategy_position_state` / `decision.strategy_daily_position_decision` 권한 보정 후 재실행 성공
- runtime 분리 / 배포 분리 결정(2026-06-13 도달):
  - 통합 entrypoint(`daily_decision_run.py`) 신규 작성은 보류. 기존 Daily Batch 의 step 6(`DAILY_BUY_SIGNAL`) / step 7(`DAILY_POSITION_SIGNAL`) 구조를 계승해 ECS 에서도 Task Definition 2개로 분리(OD-MS-013).
  - `port_strategy_common` 1차 배포는 vendoring 으로 확정(OD-MS-014). 정식 package / version 관리는 후속 분리.
- 2026-06-13 이월 / 후속 분리:
  1. EventBridge Scheduler → ECS RunTask 연계 (paper / live 시점 분리)
  2. Step Functions Standard / Express 선정 + state machine 정의(buy-signal → position-signal 순서 강제 + 자동 재시도 금지 정책 OD-SAFE-004 반영)
  3. Strategy Execution(`port_strategy_execution`) ECS / Fargate 포팅 검증(자동 BUY / SELL E2E 안전장치 OD-SAFE-001 ~ OD-SAFE-004 반영)
  4. `port_strategy_common` 정식 package / version 관리(wheel + CodeArtifact 또는 git submodule packaging) — 07 / Strategy Common 단계
  5. `decision_app` schema / table 권한 매트릭스 정식 정리 — 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) §4 GRANT / §5 검증 SQL 후속 갱신
  6. crawler ECS Fargate 경로의 runtime 검증(R-AUTO-005)과 본 결과는 별개로 유지
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 04 spec 후속 phase 진입 시 본 일자 1차 검증 결과를 입력으로 사용한다.

### 2026-06-13 후속 메모 (08 spec — SSM RunCommand 자동화 + ECS crawler smoke)

- `08-interest-crawler-and-preprocessor-ecs` 가 2026-06-13 (a) Windows EC2 worker 기반 KRX GUI 의존 수집의 1차 자동화와 (b) ECS crawler Task Definition revision 6 의 Selenium / Chrome / outbound smoke 1차 검증을 마쳤다. 자세한 결과는 [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-13 섹션 참조.
- 1차 자동화 / smoke 완료 범위(2026-06-13 도달):
  - SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1` 흐름 1차 검증 통과(RDP closed 상태에서 EC2 Running 만으로 자동 실행 가능 확인)
  - SSM RunCommand 가 wrapper 를 SYSTEM Session 0 / SessionId 0 에서 직접 실행하는 방식은 KRX GUI 로그인에 부적합으로 판단되어 채택 거부
  - wrapper 최신 로그(`krx_worker_daily_20260613_021904.log`) 의 `[Collected Date] None` 은 idempotent / no-op 정상 완료(2026-06-12 그대로)
  - ECS / Fargate `portfolio-paper-interest-crawler:6` Selenium Chrome smoke RunTask(public-a / public-b + `sgroup-crawler-tasks` + `assignPublicIp = ENABLED`) exitCode 0 / `SELENIUM CHROME SMOKE SUCCESS` / `example.com` / Naver Finance(`Npay 증권`) outbound 도달
  - revision 6 의 `command` 는 실제 daily crawler entrypoint 가 아니라 Selenium / Chrome / outbound smoke 전용. 운영용 Task Definition 분리는 후속 task 58 책임
- 결정 락(2026-06-13): OD-MS-015(SSM RunCommand → Scheduled Task trigger 1차 자동화 방식, SYSTEM Session 0 직접 실행 채택 거부). OD-MS-012(wrapper 기반 수동 실행) 본문은 그대로 유지하고 자동화 진입점 1단계만 OD-MS-015 로 분리.
- 2026-06-13 이월 / 후속 분리:
  1. EventBridge Scheduler → SSM RunCommand → `schtasks /Run` 정기 trigger 연계 (paper / live 시점 분리)
  2. Step Functions 에서 ECS Task + EC2 worker hybrid orchestration
  3. CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집
  4. wrapper 내 DB 검증 출력 자동 추가(`interest_program_raw` / `interest_shortsell_raw` 일자별 row count 출력 / R-AUTO-007 정합)
  5. non-GUI crawler 실제 운영용 Task Definition 분리(인벤토리 확정 + smoke 용 revision 과 운영용 revision 분리)
  6. EC2 worker 작업 완료 후 stop 절차 명시(idle 비용 절감)
  7. KRX GUI 수집 headless 리팩토링은 장기 후보로만 유지(현재 결정은 EC2 worker 사용)
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 08 spec 후속 phase 진입 시 본 일자 자동화 진입점 1단계 검증 결과를 입력으로 사용한다.

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
- 2026-06-13 1차 적용 결과(SSM 자동화 + ECS crawler smoke):
  - 1차 적용 환경: `aws-paper`, `ap-northeast-2`. 1차 검증 대상: KRX GUI 의존 수집의 자동화 진입점 1단계 + ECS crawler Selenium / Chrome / outbound smoke.
  - 1차 완료 범위: SSM Managed Node 점검(`portfolio-paper-crawler-worker`, `whoami` = `nt authority\system`, PowerShell 5.1) + SSM 직접 wrapper 실행 부적합 판단(SessionId 0 / SessionId 2 분리, KRX GUI 로그인 실패) + Windows Scheduled Task `Portfolio-KRX-Worker-Daily`(Administrator interactive 세션 / `Start-ScheduledTask` 1차 검증 / KRX login 성공 / `interest_program` 2026-06-12 1건 / `interest_shortsell` 2026-06-12 349건) + RDP closed 상태에서 SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` 트리거 검증(SUCCESS / Running → Ready / 최신 로그 `krx_worker_daily_20260613_021904.log` / `[Collected Date] None` idempotent) + ECS crawler `portfolio-paper-interest-crawler:6` Selenium Chrome smoke RunTask(public-a / public-b + `sgroup-crawler-tasks` + `assignPublicIp = ENABLED`, exitCode 0, `SELENIUM CHROME SUCCESS`, `example.com` / Naver Finance(`Npay 증권`) 도달) — 8건 모두 통과.
  - 결정 락(2026-06-13): OD-MS-015(SSM RunCommand → `schtasks /Run` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1` 1차 자동화 방식, SYSTEM Session 0 직접 실행 채택 거부, 🟡 잠정).
  - 범위 밖 / 이월: EventBridge Scheduler → SSM RunCommand 정기 trigger 연계, Step Functions hybrid orchestration, CloudWatch Logs Agent / SSM output 기반 EC2 worker 로그 수집, wrapper 내 DB 검증 출력 자동 추가(R-AUTO-007), non-GUI crawler 실제 운영용 Task Definition 분리(task 58), EC2 worker 작업 완료 후 stop 절차 명시(idle 비용 절감), KRX GUI 수집 headless 리팩토링 장기 후보, aws-live 적용(10 spec). 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 본 일자 작업으로 인한 변경 0건. 운영자가 EC2 안에 직접 등록한 Scheduled Task / wrapper / 환경변수 주입 ps1 파일은 사실만 본 spec operation-notes 에 기록(파일 본문 전체 인용 0건).
  - 자세한 운영자 실행 결과 / 실패 사례 / 조치는 [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-13 섹션 참조.

### 04-strategy-batch-stepfunctions

- 목적: `port_strategy_decision`, `port_strategy_execution`의 일일 batch와 intraday monitor를 EventBridge Scheduler + Step Functions + ECS RunTask로 옮기는 설계와 절차. aws-live BUY / SELL 자동 재시도 금지를 state machine 레벨에서 강제. aws-paper의 자동 BUY/SELL E2E 검증 단계 분리(주문 차단 모드 → 검증 후 단계적 허용).
- 대상 MS: `port_strategy_decision`, `port_strategy_execution`.
- 선행 의존성: `02-aws-network-and-rds`, `06-secrets-and-iam`, `03-marketconnector-ec2`(connector 호출 경로 확정), `08-interest-crawler-and-preprocessor-ecs`(입력 데이터 안정).
- 운영자 결정: Step Functions Standard vs Express, intraday polling 주기, 실패 시 운영자 승인 게이트 위치, paper E2E 단계 전환 기준.
- 예상 난이도: 상. orchestration 흐름이 가장 복잡.
- 비용 영향: 작음 ~ 중. Step Functions transitions 저비용. ECS Task 실행 시간이 비용 좌우.
- 운영 리스크: 잘못된 자동 재시도가 중복 BUY / SELL 주문으로 이어질 수 있음.
- 2026-06-13 1차 적용 결과(Strategy Decision 단건):
  - 1차 적용 환경: `aws-paper`, `ap-northeast-2`. 1차 검증 대상: Strategy Decision MS(`port_strategy_decision`) 의 ECS / Fargate 단건 RunTask.
  - 1차 완료 범위: `port_strategy_decision` Dockerfile / requirements.txt 신규 생성(`port_strategy_common` + `port_strategy_decision` vendoring) + 로컬 빌드(`portfolio-strategy-decision:paper-20260613`) + ECR push(`paper-20260613` / `paper-latest`) + CloudWatch Log Group `/portfolio/paper/strategy-decision`(retention 14일) + Secrets Manager `/portfolio/paper/rds/decision-app`(JSON multi-key) + Execution Role 에 decision-app secret read inline policy 추가(wildcard 0건) + Task Role `portfolio-paper-decision-task-role` 신규 + Task Definition 2개 분리 등록(buy-signal / position-signal, awsvpc, Fargate, cpu 512 / memory 1024, 환경변수 5종 `INTEREST_DB_*` 그대로 유지) + `daily_buy_signal_run` / `daily_position_signal_run` ECS RunTask 단건 실행 성공(exitCode 0, CloudWatch 로그 + RDS 접속 성공, `daily_run_id` 44 / `data_date` 2026-06-08 / `market_signal` BLOCK / block_watch 1건 / position decision 0건).
  - 검증 중 발견·조치(R-DATA-005 보강): (a) buy-signal 1차 실패 = `research.strategy_block_watch_candidate` 권한 부족 → `decision_app` 권한 보정 후 재실행 성공. (b) position-signal 1차 실패 = `relation "strategy_position_state" does not exist`(실제 위치 `execution.strategy_position_state`, `decision_app` 의 execution / decision schema 권한 부족) → `execution.strategy_position_state` / `decision.strategy_daily_position_decision` 권한 보정 후 재실행 성공.
  - 결정 락(2026-06-13): OD-MS-013(Task Definition 2개 분리, 통합 `daily_decision_run.py` 보류), OD-MS-014(`port_strategy_common` 1차 vendoring, 정식 package / version 관리는 후속).
  - 범위 밖 / 이월: EventBridge Scheduler → ECS RunTask 연계, Step Functions state machine 정의(buy-signal → position-signal 순서 강제 + 자동 재시도 금지 OD-SAFE-004 반영), Strategy Execution(`port_strategy_execution`) ECS / Fargate 포팅 검증(자동 BUY / SELL E2E OD-SAFE-001 ~ OD-SAFE-004), `port_strategy_common` 정식 package / version 관리(07 / Strategy Common 단계), `decision_app` 권한 매트릭스 정식 정리(02 spec db-roles-and-grants 후속 갱신), aws-live 적용(10 spec). 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 본 일자 작업으로 인한 변경 0건(운영자가 직접 작성한 `port_strategy_decision` Dockerfile / requirements.txt 는 본 spec operation-notes 에 사실로만 기록).
  - 자세한 운영자 실행 결과 / 실패 사례 / 조치는 [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 섹션 참조.

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


### 2026-06-13 후속 메모 (Strategy Execution / MarketConnector 책임 분리 + View Daily Batch 17단계 + Local-to-AWS Paper RDS 원칙)

- 2026-06-13 운영자가 직접 수행한 책임 분리 / View Daily Batch 17단계 재구성 / Local-to-AWS Paper RDS 운영 원칙 정리 결과를 반영. 자세한 결과는 [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-13 섹션, [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 §1 ~ §7, [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS 운영 모드 정리 섹션 참조.
- 1차 완료 범위(2026-06-13 도달):
  - Strategy Execution(`port_strategy_execution`) 의 `execution_repository.py` / `daily_auto_buy_execute_run.py` / `daily_auto_sell_execute_run.py` 책임 분리 — `--execute` 의미를 `READY -> REQUESTED` 상태 전환만 담당하도록 변경, connector 직접 호출 / `sys.path` 삽입 제거, `mark_execution_order_requested()` 추가, SELL `mark_position_sell_ordered()` 호출 제거. 정적 검증(`python -m py_compile`) 통과 + 직접 import / call 검색 0건 + UTF-8 한글 정상 표시.
  - MarketConnector(`port-marketconnector`) 신규 executor `connector_strategy_order_execute.py` 추가(489 insertions). `REQUESTED` + `connector_order_request_id IS NULL` strategy 주문 조회, SELL 우선 / BUY 후순위 정렬, 기본 dry run, `--execute` 시에만 `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` guard + `connector_buy` / `connector_sell` lazy import + `SUBMITTED` / `FAILED` 갱신 + SELL position `SELL_ORDERED` 갱신. 기존 검증된 paper 주문 entrypoint(`connector_buy.py` / `connector_sell.py` / `connector_order_common.py` / `connector_order_check.py` / `connector_balance.py` / `db_config.py` / `config.py` / `token_manager.py`) 변경 0건. dry run 결과 = `[NO_TARGET] REQUESTED strategy order 없음`.
  - View(`port-view`) Daily Batch 17단계 재구성 — `DailyBatchService.java` 에 신규 step `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`(order 12, command `python connector_strategy_order_execute.py --execute`) 추가, `CONNECTOR_ORDER_CHECK`(13) / `SYNC_SELL_FILL`(14) / `SYNC_BUY_FILL`(15) / `SYNC_BUY_POSITION`(16) / `BALANCE_REFRESH`(17) order 조정. `DailyBatchLabelUtils.java` 에 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE -> Strategy 주문 실행` 라벨 추가. isNoTarget 인식 문구 5종 추가. `.\mvnw.cmd clean compile` 성공.
  - Local 개발 / AWS Paper RDS 운영 원칙 정리 — Paper 환경 source of truth = AWS Paper RDS 단일, 로컬에서 실행하더라도 `PORT_ENVIRONMENT=paper` 이면 AWS Paper RDS 사용, SSM Port Forwarding 기반 접속(RDS Private 유지), `localhost` host 만으로 환경 식별 금지, `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` 조합으로 paper 주문 실행 guard, local DB ↔ AWS Paper RDS 간 주문 / 체결 / 포지션 병합 / 동기화 금지.
- 결정 락(2026-06-13): OD-MS-016(Strategy Execution / MarketConnector 책임 분리, 🟡 잠정), OD-ENV-006(Paper 환경 DB source of truth = AWS Paper RDS 단일, 🟡 잠정), OD-ENV-007(Local PC → AWS Paper RDS 접속 방식 = SSM Port Forwarding 만 사용, 🟡 잠정), OD-ENV-008(Local DB ↔ AWS Paper RDS 동기화 미사용, 🟡 잠정).
- 2026-06-13 이월 / 후속 분리:
  1. SSM Port Forwarding runbook 정리 — 02 spec runbook 또는 별도 운영 노트에 SSM Port Forwarding → AWS Paper RDS 접속 절차 정식 기재.
  2. 모든 MS 의 `PORT_ENVIRONMENT` / `PORT_DB_TARGET` 점검 — 8개 MS 의 환경변수 인벤토리에 두 키 정합 여부 점검 + 누락 또는 잘못된 값 식별.
  3. Strategy Execution AWS 포팅 — `port_strategy_execution` ECS / Fargate 포팅 검증(자동 BUY / SELL E2E OD-SAFE-001 ~ OD-SAFE-004 반영). 04 spec 후속 phase 또는 별도 spec 책임.
  4. MarketConnector 신규 executor EC2 배포 — `connector_strategy_order_execute.py` 의 EC2 배포 후보 zip 또는 tag 산출. 03 spec 후속 phase 또는 07 spec(CI/CD) 책임으로 분리.
  5. 평일 또는 안전한 테스트 데이터로 `READY -> REQUESTED -> SUBMITTED` end-to-end dry / integration 검증 — 주말 가드(WEEKEND) 영향으로 본 일자에는 dry run 단계까지만 수행. `--execute` 실호출 0건. SSM Port Forwarding / AWS Paper RDS 접속 정책 정리 후 별도 승인 하에 진행(R-AUTO-009 / R-AUTO-010 / R-AUTO-011 정합).
  6. 신규 View 17단계 운영 전 end-to-end 검증 — Java `DailyBatchService` 변경 후 실제 Daily Batch 시퀀스의 12 ~ 17단계 구간을 평일 / 안전 환경에서 운영 직전 검증(R-AUTO-011 정합).
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 04 / 03 spec 후속 phase 진입 시 본 일자 책임 분리 결과 + Local-to-AWS Paper RDS 운영 원칙을 입력으로 사용한다.


### 2026-06-13 후속 메모 (Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook 1차 본문)

- 2026-06-13 운영자가 직접 수행한 SSM Port Forwarding 기반 로컬 → AWS Paper RDS 연결 1차 실증 검증 결과를 반영. 자세한 결과 / Runbook 1차 본문은 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook 섹션 / [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-13 SSM Port Forwarding 표준 경유지 역할 1차 검증 섹션 / [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 Strategy Execution AWS 포팅 사전 검증 섹션 참조.
- 1차 완료 범위(2026-06-13 도달):
  - 로컬 도구 점검(AWS CLI `2.27.50` / Session Manager Plugin `1.2.814.0`)
  - SSM Port Forwarding 표준 경유지 결정(OD-NET-010) — `portfolio-paper-marketconnector-ec2` (instance id `i-0fce77927b7397b88`) 단일
  - target EC2 SSM Online 점검(ping `Online` / agent `3.3.4515.0`)
  - AWS Paper RDS endpoint 점검(`portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com:5432` / `PubliclyAccessible = False`)
  - SSM Port Forwarding tunnel 오픈 성공(local port `15433`, session id `terraform-vjp3fv3nz73konetcevdzjh9de`, `Port 15433 opened`)
  - Python `psycopg2` 기반 `portfolio_admin` 접속 확인(`inet_server_addr = 10.0.20.165` / `inet_server_port = 5432`)
  - Python `psycopg2` 기반 `execution_app` 접속 확인 — search_path = `execution, decision, research, connector, preprocessor, interest, reference, legacy, public` 정합(OD-DB-006 / OD-DB-007 정합)
  - Local-to-AWS Paper RDS SSM Port Forwarding Runbook 1차 본문 정리 — 사전 점검 / tunnel 오픈 / 환경변수 표준 export / 접속 검증 / MS 별 app role 매핑 / 성공 기준 / 실패·복구 7단계 + [실행]/[확인]/[준비]/[복구] 라벨
- 결정 락(2026-06-13): OD-NET-010(Local-to-AWS Paper RDS SSM Port Forwarding 표준 경유지 = `portfolio-paper-marketconnector-ec2` (`i-0fce77927b7397b88`) / local port `15433` / RDS target `portfolio-paper-rds:5432`, 🟡 잠정). OD-ENV-006 / OD-ENV-007 / OD-ENV-008 본문 변경 없이 본 일자 SSM tunnel + `portfolio_admin` / `execution_app` 접속 1차 실증으로 메모 보강(Status 모두 🟡 잠정 유지).
- 신규 / 보강 리스크: R-AUTO-012(SSM tunnel 의존성 — tunnel 창 종료 시 DB 접속 단절, mitigation = runbook 의 tunnel 유지 조건 명시 / 새 session 재기동, Status `Mitigated`), R-AUTO-013(로컬 psql client 미설치 / PATH 미등록 — Python `psycopg2` 대체 사용 가능, Status `Mitigated`), R-DATA-007 detection 보강(`inet_server_addr` / `inet_server_port` 출력으로 환경 식별 정합성 점검).
- 2026-06-13 이월 / 후속 분리:
  1. psql client 정식 설치 / PATH 등록 — 운영자 로컬 PC 에 PostgreSQL major 18 / full 18.4 client 설치 + 시스템 / 사용자 PATH 등록(R-AUTO-013 정합 / 02 spec runbook 부록 A 정합). 현재는 Python `psycopg2` 대체로 운영 가능.
  2. 모든 MS 의 Paper mode DB 환경변수 점검 — 8개 MS 의 환경변수 인벤토리에 표준 키(`PORT_ENVIRONMENT=paper` / `PORT_DB_TARGET=aws-paper` / `INTEREST_DB_HOST=localhost` / `INTEREST_DB_PORT=15433` / `INTEREST_DB_NAME=portfolio` / `INTEREST_DB_USER=<MS 별 app role>` / `INTEREST_DB_PASSWORD=[REDACTED]`) 정합 여부 점검 + 누락 또는 잘못된 값 식별. MS 별 app role 매핑(Strategy Execution `execution_app` / MarketConnector `marketconnector_app` / Interest Crawler `crawler_app` / Interest Preprocessor `preprocessor_app` / View `view_app`) 입력 사용.
  3. Strategy Execution AWS 포팅 본 phase — `port_strategy_execution` Dockerfile / requirements.txt 신규 생성(`port_strategy_decision` Dockerfile 패턴 정합) + 로컬 build + ECR push + Secrets Manager `/portfolio/paper/rds/execution-app` 신규 생성 + Execution Role inline policy 추가 + Task Role 신규 생성 + ECS Task Definition 등록 + ECS RunTask 단건 실행 검증 — 본 일자 `execution_app` 접속 1차 실증으로 사전 조건 충족.
  4. MarketConnector 신규 executor(`connector_strategy_order_execute.py`) EC2 배포 후보 zip / tag 산출 — 기존 EC2 검증본 덮어쓰기 금지, 신규 배포 후보로 분리 관리. 03 spec 후속 phase 또는 07 spec(CI/CD) 책임.
  5. 평일 또는 안전한 테스트 데이터로 `READY -> REQUESTED -> SUBMITTED` end-to-end dry / integration 검증 — 실제 `--execute` 실호출은 별도 승인 후 진행(R-AUTO-009 / R-AUTO-010 정합).
  6. SSM Port Forwarding session 자동 keep-alive / reconnect — 현재는 수동 재기동 정책(R-AUTO-012 정합) — 운영자 확인 후 후속 도입 여부 결정.
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 04 / 03 / 02 / 06 spec 후속 phase 진입 시 본 일자 SSM Port Forwarding 검증 결과 + Runbook 1차 본문을 입력으로 사용한다.


### 2026-06-13 후속 메모 (Local-to-AWS Paper RDS SSM Port Forwarding 보강 — psql 18 client + pgAdmin4 접속 검증)

- 2026-06-13 같은 일자 앞 SSM Port Forwarding 후속 메모(앞 섹션) 의 후속이며, 운영자가 동일 SSM tunnel 위에서 추가 client 2종(로컬 PostgreSQL 18 `psql.exe` + pgAdmin4) 으로 AWS Paper RDS 접속을 1차 실증한 결과를 반영. 자세한 결과는 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 보강(psql 18 client + pgAdmin4 접속 검증) 섹션 / [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-13 SSM Port Forwarding 표준 경유지 역할 — psql 18 + pgAdmin4 보강 섹션 / [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 Strategy Execution AWS 포팅 사전 검증 — psql 18 + pgAdmin4 보강 섹션 참조.
- 1차 추가 검증 완료 범위(2026-06-13 도달):
  - 로컬 PostgreSQL 18 psql client 직접 경로 실행 — `& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -p 15433 -U portfolio_admin -d portfolio` / client `18.1` / server `18.4` / `SSL TLSv1.3` / `inet_server_addr = 10.0.20.165` / `inet_server_port = 5432` 출력
  - pgAdmin4 Server 등록 — Name `AWS Paper RDS - portfolio` / Host `localhost` / Port `15433` / Maintenance database `portfolio` / Username `portfolio_admin` / SSL mode `Prefer`
  - pgAdmin4 검증 SQL — `current_user = portfolio_admin` / `current_database = portfolio` / `inet_server_addr = 10.0.20.165` / `inet_server_port = 5432` / `search_path = "$user", public`
  - pgAdmin4 SELECT 가능 확인 — connector / decision / execution / interest / ops / preprocessor 6개 schema 의 핵심 table 16개(`connector.connector_account` / `connector.connector_order_request` / `connector.connector_order_event` / `connector.connector_fill` / `decision.strategy_daily_position_decision` / `decision.strategy_daily_run` / `execution.connector_signal_order_map` / `execution.strategy_execution_order` / `execution.strategy_execution_plan` / `execution.strategy_position_state` / `interest.interest_pool` / `ops.strategy_daily_batch_run` / `ops.strategy_daily_batch_step_log` / `preprocessor.pre_agency_analysis` / `preprocessor.pre_news_daily_feature`)
  - SSM Port Forwarding tunnel 위에서 client 3종(Python `psycopg2` / psql 18 / pgAdmin4) 모두 동일하게 동작 1차 실증
  - Local-to-AWS Paper RDS SSM Port Forwarding Runbook 보강 — psql 18 직접 경로 실행 + pgAdmin4 Server 등록 절차 + 성공 기준 추가([`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 보강 §3)
- 결정 락(2026-06-13): OD-NET-011(Local-to-AWS Paper RDS pgAdmin4 사용 원칙 = `localhost:15433` SSM tunnel 만 등록 / RDS endpoint 직접 등록 금지, 🟡 잠정).
- 보강 리스크: R-AUTO-012 detection 보강(pgAdmin4 connection 단절 패턴 / `Connection terminated` / `server closed the connection unexpectedly` 모니터 + tunnel 재기동 후 pgAdmin4 / psql 18 자동 복구). R-AUTO-013 mitigation 보강 — 운영자 로컬 PC 의 PostgreSQL 18 client 가 `C:\Program Files\PostgreSQL\18\bin\psql.exe` 에 이미 설치되어 있어 full path 직접 실행으로 1차 실증 통과(client `18.1` / server `18.4`). 본 리스크는 client 미설치가 아니라 일반 `psql` PATH 미등록 문제로 좁혀짐 — full path 직접 실행 또는 PATH 등록 후속.
- 2026-06-13 이월 / 후속 분리(앞 SSM 후속 메모와 별개로 추가):
  1. 일반 `psql` 명령어 PATH 등록 — `C:\Program Files\PostgreSQL\18\bin` 을 시스템 / 사용자 PATH 에 등록(R-AUTO-013 mitigation 의 영구 보정). 본 일자에는 full path 직접 실행으로 운영 가능. 별도 AWS / RDS 작업 영향 없음 — 운영자 로컬 PC GUI(시스템 속성 → 환경 변수) 또는 `setx PATH ...` 직접 작업.
  2. pgAdmin4 의 환경별(paper / live) 서버 분리 등록 정책 표준화 — 서버 이름 prefix(예: `AWS Paper RDS - portfolio` / 후속 `AWS Live RDS - portfolio`) 정책 표준화로 paper / live 환경 잘못 등록 방지. live 환경 진입 시점(10 spec) 책임.
  3. pgAdmin4 / psql 의 app role 별 비밀번호 보관 — 운영자 로컬 PC 환경 책임. Secrets Manager 에서 pgAdmin4 / psql 로 직접 주입하지 않음. 화면 캡처 / 채팅 / 노트에 평문 기록 금지 원칙 유지(R-DOCS-001 정합).
  4. pgAdmin4 connection pool 의 자동 reconnect 여부 검토 — 현재는 SSM tunnel 재기동 후 pgAdmin4 측 자동 복구가 가능하지만 일부 query session 은 복구 안 될 가능성 존재. 운영자 확인.
  5. psql 18 / pgAdmin4 의 `execution_app` / `marketconnector_app` / `crawler_app` / `preprocessor_app` / `view_app` 별 접속 보강 — 본 일자에는 `portfolio_admin` 으로만 추가 client 검증. 각 MS app role 별 client 접속 검증은 후속(MS 별 환경변수 점검과 함께 진행).
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 04 / 03 / 02 / 06 / 10 spec 후속 phase 진입 시 본 일자 SSM Port Forwarding + 다중 client 검증 결과 + Runbook 보강 본문을 입력으로 사용한다.


### 2026-06-13 후속 메모 (04 spec — Strategy Execution ECS / Fargate 1차 검증)

- `04-strategy-batch-stepfunctions` 가 2026-06-13 `port_strategy_execution` 의 ECS / Fargate 본 phase 1차 포팅 검증을 마쳤다. 자세한 결과는 [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 Strategy Execution ECS / Fargate 1차 포팅 검증 섹션 참조. 같은 일자 앞 섹션들(Strategy Decision ECS / Fargate 1차 포팅 검증 / 책임 분리 + View Daily Batch 17단계 / Local-to-AWS Paper RDS SSM Port Forwarding 사전 검증 / psql 18 + pgAdmin4 보강)의 후속이며, Strategy Execution 본 phase 검증으로 `port_strategy_execution` AWS 포팅의 핵심 진입점이 닫힘.
- 1차 완료 범위(2026-06-13 도달):
  - `port_strategy_execution` Dockerfile / requirements.txt 운영자 직접 신규 생성(Docker build context `C:\Workspaces` / image 내부에 `port_strategy_execution` 소스 vendoring / default CMD 안전한 `py_compile` 계열 / 1차 dependency `psycopg2-binary`)
  - 로컬 build(`portfolio-strategy-execution:paper-20260613` / `portfolio-strategy-execution:paper-latest`) + container `py_compile` smoke + import smoke 통과(`execution_config.py` 의 `INTEREST_DB_PASSWORD` 환경변수 요구 구조 확인 / dummy env 로 통과 / 실제 ECS 실행에서는 Secrets Manager 기반 environment injection 으로 해결)
  - ECR repository `portfolio-strategy-execution` push(`paper-20260613` / `paper-latest`)
  - CloudWatch Log Group `/portfolio/paper/strategy-execution`(retention 14일) + Secrets Manager `/portfolio/paper/rds/execution-app` JSON multi-key(`host` / `port` / `dbname` / `username` / `password`) 신규 생성
  - ECS Task Execution Role(`portfolio-paper-ecs-task-execution-role`) 에 execution-app DB Secret read inline policy 추가(secret ARN 한정 / wildcard 0건 / 03 §13 / OD-SEC-006 정합)
  - ECS Task Role `portfolio-paper-execution-task-role`(trust `ecs-tasks.amazonaws.com`) 확인
  - Network 확인 — cluster `portfolio-paper-cluster` / public-a + public-b / SG `sgroup-strategy-tasks` / RDS SG inbound 5432 + VPC Endpoint SG 443 source 허용 / RunTask `assignPublicIp = ENABLED`(OD-NET-004 정합)
  - 단일 Task Definition `portfolio-paper-strategy-execution` revision 1 ACTIVE(awsvpc / Fargate / cpu 512 / memory 1024 / container `strategy-execution` / `INTEREST_DB_*` 5종 secrets injection / `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` environment 주입)
  - command override 로 7개 entrypoint RunTask 실행 + 기본 `py_compile` RunTask = 총 8종 모두 exitCode 0 / lastStatus STOPPED
  - sync 계열(3종): NO_TARGET 정상 종료(submitted / synced / skipped 모두 0)
  - daily build(2종) + auto execute `--execute`(2종) = 4건: 한국 영업일 아님 / WEEKEND guard 차단 / NO_TARGET / `connector_order_request` 생성 0건 / `READY -> REQUESTED` 실제 전환 0건 / broker / KIS 호출 0건
- runtime / orchestration 결정(2026-06-13 도달):
  - Strategy Execution 은 단일 Task Definition + command override 방식으로 1차 확정(OD-MS-017). Strategy Decision 의 Task Definition 2개 분리(OD-MS-013) 와는 의도적으로 다른 패턴 — 7개 entrypoint 가 같은 image / role / secret / log group / cpu / memory 를 공유하므로 Task Definition 7종 분리는 과도.
  - View Daily Batch 의 7개 step 은 논리적으로 유지 가능 — AWS orchestration 에서는 Step Functions state 별 command override 로 매핑(후속 분리). 본 일자 작업은 ECS RunTask command override 가 가능한 것을 검증한 상태.
  - EventBridge Scheduler 는 정시 시작 트리거 역할(후속).
  - MarketConnector executor `connector_strategy_order_execute.py --execute` 는 본 Task Definition 에 포함하지 않음 — MarketConnector EC2 측 별도 책임(OD-MS-016 / 03 spec 2026-06-13 §1 ~ §5 정합).
- 결정 락(2026-06-13): OD-MS-017(port_strategy_execution Task Definition 운영 방식 = 단일 Task Definition + command override / family `portfolio-paper-strategy-execution` revision 1 ACTIVE / 7개 entrypoint allowlist, 🟡 잠정). OD-MS-007(`port_strategy_execution` 컴퓨트 = ECS Fargate Task + Step Functions + EventBridge Scheduler) / OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) / OD-SAFE-004(자동 재시도 금지 step) 본문은 변경하지 않고 본 일자 RunTask 1차 검증으로 1차 실증 메모만 보강 — Status 기존 값 유지.
- 보강 / 신규 리스크: R-AUTO-001 mitigation·detection 보강(state machine definition review checklist + Step Functions state 별 Retry 없음 검증 + command override script 명 검증). R-AUTO-014 신규(단일 Task Definition + command override 오매핑 위험, mitigation = state ↔ command 1:1 표 + 7종 entrypoint allowlist + 자동 retry 금지 + 임의 command 입력 금지, Status `Open`).
- 2026-06-13 이월 / 후속 분리:
  1. Step Functions Standard / Express 선정 + state machine definition — Strategy Decision 2개 Task Definition(buy-signal / position-signal, OD-MS-013) + Strategy Execution 단일 Task Definition + command override(OD-MS-017) 를 하나의 state machine 으로 연결. BUY / SELL / fill sync / position 변경 step 자동 retry 금지(OD-SAFE-004 / R-AUTO-001), idempotent step(sync 계열 NO_TARGET 종료) 만 자동 retry 허용. state name ↔ command override allowlist 1:1 표 정식 정리(R-AUTO-014).
  2. EventBridge Scheduler → Step Functions / ECS RunTask 정기 트리거 연계 — paper / live 시점 분리.
  3. MarketConnector executor 인계 검증 — Strategy Execution `--execute`(`READY -> REQUESTED`) 후 MarketConnector executor `connector_strategy_order_execute.py --execute`(`REQUESTED -> SUBMITTED`/`FAILED`) end-to-end dry / integration 검증(R-AUTO-009 / R-AUTO-010 / R-AUTO-011 정합).
  4. 평일 또는 안전 테스트 데이터 기반 `READY -> REQUESTED -> SUBMITTED` end-to-end 검증 — `--execute` 실호출은 별도 승인 후 진행(OD-SAFE-001 / OD-SAFE-002 / OD-SAFE-003 정합).
  5. View Daily Batch 의 ProcessBuilder 직접 실행 → 관제 UI 격상 — 장기 / 05 spec. 본 일자 작업 범위 밖. View Daily Batch 17단계(2026-06-13 §4) 자체 구현 변경은 후속 phase 책임.
  6. Strategy Execution `execution_app` 의 schema / table 권한 매트릭스 정식 정리 — 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) §4 GRANT / §5 검증 SQL 후속 갱신(R-DATA-005 정합).
  7. 실패 / skip / manual approval gate 설계 — Step Functions state 별 fail-fast / catch / Choice / manual approval(SNS 또는 View 측 승인 콜백) 패턴 04 spec 후속 phase 책임. live 자동 주문 활성화 결정은 10 spec.
  8. CI/CD OIDC / GitHub Actions 자동 build / push: 후속(07 spec 책임).
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 본 일자 결과는 04 / 05 / 10 후속 phase 입력으로 사용한다.


### 2026-06-13 후속 메모 (09 spec — Strategy Research Batch image 1차 준비)

- `09-strategy-research-batch` 가 2026-06-13 `port_strategy_research` 의 AWS Batch 용 Docker / ECR 1차 준비를 마쳤다. 자세한 결과는 [`../09-strategy-research-batch/operation-notes.md`](../09-strategy-research-batch/operation-notes.md) 2026-06-13 Strategy Research Batch image 1차 준비 섹션 참조. 같은 일자 Strategy Decision / Strategy Execution ECS 1차 검증의 후속이며, Strategy Research 의 최종 컴퓨트 1순위는 AWS Batch 유지(OD-MS-008 본문 변경 없음 / Status 그대로 유지) — ECS Fargate Task 는 2순위이며 본 일자 image smoke 의 대체 수단으로만 활용.
- 1차 완료 범위(2026-06-13 도달):
  - As-Is entrypoint 확인 — 7개 후보(`backtest_research_run` / `backtest_report_run` / `run_extended_analysis` / `block_watch_analysis_run` / `block_watch_backtest_run` / `block_exception_buy_backtest_run` / `block_exception_buy_engine_run`)
  - heavy / light command 분리 — heavy 후보(full backtest / 장시간 research / report 생성 / extended analysis / block 계열) 본 일자 실행 0건 / light(py_compile / import smoke / Docker MS 경계 확인) 만 사용
  - 외부 dependency 확인(`psycopg2-binary` / `pandas` / `numpy`) + 내부 dependency 확인(`port_strategy_research` + `port_strategy_common`)
  - `research_app` DB 환경변수 확인(`INTEREST_DB_PASSWORD` 필수 / `INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / search_path = `research, preprocessor, interest, reference, legacy, public` / OD-DB-006·OD-DB-007 정합)
  - Research → Decision 직접 런타임 의존 제거(OD-MS-018) — Research 내부 adapter 3개 신규 생성(`research_backtest_market_adapter.py` / `research_backtest_filter_adapter.py` / `research_backtest_sizing_adapter.py`) + import 변경 3개 파일(`backtest_engine.py` / `backtest_buy_logic.py` / `block_exception_buy_engine_run.py`)
  - Dockerfile / requirements.txt 운영자 직접 신규 생성(Docker build context `C:\Workspaces` / image 포함 = `port_strategy_research` + `port_strategy_common` / image 제외 = `port_strategy_decision` / 기본 CMD 안전한 `py_compile` 계열 / 1차 dependency `psycopg2-binary` / `pandas` / `numpy`)
  - local docker build(`portfolio-strategy-research:paper-20260613`) + container `py_compile` smoke + import smoke 통과(`port_strategy_research.db_config` / `backtest_engine` / `backtest_buy_logic` / `block_exception_buy_engine_run`) + Docker image MS 경계 확인(`/app/port_strategy_decision` 부재 = OD-MS-018 정합 1차 검증)
  - ECR repository `portfolio-strategy-research` 신규 생성 + push(`paper-20260613` / `paper-latest`) — image size 약 90MB
- runtime / packaging 결정(2026-06-13 도달):
  - Strategy Research 는 AWS Batch 1순위 유지(OD-MS-008 본문 변경 없음). 본 일자 작업은 AWS Batch 용 container image 준비 / ECR push 1차 검증.
  - Docker image 에는 `port_strategy_research` + `port_strategy_common` 만 포함 / `port_strategy_decision` 은 포함하지 않음(OD-MS-018).
  - full backtest / 장시간 research / report 생성 / extended analysis / block 계열 backtest 는 본 일자 실행하지 않음 — 후속 분리(R-AUTO-015 정합).
  - ECS Fargate Task 는 2순위 / 본 일자 image smoke 의 대체 수단으로만 활용.
- 결정 락(2026-06-13): OD-MS-018(Research Batch image dependency boundary = Research 내부 adapter 로 이관 / Batch image 포함 대상 `port_strategy_research` + `port_strategy_common` / `port_strategy_decision` 은 image 에 포함하지 않음, 🟡 잠정).
- 보강 / 신규 리스크: R-NET-002(NAT-free 외부 outbound 정책 — Batch CE subnet 배치 정책 후속 검토) / R-NET-003(VPC Endpoint 누락 시 ECR pull / Secrets / Logs 실패 — 후속 phase 점검) / R-DATA-005(`research_app` schema 권한·search_path 검증 후속) 모두 09 spec 후속 phase 입력으로 메모 보강. 신규 R-AUTO-015(heavy backtest / report job 실수 full 실행 시 Batch 비용·장시간 점유 위험 — `smoke` / `full` job name prefix + allowlist + Batch Job timeout / vCPU / memory 상한 + 최초 SubmitJob 은 no-op / import smoke 만 허용, Status `Open`). 신규 R-DATA-008(Research 내부 adapter 가 `port_strategy_common` 계약 변경을 따라가지 못해 backtest 결과가 Daily Decision 과 불일치 — adapter 는 common 호출만 수행 자체 로직 미포함 + common 변경 시 Research py_compile / import smoke / small sample test 필수 + 장기적으로 adapter 를 common package 로 정식 이동, Status `Open`).
- 2026-06-13 이월 / 후속 분리:
  1. AWS Batch Compute Environment 후보 확인(Fargate vs EC2 managed) + Job Queue 후보 확인 + Job Definition 1차 생성(family / revision / cpu / memory / timeout / job role / execution role / log configuration)
  2. CloudWatch Log Group `/portfolio/paper/strategy-research` 생성 + retention 14일(OD-OBS-002 정합)
  3. Secrets Manager `/portfolio/paper/rds/research-app` JSON multi-key(`host` / `port` / `dbname` / `username` / `password`) 신규 생성
  4. IAM Role 보강 — Execution Role 에 research-app secret read inline policy 추가(secret ARN 한정 / wildcard 0건 / OD-SEC-006 정합) + Job Role `portfolio-paper-research-job-role` 신규 생성 후보
  5. 짧은 no-op / import smoke SubmitJob 검증 — `smoke` job name prefix + light command allowlist 기반(R-AUTO-015 mitigation 정합)
  6. full backtest / 장시간 research / report 생성 — 별도 비용 / 시간 / timeout / 운영자 승인 기준 확정 후 실행. heavy 분류 후보(`backtest_research_run` / `backtest_report_run` / `run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) 별 timeout / vCPU / memory 정식 결정
  7. adapter 중복 구조의 장기 정리 — Research 내부 adapter 3개를 `port_strategy_common` 정식 package / adapter 로 이동(R-DATA-008 mitigation 정합 / OD-MS-005 / OD-MS-014 후속 정합)
  8. Step Functions 통합 / EventBridge Scheduler 연계 — 04 spec 후속 phase / OD-MS-008 보조 정책 정합
  9. CI/CD OIDC / GitHub Actions 자동 build / push: 후속(07 spec)
  10. aws-live cutover: 후속(10 spec)
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지 — 단, 09 의 Batch image 준비는 2026-06-13 에 선행 완료된 것으로 기록. 본 일자 결과는 09 / 07 / 10 후속 phase 입력으로 사용한다.


### 2026-06-15 후속 메모 (09 spec — Strategy Research AWS Batch 실행 검증 완료)

- `09-strategy-research-batch` 가 2026-06-15 (a) AWS Batch 실행 골격(Compute Environment / Job Queue / Job Definition revision 1 / CloudWatch Log Group / Secrets Manager / Execution Role + Job Role) 신규 생성 + smoke SubmitJob 1차 검증, (b) BACKTEST_RESEARCH full 실행 + BACKTEST_REPORT 4개 리포트 생성 본 phase 검증, (c) BACKTEST_REPORT S3 업로드 보강 + Job Definition revision 3 + S3 4개 객체 존재 확인까지 도달했다. 자세한 결과는 [`../09-strategy-research-batch/operation-notes.md`](../09-strategy-research-batch/operation-notes.md) 2026-06-15 3개 섹션 참조. 같은 일자(2026-06-13 Strategy Research Batch image 1차 준비) 후속이며, Strategy Research 의 최종 컴퓨트 1순위는 AWS Batch 유지(OD-MS-008 본문 변경 없음 / Status 그대로 유지) — 본 일자 결과로 1차 실증 메모 보강.
- 1차 완료 범위(2026-06-15 도달):
  - AWS Batch Compute Environment `portfolio-paper-strategy-research-ce`(MANAGED / FARGATE / maxvCpus 4 / state ENABLED / status VALID)
  - Job Queue `portfolio-paper-strategy-research-queue`(priority 10 / state ENABLED / status VALID)
  - Job Definition revision 1 `portfolio-paper-strategy-research:1`(image `paper-latest` / vCPU 1 / memory 2048 / timeout 600초 / FARGATE / assignPublicIp ENABLED / 기본 command 안전한 `py_compile` smoke)
  - CloudWatch Log Group `/portfolio/paper/strategy-research`(retention 14일)
  - Secrets Manager `/portfolio/paper/rds/research-app`(JSON multi-key `host` / `port` / `dbname` / `username` / `password` / secret value 노출 0건 / key presence 검증 완료)
  - Execution Role `portfolio-paper-research-batch-execution-role`(`AmazonECSTaskExecutionRolePolicy` + research-app secret read inline policy / secret ARN 한정 / wildcard 0건 / OD-SEC-006 정합)
  - Job Role `portfolio-paper-research-job-role`(최초 smoke 단계 최소 권한 + 이후 prefix 한정 `s3:PutObject` 추가 / Resource = `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` / public read 0건 / wildcard 0건)
  - py_compile smoke SubmitJob `smoke-strategy-research-import-20260615`(jobId `81ec3581-0204-43ea-8238-a2a6d22f3f28` / `SUCCEEDED` / exitCode 0)
  - DB smoke SubmitJob `smoke-strategy-research-db-20260615`(jobId `5399aa10-0fdd-466b-8079-236d3b7e7e37` / `SUCCEEDED` / exitCode 0 / `db smoke ok` / `research_app` / `portfolio` / schema `research` / search_path 정합 / image pull · secret injection · log delivery 오류 0건)
  - Strategy Common 1차 정합성 확인 완료(별도 컴퓨트 없음 / Decision · Execution · Research image 에 vendoring 유지 / common 주요 모듈 + Research adapter 3개 py_compile 통과 + 각 MS 의 common import smoke 통과 / 표현은 "Strategy Common 1차 정합성 확인 완료 / 정식 package 관리는 후속")
  - BACKTEST_RESEARCH full 실행 SubmitJob `SUCCEEDED` / exitCode 0 / `run_id a39b0b0c-cfe9-474e-8a4a-4ddb33f09567` / `total_return 4.55930879` / `mdd -0.08941942` / `sharpe 2.65561307` / `trade_count 308` / extended analysis 내부 수행 포함
  - BACKTEST_REPORT SubmitJob `SUCCEEDED` / exitCode 0 / 4개 리포트 생성 / `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` 적용
  - BACKTEST_REPORT S3 업로드 보강 — 기존 S3 bucket `portfolio-paper-migration-yukiever` 재사용 / `boto3` requirements.txt 추가 + Docker 내부 import smoke 성공 / report wrapper 옵션 추가(`REPORT_S3_BUCKET` 미설정 시 skip / `REPORT_S3_PREFIX` 기본값 `strategy-research/reports` / `AWS_BATCH_JOB_ID` 기준 하위 경로 분리 / `REPORT_OUTPUT_DIR` 기본값 `/tmp/portfolio-reports` 유지) / Docker rebuild + ECR push(image tag `paper-20260615-report-s3` / image digest sha256 placeholder 사용 / image size 약 106MB / pushedAt 2026-06-15T17:00:10+09:00) / Job Definition revision 3 등록(image `paper-20260615-report-s3` / TaskRole `portfolio-paper-research-job-role`)
  - S3 업로드 SubmitJob `strategy-research-backtest-report-s3-20260615`(jobId `112f5fe4-02f3-4614-a88c-60a9842e1447` / `SUCCEEDED` / exitCode 0 / logStreamName `strategy-research/default/b18e548d46764cd791028088e1d32a6d`)
  - S3 객체 4건 존재 확인(prefix `strategy-research/reports/20260615/112f5fe4-.../` / `01_요약 리포트` ~ `04_추천 리포트` / private 유지 / public read 0건)
- runtime / packaging 결정(2026-06-15 도달):
  - Strategy Research AWS Batch 1순위 실증 1차 완료(OD-MS-008 본문 변경 없음 — 1차 실증 메모만 보강).
  - View Daily Batch 기준 AWS Batch 포팅 대상 = `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종 확정(OD-MS-019 신규 결정).
  - `run_extended_analysis.py` 는 `BACKTEST_RESEARCH` 내부에서 이미 수행되므로 별도 AWS Batch 포팅 대상 제외 + 수동 보조 도구로 분류.
  - `block_watch_*` / `block_exception_buy_*` 4종은 AWS Batch 포팅 대상 제외 + heavy 분류 후속.
  - Research report artifact 보존 = S3 prefix `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/`(OD-MS-019 정합) / 기존 bucket `portfolio-paper-migration-yukiever` 재사용.
  - `port_strategy_common` 1차 배포 방식 = vendoring 유지(OD-MS-014 변경 없음 / 정식 package / version 관리는 후속).
- 결정 락(2026-06-15): OD-MS-019(신규 / 🟡 잠정). OD-MS-008 / OD-MS-018 본문 변경 없이 1차 실증 메모 보강 — Status 모두 기존 값 유지.
- 보강 / 신규 리스크: R-AUTO-015 mitigation·detection 보강 + Status `Open` → `Mitigated` 승격(View Daily Batch SubmitJob 연동 / Step Functions orchestration 전까지 완전 Closed 가 아닌 Mitigated 유지가 적절). R-DATA-008 detection 보강(Strategy Common 1차 정합성 확인 절차 + smoke / sample 비교 절차 후속 문서화). 신규 R-COST-003(Research S3 report 누적 비용 / lifecycle 미설정 위험, mitigation = OD-MS-019 prefix 한정 + Job Role Resource 한정 + public read 0건 + S3 lifecycle 정책 후속 결정 + 06 spec KMS encryption 결정, Status `Open`).
- 6/13 후속 항목 중 본 일자 회수(완료):
  1. AWS Batch Compute Environment / Job Queue / Job Definition revision 1 1차 생성: 완료
  2. CloudWatch Log Group `/portfolio/paper/strategy-research` 생성 + retention 14일: 완료
  3. Secrets Manager `/portfolio/paper/rds/research-app` JSON multi-key 신규 생성: 완료
  4. IAM Execution Role + Job Role 신규 생성(secret ARN 한정 / wildcard 0건) + Job Role 의 prefix 한정 `s3:PutObject` 추가: 완료
  5. 짧은 no-op / import smoke SubmitJob 검증 — `smoke` job name prefix + light command allowlist 기반(R-AUTO-015 mitigation 정합): 완료
  6. full backtest / 장시간 research / report 생성 — 별도 비용 / 시간 / timeout / 운영자 승인 기준 확정 후 실행: 완료(BACKTEST_RESEARCH + BACKTEST_REPORT 단건 검증 / vCPU 1 / memory 2048 / timeout 600초 안에서 1차 통과)
  7. BACKTEST_REPORT S3 업로드 옵션 / `REPORT_OUTPUT_DIR` 분리 / S3 prefix 정합: 완료
- 2026-06-15 이월 / 후속 분리(남은 항목):
  1. View Daily Batch 의 `BACKTEST_RESEARCH` / `BACKTEST_REPORT` step 을 ProcessBuilder 직접 실행 → AWS Batch SubmitJob 호출로 매핑 — 05 / 04 spec 후속 phase 책임
  2. Step Functions state machine 정의 — `BACKTEST_RESEARCH` → `BACKTEST_REPORT` 순서 강제 + 자동 재시도 금지(OD-SAFE-004 / R-AUTO-001 정합) + EventBridge Scheduler 정기 트리거. 04 spec 후속 phase 책임
  3. `block_watch_*` / `block_exception_buy_*` 수동 보조 도구 운영 절차 명문화(09 후속 phase 책임 / heavy 분류 / R-AUTO-015 mitigation 정합 / OD-MS-019 정합)
  4. Research 내부 adapter 3개(`research_backtest_market_adapter` / `research_backtest_filter_adapter` / `research_backtest_sizing_adapter`) → `port_strategy_common` 정식 adapter 이동 — R-DATA-008 mitigation 정합 / OD-MS-005 / OD-MS-014 / OD-MS-018 후속 정합
  5. `port_strategy_common` 정식 package / version 관리(wheel / sdist / CodeArtifact 또는 git submodule) — 07 spec / Strategy Common 단계 책임. 본 일자에는 vendoring 유지(OD-MS-014 변경 없음).
  6. CI/CD OIDC build / push 자동화 — 07 spec 책임
  7. aws-live cutover — 10 spec 책임
  8. S3 lifecycle 정책 결정 + KMS encryption 결정 — R-COST-003 mitigation / 06 후속 phase 책임
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지 — 단, 09 의 BACKTEST_RESEARCH / BACKTEST_REPORT AWS Batch 1차 실행 검증 + S3 업로드 보강은 2026-06-15 에 본 phase 까지 완료된 것으로 기록. 본 일자 결과는 09 / 04 / 05 / 07 / 10 후속 phase 입력으로 사용한다.


### 2026-06-15 후속 메모 (08 spec — Backend AWS E2E dry-run 1차 + Interest Crawler 상태 재판정)

- 운영자가 2026-06-15 직접 수행한 (a) AWS 계정 / region / EC2 / ECS / AWS Batch 사전 점검, (b) MarketConnector EC2 기반 `CONNECTOR_BALANCE` 1차 실행, (c) Windows EC2 worker 기반 KRX worker 재실행 + KRX raw 최신일 점검, (d) non-GUI raw 최신일 SQL 점검, (e) preprocessor ECS RunTask 단발 실행 + DB `updated_at` 갱신 확인 결과를 반영. View 진입 전 backend AWS 측 dry-run 을 17단계 순서로 1차 점검해 stale raw data 이슈를 조기 식별. 자세한 결과는 [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-15 §1 ~ §8 참조. 같은 일자 Strategy Research AWS Batch 골격 + full / report + S3 업로드 후속 메모(앞 섹션) 와 별개의 두 번째 세션이며, 진행 순서(08 → 04 → 05 → 09 → 07 → 10) 자체는 변경 없음.
- 1차 완료 범위(2026-06-15 도달):
  - 사전 점검 — region `ap-northeast-2` / account 확인 / 로컬 AWS CLI 기본 실행 가능 / MarketConnector EC2 running / Windows crawler worker running / SSM managed instance Online / `portfolio-paper-cluster` ACTIVE / preprocessor·decision buy-signal·decision position-signal task definition / Strategy Research Compute Environment·Job Queue·Job Definition active revision(BACKTEST_REPORT S3 upload 포함 latest revision = `portfolio-paper-strategy-research:3`) ENABLED / VALID / Healthy
  - `CONNECTOR_BALANCE`(Backend E2E dry-run 1번) 완료 — MarketConnector EC2(`/home/ec2-user/apps/port-marketconnector` / venv python / SSM RunCommand) / KIS Secrets JSON key parsing 정정(`APP_KEY` → `KIS_APP_KEY` / `APP_SECRET` → `KIS_APP_SECRET` / `PAPER_ACNT` → `KIS_PAPER_ACNT` / `ACNT_PRDT_CD` → `KIS_ACNT_PRDT_CD`) / 기존 `access_token.txt` 백업 / 신규 token 발급 / KIS balance API status 200 / 모의투자 잔고 조회 / `connector_balance_snapshot` 저장 / `connector_position_snapshot` 보유종목 0건 처리 / legacy holdings 0건
  - KRX GUI worker 재실행 idempotent 정상 완료 — `KRX already logged in` / `KRX Login Ready` / `[Collected Date] None` / `interest_program_raw` · `interest_shortsell_raw` 최신일 2026-06-12(직전 거래일까지 적재 정상)
  - non-GUI raw 7종 최신일자 SQL 점검 — `interest_agency_raw` 2026-06-11 / `interest_news_raw` 2026-06-11 / `interest_commodity_raw` · `interest_foreignindex_raw` · `interest_investorflow_raw` · `interest_marketbreadth_raw` · `interest_price_raw` 모두 2026-06-08 / `interest_ticker_value_raw` 2026-03-09(dry-run 핵심 차단 요인 제외)
  - preprocessor ECS RunTask 단발 실행 성공 — `portfolio-paper-cluster` / `portfolio-paper-interest-preprocessor:1` / FARGATE / awsvpc / public-a + public-b / `assignPublicIp = ENABLED` / `sgroup-preprocessor-tasks` / lastStatus `STOPPED` / stopCode `EssentialContainerExited` / container `interest-preprocessor` / exitCode 0 / 약 3분 43초 / DB `updated_at` 2026-06-15 11:03:55+00(KST 2026-06-15 20:03:55) 갱신 확인 / 신규 2026-06-15 feature date 0건 — 원인은 raw 최신성 부족
  - Secret · IAM 권한 분리 1차 실증 — MarketConnector EC2 role(`portfolio-paper-marketconnector-ec2-role`)에서 `/portfolio/paper/rds/preprocessor-app` `GetSecretValue` `AccessDeniedException`. 장애가 아니라 OD-SEC-006 / OD-DB-008 정합으로 정상 동작 / 권한 추가 0건 / preprocessor DB 확인은 preprocessor ECS Task 또는 운영자 로컬 SSM Port Forwarding(OD-NET-010 / OD-NET-011) 으로만 수행
  - Backend AWS E2E dry-run 17단계 진행 상태 정리 — 1번 완료 / 2번 부분 완료 / 3번 실행 완료(데이터 최신성 제약) / 4 ~ 7번 미진행 / 8 ~ 17번 미진행 또는 dry-run skip 예정. 실제 BUY / SELL / `--execute` 주문 전송 0건 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건
- 결정 락(2026-06-15 두 번째 세션): OD-MS-020(Interest Crawler 상태 재판정 / hybrid 1차 구현 부분 완료 표현 통일, 🟡 잠정), OD-MS-021(Backend AWS E2E dry-run 17단계 순서 + 안전 기준, 🟡 잠정). OD-SEC-006 본문 변경 없이 1차 실증 메모만 보강 — Status 🟡 잠정 유지. OD-DB-008(marketconnector_app execution R-only) 본문 변경 없음.
- 보강 / 신규 리스크: R-DATA-009 신규(smoke 검증을 daily 데이터 최신성 완료로 오해할 위험, Status `Open`), R-DATA-010 신규(raw 최신성 부족으로 downstream Research / Decision 결과가 stale data 기반이 될 위험, Status `Open`). 기존 R-DATA-005 / R-DATA-006 / R-DATA-007 / R-DATA-008 / R-COST-003 본문 변경 없음.
- 표현 보정(OD-MS-020 정합):
  - 기존 표현 — "Interest Crawler 완성: 완료" / "Interest Crawler 는 hybrid execution model 기준으로 1차 완성"
  - 보정 표현 — "Interest Crawler hybrid 1차 구현: 부분 완료" / "KRX GUI worker 는 운영 가능 상태로 1차 완성" / "ECS · Fargate crawler 는 smoke 검증 완료" / "non-GUI daily raw 수집 운영 경로와 raw 전체 최신성 검증은 후속"
  - 원칙 — "완료" 표기는 실제 데이터 적재 / 최신성 검증까지 확인된 경우에만 사용 / KRX worker 완료와 Interest Crawler 전체 완료를 혼동 금지 / smoke 성공과 daily raw 최신성 성공을 분리
- 2026-06-15 두 번째 세션 이월 / 후속 분리(남은 항목):
  1. non-GUI Interest Crawler 운영 실행 경로 정리 — `interest_crawler_daily.py` 에서 KRX GUI 단계 제외한 실행 경로 분리 + ECS / Fargate 용 non-GUI crawler command 분리. 대상 후보 = `interest_news.py` / `interest_agency.py` / `interest_foreignindex.py` / `interest_commodity.py` / `interest_macroeconomic.py` / `interest_price.py` / `interest_investorflow.py` / `interest_marketbreadth.py`. 제외 후보 = `interest_krx_login_new.py` / `interest_program.py` / `interest_shortsell.py` / `interest_ticker_value.py`. 08 spec task 58 / task 72 와 합쳐 진행
  2. interest raw 최신성 검증 자동화 — non-GUI raw 7종 + KRX raw 2종의 `MAX(trade_date)` 와 직전 거래일 비교 SQL 정기 실행. 08 spec task 73 / R-DATA-009 / R-DATA-010 mitigation 정합
  3. preprocessor 실행 후 raw / feature 최신성 검증 SQL 자동화 — preprocessor `updated_at` 갱신 후 신규 feature date 생성 여부를 자동 비교. 08 spec task 78 / R-DATA-010 mitigation 정합
  4. raw 최신성 회복 — `interest_price_raw` / `interest_investorflow_raw` / `interest_marketbreadth_raw` / `interest_commodity_raw` / `interest_foreignindex_raw` / `interest_news_raw` / `interest_agency_raw` 직전 거래일 적재. 08 spec task 74
  5. preprocessor 재실행 — raw 최신성 회복 후 `portfolio-paper-interest-preprocessor` 재실행 → exitCode 0 확인 → feature table max date / `updated_at` 확인 → 신규 feature date 생성 여부 확인. 08 spec task 77
  6. Backend E2E dry-run 재개 — `BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL` 순서로 진행. Research 가 Decision 보다 먼저 실행. 주문 전송 / execution 계열은 안전 기준에 따라 skip 또는 dry-run 만 수행(OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 / R-AUTO-010 / R-AUTO-011 정합)
  7. 표현 통일 점검 — 운영 문서 / 보고 / 슬라이드 등에서 "Interest Crawler 완성: 완료" 잔존 grep 정기 점검(R-DATA-009 detection 정합)
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 단, 08 의 hybrid 1차 구현은 본 일자(2026-06-15) 시점에 **부분 완료** 로 정리되었으며, non-GUI daily 운영 경로와 raw 최신성 회복은 후속 spec / 후속 phase 책임으로 명시한다. 본 일자 결과는 04 / 05 / 09 / 10 후속 phase 입력으로 사용한다.


### 2026-06-16 후속 메모 (08 spec — Crawler 데이터 미수집 해결 + KRX EC2 자동화 성공)

- 운영자가 2026-06-16 직접 수행한 (a) Crawler 데이터 미수집 원인 진단(rev6 = Selenium / Chrome smoke command / 원본 `interest_crawler_daily.py` KRX GUI 단계 포함 / non-GUI orchestration 부재), (b) `interest_crawler_daily_nongui.py` 신규 생성 + Docker rebuild + ECR push + ECS Task Definition `portfolio-paper-interest-crawler:7` 등록 + RunTask exitCode 0 / 약 9분 51초 / 전체 step SUCCESS, (c) raw 최신성 회복(non-GUI 6종 + KRX 2종 + news / agency), (d) Windows EC2 worker Autologon + Administrator console session Active + SSM RunCommand → `schtasks /Run` → Scheduled Task → Administrator interactive session 흐름으로 KRX login / program / shortsell 2026-06-15 적재 성공 결과를 반영. 자세한 결과는 [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-16 §1 ~ §5 참조. 본 일자로 Interest Crawler hybrid 구조 완료 / Crawler 데이터 미수집 해결 완료 / Preprocessor ECS 재실행 가능 상태 도달.
- 1차 완료 범위:
  1. non-GUI crawler 실제 운영용 ECS Task Definition revision 7 분리(image `paper-20260616-nongui` / command `["python", "interest_crawler_daily_nongui.py"]` / log stream prefix `ecs-crawler-nongui-daily` / FARGATE / awsvpc / cpu 1024 / memory 2048 / crawler-app DB secret 주입 유지)
  2. RunTask exitCode 0 / failures 0 / lastStatus `STOPPED` / stopCode `EssentialContainerExited` / 실행 시간 약 9분 51초 / CloudWatch log stream 생성 / 전체 step SUCCESS(`interest_news` / `interest_agency` / `interest_foreignindex` / `interest_commodity` / `interest_macroeconomic` / `interest_price` / `interest_investorflow` / `interest_marketbreadth`)
  3. raw 최신성 회복 — `interest_price_raw`(2026-06-08 → 2026-06-15 / 1,207,904 → 1,209,624) / `interest_investorflow_raw`(2026-06-08 → 2026-06-15 / 274,204 → 275,949) / `interest_marketbreadth_raw`(2026-06-08 → 2026-06-15 / 4,788 → 4,793) / `interest_commodity_raw`(2026-06-08 → 2026-06-15 / 29,132 → 29,162) / `interest_foreignindex_raw`(2026-06-08 → 2026-06-15 / 33,738 → 33,766 / HANGSENG · NIKKEI225 · SHANGHAI 일부 NULL Data 는 non-blocker 분리) / `interest_news_raw`(2026-06-11 → 2026-06-16 / 68,881 → 71,614) / `interest_agency_raw`(2026-06-11 → 2026-06-16 / 49,018 → 49,044) / `interest_macroeconomic_raw`(2026-06-08 → 2026-06-15 / 75,742 → 75,777). KRX `interest_program_raw` 547 → 548 / `interest_shortsell_raw` 190,554 → 190,903.
  4. KRX EC2 worker 자동화 재검증 — Microsoft Sysinternals Autologon 적용 / EC2 재부팅 후 SSM Online + `query user` Administrator console session Active(USERNAME `administrator` / SESSIONNAME `console` / ID `1` / STATE `Active`) / SSM RunCommand → `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"` SUCCESS / Status `Running` → `Ready` / Last Result `267009` → `0` / Last Run Time 2026-06-16 04:55:49 / wrapper 로그 `C:\portfolio\logs\krx_worker_daily_20260616_045550.log` / KRX login 성공(elapsed 92.83s) / `interest_program` 2026-06-15 Collected / `interest_shortsell` 2026-06-15 349 Company / wrapper `DONE :: KRX worker daily`.
- 결정 락(2026-06-16):
  1. OD-MS-022(KRX GUI crawler 자동 로그인 기반 운영 방식 = Windows Autologon + Administrator interactive session + Scheduled Task + SSM trigger / SSM direct Python · wrapper 실행은 SYSTEM Session 0 / 비대화형 GUI 한계로 운영 방식에서 제외 / Headless · 비대화형 KRX 수집은 로컬 검증상 운영 방식에서 제외 / Autologon 은 paper 전용 Windows worker 보안 예외, 🟡 잠정).
  2. OD-MS-011(port-interest-crawler runtime 분리 Hybrid execution model) / OD-MS-015(KRX GUI 의존 crawler 1차 자동화 방식 = SSM RunCommand → `schtasks /Run` → Scheduled Task) / OD-MS-020(Interest Crawler 상태 표현 / 완료 정의) 본문 변경 없이 1차 실증 메모만 보강 — Status 모두 🟡 잠정 유지. 표현 보정(OD-MS-020 정합) — 이전 "hybrid 1차 구현 부분 완료" → "hybrid 구조 완료(non-GUI rev7 운영 경로 생성 + RunTask 성공 + raw 최신성 회복 + KRX GUI = Windows EC2 worker + Autologon + Scheduled Task + SSM trigger)". Preprocessor 는 "raw 입력 데이터 회복 후 ECS 재실행 가능 상태 도달" 까지만 기록("완료" 표기는 사용하지 않음).
- 보강·신규 리스크:
  1. R-SEC-009 신규(Windows Autologon 보안 예외 — paper 전용 worker 한정 / RDP inbound 제한 / 자동 로그인 자격 증명 문서화 금지 / Administrator password 미기록 / EC2 stop 절차 / 추후 전용 local user 검토, Status `Open`).
  2. R-AUTO-016 신규(Administrator interactive session 부재 시 KRX GUI 수집 실패, Status `Open`).
  3. R-AUTO-017 신규(Chrome process 잔존 — wrapper 종료 시 Chrome 정리 옵션 검토 / 다음 실행 전 정리, Status `Open`).
  4. R-AUTO-008 detection·mitigation 보강(2026-06-16 Autologon bootstrap + Administrator console session Active 확인 + Scheduled Task trigger 흐름이 본 일자 1차 실증 통과).
  5. R-DATA-009 / R-DATA-010 mitigation 보강(2026-06-16 raw 최신성 회복으로 1차 실증 / `interest_foreignindex_raw` HANGSENG · NIKKEI225 · SHANGHAI NULL Data 는 본 일자 Preprocessor blocker 가 아닌 non-blocker 후보로 분리).
- 2026-06-15 이월 항목 중 본 일자 회수(완료):
  1. non-GUI crawler 실제 운영용 Task Definition 분리(task 58 / task 72) — 완료
  2. raw 최신성 회복(task 74) — 완료
  3. non-GUI raw 최신일자 SQL 점검(task 73) — 1차 회복 적재 결과 확인 / 정기 자동화는 task 78 후속
  4. KRX EC2 worker Autologon bootstrap(task 86) — 완료
  5. KRX EC2 worker Scheduled Task trigger 재검증(task 88) — 완료
  6. KRX program / shortsell 2026-06-15 DB 최신성 확인(task 89) — 완료
  7. Preprocessor 재실행 가능 상태 판단(task 91) — 완료
- 남은 후속(본 일자 이월):
  1. Preprocessor ECS 재실행(task 77) — `portfolio-paper-interest-preprocessor` 재실행 → exitCode 0 확인 → feature table max date / `updated_at` 확인 → 신규 feature date 생성 여부 확인
  2. Backend AWS E2E dry-run 재개 — `BACKTEST_RESEARCH` → `BACKTEST_REPORT` → `DAILY_BUY_SIGNAL` → `DAILY_POSITION_SIGNAL` 순서. Research 가 Decision 보다 먼저 실행. 주문 전송 / execution 계열은 안전 기준에 따라 skip 또는 dry-run 만 수행(OD-MS-021 정합)
  3. EventBridge Scheduler → SSM RunCommand → `schtasks /Run` 정기 trigger 연계(task 54)
  4. Step Functions 에서 ECS Task + EC2 worker hybrid orchestration(task 55)
  5. CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집(task 56)
  6. wrapper 내 DB 검증 출력 자동 추가(task 57 / R-AUTO-007 정합)
  7. EC2 worker 작업 완료 후 stop 절차 명시(task 59 / 비용 절감 / R-COST 후보)
  8. Chrome process 정리 옵션 검토(task 92 / R-AUTO-017 신규)
  9. preprocessor 실행 후 raw / feature 최신성 검증 SQL 자동화(task 78 / R-DATA-009 / R-DATA-010 mitigation 정합)
  10. `interest_foreignindex_raw` HANGSENG / NIKKEI225 / SHANGHAI NULL Data 후속 점검(non-blocker)
  11. `interest_ticker_value_raw` 최신성 회복(2026-03-09 → 직전 거래일 / 별도 후속)
  12. View 구현 — 후속 예정
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 04 / 05 / 09 / 10 spec 후속 phase 진입 시 본 일자 hybrid 구조 완료 + Preprocessor 재실행 가능 상태 + KRX GUI 자동 로그인 운영 방식 결과를 입력으로 사용한다. password rotate 는 본 일자 작업 범위 밖.


### 2026-06-16 후속 메모 (Backend AWS E2E dry-run safe subset 재개 — 09 / 04 spec)

- 운영자가 2026-06-16 같은 일자 첫 번째 세션(Crawler 데이터 미수집 해결 + KRX EC2 자동화 성공) 후속으로 직접 수행한 (a) BACKTEST_RESEARCH AWS Batch 단건 재실행(`portfolio-paper-strategy-research:5` `SUCCEEDED` / run_id `439d78e7-41fd-4bb7-b455-18564ddff758` / backtest_end_date `2026-06-15` / total_return `4.66534417` / mdd `-0.08941942` / sharpe `2.68071466` / trade_count `310`), (b) BACKTEST_REPORT 정식 Job Definition rev1 ~ rev3 교정 진행 + 최종 `portfolio-paper-strategy-report:3` SubmitJob `SUCCEEDED` + S3 prefix `strategy-research/reports/20260616/67522706-9b5f-4770-a312-ceb1987c4655/` 안 4개 객체 존재 확인, (c) DAILY_BUY_SIGNAL 단건 ECS / Fargate 재실행(exitCode 0 / `decision.strategy_daily_signal` row 4건 `READY` / signal_date `2026-06-16`), (d) DAILY_POSITION_SIGNAL 단건 ECS / Fargate 재실행(exitCode 0 / positions 0 정상 skip — 보유 포지션 0건 정상 케이스) 결과를 반영. 자세한 결과는 [`../09-strategy-research-batch/operation-notes.md`](../09-strategy-research-batch/operation-notes.md) 2026-06-16 §1 ~ §8 / [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-16 §1 ~ §7 참조.
- 1차 완료 범위 (Backend AWS E2E dry-run safe subset, OD-MS-021 17단계 정합):
  1. 1번 `CONNECTOR_BALANCE`: 완료(2026-06-15 / 03 spec)
  2. 2번 `INTEREST_CRAWLER`: 완료(2026-06-16 첫 번째 세션 / 08 spec — non-GUI rev7 + KRX EC2 worker hybrid 구조 완료 + raw 최신성 회복)
  3. 3번 `PREPROCESSOR`: 실행 완료(2026-06-15 / 08 spec — raw 입력 데이터 회복 후 재실행 가능 상태 도달까지 1차 / 신규 raw 입력 기반 재실행은 후속 task 77)
  4. 4번 `BACKTEST_RESEARCH`: 완료(2026-06-16 / 09 spec)
  5. 5번 `BACKTEST_REPORT`: 완료(2026-06-16 / 09 spec — `portfolio-paper-strategy-report:3` 최종 확정 + S3 4개 객체)
  6. 6번 `DAILY_BUY_SIGNAL`: 완료(2026-06-16 / 04 spec — row 4건 `READY`)
  7. 7번 `DAILY_POSITION_SIGNAL`: 완료(2026-06-16 / 04 spec — positions 0 정상 skip)
- BACKTEST_REPORT 운영 경로 교정(closed 성격): rev1(`python -m port_strategy_research.backtest_report_run` / local-only) / rev2(`python aws_batch_backtest_report_wrapper.py` / 컨테이너 내 파일 경로 부재로 실패) → rev3(`python -m port_strategy_research.aws_batch_backtest_report_wrapper` / 최종 운영 경로 / image `paper-20260615-report-s3` / env `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` / `REPORT_S3_BUCKET=portfolio-paper-migration-yukiever` / `REPORT_S3_PREFIX=strategy-research/reports`). Job Role 의 S3 PutObject Resource 는 prefix 한정 / public read 0건 / wildcard 0건 유지(OD-MS-019 / R-COST-003 정합).
- DAILY_POSITION_SIGNAL positions 0 정상 skip 정리: `decision.strategy_daily_position_decision` 신규 row 미생성은 보유 포지션 0건(`connector_order_request` 0건 / `strategy_position_state` OPEN 0건)으로 인한 정상 운영 케이스 — 오류 / R-AUTO-001 / R-AUTO-002 위반 아님. 04 spec operation-notes 2026-06-16 §2 / §4 에만 사실 기록(리스크 register 변경 0건).
- 결정 락(2026-06-16 두 번째 세션): 신규 OD 0건. OD-MS-008(Strategy Research 컴퓨트 1순위 = AWS Batch) / OD-MS-013(Strategy Decision Task Definition 2개 분리) / OD-MS-019(BACKTEST_RESEARCH + BACKTEST_REPORT 2종 한정 + report artifact S3 prefix) / OD-MS-021(Backend AWS E2E dry-run 17단계 순서 + 안전 기준) 본문 변경 없이 1차 실증 메모만 보강 — Status 모두 🟡 잠정 유지.
- 보강·신규 리스크: 신규 R 0건. R-AUTO-015(Strategy Research heavy backtest / report job 실수 full 실행 위험) detection 에 BACKTEST_REPORT command 경로 불일치(rev1 / rev2 사례) 보강 — rev3 module 호출 방식 확정으로 Status `Mitigated` 유지. R-COST-003(Research S3 report 누적 비용 / lifecycle 미설정) 본문 변경 없음 — S3 lifecycle 정책 + KMS encryption 결정은 후속 분리.
- 남은 후속 (본 일자 두 번째 세션 이월):
  1. View AWS 실행 매핑표 작성 — 05 spec 후속 phase 책임. View Daily Batch 의 `BACKTEST_RESEARCH` / `BACKTEST_REPORT` step → AWS Batch SubmitJob 매핑 / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL` step → ECS RunTask 매핑.
  2. safe step 우선 연결 — Step Functions state machine 정의(BACKTEST_RESEARCH → BACKTEST_REPORT → DAILY_BUY_SIGNAL → DAILY_POSITION_SIGNAL 순서 강제 + 자동 재시도 금지 OD-SAFE-004 반영) + EventBridge Scheduler 정기 트리거. 04 spec / 05 spec 후속 phase 책임.
  3. 주문 / 체결 / sync 계열(`DAILY_BUY_EXECUTION` ~ `BALANCE_REFRESH`) 의 paper 운영 환경 활성화: 별도 운영자 승인 + 평일 / 안전 테스트 데이터 환경에서 검증 후 진행(R-AUTO-009 / R-AUTO-010 / R-AUTO-011 / OD-SAFE-002 / OD-SAFE-003 정합). 본 일자 두 번째 세션까지는 safe subset 한정으로 미진행 / dry-run skip 유지.
  4. Execution 계열 dry-run 가능 여부 별도 판단 — `READY -> REQUESTED -> SUBMITTED` end-to-end 검증 진입 전 안전 가드 점검(R-AUTO-009 / R-AUTO-010 정합). 04 spec 후속 phase.
  5. Preprocessor 재실행(task 77) — 첫 번째 세션의 raw 최신성 회복 입력 기반으로 재실행 후 feature table max date / `updated_at` / 신규 feature date 생성 여부 확인. 08 spec 후속 phase.
  6. S3 lifecycle 정책 + KMS encryption 결정 — R-COST-003 정합 / 06 spec 후속 phase.
  7. `portfolio-paper-strategy-report:1` / `portfolio-paper-strategy-report:2` 정리(Inactive 처리 또는 deregister) — 운영자 후속 결정으로 분리.
  8. heavy 분류(`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) 수동 보조 도구 운영 절차 명문화 — R-AUTO-015 mitigation / 09 spec 후속 phase.
  9. aws-live cutover — 10 spec 책임.
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 04 / 05 / 09 / 10 spec 후속 phase 진입 시 본 일자 safe subset 1차 실증 결과 + BACKTEST_REPORT rev3 운영 경로 + DAILY_POSITION_SIGNAL positions 0 정상 skip 케이스를 입력으로 사용한다.

### 2026-06-17 후속 메모 (03 spec — MarketConnector 조회성 dry-run 재검증)

- 운영자가 2026-06-17 직접 수행한 (a) `CONNECTOR_BALANCE` SSM RunCommand 재검증(1차 실패 = JSON SecretString 전체를 환경변수 값으로 export 한 mapping 오류 / v5 패턴 = JSON 내부 key value 추출 + `APP_*` 호환 key + `KIS_*` alias 동시 export 로 보정 후 성공 / `connector_balance_snapshot` 최신 row `as_of_date 2026-06-17` / `as_of_ts 2026-06-17 00:46:17 UTC` / `source_api inquire-balance` / `source_version connector-balance-1.0.0` / 보유종목 0건 정상), (b) `CONNECTOR_ORDER_CHECK` SSM RunCommand 재검증(MarketConnector 조회계열 선행 검증 — Daily 17단계 후반 step 13 을 단건 선행 실행 / v5 패턴 재사용 / KIS `inquire-daily-ccld` `response_status=200` / `response_code=0` / `is_success=true` / `called_at 2026-06-17 00:51:03 UTC` / row count `connector_order_request 33` / `connector_order_event 18` / `connector_fill 13` / 신규 0건은 본 일자 신규 주문·체결 미발생 정상 판단) 결과를 반영. 자세한 결과는 [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-17 §1 ~ §3 참조.
- 1차 완료 범위 (MarketConnector 조회계열 선행 검증):
  1. `CONNECTOR_BALANCE` 조회성 경로: 완료(SSM RunCommand / Secrets Manager + SSM Parameter Store + Instance Role / v5 env injection / `connector_balance_snapshot` 최신 row 검증 / 보유종목 0건 정상)
  2. `CONNECTOR_ORDER_CHECK` 조회성 경로: 완료(SSM RunCommand / v5 env injection 재사용 / `inquire-daily-ccld` 정상 응답 / row count 인벤토리 / 신규 0건 정상 판단)
  3. JSON SecretString 내부 key 추출 정책 1차 실증 — 03 spec design.md §8.2.1 / §8.2.2 / §8.2.3 / runbook.md §2 보강 / runbook.md §4.1 / §4.2 검증 SQL 후보 추가 / validation-checklist 2026-06-17 [O] 행 추가.
- 결정 락(2026-06-17): 신규 OD 0건. OD-SEC-006(EC2 / ECS IAM Role 기반 secret / parameter read 최소 권한) 본문 변경 없이 1차 실증 메모만 보강(MarketConnector EC2 Instance Role 기반 secret read 성공 / secret 평문 0건 / JSON SecretString 내부 key parsing 필요성 1차 실증). OD-MS-001(MarketConnector 컴퓨트 = EC2+EIP) / OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) 본문 변경 없이 1차 실증 메모만 보강(MarketConnector EC2 조회성 경로 재검증 통과). OD-SAFE-001 ~ OD-SAFE-004 / OD-MS-021 정합 — 신규 주문 / `--execute` / fill·position sync 자동 재시도 / aws-live 작업 0건. CONNECTOR_ORDER_CHECK 는 Daily 17단계 후반 step 이지만 본 실행은 선행 단건 검증으로 표현 통일.
- 보강·신규 리스크: 신규 R 0건. R-DOCS-001(secret 평문 기록 위험) detection / mitigation 에 [2026-06-17 보강] 메모 추가 — JSON SecretString 내부 value 추출 후 환경변수 export / value 평문 출력 0건 / runbook §4.1 / §4.2 검증 SQL 기반 점검 / `connector_api_call_log` BALANCE / ORDER 의 `response_status` / `response_code` / `is_success` / `connector_balance_snapshot` 최신 row / `CONNECTOR_ORDER_CHECK` 신규 row 0건도 정상 판단.
- 남은 후속 (본 일자 이월):
  1. systemd / startup script 정상 운영 모드 전환 시 v5 env mapping 패턴(JSON SecretString 내부 key 추출 + `APP_*` 호환 key + `KIS_*` alias 동시 export) 반영 — 03 spec task 26 후속 phase.
  2. `/tmp/inject-env.sh` 운영 스크립트 승격 여부 판단(권한 700 / 메모리 export 만 / secret 평문 미저장 정책 유지) — 03 spec task 27 후속 phase.
  3. `APP_*` 호환 key vs `KIS_*` alias 단일화 / 양쪽 유지 결정(현재 동시 export 로 안전 / 후속 코드 정리 시점에 단일화 후보) — 03 spec task 28 후속 phase.
  4. View / Step Functions 연동 전 safe command wrapper 정리(SSM RunCommand 가 신규 주문 entrypoint 를 trigger 하지 못하도록 allowlist 운영) — 04 spec / 05 spec 후속 phase.
  5. 주문 / 체결 / sync 계열(`DAILY_BUY_EXECUTION` ~ `BALANCE_REFRESH`) 의 paper 운영 환경 활성화: 별도 운영자 승인 + 평일 / 안전 테스트 데이터 환경에서 검증 후 진행(R-AUTO-009 / R-AUTO-010 / R-AUTO-011 / OD-SAFE-002 / OD-SAFE-003 정합). 2026-06-16 두 번째 세션 후속 메모와 동일 정책 유지.
  6. aws-live cutover — 10 spec 책임.
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 03 spec 후속 phase / 04 / 05 spec 진입 시 본 일자 v5 env injection + JSON SecretString 내부 key 추출 + `KIS_*` alias 정책 + runbook §4.1 / §4.2 검증 SQL 후보를 입력으로 사용한다.
### 2026-06-17 후속 메모 (Daily AWS 17-step E2E 완료 — 03 / 04 / 06 / 08 / 09 / 02 spec)

- 같은 일자 첫 번째 세션(MarketConnector 조회성 dry-run 재검증) 후속으로 운영자가 직접 수행한 Daily AWS 17-step E2E 흐름이 본 일자에 끝까지 연결됐다. 17 step 결과 — 1번 `CONNECTOR_BALANCE` 완료 / 2번 `INTEREST_CRAWLER` 완료(non-GUI ECS Fargate `portfolio-paper-interest-crawler:7` + KRX Windows EC2 worker Scheduled Task / `interest_program_raw` / `interest_shortsell_raw` 2026-06-16 / crawler worker stop 요청 완료) / 3번 `PREPROCESSOR` 완료(`pre_total_*` 2026-06-16) / 4번 `BACKTEST_RESEARCH` 완료(Sharpe Ratio `2.68`) / 5번 `BACKTEST_REPORT` 완료(S3 4개) / 6번 `DAILY_BUY_SIGNAL` 완료(BUY READY 4건 / 후보 `282330` / `004990` / `003490` / `088350`) / 7번 `DAILY_POSITION_SIGNAL` 정상 skip / 8번 `DAILY_BUY_EXECUTION` 완료(`execution_plan_id 92`) / 9번 `DAILY_SELL_EXECUTION` 정상 skip / 10번 `DAILY_AUTO_SELL` 정상 skip / 11번 `DAILY_AUTO_BUY` 완료(`READY -> REQUESTED` 4건 / `total_qty 378` / `total_target_amount 6908189.40`) / 12번 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` 완료(KIS paper BUY 4건 제출 성공 / `execution_order` id `26 ~ 29` SUBMITTED / `connector_order_request` id `34 ~ 37` 생성) / 13번 `CONNECTOR_ORDER_CHECK` 완료(`output1 empty` + `output2 aggregate summary` 식별 + summary fallback guard 패치 후 broker_order_no 별 단건 조회로 동기화 / `connector_order_request 34 ~ 37` FILLED / `connector_fill 26 ~ 29` 생성) / 14번 `SYNC_SELL_FILL` 정상 skip / 15번 `SYNC_BUY_FILL` 완료(`execution_order` FILLED 전환) / 16번 `SYNC_BUY_POSITION` 완료(`strategy_position_state` 4건 OPEN / `position_state_id 6 ~ 9`) / 17번 `BALANCE_REFRESH` 완료(legacy.holdings 권한 / search_path 보정 후 재실행 / `connector_position_snapshot` 4종목 최신). 자세한 결과는 [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-17 (Daily AWS 17-step E2E 완료) §1 ~ §5 / [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-17 §1 ~ §6 / [`../09-strategy-research-batch/operation-notes.md`](../09-strategy-research-batch/operation-notes.md) 2026-06-17 §1 ~ §3 / [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-17 §1 ~ §3 / [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-17 §1 ~ §3 / [`../06-secrets-and-iam/operation-notes.md`](../06-secrets-and-iam/operation-notes.md) 2026-06-17 §1 ~ §3 참조.
- 1차 완료 범위 (Daily AWS 17-step E2E):
  1. 조회성 MarketConnector 선행 검증 포함 17 step 전체 실행 흐름 완료
  2. 수집 / 전처리 / 리서치 / 리포트 / Decision / Execution / MarketConnector 주문 / 체결 조회 / fill sync / position sync / balance refresh 까지 `aws-paper` 기준 backend AWS E2E 연결 1차 통과
  3. 실제 broker 호출은 KIS paper BUY 4건 한정 / SELL · 취소 · 정정 호출 0건 / aws-live 작업 0건 / OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004 정합
  4. `READY -> REQUESTED -> SUBMITTED -> FILLED + position OPEN` end-to-end 상태 전이 1차 통과(R-AUTO-009 / R-AUTO-010 / R-AUTO-011 mitigation 1차 실증 / Status `Mitigated`)
  5. 03 spec 운영자 직접 패치 / 정식 배포 — `connector_strategy_order_execute.py` patch(MarketConnector EC2 정식 배포 + venv python 사용 + `source_run_id` fallback) / `connector_order_check.py` summary fallback guard patch(active 후보 정확히 1건일 때만 fallback 허용 / 다건이면 event / fill / status 변경 금지 / `broker_order_no` 별 단건 조회 패턴)
  6. 02 spec / 06 spec — `execution_app` interest schema USAGE / table SELECT / sequence / default privileges 보정(Step 8) + `marketconnector_app` legacy schema USAGE / `legacy.holdings` DML / sequence / database search_path 보정(Step 17) 1차 실증
  7. 08 spec — Interest Crawler hybrid 구조(non-GUI ECS Fargate + KRX Windows EC2 worker) + Preprocessor ECS / Fargate 본 일자 재가동 1차 실증 / crawler worker stop 요청 완료 / KRX GUI = Windows interactive desktop session 기반 유지
  8. 09 spec — BACKTEST_RESEARCH + BACKTEST_REPORT 본 일자 정상 실행 / heavy 분류 SubmitJob 0건 유지(R-AUTO-015 정합)
- 결정 락(2026-06-17 두 번째 세션): 신규 OD 0건. OD-MS-016 / OD-MS-021 / OD-DB-008 / OD-MS-008 / OD-MS-019 본문 변경 없이 1차 실증 메모만 보강 — 자세한 메모는 [`./operator-decisions.md`](./operator-decisions.md) Change Log 2026-06-17 두 번째 항목 참조.
- 보강·신규 리스크: 신규 R-AUTO-018(KIS `inquire-daily-ccld` `output1 empty` + `output2 aggregate summary` 응답 형태에서 active 주문 후보 다건일 때 summary fallback 으로 잘못된 fill / status 매핑 위험, Status `Mitigated`). 신규 R-DATA-011(`marketconnector_app` 의 `legacy` schema USAGE / `legacy.holdings` DML / sequence / database search_path 누락으로 BALANCE_REFRESH 실패 위험, Status `Mitigated`). R-DOCS-001 detection / mitigation 에 [2026-06-17 보강(17-step E2E)] 메모 추가(KIS paper BUY 4건 broker 호출 + 17-step 전체 흐름에서도 secret value 평문 0건 재실증). R-DATA-005 detection / mitigation 에 [2026-06-17 보강] 메모 추가(`execution_app` interest 권한 누락 + `marketconnector_app` legacy.holdings 권한 누락 사례). R-AUTO-009 / R-AUTO-010 / R-AUTO-011 detection / mitigation Status 보강(KIS paper BUY 4건 한정 1차 end-to-end 통과 / Status `Mitigated`). R-AUTO-015 Status `Mitigated` 유지.
- 남은 후속 (본 일자 이월):
  1. `connector_order_check.py` summary fallback guard 테스트 케이스 추가(active 후보 0 / 1 / 2 / 다건 시 동작 / output1 / output2 형태별 입력값 / event · fill · status 변경 0건 검증 / KIS API 응답 fixture) — 03 spec task 29 후속 phase.
  2. `connector_strategy_order_execute.py` 의 `result_payload` 내 `source_daily_signal_id null` 보정 검토(OPTIONAL_COLUMNS 또는 fallback 패턴) — 03 spec task 30 후속 phase.
  3. `legacy.holdings` 권한 / search_path 보정 정식 문서화(02 spec db-roles-and-grants §4 GRANT / §5 검증 SQL / `marketconnector_app` 의 `legacy` USAGE / `legacy.holdings` DML / sequence / database search_path 정식 매트릭스) — 02 / 06 spec 후속 phase.
  4. `connector_balance_snapshot` 최신성 검증 SQL 정리(`as_of_ts` / `max(created_at)` 의미 구분 / `connector_balance_snapshot` / `connector_position_snapshot` / `connector_api_call_log` 분리 점검) — 03 spec task 31 후속 phase.
  5. Windows cp949 콘솔에서 CloudWatch / SSM stdout 이모지 출력 실패 회피 패턴 문서화(SSM 응답 본문 전체 인용 0건 정책 정합 / `chcp 65001` 또는 ASCII fallback) — 03 spec / 운영자 로컬 PC 도구 후속 phase.
  6. AWS paper 자동화 orchestrator 후보 정리(View 또는 Step Functions 기반) — 04 / 05 spec 후속 phase.
  7. View AWS 실행 매핑표 작성 / safe step 우선 연결 / Step Functions state machine 정의 + EventBridge Scheduler 정기 트리거 — 04 spec 후속 phase.
  8. 주문 / 체결 / sync 계열의 paper 운영 환경 정기 트리거 활성화: 별도 운영자 승인 + 평일 / 안전 테스트 데이터 환경에서 검증 후 진행(OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 / R-AUTO-010 / R-AUTO-011 정합) — 04 / 10 spec 후속 phase.
  9. CI/CD OIDC / GitHub Actions 자동 build / push / 배포 체크리스트(MarketConnector EC2 단일 파일 수동 배포 재발 방지) — 07 spec 후속 phase.
  10. aws-live cutover — 10 spec 책임.
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 본 일자 17-step E2E paper 1차 통과 결과는 04 / 05 / 06 / 09 / 02 spec 후속 phase 진입 시 입력으로 사용한다. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건. password rotate 는 본 일자 작업 범위 밖.


### 2026-06-17 후속 메모 (Daily AWS PowerShell wrapper 구현 — 03 spec + 운영자 로컬 도구)

- 같은 일자 두 번째 세션(Daily AWS 17-step E2E 완료) 후속으로 운영자가 직접 수행한 Daily AWS 17-step 운영자용 Windows PowerShell wrapper 구현 결과를 반영. 본 작업의 산출물은 `.kiro/scripts/` 신규 폴더 / 운영자 로컬 PC 도구 / Kiro spec 산출물 외부 / 8개 MS 소스와 별개. 본 wrapper 작업 중 실제 broker / KIS / 신규 BUY · SELL · 취소 · 정정 / `--execute` 주문 제출 0건 / aws-live 작업 0건 / wrapper 기반 전체 1~17 실제 재실행 0건 / Step 12 `-AllowPaperOrderExecute` 사용 0건 / bundled wrapper 미생성. 자세한 결과는 [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-17 (Daily AWS PowerShell wrapper 구현) §1 ~ §6 / [`./operator-decisions.md`](./operator-decisions.md) Change Log 2026-06-17 세 번째 항목(OD-MS-023 추가) 참조.
- 1차 완료 범위 (Daily AWS PowerShell wrapper 기준선 수립):
  1. main wrapper `run-daily-aws-paper.ps1` + config `daily-aws-paper.config.ps1` + functions `daily-aws-paper.functions.ps1` + step 파일 17개 분리 파일 구조 1차 수립
  2. SSM RunCommand 공통 함수(`AWS-RunShellScript` Linux + `AWS-RunPowerShellScript` Windows) / ECS RunTask 공통 함수(UTF-8 no BOM JSON `--overrides file://...`) / AWS Batch SubmitJob 공통 함수 작성 + CloudWatch log 수집 / SSM stdout · stderr 저장 / summary 기록 / 성공·실패·blocker 판정 / PowerShell UTF-8 보정
  3. 전체 1~17 DryRun FOUND 17건 통과 / 실제 ECS RunTask · Batch SubmitJob · SSM command 제출 0건
  4. Step 1 단독 SSM 1차 검증 통과(MarketConnector EC2 / `connector_balance.py` / Success / ResponseCode 0 / `connector_balance_snapshot` 저장 / 보유종목 0건)
  5. Step 12 PAPER_ORDER_GATE 안전 차단 검증 통과(중앙 wrapper gate + 내부 이중 gate / `-AllowPaperOrderExecute` 없으면 SSM command 제출 0건 / 실제 KIS 주문 제출 0건)
  6. PowerShell parser validation 20개 파일 모두 OK / parser error 0건
  7. 위험 키워드 safety grep 통과(Step 10 / Step 11 의 `--execute` 는 strategy execution 내부 상태 갱신 / Step 12 의 `--execute` 만 KIS paper 주문 제출 가능 step / 의도하지 않은 broker · KIS 주문 제출 command 추가 0건)
- 결정 락(2026-06-17 세 번째 세션): 신규 OD-MS-023(Daily AWS wrapper 운영 정책 = 운영자 로컬 Windows PowerShell wrapper 분리 파일 구조 / 환경 입력 = `aws-paper` 만 허용 / wrapper 자체는 EC2 · ECS · Batch 내부 실행 대상 아님 / 17 step 매핑 · 진행 순서 · 안전 기준은 OD-MS-021 그대로 유지 / Step 12 는 wrapper 중앙 PAPER_ORDER_GATE + 내부 이중 gate 로 기본 차단 / `-AllowPaperOrderExecute` 명시 시에만 허용 / Step 10 / Step 11 의 `--execute` 는 strategy execution 내부 상태 생성·갱신 의미 / broker 직접 제출 아님 / bundled wrapper 는 필요 시에만 생성 / 완전 자동화 이전 단계의 단계별 확인 가능한 CLI 기준선, 🟡 잠정). OD-MS-009 / OD-MS-021 / OD-MS-016 / OD-SAFE-002 / OD-SAFE-003 / OD-SAFE-004 본문 변경 없이 1차 실증 메모만 보강.
- 보강·신규 리스크: 신규 R-AUTO-019(wrapper 기반 Step 12 의도하지 않은 `-AllowPaperOrderExecute` 사용 위험, Status `Mitigated`). R-AUTO-016(Administrator interactive session 부재 시 KRX GUI 수집 실패) detection / mitigation 에 [2026-06-17 wrapper 보강] 메모 추가 — Step 2 의 KRX GUI Scheduled Task trigger 는 Windows KRX crawler worker 가 `running` 이 아니면 wrapper 안에서 자동 skip(EC2 instance state / SSM Online 사전 점검) / Status 기존 값 그대로 유지. R-AUTO-002(live 자동매매 조기 활성화) mitigation 에 [2026-06-17 wrapper 보강] 메모 추가 — wrapper 의 환경 입력은 `aws-paper` 만 허용 / aws-live 분기 코드 레벨 미존재. R-DOCS-001(secret 평문 기록 위험) detection / mitigation 에 [2026-06-17 wrapper 보강] 메모 추가 — wrapper summary / overrides JSON / SSM stdout · stderr 파일에 secret 평문 출력 0건 / `/tmp/inject-env.sh` v5 env injection 만 사용.
- 남은 후속 (본 일자 이월):
  1. bundled wrapper(`run-daily-aws-paper-bundled.ps1`) 생성 여부 결정 — 17개 step 별 파일 검증이 충분히 안정화된 시점에 단일 파일 실행이 필요하면 생성 / 기본 개발 · 검증 · 운영 기준은 분리 파일 구조 유지(OD-MS-023 정합).
  2. wrapper 기반 Step 1~7 safe subset 실제 실행 검증 — `-StartStep 1 -EndStep 7` / paper 환경 한정 / broker · KIS 주문 제출 없음 검증.
  3. wrapper 기반 Step 8~11 execution-side 상태 생성·갱신 구간 별도 검증 — Step 8 / Step 9 / Step 10 / Step 11 의 `--execute` 는 strategy execution 내부 상태 갱신만 / broker · KIS 직접 제출 아님(OD-MS-016 책임 분리 정합) / `strategy_execution_order` row 생성·전이 검증.
  4. Step 12 실제 KIS paper 주문 제출은 운영자 확인 후 `-AllowPaperOrderExecute` 명시로만 진행 — 별도 운영자 승인 + 평일 / 안전 테스트 데이터 환경에서 진행(R-AUTO-019 mitigation 정합).
  5. wrapper 기반 Step 13~17 주문 / 체결 / sync / balance 결과 확인 후 연계 실행 — Step 13 = 주문 상태 조회 · DB 갱신 / Step 14~16 = fill · position sync DB 갱신 / Step 17 = balance · position snapshot refresh / 신규 broker · KIS 주문 제출 없음.
  6. Windows KRX crawler worker stopped 상태일 때 Step 2 실제 실행 검증(skip 동작) — wrapper 안 EC2 instance state pre-check 흐름의 실제 시나리오 검증(R-AUTO-016 [2026-06-17 wrapper 보강] mitigation 정합).
  7. View backend orchestration 또는 Step Functions 이전 검토 — wrapper 의 17 step 매핑 / 진행 순서 / 안전 기준을 그대로 Step Functions state machine 으로 옮길 수 있는지 검토(05 / 04 spec 후속 phase 책임).
  8. EventBridge Scheduler 정기 트리거 도입 — wrapper 검증이 안정화된 후 정기 실행으로 전환(04 spec 후속 phase 책임).
  9. aws-live cutover — 10 spec 책임. wrapper 의 환경 입력에 `aws-live` 분기 추가는 10 spec 진입 시점에 별도 결정.
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 본 일자 wrapper 기준선 수립은 04 / 05 spec 후속 phase 의 Step Functions / View orchestration 진입 시점에 입력으로 사용한다. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 wrapper 작업으로 인한 변경 0건. `.kiro/scripts/` 신규 폴더 / 파일은 운영자 로컬 PC 도구 영역 / Kiro spec 산출물 외부.


### 2026-06-18 후속 메모 (Daily AWS Paper Wrapper 17단계 실운영 검증 완료 — 03 / 04 spec + 운영자 로컬 도구)

- 2026-06-17 wrapper 구조 1차 수립(OD-MS-023) 후속으로 2026-06-18 운영자가 직접 수행한 Daily AWS Paper Wrapper(`.kiro/scripts/run-daily-aws-paper.ps1`) 의 Step 1 ~ Step 17 실 실행 결과를 반영. View 구현 전 CLI 기준 Daily 전체 실행 기준선 1차 완성. 환경 `aws-paper` / RunDate `2026-06-18` / region `ap-northeast-2` / Step 12 `-AllowPaperOrderExecute` 첫 사용. 실제 KIS paper BUY 4건 제출 + 체결 + execution fill sync + position sync + balance refresh E2E 완료. 자세한 결과는 [`./operator-decisions.md`](./operator-decisions.md) Change Log 2026-06-18(OD-MS-024 신규) / [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-18 §1 ~ §4 / [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-18 §1 ~ §6 참조.
- 1차 완료 범위 (Daily AWS Paper Wrapper 17단계 실운영 검증):
  1. wrapper 1~17 실 실행 통과(DryRun 아님 / 실제 ECS RunTask · Batch SubmitJob · SSM command 제출 흐름 / 운영자 로컬 PC 에서 단계별 확인 실행)
  2. Step 12 `-AllowPaperOrderExecute` 첫 사용 / KIS paper BUY 4건 제출 성공(broker_order_no `0000025576` / `0000025740` / `0000025744` / `0000025747`)
  3. Step 13 broad 조회 `output1 empty` + `output2 summary-only` 응답 + active candidate 4건 → summary fallback 자동 skip(R-AUTO-018 mitigation 1차 실증) → 단건 `--code` / `--order-no` / `--no-broad` 조회로 4건 체결 반영
  4. Step 16 추가매수 케이스 `strategy_position_state` unique constraint 충돌 발견(R-DATA-012 신규) → `execution_sync_buy_position.py` merge 패치(`merge_open_position_state()` + `additional_buys` + idempotency) → Docker rebuild + ECR push + ECS 재실행 통과 → OD-MS-024 신규 결정 락
  5. 운영 예외 3종(KIS paper API read timeout / KIS summary-only 응답 / 추가매수 unique constraint) 모두 운영 중 식별 + 통제된 복구 + end-to-end 통과
  6. 최종 6종목 OPEN(003490 58주 / 004990 69주 / 023530 8주 / 042660 11주 / 088350 244주 / 282330 17주) 정합 / Step 17 SSM Success / `connector_position_snapshot` row_count 36 / `legacy.holdings` row_count 41
- 결정 락(2026-06-18): 신규 OD-MS-024(추가매수 허용 정책 + 기존 OPEN row merge / `execution_sync_buy_position.py` patch 적용 / OD-MS-016 책임 분리 / OD-MS-017 단일 Task Definition + command override 정합 유지). OD-MS-016 / OD-MS-021 / OD-MS-023 본문 변경 없이 1차 실증 메모만 보강 — 자세한 메모는 [`./operator-decisions.md`](./operator-decisions.md) Change Log 2026-06-18 항목 참조.
- 보강·신규 리스크: 신규 R-BROKER-004(KIS paper API read timeout 시 단순 재실행 broker 중복 주문 위험, mitigation = `connector_order_request` / `connector_api_call_log` / `broker_order_no` 사전 점검 후 통제된 REQUESTED 복구, Status `Mitigated`). 신규 R-DATA-012(추가매수 시 `strategy_position_state` unique constraint 충돌 위험, mitigation = `execution_sync_buy_position.py` merge 패치 + idempotency, Status `Mitigated`). R-AUTO-018(KIS summary-only fallback 오매핑) detection / mitigation 에 [2026-06-18 보강] — wrapper Step 13 자동 skip + 단건 조회로만 fallback 1차 실증. R-AUTO-009 / R-AUTO-010 / R-AUTO-011 / R-AUTO-019 detection / mitigation Status 보강 — Step 12 첫 사용 / 1차 timeout → 통제된 복구 → end-to-end 통과 / Status 기존 `Mitigated` 유지. R-DATA-005 detection / mitigation 에 [2026-06-18 보강] — view_app 의 `legacy.holdings` SELECT 권한 부재로 `portfolio_admin` 우회 / 정식 GRANT 후속 검토(02 spec db-roles-and-grants 후속).
- 남은 후속 (본 일자 이월):
  1. Step 13 단건 체결조회(`--code` / `--order-no` / `--no-broad`) wrapper 안 자동화 보완 — 다건 active candidate 상황에서 broad 조회 후 자동으로 단건 조회 loop 호출 / 운영자 수동 실행 단계 제거(R-AUTO-018 mitigation 강화).
  2. KIS paper API timeout 재시도 정책 명문화 — 03 spec runbook §4.2 또는 후속 phase 산출물에 정식 절차 기재(connector_order_request · api_call_log · broker_order_no 사전 점검 → broker_order_no 부재 시에만 통제된 REQUESTED 복구 → broker_order_no 존재 시 단건 조회로 동기화). R-BROKER-004 mitigation 확정.
  3. Step 16 추가매수 merge 패치 정식 commit + 07 spec CI/CD 연동 — 운영자가 직접 patch 한 `port_strategy_execution/execution_sync_buy_position.py` 변경분의 정식 commit / image tag 관리 / ECR push 자동화 / R-DATA-012 mitigation 정합 회귀 점검 자동화.
  4. view_app `legacy.holdings` SELECT 권한 검토 — 02 spec db-roles-and-grants §4 GRANT / §5 검증 SQL 갱신 후보(R-DATA-005 [2026-06-18 보강] 정합 / view_app 의 legacy schema 운영 조회 필요 여부 운영자 결정 후 진행).
  5. wrapper run summary 출력 보강 — `summary/run-summary.txt` 에 KIS paper API timeout 발생 / 통제된 복구 결과 / Step 13 broad → 단건 조회 전환 / Step 16 patch 재실행 흐름 등 운영 예외 추적 라벨 자동 기록.
  6. View Daily Batch 화면 연동 전 최종 운영 summary 개선 — 05 spec 진입 시점에 wrapper summary 형식을 그대로 사용할지 / View 측 별도 summary 형식 정할지 결정.
  7. bundled wrapper 생성 여부 결정 — wrapper 1~17 실 실행 통과 후에도 본 일자 미생성 유지 / OD-MS-023 정합 분리 파일 구조 우선.
  8. EventBridge Scheduler 정기 트리거 / Step Functions hybrid orchestration 이전 — 04 spec 후속 phase / 05 spec 후속 phase 책임.
  9. aws-live cutover — 10 spec 책임. wrapper 의 환경 입력에 `aws-live` 분기 추가는 10 spec 진입 시점에 별도 결정.
  10. SELL closing 흐름 / 부분 청산 / 전량 청산 시 `additional_buys` 처리 방식 검증 — OD-MS-024 정합 / SELL 측 `mark_position_sell_ordered()` 호출 시점에 `additional_buys` 데이터를 어떻게 활용할지 / SELL fill 후 position_state status 전이 시점에 `additional_buys` 보존 여부 / 별도 spec 검증 후속.
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 본 일자 wrapper 17단계 실운영 검증 결과는 04 / 05 spec 후속 phase 의 Step Functions / View orchestration 진입 시점에 입력으로 사용한다. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 wrapper 작업으로 인한 변경 0건(spec 영역) — `port_strategy_execution/execution_sync_buy_position.py` 운영자 직접 patch 결과는 04 spec operation-notes 에 사실로만 기록(본문 전체 인용 0건 / R-DOCS-001 정합).


### 2026-06-18 후속 메모 (Step 13 `connector_order_check.py` 단건 순차 조회 기본화 — 03 spec)

- 2026-06-18 같은 일자 첫 번째 세션(Daily AWS Paper Wrapper 17단계 실운영 검증 완료) 후속으로 운영자가 직접 수행한 MarketConnector `connector_order_check.py` 기본 실행 모드 변경 결과를 반영. 운영 실패가 아니라 운영 안전성 강화를 위한 설계 변경 — 첫 번째 세션의 broad 조회 `output1 empty` + `output2 summary-only` + active candidate 4건 응답 패턴(R-AUTO-018 mitigation 1차 실증) 결과를 더 안전한 기본 동작으로 코드 레벨에 반영. 자세한 결과는 [`./operator-decisions.md`](./operator-decisions.md) Change Log 2026-06-18 두 번째 항목(OD-MS-025 신규) / [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-18 (Step 13 `connector_order_check.py` 단건 순차 조회 기본화) 섹션 참조.
- 1차 완료 범위 (Step 13 `connector_order_check.py` 단건 순차 조회 기본화):
  1. 기본 실행 모드 변경 — 변경 전 = broad 체결조회 중심 → 변경 후 = active 주문 목록 조회 + 주문번호 / 종목코드 기준 단건 direct-only 순차 조회
  2. 신규 옵션 — `--broad`(legacy broad 조회 모드 / 기본값 미사용) / `--active-limit`(기본 active 주문 단건 순차 조회 최대 처리 건수) / 명시 주문 조회는 `--code {ticker_code} --order-no {broker_order_no} --no-broad` 조합으로 broad fallback 없이 해당 주문만 조회
  3. 안전 기준 — 다건 active 주문 상태에서 `output2 summary-only` 응답을 DB 반영 근거로 사용하지 않고 `connector_order_request` 후보가 주문번호 / 종목코드 기준으로 1건 확정된 경우에만 summary fallback 허용(R-AUTO-018 [2026-06-18 추가 보강] mitigation 정합)
  4. 책임 분리 — wrapper ps1(`.kiro/scripts/steps/step-13-connector-order-check.ps1`) = Step 13 orchestration(SSM RunCommand 호출 / 환경 검증 / log 저장) 만 담당 / 체결조회 방식 제어(active 조회 / 단건 direct-only / broad 모드 분리) = `connector_order_check.py` 내부 책임
  5. 검증 milestone — 로컬 `python -m py_compile connector_order_check.py` 통과 / `python connector_order_check.py --help` 의 `--broad` / `--active-limit` 옵션 표시 확인 / 로컬 commit `75cb804`(`fix(connector): run order checks sequentially per active order` / 현재 repository 에 remote push destination 미설정 → git push 미수행 / 후속 분리)
  6. EC2 반영 — S3 경유(bucket `portfolio-paper-migration-yukiever` / key `deploy/marketconnector/connector_order_check.py` / size `39159 bytes` / EC2 backup `connector_order_check.py.bak-20260618-step13-per-order` / SSM commandId `b344d404-07a4-4bb6-9d63-34151e648bab`) → wrapper 단독 실행 검증(`-StartStep 13 -EndStep 13`) 통과 — 단건 direct-only 조회 1건(`004990` / `0000025576`) 기준 `connector_order_event` / `connector_fill` 정상 생성
- 결정 락(2026-06-18 두 번째 세션): 신규 OD-MS-025(MarketConnector `connector_order_check.py` 운영 모드 = active 주문 단건 순차 조회 기본 + broad 옵션 격리 + `connector_order_check.py` 내부 분기). OD-MS-016 / OD-MS-021 / OD-MS-023 본문 변경 없이 1차 실증 메모만 보강 — wrapper ps1 의 책임은 orchestration 만으로 유지 / 체결조회 방식 제어는 `connector_order_check.py` 내부 책임으로 명확화. 자세한 메모는 [`./operator-decisions.md`](./operator-decisions.md) Change Log 2026-06-18 두 번째 항목 참조.
- 보강·신규 리스크: 신규 R 0건. R-AUTO-018(KIS `inquire-daily-ccld` summary-only fallback 오매핑) detection / mitigation 에 [2026-06-18 추가 보강] — 본 일자 첫 번째 세션의 wrapper 안 자동 skip 1차 실증에 더해 `connector_order_check.py` 자체의 기본 실행 모드가 active 주문 단건 순차 조회로 변경됨으로써 broad 체결조회 호출 빈도 자체가 줄고 다건 active 상태에서 summary fallback 노출 표면이 감소 / Status `Mitigated` 유지(verified on 2026-06-18 두 번째 세션).
- 남은 후속 (본 일자 두 번째 세션 이월):
  1. Git remote push destination 설정 + commit `75cb804` 정식 push — 현재 `port-marketconnector` repository 의 remote push destination 미설정 / 운영자 결정 후 origin 등록 + push / 07 spec CI/CD pipelines 연동 시점에 정식 origin 정책과 함께 정리.
  2. Step 13 wrapper 단독 실행 summary 출력 보강 — `summary/run-summary.txt` 에 `active_order_count` / `single_check_success_count` / `single_check_failed_count` / `broad_mode_used` 여부 / 실패 시 종목코드 / 주문번호 / 사유 자동 기록(R-AUTO-018 detection 정합 / 운영자 PowerShell summary 만으로 사후 검증 가능).
  3. View Daily Batch 화면 연동 시 주문별 표시 — 05 spec 진입 시점에 단건 direct-only 조회 결과를 주문별 row 단위로 화면에 표시할지 결정 / wrapper summary 형식과 화면 형식의 정합성 검토.
  4. Windows PowerShell · AWS CLI SSM 출력 cp949 인코딩 회피 패턴 정리 — wrapper Step 13 stdout / SSM 응답 본문이 한글 라벨 / 파일명 포함 시 cp949 vs UTF-8 인코딩 충돌 가능성 / 운영자 노트 / spec 산출물 본문에 cp949 깨짐 0건 정책 유지(R-DOCS-001 정합).
  5. KIS paper API timeout 재시도 정책 명문화(2026-06-18 첫 번째 세션 §2 followups §2 와 결합) — 단건 direct-only 조회 패턴이 `--no-broad` + `--code` + `--order-no` 로 통제되는 구조이므로 timeout 재시도 시점에도 broker_order_no 별 단건 조회로만 동기화 / 03 spec runbook §4.2 정식 절차 기재 후속.
  6. `--active-limit` 기본값 결정 후속 — 본 일자 1차 실증 시점의 default 값 적용 / 운영 환경에서 active 주문 수 누적 시 처리 건수 상한 정책 결정.
- 진행 순서 자체는 변경하지 않는다. 08 → 04 → 05 → 09 → 07 → 10 흐름 유지. 본 일자 두 번째 세션의 단건 순차 조회 기본화 결과는 03 / 04 / 05 spec 후속 phase 의 Step Functions / View orchestration 진입 시점에 입력으로 사용한다. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 패키징 본 일자 두 번째 세션 작업으로 인한 변경 0건(spec 영역) — 운영자 직접 patch 한 `port-marketconnector/connector_order_check.py` 변경분은 03 spec operation-notes 에 사실로만 기록(본문 전체 인용 0건 / R-DOCS-001 정합).
