# port-view

Spring MVC와 Thymeleaf 기반의 Portfolio View 마이크로서비스다.

계좌, 잔고, 포지션, 주문, 전략 실행 계획, 전략 결과와 Daily Batch 실행 상태를 조회하고, 운영자가 AWS Step Functions 실행을 안전하게 요청할 수 있는 View UI를 제공한다.

외부 마이크로서비스의 내부 비즈니스 로직과 AWS orchestration 세부 구현은 이 저장소의 책임 범위가 아니다.

## 현재 상태

| 항목 | 값 |
| --- | --- |
| 애플리케이션 | 🟢 주요 화면과 AWS Paper DB 조회 검증 완료 |
| ECS Fargate | 🟢 1차 포팅과 화면 조회 실증 완료 |
| View CI/CD | 🟢 GitHub Actions · OIDC · CodeBuild · ECR 배포 검증 완료 |
| 운영 배포 검증 | 🟢 Candidate 검증 · 운영 승격 · Rollback 재승격 확인 |
| 현재 운영 성격 | 포트폴리오 실증 상태 유지 |
| 기본 실행 backend | `aws-stepfunctions` |
| Local File backend | 운영자 로컬 검증과 복구용 |
| Step 1~11 | safe trigger 분리 |
| Step 12~17 | approval trigger 분리 |
| Paper Daily | 2026-07-22 기준 1차 안정화 완료 |
| P2 View 고도화 | 🟠 미수행 · 현재 범위 제외 |
| aws-live BUY/SELL | 🔴 미진행 |

> ALB, HTTPS, Route53, 인증, Auto Scaling, Blue/Green과 외부 공개는 현재 완료 상태가 아니다. 외부 공개 또는 다중 사용자 운영이 필요할 때 재검토한다.

## 기술 스택

| 항목 | 값 |
| --- | --- |
| Java | 25 |
| Spring Boot | 4.1.0-SNAPSHOT |
| Web | Spring MVC |
| Template | Thymeleaf |
| Persistence | Spring Data JPA · JdbcTemplate |
| Database | PostgreSQL |
| Utility | Lombok |
| Build | Maven Wrapper |
| Container | Docker · ECS Fargate 실증 구성 |

## 주요 화면

| 화면 | 경로 |
| --- | --- |
| Dashboard | `/` · `/dashboard` |
| Balance | `/balance-summary` |
| Positions | `/positions` · `/positions/{tickerCode}` |
| Orders | `/orders` · `/orders/{id}` |
| Strategy Execution | `/strategy/execution/plans` · `/strategy/execution/plans/{planId}` |
| Strategy Report | `/strategy/reports/latest` · `/strategy/reports/{runId}` |
| Strategy Daily | `/strategy/daily/latest` · `/strategy/daily/{dailyRunId}` |
| Daily Batch | `/daily-batch` · `/daily-batch/{batchRunId}` |

### 화면별 역할

| 화면 | 내용 |
| --- | --- |
| Dashboard | 계좌 요약 · 최근 주문 · 포지션 · 최신 전략 결과 |
| Balance | 잔고와 평가금액 조회 |
| Positions | 보유 종목 목록과 상세 조회 |
| Orders | 주문 요청 · 주문 체인 · 이벤트 · 체결 조회 |
| Strategy Execution | 실행 계획과 주문 후보 조회 |
| Strategy Report | 백테스트 리포트 · 통계 · 거래 상세 |
| Strategy Daily | Daily Run · signal · position decision |
| Daily Batch | Batch Run · Step 결과 · 상태 · 로그 조회 |

Daily Batch 화면은 AWS Step Functions 실행 요청 UI를 제공한다.

- Step 1~11은 safe trigger로 분리한다.
- Step 12~17은 approval trigger와 Paper 주문 gate 뒤에서만 허용한다.
- `local-file` 실행과 Slack 테스트 기능은 운영자 로컬 검증과 복구 범위에서만 사용한다.

