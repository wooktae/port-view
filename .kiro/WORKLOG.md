# Kiro AWS Migration Worklog

본 문서는 `.kiro` 작업공간의 간단 작업 로그다. 각 MS의 `docs/worklog/YYYY-MM-DD.md`처럼 상세하게 쓰지 않으며, 날짜별로 별도 파일을 만들지 않고 본 단일 파일에 누적한다.

## 작성 원칙

- 날짜별 섹션을 본 파일 안에 누적한다.
- 각 날짜 섹션은 5 ~ 10줄 정도로 간단히 기록한다.
- 상세 구현 로그, 긴 검증 로그, 코드 변경 세부사항은 기록하지 않는다.
- 실제 AWS 리소스 생성 여부, 애플리케이션 소스 코드 수정 여부, 8개 MS 문서 수정 여부, 민감정보 기록 여부는 짧게 남긴다.

## 2026-06-12 (Windows EC2 worker 기반 KRX GUI 의존 수집 1차 검증)

- 08-interest-crawler-and-preprocessor-ecs 의 KRX GUI 의존 수집(KRX program / KRX shortsell) 1차 운영 가능 상태 도달. 운영자가 직접 Windows EC2 worker(venv `C:\portfolio\venvs\interest-crawler` / Python 3.13.5)에서 RDS Secret(`/portfolio/paper/rds/crawler-app`) 기반 DB 환경변수 주입, 다운로드 경로 junction 조치(`C:\Users\USER\Downloads` → `C:\Users\Administrator\Downloads`), KRX Secrets Manager(`/portfolio/paper/krx/crawler-login`) 신규 생성 + IAM 권한 추가(secret 한정 / wildcard 0건), `interest_krx_login_new.py` / `interest_program.py` / `interest_shortsell.py` 단독 실행 성공.
- DB 적재 결과: `interest_program_raw` 543 → 546(2026-06-09 / 2026-06-10 / 2026-06-11 각 1건), `interest_shortsell_raw` 189158 → 190205(각 349건). 적재 정합성은 `check_program_rows.py` / `check_shortsell_rows.py` 로 운영자가 직접 확인.
- EC2 worker daily wrapper(`run_krx_worker_daily.ps1`) 운영자 직접 신규 생성. 1차 실행에서 KRX login / program / shortsell 모두 성공. 재실행에서는 신규 수집 대상이 없어 `[Collected Date] None` 출력 — 실패가 아니라 idempotent / no-op 성격의 정상 완료로 분류. 로그 파일 형식 `krx_worker_daily_yyyyMMdd_HHmmss.log` 확인.
- 분류 결정(Hybrid execution model): KRX GUI 의존 crawler = Windows EC2 worker 로 분리 확정 / non-GUI crawler = ECS Fargate Task 후보 유지 / preprocessor = ECS Fargate Task 유지(2026-06-10 그대로). 완전 자동화(SSM RunCommand / EventBridge Scheduler / Step Functions hybrid orchestration / CloudWatch Logs / wrapper 내 DB 검증 자동 추가 / EC2 worker stop 절차)는 후속 분리.
- 한글 literal / 인코딩 검증 결과 향후 EC2 내부 Python 소스 수정 시 PowerShell `Get-Content` / `Set-Content` 직접 replace 대신 Python patch script(`read_text(encoding="utf-8-sig")` + `write_text(encoding="utf-8")`) 방식 사용 결정.
- Kiro 작업 산출물: `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` 2026-06-12 섹션 추가, 같은 spec 의 tasks.md(crawler runtime 항목 갱신 + §11 Windows EC2 worker task 44 ~ 52 + §12 후속 task 53 ~ 60), design.md(§12 Hybrid execution model 보강), requirements.md(R8 acceptance criterion 5 추가). `.kiro/specs/_common/operator-decisions.md`(OD-MS-011 / OD-MS-012 / OD-SEC-008 추가 + Decision Summary / Tentative / Change Log 갱신), risk-register.md(R-AUTO-005 mitigation 보강 + R-AUTO-006 / R-SEC-005 / R-DOCS-002 / R-AUTO-007 신규 추가), followups-overview.md(2026-06-12 후속 메모), `.kiro/CHANGELOG.md` 2026-06-12 섹션, 본 파일.
- 실제 AWS / IAM / Secrets Manager / EC2 / RDS 작업은 운영자가 직접 수행. Kiro 는 문서 / 절차 / 검증 항목 정리만 수행. 8개 MS(`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건. 운영자가 EC2 내부에 직접 생성한 ps1 / py 테스트 / wrapper 파일은 사실만 operation-notes 에 기록하고 파일 본문 전체 인용 0건.
- 민감정보(secret value / KRX 로그인 password / RDS password / KIS app key / KIS app secret / 계좌번호 / token / RDS endpoint hostname / account-id / 실제 ARN / image digest / IAM access key id / instance-id) 신규 기록 없음. 모두 `[REDACTED]` 또는 placeholder. KRX 로그인 ID / password 는 "Secrets Manager 에서 주입" 으로만 표기. password rotate 는 본 일자 작업 범위 밖.
- 다음 작업: SSM RunCommand 기반 EC2 worker 무인 실행, EventBridge Scheduler / Step Functions hybrid orchestration, CloudWatch Logs Agent 연동, wrapper 내 DB 검증 출력 자동 추가, ECS / Fargate non-GUI crawler 범위 재정리, EC2 worker stop 절차 명시 — 모두 후속 분리.

## 2026-06-10 (afternoon ~ evening session)

- 08-interest-crawler-and-preprocessor-ecs 기본 포팅 1차 진행. 운영자가 직접 ECR repository 2개(`portfolio-interest-crawler`, `portfolio-interest-preprocessor`) 생성, port-interest-preprocessor / port-interest-crawler 의 Dockerfile + requirements.txt 신규 생성(preprocessor: `python:3.13-slim` + `pre_daily.py`. crawler: `python:3.13-slim` + Chromium / chromedriver + `interest_crawler_daily.py`), 로컬 빌드(`paper-20260610` / `paper-latest`), ECR push 완료. crawler 의 Selenium 의존성은 빌드 단계에서 1차 해소.
- 운영자가 ECS Cluster `portfolio-paper-cluster`, Task Execution Role `portfolio-paper-ecs-task-execution-role`(managed `AmazonECSTaskExecutionRolePolicy` + preprocessor DB secret read inline / 단일 secret ARN 한정), Task Role 2종(`portfolio-paper-preprocessor-task-role` / `portfolio-paper-crawler-task-role`), CloudWatch Log Group 2개(`/portfolio/paper/preprocessor` / `/portfolio/paper/crawler`, retention 14일) 준비 완료. preprocessor task SG → RDS PostgreSQL SG 5432 inbound 허용 확인.
- 운영자가 `/portfolio/paper/rds/preprocessor-app` JSON multi-key secret 신규 생성, preprocessor Task Definition 등록(family `portfolio-paper-interest-preprocessor`, revision 1, awsvpc, cpu 512 / memory 1024, image `paper-20260610`, ECS `secrets` env 주입). preprocessor RunTask 1차 검증 결과 lastStatus `STOPPED` / exitCode `0` / `PREPROCESSOR PIPELINE END` 확인.
- 검증 도중 1차 실패는 Secrets Manager JSON `host` key 누락(psycopg2 가 Unix socket `/var/run/postgresql/.s.PGSQL.5432` 시도) → secret 재생성으로 해소. 2차 실패는 `public` schema 잔존 sequence 2건(`pre_marketbreadth_daily_feature_id_seq`, `pre_macroeconomic_daily_feature_id_seq`) 의 `preprocessor_app` USAGE / SELECT 권한 부족 → 운영자가 직접 GRANT 후 해소. 두 사례 모두 운영자가 직접 SQL 실행, Kiro 는 결과 / 사유 / 조치를 문서로만 정리.
- crawler 는 Dockerfile / requirements / 빌드 / push 까지 완료. Task Definition 등록 / RunTask runtime / KRX·Naver·yfinance outbound 도달 검증은 이월. 안정화 100% 는 본 spec 범위 밖.
- Kiro 작업 산출물: `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md` 갱신, `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` 신규 생성, `.kiro/specs/_common/risk-register.md` 보강(R-DATA-005 / R-DOCS-001) + 신규(R-DATA-006 / R-AUTO-005), `.kiro/specs/_common/followups-overview.md` 2026-06-10 후속 메모 + 08 spec 1차 적용 결과 보강, `.kiro/specs/_common/operator-decisions.md` Change Log OD-NET-004 메모 추가, `.kiro/CHANGELOG.md` 2026-06-10 섹션 추가, 본 파일.
- 실제 AWS / ECR / ECS / IAM / Secrets / RDS 작업은 운영자가 직접 수행. Kiro 는 문서 / 절차 / 검증 항목 정리만 수행. 8개 MS 의 README / AGENTS.md / CHANGELOG / docs / worklog 본 spec 작업으로 인한 변경 0건(운영자 직접 작업으로 port-interest-preprocessor / port-interest-crawler Dockerfile · requirements.txt 신규 생성 사실은 운영 노트에만 기록).
- 민감정보(secret value / password / KIS app key / KIS app secret / 계좌번호 / token / RDS endpoint hostname / account-id / 실제 ARN / image digest / IAM access key id) 신규 기록 없음. 모두 `[REDACTED]` 또는 placeholder. password rotate 는 본 일자 문서 작업 범위에 포함하지 않음.
- 다음 작업: crawler Task Definition 등록 / RunTask runtime 검증 / KRX·Naver·yfinance outbound runtime 도달 검증, `public` schema 잔존 sequence 추가 점검, 08 spec runbook / validation-checklist 작성 시점 결정.

## 2026-06-09 (afternoon ~ evening session)

- 2026-06-09 Daily 실행 완료(현황 점검만, 코드 / 자동 매매 변경 없음).
- `.kiro` 문서 구조 정리. 각 문서의 목적 / 자동 수정 가능 여부 / 수기 수정 필요성 / 유지 필요성을 일관 정리.
  - 후속 작업: `_common/operator-decisions.md` 파일명을 잘못 부르던 일부 텍스트(`operator-dicisions.md` 오타 등)를 발견 시 정정 예정.
- Local PostgreSQL `portfolio` DB pg_dump 백업 완료. 파일 `portfolio_full_20260609.dump`(422,334,494 bytes), 보관 위치 `C:\Workspaces\db-backup\portfolio_20260609\` (로컬). 기준선으로 schema별 table count, table별 row count snapshot, 주요 object count(index / trigger / sequence / FK)를 확보. 기준선 총 table 수 81개 확인.
- 로컬 PC에서 RDS 직접 접속 시 timeout 확인. RDS endpoint가 private IP로 resolve되고 Publicly accessible = No 구조이므로 기대 동작으로 판단. RDS restore runner는 MarketConnector EC2로 결정.
- aws-paper용 MarketConnector EC2 생성 완료. Amazon Linux 2023, public subnet, EIP attach, IAM Role(SSM managed policy + S3 임시 migration bucket read), EC2 Instance Connect 접속 성공. 로컬 SSH 직접 접속은 outbound 22 제한 가능성으로 보류. EC2 OS 업데이트 + AWS CLI 기본 제공 확인. PostgreSQL client 15.18로 시작했다가 dump archive header 불일치 확인 후 18.4 client로 전환(psql 18.4 / pg_restore 18.4). EC2에서 private RDS 접속 성공.
- RDS Restore: 로컬 dump → S3 임시 bucket → MarketConnector EC2 전송. 로컬 / S3 / EC2 dump 파일 크기 422,334,494 bytes 일치. pg_restore --list TOC 812 entries / line count 823 / dump source PostgreSQL 18.1 / format CUSTOM + gzip 확인. 기존 RDS engine PostgreSQL 16.14에 18.1 dump를 restore하는 것은 하위 major version restore라 위험 판단 → 기존 RDS 삭제 후 PostgreSQL 18.4 기준으로 재생성(Public access No, initial DB `portfolio`). 1차 restore에서 `role "postgres" does not exist` 오류 발생, 원인은 dump owner role과 RDS role 불일치. `portfolio` DB drop/recreate 후 `--no-owner --no-privileges`로 재실행하여 에러 없이 완료. RDS 객체 owner는 restore 실행 계정 기준으로 정리.
- 정합성 검증: schema별 table count 81개 일치, table / index / sequence / FK(33) / trigger(23) / table별 row count CSV 82줄 모두 로컬 기준선과 diff 0. CRLF / LF 차이는 `--strip-trailing-cr` 정규화 후 비교 완료.
- DB Role / 권한 분리 1차 적용 완료. `db-roles-and-grants.md` §4 SQL을 운영자가 직접 실행. `portfolio_owner` NOLOGIN 생성 → `portfolio_admin`에 `portfolio_owner` 멤버십 부여 → 9개 도메인 schema(reference / interest / preprocessor / research / decision / execution / connector / ops / legacy) owner를 `portfolio_owner`로 이관(public은 변경 안 함). 기존 table / sequence / index owner는 `portfolio_admin`으로 잔존, `REASSIGN OWNED BY portfolio_admin TO portfolio_owner`는 1차 적용에서 미실행 결정.
- 7개 app role 생성 완료(`marketconnector_app`, `view_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `research_app`, `execution_app`). 모두 LOGIN true, SUPERUSER / CREATEDB / CREATEROLE / REPLICATION / BYPASSRLS false. GRANT 매트릭스(legacy 미부여, marketconnector_app은 connector R/W + execution R-only, view_app은 ops R/W + execution R-only) + DEFAULT PRIVILEGES + role별 search_path 7건 적용 완료. 모든 app role의 legacy USAGE = false 확인. sequence 권한 요약 정상(connector 9 / decision 3 / execution 5 / interest 14 / ops 2 / preprocessor 15 / public 2 / reference 3 / research 19).
- App role 접속 테스트: marketconnector_app(connector_order_request 33 / strategy_execution_order 17 read 성공, execution write 차단, legacy USAGE false), execution_app(execution / decision / connector read + execution write 성공, legacy USAGE false), view_app(execution / connector / decision read 성공, execution write 차단, legacy USAGE false) 모두 기대 동작과 일치.
- 본 세션은 문서 + DB 안 SQL 실행만 수행. AWS 리소스는 EC2 신규 생성과 기존 RDS 삭제/재생성을 운영자가 직접 진행했고 Kiro는 결과만 기록. 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음. 민감정보(password / secret value / endpoint hostname / account-id / 계좌번호 / token / app key / app secret / webhook URL) 신규 기록 없음(`[REDACTED]` 또는 placeholder만 사용).
- 다음 작업(2026-06-10 예정): MarketConnector 기본 포팅(EC2 Python venv / requirements / 소스 배치 / KIS paper 계좌·RDS 접속 외부화 / `connector_balance.py`·`connector_order_check.py` 검증), 03 spec runbook / validation-checklist / operation-notes 반영, 통합 검증·rollback 절차 문서 반영. 원래 계획이던 ECS 기본 포팅도 진행 예정이나, 오늘 이월된 MarketConnector 기본 포팅을 우선한다.

