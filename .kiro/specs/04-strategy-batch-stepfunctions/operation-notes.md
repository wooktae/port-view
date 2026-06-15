# Operation Notes — 04-strategy-batch-stepfunctions

본 문서는 04-strategy-batch-stepfunctions 진행 중 운영자 / Kiro 가 실제 수행한 작업 결과를 일자별로 누적 기록하는 운영 노트다. 1차 적용 환경은 `aws-paper`, region 은 `ap-northeast-2`, 1차 검증 대상은 Strategy Decision MS(`port_strategy_decision`)의 ECS / Fargate 단건 RunTask 검증이다. EventBridge Scheduler / Step Functions / Strategy Execution(`port_strategy_execution`) 연계는 본 일자 작업 범위 밖이며 04 spec 후속 phase 또는 별도 spec(execution / orchestration) 책임이다.

## 기록 형식

- 일자별 `## YYYY-MM-DD <요약>` 헤더로 누적한다(02 / 03 / 06 / 08 spec operation-notes 와 동일).
- 결과는 성공 / 실패 / 보류 / 해당 없음 / 이월만 짧게 적는다. 실패 사례는 1줄 사유 + 1줄 조치 + 결과까지만 요약한다.
- AWS CLI / Console / CloudWatch 로그 / Task event message / docker build 로그 전문은 본 문서에 인용하지 않는다(보안 / 분량 절감).
- IAM Role / Policy / secret 변경은 변경 일자 / 변경자 / 변경 사유 / 변경 전·후 항목 요약 4줄로만 기록한다(JSON 본문 전체 인용 금지).
- 8개 MS 의 소스 / Dockerfile / requirements.txt 본문 전체 인용은 금지한다. 운영자가 직접 신규 작성한 사실만 기록한다.

## 안전 원칙

