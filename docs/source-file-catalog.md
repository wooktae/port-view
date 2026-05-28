# Source File Catalog

포트폴리오 AWS Migration 전 port-view 모듈의 주요 파일 역할을 정리한 문서다. repository root 기준 상대 경로를 사용하며, build 결과물과 cache 파일은 제외했다.

## Java Application

- `src/main/java/my/portfolio/port_view/PortViewApplication.java`
  - 제목: Spring Boot 실행 진입점
  - 파일 내용: port-view 애플리케이션을 시작하고 ConfigurationProperties scan을 활성화한다.
  - 주요 역할: Spring MVC/Thymeleaf View 마이크로서비스 부트스트랩.
  - 주의사항: 서버 실행 시 DB, Connector, Batch 관련 환경 설정이 함께 로딩된다.

- `src/main/java/my/portfolio/port_view/common/ViewNames.java`
  - 제목: View 이름 상수
  - 파일 내용: Thymeleaf template 경로 상수를 한 곳에서 관리한다.
  - 주요 역할: Controller가 문자열 view name을 직접 반복하지 않도록 한다.
  - 주의사항: 기존 URL과 template 경로 변경 없이 유지해야 한다.

## Controller

- `src/main/java/my/portfolio/port_view/controller/BalanceController.java`
  - 제목: 잔고 화면 Controller
  - 파일 내용: `/balance-summary` 요청을 처리하고 잔고/초기자본/누적손익 model을 구성한다.
  - 주요 역할: 계좌번호 해석, Connector 잔고 갱신 호출, 잔고 화면 model 전달.
  - 주의사항: 화면 진입 시 외부 Connector 잔고 스크립트가 호출될 수 있다.

- `src/main/java/my/portfolio/port_view/controller/DashboardController.java`
  - 제목: 대시보드 Controller
  - 파일 내용: `/`, `/dashboard` 요청을 처리하고 대시보드 DTO와 최신 리포트 요약을 전달한다.
  - 주요 역할: 대시보드 첫 화면의 통합 model 구성.
  - 주의사항: URL과 `dashboard`, `latestBacktestSummary` model attribute 이름을 유지한다.

- `src/main/java/my/portfolio/port_view/controller/PositionController.java`
  - 제목: 보유 종목 Controller
  - 파일 내용: `/positions`, `/positions/{tickerCode}` 요청을 처리한다.
  - 주요 역할: 보유 종목 목록/상세 화면 model 구성.
  - 주의사항: 화면 진입 시 Connector 잔고 갱신이 호출될 수 있다.

- `src/main/java/my/portfolio/port_view/controller/OrderController.java`
  - 제목: 주문 화면 Controller
  - 파일 내용: `/orders`, `/orders/{id}` 요청을 처리한다.
  - 주요 역할: 주문 목록/상세 model 구성과 최신 주문 체결 상태 갱신 호출.
  - 주의사항: 화면 조회 전 Connector 주문 확인 스크립트가 호출될 수 있다.

- `src/main/java/my/portfolio/port_view/controller/DailyBatchController.java`
  - 제목: Daily Batch Controller
  - 파일 내용: Daily Batch 조회, 실행, 재실행, Slack 테스트 요청을 처리한다.
  - 주요 역할: 운영 배치 화면과 수동 실행 액션 연결.
  - 주의사항: 실제 Batch/Slack 실행 endpoint가 있으므로 검증 중 호출 금지.

- `src/main/java/my/portfolio/port_view/controller/StrategyExecutionViewController.java`
  - 제목: 전략 실행 계획 Controller
  - 파일 내용: 전략 실행 계획 목록/상세와 주문 제출 요청을 처리한다.
  - 주요 역할: execution plan 조회와 submit service 연결.
  - 주의사항: 주문 제출 액션은 외부 실행 API와 연결될 수 있어 임의 호출 금지.

- `src/main/java/my/portfolio/port_view/controller/StrategyReportController.java`
  - 제목: 백테스트 리포트 Controller
  - 파일 내용: 최신/특정 runId의 백테스트 리포트 화면을 제공한다.
  - 주요 역할: ReportService 결과를 Thymeleaf model로 전달.
  - 주의사항: runId path variable과 기존 model attribute를 유지한다.

## Service

- `src/main/java/my/portfolio/port_view/service/BalanceService.java`
  - 제목: 잔고 조회 Service
  - 파일 내용: 계좌별 잔고 목록, 주식 평가금액, 초기자본, 누적손익을 조회/계산한다.
  - 주요 역할: Balance 화면 계산 로직의 application service.
  - 주의사항: Connector snapshot repository 결과 의미를 바꾸지 않는다.