## 2026-06-09

- `.kiro/specs` 루트의 공통 문서 6종(`operator-decisions.md`, `ms-aws-service-decision-matrix.md`, `cost-simulation.md`, `followups-overview.md`, `aws-resource-glossary.md`, `risk-register.md`)을 `_common/` 폴더로 이동했다.
- `note-aws-landscape-2021-vs-2026.md`는 `_archive/` 폴더로 이동했다. 파일명과 본문은 유지했다.
- `02-aws-network-and-rds/README.md`를 운영자용 한 장 요약 문서로 신규 추가했다(현재 상태 / 다음 작업 / 완료 리소스 / 관련 문서 링크 / 자동 갱신 기준 5개 섹션).
- `.kiro/README.md` 폴더 구조와 `.kiro/AGENTS.md` 루트 참조 문서 섹션을 새 위치(`_common/`, `_archive/`) 기준으로 갱신했다.
- 01 / 02 spec 폴더의 모든 `.md`와 `.kiro/docs/kiro-readonly-validator-iam.md`, 루트 메타 문서(README / CHANGELOG / WORKLOG)의 상대 링크를 새 경로로 일괄 보정했다. 본문 내용은 링크 경로 외 변경 없음.
- 실제 AWS 리소스 생성 / 변경 / 삭제 없음.
- 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음.
- 민감정보(secret / token / password / app key / app secret / 계좌번호 / webhook URL / RDS endpoint hostname / account-id / access key id) 신규 기록 없음.

