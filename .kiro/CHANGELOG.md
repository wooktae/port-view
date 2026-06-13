# Kiro AWS Migration Changelog

본 문서는 `.kiro` 작업공간 안의 AWS Migration spec 문서 변경 이력만 간단히 기록한다.

## 작성 원칙

- spec 구조 변경, 루트 공통 문서 변경, 신규 spec 생성, 주요 문서 재구성만 기록한다.
- 8개 MS의 세부 코드 변경 이력은 본 문서에 기록하지 않는다.
- 너무 자세한 일일 작업 로그는 `.kiro/WORKLOG.md`에 남기고, 본 문서에는 의미 있는 변경만 짧게 정리한다.
- 항목 분류는 `Added`, `Changed`, `Removed`, `Security`로 통일한다.
- 날짜는 한국 기준의 작업 일자를 사용한다.

## 2026-06-12

### Changed

- `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` — 2026-06-12 Windows EC2 worker 기반 KRX GUI 의존 수집 1차 검증 결과 섹션(§1 ~ §11) 추가. EC2 worker 환경 확인 / RDS Secret(`/portfolio/paper/rds/crawler-app`) DB env 주입 / 다운로드 경로 junction 조치 / KRX program 단독 수집 성공(2026-06-09 / 2026-06-10 / 2026-06-11 각 1건, `interest_program_raw` 543 → 546) / KRX shortsell 단독 수집 성공(각 349건, `interest_shortsell_raw` 189158 → 190205) / 한글 literal·인코딩 검증(Python patch script 방식 결정) / KRX Secrets Manager 연동(`/portfolio/paper/krx/crawler-login`) + IAM 권한 추가 / EC2 worker daily wrapper(`run_krx_worker_daily.ps1`) 1차 실행 + 재실행 idempotent no-op / Hybrid execution model 분류 결정(KRX GUI = Windows EC2 worker, non-GUI = ECS Fargate Task 후보 유지, preprocessor = ECS Fargate Task 유지) / 후속 인계 8건 기록. 실제 secret value / KRX 로그인 password / RDS endpoint hostname / account-id / 실제 ARN / instance-id / image digest 평문 기록 0건. KRX 로그인 ID / password 는 "Secrets Manager 에서 주입" 으로만 표기.
- `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md` — 2026-06-12 결과 반영. crawler runtime 관련 task 29 / 30 / 31 / 32 갱신(KRX GUI 경로는 Windows EC2 worker 분리 확정 / non-GUI 경로 runtime 검증은 부분 완료 또는 보류 / workload 재분류). §11 Windows EC2 worker 기반 KRX GUI 의존 수집 task 44 ~ 52 신규 추가(EC2 worker 점검 / DB env 주입 / 다운로드 경로 junction / KRX program 수집 / KRX shortsell 수집 / 한글 인코딩 검증 / KRX Secrets Manager 연동 / wrapper 1차 실행 / Hybrid execution model 분류 결정). §12 후속 task 53 ~ 60 신규 추가(SSM RunCommand / EventBridge Scheduler / Step Functions hybrid orchestration / CloudWatch Logs Agent / wrapper 내 DB 검증 출력 자동 추가 / non-GUI crawler 범위 재정리 / EC2 worker stop 절차 / KRX 수집 headless 리팩토링 장기 후보). Task Dependency Graph 와 이월 항목 요약(2026-06-10 / 2026-06-12) 갱신.
- `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/design.md` — §12 "2026-06-12 운영자 검증 결과 / Hybrid execution model" 섹션 신규 추가. 기존 §1 ~ §11 결정값은 변경하지 않고 §12.1(Hybrid execution model 분류 표) / §12.2(KRX GUI 경로가 EC2 worker 로 분리된 이유 — Chrome GUI / download / login session / debug attach / KRX 사이트 특성 / Fargate GUI 미지원) / §12.3(Secrets Manager 연동 방식 — RDS preprocessor-app / RDS crawler-app / KRX crawler-login, secret value 평문 기록 금지) / §12.4(EC2 worker 운영 모드 분리 — wrapper 기반 수동 실행 1차 운영 가능 / 자동화 후속) / §12.5(본 섹션 갱신 원칙) 만 보강.
- `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/requirements.md` — Requirement 8 에 acceptance criterion 5 추가. KRX GUI 의존 수집(KRX program / KRX shortsell) 이 ECS Fargate Task 의 GUI / Chrome download / OTP 세션 흐름과 호환되지 않는 경우 Windows EC2 worker 위에서 실행될 수 있음과 non-GUI crawler / KRX GUI 의존 crawler 의 runtime 분리(hybrid execution model) 명시. 기존 R1 ~ R11 본문은 변경하지 않음.
- `.kiro/specs/_common/operator-decisions.md` — 신규 결정 OD-MS-011 / OD-MS-012 / OD-SEC-008 추가. OD-MS-011(port-interest-crawler runtime = Hybrid: KRX GUI = Windows EC2 worker, non-GUI = ECS Fargate Task 후보 유지, preprocessor = ECS Fargate Task 유지, 🟡 잠정), OD-MS-012(KRX GUI 의존 crawler 1차 운영 모드 = wrapper 기반 수동 실행, 🟡 잠정), OD-SEC-008(KRX 로그인 자격 = Secrets Manager `/portfolio/{env}/krx/crawler-login` JSON `username` / `password`, 🟡 잠정). Decision Summary 카운트(전체 62 → 65 / 🟡 잠정 17 → 20) / Tentative Decisions 모음 / Change Log 2026-06-12 항목 갱신. 기존 결정값(OD-NET-004 등) 본문 변경 없음.
- `.kiro/specs/_common/risk-register.md` — R-AUTO-005 mitigation / detection / rollback 보강(2026-06-12 KRX GUI 경로 EC2 worker 1차 운영 가능 상태 도달 / wrapper 로그 패턴 모니터 / wrapper 재실행 즉시 복구) + Status `Open` → `Mitigated` 갱신. 신규 리스크 4건 추가: R-AUTO-006(EC2 worker 다운로드 경로 불일치, junction 조치, Mitigated), R-SEC-005(KRX 로그인 secret read 권한 누락 시 worker 기동 실패, IAM Role inline policy + Resource·Action wildcard 0건, Mitigated), R-DOCS-002(EC2 worker 안 password 평문 노출 위험, length 만 점검 + grep 점검, Mitigated), R-AUTO-007(wrapper 성공이 실제 DB 적재 성공 보장 못함, wrapper 내 DB 검증 출력 자동 추가는 후속, Open).
- `.kiro/specs/_common/followups-overview.md` — 2026-06-12 후속 메모 추가. 08 spec Hybrid execution model 1차 운영 가능 상태 도달(KRX program / KRX shortsell EC2 worker 수집 성공 + 다운로드 경로 junction + RDS / KRX Secret 주입 + wrapper) 반영. KRX GUI = Windows EC2 worker / non-GUI = ECS Fargate Task 후보 유지 / preprocessor = ECS Fargate Task 유지 분류 결정 명시. 후속 분리 8건(SSM RunCommand / EventBridge Scheduler / Step Functions hybrid orchestration / CloudWatch Logs Agent / wrapper 내 DB 검증 출력 자동 추가 / non-GUI crawler 범위 재정리 / EC2 worker stop 절차 / KRX 수집 headless 리팩토링 장기 후보) 추가. 진행 순서표는 변경하지 않음.

