# Operator Decisions — AWS Migration

## Purpose

본 문서는 PORT-STRATEGY-AI AWS Migration의 운영자 결정을 관리하는 단일 진실원이다.

한 번 부여한 Decision ID는 재사용하거나 재번호를 부여하지 않는다.

각 결정은 `상태 / 결정 / 선택값 / 다음 검토 / 비용 영향 / 운영 주의 / 관련 spec`을 기본으로 하는 2열 표로 관리한다.

날짜별 검증 이력, executionName, ARN, commandId, revision, SHA256, DB 수치와 raw log는 본 문서에 누적하지 않는다.

상세 실행 근거는 각 spec의 `operation-notes.md`, 작업 이력은 `WORKLOG.md`, 리스크는 `risk-register.md`에서 관리한다.

민감정보 원문은 기록하지 않고 `[REDACTED]` 계열 placeholder만 사용한다.

## Status Legend

| 항목 | 값 |
| --- | --- |
| 🟢 확정 | 후속 spec의 기준값으로 사용 |
| 🟡 잠정 | 현재 채택했지만 후속 검증 후 변경 가능 |
| 🔴 미정 | 운영자 결정 필요 |
| 🔵 보류 | 별도 phase 또는 조건 충족 후 재검토 |

## Decision Dashboard

### 상태 요약

| 항목 | 값 |
| --- | --- |
| 전체 | 100건 |
| 🟢 확정 | 55건 |
| 🟡 잠정 | 42건 |
| 🔴 미정 | 2건 |
| 🔵 보류 | 1건 |
| 집계 기준 | 고유 Decision ID |

### 영역별 현황

| 항목 | 값 |
| --- | --- |
| Environment | 8건 |
| Network | 11건 |
| RDS | 9건 |
| Database | 12건 |
| Compute / Service | 40건 |
| Security / IAM | 8건 |
| Observability | 4건 |
| Cutover | 4건 |
| Safety | 4건 |

## At a Glance

### 핵심 환경·네트워크·RDS

| 항목 | 값 |
| --- | --- |
| OD-ENV-001 | AWS dev 환경 구축 여부 · 미구축 · 🟢 확정 |
| OD-ENV-003 | AWS 1차 구축 환경 · aws-paper · 🟢 확정 |
| OD-NET-001 | NAT Gateway 사용 (aws-paper) · 미사용 · 🟢 확정 |
| OD-NET-002 | NAT Gateway 사용 (aws-live) · 미사용(기본안) · 🟢 확정 |
| OD-NET-005 | VPC Endpoint 활성 항목 · 기본 세트(S3 GW + ECR api+dkr + Secrets + Logs) · SSM 선택형 · 🟢 확정 |
| OD-NET-009 | 운영자 접근 방식 · SSM Session Manager만 사용 · 🟢 확정 |
| OD-RDS-001 | aws-paper RDS 인스턴스 · db.t4g.small single-AZ · 🟢 확정 |
| OD-RDS-003 | aws-live RDS 안정성 우선안 · multi-AZ · 🟢 확정 |
| OD-CUT-001 | cutover 방식 · pg_dump+pg_restore 1순위 · 🟢 확정 |

### 핵심 자동화 안전

| 항목 | 값 |
| --- | --- |
| OD-SAFE-001 | aws-paper 자동 BUY/SELL E2E · 초기 차단 → 검증 후 허용 · 🟢 확정 |
| OD-SAFE-002 | aws-live 자동 BUY · 후보+수동 승인 우선, 검증 후 단계적 · 🟢 확정 |
| OD-SAFE-003 | aws-live 자동 SELL · OD-SAFE-002와 동일 정책 · 🟢 확정 |
| OD-SAFE-004 | 자동 재시도 정책 · idempotent step만 자동 재시도 · 🟢 확정 |

### 핵심 서비스 배치

| 항목 | 값 |
| --- | --- |
| OD-MS-001 | port-marketconnector 컴퓨트 · EC2+EIP · 🟢 확정 |
| OD-MS-002 | port-view 컴퓨트 · ECS Fargate Service (1순위), Elastic Beanstalk (2순위 비교 본문 유지) · 🟢 확정 |
| OD-MS-003 | port-interest-crawler 컴퓨트 · ECS Fargate Task NAT-free public (1순위), ECS on EC2 (Selenium 안정성 미달 시 승격) · 🟢 확정 |
| OD-MS-004 | port-interest-preprocessor 컴퓨트 · ECS Fargate Task (1순위), Lambda는 짧은 step만 보조 · 🟢 확정 |
| OD-MS-005 | port_strategy_common 배포 · 별도 컴퓨트 없음. git submodule packaging (1순위), wheel+CodeArtifact (성숙기 2순위) · 🟢 확정 |
| OD-MS-006 | port_strategy_decision 컴퓨트 · ECS Fargate Task + EventBridge Scheduler (1순위, 04에서 Step Functions 통합), Lambda 비권고 · 🟢 확정 |
| OD-MS-007 | port_strategy_execution 컴퓨트 · ECS Fargate Task + Step Functions + EventBridge Scheduler (1순위), Lambda 비권고 · 🟢 확정 |
| OD-MS-008 | port_strategy_research 컴퓨트 · AWS Batch (1순위, Step Functions 보조), ECS Fargate Task (2순위), Lambda 비권고 · 🟢 확정 |
| OD-MS-009 | Daily Batch orchestration · Step Functions + EventBridge Scheduler + ECS RunTask · 🟢 확정 |

## Decision Inventory

각 Decision ID는 한 번만 표시한다. 결정의 검증 과정과 날짜별 변경 내역은 관련 spec `operation-notes.md`에서 관리한다.

## Environment
### OD-ENV-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | AWS dev 환경 구축 여부 |
| 선택값 | 미구축 |
| 다음 검토 | 없음 |
| 비용 영향 | 큰 절감(별도 RDS/ECS/NAT/ALB 0) |
| 운영 주의 | 통합 검증은 aws-paper에서 수행 |
| 관련 spec | 02, 03, 04, 05, 08, 09 모두 dev 항목 제거 |

### OD-ENV-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | local-dev 운영 위치 |
| 선택값 | 기존 로컬 PostgreSQL 환경 유지 |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | 로컬과 aws-paper 사이 데이터 차이 관리 필요 |
| 관련 spec | 02 cutover, 10 cutover-runbook |