## 2026-06-06

- 02 spec 1차 적용을 위해 운영자가 AWS Console에서 단계별로 따라 할 수 있는 보조 문서 4종을 추가했다.
- `.kiro/specs/_common/risk-register.md`를 루트 공통 문서로 신규 생성하고 R-NET / R-SEC / R-DATA / R-BROKER / R-AUTO / R-DOCS / R-COST 카테고리에 12개 리스크를 등록했다. 동일 날짜 후속 작업으로 R-SEC-002(Root 자격 / MFA 분실), R-SEC-003(portadmin 자격 분실) 2개를 누적 추가했다.
- `.kiro/specs/01-aws-migration-foundation/traceability-matrix.md`를 추가해 Requirement → Design → Task → Decision 매핑을 한눈에 보이도록 정리했다.
- `.kiro/specs/02-aws-network-and-rds/runbook.md`를 1차로 18 Step(VPC → Subnet → IGW → Route Table → NAT 미사용 확인 → SG → VPC Endpoint → RDS → DB 준비 → cutover 사전 준비 → 검증 → rollback) 실행 절차서로 작성했다. 이후 같은 날 모든 Step 제목에 한글 실행 구분 라벨(`[실행]` / `[확인]` / `[준비]` / `[복구]`)을 추가하고 본문 상단에 라벨 범례 섹션을 신설했다.
- 같은 runbook에 신규 Step 0 `IAM 관리자 사용자 portadmin 생성`을 추가하면서 기존 Step 0 ~ 18을 Step 1 ~ 19로 재번호하고 본문 안의 cross-reference를 모두 +1로 일괄 갱신했다. 신규 Step 0은 portadmin 사용자 생성 → AdministratorAccess 정책 부여 → 콘솔 sign-in URL / account alias → MFA 활성 → Root 로그아웃 후 portadmin 로그인 → Root 보안 강화 6개 sub-step으로 구성했다.
- runbook Step 재번호에 맞춰 `validation-checklist.md`(`Step 18 rollback` → `Step 19 rollback`)와 `traceability-matrix.md`의 매핑 표 / Acceptance Criteria 보강 / Risk 매핑 안의 runbook Step 참조 숫자를 모두 +1로 갱신했다.
- `.kiro/specs/02-aws-network-and-rds/validation-checklist.md`를 10개 섹션 체크박스로 작성해 runbook 통과 여부를 일관되게 점검하도록 했다.
- `.kiro/specs/02-aws-network-and-rds/traceability-matrix.md`로 02 spec Requirement → Design → Task → Validation → Decision 매핑과 Risk 매핑을 정리했다.
- `.kiro/specs/_common/aws-resource-glossary.md`에 IAM User 용어 항목을 신규 추가했다. portadmin 같은 IAM 관리자 사용자 개념이 02 runbook에서 본격적으로 사용되기 시작한 것을 반영했다.
- `.kiro/README.md`, `.kiro/CHANGELOG.md`도 신규 문서 안내와 변경 이력에 맞춰 갱신했다.
- 결정값(Decision ID, 선택값, 비용 영향, 운영 리스크, 후속 spec 영향)과 Console 클릭 경로 / 입력값 / 검증 항목 / rollback 순서는 변경하지 않았다.
- 실제 AWS 리소스 생성 없음.
- 8개 MS의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음.
- 민감정보(secret / token / password / app key / app secret / 계좌번호 / webhook URL / portadmin 비밀번호 / Root·portadmin MFA 시리얼 / 백업 코드) 기록 없음(`[REDACTED]` 또는 placeholder만 사용).

