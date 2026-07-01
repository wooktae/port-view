# Operation Notes — 05-port-view-ecs-and-runbook

본 문서는 05-port-view-ecs-and-runbook 진행 중 운영자 / Kiro 가 수행한 작업 결과를 일자별로 누적 기록하는 운영 노트다. 1차 적용 환경은 `aws-paper`, region 은 `ap-northeast-2`, 1차 적용 대상은 port-view(Spring Boot / Thymeleaf 통합 운영 콘솔). port-view 의 ECS Fargate Service 포팅 계획 / 구현 / 검증 결과를 본 문서에 누적한다.

## 기록 형식

- 일자별 섹션을 본 문서에 누적한다.
- 사실 식별자(commit hash / Class 이름 / Controller endpoint path / Spring properties key / StartExecution payload 필드 / Step Functions state name / Slack 이벤트 라벨 등) 는 사용자 명시 정책 정합으로 사실 기록한다.
- secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN / broker_order_no 원문 / Slack webhook URL / Administrator password / 실제 state machine ARN 본 문서 평문 기록 금지(`[REDACTED]` 또는 placeholder).
- Lambda 코드 본문 / IAM Policy 전체 본문 / Step Functions ASL 전체 본문 / Lambda 응답 전문 / CloudWatch Logs 전문 / KIS API response body / Spring Boot application log 전문 / Step Functions execution history 본문 / Slack 메시지 본문 / commit diff 본문 평문 인용 금지(R-DOCS-001 정합).

## 2026-06-29 (2) — ECS Fargate 포팅 계획 / 구현 1차 현황 정리

6. ECS Fargate 포팅 계획: 완료
 1) 최종 포팅 방향 확정: 완료
   (1) View 실행 위치
       - port-view 는 ECS Fargate Service 로 포팅
       - View 는 AWS Paper 운영 콘솔 역할 유지
       - 화면 조회 / 실행 이력 조회 / 승인 트리거 UI 중심으로 구성
       - Batch 실제 실행 책임은 View local subprocess 가 아니라 Step Functions 로 이관
   (2) Local File 실행 방식 보존
       - 로컬 View 에서 local-file 실행 버튼과 실행 경로는 그대로 보존
       - 기존 Local View 버튼 → 로컬 source ProcessBuilder 실행 → AWS Paper DB 저장 / 조회 흐름은 운영자 로컬 검증 도구로 유지
       - Fargate 실행환경에서는 local-file backend 를 사용하지 않음
       - ECS 컨테이너 안에 `C:/Workspaces` 기반 로컬 source 실행 구조를 들고 가지 않음
   (3) AWS Step Functions backend 추가 방향 확정
       - `StepFunctionsDailyBatchExecutionService` 를 신규 추가
       - Daily Batch 실행 backend 를 `local-file` / `aws-stepfunctions` 로 분리
       - `portfolio.batch.execution-mode` 값으로 실행 backend 를 선택
       - `aws-stepfunctions` mode 에서는 Python script 직접 실행 없이 `StartExecution` 만 수행
       - View 는 `executionArn` / 실행 요청 payload / DB run 결과를 화면에 표시
 2) 로컬 선검증 후 Fargate 진입 순서 확정: 완료
   (1) 로컬 Step Functions 연동 선검증
       - Fargate 배포 전에 로컬 View 에서 먼저 Step Functions `StartExecution` 연동을 검증
       - 로컬 실행환경을 Fargate 로 흉내내는 것이 아니라 Daily Batch backend 만 `aws-stepfunctions` 로 전환해 검증
       - 로컬에서 Controller / Service / DTO / button gate / payload 생성 / `executionArn` 표시 흐름을 먼저 검증
       - 이후 Fargate 에서는 컨테이너 / IAM / VPC / Secret / RDS 접근 문제만 분리 검증
   (2) Step 1~11 우선 연결
       - 첫 연결 대상은 Step 1~11 `StartExecution`
       - `allowPaperOrderExecute=false` 기준으로 실행
       - Step 12~17 은 approval gate 로 차단 유지
       - 실주문 제출 없는 사전 검증 경로부터 연결
   (3) Step 12~17 승인형 연결
       - Step 12~17 은 별도 승인 버튼으로 분리
       - paper-order gate 뒤에서만 버튼 활성화
       - 실행 전 REQUESTED `strategy_execution_order` / retryable rejected `connector_order_request` / active `connector_order_request` preflight 확인 정책 유지
       - 로컬에서 Step 12~17 NO_TARGET 또는 safe path 검증 후 Fargate 연결
 3) ECS / Fargate 진입 기준 확정: 완료
   (1) ECS 용 실행 설정 분리
       - aws-paper-local 은 `127.0.0.1:15433` RDS port forwarding 및 로컬 검증 기준
       - aws-paper-ecs 는 RDS private endpoint / Secrets Manager 또는 SSM SecureString / ECS Task Role 기준
       - Fargate 에서는 `portfolio.batch.local-file-execution-enabled=false`
       - Fargate 초기 기동은 조회-only 또는 Step 1~11 safe trigger 부터 시작
   (2) Fargate 배포 순서
       - Dockerfile 작성
       - ECR image push
       - ECS Task Definition 등록
       - ECS Service 기동
       - 조회-only smoke test
       - Step 1~11 `StartExecution` 검증
       - 승인형 Step 12~17 검증
   (3) 안전 기본값
       - Fargate 초기 `paperOrderEnabled=false` 권장
       - `fullPipelineExecutionEnabled=false` 부터 시작
       - Step 12 이상 주문성 구간은 별도 gate / preflight / approval 확인 후 활성화
       - Snapshot Refresh 는 초기 OFF 또는 별도 trigger 방식으로 제한 검토
 4) 결론
   (1) ECS Fargate 포팅 계획 확정
       - View 는 ECS Fargate Service 로 포팅
       - local-file batch 실행은 로컬 운영자 도구로 보존
       - Fargate View 는 Step Functions `StartExecution` 기반 실행 trigger 로 전환
       - 로컬 `aws-stepfunctions` mode 검증 후 Docker / ECR / ECS Task Definition 단계로 진입
       - `StepFunctionsDailyBatchExecutionService` 추가 및 `aws-stepfunctions` mode 구현은 1차 완료
       - 다음 작업은 Docker / ECR / ECS Task Definition / Fargate 조회-only smoke test 진입