### OD-ENV-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | AWS 1차 구축 환경 |
| 선택값 | aws-paper |
| 다음 검토 | 없음 |
| 비용 영향 | 중 |
| 운영 주의 | 자동 주문은 paper 안에서 검증 |
| 관련 spec | 03, 04, 08 우선 적용 |

### OD-ENV-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | aws-live 구축 시점 |
| 선택값 | paper 검증 후 후속 구축 |
| 다음 검토 | 없음 |
| 비용 영향 | live 비용 보류 |
| 운영 주의 | live 자동매매 단계적 도입 |
| 관련 spec | 10 cutover-and-validation-runbook |

### OD-ENV-005

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | VPC 분리 정책 |
| 선택값 | 단일 VPC 유지 |
| 다음 검토 | 없음 |
| 비용 영향 | 절감 |
| 운영 주의 | 환경 사이 SG / Subnet 태그로 격리 |
| 관련 spec | 02 |

### OD-ENV-006

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | Paper 환경 DB source of truth |
| 선택값 | AWS Paper RDS 단일 source of truth |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 (RDS 비용은 OD-RDS-001 별도) |
| 운영 주의 | local DB 와 AWS Paper RDS 간 주문 / 체결 / 포지션 병합 / 동기화는 운영자 실수의 가장 큰 risk(R-DATA-007 정합). local 은 `LOCAL_DEV` fixture / 실험 / 백업 참고용으로만 사용. |
| 관련 spec | 02, 03, 04, 05, 08, 09, 10 |

### OD-ENV-007

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | Local PC → AWS Paper RDS 접속 방식 |
| 선택값 | SSM Port Forwarding 만 사용 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 (SSM Port Forwarding 자체 비용 없음) |
| 운영 주의 | RDS Public 허용 시 R-SEC-001 위반. DB host 가 `localhost` 라도 실제 대상은 AWS Paper RDS 일 수 있어 host 만으로 환경 식별 금지. |
| 관련 spec | 02, 03, 04, 05 |

### OD-ENV-008

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | Local DB 와 AWS Paper RDS 간 동기화 정책 |
| 선택값 | 미사용 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 |
| 운영 주의 | 동기화 도입 시 데이터 정합성 / 중복 주문 / fill 중복 / position 상태 충돌 risk(R-DATA-007). |
| 관련 spec | 02, 03, 04, 10 |

## Network
### OD-NET-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | NAT Gateway 사용 (aws-paper) |
| 선택값 | 미사용 |
| 다음 검토 | 없음 |
| 비용 영향 | ~$43+ /월 절감 |
| 운영 주의 | 인터넷 outbound 워크로드를 명시 분리해야 함 |
| 관련 spec | 02, 03, 08 |

### OD-NET-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | NAT Gateway 사용 (aws-live) |
| 선택값 | 미사용(기본안) |
| 다음 검토 | 없음 |
| 비용 영향 | ~$86+ /월 절감 |
| 운영 주의 | live AZ 장애 시 outbound는 public 워크로드에 의존 |
| 관련 spec | 02, 03, 08 |

### OD-NET-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | marketconnector outbound IP |
| 선택값 | EC2+EIP 유지 |
| 다음 검토 | 없음 |
| 비용 영향 | EC2 단가만 |
| 운영 주의 | broker IP 등록을 EIP에 묶음. EC2 교체 시 EIP detach/attach Runbook 필요 |
| 관련 spec | 03 marketconnector-ec2 |

### OD-NET-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | crawler/preprocessor outbound 방식 |
| 선택값 | public subnet + assignPublicIp (1순위) |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 |
| 운영 주의 | Task SG 실수 시 외부 노출 위험. Egress 검증 필요 |
| 관련 spec | 08 |

### OD-NET-005

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | VPC Endpoint 활성 항목 |
| 선택값 | 기본 유지 세트 = S3 Gateway + ECR api/dkr Interface + Secrets Manager Interface + CloudWatch Logs Interface |
| 선택값 보충 | SSM Endpoint는 필수 세트가 아니라 NAT-free 여부·public outbound 구조·운영 접근 경로에 따라 선택 |
| 선택값 보충 | 현재 aws-paper는 SSM Endpoint 제거 상태 |
| 다음 검토 | 없음 |
| 비용 영향 | Interface Endpoint 개수와 AZ 수에 비례(AZ 축소 시 감소) |
| 운영 주의 | 기본 세트 누락 시 NAT 없이 해당 AWS API 접근 불가 |
| 운영 주의 보충 | SSM Endpoint 유무는 SSM Session Manager 사용과 별개 개념 |
| 관련 spec | 02, 06, 07 |

### OD-NET-006

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | STS / KMS Endpoint 활성 |
| 선택값 | 우선 미사용. 필요 시 활성 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | AZ당 ~$8/월 추가 |
| 운영 주의 | 미사용 시 KMS 호출이 NAT가 없으면 실패할 수 있음 |
| 관련 spec | 02, 06 |

### OD-NET-007

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | ALB 사용 (aws-paper) |
| 선택값 | 초기 미사용 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | ~$16.5/월 절감 |
| 운영 주의 | port-view 운영자 접근은 SSM 포트포워딩 또는 internal IP |
| 관련 spec | 05 port-view-ecs |

### OD-NET-008

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | ALB 사용 (aws-live) |
| 선택값 | 초기 비용 절감안 보류 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | ~$16.5/월 절감 |
| 운영 주의 | live 운영자 접근 정책에 따라 재검토 |
| 관련 spec | 05 |

### OD-NET-009

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | 운영자 접근 방식 |
| 선택값 | SSM Session Manager만 사용 |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | EC2 SSH 22 inbound 0.0.0.0/0 절대 금지 |
| 관련 spec | 02, 03 |

### OD-NET-010

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | Local-to-AWS Paper RDS SSM Port Forwarding 표준 경유지 |
| 선택값 | `portfolio-paper-marketconnector-ec2` 단일 · local port `15433` → tunnel → AWS Paper RDS |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 (SSM Port Forwarding) |
| 운영 주의 | tunnel 종료 시 DB 접속 단절 (R-AUTO-012) |
| 관련 spec | 02, 03, 04, 05, 06 |

### OD-NET-011

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | Local-to-AWS Paper RDS pgAdmin4 사용 원칙 |
| 선택값 | `localhost:15433` (SSM tunnel) 만 등록. RDS endpoint 직접 등록 금지 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 |
| 운영 주의 | tunnel 미오픈 시 접속이 외부로 향하지 않음 (R-SEC-001) |
| 관련 spec | 02, 03, 04, 05, 10 |

