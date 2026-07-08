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

- Kiro ReadOnly 검증 IAM 설계 완료.
  - 사용자 이름 `portfolio-kiro-readonly-validator` / 정책 이름 `PortfolioKiroReadOnlyValidatorPolicy`
  - 상세 설계 참조: [`../../docs/kiro-readonly-validator-iam.md`](../../docs/kiro-readonly-validator-iam.md)
  - 정책 포함: ec2 / rds / secretsmanager(metadata 한정) / iam / cloudwatch / logs / tag ReadOnly
  - 명시 Deny: `secretsmanager:GetSecretValue`, KMS Decrypt
- 자동 검증 실행: AWS CLI ReadOnly 호출만 사용. 리소스 생성/수정/삭제 없음. SecretsManager는 DescribeSecret만 호출하고 GetSecretValue는 호출하지 않음.
- 자동 검증 가능 항목 — 모두 기대값 일치.
  - VPC / 6개 Subnet / IGW attach
  - NAT 미생성 / NAT 역할 EC2 미생성
  - Route Table 3종(rt-public 0/0→IGW, rt-app/rt-data 외부 라우트 없음)
  - SG 8개 인벤토리 / sg-rds-postgres inbound SG 참조만(0/0:5432 없음) / SSH 22 0/0 inbound 0건
  - sg-vpc-endpoints inbound / 운영 SG outbound 표 일치 점검
  - VPC Endpoint 6종 available + Private DNS + sgroup-vpc-endpoints
  - RDS subnet group / parameter group / instance(class / storage / public access / backup retention / deletion protection / encryption / parameter group in-sync)
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
  - §9 secret 자리 [REDACTED] 일관성 — `.kiro/**/*.md` 트리에 대해 아래 패턴 grep 모두 0건. [REDACTED-CANDIDATE] 출력 없음. [O]로 격상. repo 외부(콘솔 캡처 / 개인 노트 / 외부 PC 파일)는 §1 [운영자 확인 필요]로 분리 유지.
    - `(AKIA|ASIA)` access key id
    - Slack incoming webhook URL / JWT
    - `(PASSWORD|SECRET|TOKEN|APP_KEY|APP_SECRET)=값`
    - `aws_secret_access_key=값`
    - `KIS*KEY|SECRET|TOKEN=값`
    - 계좌번호 평문 패턴
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
- 기존 table / sequence / index의 owner는 `portfolio_admin`(restore 실행 계정)으로 그대로 남아 있음.
  - 본 세션에서는 `REASSIGN OWNED BY portfolio_admin TO portfolio_owner`를 실행하지 않음.
  - 기존 객체에 대해서는 §4.5 default privileges가 자동 적용되지 않음.
  - 본 세션에서 §4.4 명시 GRANT만으로 권한 매트릭스를 적용한 상태(이후 새로 만드는 객체는 default privileges 적용 대상).
  - 후속 세션에서 owner 일괄 이관 여부는 운영자 결정으로 분리 관리한다.

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


## 2026-06-13 Local-to-AWS Paper RDS 운영 모드 정리

본 섹션은 2026-06-13 운영자가 결정한 Local 개발 / AWS Paper RDS 운영 원칙을 02 spec 운영 노트에 누적 기록한다.

- RDS Public access 미허용 정책은 변경 없음(02 spec 1차 적용 결과 / R-SEC-001 / R-NET-004 정합).
- 로컬 개발 환경에서 AWS Paper RDS 에 접속할 때의 흐름과 guard 조합을 명문화한다.
- 동일 원칙은 04 spec 2026-06-13 §5(Local-to-AWS Paper RDS) 와 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-ENV-006 / OD-ENV-007 / OD-ENV-008 에 동기화한다.

### 1. Paper 환경 source of truth

1. Paper 환경의 source of truth: 완료
   1) AWS Paper RDS 단일 source of truth 로 고정.
   2) 로컬에서 실행하더라도 `PORT_ENVIRONMENT=paper` 이면 AWS Paper RDS 를 바라본다.
   3) AWS 에서 실행하더라도 동일한 AWS Paper RDS 를 사용한다.
   4) 로컬 PostgreSQL 은 `LOCAL_DEV` fixture / 실험 / 백업 참고용으로만 사용한다.
   5) local DB 와 AWS Paper RDS 간 주문 / 체결 / 포지션 데이터 병합 또는 동기화는 하지 않는다(R-DATA-007 정합 후속 추가).

### 2. 환경 구분 라벨

1. `LOCAL_DEV`: 완료
   1) local PostgreSQL 사용 가능.
   2) 개발 / 실험 / fixture 전용.
   3) 실제 paper 운영 아님.
   4) 주문 실행 금지.
2. `PAPER`: 완료
   1) AWS Paper RDS 사용.
   2) 로컬 실행도 AWS Paper RDS 사용.
   3) AWS 실행도 AWS Paper RDS 사용.
   4) paper 주문 / 체결 / 포지션 source of truth.
3. `LIVE`: 후속
   1) 후속 설계 대상(10 spec 통합).
   2) 실전 운영 source of truth 는 paper 와 분리 필요.

