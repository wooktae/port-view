# Kiro AWS Migration Worklog

본 문서는 `.kiro` 작업공간의 간단 작업 로그다. 각 MS의 `docs/worklog/YYYY-MM-DD.md`처럼 상세하게 쓰지 않으며, 날짜별로 별도 파일을 만들지 않고 본 단일 파일에 누적한다.

## 작성 원칙

- 날짜별 섹션을 본 파일 안에 누적한다.
- 각 날짜 섹션은 5 ~ 10줄 정도로 간단히 기록한다.
- 상세 구현 로그, 긴 검증 로그, 코드 변경 세부사항은 기록하지 않는다.
- 실제 AWS 리소스 생성 여부, 애플리케이션 소스 코드 수정 여부, 8개 MS 문서 수정 여부, 민감정보 기록 여부는 짧게 남긴다.

## 2026-06-06

- 02 spec 1차 적용을 위해 운영자가 AWS Console에서 단계별로 따라 할 수 있는 보조 문서 4종을 추가했다.
- `.kiro/specs/risk-register.md`를 루트 공통 문서로 신규 생성하고 R-NET / R-SEC / R-DATA / R-BROKER / R-AUTO / R-DOCS / R-COST 카테고리에 12개 리스크를 등록했다. 동일 날짜 후속 작업으로 R-SEC-002(Root 자격 / MFA 분실), R-SEC-003(portadmin 자격 분실) 2개를 누적 추가했다.
- `.kiro/specs/01-aws-migration-foundation/traceability-matrix.md`를 추가해 Requirement → Design → Task → Decision 매핑을 한눈에 보이도록 정리했다.
- `.kiro/specs/02-aws-network-and-rds/runbook.md`를 1차로 18 Step(VPC → Subnet → IGW → Route Table → NAT 미사용 확인 → SG → VPC Endpoint → RDS → DB 준비 → cutover 사전 준비 → 검증 → rollback) 실행 절차서로 작성했다. 이후 같은 날 모든 Step 제목에 한글 실행 구분 라벨(`[실행]` / `[확인]` / `[준비]` / `[복구]`)을 추가하고 본문 상단에 라벨 범례 섹션을 신설했다.
- 같은 runbook에 신규 Step 0 `IAM 관리자 사용자 portadmin 생성`을 추가하면서 기존 Step 0 ~ 18을 Step 1 ~ 19로 재번호하고 본문 안의 cross-reference를 모두 +1로 일괄 갱신했다. 신규 Step 0은 portadmin 사용자 생성 → AdministratorAccess 정책 부여 → 콘솔 sign-in URL / account alias → MFA 활성 → Root 로그아웃 후 portadmin 로그인 → Root 보안 강화 6개 sub-step으로 구성했다.
- runbook Step 재번호에 맞춰 `validation-checklist.md`(`Step 18 rollback` → `Step 19 rollback`)와 `traceability-matrix.md`의 매핑 표 / Acceptance Criteria 보강 / Risk 매핑 안의 runbook Step 참조 숫자를 모두 +1로 갱신했다.
- `.kiro/specs/02-aws-network-and-rds/validation-checklist.md`를 10개 섹션 체크박스로 작성해 runbook 통과 여부를 일관되게 점검하도록 했다.
- `.kiro/specs/02-aws-network-and-rds/traceability-matrix.md`로 02 spec Requirement → Design → Task → Validation → Decision 매핑과 Risk 매핑을 정리했다.
- `.kiro/specs/aws-resource-glossary.md`에 IAM User 용어 항목을 신규 추가했다. portadmin 같은 IAM 관리자 사용자 개념이 02 runbook에서 본격적으로 사용되기 시작한 것을 반영했다.
- `.kiro/README.md`, `.kiro/CHANGELOG.md`도 신규 문서 안내와 변경 이력에 맞춰 갱신했다.
- 결정값(Decision ID, 선택값, 비용 영향, 운영 리스크, 후속 spec 영향)과 Console 클릭 경로 / 입력값 / 검증 항목 / rollback 순서는 변경하지 않았다.
- 실제 AWS 리소스 생성 없음.
- 8개 MS의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음.
- 민감정보(secret / token / password / app key / app secret / 계좌번호 / webhook URL / portadmin 비밀번호 / Root·portadmin MFA 시리얼 / 백업 코드) 기록 없음(`[REDACTED]` 또는 placeholder만 사용).

## 2026-06-05

- `.kiro/README.md`, `.kiro/CHANGELOG.md`, `.kiro/WORKLOG.md` 생성 작업을 진행했다.
- `.kiro` 작업공간이 8개 MS 전체 AWS Migration spec을 관리하는 공간임을 README에 문서화했다.
- 루트 공통 문서(`operator-decisions.md`, `ms-aws-service-decision-matrix.md`, `cost-simulation.md`, `followups-overview.md`, `aws-resource-glossary.md`, `note-aws-landscape-2021-vs-2026.md`)의 역할과 source of truth 기준을 README에 정리했다.
- Kiro 작업 로그는 날짜별 파일로 나누지 않고 `.kiro/WORKLOG.md` 하나에 간단히 누적하기로 했다.
- `.kiro/AGENTS.md`에 README / CHANGELOG / WORKLOG 관리 규칙 섹션을 추가했다. 기존 작업 규칙의 의미는 변경하지 않았다.
- 같은 날 진행한 `.kiro/specs/operator-decisions.md` 구조 재정리(Status 한글 라벨, At a Glance, Open/Tentative/Deferred 모음 추가)는 결정값을 변경하지 않은 가독성 개선이며, 자세한 변경 항목은 `.kiro/CHANGELOG.md` 2026-06-05 항목에 기록했다.
- 실제 AWS 리소스 생성 없음.
- 애플리케이션 소스 코드 수정 없음.
- 8개 MS 문서(README / CHANGELOG / worklog / AGENTS.md / 소스) 수정 없음.
- 민감정보 기록 없음.
