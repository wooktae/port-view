# Implementation Plan — 02-aws-network-and-rds

본 작업 계획은 운영자가 AWS Console 또는 IaC로 직접 진행하기 위한 단계별 절차다. 환경 모델은 local-dev / aws-paper / aws-live 3개. 본 spec의 1차 적용 환경은 aws-paper이며, aws-live 적용은 `10-cutover-and-validation-runbook`에서 통합 진행한다.

각 task는 단일 세션에서 검토하고 승인할 수 있는 작은 단위로 분해한다. 비용에 영향이 큰 task는 "approval required"로 표시한다.

본 spec 범위에서는 실제 AWS 리소스 생성, 8개 MS 코드 / 기존 README / AGENTS.md / docs / CHANGELOG / worklog 수정, 8개 MS entrypoint 실행, 실제 cutover는 수행하지 않는다. 모든 secret은 `[REDACTED]`만 사용한다.

## Phase 0 — 사전 결정 (문서)

- [ ] 1. 비용 절감안 vs 안정성 우선안 라인 결정
  - `decision-matrix.md`의 비용 프로파일(low / realistic / stable)을 검토하고 aws-paper와 aws-live 각각의 라인을 결정한다.
  - 결과를 루트 공통 [`../_common/operator-decisions.md`](../_common/operator-decisions.md)의 해당 Decision ID 옆에 기록한다.
  - 본 task는 문서 작업.
  - _Requirements: 1, 11_

- [ ] 2. CIDR / AZ 선택
  - VPC CIDR(권고 `10.0.0.0/16`)이 사내 다른 네트워크와 충돌하지 않는지 확인.
  - 사용 AZ 2개 결정(권고 `ap-northeast-2a`, `ap-northeast-2c`).
  - _Requirements: 1, 2_

- [ ] 3. aws-paper / aws-live RDS 인스턴스 클래스와 multi-AZ 결정
  - aws-paper: `db.t4g.small` single-AZ 권고.
  - aws-live: 비용 절감안 single-AZ vs 안정성 우선안 multi-AZ 중 선택.
  - 결과를 루트 공통 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-RDS-002 / OD-RDS-003에 기록.
  - _Requirements: 5_

- [ ] 4. NAT-free 전략 적용 워크로드 매핑 결정
  - 인터넷 outbound가 필요한 워크로드를 OPT-1 / OPT-2 / OPT-3 / OPT-4 중 어디에 배치할지 결정한다.
  - 권고: marketconnector OPT-2, crawler / preprocessor / research OPT-1, 필요 시 crawler만 OPT-3 승격.
  - 결과를 루트 공통 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-NET-004에 반영.
  - _Requirements: 3_

- [ ] 5. VPC Endpoint 활성 항목 결정
  - 권고 5종(S3, ECR api+dkr, Secrets Manager, SSM, CloudWatch Logs) + 옵션(STS / KMS) 중 활성화 항목을 환경별로 결정.
  - 결과를 루트 공통 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-NET-005, OD-NET-006에 반영.
  - _Requirements: 3_

## Phase 1 — VPC / 네트워크 (AWS Console, aws-paper)

- [ ] 6. VPC 생성 (approval required, 비용 영향 0)
  - Console: VPC → Your VPCs → Create VPC.
  - 입력: name `portfolio-vpc`, CIDR `10.0.0.0/16`, IPv6 비활성, tenancy default.
  - 검증: VPC 목록에 `portfolio-vpc` 표시, state `available`.
  - rollback: VPC 삭제. 단 attach된 리소스가 있으면 삭제 불가.
  - _Requirements: 1, 2_

- [ ] 7. Subnet 6개 생성
  - public-a, public-b, app-a, app-b, data-a, data-b를 design.md CIDR 표 기준 생성.
  - 각 subnet AZ를 task 2 결정에 맞춘다.
  - 검증: 6개 subnet이 각 AZ에 매핑되어 있는지 확인.
  - rollback: 잘못 만든 subnet 삭제.
  - _Requirements: 2_

- [ ] 8. Internet Gateway 생성 / VPC attach
  - Console: VPC → Internet Gateways → Create.
  - VPC에 attach.
  - 검증: state `attached`.
  - rollback: detach 후 삭제.
  - _Requirements: 2_

- [ ] 9. (NAT 미사용 확정 task) NAT Gateway / NAT Instance 생성 안 함
  - 본 spec 결정에 따라 NAT 리소스를 만들지 않는다.
  - 해당 사실을 루트 공통 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-NET-001 / OD-NET-002에 CONFIRMED 상태로 기록.
  - _Requirements: 3_

- [ ] 10. Route Table 3종 생성 및 연결
  - rt-public: 0.0.0.0/0 → IGW. public-a, public-b 연결.
  - rt-app: VPC local만. 외부 라우트 없음. app-a, app-b 연결.
  - rt-data: VPC local만. 외부 라우트 없음. data-a, data-b 연결.
  - 검증: 각 Route Table의 routes / associations 확인.
  - rollback: 잘못 매핑된 라우트 제거.
  - _Requirements: 2_