- 실제 secret value, password, KIS app key, KIS app secret, 계좌번호, RDS endpoint hostname, RDS password, token, IAM access key id, account-id, 실제 secret ARN, 실제 KMS Key ARN, instance-id, image digest, task ARN 은 본 문서에 평문 기록 금지. 모두 `[REDACTED]` 또는 placeholder(`<account-id>` / `<region>` / `<rds-endpoint>` / `<image-tag>` / `<image-digest>` / `<task-arn>`).
- secret 조회 결과(value)는 기록 금지. 성공 / 실패 + 마지막 갱신 시각(필요 시 ISO 8601 `YYYY-MM-DDTHH:MM:SS+09:00`)만.
- 실제 AWS 리소스 생성 / 수정 / 삭제는 운영자가 직접 수행한다. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행한다.
- 8개 MS(`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) 의 README / AGENTS.md / CHANGELOG / docs / worklog 는 본 spec 작업으로 변경하지 않는다. 운영자가 직접 작성한 Dockerfile / requirements.txt 는 운영자 직접 작업이며 본 노트에 사실만 기록한다.
- `secretsmanager:GetSecretValue` 실호출은 운영자만. Kiro 자동 검증은 `secretsmanager:DescribeSecret` metadata 만.
- secret value 가 작업 채팅 / 명령 출력 / 콘솔 캡처 / CloudWatch Logs 본문 / 운영자 노트에 평문 노출되지 않도록 후속 작업에서도 동일 원칙 유지(R-DOCS-001 정합).

## 2026-06-13 Strategy Decision ECS / Fargate 1차 포팅 검증

운영자가 2026-06-13 직접 수행한 `port_strategy_decision` 의 ECS / Fargate 1차 포팅 검증 결과를 누적 기록한다. 본 일자에 Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행했고, 실제 AWS / Docker / ECR / ECS / IAM / Secrets / RDS / GRANT 작업은 운영자가 직접 진행했다.

### 1. AWS 실행 구조 확인

1. As-Is 실행 방식 확인: 완료
   1) 로컬 repository 경로 확인: 완료 (`C:\Workspaces\port_strategy_decision`)
   2) 주요 entrypoint 후보 확인: 완료
       - `daily_buy_signal_run.py`
       - `daily_position_signal_run.py`
   3) `port_strategy_common` import 가능 여부 확인: 완료 (로컬 `PYTHONPATH=C:\Workspaces` 기반)
   4) DB 접속 환경변수 확인: 완료
       - `INTEREST_DB_HOST`
       - `INTEREST_DB_PORT`
       - `INTEREST_DB_NAME`
       - `INTEREST_DB_USER`
       - `INTEREST_DB_PASSWORD`
   5) 기존 Daily Batch 실행 구조 확인: 완료
       - `port-view` Daily Batch 의 step 6 = `DAILY_BUY_SIGNAL`, step 7 = `DAILY_POSITION_SIGNAL`
       - Java `DailyBatchService` 가 두 entrypoint 를 별도 ProcessBuilder 프로세스로 순차 실행
2. 데이터 의존성 확인: 완료
   1) preprocessor schema 읽기: 완료 (input feature)
   2) research schema 쓰기: 완료 (`research.strategy_block_watch_candidate`)
   3) execution schema 읽기: 완료 (`execution.strategy_position_state`)
   4) decision schema 쓰기: 완료 (`decision.strategy_daily_position_decision`)
   5) connector / reference / legacy 참조 여부 확인: 완료 (본 일자 검증 범위에서는 직접 의존 없음)
3. To-Be 실행 방식 확정: 완료
   1) 통합 `daily_decision_run.py` 신규 작성 방안: 보류
       - 기존 Daily Batch 가 buy-signal / position-signal 을 별도 step 으로 실행하는 구조 계승
       - AWS 에서도 두 entrypoint 를 별도 Task Definition 으로 분리하는 방식으로 확정
   2) 1차 실행 후보: `daily_buy_signal_run.py` 로 확정
   3) 2차 실행 후보: `daily_position_signal_run.py` 로 확정
   4) Docker build context: `C:\Workspaces` 기준으로 확정
   5) 이미지 동봉 소스: `port_strategy_common` + `port_strategy_decision` 두 소스 vendoring
   6) `port_strategy_common` 정식 package / version 관리: 후속 (Strategy Common 또는 DevOps 고도화 단계)
   7) Task Definition 분리 정책: 본 일자 확정 (OD-MS-013 참조)
       - buy-signal: `portfolio-paper-strategy-decision-buy-signal`
       - position-signal: `portfolio-paper-strategy-decision-position-signal`
   8) EventBridge Scheduler 연계 / Step Functions orchestration: 후속 분리

### 2. Dockerfile / requirements.txt 신규 생성

1. `port_strategy_decision` 1차 Dockerfile / requirements.txt 운영자 직접 신규 생성: 완료
   1) Docker build context: `C:\Workspaces`
   2) 이미지 안에 `port_strategy_common` 과 `port_strategy_decision` 두 소스 vendoring 방식 반영
   3) 1차 운영용 entrypoint 는 ECS Task Definition `command` 에서 결정 (이미지 자체는 두 entrypoint 모두 실행 가능)
2. `port_strategy_common` 정식 package / version 관리: 보류
   1) 1차 ECS smoke image 에서는 vendoring 방식 사용
   2) 정식 package / version 관리는 후속 Strategy Common 또는 DevOps 고도화 단계로 분리

### 3. 로컬 이미지 빌드 / Smoke

1. 로컬 Docker build: 완료
   1) `portfolio-strategy-decision:paper-20260613`
2. container import smoke 성공: 완료
   1) `port_strategy_common` import: 성공
   2) `port_strategy_decision.daily_buy_signal_run` import: 성공
   3) `port_strategy_decision.daily_position_signal_run` import: 성공

### 4. ECR Repository / Push

1. ECR repository 생성 / 확인: 완료
   1) `portfolio-strategy-decision` repository 신규 생성
   2) 환경 분리 정책: paper / live 환경별 repository 분리하지 않음 (08 spec 정책 정합)
       - paper / live 구분은 image tag / Task Definition / Secrets·SSM path / IAM Task Role / environment variables / RDS 설정 6개 항목에서 처리
2. ECR push: 완료
   1) tag `paper-20260613`
   2) tag `paper-latest`
3. image digest: 운영자가 직접 확인. 실제 digest 값은 본 노트 / spec 산출물에 미기록(`<image-digest>` placeholder)

### 5. CloudWatch Log Group / Secrets Manager / IAM Role

1. CloudWatch Log Group 생성 / 확인: 완료
   1) `/portfolio/paper/strategy-decision`
   2) retention: 14일 (08 spec 정합 / OD-OBS-002 정합)
2. Secrets Manager secret 생성 / 확인: 완료
   1) `/portfolio/paper/rds/decision-app`
   2) JSON multi-key 방식 (`host` / `port` / `dbname` / `username` / `password`) — 08 preprocessor secret 패턴 정합
   3) 실제 secret value / endpoint / password / ARN / account-id 본 노트 미기록 (R-DOCS-001 / R-DATA-006 정합)
3. ECS Task Execution Role 확인: 완료
   1) 이름: `portfolio-paper-ecs-task-execution-role` (08 spec 에서 1차 생성, 본 일자 재사용)
   2) decision-app DB Secret read inline policy 추가: 완료
       - secret ARN 한정 / Resource·Action wildcard 0건 / 03 §13 / OD-SEC-006 정합
       - 변경 일자 / 변경자 / 변경 사유 / 변경 전·후 항목 요약은 본 노트에서만 4줄 요약(JSON 본문 전체 인용 금지)
4. ECS Task Role 생성 / 확인: 완료
   1) 이름: `portfolio-paper-decision-task-role`
   2) trust: `ecs-tasks.amazonaws.com`
   3) decision-app 실행 시점 SDK / runtime 권한은 본 일자에 추가하지 않음 (RDS 접속은 Execution Role 의 secret 주입 + RDS 자격으로 처리)

### 6. ECS Task Definition

1. Task Definition 분리 등록: 완료
   1) buy-signal:
       - family: `portfolio-paper-strategy-decision-buy-signal`
       - command: `python -m port_strategy_decision.daily_buy_signal_run`
   2) position-signal:
       - family: `portfolio-paper-strategy-decision-position-signal`
       - command: `python -m port_strategy_decision.daily_position_signal_run`
2. 공통 항목: 완료
   1) network mode: `awsvpc`
   2) launch type: Fargate
   3) cpu: 512
   4) memory: 1024
   5) ECS cluster: `portfolio-paper-cluster` (08 spec 에서 1차 생성, 본 일자 재사용)
   6) network: 기존 public subnet + assignPublicIp ENABLED + RDS 접근 가능한 ECS SG 구조 재사용 (OD-NET-004 정합)
   7) log group 연결: `/portfolio/paper/strategy-decision`
   8) Secret 환경변수 연결: 5종 (`INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD`)
3. 환경변수 호환 정책: 기존 코드와의 호환을 위해 `INTEREST_DB_*` 환경변수 명을 그대로 사용 (OD-DB-003 정합)

### 7. RunTask 단건 실행 검증

#### 7-1. `daily_buy_signal_run` 검증

1. ECS RunTask 1차 실행: 실패
   1) 사유: `research.strategy_block_watch_candidate` 권한 부족
   2) `decision_app` 이 research schema 의 candidate table 에 INSERT / UPDATE 권한 미보유 — search_path 에 research 가 포함되어 있어도 GRANT 가 없으면 실패
2. 권한 보정: 완료
   1) `research.strategy_block_watch_candidate` 에 `decision_app` 권한 부여
   2) research schema sequence usage / select 권한 보정
3. ECS RunTask 재실행: 완료
   1) ECR image pull: 성공
   2) Secret injection: 성공
   3) RDS 접속: 성공
   4) CloudWatch Logs 출력: 성공
   5) exitCode: 0
4. CloudWatch 결과 요약: 완료
   1) `daily_run_id`: 44
   2) `run_date`: 2026-06-13
   3) `data_date`: 2026-06-08
   4) `market_signal`: BLOCK
   5) `base_exposure`: 0.0
   6) `max_positions`: 0
   7) `candidates`: 0
   8) `signals`: 0
   9) `block_watch`: 1

#### 7-2. `daily_position_signal_run` 검증

1. ECS RunTask 1차 실행: 실패
   1) 사유: `relation "strategy_position_state" does not exist`
   2) 원인: 실제 테이블 위치는 `execution.strategy_position_state` 인데 `decision_app` 의 execution schema / table 권한 부족으로 search_path 안에서도 탐색 실패
2. RDS 테이블 위치 확인: 완료
   1) `decision.strategy_daily_position_decision`
   2) `execution.strategy_position_state`
   3) `legacy.strategy_daily_position_decision_test_backup` (참조 대상 아님 / OD-DB-007 정합)
3. 권한 보정: 완료
   1) `execution.strategy_position_state` 에 `decision_app` 권한 부여
   2) `decision.strategy_daily_position_decision` 에 `decision_app` 권한 부여
   3) 관련 schema sequence usage / select 권한 보정
4. ECS RunTask 재실행: 완료
   1) ECR image pull: 성공
   2) Secret injection: 성공
   3) RDS 접속: 성공
   4) CloudWatch Logs 출력: 성공
   5) exitCode: 0
5. CloudWatch 결과 요약: 완료
   1) `daily_run_id`: 44
   2) `run_date`: 2026-06-13
   3) `data_date`: 2026-06-08
   4) `market_signal`: BLOCK
   5) `evaluator_version`: v1
   6) `positions`: 0
   7) `decision_count`: 0
   8) `sell_count`: 0
   9) `hold_count`: 0
   10) `skip_count`: 0
   11) `decision_ids`: []

### 8. 1차 검증 완료 기준

1. Strategy Decision ECS Task 단건 실행 성공: 완료
   1) `daily_buy_signal_run` 성공
   2) `daily_position_signal_run` 성공
2. `decision_app` 권한으로 필요한 DB 접근 성공: 완료
   1) preprocessor / research / execution / decision 4개 schema 권한 정합 확인
   2) legacy schema USAGE 미부여 정책 유지 (OD-DB-007 정합)
3. 실행 로그 확인 가능: 완료
   1) CloudWatch Logs 출력 확인
4. 후속 자동화 항목 분리: 완료
   1) EventBridge Scheduler 연계는 후속
   2) Step Functions orchestration 연계는 strategy execution / batch orchestration 단계에서 검토
   3) `port_strategy_common` 정식 package / version 관리는 후속 Strategy Common 또는 DevOps 고도화 단계로 분리
   4) DB 권한 매트릭스 정식 정리는 06 / 02 spec db-roles-and-grants 후속 갱신으로 분리
5. 판단: Strategy Decision AWS ECS / Fargate 포팅 핵심 검증 완료

### 9. 본 일자 범위 밖 / 후속 인계

1. EventBridge Scheduler → ECS RunTask 연계: 후속
2. Step Functions Standard / Express 선정 + state machine 정의: 후속 (04 spec 후속 phase)
3. Strategy Execution(`port_strategy_execution`) ECS / Fargate 포팅 검증: 후속 (별도 spec / 04 spec 후속 phase 책임)
4. `port_strategy_common` 정식 package / version 관리(wheel + CodeArtifact 또는 git submodule packaging): 후속 (07 / Strategy Common 단계)
5. DB 권한 매트릭스 정식 정리: 후속 (`decision_app` 의 research / execution / decision schema 권한을 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) §4 GRANT / §5 검증 SQL 에 정식 반영)
6. aws-live cutover: 후속 (10 spec 책임)
7. CI/CD OIDC / GitHub Actions 자동 build / push: 후속 (07 spec 책임)

### 10. 안전 / 보안 점검 결과

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 변경 0건 (운영자 직접 작성한 `port_strategy_decision` Dockerfile / requirements.txt 는 본 노트 §2 에 사실로만 기록).
2. 실제 secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 ARN / image digest / IAM access key id / task ARN 본 노트 평문 기록 0건.
3. `secretsmanager:GetSecretValue` 실호출은 운영자 한정. Kiro 자동 검증 / 본 노트 작성 과정에서 secret value 호출 0건 (R-DOCS-001 정합).
4. AWS / Docker / ECR / ECS / IAM / Secrets Manager / RDS / GRANT 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행.
5. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 0건. 본 일자 검증은 `daily_buy_signal_run` / `daily_position_signal_run` 두 entrypoint 의 ECS RunTask 단건 실행만 다룸.


## 2026-06-13 Strategy Execution 책임 분리 + View Daily Batch 17단계 변경

운영자가 2026-06-13 직접 수행한 Strategy Execution(`port_strategy_execution`) 의 책임 분리, MarketConnector 신규 executor 와의 인계, View Daily Batch 17단계 재구성 결과를 누적 기록한다. 본 일자에 Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행했고, 실제 코드 / 정적 검증 / Maven compile 은 운영자가 직접 진행했다.

본 섹션은 같은 일자의 Strategy Decision ECS / Fargate 1차 포팅 검증(§1 ~ §10) 과 별개로 누적되는 두 번째 작업 결과다.

### 1. 책임 분리 결정

1. 결정 배경: 완료
   1) Strategy Execution 의 자동 buy / sell entrypoint 가 그동안 직접 `connector_buy.buy_stock()` / `connector_sell.sell_stock()` 를 호출하던 구조를 Strategy Execution = 주문 후보 생성 / 상태 전환만 책임, MarketConnector = 실제 broker 주문 제출 책임으로 분리.
   2) Strategy Execution 의 `--execute` 의미를 실제 broker 주문 제출이 아니라 `READY -> REQUESTED` 상태 전환만 담당하도록 좁힘.
   3) `REQUESTED` 상태의 strategy 주문을 broker / KIS API 로 실제 제출하는 책임은 MarketConnector 의 신규 executor `connector_strategy_order_execute.py` 가 담당(03 spec 1차 검증 결과 [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-13 섹션 정합).
2. 책임 분리 후 동작 정책: 완료
   1) Strategy Execution 의 `--execute` = `READY -> REQUESTED` 전환만 담당.
   2) MarketConnector executor `--execute` = `REQUESTED -> SUBMITTED` 또는 `FAILED` 전환만 담당.
   3) `connector_order_request_id` 는 Strategy Execution 에서 생성 / 업데이트하지 않는다.
   4) SELL position 의 `mark_position_sell_ordered()` 호출은 MarketConnector executor 측으로 이관(SELL 성공 시점에서만 실행).

### 2. Strategy Execution(`port_strategy_execution`) 변경 / 미변경 인벤토리

1. 변경 파일: 완료
   1) `execution_repository.py`
   2) `daily_auto_buy_execute_run.py`
   3) `daily_auto_sell_execute_run.py`
2. 주요 변경 요약: 완료
   1) `mark_execution_order_requested(conn, execution_order_id, result_payload)` repository 함수 신규 추가
   2) BUY / SELL 자동 실행 entrypoint 에서 MarketConnector 경로 하드코딩 제거
   3) `sys.path` 삽입 코드 제거
   4) `connector_buy` / `connector_sell` import 제거
   5) `buy_stock()` / `sell_stock()` 직접 호출 제거
   6) `--execute` 의미를 `READY -> REQUESTED` 로 변경
   7) SELL 경로의 `mark_position_sell_ordered()` 호출 제거(MarketConnector 측으로 이관)
   8) `connector_order_request_id` 는 Strategy Execution 측에서 생성 / 업데이트하지 않도록 유지
3. 정적 검증: 완료
   1) `python -m py_compile execution_repository.py daily_auto_buy_execute_run.py daily_auto_sell_execute_run.py` 통과
   2) 직접 import / call 검색 결과 0건(`connector_buy` / `connector_sell` / `buy_stock` / `sell_stock`)
   3) UTF-8 한글 literal 정상 표시 확인
4. 본 일자 검증 한계: 보류
   1) 주말 가드(WEEKEND) 차단으로 dry run 시 DB 후보 조회 단계까지는 미확인.
   2) `--execute` 실호출 0건.
   3) 평일 또는 안전한 테스트 데이터 환경에서 `READY -> REQUESTED` 전환의 실행 검증은 후속 분리.

### 3. MarketConnector(`port-marketconnector`) 신규 executor 인계

1. 인계 결과: 완료
   1) MarketConnector 측 신규 executor `connector_strategy_order_execute.py` 추가는 03 spec 1차 검증 결과로 누적([`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-13 섹션 §1 ~ §6 정합).
   2) 기존 검증된 paper 주문 entrypoint(`connector_buy.py` / `connector_sell.py` / `connector_order_common.py` / `connector_order_check.py` / `connector_balance.py` / `db_config.py` / `config.py` / `token_manager.py`) 변경 없음.
