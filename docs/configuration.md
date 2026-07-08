# Configuration

이 문서는 `application.properties`와 config class에서 확인 가능한 설정 범주를 설명합니다. 실제 민감정보 값은 문서에 기록하지 않습니다.

## 설정 파일

기본 설정 파일:

- `src/main/resources/application.properties`

관련 config class:

- `PortfolioViewProperties`
- `ConnectorProperties`
- `DailyBatchProperties`
- `SnapshotRefreshProperties`
- `AsyncConfig`

## Server 설정

카테고리:

- `server.port`

Spring Boot 내장 서버 포트를 설정합니다.

## DB 설정

카테고리:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.datasource.driver-class-name`
- `spring.datasource.hikari.connection-init-sql`

역할:

- PostgreSQL 연결 정보
- JPA Repository와 JdbcTemplate Repository가 공통으로 사용
- 단일 DB `portfolio` 안의 domain schema를 `search_path` 기반으로 통합 조회

관리 원칙:

- DB password는 저장소에 커밋하지 않습니다.
- 로컬 개발용 값은 local config 또는 환경변수로 분리합니다.
- 운영 환경에서는 secret manager, environment variable, deployment config 등 외부 주입 방식을 사용합니다.
- password, token, account, webhook 실제 값은 문서에 기록하지 않습니다.

DB 이름과 schema 구성:

- 기본 DB name은 `portfolio`입니다.
- 기존 `INTEREST_DB_NAME`을 사용하는 경우 기본값은 `portfolio`입니다.
- 환경변수명을 도메인 중립적으로 분리해 `PORTFOLIO_DB_NAME`을 사용하는 경우에도 기본값은 `portfolio`로 둡니다.
- AWS Migration 준비 관점에서 단일 PostgreSQL DB `portfolio`와 schema-per-domain 구조를 사용합니다.
- domain schema는 `reference`, `interest`, `preprocessor`, `research`, `decision`, `execution`, `connector`, `ops`, `legacy`, `public`입니다.

port-view `search_path`:

- port-view는 Dashboard, Balance, Holdings, Strategy Plan, Daily Batch, Report 등 여러 domain schema를 통합 조회하는 운영 콘솔입니다.
- 따라서 Hikari `spring.datasource.hikari.connection-init-sql`로 가장 넓은 `search_path`를 설정합니다.
- 적용 순서는 `ops, execution, decision, research, connector, preprocessor, interest, reference, legacy, public`입니다.
- schema-per-domain 전환 후에도 기존 SQL은 명시 schema prefix 없이 위 `search_path` 기반으로 동작합니다.
- 로컬에서 Dashboard, Balance, Holdings, Strategy Plan, Daily Batch, Report 화면 조회 검증이 완료된 구성입니다.

## JPA 설정

카테고리:

- `spring.jpa.hibernate.ddl-auto`
- `spring.jpa.show-sql`
- `spring.jpa.properties.hibernate.format_sql`
- `spring.jpa.open-in-view`

역할:

- Hibernate DDL 처리 방식
- SQL 로그 출력 여부
- Open Session in View 사용 여부

운영 방향:

- 운영 환경에서는 SQL 로그 출력 여부를 별도 profile로 제어하는 것이 좋습니다.
- schema migration은 별도 migration 도구 또는 명시적 SQL 관리 정책을 두는 방향이 안전합니다.

## Portfolio View 설정

카테고리:

- `portfolio.view.account.default-account-no`
- `portfolio.view.dashboard.recent-order-limit`
- `portfolio.view.dashboard.recent-trade-limit`
- `portfolio.view.orders.default-limit`
- `portfolio.view.positions.default-limit`
- `portfolio.view.strategy-execution.default-plan-limit`
- `portfolio.view.strategy-execution.default-order-limit`

역할:

- 화면 기본 계좌번호
- 화면별 표시 limit

관리 원칙:

- 실제 계좌번호는 민감정보 후보로 취급합니다.
- 기본 계좌번호는 환경변수 또는 개인 local config로 분리하는 것이 좋습니다.
- 템플릿 내 fallback 문자열로 계좌번호가 반복되지 않도록 장기적으로 공통 resolver/model 처리로 모으는 방향을 권장합니다.

## Connector 설정

카테고리:

- `connector.base-url`
- `connector.api.balance-latest-path`
- `connector.api.positions-latest-path`
- `connector.api.orders-path`
- `connector.api.order-detail-path`
- `connector.api.realtime-price-path`
- `connector.api.eod-price-path`

역할:

- `port-marketconnector` 또는 관련 Connector API 호출 base URL/path
- 실시간 가격 조회, 주문 제출, view API 호출에 사용

관리 원칙:

- Connector URL은 환경별로 달라질 수 있으므로 profile 또는 환경변수로 분리합니다.
- 운영 환경 URL은 문서와 코드에 직접 적지 않습니다.
- token/API key가 도입되면 반드시 secret으로 관리합니다.

## Daily Batch 외부 경로 설정

카테고리:

- `portfolio.batch.python-executable`
- `portfolio.batch.workspace-root`
- `portfolio.batch.marketconnector-dir`
- `portfolio.batch.interest-crawler-dir`
- `portfolio.batch.preprocessor-dir`
- `portfolio.batch.execution-dir`
- `portfolio.batch.default-account-no`
- `portfolio.batch.environment`
- `portfolio.batch.command-timeout-minutes`
- `portfolio.batch.log-tail-length`

역할:

- Daily Batch에서 외부 Python command를 실행하기 위한 Python 실행 파일과 작업 디렉터리 설정
- batch 기본 계좌번호와 실행 환경 지정
- command timeout과 DB에 남길 log tail 길이 지정

외부 의존 모듈:

- `port-marketconnector`
- `port-interest-crawler`
- `port-interest-preprocessor`
- `port_strategy_research`
- `port_strategy_decision`
- `port_strategy_execution`

관리 원칙:

- 외부 절대 경로는 로컬 환경에 강하게 의존합니다.
- 운영/개발/개인 PC별 경로를 profile, environment variable, local config로 분리합니다.
- 실제 계좌번호는 문서나 Git tracked config에 기록하지 않습니다.

## Snapshot Refresh 설정

카테고리:

- `portfolio.snapshot-refresh.*`

역할:

- balance/position snapshot이 오래된 경우 외부 connector script를 실행할지 결정합니다.
- stale 기준 시간, timeout, Python 실행 파일, connector 작업 디렉터리, script name을 관리합니다.

관리 원칙:

- 외부 프로젝트 절대 경로는 환경별 설정으로 분리합니다.
- script 실행 권한과 timeout 정책을 운영 환경에 맞게 별도 검토합니다.

## Slack 설정

카테고리:

- `slack.enabled`
- `slack.webhook-url`

역할:

- Slack 알림 활성화 여부
- Slack incoming webhook URL

관리 원칙:

- webhook URL은 secret입니다.
- 문서, README, Git tracked config에 직접 기록하지 않습니다.
- 환경변수 또는 secret manager로 주입합니다.
- 알림 테스트 시에도 webhook URL을 로그에 출력하지 않습니다.

## 민감정보 관리 원칙

민감정보 또는 환경 의존 정보 후보:

- DB password
- Slack webhook URL
- 실제 계좌번호
- Connector URL
- 외부 프로젝트 절대 경로
- token/API key/authorization header 계열 값

원칙:

- Git tracked 파일에 실제 값을 기록하지 않습니다.
- README/docs에는 값이 아니라 설정 키와 관리 방법만 기록합니다.
- 로컬 개발자는 Git ignored local config 또는 환경변수를 사용합니다.
- 운영 환경은 secret manager/deployment config를 통해 주입합니다.

## 운영/로컬 분리 방향

권장 방향:

- `application-local.properties`: 개인 개발 환경 전용, Git ignore
- `application-dev.properties`: 개발 서버 공통값, 민감정보 제외
- `application-prod.properties`: 운영 기본값, 민감정보 제외
- 환경변수: DB password, Slack webhook, 계좌번호, endpoint secret
- 배포 시스템 secret: 운영 민감정보

Spring profile 예:

```bash
SPRING_PROFILES_ACTIVE=local
```

실제 값은 각 환경의 외부 설정으로 주입합니다.

## Fargate 안전 기본값

port-view를 ECS Fargate Service로 운영할 때 Daily Batch 관련 설정의 안전 기본값을 다음과 같이 권장합니다.

- `portfolio.batch.execution-mode=aws-stepfunctions`: Fargate 기준 기본 backend
- `portfolio.batch.local-file-execution-enabled=false`: Fargate에서는 로컬 subprocess 실행 미사용
- `portfolio.batch.paper-order-enabled=false`: 주문성 gate 초기 차단
- `portfolio.batch.full-pipeline-execution-enabled=false`: 전체 1~17 실행 초기 차단
- `portfolio.batch.max-executable-step-order=11`: safe 범위 상한을 Step 11로 제한

Step 12~17 주문성 구간은 approval workflow state machine과 별도 gate 뒤에서만 활성화됩니다. 운영자가 명시적으로 gate를 ENABLE하고 approval workflow ARN이 주입되어 있을 때만 승인 실행 endpoint가 활성화됩니다.

State machine ARN은 환경변수로 주입하고 실제 값은 문서에 기록하지 않습니다. 필요한 경우 다음 placeholder를 사용합니다.

- `<STATE_MACHINE_ARN>`: 일반 workflow(`portfolio-paper-daily-step1-17-approval`) ARN
- `<APPROVAL_STATE_MACHINE_ARN>`: 승인형 workflow(`portfolio-paper-daily-step12-17-approval`) ARN

Fargate 관점에서 datasource, Step Functions 설정, Snapshot Refresh gate, Daily Batch gate는 모두 container 환경변수로 주입합니다. secret 유형(값이 민감한 항목)은 secret manager 또는 배포 secret으로 주입하고, 저장소 문서에는 키 이름만 남깁니다.
