# Design Document — 03-marketconnector-ec2

## Introduction

본 spec 의 핵심은 한 줄로 요약한다.

> **이미 생성된 MarketConnector EC2 가 02 / 06 spec 의 1차 적용 결과를 입력으로 받아, KIS Connector(Flask) / `marketconnector_app` 기반 RDS 접속 / Secrets Manager·SSM Parameter Store env 주입 / Instance Role 기반 Access Key 미사용 운영 형태로 정식 전환되도록 절차 / 검증 / 운영 노트 작성 기준을 확정하고, 2026-06-10 검증 결과 8건을 산출물에 누적 기록한다.**

관련 spec: [`../02-aws-network-and-rds`](../02-aws-network-and-rds) / [`../06-secrets-and-iam`](../06-secrets-and-iam)

- 1차 적용 환경: `aws-paper` / region `ap-northeast-2` / 1차 적용 대상: MarketConnector EC2 1대.
- 본 spec 의 입력:
  - [`./requirements.md`](./requirements.md) R1 ~ R14
  - [`../02-aws-network-and-rds/`](../02-aws-network-and-rds/) 1차 적용 결과(VPC / Subnet / SG / VPC Endpoint 5종 / RDS PostgreSQL / DB role 7종)
  - [`../06-secrets-and-iam/`](../06-secrets-and-iam/) 1차 적용 결과(Secrets Manager 4건 / SSM Parameter Store 6건 / Instance Role + Profile + read-only Policy / Access Key 미사용 원칙)
- 본 spec 의 산출물: 본 phase 에서는 [`./design.md`](./design.md) 한 개.
  - [`./tasks.md`](./tasks.md) / [`./runbook.md`](./runbook.md) / [`./validation-checklist.md`](./validation-checklist.md) / [`./operation-notes.md`](./operation-notes.md) 는 후속 phase. R14.7 근거.
- 본 spec 의 범위 밖(반드시 명시). R14 근거.
  - EC2 신규 생성
  - 신규 주문 / 매수 / 매도 / 취소 / 정정 호출
  - live rotation 자동화
  - GitHub Actions OIDC / CI/CD Role(07 spec)
  - 8개 MS 전체 full IAM 매트릭스(04 / 05 / 08 / 09 분담)
  - aws-live IAM(10 spec)
  - 8개 MS 의 README / AGENTS.md / CHANGELOG / docs / worklog 와 소스 코드 / 패키징 파일 수정

본 문서에는 아래 실제 값을 절대 적지 않는다. 모두 `[REDACTED]` 또는 placeholder(`<account-id>`, `<region>`, `<instance-id>`, `<eip>`, `<rds-endpoint>`, `<venv-path>`, `<venv-name>`, `<ebs-size>`, `<instance-type>`) 만 사용한다. R14.4 근거.

- 실제 secret value / RDS password / 토큰 / Slack webhook URL
- KIS app key / KIS app secret / 계좌번호
- RDS endpoint hostname / account-id
- 실제 secret ARN / IAM access key id
- instance-id / EIP / EBS volume id

## 1. 범위 / 범위 밖 (R1)

### 1.1 범위 안 (R1.2 근거)

| 항목 | 설명 |
|------|------|
| EC2 OS | Amazon Linux 2023 (사전 완료) |
| Python 실행환경 | Python 3.9.25 + venv 1개 |
| 1차 의존 라이브러리 | `requests`, `flask`, `psycopg2-binary`, `psycopg`, `pandas` (5종) |
| PostgreSQL client | client major 18 / full 18.4 |
| pg_restore | major 18 / full 18.4 |
| KIS Connector(Flask) | `connector_app.py` 기반 정상 운영 형태 진입 가능성 검증 |
| RDS 접속 | `marketconnector_app` 사용자 기준 private RDS 접속 |
| Secrets / SSM env 주입 | 06 §1 / §2 / §3 결과를 입력으로 환경변수 매핑 (06 인계) |
| Access Key 미사용 | IMDSv2 + Instance Role 자격증명만 사용 (06 인계) |
| 6/10 검증 결과 8건 | 산출물 누적 기록 |
| 운영자 절차 / 검증 / 노트 작성 기준 | runbook / validation-checklist / operation-notes 후속 phase |

### 1.2 범위 밖 (R1.3 근거)

| 항목 | 분담 spec / 사유 |
|------|------------------|
| EC2 신규 생성 | 02 spec 시점에 RDS restore runner 로 이미 생성 완료 (사전 완료) |
| 신규 주문 / 매수 / 매도 / 취소 / 정정 호출 | 본 spec 모든 phase 에서 0건. 조회성 smoke test 만 |
| live rotation 자동화 | 06 spec 범위 밖과 동일 |
| GitHub Actions OIDC / CI/CD Role | 07 spec |
| 8개 MS 전체 full IAM 매트릭스 | 04 / 05 / 08 / 09 / 10 spec 분담 |
| aws-live IAM 매트릭스 | 10 spec 통합 |
| 8개 MS 코드 / docs 수정 | R14.1 안전 제약 |

### 1.3 후속 spec 인계 항목 (R1.4 근거)

| 후속 spec | 인계 항목 | 형태 |
|-----------|-----------|------|
| 04 strategy-batch-stepfunctions | EC2 운영 패턴 → ECS Task Role 패턴 매핑 (§13) | Task Role 골격 + `/portfolio/paper/strategy/*` service prefix |
| 05 port-view-ecs-and-runbook | 동상 | Task Role 골격 + `/portfolio/paper/view/*` service prefix |
| 08 interest-crawler-and-preprocessor-ecs | 동상 | Task Role 골격 + `/portfolio/paper/crawler/*`, `/portfolio/paper/preprocessor/*` |
| 09 strategy-research-batch | 동상 | Task Role 골격 + `/portfolio/paper/research/*` |
| 10 cutover-and-validation-runbook | aws-live `/portfolio/live/...` prefix 통합 | env prefix 분리 + Role 권한 매트릭스 락 |