### Security

- 본 변경에서도 secret value / token / password / KRX 로그인 password / KIS app key / KIS app secret / 계좌번호 / RDS endpoint hostname / account-id / 실제 ARN / image digest / IAM access key id / instance-id 평문 기록 0건. 모든 placeholder 는 `[REDACTED]` 또는 `<account-id>` / `<region>` / `<rds-endpoint>` / `<image-tag>` / `<image-digest>` / `<task-arn>` 만 사용. KRX 로그인 ID / password 는 "Secrets Manager 에서 주입" 으로만 기록. password rotate 는 본 일자 작업 범위 밖. 운영자가 EC2 내부에 직접 생성한 ps1 / py 테스트 / wrapper 파일은 사실만 operation-notes 에 기록하고 본문 전체 인용 0건.

## 2026-06-10

### Added

- `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` — 2026-06-10 운영자 실행 결과 누적 기록 신규 생성. ECR repository 2개 생성, port-interest-preprocessor / port-interest-crawler Dockerfile 및 requirements.txt 신규 생성, 로컬 빌드(`paper-20260610` / `paper-latest`), ECR push, ECS Cluster `portfolio-paper-cluster` / Task Execution Role / Task Role 2종 / CloudWatch Log Group 2종(retention 14일), Secrets Manager `/portfolio/paper/rds/preprocessor-app` JSON multi-key 신규 생성, preprocessor Task Definition(family `portfolio-paper-interest-preprocessor`, awsvpc, cpu 512 / memory 1024, image `paper-20260610`), preprocessor RunTask 1차 검증(lastStatus `STOPPED` / exitCode `0` / `PREPROCESSOR PIPELINE END`) 결과 기록. 1차 실패(Secrets JSON `host` 누락 → Unix socket 시도)와 2차 실패(public schema 잔존 sequence 2건 권한 부족) 사례 / 조치 / 결과를 짧게 기록. crawler runtime 검증 이월 명시. 실제 secret value / endpoint / account-id / 실제 ARN / image digest 평문 기록 0건.

