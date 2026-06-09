# Kiro AWS Migration Changelog

본 문서는 `.kiro` 작업공간 안의 AWS Migration spec 문서 변경 이력만 간단히 기록한다.

## 작성 원칙

- spec 구조 변경, 루트 공통 문서 변경, 신규 spec 생성, 주요 문서 재구성만 기록한다.
- 8개 MS의 세부 코드 변경 이력은 본 문서에 기록하지 않는다.
- 너무 자세한 일일 작업 로그는 `.kiro/WORKLOG.md`에 남기고, 본 문서에는 의미 있는 변경만 짧게 정리한다.
- 항목 분류는 `Added`, `Changed`, `Removed`, `Security`로 통일한다.
- 날짜는 한국 기준의 작업 일자를 사용한다.

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
