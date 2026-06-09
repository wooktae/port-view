## 2026-06-08 AWS Foundation 실행 기록

- Step 2 비용 / 환경 결정 확인 완료: aws-paper low 적용. secret 값 기록 없음.
- CIDR: 10.0.0.0/16 (권고)
- AZ 2개: ap-northeast-2a, ap-northeast-2c (권고)
- Subnet 6개: public-a, public-b, app-a, app-b, data-a, data-b
- Internet Gateway: portfolio-igw
- NAT Gateways 목록 비어 있음
- NAT 역할의 EC2 인스턴스 없음
- Route 테이블: rt-public, rt-app, rt-data
- 보안 그룹 생성
 1) sgroup-marketconnector-ec2 (sg로 시작 불가)
 2) sgroup-port-view-ecs
 3) sgroup-strategy-tasks
 4) sgroup-crawler-tasks 
 5) sgroup-preprocessor-tasks
 6) sgroup-research-batch
 7) sgroup-rds-postgres
 8) sgroup-vpc-endpoints
 - Security Group 규칙 채우기: 완료
 - VPC Endpoint 생성: 완료
 - RDS Subnet Group 생성: 완료
 - RDS Parameter Group 생성: 완료
 - Secret 이름: /portfolio/paper/rds/master
 - Step 14-1 RDS 생성 진행: PostgreSQL 16.14-R1, portfolio-paper-rds, portfolio_admin, 자체 관리 암호 방식 선택. 실제 password 값 기록 없음.
 - KMS Key ID: alias/aws/rds
 - RDS 생성: 완료
 - INTEREST_DB_HOST:
 - DB Endpoint: portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com
 - DB 프로그래밍 언어: PSQL (Windows)로 변경
 - Step 15 RDS 접속 보안 / Parameter / Option 확인 완료: Publicly accessible = No, pg-portfolio-paper 적용, backup retention 7일, deletion protection enabled 확인. secret 값 기록 없음.


## 2026-06-08 Kiro ReadOnly 검증 IAM 설계 및 자동 검증 결과

- Kiro ReadOnly 검증 IAM 설계 완료: 사용자 이름 `portfolio-kiro-readonly-validator`, 정책 이름 `PortfolioKiroReadOnlyValidatorPolicy`. 상세 설계는 [`../../docs/kiro-readonly-validator-iam.md`](../../docs/kiro-readonly-validator-iam.md). 정책에는 ec2/rds/secretsmanager(metadata 한정)/iam/cloudwatch/logs/tag ReadOnly만 포함하고, `secretsmanager:GetSecretValue`와 KMS Decrypt는 명시 Deny.
- 자동 검증 실행: AWS CLI ReadOnly 호출만 사용. 리소스 생성/수정/삭제 없음. SecretsManager는 DescribeSecret만 호출하고 GetSecretValue는 호출하지 않음.
- 자동 검증 가능 항목: VPC, 6개 Subnet, IGW attach, NAT 미생성, NAT 역할 EC2 미생성, Route Table 3종(rt-public 0/0→IGW, rt-app/rt-data 외부 라우트 없음), SG 8개 인벤토리, sg-rds-postgres inbound SG 참조만(0/0:5432 없음), SSH 22 0/0 inbound 0건, sg-vpc-endpoints inbound, 운영 SG outbound 표 일치 점검, VPC Endpoint 6종 available + Private DNS + sgroup-vpc-endpoints, RDS subnet group / parameter group / instance(class/storage/public access/backup retention/deletion protection/encryption/parameter group in-sync) — 모두 기대값 일치.
- 자동 검증 완료 항목 수: 약 38건 (Network 13 + SG 16 + VPC Endpoint 8 + RDS 11에서 자동 가능한 부분 포함, 일부 sub-항목 합산 추정).
- 수동 확인 필요 항목 수: 약 31건 (Pre-flight 운영자 인지 / 비용 프로파일 결정 인지 / RDS PITR Console 확인 / Cost Validation Billing Dashboard / DB SQL 미실행 / Cutover 합의 / Rollback 미수행 / Documentation 사람 점검 등).
- 불일치 항목 수: 0건. 기존 `sgroup-marketconnector-ec2` outbound는 AWS CLI 출력 구조상 list 표기 차이로 [X] 후보였으나, 실제 규칙은 HTTPS 0.0.0.0/0 + sgroup-vpc-endpoints(443) + sgroup-rds-postgres(5432)를 모두 충족하므로 운영 불일치 없음.
- secret value 조회 없음 (DescribeSecret metadata만 호출).
- AWS 리소스 생성 / 수정 / 삭제 없음 (read-only API만 호출).
- 본 검증 산출물 갱신 대상: `.kiro/docs/kiro-readonly-validator-iam.md`, `.kiro/specs/02-aws-network-and-rds/validation-checklist.md`, `.kiro/specs/02-aws-network-and-rds/operation-notes.md` 3개 파일. 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음.
- 후속 작업 권고
  - 운영자가 portadmin으로 `portfolio-kiro-readonly-validator` IAM User 생성 + 정책 attach + access key 발급 후 Kiro 환경의 별도 profile에 등록.
  - access key는 본 문서 / repo / 평문 파일에 적지 않는다(`[REDACTED]`).
  - 03 / 06 spec 진행 시점에 DB / schema / role SQL 실행 후 Section 6 수동 확인 항목을 [O]로 갱신.
  - Cost Anomaly Detection alert 등록 후 Section 8 항목을 [O]로 갱신.