## RDS
### OD-RDS-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | aws-paper RDS 인스턴스 |
| 선택값 | db.t4g.small single-AZ |
| 다음 검토 | 없음 |
| 비용 영향 | ~$26 + storage |
| 운영 주의 | dev 부재로 paper에 검증 부하 집중 |
| 관련 spec | 02, 04, 05 |

### OD-RDS-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | aws-live RDS 비용 절감안 |
| 선택값 | single-AZ 시작 가능 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | multi-AZ 대비 약 50% 절감 |
| 운영 주의 | AZ 장애 시 다운타임. PITR로만 복구 |
| 관련 spec | 02, 10 |

### OD-RDS-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | aws-live RDS 안정성 우선안 |
| 선택값 | multi-AZ |
| 다음 검토 | 없음 |
| 비용 영향 | 비용 절감안 대비 약 2배 |
| 운영 주의 | AZ failover 자동, 다운타임 최소 |
| 관련 spec | 02, 10 |

### OD-RDS-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | PostgreSQL major version |
| 선택값 | 16 이상 |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | minor auto upgrade 정책 paper/live 활성 |
| 관련 spec | 02 |

### OD-RDS-005

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | encryption at rest |
| 선택값 | aws-paper KMS default, aws-live CMK 권고 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | KMS key 관리 비용 작음 |
| 운영 주의 | CMK 사용 시 IAM 권한 매트릭스 추가 |
| 관련 spec | 02, 06 |

### OD-RDS-006

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | backup retention (aws-paper) |
| 선택값 | 7일 |
| 다음 검토 | 없음 |
| 비용 영향 | 작음 |
| 운영 주의 | 7일 이상 데이터 손실 시 외부 백업 필요 |
| 관련 spec | 02 |

### OD-RDS-007

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | backup retention (aws-live) |
| 선택값 | 14일 |
| 다음 검토 | 없음 |
| 비용 영향 | 중 |
| 운영 주의 | 비용 절감 시 7일까지 단축 가능 |
| 관련 spec | 02, 10 |

### OD-RDS-008

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | PITR |
| 선택값 | aws-paper on, aws-live on |
| 다음 검토 | 없음 |
| 비용 영향 | 작음 |
| 운영 주의 | 다른 시점 복구 가능 |
| 관련 spec | 02 |

### OD-RDS-009

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | manual snapshot 정책 |
| 선택값 | cutover 직전 + 분기 |
| 다음 검토 | 없음 |
| 비용 영향 | 매우 작음 |
| 운영 주의 | snapshot 명명 규칙(`before-cutover-{date}`) |
| 관련 spec | 02, 10 |

## Database
### OD-DB-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | DB 이름 |
| 선택값 | portfolio (모든 환경 동일) |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | 환경변수 호환성 유지 |
| 관련 spec | 02, 06 |

### OD-DB-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | schema 구성 |
| 선택값 | schema-per-domain 10개 유지 |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | 기존 search_path 유지 |
| 관련 spec | 02 |

### OD-DB-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | 환경변수 키 호환 |
| 선택값 | INTEREST_DB_* / PORT_* / PORTFOLIO_DB_NAME 모두 유지 |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | 코드 무수정 정책 강제 |
| 관련 spec | 02, 06 |

### OD-DB-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | DB role 분리 |
| 선택값 | 7개 role(marketconnector_app, crawler_app, preprocessor_app, decision_app, execution_app, research_app, view_app) |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | 권한 매트릭스 운영 부담 |
| 관련 spec | 02, 06 |

### OD-DB-005

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | view_app 권한 |
| 선택값 | 모든 schema READ + ops WRITE 기본. execution write는 05에서 재검토 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 |
| 운영 주의 | port-view에서 execution write가 필요하면 05에서 변경 |
| 관련 spec | 02, 05 |

### OD-DB-006

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | search_path 정책 |
| 선택값 | MS별 README 그대로 유지 |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | role별 ALTER ROLE SET search_path 적용 |
| 관련 spec | 02 |

### OD-DB-007

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | legacy schema의 app role 권한 |
| 선택값 | 모든 app role에 USAGE / SELECT 미부여(2026-06-09 1차 적용 결과 반영) |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | 02 design.md 매트릭스(legacy R 일부 부여) 대비 보안 강화. legacy 데이터 접근 필요한 MS 식별 시 별도 결정으로 grant |
| 관련 spec | 02, 05, 06 |

### OD-DB-008

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | marketconnector_app의 execution 권한 |
| 선택값 | R-only 축소(2026-06-09 1차 적용 반영) |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | execution write는 execution_app 단독으로 한정. 02 design.md 매트릭스(R/W) 대비 권한 분리 강화 |
| 관련 spec | 02, 03, 04 |

### OD-DB-009

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | view_app의 execution 권한 |
| 선택값 | R-only 유지(write 필요성은 05 spec에서 재검토) |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 |
| 운영 주의 | OD-DB-005를 본 결정으로 분리. View에서 execution write가 꼭 필요해지면 05에서 변경 |
| 관련 spec | 02, 05 |

### OD-DB-010

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | 1차 적용 시 기존 객체 owner 일괄 이관 (REASSIGN OWNED) |
| 선택값 | 미실행(기존 table / sequence / index owner는 `portfolio_admin` 유지) |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | schema owner는 `portfolio_owner`로 이관 완료. default privileges는 새 객체에만 자동 적용. 기존 객체 일괄 이관 여부는 후속 결정으로 분리 관리 |
| 관련 spec | 02, 06 |

### OD-DB-011

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | `execution_app` 의 `decision` schema UPDATE 권한 (Step 9 SELL execution link) |
| 선택값 | `decision.strategy_daily_position_decision` 제한적 UPDATE 만 부여 |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | UPDATE 권한이 `decision.strategy_daily_position_decision` 한 테이블에 한정. |
| 관련 spec | 02, 04 |

### OD-DB-012

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | `ops_recorder_app` 신규 role (Step Functions 실행 이력 OPS mirror 전용 · 2026-07-03) |
| 선택값 | 전용 최소 권한 role 신설 · `ops` schema USAGE + `ops.strategy_daily_batch_run` · `ops.strategy_daily_batch_step_log` SELECT · INSERT · UPDATE + 관련 sequence USAGE · SELECT |
| 선택값 보충 | DELETE 미부여 · `view_app` 재사용 안 함 · `execution_app` 권한 확대 안 함 · `chk_strategy_daily_batch_run_type` 에 `AWS_STEPFUNCTIONS` 값 추가(기존 `MANUAL` · `SCHEDULED` · `RETRY` |
| 선택값 보충 | `MANUAL_PARTIAL` 유지) |
| 다음 검토 | 없음 |