7. ECS Fargate 포팅 구현
 1) Step Functions 실행 backend 구현: 완료
   (1) `StepFunctionsDailyBatchExecutionService` 추가: 완료
       - Daily Batch 실행 요청을 Step Functions `StartExecution` 호출로 변환
       - `local-file` 실행 서비스와 분리
       - `execution-mode=aws-stepfunctions` 기준으로 동작
       - Python subprocess / `C:/Workspaces` 로컬 source 직접 실행 없음
       - AWS SDK v2 Step Functions client 를 사용해 `StartExecution` 호출
       - `stateMachineArn` 은 `application.properties` 에 직접 고정하지 않고 환경변수로 주입
   (2) `StartExecution` payload 구성: 완료
       - `environment=paper` 포함
       - `dbTarget=aws-paper` 포함
       - `requestedBy=VIEW_BUTTON` 기준으로 요청 주체 구분
       - `source=PORT_VIEW` 포함
       - `requestedFrom=port-view` 포함
       - `runDate` 는 Asia/Seoul 기준 yyyy-MM-dd 값으로 포함
       - `fromStepCode` / `toStepCode` 포함
       - `fromStepOrder` / `toStepOrder` 포함
       - `startStep` / `endStep` 포함
       - `allowPaperOrderExecute=false` 기준 Step 1~11 safe trigger 우선 지원
       - `accountNo` 는 실행 payload 에 포함하되 화면 / 로그 / 문서에는 원문 노출하지 않는 방향 유지
   (3) `executionArn` 처리: 완료
       - `StartExecution` 응답의 `executionName` / `executionArn` 수신
       - 화면 flash message 에는 account-id 를 redaction 한 `executionArn` 표시
       - 실행 요청 payload 는 서비스 내부에서 구성
       - 실행 상태 전문은 Step Functions history 직접 노출이 아니라 DB run / step log 요약 기준으로 표시하는 방향 유지
   (4) 안전 차단 로직: 완료
       - `aws-stepfunctions` mode 와 start-enabled gate 가 꺼져 있으면 실행 차단
       - `stateMachineArn` 이 비어 있으면 실행 차단
       - `minExecutableStepOrder` / `maxExecutableStepOrder` 범위 밖 요청 차단
       - `allowPaperOrderExecute=false` 상태에서 Step 12 이상 요청 차단
       - approval 요청은 `paperOrderEnabled=true` 조건에서만 허용하도록 분리
 2) `aws-stepfunctions` mode 추가: 완료
   (1) application 설정 추가: 완료
       - `portfolio.batch.aws-stepfunctions-region` 추가
       - `portfolio.batch.aws-stepfunctions-state-machine-arn` 추가
       - `portfolio.batch.aws-stepfunctions-execution-name-prefix` 추가
       - `portfolio.batch.aws-stepfunctions-start-enabled` 기존 gate 와 연동
       - `portfolio.batch.aws-stepfunctions-step-start-enabled` 기존 gate 와 연동
       - `portfolio.batch.local-file-execution-enabled=false` 기준으로 local subprocess 실행 차단 가능
       - `portfolio.batch.paper-order-enabled=false` 기준으로 주문성 구간 차단 가능
   (2) 화면 gate 반영: 완료
       - `/daily-batch` 화면에 AWS Step 1~11 시작 버튼 추가
       - `hasRunningBatch` 상태에서는 실행 버튼 비활성
       - `canStartAwsStepfunctions=false` 상태에서는 AWS Step 1~11 버튼 비활성
       - `aws-stepfunctions` mode 에서는 local-file backend 와 별도 실행 경로 사용
       - Step 12~17 승인 버튼은 아직 별도 후속 구현 대상으로 분리
   (3) Controller endpoint 추가: 완료
       - `/daily-batch/aws-stepfunctions/start-range` POST endpoint 추가
       - `fromStepCode` / `toStepCode` / `accountNo` 요청값 수신
       - `DailyBatchProperties` gate 확인 후 `StepFunctionsDailyBatchExecutionService` 호출
       - `StartExecution` 성공 시 `executionName` 과 redaction 된 `executionArn` 을 flash message 로 표시
       - 실패 시 원인 메시지를 flash error 로 표시하고 `/daily-batch` 화면으로 redirect
 3) 로컬 Step 1~11 `StartExecution` 검증: 완료
   (1) 로컬 실행 조건: 완료
       - Spring profile `aws-paper` 기준으로 로컬 View 실행
       - `execution-mode=aws-stepfunctions` 기준으로 backend 전환
       - `local-file-execution-enabled=false` 기준으로 로컬 subprocess 실행 차단
       - `paper-order-enabled=false` 기준으로 주문성 구간 차단
       - AWS Paper RDS tunnel 과 View DB 조회 흐름 유지
       - Step Functions 호출 권한은 로컬 AWS credential 기준으로 검증
   (2) 검증 항목: 완료
       - `/daily-batch` 화면에서 AWS Step 1~11 실행 버튼 확인
       - View 버튼 클릭으로 Step Functions `StartExecution` 성공 확인
       - `executionName` 반환 확인
       - redaction 된 `executionArn` 반환 확인
       - `allowPaperOrderExecute=false` 기준 실행 확인
       - Step 1~11 workflow 실행 확인
       - Step 12~17 approval gate 전 `APPROVAL_REQUIRED` Slack 수신 확인
       - 신규 broker 주문 제출 없는 safe trigger 경로 확인
   (3) `runDate` 누락 보완: 완료
       - 최초 검증에서 Step 1~11 완료 후 `StopCrawlerEc2AfterStep11Success` 상태에서 `States.Runtime` 발생
       - 원인은 ASL Payload 의 `runDate.$=$.runDate` 참조 대비 View `StartExecution` input 에 `runDate` 누락
       - `StepFunctionsDailyBatchExecutionService` 에서 Asia/Seoul 기준 `runDate` 를 input JSON 에 추가
       - 재검증 결과 `StopCrawlerEc2AfterStep11Success` 이후 `SendApprovalRequiredSlack` 까지 통과
       - Slack `APPROVAL_REQUIRED` 수신으로 local `aws-stepfunctions` end-to-end 검증 완료
   (4) 구현 커밋: 완료
       - commit `e72de6f`
       - message `feat(view): add Step Functions daily batch trigger`
       - 변경 파일은 `pom.xml`, `DailyBatchProperties.java`, `DailyBatchController.java`, `application-aws-paper.properties`, `daily_batch.html`, `StepFunctionsDailyBatchExecutionService.java`
       - git working tree 정리 완료
       - 본 노트에 commit diff 본문 / Class 내부 코드 / `application-aws-paper.properties` 본문 평문 인용 0건(R-DOCS-001 정합)
 4) Step 12~17 승인형 검증: 완료
   (1) 실행 전 preflight: 완료
       - REQUESTED / READY `strategy_execution_order` 확인 완료
       - retryable rejected `connector_order_request` 확인 완료
       - active `connector_order_request` 확인 완료
       - strategy-linked active `connector_order_request` 없음 확인 완료
       - 기존 stale `connector_order_request` ACCEPTED 주문은 2026-04-27 삼성전자 미매핑 주문으로 별도 cleanup 대상으로 분리
       - Step 12~17 검증 대상 신규 주문 없음 확인 완료
   (2) 승인 실행 버튼 / endpoint 구현: 완료
       - AWS Step 12~17 승인 실행 버튼 추가(`daily_batch.html` / safe 버튼과 분리 / safe · approval 활성화 조건 분리)
       - `POST /daily-batch/aws-stepfunctions/start-approval-range` Controller endpoint 추가
       - `requestedBy=VIEW_APPROVAL_BUTTON` payload 생성 확인
       - `allowPaperOrderExecute=true` payload 전달 확인
       - `paperOrderEnabled=true` payload 전달 확인
       - `executionName` / redaction 된 `executionArn` 화면 표시 확인
   (3) Step 12~17 전용 state machine ARN 분리: 완료
       - 일반 workflow ARN: `portfolio-paper-daily-step1-17-approval`
       - approval workflow ARN: `portfolio-paper-daily-step12-17-approval`
       - 기존 `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_STATE_MACHINE_ARN` 사용
       - `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_APPROVAL_STATE_MACHINE_ARN` 신규 환경변수 추가
       - `application.properties` key 추가: `portfolio.batch.aws-stepfunctions-approval-state-machine-arn`
       - `DailyBatchProperties` 에 `awsStepfunctionsApprovalStateMachineArn` 필드 + getter / setter 추가
       - `StepFunctionsDailyBatchExecutionService` 의 `startSafeRange` 는 일반 state machine ARN 사용
       - `StepFunctionsDailyBatchExecutionService` 의 `startApprovalRange` 는 approval 전용 state machine ARN 사용
       - approval ARN 이 비어 있으면 승인형 실행 차단(서비스 레벨 안전 gate)
       - 실제 state machine ARN 의 account-id 부분은 본 노트 평문 기록 0건(`[REDACTED]` 또는 placeholder)
   (4) payload 타입 보완: 완료
       - 최초 Step 12~17 approval 실행은 전용 state machine 에 진입했으나 `Step12_CheckApproval` 에서 차단
       - 원인은 `allowPaperOrderExecute` 와 `paperOrderEnabled` 가 문자열 `"true"` 로 전달된 것
       - Step Functions Choice `BooleanEquals` 조건과 맞도록 boolean `true` 로 수정
       - `fromStepOrder` / `toStepOrder` / `startStep` / `endStep` 도 numeric 값으로 수정
       - 재검증에서 `Step12_CheckApproval` 통과 확인
       - 본 노트 commit diff / Java 본문 / JSON payload 본문 평문 인용 0건(R-DOCS-001 정합)
   (5) AWS Step Functions 실행 검증: 완료
       - executionName: `port-view-step12-17-step12-17-20260629-194314-ba5edaf8`
       - state machine: `portfolio-paper-daily-step12-17-approval`
       - status: `SUCCEEDED`
       - start: `2026-06-29T19:43:15.673+09:00`
       - stop: `2026-06-29T19:46:06.546+09:00`
       - `Step12_CheckApproval` 통과
       - `Step12_RunMarketConnectorStrategyOrderExecute` 실행
       - `Step12_GetCommandInvocation` 성공
       - Step 13~17 전체 진행
       - `ExecutionSucceeded` 확인
   (6) DB 안전 후검증: 완료
       - 운영 marker: `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS`
       - 오늘 신규 `connector_order_request` 0건
       - READY / REQUESTED `strategy_execution_order` 0건
       - 신규 broker 주문 제출 없음
       - 최근 `connector_order_request` 는 2026-06-22 ~ 2026-06-24 기존 주문만 표시
 5) 로컬 View 구동 wrapper 정리: 완료
   (1) 실행 방식 분리: 완료
       - Local-file 구동 wrapper 와 AWS Step Functions 구동 wrapper 를 분리
       - 두 wrapper 모두 로컬 View 를 `aws-paper` profile 로 기동
       - 두 wrapper 모두 Step 1~17 전체 실행 가능하도록 gate 를 구성
       - safe-only 검증용 wrapper 가 아니라 운영자 선택형 전체 실행 wrapper 로 정리
   (2) Local-file 구동 명령: 완료
       - `powershell.exe -NoProfile -ExecutionPolicy Bypass -File "C:\Workspaces\portfolio-local-env\Start-PortfolioViewAwsPaperLocalFile.ps1"`
       - `PORTFOLIO_BATCH_EXECUTION_MODE=local-file`
       - `PORTFOLIO_BATCH_EXECUTION_ENABLED=true`
       - `PORTFOLIO_BATCH_LOCAL_FILE_EXECUTION_ENABLED=true`
       - `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_START_ENABLED=false`
       - `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_STEP_START_ENABLED=false`
       - `PORTFOLIO_BATCH_FULL_PIPELINE_EXECUTION_ENABLED=true`
       - `PORTFOLIO_BATCH_PAPER_ORDER_ENABLED=true`
       - `PORTFOLIO_BATCH_MIN_EXECUTABLE_STEP_ORDER=1`
       - `PORTFOLIO_BATCH_MAX_EXECUTABLE_STEP_ORDER=17`
       - env loader = `Load-PortfolioViewAwsPaperLocalFileEnv.ps1`(운영자 로컬 도구 폴더 / 본 spec 범위 밖)
   (3) AWS Step Functions 구동 명령: 완료
       - `powershell.exe -NoProfile -ExecutionPolicy Bypass -File "C:\Workspaces\portfolio-local-env\Start-PortfolioViewAwsPaperStepFunctions.ps1"`
       - `PORTFOLIO_BATCH_EXECUTION_MODE=aws-stepfunctions`
       - `PORTFOLIO_BATCH_EXECUTION_ENABLED=true`
       - `PORTFOLIO_BATCH_LOCAL_FILE_EXECUTION_ENABLED=false`
       - `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_START_ENABLED=true`
       - `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_STEP_START_ENABLED=true`
       - `PORTFOLIO_BATCH_FULL_PIPELINE_EXECUTION_ENABLED=true`
       - `PORTFOLIO_BATCH_PAPER_ORDER_ENABLED=true`
       - `PORTFOLIO_BATCH_MIN_EXECUTABLE_STEP_ORDER=1`
       - `PORTFOLIO_BATCH_MAX_EXECUTABLE_STEP_ORDER=17`
       - `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_STATE_MACHINE_ARN` 은 `portfolio-paper-daily-step1-17-approval` 조회값 사용
       - `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_APPROVAL_STATE_MACHINE_ARN` 은 `portfolio-paper-daily-step12-17-approval` 조회값 사용
       - env loader = `Load-PortfolioViewAwsPaperStepFunctionsEnv.ps1`(운영자 로컬 도구 폴더 / 본 spec 범위 밖)
   (4) wrapper 실행 검증: 완료
       - `Unblock-File` 적용 완료
       - `ExecutionPolicy Bypass` 방식으로 실행 가능 확인
       - `VIEW_AWS_PAPER_STEPFUNCTIONS_ENV_READY` 출력 확인
       - AWS Step Functions ARN set 확인(일반 + approval 2종 모두 set / 실제 ARN 본 노트 평문 기록 0건)
       - Spring profile `aws-paper` 확인
       - DB 접속 `jdbc:postgresql://127.0.0.1:15433/portfolio` 확인
       - View DB user `view_app` 확인
       - Tomcat 8080 기동 확인
       - `PortViewApplication started` 확인
 6) Docker / ECR / ECS Task Definition: 미완료
   (1) Dockerfile 작성
       - Spring Boot jar 기반 port-view image 생성
       - 로컬 `C:/Workspaces` 경로 의존 제거
       - secret / password / token image 포함 금지
   (2) ECR push
       - `portfolio-view` repository 또는 기존 naming convention 에 맞춰 image push
       - tag 는 paper 날짜 또는 `paper-latest` 기준으로 관리
   (3) ECS Task Definition 등록
       - `SPRING_PROFILES_ACTIVE=aws-paper,aws-paper-ecs`
       - RDS 접속정보는 Secrets Manager 또는 SSM SecureString 주입
       - ECS Task Role 에 `states:StartExecution` 최소 권한 부여(특정 state machine ARN 한정 권장 / 일반 + approval 2종 모두 Resource 한정 / R-AUTO-034 신규 mitigation 정합 / 06 spec 후속 phase 책임)
       - CloudWatch Logs 연결
       - `local-file-execution-enabled=false`
 7) Fargate 검증: 미완료
   (1) 조회-only 기동
       - ECS Service 기동
       - `/dashboard` 조회
       - `/balance-summary` 조회
       - `/positions` 조회
       - `/orders` 조회
       - `/strategy` 조회
       - `/daily-batch` 조회
   (2) Step 1~11 `StartExecution`
       - Fargate View 에서 Step 1~11 `StartExecution` 호출
       - `executionArn` 반환 확인
       - `allowPaperOrderExecute=false` 확인
       - Step 12~17 차단 확인
       - DB run / step log 조회 확인
   (3) 승인형 Step 12~17
       - preflight 후 승인 버튼 활성
       - `allowPaperOrderExecute=true` (boolean) + `paperOrderEnabled=true` (boolean) payload 명시 확인
       - approval state machine ARN(`portfolio-paper-daily-step12-17-approval`) 사용 확인
       - Step 12~17 실행 결과 확인
       - 신규 주문 생성 / broker 제출 여부 후검증
       - 문제 없을 때만 운영 runbook 에 반영