## 2026-06-08

- 운영자가 AWS Console에서 02 spec Foundation(VPC / Subnet / IGW / Route Table / SG 8종 / VPC Endpoint 6종 / RDS subnet group / parameter group / `portfolio-paper-rds` / Secrets Manager `/portfolio/paper/rds/master`)을 직접 구축 완료. Kiro는 ReadOnly 검증과 문서화만 수행.
- `.kiro/docs/kiro-readonly-validator-iam.md` 신규 생성. ReadOnly 전용 IAM 사용자 `portfolio-kiro-readonly-validator` / 정책 `PortfolioKiroReadOnlyValidatorPolicy` 설계, Allow / Deny 정책 JSON, Console 절차, AWS CLI 절차, 검증 명령 모음, 자동 검증 가능 / 수동 확인 분류 정리. `secretsmanager:GetSecretValue`와 KMS Decrypt는 명시 Deny.
- `.kiro/specs/02-aws-network-and-rds/operation-notes.md` 신규 생성. AWS Foundation 실행 기록(SG 이름 prefix `sgroup-` 결정, RDS 생성 결과, KMS default 등)과 후속 검증 세션 누적 기록 추가.
- `.kiro/specs/02-aws-network-and-rds/validation-checklist.md` 상태 라벨 시스템 신규 도입. 기존 체크박스(`- [ ]`)를 4종 라벨(`[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]`)로 전환하고 AWS ReadOnly 자동 검증 + 문서 비교 + grep 결과를 반영. 최종 카운트 [O] 78 / [X] 0 / [Kiro 후속] 2 / [운영자] 20.
- AWS ReadOnly 자동 검증으로 VPC / Subnet 6개 / IGW / NAT 미생성 / Route Table / SG 8종 / VPC Endpoint 6종 / RDS instance / Secrets metadata / ALB·ELB 0건 / Cost Anomaly Detection 0건 모두 기대값 일치 확인.
- secret value 조회 없음(`DescribeSecret` metadata만 사용). KMS Decrypt 호출 없음. AWS 리소스 생성 / 수정 / 삭제 없음.
- 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음(`git status --short` 8개 워크스페이스 모두 무변경 확인).
- 민감정보(secret / password / access key / token / webhook URL / 계좌번호) 기록 없음. `.kiro/**/*.md` grep 결과 평문 패턴 0건.

