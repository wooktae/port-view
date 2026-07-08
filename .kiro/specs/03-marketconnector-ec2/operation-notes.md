# Operation Notes — 03-marketconnector-ec2

03-marketconnector-ec2 진행 결과를 일자별로 누적 기록한다. 사실 / 결과 / 후속 조치만. 1차 적용 환경 `aws-paper`, region `ap-northeast-2`.

원칙

- 실제 secret value, KIS app key / app secret, 계좌번호, RDS endpoint hostname, RDS password, S3 bucket 이름, dump 파일 경로, account-id, 실제 secret ARN, IAM access key id, instance-id, EIP 평문 기록 금지. 모두 `[REDACTED]` 또는 placeholder.
- stdout / stderr 원문 붙여넣기 금지. 결과는 성공 / 실패 / 후속 필요 세 값 중 하나.
- 신규 주문 / 매수 / 매도 / 취소 / 정정 호출 0건.
- 실제 AWS 리소스 생성 / 수정 / 삭제는 운영자가 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만.

## 2026-06-09 RDS restore runner 사용

- 사용 형태: 본 EC2 를 RDS restore runner 로 사용.
- 흐름: local PostgreSQL `pg_dump` → S3 임시 bucket → EC2 → private RDS `pg_restore` (client 18.4).
- 결과:
  - schema 10개 생성 = 성공
  - role 7개 생성 = 성공
  - 핵심 테이블 row count match = 성공
  - `pg_restore` exit code 0 = 성공
- 실제 dump 파일 경로 / S3 bucket 이름 / RDS endpoint hostname / instance-id / EIP / account-id 미기록.
- AWS 리소스 변경: 운영자 직접 수행 (Kiro 미수행).

## 2026-06-10 MarketConnector EC2 운영 전환 검증

- 사용 형태: 동일 EC2 를 RDS restore runner → MarketConnector 정식 운영 EC2 로 전환.
- 입력: 06 spec 1차 적용 결과(Secrets 4건 + SSM Parameter 6건 + Instance Role + read-only Policy).
- 검증 8건:
  - (a) Python 3.9.25 / venv 1개 구성 = 성공
  - (b) `requests` / `flask` / `psycopg2-binary` / `psycopg` / `pandas` 5종 설치 = 성공
  - (c) `marketconnector_app` 기준 RDS 접속 = 성공
  - (d) `connector_balance.py` 실행 = 성공
  - (e) `connector_order_check.py` 실행 = 성공
  - (f) Flask 내부 smoke test (조회성 endpoint) = 성공
  - (g) Secrets Manager / SSM Parameter Store 기반 env 주입 = 성공
  - (h) EC2 Instance Role 기반 Access Key 없이 실행 = 성공
- 신규 주문 / 매수 / 매도 / 취소 / 정정 entrypoint 호출 0건 (`connector_buy.py` / `connector_sell.py` / `connector_cancel.py` / `connector_modify.py` 미실행).
- RDS DDL/DML 0건. 조회성 SELECT 만.
- `secretsmanager:GetSecretValue` 호출은 운영자만 수행. Kiro 자동 검증은 `DescribeSecret` / `DescribeParameters` 수준으로 제한.
- 실제 secret value / 계좌번호 / RDS endpoint / RDS password / account-id / 실제 ARN / IAM access key id / instance-id / EIP 미기록.
- AWS 리소스 변경: 운영자 직접 수행 (Kiro 미수행).

## 2026-06-13 Strategy 주문 실행 executor 추가

운영자가 2026-06-13 직접 수행한 MarketConnector 신규 executor(`connector_strategy_order_execute.py`) 추가 결과를 누적 기록한다. 본 일자에 Kiro 는 문서 작성 / 절차 정리만 수행했고, 실제 코드 작성 / 정적 검증 / `python -m py_compile` 실행은 운영자가 직접 진행했다.

### 1. 신규 executor 추가 배경

1. 책임 분리 결정: 완료
   1) Strategy Execution(`port_strategy_execution`) 의 자동 buy / sell entrypoint 가 그동안 직접 `connector_buy.buy_stock()` / `connector_sell.sell_stock()` 를 호출하는 구조였다.
   2) 책임 분리 후속 결정으로 Strategy Execution 의 `--execute` 의미를 `READY -> REQUESTED` 상태 전환만 담당하도록 좁혔다.
   3) MarketConnector 측에서 `REQUESTED` 상태의 strategy 주문을 broker / KIS API 로 실제 제출하는 신규 executor 가 필요했다.
2. 본 일자 결과: 완료
   1) 신규 executor 추가 — `connector_strategy_order_execute.py`
   2) 기존 검증된 paper 주문 entrypoint 는 변경 없음
   3) Strategy Execution 측 책임 분리 사실은 [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 §2 에 별도 누적

### 2. 변경 / 미변경 파일 인벤토리

1. 신규 추가: 완료
   1) `connector_strategy_order_execute.py`
       - 489 insertions
       - 신규 파일이며 기존 tracked 변경 없음
2. 기존 검증된 entrypoint 미변경 보장: 완료
   1) `connector_buy.py`: 변경 없음
   2) `connector_sell.py`: 변경 없음
   3) `connector_order_common.py`: 변경 없음
   4) `connector_order_check.py`: 변경 없음
   5) `connector_balance.py`: 변경 없음
   6) `db_config.py`: 변경 없음
   7) `config.py`: 변경 없음
   8) `token_manager.py`: 변경 없음

### 3. 신규 executor 동작 정의

1. 대상 주문 조회: 완료
   1) `strategy_execution_order` 의 `status = 'REQUESTED'` + `connector_order_request_id IS NULL` 조건만 대상.
   2) 정렬: SELL 우선, BUY 후순위.
2. 기본 실행 모드 = dry run: 완료
   1) 대상 주문 목록을 stdout 으로만 출력.
   2) KIS / broker API 호출 0건.
   3) DB update 0건.
3. `--execute` 실행 모드: 완료
   1) `PORT_ENVIRONMENT=paper` 와 `PORT_DB_TARGET=aws-paper` guard 통과 시에만 실주문 진입.
   2) `connector_buy` / `connector_sell` 의 `buy_stock()` / `sell_stock()` lazy import 후 호출.
   3) 성공 시 `strategy_execution_order` 를 `SUBMITTED` 로 갱신.
   4) 실패 시 `strategy_execution_order` 를 `FAILED` 로 갱신.
   5) SELL 성공 시 position `SELL_ORDERED` 갱신 helper 호출.

### 4. 정적 검증

1. 컴파일 점검: 완료
   1) `python -m py_compile connector_strategy_order_execute.py` 통과.
2. 인코딩 점검: 완료
   1) UTF-8 한글 literal 정상 표시 확인.
3. dry run 1회 실행 결과: 완료
   1) 출력: `[NO_TARGET] REQUESTED strategy order 없음`
   2) 사유: 본 일자 시점에 `REQUESTED` + `connector_order_request_id IS NULL` 대상 row 가 0건이라 정상 dry run 결과로 판단.

### 5. 본 일자 범위 밖 / 후속 인계

1. 실제 `--execute` 실행: 보류
   1) 본 일자 검증 범위에 포함되지 않음.
   2) 사유: REQUESTED 대상 주문 row 가 0건 + SSM Port Forwarding / AWS Paper RDS 접속 정책 정리 필요.
2. EC2 배포: 후속
   1) 신규 executor 의 EC2 배포는 후속 작업.
   2) 운영자 결정에 따라 zip 또는 tag 형태 후보 산출 후 배포.
3. 평일 또는 안전한 테스트 데이터 검증: 후속
   1) `READY -> REQUESTED -> SUBMITTED` end-to-end dry / integration 검증은 후속 phase.
   2) 주말 가드(WEEKEND) 차단 영향으로 본 일자에는 dry run 단계까지만 가능.

### 6. 안전 / 보안 점검 결과

1. 본 일자 작업 범위에서 KIS / broker API 호출 0건. `--execute` 실호출 0건.
2. RDS DDL/DML 0건. dry run 은 조회성 SELECT 만 사용 가능.
3. 실제 secret value / 계좌번호 / RDS endpoint hostname / RDS password / KIS app key / KIS app secret / token / account-id / 실제 ARN / instance-id / EIP 본 노트 평문 기록 0건.
4. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 본 일자 작업으로 인한 변경 0건. 운영자가 직접 추가한 `connector_strategy_order_execute.py` 는 본 노트에 사실로만 기록하고 본문 전체 인용 0건.

## 2026-06-13 SSM Port Forwarding 표준 경유지 역할 1차 검증

본 EC2 가 Local-to-AWS Paper RDS 접속의 SSM Port Forwarding 표준 경유지로 1차 검증되었음을 누적 기록한다. 자세한 검증 절차 / Runbook 본문은 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook 섹션 참조.

### 1. 본 EC2 의 추가 역할

1. SSM Port Forwarding 표준 경유지: 완료
   1) 결정 락: OD-NET-010 (SSM Port Forwarding 표준 경유지 = `portfolio-paper-marketconnector-ec2`).
   2) instance id: `i-0fce77927b7397b88`
   3) private ip: `10.0.0.181`
   4) state: `running` (본 일자 점검 시점)
   5) SSM Managed Node 상태: ping `Online` / platform `Linux` / agent `3.3.4515.0`
2. 사용 흐름: 완료
   1) Local PC `localhost:15433` → SSM Session Manager tunnel → 본 EC2 → AWS Paper RDS `portfolio-paper-rds:5432`
   2) RDS Public 노출 없이(`PubliclyAccessible = False`) 로컬에서 AWS Paper RDS 접속 가능.
   3) `portfolio-paper-crawler-worker`(instance id `i-0ff768ea639a91355`) 는 KRX GUI / Windows worker 역할로 유지(08 spec 정합) — SSM Port Forwarding 경유지로 사용하지 않는다.

### 2. 03 spec 영향

1. 본 EC2 의 정식 운영 역할: 완료
   1) 본 EC2 는 03 spec design §2 / §3 의 EC2 운영 패턴(MarketConnector 정식 운영 + Instance Role + Secrets Manager / SSM Parameter Store env 주입) 을 그대로 유지한다.
   2) 본 일자에 추가된 역할은 SSM Port Forwarding 경유지뿐이며, EC2 신규 생성 / 인스턴스 타입 변경 / EBS 재생성 / public subnet 변경 / EIP detach 0건(03 design §2.3 정합).
2. SG / IAM 변경 0건: 완료
   1) `sg-marketconnector-ec2` 인바운드 / 아웃바운드 규칙 변경 0건.
   2) Instance Role / Instance Profile 권한 변경 0건. 본 EC2 의 SSM Managed Node 권한은 03 spec §9 의 `AmazonSSMManagedInstanceCore` managed policy 부착 결과 그대로 사용.

### 3. 본 일자 검증 결과 요약

1. SSM Port Forwarding tunnel 오픈: 완료(session id `terraform-vjp3fv3nz73konetcevdzjh9de` / local port 15433 / remote 5432).
2. `portfolio_admin` 접속: 완료(`current_user` = `portfolio_admin`, `inet_server_addr` = `10.0.20.165`, `inet_server_port` = `5432`).
3. `execution_app` 접속: 완료(Strategy Execution 포팅 사전 검증 통과 / search_path = `execution, decision, research, connector, preprocessor, interest, reference, legacy, public`).
4. broker / KIS / 주문 / 체결 entrypoint 호출 0건. RDS DDL/DML 0건. SELECT 조회 한정.

### 4. 본 일자 범위 밖 / 후속 인계

1. SSM Port Forwarding session 자동 keep-alive / reconnect: 후속(R-AUTO-012).
2. psql client 정식 설치 / PATH 등록 (운영자 로컬 PC): 후속(R-AUTO-013).
3. MarketConnector 신규 executor(`connector_strategy_order_execute.py`) EC2 배포 후보 zip / tag 산출: 후속(03 spec 후속 phase 또는 07 spec).
4. 본 EC2 의 systemd unit / startup script 기반 정상 운영 모드 전환: 03 spec 후속 task 또는 별도 phase 책임.

### 5. 안전 / 보안 점검

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 변경 0건. AWS 리소스 변경 0건.
2. password / secret value / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 IAM access key id / 실제 secret ARN / EIP 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
3. instance id / private IP / SSM session id / local port / RDS endpoint hostname 은 운영 식별자로서 사실 기록(사용자 명시 정책 정합 — secret 가 아님).

## 2026-06-13 SSM Port Forwarding 표준 경유지 역할 — psql 18 + pgAdmin4 보강

본 섹션은 같은 일자 앞 섹션(`## 2026-06-13 SSM Port Forwarding 표준 경유지 역할 1차 검증`) §1 ~ §5 의 후속이다. 본 EC2 가 SSM Port Forwarding 표준 경유지 역할을 하는 동안 추가 client 2종(로컬 PostgreSQL 18 `psql.exe` + pgAdmin4) 으로도 AWS Paper RDS 접속이 1차 실증되었음을 누적 기록한다.

자세한 결과는 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 보강(psql 18 client + pgAdmin4 접속 검증) 섹션 참조.

### 1. 본 EC2 의 추가 client 호환 확인

1. 로컬 PostgreSQL 18 psql client: 완료
   1) 직접 경로 실행 — `C:\Program Files\PostgreSQL\18\bin\psql.exe`
   2) client `18.1` / server `18.4` / SSL `TLSv1.3` 정합.
   3) client major 18 / full 18.4 정책(03 spec design §5 / R-DATA-003 mitigation) 정합.
2. pgAdmin4: 완료
   1) `localhost:15433` 등록 — RDS endpoint 직접 등록 금지(OD-NET-011 정합).
   2) 본 EC2 의 SSM Port Forwarding tunnel 가 열려 있을 때만 접속 성립.
   3) tunnel 종료 시 pgAdmin4 연결 즉시 단절(R-AUTO-012 정합).

### 2. 본 EC2 의 운영 영향

1. SG / Instance Role / Instance Profile 권한 변경 0건 — 본 EC2 측 변경 없음.
2. 본 EC2 의 SSM Managed Node 권한은 03 spec §9 의 `AmazonSSMManagedInstanceCore` managed policy 부착 결과 그대로 사용(앞 섹션 §1 ~ §2 정합).
3. SSM Port Forwarding tunnel 위에서 사용되는 client 종류(Python `psycopg2` / psql 18 / pgAdmin4) 확장은 본 EC2 의 책임 영역 밖 — 운영자 로컬 PC 의 client 책임.

### 3. 안전 / 보안 점검

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 변경 0건. AWS 리소스 변경 0건.
2. password / secret value 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder. pgAdmin4 / psql 18 의 비밀번호는 운영자 로컬 PC 환경에서만 사용되며 본 노트 / 콘솔 캡처 / 로그 평문 기록 금지(R-DOCS-001 정합).
3. 본 EC2 의 운영 식별자(앞 섹션 §1 의 instance id / private IP / SSM session id) 그대로 재사용. 추가 운영 식별자 없음.

## 2026-06-17 MarketConnector 조회성 dry-run 재검증

