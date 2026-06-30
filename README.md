# port-view

Spring MVC 기반 Portfolio View 모듈입니다. 포트폴리오 현황, 잔고, 보유 종목, 주문, 전략 실행 계획, 전략 리포트, Daily Batch 실행 상태를 Thymeleaf 화면으로 조회하고 일부 실행 액션을 제공합니다.

이 문서는 현재 코드에서 확인 가능한 구조를 기준으로 작성되었습니다. 외부 모듈의 세부 동작은 이 저장소 범위 밖이므로 요약 수준으로만 다룹니다.

## 기술 스택

- Java 25
- Spring Boot 4.1.0-SNAPSHOT
- Spring MVC
- Thymeleaf
- Spring Data JPA
- JdbcTemplate
- PostgreSQL
- Lombok
- Maven Wrapper

## 주요 화면

- Dashboard: `/`, `/dashboard`
  - 계좌 요약, 최근 주문, 보유 종목, 최신 전략 실행/리포트 요약을 DB 기준으로 표시합니다.
  - `portfolio.snapshot-refresh.enabled=true` + `portfolio.dashboard.snapshot-refresh-enabled=true` 조합이면 진입 시 AWS Paper 계좌 Snapshot Refresh가 활성화될 수 있습니다.
- Balance: `/balance-summary`
  - 계좌 잔고 요약과 평가금액 관련 정보를 표시합니다.
  - `portfolio.snapshot-refresh.enabled=true`이면 진입 시 계좌 스냅샷 refresh 후 잔고를 조회합니다.
- Positions: `/positions`, `/positions/{tickerCode}`
  - 보유 종목 목록과 종목 상세 화면을 제공합니다.
  - 목록 진입 시 Snapshot Refresh 후 DB 기준으로 보유 종목을 조회하고, 상세 화면은 stale 기준으로 refresh를 수행합니다.
- Orders: `/orders`, `/orders/{id}`
  - Connector 주문 요청, 주문 체인, 이벤트, 체결 정보를 조회합니다.
- Strategy Execution: `/strategy/execution/plans`, `/strategy/execution/plans/{planId}`
  - 전략 실행 계획과 주문 후보를 조회하고, 주문 제출 액션을 제공합니다.
- Strategy Report: `/strategy/reports/latest`, `/strategy/reports/{runId}`
  - 백테스트 리포트 요약, 통계, 거래 상세를 표시합니다.
- Strategy Daily: `/strategy/daily/latest`, `/strategy/daily/{dailyRunId}`
  - Daily Run, signal, position decision 조회 화면입니다.
- Daily Batch: `/daily-batch`, `/daily-batch/{batchRunId}`
  - Daily Batch 실행 이력, 단계별 실행 결과, status 색상, 로그 보기 UI를 제공합니다.
  - Local File 기반 Step 1~17 실행 gate를 화면에서 확인할 수 있고, 수동 실행/재실행/Slack 테스트 액션을 제공합니다.
  - 전체 1~17 또는 Step 10/11/12 포함 범위는 AWS Paper 주문 제출 가능성이 있으므로, 운영자는 실행 버튼 클릭 전 실행 범위와 gate를 반드시 확인해야 합니다.

## 패키지 구조 요약

- `controller`: Spring MVC Controller. 요청 파라미터를 해석하고 service 결과를 Model에 담아 Thymeleaf view를 반환합니다.
- `service`: 화면 DTO 조립, 외부 Connector 호출, Daily Batch 실행, Slack 알림 등 application service 역할을 담당합니다.
- `repository`: JPA Repository와 JdbcTemplate 기반 query repository가 함께 존재합니다.
- `dto`: 화면 표시용 DTO입니다. Lombok class DTO와 Java record DTO가 혼재되어 있습니다.
- `entity`: JPA Entity입니다. balance, holdings, trade orders, connector snapshot/order/fill/event 계열이 있습니다.
- `config`: `@ConfigurationProperties`, async executor 등 설정 객체입니다.
- `util`: 화면 포맷팅, label 변환, account resolver, CSS class helper입니다.
- `common`: Thymeleaf view 이름 상수(`ViewNames`)를 관리합니다.