## 2026-06-08 validation-checklist 상태 라벨 4종 재분류

- validation-checklist.md 상태 라벨을 3종에서 4종으로 재분류함. 기존 `<span style="color:black">[확인 필요]</span>`를 모두 제거하고 `<span style="color:green">[Kiro 후속 작업 필요]</span>` 또는 `<span style="color:black">[운영자 확인 필요]</span>`로 분리함.
- Rollback Validation은 현재 rollback 미수행이 정상 상태이므로 모두 `[O] Rollback 미수행 — 현재 대상 아님` 형태로 정리함. 향후 실제 rollback 시점에 AWS API 재검증으로 [O]/[X]를 갱신할 예정.
- Cost / Documentation 항목 중 Kiro가 추가 ReadOnly 호출 / 파일 grep / 문서 비교로 확인 가능한 것은 `[Kiro 후속 작업 필요]`로 분류함. 본 세션에서 ALB 0건(elbv2 / classic elb), Cost Anomaly Detection alert 0건, repo grep(AKIA / Slack webhook / JWT / 평문 secret 0건)은 추가로 자동 검증해 [O] 또는 [운영자 확인 필요]로 즉시 확정함.
- 실제 운영자 판단 / 비용 승인 / 외부 노출 / 사람 인지 / 미래 합의는 `[운영자 확인 필요]`로 분리함(operator-decisions / risk-register 인지, 비용 라인 결정, cutover 합의, public 문서 마스킹, Cost Anomaly Detection 등록 결정, DB SQL 합의 등).
- 기존 [X] 항목 정정: `sgroup-marketconnector-ec2` outbound는 design.md 표(0.0.0.0/0 broker, sg-rds-postgres 5432, VPC Endpoints 443) 요건을 모두 충족하며 표기 순서만 다름. 따라서 [X] 불일치가 아니라 [O]로 재분류함.
- 변경 요약 (라벨 개수, 라벨 규칙 설명 줄 제외): [O] 74건 / [X] 0건 / [Kiro 후속 작업 필요] 6건 / [운영자 확인 필요] 20건. (Rollback Validation 10건 [O] 포함.)
- secret value 조회 없음. SecretsManager는 metadata만 사용.
- AWS 리소스 생성 / 수정 / 삭제 없음 (read-only API만 호출).
- 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음 (`git status --short` 결과 .kiro 외 변경 없음).
- 본 작업 산출물 갱신 대상: `.kiro/specs/02-aws-network-and-rds/validation-checklist.md`, `.kiro/specs/02-aws-network-and-rds/operation-notes.md`. account-id / RDS endpoint hostname / secret ARN / access key id는 본 작업으로 새로 추가 출력하지 않음. 이미 운영자가 본 파일 상단에 적은 내부 식별자는 외부 공개 전 마스킹 권고.


