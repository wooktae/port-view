# Source File Catalog

port-view의 주요 파일과 디렉터리 역할을 빠르게 확인하기 위한 문서다.

모든 파일을 나열하는 inventory가 아니라, 구조 이해와 변경 영향 판단에 필요한 항목만 기록한다.

repository root 기준 상대 경로를 사용하며 build 결과물과 cache 파일은 제외한다.

## 사용 원칙

| 항목 | 값 |
| --- | --- |
| 기준 | 현재 port-view 코드와 문서 구조 |
| 포함 | 주요 entrypoint · 계층별 묶음 · 운영 영향 파일 |
| 제외 | build 결과물 · cache · 단순 생성 파일 |
| 갱신 | 파일의 경로 · 책임 · 운영 영향이 바뀔 때 |
| 생략 | 내부 구현만 바뀌고 파일 책임이 동일한 경우 |
| 민감정보 | 실제 계좌번호 · secret · token · ARN · public IP 기록 금지 |

## Root

| 파일 | 역할 |
| --- | --- |
| `AGENTS.md` | port-view 코드와 문서 작업 규칙 |
| `README.md` | 현재 구조 · 실행 방법 · 운영 AS-IS |
| `CHANGELOG.md` | port-view 주요 변경 이력 |
| `pom.xml` | Maven 의존성과 build 설정 |
| `mvnw` · `mvnw.cmd` | Maven Wrapper |
| `Dockerfile` | ECS Fargate용 container image build |

### 변경 시 확인

| 대상 | 확인 |
| --- | --- |
| `AGENTS.md` | 작업 범위 · 안전 gate · 문서 갱신 규칙 |
| `README.md` | 사용자 관점 구조와 운영 상태 |
| `CHANGELOG.md` | 실제 port-view 변경만 기록 |
| `pom.xml` | compile · test · package 영향 |
| `Dockerfile` | runtime · port · profile · build context |

## Application

| 파일 | 역할 |
| --- | --- |
| `src/main/java/my/portfolio/port_view/PortViewApplication.java` | Spring Boot entrypoint와 ConfigurationProperties scan |
| `src/main/java/my/portfolio/port_view/common/ViewNames.java` | Thymeleaf View 이름 상수 |

`PortViewApplication` 변경 시 context load와 설정 scan을 확인한다.

`ViewNames` 변경 시 Controller 반환값과 template 경로를 함께 확인한다.

## Controller

| 파일 · 경로 | 역할 |
| --- | --- |
| `BalanceController.java` | `/balance-summary` 잔고 화면 |
| `DashboardController.java` | `/` · `/dashboard` 통합 화면 |
| `PositionController.java` | `/positions` 목록과 상세 |
| `OrderController.java` | `/orders` 목록과 상세 |
| `DailyBatchController.java` | Daily Batch 조회 · local action · AWS trigger |
| `StrategyExecutionViewController.java` | 실행 계획 조회와 제출 action |
| `StrategyReportController.java` | 최신 · 특정 run 리포트 |
| `controller/*.java` | Strategy Daily 등 화면 Controller 묶음 |

### 변경 시 확인

| 항목 | 값 |
| --- | --- |
| URL | 기존 endpoint 호환성 |
| Model | template attribute 이름 |
| 실행 action | gate와 running 상태 |
| 민감정보 | 계좌번호 · ARN · 주문번호 출력 금지 |
| 외부 호출 | 조회 요청과 실행 요청 구분 |

## Service

| 파일 · 경로 | 역할 |
| --- | --- |
| `BalanceService.java` | 잔고 · 초기자본 · 누적손익 조회 |
| `DashboardService.java` | Dashboard DTO 조립 |
| `PositionService.java` | 포지션 목록과 상세 DTO 조립 |
| `OrderService.java` | 주문 · 체결 · 이벤트 timeline 조립 |
| `ReportService.java` | 리포트 summary · stat · detail 조립 |
| `StrategyExecutionViewService.java` | 실행 계획과 주문 후보 조회 |
| `StrategyExecutionSubmitService.java` | 외부 execution 제출 요청 |
| `ConnectorSnapshotRefreshService.java` | Local View Snapshot Refresh |
| `ConnectorOrderRefreshService.java` | Local View 주문 상태 Refresh |
| `DailyBatchService.java` | local-file 실행과 Step Log 조회 |
| `DailyBatchAsyncService.java` | local-file 비동기 실행 위임 |
| `StepFunctionsDailyBatchExecutionService.java` | AWS Step Functions `StartExecution` |
| `SlackNotificationService.java` | Local View Slack 메시지 조립 |
| `SlackClient.java` | Slack webhook HTTP 전송 |
| `service/*.java` | 화면 application service 묶음 |