## 소스 파일 카탈로그

AWS Migration 전 초기 정리를 위해 주요 소스/설정/문서 파일의 역할을 [docs/source-file-catalog.md](docs/source-file-catalog.md)에 정리했습니다.
파일 삭제 없이 Java/Thymeleaf/CSS/properties/docs 파일의 책임과 운영 주의사항을 한글로 기록합니다.

## 실행 방법

로컬 실행 전 PostgreSQL, Connector API, Daily Batch에서 호출하는 외부 모듈 경로가 준비되어 있어야 합니다.

```bash
./mvnw spring-boot:run
```

Windows PowerShell에서는 다음을 사용할 수 있습니다.

```powershell
.\mvnw.cmd spring-boot:run
```

기본 서버 포트는 `application.properties`의 `server.port` 설정을 따릅니다.

### AWS Paper Local View 실행

AWS Paper 환경의 RDS와 secret을 사용해서 Local View를 띄우는 경우에는 다음 전제가 충족되어 있어야 합니다.

- AWS Paper RDS로 향하는 port forwarding(예: `127.0.0.1:15433`)이 별도 창에서 미리 열려 있어야 합니다.
- 로컬 secret loader 또는 환경변수로 DB/KIS secret과 계좌번호가 준비되어 있어야 합니다. 실제 값은 저장소에 기록하지 않고 `[REDACTED]` 처리합니다.

View 실행은 운영자 로컬 도구 폴더의 starter 스크립트 한 줄로 가능합니다.

```powershell
C:\Workspaces\portfolio-local-env\Start-PortfolioViewAwsPaperBatch.ps1
```

실행 스크립트 역할은 다음과 같습니다(스크립트 자체는 운영자 로컬 도구 폴더에 위치하며 본 저장소 범위 밖입니다).

- `Load-PortfolioViewAwsPaperBatchEnv.ps1`: UTF-8 콘솔, DB/KIS secret, `aws-paper` profile, 기본 계좌, Snapshot Refresh, Daily Batch gate 환경변수를 로드합니다.
- `Start-PortfolioViewAwsPaperBatch.ps1`: 위 Load 파일을 dot-source한 뒤 `port-view`에서 `mvnw spring-boot:run -Dspring-boot.run.profiles=aws-paper`를 실행합니다.

## 빌드 방법

```bash
./mvnw clean package
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean package
```

## 외부 의존 모듈

Daily Batch와 Connector 연동은 외부 프로젝트 및 API에 의존합니다.

- `port-marketconnector`
- `port-interest-crawler`
- `port-interest-preprocessor`
- `port_strategy_research`
- `port_strategy_decision`
- `port_strategy_execution`

위 모듈의 실제 경로, 실행 명령, 운영 환경별 설정은 환경변수 또는 local config로 분리하는 방향이 적합합니다.

## 주요 설정 항목

주요 설정은 `src/main/resources/application.properties`와 `config` 패키지의 `@ConfigurationProperties` 객체에서 확인할 수 있습니다.

- `spring.datasource.*`: PostgreSQL 연결 설정. DB 접속정보는 `INTEREST_DB_*` 환경변수로 주입합니다.
- `spring.jpa.*`: JPA/Hibernate 설정
- `portfolio.view.*`: 화면 기본 계좌번호와 화면별 limit 설정
- `connector.*`: Connector base URL과 API path 설정
- `portfolio.batch.*`: Daily Batch 실행 경로, Python 실행 파일, timeout, log tail 설정
- `portfolio.snapshot-refresh.*`: snapshot stale 여부와 refresh 실행 설정
- `slack.*`: Slack 알림 활성화 여부와 webhook 설정

