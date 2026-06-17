# Validation Checklist — 03-marketconnector-ec2

[`./runbook.md`](./runbook.md) 의 [확인] 결과를 4종 라벨로 기록하는 최소 체크리스트.

라벨

- `[O]` 통과
- `[X]` 미충족 / 실패
- `[Kiro 후속 작업 필요]` Kiro 가 추가로 만들거나 채울 산출물 보류
- `[운영자 확인 필요]` 운영자만 직접 확인 가능

원칙

- 실제 secret value, KIS app key / app secret, 계좌번호, RDS endpoint hostname, RDS password, account-id, 실제 secret ARN, IAM access key id, instance-id, EIP 평문 기록 금지. 모두 `[REDACTED]` 또는 placeholder.
- `GetSecretValue` 실호출은 운영자만. Kiro 자동 검증은 `DescribeSecret` / `GetParameter` metadata 만.

## 1. Python / venv / 의존 라이브러리

| 항목 | 결과 | 근거 | 비고 |
|------|------|------|------|
| Python `3.9.25` 설치 | [O] | 2026-06-10 운영자 직접 확인 | (§4.1) |
| venv 1개 구성 | [O] | 2026-06-10 운영자 직접 확인 | path / name placeholder |
| `requests` / `flask` / `psycopg2-binary` / `psycopg` / `pandas` 5종 설치 | [O] | 2026-06-10 운영자 직접 확인 | (§4.2) |
| 8개 MS 패키징 파일(`requirements.txt` 등) 미수정 | [O] | 본 spec 안전 제약 | (R14.1) |

## 2. PostgreSQL client / pg_restore / psql 18.4

| 항목 | 결과 | 근거 | 비고 |
|------|------|------|------|
| `psql --version` 출력 major 18 / full 18.4 이상 | [운영자 확인 필요] | 운영자 EC2 shell | (§5) |
| `pg_dump --version` 출력 major 18 / full 18.4 이상 | [운영자 확인 필요] | 동상 | |
| `pg_restore --version` 출력 major 18 / full 18.4 이상 | [운영자 확인 필요] | 동상 | 6/9 RDS restore 사용 client 그대로 |
| client 18.4 미만 다운그레이드 0건 | [O] | 본 spec 다운그레이드 금지 정책 | (§5.2) |

## 3. `marketconnector_app` RDS 접속

| 항목 | 결과 | 근거 | 비고 |
|------|------|------|------|
| `marketconnector_app` 사용자 SELECT 1 통과 | [O] | 2026-06-10 운영자 직접 확인 | endpoint / password 미기록 |
| RDS Public access = `No` | [운영자 확인 필요] | RDS Console | 02 spec 정합 |
| SG inbound: `sg-marketconnector-ec2` → `sg-rds-postgres` 5432 만 허용 | [운영자 확인 필요] | EC2 Console | (§6.1) |
| 본 spec 시점 RDS DDL/DML 호출 0건 | [O] | 본 spec 안전 제약 | (R14, §6.5) |

## 4. Connector / Flask 조회성 smoke test

| 항목 | 결과 | 근거 | 비고 |
|------|------|------|------|
| `connector_balance.py` 잔고 조회 통과 | [O] | 2026-06-10 운영자 직접 확인 / [2026-06-17 재검증] 통과 | (§10 (d)) |
| `connector_order_check.py` 주문 / 체결 조회 통과 | [O] | 2026-06-10 운영자 직접 확인 / [2026-06-17 재검증] 통과 | (§10 (e)) |
| Flask 내부 smoke test (`/api/v1/view/...` 조회성 endpoint) 통과 | [O] | 2026-06-10 운영자 직접 확인 | (§10 (f)) |
| 운영 가능 후보(`connector_quote_realtime.py` / `connector_quote_closed.py` / `connector_view_service.py`) 검증 | [Kiro 후속 작업 필요] | 후속 phase 책임 | (§7.3, §7.4) |
| `CONNECTOR_DEBUG=true` 운영 노출 0건 | [운영자 확인 필요] | Flask startup 로그 | 정상 운영 모드 `false` 강제(§8.2) |
| [2026-06-17] `CONNECTOR_BALANCE` SSM RunCommand 재검증 | [O] | 2026-06-17 운영자 직접 확인(SSM RunCommand) | `connector_balance_snapshot` 최신 `as_of_date 2026-06-17` / `source_api inquire-balance` / `source_version connector-balance-1.0.0` / 보유종목 0건 정상(runbook §4.1) |
| [2026-06-17] `CONNECTOR_ORDER_CHECK` SSM RunCommand 재검증(MarketConnector 조회계열 선행 검증) | [O] | 2026-06-17 운영자 직접 확인(SSM RunCommand) | KIS `inquire-daily-ccld` `response_status=200` / `response_code=0` / `is_success=true` / `called_at 2026-06-17 00:51:03 UTC` / row count `connector_order_request 33` / `connector_order_event 18` / `connector_fill 13` / 신규 0건은 본 일자 신규 주문·체결 미발생 정상 판단(runbook §4.2) |