### 결론

- Local View 기준으로 `local-file` backend 와 `aws-stepfunctions` backend 모두 Step 1~17 전체 실행 가능한 운영자용 wrapper 가 정리되었다.
- AWS Step Functions backend 는 Step 1~11 safe trigger 와 Step 12~17 approval trigger 를 모두 로컬에서 검증 완료했다.
- Step 12~17 approval trigger 는 `portfolio-paper-daily-step12-17-approval` state machine 에서 `SUCCEEDED` 로 완료했고, DB 후검증상 신규 주문은 생성되지 않았다.
- 따라서 Local View 기반 Step Functions 연동 선검증은 완료되었고, 다음 단계는 Docker / ECR / ECS Task Definition / Fargate 조회-only smoke test 이다.

### 결정 / 리스크 매핑

- OD-MS-002(port-view 컴퓨트 = ECS Fargate Service 1순위) 정합 — 본 일자 결정 본문 변경 없음 / 1차 실증 메모 보강.
- OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) 정합 — Batch 실행 책임이 View 내부 subprocess 가 아니라 Step Functions `StartExecution` 으로 이관 / 자동 trigger 는 기존 EventBridge Scheduler 한정 그대로 유지.
- OD-MS-037(View Local AWS Paper read-only 1차 scope + ECS / Fargate 진입 전 batch 2차 검증 선행 정책) 정합 — Fargate 진입 전 local `aws-stepfunctions` mode Step 1~11 trigger 선검증 완료.
- OD-SAFE-001 ~ OD-SAFE-004(자동 BUY · SELL E2E 단계적 도입 / 자동 재시도 금지 idempotent 한정) 정합 — `StepFunctionsDailyBatchExecutionService` 의 서비스 레벨 안전 gate 1차 실증.
- R-AUTO-033 [2026-06-29 보강 (2)] — port-view Step Functions trigger 분리 + Step 1~11 검증 통과 / Status `Mitigated` 유지.
- R-AUTO-034 신규 — Fargate View `states:StartExecution` 권한 과다 부여 + Step 12 gate 우회 위험 / Status `Open` / Task Role 특정 state machine ARN 한정 + Fargate 안전 기본값 + 서비스 레벨 안전 gate + executionArn redaction + Step 12~17 승인형 / preflight / paper-order gate 분리 mitigation.

