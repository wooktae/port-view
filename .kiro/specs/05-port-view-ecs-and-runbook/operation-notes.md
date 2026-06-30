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