2. 신규 executor 동작 요약: 완료
   1) `REQUESTED` + `connector_order_request_id IS NULL` strategy 주문 조회.
   2) SELL 우선, BUY 후순위 정렬.
   3) 기본 dry run 은 목록 출력만 수행.
   4) `--execute` 시에만 `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` guard 적용.
   5) `--execute` 시 `connector_buy.buy_stock()` / `connector_sell.sell_stock()` lazy import 후 호출.
   6) 성공 시 `strategy_execution_order` 를 `SUBMITTED`, 실패 시 `FAILED` 로 갱신.
   7) SELL 성공 시 position `SELL_ORDERED` 갱신 helper 호출.
3. 본 일자 dry run 결과: 완료
   1) 출력: `[NO_TARGET] REQUESTED strategy order 없음`
   2) `REQUESTED` 대상 row 0건 상태이므로 정상 dry run 결과로 판단.
   3) 실제 REQUESTED 주문 row 처리 출력은 미검증(R-AUTO-009 정합 후속 추가).

### 4. View Daily Batch 17단계 변경(`port-view`)

1. 변경 파일: 완료
   1) `src/main/java/my/portfolio/port_view/service/DailyBatchService.java`
   2) `src/main/java/my/portfolio/port_view/util/DailyBatchLabelUtils.java`
2. DailyBatchService 신규 step 추가: 완료
   1) 위치: `DAILY_AUTO_BUY` 다음, `CONNECTOR_ORDER_CHECK` 이전.
   2) order: 12
   3) code: `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`
   4) name: `Strategy 주문 실행`
   5) workDir: `properties.getMarketconnectorDir()`
   6) command: `python connector_strategy_order_execute.py --execute`
