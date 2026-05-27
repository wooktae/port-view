# CHANGELOG

## 2026-05-27

### Changed

- DB datasource 설정을 `INTEREST_DB_*` 환경변수 placeholder 기준으로 외부화했다.

### Notes

- 실제 DB 접속, 서버 실행, 외부 API 호출은 실행하지 않았다.

## 2026-05-26

### Changed

- Daily Batch의 `DAILY_AUTO_BUY` 단계가 한국 시간 09:00 이전에는 실제 자동 매수 실행 스크립트를 호출하지 않고 보류 메시지와 함께 NO_TARGET 성격으로 종료되도록 변경했다.

### Notes

- Daily Batch 실행, Slack Webhook 테스트, 외부 주문 API 호출, DB 명령은 실행하지 않았다.

## 2026-05-23

### Added

- 사용하지 않는 View 파일 후보 분석 문서 `docs/unused-view-file-candidates.md`를 추가했다.

### Changed

- `templates/pages`와 `static/css/pages` 구조로 전환된 화면 기준으로, 루트의 과거 HTML/CSS 파일을 정리했다.
- 삭제된 과거 화면에 연결된 Controller, ViewNames 상수, Service, Repository, DTO, Entity를 정리했다.
- 더 이상 참조되지 않는 `StrategyDailyViewService`, `StrategyExecutionViewService.getPlans()`, `DailyBatchRepository.markStepSkipped(...)`를 정리했다.
- 사용하지 않는 `connector.api.*`, 일부 `portfolio.view.*` 설정 바인딩과 기본 설정 항목을 정리했다.
- 화면 DTO를 `dailybatch`, `dashboard`, `order`, `position`, `strategy` 기능별 하위 패키지로 정리했다.
- 2026-05-23 작업 일지에 최종 문서화와 금지 작업 미실행 상태를 정리했다.

### Fixed

- 삭제된 템플릿을 반환하던 과거 URL Controller를 함께 제거해 런타임 템플릿 resolve 오류 가능성을 줄였다.

### Notes

- Daily Batch 실행, Slack Webhook 테스트, 외부 투자/주문 API 호출, DB 명령은 실행하지 않았다.
- 변경 후 `.\mvnw.cmd clean compile` 검증은 성공했다.