운영자가 2026-06-17 직접 수행한 MarketConnector EC2 조회성 dry-run 재검증 결과를 누적 기록한다. 본 일자에 Kiro 는 문서 작성 / 절차 정리만 수행했고, SSM RunCommand / Secrets Manager / SSM Parameter Store / Instance Role 기반 실행과 RDS 접근은 모두 운영자가 직접 진행했다. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건 / 신규 주문 0건 / `--execute` 0건.

### 1. `CONNECTOR_BALANCE` 재검증

1. 실행 형태: 완료
   1) SSM RunCommand 로 MarketConnector EC2 안의 venv 환경에서 `connector_balance.py` 실행.
   2) 환경변수는 `/tmp/inject-env.sh` 패턴으로 주입(권한 700 / secret 평문 미저장 / 메모리 export 만).
2. 1차 실패 원인: 확인
   1) Secrets Manager 의 `kis-app-key` / `kis-app-secret` / `paper-account` 가 plain string 이 아니라 JSON SecretString.
   2) 1차 시도에서 SecretString 전체(JSON dict 형태)를 그대로 환경변수 값으로 export — 코드가 기대한 plain value 와 형태가 달라 인증 실패.
3. 보정(v5 패턴): 완료
   1) `aws secretsmanager get-secret-value --secret-id <name> --query SecretString --output text` 결과를 JSON parse 후 내부 key 별 value 만 추출.
   2) `APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD` / `BASE_URL` 추출 결과를 호환 key 와 `KIS_*` alias 동시 export(`KIS_APP_KEY` / `KIS_APP_SECRET` / `KIS_PAPER_ACNT` / `KIS_ACNT_PRDT_CD` / `KIS_BASE_URL`).
   3) secret value 자체는 stdout / 로그 / 콘솔 캡처 / 운영자 노트 평문 기록 0건. value length / key presence 만 확인.
4. 결과 확인: 완료
   1) `connector.connector_balance_snapshot` 최신 row 1건 확인.
   2) `as_of_date` `2026-06-17`
   3) `as_of_ts` `2026-06-17 00:46:17 UTC`
   4) `created_at` `2026-06-17 00:46:17 UTC`
   5) `source_api` `inquire-balance`
   6) `source_version` `connector-balance-1.0.0`
   7) 보유종목 0건은 `connector.connector_position_snapshot` 동일 `as_of_date` 기준 정상 0건 처리(legacy holdings 0건 정합).
   8) KIS API 호출은 잔고 조회성 한정. 신규 주문 / 매수 / 매도 / 취소 / 정정 호출 0건.

### 2. `CONNECTOR_ORDER_CHECK` 재검증(MarketConnector 조회계열 선행 검증)

본 단계는 Daily 17단계 중 후반에 위치한 step `CONNECTOR_ORDER_CHECK`(order 13) 를 운영자가 단건 선행 검증한 결과다. 본 일자는 Daily 17단계 전체 흐름이 아니라 MarketConnector 조회계열 선행 검증으로만 실행됐다.

1. 실행 형태: 완료
   1) SSM RunCommand 로 MarketConnector EC2 안의 venv 환경에서 `connector_order_check.py` 실행.
   2) v5 패턴 재사용 — JSON SecretString 내부 key 추출 + `APP_*` / `KIS_*` 동시 export. 1차 실패 없음.
3. KIS API 호출 결과: 완료
   1) `api_name` `inquire-daily-ccld`
   2) `response_status` `200`
   3) `response_code` `0`
   4) `is_success` `true`
   5) `called_at` `2026-06-17 00:51:03 UTC`
   6) 호출은 조회성 한정. 신규 주문 / 매수 / 매도 / 취소 / 정정 호출 0건.
4. row count 인벤토리: 완료
   1) `connector.connector_order_request` 누적 `33`
   2) `connector.connector_order_event` 누적 `18`
   3) `connector.connector_fill` 누적 `13`
   4) 본 실행 시점 신규 `connector_order_event` / `connector_fill` row 0건은 본 일자 신규 주문 / 체결 미발생 정상 판단(KIS 신규 주문 / 체결 0건 정합 / OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004 정합).
5. PowerShell 변수 소실(경미 이슈): 확인
   1) 운영자 측 로컬 PowerShell 변수가 일시 소실되어 명령 재구성을 진행.
   2) AWS / EC2 / RDS / KIS 측 영향 없음 — MarketConnector EC2 안의 실행 결과는 정상.

### 3. 안전 / 보안 점검 결과

1. 본 일자 작업 범위에서 KIS / broker / 신규 주문 호출 0건. `--execute` 실호출 0건. aws-live 작업 0건.
2. RDS DDL/DML 0건. `connector_balance_snapshot` insert 1건 / `connector_position_snapshot` insert 0건(보유종목 0건). 그 외 SELECT 한정.
3. 실제 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / instance-id / EIP / image digest 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
4. JSON SecretString 내부 key parsing 사실은 mapping 사실로만 기록. value 평문 기록 0건. raw SecretString export 금지 정책 1차 실증(R-DOCS-001 정합).
5. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건. Kiro 는 본 spec 문서(design.md §8.2 / §8.2.1 / §8.2.2 / §8.2.3 / §8.3 / runbook.md §2 / §4 / validation-checklist 일부 / 본 노트 / tasks.md / 루트 공통 문서)만 수정했다.


- [`./runbook.md`](./runbook.md) §3 ~ §6 결과 반영 → 운영자 EC2 shell 작업 시점에 본 노트 일자별 누적 갱신.
- [`./validation-checklist.md`](./validation-checklist.md) §2 / §4 / §5 / §6 의 `[운영자 확인 필요]` 항목 점검 후 결과 반영.
- [`./validation-checklist.md`](./validation-checklist.md) §4 운영 가능 후보 entrypoint(`connector_quote_realtime.py` / `connector_quote_closed.py` / `connector_view_service.py`) 검증 → 후속 phase 또는 별도 phase.
- [`./validation-checklist.md`](./validation-checklist.md) §6 `CloudWatchLogsWrite` 권고 옵션(log group 사전 생성 + `CreateLogGroup` 제외) 적용 → 운영자 직접 작업 후 갱신.
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 갱신 후보: OD-SEC-005 / OD-SEC-006 잠정 → 확정 후보, `AmazonSSMManagedInstanceCore` 사용 정책 신규 후보 (운영자 승인 시).
- [`../_common/risk-register.md`](../_common/risk-register.md) 갱신 후보: R-DATA / R-CAP / R-BROKER / R-SEC / R-AUTO 5건 (다음 가용 ID 부여 후 운영자 승인 시).
- [`../_common/followups-overview.md`](../_common/followups-overview.md) 갱신 후보: 03 1차 적용 환경 / 1차 범위 / 범위 밖 / 04 / 05 / 08 / 09 / 10 spec 인계.
- 정상 운영 모드(systemd unit 또는 startup script) 전환 = 본 spec 후속 task 또는 별도 phase 책임. 본 시점에는 임시 검증 단계(shell + 임시 export 스크립트) 유지.

## 2026-06-17 (Daily AWS 17-step E2E 완료)

운영자가 같은 일자 첫 번째 세션(MarketConnector 조회성 dry-run 재검증) 후속으로 직접 수행한 Daily AWS 17-step E2E 흐름이 본 일자에 끝까지 연결됐다.

본 spec 범위에 해당하는 step 은 4개다.

- 1번 `CONNECTOR_BALANCE`(첫 세션 결과 그대로 사용 / 본 노트 앞 섹션 §1 정합)
- 12번 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`
- 13번 `CONNECTOR_ORDER_CHECK`
- 17번 `BALANCE_REFRESH`

나머지 step(2 / 3 / 4 / 5 / 6 / 7 / 8 / 9 / 10 / 11 / 14 / 15 / 16) 은 04 / 08 / 09 spec operation-notes 의 2026-06-17 §1 ~ §6 / §1 ~ §3 누적 정합.

Kiro 는 문서 작성 / 절차 정리만 수행. 실제 SSM RunCommand / KIS API / RDS 작업은 운영자 직접 진행. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건. 실제 broker 호출은 KIS paper BUY 4건만 발생 / SELL · 취소 · 정정 호출 0건.

### 1. Step 1 `CONNECTOR_BALANCE` (첫 세션 결과 사용)

1. 첫 세션 결과 그대로 사용: 완료
   1) 본 노트 앞 섹션 2026-06-17 MarketConnector 조회성 dry-run 재검증 §1 정합.
   2) `connector.connector_balance_snapshot` 최신 row `as_of_date 2026-06-17` / `as_of_ts 2026-06-17 00:46:17 UTC` / `source_api inquire-balance` / `source_version connector-balance-1.0.0` / 보유종목 0건.
   3) v5 env injection 결과(JSON SecretString 내부 key 추출 + `APP_*` 호환 key + `KIS_*` alias 동시 export)를 17-step 전 구간에서 그대로 재사용.

### 2. Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`

1. 1차 시도 / 실패 원인: 확인
   1) `connector_strategy_order_execute.py` MarketConnector EC2 미배포 — 최초 dry-run 시점 EC2 로컬에 신규 executor 가 없음.
   2) EC2 배포 후 system python 진입 시 `psycopg` 부재로 import 실패 — venv python 사용 필요 확인.
   3) `execution` table UPDATE 권한 누락 — `marketconnector_app` 의 OD-DB-008 R-only 정책 정합 / SUBMITTED 전환을 위해 운영자 직접 GRANT 보정 필요.
   4) `source_run_id` fallback 패치 필요 이슈 — `result_payload` 의 `source_daily_signal_id null` 보정 검토 후속(R-AUTO-009 / R-DATA-005 [2026-06-17 보강] 정합).
2. 보정 / 정식 배포: 완료
   1) `connector_strategy_order_execute.py` MarketConnector EC2 venv python 환경에 정식 배포(운영자 직접 작업).
   2) `execution` table UPDATE 권한 GRANT 보정(운영자 직접 작업 / 02 spec operation-notes 2026-06-17 §1 사실 기록 정합).
   3) `source_run_id` fallback 패치 적용(운영자 직접 작업 / 변경분은 본 노트 사실 기록만).
   4) `--execute` guard(`PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper`) 정합으로 paper 환경 한정 진입(R-AUTO-010 [2026-06-17 보강] 정합).
3. KIS paper BUY 4건 제출 결과: 성공
   1) `282330` BGF리테일 — `execution_order_id 26` SUBMITTED / `connector_order_request_id 34` 생성 / `broker_order_no 0000035906`.
   2) `004990` 롯데지주 — `execution_order_id 27` SUBMITTED / `connector_order_request_id 35` 생성 / `broker_order_no 0000035912`.
   3) `003490` 대한항공 — `execution_order_id 28` SUBMITTED / `connector_order_request_id 36` 생성 / `broker_order_no 0000035918`.
   4) `088350` 한화생명 — `execution_order_id 29` SUBMITTED / `connector_order_request_id 37` 생성 / `broker_order_no 0000035932`.
   5) 4건 모두 paper 응답 정상 / SELL / 취소 / 정정 호출 0건 / aws-live 작업 0건(OD-MS-016 / OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004 정합).
   6) Strategy Execution `READY -> REQUESTED` 전환은 04 spec Step 11 `DAILY_AUTO_BUY` 책임 / 본 step 은 `REQUESTED -> SUBMITTED` 전환만 담당(OD-MS-016 책임 분리 1차 실증).
4. 후속 보완 후보: 후속
   1) `connector_strategy_order_execute.py` `result_payload` 내 `source_daily_signal_id null` 보정 검토(OPTIONAL_COLUMNS 또는 fallback 패턴) — tasks.md task 30 후속 phase.
   2) `execution` table UPDATE 권한 정식 매트릭스 갱신은 02 spec db-roles-and-grants 후속 phase.
   3) MarketConnector EC2 단일 파일 수동 배포 재발 방지 / 배포 체크리스트 / patch 배포 절차 — 07 spec 후속 phase.

### 3. Step 13 `CONNECTOR_ORDER_CHECK` (본 실행)

본 step 은 같은 일자 첫 번째 세션의 선행 단건 검증(본 노트 앞 섹션 §2)과 별개의 본 실행이다. 본 실행은 Step 12 KIS paper BUY 4건 제출 직후의 체결 동기화 흐름이다.

1. KIS `inquire-daily-ccld` 호출: 성공
   1) API 호출 자체는 정상 응답.
   2) 1차 응답 형태가 `output1 empty` + `output2 summary only` — `output2` 는 4개 주문 전체 aggregate summary 였음.
2. 1차 fallback 매핑 오염 식별: 확인
   1) 기존 summary fallback 로직이 마지막 주문 id `37` / `088350` 한화생명 에 summary 값을 잘못 매핑.
   2) 잘못 생성된 `connector.connector_order_event` / `connector.connector_fill` row 가 식별됨(R-AUTO-018 신규 정합).
   3) 운영자가 즉시 잘못 생성된 row 삭제 + `connector_order_request` 상태 복구.
3. summary fallback guard 패치: 완료
   1) `connector_order_check.py` summary fallback guard 패치 적용(운영자 직접 작업).
   2) active 주문 후보가 2건 이상이면 summary fallback 으로 event / fill / status 변경 금지.
   3) active 주문 후보가 정확히 1건일 때만 summary fallback 허용.
   4) 패치 변경분은 본 노트 사실 기록만 — 본문 전체 인용 0건(R-AUTO-018 mitigation 1차 실증 / R-DOCS-001 정합).
4. `broker_order_no` 별 단건 조회로 체결 동기화: 성공
   1) 4건 모두 `broker_order_no` 입력 파라미터 기반 KIS 단건 조회로 체결 동기화 진행.
   2) `connector.connector_order_request 34 ~ 37` 모두 FILLED 전환 확인.
   3) `connector.connector_fill 26 ~ 29` 신규 row 생성 확인.
   4) 다건 active 주문 상황에서 마지막 row 만 갱신되는 패턴 0건(R-AUTO-018 mitigation 1차 실증).
5. 후속 보완 후보: 후속
   1) summary fallback guard 테스트 케이스 추가(active 후보 0 / 1 / 2 / 다건 + `output1` / `output2` 입력 형태별) — tasks.md task 29 후속 phase.

### 4. Step 17 `BALANCE_REFRESH`

1. 1차 실패 원인: 확인
   1) `legacy.holdings` search_path / 권한 문제로 SSM RunCommand 결과가 `relation "holdings" does not exist` 패턴.
   2) AWS DB 에서 bare `holdings` 가 `marketconnector_app` 의 search_path 안에서 탐색되지 못함.
   3) `marketconnector_app` 의 `legacy` schema USAGE 미부여(OD-DB-007 정합 — legacy schema 모든 app role 미부여) + `legacy.holdings` DML 미부여 + sequence 미부여 + database search_path 누락 — bare table name 의존 legacy 경로 호출 부적합.
2. 운영자 조치: 완료(02 spec / 06 spec operation-notes 사실 기록 정합 / R-DATA-011 신규 정합)
   1) `marketconnector_app` 의 database search_path 를 `connector, execution, legacy, reference, public` 로 보정.
   2) `legacy` schema USAGE 권한 부여(legacy 운영 데이터 접근 필요한 1건의 예외 GRANT — OD-DB-007 의 후속 재검토 후보).
   3) `legacy.holdings` DML(SELECT / INSERT / UPDATE / DELETE) 권한 부여(legacy 운영 데이터 갱신 최소 권한 한정).
   4) `legacy` schema sequence 권한 부여 + future default privileges 보정.
