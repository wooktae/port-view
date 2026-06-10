# Requirements Document — 03-marketconnector-ec2

## Introduction

이 spec은 PORT-STRATEGY-AI AWS Migration 의 3번째 단계로, 이미 생성된 MarketConnector EC2 를 정식 운영 형태(Connector / Flask / KIS API / RDS 접속) 로 전환하는 절차 / 검증 / 운영 노트의 작성 기준을 확정한다.

선행 spec 의 1차 적용은 모두 완료된 상태다. [`../02-aws-network-and-rds`](../02-aws-network-and-rds) 에서 VPC / Subnet / SG / VPC Endpoint(Secrets Manager / SSM / CloudWatch Logs / ECR / S3) / RDS PostgreSQL / 7개 DB role(`marketconnector_app`, `view_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `research_app`, `execution_app`) 이 운영자 직접 작업으로 적용되었다. [`../06-secrets-and-iam`](../06-secrets-and-iam) 에서 Secrets Manager / SSM Parameter Store 분류 기준, naming 규칙(`/portfolio/{env}/{service}/{item}`), MarketConnector EC2 Instance Role 최소 권한 read 정책, EC2 내부 Access Key 미사용 원칙이 1차 락 되었다.

본 spec(03) 작업 시점의 MarketConnector EC2 는 다음 두 단계를 이미 거쳤다.

- 2026-06-09: aws-paper MarketConnector EC2(Amazon Linux 2023, public subnet, EIP attach) 가 RDS restore runner 로 신규 생성되었다. 로컬 PostgreSQL → S3 임시 bucket → EC2 → private RDS 경로로 dump 파일을 옮기고, PostgreSQL client / pg_restore 18.4 를 사용해 aws-paper RDS PostgreSQL 18.4 로 restore 와 정합성 검증을 마쳤다([`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-09 섹션, [`../02-aws-network-and-rds/runbook.md`](../02-aws-network-and-rds/runbook.md) 부록 A 참조).
- 2026-06-10: 같은 EC2 를 MarketConnector 정식 운영 EC2 로 전환했다. 운영자가 직접 Python 3.9.25 / venv 구성, 의존 라이브러리(`requests`, `flask`, `psycopg2-binary`, `psycopg`, `pandas`) 설치, `marketconnector_app` 사용자 기준 RDS 접속, `connector_balance.py` / `connector_order_check.py` 실행, Flask 내부 smoke test, Secrets Manager / SSM Parameter Store 기반 env 주입, EC2 Instance Role 기반 Access Key 없이 실행을 검증했고 8건 모두 성공으로 확인되었다.

본 spec 의 1차 적용 환경은 `aws-paper` 다. 핵심은 다음 한 줄로 좁힌다.

- 이미 생성된 MarketConnector EC2 가 02 / 06 의 1차 적용 결과를 입력으로 받아, KIS Connector(Flask) / `marketconnector_app` 기반 RDS 접속 / Secrets Manager·SSM Parameter Store env 주입 / Instance Role 기반 Access Key 미사용 운영 형태로 정식 전환되도록 절차 / 검증 / 운영 노트 작성 기준을 확정하고, 2026-06-10 검증 결과 8건을 산출물에 누적 기록한다.

본 spec 은 다음을 포함하지 않는다(범위 밖).

- EC2 신규 생성(이미 완료, 사전 완료 항목으로 처리).
- 신규 주문 / 매수 / 매도 / 취소 / 정정 호출. 본 spec 시점 검증은 조회성 smoke test 만 수행한다.
- live rotation 자동화(06 spec 범위 밖과 동일).
- CI/CD OIDC / GitHub Actions Role 설계(07 spec).
- 8개 MS 전체 full IAM 매트릭스(04 / 05 / 08 / 09 spec 분담).
- aws-live IAM(10 spec 통합).
- 8개 MS 의 README / AGENTS.md / CHANGELOG / docs / worklog 와 소스 코드 수정.

