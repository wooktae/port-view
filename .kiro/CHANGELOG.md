# Kiro AWS Migration Changelog

본 문서는 `.kiro` 작업공간 안의 AWS Migration spec 문서 변경 이력만 간단히 기록한다.

## 작성 원칙

- spec 구조 변경, 루트 공통 문서 변경, 신규 spec 생성, 주요 문서 재구성만 기록한다.
- 8개 MS의 세부 코드 변경 이력은 본 문서에 기록하지 않는다.
- 너무 자세한 일일 작업 로그는 `.kiro/WORKLOG.md`에 남기고, 본 문서에는 의미 있는 변경만 짧게 정리한다.
- 항목 분류는 `Added`, `Changed`, `Removed`, `Security`로 통일한다.
- 날짜는 한국 기준의 작업 일자를 사용한다.

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
