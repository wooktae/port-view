# CHANGELOG

## 2026-07-01

### Added

- README에 "View 책임 경계" 섹션을 추가했습니다. port-view가 조회 / 승인 / 트리거 UI를 담당하고, 실제 Daily Batch 실행 책임은 AWS Step Functions / EventBridge Scheduler / ECS RunTask / SSM RunCommand / AWS Batch / Lambda 쪽에 있다는 책임 경계를 명시했습니다.
- README에 "ECS Fargate 배포 관점" 섹션을 추가했습니다. Docker image build → ECR push → ECS Task Definition revision → ECS Service rollout 흐름 요약, Fargate 운영 안전 기본값(`execution-mode=aws-stepfunctions`, `local-file-execution-enabled=false`, `paper-order-enabled=false`, `full-pipeline-execution-enabled=false`, `max-executable-step-order=11`), container 환경변수 주입 유형, ALB 노출 원칙, 로컬 절대 경로 의존이 Fargate 운영 경로가 아니라는 점을 정리했습니다.
- `docs/architecture.md`에 배포 구조(ECS Fargate) 요약 섹션을 추가했습니다. View 책임 경계, 컨테이너 안의 흐름 두 가지(조회 흐름 + Step Functions `StartExecution` 트리거 흐름), 저장소에 원문 기록하지 않는 값과 placeholder 정책을 정리했습니다.
- `docs/configuration.md`에 "Fargate 안전 기본값" subsection을 추가했습니다. Daily Batch 안전 기본값 5종과 state machine ARN placeholder(`<STATE_MACHINE_ARN>`, `<APPROVAL_STATE_MACHINE_ARN>`)을 정리했습니다.
- `docs/daily-batch.md`에 "AWS Step Functions backend와 View 트리거 UI" subsection을 추가했습니다. safe/approval endpoint 분리, 일반 workflow와 approval workflow ARN 분리, `StartExecution` payload boolean/numeric/string 타입 정합, View는 조회/승인/트리거 UI만 담당한다는 책임 경계를 정리했습니다.
- `docs/worklog/2026-07-01.md`를 신규 작성했습니다.

### Changed

- 없음. 코드 / properties / template / Java 파일 변경 없음.

### Notes

- 본 CHANGELOG entry는 port-view MS 저장소 문서 최신화만 다룹니다. MarketConnector, StrategyExecution, StrategyDecision, StrategyResearch, Preprocessor, Crawler, EventBridge Scheduler, Lambda, Step Functions 내부 orchestration의 세부 운영 내용은 port-view 책임 경계 밖이며 본 저장소 문서에는 반영하지 않았습니다. cross-service 세부 이력은 `.kiro/` 하위 AWS Migration spec에서 관리됩니다.
- ECS Fargate 포팅 실행, Docker image build, ECR push, ECS Task Definition / Service 생성 · 갱신, ALB 구성, Daily Batch 실행, Slack Webhook 테스트, 외부 투자/주문 API 호출, DB DDL/DML, AWS CLI / boto3 / Spring Boot 실행은 본 문서 업데이트 작업에서 수행하지 않았습니다.
- commit/add/reset/checkout/stash는 실행하지 않았습니다.
- DB password, API key, token, Slack webhook URL, 계좌번호 전체, account-id 12자리 원문, 실제 secret ARN, 실제 IAM Role ARN, 실제 state machine ARN 전체, ALB DNS 또는 공개 endpoint 원문, command id, 일회성 실행 로그는 본 변경 문서에 기록하지 않았습니다. 필요 시 `[REDACTED]` / `<STATE_MACHINE_ARN>` / `<APPROVAL_STATE_MACHINE_ARN>` / `<ALB_ENDPOINT>` / `<ECR_IMAGE_URI>` placeholder로 표기했습니다.

## 2026-06-29 (3)

### Added

