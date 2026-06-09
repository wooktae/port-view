# Operator Decisions — AWS Migration

본 문서는 PORT-STRATEGY-AI AWS Migration 전체(01-aws-migration-foundation, 02-aws-network-and-rds 및 후속 03 ~ 10)의 운영자 결정을 누적해 기록하는 단일 진실원이다. 02 spec 시점에 작성을 시작했고, 03 ~ 10 spec에서 새 결정 항목이 생기면 본 파일에 동일 컬럼으로 누적한다(예: 06 spec의 OD-SEC-* 추가 결정, 03 spec의 OD-MC-* 등). 모든 후속 spec은 본 표를 입력으로 참조한다.

본 문서에는 실제 secret / password / token / app key / app secret / 계좌번호 / webhook URL 값을 적지 않는다. 모두 `[REDACTED]`만 사용한다.

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

## Decision Summary

본 문서에 누적된 결정 현황 한눈 요약이다. (집계 시점: 02 spec 기준)

### 결정 개수

- 전체: 59건
- 🟢 확정 (CONFIRMED): 42건
- 🟡 잠정 (TENTATIVE): 14건
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

## Detailed Decisions

카테고리별 상세 결정표다. 기존 Decision ID, 선택값, 비용 영향, 운영 리스크, 후속 spec 영향은 그대로 보존하며, Status 컬럼만 운영자 표시용 한글/색상 라벨로 표현한다.

### 1. Environment Decisions / 환경 구성

| Decision ID | 결정 항목 | 선택지 | 선택값 | Status | 비용 영향 | 운영 리스크 | 후속 Spec 영향 |
|-------------|-----------|--------|---------|--------|----------|------------|--------------|
| OD-ENV-001 | AWS dev 환경 구축 여부 | 구축 / 미구축 | 미구축 | 🟢 확정 | 큰 절감(별도 RDS/ECS/NAT/ALB 0) | 통합 검증은 aws-paper에서 수행 | 02, 03, 04, 05, 08, 09 모두 dev 항목 제거 |
| OD-ENV-002 | local-dev 운영 위치 | 기존 로컬 / 신규 | 기존 로컬 PostgreSQL 환경 유지 | 🟢 확정 | 0 | 로컬과 aws-paper 사이 데이터 차이 관리 필요 | 02 cutover, 10 cutover-runbook |
| OD-ENV-003 | AWS 1차 구축 환경 | aws-paper / aws-live | aws-paper | 🟢 확정 | 중 | 자동 주문은 paper 안에서 검증 | 03, 04, 08 우선 적용 |
| OD-ENV-004 | aws-live 구축 시점 | 즉시 / paper 검증 후 | paper 검증 후 후속 구축 | 🟢 확정 | live 비용 보류 | live 자동매매 단계적 도입 | 10 cutover-and-validation-runbook |
| OD-ENV-005 | VPC 분리 정책 | 단일 VPC / prod-nonprod 분리 | 단일 VPC 유지 | 🟢 확정 | 절감 | 환경 사이 SG / Subnet 태그로 격리 | 02 |

### 2. Network Decisions / 네트워크