## 2026-06-08 [Kiro 후속 작업 필요] 항목 추가 검증 및 [O] 격상

- 대상: 직전 세션에서 [Kiro 후속 작업 필요]로 남겨진 6개 항목.
- 결과: 4개 항목 [O]로 격상, 2개 항목 [Kiro 후속 작업 필요] 유지(Cost Explorer 데이터 누적 후 재시도 사유 명시).
- 격상한 항목
  - §1 aws-paper 대상 작업인지 확인 — operator-decisions.md At a Glance OD-ENV-003 = `aws-paper` 🟢 확정과 본 spec 일관 비교로 [O].
  - §9 operator-decisions.md 본 spec 결정 상태 일치 — OD-ENV / OD-NET / OD-RDS / OD-DB / OD-CUT / OD-SAFE 카테고리 모든 핵심 결정이 본 spec design / runbook / decision-matrix와 일관(불일치 0건)으로 [O].
  - §9 risk-register.md mitigation 점검 — R-NET-001~003 / R-SEC-001 / R-DATA-001~002 / R-AUTO-001~002 / R-DOCS-001 / R-COST-001~002 / R-SEC-002~003 mitigation 본문이 본 spec의 design / runbook / validation-checklist 통제와 일관으로 [O].
  - §9 secret 자리 [REDACTED] 일관성 — `.kiro/**/*.md` 트리에 대해 (AKIA|ASIA) access key id / Slack incoming webhook URL / JWT / `(PASSWORD|SECRET|TOKEN|APP_KEY|APP_SECRET)=값` / `aws_secret_access_key=값` / `KIS*KEY|SECRET|TOKEN=값` / 계좌번호 평문 패턴 grep 모두 0건. [REDACTED-CANDIDATE] 출력 없음. [O]로 격상. repo 외부(콘솔 캡처 / 개인 노트 / 외부 PC 파일)는 §1 [운영자 확인 필요]로 분리 유지.
- 유지한 항목 (Kiro 후속 작업 필요)
  - §8 Endpoint-Hours 청구 매칭 — Cost Explorer ReadOnly(`ce get-cost-and-usage`)로 USAGE_TYPE 그룹 조회 결과 본 시점 0건(데이터 누적 lag 추정). 며칠 후 재시도하여 [O] / [X]로 갱신 예정.
  - §8 RDS-InstanceUsage 청구 매칭 — 동일 사유. RDS instance class / Single-AZ는 자동 검증으로 [O] 상태이나 청구 라인 매칭은 데이터 누적 후 재시도.
- 변경 요약 (라벨 개수, 라벨 규칙 설명 줄 제외): [O] 78건 / [X] 0건 / [Kiro 후속 작업 필요] 2건 / [운영자 확인 필요] 20건.
- 새로 발견한 리스크 또는 운영자 확인 필요 항목 요약
  - 신규 리스크 식별 0건. risk-register.md에 추가 row 필요 없음.
  - operation-notes.md 상단의 RDS endpoint hostname 한 줄과 secret 이름 `/portfolio/paper/rds/master`는 secret value는 아니지만 외부 공개 시 식별자 노출 가능성이 있음. §1 [운영자 확인 필요] 항목 "실제 secret 값이 본 spec / 운영자 노트 / 콘솔 캡처에 노출되지 않았는지 확인"의 보강 사항으로 운영자가 외부 공개 전 마스킹 필요(R-DOCS-001 mitigation 범위).
- secret value 조회 없음 (DescribeSecret metadata만 사용. GetSecretValue 0회).
- AWS 리소스 생성 / 수정 / 삭제 없음 (read-only describe / list / get / cost explorer get-cost-and-usage만 호출).
- 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음.
- 본 작업 산출물 갱신 대상: `.kiro/specs/02-aws-network-and-rds/validation-checklist.md`, `.kiro/specs/02-aws-network-and-rds/operation-notes.md`. account-id / RDS endpoint hostname / secret ARN / access key id는 본 작업으로 새로 추가 출력하지 않음.

