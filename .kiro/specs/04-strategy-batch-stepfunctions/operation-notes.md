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


## 2026-06-16 Strategy Decision safe step ECS dry-run 재검증

운영자가 2026-06-16 직접 수행한 Strategy Decision safe step(`DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL`) ECS / Fargate 단건 재실행 결과를 누적 기록한다. 본 섹션은 같은 spec 의 2026-06-13 Strategy Decision ECS / Fargate 1차 포팅 검증(§1 ~ §10) 의 후속이며, 08 spec 의 2026-06-16 Crawler 데이터 미수집 해결 + KRX EC2 자동화 성공으로 raw 최신성이 회복된 입력 데이터 + 09 spec 의 2026-06-16 Strategy Research AWS Batch Backend dry-run 재검증으로 backtest run row 가 갱신된 상태(run_id `439d78e7-...` / backtest_end_date `2026-06-15`) 위에서 backend AWS E2E dry-run safe subset(Research → Decision)을 재가동하기 위한 1차 검증이다. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행했고, 실제 ECS RunTask / IAM / RDS 작업은 운영자가 직접 수행했다. 실제 BUY / SELL 주문 / `--execute` 주문 전송 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건(OD-SAFE-001 ~ OD-SAFE-004 / OD-MS-021 / OD-MS-013 정합).

### 1. DAILY_BUY_SIGNAL 단건 ECS / Fargate 재실행

1. RunTask 실행: 완료
   1) Task Definition: `portfolio-paper-strategy-decision-buy-signal:1`
   2) launch type: FARGATE / awsvpc
   3) command override: `python -m port_strategy_decision.daily_buy_signal_run`
   4) image tag: `paper-20260613`(2026-06-13 §3 / §4 그대로 재사용)
   5) decision-app DB secret 환경변수 주입(`/portfolio/paper/rds/decision-app` JSON multi-key 5종 / `INTEREST_DB_*` 환경변수 호환 정책 / OD-DB-003 정합)
   6) log group: `/portfolio/paper/strategy-decision`
   7) log stream prefix: `buy-signal`
2. 실행 결과: 성공
   1) lastStatus: `STOPPED`
   2) stopCode: `EssentialContainerExited`
   3) container exitCode: `0`
   4) Batch / 자동 재시도 0건(OD-SAFE-004 / R-AUTO-001 정합 — Strategy Decision 은 ECS RunTask 단건 / Step Functions 자동 재시도 정책 도입 전)
   5) 실제 주문 전송 옵션 없음 — broker / KIS 호출 0건 / `connector_order_request` 생성 0건
3. `decision.strategy_daily_signal` 결과 확인: 완료
   1) run_date: `2026-06-16`
   2) data_date: `2026-06-15`(2026-06-16 §3 정합 — 09 spec backtest run 의 backtest_end_date 와 동일 일자)
   3) signal_date: `2026-06-16`
   4) signal_type: `BUY`
   5) signal_status: `READY`
   6) row_count: `4`
   7) rank range: `1 ~ 4`
   8) created_at / updated_at: `2026-06-16 07:28:56 UTC`
4. 안전 점검: 완료
   1) BUY signal `READY` 상태 row 만 생성 — 실제 주문 전송은 후속 step(`DAILY_AUTO_BUY` / `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) 책임이며 본 일자 미실행
   2) `connector_order_request` / `connector_fill` row 변경 0건
   3) image digest / task ARN / account-id 본 노트 평문 기록 0건(`<image-digest>` / `<task-arn>` / `<account-id>` placeholder)

### 2. DAILY_POSITION_SIGNAL 단건 ECS / Fargate 재실행

1. RunTask 실행: 완료
   1) Task Definition: `portfolio-paper-strategy-decision-position-signal:1`
   2) launch type: FARGATE / awsvpc
   3) command override: `python -m port_strategy_decision.daily_position_signal_run`
   4) image tag: `paper-20260613`
   5) decision-app DB secret 환경변수 주입
   6) log group: `/portfolio/paper/strategy-decision`
   7) log stream prefix: `position-signal`
2. 실행 결과: 성공
   1) lastStatus: `STOPPED`
   2) stopCode: `EssentialContainerExited`
   3) container exitCode: `0`
   4) Batch / 자동 재시도 0건
   5) 실제 주문 전송 옵션 없음 — broker / KIS 호출 0건
3. position signal 실행 결과 확인: 완료
   1) daily_run_id: `45`
   2) run_date: `2026-06-16`
   3) data_date: `2026-06-15`
   4) market_signal: `AGGRESSIVE`
   5) positions: `0`
   6) decision_count: `0`
   7) sell_count: `0`
   8) hold_count: `0`
   9) skip_count: `0`
   10) decision_ids: `[]`
4. `decision.strategy_daily_position_decision` 상태: 정상 skip
   1) max_decision_date: `2026-05-29` 유지(신규 row 미생성)
   2) 원인: 현재 보유 포지션 0건 — position decision 대상 자체가 없음(BUY signal `READY` 상태 row 가 실제 매수로 전환된 적 없음 / `connector_order_request` 0건 / `strategy_position_state` OPEN 0건)
   3) 판단: 오류가 아닌 정상 skip — Strategy Decision position-signal 의 정상 운영 케이스이며 본 일자 결과는 R-AUTO-001 / R-AUTO-002 위반 아님
5. 안전 점검: 완료
   1) `connector_order_request` / `connector_fill` / `strategy_position_state` row 변경 0건
   2) SELL position `mark_position_sell_ordered()` 호출 0건(OD-MS-016 정합 — MarketConnector executor 측 책임)
   3) intraday stop SELL 생성 0건

### 3. Backend AWS E2E dry-run safe subset 진행 상태

1. safe subset 완료 항목(2026-06-16 시점, OD-MS-021 정합 — 17단계 순서 그대로):
   1) 1번 `CONNECTOR_BALANCE`: 완료(2026-06-15 §2 / 03 spec / `connector_balance_snapshot` 저장)
   2) 2번 `INTEREST_CRAWLER`: 완료(2026-06-16 / 08 spec — non-GUI rev7 + KRX EC2 worker hybrid 구조 완료 / raw 최신성 회복)
   3) 3번 `PREPROCESSOR`: 실행 완료(2026-06-15 §4 / 08 spec — raw 입력 데이터 회복 후 재실행 가능 상태 도달까지 1차 / 신규 raw 입력 기반 재실행은 후속 task 77)
   4) 4번 `BACKTEST_RESEARCH`: 완료(2026-06-16 / 09 spec §1 — `portfolio-paper-strategy-research:5` / run_id `439d78e7-...`)
   5) 5번 `BACKTEST_REPORT`: 완료(2026-06-16 / 09 spec §2 / §3 / §4 — `portfolio-paper-strategy-report:3` / S3 객체 4건)
   6) 6번 `DAILY_BUY_SIGNAL`: 완료(본 섹션 §1 — row_count `4` / `READY`)
   7) 7번 `DAILY_POSITION_SIGNAL`: 완료(본 섹션 §2 — positions 0 정상 skip)
2. safe subset 보류 / 후속 항목(주문 / 체결 / sync / execution 계열):
   1) 8번 `DAILY_BUY_EXECUTION` ~ 11번 `DAILY_AUTO_BUY`: 미진행 / dry-run skip 예정 — `--execute` 주문 전송 계열 실행 금지(R-AUTO-009 / R-AUTO-010 / OD-SAFE-002 정합)
   2) 12번 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`: 미진행 / dry-run skip 예정 — paper 운영 환경 활성화 보류(R-AUTO-011 정합)
   3) 13번 `CONNECTOR_ORDER_CHECK` ~ 16번 `SYNC_BUY_POSITION`: 미진행 / dry-run skip 예정 — fill · position sync 자동 재시도 금지(OD-SAFE-004 정합)
   4) 17번 `BALANCE_REFRESH`: 미진행
3. View AWS 실행 매핑표 작성 / safe step 우선 연결 / Execution 계열 dry-run 가능 여부 별도 판단: 후속 분리(05 spec / 04 spec 후속 phase 책임).

### 4. 1차 검증 완료 기준

1. DAILY_BUY_SIGNAL ECS / Fargate 단건 실행: 완료(§1 — exitCode 0 / `decision.strategy_daily_signal` row 4건 생성 / signal_date `2026-06-16`)
2. DAILY_POSITION_SIGNAL ECS / Fargate 단건 실행: 완료(§2 — exitCode 0 / positions 0 정상 skip / `decision.strategy_daily_position_decision` 신규 row 미생성)
3. data_date `2026-06-15` 기반 입력으로 stale data 위험 해소(R-DATA-009 / R-DATA-010 mitigation 1차 실증)
4. 실제 주문 전송 / `--execute` / fill · position sync 자동 재시도 / SELL position 변경 0건 — OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 ~ R-AUTO-011 정합
5. aws-live 작업 0건 — 본 일자는 `aws-paper` 한정
6. 판단: Strategy Decision safe step ECS dry-run 재검증 완료. 후속은 주문 / 체결 / sync 계열의 안전 기준에 따른 별도 승인 + 평일 또는 안전 테스트 데이터 환경에서 진행.

### 5. 후속 인계

1. View Daily Batch 의 `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL` step ProcessBuilder → ECS RunTask 호출 매핑 — 05 spec 후속 phase 책임
2. Step Functions state machine 정의(buy-signal → position-signal 순서 강제 + 자동 재시도 금지 정책 OD-SAFE-004 반영) + EventBridge Scheduler 정기 트리거 — 04 spec 후속 phase 책임
3. 주문 / 체결 / sync 계열(`DAILY_BUY_EXECUTION` ~ `BALANCE_REFRESH`) 의 paper 운영 환경 활성화: 별도 운영자 승인 + 평일 / 안전 테스트 데이터 환경에서 검증 후 진행(R-AUTO-009 / R-AUTO-010 / R-AUTO-011 / OD-SAFE-002 / OD-SAFE-003 정합)
4. Execution 계열 dry-run 가능 여부 별도 판단(`READY -> REQUESTED -> SUBMITTED` end-to-end 검증 진입 전 안전 가드 점검) — 04 spec 후속 phase
5. aws-live cutover — 10 spec 책임

### 6. Task 완료 처리 (본 spec)

본 spec 은 별도 tasks.md 가 없으므로 task 단위 완료 처리는 본 섹션에서 직접 기록한다.

1. DAILY_BUY_SIGNAL 단건 ECS / Fargate 실행 검증: 완료(§1 정합 / 2026-06-13 §7 후속으로 본 일자 재실행 통과)
2. DAILY_POSITION_SIGNAL 단건 ECS / Fargate 실행 검증: 완료(§2 정합 / positions 0 정상 skip 으로 정상 운영 케이스 1차 실증)
3. Strategy Decision safe step Backend dry-run 재검증: 완료(§3 / safe subset 1 ~ 7번 모두 완료)
4. 주문 / 체결 / sync / execution 단계: 미실행 또는 후속 보류(§3 / §5)
5. View Daily Batch 의 `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL` 매핑: 후속(§5 / 05 spec)
6. Step Functions + EventBridge Scheduler 정기 트리거: 후속(§5 / 04 spec 후속 phase)

### 7. 안전 / 보안 점검 결과

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 변경 0건. 운영자가 직접 수정한 파일 0건(본 일자 작업은 ECS RunTask 단건 실행 검증 + DB 결과 조회만 수행).
2. 실제 secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 secret ARN / 실제 IAM Role ARN / image digest full sha256 / IAM access key id / task ARN 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
3. ECS / IAM / Secrets Manager / RDS 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건.
4. CloudWatch Logs 본문 / ECS Task event / SSM 응답 본문 본 노트 평문 인용 0건. 사실(Task Definition family·revision / image tag / lastStatus / exitCode / row count / data_date / signal_date / signal_status) 만 기록.
5. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 직접 호출 0건. 신규 BUY / SELL / 취소 / 정정 / `--execute` 0건. fill / position sync 자동 재시도 0건. SELL position `mark_position_sell_ordered()` 호출 0건.
6. RDS DDL 0건. DML 은 `decision.strategy_daily_run` / `decision.strategy_daily_signal` insert(BUY signal `READY` row 4건) 한정. `decision.strategy_daily_position_decision` 신규 row 0건(positions 0 정상 skip).
7. live 자동 BUY / SELL E2E 검증은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 본 일자는 paper 환경 한정.


## 2026-06-17 Daily AWS 17-step E2E 완료 (Strategy Decision · Strategy Execution)

