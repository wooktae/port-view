# Refactoring Backlog

## DailyBatchService 분리 최신 계획

상세 실행 계획: [`daily-batch-service-refactoring-plan.md`](daily-batch-service-refactoring-plan.md)

`DailyBatchService`는 현재 batch run 생성/상태 전이, step registry, 외부 Python 프로세스 실행, stdout/stderr 수집, timeout 처리, result payload JSON 생성, step log 저장, retry/from-step/rerun-failed 흐름, Slack summary 호출, 화면 조회 DTO 조립을 함께 담당한다.

분리 후보:

- `DailyBatchStepRegistry`
- `DailyBatchSlackNotifier`
- `DailyBatchResultPayloadBuilder`
- `DailyBatchQueryService`
- `ExternalProcessRunner`
- `ProcessOutputCollector`
- `IntradayPositionMonitorService`
- `DailyBatchRunService`
- `DailyBatchRetryPlanner`
- `DailyBatchStepExecutor`
- `DailyBatchOrchestrator`

가장 먼저 실제 코드로 분리할 후보는 `DailyBatchStepRegistry`이다. DB 상태 전이와 외부 프로세스 실행을 건드리지 않고, step code/order/name/workDir/command 정의만 옮길 수 있어 위험이 가장 낮다.

먼저 건드리지 말아야 할 영역:

- `executeDailyPipeline(...)` step loop
- `isNoTarget(...)` stdout/stderr 문구 판정
- retry/from-step/rerun-failed step slicing
- 외부 프로세스 timeout/stream 처리
- request/result payload key 변경

이 문서는 현재 구조 분석 기준의 리팩토링 후보를 정리합니다. 실제 삭제나 리팩토링 작업을 의미하지 않으며, 검토 후보 목록입니다.

## 우선순위 요약

### High

- `DailyBatchService` 책임 분리
- 민감정보/환경 의존 설정 분리
- `SlackNotificationService` webhook 설정을 typed properties로 전환

### Medium

- View 공통 util/label util 정리
- `templates/pages`와 루트 template 병존 구조 정리 검토
- CSS 구조 정리
- `StrategyExecutionSubmitService`의 JdbcTemplate 직접 사용 분리 검토

### Low

- DTO class/record 스타일 통일 검토
- 주석 인코딩 깨짐 정리
- legacy/보조 화면 유지 정책 문서화

## DailyBatchService 분리 후보

대상:

- `src/main/java/my/portfolio/port_view/service/DailyBatchService.java`

현재 역할:

- Daily Batch page DTO 조회
- batch run 생성
- step 목록 구성
- 특정 step부터 실행
- 실패 step 재실행
- 외부 process 실행
- stdout/stderr stream 수집
- timeout 처리
- step/run 상태 갱신
- result payload JSON 문자열 생성
- Slack summary 호출

분리 후보:

- `DailyBatchRunService`
  - run 생성, 상태 전이, 실패 처리
- `DailyBatchStepRegistry`
  - step code/name/command/working directory 정의
- `DailyBatchOrchestrator`
  - 전체 실행 흐름 제어
- `ExternalProcessRunner`
  - ProcessBuilder, timeout, stdout/stderr 수집
- `StepExecutionResultMapper`
  - exit code/stdout/stderr/result payload mapping
- `DailyBatchPayloadBuilder`
  - JSON payload 생성

기대 효과:

- batch 실행 로직 테스트 단위 축소
- 외부 command 실행 공통화
- step 정의 변경 영향 범위 축소
- 실패/재실행 흐름 가독성 개선

## SlackNotificationService 분리 후보

대상:

- `src/main/java/my/portfolio/port_view/service/SlackNotificationService.java`

현재 역할:

- Slack enable/webhook 설정 읽기
- Daily Batch summary 데이터 조회
- Daily Run summary 조립
- Strategy Execution summary 조립
- Balance/Position summary 조립
- Slack message text 조립
- webhook POST 전송
- formatting/helper 처리

분리 후보:

- `SlackProperties`
  - `slack.enabled`, `slack.webhook-url` typed config
- `SlackClient`
  - webhook payload 전송
- `DailyBatchSlackMessageBuilder`
  - Daily Batch summary text 조립
- `SlackSummaryQueryService`
  - summary에 필요한 데이터 조회
- 공통 formatting util 재사용
  - money/date/duration/null 처리

기대 효과:

- webhook 전송 테스트와 메시지 조립 테스트 분리
- 민감정보 설정 관리 개선
- label/format 중복 감소

## View 공통 Util 정리 후보

대상:

- `ViewFormatUtils`
- `ViewTextUtils`
- `OrderLabelUtils`
- `ConnectorLabelUtils`
- `StrategyExecutionLabelUtils`
- `DailyBatchLabelUtils`
- `ProfitClassUtils`
- `AccountNoResolver`
- `ViewNames`

검토 포인트:

- `requestTypeLabel`, `requestStatusLabel`, `executionStatusLabel`, `orderMethodLabel` 계열 중복
- `marketSignalLabel` 중복
- `ViewFormatUtils`가 service/repository/template에서 모두 사용되는 계층 혼재
- Thymeleaf static util 호출과 Service DTO pre-format 방식이 혼재
- 계좌번호 fallback이 Controller, template, batch config에 분산

개선 방향:

- domain별 label enum 또는 mapping registry 검토
- template static call 최소화, ViewModel에서 표시 label/class를 명확히 제공
- account no resolution을 공통 Model 또는 argument resolver로 정리
- View formatting 책임을 service/view adapter 계층으로 모으기

## Legacy/Unused 검토 후보

삭제 대상이 아니라 사용 여부 검증 후보입니다.

루트 template 중복 후보:

- `templates/dashboard.html`
- `templates/balance-summary.html`
- `templates/orders.html`
- `templates/order-detail.html`
- `templates/positions.html`
- `templates/strategy_detail.html`
- `templates/strategy_plans.html`
- `templates/strategy_report.html`

아직 route에서 사용 중인 legacy/보조 화면 후보:

- `templates/holdings.html`
- `templates/trade-orders.html`
- `templates/get-price-realtime.html`

별도 검토 후보:

- `templates/strategy_daily.html`
  - 현재 `StrategyDailyViewController`가 직접 반환
  - `ViewNames`에 없음
  - `pages/` layout 이전 여부 검토 대상

검토 절차:

1. Controller 반환 view name 기준으로 실제 사용 여부 확인
2. access log 또는 사용자 동선 기준으로 사용 여부 확인
3. 대체 화면 존재 여부 확인
4. 삭제가 필요하면 별도 이슈/PR에서 처리

## Template/Pages 구조 정리 후보

현재 상태:

- 신규 화면은 `templates/pages/*` 사용
- 공통 sidebar는 `templates/fragments/sidebar.html`
- 일부 루트 template이 남아 있음

정리 방향:

- 모든 운영 화면을 `pages/*` 기준으로 통일할지 결정
- legacy 화면도 sidebar layout으로 이전할지 결정
- `ViewNames`에 모든 view name을 등록할지 결정
- 직접 문자열 반환을 줄이고 `ViewNames` 사용으로 통일

## CSS 구조 정리 후보

현재 상태:

- 루트 CSS: `static/css/*.css`
- 신규 page CSS: `static/css/pages/*.css`
- layout CSS: `static/css/layout/app-layout.css`
- legacy 화면 스타일 일부가 루트 `dashboard.css`에 포함

정리 방향:

- layout/common/page CSS 경계를 명확히 분리
- 루트 template 제거 여부와 함께 루트 CSS 사용 여부 검토
- legacy 전용 스타일은 별도 파일 또는 제거 후보로 분류
- 중복 class와 page별 override 정리

## Configuration 정리 후보

대상:

- DB 연결 설정
- Slack webhook
- 기본 계좌번호
- Connector URL
- 외부 프로젝트 절대 경로

정리 방향:

- 환경변수 또는 local config로 민감정보 분리
- profile별 설정 파일 도입
- Git tracked config에는 example/default non-secret만 유지
- `@ConfigurationProperties` 기반 typed config 확대

## Repository/Service 경계 정리 후보

대상:

- `StrategyExecutionSubmitService`

검토 포인트:

- service 내부에 JdbcTemplate query/update가 직접 존재
- submit command 처리와 persistence query가 한 클래스에 결합

정리 방향:

- submit 대상 조회/update를 별도 repository로 이동
- Connector API client와 DB update 책임 분리
- transaction boundary 명확화

## 문서화 유지 후보

- route 목록
- external module dependency
- Daily Batch step code 목록
- 상태값 정의
- 운영 설정 방법
- 민감정보 관리 정책