3. 후속 step order 조정: 완료
   1) `CONNECTOR_ORDER_CHECK`: 13
   2) `SYNC_SELL_FILL`: 14
   3) `SYNC_BUY_FILL`: 15
   4) `SYNC_BUY_POSITION`: 16
   5) `BALANCE_REFRESH`: 17
4. 변경 후 Daily Batch 17단계 순서: 완료
   1) 1. `CONNECTOR_BALANCE`
   2) 2. `INTEREST_CRAWLER`
   3) 3. `PREPROCESSOR`
   4) 4. `BACKTEST_RESEARCH`
   5) 5. `BACKTEST_REPORT`
   6) 6. `DAILY_BUY_SIGNAL`
   7) 7. `DAILY_POSITION_SIGNAL`
   8) 8. `DAILY_BUY_EXECUTION`
   9) 9. `DAILY_SELL_EXECUTION`
   10) 10. `DAILY_AUTO_SELL`
   11) 11. `DAILY_AUTO_BUY`
   12) 12. `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`
   13) 13. `CONNECTOR_ORDER_CHECK`
   14) 14. `SYNC_SELL_FILL`
   15) 15. `SYNC_BUY_FILL`
   16) 16. `SYNC_BUY_POSITION`
   17) 17. `BALANCE_REFRESH`
5. isNoTarget 인식 문구 추가: 완료
   1) `REQUESTED strategy order 없음`
   2) `strategy execution requested 주문이 없음`
   3) `[NO_TARGET] REQUESTED strategy order 없음`
   4) `no requested`
   5) `no target`
6. DailyBatchLabelUtils 변경: 완료
   1) `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE -> Strategy 주문 실행`
   2) `stepCodeLabel()` 과 `stepCodeShortLabel()` 양쪽에 추가.
7. 정적 검증: 완료
   1) `.\mvnw.cmd clean compile` 성공.
   2) 신규 step 의 실제 Daily Batch 실행 / Python 주문 스크립트 실행 / DB / AWS 접근 0건.

### 5. Local 개발 / AWS Paper RDS 운영 원칙 (Local-to-AWS Paper RDS)

1. Paper 환경 source of truth: 완료
   1) Paper 환경의 source of truth 는 AWS Paper RDS 하나로 고정.
   2) 로컬에서 실행하더라도 `PORT_ENVIRONMENT=paper` 이면 AWS Paper RDS 를 바라본다.
   3) AWS 에서 실행하더라도 동일한 AWS Paper RDS 를 사용한다.
   4) 로컬 PostgreSQL 은 `LOCAL_DEV` fixture / 실험 / 백업 참고용으로만 사용한다.
   5) local DB 와 AWS Paper RDS 간 주문 / 체결 / 포지션 데이터 병합 또는 동기화는 하지 않는다.
2. 환경 구분: 완료
   1) `LOCAL_DEV`
       - local PostgreSQL 사용 가능
       - 개발 / 실험 / fixture 전용
       - 실제 paper 운영 아님
       - 주문 실행 금지
   2) `PAPER`
       - AWS Paper RDS 사용
       - 로컬 실행도 AWS Paper RDS 사용
       - AWS 실행도 AWS Paper RDS 사용
       - paper 주문 / 체결 / 포지션 source of truth
   3) `LIVE`
       - 후속 설계 대상(10 spec 통합)
       - 실전 운영 source of truth 는 paper 와 분리 필요
3. SSM Port Forwarding 방향: 완료
   1) RDS 는 Private 유지(02 spec 결정 정합 / R-SEC-001 정합).
   2) RDS Public 접근 허용 금지.
   3) 로컬에서 AWS Paper RDS 접속 시 SSM Port Forwarding 사용.
   4) 로컬에서는 `localhost:15433` 같은 포트로 접속하지만 실제 대상은 AWS Paper RDS.
   5) DB host 가 `localhost` 라고 해서 무조건 local DB 로 판단하면 안 된다.
   6) 실제 paper 주문 실행 guard 는 `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` 조합으로 판단(MarketConnector executor `--execute` guard 정합).
4. 예시 환경 변수(Local PC + SSM Port Forwarding 시): 완료
   1) `PORT_ENVIRONMENT=paper`
   2) `PORT_DB_TARGET=aws-paper`
   3) `INTEREST_DB_HOST=localhost`
   4) `INTEREST_DB_PORT=15433`
   5) `INTEREST_DB_NAME=portfolio`
   6) 비밀번호 / 계정 / 실제 RDS endpoint 평문 기록 금지(R-DOCS-001 정합).
5. 금지 사항: 완료
   1) local PostgreSQL 에서 paper 주문 실행 금지.
   2) local DB 와 AWS Paper RDS 간 `connector_order_request` 병합 금지.
   3) local DB 와 AWS Paper RDS 간 `connector_fill` 병합 금지.
   4) local DB 와 AWS Paper RDS 간 `strategy_execution_order` 병합 금지.
   5) local DB 와 AWS Paper RDS 간 `strategy_position_state` 병합 금지.

### 6. 본 일자 범위 밖 / 후속 인계

1. 실제 `--execute` end-to-end 검증: 후속(운영자 확인)
   1) Strategy Execution `--execute`(`READY -> REQUESTED`) + MarketConnector executor `--execute`(`REQUESTED -> SUBMITTED`) 의 평일 / 안전 테스트 데이터 기반 dry / integration 검증.
   2) SSM Port Forwarding / AWS Paper RDS 접속 정책 정리 후 별도 승인 하에 진행.
2. SSM Port Forwarding runbook 정리: 후속
   1) 02 spec runbook 또는 별도 운영 노트에 SSM Port Forwarding → AWS Paper RDS 접속 절차 정식 기재.
3. 모든 MS 의 `PORT_ENVIRONMENT` / `PORT_DB_TARGET` 점검: 후속
   1) 8개 MS 의 환경변수 인벤토리에 `PORT_ENVIRONMENT` / `PORT_DB_TARGET` 정합 여부 점검.
4. Strategy Execution AWS 포팅: 후속
   1) `port_strategy_execution` ECS / Fargate 포팅 검증(자동 BUY / SELL E2E OD-SAFE-001 ~ OD-SAFE-004 반영).
5. MarketConnector 신규 executor EC2 배포: 후속
   1) `connector_strategy_order_execute.py` 의 EC2 배포 후보 zip 또는 tag 산출.
   2) 03 spec 후속 phase 또는 07 spec(CI/CD) 책임으로 분리.
6. 신규 View 17단계 운영 전 end-to-end 검증: 후속
   1) Java `DailyBatchService` 변경 후 실제 Daily Batch 시퀀스의 12 ~ 17단계 구간이 운영 환경에서 매끄럽게 실행되는지 평일 / 안전 환경에서 검증(R-AUTO-011 정합).

### 7. 안전 / 보안 점검 결과

1. 본 일자 작업 범위에서 KIS / broker API 호출 0건. `--execute` 실호출 0건. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 0건.
2. RDS DDL/DML 0건. AWS 리소스 생성 / 변경 0건.
3. 실제 secret value / 계좌번호 / RDS endpoint hostname / RDS password / KIS app key / KIS app secret / token / account-id / 실제 ARN / instance-id / image digest / IAM access key id 본 노트 평문 기록 0건.
4. Strategy Execution 측 변경 파일 3종(`execution_repository.py` / `daily_auto_buy_execute_run.py` / `daily_auto_sell_execute_run.py`), MarketConnector 측 신규 파일 1종(`connector_strategy_order_execute.py`), View 측 변경 파일 2종(`DailyBatchService.java` / `DailyBatchLabelUtils.java`) 은 본 노트에 사실로만 기록하고 본문 전체 인용 0건.
5. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 본 일자 작업으로 인한 변경 0건.