3. 재실행 결과: 성공
   1) SSM command Status `Success`.
   2) ResponseCode `0`.
   3) StdErr empty.
   4) `connector.connector_position_snapshot` 4종목 최신 row 생성 확인.
4. 최종 보유 스냅샷: 확인
   1) `282330` BGF리테일 — `position_snapshot_id 123` / `quantity 17` / `avg_buy_price 120182.35`.
   2) `004990` 롯데지주 — `position_snapshot_id 121` / `quantity 65` / `avg_buy_price 27043.08`.
   3) `003490` 대한항공 — `position_snapshot_id 120` / `quantity 52` / `avg_buy_price 28980.77`.
   4) `088350` 한화생명 — `position_snapshot_id 122` / `quantity 244` / `avg_buy_price 5744.41`.
5. 후속 보완 후보: 후속
   1) `connector.connector_balance_snapshot` 최신성 검증 SQL 정리 — `created_at` 단독이 아닌 `as_of_ts` / `max(created_at)` 의미 구분(`as_of_ts` 는 broker 응답 기준 시점 / `created_at` 은 row insert 시점) — tasks.md task 31 후속 phase.
   2) `legacy.holdings` 권한 / search_path 보정 정식 문서화 — 02 spec db-roles-and-grants 후속 phase.
   3) legacy schema 가 모든 app role 미부여 정책(OD-DB-007) 의 marketconnector_app 한정 예외 1건 사실은 02 spec / 06 spec operation-notes 에 누적 기록 / 정식 매트릭스 갱신 후속.

### 5. 안전 / 보안 점검 결과 (Daily AWS 17-step E2E)

1. 본 일자 작업 범위에서 broker / KIS 호출은 KIS paper BUY 4건(Step 12) + balance / order check 조회성 호출(Step 1 / Step 13) 한정. SELL / 취소 / 정정 / 추가 `--execute` 호출 0건. SELL position `mark_position_sell_ordered()` 호출 0건(OD-MS-016 정합 — 본 일자 SELL 흐름 미발생). aws-live 작업 0건.
2. RDS DDL 0건. DML 은 본 일자 17-step 정상 흐름 한정.
   - `connector.connector_order_request` insert 4건(`id 34 ~ 37`)
   - `connector.connector_order_event` insert(잘못 생성된 row 삭제 + 정상 row 재생성)
   - `connector.connector_fill` insert 4건(`id 26 ~ 29`)
   - `execution.strategy_execution_order` update(`id 26 ~ 29` SUBMITTED → FILLED)
   - `connector.connector_balance_snapshot` insert 1건
   - `connector.connector_position_snapshot` insert 4건(`id 120 ~ 123`)

   `--execute` 영향 row 4건(KIS paper BUY) 모두 SUBMITTED → FILLED → position OPEN 정합 전이.
3. 아래 민감정보는 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.
   - 실제 secret value / KIS app key / KIS app secret / token
   - 계좌번호
   - RDS password / RDS endpoint hostname
   - account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id
   - instance-id / EIP / image digest full sha256 / task ARN / job ARN
4. KIS paper 4건의 `broker_order_no`(`0000035906` / `0000035912` / `0000035918` / `0000035932`) 는 broker 응답값으로 운영 식별자 — 실계좌 주문번호 아님 / 본 노트 사실 기록 정합(R-DOCS-001 [2026-06-17 보강(17-step E2E)] 정합).
5. 운영자가 직접 패치 / 정식 배포한 `connector_strategy_order_execute.py` / `connector_order_check.py` 변경분은 본 노트에 사실로만 기록 — 본문 전체 인용 0건. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건.
6. AWS / SSM / EC2 / RDS / Secrets Manager / SSM Parameter Store / KIS API 호출은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행.
   - AWS CLI 실행 0건
   - AWS 리소스 생성 / 수정 / 삭제 0건
   - `secretsmanager:GetSecretValue` 결과값 평문 기록 0건
   - CloudWatch Logs 본문 / SSM 응답 본문 / KIS API response body / 운영자 PowerShell stdout 전문 평문 인용 0건
7. 아래 운영 식별자는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.
   - execution_plan_id `92` / execution_order id `26 ~ 29`
   - connector_order_request id `34 ~ 37` / broker_order_no 4종
   - position_state_id `6 ~ 9` / connector_position_snapshot id `120 ~ 123`
   - 종목 코드 `282330` / `004990` / `003490` / `088350` / 종목명 / 수량 / avg_buy_price
   - total_qty `378` / total_target_amount `6908189.40`
   - data_date `2026-06-16` / signal_date · run_date `2026-06-17`

## 2026-06-17 (Daily AWS PowerShell wrapper 구현)

같은 일자 두 번째 세션(Daily AWS 17-step E2E 완료) 후속으로 운영자가 직접 수행한 Daily AWS 17-step 운영자용 Windows PowerShell wrapper 구현 결과를 누적 기록한다.

본 wrapper 작업은 03 spec(MarketConnector EC2) 단일 책임은 아니지만 아래 이유로 03 spec operation-notes 에 cross-cutting 누적 기록 위치로 채택한다.

- (a) Step 12 PAPER_ORDER_GATE 가 03 spec 의 `connector_strategy_order_execute.py --execute` 를 직접 다룬다.
- (b) Step 1 / Step 13 / Step 17 모두 03 spec 의 MarketConnector EC2 SSM RunCommand 흐름과 직결된다.
- 04 / 06 / 08 / 09 spec 은 followups-overview.md 2026-06-17 세 번째 후속 메모로만 참조 / 별도 누적하지 않는다(중복 누적 방지).

본 wrapper 작업 범위 안전 사실:

- 실제 broker / KIS / 신규 BUY · SELL · 취소 · 정정 / `--execute` 주문 제출 0건
- aws-live 작업 0건
- wrapper 기반 전체 1~17 실제 재실행 0건
- Step 12 `-AllowPaperOrderExecute` 사용 0건
- bundled wrapper 미생성

Kiro 는 문서 작성 / 절차 정리만 수행. 실제 wrapper 코드 작성 / parser validation / DryRun / Step 1 SSM 실행 / Step 12 gate 검증은 모두 운영자 직접 진행.

### 1. wrapper 산출물 인벤토리

1. main wrapper: 완료
   1) `.kiro/scripts/run-daily-aws-paper.ps1`
   2) 파라미터 처리 — `RunDate` / `Region` / `Environment` / `StartStep` / `EndStep` / `DryRun` / `AllowPaperOrderExecute`
   3) 환경 입력 = `aws-paper` 만 허용(R-AUTO-002 [2026-06-17 wrapper 보강] mitigation 정합)
   4) Step 12 PAPER_ORDER_GATE 중앙 차단
   5) 전체 실행 결과 summary 생성 — `summary/run-summary.txt`
2. config: 완료
   1) `.kiro/scripts/daily-aws-paper.config.ps1`
   2) region / cluster / instance id / subnet / SG / task definition / job definition / log group / output path 관리
   3) ECS public subnet / Preprocessor TD / Interest Crawler TD / Strategy Decision TD / Strategy Execution TD / AWS Batch job queue / job definition / MarketConnector EC2 instance id / Windows KRX crawler worker instance id 관리
3. functions: 완료
   1) `.kiro/scripts/daily-aws-paper.functions.ps1`
   2) Step registry 관리
   3) SSM 실행 공통 함수(`AWS-RunShellScript` Linux 기본 + `AWS-RunPowerShellScript` Windows 지원 추가) — 실행 로그에 documentName 출력 추가
   4) ECS RunTask 공통 함수(`Invoke-DailyAwsPaperEcsTask` / Python patch script 로 UTF-8 no BOM JSON 생성 / `--overrides file://...` 패턴 / taskArn 수집 / `describe-tasks` polling / `STOPPED` 대기 / container exitCode 확인 / log group · stream prefix 보완 / `describe-log-streams` / `get-log-events` 기반 로그 저장)
   5) AWS Batch SubmitJob 공통 함수(`Invoke-DailyAwsPaperBatchJob`)
       - Python patch script + `aws batch submit-job`
       - jobId 수집 / `describe-jobs` polling / `SUCCEEDED` · `FAILED` 판정
       - Batch CloudWatch log stream 확인 / `aws logs get-log-events` 기반 로그 저장
       - 실패 시 jobId / status / statusReason / exitCode / cloudWatchLog 포함 throw
   6) CloudWatch log 수집 / SSM stdout · stderr 저장 / summary 기록 / 성공·실패·blocker 판정 / PowerShell UTF-8 보정
4. step files 17개: 완료
   1) `.kiro/scripts/steps/step-01-connector-balance.ps1` (MarketConnector EC2 SSM / `connector_balance.py`)
   2) `.kiro/scripts/steps/step-02-interest-crawler.ps1` (non-GUI ECS RunTask + Windows KRX worker `Portfolio-KRX-Worker-Daily` Scheduled Task trigger / worker `running` 아니면 trigger skip)
   3) `.kiro/scripts/steps/step-03-preprocessor.ps1` (`portfolio-paper-interest-preprocessor:1` ECS RunTask)
   4) `.kiro/scripts/steps/step-04-backtest-research.ps1` (`portfolio-paper-strategy-research:5` AWS Batch SubmitJob)
   5) `.kiro/scripts/steps/step-05-backtest-report.ps1` (`portfolio-paper-strategy-report:3` AWS Batch SubmitJob / S3 upload wrapper)
   6) `.kiro/scripts/steps/step-06-daily-buy-signal.ps1` (`portfolio-paper-strategy-decision-buy-signal:1` ECS RunTask)
   7) `.kiro/scripts/steps/step-07-daily-position-signal.ps1` (`portfolio-paper-strategy-decision-position-signal:1` ECS RunTask)
   8) `.kiro/scripts/steps/step-08-daily-buy-execution.ps1` (`portfolio-paper-strategy-execution:1` + command override `python daily_buy_execution_run.py`)
   9) `.kiro/scripts/steps/step-09-daily-sell-execution.ps1` (`portfolio-paper-strategy-execution:1` + command override `python daily_sell_execution_run.py`)
   10) `.kiro/scripts/steps/step-10-daily-auto-sell.ps1` (`portfolio-paper-strategy-execution:1` + command override `python daily_auto_sell_execute_run.py --execute`) — `--execute` 는 strategy execution 내부 상태 생성·갱신 의미 / broker · KIS 직접 제출 아님(OD-MS-016 책임 분리 정합)
   11) `.kiro/scripts/steps/step-11-daily-auto-buy.ps1` (`portfolio-paper-strategy-execution:1` + command override `python daily_auto_buy_execute_run.py --execute`) — `--execute` 는 strategy execution 내부 상태 생성·갱신 의미 / broker · KIS 직접 제출 아님(OD-MS-016 책임 분리 정합)
   12) `.kiro/scripts/steps/step-12-marketconnector-strategy-order-execute.ps1` (MarketConnector EC2 SSM / `connector_strategy_order_execute.py --execute`) — **실제 KIS paper 주문 제출 가능 step / 기본 차단 / `-AllowPaperOrderExecute` 명시 시에만 허용**
   13) `.kiro/scripts/steps/step-13-connector-order-check.ps1` (MarketConnector EC2 SSM / `connector_order_check.py`) — 주문 상태 조회 / DB 상태 갱신 / 신규 주문 제출 없음
   14) `.kiro/scripts/steps/step-14-sync-sell-fill.ps1` (`portfolio-paper-strategy-execution:1` + command override `python execution_sync_sell_fill.py`) — SELL fill / status DB 갱신 / broker · KIS 호출 없음
   15) `.kiro/scripts/steps/step-15-sync-buy-fill.ps1` (`portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_fill.py`) — BUY fill / status DB 갱신 / broker · KIS 호출 없음
   16) `.kiro/scripts/steps/step-16-sync-buy-position.ps1` (`portfolio-paper-strategy-execution:1` + command override `python execution_sync_buy_position.py`) — BUY position DB 갱신 / broker · KIS 호출 없음
   17) `.kiro/scripts/steps/step-17-balance-refresh.ps1` (MarketConnector EC2 SSM / `connector_balance.py`) — balance / position snapshot refresh / 신규 주문 제출 없음
5. bundled wrapper: 미생성
   1) `.kiro/scripts/run-daily-aws-paper-bundled.ps1` 본 일자 미생성 — 후속 선택 작업
   2) 17개 step 별 파일 검증이 충분히 안정화된 시점에 단일 파일 실행이 필요하면 생성
   3) 기본 개발 · 검증 · 운영 기준은 분리 파일 구조 유지(OD-MS-023 정합)

### 2. Step 1 단독 SSM 1차 검증

1. 실행 형태: 완료
   1) `-StartStep 1 -EndStep 1` 단독 실행
   2) MarketConnector EC2 SSM RunCommand 로 venv python 환경에서 `connector_balance.py` 실행
   3) `/tmp/inject-env.sh` v5 env injection 사용(JSON SecretString 내부 key 추출 + `APP_*` 호환 key + `KIS_*` alias 동시 export / R-DOCS-001 정합)
