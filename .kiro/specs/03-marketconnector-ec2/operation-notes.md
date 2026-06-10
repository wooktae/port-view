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