## 2026-06-13 Strategy Execution AWS 포팅 사전 검증 (`execution_app` AWS Paper RDS 접속)

본 섹션은 같은 일자(2026-06-13) 의 Strategy Decision ECS / Fargate 1차 포팅 검증(§1 ~ §10) 및 Strategy Execution 책임 분리 + View Daily Batch 17단계(앞 섹션 §1 ~ §7) 와 별개로, 운영자가 직접 수행한 Strategy Execution(`port_strategy_execution`) AWS 포팅 진입 전 단계의 `execution_app` 기반 AWS Paper RDS 접속 1차 검증 결과를 누적 기록한다. 자세한 SSM Port Forwarding Runbook / 절차는 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook 섹션 참조.

### 1. 검증 배경 / 입력

1. 책임 분리 결과(앞 섹션 §1 ~ §3) 입력: 완료
   1) Strategy Execution `--execute` = `READY -> REQUESTED` 상태 전환만 담당.
   2) MarketConnector 신규 executor `connector_strategy_order_execute.py` `--execute` = `REQUESTED -> SUBMITTED` / `FAILED` 전환만 담당.
   3) `connector_order_request_id` 는 Strategy Execution 측에서 생성 / 업데이트하지 않음.
2. SSM Port Forwarding 표준 경유지 결정 입력: 완료
   1) OD-NET-010 = `portfolio-paper-marketconnector-ec2` (instance id `i-0fce77927b7397b88`).
   2) Local PC `localhost:15433` → SSM tunnel → 본 EC2 → AWS Paper RDS `portfolio-paper-rds:5432`.
   3) RDS `PubliclyAccessible = False` 유지.

### 2. `execution_app` 접속 1차 검증

1. 접속 파라미터: 완료
   1) host: `localhost`
   2) port: `15433`
   3) dbname: `portfolio`
   4) user: `execution_app`
   5) password: 환경변수 `PGPASSWORD` 사용(평문 노출 0건, R-DOCS-001 정합)
2. Python `psycopg2` 접속 결과: 완료
   1) `current_user`: `execution_app`
   2) `current_database`: `portfolio`
   3) `search_path`: `execution, decision, research, connector, preprocessor, interest, reference, legacy, public`
3. 정합성 판단: 완료
   1) `execution_app` 의 search_path 가 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) §3 / OD-DB-006 / OD-DB-007 정합(legacy 포함되지만 USAGE 미부여로 실제 접근 차단).
   2) Strategy Execution 포팅 진입 전 단계에서 AWS Paper RDS 접속 가능성 1차 실증.
   3) SELECT 조회 한정 — INSERT / UPDATE / DELETE / DDL 0건 / 주문 / 체결 entrypoint 호출 0건.

### 3. Strategy Execution 포팅 후속 인계

1. AWS Paper RDS app role 접속 확인: 완료
   1) Strategy Execution 의 entrypoint(`daily_auto_buy_execute_run.py` / `daily_auto_sell_execute_run.py` / `execution_repository.py`) 가 `execution_app` 으로 AWS Paper RDS 에 접속할 수 있는 사전 조건 충족.
2. 본 일자 범위 밖: 후속
   1) Strategy Execution Dockerfile / requirements.txt 신규 생성: 후속(`port_strategy_decision` Dockerfile 패턴 정합 — 04 spec 2026-06-13 §2 입력).
   2) 로컬 build + container import smoke + ECR push: 후속.
   3) ECS Task Definition 등록(awsvpc / Fargate / cpu-memory / Secrets Manager `/portfolio/paper/rds/execution-app` 신규 생성 + Execution Role inline policy 추가 + Task Role 신규 생성): 후속.
   4) ECS RunTask 단건 실행 검증: 후속.
   5) `execution_app` 의 schema 별 GRANT 매트릭스 정식 정리는 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) 후속 갱신으로 분리(R-DATA-005 정합).
3. 평일 또는 안전한 테스트 데이터로 `READY -> REQUESTED -> SUBMITTED` end-to-end dry / integration 검증: 후속(R-AUTO-009 / R-AUTO-010 정합).
4. EventBridge Scheduler / Step Functions state machine 정의(buy-signal → position-signal → strategy-execution 순서 강제 + 자동 재시도 금지 OD-SAFE-004 반영): 04 spec 후속 phase 책임.

### 4. 안전 / 보안 점검

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 변경 0건. AWS 리소스 생성 / 수정 / 삭제 0건.
2. password / secret value / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 IAM access key id / 실제 secret ARN 본 노트 평문 기록 0건.
3. broker / KIS / 주문 / 체결 entrypoint 호출 0건. RDS DDL/DML 0건. `--execute` 실호출 0건.
4. instance id(`i-0fce77927b7397b88`) / private IP(`10.0.20.165`) / local port(`15433`) / RDS endpoint hostname / SSM session id(`terraform-vjp3fv3nz73konetcevdzjh9de`) 는 운영 식별자로서 사실 기록(사용자 명시 정책 정합 — secret 가 아님).


## 2026-06-13 Strategy Execution AWS 포팅 사전 검증 — psql 18 + pgAdmin4 보강

본 섹션은 같은 일자 앞 섹션(`## 2026-06-13 Strategy Execution AWS 포팅 사전 검증 (`execution_app` AWS Paper RDS 접속)`) §1 ~ §4 의 후속이며, Strategy Execution(`port_strategy_execution`) AWS 포팅 진입 전 단계의 사전 client 호환 점검을 추가 2종(로컬 PostgreSQL 18 `psql.exe` + pgAdmin4) 으로 보강한 결과를 누적 기록한다. 자세한 결과는 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 보강(psql 18 client + pgAdmin4 접속 검증) 섹션 참조.

### 1. 추가 client 호환 1차 실증

1. 로컬 PostgreSQL 18 psql client: 완료
   1) 직접 경로 실행 — `& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -p 15433 -U portfolio_admin -d portfolio`
   2) client `18.1` / server `18.4` / `SSL TLSv1.3` / `inet_server_addr = 10.0.20.165` / `inet_server_port = 5432` 출력 확인.
   3) `execution_app` 별도 psql 접속은 본 일자 작업 범위 밖 — Python `psycopg2` 검증(앞 섹션 §2)으로 이미 통과.
2. pgAdmin4: 완료
   1) Server 등록 — Name `AWS Paper RDS - portfolio` / Host `localhost` / Port `15433` / Maintenance database `portfolio` / Username `portfolio_admin` / SSL mode `Prefer`.
   2) `current_user` / `current_database` / `inet_server_addr` / `inet_server_port` 출력 확인 — `portfolio_admin` / `portfolio` / `10.0.20.165` / `5432`.
   3) Strategy Execution 이 다룰 핵심 schema / table 조회 가능 확인:
       - `execution.strategy_execution_order` (책임 분리 결과 `READY -> REQUESTED -> SUBMITTED`/`FAILED` 상태 전이 대상)
       - `execution.strategy_execution_plan`
       - `execution.strategy_position_state`
       - `execution.connector_signal_order_map`
       - `connector.connector_account` / `connector.connector_order_request` / `connector.connector_order_event` / `connector.connector_fill`
       - `decision.strategy_daily_position_decision` / `decision.strategy_daily_run`
   4) 본 일자 검증은 SELECT 가능 여부 확인까지만 수행 — INSERT / UPDATE / DELETE / DDL 0건.