### 책임 경계

| Service | 운영 기준 |
| --- | --- |
| `StepFunctionsDailyBatchExecutionService` | Fargate와 AWS Paper 기본 trigger 경로 |
| `DailyBatchService` | local-file 검증과 복구용 |
| Connector Refresh Service | Local View에서만 subprocess 사용 가능 |
| Submit · Slack Service | 실제 외부 요청 가능 · 임의 실행 금지 |

Service 변경 시 성공, 실패, 차단 경로를 분리해서 확인한다.

## Repository

| 파일 · 경로 | 역할 |
| --- | --- |
| `repository/*.java` | JPA와 JdbcTemplate 기반 DB 조회 |
| `ConnectorOrderRequestRepository.java` | 주문 목록 · 상세 · Refresh 대상 |
| `ReportRepository.java` | 리포트 summary · stat · trade detail |
| Daily Batch repository | `ops` Batch Run과 Step Log 조회 |
| Strategy repository | `execution` · `decision` · `research` 조회 |

### 변경 시 확인

| 항목 | 값 |
| --- | --- |
| Schema | `ops` · `execution` · `decision` · `research` · `connector` |
| SQL | 추정 컬럼명 사용 금지 |
| search path | 기존 unqualified SQL 해석 순서 |
| Mapping | Entity · DTO 타입 정합 |
| 결과 의미 | 화면 status와 집계 기준 유지 |

신규 운영 SQL과 진단 SQL은 가능한 한 schema-qualified 이름을 사용한다.

## Entity

| 경로 | 역할 |
| --- | --- |
| `src/main/java/my/portfolio/port_view/entity/*.java` | DB row와 JPA Entity 매핑 |

주요 영역:

- balance
- connector snapshot
- connector order request
- fill
- event
- position

Entity 변경 시 실제 table과 column을 확인한다.

DDL 또는 컬럼을 추정해서 Entity를 수정하지 않는다.

## DTO

| 경로 | 역할 |
| --- | --- |
| `dto/dashboard/*.java` | Dashboard 카드 · 포지션 · 최근 주문 |
| `dto/position/*.java` | 포지션 목록 · 상세 · insight |
| `dto/order/*.java` | 주문 목록 · 상세 · fill · event |
| `dto/dailybatch/*.java` | Batch Run · Step Log · 실행 option |
| `dto/strategy/*.java` | 실행 계획 · Daily Signal · Report |

DTO 변경 시 Service 조립 코드와 Thymeleaf field를 함께 확인한다.

화면 표시 전용 값은 DTO에 두고 Entity를 직접 노출하지 않는다.

## Config

| 파일 · 경로 | 역할 |
| --- | --- |
| `AsyncConfig.java` | 비동기 executor |
| `ConnectorProperties.java` | Connector URL과 API 설정 |
| `DailyBatchProperties.java` | backend · gate · Step Functions 설정 |
| `PortfolioViewProperties.java` | 기본 계좌와 화면 limit |
| `SlackProperties.java` | Slack 활성화와 webhook 설정 |
| `SnapshotRefreshProperties.java` | Snapshot Refresh 설정 |
| `config/*.java` | typed configuration 묶음 |

설정 변경 시 아래를 함께 확인한다.

1. `application.properties`
2. profile별 properties
3. `@ConfigurationProperties`
4. README
5. container 환경변수 이름

## Util

| 경로 | 역할 |
| --- | --- |
| `src/main/java/my/portfolio/port_view/util/*.java` | 금액 · 날짜 · label · CSS class · 계좌 해석 |

Util 변경 시 전체 화면의 표시값과 CSS class 영향을 확인한다.

계좌번호를 화면이나 로그에 원문으로 출력하지 않는다.

## Templates

| 파일 · 경로 | 역할 |
| --- | --- |
| `templates/fragments/sidebar.html` | 공통 navigation |
| `templates/pages/dashboard.html` | Dashboard |
| `templates/pages/balance-summary.html` | Balance |
| `templates/pages/positions.html` | Position 목록 |
| `templates/pages/position-detail.html` | Position 상세 |
| `templates/pages/orders.html` | Order 목록 |
| `templates/pages/order-detail.html` | Order 상세 |
| `templates/pages/daily_batch.html` | Batch 상태와 실행 action |
| `templates/pages/*.html` | Strategy · Daily · Report 포함 화면 묶음 |

### 변경 시 확인

| 항목 | 값 |
| --- | --- |
| Model | Controller attribute 이름 |
| Link | 기존 URL |
| Form | action endpoint와 method |
| Gate | safe와 approval 버튼 조건 |
| 상태 | badge와 CSS class |
| 보안 | 계좌 · ARN · 주문 식별자 redaction |