- `src/main/java/my/portfolio/port_view/service/DashboardService.java`
  - 제목: 대시보드 조립 Service
  - 파일 내용: 잔고, 포지션, 주문 데이터를 대시보드 DTO로 조립한다.
  - 주요 역할: 첫 화면 카드와 목록 표시값 계산.
  - 주의사항: 현금 표시 계산은 화면 표시 기준이므로 DB 원천값을 변경하지 않는다.

- `src/main/java/my/portfolio/port_view/service/PositionService.java`
  - 제목: 보유 종목 조립 Service
  - 파일 내용: 보유 종목 목록/상세 화면에 필요한 평가손익과 insight 데이터를 조립한다.
  - 주요 역할: 포지션 row, 최고/최저 성과 카드, 상세 리서치 정보 구성.
  - 주의사항: 수익률 class와 표시 문구는 Thymeleaf CSS class와 맞물린다.

- `src/main/java/my/portfolio/port_view/service/OrderService.java`
  - 제목: 주문 조회 Service
  - 파일 내용: 주문 목록, 상세, 체결, 이벤트 타임라인을 DTO로 변환한다.
  - 주요 역할: Connector 주문 entity를 화면 전용 구조로 변환.
  - 주의사항: broker order number와 branch code는 민감/운영 정보로 로그 노출에 주의한다.

- `src/main/java/my/portfolio/port_view/service/ConnectorSnapshotRefreshService.java`
  - 제목: Connector 잔고 갱신 Service
  - 파일 내용: 외부 Python 잔고 스크립트를 실행해 snapshot 최신화를 유도한다.
  - 주요 역할: 화면 진입 시 잔고 데이터 stale/즉시 갱신 처리.
  - 주의사항: 외부 프로세스, timeout, 작업 디렉터리 설정에 의존한다.

- `src/main/java/my/portfolio/port_view/service/ConnectorOrderRefreshService.java`
  - 제목: Connector 주문 갱신 Service
  - 파일 내용: 진행 중 주문을 선별해 외부 주문 확인 Python 스크립트를 실행한다.
  - 주요 역할: 주문 화면 진입 전 체결 상태 동기화 보조.
  - 주의사항: 외부 API 호출 가능성이 있으므로 수동 검증 시 실행 금지.

- `src/main/java/my/portfolio/port_view/service/DailyBatchService.java`
  - 제목: Daily Batch Service
  - 파일 내용: Daily Batch 실행, 재실행, step 로그 조회와 외부 Python command 구성을 담당한다.
  - 주요 역할: 운영 배치 orchestration.
  - 주의사항: 실제 배치 실행, 주문/Slack 연동 가능성이 있어 임의 실행 금지.

- `src/main/java/my/portfolio/port_view/service/DailyBatchAsyncService.java`
  - 제목: Daily Batch 비동기 실행 Service
  - 파일 내용: Batch 실행 요청을 비동기로 위임한다.
  - 주요 역할: 화면 요청 thread와 장시간 batch 실행 분리.
  - 주의사항: async executor 설정과 batch 실행 순서에 의존한다.

- `src/main/java/my/portfolio/port_view/service/SlackClient.java`
  - 제목: Slack 전송 Client
  - 파일 내용: Slack webhook으로 메시지를 전송한다.
  - 주요 역할: SlackNotificationService의 실제 HTTP 전송 담당.
  - 주의사항: webhook URL은 환경변수/로컬 설정에서 주입하며 문서에 값 기록 금지.

- `src/main/java/my/portfolio/port_view/service/SlackNotificationService.java`
  - 제목: Slack 알림 Service
  - 파일 내용: Batch/전략/계좌 요약 메시지를 조립해 SlackClient로 전달한다.
  - 주요 역할: 운영 알림 메시지 구성.
  - 주의사항: Slack 테스트 실행 금지, 민감정보 마스킹 유지.

- `src/main/java/my/portfolio/port_view/service/ReportService.java`
  - 제목: 리포트 조립 Service
  - 파일 내용: 백테스트 리포트 repository 결과를 화면 DTO로 조립한다.
  - 주요 역할: 리포트 화면의 summary/stat/detail 구성.
  - 주의사항: runId 기준 조회 의미를 유지한다.

- `src/main/java/my/portfolio/port_view/service/StrategyExecutionViewService.java`
  - 제목: 전략 실행 조회 Service
  - 파일 내용: 전략 실행 계획과 주문 후보를 화면 DTO로 변환한다.
  - 주요 역할: 전략 실행 계획 목록/상세 조회.
  - 주의사항: execution schema 조회와 plan/order 관계에 의존한다.