## View 책임 경계

### 담당 범위

| 항목 | 내용 |
| --- | --- |
| 조회 | Dashboard · Balance · Positions · Orders · Strategy · Daily Batch |
| 상태 표시 | Batch Run · Step Log · 주문 · 체결 · 포지션 · 잔고 |
| Safe trigger | AWS Step 1~11 실행 요청 |
| Approval trigger | AWS Step 12~17 승인 실행 요청 |
| Gate | 실행 가능 범위와 Paper 주문 허용 상태 표시 |

### 직접 담당하지 않는 범위

| 항목 | 실제 책임 영역 |
| --- | --- |
| broker 주문 제출 | MarketConnector와 Strategy Execution |
| 전략 판단 | Strategy Research · Decision · Execution |
| 데이터 수집 | Interest Crawler |
| 전처리 | Interest Preprocessor |
| orchestration | Step Functions · EventBridge Scheduler |
| 원격 실행 | ECS RunTask · SSM RunCommand · AWS Batch |
| 자동 Slack | Lambda와 cross-service workflow |
| aws-live | 별도 승인과 cutover 범위 |

View 코드 안에 다른 MS의 핵심 로직을 복제하지 않는다.

## 패키지 구조

| 패키지 | 역할 |
| --- | --- |
| `controller` | 요청 파라미터 처리 · Model 조립 · View 반환 |
| `service` | 화면 DTO 조립 · Connector 연동 · 실행 요청 |
| `repository` | JPA Repository · JdbcTemplate query |
| `dto` | 화면 표시용 DTO |
| `entity` | JPA Entity |
| `config` | `@ConfigurationProperties` · executor 설정 |
| `util` | 포맷팅 · label · account resolver · CSS helper |
| `common` | Thymeleaf View 이름 상수 |

주요 파일의 상세 역할은 [소스 파일 카탈로그](docs/source-file-catalog.md)를 참고한다.

## 로컬 실행

### 기본 실행

Linux와 macOS:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

기본 포트는 `application.properties`의 `server.port`를 따른다.

### 빌드

Linux와 macOS:

```bash
./mvnw clean package
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean package
```

### AWS Paper Local View

AWS Paper RDS와 secret을 사용하는 Local View는 다음 전제가 필요하다.

| 항목 | 값 |
| --- | --- |
| RDS 연결 | 운영자가 별도 port forwarding 준비 |
| Spring profile | `aws-paper` |
| DB · KIS secret | local secret loader 또는 환경변수 |
| 계좌번호 | 환경변수 주입 · 저장소 기록 금지 |
| wrapper 위치 | `C:\Workspaces\portfolio-local-env` |

backend별 wrapper는 분리되어 있다.

| backend | Starter |
| --- | --- |
| `local-file` | `Start-PortfolioViewAwsPaperLocalFile.ps1` |
| `aws-stepfunctions` | `Start-PortfolioViewAwsPaperStepFunctions.ps1` |

각 starter는 동일 이름의 `Load-*Env.ps1`을 dot-source한 뒤 port-view를 실행한다.

구형 `Start-PortfolioViewAwsPaperBatch.ps1`은 현행 기본 wrapper로 문서화하지 않는다.

## 실행 backend

`portfolio.batch.execution-mode`로 backend를 선택한다.

### `aws-stepfunctions`

ECS Fargate와 AWS Paper View의 기본 경로다.

View는 Python subprocess를 직접 실행하지 않고 AWS Step Functions `StartExecution`만 호출한다.

실제 Step 실행은 다음 서비스가 담당한다.

- AWS Step Functions
- ECS RunTask
- SSM RunCommand
- AWS Batch
- Lambda

### `local-file`

운영자 Local View의 검증과 복구용 backend다.

ProcessBuilder로 운영자 로컬 source의 Python command를 실행할 수 있다.