## 5. Secrets Manager / SSM env 주입

| 항목 | 결과 | 근거 | 비고 |
|------|------|------|------|
| Secrets Manager 4건 metadata 정상 (`describe-secret`) | [운영자 확인 필요] | 운영자 EC2 shell | KIS app key / secret / paper-account / rds/marketconnector-app |
| 기존 `/portfolio/paper/rds/master` 유지 | [운영자 확인 필요] | 02 spec 정합 | 이름 변경 / 삭제 0건 |
| SSM Parameter 6건 정상 (`get-parameters-by-path /portfolio/paper/marketconnector`) | [운영자 확인 필요] | 운영자 EC2 shell | base-url / connector-host·port·debug / environment / broker-name |
| 환경변수 매핑(`APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD` / `INTEREST_DB_*` / `BASE_URL` / `PORT_*` / `CONNECTOR_*`) 주입 통과 | [O] | 2026-06-10 운영자 직접 확인 / [2026-06-17 재검증] 통과(v5 패턴) | (§10 (g), §8.2) |
| 임시 export 스크립트 secret 평문 저장 0건 | [운영자 확인 필요] | EC2 shell file 점검 | 권한 700 권고 |
| `GetSecretValue` 자동 호출 0건 (Kiro 측) | [O] | 본 spec 안전 제약 | 운영자만 수행 |
| [2026-06-17] JSON SecretString 내부 key 추출 + `KIS_*` alias 동시 export 검증(v5 패턴) | [O] | 2026-06-17 운영자 직접 확인 | `kis-app-key` / `kis-app-secret` / `paper-account` JSON SecretString 내부 key(`APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD`) 추출 후 `APP_*` 호환 key + `KIS_*` alias 동시 export. JSON dict 전체를 환경변수 값으로 export 한 1차 실패는 v5 패턴 보정으로 해소(design.md §8.2.1 / §8.2.2 / runbook.md §2 정합) |
| [2026-06-17] secret value / 계좌번호 / token 평문 기록 0건 | [O] | 2026-06-17 운영자 직접 확인 | secret name path / JSON shape / value length / key presence 만 기록(R-DOCS-001 정합 / 보안 정책) |

## 6. Instance Role / Access Key 미사용

| 항목 | 결과 | 근거 | 비고 |
|------|------|------|------|
| `aws sts get-caller-identity` 결과 `assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>` | [O] | 2026-06-10 운영자 직접 확인 | (§9.4) |
| `aws configure list` access_key Source = `iam-role` 또는 `Ec2InstanceMetadata` | [O] | 2026-06-10 운영자 직접 확인 | |
| `~/.aws/credentials` 미존재 | [O] | 2026-06-10 운영자 직접 확인 | |
| `~/.aws/config`, dotfile, systemd `EnvironmentFile=` 안 access key 패턴 0건 | [운영자 확인 필요] | EC2 shell grep | (§9.5) |
| `AmazonSSMManagedInstanceCore` attach 상태 | [운영자 확인 필요] | IAM Console | (§9.2) |
| `CloudWatchLogsWrite` 권고 옵션(log group 사전 생성, `CreateLogGroup` 제외) 적용 | [Kiro 후속 작업 필요] | 운영자 직접 작업 후 갱신 | (§9.2) |

## 7. 신규 주문 / 매수 / 매도 / 취소 / 정정 호출 0건