### 3. SSM Port Forwarding 방향

1. RDS 노출 정책: 완료
   1) RDS 는 Private 유지(02 spec 1차 적용 결과 / OD-NET-009 / R-SEC-001 정합).
   2) RDS Public 접근 허용 금지.
2. 로컬에서 AWS Paper RDS 접속 시: 완료
   1) SSM Port Forwarding 사용.
   2) 로컬에서는 `localhost:15433` 같은 포트로 접속하지만 실제 대상은 AWS Paper RDS.
   3) DB host 가 `localhost` 라고 해서 무조건 local DB 로 판단하면 안 된다.
   4) 실제 paper 주문 실행 guard 는 `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` 조합으로 판단(MarketConnector executor `--execute` guard 정합).

### 4. 예시 환경 변수(Local PC + SSM Port Forwarding)

1. 환경 변수 예시: 완료
   1) `PORT_ENVIRONMENT=paper`
   2) `PORT_DB_TARGET=aws-paper`
   3) `INTEREST_DB_HOST=localhost`
   4) `INTEREST_DB_PORT=15433`
   5) `INTEREST_DB_NAME=portfolio`
2. 표기 정책: 완료
   1) 비밀번호 / 계정 / 실제 RDS endpoint hostname / 실제 SSM Port Forwarding session id 평문 기록 금지(R-DOCS-001 정합).
   2) 본 노트 / 후속 spec 산출물에는 placeholder 만 사용한다.

### 5. 금지 사항

1. local 환경의 paper 주문 실행 금지: 완료
   1) local PostgreSQL 에서 paper 주문 실행 금지.
2. local DB 와 AWS Paper RDS 간 동기화 금지: 완료
   1) `connector_order_request` 병합 금지.
   2) `connector_fill` 병합 금지.
   3) `strategy_execution_order` 병합 금지.
   4) `strategy_position_state` 병합 금지.

### 6. 후속 인계

1. SSM Port Forwarding runbook 정리: 후속
   1) 02 spec runbook 또는 별도 운영 노트에 SSM Port Forwarding → AWS Paper RDS 접속 절차 정식 기재.
2. 모든 MS 의 환경변수 점검: 후속
   1) 8개 MS 의 환경변수 인벤토리에서 `PORT_ENVIRONMENT` / `PORT_DB_TARGET` 정합 여부 점검.
3. aws-live 통합 시점: 후속
   1) `LIVE` 라벨의 source of truth 는 paper 와 별도 RDS 로 분리(10 spec 통합 시점 결정).


## 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook

본 섹션은 같은 일자(2026-06-13)의 Local-to-AWS Paper RDS 운영 모드 정리(앞 섹션) 와 별개로, 운영자가 직접 수행한 SSM Port Forwarding 기반 로컬 → AWS Paper RDS 연결 1차 실증 검증 결과를 누적 기록한다.

- 본 섹션은 동시에 Local-to-AWS Paper RDS SSM Port Forwarding Runbook 의 1차 본문으로 사용된다.
- RDS Public access 미허용 정책(R-SEC-001 / R-NET-004) 변경 없음.
- AWS / RDS / IAM 변경 0건 — 본 일자에는 read-only AWS API 호출과 SSM Port Forwarding 세션 + Python `psycopg2` 접속 검증만 수행했다.

### 1. 사전 도구 확인

1. AWS CLI 확인: 완료
   1) 명령어: `aws --version`
   2) 결과: `aws-cli/2.27.50 Python/3.13.4 Windows/11 exe/AMD64`
2. Session Manager Plugin 확인: 완료
   1) 1차 실행 시 `session-manager-plugin` PowerShell 인식 실패 → Plugin 설치 후 재확인.
   2) 명령어: `session-manager-plugin --version`
   3) 결과: `1.2.814.0`
   4) 판단: SSM Port Forwarding session 기동 가능 상태.

### 2. SSM Port Forwarding 표준 경유지 결정

1. 실행 중 EC2 점검: 완료
   1) 명령어: `aws ec2 describe-instances`
   2) `portfolio-paper-marketconnector-ec2`
       - instance id: `i-0fce77927b7397b88`
       - private ip: `10.0.0.181`
       - state: `running`
   3) `portfolio-paper-crawler-worker`
       - instance id: `i-0ff768ea639a91355`
       - private ip: `10.0.0.169`
       - state: `running`
2. 표준 경유지 결정: 완료
   1) SSM Port Forwarding 경유지 = `portfolio-paper-marketconnector-ec2` (instance id `i-0fce77927b7397b88`).
   2) 결정 사유:
       - 본 EC2 는 2026-06-09 RDS restore runner 로 동일 RDS 접근 검증 이력 보유(앞 섹션 §3 정합).
       - MarketConnector / Strategy Execution / View 가 바라볼 Paper DB 의 운영 경유지 역할이 자연스러움(03 spec 정합).
       - `portfolio-paper-crawler-worker` 는 KRX GUI / Windows worker 역할로 유지(08 spec 정합 — 본 EC2 는 SSM Port Forwarding 경유지로 사용하지 않는다).
   3) 결정 락: OD-NET-010 (SSM Port Forwarding 표준 경유지) 으로 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 갱신.