| Decision ID | 결정 항목 | 선택지 | 선택값 | Status | 비용 영향 | 운영 리스크 | 후속 Spec 영향 |
|-------------|-----------|--------|---------|--------|----------|------------|--------------|
| OD-NET-001 | NAT Gateway 사용 (aws-paper) | NAT GW 1개 / NAT Instance / 미사용 | 미사용 | 🟢 확정 | ~$43+ /월 절감 | 인터넷 outbound 워크로드를 명시 분리해야 함 | 02, 03, 08 |
| OD-NET-002 | NAT Gateway 사용 (aws-live) | NAT GW multi-AZ / 미사용 | 미사용(기본안) | 🟢 확정 | ~$86+ /월 절감 | live AZ 장애 시 outbound는 public 워크로드에 의존 | 02, 03, 08 |
| OD-NET-003 | marketconnector outbound IP | EC2+EIP / NAT GW EIP / BYOIP | EC2+EIP 유지 | 🟢 확정 | EC2 단가만 | broker IP 등록을 EIP에 묶음. EC2 교체 시 EIP detach/attach Runbook 필요 | 03 marketconnector-ec2 |
| OD-NET-004 | crawler/preprocessor outbound 방식 | public subnet + publicIp / ECS on EC2 / NAT Instance | public subnet + assignPublicIp (1순위) | 🟡 잠정 | 0 | Task SG 실수 시 외부 노출 위험. Egress 검증 필요 | 08 |
| OD-NET-005 | VPC Endpoint 활성 항목 | 최소 / 권고 / 확장 | 권고(S3 GW + ECR api+dkr + Secrets + SSM + Logs) | 🟢 확정 | ~$30 ~ $80 /월 (AZ 수에 비례) | endpoint 누락 시 NAT 없이 AWS API 접근 불가 | 02, 06, 07 |
| OD-NET-006 | STS / KMS Endpoint 활성 | 활성 / 미사용 | 우선 미사용. 필요 시 활성 | 🟡 잠정 | AZ당 ~$8/월 추가 | 미사용 시 KMS 호출이 NAT가 없으면 실패할 수 있음 | 02, 06 |
| OD-NET-007 | ALB 사용 (aws-paper) | internal ALB / 미사용 | 초기 미사용 | 🟡 잠정 | ~$16.5/월 절감 | port-view 운영자 접근은 SSM 포트포워딩 또는 internal IP | 05 port-view-ecs |
| OD-NET-008 | ALB 사용 (aws-live) | internal ALB(안정성) / 미사용(절감) | 초기 비용 절감안 보류 | 🟡 잠정 | ~$16.5/월 절감 | live 운영자 접근 정책에 따라 재검토 | 05 |
| OD-NET-009 | 운영자 접근 방식 | SSH / SSM Session Manager | SSM Session Manager만 사용 | 🟢 확정 | 0 | EC2 SSH 22 inbound 0.0.0.0/0 절대 금지 | 02, 03 |

### 3. RDS / Database Decisions

#### 3-1. RDS 인스턴스 / 백업

| Decision ID | 결정 항목 | 선택지 | 선택값 | Status | 비용 영향 | 운영 리스크 | 후속 Spec 영향 |
|-------------|-----------|--------|---------|--------|----------|------------|--------------|
| OD-RDS-001 | aws-paper RDS 인스턴스 | db.t4g.micro / small / medium | db.t4g.small single-AZ | 🟢 확정 | ~$26 + storage | dev 부재로 paper에 검증 부하 집중 | 02, 04, 05 |
| OD-RDS-002 | aws-live RDS 비용 절감안 | single-AZ / multi-AZ | single-AZ 시작 가능 | 🟡 잠정 | multi-AZ 대비 약 50% 절감 | AZ 장애 시 다운타임. PITR로만 복구 | 02, 10 |
| OD-RDS-003 | aws-live RDS 안정성 우선안 | single-AZ / multi-AZ | multi-AZ | 🟢 확정 | 비용 절감안 대비 약 2배 | AZ failover 자동, 다운타임 최소 | 02, 10 |
| OD-RDS-004 | PostgreSQL major version | 14 / 15 / 16 / 17 | 16 이상 | 🟢 확정 | 0 | minor auto upgrade 정책 paper/live 활성 | 02 |
| OD-RDS-005 | encryption at rest | KMS default / CMK | aws-paper KMS default, aws-live CMK 권고 | 🟡 잠정 | KMS key 관리 비용 작음 | CMK 사용 시 IAM 권한 매트릭스 추가 | 02, 06 |
| OD-RDS-006 | backup retention (aws-paper) | 1 / 3 / 7 / 14일 | 7일 | 🟢 확정 | 작음 | 7일 이상 데이터 손실 시 외부 백업 필요 | 02 |
| OD-RDS-007 | backup retention (aws-live) | 7 / 14 / 35일 | 14일 | 🟢 확정 | 중 | 비용 절감 시 7일까지 단축 가능 | 02, 10 |
| OD-RDS-008 | PITR | on / off | aws-paper on, aws-live on | 🟢 확정 | 작음 | 다른 시점 복구 가능 | 02 |
| OD-RDS-009 | manual snapshot 정책 | 분기 / cutover 전 / 운영자 결정 | cutover 직전 + 분기 | 🟢 확정 | 매우 작음 | snapshot 명명 규칙(`before-cutover-{date}`) | 02, 10 |

#### 3-2. DB schema / role