운영자가 같은 일자 첫 번째 세션(MarketConnector 조회성 dry-run 재검증) 후속으로 직접 수행한 Daily AWS 17-step E2E 흐름이 본 일자에 끝까지 연결됐다. 본 spec 범위에 해당하는 step 은 6번 `DAILY_BUY_SIGNAL` / 7번 `DAILY_POSITION_SIGNAL` / 8번 `DAILY_BUY_EXECUTION` / 9번 `DAILY_SELL_EXECUTION` / 10번 `DAILY_AUTO_SELL` / 11번 `DAILY_AUTO_BUY` / 14번 `SYNC_SELL_FILL` / 15번 `SYNC_BUY_FILL` / 16번 `SYNC_BUY_POSITION` 총 9개 step. 1번 `CONNECTOR_BALANCE` / 12번 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` / 13번 `CONNECTOR_ORDER_CHECK` / 17번 `BALANCE_REFRESH` 4개 step 은 03 spec operation-notes 2026-06-17 §1 ~ §5 정합. 2번 `INTEREST_CRAWLER` / 3번 `PREPROCESSOR` 2개 step 은 08 spec / 4번 `BACKTEST_RESEARCH` / 5번 `BACKTEST_REPORT` 2개 step 은 09 spec operation-notes 2026-06-17 정합. Kiro 는 문서 작성 / 절차 정리만 수행. 실제 ECS RunTask / IAM / RDS / GRANT 작업은 운영자 직접 진행. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건. 실제 broker / KIS 호출은 본 spec 범위에서 0건(주문 제출 책임은 03 spec Step 12) — Strategy Execution `--execute` 는 `READY -> REQUESTED` 상태 전환만 담당(OD-MS-016 정합).

### 1. Step 6 `DAILY_BUY_SIGNAL`

1. RunTask 실행: 완료
   1) Task Definition: `portfolio-paper-strategy-decision-buy-signal:1`
   2) launch type: FARGATE / awsvpc / image tag `paper-20260613`
   3) command override: `python -m port_strategy_decision.daily_buy_signal_run`
2. 실행 결과: 성공
   1) lastStatus: `STOPPED` / stopCode: `EssentialContainerExited` / exitCode: `0`
   2) `decision.strategy_daily_run` 신규 row 생성 / run_date `2026-06-17` / data_date `2026-06-16`
3. `decision.strategy_daily_signal` BUY READY 결과: 확인
   1) row_count: 4건
   2) signal_type: BUY / signal_status: READY
   3) 후보 4종:
      - `282330` BGF리테일
      - `004990` 롯데지주
      - `003490` 대한항공
      - `088350` 한화생명
   4) rank range: `1 ~ 4`
4. 안전 점검: 완료
   1) BUY signal `READY` 상태 row 만 생성 — 실제 주문 전송은 후속 step(`DAILY_AUTO_BUY` / `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) 책임
   2) `connector_order_request` / `connector_fill` row 변경 0건 / broker · KIS 호출 0건

### 2. Step 7 `DAILY_POSITION_SIGNAL`

1. RunTask 실행: 완료
   1) Task Definition: `portfolio-paper-strategy-decision-position-signal:1`
   2) command override: `python -m port_strategy_decision.daily_position_signal_run`
2. 실행 결과: 성공
   1) lastStatus: `STOPPED` / exitCode: `0`
3. position signal 결과: 정상 skip
   1) positions: 0
   2) decision_count: 0
   3) `decision.strategy_daily_position_decision` 신규 row 미생성 — 정상 skip(보유 포지션 0건 / `strategy_position_state` OPEN 0건 시점 / `connector_order_request` 0건 시점)
4. 판단: 정상 운영 케이스(R-AUTO-001 / R-AUTO-002 위반 0건 / 2026-06-16 §2 / §4 정합)

### 3. Step 8 `DAILY_BUY_EXECUTION`

1. 1차 blocker / 운영자 조치: 완료
   1) 1차 실패 원인 = `execution_app` 의 `interest` schema USAGE / table SELECT / sequence / default privileges 누락(R-DATA-005 [2026-06-17 보강] 정합)
   2) 운영자가 직접 GRANT 보정 — `interest` schema USAGE + `interest.*` table SELECT + sequence + default privileges(추후 객체 자동 적용) — 02 spec / 06 spec operation-notes 2026-06-17 사실 기록 정합
   3) 02 spec db-roles-and-grants 정식 매트릭스 갱신은 후속 phase
2. 재실행 결과: 완료
   1) RunTask exitCode 0 / lastStatus STOPPED
   2) `execution.strategy_execution_plan` 신규 row 생성 — `execution_plan_id 92`
   3) BUY READY 4건 생성(execution_order)
   4) `connector_order_request_id` 4건 모두 NULL — Strategy Execution 은 `READY -> REQUESTED` 까지만 담당 / `connector_order_request_id` 매핑은 MarketConnector executor `--execute` 책임(OD-MS-016 정합)
   5) 실제 broker / KIS 주문 호출 0건

### 4. Step 9 `DAILY_SELL_EXECUTION`

1. RunTask 실행: 완료
2. 실행 결과: 정상 skip
   1) sell_decisions: 0
   2) orders_to_upsert: 0
   3) 실제 broker / KIS 주문 호출 0건

### 5. Step 10 `DAILY_AUTO_SELL`

1. RunTask 실행: 완료
2. 실행 결과: 정상 skip
   1) READY SELL 주문: 0건
   2) 실제 broker / KIS 주문 호출 0건
   3) SELL position `mark_position_sell_ordered()` 호출 0건(OD-MS-016 정합 — MarketConnector 측 책임)

### 6. Step 11 `DAILY_AUTO_BUY`

1. RunTask 실행: 완료
   1) command override: `python -m daily_auto_buy_execute_run --execute`
   2) `--execute` guard(영업일 / 환경) 통과
2. 실행 결과: 성공
   1) BUY execution_order 4건 `READY -> REQUESTED` 전환 완료
   2) `execution_plan_id 92`
   3) BUY REQUESTED 4건
   4) total_qty: `378`
   5) total_target_amount: `6908189.40`
   6) `connector_order_request_id` 4건 모두 NULL — `REQUESTED -> SUBMITTED` 전환과 `connector_order_request_id` 매핑은 MarketConnector executor 책임(OD-MS-016 정합 / 03 spec Step 12 책임 경계)
   7) 실제 broker / KIS 주문 호출 0건 — Strategy Execution `--execute` 는 상태 전환만
3. 책임 경계 1차 실증: 완료
   1) Strategy Execution = `READY -> REQUESTED` 상태 전환만 담당
   2) MarketConnector = 실제 KIS paper 주문 제출 + `REQUESTED -> SUBMITTED` 전환(03 spec Step 12)
   3) View Daily Batch 17단계의 11번(`DAILY_AUTO_BUY`) → 12번(`MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) 흐름 1차 실증(R-AUTO-009 / R-AUTO-010 / R-AUTO-011 [2026-06-17 보강] 정합)

### 7. Step 14 `SYNC_SELL_FILL`

1. RunTask 실행: 완료
   1) command override: `python -m execution_sync_sell_fill`
2. 실행 결과: 정상 skip
   1) SELL fill 0건 — 본 일자 SELL 주문 미발생
   2) `execution.strategy_execution_order` SELL row 변경 0건

### 8. Step 15 `SYNC_BUY_FILL`

1. RunTask 실행: 완료
   1) command override: `python -m execution_sync_buy_fill`
2. 실행 결과: 성공
   1) `connector.connector_fill 26 ~ 29` 기준 BUY fill sync 완료(03 spec Step 13 결과 정합)
   2) `execution.strategy_execution_order 26 ~ 29` SUBMITTED → FILLED 전환 완료
   3) sync_result 반영(`filled_qty` / `filled_avg_price` 등) — `connector_fill` 의 broker 응답값 매핑

### 9. Step 16 `SYNC_BUY_POSITION`

1. RunTask 실행: 완료
   1) command override: `python -m execution_sync_buy_position`
2. 실행 결과: 성공
   1) `execution.strategy_position_state` 4건 OPEN 신규 생성
   2) position_state_id 매핑:
      - `282330` BGF리테일: `position_state_id 6`
      - `004990` 롯데지주: `position_state_id 7`
      - `003490` 대한항공: `position_state_id 8`
      - `088350` 한화생명: `position_state_id 9`
   3) BUY fill 기준 OPEN row 생성 — Strategy Execution position 상태 1차 OPEN 전이 완료

### 10. Backend AWS E2E 17-step 본 일자 진행 상태

1. 17 step 모두 완료 또는 정상 skip(OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004 정합):
   1) 1번 `CONNECTOR_BALANCE`: 완료(03 spec §1)
   2) 2번 `INTEREST_CRAWLER`: 완료(08 spec §1)
   3) 3번 `PREPROCESSOR`: 완료(08 spec §2)
   4) 4번 `BACKTEST_RESEARCH`: 완료(09 spec §1)
   5) 5번 `BACKTEST_REPORT`: 완료(09 spec §2)
   6) 6번 `DAILY_BUY_SIGNAL`: 완료(본 섹션 §1)
   7) 7번 `DAILY_POSITION_SIGNAL`: 완료 정상 skip(본 섹션 §2)
   8) 8번 `DAILY_BUY_EXECUTION`: 완료(본 섹션 §3)
   9) 9번 `DAILY_SELL_EXECUTION`: 완료 정상 skip(본 섹션 §4)
   10) 10번 `DAILY_AUTO_SELL`: 완료 정상 skip(본 섹션 §5)
   11) 11번 `DAILY_AUTO_BUY`: 완료(본 섹션 §6)
   12) 12번 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`: 완료(03 spec §2)
   13) 13번 `CONNECTOR_ORDER_CHECK`: 완료(03 spec §3)
   14) 14번 `SYNC_SELL_FILL`: 완료 정상 skip(본 섹션 §7)
   15) 15번 `SYNC_BUY_FILL`: 완료(본 섹션 §8)
   16) 16번 `SYNC_BUY_POSITION`: 완료(본 섹션 §9)
   17) 17번 `BALANCE_REFRESH`: 완료(03 spec §4)
2. ECS RunTask + command override 패턴 1차 검증 완료(OD-MS-013 / OD-MS-017 정합) — 본 일자 검증으로 mitigation 1차 실증.
3. PowerShell AWS CLI `--overrides` 인라인 JSON quoting 문제로 UTF-8 no BOM JSON 파일 + `--overrides file://...` 패턴 사용 필요 — 본 일자에도 재실증(R-AUTO-014 mitigation 정합 / 자세한 내용은 2026-06-13 §3.1 / §4.5 정합).
4. Strategy Execution = 실제 broker 주문 제출이 아닌 execution 후보 / 상태 전이 책임 / 실제 KIS 주문 제출은 MarketConnector Step 12 책임 — 본 일자 17-step E2E 1차 실증으로 책임 경계 명확화(OD-MS-016 / R-AUTO-009 / R-AUTO-010 / R-AUTO-011 [2026-06-17 보강] 정합).

### 11. 후속 인계

1. View Daily Batch 의 9개 step ProcessBuilder → ECS RunTask 호출 매핑 — 05 spec 후속 phase 책임
2. Step Functions state machine 정의(buy-signal → position-signal 순서 강제 + sell-execution / auto-buy / sync 계열의 자동 재시도 금지 정책 OD-SAFE-004 반영) + EventBridge Scheduler 정기 트리거 — 04 spec 후속 phase 책임
3. `execution_app` interest 권한 정식 매트릭스 갱신 — 02 spec db-roles-and-grants 후속 phase
4. heavy 분류(`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) SubmitJob 0건 정책 유지(R-AUTO-015 정합) — 09 spec 후속 phase
5. AWS paper 자동화 orchestrator 후보 정리(View 또는 Step Functions 기반) — 04 / 05 spec 후속 phase
6. aws-live cutover — 10 spec 책임

### 12. 안전 / 보안 점검 결과

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 변경 0건. 운영자가 직접 GRANT 보정한 `execution_app` interest 권한 변경분은 02 spec / 06 spec operation-notes 에 사실로만 기록(본문 전체 인용 0건).
2. 실제 secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 secret ARN / 실제 IAM Role ARN / image digest full sha256 / IAM access key id / task ARN 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
3. ECS / IAM / Secrets Manager / RDS / GRANT 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건. CloudWatch Logs 본문 / ECS Task event / SSM 응답 본문 본 노트 평문 인용 0건. 운영 식별자(execution_plan_id `92` / execution_order id `26 ~ 29` / connector_order_request id `34 ~ 37` / position_state_id `6 ~ 9` / 종목 코드 / 종목명 / 수량 / total_qty `378` / total_target_amount `6908189.40` / data_date `2026-06-16` / signal_date · run_date `2026-06-17`) 만 사실 기록.
4. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 본 spec 범위 직접 호출 0건(주문 제출은 03 spec Step 12 책임). Strategy Execution `--execute` 는 11번(`DAILY_AUTO_BUY`) 한정 / `READY -> REQUESTED` 상태 전환만 / broker / KIS 호출 0건. SELL position `mark_position_sell_ordered()` 호출 0건. fill / position sync 자동 재시도 0건.
5. RDS DDL 0건. DML 은 본 spec 범위에서 `decision.strategy_daily_run` / `decision.strategy_daily_signal` insert / `execution.strategy_execution_plan` insert(`id 92`) / `execution.strategy_execution_order` insert(BUY READY 4건) + update(REQUESTED → SUBMITTED 4건 → FILLED 4건) / `execution.strategy_position_state` insert(OPEN 4건 / `id 6 ~ 9`) 한정. `decision.strategy_daily_position_decision` 신규 row 0건(positions 0 정상 skip).
6. live 자동 BUY / SELL E2E 검증은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건. paper 환경에서의 17-step end-to-end 1차 통과로 R-AUTO-009 / R-AUTO-010 / R-AUTO-011 mitigation 1차 실증 / Status `Mitigated` 갱신 정합.


## 2026-06-18 Daily AWS Paper Wrapper 17단계 실운영 검증 (Strategy Decision · Strategy Execution)

운영자가 2026-06-18 직접 수행한 Daily AWS Paper Wrapper(`.kiro/scripts/run-daily-aws-paper.ps1`) 의 Step 1 ~ Step 17 실 실행 결과 중 04 spec(Strategy Decision / Strategy Execution) 책임 step 인 Step 6 / Step 7 / Step 8 / Step 9 / Step 10 / Step 11 / Step 14 / Step 15 / Step 16 결과를 누적 기록한다. Step 1 / Step 12 / Step 13 / Step 17 = 03 spec / Step 2 / Step 3 = 08 spec / Step 4 / Step 5 = 09 spec operation-notes 의 2026-06-18 섹션 정합. 환경 `aws-paper` / RunDate `2026-06-18` / region `ap-northeast-2`. Kiro 는 문서 작성 / 절차 정리만 수행 / 실제 wrapper 실행 / ECS RunTask / 운영자 직접 patch / Docker rebuild / ECR push / ECS 재실행은 모두 운영자가 직접 진행했다. 04 spec 범위에서 broker / KIS 직접 호출 0건(KIS paper BUY 본 실행은 03 spec Step 12 책임 / 본 spec 의 `--execute` 는 strategy execution 내부 상태 생성·갱신 의미 / OD-MS-016 책임 분리 정합). aws-live 작업 0건.

### 1. Step 6 `DAILY_BUY_SIGNAL` / Step 7 `DAILY_POSITION_SIGNAL` / Step 8 `DAILY_BUY_EXECUTION`

1. Step 6: 완료
   1) wrapper Step 6 호출 → ECS RunTask `portfolio-paper-strategy-decision-buy-signal:1` (image `paper-20260613` / OD-MS-013 정합)
   2) exitCode 0 / lastStatus STOPPED
   3) `decision.strategy_daily_signal` BUY READY 4건 / signal_date `2026-06-18` / data_date `2026-06-17`
   4) 후보 4종 — `004990` 롯데지주 / `023530` 롯데쇼핑 / `003490` 대한항공 / `042660` 한화오션
2. Step 7: 완료
   1) wrapper Step 7 호출 → ECS RunTask `portfolio-paper-strategy-decision-position-signal:1` (image `paper-20260613`)
   2) exitCode 0 / lastStatus STOPPED
   3) 보유 포지션(2026-06-17 17-step E2E 결과로 4종목 OPEN: `282330` / `004990` / `003490` / `088350`) 기준 HOLD decision 정상 생성
   4) `decision.strategy_daily_position_decision` 신규 row 생성 정합
3. Step 8: 완료
   1) wrapper Step 8 호출 → ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python daily_buy_execution_run.py` (OD-MS-017 정합)
   2) exitCode 0 / lastStatus STOPPED
   3) `execution.strategy_execution_plan` 신규 row 생성 / `execution_plan_id 94`
   4) BUY READY 주문 4건 생성 / blocked_order_count 0 / skipped_order_count 0
   5) 추가매수 허용 정책에 따라 기존 보유 종목(`004990` / `003490`)도 BUY 후보로 주문 생성 — OD-MS-024 신규 정합
   6) 주문 수량:
       - `004990` 롯데지주 4주
       - `023530` 롯데쇼핑 8주
       - `003490` 대한항공 6주
       - `042660` 한화오션 11주