## Compute / Service
### OD-MS-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | port-marketconnector 컴퓨트 |
| 선택값 | EC2+EIP |
| 다음 검토 | 없음 |
| 비용 영향 | EC2 단가 + EIP attach 무료 |
| 운영 주의 | EC2 SG 실수 시 외부 노출. SSM + SG 통제 필수 |
| 관련 spec | 03 |

### OD-MS-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | port-view 컴퓨트 |
| 선택값 | ECS Fargate Service (1순위), Elastic Beanstalk (2순위 비교 본문 유지) |
| 다음 검토 | 없음 |
| 비용 영향 | Fargate per-task |
| 운영 주의 | ALB 도입 여부는 OD-NET-007 / 008 |
| P2 범위 | View 운영 보안·표시 고도화(ALB · HTTPS · Route53 · 인증 · Auto Scaling · Blue/Green · UI 고도화 · 외부 공개) 미수행 · 범위 제외 |
| P2 현재 상태 | ECS Fargate 1차 실증 상태 유지 · 외부 미공개 |
| P2 재검토 | 외부 공개 또는 다중 사용자 운영 필요 시 |
| 관련 spec | 05 |

### OD-MS-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | port-interest-crawler 컴퓨트 |
| 선택값 | ECS Fargate Task NAT-free public (1순위), ECS on EC2 (Selenium 안정성 미달 시 승격) |
| 관점 | 목표 컴퓨트 선택 관점(KRX headless 전환 가능 시 ECS 통합 재검토) |
| 관점 보충 | 현재 실제 운영은 Hybrid(OD-MS-011) |
| 다음 검토 | 없음 |
| 비용 영향 | Fargate per-task. NAT 없음 |
| 운영 주의 | Selenium / KRX 로그인 stateful. SG 통제 필수 |
| 관련 spec | 08 |

### OD-MS-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | port-interest-preprocessor 컴퓨트 |
| 선택값 | ECS Fargate Task (1순위), Lambda는 짧은 step만 보조 |
| 다음 검토 | 없음 |
| 비용 영향 | Fargate per-task |
| 운영 주의 | NAT-free에서 holiday API outbound는 public subnet 필요 |
| 관련 spec | 08 |

### OD-MS-005

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | port_strategy_common 배포 |
| 선택값 | 별도 컴퓨트 없음. git submodule packaging (1순위), wheel+CodeArtifact (성숙기 2순위) |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | 각 MS 이미지 빌드 시점 버전 동기화 부담 |
| 관련 spec | 07 |

### OD-MS-006

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | port_strategy_decision 컴퓨트 |
| 선택값 | ECS Fargate Task + EventBridge Scheduler (1순위, 04에서 Step Functions 통합), Lambda 비권고 |
| 다음 검토 | 없음 |
| 비용 영향 | Fargate per-task |
| 운영 주의 | 다중 schema read·write + 공통 라이브러리. Lambda timeout / connection 누수 위험 |
| 관련 spec | 04 |

### OD-MS-007

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | port_strategy_execution 컴퓨트 |
| 선택값 | ECS Fargate Task + Step Functions + EventBridge Scheduler (1순위), Lambda 비권고 |
| 다음 검토 | 없음 |
| 비용 영향 | Fargate per-task |
| 운영 주의 | live BUY/SELL 자동 재시도 금지 정책을 state machine 레벨에서 강제 |
| 관련 spec | 04, 10 |

### OD-MS-008

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | port_strategy_research 컴퓨트 |
| 선택값 | AWS Batch (1순위, Step Functions 보조), ECS Fargate Task (2순위), Lambda 비권고 |
| 다음 검토 | 없음 |
| 비용 영향 | 사용량 기반 |
| 운영 주의 | 장시간 backtest / RDS connection 누수 점검. report S3 보관 |
| 관련 spec | 09 |

### OD-MS-009

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | Daily Batch orchestration |
| 선택값 | Step Functions + EventBridge Scheduler + ECS RunTask |
| 다음 검토 | 없음 |
| 비용 영향 | Step Functions transitions ≪ ECS Task 비용 |
| 운영 주의 | 기존 port-view subprocess는 AWS에서 그대로 쓰지 않음 |
| 관련 spec | 04, 05 |

### OD-MS-010

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | infra alarm 채널 |
| 선택값 | 도메인 알림은 SlackNotificationService 유지, 인프라 알람은 SNS → Lambda → Slack webhook fan-out |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 무료 한도 안 |
| 운영 주의 | webhook URL은 Secrets Manager 또는 SSM SecureString. 06에서 최종 결정 |
| 관련 spec | 05, 10 |

### OD-MS-011

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | port-interest-crawler runtime 분리 (Hybrid execution model) |
| 선택값 | Hybrid(KRX GUI=Windows EC2 interactive worker · non-GUI=ECS Fargate Task). preprocessor=ECS Fargate Task |
| 관점 | 현재 실제 Hybrid 운영 모델 관점 |
| 관점 보충 | 목표 구조(KRX headless 전환 시 ECS 통합)는 OD-MS-003 |
| 관점 보충 | 서비스 배치 결론 유지 · 세부 운영 방식은 후속 검증 가능 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | EC2 idle + Fargate per-task |
| 운영 주의 | EC2 worker stop 절차 · 자동화 미도달 |
| 관련 spec | 08, 04, 05, 09, 10 |

### OD-MS-012

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | KRX GUI 의존 crawler 1차 운영 모드 |
| 선택값 | wrapper 기반 수동 실행(`run_krx_worker_daily.ps1`) |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 (wrapper 자체) |
| 운영 주의 | wrapper 성공이 DB 적재 성공을 보장 안 함 (R-AUTO-007) |
| 관련 spec | 08 |

### OD-MS-013

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | port_strategy_decision Task Definition 분리 정책 |
| 선택값 | buy-signal · position-signal 별도 Task Definition 2개 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | Fargate per-task |
| 운영 주의 | 호출 순서를 orchestration 레벨에서 강제 |
| 관련 spec | 04, 05 |

### OD-MS-014

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | port_strategy_common 1차 배포 방식 |
| 선택값 | 1차 ECS smoke image 에서는 vendoring |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 (vendoring) |
| 운영 주의 | 소스 동시 갱신 시 버전 불일치 위험 |
| 관련 spec | 04, 05, 07, 09 |