| Decision ID | 결정 항목 | 선택지 | 선택값 | Status | 비용 영향 | 운영 리스크 | 후속 Spec 영향 |
|-------------|-----------|--------|---------|--------|----------|------------|--------------|
| OD-DB-001 | DB 이름 | portfolio / 환경별 분리 | portfolio (모든 환경 동일) | 🟢 확정 | 0 | 환경변수 호환성 유지 | 02, 06 |
| OD-DB-002 | schema 구성 | schema-per-domain / DB 분리 | schema-per-domain 10개 유지 | 🟢 확정 | 0 | 기존 search_path 유지 | 02 |
| OD-DB-003 | 환경변수 키 호환 | 변경 / 유지 | INTEREST_DB_* / PORT_* / PORTFOLIO_DB_NAME 모두 유지 | 🟢 확정 | 0 | 코드 무수정 정책 강제 | 02, 06 |
| OD-DB-004 | DB role 분리 | 단일 admin / role 분리 | 7개 role(marketconnector_app, crawler_app, preprocessor_app, decision_app, execution_app, research_app, view_app) | 🟢 확정 | 0 | 권한 매트릭스 운영 부담 | 02, 06 |
| OD-DB-005 | view_app 권한 | 읽기 전용 / read+ops write / read+ops+execution write | 모든 schema READ + ops WRITE 기본. execution write는 05에서 재검토 | 🟡 잠정 | 0 | port-view에서 execution write가 필요하면 05에서 변경 | 02, 05 |
| OD-DB-006 | search_path 정책 | 변경 / 유지 | MS별 README 그대로 유지 | 🟢 확정 | 0 | role별 ALTER ROLE SET search_path 적용 | 02 |
| OD-DB-007 | legacy schema의 app role 권한 | 부여 / 미부여 | 모든 app role에 USAGE / SELECT 미부여(2026-06-09 1차 적용 결과 반영) | 🟢 확정 | 0 | 02 design.md 매트릭스(legacy R 일부 부여) 대비 보안 강화. legacy 데이터 접근 필요한 MS 식별 시 별도 결정으로 grant | 02, 05, 06 |
| OD-DB-008 | marketconnector_app의 execution 권한 | R/W / R-only / 미부여 | R-only 축소(2026-06-09 1차 적용 반영) | 🟢 확정 | 0 | execution write는 execution_app 단독으로 한정. 02 design.md 매트릭스(R/W) 대비 권한 분리 강화 | 02, 03, 04 |
| OD-DB-009 | view_app의 execution 권한 | R-only / R+W | R-only 유지(write 필요성은 05 spec에서 재검토) | 🟡 잠정 | 0 | OD-DB-005를 본 결정으로 분리. View에서 execution write가 꼭 필요해지면 05에서 변경 | 02, 05 |
| OD-DB-010 | 1차 적용 시 기존 객체 owner 일괄 이관 (REASSIGN OWNED) | 즉시 실행 / 미실행 | 미실행(기존 table / sequence / index owner는 `portfolio_admin` 유지) | 🟢 확정 | 0 | schema owner는 `portfolio_owner`로 이관 완료. default privileges는 새 객체에만 자동 적용. 기존 객체 일괄 이관 여부는 후속 결정으로 분리 관리 | 02, 06 |

### 4. Compute / Service Placement Decisions

8개 MS별 AWS 컴퓨트 / orchestration 1순위 결정. 근거와 후보 비교는 루트 공통 [`ms-aws-service-decision-matrix.md`](./ms-aws-service-decision-matrix.md) 5장 / 4장 / 1.4장 참고. 본 표는 결정 락 기록만 다룬다.