### 3. 대상 EC2 SSM Managed Node 점검

1. SSM Online 상태 확인: 완료
   1) 명령어: `aws ssm describe-instance-information --filters "Key=InstanceIds,Values=i-0fce77927b7397b88"`
   2) 결과:
       - instance id: `i-0fce77927b7397b88`
       - ping status: `Online`
       - platform type: `Linux`
       - agent version: `3.3.4515.0`
   3) 판단: SSM Port Forwarding target 으로 사용 가능.

### 4. AWS Paper RDS endpoint 확인

1. RDS metadata 조회: 완료
   1) 명령어: `aws rds describe-db-instances --db-instance-identifier portfolio-paper-rds`
   2) 결과:
       - DB name: `portfolio`
       - endpoint hostname: `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com`
       - port: `5432`
       - publicly accessible: `False`
       - status: `available`
   3) 판단: RDS 는 Private 유지(OD-NET-009 / R-SEC-001 / R-NET-004 정합). Public 노출 없이 SSM Port Forwarding 으로 로컬 접속 가능.

### 5. SSM Port Forwarding 터널 오픈

1. 표준 명령어: 완료
   1) 명령어: `aws ssm start-session --target i-0fce77927b7397b88 --document-name AWS-StartPortForwardingSessionToRemoteHost --parameters host="portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com",portNumber="5432",localPortNumber="15433"`
2. 세션 결과: 완료
   1) session id: `terraform-vjp3fv3nz73konetcevdzjh9de`
   2) local port: `15433`
   3) remote RDS port: `5432`
   4) message: `Port 15433 opened`
   5) message: `Waiting for connections...`
3. 연결 구조: 완료
   1) Local PC `localhost:15433`
   2) → SSM Session Manager tunnel
   3) → `portfolio-paper-marketconnector-ec2` (instance id `i-0fce77927b7397b88`)
   4) → AWS Paper RDS `portfolio-paper-rds:5432`

### 6. 로컬 psql client 미설치 / PATH 미등록 확인

1. 1차 시도: 실패
   1) 명령어: `psql -h localhost -p 15433 -U portfolio_admin -d portfolio`
   2) 결과: PowerShell 에서 `psql` 명령어 인식 실패.
   3) 판단: AWS / SSM / RDS 정합성 문제 아님. 로컬 PostgreSQL client PATH 미등록 문제(R-AUTO-013 정합 후속 추가).
2. 보정 방향: 완료(즉시 조치는 보류)
   1) 즉시 psql 설치 진행하지 않음.
   2) Python `psycopg2` 로 접속 확인 진행(§7 / §8 / §9).
   3) psql client 정식 설치 / PATH 등록은 후속(`_common/followups-overview.md` 2026-06-13 §1).

### 7. Python `psycopg2` 사용 가능 확인

1. 모듈 import 점검: 완료
   1) 명령어: `python -c "import psycopg2; print('psycopg2 OK')"`
   2) 결과: `psycopg2 OK`
   3) 판단: Python 기반 DB 접속 테스트 가능.

### 8. `portfolio_admin` 접속 확인

1. 접속 파라미터: 완료
   1) host: `localhost`
   2) port: `15433`
   3) dbname: `portfolio`
   4) user: `portfolio_admin`
   5) password: 환경변수 `PGPASSWORD` 사용(평문 노출 0건, R-DOCS-001 정합)
2. 접속 결과: 완료
   1) 출력 요약(`current_user`, `current_database`, `inet_server_addr`, `inet_server_port`):
       - `('portfolio_admin', 'portfolio', '10.0.20.165', 5432)`
   2) 판단:
       - 로컬 PC 에서 SSM tunnel 을 경유해 AWS Paper RDS 접속 성공.
       - 실제 RDS private IP = `10.0.20.165` (RDS endpoint resolve 결과).
       - 실제 RDS port = `5432`.
       - `localhost:15433` 의 실제 대상이 AWS Paper RDS 임이 1차 실증됨(OD-ENV-007 정합).

### 9. `execution_app` 접속 확인 (Strategy Execution 포팅 사전 검증)

1. 접속 파라미터: 완료
   1) host: `localhost`
   2) port: `15433`
   3) dbname: `portfolio`
   4) user: `execution_app`
   5) password: 환경변수 `PGPASSWORD` 사용(평문 노출 0건)
2. 접속 결과: 완료
   1) `current_user`: `execution_app`
   2) `current_database`: `portfolio`
   3) `search_path`: `execution, decision, research, connector, preprocessor, interest, reference, legacy, public`