2. 실행 결과: 성공
   1) SSM RunCommand `Status: Success`
   2) `ResponseCode: 0`
   3) stdout / stderr 파일 저장(`C:\Temp\portfolio-daily-aws-paper\<run-id>\logs\` 하위)
   4) `connector.connector_balance_snapshot` 저장 — 03 spec 본문 책임 흐름 정합
   5) `connector.connector_position_snapshot` 삭제 후 재저장 — 03 spec 본문 책임 흐름 정합
   6) `legacy.holdings` 저장 — 03 spec 본문 책임 흐름 정합(R-DATA-011 mitigation 으로 marketconnector_app legacy schema USAGE / DML / search_path 보정 완료 상태 정합)
   7) 신규 주문 / 매수 / 매도 / 취소 / 정정 호출 0건 / broker · KIS API 응답은 잔고 조회성 한정

### 3. Step 12 PAPER_ORDER_GATE 안전 차단 검증

본 검증은 wrapper 의 가장 핵심 안전 점검이다. 실제 KIS paper 주문 제출 가능 step 인 Step 12 가 의도하지 않은 시점에 실행되지 않도록 wrapper 중앙 PAPER_ORDER_GATE + Step 12 내부 이중 gate 가 동시에 차단하는지 1차 검증한다.

1. 실행 형태: 완료
   1) `-StartStep 12 -EndStep 12` 실행 모드(DryRun 아님)
   2) 옵션 입력 — `DryRun: False` / `PaperOrder: False` 기본값
   3) `-AllowPaperOrderExecute` 옵션 명시 0건
2. 1차 차단 — wrapper 중앙 PAPER_ORDER_GATE: 완료
   1) main wrapper 의 step registry 가 Step 12 의 Risk 분류를 `PAPER_ORDER_GATE` 로 식별
   2) `$PaperOrder` 가 false 이므로 wrapper 중앙 gate 에서 즉시 차단 / 차단 메시지 정상 출력
   3) Step 12 의 SSM command 제출 자체 0건
3. 2차 차단 — Step 12 내부 이중 gate: 완료
   1) step 파일 자체에서도 `-AllowPaperOrderExecute` 입력값을 다시 검증
   2) 미명시 시 즉시 종료 / 의도하지 않은 우회 진입 0건
4. 안전 점검 결과: 완료
   1) 실제 SSM command 제출 없음
   2) 실제 KIS 주문 제출 0건
   3) `connector.connector_order_request` 신규 row 0건
   4) Step 12 stdout / stderr 파일에 secret value / KIS app key / KIS app secret / 계좌번호 / token 평문 출력 0건(R-DOCS-001 [2026-06-17 wrapper 보강] mitigation 정합)
   5) R-AUTO-019 mitigation 1차 실증

### 4. Step 13 / Step 17 wrapper 흐름 정리

Step 13 / Step 17 은 03 spec 의 MarketConnector EC2 SSM RunCommand 흐름을 그대로 사용하지만 둘 다 신규 broker · KIS 주문 제출 없음 step 으로 분류된다.

1. Step 13 `CONNECTOR_ORDER_CHECK`: 완료(DryRun FOUND 확인)
   1) MarketConnector EC2 SSM RunCommand 구조 연결
   2) `/tmp/inject-env.sh` 로딩 + `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` 검증
   3) `connector_order_check.py` 실행
   4) **분류: 주문 상태 조회 / DB 상태 갱신 step / 신규 broker · KIS 주문 제출 없음**
   5) `output1 empty` + `output2 aggregate summary` 형태에서 active 주문 다건 시 summary fallback guard 패치(2026-06-17 17-step E2E §3 정합) 사용 — wrapper 는 03 spec 에 정식 배포된 `connector_order_check.py` 를 그대로 호출 / wrapper 가 별도 fallback 로직을 가지지 않음(R-AUTO-018 mitigation 정합)
2. Step 17 `BALANCE_REFRESH`: 완료(DryRun FOUND 확인)
   1) MarketConnector EC2 SSM RunCommand 구조 연결
   2) `/tmp/inject-env.sh` 로딩 + 환경 검증
   3) `connector_balance.py` 실행(Step 1 과 동일 entrypoint / wrapper 안에서 step 명만 다름)
   4) **분류: balance / position snapshot refresh step / 신규 broker · KIS 주문 제출 없음**
   5) `marketconnector_app` 의 legacy schema USAGE / `legacy.holdings` DML / sequence / database search_path 가 17-step E2E §4 에서 보정된 상태 정합(R-DATA-011 mitigation 1차 실증)

### 5. 전체 1~17 DryRun 결과 / parser validation / safety grep

1. 전체 1~17 DryRun: 완료
   1) `-DryRun -StartStep 1 -EndStep 17` 실행
   2) 17 step 모두 `FOUND` 확인
   3) 실제 ECS RunTask 제출 0건
   4) 실제 Batch SubmitJob 제출 0건
   5) 실제 SSM command 제출 0건
2. PowerShell parser validation: 완료
   1) main wrapper / config / functions / step 17개 = 총 20개 파일
   2) 모두 parser OK
   3) parser error 0건
3. 위험 키워드 safety grep: 완료
   1) `--execute` / order / buy / sell 키워드 점검
   2) Step 10 / Step 11 의 `--execute` 는 strategy execution 내부 상태 생성·갱신 용도 / broker · KIS 직접 제출 아님 분류(OD-MS-016 책임 분리 정합)
   3) Step 12 의 `--execute` 는 실제 KIS paper 주문 제출 가능 step 분류 + Risk `PAPER_ORDER_GATE` 등록 + 중앙 + 내부 이중 gate 점검 통과
   4) Step 13 = 주문 상태 조회 / DB 상태 갱신 분류
   5) Step 14 / Step 15 / Step 16 = fill / position sync DB 갱신 분류
   6) 의도하지 않은 broker · KIS 주문 제출 command 추가 0건

### 6. 안전 / 보안 점검 결과 (Daily AWS PowerShell wrapper 구현)

1. 본 일자 wrapper 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 변경 0건. `.kiro/scripts/` 신규 폴더 / 파일은 운영자 로컬 PC 도구 / Kiro spec 산출물 외부.
2. 아래 민감정보는 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.
   - 실제 secret value / KIS app key / KIS app secret / token
   - 계좌번호
   - RDS password / RDS endpoint hostname
   - account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id
   - instance-id / EIP / image digest full sha256 / task ARN / job ARN
3. AWS / SSM / EC2 / ECS / Batch / IAM / Secrets Manager / SSM Parameter Store / RDS / KIS 호출은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행.
   - AWS CLI / boto3 실행 0건
   - AWS 리소스 생성 / 수정 / 삭제 0건
   - `secretsmanager:GetSecretValue` 결과값 평문 기록 0건
   - CloudWatch Logs 본문 / SSM 응답 본문 / KIS API response body / 운영자 PowerShell stdout 전문 평문 인용 0건
4. wrapper summary / overrides JSON / SSM stdout · stderr 파일에 secret 평문 출력 0건(R-DOCS-001 [2026-06-17 wrapper 보강] mitigation 정합 / `/tmp/inject-env.sh` v5 env injection 만 사용 / value 평문 출력 0건 / key presence / length 만 점검).
5. broker / KIS 호출 0건(본 wrapper 작업 중). 신규 BUY / SELL / 취소 / 정정 / `--execute` 0건. SELL position `mark_position_sell_ordered()` 호출 0건. fill · position sync 자동 재시도 0건.
   - live 자동 BUY / SELL E2E 검증은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지.
   - 본 wrapper 작업은 `aws-paper` 한정 / aws-live 작업 0건 / wrapper 환경 입력 자체가 `aws-paper` 만 허용.
6. RDS DDL 0건. DML 은 Step 1 단독 SSM 1차 검증 한정.
   - `connector.connector_balance_snapshot` insert 1건
   - `connector.connector_position_snapshot` 보유종목 0건 처리
   - `legacy.holdings` 저장은 03 spec Step 1 흐름 정합 한정

   그 외 17 step DryRun 은 ECS RunTask / Batch SubmitJob / SSM command 제출 0건이므로 RDS write 0건. 미실행 사항:
   - wrapper 기반 전체 1~17 실제 재실행 0건
   - Step 12 `-AllowPaperOrderExecute` 사용 0건
   - Windows KRX crawler worker stopped 상태 시 Step 2 실제 실행 검증 0건(skip 동작 코드 존재 / 실제 시나리오 검증은 후속)
   - bundled wrapper 미생성
7. 아래 운영 식별자만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.
   - wrapper 파일명 / step 파일명
   - cluster `portfolio-paper-cluster`
   - Task Definition family·revision
   - Job Queue · Job Definition family · revision
   - Log Group 이름
   - `C:\Temp\portfolio-daily-aws-paper` 하위 run 폴더
   - parser validation 20개 파일

## 2026-06-18 (Daily AWS Paper Wrapper 17단계 실운영 검증 — 03 spec 책임 step)

같은 일자 운영자가 직접 수행한 Daily AWS Paper Wrapper(`.kiro/scripts/run-daily-aws-paper.ps1`) 의 Step 1 ~ Step 17 실 실행 중 03 spec(MarketConnector EC2) 책임 step 인 Step 1 / Step 12 / Step 13 / Step 17 결과를 누적 기록한다.

- Step 2 / Step 3 = 08 spec
- Step 4 / Step 5 = 09 spec
- Step 6 ~ Step 11 / Step 14 ~ Step 16 = 04 spec operation-notes 의 2026-06-18 섹션 정합

환경 `aws-paper` / RunDate `2026-06-18` / region `ap-northeast-2` / Step 12 `-AllowPaperOrderExecute` 첫 사용.

Kiro 는 문서 작성 / 절차 정리만 수행. 실제 wrapper 실행 / SSM RunCommand / KIS API 호출 / 운영자 직접 patch / Docker rebuild / ECR push / ECS 재실행은 모두 운영자가 직접 진행. 실제 broker / KIS 호출은 KIS paper BUY 4건(Step 12) + balance · order check 조회성 한정 / SELL · 취소 · 정정 호출 0건 / aws-live 작업 0건.

### 1. Step 1 `CONNECTOR_BALANCE`

1. 실행 형태: 완료
   1) wrapper `-StartStep 1 -EndStep 1` 또는 통합 1~17 실행의 일부로 진입(운영자 직접 결정)
   2) MarketConnector EC2 SSM RunCommand 로 venv python 환경에서 `connector_balance.py` 실행
   3) `/tmp/inject-env.sh` v5 env injection 사용(JSON SecretString 내부 key 추출 + `APP_*` 호환 key + `KIS_*` alias 동시 export)
2. 실행 결과: 성공
   1) SSM RunCommand `Status: Success` / `ResponseCode: 0`
   2) `connector.connector_balance_snapshot` 신규 row 저장(03 spec 본문 책임 흐름 정합)
   3) `connector.connector_position_snapshot` 갱신 / `legacy.holdings` 갱신은 Step 17 본 실행 결과 정합으로 기록(§4)
   4) 신규 broker / KIS 주문 호출 0건 / 잔고 조회성 한정

### 2. Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` — `-AllowPaperOrderExecute` 첫 사용

본 step 은 wrapper 구조 1차 수립(2026-06-17 OD-MS-023) 후 운영자가 `-AllowPaperOrderExecute` 옵션을 처음 명시 사용한 사례다. 사용 사실 / 영향 row / 통제된 복구 절차를 R-AUTO-019 mitigation 정합 형식으로 누적 기록한다.

1. 실행 형태: 완료
   1) wrapper `-StartStep 12 -EndStep 12 -AllowPaperOrderExecute` 명시 진입(R-AUTO-019 mitigation 정합 — 옵션 default OFF / 운영자 직접 옵션 명시 시에만 진입)
   2) wrapper 중앙 PAPER_ORDER_GATE + Step 12 내부 이중 gate 모두 통과
   3) `aws-paper` 환경 + `PORT_DB_TARGET=aws-paper` guard 통과
   4) MarketConnector EC2 SSM RunCommand 로 venv python 환경에서 `connector_strategy_order_execute.py --execute` 호출
2. 1차 시도 — KIS paper API read timeout: 확인
   1) 네트워크 / DNS / TCP / HTTPS 기본 연결은 정상 확인 — KIS paper endpoint 일시 지연성 장애로 판단(R-BROKER-004 신규 정합)
   2) `connector.connector_order_request` id `38 ~ 41` 생성 후 FAILED
   3) `execution.strategy_execution_order` id `30 ~ 33` FAILED 처리
   4) `connector.connector_api_call_log` 의 `response_status` / 응답 timeout 패턴 / `broker_order_no` 부재 확인
3. 통제된 REQUESTED 복구: 완료(R-BROKER-004 mitigation 1차 실증)
   1) 단순 재실행 금지 정책 정합 — `connector_order_request` / `connector_api_call_log` / `broker_order_no` 존재 여부 사전 점검
   2) `broker_order_no` 부재 확인 후 영향 strategy_execution_order 를 SUBMITTED 직전 상태인 REQUESTED 로 운영자 직접 SQL 보정
   3) wrapper 우회 금지 / 운영자 직접 SQL 만 사용
4. 재시도 결과: 성공
   1) wrapper Step 12 재진입(`-AllowPaperOrderExecute` 옵션 그대로)
   2) KIS paper BUY 4건 제출 성공
   3) 종목 / 매핑:
       - `004990` 롯데지주 — `strategy_execution_order id 30` SUBMITTED / `connector_order_request id 42` ACCEPTED / `broker_order_no 0000025576`
       - `023530` 롯데쇼핑 — `strategy_execution_order id 31` SUBMITTED / `connector_order_request id 43` ACCEPTED / `broker_order_no 0000025740`
       - `003490` 대한항공 — `strategy_execution_order id 32` SUBMITTED / `connector_order_request id 44` ACCEPTED / `broker_order_no 0000025744`
       - `042660` 한화오션 — `strategy_execution_order id 33` SUBMITTED / `connector_order_request id 45` ACCEPTED / `broker_order_no 0000025747`
   4) 4건 모두 paper 응답 정상 / SELL · 취소 · 정정 호출 0건 / aws-live 작업 0건
   5) 중복 broker 주문 0건 — broker_order_no 4종 모두 unique / R-BROKER-004 mitigation 정합 정상
5. 후속 인계: 후속
   1) KIS paper API timeout 재시도 정책 정식 문서화(03 spec runbook §4.2 또는 후속 phase 산출물 / followups-overview 2026-06-18 §2)
   2) wrapper Step 12 stdout 의 timeout / 복구 / 재시도 라벨 자동 기록(wrapper run summary 보강 / followups-overview 2026-06-18 §5)

### 3. Step 13 `CONNECTOR_ORDER_CHECK` — broad → 단건 fallback skip 1차 실증

본 step 은 2026-06-17 17-step E2E 의 summary fallback guard 패치(R-AUTO-018 mitigation) 가 본 일자 다건 active candidate 상황에서 자동으로 작동하는지 1차 실증한 결과다.

1. broad 조회 결과: 확인
   1) wrapper Step 13 `connector_order_check.py` 호출(MarketConnector EC2 SSM)
   2) KIS `inquire-daily-ccld` 응답 — `output1 empty` + `output2 summary-only`
   3) active_order_candidates = 4(Step 12 의 connector_order_request id `42 ~ 45` ACCEPTED 정합)
2. summary fallback 자동 skip: 완료(R-AUTO-018 [2026-06-18 보강] 정합)
   1) summary fallback guard 가 active_order_candidates >= 2 조건에서 자동 skip
   2) `connector.connector_order_event` / `connector.connector_fill` / `connector_order_request status` 잘못 매핑 0건
   3) wrapper Step 13 stdout 에 `summary fallback skipped (active_order_candidates=4)` 라벨 출력
3. 단건 재조회: 완료
   1) `--code` / `--order-no` / `--no-broad` 단건 재조회 4회 진행
   2) 4건 모두 정상 체결 반영
   3) 체결 결과:
       - `004990` 롯데지주 4주 체결 → `connector_order_event` / `connector_fill` 생성
       - `023530` 롯데쇼핑 8주 체결 → 동상
       - `003490` 대한항공 6주 체결 → 동상
       - `042660` 한화오션 11주 체결 → 동상
   4) `connector_order_request 42 ~ 45` 모두 FILLED 전환
4. 후속 인계: 후속
   1) wrapper 안에 broad 조회 후 active_order_candidates >= 2 검출 시 자동 단건 조회 loop 호출 추가 — followups-overview 2026-06-18 §1 후속

### 4. Step 17 `BALANCE_REFRESH` — view_app legacy schema 권한 우회 사례

1. 실행 결과: 성공
   1) wrapper Step 17 `connector_balance.py` 호출(MarketConnector EC2 SSM)
   2) SSM commandId `66ec8831-74b9-469c-8410-6ccb11cb3400`
   3) responseCode `0`
   4) Status `Success`
   5) `connector.connector_position_snapshot` row_count `36` / max_created_at `2026-06-18 04:33:07.456056+00`
   6) `legacy.holdings` row_count `41` / max_created_at `2026-06-18 04:33:07.420226`