### OD-MS-015

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | KRX GUI 의존 crawler 1차 자동화 방식 |
| 선택값 | SSM RunCommand → schtasks → Scheduled Task → Autologon session → wrapper |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 (SSM/Task) |
| 운영 주의 | Autologon session 부재 시 KRX 로그인 실패 (R-AUTO-008) |
| 관련 spec | 08 |

### OD-MS-016

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | Strategy Execution / MarketConnector 주문 실행 책임 분리 |
| 선택값 | 책임 분리. Execution=READY→REQUESTED · Connector=REQUESTED→SUBMITTED/FAILED |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 |
| 운영 주의 | View Daily Batch Step 12가 Connector executor 호출 |
| 관련 spec | 03, 04, 05 |

### OD-MS-017

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | port_strategy_execution Task Definition 운영 방식 |
| 선택값 | 단일 Task Definition + command override |
| 다음 검토 | 후속 검토 |
| 비용 영향 | Task Def 수 감소, Fargate 비용 동일 |
| 운영 주의 | command override 매핑 오류 위험 (R-AUTO-014) |
| 관련 spec | 04, 05, 10 |

### OD-MS-018

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | port_strategy_research Batch image dependency boundary |
| 선택값 | Research 내부 adapter 로 이관. Batch image=research+common, decision 미포함 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 낮음 (image size 영향) |
| 운영 주의 | adapter 중복으로 common 계약 변경 시 갱신 누락 (R-DATA-008) |
| 관련 spec | 09, 07, 10 |