- `src/main/java/my/portfolio/port_view/service/StrategyExecutionSubmitService.java`
  - 제목: 전략 주문 제출 Service
  - 파일 내용: 전략 실행 주문 제출 요청을 외부 execution API로 전달한다.
  - 주요 역할: View에서 실행 모듈로 주문 요청 연결.
  - 주의사항: 실제 주문/외부 API 호출 가능성이 있어 검증 중 호출 금지.

## Repository

- `src/main/java/my/portfolio/port_view/repository/*.java`
  - 제목: DB 조회 Repository 묶음
  - 파일 내용: JPA repository와 JdbcTemplate query repository가 함께 존재한다.
  - 주요 역할: connector, execution, research, ops 계열 테이블 조회.
  - 주의사항: DB schema/table/column 이름과 SQL 결과 의미 변경 금지.

- `src/main/java/my/portfolio/port_view/repository/ConnectorOrderRequestRepository.java`
  - 제목: Connector 주문 요청 Repository
  - 파일 내용: 계좌별 주문 목록, 주문 상세, 주문 갱신 대상 조회 query를 제공한다.
  - 주요 역할: 주문 화면과 Connector 주문 갱신 대상 선별.
  - 주의사항: request_status 조건과 lookback 기준은 운영 표시 결과에 직접 영향이 있다.

- `src/main/java/my/portfolio/port_view/repository/ReportRepository.java`
  - 제목: 백테스트 리포트 Repository
  - 파일 내용: 리포트 summary/stat/trade detail SQL을 보유한다.
  - 주요 역할: strategy/research 테이블을 읽어 리포트 화면 데이터 생성.
  - 주의사항: `search_path`와 runId UUID cast에 의존한다.

## Entity

- `src/main/java/my/portfolio/port_view/entity/*.java`
  - 제목: JPA Entity 묶음
  - 파일 내용: BalanceSummary, Connector snapshot/order/fill/event entity를 정의한다.
  - 주요 역할: DB table row와 Java 객체 매핑.
  - 주의사항: 테이블명/컬럼명 변경 금지, DDL/DML 직접 실행 금지.

## DTO

- `src/main/java/my/portfolio/port_view/dto/dashboard/*.java`
  - 제목: 대시보드 DTO
  - 파일 내용: 대시보드 카드, 보유 종목, 최근 주문 표시값을 담는다.
  - 주요 역할: DashboardService와 dashboard template 사이의 view model.
  - 주의사항: Thymeleaf model field 이름 변경 시 화면 영향이 있다.

- `src/main/java/my/portfolio/port_view/dto/position/*.java`
  - 제목: 보유 종목 DTO
  - 파일 내용: 보유 종목 목록, 상세 요약, 뉴스/리포트/수익 포인트 정보를 담는다.
  - 주요 역할: PositionService와 positions/position-detail template 사이의 view model.
  - 주의사항: 수익률 표시 class와 금액 문구는 CSS와 연결된다.

- `src/main/java/my/portfolio/port_view/dto/order/*.java`
  - 제목: 주문 DTO
  - 파일 내용: 주문 목록 row, 주문 상세, 체결, 이벤트 timeline을 담는다.
  - 주요 역할: OrderService와 orders/order-detail template 사이의 view model.
  - 주의사항: 주문번호/체결정보 전체 노출에 주의한다.

- `src/main/java/my/portfolio/port_view/dto/dailybatch/*.java`
  - 제목: Daily Batch DTO
  - 파일 내용: 배치 실행, step 로그, 수동 실행 옵션, Block Watch 후보를 담는다.
  - 주요 역할: Daily Batch 운영 화면 표시.
  - 주의사항: 실제 실행 상태와 혼동되지 않도록 상태 label 유지가 필요하다.

- `src/main/java/my/portfolio/port_view/dto/strategy/*.java`
  - 제목: 전략/리포트 DTO
  - 파일 내용: 전략 실행 계획, 주문 후보, Daily Signal, 백테스트 리포트 통계를 담는다.
  - 주요 역할: Strategy execution/report 화면 표시.
  - 주의사항: runId, planId, orderId 관계를 변경하지 않는다.

## Util / Config

- `src/main/java/my/portfolio/port_view/util/*.java`
  - 제목: View 표시 유틸리티
  - 파일 내용: 금액/날짜/문구 포맷, 수익 class, label 변환, 계좌번호 해석을 제공한다.
  - 주요 역할: 화면 표시 규칙 공통화.
  - 주의사항: 표시 문구는 기본적으로 한글을 유지한다.