| 항목 | 결과 | 근거 | 비고 |
|------|------|------|------|
| `connector_buy.py` 미실행 | [O] | 본 spec 안전 제약 | (R14, §7.3) |
| `connector_sell.py` 미실행 | [O] | 동상 | |
| `connector_cancel.py` 미실행 | [O] | 동상 | |
| `connector_modify.py` 미실행 | [O] | 동상 | |
| Flask 신규 주문 endpoint(`/api/v1/buy|sell|cancel|modify/...`) 호출 0건 | [O] | 본 spec 안전 제약 | runbook §5 정합 |
| RDS DDL/DML 0건 | [O] | 본 spec 안전 제약 | (§6.5) |
| [2026-06-17] 신규 주문 / `--execute` / aws-live 작업 0건 재검증 | [O] | 2026-06-17 운영자 직접 확인 | 본 일자 실행은 `connector_balance.py` / `connector_order_check.py` 조회성 단건만. `connector_buy.py` / `connector_sell.py` / `connector_cancel.py` / `connector_modify.py` 미실행 / `--execute` 0건 / aws-live 작업 0건 / `connector_order_event` / `connector_fill` 신규 row 0건 = 정상(runbook §4.2 / OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004 정합) |
| [2026-06-17] (Daily AWS 17-step E2E) Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` 본 실행 — KIS paper BUY 4건 제출 성공 | [O] | 2026-06-17 운영자 직접 확인 / operation-notes 2026-06-17 (Daily AWS 17-step E2E 완료) §2 정합 | `execution_order` id `26 ~ 29` SUBMITTED / `connector_order_request` id `34 ~ 37` 생성 / `broker_order_no 0000035906` / `0000035912` / `0000035918` / `0000035932` / SELL · 취소 · 정정 호출 0건 / aws-live 작업 0건. `connector_strategy_order_execute.py` MarketConnector EC2 정식 배포 + venv python 사용 + `execution` table UPDATE 권한 보정 + `source_run_id` fallback 패치 후 통과(R-AUTO-009 / R-AUTO-010 [2026-06-17 보강] 정합) |
| [2026-06-17] (Daily AWS 17-step E2E) Step 13 `CONNECTOR_ORDER_CHECK` 본 실행 — `output1 empty` + `output2 aggregate summary` 응답 형태 식별 + summary fallback guard 패치 후 broker_order_no 별 단건 조회 통과 | [O] | 2026-06-17 운영자 직접 확인 / operation-notes 2026-06-17 (Daily AWS 17-step E2E 완료) §3 정합 | 1차 응답이 `output1 empty` + `output2 summary` 로 마지막 주문 row 에 잘못 매핑된 사례 즉시 식별 + 잘못 생성된 `connector_order_event` / `connector_fill` 삭제 + `connector_order_request` 상태 복구 + `connector_order_check.py` summary fallback guard 패치 적용(active 후보 정확히 1건일 때만 fallback 허용 / 다건이면 event · fill · status 변경 금지) + `broker_order_no` 별 단건 조회로 4건 모두 정상 동기화(`connector_order_request 34 ~ 37` FILLED / `connector_fill 26 ~ 29` 생성). R-AUTO-018 신규 mitigation 1차 실증 |
| [2026-06-17] (Daily AWS 17-step E2E) Step 17 `BALANCE_REFRESH` 본 실행 — `marketconnector_app` legacy 권한 / search_path 보정 후 재실행 통과 | [O] | 2026-06-17 운영자 직접 확인 / operation-notes 2026-06-17 (Daily AWS 17-step E2E 완료) §4 정합 | 1차 실패 = bare `holdings` `relation does not exist`(legacy schema USAGE / `legacy.holdings` DML / sequence / database search_path 누락) → 운영자가 search_path 를 `connector, execution, legacy, reference, public` 로 보정 + USAGE / DML / sequence GRANT + default privileges 보정 후 SSM 재실행 `Status Success` / `ResponseCode 0` / `connector_position_snapshot` 4종목 최신 생성(`position_snapshot_id 120 ~ 123` / quantity `52 / 65 / 244 / 17` / avg_buy_price `28980.77 / 27043.08 / 5744.41 / 120182.35`). R-DATA-011 신규 mitigation 1차 실증 |

## 8. 후속 인계

- [Kiro 후속 작업 필요] [`./operation-notes.md`](./operation-notes.md) 에 본 체크리스트 결과(일자 / 성공·실패) 누적 기록.
- [Kiro 후속 작업 필요] [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 에 OD-SEC-005 / OD-SEC-006 잠정→확정 후보 + `AmazonSSMManagedInstanceCore` 사용 정책 신규 후보 반영(운영자 승인 시).
- [Kiro 후속 작업 필요] [`../_common/risk-register.md`](../_common/risk-register.md) 에 R-DATA / R-CAP / R-BROKER / R-SEC / R-AUTO 5건 다음 가용 ID 로 등록(운영자 승인 시).
- [Kiro 후속 작업 필요] [`../_common/followups-overview.md`](../_common/followups-overview.md) 에 03 1차 적용 환경 / 1차 범위 / 04·05·08·09·10 인계 반영.
- [운영자 확인 필요] 04 / 05 / 08 / 09 spec 진입 시 본 spec §13 EC2 → ECS 매핑을 입력으로 받는다.