4. 안전 점검: 완료
   1) `connector_order_request_id` 4건 모두 NULL — Strategy Execution 은 `READY -> REQUESTED` 까지만 담당 / `connector_order_request_id` 매핑은 MarketConnector executor 책임(OD-MS-016 정합)
   2) broker / KIS 직접 호출 0건

### 2. Step 9 `DAILY_SELL_EXECUTION` / Step 10 `DAILY_AUTO_SELL`

1. Step 9: 완료 정상 skip
   1) wrapper Step 9 호출 → ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python daily_sell_execution_run.py`
   2) exitCode 0 / lastStatus STOPPED
   3) SELL 대상 없음 / sell_decisions 0 / orders_to_upsert 0
   4) broker / KIS 직접 호출 0건
2. Step 10: 완료 정상 skip
   1) wrapper Step 10 호출 → ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python daily_auto_sell_execute_run.py --execute`
   2) exitCode 0 / lastStatus STOPPED
   3) READY SELL 주문 0건 / `--execute` 는 strategy execution 내부 상태 생성·갱신 의미만(OD-MS-016 책임 분리 정합)
   4) broker / KIS 직접 호출 0건

### 3. Step 11 `DAILY_AUTO_BUY` — `READY -> REQUESTED` 4건

1. wrapper Step 11 호출: 완료
   1) ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python daily_auto_buy_execute_run.py --execute`
   2) `--execute` 는 strategy execution 내부 상태 생성·갱신 의미만 / broker · KIS 직접 제출 아님(OD-MS-016 책임 분리 정합)
2. 실행 결과: 완료
   1) exitCode 0 / lastStatus STOPPED
   2) BUY 4건 `READY -> REQUESTED` 전환 — `strategy_execution_order id 30 ~ 33` REQUESTED
   3) `execution_plan_id 94`
   4) `connector_order_request_id` 4건 모두 NULL — Step 12 MarketConnector executor 가 `REQUESTED -> SUBMITTED` 전환 + `connector_order_request_id` 매핑 책임(OD-MS-016 정합 / 03 spec 2026-06-18 §2 정합)
   5) broker / KIS 직접 호출 0건

### 4. Step 14 `SYNC_SELL_FILL` / Step 15 `SYNC_BUY_FILL`

1. Step 14: 완료 정상 skip
   1) wrapper Step 14 호출 → ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python execution_sync_sell_fill.py`
   2) exitCode 0 / lastStatus STOPPED
   3) SELL fill 0건 / SELL 주문 미발생 정합