### Changed

- `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md` — 2026-06-10 운영자 실행 결과 반영. 43개 task 중 ECR repository 생성 / Dockerfile · requirements 신규 생성 / 로컬 빌드 / ECR push / ECS Cluster · Role · Log Group / Secrets Manager / Task Definition / Preprocessor RunTask / CloudWatch Logs / exit code 0 항목을 체크 완료로 갱신. crawler 항목 중 build / push 는 체크 완료, runtime outbound 검증(task 30) 과 crawler runtime 도달 검증(task 31) 은 이월 표기. 실패 원인 분리 결과(Secrets JSON `host` 누락, public schema 잔존 sequence 권한 부족 2건) 를 task 28 / task 26 에 짧게 반영. 본 spec 안전 제약(35 ~ 40) 은 Kiro 본 spec 작업 기준 0건 유지 + 운영자 직접 작업은 operation-notes 참조 형태로 노트 추가.
- `.kiro/specs/_common/risk-register.md` — 2026-06-10 08 진행 결과 반영. R-DATA-005 mitigation / detection 보강(public schema 잔존 sequence 권한 누락 사례 + ECS Task event `permission denied for sequence` 패턴 모니터). R-DOCS-001 detection 보강(secret value 작업 채팅 / 명령 출력 / 콘솔 캡처 / CloudWatch Logs 비노출 운영 점검). 신규 R-DATA-006(Secrets Manager JSON multi-key 의 `host` key 누락으로 ECS Task 가 Unix socket `/var/run/postgresql/.s.PGSQL.5432` 시도) 추가. 신규 R-AUTO-005(crawler Selenium / Chromium / chromedriver runtime 또는 KRX / Naver / yfinance outbound 도달 미검증 상태로 운영 진입) 추가. 두 신규 항목 모두 password rotate 를 mitigation 으로 강제하지 않으며 secret value 비노출 / runtime 검증 후속 분리 원칙으로 정리.
- `.kiro/specs/_common/followups-overview.md` — 2026-06-10 후속 메모 보강. 08 preprocessor ECS Task 1회 실행 성공(lastStatus `STOPPED` / exitCode `0`) 반영. 08 spec 섹션 안에 2026-06-10 1차 적용 결과(완료 범위 / 범위 밖 / 후속 spec 인계) 추가. crawler Task Definition / RunTask runtime 검증, public schema 잔존 sequence 정리는 이월 항목으로 명시.
- `.kiro/specs/_common/operator-decisions.md` — Change Log 에 OD-NET-004(crawler / preprocessor outbound 방식 = public subnet + assignPublicIp) 가 2026-06-10 preprocessor RunTask 로 1차 검증되었다는 메모 1건 추가. Status 는 🟡 잠정 유지(crawler runtime 검증은 이월). 결정값(선택지 / 선택값 / 비용 영향 / 운영 리스크 / 후속 spec 영향) 변경 없음.