민감정보 값은 저장소에 직접 두지 않고 환경변수 또는 로컬 전용 설정으로 분리해야 합니다.

### Snapshot Refresh 설정

Dashboard / Balance / Positions 진입 시 계좌 스냅샷을 새로 받아오는 동작은 다음 설정으로 제어합니다.

- `portfolio.snapshot-refresh.enabled`: Snapshot Refresh 전체 활성 여부
- `portfolio.dashboard.snapshot-refresh-enabled`: Dashboard 진입 시 Snapshot Refresh 사용 여부
- `portfolio.snapshot-refresh.stale-minutes`: 스냅샷을 stale로 판정하는 분 단위 임계값
- `portfolio.snapshot-refresh.timeout-seconds`: refresh subprocess 최대 실행 시간
- `portfolio.snapshot-refresh.marketconnector-dir`: `connector_balance.py`를 실행할 MarketConnector 디렉터리 경로
- `portfolio.snapshot-refresh.balance-script-name`: 실행할 balance 스크립트 파일명
- `portfolio.snapshot-refresh.python-executable`: 사용할 Python 실행 파일 경로

### DB user 분리

Spring View datasource와 Connector subprocess는 서로 다른 DB user를 사용합니다.

- Spring View datasource는 `view_app`을 사용합니다.
- `ConnectorSnapshotRefreshService`가 실행하는 `connector_balance.py` subprocess는 `marketconnector_app`을 사용합니다.
- `view_app`에 connector 쓰기 권한을 부여하는 방식이 아니라, subprocess의 DB user 자체를 분리한 구조입니다.

### Daily Batch gate

Daily Batch 화면에서 실행 가능한 step 범위는 다음 gate로 제한합니다.

- `portfolio.batch.execution-enabled`: 화면에서 Daily Batch 실행 액션 허용 여부
- `portfolio.batch.local-file-execution-enabled`: Local File 기반 실행 허용 여부
- `portfolio.batch.full-pipeline-execution-enabled`: 전체 1~17 실행 허용 여부
- `portfolio.batch.paper-order-enabled`: Paper 주문 제출 가능 step 허용 여부
- `portfolio.batch.min-executable-step-order`: 실행 허용 최소 step order
- `portfolio.batch.max-executable-step-order`: 실행 허용 최대 step order

### 안전 주의

`BATCH_PAPER_ORDER_ENABLED=true`이면 Daily Batch 화면에서 Step 12 이상 또는 전체 1~17 범위 실행 시 실제 AWS Paper 주문이 제출될 수 있습니다. 운영자는 버튼 클릭 전 실행 범위와 gate를 반드시 확인해야 합니다.

DB 접속 환경변수:

- `INTEREST_DB_HOST`: PostgreSQL host. 기본값은 `localhost`
- `INTEREST_DB_PORT`: PostgreSQL port. 기본값은 `5433`
- `INTEREST_DB_NAME`: PostgreSQL database name. 기본값은 `portfolio`
- `PORTFOLIO_DB_NAME`: 별도 환경변수로 분리하는 경우 PostgreSQL database name. 기본값은 `portfolio`
- `INTEREST_DB_USER`: PostgreSQL username. 기본값은 `postgres`
- `INTEREST_DB_PASSWORD`: PostgreSQL password. 기본값 없음

DB schema 구성:

- AWS Migration 준비 관점에서 단일 PostgreSQL database `portfolio`와 schema-per-domain 구조를 사용합니다.
- domain schema는 `reference`, `interest`, `preprocessor`, `research`, `decision`, `execution`, `connector`, `ops`, `legacy`, `public`입니다.
- port-view는 여러 domain schema를 통합 조회하는 운영 콘솔이므로 가장 넓은 `search_path`를 사용합니다.
- `spring.datasource.hikari.connection-init-sql`로 `search_path`를 `ops, execution, decision, research, connector, preprocessor, interest, reference, legacy, public` 순서로 설정합니다.
- schema-per-domain 전환 후에도 기존 SQL은 명시 schema prefix 없이 위 `search_path` 기반으로 동작합니다.
- Dashboard, Balance, Holdings, Strategy Plan, Daily Batch, Report 화면 조회 검증이 완료된 구조입니다.