## 2026-06-05

- `.kiro/README.md`, `.kiro/CHANGELOG.md`, `.kiro/WORKLOG.md` 생성 작업을 진행했다.
- `.kiro` 작업공간이 8개 MS 전체 AWS Migration spec을 관리하는 공간임을 README에 문서화했다.
- 루트 공통 문서(`operator-decisions.md`, `ms-aws-service-decision-matrix.md`, `cost-simulation.md`, `followups-overview.md`, `aws-resource-glossary.md`, `note-aws-landscape-2021-vs-2026.md`)의 역할과 source of truth 기준을 README에 정리했다.
- Kiro 작업 로그는 날짜별 파일로 나누지 않고 `.kiro/WORKLOG.md` 하나에 간단히 누적하기로 했다.
- `.kiro/AGENTS.md`에 README / CHANGELOG / WORKLOG 관리 규칙 섹션을 추가했다. 기존 작업 규칙의 의미는 변경하지 않았다.
- 같은 날 진행한 `.kiro/specs/_common/operator-decisions.md` 구조 재정리(Status 한글 라벨, At a Glance, Open/Tentative/Deferred 모음 추가)는 결정값을 변경하지 않은 가독성 개선이며, 자세한 변경 항목은 `.kiro/CHANGELOG.md` 2026-06-05 항목에 기록했다.
- 실제 AWS 리소스 생성 없음.
- 애플리케이션 소스 코드 수정 없음.
- 8개 MS 문서(README / CHANGELOG / worklog / AGENTS.md / 소스) 수정 없음.
- 민감정보 기록 없음.
