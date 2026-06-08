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
