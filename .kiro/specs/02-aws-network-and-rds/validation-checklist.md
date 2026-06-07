# Validation Checklist — 02-aws-network-and-rds

본 체크리스트는 운영자가 [`./runbook.md`](./runbook.md)의 각 Step을 진행한 뒤 통과 여부를 한 줄씩 확인하기 위한 점검표다. 모든 항목이 체크되어야 본 spec 1차(aws-paper) 진행이 마무리된다.

본 문서에는 실제 secret / password / token / app key / app secret / 계좌번호 / webhook URL 값을 적지 않는다. 모두 `[REDACTED]`만 사용한다.

## 1. Pre-flight Checklist

- [ ] AWS Region이 `ap-northeast-2`인지 확인
- [ ] aws-paper 대상 작업인지 확인 (OD-ENV-003)
- [ ] 본 작업이 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog를 수정하지 않는지 확인
- [ ] 실제 secret 값이 본 spec / 운영자 노트 / 콘솔 캡처에 노출되지 않았는지 확인
- [ ] 비용 발생 리소스(VPC Endpoint, RDS, EIP) 생성 전 운영자 승인 여부 확인 (R-COST-001)
- [ ] [`../operator-decisions.md`](../operator-decisions.md) `At a Glance`의 핵심 결정 락 상태 확인 (OD-NET-001 / OD-NET-005 / OD-NET-009 / OD-RDS-001 / OD-CUT-001)
- [ ] [`./decision-matrix.md`](./decision-matrix.md) 비용 프로파일 라인(low / realistic / stable) 결정 기록 존재
- [ ] [`../risk-register.md`](../risk-register.md)의 R-NET-001 ~ R-COST-002 항목 인지

## 2. Network Validation

- [ ] VPC `portfolio-vpc` 생성 확인. State = `Available`. CIDR = `10.0.0.0/16`
- [ ] VPC Tags에 `env=paper`, `project=portfolio` 적용
- [ ] Public subnet 2개(`public-a` 10.0.0.0/24, `public-b` 10.0.1.0/24) 생성
- [ ] Private app subnet 2개(`app-a` 10.0.10.0/24, `app-b` 10.0.11.0/24) 생성
- [ ] Private data subnet 2개(`data-a` 10.0.20.0/24, `data-b` 10.0.21.0/24) 생성
- [ ] 6개 subnet의 AZ 매핑(a / c)이 의도와 일치
- [ ] Internet Gateway `portfolio-igw` 생성 + `portfolio-vpc`에 attach (State = `Attached`)
- [ ] NAT Gateway 미생성 확인 (NAT Gateways 메뉴 비어 있음, R-COST-002)
- [ ] NAT 역할 EC2 미생성 확인 (Name `nat-*` 인스턴스 없음)
- [ ] Route Table 3종 생성: `rt-public`, `rt-app`, `rt-data`
- [ ] `rt-public` Routes에 `0.0.0.0/0 → portfolio-igw` 존재
- [ ] `rt-app`, `rt-data` Routes는 VPC local만 존재 (외부 라우트 없음, R-NET-002)
- [ ] Subnet associations: `rt-public` ↔ public-a/b, `rt-app` ↔ app-a/b, `rt-data` ↔ data-a/b

## 3. Security Group Validation