### Security

- 본 변경에서도 secret / token / password / KIS app key / KIS app secret / 계좌번호 / webhook URL / RDS endpoint hostname / account-id / 실제 ARN / image digest / IAM access key id 평문 기록 0건. 모든 placeholder 는 `[REDACTED]` 또는 `<account-id>` / `<region>` / `<rds-endpoint>` / `<image-tag>` / `<image-digest>` / `<task-arn>` 만 사용. password rotate 는 본 일자 문서 작업 범위에 포함하지 않음. secret value 가 작업 채팅 / 명령 출력 / 콘솔 캡처 / CloudWatch Logs 본문에 평문 노출되지 않도록 후속 작업에서도 동일 원칙 유지(R-DOCS-001 정합).

## 2026-06-09

### Added

- `.kiro/specs/02-aws-network-and-rds/README.md` — 운영자용 한 장 요약 문서. 현재 상태 / 다음 작업 / 완료된 AWS 리소스 / 관련 문서 링크 / 자동 갱신 기준을 짧게 정리한다. 결정값과 RDS endpoint hostname / secret value 같은 식별자는 기록하지 않는다.
- `.kiro/specs/02-aws-network-and-rds/db-roles-and-grants.md` — DB Role / 권한 분리 보조 문서. 7개 app role 설계, 권한 매트릭스, search_path 전략, GRANT / ALTER ROLE / DEFAULT PRIVILEGES SQL 초안, 검증 SQL, rollback 절차, operation-notes / validation-checklist 반영용 문구를 한 장에 정리. 결정 갱신 사항(legacy 미부여, marketconnector_app execution R-only 축소)을 02 design.md 매트릭스 대비 명시.

### Changed

- `.kiro/specs/02-aws-network-and-rds/operation-notes.md` — 2026-06-09 RDS Restore 결과(로컬 → S3 → EC2 → RDS 경유, dump 422,334,494 bytes / TOC 812, dump source PG 18.1, 기존 RDS PG 16.14에서 PG 18.4로 재생성, `role "postgres" does not exist` 1차 오류 후 `--no-owner --no-privileges`로 재실행, 정합성 diff 0)와 DB Role / 권한 분리 1차 적용 결과(7개 app role 생성, `portfolio_owner` 도입, 9개 도메인 schema owner 이관, 기존 table / sequence / index owner는 `portfolio_admin` 유지, `REASSIGN OWNED` 미실행, GRANT 매트릭스 / DEFAULT PRIVILEGES / search_path 적용, 3개 role 접속 검증 성공) 섹션 추가.
- `.kiro/specs/02-aws-network-and-rds/validation-checklist.md` — §6 DB / schema / role 준비 항목 5건 `[운영자 확인 필요]` → `[O]` 격상(DB / schema / role 존재, 권한 매트릭스, search_path 일치). §11 DB Role 권한 매트릭스 Validation 섹션 신규 추가(13개 라벨, [O] 11 / [운영자 확인 필요] 2). 추가로 RDS Restore 검증 항목(schema / table / index / sequence / FK / trigger / row count 일치)도 운영 노트와 일관되게 [O] 로 정리.
- `.kiro/specs/02-aws-network-and-rds/runbook.md` — 보강 섹션 "RDS Restore Runner & DB Role / 권한 적용 교훈"(major version mismatch 회피, private RDS는 EC2 / SSM 경유, dump role mismatch에는 `--no-owner --no-privileges`, schema-only owner 이관 + REASSIGN 분리 결정 등) 짧게 추가. 결정값 / 기존 Step 번호는 변경하지 않음.
- `.kiro/specs/_common/risk-register.md` — 신규 리스크 4건 추가: R-DATA-003(PostgreSQL major version mismatch로 restore 실패), R-DATA-004(dump owner role과 RDS role 불일치로 restore 실패), R-NET-004(private RDS 직접 접속 불가, EC2 / SSM restore runner 의존), R-DATA-005(app role 최소 권한 적용 후 view / execution / marketconnector 권한 경계 검증 필요).
- `.kiro/specs/_common/operator-decisions.md` — OD-DB-007 ~ OD-DB-010 추가: legacy schema 모든 app role 미부여 / marketconnector_app execution R-only / view_app execution R-only(write는 후속 spec 재검토) / 1차 적용에서 `REASSIGN OWNED BY portfolio_admin TO portfolio_owner` 미실행. OD-DB-005는 OD-DB-009로 분리되며 본문은 유지하고 Status는 그대로 둔다(중복 row 미생성).
- `.kiro/specs/_common/followups-overview.md` — 진행 순서에 2026-06-10 후속 메모를 추가하고, `03-marketconnector-ec2` 항목에 MarketConnector 기본 포팅 이월 작업과 ECS 기본 포팅 의존성을 명시. 03 spec 입력으로 사용할 RDS 1차 적용 결과를 짧게 링크.