3. 판단: 완료
   1) Strategy Execution(`port_strategy_execution`) AWS 포팅 전 단계의 AWS Paper RDS app role 접속 1차 검증 통과.
   2) `execution_app` 의 search_path 가 [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §3 / OD-DB-006 / OD-DB-007 정합(legacy 는 search_path 에 포함되지만 USAGE 미부여로 실제 접근 차단).
   3) 본 일자 검증은 SELECT 조회 한정(`current_user` / `current_database` / `inet_server_addr` / `inet_server_port` / `search_path`) — INSERT / UPDATE / DELETE / DDL 0건.

### 10. Local-to-AWS Paper RDS SSM Port Forwarding Runbook (1차 본문)

1. 사전 점검 단계: [확인]
   1) 로컬 AWS CLI 확인 — `aws --version`
   2) Session Manager Plugin 확인 — `session-manager-plugin --version`
   3) 대상 EC2 running 상태 확인 — `aws ec2 describe-instances --instance-ids i-0fce77927b7397b88`
   4) 대상 EC2 SSM Online 상태 확인 — `aws ssm describe-instance-information --filters "Key=InstanceIds,Values=i-0fce77927b7397b88"`
   5) AWS Paper RDS endpoint / status 확인 — `aws rds describe-db-instances --db-instance-identifier portfolio-paper-rds`
2. SSM Port Forwarding 터널 오픈 단계: [실행]
   1) 명령어:
       - `aws ssm start-session --target i-0fce77927b7397b88 --document-name AWS-StartPortForwardingSessionToRemoteHost --parameters host="portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com",portNumber="5432",localPortNumber="15433"`
   2) 성공 출력:
       - `Port 15433 opened`
       - `Waiting for connections...`
   3) tunnel 유지 조건:
       - 본 PowerShell / 터미널 창을 닫지 않는다(R-AUTO-012 정합).
       - 추가 작업은 별도 PowerShell 창에서 수행한다.
3. 로컬 환경변수 표준 export 단계: [준비]
   1) 본 spec / 다른 산출물 본문에 password / secret 평문 기록 금지(R-DOCS-001 정합).
   2) 표준 환경변수 키:
       - `PORT_ENVIRONMENT=paper`
       - `PORT_DB_TARGET=aws-paper`
       - `INTEREST_DB_HOST=localhost`
       - `INTEREST_DB_PORT=15433`
       - `INTEREST_DB_NAME=portfolio`
   3) `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD` 는 MS 별 app role 로 분리(§10.5).
4. 접속 검증 단계: [확인]
   1) Python `psycopg2` 점검 — `python -c "import psycopg2; print('psycopg2 OK')"`
   2) `portfolio_admin` 접속 확인:
       - `current_user` = `portfolio_admin`
       - `current_database` = `portfolio`
       - `inet_server_addr` = `10.0.20.165`
       - `inet_server_port` = `5432`
   3) MS 별 app role 접속 확인(§10.5 표) — `current_user` / `current_database` / `search_path` 출력 검증.
   4) psql client 가 PATH 에 있는 경우 한해 `psql -h localhost -p 15433 -U <app_role> -d portfolio` 도 사용 가능. 본 일자 시점에는 PATH 미등록(R-AUTO-013 정합).
5. MS 별 app role 매핑: [준비]
   1) Strategy Execution: `execution_app`
   2) MarketConnector: `marketconnector_app`
   3) Interest Crawler: `crawler_app`
   4) Interest Preprocessor: `preprocessor_app`
   5) View: `view_app`
   6) password / secret value 본 runbook 본문 / 운영자 노트 / 콘솔 캡처 / 로그 평문 기록 금지(R-DOCS-001 정합).
6. 성공 기준: [확인]
   1) SSM tunnel 메시지가 `Port 15433 opened` 상태로 유지됨.
   2) `portfolio_admin` 으로 AWS Paper RDS 접속 가능.
   3) `execution_app` 으로 AWS Paper RDS 접속 가능(본 일자 1차 검증 완료).
   4) `execution_app` search_path = `execution, decision, research, connector, preprocessor, interest, reference, legacy, public` 일치.
   5) RDS `PubliclyAccessible` 값이 `False` 로 유지됨.
