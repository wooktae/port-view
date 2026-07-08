# Validation Checklist — 02-aws-network-and-rds

본 체크리스트는 운영자가 [`./runbook.md`](./runbook.md)의 각 Step을 진행한 뒤 통과 여부를 한 줄씩 확인하기 위한 점검표다. 모든 항목이 체크되어야 본 spec 1차(aws-paper) 진행이 마무리된다.

본 문서에는 실제 secret / password / token / app key / app secret / 계좌번호 / webhook URL 값을 적지 않는다. 모두 `[REDACTED]`만 사용한다.

## 상태 표기 규칙

기존 Markdown 체크박스(`- [ ]`, `- [x]`)는 사용하지 않는다. 본 체크리스트는 다음 4종 라벨을 사용한다.

- `<span style="color:red">[O]</span>` — 확인 완료 또는 성공.
- `<span style="color:blue">[X]</span>` — 실패 또는 기대값 불일치. 항목 끝에 `불일치: 실제값 = ..., 기대값 = ...`을 기록한다.
- `<span style="color:green">[Kiro 후속 작업 필요]</span>` — Kiro가 추가 파일 읽기 / grep / git status / AWS ReadOnly 조회 / 문서 간 비교로 확인할 수 있는 항목. 본 세션 미수행 또는 권한 부족 사유를 함께 적는다.
- `<span style="color:black">[운영자 확인 필요]</span>` — 운영자 판단, 비용 승인, 외부 노출 여부, 사람의 의사결정 / 인지가 필요한 항목.

자동 검증은 [`../../docs/kiro-readonly-validator-iam.md`](../../docs/kiro-readonly-validator-iam.md)의 ReadOnly IAM 권한을 사용해 AWS API 호출 결과로 갱신한다. Secret value는 절대 조회하지 않는다(metadata만).

## 1. Pre-flight Checklist

- <span style="color:red">[O]</span> AWS Region이 `ap-northeast-2`인지 확인 — 본 검증의 모든 AWS API 호출이 `ap-northeast-2` region에서 정상 응답
- <span style="color:red">[O]</span> aws-paper 대상 작업인지 확인 (OD-ENV-003) — `../_common/operator-decisions.md` At a Glance에 OD-ENV-003 = `aws-paper` 🟢 확정으로 등록되어 있고, 본 spec(02) requirements / design / runbook 모두 aws-paper를 1차 적용 환경으로 일관되게 명시(불일치 0건)
- <span style="color:red">[O]</span> 본 작업이 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog를 수정하지 않는지 확인 — 8개 워크스페이스 `git status --short` 결과 모두 무변경. port-view 내부도 `.kiro/` 외 변경 없음
- <span style="color:red">[O]</span> 실제 secret 값이 본 spec / 운영자 노트 / 콘솔 캡처에 노출되지 않았는지 확인 — repo 안 grep은 §9에서 다룸. 콘솔 캡처 / 외부 노트 / 개인 PC 파일 등 외부 영역은 운영자만 점검 가능 -> 확인 완료
- <span style="color:red">[O]</span> 비용 발생 리소스(VPC Endpoint, RDS, EIP) 생성 전 운영자 승인 여부 확인 (R-COST-001) -> 확인 완료
- <span style="color:red">[O]</span> [`../_common/operator-decisions.md`](../_common/operator-decisions.md) `At a Glance`의 핵심 결정 락 상태 확인 (OD-NET-001 / OD-NET-005 / OD-NET-009 / OD-RDS-001 / OD-CUT-001) — 사람의 인지 자체는 운영자 직접 확인 -> 확인 완료
- <span style="color:red">[O]</span> [`./decision-matrix.md`](./decision-matrix.md) 비용 프로파일 라인(low / realistic / stable) 결정 기록 존재 — operation-notes의 `aws-paper low 적용` 라인으로 확인
- <span style="color:red">[O]</span> [`../_common/risk-register.md`](../_common/risk-register.md)의 R-NET-001 ~ R-COST-002 항목 인지 — 사람 인지 항목 -> 확인 완료

## 2. Network Validation

