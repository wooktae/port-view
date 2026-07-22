# CHANGELOG

port-view 코드와 문서의 주요 변경 이력을 기록한다.

## 작성 원칙

| 항목 | 값 |
| --- | --- |
| 기록 범위 | port-view 코드 · 설정 · 화면 · 테스트 · 문서 변경 |
| 제외 범위 | 다른 MS 내부 구현 · Step Functions 내부 세부 이력 · 일회성 운영 로그 |
| 정렬 | 최신 날짜를 상단에 추가 |
| 분류 | Added · Changed · Fixed · Removed · Security |
| 상세 근거 | 필요한 경우 `docs` 또는 commit 참조 |
| 민감정보 | 실제 계좌번호 · secret · token · ARN · public IP 원문 기록 금지 |

## 2026-07-22 — port-view 문서 기준 재정비

### Changed

| 항목 | 값 |
| --- | --- |
| `AGENTS.md` | `.kiro` 전용 규칙을 제거하고 port-view 전용 작업 규칙으로 전면 재작성 |
| 작업 범위 | Java · 설정 · Thymeleaf · CSS · 테스트 · 문서 · Docker 기준 명확화 |
| 책임 경계 | 조회 · 승인 · Step Functions 트리거 UI로 한정 |
| 실행 backend | Fargate는 `aws-stepfunctions` · `local-file`은 로컬 검증과 복구용으로 구분 |
| Paper 주문 gate | Step 1~11 safe trigger와 Step 12~17 approval trigger 분리 원칙 명시 |
| DB 기준 | `view_app` · `marketconnector_app` 역할과 schema-per-domain 주의사항 정리 |
| 문서 기준 | 신규 독립 표 2컬럼 기본 · 국소 수정 · 긴 셀 금지 · UTF-8 No BOM 명시 |
| `AGENTS.md` 판정 | 단독으로 port-view 코드와 문서 작업 기준을 충족하도록 확정 |
| `README.md` | 삭제된 docs로 향하던 링크 제거 · 상세 문서 목록을 실제 존재 문서로 축소 |
| `docs/source-file-catalog.md` | Documents 목록을 유지 중인 4개 문서로 정리 · 삭제 문서 참조 제거 |
| 문서 구조 | README · CHANGELOG · `docs/source-file-catalog.md` 중심으로 단순화 |
| `CHANGELOG.md` | 중복 이력과 반복 Notes를 정리하고 날짜별 핵심 변경 중심으로 전면 재구성 |

### Removed

| 항목 | 값 |
| --- | --- |
| 상세 docs | architecture · configuration · daily-batch · security · refactoring · unused 후보 문서 제거 |
| worklog | `docs/worklog` 디렉터리와 날짜별 파일 제거 · 신규 생성 중단 |

### Security

| 항목 | 결과 |
| --- | --- |
| 코드 변경 | 없음 |
| AWS · DB · broker · KIS · Slack 실행 | 0건 |
| Git write 명령 | 0건 |
| 민감정보 원문 신규 기록 | 0건 |

## 2026-07-01 — View 책임 경계와 Fargate 문서화

### Added

| 파일 | 변경 |
| --- | --- |
| `README.md` | View 책임 경계와 ECS Fargate 배포 관점 추가 |
| `docs/architecture.md` | 조회 흐름과 Step Functions `StartExecution` 트리거 흐름 정리 |
| `docs/configuration.md` | Fargate 안전 기본값과 ARN placeholder 정리 |
| `docs/daily-batch.md` | safe · approval endpoint와 payload 타입 정합 설명 |
| `docs/worklog/2026-07-01.md` | port-view 문서 최신화 작업 기록 |

### Changed

| 항목 | 값 |
| --- | --- |
| View 책임 | 조회 · 승인 · 트리거 UI |
| 실제 실행 책임 | Step Functions · EventBridge Scheduler · ECS · SSM · AWS Batch · Lambda |
| Fargate 안전 기본값 | `aws-stepfunctions` · local-file 비활성 · Paper 주문 비활성 · 최대 Step 11 |
| 로컬 경로 | Fargate 운영 경로에서 사용하지 않음 |

### Security

| 항목 | 결과 |
| --- | --- |
| 애플리케이션 코드 변경 | 없음 |
| AWS · DB · 외부 API 실행 | 0건 |
| 민감정보 원문 신규 기록 | 0건 |

## 2026-06-29 — AWS Step Functions backend와 승인 실행 UI

### Added

| 항목 | 값 |
| --- | --- |
| 실행 backend | `aws-stepfunctions` 모드 |
| Safe endpoint | `POST /daily-batch/aws-stepfunctions/start-range` |
| Approval endpoint | `POST /daily-batch/aws-stepfunctions/start-approval-range` |
| 화면 | Step 1~11 safe 버튼과 Step 12~17 승인 버튼 |
| 설정 | 일반 workflow ARN과 approval workflow ARN 분리 |
| 로컬 도구 | Local File용과 Step Functions용 wrapper 분리 |

### Changed