Fargate 운영 경로에서는 사용하지 않는다.

| 환경 | backend |
| --- | --- |
| ECS Fargate | `aws-stepfunctions` |
| Local View 운영 검증 | `aws-stepfunctions` |
| Local View 복구 | 필요 시 `local-file` |

## AWS Step Functions trigger

### Controller endpoint

| 경로 | 역할 |
| --- | --- |
| `POST /daily-batch/aws-stepfunctions/start-range` | Step 1~11 safe trigger |
| `POST /daily-batch/aws-stepfunctions/start-approval-range` | Step 12~17 approval trigger |

일반 workflow와 approval workflow ARN은 별도로 주입한다.

approval ARN이 없거나 gate가 비활성화되면 Step 12~17 요청을 차단한다.

### StartExecution payload

| 타입 | 필드 |
| --- | --- |
| boolean | `allowPaperOrderExecute` · `paperOrderEnabled` |
| numeric | `fromStepOrder` · `toStepOrder` · `startStep` · `endStep` |
| date string | `runDate` · Asia/Seoul 기준 `yyyy-MM-dd` |
| string | `environment` · `dbTarget` · `source` · `requestedBy` |
| string | `requestedFrom` · `fromStepCode` · `toStepCode` |

Choice State와 타입이 일치해야 하므로 boolean과 numeric 값을 문자열로 보내지 않는다.

성공 화면에는 `executionName`과 redaction된 `executionArn`만 표시한다.

## Daily Batch gate

| 설정 | 역할 |
| --- | --- |
| `portfolio.batch.execution-enabled` | 실행 액션 전체 허용 |
| `portfolio.batch.local-file-execution-enabled` | Local File 실행 허용 |
| `portfolio.batch.full-pipeline-execution-enabled` | 전체 1~17 실행 허용 |
| `portfolio.batch.paper-order-enabled` | Paper 주문성 Step 허용 |
| `portfolio.batch.min-executable-step-order` | 최소 실행 Step |
| `portfolio.batch.max-executable-step-order` | 최대 실행 Step |
| `portfolio.batch.aws-stepfunctions-start-enabled` | Step Functions backend 허용 |
| `portfolio.batch.aws-stepfunctions-step-start-enabled` | Step 1~11 버튼 허용 |

### Fargate 안전 기본값

| 설정 | 값 |
| --- | --- |
| execution mode | `aws-stepfunctions` |
| local file | `false` |
| Paper order | `false` |
| full pipeline | `false` |
| max Step | `11` |

> `BATCH_PAPER_ORDER_ENABLED=true`이고 Step 12 이상을 실행하면 AWS Paper 주문이 제출될 수 있다. 실행 범위, approval workflow와 gate를 반드시 확인한다.

## Snapshot Refresh

Dashboard, Balance, Positions의 Snapshot Refresh는 환경별 책임을 구분한다.

| 환경 | 처리 |
| --- | --- |
| Local View | MarketConnector subprocess refresh 사용 가능 |
| ECS Fargate | 로컬 subprocess 사용 금지 |
| Fargate 조회 | DB에 적재된 Snapshot 조회 중심 |
| 원격 refresh | AWS orchestration 또는 Connector 경로 |

주요 설정:

| 설정 | 역할 |
| --- | --- |
| `portfolio.snapshot-refresh.enabled` | 전체 활성화 |
| `portfolio.dashboard.snapshot-refresh-enabled` | Dashboard 진입 시 활성화 |
| `portfolio.snapshot-refresh.stale-minutes` | stale 기준 |
| `portfolio.snapshot-refresh.timeout-seconds` | subprocess timeout |
| `portfolio.snapshot-refresh.marketconnector-dir` | Local MarketConnector 경로 |
| `portfolio.snapshot-refresh.balance-script-name` | balance script |
| `portfolio.snapshot-refresh.python-executable` | Python 실행 파일 |