### 1.4 phase 분리 정책 (R1.5 근거)

본 phase(design) 시점에는 [`./design.md`](./design.md) 한 개만 작성한다. tasks / runbook / validation-checklist / operation-notes 는 후속 phase 의 책임이며 본 phase 에서 생성하지 않는다.

## 2. EC2 사전 완료 (R2)

### 2.1 사전 완료 항목 (R2.1 / R2.3 근거)

| 항목 | 가정값 | 본 design 표기 |
|------|--------|---------------|
| EC2 OS | Amazon Linux 2023 | 명시 |
| Subnet 배치 | public subnet | 02 spec 결정(`public-a`) 입력 |
| EIP | attach + running | placeholder `<eip>` |
| 기본 SG | `sg-marketconnector-ec2` ↔ `sg-rds-postgres` 5432 inbound (02 spec 매트릭스) | 명시 |
| IAM Role | `portfolio-paper-marketconnector-ec2-role` (06 spec §4.1) | 이름 명시 |
| IAM Instance Profile | `portfolio-paper-marketconnector-ec2-profile` (06 spec §4.1) attach 완료 | 이름 명시 |

### 2.2 식별자 placeholder 정책 (R2.2 근거)

본 design 은 EC2 인스턴스 타입, EBS 볼륨 크기, instance-id, EIP 값, 실제 account-id 를 적지 않는다. 모든 자리에 placeholder(`<instance-type>`, `<ebs-size>`, `<instance-id>`, `<eip>`, `<account-id>`) 또는 `[REDACTED]` 만 사용한다.

### 2.3 미변경 항목 (R2.4 근거)

| 항목 | 정책 |
|------|------|
| EC2 신규 생성 | **금지** |
| 인스턴스 타입 변경 | **금지** |
| EBS 재생성 / 볼륨 교체 | **금지** |
| public subnet 변경 | **금지** |
| EIP detach 또는 교체 | **금지** |

### 2.4 mismatch 시 처리 흐름 (R2.5 근거)

운영자 점검 시점에 사전 완료 항목 한 가지가 본 design 가정과 다르게 발견되는 경우, 본 spec 임의 변경 금지. 처리 흐름은 다음과 같다.