### 본 일자 사실 기록 범위

- 본 일자 Kiro 작업 = 05 spec `operation-notes.md` 신규 생성 + 본 6 · 7 섹션 1차 작성.
- 운영자 직접 commit `e72de6f`(`feat(view): add Step Functions daily batch trigger`) 의 코드 변경분은 port-view MS 영역으로 cross-service AWS Migration spec 본 일자 작업으로 인한 변경 0건(spec 영역).
- AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 본 일자 변경 0건.
- AWS 리소스 신규 생성 · 수정 · 삭제 본 일자 변경 0건.
- broker / KIS 호출 본 일자 신규 변경 0건(검증 시점은 운영자 직접 로컬 실행 한정 / 신규 broker 주문 제출 0건 / Step 12~17 approval gate 차단 유지 / aws-live 작업 0건).
- 민감정보(secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN / broker_order_no 원문 / Slack webhook URL / DB password / Administrator password / 실제 state machine ARN) 본 노트 평문 기록 0건.
- 운영 식별자(port-view commit hash `e72de6f` / commit message `feat(view): add Step Functions daily batch trigger` / Class 이름 `StepFunctionsDailyBatchExecutionService` / Controller endpoint path `/daily-batch/aws-stepfunctions/start-range` / Spring properties key 라벨 / StartExecution payload 필드 라벨 + `runDate`(Asia/Seoul yyyy-MM-dd) / Step Functions state name `StopCrawlerEc2AfterStep11Success` · `SendApprovalRequiredSlack` · `Step6ToStep11_Succeeded` · `Step12_CheckApproval` / Slack 이벤트 라벨 `APPROVAL_REQUIRED` / 에러 라벨 `States.Runtime` / Spring profile `aws-paper`) 는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.


## 2026-06-30 — View 운영 경로 4종 정리 완료 + Daily Batch gate 운영 의도 정합 수정 + DB 검증 쿼리 작성 원칙 추가

본 일자 운영자가 직접 수행한 `DailyBatchController.java` Daily Batch gate 운영 의도 정합 수정 + Local View → AWS Step Functions Step 12~17 승인 실행 2차 실증 통과 결과를 05 spec 의 ECS Fargate 포팅 관점에서 누적 기록한다. 본 노트는 port-view 측 코드 본문 / IAM Policy / ASL / 응답 본문 / `StartExecution` 입력 JSON 본문 / DB 후검증 raw output 전문 평문 인용 0건(R-DOCS-001 정합). Step Functions 측 상세 사실은 [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-30 섹션 참조.

8. View 운영 경로 4종 정리: 완료
 1) Local View 측 운영자 수동 trigger 분리 4종: 완료
   (1) Local View → Local File Step 1 단독 실행: 완료(2026-06-28 Run #46)
   (2) Local View → Local File Step 1~11 실행: 완료(2026-06-28 Run #47)
   (3) Local View → Local File Step 12~17 실행: 완료(2026-06-29 (1) Run #48)
   (4) Local View → AWS Step Functions Step 12~17 승인 실행: 완료(본 일자)
       - executionName `port-view-daily-step12-17-20260630-095111-aae2595c`
       - state machine `portfolio-paper-daily-step12-17-approval`
       - status `SUCCEEDED`
       - start `2026-06-30T09:51:11.903+09:00`
       - stop `2026-06-30T09:54:16.484+09:00`
       - 주문 대상 없음 상태에서 안전 종료
       - DB 후검증 통과(신규 `connector_order_request` 0건 / 신규 broker 주문 0건)
 2) 운영 경로 분리 정합: 완료
   (1) Local File 실행과 AWS Step Functions 실행 분리 동작 정합
       - Local File 실행 gate: `localFileExecutionEnabled`
       - AWS Step Functions 실행 gate: `awsStepfunctionsStartEnabled` / `awsStepfunctionsStepStartEnabled`
       - approval range gate: `paperOrderEnabled=true` + approval ARN set
   (2) Step 12~17 주문성 구간 별도 승인형 state machine + paper-order gate 통과 시에만 실행 정합

9. Daily Batch gate 운영 의도 정합 수정: 완료
 1) `DailyBatchController.java` 수정: 완료
   (1) AWS Step Functions 버튼 활성 조건 수정
       - `fullPipelineExecutionEnabled=true` 상태에서도 AWS Step 12~17 승인 버튼 활성
       - `paperOrderEnabled=true` 상태에서도 AWS Step 1~11 버튼 조건과 충돌 회피
       - Local File 실행 gate 와 AWS Step Functions 실행 gate 분리 유지
       - Step 12~17 은 `paperOrderEnabled=true` + approval range gate 통과 시에만 실행
       - 본 노트 Java 본문 / commit diff 평문 인용 0건(R-DOCS-001 정합)
   (2) 검증
       - mvn compile 성공
       - Local View `aws-paper` profile 재기동 성공
       - 화면 표시 통과(Execution ON / Local File OFF / Full Pipeline ON / Paper Order ON / 허용 범위 `1~17` / AWS Step 12~17 승인 실행 버튼 활성)

10. DB 검증 쿼리 작성 원칙 추가: 미완료 (정식 반영은 후속 phase 책임)
 1) 본 일자 식별된 운영 원칙: 완료 (사실 기록)
   (1) 컬럼명 사전 확인 의무화
       - `information_schema.columns` 로 대상 컬럼 사전 확인 후 SELECT
       - 본 일자 `connector_position_snapshot.balance_snapshot_id` 컬럼 부재 사례 식별
   (2) 확인된 컬럼만 SELECT
       - 관계 컬럼(예: `balance_snapshot_id`) 도 예상 사용 금지
       - 부재 시 `account_no` + `as_of_date` 같은 자연키로 검증
   (3) 결과 노출 패턴
       - 후검증 쿼리는 `DO` / `EXECUTE` 로 결과 숨김 금지
       - 최종 SELECT 결과가 화면에 직접 나오게 작성
   (4) Windows / PowerShell / psql 환경 정합
       - 한글 SQL 은 `psql -c` 직접 실행 대신 UTF-8 No BOM `.sql` 파일 + `psql -f` 패턴 유지
       - SSM multiline command 는 UTF-8 No BOM JSON 파일 + `--parameters file://...` 패턴 유지
 2) 정식 반영 후속 (05 spec 후속 phase 책임)
   (1) `validation-checklist.md` 또는 동등 문서 신규 생성 시 본 원칙 정식 반영
   (2) Fargate cutover 시점 cross-spec audit 항목으로 등록
   (3) DB 후검증 쿼리 모음 정리 시 본 원칙 정합