- [ ] 11. Security Group 생성 (빈 규칙)
  - design.md SG 표의 SG들을 모두 생성: sg-marketconnector-ec2, sg-port-view-ecs, sg-strategy-tasks, sg-crawler-tasks, sg-preprocessor-tasks, sg-research-batch, sg-rds-postgres, sg-vpc-endpoints. (ALB SG는 ALB 도입 결정 시 추가.)
  - 규칙은 task 12에서 채운다.
  - _Requirements: 4_

- [ ] 12. SG 규칙 채우기 (RDS 우선)
  - sg-rds-postgres inbound: app SG들을 5432 source SG로 등록. 0.0.0.0/0 inbound 절대 미허용.
  - sg-vpc-endpoints inbound: app SG들 443 source SG로 등록.
  - sg-marketconnector-ec2 outbound: 0.0.0.0/0 (broker) + sg-rds-postgres + sg-vpc-endpoints.
  - sg-crawler-tasks outbound: 0.0.0.0/0 (KRX/Naver/yfinance) + sg-rds-postgres + sg-vpc-endpoints. inbound 절대 미허용.
  - sg-preprocessor-tasks outbound: 0.0.0.0/0 (holiday API) + sg-rds-postgres + sg-vpc-endpoints.
  - sg-port-view-ecs outbound: 0.0.0.0/0 (Slack), sg-marketconnector-ec2, sg-rds-postgres, VPC Endpoints.
  - sg-strategy-tasks / sg-research-batch outbound: design.md 표 기준.
  - 검증: SG 규칙을 Console에서 한 번 더 비교.
  - _Requirements: 4_

- [ ] 13. VPC Endpoint 생성 (approval required, 비용 영향 중)
  - S3 (Gateway) endpoint: rt-app, rt-data 연결.
  - ECR (api+dkr), Secrets Manager, SSM, CloudWatch Logs Interface endpoint: app subnet 배치, sg-vpc-endpoints attach, private DNS 활성.
  - (옵션) STS / KMS endpoint: task 5 결정에 따라 활성.
  - 검증: 각 endpoint state `available`. ECR endpoint `availabilityZones`에 app subnet AZ 모두 포함.
  - rollback: endpoint 삭제(단 NAT-free 환경에서는 endpoint 없으면 ECR pull 등 실패).
  - _Requirements: 3_

## Phase 2 — RDS (aws-paper)

- [ ] 14. RDS subnet group 생성
  - Console: RDS → Subnet groups → Create.
  - data-a, data-b 두 subnet 포함(multi-AZ 가능 형태).
  - 검증: status `Complete`.
  - _Requirements: 5_

- [ ] 15. RDS parameter group 생성
  - PostgreSQL 16 family용 parameter group `pg-portfolio-paper`.
  - `rds.force_ssl = 1`, `log_min_duration_statement = 1000`(필요 시 조정), `log_lock_waits = 1`, `idle_in_transaction_session_timeout = 60000`.
  - 검증: parameter group 상세에서 변경 사항 적용 확인.
  - _Requirements: 5_

- [ ] 16. (선행) DB master password를 Secrets Manager에 등록
  - secret name: `/portfolio/paper/rds/master`.
  - 값: `[REDACTED]`(운영자 직접 입력).
  - 본 task는 `06-secrets-and-iam` spec과 중복되므로 그쪽 spec이 먼저 진행됐다면 생략.
  - _Requirements: 7_

- [ ] 17. RDS instance 생성 (approval required, 비용 영향 큼)
  - 환경별 task 3 결정을 따른다(현 task는 aws-paper 진행).
  - 입력: engine PostgreSQL 16, 인스턴스 클래스 `db.t4g.small`, single-AZ, gp3 50 GB, encryption at rest 활성(KMS default), publicly accessible false, VPC subnet group, sg-rds-postgres, parameter group `pg-portfolio-paper`, backup retention 7일, PITR on.
  - master username: `portfolio_admin`. password: Secrets Manager 값(`[REDACTED]`).
  - DB name: 본 단계에서는 비워두고 task 18에서 `portfolio` DB 생성.
  - 검증: instance status `Available`. endpoint URL 확보.
  - rollback: instance 삭제(snapshot 옵션 결정 후).
  - _Requirements: 5, 9_

## Phase 3 — DB 초기화 (SQL via SSM)

운영자는 SSM Session Manager로 같은 VPC 내 ECS Task 또는 EC2에서 psql로 RDS에 접속한다. SSH 22 inbound는 절대 열지 않는다.

- [ ] 18. `portfolio` 데이터베이스 생성
  - psql로 master 계정 접속 후 `CREATE DATABASE portfolio;`.
  - 검증: `\l`로 portfolio DB 확인.
  - _Requirements: 6_