| Decision ID | 결정 항목 | 선택지 | 선택값 | Status | 비용 영향 | 운영 리스크 | 후속 Spec 영향 |
|-------------|-----------|--------|---------|--------|----------|------------|--------------|
| OD-MS-001 | port-marketconnector 컴퓨트 | EC2+EIP / ECS Fargate / Beanstalk / App Runner / Lambda | EC2+EIP | 🟢 확정 | EC2 단가 + EIP attach 무료 | EC2 SG 실수 시 외부 노출. SSM + SG 통제 필수 | 03 |
| OD-MS-002 | port-view 컴퓨트 | ECS Fargate Service / Elastic Beanstalk / App Runner / EC2 / EKS | ECS Fargate Service (1순위), Elastic Beanstalk (2순위 비교 본문 유지) | 🟢 확정 | Fargate per-task | ALB 도입 여부는 OD-NET-007 / 008 | 05 |
| OD-MS-003 | port-interest-crawler 컴퓨트 | ECS Fargate Task (NAT-free public) / ECS on EC2 / EC2 / Lambda / Beanstalk | ECS Fargate Task NAT-free public (1순위), ECS on EC2 (Selenium 안정성 미달 시 승격) | 🟢 확정 | Fargate per-task. NAT 없음 | Selenium / KRX 로그인 stateful. SG 통제 필수 | 08 |
| OD-MS-004 | port-interest-preprocessor 컴퓨트 | ECS Fargate Task / Lambda / AWS Batch / EC2 | ECS Fargate Task (1순위), Lambda는 짧은 step만 보조 | 🟢 확정 | Fargate per-task | NAT-free에서 holiday API outbound는 public subnet 필요 | 08 |
| OD-MS-005 | port_strategy_common 배포 | git submodule packaging / wheel+CodeArtifact / 컴퓨트 배포 | 별도 컴퓨트 없음. git submodule packaging (1순위), wheel+CodeArtifact (성숙기 2순위) | 🟢 확정 | 0 | 각 MS 이미지 빌드 시점 버전 동기화 부담 | 07 |
| OD-MS-006 | port_strategy_decision 컴퓨트 | ECS Fargate Task + EventBridge / Lambda / AWS Batch / EC2 | ECS Fargate Task + EventBridge Scheduler (1순위, 04에서 Step Functions 통합), Lambda 비권고 | 🟢 확정 | Fargate per-task | 다중 schema read·write + 공통 라이브러리. Lambda timeout / connection 누수 위험 | 04 |
| OD-MS-007 | port_strategy_execution 컴퓨트 | ECS Fargate Task + Step Functions + EventBridge / ECS Service / Lambda / AWS Batch | ECS Fargate Task + Step Functions + EventBridge Scheduler (1순위), Lambda 비권고 | 🟢 확정 | Fargate per-task | live BUY/SELL 자동 재시도 금지 정책을 state machine 레벨에서 강제 | 04, 10 |
| OD-MS-008 | port_strategy_research 컴퓨트 | AWS Batch / ECS Fargate Task / Lambda / EC2 | AWS Batch (1순위, Step Functions 보조), ECS Fargate Task (2순위), Lambda 비권고 | 🟢 확정 | 사용량 기반 | 장시간 backtest / RDS connection 누수 점검. report S3 보관 | 09 |
| OD-MS-009 | Daily Batch orchestration | port-view subprocess / Step Functions + EventBridge + ECS RunTask / EKS CronJob | Step Functions + EventBridge Scheduler + ECS RunTask | 🟢 확정 | Step Functions transitions ≪ ECS Task 비용 | 기존 port-view subprocess는 AWS에서 그대로 쓰지 않음 | 04, 05 |
| OD-MS-010 | infra alarm 채널 | SlackNotificationService 단독 / SNS+Lambda+Slack 보조 | 도메인 알림은 SlackNotificationService 유지, 인프라 알람은 SNS → Lambda → Slack webhook fan-out | 🟡 잠정 | 무료 한도 안 | webhook URL은 Secrets Manager 또는 SSM SecureString. 06에서 최종 결정 | 05, 10 |

위 결정은 `ms-aws-service-decision-matrix.md` 5장 최종 권고안과 정합되며, 본 spec(02)와 후속 spec(03 ~ 10)에서 입력으로 사용한다. Lambda는 모든 핵심 batch 워크로드에서 비권고이며, infra alarm fan-out / 짧은 보조 후처리 / S3 metadata 처리 같은 보조 용도로만 사용한다.

### 5. Security / Secrets / IAM Decisions

| Decision ID | 결정 항목 | 선택지 | 선택값 | Status | 비용 영향 | 운영 리스크 | 후속 Spec 영향 |
|-------------|-----------|--------|---------|--------|----------|------------|--------------|
| OD-SEC-001 | Secrets 보관 위치 | Secrets Manager / SSM SecureString / 혼합 | 06에서 최종 결정. 본 spec은 placeholder | 🔴 미정 | secret 수에 비례 | rotation 정책 미정 | 06 |
| OD-SEC-002 | RDS master password 보관 | Secrets Manager / 직접 입력 | Secrets Manager(권고) | 🟡 잠정 | $0.40/secret/월 | 06 결정에 따라 변경 가능 | 06 |
| OD-SEC-003 | KIS access_token 보관 | EC2 로컬+S3 backup / EFS / Secrets Manager | EC2 로컬+S3 backup 1순위 | 🟡 잠정 | 매우 작음 | EC2 교체 시 토큰 인계 Runbook 필수 | 03 |
| OD-SEC-004 | EC2 SSH 22 inbound | 0.0.0.0/0 / 미오픈 | 미오픈. SSM Session Manager만 사용 | 🟢 확정 | 0 | OD-NET-009와 동일 | 02, 03 |