Fargate에서는 `C:/Workspaces/...` 같은 로컬 절대 경로를 운영 경로로 사용하지 않는다.

## Database

### 연결 기준

| 항목 | 값 |
| --- | --- |
| Database | `portfolio` |
| Spring datasource user | `view_app` |
| Connector subprocess user | `marketconnector_app` |
| Password | 환경변수 또는 secret 주입 |
| SQL 해석 | Hikari `search_path` 기반 |

`view_app`에 connector 쓰기 권한을 추가하는 방식으로 DB user 분리를 무력화하지 않는다.

### Domain schema

- `ops`
- `execution`
- `decision`
- `research`
- `connector`
- `preprocessor`
- `interest`
- `reference`
- `legacy`
- `public`

기본 `search_path`:

```text
ops, execution, decision, research, connector,
preprocessor, interest, reference, legacy, public
```

기존 unqualified SQL은 위 `search_path` 기준으로 동작한다.

신규 운영 SQL과 진단 SQL은 가능한 한 schema-qualified 이름을 사용한다.

OPS Mirror 기준 테이블:

| 테이블 | 역할 |
| --- | --- |
| `ops.strategy_daily_batch_run` | Batch Run |
| `ops.strategy_daily_batch_step_log` | Step 실행 로그 |

### DB 환경변수

| 환경변수 | 기본값 · 역할 |
| --- | --- |
| `INTEREST_DB_HOST` | `localhost` |
| `INTEREST_DB_PORT` | `5433` |
| `INTEREST_DB_NAME` | `portfolio` |
| `PORTFOLIO_DB_NAME` | `portfolio` |
| `INTEREST_DB_USER` | 환경별 DB user |
| `INTEREST_DB_PASSWORD` | 기본값 없음 |

## 주요 설정 범주

| 설정 | 역할 |
| --- | --- |
| `spring.datasource.*` | PostgreSQL 연결 |
| `spring.jpa.*` | JPA · Hibernate |
| `portfolio.view.*` | 기본 계좌와 화면 limit |
| `connector.*` | Connector URL과 API path |
| `portfolio.batch.*` | backend · gate · timeout · log |
| `portfolio.snapshot-refresh.*` | Snapshot Refresh |
| `slack.*` | Local View Slack 기능 |

설정 키를 추가하거나 변경하면 properties, `@ConfigurationProperties`, README와 배포 환경변수 이름을 함께 확인한다.

## ECS Fargate

port-view는 ECS Fargate Service 1차 포팅과 AWS Paper 연동 실증을 완료했다.

2026-07-29 기준 GitHub Actions · OIDC · CodeBuild · ECR 기반 배포와 Candidate 검증, 운영 승격, Rollback 재승격까지 검증했다.

현재는 상시 외부 공개 서비스가 아니라 포트폴리오 실증 상태로 유지한다.

### 배포 흐름

1. GitHub Actions 수동 실행
2. GitHub OIDC 인증
3. CodeBuild Maven Test · Package
4. Docker Image Build
5. Git Commit SHA 기반 ECR Image Push
6. Standalone Candidate Task 실행
7. Candidate 주요 8개 화면 Smoke Test
8. 수동 승인 후 ECS Service Revision 승격
9. 운영 주요 8개 화면 Smoke Test
10. 이전 정상 Revision Rollback
11. Rollback Smoke Test
12. 최신 Revision 재승격

### 배포 안전 기준

| 항목 | 값 |
| --- | --- |
| Candidate 실행 | 운영 Service와 분리된 Standalone Task |
| Candidate 배치 · Step Functions | 비활성 |
| Candidate Paper 주문 · Strategy Execution 제출 | 비활성 |
| 운영 승격 Task Definition | Candidate Revision 그대로 미사용 |
| 운영 Revision 구성 | 기존 운영 환경변수와 신규 Image Digest 결합 |
| Rollback 기준 | 승격 전 이전 정상 Revision 저장 |