2. 결과 확인 시 권한 우회 사례: 확인
   1) view_app 의 `legacy` schema USAGE / `legacy.holdings` SELECT 권한 부재(OD-DB-007 정합 — legacy schema 모든 app role 미부여 정책 / `marketconnector_app` 만 R-DATA-011 mitigation 으로 1건 예외 GRANT 보유)
   2) Step 17 결과 확인 시 view_app 으로 `legacy.holdings` 조회 시 `permission denied` 발생
   3) 운영자가 `portfolio_admin` 으로 우회 조회 — 운영 검증은 통과 / 단 view_app 의 정식 GRANT 필요 여부는 운영자 결정 후속(R-DATA-005 [2026-06-18 보강] 정합)
3. 안전 점검: 완료
   1) Step 17 자체 실행은 성공 — 본 사례는 결과 확인 권한 문제이며 wrapper / 본 step 동작 자체에는 영향 없음
   2) view_app 의 `legacy.holdings` SELECT 권한 추가 여부는 02 spec db-roles-and-grants 후속 phase 책임으로 분리(followups-overview 2026-06-18 §4)
4. 최종 OPEN 포지션(Step 16 결과 + Step 17 snapshot 정합): 확인
   1) `003490` 대한항공 — 58주 / entry_price `28979.3103` / `position_state_id 8`
   2) `004990` 롯데지주 — 69주 / entry_price `27008.6956` / `position_state_id 7`
   3) `023530` 롯데쇼핑 — 8주 / entry_price `194225.0000` / `position_state_id 11`(신규)
   4) `042660` 한화오션 — 11주 / entry_price `126118.1818` / `position_state_id 12`(신규)
   5) `088350` 한화생명 — 244주 / entry_price `5744.4057` / 기존 OPEN 유지
   6) `282330` BGF리테일 — 17주 / entry_price `120182.3529` / 기존 OPEN 유지
   7) 추가매수 merge 2건(004990 / 003490) + 신규 OPEN 2건(023530 / 042660) — OD-MS-024 신규 / R-DATA-012 mitigation 1차 실증

### 5. 안전 / 보안 점검 결과 (Daily AWS Paper Wrapper 17단계 실운영 검증)

1. 본 일자 작업 범위에서 broker / KIS 호출은 KIS paper BUY 4건(Step 12 본 실행) + balance · order check 조회성 호출(Step 1 / Step 13 / Step 17) 한정. SELL / 취소 / 정정 / 추가 `--execute` 호출 0건. SELL position `mark_position_sell_ordered()` 호출 0건(OD-MS-016 정합 — 본 일자 SELL 흐름 미발생). aws-live 작업 0건.
2. RDS DDL 0건. DML 은 본 일자 17-step 정상 흐름 한정.
   - `connector.connector_order_request` insert(id `38 ~ 41` FAILED + `42 ~ 45` ACCEPTED)
   - `connector.connector_order_event` insert(체결 4건)
   - `connector.connector_fill` insert(체결 4건)
   - `execution.strategy_execution_order` update(id `30 ~ 33` FAILED → REQUESTED → SUBMITTED → FILLED)
   - `connector.connector_balance_snapshot` insert
   - `connector.connector_position_snapshot` insert(row_count 36)
   - `legacy.holdings` insert(row_count 41)

   운영자 직접 SQL 보정 — strategy_execution_order id `30 ~ 33` 의 FAILED → REQUESTED 복구(R-BROKER-004 mitigation 정합 / wrapper 우회 금지 / 운영자 직접 SQL 만 사용).
3. 아래 민감정보는 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.
   - 실제 secret value / KIS app key / KIS app secret / token / KIS paper login credential
   - 계좌번호 / 계좌 비밀번호
   - RDS password / RDS endpoint hostname
   - account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id
   - instance-id / EIP / image digest full sha256 / task ARN / job ARN
4. KIS paper 4건의 `broker_order_no`(`0000025576` / `0000025740` / `0000025744` / `0000025747`) 는 broker 응답값으로 운영 식별자 — 실계좌 주문번호 아님 / 본 노트 사실 기록 정합. SSM commandId `66ec8831-74b9-469c-8410-6ccb11cb3400` 도 운영 식별자로 사실 기록.
5. 운영자가 직접 patch / 정식 배포한 `port_strategy_execution/execution_sync_buy_position.py` 변경분은 04 spec operation-notes 에 사실로만 기록(본문 전체 인용 0건 / R-DOCS-001 정합). 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역).
6. AWS / SSM / EC2 / ECS / Batch / IAM / Secrets Manager / SSM Parameter Store / RDS / GRANT / KIS / S3 / CloudWatch / Docker / ECR 호출은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행.
   - AWS CLI / boto3 실행 0건
   - AWS 리소스 생성 / 수정 / 삭제 0건
   - `secretsmanager:GetSecretValue` 결과값 평문 기록 0건
   - CloudWatch Logs 본문 / SSM 응답 본문 / KIS API response body / Docker build · push 로그 / 운영자 PowerShell stdout 전문 평문 인용 0건
7. 아래 운영 식별자는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.
   - connector_order_request id `38 ~ 41`(FAILED) + `42 ~ 45`(ACCEPTED)
   - strategy_execution_order id `30 ~ 33` / broker_order_no 4종
   - position_state_id `7` · `8` · `11` · `12` / SSM commandId
   - `connector.connector_position_snapshot` row_count `36` / `legacy.holdings` row_count `41`
   - 종목 코드 `004990` · `023530` · `003490` · `042660` · `088350` · `282330` / 종목명 / 수량 / entry_price
   - data_date `2026-06-17` / signal_date · run_date `2026-06-18`

## 2026-06-18 (Step 13 `connector_order_check.py` 단건 순차 조회 기본화 — 운영 안전성 강화)

본 섹션은 2026-06-18 같은 일자 첫 번째 세션(Daily AWS Paper Wrapper 17단계 실운영 검증 완료) 후속으로 운영자가 직접 수행한 MarketConnector `connector_order_check.py` 기본 실행 모드 변경(broad 체결조회 일괄 → active 주문 단건 direct-only 순차 조회) 결과를 본 spec 운영 노트에 누적한다.

운영 실패가 아니라 운영 안전성 강화를 위한 설계 변경. 첫 번째 세션의 broad 조회 `output1 empty` + `output2 summary-only` + active candidate 4건 응답 패턴(R-AUTO-018 [2026-06-18 보강] mitigation 1차 실증) 결과를 더 안전한 기본 동작으로 코드 레벨에 반영.

본 작업은 아래에 해당하며 OD-MS-025 신규 결정 락에 정합한다.

- 03 spec MarketConnector EC2 의 운영 안전성 강화
- 17-step Daily AWS Paper Wrapper 의 Step 13 단독 실행 책임 경계 명확화

### 1. 변경 대상 파일 + 기본 실행 모드 변경: 완료

1. 변경 대상 파일: 확인
   1) `port-marketconnector/connector_order_check.py`
   2) EC2 실운영 경로: `/home/ec2-user/apps/port-marketconnector/src/connector_order_check.py`
2. 기본 실행 모드: 변경 완료
   1) 변경 전 — broad 체결조회 중심(active 주문 후보 다건 상황에서 `output1 empty` + `output2 summary-only` 응답 시 마지막 주문 row 또는 임의의 단일 주문에 summary 값을 잘못 매핑할 수 있는 위험 구조 / R-AUTO-018 1차 실증 패턴)
   2) 변경 후 — active 주문 목록 조회 후 주문번호 / 종목코드 기준 단건 direct-only 순차 조회(다건 active 주문 상태에서도 broker_order_no 단위 단건 조회로만 체결 동기화 / summary fallback 노출 표면 감소)
3. 운영 실패 / 운영 안전성 강화 구분: 확인
   1) 본 변경은 운영 실패 복구가 아니라 운영 안전성 강화를 위한 설계 변경
   2) 같은 일자 첫 번째 세션의 wrapper 안 자동 skip 1차 실증에 더해 `connector_order_check.py` 자체의 기본 동작을 한 단계 더 안전한 방향으로 변경
   3) wrapper ps1 책임은 orchestration 만(`.kiro/scripts/steps/step-13-connector-order-check.ps1` = SSM RunCommand 호출 / 환경 검증 / log 저장) / 체결조회 방식 제어(active 조회 / 단건 direct-only / broad 모드 분리) = `connector_order_check.py` 내부 책임 — OD-MS-025 정합

### 2. 신규 옵션 + 운영 모드 정책: 완료

1. 신규 옵션: 추가 완료
   1) `--broad` — legacy broad 조회 모드 / 기본값 미사용 / 진단 · legacy 용도 한정 / 운영 기본 모드는 active 주문 단건 순차 조회
   2) `--active-limit` — active 주문 단건 순차 조회 최대 처리 건수(`--active-limit` 기본값 결정 후속 / followups-overview 2026-06-18 §6)
   3) `--code` / `--order-no` / `--no-broad` — 명시 주문 조회 조합으로 broad fallback 없이 해당 주문만 조회
2. 운영 모드 정책: 확정
   1) 기본 실행(`python connector_order_check.py`) — active 주문 목록 조회 후 주문번호 / 종목코드 기준 단건 direct-only 순차 조회
   2) legacy broad 일괄 조회(`python connector_order_check.py --broad`) — 진단 · legacy 용도 한정
   3) 명시 주문 조회(`python connector_order_check.py --code {ticker_code} --order-no {broker_order_no} --no-broad`) — 단건 direct-only / broad fallback 없이 해당 주문만 조회
   4) summary fallback 허용 조건 — `connector_order_request` 후보가 주문번호 / 종목코드 기준으로 1건 확정된 경우에만 허용 / 다건 active 상태에서 `output1 empty` + `output2 summary-only` 응답을 DB 반영 근거로 사용하지 않음(R-AUTO-018 [2026-06-18 추가 보강] mitigation 정합)

### 3. 검증 milestone (2026-06-18 두 번째 세션): 완료

1. 로컬 검증: 통과
   1) `python -m py_compile connector_order_check.py` 통과
   2) `python connector_order_check.py --help` 의 `--broad` / `--active-limit` 옵션 표시 확인
2. 로컬 commit: 완료
   1) commit hash `75cb804`
   2) commit message `fix(connector): run order checks sequentially per active order`
   3) 현재 repository 의 remote push destination 미설정 → git push 미수행
   4) Git remote 설정 + commit `75cb804` 정식 push 는 후속 분리(followups-overview 2026-06-18 §1)
3. EC2 반영(S3 경유): 완료
   1) S3 bucket `portfolio-paper-migration-yukiever`
   2) S3 key `deploy/marketconnector/connector_order_check.py`
   3) 파일 크기 `39159 bytes`
   4) EC2 instance id `i-0fce77927b7397b88`(OD-NET-010 표준 SSM Port Forwarding 표준 경유지 / 03 spec 정식 운영 EC2)
   5) EC2 backup 파일명 `connector_order_check.py.bak-20260618-step13-per-order`(원본 보존 정책 정합)
   6) SSM commandId `b344d404-07a4-4bb6-9d63-34151e648bab`(SSM RunCommand 응답 식별자)
4. wrapper 단독 실행 검증: 통과
   1) 명령 `-StartStep 13 -EndStep 13`(StartStep / EndStep — FromStep / ToStep 실수 시 default 1/17 진입 위험 / wrapper 파라미터 명시)
   2) 단건 direct-only 조회 1건(`004990` / broker_order_no `0000025576`) 기준 `connector_order_event` / `connector_fill` 정상 생성
   3) 신규 broker · KIS 주문 제출 0건 / 모두 조회성 호출

### 4. 결정 / 리스크 변경 요약: 완료

1. 신규 결정: 완료
   1) OD-MS-025(MarketConnector `connector_order_check.py` 운영 모드 = active 주문 단건 순차 조회 기본 + broad 옵션 격리 + `connector_order_check.py` 내부 분기, 🟡 잠정)
2. 본문 변경 없는 결정: 1차 실증 메모만 보강
   1) OD-MS-016(Strategy Execution / MarketConnector 책임 분리) — MarketConnector 내부 운영 모드 변경 / Strategy Execution 측 책임 경계 그대로 / Status 기존 값 그대로 유지
   2) OD-MS-021(Backend AWS E2E dry-run 17단계 순서 + 안전 기준) — 17단계 순서 / 안전 기준 변경 없음 / Step 13 단독 실행으로 동작 변경 검증 / Status 기존 값 그대로 유지
   3) OD-MS-023(Daily AWS wrapper 운영 정책) — wrapper ps1 의 책임은 orchestration 만 / 체결조회 방식 제어는 `connector_order_check.py` 내부 책임으로 명확화 / Status 기존 값 그대로 유지
3. 신규 리스크: 0건
4. 본문 변경 없는 리스크: detection / mitigation 메모 보강
   1) R-AUTO-018(KIS `inquire-daily-ccld` summary-only fallback 오매핑) — [2026-06-18 추가 보강] 메모 추가.
       - 본 일자 첫 번째 세션의 wrapper 안 자동 skip 1차 실증에 더해 `connector_order_check.py` 자체의 기본 실행 모드가 active 주문 단건 순차 조회로 변경.
       - broad 체결조회 호출 빈도 자체가 줄고 다건 active 상태에서 summary fallback 노출 표면이 감소.
       - Status `Mitigated` 유지(verified on 2026-06-18 두 번째 세션).

### 5. 안전 / 보안 점검 결과 (Step 13 `connector_order_check.py` 단건 순차 조회 기본화): 완료

1. broker / KIS 호출 범위: 단건 direct-only 조회 1건(`004990` / `0000025576`) + wrapper Step 13 단독 실행(`-StartStep 13 -EndStep 13`) 1건 — 모두 조회성. 신규 BUY · SELL · 취소 · 정정 · `--execute` 호출 0건. SELL position `mark_position_sell_ordered()` 호출 0건. fill · position sync 자동 재시도 0건. aws-live 작업 0건.
2. RDS DDL 0건. DML 은 Step 13 정상 흐름 한정 — `connector.connector_order_event` / `connector.connector_fill` 신규 row 는 단건 direct-only 조회 결과 정합.
3. 아래 민감정보는 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.
   - 실제 secret value / KIS app key / KIS app secret / token / KIS paper login credential
   - 계좌번호 / 계좌 비밀번호
   - RDS password / RDS endpoint hostname
   - account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id
   - EIP / image digest full sha256 / task ARN / job ARN
4. AWS / SSM / EC2 / S3 / IAM / Secrets Manager / SSM Parameter Store / RDS / KIS 호출은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행.
   - AWS CLI / boto3 실행 0건
   - AWS 리소스 생성 / 수정 / 삭제 0건
   - `secretsmanager:GetSecretValue` 결과값 평문 기록 0건
   - CloudWatch Logs 본문 / SSM 응답 본문 / KIS API response body / 운영자 PowerShell stdout 전문 평문 인용 0건