### 6. Observability / Alerting Decisions

| Decision ID | 결정 항목 | 선택지 | 선택값 | Status | 비용 영향 | 운영 리스크 | 후속 Spec 영향 |
|-------------|-----------|--------|---------|--------|----------|------------|--------------|
| OD-OBS-001 | CloudWatch Logs 사용 | 사용 / Logs 외 도구 | CloudWatch Logs 사용 | 🟢 확정 | retention 비례 | 비용 폭주 방지 위해 retention 짧게 시작 | 04, 05, 08 |
| OD-OBS-002 | CloudWatch Logs retention (aws-paper) | 7 / 14 / 30일 | 7일 시작 | 🟡 잠정 | 작음 | 장애 분석 회고에 부족할 수 있음 | 02, 05 |
| OD-OBS-003 | CloudWatch Logs retention (aws-live) | 14 / 30 / 90일 | 14일 시작, 운영 안정 후 30일로 상향 가능 | 🟡 잠정 | 중 | live 감사용 90일 검토 가능 | 05, 10 |
| OD-OBS-004 | Slack webhook 보관 | Secrets Manager / SSM SecureString | 06에서 최종 결정 | 🔴 미정 | 작음 | webhook URL 노출 시 외부 발신 위험 | 06 |

### 7. CI/CD Decisions

본 시점(02 spec)에서는 CI/CD 단독 결정이 아직 락되지 않았다. 컴퓨트 결정(OD-MS-* 4번 카테고리)이 우선이며, 본 카테고리는 후속 spec(주로 07 / 08 / 09)에서 추가된다.

- 신규 결정이 발생하면 `OD-CICD-XXX` 형태로 본 섹션에 추가한다.
- 현재는 placeholder 상태로 카테고리만 유지한다.

### 8. Cutover / Operation Decisions

#### 8-1. Cutover

| Decision ID | 결정 항목 | 선택지 | 선택값 | Status | 비용 영향 | 운영 리스크 | 후속 Spec 영향 |
|-------------|-----------|--------|---------|--------|----------|------------|--------------|
| OD-CUT-001 | cutover 방식 | pg_dump+pg_restore / AWS DMS / snapshot 복원 | pg_dump+pg_restore 1순위 | 🟢 확정 | 0 | 짧은 다운타임 허용 | 02, 10 |
| OD-CUT-002 | AWS DMS 도입 | 즉시 / 보류 | 보류 | 🔵 보류 | DMS 인스턴스 비용 | 무중단이 꼭 필요해질 때 재검토 | 10 |
| OD-CUT-003 | aws-live cutover 시점 | paper 검증 후 / 즉시 | paper 검증 후 | 🟢 확정 | live 비용 지연 | paper 검증 N영업일 운영자 결정 | 10 |
| OD-CUT-004 | local-dev 유지 기간 | aws-paper 가동 후 즉시 종료 / 병행 운영 | 병행 운영(rollback 보험용) | 🟡 잠정 | local 호스트 비용만 | 데이터 분기 관리 부담 | 10 |

#### 8-2. 운영 안전장치 (자동화 / safety)