| 파일 · 영역 | 변경 |
| --- | --- |
| `pom.xml` | AWS SDK v2 Step Functions 의존성 반영 |
| `DailyBatchProperties` | backend · ARN · 실행 gate 설정 추가 |
| `StepFunctionsDailyBatchExecutionService` | safe와 approval 실행 경로 분리 |
| `DailyBatchController` | safe · approval endpoint 처리 추가 |
| `daily_batch.html` | backend와 gate별 실행 버튼 노출 |
| `application-aws-paper.properties` | Step Functions 관련 환경변수 placeholder 추가 |
| StartExecution payload | boolean · numeric · string 타입 구분 |
| 실행일 | Asia/Seoul 기준 `runDate` 포함 |

### Fixed

| 문제 | 해결 |
| --- | --- |
| `runDate` 누락으로 Step 11 이후 `States.Runtime` 발생 | View input에 KST 기준 `runDate` 추가 |
| approval boolean이 문자열로 전달됨 | JSON boolean으로 변경 |
| Step order가 문자열로 전달됨 | JSON numeric으로 변경 |
| approval 버튼이 일반 workflow ARN 호출 | approval 전용 ARN 사용 |
| 로컬 wrapper에 safe-only gate 잔존 | backend별 wrapper 분리 |

### Validation

| 항목 | 결과 |
| --- | --- |
| Step 1~11 | `StartExecution` 후 approval-required 경로 통과 |
| Step 12~17 | approval gate 통과와 execution 성공 확인 |
| 주문 발생 | 검증 회차 신규 broker 주문 없음 |
| 참조 commit | `e72de6f` |

### Security

| 항목 | 결과 |
| --- | --- |
| 계좌번호 화면 노출 | redaction 유지 |
| ARN · account-id 문서 기록 | placeholder만 사용 |
| 추가 운영 실행 | 문서 정리 회차에서 0건 |

## 2026-06-29 — Snapshot Refresh와 Daily Batch 화면 정비

### Added

| 항목 | 값 |
| --- | --- |
| AWS Paper Local View | 실행 전제와 starter 사용 방법 문서화 |
| Snapshot Refresh | Dashboard · Balance · Positions 동작 설명 |
| Daily Batch UI | 실행 이력 카드와 status 표시 개선 |

### Changed

| 항목 | 값 |
| --- | --- |
| `aws-paper` profile | Snapshot Refresh 설정을 env override 기준으로 정리 |
| Spring datasource | `view_app` 사용 |
| Connector subprocess | `marketconnector_app` 사용 |
| Daily Batch 화면 | Step Logs · Payload · 실행 모드 표시 보강 |

### Fixed

| 문제 | 해결 |
| --- | --- |
| `connector_balance.py`가 `view_app`으로 실행 | subprocess DB user를 `marketconnector_app`으로 분리 |
| 실행 이력과 status pill 스타일 부족 | 화면 CSS 보강 |

## 2026-05-28 — 소스 파일 카탈로그와 설명 보강

### Added

| 항목 | 값 |
| --- | --- |
| `docs/source-file-catalog.md` | 주요 Java · 설정 · 문서 파일 역할 정리 |
| 소스 설명 | Connector · Dashboard · Position · Report 관련 한글 주석 보강 |

### Changed

| 항목 | 값 |
| --- | --- |
| `README.md` | 소스 파일 카탈로그 링크 추가 |
| 문서 범위 | 2026-05-27 이후 변경사항 정리 |

## 2026-05-27 — schema-per-domain 전환 문서화

### Added

| 항목 | 값 |
| --- | --- |
| Database | PostgreSQL 단일 DB `portfolio` |
| Schema | domain별 schema 구조 |
| View SQL | Hikari `search_path` 기반 조회 |

### Changed

| 항목 | 값 |
| --- | --- |
| datasource 설정 | `INTEREST_DB_*` 환경변수 기반 |
| DB name 기본값 | `portfolio` |
| 검증 화면 | Dashboard · Balance · Holdings · Strategy Plan · Daily Batch · Report |

## 2026-05-26 — 장 시작 전 자동 매수 차단

### Changed

| 항목 | 값 |
| --- | --- |
| 대상 Step | `DAILY_AUTO_BUY` |
| 기준 시각 | 09:00 KST 이전 |
| 처리 | 실제 자동 매수 스크립트 호출 없이 보류 |
| 결과 성격 | `NO_TARGET` |

## 2026-05-23 — 미사용 View 구조 정리

### Added

| 항목 | 값 |
| --- | --- |
| `docs/unused-view-file-candidates.md` | 미사용 View 파일 후보 분석 |

### Changed

| 항목 | 값 |
| --- | --- |
| Template · CSS | `templates/pages` · `static/css/pages` 구조로 정리 |
| Controller | 삭제된 화면과 연결된 과거 URL 제거 |
| Service · Repository | 미사용 메서드와 구현 제거 |
| DTO | 기능별 하위 패키지로 정리 |
| 설정 | 미사용 `connector.api.*`와 일부 `portfolio.view.*` 제거 |

### Fixed

| 문제 | 해결 |
| --- | --- |
| 삭제된 template 반환 가능성 | 관련 Controller와 View 상수 제거 |
| compile 정합 | `mvnw.cmd clean compile` 성공 |