7. 실패 / 복구: [복구]
   1) `Port 15433 opened` 메시지가 안 나오면 SSM Plugin 설치 / EC2 SSM Online / IAM Role / VPC Endpoint 5종(`com.amazonaws.<region>.ssm` / `ssmmessages` / `ec2messages`) 점검 후 재시도.
   2) tunnel 창이 닫혔다면 같은 명령어로 새 session 재기동(R-AUTO-012 정합).
   3) 접속 단계에서 `password authentication failed` 발생 시 environment 의 `PGPASSWORD` 값 / app role password 정합 점검(평문 출력 금지).
   4) `relation does not exist` / `permission denied` 발생 시 02 spec [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4 GRANT 매트릭스 / §5 검증 SQL 재확인(R-DATA-005 정합).

### 11. 안전 / 보안 점검 결과

1. AWS 리소스 생성 / 수정 / 삭제 0건. read-only AWS API(`aws ec2 describe-instances` / `aws ssm describe-instance-information` / `aws rds describe-db-instances`) + SSM Port Forwarding 세션(`aws ssm start-session` 한정) + Python `psycopg2` SELECT 조회만 사용.
2. RDS DDL/DML 0건. INSERT / UPDATE / DELETE / DDL 0건. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 0건.
3. 실제 password / secret value / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 IAM access key id / 실제 secret ARN / EIP 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
4. 본 노트 / runbook 본문에 평문으로 포함된 운영 식별자:
   - instance id: `i-0fce77927b7397b88` / `i-0ff768ea639a91355`
   - private IP: `10.0.0.181` / `10.0.0.169` / `10.0.20.165`
   - local port: `15433`
   - SSM session id: `terraform-vjp3fv3nz73konetcevdzjh9de`
   - RDS endpoint hostname: `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com`

   사용자 명시 정책에 따라 운영 식별자는 작업 로그 / runbook 에는 기록 가능, 민감정보(secret value / password / token / account-id / KIS 자격)는 절대 평문 기록 금지.
5. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 본 일자 작업으로 인한 변경 0건.
6. session id 는 본 일자 검증 세션의 식별자(임시값)이며, 동일 세션 재기동 시 다른 식별자가 생성된다. 본 노트의 session id 는 재현 / 추적 목적의 사실 기록일 뿐 secret 이 아니다.

### 12. 본 일자 범위 밖 / 후속 인계

1. psql client 정식 설치 / PATH 등록: 후속(R-AUTO-013).
2. 모든 MS 의 Paper mode DB 환경변수 인벤토리 점검: 후속(`_common/followups-overview.md` 2026-06-13 §2).
3. Strategy Execution(`port_strategy_execution`) AWS 포팅 본 phase: 후속(04 spec 후속 phase).
4. MarketConnector 신규 executor(`connector_strategy_order_execute.py`) EC2 배포 후보 zip / tag 산출: 후속(03 spec 후속 phase 또는 07 spec).
5. 평일 또는 안전한 테스트 데이터로 `READY -> REQUESTED -> SUBMITTED` end-to-end dry / integration 검증: 후속(R-AUTO-009 / R-AUTO-010 / R-AUTO-011 정합).
6. EventBridge Scheduler 정기 trigger / Step Functions hybrid orchestration: 04 / 08 spec 후속 phase.
7. SSM Port Forwarding session 자동 keep-alive / reconnect: 운영자 확인(현재는 수동 재기동 정책, R-AUTO-012 정합).


## 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 보강 (psql 18 client + pgAdmin4 접속 검증)

본 섹션은 같은 일자 앞 섹션(`## 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook`) §1 ~ §12 의 후속이다.

- 운영자가 동일 SSM Port Forwarding tunnel 위에서 추가 client 2종(로컬 PostgreSQL 18 `psql.exe` 직접 경로 실행 + pgAdmin4) 으로 AWS Paper RDS 접속을 1차 실증한 결과를 누적 기록한다.
- SSM tunnel 자체는 재사용(같은 명령 / 같은 local port `15433` / 새 session id 가능).
- RDS Public access 미허용 정책 / OD-NET-009 / R-SEC-001 / R-NET-004 본문 변경 없음.
- AWS / RDS / IAM 변경 0건 — read-only AWS API + SSM Port Forwarding 세션 + psql / pgAdmin4 SELECT 조회만 사용.

### 1. 로컬 PostgreSQL 18 psql client 접속 검증

1. 로컬 PostgreSQL 설치 인벤토리: 완료
   1) 경로: `C:\Program Files\PostgreSQL`
   2) 버전 폴더: `17`, `18`
2. 직접 경로 실행 명령어: 완료
   1) 명령어: `& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -p 15433 -U portfolio_admin -d portfolio`
   2) 일반 `psql` PATH 등록은 여전히 미완료 — full path 직접 실행으로 작업 진행(R-AUTO-013 mitigation 보강 정합).
3. 접속 결과: 완료
   1) psql client: `18.1`
   2) server: `18.4`
   3) SSL connection: `TLSv1.3`
   4) `current_user`: `portfolio_admin`
   5) `current_database`: `portfolio`
   6) `inet_server_addr`: `10.0.20.165`
   7) `inet_server_port`: `5432`
4. 비밀번호 입력 관련 메모: 운영자 확인
   1) 접속 시 비밀번호를 묻지 않음 — PowerShell 세션의 `PGPASSWORD` 환경변수를 psql 이 사용했기 때문으로 판단.
   2) 필요 시 `Remove-Item Env:PGPASSWORD` 로 세션 내 비밀번호 환경변수 제거 가능.
   3) 본 노트 / 콘솔 캡처 / 로그 평문 기록 0건(R-DOCS-001 정합).
5. 판단: 완료
   1) 로컬 PostgreSQL 18 psql client → SSM tunnel → AWS Paper RDS 18.4 접속 1차 실증.
   2) client major 18 / full 18.4 / server 18.4 정합(R-DATA-003 mitigation 정합 / 03 spec design §5 정합).

### 2. pgAdmin4 접속 검증