2. Step 15: 완료
   1) wrapper Step 15 호출 → ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_fill.py`
   2) exitCode 0 / lastStatus STOPPED
   3) `connector.connector_fill` 기준 BUY fill sync — strategy_execution_order id `30 ~ 33` REQUESTED → FILLED 전환(03 spec 2026-06-18 §3 의 단건 체결 동기화 결과 정합)
   4) `result_payload.sync_result` 생성 / 4건 모두 FILLED 정합

### 5. Step 16 `SYNC_BUY_POSITION` — 추가매수 unique constraint 충돌 + merge 패치

본 step 은 본 일자 wrapper 실 실행에서 가장 큰 운영 예외다. 추가매수 허용 정책 정합으로 기존 OPEN position(`004990` / `003490`)이 있는 종목에 대해 신규 INSERT 를 시도하면 `strategy_position_state` `unique(account_id, ticker_code, status='OPEN')` 충돌이 발생한다. 운영자가 직접 patch 로 해결한 결과를 OD-MS-024 신규 / R-DATA-012 신규 mitigation 정합으로 누적 기록한다.

1. 1차 시도: 실패(R-DATA-012 신규 정합)
   1) wrapper Step 16 호출 → ECS RunTask `portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_position.py`
   2) ECS task 실패 — `duplicate key value violates unique constraint` (`strategy_position_state` unique constraint on `account_id` / `ticker_code` / OPEN status)
   3) 영향 종목 — `004990` 롯데지주(기존 `position_state_id 7` OPEN) / `003490` 대한항공(기존 `position_state_id 8` OPEN) — 추가매수 케이스 2건
   4) 신규 OPEN 2건(`023530` / `042660`)은 기존 OPEN row 부재로 unique 위반 0건
2. patch 내용: 완료
   1) `port_strategy_execution/execution_sync_buy_position.py` 운영자 직접 patch
   2) 동일 `account_id` / `ticker_code` / OPEN status 기준 기존 position 조회 로직 추가
   3) 기존 OPEN position 존재 시 신규 INSERT 대신 `merge_open_position_state()` 호출 — quantity 단순 합산 / entry_price 가중평균 / status `OPEN` 유지
   4) `buy_info.additional_buys` 에 추가매수 이력 누적(`execution_order_id` / `connector_order_request_id` / 매수 일자 / 수량 / 단가)
   5) idempotency 체크 — 같은 `execution_order_id` 또는 `connector_order_request_id` 가 이미 `additional_buys` 에 존재하면 다시 누적하지 않음
   6) 기존 OPEN position 부재 시에만 신규 INSERT 진행
   7) patch 변경분은 본 노트 사실 기록만(본문 전체 인용 0건 / R-DOCS-001 정합)
3. Docker build 및 ECR push: 완료
   1) `portfolio-strategy-execution:paper-20260613` 재빌드
   2) Docker build context 주의 — Dockerfile 이 `port_strategy_execution/...` 경로를 COPY 하므로 `C:\Workspaces` 기준으로 build 진행
   3) ECR push 완료(image digest 운영자 보관 / 본 spec 산출물 평문 기록 금지 / R-DOCS-001 정합)
   4) PowerShell pipe 기반 `docker login` 시 400 응답 발생 → `cmd /c` pipe 우회 방법으로 해결(운영자 로컬 PC 환경 운영 메모)
4. 재실행 결과: 성공
   1) wrapper Step 16 재진입 → ECS task exitCode 0 / Step 16 SUCCESS
   2) `position_sync_result` 생성 / 모든 4건 정합 처리
5. 포지션 반영 결과: 확인(OD-MS-024 / R-DATA-012 mitigation 1차 실증)
   1) `004990` 롯데지주 — 기존 `position_state_id 7` 유지 / 65주 → 69주 / entry_price `27008.6956`(가중평균 갱신) / `additional_buys` 에 `execution_order_id 30` 기록
   2) `003490` 대한항공 — 기존 `position_state_id 8` 유지 / 52주 → 58주 / entry_price `28979.3103` / `additional_buys` 에 `execution_order_id 32` 기록
   3) `023530` 롯데쇼핑 — 신규 `position_state_id 11` 생성 / 8주 / entry_price `194225.0000`
   4) `042660` 한화오션 — 신규 `position_state_id 12` 생성 / 11주 / entry_price `126118.1818`
   5) 추가매수 merge 2건 + 신규 OPEN 2건 모두 정합 처리
6. 후속 인계: 후속
   1) patch 정식 commit + 07 spec CI/CD 연동 — followups-overview 2026-06-18 §3
   2) idempotency 회귀 점검 자동화 — 같은 `execution_order_id` 가 두 번 누적되지 않는지 정기 SQL 점검
   3) SELL closing 흐름 / 부분 청산 / 전량 청산 시 `additional_buys` 처리 방식 검증 — followups-overview 2026-06-18 §10

### 6. 안전 / 보안 점검 결과 (Strategy Decision · Strategy Execution)

1. 본 일자 wrapper 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 변경 0건(spec 영역). 운영자가 직접 patch / Docker rebuild / ECR push 한 `port_strategy_execution/execution_sync_buy_position.py` 변경분은 본 노트 §5 에 사실로만 기록(본문 전체 인용 0건 / R-DOCS-001 정합).
2. 실제 secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / account-id / 실제 secret ARN / 실제 IAM Role ARN / image digest full sha256 / IAM access key id / task ARN 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
3. ECS / IAM / Secrets Manager / RDS / Docker / ECR / GRANT 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건. CloudWatch Logs 본문 / ECS Task event / SSM 응답 본문 / Docker build · push 로그 본 노트 평문 인용 0건. 운영 식별자(`execution_plan_id 94` / strategy_execution_order id `30 ~ 33` / connector_order_request_id 4건은 03 spec Step 12 책임 / position_state_id `7` · `8` · `11` · `12` / 종목 코드 / 종목명 / 수량 / entry_price / data_date `2026-06-17` / signal_date · run_date `2026-06-18`) 만 사실 기록.
4. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 본 spec 범위 직접 호출 0건(주문 제출은 03 spec Step 12 책임). Strategy Execution `--execute` 는 Step 10 / Step 11 / Step 14 / Step 15 / Step 16 한정 / 모두 strategy execution 내부 상태 생성·갱신 / broker · KIS 직접 제출 아님(OD-MS-016 책임 분리 정합). SELL position `mark_position_sell_ordered()` 호출 0건. fill / position sync 자동 재시도 0건(운영자 직접 patch 후 재실행 / wrapper 자동 retry 미사용).
5. RDS DDL 0건. DML 은 본 spec 범위에서 `decision.strategy_daily_run` / `decision.strategy_daily_signal` insert / `decision.strategy_daily_position_decision` insert / `execution.strategy_execution_plan` insert(`id 94`) / `execution.strategy_execution_order` insert · update(BUY READY 4건 → REQUESTED → SUBMITTED → FILLED) / `execution.strategy_position_state` insert · merge(추가매수 merge 2건 + 신규 INSERT 2건) 한정. SELL row 변경 0건 / `mark_position_sell_ordered()` 호출 0건.
6. live 자동 BUY / SELL E2E 검증은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건. paper 환경에서의 wrapper 17-step 실 실행 + 추가매수 merge end-to-end 1차 통과로 OD-MS-024 / R-DATA-012 mitigation 1차 실증 / Status `Mitigated` 갱신 정합.


## 2026-06-22 Daily AWS Paper execution steps 6~17 운영 검증

운영자가 2026-06-22 직접 수행한 Daily AWS Paper Wrapper(`.kiro/scripts/run-daily-aws-paper.ps1`) 1 ~ 17 두 번째 실 운영 실행 중 본 spec(Strategy Decision · Strategy Execution) 책임 step 결과를 누적 기록한다. 본 spec 범위에 해당하는 step 은 Step 6 / Step 7 / Step 8 / Step 9 / Step 10 / Step 11 / Step 14 / Step 15 / Step 16 총 9개. Step 1 / Step 12 / Step 13 / Step 17 = 03 spec / Step 2 / Step 3 = 08 spec / Step 4 / Step 5 = 09 spec operation-notes 의 2026-06-22 섹션 정합. 환경 `aws-paper` / RunDate `2026-06-22` / region `ap-northeast-2`. 표현 = "Daily wrapper 기반 수동 orchestration 검증" — Step Functions 자체 구현은 아직 후속 orchestration target 으로 유지. Kiro 는 문서 작성 / 절차 정리만 수행 / 실제 ECS RunTask / IAM / RDS / GRANT 작업은 운영자 직접 진행. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건. 실제 broker / KIS 호출은 본 spec 범위에서 0건(주문 제출 책임은 03 spec Step 12 / KIS paper SELL 1건은 03 spec 책임 / 본 spec 의 `--execute` 는 strategy execution 내부 상태 생성·갱신 의미 / OD-MS-016 정합).

### 1. Step 6 ~ Step 8 (Daily Decision · Buy Execution Plan)

1. Step 6 `DAILY_BUY_SIGNAL`: 완료
   1) ECS RunTask `portfolio-paper-strategy-decision-buy-signal:1` exitCode 0
   2) `decision.strategy_daily_signal` 신규 row 정합
2. Step 7 `DAILY_POSITION_SIGNAL`: 완료
   1) ECS RunTask `portfolio-paper-strategy-decision-position-signal:1` exitCode 0
   2) 본 일자 보유 6종목(`088350` 포함) 의 position decision 생성 — `088350` 한화생명 SELL_HARD_STOP 결정 포함
3. Step 8 `DAILY_BUY_EXECUTION`: 완료
   1) ECS RunTask(`portfolio-paper-strategy-execution:1` + command override `python daily_buy_execution_run.py`) exitCode 0
   2) `execution.strategy_execution_plan id 96` 생성 — `plan_date 2026-06-22` / `strategy_name strategy_ai` / `market_signal DEFENSIVE` / `risk_regime DEFENSIVE` / `plan_status PARTIALLY_BLOCKED` / total_candidate 3 / ready 1 / blocked 2 / skipped 0 / total_target_amount `1,237,080` / available_cash `-62,763` / max_order_amount `-31,381.50`
   3) BUY 2건 BLOCKED — 현금 부족 사유 정합 / 안전 기준 위반 0건(OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004 정합)

### 2. Step 9 `DAILY_SELL_EXECUTION` — 1차 권한 누락 실패 + GRANT 보정

1. 1차 실패 원인 식별: 확인
   1) ECS RunTask 실패 — `permission denied for table strategy_daily_position_decision` 패턴(또는 동등 권한 오류)
   2) `execution_app` 은 기존에 `decision.strategy_daily_position_decision` SELECT 만 보유 / UPDATE 미부여
   3) Step 9 흐름 — SELL execution_order 생성 후 daily position decision row 의 `execution_order_id` 를 UPDATE 해 SELL 결정과 execution order 를 link 해야 함 → UPDATE 권한 필수(R-DATA-005 [2026-06-22 보강] 정합 / R-DATA-013 신규)
2. 운영자 직접 GRANT 보정: 완료(02 spec / 06 spec operation-notes 후속 갱신 대상)
   1) `GRANT USAGE ON SCHEMA decision TO execution_app` 수행
   2) `GRANT UPDATE ON TABLE decision.strategy_daily_position_decision TO execution_app` 수행
   3) 권한 확인 — `execution_app` 에 SELECT + UPDATE 두 권한 확인
3. Step 9 재실행: 완료
   1) ECS RunTask exitCode 0
   2) `execution.strategy_execution_order id 37`(SELL `088350` 244주 MARKET) 생성 / `execution_status READY`
   3) `decision.strategy_daily_position_decision` row 의 `execution_order_id` UPDATE 정합

### 3. Step 10 / Step 11 (Daily Auto SELL / BUY)

1. Step 10 `DAILY_AUTO_SELL`: 완료
   1) ECS RunTask(`portfolio-paper-strategy-execution:1` + command override `python daily_auto_sell_execute_run.py --execute`) exitCode 0
   2) `execution_order id 37` 상태 `READY -> REQUESTED` 전환(OD-MS-016 책임 분리 정합 / broker · KIS 직접 호출 0건)
2. Step 11 `DAILY_AUTO_BUY`: 완료
   1) ECS RunTask(`portfolio-paper-strategy-execution:1` + command override `python daily_auto_buy_execute_run.py --execute`) exitCode 0
   2) BUY 2건은 Step 8 에서 이미 BLOCKED 상태 / 본 step 에서 신규 `READY -> REQUESTED` 전환 대상 0건
   3) `connector_order_request_id` 4건 모두 NULL — broker · KIS 직접 호출 0건(03 spec Step 12 책임)

### 4. Step 12 ~ Step 17 (03 spec Step 12·13·17 / 04 spec Step 14·15·16)

1. Step 12 ~ Step 13: 03 spec operation-notes 2026-06-22 §2 / §3 정합. 본 spec 범위 밖.
2. Step 14 `SYNC_SELL_FILL`: 완료
   1) ECS RunTask(`portfolio-paper-strategy-execution:1` + command override `python execution_sync_sell_fill.py`) exitCode 0
   2) `connector_fill id 34` 기준 SELL fill sync — `execution_order id 37` execution_status `REQUESTED -> SUBMITTED -> FILLED` 전환 정합
3. Step 15 `SYNC_BUY_FILL`: 완료
   1) ECS RunTask exitCode 0
   2) BUY fill 0건(본 일자 BUY 주문 0건) — `[NO_TARGET]` 정상 처리
4. Step 16 `SYNC_BUY_POSITION`: 완료
   1) ECS RunTask(`portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_position.py`) exitCode 0 — 2026-06-18 추가매수 merge 패치(OD-MS-024 정합) 회귀 0건
   2) BUY position 변경 0건 / 본 일자 SELL 청산만 발생 / 별도 sync 책임은 `mark_position_sell_ordered()` + Step 17 BALANCE_REFRESH 책임
5. Step 17 `BALANCE_REFRESH`: 03 spec operation-notes 2026-06-22 §4 정합. `strategy_position_state id 9` 의 remaining_qty 0 / position_status `CLOSED` / latest_sell_reason `SELL_HARD_STOP` 전이는 본 spec 의 SELL fill sync 결과 + MarketConnector executor SELL 성공 시점의 `mark_position_sell_ordered()` 호출 결과의 정합 종착점.

### 5. 결정 / 리스크 변경 요약

1. 신규 결정: 완료
   1) OD-DB-011(`execution_app` 의 `decision.strategy_daily_position_decision` 제한적 UPDATE 권한, 🟢 확정 / 02 spec db-roles-and-grants 정식 매트릭스 갱신 후속 책임)
2. 본문 변경 없는 결정: 1차 실증 메모 보강
   1) OD-MS-016 — Strategy Execution `READY -> REQUESTED`(Step 10) / MarketConnector `REQUESTED -> SUBMITTED`(03 spec Step 12) 책임 분리가 SELL 흐름에서도 1차 실증
   2) OD-MS-021 — 17단계 안전 기준 정합 / Step 9 권한 보정 외 안전 기준 위반 0건
   3) OD-MS-017 — 단일 Task Definition + command override 패턴 회귀 0건(7개 entrypoint 모두 정상 호출)
3. 신규 리스크: 완료
   1) R-DATA-013(`execution_app` 의 `decision` schema UPDATE 권한 누락으로 Step 9 SELL execution link update 실패 위험, Status `Mitigated`)
4. 본문 변경 없는 리스크: 보강 메모
   1) R-DATA-005 — [2026-06-22 보강] `execution_app` 의 `decision` schema USAGE / `decision.strategy_daily_position_decision` UPDATE 누락 사례 추가 / 02 spec db-roles-and-grants 정식 매트릭스 갱신 후속 유지

### 6. 안전 / 보안 점검 결과 (2026-06-22)

1. 본 spec 범위에서 broker / KIS / 신규 주문 호출 0건. `--execute` 는 strategy execution 내부 상태 갱신 의미로만 사용 / 03 spec Step 12 의 broker 호출과 독립. SELL position `mark_position_sell_ordered()` 호출은 MarketConnector executor 책임. fill · position sync 자동 재시도 0건. aws-live 작업 0건.
2. RDS DDL 0건. DML 은 본 일자 17-step 정상 흐름 한정 — `decision.strategy_daily_signal` / `decision.strategy_daily_run` / `decision.strategy_daily_position_decision`(SELL HOLD 결정 + `execution_order_id` UPDATE) / `execution.strategy_execution_plan` insert(`id 96`) / `execution.strategy_execution_order` insert · update(`id 37` READY → REQUESTED → SUBMITTED → FILLED) / `execution.strategy_position_state` update(`id 9` remaining_qty 244 → 0 / position_status OPEN → CLOSED / latest_sell_reason `SELL_HARD_STOP`). GRANT 는 운영자 직접 수행(USAGE ON SCHEMA decision + UPDATE ON decision.strategy_daily_position_decision to execution_app / 02 spec / 06 spec operation-notes 후속 갱신).
3. 실제 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / image digest full sha256 / task ARN / job ARN 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
4. 운영 식별자(`execution_plan_id 96` / execution_order id `37` / connector_order_request id `46` / connector_fill id `34` / position_state_id `9` / 종목 코드 / 종목명 / 수량 · 가격 · 비율 / signal_date · run_date `2026-06-22`) 는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.
5. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역). Step Functions state machine 정의 / EventBridge Scheduler 정기 트리거 / View 측 orchestration 매핑은 모두 후속 orchestration target 으로 유지.

## 2026-06-23 Step Functions approval workflow 실전 검증 + Strategy Execution SELL E2E (Strategy Decision · Strategy Execution + Step Functions orchestration)

운영자가 2026-06-23 직접 수행한 Daily AWS Paper Step Functions state machine `portfolio-paper-daily-step1-17-approval` 실전 검증 + Step 12 ~ Step 17 `allowPaperOrderExecute=true` approval true path 첫 실 SELL E2E 통과 결과 중 본 spec(Strategy Decision · Strategy Execution + Step Functions orchestration) 책임 step 결과를 누적 기록한다. 본 spec 범위에 해당하는 step 은 Step 6 / Step 7 / Step 8 / Step 9 / Step 10 / Step 11 / Step 14 / Step 15 / Step 16 총 9개 + Step Functions state machine orchestration 전체 흐름 측 04 spec 측 정합. Step 1 / Step 12 / Step 13 / Step 17 = 03 spec / Step 2 / Step 3 = 08 spec / Step 4 / Step 5 = 09 spec operation-notes 의 2026-06-23 섹션 정합 — 03 spec operation-notes 2026-06-23 §1 ~ §7 누적은 본 일자 2차 작업 결과로 이미 진행되었고, 08 / 09 spec operation-notes 2026-06-23 누적은 별도 후속 작업 책임으로 분리(Step 2 INTEREST_CRAWLER / Step 3 PREPROCESSOR / Step 4 BACKTEST_RESEARCH / Step 5 BACKTEST_REPORT 는 본 일자 Step Functions approval workflow 실전 검증에서 정상 통과 사실만 본 spec §1 도입 문단에서 단순 인용 / 상세 누적은 책임 spec 으로 분리). 환경 `aws-paper` / RunDate `2026-06-23` / region `ap-northeast-2`. **표현 정정** — Step Functions 자체 구현은 본 일자에 운영자 실증 단계 진입 / 정식 production 자동화 진입은 여전히 후속 phase 책임 / ms-aws-service-decision-matrix 본문의 "Step Functions 후속 orchestration target" 표현은 그대로 유지(2026-06-22 wrapper 기반 첫 실 SELL E2E 회차에 이어 본 일자 = Step Functions approval workflow 기반 첫 실 SELL E2E 회차 / OD-MS-029 신규 정합). Kiro 는 문서 작성 / 절차 정리만 수행 / 실제 Step Functions state machine 정의 · 실행 / ECS RunTask / SSM RunCommand / IAM / RDS / `port-marketconnector/connector_strategy_order_execute.py` patch · 배포 작업은 모두 운영자 직접 진행. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건. 본 spec 범위에서 broker / KIS 직접 호출 0건(KIS paper SELL 1건은 03 spec Step 12 책임 / OD-MS-016 책임 분리 정합).

### 1. Step Functions state machine `portfolio-paper-daily-step1-17-approval` orchestration 실전 검증

1. state machine 운영 정책(OD-MS-029 신규 정합): 확인
   1) 운영자 직접 정의 / 본 노트는 사실 식별자 + 운영 절차 정합만 기록 / state machine 정의 본문 / Amazon States Language(ASL) 전체 인용 0건(R-DOCS-001 정합)
   2) **단일 state machine + 입력 파라미터 분기** — false path 와 true path 가 별도 state machine 으로 분리되지 않음 / 동일 `portfolio-paper-daily-step1-17-approval` 의 입력 파라미터(`allowPaperOrderExecute`) 와 시작 step(`StartStep` / `EndStep` 대응 input)으로만 분기
   3) approval gate = `allowPaperOrderExecute` 입력값. true path 진입은 운영자가 false path 결과 점검 후에만 수행(R-AUTO-019 mitigation Step Functions 측 정합 / Step 12 stdout 의 `PaperOrder: True` 라벨 사후 검증)
2. false path 사전 검증: 통과
   1) `allowPaperOrderExecute=false` / Step 1 ~ Step 17 approval blocked path
   2) Step 10 / Step 11 / Step 12 입력값(READY / REQUESTED 후보 / 종목 / 수량 / 사유) paper 환경 dry-run 확인
   3) Step 12 진입 전 broker · KIS 직접 호출 0건 차단 / 04 spec 측 `execution.strategy_execution_order` 의 `READY -> REQUESTED` 전환은 정상 진행(Step 10 / Step 11 측 `--execute` 는 strategy execution 내부 상태 갱신 의미 / OD-MS-016 책임 분리 정합 / broker 호출 0건)
   4) 운영자 결과 점검 — 본 일자 Step 7 DAILY_POSITION_SIGNAL 의 BGF리테일 `282330` SELL_HARD_STOP 결정 / Step 9 DAILY_SELL_EXECUTION 의 `execution.strategy_execution_order id 40` 생성 / 04 spec 측 흐름 정합 사전 검증 통과
3. true path 승인 실행: 통과
   1) `allowPaperOrderExecute=true` / Step 12 ~ Step 17 approval true path
   2) Step 12 KIS paper SELL 1건 제출 = 03 spec 2026-06-23 §2 정합(`connector.connector_order_request id 48` / `broker_order_no 0000006143` / 04 spec 측 `execution.strategy_execution_order id 40` SUBMITTED → FILLED)
   3) Step 14 SYNC_SELL_FILL / Step 17 BALANCE_REFRESH 정상 통과 — 본 spec §5 / 03 spec 2026-06-23 §4 정합
4. Step Functions 측 ECS RunTask / SSM RunCommand / AWS Batch 호출 정합: 확인
   1) 본 spec 책임 step(6 / 7 / 8 / 9 / 10 / 11 / 14 / 15 / 16) 의 ECS RunTask Task Definition 은 wrapper 기반(2026-06-17 / 2026-06-18 / 2026-06-22) 과 동일 — `portfolio-paper-strategy-decision-buy-signal:1` / `portfolio-paper-strategy-decision-position-signal:1` / `portfolio-paper-strategy-execution:1` + command override(OD-MS-013 / OD-MS-017 정합 / 회귀 0건)
   2) 03 spec 책임 step(1 / 12 / 13 / 17) 의 SSM RunCommand 는 MarketConnector EC2 대상 / Step Functions 진입 직전 wrapper 공통 MarketConnector env bootstrap 함수 호출 흐름 유지(R-AUTO-021 [2026-06-23 보강] 정합)
   3) 09 spec 책임 step(4 / 5) 의 AWS Batch SubmitJob 은 `portfolio-paper-strategy-research-queue` 대상(OD-MS-019 정합 / 회귀 0건)
   4) Step Functions state 정의가 동일 entrypoint(ECS Task Definition / SSM Document / Batch Job Definition) 를 호출하므로 wrapper 기반 → Step Functions 기반 전환 시 기존 운영 정책(OD-MS-013 / OD-MS-016 / OD-MS-017 / OD-MS-019 / OD-MS-021 / OD-MS-023 / OD-MS-024 / OD-MS-025 / OD-MS-026 / OD-MS-027 / OD-MS-028) 본문은 변경 없음

### 2. Step 6 ~ Step 8 (Daily Decision · Buy Execution Plan)

1. Step 6 `DAILY_BUY_SIGNAL`: 완료
   1) ECS RunTask `portfolio-paper-strategy-decision-buy-signal:1` exitCode 0
   2) `decision.strategy_daily_signal` 신규 row 정합 / 본 일자 BUY READY 후보 0건 또는 BLOCKED(현금 상황 정합) — 정확한 후보 수 / 종목 코드는 _common 메타에서 명시되지 않음 / Step 11 DAILY_AUTO_BUY 의 신규 `READY -> REQUESTED` 전환 대상 0건 으로 정합 확인(후속 spec 확인 시점에 본 §의 BUY 후보 수 사실 보강)
2. Step 7 `DAILY_POSITION_SIGNAL`: 완료
   1) ECS RunTask `portfolio-paper-strategy-decision-position-signal:1` exitCode 0
   2) 본 일자 보유 5종목(2026-06-22 §4 잔고 5종목 정합 / `003490` / `004990` / `023530` / `042660` / `282330`)의 position decision 생성
   3) `282330` BGF리테일 SELL_HARD_STOP 결정 포함 — 정확한 entry_date / entry_price / current_price / expected_pnl_rate / hard_stop_loss_rate / holding_days / snapshot_qty / sellable_qty / remaining_qty 17 / expected_pnl_amount 는 _common 메타에서 명시되지 않음 / 후속 spec 확인 시점에 본 §의 SELL_HARD_STOP 결정 메타데이터 보강
3. Step 8 `DAILY_BUY_EXECUTION`: 완료
   1) ECS RunTask(`portfolio-paper-strategy-execution:1` + command override `python daily_buy_execution_run.py`) exitCode 0
   2) `execution.strategy_execution_plan` 신규 row insert 정합 — 본 일자 plan id 는 _common 메타에서 명시되지 않음 / 2026-06-22 plan id `96` 의 후속 id 로 추정(후속 spec 확인 시점에 본 §의 plan id 사실 보강)
   3) 안전 기준 위반 0건(OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004 정합) / BUY 후보 0건 또는 BLOCKED 정합 / 중복 plan 생성 0건

### 3. Step 9 `DAILY_SELL_EXECUTION` — execution_order id 40 생성 (R-DATA-013 mitigation 회귀 0건)

1. 권한 회귀 점검: 통과
   1) 2026-06-22 §2 의 `execution_app` GRANT 보정(`USAGE ON SCHEMA decision` + `UPDATE ON decision.strategy_daily_position_decision`) 본 일자에도 그대로 유지 / R-DATA-013 mitigation 회귀 0건
   2) `permission denied for table strategy_daily_position_decision` 패턴 발생 0건
2. Step 9 실행 결과: 완료
   1) ECS RunTask(`portfolio-paper-strategy-execution:1` + command override `python daily_sell_execution_run.py`) exitCode 0
   2) `execution.strategy_execution_order id 40` 신규 생성 — action_type SELL / 종목 코드 `282330` / 종목명 BGF리테일 / 수량 17 / order_method MARKET / `execution_status READY` / `connector_order_request_id IS NULL`(Step 12 진입 전 connector 측 link 미존재 — 03 spec Step 12 책임 / OD-MS-016 책임 분리 정합)
   3) `decision.strategy_daily_position_decision` row 의 `execution_order_id` UPDATE 정합 — SELL 결정과 execution_order id 40 link 통과(R-DATA-013 mitigation 정합 / R-DATA-005 [2026-06-22 보강] 정합)
3. 부수 사실: 확인
   1) Step 9 의 SELL execution_order 생성 + daily_position_decision link UPDATE 흐름은 본 spec `daily_sell_execution_run.py` entrypoint 책임 / 04 spec Task Definition `portfolio-paper-strategy-execution:1` 의 7개 command override 중 1개(OD-MS-017 정합 / 회귀 0건)
   2) 본 spec 측 `execution.strategy_position_state` 의 SELL 청산 상태 변경은 본 step 이 아니라 Step 14 SYNC_SELL_FILL + MarketConnector executor 의 `mark_position_sell_ordered()` 책임(OD-MS-016 정합 / 본 노트 §5 정합)

### 4. Step 10 / Step 11 (Daily Auto SELL / BUY)

1. Step 10 `DAILY_AUTO_SELL`: 완료
   1) ECS RunTask(`portfolio-paper-strategy-execution:1` + command override `python daily_auto_sell_execute_run.py --execute`) exitCode 0
   2) `execution.strategy_execution_order id 40` 상태 `READY -> REQUESTED` 전환(OD-MS-016 책임 분리 정합 / broker · KIS 직접 호출 0건)
   3) `connector_order_request_id` 는 본 step 에서 NULL 유지(MarketConnector 측 Step 12 책임 / OD-MS-016 정합)
   4) `--execute` 는 strategy execution 내부 상태 갱신 의미 / broker 직접 제출 아님(R-AUTO-014 mitigation 정합 / state ↔ command 1:1 매핑 회귀 0건)
2. Step 11 `DAILY_AUTO_BUY`: 완료
   1) ECS RunTask(`portfolio-paper-strategy-execution:1` + command override `python daily_auto_buy_execute_run.py --execute`) exitCode 0
   2) BUY 후보 0건 또는 BLOCKED — 본 step 에서 신규 `READY -> REQUESTED` 전환 대상 0건 / 정합
   3) broker · KIS 직접 호출 0건(03 spec Step 12 책임 / 본 일자 BUY 주문 0건)
3. retry-normalizer 진입 검토(OD-MS-028 정합): 확인
   1) 본 일자 `execution.strategy_execution_order` 의 `execution_status IN (READY, FAILED)` + `connector_order_request_id IS NOT NULL` 조합 row 0건 → 03 spec Step 12 시작부의 retry-normalizer 적용 0건(R-AUTO-022 mitigation 정합 / 04 spec 측 `strategy_execution_order` UPDATE 적용 0건)
   2) Step 12 진입 직전 04 spec 측 `execution.strategy_execution_order id 40` 상태 = REQUESTED + `connector_order_request_id IS NULL` / 기본 필터 정합 / retry-normalizer 우회 없이 정상 broker 제출 진행

### 5. Step 14 / Step 15 / Step 16 (Strategy Execution sync)

1. Step 12 ~ Step 13 / Step 17: 03 spec operation-notes 2026-06-23 §2 / §3 / §4 정합. 본 spec 범위 밖. broker_order_no `0000006143` / `connector.connector_order_request id 48` / Step 17 BALANCE_REFRESH 후 보유 4종목 정합 — 본 spec 측 사실 인용만.
2. Step 14 `SYNC_SELL_FILL`: 완료
   1) ECS RunTask(`portfolio-paper-strategy-execution:1` + command override `python execution_sync_sell_fill.py`) exitCode 0
   2) `connector.connector_fill` row(03 spec 2026-06-23 §3 정합 / SELL side / 종목 `282330` BGF리테일) 기준 SELL fill sync — `execution.strategy_execution_order id 40` 의 execution_status `REQUESTED -> SUBMITTED -> FILLED` 전환 정합
   3) `connector.connector_order_request id 48` 와 `execution.strategy_execution_order id 40` 간 1:1 link 정합 / 중복 sync 0건 / idempotency 회귀 0건
3. Step 15 `SYNC_BUY_FILL`: 완료
   1) ECS RunTask(`portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_fill.py`) exitCode 0
   2) BUY fill 0건(본 일자 BUY 주문 0건) — `[NO_TARGET]` 정상 처리
4. Step 16 `SYNC_BUY_POSITION`: 완료
   1) ECS RunTask(`portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_position.py`) exitCode 0
   2) BUY position 변경 0건 / 본 일자 SELL 청산만 발생 / 추가매수 merge 패치(OD-MS-024 정합) 회귀 0건 / `strategy_position_state` 의 신규 OPEN INSERT 0건 / 추가매수 merge 경로 진입 0건
   3) SELL 청산 측 `execution.strategy_position_state` 의 BGF리테일 OPEN → CLOSED 전이는 MarketConnector executor 의 `mark_position_sell_ordered()` 호출 + Step 17 BALANCE_REFRESH 의 `connector.connector_position_snapshot` 갱신 시점이 정합 종착점(OD-MS-016 책임 분리 정합 / 본 일자 BGF리테일 position_state row 의 OPEN → CLOSED 전이 / remaining_qty 17 → 0 / latest_sell_reason `SELL_HARD_STOP` — 정확한 position_state row id 는 _common 메타에서 명시되지 않음 / 후속 spec 확인 시점에 본 §의 position_state row id 사실 보강)

### 6. Step 12 retry-normalizer 04 spec 측 정합 (OD-MS-028 신규)

1. 본 spec 측 영향 범위: 확인
   1) OD-MS-028 영향 spec = 03 · 04 · 10 / 04 spec 측 영향은 `execution.strategy_execution_order` 의 retry-normalizer UPDATE 적용 가능성
   2) Step Functions Step 12 state 가 `port-marketconnector/connector_strategy_order_execute.py` 를 호출 시 `.venv/bin/python` 사용 정합 — Step Functions 전환 이후에도 동일 entrypoint 가 호출되어 retry-normalizer 가 그대로 동작
   3) 04 spec 측 `execution.strategy_execution_order` 는 retry-normalizer 의 복구 처리(`execution_status = REQUESTED` + `connector_order_request_id = NULL` + `result_payload.retry_normalizer` 메타데이터 저장) 대상 / 본 일자 retry 후보 0건 / 복구 적용 0건 / 04 spec 측 UPDATE 적용 0건
2. 책임 경계 정합: 확인
   1) OD-MS-016 본문 변경 없음 — Strategy Execution(04 spec) `READY -> REQUESTED` / MarketConnector(03 spec) `REQUESTED -> SUBMITTED` 책임 분리는 그대로 유지
   2) retry-normalizer 는 MarketConnector 측 Step 12 시작부 내부 안전 보완 / 04 spec 의 entrypoint(`daily_auto_sell_execute_run.py` / `daily_auto_buy_execute_run.py` / `execution_sync_sell_fill.py` 등) 본문은 본 일자 변경 없음
   3) 04 spec 측 후속 phase 에서 Step Functions state 정의 가 Step 12 외부에서 broker 호출 / `connector.connector_order_request` 생성 흐름을 추가하지 않는 한 retry-normalizer 적용 범위는 그대로 유지(OD-MS-028 본문 위험 메모 정합)
3. 복구 조건 6종 정합 인용: 확인 (03 spec §1 정합 / 본 노트 본문 인용은 사실 매핑 한정)
   1) `execution_mode = PAPER_STRATEGY` + `action_type IN (BUY, SELL)` + `execution_status IN (READY, FAILED)` + `connector_order_request_id IS NOT NULL` + linked `connector_order_request.request_status = REJECTED` + `rejection_code = 40580000` + `broker_order_no IS NULL` + `connector_fill` 없음
   2) 본 일자 04 spec 측 `execution.strategy_execution_order` 인벤토리에서 위 조건 6종 모두 만족 row 0건 / retry-normalizer 진입 0건

### 7. 결정 / 리스크 변경 요약

1. 신규 결정: 완료
   1) OD-MS-028(Step 12 retry-normalizer 내장 정책 / 🟡 잠정 / 영향 spec 03 · 04 · 10 / 본 spec 측 영향은 `execution.strategy_execution_order` UPDATE 적용 범위)
   2) OD-MS-029(Daily AWS Paper Step Functions approval workflow false / true path 운영 절차 / 🟢 확정 / 영향 spec 04 · 10 / 본 spec 측 영향은 Step Functions state machine orchestration 책임 / Step 6 ~ Step 11 / Step 14 ~ Step 16 의 ECS RunTask Task Definition · command override 매핑은 wrapper 기반과 동일)
2. 본문 변경 없는 결정: 1차 실증 메모 보강
   1) OD-MS-009 — Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask 의 Step Functions 측이 본 일자 실증 단계 진입(approval workflow 검증 통과) / EventBridge Scheduler 정기 트리거 / 정식 production 자동화 진입은 후속 phase
   2) OD-MS-013 / OD-MS-017 — Strategy Decision Task Definition 2개 분리 / Strategy Execution 단일 Task Definition + command override 패턴이 Step Functions 측에서도 동일 호출 / 회귀 0건
   3) OD-MS-016 — Strategy Execution / MarketConnector 책임 분리가 SELL 1건 한정으로 Step Functions 측에서도 1차 실증
   4) OD-MS-021 / OD-MS-023 — 17단계 안전 기준 / wrapper 운영 정책 본문 그대로 유지 / Step Functions approval gate 는 wrapper 의 `-AllowPaperOrderExecute` PAPER_ORDER_GATE 와 등가 역할(R-AUTO-019 mitigation Step Functions 측 정합)
   5) OD-MS-024 — `execution_sync_buy_position.py` 추가매수 merge 패치 회귀 0건 / 본 일자 BUY 0건 / merge 경로 진입 0건
   6) OD-DB-011 — `execution_app` 의 `decision.strategy_daily_position_decision` 제한적 UPDATE 권한 유지 / Step 9 회귀 0건
3. 신규 리스크: 완료
   1) R-AUTO-022(장종료 REJECTED · `40580000` 후 자동 재제출 경로 끊김 위험, Status `Mitigated` — Step 12 시작부 retry-normalizer 내장 + 복구 조건 6종 / 본 일자 dry-run 통과 / 04 spec 측 `execution.strategy_execution_order` UPDATE 적용 0건 / 실 retry 후보 발생 첫 회차 검증은 followups-overview 2026-06-23 §3 후속)
4. 본문 변경 없는 리스크: 보강 메모
   1) R-AUTO-021 — [2026-06-23 보강] Step Functions 전환 이후에도 Step 1 / 12 / 13 / 17 wrapper 공통 bootstrap 호출 흐름 유지 필요 / Status `Mitigated` 유지
   2) R-DATA-005 — [2026-06-22 보강] `execution_app` decision schema USAGE / UPDATE 누락 회귀 0건 / 본 일자 권한 정합 / 02 spec db-roles-and-grants 정식 매트릭스 갱신 후속 유지
   3) R-DATA-013 — `execution_app` 의 `decision` schema UPDATE 권한 누락으로 Step 9 SELL execution link update 실패 위험 / Status `Mitigated` 유지 / 본 일자 회귀 0건
   4) R-AUTO-001 / R-AUTO-014 / R-AUTO-015 — 자동 재시도 / command override 오매핑 / Strategy Research heavy job 실수 실행 위험 / 본 일자 모두 mitigation 회귀 0건
   5) R-BROKER-004 — KIS paper API timeout 후 중복 주문 위험 / 본 일자 timeout 발생 0건 / mitigation 회귀 0건

### 8. 안전 / 보안 점검 결과 (2026-06-23)

1. 본 spec 범위에서 broker / KIS / 신규 주문 호출 0건. `--execute` 는 strategy execution 내부 상태 갱신 의미로만 사용 / 03 spec Step 12 의 broker 호출과 독립. SELL position `mark_position_sell_ordered()` 호출은 MarketConnector executor 책임(OD-MS-016 정합). fill · position sync 자동 재시도 0건. aws-live 작업 0건.
2. RDS DDL 0건. DML 은 본 일자 Step Functions approval true path 정상 흐름 한정 — `decision.strategy_daily_signal` insert / `decision.strategy_daily_run` insert · update / `decision.strategy_daily_position_decision` insert(282330 BGF리테일 SELL_HARD_STOP 결정 포함) + `execution_order_id` UPDATE(`id 40` link) / `execution.strategy_execution_plan` insert(본 일자 plan id 는 _common 메타에서 명시되지 않음 / 후속 spec 확인 시점에 사실 보강) / `execution.strategy_execution_order` insert · update(`id 40` READY → REQUESTED → SUBMITTED → FILLED) / `execution.strategy_position_state` update(BGF리테일 OPEN → CLOSED 전이 / remaining_qty 17 → 0 / latest_sell_reason `SELL_HARD_STOP` / position_state row id 는 _common 메타에서 명시되지 않음 / 후속 spec 확인 시점에 사실 보강). GRANT 본 일자 신규 0건(2026-06-22 §2 GRANT 회귀 0건). retry-normalizer 의 `execution.strategy_execution_order` UPDATE 적용 0건(현재 retry 후보 0건 / OD-MS-028 정합).
3. 실제 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id 본문 외 평문 / image digest full sha256 / task ARN / job ARN / Step Functions execution ARN / 운영자 PowerShell stdout 전문 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
4. AWS / Step Functions / ECS / SSM / EC2 / RDS / Secrets Manager / SSM Parameter Store / KIS API / S3 / CloudWatch 호출은 모두 운영자 직접 수행 — Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건. CloudWatch Logs 본문 / Step Functions execution history 본문 / state machine 정의(ASL) 본문 / SSM 응답 본문 / KIS API response body / ECS Task describe 본문 / 운영자 patch 본문 평문 인용 0건.
5. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역). 운영자 직접 patch 한 `port-marketconnector/connector_strategy_order_execute.py` 전체 교체 + Step 12 시작부 retry-normalizer 내장 변경분은 03 spec operation-notes 2026-06-23 §1 에 사실로만 기록(본 spec 본문 전체 인용 0건 / R-DOCS-001 정합 / port-marketconnector 영역). Step Functions state machine 정의 / EventBridge Scheduler 정기 트리거 / View 측 orchestration 매핑은 본 일자 실증 단계 진입 / 정식 production 자동화 진입 / state 정의 본문 정식 누적은 후속 orchestration phase 책임으로 분리.
6. 운영 식별자(Step Functions state machine `portfolio-paper-daily-step1-17-approval` / `execution.strategy_execution_order id 40` / `connector.connector_order_request id 48` / `broker_order_no 0000006143` / 종목 코드 `282330` / 종목명 BGF리테일 / 수량 17 / 매도 방식 MARKET / SELL 사유 `SELL_HARD_STOP` / Step Functions approval gate 라벨 `allowPaperOrderExecute=false` · `allowPaperOrderExecute=true` / wrapper stdout 라벨 `PaperOrder: True` / Task Definition `portfolio-paper-strategy-decision-buy-signal:1` · `portfolio-paper-strategy-decision-position-signal:1` · `portfolio-paper-strategy-execution:1` / signal_date · run_date `2026-06-23`) 는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.


### 9. AWS 공통 Slack notifier 1차 검증 + Step Functions 3종 Slack 수신 검증 (OD-MS-030 / OD-MS-031 신규)

본 § 는 같은 일자(2026-06-23) §1 ~ §8 의 후속이며, 운영자가 직접 수행한 AWS 공통 Slack notifier Lambda 구현 + Step Functions approval workflow 3종 Slack 수신 검증 결과를 04 spec 책임 영역(Step Functions orchestration + 운영 관측성 보조 계층) 한정으로 누적 기록한다. 본 일자 Slack 작업은 Lambda 코드 / IAM Role inline policy / 환경변수 값 / Slack webhook URL / Slack 메시지 본문 / Step Functions Catch state ASL 본문을 본 노트에 평문 인용 0건(R-DOCS-001 정합) — 운영 식별자(Lambda 이름 / Role 이름 / Runtime / 환경변수 key / Slack 이벤트 라벨 / 완료 marker / 봇 이름)만 사실 기록한다.

1. 신규 AWS 공통 Slack notifier (OD-MS-030 신규 / 🟡 잠정): 완료
   1) Lambda 이름: `portfolio-event-notifier`
   2) Runtime: Python 3.12
   3) IAM Role: `portfolio-event-notifier-lambda-role`
   4) 역할 = PORT-STRATEGY-AI 전체 운영 이벤트 공통 Slack 알림 보조 계층 — Step Functions / EventBridge / EC2 SSM / Batch / Lambda 어디서든 호출 가능한 단일 진입점
   5) Slack webhook URL = Lambda 환경변수 `SLACK_WEBHOOK_URL` 로 1차 검증(값 미기록 / R-DOCS-001 정합 / 본 노트 평문 출력 0건)
   6) 운영 안정화 이후 Secrets Manager 또는 SSM Parameter Store(SecureString) 로 이전 예정(R-AUTO-024 신규 / Status `Accepted` / 06 spec 후속)
   7) **본 Lambda 는 본 spec 의 Strategy Execution / Strategy Decision MS 의 주 compute 가 아님** — Lambda 비권고 정책(ms-aws-service-decision-matrix 본문) 그대로 유지 / Slack notifier 는 운영 이벤트 알림 보조 계층으로만 사용
2. Smoke / template 검증: 완료
   1) `hello wook` 수동 invoke 성공 — Lambda 콘솔 / CLI 의 invoke 경로 정합
   2) Slack 수신 확인 — 운영자 Slack 채널에 메시지 정상 도달
   3) Portfolio Daily Bot 메시지 수신 — Slack 봇 이름 정합
   4) 완료 marker: `SLACK_LAMBDA_SMOKE_TEST=SUCCESS`
   5) 공통 메시지 템플릿 6종 1차 구성 — (a) 장 전 잔고 / 보유 종목 상태 / (b) Daily 검증 완료 · 승인 필요 / (c) Daily 실행 성공 / (d) Daily 실행 실패 / (e) 장중 손절 후보 / (f) 장 후 잔고 / 보유 종목 상태
   6) 수익 / 손실 표시 = 🔴 / 🔵 / ⚪ 이모지 적용 / Slack attachment color bar 적용
   7) 완료 marker: `PORTFOLIO_EVENT_NOTIFIER_TEMPLATE_TEST=SUCCESS`
3. Step Functions 3종 Slack 수신 검증 (OD-MS-031 신규 / 🟢 확정 / R-AUTO-023 신규 mitigation 1차 실증): 완료
   1) `APPROVAL_REQUIRED` — Step 1~11 완료 후 approval gate 진입 시 발송. Role 부여 완료 / Step 1~11 완료 후 승인 필요 Slack 수신 확인 / approval gate 진입 시 운영자가 Slack 으로 수동 승인 필요 상태를 인지할 수 있는 흐름 검증 통과(본 § §2 의 false path 사전 검증 + true path 승인 실행 흐름과 정합).
   2) `DAILY_EXECUTION_SUCCESS` — Step 17 완료 후 전체 성공 시 발송. Step 17 완료 후 Daily 성공 Slack 수신 확인 / Step Functions 전체 성공 종료 시 Slack 수신 경로 검증 통과(본 § §1 true path 검증의 Step 17 BALANCE_REFRESH 통과 정합 — 03 spec operation-notes 2026-06-23 §4 정합).
   3) `DAILY_EXECUTION_FAILED` — Step Functions 실행 중 실패 시 발송. 12~17 test-only 실패 Slack 수신 확인 / 1~17 full workflow 실패 Slack ASL 적용 / 1~17 full workflow test-only 실패 Slack 수신 확인 / 운영 실패 케이스에서 `DAILY_EXECUTION_FAILED` 알림이 발송되는 경로를 검증. **실패 검증은 test-only 실패 주입이며 실제 broker 주문 실패를 의도적으로 발생시킨 것이 아님** — 추가 broker 호출 0건 / aws-live 작업 0건 / R-AUTO-001 · R-AUTO-002 · OD-SAFE-001 ~ OD-SAFE-004 정합.
4. EventBridge 자동화 인계 (OD-MS-031 정합): 인계
   1) 7번 EventBridge 자동화는 Slack 3종(`APPROVAL_REQUIRED` + `DAILY_EXECUTION_SUCCESS` + `DAILY_EXECUTION_FAILED`) 만 우선 적용 / EventBridge Scheduler 정기 트리거 cron 시각 / timezone / 휴장일 가드 결정은 04 / 10 spec 후속 phase 책임(OD-MS-009 본문 변경 없음).
   2) 장 전 잔고 / 장 후 잔고 / 장중 손절 알림은 후속 단계로 분리 — 본 일자 6종 템플릿은 1차 구성만 완료 / 실제 발송 흐름은 본 적용 범위 밖.
   3) 1차 목표는 메시지 문구 고도화가 아니라 Step Functions 실행 흐름에서 Slack 수신 여부를 검증하는 것이다 — 본 § §3 결과로 1차 목표 통과.
   4) **6. Slack 구현: 완료 / 7. EventBridge 자동화는 다음 단계** — 본 spec 후속 phase 의 Step Functions state machine 정식 정의 / EventBridge Scheduler 정기 트리거 진입 / Slack 6종 전체 적용 / DLQ · retry · CloudWatch Alarm 도입은 후속 분리.
5. 결정 / 리스크 변경 요약 (본 § §9 한정): 완료
   1) 신규 결정 — OD-MS-030(AWS 공통 Slack notifier Lambda 도입 정책, 🟡 잠정 / 영향 spec 04 · 05 · 10) + OD-MS-031(Step Functions / EventBridge 1차 Slack 연동 범위 3종 한정 정책, 🟢 확정 / 영향 spec 04 · 05 · 10).
   2) 본문 변경 없는 결정 — OD-MS-009(Daily Batch orchestration) / OD-MS-010(infra alarm 채널 = port-view SlackNotificationService 유지 + SNS·Lambda·Slack fan-out 보조) / OD-MS-029(Step Functions approval workflow) 본문 변경 없이 1차 실증 메모 보강.
   3) 신규 리스크 — R-AUTO-023(Step Functions 실패 경로 Slack 누락 위험, Status `Mitigated` — Catch 경로 `DAILY_EXECUTION_FAILED` 발송 + test-only 실패 주입 수신 검증 통과). R-AUTO-024(Slack webhook URL Lambda 환경변수 장기 보관 secret 관리 약화 위험, Status `Accepted` — 운영 안정화 후 Secrets Manager · SSM Parameter Store 이전 진행).
6. 안전 / 보안 점검 (본 § §9 한정): 완료
   1) Slack webhook URL 평문 기록 0건 — Lambda 환경변수 key 이름과 의미만 기록 / value 평문 / Lambda 코드 본문 / Slack 메시지 본문 / Step Functions Catch state ASL 본문 / Slack webhook 응답 본문 평문 인용 0건(R-DOCS-001 정합 / 모두 `[REDACTED]` 또는 placeholder)
   2) secret value 기록 0건 — 본 § §9 의 모든 운영 식별자(Lambda 이름 / Role 이름 / Runtime / 환경변수 key / Slack 이벤트 라벨 / 완료 marker / 봇 이름) 는 secret 가 아닌 사실 식별자
   3) 실패 검증은 test-only 실패 주입이며 실제 broker 주문 실패 유발 0건 — Step Functions Catch state 가 test-only 실패 신호를 받아 `DAILY_EXECUTION_FAILED` Slack 을 발송하는 흐름만 검증 / 실제 broker / KIS 호출 0건 / `--execute` 추가 호출 0건 / aws-live 작업 0건
   4) AWS Lambda / IAM / Step Functions / Slack webhook 호출은 모두 운영자 직접 수행 — Kiro 는 문서 작성 / 절차 정리만. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 § 작업으로 인한 변경 0건(spec 영역 / port-view SlackNotificationService 본문 변경 없음)
   5) 기존 port-view Slack 은 유지 — port-view SlackNotificationService 의 View Daily Batch 수동 실행 결과 알림 책임은 그대로 / 이번 작업에서 제거 · 대체되지 않음 / 향후 공통 notifier 로 이전 가능성만 기록(05 spec 후속 phase 책임)



## 2026-06-29 (2) — port-view 가 Step Functions StartExecution external caller 로 붙는 첫 local 검증

본 일자 운영자가 직접 수행한 port-view 측 commit `e72de6f`(`feat(view): add Step Functions daily batch trigger`) 결과를 04 spec 의 Step Functions state machine 운영 관점에서 누적 기록한다. 본 노트는 port-view 측 코드 본문 / IAM Policy / ASL / 응답 본문 평문 인용 0건(R-DOCS-001 정합).

§1. port-view 가 외부 caller 로 추가됨
 1) 기존 자동 trigger 경로(OD-MS-032)
   (1) EventBridge Scheduler → Dispatcher Lambda → Step Functions `StartExecution`
       - Scheduler `portfolio-paper-daily-step1-11-approval-0800-kst`(`ENABLED`) / 08:00 KST
       - Scheduler `portfolio-paper-daily-step12-17-order-0901-kst`(`DISABLED`) / 09:01 KST 자동 ENABLE 보류(OD-MS-033)
 2) 신규 운영자 수동 trigger 경로(OD-MS-002 / OD-MS-009 / OD-MS-037 메모 보강)
   (1) View `/daily-batch` 화면 AWS Step 1~11 safe trigger 버튼
       - port-view 의 `StepFunctionsDailyBatchExecutionService` 가 AWS SDK v2 Step Functions client 로 `StartExecution` 호출
       - Controller endpoint `POST /daily-batch/aws-stepfunctions/start-range`
       - 성공 시 `executionName` + account-id redaction `executionArn` flash message 표시
       - `aws-stepfunctions` mode 에서는 View 가 Python subprocess · `C:/Workspaces` 로컬 source 직접 실행 없음
   (2) 본 경로는 자동 trigger 가 아닌 **운영자 수동 trigger** 한정 — Daily Batch 자동 trigger 정책(OD-MS-032 / OD-MS-033) 변경 없음.

§2. View input 의 `runDate` 필수 사실
 1) ASL 의 `runDate.$=$.runDate` 참조 state
   (1) `StopCrawlerEc2AfterStep11Success`(OD-MS-034 정합) 가 input 의 `runDate` 를 직접 참조
       - 외부 caller 의 `StartExecution` input 에 `runDate` 가 없으면 `States.Runtime` 발생
       - View 1차 검증에서 식별 후 보완
 2) 보완 정책
   (1) 외부 `StartExecution` caller 는 input JSON 에 `runDate`(Asia/Seoul yyyy-MM-dd) 를 반드시 포함
       - port-view `StepFunctionsDailyBatchExecutionService` 가 Asia/Seoul 기준 `runDate` 를 input 에 자동 포함하도록 보완 완료
       - EventBridge Scheduler + Dispatcher Lambda 경로는 OD-MS-032 정합으로 Lambda 가 KST `runDate` 를 생성해 input 에 포함 / 기존 정책 변경 없음
   (2) ASL 측 후속 검토
       - `runDate` 가 외부 caller input 누락 시 fail-fast 분기 또는 default `runDate` 보강 분기 도입 검토(04 spec 후속 phase 책임 / 본 일자 작업 범위 밖)

§3. Step 1~11 → StopCrawlerEc2AfterStep11Success → SendApprovalRequiredSlack 흐름 검증
 1) end-to-end 통과
   (1) View 운영자 수동 trigger 경유 1차 실증
       - Step 1~11 workflow 실행 통과
       - `StopCrawlerEc2AfterStep11Success` task state(OD-MS-034) `runDate` 보완 후 통과
       - `SendApprovalRequiredSlack` 도달
       - Slack `APPROVAL_REQUIRED` 수신 확인
       - `connector_order_request` 신규 0건 / broker 주문 제출 0건
 2) Step 12~17 정책 유지
   (1) `allowPaperOrderExecute=false` 기준 차단 유지
       - Step 12 approval gate 차단 정합
       - paper-order gate 정책 변경 없음
       - 09:01 schedule 자동 ENABLE 보류 정책(OD-MS-033) 그대로 유지

§4. View 측 안전 gate 정합(외부 caller 책임 분리)
 1) `StepFunctionsDailyBatchExecutionService` 서비스 레벨 안전 gate
   (1) 서비스 레벨 차단 조건
       - `canStartAwsStepfunctions=false`
       - `hasRunningBatch=true`
       - `stateMachineArn` 빈 값
       - `minExecutableStepOrder` · `maxExecutableStepOrder` 범위 밖
       - `allowPaperOrderExecute=false` 상태 Step 12 이상
       - approval 요청은 `paperOrderEnabled=true` 외 모두 차단
   (2) 04 spec 측 정합
       - state machine 자체의 approval gate(`Step12_CheckApproval`) 와 View 측 서비스 레벨 gate 는 별도 계층
       - 둘 다 만족해야 Step 12 이상 broker 호출 진입 / R-AUTO-033 [2026-06-29 보강 (2)] / R-AUTO-034 신규 정합

§5. 결정 / 리스크 매핑
 1) 결정 본문 변경 없음
   (1) OD-MS-009 / OD-MS-029 / OD-MS-031 / OD-MS-032 / OD-MS-033 / OD-MS-034 / OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없이 1차 실증 메모 보강
       - 자세한 결정 변경은 `../_common/operator-decisions.md` Change Log 2026-06-29 (2) 항목 참조
 2) 리스크 매핑
   (1) R-AUTO-033 [2026-06-29 보강 (2)] / R-AUTO-034 신규 — `../_common/risk-register.md` 참조
       - port-view 외부 caller 1차 검증 통과 / Status `Mitigated` 유지
       - Fargate View 권한 과다 + Step 12 gate 우회 위험 / Status `Open` / 06 spec 후속 phase 책임

§6. 후속 (04 spec 후속 phase 책임)
 1) ASL `runDate` 누락 대응 검토
   (1) 외부 caller 의 input `runDate` 누락 시 fail-fast 또는 default 보강 분기 도입 검토
 2) Step 12~17 외부 caller 책임 분리
   (1) Step 12~17 승인형 trigger 가 신규 추가될 경우 View 측 preflight + approval gate + paper-order gate 와 04 spec state machine 의 approval gate 정합 cross-spec audit
 3) Slack `APPROVAL_REQUIRED` summary 0/0 표시 개선(R-AUTO-027 mitigation 확장) 후속 그대로 유지
 4) 09:01 schedule 자동 ENABLE 여부(OD-MS-033 보류 정책) 그대로 유지

§7. 본 일자 사실 기록 범위
 1) Kiro 작업 = 04 spec `operation-notes.md` 본 섹션 누적만 수행
 2) 운영자 직접 commit `e72de6f` 코드 변경분은 port-view MS 영역(04 spec 영역 변경 0건)
 3) AWS / EventBridge Scheduler / Lambda / Step Functions / SSM / EC2 / RDS / S3 / KIS API 호출 본 일자 변경 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 0건
 4) `StartExecution` 응답 본문 / Step Functions execution history 본문 / Slack 메시지 본문 / Lambda 응답 본문 / KIS API response body / Spring Boot application log 전문 평문 인용 0건
 5) 민감정보(secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / 실제 state machine ARN / Slack webhook URL) 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder
 6) 운영 식별자(commit hash `e72de6f` / commit message / Class 이름 `StepFunctionsDailyBatchExecutionService` / Controller endpoint path `/daily-batch/aws-stepfunctions/start-range` / state name 4종(`StopCrawlerEc2AfterStep11Success` · `SendApprovalRequiredSlack` · `Step6ToStep11_Succeeded` · `Step12_CheckApproval`) / Slack 이벤트 라벨 `APPROVAL_REQUIRED` / 에러 라벨 `States.Runtime` / payload 필드 라벨 / Spring profile `aws-paper`) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님


## 2026-06-29 (3) — port-view 가 Step 12~17 approval state machine 의 external caller 로 붙는 두 번째 phase 검증

본 일자 운영자가 직접 수행한 port-view 측 추가 변경(`DailyBatchProperties.java` · `StepFunctionsDailyBatchExecutionService.java` · `application-aws-paper.properties` · `DailyBatchController.java` · `daily_batch.html`) 의 결과를 04 spec 의 Step Functions state machine 운영 관점에서 누적 기록한다. 본 노트는 port-view 측 코드 본문 / IAM Policy / ASL / 응답 본문 / `StartExecution` 입력 JSON 본문 평문 인용 0건(R-DOCS-001 정합).

§1. Step 12~17 approval state machine ARN 분리
 1) 일반 workflow 와 approval workflow 분리: 완료
   (1) 대상 state machine 2종
       - 일반 workflow: `portfolio-paper-daily-step1-17-approval`
       - approval workflow: `portfolio-paper-daily-step12-17-approval`
   (2) View 측 매핑
       - `startSafeRange` = 일반 ARN 사용
       - `startApprovalRange` = approval 전용 ARN 사용
       - approval ARN 비어 있으면 View 서비스 레벨에서 차단
   (3) Fargate Task Role 후속 (06 spec 책임)
       - `states:StartExecution` Resource 패턴은 일반 ARN + approval ARN 2종 모두 한정 부여 필요
       - Resource · Action wildcard 0건 유지 정책 그대로 유지

§2. `Step12_CheckApproval` Choice `BooleanEquals` 조건과 외부 caller payload 정합
 1) payload 타입 정합 1차 실증: 완료
   (1) boolean 필드
       - `allowPaperOrderExecute=true` (boolean JSON)
       - `paperOrderEnabled=true` (boolean JSON)
       - 문자열 `"true"` 로 전달되면 `Step12_CheckApproval` 에서 차단되는 사례 식별
   (2) numeric 필드
       - `fromStepOrder` · `toStepOrder` · `startStep` · `endStep` 는 numeric JSON 으로 전달
       - 문자열 numeric 도 일부 state 에서 비정상 분기 가능성 존재 → numeric 전달 권장
   (3) ASL 측 후속 검토
       - 외부 caller payload 타입 정합을 강제하기 위한 fail-fast 분기 또는 default coercion 분기 도입은 04 spec 후속 phase 책임(본 일자 작업 범위 밖)
       - 외부 caller 가 본 노트 §2.1.1 / §2.1.2 정합으로 payload 를 전달하면 현재 ASL 그대로 동작 정합

§3. Step 12~17 → 13~17 흐름 검증
 1) end-to-end 통과: 완료
   (1) state 진행 정합
       - `Step12_CheckApproval` 통과
       - `Step12_RunMarketConnectorStrategyOrderExecute` 실행
       - `Step12_GetCommandInvocation` 성공
       - Step 13~17 전체 진행
       - `ExecutionSucceeded` 확인
   (2) DB 후검증
       - 운영 marker `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS`
       - 오늘 신규 `connector_order_request` 0건
       - READY / REQUESTED `strategy_execution_order` 0건
       - 신규 broker 주문 제출 없음
       - 최근 `connector_order_request` 는 2026-06-22 ~ 2026-06-24 기존 주문만 표시
   (3) 검증 식별자
       - executionName `port-view-step12-17-step12-17-20260629-194314-ba5edaf8`
       - status `SUCCEEDED`
       - start `2026-06-29T19:43:15.673+09:00`
       - stop `2026-06-29T19:46:06.546+09:00`
       - 본 노트에 Step Functions execution history 본문 / SSM stdout 본문 평문 인용 0건

§4. View 측 안전 gate 정합(외부 caller 책임 분리)
 1) `StepFunctionsDailyBatchExecutionService` 서비스 레벨 안전 gate
   (1) 서비스 레벨 차단 조건(approval 진입 추가)
       - `canStartAwsStepfunctions=false`
       - `hasRunningBatch=true`
       - 일반 `stateMachineArn` 빈 값 또는 approval `stateMachineArn` 빈 값
       - `minExecutableStepOrder` · `maxExecutableStepOrder` 범위 밖
       - `allowPaperOrderExecute=false` 상태 Step 12 이상
       - approval 요청은 `paperOrderEnabled=true` 외 모두 차단
       - `startApprovalRange` 진입 시 approval ARN 없으면 즉시 차단
   (2) 04 spec 측 정합
       - state machine 자체의 approval gate(`Step12_CheckApproval`) 와 View 측 서비스 레벨 gate 는 별도 계층
       - 둘 다 만족해야 Step 12 이상 broker 호출 진입 / R-AUTO-033 [2026-06-29 보강 (3)] / R-AUTO-034 [2026-06-29 보강] 정합

§5. 결정 / 리스크 매핑
 1) 결정 본문 변경 없음
   (1) OD-MS-009 / OD-MS-029 / OD-MS-031 / OD-MS-032 / OD-MS-033 / OD-MS-034 / OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없이 1차 실증 메모 보강
       - 자세한 결정 변경은 `../_common/operator-decisions.md` Change Log 2026-06-29 (3) 항목 참조
 2) 리스크 매핑
   (1) R-AUTO-033 [2026-06-29 보강 (3)] / R-AUTO-034 [2026-06-29 보강] — `../_common/risk-register.md` 참조
       - port-view 외부 caller approval phase 검증 통과 / Status `Mitigated` 유지 / Fargate Task Role 권한 분리는 06 spec 후속 phase 책임

§6. 후속 (04 spec 후속 phase 책임)
 1) ASL payload 타입 정합 강제 검토
   (1) `Step12_CheckApproval` 진입 전 외부 caller payload 의 boolean / numeric 타입 정합을 fail-fast 또는 default coercion 으로 강제하는 분기 도입 검토
 2) approval workflow Step 12~17 외부 caller 책임 분리
   (1) 신규 외부 caller(예: Fargate View / 다른 운영자 도구) 가 추가될 경우 boolean / numeric payload 타입 정합 cross-spec audit
 3) Slack `APPROVAL_REQUIRED` summary 0/0 표시 개선(R-AUTO-027 mitigation 확장) 후속 그대로 유지
 4) 09:01 schedule 자동 ENABLE 여부(OD-MS-033 보류 정책) 그대로 유지
 5) Step Functions execution history Catch state audit + DLQ · retry · CloudWatch Alarm 도입(R-AUTO-023) 그대로 유지

§7. 본 일자 사실 기록 범위
 1) Kiro 작업 = 04 spec `operation-notes.md` 본 섹션 누적만 수행
 2) 운영자 직접 변경분(port-view MS 영역) 은 04 spec 영역 변경 0건
 3) AWS / EventBridge Scheduler / Lambda / Step Functions / SSM / EC2 / RDS / S3 / KIS API 호출 본 일자 신규 변경 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 0건
 4) `StartExecution` 응답 본문 / Step Functions execution history 본문 / Slack 메시지 본문 / Lambda 응답 본문 / KIS API response body / Spring Boot application log 전문 / SSM stdout 본문 / commit diff 본문 / PowerShell wrapper 본체 평문 인용 0건
 5) 민감정보(secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / 실제 state machine ARN / Slack webhook URL / DB password) 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder
 6) 운영 식별자(executionName `port-view-step12-17-step12-17-20260629-194314-ba5edaf8` / 운영 marker `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS` / state machine 이름 2종(`portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval`) / state 이름 4종(`Step12_CheckApproval` · `Step12_RunMarketConnectorStrategyOrderExecute` · `Step12_GetCommandInvocation` · `ExecutionSucceeded`) / Controller endpoint path 2종(`/daily-batch/aws-stepfunctions/start-range` · `/daily-batch/aws-stepfunctions/start-approval-range`) / Spring properties key 7종 / 환경변수 라벨 / payload 필드 라벨 + boolean / numeric 타입 / `requestedBy=VIEW_APPROVAL_BUTTON` 라벨 / Spring profile `aws-paper` / start · stop timestamp) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님


## 2026-06-30 — View 운영 경로 4종 정리 완료 + Daily Batch gate 운영 의도 정합 수정 + DB 검증 쿼리 작성 원칙 추가

본 일자 운영자가 직접 수행한 `DailyBatchController.java` Daily Batch gate 운영 의도 정합 수정 + Local View → AWS Step Functions Step 12~17 승인 실행 2차 실증 통과 결과를 05 spec 의 ECS Fargate 포팅 관점에서 누적 기록한다. 본 노트는 port-view 측 코드 본문 / IAM Policy / ASL / 응답 본문 / `StartExecution` 입력 JSON 본문 / DB 후검증 raw output 전문 평문 인용 0건(R-DOCS-001 정합).

8. View 운영 경로 4종 정리: 완료
 1) Local View 측 운영자 수동 trigger 분리 4종: 완료
   (1) Local View → Local File Step 1 단독 실행: 완료(2026-06-28 Run #46)
   (2) Local View → Local File Step 1~11 실행: 완료(2026-06-28 Run #47)
   (3) Local View → Local File Step 12~17 실행: 완료(2026-06-29 (1) Run #48)
   (4) Local View → AWS Step Functions Step 12~17 승인 실행: 완료(본 일자)
       - executionName `port-view-daily-step12-17-20260630-095111-aae2595c`
       - state machine `portfolio-paper-daily-step12-17-approval`
       - status `SUCCEEDED`
       - start `2026-06-30T09:51:11.903+09:00`
       - stop `2026-06-30T09:54:16.484+09:00`
       - 주문 대상 없음 상태에서 안전 종료
       - DB 후검증 통과(신규 `connector_order_request` 0건 / 신규 broker 주문 0건)
 2) 운영 경로 분리 정합: 완료
   (1) Local File 실행과 AWS Step Functions 실행 분리 동작 정합
       - Local File 실행 gate: `localFileExecutionEnabled`
       - AWS Step Functions 실행 gate: `awsStepfunctionsStartEnabled` / `awsStepfunctionsStepStartEnabled`
       - approval range gate: `paperOrderEnabled=true` + approval ARN set
   (2) Step 12~17 주문성 구간 별도 승인형 state machine + paper-order gate 통과 시에만 실행 정합

9. Daily Batch gate 운영 의도 정합 수정: 완료
 1) `DailyBatchController.java` 수정: 완료
   (1) AWS Step Functions 버튼 활성 조건 수정
       - `fullPipelineExecutionEnabled=true` 상태에서도 AWS Step 12~17 승인 버튼 활성
       - `paperOrderEnabled=true` 상태에서도 AWS Step 1~11 버튼 조건과 충돌 회피
       - Local File 실행 gate 와 AWS Step Functions 실행 gate 분리 유지
       - Step 12~17 은 `paperOrderEnabled=true` + approval range gate 통과 시에만 실행
       - 본 노트 Java 본문 / commit diff 평문 인용 0건(R-DOCS-001 정합)
   (2) 검증
       - mvn compile 성공
       - Local View `aws-paper` profile 재기동 성공
       - 화면 표시 통과(Execution ON / Local File OFF / Full Pipeline ON / Paper Order ON / 허용 범위 `1~17` / AWS Step 12~17 승인 실행 버튼 활성)

10. DB 검증 쿼리 작성 원칙 추가: 미완료 (정식 반영은 후속 phase 책임)
 1) 본 일자 식별된 운영 원칙: 완료 (사실 기록)
   (1) 컬럼명 사전 확인 의무화
       - `information_schema.columns` 로 대상 컬럼 사전 확인 후 SELECT
       - 본 일자 `connector_position_snapshot.balance_snapshot_id` 컬럼 부재 사례 식별
   (2) 확인된 컬럼만 SELECT
       - 관계 컬럼(예: `balance_snapshot_id`) 도 예상 사용 금지
       - 부재 시 `account_no` + `as_of_date` 같은 자연키로 검증
   (3) 결과 노출 패턴
       - 후검증 쿼리는 `DO` / `EXECUTE` 로 결과 숨김 금지
       - 최종 SELECT 결과가 화면에 직접 나오게 작성
   (4) Windows / PowerShell / psql 환경 정합
       - 한글 SQL 은 `psql -c` 직접 실행 대신 UTF-8 No BOM `.sql` 파일 + `psql -f` 패턴 유지
       - SSM multiline command 는 UTF-8 No BOM JSON 파일 + `--parameters file://...` 패턴 유지
 2) 정식 반영 후속 (05 spec 후속 phase 책임)
   (1) `validation-checklist.md` 또는 동등 문서 신규 생성 시 본 원칙 정식 반영
   (2) Fargate cutover 시점 cross-spec audit 항목으로 등록
   (3) DB 후검증 쿼리 모음 정리 시 본 원칙 정합