11. Daily Batch 화면 문구 정리: 미완료 (후속 phase 책임)
 1) `local-file` 중심 문구에서 `aws-stepfunctions` mode 도 명확히 보이도록 정리: 미완료
   (1) 후속 작업
       - `daily_batch.html` 의 현재 모드 표시 / 버튼 라벨 / 실행 이력 라벨 등 정리
       - Fargate 진입 시점에 `local-file` 버튼 제외 또는 별도 운영자 전용 경로 분리 결정
       - 본 일자 코드 변경 0건 / 추가 변경 시점에 별도 commit
   (2) 결정 매핑
       - OD-MS-002 / OD-MS-037 정합
       - R-AUTO-034 mitigation 확장 결합

### 결정 / 리스크 매핑 (2026-06-30)

- OD-MS-002 / OD-MS-009 / OD-MS-037 / OD-SAFE-001 ~ OD-SAFE-004 본문 변경 없이 1차 실증 메모 보강(2026-06-29 (3) Change Log 항목 정합 그대로 유지 / 본 일자 신규 결정 없음 / Decision Summary 카운트 변경 없음).
- R-AUTO-033 [2026-06-30 보강] — Daily Batch gate 운영 의도 정합 + AWS Step Functions Step 12~17 승인 실행 2차 실증 / Status `Mitigated` 유지.
- R-AUTO-034 [2026-06-30 보강] — View 측 4가지 운영 경로 분리 1차 실증 + DB 검증 쿼리 작성 원칙 보강 / Status `Open` 유지 / Fargate Task Role 권한 분리는 06 spec 후속 phase 책임 그대로 유지.

### 본 일자 사실 기록 범위 (2026-06-30)

- 본 일자 Kiro 작업 = 05 spec `operation-notes.md` 본 섹션 누적(8 · 9 · 10 · 11 항목) + 04 spec `operation-notes.md` append(Step Functions 외부 caller 측 사실) + `_common` 2개(`followups-overview.md` 2026-06-30 후속 메모 / `risk-register.md` R-AUTO-033 + R-AUTO-034 보강) + `.kiro` 루트 2개(`WORKLOG.md` / `CHANGELOG.md` 2026-06-30 섹션 prepend).
- 운영자 직접 변경분(`DailyBatchController.java` Daily Batch gate 수정) 은 port-view MS 영역으로 cross-service AWS Migration spec 본 일자 작업으로 인한 변경 0건(spec 영역).
- AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 본 일자 변경 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 0건 / commit/add/reset/checkout/stash 0건.
- broker / KIS 호출 = 오전 Step 1~11 자동 trigger 한정(`connector_order_request` 신규 0건) + Local View → AWS Step Functions Step 12~17 승인 실행 1건(`SUCCEEDED` / NO_TARGET / broker 주문 제출 0건) + balance refresh 한정 / 추가 BUY · SELL · 취소 · 정정 0건 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건.
- 민감정보(secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN / broker_order_no 원문 / Slack webhook URL / DB password / Administrator password / 실제 state machine ARN) 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder.
- 운영 식별자(executionName `port-view-daily-step12-17-20260630-095111-aae2595c` / state machine 이름 `portfolio-paper-daily-step12-17-approval` / Controller class `DailyBatchController` / Daily Batch gate 라벨 6종 / 화면 표시 라벨 / balance snapshot `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505` · `eval_profit=0` · `source_version=connector-intraday-snapshot-refresh-1.0.0` / DB 컬럼명 `balance_snapshot_id`(부재) · `account_no` · `as_of_date` / Run id `#46` · `#47` · `#48` / Spring profile `aws-paper` / start · stop timestamp) 는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.