| Decision ID | 결정 항목 | 선택지 | 선택값 | Status | 비용 영향 | 운영 리스크 | 후속 Spec 영향 |
|-------------|-----------|--------|---------|--------|----------|------------|--------------|
| OD-SAFE-001 | aws-paper 자동 BUY/SELL E2E | 차단 / 허용 | 초기 차단 → 검증 후 허용 | 🟢 확정 | 0 | 모의투자라도 fill/position sync 오류는 재현해야 함 | 04, 10 |
| OD-SAFE-002 | aws-live 자동 BUY | 즉시 자동 / 후보+수동 승인 / 단계적 자동 | 후보+수동 승인 우선, 검증 후 단계적 | 🟢 확정 | 0 | 1회 잘못된 자동 재시도가 큰 손실로 이어질 수 있음 | 04, 05, 10 |
| OD-SAFE-003 | aws-live 자동 SELL | 동상 | OD-SAFE-002와 동일 정책 | 🟢 확정 | 0 | 동상 | 04, 05, 10 |
| OD-SAFE-004 | 자동 재시도 정책 | 모든 step / idempotent만 | idempotent step만 자동 재시도 | 🟢 확정 | 0 | BUY/SELL/fill sync/position 변경/intraday stop SELL 생성은 재시도 금지 | 04, 08, 10 |

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
| OD-RDS-002 | aws-live RDS 비용 절감안 | single-AZ 시작 가능 | 🟡 잠정 | 02, 10 |
| OD-RDS-005 | encryption at rest | aws-paper KMS default, aws-live CMK 권고 | 🟡 잠정 | 02, 06 |
| OD-DB-005 | view_app 권한 | 모든 schema READ + ops WRITE 기본. execution write는 05에서 재검토 | 🟡 잠정 | 02, 05 |
| OD-DB-009 | view_app의 execution 권한 | R-only 유지(write 필요성은 05 spec에서 재검토) | 🟡 잠정 | 02, 05 |
| OD-CUT-004 | local-dev 유지 기간 | 병행 운영(rollback 보험용) | 🟡 잠정 | 10 |
| OD-OBS-002 | CloudWatch Logs retention (aws-paper) | 7일 시작 | 🟡 잠정 | 02, 05 |
| OD-OBS-003 | CloudWatch Logs retention (aws-live) | 14일 시작, 운영 안정 후 30일로 상향 가능 | 🟡 잠정 | 05, 10 |
| OD-SEC-002 | RDS master password 보관 | Secrets Manager(권고) | 🟡 잠정 | 06 |
| OD-SEC-003 | KIS access_token 보관 | EC2 로컬+S3 backup 1순위 | 🟡 잠정 | 03 |
| OD-MS-010 | infra alarm 채널 | 도메인 알림은 SlackNotificationService 유지, 인프라 알람은 SNS → Lambda → Slack webhook fan-out | 🟡 잠정 | 05, 10 |

---

## Deferred Decisions / 🔵 보류 결정

상태가 `DEFERRED`인 항목만 모았다. 본 spec 범위 밖으로 보류했고, 별도 시점에 재검토한다.

| Decision ID | 결정 항목 | 선택값(현재) | Status | 재검토 조건 |
|-------------|-----------|--------------|--------|-------------|
| OD-CUT-002 | AWS DMS 도입 | 보류 | 🔵 보류 | 무중단 cutover가 꼭 필요해질 때(10 spec 시점) 재검토 |

---

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

## 본 spec 작업 안전 제약

- 실제 AWS 리소스 생성 / 변경 금지
- 8개 MS의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 금지
- 본 문서에 실제 secret 값 출력 금지(모두 `[REDACTED]`)
- 06-secrets-and-iam은 본 작업 시점에 작성하지 않는다
- 본 표는 후속 spec에서 입력으로만 사용. 실제 적용은 각 후속 spec과 운영자 직접 작업.

---

## Change Log

| 일자 | 변경 내용 | 비고 |
|------|-----------|------|
| 2026-06-05 | 문서 구조 가독성 개선 | 실제 AWS 결정값(Decision ID, 선택지, 선택값, 비용 영향, 운영 리스크, 후속 spec 영향)은 변경하지 않았다. Status 내부 기준값(CONFIRMED / TENTATIVE / TBD / DEFERRED)도 변경하지 않았다. 운영자 표시용으로 한글/색상 라벨(🟢 확정 / 🟡 잠정 / 🔴 미정 / 🔵 보류)을 표 안에 함께 사용하도록 표시 형식만 개선했다. Status Legend, Decision Summary, At a Glance, 카테고리별 상세 결정표 재정리, Open / Tentative / Deferred 결정 모음, Decision Update Rules, Change Log 섹션을 추가했다. |
| 2026-06-09 | OD-DB-007 ~ OD-DB-010 추가 (DB Role / 권한 1차 적용 결과 반영) | 2026-06-09 운영자가 직접 실행한 [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) §4 SQL 결과를 반영했다. 신규 결정: OD-DB-007(legacy schema 모든 app role 미부여, 🟢 확정), OD-DB-008(marketconnector_app execution R-only, 🟢 확정), OD-DB-009(view_app execution R-only, write는 05에서 재검토, 🟡 잠정), OD-DB-010(1차 적용에서 REASSIGN OWNED BY portfolio_admin TO portfolio_owner 미실행, 🟢 확정). OD-DB-005는 OD-DB-009로 분리되었으나 본문은 보존한다(중복 row 미생성). 비밀번호 / endpoint hostname / 계좌번호는 본 문서에 평문 기록 금지(`[REDACTED]`). 결정값 변경 외 본 일자에 추가된 운영 결과 기록은 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-09 섹션 참조. |