코드 / AWS 리소스 / SQL 실행 변경 없음(EC2 신규 생성과 기존 RDS 삭제/재생성, SQL 적용은 운영자가 직접 진행했고 본 변경은 결과 기록만 다룬다). 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음.

(같은 날짜의 사전 변경)

- `.kiro/specs` 루트 공통 문서 6종(`operator-decisions.md`, `ms-aws-service-decision-matrix.md`, `cost-simulation.md`, `followups-overview.md`, `aws-resource-glossary.md`, `risk-register.md`)을 `.kiro/specs/_common/` 폴더로 이동했다. 파일명과 본문은 변경하지 않았다.
- `.kiro/specs/note-aws-landscape-2021-vs-2026.md`를 `.kiro/specs/_archive/` 폴더로 이동했다. 파일명과 본문은 변경하지 않았다.
- `.kiro/README.md` 폴더 구조 예시와 루트 공통 문서 링크 경로를 새 위치(`_common/`, `_archive/`)에 맞게 갱신했다.
- `.kiro/AGENTS.md`의 루트 참조 문서 섹션 경로 표기를 `_common/` / `_archive/` 기준으로 갱신했다. 작업 규칙의 의미는 변경하지 않았다.
- `.kiro/CHANGELOG.md`, `.kiro/WORKLOG.md`, `.kiro/docs/kiro-readonly-validator-iam.md`, `01-aws-migration-foundation/*.md`, `02-aws-network-and-rds/*.md`의 상대 링크를 새 폴더 구조에 맞게 보정했다(`../X.md` → `../_common/X.md`, `specs/X.md` → `specs/_common/X.md`).

### Security

- 본 변경에서도 secret / token / password / app key / app secret / 계좌번호 / webhook URL 값은 작성하지 않았다. 모든 placeholder는 `[REDACTED]`만 사용한다. RDS endpoint hostname / account-id / access key id는 새 README에 기록하지 않는다.

## 2026-06-06

### Added

- `.kiro/specs/_common/risk-register.md` — AWS Migration 운영 / 보안 / 비용 리스크 단일 누적 표. R-NET-001 ~ R-COST-002 12개 리스크 등록. 동일 날짜 후속 작업으로 R-SEC-002(Root 보안), R-SEC-003(portadmin 자격 분실) 2개 항목을 추가 누적했다.
- `.kiro/specs/01-aws-migration-foundation/traceability-matrix.md` — 01 spec Requirement → Design → Task → Decision 매핑.
- `.kiro/specs/02-aws-network-and-rds/runbook.md` — 운영자가 AWS Console에서 단계별로 따라 할 수 있는 실행 절차서. 1차 작성은 18 Step(Region ~ Rollback)이었고, 동일 날짜 후속 작업으로 신규 Step 0 `IAM 관리자 사용자 portadmin 생성`을 추가하면서 전체가 19 Step(Step 0 ~ Step 19)으로 재구성됐다.
- `.kiro/specs/02-aws-network-and-rds/validation-checklist.md` — 02 spec runbook 통과 여부 점검 체크박스(10개 섹션, Pre-flight / Network / SG / Endpoint / RDS / DB / Cutover / Cost / Docs / Rollback).
- `.kiro/specs/02-aws-network-and-rds/traceability-matrix.md` — 02 spec Requirement → Design → Task → Validation → Decision 매핑 + Risk와의 매핑.
- `.kiro/specs/_common/aws-resource-glossary.md` — IAM User 용어 항목 신규 추가. portadmin 같은 IAM 관리자 사용자 운영 개념이 02 spec runbook에서 처음 본격적으로 사용되기 시작한 것을 반영했다.