5. 운영자 직접 patch 한 `port-marketconnector/connector_order_check.py` 변경분은 본 노트에 사실로만 기록(본문 전체 인용 0건 / R-DOCS-001 정합). 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 두 번째 세션 작업으로 인한 변경 0건(spec 영역).
6. wrapper 파라미터 `StartStep` / `EndStep` 사용(FromStep / ToStep 실수 시 default 1/17 진입 위험) — 본 일자 단독 실행은 `-StartStep 13 -EndStep 13` 으로 진행 / wrapper 파라미터 명시 정책 정합.
7. 아래 운영 식별자는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.
   - commit `75cb804`
   - S3 bucket `portfolio-paper-migration-yukiever`
   - S3 key `deploy/marketconnector/connector_order_check.py`
   - 파일 크기 `39159 bytes`
   - EC2 instance id `i-0fce77927b7397b88`
   - EC2 backup 파일명 `connector_order_check.py.bak-20260618-step13-per-order`
   - SSM commandId `b344d404-07a4-4bb6-9d63-34151e648bab`
   - 종목 코드 `004990` / broker_order_no `0000025576`
   - wrapper 옵션 `--broad` · `--active-limit` · `--code` · `--order-no` · `--no-broad`
   - wrapper 파라미터 `StartStep` · `EndStep`

   instance id 는 OD-NET-010 에 이미 사실 기록되어 있는 표준 SSM Port Forwarding 표준 경유지 식별자.

### 6. 후속 작업 (2026-06-18 두 번째 세션 이월): 예정

1. Git remote push destination 설정 + commit `75cb804` 정식 push: 예정
   1) 현재 `port-marketconnector` repository 의 remote push destination 미설정
   2) 운영자 결정 후 origin 등록 + push
   3) 07 spec CI/CD pipelines 연동 시점에 정식 origin 정책과 함께 정리
2. Step 13 wrapper 단독 실행 summary 출력 보강: 예정
   1) `summary/run-summary.txt` 에 `active_order_count` / `single_check_success_count` / `single_check_failed_count` / `broad_mode_used` 여부 자동 기록
   2) 실패 시 종목코드 / 주문번호 / 사유 자동 기록(R-AUTO-018 detection 정합 / 운영자 PowerShell summary 만으로 사후 검증 가능)
3. View Daily Batch 화면 연동 시 주문별 표시: 예정
   1) 05 spec 진입 시점에 단건 direct-only 조회 결과를 주문별 row 단위로 화면에 표시할지 결정
   2) wrapper summary 형식과 화면 형식의 정합성 검토
4. Windows PowerShell · AWS CLI SSM 출력 cp949 인코딩 회피 패턴 정리: 예정
   1) wrapper Step 13 stdout / SSM 응답 본문이 한글 라벨 / 파일명 포함 시 cp949 vs UTF-8 인코딩 충돌 가능성
   2) 운영자 노트 / spec 산출물 본문에 cp949 깨짐 0건 정책 유지(R-DOCS-001 정합)
5. KIS paper API timeout 재시도 정책 명문화(2026-06-18 첫 번째 세션 §2 followups §2 와 결합): 예정
   1) 단건 direct-only 조회 패턴이 `--no-broad` + `--code` + `--order-no` 로 통제되는 구조이므로 timeout 재시도 시점에도 broker_order_no 별 단건 조회로만 동기화
   2) 03 spec runbook §4.2 정식 절차 기재 후속
6. `--active-limit` 기본값 결정 후속: 예정
   1) 본 일자 1차 실증 시점의 default 값 적용
   2) 운영 환경에서 active 주문 수 누적 시 처리 건수 상한 정책 결정

## IAM 변경 기록 템플릿 (필요 시 일자별 추가)

```
## YYYY-MM-DD IAM 변경
- 변경 일자: YYYY-MM-DDTHH:MM:SS+09:00
- 변경자: [운영자 식별자]
- 변경 사유: [한 줄]
- 변경 전 / 후 항목 요약: [추가·삭제 statement 수, Action·Resource 변경 요약 — JSON 본문 전체 인용 금지]
```


## 2026-06-22 MarketConnector env bootstrap 재생성 + Paper SELL 주문 제출 검증

운영자가 2026-06-22 직접 수행한 Daily AWS Paper Wrapper 1 ~ 17 두 번째 실 운영 실행 중 본 spec 책임 step(Step 1 / Step 12 / Step 13 / Step 17) 결과를 누적 기록한다.

흐름 요약:

- Step 1 최초 실패 원인 식별(`/tmp/inject-env.sh not found`)
- `daily-aws-paper.functions.ps1` MarketConnector env bootstrap 함수 추가 후 Step 1 재실행 성공
- Step 12 한화생명 244주 SELL_HARD_STOP MARKET 제출 성공
- Step 13 체결조회 성공
- Step 17 BALANCE_REFRESH Success

본 일자에 Kiro 는 문서 작성 / 절차 정리만 수행. 실제 wrapper 실행 / EC2 / SSM / KIS / RDS / GRANT 작업은 모두 운영자가 직접 진행. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건.

본 작업은 단순 dry-run 이 아니라 실제 Paper SELL 주문 제출 / KIS 접수 / 체결조회 / SELL fill sync / position CLOSED / balance refresh 까지 end-to-end 통과한 **첫 완주성 운영 결과**다.

### 1. Step 1 `CONNECTOR_BALANCE` 최초 실패 원인 분석 + bootstrap 패치

1. 1차 실패 패턴: 확인
   1) SSM RunCommand 응답에 `/tmp/inject-env.sh: not found` 또는 동등 env missing 메시지
   2) MarketConnector EC2 의 `/tmp` 디렉터리는 ephemeral / EC2 stop · start 이후 휘발됨
   3) 기존 SSM step 은 env 파일이 EC2 위에 선존재한다는 가정으로 작성되어 있었음(이전 일자 일자에는 v5 env injection 운영자 직접 작성으로 유지)
2. 운영자 직접 patch: 완료
   1) 대상 파일 = 운영자 로컬 wrapper `daily-aws-paper.functions.ps1`(`.kiro/scripts/` 영역 / 운영자 도구 / 본 spec 소스 영역 외부)
   2) MarketConnector env bootstrap 함수 신규 추가 / Step 1 / Step 12 / Step 13 / Step 17 에서 공통 호출(OD-MS-027 신규 정합)
   3) bootstrap 흐름 — `secretsmanager:GetSecretValue` 호출 → JSON SecretString parse → 내부 key(`APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD` / `BASE_URL`) 추출 → `APP_*` 호환 key + `KIS_*` alias 동시 export → `/tmp/inject-env.sh` chmod 700 / 메모리 export 한정 / secret value 평문 출력 0건(R-DOCS-001 정합)
   4) PowerShell parser validation 통과 후 Step 1 재실행 — 1차 실패 후 단 한 차례 재실행으로 통과 / 본 노트 stdout 본문 평문 인용 0건
3. 본 패치의 운영 정책 정합: 확인
   1) OD-MS-027 신규 — `/tmp/inject-env.sh` 선존재 가정 폐기 / step 실행 시점 재생성 / wrapper 공통 함수 호출 / secret value 미출력
   2) R-AUTO-021 신규 mitigation 정합 — MarketConnector EC2 stop · start 후 `/tmp` 휘발로 Step 1 / 12 / 13 / 17 실패 위험에 대한 운영 단계 1차 차단
   3) 본 spec 소스 코드 / 운영 entrypoint(`connector_balance.py` / `connector_order_check.py` / `connector_strategy_order_execute.py`) 변경 0건 — 운영자 직접 patch 는 wrapper 영역 한정

### 2. Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` Paper SELL 제출

1. Step 12 진입 전 사전 점검(03 spec runbook / safety gate 정합): 완료
   1) execution plan id `96` 생성 확인.
       - `plan_date 2026-06-22` / `strategy_name strategy_ai`
       - `market_signal DEFENSIVE` / `risk_regime DEFENSIVE`
       - `plan_status PARTIALLY_BLOCKED`
       - total_candidate 3 / ready 1 / blocked 2 / skipped 0
       - total_target_amount `1,237,080` / available_cash `-62,763` / max_order_amount `-31,381.50`
   2) Step 12 대상 = `088350` 한화생명 SELL 244주 MARKET 1건(BUY 2건은 현금 부족으로 BLOCKED 처리됨)
   3) 중복 주문 점검 — `connector.connector_order_request` 동일 strategy_execution_order 대상 row 0건
2. 매도 사유(SELL_HARD_STOP) 정합 확인: 완료
   1) entry_date `2026-06-17` / entry_price `5,744.4057` / current_price `5,070`
   2) expected_pnl_rate 약 `-11.7402%` / hard_stop_loss_rate `-10%` / holding_days 5
   3) snapshot_qty 244 / sellable_qty 244 / remaining_qty 244
   4) expected_pnl_amount 약 `-164,554.9908`
   5) `strategy_position_state id 9` 기준 한화생명 OPEN position 의 entry_qty 244 / remaining_qty 244 와 Step 12 SELL 수량 244 일치
3. `-AllowPaperOrderExecute` 명시 실행 + 결과: 완료
   1) wrapper PAPER_ORDER_GATE 통과 — `PaperOrder: True` 라벨 출력(R-AUTO-019 mitigation 정합)
   2) `execution_order id 37` 상태 `SUBMITTED`
   3) `connector_order_request id 46` 생성 / request_type SELL / order_method MARKET / order_qty 244 / request_status `ACCEPTED`
   4) broker_order_no 생성됨 / broker_branch_code 생성됨 — 본 노트 broker 응답값 평문 기록 0건 / `[REDACTED]` 처리 정합(R-DOCS-001 정합)
   5) rejection 없음 / KIS paper API 정상 응답 / 1차 시도 통과(2026-06-18 KIS paper read timeout 회귀 0건 / R-BROKER-004 mitigation 정합)

### 3. Step 13 `CONNECTOR_ORDER_CHECK` 체결 동기화

1. KIS `inquire-daily-ccld` 호출: 성공
   1) `connector_order_check.py` 2026-06-18 패치(`--code` / `--order-no` / `--no-broad` 단건 direct-only 조회 기본 / OD-MS-025 정합) 그대로 사용
   2) 단건 active candidate(`088350` SELL 1건) 정합 — summary fallback guard 진입 없음
2. 체결 결과: 완료
   1) `connector_order_request id 46` request_status `FILLED`
   2) `connector_fill id 34` 신규 생성 / fill_qty 244 / fill_price `5,075.8607` / fill_amount `1,238,510.01` / side SELL / fill_ts `2026-06-22 00:46:58 UTC`
   3) `connector_order_event` 신규 row 정합

### 4. Step 17 `BALANCE_REFRESH`

1. SSM 실행 결과: 완료
   1) SSM Status `Success` / ResponseCode `0` / StdErr empty
   2) 본 일자에는 `marketconnector_app` 의 `legacy.holdings` search_path / 권한 보정(2026-06-17 §4) 이후 회귀 0건 / R-DATA-011 mitigation 그대로 유지
2. 최종 잔고 스냅샷: 확인
   1) `connector.connector_position_snapshot` 최신 `created_at 2026-06-22 00:50:50 UTC`
   2) 보유 5종목 — `003490` 대한항공 58주 / `004990` 롯데지주 69주 / `023530` 롯데쇼핑 8주 / `042660` 한화오션 11주 / `282330` BGF리테일 17주
   3) `088350` 한화생명은 최신 잔고 스냅샷에서 제거 확인(Step 12 SELL 청산 정합)
   4) `legacy.holdings` 결과 확인은 view_app 권한 부재로 운영자가 `portfolio_admin` 으로 우회 조회(R-DATA-005 [2026-06-22 보강] 정합 / 정식 GRANT 후속)

### 5. 결정 / 리스크 변경 요약

1. 신규 결정: 완료
   1) OD-MS-027(MarketConnector env bootstrap 재생성 운영 정책, 🟡 잠정)
   2) OD-DB-011(`execution_app` 의 `decision.strategy_daily_position_decision` 제한적 UPDATE 권한, 🟢 확정)
2. 본문 변경 없는 결정: 1차 실증 메모 보강
   1) OD-MS-016 — Strategy Execution `READY -> REQUESTED` / MarketConnector `REQUESTED -> SUBMITTED` 책임 분리가 SELL 1건 한정으로도 1차 실증
   2) OD-MS-021 — 17단계 안전 기준 정합 / Step 9 권한 보정 외 안전 기준 위반 0건
   3) OD-MS-023 — wrapper 1 ~ 17 두 번째 실 완주 / `-AllowPaperOrderExecute` 두 번째 사용(2026-06-18 BUY 4건 / 2026-06-22 SELL 1건)
3. 신규 리스크: 완료
   1) R-AUTO-021(MarketConnector EC2 stop / start 후 `/tmp` 휘발로 Step 1 / 12 / 13 / 17 실패 위험, Status `Mitigated` — wrapper bootstrap 함수로 1차 차단)
   2) R-DATA-013(`execution_app` 의 `decision` schema UPDATE 권한 누락으로 Step 9 SELL execution link update 실패 위험, Status `Mitigated` — 운영자 직접 GRANT 보정)
4. 본문 변경 없는 리스크: 보강 메모
   1) R-DATA-005 — [2026-06-22 보강] `execution_app` 의 `decision` schema USAGE / `decision.strategy_daily_position_decision` UPDATE 누락이 Step 9 1차 실패 원인으로 추가 식별 / 02 spec db-roles-and-grants 정식 매트릭스 갱신 후속 유지

### 6. 안전 / 보안 점검 결과 (2026-06-22)

1. broker / KIS 호출 = KIS paper SELL 1건(Step 12 본 실행) + balance · order check 조회성 한정. BUY / 취소 / 정정 / 추가 `--execute` 호출 0건. SELL position `mark_position_sell_ordered()` 호출은 MarketConnector executor SELL 성공 시점 책임(OD-MS-016 정합). fill · position sync 자동 재시도 0건. aws-live 작업 0건.
2. RDS DDL 0건. DML 은 17-step 정상 흐름 한정 — `connector.connector_order_request` insert(`id 46` ACCEPTED → FILLED) / `connector.connector_order_event` / `connector.connector_fill` insert(`id 34`) / `connector.connector_balance_snapshot` insert / `connector.connector_position_snapshot` insert(보유 5종목).
3. 아래 민감정보는 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.
   - 실제 secret value / KIS app key / KIS app secret / token / KIS paper login credential
   - 계좌번호 / 계좌 비밀번호
   - RDS password / RDS endpoint hostname
   - account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id
   - instance-id / EIP / image digest full sha256 / task ARN / job ARN
   - broker_order_no 원문 / broker_branch_code 원문
4. AWS / SSM / EC2 / RDS / Secrets Manager / SSM Parameter Store / KIS API 호출은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행.
   - AWS CLI / boto3 실행 0건
   - AWS 리소스 생성 / 수정 / 삭제 0건
   - `secretsmanager:GetSecretValue` 결과값 평문 기록 0건
   - CloudWatch Logs 본문 / SSM 응답 본문 / KIS API response body / 운영자 PowerShell stdout 전문 평문 인용 0건
5. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역). 운영자 직접 patch 한 `daily-aws-paper.functions.ps1` MarketConnector env bootstrap 함수 변경분은 본 노트 § 1 에 사실로만 기록(본문 전체 인용 0건 / R-DOCS-001 정합 / `.kiro/scripts/` 영역).
6. 아래 운영 식별자는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.
   - `execution_plan_id 96` / execution_order id `37`
   - connector_order_request id `46` / connector_fill id `34`
   - position_state_id `9`
   - 종목 코드 / 종목명 / 수량 · 가격 · 비율
   - data_date / signal_date · run_date `2026-06-22`
   - `fill_ts 2026-06-22 00:46:58 UTC` / `created_at 2026-06-22 00:50:50 UTC`