- `src/main/java/my/portfolio/port_view/config/*.java`
  - 제목: 설정 바인딩과 비동기 설정
  - 파일 내용: Daily Batch, Connector, Slack, portfolio view, snapshot refresh 설정을 바인딩한다.
  - 주요 역할: 환경변수/local 설정 값을 typed property로 제공.
  - 주의사항: 민감정보 값은 코드/문서에 기록하지 않고 설정 key 이름은 유지한다.

## Templates

- `src/main/resources/templates/fragments/sidebar.html`
  - 제목: 공통 사이드바 fragment
  - 파일 내용: 주요 화면으로 이동하는 navigation fragment를 정의한다.
  - 주요 역할: 모든 화면의 좌측 navigation 공통화.
  - 주의사항: 기존 URL 링크 유지.

- `src/main/resources/templates/pages/*.html`
  - 제목: Thymeleaf 화면 template 묶음
  - 파일 내용: dashboard, balance, positions, orders, strategy, report, daily batch 화면을 렌더링한다.
  - 주요 역할: Controller model attribute를 한글 화면으로 표시.
  - 주의사항: model attribute 이름과 form action URL 변경 금지.

- `src/main/resources/templates/pages/daily_batch.html`
  - 제목: Daily Batch 운영 화면
  - 파일 내용: 배치 실행 이력, step 로그, 수동 실행 버튼, Slack 관련 액션을 표시한다.
  - 주요 역할: 운영자가 배치 상태를 확인하고 제한된 수동 액션을 수행하는 화면.
  - 주의사항: 실제 배치/Slack 실행 버튼이 있으므로 테스트 클릭 금지.

## Static CSS

- `src/main/resources/static/css/layout/app-layout.css`
  - 제목: 공통 app layout CSS
  - 파일 내용: app shell, sidebar, main layout 스타일을 정의한다.
  - 주요 역할: View 전체의 공통 레이아웃 제공.
  - 주의사항: 모든 화면에 영향이 있어 변경 범위를 작게 유지한다.

- `src/main/resources/static/css/pages/*.css`
  - 제목: 화면별 CSS 묶음
  - 파일 내용: dashboard, balance, orders, positions, strategy, daily batch 화면별 스타일을 정의한다.
  - 주요 역할: 각 template의 card/grid/status 색상과 반응형 배치를 담당.
  - 주의사항: 공통 class 변경 시 여러 화면이 동시에 영향받는다.

## Resources / Build

- `src/main/resources/application.properties`
  - 제목: 기본 Spring 설정
  - 파일 내용: server, datasource, JPA, View, Connector, Batch, Slack 설정 key를 정의한다.
  - 주요 역할: 실행 환경 기본값과 환경변수 placeholder 제공.
  - 주의사항: password/webhook/account 등 민감정보 값 기록 금지.

- `src/main/resources/application-local.properties.example`
  - 제목: local 설정 예시
  - 파일 내용: 로컬 실행에 필요한 override 예시를 민감정보 없이 제공한다.
  - 주요 역할: 개인 local 설정 작성 기준 제공.
  - 주의사항: 실제 local 설정 파일과 민감값은 repository에 기록하지 않는다.

- `pom.xml`, `mvnw`, `mvnw.cmd`
  - 제목: Maven 빌드 파일
  - 파일 내용: Spring Boot/Lombok/PostgreSQL 등 의존성과 Maven Wrapper를 제공한다.
  - 주요 역할: compile/package/run 명령 실행 기반.
  - 주의사항: 의존성 변경 시 `.\mvnw.cmd clean compile` 검증 필요.

## Test

- `src/test/java/my/portfolio/port_view/PortViewApplicationTests.java`
  - 제목: Spring context smoke test
  - 파일 내용: 애플리케이션 context load 기본 테스트를 포함한다.
  - 주요 역할: Spring Boot 설정 로딩의 최소 검증.
  - 주의사항: 외부 API/DB 의존성이 있으면 테스트 환경 설정이 필요할 수 있다.

## Documents

- `README.md`
  - 제목: 프로젝트 개요 문서
  - 파일 내용: 기술 스택, 주요 화면, 실행/빌드 방법, 설정, 보안 주의사항을 설명한다.
  - 주요 역할: 신규 작업자의 port-view 이해 진입점.
  - 주의사항: 사용자 관점 실행/구조 변경이 있을 때만 갱신한다.

- `CHANGELOG.md`
  - 제목: 변경 이력 문서
  - 파일 내용: 날짜별 주요 변경사항과 검증/주의사항을 기록한다.
  - 주요 역할: 미커밋/완료 작업의 이력 추적.
  - 주의사항: 기능 변경 없음 여부를 명시한다.