1. 서버 등록 파라미터: 완료
   1) Host name/address: `localhost`
   2) Port: `15433`
   3) Maintenance database: `portfolio`
   4) Username: `portfolio_admin`
   5) Password: `portfolio_admin` 비밀번호 — 본 노트 / 콘솔 캡처 / 로그 평문 기록 0건(R-DOCS-001 정합).
   6) SSL mode: `Prefer` 또는 기본 TLS 자동 연결.
2. SSM tunnel 측 확인: 완료
   1) 같은 일자 앞 섹션 §5 의 tunnel 창에서 pgAdmin4 접속 시 `Connection accepted for session [...]` 메시지 출력 확인.
   2) tunnel 창 종료 시 pgAdmin4 연결도 즉시 단절(R-AUTO-012 정합).
3. 확인 SQL: 완료
   1) `select current_user, current_database(), inet_server_addr(), inet_server_port(), current_setting('search_path');`
4. 확인 결과: 완료
   1) `current_user`: `portfolio_admin`
   2) `current_database`: `portfolio`
   3) `inet_server_addr`: `10.0.20.165`
   4) `inet_server_port`: `5432`
   5) `search_path`: `"$user", public`
       - 비고: `portfolio_admin` 은 본 일자 시점에 `ALTER ROLE ... SET search_path` 적용 대상이 아님 — app role(7종) 만 search_path 적용(OD-DB-006 정합 / [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §3 정합). 따라서 `portfolio_admin` 의 `"$user", public` 출력은 정상.
5. schema / table 조회 가능 확인: 완료
   1) `connector.connector_account`
   2) `connector.connector_order_request`
   3) `connector.connector_order_event`
   4) `connector.connector_fill`
   5) `decision.strategy_daily_position_decision`
   6) `decision.strategy_daily_run`
   7) `execution.connector_signal_order_map`
   8) `execution.strategy_execution_order`
   9) `execution.strategy_execution_plan`
   10) `execution.strategy_position_state`
   11) `interest.interest_pool`
   12) `ops.strategy_daily_batch_run`
   13) `ops.strategy_daily_batch_step_log`
   14) `preprocessor.pre_agency_analysis`
   15) `preprocessor.pre_news_daily_feature`
   16) 본 일자 검증은 SELECT 가능 여부 확인까지만 수행 — INSERT / UPDATE / DELETE / DDL 0건.
6. 판단: 완료
   1) pgAdmin4 → SSM tunnel → AWS Paper RDS 접속 1차 실증.
   2) pgAdmin4 는 `localhost:15433` 로 접속하지만 실제 대상은 AWS Private RDS(`10.0.20.165:5432`).
   3) SSM tunnel 이 유지되는 동안 pgAdmin4 에서 AWS Paper RDS 운영 데이터 조회 가능.
   4) tunnel 종료 시 pgAdmin4 연결 즉시 단절(R-AUTO-012 detection 보강 정합).

### 3. Runbook §10 보강(앞 섹션 §10 의 보조 절차)

본 섹션은 앞 섹션 §10 의 Runbook 1차 본문 본문을 변경하지 않고, 추가 client 2종에 대한 보조 절차를 보강한다.

1. 접속 검증 단계 (보조): [확인]
   1) 로컬 PostgreSQL 18 psql client 직접 경로 실행 — `& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -p 15433 -U portfolio_admin -d portfolio`
   2) 결과 확인 항목:
       - psql client major / full / server major / full 일치(client `18.1` / server `18.4`)
       - `SSL connection: TLSv1.3`
       - `current_user` / `current_database` / `inet_server_addr` / `inet_server_port` 일치
   3) 일반 `psql` PATH 미등록은 정상 — full path 직접 실행 또는 PATH 등록 후속(R-AUTO-013 정합).
2. pgAdmin4 서버 등록 단계: [준비]
   1) Register Server
       - Name: `AWS Paper RDS - portfolio`
   2) Connection
       - Host name/address: `localhost`
       - Port: `15433`
       - Maintenance database: `portfolio`
       - Username: `portfolio_admin` 또는 MS 별 app role
       - Password: 해당 DB 비밀번호 — 본 runbook 본문 / 운영자 노트 / 콘솔 캡처 / 로그 평문 기록 금지(R-DOCS-001 정합).
   3) SSL
       - SSL mode: `Prefer`
   4) 주의:
       - SSM Port Forwarding PowerShell 창이 열려 있어야 접속 가능(`Port 15433 opened` 유지).
       - tunnel 종료 / `Ctrl + C` 시 pgAdmin4 연결도 즉시 단절(R-AUTO-012).
       - pgAdmin4 서버 등록은 RDS endpoint 가 아니라 `localhost:15433` 으로 설정(OD-NET-011 정합).
3. 성공 기준 보강: [확인]
   1) psql / pgAdmin4 출력에서 `inet_server_addr = 10.0.20.165` / `inet_server_port = 5432` 일치(앞 섹션 §10.6 성공 기준 보강).
   2) RDS `PubliclyAccessible` 값 `False` 유지(앞 섹션 §10.6 그대로).

### 4. 안전 / 보안 점검 결과