## 2026-06-23 Step 12 retry-normalizer 내장 + Step Functions approval true path Paper SELL E2E

운영자가 2026-06-23 직접 수행한 아래 4개 작업 중 본 spec 책임 step(Step 12 / Step 13 / Step 17) 결과를 누적 기록한다.

- (a) Daily AWS Paper Step Functions state machine `portfolio-paper-daily-step1-17-approval` 실전 검증
- (b) Step 12 `connector_strategy_order_execute.py` 전체 교체 + Step 12 시작부 retry-normalizer 내장
- (c) Step 12 ~ Step 17 `allowPaperOrderExecute=true` approval true path 첫 실 SELL E2E 통과
- (d) connector 측 DB 한글 저장 정상 확인

2026-06-17 첫 BUY 4건 완주(첫 실 BUY E2E) / 2026-06-22 SELL 1건 완주(첫 실 SELL E2E / wrapper 기반) 에 이어 본 일자는 Step Functions approval workflow 를 통한 첫 실 SELL E2E 회차로 분류된다.

본 일자에 Kiro 는 문서 작성 / 절차 정리만 수행. 실제 Step Functions / EC2 / SSM / KIS / RDS / Secrets Manager / Step 12 entrypoint patch / EC2 정식 배포 작업은 모두 운영자가 직접 진행. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건.

04 spec 측 step(Step 6 ~ Step 11 Strategy Decision · Execution / Step 14 ~ Step 16 fill sync · position sync) 의 상세 누적은 3차 작업 책임으로 분리 — 본 노트는 MarketConnector EC2 책임 영역(Step 12 / Step 13 / Step 17) 한정.

### 1. Step 12 retry-normalizer 내장 (`connector_strategy_order_execute.py` 전체 교체 + EC2 정식 배포 + dry-run 통과)

1. 1차 식별 패턴(R-AUTO-022 신규): 확인
   1) 장종료 이후 KIS broker 가 `connector.connector_order_request` 에 대해 REJECTED + `rejection_code = 40580000` 응답을 반환하는 경우, 해당 `connector_order_request_id` 가 `execution.strategy_execution_order` 에 그대로 link 된 상태로 다음 영업일까지 남음
   2) Step 12 의 기본 필터(`execution_status = REQUESTED` + `connector_order_request_id IS NULL`) 가 해당 row 를 잡지 못하므로 자동 재제출 경로가 끊김
   3) 운영자 직접 SQL 복구(`execution_status = REQUESTED` + `connector_order_request_id = NULL`) 없이는 운영 자동화로 진입 불가능한 상태가 발생할 수 있음
   4) SELL · BUY 모두 동일 패턴 / Step Functions 전환 이후에도 동일 entrypoint 가 호출되므로 본 위험은 wrapper 기반 / Step Functions 기반 양쪽에서 동일하게 적용
2. 운영자 직접 patch: 완료
   1) 대상 파일 = `port-marketconnector/connector_strategy_order_execute.py` 전체 교체(`.kiro/scripts/` 외부 / port-marketconnector 영역 / 본 spec 소스 영역의 운영자 직접 patch — 본 노트는 사실 기록만 / 본문 전체 인용 0건 / R-DOCS-001 정합)
   2) 적용 위치 = Step 12(`MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) 시작부 — **별도 Step 11.5 분리 아님 / 기존 Step 12 내부 안전 보완**
   3) 복구 조건 6종 모두 만족할 때만 적용.
       - `execution_mode = PAPER_STRATEGY`
       - `action_type IN (BUY, SELL)`
       - `execution_status IN (READY, FAILED)`
       - `connector_order_request_id IS NOT NULL` + linked `connector_order_request.request_status = REJECTED`
       - `rejection_code = 40580000`
       - `broker_order_no IS NULL` + `connector_fill` 없음
   4) `broker_order_no IS NOT NULL` 이거나 `connector_fill` 이 이미 존재하는 경우는 복구 대상에서 제외 — R-BROKER-004 / R-AUTO-001 의 중복 주문 위험과 충돌하지 않도록 보장
   5) 복구 처리 — `execution_status = REQUESTED` + `connector_order_request_id = NULL` + `result_payload.retry_normalizer` 에 old request 이력(`old_connector_order_request_id` / `request_status` / `rejection_code` / `recovered_at` 등 사후 추적 가능 메타데이터) 저장
3. EC2 정식 배포 + dry-run 통과: 완료
   1) EC2 = `portfolio-paper-marketconnector-ec2`(instance id `i-0fce77927b7397b88` / OD-NET-010 정합 / 본 노트 운영 식별자 사실 기록 — secret 아님)
   2) 정식 배포 흐름 = 운영자 직접 patch → EC2 배포(2026-06-18 §2 와 동일 흐름 — 본 노트 본문 전체 인용 0건)
   3) `.venv/bin/python` dry-run 통과 — 현재 retry 후보 0건 / REQUESTED 주문 0건 / 본 일자에는 R-AUTO-022 의 실제 후보 발생 0건
   4) Step Functions Step 12 도 `.venv/bin/python` 사용 정합 — Step Functions 전환 이후에도 동일 entrypoint 가 호출되어 retry-normalizer 가 그대로 동작
4. 운영 정책 정합: 확인
   1) OD-MS-028 신규(Step 12 retry-normalizer 내장 정책 / 🟡 잠정 / 영향 spec 03 · 04 · 10 / 별도 Step 11.5 분리 아님 명시)
   2) OD-MS-016 본문 변경 없음 — Strategy Execution `READY -> REQUESTED` / MarketConnector `REQUESTED -> SUBMITTED` 책임 분리는 그대로 유지 / retry-normalizer 는 MarketConnector 측 Step 12 내부 안전 보완으로 분류
   3) OD-MS-009 / OD-MS-021 / OD-MS-023 본문 변경 없음 — Daily Batch orchestration / 17단계 안전 기준 / wrapper 운영 정책 본문 그대로 유지
   4) R-AUTO-022 신규(장종료 REJECTED · `40580000` 후 자동 재제출 경로 끊김 위험, Status `Mitigated` / 본 일자 dry-run 통과로 1차 차단 — 실 retry 후보 발생 첫 회차의 검증은 followups-overview 2026-06-23 §3 후속)
   5) 본 mitigation 의 자동 복구 대상은 `40580000`(장종료) 한정 — 다른 rejection_code(거래정지 / 거부 / 호가 단위 오류 등) 는 운영자 직접 점검 책임 유지(R-AUTO-001 / R-BROKER-004 정합)

### 2. Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` Step Functions approval true path Paper SELL 제출

1. Step Functions approval workflow 사전 검증(OD-MS-029 신규 정합): 완료
   1) state machine = `portfolio-paper-daily-step1-17-approval`(운영자 직접 정의 / 본 노트는 사실 식별자 기록만 / state 정의 본문 전체 인용 0건)
   2) Step 1 ~ Step 17 `allowPaperOrderExecute=false` approval blocked path 사전 통과 — Step 10 / Step 11 / Step 12 의 입력값(READY / REQUESTED 후보 / 종목 / 수량 / 사유) paper 환경 dry-run 확인
   3) 운영자가 사전 검증 결과 점검 후 Step 12 ~ Step 17 `allowPaperOrderExecute=true` approval true path 로 승인 실행 진입 — 동일 state machine 의 입력 파라미터(`allowPaperOrderExecute`) 와 시작 step 분기로만 분리 / 별도 state machine 분리 없음
   4) Step Functions Step 12 진입 직전 wrapper 공통 MarketConnector env bootstrap 함수 호출 흐름 유지(R-AUTO-021 [2026-06-23 보강] 정합 / Step Functions 전환 이후에도 `/tmp/inject-env.sh` 재생성 패턴 유지)
2. Step 12 진입 전 사전 점검(03 spec runbook §2 / safety gate 정합): 완료
   1) Step 12 대상 = `282330` BGF리테일 SELL 17주 MARKET 1건 — 04 spec 측 Step 9 ~ Step 11 흐름의 결과로 `execution.strategy_execution_order id 40` 가 REQUESTED 상태 진입(상세 누적은 3차 작업 04 spec operation-notes 책임)
   2) 중복 주문 점검 — `connector.connector_order_request` 동일 `strategy_execution_order_id 40` 대상 기존 row 0건
   3) retry-normalizer 진입 검토 — `40580000` REJECTED 이력 없음 / 복구 조건 6종 미충족 / retry-normalizer 적용 0건(R-AUTO-022 mitigation 정합)
3. `allowPaperOrderExecute=true` approval true path 실행 + 결과: 완료
   1) Step Functions approval gate 통과 — Step 12 stdout 의 `PaperOrder: True` 라벨 정합(R-AUTO-019 mitigation Step Functions 측 정합 / OD-MS-029 신규 정합)
   2) `execution.strategy_execution_order id 40` 상태 `SUBMITTED` → `FILLED`
   3) `connector.connector_order_request id 48` 신규 생성 / request_type SELL / order_method MARKET / order_qty 17 / request_status `ACCEPTED` → `FILLED`
   4) broker_order_no `0000006143` 생성됨 / broker_branch_code 생성됨 — 본 노트 broker 응답 전문 평문 기록 0건 / `[REDACTED]` 처리 정합(R-DOCS-001 정합)
   5) rejection 없음 / KIS paper API 정상 응답 / 1차 시도 통과(2026-06-18 KIS paper read timeout / R-BROKER-004 회귀 0건 / 본 일자 retry-normalizer 진입 0건)

### 3. Step 13 `CONNECTOR_ORDER_CHECK` 체결 동기화

1. KIS `inquire-daily-ccld` 호출: 성공
   1) `connector_order_check.py` 2026-06-18 §2 패치(`--code` / `--order-no` / `--no-broad` 단건 direct-only 조회 기본 / OD-MS-025 정합) 그대로 사용
   2) 단건 active candidate(`282330` SELL 1건) 정합 — summary fallback guard 진입 없음(다건 active 상태에서 `output2 summary-only` 오매핑 위험 차단 / R-AUTO-018 mitigation 정합)
2. 체결 결과: 완료
   1) `connector.connector_order_request id 48` request_status `FILLED`
   2) `connector.connector_fill` 신규 row / side SELL / 종목 `282330` BGF리테일 / 정상 매핑(상세 fill_qty / fill_price / fill_amount / fill_ts 누적은 04 spec operation-notes 3차 작업 책임으로 분리 — 본 노트는 03 spec MarketConnector EC2 책임 영역 한정)
   3) `connector.connector_order_event` 신규 row 정합

### 4. Step 17 `BALANCE_REFRESH`

1. SSM 실행 결과: 완료
   1) SSM Status `Success` / ResponseCode `0` / StdErr empty
   2) wrapper 공통 MarketConnector env bootstrap 함수 호출 정합(2026-06-22 §1 / OD-MS-027 정합 / Step Functions 전환 이후에도 Step 17 진입 직전 bootstrap 호출 흐름 유지 — R-AUTO-021 [2026-06-23 보강] 정합)
   3) `marketconnector_app` 의 `legacy.holdings` search_path / 권한 보정(2026-06-17 §4) 이후 회귀 0건 / R-DATA-011 mitigation 그대로 유지
2. 최종 잔고 스냅샷: 확인
   1) `connector.connector_position_snapshot` 최신 row insert / 보유 4종목 정상 반영(Step 12 SELL 청산 정합)
   2) `282330` BGF리테일은 최신 잔고 스냅샷에서 제거 확인(Step 12 SELL 17주 청산 → `connector_position_snapshot` 최신 row 에서 보유 종목 4종으로 감소 / 2026-06-22 잔고 5종목 → 본 일자 4종목 차분 정합)
   3) `legacy.holdings` 결과 확인은 view_app 권한 부재로 운영자가 `portfolio_admin` 으로 우회 조회(R-DATA-005 [2026-06-22 보강] 정합 / 정식 GRANT 후속 유지)
3. KIS 호출 측 이벤트: 식별
   1) EGW00123 token 만료 후 재발급 동작 통과 확인 — 본 일자 Step 17 흐름에서 token 자동 재발급 경로 정상 동작
   2) EGW00215 rate limit 발생 사례 식별 — 본 일자 신규 R 부여 보류.
       - R-AUTO-001 / R-AUTO-015 / OD-SAFE-004 정합.
       - 자동 재시도 정책은 idempotent step 한정 그대로 유지.
       - Step 17 BALANCE_REFRESH 는 idempotent 분류.
       - Step 17 wrapper · Step Functions Step 17 state 측 backoff · retry policy 결정은 followups-overview 2026-06-23 §1 후속 phase 책임으로 분리.

### 5. DB 한글 저장 정상 확인 (connector 측 책임 테이블 한정)

1. 점검 대상: 완료
   1) `connector.connector_order_request` — 종목명 / 사유 등 한글 컬럼 저장 / 조회 정상
   2) `connector.connector_api_call_log` — KIS API 호출 메시지 한글 컬럼 저장 / 조회 정상
   3) `connector.connector_position_snapshot` — 보유 종목명 한글 컬럼 저장 / 조회 정상
   4) `execution.strategy_execution_order` 한글 저장 / 조회 정상 — 단, 본 테이블은 04 spec 책임 / 본 노트는 사실 인용만 / 상세 누적은 3차 작업 04 spec operation-notes 책임으로 분리
2. encoding 점검: 확인
   1) `SHOW server_encoding` = `UTF8`
   2) `SHOW client_encoding` = `UTF8`
   3) PGAdmin4 / `portfolio_admin` 세션 기준 위 4개 테이블 한글 정상 조회(SELECT 한정 / DDL · DML 0건)
3. 결론: 정정
   1) "DB 한글 깨짐" 이 아니라 일부 PowerShell / SSM / AWS CLI 콘솔 출력 표시 경로의 인코딩 이슈로 판단
   2) DB 리스크로 신규 등록하지 않음 / R-DATA 계열 신규 R 부여 0건
   3) 콘솔 출력 표시 경로 정리는 followups-overview 2026-06-23 §2 후속(wrapper run summary 출력 인코딩 통일 / SSM RunCommand 응답 본문의 한글 라벨 표시 / AWS CLI `--output text` 한글 컬럼 표시 / 2026-06-18 두 번째 세션 후속 메모와 결합) 책임으로 분리

### 6. 결정 / 리스크 변경 요약

1. 신규 결정: 완료
   1) OD-MS-028(Step 12 retry-normalizer 내장 정책 / 🟡 잠정 / 영향 spec 03 · 04 · 10 / 별도 Step 11.5 분리 아님 / 기존 Step 12 시작부 내장)
   2) OD-MS-029(Daily AWS Paper Step Functions approval workflow false / true path 운영 절차 / 🟢 확정 / 영향 spec 04 · 10 / 동일 state machine 입력 파라미터 분기)