### 결정 / 리스크 매핑

- OD-MS-002 / OD-MS-009 / OD-MS-037 / OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없이 1차 실증 메모 보강(2026-06-29 (3) Change Log 항목 정합 그대로 유지 / 본 일자 신규 결정 없음 / Decision Summary 카운트 변경 없음).
- R-AUTO-033 [2026-06-30 보강] — Daily Batch gate 운영 의도 정합 + AWS Step Functions Step 12~17 승인 실행 2차 실증 / Status `Mitigated` 유지.
- R-AUTO-034 [2026-06-30 보강] — View 측 4가지 운영 경로 분리 1차 실증 + DB 검증 쿼리 작성 원칙 보강 / Status `Open` 유지 / Fargate Task Role 권한 분리는 06 spec 후속 phase 책임 그대로 유지.

### 본 일자 사실 기록 범위

- 본 일자 Kiro 작업 = 05 spec `operation-notes.md` 본 섹션 누적(8 · 9 · 10 항목)만 수행.
- 운영자 직접 변경분(`DailyBatchController.java` Daily Batch gate 수정) 은 port-view MS 영역으로 cross-service AWS Migration spec 본 일자 작업으로 인한 변경 0건(spec 영역).
- AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 본 일자 변경 0건.
- AWS 리소스 신규 생성 · 수정 · 삭제 본 일자 변경 0건.
- broker / KIS 호출 = 오전 Step 1~11 자동 trigger 한정(`connector_order_request` 신규 0건) + Local View → AWS Step Functions Step 12~17 승인 실행 1건(`SUCCEEDED` / NO_TARGET / broker 주문 제출 0건) + balance refresh 한정 / 추가 BUY · SELL · 취소 · 정정 0건 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건.
- 민감정보(secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN / broker_order_no 원문 / Slack webhook URL / DB password / Administrator password / 실제 state machine ARN) 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.
- 운영 식별자(executionName `port-view-daily-step12-17-20260630-095111-aae2595c` / state machine 이름 `portfolio-paper-daily-step12-17-approval` / Controller class `DailyBatchController` / Daily Batch gate 라벨 6종 / 화면 표시 라벨 / balance snapshot `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505` · `eval_profit=0` · `source_version=connector-intraday-snapshot-refresh-1.0.0` / DB 컬럼명 `balance_snapshot_id`(부재) · `account_no` · `as_of_date` / Run id `#46` · `#47` · `#48` / Spring profile `aws-paper` / start · stop timestamp) 는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.