1. AWS / RDS / IAM / Secrets Manager / SSM 변경 0건. read-only AWS API + 같은 SSM Port Forwarding tunnel 재사용 + psql / pgAdmin4 SELECT 조회만 사용.
2. RDS DDL/DML 0건. INSERT / UPDATE / DELETE / DDL 0건. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 0건. `--execute` 실호출 0건.
3. 실제 password / secret value / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 IAM access key id / 실제 secret ARN / EIP 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
4. 운영 식별자(앞 섹션 §11 의 instance id / private IP / local port / SSM session id / RDS endpoint hostname) 그대로 재사용. 본 섹션 추가 운영 식별자: 로컬 PostgreSQL 설치 경로(`C:\Program Files\PostgreSQL\18\bin\psql.exe`) — 로컬 PC 의 도구 경로이며 secret 가 아님.
5. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 본 일자 작업으로 인한 변경 0건. pgAdmin4 / psql 18 client 는 운영자 로컬 PC 도구로 별도 spec 산출물 영향 없음.

### 5. 본 일자 범위 밖 / 후속 인계

1. 일반 `psql` 명령어 PATH 등록 — `C:\Program Files\PostgreSQL\18\bin` 을 시스템 / 사용자 PATH 에 등록(R-AUTO-013 정합 / `_common/followups-overview.md` 2026-06-13 SSM Port Forwarding 후속 메모 보강).
2. pgAdmin4 의 환경별(paper / live) 서버 분리 등록 정책 — paper 환경에서 잘못해서 live RDS 를 등록하지 않도록 서버 이름 prefix 정책(예: `AWS Paper RDS - portfolio`) 표준화. live 환경은 후속 분리(10 spec).
3. pgAdmin4 / psql 의 app role 별 비밀번호 보관 — 운영자 로컬 PC 환경 책임. Secrets Manager 에서 직접 주입하지 않음. 운영자 실수 시 R-DOCS-001 위반 방지를 위해 화면 캡처 / 채팅 / 노트에 평문 기록 금지 원칙 유지.
4. Strategy Execution(`port_strategy_execution`) AWS 포팅 본 phase: 후속(04 spec 후속 phase). 본 일자에는 client 3종(Python `psycopg2` / psql 18 / pgAdmin4) 으로 사전 접속 가능성 1차 실증 완료.


## 2026-06-17 Daily AWS 17-step E2E 흐름 중 발견된 DB Role / 권한 / search_path 보정

운영자가 같은 일자 두 번째 세션(Daily AWS 17-step E2E 완료) 진행 중 발견한 본 spec 범위의 DB Role / 권한 / search_path 보정 사실을 누적 기록한다.

- 본 spec 자체의 추가 결정 0건 / 본문 변경 0건.
- 결정 정합 검증과 후속 정식 매트릭스 갱신만 사실 기록.
- 자세한 17 step 전체 진행 상태는 03 / 04 / 06 / 08 / 09 spec operation-notes 의 2026-06-17 섹션 참조.
- Kiro 는 문서 작성 / 절차 정리만 수행. 실제 GRANT / search_path 변경은 운영자 직접 진행.
- 본 일자는 `aws-paper` 한정 / aws-live 작업 0건.
- password / endpoint hostname / account-id / 실제 ARN / 계좌번호 본 노트 평문 기록 0건.

### 1. `execution_app` 의 `interest` schema 권한 보정 (Step 8 영향)

1. 1차 실패 사실: 확인
   1) Daily AWS 17-step 의 8번 `DAILY_BUY_EXECUTION` ECS RunTask 가 1차 실행에서 `execution_app` 의 `interest` schema / table SELECT 권한 누락으로 실패(R-DATA-005 [2026-06-17 보강] 정합).
   2) 영향 범위: 04 spec operation-notes 2026-06-17 §3 정합. preprocessor 자체(3번 step)는 정상 완료 / 본 권한 누락은 8번 step 영향에 한정.
2. 운영자 조치 사실: 완료(2026-06-17 §3 운영자 직접 GRANT 보정)
   1) `execution_app` 에 `interest` schema USAGE 권한 부여.
   2) `interest.*` table SELECT 권한 부여(execution_app 이 buy execution 흐름에서 interest schema 읽기 필요).
   3) sequence 권한 부여(필요한 sequence 한정).
   4) future default privileges 보정(`ALTER DEFAULT PRIVILEGES IN SCHEMA interest GRANT SELECT ON TABLES TO execution_app` 등) — 후속 객체 신규 생성 시 자동 적용.
3. 02 spec 정식 매트릭스 갱신: 후속
   1) [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4 GRANT / §5 검증 SQL 의 `execution_app` 행에 `interest` schema USAGE / table SELECT / sequence / default privileges 사실 반영은 후속 phase.
   2) 운영자 직접 GRANT 결과는 본 노트에 사실로만 기록 — 정식 매트릭스 갱신 시점에 02 spec 본문 갱신 / 본 일자에는 본문 변경 없음.

### 2. `marketconnector_app` 의 `legacy` schema / `legacy.holdings` / search_path 보정 (Step 17 영향)