작업 일시 중지(suspend) → [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-MC 또는 OD-NET 카테고리에 변경 제안만 기록 → 운영자 승인 / 거절 → 본 design 갱신 후 작업 재개.

## 3. 운영자 인수 사실 (R3)

### 3.1 일자별 인수 사실 (R3.1 / R3.3 근거)

| 일자 | 사용 형태 | 입력 | 결과 |
|------|-----------|------|------|
| 2026-06-09 | RDS restore runner | local PostgreSQL `pg_dump` → S3 임시 bucket → EC2 → private RDS `pg_restore` 18.4 | schema 10개 생성 / role 7개 생성 / 핵심 테이블 row count match / `pg_restore` exit code 0 |
| 2026-06-10 | MarketConnector 운영 전환 | 06 spec 1차 적용 결과(Secrets 4건 + SSM 6건 + Instance Role + read-only Policy) | 8건 검증 모두 성공(§10) |

### 3.2 RDS private endpoint 정책 (R3.2 근거)

본 RDS 는 `Publicly accessible = No` 로 운영되며, 같은 VPC 안의 EC2 에서만 접속 가능하다(R-NET-004 mitigation 입력). 본 EC2 가 같은 VPC 의 public subnet 에 배치되어 있고 SG 매트릭스(`sg-marketconnector-ec2` → `sg-rds-postgres` 5432) 가 적용된 상태에서만 접속이 성립한다.

### 3.3 표기 정책 (R3.4 / R3.5 근거)

본 design 의 일자별 인수 사실 본문에는 실제 dump 파일 경로 / S3 bucket 이름 / RDS endpoint hostname / 실제 EIP / 실제 instance-id / 실제 account-id / 비밀번호 / 토큰 을 평문으로 적지 않는다. 모두 placeholder 또는 `[REDACTED]` 만 사용한다. operation-notes.md(후속 phase) 가 일자별 누적 기록 시점에도 동일 정책을 따른다.

## 4. Python / venv / 의존 (R4)

### 4.1 Python 실행환경 (R4.1 / R4.2 근거)

| 항목 | 본 spec 결정 |
|------|--------------|
| Python 버전 | `3.9.25` |
| venv 개수 | 1개 |
| venv 경로 | placeholder `<venv-path>` |
| venv 이름 | placeholder `<venv-name>` |

### 4.2 1차 의존 라이브러리 (R4.3 근거)

| 패키지 | 용도 | 본 spec 시점 결과 |
|--------|------|------------------|
| `requests` | KIS HTTP client | 설치 성공 |
| `flask` | Connector API | 설치 성공 |
| `psycopg2-binary` | RDS 접속(주 사용) | 설치 성공 |
| `psycopg` | RDS 접속(보조) | 설치 성공 |
| `pandas` | 시세 / 잔고 처리 | 설치 성공 |

### 4.3 추가 의존 / MS 코드 미수정 (R4.4 / R4.5 근거)

위 5종 외 추가 라이브러리(예: 운영자 결정으로 추가될 보조 패키지) 는 본 spec 의 후속 phase 또는 별도 spec 의 책임이며 본 design 에서 1차 락 하지 않는다. 본 spec 작업은 8개 MS 의 소스 코드 / `requirements.txt` / `setup.py` / `pyproject.toml` 을 수정하지 않는다(R14.1 정합).

### 4.4 설치 결과 기록 정책 (R4.6 근거)

설치 결과는 성공 / 실패 두 값만 산출물에 기록한다. 실제 사용된 PyPI mirror, pip cache 경로, 운영자 사용자 home 절대경로는 본 design / 후속 phase 산출물 어디에도 기록하지 않는다.

## 5. PostgreSQL client / pg_restore 18.4 (R5)

### 5.1 권고값 (R5.1 / R5.2 근거)

| 도구 | 권고값 | 호환 메모 |
|------|--------|-----------|
| `pg_dump` | client major 18 / full 18.4 이상 유지 | 02 spec 부록 A 정합 |
| `pg_restore` | client major 18 / full 18.4 이상 유지 | 02 spec 부록 A 정합 |
| `psql` | client major 18 / full 18.4 이상 유지 | 02 spec 부록 A 정합 |

aws-paper RDS engine 은 PostgreSQL 18.4 로 운영 중이다. 본 spec 시점 client major 18 (full 18.4) 은 RDS engine major 18 (full 18.4) 과 동일 major 정합 상태로 호환된다(R-DATA-003 mitigation 인용). 향후 RDS engine major 가 변경되는 경우의 호환 매트릭스는 본 spec 범위 밖이며, 02 spec 부록 A 와 R-DATA-003 mitigation 재확인 후 후속 spec 결정으로 처리한다.

### 5.2 다운그레이드 금지 정책 (R5.3 / R5.4 근거)

본 spec 시점에 client 를 18.4 미만으로 다운그레이드하지 않는다.

dump source major version 또는 RDS engine major version 변경이 발생한 경우, 본 spec 임의 결정 금지. 아래 참조 후 후속 spec(02 또는 10) 의 결정으로 처리한다.

- [`../02-aws-network-and-rds/runbook.md`](../02-aws-network-and-rds/runbook.md) 부록 A
- [`../_common/risk-register.md`](../_common/risk-register.md) R-DATA-003 mitigation

### 5.3 후속 phase 책임 (R5.5 근거)

[`./runbook.md`](./runbook.md)(후속 phase) 가 `pg_dump --version`, `pg_restore --version`, `psql --version` 출력으로 client major version 18 임을 확인하는 단계를 포함한다.

## 6. RDS `marketconnector_app` 접속 (R6)

### 6.1 SG / 접근 정책 (R6.1 근거)

| 항목 | 정책 |
|------|------|
| RDS endpoint | private endpoint 만 |
| RDS SG inbound | `sg-marketconnector-ec2` → `sg-rds-postgres` 5432 만 허용 |
| Public access | `No` |
| 외부 CIDR inbound | 절대 금지(R-SEC-001 mitigation 정합) |

### 6.2 DB role / 사용 정책 (R6.2 근거)

| Role | 사용 시점 | 정책 |
|------|-----------|------|
| `marketconnector_app` | application runtime | 본 spec 의 일상 application 접속에 사용 |
| `portfolio_admin` | DDL / 관리 작업 시점만 | 일상 application 접속 금지. OD-DB-007 / OD-DB-008 정합 |

### 6.3 secret 매핑 (R6.3 / R6.4 근거)

| Secret 경로 (06 spec §2.4) | JSON key | 환경변수 키 (8개 MS 호환) |
|----------------------------|----------|--------------------------|
| `/portfolio/paper/rds/marketconnector-app` | `host` | `INTEREST_DB_HOST` |
| 동상 | `port` | `INTEREST_DB_PORT` |
| 동상 | `dbname` | `INTEREST_DB_NAME` |
| 동상 | `username` | `INTEREST_DB_USER` |
| 동상 | `password` | `INTEREST_DB_PASSWORD` |

본 design / 후속 phase 산출물 어디에도 실제 RDS endpoint hostname 을 평문으로 적지 않는다. placeholder `<rds-endpoint>` 또는 `[REDACTED]` 만 사용한다.

### 6.4 외부 접속 처리 (R6.5 근거)

VPC 밖(운영자 로컬 PC) 에서 RDS 접속이 필요한 경우, 본 EC2 + SSM Session Manager 또는 EC2 Instance Connect 경유로만 진입한다. RDS SG 에 외부 IP 를 직접 허용하지 않는다(R-SEC-001 / R-NET-004 mitigation 정합).

### 6.5 DDL/DML 정책 (R6.6 근거)

본 spec 의 모든 phase 에서 application 이 일으키는 조회성 SELECT 외에 INSERT / UPDATE / DELETE / CREATE / ALTER / DROP 호출 0건. 본 spec 시점의 RDS 측 변경은 운영자 직접 작업 외 모두 금지(R14 정합).

## 7. KIS Connector / Flask 운영 형태 (R7)

### 7.1 운영 모드 비교 (R7.2 / R7.3 근거)

| 모드 | 형태 | 본 spec 시점 책임 |
|------|------|------------------|
| 임시 검증 단계 | shell session 또는 임시 export 스크립트 + 운영자 수동 entrypoint 실행 + Flask debug flag(필요 시 일시 ON) | **본 spec 책임** |
| 정상 운영 모드 | systemd unit 또는 startup script 자동 기동 + Flask debug OFF + 로그 위치 결정 + 재기동 정책 | **본 spec 의 후속 task 또는 별도 phase 책임** |

### 7.2 정상 운영 모드 전환 항목 (R7.3 근거)

| 항목 | 책임 시점 |
|------|-----------|
| systemd unit 작성 | 후속 phase |
| startup script 작성 | 후속 phase |
| Flask debug 모드 OFF 강제 | 후속 phase |
| process 재기동 정책 | 후속 phase |
| 로그 파일 위치 결정 | 후속 phase |

### 7.3 entrypoint 분류 (R7.1 / R7.4 근거)

| 분류 | 파일 | 본 spec 시점 정책 |
|------|------|------------------|
| 조회성 — 2026-06-10 검증 완료 | `connector_balance.py`, `connector_order_check.py`, `connector_app.py` 의 Flask 내부 smoke test (조회성 endpoint) | §10 의 (d) / (e) / (f) 결과 [O] 로 기록 |
| 조회성 — 운영 가능 후보 (본 spec 시점 정식 검증 미완) | `connector_quote_realtime.py`, `connector_quote_closed.py`, `connector_view_service.py` | 정상 운영 형태에서 기동 가능해야 하며, 운영 진입 시점(후속 phase 또는 별도 phase) 에 검증 필요 |
| 신규 주문 | `connector_buy.py`, `connector_sell.py`, `connector_cancel.py`, `connector_modify.py` | 본 spec 의 어떤 phase 에서도 호출 0건 |

### 7.4 후속 phase 책임 (R7.5 / R7.6 근거)

[`./runbook.md`](./runbook.md)(후속 phase) 는 임시 검증 단계의 Flask 기동 명령(예: `python connector_app.py`) 과 조회성 endpoint 호출 패턴을 placeholder 기반으로 포함한다. 신규 주문 endpoint 호출 예제는 절대 포함하지 않는다. systemd unit 기반 정상 운영 모드 전환은 본 spec 의 후속 task 또는 별도 phase 진입 시점에만 적용한다.

운영 가능 후보 entrypoint(`connector_quote_realtime.py`, `connector_quote_closed.py`, `connector_view_service.py`) 는 본 spec 후속 task 또는 별도 phase 진입 시점에 조회성 smoke test 로 검증 후 §10 결과 표에 별도 행으로 추가한다.

## 8. Secrets Manager / SSM Parameter Store env 주입 (R8)

### 8.1 06 spec 인용 정책 (R8.1 근거)

본 spec 은 06 spec §1(분류 기준) / §2(naming 매트릭스) / §3(환경변수 호환성) 표를 변형 없이 입력으로 받는다. 본 design 은 매핑 표를 다시 1차 락 하지 않고, 06 spec 의 결정값을 그대로 인용한다.

### 8.2 환경변수 매핑 (R8.2 근거)

| 보관소 | 이름 (예) | 환경변수 키 (8개 MS 호환) | 비고 |
|--------|-----------|--------------------------|------|
| Secrets Manager | `/portfolio/paper/marketconnector/kis-app-key` | `APP_KEY` | 단일값 |
| Secrets Manager | `/portfolio/paper/marketconnector/kis-app-secret` | `APP_SECRET` | 단일값 |
| Secrets Manager | `/portfolio/paper/marketconnector/paper-account` | `PAPER_ACNT`, `ACNT_PRDT_CD` | JSON multi-key |
| Secrets Manager | `/portfolio/paper/rds/marketconnector-app` | `INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD` | JSON multi-key |
| SSM Parameter | `/portfolio/paper/marketconnector/kis-base-url` | `BASE_URL` | 일반 설정값 |
| SSM Parameter | `/portfolio/paper/marketconnector/environment` | `PORT_ENVIRONMENT` | 환경 식별자 |
| SSM Parameter | `/portfolio/paper/marketconnector/broker-name` | `PORT_BROKER_NAME` | broker 이름 |
| SSM Parameter | `/portfolio/paper/marketconnector/connector-host` | `CONNECTOR_HOST` | Flask host. 코드 기본값 `127.0.0.1` (port-marketconnector `connector_app.py` 기준) |
| SSM Parameter | `/portfolio/paper/marketconnector/connector-port` | `CONNECTOR_PORT` | Flask port. 코드 기본값 `5000` |
| SSM Parameter | `/portfolio/paper/marketconnector/connector-debug` | `CONNECTOR_DEBUG` | Flask debug flag. 코드 기본값 `False`. 정상 운영 모드에서는 반드시 `false` 강제 |

#### 8.2.1 KIS_* alias 동시 export 정책 (2026-06-17 보강)

본 design 의 환경변수 키(`APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD` / `BASE_URL`) 는 8개 MS 호환을 위해 그대로 유지한다.

단, 실제 `port-marketconnector` 코드 실행 시점에는 본 design 시점의 호환 key 외에 `KIS_*` prefix alias 가 함께 필요하다는 사실이 2026-06-17 운영자 검증으로 1차 실증되었다.

자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-17 섹션 참조.

| 호환 key (8개 MS 정합) | 실제 코드 실행에 필요한 alias | 출처 |
|-----------------------|------------------------------|------|
| `APP_KEY` | `KIS_APP_KEY` | Secrets Manager `/portfolio/paper/marketconnector/kis-app-key` JSON 내부 key `APP_KEY` 의 값 |
| `APP_SECRET` | `KIS_APP_SECRET` | Secrets Manager `/portfolio/paper/marketconnector/kis-app-secret` JSON 내부 key `APP_SECRET` 의 값 |
| `PAPER_ACNT` | `KIS_PAPER_ACNT` | Secrets Manager `/portfolio/paper/marketconnector/paper-account` JSON 내부 key `PAPER_ACNT` 의 값 |
| `ACNT_PRDT_CD` | `KIS_ACNT_PRDT_CD` | Secrets Manager `/portfolio/paper/marketconnector/paper-account` JSON 내부 key `ACNT_PRDT_CD` 의 값 |
| `BASE_URL` | `KIS_BASE_URL` | SSM Parameter `/portfolio/paper/marketconnector/kis-base-url` 의 값 |

임시 검증 단계에서는 호환 key 와 alias 를 **동시 export** 한다(예: 같은 secret 내부 value 를 `APP_KEY` 와 `KIS_APP_KEY` 두 환경변수로 동시 export).

동시 export 정책은 정상 운영 모드(systemd / startup script) 전환 시점까지 유지한다. 그 이후의 정합(호환 key / alias 중 어느 한 쪽으로 단일화할지, 또는 양쪽 동시 export 를 운영 표준으로 유지할지) 은 본 spec 후속 task 또는 별도 phase 책임으로 분리한다(§8.5 그대로 후속 인계).

#### 8.2.2 JSON SecretString 내부 key 추출 정책 (2026-06-17 보강)

아래 secret 은 plain string SecretString 이 아니라 **JSON SecretString** 이다.

- `/portfolio/paper/marketconnector/kis-app-key` — 내부 key `APP_KEY`
- `/portfolio/paper/marketconnector/kis-app-secret` — 내부 key `APP_SECRET`
- `/portfolio/paper/marketconnector/paper-account` — 내부 key `PAPER_ACNT` / `ACNT_PRDT_CD`

따라서 임시 export 스크립트(§8.3) 는 다음 절차를 따른다.

1. Secrets Manager `GetSecretValue` 결과의 `SecretString` 을 JSON 으로 parse.
2. 내부 key 의 value 만 환경변수로 export. JSON dict 전체를 환경변수 값으로 export 하지 않는다.
3. 동일 value 를 호환 key / `KIS_*` alias 양쪽에 동시 export.

`/portfolio/paper/rds/marketconnector-app` 도 JSON multi-key SecretString 이며 아래 매핑을 그대로 export 하는 정책은 그대로 유지한다(02 / 06 spec 정합 / 변경 0건).

- `host` → `INTEREST_DB_HOST`
- `port` → `INTEREST_DB_PORT`
- `dbname` → `INTEREST_DB_NAME`
- `username` → `INTEREST_DB_USER`
- `password` → `INTEREST_DB_PASSWORD`

#### 8.2.3 1차 실패 → 보정 사례 (2026-06-17 보강)

2026-06-17 `CONNECTOR_BALANCE` 1차 실행에서 KIS balance API 호출이 도달했음에도 `response_status=500` / `response_code=1` / `is_success=false` 가 반환된 사례는 KIS credential 자체 폐기가 아니라 JSON SecretString 전체를 env 값으로 그대로 export 한 mapping 오류로 1차 진단되었다.

JSON 내부 `APP_KEY` / `APP_SECRET` 의 value 만 추출해 호환 key + `KIS_*` alias 양쪽에 동시 export 한 v5 패턴에서 KIS balance API 가 `response_status=200` / `response_code=0` / `is_success=true` 로 정상 응답하고 `connector_balance_snapshot` 신규 row 가 저장되었다.

본 사례는 secret value / KIS app key / KIS app secret 평문 기록 0건으로 누적된다. secret value 는 절대 기록하지 않고 다음 수준까지만 산출물에 기록 가능하다.

- secret name path
- shape(JSON SecretString)
- 내부 key 이름
- value length

자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-17 섹션 참조.

### 8.3 임시 → 정상 전환 (R8.3 / R8.4 근거)

| 단계 | 형태 | 보안 정책 | 책임 시점 |
|------|------|-----------|-----------|
| 임시 검증 단계 | `/tmp/inject-env.sh` source + 메모리 export | 스크립트 본문에 secret 평문 미저장 / 권한 700 권고 / JSON SecretString 은 내부 key value 만 추출(§8.2.2) / 호환 key + `KIS_*` alias 동시 export(§8.2.1) | 본 spec |
| 정상 운영 모드 | systemd `EnvironmentFile=` 또는 startup script 메모리 export | `EnvironmentFile=` 본문에 secret 평문 미저장. 권한 600 권고. v5 mapping 패턴(§8.2.1 / §8.2.2) 반영 후 운영자 결정으로 호환 key / alias 단일화 또는 양쪽 유지 결정 | 본 spec 후속 task 또는 별도 phase |

### 8.4 호환 정책 (R8.5 근거)

| 항목 | 정책 |
|------|------|
| 8개 MS 코드 / `port-marketconnector` `config.py` | 미수정 (OD-DB-003 정합) |
| 환경변수 키 이름 | 미변경. secret 값만 외부화 |

### 8.5 후속 phase 책임 (R8.6 근거)

[`./runbook.md`](./runbook.md)(후속 phase) 는 임시 export 스크립트 작성, secret / parameter read, env 주입, Connector 재기동 후 KIS / RDS 조회성 smoke test 통과 단계를 [실행] / [확인] / [준비] / [복구] 라벨로 분리한다.

### 8.6 민감정보 표기 정책 (R8.7 근거)

본 spec 산출물 어디에도 실제 secret value, KIS app key / app secret, 계좌번호, RDS password, RDS endpoint hostname 을 평문으로 적지 않는다. 모두 `[REDACTED]` 또는 placeholder 만 사용한다.

## 9. Instance Role 기반 Access Key 미사용 (R9)

### 9.1 06 spec 입력 (R9.1 근거)

본 spec 은 06 spec §4(Instance Role 설계) / §5(Access Key 미사용 원칙) 의 결정을 그대로 입력으로 받는다. 본 design 은 06 spec 결정을 다시 락 하지 않고, 03 책임 보조 권한과 자격증명 정상 상태 점검 위치만 명시한다.

### 9.2 03 책임 보조 권한 매트릭스 (R9.3 / R9.4 근거)

| Statement | Effect | Action | Resource | 비고 |
|-----------|--------|--------|----------|------|
| `SsmManagedInstanceCore` | (managed) | AWS managed `AmazonSSMManagedInstanceCore` attach | (managed) | SSM Session Manager 접속용 |
| `CloudWatchLogsWrite (권고: log group 사전 생성)` | Allow | `logs:CreateLogStream`, `logs:PutLogEvents`, `logs:DescribeLogStreams` | `arn:aws:logs:<region>:<account-id>:log-group:/portfolio/paper/marketconnector*` | 본 spec 기본 권고. 운영자가 log group 사전 생성. See Details §9.1 Notes |
| `CloudWatchLogsWrite (옵션: CreateLogGroup 포함)` | Allow | `logs:CreateLogStream`, `logs:PutLogEvents`, `logs:DescribeLogStreams`, `logs:CreateLogGroup` | `arn:aws:logs:<region>:<account-id>:log-group:/portfolio/paper/marketconnector*` | 권고 옵션 채택 불가 시 한정 적용. See Details §9.1 Notes |

Details §9.1 Notes:

- `CloudWatchLogsWrite (권고: log group 사전 생성)`: 본 spec 의 기본 권고 옵션. 운영자가 log group 을 사전 생성한다. application / Connector 가 임의 log group 을 만들 수 없다. 다른 service log-group prefix 포함 금지.
- `CloudWatchLogsWrite (옵션: CreateLogGroup 포함)`: 권고 옵션을 채택할 수 없는 경우(예: 운영자 직접 사전 생성이 어려운 일시적 시점) 에만 한정 적용. 적용 후에는 본 spec 의 후속 task 에서 사전 생성 옵션으로 회수한다.

log group 이름 후보(권고): `/portfolio/paper/marketconnector/app`, `/portfolio/paper/marketconnector/flask`, `/portfolio/paper/marketconnector/system`. 실제 log group 생성 / Resource ARN 채움은 운영자 직접 작업이며 본 spec 범위 밖.

### 9.3 06 / 03 권한 분담 (R9.4 근거)

| spec | 책임 권한 |
|------|-----------|
| 06 | Secrets Manager / SSM Parameter Store / (조건부) KMS Decrypt — read 권한 |
| 03 (본 spec) | SSM Session Manager 접속 / CloudWatch Logs write — EC2 운영 보조 권한 |

### 9.4 자격증명 정상 상태 (R9.2 근거)

| 점검 항목 | 정상 상태 |
|-----------|-----------|
| `aws sts get-caller-identity` `Arn` | `arn:aws:sts::<account-id>:assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>` |
| `aws configure list` `access_key` Source | `iam-role` 또는 `Ec2InstanceMetadata` |

### 9.5 금지 정책 (R9.1 근거)

| 패턴 | 정책 |
|------|------|
| `~/.aws/credentials` long-lived access key 파일 | **금지** |
| `~/.aws/config` access key 항목 | **금지** |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` 환경변수 export | **금지** |
| dotfile(`~/.bashrc`, `~/.profile`, `~/.bash_profile`) access key export | **금지** |
| systemd unit `Environment=AWS_ACCESS_KEY_ID=...` | **금지** |
| systemd unit `EnvironmentFile=` access key 저장 | **금지** |
| application config / `.env` access key 저장 | **금지** |

### 9.6 비상 시나리오 처리 (R9.6 근거)

본 spec 운영 도중 EC2 안에 access key 가 발견되는 경우, 발견 1시간 이내에 다음을 수행한다.

발견 → IAM Console 에서 access key 즉시 폐기(`Make inactive` → `Delete`) → EC2 안 `~/.aws/credentials` 백업 이동(타임스탬프 suffix) → IMDSv2 + Instance Role 자격증명만 사용하는 모드로 복귀 → 06 / 03 의 자격증명 정상 상태(§9.4) 재검증 → operation-notes.md(후속 phase) 에 사실 누적 기록.

## 10. 2026-06-10 검증 결과 8건 (R10)

### 10.1 8건 결과 (R10.1 / R10.2 근거)

| 번호 | 항목 | 결과 | 라벨 후보 |
|------|------|------|-----------|
| (a) | Python 3.9.25 / venv 구성 | 성공 | `[O]` |
| (b) | `requests` / `flask` / `psycopg2-binary` / `psycopg` / `pandas` 설치 | 성공 | `[O]` |
| (c) | `marketconnector_app` 기준 RDS 접속 | 성공 | `[O]` |
| (d) | `connector_balance.py` 실행 | 성공 | `[O]` |
| (e) | `connector_order_check.py` 실행 | 성공 | `[O]` |
| (f) | Flask 내부 smoke test (조회성 endpoint) | 성공 | `[O]` |
| (g) | Secrets Manager / SSM Parameter Store 기반 env 주입 | 성공 | `[O]` |
| (h) | EC2 Instance Role 기반 Access Key 없이 실행 | 성공 | `[O]` |

### 10.2 기록 정책 (R10.3 / R10.4 / R10.6 근거)

operation-notes.md(후속 phase) 가 8건을 일자별 누적(`## 2026-06-10 ...`) 으로 기록한다.

- 결과(성공 / 실패) + 점검 일자 + 운영자 직접 확인 사실만 기록.
- 검증 명령의 stdout / stderr 본문, secret value, 토큰 값, 실제 endpoint hostname 은 절대 본문에 인용하지 않는다.
- validation-checklist.md(후속 phase) 는 02 / 06 spec 동일 4종 라벨(`[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]`) 로 점검 항목화한다.

### 10.3 실패 발생 시 처리 (R10.5 근거)

8건 중 한 건이 향후 점검 시점에 실패로 바뀌는 경우, 본 spec 임의 결정 금지. 라벨을 `[X]` 또는 `[Kiro 후속 작업 필요]` 로 갱신 → 원인을 [`../_common/risk-register.md`](../_common/risk-register.md) R-* 후보로 기록 → 운영자 승인 절차 진행.

## 11. 산출물 형식 / 라벨 (R11)

### 11.1 [`./runbook.md`](./runbook.md) 8단계 (R11.2 / R11.3 근거)

| 단계 | 라벨 후보 | 책임 |
|------|-----------|------|
| (a) 사전 확인(EC2 / SG / Profile attach / Python / venv / client 18) | [확인] | 운영자 직접 |
| (b) 의존 라이브러리 설치 결과 확인 | [확인] | 운영자 직접 |
| (c) 임시 export 스크립트 작성 / source 후 환경변수 주입 | [실행] | 운영자 직접 |
| (d) `marketconnector_app` 기반 RDS 접속 확인 | [확인] | 운영자 직접 |
| (e) `connector_balance.py` / `connector_order_check.py` 실행 | [확인] | 운영자 직접 |
| (f) Flask 내부 smoke test (조회성만) | [확인] | 운영자 직접 |
| (g) Instance Role 자격증명 / Access Key 미존재 점검 | [확인] | 운영자 직접 |
| (h) 8건 결과 운영자 노트 누적 | [준비] | 운영자 직접 |

### 11.2 [`./validation-checklist.md`](./validation-checklist.md) 7개 점검 영역 (R11.5 근거)

| 영역 | 점검 항목 (예) | 라벨 후보 |
|------|---------------|-----------|
| (a) Python / venv / 의존 라이브러리 인벤토리 | Python 3.9.25 / venv 1개 / 5종 설치 | `[O]` 후보 |
| (b) PostgreSQL client / pg_restore version | client major 18 / full 18.4 | `[O]` 후보 |
| (c) `marketconnector_app` RDS 접속 결과 | private endpoint 접속 통과 | `[O]` 후보 |
| (d) Connector / Flask 조회성 smoke test 결과 | 잔고 / 주문 조회 / Flask `/api/v1/view/...` 통과 | `[O]` 후보 |
| (e) Secrets Manager / SSM env 주입 결과 | 환경변수 매핑(§8.2) 통과 | `[O]` 후보 |
| (f) Instance Role 자격증명 / Access Key 미존재 결과 | assumed-role / Source `iam-role` / `~/.aws/credentials` 미존재 | `[O]` 후보 |
| (g) 신규 주문 / 매수 / 매도 / 취소 / 정정 호출 0건 | 본 spec 모든 phase 호출 0건 | `[O]` 후보 |

### 11.3 4종 라벨 규칙 (R11.4 근거)

`[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]` 만 사용한다. 그 외 라벨 사용 금지. 02 / 06 spec 동일 규칙.

### 11.4 [`./operation-notes.md`](./operation-notes.md) 형식 (R11.6 / R11.7 근거)

| 항목 | 정책 |
|------|------|
| 누적 기록 형식 | `## YYYY-MM-DD <요약>` (02 / 06 spec 동일) |
| secret / 식별자 기록 | 0건. `[REDACTED]` 또는 placeholder 만 |
| IAM 변경 기록 템플릿 | 변경 일자 / 변경자 / 변경 사유 / 변경 전 후 항목 요약 4줄. JSON 본문 전체 인용 금지 |
| 운영자 / Kiro 작업 분담 | 본문 상단에 명시: 실제 AWS 리소스 생성 / 변경 / 삭제는 운영자 직접, Kiro 는 문서 / 절차 / 검증 항목 정리만 |

## 12. _common 갱신 후보 (실제 갱신은 tasks 단계, R12)

### 12.1 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 갱신 후보 (R12.1 근거)

| ID | 항목 | 본 spec 후보값 | Status |
|----|------|---------------|--------|
| OD-SEC-005 | EC2 / 8개 MS Access Key 미사용 원칙 | EC2 안 access key 파일 / 환경변수 / dotfile 저장 금지. IMDSv2 + Instance Role 만 | TENTATIVE 🟡 → CONFIRMED 🟢 (운영자 승인 시) |
| OD-SEC-006 | EC2 / ECS IAM Role 기반 secret / parameter read 원칙 | 최소 권한. Resource wildcard 금지. Action wildcard 금지. service prefix 분리 | TENTATIVE 🟡 → CONFIRMED 🟢 (운영자 승인 시) |
| 신규 OD-SEC-* 또는 OD-NET-009 보조 | `AmazonSSMManagedInstanceCore` 사용 정책 | EC2 SSH 22 inbound 미허용 + SSM Session Manager + managed policy attach 만 | TENTATIVE 🟡 |

### 12.2 [`../_common/risk-register.md`](../_common/risk-register.md) 갱신 후보 (R12.2 / R12.4 근거)

| ID (잠정) | Risk | Mitigation | Detection | Rollback |
|-----------|------|------------|-----------|----------|
| R-DATA-XXX | pg_restore client / RDS engine major version mismatch 재발(R-DATA-003 보강) | client major 18 / full 18.4 유지 정책 명시. 다운그레이드 금지 | `pg_restore --version` / `pg_dump --version` 출력 점검 | 02 runbook 부록 A 재실행 후 client 재설치 |
| R-CAP-XXX | EC2 disk(EBS) 부족으로 venv / 로그 / token 파일 저장 실패 | 운영자 정기 점검 / log rotation / Connector 로그 위치 결정(후속 phase) | `df -h` / EBS CloudWatch metric / Connector startup error | log rotation 적용 / EBS volume 증설(운영자 결정) |
| R-BROKER-XXX | KIS API rate limit 초과로 조회성 호출 실패 | 본 spec 시점 호출 빈도 최소화 + token 재사용 정책 + Connector 재기동 횟수 제한 | KIS API 응답 코드 점검 / Connector 로그 패턴 | 호출 일시 중지 + 운영자 결정으로 재개 |
| R-SEC-XXX | Flask debug 모드 노출(임시 검증 단계 부주의) | 정상 운영 모드 전환 시 `CONNECTOR_DEBUG=false` 강제. 임시 검증 시 외부 노출 SG 미허용 | EC2 SG inbound 점검 / Flask startup 로그의 debug flag | debug flag OFF 후 재기동 + 외부 노출 여부 audit |
| R-AUTO-XXX | systemd 미설정으로 Connector / Flask 비의도 종료(EC2 reboot 후 재기동 누락) | 정상 운영 모드 전환(systemd unit / startup script) 책임을 본 spec 후속 task 또는 별도 phase 로 명시 | EC2 reboot 후 Connector 재기동 점검 / 헬스체크 | systemd unit 적용 후 재기동 |

### 12.3 [`../_common/followups-overview.md`](../_common/followups-overview.md) 갱신 후보 (R12.3 근거)

| 섹션 | 갱신 내용 |
|------|-----------|
| 03-marketconnector-ec2 1차 적용 환경 | `aws-paper`, `ap-northeast-2` |
| 1차 범위 | MarketConnector EC2 정식 운영 전환 + 8건 검증 누적 기록 |
| 1차 범위 밖 | EC2 신규 생성 / 신규 주문 / live rotation / OIDC / full IAM 매트릭스 |
| 04 / 05 / 08 / 09 / 10 spec 인계 | EC2 운영 패턴 → ECS Task Role 패턴 매핑 (§13) |

### 12.4 ID 부여 규칙 (R12.6 근거)

신규 결정 ID / 신규 risk ID 는 02 / 06 spec 의 ID 부여 규칙(한 번 부여한 ID 는 재사용 / 재번호하지 않는다, 다음 가용 번호 부여) 을 그대로 따른다.

## 13. EC2 → ECS 운영 패턴 매핑 (R13)

### 13.1 매핑 표 (R13.1 / R13.4 근거)

| 영역 | EC2 (03 본 spec) | ECS Fargate (04 / 05 / 08 / 09) |
|------|------------------|--------------------------------|
| 자격증명 | Instance Role + IMDSv2 | Task Role |
| secret / parameter read | 06 정책 + 환경변수 주입 | ECS Task Definition `secrets` 필드 + Task Role |
| 로그 | CloudWatch Logs Agent 또는 `PutLogEvents` | awslogs driver |
| 접속 / 디버깅 | SSM Session Manager | ECS Exec |
| 자동 기동 | systemd unit 또는 startup script | Task Definition + ECS Service |
| 재기동 / 복구 | systemd `Restart=` | ECS Service `desiredCount` + Step Functions |

### 13.2 책임 분담 (R13.2 근거)

03 (본 spec) = EC2 운영 패턴 1차 락. 04 / 05 / 08 / 09 = service prefix(`/portfolio/paper/strategy/*`, `/portfolio/paper/view/*`, `/portfolio/paper/crawler/*`, `/portfolio/paper/preprocessor/*`, `/portfolio/paper/research/*`) 채움.

### 13.3 후속 spec 변경 금지 결정 (R13.3 근거)

후속 spec 은 다음을 변경하지 않는다.

Resource wildcard(`Resource: "*"`) 금지 / Action wildcard(`secretsmanager:*` / `ssm:*` / `Action: "*"`) 금지 / env prefix `paper` / `live` 만 사용 / 다른 service prefix Resource 부여 금지 / Access Key 미사용 원칙(IMDSv2 + Role only).

## 14. 안전 제약 (R14)

### 14.1 영역별 정책

| 영역 | 정책 |
|------|------|
| 8개 MS 코드 / docs / 패키징 | 8개 MS 미수정. See Details §14 Notes |
| 실제 AWS 리소스 | EC2 / EBS / EIP / SG / IAM Role / Policy / Instance Profile / Secrets Manager secret / SSM Parameter / RDS / parameter group / CloudWatch Logs Group / KMS Key 미작업. 모든 실제 생성 / 수정 / 삭제는 운영자 직접 |
| 외부 호출 | broker / KIS API / Selenium / KRX / Naver / yfinance 호출 0건 |
| 신규 주문 | 매수 / 매도 / 취소 / 정정 / Daily Batch / intraday monitor 호출 0건 |
| RDS DDL/DML | 0건. 조회성 SELECT 만 허용 |
| 조회성 smoke test | 운영자 직접 작업으로만 수행 |
| secret / 식별자 표기 | `[REDACTED]` 또는 placeholder 만 사용. See Details §14.3 |
| GetSecretValue 호출 | 운영자만 수행. Kiro 자동 검증은 `secretsmanager:DescribeSecret` metadata 만 |
| 위반 감지 시 | 즉시 작업 중단 → 운영자 보고 → 롤백 절차 진행 |

### 14.2 phase 분리 (R14.7 근거)

본 phase(design) 시점에 [`./design.md`](./design.md) 외 다른 산출물(`tasks.md`, `runbook.md`, `validation-checklist.md`, `operation-notes.md`) 을 생성하지 않는다. 후속 phase 의 책임이다.

### 14 Notes — 8개 MS 미수정 대상 상세

8개 MS 미수정 대상은 아래 저장소의 README / AGENTS.md / CHANGELOG / docs / worklog / 소스 코드 / `requirements.txt` / `setup.py` / `pyproject.toml` 을 포함한다.

- `port-view`
- `port-marketconnector`
- `port-interest-crawler`
- `port-interest-preprocessor`
- `port_strategy_common`
- `port_strategy_decision`
- `port_strategy_execution`
- `port_strategy_research`

### 14.3 secret / 식별자 표기 상세

secret / 식별자 표기 정책의 실제 대상은 다음과 같다. 모두 `[REDACTED]` 또는 placeholder 만 사용한다.

- 실제 secret value / password / token / webhook URL
- KIS app key / KIS app secret / 계좌번호
- access key id / secret access key
- RDS endpoint hostname / account-id
- 실제 secret ARN / 실제 KMS Key ARN
- instance-id / EIP / EBS volume id

## Testing Strategy (참고)

본 spec 은 EC2 운영 패턴 / IAM 보조 권한 / 환경변수 주입 흐름 / 절차서 산출물이다. 코드 / 순수 함수 / 입력 변동에 따라 행위가 달라지는 알고리즘이 없다. 따라서 property-based testing(PBT) 은 적용되지 않는다. 본 design 은 Correctness Properties 섹션을 포함하지 않는다.

본 spec 의 검증은 다음 두 형태로만 수행된다.

- 환경 정합성 정적 점검: Python / venv / 의존 라이브러리 / client major version / Instance Role assumed-role ARN / Access Key 미존재 / Secret · Parameter Describe metadata 일치 점검. [`./validation-checklist.md`](./validation-checklist.md)(후속 phase) 책임.
- 조회성 smoke test 통과 여부 점검. [`./runbook.md`](./runbook.md) / [`./validation-checklist.md`](./validation-checklist.md)(후속 phase) 책임.
  - `marketconnector_app` 기준 RDS 접속
  - `connector_balance.py` / `connector_order_check.py` 실행
  - Flask 조회성 endpoint(`/api/v1/view/...`) 호출
  - `secretsmanager:GetSecretValue` 호출은 운영자만 수행
  - Kiro 자동 검증은 `secretsmanager:DescribeSecret` metadata 만 사용