2. 본문 변경 없는 결정: 1차 실증 메모 보강
   1) OD-MS-009 — Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask 의 Step Functions 측 운영자 실증 단계 진입(EventBridge Scheduler 정기 트리거 / 정식 production 진입은 여전히 후속 phase)
   2) OD-MS-027 — MarketConnector env bootstrap 재생성 운영 정책이 Step Functions Step 1 / 12 / 13 / 17 진입 직전에도 동일하게 호출되어야 한다는 점이 R-AUTO-021 [2026-06-23 보강] 으로 1차 실증
   3) OD-MS-016 / OD-MS-021 / OD-MS-023 — Strategy Execution / MarketConnector 책임 분리 / 17단계 안전 기준 / wrapper 운영 정책 본문 그대로 유지 / Step 12 retry-normalizer 는 MarketConnector 측 Step 12 내부 안전 보완으로만 분류 / Step Functions 전환 이후에도 동일 책임 경계 유지
   4) OD-SAFE-001 / OD-SAFE-004 — 자동 BUY · SELL E2E 단계적 도입 / 자동 재시도 금지 step 본문 그대로 유지 / Step Functions approval true path 진입은 운영자 명시 승인 시점에만 / Step 12 retry-normalizer 의 자동 복구는 idempotent step 의 통제된 복구 범위(조건 6종) 안에서만 동작
3. 신규 리스크: 완료
   1) R-AUTO-022(장종료 REJECTED · `40580000` 후 자동 재제출 경로 끊김 위험, Status `Mitigated` — Step 12 retry-normalizer 내장 + 복구 조건 6종으로 1차 차단 / 본 일자 dry-run 통과 / 실 retry 후보 발생 첫 회차 검증은 followups-overview 2026-06-23 §3 후속)
4. 본문 변경 없는 리스크: 보강 메모
   1) R-AUTO-021 — [2026-06-23 보강] Step Functions 전환 이후에도 Step 1 / 12 / 13 / 17 wrapper 공통 bootstrap 호출 흐름 유지 필요 / 정식 systemd unit + `EnvironmentFile` 등록까지 진입하기 전까지는 wrapper bootstrap 함수가 single source of truth / Status `Mitigated` 유지
   2) R-AUTO-018 — KIS `inquire-daily-ccld` summary-only fallback 오매핑 / 본 일자 Step 13 단건 active candidate(`282330` SELL 1건) 한정 / 다건 active 상태 진입 없음 / summary fallback guard 진입 0건 / mitigation 정합 회귀 0건
   3) R-AUTO-019 — `-AllowPaperOrderExecute` 의도하지 않은 시점 실주문 위험 / 본 일자 Step Functions approval true path 진입은 운영자 명시 승인(`allowPaperOrderExecute=true`) 시점 한정 / 의도하지 않은 옵션 사용 0건 / Step 12 stdout 의 `PaperOrder: True` 라벨 사후 검증 가능

### 7. 안전 / 보안 점검 결과 (2026-06-23)

1. broker / KIS 호출 = KIS paper SELL 1건(Step 12 Step Functions approval true path 본 실행) + balance · order check 조회성 한정.
   - BUY / 취소 / 정정 / 추가 `--execute` 호출 0건.
   - SELL position `mark_position_sell_ordered()` 호출은 MarketConnector executor SELL 성공 시점 책임(OD-MS-016 정합).
   - fill · position sync 자동 재시도 0건 / aws-live 작업 0건.
2. RDS DDL 0건. DML 은 Step Functions approval true path 정상 흐름 한정.
   - `connector.connector_order_request` insert(`id 48` ACCEPTED → FILLED)
   - `connector.connector_order_event` / `connector.connector_fill` insert(상세 row id 누적은 04 spec 책임)
   - `connector.connector_balance_snapshot` insert
   - `connector.connector_position_snapshot` insert(보유 4종목)

   본 일자 retry-normalizer 의 `execution.strategy_execution_order` UPDATE 적용 0건(현재 retry 후보 0건).
3. 아래 민감정보는 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.
   - 실제 secret value / KIS app key / KIS app secret / token / KIS paper login credential
   - 계좌번호 / 계좌 비밀번호
   - RDS password / RDS endpoint hostname
   - account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id
   - instance-id 본문 외 평문 / EIP / image digest full sha256 / task ARN / job ARN
   - broker_order_no 외 broker 응답 전문
   - Administrator password / Step Functions execution ARN
4. AWS / SSM / EC2 / RDS / Secrets Manager / SSM Parameter Store / Step Functions / KIS API 호출은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행.
   - AWS CLI / boto3 실행 0건
   - AWS 리소스 생성 / 수정 / 삭제 0건
   - `secretsmanager:GetSecretValue` 결과값 평문 기록 0건
   - CloudWatch Logs 본문 / SSM 응답 본문 / KIS API response body / Step Functions execution history 본문 / 운영자 PowerShell stdout 전문 평문 인용 0건
   - PGAdmin4 점검 SELECT 한정 / INSERT · UPDATE · DELETE · DDL 0건
5. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역).
   - 운영자 직접 patch 한 `port-marketconnector/connector_strategy_order_execute.py` 전체 교체 + Step 12 시작부 retry-normalizer 내장 변경분은 본 노트 §1 에 사실로만 기록.
   - 본문 전체 인용 0건 / 함수 시그니처 / SQL 본문 / patch diff 인용 0건 / R-DOCS-001 정합 / port-marketconnector 영역.
6. 아래 운영 식별자는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.
   - Step Functions state machine `portfolio-paper-daily-step1-17-approval`
   - `execution.strategy_execution_order id 40` / `connector.connector_order_request id 48`
   - `broker_order_no 0000006143`
   - 종목 코드 `282330` / 종목명 BGF리테일 / 수량 17 / 매도 방식 MARKET
   - Step Functions approval gate 라벨 `allowPaperOrderExecute=false` · `allowPaperOrderExecute=true`
   - wrapper stdout 라벨 `PaperOrder: True`
   - EC2 instance id `i-0fce77927b7397b88`(OD-NET-010 정합 / 본 일자 이전 spec 산출물에 이미 사실 기록)
   - signal_date · run_date `2026-06-23`
   - `server_encoding` · `client_encoding` 값 `UTF8`
   - KIS error code `EGW00123` · `EGW00215`



## 2026-06-30 (오후) — 장중 손절 Slack 실 연동(MarketConnector evaluate 교체 배포 + 장중 runner `--create-order --notify-slack` 연결 + EC2 IAM `lambda:InvokeFunction` 부여 + 1회 안전 검증 통과)

본 일자 오후 추가 작업분으로 운영자가 직접 수행한 장중 손절 Slack 실 연동 결과를 MarketConnector EC2 운영 관점에서 누적 기록한다. 본 노트는 `connector_intraday_position_evaluate.py` 본문 / runner ps1 본문 / Lambda 코드 본문 / IAM Policy 전체 본문 / SSM 응답 본문 / Slack 메시지 본문 / CloudWatch Logs 전문 평문 인용 0건(R-DOCS-001 정합).

1. MarketConnector evaluate 교체 배포: 완료
 1) 대상 파일 / 배포 식별자
   (1) 파일 경로
       - `/home/ec2-user/apps/port-marketconnector/src/connector_intraday_position_evaluate.py`
       - 운영자 직접 교체 배포
   (2) 배포 버전 / SHA256
       - 배포 버전 `connector-intraday-position-evaluate-1.1.1-slack-notify`
       - 배포 SHA256 `5ec6914853ab34e200682f256de53693f972b3e5d337a5bd8ab8ebf5296230ed`
   (3) 신규 CLI option 3종 추가
       - `--notify-slack`
       - `--slack-function-name`
       - `--slack-region`
   (4) 정적 검증
       - `.venv/bin/python` 기준 `py_compile` 통과
       - `--help` 출력 검증 통과

2. 장중 runner 갱신: 완료
 1) 대상 파일 / 식별자
   (1) runner 경로
       - `/home/ec2-user/apps/port-marketconnector/scripts/run_intraday_snapshot_and_evaluate.sh`
       - 운영자 직접 갱신
   (2) backup / SHA256
       - backup 이름 `run_intraday_snapshot_and_evaluate.sh.bak.20260630T112255Z.create-order-notify-slack`
       - runner SHA256 `8fe7657a75b5d7637ec645b6d8a55bf75c993d46c1c71e8a5e88bded29baa8a0`
   (3) 변경 사항
       - 기존 evaluate 호출에 `--create-order` 추가
       - 기존 evaluate 호출에 `--notify-slack` 추가
       - runner 문법 검증 통과

3. MarketConnector EC2 IAM 권한 부여: 완료
 1) Instance Role / inline policy
   (1) IAM Role 이름 `portfolio-paper-marketconnector-ec2-role`
   (2) inline policy 이름 `portfolio-paper-marketconnector-event-notifier-invoke`
   (3) action `lambda:InvokeFunction`
   (4) Resource = `portfolio-event-notifier` Lambda 한정 / Resource · Action wildcard 0건
   (5) OD-SEC-005 / OD-SEC-006 정합
 2) EC2 invoke smoke
   (1) EC2 측에서 Notifier Lambda invoke smoke 성공
   (2) Lambda 응답 본문 평문 인용 0건

4. 실제 runner 1회 안전 검증: 완료
 1) SSM commandId / 결과
   (1) commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131`
   (2) snapshot refresh 성공
   (3) evaluate 실행 성공
   (4) source_version `connector-intraday-position-evaluate-1.1.1-slack-notify` 확인
   (5) `create_order=True` 확인
   (6) `notify_slack=True` 확인
   (7) `open_position_count=0` 확인
   (8) `EMPTY_NORMAL` 확인
   (9) `INTRADAY_SNAPSHOT_AND_EVALUATE=SUCCESS` 확인
   (10) runner exit code 0 확인
   (11) OPEN position 0건 상태라 check / order / slack 없이 정상 종료

5. DB after-check: 완료
 1) marker / count
   (1) marker `STEP19C_INTRADAY_STOP_FINAL_DB_AFTER_CHECK=SUCCESS`
   (2) `TODAY_INTRADAY_CHECKS` count 0
   (3) `TODAY_INTRADAY_STOP_EXECUTION_ORDERS` count 0
   (4) `ACTIVE_INTRADAY_STOP_EXECUTION_ORDERS` count 0
   (5) `TODAY_INTRADAY_STOP_CONNECTOR_ORDERS` count 0
   (6) `TODAY_INTRADAY_STOP_CONNECTOR_ORDER_ROWS` 0 rows
 2) balance snapshot
   (1) latest balance snapshot id `281`
   (2) `as_of_date=2026-06-30`
   (3) `as_of_ts=2026-06-30 11:23:34.973842+00`
   (4) `total_eval_amount=8,706,505`원
   (5) `cash_balance=8,706,505`원
   (6) `source_version=connector-intraday-snapshot-refresh-1.0.0`
   (7) PSQL exit code 0
 3) DB 검증 쿼리 작성 원칙
   (1) `information_schema.columns` 사전 확인 후 작성된 컬럼만 사용
   (2) 추정 컬럼명 사용 0건
   (3) `.kiro/AGENTS.md` "운영 명령 작성 규칙(추가)" 규칙 2 · 4 정합

6. 결정 / 리스크 매핑 (2026-06-30 오후 장중 손절 Slack)
 1) 결정 본문 변경 없음
   (1) OD-MS-001(port-marketconnector 컴퓨트 = EC2+EIP) / OD-MS-016(Strategy Execution / MarketConnector 책임 분리) / OD-MS-035(장중 포지션 확인 3단계 구조) / OD-MS-036(Intraday Stop Sell Submit Workflow) / OD-MS-030(AWS 공통 Slack notifier Lambda) / OD-MS-038(Daily Brief Slack mini workflow) 본문 변경 없이 evidence 보강
   (2) 자세한 결정 변경은 `../_common/operator-decisions.md` Change Log `2026-06-30 (오후) 장중 손절 Slack` 항목 참조
   (3) 신규 결정 없음 / Decision Summary 카운트 변경 없음(전체 97 / 확정 52 / 잠정 42 유지)
 2) 리스크 매핑
   (1) R-AUTO-036 신규 — 장중 손절 READY 생성 후 Slack 발송 실패 시 rollback 없는 정책의 부작용 위험 / Status `Mitigated`
   (2) R-AUTO-035 [2026-06-30 오후 추가 보강] — Notifier Lambda 호출 진입점이 MarketConnector EC2 runner 까지 확대 / Status `Mitigated` 그대로 유지
   (3) R-AUTO-024(Slack webhook URL 평문 노출 위험 / `Accepted`) mitigation 그대로 유지

7. 후속 (03 spec 후속 phase 책임 또는 04 / 06 spec 후속 phase 책임)
 1) 실제 보유 종목 발생 후 hard stop 조건 충족 시 `INTRADAY_STOP_LOSS` Slack 실이벤트 수신 확인
 2) 실제 보유 종목 발생 후 `INTRADAY_STOP_SELL` READY 생성 + approval gate 차단 상태 재확인
 3) Slack 메시지에 계좌 / 현재가 / 진입가 / 예상손익금액 추가 여부 검토(03 spec 후속 phase 책임)
 4) 장중 손절 READY 생성 후 별도 approval summary Slack 추가 여부 검토(04 spec 후속 phase 책임)
 5) `portfolio-paper-intraday-stop-sell-approval` state machine 자동 ENABLE 진입 운영자 별도 승인 후 후속(04 spec 후속 phase 책임 / OD-MS-035 / OD-MS-036 / R-AUTO-030 ~ R-AUTO-032 / R-BROKER-005 정합)
 6) Slack webhook URL Secrets Manager 또는 SSM SecureString 이전(R-AUTO-024 / 06 spec 후속 phase 책임)
 7) R-AUTO-036 운영 detection 자동화(05 · 06 spec 후속 phase 책임)

8. 본 일자 사실 기록 범위 (2026-06-30 오후)
 1) Kiro 작업 = 03 spec `operation-notes.md` 본 섹션 누적만 수행
 2) 운영자 직접 수행 영역 = `connector_intraday_position_evaluate.py` 교체 배포 + 장중 runner 갱신 + EC2 IAM inline policy 부여 + EC2 invoke smoke + 1회 실제 runner 안전 검증
 3) AWS CLI / boto3 / psql / Lambda 실행 / Step Functions 실행 / SSM RunCommand / 외부 API 호출 본 일자 Kiro 측 변경 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 본 일자 Kiro 측 변경 0건
 4) `connector_intraday_position_evaluate.py` 본문 / runner ps1 본문 / IAM Policy 전체 본문 / Lambda 코드 본문 / Step Functions ASL 본문 / SSM 응답 본문 / Slack 메시지 본문 / CloudWatch Logs 전문 평문 인용 0건(R-DOCS-001 정합)
 5) 아래 민감정보는 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder
       - secret value / KIS app key / KIS app secret / token
       - 계좌번호 12자리 원문 / DB password / Administrator password
       - RDS password / RDS endpoint hostname
       - account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / 실제 state machine ARN
       - Slack webhook URL / EIP / public IP / broker_order_no 원문
 6) 아래 운영 식별자만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님
       - 파일 경로 / 배포 SHA256 / 배포 버전 / CLI option 라벨
       - runner SHA256 / runner backup 이름 / SSM commandId
       - IAM Role 이름 / inline policy 이름 / Lambda 이름 / state machine 이름
       - Slack 이벤트 라벨 / 문구 라벨
       - source_version 2종 / marker 이름 3종 / table count 라벨 5종
       - balance snapshot id · as_of_date · as_of_ts · 금액 요약