`daily_batch.html`의 실행 버튼은 화면 확인 목적으로 임의 클릭하지 않는다.

## Static

| 경로 | 역할 |
| --- | --- |
| `static/css/layout/app-layout.css` | 공통 layout |
| `static/css/pages/*.css` | 화면별 card · grid · status 스타일 |
| `static/*` | 정적 자원 묶음 |

공통 CSS 변경은 여러 화면에 영향을 주므로 변경 범위를 작게 유지한다.

Template class 변경 시 대응 CSS를 함께 확인한다.

## Resources

| 파일 · 경로 | 역할 |
| --- | --- |
| `application.properties` | 공통 server · DB · View · Connector · Batch · Slack 설정 |
| `application-aws-paper.properties` | AWS Paper profile override |
| `application-local.properties.example` | 민감정보 없는 Local 설정 예시 |
| `src/main/resources/*` | profile별 설정과 View resource |

### 주요 설정 범주

| 설정 | 역할 |
| --- | --- |
| `spring.datasource.*` | PostgreSQL |
| `spring.jpa.*` | JPA · Hibernate |
| `portfolio.view.*` | View 기본값 |
| `connector.*` | Connector 연동 |
| `portfolio.batch.*` | backend와 실행 gate |
| `portfolio.snapshot-refresh.*` | Snapshot Refresh |
| `slack.*` | Local View Slack |

실제 password, webhook, account, ARN은 저장소에 기록하지 않는다.

## Database Contract

| 항목 | 값 |
| --- | --- |
| Database | `portfolio` |
| View DB user | `view_app` |
| Connector subprocess user | `marketconnector_app` |
| SQL 해석 | Hikari `search_path` |
| Batch Run | `ops.strategy_daily_batch_run` |
| Step Log | `ops.strategy_daily_batch_step_log` |

기본 schema 순서:

```text
ops, execution, decision, research, connector,
preprocessor, interest, reference, legacy, public
```

`view_app`에 connector 쓰기 권한을 추가해 user 분리를 우회하지 않는다.

## Tests

| 파일 · 경로 | 역할 |
| --- | --- |
| `PortViewApplicationTests.java` | Spring context smoke test |
| `src/test/**/*.java` | Controller · Service · Repository 관련 테스트 |

변경 범위별 최소 검증:

| 변경 | 검증 |
| --- | --- |
| Java | compile 또는 관련 test |
| Controller | mapping · gate |
| Service | 성공 · 실패 · 차단 |
| Repository | query · schema · mapping |
| Template | parse · Model field |
| Properties | binding · 기본값 |
| Docker | build context · runtime |

외부 DB나 API 의존 때문에 실행하지 못한 검증은 완료로 기록하지 않는다.

## Documents

| 문서 | 역할 |
| --- | --- |
| `AGENTS.md` | port-view 작업 규칙 |
| `README.md` | 현재 구조와 운영 AS-IS |
| `CHANGELOG.md` | port-view 변경 이력 |
| `docs/source-file-catalog.md` | 주요 파일과 책임 |

구조 · 설정 · Daily Batch · 보안 · 리팩토링 기준은 README와 이 카탈로그에 통합한다.

날짜별 `docs/worklog/*.md`는 신규 생성하지 않는다.

코드와 문서의 변경 이력은 `CHANGELOG.md`에 남기고, 상세 기준은 관련 `docs` 문서에 반영한다.

## 외부 의존 모듈

| 모듈 | 관계 |
| --- | --- |
| `port-marketconnector` | 계좌 · 주문 · 체결 |
| `port-interest-crawler` | 원천 데이터 수집 |
| `port-interest-preprocessor` | 전처리 |
| `port_strategy_common` | 공통 전략 모델 |
| `port_strategy_research` | 전략 연구 |
| `port_strategy_decision` | 전략 판단 |
| `port_strategy_execution` | 계획과 주문 실행 |

다른 MS의 내부 코드와 문서는 port-view 변경 범위에 자동 포함하지 않는다.

## 카탈로그 갱신 조건

| 변경 | 처리 |
| --- | --- |
| 주요 파일 신규 생성 · 삭제 · 이름 변경 | 갱신 |
| package · template · CSS 디렉터리 변경 | 갱신 |
| Controller · Service · Repository 책임 변경 | 갱신 |
| 설정 파일 · Docker · build 역할 변경 | 갱신 |
| 문서 신규 생성 · 삭제 · 역할 변경 | 갱신 |
| 내부 구현만 변경 · 책임 동일 | 생략 가능 |

카탈로그 갱신 시 전체 repository inventory를 새로 만들지 않는다.

변경된 영역과 인접 항목만 확인한다.