## Daily Batch 요약

Daily Batch는 여러 외부 모듈의 Python command를 순차 실행하고, 실행 이력을 `strategy_daily_batch_run`, step 로그를 `strategy_daily_batch_step_log`에 기록하는 흐름입니다.

지원되는 주요 액션:

- 전체 Daily Pipeline 실행
- 특정 step부터 재실행
- 실패 step 재실행
- Intraday Monitor 단독 실행
- Slack 테스트 메시지 전송
- Daily Batch Slack summary 전송

상세 내용은 [docs/daily-batch.md](docs/daily-batch.md)를 참고합니다.

### Daily Batch 실행 backend

Daily Batch 실행 backend는 두 가지로 분리되어 있습니다. `portfolio.batch.execution-mode` 값으로 선택합니다.

- `local-file`: 운영자 로컬 검증 도구로 보존하는 backend. Local View 버튼이 ProcessBuilder로 로컬 source(예: `C:/Workspaces/port-marketconnector`)의 Python command를 직접 실행해 AWS Paper DB에 결과를 저장합니다. Fargate에서는 사용하지 않습니다.
- `aws-stepfunctions`: View가 Python subprocess나 로컬 source를 직접 실행하지 않고 AWS Step Functions `StartExecution`만 호출하는 backend입니다. 실제 Step 1~17 실행은 Step Functions state machine + ECS RunTask + SSM RunCommand + AWS Batch 가 담당합니다.

`aws-stepfunctions` backend 는 `StepFunctionsDailyBatchExecutionService`가 담당하고, Daily Batch 화면에 `AWS Step 1~11` safe trigger 버튼과 `AWS Step 12~17` 승인 실행 버튼을 분리해 노출합니다. 기본은 `allowPaperOrderExecute=false` 기준이며, Step 12~17 주문성 구간은 별도 approval / preflight / paper-order gate 뒤에서만 활성화됩니다.

Controller endpoint(기존 endpoint 이름은 그대로 유지):

- `POST /daily-batch/aws-stepfunctions/start-range`: Step 1~11 safe trigger
- `POST /daily-batch/aws-stepfunctions/start-approval-range`: Step 12~17 승인 실행

성공 시 `executionName`과 account-id를 redaction한 `executionArn`을 flash message로 표시합니다. 승인 실행은 `requestedBy=VIEW_APPROVAL_BUTTON`, `allowPaperOrderExecute=true`, `paperOrderEnabled=true` payload를 사용합니다.

State machine 식별자는 일반 workflow와 approval workflow를 분리해 환경변수로 주입합니다. 실제 ARN 값은 본 저장소 문서에 기록하지 않고 `[REDACTED]` 또는 placeholder로 표기합니다.

- `portfolio.batch.aws-stepfunctions-region`: Step Functions 호출 region (예: `ap-northeast-2`)
- `portfolio.batch.aws-stepfunctions-state-machine-arn`: 일반 workflow state machine ARN (`portfolio-paper-daily-step1-17-approval`)
- `portfolio.batch.aws-stepfunctions-approval-state-machine-arn`: 승인형 workflow state machine ARN (`portfolio-paper-daily-step12-17-approval`). 승인 ARN이 비어 있으면 승인형 실행은 서비스 레벨에서 차단됩니다.
- `portfolio.batch.aws-stepfunctions-execution-name-prefix`: `StartExecution` `name` prefix
- `portfolio.batch.aws-stepfunctions-start-enabled`: aws-stepfunctions backend 사용 허용 여부
- `portfolio.batch.aws-stepfunctions-step-start-enabled`: AWS Step 1~11 safe trigger 버튼 활성 허용 여부