- <span style="color:red">[O]</span> VPC `portfolio-vpc` 생성 확인. State = `Available`. CIDR = `10.0.0.0/16`
- <span style="color:red">[O]</span> VPC Tags에 `env=paper`, `project=portfolio` 적용
- <span style="color:red">[O]</span> Public subnet 2개(`public-a` 10.0.0.0/24, `public-b` 10.0.1.0/24) 생성
- <span style="color:red">[O]</span> Private app subnet 2개(`app-a` 10.0.10.0/24, `app-b` 10.0.11.0/24) 생성
- <span style="color:red">[O]</span> Private data subnet 2개(`data-a` 10.0.20.0/24, `data-b` 10.0.21.0/24) 생성
- <span style="color:red">[O]</span> 6개 subnet의 AZ 매핑(a / c)이 의도와 일치 — public-a/app-a/data-a = ap-northeast-2a, public-b/app-b/data-b = ap-northeast-2c
- <span style="color:red">[O]</span> Internet Gateway `portfolio-igw` 생성 + `portfolio-vpc`에 attach (State = `available`)
- <span style="color:red">[O]</span> NAT Gateway 미생성 확인 (NAT Gateways 메뉴 비어 있음, R-COST-002)
- <span style="color:red">[O]</span> NAT 역할 EC2 미생성 확인 (Name `nat-*` 인스턴스 없음, VPC 내 EC2 instance 0개)
- <span style="color:red">[O]</span> Route Table 3종 생성: `rt-public`, `rt-app`, `rt-data`
- <span style="color:red">[O]</span> `rt-public` Routes에 `0.0.0.0/0 → portfolio-igw` 존재
- <span style="color:red">[O]</span> `rt-app`, `rt-data` Routes는 VPC local + S3 Gateway endpoint(prefix list) 외 외부 라우트 없음 (0.0.0.0/0 라우트 없음, R-NET-002)
- <span style="color:red">[O]</span> Subnet associations: `rt-public` ↔ public-a/b, `rt-app` ↔ app-a/b, `rt-data` ↔ data-a/b

## 3. Security Group Validation

- <span style="color:red">[O]</span> SG 8개 생성(이름 prefix는 운영자 결정에 따라 `sgroup-`):
  - `sgroup-marketconnector-ec2`, `sgroup-port-view-ecs`
  - `sgroup-strategy-tasks`, `sgroup-crawler-tasks`, `sgroup-preprocessor-tasks`, `sgroup-research-batch`
  - `sgroup-rds-postgres`, `sgroup-vpc-endpoints`
- <span style="color:red">[O]</span> `sgroup-rds-postgres` inbound = app/EC2 SG 6개 참조(5432)만. **0.0.0.0/0 inbound 없음** (R-SEC-001)
- <span style="color:red">[O]</span> `sgroup-rds-postgres` outbound 비어 있음
- <span style="color:red">[O]</span> `sgroup-vpc-endpoints` inbound = app/EC2 SG 6개 참조(443)만
- <span style="color:red">[O]</span> `sgroup-marketconnector-ec2` inbound = `sgroup-port-view-ecs` (TCP 5000 또는 운영자 결정 포트)
- <span style="color:red">[O]</span> `sgroup-marketconnector-ec2` outbound 요건 충족.
  - 실제값: HTTPS(443) → 0.0.0.0/0(broker) + sgroup-vpc-endpoints(443) + sgroup-rds-postgres(5432)
  - design.md 표 요건(0.0.0.0/0 broker, sg-rds-postgres 5432, VPC Endpoints 443) 모두 일치(표기 순서만 다름)