### Changed

- `.kiro/README.md` — 신규 보조 문서 역할(runbook / validation-checklist / traceability-matrix / risk-register) 안내 섹션과 폴더 구조 갱신.
- `.kiro/specs/02-aws-network-and-rds/runbook.md` — 모든 Step 제목에 한글 실행 구분 라벨(`[실행]` / `[확인]` / `[준비]` / `[복구]`)을 추가하고, 본문 상단에 라벨 범례 섹션을 신설했다. 이후 Step 0(IAM portadmin) 추가에 따라 기존 Step 0 ~ 18을 Step 1 ~ 19로 일괄 재번호하고 본문 안의 cross-reference(`Step N 완료`, `Step N에서 다시 결정` 등)도 모두 +1로 갱신했다. 결정값 / Console 클릭 경로 / 입력값 / 검증 항목 / rollback 순서는 변경하지 않았다.
- `.kiro/specs/02-aws-network-and-rds/validation-checklist.md` — runbook Step 재번호에 맞춰 `Step 18 rollback` 참조를 `Step 19 rollback`으로 갱신했다.
- `.kiro/specs/02-aws-network-and-rds/traceability-matrix.md` — runbook Step 재번호에 맞춰 매핑 표(Requirement, Acceptance Criteria 보강, Risk 매핑) 안의 모든 runbook Step 참조 숫자를 +1로 일괄 갱신했다.

### Security

- 본 변경에서도 secret / token / password / app key / app secret / 계좌번호 / webhook URL 값은 작성하지 않았다. 모든 placeholder는 `[REDACTED]`만 사용했다.
- portadmin 비밀번호, MFA 시리얼, 백업 코드, Root MFA 시리얼은 본 작업공간 어떤 문서에도 기록하지 않는다. 운영자가 외부의 안전한 위치에 별도 보관하며, 문서에는 `[REDACTED]`로만 표기한다.

## 2026-06-05

### Added

- `.kiro/README.md`를 추가하여 AWS Migration spec 작업공간의 목적과 문서 구조를 정리했다.
- `.kiro/CHANGELOG.md`를 추가하여 Kiro spec 문서 변경 이력 관리 기준을 만들었다.
- `.kiro/WORKLOG.md`를 추가하여 간단 작업 로그를 하나의 파일에 누적하는 방식으로 정리했다.

### Changed

- `.kiro/AGENTS.md`에 README / CHANGELOG / WORKLOG 관리 규칙을 추가했다. 단, 기존 작업 규칙(범위, 단일 기준 문서, MS별 AGENTS.md 참조, spec 작성, 보안, 실행 규칙)의 의미는 변경하지 않았다.
- `.kiro/specs/_common/operator-decisions.md`의 구조를 운영자 가독성 중심으로 재정리했다. 실제 결정값(Decision ID, 선택지, 선택값, 비용 영향, 운영 리스크, 후속 spec 영향)은 변경하지 않았고, Status 표시만 한글/색상 라벨(🟢 확정 / 🟡 잠정 / 🔴 미정 / 🔵 보류)을 함께 사용하도록 개선했다. Status Legend, Decision Summary, At a Glance, 카테고리별 상세 결정표, Open / Tentative / Deferred 결정 모음, Decision Update Rules, Change Log 섹션을 추가했다.

### Security

- secret, token, password, app key, app secret, 계좌번호, webhook URL은 본 작업공간 문서에 절대 기록하지 않고 `[REDACTED]`로만 표기한다는 원칙을 재확인했다.