- 운영 결정: `portfolio-kiro-readonly-validator` IAM User 생성은 우선 보류하고, 기존 AWS CLI 기본 자격증명인 `terraform` IAM User로 Kiro 검증을 진행한다. 단, `terraform`은 AdministratorAccess 권한이므로 Kiro 작업 시 AWS 리소스 생성/수정/삭제 명령은 금지하고 ReadOnly 조회만 허용한다. 향후 보안 정리 단계에서 ReadOnly 전용 IAM 분리를 재검토한다.


## 2026-06-09 DB Role / 권한 분리 1차 적용

본 spec [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4 SQL을 운영자가 직접 실행하고, §5 검증 SQL로 결과를 확인했다. SQL 본문 / 실제 password / endpoint / secret value는 본 문서에 기록하지 않는다.

### owner / membership

- `portfolio_owner` 생성 완료(NOLOGIN).
- `portfolio_admin`에 `portfolio_owner` 멤버십 부여 완료. 이후 `ALTER SCHEMA ... OWNER TO portfolio_owner` 실행 가능 상태로 진입.
- 9개 도메인 schema(`reference`, `interest`, `preprocessor`, `research`, `decision`, `execution`, `connector`, `ops`, `legacy`)의 owner를 `portfolio_owner`로 이관 완료.
- `public` schema는 변경하지 않음(RDS 정책 / 호환성 유지).
- 기존 table / sequence / index의 owner는 `portfolio_admin`(restore 실행 계정)으로 그대로 남아 있음. 본 세션에서는 `REASSIGN OWNED BY portfolio_admin TO portfolio_owner`를 실행하지 않음. 기존 객체에 대해서는 §4.5 default privileges가 자동 적용되지 않으므로, 본 세션에서 §4.4 명시 GRANT만으로 권한 매트릭스를 적용한 상태다(이후 새로 만드는 객체는 default privileges 적용 대상). 후속 세션에서 owner 일괄 이관 여부는 운영자 결정으로 분리 관리한다.

### app role 7종

- `marketconnector_app`, `view_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `research_app`, `execution_app` 생성 완료.
- 7종 role 모두 `LOGIN = true`, `SUPERUSER / CREATEDB / CREATEROLE / REPLICATION / BYPASSRLS = false` 확인.
- 7종 role 모두 `GRANT CONNECT ON DATABASE portfolio` 부여 + `GRANT USAGE ON SCHEMA public` 부여 완료.

### GRANT 매트릭스 / DEFAULT PRIVILEGES / search_path

- §4.4 schema 단위 GRANT (USAGE / SELECT / INSERT-UPDATE-DELETE / sequence USAGE-SELECT) 적용 완료. legacy schema는 의도적으로 생략.
- §4.5 `ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA <9개>` 적용 완료. legacy 제외. 이후 `portfolio_owner` 명의로 새 객체가 만들어질 때 권한 매트릭스가 자동 적용됨.
- §4.6 `ALTER ROLE ... SET search_path` 7건 적용 완료. 각 MS README 정의와 일치(legacy 항목은 호환성 유지용으로 search_path에는 포함되지만 USAGE 미부여라 실제 접근은 차단).

### 검증 SQL 결과 요약 (§5)

- §5.1 role / 속성: 7개 app role + `portfolio_owner` + `portfolio_admin` 모두 존재 확인. 7개 app role의 superuser / createdb / createrole / replication / bypassrls 모두 false.
- §5.1 search_path: 7개 role 모두 본 문서 §3 표와 일치.
- §5.2 schema USAGE / CREATE 매트릭스: §2 표와 불일치 0건. 모든 app role의 `legacy` USAGE = false 확인.
- §5.3 table 권한 요약 / sequence 권한 요약: §2 표와 불일치 0건.
- §5.4 실제 connection 검증
  - `marketconnector_app`: 접속 성공. `connector` 조회 성공, `execution` 조회 성공, `legacy` USAGE = false 확인. `execution` schema에 INSERT / UPDATE / DELETE 시도 → permission denied(기대값 일치).
  - `execution_app`: 접속 성공. `execution` / `decision` / `connector` 조회 성공, `legacy` USAGE = false 확인. `execution` schema에 INSERT / UPDATE / DELETE 가능(기대값 일치).
  - `view_app`: 접속 성공. `execution` / `connector` / `decision` 조회 성공, `legacy` USAGE = false 확인. `execution` schema에 INSERT / UPDATE / DELETE 시도 → permission denied(기대값 일치, view write 범위는 현재 `ops` 한정).

### 본 세션 안전 제약 점검

- secret value 조회 없음. 실제 password / endpoint / account-id / 계좌번호 / token / app key / app secret 기록 없음. 모두 `[REDACTED]` 또는 placeholder만 사용.
- AWS 리소스 생성 / 변경 / 삭제 없음. 본 작업은 RDS 안 SQL 실행과 SQL 결과 기록만 수행.
- 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음.
- 본 작업 산출물 갱신 대상: 본 파일과 [`./validation-checklist.md`](./validation-checklist.md) 2개 파일.

### 후속 작업 권고

- 7개 app role 비밀번호의 정식 보관(Secrets Manager / SSM SecureString)과 IAM 매트릭스는 후속 spec `06-secrets-and-iam`에서 정리.
- 기존 객체(`portfolio_admin` 소유) owner 일괄 이관 여부 결정. 결정에 따라 [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4.2.1 옵션 A / 옵션 B 중 선택.
- 매트릭스 변경 사항(legacy 미부여, `marketconnector_app` execution R only 축소)은 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-DB 카테고리에 갱신 제안.


## 2026-06-09 Local → RDS Migration & RDS 재생성 실행 기록

본 섹션은 운영자가 2026-06-09에 직접 수행한 Local PostgreSQL → aws-paper RDS migration 결과를 기록한다. 본 문서에는 실제 endpoint hostname / password / secret value / account-id / 계좌번호를 적지 않는다(`[REDACTED]` 또는 placeholder만 사용).

### 실행 요약

- 실행 흐름: Local Windows PC → S3 임시 migration bucket → aws-paper MarketConnector EC2 → private RDS PostgreSQL.
- Restore Runner: aws-paper MarketConnector EC2(Amazon Linux 2023, public subnet, EIP attach). Kiro는 본 작업에서 ReadOnly 검증과 문서화만 수행했다.
- 결과: schema / table / index / sequence / FK / trigger / table별 row count 모두 로컬 기준선과 일치(diff 0).

### 1. Local 백업 및 기준선 확보

- pg_dump 결과: `portfolio_full_20260609.dump`(format custom + gzip), 크기 422,334,494 bytes.
- 보관 위치: `C:\Workspaces\db-backup\portfolio_20260609\` (로컬 PC).
- 기준선: schema별 table count, table별 row count snapshot, 주요 object count(index / trigger / sequence / FK), 총 table 수 81개.

### 2. private RDS 직접 접속 시도와 결정

- 로컬 PC에서 RDS endpoint로 직접 접속 시 timeout. RDS endpoint가 private IP로 resolve되고 RDS Publicly accessible = No 상태이므로 기대 동작으로 판단.
- 결정: RDS restore runner를 MarketConnector EC2로 한다. 로컬 PC IP를 RDS Security Group에 직접 허용하지 않는다. RDS Public access = No 유지.

### 3. MarketConnector EC2 준비 (restore runner)

- aws-paper용 EC2 1대 신규 생성: Amazon Linux 2023, public subnet 배치, EIP attach.
- Security Group: RDS PostgreSQL 5432 inbound source를 MarketConnector EC2 SG로 허용. 0.0.0.0/0 5432 허용 없음.
- 접속: EC2 Instance Connect 성공. 로컬 SSH 직접 접속은 outbound 22 제한 가능성으로 보류.
- IAM Role: SSM Session Manager 전환을 위한 기본 managed policy 연결 + 임시 migration bucket read 권한 부여. Access Key는 EC2 내부에 저장하지 않음.
- 패키지: OS 업데이트 완료, AWS CLI 기본 제공 확인. PostgreSQL client는 처음 15.18 설치 후 dump archive header version 불일치 확인 → 15 client 제거 후 18.4 client로 전환(psql 18.4 / pg_restore 18.4). EC2에서 private RDS psql 접속 성공.

### 4. dump 파일 전송과 메타데이터 확인

- 로컬 dump → S3 임시 bucket 업로드 → MarketConnector EC2로 다운로드.
- 무결성: 로컬 / S3 / EC2 dump 파일 크기 422,334,494 bytes 일치.
- pg_restore --list 결과: TOC Entries 812, line count 823, dump source PostgreSQL 18.1, dump format CUSTOM + gzip compression 확인.

### 5. Major version mismatch 발견과 RDS 재생성

- 기존 RDS engine PostgreSQL 16.14 확인. dump source 18.1을 16.14에 restore하는 것은 하위 major version restore라 위험으로 판단.
- 결정: 기존 PostgreSQL 16.14 RDS를 삭제하고 PostgreSQL 18.4 기준으로 재생성한다. RDS Public access = No 유지, initial database name `portfolio` 지정.
- 결과: 신규 RDS `portfolio` DB 접속 성공, version PostgreSQL 18.4 확인.

### 6. 1차 restore 실패와 옵션 보정

- 1차 시도 결과: `role "postgres" does not exist` 오류. 원인은 로컬 dump의 object owner가 `postgres`인데 RDS에는 `postgres` role이 없기 때문으로 판단.
- 보정: `portfolio` DB를 drop / recreate 후 `pg_restore --no-owner --no-privileges` 옵션으로 재실행.
- 결과: 에러 없이 완료. RDS 객체 owner는 restore 실행 계정(`portfolio_admin`) 기준으로 정리됐다.

### 7. 정합성 검증

- schema별 table count 81개 일치. table / index / sequence / FK(33) / trigger(23) / table별 row count clean CSV 82줄 모두 로컬 기준선과 diff 0.
- CRLF / LF 차이는 `--strip-trailing-cr` 옵션으로 정규화한 뒤 비교했다.

### 8. 본 세션 안전 제약 / 민감정보 점검

- 실제 password / secret value / endpoint hostname / account-id / 계좌번호 / token / app key / app secret / webhook URL 신규 기록 없음. 모두 `[REDACTED]` 또는 placeholder만 사용.
- AWS 리소스 변경: 운영자가 직접 진행한 EC2 신규 생성과 기존 RDS 삭제 / 재생성. Kiro는 본 작업으로 AWS 리소스를 변경하지 않았다.
- 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음.
- 본 작업 산출물 갱신 대상: 본 파일 + [`./validation-checklist.md`](./validation-checklist.md). 결정 변경(legacy 미부여 / marketconnector_app execution R-only / view_app execution R-only / REASSIGN OWNED 미실행)은 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-DB-007 ~ OD-DB-010에 별도 누적했다.

### 9. 교훈 / 후속 권고

- PostgreSQL major version mismatch는 시작 단계에서 dump의 `pg_restore --list` 출력과 RDS engine version을 비교해 사전 차단한다(R-DATA-003).
- private RDS는 로컬에서 직접 접속하지 않는다. 항상 EC2 + SSM Session Manager 또는 EIP 기반 restore runner 경유로 접속한다(R-NET-004).
- dump owner role과 RDS role이 다르면 `--no-owner --no-privileges`로 우회하고, 권한 / owner 정리는 별도 SQL(본 spec [`./db-roles-and-grants.md`](./db-roles-and-grants.md))로 진행한다(R-DATA-004).
- 1차 적용에서 `REASSIGN OWNED BY portfolio_admin TO portfolio_owner`를 실행하지 않았으므로, 새 객체에는 default privileges가 자동 적용되지만 기존 객체는 적용되지 않는다. 추가 이관 여부는 운영자 결정으로 후속 분리 관리한다.