- <span style="color:red">[O]</span> `sgroup-port-view-ecs` inbound 비어 있음 (또는 운영자 IP allowlist만)
- <span style="color:red">[O]</span> `sgroup-port-view-ecs` outbound = HTTPS 0.0.0.0/0 + sgroup-marketconnector-ec2(5000) + sgroup-rds-postgres(5432) + sgroup-vpc-endpoints(443)
- <span style="color:red">[O]</span> `sgroup-crawler-tasks` inbound 비어 있음 (R-NET-001)
- <span style="color:red">[O]</span> `sgroup-crawler-tasks` outbound = HTTPS 0.0.0.0/0 + sgroup-rds-postgres + sgroup-vpc-endpoints
- <span style="color:red">[O]</span> `sgroup-preprocessor-tasks` inbound 비어 있음
- <span style="color:red">[O]</span> `sgroup-preprocessor-tasks` outbound = HTTPS 0.0.0.0/0 + sgroup-rds-postgres + sgroup-vpc-endpoints
- <span style="color:red">[O]</span> `sgroup-strategy-tasks` outbound = sgroup-marketconnector-ec2(5000) + sgroup-rds-postgres(5432) + sgroup-vpc-endpoints(443) + HTTPS 0.0.0.0/0
- <span style="color:red">[O]</span> `sgroup-research-batch` outbound = sgroup-rds-postgres + sgroup-vpc-endpoints + HTTPS 0.0.0.0/0
- <span style="color:red">[O]</span> EC2 SSH 22 inbound 0.0.0.0/0 SG 어디에도 없음 (OD-NET-009) — VPC 전체 22번 포트 0/0 inbound 검색 결과 0건

## 4. VPC Endpoint Validation

- <span style="color:red">[O]</span> S3 Gateway endpoint(`vpce-s3-gw`) 생성. `rt-app`, `rt-data` 연결
- <span style="color:red">[O]</span> ECR API Interface endpoint(`vpce-ecr-api`) State = `available`. AZ에 app-a, app-b 포함
- <span style="color:red">[O]</span> ECR DKR Interface endpoint(`vpce-ecr-dkr`) State = `available`
- <span style="color:red">[O]</span> Secrets Manager Interface endpoint(`vpce-secretsmanager`) State = `available`
- <span style="color:red">[O]</span> SSM Interface endpoint(`vpce-ssm`) State = `available`
- <span style="color:red">[O]</span> CloudWatch Logs Interface endpoint(`vpce-logs`) State = `available`
- <span style="color:red">[O]</span> 모든 Interface endpoint Private DNS = `enabled`
- <span style="color:red">[O]</span> 모든 Interface endpoint SG = `sgroup-vpc-endpoints`
- <span style="color:black">[운영자 확인 필요]</span> (옵션) STS / KMS endpoint는 OD-NET-006 결정에 따라 활성 / 미활성 — 현재 미생성. 활성 여부는 운영자 결정

## 5. RDS Validation

- <span style="color:red">[O]</span> RDS Subnet Group `portfolio-paper-subnet-group` Status = `Complete`. data-a, data-b 포함
- <span style="color:red">[O]</span> RDS Parameter Group `pg-portfolio-paper` 생성. family = `postgres16`
- Parameter group 적용 값:
  - <span style="color:red">[O]</span> `rds.force_ssl = 1` (system source. effective value = 1)
  - <span style="color:red">[O]</span> `log_min_duration_statement = 1000` (user source)
  - <span style="color:red">[O]</span> `log_lock_waits = 1` (user source)
  - <span style="color:red">[O]</span> `idle_in_transaction_session_timeout = 60000` (user source)
- <span style="color:red">[O]</span> (선행) Secrets Manager `/portfolio/paper/rds/master` 등록. 값은 `[REDACTED]` — DescribeSecret로 metadata만 확인. value 조회 없음
- <span style="color:red">[O]</span> RDS instance `portfolio-paper-rds` Status = `available`
- <span style="color:red">[O]</span> RDS engine = PostgreSQL 16.x (실제값 = 16.14)
- <span style="color:red">[O]</span> RDS instance class = `db.t4g.small` (OD-RDS-001)
- <span style="color:red">[O]</span> RDS deployment = Single-AZ (MultiAZ = false)
- <span style="color:red">[O]</span> RDS storage = gp3 50 GB (storage autoscaling max = 100 GB)
- <span style="color:red">[O]</span> RDS encryption at rest = Enabled (KMS default)
- <span style="color:red">[O]</span> RDS Performance Insights = Enabled
- <span style="color:red">[O]</span> RDS VPC = `portfolio-vpc`, subnet group = `portfolio-paper-subnet-group`, SG = `sgroup-rds-postgres`
- <span style="color:red">[O]</span> RDS Publicly accessible = `No` (R-SEC-001)
- <span style="color:red">[O]</span> RDS Backup retention = 7 days (OD-RDS-006)
- <span style="color:red">[O]</span> RDS PITR = Enabled (OD-RDS-008) — BackupRetentionPeriod=7 + automated backups enabled로 PITR 활성. 운영자가 Console에서 추가 시각 확인 권고
- <span style="color:red">[O]</span> RDS Deletion protection = Enabled
- <span style="color:red">[O]</span> RDS DB parameter group = `pg-portfolio-paper` (Status `in-sync`)
- <span style="color:red">[O]</span> RDS Endpoint URL이 운영자 노트에 기록(`INTEREST_DB_HOST`) — operation-notes의 DB Endpoint 라인 참조. 본 체크리스트에는 hostname 미기록(public 노출 금지)