- `docs/architecture.md`, `docs/configuration.md`, `docs/security-notes.md`
  - 제목: 구조/설정/보안 문서
  - 파일 내용: 시스템 구조, 환경 설정, 민감정보 처리 원칙을 설명한다.
  - 주요 역할: AWS Migration 전 운영 이해 보조.
  - 주의사항: 실제 password/token/webhook/account 전체값 기록 금지.

- `docs/daily-batch.md`, `docs/daily-batch-service-refactoring-plan.md`
  - 제목: Daily Batch 문서
  - 파일 내용: Daily Batch 실행 흐름과 service refactoring 계획을 설명한다.
  - 주요 역할: Batch 운영/개선 작업 기준.
  - 주의사항: 실제 Batch 실행 없이 문서/코드 기준으로만 검토한다.

- `docs/refactoring-backlog.md`
  - 제목: 리팩토링 backlog
  - 파일 내용: 향후 정리 후보와 우선순위를 기록한다.
  - 주요 역할: 큰 리팩토링 전 분석 목록.
  - 주의사항: 계획 문서이며 즉시 기능 변경 근거로 사용하지 않는다.

- `docs/unused-view-file-candidates.md`
  - 제목: 미사용 View 파일 후보
  - 파일 내용: unused/legacy 의심 파일을 삭제 없이 후보로만 정리한다.
  - 주요 역할: 안전한 View 정리 전 검토 목록.
  - 주의사항: 정리 후보이며 파일 삭제 금지.

- `docs/worklog/*.md`
  - 제목: 날짜별 작업 로그
  - 파일 내용: 사용자의 계획/완료 처리 형식으로 작업 내용을 기록한다.
  - 주요 역할: 일자별 변경 맥락 보존.
  - 주의사항: 상태 표기는 `작업 명: 완료` 형식을 유지한다.

## 정리 후보

- `docs/unused-view-file-candidates.md`에 이미 정리 후보가 별도 관리된다.
- 이번 작업에서는 파일 또는 폴더를 삭제하지 않았고, legacy/unused 의심 항목은 문서 후보로만 유지한다.

## 개별 파일 경로 색인

아래 색인은 repository root 기준 상대 경로별 빠른 확인용이다. 상세 설명은 위 영역별 설명을 함께 참조한다.

- `AGENTS.md`
  - 제목: Codex 작업 지침
  - 파일 내용: port-view 작업 범위, 금지사항, 검증 명령, 문서화 규칙을 정의한다.
  - 주요 역할: 자동화 작업의 안전 기준 제공.
  - 주의사항: 현재 repository 안에서만 작업하고 commit은 사용자 요청 없이는 금지한다.

- `CHANGELOG.md`
  - 제목: 변경 이력
  - 파일 내용: 날짜별 변경사항과 검증/주의사항을 기록한다.
  - 주요 역할: AWS Migration 전 변경 맥락 보존.
  - 주의사항: 기능 변경 없음 여부와 금지 작업 미실행 여부를 명시한다.

- `README.md`
  - 제목: 프로젝트 안내
  - 파일 내용: 주요 화면, 패키지 구조, 실행/빌드/설정/보안 사항을 설명한다.
  - 주요 역할: 신규 작업자 온보딩 문서.
  - 주의사항: 실행 방법이나 구조가 바뀐 경우에만 갱신한다.

- `pom.xml`, `mvnw`, `mvnw.cmd`
  - 제목: Maven 빌드 구성
  - 파일 내용: 의존성, 플러그인, Maven Wrapper 실행 파일을 제공한다.
  - 주요 역할: compile/package/run 기준 제공.
  - 주의사항: 의존성 변경 시 compile 검증이 필요하다.

- `docs/architecture.md`
  - 제목: 구조 문서
  - 파일 내용: port-view의 화면/계층/외부 의존 구조를 설명한다.
  - 주요 역할: AWS Migration 전 구조 이해 보조.
  - 주의사항: 실제 외부 경로나 민감값은 기록하지 않는다.

- `docs/configuration.md`
  - 제목: 설정 문서
  - 파일 내용: 환경변수와 local 설정 기준을 설명한다.
  - 주요 역할: 실행 환경별 설정 분리 기준 제공.
  - 주의사항: password, webhook, token 값 기록 금지.

- `docs/daily-batch.md`
  - 제목: Daily Batch 운영 문서
  - 파일 내용: Daily Batch 화면과 실행 흐름을 설명한다.
  - 주요 역할: 운영 배치 이해와 점검 기준 제공.
  - 주의사항: 문서 확인 중 실제 batch 실행 금지.

