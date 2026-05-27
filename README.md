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
  - 계좌 요약, 최근 주문, 보유 종목, 최신 전략 실행/리포트 요약을 표시합니다.
- Balance: `/balance-summary`
  - 계좌 잔고 요약과 평가금액 관련 정보를 표시합니다.
- Positions: `/positions`, `/positions/{tickerCode}`
  - 보유 종목 목록과 종목 상세 화면을 제공합니다.
- Orders: `/orders`, `/orders/{id}`
  - Connector 주문 요청, 주문 체인, 이벤트, 체결 정보를 조회합니다.
- Strategy Execution: `/strategy/execution/plans`, `/strategy/execution/plans/{planId}`
  - 전략 실행 계획과 주문 후보를 조회하고, 주문 제출 액션을 제공합니다.
- Strategy Report: `/strategy/reports/latest`, `/strategy/reports/{runId}`
  - 백테스트 리포트 요약, 통계, 거래 상세를 표시합니다.
- Strategy Daily: `/strategy/daily/latest`, `/strategy/daily/{dailyRunId}`
  - Daily Run, signal, position decision 조회 화면입니다.
- Daily Batch: `/daily-batch`, `/daily-batch/{batchRunId}`
  - Daily Batch 실행 이력, step 로그, 수동 실행, 재실행, Slack 테스트 액션을 제공합니다.

## 패키지 구조 요약

- `controller`: Spring MVC Controller. 요청 파라미터를 해석하고 service 결과를 Model에 담아 Thymeleaf view를 반환합니다.
- `service`: 화면 DTO 조립, 외부 Connector 호출, Daily Batch 실행, Slack 알림 등 application service 역할을 담당합니다.
- `repository`: JPA Repository와 JdbcTemplate 기반 query repository가 함께 존재합니다.
- `dto`: 화면 표시용 DTO입니다. Lombok class DTO와 Java record DTO가 혼재되어 있습니다.
- `entity`: JPA Entity입니다. balance, holdings, trade orders, connector snapshot/order/fill/event 계열이 있습니다.
- `config`: `@ConfigurationProperties`, async executor 등 설정 객체입니다.
- `util`: 화면 포맷팅, label 변환, account resolver, CSS class helper입니다.
- `common`: Thymeleaf view 이름 상수(`ViewNames`)를 관리합니다.

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