- [ ] SG 8개 생성: `sg-marketconnector-ec2`, `sg-port-view-ecs`, `sg-strategy-tasks`, `sg-crawler-tasks`, `sg-preprocessor-tasks`, `sg-research-batch`, `sg-rds-postgres`, `sg-vpc-endpoints`
- [ ] `sg-rds-postgres` inbound = app/EC2 SG 6개 참조(5432)만. **0.0.0.0/0 inbound 없음** (R-SEC-001)
- [ ] `sg-rds-postgres` outbound 비어 있음
- [ ] `sg-vpc-endpoints` inbound = app/EC2 SG 6개 참조(443)만
- [ ] `sg-marketconnector-ec2` inbound = `sg-port-view-ecs` (TCP 5000 또는 운영자 결정 포트)
- [ ] `sg-marketconnector-ec2` outbound = HTTPS 0.0.0.0/0 + sg-rds-postgres + sg-vpc-endpoints
- [ ] `sg-port-view-ecs` inbound 비어 있음 (또는 운영자 IP allowlist만)
- [ ] `sg-port-view-ecs` outbound = HTTPS 0.0.0.0/0 + sg-marketconnector-ec2 + sg-rds-postgres + sg-vpc-endpoints
- [ ] `sg-crawler-tasks` inbound 비어 있음 (R-NET-001)
- [ ] `sg-crawler-tasks` outbound = HTTPS 0.0.0.0/0 + sg-rds-postgres + sg-vpc-endpoints
- [ ] `sg-preprocessor-tasks` inbound 비어 있음
- [ ] `sg-preprocessor-tasks` outbound = HTTPS 0.0.0.0/0 + sg-rds-postgres + sg-vpc-endpoints
- [ ] `sg-strategy-tasks` outbound = sg-marketconnector-ec2 + sg-rds-postgres + sg-vpc-endpoints (+ 필요 시 HTTPS 0/0)
- [ ] `sg-research-batch` outbound = sg-rds-postgres + sg-vpc-endpoints (+ 필요 시 HTTPS 0/0)
- [ ] EC2 SSH 22 inbound 0.0.0.0/0 SG 어디에도 없음 (OD-NET-009)

## 4. VPC Endpoint Validation

- [ ] S3 Gateway endpoint(`vpce-s3-gw`) 생성. `rt-app`, `rt-data` 연결
- [ ] ECR API Interface endpoint(`vpce-ecr-api`) State = `Available`. AZ에 app-a, app-b 포함
- [ ] ECR DKR Interface endpoint(`vpce-ecr-dkr`) State = `Available`
- [ ] Secrets Manager Interface endpoint(`vpce-secretsmanager`) State = `Available`
- [ ] SSM Interface endpoint(`vpce-ssm`) State = `Available`
- [ ] CloudWatch Logs Interface endpoint(`vpce-logs`) State = `Available`
- [ ] 모든 Interface endpoint Private DNS = `enabled`
- [ ] 모든 Interface endpoint SG = `sg-vpc-endpoints`
- [ ] (옵션) STS / KMS endpoint는 OD-NET-006 결정에 따라 활성 / 미활성

## 5. RDS Validation

- [ ] RDS Subnet Group `portfolio-paper-subnet-group` Status = `Complete`. data-a, data-b 포함
- [ ] RDS Parameter Group `pg-portfolio-paper` 생성. family = `postgres16`
- [ ] Parameter group 적용 값:
  - [ ] `rds.force_ssl = 1`
  - [ ] `log_min_duration_statement = 1000`
  - [ ] `log_lock_waits = 1`
  - [ ] `idle_in_transaction_session_timeout = 60000`
- [ ] (선행) Secrets Manager `/portfolio/paper/rds/master` 등록. 값은 `[REDACTED]`
- [ ] RDS instance `portfolio-paper-rds` Status = `Available`
- [ ] RDS engine = PostgreSQL 16.x
- [ ] RDS instance class = `db.t4g.small` (OD-RDS-001)
- [ ] RDS deployment = Single-AZ
- [ ] RDS storage = gp3 50 GB
- [ ] RDS encryption at rest = Enabled (KMS default)
- [ ] RDS Performance Insights = Enabled
- [ ] RDS VPC = `portfolio-vpc`, subnet group = `portfolio-paper-subnet-group`, SG = `sg-rds-postgres`
- [ ] RDS Publicly accessible = `No` (R-SEC-001)
- [ ] RDS Backup retention = 7 days (OD-RDS-006)
- [ ] RDS PITR = Enabled (OD-RDS-008)
- [ ] RDS Deletion protection = Enabled
- [ ] RDS DB parameter group = `pg-portfolio-paper` (Status `in-sync`)
- [ ] RDS Endpoint URL이 운영자 노트에 기록(`INTEREST_DB_HOST`)

## 6. DB / schema / role 준비 Validation

본 spec은 SQL 실행을 운영자에게 위임한다. 본 섹션은 SQL이 실행된 경우(03 / 06 spec과 통합) 점검 항목이다.