- `docs/daily-batch-service-refactoring-plan.md`
  - 제목: Daily Batch 리팩토링 계획
  - 파일 내용: DailyBatchService 책임 분리 계획을 정리한다.
  - 주요 역할: 큰 리팩토링 전 분석 자료.
  - 주의사항: 계획 문서이며 즉시 구조 변경 근거로 사용하지 않는다.

- `docs/refactoring-backlog.md`
  - 제목: 리팩토링 backlog
  - 파일 내용: 향후 정리 후보와 우선순위를 기록한다.
  - 주요 역할: 변경 후보 추적.
  - 주의사항: 후보 파일을 즉시 삭제하지 않는다.

- `docs/security-notes.md`
  - 제목: 보안 주의사항
  - 파일 내용: 민감정보 유형과 기록 금지 원칙을 설명한다.
  - 주요 역할: 문서/로그/설정 관리 기준 제공.
  - 주의사항: 실제 계좌번호 전체, token, key, webhook 값 기록 금지.

- `docs/source-file-catalog.md`
  - 제목: 소스 파일 카탈로그
  - 파일 내용: 주요 파일의 내용, 역할, 운영 주의사항을 정리한다.
  - 주요 역할: AWS Migration 전 초기 정리 기준 문서.
  - 주의사항: build/cache 생성물은 제외한다.

- `docs/unused-view-file-candidates.md`
  - 제목: 미사용 View 후보 문서
  - 파일 내용: unused/legacy 의심 View 파일 후보를 정리한다.
  - 주요 역할: 삭제 전 검증 대상 관리.
  - 주의사항: 후보 문서일 뿐 파일 삭제 금지.

- `docs/worklog/2026-05-23.md`, `docs/worklog/2026-05-26.md`, `docs/worklog/2026-05-27.md`, `docs/worklog/2026-05-28.md`
  - 제목: 날짜별 작업 일지
  - 파일 내용: 일자별 완료/보류/확인 상태를 기록한다.
  - 주요 역할: 미커밋 변경 맥락 보존.
  - 주의사항: 지정된 들여쓰기와 상태 표기 형식을 유지한다.

- `src/main/resources/application.properties`
  - 제목: 기본 애플리케이션 설정
  - 파일 내용: 서버, DB, JPA, View, Connector, Batch, Slack 설정 key를 정의한다.
  - 주요 역할: 환경변수 placeholder 기반 실행 설정 제공.
  - 주의사항: 민감값은 환경변수/로컬 설정에서 주입한다.

- `src/main/resources/application-local.properties.example`
  - 제목: 로컬 설정 예시
  - 파일 내용: 개인 local 설정의 예시 key를 제공한다.
  - 주요 역할: 실제 local 설정 작성 기준 제공.
  - 주의사항: 실제 민감값을 넣지 않는다.

- `src/main/java/my/portfolio/port_view/PortViewApplication.java`
  - 제목: 애플리케이션 진입점
  - 파일 내용: Spring Boot main class를 정의한다.
  - 주요 역할: port-view 서버 시작.
  - 주의사항: ConfigurationProperties scan 유지.

- `src/main/java/my/portfolio/port_view/common/ViewNames.java`
  - 제목: View 이름 상수
  - 파일 내용: template view name 상수를 관리한다.
  - 주요 역할: Controller view 반환값 공통화.
  - 주의사항: 기존 template 경로 유지.

- `src/main/java/my/portfolio/port_view/config/AsyncConfig.java`
  - 제목: 비동기 실행 설정
  - 파일 내용: async executor 설정을 제공한다.
  - 주요 역할: 장시간 batch 실행을 요청 thread와 분리.
  - 주의사항: executor 이름/용도 변경 시 batch 실행 영향 확인 필요.

- `src/main/java/my/portfolio/port_view/config/ConnectorProperties.java`
  - 제목: Connector 설정
  - 파일 내용: connector base URL 설정을 바인딩한다.
  - 주요 역할: 외부 Connector 호출 endpoint 기준 제공.
  - 주의사항: 실제 URL 값은 환경별 설정으로 관리한다.

- `src/main/java/my/portfolio/port_view/config/DailyBatchProperties.java`
  - 제목: Daily Batch 설정
  - 파일 내용: Python 실행 파일, 작업 디렉터리, timeout, 계좌/환경 설정을 바인딩한다.
  - 주요 역할: batch 실행 command 구성 기준 제공.
  - 주의사항: 작업 디렉터리와 실행 순서 변경은 운영 영향이 크다.

- `src/main/java/my/portfolio/port_view/config/PortfolioViewProperties.java`
  - 제목: View 기본 설정
  - 파일 내용: 기본 계좌와 화면별 limit 설정을 바인딩한다.
  - 주요 역할: Controller/Service의 기본 조회 기준 제공.
  - 주의사항: 기본 계좌 값은 민감정보로 취급한다.