3. ECS Fargate 포팅: 완료
 1) 1차 접근 방식
   (1) ALB 미사용 Public IP 직접 접근
       - ECS Fargate task 를 public subnet 에 배치
       - assign public IP enabled 설정
       - ALB 는 생성하지 않음
       - NAT Gateway 는 생성하지 않음
       - Fargate task public IP 와 port 8080 으로 View 접속
       - 접근 URL 형식은 `http://<FARGATE_TASK_PUBLIC_IP>:8080`
       - task 재시작 또는 재배포 시 public IP 가 변경될 수 있음을 전제로 운영
   (2) 보안 원칙
       - Security Group inbound 는 TCP 8080 만 허용
       - source 는 운영자 공인 IP/32 만 허용
       - 초기 검증 중 0.0.0.0/0 전체 오픈은 금지
       - 모바일 접근은 같은 Wi-Fi 또는 임시 모바일 공인 IP/32 추가 방식으로 확인
       - 확인 완료 후 불필요한 inbound rule 은 제거
   (3) 비용 절감 원칙
       - ALB 비용 제거
       - NAT Gateway 비용 제거
       - public IPv4 비용과 Fargate 실행 시간 비용만 부담
       - ECS service desired count 는 필요할 때만 1
       - 확인 완료 후 desired count 0 전환 가능하도록 운영
       - CloudWatch Logs retention 은 짧게 설정(7일)
 2) 1차 목표
   (1) read-only View 배포
       - Local View 에서 검증된 AWS Paper View 를 ECS Fargate 에 배포
       - 초기 배포 범위는 read-only View 우선
       - Dashboard / Balance / Positions / Orders / Reports / Daily 화면 확인
       - Spring Boot property 구조 변경은 최소화
       - PowerShell 구동 스크립트에서 주입하던 환경변수를 ECS Task Definition 환경변수로 이관
   (2) Step Functions 버튼 safe gate 확인
       - AWS Step 1~11 버튼 표시 여부 확인
       - AWS Step 12~17 승인 버튼 표시 여부 확인
       - `fullPipelineExecutionEnabled` / `paperOrderEnabled` 조건 충돌 없음 확인
       - 실제 Step Functions 실행은 read-only 화면 검증 후 별도 승인 단계에서 진행
   (3) ECS View 에서 Step Functions 수동 실행 검증
       - ECS View Daily 화면에서 AWS Step 12~17 승인 실행 버튼 클릭
       - ECS task role 을 통해 AWS Step Functions `StartExecution` 호출
       - `portfolio-paper-daily-step12-17-approval` state machine 실행
       - 실행 완료 후 Slack `DAILY_EXECUTION_SUCCESS` 수신
       - DB after-check 정상 확인
 3) 완료 기준
   (1) ECS 배포 완료 기준
       - Docker image build 성공
       - ECR image push 완료
       - ECS task definition 생성 완료
       - ECS service desired count 1 기동 성공
       - ECS task RUNNING 확인
       - CloudWatch Logs 에서 Spring Boot started 확인
   (2) 네트워크 완료 기준
       - Fargate task public IP 확인
       - `http://<FARGATE_TASK_PUBLIC_IP>:8080` 접속 성공
       - Security Group inbound TCP 8080 source 운영자 IP/32 확인
       - ECS task 에서 Private RDS 연결 성공
       - NAT Gateway 없이 RDS 접근 구성 확인
   (3) View 완료 기준
       - Dashboard 조회 성공
       - Balance 조회 성공
       - Positions 조회 성공
       - Orders 조회 성공
       - Reports 조회 성공
       - Daily 조회 성공
       - 최초 접속 및 메뉴 이동 시 기본 계좌번호 정상 반영
   (4) safe gate 완료 기준
       - AWS Step 1~11 버튼 조건 확인
       - AWS Step 12~17 승인 버튼 조건 확인
       - `fullPipelineExecutionEnabled=true` 상태에서도 Step 12~17 승인 버튼 표시 확인
       - `paperOrderEnabled=true` 상태에서도 Step 1~11 버튼 조건 충돌 없음 확인
       - local-file 실행 버튼은 ECS 환경에서 비활성 상태 확인
       - 전체 실행 버튼은 잠금 상태 확인
   (5) Step Functions 실행 완료 기준
       - ECS View 에서 AWS Step 12~17 승인 실행 버튼 클릭 성공
       - execution `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` 생성 확인
       - state machine `portfolio-paper-daily-step12-17-approval` 실행 확인
       - execution status `SUCCEEDED` 확인
       - Slack `DAILY_EXECUTION_SUCCESS` 수신 확인
       - after-check 결과 REQUESTED strategy order 0 확인
       - after-check 결과 retryable rejected strategy order 0 확인
       - after-check 결과 active connector order 0 확인
       - after-check 결과 당일 신규 connector order 0 rows 확인
       - latest connector balance snapshot 기준일 2026-06-30 확인
 4) 완료 결과
   (1) AWS 리소스
       - ECS cluster: `portfolio-paper-cluster`
       - ECS service: `portfolio-view-service`
       - ECS task definition: `portfolio-view:2`
       - ECR repository: `portfolio-view`
       - CloudWatch Logs group: `/ecs/portfolio-view`
       - task execution role: `portfolio-paper-ecs-task-execution-role` (실제 ARN 평문 기록 0건 / `[REDACTED_ARN]`)
       - task role: `portfolio-paper-view-task-role` (실제 ARN 평문 기록 0건 / `[REDACTED_ARN]`)
       - security group: `sgroup-port-view-ecs`
   (2) ECS task definition 주요 설정
       - launch type: FARGATE
       - network mode: awsvpc
       - cpu: 512
       - memory: 1024
       - container port: 8080
       - Spring profile: `aws-paper`
       - default account no: `[REDACTED_ACCOUNT_NO]`
       - local file execution: false
       - execution mode: `aws-stepfunctions`
       - Step Functions start enabled: true
       - Step Functions step start enabled: true
       - full pipeline execution enabled: true
       - paper order enabled: true
   (3) 검증된 접속 결과
       - Fargate public IP 직접 접속 성공(public IP 평문 기록 0건 / `[REDACTED_PUBLIC_IP]`)
       - Spring Boot started 확인
       - HikariPool RDS connection 성공
       - default schema `ops` 확인
       - Dashboard / Balance / Positions / Orders / Reports / Daily 화면 정상 표시
       - 기본 계좌번호 누락 문제는 task definition revision 2 에서 env 추가로 보정 완료(`PORTFOLIO_BATCH_DEFAULT_ACCOUNT_NO` + `PORTFOLIO_VIEW_ACCOUNT_DEFAULT_ACCOUNT_NO`)
   (4) 검증된 실행 결과
       - ECS View Daily 화면에서 AWS Step 12~17 승인 실행 성공
       - execution `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8`
       - start: 2026-06-30 14:15:42 KST
       - stop: 2026-06-30 14:18:48 KST
       - status: SUCCEEDED
       - history 최종 `ExecutionSucceeded`
       - Slack `DAILY_EXECUTION_SUCCESS` 수신
       - after-check 정상
   (5) after-check 결과
       - REQUESTED strategy orders after: 0
       - retryable rejected strategy orders after: 0
       - active connector orders after: 0
       - today connector orders after: 0 rows
       - latest connector_balance_snapshot id: 281
       - latest connector_balance_snapshot as_of_date: 2026-06-30
       - total_eval_amount: 8,706,505
       - cash_balance: 8,706,505
       - 과거 stale connector order 6건 식별 — 모두 운영 계좌가 아닌 과거 테스트 계좌의 2026-04-27 ACCEPTED 잔여 / 이번 Step 12~17 실행과 무관 / 후속 cleanup 후보로 분리
   (6) 비용 절감 종료 결과
       - 검증 완료 후 ECS service desired count 0 전환 완료
       - Fargate task 종료 완료
       - public IP 해제 전제로 운영
       - 다음 기동 시 desired count 1 전환 후 새 public IP 확인 필요
 5) 보류 항목
   (1) 1차 포팅 이후로 보류
       - ALB 생성
       - HTTPS 정식 구성
       - Route53 도메인 연결
       - Cloudflare Tunnel
       - 인증 / 인가 고도화
       - Slack 문구 개선
       - property 구조 재정리
       - application-ecs.yml 신규 분리
       - ECS Auto Scaling
       - Blue/Green 배포
   (2) 후속 정리 후보
       - Daily 화면 문구에서 "로컬 실행 검증" 표현을 ECS / AWS mode 에 맞게 수정
       - stale `connector_order_request` 과거 ACCEPTED 6건 처리 여부 검토
       - ECS service desired count 0/1 운영 명령 문서화(runbook.md 후속)
       - 운영자 IP 변경 시 Security Group inbound 갱신 절차 문서화
       - 필요 시 AWS Step 1~11 ECS View 실행 별도 검증
 6) 결론
   (1) 1차 포팅 방향
       - ALB 없이 ECS Fargate Public IP 직접 접근 방식으로 진행
       - 최소 비용으로 View ECS 배포 완료
       - 운영자 IP 제한으로 외부 노출 범위 최소화
       - Spring Boot property 구조 변경 없이 ECS Task Definition env 이관 방식으로 완료
       - 이후 필요 시 ALB / HTTPS / Cloudflare Tunnel 은 별도 검토
   (2) 완료 판정
       - ECS Fargate View 접속 성공
       - read-only 화면 정상 조회
       - Private RDS 연결 정상
       - 기본 계좌번호 정상 반영
       - Step Functions 버튼 조건 정상
       - ECS View 에서 AWS Step 12~17 승인 실행 성공
       - Slack 성공 알림 수신
       - DB after-check 정상
       - desired count 0 종료 완료
       - **3. ECS Fargate 포팅: 완료**

### 운영 절차 (runbook 후속 책임 / 본 노트는 사실 기록)

운영자 명령 예시는 ARN / task ARN / ENI ID / LOG_STREAM 수동 치환 없이 `list/describe → 변수 추출 → 후속 검증` 패턴을 따른다. 본 노트는 사실 기록만 담고 정식 runbook 은 05 spec 후속 phase 책임.

ECS View 기동:
 1) ECS service desired count 1 전환
   (1) `aws ecs update-service --cluster portfolio-paper-cluster --service portfolio-view-service --desired-count 1`
   (2) `aws ecs wait services-stable` 로 `RUNNING` 진입 대기
 2) public IP 자동 조회
   (1) `aws ecs list-tasks --cluster portfolio-paper-cluster --service-name portfolio-view-service` → `TASK_ARN` 추출
   (2) `aws ecs describe-tasks --cluster portfolio-paper-cluster --tasks $TASK_ARN` → `ENI_ID` 추출(`attachments[].details[?name=='networkInterfaceId'].value`)
   (3) `aws ec2 describe-network-interfaces --network-interface-ids $ENI_ID` → `PUBLIC_IP` 추출(`Association.PublicIp`)
   (4) 브라우저 접속 URL 출력: `http://$PUBLIC_IP:8080`(본 노트 평문 기록 0건 / `[REDACTED_PUBLIC_IP]`)

ECS View 종료:
 1) ECS service desired count 0 전환
   (1) `aws ecs update-service --cluster portfolio-paper-cluster --service portfolio-view-service --desired-count 0`
   (2) Fargate task 종료 / public IP 해제
 2) 다음 기동 시 주의
   (1) desired count 0 → 1 전환 시 새로운 public IP 가 발급됨
   (2) 운영자가 새 public IP 를 다시 조회해 브라우저 URL 갱신
   (3) Security Group inbound rule 의 운영자 IP/32 는 그대로 유지

### 검증 체크리스트 (validation-checklist 후속 책임 / 본 노트는 사실 기록)

