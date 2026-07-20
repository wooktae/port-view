# Operator Decisions — AWS Migration

## Purpose

본 문서는 PORT-STRATEGY-AI AWS Migration 전체(01-aws-migration-foundation · 02-aws-network-and-rds 및 후속 03 ~ 10)의 운영자 결정을 누적 기록하는 단일 진실원(source of truth)이다. 02 spec 시점에 작성을 시작했고, 03 ~ 10 spec 진행 중 새 결정 항목이 생기면 본 파일에 동일 컬럼으로 누적한다. 모든 후속 spec은 본 표를 입력으로 참조한다.

문서 흐름: **Purpose → Review Needed → Status Legend → Decision Dashboard → At a Glance → Decision Index → Open/Tentative/Deferred Decisions → Decision Details → Change Log → Decision Change Log Details → Decision Update Rules → Security Notes**. 표(Decision Index · At a Glance · 카테고리별 표) 는 인덱스 역할만 담고, 긴 선택지 상세 · 비용 영향 · 운영 리스크 · 후속 spec 영향은 `Decision Details` 로 이동한다.

Decision ID(`OD-*`) · Status 문자열(CONFIRMED · TENTATIVE · TBD · DEFERRED) · 날짜(YYYY-MM-DD) · 비용 수치(USD/월) · Scheduler / State Machine / Lambda / IAM Role 이름 · spec 번호 · evidence 관계는 원문 그대로 보존한다. secret · password · token · KIS app key · KIS app secret · Slack webhook URL · 계좌번호 · account-id · 실제 ARN · public IP · broker_order_no 원문은 어디에도 기록하지 않고 `[REDACTED]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_PUBLIC_IP]` · `[REDACTED_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_SECRET_ARN]` · `[REDACTED_BROKER_ORDER_NO]` placeholder 계열만 사용한다.

---

## Review Needed

본 문서 리팩토링 과정에서 다음 항목이 후속 검토 대상으로 식별되었다. 본 회차에서는 결정값 / ID / status / 날짜 / 비용 수치를 보존하기 위해 해결하지 않고 이월한다.

- **OD-MS-028 중복 row** — "4. Compute / Service Placement Decisions" 표에 `OD-MS-028` 로 시작하는 row 가 두 번 연속으로 등장한다(짧은 요약형 + 긴 상세형). 두 row 의 결정 내용은 실질적으로 같은 결정(Step 12 retry-normalizer 내장 정책)을 표현한다. 본 리팩토링 회차에서는 두 row 를 모두 그대로 유지했고, 어느 row 를 정본으로 남길지 / 하나로 통합할지는 후속 결정으로 분리한다.
- **결정 개수 재집계 필요** — `## Decision Summary`의 "전체: 97건" 은 02 spec 시점의 집계이며, 이후 03 ~ 10 spec 진행 중 신규 OD-* 결정이 계속 누적되었다. 카테고리별 상세 결정표 / Tentative / Deferred / Open 결정 모음 섹션에 등장하는 실제 OD-* 고유 ID 수와 다를 수 있다. 후속 회차에서 재집계를 권고한다.

---

## Status Legend

상태(status)의 내부 기준값은 영문(CONFIRMED, TENTATIVE, TBD, DEFERRED)을 유지한다. 운영자 표시용 표/섹션에서는 아래 한글/색상 라벨을 함께 사용해 가독성을 높인다.

| 영문 Status | 운영자 표시 | 의미 |
|-------------|-------------|------|
| CONFIRMED   | 🟢 확정     | 운영자 확정. 후속 spec에서 입력으로 사용한다. |
| TENTATIVE   | 🟡 잠정     | 임시 결정. 후속 spec 검토 후 변경 가능하다. |
| TBD         | 🔴 미정     | 아직 결정되지 않았다. 결정 후 본 표를 갱신한다. |
| DEFERRED    | 🔵 보류     | 본 spec 범위 밖으로 보류했다. 별도 시점에 재검토한다. |

---

## Decision Dashboard

본 문서에 누적된 결정 현황 한눈 요약이다.

> ⚠️ **집계 시점: 02 spec 기준 · 재집계 필요.** 이후 03 ~ 10 spec 진행 중 신규 `OD-*` 결정이 계속 누적되었으므로, 아래 count 는 카테고리별 상세 결정표 / Tentative / Deferred / Open 결정 모음 섹션의 실제 `OD-*` 고유 ID 수와 다를 수 있다. 후속 회차에서 재집계를 권고한다(Review Needed 이월).

### 결정 개수 (재집계 필요)

- 전체: 97건
- 🟢 확정 (CONFIRMED): 52건
- 🟡 잠정 (TENTATIVE): 42건
- 🔴 미정 (TBD): 2건
- 🔵 보류 (DEFERRED): 1건

### 비용 영향이 큰 결정

다음 결정은 월 단위 비용 또는 환경 단위 인프라 비용에 큰 영향을 미친다.

- `OD-ENV-001` AWS dev 환경 미구축 — dev RDS/ECS/NAT/ALB 비용 0
- `OD-NET-001` aws-paper NAT Gateway 미사용 — 약 $43/월 이상 절감
- `OD-NET-002` aws-live NAT Gateway 미사용(기본안) — 약 $86/월 이상 절감
- `OD-NET-005` VPC Endpoint 권고 세트 활성 — 약 $30 ~ $80/월(AZ 수 비례) 비용
- `OD-NET-007` / `OD-NET-008` ALB 초기 미사용 — 약 $16.5/월 절감
- `OD-RDS-002` vs `OD-RDS-003` aws-live single-AZ vs multi-AZ — RDS 비용 약 2배 차이
- `OD-RDS-007` aws-live backup retention 14일 — backup 저장 비용 중간 영향

### 후속 spec 영향이 큰 결정

다음 결정은 3개 이상의 후속 spec(03 ~ 10)에 직접 입력으로 영향을 준다.

- `OD-ENV-001` AWS dev 미구축 → 02, 03, 04, 05, 08, 09
- `OD-ENV-003` 1차 구축 환경 aws-paper → 03, 04, 08
- `OD-NET-001` paper NAT GW 미사용 → 02, 03, 08
- `OD-NET-002` live NAT GW 미사용(기본안) → 02, 03, 08
- `OD-SAFE-002` / `OD-SAFE-003` live 자동 BUY/SELL 단계적 도입 → 04, 05, 10
- `OD-SAFE-004` 자동 재시도 정책(idempotent만) → 04, 08, 10
- `OD-MS-009` Daily Batch orchestration(Step Functions + EventBridge) → 04, 05

---

## At a Glance — 핵심 결정 요약

운영자가 가장 자주 참조하는 핵심 결정만 추렸다. 상세 근거와 근거 표는 아래 카테고리별 결정표에서 확인한다.

| Area | Key Decision | Selected Option | Status | Cost Impact | Risk Level | Affected Specs |
|------|--------------|-----------------|--------|-------------|------------|----------------|
| Environment | `OD-ENV-001` AWS dev 환경 구축 여부 | 미구축 | 🟢 확정 | 큰 절감 | 중 (paper에 검증 부하 집중) | 02, 03, 04, 05, 08, 09 |
| Environment | `OD-ENV-003` AWS 1차 구축 환경 | aws-paper | 🟢 확정 | 중 | 중 (자동 주문은 paper에서 검증) | 03, 04, 08 |
| Network | `OD-NET-001` paper NAT Gateway | 미사용 | 🟢 확정 | 큰 절감 (~$43/월↑) | 중 (outbound 워크로드 분리 필요) | 02, 03, 08 |
| Network | `OD-NET-005` VPC Endpoint 활성 항목 | 권고 세트 (S3 GW + ECR api+dkr + Secrets + SSM + Logs) | 🟢 확정 | 비용 발생 (~$30 ~ $80/월) | 중 (endpoint 누락 시 AWS API 접근 실패) | 02, 06, 07 |
| Network | `OD-NET-009` 운영자 접근 방식 | SSM Session Manager만 사용 | 🟢 확정 | 0 | 높음 (SSH 22 0.0.0.0/0 절대 금지) | 02, 03 |
| RDS | `OD-RDS-001` aws-paper RDS 인스턴스 | db.t4g.small single-AZ | 🟢 확정 | 약 $26 + storage | 중 | 02, 04, 05 |
| RDS | `OD-RDS-003` aws-live RDS 안정성안 | multi-AZ (권고) | 🟢 확정 | 절감안 대비 약 2배 | 낮음 (자동 failover) | 02, 10 |
| Cutover | `OD-CUT-001` cutover 방식 | pg_dump + pg_restore 1순위 | 🟢 확정 | 0 | 중 (짧은 다운타임 허용) | 02, 10 |
| Safety | `OD-SAFE-001` paper 자동 BUY/SELL E2E | 초기 차단 → 검증 후 허용 | 🟢 확정 | 0 | 중 (모의투자 fill/position sync 오류 재현 필요) | 04, 10 |
| Safety | `OD-SAFE-002` live 자동 BUY 정책 | 후보+수동 승인 우선, 검증 후 단계적 자동 | 🟢 확정 | 0 | 높음 (잘못된 자동 재시도가 큰 손실) | 04, 05, 10 |
| Compute | `OD-MS-001` port-marketconnector 컴퓨트 | EC2+EIP | 🟢 확정 | EC2 단가 | 중 (broker IP 등록을 EIP에 묶음) | 03 |
| Compute | `OD-MS-009` Daily Batch orchestration | Step Functions + EventBridge Scheduler + ECS RunTask | 🟢 확정 | Step Functions transitions ≪ ECS Task | 중 (port-view subprocess 미사용) | 04, 05 |

---

## Decision Index

카테고리별 결정 인덱스다. 표는 짧은 인덱스 역할만 담고, 긴 선택지 상세 · 비용 영향 상세 · 운영 리스크 상세 · 후속 spec 영향 상세는 하단 `Decision Details` 로 이동한다(각 결정의 `Details` 컬럼 또는 표 셀 안 `See details: OD-XXX-YYY Details` anchor 참조).

핵심 컬럼: `Decision ID` · `Area` · `Decision` · `Selected` · `Status` · `Next` · `Details`. Status 값은 내부 기준(CONFIRMED / TENTATIVE / TBD / DEFERRED) 원문 유지 + 한글/색상 라벨(🟢 확정 · 🟡 잠정 · 🔴 미정 · 🔵 보류) 병기.

Decision ID · 선택값 · 비용 영향 · 운영 리스크 · 후속 spec 영향 · evidence 링크 원문은 카테고리별 표 및 `Decision Details` 에서 그대로 유지된다(fact-loss 방지 원칙 · Requirement 2 · 14).

### 1. Environment Decisions / 환경 구성

| Decision ID | Area | Decision | Selected | Status | Next | Details |
|-------------|------|----------|----------|--------|------|---------|
| OD-ENV-001 | Environment | AWS dev 환경 구축 여부 | 미구축 | 🟢 확정 | — (확정) | [See details: OD-ENV-001 Details](#od-env-001-details) |
| OD-ENV-002 | Environment | local-dev 운영 위치 | 기존 로컬 PostgreSQL 환경 유지 | 🟢 확정 | — (확정) | [See details: OD-ENV-002 Details](#od-env-002-details) |
| OD-ENV-003 | Environment | AWS 1차 구축 환경 | aws-paper | 🟢 확정 | — (확정) | [See details: OD-ENV-003 Details](#od-env-003-details) |
| OD-ENV-004 | Environment | aws-live 구축 시점 | paper 검증 후 후속 구축 | 🟢 확정 | — (확정) | [See details: OD-ENV-004 Details](#od-env-004-details) |
| OD-ENV-005 | Environment | VPC 분리 정책 | 단일 VPC 유지 | 🟢 확정 | — (확정) | [See details: OD-ENV-005 Details](#od-env-005-details) |
| OD-ENV-006 | Environment | Paper 환경 DB source of truth | AWS Paper RDS 단일 source of truth | 🟡 잠정 | — (잠정) | [See details: OD-ENV-006 Details](#od-env-006-details) |
| OD-ENV-007 | Environment | Local PC → AWS Paper RDS 접속 방식 | SSM Port Forwarding 만 사용 | 🟡 잠정 | — (잠정) | [See details: OD-ENV-007 Details](#od-env-007-details) |
| OD-ENV-008 | Environment | Local DB 와 AWS Paper RDS 간 동기화 정책 | 미사용 | 🟡 잠정 | — (잠정) | [See details: OD-ENV-008 Details](#od-env-008-details) |

### 2. Network Decisions / 네트워크

| Decision ID | Area | Decision | Selected | Status | Next | Details |
|-------------|------|----------|----------|--------|------|---------|
| OD-NET-001 | Network | NAT Gateway 사용 (aws-paper) | 미사용 | 🟢 확정 | — (확정) | [See details: OD-NET-001 Details](#od-net-001-details) |
| OD-NET-002 | Network | NAT Gateway 사용 (aws-live) | 미사용(기본안) | 🟢 확정 | — (확정) | [See details: OD-NET-002 Details](#od-net-002-details) |
| OD-NET-003 | Network | marketconnector outbound IP | EC2+EIP 유지 | 🟢 확정 | — (확정) | [See details: OD-NET-003 Details](#od-net-003-details) |
| OD-NET-004 | Network | crawler/preprocessor outbound 방식 | public subnet + assignPublicIp (1순위) | 🟡 잠정 | — (잠정) | [See details: OD-NET-004 Details](#od-net-004-details) |
| OD-NET-005 | Network | VPC Endpoint 활성 항목 | 권고(S3 GW + ECR api+dkr + Secrets + SSM + Logs) | 🟢 확정 | — (확정) | [See details: OD-NET-005 Details](#od-net-005-details) |
| OD-NET-006 | Network | STS / KMS Endpoint 활성 | 우선 미사용. 필요 시 활성 | 🟡 잠정 | — (잠정) | [See details: OD-NET-006 Details](#od-net-006-details) |
| OD-NET-007 | Network | ALB 사용 (aws-paper) | 초기 미사용 | 🟡 잠정 | — (잠정) | [See details: OD-NET-007 Details](#od-net-007-details) |
| OD-NET-008 | Network | ALB 사용 (aws-live) | 초기 비용 절감안 보류 | 🟡 잠정 | — (잠정) | [See details: OD-NET-008 Details](#od-net-008-details) |
| OD-NET-009 | Network | 운영자 접근 방식 | SSM Session Manager만 사용 | 🟢 확정 | — (확정) | [See details: OD-NET-009 Details](#od-net-009-details) |
| OD-NET-010 | Network | Local-to-AWS Paper RDS SSM Port Forwarding 표준 경유지 | `portfolio-paper-marketconnector-ec2` 단일 · local port `15433` → tunnel → AWS Paper RDS | 🟡 잠정 | — (잠정) | [See details: OD-NET-010 Details](#od-net-010-details) |
| OD-NET-011 | Network | Local-to-AWS Paper RDS pgAdmin4 사용 원칙 | `localhost:15433` (SSM tunnel) 만 등록. RDS endpoint 직접 등록 금지 | 🟡 잠정 | — (잠정) | [See details: OD-NET-011 Details](#od-net-011-details) |

### 3. RDS / Database Decisions

#### 3-1. RDS 인스턴스 / 백업

| Decision ID | Area | Decision | Selected | Status | Next | Details |
|-------------|------|----------|----------|--------|------|---------|
| OD-RDS-001 | RDS | aws-paper RDS 인스턴스 | db.t4g.small single-AZ | 🟢 확정 | — (확정) | [See details: OD-RDS-001 Details](#od-rds-001-details) |
| OD-RDS-002 | RDS | aws-live RDS 비용 절감안 | single-AZ 시작 가능 | 🟡 잠정 | — (잠정) | [See details: OD-RDS-002 Details](#od-rds-002-details) |
| OD-RDS-003 | RDS | aws-live RDS 안정성 우선안 | multi-AZ | 🟢 확정 | — (확정) | [See details: OD-RDS-003 Details](#od-rds-003-details) |
| OD-RDS-004 | RDS | PostgreSQL major version | 16 이상 | 🟢 확정 | — (확정) | [See details: OD-RDS-004 Details](#od-rds-004-details) |
| OD-RDS-005 | RDS | encryption at rest | aws-paper KMS default, aws-live CMK 권고 | 🟡 잠정 | — (잠정) | [See details: OD-RDS-005 Details](#od-rds-005-details) |
| OD-RDS-006 | RDS | backup retention (aws-paper) | 7일 | 🟢 확정 | — (확정) | [See details: OD-RDS-006 Details](#od-rds-006-details) |
| OD-RDS-007 | RDS | backup retention (aws-live) | 14일 | 🟢 확정 | — (확정) | [See details: OD-RDS-007 Details](#od-rds-007-details) |
| OD-RDS-008 | RDS | PITR | aws-paper on, aws-live on | 🟢 확정 | — (확정) | [See details: OD-RDS-008 Details](#od-rds-008-details) |
| OD-RDS-009 | RDS | manual snapshot 정책 | cutover 직전 + 분기 | 🟢 확정 | — (확정) | [See details: OD-RDS-009 Details](#od-rds-009-details) |

#### 3-2. DB schema / role

| Decision ID | Area | Decision | Selected | Status | Next | Details |
|-------------|------|----------|----------|--------|------|---------|
| OD-DB-001 | DB | DB 이름 | portfolio (모든 환경 동일) | 🟢 확정 | — (확정) | [See details: OD-DB-001 Details](#od-db-001-details) |
| OD-DB-002 | DB | schema 구성 | schema-per-domain 10개 유지 | 🟢 확정 | — (확정) | [See details: OD-DB-002 Details](#od-db-002-details) |
| OD-DB-003 | DB | 환경변수 키 호환 | INTEREST_DB_* / PORT_* / PORTFOLIO_DB_NAME 모두 유지 | 🟢 확정 | — (확정) | [See details: OD-DB-003 Details](#od-db-003-details) |
| OD-DB-004 | DB | DB role 분리 | 7개 role(marketconnector_app, crawler_app, preprocessor_app, decision_app, execution_app, research_app, view_app) | 🟢 확정 | — (확정) | [See details: OD-DB-004 Details](#od-db-004-details) |
| OD-DB-005 | DB | view_app 권한 | 모든 schema READ + ops WRITE 기본. execution write는 05에서 재검토 | 🟡 잠정 | — (잠정) | [See details: OD-DB-005 Details](#od-db-005-details) |
| OD-DB-006 | DB | search_path 정책 | MS별 README 그대로 유지 | 🟢 확정 | — (확정) | [See details: OD-DB-006 Details](#od-db-006-details) |
| OD-DB-007 | DB | legacy schema의 app role 권한 | 모든 app role에 USAGE / SELECT 미부여(2026-06-09 1차 적용 결과 반영) | 🟢 확정 | — (확정) | [See details: OD-DB-007 Details](#od-db-007-details) |
| OD-DB-008 | DB | marketconnector_app의 execution 권한 | R-only 축소(2026-06-09 1차 적용 반영) | 🟢 확정 | — (확정) | [See details: OD-DB-008 Details](#od-db-008-details) |
| OD-DB-009 | DB | view_app의 execution 권한 | R-only 유지(write 필요성은 05 spec에서 재검토) | 🟡 잠정 | — (잠정) | [See details: OD-DB-009 Details](#od-db-009-details) |
| OD-DB-010 | DB | 1차 적용 시 기존 객체 owner 일괄 이관 (REASSIGN OWNED) | 미실행(기존 table / sequence / index owner는 `portfolio_admin` 유지) | 🟢 확정 | — (확정) | [See details: OD-DB-010 Details](#od-db-010-details) |
| OD-DB-011 | DB | `execution_app` 의 `decision` schema UPDATE 권한 (Step 9 SELL execution link) | `decision.strategy_daily_position_decision` 제한적 UPDATE 만 부여 | 🟢 확정 | — (확정) | [See details: OD-DB-011 Details](#od-db-011-details) |
| OD-DB-012 | DB | `ops_recorder_app` 신규 role (Step Functions 실행 이력 OPS mirror 전용 · 2026-07-03) | 전용 최소 권한 role 신설. `ops` schema USAGE + `ops.strategy_daily_batch_run` · `ops.strategy_daily_batch_step_log` SELECT / INSERT / UPDATE + 관련 sequence USAGE / SELECT. DELETE 미부여. `view_app` 재사용 안 함 · `execution_app` 권한 확대 안 함. `chk_strategy_daily_batch_run_type` 에 `AWS_STEPFUNCTIONS` 값 추가(기존 `MANUAL` / `SCHEDULED` / `RETRY` / `MANUAL_PARTIAL` 유지) | 🟢 확정 | — (확정) | [See details: OD-DB-012 Details](#od-db-012-details) |

### 4. Compute / Service Placement Decisions

8개 MS별 AWS 컴퓨트 / orchestration 1순위 결정. 근거와 후보 비교는 루트 공통 [`ms-aws-service-decision-matrix.md`](./ms-aws-service-decision-matrix.md) 5장 / 4장 / 1.4장 참고. 본 표는 결정 락 기록만 다룬다.

| Decision ID | Area | Decision | Selected | Status | Next | Details |
|-------------|------|----------|----------|--------|------|---------|
| OD-MS-001 | Compute | port-marketconnector 컴퓨트 | EC2+EIP | 🟢 확정 | — (확정) | [See details: OD-MS-001 Details](#od-ms-001-details) |
| OD-MS-002 | Compute | port-view 컴퓨트 | ECS Fargate Service (1순위), Elastic Beanstalk (2순위 비교 본문 유지) | 🟢 확정 | — (확정) | [See details: OD-MS-002 Details](#od-ms-002-details) |
| OD-MS-003 | Compute | port-interest-crawler 컴퓨트 | ECS Fargate Task NAT-free public (1순위), ECS on EC2 (Selenium 안정성 미달 시 승격) | 🟢 확정 | — (확정) | [See details: OD-MS-003 Details](#od-ms-003-details) |
| OD-MS-004 | Compute | port-interest-preprocessor 컴퓨트 | ECS Fargate Task (1순위), Lambda는 짧은 step만 보조 | 🟢 확정 | — (확정) | [See details: OD-MS-004 Details](#od-ms-004-details) |
| OD-MS-005 | Compute | port_strategy_common 배포 | 별도 컴퓨트 없음. git submodule packaging (1순위), wheel+CodeArtifact (성숙기 2순위) | 🟢 확정 | — (확정) | [See details: OD-MS-005 Details](#od-ms-005-details) |
| OD-MS-006 | Compute | port_strategy_decision 컴퓨트 | ECS Fargate Task + EventBridge Scheduler (1순위, 04에서 Step Functions 통합), Lambda 비권고 | 🟢 확정 | — (확정) | [See details: OD-MS-006 Details](#od-ms-006-details) |
| OD-MS-007 | Compute | port_strategy_execution 컴퓨트 | ECS Fargate Task + Step Functions + EventBridge Scheduler (1순위), Lambda 비권고 | 🟢 확정 | — (확정) | [See details: OD-MS-007 Details](#od-ms-007-details) |
| OD-MS-008 | Compute | port_strategy_research 컴퓨트 | AWS Batch (1순위, Step Functions 보조), ECS Fargate Task (2순위), Lambda 비권고 | 🟢 확정 | — (확정) | [See details: OD-MS-008 Details](#od-ms-008-details) |
| OD-MS-009 | Compute | Daily Batch orchestration | Step Functions + EventBridge Scheduler + ECS RunTask | 🟢 확정 | — (확정) | [See details: OD-MS-009 Details](#od-ms-009-details) |
| OD-MS-010 | Compute | infra alarm 채널 | 도메인 알림은 SlackNotificationService 유지, 인프라 알람은 SNS → Lambda → Slack webhook fan-out | 🟡 잠정 | — (잠정) | [See details: OD-MS-010 Details](#od-ms-010-details) |
| OD-MS-011 | Compute | port-interest-crawler runtime 분리 (Hybrid execution model) | Hybrid(KRX GUI=Windows EC2 worker · non-GUI=ECS Fargate Task 후보). preprocessor=ECS Fargate Task | 🟡 잠정 | — (잠정) | [See details: OD-MS-011 Details](#od-ms-011-details) |
| OD-MS-012 | Compute | KRX GUI 의존 crawler 1차 운영 모드 | wrapper 기반 수동 실행(`run_krx_worker_daily.ps1`) | 🟡 잠정 | — (잠정) | [See details: OD-MS-012 Details](#od-ms-012-details) |
| OD-MS-013 | Compute | port_strategy_decision Task Definition 분리 정책 | buy-signal · position-signal 별도 Task Definition 2개 | 🟡 잠정 | — (잠정) | [See details: OD-MS-013 Details](#od-ms-013-details) |
| OD-MS-014 | Compute | port_strategy_common 1차 배포 방식 | 1차 ECS smoke image 에서는 vendoring | 🟡 잠정 | — (잠정) | [See details: OD-MS-014 Details](#od-ms-014-details) |
| OD-MS-015 | Compute | KRX GUI 의존 crawler 1차 자동화 방식 | SSM RunCommand → schtasks → Scheduled Task → Autologon session → wrapper | 🟡 잠정 | — (잠정) | [See details: OD-MS-015 Details](#od-ms-015-details) |
| OD-MS-016 | Compute | Strategy Execution / MarketConnector 주문 실행 책임 분리 | 책임 분리. Execution=READY→REQUESTED · Connector=REQUESTED→SUBMITTED/FAILED | 🟡 잠정 | — (잠정) | [See details: OD-MS-016 Details](#od-ms-016-details) |
| OD-MS-017 | Compute | port_strategy_execution Task Definition 운영 방식 | 단일 Task Definition + command override | 🟡 잠정 | — (잠정) | [See details: OD-MS-017 Details](#od-ms-017-details) |
| OD-MS-018 | Compute | port_strategy_research Batch image dependency boundary | Research 내부 adapter 로 이관. Batch image=research+common, decision 미포함 | 🟡 잠정 | — (잠정) | [See details: OD-MS-018 Details](#od-ms-018-details) |
| OD-MS-019 | Compute | port_strategy_research AWS Batch 포팅 대상 entrypoint + report artifact 보존 | 포팅 대상 = `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종 한정 | 🟡 잠정 | — (잠정) | [See details: OD-MS-019 Details](#od-ms-019-details) |
| OD-MS-020 | Compute | port-interest-crawler 상태 표현 / 완료 정의 | Interest Crawler = **hybrid 1차 구현 부분 완료** | 🟡 잠정 | — (잠정) | [See details: OD-MS-020 Details](#od-ms-020-details) |
| OD-MS-021 | Compute | Backend AWS E2E dry-run 17단계 순서 + 안전 기준 | 로컬 View Daily Batch 17단계 순서 그대로. 안전 기준 8종 | 🟡 잠정 | — (잠정) | [See details: OD-MS-021 Details](#od-ms-021-details) |
| OD-MS-022 | Compute | KRX GUI crawler 자동 로그인 기반 운영 방식 | Windows Autologon + Administrator session + Scheduled Task + SSM trigger | 🟡 잠정 | — (잠정) | [See details: OD-MS-022 Details](#od-ms-022-details) |
| OD-MS-023 | Compute | Daily AWS wrapper 운영 정책 (운영자 로컬 PowerShell 도구) | 로컬 Windows PowerShell wrapper 분리 파일 구조(main + config + functions + step 17개) | 🟡 잠정 | — (잠정) | [See details: OD-MS-023 Details](#od-ms-023-details) |
| OD-MS-024 | Compute | 추가매수 허용 정책 + position_state 병합 방식 | 추가매수 허용 + merge. `merge_open_position_state()` 로 가중평균 병합 + idempotency | 🟡 잠정 | — (잠정) | [See details: OD-MS-024 Details](#od-ms-024-details) |
| OD-MS-025 | Compute | MarketConnector `connector_order_check.py` 운영 모드 (Step 13 체결조회) | active 주문 단건 순차 조회 기본 + broad 옵션 격리 + 내부 분기 | 🟡 잠정 | — (잠정) | [See details: OD-MS-025 Details](#od-ms-025-details) |
| OD-MS-026 | Compute | Step 2 INTEREST_CRAWLER 운영 성공 기준 (wrapper 성공판정 강화) | Task trigger + Running→Ready wait + Last Result 0 + worker log + KRX raw DB validation 모두 충족 | 🟡 잠정 | — (잠정) | [See details: OD-MS-026 Details](#od-ms-026-details) |

위 결정은 `ms-aws-service-decision-matrix.md` 5장 최종 권고안과 정합되며, 본 spec(02)와 후속 spec(03 ~ 10)에서 입력으로 사용한다. Lambda는 모든 핵심 batch 워크로드에서 비권고이며, infra alarm fan-out / 짧은 보조 후처리 / S3 metadata 처리 같은 보조 용도로만 사용한다.

### 5. Security / Secrets / IAM Decisions

| Decision ID | Area | Decision | Selected | Status | Next | Details |
|-------------|------|----------|----------|--------|------|---------|
| OD-SEC-001 | Security | Secrets 보관 위치 | 06에서 최종 결정. 본 spec은 placeholder | 🔴 미정 | — (미정) | [See details: OD-SEC-001 Details](#od-sec-001-details) |
| OD-SEC-002 | Security | RDS master password 보관 | Secrets Manager(권고) | 🟡 잠정 | — (잠정) | [See details: OD-SEC-002 Details](#od-sec-002-details) |
| OD-SEC-003 | Security | KIS access_token 보관 | EC2 로컬+S3 backup 1순위 | 🟡 잠정 | — (잠정) | [See details: OD-SEC-003 Details](#od-sec-003-details) |
| OD-SEC-004 | Security | EC2 SSH 22 inbound | 미오픈. SSM Session Manager만 사용 | 🟢 확정 | — (확정) | [See details: OD-SEC-004 Details](#od-sec-004-details) |
| OD-SEC-005 | Security | EC2 / 8개 MS Access Key 미사용 원칙 | EC2 안 access key 저장 금지. IMDSv2 + Instance Role 또는 ECS Task Role 만 사용 | 🟡 잠정 | — (잠정) | [See details: OD-SEC-005 Details](#od-sec-005-details) |
| OD-SEC-006 | Security | EC2 / ECS IAM Role 기반 secret / parameter read 원칙 | 최소 권한. Resource wildcard 금지. Action wildcard 금지 | 🟡 잠정 | — (잠정) | [See details: OD-SEC-006 Details](#od-sec-006-details) |
| OD-SEC-007 | Security | EC2 운영자 접근 = SSM Session Manager 중심 | `AmazonSSMManagedInstanceCore` attach + SSM Session Manager 진입 중심 | 🟡 잠정 | — (잠정) | [See details: OD-SEC-007 Details](#od-sec-007-details) |
| OD-SEC-008 | Security | KRX 로그인 자격 보관 | Secrets Manager (`/portfolio/{env}/krx/crawler-login`, JSON `username` / `password`). EC2 worker IAM Role inline poli... | 🟡 잠정 | — (잠정) | [See details: OD-SEC-008 Details](#od-sec-008-details) |

### 6. Observability / Alerting Decisions

| Decision ID | Area | Decision | Selected | Status | Next | Details |
|-------------|------|----------|----------|--------|------|---------|
| OD-OBS-001 | Observability | CloudWatch Logs 사용 | CloudWatch Logs 사용 | 🟢 확정 | — (확정) | [See details: OD-OBS-001 Details](#od-obs-001-details) |
| OD-OBS-002 | Observability | CloudWatch Logs retention (aws-paper) | 7일 시작 | 🟡 잠정 | — (잠정) | [See details: OD-OBS-002 Details](#od-obs-002-details) |
| OD-OBS-003 | Observability | CloudWatch Logs retention (aws-live) | 14일 시작, 운영 안정 후 30일로 상향 가능 | 🟡 잠정 | — (잠정) | [See details: OD-OBS-003 Details](#od-obs-003-details) |
| OD-OBS-004 | Observability | Slack webhook 보관 | 06에서 최종 결정 | 🔴 미정 | — (미정) | [See details: OD-OBS-004 Details](#od-obs-004-details) |

### 7. CI/CD Decisions

본 시점(02 spec)에서는 CI/CD 단독 결정이 아직 락되지 않았다. 컴퓨트 결정(OD-MS-* 4번 카테고리)이 우선이며, 본 카테고리는 후속 spec(주로 07 / 08 / 09)에서 추가된다.

- 신규 결정이 발생하면 `OD-CICD-XXX` 형태로 본 섹션에 추가한다.
- 현재는 placeholder 상태로 카테고리만 유지한다.

### 8. Cutover / Operation Decisions

#### 8-1. Cutover

| Decision ID | Area | Decision | Selected | Status | Next | Details |
|-------------|------|----------|----------|--------|------|---------|
| OD-CUT-001 | Cutover | cutover 방식 | pg_dump+pg_restore 1순위 | 🟢 확정 | — (확정) | [See details: OD-CUT-001 Details](#od-cut-001-details) |
| OD-CUT-002 | Cutover | AWS DMS 도입 | 보류 | 🔵 보류 | — (보류) | [See details: OD-CUT-002 Details](#od-cut-002-details) |
| OD-CUT-003 | Cutover | aws-live cutover 시점 | paper 검증 후 | 🟢 확정 | — (확정) | [See details: OD-CUT-003 Details](#od-cut-003-details) |
| OD-CUT-004 | Cutover | local-dev 유지 기간 | 병행 운영(rollback 보험용) | 🟡 잠정 | — (잠정) | [See details: OD-CUT-004 Details](#od-cut-004-details) |

#### 8-2. 운영 안전장치 (자동화 / safety)

| Decision ID | Area | Decision | Selected | Status | Next | Details |
|-------------|------|----------|----------|--------|------|---------|
| OD-SAFE-001 | Safety | aws-paper 자동 BUY/SELL E2E | 초기 차단 → 검증 후 허용 | 🟢 확정 | — (확정) | [See details: OD-SAFE-001 Details](#od-safe-001-details) |
| OD-SAFE-002 | Safety | aws-live 자동 BUY | 후보+수동 승인 우선, 검증 후 단계적 | 🟢 확정 | — (확정) | [See details: OD-SAFE-002 Details](#od-safe-002-details) |
| OD-SAFE-003 | Safety | aws-live 자동 SELL | OD-SAFE-002와 동일 정책 | 🟢 확정 | — (확정) | [See details: OD-SAFE-003 Details](#od-safe-003-details) |
| OD-SAFE-004 | Safety | 자동 재시도 정책 | idempotent step만 자동 재시도 | 🟢 확정 | — (확정) | [See details: OD-SAFE-004 Details](#od-safe-004-details) |

---

## Open Decisions / 🔴 미정 결정

상태가 `TBD`인 항목만 모았다. 운영자가 아직 결정해야 하는 항목이다. 결정이 락되면 본 섹션에서 제거하지 말고 카테고리별 상세 결정표의 Status만 갱신하고, 본 섹션은 다음 갱신 시점에 동기화한다.

| Decision ID | 결정 항목 | 선택값(현재) | Status | 후속 Spec |
|-------------|-----------|--------------|--------|-----------|
| OD-SEC-001 | Secrets 보관 위치 | 06에서 최종 결정. 본 spec은 placeholder | 🔴 미정 | 06 |
| OD-OBS-004 | Slack webhook 보관 | 06에서 최종 결정 | 🔴 미정 | 06 |

운영자 액션:

- 06-secrets-and-iam spec 진입 시 두 결정을 함께 락한다.
- 락 후 본 표의 Status는 🟢 확정 또는 🟡 잠정으로 갱신한다.

---

## Tentative Decisions / 🟡 잠정 결정

상태가 `TENTATIVE`인 항목만 모았다. 후속 spec에서 재검토가 필요한 항목이다.

| Decision ID | 결정 항목 | 선택값(현재) | Status | 재검토 시점 |
|-------------|-----------|--------------|--------|-------------|
| OD-NET-004 | crawler/preprocessor outbound 방식 | public subnet + assignPublicIp (1순위) | 🟡 잠정 | 08 |
| OD-NET-006 | STS / KMS Endpoint 활성 | 우선 미사용. 필요 시 활성 | 🟡 잠정 | 02, 06 |
| OD-NET-007 | ALB 사용 (aws-paper) | 초기 미사용 | 🟡 잠정 | 05 |
| OD-NET-008 | ALB 사용 (aws-live) | 초기 비용 절감안 보류 | 🟡 잠정 | 05 |
| OD-NET-010 | Local-to-AWS Paper RDS SSM Port Forwarding 표준 경유지 | `portfolio-paper-marketconnector-ec2` 단일 / local port `15433`. See details: OD-NET-010 Details | 🟡 잠정 | 02, 03, 04, 05, 06 |
| OD-NET-011 | Local-to-AWS Paper RDS pgAdmin4 사용 원칙 | `localhost:15433` (SSM tunnel) 만 등록. RDS endpoint 직접 등록 금지. See details: OD-NET-011 Details | 🟡 잠정 | 02, 03, 04, 05, 10 |
| OD-RDS-002 | aws-live RDS 비용 절감안 | single-AZ 시작 가능 | 🟡 잠정 | 02, 10 |
| OD-RDS-005 | encryption at rest | aws-paper KMS default, aws-live CMK 권고 | 🟡 잠정 | 02, 06 |
| OD-DB-005 | view_app 권한 | 모든 schema READ + ops WRITE 기본. execution write는 05에서 재검토 | 🟡 잠정 | 02, 05 |
| OD-DB-009 | view_app의 execution 권한 | R-only 유지(write 필요성은 05 spec에서 재검토) | 🟡 잠정 | 02, 05 |
| OD-CUT-004 | local-dev 유지 기간 | 병행 운영(rollback 보험용) | 🟡 잠정 | 10 |
| OD-OBS-002 | CloudWatch Logs retention (aws-paper) | 7일 시작 | 🟡 잠정 | 02, 05 |
| OD-OBS-003 | CloudWatch Logs retention (aws-live) | 14일 시작, 운영 안정 후 30일로 상향 가능 | 🟡 잠정 | 05, 10 |
| OD-SEC-002 | RDS master password 보관 | Secrets Manager(권고) | 🟡 잠정 | 06 |
| OD-SEC-003 | KIS access_token 보관 | EC2 로컬+S3 backup 1순위 | 🟡 잠정 | 03 |
| OD-MS-010 | infra alarm 채널 | 도메인 알림=SlackNotificationService · 인프라 알람=SNS→Lambda→Slack fan-out | 🟡 잠정 | 05, 10 |
| OD-SEC-005 | EC2 / 8개 MS Access Key 미사용 원칙 | EC2 안 access key 저장 금지. IMDSv2 + Role 만 사용. See details: OD-SEC-005 Details | 🟡 잠정 | 03, 06 |
| OD-SEC-006 | EC2 / ECS IAM Role 기반 secret / parameter read 원칙 | 최소 권한. Resource/Action wildcard 금지. See details: OD-SEC-006 Details | 🟡 잠정 | 03, 06 |
| OD-SEC-007 | EC2 운영자 접근 = SSM Session Manager 중심 | `AmazonSSMManagedInstanceCore` + SSM Session Manager. See details: OD-SEC-007 Details | 🟡 잠정 | 03, 06, 10 |
| OD-MS-011 | port-interest-crawler runtime 분리 (Hybrid execution model) | Hybrid(KRX GUI=Windows EC2 · non-GUI=ECS Fargate). See details: OD-MS-011 Details | 🟡 잠정 | 08, 04, 05, 09, 10 |
| OD-MS-012 | KRX GUI 의존 crawler 1차 운영 모드 | wrapper 기반 수동 실행 1차 운영 모드. See details: OD-MS-012 Details | 🟡 잠정 | 08 |
| OD-SEC-008 | KRX 로그인 자격 보관 | Secrets Manager (`/portfolio/{env}/krx/crawler-login`, JSON) | 🟡 잠정 | 06, 08 |
| OD-MS-013 | port_strategy_decision Task Definition 분리 정책 | buy-signal · position-signal 별도 Task Definition 2개. See details: OD-MS-013 Details | 🟡 잠정 | 04, 05 |
| OD-MS-014 | port_strategy_common 1차 배포 방식 | 1차 ECS smoke image 는 vendoring. See details: OD-MS-014 Details | 🟡 잠정 | 04, 05, 07, 09 |
| OD-MS-015 | KRX GUI 의존 crawler 1차 자동화 방식 | SSM RunCommand → schtasks → Scheduled Task → Autologon session → wrapper. See details: OD-MS-015 Details | 🟡 잠정 | 08 |
| OD-MS-016 | Strategy Execution / MarketConnector 주문 실행 책임 분리 | Execution=READY→REQUESTED · Connector=REQUESTED→SUBMITTED/FAILED. See details: OD-MS-016 Details | 🟡 잠정 | 03, 04, 05 |
| OD-MS-017 | port_strategy_execution Task Definition 운영 방식 | 단일 Task Definition + command override. See details: OD-MS-017 Details | 🟡 잠정 | 04, 05, 10 |
| OD-MS-018 | port_strategy_research Batch image dependency boundary | Research 내부 adapter. image=research+common, decision 미포함. See details: OD-MS-018 Details | 🟡 잠정 | 09, 07, 10 |
| OD-MS-019 | port_strategy_research AWS Batch 포팅 대상 entrypoint + report artifact 보존 | `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종 한정. See details: OD-MS-019 Details | 🟡 잠정 | 09, 04, 05, 06, 07, 10 |
| OD-MS-020 | port-interest-crawler 상태 표현 / 완료 정의 | Interest Crawler = hybrid 1차 구현 부분 완료. See details: OD-MS-020 Details | 🟡 잠정 | 08, 04, 05, 09 |
| OD-MS-021 | Backend AWS E2E dry-run 17단계 순서 + 안전 기준 | 17단계 순서 유지 + 안전 기준 8종. See details: OD-MS-021 Details | 🟡 잠정 | 08, 04, 05, 09, 10 |
| OD-MS-022 | KRX GUI crawler 자동 로그인 기반 운영 방식 | Windows Autologon + Admin session + Scheduled Task + SSM trigger. See details: OD-MS-022 Details | 🟡 잠정 | 08, 05, 10 |
| OD-MS-023 | Daily AWS wrapper 운영 정책 (운영자 로컬 PowerShell 도구) | 로컬 PowerShell wrapper 분리 파일 구조. 환경=`aws-paper` 만. See details: OD-MS-023 Details | 🟡 잠정 | 03, 04, 05, 08, 09, 10 |
| OD-MS-024 | 추가매수 허용 정책 + position_state 병합 방식 | 추가매수 허용 + 기존 OPEN row merge + idempotency. See details: OD-MS-024 Details | 🟡 잠정 | 04, 05, 10 |
| OD-MS-025 | MarketConnector `connector_order_check.py` 운영 모드 (Step 13 체결조회 방식) | active 주문 단건 순차 조회 기본 + broad 격리. See details: OD-MS-025 Details | 🟡 잠정 | 03, 04, 05, 10 |
| OD-MS-026 | Step 2 INTEREST_CRAWLER 운영 성공 기준 (wrapper 성공판정 강화) | trigger + wait + Last Result 0 + worker log + KRX raw DB validation 모두 충족. See details: OD-MS-026 Details | 🟡 잠정 | 08, 04, 05, 10 |
| OD-MS-027 | MarketConnector env bootstrap 재생성 운영 정책 (`/tmp/inject-env.sh` 휘발 대응) | wrapper 공통 함수 재생성. Step 1/12/13/17 진입 직전 호출. See details: OD-MS-027 Details | 🟡 잠정 | 03, 04, 06, 10 |
| OD-MS-028 | Step 12 retry-normalizer 내장 정책 (장종료 REJECTED · `40580000` 후 다음날 자동 재제출 경로 대응) | Step 12 시작부 내장 · 복구 조건 6종 충족 시. See details: OD-MS-028 Details | 🟡 잠정 | 03, 04, 10 |
| OD-MS-028 | Step 12 retry-normalizer 내장 정책 (재검토 후보 · 중복 row) | Step 12 시작부 내장 · 복구 조건 6종 만족 시 (중복 row · Review Needed 참조). See details: OD-MS-028 Details | 🟡 잠정 | 03, 04, 10 |
| OD-MS-029 | Daily AWS Paper Step Functions approval workflow false / true path 운영 절차 | false path 사전 검증 후 true path 승인 실행. See details: OD-MS-029 Details | 🟢 확정 | 04, 10 |
| OD-MS-030 | AWS 공통 Slack notifier Lambda 도입 정책 (운영 이벤트 알림 보조 계층) | Lambda `portfolio-event-notifier` 기반 AWS 공통 notifier. See details: OD-MS-030 Details | 🟡 잠정 | 04, 05, 10 |
| OD-MS-031 | Step Functions / EventBridge 1차 Slack 연동 범위 3종 한정 정책 | 3종(APPROVAL_REQUIRED + DAILY_EXECUTION_SUCCESS/FAILED) 한정. See details: OD-MS-031 Details | 🟢 확정 | 04, 05, 10 |
| OD-MS-032 | EventBridge Scheduler + Dispatcher Lambda 기반 Daily 자동화 구조 | 2개 Scheduler + Dispatcher Lambda + 단계적 활성화. See details: OD-MS-032 Details | 🟢 확정 | 04, 05, 10 |
| OD-MS-033 | Step 12 retry-normalizer 재시도 확장 + KIS rate-limit backoff + 09:01 ENABLE 보류 | `40580000` + `EGW00201` 재시도 · sleep/backoff · 09:01 auto ENABLE 보류. See details: OD-MS-033 Details | 🟢 확정 | 03, 04, 05, 10 |
| OD-MS-034 | EC2 lifecycle 자동 실행 구성 (07:50 start / Step 1~11 성공 시 Crawler stop / 15:50 stop) | EventBridge Scheduler + Lambda 기반 EC2 start/stop. See details: OD-MS-034 Details | 🟢 확정 | 03, 04, 05, 08, 10 |
| OD-MS-035 | 장중 포지션 확인 3단계 구조 (Snapshot Refresh + Intraday Evaluate + Stop Sell Submit & Refresh) | 3단계 분리 + 신규 파일 + Submit 초기 수동·승인 후. See details: OD-MS-035 Details | 🟢 확정 | 03, 04, 05, 10 |
| OD-MS-036 | Intraday Stop Sell Submit Workflow (장중 손절 주문 제출 전용 State Machine + 수동 승인 운영) | 별도 state machine `portfolio-paper-intraday-stop-sell-approval` + 수동 승인. See details: OD-MS-036 Details | 🟢 확정 | 03, 04, 05, 10 |
| OD-MS-038 | Daily Brief Slack mini workflow 운영 방식 (Daily 주문 실행 경로와 분리) | 별도 mini Step Functions + Builder Lambda + Notifier Lambda + Scheduler 2개. See details: OD-MS-038 Details | 🟢 확정 | 04, 05, 10 |
| OD-MS-039 | Step Functions 실행 이력 OPS mirror 운영 방식 (Recorder Lambda + 전용 최소 권한 role · 2026-07-03) | Recorder Lambda `portfolio-daily-batch-ops-recorder`(Python 3.12 / ap-northeast-2 / VPC Lambda / Secrets Manager `ops_recorder_app` secret 사용 / action `RECORD_START` · `RECORD_STEP` · `RECORD_SUCCESS` · `RECORD_FAILURE`) 를 Step Functions 실행 role 에 `lambda:InvokeFunction` 한정 부여 후 State Machine 이 `ops.strategy_daily_batch_run` + `ops.strategy_daily_batch_step_log` 에 run-level + 대표 workflow step 을 mirror 한다. 대상 State Machine 은 `portfolio-paper-daily-step1-17-approval` + `portfolio-paper-daily-step12-17-approval` 2종. 1차 범위는 전체 세부 step mirror 가 아니라 run-level + 대표 workflow step 중심. View 는 reader / controller 역할 유지. See details: OD-MS-039 Details | 🟢 확정 | 04, 05, 10 |
| OD-MS-040 | ECS batch container Asia/Seoul timezone 정책 (Daily market-date 계산 포함 컨테이너 · 2026-07-08) | Daily market-date 계산을 포함하는 ECS batch container 는 UTC 기본값에 의존하지 않고 `TZ=Asia/Seoul` 을 명시한다. 단기 조치는 TaskDefinition env 에 `TZ=Asia/Seoul` 추가(`portfolio-paper-interest-crawler:8` · `portfolio-paper-interest-preprocessor:2`) + State Machine `portfolio-paper-daily-step1-17-approval` 의 Step2A_RunInterestCrawlerNongui · Step3_RunPreprocessor task revision 갱신. 근본 개선은 코드에서 `datetime.now()` 직접 사용을 timezone-aware helper 로 대체하여 컨테이너 default TZ 의존을 제거하는 것(후속). See details: OD-MS-040 Details | 🟢 확정 | 04, 08 |

| OD-MS-037 | View Local AWS Paper read-only 1차 scope 확정 + ECS / Fargate 진입 전 batch 2차 검증 선행 정책 | read-only 1차 scope 확정 + ECS 진입 전 batch 2차 검증 선행. See details: OD-MS-037 Details | 🟢 확정 | 04, 05, 10 |
| OD-ENV-006 | Paper 환경 DB source of truth | AWS Paper RDS 단일 source of truth. `PORT_ENVIRONMENT=paper` 이면 로컬 실행에서도 AWS Paper RDS 사용 | 🟡 잠정 | 02, 03, 04, 05, 08, 09, 10 |
| OD-ENV-007 | Local PC → AWS Paper RDS 접속 방식 | SSM Port Forwarding 만 사용. RDS 는 Private 유지. 실제 paper 주문 실행 guard 는 `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` 조합 | 🟡 잠정 | 02, 03, 04, 05 |
| OD-ENV-008 | Local DB 와 AWS Paper RDS 간 동기화 정책 | 미사용. `connector_order_request` / `connector_fill` / `strategy_execution_order` / `strategy_position_state` 병합 금지 | 🟡 잠정 | 02, 03, 04, 10 |

---

## Deferred Decisions / 🔵 보류 결정

상태가 `DEFERRED`인 항목만 모았다. 본 spec 범위 밖으로 보류했고, 별도 시점에 재검토한다.

| Decision ID | 결정 항목 | 선택값(현재) | Status | 재검토 조건 |
|-------------|-----------|--------------|--------|-------------|
| OD-CUT-002 | AWS DMS 도입 | 보류 | 🔵 보류 | 무중단 cutover가 꼭 필요해질 때(10 spec 시점) 재검토 |

---

## Decision Details

카테고리별 상세 결정표 / Tentative / Open / Deferred 결정 모음 표에서 `See details: OD-XXX-YYY Details` 로 참조된 상세 근거를 모은 섹션이다. 결정 요약 표는 dashboard 성격으로 짧게 유지하고, 본 섹션에서 각 결정의 상세 근거(선택값 상세 / 운영 리스크 상세 / 비용 상세 / 검증 이력 등)를 보존한다. Decision ID / 날짜 / status / 비용 수치 / spec 번호 / evidence 링크는 원문 그대로 유지한다.

### OD-ENV-006 Details

- Decision ID: OD-ENV-006
- Selected: AWS Paper RDS 단일 source of truth.
  - 선택값 상세: AWS Paper RDS 단일 source of truth (`PORT_ENVIRONMENT=paper` 이면 로컬 실행에서도 AWS Paper RDS 를 바라본다).
- Status: 🟡 잠정
- 선택지: AWS Paper RDS 단일 / local PostgreSQL 병행 / 환경별 분리
- 비용 영향 상세: 0 (RDS 비용은 OD-RDS-001 별도)
- 운영 리스크 상세: local DB 와 AWS Paper RDS 간 주문 / 체결 / 포지션 병합 / 동기화는 운영자 실수의 가장 큰 risk(R-DATA-007 정합). local 은 `LOCAL_DEV` fixture / 실험 / 백업 참고용으로만 사용.
- 후속 spec 영향: 02, 03, 04, 05, 08, 09, 10
- 근거 링크: — (없음)

### OD-ENV-007 Details

- Decision ID: OD-ENV-007
- Selected: SSM Port Forwarding 만 사용.
  - 선택값 상세: SSM Port Forwarding 만 사용. RDS 는 Private 유지(R-SEC-001 / R-NET-004 / OD-NET-009 정합). 로컬 host 는 `localhost:15433` 같은 포트로 접속하지만 실제 대상은 AWS Paper RDS. 실제 paper 주문 실행 guard 는 `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` 조합으로 판단.
- Status: 🟡 잠정
- 선택지: SSM Port Forwarding / RDS Public 허용 / VPN
- 비용 영향 상세: 0 (SSM Port Forwarding 자체 비용 없음)
- 운영 리스크 상세: RDS Public 허용 시 R-SEC-001 위반. DB host 가 `localhost` 라도 실제 대상은 AWS Paper RDS 일 수 있어 host 만으로 환경 식별 금지.
- 후속 spec 영향: 02, 03, 04, 05
- 근거 링크: — (없음)

### OD-ENV-008 Details

- Decision ID: OD-ENV-008
- Selected: 미사용.
  - 선택값 상세: 미사용. `connector_order_request` / `connector_fill` / `strategy_execution_order` / `strategy_position_state` 병합 금지.
- Status: 🟡 잠정
- 선택지: 양방향 동기화 / 단방향 / 미사용
- 비용 영향 상세: 0
- 운영 리스크 상세: 동기화 도입 시 데이터 정합성 / 중복 주문 / fill 중복 / position 상태 충돌 risk(R-DATA-007).
  - 동기화 도입 시 데이터 정합성 / 중복 주문 / fill 중복 / position 상태 충돌 risk(R-DATA-007). 후속 분리 시에도 단방향(local → AWS) 만 허용 후보.
- 후속 spec 영향: 02, 03, 04, 10
- 근거 링크: — (없음)

### OD-NET-010 Details

- Decision ID: OD-NET-010
- Selected: `portfolio-paper-marketconnector-ec2` 단일 · local port `15433` → tunnel → AWS Paper RDS.
  - 선택값 상세: `portfolio-paper-marketconnector-ec2` (instance id `i-0fce77927b7397b88`) 단일 사용. local port `15433` → SSM Session Manager tunnel → 본 EC2 → AWS Paper RDS `portfolio-paper-rds:5432`.
    본 EC2 는 03 spec 의 정식 운영 EC2 와 동일하며, RDS 접근 검증 이력(2026-06-09 RDS restore runner) 보유. `portfolio-paper-crawler-worker` 는 KRX GUI / Windows worker 전담으로 SSM Port Forwarding 경유지 사용하지 않음.
- Status: 🟡 잠정
- 선택지: 여러 후보 검토
- 비용 영향 상세: 0 (SSM Port Forwarding)
- 운영 리스크 상세: tunnel 종료 시 DB 접속 단절 (R-AUTO-012)
  - tunnel 창 종료 시 DB 접속 즉시 단절(R-AUTO-012 정합). target EC2 가 Stop 상태이거나 SSM Online 상실 시 tunnel 기동 실패 — Plugin / IAM / VPC Endpoint 5종(`com.amazonaws.<region>.ssm` / `ssmmessages` / `ec2messages`) 점검 후 재시도. instance id 는 운영 식별자로서 본 결정에 포함, secret 가 아님.
- 후속 spec 영향: 02, 03, 04, 05, 06
- 근거 링크: — (없음)

### OD-NET-011 Details

- Decision ID: OD-NET-011
- Selected: `localhost:15433` (SSM tunnel) 만 등록. RDS endpoint 직접 등록 금지.
  - 선택값 상세: `localhost:15433` (SSM Port Forwarding tunnel) 만 등록.
    - pgAdmin4 Register Server 의 Host name/address 는 `localhost`, Port 는 `15433`, Maintenance database 는 `portfolio`, Username 은 `portfolio_admin` 또는 MS 별 app role, SSL mode 는 `Prefer` 또는 기본 TLS 자동 연결.
    - RDS endpoint(`portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com`) 직접 등록 금지 — RDS Public 접근 미허용(R-SEC-001 / R-NET-004) 정책 정합 + DB host 만으로 환경 식별 금지 정책(OD-ENV-007) 정합.
    - SSM tunnel(앞 OD-NET-010) 가 열려 있는 동안만 접속 성립, tunnel 종료 시 pgAdmin4 연결 즉시 단절.
- Status: 🟡 잠정
- 선택지: RDS endpoint 직접 / SSM tunnel
- 비용 영향 상세: 0
- 운영 리스크 상세: tunnel 미오픈 시 접속이 외부로 향하지 않음 (R-SEC-001)
  - tunnel 미오픈 상태에서 pgAdmin4 가 RDS endpoint 직접 등록되어 있으면 접속 시도가 외부에서 RDS 로 향하지 않음(RDS Private 유지 / R-SEC-001 정합).
    운영자 실수로 paper / live 환경 RDS 를 잘못 등록하지 않도록 서버 이름 prefix 정책(예: `AWS Paper RDS - portfolio`) 표준화 후속(`_common/followups-overview.md` 2026-06-13 SSM Port Forwarding 후속 메모 보강 §3). 비밀번호 / secret value 본 결정 / 운영 노트 / 콘솔 캡처 / 로그 평문 기록 금지(R-DOCS-001 정합).
- 후속 spec 영향: 02, 03, 04, 05, 10
- 근거 링크: — (없음)

### OD-DB-011 Details

- Decision ID: OD-DB-011
- Selected: `decision.strategy_daily_position_decision` 제한적 UPDATE 만 부여.
  - 선택값 상세: `decision.strategy_daily_position_decision` 제한적 UPDATE 만 부여(`execution_app` 의 SELL execution link 책임 한정).
    - `execution_app` 은 Step 9 `DAILY_SELL_EXECUTION` 흐름에서 SELL execution_order 생성 후 daily position decision row 의 `execution_order_id` 를 UPDATE 해야 한다.
    - SELECT + UPDATE 두 권한만 부여(INSERT / DELETE / TRUNCATE 미부여). `decision` schema 전체 UPDATE 는 부여하지 않는다(권한 최소화 정합 / OD-DB-007 정책의 SELECT-only 원칙은 본 결정의 한정된 예외 외 다른 테이블에는 그대로 유지).
    - 본 결정은 2026-06-22 Daily AWS Paper Step 9 1차 실패(`execution_app` UPDATE 권한 누락) 후 운영자가 직접 `GRANT USAGE ON SCHEMA decision TO execution_app` + `GRANT UPDATE ON TABLE decision.strategy_daily_position_decision TO execution_app` 수행 + Step 9 재실행 통과 후의 결정 락.
    - 02 spec db-roles-and-grants 정식 매트릭스 갱신은 후속 책임(R-DATA-005 [2026-06-22 보강] / R-DATA-013 신규 정합).
- Status: 🟢 확정
- 선택지: 미부여(SELECT 한정 유지) / `decision.strategy_daily_position_decision` 제한적 UPDATE 부여 / `decision` schema 전체 UPDATE 부여
- 비용 영향 상세: 0
- 운영 리스크 상세: UPDATE 권한이 `decision.strategy_daily_position_decision` 한 테이블에 한정.
  - UPDATE 권한이 `decision.strategy_daily_position_decision` 한 테이블에 한정되므로 권한 분리 강화 / `execution_app` 이 decision 계열 다른 테이블 / `decision.strategy_daily_signal` / `decision.strategy_daily_run` 등에 대해 UPDATE 권한을 갖지 않는 상태 유지.
- 후속 spec 영향: 02, 04
- 근거 링크: — (없음)

### OD-SEC-005 Details

- Decision ID: OD-SEC-005
- Selected: EC2 안 access key 저장 금지. IMDSv2 + Instance Role 또는 ECS Task Role 만 사용.
  - 선택값 상세: EC2 안 access key 파일·환경변수·dotfile·systemd `EnvironmentFile=` 저장 금지. IMDSv2 + Instance Role 또는 ECS Task Role 만 사용.
- Status: 🟡 잠정
- 선택지: 허용 / 금지
- 비용 영향 상세: 0
- 운영 리스크 상세: 위반 시 IAM Console 즉시 폐기 + IMDSv2 + Role only 모드 복귀.
  - 위반 시 IAM Console 즉시 폐기 + `~/.aws/credentials` 백업 이동 후 IMDSv2 + Role only 모드 복귀.
- 후속 spec 영향: 03, 04, 05, 06, 08, 09
- 근거 링크: — (없음)

### OD-SEC-006 Details

- Decision ID: OD-SEC-006
- Selected: 최소 권한. Resource wildcard 금지. Action wildcard 금지.
  - 선택값 상세: 최소 권한. Resource wildcard 금지. Action wildcard 금지(`secretsmanager:*` / `ssm:*` / `*` 모두 금지). service prefix(`/portfolio/{env}/{service}/*`) 분리.
- Status: 🟡 잠정
- 선택지: 광범위 / 최소 권한
- 비용 영향 상세: 0
- 운영 리스크 상세: 정책 detach + 이전 정책 복구 / 정책 정적 검사로 wildcard 0건 점검
- 후속 spec 영향: 03, 04, 05, 06, 08, 09
- 근거 링크: — (없음)

### OD-SEC-007 Details

- Decision ID: OD-SEC-007
- Selected: `AmazonSSMManagedInstanceCore` attach + SSM Session Manager 진입 중심.
  - 선택값 상세: `AmazonSSMManagedInstanceCore` managed policy attach + SSM Session Manager 진입 중심. SSH 22 inbound 최소화(OD-SEC-004 / OD-NET-009 정합). 잔존 SSH 운영은 후속 spec(03 후속 task / 10)에서 정리.
- Status: 🟡 잠정
- 선택지: SSH / SSM / 혼합
- 비용 영향 상세: 0
- 운영 리스크 상세: SSM Endpoint(VPC Endpoint) 확보 필요. 미사용 시 NAT-free 환경에서 진입 불가
- 후속 spec 영향: 03, 04, 05, 08, 09
- 근거 링크: — (없음)

### Compute / Service Placement (OD-MS-013 ~ OD-MS-038)

### OD-MS-013 Details

- Decision ID: OD-MS-013
- Selected: buy-signal · position-signal 별도 Task Definition 2개.
  - 선택값 상세: 기존 Daily Batch 구조 계승 — buy-signal / position-signal 별도 Task Definition 2개 분리. `portfolio-paper-strategy-decision-buy-signal` 과 `portfolio-paper-strategy-decision-position-signal` family 사용.
    command 는 각각 `python -m port_strategy_decision.daily_buy_signal_run` 과 `python -m port_strategy_decision.daily_position_signal_run`.
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: Fargate per-task
  - 비용 상세: Fargate per-task. 단계 분리로 step 별 cpu / memory / retry 정책 / Step Functions state 매핑 자유도 확보. 통합안 대비 ECS Task 기동 횟수 증가.
- 운영 리스크 상세: 호출 순서를 orchestration 레벨에서 강제
  - 두 entrypoint 호출 순서를 orchestration 레벨에서 강제(현재 port-view DailyBatchService 의 step 6 → step 7 순서). EventBridge Scheduler / Step Functions 연계는 후속 phase 책임.
- 후속 spec 영향: 04, 05
- 근거 링크: — (없음)

### OD-MS-014 Details

- Decision ID: OD-MS-014
- Selected: 1차 ECS smoke image 에서는 vendoring.
  - 선택값 상세: 1차 ECS smoke image 에서는 vendoring(이미지 안에 `port_strategy_common` + `port_strategy_decision` 두 소스 포함). 정식 package / version 관리는 후속(Strategy Common 단계 또는 DevOps 고도화).
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: 0 (vendoring)
  - 비용 상세: 0 (vendoring 자체는 비용 없음). 정식 package 전환 시 CodeArtifact / 빌드 파이프라인 비용 미미.
- 운영 리스크 상세: 소스 동시 갱신 시 버전 불일치 위험
  - 두 소스 동시 갱신 시점 차이로 인한 버전 불일치 위험. 본 결정은 후속 spec(07 / Strategy Common) 에서 wheel + CodeArtifact 또는 git submodule packaging 으로 전환될 가능성 높음.
- 후속 spec 영향: 04, 05, 07, 09
- 근거 링크: — (없음)

### OD-MS-015 Details

- Decision ID: OD-MS-015
- Selected: SSM RunCommand → schtasks → Scheduled Task → Autologon session → wrapper.
  - 선택값 상세: SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1` 흐름. 직접 실행은 SYSTEM Session 0 / SessionId 0 에서 KRX GUI 로그인 부적합으로 채택 거부. EventBridge Scheduler 정기 트리거 연계는 후속 분리.
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: 0 (SSM/Task)
- 운영 리스크 상세: Autologon session 부재 시 KRX 로그인 실패 (R-AUTO-008)
  - RDP 접속 미사용으로도 EC2 Running 상태에서 자동 실행 가능. Administrator interactive session 부재 시 KRX GUI 로그인 실패 가능(R-AUTO-008 정합).
- 후속 spec 영향: 08
- 근거 링크: — (없음)

### OD-MS-016 Details

- Decision ID: OD-MS-016
- Selected: 책임 분리. Execution=READY→REQUESTED · Connector=REQUESTED→SUBMITTED/FAILED.
  - 선택값 상세: 책임 분리. Strategy Execution `--execute` = `READY -> REQUESTED` 상태 전환만 담당, MarketConnector 신규 executor `connector_strategy_order_execute.py` `--execute` = `REQUESTED -> SUBMITTED` / `FAILED` 전환만 담당.
    `connector_order_request_id` 는 Strategy Execution 에서 생성 / 업데이트하지 않는다. SELL position 의 `mark_position_sell_ordered()` 호출은 MarketConnector 측에서 SELL 성공 시점에 실행.
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: 0
- 운영 리스크 상세: View Daily Batch Step 12가 Connector executor 호출
  - Strategy Execution 측 자동 buy / sell entrypoint 의 connector 직접 호출 / `sys.path` 삽입을 제거해 책임 경계 명확화. View Daily Batch 17단계의 12번째 step `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` 가 MarketConnector executor 를 호출하는 구조로 정합.
    실제 `--execute` end-to-end 검증은 평일 / 안전 테스트 데이터 환경에서 후속 분리(R-AUTO-009 / R-AUTO-010 / R-AUTO-011 정합).
- 후속 spec 영향: 03, 04, 05
- 근거 링크: — (없음)

### OD-MS-017 Details

- Decision ID: OD-MS-017
- Selected: 단일 Task Definition + command override.
  - 선택값 상세: 단일 Task Definition + command override.
    - family `portfolio-paper-strategy-execution` / container name `strategy-execution` / revision 1 ACTIVE / awsvpc / Fargate / cpu 512 / memory 1024.
    - 7개 Daily Batch step(`daily_buy_execution_run` / `daily_sell_execution_run` / `daily_auto_buy_execute_run` / `daily_auto_sell_execute_run` / `execution_sync_buy_fill` / `execution_sync_sell_fill` /
      `execution_sync_buy_position`) 은 Step Functions state 또는 View orchestration layer 에서 RunTask `--overrides` 의 `containerOverrides[].command` 로 매핑.
    - 7개 entrypoint 가 같은 image / role / secret / log group / cpu / memory 를 공유하므로 Task Definition 7개 분리는 과도.
    - MarketConnector executor `connector_strategy_order_execute.py --execute` 는 본 Task Definition 에 포함하지 않음(OD-MS-016 정합 / 03 spec EC2 측 별도 책임).
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: Task Def 수 감소, Fargate 비용 동일
  - 비용 상세: Task Definition 수 감소로 운영 복잡도 감소 / Fargate 비용은 실제 RunTask 횟수·실행 시간 기준(7개 entrypoint 분리 안과 동일).
- 운영 리스크 상세: command override 매핑 오류 위험 (R-AUTO-014)
  - command override 매핑 오류 시 잘못된 entrypoint 실행 가능(R-AUTO-014 정합). Step Functions state 정의 / View ECS RunTask 호출부에서 script 명·인자 검증 필요. BUY / SELL / fill sync / position 변경 step 자동 재시도 금지 정책 유지 필요(OD-SAFE-004 / R-AUTO-001 정합). state name ↔ command override allowlist 1:1 표 정식 정리는 후속 분리.
- 후속 spec 영향: 04, 05, 10
- 근거 링크: — (없음)

### OD-MS-018 Details

- Decision ID: OD-MS-018
- Selected: Research 내부 adapter 로 이관. Batch image=research+common, decision 미포함.
  - 선택값 상세: Research 내부 adapter 로 이관. Batch image 포함 대상 = `port_strategy_research` + `port_strategy_common`. `port_strategy_decision` 은 image 에 포함하지 않음.
    - Research 내부 adapter 3개 신규 생성(`research_backtest_market_adapter.py` / `research_backtest_filter_adapter.py` / `research_backtest_sizing_adapter.py`) 으로 기존 `port_strategy_decision.backtest_market` / `backtest_filter` / `backtest_sizing` 직접 import 를 제거.
    - `backtest_engine.py` / `backtest_buy_logic.py` / `block_exception_buy_engine_run.py` import 변경 적용.
    - adapter 는 `port_strategy_common` 호출만 수행하며 자체 판단 로직 미포함(R-DATA-008 mitigation 정합).
    - 장기적으로는 adapter 를 `port_strategy_common` 정식 package 로 이동하는 후보 유지(OD-MS-005 / OD-MS-014 후속 정합). Strategy Research 의 컴퓨트 1순위는 AWS Batch 유지(OD-MS-008 본문 변경 없음).
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: 낮음 (image size 영향)
  - 비용 상세: 직접 비용 영향 낮음 — image 크기·dependency boundary·유지보수 복잡도에 영향. Research image 안에 Decision MS 전체를 vendoring 하지 않음으로써 image size / 빌드 시간 / MS 경계 모두 작은 쪽 유지.
- 운영 리스크 상세: adapter 중복으로 common 계약 변경 시 갱신 누락 (R-DATA-008)
  - adapter 중복으로 `port_strategy_common` 계약 변경 시 Research adapter 갱신 누락 가능(R-DATA-008 정합). `port_strategy_common` 쪽 로직 변경 시 Research smoke / Batch job 검증 필요. 장기적으로는 adapter 를 common package 로 정식 이동하는 후보 유지.
- 후속 spec 영향: 09, 07, 10
- 근거 링크: — (없음)

### OD-MS-019 Details

- Decision ID: OD-MS-019
- Selected: 포팅 대상 = `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종 한정.
  - 선택값 상세: AWS Batch 포팅 대상 = `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종 한정.
    report artifact = S3 bucket `portfolio-paper-migration-yukiever` / prefix `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/` / Job Role `s3:PutObject` Resource 는 `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` 한정.
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: Fargate 사용량 기반
  - 비용 상세: Fargate 사용량 기반. heavy 분류(`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) SubmitJob 0건 유지 정책으로 누적 비용 억제.
- 운영 리스크 상세: View Daily Batch → SubmitJob 매핑 전 운영자 수동 (R-AUTO-015)
  - View Daily Batch step 을 AWS Batch SubmitJob 매핑 전까지 운영자 수동 SubmitJob 의존(R-AUTO-015 정합). heavy 분류 자동 실행 방지 정책 유지 필요.
- 후속 spec 영향: 09, 04, 05, 06, 07, 10
- 근거 링크: — (없음)

### OD-MS-020 Details

- Decision ID: OD-MS-020
- Selected: Interest Crawler = **hybrid 1차 구현 부분 완료**.
  - 선택값 상세: Interest Crawler = **hybrid 1차 구현 부분 완료**. "완료" 표기는 실제 데이터 적재 / 최신성 검증까지 확인된 경우에만 사용. KRX worker 완료와 Interest Crawler 전체 완료 혼동 금지. smoke 성공과 daily raw 최신성 성공 분리.
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: 0 (표현 정정)
  - 비용 상세: 0 — 표현 정정에 따른 추가 비용 없음.
- 운영 리스크 상세: 잔존 표현 정기 grep + raw 최신일 SQL 점검 (R-DATA-009/010)
  - 운영 문서 / 보고 / 운영자 노트 / spec 산출물의 잔존 표현 정기 grep + raw 최신일 SQL 점검(R-DATA-009 / R-DATA-010 정합).
- 후속 spec 영향: 08, 04, 05, 09
- 근거 링크: — (없음)

### OD-MS-021 Details

- Decision ID: OD-MS-021
- Selected: 로컬 View Daily Batch 17단계 순서 그대로. 안전 기준 8종.
  - 선택값 상세: Backend AWS E2E dry-run 은 로컬 View Daily Batch 17단계 순서(`CONNECTOR_BALANCE` → ... → `BALANCE_REFRESH`) 그대로 진행. 안전 기준 8종.
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: 0 (paper 한정)
  - 비용 상세: 0 — dry-run 운영자 직접 SubmitJob / RunTask. paper 환경 한정 / aws-live 작업 0건.
- 운영 리스크 상세: 안전 기준 위반은 R-AUTO-001
  - 안전 기준(BUY · SELL 실행 금지 / `--execute` 금지 / fill·position sync 자동 재시도 금지 / aws-live 금지 / Research → Decision 순서 / Connector Balance 1번 / Balance Refresh 17번 위치 유지) 위반은 R-AUTO-001 정합.
- 후속 spec 영향: 08, 04, 05, 09, 10
- 근거 링크: — (없음)

### OD-MS-022 Details

- Decision ID: OD-MS-022
- Selected: Windows Autologon + Administrator session + Scheduled Task + SSM trigger.
  - 선택값 상세: Windows Autologon + Administrator interactive session + Scheduled Task + SSM trigger. SSM direct / Headless 방식 채택 거부.
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: 0 (Autologon/Task)
  - 비용 상세: 0 (Autologon / Scheduled Task / SSM RunCommand 자체 비용 0).
- 운영 리스크 상세: Autologon 은 paper Windows worker 한정 보안 예외 (R-SEC-009 등)
  - Autologon 은 paper 전용 Windows worker 한정 보안 예외(R-SEC-009 / R-AUTO-016 / R-AUTO-017 신규).
- 후속 spec 영향: 08, 05, 10
- 근거 링크: — (없음)

### OD-MS-023 Details

- Decision ID: OD-MS-023
- Selected: 로컬 Windows PowerShell wrapper 분리 파일 구조(main + config + functions + step 17개).
  - 선택값 상세: 운영자 로컬 Windows PowerShell wrapper 분리 파일 구조(main + config + functions + step 17개). 환경 = `aws-paper` 만 허용. Step 12 는 PAPER_ORDER_GATE + 이중 gate 로 기본 차단 / `-AllowPaperOrderExecute` 시 허용.
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: 0 (로컬 도구)
  - 비용 상세: 0 (운영자 로컬 PC 도구 자체 비용 0). 실제 비용은 wrapper 가 호출하는 ECS RunTask / Batch SubmitJob / SSM command 의 사용량 기반.
- 운영 리스크 상세: `-AllowPaperOrderExecute` 오사용 시 실주문 (R-AUTO-019)
  - `-AllowPaperOrderExecute` 의도하지 않은 사용 시 실주문 위험(R-AUTO-019 신규).
- 후속 spec 영향: 03, 04, 05, 08, 09, 10
- 근거 링크: — (없음)

### OD-MS-024 Details

- Decision ID: OD-MS-024
- Selected: 추가매수 허용 + merge. `merge_open_position_state()` 로 가중평균 병합 + idempotency.
  - 선택값 상세: 추가매수 허용 + merge. `merge_open_position_state()` 로 quantity / entry_price 가중평균 병합 + `buy_info.additional_buys` 누적 + idempotency.
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: 0 (정상 흐름)
  - 비용 상세: 0 — 추가매수는 strategy 결정 기반 정상 흐름 / 비용 모델 변경 없음.
- 운영 리스크 상세: idempotency 미적용 시 execution_order_id 중복 (R-DATA-012)
  - idempotency 미적용 시 같은 execution_order_id 두 번 누적 위험(R-DATA-012 정합).
- 후속 spec 영향: 04, 05, 10
- 근거 링크: — (없음)

### OD-MS-025 Details

- Decision ID: OD-MS-025
- Selected: active 주문 단건 순차 조회 기본 + broad 옵션 격리 + 내부 분기.
  - 선택값 상세: active 주문 단건 순차 조회 기본 + broad 옵션 격리 + `connector_order_check.py` 내부 분기. 기본 실행은 active 목록 조회 후 단건 direct-only 순차 조회 / legacy broad 는 `--broad` 옵션 명시 시에만 진입.
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: 0
  - 비용 상세: 0 — 본 결정 자체는 비용 모델 변경 없음.
- 운영 리스크 상세: broad 호출 감소로 summary fallback 노출 표면 감소 (R-AUTO-018)
  - broad 호출 빈도 자체가 줄어 다건 active 상태에서 summary fallback 노출 표면 감소(R-AUTO-018 mitigation 강화).
- 후속 spec 영향: 03, 04, 05, 10
- 근거 링크: — (없음)

### OD-MS-026 Details

- Decision ID: OD-MS-026
- Selected: Task trigger + Running→Ready wait + Last Result 0 + worker log + KRX raw DB validation 모두 충족.
  - 선택값 상세: Scheduled Task trigger + Running → Ready 복귀 wait + Last Result 0 / 0x0 + latest worker log + KRX raw DB validation 통과까지 모두 충족해야 Step 2 SUCCESS.
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: 0
  - 비용 상세: 0 — 본 결정 자체는 비용 모델 변경 없음.
- 운영 리스크 상세: Scheduled Task trigger 성공만 SUCCESS 처리하던 한계 축소 (R-AUTO-020)
  - Scheduled Task trigger 성공만 SUCCESS 로 처리하던 한계(R-AUTO-007 / R-AUTO-020 정합) 가 KRX raw DB validation guard 도입으로 축소.
- 후속 spec 영향: 08, 04, 05, 10
- 근거 링크: — (없음)

### OD-MS-027 Details

- 선택값 상세: step 실행 시점 wrapper 공통 함수가 `/tmp/inject-env.sh` 재생성. Step 1 / 12 / 13 / 17 진입 직전 호출. JSON SecretString 내부 key 추출 + `APP_*` + `KIS_*` alias export + chmod 700 + secret value 평문 출력 0건.
- 비용 상세: 0 — wrapper 공통 함수 재생성 자체는 비용 없음. Secrets Manager `GetSecretValue` 호출은 기존 흐름과 동일.
- 운영 리스크 상세: MarketConnector EC2 stop / start 후 `/tmp` 휘발로 Step 1 / 12 / 13 / 17 실패 위험(R-AUTO-021 신규 / Mitigated).

### OD-MS-028 Details

- 선택값 상세: Step 12 시작부 내장. `connector_strategy_order_execute.py` 전체 교체. 복구 조건 6종 모두 만족 시에만 적용. `rejection_code` 확장은 OD-MS-033 에서 별도로 관리.
- 비용 상세: 0 — retry-normalizer 자체는 비용 모델 변경 없음.
- 운영 리스크 상세: 복구 조건이 광역으로 확장되면 의도하지 않은 broker 중복 주문 위험(R-AUTO-001 / R-BROKER-004 정합). `broker_order_no IS NULL` + `connector_fill` 없음 조건 유지로 mitigation.

### OD-MS-029 Details

- 선택값 상세: false path 사전 검증 후 true path 승인 실행. State machine `portfolio-paper-daily-step1-17-approval` 사용 / `allowPaperOrderExecute` 파라미터로 분기. 2026-06-23 실 검증 통과.
- 비용 상세: Step Functions transitions 비용 + 동일 ECS RunTask · AWS Batch · SSM RunCommand 사용량 — 비용 모델 변경 미미.
- 운영 리스크 상세: true path 진입 시 의도하지 않은 `allowPaperOrderExecute=true` 입력은 broker 실 주문 제출로 이어질 수 있음(R-AUTO-019 / R-AUTO-023 정합).

### OD-MS-030 Details

- 선택값 상세: Lambda `portfolio-event-notifier` (Runtime Python 3.12 / IAM Role `portfolio-event-notifier-lambda-role`) 기반 AWS 공통 Slack notifier 채택. Step Functions · EventBridge · EC2 SSM · Batch · Lambda 어디서든 호출 가능한 단일 진입점.
- 비용 상세: 매우 낮음 — Lambda 1M requests + 400,000 GB-sec 무료 한도 안. Slack webhook 호출 자체 비용 없음.
- 운영 리스크 상세: webhook URL 환경변수 장기 보관 위험(R-AUTO-024 신규 / Accepted — 운영 안정화 후 Secrets Manager 또는 SSM SecureString 이전) + Slack 발송 실패 위험(R-AUTO-023 신규).
- 운영 실증 상세 [2026-07-20]: 공통 notifier Lambda `portfolio-event-notifier` 가 실패 알림뿐 아니라 수동 복구 완료 알림에도 보조 계층으로 사용됐다. 2026-07-20 09:01 자동 실행이 Step 13 실패로 성공 Slack 을 자동 발송하지 못한 뒤, 운영자가 Step 13~17 수동 복구를 완료하고 Lambda 를 `DAILY_EXECUTION_SUCCESS`(runDate=2026-07-20 · stage=AFTER_STEP_17) 로 수동 호출해 StatusCode 200 · ok=true · 채널 실제 수신을 확인했다(자동 실행 성공으로 표현하지 않음 · 09:01 자동 이력 FAILED 유지). 또한 결과 실패 시 실패 Slack 이 우회되던 State Machine 경로를 실패 컨텍스트 Pass State 경유로 보완해 Choice 결과 실패도 notifier 실패 경로로 연결했다(OD-MS-009 [2026-07-20] 정합). Lambda 는 여전히 notifier 보조 계층이며 주 compute 아님 · Status 기존 값 그대로 유지 · 신규 Decision ID 없음.
- 운영 실증 상세 [2026-07-20 성공 Slack 체결 내역 표시]: `portfolio-event-notifier` 는 formatter · 전송 역할만 유지하고, 성공 Slack 에 표시할 당일 실제 체결(`connector.connector_fill` · `reference.stock_master`) DB 조회는 신규 Builder Lambda(`portfolio-daily-execution-slack-summary-builder`) 로 분리했다. Notifier 의 `DAILY_EXECUTION_SUCCESS` formatter 는 Builder payload(`buyFills` · `sellFills`) 를 받아 매수·매도 체결 목록을 표시(없으면 `- 없음`)한다. 두 Lambda 모두 주 compute 가 아닌 Slack 보조 계층이라는 기존 결정을 유지한다 · Status 기존 값 그대로 유지 · 신규 Decision ID 없음 · 상세 근거는 R-AUTO-023 [2026-07-20 보강] 참조.

### OD-MS-031 Details

- 선택값 상세: 3종(`APPROVAL_REQUIRED` + `DAILY_EXECUTION_SUCCESS` + `DAILY_EXECUTION_FAILED`) 한정. 2026-06-23 검증 통과. 장 전 / 장 후 / 장중 손절 알림은 후속 분리(OD-MS-035 / OD-MS-036 / OD-MS-038 정합).
- 비용 상세: Step Functions transitions 비용 변화 미미 / Lambda 호출 비용 무료 한도 안 / 매 운영 회차 Slack 발송 3건 이내.
- 운영 리스크 상세: 본 결정의 핵심은 메시지 누락이 아니라 Step Functions Catch 경로에서 실패 알림이 끊기지 않는 것(R-AUTO-023 정합).
- 운영 실증 상세 [2026-07-15]: `APPROVAL_REQUIRED` Slack Builder(`portfolio-approval-slack-summary-builder`) 조회 구조를 단일 `daily_run_id` 기준으로 정합. 서로 다른 최신 Execution Plan(`latestPlanId=133` · `latestPlanDate=2026-07-09`) 과 최신 Daily Run(`latestDailyRunId=73`) 을 각각 독립 조회하던 방식을 제거하고, 하나의 `daily_run_id` 를 먼저 확정 후 상태 · 기준일 · 신호 수 · 후보 수를 동일 Daily Run 기준으로 조회. `source_daily_run_id` 로 연결된 Execution Order 만 조회 · 해당 Order 가 참조하는 Execution Plan 만 사용 · 연결된 Plan 부재 시 `latestPlanId=null` 로 처리해 임의 과거 Plan 사용 안 함. Builder 와 Notifier 책임 분리 유지 — Builder 가 후보별 점수 필드 구조화(`final_score` · `flow_score` · `info_score` · `tape_score` · `short_score`) 후 Notifier 로 전달 · Notifier 가 소수점 셋째 자리 · 한글 라벨(종합 · 수급 · 정보 · 추세 · 공매도) 로 표시 · 원본 `buy_info` dict Slack 노출 제거. 두 Lambda 재배포 결과 `Active` · `LastUpdateStatus=Successful` · Runtime · Handler 변경 없음 · 이전 버전 롤백 ZIP 확보. Builder DB 검증 상태 `AGGRESSIVE` · 기준일 2026-07-10 · 신호 4건 · 후보 4건 · `latestPlanId=null` · 후보 종목 4건 정상(DL · 대주전자재료 · 삼성SDI · 한국피아이엠). Builder → Notifier → Slack 메시지 표시 E2E 완료 (실제 자동 주문 체결 E2E 는 아님). Status 기존 값 그대로 유지 · 신규 Decision ID 없음 · Decision Summary count 변경 없음. 다음 자동 Scheduler 실전 수신 관찰은 후속 유지.
- 운영 실증 상세 [2026-07-20]: `DAILY_EXECUTION_SUCCESS` 메시지가 실행일 · 최종 상태 외에 당일 실제 매수·매도 체결 목록([매수 체결] · [매도 체결]) 을 표시하도록 개선됐다. 체결이 없으면 `- 없음`, 있으면 `- 종목명 / 수량주` 로 표시한다. 2026-07-20 데이터 수동 smoke 에서 매수 0건 · 매도 2종목(엔씨소프트 036570 13주 · 코오롱생명과학 102940 78주) 체결 표시를 실제 Slack 수신으로 확인했다. Slack 이벤트 종류 3종(`APPROVAL_REQUIRED` + `DAILY_EXECUTION_SUCCESS` + `DAILY_EXECUTION_FAILED`) 한정 결정 자체는 변경 없음 · Status 기존 값 그대로 유지 · 신규 Decision ID 없음 · Decision Summary count 변경 없음 · 상세 근거는 R-AUTO-023 [2026-07-20 보강] 참조.

### OD-MS-032 Details

- 선택값 상세: 2개 Scheduler 분리 + Dispatcher Lambda 호출 + 단계적 활성화. 08:00 KST `portfolio-paper-daily-step1-11-approval-0800-kst`(ENABLED / `allowPaperOrderExecute=false`) + 09:01 KST `portfolio-paper-daily-step12-17-order-0901-kst`(초기 DISABLED / 후속 ENABLE).
- 비용 상세: EventBridge Scheduler 14M / Lambda 1M requests 무료 한도 안 / Step Functions Standard transitions 비용은 OD-MS-009 정합.
- 운영 리스크 상세: Scheduler / Dispatcher Lambda / Step Functions 연결 실패 위험(R-AUTO-025 신규) — IAM simulate + Lambda dryRun + Scheduler get-schedule 상태 확인으로 mitigation.
- 운영 실증 상세 [2026-07-15]: 장전 · 장후 Daily Brief Scheduler Target 을 Daily Brief State Machine 직접 호출 대신 본 결정의 Dispatcher Lambda 호출로 전환. Dispatcher 에 `MORNING_BRIEF` · `EVENING_BRIEF` scheduleType 지원과 Daily Brief State Machine ARN 환경변수 · Resource 한정 `states:StartExecution` IAM 추가. Holiday Guard 책임을 Dispatcher 로 통합해 08:00 Step 1~11 · 09:01 Step 12~17 · 07:50 장전 · 15:50 장후 4종 모두 동일 Dispatcher · 동일 Holiday Guard 통과 구조로 정합. EC2 stop Scheduler 15:50 은 휴일 여부와 무관하게 실행하는 기존 구조 유지. Status 기존 값 그대로 유지 · 신규 Decision ID 없음 · Decision Summary count 변경 없음 · 상세 근거는 R-AUTO-035 [2026-07-15 보강] 참조.
- 운영 실증 상세 [2026-07-16]: 본 결정의 2개 Scheduler + Dispatcher Lambda + Step Functions 사슬 위에서 Paper Daily 자동화 라인업의 실 매수·체결·Fill·Position E2E 가 처음 완료됐다. Daily Run 76 이 `run_date=2026-07-16` · `data_date=2026-07-15` · `market_signal=AGGRESSIVE` 로 정상 계산되어 BUY 후보 2건(엔씨소프트 036570 · 코오롱생명과학 102940) 이 생성됐고, 운영자가 Step 12~17 을 수동 재실행해 13주 · 78주 시장가 매수 주문을 접수했으며 두 주문 모두 KIS 모의투자에서 전량 체결(엔씨소프트 최종 보유수량 13주 · 평균 체결가 223,500 원 · 코오롱생명과학 최종 보유수량 78주 · 평균 체결가 약 38,839.7436 원) 됐다. 초기 Step 13 조회가 주문 제출 후 약 10초 시점에 이뤄져 엔씨소프트 9주 부분체결 · 코오롱생명과학 접수 상태로 고착되고 실제 시장가 주문은 이후 계속 체결됐으나 내부 상태가 최초 조회 결과에 머무는 문제를 확인했으며(전체 Step Functions 는 ExitCode 0 기준 SUCCESS 처리 · Step Functions SUCCESS 가 Fill · Position 정합 완료를 보장하지 않는 축 재확인), 운영자가 Step 13(`connector_order_check.py`) → Step 15(`execution_sync_buy_fill.py`) → Step 16(`execution_sync_buy_position.py`) 재실행 순서로 두 주문 `FILLED` 반영 · Execution Order 2건 `SUBMITTED` → `FILLED` · `synced_count=2` · Position ID 13 · 14 `OPEN` 생성으로 DB 정합을 복구했다. 재발 방지 조치로 State Machine `portfolio-paper-daily-step12-17-approval` Wait State `Step12_WaitBeforeCheck` `Seconds` 10 → 60 변경 · Next State `Step12_GetCommandInvocation` 유지 · State Machine 업데이트 후 재조회 검증 완료(실제 ARN · revision ID · broker 주문번호 원문 문서 미기록). 시장가 주문 접수 성공만으로 체결 완료를 판단하지 않고 Order Request · Fill · Execution Order · Position 정합을 함께 확인해야 함이 명확해졌다. 대기시간 조정만으로 완전 해결됐다고 판단하지 않으며 `ACCEPTED` / `PARTIAL_FILLED` polling · Step 13 · 15 · 16 처리 건수 기반 실패 전파 강화 · 불일치 시 Workflow FAIL · OPS Mirror 세부 Step 확장 · 10분 잔고 스냅샷 연계는 후속 유지. 본 변경은 aws-paper 한정 · aws-live 자동 BUY / SELL 정책 변경 없음(OD-SAFE-002 / OD-SAFE-003 유지). Status 기존 값 그대로 유지 · 신규 Decision ID 없음 · Decision Summary count 변경 없음 · 상세 근거는 R-AUTO-037 · R-AUTO-038 · R-BROKER-004 [2026-07-16 보강] 참조.
- 운영 실증 상세 [2026-07-20]: 본 결정의 2개 Scheduler + Dispatcher Lambda + Step Functions 사슬 위에서 09:01 자동 실행의 실제 실패와 수동 복구가 함께 관찰됐다. 09:01 자동 실행된 State Machine `portfolio-paper-daily-step12-17-approval` 이 Step 13(주문·체결 조회)에서 KIS `EGW00201`(초당 거래건수 초과) 로 실패했고(Step 12 주문 제출 자체는 정상 · 매도 2건 broker 접수 · 첫 주문 전량 체결), Step 13 실패로 Step 14~17 과 성공 Slack 이 자동 실행되지 않았다. 운영자가 Step 12 를 중복 주문 방지 위해 재실행하지 않고 Step 13 만 수동 실행해 두 주문 `FILLED` 복구 후 Step 14~17 을 순차 수동 완주했으며, 성공 Slack 은 수동 복구 완료 후 Lambda 수동 호출로 수신했다(09:01 자동 실행 이력은 실제 장애 보존을 위해 FAILED 유지 · 수동 복구를 자동 성공으로 덮어쓰지 않음). 추가로 잔고 명령 runDate 하드코딩(`--run-date 2026-06-22`) 을 제거해 `$.runDate` 를 하위 SSM 명령까지 동적 전달하고, 결과 실패 시 실패 Slack 을 우회하던 Choice Default 경로를 실패 컨텍스트 Pass State 경유로 보완했다(OD-MS-009 · OD-MS-030 [2026-07-20] 정합). aws-paper 한정 · aws-live 정책 변경 없음 · Status 기존 값 그대로 유지 · 신규 Decision ID 없음 · Decision Summary count 변경 없음 · 상세 근거는 R-AUTO-037 · R-AUTO-038 [2026-07-20 보강] 참조. 같은 일자 성공 경로에 Builder → Notifier(`BuildDailyExecutionSuccessSlackSummary`) 가 삽입됐으나 Scheduler · Dispatcher 구조 자체는 변경 없음이며, 새 성공 경로가 자동 진입해 성공 Slack 에 당일 체결 내역을 표시하는지는 다음 정상 09:01 자동 회차에서 관찰이 필요하다(강제 주문·오류 유발 없이 · OD-MS-023 · R-AUTO-023 정합).

### OD-MS-033 Details

- 선택값 상세: `40580000` + `EGW00201` 재시도 + sleep · backoff + 09:01 자동 ENABLE 보류. `broker_order_no IS NULL` + `connector_fill` 없음 조건 유지(R-AUTO-001 / R-BROKER-004 정합).
- 비용 상세: Step 12 entrypoint 실행 시간 영향 미미 / 운영 회차 누적 비용 모델 변경 없음.
- 운영 리스크 상세: 09:01 schedule 자동 ENABLE 보류 정책 위반 시 즉시 `disable-schedule` 호출 + 운영자 직접 SQL 점검(R-AUTO-025 / R-AUTO-026 정합).

### OD-MS-034 Details

- 선택값 상세: EventBridge Scheduler + Lambda 기반 EC2 start/stop. 07:50 KST MarketConnector + Crawler start / Step 1~11 성공 시 Crawler stop / 15:50 KST MarketConnector stop. Scheduler 이름 `portfolio-paper-ec2-start-0750-kst` · `portfolio-paper-marketconnector-stop-1550-kst`.
- 비용 상세: EventBridge Scheduler 14M / Lambda 1M requests 무료 한도 안 + EC2 lifecycle 비용 절감 효과(영업 시간 외 EC2 stop).
- 운영 리스크 상세: Scheduler / EC2 lifecycle Lambda / Step Functions 연결 실패 위험(R-AUTO-028 신규).

### OD-MS-035 Details

- 선택값 상세: 3단계 분리 + 신규 파일 + Submit 초기 수동·승인 후. 1단계 = MarketConnector 10분 Snapshot Refresh / 2단계 = StrategyExecution Intraday Evaluate + `strategy_intraday_position_check` insert + `INTRADAY_STOP_SELL` READY 생성 / 3단계 = Submit & Refresh (OD-MS-036).
- 비용 상세: EventBridge Scheduler + Lambda dispatcher 각 무료 한도 안 / Step Functions transitions Standard 매우 낮음 / aws-paper 한정 범위 안에서 누적 비용 증가 매우 낮음.
- 운영 리스크 상세: Stale snapshot(R-DATA-014) / Duplicate order(R-AUTO-029) / sellable_qty(R-DATA-015) / current_price(R-DATA-016) 신규 리스크 4종 모두 Mitigated.

### OD-MS-036 Details

- 선택값 상세: 별도 Step Functions state machine `portfolio-paper-intraday-stop-sell-approval` + 수동 승인 운영 + `INTRADAY_STOP_SELL` 전용 `signal_type` 필터 + `connector_strategy_order_execute.py --execute --intraday-stop-only` 실행.
- 비용 상세: Step Functions Standard transitions 비용 매우 낮음 / SSM RunCommand + ECS RunTask.sync 사용량 기준 / 누적 비용 증가 매우 낮음.
- 운영 리스크 상세: approval gate 오설정 위험(R-AUTO-031 신규) / Daily SELL vs Intraday Stop SELL 흐름 혼선 위험(R-AUTO-032 신규) / 보유 부재 상태 실주문 테스트 위험(R-BROKER-005 신규) 모두 Mitigated.

### OD-MS-037 Details

- 선택값 상세: read-only 1차 scope 확정 + ECS / Fargate 진입 전 batch 2차 검증 선행. Local Spring Boot 가 SSM Port Forwarding `127.0.0.1:15433` 경유로 AWS Paper RDS 조회 / `view_app` DB user 유지 / Spring Boot feature flag 4종 기본 false + 서버단 POST 우회 차단.
- 비용 상세: SSM Port Forwarding 무료 / RDS Free Tier(`db.t4g.micro`) 안 / 누적 비용 증가 0건.
- 운영 리스크 상세: View Local 실행성 호출 위험(R-AUTO-033 신규) / SSM tunnel 종료 시 DB 접속 단절(R-AUTO-012 정합).

### OD-MS-038 Details

- 선택값 상세: 별도 mini Step Functions `portfolio-daily-brief-slack-notification` + Builder Lambda `portfolio-daily-brief-slack-summary-builder` + Notifier Lambda `portfolio-event-notifier` + Scheduler 2개(`portfolio-daily-brief-morning-slack-0750-kst` ·
  `portfolio-daily-brief-evening-slack-1550-kst`) + 알림 전용 IAM Role 2종.
- 비용 상세: mini Step Functions transitions + Lambda 호출 + Scheduler 2개 invocations 모두 매우 작음(평일 2회 = 월 약 44 invocation).
- 운영 리스크 상세: Daily Brief 알림 누락 / 중복 위험(R-AUTO-035 신규) / Slack webhook URL Lambda 환경변수 노출 위험(R-AUTO-024 정합) / DB password Lambda 환경변수 주입 위험은 Secrets Manager `valueFrom` 방식으로 완화.
- 운영 실증 상세 [2026-07-15]: Builder Lambda `portfolio-daily-brief-slack-summary-builder` 는 메시지 생성 · DB 조회 책임만 유지하도록 내부 Holiday Guard 를 환경변수 제어 방식으로 비활성화. 정상 응답에 `skipped=false` · `skipReason=null` 을 추가하여 mini state machine `CheckHolidaySkip` Choice 정합 확보. `pg8000` 및 관련 의존성 패키징 재배포. Holiday Guard 책임 자체는 Daily Scheduler Dispatcher(OD-MS-032) 로 통합. 2026-07-15 15:50 자동 장후 초기 `CheckHolidaySkip` 오류 실패 후 16:02 장후 실전 smoke 에서 Step Functions `SUCCEEDED` · Notifier `statusCode=200` · Slack 실제 수신 확인. 07:50 자동 장전 실 수신은 다음 평일 후속 유지. Status 기존 값 그대로 유지 · 신규 Decision ID 없음 · Decision Summary count 변경 없음 · 상세 근거는 R-AUTO-035 [2026-07-15 보강] 참조.

### OD-DB-012 Details <a id="od-db-012-details"></a>

| 항목 | 값 |
|---|---|
| Decision ID | OD-DB-012 |
| Selected | `ops_recorder_app` 전용 최소 권한 role 신설 (Step Functions 실행 이력 OPS mirror writer 한정) |
| Status | 🟢 확정 |
| 결정 일자 | 2026-07-03 |
| 선택지 | (a) `view_app` 재사용 · (b) `execution_app` 권한 확대 · (c) 전용 `ops_recorder_app` 신설 |
| 최종 선택 근거 | (c) 전용 role. Recorder 는 이력 writer 역할만 수행. View 는 reader / controller 역할 유지. 업무 테이블 write 권한 확대 없음. |
| 부여 권한 | `ops` schema USAGE · `ops.strategy_daily_batch_run` SELECT / INSERT / UPDATE · `ops.strategy_daily_batch_step_log` SELECT / INSERT / UPDATE · 관련 sequence USAGE / SELECT |
| 미부여 권한 | DELETE · TRUNCATE · 다른 schema 접근 · 업무 테이블(`strategy_execution_order` · `strategy_position_state` 등) write |
| 제약 변경 | `chk_strategy_daily_batch_run_type` 에 `AWS_STEPFUNCTIONS` 값 추가. 기존 `MANUAL` · `SCHEDULED` · `RETRY` · `MANUAL_PARTIAL` 값 유지 |
| 비용 영향 | 0 (신규 role · 신규 secret 1건 발생 가능 · Secrets Manager 비용 항목만 소폭) |
| 운영 리스크 | R-AUTO-038 정합 — Recorder Lambda 실행 실패 시 mirror 누락. 실패는 Step Functions 흐름 자체를 중단시키지 않도록 fail-open 정책(업무 테이블 · Slack 은 정상 진행). |
| 후속 spec 영향 | 02, 04, 05, 06 |
| 근거 링크 | 2026-07-03 Change Log Details 참조 |

### OD-MS-039 Details <a id="od-ms-039-details"></a>

| 항목 | 값 |
|---|---|
| Decision ID | OD-MS-039 |
| Selected | Step Functions 실행 이력 OPS mirror 를 Recorder Lambda + State Machine 연동으로 구현 |
| Status | 🟢 확정 |
| 결정 일자 | 2026-07-03 |
| 배경 | 기존 Step Functions 실행은 업무 테이블 · Slack 은 갱신했지만 `ops.strategy_daily_batch_run` 에는 기록되지 않아 View Daily Batch 화면이 AWS Step Functions 실행 이력을 직접 보여주지 못했다(2026-07-02 View Daily Batch 이력 불일치 이슈 정합) |
| Recorder Lambda | `portfolio-daily-batch-ops-recorder` (Python 3.12 · ap-northeast-2 · VPC Lambda · Secrets Manager `ops_recorder_app` secret 사용) |
| 지원 action | `RECORD_START` · `RECORD_STEP` · `RECORD_SUCCESS` · `RECORD_FAILURE` |
| 대상 State Machine | `portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval` |
| 대상 테이블 | `ops.strategy_daily_batch_run` · `ops.strategy_daily_batch_step_log` |
| 1차 범위 | 전체 세부 step mirror 가 아니라 **run-level + 대표 workflow step mirror 중심** (후속 phase 에서 세부 step 확장) |
| Step Functions 실행 role 변경 | `lambda:InvokeFunction` 권한을 Recorder Lambda ARN 한정으로 부여. Resource wildcard 0건. Action wildcard 0건. |
| 검증 요약 | RECORD_START 응답 `ok true` 확인 · RECORD_STEP 응답 `ok true` + `stepLogId` 생성 확인 · RECORD_SUCCESS 응답 `ok true` 확인 · commit smoke `batchRunId=53` 확인 · Step 12 approval-blocked smoke 후 `ops.strategy_daily_batch_run` + step log 기록 확인 |
| 비용 영향 | Lambda 호출 · Secrets Manager `GetSecretValue` · DB INSERT/UPDATE 비용 모두 매우 작음. Step Functions transitions 비용은 무변화(기존 flow 안 mirror step 추가). |
| 운영 리스크 | R-AUTO-038 정합 — Step Functions 이력 mirror 실패 시 View Daily Batch 화면이 다시 이력 불일치를 노출할 위험. Lambda fail-open + CloudWatch Logs 관찰 + 후속 phase 에서 CloudWatch Alarm 도입 후보. |
| 보안 판단 | Recorder 는 writer 역할만 수행. View 는 reader / controller 역할 유지. 업무 테이블 write 권한 확대 없음. `execution_app` / `view_app` 권한 확대 0건. |
| 후속 spec 영향 | 04, 05, 10 |
| 후속 작업 | 전체 세부 step mirror 확장 · Recorder Lambda 실패 CloudWatch Alarm 도입 검토 · `/daily-batch` 화면 표시 계층 redaction 후속 (`requestPayload` / `resultPayload` 마스킹 · `accountNo` 마스킹 · `executionArn` / `stateMachineArn` redaction · timestamp UTC → KST 표시 · `APPROVAL_BLOCKED` / SKIPPED 문구 운영자 친화적 개선) |
| 2026-07-08 UI consumption 확인 | 완료 (운영자 직접 수행). `view_app` 기준 `ops` schema USAGE · `ops.strategy_daily_batch_run` · `ops.strategy_daily_batch_step_log` SELECT 확인 · `AWS_STEPFUNCTIONS` run 이력 적재 확인(#61 Step 12~17 SUCCESS · #60 Step 1~11 `APPROVAL_BLOCKED` / SKIPPED) · `/daily-batch` 화면 aws-stepfunctions 실행 모드 표시 · 선택 run 상세 + step log 렌더링 · AWS Step 1~11 시작 · AWS Step 12~17 승인 실행 버튼 표시 · Local File 실행 OFF · aws-stepfunctions 실행 ON 표시 확인. 1차 범위(전체 세부 step mirror 아님 · run-level + 대표 workflow step) 결정 유지. |
| 근거 링크 (2026-07-08) | `.kiro/WORKLOG.md` 2026-07-08 view-daily-batch-ops-mirror-ui-consumption-confirmed 섹션 · `_common/risk-register.md` R-AUTO-038 Mitigation history 2026-07-08 보강 · `_common/followups-overview.md` Done recently 2026-07-08 row |

### OD-MS-040 Details <a id="od-ms-040-details"></a>

| 항목 | 값 |
|---|---|
| Decision ID | OD-MS-040 |
| Selected | Daily market-date 계산을 포함하는 ECS batch container 는 UTC 기본값에 의존하지 않고 `TZ=Asia/Seoul` 을 명시한다 |
| Status | 🟢 확정 |
| 결정 일자 | 2026-07-08 |
| 배경 | 2026-07-08 08:00 KST Daily Step1~11 자동 실행이 SUCCEEDED 였음에도 `interest_price_raw` · `interest_investorflow_raw` · `interest_program_raw` · `interest_shortsell_raw` · `pre_total_market_daily_feature` · `pre_total_stock_daily_feature` `MAX(trade_date)=2026-07-06` 정체 · `decision.strategy_daily_run.data_date=2026-07-06` · `execution.strategy_execution_plan` NO_CANDIDATE 로 관찰됨 |
| 직접 원인 | ECS Fargate 컨테이너 기본 timezone 이 UTC 인 상태에서 crawler `interest_price.py` · `interest_investorflow.py` 등이 timezone 없는 `datetime.now().date() - timedelta(days=1)` 을 사용하여 최신 영업일을 계산 → 08:00 KST 실행 시 UTC 는 전일 23시대이므로 `datetime.now().date()=2026-07-07` → `-1 day` 적용 후 target date 2026-07-06 으로 밀림 |
| 배제 원인 | `interest_get_holidays.is_holiday(2026-07-07,"KR")==False` 확인 · 2026-07-07 은 KRX 정상 개장일이며 휴장일 오판 원인 아님 |
| 단기 조치 | (1) 신규 TaskDefinition `portfolio-paper-interest-crawler:8` · `portfolio-paper-interest-preprocessor:2` 등록 · env 에 `TZ=Asia/Seoul` 추가 · 이미지 · 명령 · IAM Role · 리소스 스펙 변경 없음 · (2) State Machine `portfolio-paper-daily-step1-17-approval` 의 Step2A_RunInterestCrawlerNongui task revision `:7`→`:8` · Step3_RunPreprocessor task revision `:1`→`:2` 갱신 · revisionId 원문 미기록(`[REDACTED_REVISION_ID]`) · (3) one-off TZ smoke 성공 — crawler:8 · preprocessor:2 각 `TZ_ENV=Asia/Seoul` · `time.tzname=('KST','KST')` · `naive_yesterday=2026-07-07` |
| 근본 개선 (후속) | 코드에서 `datetime.now()` 직접 사용을 timezone-aware helper 로 대체하여 컨테이너 default TZ 의존을 제거한다. 대상 파일 후보: `interest_price.py` · `interest_investorflow.py` 및 유사 crawler / preprocessor entry point. 08 spec 후속 phase 로 유지. |
| 오늘 조치 | Step1~11 수동 재실행은 하지 않는다 |
| 검증 예정 | 2026-07-09 08:00 KST Daily Step1~11 자동 실행 · 09:01 Step 12~17 자동 실행에서 (a) Step2A crawler log 의 `interest_price` · `interest_investorflow` 가 2026-07-07 을 수집하는지 · (b) preprocessor latest date 가 2026-07-07 로 올라오는지 · (c) `decision.strategy_daily_run.data_date=2026-07-07` · (d) Step 12~17 자동 실행이 stale data 영향 없이 정상 흐름인지 |
| 비용 영향 | TaskDefinition env 추가는 월 비용 영향 없음. Step Functions transitions · Fargate cpu / memory · CloudWatch Logs 비용은 무변화 |
| 운영 리스크 | R-DATA-010 정합 — partial mitigation applied · next auto-run verification pending. 다음 자동 실행에서 최신성 회복이 관찰되지 않으면 원인 재조사 필요. R-DATA-017(KRX GUI worker 계열 · Step2B 성공 기준) 과는 원인이 구분됨. |
| 보안 판단 | 이미지 · 명령 · IAM Role · Secrets Manager 참조 · Task Role · Execution Role 변경 0건. env `TZ=Asia/Seoul` 만 추가. secret 원문 노출 0건. |
| 후속 spec 영향 | 04, 08 |
| 후속 작업 | 2026-07-09 자동 실행 검증 후 성공 시 Status 유지 · 실패 시 원인 재조사 · 근본 개선(timezone-aware helper 도입) 은 08 spec 후속 phase |
| 근거 링크 | `.kiro/WORKLOG.md` 2026-07-08 daily-step1-11-ecs-timezone-tz-patch 섹션 · `_common/risk-register.md` R-DATA-010 Mitigation history 2026-07-08 보강 · [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |
| 2026-07-09 메모 | 본 결정의 범위는 ECS Fargate batch container 이지만, KRX GUI 경로의 CRAWLER Windows EC2 도 동일 취지로 KST timezone 으로 운영한다. 2026-07-09 회차에서 CRAWLER Windows EC2 timezone 이 UTC 로 설정되어 있어 08:00 KST 실행 시 서버 로컬 날짜가 UTC 기준 전일 23시대로 계산되어 `datetime.today() - 1` 계열 로직이 target date 를 2026-07-07 로 산출하는 문제가 확인됨. 운영자 직접 timezone 을 Korea Standard Time 으로 변경 후 KRX 수동 재수집으로 `interest_program_raw` · `interest_shortsell_raw` `MAX(trade_date)=2026-07-08` 회복. DB session timezone 자체는 UTC 유지 권고이며 운영 표시는 `at time zone 'Asia/Seoul'` 로 KST 변환. R-DATA-010 `Mitigated` 승격 근거. 신규 Decision ID 는 부여하지 않고 본 OD-MS-040 범위 안 확장으로 관리한다. |
| 2026-07-09 근거 링크 | `.kiro/WORKLOG.md` 2026-07-09 krx-crawler-ec2-timezone-fix-and-recollect 섹션 · `_common/risk-register.md` R-DATA-010 Mitigation history 2026-07-09 보강 · `_common/ms-aws-service-decision-matrix.md` 4.3 port-interest-crawler 절 KST 메모 |
| 2026-07-15 메모 | 본 결정의 범위 확장을 Daily BUY / SELL 실행 ECS 컨테이너까지 명시적으로 적용. 2026-07-13 (월) 매수 후보 4건 미실행 사건 정합 — 실행 컨테이너가 `date.today()` · `datetime.today()` naive 함수로 08:00 KST (= 2026-07-12 23:00 UTC) 를 2026-07-12 (일) 로 판단 · Step8 이 `WEEKEND / NO_TARGET` 으로 종료 · Execution Plan · Execution Order 미생성 · ExitCode 0 으로 Step Functions 전체 SUCCESS 로 표시되어 자동 감지가 어려웠음. Daily 전략 계산과 2026-07-10 기준 데이터는 정상(`daily_run_id=73` · `run_date=2026-07-12` · `data_date=2026-07-10` · `market_signal=AGGRESSIVE` · BUY 신호 4건 · 후보 4건). 운영자 직접 원칙을 DB timestamp 저장 UTC 유지 · 업무 날짜 판단 Asia/Seoul 통일로 확정하고 Daily BUY / SELL 실행 관련 ECS Task Definition 에 `TZ=Asia/Seoul` 추가 · Market EC2 서버 timezone Korea Standard Time 변경 · 실행 스크립트 KST 업무 날짜 산출 보완 · 신규 Task Definition revision 을 실제 Step Functions 실행 경로에 연결. UTC 날짜 오판 직접 원인 해결 완료 · 신규 revision 연결 완료. 다음 자동 실행 재발 여부 · `NO_TARGET` 성공 상태 구분 강화 · ExitCode 0 및 Step Functions SUCCESS 로 묻히지 않도록 실패 전파 강화 · 주문 검증 체인 보강은 별도 후속. Status 기존 값 그대로 유지 · 신규 Decision ID 없음 · Decision Summary count 변경 없음 · R-DATA-010 · R-DATA-017 Mitigation history 별도 보강. |
| 2026-07-15 근거 링크 | `.kiro/WORKLOG.md` 2026-07-15 오후 Daily BUY KST 섹션 · [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-07-15 (오후) 섹션 · `_common/risk-register.md` R-DATA-010 · R-DATA-017 Mitigation history 2026-07-15 보강 · `_common/followups-overview.md` Now |

---


### OD-ENV-001 Details

- Decision ID: OD-ENV-001
- Selected: 미구축
- Status: 🟢 확정
- 선택지: 구축 / 미구축
- 비용 영향 상세: 큰 절감(별도 RDS/ECS/NAT/ALB 0)
- 운영 리스크 상세: 통합 검증은 aws-paper에서 수행
- 후속 spec 영향: 02, 03, 04, 05, 08, 09 모두 dev 항목 제거
- 근거 링크: — (없음)

### OD-ENV-002 Details

- Decision ID: OD-ENV-002
- Selected: 기존 로컬 PostgreSQL 환경 유지
- Status: 🟢 확정
- 선택지: 기존 로컬 / 신규
- 비용 영향 상세: 0
- 운영 리스크 상세: 로컬과 aws-paper 사이 데이터 차이 관리 필요
- 후속 spec 영향: 02 cutover, 10 cutover-runbook
- 근거 링크: — (없음)

### OD-ENV-003 Details

- Decision ID: OD-ENV-003
- Selected: aws-paper
- Status: 🟢 확정
- 선택지: aws-paper / aws-live
- 비용 영향 상세: 중
- 운영 리스크 상세: 자동 주문은 paper 안에서 검증
- 후속 spec 영향: 03, 04, 08 우선 적용
- 근거 링크: — (없음)

### OD-ENV-004 Details

- Decision ID: OD-ENV-004
- Selected: paper 검증 후 후속 구축
- Status: 🟢 확정
- 선택지: 즉시 / paper 검증 후
- 비용 영향 상세: live 비용 보류
- 운영 리스크 상세: live 자동매매 단계적 도입
- 후속 spec 영향: 10 cutover-and-validation-runbook
- 근거 링크: — (없음)

### OD-ENV-005 Details

- Decision ID: OD-ENV-005
- Selected: 단일 VPC 유지
- Status: 🟢 확정
- 선택지: 단일 VPC / prod-nonprod 분리
- 비용 영향 상세: 절감
- 운영 리스크 상세: 환경 사이 SG / Subnet 태그로 격리
- 후속 spec 영향: 02
- 근거 링크: — (없음)

### OD-NET-001 Details

- Decision ID: OD-NET-001
- Selected: 미사용
- Status: 🟢 확정
- 선택지: NAT GW 1개 / NAT Instance / 미사용
- 비용 영향 상세: ~$43+ /월 절감
- 운영 리스크 상세: 인터넷 outbound 워크로드를 명시 분리해야 함
- 후속 spec 영향: 02, 03, 08
- 근거 링크: — (없음)

### OD-NET-002 Details

- Decision ID: OD-NET-002
- Selected: 미사용(기본안)
- Status: 🟢 확정
- 선택지: NAT GW multi-AZ / 미사용
- 비용 영향 상세: ~$86+ /월 절감
- 운영 리스크 상세: live AZ 장애 시 outbound는 public 워크로드에 의존
- 후속 spec 영향: 02, 03, 08
- 근거 링크: — (없음)

### OD-NET-003 Details

- Decision ID: OD-NET-003
- Selected: EC2+EIP 유지
- Status: 🟢 확정
- 선택지: EC2+EIP / NAT GW EIP / BYOIP
- 비용 영향 상세: EC2 단가만
- 운영 리스크 상세: broker IP 등록을 EIP에 묶음. EC2 교체 시 EIP detach/attach Runbook 필요
- 후속 spec 영향: 03 marketconnector-ec2
- 근거 링크: — (없음)

### OD-NET-004 Details

- Decision ID: OD-NET-004
- Selected: public subnet + assignPublicIp (1순위)
- Status: 🟡 잠정
- 선택지: public subnet + publicIp / ECS on EC2 / NAT Instance
- 비용 영향 상세: 0
- 운영 리스크 상세: Task SG 실수 시 외부 노출 위험. Egress 검증 필요
- 후속 spec 영향: 08
- 근거 링크: — (없음)

### OD-NET-005 Details

- Decision ID: OD-NET-005
- Selected: 권고(S3 GW + ECR api+dkr + Secrets + SSM + Logs)
- Status: 🟢 확정
- 선택지: 최소 / 권고 / 확장
- 비용 영향 상세: ~$30 ~ $80 /월 (AZ 수에 비례)
- 운영 리스크 상세: endpoint 누락 시 NAT 없이 AWS API 접근 불가
- 후속 spec 영향: 02, 06, 07
- 근거 링크: — (없음)

### OD-NET-006 Details

- Decision ID: OD-NET-006
- Selected: 우선 미사용. 필요 시 활성
- Status: 🟡 잠정
- 선택지: 활성 / 미사용
- 비용 영향 상세: AZ당 ~$8/월 추가
- 운영 리스크 상세: 미사용 시 KMS 호출이 NAT가 없으면 실패할 수 있음
- 후속 spec 영향: 02, 06
- 근거 링크: — (없음)

### OD-NET-007 Details

- Decision ID: OD-NET-007
- Selected: 초기 미사용
- Status: 🟡 잠정
- 선택지: internal ALB / 미사용
- 비용 영향 상세: ~$16.5/월 절감
- 운영 리스크 상세: port-view 운영자 접근은 SSM 포트포워딩 또는 internal IP
- 후속 spec 영향: 05 port-view-ecs
- 근거 링크: — (없음)

### OD-NET-008 Details

- Decision ID: OD-NET-008
- Selected: 초기 비용 절감안 보류
- Status: 🟡 잠정
- 선택지: internal ALB(안정성) / 미사용(절감)
- 비용 영향 상세: ~$16.5/월 절감
- 운영 리스크 상세: live 운영자 접근 정책에 따라 재검토
- 후속 spec 영향: 05
- 근거 링크: — (없음)

### OD-NET-009 Details

- Decision ID: OD-NET-009
- Selected: SSM Session Manager만 사용
- Status: 🟢 확정
- 선택지: SSH / SSM Session Manager
- 비용 영향 상세: 0
- 운영 리스크 상세: EC2 SSH 22 inbound 0.0.0.0/0 절대 금지
- 후속 spec 영향: 02, 03
- 근거 링크: — (없음)

### OD-RDS-001 Details

- Decision ID: OD-RDS-001
- Selected: db.t4g.small single-AZ
- Status: 🟢 확정
- 선택지: db.t4g.micro / small / medium
- 비용 영향 상세: ~$26 + storage
- 운영 리스크 상세: dev 부재로 paper에 검증 부하 집중
- 후속 spec 영향: 02, 04, 05
- 근거 링크: — (없음)

### OD-RDS-002 Details

- Decision ID: OD-RDS-002
- Selected: single-AZ 시작 가능
- Status: 🟡 잠정
- 선택지: single-AZ / multi-AZ
- 비용 영향 상세: multi-AZ 대비 약 50% 절감
- 운영 리스크 상세: AZ 장애 시 다운타임. PITR로만 복구
- 후속 spec 영향: 02, 10
- 근거 링크: — (없음)

### OD-RDS-003 Details

- Decision ID: OD-RDS-003
- Selected: multi-AZ
- Status: 🟢 확정
- 선택지: single-AZ / multi-AZ
- 비용 영향 상세: 비용 절감안 대비 약 2배
- 운영 리스크 상세: AZ failover 자동, 다운타임 최소
- 후속 spec 영향: 02, 10
- 근거 링크: — (없음)

### OD-RDS-004 Details

- Decision ID: OD-RDS-004
- Selected: 16 이상
- Status: 🟢 확정
- 선택지: 14 / 15 / 16 / 17
- 비용 영향 상세: 0
- 운영 리스크 상세: minor auto upgrade 정책 paper/live 활성
- 후속 spec 영향: 02
- 근거 링크: — (없음)

### OD-RDS-005 Details

- Decision ID: OD-RDS-005
- Selected: aws-paper KMS default, aws-live CMK 권고
- Status: 🟡 잠정
- 선택지: KMS default / CMK
- 비용 영향 상세: KMS key 관리 비용 작음
- 운영 리스크 상세: CMK 사용 시 IAM 권한 매트릭스 추가
- 후속 spec 영향: 02, 06
- 근거 링크: — (없음)

### OD-RDS-006 Details

- Decision ID: OD-RDS-006
- Selected: 7일
- Status: 🟢 확정
- 선택지: 1 / 3 / 7 / 14일
- 비용 영향 상세: 작음
- 운영 리스크 상세: 7일 이상 데이터 손실 시 외부 백업 필요
- 후속 spec 영향: 02
- 근거 링크: — (없음)

### OD-RDS-007 Details

- Decision ID: OD-RDS-007
- Selected: 14일
- Status: 🟢 확정
- 선택지: 7 / 14 / 35일
- 비용 영향 상세: 중
- 운영 리스크 상세: 비용 절감 시 7일까지 단축 가능
- 후속 spec 영향: 02, 10
- 근거 링크: — (없음)

### OD-RDS-008 Details

- Decision ID: OD-RDS-008
- Selected: aws-paper on, aws-live on
- Status: 🟢 확정
- 선택지: on / off
- 비용 영향 상세: 작음
- 운영 리스크 상세: 다른 시점 복구 가능
- 후속 spec 영향: 02
- 근거 링크: — (없음)

### OD-RDS-009 Details

- Decision ID: OD-RDS-009
- Selected: cutover 직전 + 분기
- Status: 🟢 확정
- 선택지: 분기 / cutover 전 / 운영자 결정
- 비용 영향 상세: 매우 작음
- 운영 리스크 상세: snapshot 명명 규칙(`before-cutover-{date}`)
- 후속 spec 영향: 02, 10
- 근거 링크: — (없음)

### OD-DB-001 Details

- Decision ID: OD-DB-001
- Selected: portfolio (모든 환경 동일)
- Status: 🟢 확정
- 선택지: portfolio / 환경별 분리
- 비용 영향 상세: 0
- 운영 리스크 상세: 환경변수 호환성 유지
- 후속 spec 영향: 02, 06
- 근거 링크: — (없음)

### OD-DB-002 Details

- Decision ID: OD-DB-002
- Selected: schema-per-domain 10개 유지
- Status: 🟢 확정
- 선택지: schema-per-domain / DB 분리
- 비용 영향 상세: 0
- 운영 리스크 상세: 기존 search_path 유지
- 후속 spec 영향: 02
- 근거 링크: — (없음)

### OD-DB-003 Details

- Decision ID: OD-DB-003
- Selected: INTEREST_DB_* / PORT_* / PORTFOLIO_DB_NAME 모두 유지
- Status: 🟢 확정
- 선택지: 변경 / 유지
- 비용 영향 상세: 0
- 운영 리스크 상세: 코드 무수정 정책 강제
- 후속 spec 영향: 02, 06
- 근거 링크: — (없음)

### OD-DB-004 Details

- Decision ID: OD-DB-004
- Selected: 7개 role(marketconnector_app, crawler_app, preprocessor_app, decision_app, execution_app, research_app, view_app)
- Status: 🟢 확정
- 선택지: 단일 admin / role 분리
- 비용 영향 상세: 0
- 운영 리스크 상세: 권한 매트릭스 운영 부담
- 후속 spec 영향: 02, 06
- 근거 링크: — (없음)

### OD-DB-005 Details

- Decision ID: OD-DB-005
- Selected: 모든 schema READ + ops WRITE 기본. execution write는 05에서 재검토
- Status: 🟡 잠정
- 선택지: 읽기 전용 / read+ops write / read+ops+execution write
- 비용 영향 상세: 0
- 운영 리스크 상세: port-view에서 execution write가 필요하면 05에서 변경
- 후속 spec 영향: 02, 05
- 근거 링크: — (없음)

### OD-DB-006 Details

- Decision ID: OD-DB-006
- Selected: MS별 README 그대로 유지
- Status: 🟢 확정
- 선택지: 변경 / 유지
- 비용 영향 상세: 0
- 운영 리스크 상세: role별 ALTER ROLE SET search_path 적용
- 후속 spec 영향: 02
- 근거 링크: — (없음)

### OD-DB-007 Details

- Decision ID: OD-DB-007
- Selected: 모든 app role에 USAGE / SELECT 미부여(2026-06-09 1차 적용 결과 반영)
- Status: 🟢 확정
- 선택지: 부여 / 미부여
- 비용 영향 상세: 0
- 운영 리스크 상세: 02 design.md 매트릭스(legacy R 일부 부여) 대비 보안 강화. legacy 데이터 접근 필요한 MS 식별 시 별도 결정으로 grant
- 후속 spec 영향: 02, 05, 06
- 근거 링크: — (없음)

### OD-DB-008 Details

- Decision ID: OD-DB-008
- Selected: R-only 축소(2026-06-09 1차 적용 반영)
- Status: 🟢 확정
- 선택지: R/W / R-only / 미부여
- 비용 영향 상세: 0
- 운영 리스크 상세: execution write는 execution_app 단독으로 한정. 02 design.md 매트릭스(R/W) 대비 권한 분리 강화
- 후속 spec 영향: 02, 03, 04
- 근거 링크: — (없음)

### OD-DB-009 Details

- Decision ID: OD-DB-009
- Selected: R-only 유지(write 필요성은 05 spec에서 재검토)
- Status: 🟡 잠정
- 선택지: R-only / R+W
- 비용 영향 상세: 0
- 운영 리스크 상세: OD-DB-005를 본 결정으로 분리. View에서 execution write가 꼭 필요해지면 05에서 변경
- 후속 spec 영향: 02, 05
- 근거 링크: — (없음)

### OD-DB-010 Details

- Decision ID: OD-DB-010
- Selected: 미실행(기존 table / sequence / index owner는 `portfolio_admin` 유지)
- Status: 🟢 확정
- 선택지: 즉시 실행 / 미실행
- 비용 영향 상세: 0
- 운영 리스크 상세: schema owner는 `portfolio_owner`로 이관 완료. default privileges는 새 객체에만 자동 적용. 기존 객체 일괄 이관 여부는 후속 결정으로 분리 관리
- 후속 spec 영향: 02, 06
- 근거 링크: — (없음)

### OD-MS-001 Details

- Decision ID: OD-MS-001
- Selected: EC2+EIP
- Status: 🟢 확정
- 선택지: EC2+EIP / ECS Fargate / Beanstalk / App Runner / Lambda
- 비용 영향 상세: EC2 단가 + EIP attach 무료
- 운영 리스크 상세: EC2 SG 실수 시 외부 노출. SSM + SG 통제 필수
- 후속 spec 영향: 03
- 근거 링크: — (없음)

### OD-MS-002 Details

- Decision ID: OD-MS-002
- Selected: ECS Fargate Service (1순위), Elastic Beanstalk (2순위 비교 본문 유지)
- Status: 🟢 확정
- 선택지: ECS Fargate Service / Elastic Beanstalk / App Runner / EC2 / EKS
- 비용 영향 상세: Fargate per-task
- 운영 리스크 상세: ALB 도입 여부는 OD-NET-007 / 008
- 후속 spec 영향: 05
- 근거 링크: — (없음)

### OD-MS-003 Details

- Decision ID: OD-MS-003
- Selected: ECS Fargate Task NAT-free public (1순위), ECS on EC2 (Selenium 안정성 미달 시 승격)
- Status: 🟢 확정
- 선택지: ECS Fargate Task (NAT-free public) / ECS on EC2 / EC2 / Lambda / Beanstalk
- 비용 영향 상세: Fargate per-task. NAT 없음
- 운영 리스크 상세: Selenium / KRX 로그인 stateful. SG 통제 필수
- 후속 spec 영향: 08
- 근거 링크: — (없음)

### OD-MS-004 Details

- Decision ID: OD-MS-004
- Selected: ECS Fargate Task (1순위), Lambda는 짧은 step만 보조
- Status: 🟢 확정
- 선택지: ECS Fargate Task / Lambda / AWS Batch / EC2
- 비용 영향 상세: Fargate per-task
- 운영 리스크 상세: NAT-free에서 holiday API outbound는 public subnet 필요
- 후속 spec 영향: 08
- 근거 링크: — (없음)

### OD-MS-005 Details

- Decision ID: OD-MS-005
- Selected: 별도 컴퓨트 없음. git submodule packaging (1순위), wheel+CodeArtifact (성숙기 2순위)
- Status: 🟢 확정
- 선택지: git submodule packaging / wheel+CodeArtifact / 컴퓨트 배포
- 비용 영향 상세: 0
- 운영 리스크 상세: 각 MS 이미지 빌드 시점 버전 동기화 부담
- 후속 spec 영향: 07
- 근거 링크: — (없음)

### OD-MS-006 Details

- Decision ID: OD-MS-006
- Selected: ECS Fargate Task + EventBridge Scheduler (1순위, 04에서 Step Functions 통합), Lambda 비권고
- Status: 🟢 확정
- 선택지: ECS Fargate Task + EventBridge / Lambda / AWS Batch / EC2
- 비용 영향 상세: Fargate per-task
- 운영 리스크 상세: 다중 schema read·write + 공통 라이브러리. Lambda timeout / connection 누수 위험
- 후속 spec 영향: 04
- 근거 링크: — (없음)

### OD-MS-007 Details

- Decision ID: OD-MS-007
- Selected: ECS Fargate Task + Step Functions + EventBridge Scheduler (1순위), Lambda 비권고
- Status: 🟢 확정
- 선택지: ECS Fargate Task + Step Functions + EventBridge / ECS Service / Lambda / AWS Batch
- 비용 영향 상세: Fargate per-task
- 운영 리스크 상세: live BUY/SELL 자동 재시도 금지 정책을 state machine 레벨에서 강제
- 후속 spec 영향: 04, 10
- 근거 링크: — (없음)

### OD-MS-008 Details

- Decision ID: OD-MS-008
- Selected: AWS Batch (1순위, Step Functions 보조), ECS Fargate Task (2순위), Lambda 비권고
- Status: 🟢 확정
- 선택지: AWS Batch / ECS Fargate Task / Lambda / EC2
- 비용 영향 상세: 사용량 기반
- 운영 리스크 상세: 장시간 backtest / RDS connection 누수 점검. report S3 보관
- 후속 spec 영향: 09
- 근거 링크: — (없음)

### OD-MS-009 Details

- Decision ID: OD-MS-009
- Selected: Step Functions + EventBridge Scheduler + ECS RunTask
- Status: 🟢 확정
- 선택지: port-view subprocess / Step Functions + EventBridge + ECS RunTask / EKS CronJob
- 비용 영향 상세: Step Functions transitions ≪ ECS Task 비용
- 운영 리스크 상세: 기존 port-view subprocess는 AWS에서 그대로 쓰지 않음
- 후속 spec 영향: 04, 05
- 근거 링크: — (없음)
- 운영 실증 상세 [2026-07-20]: Step Functions orchestration 의 실패 처리 정합 보강. Task 자체 오류는 기존 Catch 로 실패 Slack 에 연결돼 있었으나, Task 가 정상 종료했지만 결과값이 비정상인 경우 Choice Default 가 직접 Fail State 로 진입해 실패 Slack 을 우회하던 문제를 확인했다. Step13·14·15·16·Step1 각 Default 를 실패 컨텍스트 Pass State(`$.dailyExecutionFailure` 에 Error·Cause 저장 후 실패 Slack · OPS 실패 기록 실행, 최종 상태는 FAILED 유지) 로 변경해 Choice 결과 실패도 notifier · recorder 경로로 연결했다(`Step12_Failed` 제외). 또한 `Step1_SendConnectorBalanceCommand` 의 잔고 명령 `--run-date 2026-06-22` 하드코딩을 제거하고 실행 입력 `$.runDate` 를 States.Array · States.Format 기반 동적 commands 배열로 하위 SSM 명령까지 전달하도록 변경했다. ASL 검증 OK · 배포 후 재조회 확인 · Status 기존 값 그대로 유지 · 신규 Decision ID 없음 · 상세 근거는 R-AUTO-037 [2026-07-20 보강] 참조.
- 운영 실증 상세 [2026-07-20 성공 경로 Builder 연결]: 같은 일자 별도 후속으로 Step 12~17 성공 경로에 DB-backed Builder Lambda(`BuildDailyExecutionSuccessSlackSummary` · `portfolio-daily-execution-slack-summary-builder`) 를 삽입해 Step 완료 → Builder → Notifier → `RecordWorkflowStepSuccess` 순서로 연결했다. Builder 는 runDate 를 입력받아 당일 실제 체결을 조회한 결과를 `$.dailyExecutionSuccessSummary` 에 저장하고, Notifier 는 `$.dailyExecutionSuccessSummary.Payload` 를 성공 Slack payload 로 받는다. Notifier 성공 후 기존 최종 OPS 성공 기록 경로(`RecordWorkflowStepSuccess`)는 그대로 유지하며 Builder 실패 시 기존 `SendDailyExecutionFailedSlack` 경로로 연결한다. ASL 검증 OK · canonical 의미 비교 차이 0건 · 수동 smoke 만 수행(전체 execution 미시작) · Status 기존 값 그대로 유지 · 신규 Decision ID 없음 · 상세 근거는 R-AUTO-023 [2026-07-20 보강] 참조.

### OD-MS-010 Details

- Decision ID: OD-MS-010
- Selected: 도메인 알림은 SlackNotificationService 유지, 인프라 알람은 SNS → Lambda → Slack webhook fan-out
- Status: 🟡 잠정
- 선택지: SlackNotificationService 단독 / SNS+Lambda+Slack 보조
- 비용 영향 상세: 무료 한도 안
- 운영 리스크 상세: webhook URL은 Secrets Manager 또는 SSM SecureString. 06에서 최종 결정
- 후속 spec 영향: 05, 10
- 근거 링크: — (없음)

### OD-MS-011 Details

- Decision ID: OD-MS-011
- Selected: Hybrid(KRX GUI=Windows EC2 worker · non-GUI=ECS Fargate Task 후보). preprocessor=ECS Fargate Task. See details: OD-MS-011 Details
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: EC2 idle + Fargate per-task
- 운영 리스크 상세: EC2 worker stop 절차 · 자동화 미도달
- 후속 spec 영향: 08, 04, 05, 09, 10
- 근거 링크: — (없음)

### OD-MS-012 Details

- Decision ID: OD-MS-012
- Selected: wrapper 기반 수동 실행(`run_krx_worker_daily.ps1`). See details: OD-MS-012 Details
- Status: 🟡 잠정
- 선택지: 여러 선택지 검토
- 비용 영향 상세: 0 (wrapper 자체)
- 운영 리스크 상세: wrapper 성공이 DB 적재 성공을 보장 안 함 (R-AUTO-007)
- 후속 spec 영향: 08
- 근거 링크: — (없음)

### OD-SEC-001 Details

- Decision ID: OD-SEC-001
- Selected: 06에서 최종 결정. 본 spec은 placeholder
- Status: 🔴 미정
- 선택지: Secrets Manager / SSM SecureString / 혼합
- 비용 영향 상세: secret 수에 비례
- 운영 리스크 상세: rotation 정책 미정
- 후속 spec 영향: 06
- 근거 링크: — (없음)

### OD-SEC-002 Details

- Decision ID: OD-SEC-002
- Selected: Secrets Manager(권고)
- Status: 🟡 잠정
- 선택지: Secrets Manager / 직접 입력
- 비용 영향 상세: $0.40/secret/월
- 운영 리스크 상세: 06 결정에 따라 변경 가능
- 후속 spec 영향: 06
- 근거 링크: — (없음)

### OD-SEC-003 Details

- Decision ID: OD-SEC-003
- Selected: EC2 로컬+S3 backup 1순위
- Status: 🟡 잠정
- 선택지: EC2 로컬+S3 backup / EFS / Secrets Manager
- 비용 영향 상세: 매우 작음
- 운영 리스크 상세: EC2 교체 시 토큰 인계 Runbook 필수
- 후속 spec 영향: 03
- 근거 링크: — (없음)

### OD-SEC-004 Details

- Decision ID: OD-SEC-004
- Selected: 미오픈. SSM Session Manager만 사용
- Status: 🟢 확정
- 선택지: 0.0.0.0/0 / 미오픈
- 비용 영향 상세: 0
- 운영 리스크 상세: OD-NET-009와 동일
- 후속 spec 영향: 02, 03
- 근거 링크: — (없음)

### OD-SEC-008 Details

- Decision ID: OD-SEC-008
- Selected: Secrets Manager (`/portfolio/{env}/krx/crawler-login`, JSON `username` / `password`). EC2 worker IAM Role inline policy 에 secret 한정 read 허용
- Status: 🟡 잠정
- 선택지: 코드 / 설정 파일 / 환경변수 직접 / Secrets Manager / SSM SecureString
- 비용 영향 상세: $0.40/secret/월 + KMS 호출 미미
- 운영 리스크 상세: secret read 권한 누락 시 worker 기동 실패. 권한 변경 시 운영자 노트에 4줄 요약 기록
- 후속 spec 영향: 06, 08
- 근거 링크: — (없음)

### OD-OBS-001 Details

- Decision ID: OD-OBS-001
- Selected: CloudWatch Logs 사용
- Status: 🟢 확정
- 선택지: 사용 / Logs 외 도구
- 비용 영향 상세: retention 비례
- 운영 리스크 상세: 비용 폭주 방지 위해 retention 짧게 시작
- 후속 spec 영향: 04, 05, 08
- 근거 링크: — (없음)

### OD-OBS-002 Details

- Decision ID: OD-OBS-002
- Selected: 7일 시작
- Status: 🟡 잠정
- 선택지: 7 / 14 / 30일
- 비용 영향 상세: 작음
- 운영 리스크 상세: 장애 분석 회고에 부족할 수 있음
- 후속 spec 영향: 02, 05
- 근거 링크: — (없음)

### OD-OBS-003 Details

- Decision ID: OD-OBS-003
- Selected: 14일 시작, 운영 안정 후 30일로 상향 가능
- Status: 🟡 잠정
- 선택지: 14 / 30 / 90일
- 비용 영향 상세: 중
- 운영 리스크 상세: live 감사용 90일 검토 가능
- 후속 spec 영향: 05, 10
- 근거 링크: — (없음)

### OD-OBS-004 Details

- Decision ID: OD-OBS-004
- Selected: 06에서 최종 결정
- Status: 🔴 미정
- 선택지: Secrets Manager / SSM SecureString
- 비용 영향 상세: 작음
- 운영 리스크 상세: webhook URL 노출 시 외부 발신 위험
- 후속 spec 영향: 06
- 근거 링크: — (없음)

### OD-CUT-001 Details

- Decision ID: OD-CUT-001
- Selected: pg_dump+pg_restore 1순위
- Status: 🟢 확정
- 선택지: pg_dump+pg_restore / AWS DMS / snapshot 복원
- 비용 영향 상세: 0
- 운영 리스크 상세: 짧은 다운타임 허용
- 후속 spec 영향: 02, 10
- 근거 링크: — (없음)

### OD-CUT-002 Details

- Decision ID: OD-CUT-002
- Selected: 보류
- Status: 🔵 보류
- 선택지: 즉시 / 보류
- 비용 영향 상세: DMS 인스턴스 비용
- 운영 리스크 상세: 무중단이 꼭 필요해질 때 재검토
- 후속 spec 영향: 10
- 근거 링크: — (없음)

### OD-CUT-003 Details

- Decision ID: OD-CUT-003
- Selected: paper 검증 후
- Status: 🟢 확정
- 선택지: paper 검증 후 / 즉시
- 비용 영향 상세: live 비용 지연
- 운영 리스크 상세: paper 검증 N영업일 운영자 결정
- 후속 spec 영향: 10
- 근거 링크: — (없음)

### OD-CUT-004 Details

- Decision ID: OD-CUT-004
- Selected: 병행 운영(rollback 보험용)
- Status: 🟡 잠정
- 선택지: aws-paper 가동 후 즉시 종료 / 병행 운영
- 비용 영향 상세: local 호스트 비용만
- 운영 리스크 상세: 데이터 분기 관리 부담
- 후속 spec 영향: 10
- 근거 링크: — (없음)

### OD-SAFE-001 Details

- Decision ID: OD-SAFE-001
- Selected: 초기 차단 → 검증 후 허용
- Status: 🟢 확정
- 선택지: 차단 / 허용
- 비용 영향 상세: 0
- 운영 리스크 상세: 모의투자라도 fill/position sync 오류는 재현해야 함
- 후속 spec 영향: 04, 10
- 근거 링크: — (없음)
- 운영 실증 상세 [2026-07-16]: 본 결정의 "검증 후 허용" 라인이 처음으로 실 매수·체결·Fill·Position E2E 로 회수됐다. Daily Run 76 · BUY 후보 2건(엔씨소프트 036570 13주 · 코오롱생명과학 102940 78주) 이 KIS 모의투자에서 전량 체결되어 Execution Order 2건 `FILLED` · Position ID 13 · 14 `OPEN` 생성까지 이어졌다. 다만 초기 Step 13 조회가 주문 제출 후 약 10초 시점에 이뤄져 부분체결 · 접수 상태로 고착됐고 실제 시장가 주문은 이후 계속 체결됐으나 내부 상태가 최초 조회 결과에 머무는 문제(Step Functions ExitCode 0 SUCCESS 가 Fill · Position 정합 완료를 보장하지 않는 축) 가 확인되어, 운영자가 Step 13 → Step 15 → Step 16 순서로 재실행해 DB 정합을 복구했다. 재발 방지 조치로 State Machine `portfolio-paper-daily-step12-17-approval` Wait State `Step12_WaitBeforeCheck` `Seconds` 10 → 60 변경(실제 ARN · revision ID · broker 주문번호 원문 문서 미기록). 시장가 주문 접수 성공만으로 체결 완료를 판단하지 않고 Order Request · Fill · Execution Order · Position 정합을 함께 확인한다는 원칙이 명확해졌으며, `ACCEPTED` / `PARTIAL_FILLED` polling · Step 13 · 15 · 16 처리 건수 기반 실패 전파 강화 · Workflow FAIL 전파 · OPS Mirror 세부 Step 확장은 후속 유지된다. 본 변경은 aws-paper 한정 · aws-live 자동 BUY / SELL 정책 변경 없음(OD-SAFE-002 / OD-SAFE-003 유지). Status 기존 값 그대로 유지 · 신규 Decision ID 없음 · Decision Summary count 변경 없음 · 상세 근거는 R-AUTO-037 · R-AUTO-038 · R-BROKER-004 [2026-07-16 보강] 참조.
- 운영 실증 상세 [2026-07-20]: paper 자동 BUY/SELL 실운영 장애 복구 회차. 2026-07-20 09:01 자동 실행 State Machine `portfolio-paper-daily-step12-17-approval` 이 Step 13(주문·체결 조회)에서 KIS `EGW00201`(초당 거래건수 초과) 로 실패했고, Step 12 주문 제출 자체는 정상 완료(매도 2건 broker 접수 · 첫 주문 전량 체결) 였다. 운영자는 Step 12 를 중복 제출 방지를 위해 재실행하지 않고 Step 13 만 수동 실행해 두 주문 모두 `FILLED` 로 복구한 뒤 Step 14~17 을 순차 수동 완주했다. 성공 Slack 은 자동 실행 결과가 아니라 수동 복구 완료 후 `portfolio-event-notifier` 수동 호출로 수신했으며 09:01 자동 실행 이력은 실제 장애 보존을 위해 FAILED 로 유지했다. 본 결정의 "초기 차단 → 검증 후 허용" 라인 위에서 자동 실행 실패 시에도 중복 주문 없이 수동 복구가 가능함을 재확인했다. aws-paper 한정 · aws-live 정책 변경 없음(OD-SAFE-002 / OD-SAFE-003 유지). Status 기존 값 그대로 유지 · 신규 Decision ID 없음 · Decision Summary count 변경 없음 · 상세 근거는 R-AUTO-001 · R-AUTO-037 · R-BROKER-004 [2026-07-20 보강] 참조.

### OD-SAFE-002 Details

- Decision ID: OD-SAFE-002
- Selected: 후보+수동 승인 우선, 검증 후 단계적
- Status: 🟢 확정
- 선택지: 즉시 자동 / 후보+수동 승인 / 단계적 자동
- 비용 영향 상세: 0
- 운영 리스크 상세: 1회 잘못된 자동 재시도가 큰 손실로 이어질 수 있음
- 후속 spec 영향: 04, 05, 10
- 근거 링크: — (없음)

### OD-SAFE-003 Details

- Decision ID: OD-SAFE-003
- Selected: OD-SAFE-002와 동일 정책
- Status: 🟢 확정
- 선택지: 동상
- 비용 영향 상세: 0
- 운영 리스크 상세: 동상
- 후속 spec 영향: 04, 05, 10
- 근거 링크: — (없음)

### OD-SAFE-004 Details

- Decision ID: OD-SAFE-004
- Selected: idempotent step만 자동 재시도
- Status: 🟢 확정
- 선택지: 모든 step / idempotent만
- 비용 영향 상세: 0
- 운영 리스크 상세: BUY/SELL/fill sync/position 변경/intraday stop SELL 생성은 재시도 금지
- 후속 spec 영향: 04, 08, 10
- 근거 링크: — (없음)
- 운영 실증 상세 [2026-07-20]: 본 결정의 "idempotent step 만 자동 재시도" 원칙이 rate-limit 보완 회차에서 재확인됐다. 2026-07-20 Step 13 조회에서 KIS `EGW00201`(초당 거래건수 초과) 가 발생해 운영자가 `connector_order_check.py`(2.0.1 → 2.0.2) 에 재시도를 추가했는데, 이는 broker 주문 제출(BUY/SELL) 자동 재시도가 아니라 idempotent 한 주문·체결 조회 API 의 `EGW00201` 한정 최대 2회 재시도(1차 1.5초 · 2차 5초 · 주문 간 5초 대기 결합) 다. broker 주문 자동 재시도 금지 정책은 그대로 유지되고, Step 12 는 중복 주문 방지를 위해 재실행하지 않았다. Status 기존 값 그대로 유지 · 신규 Decision ID 없음 · 상세 근거는 R-AUTO-001 · R-BROKER-004 [2026-07-20 보강] 참조.

## Change Log

각 row 의 `Details` 링크는 아래 `Decision Change Log Details` 섹션의 원본 evidence · 리스크 · 안전 기록 상세로 이동한다. spec operation-notes 링크는 각 Details 항목 안에 유지한다.

| 일자 | 변경 요약 | Details |
|------|-----------|---------|
| 2026-06-05 | 문서 구조 가독성 개선 (Status Legend / Decision Summary / At a Glance / 카테고리별 상세 결정표 재정리). 결정값 / Status 내부 기준값 변경 없음. | (본문 유지) |
| 2026-06-09 | OD-DB-007 ~ OD-DB-010 추가 (DB Role / 권한 1차 적용) | [2026-06-09 1차](#change-log-details-2026-06-09-1) |
| 2026-06-10 | OD-SEC-005 / OD-SEC-006 / OD-SEC-007 추가 (06 / 03 spec 결정) | [2026-06-10 1차](#change-log-details-2026-06-10-1) |
| 2026-06-10 | OD-NET-004 1차 검증 메모 추가 (08 spec 결과) | [2026-06-10 2차](#change-log-details-2026-06-10-2) |
| 2026-06-12 | OD-MS-011 / OD-MS-012 / OD-SEC-008 추가 (08 spec Hybrid execution model) | [2026-06-12 1차](#change-log-details-2026-06-12-1) |
| 2026-06-13 | OD-MS-013 / OD-MS-014 추가 + OD-MS-009 보강 (Strategy Decision ECS/Fargate 1차 검증) | [2026-06-13 1차](#change-log-details-2026-06-13-1) |
| 2026-06-13 | OD-MS-015 추가 + OD-MS-012 보강 (SSM RunCommand 자동화 + ECS crawler smoke) | [2026-06-13 2차](#change-log-details-2026-06-13-2) |
| 2026-06-13 | OD-MS-016 + OD-ENV-006/007/008 추가 (Execution/Connector 책임 분리 + Local↔Paper RDS) | [2026-06-13 3차](#change-log-details-2026-06-13-3) |
| 2026-06-13 | OD-NET-010 추가 + OD-ENV-006/007/008 1차 실증 (SSM Port Forwarding 연결 검증) | [2026-06-13 4차](#change-log-details-2026-06-13-4) |
| 2026-06-13 | OD-NET-011 추가 + R-AUTO-012/013 보강 (psql 18 + pgAdmin4 접속 검증) | [2026-06-13 5차](#change-log-details-2026-06-13-5) |
| 2026-06-13 | OD-MS-017 추가 + OD-MS-007/009 + OD-SAFE-004 1차 실증 (Strategy Execution 포팅) | [2026-06-13 6차](#change-log-details-2026-06-13-6) |
| 2026-06-13 | OD-MS-018 추가 + OD-MS-008 1차 실증 (Strategy Research Batch image 1차 준비) | [2026-06-13 7차](#change-log-details-2026-06-13-7) |
| 2026-06-15 | OD-MS-019 추가 + OD-MS-008/018 1차 실증 (Research AWS Batch 실행 검증) | [2026-06-15 1차](#change-log-details-2026-06-15-1) |
| 2026-06-15 | OD-MS-020/021 추가 + OD-SEC-006 1차 실증 (E2E dry-run + Crawler 상태 재판정) | [2026-06-15 2차](#change-log-details-2026-06-15-2) |
| 2026-06-16 | OD-MS-022 추가 + OD-MS-011/015/020 1차 실증 (Crawler 데이터 미수집 해결 + KRX EC2) | [2026-06-16 1차](#change-log-details-2026-06-16-1) |
| 2026-06-16 | OD-MS-008/013/019/021 1차 실증 (E2E dry-run safe subset 재개) | [2026-06-16 2차](#change-log-details-2026-06-16-2) |
| 2026-06-17 | OD-SEC-006 + OD-MS-001/009 1차 실증 (MarketConnector 조회성 dry-run 재검증) | [2026-06-17 1차](#change-log-details-2026-06-17-1) |
| 2026-06-17 | OD-MS-016/021 + OD-DB-008 + OD-MS-008/019 1차 실증 (Daily AWS 17-step E2E 완료) | [2026-06-17 2차](#change-log-details-2026-06-17-2) |
| 2026-06-17 | OD-MS-023 추가 + OD-MS-009/021/016 + OD-SAFE-002/003/004 1차 실증 (PowerShell wrapper) | [2026-06-17 3차](#change-log-details-2026-06-17-3) |
| 2026-06-18 | OD-MS-024 추가 + OD-MS-016/021/023 1차 실증 (Daily AWS Paper Wrapper 실운영) | [2026-06-18 1차](#change-log-details-2026-06-18-1) |
| 2026-06-18 | OD-MS-025 추가 + OD-MS-016/021/023 보강 (Step 13 단건 순차 조회 기본화) | [2026-06-18 2차](#change-log-details-2026-06-18-2) |
| 2026-06-21 | OD-MS-026 추가 + OD-MS-011/015/022/023 보강 (Step 2 성공판정 강화) | [2026-06-21 1차](#change-log-details-2026-06-21-1) |
| 2026-06-22 | OD-MS-027/OD-DB-011 신규 + OD-MS-016/021/023 보강 (Daily 1~17 두 번째 완주 + 첫 SELL E2E) | [2026-06-22 1차](#change-log-details-2026-06-22-1) |
| 2026-06-23 | OD-MS-028/029 신규 + OD-MS-009/027 + OD-SAFE-001/004 보강 (Step Fn approval 실전 검증) | [2026-06-23 1차](#change-log-details-2026-06-23-1) |
| 2026-06-23 | OD-MS-030/031 신규 + OD-MS-009/010/029 보강 (AWS 공통 Slack notifier + 3종 Slack) | [2026-06-23 2차](#change-log-details-2026-06-23-2) |
| 2026-06-23 | OD-MS-032 신규 + OD-MS-009/029/031 보강 (Scheduler + Dispatcher Lambda 자동화) | [2026-06-23 3차](#change-log-details-2026-06-23-3) |
| 2026-06-24 | OD-MS-033 신규 + OD-MS-028/032 + OD-SAFE-001~004 보강 (08:00 Scheduler + retry-normalizer) | [2026-06-24 1차](#change-log-details-2026-06-24-1) |
| 2026-06-24 | OD-MS-034 신규 + OD-MS-009/032/001 + OD-NET-003 보강 (EC2 lifecycle 자동화) | [2026-06-24 2차](#change-log-details-2026-06-24-2) |
| 2026-06-24 | OD-MS-035 신규 (장중 포지션 확인 3단계 구조 최종안 확정 / 문서 반영) | [2026-06-24 3차](#change-log-details-2026-06-24-3) |
| 2026-06-25 | OD-MS-036 신규 + OD-MS-035 보강 (장중 포지션 Step Function 구현 완료 · 실주문 테스트 보류) | [2026-06-25 1차](#change-log-details-2026-06-25-1) |
| 2026-06-27 | OD-MS-037 신규 (View Local AWS Paper read-only 1차 scope · ECS 이전 로컬 검증) | [2026-06-27 1차](#change-log-details-2026-06-27-1) |
| 2026-06-29 (2) | OD-MS-002/009/037 보강 (port-view aws-stepfunctions Daily Batch trigger + Step 1~11 검증) | [2026-06-29 (2) 1차](#change-log-details-2026-06-29--2--1) |
| 2026-06-29 (3) | OD-MS-002/009/037 보강 (Step 12~17 승인형 검증 + Local wrapper 정리 + Approval SM 분리) | [2026-06-29 (3) 1차](#change-log-details-2026-06-29--3--1) |
| 2026-06-30 (오후) | OD-MS-002/009/037 보강 (port-view ECS Fargate Public IP 1차 + Step 12~17 승인 실행) | [2026-06-30 (오후) 1차](#change-log-details-2026-06-30------1) |
| 2026-06-30 (오후) Slack | OD-MS-038 신규 + OD-MS-030/031 evidence 보강 (Slack 문구 개선 최종 완료) | [2026-06-30 (오후) Slack 1차](#change-log-details-2026-06-30------Slack-1) |
| 2026-06-30 (오후) 장중 손절 Slack | OD-MS-030/035/036/038 evidence 보강 (`INTRADAY_STOP_LOSS` Slack 실제 이벤트 연동 완료) | [2026-06-30 (오후) 장중 손절 Slack 1차](#change-log-details-2026-06-30------------Slack-1) |
| 2026-07-01 | OD-SAFE-001/002/003 + OD-MS-009/032/033 evidence 보강 (aws-paper Daily 자동화 1차 풀 ON) | [2026-07-01 1차](#change-log-details-2026-07-01-1) |
| 2026-07-03 | OD-DB-012 · OD-MS-039 신규 (Step Functions 실행 이력 OPS mirror + `ops_recorder_app` 전용 role · Recorder Lambda) | [2026-07-03 1차](#change-log-details-2026-07-03-1) |
| 2026-07-08 | OD-MS-040 신규 (ECS batch container Asia/Seoul timezone 정책 · crawler / preprocessor TaskDefinition `TZ=Asia/Seoul` 단기 패치 · State Machine Step2A / Step3 task revision 갱신) | [2026-07-08 1차](#change-log-details-2026-07-08-1) |

## Decision Change Log Details

본 섹션은 Change Log 표에서 500자 초과로 축약된 각 차수의 원본 evidence · 리스크 · 안전 기록 상세다. 각 차수의 표의 `See details` 링크로 접근한다.

### Change Log Details 2026-06-09 (1차) <a id="change-log-details-2026-06-09-1"></a>

변경 요약: OD-DB-007 ~ OD-DB-010 추가 (DB Role / 권한 1차 적용 결과 반영)

2026-06-09 운영자가 직접 실행한 [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) §4 SQL 결과를 반영했다.

신규 결정: OD-DB-007(legacy schema 모든 app role 미부여, 🟢 확정), OD-DB-008(marketconnector_app execution R-only, 🟢 확정), OD-DB-009(view_app execution R-only, write는 05에서 재검토, 🟡 잠정), OD-DB-010(1차 적용에서 REASSIGN OWNED BY portfolio_admin TO portfolio_owner 미실행, 🟢 확정).

OD-DB-005는 OD-DB-009로 분리되었으나 본문은 보존한다(중복 row 미생성). 비밀번호 / endpoint hostname / 계좌번호는 본 문서에 평문 기록 금지(`[REDACTED]`). 결정값 변경 외 본 일자에 추가된 운영 결과 기록은 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-09 섹션 참조.


### Change Log Details 2026-06-10 (1차) <a id="change-log-details-2026-06-10-1"></a>

변경 요약: OD-SEC-005 / OD-SEC-006 / OD-SEC-007 추가 (06 / 03 spec 결정 반영)

06-secrets-and-iam 1차 락 + 03-marketconnector-ec2 정식 운영 전환 검증 결과를 반영. 신규 결정: OD-SEC-005(EC2 / 8개 MS Access Key 미사용 원칙, 🟡 잠정), OD-SEC-006(EC2 / ECS IAM Role 기반 secret / parameter read 최소 권한 원칙, 🟡 잠정), OD-SEC-007(EC2 운영자 접근 SSM Session Manager 중심 + `AmazonSSMManagedInstanceCore`, 🟡 잠정).

세 결정 모두 06 / 03 spec design 의 입력으로 1차 락 되었으며, 후속 spec(04 / 05 / 08 / 09 / 10)에서 ECS Task Role 패턴 / cutover 검증 통과 후 🟢 확정으로 승격 후보. 실제 secret value / IAM access key id / account-id / 실제 ARN 본 문서 평문 기록 0건.

결정 외 운영 결과 기록은 [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-10 섹션 참조.


### Change Log Details 2026-06-10 (2차) <a id="change-log-details-2026-06-10-2"></a>

변경 요약: OD-NET-004 1차 검증 메모 추가 (08 spec 결과 반영)

08-interest-crawler-and-preprocessor-ecs 의 preprocessor ECS RunTask 가 public subnet + `assignPublicIp = ENABLED` 방식으로 실행되어 OD-NET-004(crawler / preprocessor outbound 방식 = public subnet + assignPublicIp 1순위) 가 preprocessor 측에서 1차 검증되었다는 사실을 메모로 반영.

Status 는 🟡 잠정 유지(crawler runtime 검증은 이월 — R-AUTO-005). 결정값(선택지 / 선택값 / 비용 영향 / 운영 리스크 / 후속 spec 영향) 변경 없음. 실제 secret value / account-id / 실제 ARN / image digest 본 문서 평문 기록 0건.

결정 외 운영 결과 기록은 [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-10 섹션 참조.


### Change Log Details 2026-06-12 (1차) <a id="change-log-details-2026-06-12-1"></a>

변경 요약: OD-MS-011 / OD-MS-012 / OD-SEC-008 추가 (08 spec Hybrid execution model 반영)

2026-06-12 Windows EC2 worker 기반 KRX GUI 의존 수집 1차 검증 결과를 반영.

신규 결정: OD-MS-011(port-interest-crawler runtime 분리: KRX GUI 의존 = Windows EC2 worker, non-GUI = ECS Fargate Task 후보 유지, preprocessor = ECS Fargate Task 유지, 🟡 잠정), OD-MS-012(KRX GUI 의존 crawler 1차 운영 모드 = wrapper 기반 수동 실행, 🟡 잠정),
OD-SEC-008(KRX 로그인 자격 = Secrets Manager `/portfolio/{env}/krx/crawler-login` JSON `username` / `password`, 🟡 잠정).

OD-NET-004 는 KRX GUI 경로가 EC2 worker 로 분리됨에 따라 preprocessor + non-GUI crawler 의 NAT-free public subnet 정책으로 적용 범위가 명확해졌고, 결정값(선택지 / 선택값 / 비용 영향 / 운영 리스크 / 후속 spec 영향) 변경은 없음. R-AUTO-005 는 KRX GUI 경로 1차 운영 가능 상태 도달로 mitigation 보강(R-AUTO-005 row 갱신은 risk-register.md 참조).

실제 secret value / KRX 로그인 password / account-id / 실제 ARN / instance-id 본 문서 평문 기록 0건. KRX 로그인 ID / password 는 "Secrets Manager 에서 주입" 으로만 표기.

결정 외 운영 결과 기록은 [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-12 섹션 참조.


### Change Log Details 2026-06-13 (1차) <a id="change-log-details-2026-06-13-1"></a>

변경 요약: OD-MS-013 / OD-MS-014 추가 + OD-MS-009 보강 (04 spec Strategy Decision ECS / Fargate 1차 검증 반영)

2026-06-13 운영자가 직접 수행한 `port_strategy_decision` 의 ECS / Fargate 1차 포팅 검증 결과를 반영.

신규 결정: OD-MS-013(port_strategy_decision Task Definition 분리 정책 = 기존 Daily Batch 구조 계승, buy-signal / position-signal 별도 Task Definition 2개 분리, family `portfolio-paper-strategy-decision-buy-signal` / `portfolio-paper-strategy-decision-position-signal`, 🟡 잠정),
OD-MS-014(port_strategy_common 1차 배포 방식 = 1차 ECS smoke image 에서는 vendoring, 정식 package / version 관리는 후속, 🟡 잠정).

OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) 본문은 변경하지 않고 OD-MS-013 으로 분리 — Step Functions / EventBridge Scheduler 연계는 본 일자 작업 범위 밖이며 04 spec 후속 phase 책임.

OD-NET-004(crawler / preprocessor outbound 방식 = public subnet + assignPublicIp) 가 본 일자 strategy-decision RunTask(public subnet + `assignPublicIp = ENABLED`)에서도 1차 적용되었음. 결정값(선택지 / 선택값 / 비용 영향 / 운영 리스크 / 후속 spec 영향) 변경 없음.

OD-DB-007(legacy schema 모든 app role 미부여) / OD-DB-008(execution write 단독 권한 정책) 은 `decision_app` 의 research / execution / decision schema 권한 보정 결과로 1차 검증되었음(02 spec db-roles-and-grants 후속 정식 갱신은 후속 분리).

실제 secret value / RDS endpoint hostname / RDS password / account-id / 실제 ARN / image digest / task ARN 본 문서 평문 기록 0건. 결정 외 운영 결과 기록은 [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 섹션 참조.


### Change Log Details 2026-06-13 (2차) <a id="change-log-details-2026-06-13-2"></a>

변경 요약: OD-MS-015 추가 + OD-MS-012 보강 (08 spec SSM RunCommand 자동화 + ECS crawler smoke 1차 검증 반영)

2026-06-13 운영자가 직접 수행한 (a) Windows EC2 worker 기반 KRX GUI 의존 수집의 1차 자동화 검증과 (b) ECS crawler Task Definition revision 6 의 Selenium / Chrome / outbound smoke 검증 결과를 반영.

신규 결정: OD-MS-015(KRX GUI 의존 crawler 1차 자동화 방식 = SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1`.

SSM RunCommand 가 wrapper 를 SYSTEM Session 0 / SessionId 0 에서 직접 실행하는 방식은 KRX GUI 로그인에 부적합으로 판단되어 채택 거부, 🟡 잠정).

OD-MS-012(KRX GUI 의존 crawler 1차 운영 모드 = wrapper 기반 수동 실행) 본문은 변경하지 않고 OD-MS-015 로 자동화 진입점 1단계 분리 — EventBridge Scheduler 정기 트리거 / Step Functions hybrid orchestration 은 본 일자 작업 범위 밖이며 08 spec 후속 phase 책임.

OD-NET-004(crawler / preprocessor outbound 방식 = public subnet + assignPublicIp) 가 본 일자 ECS crawler `portfolio-paper-interest-crawler:6` Selenium Chrome smoke RunTask(public-a / public-b + `sgroup-crawler-tasks` + `assignPublicIp = ENABLED`) 에서도 1차 적용되었고 `example.com` / Naver Finance outbound 도달 통과.

결정값(선택지 / 선택값 / 비용 영향 / 운영 리스크 / 후속 spec 영향) 변경 없음. R-AUTO-005 / R-AUTO-006 / R-AUTO-007 보강 + R-AUTO-008 신규(SSM Session 0 직접 실행 부적합)는 risk-register.md 참조. 실제 secret value / KRX 로그인 password / RDS endpoint hostname / account-id / 실제 ARN / image digest / task ARN / instance-id 본 문서 평문 기록 0건.

KRX 로그인 ID / password 는 "Secrets Manager 에서 주입" 으로만 표기. 결정 외 운영 결과 기록은 [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-13 섹션 참조.


### Change Log Details 2026-06-13 (3차) <a id="change-log-details-2026-06-13-3"></a>

변경 요약: OD-MS-016 추가 + OD-ENV-006 / OD-ENV-007 / OD-ENV-008 추가 (Strategy Execution / MarketConnector 책임 분리 + Local-to-AWS Paper RDS 운영 원칙 반영)

2026-06-13 운영자가 직접 수행한 (a) Strategy Execution(`port_strategy_execution`) 의 자동 buy / sell entrypoint 책임 분리, (b) MarketConnector(`port-marketconnector`) 신규 executor `connector_strategy_order_execute.py` 추가, (c) View Daily Batch 17단계 재구성, (d) Local 개발 / AWS Paper RDS 운영 원칙 정리 결과를 반영.

신규 결정: OD-MS-016(Strategy Execution `--execute` = `READY -> REQUESTED` / MarketConnector executor `--execute` = `REQUESTED -> SUBMITTED`/`FAILED` 책임 분리. SELL `mark_position_sell_ordered()` 호출은 MarketConnector 측.

`connector_order_request_id` 는 Strategy Execution 에서 생성 / 업데이트하지 않음, 🟡 잠정), OD-ENV-006(Paper 환경 DB source of truth = AWS Paper RDS 단일. `PORT_ENVIRONMENT=paper` 이면 로컬 실행에서도 AWS Paper RDS 사용, 🟡 잠정), OD-ENV-007(Local PC → AWS Paper RDS 접속 방식 = SSM Port Forwarding 만 사용. RDS 는 Private 유지.

실제 paper 주문 실행 guard 는 `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` 조합, 🟡 잠정), OD-ENV-008(Local DB 와 AWS Paper RDS 간 동기화 미사용. `connector_order_request` / `connector_fill` / `strategy_execution_order` / `strategy_position_state` 병합 금지, 🟡 잠정).

OD-NET-009 / R-SEC-001 / R-NET-004 / OD-DB-003 본문 변경 없음. 책임 분리에 따른 정적 검증은 운영자 직접 `python -m py_compile` 통과 확인 + Java `mvnw clean compile` 통과 확인. 실제 `--execute` 실호출 0건 / KIS / broker API 호출 0건 / RDS DDL/DML 0건.

실제 secret value / 계좌번호 / RDS endpoint hostname / RDS password / KIS app key / KIS app secret / token / account-id / 실제 ARN / instance-id / EIP 본 문서 평문 기록 0건.

결정 외 운영 결과 기록은 [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-13 섹션 / [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 섹션 /
[`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS 운영 모드 정리 섹션 참조.


### Change Log Details 2026-06-13 (4차) <a id="change-log-details-2026-06-13-4"></a>

변경 요약: OD-NET-010 추가 + OD-ENV-006 / OD-ENV-007 / OD-ENV-008 1차 실증 메모 (Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 반영)

2026-06-13 운영자가 직접 수행한 SSM Port Forwarding 기반 로컬 → AWS Paper RDS 연결 1차 실증 검증 결과를 반영. 신규 결정: OD-NET-010(Local-to-AWS Paper RDS SSM Port Forwarding 표준 경유지 = `portfolio-paper-marketconnector-ec2` (instance id `i-0fce77927b7397b88`), local port `15433`, RDS target `portfolio-paper-rds:5432`, 🟡 잠정).

OD-ENV-006 / OD-ENV-007 / OD-ENV-008 본문은 변경하지 않고 본 일자 SSM tunnel + Python `psycopg2` 기반 `portfolio_admin` / `execution_app` 접속 1차 실증으로 메모 보강 — Status 는 모두 🟡 잠정 유지(aws-live cutover 시점에 🟢 확정 승격 후보).

OD-NET-009(SSM Session Manager 만 사용) / R-SEC-001 / R-NET-004(RDS Public 미허용) 본문 변경 없음 — RDS `PubliclyAccessible = False` 유지하며 SSM Port Forwarding 으로만 로컬 접속.

OD-DB-006 / OD-DB-007(`execution_app` search_path = `execution, decision, research, connector, preprocessor, interest, reference, legacy, public` / legacy USAGE 미부여) 본문 변경 없이 본 일자 SELECT 조회로 정합 확인.

신규 리스크 R-AUTO-012(SSM tunnel 의존성 — tunnel 창 종료 시 DB 접속 단절) / R-AUTO-013(로컬 psql client 미설치 또는 PATH 미등록 — Python `psycopg2` 대체 사용 가능)은 risk-register.md 참조. 실제 password / secret value / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 IAM access key id / 실제 secret ARN 본 문서 평문 기록 0건.

운영 식별자(instance id `i-0fce77927b7397b88` / `i-0ff768ea639a91355` / private IP `10.0.0.181` / `10.0.0.169` / `10.0.20.165` / local port `15433` / SSM session id `terraform-vjp3fv3nz73konetcevdzjh9de` /
RDS endpoint hostname `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com`)는 사용자 명시 정책에 따라 작업 로그 / runbook 본문에 사실로만 기록 — secret 가 아님.

AWS / RDS / IAM / Secrets Manager / SSM 변경 0건.

결정 외 운영 결과 기록 + Local-to-AWS Paper RDS SSM Port Forwarding Runbook 1차 본문은 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook 섹션 /
[`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-13 SSM Port Forwarding 표준 경유지 역할 1차 검증 섹션 /

[`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 Strategy Execution AWS 포팅 사전 검증 섹션 참조.


### Change Log Details 2026-06-13 (5차) <a id="change-log-details-2026-06-13-5"></a>

변경 요약: OD-NET-011 추가 + R-AUTO-012 / R-AUTO-013 mitigation·detection 보강 (Local-to-AWS Paper RDS SSM Port Forwarding 보강 — psql 18 client + pgAdmin4 접속 검증 반영)

2026-06-13 운영자가 직접 수행한 추가 client 2종(로컬 PostgreSQL 18 `psql.exe` + pgAdmin4) 의 AWS Paper RDS 접속 1차 실증 결과를 반영.

신규 결정: OD-NET-011(Local-to-AWS Paper RDS pgAdmin4 사용 원칙 = `localhost:15433` SSM tunnel 만 등록, RDS endpoint 직접 등록 금지, Server 등록 표준 — Name `AWS Paper RDS - portfolio` / Host `localhost` / Port `15433` / Maintenance database `portfolio` /
Username `portfolio_admin` 또는 MS 별 app role / SSL mode `Prefer`, 🟡 잠정).

OD-NET-010(SSM Port Forwarding 표준 경유지) 본문 변경 없음 — 같은 tunnel 위에서 client 종류만 확장(Python `psycopg2` / psql 18 / pgAdmin4 3종 모두 통과). OD-ENV-006 / OD-ENV-007 / OD-ENV-008 본문 변경 없이 다중 client 1차 실증 메모 보강 — Status 모두 🟡 잠정 유지.

OD-NET-009 / R-SEC-001 / R-NET-004(RDS Public 미허용 / `PubliclyAccessible = False` 유지) 본문 변경 없음. 보강 리스크: R-AUTO-012 detection 에 pgAdmin4 연결 단절 패턴 추가(tunnel 종료 시 pgAdmin4 connection 즉시 단절).

R-AUTO-013 mitigation 보강 — 운영자 로컬 PC 의 PostgreSQL 18 client 가 `C:\Program Files\PostgreSQL\18\bin\psql.exe` 에 설치되어 있어 full path 직접 실행으로 작업 진행 가능, 일반 `psql` PATH 등록은 후속 분리.

psql client `18.1` / server `18.4` / SSL `TLSv1.3` / `inet_server_addr = 10.0.20.165` / `inet_server_port = 5432` 출력으로 client major 18 / full 18.4 정책(R-DATA-003 / 03 spec design §5) 정합 확인.

pgAdmin4 검증에서 `current_user = portfolio_admin` / `current_database = portfolio` / `inet_server_addr = 10.0.20.165` / `inet_server_port = 5432` / `search_path = "$user", public` 출력 확인 — `portfolio_admin` 은 본 일자 시점에 `ALTER ROLE ... SET search_path` 적용 대상 아님(OD-DB-006 정합).

pgAdmin4 에서 connector / decision / execution / interest / ops / preprocessor 6개 schema 의 핵심 table 16개 SELECT 가능 확인 — INSERT / UPDATE / DELETE / DDL 0건. 실제 password / secret value / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 IAM access key id / 실제 secret ARN 본 문서 평문 기록 0건.

운영 식별자(앞 항목의 instance id / private IP / local port / SSM session id / RDS endpoint hostname) 그대로 재사용. 추가 운영 식별자: 로컬 PostgreSQL 설치 경로(`C:\Program Files\PostgreSQL\18\bin\psql.exe`) — 운영자 로컬 PC 도구 경로이며 secret 가 아님. AWS / RDS / IAM / Secrets Manager / SSM 변경 0건.

결정 외 운영 결과 기록은 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 보강(psql 18 client + pgAdmin4 접속 검증) 섹션 /
[`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-13 SSM Port Forwarding 표준 경유지 역할 — psql 18 + pgAdmin4 보강 섹션 /

[`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 Strategy Execution AWS 포팅 사전 검증 — psql 18 + pgAdmin4 보강 섹션 참조.


### Change Log Details 2026-06-13 (6차) <a id="change-log-details-2026-06-13-6"></a>

변경 요약: OD-MS-017 추가 + OD-MS-007 / OD-MS-009 / OD-SAFE-004 1차 실증 메모 (Strategy Execution ECS / Fargate 1차 포팅 검증 반영)

2026-06-13 운영자가 직접 수행한 `port_strategy_execution` 의 ECS / Fargate 본 phase 1차 포팅 검증 결과를 반영. 신규 결정: OD-MS-017(port_strategy_execution Task Definition 운영 방식 = 단일 Task Definition + command override.

family `portfolio-paper-strategy-execution` / container name `strategy-execution` / revision 1 ACTIVE / awsvpc / Fargate / cpu 512 / memory 1024.

7개 entrypoint(`daily_buy_execution_run` / `daily_sell_execution_run` / `daily_auto_buy_execute_run` / `daily_auto_sell_execute_run` / `execution_sync_buy_fill` / `execution_sync_sell_fill` / `execution_sync_buy_position`) 는 RunTask `--overrides` 의 `containerOverrides[].command` 로 매핑.

MarketConnector executor `connector_strategy_order_execute.py --execute` 는 본 Task Definition 에 포함하지 않음 — 03 spec EC2 측 별도 책임, 🟡 잠정).

OD-MS-007(`port_strategy_execution` 컴퓨트 = ECS Fargate Task + Step Functions + EventBridge Scheduler) / OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) / OD-SAFE-004(자동 재시도 금지 step) 본문은 변경하지 않고 본 일자 RunTask 1차 검증으로 1차 실증 메모만 보강 — Status 기존 값 그대로 유지.

OD-MS-013(Strategy Decision Task Definition 2개 분리) 와는 의도적으로 다른 패턴(7개 entrypoint 가 같은 image / role / secret / log group / cpu / memory 공유). 보강 리스크 R-AUTO-001 mitigation·detection 보강(state machine definition review checklist + Step Functions state 별 Retry 없음 검증 + command override script명 검증).

신규 R-AUTO-014(단일 Task Definition + command override 오매핑 위험, mitigation = state ↔ command 1:1 표 + allowlist + 자동 retry 금지 + 임의 command 입력 금지, Status `Open`). RunTask 8종(기본 `py_compile` / 3종 sync / 2종 build / 2종 auto execute `--execute`) 모두 exitCode 0 / lastStatus STOPPED.

sync 3종은 NO_TARGET 정상 종료(submitted / synced / skipped 모두 0). build 2종 + auto execute `--execute` 2종 4건은 한국 영업일 아님 / WEEKEND guard 차단 / NO_TARGET / `connector_order_request` 생성 0건 / `READY -> REQUESTED` 실제 전환 0건 / broker / KIS 주문 호출 0건.

live 자동 주문은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 실제 secret value / RDS endpoint hostname / RDS password / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 ARN / image digest / IAM access key id / task ARN 본 문서 평문 기록 0건.

AWS / Docker / ECR / ECS / IAM / Secrets Manager / RDS / GRANT 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(운영자가 직접 작성한 `port_strategy_execution` Dockerfile / requirements.txt 는 본 spec operation-notes 에 사실로만 기록 / 본문 전체 인용 0건).

결정 외 운영 결과 기록은 [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 Strategy Execution ECS / Fargate 1차 포팅 검증 섹션 참조.


### Change Log Details 2026-06-13 (7차) <a id="change-log-details-2026-06-13-7"></a>

변경 요약: OD-MS-018 추가 + OD-MS-008 1차 실증 메모 (Strategy Research Batch image 1차 준비 반영)

2026-06-13 운영자가 직접 수행한 `port_strategy_research` 의 AWS Batch 용 Docker / ECR 1차 준비 결과를 반영.

신규 결정: OD-MS-018(port_strategy_research Batch image dependency boundary = Research 내부 adapter 로 이관 / Batch image 포함 대상 `port_strategy_research` + `port_strategy_common` / `port_strategy_decision` 은 image 에 포함하지 않음 /
Research 내부 adapter 3개 신규 생성(`research_backtest_market_adapter.py` / `research_backtest_filter_adapter.py` /

`research_backtest_sizing_adapter.py`) 으로 기존 `port_strategy_decision.backtest_market` / `backtest_filter` / `backtest_sizing` 직접 import 제거 / `backtest_engine.py` / `backtest_buy_logic.py` / `block_exception_buy_engine_run.py` import 변경 적용 /
adapter 는 `port_strategy_common` 호출만 수행 자체 판단 로직 미포함 / 장기적으로 adapter 를 `port_strategy_common` 정식 package 로 이동하는 후보 유지, 🟡 잠정).

OD-MS-008(`port_strategy_research` 컴퓨트 = AWS Batch 1순위 / ECS Fargate Task 2순위 / Lambda 비권고) 본문 변경 없음 — Strategy Research 의 최종 컴퓨트 1순위는 AWS Batch 유지 / ECS Fargate Task 는 2순위 / 본 일자 작업은 AWS Batch 용 container image 준비 + ECR push 1차 검증 / Status 기존 값 그대로 유지.

AWS Batch Compute Environment / Job Queue / Job Definition / SubmitJob / CloudWatch Log Group(Research 용) / Secrets Manager(`/portfolio/paper/rds/research-app`) / IAM Role(execution / job) 모두 본 일자 미생성 — 후속 분리.

보강 리스크 R-NET-002(NAT-free 외부 outbound) / R-NET-003(VPC Endpoint 누락 시 ECR pull / Secrets / Logs 실패) / R-DATA-005(`research_app` schema 권한·search_path 검증) 모두 09 spec 후속 phase 입력으로 메모 보강.

신규 R-AUTO-015(Strategy Research heavy backtest / report job 실수 full 실행 시 Batch 비용·장시간 점유 위험, mitigation = `smoke` / `full` job name prefix + allowlist + Batch Job timeout / vCPU / memory 상한 + 최초 SubmitJob 은 no-op / import smoke 만 허용, Status `Open`).

신규 R-DATA-008(Research 내부 adapter 가 `port_strategy_common` 계약 변경을 따라가지 못해 backtest 결과가 Daily Decision 과 불일치할 위험, mitigation = adapter 는 common 호출만 수행 자체 로직 미포함 + common config / result 계약 변경 시 Research py_compile / import smoke /
small sample test 필수 + 장기적으로 adapter 를 common package 로 정식 이동, Status `Open`).

local docker build(`portfolio-strategy-research:paper-20260613`) + container `py_compile` 실행 / import smoke(`port_strategy_research.db_config` / `backtest_engine` / `backtest_buy_logic` / `block_exception_buy_engine_run`) 통과 /
Docker image MS 경계 1차 검증(`/app/port_strategy_decision` 부재) / ECR repository `portfolio-strategy-research` 신규 생성 + push(`paper-20260613` / `paper-latest`) / image size 약 90MB.

full backtest / 장시간 research / report 생성 / extended analysis / block 계열 backtest 0건 / RDS DDL · DML 0건 / broker · KIS 호출 0건 / AWS Batch SubmitJob 0건 / `secretsmanager:GetSecretValue` 실호출 0건.

실제 secret value / RDS endpoint hostname / RDS password / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 ARN / image digest / IAM access key id 본 문서 평문 기록 0건.

운영자가 직접 작성한 `port_strategy_research` Dockerfile / requirements.txt / Research 내부 adapter 3개 / import 변경 3개 파일은 본 spec operation-notes 에 사실로만 기록 / 본문 전체 인용 0건 — 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 본 일자 작업으로 인한 변경 0건.

결정 외 운영 결과 기록은 [`../09-strategy-research-batch/operation-notes.md`](../09-strategy-research-batch/operation-notes.md) 2026-06-13 Strategy Research Batch image 1차 준비 섹션 참조.


### Change Log Details 2026-06-15 (1차) <a id="change-log-details-2026-06-15-1"></a>

변경 요약: OD-MS-019 추가 + OD-MS-008 / OD-MS-018 1차 실증 메모 (Strategy Research AWS Batch 실행 검증 완료)

2026-06-15 운영자가 직접 수행한 (a) `port_strategy_research` AWS Batch 실행 골격 + smoke SubmitJob 1차 검증, (b) BACKTEST_RESEARCH full + BACKTEST_REPORT 본 phase 검증, (c) BACKTEST_REPORT S3 업로드 보강 결과를 반영.

신규 결정: OD-MS-019(View Daily Batch 기준 AWS Batch 포팅 대상 = `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종 한정 / `run_extended_analysis.py` 는 BACKTEST_RESEARCH 내부에서 이미 수행되어 별도 AWS Batch 포팅 대상 제외 + 수동 보조 도구 분류 / `block_watch_*` / `block_exception_buy_*` 4종은 AWS Batch 포팅 대상 제외 + heavy 분류 후속 /

Research report artifact 보존 = 기존 S3 bucket `portfolio-paper-migration-yukiever` 재사용 + prefix `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/` /
Job Role 의 `s3:PutObject` Resource 는 `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` 한정 + public read 0건 + wildcard 0건, 🟡 잠정).

OD-MS-008(Research 컴퓨트 1순위 = AWS Batch / ECS Fargate Task 2순위 / Lambda 비권고) 본문 변경 없음 — Strategy Research AWS Batch 1순위 1차 실증 메모만 보강(BACKTEST_RESEARCH full `SUCCEEDED` / `run_id a39b0b0c-cfe9-474e-8a4a-4ddb33f09567` / `total_return 4.55930879` /
`mdd -0.08941942` / `sharpe 2.65561307` / `trade_count 308` / extended analysis 내부 수행 포함).

OD-MS-018(Research Batch image dependency boundary = Research 내부 adapter 로 이관 / image 포함 = `port_strategy_research` + `port_strategy_common` / `port_strategy_decision` 미포함) 본문 변경 없음 — image rebuild(`paper-20260615-report-s3` /
image digest sha256 placeholder 사용 / image size 약 106MB / pushedAt 2026-06-15T17:00:10+09:00) 시점에도 동일 정책 유지.

AWS Batch Compute Environment `portfolio-paper-strategy-research-ce`(MANAGED / FARGATE / maxvCpus 4) / Job Queue `portfolio-paper-strategy-research-queue`(priority 10) /
Job Definition revision 1 `portfolio-paper-strategy-research:1`(image `paper-latest` / vCPU 1 / memory 2048 / timeout 600초 / FARGATE / assignPublicIp ENABLED) / revision 3(image `paper-20260615-report-s3` /

TaskRole `portfolio-paper-research-job-role`) / CloudWatch Log Group `/portfolio/paper/strategy-research`(retention 14일) / Secrets Manager `/portfolio/paper/rds/research-app`(JSON multi-key 5종 / secret value 노출 0건) /
Execution Role `portfolio-paper-research-batch-execution-role`(`AmazonECSTaskExecutionRolePolicy` + research-app secret read inline policy / secret ARN 한정 / wildcard 0건) /

Job Role `portfolio-paper-research-job-role`(최초 smoke 단계 최소 권한 + 이후 prefix 한정 `s3:PutObject` 추가) 모두 신규 생성 — 운영자 직접 수행.

py_compile smoke(`smoke-strategy-research-import-20260615` / jobId `81ec3581-...`) + DB smoke(`smoke-strategy-research-db-20260615` / jobId `5399aa10-...`) + S3 업로드 검증(`strategy-research-backtest-report-s3-20260615` / jobId `112f5fe4-...` /
logStreamName `strategy-research/default/b18e548d46764cd791028088e1d32a6d`) 모두 `SUCCEEDED` / exitCode 0.

S3 객체 4건 존재 확인(prefix `strategy-research/reports/20260615/112f5fe4-.../` / `01_요약 리포트` ~ `04_추천 리포트` / private 유지 / public read 0건).

Strategy Common 1차 정합성 확인 완료(common 주요 모듈 + Research adapter 3개 py_compile + 각 MS common import smoke / 표현은 "Strategy Common 1차 정합성 확인 완료 / 정식 package 관리는 후속") — OD-MS-014 본문 변경 없이 vendoring 유지 정합.

보강 / 신규 리스크: R-AUTO-015 mitigation·detection 보강 + Status `Open` → `Mitigated` 승격(View Daily Batch SubmitJob 연동 / Step Functions orchestration 전까지 완전 Closed 가 아닌 Mitigated 유지). R-DATA-008 detection 보강(Strategy Common 1차 정합성 확인 절차 + smoke / sample 비교 절차 후속 문서화).

신규 R-COST-003(Research S3 report 누적 비용 / lifecycle 미설정 위험, mitigation = OD-MS-019 prefix 한정 + Job Role Resource 한정 + public read 0건 + S3 lifecycle 정책 후속 결정 + 06 spec KMS encryption 결정, Status `Open`).

RDS DDL / DML 0건 / broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 0건 / heavy 분류 entrypoint(`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) SubmitJob 0건 / AWS Batch automatic retry 0건(OD-SAFE-004 / R-AUTO-001 / R-AUTO-015 정합).

live 자동 batch / report 생성은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 실제 secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 secret ARN / 실제 IAM Role ARN / image digest full sha256 / IAM access key id 본 문서 평문 기록 0건.

운영자가 직접 작성 / 수정한 `port_strategy_research` requirements.txt(boto3 추가) / report wrapper 변경분(있는 경우) / Dockerfile 변경분(있는 경우) 은 09 spec operation-notes 에 사실로만 기록 / 본문 전체 인용 0건. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 본 일자 작업으로 인한 변경 0건.

결정 외 운영 결과 기록은 [`../09-strategy-research-batch/operation-notes.md`](../09-strategy-research-batch/operation-notes.md) 2026-06-15 3개 섹션 참조.


### Change Log Details 2026-06-15 (2차) <a id="change-log-details-2026-06-15-2"></a>

변경 요약: OD-MS-020 / OD-MS-021 추가 + OD-SEC-006 1차 실증 메모 (Backend AWS E2E dry-run 1차 + Interest Crawler 상태 재판정)

2026-06-15 운영자가 직접 수행한 (a) AWS 계정 / region / EC2 / ECS / AWS Batch 사전 점검, (b) MarketConnector EC2 기반 `CONNECTOR_BALANCE` 1차 실행, (c) Windows EC2 worker 기반 KRX worker 재실행 + KRX raw 최신일 점검, (d) non-GUI raw 최신일 SQL 점검, (e) preprocessor ECS RunTask 단발 실행 + DB `updated_at` 갱신 확인 결과를 반영.

같은 일자 첫 번째 세션(Strategy Research AWS Batch 골격 + full / report + S3 업로드) 후속이며, 본 항목은 두 번째 세션의 결정 락이다. 신규 결정: OD-MS-020(port-interest-crawler 상태 표현 / 완료 정의 = Interest Crawler hybrid 1차 구현 부분 완료.

보정 표현 = "Interest Crawler hybrid 1차 구현: 부분 완료" / "KRX GUI worker 는 운영 가능 상태로 1차 완성"(KRX program / KRX shortsell 직전 거래일까지 적재) / "ECS · Fargate crawler 는 smoke 검증 완료"(2026-06-13 `portfolio-paper-interest-crawler:6` Selenium Chrome smoke 통과) /
"non-GUI daily raw 수집 운영 경로와 raw 전체 최신성 검증은 후속"(non-GUI raw 7종 직전 거래일까지 미적재).

원칙 = "완료" 표기는 실제 데이터 적재 / 최신성 검증까지 확인된 경우에만 사용 / KRX worker 완료와 Interest Crawler 전체 완료 혼동 금지 /

smoke 성공과 daily raw 최신성 성공 분리, 🟡 잠정).

OD-MS-021(Backend AWS E2E dry-run 17단계 순서 + 안전 기준):

- (1) `CONNECTOR_BALANCE` (2) `INTEREST_CRAWLER` (3) `PREPROCESSOR` (4) `BACKTEST_RESEARCH` (5) `BACKTEST_REPORT`.
- (6) `DAILY_BUY_SIGNAL` (7) `DAILY_POSITION_SIGNAL` (8) `DAILY_BUY_EXECUTION` (9) `DAILY_SELL_EXECUTION`.
- (10) `DAILY_AUTO_SELL` (11) `DAILY_AUTO_BUY` (12) `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` (13) `CONNECTOR_ORDER_CHECK`.
- (14) `SYNC_SELL_FILL` (15) `SYNC_BUY_FILL` (16) `SYNC_BUY_POSITION` (17) `BALANCE_REFRESH`.

안전 기준 = BUY · SELL 실행 금지 / `--execute` 주문 전송 계열 실행 금지 / fill · position sync 자동 재시도 금지 / aws-live 작업 금지 / Research 는 Decision 보다 먼저 실행 / Connector Balance 1번 위치 유지 / Balance Refresh 17번 위치 유지 / Execution · MarketConnector 주문 계열은 dry-run 또는 skip 기준으로만 문서화, 🟡 잠정).

OD-SEC-006(EC2 / ECS IAM Role 기반 secret / parameter read 최소 권한) 본문 변경 없음 — 본 일자 MarketConnector EC2 role(`portfolio-paper-marketconnector-ec2-role`)에서 `/portfolio/paper/rds/preprocessor-app` `GetSecretValue` 시도 시 `AccessDeniedException` 1차 실증으로 메모만 보강(MS 별
Secret 접근 분리가 정상 동작 / 권한 추가 0건 / preprocessor DB 확인은 preprocessor ECS Task 또는 운영자 로컬 SSM Port Forwarding 으로만 수행 / Status 🟡 잠정 유지).

OD-DB-008(marketconnector_app execution R-only) 본문 변경 없음. OD-MS-009(Daily Batch orchestration) / OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없음 — OD-MS-021 은 Step Functions / EventBridge Scheduler 정기 트리거 도입 전 단계의 dry-run 진행 순서 + 안전 기준만 별도 락.

OD-MS-011(Hybrid execution model) / OD-MS-012(KRX GUI 의존 crawler 1차 운영 모드) / OD-MS-015(SSM RunCommand → Scheduled Task trigger) 본문 변경 없음 — OD-MS-020 은 표현 / 완료 정의만 별도 락.

보강 / 신규 리스크: R-DATA-009 신규(smoke 검증을 daily 데이터 최신성 완료로 오해할 위험 — 운영 문서 / 보고 / 슬라이드의 "Interest Crawler 완성: 완료" 잔존 grep 정기 점검, Status `Open`).

R-DATA-010 신규(raw 최신성 부족으로 downstream Research / Decision 결과가 stale data 기반이 될 위험 — `interest_*_raw` 의 `MAX(trade_date)` 와 직전 거래일 비교 SQL 정기 점검 + preprocessor 재실행 후 신규 feature date 생성 여부 비교, Status `Open`).

본 일자 17단계 진행 상태 — 1번 `CONNECTOR_BALANCE` 완료 / 2번 `INTEREST_CRAWLER` 부분 완료 / 3번 `PREPROCESSOR` 실행 완료(데이터 최신성 제약) / 4 ~ 7번 미진행 / 8 ~ 17번 미진행 또는 dry-run skip 예정. 실제 BUY / SELL / `--execute` 주문 전송 0건 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건.

KIS Secrets JSON key parsing 정정(`APP_KEY` → `KIS_APP_KEY` / `APP_SECRET` → `KIS_APP_SECRET` / `PAPER_ACNT` → `KIS_PAPER_ACNT` / `ACNT_PRDT_CD` → `KIS_ACNT_PRDT_CD`) 후 KIS balance API status 200 / 모의투자 잔고 조회 성공 / `connector_balance_snapshot` 저장 / 보유종목 0건 처리.

KRX raw 최신일 = `interest_program_raw` 2026-06-12 / `interest_shortsell_raw` 2026-06-12.

non-GUI raw 7종 최신일 = `interest_agency_raw` 2026-06-11 / `interest_news_raw` 2026-06-11 / `interest_commodity_raw` · `interest_foreignindex_raw` · `interest_investorflow_raw` · `interest_marketbreadth_raw` · `interest_price_raw` 모두 2026-06-08 /
`interest_ticker_value_raw` 2026-03-09(dry-run 핵심 차단 요인 제외).

preprocessor ECS RunTask exitCode 0 / DB `updated_at` 2026-06-15 11:03:55+00(KST 2026-06-15 20:03:55) / 신규 2026-06-15 feature date 0건.

실제 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 secret ARN / 실제 IAM Role ARN / image digest full sha256 / IAM access key id / instance-id / task ARN 본 문서 평문 기록 0건.

운영자가 직접 수행한 KIS Secrets 환경변수 mapping / SSM RunCommand / ECS RunTask / SQL 점검 결과는 08 spec operation-notes 2026-06-15 §1 ~ §8 에 사실로만 기록 / 본문 전체 인용 0건. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건.

결정 외 운영 결과 기록은 [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-15 §1 ~ §8 참조.


### Change Log Details 2026-06-16 (1차) <a id="change-log-details-2026-06-16-1"></a>

변경 요약: OD-MS-022 추가 + OD-MS-011 / OD-MS-015 / OD-MS-020 1차 실증 메모 (08 spec Crawler 데이터 미수집 해결 + KRX EC2 자동화 성공 반영)

2026-06-16 운영자가 직접 수행한 (a) Crawler 데이터 미수집 원인 진단(rev6 = Selenium / Chrome smoke command / 원본 `interest_crawler_daily.py` KRX GUI 단계 포함 / non-GUI orchestration 부재),
(b) `interest_crawler_daily_nongui.py` 신규 생성 + `paper-20260616-nongui` 빌드 + ECR push + Task Definition `portfolio-paper-interest-crawler:7`(FARGATE / awsvpc / cpu 1024 / memory 2048 / log group `/portfolio/paper/crawler` /

log stream prefix `ecs-crawler-nongui-daily`) 등록 + RunTask exitCode 0 / 약 9분 51초 / 전체 step SUCCESS(`interest_news` / `interest_agency` / `interest_foreignindex` / `interest_commodity` / `interest_macroeconomic` / `interest_price` / `interest_investorflow` /
`interest_marketbreadth`), (c) raw 최신성 회복(price / investorflow / marketbreadth / commodity / foreignindex / macro 2026-06-15 / news /

agency 2026-06-16 /

`interest_foreignindex_raw` HANGSENG · NIKKEI225 · SHANGHAI 일부 NULL Data 는 본 일자 Preprocessor blocker 가 아닌 non-blocker 후보로 분리).

(d) Windows EC2 worker Autologon bootstrap + EC2 재부팅 후 SSM Online + `query user` Administrator console session Active 확인 + SSM RunCommand → `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"` → Windows Scheduled Task → Administrator console interactive session →
`powershell.exe -ExecutionPolicy Bypass -File C:\portfolio\run_krx_worker_daily.ps1` → KRX login(elapsed 92.83s) /

`interest_program` 2026-06-15 / `interest_shortsell` 2026-06-15 349 Company / wrapper `DONE :: KRX worker daily` / Last Result 0 / Last Run Time 2026-06-16 04:55:49 / `interest_program_raw` 547 → 548 / `interest_shortsell_raw` 190,554 → 190,903 결과를 반영.

신규 결정: OD-MS-022(KRX GUI crawler 자동 로그인 기반 운영 방식 = Windows Autologon + Administrator interactive session + Scheduled Task + SSM trigger / SSM RunCommand 가 wrapper · Python 을 SYSTEM Session 0 에서 직접 실행하는 방식은 KRX GUI 로그인에 부적합 /
Headless · 비대화형 KRX 수집은 로컬 검증상 운영 방식에서 제외 / Autologon 은 paper 전용 Windows worker 보안 예외, 🟡 잠정).

OD-MS-011(port-interest-crawler runtime 분리 Hybrid execution model) / OD-MS-015(KRX GUI 의존 crawler 1차 자동화 방식 = SSM RunCommand → `schtasks /Run` → Scheduled Task) / OD-MS-020(Interest Crawler 상태 표현 / 완료 정의) 본문 변경 없이 1차 실증 메모만 보강 — Status 모두 🟡 잠정 유지.

표현 보정(OD-MS-020 정합) — 이전 "hybrid 1차 구현 부분 완료" 표현은 "hybrid 구조 완료(non-GUI rev7 운영 경로 생성 + RunTask 성공 + raw 최신성 회복 + KRX GUI = Windows EC2 worker + Autologon + Scheduled Task + SSM trigger)" 로 갱신.

Preprocessor MS = ECS Fargate Task 유지 / "raw 입력 데이터 회복 후 ECS 재실행 가능 상태 도달" 까지만 기록("완료" 표기는 사용하지 않음 / OD-MS-020 정합). "완료" 표기 원칙은 유지 — 실제 데이터 적재 / 최신성 검증까지 확인된 경우에만 사용.

보강 / 신규 리스크: R-SEC-009 신규(Windows Autologon 보안 예외, Status `Open`), R-AUTO-016 신규(Administrator interactive session 부재 시 KRX GUI 수집 실패, Status `Open`), R-AUTO-017 신규(Chrome process 잔존, Status `Open`).

R-AUTO-008 detection·mitigation 보강(Autologon bootstrap + Administrator console session Active 확인 + Scheduled Task trigger 흐름이 본 일자 1차 실증). R-DATA-009 / R-DATA-010 mitigation 보강(2026-06-16 raw 최신성 회복 1차 실증).

실제 secret value / Administrator password / KRX 로그인 password / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 secret ARN / 실제 IAM Role ARN / image digest full sha256 / IAM access key id / instance-id / task ARN 본 문서 평문 기록 0건.

AWS / SSM / EC2 / ECS / ECR / Docker / IAM / Secrets Manager / RDS / GRANT 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 0건 / `--execute` 0건 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건.

운영자가 직접 작성 / 수정한 `interest_crawler_daily_nongui.py` / Dockerfile / requirements.txt 변경분은 08 spec operation-notes 에 사실로만 기록(본문 전체 인용 0건) — 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 본 일자 작업으로 인한 변경 0건.

결정 외 운영 결과 기록은 [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-16 §1 ~ §5 참조.


### Change Log Details 2026-06-16 (2차) <a id="change-log-details-2026-06-16-2"></a>

변경 요약: OD-MS-008 / OD-MS-013 / OD-MS-019 / OD-MS-021 1차 실증 메모 (Backend AWS E2E dry-run safe subset 재개)

2026-06-16 같은 일자 첫 번째 세션(Crawler 데이터 미수집 해결 + KRX EC2 자동화 성공) 후속으로 운영자가 직접 수행한 (a) BACKTEST_RESEARCH AWS Batch 단건 재실행(`portfolio-paper-strategy-research:5` `SUCCEEDED` / exitCode 0 / run_id `439d78e7-41fd-4bb7-b455-18564ddff758` /
backtest_end_date `2026-06-15` / `strategy_trade_log` 310 / `strategy_backtest_daily` 822 / `strategy_backtest_daily_position` 2375 / total_return `4.66534417` /

mdd `-0.08941942` / sharpe `2.68071466` / trade_count `310`),
(b) BACKTEST_REPORT 정식 Job Definition rev1 ~ rev3 교정(rev1 `python -m port_strategy_research.backtest_report_run` local-only / rev2 `python aws_batch_backtest_report_wrapper.py` 컨테이너 내 파일 경로 부재로 실패 /
rev3 `python -m port_strategy_research.aws_batch_backtest_report_wrapper` module 호출 방식으로 최종 확정 / image `paper-20260615-report-s3` /

env `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` / `REPORT_S3_BUCKET=portfolio-paper-migration-yukiever` /
`REPORT_S3_PREFIX=strategy-research/reports`) + `portfolio-paper-strategy-report:3` SubmitJob `SUCCEEDED` + S3 prefix `strategy-research/reports/20260616/67522706-9b5f-4770-a312-ceb1987c4655/` 안 4개 객체 존재 확인(`01_요약_리포트` 7,258 bytes /
`02_일자별_매매_리포트` 552,540 bytes /

`03_거래_상세_리포트` 329,088 bytes / `04_추천_리포트` 11,847 bytes / private 유지 / public read 부여 0건),
(c) DAILY_BUY_SIGNAL 단건 ECS / Fargate 재실행(`portfolio-paper-strategy-decision-buy-signal:1` exitCode 0 / image `paper-20260613` / `decision.strategy_daily_signal` row 4건 `READY` / signal_date `2026-06-16` / data_date `2026-06-15` / rank `1 ~ 4`),
(d) DAILY_POSITION_SIGNAL 단건 ECS /

Fargate 재실행(`portfolio-paper-strategy-decision-position-signal:1` exitCode 0 / daily_run_id `45` / market_signal `AGGRESSIVE` / positions 0 정상 skip / `decision.strategy_daily_position_decision` 신규 row 미생성 — 보유 포지션 0건 정상 케이스 / max_decision_date `2026-05-29` 유지) 결과를 반영.

신규 결정 0건.

OD-MS-008(`port_strategy_research` 컴퓨트 1순위 = AWS Batch / ECS Fargate Task 2순위 / Lambda 비권고) 본문 변경 없음 — Strategy Research AWS Batch 1순위 1차 실증 메모만 보강(BACKTEST_RESEARCH `portfolio-paper-strategy-research:5` `SUCCEEDED` + BACKTEST_REPORT
`portfolio-paper-strategy-report:3` `SUCCEEDED` + S3 4개 객체 존재 확인 + Status 기존 값 그대로 유지).

OD-MS-013(`port_strategy_decision` Task Definition 분리 정책 = buy-signal / position-signal 별도 Task Definition 2개 분리) 본문 변경 없음 — 본 일자 두 번째 세션의 ECS / Fargate 재실행 통과로 1차 실증 메모만 보강(buy-signal exitCode 0 + signal row 4건 / position-signal exitCode 0 + positions 0 정상 skip + Status 기존 값 그대로 유지).

OD-MS-019(View Daily Batch 기준 AWS Batch 포팅 대상 = `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종 한정 / report artifact S3 prefix `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/`) 본문 변경 없음 — 정식 Job Definition `portfolio-paper-strategy-report:3` 단건 SubmitJob
통과 + S3 4개 객체 존재 확인으로 1차 실증 메모만 보강 / heavy 분류(`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) SubmitJob 0건 유지.

OD-MS-021 (Backend AWS E2E dry-run 17단계 순서 + 안전 기준) 본문 변경 없음:

- 본 일자 두 번째 세션 결과로 safe subset 1 ~ 7번 모두 완료: `CONNECTOR_BALANCE` / `INTEREST_CRAWLER` / `PREPROCESSOR` / `BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL`
- 8 ~ 17번 미진행 또는 dry-run skip 예정
- 안전 기준 위반 0건: BUY · SELL 실행 금지 / `--execute` 주문 전송 계열 실행 금지 / fill · position sync 자동 재시도 금지 / aws-live 작업 금지 / Research 는 Decision 보다 먼저 실행 / Connector Balance 1번 / Balance Refresh 17번 위치 유지
- Status 기존 값 그대로 유지

OD-MS-016(Strategy Execution / MarketConnector 책임 분리 = Strategy Execution `READY -> REQUESTED` / MarketConnector `REQUESTED -> SUBMITTED`/`FAILED`) / OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없음 — 본 일자 두 번째 세션은 safe subset 한정 / Execution 계열 단계 진입 0건 / `--execute` 실호출 0건. 보강 / 신규 리스크: 신규 R 0건.

R-AUTO-015 detection 보강(BACKTEST_REPORT 정식 Job Definition 의 wrapper command 경로 불일치 사례 — rev1 local-only / rev2 wrapper 파일 경로 부재 / rev3 module 호출 방식 확정 / Job Definition 등록 직후 SubmitJob 1회 단건 실행으로 wrapper 경로 정상 동작 여부 사전 검증 / 실패 시 즉시 다음 revision 으로 교정 /
운영 사용 금지) — Status `Mitigated` 유지 / risk-register.md R-AUTO-015 detection 컬럼 정합.

R-AUTO-001 / R-AUTO-002(자동 재시도 / live 자동매매 조기 활성화) 본문 변경 없음 — 본 일자 두 번째 세션은 safe subset 한정으로 안전 기준 위반 0건 / Status 기존 값 그대로 유지.

R-DATA-009 / R-DATA-010(stale raw data 위험) 본문 변경 없음 — 본 일자 두 번째 세션 시점에 BACKTEST_RESEARCH backtest_end_date `2026-06-15` / DAILY_BUY_SIGNAL data_date `2026-06-15` 정합으로 stale data 위험 1차 해소 1차 실증(첫 번째 세션의 raw 최신성 회복 정합).

DAILY_POSITION_SIGNAL positions 0 정상 skip 케이스는 리스크가 아닌 정상 운영 케이스로 04 spec operation-notes 2026-06-16 §2 / §4 에만 사실 기록.

실제 secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 IAM Role ARN / 실제 secret ARN / image digest full sha256 / IAM access key id / job ARN / task ARN 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

AWS Batch / ECS / IAM / S3 / Docker / ECR / RDS 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 직접 호출 0건.

신규 BUY / SELL / 취소 / 정정 / `--execute` 0건 / fill · position sync 자동 재시도 0건 / SELL position `mark_position_sell_ordered()` 호출 0건. live 자동 BUY / SELL E2E 검증은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 두 번째 세션으로 인한 변경 0건 — 운영자가 직접 수정 / 작성한 `port_strategy_research/aws_batch_backtest_report_wrapper.py` / 관련 Dockerfile / requirements.txt 변경분은 09 spec operation-notes 2026-06-16 §2 / §3 에 사실로만 기록(본문 전체 인용 0건).

결정 외 운영 결과 기록은 [`../09-strategy-research-batch/operation-notes.md`](../09-strategy-research-batch/operation-notes.md) 2026-06-16 §1 ~ §8 / [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-16 §1 ~ §7 참조.


### Change Log Details 2026-06-17 (1차) <a id="change-log-details-2026-06-17-1"></a>

변경 요약: OD-SEC-006 / OD-MS-001 / OD-MS-009 1차 실증 메모 (03 spec MarketConnector 조회성 dry-run 재검증)

2026-06-17 운영자가 직접 수행한 (a) `CONNECTOR_BALANCE` SSM RunCommand 재검증(1차 실패 = JSON SecretString 전체를 환경변수 값으로 export 한 mapping 오류 / v5 패턴 = JSON 내부 key value 추출 + `APP_*` 호환 key + `KIS_*` alias 동시 export 로 보정 후 성공 /
`connector_balance_snapshot` 최신 row `as_of_date 2026-06-17` / `as_of_ts 2026-06-17 00:46:17 UTC` / `created_at 2026-06-17 00:46:17 UTC` / `source_api inquire-balance` /

`source_version connector-balance-1.0.0` / 보유종목 0건 정상),
(b) `CONNECTOR_ORDER_CHECK` SSM RunCommand 재검증(MarketConnector 조회계열 선행 검증 — Daily 17단계 후반 step 13 을 단건 선행 실행 / v5 패턴 재사용 / KIS `inquire-daily-ccld` `response_status=200` / `response_code=0` / `is_success=true` / `called_at 2026-06-17 00:51:03 UTC` /
row count `connector_order_request 33` / `connector_order_event 18` / `connector_fill 13` /

신규 0건은 본 일자 신규 주문·체결 미발생 정상 판단), (c) PowerShell 변수 일시 소실은 운영자 측 경미 이슈(EC2 / RDS / KIS 측 영향 없음) 결과를 반영.

신규 결정 0건.

OD-SEC-006(EC2 / ECS IAM Role 기반 secret / parameter read 최소 권한) 본문 변경 없음 — MarketConnector EC2 Instance Role 기반 Secrets Manager + SSM Parameter Store read 성공 / secret 평문 0건 / JSON SecretString 내부 key parsing 필요성 1차 실증(plain string 형태 기대 시 JSON dict 전체 export 가 mapping 오류로 이어짐) / Status 🟡 잠정 유지.

OD-MS-001(MarketConnector 컴퓨트 = EC2+EIP) /
OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) 본문 변경 없음 — MarketConnector EC2 조회성 경로 재검증 통과로 1차 실증 메모만 보강 / Status 기존 값 그대로 유지 /
`CONNECTOR_ORDER_CHECK` 는 Daily 17단계 후반 step 이지만 본 실행은 선행 단건 검증으로 표현 통일(Daily 17단계 본 실행 0건).

OD-MS-021(Backend AWS E2E dry-run 17단계 순서 + 안전 기준) 본문 변경 없음 — 본 일자는 17단계 전체 흐름이 아닌 MarketConnector 조회계열 선행 검증 / 안전 기준(BUY · SELL 실행 금지 / `--execute` 주문 전송 계열 실행 금지 / fill · position sync 자동 재시도 금지 / aws-live 작업 금지) 위반 0건 / Status 기존 값 그대로 유지.

OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없음 — 신규 BUY / SELL / 취소 / 정정 / `--execute` / fill·position sync 자동 재시도 / SELL position `mark_position_sell_ordered()` 호출 0건. OD-MS-016(Strategy Execution / MarketConnector 책임 분리) 본문 변경 없음 — `connector_strategy_order_execute.py --execute` 호출 0건. 보강 / 신규 리스크: 신규 R 0건.

R-DOCS-001(secret 평문 기록 위험) detection / mitigation 에 [2026-06-17 보강] 메모 추가 — JSON SecretString 내부 value 추출 후 환경변수 export / value 평문 출력 0건 / runbook §4.1 / §4.2 검증 SQL 기반 점검 / `connector_api_call_log` BALANCE / ORDER 의 `response_status` / `response_code` /
`is_success` 만 기록 / response body 평문 인용 0건 / `connector_balance_snapshot` 최신 row / `CONNECTOR_ORDER_CHECK` 신규 row 0건도 정상 판단 — Status 기존 값 그대로 유지.

03 spec 후속 task 26(systemd / startup script 정상 운영 모드 전환 시 v5 env mapping 패턴 반영) / task 27(`/tmp/inject-env.sh` 운영 스크립트 승격 판단) / task 28(`APP_*` 호환 key vs `KIS_*` alias 단일화 결정) 신규 추가 — 후속 phase 책임.

실제 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

JSON SecretString 내부 key parsing 사실은 mapping 사실로만 기록(value 평문 0건). raw SecretString 전체를 환경변수 값으로 export 한 1차 실패 사례는 원인 / 조치 / 결과 중심 요약. AWS / SSM / EC2 / RDS / Secrets Manager / SSM Parameter Store / KIS API 호출은 모두 운영자 직접 수행 — Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. AWS CLI 실행 0건.

AWS 리소스 생성 / 수정 / 삭제 0건. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 직접 호출 0건 / `--execute` 0건 / aws-live 작업 0건. RDS DDL 0건 / DML 은 `connector.connector_balance_snapshot` insert 1건 한정 / `connector.connector_position_snapshot` 신규 row 0건(보유종목 0건 정상).

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 본 일자 작업으로 인한 변경 0건.

운영 식별자(secret name path / SSM Parameter name path / 환경변수 key 이름 / KIS API category / api_name / response_status / response_code / is_success / DB table name / row count / `as_of_date` / `as_of_ts` / `called_at` / `source_api` / `source_version`) 는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

결정 외 운영 결과 기록은 [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-17 §1 ~ §3 참조.


### Change Log Details 2026-06-17 (2차) <a id="change-log-details-2026-06-17-2"></a>

변경 요약: OD-MS-016 / OD-MS-021 / OD-DB-008 / OD-MS-008 / OD-MS-019 1차 실증 메모 (Daily AWS 17-step E2E 완료)

2026-06-17 같은 일자 첫 번째 세션(MarketConnector 조회성 dry-run 재검증) 후속으로 운영자가 직접 수행한 Daily AWS 17-step E2E 흐름이 본 일자에 끝까지 연결됐다.

17 step 결과 — 1번 `CONNECTOR_BALANCE` 완료(첫 세션 v5 env injection 결과 그대로 사용 / `connector_balance_snapshot` 최신 row `as_of_date 2026-06-17`) /
2번 `INTEREST_CRAWLER` 완료(non-GUI ECS Fargate `portfolio-paper-interest-crawler:7` + KRX Windows EC2 worker Scheduled Task / `interest_program_raw` / `interest_shortsell_raw` 2026-06-16 적재 / crawler worker stop 요청 완료 /

KRX GUI = Windows interactive desktop session 기반 유지) / 3번 `PREPROCESSOR` 완료(ECS RunTask exitCode 0 / `PREPROCESSOR PIPELINE END` / `pre_total_market_daily_feature` / `pre_total_stock_daily_feature` 2026-06-16) /
4번 `BACKTEST_RESEARCH` 완료(AWS Batch SUCCEEDED / latest result date `2026-06-16` / Sharpe Ratio `2.68` / `research.strategy_backtest_daily` /

`research.strategy_backtest_daily_position` 2026-06-16) / 5번 `BACKTEST_REPORT` 완료(AWS Batch SUCCEEDED / S3 report 4개) /
6번 `DAILY_BUY_SIGNAL` 완료(ECS RunTask exitCode 0 / run_date `2026-06-17` / data_date `2026-06-16` / BUY READY 4건 / 후보 4종 `282330 BGF리테일` / `004990 롯데지주` / `003490 대한항공` / `088350 한화생명`) / 7번 `DAILY_POSITION_SIGNAL` 완료(positions 0 / decision_count 0 정상 skip) /

8번 `DAILY_BUY_EXECUTION` 완료(1차 blocker = `execution_app` 의 interest schema USAGE / table SELECT / sequence / default privileges 누락 → 운영자 직접 GRANT 보정 / 재실행 `execution_plan_id 92` 생성 / BUY READY 4건 / `connector_order_request_id` 4건 모두 NULL / 실제 broker /
KIS 호출 0건) / 9번 `DAILY_SELL_EXECUTION` 정상 skip(sell_decisions 0 / orders_to_upsert 0) / 10번 `DAILY_AUTO_SELL` 정상 skip(READY SELL 0건) /

11번 `DAILY_AUTO_BUY` 완료(BUY 4건 `READY -> REQUESTED` 전환 / `execution_plan_id 92` / `total_qty 378` / `total_target_amount 6908189.40` / `connector_order_request_id` 모두 NULL / 실제 broker / KIS 호출 0건 / OD-MS-016 책임 분리 정합) /
12번 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` 완료(KIS paper BUY 4건 제출 성공 / `execution_order` id `26 ~ 29` SUBMITTED / `connector_order_request` id `34 ~ 37` 생성 /

`broker_order_no 0000035906` 282330 / `0000035912` 004990 / `0000035918` 003490 / `0000035932` 088350 / 1차 시도 / 실패 원인 = `connector_strategy_order_execute.py` MarketConnector EC2 미배포 / system python `psycopg` 부재로 venv python 사용 필요 /
`execution` table UPDATE 권한 누락 운영자 직접 GRANT 보정 / `source_run_id` fallback 패치 후 정식 배포로 KIS paper BUY 4건 제출 성공) /

13번 `CONNECTOR_ORDER_CHECK` 완료(KIS `inquire-daily-ccld` 호출 성공 / `output1 empty` + `output2 aggregate summary` 응답 형태 1차 식별 / 잘못 생성된 `connector_order_event` /
`connector_fill` 삭제 + `connector_order_request` 상태 복구 + `connector_order_check.py` summary fallback guard 패치(active 후보 정확히 1건일 때만 fallback 허용 / 다건이면 event / fill / status 변경 금지) + `broker_order_no` 별 단건 조회로 체결 동기화 성공 /

`connector_order_request 34 ~ 37` FILLED / `connector_fill 26 ~ 29` 생성) / 14번 `SYNC_SELL_FILL` 정상 skip(SELL fill 0건) / 15번 `SYNC_BUY_FILL` 완료(`connector_fill 26 ~ 29` 기준 BUY fill sync / `execution_order 26 ~ 29` FILLED 전환) /
16번 `SYNC_BUY_POSITION` 완료(`strategy_position_state` 4건 OPEN 생성 / `position_state_id 6 ~ 9`) /

17번 `BALANCE_REFRESH` 완료(1차 실패 원인 = `marketconnector_app` role 의 `legacy` schema USAGE / `legacy.holdings` DML / sequence / database search_path 누락으로 bare `holdings` relation not found /
조치 = role database search_path 를 `connector, execution, legacy, reference, public` 로 보정 + `legacy` schema USAGE / `legacy.holdings` DML / sequence GRANT + default privileges 보정 / 재실행 SSM `Status Success` /

`ResponseCode 0` / `connector_position_snapshot` 4종목 최신 / `position_snapshot_id 120 ~ 123` / quantity `52 / 65 / 244 / 17` / avg_buy_price `28980.77 / 27043.08 / 5744.41 / 120182.35`).

신규 결정 0건.

OD-MS-016(Strategy Execution / MarketConnector 주문 실행 책임 분리 = Strategy Execution `READY -> REQUESTED` / MarketConnector `REQUESTED -> SUBMITTED`/`FAILED` / SELL `mark_position_sell_ordered()` 는 MarketConnector 측 /
`connector_order_request_id` 는 Strategy Execution 에서 생성 / 업데이트하지 않음) 본문 변경 없음 — KIS paper BUY 4건 end-to-end 1차 통과로 1차 실증 메모만 보강(`READY -> REQUESTED` 전환은 Strategy Execution /

`REQUESTED -> SUBMITTED` 전환은 MarketConnector executor `connector_strategy_order_execute.py` `--execute` / SELL 호출 0건 → `mark_position_sell_ordered()` 본 일자 호출 0건) / Status 기존 값 그대로 유지.

OD-MS-021(Backend AWS E2E dry-run 17단계 순서 + 안전 기준) 본문 변경 없음 — 17단계 순서대로 진행 / 안전 기준 정합(실제 broker 호출은 KIS paper BUY 4건 한정 / SELL · 취소 · 정정 호출 0건 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건 / Research → Decision 순서 / Connector Balance 1번 /
Balance Refresh 17번 위치 유지) / 12 ~ 17 구간 paper 환경 1차 통과 / Status 기존 값 그대로 유지.

OD-DB-008(`marketconnector_app` execution R-only) 본문 변경 없음 — execution UPDATE 권한 누락이 Step 12 1차 실패의 한 원인 / 운영자 직접 GRANT 로 SUBMITTED 전환 가능하도록 1차 보정 / R-only 본 결정의 정책은 후속 재검토 후보(MarketConnector executor 가 `strategy_execution_order` 를 SUBMITTED /
FAILED 로 갱신해야 하므로 execution-table 일부 UPDATE 권한이 필요한지 02 spec 후속 분리).

OD-MS-008(Research 컴퓨트 1순위 = AWS Batch) /
OD-MS-019(View Daily Batch 기준 AWS Batch 포팅 대상 = `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종 + S3 prefix `strategy-research/reports/`) 본문 변경 없음 — Step 4 / Step 5 정상 실행으로 1차 실증 메모만 보강 / heavy 분류(`run_extended_analysis` / `block_watch_*` /
`block_exception_buy_*`) SubmitJob 0건 유지(R-AUTO-015 정합) / Status 기존 값 그대로 유지.

OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없음 — 실제 broker 호출은 KIS paper BUY 4건만 발생 / SELL / 취소 / 정정 / 추가 `--execute` 0건 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건 / Status 기존 값 그대로 유지.

보강 / 신규 리스크: 신규 R-AUTO-018(KIS `inquire-daily-ccld` `output1 empty` + `output2 aggregate summary` 응답 형태에서 active 주문 후보 다건일 때 summary fallback 으로 잘못된 fill /
status 매핑 위험, mitigation = `connector_order_check.py` summary fallback guard 패치 + active 후보 정확히 1건일 때만 fallback 허용 + 다건이면 event / fill / status 변경 금지 + `broker_order_no` 별 단건 조회 패턴, Status `Mitigated`).

신규 R-DATA-011(`marketconnector_app` 의 `legacy` schema USAGE / `legacy.holdings` DML / sequence / database search_path 누락으로 BALANCE_REFRESH 실패 위험, mitigation = role database search_path 보정 + USAGE / DML / sequence GRANT + default privileges 보정, Status `Mitigated`).

R-DOCS-001(secret 평문 기록 위험) detection / mitigation 에 [2026-06-17 보강(17-step E2E)] 메모 추가 — KIS paper BUY 4건 broker 호출이 발생했음에도 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 ARN /
IAM access key id / EIP / image digest 평문 기록 0건 재실증 / Status 기존 값 그대로 유지.

R-DATA-005(DB Role 최소 권한 적용 후 부족 / 과다 가능) detection / mitigation 에 [2026-06-17 보강] 메모 추가 — `execution_app` interest 권한 누락(Step 8) + `marketconnector_app` legacy.holdings 권한 누락(Step 17) 사례 + 운영자 직접 GRANT 보정으로 해소 / 02 spec db-roles-and-grants 정식 갱신은 후속 / Status 기존 값 그대로 유지.

R-AUTO-009 / R-AUTO-010 detection / mitigation Status 보강 — KIS paper BUY 4건 한정 1차 end-to-end 통과(`READY -> REQUESTED -> SUBMITTED -> FILLED` + position OPEN) / `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` guard 정합 / Status `Mitigated` / live cutover 진입 전 평일 / 안전 데이터 추가 검증 후속.

R-AUTO-011 detection / mitigation Status 보강 — View Daily Batch 17단계 12 ~ 17 구간 paper 환경 1차 통과 / Status `Mitigated`. R-AUTO-015 mitigation·detection 보강(BACKTEST_RESEARCH + BACKTEST_REPORT 정상 실행 / heavy 분류 SubmitJob 0건 유지) / Status `Mitigated` 유지.

실제 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

AWS / SSM / EC2 / ECS / AWS Batch / IAM / Secrets Manager / SSM Parameter Store / RDS / GRANT / KIS / S3 / CloudWatch 작업은 모두 운영자 직접 수행 — Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건.

broker / KIS 호출은 KIS paper BUY 4건(Step 12) + balance / order check 조회성 한정. SELL / 취소 / 정정 / 추가 `--execute` 호출 0건. fill · position sync 자동 재시도 0건. SELL position `mark_position_sell_ordered()` 호출 0건. live 자동 BUY / SELL E2E 검증은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지.

본 일자는 `aws-paper` 한정 / aws-live 작업 0건. RDS DDL 0건. DML 은 본 일자 17-step 정상 흐름 한정.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건 — 운영자가 직접 패치 / 배포한 `connector_strategy_order_execute.py` / `connector_order_check.py` 변경분은 03 spec operation-notes 에 사실로만 기록(본문 전체 인용 0건).

운영 식별자(execution_plan_id `92` / execution_order id `26 ~ 29` / connector_order_request id `34 ~ 37` / broker_order_no `0000035906` / `0000035912` / `0000035918` / `0000035932` / position_state_id `6 ~ 9` / connector_position_snapshot id `120 ~ 123` /
종목 코드 `282330` / `004990` / `003490` / `088350` / 종목명 / 수량 / avg_buy_price / total_qty `378` / total_target_amount `6908189.40` /

Sharpe Ratio `2.68` / data_date `2026-06-16` / signal_date · run_date `2026-06-17`) 는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

KIS paper 4건의 `broker_order_no` 는 broker 응답값으로 운영 식별자 — 실계좌 주문번호 아님.

결정 외 운영 결과 기록은 [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-17 (Daily AWS 17-step E2E 완료) §1 ~ §5 / [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-17 §1 ~ §6 /

[`../09-strategy-research-batch/operation-notes.md`](../09-strategy-research-batch/operation-notes.md) 2026-06-17 §1 ~ §3 /
[`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-17 §1 ~ §3 /
[`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-17 §1 ~ §3 /

[`../06-secrets-and-iam/operation-notes.md`](../06-secrets-and-iam/operation-notes.md) 2026-06-17 §1 ~ §3 참조.


### Change Log Details 2026-06-17 (3차) <a id="change-log-details-2026-06-17-3"></a>

변경 요약: OD-MS-023 추가 + OD-MS-009 / OD-MS-021 / OD-MS-016 / OD-SAFE-002 / OD-SAFE-003 / OD-SAFE-004 1차 실증 메모 (Daily AWS PowerShell wrapper 구현)

2026-06-17 같은 일자 두 번째 세션(Daily AWS 17-step E2E 완료) 후속으로 운영자가 직접 수행한 Daily AWS 17-step 운영자용 Windows PowerShell wrapper 구현 결과를 반영.

신규 결정: OD-MS-023(Daily AWS wrapper 운영 정책 = 운영자 로컬 Windows PowerShell wrapper 분리 파일 구조(main wrapper `run-daily-aws-paper.ps1` + config `daily-aws-paper.config.ps1` + functions `daily-aws-paper.functions.ps1` + step 파일 17개 `steps/step-01-...` ~
`steps/step-17-...`) / 환경 입력은 `aws-paper` 만 허용 / wrapper 자체는 EC2 · ECS · Batch 내부 실행 대상 아님 / 17 step 매핑 · 진행 순서 · 안전 기준은 OD-MS-021 그대로 유지 /

Step 12 는 wrapper 중앙 PAPER_ORDER_GATE + Step 12 내부 이중 gate 로 기본 차단 / `-AllowPaperOrderExecute` 명시 시에만 허용 / Step 10 · Step 11 의 `--execute` 는 strategy execution 내부 상태 생성 / 갱신 의미 / broker · KIS 직접 제출 아님(OD-MS-016 정합) /
bundled wrapper(`run-daily-aws-paper-bundled.ps1`) 는 필요 시에만 생성 / 기본 분리 파일 구조 유지 / 완전 자동화 이전 단계의 단계별 확인 가능한 CLI 기준선, 🟡 잠정).

OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) 본문 변경 없이 1차 실증 메모만 보강 — Step Functions 이전 단계의 운영자 로컬 PowerShell wrapper 가 같은 17 step 매핑 / 진행 순서를 그대로 따른다는 정합성 / Status 기존 값 그대로 유지.

OD-MS-021(Backend AWS E2E dry-run 17단계 순서 + 안전 기준) 본문 변경 없이 1차 실증 메모만 보강 — wrapper 가 같은 17단계 순서 + 같은 안전 기준(BUY · SELL 실행 금지 / `--execute` 주문 전송 계열 차단 / fill · position sync 자동 재시도 금지 / aws-live 작업 금지 / Research → Decision 순서 / Connector Balance 1번 /
Balance Refresh 17번) 을 코드 레벨에서 강제 / Status 기존 값 그대로 유지.

OD-MS-016(Strategy Execution / MarketConnector 책임 분리) 본문 변경 없이 1차 실증 메모만 보강 — Step 10 · Step 11 의 `--execute` 는 strategy execution 내부 상태 생성 / 갱신 의미로 wrapper 안에 분류 / Step 12 만 실제 KIS paper 주문 제출 가능 step 으로 분류(broker / KIS 직접 제출 책임은 Step 12 한정) / Status 기존 값 그대로 유지.

OD-SAFE-002 / OD-SAFE-003 / OD-SAFE-004 본문 변경 없이 1차 실증 메모만 보강 — wrapper 의 환경 입력은 `aws-paper` 만 허용 / aws-live 분기 자체가 코드 레벨에서 미존재 / Step 12 PAPER_ORDER_GATE 차단 / 신규 자동 retry 미도입 / Status 기존 값 그대로 유지.

보강 / 신규 리스크: 신규 R-AUTO-019(wrapper 기반 Step 12 의도하지 않은 `-AllowPaperOrderExecute` 사용 위험, mitigation = 중앙 PAPER_ORDER_GATE + Step 12 내부 이중 gate + 운영자 직접 옵션 명시 + 옵션 자동 default OFF + 옵션 사용 시 운영자 노트 사전 기록 권고, Status `Mitigated`).

R-AUTO-016(Administrator interactive session 부재 시 KRX GUI 수집 실패) detection / mitigation 에 [2026-06-17 wrapper 보강] 메모 추가 — Step 2 의 KRX GUI Scheduled Task trigger 는 Windows KRX crawler worker 가 `running` 이 아니면 wrapper 안에서 자동 skip(EC2 instance state / SSM Online 사전 점검) / Status 기존 값 그대로 유지.

R-AUTO-002(live 자동매매 조기 활성화) mitigation 에 [2026-06-17 wrapper 보강] 메모 추가 — wrapper 의 환경 입력은 `aws-paper` 만 허용 / aws-live 분기 코드 레벨 미존재 / Status 기존 값 그대로 유지.

R-DOCS-001(secret 평문 기록 위험) detection 에 [2026-06-17 wrapper 보강] 메모 추가 — wrapper summary / overrides JSON / SSM stdout · stderr 파일에 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname 평문 출력 0건 /
`/tmp/inject-env.sh` v5 env injection 만 사용 / Status 기존 값 그대로 유지.

wrapper 검증 milestone(2026-06-17): 전체 1~17 DryRun FOUND 17건 통과 / 실제 ECS RunTask · Batch SubmitJob · SSM command 제출 0건 / Step 1 단독 SSM 1차 검증 통과(MarketConnector EC2 / `connector_balance.py` / Success / ResponseCode 0 / `connector_balance_snapshot` 저장 / 보유종목 0건) /
Step 12 PAPER_ORDER_GATE 안전 차단 검증 통과(`-StartStep 12 -EndStep 12` / `DryRun: False` / `PaperOrder: False` 기본 / 중앙 + 내부 이중 gate 차단 /

`-AllowPaperOrderExecute` 없으면 SSM command 제출 자체 0건 / 실제 KIS 주문 제출 0건) / PowerShell parser validation 20개 파일 모두 OK / 위험 키워드 safety grep 통과(Step 10 / Step 11 의 `--execute` 는 strategy execution 내부 상태 갱신 / Step 12 의 `--execute` 만 KIS paper 주문 제출 가능 step / 의도하지 않은 broker · KIS 주문 제출 command 추가 0건).

본 wrapper 작업 중 실제 broker / KIS / 신규 BUY · SELL · 취소 · 정정 / `--execute` 주문 제출 0건 / SELL position `mark_position_sell_ordered()` 호출 0건 / fill · position sync 자동 재시도 0건 / wrapper 기반 전체 1~17 실제 재실행 0건 / Step 12 `-AllowPaperOrderExecute` 사용 0건 /
Windows KRX crawler worker stopped 상태 시 Step 2 실제 실행 검증 0건(skip 동작 코드 존재 / 실제 시나리오 검증은 후속) / bundled wrapper(`run-daily-aws-paper-bundled.ps1`) 미생성.

본 일자는 `aws-paper` 한정 / aws-live 작업 0건.

실제 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

AWS / SSM / EC2 / ECS / Batch / IAM / Secrets Manager / SSM Parameter Store / RDS / KIS 호출은 모두 운영자 직접 수행 — Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건.

CloudWatch Logs 본문 / SSM 응답 본문 / KIS API response body / Docker build 로그 / 운영자 PowerShell stdout 전문 평문 인용 0건.

RDS DDL 0건 / DML 은 Step 1 단독 SSM 1차 검증의 `connector.connector_balance_snapshot` insert 1건 / `connector.connector_position_snapshot` 보유종목 0건 처리 / `legacy.holdings` 저장은 03 spec Step 1 흐름 정합 한정.

`.kiro/scripts/` 신규 폴더 / 파일은 운영자 로컬 PC 도구 / Kiro spec 산출물 외부 / 8개 MS 소스 / 패키징 / docs / worklog / README / AGENTS.md / CHANGELOG 영역과 분리.

04 / 06 / 08 / 09 spec operation-notes 본 일자 wrapper 작업으로 인한 변경 0건 — wrapper 는 cross-cutting 운영자 도구이므로 03 spec operation-notes 에만 누적 / 다른 spec 은 followups-overview 메모로만 참조.

운영 식별자(wrapper 파일명 / step 파일명 / cluster `portfolio-paper-cluster` / Task Definition family·revision / Job Queue · Job Definition family · revision / Log Group 이름 / `C:\Temp\portfolio-daily-aws-paper` 하위 run 폴더 / parser validation 20개 파일) 만 사실 기록 — secret 가 아님.

결정 외 운영 결과 기록은 [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-17 (Daily AWS PowerShell wrapper 구현) §1 ~ §6 참조.


### Change Log Details 2026-06-18 (1차) <a id="change-log-details-2026-06-18-1"></a>

변경 요약: OD-MS-024 추가 + OD-MS-016 / OD-MS-021 / OD-MS-023 1차 실증 메모 (Daily AWS Paper Wrapper 17단계 실운영 검증 완료)

2026-06-18 운영자가 직접 수행한 Daily AWS Paper Wrapper(`.kiro/scripts/run-daily-aws-paper.ps1`) 의 Step 1 ~ Step 17 실 실행 결과를 반영. 환경 `aws-paper` / RunDate `2026-06-18` / region `ap-northeast-2` / Step 12 `-AllowPaperOrderExecute` 첫 사용.

17 step 결과(요약) — Step 1 ~ Step 5 정상(MarketConnector EC2 SSM / 비즈니스 step ECS / AWS Batch / interest raw `2026-06-17` / `pre_total_*_feature` `2026-06-17` / Sharpe / S3 산출물). Step 6 ~ Step 11 정상(BUY READY 4건 / `execution_plan_id 94` / 추가매수 허용 정책 정합).

Step 12 1차 KIS paper API read timeout(connector_order_request id `38 ~ 41` FAILED / strategy_execution_order id `30 ~ 33` FAILED) → 통제된 REQUESTED 복구(connector_order_request · api_call_log · broker_order_no 사전 점검) → 재시도 KIS paper BUY 4건 제출 성공(broker_order_no
`0000025576` / `0000025740` / `0000025744` / `0000025747` / connector_order_request id `42 ~ 45` ACCEPTED / strategy_execution_order id `30 ~ 33` SUBMITTED).

Step 13 broad 조회 `output1 empty` + `output2 summary-only` + active candidate 4건 → summary fallback 자동 skip → 단건 `--code` / `--order-no` / `--no-broad` 조회로 4건 체결 반영. Step 14 정상 skip. Step 15 strategy_execution_order id `30 ~ 33` FILLED.

Step 16 1차 strategy_position_state unique constraint 충돌(R-DATA-012 신규) → `execution_sync_buy_position.py` merge 패치(`merge_open_position_state()` + `buy_info.additional_buys` + `execution_order_id` · `connector_order_request_id` idempotency) → Docker rebuild +
ECR push(image digest 운영자 보관 / 본 문서 평문 기록 0건) → ECS 재실행 통과(004990 65→69주 id `7` / 003490 52→58주 id `8` / 023530 신규 8주 id `11` / 042660 신규 11주 id `12`).

Step 17 SSM commandId `66ec8831-74b9-469c-8410-6ccb11cb3400` / responseCode 0 / `connector.connector_position_snapshot` row_count `36` / `legacy.holdings` row_count `41` / view_app 권한 부재로 `portfolio_admin` 우회 조회.

최종 6종목 OPEN(003490 58주 / 004990 69주 / 023530 8주 / 042660 11주 / 088350 244주 / 282330 17주). 신규 결정: OD-MS-024(추가매수 허용 정책 + 기존 OPEN row merge / `merge_open_position_state()` + `additional_buys` + idempotency / 기존 OPEN 부재 시에만 신규 INSERT, 🟡 잠정).

OD-MS-016(Strategy Execution / MarketConnector 책임 분리) 본문 변경 없음 — Step 11 `READY -> REQUESTED` / Step 12 `REQUESTED -> SUBMITTED` + KIS paper BUY 본 실행 / Step 15 FILLED / Step 16 추가매수 merge + 신규 INSERT 동시 통과로 1차 end-to-end 실증 / Status 기존 값 그대로 유지.

OD-MS-021(Backend AWS E2E dry-run 17단계 순서 + 안전 기준) 본문 변경 없음 — wrapper 가 17단계 순서 + 안전 기준(BUY · SELL 자동 재시도 금지 / Research → Decision 순서 / Connector Balance 1번 / Balance Refresh 17번) 그대로 따라 실 실행으로 통과 / Status 기존 값 그대로 유지.

OD-MS-023(Daily AWS wrapper 운영 정책) 본문 변경 없음 — wrapper 1~17 실 실행 통과 / Step 12 `-AllowPaperOrderExecute` 첫 사용 / 분리 파일 구조 + 환경 입력 `aws-paper` 한정 정합 / bundled wrapper 미생성 정책 유지 / Status 기존 값 그대로 유지.

OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없음 — 실제 broker 호출은 KIS paper BUY 4건 한정 / SELL · 취소 · 정정 · 추가 `--execute` 호출 0건 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건.

보강 / 신규 리스크: 신규 R-BROKER-004(KIS paper API read timeout 시 단순 재실행으로 인한 broker 중복 주문 위험, mitigation = `connector_order_request` / `connector_api_call_log` / `broker_order_no` 존재 여부 사전 점검 후 통제된 REQUESTED 복구 / 본 일자 1차 실증, Status `Mitigated`).

신규 R-DATA-012(추가매수 시 `strategy_position_state` `unique(account_id, ticker_code, status='OPEN')` 충돌 위험, mitigation = `execution_sync_buy_position.py` merge 패치 + idempotency, Status `Mitigated`).

R-AUTO-018(KIS `inquire-daily-ccld` summary-only fallback 오매핑) detection / mitigation 에 [2026-06-18 보강] — wrapper Step 13 broad 조회에서 `output1 empty` + `output2 summary-only` + active candidate 4건 상태에서 summary fallback 자동 skip 1차 실증 / 단건 `--code` · `--order-no` · `--no-broad` 조회로만 fallback 허용 정책 정합.

R-AUTO-009 / R-AUTO-010 / R-AUTO-011 / R-AUTO-019 detection / mitigation Status 보강 — Step 12 `-AllowPaperOrderExecute` 첫 사용 / 1차 timeout → 통제된 복구 → end-to-end 통과 / Status 기존 `Mitigated` 유지.

R-DATA-005 detection / mitigation 에 [2026-06-18 보강] — view_app 의 `legacy.holdings` SELECT 권한 부재로 Step 17 결과 확인 시 `portfolio_admin` 으로 우회 조회 / 정식 GRANT 후속 검토(02 spec db-roles-and-grants 후속).

실제 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN /
KIS paper login credential 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

AWS / SSM / EC2 / ECS / Batch / IAM / Secrets Manager / SSM Parameter Store / RDS / GRANT / KIS / S3 / CloudWatch / Docker / ECR 작업은 모두 운영자 직접 수행 — Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건.

CloudWatch Logs 본문 / SSM 응답 본문 / KIS API response body / Docker build · push 로그 / 운영자 PowerShell stdout 전문 평문 인용 0건. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 직접 호출은 KIS paper BUY 4건(Step 12 본 실행) + balance · order check 조회성 한정. SELL / 취소 / 정정 / 추가 `--execute` 호출 0건.

SELL position `mark_position_sell_ordered()` 호출 0건. fill · position sync 자동 재시도 0건(운영자 직접 patch 후 재실행). live 자동 BUY · SELL E2E 검증은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건. KIS paper 4건의 `broker_order_no` 는 broker 응답값으로 운영 식별자 — 실계좌 주문번호 아님.

RDS DDL 0건. DML 은 본 일자 17-step 정상 흐름 한정. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역) — 운영자 직접 patch / Docker rebuild / ECR push 한 `port_strategy_execution/execution_sync_buy_position.py` 변경분은 04 spec operation-notes 에 사실로만 기록(본문 전체 인용 0건).

운영 식별자(`execution_plan_id 94` / connector_order_request id `38 ~ 41`(FAILED) + `42 ~ 45`(ACCEPTED) /
strategy_execution_order id `30 ~ 33` / broker_order_no `0000025576` · `0000025740` · `0000025744` · `0000025747` / position_state_id `7` · `8` · `11` · `12` / SSM commandId `66ec8831-74b9-469c-8410-6ccb11cb3400` /
`connector.connector_position_snapshot` row_count `36` /

`legacy.holdings` row_count `41` / 종목 코드 / 종목명 / 수량 / entry_price / data_date `2026-06-17` / signal_date · run_date `2026-06-18`) 만 사용자 명시 정책 정합 사실 기록 — secret 가 아님.

결정 외 운영 결과 기록은 [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-18 §1 ~ §4 / [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-18 §1 ~ §6 참조.


### Change Log Details 2026-06-18 (2차) <a id="change-log-details-2026-06-18-2"></a>

변경 요약: OD-MS-025 추가 + OD-MS-016 / OD-MS-021 / OD-MS-023 1차 실증 보강 (Step 13 `connector_order_check.py` 단건 순차 조회 기본화)

2026-06-18 같은 일자 첫 번째 세션(Daily AWS Paper Wrapper 17단계 실운영 검증 완료) 후속으로 운영자가 직접 수행한 MarketConnector `connector_order_check.py` 기본 실행 모드 변경(broad 체결조회 일괄 → active 주문 단건 direct-only 순차 조회) 결과를 반영.

운영 실패가 아니라 운영 안전성 강화를 위한 설계 변경 — 같은 일자 첫 번째 세션의 broad 조회 `output1 empty` + `output2 summary-only` + active candidate 4건 응답 패턴(R-AUTO-018 [2026-06-18 보강] mitigation 1차 실증) 결과를 더 안전한 기본 동작으로 코드 레벨에 반영.

신규 결정: OD-MS-025(MarketConnector `connector_order_check.py` 운영 모드 = active 주문 단건 순차 조회 기본 + broad 옵션 격리 + `connector_order_check.py` 내부 분기.

기본 실행은 active 주문 목록 조회 후 주문번호 / 종목코드 기준 단건 direct-only 순차 조회 / legacy broad 일괄 조회는 `--broad` 옵션 명시 시에만 진입 / 명시 주문 조회는 `--code` · `--order-no` · `--no-broad` 조합으로 broad fallback 없이 해당 주문만 조회 /
다건 active 주문 상태에서 KIS `output1 empty` + `output2 summary-only` 응답이라도 `connector_order_request` 후보가 1건 확정된 경우에만 summary fallback 허용 / wrapper ps1 = orchestration 만 / 체결조회 방식 제어 = `connector_order_check.py` 내부, 🟡 잠정).

OD-MS-016(Strategy Execution / MarketConnector 책임 분리) 본문 변경 없음 — 본 결정은 MarketConnector 내부 운영 모드 변경 / Strategy Execution 측 책임 경계는 그대로 / Status 기존 값 그대로 유지.

OD-MS-021(Backend AWS E2E dry-run 17단계 순서 + 안전 기준) 본문 변경 없음 — 17단계 순서 / 안전 기준은 변경 없음 / Step 13 단독 실행 `-StartStep 13 -EndStep 13` 으로 동작 변경 검증 완료 / Status 기존 값 그대로 유지.

OD-MS-023(Daily AWS wrapper 운영 정책) 본문 변경 없음 — wrapper ps1 의 책임은 orchestration 만(SSM RunCommand 호출 / 환경 검증 / log 저장) 으로 유지 / 체결조회 방식 제어는 `connector_order_check.py` 내부 책임으로 명확화 / Status 기존 값 그대로 유지.

OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없음 — 본 일자 두 번째 세션의 broker / KIS 호출은 단건 direct-only 조회 1건(`004990` / `0000025576`) + Step 13 단독 wrapper 실행 1건 한정 / 모두 조회성 / 신규 BUY · SELL · 취소 · 정정 · `--execute` 호출 0건 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건. 보강 / 신규 리스크: 신규 R 0건.

R-AUTO-018(KIS `inquire-daily-ccld` summary-only fallback 오매핑) detection / mitigation 에 [2026-06-18 추가 보강] 메모 추가 — 본 일자 첫 번째 세션의 wrapper 안 자동 skip 1차 실증에 더해 `connector_order_check.py` 자체의 기본 실행 모드가 active 주문 단건 순차 조회로 변경됨으로써 broad 체결조회 호출 빈도 자체가 줄고 다건 active
상태에서 summary fallback 노출 표면이 감소 / Status `Mitigated` 유지(verified on 2026-06-18 두 번째 세션).

기존 Step 13 자동화 보완 후속(followups-overview 2026-06-18 §1) 은 본 일자 결과로 1차 실증 / 정식 명문화 / wrapper run summary 출력 보강 / View Daily Batch 화면 연동 / cp949 인코딩 회피 패턴은 후속 분리.

검증 milestone(2026-06-18 두 번째 세션): 로컬 `python -m py_compile connector_order_check.py` 통과 / `python connector_order_check.py --help` 의 `--broad` / `--active-limit` 옵션 표시 확인 / 로컬 commit `75cb804`(`fix(connector): run order checks sequentially per active order` /
현재 repository 에 remote push destination 미설정 → git push 미수행 / 후속 분리 — followups-overview 2026-06-18 §1).

EC2 반영 = S3 경유(bucket `portfolio-paper-migration-yukiever` / key `deploy/marketconnector/connector_order_check.py` / size `39159 bytes` / EC2 instance id `i-0fce77927b7397b88` / EC2 backup 파일명 `connector_order_check.py.bak-20260618-step13-per-order` /
SSM commandId `b344d404-07a4-4bb6-9d63-34151e648bab`).

wrapper 단독 실행 검증(`-StartStep 13 -EndStep 13`) — 단건 direct-only 조회 1건(`004990` / `0000025576`) 기준 `connector_order_event` / `connector_fill` 정상 생성.

실제 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / EIP / image digest full sha256 / task ARN / job ARN /
KIS paper login credential 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

AWS / SSM / EC2 / S3 / IAM / Secrets Manager / SSM Parameter Store / RDS / KIS 호출은 모두 운영자 직접 수행 — Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건.

CloudWatch Logs 본문 / SSM 응답 본문 / KIS API response body / 운영자 PowerShell stdout 전문 평문 인용 0건. broker / KIS 호출은 단건 direct-only 조회 1건 + wrapper Step 13 단독 실행 1건 한정 / 모두 조회성 / 신규 BUY · SELL · 취소 · 정정 · `--execute` 호출 0건 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건.

RDS DDL 0건 / DML 은 Step 13 정상 흐름 한정(`connector.connector_order_event` / `connector.connector_fill` 신규 row 는 단건 direct-only 조회 결과 정합).

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 두 번째 세션 작업으로 인한 변경 0건(spec 영역) — 운영자 직접 patch 한 `port-marketconnector/connector_order_check.py` 변경분은 03 spec operation-notes 에 사실로만 기록(본문 전체 인용 0건 / R-DOCS-001 정합).

운영 식별자(commit `75cb804` / S3 bucket / S3 key / 파일 크기 `39159 bytes` / EC2 instance id `i-0fce77927b7397b88` / EC2 backup 파일명 / SSM commandId `b344d404-07a4-4bb6-9d63-34151e648bab` / 종목 코드 `004990` / broker_order_no `0000025576` /
wrapper 옵션 `--broad` · `--active-limit` · `--code` · `--order-no` · `--no-broad` / wrapper 파라미터 `StartStep` · `EndStep`) 만 사용자 명시 정책 정합 사실 기록 — secret 가 아님.

instance id 는 OD-NET-010 에 이미 사실 기록되어 있는 표준 SSM Port Forwarding 표준 경유지 식별자. 결정 외 운영 결과 기록은 [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-18 (Step 13 `connector_order_check.py` 단건 순차 조회 기본화) §1 ~ §6 참조.


### Change Log Details 2026-06-21 (1차) <a id="change-log-details-2026-06-21-1"></a>

변경 요약: OD-MS-026 추가 + OD-MS-011 / OD-MS-015 / OD-MS-022 / OD-MS-023 1차 실증 보강 (Step 2 INTEREST_CRAWLER 성공판정 강화)

2026-06-20(AWS 자동 Wrapper 최종 점검 / 6/18 중복 실행 시도 안전 중단 / 6/19 KRX raw 최신성 복구 상태 점검 / EC2 lifecycle 후속 필요성 / Scheduled Task trigger / LASTEXITCODE 중심 성공판정 한계 식별) 후속으로 2026-06-21 운영자가 직접 수행한 (a) `step-02-interest-crawler.ps1` 성공판정 강화 — Chrome /
chromedriver best-effort reset / Administrator interactive Scheduled Task 경로 유지(SSM direct python 채택 거부) /

Running 상태 polling 후 Ready 복귀 wait + `sawRunning` / Last Result 0 또는 0x0 만 SUCCESS / latest worker log path · last write time · size · tail 출력 / crawler worker EC2 fail-closed(`running` 아니면 즉시 실패 / instanceId · state 출력),
(b) non-GUI ECS crawler env 보강 — `Invoke-DailyAwsPaperEcsTask` `EnvironmentVariables` 파라미터 / `containerOverrides.environment` 전달 /

`New-SsmParameterFile` · `Invoke-SsmCommandAndWait` `ExecutionTimeoutSeconds` 지원 / 환경변수 `TEMP=/tmp` · `TMP=/tmp` · `PYTHONUTF8=1` · `PYTHONIOENCODING=utf-8`, (c) `interest_krx_raw_validate_daily.py` 운영 검증 스크립트 신규 생성 — `interest_program_raw` /
`interest_shortsell_raw` expected trade_date 기준 row_count + `max(trade_date)` 검증 / 실패 시 exit code 30 /

로컬 py_compile + UTF-8 read + program · shortsell · exit30 marker 통과, (d) S3 presigned URL 경유 Windows crawler worker EC2 배포 — bucket `portfolio-paper-migration-yukiever` / key `tmp/krx/interest_krx_raw_validate_daily.py` /
배포 대상 `C:\portfolio\port-interest-crawler\interest_krx_raw_validate_daily.py` / SSM commandId `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3` /

원격 py_compile + marker + 인자 + exit code 30 로직 + `sys.exit(run(parsed_args))` 확인, (e) EC2 단독 검증 — `load-crawler-db-env.ps1` + `venvs/interest-crawler` venv + Python `3.13.5` /
DB session user `crawler_app` schema `interest` search_path `interest, reference, legacy, public` / interest_program_raw expected=`2026-06-19` max_date=`2026-06-19` expected_count=`1` /

interest_shortsell_raw expected=`2026-06-19` max_date=`2026-06-19` expected_count=`349` / exit code 0 / SSM commandId `c844aea5-1429-430a-9510-39fc99f17f05`, (f) `step-02-interest-crawler.ps1` DB validation 연동 — `ExpectedKrxRawDate` 계산(RunDate 기준 전 영업일) /
`INTEREST_CRAWLER_KRX_DB_VALIDATE` SSM step / step result 에 `KrxDbValidationCommandId` 포함 /

non-zero exit 또는 row_count 0 시 Step 2 fail, (g) Step 2 단독 실행 검증 — RunId `daily-aws-paper-20260621-204017` / Status `SUCCESS` / Runner `ECS+SSM` / ExpectedKrxRawDate `2026-06-19` /
non-GUI ECS taskDefinition `portfolio-paper-interest-crawler:7` taskId `78979b5cbb714d0eb94f5946e15a14ce` exitCode 0 stoppedReason `Essential container in task exited` /

KRX GUI worker SSM commandId `f9d82fcc-1e26-4710-87c3-1d20483b63ef` Success ResponseCode 0 / Scheduled Task elapsedSeconds=`111` sawRunning=True FinalStatus=Ready FinalLastResult=0 Last Result=0 /
Logon Mode `Interactive only` Run As `Administrator` Task To Run `powershell.exe -ExecutionPolicy Bypass -File C:\portfolio\run_krx_worker_daily.ps1` /

latest worker log `C:\portfolio\logs\krx_worker_daily_20260621_114154.log`(KRX login · program · shortsell SUCCESS / `DONE :: KRX worker daily`) /
KRX raw DB validation SSM commandId `2279c6d7-2da6-4317-9c10-7cc77374b317` Success ResponseCode 0 / interest_program_raw OK / interest_shortsell_raw OK / validation exit code 0 / stderr empty 결과를 반영.

신규 결정: OD-MS-026(Step 2 INTEREST_CRAWLER 운영 성공 기준 = Scheduled Task trigger 가 아니라 KRX raw DB validation 까지 / KRX GUI 경로는 Windows Administrator interactive Scheduled Task / wrapper 는 실행 · 종료 대기 · Last Result · latest log · DB validation orchestration 담당 /
`interest_krx_raw_validate_daily.py` 가 raw 최신성 검증 담당 / crawler worker stopped 는 fail-closed, 🟡 잠정).

OD-MS-011(Hybrid execution model) / OD-MS-015(SSM RunCommand → `schtasks /Run` 자동화) / OD-MS-022(KRX GUI 자동 로그인 + Administrator interactive session + Scheduled Task + SSM trigger) /
OD-MS-023(Daily AWS wrapper 운영 정책) 본문 변경 없이 1차 실증 메모만 보강 — wrapper Step 2 가 6개 성공 조건(non-GUI ECS exitCode 0 / Crawler Worker EC2 running / Scheduled Task Running → Ready / Last Result 0 또는 0x0 / latest worker log / KRX raw DB validation 통과) 모두 통과로 1차 실증.

보강 / 신규 리스크: 신규 R-AUTO-020(Scheduled Task trigger 성공만 보고 Step 2 SUCCESS 처리 시 KRX raw 미적재가 Step 3 이후로 전파될 위험, mitigation = Chrome / chromedriver best-effort reset + Running → Ready wait + Last Result 확인 + latest worker log 출력 + KRX raw DB validation + worker stopped fail-closed, Status `Mitigated`).

R-AUTO-007(wrapper 성공 종료가 실제 DB 적재 성공을 보장하지 못함) detection / mitigation 에 [2026-06-21 보강] 메모 추가 — wrapper 안 DB 검증 자동 출력이 task 57 후속에서 task 단위 완료로 승격 / `KrxDbValidationCommandId` step result 추적 / `interest_program_raw` · `interest_shortsell_raw` expected date row_count 자동 확인 / Status `Mitigated` 갱신.

R-AUTO-016(Administrator interactive session 부재 시 KRX GUI 수집 실패) mitigation 갱신 — 이전 "wrapper 안에서 자동 skip" 표현은 본 일자에 fail-closed 로 갱신 / crawler worker EC2 stopped 시 즉시 실패 / instanceId · state 출력 / Step 2 SUCCESS 진입 차단 / Status 기존 값 그대로 유지.

R-AUTO-017(Chrome process 잔존) mitigation 에 [2026-06-21 보강] 메모 추가 — Chrome / chromedriver best-effort reset 1차 실증 / 실패는 warning 으로 진행 / 후속 EC2 stop / restart 절차는 R-AUTO-016 보강과 결합 / Status `Mitigated` 갱신.

실제 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / EIP / image digest full sha256 / task ARN / job ARN /
KIS paper login credential / Administrator password / S3 presigned URL 실값 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

운영 식별자(SSM commandId 4종 `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3` / `c844aea5-1429-430a-9510-39fc99f17f05` / `2279c6d7-2da6-4317-9c10-7cc77374b317` / `f9d82fcc-1e26-4710-87c3-1d20483b63ef` / RunId / taskDefinition / taskId / S3 bucket · key / Scheduled Task 이름 /
latest worker log 파일명 / wrapper 옵션 / DB session user · search_path · row_count) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / SSM / EC2 / S3 / ECS / RDS / KRX 호출은 모두 운영자 직접 수행 — Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건. broker / KIS / 신규 BUY · SELL · 취소 · 정정 · `--execute` 호출 0건. SELL position `mark_position_sell_ordered()` 호출 0건. fill · position sync 자동 재시도 0건.

aws-live 작업 0건. RDS DDL 0건 / DML 은 검증 SQL SELECT 한정 / 본 일자 신규 Step 2 wrapper 실행에서는 idempotent / no-op.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역) — 운영자 직접 작성 / patch 한 `port-interest-crawler/interest_krx_raw_validate_daily.py` / `step-02-interest-crawler.ps1` /
`daily-aws-paper.functions.ps1` 변경분은 08 spec operation-notes / 03 spec operation-notes 영역 외부 운영자 도구로 사실 기록만(본문 전체 인용 0건 / R-DOCS-001 정합).

결정 외 운영 결과 기록은 [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-20 §1 ~ §5 / 2026-06-21 §1 ~ §8 참조.


### Change Log Details 2026-06-22 (1차) <a id="change-log-details-2026-06-22-1"></a>

변경 요약: OD-MS-027 / OD-DB-011 신규 + OD-MS-016 / OD-MS-021 / OD-MS-023 1차 실증 보강 (Daily AWS Paper 1~17 두 번째 실 완주 + 첫 실제 SELL E2E)

2026-06-21(Step 2 INTEREST_CRAWLER 성공판정 강화 + KRX raw DB validation 연동) 후속으로 2026-06-22 운영자가 직접 수행한 Daily AWS Paper Wrapper 1 ~ 17 두 번째 실 운영 실행 + 첫 실제 SELL E2E 결과를 반영. 환경 `aws-paper` / RunDate `2026-06-22` / region `ap-northeast-2`.

단순 dry-run 이 아니라 실제 Paper SELL 1건 제출 / KIS 접수 / 체결조회 / SELL fill sync / position CLOSED / balance refresh 까지 end-to-end 통과 / AWS 운영 wrapper 가 주문 · 체결 · 포지션 · 잔고까지 정상 종료한 첫 완주성 운영 결과로 기록.

신규 결정: OD-MS-027(MarketConnector env bootstrap 재생성 운영 정책 = `/tmp/inject-env.sh` 선존재 가정 폐기 / Daily AWS Paper Wrapper 의 Step 1 / 12 / 13 / 17 가 진입 직전 wrapper 공통 함수로 `/tmp/inject-env.sh` 재생성 / Secrets Manager JSON SecretString 내부 key 추출 + `APP_*` /
`KIS_*` alias 동시 export / chmod 700 / 메모리 export 한정 / secret value 평문 출력 0건 / 정식 systemd unit + `EnvironmentFile` 등록은 후속 분리, 🟡 잠정).

OD-DB-011(`execution_app` 의 `decision.strategy_daily_position_decision` 제한적 UPDATE 권한 = SELL execution link update 책임 한정 / SELECT + UPDATE 두 권한 / INSERT · DELETE · TRUNCATE 미부여 / `decision` schema 다른 테이블 UPDATE 미부여 / OD-DB-007 SELECT-only 정책의 한정된 예외 /
02 spec db-roles-and-grants 정식 매트릭스 갱신 후속, 🟢 확정).

OD-MS-016(Strategy Execution / MarketConnector 책임 분리) 본문 변경 없이 1차 실증 메모 보강 — Step 10 `READY -> REQUESTED`(Strategy Execution) / Step 12 `REQUESTED -> SUBMITTED`(MarketConnector executor `connector_strategy_order_execute.py --execute`) /
Step 14 `SUBMITTED -> FILLED`(SELL fill sync) / Step 17 `OPEN -> CLOSED`(position state) 전이가 SELL 1건 한정으로도 1차 실증 / `mark_position_sell_ordered()` 호출은 MarketConnector executor SELL 성공 시점 책임(OD-MS-016 정합) / Status 기존 값 그대로 유지.

OD-MS-021(Backend AWS E2E dry-run 17단계 순서 + 안전 기준) 본문 변경 없이 1차 실증 메모 보강 — wrapper 가 17단계 순서 + 안전 기준 그대로 따라 실 실행으로 두 번째 실 완주 통과 / Status 기존 값 그대로 유지.

OD-MS-023(Daily AWS wrapper 운영 정책) 본문 변경 없이 1차 실증 메모 보강 — wrapper 1 ~ 17 두 번째 실 완주 / Step 12 `-AllowPaperOrderExecute` 두 번째 사용(2026-06-18 BUY 4건 / 2026-06-22 SELL 1건) / 분리 파일 구조 + 환경 입력 `aws-paper` 한정 정합 / bundled wrapper 미생성 정책 유지 / Status 기존 값 그대로 유지.

OD-MS-026(Step 2 성공판정 강화) 본문 변경 없이 1차 실증 메모 보강 — Step 2 의 6개 성공 조건 Daily run 회귀 0건 / Status 기존 값 그대로 유지. OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없음 — 실제 broker 호출은 KIS paper SELL 1건 한정 / BUY · 취소 · 정정 · 추가 `--execute` 호출 0건 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건.

보강 / 신규 리스크: 신규 R-AUTO-021(MarketConnector EC2 stop / start 후 `/tmp` 휘발로 Step 1 / 12 / 13 / 17 실패 위험, mitigation = step 실행 시점 wrapper 공통 함수의 MarketConnector env bootstrap 재생성 + secret value 미출력 + PowerShell parser validation + Step 1 재실행 검증, Status `Mitigated`).

신규 R-DATA-013(`execution_app` 의 `decision` schema UPDATE 권한 누락으로 Step 9 SELL execution link update 실패 위험, mitigation = `decision.strategy_daily_position_decision` UPDATE grant + grant 검증 SQL + Step 9 재실행 성공, Status `Mitigated`).

R-DATA-005(DB Role 최소 권한 적용 후 부족 / 과다 가능) detection / mitigation 에 [2026-06-22 보강] 메모 추가 — `execution_app` 의 `decision` schema USAGE / `decision.strategy_daily_position_decision` UPDATE 누락이 Step 9 1차 실패 원인으로 추가 식별 / 운영자 직접 GRANT 보정으로 해소 /
02 spec db-roles-and-grants 정식 매트릭스 갱신은 후속 / Status 기존 값 그대로 유지.

R-DATA-011 / R-AUTO-016 / R-AUTO-017 / R-AUTO-018 / R-AUTO-019 / R-AUTO-020 / R-BROKER-004 / R-DATA-012 detection / mitigation Status 회귀 0건 / Status 기존 `Mitigated` 유지.

검증 milestone(2026-06-22): execution plan id `96` 생성(plan_date `2026-06-22` / market_signal `DEFENSIVE` / plan_status `PARTIALLY_BLOCKED` / total_candidate 3 / ready 1 / blocked 2).

Step 12 대상 = `088350` 한화생명 SELL 244주 MARKET 1건 / 매도 사유 `SELL_HARD_STOP`(entry_date 2026-06-17 / entry_price 5,744.4057 / current_price 5,070 / expected_pnl_rate 약 -11.7402% / hard_stop_loss_rate -10% / holding_days 5 /
snapshot_qty · sellable_qty · remaining_qty 모두 244 / expected_pnl_amount 약 -164,554.9908).

`strategy_position_state id 9` 의 entry_qty 244 / remaining_qty 244 일치.

Step 12 `-AllowPaperOrderExecute` 명시 실행 성공(`execution_order id 37` SUBMITTED / `connector_order_request id 46` ACCEPTED / request_type SELL / order_method MARKET / order_qty 244 / broker_order_no · broker_branch_code 생성됨 / rejection 없음 / 본 문서 broker 응답값 평문 기록 0건).

Step 13 체결조회 성공(`connector_order_request id 46` FILLED / `connector_fill id 34` 생성 / fill_qty 244 / fill_price 5,075.8607 / fill_amount 1,238,510.01 / side SELL / fill_ts 2026-06-22 00:46:58 UTC).

Step 14 / 15 / 16 ECS exitCode 0 / Step 17 SSM Success / 최종 DB 검증 = `execution_order id 37` FILLED / `strategy_position_state id 9` remaining_qty 0 + position_status `CLOSED` + latest_sell_reason `SELL_HARD_STOP` /
`connector_position_snapshot` 최신 `created_at 2026-06-22 00:50:50 UTC` / 보유 5종목(`003490` 58주 / `004990` 69주 / `023530` 8주 / `042660` 11주 / `282330` 17주) / `088350` 한화생명 잔고 스냅샷에서 제거 확인.

실제 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN /
broker_order_no 원문 / broker_branch_code 원문 / KIS paper login credential / Administrator password 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

운영 식별자(`execution_plan_id 96` / execution_order id `37` / connector_order_request id `46` / connector_fill id `34` / position_state_id `9` / 종목 코드 / 종목명 / 수량 · 가격 · 비율 / data_date / signal_date · run_date `2026-06-22` / fill_ts · created_at UTC) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / SSM / EC2 / ECS / Batch / IAM / Secrets Manager / SSM Parameter Store / RDS / GRANT / KIS / S3 / CloudWatch 작업은 모두 운영자 직접 수행 — Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건.

CloudWatch Logs · SSM 응답 · KIS API response body / 운영자 PowerShell stdout 전문 평문 인용 0건. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역).

운영자 직접 patch 한 `daily-aws-paper.functions.ps1` MarketConnector env bootstrap 함수 / `GRANT USAGE ON SCHEMA decision TO execution_app` + `GRANT UPDATE ON TABLE decision.strategy_daily_position_decision TO execution_app` SQL 은 03 /
04 spec operation-notes 2026-06-22 에 사실로만 기록(본문 전체 인용 0건 / R-DOCS-001 정합).

결정 외 운영 결과 기록은 [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md) 2026-06-22 §1 ~ §6 / [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-22 §1 ~ §6 /

[`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-22 §1 ~ §4 / [`../09-strategy-research-batch/operation-notes.md`](../09-strategy-research-batch/operation-notes.md) 2026-06-22 §1 ~ §5 참조.


### Change Log Details 2026-06-23 (1차) <a id="change-log-details-2026-06-23-1"></a>

변경 요약: OD-MS-028 / OD-MS-029 신규 + OD-MS-009 / OD-MS-027 / OD-SAFE-001 / OD-SAFE-004 1차 실증 보강 (Step Functions approval 실전 검증 + Step 12 retry-normalizer + DB 한글 정상 확인)

2026-06-22(Daily AWS Paper 1~17 두 번째 실 완주 + 첫 실제 SELL E2E) 후속으로 2026-06-23 오전 운영자가 직접 수행한 Step Functions state machine `portfolio-paper-daily-step1-17-approval` 실전 검증 + Step 12 `connector_strategy_order_execute.py` 전체 교체 + Step 12 시작부에 retry-normalizer 내장 + DB 한글 정상 확인 결과를 반영.

환경 `aws-paper` / RunDate `2026-06-23` / region `ap-northeast-2`. 실제 broker 호출은 KIS paper SELL 1건(BGF리테일 `282330` 17주 MARKET / `connector_order_request id 48` FILLED / `strategy_execution_order id 40` FILLED / `broker_order_no 0000006143`) 한정. Step 17 balance refresh 후 최신 보유 4종목 정상 반영.

신규 결정: OD-MS-028(Step 12 retry-normalizer 내장 정책 = Step 12 외부 별도 step 분리가 아닌 Step 12 시작부 내장 / 복구 조건 6종 모두 만족 시에만 `execution_status = REQUESTED` + `connector_order_request_id = NULL` + `result_payload.retry_normalizer` 에 old request 이력 저장 /
`port-marketconnector/connector_strategy_order_execute.py` 전체 교체 / EC2 정식 배포 + `.venv/bin/python` dry-run 통과 / 현재 retry 후보 0건 / REQUESTED 주문 0건 / Step Functions Step 12 도 `.venv/bin/python` 사용 정합, 🟡 잠정 / 영향 spec 03 · 04 · 10).

OD-MS-029(Daily AWS Paper Step Functions approval workflow false / true path 운영 절차 = false path 사전 검증 후 true path 승인 실행 / 동일 state machine 의 `allowPaperOrderExecute` 입력 파라미터로만 분기 / 별도 state machine 분리 없음 / 2026-06-23 운영자 직접 실 검증으로 통과, 🟢 확정 / 영향 spec 04 · 10).

OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) 본문 변경 없이 1차 실증 메모 보강 — Step Functions state machine `portfolio-paper-daily-step1-17-approval` 가 17단계 순서 + 안전 기준 그대로 따라 false / true path 모두 통과 / EventBridge Scheduler 정기 트리거 도입은 후속 / Status 기존 값 그대로 유지.

OD-MS-027(MarketConnector env bootstrap 재생성 운영 정책) 본문 변경 없이 1차 실증 메모 보강 — Step Functions 전환 이후에도 Step 1 / Step 12 / Step 13 / Step 17 의 bootstrap 호출 흐름이 그대로 유지 / Status 기존 값 그대로 유지(R-AUTO-021 [2026-06-23 보강] 정합).

OD-SAFE-001 / OD-SAFE-004 본문 변경 없이 1차 실증 메모 보강:

- OD-SAFE-001 (paper 자동 BUY / SELL E2E 초기 차단 → 검증 후 허용)
- OD-SAFE-004 (자동 재시도 금지 정책 = idempotent 만)
- Step Functions approval workflow 가 `allowPaperOrderExecute` 입력값으로 BUY · SELL 실주문 제출을 명시적 승인 시점에만 허용하는 구조로 동작
- Step 12 retry-normalizer 의 복구 조건은 `rejection_code = 40580000` + `broker_order_no IS NULL` + `connector_fill` 없음으로 제한되어 idempotent step 의 통제된 복구 범위 안에서만 동작
- OD-SAFE-002 · OD-SAFE-003 본문 변경 없음 — 본 일자 작업은 `aws-paper` 한정 / aws-live 자동 BUY · SELL 정책은 후속 검증 / 승인 전까지 여전히 금지

보강 / 신규 리스크: 신규 R-AUTO-022 (Status `Mitigated`)

- Risk: 장종료 REJECTED / `40580000` 후 rejected `connector_order_request_id` 를 물고 있는 execution_order 가 다음날 자동 재제출되지 않는 위험
- Mitigation: Step 12 retry-normalizer 내장 + 복구 조건 6종 + `result_payload.retry_normalizer` 이력 저장
- 본 일자 dry-run 통과 / 실제 retry 후보 발생 시 운영 검증은 후속

R-AUTO-021(MarketConnector EC2 stop / start 후 `/tmp` 휘발) mitigation 에 [2026-06-23 보강] 메모 추가 — Step Functions state machine `portfolio-paper-daily-step1-17-approval` 의 Step 1 / Step 12 / Step 13 /
Step 17 state 가 MarketConnector EC2 SSM step 을 호출할 때도 동일한 wrapper 공통 bootstrap 흐름이 그대로 유지되는지 점검 / Step Functions 전환 이후에도 bootstrap 호출 흐름 유지 필요 / 정식 systemd unit + `EnvironmentFile` 등록은 후속(03 spec task 7 / task 26 / task 27) 책임 그대로 / Status 기존 `Mitigated` 유지.

DB 한글 깨짐은 신규 리스크 등록하지 않음 — PGAdmin4 기준 `connector_order_request` / `strategy_execution_order` / `connector_api_call_log` / `connector_position_snapshot` 한글 저장 / 조회 정상 + `server_encoding` · `client_encoding` 모두 `UTF8` 확인으로 DB 정합성 정상 / 일부 PowerShell / SSM /
AWS CLI 콘솔 출력 표시 경로의 인코딩 이슈로 판단 / followups-overview 2026-06-23 후속 메모로만 분리.

EGW00215 rate limit / balance refresh backoff · retry policy 도 신규 리스크 등록하지 않음 — 후속 과제로 followups-overview 2026-06-23 §1 에 분리.

실제 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN /
broker 응답 전문 / webhook URL / KIS paper login credential / Administrator password 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

운영 식별자(Step Functions state machine 이름 `portfolio-paper-daily-step1-17-approval` / `strategy_execution_order id 40` / `connector_order_request id 48` / `broker_order_no 0000006143` / 종목 코드 `282330` / 종목명 / 수량 17 / approval gate true · false 라벨 /
DB session table name 4종) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / SSM / EC2 / ECS / Batch / IAM / Secrets Manager / SSM Parameter Store / RDS / GRANT / KIS / Step Functions / S3 / CloudWatch / Docker / ECR 작업은 모두 운영자 직접 수행 — Kiro 는 본 일자 루트 / `_common` 문서 갱신만 수행. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건.

CloudWatch Logs · SSM 응답 · KIS API response body · Step Functions execution history 본문 / Docker build · push 로그 / 운영자 PowerShell stdout 전문 평문 인용 0건. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역).

운영자 직접 patch / 정식 배포한 `port-marketconnector/connector_strategy_order_execute.py` 전체 교체 + Step 12 시작부 retry-normalizer 내장 변경분은 03 spec operation-notes 후속 갱신(2차 / 3차) 책임으로 분리 — 본문 전체 인용 0건 / R-DOCS-001 정합.

Step Functions state machine 정의 / state-by-state input · output / `allowPaperOrderExecute` 분기 정합도 04 spec operation-notes 후속 갱신(2차 / 3차) 책임으로 분리.

본 1차 작업은 루트 / `_common` 문서 갱신 한정 — WORKLOG.md / CHANGELOG.md / README.md / `_common/operator-decisions.md` / `_common/risk-register.md` / `_common/followups-overview.md` / `_common/ms-aws-service-decision-matrix.md` 7개 문서. 결정 외 운영 결과 기록은 후속 phase 의 03 / 04 spec operation-notes 갱신을 통해 누적 책임으로 분리.


### Change Log Details 2026-06-23 (2차) <a id="change-log-details-2026-06-23-2"></a>

변경 요약: OD-MS-030 / OD-MS-031 신규 + OD-MS-009 / OD-MS-010 / OD-MS-029 1차 실증 보강 (AWS 공통 Slack notifier 구현 완료 + Step Functions 3종 Slack 검증)

2026-06-23 첫 번째 항목(Step Functions approval 실전 검증 + Step 12 retry-normalizer + DB 한글 정상 확인) 후속으로 같은 일자 운영자가 직접 수행한 (a) AWS 공통 Slack notifier Lambda 구현 + smoke / template test 통과 + (b) Step Functions approval workflow 의 3종 Slack(`APPROVAL_REQUIRED` /
`DAILY_EXECUTION_SUCCESS` / `DAILY_EXECUTION_FAILED`) 수신 검증 결과를 반영.

신규 결정: OD-MS-030(AWS 공통 Slack notifier Lambda 도입 정책 = Lambda `portfolio-event-notifier` (Runtime Python 3.12 / IAM Role `portfolio-event-notifier-lambda-role`) 기반 공통 운영 이벤트 알림 보조 계층 / Step Functions · EventBridge · EC2 SSM · Batch · Lambda 어디서든 호출 가능한 단일 진입점 /
`hello wook` 수동 invoke smoke + Portfolio Daily Bot 수신 + 6종 메시지 템플릿(장 전 잔고 / Daily 검증 완료 / Daily 실행 성공 / Daily 실행 실패 / 장중 손절 /

장 후 잔고) + 🔴 · 🔵 · ⚪ 손익 이모지 + Slack attachment color bar 1차 구성 / 완료 marker `SLACK_LAMBDA_SMOKE_TEST=SUCCESS` · `PORTFOLIO_EVENT_NOTIFIER_TEMPLATE_TEST=SUCCESS` / Slack webhook URL 은 현재 Lambda 환경변수 `SLACK_WEBHOOK_URL` 로 1차 검증 /
운영 안정화 후 Secrets Manager 또는 SSM Parameter Store 이전 예정 / 기존 port-view SlackNotificationService 는 이번 작업에서 제거 · 대체되지 않음 / 향후 공통 notifier 이전 가능성만 기록, 🟡 잠정 /

영향 spec 04 · 05 · 10).

OD-MS-031(Step Functions / EventBridge 1차 Slack 연동 범위 3종 한정 정책 = `APPROVAL_REQUIRED`(Step 1~11 완료 후 approval gate 진입) · `DAILY_EXECUTION_SUCCESS`(Step 17 완료 후 전체 성공) · `DAILY_EXECUTION_FAILED`(Step Functions 실행 중 실패 / Catch 경로) 3종만 1차 적용 /
장 전 잔고 · 장 후 잔고 · 장중 손절 알림은 후속 분리 / 1차 목표는 메시지 문구 고도화가 아니라 Step Functions 실행 흐름에서 Slack 수신 여부 검증, 🟢 확정 / 영향 spec 04 · 05 · 10).

OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) 본문 변경 없이 1차 실증 메모 보강 — Step Functions Catch 경로에서 `DAILY_EXECUTION_FAILED` 발송 가능성 확인 / 12~17 test-only 실패 수신 검증 + 1~17 full workflow test-only 실패 ASL 적용 + 수신 검증 완료 /
실제 broker 주문 실패 유발 0건 / EventBridge Scheduler 정기 트리거 진입은 후속 phase.

OD-MS-010(infra alarm 채널 = 도메인 알림은 port-view SlackNotificationService 유지 / 인프라 알람은 SNS → Lambda → Slack fan-out) 본문 변경 없이 1차 실증 메모 보강 — Slack notifier Lambda `portfolio-event-notifier` 가 인프라 알람 fan-out 보조 계층으로 추가 /
port-view SlackNotificationService 의 View Daily Batch 수동 실행 결과 알림 책임은 그대로 유지 / Status 기존 값 그대로 유지.

OD-MS-029(Step Functions approval workflow false / true path) 본문 변경 없이 1차 실증 메모 보강 — approval gate 진입 시점에 `APPROVAL_REQUIRED` Slack 발송 / 운영자가 Slack 으로 수동 승인 필요 상태를 인지하는 흐름 검증 통과.

보강 / 신규 리스크: 신규 R-AUTO-023(Step Functions 실패 경로에서 Slack notifier 호출 누락 시 운영자가 실패를 즉시 인지하지 못하는 위험, mitigation = Step Functions Catch 경로의 `DAILY_EXECUTION_FAILED` 발송 + test-only 실패 주입 수신 검증, Status `Mitigated`).

신규 R-AUTO-024(Slack webhook URL 을 Lambda 환경변수에 장기 보관 시 secret 관리 정책이 약해지는 위험, mitigation = 현재 환경변수는 1차 검증용 · 운영 안정화 후 Secrets Manager 또는 SSM Parameter Store 이전 · 문서에는 webhook URL 평문 기록 금지, Status `Accepted` — 운영 안정화 후 이전 진행).

본 결정에서도 Slack webhook URL / Lambda 환경변수 값 / Lambda 코드 본문 / Slack 메시지 전체 본문 / Step Functions execution history · ASL 본문 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

운영 식별자(Lambda 이름 `portfolio-event-notifier` / IAM Role 이름 `portfolio-event-notifier-lambda-role` / Runtime `Python 3.12` / 환경변수 key `SLACK_WEBHOOK_URL`(값 미기록) /
Slack 이벤트 라벨 `APPROVAL_REQUIRED` · `DAILY_EXECUTION_SUCCESS` · `DAILY_EXECUTION_FAILED` / 완료 marker `SLACK_LAMBDA_SMOKE_TEST=SUCCESS` · `PORTFOLIO_EVENT_NOTIFIER_TEMPLATE_TEST=SUCCESS` / smoke 메시지 `hello wook` /
Slack 봇 이름 `Portfolio Daily Bot`) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS Lambda / IAM / Step Functions / Slack webhook 호출은 모두 운영자 직접 수행 — Kiro 는 문서 작성 / 절차 정리만 수행. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건. Lambda 코드 / Step Functions Catch state ASL / Slack 메시지 본문 평문 인용 0건.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역) — 기존 port-view SlackNotificationService 본문 / 신규 Lambda 코드 / 환경변수 값 / Slack webhook URL 본문 인용 0건. **6. Slack 구현: 완료 / 7.

EventBridge 자동화는 다음 단계** 운영 인계 명시 — 1차 EventBridge / Step Functions 연동 Slack 범위는 3종(`APPROVAL_REQUIRED` + `DAILY_EXECUTION_SUCCESS` + `DAILY_EXECUTION_FAILED`) 한정 / 장 전 잔고 · 장 후 잔고 · 장중 손절 Slack 은 후속 분리. Decision Summary 카운트 갱신(전체 88 → 90 / 확정 44 → 45 / 잠정 41 → 42).


### Change Log Details 2026-06-23 (3차) <a id="change-log-details-2026-06-23-3"></a>

변경 요약: OD-MS-032 신규 + OD-MS-009 / OD-MS-029 / OD-MS-031 1차 실증 보강 (EventBridge Scheduler + Dispatcher Lambda 기반 Daily 자동화 구현 완료)

2026-06-23 두 번째 항목(AWS 공통 Slack notifier 구현 완료 + Step Functions 3종 Slack 검증) 후속으로 같은 일자 운영자가 직접 수행한 **7. EventBridge 자동화 구현: 완료** 결과를 반영.

신규 결정: OD-MS-032(EventBridge Scheduler + Dispatcher Lambda 기반 Daily 자동화 구조 = (a) 2개 schedule 분리 + Dispatcher Lambda 호출 + 단계적 활성화 / (b) Scheduler 가 Step Functions 직접 시작이 아니라 Dispatcher Lambda(`portfolio-paper-daily-scheduler-dispatcher` / Runtime Python 3.12 /
Handler `lambda_function.lambda_handler` / Timeout 30s / Memory 256MB / State Active) 호출 /

Lambda 가 KST `runDate`(YYYY-MM-DD) 생성 + 주말 · 휴장일 skip + scheduleType 별 payload 분기 + Step Functions `StartExecution` 호출 + 즉시 종료(Step Functions 완료 대기 없음) /
(c) 08:00 KST schedule `portfolio-paper-daily-step1-11-approval-0800-kst`(cron `cron(0 8 ? * MON-FRI *)` / Asia/Seoul / Flexible OFF / Target Lambda 동일 / Target input `{"scheduleType":"STEP1_11_APPROVAL","dryRun":false}` / **State ENABLED** /

`allowPaperOrderExecute=false` / Step 12 approval gate 차단 / 주문 제출 없음) /
(d) 09:01 KST schedule `portfolio-paper-daily-step12-17-order-0901-kst`(cron `cron(1 9 ? * MON-FRI *)` / Asia/Seoul / Flexible OFF / Target Lambda 동일 / Target input `{"scheduleType":"STEP12_17_ORDER","dryRun":false}` / **State DISABLED** /
`allowPaperOrderExecute=true` / 주문 자동화 ENABLE 전 최종 안전 점검 후 별도 판단) /

(e) Scheduler IAM Role `portfolio-paper-eventbridge-scheduler-role`(trust `scheduler.amazonaws.com`) 에 `portfolio-paper-scheduler-start-execution-policy`(Step Functions 2개 state machine `states:StartExecution` 한정) +
`portfolio-paper-scheduler-invoke-dispatcher-policy`(Dispatcher Lambda `lambda:InvokeFunction` 한정) 부착 /

Lambda Role `portfolio-paper-daily-scheduler-dispatcher-role`(trust `lambda.amazonaws.com`) 에 `portfolio-paper-daily-scheduler-dispatcher-policy`(CloudWatch Logs `CreateLogGroup` · `CreateLogStream` · `PutLogEvents` + Step Functions 2개 state machine
`states:StartExecution` 한정) 부착 / `simulate-principal-policy` 통과 / Resource · Action wildcard 0건 /

(f) Lambda 환경변수 = `TIMEZONE=Asia/Seoul` · `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true` · Step1~17 State Machine ARN · Step12~17 State Machine ARN(ARN 평문 기록 0건 / state machine 이름만 사실 기록) /
(g) 대상 Step Functions 2개 모두 ACTIVE 확인 — `portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval` /

(h) Lambda dryRun 검증 통과 — 08:00 dryRun(`scheduleType=STEP1_11_APPROVAL`) → `started=false` / `reason=DRY_RUN_NO_START_EXECUTION` / `target=portfolio-paper-daily-step1-17-approval` / `allowPaperOrderExecute=false` /
09:01 dryRun(`scheduleType=STEP12_17_ORDER`) → `started=false` / `reason=DRY_RUN_NO_START_EXECUTION` / `target=portfolio-paper-daily-step12-17-approval` /

`allowPaperOrderExecute=true` / (i) 실제 `StartExecution`(`dryRun=false`) 은 본 일자 미검증 — 내일 08:00 schedule 실행 시 첫 검증 예정, 🟢 확정 / 영향 spec 04 · 05 · 10).

OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) 본문 변경 없이 1차 실증 메모 보강 — EventBridge Scheduler 정기 트리거 진입이 본 일자에 1차 구현 / Dispatcher Lambda 가 input 보정 / 휴장일 guard / StartExecution dispatcher 역할로 추가 / Status 기존 값 그대로 유지.

OD-MS-029(Step Functions approval workflow false / true path) 본문 변경 없이 1차 실증 메모 보강 — 본 결정의 "동일 state machine + 입력 파라미터 분기" 의미는 `allowPaperOrderExecute` 분기 차원에서 그대로 유지 / OD-MS-032 의 schedule + state machine 2개 분리는 EventBridge 자동화 운영 안전성(주문 자동화 단계 분리) 관점의 운영 분리 / Status 기존 값 그대로 유지.

OD-MS-031(1차 Slack 연동 범위 3종 한정) 본문 변경 없이 1차 실증 메모 보강 — Dispatcher Lambda → Step Functions StartExecution → 3종 Slack(`APPROVAL_REQUIRED` / `DAILY_EXECUTION_SUCCESS` / `DAILY_EXECUTION_FAILED`) 의 정기 트리거 경로가 본 결정으로 운영 자동화 구조 안에서 완결 / 본 일자에는 dryRun 까지만 검증 / 실제 첫 발송은 내일 08:00 첫 실행 예정.

보강 / 신규 리스크: 신규 R-AUTO-025(Scheduler / Dispatcher Lambda / Step Functions 연결 실패로 Daily 자동 검증이 시작되지 않는 위험, mitigation = IAM `simulate-principal-policy` 검증 + Lambda dryRun 검증 + Scheduler `get-schedule` 상태 확인 + 08:00 ENABLED · 09:01 DISABLED 단계적 적용, Status `Mitigated`).

본 결정에서도 Lambda 코드 본문 / Slack webhook URL / Step Functions ASL 본문 / 실제 ARN 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

운영 식별자(Scheduler 이름 2종 / Dispatcher Lambda 이름 / IAM Role 이름 4종 / Inline policy 이름 3종 / state machine 이름 2종 / cron 표현식 / Asia/Seoul / scheduleType 라벨 2종 / Target input JSON `{"scheduleType":"...","dryRun":false}` / Lambda 환경변수 key 3종 /
dryRun 응답 라벨 `started=false` · `reason=DRY_RUN_NO_START_EXECUTION`) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS EventBridge Scheduler / Lambda / IAM / Step Functions 작업은 모두 운영자 직접 수행 — Kiro 는 문서 작성 / 절차 정리만. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건.

CloudWatch Logs 본문 / Scheduler `get-schedule` 응답 본문 / `simulate-principal-policy` 응답 본문 / Lambda dryRun 응답 전문 / Step Functions execution history 본문 평문 인용 0건. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역).

**후속 = 내일 08:00 Scheduler 첫 실행 결과 확인** — Scheduler invocation / Lambda CloudWatch Logs / Step Functions execution 생성 / `APPROVAL_REQUIRED` Slack 수신 / Step 12 approval gate 차단 / 주문 제출 없음 점검 / 09:01 Scheduler ENABLE 여부는 08:00 자동 검증 누적 결과 + 별도 안전 점검 후 판단.

Decision Summary 카운트 갱신(전체 90 → 91 / 확정 45 → 46 / 잠정 42 유지).


### Change Log Details 2026-06-24 (1차) <a id="change-log-details-2026-06-24-1"></a>

변경 요약: OD-MS-033 신규 + OD-MS-028 / OD-MS-032 / OD-SAFE-001 ~ OD-SAFE-004 1차 실증 보강 (08:00 Scheduler 실제 실행 검증 + Step 12 retry-normalizer `EGW00201` 확장 + Step 12~17 수동 실행 4건 FILLED)

2026-06-23 EventBridge 자동화 리소스 구현 완료(OD-MS-032) 후속으로 2026-06-24 오전 운영자가 직접 수행한 (a) 08:00 schedule 실제 실행 검증 + (b) Step 12 retry-normalizer 의 KIS `EGW00201`(rate limit) 재시도 대상 확장 + sleep · backoff 보완 + (c) Step 12~17 수동 Dispatcher invoke 후 4건 전량체결 검증 결과를 반영.

신규 결정: OD-MS-033(Step 12 retry-normalizer 재시도 대상 확장 `40580000` + `EGW00201` + KIS rate-limit backoff retry 정책 + 09:01 schedule 자동 ENABLE 별도 운영자 승인 보류 / `EGW00201` 은 broker 주문 내용 오류가 아니라 KIS gateway 측 rate limit 이므로 동일 주문 자동 재시도 허용 단 즉시 반복 제출 아닌 sleep · backoff retry 적용 /

`port-marketconnector/connector_strategy_order_execute.py` 전체 교체 + 주문 사이 기본 sleep + `EGW00201` backoff 재시도 + `submit_attempts` `result_payload` 기록 / S3 업로드 · EC2 정식 배포 · `py_compile` · 운영 마커 확인 완료 / dry-run `candidate_count=4` 확인 /
수동 실행 후 4건 새 `connector_order_request` + `broker_order_no` 생성 + 최종 체결 (`042660` 한화오션 FILLED / `004990` 롯데지주 FILLED / `003490` 대한항공 FILLED / `023530` 롯데쇼핑 FILLED) /

`004990` 단건 order-check 재조회 후 `tot_ccld_qty=69` 전량체결 확인 / 09:01 schedule 자동 ENABLE 별도 운영자 승인 보류, 🟢 확정 / 영향 spec 03 · 04 · 05 · 10).

OD-MS-028(Step 12 retry-normalizer 내장 정책) 본문 변경 없이 1차 실증 메모 보강 — 복구 조건 6종 중 `rejection_code = 40580000` 한정 조건이 OD-MS-033 으로 `rejection_code IN (40580000, EGW00201)` 확장 / `broker_order_no IS NULL` + `connector_fill` 없음 조건 그대로 유지 / R-AUTO-001 /
R-BROKER-004 의 중복 주문 위험 우회 0건 / 본 일자 4건 재시도에서 broker 중복 주문 0건 1차 실증.

OD-MS-032(EventBridge Scheduler + Dispatcher Lambda 기반 Daily 자동화 구조) 본문 변경 없이 1차 실증 메모 보강 — 08:00 schedule `portfolio-paper-daily-step1-11-approval-0800-kst` 실제 호출 통과 / Scheduler invocation 1건 /
Dispatcher Lambda 가 Step Functions `portfolio-paper-daily-step1-17-approval` execution 생성 / Step 1~11 수행 후 approval-required 흐름으로 완료 / `APPROVAL_REQUIRED` Slack 수신 / Step 12 이후 주문 차단 /

`connector.connector_order_request` · `execution.strategy_execution_order` 2026-06-24 08:00 자동 실행 기준 신규 주문 제출 0건 / 09:01 schedule `DISABLED` 유지(R-AUTO-025 mitigation 1차 실증).

OD-SAFE-001 ~ OD-SAFE-004(자동 BUY · SELL E2E 단계적 도입 / 자동 재시도 금지 idempotent 한정) 본문 변경 없이 1차 실증 메모 보강 — 08:00 schedule 의 `allowPaperOrderExecute=false` 가 Step 12 approval gate 차단으로 정합 /
Step 12 retry-normalizer 의 `EGW00201` 자동 재시도 확장은 `broker_order_no IS NULL` + `connector_fill` 없음 조건 안에서만 동작 / 09:01 schedule 자동 ENABLE 별도 운영자 승인 보류 정책 정합.

보강 / 신규 리스크: R-AUTO-025 mitigation 에 [2026-06-24 보강] 메모 — 08:00 Scheduler 첫 실 실행 통과 / Scheduler · Dispatcher Lambda · Step Functions 연결 사슬 정상 / Status `Mitigated` 그대로 유지 / 09:01 schedule 자동 ENABLE 별도 운영자 승인 보류 정책 유지.

신규 R-AUTO-026(KIS `EGW00201` rate limit 으로 주문 일부가 REJECTED 되고 다음 실행에서 재시도되지 않는 위험, mitigation = retry-normalizer 대상에 `EGW00201` 추가 + 주문 사이 sleep + backoff retry + dry-run 및 실제 재주문 검증 통과, Status `Mitigated`).

신규 R-AUTO-027(`APPROVAL_REQUIRED` Slack summary 가 실제 Step · DB 요약을 반영하지 못하고 0/0 으로 표시되어 운영자가 실제 후보 / 결과를 오해할 위험, mitigation = 후속에서 Slack approval payload builder 개선 예정 / 단기적으로는 운영자가 Step Functions execution history + DB 직접 점검으로 사후 검증 보완, Status `Accepted`).

본 결정에서도 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN / broker 응답 전문 /
webhook URL / KIS paper login credential / Administrator password 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

운영 식별자(Scheduler 이름 2종 / Dispatcher Lambda 이름 / Step Functions state machine 이름 2종 / `connector_strategy_order_execute.py` 운영자 직접 patch 사실 / dry-run `candidate_count=4` / rejection_code `40580000` · `EGW00201` /
종목 코드 `042660` · `004990` · `003490` · `023530` / 종목명 / `004990` PARTIAL_FILLED → FILLED 전환 / `tot_ccld_qty=69`) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / SSM / EC2 / RDS / Step Functions / EventBridge Scheduler / Lambda / Slack webhook / S3 작업은 모두 운영자 직접 수행 — Kiro 는 본 일자 루트 / `_common` 문서 갱신만 수행. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건.

CloudWatch Logs · SSM 응답 · KIS API response body · Step Functions execution history · Lambda 응답 본문 · `connector_strategy_order_execute.py` patch 본문 평문 인용 0건.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역) — 운영자 직접 patch / EC2 배포 한 `connector_strategy_order_execute.py` 전체 교체 변경분은 03 spec operation-notes 후속 갱신 책임.

**남은 후속** = (a) `APPROVAL_REQUIRED` Slack summary 0/0 표시 개선(R-AUTO-027 mitigation 확장), (b) Dispatcher Lambda application log 보강, (c) 09:01 Scheduler 자동 ENABLE 최종 판단 보류, (d) Slack webhook URL Secrets Manager · SSM Parameter Store 이전(R-AUTO-024 정합),
(e) balance refresh EGW00215 rate-limit backoff · retry policy 는 기존 후속 유지(2026-06-23 followups §1 정합).

Decision Summary 카운트 갱신(전체 91 → 92 / 확정 46 → 47 / 잠정 42 유지).


### Change Log Details 2026-06-24 (2차) <a id="change-log-details-2026-06-24-2"></a>

변경 요약: OD-MS-034 신규 + OD-MS-009 / OD-MS-032 / OD-MS-001 / OD-NET-003 1차 실증 보강 (EC2 lifecycle 자동 실행 구현 완료)

2026-06-24 첫 번째 항목(08:00 Scheduler 실 실행 검증 + Step 12 retry-normalizer `EGW00201` 확장 + Step 12~17 수동 실행 4건 FILLED) 후속으로 같은 일자 운영자가 직접 수행한 **EC2 자동 실행 구현 완료** 결과를 반영.

신규 결정: OD-MS-034(EC2 lifecycle 자동 실행 구성 = (a) EventBridge Scheduler + Lambda 기반 EC2 start/stop + (b) 07:50 EventBridge Scheduler `portfolio-paper-ec2-start-0750-kst` ENABLED + (c) Step Functions Step 1~11 성공 시 Crawler stop + (d) 15:50 EventBridge Scheduler
`portfolio-paper-marketconnector-stop-1550-kst` ENABLED + (e) start 요청에만 휴일 체크 /

stop 요청에는 휴일 체크 미적용 + (f) Lambda 역할 3분리(`portfolio-paper-daily-scheduler-dispatcher` · `portfolio-paper-ec2-lifecycle-dispatcher` · `portfolio-event-notifier`) 명확화, 🟢 확정 / 영향 spec 03 · 04 · 05 · 08 · 10).

핵심 운영 식별자 — EC2 lifecycle Lambda `portfolio-paper-ec2-lifecycle-dispatcher`(EC2 start/stop 전담 / 07:50 start 에만 휴일 체크 / stop 요청 미적용 / `TIMEZONE=Asia/Seoul` · `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true` / 휴일 API 장애 시 fail-closed /
IAM Role 생성 + EC2 start · stop 권한 부여 완료) / 07:50 Scheduler `portfolio-paper-ec2-start-0750-kst`(cron `cron(50 7 ? * MON-FRI *)` / Asia/Seoul /

Flexible OFF / Target Lambda 동일 / Target input `{"action":"start","target":"BOTH","holidayCheck":true,"reason":"PRE_DAILY_STEP1_11","dryRun":false}` / State `ENABLED`) /
15:50 Scheduler `portfolio-paper-marketconnector-stop-1550-kst`(cron `cron(50 15 ? * MON-FRI *)` / Asia/Seoul / Flexible OFF / Target Lambda 동일 /

Target input `{"action":"stop","target":"MARKETCONNECTOR","holidayCheck":false,"reason":"POST_MARKET_CLOSE","dryRun":false}` / State `ENABLED`).

Step Functions `portfolio-paper-daily-step1-17-approval` ASL 백업 + update 통과 — `Step6ToStep11_Succeeded` → `StopCrawlerEc2AfterStep11Success`(Target Lambda `portfolio-paper-ec2-lifecycle-dispatcher` /
Input `{"action":"stop","target":"CRAWLER","holidayCheck":false,"reason":"STEP1_11_SUCCESS","dryRun":false,"runDate.$":"$.runDate"}`) → `SendApprovalRequiredSlack` → `Step12_CheckApproval` 흐름.

Step 1~11 실패 시 → `SendDailyExecutionFailedSlack`(Target Lambda `portfolio-event-notifier` / Input `{"eventType":"DAILY_EXECUTION_FAILED","reason":"STEP1_11_FAILED"}` / Crawler EC2 디버깅 위해 유지).

State Machine `ACTIVE` / RevisionId `edd92cc9-1d94-4752-9a43-b7eb5b2f3c2c` / Step Functions execution role 에 EC2 lifecycle Lambda invoke 권한 추가 완료 / Scheduler invoke Role 생성 + Lambda invoke 권한 부여 완료(Resource · Action wildcard 0건).

검증 통과 — Lambda source 생성 + `py_compile` + IAM Role + EC2 start · stop 권한 + Lambda create + dryRun 3종(`start BOTH` + `stop CRAWLER` + `stop MARKETCONNECTOR`) + `runDate=2026-06-27` 주말 skip + 두 Scheduler `get-schedule` 응답 정합(State `ENABLED` /
`FlexibleTimeWindow Mode=OFF` / Target Lambda · Target input 정합).

OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) 본문 변경 없이 1차 실증 메모 보강 — Daily Batch orchestration 에 EC2 lifecycle 자동화가 연결 / Lambda 는 본 표의 주 compute 가 아니라 orchestration 보조 계층 유지 /
Step Functions + EventBridge Scheduler + Dispatcher Lambda + EC2 lifecycle Lambda + ECS RunTask + SSM RunCommand + AWS Batch 의 6계층 조합으로 확장(OD-MS-009 / OD-MS-032 본문 그대로 유지).

OD-MS-032(EventBridge Scheduler + Dispatcher Lambda 기반 Daily 자동화 구조) 본문 변경 없이 1차 실증 메모 보강 — EC2 lifecycle dispatcher Lambda 는 기존 Daily scheduler dispatcher Lambda 와 별도 책임 / 휴일 체크 방식은 동일하게 이식 / Resource · Action wildcard 0건 정합 유지.

OD-MS-001(port-marketconnector 컴퓨트 = EC2+EIP) / OD-NET-003(marketconnector outbound IP = EC2+EIP) 본문 변경 없이 1차 실증 메모 보강 — MarketConnector EC2 의 07:50 start / 15:50 stop / EIP attach 상태 유지 / KIS broker 측 IP 등록 변경 0건 / R-BROKER-001 mitigation 회귀 0건.

보강 / 신규 리스크: 신규 R-AUTO-028(EC2 lifecycle Scheduler 또는 Lambda 설정 오류로 장 전 EC2 가 기동되지 않거나 장 후 EC2 가 종료되지 않는 위험, mitigation = 07:50 start Scheduler ENABLED + 15:50 stop Scheduler ENABLED + Lambda dryRun 3종 검증 + 주말 skip 검증 + start 요청에만 휴일 체크 + stop 요청 미적용 + Step
1~11 성공 후 Crawler stop 을 Step Functions 경로에 연결, Status `Mitigated`).

R-AUTO-025 mitigation 에 [2026-06-24 두 번째 보강] 메모 추가 — 08:00 Scheduler 실 실행 검증 이후 EC2 lifecycle 자동화(07:50 start + 15:50 stop + Step Functions Step 1~11 성공 시 Crawler stop) 까지 ENABLED 됨 / Scheduler · Dispatcher Lambda · Step Functions · EC2 lifecycle Lambda 연결 사슬 정상 / Status `Mitigated` 유지.

본 결정에서도 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN /
broker 응답 전문 / Slack webhook URL / KIS paper login credential 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

운영 식별자(Lambda 이름 `portfolio-paper-ec2-lifecycle-dispatcher` / Scheduler 이름 `portfolio-paper-ec2-start-0750-kst` · `portfolio-paper-marketconnector-stop-1550-kst` / Step Functions state 이름 `StopCrawlerEc2AfterStep11Success` /
RevisionId `edd92cc9-1d94-4752-9a43-b7eb5b2f3c2c` / cron 표현식 `cron(50 7 ? * MON-FRI *)` · `cron(50 15 ? * MON-FRI *)` / scheduleType action 라벨 `start` · `stop` /

target 라벨 `BOTH` · `CRAWLER` · `MARKETCONNECTOR` / reason 라벨 `PRE_DAILY_STEP1_11` · `STEP1_11_SUCCESS` · `POST_MARKET_CLOSE` · `STEP1_11_FAILED` / runDate `2026-06-27` 주말 skip 검증) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / EventBridge Scheduler / Lambda / Step Functions / IAM / EC2 / SSM 작업은 모두 운영자 직접 수행 — Kiro 는 본 일자 루트 / `_common` 문서 갱신만 수행. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건. Lambda 코드 본문 / IAM Policy 전체 본문 / Step Functions ASL 전체 본문 / Lambda 응답 전문 / CloudWatch Logs 전문 평문 인용 0건.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역).

**남은 후속** = (a) 다음 영업일 07:50 실 start 관찰, (b) 다음 영업일 Step 1~11 성공 후 Crawler stop 실 실행 관찰, (c) 15:50 MarketConnector stop 실 실행 관찰, (d) 09:01 Step 12~17 자동 ENABLE 여부는 기존 OD-MS-033 보류 정책 유지,
(e) Slack `APPROVAL_REQUIRED` summary 0/0 표시 개선(R-AUTO-027 mitigation 확장), (f) Dispatcher Lambda application log 보강, (g) Slack webhook URL Secrets Manager 또는 SSM Parameter Store 이전(R-AUTO-024 정합),
(h) balance refresh `EGW00215` rate-limit backoff · retry policy 는 기존 후속 유지.

Decision Summary 카운트 갱신(전체 92 → 93 / 확정 47 → 48 / 잠정 42 유지).


### Change Log Details 2026-06-24 (3차) <a id="change-log-details-2026-06-24-3"></a>

변경 요약: OD-MS-035 신규 + OD-MS-001 / OD-MS-007 / OD-MS-009 / OD-MS-016 / OD-MS-021 / OD-MS-032 / OD-MS-033 / OD-MS-034 / OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없이 메모 보강 (장중 포지션 확인 3단계 구조 최종안 확정 / 문서 반영)

2026-06-24 두 번째 항목(EC2 lifecycle 자동 실행 구현 완료) 후속으로 같은 일자 운영자가 정의한 **5. 장중 포지션 확인** 최종 설계안을 spec 문서에 반영(실제 AWS 리소스 생성 · 수정 · 삭제 0건 / 소스 수정 0건 / Kiro 는 문서 갱신만 수행 / 1·2단계는 다음 영업일 후속 구현 예정 / 3단계는 초기 수동 · 승인 후 별도 Step Functions 진입).

신규 결정: OD-MS-035(장중 포지션 확인 3단계 구조 = (a) 1단계 MarketConnector 10분 Snapshot Refresh: EventBridge Scheduler → Lambda → SSM → MarketConnector EC2 / 잔고 · 보유종목 · 현재가 · 매도가능수량 갱신 / `connector_balance_snapshot` + `connector_position_snapshot` 저장 / 판단 · 주문 0건 / idempotent.

(b) 2단계 StrategyExecution Intraday Evaluate: 1단계 직후 SSM 실행 / `strategy_intraday_position_check` 저장 / stop candidate + `INTRADAY_STOP_SELL` `READY` order 생성 / broker 주문 제출 0건 / 안전장치 4종(stale snapshot · duplicate order · `sellable_qty` · `current_price` 검증) /
`daily_intraday_position_monitor_run.py` 수정 없이 신규 파일로 구현 / Step Functions Retry 정책에서 자동 주문 제출과 연결되지 않도록 ASL 정의 시 명시.

(c) 3단계 Intraday Stop Sell Submit & Refresh: 별도 Step Functions state machine / `source_type=INTRADAY_STOP_SELL` 전용 필터 / `READY` 손절 주문 제출 + 체결조회 + 포지션 sync + 잔고 refresh + Slack 알림 / 초기에는 수동 · 승인 후 실행 / Daily BUY/SELL 주문과 분리.

(d) 장중 전용 경로 분리 — 기존 Daily Step 1~17 / 08:00 · 09:01 Scheduler / EC2 lifecycle 자동화와 충돌하지 않도록 분리 / `source_type=INTRADAY_STOP_SELL` 필터를 Submit & Refresh entrypoint + Slack payload 양쪽에서 명시.

(e) 신규 / 보강 후보 — 1단계 EventBridge Scheduler 신규 / 1단계 dispatcher Lambda 신규(OD-MS-032 / OD-MS-034 의 dispatcher 패턴 재사용 / Lambda 는 본 spec 의 주 compute 가 아니라 orchestration helper 역할 / OD-MS-009 / OD-MS-030 본문 변경 없음) /
2단계 SSM step 은 MarketConnector EC2 의 venv python 으로 신규 evaluate 파일 실행 / 3단계 별도 Step Functions state machine 정의는 후속 phase 책임, 🟢 확정 / 영향 spec 03 · 04 · 05 · 10).

OD-MS-001(port-marketconnector 컴퓨트 = EC2+EIP) 본문 변경 없이 1차 실증 메모 보강 — MarketConnector EC2 가 장중 10분 Snapshot Refresh 의 SSM 실행 대상 / EIP attach 상태 유지 / KIS broker 측 IP 등록 변경 0건 / R-BROKER-001 mitigation 회귀 0건.

OD-MS-007(`port_strategy_execution` 컴퓨트 = ECS Fargate Task + Step Functions + EventBridge Scheduler) 본문 변경 없이 메모 보강 — StrategyExecution Intraday Evaluate 는 신규 파일로 SSM 실행 / Step Functions Retry 정책에서 자동 주문 제출과 연결되지 않도록 ASL 분리 / OD-SAFE-004 정합.

OD-MS-009(Daily Batch orchestration) 본문 변경 없이 메모 보강 — 장중 포지션 확인이 Daily Batch orchestration 6계층 조합과 분리된 장중 전용 경로로 추가 / Step Functions + EventBridge Scheduler + Lambda dispatcher + SSM RunCommand 패턴 재사용 / Lambda 는 주 compute 가 아니라 orchestration helper.

OD-MS-016(Strategy Execution / MarketConnector 책임 분리) 본문 변경 없이 메모 보강 — 1단계는 MarketConnector 책임(broker 호출 + DB snapshot 갱신) / 2단계는 StrategyExecution 책임(판단 + `READY` order 생성) / 3단계는 MarketConnector 책임(broker 주문 제출 + 체결조회 + 잔고 refresh) /
Strategy Execution `READY -> REQUESTED` 책임 경계 그대로 유지 — 단, Intraday 경로에서는 `READY` order 가 `INTRADAY_STOP_SELL` `source_type` 으로 별도 분류되어 Daily BUY/SELL 흐름과 충돌하지 않음.

OD-MS-021(17단계 안전 기준) 본문 변경 없이 메모 보강 — 17단계 안전 기준의 (a) 실제 BUY · SELL 실행 금지 · (b) `--execute` 주문 전송 계열 실행 금지 · (c) fill · position sync 자동 재시도 금지 · (d) aws-live 작업 금지 정책이 본 OD-MS-035 의 1·2단계 신규 entrypoint 에도 동일하게 적용 / 3단계 Submit & Refresh 의 자동 ENABLE 시점도 OD-MS-033 의 09:01 보류 패턴과 동일하게 운영자 승인 게이트로 운영.

OD-MS-032(EventBridge Scheduler + Dispatcher Lambda) / OD-MS-033(Step 12 retry-normalizer + 09:01 보류) /
OD-MS-034(EC2 lifecycle 자동 실행) 본문 변경 없이 메모 보강 — 1단계 Scheduler 의 휴일 · 주말 guard 는 OD-MS-032 / OD-MS-034 의 holiday guard 패턴 동일 이식 / Dispatcher Lambda 환경변수(`TIMEZONE=Asia/Seoul` · `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true`) 정합 /
3단계 자동 ENABLE 보류 정책은 OD-MS-033 의 09:01 보류와 동일한 운영자 승인 게이트.

OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없이 메모 보강 — 자동 BUY · SELL E2E 단계적 도입 + 자동 재시도 금지 (idempotent 한정) 정책이 본 결정의 1·2·3단계 모두에 적용 / 1단계는 idempotent refresh / 2단계는 `READY` order 생성만 / 3단계는 운영자 승인 후 broker 제출.

신규 / 보강 리스크: 신규 R-DATA-014(stale snapshot 기반 잘못된 손절 판단 위험, Status `Mitigated` / 1단계 refresh `as_of_ts` 검증 + 2단계 evaluate 진입 전 snapshot 최신성 검증). 신규 R-AUTO-029(duplicate `INTRADAY_STOP_SELL` `READY` order 생성 위험, Status `Mitigated` / 동일 종목 · `source_type` 사전 점검 + idempotency 키 도입 검토).

신규 R-DATA-015(`sellable_qty` 부족 상태에서 손절 주문 제출 위험, Status `Mitigated` / evaluate 단계 사전 비교 + 부족 시 READY 생성 차단). 신규 R-DATA-016(`current_price` 누락 또는 비정상 가격 기반 판단 위험, Status `Mitigated` / refresh 시 null · 0 · 과거 timestamp 검증).

신규 R-AUTO-030(3단계 Submit & Refresh Step Functions 가 너무 일찍 ENABLE 되어 승인 없이 주문 제출 위험, Status `Open` — 초기 `DISABLED` 유지 정책은 설계 단계이며 실제 state machine 미정의 / 정식 구현 + 검증 통과 후 `Mitigated` 승격 후보).

본 결정에서도 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN /
broker 응답 전문 / Slack webhook URL / KIS API response body / CloudWatch Logs 전문 / Lambda 응답 전문 / Step Functions ASL 전체 본문 / IAM policy 전체 본문 / patch 전문 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

운영 식별자(저장 테이블 `connector_balance_snapshot` · `connector_position_snapshot` · `strategy_intraday_position_check` / `source_type=INTRADAY_STOP_SELL` / `READY` order status / 안전장치 4종 라벨 `stale snapshot · duplicate order · sellable_qty · current_price` /
1·2·3단계 분리 라벨) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

**본 결정은 최종 설계안 확정 / 문서 반영 단계** — 실제 AWS / Lambda / SSM / Step Functions / RDS / KIS API 실행 0건 / AWS 리소스 생성 · 수정 · 삭제 0건 / 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역).

운영자가 후속 구현 시점에 신규 evaluate 파일(`daily_intraday_position_monitor_run.py` 와 별도) 직접 작성 / Lambda dispatcher 신규 직접 작성 / Step Functions state machine 정식 정의 / SSM Document / IAM policy / EventBridge Scheduler 신규 직접 생성 — 본 결정은 결정 락만 다루고 03 · 04 spec operation-notes 후속 갱신은 후속 phase 책임.

**남은 후속** = (a) 다음 영업일 1단계 MarketConnector 10분 Snapshot Refresh Scheduler · Lambda · SSM 설계 + 구현, (b) 다음 영업일 2단계 StrategyExecution 신규 intraday evaluate 파일 구현 + `strategy_intraday_position_check` 저장 + stop candidate · `INTRADAY_STOP_SELL` `READY` order 생성 검증,
(c) 3단계 Intraday Stop Sell Submit & Refresh 별도 Step Functions 설계 + Slack 알림 연계(자동 ENABLE 보류 /

초기 수동 · 승인 후 실행), (d) 03 · 04 · 05 · 06 · 10 spec 하위 문서 갱신 후속 / 05 spec View Daily Batch 화면 연동 후속(stop candidate · `READY` order · 제출 결과 · Slack 알림 표시), (e) 06 spec IAM 권한 매트릭스에 Lambda · SSM · Step Functions · EC2 role 권한 추가 후속(실제 IAM 변경은 후속 phase),
(f) 10 spec cutover-and-validation-runbook 에 aws-paper 장중 포지션 확인 → 수동 승인 제출 → 체결조회 → sync → 잔고 refresh 검증 시나리오 추가.

Decision Summary 카운트 갱신(전체 93 → 94 / 확정 48 → 49 / 잠정 42 유지).


### Change Log Details 2026-06-25 (1차) <a id="change-log-details-2026-06-25-1"></a>

변경 요약: OD-MS-036 신규 + OD-MS-035 [2026-06-25 1·2단계 구현 + 3단계 State Machine 생성 + no-target 검증 통과] 보강 + OD-MS-001 / OD-MS-007 / OD-MS-009 / OD-MS-016 / OD-MS-021 / OD-MS-032 / OD-MS-033 / OD-MS-034 / OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없이 메모 보강 (장중 포지션 Step Function 구현 완료 / 실주문 테스트 보류)

2026-06-24 세 번째 항목(장중 포지션 확인 3단계 구조 최종안 확정) 후속으로 2026-06-25 운영자가 직접 수행한 **4. 장중 포지션 Step Function 구현 완료** 결과를 반영.

신규 결정: OD-MS-036(Intraday Stop Sell Submit Workflow = (a) Daily Step 12 와 분리된 별도 Step Functions state machine `portfolio-paper-intraday-stop-sell-approval`(type `STANDARD` / status `ACTIVE` / 18-state /

creationDate `2026-06-25T14:58:45+09:00`) + (b) 수동 승인 운영(`allowIntradayStopOrderExecute=true` 입력값 한정) + (c) `INTRADAY_STOP_SELL` 전용 `signal_type` 필터 + (d) 실행 command `connector_strategy_order_execute.py --execute --intraday-stop-only` + (e)
`connector_strategy_order_execute.py` 의 `--signal-type` · `--intraday-stop-only` 신규 옵션 + `fetch_requested_strategy_orders()` · `normalize_retryable_rejected_orders()` 의 `signal_type` 필터 + (f) 자동 트리거 /

Slack 연계는 후속 phase 책임, 🟢 확정 / 영향 spec 03 · 04 · 05 · 10).

핵심 운영 식별자 — State Machine arn `arn:aws:states:ap-northeast-2:[REDACTED]:stateMachine:portfolio-paper-intraday-stop-sell-approval` / roleArn `arn:aws:iam::[REDACTED]:role/portfolio-paper-stepfunctions-execution-role` /

state 11종(`CheckIntradayStopApproval` · `BlockedByIntradayStopApprovalGate` · `RunIntradayStopOrderExecute` · `GetIntradayStopOrderExecuteInvocation` · `RunConnectorOrderCheck` · `GetConnectorOrderCheckInvocation` · `RunSyncSellFill` ·
`RunConnectorBalanceRefresh` · `GetConnectorBalanceRefreshInvocation` · `IntradayStopWorkflowSucceeded` · `IntradayStopWorkflowFailed`).

1·2단계 구현·배포(`connector_intraday_snapshot_refresh.py` source_version `connector-intraday-snapshot-refresh-1.0.0` + `connector_intraday_position_evaluate.py` source_version `connector-intraday-position-evaluate-1.1.0`(`INTRADAY_STOP_SELL` `READY` 생성 로직 추가) +
wrapper `run_intraday_snapshot_and_evaluate.sh`) + 10분 Scheduler `portfolio-paper-intraday-snapshot-evaluate-10min-kst`(cron `cron(10/10 9-15 ? * MON-FRI *)` / Asia/Seoul) 자동 tick 검증 통과.

`connector_strategy_order_execute.py` 패치(`--signal-type` · `--intraday-stop-only` 신규 / `--intraday-stop-only` 사용 시 `action=SELL` + `signal_type=INTRADAY_STOP_SELL` 강제 / `--action BUY` 함께 사용 시 실패 / 잘못된 `signal_type` 실패 /
`fetch_requested_strategy_orders()` · `normalize_retryable_rejected_orders()` `signal_type` 필터 추가 /

`_submit_order()` payload `signal_type` 전달 보완)을 운영자가 직접 작업 + EC2 정식 배포(SSM command_id `b44d7c4e-c21c-48c0-a3c0-3a5572935577` + 운영 marker `DEPLOY_CONNECTOR_STRATEGY_ORDER_EXECUTE_INTRADAY_STOP_FILTER=SUCCESS`) + EC2 dry-run 통과(`--intraday-stop-only --limit 20`
와 `--action SELL --signal-type INTRADAY_STOP_SELL --limit 20` 모두 no-target 정상 + guard failure 정상).

검증 통과 — ASL `JSON_PARSE=SUCCESS` / `StateCount=18` / `validate-state-machine-definition result=OK` / IAM 검증(`ssm:SendCommand` · `ssm:GetCommandInvocation` · `ecs:RunTask` · `ecs:DescribeTasks` · `iam:PassRole` 모두 허용 / 추가 IAM 수정 0건) /
State Machine 생성 통과 + blocked gate 안전 테스트(`intraday-stop-blocked-gate-20260625-145928` / SUCCEEDED / `allowIntradayStopOrderExecute=false` / SSM · ECS 미실행 /

주문 제출 0건) + true-path no-target 안전 테스트(`intraday-stop-truepath-notarget-20260625-150146` / SUCCEEDED / `2026-06-25T15:01:46+09:00` ~ `15:02:53+09:00` / 모든 true-path state 진입 /
실행 전 후 비교에서 `INTRADAY_STOP_SELL` 0 rows · `READY/FAILED SELL` 0 rows · max `connector_order_request.id=56` 변동 없음 · `created_after_truepath_count=0` / 신규 `connector_order_request` 0건 / KIS 주문 제출 0건).

실제 1주 `INTRADAY_STOP_SELL` 주문 테스트는 KIS 보유 종목 0건 + `strategy_position_state OPEN` 0건 + KIS 잔고 refresh(SSM command_id `5b1e5757-ea93-4eb6-a7a0-6d5a38d87840` / `rt_cd=0` / `msg_cd=20310000` / `output1_count=0` / `output2_count=1` / `as_of_date=2026-06-25` /
`EMPTY_NORMAL`) 결과로 보유 종목 부재 확인 → 실주문 검증 중단·보류 / 다음 보유 포지션 발생 후 재개.

**운영 가능 상태** — 10분 Scheduler 는 snapshot/evaluate 및 후보 생성 가능 / `portfolio-paper-intraday-stop-sell-approval` State Machine 은 수동 승인 실행 가능 / no-target true-path 안전 검증 완료 / 자동 트리거·Slack 연동은 후속 단계.

OD-MS-035(장중 포지션 확인 3단계 구조 최종안) 본문 변경 없이 [2026-06-25 1·2단계 구현 + 3단계 State Machine 생성 + no-target 검증 통과] 메모 보강 — 1·2단계가 본 일자 구현 완료 / 3단계 State Machine 생성 완료 / 실제 1주 주문 테스트만 보유 종목 부재로 보류.

OD-MS-001 / OD-MS-007 / OD-MS-009 / OD-MS-016 / OD-MS-021 / OD-MS-032 / OD-MS-033 / OD-MS-034 / OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없이 1차 실증 메모 보강 — MarketConnector EC2 + EIP 정합 유지 / Strategy Execution / MarketConnector 책임 분리 정합 /
Daily Batch orchestration 과 분리된 장중 전용 경로 / 자동 BUY · SELL E2E 단계적 도입 + 자동 재시도 금지 정책 그대로 유지(3단계 자동 ENABLE 보류).

보강 / 신규 리스크: 신규 R-AUTO-031(Intraday Stop Sell State Machine approval gate misconfiguration 위험, Status `Mitigated` — blocked gate 안전 테스트 통과 + true-path 수동 승인 입력 한정 + Daily Step 12 와 분리 + `--intraday-stop-only` 필터).

신규 R-AUTO-032(Daily SELL 과 Intraday Stop SELL orders mixing 위험, Status `Mitigated` — `--signal-type` · `--intraday-stop-only` 신규 + EC2 배포 + guard 검증 통과 + `--action SELL` 단독 사용 금지).

신규 R-BROKER-005(Real intraday stop-sell order test 보유 없이 시도 위험, Status `Mitigated` — KIS `output1_count` + `strategy_position_state OPEN` + `connector_position_snapshot.sellable_quantity` 사전 점검 정책 + 2026-06-25 보유 종목 부재로 실주문 보류 + 없는 포지션 매도 주문 생성 금지).

R-AUTO-030(3단계 자동 ENABLE 보류 위험) mitigation 에 [2026-06-25 보강] 메모 추가 — State Machine 생성 + blocked gate + true-path no-target 검증 통과 / 자동 트리거 여전히 미연결 / 실제 1주 주문 테스트는 보유 종목 부재로 보류 / Status `Open` 유지(정식 자동 ENABLE 진입은 후속 phase + 운영자 별도 승인 후).

본 결정에서도 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM access key id / instance-id 본문 외 평문 / EIP / image digest full sha256 / task ARN / job ARN / broker 응답 전문 /
Slack webhook URL / KIS API response body / CloudWatch Logs 전문 / Lambda 응답 전문 / Step Functions ASL 전체 본문 / IAM policy 전체 본문 / patch 전문 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder 또는 사실 기록 가능 식별자.

운영 식별자(State Machine arn 의 account-id 부분 `[REDACTED]` 처리 외 사실 기록 / State Machine 이름 `portfolio-paper-intraday-stop-sell-approval` / Scheduler 이름 `portfolio-paper-intraday-snapshot-evaluate-10min-kst` / cron 표현식 / source_version 라벨 / sha256 해시 /
SSM command_id 2종(`b44d7c4e-c21c-48c0-a3c0-3a5572935577` · `5b1e5757-ea93-4eb6-a7a0-6d5a38d87840`) /

execution_name 2종(`intraday-stop-blocked-gate-20260625-145928` · `intraday-stop-truepath-notarget-20260625-150146`) /
state 이름 11종 / 운영 marker 라벨 5종 / KIS 응답 라벨 `rt_cd=0` · `msg_cd=20310000` · `output1_count=0` · `output2_count=1` · `EMPTY_NORMAL` / `connector_order_request.id=56` / scheduleType action `start` · `stop` 등) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / Step Functions / EventBridge Scheduler / Lambda / SSM / EC2 / IAM / RDS / KIS API 호출은 모두 운영자 직접 수행 — Kiro 는 본 일자 루트 / `_common` 문서 갱신만 수행 / AWS CLI / boto3 실행 0건 / AWS 리소스 생성 · 수정 · 삭제 0건(State Machine 신규 생성은 운영자 직접 작업) /
Lambda 코드 본문 / IAM Policy 전체 본문 / Step Functions ASL 전체 본문 / Lambda 응답 전문 / CloudWatch Logs 전문 / KIS API response body 평문 인용 0건 / SSM command stdout 의 mojibake 원문 인용 0건.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역) — 운영자 직접 patch 한 `port-marketconnector/connector_strategy_order_execute.py`(`--intraday-stop-only` 필터 추가) /
`port-marketconnector/connector_intraday_snapshot_refresh.py`(신규) / `port-marketconnector/connector_intraday_position_evaluate.py`(신규 1.0.0 + 1.1.0) /

`port-marketconnector/scripts/run_intraday_snapshot_and_evaluate.sh`(신규 wrapper) / `port-marketconnector/scripts/run_connector_balance_daily.sh`(KST 동적 run-date 적용) 변경분은 본 spec 산출물에 사실로만 기록(본문 전체 인용 0건 / R-DOCS-001 정합 / 03 spec operation-notes 후속 갱신 책임).

**남은 후속** = (a) 실제 보유 포지션 발생 후 1주 `INTRADAY_STOP_SELL` 주문 테스트 재개, (b) Slack 알림 개선(`INTRADAY_STOP_CANDIDATE` · `INTRADAY_STOP_SELL_SUBMITTED` · `INTRADAY_STOP_SELL_FILLED` 등 eventType 추가 / R-AUTO-027 mitigation 확장 결합),
(c) 자동 trigger 연결 여부 결정(EventBridge Scheduler 또는 다른 trigger 도입 시점은 운영 회차 누적 결과 점검 후), (d) 운영자 승인 UX 정리(05 spec View Daily Batch 화면 연동 후보 + 운영자 노트 / runbook 정리), (e) 03 · 04 · 05 · 10 spec 하위 문서 갱신은 후속 phase 책임.

Decision Summary 카운트 갱신(전체 94 → 95 / 확정 49 → 50 / 잠정 42 유지).


### Change Log Details 2026-06-27 (1차) <a id="change-log-details-2026-06-27-1"></a>

변경 요약: OD-MS-037 신규 + OD-MS-002 / OD-DB-009 / OD-ENV-006 / OD-ENV-007 / OD-NET-010 / OD-NET-011 / OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없이 메모 보강 (View Local AWS Paper read-only 1차 scope 완료 / ECS Fargate 이전 로컬 검증 통과)

2026-06-27 운영자가 직접 수행한 **View Local AWS Paper read-only 1차 scope 완료** 결과를 반영.

신규 결정: OD-MS-037(View Local AWS Paper read-only 1차 scope 확정 + ECS / Fargate 진입 전 batch 2차 검증(View Local Batch Step 1~17 실행 검증) 선행 정책 / Local Spring Boot 가 SSM Port Forwarding `127.0.0.1:15433` 경유로 AWS Paper RDS 조회 /

aws-paper profile 신규 + `view_app` DB user 유지 + Spring Boot feature flag 4종(`portfolio.snapshot-refresh.enabled` · `portfolio.order-refresh.enabled` · `portfolio.strategy.execution.submit-enabled` · Slack · Connector refresh 실행성 호출 모두 false 기본) + 서버단 POST 우회 차단, 🟢 확정 / 영향 spec 04 · 05 · 10).

화면 검증 통과 — (a) 잔고 / 보유 종목 = BalanceService 조회 원천을 legacy `balance_summary` 의존에서 최신 `connector_balance_snapshot` 으로 전환 + 보유 종목은 `connector_position_snapshot` 기준 표시 + `/balance-summary` · `/positions` 정상 + 보유 0건 empty card 정상 / 화면 진입 중 KIS · Connector refresh · Slack · Batch 호출 0건.

(b) 주문 / 주문 상세 = `connector_order_request` · `connector_order_event` · `connector_fill` 기반 표시 + `/orders` 목록 + `/orders/56` 상세 정상 + `portfolio.order-refresh.enabled=false` + 주문 API 호출 0건.

(c) 전략 / 리포트 = `/strategy/execution/plans` 에 최신 plan #113(신규 매수 차단 / 실행 후보 없음) 표시 + `portfolio.strategy.execution.submit-enabled=false` + POST submit 서버단 차단 확인 + `/strategy/reports/latest` 백테스트 요약 표시(기간 `2023-01-27 ~ 2026-06-24` / 누적 수익률 `427.69%` / MDD `-8.94%` / Sharpe `2.48`).

(d) Daily Batch / 대시보드 = 기존 run · step log 조회-only 화면 + 실행 버튼 disabled + 서버단 POST 우회 차단 + Step 12 이상 주문성 경로 기본 차단 + `/dashboard` 가 잔고 · 보유 · 최신 plan · 최신 백테스트 요약을 "AWS Paper / Snapshot 기준 / 조회-only" 문구와 함께 표시.

**결론** = Local View 가 AWS Paper DB 를 조회하는 read-only 운영 콘솔로 동작 확인 / ECS Fargate 이전 로컬 검증 1차 완료. **후속** = ECS / Fargate 즉시 진입 전 View Local Batch Step 1~17 실행 검증(2차 로컬 검증) 선행 / read-only 검증과 batch 실행 검증은 리스크 성격이 다름 / ECS 배포 설계 진입은 2차 로컬 검증 통과 후 / 05 spec port-view-ecs-and-runbook 후속 phase 책임으로 분리.

OD-MS-002(port-view 컴퓨트 = ECS Fargate Service) / OD-DB-009(view_app execution R-only / write 필요성은 05 spec 재검토) /
OD-ENV-006 / OD-ENV-007 / OD-NET-010 / OD-NET-011 / OD-SAFE-001 ~ OD-SAFE-004 본문 모두 변경 없이 1차 실증 메모 보강(Local Spring Boot 의 AWS Paper RDS read-only 접속이 본 일자 1차 가동 통과 / SSM Port Forwarding 의존성 정합 유지 / `view_app` execution R-only 정책 유지 /
자동 BUY · SELL E2E 단계적 도입 + 자동 재시도 금지 정책 그대로 유지).

보강 / 신규 리스크: 신규 R-AUTO-033(View Local 측 실행성 호출 / POST 우회로 의도하지 않은 broker · batch · Slack 트리거 위험, Status `Mitigated` — Spring Boot feature flag 4종 기본 false + 서버단 POST 차단 + 운영자 명시 ENABLE 후 단계적 진입).

본 결정에서도 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN /
broker_order_no 원문 / broker_branch_code 원문 / KIS paper login credential / Slack webhook URL / DB password / Administrator password 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

운영 식별자(`view_app` DB user / `127.0.0.1:15433` local port / SSM Port Forwarding 경유지 패턴 / plan id `113` / order id `56` / 누적 수익률 `427.69%` / MDD `-8.94%` / Sharpe `2.48` / 백테스트 기간 `2023-01-27 ~ 2026-06-24` / Spring Boot feature flag 라벨 4종 /
route prefix `/balance-summary` · `/positions` · `/orders` · `/orders/{id}` · `/strategy/execution/plans` · `/strategy/reports/latest` · `/dashboard` · `/daily-batch`) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / Step Functions / EventBridge Scheduler / Lambda / SSM / EC2 / IAM / RDS / KIS API / Slack 호출은 모두 운영자 직접 수행 — Kiro 는 본 일자 루트 / `_common` 문서 갱신만 수행 / AWS CLI / boto3 실행 0건 / AWS 리소스 생성 · 수정 · 삭제 0건 / Lambda 코드 본문 / IAM Policy 전체 본문 /
Step Functions ASL 전체 본문 / Lambda 응답 전문 / CloudWatch Logs 전문 / KIS API response body / Spring Boot application log 전문 평문 인용 0건.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역) — 운영자 직접 patch 한 port-view MS 의 Spring Boot 소스(`application-aws-paper.properties` 신규 + `BalanceService.java` 조회 원천 전환 + Daily Batch / Dashboard / Position / Order /
Balance / Strategy Execution controller · template 의 read-only 표현) 변경분은 port-view MS 영역으로 cross-service AWS Migration spec 본 일자 작업으로 인한 변경 0건.

**남은 후속** = (a) View Local Batch Step 1~17 실행 검증(2차 로컬 검증) 진입(ECS / Fargate 진입 전), (b) Slack notifier Lambda(`portfolio-event-notifier`) 와 port-view SlackNotificationService 통합 / 이전 가능성 검토(OD-MS-030 후속),
(c) ECS / Fargate 배포 설계(05 spec port-view-ecs-and-runbook 후속 phase), (d) 05 spec 하위 spec / operation-notes / validation-checklist 정식 갱신은 후속 phase 책임으로 분리.

Decision Summary 카운트 갱신(전체 95 → 96 / 확정 50 → 51 / 잠정 42 유지).


### Change Log Details 2026-06-29 (2) (1차) <a id="change-log-details-2026-06-29--2--1"></a>

변경 요약: OD-MS-002 / OD-MS-009 / OD-MS-037 / OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없이 메모 보강 (port-view aws-stepfunctions Daily Batch trigger 구현 + 로컬 Step 1~11 StartExecution 검증 통과)

2026-06-29 두 번째 항목 — port-view 가 Daily Batch 실행 backend 를 `local-file` / `aws-stepfunctions` 로 분리한 첫 구현 통과 사실을 결정 본문 변경 없이 메모로 보강. 신규 결정 없음 / Decision Summary 카운트 변경 없음(전체 96 / 확정 51 / 잠정 42 유지).

핵심 사실(commit `e72de6f` / `feat(view): add Step Functions daily batch trigger` / 변경 파일 6종 = `pom.xml`, `DailyBatchProperties.java`, `DailyBatchController.java`, `StepFunctionsDailyBatchExecutionService.java`, `application-aws-paper.properties`,
`daily_batch.html`)은 본 결정 평문 인용 0건 / 운영 식별자만 사실 기록 — secret 가 아님.

OD-MS-002(port-view 컴퓨트 = ECS Fargate Service 1순위) 본문 변경 없이 1차 실증 메모 보강 — port-view 의 운영 안정성 1순위 ECS Fargate Service 유지 + Batch 실행 책임은 View 내부 subprocess 가 아니라 Step Functions `StartExecution` client 로 분리 / local-file backend 는 로컬 운영자 검증 도구로 보존 /
Fargate 에서는 `portfolio.batch.local-file-execution-enabled=false` 기본값 / Step 12 이상 주문성 구간은 별도 approval / preflight / paper-order gate 뒤에서만 허용.

OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) 본문 변경 없이 1차 실증 메모 보강 — port-view 가 Step Functions `StartExecution` external caller 로 붙는 첫 local 검증 통과 /
기존 EventBridge Scheduler · Dispatcher Lambda 자동 trigger 와 분리된 운영자 수동 trigger 경로(View 화면 AWS Step 1~11 safe trigger 버튼) 가 본 결정으로 추가됨 / Step 12~17 자동 trigger 정책(OD-MS-033 09:01 보류) 변경 없음.

OD-MS-037(View Local AWS Paper read-only 1차 scope + ECS / Fargate 진입 전 batch 2차 검증 선행 정책) 본문 변경 없이 1차 실증 메모 보강 — 2차 로컬 검증의 read-only / Step 1 / Step 1~11 / Step 12~17 통과(2026-06-27 / 2026-06-28 /
2026-06-29 (1)) 에 더해, Fargate 진입 전 local `aws-stepfunctions` mode Step 1~11 trigger 선검증 (본 일자 2026-06-29 (2)) 까지 완료 / ECS Fargate 배포 설계 진입 전 사전 검증 단계가 모두 통과 / 정식 Fargate 진입(Dockerfile · ECR · Task Definition · Service · Fargate Step 1~11 / Step 12~17 검증)은 05 /
06 / 07 / 10 spec 후속 phase 책임으로 분리.

OD-SAFE-001 ~ OD-SAFE-004 (자동 BUY · SELL E2E 단계적 도입 / 자동 재시도 금지 idempotent 한정) 본문 변경 없이 1차 실증 메모 보강:

- `StepFunctionsDailyBatchExecutionService` 의 서비스 레벨 안전 gate 1차 실증 통과 (아래 조건 만족 시 차단):
  - `canStartAwsStepfunctions=false`
  - `hasRunningBatch=true`
  - `stateMachineArn` 빈 값
  - `minExecutableStepOrder` · `maxExecutableStepOrder` 범위 밖
  - `allowPaperOrderExecute=false` 상태 Step 12 이상
  - approval 요청은 `paperOrderEnabled=true` 외 모두 차단
- `aws-stepfunctions` backend 진입 시점에도 자동 재시도 금지 정책 유지
- Step 12~17 주문성 구간은 운영자 명시 승인 후에만 활성

보강 / 신규 리스크: R-AUTO-033 mitigation 에 [2026-06-29 보강 (2)] 메모 추가(port-view Step Functions trigger 분리 + 로컬 Step 1~11 검증 통과 / Status `Mitigated` 유지).

신규 R-AUTO-034(Fargate View `states:StartExecution` 권한 과다 부여 + Step 12 gate 우회 위험, Status `Open` / Task Role 특정 state machine ARN 한정 + Fargate 안전 기본값 + 서비스 레벨 안전 gate + executionArn redaction + Step 12~17 승인형 / preflight / paper-order gate 분리 mitigation).

본 결정에서도 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN /
broker_order_no 원문 / broker_branch_code 원문 / KIS paper login credential / Slack webhook URL / DB password / Administrator password / 실제 state machine ARN 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

운영 식별자(port-view commit hash `e72de6f` / commit message `feat(view): add Step Functions daily batch trigger` / Class 이름 `StepFunctionsDailyBatchExecutionService` / Controller endpoint path `/daily-batch/aws-stepfunctions/start-range` /

Spring properties key 라벨 6종(`portfolio.batch.execution-mode` · `portfolio.batch.aws-stepfunctions-region` · `portfolio.batch.aws-stepfunctions-state-machine-arn` · `portfolio.batch.aws-stepfunctions-execution-name-prefix` ·
`portfolio.batch.aws-stepfunctions-start-enabled` · `portfolio.batch.aws-stepfunctions-step-start-enabled`) /

StartExecution payload 필드 라벨 11종 + `runDate`(Asia/Seoul yyyy-MM-dd) /
Step Functions state name `StopCrawlerEc2AfterStep11Success` · `SendApprovalRequiredSlack` · `Step6ToStep11_Succeeded` · `Step12_CheckApproval` / Slack 이벤트 라벨 `APPROVAL_REQUIRED` / 에러 라벨 `States.Runtime` / Spring profile `aws-paper` /
Tomcat port `8080`) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / EventBridge Scheduler / Lambda / Step Functions / SSM / EC2 / IAM / RDS / S3 / KIS API 호출은 본 일자 변경 0건 — Kiro 는 본 일자 루트 / `_common` / 05 / 04 / 06 spec 문서 갱신만 수행 / AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 0건. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건.

Lambda 코드 본문 / IAM Policy 전체 본문 / Step Functions ASL 전체 본문 / Lambda 응답 전문 / CloudWatch Logs 전문 / KIS API response body / Spring Boot application log 전문 / Step Functions execution history 본문 / Slack 메시지 본문 / `StepFunctionsDailyBatchExecutionService` Java 본문 /
`DailyBatchProperties` Java 본문 / `daily_batch.html` Thymeleaf 본문 / `application-aws-paper.properties` 본문 / commit diff 본문 평문 인용 0건 / commit/add/reset/checkout/stash 0건.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역) — 운영자 직접 commit `e72de6f` 변경분(port-view MS 영역)은 port-view 루트 `README.md` / `CHANGELOG.md` 및 05 spec `operation-notes.md` 에 사실로만 기록(본문 전체 인용 0건).

**남은 후속** = (a) Dockerfile 작성, (b) ECR repository / image push, (c) ECS Task Definition 등록, (d) ECS Service 조회-only smoke test, (e) Fargate Step 1~11 `StartExecution` 검증, (f) Step 12~17 승인형 trigger / preflight /
paper-order gate 후속 구현, (g) Fargate Task Role `states:StartExecution` 최소 권한(특정 state machine ARN 한정) 부여, (h) 10 spec `cutover-and-validation-runbook` 폴더 신규 생성 + Fargate cutover 체크리스트 정식 작성.

Decision Summary 카운트 변경 없음(전체 96 / 확정 51 / 잠정 42 유지).


### Change Log Details 2026-06-29 (3) (1차) <a id="change-log-details-2026-06-29--3--1"></a>

변경 요약: OD-MS-002 / OD-MS-009 / OD-MS-037 / OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없이 메모 보강 (port-view Step 12~17 승인형 검증 완료 + Local View wrapper 정리 완료 + Approval state machine ARN 분리)

2026-06-29 세 번째 항목 — Local View 가 Step Functions `StartExecution` external caller 로 붙는 두 번째 phase / Step 12~17 approval workflow 1차 실증 통과 사실을 결정 본문 변경 없이 메모로 보강. 신규 결정 없음 / Decision Summary 카운트 변경 없음(전체 96 / 확정 51 / 잠정 42 유지).

핵심 검증 식별자 — executionName `port-view-step12-17-step12-17-20260629-194314-ba5edaf8` / state machine `portfolio-paper-daily-step12-17-approval` / status `SUCCEEDED` / start `2026-06-29T19:43:15.673+09:00` / stop `2026-06-29T19:46:06.546+09:00` /

`Step12_CheckApproval` 통과 → `Step12_RunMarketConnectorStrategyOrderExecute` 실행 → `Step12_GetCommandInvocation` 성공 → Step 13~17 전체 진행 → `ExecutionSucceeded` 확인 / DB 안전 후검증 통과(운영 marker `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS` /
오늘 신규 `connector_order_request` 0건 / READY · REQUESTED `strategy_execution_order` 0건 / 신규 broker 주문 0건).

OD-MS-002(port-view 컴퓨트 = ECS Fargate Service 1순위) 본문 변경 없이 1차 실증 메모 보강 — Local View 측에서 일반 workflow + approval workflow 2종 state machine 진입 분리 1차 실증 / Fargate 진입 시 Task Role `states:StartExecution` Resource 패턴 한정 정책은 06 spec 후속 phase 책임.

OD-MS-009(Daily Batch orchestration) 본문 변경 없이 1차 실증 메모 보강 — port-view 가 일반 workflow + approval workflow 2종에 external caller 로 붙는 첫 phase /
기존 EventBridge Scheduler · Dispatcher Lambda 자동 trigger 와 분리된 운영자 수동 trigger 경로(View 화면 AWS Step 1~11 safe trigger + AWS Step 12~17 승인 실행 버튼) / Step 12~17 자동 trigger 정책(OD-MS-033 09:01 보류) 변경 없음.

OD-MS-037(View Local AWS Paper read-only 1차 scope + ECS / Fargate 진입 전 batch 2차 검증 선행 정책) 본문 변경 없이 1차 실증 메모 보강 — 2차 로컬 검증의 read-only + Step 1 + Step 1~11 + Step 12~17(safe trigger) + **Step 12~17(approval trigger)** 통과(2026-06-27 / 2026-06-28 /
2026-06-29 (1) / 2026-06-29 (2) / 2026-06-29 (3)) 까지 완료 / 정식 Fargate 진입(Dockerfile · ECR · Task Definition · Service · Fargate Step 1~11 / Step 12~17 검증)은 05 / 06 / 07 / 10 spec 후속 phase 책임으로 분리.

OD-SAFE-001 ~ OD-SAFE-004(자동 BUY · SELL E2E 단계적 도입 / 자동 재시도 금지 idempotent 한정) 본문 변경 없이 1차 실증 메모 보강 — `StepFunctionsDailyBatchExecutionService` 의 서비스 레벨 안전 gate(`canStartAwsStepfunctions=false` / `hasRunningBatch=true` / `stateMachineArn` 빈 값 /
approval ARN 빈 값 / `min-` · `max-executable-step-order` 범위 / `allowPaperOrderExecute=false` 상태 Step 12 이상 / approval 요청 `paperOrderEnabled=true` 외) 1차 실증 통과 / Local View 측 approval workflow 진입 시 Step 12~17 NO_TARGET 또는 safe path 정합으로 신규 broker 주문 0건.

보강 / 신규 리스크: R-AUTO-033 mitigation 에 [2026-06-29 보강 (3)] 메모 추가(Step 12~17 approval state machine 분리 + boolean / numeric payload 1차 실증 통과 / Status `Mitigated` 유지). R-AUTO-034 mitigation 에 [2026-06-29 보강] 메모 추가(approval ARN 분리 + 서비스 레벨 안전 gate 1차 실증 + Fargate Task Role 후속 책임 명시 / Status `Open` 유지).

본 결정에서도 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN /
broker_order_no 원문 / broker_branch_code 원문 / KIS paper login credential / Slack webhook URL / DB password / Administrator password / 실제 state machine ARN 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

운영 식별자(executionName `port-view-step12-17-step12-17-20260629-194314-ba5edaf8` / 운영 marker `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS` / state machine 이름 2종 `portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval` /
state 이름 4종(`Step12_CheckApproval` · `Step12_RunMarketConnectorStrategyOrderExecute` · `Step12_GetCommandInvocation` · `ExecutionSucceeded`) /

Controller endpoint path 2종(`/daily-batch/aws-stepfunctions/start-range` · `/daily-batch/aws-stepfunctions/start-approval-range`) / Spring properties key 7종(기존 6종 + `portfolio.batch.aws-stepfunctions-approval-state-machine-arn`) /
환경변수 라벨 / payload 필드 라벨 + boolean / numeric 타입 / `requestedBy=VIEW_APPROVAL_BUTTON` 라벨 /

PowerShell wrapper 파일명 4종(`Start-PortfolioViewAwsPaperLocalFile.ps1` · `Load-PortfolioViewAwsPaperLocalFileEnv.ps1` · `Start-PortfolioViewAwsPaperStepFunctions.ps1` · `Load-PortfolioViewAwsPaperStepFunctionsEnv.ps1`) /
환경변수 set 라벨 `VIEW_AWS_PAPER_STEPFUNCTIONS_ENV_READY` / DB URL `jdbc:postgresql://127.0.0.1:15433/portfolio` / View DB user `view_app` / Tomcat port `8080` /

Spring profile `aws-paper` / start · stop timestamp / Class 이름 `StepFunctionsDailyBatchExecutionService` · `DailyBatchProperties` · `DailyBatchController` / Java field 이름 `awsStepfunctionsApprovalStateMachineArn`) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / EventBridge Scheduler / Lambda / Step Functions / SSM / EC2 / IAM / RDS / S3 / KIS API 호출은 본 일자 추가 변경 0건 — Kiro 는 본 일자 루트 / `_common` / 05 / 04 spec 문서 갱신만 수행 / AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 0건. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건.

Lambda 코드 본문 / IAM Policy 전체 본문 / Step Functions ASL 전체 본문 / Lambda 응답 전문 / CloudWatch Logs 전문 / KIS API response body / Spring Boot application log 전문 / Step Functions execution history 본문 / Slack 메시지 본문 / `StepFunctionsDailyBatchExecutionService` Java 본문 /
`DailyBatchProperties` Java 본문 / `DailyBatchController` Java 본문 / `daily_batch.html` Thymeleaf 본문 / `application-aws-paper.properties` 본문 / commit diff 본문 / PowerShell wrapper 본체 / env loader 본체 평문 인용 0건 / commit/add/reset/checkout/stash 0건.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역) — 운영자 직접 commit 변경분(`DailyBatchProperties.java` · `StepFunctionsDailyBatchExecutionService.java` · `application-aws-paper.properties` · `DailyBatchController.java` ·
`daily_batch.html` 및 운영자 로컬 도구 폴더의 PowerShell wrapper 4종) 은 port-view 루트 `README.md` / `CHANGELOG.md` 및 05 spec `operation-notes.md` 에 사실로만 기록(본문 전체 인용 0건).

**남은 후속** = (a) 2026-04-27 삼성전자 stale ACCEPTED `connector_order_request` cleanup 결정, (b) Fargate 용 `aws-paper-ecs` profile / Secret / IAM /
VPC 설정, (c) Fargate 초기 `paperOrderEnabled` 기본값 결정, (d) Fargate 조회-only smoke test runbook 작성, (e) ALB source IP 제한 또는 최소 인증 적용, (f) CloudWatch Logs / alarms / failure Slack 연동 점검, (g) Dockerfile 작성 / ECR repository · image push / ECS Task Definition 등록 /

ECS Service 기동 / Fargate Step 1~11 · Step 12~17 검증(05 · 06 · 07 spec 후속 phase 책임), (h) 10 spec `cutover-and-validation-runbook` 폴더 신규 생성 후속.

Decision Summary 카운트 변경 없음(전체 96 / 확정 51 / 잠정 42 유지).


### Change Log Details 2026-06-30 (오후) (1차) <a id="change-log-details-2026-06-30------1"></a>

변경 요약: OD-MS-002 / OD-MS-009 / OD-MS-037 / OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없이 메모 보강 (port-view ECS Fargate Public IP 1차 포팅 완료 + ECS View → AWS Step Functions Step 12~17 승인 실행 1차 실증 + desiredCount 0 종료)

2026-06-30 오후 항목 — 운영자가 직접 수행한 port-view ECS Fargate Public IP 1차 포팅 + ECS View → AWS Step Functions Step 12~17 승인 실행 1차 실증 통과 + 검증 후 ECS service desiredCount 0 종료 사실을 결정 본문 변경 없이 메모로 보강. 신규 결정 없음 / Decision Summary 카운트 변경 없음(전체 96 / 확정 51 / 잠정 42 유지).

핵심 검증 식별자 — ECS cluster `portfolio-paper-cluster` / ECS service `portfolio-view-service` / task definition `portfolio-view:1` → `portfolio-view:2`(기본 계좌번호 env 보정 한정 = `PORTFOLIO_BATCH_DEFAULT_ACCOUNT_NO` + `PORTFOLIO_VIEW_ACCOUNT_DEFAULT_ACCOUNT_NO`) /
ECR repository `portfolio-view` / CloudWatch Logs group `/ecs/portfolio-view`(retention 7일) /

Security Group `sgroup-port-view-ecs`(inbound TCP 8080 운영자 IP/32 한정) / task execution role 이름 `portfolio-paper-ecs-task-execution-role`(실제 ARN `[REDACTED_ARN]`) / task role 이름 `portfolio-paper-view-task-role`(실제 ARN `[REDACTED_ARN]`) /
launch type `FARGATE` / network mode `awsvpc` / cpu 512 / memory 1024 / container port 8080 / `assignPublicIp=ENABLED` / Spring profile `aws-paper` /

Tomcat port 8080 / RDS PostgreSQL + HikariPool + default schema `ops`.

ECS View → Step Functions Step 12~17 승인 실행 — executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / state machine `portfolio-paper-daily-step12-17-approval` / status `SUCCEEDED` / start `2026-06-30T14:15:42.899+09:00` /
stop `2026-06-30T14:18:48.358+09:00` / Slack `DAILY_EXECUTION_SUCCESS` 수신 / NO_TARGET 안전 종료 /

DB after-check 통과(REQUESTED · retryable rejected · active · today connector orders 모두 0건 / 최신 `connector_balance_snapshot id=281` / `as_of_date=2026-06-30` / `total_eval_amount=8,706,505` / `cash_balance=8,706,505` / `eval_profit=0` /
`source_version=connector-intraday-snapshot-refresh-1.0.0` / 보유 종목 0건 /

`connector_position_snapshot` 의 컬럼 `balance_snapshot_id` 부재로 `account_no` + `as_of_date` 기준 검증).

본 일자 식별된 stale `connector_order_request` 6건(2026-04-27 ACCEPTED 잔여 / 본 일자 실행과 무관 / 후속 cleanup 후보). 검증 후 ECS service `portfolio-view-service` desiredCount 0 전환 + Fargate task 종료 + public IP 해제 / 다음 기동 시 새 public IP 발급 전제 / SG inbound 운영자 IP/32 유지.

OD-MS-002(port-view 컴퓨트 = ECS Fargate Service 1순위) 본문 변경 없이 1차 실증 메모 보강 — Fargate 진입 시 ALB 미사용 + public subnet + `assignPublicIp=ENABLED` + 운영자 IP/32 SG inbound + CloudWatch Logs retention 7일 방식 1차 포팅 통과 / Elastic Beanstalk 2순위 /
App Runner · EC2 · ECS on EC2 · EKS · Lambda 비권고 판단 유지 / ALB · HTTPS · Route53 · Cloudflare Tunnel · 인증 · 인가 · Auto Scaling · Blue/Green · multi-AZ 운영은 1차 포팅 이후 보류.

OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) 본문 변경 없이 1차 실증 메모 보강 — ECS View 가 Step Functions `StartExecution` external caller 로 붙는 세 번째 phase(Local View → Step 1~11 / Local View → Step 12~17 approval / **ECS View → Step 12~17 approval**) 1차 실증 통과.

OD-MS-037(View Local AWS Paper read-only 1차 scope + ECS / Fargate 진입 전 batch 2차 검증 선행 정책) 본문 변경 없이 1차 실증 메모 보강 — Local 검증(2026-06-27 read-only / 2026-06-28 Step 1 + Step 1~11 / 2026-06-29 (1) Step 12~17 local-file / 2026-06-29 (2) Step 1~11 aws-stepfunctions /
2026-06-29 (3) + 2026-06-30 오전 Step 12~17 approval) 완료 후 본 일자 오후 정식 Fargate 진입 + ECS View → Step 12~17 승인 실행 1차 실증 통과.

OD-SAFE-001 ~ OD-SAFE-004(자동 BUY · SELL E2E 단계적 도입 / 자동 재시도 금지 idempotent 한정) 본문 변경 없이 1차 실증 메모 보강 — Fargate task definition 안 application env 기본값 회귀 0건(`portfolio.batch.local-file-execution-enabled=false` /
`paperOrderEnabled` · `fullPipelineExecutionEnabled` · `maxExecutableStepOrder` 등 gate 값 default 보다 완화 회귀 0건) / Step 12~17 주문성 구간은 운영자 명시 승인(approval) 후에만 활성 / NO_TARGET 안전 종료 / broker 주문 제출 0건.

보강 / 신규 리스크: R-AUTO-033 mitigation 에 [2026-06-30 오후 보강] 메모 추가(ECS Fargate 1차 포팅 + 운영자 IP/32 SG + ECS View → Step 12~17 approval 3차 실증 통과 / Status `Mitigated` 유지).

R-AUTO-034 mitigation 에 [2026-06-30 오후 보강] 메모 추가(Fargate ECS task role 의 `states:StartExecution` 권한이 Step 12~17 approval state machine ARN 한정 부여 1차 실증 + Task Role / Execution Role 분리 1차 실증 + Fargate task definition 안 application env 기본값 회귀 0건 /
Status `Open` 유지 — Fargate Task Role `states:StartExecution` Resource 패턴을 일반 + approval ARN 2종 모두 한정으로 부여 + Fargate 외부 노출 시점 default ENABLE 회귀 audit 통과 시점에 `Mitigated` 승격 / 06 spec 후속 phase 책임 그대로 유지).

본 결정에서도 secret value / KIS app key / KIS app secret / 계좌번호 12자리 원문 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / public IP / image digest full sha256 /
task ARN / ENI ID / job ARN / broker_order_no 원문 / broker_branch_code 원문 / KIS paper login credential / Slack webhook URL / DB password /

Administrator password / 실제 state machine ARN 본 문서 평문 기록 0건 — 모두 `[REDACTED]` / `[REDACTED_ACCOUNT_NO]` / `[REDACTED_PUBLIC_IP]` / `[REDACTED_ARN]` / `[REDACTED_TASK_ARN]` / `[REDACTED_SECRET_ARN]` / `[REDACTED_BROKER_ORDER_NO]` placeholder.

운영 식별자(ECS cluster `portfolio-paper-cluster` / ECS service `portfolio-view-service` / task definition `portfolio-view:2` / ECR repository `portfolio-view` / CloudWatch Logs group `/ecs/portfolio-view` / Security Group `sgroup-port-view-ecs` /
task execution role 이름 `portfolio-paper-ecs-task-execution-role` / task role 이름 `portfolio-paper-view-task-role` /

executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / state machine `portfolio-paper-daily-step12-17-approval` / Spring profile `aws-paper` / Tomcat port `8080` / launch type `FARGATE` / awsvpc / cpu 512 / memory 1024 / container port 8080 /
start · stop timestamp / status `SUCCEEDED` / Slack 이벤트 `DAILY_EXECUTION_SUCCESS` /

balance snapshot `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505` / 화면 라벨 6종(Dashboard / Balance / Positions / Orders / Reports / Daily) /
Spring properties env label `PORTFOLIO_BATCH_DEFAULT_ACCOUNT_NO` + `PORTFOLIO_VIEW_ACCOUNT_DEFAULT_ACCOUNT_NO` / desiredCount 0/1 라벨 /

Daily Batch gate 라벨 6종(`executionEnabled` · `localFileExecutionEnabled` · `fullPipelineExecutionEnabled` · `paperOrderEnabled` / 허용 범위 `1~17` / 화면 라벨 `Execution ON` · `Local File OFF` · `Full Pipeline ON` · `Paper Order ON`) /
DB 컬럼명 `balance_snapshot_id`(부재) · `account_no` · `as_of_date` / stale `connector_order_request` 6건 식별 사실) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / EventBridge Scheduler / Lambda / Step Functions / SSM / EC2 / IAM / RDS / S3 / KIS API 호출은 본 일자 Kiro 측 변경 0건 — Kiro 는 본 일자 `.kiro` 루트 + `.kiro/specs` 하위 문서 갱신만 수행 / AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 본 일자 Kiro 측 변경 0건(운영자 직접 수행 영역).

Lambda 코드 본문 / IAM Policy 전체 본문 / Step Functions ASL 전체 본문 / Lambda 응답 전문 / CloudWatch Logs 전문 / KIS API response body / Spring Boot application log 전문 / Step Functions execution history 본문 / Slack 메시지 본문 / commit diff 본문 / DB after-check raw output 전문 /
task definition JSON 전체 본문 / `ENI_ID` raw / `TASK_ARN` raw / `LOG_STREAM` raw 평문 인용 0건 / commit/add/reset/checkout/stash 0건.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역) — 운영자 직접 수행한 Dockerfile /
`.dockerignore` 추가 + Docker image build + ECR push + ECS Task Definition 등록 + ECS Service 생성 + Security Group · CloudWatch Logs · IAM Role 구성 + ECS View 접속 + AWS Step 12~17 승인 실행 클릭 + desiredCount 0 종료 변경분은 port-view MS 영역 /
운영자 영역으로 본 spec 산출물에 사실로만 기록(본문 전체 인용 0건).

**남은 후속** = (a) Daily Batch 화면 문구 ECS / `aws-stepfunctions` mode 정합 정리, (b) stale `connector_order_request` 6건 cleanup 결정(03 · 04 spec 후속 phase 책임), (c) ECS service desiredCount 0/1 운영 명령 runbook 정식화(05 spec 후속 phase 책임 / 본 일자 1차 통과),
(d) 운영자 IP 변경 시 Security Group inbound 갱신 절차(05 spec 후속 phase 책임 /

본 일자 1차 통과), (e) ALB · HTTPS · Route53 · Cloudflare Tunnel · 인증 · 인가 · application-ecs.yml 분리 · ECS Auto Scaling · Blue/Green 1차 포팅 이후 보류 phase 책임, (f) Fargate Task Role `states:StartExecution` Resource 패턴을 일반 + approval ARN 2종 모두 한정으로 부여(06 spec 후속 phase 책임),
(g) 10 spec `cutover-and-validation-runbook` 폴더 신규 생성 + Fargate cutover 체크리스트 정식 작성 후속.

Decision Summary 카운트 변경 없음(전체 96 / 확정 51 / 잠정 42 유지).


### Change Log Details 2026-06-30 (오후) Slack (1차) <a id="change-log-details-2026-06-30------Slack-1"></a>

변경 요약: OD-MS-038 신규 + OD-MS-030 / OD-MS-031 본문 변경 없이 evidence 보강 (Slack 문구 개선 최종 완료 — Approval Required Builder + Daily Brief Builder + notifier formatter + mini Step Functions + 장전/장후 Scheduler)

2026-06-30 오후 두 번째 Change Log 항목 — port-view ECS Fargate 1차 포팅 row 와 같은 일자 오후의 별도 Slack 문구 개선 최종 결과를 결정으로 락.

신규 결정 OD-MS-038(Daily Brief Slack mini workflow 운영 방식 — Daily 주문 실행 경로와 분리된 별도 mini Step Functions `portfolio-daily-brief-slack-notification` + Builder Lambda `portfolio-daily-brief-slack-summary-builder` + Notifier Lambda `portfolio-event-notifier` + IAM Role
2종 `portfolio-daily-brief-sfn-role` · `portfolio-daily-brief-scheduler-role` + Scheduler 2개 ENABLED `portfolio-daily-brief-morning-slack-0750-kst`(`cron(50 7 ? * MON-FRI *)` /

Asia/Seoul / `MORNING_BRIEF`) + `portfolio-daily-brief-evening-slack-1550-kst`(`cron(50 15 ? * MON-FRI *)` / Asia/Seoul / `EVENING_BRIEF`) / 🟢 확정 / 영향 spec 04 · 05 · 10).

결정 본문은 위 OD-MS-038 row 상세 참조.

**OD-MS-030(AWS 공통 Slack notifier Lambda 도입 정책) 본문 변경 없이 evidence 보강** — 본 일자 1차 검증 범위(3종 = `APPROVAL_REQUIRED` / `DAILY_EXECUTION_SUCCESS` / `DAILY_EXECUTION_FAILED`) 가 Daily Brief alias 2종(`MORNING_BRIEF` / `EVENING_BRIEF`) 까지 확대 + nested `balance` /
`positions` adapter + Builder `title` 우선 + 장후 `어제 대비` 표시 + 손익 prefix 규칙(음수 `🔵` / 양수 `🔴` / 0 `⚪`) 일관 적용 /

Approval Required 측은 별도 Builder `portfolio-approval-slack-summary-builder` + `portfolio-paper-daily-step1-17-approval` ASL 에 `BuildApprovalSlackPayload → SendApprovalRequiredSlack` 흐름 반영(revisionId `da8642c6-8409-41b6-ad57-e066ff672332`) /
Builder output → Notifier Slack smoke 성공(`marketStatusCode=BLOCK` / `marketStatusLabel=차단` / 차단 이유 + Daily 매수 신호 0/0 + Daily 포지션 판단 없음 + 매수·매도 후보 없음 표시) /

기존 View `SlackNotificationService` 는 제거되지 않고 유지.

**OD-MS-031(Step Functions / EventBridge 1차 Slack 연동 범위 3종 한정 정책) 본문 변경 없이 evidence 보강** — Daily Brief 2종은 OD-MS-031 의 "장 전 잔고 / 장 후 잔고 / 장중 손절 알림은 후속 단계로 분리" 항목에서 **본 일자 완료**로 이동(장 전 잔고 + 장 후 잔고) / 장중 손절 (`INTRADAY_STOP_LOSS`) 알림은 여전히 후속(OD-MS-035 / OD-MS-036 정합) / 1차 범위 정책 자체는 그대로 유지.

**smoke 통과** — Approval Required Builder output → Notifier Slack smoke 성공 + Daily Brief mini Step Functions morning smoke `daily-brief-morning-smoke-safe-20260630-193255-68f50aeb` `SUCCEEDED` + evening smoke
`daily-brief-evening-smoke-safe-20260630-193300-aa2b9a12` `SUCCEEDED` + Slack 장전 (`🌅 [장 전] 6/30 (화)` / `8,706,505원` / `🔵 -12.94%` / `🔵 -1,293,495원`) · 장후 (`🌅 [장 후] 6/30 (화)` / `8,706,505원` / `🔵 -12.94%` / `🔵 -1,293,495원` / `⚪ 0원`) 수신 확인.

**DB password 주입 방식** — Builder Lambda 는 Python 3.12 + `pg8000` + `DB_PASSWORD_SECRET_VALUE_FROM` 환경변수에 Secrets Manager `valueFrom` / Lambda 코드 안에서 `get_secret_value` runtime 조회 / `psycopg2` 미사용 / Lambda 본문 / Secrets Manager secret value / 실제 secret ARN 평문 기록 0건.

보강 / 신규 리스크: **R-AUTO-035 신규**(Daily Brief Slack 자동 발송 실패 또는 중복 발송 위험 / Status `Mitigated` — mini Step Functions 분리 + Scheduler 2개 ENABLED + 수동 smoke `SUCCEEDED` + Slack 수신 확인 + CloudWatch / Step Functions execution audit / Rollback = Scheduler DISABLED 또는 수동 Step Functions 실행).

R-AUTO-024(Slack webhook URL Lambda 환경변수 평문 노출 위험 / Status `Accepted`) mitigation 그대로 유지 — 본 일자에도 webhook URL 평문 기록 0건 / Lambda 환경변수명(`SLACK_WEBHOOK_URL`) 까지만 기록 / 운영 안정화 후 Secrets Manager 또는 SSM SecureString 이전 후속(06 spec 후속 phase 책임).

본 결정에서도 secret value / KIS app key / KIS app secret / 계좌번호 12자리 원문 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / public IP / image digest full sha256 /
task ARN / ENI ID / job ARN / broker_order_no 원문 / KIS paper login credential / Slack webhook URL / DB password / Administrator password / 실제 state machine ARN 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

운영 식별자(Builder Lambda 이름 `portfolio-approval-slack-summary-builder` · `portfolio-daily-brief-slack-summary-builder` / Notifier Lambda 이름 `portfolio-event-notifier` / state machine 이름 `portfolio-daily-brief-slack-notification` /
IAM Role 이름 `portfolio-daily-brief-sfn-role` · `portfolio-daily-brief-scheduler-role` /

Scheduler 이름 `portfolio-daily-brief-morning-slack-0750-kst` · `portfolio-daily-brief-evening-slack-1550-kst` / cron 표현식 `cron(50 7 ? * MON-FRI *)` · `cron(50 15 ? * MON-FRI *)` / timezone `Asia/Seoul` / Flexible OFF /
eventType 라벨 7종(`MORNING_BRIEF` · `EVENING_BRIEF` · `PRE_MARKET_STATUS` · `POST_MARKET_STATUS` · `APPROVAL_REQUIRED` · `DAILY_EXECUTION_SUCCESS` · `DAILY_EXECUTION_FAILED`) /

Slack color `#ECB22E` / 손익 prefix 라벨 `🔵` · `🔴` · `⚪` / state machine revisionId `da8642c6-8409-41b6-ad57-e066ff672332` / smoke execution name 2종 / smoke status `SUCCEEDED` / Lambda runtime `Python 3.12` / DB driver `pg8000` /
DB password 주입 방식 `DB_PASSWORD_SECRET_VALUE_FROM` / Slack webhook 환경변수명 `SLACK_WEBHOOK_URL` /

balance snapshot `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505` · `cumulativeProfitRate=-12.94%` · `cumulativeProfitAmount=-1,293,495` · `positionCount=0` · evening delta `0원`) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / Lambda / Step Functions / EventBridge Scheduler / IAM / RDS / Secrets Manager / Slack webhook / KIS API 호출은 모두 운영자 직접 수행 — Kiro 는 본 일자 루트 / `_common` / 04 · 05 · 06 spec 문서 갱신만 수행 / AWS CLI / boto3 / psql / Lambda 실행 / Step Functions 실행 / 외부 API 호출 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 0건.

Lambda 코드 본문 / Step Functions ASL 본문 / Scheduler target JSON 본문 / Slack 메시지 본문 / Builder output 전문 / Notifier input 전문 / CloudWatch Logs 전문 / IAM Policy 전체 본문 평문 인용 0건(R-DOCS-001 정합). commit/add/reset/checkout/stash 0건.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역).

**남은 후속** = (a) 다음 평일 07:50 자동 장전 Slack 수신 확인, (b) 다음 평일 15:50 자동 장후 Slack 수신 확인, (c) 다음 Step1~11 실제 실행 시 풍부한 `APPROVAL_REQUIRED` Slack 자동 수신 확인, (d) 보유종목 존재 시 종목별 손익 · 수익률 표시 재확인, (e) `DAILY_EXECUTION_SUCCESS` 주문/체결/잔고 refresh 요약 강화,
(f) `DAILY_EXECUTION_FAILED` 실패 cause truncation 정책, (g) `INTRADAY_STOP_LOSS` 실제 장중 포지션 이벤트 연동(OD-MS-035 /

OD-MS-036 후속 phase 책임), (h) Slack webhook URL Secrets Manager 또는 SSM SecureString 이전(R-AUTO-024 / 06 spec 후속 phase 책임), (i) 10 spec `cutover-and-validation-runbook` 폴더 신규 생성 시 live 전환 Daily Brief Slack 자동 발송 검증 항목 추가(자동 주문과 별개로 알림만 먼저 검증하는 safe path 후보).

Decision Summary 카운트 갱신(전체 96 → 97 / 확정 51 → 52 / 잠정 42 유지).


### Change Log Details 2026-06-30 (오후) 장중 손절 Slack (1차) <a id="change-log-details-2026-06-30------------Slack-1"></a>

변경 요약: OD-MS-030 / OD-MS-035 / OD-MS-036 / OD-MS-038 본문 변경 없이 evidence 보강 (장중 손절 `INTRADAY_STOP_LOSS` Slack 실제 이벤트 연동 최종 완료 — MarketConnector evaluate 교체 배포 + 장중 runner `--create-order --notify-slack` 연결 + MarketConnector EC2 IAM `lambda:InvokeFunction` Resource 한정 부여 + 실제 runner 1회 안전 검증 통과)

2026-06-30 오후 세 번째 Change Log 항목 — 같은 일자 오후의 (a) port-view ECS Fargate 1차 포팅 + (b) Slack 문구 개선 최종 완료(Daily Brief mini workflow) 다음의 추가 작업분으로, 장중 손절 측 Slack 실 연동 최종 완료 사실을 결정 본문 변경 없이 evidence 로 보강. **신규 결정 없음 / Decision Summary 카운트 변경 없음(전체 97 / 확정 52 / 잠정 42 유지)**.

**OD-MS-030(AWS 공통 Slack notifier Lambda 도입 정책) 본문 변경 없이 evidence 보강** — Notifier Lambda `portfolio-event-notifier` 의 호출 진입점이 본 일자 Daily Brief(EventBridge Scheduler + mini Step Functions) + Daily 본 실행 ASL Approval Required + **MarketConnector EC2
runner(`/home/ec2-user/apps/port-marketconnector/scripts/run_intraday_snapshot_and_evaluate.sh`)** 까지 확대 / Notifier Lambda 책임 본문 변경 없이 진입점만 추가 /

Builder Lambda 측 책임 분리 그대로 유지(`portfolio-approval-slack-summary-builder` Daily 본 실행 / `portfolio-daily-brief-slack-summary-builder` Daily Brief mini / MarketConnector evaluate 는 Builder 없이 직접 Notifier invoke 책임).

**OD-MS-035(장중 포지션 확인 3단계 구조) 본문 변경 없이 evidence 보강** — 1단계 MarketConnector 10분 Snapshot Refresh + 2단계 StrategyExecution Intraday Evaluate(MarketConnector EC2 runner 안에서 결합 실행) 정합 그대로 유지 /
2단계 가 `strategy_intraday_position_check` insert + `INTRADAY_STOP_SELL` `READY` order 생성 + **`portfolio-event-notifier` Lambda invoke** 까지 수행하도록 evidence 확대 / broker 주문 제출은 본 단계에서 여전히 불가능.

**OD-MS-036(Intraday Stop Sell Submit Workflow / `portfolio-paper-intraday-stop-sell-approval` state machine) 본문 변경 없이 evidence 보강** — broker 주문 제출은 `portfolio-paper-intraday-stop-sell-approval` Step Functions approval gate 통과 후에만 가능 정합 그대로 유지 /
본 일자 오후 추가 작업분으로 MarketConnector EC2 runner 측 Slack 발송이 분리 부착 / approval gate 자동 ENABLE 진입은 여전히 후속.

**OD-MS-038(Daily Brief Slack mini workflow 운영 방식) 본문 변경 없이 evidence 보강** — Daily Brief mini workflow 와 장중 손절 Slack 은 모두 별도 진입점이며 Notifier Lambda 만 공유 / 분리 정책 정합 유지 / 장중 손절 측은 mini Step Functions 미사용(MarketConnector EC2 runner 가 직접 Notifier invoke).

**본 일자 추가 작업분 핵심 사실** — (a) MarketConnector evaluate 파일 `/home/ec2-user/apps/port-marketconnector/src/connector_intraday_position_evaluate.py` 교체 배포(버전 `connector-intraday-position-evaluate-1.1.1-slack-notify` /
SHA256 `5ec6914853ab34e200682f256de53693f972b3e5d337a5bd8ab8ebf5296230ed` / 신규 CLI option 3종 `--notify-slack` · `--slack-function-name` · `--slack-region` / `.venv/bin/python` 기준 `py_compile` + `--help` 검증 통과).

(b) 장중 runner `/home/ec2-user/apps/port-marketconnector/scripts/run_intraday_snapshot_and_evaluate.sh` 갱신(SHA256 `8fe7657a75b5d7637ec645b6d8a55bf75c993d46c1c71e8a5e88bded29baa8a0` /
backup `run_intraday_snapshot_and_evaluate.sh.bak.20260630T112255Z.create-order-notify-slack` / 기존 evaluate 호출에 `--create-order --notify-slack` 추가 / runner 문법 검증 통과).

(c) MarketConnector EC2 IAM 권한 부여 — role `portfolio-paper-marketconnector-ec2-role` 에 inline policy `portfolio-paper-marketconnector-event-notifier-invoke`(action `lambda:InvokeFunction` / Resource `portfolio-event-notifier` 한정 /
Resource · Action wildcard 0건 / OD-SEC-005 / OD-SEC-006 정합) + EC2 invoke smoke 성공.

(d) Notifier Lambda formatter — eventType `INTRADAY_STOP_LOSS` 지원 확인 / Slack 문구 `🚨 [장중 손절]` 수신 확인 / `evalProfitRate` 표시값에 `%` suffix 추가 반영 / 로컬 smoke `🔵 -4.2%` 표시 확인 / EC2 IAM invoke smoke 수신 확인.

(e) 실제 runner 1회 안전 검증 — SSM commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` / snapshot refresh 성공 / evaluate 실행 성공 / source_version `connector-intraday-position-evaluate-1.1.1-slack-notify` / `create_order=True` / `notify_slack=True` /
`open_position_count=0` / `EMPTY_NORMAL` / `INTRADAY_SNAPSHOT_AND_EVALUATE=SUCCESS` / runner exit code 0 / OPEN position 0건 상태라 check / order / slack 없이 정상 종료.

(f) DB after-check — marker `STEP19C_INTRADAY_STOP_FINAL_DB_AFTER_CHECK=SUCCESS` + 5종 count 모두 0(`TODAY_INTRADAY_CHECKS` · `TODAY_INTRADAY_STOP_EXECUTION_ORDERS` · `ACTIVE_INTRADAY_STOP_EXECUTION_ORDERS` · `TODAY_INTRADAY_STOP_CONNECTOR_ORDERS` ·
`TODAY_INTRADAY_STOP_CONNECTOR_ORDER_ROWS`) + 최신 `connector_balance_snapshot id=281` / `as_of_date=2026-06-30` /

`as_of_ts=2026-06-30 11:23:34.973842+00` / `total_eval_amount=8,706,505` / `cash_balance=8,706,505` / `source_version=connector-intraday-snapshot-refresh-1.0.0` / PSQL exit code 0 / 컬럼명은 `information_schema.columns` 사전 확인 후 작성(추정 컬럼명 사용 0건).

**Slack 발송 위치 확정** — `strategy_intraday_position_check` insert 후 평가 결과 보존 / hard stop 조건 충족 시 `strategy_execution_order` READY 생성 / `attach_created_execution_order()` 성공 후 Slack 발송 / Slack 실패 시 READY 생성 자체는 rollback 하지 않고 warning 출력(R-AUTO-036 신규 /
Status `Mitigated` mitigation 정합) / broker 주문 제출은 본 파일에서 여전히 불가능 / 실제 broker 주문은 `portfolio-paper-intraday-stop-sell-approval` Step Functions approval gate 통과 후에만 가능.

보강 / 신규 리스크: **R-AUTO-036 신규**(장중 손절 READY 생성 후 Slack 발송 실패 시 rollback 없는 정책의 부작용 위험 / Status `Mitigated` — Lambda CloudWatch Logs / Step Functions execution audit / DB after-check 5종 count audit 로 보강).

R-AUTO-035 mitigation 에 [2026-06-30 오후 추가 보강] 메모 추가(Notifier Lambda 호출 진입점이 MarketConnector EC2 runner 까지 확대 / Status `Mitigated` 유지). R-AUTO-024(Slack webhook URL Lambda 환경변수 평문 노출 / Status `Accepted`) mitigation 그대로 유지 — 본 일자에도 webhook URL 평문 기록 0건.

본 결정에서도 secret value / KIS app key / KIS app secret / 계좌번호 12자리 원문 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / public IP / image digest full sha256 /
task ARN / ENI ID / job ARN / broker_order_no 원문 / KIS paper login credential / Slack webhook URL / DB password / Administrator password / 실제 state machine ARN 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.

운영 식별자(MarketConnector evaluate 파일 경로 / 배포 SHA256 `5ec6914853ab34e200682f256de53693f972b3e5d337a5bd8ab8ebf5296230ed` / 배포 버전 `connector-intraday-position-evaluate-1.1.1-slack-notify` /
신규 CLI option `--notify-slack` · `--slack-function-name` · `--slack-region` · `--create-order` / runner 파일 경로 / runner SHA256 `8fe7657a75b5d7637ec645b6d8a55bf75c993d46c1c71e8a5e88bded29baa8a0` / runner backup 이름 /

SSM commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` / IAM Role 이름 `portfolio-paper-marketconnector-ec2-role` / inline policy 이름 `portfolio-paper-marketconnector-event-notifier-invoke` / Lambda 이름 `portfolio-event-notifier` /
state machine 이름 `portfolio-paper-intraday-stop-sell-approval` / Slack 이벤트 라벨 `INTRADAY_STOP_LOSS` / 문구 라벨 `🚨 [장중 손절]` / 손익 prefix smoke 라벨 `🔵 -4.2%` /

source_version 2종(`connector-intraday-position-evaluate-1.1.1-slack-notify` · `connector-intraday-snapshot-refresh-1.0.0`) /
`as_of_ts=2026-06-30 11:23:34.973842+00` / marker 이름 3종(`STEP19C_INTRADAY_STOP_FINAL_DB_AFTER_CHECK=SUCCESS` · `INTRADAY_SNAPSHOT_AND_EVALUATE=SUCCESS` · `EMPTY_NORMAL`) / table count 라벨 5종) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / Lambda / Step Functions / EventBridge Scheduler / IAM / RDS / Secrets Manager / SSM RunCommand / Slack webhook / KIS API 호출은 모두 운영자 직접 수행 — Kiro 는 본 일자 추가 분으로 루트 / `_common` / 03 · 04 · 06 spec 문서 갱신만 수행 / AWS CLI / boto3 / psql / Lambda 실행 /
Step Functions 실행 / SSM RunCommand / 외부 API 호출 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 0건.

Lambda 코드 본문 / Step Functions ASL 본문 / `connector_intraday_position_evaluate.py` 본문 / runner ps1 본문 / SSM 응답 본문 / Slack 메시지 본문 / Builder output 전문 / Notifier input 전문 / CloudWatch Logs 전문 / IAM Policy 전체 본문 평문 인용 0건(R-DOCS-001 정합). commit/add/reset/checkout/stash 0건.

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역) — 운영자 직접 evaluate 교체 배포 + runner 갱신 + IAM inline policy 부여 + EC2 invoke smoke + 1회 실 runner 안전 검증 변경분은 본 spec 산출물에 사실로만 기록(본문 전체 인용 0건).

운영자 노트 작성 중 식별된 운영 사고 2건 — (i) 추정 컬럼명(`connector_order_request.order_side` 등) SQL 작성 / (ii) 실패 명령 / SQL 뒤 SUCCESS marker 출력 — 은 `.kiro/AGENTS.md` "운영 명령 작성 규칙(추가)" 규칙 2 · 3 · 4 의 evidence 로 사용(value 평문 0건 / R-DOCS-001 정합).

운영자 로컬 세션에서 DB password 가 우연히 노출된 사실은 본 Change Log 에 password 값 없이 "credential rotation / history cleanup 권고" 수준으로만 기록(R-SEC-010 / R-DOCS-002 정합 / 06 spec 후속 phase 책임 그대로 유지).

**남은 후속** = (a) 실제 보유 종목 발생 후 hard stop 조건 충족 시 `INTRADAY_STOP_LOSS` Slack 실이벤트 수신 확인, (b) 실제 보유 종목 발생 후 `INTRADAY_STOP_SELL` READY 생성 + approval gate 차단 상태 재확인, (c) Slack 메시지에 계좌 / 현재가 / 진입가 /
예상손익금액 추가 여부 검토, (d) 장중 손절 READY 생성 후 별도 approval summary Slack 추가 여부 검토, (e) `portfolio-paper-intraday-stop-sell-approval` state machine 자동 ENABLE 진입 운영자 별도 승인 후 후속(OD-MS-035 / OD-MS-036 /

R-AUTO-030 ~ R-AUTO-032 / R-BROKER-005 정합 그대로 유지), (f) Slack webhook URL Secrets Manager 또는 SSM SecureString 이전(R-AUTO-024 / 06 spec 후속 phase 책임),
(g) R-AUTO-036 운영 detection 자동화(Lambda CloudWatch Logs metric filter / Slack 실패 alarm / DB after-check ↔ Slack 수신 cross-reference 자동화 / 05 · 06 spec 후속 phase 책임).

**신규 결정 0건** — 본 일자 추가 분은 evidence 보강만 / Decision Summary 카운트 변경 없음(전체 97 / 확정 52 / 잠정 42 유지).


### Change Log Details 2026-07-01 (1차) <a id="change-log-details-2026-07-01-1"></a>

변경 요약: OD-SAFE-001 / OD-SAFE-002 / OD-SAFE-003 / OD-MS-009 / OD-MS-032 / OD-MS-033 본문 변경 없이 evidence 보강 (aws-paper Daily 자동화 1차 풀 ON + Step 12~17 Scheduler ENABLED + 전체 Daily 스케줄 라인업 7종 확인)

2026-07-01 Change Log 항목 — 운영자가 직접 수행한 aws-paper Step 12~17 자동 실행 진입(Scheduler `portfolio-paper-daily-step12-17-order-0901-kst` DISABLED → ENABLED / LastModificationDate `2026-07-01T13:53:57.160+09:00` / ScheduleExpression `cron(1 9 ? * MON-FRI *)` /
Asia/Seoul / FlexibleTimeWindow OFF / Target `portfolio-paper-daily-scheduler-dispatcher` /

Target Input `{"scheduleType":"STEP12_17_ORDER","dryRun":false}`) + Step 12~17 수동 실행 1회 검증 통과(executionName `port-manual-daily-step12-17-20260701-043747` / state machine `portfolio-paper-daily-step12-17-approval` / status `SUCCEEDED` /
start `2026-07-01T13:37:47.856+09:00` / stop `2026-07-01T13:40:41.212+09:00` / execution history `ExecutionSucceeded` / Slack 3종 수신 — 07:50 장전 / 08:24 승인 필요 /

Step 12~17 성공) + DB after-check 통과(2026-07-01 `strategy_execution_order` 0건 / REQUESTED 전략 주문 0건 / active `connector_order_request` 0건 / 오늘 connector 주문 0건 / 최신 `connector_balance_snapshot id=321` / `as_of_date=2026-07-01` / `total_eval_amount=8,706,505` /
`cash_balance=8,706,505` / `eval_profit=0` / Step 12~17 NO_TARGET 안전 종료 판정 /

stale `connector_position_snapshot` 2026-06-23 4건 잔여 사실 확인 — 최신 balance + 당일 주문 0건 기준 전액 현금 판정) + 전체 Daily 스케줄 라인업 7종 ENABLED 확인(1.

`portfolio-paper-ec2-start-0750-kst`(07:50 EC2 start) / 2. `portfolio-daily-brief-morning-slack-0750-kst`(07:50 장전 Slack / eventType `MORNING_BRIEF`) / 3. `portfolio-paper-daily-step1-11-approval-0800-kst`(08:00 Step 1~11 / dryRun false) / 4.

**`portfolio-paper-daily-step12-17-order-0901-kst`(09:01 Step 12~17 / dryRun false / 본 일자 ENABLED)** / 5. `portfolio-paper-intraday-snapshot-evaluate-10min-kst`(09:10~15:50 10분 장중 손절 / cron `cron(10/10 9-15 ? * MON-FRI *)`) / 6.

`portfolio-daily-brief-evening-slack-1550-kst`(15:50 장후 Slack / eventType `EVENING_BRIEF`) / 7. `portfolio-paper-marketconnector-stop-1550-kst`(15:50 MarketConnector EC2 stop)) 사실을 결정 본문 변경 없이 evidence 로 보강.

OD-SAFE-001(paper 자동 BUY / SELL E2E 초기 차단 → 검증 후 허용) 본문 변경 없이 evidence 보강 — 본 일자 결과로 paper E2E 1차 풀 ON 상태 진입 / 09:01 Step 12~17 자동 진입 통과 사실 반영. OD-SAFE-002(live 자동 BUY 정책 후보+수동 승인 우선) / OD-SAFE-003(live 자동 SELL 정책 후보+수동 승인 우선) 본문 변경 없이 evidence 보강 — **본 변경은 aws-paper 에 한정된다.

aws-live 자동 BUY / SELL 정책은 변경하지 않으며, live 는 후보 + 수동 승인 우선 정책을 유지한다.** OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) 본문 변경 없이 evidence 보강 — 08:00 Step 1~11 approval Scheduler + 09:01 Step 12~17 approval Scheduler 2개
축으로 자동 실행 라인업 확정 / EC2 lifecycle Scheduler 2개(07:50 start / 15:50 stop) + Daily Brief Slack Scheduler 2개(07:50 장전 / 15:50 장후) + 10분 장중 손절 Scheduler 1개 결합해 총 7종 라인업.

OD-MS-032(EventBridge Scheduler + Dispatcher Lambda) 본문 변경 없이 evidence 보강 — 본 일자 Step 12~17 Scheduler ENABLE 전환 시점에 Target Input `{"scheduleType":"STEP12_17_ORDER","dryRun":false}` 정합 확인 + Dispatcher Lambda `portfolio-paper-daily-scheduler-dispatcher` 진입점 동일 유지.

OD-MS-033(Step 12 retry-normalizer + 09:01 보류) 본문 변경 없이 evidence 보강 — 본 결정의 "09:01 보류" 승격 조건(운영자 별도 판단 후 ENABLE 진입)이 본 일자 통과 / 결정 본문의 승격 조건 그대로 반영되어 신규 결정 ID 부여 없이 evidence 보강만 진행.

보강 / 신규 리스크: R-AUTO-001 mitigation 에 [2026-07-01 보강] 메모 추가(BUY / SELL / fill sync / position 변경 step 자동 재시도 금지 정책 유지 / Step 12~17 Scheduler ENABLED 후에도 Step Functions Retry 정책 검토 대상 유지 / Status `Open` 유지).

R-AUTO-025 mitigation 에 [2026-07-01 자동 ENABLE 진입] 메모 추가(09:01 schedule 보류 정책이 운영자 승인 후 ENABLE 진입 통과 / Status `Mitigated` 유지).

**R-AUTO-037 신규** — Area `Automation` / Risk "Step 12~17 자동 실행이 Step 1~11 실패 또는 데이터 미준비 상태에서도 실행될 위험" / Impact `High` / Probability `Low` / Mitigation(dispatcher · state machine 에서 Step 1~11 성공 여부 · 당일 run 상태 · REQUESTED 주문 존재 여부 · market status 확인 /
Slack `APPROVAL_REQUIRED` 흐름의 마지막 안전 gate 유지) / Detection(Scheduler invocation log / Step Functions execution status / Slack failure /

DB after-check) / Rollback(`portfolio-paper-daily-step12-17-order-0901-kst` DISABLED 즉시 전환) / Affected Spec `04, 05, 10` / Status `Mitigated`.

본 결정에서도 secret value / KIS app key / KIS app secret / 계좌번호 12자리 원문 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / public IP / image digest full sha256 /
task ARN / ENI ID / job ARN / broker_order_no 원문 / broker_branch_code 원문 / KIS paper login credential / Slack webhook URL / DB password /

Administrator password / 실제 state machine ARN / 실제 Lambda ARN 본 문서 평문 기록 0건 — 모두 `[REDACTED]` 계열 placeholder.

운영 식별자(Scheduler 이름 `portfolio-paper-daily-step12-17-order-0901-kst` / cron `cron(1 9 ? * MON-FRI *)` / Asia/Seoul / FlexibleTimeWindow OFF / Target Lambda `portfolio-paper-daily-scheduler-dispatcher` /
Target Input `{"scheduleType":"STEP12_17_ORDER","dryRun":false}` / State `ENABLED` / LastModificationDate `2026-07-01T13:53:57.160+09:00` /

state machine `portfolio-paper-daily-step12-17-approval` / executionName `port-manual-daily-step12-17-20260701-043747` / status `SUCCEEDED` / start · stop timestamp / execution history label `ExecutionSucceeded` / Slack 이벤트 라벨 3종(장전 / 승인 필요 / 성공) /
balance snapshot id `321` / `as_of_date=2026-07-01` / `total_eval_amount=8,706,505` / `cash_balance=8,706,505` / `eval_profit=0` /

라인업 7종 이름) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / EventBridge Scheduler / Lambda / Step Functions / SSM / EC2 / IAM / RDS / KIS API 호출은 본 일자 Kiro 측 변경 0건 — Kiro 는 본 일자 `.kiro` 루트 + `.kiro/specs` 하위 문서 갱신만 수행 / AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 0건 /
AWS 리소스 신규 생성 · 수정 · 삭제 본 일자 Kiro 측 변경 0건(운영자 직접 수행 영역 — Step 12~17 Scheduler ENABLE 전환 + Step 12~17 수동 실행 + DB after-check).

8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역). **신규 결정 0건** — 본 일자 추가 분은 OD-SAFE-001 / OD-SAFE-002 / OD-SAFE-003 / OD-MS-009 / OD-MS-032 / OD-MS-033 evidence 보강만 / Decision Summary 카운트 변경 없음(전체 97 / 확정 52 / 잠정 42 유지).


### Change Log Details 2026-07-03 (1차) <a id="change-log-details-2026-07-03-1"></a>

변경 요약: **OD-DB-012** · **OD-MS-039** 신규 (Step Functions 실행 이력 OPS mirror + 전용 최소 권한 role + Recorder Lambda). 2026-07-02 View Daily Batch 화면 이력 불일치 이슈(followups-overview `Done recently` 정합) 후속 조치로 진행. 대상 State Machine 은 `portfolio-paper-daily-step1-17-approval` + `portfolio-paper-daily-step12-17-approval` 2종.

| 항목 | 값 |
|---|---|
| 반영 주제 | 2026-07-03 Step Functions 실행 이력 OPS mirror 기반 완료 |
| 핵심 결론 | AWS_STEPFUNCTIONS run 이력을 `ops.strategy_daily_batch_run` + `ops.strategy_daily_batch_step_log` 에 남기는 기반 완료. View Daily Batch 화면이 AWS Step Functions 실행 이력을 볼 수 있는 기반 마련 |
| 권한 설계 | `ops_recorder_app` 전용 최소 권한 role 신설 · `view_app` 재사용 안 함 · `execution_app` 권한 확대 안 함 · 업무 테이블 write 권한 확대 0건 |
| 신규 role 권한 | `ops` schema USAGE · `strategy_daily_batch_run` · `strategy_daily_batch_step_log` SELECT / INSERT / UPDATE · 관련 sequence USAGE / SELECT · DELETE 미부여 |
| 제약 변경 | `chk_strategy_daily_batch_run_type` 에 `AWS_STEPFUNCTIONS` 추가. 기존 `MANUAL` / `SCHEDULED` / `RETRY` / `MANUAL_PARTIAL` 값 유지 |
| Recorder Lambda | `portfolio-daily-batch-ops-recorder` · Python 3.12 · ap-northeast-2 · VPC Lambda · Secrets Manager `ops_recorder_app` secret |
| 지원 action | `RECORD_START` · `RECORD_STEP` · `RECORD_SUCCESS` · `RECORD_FAILURE` |
| Step Functions 연동 | 실행 role 에 `lambda:InvokeFunction` 한정 부여 (Resource ARN 한정 · wildcard 0건) |
| 1차 범위 | 전체 세부 step mirror 가 아니라 run-level + 대표 workflow step mirror 중심 |
| 검증 요약 | RECORD_START 응답 `ok true` 확인 · RECORD_STEP 응답 `ok true` + `stepLogId` 생성 확인 · RECORD_SUCCESS 응답 `ok true` 확인 · commit smoke `batchRunId=53` 확인 · Step 12 approval-blocked smoke 후 `ops.strategy_daily_batch_run` + step log 기록 확인 |
| 실행 | 본 문서 반영 중 실제 AWS / DB / Slack / crawler / broker / KIS API 호출 0건 (Kiro 문서 갱신만) |
| 후속 | 전체 세부 step mirror 확장 · View Daily Batch 화면이 `AWS_STEPFUNCTIONS` run 이력을 렌더링하는지 실 실행 회차 확인 · CloudWatch Alarm 도입 후보 |

관련 리스크: **R-AUTO-038 신규** (Step Functions 실행 이력 mirror 누락 → View Daily Batch 화면 이력 불일치 · Mitigated by OD-DB-012 + OD-MS-039). 상세는 `risk-register.md` 의 R-AUTO-038 항목 참조.

secret · Secrets Manager value · Lambda 실제 ARN · Recorder invocation 응답 raw JSON · State Machine 실 executionName / 실행 role ARN 평문 인용 0건(R-DOCS-001 정합). 운영 식별자(role 이름 `ops_recorder_app` · Recorder Lambda 이름 · State Machine 이름 · 테이블 이름 · action 이름 · `batchRunId=53` · check constraint 이름) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

AWS / EventBridge Scheduler / Lambda / Step Functions / SSM / EC2 / IAM / RDS / KIS API 호출은 본 일자 Kiro 측 변경 0건 — Kiro 는 본 일자 `.kiro/specs/_common` 하위 문서 갱신만 수행 / AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 0건 / broker 주문 0건 / aws-live 작업 0건 / secret 원문 기록 0건.

**신규 결정 2건** — OD-DB-012 · OD-MS-039 (모두 CONFIRMED). Decision Summary 카운트는 재집계 필요(Review Needed 이월 상태) — 본 회차에서는 원문 유지 원칙에 따라 상단 dashboard 숫자 갱신을 이월한다.


### Change Log Details 2026-07-08 (1차) <a id="change-log-details-2026-07-08-1"></a>

변경 요약: **OD-MS-040** 신규 (ECS batch container Asia/Seoul timezone 정책 + crawler / preprocessor TaskDefinition `TZ=Asia/Seoul` 단기 패치 + State Machine `portfolio-paper-daily-step1-17-approval` 의 Step2A / Step3 task revision 갱신). 2026-07-08 08:00 KST Daily Step1~11 자동 실행이 SUCCEEDED 였음에도 DB 최신 raw / feature / decision `data_date` 가 2026-07-06 에 정체된 사실을 원인 분석한 결과, ECS Fargate 컨테이너 UTC timezone + `datetime.now().date()` naive 사용이 stale target date 로 직접 이어졌음을 확정.

| 항목 | 값 |
|---|---|
| 반영 주제 | 2026-07-08 Daily Step1~11 stale data 원인 확정 + ECS TZ 단기 패치 |
| 배경 | Daily Step1~11 자동 실행 SUCCEEDED (2026-07-08 08:00 KST) 였으나 `interest_price_raw` · `interest_investorflow_raw` · `interest_program_raw` · `interest_shortsell_raw` · `pre_total_market_daily_feature` · `pre_total_stock_daily_feature` `MAX(trade_date)=2026-07-06` 정체 · `decision.strategy_daily_run.data_date=2026-07-06` · `execution.strategy_execution_plan` NO_CANDIDATE |
| 직접 원인 | ECS Fargate 컨테이너 기본 timezone 이 UTC 인 상태에서 crawler `interest_price.py` · `interest_investorflow.py` 등이 timezone 없는 `datetime.now().date() - timedelta(days=1)` 을 사용 → 08:00 KST 실행 시 UTC 는 전일 23시대 → `datetime.now().date()=2026-07-07` → `-1 day` 후 target date 2026-07-06 |
| 배제 원인 | `is_holiday(2026-07-07,"KR")==False` 확인 · 2026-07-07 KRX 정상 개장 · 휴장일 오판 원인 아님 |
| 단기 조치 (운영자 직접 수행) | (1) `portfolio-paper-interest-crawler:8` · `portfolio-paper-interest-preprocessor:2` 신규 revision 등록 · env 에 `TZ=Asia/Seoul` 추가 · (2) State Machine `portfolio-paper-daily-step1-17-approval` Step2A_RunInterestCrawlerNongui task `:7`→`:8` · Step3_RunPreprocessor task `:1`→`:2` 갱신 · State Machine revisionId 원문 미기록(`[REDACTED_REVISION_ID]`) · (3) crawler:8 · preprocessor:2 one-off TZ smoke 성공(`TZ_ENV=Asia/Seoul` · `time.tzname=('KST','KST')` · `naive_yesterday=2026-07-07`) |
| 오늘 조치 | Step1~11 수동 재실행 없음 |
| 검증 예정 | 2026-07-09 08:00 KST Daily Step1~11 자동 실행 + 09:01 Step 12~17 자동 실행에서 (a) Step2A crawler log 의 `interest_price` · `interest_investorflow` 2026-07-07 수집 · (b) preprocessor latest date 2026-07-07 · (c) `decision.strategy_daily_run.data_date=2026-07-07` · (d) Step 12~17 자동 실행이 stale data 영향 없이 정상 흐름 |
| 근본 개선 (후속) | 코드에서 `datetime.now()` 직접 사용을 timezone-aware helper 로 대체 · 대상 후보 `interest_price.py` · `interest_investorflow.py` 등 crawler / preprocessor entry point · 08 spec 후속 phase |
| 비용 영향 | TaskDefinition env 추가는 월 비용 영향 없음. Step Functions transitions · Fargate cpu / memory · CloudWatch Logs 비용 무변화 |
| 실행 | 본 문서 반영 중 실제 AWS / DB / Slack / crawler / broker / KIS API 호출 0건 (Kiro 문서 갱신만) · ECS RegisterTaskDefinition · State Machine UpdateStateMachine · one-off TZ smoke 는 운영자 직접 수행 |
| 후속 | 2026-07-09 자동 실행 최신성 회복 검증 · 성공 시 R-DATA-010 Status 승격 판단 · 실패 시 원인 재조사 |

관련 리스크: **R-DATA-010 Mitigation history 보강** (partial mitigation applied · next auto-run verification pending · Status `Open` 유지). R-DATA-017(KRX GUI worker 계열 · Step2B 성공 기준) 과는 원인이 구분됨.

secret · Secrets Manager value · Lambda 실제 ARN · Task Definition ARN · State Machine 실 executionName · 실행 role ARN · Task ARN · ENI ID · public IP · account-id · image digest full sha256 평문 인용 0건(R-DOCS-001 정합). 운영 식별자(TaskDefinition 이름 · State Machine 이름 · State 이름 `Step2A_RunInterestCrawlerNongui` · `Step3_RunPreprocessor` · revision number `:7` / `:8` / `:1` / `:2` · env key `TZ` · env value `Asia/Seoul` · smoke 결과 라벨) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님. State Machine revisionId 는 `[REDACTED_REVISION_ID]` placeholder 로 표기.

AWS / EventBridge Scheduler / Lambda / Step Functions / SSM / EC2 / IAM / RDS / KIS API 호출은 본 일자 Kiro 측 변경 0건 — Kiro 는 본 일자 `.kiro` 루트 + `.kiro/specs/_common` 하위 문서 갱신만 수행 / AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 0건 / broker 주문 0건 / aws-live 작업 0건 / secret 원문 기록 0건 / git add · commit · push 실행 0건.

**신규 결정 1건** — OD-MS-040 (CONFIRMED). Decision Summary 카운트는 재집계 필요(Review Needed 이월 상태) — 본 회차에서는 원문 유지 원칙에 따라 상단 dashboard 숫자 갱신을 이월한다.

## Decision Update Rules

본 문서를 갱신할 때 반드시 따라야 하는 규칙이다.

1. 새 spec(03 ~ 10)에서 운영자 결정이 생기면 본 파일의 적절한 카테고리에 같은 컬럼 형식으로 누적한다.
2. 결정이 바뀌면 기존 row를 삭제하지 않는다. 선택값과 Status를 갱신하고, 비고 또는 Change Log에 변경 사실을 남긴다.
3. Decision ID는 안정적으로 유지한다. 한 번 부여한 ID는 재사용하거나 재번호하지 않는다.
4. Status의 내부 기준값은 영문(CONFIRMED, TENTATIVE, TBD, DEFERRED)을 유지한다. 표시는 한글/색상 라벨(🟢 확정 / 🟡 잠정 / 🔴 미정 / 🔵 보류)을 사용한다.
5. Status가 바뀌면 카테고리별 상세 결정표뿐 아니라 Decision Summary, At a Glance, Open / Tentative / Deferred 모음 섹션도 함께 갱신한다.
6. 같은 결정이 카테고리 사이에 중복되어 보이는 경우 한 곳으로 합치되, 원래 ID와 정보는 보존한다(다른 곳은 참조만 남긴다).
7. 실제 secret, token, password, app key, app secret, 계좌번호, webhook URL 값은 본 문서에 절대 쓰지 않는다. 모두 `[REDACTED]`로만 표기한다.
8. AWS 리소스 생성 / 변경, 코드 / README / docs 외 운영 파일 수정은 본 문서 갱신과 분리한다. 본 문서 갱신은 결정 기록만 다룬다.

---

## Security Notes

본 문서 편집 · 갱신 회차 전반의 안전 제약이다.

- 실제 AWS 리소스 생성 · 수정 · 삭제 금지.
- 8개 MS 저장소(port-marketconnector · port-view · port-interest-crawler · port-interest-preprocessor · port_strategy_common · port_strategy_decision · port_strategy_research · port_strategy_execution) 의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 금지.
- 본 문서에 실제 secret · password · token · KIS app key · KIS app secret · Slack webhook URL · 계좌번호 · account-id · 실제 ARN · public IP · broker_order_no · image digest full sha256 원문 기록 금지. 모두 `[REDACTED*]` placeholder 만 사용.
- 본 문서는 후속 spec(03 ~ 10) 에서 입력으로만 사용. 실제 적용은 각 후속 spec 과 운영자 직접 작업.
- 쓰기 계열 git 명령(`git add` · `git commit` · `git rm` · `git mv` · `git push`) 실행 금지. 롤백 계열 git 명령(`git checkout` · `git reset` · `git stash` · `git restore`) 자동 실행 금지. git 상태 확인은 읽기 전용 3종(`git status --short` · `git diff --stat` · `git diff --check`) 만 사용.