## 6. DB / schema / role 준비 Validation

본 spec [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4 SQL을 운영자가 실행했고, §5 검증 SQL로 결과를 확인했다. 상세 실행 기록은 [`./operation-notes.md`](./operation-notes.md) `## 2026-06-09 DB Role / 권한 분리 1차 적용` 섹션 참조.

- <span style="color:red">[O]</span> `portfolio` 데이터베이스 존재 확인 — restore 완료 시점에 존재. 본 세션 SQL 적용 대상
- <span style="color:red">[O]</span> 10개 schema 존재 확인: `reference, interest, preprocessor, research, decision, execution, connector, ops, legacy, public` — §4.1 사전 점검에서 확인
- <span style="color:red">[O]</span> 7개 app role 생성 완료 — §5.1 결과 7행 모두 존재.
  - `marketconnector_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `execution_app`, `research_app`, `view_app`
  - 모두 LOGIN = true, SUPERUSER / CREATEDB / CREATEROLE / REPLICATION / BYPASSRLS = false 확인
- <span style="color:red">[O]</span> role별 schema 권한 매트릭스가 [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §2 표와 일치 — §5.2 schema USAGE / CREATE, §5.3 table / sequence 권한 요약 모두 불일치 0건. legacy USAGE는 7개 app role 모두 false 확인
- <span style="color:red">[O]</span> role별 search_path = 8개 MS README 정의대로 적용 (R-DATA-001) — §5.1 search_path 결과가 [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §3 표와 일치
- <span style="color:black">[운영자 확인 필요]</span> role 비밀번호는 Secrets Manager / SSM SecureString placeholder만 기록 (`[REDACTED]`) — 정식 보관은 `06-secrets-and-iam` 진행 시점에 등록 후 본 항목을 [O]로 갱신

## 7. Cutover 사전 준비 Validation

- <span style="color:black">[운영자 확인 필요]</span> pg_dump 명령 형식 운영자 노트에 합의 (`--format=custom --no-owner --no-privileges`)
- <span style="color:black">[운영자 확인 필요]</span> pg_restore 명령 형식 운영자 노트에 합의 (`--no-owner --no-privileges --jobs=N`)
- <span style="color:black">[운영자 확인 필요]</span> dump 파일 보관 위치 / 삭제 정책 합의
- <span style="color:black">[운영자 확인 필요]</span> [`./design.md`](./design.md) "검증 SQL 후보" 섹션을 cutover 검증에 사용한다는 합의
- <span style="color:black">[운영자 확인 필요]</span> [`./design.md`](./design.md) "Rollback 기준" 합의 (R-DATA-002 mitigation 일치)
- <span style="color:black">[운영자 확인 필요]</span> aws-paper에서 aws-live broker로 호출이 발생하지 않도록 `PORT_ENVIRONMENT=paper` / Secret prefix 분리 합의 (R-AUTO-002)
- <span style="color:black">[운영자 확인 필요]</span> 자동 재시도 정책 합의: BUY/SELL/fill sync/position 변경/intraday stop SELL 생성은 자동 재시도 금지 (OD-SAFE-004, R-AUTO-001)

## 8. Cost Validation

- <span style="color:black">[운영자 확인 필요]</span> AWS Billing Dashboard에서 본 spec 관련 일별 비용이 [`./decision-matrix.md`](./decision-matrix.md) 비용 프로파일 라인 안 (R-COST-001) — 청구 금액 자체는 Billing Dashboard 직접 확인
- <span style="color:red">[O]</span> NAT Gateway-Hours 라인 0 (R-COST-002) — NAT Gateway 0개 자동 검증
- <span style="color:red">[O]</span> ALB-Hours 라인 0 또는 운영자 결정 라인과 일치 — `elbv2 describe-load-balancers` 및 classic `elb describe-load-balancers` 결과 portfolio-vpc 안 LB 0건. 본 spec 결정상 ALB 미사용과 일치
- <span style="color:green">[Kiro 후속 작업 필요]</span> Endpoint-Hours 라인이 활성 endpoint 수 × AZ 수 단가에 부합.
  - Interface endpoint 5종 × 2 AZ + S3 Gateway 자동 검증으로 endpoint 인벤토리는 확인.
  - Cost Explorer ReadOnly 호출 결과 본 시점 USAGE_TYPE 그룹 0건(데이터 누적 lag 추정).
  - 며칠 후 Cost Explorer 데이터 누적 후 재시도하여 [O] / [X]로 갱신.
- <span style="color:green">[Kiro 후속 작업 필요]</span> RDS-InstanceUsage 라인이 single-AZ 단가 기준.
  - RDS instance class / Single-AZ는 자동 검증 완료.
  - Cost Explorer ReadOnly 호출 결과 본 시점 USAGE_TYPE 그룹 0건(데이터 누적 lag 추정).
  - 며칠 후 Cost Explorer 데이터 누적 후 재시도하여 [O] / [X]로 갱신.
- <span style="color:black">[운영자 확인 필요]</span> Cost Anomaly Detection alert 등록 (운영자 결정) — `ce get-anomaly-monitors` / `ce get-anomaly-subscriptions` 결과 0건. 등록할지 여부는 운영자 결정

## 9. Documentation Validation

- <span style="color:red">[O]</span> 본 spec 4개 파일과 본 runbook / validation-checklist / traceability-matrix 모두 작성 완료 — 파일 시스템 직접 확인
- <span style="color:red">[O]</span> [`../_common/operator-decisions.md`](../_common/operator-decisions.md)의 본 spec 관련 결정 상태 일치 — Kiro가 두 문서를 비교한 결과 아래 결정 모두 본 spec design / runbook과 일관(불일치 0건).
  - OD-ENV-003 aws-paper / OD-ENV-005 단일 VPC
  - OD-NET-001 paper NAT 미사용 / OD-NET-005 권고 endpoint 5종 / OD-NET-009 SSM only
  - OD-RDS-001 db.t4g.small single-AZ / OD-RDS-004 PG16 / OD-RDS-006 backup 7일 / OD-RDS-008 PITR on
  - OD-DB-001~006 portfolio + schema-per-domain + role 7개
  - OD-CUT-001 pg_dump+pg_restore
  - OD-SAFE-001~004
- <span style="color:red">[O]</span> [`../_common/risk-register.md`](../_common/risk-register.md)의 R-NET / R-SEC / R-DATA / R-COST 항목 mitigation 점검 완료 — Kiro가 risk row별 mitigation 본문과 본 spec 통제를 비교한 결과 일관.
  - R-NET-001 = sgroup-* inbound 비어 있음
  - R-NET-002 = OPT-1 매핑
  - R-NET-003 = endpoint 5종 available
  - R-SEC-001 = sgroup-rds-postgres SG 참조 inbound + RDS public access No
  - R-DATA-001 = role search_path 정책 / R-DATA-002 = pg_dump options 합의 항목
  - R-AUTO-001~002 = OD-SAFE-* state machine 정책
  - R-DOCS-001 = `[REDACTED]` grep 0건
  - R-COST-001~002 = NAT / ALB 0건 + Endpoint 권고 세트
  - R-SEC-002 / R-SEC-003은 02 runbook Step 0 portadmin 흐름과 일관
- <span style="color:red">[O]</span> [`./traceability-matrix.md`](./traceability-matrix.md) Requirement → Design → Task → Decision 매핑 완성 — 직전 세션에서 매핑 완성 + Step 재번호에 맞춰 동기화됨
- <span style="color:red">[O]</span> 모든 secret 자리에 `[REDACTED]`만 사용. 실제 값 노출 없음 (R-DOCS-001) — `.kiro/**/*.md` 트리에 대한 grep 결과 아래 패턴 모두 0건. 콘솔 캡처 / 외부 노트 / 개인 PC 파일 / repo 외부 영역은 [운영자 확인 필요](§1)에서 분리 관리.
  - `(AKIA|ASIA)` access key id
  - Slack incoming webhook URL / JWT(`eyJ...`)
  - `(PASSWORD|SECRET|TOKEN|APP_KEY|APP_SECRET)=값`
  - `aws_secret_access_key=값`
  - `KIS*KEY|SECRET|TOKEN=값`
  - 계좌번호 평문 패턴

## 10. Rollback Validation

본 섹션은 rollback을 실제 수행한 경우에만 사용하는 조건부 체크리스트다. 현재는 AWS Foundation 생성이 성공했고 rollback을 수행하지 않았으므로 rollback 미수행 상태가 정상이다. 향후 운영자가 [`./runbook.md`](./runbook.md) Step 19 rollback을 실행하면 각 항목을 AWS API로 재검증하여 [O] 또는 [X]로 갱신한다.

- <span style="color:red">[O]</span> Rollback 미수행 — Foundation 생성 성공 상태이므로 RDS instance `portfolio-paper-rds`는 현재 삭제 / final snapshot 대상 아님
- <span style="color:red">[O]</span> Rollback 미수행 — RDS Subnet Group / Parameter Group은 현재 삭제 대상 아님
- <span style="color:red">[O]</span> Rollback 미수행 — VPC Endpoint 6개는 현재 삭제 대상 아님
- <span style="color:red">[O]</span> Rollback 미수행 — Security Group 8개는 현재 삭제 대상 아님
- <span style="color:red">[O]</span> Rollback 미수행 — Route Table 3종은 현재 삭제 / association 해제 대상 아님
- <span style="color:red">[O]</span> Rollback 미수행 — Internet Gateway는 현재 detach + delete 대상 아님
- <span style="color:red">[O]</span> Rollback 미수행 — Subnet 6개는 현재 삭제 대상 아님
- <span style="color:red">[O]</span> Rollback 미수행 — VPC `portfolio-vpc`는 현재 삭제 대상 아님
- <span style="color:red">[O]</span> Rollback 미수행 — AWS Billing 일별 비용은 현재 정상 운영 라인이 유지되는 것이 정상
- <span style="color:red">[O]</span> Rollback 미수행 — 본 시점에는 rollback 사유 / 일자 / 다음 시도 계획 기록 불필요. 향후 rollback 실행 시 운영자 노트에 기록

## 11. DB Role 권한 매트릭스 Validation

본 섹션은 [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4 SQL 적용 후 §5 검증 SQL과 §5.4 connection 검증으로 확인한 결과를 라벨로 정리한다. 상세 실행 기록은 [`./operation-notes.md`](./operation-notes.md) `## 2026-06-09 DB Role / 권한 분리 1차 적용` 섹션 참조.

- <span style="color:red">[O]</span> `portfolio_owner` 생성 완료(NOLOGIN). `portfolio_admin`에 `portfolio_owner` 멤버십 부여 완료 — §5.1 query 결과 row 존재 확인
- <span style="color:red">[O]</span> 9개 도메인 schema(`reference, interest, preprocessor, research, decision, execution, connector, ops, legacy`) owner = `portfolio_owner` 이관 완료 — `public`은 변경하지 않음
- <span style="color:black">[운영자 확인 필요]</span> 기존 table / sequence / index의 owner는 `portfolio_admin`으로 남아 있음.
  - `REASSIGN OWNED BY portfolio_admin TO portfolio_owner`는 본 세션 미실행.
  - 후속 운영자 결정으로 일괄 이관 여부 분리 관리.
  - 현 시점에는 §4.4 명시 GRANT로 권한 매트릭스 적용 완료.
  - §4.5 default privileges는 이후 새 객체에만 자동 적용.
- <span style="color:red">[O]</span> 7개 app role 생성 + DB CONNECT + public USAGE 부여 완료 (§4.3 SQL 적용 결과)
- <span style="color:red">[O]</span> §5.1 7개 app role 모두 LOGIN = true, SUPERUSER / CREATEDB / CREATEROLE / REPLICATION / BYPASSRLS = false
- <span style="color:red">[O]</span> §5.1 7개 app role의 `search_path`가 [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §3 표와 일치
- <span style="color:red">[O]</span> §5.2 schema USAGE / CREATE 매트릭스 [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §2 표와 불일치 0건. 모든 app role의 `legacy` USAGE = false
- <span style="color:red">[O]</span> §5.3 table 권한 요약 / sequence 권한 요약 [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §2 표와 불일치 0건
- <span style="color:red">[O]</span> §5.4 `marketconnector_app` 접속 검증 — 접속 성공. `connector` 조회 성공, `execution` 조회 성공, `legacy` USAGE = false. `execution` INSERT/UPDATE/DELETE → permission denied 정상(read only 의도와 일치)
- <span style="color:red">[O]</span> §5.4 `execution_app` 접속 검증 — 접속 성공. `execution` / `decision` / `connector` 조회 성공, `legacy` USAGE = false. `execution` INSERT/UPDATE/DELETE 성공(R/W 의도와 일치)
- <span style="color:red">[O]</span> §5.4 `view_app` 접속 검증 — 접속 성공. `execution` / `connector` / `decision` 조회 성공, `legacy` USAGE = false. `execution` INSERT/UPDATE/DELETE → permission denied 정상(view write 범위는 현재 `ops` 한정)
- <span style="color:black">[운영자 확인 필요]</span> 7개 app role 비밀번호의 Secrets Manager / SSM SecureString 정식 등록 — `06-secrets-and-iam` 진행 시점에 등록 후 [O]로 갱신
- <span style="color:red">[O]</span> 매트릭스 변경(legacy 미부여, `marketconnector_app` execution R only 축소)을 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-DB 카테고리에 갱신 완료 — OD-DB-007 / OD-DB-008 / OD-DB-009 / OD-DB-010 추가

## 12. RDS Restore Validation (Local → aws-paper)

본 섹션은 2026-06-09 운영자가 직접 수행한 Local PostgreSQL → aws-paper RDS migration 결과를 라벨로 정리한다. 상세 실행 기록은 [`./operation-notes.md`](./operation-notes.md) `## 2026-06-09 Local → RDS Migration & RDS 재생성 실행 기록` 섹션 참조.

- <span style="color:red">[O]</span> 로컬 dump 파일 무결성 — `portfolio_full_20260609.dump`(format custom + gzip), 로컬 / S3 임시 bucket / MarketConnector EC2 3 곳 모두 422,334,494 bytes 일치
- <span style="color:red">[O]</span> dump 메타데이터 — `pg_restore --list` TOC Entries 812, line count 823, dump source PostgreSQL 18.1
- <span style="color:red">[O]</span> RDS engine version — major version mismatch 회피 결정에 따라 RDS 재생성. 신규 RDS engine = PostgreSQL 18.4, initial DB = `portfolio`, Public access = No 유지
- <span style="color:red">[O]</span> Restore Runner 경로 — Local Windows PC → S3 임시 bucket → MarketConnector EC2(Amazon Linux 2023, public subnet, EIP attach) → private RDS. 로컬 PC IP는 RDS SG에 직접 허용하지 않음
- <span style="color:red">[O]</span> 1차 `role "postgres" does not exist` 오류 처리 — DB drop / recreate 후 `pg_restore --no-owner --no-privileges`로 재실행. 에러 없이 완료. RDS 객체 owner는 restore 실행 계정(`portfolio_admin`) 기준
- <span style="color:red">[O]</span> 정합성 — schema별 table count 81개 일치, table / index / sequence / FK 33개 / trigger 23개 / table별 row count clean CSV 82줄 모두 로컬 기준선과 diff 0(CRLF / LF는 `--strip-trailing-cr` 정규화 후 비교)
- <span style="color:red">[O]</span> 민감정보 미기록 — 본 검증 결과에 RDS endpoint hostname / password / secret value / account-id / 계좌번호 / token 신규 기록 없음. `[REDACTED]` 또는 placeholder만 사용

## 13. DB Role 권한 / search_path 보강 Validation (2026-06-17 17-step E2E)

본 섹션은 2026-06-17 Daily AWS 17-step E2E 흐름 중 운영자가 발견 / 보정한 DB role 권한 / search_path 사실을 라벨로 정리한다.

- 상세 실행 기록: [`./operation-notes.md`](./operation-notes.md) `## 2026-06-17 Daily AWS 17-step E2E 흐름 중 발견된 DB Role / 권한 / search_path 보정` §1 ~ §4.
- 리스크 참조: [`../_common/risk-register.md`](../_common/risk-register.md) R-DATA-005 [2026-06-17 보강] / R-DATA-011 신규.
- 본 spec 본문 결정값(§4 GRANT / §5 검증 SQL) 변경은 후속 phase / 본 일자에는 사실 기록만.

- <span style="color:red">[O]</span> `execution_app` 의 `interest` schema USAGE 권한 보정(Step 8 1차 실패 → 운영자 직접 GRANT 후 통과 / R-DATA-005 [2026-06-17 보강] 정합)
- <span style="color:red">[O]</span> `execution_app` 의 `interest.*` table SELECT 권한 보정(execution buy 흐름 정합)
- <span style="color:red">[O]</span> `execution_app` 의 interest sequence 권한 / future default privileges 보정(`ALTER DEFAULT PRIVILEGES IN SCHEMA interest GRANT SELECT ON TABLES TO execution_app` 등)
- <span style="color:red">[O]</span> `marketconnector_app` 의 `legacy` schema USAGE 권한 보정(Step 17 1차 실패 → 운영자 직접 GRANT 후 통과 / OD-DB-007 의 marketconnector_app 한정 1건 예외 / R-DATA-011 신규 정합)
- <span style="color:red">[O]</span> `marketconnector_app` 의 `legacy.holdings` DML(SELECT / INSERT / UPDATE / DELETE) 권한 보정(legacy 운영 데이터 갱신 최소 권한)
- <span style="color:red">[O]</span> `marketconnector_app` 의 legacy sequence 권한 / future default privileges 보정
- <span style="color:red">[O]</span> `marketconnector_app` 의 database search_path 보정 — `connector, execution, legacy, reference, public`(`ALTER ROLE marketconnector_app IN DATABASE portfolio SET search_path = ...`)
- <span style="color:red">[O]</span> bare table name 의존 legacy 경로(`holdings`) 가 search_path 안에서 탐색되는지 검증 SQL 후보 추가(`SET ROLE marketconnector_app; SELECT 1 FROM holdings LIMIT 1;`)
- <span style="color:red">[O]</span> 본 일자 password / RDS endpoint hostname / account-id / 실제 ARN / 계좌번호 평문 기록 0건. R-DOCS-001 [2026-06-17 보강(17-step E2E)] 정합

## 본 체크리스트 작업 안전 제약

- 본 점검은 운영자가 직접 확인하거나 ReadOnly IAM([`../../docs/kiro-readonly-validator-iam.md`](../../docs/kiro-readonly-validator-iam.md))으로 자동 검증한다. 콘솔 출력 캡처 시 secret / endpoint 호스트 prefix는 마스킹.
- 8개 MS 소스 / 문서 수정 없음.
- 실제 AWS 리소스 변경은 [`./runbook.md`](./runbook.md)으로만 진행. 본 체크리스트는 점검 전용.
- 결정값 변경 필요 시 본 문서가 아니라 [`../_common/operator-decisions.md`](../_common/operator-decisions.md)에 변경 제안 기록.
- secret value는 절대 조회하지 않는다(GetSecretValue 금지). DescribeSecret metadata만 사용한다.
- account-id, RDS endpoint hostname, secret ARN, access key id는 본 문서에 기록하지 않는다. 이미 운영자 노트에 적힌 내부 식별자는 외부 공개 전 운영자가 직접 마스킹한다.
