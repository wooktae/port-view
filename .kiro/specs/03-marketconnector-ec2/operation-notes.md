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

본 섹션은 같은 일자 앞 섹션(`## 2026-06-13 SSM Port Forwarding 표준 경유지 역할 1차 검증`) §1 ~ §5 의 후속이며, 본 EC2 가 SSM Port Forwarding 표준 경유지 역할을 하는 동안 추가 client 2종(로컬 PostgreSQL 18 `psql.exe` + pgAdmin4) 으로도 AWS Paper RDS 접속이 1차 실증되었음을 누적 기록한다. 자세한 결과는 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 보강(psql 18 client + pgAdmin4 접속 검증) 섹션 참조.

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

## 후속 필요

- [`./runbook.md`](./runbook.md) §3 ~ §6 결과 반영 → 운영자 EC2 shell 작업 시점에 본 노트 일자별 누적 갱신.
- [`./validation-checklist.md`](./validation-checklist.md) §2 / §4 / §5 / §6 의 `[운영자 확인 필요]` 항목 점검 후 결과 반영.
- [`./validation-checklist.md`](./validation-checklist.md) §4 운영 가능 후보 entrypoint(`connector_quote_realtime.py` / `connector_quote_closed.py` / `connector_view_service.py`) 검증 → 후속 phase 또는 별도 phase.
- [`./validation-checklist.md`](./validation-checklist.md) §6 `CloudWatchLogsWrite` 권고 옵션(log group 사전 생성 + `CreateLogGroup` 제외) 적용 → 운영자 직접 작업 후 갱신.
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 갱신 후보: OD-SEC-005 / OD-SEC-006 잠정 → 확정 후보, `AmazonSSMManagedInstanceCore` 사용 정책 신규 후보 (운영자 승인 시).
- [`../_common/risk-register.md`](../_common/risk-register.md) 갱신 후보: R-DATA / R-CAP / R-BROKER / R-SEC / R-AUTO 5건 (다음 가용 ID 부여 후 운영자 승인 시).
- [`../_common/followups-overview.md`](../_common/followups-overview.md) 갱신 후보: 03 1차 적용 환경 / 1차 범위 / 범위 밖 / 04 / 05 / 08 / 09 / 10 spec 인계.
- 정상 운영 모드(systemd unit 또는 startup script) 전환 = 본 spec 후속 task 또는 별도 phase 책임. 본 시점에는 임시 검증 단계(shell + 임시 export 스크립트) 유지.

## IAM 변경 기록 템플릿 (필요 시 일자별 추가)

```
## YYYY-MM-DD IAM 변경
- 변경 일자: YYYY-MM-DDTHH:MM:SS+09:00
- 변경자: [운영자 식별자]
- 변경 사유: [한 줄]
- 변경 전 / 후 항목 요약: [추가·삭제 statement 수, Action·Resource 변경 요약 — JSON 본문 전체 인용 금지]
```