1. 1차 실패 사실: 확인
   1) Daily AWS 17-step 의 17번 `BALANCE_REFRESH` SSM RunCommand 가 1차 실행에서 bare `holdings` 의 `relation does not exist` 오류로 실패(R-DATA-011 신규 정합).
   2) 원인: `marketconnector_app` 의 `legacy` schema USAGE 미부여(OD-DB-007 정합 — legacy schema 모든 app role 미부여 정책의 1건 예외 발생) + `legacy.holdings` DML 미부여 + sequence 미부여 + database search_path 누락.
   3) 영향 범위: 03 spec operation-notes 2026-06-17 §4 정합.
2. 운영자 조치 사실: 완료(03 spec §4 운영자 직접 작업 정합)
   1) `marketconnector_app` 의 database search_path 를 `connector, execution, legacy, reference, public` 로 보정(`ALTER ROLE marketconnector_app IN DATABASE portfolio SET search_path = ...`).
   2) `legacy` schema USAGE 권한 부여(legacy 운영 데이터 접근 필요 — OD-DB-007 의 1건 예외 / 후속 재검토 후보).
   3) `legacy.holdings` DML(SELECT / INSERT / UPDATE / DELETE) 권한 부여(legacy 운영 데이터 갱신 최소 권한 한정).
   4) `legacy` schema sequence 권한 부여 + future default privileges 보정.
3. 02 spec 정식 매트릭스 갱신: 후속
   1) [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4 GRANT / §5 검증 SQL 의 `marketconnector_app` 행에 `legacy` schema USAGE / `legacy.holdings` DML / sequence / database search_path 사실 반영은 후속 phase.
   2) OD-DB-007(legacy schema 모든 app role 미부여) 정책의 marketconnector_app 한정 1건 예외 사실은 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) Change Log 2026-06-17 두 번째 항목에 사실 기록 / 본문 결정값 변경 후속 분리.

### 3. 검증 SQL 보강 후보 / app role 별 search_path / grants 검증 SQL

1. app role 별 search_path 점검 SQL 후보:
   1) `SELECT rolname, rolconfig FROM pg_roles WHERE rolname IN ('execution_app', 'marketconnector_app', 'decision_app', 'preprocessor_app', 'crawler_app', 'research_app', 'view_app');` — `rolconfig` 에서 `search_path=...` 항목 추출.
   2) 또는 각 role 로 접속 후 `SHOW search_path;` 직접 실행.
   3) 본 일자 `marketconnector_app` 의 search_path 가 `connector, execution, legacy, reference, public` 로 보정되었음을 정기 검증 항목으로 추가.
2. future default privileges 점검 SQL 후보:
   1) `SELECT * FROM pg_default_acl WHERE defaclnamespace = 'legacy'::regnamespace;` — legacy schema 의 default ACL 확인.
   2) `SELECT * FROM pg_default_acl WHERE defaclnamespace = 'interest'::regnamespace;` — interest schema 의 default ACL 확인.
   3) 본 일자 보정 결과로 `execution_app` interest / `marketconnector_app` legacy 에 대한 default privileges 가 적용되었음을 정기 검증.
3. bare table name 의존 legacy 경로 검증 SQL 후보:
   1) `SET ROLE marketconnector_app;` `SELECT 1 FROM holdings LIMIT 1;` — bare `holdings` 가 search_path 안에서 탐색되는지 확인.
   2) BALANCE_REFRESH 진입 전 사전 점검 SQL 로 추가.
4. R-DATA-011 detection 정합으로 본 검증 SQL 들을 17-step 진입 단계의 정기 항목으로 추가 — 정식 매트릭스 갱신은 후속 phase.

### 4. 안전 / 보안 점검 결과

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 변경 0건. 02 spec 본문 결정값 변경 0건 — 정식 매트릭스 갱신은 후속.
2. 아래 민감정보는 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.
   - 실제 password / RDS endpoint hostname / RDS port / database name / username
   - account-id / 실제 IAM Role ARN / 실제 secret ARN

   운영 식별자만 사실 기록:
   - role 이름 `execution_app` / `marketconnector_app`
   - schema 이름 `interest` / `legacy` / table 이름 `holdings`
   - search_path 값 `connector, execution, legacy, reference, public`
3. RDS / GRANT / ALTER ROLE 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건. CloudWatch Logs 본문 / SSM 응답 본문 / 운영자 PowerShell stdout 전문 본 노트 평문 인용 0건.
4. RDS DDL 0건(본 spec 범위 — schema 생성 / drop / table 생성 / drop 0건). DML 0건(본 spec 범위 — `legacy.holdings` 직접 변경은 03 spec Step 17 책임). GRANT / REVOKE / ALTER DEFAULT PRIVILEGES / ALTER ROLE 작업이 본 일자에 발생했고 이는 본 노트에 사실로만 기록.
5. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 직접 호출 0건. live 자동 GRANT / DDL / DML 은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건. R-DATA-005 / R-DATA-011 mitigation 1차 실증 / Status `Mitigated` 갱신 정합.