### 2. Strategy Execution 포팅 사전 검증 종합

1. Python `psycopg2` 기반 `execution_app` 접속(앞 섹션 §2): 통과.
2. 로컬 PostgreSQL 18 `psql.exe` 직접 경로 기반 `portfolio_admin` 접속: 통과.
3. pgAdmin4 기반 `portfolio_admin` 접속 + Strategy Execution 핵심 schema / table 조회: 통과.
4. 본 일자 client 3종(Python `psycopg2` / psql 18 / pgAdmin4) 모두 같은 SSM Port Forwarding tunnel(local port `15433`) 위에서 동작.
5. Strategy Execution AWS 포팅 본 phase 진입 전 단계의 사전 접속 가능성 다중 client 1차 실증 완료.

### 3. 본 일자 범위 밖 / 후속 인계

1. Strategy Execution Dockerfile / requirements.txt 신규 생성 / ECR push / Secrets Manager `/portfolio/paper/rds/execution-app` 신규 / Execution Role inline policy 추가 / Task Role 신규 / ECS Task Definition 등록 / RunTask 단건 실행 검증: 후속(앞 섹션 §3 그대로).
2. `execution_app` 의 schema 별 GRANT 매트릭스 정식 정리는 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) 후속 갱신으로 분리(R-DATA-005 정합).
3. 평일 또는 안전한 테스트 데이터로 `READY -> REQUESTED -> SUBMITTED` end-to-end dry / integration 검증: 후속(R-AUTO-009 / R-AUTO-010 정합).
4. pgAdmin4 의 환경별(paper / live) 서버 분리 등록 정책: 후속(10 spec — live 환경 진입 시점).

### 4. 안전 / 보안 점검

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 변경 0건. AWS 리소스 변경 0건.
2. password / secret value / KIS app key / KIS app secret / 계좌번호 / token / account-id 평문 기록 0건. pgAdmin4 / psql 18 의 비밀번호는 운영자 로컬 PC 환경에서만 사용되며 본 노트 / 콘솔 캡처 / 로그 평문 기록 금지(R-DOCS-001 정합).
3. broker / KIS / 주문 / 체결 entrypoint 호출 0건. RDS DDL/DML 0건. `--execute` 실호출 0건.


## 2026-06-13 Strategy Execution ECS / Fargate 1차 포팅 검증

본 섹션은 같은 일자 앞 섹션들(`## 2026-06-13 Strategy Decision ECS / Fargate 1차 포팅 검증` / `## 2026-06-13 Strategy Execution 책임 분리 + View Daily Batch 17단계 변경` / `## 2026-06-13 Strategy Execution AWS 포팅 사전 검증` / `## 2026-06-13 Strategy Execution AWS 포팅 사전 검증 — psql 18 + pgAdmin4 보강`) 의 후속이며, 운영자가 직접 수행한 `port_strategy_execution` 의 ECS / Fargate 본 phase 1차 포팅 검증 결과를 누적 기록한다. 본 일자에 Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행했고, 실제 Docker / ECR / ECS / IAM / Secrets Manager / RDS / GRANT 작업은 운영자가 직접 진행했다. 1차 적용 환경은 `aws-paper`, region `ap-northeast-2`. broker / KIS 호출 0건 / `connector_order_request` 생성 0건 / `READY -> REQUESTED` 실제 전환 0건. live 자동 주문은 후속 검증 / 승인 전까지 여전히 금지(OD-SAFE-002 / OD-SAFE-003 / OD-SAFE-004 정합).

### 1. AWS 실행 구조 확인 / Docker 결정

1. As-Is 실행 방식 확인: 완료
   1) 로컬 repository 경로 확인: 완료 (`C:\Workspaces\port_strategy_execution`)
   2) 책임 분리 입력: 완료 (앞 섹션 §1 정합)
       - Strategy Execution `--execute` = `READY -> REQUESTED` 상태 전환만 담당
       - MarketConnector executor `connector_strategy_order_execute.py --execute` = `REQUESTED -> SUBMITTED` / `FAILED` 전환 (별도 책임 / 별도 EC2 / 본 Task Definition 에 포함하지 않음)
       - SELL position `mark_position_sell_ordered()` 호출은 MarketConnector 측
   3) 7개 Daily Batch step 의 entrypoint 후보 확인: 완료
       - `daily_buy_execution_run.py`
       - `daily_sell_execution_run.py`
       - `daily_auto_buy_execute_run.py`
       - `daily_auto_sell_execute_run.py`
       - `execution_sync_buy_fill.py`
       - `execution_sync_sell_fill.py`
       - `execution_sync_buy_position.py`
   4) `execution_config.py` 가 import 시점에 `INTEREST_DB_PASSWORD` 환경변수를 요구하는 구조 확인: 완료 (smoke 시 dummy env 로 통과 / ECS 실행에서는 Secrets Manager 기반 environment injection 으로 해결)
2. Task Definition 운영 방식 확정: 완료 (OD-MS-017 정합)
   1) 단일 Task Definition + command override 방식 확정
   2) Strategy Decision(2026-06-13 §6 / OD-MS-013) 의 Task Definition 2개 분리 방식과는 의도적으로 다른 패턴
   3) 선택 이유:
       - 7개 entrypoint 가 같은 image / role / secret / log group / cpu / memory 를 공유
       - Task Definition 7개 분리는 운영 복잡도 측면에서 과도
       - Step Functions state 별 command override 매핑에 적합
   4) View Daily Batch 의 7개 step 은 논리적으로 유지 가능 — AWS orchestration 에서는 Step Functions state 별 command override 로 매핑(후속 분리)
   5) MarketConnector executor `connector_strategy_order_execute.py --execute` 는 본 Task Definition 에 포함하지 않음

### 2. Dockerfile / requirements.txt 신규 생성

1. `port_strategy_execution` 1차 Dockerfile / requirements.txt 운영자 직접 신규 생성: 완료
   1) Docker build context: `C:\Workspaces`
   2) image 내부에 `port_strategy_execution` 소스 vendoring (Strategy Decision Dockerfile 패턴 정합)
   3) 1차 dependency: `psycopg2-binary`
   4) default CMD 는 안전한 `py_compile` 계열 — 실제 운영 entrypoint 는 ECS Task Definition `command` 또는 RunTask `--overrides` 의 command override 에서 결정
2. Dockerfile / requirements.txt 본문 전체 인용 0건 — 본 노트에는 운영자가 직접 신규 작성한 사실만 기록(R-DOCS-001 정합).

### 3. 로컬 이미지 빌드 / Smoke

1. 로컬 Docker build: 완료
   1) image tag: `portfolio-strategy-execution:paper-20260613`
   2) local latest tag: `portfolio-strategy-execution:paper-latest`
2. container smoke: 완료
   1) `python -m py_compile` 통과
   2) import smoke 통과 (`execution_config.py` import 시 dummy env 주입으로 통과)
   3) `port_strategy_execution` 의 7개 entrypoint import 시 broker / KIS / RDS 호출 0건 확인
   4) 실제 ECS 실행에서는 dummy env 가 아니라 Secrets Manager 의 `/portfolio/paper/rds/execution-app` JSON multi-key 가 environment 로 주입됨

### 4. ECR Repository / Push