### OD-MS-019

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | port_strategy_research AWS Batch 포팅 대상 entrypoint + report artifact 보존 |
| 선택값 | 포팅 대상 = `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종 한정 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | Fargate 사용량 기반 |
| 운영 주의 | View Daily Batch → SubmitJob 매핑 전 운영자 수동 (R-AUTO-015) |
| 관련 spec | 09, 04, 05, 06, 07, 10 |

### OD-MS-020

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | port-interest-crawler 상태 표현 / 완료 정의 |
| 선택값 | Interest Crawler = **hybrid 1차 구현 부분 완료** |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 (표현 정정) |
| 운영 주의 | 잔존 표현 정기 grep + raw 최신일 SQL 점검 (R-DATA-009/010) |
| 관련 spec | 08, 04, 05, 09 |

### OD-MS-021

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | Backend AWS E2E dry-run 17단계 순서 + 안전 기준 |
| 선택값 | 로컬 View Daily Batch 17단계 순서 그대로. 안전 기준 8종 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 (paper 한정) |
| 운영 주의 | 안전 기준 위반은 R-AUTO-001 |
| 관련 spec | 08, 04, 05, 09, 10 |

### OD-MS-022

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | KRX GUI crawler 자동 로그인 기반 운영 방식 |
| 선택값 | Windows Autologon + Administrator session + Scheduled Task + SSM trigger |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 (Autologon/Task) |
| 운영 주의 | Autologon 은 paper Windows worker 한정 보안 예외 (R-SEC-009 등) |
| 관련 spec | 08, 05, 10 |

### OD-MS-023

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | Daily AWS wrapper 운영 정책 (운영자 로컬 PowerShell 도구) |
| 선택값 | 로컬 Windows PowerShell wrapper 분리 파일 구조(main + config + functions + step 17개) |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 (로컬 도구) |
| 운영 주의 | `-AllowPaperOrderExecute` 오사용 시 실주문 (R-AUTO-019) |
| 관련 spec | 03, 04, 05, 08, 09, 10 |

### OD-MS-024

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | 추가매수 허용 정책 + position_state 병합 방식 |
| 선택값 | 추가매수 허용 + merge. `merge_open_position_state()` 로 가중평균 병합 + idempotency |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 (정상 흐름) |
| 운영 주의 | idempotency 미적용 시 execution_order_id 중복 (R-DATA-012) |
| 관련 spec | 04, 05, 10 |

### OD-MS-025

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | MarketConnector `connector_order_check.py` 운영 모드 (Step 13 체결조회) |
| 선택값 | active 주문 단건 순차 조회 기본 + broad 옵션 격리 + 내부 분기 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 |
| 운영 주의 | broad 호출 감소로 summary fallback 노출 표면 감소 (R-AUTO-018) |
| 관련 spec | 03, 04, 05, 10 |

### OD-MS-026

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | Step 2 INTEREST_CRAWLER 운영 성공 기준 (wrapper 성공판정 강화) |
| 선택값 | Task trigger + Running→Ready wait + Last Result 0 + worker log + KRX raw DB validation 모두 충족 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 |
| 운영 주의 | Scheduled Task trigger 성공만 SUCCESS 처리하던 한계 축소 (R-AUTO-020) |
| 관련 spec | 08, 04, 05, 10 |

### OD-MS-027

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | MarketConnector env bootstrap 재생성 운영 정책 (`/tmp/inject-env.sh` 휘발 대응) |
| 선택값 | wrapper 공통 함수 재생성. Step 1/12/13/17 진입 직전 호출. |
| 다음 검토 | 03, 04, 06, 10 |
| 비용 영향 | 0 — wrapper 공통 함수 재생성 자체는 비용 없음. Secrets Manager `GetSecretValue` 호출은 기존 흐름과 동일. |
| 운영 주의 | MarketConnector EC2 stop / start 후 `/tmp` 휘발로 Step 1 / 12 / 13 / 17 실패 위험(R-AUTO-021 신규 / Mitigated). |

### OD-MS-028

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | Step 12 retry-normalizer 내장 정책 (장종료 REJECTED · `40580000` 후 다음날 자동 재제출 경로 대응) |
| 선택값 | Step 12 시작부 내장 · 복구 조건 6종 충족 시. |
| 다음 검토 | 03, 04, 10 |
| 비용 영향 | 0 — retry-normalizer 자체는 비용 모델 변경 없음. |
| 운영 주의 | 복구 조건이 광역으로 확장되면 의도하지 않은 broker 중복 주문 위험(R-AUTO-001 / R-BROKER-004 정합). `broker_order_no IS NULL` + `connector_fill` 없음 조건 유지로 mitigation. |

### OD-MS-029

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | Daily AWS Paper Step Functions approval workflow false / true path 운영 절차 |
| 선택값 | false path 사전 검증 후 true path 승인 실행. |
| 다음 검토 | 04, 10 |
| 비용 영향 | Step Functions transitions 비용 + 동일 ECS RunTask · AWS Batch · SSM RunCommand 사용량 — 비용 모델 변경 미미. |
| 운영 주의 | true path 진입 시 의도하지 않은 `allowPaperOrderExecute=true` 입력은 broker 실 주문 제출로 이어질 수 있음(R-AUTO-019 / R-AUTO-023 정합). |

### OD-MS-030

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | AWS 공통 Slack notifier Lambda 도입 정책 (운영 이벤트 알림 보조 계층) |
| 선택값 | Lambda `portfolio-event-notifier` 기반 AWS 공통 notifier. |
| 다음 검토 | 04, 05, 10 |
| 비용 영향 | 매우 낮음 — Lambda 1M requests + 400,000 GB-sec 무료 한도 안. Slack webhook 호출 자체 비용 없음. |
| 운영 주의 | webhook URL 환경변수 장기 보관 위험(R-AUTO-024 신규 / Accepted — 운영 안정화 후 Secrets Manager 또는 SSM SecureString 이전) + Slack 발송 실패 위험(R-AUTO-023 신규). |

### OD-MS-031

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | Step Functions / EventBridge 1차 Slack 연동 범위 3종 한정 정책 |
| 선택값 | 3종(APPROVAL_REQUIRED + DAILY_EXECUTION_SUCCESS/FAILED) 한정. |
| 다음 검토 | 04, 05, 10 |
| 비용 영향 | Step Functions transitions 비용 변화 미미 / Lambda 호출 비용 무료 한도 안 / 매 운영 회차 Slack 발송 3건 이내. |
| 운영 주의 | 본 결정의 핵심은 메시지 누락이 아니라 Step Functions Catch 경로에서 실패 알림이 끊기지 않는 것(R-AUTO-023 정합). |

### OD-MS-032

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | EventBridge Scheduler + Dispatcher Lambda 기반 Daily 자동화 구조 |
| 선택값 | 2개 Scheduler + Dispatcher Lambda + 단계적 활성화. |
| 다음 검토 | 04, 05, 10 |
| 비용 영향 | EventBridge Scheduler 14M / Lambda 1M requests 무료 한도 안 / Step Functions Standard transitions 비용은 OD-MS-009 정합. |
| 운영 주의 | Scheduler / Dispatcher Lambda / Step Functions 연결 실패 위험(R-AUTO-025 신규) — IAM simulate + Lambda dryRun + Scheduler get-schedule 상태 확인으로 mitigation. |

### OD-MS-033

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | Step 12 retry-normalizer 재시도 확장 + KIS rate-limit backoff + 09:01 ENABLE 보류 |
| 선택값 | `40580000` + `EGW00201` 재시도 · sleep/backoff · 09:01 auto ENABLE 보류. |
| 다음 검토 | 03, 04, 05, 10 |
| 비용 영향 | Step 12 entrypoint 실행 시간 영향 미미 / 운영 회차 누적 비용 모델 변경 없음. |
| 운영 주의 | 09:01 schedule 자동 ENABLE 보류 정책 위반 시 즉시 `disable-schedule` 호출 + 운영자 직접 SQL 점검(R-AUTO-025 / R-AUTO-026 정합). |

### OD-MS-034

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | EC2 lifecycle 자동 실행 구성 (07:50 start / Step 1~11 성공 시 Crawler stop / 15:50 stop) |
| 선택값 | EventBridge Scheduler + Lambda 기반 EC2 start/stop. |
| 다음 검토 | 03, 04, 05, 08, 10 |
| 비용 영향 | EventBridge Scheduler 14M / Lambda 1M requests 무료 한도 안 + EC2 lifecycle 비용 절감 효과(영업 시간 외 EC2 stop). |
| 운영 주의 | Scheduler / EC2 lifecycle Lambda / Step Functions 연결 실패 위험(R-AUTO-028 신규). |

### OD-MS-035

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | 장중 포지션 확인 3단계 구조 (Snapshot Refresh + Intraday Evaluate + Stop Sell Submit & Refresh) |
| 선택값 | 3단계 분리 + 신규 파일 + Submit 초기 수동·승인 후. |
| 다음 검토 | 03, 04, 05, 10 |
| 비용 영향 | EventBridge Scheduler + Lambda dispatcher 각 무료 한도 안 / Step Functions transitions Standard 매우 낮음 / aws-paper 한정 범위 안에서 누적 비용 증가 매우 낮음. |
| 운영 주의 | Stale snapshot(R-DATA-014) / Duplicate order(R-AUTO-029) / sellable_qty(R-DATA-015) / current_price(R-DATA-016) 신규 리스크 4종 모두 Mitigated. |

### OD-MS-036

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | Intraday Stop Sell Submit Workflow (장중 손절 주문 제출 전용 State Machine + 수동 승인 운영) |
| 선택값 | 별도 state machine `portfolio-paper-intraday-stop-sell-approval` + 수동 승인. |
| 다음 검토 | 03, 04, 05, 10 |
| 비용 영향 | Step Functions Standard transitions 비용 매우 낮음 / SSM RunCommand + ECS RunTask.sync 사용량 기준 / 누적 비용 증가 매우 낮음. |
| 운영 주의 | approval gate 오설정 위험(R-AUTO-031 신규) / Daily SELL vs Intraday Stop SELL 흐름 혼선 위험(R-AUTO-032 신규) / 보유 부재 상태 실주문 테스트 위험(R-BROKER-005 신규) 모두 Mitigated. |

### OD-MS-037

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | View Local AWS Paper read-only 1차 scope 확정 + ECS / Fargate 진입 전 batch 2차 검증 선행 정책 |
| 선택값 | read-only 1차 scope 확정 + ECS 진입 전 batch 2차 검증 선행. |
| 다음 검토 | 04, 05, 10 |
| 비용 영향 | SSM Port Forwarding 무료 / RDS Free Tier(`db.t4g.micro`) 안 / 누적 비용 증가 0건. |
| 운영 주의 | View Local 실행성 호출 위험(R-AUTO-033 신규) / SSM tunnel 종료 시 DB 접속 단절(R-AUTO-012 정합). |

### OD-MS-038

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | Daily Brief Slack mini workflow 운영 방식 (Daily 주문 실행 경로와 분리) |
| 선택값 | 별도 mini Step Functions + Builder Lambda + Notifier Lambda + Scheduler 2개. |
| 다음 검토 | 04, 05, 10 |
| 비용 영향 | mini Step Functions transitions + Lambda 호출 + Scheduler 2개 invocations 모두 매우 작음(평일 2회 = 월 약 44 invocation). |
| 운영 주의 | Daily Brief 알림 누락 / 중복 위험(R-AUTO-035 신규) / Slack webhook URL Lambda 환경변수 노출 위험(R-AUTO-024 정합) / DB password Lambda 환경변수 주입 위험은 Secrets Manager `valueFrom` 방식으로 완화. |

### OD-MS-039

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | Step Functions 실행 이력 OPS mirror 운영 방식 (Recorder Lambda + 전용 최소 권한 role · 2026-07-03) |
| 선택값 | Recorder Lambda `portfolio-daily-batch-ops-recorder`(Python 3.12 · ap-northeast-2 · VPC Lambda · Secrets Manager `ops_recorder_app` secret 사용 · action `RECORD_START` |
| 선택값 보충 | `RECORD_STEP` · `RECORD_SUCCESS` |
| 선택값 보충 | `RECORD_FAILURE`) 를 Step Functions 실행 role 에 `lambda:InvokeFunction` 한정 부여 후 State Machine 이 `ops.strategy_daily_batch_run` + `ops.strategy_daily_batch_step_log` 에 run-level + 대표 workflow step 을 mirror 한다 |
| 선택값 보충 | 대상 State Machine 은 `portfolio-paper-daily-step1-17-approval` + `portfolio-paper-daily-step12-17-approval` 2종 |
| 선택값 보충 | 1차 범위는 전체 세부 step mirror 가 아니라 run-level + 대표 workflow step 중심 · View 는 reader · controller 역할 유지 |
| 다음 검토 | 04, 05, 10 |

### OD-MS-040

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | ECS batch container Asia/Seoul timezone 정책 (Daily market-date 계산 포함 컨테이너 · 2026-07-08) |
| 선택값 | Daily market-date 계산을 포함하는 ECS batch container 는 UTC 기본값에 의존하지 않고 `TZ=Asia · Seoul` 을 명시한다 · 단기 조치는 TaskDefinition env 에 `TZ=Asia |
| 선택값 보충 | Seoul` 추가(`portfolio-paper-interest-crawler:8` |
| 선택값 보충 | `portfolio-paper-interest-preprocessor:2`) + State Machine `portfolio-paper-daily-step1-17-approval` 의 Step2A_RunInterestCrawlerNongui |
| 선택값 보충 | Step3_RunPreprocessor task revision 갱신 · 근본 개선은 코드에서 `datetime.now()` 직접 사용을 timezone-aware helper 로 대체하여 컨테이너 default TZ 의존을 제거하는 것(후속) |
| 다음 검토 | 04, 08 |

## Security / IAM
### OD-SEC-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 미정 |
| 결정 | Secrets 보관 위치 |
| 선택값 | 06에서 최종 결정. 본 spec은 placeholder |
| 다음 검토 | 운영자 결정 필요 |
| 비용 영향 | secret 수에 비례 |
| 운영 주의 | rotation 정책 미정 |
| 관련 spec | 06 |

### OD-SEC-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | RDS master password 보관 |
| 선택값 | Secrets Manager(권고) |
| 다음 검토 | 후속 검토 |
| 비용 영향 | $0.40/secret/월 |
| 운영 주의 | 06 결정에 따라 변경 가능 |
| 관련 spec | 06 |

### OD-SEC-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | KIS access_token 보관 |
| 선택값 | EC2 로컬+S3 backup 1순위 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 매우 작음 |
| 운영 주의 | EC2 교체 시 토큰 인계 Runbook 필수 |
| 관련 spec | 03 |

### OD-SEC-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | EC2 SSH 22 inbound |
| 선택값 | 미오픈. SSM Session Manager만 사용 |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | OD-NET-009와 동일 |
| 관련 spec | 02, 03 |

### OD-SEC-005

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | EC2 / 8개 MS Access Key 미사용 원칙 |
| 선택값 | EC2 안 access key 저장 금지. IMDSv2 + Instance Role 또는 ECS Task Role 만 사용 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 |
| 운영 주의 | 위반 시 IAM Console 즉시 폐기 + IMDSv2 + Role only 모드 복귀. |
| 관련 spec | 03, 04, 05, 06, 08, 09 |

### OD-SEC-006

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | EC2 / ECS IAM Role 기반 secret / parameter read 원칙 |
| 선택값 | 최소 권한. Resource wildcard 금지. Action wildcard 금지 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 |
| 운영 주의 | 정책 detach + 이전 정책 복구 / 정책 정적 검사로 wildcard 0건 점검 |
| 관련 spec | 03, 04, 05, 06, 08, 09 |

### OD-SEC-007

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | EC2 운영자 접근 = SSM Session Manager 중심 |
| 선택값 | `AmazonSSMManagedInstanceCore` attach + SSM Session Manager 진입 중심 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 0 |
| 운영 주의 | SSM Session Manager 접근에는 AWS API outbound 경로가 필요 |
| 운영 주의 보충 | 그 경로는 Public outbound 또는 SSM Interface Endpoint 중 하나일 수 있음 |
| 운영 주의 보충 | 현재 aws-paper는 SSM Endpoint 제거 상태 |
| 관련 spec | 03, 04, 05, 08, 09 |

### OD-SEC-008

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | KRX 로그인 자격 보관 |
| 선택값 | Secrets Manager (`/portfolio/{env}/krx/crawler-login`, JSON `username` / `password`). EC2 worker IAM Role inline poli... |
| 다음 검토 | 후속 검토 |
| 비용 영향 | $0.40/secret/월 + KMS 호출 미미 |
| 운영 주의 | secret read 권한 누락 시 worker 기동 실패. 권한 변경 시 운영자 노트에 4줄 요약 기록 |
| 관련 spec | 06, 08 |

## Observability
### OD-OBS-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | CloudWatch Logs 사용 |
| 선택값 | CloudWatch Logs 사용 |
| 다음 검토 | 없음 |
| 비용 영향 | retention 비례 |
| 운영 주의 | 비용 폭주 방지 위해 retention 짧게 시작 |
| 관련 spec | 04, 05, 08 |

### OD-OBS-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | CloudWatch Logs retention (aws-paper) |
| 선택값 | 7일 시작 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 작음 |
| 운영 주의 | 장애 분석 회고에 부족할 수 있음 |
| 관련 spec | 02, 05 |

### OD-OBS-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | CloudWatch Logs retention (aws-live) |
| 선택값 | 14일 시작, 운영 안정 후 30일로 상향 가능 |
| 다음 검토 | 후속 검토 |
| 비용 영향 | 중 |
| 운영 주의 | live 감사용 90일 검토 가능 |
| 관련 spec | 05, 10 |

### OD-OBS-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 미정 |
| 결정 | Slack webhook 보관 |
| 선택값 | 06에서 최종 결정 |
| 다음 검토 | 운영자 결정 필요 |
| 비용 영향 | 작음 |
| 운영 주의 | webhook URL 노출 시 외부 발신 위험 |
| 관련 spec | 06 |

## Cutover
### OD-CUT-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | cutover 방식 |
| 선택값 | pg_dump+pg_restore 1순위 |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | 짧은 다운타임 허용 |
| 관련 spec | 02, 10 |

### OD-CUT-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🔵 보류 |
| 결정 | AWS DMS 도입 |
| 선택값 | 보류 |
| 다음 검토 | 별도 phase에서 재검토 |
| 비용 영향 | DMS 인스턴스 비용 |
| 운영 주의 | 무중단이 꼭 필요해질 때 재검토 |
| 관련 spec | 10 |

### OD-CUT-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | aws-live cutover 시점 |
| 선택값 | paper 검증 후 |
| 다음 검토 | 없음 |
| 비용 영향 | live 비용 지연 |
| 운영 주의 | paper 검증 N영업일 운영자 결정 |
| 관련 spec | 10 |

### OD-CUT-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🟡 잠정 |
| 결정 | local-dev 유지 기간 |
| 선택값 | 병행 운영(rollback 보험용) |
| 다음 검토 | 후속 검토 |
| 비용 영향 | local 호스트 비용만 |
| 운영 주의 | 데이터 분기 관리 부담 |
| 관련 spec | 10 |

## Safety
### OD-SAFE-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | aws-paper 자동 BUY/SELL E2E |
| 선택값 | 초기 차단 → 검증 후 허용 |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | 모의투자라도 fill/position sync 오류는 재현해야 함 |
| Paper Daily 안정화 | 2026-07-22 정상 자동 회차 end-to-end 성공 · 1차 안정화 완료 |
| 확대 해석 금지 | aws-live 준비·장기 무장애·전체 AWS Migration 완료 아님 |
| broker 재시도 정책 | 자동 재시도 금지 유지(OD-SAFE-004) · 기존 실패 execution 이력 보존 |
| 관련 spec | 04, 10 |

### OD-SAFE-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | aws-live 자동 BUY |
| 선택값 | 후보+수동 승인 우선, 검증 후 단계적 |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | 1회 잘못된 자동 재시도가 큰 손실로 이어질 수 있음 |
| 관련 spec | 04, 05, 10 |

### OD-SAFE-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | aws-live 자동 SELL |
| 선택값 | OD-SAFE-002와 동일 정책 |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | 동상 |
| 관련 spec | 04, 05, 10 |

### OD-SAFE-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 확정 |
| 결정 | 자동 재시도 정책 |
| 선택값 | idempotent step만 자동 재시도 |
| 다음 검토 | 없음 |
| 비용 영향 | 0 |
| 운영 주의 | BUY/SELL/fill sync/position 변경/intraday stop SELL 생성은 재시도 금지 |
| 관련 spec | 04, 08, 10 |

## Review Queue

### 미정

| 항목 | 값 |
| --- | --- |
| OD-SEC-001 | Secrets 보관 위치 · 현재 선택값: 06에서 최종 결정. 본 spec은 placeholder · 다음: 운영자 결정 필요 |
| OD-OBS-004 | Slack webhook 보관 · 현재 선택값: 06에서 최종 결정 · 다음: 운영자 결정 필요 |

### 보류

| 항목 | 값 |
| --- | --- |
| OD-CUT-002 | AWS DMS 도입 · 보류 · 별도 phase에서 재검토 |

### 잠정 결정

| 항목 | 값 |
| --- | --- |
| 대상 | 42건 |
| 관리 방식 | 관련 spec 검증 후 확정·변경·보류 중 하나로 갱신 |
| 본 문서 | 최종 선택값과 상태만 유지 |
| 실행 근거 | 해당 spec `operation-notes.md`에 기록 |
| 날짜별 이력 | `WORKLOG.md`와 `CHANGELOG.md`에서 관리 |

## Evidence Management

| 항목 | 값 |
| --- | --- |
| 본 문서 | Decision ID · 상태 · 결정 · 선택값 · 다음 검토 · 관련 spec |
| 상세 실행 근거 | 각 spec `operation-notes.md` |
| 날짜별 작업 | `WORKLOG.md` |
| 문서 변경 | `CHANGELOG.md` |
| 관련 리스크 | `risk-register.md` |
| 서비스 비교 | `ms-aws-service-decision-matrix.md` |
| Decision Details | 별도 장문 섹션을 사용하지 않음 |
| Decision Change Log Details | 별도 장문 섹션을 사용하지 않음 |
| executionName · ARN · SHA256 | 본 문서에 기록하지 않음 |

## Decision Update Rules

| 항목 | 값 |
| --- | --- |
| 새 Decision | 해당 prefix의 마지막 번호 다음 ID 부여 |
| ID 재사용 | 금지 |
| 확정 변경 | 운영자 승인 후 선택값과 상태 갱신 |
| 잠정 승격 | 관련 spec 검증 완료 후 확정 가능 |
| 미정 | 운영자 선택 전까지 임의 값 확정 금지 |
| 보류 | 재검토 조건이 충족될 때만 상태 변경 |
| 상세 이력 | operation-notes와 WORKLOG에 기록 |
| 중복 금지 | 동일 Decision ID는 Inventory에 한 번만 표시 |
| 표 형식 | 독립 표는 `항목 / 값` 2열 |
| 긴 셀 | 여러 행으로 분리 |
| 민감정보 | `[REDACTED]` 계열 placeholder만 사용 |

## Security Notes

| 항목 | 값 |
| --- | --- |
| 실제 AWS 실행 | 없음 |
| 애플리케이션 코드 수정 | 없음 |
| AWS 리소스 변경 | 없음 |
| broker · KIS · DB 실행 | 없음 |
| 민감정보 원문 | 기록 금지 |
| 허용 표기 | `[REDACTED]` 계열 placeholder |
| 문서 역할 | AWS Migration 공통 Operator Decisions |