- [ ] `portfolio` 데이터베이스 생성 (psql `\l`로 확인)
- [ ] 10개 schema 생성: `reference, interest, preprocessor, research, decision, execution, connector, ops, legacy, public`
- [ ] 7개 role 생성: `marketconnector_app, crawler_app, preprocessor_app, decision_app, execution_app, research_app, view_app`
- [ ] role별 schema 권한 매트릭스가 [`./design.md`](./design.md) 표와 일치
- [ ] role별 search_path = 8개 MS README 정의대로 적용 (R-DATA-001)
- [ ] role 비밀번호는 Secrets Manager / SSM SecureString placeholder만 기록 (`[REDACTED]`)

## 7. Cutover 사전 준비 Validation

- [ ] pg_dump 명령 형식 운영자 노트에 합의 (`--format=custom --no-owner --no-privileges`)
- [ ] pg_restore 명령 형식 운영자 노트에 합의 (`--no-owner --no-privileges --jobs=N`)
- [ ] dump 파일 보관 위치 / 삭제 정책 합의
- [ ] [`./design.md`](./design.md) "검증 SQL 후보" 섹션을 cutover 검증에 사용한다는 합의
- [ ] [`./design.md`](./design.md) "Rollback 기준" 합의 (R-DATA-002 mitigation 일치)
- [ ] aws-paper에서 aws-live broker로 호출이 발생하지 않도록 `PORT_ENVIRONMENT=paper` / Secret prefix 분리 합의 (R-AUTO-002)
- [ ] 자동 재시도 정책 합의: BUY/SELL/fill sync/position 변경/intraday stop SELL 생성은 자동 재시도 금지 (OD-SAFE-004, R-AUTO-001)

## 8. Cost Validation

- [ ] AWS Billing Dashboard에서 본 spec 관련 일별 비용이 [`./decision-matrix.md`](./decision-matrix.md) 비용 프로파일 라인 안 (R-COST-001)
- [ ] NAT Gateway-Hours 라인 0 (R-COST-002)
- [ ] ALB-Hours 라인 0 또는 운영자 결정 라인과 일치
- [ ] Endpoint-Hours 라인이 활성 endpoint 수 × AZ 수 단가에 부합
- [ ] RDS-InstanceUsage 라인이 single-AZ 단가 기준
- [ ] Cost Anomaly Detection alert 등록 (운영자 결정)

## 9. Documentation Validation

- [ ] 본 spec 4개 파일과 본 runbook / validation-checklist / traceability-matrix 모두 작성 완료
- [ ] [`../operator-decisions.md`](../operator-decisions.md)의 본 spec 관련 결정 상태 일치
- [ ] [`../risk-register.md`](../risk-register.md)의 R-NET / R-SEC / R-DATA / R-COST 항목 mitigation 점검 완료
- [ ] [`./traceability-matrix.md`](./traceability-matrix.md) Requirement → Design → Task → Decision 매핑 완성
- [ ] 모든 secret 자리에 `[REDACTED]`만 사용. 실제 값 노출 없음 (R-DOCS-001)

## 10. Rollback Validation

본 섹션은 [`./runbook.md`](./runbook.md) Step 19 rollback이 수행된 경우의 점검표다.

- [ ] RDS instance `portfolio-paper-rds` 삭제됨 (또는 evidence 목적의 final snapshot 생성)
- [ ] RDS Subnet Group / Parameter Group 삭제됨
- [ ] VPC Endpoint 6개 삭제됨
- [ ] Security Group 8개 삭제됨 (다른 리소스 비참조 확인)
- [ ] Route Table 3종 삭제됨 (Subnet associations 해제 후)
- [ ] Internet Gateway detach + delete됨
- [ ] Subnet 6개 삭제됨
- [ ] VPC `portfolio-vpc` 삭제됨
- [ ] AWS Billing 일별 비용에서 본 spec 라인 종료
- [ ] 운영자 노트에 rollback 사유 / 일자 / 다음 시도 계획 기록

## 본 체크리스트 작업 안전 제약

- 본 점검은 운영자가 직접 확인. 콘솔 출력 캡처 시 secret / endpoint 호스트 prefix는 마스킹.
- 8개 MS 소스 / 문서 수정 없음.
- 실제 AWS 리소스 변경은 [`./runbook.md`](./runbook.md)으로만 진행. 본 체크리스트는 점검 전용.
- 결정값 변경 필요 시 본 문서가 아니라 [`../operator-decisions.md`](../operator-decisions.md)에 변경 제안 기록.