1. ECR repository 생성 / 확인: 완료
   1) repository 이름: `portfolio-strategy-execution`
   2) 환경 분리 정책: paper / live 환경별 repository 분리하지 않음 (08 / 04 spec 정합)
       - paper / live 구분은 image tag / Task Definition / Secrets·SSM path / IAM Task Role / environment variables / RDS 설정 6개 항목에서 처리
2. ECR push: 완료
   1) tag `paper-20260613`
   2) tag `paper-latest`
3. image digest: 운영자가 직접 확인. 실제 digest / 실제 ECR URI / account-id 본 노트 / spec 산출물 평문 기록 0건(`<image-digest>` placeholder).

### 5. CloudWatch Log Group / Secrets Manager / IAM Role

1. CloudWatch Log Group 생성 / 확인: 완료
   1) `/portfolio/paper/strategy-execution`
   2) retention: 14일 (08 / 04 spec 정합 / OD-OBS-002 정합)
2. Secrets Manager secret 생성 / 확인: 완료
   1) `/portfolio/paper/rds/execution-app`
   2) JSON multi-key 방식 (`host` / `port` / `dbname` / `username` / `password`) — 08 / 04 spec preprocessor·decision-app secret 패턴 정합
   3) 실제 secret value / endpoint / password / ARN / account-id 본 노트 미기록 (R-DOCS-001 / R-DATA-006 정합)
3. ECS Task Execution Role 보강: 완료
   1) 이름: `portfolio-paper-ecs-task-execution-role` (08 / 04 spec 에서 1차 생성, 본 일자 재사용)
   2) execution-app DB Secret read inline policy 추가: 완료
       - secret ARN 한정 / Resource·Action wildcard 0건 / 03 §13 / OD-SEC-006 정합
       - 변경 일자 / 변경자 / 변경 사유 / 변경 전·후 항목 요약은 본 노트에서만 4줄 요약(JSON 본문 전체 인용 금지)
4. ECS Task Role 확인: 완료
   1) 이름: `portfolio-paper-execution-task-role`
   2) trust: `ecs-tasks.amazonaws.com`
   3) execution-app 실행 시점 SDK / runtime 권한은 본 일자에 추가하지 않음 (RDS 접속은 Execution Role 의 secret 주입 + RDS 자격으로 처리)

### 6. ECS Network 확인

1. ECS Cluster 재사용: 완료
   1) cluster: `portfolio-paper-cluster` (08 / 04 spec 에서 1차 생성, 본 일자 재사용)
2. Network 구조: 완료
   1) public subnet 2개 사용 — `public-a` / `public-b` (OD-NET-004 정합)
   2) security group: `sgroup-strategy-tasks`
   3) RDS SG inbound 5432 에서 `sgroup-strategy-tasks` source 허용 확인 (R-SEC-001 / 02 spec design §4 정합)
   4) VPC Endpoint SG 443 source 에서도 `sgroup-strategy-tasks` 허용 확인 (NAT-free 정책 정합 / R-NET-003 mitigation 정합)
   5) RunTask `assignPublicIp = ENABLED` 방식 (OD-NET-004 1차 검증 정합 — 08 / 04 spec 결과 재사용)
3. 실제 subnet id / sg id / VPC Endpoint id 본 노트 평문 기록 0건 — 02 spec design.md / operation-notes.md 의 식별자 표기 정책 정합.

### 7. ECS Task Definition 등록

1. Task Definition 등록: 완료
   1) family: `portfolio-paper-strategy-execution`
   2) revision: 1
   3) status: ACTIVE
   4) networkMode: `awsvpc`
   5) launch type: Fargate
   6) cpu: 512
   7) memory: 1024
   8) container name: `strategy-execution`
   9) default command: 안전한 `py_compile` 계열 — 운영자가 RunTask `--overrides` 로 command override 하여 7개 entrypoint 중 하나를 선택 실행
2. environment: 완료
   1) `PORT_ENVIRONMENT=paper` (OD-ENV-007 / OD-MS-016 guard 정합)
   2) `PORT_DB_TARGET=aws-paper` (동상)
3. secrets (Secrets Manager `/portfolio/paper/rds/execution-app` JSON multi-key 매핑): 완료
   1) `INTEREST_DB_HOST`
   2) `INTEREST_DB_PORT`
   3) `INTEREST_DB_NAME`
   4) `INTEREST_DB_USER`
   5) `INTEREST_DB_PASSWORD`
4. log group 연결: `/portfolio/paper/strategy-execution`
5. 환경변수 호환 정책: 기존 코드와의 호환을 위해 `INTEREST_DB_*` 환경변수 명을 그대로 사용 (OD-DB-003 정합 / 04 spec Strategy Decision 동일 패턴)
6. 실제 image digest / task ARN / account-id / 실제 secret ARN 본 노트 평문 기록 0건.

### 8. ECS RunTask command override 검증 (8종)

본 일자 검증은 모두 운영자가 직접 RunTask 단건 실행으로 수행. 모든 RunTask 는 `--overrides` 의 `containerOverrides[].command` 로 entrypoint 선택. broker / KIS 호출 0건 / `connector_order_request` 생성 0건 / `READY -> REQUESTED` 실제 전환 0건.

#### 8-1. 기본 command (`py_compile`) 검증

1. RunTask 실행: 완료
   1) command override: default `py_compile` 계열
   2) lastStatus: STOPPED
   3) exitCode: 0
   4) ECR pull: 성공
   5) Secret injection: 성공
   6) Task startup: 성공
   7) CloudWatch log stream 생성: 성공
2. 판단: container baseline (image / role / secret / log group / cpu / memory / network) 1차 검증 통과.

#### 8-2. `execution_sync_buy_fill.py` 검증

1. RunTask 실행: 완료
   1) command override: `python -m execution_sync_buy_fill`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch 결과 요약: 완료
   1) `submitted_buy_orders`: 0
   2) `synced_count`: 0
   3) `skipped_count`: 0
3. 판단: SUBMITTED BUY fill sync 가 sync 대상 0건 상태에서 정상 종료(NO_TARGET 정상 동작).

#### 8-3. `execution_sync_sell_fill.py` 검증

1. RunTask 실행: 완료
   1) command override: `python -m execution_sync_sell_fill`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch 결과 요약: 완료
   1) `submitted_position_sell_orders`: 0
   2) `synced_count`: 0
   3) `skipped_count`: 0
3. 판단: SUBMITTED SELL fill sync 가 sync 대상 0건 상태에서 정상 종료(NO_TARGET 정상 동작).

#### 8-4. `execution_sync_buy_position.py` 검증

1. RunTask 실행: 완료
   1) command override: `python -m execution_sync_buy_position`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch 결과 요약: 완료
   1) `filled_buy_orders_without_position`: 0
   2) `synced_count`: 0
   3) `skipped_count`: 0
3. 판단: FILLED BUY → position 생성 sync 가 대상 0건 상태에서 정상 종료(NO_TARGET 정상 동작).

#### 8-5. `daily_buy_execution_run.py` 검증

1. RunTask 실행: 완료
   1) command override: `python -m daily_buy_execution_run`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch 결과 요약: 완료
   1) WEEKEND guard 차단 — 한국 영업일 아님
   2) NO_TARGET 출력
   3) `connector_order_request` 생성 0건
3. 판단: 주말 안전 가드 정상 차단 — broker / KIS 호출 가능성 0.

#### 8-6. `daily_sell_execution_run.py` 검증

1. RunTask 실행: 완료
   1) command override: `python -m daily_sell_execution_run`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch 결과 요약: 완료
   1) WEEKEND guard 차단 — 한국 영업일 아님
   2) NO_TARGET 출력
   3) `connector_order_request` 생성 0건