> 배포 Slack 알림은 이번 범위에서 미구현이다. 기존 장전 · Daily 검증 · Daily 실행 · 장후 알림과의 채널 분리 검토 후 후순위로 진행한다.

### Container 설정

| 항목 | 값 |
| --- | --- |
| Spring profile | `SPRING_PROFILES_ACTIVE=aws-paper` |
| Database | `INTEREST_DB_*` |
| Step Functions | region · 일반 ARN · approval ARN |
| Gate | `portfolio.batch.*` |
| Snapshot | `portfolio.snapshot-refresh.*` |
| 계좌 | 환경변수 주입 · 원문 기록 금지 |

### 외부 노출 AS-IS

| 항목 | 값 |
| --- | --- |
| 현재 실증 | ECS Fargate Public IP 기반 |
| ALB | 미수행 |
| HTTPS · Route53 | 미수행 |
| 인증 | 미수행 |
| Auto Scaling | 미수행 |
| Blue/Green | 미수행 |
| 외부 공개 | 미수행 |
| 재검토 조건 | 외부 공개 또는 다중 사용자 운영 |

향후 ALB를 도입하면 source IP 제한 또는 인증 게이트를 우선 적용한다.

## Slack

`SlackNotificationService`는 Local View에서 다음 메시지를 조립할 수 있다.

- Daily Batch 결과
- Daily Run 요약
- 전략 실행 요약
- 잔고와 포지션 요약

자동 운영 Slack의 주 책임은 cross-service workflow와 Lambda에 있다.

Webhook URL은 환경변수 또는 local config로 주입하며 저장소에 기록하지 않는다.

## 외부 의존 모듈

| 모듈 | 관계 |
| --- | --- |
| `port-marketconnector` | 계좌 · 주문 · 체결 |
| `port-interest-crawler` | 원천 데이터 수집 |
| `port-interest-preprocessor` | 전처리 |
| `port_strategy_research` | 전략 연구 |
| `port_strategy_decision` | 전략 판단 |
| `port_strategy_execution` | 주문 계획과 실행 |

실제 경로와 운영 명령은 환경변수 또는 local config로 분리한다.

## 보안

아래 값은 코드, 문서, 로그에 원문으로 기록하지 않는다.

- DB password
- KIS app key · app secret
- token
- Slack webhook URL
- 실제 계좌번호
- AWS account-id
- 실제 ARN
- public IP
- broker 주문번호
- image digest full SHA256

Placeholder:

| 값 | Placeholder |
| --- | --- |
| 일반 민감정보 | `[REDACTED]` |
| 계좌번호 | `[REDACTED_ACCOUNT_NO]` |
| ARN | `[REDACTED_ARN]` |
| secret ARN | `[REDACTED_SECRET_ARN]` |
| task ARN | `[REDACTED_TASK_ARN]` |
| public IP | `[REDACTED_PUBLIC_IP]` |
| broker 주문번호 | `[REDACTED_BROKER_ORDER_NO]` |
| ECR image | `<ECR_IMAGE_URI>` |
| 향후 ALB | `<ALB_ENDPOINT>` |

보안 기준은 위 목록과 이 README 내용으로 충분하며 별도 상세 문서는 두지 않는다.

## 상세 문서

현재 유지하는 port-view 상세 문서는 아래 하나다.

| 문서 | 역할 |
| --- | --- |
| [source-file-catalog](docs/source-file-catalog.md) | 소스 파일 역할 |

구조 · 설정 · Daily Batch · 보안 · 리팩토링 기준은 README와 `docs/source-file-catalog.md`에 통합한다.

## 리팩토링 후보

- `DailyBatchService` 책임 분리
- `SlackNotificationService` 조회 · 메시지 조립 · 전송 분리
- View 공통 util과 label util 정리
- Template과 CSS의 legacy 구조 정리
- unused 후보는 삭제 전 참조 여부 검증