- `src/main/java/my/portfolio/port_view/config/SlackProperties.java`
  - 제목: Slack 설정
  - 파일 내용: Slack 활성화 여부와 webhook 설정을 바인딩한다.
  - 주요 역할: Slack 전송 기능의 환경 설정 제공.
  - 주의사항: webhook 값 기록 금지.

- `src/main/java/my/portfolio/port_view/config/SnapshotRefreshProperties.java`
  - 제목: 스냅샷 갱신 설정
  - 파일 내용: Connector 잔고 갱신 실행 옵션을 바인딩한다.
  - 주요 역할: 화면 진입 시 snapshot refresh 기준 제공.
  - 주의사항: 경로/실행 파일은 환경별 설정으로 관리한다.

- `src/main/java/my/portfolio/port_view/controller/*.java`
  - 제목: 화면 Controller 묶음
  - 파일 내용: Balance, Dashboard, Daily Batch, Order, Position, Strategy 화면 요청을 처리한다.
  - 주요 역할: URL 요청과 Service/View 연결.
  - 주의사항: 기존 URL, endpoint, model attribute 이름 변경 금지.

- `src/main/java/my/portfolio/port_view/service/*.java`
  - 제목: Application Service 묶음
  - 파일 내용: 화면 DTO 조립, 외부 Connector/Batch/Slack 호출, 리포트 조회를 담당한다.
  - 주요 역할: Controller와 Repository/외부 모듈 사이의 업무 흐름 구현.
  - 주의사항: Daily Batch, Slack, 주문/투자 API 호출 경로는 임의 실행 금지.

- `src/main/java/my/portfolio/port_view/repository/*.java`
  - 제목: Repository 묶음
  - 파일 내용: JPA와 JdbcTemplate 기반 DB 조회 코드를 제공한다.
  - 주요 역할: 화면별 필요한 DB 데이터 조회.
  - 주의사항: SQL 결과 의미, 테이블명, 컬럼명 변경 금지.

- `src/main/java/my/portfolio/port_view/entity/*.java`
  - 제목: Entity 묶음
  - 파일 내용: DB 테이블과 매핑되는 JPA entity를 정의한다.
  - 주요 역할: connector/balance/order/fill/event 데이터 매핑.
  - 주의사항: schema/table/column 이름 변경 금지.

- `src/main/java/my/portfolio/port_view/dto/dailybatch/*.java`
  - 제목: Daily Batch DTO 묶음
  - 파일 내용: 배치 실행, step log, 수동 실행 옵션, Block Watch 후보 데이터를 담는다.
  - 주요 역할: Daily Batch template view model.
  - 주의사항: 상태 label과 화면 필드 이름 유지.

- `src/main/java/my/portfolio/port_view/dto/dashboard/*.java`
  - 제목: Dashboard DTO 묶음
  - 파일 내용: 대시보드 metric, 보유 종목, 최근 주문, 전체 view DTO를 담는다.
  - 주요 역할: DashboardService 결과 전달.
  - 주의사항: 카드 CSS class와 표시 문구 영향 확인 필요.

- `src/main/java/my/portfolio/port_view/dto/order/*.java`
  - 제목: Order DTO 묶음
  - 파일 내용: 주문 목록, 상세, 체결, 이벤트 timeline, 주문 체인 정보를 담는다.
  - 주요 역할: 주문 화면 view model.
  - 주의사항: 주문번호/체결정보 노출 범위 주의.

- `src/main/java/my/portfolio/port_view/dto/position/*.java`
  - 제목: Position DTO 묶음
  - 파일 내용: 보유 종목 목록, 상세 요약, 뉴스, 리포트, 수익 포인트를 담는다.
  - 주요 역할: 보유 종목 화면 view model.
  - 주의사항: 수익률 class와 template 조건식을 함께 확인한다.

- `src/main/java/my/portfolio/port_view/dto/strategy/*.java`
  - 제목: Strategy/Report DTO 묶음
  - 파일 내용: 전략 실행 계획, 주문 후보, Daily Signal, 백테스트 통계를 담는다.
  - 주요 역할: 전략 실행/리포트 화면 view model.
  - 주의사항: runId/planId/orderId 관계 유지.

- `src/main/java/my/portfolio/port_view/util/*.java`
  - 제목: View 유틸리티 묶음
  - 파일 내용: label 변환, 금액/날짜 포맷, 수익 class, 계좌번호 해석을 제공한다.
  - 주요 역할: 화면 표시 규칙 공통화.
  - 주의사항: 한글 문구와 CSS class 계약 유지.