`StartExecution` payload의 타입은 다음을 따릅니다(Step Functions Choice `BooleanEquals` 조건과 정합).

- `allowPaperOrderExecute` · `paperOrderEnabled`: boolean JSON
- `fromStepOrder` · `toStepOrder` · `startStep` · `endStep`: numeric JSON
- `runDate`: Asia/Seoul 기준 yyyy-MM-dd 문자열
- `environment` · `dbTarget` · `source` · `requestedBy` · `requestedFrom` · `fromStepCode` · `toStepCode`: 문자열

Fargate에서는 `portfolio.batch.local-file-execution-enabled=false`, `portfolio.batch.paper-order-enabled=false`, `portfolio.batch.full-pipeline-execution-enabled=false`를 기본값으로 권장하고, Step 1~11 safe trigger부터 단계적으로 활성화합니다. Step 12 이상 또는 전체 1~17 범위는 운영자가 별도 gate를 명시적으로 ENABLE한 뒤에만 허용됩니다.

운영자 로컬 도구 폴더(`C:\Workspaces\portfolio-local-env\`)에는 backend별 wrapper 2종이 분리되어 있습니다. 본 저장소 범위 밖이라 스크립트 본체는 두지 않고 파일명만 참고로 적습니다.

- `Start-PortfolioViewAwsPaperLocalFile.ps1` + `Load-PortfolioViewAwsPaperLocalFileEnv.ps1`: `local-file` backend 기준으로 View를 띄웁니다. `PORTFOLIO_BATCH_EXECUTION_MODE=local-file`, `PORTFOLIO_BATCH_LOCAL_FILE_EXECUTION_ENABLED=true`, `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_START_ENABLED=false`로 설정됩니다.
- `Start-PortfolioViewAwsPaperStepFunctions.ps1` + `Load-PortfolioViewAwsPaperStepFunctionsEnv.ps1`: `aws-stepfunctions` backend 기준으로 View를 띄웁니다. `PORTFOLIO_BATCH_EXECUTION_MODE=aws-stepfunctions`, `PORTFOLIO_BATCH_LOCAL_FILE_EXECUTION_ENABLED=false`, 일반 + approval ARN 환경변수가 모두 set 되어야 합니다.

두 wrapper 모두 `aws-paper` profile + Step 1~17 전체 실행 가능 gate로 구성되며, safe-only 검증용이 아니라 운영자 선택형 전체 실행 wrapper입니다.

## Slack 알림 요약

`SlackNotificationService`는 Daily Batch 결과, Daily Run 요약, 전략 실행 요약, 잔고/보유 요약을 텍스트 메시지로 조립해 Slack webhook으로 전송합니다.

Webhook URL은 문서나 코드에 직접 기록하지 않고 환경변수 또는 local config를 통해 주입해야 합니다.

## 보안 주의사항

다음 유형은 민감정보 또는 환경 의존 정보로 취급합니다.

- DB password
- Slack webhook URL
- 실제 계좌번호
- Connector URL
- 외부 프로젝트 절대 경로
- 토큰/API key 유형의 값

자세한 원칙과 점검 목록은 [docs/security-notes.md](docs/security-notes.md)를 참고합니다.

## 향후 리팩토링 후보

- `DailyBatchService` 책임 분리
- `SlackNotificationService` 전송/메시지 조립/조회 책임 분리
- View 공통 util과 label util 정리
- `templates/pages`와 루트 template 병존 구조 정리 검토
- 루트 CSS와 `static/css/pages` CSS 병존 구조 정리 검토
- legacy/unused 의심 파일은 삭제가 아니라 사용 여부 검증 대상으로 관리

상세 후보는 [docs/refactoring-backlog.md](docs/refactoring-backlog.md)를 참고합니다.