- AWS Step 12~17 승인 실행 endpoint `POST /daily-batch/aws-stepfunctions/start-approval-range` 와 화면 승인 버튼을 추가했습니다. 성공 시 `executionName` 과 account-id 를 redaction 한 `executionArn` 을 flash message 로 표시합니다.
- Step 12~17 전용 state machine ARN 분리 — 일반 workflow ARN `portfolio-paper-daily-step1-17-approval` 과 approval workflow ARN `portfolio-paper-daily-step12-17-approval` 을 분리했습니다. `application.properties` 키 `portfolio.batch.aws-stepfunctions-approval-state-machine-arn` 와 환경변수 `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_APPROVAL_STATE_MACHINE_ARN` 을 추가했습니다.
- README 의 Daily Batch 섹션에 일반 / approval ARN 분리, 승인 버튼 endpoint, payload boolean / numeric 타입 정합 사실을 추가했습니다.
- 운영자 로컬 도구 폴더(`C:\Workspaces\portfolio-local-env\`) 에 wrapper 2종(`Start-PortfolioViewAwsPaperLocalFile.ps1` + `Start-PortfolioViewAwsPaperStepFunctions.ps1`) 과 env loader 2종(`Load-PortfolioViewAwsPaperLocalFileEnv.ps1` + `Load-PortfolioViewAwsPaperStepFunctionsEnv.ps1`) 분리 사실을 README 에 정리했습니다(스크립트 본체는 본 저장소 범위 밖).

### Changed

- `DailyBatchProperties` 에 `awsStepfunctionsApprovalStateMachineArn` 필드 + getter / setter 를 추가했습니다.
- `StepFunctionsDailyBatchExecutionService` 의 `startSafeRange` 는 일반 state machine ARN 을, `startApprovalRange` 는 approval 전용 state machine ARN 을 사용하도록 분리했습니다. approval ARN 이 비어 있으면 승인형 실행을 서비스 레벨에서 차단합니다.
- StartExecution payload 의 타입을 보완했습니다. `allowPaperOrderExecute` · `paperOrderEnabled` 는 boolean JSON, `fromStepOrder` · `toStepOrder` · `startStep` · `endStep` 는 numeric JSON 으로 전달합니다.
- `DailyBatchController` 에 `POST /daily-batch/aws-stepfunctions/start-approval-range` endpoint 와 승인형 Step 12~17 `StartExecution` 처리를 추가했습니다.
- `/daily-batch` 화면(`daily_batch.html`) 에서 AWS Step 1~11 시작 버튼과 AWS Step 12~17 승인 실행 버튼을 분리했고, safe / approval 활성화 조건을 분리했습니다.

### Fixed

- 최초 Step 12~17 approval 실행이 전용 state machine 에 진입한 뒤 `Step12_CheckApproval` 에서 차단되던 문제(원인: `allowPaperOrderExecute` · `paperOrderEnabled` 가 문자열 `"true"` 로 전달되어 Choice `BooleanEquals` 조건과 맞지 않음 + `fromStepOrder` · `toStepOrder` · `startStep` · `endStep` 가 문자열로 전달)를 해소했습니다. boolean / numeric 타입으로 보완 후 재검증에서 `Step12_CheckApproval` 통과 + `ExecutionSucceeded` 확인.
- View approval 버튼이 일반 Step 1~17 state machine ARN 을 호출하던 문제를 approval 전용 ARN 사용으로 분리했습니다.
- 로컬 View 실행 wrapper 에서 safe-only gate 가 남아 운영자 의도와 다르게 Step 1~17 전체 실행이 막히던 문제를 wrapper 2종 분리로 해소했습니다.

### Notes

- 본 변경의 코드 수정 사실은 commit `e72de6f`(`feat(view): add Step Functions daily batch trigger`) 후속의 추가 commit (변경 파일 = `DailyBatchProperties.java` · `StepFunctionsDailyBatchExecutionService.java` · `application-aws-paper.properties` · `DailyBatchController.java` · `daily_batch.html`) 및 운영자 로컬 도구 폴더 wrapper 4종 변경을 참조합니다. 본 저장소 범위 밖의 wrapper 본체는 기록하지 않습니다.
- AWS Step Functions `portfolio-paper-daily-step12-17-approval` 검증 결과: 승인 실행이 정상 종료되고 DB 후검증에서 신규 `connector_order_request` 및 broker 주문이 발생하지 않은 상태로 확인했습니다. 구체 executionName, executionArn, 실행 시각, 일회성 검증 marker 원문은 저장소 문서에 기록하지 않습니다.
- 본 문서 업데이트 작업에서는 Daily Batch 실행, Slack Webhook 테스트, 외부 투자/주문 API 호출, DB DDL/DML, AWS CLI / boto3 / Spring Boot 실행을 추가로 수행하지 않았습니다.
- commit/add/reset/checkout/stash 는 실행하지 않았습니다.
- secret value, KIS app key, KIS app secret, token, RDS password, account-id 12자리 원문, 계좌번호 전체값, 실제 secret ARN, 실제 IAM Role ARN, 실제 state machine ARN, Slack webhook URL 은 본 변경 문서에 기록하지 않았습니다. 필요 시 `[REDACTED]` 로 표기했습니다.

## 2026-06-29 (2)

### Added

- Daily Batch 실행 backend로 `aws-stepfunctions` 모드를 추가했습니다. `StepFunctionsDailyBatchExecutionService`가 AWS SDK v2 Step Functions client로 `StartExecution`을 호출하고, Python subprocess나 로컬 source 직접 실행 없이 Daily Batch를 트리거합니다.
- `POST /daily-batch/aws-stepfunctions/start-range` Controller endpoint를 추가했습니다. 성공 시 `executionName`과 account-id를 redaction한 `executionArn`을 flash message로 표시합니다.
- `/daily-batch` 화면에 AWS Step 1~11 safe trigger 버튼을 추가했습니다.
- README의 Daily Batch 섹션에 `local-file` / `aws-stepfunctions` backend 분리 설명과 `portfolio.batch.aws-stepfunctions-*` 환경변수 주입 키, Fargate 안전 기본값(`local-file-execution-enabled=false`, `paperOrderEnabled=false`, `fullPipelineExecutionEnabled=false`) 설명을 추가했습니다.

### Changed

- `application-aws-paper.properties`에 `portfolio.batch.execution-mode`, `portfolio.batch.aws-stepfunctions-region`, `portfolio.batch.aws-stepfunctions-state-machine-arn`, `portfolio.batch.aws-stepfunctions-execution-name-prefix`, `portfolio.batch.aws-stepfunctions-start-enabled`, `portfolio.batch.aws-stepfunctions-step-start-enabled` 환경변수 주입 placeholder를 추가했습니다. 실제 값은 본 저장소에 기록하지 않습니다.
- `DailyBatchProperties`에 aws-stepfunctions backend gate(`canStartAwsStepfunctions`, `awsStepfunctionsStartEnabled`, `awsStepfunctionsStepStartEnabled` 등)를 추가했습니다. `stateMachineArn`이 비어 있거나 `hasRunningBatch` 상태이면 버튼이 비활성화됩니다.
- Daily Batch StartExecution payload 구성을 보완했습니다. `environment=paper`, `dbTarget=aws-paper`, `source=PORT_VIEW`, `requestedBy=VIEW_BUTTON`, `requestedFrom=port-view`, `fromStepCode` / `toStepCode`, `fromStepOrder` / `toStepOrder`, `startStep` / `endStep`, `allowPaperOrderExecute`, `paperOrderEnabled`, `runDate`(Asia/Seoul 기준 yyyy-MM-dd) 필드를 포함합니다. `accountNo`는 payload에는 포함하되 화면 / 로그 / 본 저장소 문서에는 원문을 노출하지 않습니다.

### Fixed

- 최초 검증에서 Step 1~11 후 `StopCrawlerEc2AfterStep11Success` 상태에서 `States.Runtime` 오류가 발생하던 문제(원인: ASL Payload의 `runDate.$=$.runDate` 참조에 대해 View `StartExecution` input에 `runDate`가 누락)를 해소했습니다. `StepFunctionsDailyBatchExecutionService`가 Asia/Seoul 기준 `runDate`를 input JSON에 추가하도록 보완했고, 재검증에서 `StopCrawlerEc2AfterStep11Success` → `SendApprovalRequiredSlack`까지 통과하여 `APPROVAL_REQUIRED` Slack 수신을 확인했습니다.

### Notes

- 본 변경의 코드 수정 사실은 commit `e72de6f` (`feat(view): add Step Functions daily batch trigger`)을 참조합니다. 변경 파일 범위: `pom.xml`, `src/main/java/my/portfolio/port_view/config/DailyBatchProperties.java`, `src/main/java/my/portfolio/port_view/controller/DailyBatchController.java`, `src/main/java/my/portfolio/port_view/service/StepFunctionsDailyBatchExecutionService.java`, `src/main/resources/application-aws-paper.properties`, `src/main/resources/templates/pages/daily_batch.html`.
- 로컬 Step 1~11 `StartExecution` 검증 완료. Step 12~17 주문성 구간은 `allowPaperOrderExecute=false` 기준으로 차단을 유지했고, broker 주문 제출은 없었습니다.
- 본 문서 업데이트 작업에서는 Daily Batch 실행, Slack Webhook 테스트, 외부 투자/주문 API 호출, DB DDL/DML, AWS CLI / boto3 / Spring Boot 실행을 수행하지 않았습니다.
- commit/add/reset/checkout/stash는 실행하지 않았습니다.
- secret value, KIS app key, KIS app secret, token, RDS password, account-id 12자리 원문, 계좌번호 전체값, 실제 secret ARN, 실제 IAM Role ARN, Slack webhook URL은 본 변경 문서에 기록하지 않았습니다. 필요 시 `[REDACTED]`로 표기했습니다.

## 2026-06-29

### Added

- AWS Paper Local View 실행 스크립트 사용 방법을 README에 정리했습니다(운영자 로컬 도구 `Load-PortfolioViewAwsPaperBatchEnv.ps1`과 `Start-PortfolioViewAwsPaperBatch.ps1`, RDS port forwarding 전제 포함).
- Dashboard / Balance / Positions 화면의 Snapshot Refresh 재활성화 동작을 문서화했습니다.
- Daily Batch 실행 이력 카드형 UI와 status 색상 개선 사항을 문서에 반영했습니다.

### Changed

- `aws-paper` profile의 Snapshot Refresh 관련 설정을 env override 기준으로 README에 정리했습니다.
- Spring View datasource user(`view_app`)와 Connector subprocess DB user(`marketconnector_app`)를 분리한 구조를 README에 명시했습니다.
- Daily Batch 화면의 실행 이력, Step Logs, Payload, 현재 모드 표시를 운영 콘솔형으로 정리한 사실을 문서에 반영했습니다.

### Fixed

- `connector_balance.py`가 `view_app` 권한으로 실행되며 `permission denied for connector_account`가 발생하던 문제를 subprocess DB user 분리로 해소한 사실을 문서에 정리했습니다.
- Daily Batch 실행 이력과 상태 pill에 CSS가 충분히 적용되지 않던 문제 개선 사항을 문서에 정리했습니다.

### Notes

- 본 문서 업데이트 작업에서는 Daily Batch 실행, Slack Webhook 테스트, 외부 투자/주문 API 호출, DB DDL/DML을 실행하지 않았습니다.
- commit/add/reset/checkout/stash는 실행하지 않았습니다.
- 비밀번호, 토큰, Webhook URL, API Key, 계좌번호 전체 값은 문서에 기록하지 않았고 필요한 경우 `[REDACTED]`로 표기했습니다.

## 2026-05-28

### Added

- AWS Migration 전 초기 정리를 위해 `docs/source-file-catalog.md`를 추가하고 주요 소스/설정/문서 파일의 역할과 운영 주의사항을 정리했습니다.
- Connector 잔고/주문 갱신, 대시보드/보유 종목/리포트 관련 주요 Java 파일과 화면 template/CSS에 한글 설명 주석을 추가했습니다.

### Changed

- README에 소스 파일 카탈로그 안내를 추가했습니다.
- 2026-05-27 이후 미커밋 변경사항을 문서화 대상으로 정리했습니다.

### Notes

- 기능 변경 없음.
- commit/add/reset/checkout/stash는 실행하지 않았습니다.
- Daily Batch 실행, Slack Webhook 테스트, 외부 주문/투자 API 호출, DB DDL/DML은 실행하지 않았습니다.

## 2026-05-27

### Added

- PostgreSQL 단일 DB `portfolio`와 schema-per-domain 구조, port-view `search_path` 문서화를 추가했다.

### Changed

- DB datasource 설정을 `INTEREST_DB_*` 환경변수 placeholder 기준으로 외부화했다.
- DB name 기본값 설명을 `portfolio` 기준으로 정리하고, 기존 SQL은 Hikari `connection-init-sql`의 `search_path` 기반으로 동작한다고 명시했다.

### Notes

- 사용자가 Dashboard, Balance, Holdings, Strategy Plan, Daily Batch, Report 화면 조회 검증을 완료했다.
- 이번 문서 작업에서는 실제 DB 접속, 서버 실행, 외부 API 호출은 실행하지 않았다.

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
