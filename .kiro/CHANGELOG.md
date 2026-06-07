# Kiro AWS Migration Changelog

본 문서는 `.kiro` 작업공간 안의 AWS Migration spec 문서 변경 이력만 간단히 기록한다.

## 작성 원칙

- spec 구조 변경, 루트 공통 문서 변경, 신규 spec 생성, 주요 문서 재구성만 기록한다.
- 8개 MS의 세부 코드 변경 이력은 본 문서에 기록하지 않는다.
- 너무 자세한 일일 작업 로그는 `.kiro/WORKLOG.md`에 남기고, 본 문서에는 의미 있는 변경만 짧게 정리한다.
- 항목 분류는 `Added`, `Changed`, `Removed`, `Security`로 통일한다.
- 날짜는 한국 기준의 작업 일자를 사용한다.

## 2026-06-06

### Added

- `.kiro/specs/risk-register.md` — AWS Migration 운영 / 보안 / 비용 리스크 단일 누적 표. R-NET-001 ~ R-COST-002 12개 리스크 등록. 동일 날짜 후속 작업으로 R-SEC-002(Root 보안), R-SEC-003(portadmin 자격 분실) 2개 항목을 추가 누적했다.
- `.kiro/specs/01-aws-migration-foundation/traceability-matrix.md` — 01 spec Requirement → Design → Task → Decision 매핑.
- `.kiro/specs/02-aws-network-and-rds/runbook.md` — 운영자가 AWS Console에서 단계별로 따라 할 수 있는 실행 절차서. 1차 작성은 18 Step(Region ~ Rollback)이었고, 동일 날짜 후속 작업으로 신규 Step 0 `IAM 관리자 사용자 portadmin 생성`을 추가하면서 전체가 19 Step(Step 0 ~ Step 19)으로 재구성됐다.
- `.kiro/specs/02-aws-network-and-rds/validation-checklist.md` — 02 spec runbook 통과 여부 점검 체크박스(10개 섹션, Pre-flight / Network / SG / Endpoint / RDS / DB / Cutover / Cost / Docs / Rollback).
- `.kiro/specs/02-aws-network-and-rds/traceability-matrix.md` — 02 spec Requirement → Design → Task → Validation → Decision 매핑 + Risk와의 매핑.
- `.kiro/specs/aws-resource-glossary.md` — IAM User 용어 항목 신규 추가. portadmin 같은 IAM 관리자 사용자 운영 개념이 02 spec runbook에서 처음 본격적으로 사용되기 시작한 것을 반영했다.

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
- `.kiro/specs/operator-decisions.md`의 구조를 운영자 가독성 중심으로 재정리했다. 실제 결정값(Decision ID, 선택지, 선택값, 비용 영향, 운영 리스크, 후속 spec 영향)은 변경하지 않았고, Status 표시만 한글/색상 라벨(🟢 확정 / 🟡 잠정 / 🔴 미정 / 🔵 보류)을 함께 사용하도록 개선했다. Status Legend, Decision Summary, At a Glance, 카테고리별 상세 결정표, Open / Tentative / Deferred 결정 모음, Decision Update Rules, Change Log 섹션을 추가했다.

### Security

- secret, token, password, app key, app secret, 계좌번호, webhook URL은 본 작업공간 문서에 절대 기록하지 않고 `[REDACTED]`로만 표기한다는 원칙을 재확인했다.