본 일자 검증 통과 항목:
 1) Docker image build: 완료
 2) ECR push: 완료
 3) task definition registration: 완료(revision 1 → 2 보정)
 4) ECS service `portfolio-view-service` RUNNING: 완료
 5) CloudWatch Logs `/ecs/portfolio-view` 에서 Spring Boot started: 완료
 6) RDS connection success(HikariPool start completed): 완료
 7) Dashboard / Balance / Positions / Orders / Reports / Daily 화면 조회: 완료
 8) AWS Step 1~11 버튼 표시: 완료
 9) AWS Step 12~17 승인 버튼 표시: 완료
 10) ECS View → Step 12~17 execution `SUCCEEDED`: 완료(executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8`)
 11) Slack `DAILY_EXECUTION_SUCCESS` 수신: 완료
 12) DB after-check 0건 확인: 완료(REQUESTED / retryable rejected / active / today connector orders 모두 0)
 13) desired count 0 종료: 완료

### 결정 / 리스크 매핑 (2026-06-30 오후)

- OD-MS-002(port-view 컴퓨트 = ECS Fargate Service 1순위) 정합 — 본 일자 실증 완료(ALB 없이 Public IP direct access + 운영자 IP/32 SG inbound + desiredCount 0/1 수동 운영 / 결정 본문 변경 없이 evidence 보강).
- OD-MS-009(Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) 정합 — ECS View 가 Step Functions `StartExecution` external caller 로 붙는 세 번째 phase 1차 실증(Local View → AWS Step Functions Step 1~11 / Local View → AWS Step Functions Step 12~17 approval / ECS View → AWS Step Functions Step 12~17 approval).
- OD-MS-037(View Local AWS Paper read-only 1차 scope + ECS / Fargate 진입 전 batch 2차 검증 선행 정책) 정합 — Local 검증(2026-06-27 read-only / 2026-06-28 Step 1 + Step 1~11 / 2026-06-29 (1) Step 12~17 local-file / 2026-06-29 (2) Step 1~11 aws-stepfunctions / 2026-06-29 (3) + 2026-06-30 오전 Step 12~17 approval) 후 본 일자 오후 ECS Fargate 진입 + Step 12~17 ECS View 승인 실행 완료.
- R-AUTO-033 [2026-06-30 오후 보강] — ECS Fargate Public IP direct access + 운영자 IP/32 SG inbound + ECS View → AWS Step Functions Step 12~17 approval 3차 실증 / Status `Mitigated` 유지.
- R-AUTO-034 [2026-06-30 오후 보강] — Fargate ECS task role(`portfolio-paper-view-task-role`) 의 `states:StartExecution` 권한이 실제 Fargate 환경에서 1차 실증 통과 / Step 12~17 approval state machine ARN 한정 부여 사실 / Status `Open` 유지(향후 ALB · HTTPS · CloudWatch alarms 도입 시 cross-spec audit / 06 spec 후속 phase 책임).
- 신규 후속 리스크 보강 (followups-overview 2026-06-30 오후 후속 메모 + risk-register 참조):
       - Public IP 직접 접근 시 SG inbound 오픈 실수 위험(운영자 IP/32 한정 정책 정합)
       - desiredCount 1 유지로 인한 불필요한 Fargate / public IPv4 비용 누적 위험(desiredCount 0 종료 운영 정책 정합)
       - task 재시작 후 public IP 변경으로 접속 URL 이 바뀌는 위험(운영자가 매 기동 시 재조회 정책)
       - AWS CLI / psql 검증 쿼리에서 추정 컬럼명을 사용해 오진하는 위험(`information_schema.columns` 사전 확인 정책 정합 / 2026-06-29 (1) DB password 노출 + 2026-06-30 오전 `connector_position_snapshot.balance_snapshot_id` 부재 사례 결합)
       - 과거 stale `connector_order_request` 가 preflight count 를 오염시키는 위험(2026-06-30 오후 식별된 6건 / 후속 cleanup 결정)

### 본 일자 사실 기록 범위 (2026-06-30 오후)

- 본 일자 Kiro 작업 = 05 spec `operation-notes.md` 본 섹션(3. ECS Fargate 포팅: 완료) 누적 + `_common` 5개(`followups-overview` · `operator-decisions` · `ms-aws-service-decision-matrix` · `cost-simulation` · `risk-register` · `aws-resource-glossary`) + 04 spec `operation-notes.md` append + 06 spec `operation-notes.md` append + `.kiro` 루트 3개(`WORKLOG.md` / `CHANGELOG.md` / `README.md` 짧은 상태 보강) + `.kiro/AGENTS.md` 운영 명령 작성 규칙 보강.
- 운영자 직접 수행 작업(Dockerfile / `.dockerignore` 추가 + Docker image build + ECR push + ECS Task Definition 등록 + ECS Service 생성 + Security Group · CloudWatch Logs · IAM Role 구성 + ECS View 접속 + AWS Step 12~17 승인 실행 클릭 + desiredCount 0 종료)은 port-view MS 및 AWS 운영자 영역으로 cross-service AWS Migration spec 본 일자 작업으로 인한 변경 0건(spec 영역).
- AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 본 일자 Kiro 측 변경 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 본 일자 Kiro 측 변경 0건 / commit/add/reset/checkout/stash 0건.
- broker / KIS 호출 = 오전 Step 1~11 자동 trigger 한정 + Local View → Step 12~17 승인 실행(2026-06-30 오전) + ECS View → Step 12~17 승인 실행(2026-06-30 오후 / executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / `SUCCEEDED` / NO_TARGET / broker 주문 제출 0건) + balance refresh 한정 / 추가 BUY · SELL · 취소 · 정정 0건 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건.
- 민감정보(secret value / KIS app key / KIS app secret / 계좌번호 12자리 원문 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / instance-id / EIP / public IP / image digest full sha256 / task ARN / ENI ID / job ARN / broker_order_no 원문 / Slack webhook URL / DB password / Administrator password / 실제 state machine ARN) 본 노트 평문 기록 0건 — 모두 `[REDACTED]` / `[REDACTED_ACCOUNT_NO]` / `[REDACTED_PUBLIC_IP]` / `[REDACTED_ARN]` / `[REDACTED_TASK_ARN]` / `[REDACTED_SECRET_ARN]` / `[REDACTED_BROKER_ORDER_NO]` placeholder.
- 운영 식별자(ECS cluster `portfolio-paper-cluster` / ECS service `portfolio-view-service` / ECS task definition `portfolio-view:2` / ECR repository `portfolio-view` / CloudWatch Logs group `/ecs/portfolio-view` / Security Group `sgroup-port-view-ecs` / task execution role 이름 `portfolio-paper-ecs-task-execution-role` / task role 이름 `portfolio-paper-view-task-role` / executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / state machine `portfolio-paper-daily-step12-17-approval` / Spring profile `aws-paper` / Tomcat port `8080` / start · stop timestamp `2026-06-30T14:15:42.899+09:00` ~ `2026-06-30T14:18:48.358+09:00` / status `SUCCEEDED` / balance snapshot `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505` / 화면 라벨 6종(Dashboard / Balance / Positions / Orders / Reports / Daily) / launch type `FARGATE` / network mode `awsvpc` / cpu 512 / memory 1024 / container port 8080 / Spring properties env label / Slack 이벤트 라벨 `DAILY_EXECUTION_SUCCESS` / stale `connector_order_request` 6건 식별 사실 / `connector_position_snapshot.balance_snapshot_id` 컬럼 부재 사실) 는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.


## 2026-06-30 (오후) — Daily Brief Slack 자동화 독립 운영 cross-reference

같은 일자 오후의 port-view ECS Fargate 1차 포팅 + ECS View → AWS Step Functions Step 12~17 승인 실행(앞의 "3. ECS Fargate 포팅: 완료" block) 과 별도로, Daily Brief Slack 자동화가 본 일자 오후에 추가로 운영자 직접 작업으로 구성된 사실을 cross-reference 한다. 본 노트는 Lambda 코드 본문 / Step Functions ASL 본문 / Scheduler target JSON 본문 / Slack 메시지 본문 / Builder output 전문 / Notifier input 전문 / CloudWatch Logs 전문 / IAM Policy 본문 평문 인용 0건(R-DOCS-001 정합).

12. Daily Brief Slack 자동화 cross-reference: 사실 기록
 1) Daily Brief 알림은 Daily 본 실행 / ECS View / MarketConnector EC2 와 모두 독립 운영: 완료
   (1) Daily 본 실행 state machine 과 분리
       - Daily Brief 알림은 `portfolio-paper-daily-step1-17-approval` / `portfolio-paper-daily-step12-17-approval` 와 별도 mini Step Functions `portfolio-daily-brief-slack-notification` 책임
       - Daily 본 실행 실패가 Daily Brief Slack 발송에 영향을 주지 않고, Daily Brief Slack 실패가 Daily 본 실행에 영향을 주지 않도록 격리
       - OD-MS-038 신규 정합
   (2) MarketConnector EC2 start · stop 과 독립 운영
       - MarketConnector EC2 의 07:50 KST start / 15:50 KST stop(OD-MS-034 정합) 과 Daily Brief Slack 의 07:50 KST 장전 발송 / 15:50 KST 장후 발송은 시간대만 동일 / Target / Lambda / IAM Role 모두 독립
       - Daily Brief Builder Lambda `portfolio-daily-brief-slack-summary-builder` 는 RDS read 만 수행 / MarketConnector EC2 가 stop 상태여도 정상 동작
       - EC2 lifecycle Lambda `portfolio-paper-ec2-lifecycle-dispatcher` 와 책임 분리 / IAM Role / 호출 경로 / Target 모두 독립
   (3) ECS View 와 독립 운영
       - ECS View(`portfolio-view-service`) 의 desiredCount 0/1 운영과 무관하게 Daily Brief Slack 은 자동 발사
       - port-view 의 기존 `SlackNotificationService` 는 제거되지 않고 유지(View Daily Batch 수동 실행 결과 알림 책임)
       - AWS 공통 Slack notifier(`portfolio-event-notifier`) 는 운영 이벤트 알림 단일 진입점으로 별도 분리(OD-MS-030 정합)
 2) 본 일자 운영자 직접 신규 작업 사실
   (1) Builder Lambda 2종 신규
       - `portfolio-approval-slack-summary-builder` — Daily 본 실행 측 Approval Required Slack builder
       - `portfolio-daily-brief-slack-summary-builder` — Daily Brief Slack builder(Python 3.12 + `pg8000` + `DB_PASSWORD_SECRET_VALUE_FROM` Secrets Manager `valueFrom`)
   (2) mini Step Functions 신규
       - `portfolio-daily-brief-slack-notification`(ACTIVE / 구조 `BuildDailyBriefPayload → SendSlackNotifier`)
   (3) IAM Role 2종 신규
       - `portfolio-daily-brief-sfn-role`(Builder + Notifier Lambda invoke 한정 / Resource · Action wildcard 0건)
       - `portfolio-daily-brief-scheduler-role`(Daily Brief state machine StartExecution 한정 / Resource · Action wildcard 0건)
   (4) Scheduler 2개 ENABLED 추가
       - 장전 `portfolio-daily-brief-morning-slack-0750-kst`(cron `cron(50 7 ? * MON-FRI *)` / Asia/Seoul / Flexible OFF / input `MORNING_BRIEF`)
       - 장후 `portfolio-daily-brief-evening-slack-1550-kst`(cron `cron(50 15 ? * MON-FRI *)` / Asia/Seoul / Flexible OFF / input `EVENING_BRIEF`)
   (5) Notifier formatter 개선
       - eventType alias 2종(`MORNING_BRIEF → PRE_MARKET_STATUS` / `EVENING_BRIEF → POST_MARKET_STATUS`)
       - nested `balance` / `positions` adapter
       - Builder `title` 우선
       - 장후 `어제 대비` 표시
       - 손익 prefix 규칙(음수 `🔵` / 양수 `🔴` / 0 `⚪`) 일관 적용
   (6) smoke 통과
       - morning smoke `daily-brief-morning-smoke-safe-20260630-193255-68f50aeb` `SUCCEEDED`
       - evening smoke `daily-brief-evening-smoke-safe-20260630-193300-aa2b9a12` `SUCCEEDED`
       - Slack 장전 · 장후 수신 확인
       - latest balance snapshot `id=281` / `as_of_date=2026-06-30` / `total_eval_amount=8,706,505` / `cash_balance=8,706,505` / `cumulativeProfitRate=-12.94%` / `cumulativeProfitAmount=-1,293,495` / `positionCount=0` / evening delta `0원`
 3) 본 일자 05 spec 범위 변경 사실
   (1) port-view ECS Fargate task definition `portfolio-view:2` 변경 0건(Daily Brief 자동화는 ECS View 와 무관 / port-view image / SG / CloudWatch Logs `/ecs/portfolio-view` 변경 0건)
   (2) ECS service desiredCount 0/1 운영 정책 그대로 유지(검증 후 desiredCount 0 종료 정합)
   (3) ECS View → AWS Step Functions Step 12~17 승인 실행 흐름 변경 0건
   (4) MarketConnector EC2 / Crawler EC2 lifecycle 자동화 변경 0건

### 결정 / 리스크 매핑 (2026-06-30 오후 Slack)

- OD-MS-002 / OD-MS-009 / OD-MS-030 / OD-MS-031 / OD-MS-037 본문 변경 없이 evidence 보강 / OD-MS-038 신규(Daily Brief Slack mini workflow 운영 방식 / Decision Summary 카운트 96 → 97 / 확정 51 → 52 / 잠정 42 유지)
- R-AUTO-035 신규(Daily Brief Slack 자동 발송 실패 또는 중복 발송 위험 / Status `Mitigated` / 첫 실 자동 발사 검증은 다음 평일 후속)
- R-AUTO-024(Slack webhook URL 평문 노출 위험 / `Accepted`) mitigation 그대로 유지 / 운영 안정화 후 Secrets Manager 또는 SSM SecureString 이전(06 spec 후속 phase 책임)

### 본 일자 사실 기록 범위 (2026-06-30 오후 Slack)

- 본 일자 Kiro 작업 = 05 spec `operation-notes.md` 본 섹션 cross-reference 누적만 수행
- 운영자 직접 수행 영역 = Builder Lambda 2개 신규 + mini state machine 1개 신규 + Scheduler 2개 ENABLED + IAM Role 2종 신규 + Notifier formatter 개선 + `portfolio-paper-daily-step1-17-approval` ASL update
- AWS CLI / boto3 / psql / Lambda 실행 / Step Functions 실행 / Slack webhook / KIS API 호출 본 일자 Kiro 측 변경 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 본 일자 Kiro 측 변경 0건
- Lambda 코드 본문 / Step Functions ASL 본문 / Scheduler target JSON 본문 / Slack 메시지 본문 / Builder output 전문 / Notifier input 전문 / CloudWatch Logs 전문 / IAM Policy 전체 본문 / Slack webhook URL / 실제 IAM Role ARN / 실제 state machine ARN / 계좌번호 12자리 원문 / DB password 평문 인용 0건(R-DOCS-001 정합)
- 운영 식별자(Builder Lambda 이름 2종 / Notifier Lambda 이름 / mini state machine 이름 / IAM Role 이름 2종 / Scheduler 이름 2종 / cron 표현식 2종 / Asia/Seoul / Flexible OFF / eventType alias 4종 + 본 라벨 3종 / Lambda runtime `Python 3.12` / DB driver `pg8000` / DB password 주입 방식 `DB_PASSWORD_SECRET_VALUE_FROM` / Slack webhook 환경변수명 `SLACK_WEBHOOK_URL` / 손익 prefix 라벨 / smoke execution name 2종 / balance snapshot 요약) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님
