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