- [ ] 19. 10개 schema 생성
  - `CREATE SCHEMA IF NOT EXISTS reference;` 외 9개.
  - 검증: `SELECT schema_name FROM information_schema.schemata;`.
  - _Requirements: 6_

- [ ] 20. 7개 role 생성
  - `CREATE ROLE marketconnector_app LOGIN PASSWORD '[REDACTED]';` 외 6개.
  - 비밀번호는 Secrets Manager에 등록된 값을 운영자 직접 입력. 본 문서에는 적지 않는다.
  - 검증: `SELECT rolname FROM pg_roles;`.
  - _Requirements: 7_

- [ ] 21. role별 schema 권한 매트릭스 적용
  - design.md "schema별 권한 매트릭스" 표 그대로 적용.
  - `GRANT USAGE ON SCHEMA <schema> TO <role>;` + `GRANT SELECT ON ALL TABLES IN SCHEMA <schema> TO <role>;` + 필요한 경우 `GRANT INSERT, UPDATE, DELETE` + `ALTER DEFAULT PRIVILEGES`.
  - 검증: 권한 매트릭스 SQL을 한 role씩 점검.
  - _Requirements: 7_

- [ ] 22. role별 search_path 기본값 설정
  - `ALTER ROLE <role> SET search_path = <순서>;` 형태로 design.md / 8개 MS README의 search_path 순서 그대로 적용.
  - 검증: 각 role로 접속해 `SHOW search_path;`.
  - _Requirements: 6_

- [ ] 23. RDS endpoint와 환경변수 매핑 정리
  - `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME=portfolio`, `INTEREST_DB_USER=<role>`, `INTEREST_DB_PASSWORD=[REDACTED]`, `PORTFOLIO_DB_NAME=portfolio` 매핑을 운영자 노트에 기록.
  - 실제 값 등록은 `06-secrets-and-iam`에서.
  - _Requirements: 6, 7_

## Phase 4 — 검증 (read-only)

- [ ] 24. 검증 SQL 실행 (read-only)
  - design.md "검증 SQL 후보" 섹션 SQL을 순서대로 실행.
  - 결과를 운영자 노트에 기록(이 시점에는 row count 비어 있을 수 있음).
  - _Requirements: 8_

- [ ] 25. RDS 백업 동작 확인
  - automated backup 설정값과 첫 backup 생성 시각을 Console에서 확인.
  - manual snapshot 1회 생성: 이름 `before-cutover-paper`.
  - _Requirements: 9_

- [ ] 26. CloudWatch RDS 로그 / 메트릭 확인
  - PostgreSQL log group 활성 확인(`/aws/rds/instance/{db-id}/postgresql`).
  - DB Connections, FreeableMemory, CPUUtilization 메트릭 그래프 확인.
  - _Requirements: 9_

## Phase 5 — Cutover 사전 점검 (실제 cutover는 별도 spec)

- [ ] 27. local-dev → aws-paper 데이터 cutover 사전 점검
  - 본 spec 범위에서는 절차 문서화만. 실제 cutover는 `10-cutover-and-validation-runbook`에서 통합.
  - design.md "데이터 cutover 절차"의 단계 1~9를 운영자 노트에 미리 옮기고 cutover 일정을 별도 결정.
  - _Requirements: 8_

- [ ] 28. Rollback 기준 합의
  - design.md "Rollback 기준"의 항목을 운영자가 검토하고 추가 / 변경 사항을 본 task에 기록.
  - rollback 대상은 local-dev 환경으로 복귀(환경변수만 되돌리면 됨).
  - _Requirements: 8_

- [ ] 29. aws-paper 자동 BUY/SELL E2E 검증 사전 안전장치 확인
  - aws-paper에서 broker live로 호출이 발생하지 않는지 환경변수 / Secret prefix 분리 점검.
  - 1단계 주문 차단 모드 운영 합의(아직 코드 변경 없음, 운영자 노트만).
  - 자동 재시도 정책(BUY / SELL / fill sync / position 변경 / intraday stop SELL 생성 금지) 합의.
  - _Requirements: 10_

## 본 spec에서 하지 말아야 할 것

- 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 금지.
- 실제 AWS 리소스 생성을 본 task에서 강제하지 않는다. task는 운영자가 Console에서 직접 진행할 때의 절차서다.
- 실제 secret 값을 task / 검증 결과 / 운영자 노트에 적지 않는다(`[REDACTED]`).
- broker / KIS / Selenium / KRX / Naver / yfinance / 주문 / 체결 / Daily Batch / intraday monitor / 자동매매 호출 금지.
- 본 spec에서는 cutover dump / restore도 실제 실행하지 않는다(절차만 합의).
- aws-live 적용은 `10-cutover-and-validation-runbook`에서 통합 진행. 본 spec에서는 aws-paper까지만.
- Secrets / IAM 매트릭스 최종 결정은 `06-secrets-and-iam`에서 진행. 본 spec은 placeholder만 유지.