3. 판단: 주말 안전 가드 정상 차단 — broker / KIS 호출 가능성 0.

#### 8-7. `daily_auto_sell_execute_run.py --execute` 검증

1. RunTask 실행: 완료
   1) command override: `python -m daily_auto_sell_execute_run --execute`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch 결과 요약: 완료
   1) WEEKEND guard 차단 — 한국 영업일 아님
   2) NO_TARGET 출력
   3) `READY -> REQUESTED` 실제 전환 0건 (OD-MS-016 정합 — `--execute` 의미는 책임 분리 후 `READY -> REQUESTED` 로 좁혀짐)
   4) broker / KIS 주문 호출 0건
   5) `connector_order_request` 생성 0건
3. 판단: 주말 안전 가드가 `--execute` 포함 entrypoint 도 정상 차단 — `--execute` 실호출이 발생했음에도 실제 주문 / 상태 전환 0건.

#### 8-8. `daily_auto_buy_execute_run.py --execute` 검증

1. RunTask 실행: 완료
   1) command override: `python -m daily_auto_buy_execute_run --execute`
   2) lastStatus: STOPPED / exitCode: 0
2. CloudWatch 결과 요약: 완료
   1) WEEKEND guard 차단 — 한국 영업일 아님
   2) NO_TARGET 출력
   3) `READY -> REQUESTED` 실제 전환 0건 (OD-MS-016 정합)
   4) broker / KIS 주문 호출 0건
   5) `connector_order_request` 생성 0건
3. 판단: 주말 안전 가드가 `--execute` 포함 entrypoint 도 정상 차단 — `--execute` 실호출이 발생했음에도 실제 주문 / 상태 전환 0건.

### 9. 1차 검증 완료 기준

1. Strategy Execution ECS Task 단건 실행 성공: 완료 (8종 모두 exitCode 0)
2. 단일 Task Definition + command override 운영 방식 검증 완료
3. AWS Paper RDS `execution_app` 접속 검증 완료 (앞 섹션 사전 검증 + 본 섹션 실제 RunTask 실행)
4. Secrets Manager 기반 DB 환경변수 주입 검증 완료
5. CloudWatch Logs 출력 검증 완료
6. 주말 안전 가드 검증 완료 — `--execute` 포함 entrypoint 도 안전하게 차단됨
7. broker / KIS 주문 호출 0건
8. `READY -> REQUESTED` 실제 전환 0건
9. `connector_order_request` 생성 0건
10. 평일 또는 안전 테스트 데이터 기반 `READY -> REQUESTED -> SUBMITTED` end-to-end 검증은 후속 분리(R-AUTO-009 / R-AUTO-010 / R-AUTO-011 정합)
11. 판단: Strategy Execution AWS ECS / Fargate 본 phase 핵심 검증 완료. live 자동 주문은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지.

### 10. 본 일자 범위 밖 / 후속 인계

1. Step Functions Standard / Express 선정 + state machine 정의: 후속 (04 spec 후속 phase)
   1) Strategy Decision 2개 Task Definition(buy-signal / position-signal, OD-MS-013 정합) + Strategy Execution 단일 Task Definition + command override(OD-MS-017) 를 하나의 state machine 에서 연결
   2) BUY / SELL / fill sync / position 변경 step 은 자동 재시도 금지 (OD-SAFE-004 / R-AUTO-001 정합)
   3) idempotent step (sync 계열의 NO_TARGET 종료) 만 자동 재시도 허용
2. EventBridge Scheduler → Step Functions / ECS RunTask 정기 트리거 연계: 후속
3. MarketConnector executor 인계 검증: 후속
   1) Strategy Execution `--execute`(`READY -> REQUESTED`) 후 MarketConnector executor `connector_strategy_order_execute.py --execute`(`REQUESTED -> SUBMITTED`/`FAILED`) end-to-end 검증
   2) MarketConnector executor 는 본 Task Definition 에 포함하지 않음 — MarketConnector EC2 측 별도 executor 책임 (03 spec 2026-06-13 §1 ~ §5 정합)
4. 평일 또는 안전 테스트 데이터 기반 `READY -> REQUESTED -> SUBMITTED` end-to-end dry / integration 검증: 후속 (R-AUTO-009 / R-AUTO-010 정합)
5. View Daily Batch 의 ProcessBuilder 직접 실행 → 관제 UI 격상: 후속 (장기 / 05 spec)
   1) View Daily Batch 의 17단계(2026-06-13 §4 정합) 자체 구현 변경은 본 일자 작업 범위 밖
   2) 본 일자 작업은 ECS RunTask command override 가 가능함을 검증한 상태
6. Step Functions state 별 command override 매핑 표 정식 정리: 후속
   1) state name ↔ command override allowlist 1:1 표 (R-AUTO-014 mitigation 정합)
   2) View 또는 Step Functions 에서 임의 command 입력 금지 정책 명시
7. Strategy Execution `execution_app` 의 schema 별 GRANT 매트릭스 정식 정리: 후속 (02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) 후속 갱신 / R-DATA-005 정합)
8. aws-live cutover: 후속 (10 spec 책임)
9. CI/CD OIDC / GitHub Actions 자동 build / push: 후속 (07 spec 책임)

### 11. 안전 / 보안 점검 결과

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 변경 0건. 운영자 직접 작성한 `port_strategy_execution` Dockerfile / requirements.txt 는 본 노트 §2 에 사실로만 기록(본문 전체 인용 0건).
2. 실제 secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 ARN / image digest / IAM access key id / task ARN 본 노트 평문 기록 0건.
3. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건. Kiro 자동 검증 / 본 노트 작성 과정에서 secret value 호출 0건 (R-DOCS-001 정합).
4. AWS / Docker / ECR / ECS / IAM / Secrets Manager / RDS / GRANT 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행.
5. CloudWatch Logs 본문에 secret value 평문 출력 0건 — Task Definition `secrets` 필드는 환경변수로만 주입되며 stdout 출력에서는 마스킹.
6. broker / KIS / 주문 / 체결 entrypoint 호출 0건. 본 일자 검증은 7개 entrypoint 의 ECS RunTask 단건 실행만 다룸 — 모두 주말 가드 또는 sync 대상 0건으로 NO_TARGET 정상 종료.
7. `--execute` 포함 entrypoint 2종(`daily_auto_sell_execute_run.py --execute` / `daily_auto_buy_execute_run.py --execute`) 도 주말 가드로 차단 — `READY -> REQUESTED` 실제 전환 0건 / `connector_order_request` 생성 0건.
8. live 자동 주문은 후속 검증 / 승인 전까지 여전히 금지 (OD-SAFE-002 / OD-SAFE-003 / OD-SAFE-004 정합). 본 일자는 paper 1차 검증.
9. 운영 식별자(앞 섹션의 instance id / private IP / SSM session id / RDS endpoint hostname) 그대로 재사용. 추가 운영 식별자: image tag(`paper-20260613` / `paper-latest`), Task Definition family(`portfolio-paper-strategy-execution`) / revision(1) / container name(`strategy-execution`), security group 이름(`sgroup-strategy-tasks`), Log Group 이름(`/portfolio/paper/strategy-execution`), Secret 이름(`/portfolio/paper/rds/execution-app`), Task Role 이름(`portfolio-paper-execution-task-role`), Execution Role 이름(`portfolio-paper-ecs-task-execution-role`) — 모두 운영 식별자로서 사실 기록 / secret 가 아님.