본 spec(03 폴더) 안 산출물은 [`requirements.md`](./requirements.md), [`design.md`](./design.md), [`tasks.md`](./tasks.md), [`runbook.md`](./runbook.md), [`validation-checklist.md`](./validation-checklist.md), [`operation-notes.md`](./operation-notes.md) 와 보조 문서(필요 시 `traceability-matrix.md`) 를 포함한다. 본 phase 에서는 `requirements.md` 만 작성하고, 나머지 문서는 후속 phase 에서 만든다. 루트 공통 참조 / 갱신 후보 문서는 [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/followups-overview.md`](../_common/followups-overview.md), [`../_common/risk-register.md`](../_common/risk-register.md), [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md) 이며 실제 갱신은 본 spec 의 tasks 단계에서 수행한다.

선행 spec: [`../01-aws-migration-foundation`](../01-aws-migration-foundation), [`../02-aws-network-and-rds`](../02-aws-network-and-rds), [`../06-secrets-and-iam`](../06-secrets-and-iam). 본 spec 의 결정은 [`../04-strategy-batch-stepfunctions`](../04-strategy-batch-stepfunctions)(예정), [`../05-port-view-ecs-and-runbook`](../05-port-view-ecs-and-runbook)(예정), [`../08-interest-crawler-and-preprocessor-ecs`](../08-interest-crawler-and-preprocessor-ecs)(예정), [`../09-strategy-research-batch`](../09-strategy-research-batch)(예정), [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook)(예정) 의 입력으로 사용된다.

본 문서에는 실제 secret value, KIS app key, KIS app secret, 계좌번호, 토큰, RDS endpoint hostname, RDS password, account-id, 실제 secret ARN, IAM access key id, Slack webhook URL, instance-id, EIP 값을 절대 적지 않는다. 모두 `[REDACTED]` 또는 placeholder(`<account-id>`, `<region>`, `<instance-id>`, `<eip>`, `<rds-endpoint>`) 만 사용한다.

## Glossary

- MarketConnector EC2: aws-paper 의 KIS Connector 를 실행하는 EC2 인스턴스. 02 spec 시점에 RDS restore runner 용도로 먼저 생성되었고, 본 spec(03) 에서 KIS Connector / Flask app 정식 운영 형태로 전환된다.
- 사전 완료 항목: 본 spec 시작 시점에 이미 운영자가 직접 적용해 둔 항목(EC2 인스턴스 자체, Amazon Linux 2023 OS, public subnet 배치, EIP attach, 기본 Security Group 연결 등). 본 spec 은 이를 입력으로 받고, 재생성하거나 변경하지 않는다.
- 정식 운영 형태: MarketConnector EC2 위에서 Python 3.9.25 venv, 의존 라이브러리, KIS Connector entrypoint(Flask `connector_app.py`, `connector_balance.py`, `connector_order_check.py`, `connector_quote_realtime.py`, `connector_quote_closed.py`, `connector_view_service.py`), `marketconnector_app` 기반 RDS 접속, Secrets Manager / SSM Parameter Store env 주입, Instance Role 기반 Access Key 미사용 운영이 모두 구비된 상태.
- 임시 검증 단계: 본 spec 시점의 운영 모드. shell 또는 임시 export 스크립트 형태로 환경변수를 주입하고 운영자가 수동으로 entrypoint 를 실행하는 단계. systemd unit 또는 startup script 기반 정상 운영 모드 전환은 본 spec 의 후속 task 또는 별도 phase 의 책임으로 둔다.
- 정상 운영 모드: systemd unit 또는 startup script 가 EC2 부팅 / 재기동 시점에 자동으로 Connector / Flask 프로세스를 기동하고, env 주입을 메모리 export 한 뒤 KIS API / RDS 접속을 유지하는 모드. 본 spec 에서 형태 / 책임 범위만 명시하고 실제 systemd unit 작성은 본 spec 후속 task 또는 별도 phase 의 책임이다.
- 조회성 smoke test: broker 측 신규 주문 / 매수 / 매도 / 취소 / 정정 호출을 일으키지 않는 검증. 잔고 조회, 보유 종목 조회, 주문 / 체결 조회, Flask 조회성 endpoint(`/api/v1/view/...`) 호출까지만 수행한다.
- IMDSv2 + Instance Role 자격증명: EC2 metadata service(IMDSv2) 가 발급하는 임시 자격증명을 Instance Role 권한으로 사용하는 방식. EC2 안에 long-lived access key / secret access key 를 저장하지 않는다. 06 spec OD-SEC-005 / OD-SEC-006 후보 결정에 정합한다.
- 6/10 검증 결과 8건: 2026-06-10 운영자가 직접 수행한 8개 검증 항목(Python 3.9.25 / venv 구성 / 의존 라이브러리 설치 / RDS 접속 / `connector_balance.py` 실행 / `connector_order_check.py` 실행 / Flask 내부 smoke test / Secrets Manager·SSM env 주입 / Instance Role 기반 Access Key 미사용 실행) 의 통과 결과. 모두 본 spec 산출물에 누적 기록 대상이다.

## Requirements

### Requirement 1: 본 spec 의 범위와 범위 밖

**Objective**: As 운영자, I want 본 spec(03) 의 범위와 범위 밖을 명시적으로 받기, so that 후속 spec(04 / 05 / 08 / 09 / 10) 또는 운영자가 본 spec 의 작업 경계를 한눈에 파악할 수 있다.

#### Acceptance Criteria

1. WHEN design.md 가 작성되면, THE design.md SHALL 본 spec 의 1차 적용 환경을 `aws-paper` 로 명시하고 `aws-live` 는 본 spec 범위 밖이라는 점을 명시해야 한다.
2. WHEN design.md 가 작성되면, THE design.md SHALL 다음 항목을 본 spec 범위 안으로 명시해야 한다: 이미 생성된 MarketConnector EC2 의 Amazon Linux 2023 OS / Python 3.9.25 venv / 의존 라이브러리(`requests`, `flask`, `psycopg2-binary`, `psycopg`, `pandas`) / PostgreSQL client(18.4) / pg_restore(18.4) / KIS Connector(Flask) / `marketconnector_app` 기반 RDS 접속 / Secrets Manager·SSM Parameter Store env 주입(06 인계) / Instance Role 기반 Access Key 미사용(06 인계) / 2026-06-10 검증 결과 8건의 문서화.
3. WHEN design.md 가 작성되면, THE design.md SHALL 다음 항목을 본 spec 범위 밖으로 명시해야 한다: EC2 신규 생성(이미 완료된 사전 항목), 신규 주문 / 매수 / 매도 / 취소 / 정정 호출(조회성만 수행), live rotation 자동화(06 범위 밖과 동일), CI/CD OIDC / GitHub Actions Role(07 spec), 8개 MS 전체 full IAM 매트릭스(04 / 05 / 08 / 09 분담), aws-live IAM 매트릭스(10 spec), 8개 MS 의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정.
4. WHERE 후속 spec(04 / 05 / 08 / 09 / 10) 이 본 spec 의 결정을 입력으로 사용하는 경우, THE design.md SHALL 본 spec 이 후속 spec 으로 인계하는 항목(EC2 운영 패턴 / Instance Role 권한 / env 주입 흐름 / 검증 라벨 정책) 을 명시해야 한다.
5. WHEN design.md 가 작성되면, THE design.md SHALL 본 phase(requirements) 시점에는 `requirements.md` 외 다른 산출물(`design.md`, `tasks.md`, `runbook.md`, `validation-checklist.md`, `operation-notes.md`) 을 생성하지 않는다는 phase 분리 정책을 명시해야 한다.

### Requirement 2: EC2 사전 완료 항목 명시

**Objective**: As 운영자, I want 본 spec 시작 시점에 이미 갖춰져 있는 EC2 사전 완료 항목을 한 곳에 명시 받기, so that 본 spec 작업이 EC2 신규 생성을 다시 시도하거나 사전 완료 항목을 변경하지 않도록 강제한다.

#### Acceptance Criteria

1. WHEN design.md 가 작성되면, THE design.md SHALL MarketConnector EC2 의 사전 완료 항목으로 다음을 명시해야 한다: Amazon Linux 2023, public subnet 배치, EIP attach, 기본 Security Group 연결, 02 spec 의 SG 매트릭스(특히 `sg-marketconnector-ec2` ↔ `sg-rds-postgres` 5432 inbound 허용) 적용 완료.
2. WHEN design.md 가 사전 완료 항목의 식별자(인스턴스 타입, EBS 볼륨 크기, instance-id, EIP 값 등) 를 다루는 경우, THE design.md SHALL 실제 값을 적지 않고 placeholder(`<instance-type>`, `<ebs-size>`, `<instance-id>`, `<eip>`) 또는 `[REDACTED]` 만 사용해야 한다.
3. WHEN design.md 가 IAM Role / Instance Profile 의 사전 완료 항목을 다루는 경우, THE design.md SHALL 06 spec 에서 확정한 `portfolio-paper-marketconnector-ec2-role` 와 `portfolio-paper-marketconnector-ec2-profile` 이 본 EC2 에 attach 되어 있는 것을 입력으로 가정한다는 점을 명시해야 한다.
4. WHEN design.md 가 사전 완료 항목을 다루는 경우, THE design.md SHALL 본 spec 작업이 EC2 신규 생성 / 인스턴스 타입 변경 / EBS 재생성 / public subnet 변경 / EIP detach 또는 교체를 수행하지 않는다는 정책을 명시해야 한다.
5. IF 사전 완료 항목 중 한 가지가 운영자 점검 결과 본 spec 가정과 다르게 발견된 경우, THEN THE design.md SHALL 본 spec 임의 변경 대신 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 에 변경 제안만 기록하고 운영자 승인 후에만 적용한다는 정책을 명시해야 한다.

### Requirement 3: 운영자 인수 사실(2026-06-09 RDS restore runner → 2026-06-10 MarketConnector 운영 전환)

**Objective**: As 운영자, I want 동일 EC2 가 2026-06-09 RDS restore runner 에서 2026-06-10 MarketConnector 정식 운영 EC2 로 전환된 사실을 산출물에 누적 기록할 기준을 받기, so that 후속 spec / 운영 회고에서 EC2 사용 이력을 한눈에 추적할 수 있다.

#### Acceptance Criteria

1. WHEN design.md 가 작성되면, THE design.md SHALL 2026-06-09 시점에 본 EC2 가 RDS restore runner 로 사용되었고 PostgreSQL client / pg_restore 18.4 기반으로 aws-paper RDS PostgreSQL 18.4 로의 restore 와 정합성 검증이 완료되었다는 사실을 명시해야 한다.
2. WHEN design.md 가 작성되면, THE design.md SHALL 본 RDS 가 private endpoint 로 운영되며 같은 VPC 안의 EC2 에서만 접속 가능하다는 정책(R-NET-004 mitigation 입력) 을 명시해야 한다.
3. WHEN design.md 가 작성되면, THE design.md SHALL 2026-06-10 시점에 같은 EC2 가 MarketConnector 정식 운영 EC2 로 전환되었고 본 spec 검증 결과 8건이 모두 성공으로 기록되어야 한다는 점을 명시해야 한다.
4. WHEN operation-notes.md 가 후속 phase 에서 작성되면, THE operation-notes.md SHALL 2026-06-09 RDS restore runner 사용 사실 한 줄, 2026-06-10 MarketConnector 운영 전환 사실 한 줄, 8건 검증 결과 항목별 한 줄을 일자별 누적 기록으로 포함해야 한다.
5. WHEN operation-notes.md 가 위 사실을 기록하는 경우, THE operation-notes.md SHALL 실제 dump 파일 경로 / S3 bucket 이름 / RDS endpoint hostname / 실제 EIP / 실제 instance-id 를 평문으로 적지 않고 placeholder 또는 `[REDACTED]` 만 사용해야 한다.

### Requirement 4: Python / venv / 의존 라이브러리 구성

**Objective**: As 운영자, I want MarketConnector EC2 의 Python 실행환경(Python 3.9.25 / venv / 의존 라이브러리) 구성 기준을 받기, so that 본 spec 의 모든 후속 phase 산출물이 동일한 실행환경 가정에서 검증 / 절차를 작성한다.

#### Acceptance Criteria

1. WHEN design.md 가 작성되면, THE design.md SHALL MarketConnector EC2 위 Python 버전을 `3.9.25` 로 명시하고, venv 1개 구성을 권고해야 한다.
2. WHEN design.md 가 venv 위치 / 이름 을 다루는 경우, THE design.md SHALL 실제 절대 경로(예: `/opt/portfolio/...`) 또는 운영자 home 경로 자체를 강제하지 않고 placeholder(`<venv-path>`, `<venv-name>`) 만 사용해야 한다.
3. WHEN design.md 가 의존 라이브러리를 다루는 경우, THE design.md SHALL `requests`, `flask`, `psycopg2-binary`, `psycopg`, `pandas` 5종을 본 spec 시점 1차 의존 라이브러리로 명시해야 한다.
4. WHEN design.md 가 추가 의존 라이브러리를 다루는 경우, THE design.md SHALL 위 5종 외 라이브러리(예: 운영자 결정에 따라 추가될 보조 패키지) 는 본 spec 후속 phase 또는 별도 spec 의 책임으로 분리한다는 점을 명시해야 한다.
5. WHEN design.md 가 8개 MS 의 소스 / requirements.txt 와의 관계를 다루는 경우, THE design.md SHALL 본 spec 작업이 8개 MS 의 소스 코드 / `requirements.txt` / `setup.py` / `pyproject.toml` 등 패키징 파일을 수정하지 않는다는 정책을 명시해야 한다.
6. WHEN design.md 가 의존 라이브러리 설치 결과를 다루는 경우, THE design.md SHALL 설치 성공 / 실패 결과만 산출물에 기록하고 실제 사용된 PyPI mirror, pip cache 경로, 운영자 사용자 home 절대경로는 본 문서에 적지 않는다는 정책을 명시해야 한다.

### Requirement 5: PostgreSQL client / pg_restore 18.4 사용 기준

**Objective**: As 운영자, I want 2026-06-09 RDS restore 에 사용된 PostgreSQL client / pg_restore 18.4 가 본 spec 시점에도 그대로 유지된다는 기준을 받기, so that client / engine major version mismatch 리스크(R-DATA-003) 가 본 spec 시점에 다시 발생하지 않는다.

#### Acceptance Criteria

1. WHEN design.md 가 작성되면, THE design.md SHALL MarketConnector EC2 위 PostgreSQL client / pg_restore 의 major version 을 `18.4` 로 유지한다는 점을 명시해야 한다.
2. WHEN design.md 가 RDS engine version 과의 호환을 다루는 경우, THE design.md SHALL aws-paper RDS engine 이 PostgreSQL 18.4(02 spec 부록 A 참조) 로 운영 중이며, client 18 과 RDS PostgreSQL 16 (또는 그 이상의 minor version) 도 일반적으로 호환 가능하다는 한 줄 메모를 포함해야 한다(R-DATA-003 mitigation 인용).
3. WHEN design.md 가 client 다운그레이드 시나리오를 다루는 경우, THE design.md SHALL 본 spec 시점에 client 를 18.4 미만으로 다운그레이드하지 않는다는 정책을 명시해야 한다.
4. IF 운영 도중 dump source major version 이 변경되거나 RDS engine major version 이 변경되는 경우, THEN THE design.md SHALL 본 spec 임의 변경 대신 [`../02-aws-network-and-rds/runbook.md`](../02-aws-network-and-rds/runbook.md) 부록 A 와 [`../_common/risk-register.md`](../_common/risk-register.md) R-DATA-003 mitigation 을 재확인한 뒤 후속 spec(02 또는 10) 의 결정으로 처리한다는 정책을 명시해야 한다.
5. WHEN runbook.md 가 후속 phase 에서 작성되면, THE runbook.md SHALL `pg_dump --version`, `pg_restore --version`, `psql --version` 출력으로 client major version 18 임을 확인하는 단계를 포함해야 한다.

### Requirement 6: RDS `marketconnector_app` 접속 운영 기준

**Objective**: As 운영자, I want MarketConnector EC2 가 `marketconnector_app` DB role 로 private RDS 에 접속하는 운영 기준을 받기, so that EC2 외부에서 RDS 직접 접속이 발생하지 않으면서도 application 의 RDS 접근이 정상 동작한다.

#### Acceptance Criteria

1. WHEN design.md 가 작성되면, THE design.md SHALL aws-paper RDS 가 private endpoint 만 노출하며 본 EC2 의 SG(`sg-marketconnector-ec2`) → RDS SG(`sg-rds-postgres`) 5432 inbound 만 허용된다는 점(02 spec SG 매트릭스 입력) 을 명시해야 한다.
2. WHEN design.md 가 application 접속 사용자 를 다루는 경우, THE design.md SHALL `marketconnector_app` DB role 로만 접속한다는 점, `portfolio_admin` 자격으로는 일상 application 접속을 수행하지 않는다는 점, OD-DB-008(execution R-only) 결정과 OD-DB-007(legacy 미부여) 결정에 정합한다는 점을 명시해야 한다.
3. WHEN design.md 가 접속 정보 주입을 다루는 경우, THE design.md SHALL 06 spec 의 `/portfolio/paper/rds/marketconnector-app` JSON multi-key secret 을 본 spec 의 입력으로 받고, secret 의 `host` / `port` / `dbname` / `username` / `password` 가 환경변수(`INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`) 로 매핑된다는 점을 명시해야 한다.
4. WHEN design.md 가 본 spec 산출물의 hostname 표기를 다루는 경우, THE design.md SHALL 실제 RDS endpoint hostname 을 본 문서 / runbook / validation-checklist / operation-notes 어디에도 평문으로 적지 않고 placeholder(`<rds-endpoint>`) 또는 `[REDACTED]` 만 사용한다는 정책을 명시해야 한다.
5. IF 외부(VPC 밖, 운영자 로컬 PC) 에서 RDS 접속 요청이 발생하는 경우, THEN THE design.md SHALL R-NET-004 mitigation 에 따라 EC2 + SSM Session Manager 또는 EC2 Instance Connect 경유로만 접근하도록 운영 기준을 강제하고, RDS SG 에 외부 IP 를 직접 허용하지 않는다는 점(R-SEC-001 mitigation 인접) 을 명시해야 한다.
6. WHEN design.md 가 본 spec 시점의 RDS DDL/DML 을 다루는 경우, THE design.md SHALL 본 spec 의 모든 phase 에서 application 이 일으키는 조회성 SELECT 외에 추가적인 DDL/DML 을 수행하지 않는다는 정책을 명시해야 한다(R14 정합).

### Requirement 7: KIS Connector / Flask 운영 형태

**Objective**: As 운영자, I want MarketConnector EC2 위 KIS Connector(Flask) 운영 형태를 임시 검증 단계와 정상 운영 모드 두 단계로 분리해서 받기, so that 본 spec 시점의 검증과 본 spec 후속 / 별도 phase 로 넘어갈 정상 운영 모드 사이의 책임이 명확해진다.

#### Acceptance Criteria

1. WHEN design.md 가 작성되면, THE design.md SHALL `port-marketconnector` repo 의 다음 entrypoint 가 EC2 위 정상 운영 형태에서 동작 가능해야 한다는 점을 명시해야 한다: `connector_app.py`, `connector_balance.py`, `connector_order_check.py`, `connector_quote_realtime.py`, `connector_quote_closed.py`, `connector_view_service.py`.
2. WHEN design.md 가 본 spec 시점의 운영 모드를 다루는 경우, THE design.md SHALL 본 spec 시점이 임시 검증 단계(shell session 또는 임시 export 스크립트 기반 환경변수 주입 + 운영자 수동 entrypoint 실행) 임을 명시하고, systemd unit 또는 startup script 기반 정상 운영 모드 전환은 본 spec 의 후속 task 또는 별도 phase 의 책임이라는 점을 명시해야 한다.
3. WHEN design.md 가 정상 운영 모드 전환의 책임 경계를 다루는 경우, THE design.md SHALL 다음 항목이 본 spec 의 후속 task 또는 별도 phase 책임이라는 점을 명시해야 한다: systemd unit 작성 / startup script 작성 / Flask debug 모드 OFF 강제 / process 재기동 정책 / 로그 위치 결정.
4. WHEN design.md 가 신규 주문 / 매수 / 매도 / 취소 / 정정 entrypoint(`connector_buy.py`, `connector_sell.py`, `connector_cancel.py`, `connector_modify.py`) 를 다루는 경우, THE design.md SHALL 본 spec 의 어떤 phase 에서도 위 entrypoint 를 실행하지 않는다는 정책을 명시해야 한다.
5. WHEN runbook.md 가 후속 phase 에서 작성되면, THE runbook.md SHALL 임시 검증 단계의 Flask 기동 명령(예: `python connector_app.py`) 과 조회성 endpoint 호출 패턴을 placeholder 기반으로 포함하되, 신규 주문 endpoint 호출 예제는 절대 포함하지 않아야 한다.
6. IF 운영자가 본 spec 시점에 systemd unit 기반 정상 운영 모드 전환을 시도하길 원하는 경우, THEN THE design.md SHALL 본 spec 의 후속 task 로 분리하고 별도 phase(예: 후속 03 phase) 또는 별도 spec 진입 시점에만 적용한다는 정책을 명시해야 한다.

### Requirement 8: Secrets Manager / SSM Parameter Store env 주입

**Objective**: As 운영자, I want 06 spec 에서 1차 락 한 Secrets Manager / SSM Parameter Store 분류 / naming / 주입 흐름을 본 spec 의 입력으로 받기, so that MarketConnector EC2 가 코드 변경 없이 환경변수만으로 KIS / RDS / 일반 설정값을 read 한다.

#### Acceptance Criteria

1. WHEN design.md 가 작성되면, THE design.md SHALL 06 spec 의 §1(분류 기준) / §2(naming 매트릭스) / §3(환경변수 호환성) 표를 본 spec 의 입력으로 그대로 받는다는 점을 명시해야 한다.
2. WHEN design.md 가 환경변수 매핑을 다루는 경우, THE design.md SHALL 다음 키 흐름을 포함해야 한다: KIS 관련(`APP_KEY`, `APP_SECRET`, `BASE_URL`, `PAPER_ACNT`, `ACNT_PRDT_CD`), RDS 관련(`INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`), 일반 설정(`PORT_ENVIRONMENT`, `PORT_BROKER_NAME`).
3. WHEN design.md 가 임시 export 스크립트 패턴(`/tmp/inject-env.sh` 등) 을 다루는 경우, THE design.md SHALL 06 spec runbook §6-1 의 패턴을 본 spec 임시 검증 단계 입력으로 받고, 본 spec 의 후속 task 또는 별도 phase 에서 정상 운영 모드(systemd `EnvironmentFile=` 또는 startup script 메모리 export) 로 전환하는 책임을 본 spec 이 가진다는 점을 명시해야 한다.
4. WHEN design.md 가 임시 export 스크립트의 보안 정책을 다루는 경우, THE design.md SHALL 스크립트 / `EnvironmentFile=` 본문 어디에도 secret 값을 평문으로 저장하지 않고, 메모리 export 만 수행하며, 스크립트 권한은 운영자 결정으로 700 / 600 등 제한 권한을 권고한다는 정책을 명시해야 한다(R-SEC 후보 §12 인접).
5. WHEN design.md 가 본 spec 의 환경변수 키 호환을 다루는 경우, THE design.md SHALL 8개 MS 코드 / `port-marketconnector` `config.py` 를 수정하지 않고 환경변수 주입만으로 KIS / RDS 식별자가 외부화 된다는 점을 명시해야 한다(OD-DB-003 입력).
6. WHEN runbook.md 가 후속 phase 에서 작성되면, THE runbook.md SHALL 임시 export 스크립트 작성, secret / parameter read, env 주입, Connector 재기동 후 KIS / RDS 조회성 smoke test 통과 단계를 [실행] / [확인] / [준비] / [복구] 라벨로 분리해야 한다.
7. WHEN 본 spec 산출물이 secret / parameter 값을 다루는 경우, THE 산출물 SHALL 실제 secret value, KIS app key / app secret, 계좌번호, RDS password, RDS endpoint hostname 을 절대 평문으로 적지 않고 모두 `[REDACTED]` 또는 placeholder 만 사용해야 한다.

### Requirement 9: Instance Role 기반 Access Key 미사용 운영

**Objective**: As 운영자, I want MarketConnector EC2 안에 IAM access key / secret access key 가 저장되지 않고 IMDSv2 + Instance Role 자격증명만 사용하는 운영 기준을 본 spec 시점에 다시 한 번 강제하기, so that 06 spec 의 OD-SEC-005 / OD-SEC-006 후보 결정과 R-SEC 후보 리스크가 본 spec 시점에도 일관되게 통제된다.

#### Acceptance Criteria

1. WHEN design.md 가 작성되면, THE design.md SHALL 본 EC2 안의 어떤 파일 / 환경변수 / dotfile / systemd unit / `EnvironmentFile=` / application config 에도 IAM access key id 와 secret access key 를 저장하지 않는다는 정책을 다시 명시해야 한다(06 spec §5 입력).
2. WHEN design.md 가 본 spec 시점의 자격증명 흐름을 다루는 경우, THE design.md SHALL `aws sts get-caller-identity` 결과의 `Arn` 이 `arn:aws:sts::<account-id>:assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>` 형태이고, `aws configure list` 의 `access_key` Source 가 `iam-role` 또는 `Ec2InstanceMetadata` 인 상태가 본 spec 시점의 정상 상태라는 점을 명시해야 한다.
3. WHEN design.md 가 본 spec 시점의 Instance Role 보조 권한을 다루는 경우, THE design.md SHALL 본 spec 책임 범위로 다음 항목을 명시해야 한다: SSM Session Manager 접속용 `AmazonSSMManagedInstanceCore` managed policy attach, CloudWatch Logs write 권한 부여(application 로그 / Connector 로그 송신).
4. WHEN design.md 가 06 spec 권한과 본 spec 권한의 책임 경계를 다루는 경우, THE design.md SHALL 06 spec 이 secret / parameter read 권한을 책임지고, 본 spec(03) 이 SSM Session Manager / CloudWatch Logs write 등 EC2 운영 보조 권한을 책임진다는 분담을 명시해야 한다(06 spec §4.4 입력).
5. WHEN runbook.md 가 후속 phase 에서 작성되면, THE runbook.md SHALL EC2 안 `~/.aws/credentials` 미존재 점검, `aws_access_key_id` / `aws_secret_access_key` 패턴 grep 0건 점검, dotfile / systemd `EnvironmentFile=` 안 access key 패턴 0건 점검 단계를 포함해야 한다.
6. IF 본 spec 운영 도중 EC2 안에 access key 가 발견되는 경우, THEN THE design.md SHALL 즉시 access key 를 IAM Console 에서 폐기하고, EC2 안 `~/.aws/credentials` 백업 이동 후 IMDSv2 + Instance Role 자격증명만 사용하는 모드로 복귀하는 [복구] 절차가 runbook.md 에 포함되어야 한다는 점을 명시해야 한다.
7. WHEN 본 spec 산출물이 access key 를 다루는 경우, THE 산출물 SHALL 실제 access key id / secret access key 값을 절대 적지 않고 모두 `[REDACTED]` 만 사용해야 한다.

### Requirement 10: 2026-06-10 검증 결과 8건 기록 요구

**Objective**: As 운영자, I want 2026-06-10 시점에 통과된 8건의 검증 결과가 본 spec 산출물에 누적 기록되도록 기준을 받기, so that 후속 spec 또는 운영 회고가 본 EC2 의 정식 운영 전환 시점을 한눈에 추적할 수 있다.

#### Acceptance Criteria

1. WHEN operation-notes.md 가 후속 phase 에서 작성되면, THE operation-notes.md SHALL 다음 8건을 일자별 누적(`## 2026-06-10 ...`) 으로 기록해야 한다: (a) Python 3.9.25 / venv 구성 완료 = 성공, (b) `requests` / `flask` / `psycopg2-binary` / `psycopg` / `pandas` 설치 완료 = 성공, (c) `marketconnector_app` 기준 RDS 접속 성공, (d) `connector_balance.py` 실행 성공, (e) `connector_order_check.py` 실행 성공, (f) Flask 내부 smoke test 성공, (g) Secrets Manager / SSM Parameter Store 기반 env 주입 성공, (h) EC2 Instance Role 기반 Access Key 없이 실행 성공.
2. WHEN validation-checklist.md 가 후속 phase 에서 작성되면, THE validation-checklist.md SHALL 위 8건 각각을 별도 점검 항목으로 분리하고 4종 라벨(`[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]`) 중 하나로 표시해야 한다. 본 spec 시점에는 8건 모두 `[O]` 또는 운영자 직접 확인 결과로 표시한다.
3. WHEN operation-notes.md 가 위 8건 결과를 기록하는 경우, THE operation-notes.md SHALL 실제 RDS endpoint hostname / 계좌번호 / 토큰 / KIS app key / KIS app secret / 실제 secret ARN / instance-id / EIP / 실제 account-id 를 평문으로 적지 않고 모두 `[REDACTED]` 또는 placeholder 만 사용해야 한다.
4. WHEN validation-checklist.md 가 8건 점검 항목을 다루는 경우, THE validation-checklist.md SHALL 02 / 06 spec 의 라벨 / 형식 정책(라벨 4종, 일자별 누적 형식, secret `[REDACTED]`) 을 그대로 따라야 한다.
5. IF 8건 중 한 건이 향후 점검 시점에 실패로 바뀌는 경우, THEN THE design.md SHALL 본 spec 임의 결정 대신 라벨을 `[X]` 또는 `[Kiro 후속 작업 필요]` 로 갱신하고, 원인은 [`../_common/risk-register.md`](../_common/risk-register.md) R-* 후보로 기록한 뒤 운영자 승인 절차를 거친다는 정책을 명시해야 한다.
6. WHEN 본 spec 산출물이 8건 결과를 인용하는 경우, THE 산출물 SHALL 결과 자체(성공 / 실패) 와 점검 일자, 운영자가 직접 확인했다는 사실만 기록하고 검증 명령의 stdout / stderr 내용 자체나 secret value 를 본문에 그대로 붙여 넣지 않아야 한다.

### Requirement 11: 운영자 절차 / 검증 / 운영 노트 산출물 요구

**Objective**: As 운영자, I want 본 spec 의 후속 phase 산출물(`runbook.md`, `validation-checklist.md`, `operation-notes.md`) 의 형식과 라벨 규칙을 1차 확정 받기, so that 02 / 06 spec 과 동일한 점검 흐름 / 라벨 규칙으로 운영자가 작업 / 점검 / 누적 기록을 진행할 수 있다.

#### Acceptance Criteria

1. WHEN 본 spec 의 후속 phase 가 진행되면, THE 후속 phase SHALL 본 spec 폴더 안에 `runbook.md`, `validation-checklist.md`, `operation-notes.md` 산출물을 생성해야 한다.
2. WHEN runbook.md 가 작성되면, THE runbook.md SHALL 단계마다 [실행] / [확인] / [준비] / [복구] 라벨을 붙이고, 02 / 06 spec runbook 의 보안 원칙(secret `[REDACTED]`, 8개 MS 코드 / docs 미수정, 외부 호출 금지) 을 그대로 따라야 한다.
3. WHEN runbook.md 가 단계 분해를 다루는 경우, THE runbook.md SHALL 다음 항목을 단계별로 포함해야 한다: (a) 사전 확인(EC2 / SG / Instance Profile attach 상태 / Python / venv / client 18 버전), (b) 의존 라이브러리 설치 결과 확인, (c) 임시 export 스크립트 작성 / source 후 환경변수 주입, (d) `marketconnector_app` 기반 RDS 접속 확인, (e) `connector_balance.py` / `connector_order_check.py` 실행, (f) Flask 내부 smoke test, (g) Instance Role 자격증명 / Access Key 미존재 점검, (h) 8건 결과 운영자 노트 누적.
4. WHEN validation-checklist.md 가 작성되면, THE validation-checklist.md SHALL 다음 4종 라벨만 사용해야 한다: `[O]`, `[X]`, `[Kiro 후속 작업 필요]`, `[운영자 확인 필요]`. 그 외 라벨은 사용하지 않는다.
5. WHEN validation-checklist.md 가 점검 영역을 다루는 경우, THE validation-checklist.md SHALL 다음 영역을 모두 포함해야 한다: (a) Python / venv / 의존 라이브러리 인벤토리, (b) PostgreSQL client / pg_restore version, (c) `marketconnector_app` RDS 접속 결과, (d) Connector / Flask 조회성 smoke test 결과, (e) Secrets Manager / SSM Parameter Store env 주입 결과, (f) Instance Role 자격증명 / Access Key 미존재 결과, (g) 신규 주문 / 매수 / 매도 / 취소 / 정정 호출 0건 검증.
6. WHEN operation-notes.md 가 작성되면, THE operation-notes.md SHALL 02 / 06 spec operation-notes 의 일자별 누적 기록 형식(`## YYYY-MM-DD ...`) 을 그대로 따르고, 실제 secret / 토큰 / 계좌번호 / RDS hostname / account-id / 실제 ARN / 실제 instance-id / EIP 를 절대 기록하지 않아야 한다.
7. WHEN operation-notes.md 가 작성되면, THE operation-notes.md SHALL 실제 AWS 리소스 생성 / 변경 / 삭제는 운영자가 직접 수행하고 Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행한다는 작업 분담 원칙을 본문에 포함해야 한다.

### Requirement 12: 공통 결정 / 리스크 / 후속 문서 갱신 후보

**Objective**: As 운영자, I want 본 spec 시점에 갱신해야 할 [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/risk-register.md`](../_common/risk-register.md), [`../_common/followups-overview.md`](../_common/followups-overview.md) 후보를 본 requirements.md 본문에서 명시 받기, so that 후속 phase tasks 단계가 어떤 row 를 갱신해야 하는지 한눈에 알 수 있다.

#### Acceptance Criteria

1. WHEN design.md 가 작성되면, THE design.md SHALL [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 의 다음 결정에 대해 본 spec 시점의 갱신 후보를 명시해야 한다: OD-SEC-005(Access Key 미사용 원칙) 와 OD-SEC-006(IAM Role 기반 read 원칙) 의 상태를 본 spec 시점에 잠정 → 확정 후보로 갱신, `AmazonSSMManagedInstanceCore` 사용 정책을 신규 OD-SEC-* 후보 또는 OD-NET-009 보조 결정으로 명시.
2. WHEN design.md 가 작성되면, THE design.md SHALL [`../_common/risk-register.md`](../_common/risk-register.md) 의 신규 리스크 후보로 다음 항목을 명시해야 한다: pg_restore client / RDS engine major version mismatch 재발(R-DATA-003 보강), EC2 disk 부족(EBS 사용량 / log rotation), KIS API rate limit 초과로 조회 실패, Flask debug 모드 노출(임시 검증 단계 부주의 시), systemd 미설정으로 인한 Connector / Flask 비의도 종료(EC2 reboot 후 재기동 누락).
3. WHEN design.md 가 [`../_common/followups-overview.md`](../_common/followups-overview.md) 갱신 후보를 다루는 경우, THE design.md SHALL 03 spec 의 1차 적용 환경(`aws-paper`), 1차 범위(MarketConnector EC2 정식 운영 전환 + 8건 검증), 범위 밖(EC2 신규 생성 / 신규 주문 / live rotation / OIDC / full IAM 매트릭스), 04 / 05 / 08 / 09 / 10 spec 으로의 인계 항목을 갱신 후보로 명시해야 한다.
4. WHEN design.md 가 신규 리스크 후보를 다루는 경우, THE design.md SHALL 각 리스크에 대해 mitigation, detection, rollback 한 줄씩을 [`../_common/risk-register.md`](../_common/risk-register.md) 컬럼 형식과 동일하게 명시해야 한다.
5. WHEN tasks.md 가 후속 phase 에서 작성되면, THE tasks.md SHALL [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/risk-register.md`](../_common/risk-register.md), [`../_common/followups-overview.md`](../_common/followups-overview.md) 갱신 작업을 별도 task 로 분리해야 한다.
6. WHEN 본 spec 산출물이 신규 결정 ID / 신규 risk ID 를 부여하는 경우, THE 산출물 SHALL 02 / 06 spec 의 ID 부여 규칙(한 번 부여한 ID 는 재사용 / 재번호하지 않는다, 다음 가용 번호 부여) 을 그대로 따라야 한다.

### Requirement 13: 후속 spec 재사용성 (EC2 → ECS 운영 패턴 매핑)

**Objective**: As 운영자, I want 본 spec(03) 의 EC2 운영 패턴(systemd / startup script / log 위치 / Instance Role 권한 attach 방법) 이 후속 04 / 05 / 08 / 09 spec 의 ECS 운영으로 어떻게 매핑되는지 골격 한 줄을 받기, so that 같은 패턴을 spec 마다 다시 만들지 않는다.

#### Acceptance Criteria

1. WHEN design.md 가 작성되면, THE design.md SHALL 본 spec 의 EC2 운영 패턴(Instance Role 기반 secret / parameter read, 임시 export → 정상 운영 모드 전환, CloudWatch Logs write, SSM Session Manager 접속) 이 04 / 05 / 08 / 09 spec 의 ECS Fargate Task 운영(Task Role 기반 secret / parameter read, ECS `secrets` 필드 기반 환경변수 주입, awslogs driver 기반 CloudWatch Logs, SSM ECS Exec) 으로 매핑된다는 한 줄 골격을 명시해야 한다.
2. WHEN design.md 가 책임 분담을 다루는 경우, THE design.md SHALL 본 spec(03) 이 EC2 운영 패턴 자체의 1차 락 책임이며, ECS Task Role 의 service prefix 채움(`/portfolio/paper/strategy/*` / `/portfolio/paper/view/*` / `/portfolio/paper/crawler/*` 등) 은 04 / 05 / 08 / 09 spec 의 책임이라는 점을 명시해야 한다.
3. WHEN design.md 가 후속 spec 이 변경하지 말아야 할 결정을 다루는 경우, THE design.md SHALL 다음 항목을 한 줄씩 명시해야 한다: Resource wildcard 금지 정책, Action wildcard 금지 정책, env prefix `paper` / `live` 분리, 다른 service prefix 부여 금지, Access Key 미사용 원칙(IMDSv2 + Role only).
4. WHERE 본 spec 의 systemd unit 또는 startup script 형태가 후속 spec 의 ECS Task 운영 형태로 그대로 옮겨지지 않는 경우, THE design.md SHALL EC2 의 systemd 가 ECS 의 Task Definition + ECS Service / Step Functions 로 매핑된다는 한 줄 매핑을 명시해야 한다.

### Requirement 14: 본 spec 의 안전 제약

**Objective**: As 운영자, I want 본 spec 작업이 코드 / 운영 데이터 / AWS 리소스 / 외부 호출 / 신규 주문을 변경하지 않도록 명시적으로 제한하기.

#### Acceptance Criteria

1. WHEN 본 spec 의 모든 phase 가 진행되는 동안, THE 작업 SHALL 8개 MS(`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) 의 README / AGENTS.md / CHANGELOG / docs / worklog 와 소스 코드 / 패키징 파일(`requirements.txt`, `setup.py`, `pyproject.toml`) 을 수정하지 않아야 한다.
2. WHEN 본 spec 의 모든 phase 가 진행되는 동안, THE 작업 SHALL 실제 AWS 리소스(EC2 인스턴스 자체, EBS 볼륨, EIP attach 변경, Security Group 규칙, IAM Role / Policy / Instance Profile, Secrets Manager secret, SSM Parameter, RDS 인스턴스 / parameter group, CloudWatch Logs Group 등) 를 만들거나 변경 / 삭제하지 않아야 한다. 모든 실제 생성 / 수정 / 삭제는 운영자가 직접 수행한다.
3. WHEN 본 spec 의 모든 phase 가 진행되는 동안, THE 작업 SHALL 외부 호출(broker / KIS API / Selenium / KRX / Naver / yfinance) 과 신규 주문 / 매수 / 매도 / 취소 / 정정 호출, Daily Batch / intraday monitor 호출, RDS DDL/DML 호출을 실행하지 않고, 조회성 smoke test 만 운영자 직접 작업으로 수행한다.
4. WHEN 본 spec 산출물이 secret / 식별자를 다루는 경우, THE 산출물 SHALL 모든 실제 secret value, password, KIS app key, KIS app secret, 계좌번호, token, webhook URL, access key id, secret access key, RDS endpoint hostname, account-id, 실제 secret ARN, 실제 KMS Key ARN, instance-id, EIP, EBS volume id 자리에 `[REDACTED]` 만 사용하고 실제 값을 적지 않아야 한다.
5. WHEN secret 조회를 다루는 경우, THE 산출물 SHALL `secretsmanager:GetSecretValue` 호출은 운영자만 수행하고 Kiro 자동 검증은 `secretsmanager:DescribeSecret` metadata 만 사용한다는 점을 명시해야 한다(06 spec §11 정합).
6. WHEN 본 spec 이 후속 spec 과의 분리 정책을 다루는 경우, THE 산출물 SHALL EC2 신규 생성 / live rotation 자동화 / CI/CD OIDC / full IAM 매트릭스 / aws-live 환경 IAM 은 본 spec 범위 밖임을 명시해야 한다.
7. WHEN 본 phase(requirements) 가 진행되는 동안, THE 작업 SHALL 본 spec 폴더 안에 `requirements.md` 외 다른 산출물(`design.md`, `tasks.md`, `runbook.md`, `validation-checklist.md`, `operation-notes.md`) 을 생성하지 않아야 한다. 이들은 후속 phase 의 책임이다.
