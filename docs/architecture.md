# Architecture

이 문서는 `port-view` 모듈의 현재 구조를 설명합니다. 내용은 저장소 내부 Java/Thymeleaf/config 파일에서 확인 가능한 범위로 제한합니다.

## 전체 구조

`port-view`는 Spring MVC + Thymeleaf 기반 view application입니다. 주요 관심사는 portfolio 관련 데이터를 조회해 화면 DTO로 조립하고, Daily Batch와 Strategy Execution 일부 액션을 제공하는 것입니다.

주요 계층:

- Controller
- Service
- Repository
- DTO
- Entity
- Config
- Util
- Thymeleaf templates/static CSS

## Controller

Controller는 HTTP route를 받고, request parameter/path variable을 service 호출에 필요한 값으로 정리한 뒤 Model에 attribute를 추가하고 view name을 반환합니다.

주요 Controller:

- `DashboardController`: `/`, `/dashboard`
- `BalanceController`: `/balance-summary`
- `PositionController`: `/positions`, `/positions/{tickerCode}`
- `OrderController`: `/orders`, `/orders/{id}`
- `StrategyExecutionViewController`: 전략 실행 계획 목록/상세, 주문 submit
- `StrategyReportController`: 전략 리포트 latest/detail
- `StrategyDailyViewController`: Daily Run latest/detail
- `DailyBatchController`: Daily Batch 조회/실행/재실행/Slack 테스트
- `HoldingsController`, `TradeOrdersController`, `GetPriceRealTimeController`: legacy 또는 보조 조회 성격의 화면

대부분의 신규 화면은 `ViewNames`를 통해 `pages/*` template을 반환합니다. 일부 화면은 아직 루트 template 이름을 직접 반환하거나 `ViewNames`에 루트 template이 등록되어 있습니다.

## Service

Service는 화면 DTO 조립, 데이터 가공, 외부 API 호출, batch orchestration을 담당합니다.

주요 Service:

- `DashboardService`: dashboard 화면 데이터 조립
- `BalanceService`: 잔고 요약 계산
- `PositionService`: 보유 종목 목록/상세 DTO 조립
- `OrderService`: 주문 목록/상세, 주문 체인/이벤트/체결 DTO 조립
- `StrategyExecutionViewService`: 전략 실행 계획 화면 DTO 조립
- `StrategyExecutionSubmitService`: 전략 실행 주문 제출 처리
- `StrategyDailyViewService`: Daily Run 화면 데이터 조회
- `ReportService`: 전략 리포트 DTO 조립
- `DailyBatchService`: Daily Batch run/step 생성, 실행, 상태 갱신
- `DailyBatchAsyncService`: async executor를 통한 batch 실행 위임
- `SlackNotificationService`: Slack 메시지 조립/전송
- `ConnectorSnapshotRefreshService`: snapshot stale 여부 확인 후 외부 connector script 실행
- `GetPriceRealTimeService`: 실시간 가격 API 호출
- `HoldingsService`, `TradeOrdersService`: legacy table 조회

## Repository

Repository는 두 방식이 혼재합니다.

- Spring Data JPA
  - Entity 기반 단순 조회와 일부 native query
  - 예: `BalanceSummaryRepository`, `ConnectorOrderRequestRepository`, `ConnectorPositionSnapshotRepository`
- JdbcTemplate
  - 복잡한 화면 조회, 집계, batch run/step 상태 갱신
  - 예: `DailyBatchRepository`, `StrategyExecutionQueryRepository`, `StrategyDailyViewRepository`, `ReportRepository`, `PositionInsightRepository`

`StrategyExecutionSubmitService`는 service 내부에서 직접 `JdbcTemplate`을 사용합니다. 추후 repository 분리 후보입니다.

## DTO

DTO는 화면 표시 목적이 강합니다.

- Lombok mutable class DTO
  - Dashboard, Order, Position 계열에서 주로 사용
- Java record DTO
  - Daily Batch, Strategy Daily, Strategy Execution, Report 계열에서 많이 사용

일부 DTO에는 label 변환 helper method가 포함되어 있습니다. 일부 화면 label은 service에서 채워지고, 일부는 Thymeleaf에서 static util을 직접 호출합니다.

## Entity

Entity는 JPA 조회 대상 테이블을 표현합니다.

주요 Entity:

- `BalanceSummary`
- `Holdings`
- `TradeOrders`
- `ConnectorBalanceSnapshot`
- `ConnectorPositionSnapshot`
- `ConnectorOrderRequest`
- `ConnectorOrderEvent`
- `ConnectorFill`

Daily Batch, Strategy Daily, Report 계열의 복잡한 조회는 Entity보다 JdbcTemplate DTO mapping을 주로 사용합니다.

## Config

주요 설정 객체:

- `PortfolioViewProperties`
  - 화면 기본 계좌번호, dashboard/orders/positions/strategy execution limit
- `ConnectorProperties`
  - Connector base URL과 API path
- `DailyBatchProperties`
  - Python 실행 파일, workspace root, 외부 모듈 디렉터리, timeout, log tail
- `SnapshotRefreshProperties`
  - snapshot refresh enable/stale/timeout/script 설정
- `AsyncConfig`
  - Daily Batch async executor

## Util

주요 util:

- `ViewFormatUtils`: 숫자, 금액, 퍼센트, 날짜/시간 표시 포맷
- `ViewTextUtils`: null/blank/uppercase 처리
- `OrderLabelUtils`: 주문 관련 label/class 변환
- `ConnectorLabelUtils`: connector domain label 변환
- `StrategyExecutionLabelUtils`: 전략 실행 label 변환
- `DailyBatchLabelUtils`: batch run/step/status/duration label 변환
- `ProfitClassUtils`: 수익/손실 CSS class 변환
- `AccountNoResolver`: 요청 계좌번호가 없을 때 기본 계좌번호 적용
- `ViewNames`: Thymeleaf template 이름 상수

## 화면 요청 흐름

일반 조회 화면 흐름:

1. Browser가 Controller route 호출
2. Controller가 request parameter/path variable 수신
3. `AccountNoResolver` 등으로 조회 기준값 정리
4. Service 호출
5. Service가 Repository/API 결과를 DTO로 조립
6. Controller가 Model attribute 추가
7. Thymeleaf template 렌더링

예: `/orders/{id}`

1. `OrderController`
2. `OrderService.getOrderDetail`
3. `ConnectorOrderRequestRepository`, `ConnectorOrderEventRepository`, `ConnectorFillRepository`
4. `OrderDetailPageDTO`
5. `pages/order-detail`

## Connector 연동 흐름

Connector 연동은 두 형태가 있습니다.

- REST API 호출
  - `GetPriceRealTimeService`
  - `StrategyExecutionSubmitService`
  - `ConnectorProperties`의 base URL/API path 사용
- DB snapshot 조회
  - Connector가 적재한 balance/position/order/fill/event table을 JPA Repository로 조회

Snapshot stale 확인이 필요한 경우 `ConnectorSnapshotRefreshService`가 외부 connector script를 실행하는 구조가 있습니다.

## Daily Batch 실행 흐름

Daily Batch 흐름:

1. `DailyBatchController`에서 실행 요청 수신
2. `DailyBatchService`가 batch run row 생성
3. `DailyBatchAsyncService`가 async executor로 실행 위임
4. `DailyBatchService.executeDailyPipeline`이 step 목록 생성
5. 각 step을 외부 process로 실행
6. stdout/stderr와 exit code를 step log에 기록
7. run 상태를 성공/실패 등으로 갱신
8. 필요 시 `SlackNotificationService`로 summary 전송

상태와 테이블 설명은 `docs/daily-batch.md`를 참고합니다.

## Strategy Execution 조회/승인 흐름

조회 흐름:

1. `/strategy/execution/plans` 또는 `/strategy/execution/plans/{planId}` 호출
2. `StrategyExecutionViewController`
3. `StrategyExecutionViewService`
4. `StrategyExecutionQueryRepository`
5. `StrategyExecutionPlanPageDto`, `StrategyExecutionPlanDto`, `StrategyExecutionOrderDto`
6. `pages/strategy_plans` 또는 `pages/strategy_detail`

주문 submit 흐름:

1. `/strategy/execution/orders/{orderId}/submit` POST
2. `StrategyExecutionViewController`
3. `StrategyExecutionSubmitService`
4. 대상 order 조회 및 검증
5. Connector API 호출
6. DB 상태/응답 갱신
7. 전략 실행 계획 상세 화면으로 redirect

## 배포 구조 (ECS Fargate)

port-view는 ECS Fargate Service로 운영합니다. 컨테이너 이미지는 Docker로 빌드해 ECR로 push하고, ECS Task Definition revision을 등록한 뒤 ECS Service가 신규 revision으로 rollout하는 흐름을 사용합니다.

Fargate 관점에서 View의 책임 경계:

- View 컨테이너 안에서 Python subprocess로 Daily Batch를 직접 실행하지 않습니다.
- Daily Batch 실행 책임은 AWS Step Functions, EventBridge Scheduler, ECS RunTask, SSM RunCommand, AWS Batch, Lambda 쪽에 있습니다.
- port-view는 조회 / 승인 / 트리거 UI만 담당합니다.
- `local-file` backend는 운영자 로컬 검증/복구용으로만 보존되며 Fargate에서는 사용하지 않습니다.

Fargate 관점에서 View 컨테이너 안의 흐름은 기본 조회 흐름(Controller → Service → Repository → DTO → Thymeleaf)과 Step Functions `StartExecution` 트리거 흐름 두 가지로 요약됩니다. `StartExecution` 트리거 흐름은 `StepFunctionsDailyBatchExecutionService`가 AWS SDK v2 Step Functions client를 사용해 수행하며, 실제 Step 1~17 실행 결과는 Step Functions state machine + ECS RunTask + SSM RunCommand + AWS Batch가 담당합니다.

ALB, 인증, 접근 제한 등의 세부 운영 항목은 저장소 밖의 AWS 운영 영역에서 관리하며, 본 저장소는 다음 원칙만 유지합니다.

- 실제 계정 ID, 실제 IAM Role ARN, 실제 secret ARN, 실제 state machine ARN, ALB DNS 원문은 저장소에 기록하지 않습니다.
- 필요한 경우 `[REDACTED]` / `<STATE_MACHINE_ARN>` / `<APPROVAL_STATE_MACHINE_ARN>` / `<ALB_ENDPOINT>` / `<ECR_IMAGE_URI>` placeholder를 사용합니다.