- `src/main/resources/templates/fragments/sidebar.html`
  - 제목: 공통 사이드바
  - 파일 내용: 화면 navigation fragment를 정의한다.
  - 주요 역할: 모든 View의 공통 메뉴 제공.
  - 주의사항: 링크 URL 변경 금지.

- `src/main/resources/templates/pages/dashboard.html`
  - 제목: 대시보드 template
  - 파일 내용: 계좌 요약, 최근 주문, 보유 종목, 최신 전략/리포트 요약을 표시한다.
  - 주요 역할: 메인 운영 현황 화면.
  - 주의사항: Controller model attribute 이름 유지.

- `src/main/resources/templates/pages/balance-summary.html`
  - 제목: 잔고 template
  - 파일 내용: 총 평가금액, 현금, 주식 평가금액, 평가손익을 표시한다.
  - 주요 역할: 계좌 잔고 구성 확인.
  - 주의사항: 금액 계산 표시식 변경 시 실제 원천값과 구분한다.

- `src/main/resources/templates/pages/positions.html`, `src/main/resources/templates/pages/position-detail.html`
  - 제목: 보유 종목 template
  - 파일 내용: 보유 종목 목록과 종목 상세 정보를 표시한다.
  - 주요 역할: 포지션 현황과 상세 insight 확인.
  - 주의사항: 수익/손실 색상 class와 DTO 필드 유지.

- `src/main/resources/templates/pages/orders.html`, `src/main/resources/templates/pages/order-detail.html`
  - 제목: 주문 template
  - 파일 내용: 주문 목록, 상세, 체결, 이벤트 timeline을 표시한다.
  - 주요 역할: Connector 주문 처리 이력 확인.
  - 주의사항: 주문 식별자와 broker 정보 노출 범위 주의.

- `src/main/resources/templates/pages/daily_batch.html`
  - 제목: Daily Batch template
  - 파일 내용: 배치 실행 이력, step 로그, 수동 실행 액션을 표시한다.
  - 주요 역할: 운영 배치 관제 화면.
  - 주의사항: 실제 실행 버튼 클릭 금지.

- `src/main/resources/templates/pages/strategy_plans.html`, `src/main/resources/templates/pages/strategy_detail.html`
  - 제목: 전략 실행 template
  - 파일 내용: 전략 실행 계획 목록/상세와 주문 후보를 표시한다.
  - 주요 역할: 전략 실행 전 검토 화면.
  - 주의사항: 주문 제출 action URL과 model attribute 유지.

- `src/main/resources/templates/pages/strategy_report.html`
  - 제목: 전략 리포트 template
  - 파일 내용: 백테스트 요약, 통계, 거래 상세를 표시한다.
  - 주요 역할: 전략 성과 검토 화면.
  - 주의사항: runId 기준 조회 흐름 유지.

- `src/main/resources/static/css/layout/app-layout.css`
  - 제목: 공통 레이아웃 CSS
  - 파일 내용: app shell, sidebar, main 영역 스타일을 정의한다.
  - 주요 역할: 전체 화면 기본 배치.
  - 주의사항: 변경 시 모든 template 영향 확인.

- `src/main/resources/static/css/pages/dashboard.css`
  - 제목: 대시보드 공용 CSS
  - 파일 내용: 대시보드와 metric card 손익 색상/배치를 정의한다.
  - 주요 역할: Dashboard/Balance/Positions 공통 카드 스타일 제공.
  - 주의사항: 공통 class 변경 시 여러 화면에 영향이 있다.

- `src/main/resources/static/css/pages/balance-summary.css`, `src/main/resources/static/css/pages/daily-batch.css`, `src/main/resources/static/css/pages/order-detail.css`, `src/main/resources/static/css/pages/orders.css`, `src/main/resources/static/css/pages/positions.css`, `src/main/resources/static/css/pages/strategy-execution.css`, `src/main/resources/static/css/pages/strategy-report.css`
  - 제목: 화면별 CSS
  - 파일 내용: 각 화면 전용 layout, card, table, badge 스타일을 정의한다.
  - 주요 역할: template별 시각 구조 제공.
  - 주의사항: class 이름 변경 시 해당 template 동시 수정 필요.

- `src/test/java/my/portfolio/port_view/PortViewApplicationTests.java`
  - 제목: 애플리케이션 context 테스트
  - 파일 내용: Spring context load 기본 테스트를 포함한다.
  - 주요 역할: 설정 로딩 smoke test.
  - 주의사항: 외부 설정 의존성으로 실패할 수 있어 환경값 확인 필요.
