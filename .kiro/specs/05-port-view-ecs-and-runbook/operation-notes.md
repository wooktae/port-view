# Operation Notes — 05-port-view-ecs-and-runbook

This document is an operation note that cumulatively records, by date, the work results performed by the operator / Kiro during 05-port-view-ecs-and-runbook.

- First application environment = `aws-paper` / region = `ap-northeast-2`.
- Target = port-view (Spring Boot / Thymeleaf integrated operations console).
- Scope = port-view's ECS Fargate Service porting plan / implementation / validation results.

## Record format

- Accumulate dated sections in this document.
- Factual identifiers are recorded per the user-specified policy — commit hash / Class name / Controller endpoint path / Spring properties key / StartExecution payload field / Step Functions state name / Slack event label, etc.
- The items below must not be recorded in plaintext in this document (`[REDACTED]` or placeholder):
  - secret value / KIS app key · KIS app secret / account number · account password / token.
  - RDS password / RDS endpoint hostname / raw 12-digit account-id.
  - actual IAM Role ARN · secret ARN · state machine ARN / IAM access key id / instance-id / EIP.
  - image digest full sha256 / task ARN / job ARN / raw broker_order_no.
  - Slack webhook URL / Administrator password.
- No plaintext body quotation (R-DOCS-001 alignment):
  - Lambda code / full IAM Policy / full Step Functions ASL / full Lambda response.
  - full CloudWatch Logs / KIS API response body / full Spring Boot application log.
  - Step Functions execution history body / Slack message body / commit diff body.

## Top summary (Dashboard)

### Per-View-execution-location backend matrix

| # | Execution location | Backend | Batch execution method | Usage condition | Most recent validation |
|---|---|---|---|---|---|
| 1 | Local View | `local-file` | Python subprocess + `C:/Workspaces` local source ProcessBuilder | operator local validation tool (Run #46~#48) | 2026-06-29 (1) Step 12~17 local-file |
| 2 | Local View | `aws-stepfunctions` | Step Functions `StartExecution` (local AWS credential) | pre-validation before Fargate entry | 2026-06-30 morning Step 12~17 approval |
| 3 | ECS Fargate View | `aws-stepfunctions` | Step Functions `StartExecution` (ECS Task Role) | operational View / read-only + approval execution | 2026-06-30 afternoon Step 12~17 approval |

### Automatic execution entry state

- After 2026-06-30 afternoon ECS Fargate View first-porting completion, desiredCount 0 termination (cost saving).
- From 2026-07-01, at 09:01 KST the `portfolio-paper-daily-step12-17-order-0901-kst` Scheduler is ENABLED — automatic execution entry without View manual approval.
- The Daily Batch AWS Step 12~17 approval-execute button inside port-view = redefined into the operator fallback / manual re-execution path.

### Per-date index

| Date | Key result |
|---|---|
| 2026-06-29 (2) | ECS Fargate porting plan fixed + `StepFunctionsDailyBatchExecutionService` implementation + Local View `aws-stepfunctions` mode Step 1~11 · Step 12~17 approval pre-validation |
| 2026-06-30 (morning) | View operational path 4-type organization + Daily Batch gate fix + DB validation principles |
| 2026-06-30 (afternoon, ECS) | ECS Fargate first-porting complete (Public IP direct / desiredCount 0 termination) + ECS View → Step 12~17 approval third validation |
| 2026-06-30 (afternoon, Slack) | Daily Brief Slack automation independent-operation cross-reference (mini state machine + 2 Schedulers ENABLED) |
| 2026-07-01 | Step 12~17 Scheduler ENABLED (View manual approval fallback retained) |

## 2026-06-29 (2) — ECS Fargate porting plan / implementation first-status organization

6. ECS Fargate porting plan: complete
 1) Final porting direction fixed: complete
   (1) View execution location
       - port-view is ported to an ECS Fargate Service
       - View retains the AWS Paper operations console role
       - composed around screen query / execution-history query / approval-trigger UI
       - the actual Batch execution responsibility is transferred to Step Functions, not the View local subprocess
   (2) Preserve the Local File execution method
       - the local-file execution button and execution path on the Local View are preserved as is
       - the existing Local View button → local source ProcessBuilder execution → AWS Paper DB store / query flow is retained as an operator local validation tool
       - the local-file backend is not used in the Fargate execution environment
       - the `C:/Workspaces`-based local source execution structure is not carried into the ECS container
   (3) AWS Step Functions backend addition direction fixed
       - newly add `StepFunctionsDailyBatchExecutionService`
       - separate the Daily Batch execution backend into `local-file` / `aws-stepfunctions`
       - select the execution backend via the `portfolio.batch.execution-mode` value
       - in `aws-stepfunctions` mode, perform only `StartExecution` without directly running the Python script
       - View displays `executionArn` / the execution-request payload / the DB run result on the screen
 2) Fargate entry order after local pre-validation fixed: complete
   (1) Local Step Functions integration pre-validation
       - before Fargate deployment, first validate the Step Functions `StartExecution` integration on the Local View
       - validate by switching only the Daily Batch backend to `aws-stepfunctions`, not by mimicking the local execution environment as Fargate
       - first validate the Controller / Service / DTO / button gate / payload creation / `executionArn` display flow locally
       - afterward, on Fargate, separately validate only the container / IAM / VPC / Secret / RDS access issues
   (2) Connect Step 1~11 first
       - the first connection target is Step 1~11 `StartExecution`
       - execute based on `allowPaperOrderExecute=false`
       - keep Step 12~17 blocked by the approval gate
       - connect starting from the pre-validation path without actual order submission
   (3) Step 12~17 approval-type connection
       - separate Step 12~17 into a dedicated approval button
       - activate the button only behind the paper-order gate
       - retain the policy of confirming, before execution, the REQUESTED `strategy_execution_order` / retryable rejected `connector_order_request` / active `connector_order_request` preflight
       - connect to Fargate after validating Step 12~17 NO_TARGET or the safe path locally
 3) ECS / Fargate entry criteria fixed: complete
   (1) Separate the execution settings for ECS
       - aws-paper-local is based on `127.0.0.1:15433` RDS port forwarding and local validation
       - aws-paper-ecs is based on the RDS private endpoint / Secrets Manager or SSM SecureString / ECS Task Role
       - on Fargate, `portfolio.batch.local-file-execution-enabled=false`
       - Fargate initial startup begins from query-only or the Step 1~11 safe trigger
   (2) Fargate deployment order
       - author the Dockerfile
       - ECR image push
       - register the ECS Task Definition
       - start the ECS Service
       - query-only smoke test
       - Step 1~11 `StartExecution` validation
       - approval-type Step 12~17 validation
   (3) Safe defaults
       - recommend Fargate initial `paperOrderEnabled=false`
       - start from `fullPipelineExecutionEnabled=false`
       - activate the order-related segment of Step 12 and beyond only after separate gate / preflight / approval confirmation
       - review limiting Snapshot Refresh to initially OFF or a separate trigger method
 4) Conclusion
   (1) ECS Fargate porting plan fixed
       - View is ported to an ECS Fargate Service
       - local-file batch execution is preserved as a local operator tool
       - the Fargate View is switched to a Step Functions `StartExecution`-based execution trigger
       - after local `aws-stepfunctions` mode validation, enter the Docker / ECR / ECS Task Definition stage
       - the `StepFunctionsDailyBatchExecutionService` addition and `aws-stepfunctions` mode implementation are first-complete
       - the next work is entering Docker / ECR / ECS Task Definition / Fargate query-only smoke test

7. ECS Fargate porting implementation
 1) Step Functions execution backend implementation: complete
   (1) `StepFunctionsDailyBatchExecutionService` addition: complete
       - converts the Daily Batch execution request into a Step Functions `StartExecution` call
       - separated from the `local-file` execution service
       - operates based on `execution-mode=aws-stepfunctions`
       - no direct execution of Python subprocess / `C:/Workspaces` local source
       - calls `StartExecution` using the AWS SDK v2 Step Functions client
       - `stateMachineArn` is injected via an environment variable, not fixed directly in `application.properties`
   (2) `StartExecution` payload composition: complete
       - includes `environment=paper`
       - includes `dbTarget=aws-paper`
       - distinguishes the requester via `requestedBy=VIEW_BUTTON`
       - includes `source=PORT_VIEW`
       - includes `requestedFrom=port-view`
       - `runDate` is included as an Asia/Seoul-based yyyy-MM-dd value
       - includes `fromStepCode` / `toStepCode`
       - includes `fromStepOrder` / `toStepOrder`
       - includes `startStep` / `endStep`
       - prioritizes the Step 1~11 safe trigger based on `allowPaperOrderExecute=false`
       - `accountNo` is included in the execution payload but kept from being exposed in raw form on the screen / logs / documents
   (3) `executionArn` handling: complete
       - receives the `executionName` / `executionArn` of the `StartExecution` response
       - the screen flash message shows the `executionArn` with the account-id redacted
       - the execution-request payload is composed inside the service
       - the full execution state is displayed based on a DB run / step log summary rather than direct exposure of the Step Functions history
   (4) Safety blocking logic: complete
       - blocks execution when `aws-stepfunctions` mode and the start-enabled gate are off
       - blocks execution when `stateMachineArn` is empty
       - blocks requests outside the `minExecutableStepOrder` / `maxExecutableStepOrder` range
       - blocks Step 12-and-above requests in the `allowPaperOrderExecute=false` state
       - separated so that approval requests are allowed only under the `paperOrderEnabled=true` condition
 2) `aws-stepfunctions` mode addition: complete
   (1) application setting additions: complete
       - added `portfolio.batch.aws-stepfunctions-region`
       - added `portfolio.batch.aws-stepfunctions-state-machine-arn`
       - added `portfolio.batch.aws-stepfunctions-execution-name-prefix`
       - `portfolio.batch.aws-stepfunctions-start-enabled` linked with the existing gate
       - `portfolio.batch.aws-stepfunctions-step-start-enabled` linked with the existing gate
       - can block local subprocess execution based on `portfolio.batch.local-file-execution-enabled=false`
       - can block the order-related segment based on `portfolio.batch.paper-order-enabled=false`
   (2) Screen gate reflection: complete
       - added the AWS Step 1~11 start button to the `/daily-batch` screen
       - the execution button is disabled in the `hasRunningBatch` state
       - the AWS Step 1~11 button is disabled in the `canStartAwsStepfunctions=false` state
       - `aws-stepfunctions` mode uses an execution path separate from the local-file backend
       - the Step 12~17 approval button is still separated as a later implementation target
   (3) Controller endpoint addition: complete
       - added the `/daily-batch/aws-stepfunctions/start-range` POST endpoint
       - receives the `fromStepCode` / `toStepCode` / `accountNo` request values
       - calls `StepFunctionsDailyBatchExecutionService` after confirming the `DailyBatchProperties` gate
       - on `StartExecution` success, displays the `executionName` and the redacted `executionArn` as a flash message
       - on failure, displays the cause message as a flash error and redirects to the `/daily-batch` screen
 3) Local Step 1~11 `StartExecution` validation: complete
   (1) Local execution conditions: complete
       - run the Local View based on Spring profile `aws-paper`
       - switch the backend based on `execution-mode=aws-stepfunctions`
       - block local subprocess execution based on `local-file-execution-enabled=false`
       - block the order-related segment based on `paper-order-enabled=false`
       - retain the AWS Paper RDS tunnel and View DB query flow
       - the Step Functions call privilege is validated based on the local AWS credential
   (2) Validation items: complete
       - confirmed the AWS Step 1~11 execution button on the `/daily-batch` screen
       - confirmed Step Functions `StartExecution` success via the View button click
       - confirmed `executionName` is returned
       - confirmed the redacted `executionArn` is returned
       - confirmed execution based on `allowPaperOrderExecute=false`
       - confirmed the Step 1~11 workflow execution
       - confirmed `APPROVAL_REQUIRED` Slack reception before the Step 12~17 approval gate
       - confirmed the safe trigger path without new broker order submission
   (3) `runDate` omission correction: complete
       - in the first validation, `States.Runtime` occurred in the `StopCrawlerEc2AfterStep11Success` state after Step 1~11 completion
       - the cause was `runDate` missing from the View `StartExecution` input, versus the ASL Payload's `runDate.$=$.runDate` reference
       - added an Asia/Seoul-based `runDate` to the input JSON in `StepFunctionsDailyBatchExecutionService`
       - re-validation result: passed from `StopCrawlerEc2AfterStep11Success` through `SendApprovalRequiredSlack`
       - local `aws-stepfunctions` end-to-end validation complete via Slack `APPROVAL_REQUIRED` reception
   (4) Implementation commit: complete
       - commit `e72de6f`
       - message `feat(view): add Step Functions daily batch trigger`
       - changed files are `pom.xml`, `DailyBatchProperties.java`, `DailyBatchController.java`, `application-aws-paper.properties`, `daily_batch.html`, `StepFunctionsDailyBatchExecutionService.java`
       - git working tree cleanup complete
       - 0 plaintext quotations of the commit diff body / Class internal code / `application-aws-paper.properties` body in this note (R-DOCS-001 alignment)
 4) Step 12~17 approval-type validation: complete
   (1) Pre-execution preflight: complete
       - REQUESTED / READY `strategy_execution_order` confirmation complete
       - retryable rejected `connector_order_request` confirmation complete
       - active `connector_order_request` confirmation complete
       - confirmed no strategy-linked active `connector_order_request`
       - the existing stale `connector_order_request` ACCEPTED order is separated as a cleanup target as the 2026-04-27 삼성전자 unmapped order
       - confirmed no new order subject to Step 12~17 validation
   (2) Approval-execute button / endpoint implementation: complete
       - added the AWS Step 12~17 approval-execute button (`daily_batch.html` / separated from the safe button / safe · approval activation conditions separated)
       - added the `POST /daily-batch/aws-stepfunctions/start-approval-range` Controller endpoint
       - confirmed `requestedBy=VIEW_APPROVAL_BUTTON` payload creation
       - confirmed `allowPaperOrderExecute=true` payload transfer
       - confirmed `paperOrderEnabled=true` payload transfer
       - confirmed `executionName` / redacted `executionArn` screen display
   (3) Step 12~17 dedicated state machine ARN separation: complete
       - general workflow ARN: `portfolio-paper-daily-step1-17-approval`
       - approval workflow ARN: `portfolio-paper-daily-step12-17-approval`
       - uses the existing `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_STATE_MACHINE_ARN`
       - added the new environment variable `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_APPROVAL_STATE_MACHINE_ARN`
       - added the `application.properties` key: `portfolio.batch.aws-stepfunctions-approval-state-machine-arn`
       - added the `awsStepfunctionsApprovalStateMachineArn` field + getter / setter to `DailyBatchProperties`
       - `startSafeRange` of `StepFunctionsDailyBatchExecutionService` uses the general state machine ARN
       - `startApprovalRange` of `StepFunctionsDailyBatchExecutionService` uses the approval-dedicated state machine ARN
       - blocks approval-type execution when the approval ARN is empty (service-level safety gate)
       - 0 plaintext records of the account-id part of the actual state machine ARN in this note (`[REDACTED]` or placeholder)
   (4) payload type correction: complete
       - the first Step 12~17 approval execution entered the dedicated state machine but was blocked at `Step12_CheckApproval`
       - the cause was that `allowPaperOrderExecute` and `paperOrderEnabled` were passed as the string `"true"`
       - corrected to boolean `true` to match the Step Functions Choice `BooleanEquals` condition
       - also corrected `fromStepOrder` / `toStepOrder` / `startStep` / `endStep` to numeric values
       - confirmed `Step12_CheckApproval` pass on re-validation
       - 0 plaintext quotations of the commit diff / Java body / JSON payload body in this note (R-DOCS-001 alignment)
   (5) AWS Step Functions execution validation: complete
       - executionName: `port-view-step12-17-step12-17-20260629-194314-ba5edaf8`
       - state machine: `portfolio-paper-daily-step12-17-approval`
       - status: `SUCCEEDED`
       - start: `2026-06-29T19:43:15.673+09:00`
       - stop: `2026-06-29T19:46:06.546+09:00`
       - `Step12_CheckApproval` passed
       - `Step12_RunMarketConnectorStrategyOrderExecute` executed
       - `Step12_GetCommandInvocation` success
       - Step 13~17 all proceeded
       - `ExecutionSucceeded` confirmed
   (6) DB safety after-check: complete
       - operational marker: `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS`
       - 0 new `connector_order_request` today
       - 0 READY / REQUESTED `strategy_execution_order`
       - no new broker order submission
       - the recent `connector_order_request` shows only existing orders from 2026-06-22 ~ 2026-06-24
 5) Local View startup wrapper organization: complete
   (1) Execution method separation: complete
       - separated the Local-file startup wrapper and the AWS Step Functions startup wrapper
       - both wrappers start the Local View with the `aws-paper` profile
       - both wrappers configure the gate so the full Step 1~17 can be executed
       - organized into an operator-selectable full-execution wrapper rather than a safe-only validation wrapper
   (2) Local-file startup command: complete
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
       - env loader = `Load-PortfolioViewAwsPaperLocalFileEnv.ps1` (operator local tool folder / out of scope for this spec)
   (3) AWS Step Functions startup command: complete
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
       - `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_STATE_MACHINE_ARN` uses the `portfolio-paper-daily-step1-17-approval` looked-up value
       - `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_APPROVAL_STATE_MACHINE_ARN` uses the `portfolio-paper-daily-step12-17-approval` looked-up value
       - env loader = `Load-PortfolioViewAwsPaperStepFunctionsEnv.ps1` (operator local tool folder / out of scope for this spec)
   (4) wrapper execution validation: complete
       - `Unblock-File` application complete
       - confirmed executable via the `ExecutionPolicy Bypass` method
       - confirmed `VIEW_AWS_PAPER_STEPFUNCTIONS_ENV_READY` output
       - confirmed the AWS Step Functions ARN set (both general + approval set / 0 plaintext records of the actual ARN in this note)
       - confirmed Spring profile `aws-paper`
       - confirmed DB connection `jdbc:postgresql://127.0.0.1:15433/portfolio`
       - confirmed View DB user `view_app`
       - confirmed Tomcat 8080 startup
       - confirmed `PortViewApplication started`
 6) Docker / ECR / ECS Task Definition: incomplete
   (1) Author the Dockerfile
       - create a Spring Boot jar-based port-view image
       - remove the local `C:/Workspaces` path dependency
       - prohibit including secret / password / token in the image
   (2) ECR push
       - image push to the `portfolio-view` repository or per the existing naming convention
       - manage the tag based on the paper date or `paper-latest`
   (3) Register the ECS Task Definition
       - `SPRING_PROFILES_ACTIVE=aws-paper,aws-paper-ecs`
       - inject RDS connection info via Secrets Manager or SSM SecureString
       - grant the ECS Task Role minimal `states:StartExecution` privilege (limiting to a specific state machine ARN recommended / both general + approval limited by Resource / R-AUTO-034 new mitigation alignment / 06 spec follow-up phase responsibility)
       - connect CloudWatch Logs
       - `local-file-execution-enabled=false`
 7) Fargate validation: incomplete
   (1) query-only startup
       - start the ECS Service
       - query `/dashboard`
       - query `/balance-summary`
       - query `/positions`
       - query `/orders`
       - query `/strategy`
       - query `/daily-batch`
   (2) Step 1~11 `StartExecution`
       - call Step 1~11 `StartExecution` from the Fargate View
       - confirm `executionArn` is returned
       - confirm `allowPaperOrderExecute=false`
       - confirm Step 12~17 blocking
       - confirm DB run / step log query
   (3) approval-type Step 12~17
       - activate the approval button after preflight
       - confirm explicit `allowPaperOrderExecute=true` (boolean) + `paperOrderEnabled=true` (boolean) payload
       - confirm use of the approval state machine ARN (`portfolio-paper-daily-step12-17-approval`)
       - confirm the Step 12~17 execution result
       - after-check whether a new order is created / submitted to the broker
       - reflect into the operational runbook only when there is no problem

### Conclusion

- On the Local View basis, an operator wrapper capable of full Step 1~17 execution was organized for both the `local-file` backend and the `aws-stepfunctions` backend.
- The AWS Step Functions backend completed validation locally for both the Step 1~11 safe trigger and the Step 12~17 approval trigger.
- The Step 12~17 approval trigger completed as `SUCCEEDED` on the `portfolio-paper-daily-step12-17-approval` state machine, and per the DB after-check no new order was created.
- Therefore the Local View-based Step Functions integration pre-validation is complete, and the next stage is Docker / ECR / ECS Task Definition / Fargate query-only smoke test.

### Decision / risk mapping

- OD-MS-002 (port-view compute = ECS Fargate Service first priority) alignment — no change to the decision body on this date / first-validation memo reinforced.
- OD-MS-009 (Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) alignment — the Batch execution responsibility is transferred to Step Functions `StartExecution` rather than a View-internal subprocess / the automatic trigger remains limited to the existing EventBridge Scheduler.
- OD-MS-037 (View Local AWS Paper read-only first scope + policy of preceding batch second validation before ECS / Fargate entry) alignment — the local `aws-stepfunctions` mode Step 1~11 trigger pre-validation before Fargate entry is complete.
- OD-SAFE-001 ~ OD-SAFE-004 (staged introduction of automatic BUY · SELL E2E / no-automatic-retry limited to idempotent) alignment — first validation of the service-level safety gate of `StepFunctionsDailyBatchExecutionService`.
- R-AUTO-033 [2026-06-29 reinforcement (2)] — port-view Step Functions trigger separation + Step 1~11 validation pass / Status `Mitigated` retained.
- R-AUTO-034 new — Fargate View `states:StartExecution` excessive-privilege grant + Step 12 gate bypass risk / Status `Open` / mitigation of Task Role limited to a specific state machine ARN + Fargate safe defaults + service-level safety gate + executionArn redaction + Step 12~17 approval-type / preflight / paper-order gate separation.

### This date's factual recording scope

- This date's Kiro work = new creation of 05 spec `operation-notes.md` + first authoring of these sections 6 · 7.
- The code changes of the operator's direct commit `e72de6f` (`feat(view): add Step Functions daily batch trigger`) are in the port-view MS area, so 0 changes from this date's cross-service AWS Migration spec work (spec area).
- 0 changes on this date to AWS CLI / boto3 / psql / Spring Boot execution / external API calls.
- 0 changes on this date to AWS resource creation · modification · deletion.
- 0 new changes on this date to broker / KIS calls (the validation time was limited to the operator's direct local execution / 0 new broker order submissions / Step 12~17 approval gate blocking retained / 0 aws-live actions).
- 0 plaintext records in this note of sensitive information (secret value / KIS app key / KIS app secret / account number / account password / token / RDS password / RDS endpoint hostname / raw 12-digit account-id / actual IAM Role ARN / actual secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN / raw broker_order_no / Slack webhook URL / DB password / Administrator password / actual state machine ARN).
- Operational identifiers (recorded as facts per the user-specified policy / not secrets):
  - port-view commit hash `e72de6f` / commit message `feat(view): add Step Functions daily batch trigger`.
  - Class name `StepFunctionsDailyBatchExecutionService` / Controller endpoint `/daily-batch/aws-stepfunctions/start-range`.
  - Spring properties key labels / StartExecution payload field labels + `runDate` (Asia/Seoul yyyy-MM-dd).
  - Step Functions state name = `StopCrawlerEc2AfterStep11Success` · `SendApprovalRequiredSlack` · `Step6ToStep11_Succeeded` · `Step12_CheckApproval`.
  - Slack event label `APPROVAL_REQUIRED` / error label `States.Runtime` / Spring profile `aws-paper`.


## 2026-06-30 — View operational path 4-type organization complete + Daily Batch gate operational-intent alignment fix + DB validation query authoring principle addition

### Summary

- Operator direct action = `DailyBatchController.java` Daily Batch gate operational-intent alignment fix.
- Local View → AWS Step Functions Step 12~17 approval execution second validation passed (from the 05 spec ECS Fargate porting perspective).
- 0 plaintext quotations (R-DOCS-001 alignment) — port-view code body / IAM Policy / ASL / response body / `StartExecution` input JSON body / DB after-check raw output full text.
- For Step Functions-side detailed facts, see the 2026-06-30 section of [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md).

8. View operational path 4-type organization: complete
 1) Local View-side operator manual trigger separation, 4 types: complete
   (1) Local View → Local File Step 1 standalone execution: complete (2026-06-28 Run #46)
   (2) Local View → Local File Step 1~11 execution: complete (2026-06-28 Run #47)
   (3) Local View → Local File Step 12~17 execution: complete (2026-06-29 (1) Run #48)
   (4) Local View → AWS Step Functions Step 12~17 approval execution: complete (this date)
       - executionName `port-view-daily-step12-17-20260630-095111-aae2595c`
       - state machine `portfolio-paper-daily-step12-17-approval`
       - status `SUCCEEDED`
       - start `2026-06-30T09:51:11.903+09:00`
       - stop `2026-06-30T09:54:16.484+09:00`
       - safe termination in the no-order-target state
       - DB after-check passed (0 new `connector_order_request` / 0 new broker orders)
 2) Operational path separation alignment: complete
   (1) Local File execution and AWS Step Functions execution separated-operation alignment
       - Local File execution gate: `localFileExecutionEnabled`
       - AWS Step Functions execution gate: `awsStepfunctionsStartEnabled` / `awsStepfunctionsStepStartEnabled`
       - approval range gate: `paperOrderEnabled=true` + approval ARN set
   (2) Step 12~17 order-related segment executes only when the separate approval-type state machine + paper-order gate are passed, aligned

9. Daily Batch gate operational-intent alignment fix: complete
 1) `DailyBatchController.java` fix: complete
   (1) AWS Step Functions button activation condition fix
       - the AWS Step 12~17 approval button is active even in the `fullPipelineExecutionEnabled=true` state
       - avoid conflict with the AWS Step 1~11 button condition even in the `paperOrderEnabled=true` state
       - retain the separation of the Local File execution gate and the AWS Step Functions execution gate
       - Step 12~17 executes only when `paperOrderEnabled=true` + the approval range gate are passed
       - 0 plaintext quotations of the Java body / commit diff in this note (R-DOCS-001 alignment)
   (2) Validation
       - mvn compile success
       - Local View `aws-paper` profile restart success
       - screen display passed (Execution ON / Local File OFF / Full Pipeline ON / Paper Order ON / allowed range `1~17` / AWS Step 12~17 approval-execute button active)

10. DB validation query authoring principle addition: incomplete (formal reflection is a follow-up phase responsibility)
 1) Operational principles identified on this date: complete (factual record)
   (1) Mandatory prior column-name confirmation
       - SELECT after confirming the target columns in advance with `information_schema.columns`
       - identified the `connector_position_snapshot.balance_snapshot_id` column-absence case on this date
   (2) SELECT only confirmed columns
       - prohibit expected use of a relation column (e.g. `balance_snapshot_id`) as well
       - when absent, validate with a natural key such as `account_no` + `as_of_date`
   (3) Result-exposure pattern
       - prohibit hiding results with `DO` / `EXECUTE` in after-check queries
       - author so the final SELECT result appears directly on the screen
   (4) Windows / PowerShell / psql environment alignment
       - for Korean SQL, retain the UTF-8 No BOM `.sql` file + `psql -f` pattern instead of direct `psql -c` execution
       - for SSM multiline command, retain the UTF-8 No BOM JSON file + `--parameters file://...` pattern
 2) Formal reflection follow-up (05 spec follow-up phase responsibility)
   (1) formally reflect these principles when newly creating `validation-checklist.md` or an equivalent document
   (2) register as a cross-spec audit item at the Fargate cutover time
   (3) align these principles when organizing the DB after-check query collection

11. Daily Batch screen wording organization: incomplete (follow-up phase responsibility)
 1) Organize so that `aws-stepfunctions` mode is also clearly visible in the `local-file`-centered wording: incomplete
   (1) Follow-up work
       - organize the current-mode display / button labels / execution-history labels, etc. of `daily_batch.html`
       - decide, at Fargate entry, whether to exclude the `local-file` button or separate a dedicated operator path
       - 0 code changes on this date / a separate commit at the time of an additional change
   (2) Decision mapping
       - OD-MS-002 / OD-MS-037 alignment
       - R-AUTO-034 mitigation extension coupling

### Decision / risk mapping (2026-06-30)

- OD-MS-002 / OD-MS-009 / OD-MS-037 / OD-SAFE-001 ~ OD-SAFE-004 first-validation memo reinforced without changing the bodies (the 2026-06-29 (3) Change Log entry alignment retained as is / no new decision on this date / no change to the Decision Summary count).
- R-AUTO-033 [2026-06-30 reinforcement] — Daily Batch gate operational-intent alignment + AWS Step Functions Step 12~17 approval execution second validation / Status `Mitigated` retained.
- R-AUTO-034 [2026-06-30 reinforcement] — first validation of the View-side 4 operational path separation + DB validation query authoring principle reinforcement / Status `Open` retained / Fargate Task Role privilege separation remains a 06 spec follow-up phase responsibility.

### This date's factual recording scope (2026-06-30)

- This date's Kiro work = accumulation of these sections in 05 spec `operation-notes.md` (items 8 · 9 · 10 · 11) + append to 04 spec `operation-notes.md` (Step Functions external-caller-side facts) + 2 `_common` files (`followups-overview.md` 2026-06-30 follow-up memo / `risk-register.md` R-AUTO-033 + R-AUTO-034 reinforcement) + 2 `.kiro` root files (prepend of the `WORKLOG.md` / `CHANGELOG.md` 2026-06-30 sections).
- The operator's direct change (`DailyBatchController.java` Daily Batch gate fix) is in the port-view MS area, so 0 changes from this date's cross-service AWS Migration spec work (spec area).
- 0 changes on this date to AWS CLI / boto3 / psql / Spring Boot execution / external API calls / 0 AWS resource creation · modification · deletion / 0 commit/add/reset/checkout/stash.
- broker / KIS calls = morning Step 1~11 automatic trigger only (0 new `connector_order_request`) + 1 Local View → AWS Step Functions Step 12~17 approval execution (`SUCCEEDED` / NO_TARGET / 0 broker order submissions) + balance refresh only / 0 additional BUY · SELL · cancel · modify / 0 fill · position sync automatic retries / 0 aws-live actions.
- 0 plaintext records in this note of sensitive information (secret value / KIS app key / KIS app secret / account number / account password / token / RDS password / RDS endpoint hostname / raw 12-digit account-id / actual IAM Role ARN / actual secret ARN / IAM access key id / instance-id / EIP / image digest full sha256 / task ARN / job ARN / raw broker_order_no / Slack webhook URL / DB password / Administrator password / actual state machine ARN) — all `[REDACTED]` or placeholder.
- Operational identifiers (recorded as facts per the user-specified policy / not secrets):
  - executionName `port-view-daily-step12-17-20260630-095111-aae2595c` / state machine `portfolio-paper-daily-step12-17-approval`.
  - Controller class `DailyBatchController` / 6 Daily Batch gate labels / screen display labels.
  - balance snapshot `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505` · `eval_profit=0` · `source_version=connector-intraday-snapshot-refresh-1.0.0`.
  - DB column names `balance_snapshot_id` (absent) · `account_no` · `as_of_date`.
  - Run id `#46` · `#47` · `#48` / Spring profile `aws-paper` / start · stop timestamp.


3. ECS Fargate porting: complete
 1) First approach
   (1) ALB-free Public IP direct access
       - place the ECS Fargate task in a public subnet
       - assign public IP enabled setting
       - do not create an ALB
       - do not create a NAT Gateway
       - access View via the Fargate task public IP and port 8080
       - the access URL format is `http://<FARGATE_TASK_PUBLIC_IP>:8080`
       - operate on the premise that the public IP may change on task restart or redeployment
   (2) Security principles
       - Security Group inbound allows only TCP 8080
       - source allows only the operator public IP/32
       - during initial validation, full 0.0.0.0/0 opening is prohibited
       - mobile access is confirmed via the same Wi-Fi or by adding a temporary mobile public IP/32
       - remove unnecessary inbound rules after confirmation is complete
   (3) Cost-saving principles
       - remove ALB cost
       - remove NAT Gateway cost
       - bear only the public IPv4 cost and Fargate execution-time cost
       - the ECS service desired count is 1 only when needed
       - operate so the desired count can be switched to 0 after confirmation is complete
       - set the CloudWatch Logs retention short (7 days)
 2) First goal
   (1) read-only View deployment
       - deploy the AWS Paper View validated on the Local View to ECS Fargate
       - the initial deployment scope prioritizes the read-only View
       - confirm the Dashboard / Balance / Positions / Orders / Reports / Daily screens
       - minimize Spring Boot property structure changes
       - transfer the environment variables that were injected by the PowerShell startup script to ECS Task Definition environment variables
   (2) Step Functions button safe gate confirmation
       - confirm whether the AWS Step 1~11 button is displayed
       - confirm whether the AWS Step 12~17 approval button is displayed
       - confirm no `fullPipelineExecutionEnabled` / `paperOrderEnabled` condition conflict
       - actual Step Functions execution proceeds in a separate approval stage after read-only screen validation
   (3) Step Functions manual execution validation from the ECS View
       - click the AWS Step 12~17 approval-execute button on the ECS View Daily screen
       - call AWS Step Functions `StartExecution` via the ECS task role
       - execute the `portfolio-paper-daily-step12-17-approval` state machine
       - receive Slack `DAILY_EXECUTION_SUCCESS` after execution completion
       - confirm the DB after-check is normal
 3) Completion criteria
   (1) ECS deployment completion criteria
       - Docker image build success
       - ECR image push complete
       - ECS task definition creation complete
       - ECS service desired count 1 startup success
       - ECS task RUNNING confirmed
       - Spring Boot started confirmed in CloudWatch Logs
   (2) Network completion criteria
       - Fargate task public IP confirmed
       - `http://<FARGATE_TASK_PUBLIC_IP>:8080` access success
       - Security Group inbound TCP 8080 source operator IP/32 confirmed
       - Private RDS connection success from the ECS task
       - RDS access configuration without a NAT Gateway confirmed
   (3) View completion criteria
       - Dashboard query success
       - Balance query success
       - Positions query success
       - Orders query success
       - Reports query success
       - Daily query success
       - the default account number is reflected normally on first access and menu navigation
   (4) safe gate completion criteria
       - AWS Step 1~11 button condition confirmed
       - AWS Step 12~17 approval button condition confirmed
       - Step 12~17 approval button display confirmed even in the `fullPipelineExecutionEnabled=true` state
       - no Step 1~11 button condition conflict confirmed even in the `paperOrderEnabled=true` state
       - the local-file execution button confirmed disabled in the ECS environment
       - the full-execution button confirmed in the locked state
   (5) Step Functions execution completion criteria
       - AWS Step 12~17 approval-execute button click success from the ECS View
       - execution `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` creation confirmed
       - state machine `portfolio-paper-daily-step12-17-approval` execution confirmed
       - execution status `SUCCEEDED` confirmed
       - Slack `DAILY_EXECUTION_SUCCESS` reception confirmed
       - after-check result REQUESTED strategy order 0 confirmed
       - after-check result retryable rejected strategy order 0 confirmed
       - after-check result active connector order 0 confirmed
       - after-check result today's new connector order 0 rows confirmed
       - latest connector balance snapshot reference date 2026-06-30 confirmed
 4) Completion result
   (1) AWS resources
       - ECS cluster: `portfolio-paper-cluster`
       - ECS service: `portfolio-view-service`
       - ECS task definition: `portfolio-view:2`
       - ECR repository: `portfolio-view`
       - CloudWatch Logs group: `/ecs/portfolio-view`
       - task execution role: `portfolio-paper-ecs-task-execution-role` (0 plaintext records of the actual ARN / `[REDACTED_ARN]`)
       - task role: `portfolio-paper-view-task-role` (0 plaintext records of the actual ARN / `[REDACTED_ARN]`)
       - security group: `sgroup-port-view-ecs`
   (2) ECS task definition main settings
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
   (3) Validated access result
       - Fargate public IP direct access success (0 plaintext records of the public IP / `[REDACTED_PUBLIC_IP]`)
       - Spring Boot started confirmed
       - HikariPool RDS connection success
       - default schema `ops` confirmed
       - Dashboard / Balance / Positions / Orders / Reports / Daily screens display normally
       - the default account-number omission issue was corrected by adding env in task definition revision 2 (`PORTFOLIO_BATCH_DEFAULT_ACCOUNT_NO` + `PORTFOLIO_VIEW_ACCOUNT_DEFAULT_ACCOUNT_NO`)
   (4) Validated execution result
       - AWS Step 12~17 approval execution success on the ECS View Daily screen
       - execution `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8`
       - start: 2026-06-30 14:15:42 KST
       - stop: 2026-06-30 14:18:48 KST
       - status: SUCCEEDED
       - history final `ExecutionSucceeded`
       - Slack `DAILY_EXECUTION_SUCCESS` reception
       - after-check normal
   (5) after-check result
       - REQUESTED strategy orders after: 0
       - retryable rejected strategy orders after: 0
       - active connector orders after: 0
       - today connector orders after: 0 rows
       - latest connector_balance_snapshot id: 281
       - latest connector_balance_snapshot as_of_date: 2026-06-30
       - total_eval_amount: 8,706,505
       - cash_balance: 8,706,505
       - identified 6 past stale connector orders — all are 2026-04-27 ACCEPTED remainders of a past test account, not the operational account / unrelated to this Step 12~17 execution / separated as a follow-up cleanup candidate
   (6) Cost-saving termination result
       - after validation completion, ECS service desired count 0 transition complete
       - Fargate task termination complete
       - operate on the premise of public IP release
       - at next startup, a new public IP must be confirmed after the desired count 1 transition
 5) On-hold items
   (1) On hold until after first porting
       - ALB creation
       - formal HTTPS configuration
       - Route53 domain connection
       - Cloudflare Tunnel
       - authentication / authorization enhancement
       - Slack wording improvement
       - property structure reorganization
       - new application-ecs.yml separation
       - ECS Auto Scaling
       - Blue/Green deployment
   (2) Follow-up cleanup candidates
       - modify the "local execution validation" expression on the Daily screen to match the ECS / AWS mode
       - review whether to handle the 6 past ACCEPTED stale `connector_order_request`
       - document the ECS service desired count 0/1 operation commands (runbook.md follow-up)
       - document the Security Group inbound update procedure when the operator IP changes
       - separately validate AWS Step 1~11 ECS View execution if needed
 6) Conclusion
   (1) First porting direction
       - proceed with the ALB-free ECS Fargate Public IP direct access method
       - View ECS deployment complete at minimal cost
       - minimize the external exposure scope via operator IP restriction
       - completed via the ECS Task Definition env transfer method without Spring Boot property structure changes
       - later, if needed, ALB / HTTPS / Cloudflare Tunnel are reviewed separately
   (2) Completion judgment
       - ECS Fargate View access success
       - read-only screen normal query
       - Private RDS connection normal
       - default account number reflected normally
       - Step Functions button condition normal
       - AWS Step 12~17 approval execution success from the ECS View
       - Slack success notification received
       - DB after-check normal
       - desired count 0 termination complete
       - **3. ECS Fargate porting: complete**

### Operation procedure (runbook follow-up responsibility / this note is a factual record)

Operator command examples follow the `list/describe → extract variable → subsequent verification` pattern without manual substitution of ARN / task ARN / ENI ID / LOG_STREAM. This note contains only factual records, and the formal runbook is a 05 spec follow-up phase responsibility.

ECS View start:
 1) ECS service desired count 1 transition
   (1) `aws ecs update-service --cluster portfolio-paper-cluster --service portfolio-view-service --desired-count 1`
   (2) wait for `RUNNING` entry with `aws ecs wait services-stable`
 2) public IP auto-query
   (1) `aws ecs list-tasks --cluster portfolio-paper-cluster --service-name portfolio-view-service` → extract `TASK_ARN`
   (2) `aws ecs describe-tasks --cluster portfolio-paper-cluster --tasks $TASK_ARN` → extract `ENI_ID` (`attachments[].details[?name=='networkInterfaceId'].value`)
   (3) `aws ec2 describe-network-interfaces --network-interface-ids $ENI_ID` → extract `PUBLIC_IP` (`Association.PublicIp`)
   (4) output the browser access URL: `http://$PUBLIC_IP:8080` (0 plaintext records in this note / `[REDACTED_PUBLIC_IP]`)

ECS View stop:
 1) ECS service desired count 0 transition
   (1) `aws ecs update-service --cluster portfolio-paper-cluster --service portfolio-view-service --desired-count 0`
   (2) Fargate task termination / public IP release
 2) Caution on next start
   (1) a new public IP is issued on the desired count 0 → 1 transition
   (2) the operator re-queries the new public IP and updates the browser URL
   (3) the operator IP/32 of the Security Group inbound rule is retained as is

### Validation checklist (validation-checklist follow-up responsibility / this note is a factual record)

Items that passed validation on this date:
 1) Docker image build: complete
 2) ECR push: complete
 3) task definition registration: complete (revision 1 → 2 correction)
 4) ECS service `portfolio-view-service` RUNNING: complete
 5) Spring Boot started in CloudWatch Logs `/ecs/portfolio-view`: complete
 6) RDS connection success (HikariPool start completed): complete
 7) Dashboard / Balance / Positions / Orders / Reports / Daily screen query: complete
 8) AWS Step 1~11 button display: complete
 9) AWS Step 12~17 approval button display: complete
 10) ECS View → Step 12~17 execution `SUCCEEDED`: complete (executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8`)
 11) Slack `DAILY_EXECUTION_SUCCESS` reception: complete
 12) DB after-check 0 confirmation: complete (REQUESTED / retryable rejected / active / today connector orders all 0)
 13) desired count 0 termination: complete

### Decision / risk mapping (2026-06-30 afternoon)

- OD-MS-002 (port-view compute = ECS Fargate Service first priority) alignment — validation complete on this date (ALB-free Public IP direct access + operator IP/32 SG inbound + desiredCount 0/1 manual operation / evidence reinforced without changing the decision body).
- OD-MS-009 (Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) alignment — the third phase where the ECS View attaches as a Step Functions `StartExecution` external caller, first validated (Local View → AWS Step Functions Step 1~11 / Local View → AWS Step Functions Step 12~17 approval / ECS View → AWS Step Functions Step 12~17 approval).
- OD-MS-037 (View Local AWS Paper read-only first scope + policy of preceding batch second validation before ECS / Fargate entry) alignment — after local validation (2026-06-27 read-only / 2026-06-28 Step 1 + Step 1~11 / 2026-06-29 (1) Step 12~17 local-file / 2026-06-29 (2) Step 1~11 aws-stepfunctions / 2026-06-29 (3) + 2026-06-30 morning Step 12~17 approval), this date's afternoon ECS Fargate entry + Step 12~17 ECS View approval execution complete.
- R-AUTO-033 [2026-06-30 afternoon reinforcement] — ECS Fargate Public IP direct access + operator IP/32 SG inbound + ECS View → AWS Step Functions Step 12~17 approval third validation / Status `Mitigated` retained.
- R-AUTO-034 [2026-06-30 afternoon reinforcement] — the Fargate ECS task role (`portfolio-paper-view-task-role`) `states:StartExecution` privilege passed first validation in the actual Fargate environment / fact of the grant limited to the Step 12~17 approval state machine ARN / Status `Open` retained (cross-spec audit when ALB · HTTPS · CloudWatch alarms are introduced in the future / 06 spec follow-up phase responsibility).
- New follow-up risk reinforcement (see followups-overview 2026-06-30 afternoon follow-up memo + risk-register):
       - risk of an SG inbound opening mistake on Public IP direct access (operator IP/32-limited policy alignment)
       - risk of unnecessary Fargate / public IPv4 cost accumulation from keeping desiredCount 1 (desiredCount 0 termination operation policy alignment)
       - risk of the access URL changing due to the public IP change after task restart (operator re-query at each startup policy)
       - risk of misdiagnosis from using an assumed column name in AWS CLI / psql validation queries (`information_schema.columns` prior-confirmation policy alignment / combining the 2026-06-29 (1) DB password exposure + 2026-06-30 morning `connector_position_snapshot.balance_snapshot_id` absence cases)
       - risk of past stale `connector_order_request` contaminating the preflight count (the 6 identified on 2026-06-30 afternoon / follow-up cleanup decision)

### This date's factual recording scope (2026-06-30 afternoon)

- This date's Kiro work scope:
  - accumulation of this section (3. ECS Fargate porting: complete) in 05 spec `operation-notes.md`.
  - update of 5 `_common` files (`followups-overview` · `operator-decisions` · `ms-aws-service-decision-matrix` · `cost-simulation` · `risk-register` · `aws-resource-glossary`).
  - 04 spec `operation-notes.md` append + 06 spec `operation-notes.md` append.
  - 3 `.kiro` root files (`WORKLOG.md` / `CHANGELOG.md` / `README.md` short status reinforcement) + `.kiro/AGENTS.md` operation command authoring rule reinforcement.
- Operator direct-performed work (port-view MS and AWS operator area / 0 changes to this date's cross-service AWS Migration spec area):
  - Dockerfile / `.dockerignore` addition + Docker image build + ECR push.
  - ECS Task Definition registration + ECS Service creation.
  - Security Group · CloudWatch Logs · IAM Role configuration.
  - ECS View access + AWS Step 12~17 approval-execute click + desiredCount 0 termination.
- 0 Kiro-side execution / changes — 0 AWS CLI · boto3 · psql · Spring Boot · external API execution / 0 AWS resource creation · modification · deletion / 0 commit · add · reset · checkout · stash.
- broker / KIS call scope:
  - morning Step 1~11 automatic trigger only.
  - Local View → Step 12~17 approval execution (2026-06-30 morning).
  - ECS View → Step 12~17 approval execution (2026-06-30 afternoon / executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / `SUCCEEDED` / NO_TARGET / 0 broker order submissions).
  - balance refresh only / 0 additional BUY · SELL · cancel · modify / 0 fill · position sync automatic retries / 0 aws-live actions.
- 0 plaintext records of sensitive information — all `[REDACTED]` / `[REDACTED_ACCOUNT_NO]` / `[REDACTED_PUBLIC_IP]` / `[REDACTED_ARN]` / `[REDACTED_TASK_ARN]` / `[REDACTED_SECRET_ARN]` / `[REDACTED_BROKER_ORDER_NO]` placeholders:
  - secret value / KIS app key · KIS app secret / 12-digit account number · account password / token.
  - RDS password / RDS endpoint hostname / raw 12-digit account-id.
  - actual IAM Role · secret · state machine ARN / IAM access key id / instance-id / EIP / public IP.
  - image digest full sha256 / task ARN / ENI ID / job ARN / raw broker_order_no.
  - Slack webhook URL / DB password / Administrator password.
- Operational identifiers (recorded as facts per the user-specified policy / not secrets):
  - AWS resources:
    - ECS cluster `portfolio-paper-cluster` / ECS service `portfolio-view-service` / task definition `portfolio-view:2`.
    - ECR repository `portfolio-view` / CloudWatch Logs group `/ecs/portfolio-view` / Security Group `sgroup-port-view-ecs`.
    - task execution role `portfolio-paper-ecs-task-execution-role` / task role `portfolio-paper-view-task-role`.
  - Step Functions execution:
    - executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / state machine `portfolio-paper-daily-step12-17-approval` / status `SUCCEEDED`.
    - start · stop timestamp `2026-06-30T14:15:42.899+09:00` ~ `2026-06-30T14:18:48.358+09:00`.
    - Slack event label `DAILY_EXECUTION_SUCCESS`.
  - Runtime settings:
    - Spring profile `aws-paper` / Tomcat port `8080` / launch type `FARGATE` / network mode `awsvpc` / cpu 512 / memory 1024 / container port 8080 / Spring properties env label.
  - View screen and DB state:
    - 6 screen labels (Dashboard / Balance / Positions / Orders / Reports / Daily).
    - balance snapshot `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505`.
    - fact of identifying 6 stale `connector_order_request` / fact of the `connector_position_snapshot.balance_snapshot_id` column absence.


## 2026-06-30 (afternoon) — Daily Brief Slack automation independent-operation cross-reference

### Summary

- Separate from the same date's afternoon port-view ECS Fargate first-porting + ECS View → Step 12~17 approval execution (the preceding "3. ECS Fargate porting: complete" block).
- A cross-reference of the fact that Daily Brief Slack automation was configured as additional operator direct work on this date's afternoon.
- 0 plaintext quotations (R-DOCS-001 alignment) — Lambda code / Step Functions ASL / Scheduler target JSON / Slack message / Builder output / Notifier input / CloudWatch Logs / IAM Policy body.

12. Daily Brief Slack automation cross-reference: factual record
 1) The Daily Brief notification operates independently of the main Daily execution / ECS View / MarketConnector EC2: complete
   (1) Separated from the main Daily execution state machine
       - the Daily Brief notification is the responsibility of the mini Step Functions `portfolio-daily-brief-slack-notification`, separate from `portfolio-paper-daily-step1-17-approval` / `portfolio-paper-daily-step12-17-approval`
       - isolated so that a main Daily execution failure does not affect Daily Brief Slack delivery, and a Daily Brief Slack failure does not affect the main Daily execution
       - OD-MS-038 new alignment
   (2) Operates independently of MarketConnector EC2 start · stop
       - the MarketConnector EC2's 07:50 KST start / 15:50 KST stop (OD-MS-034 alignment) and the Daily Brief Slack's 07:50 KST pre-market delivery / 15:50 KST post-market delivery share only the time window / Target / Lambda / IAM Role are all independent
       - the Daily Brief Builder Lambda `portfolio-daily-brief-slack-summary-builder` performs only RDS read / operates normally even when the MarketConnector EC2 is in the stop state
       - responsibility separated from the EC2 lifecycle Lambda `portfolio-paper-ec2-lifecycle-dispatcher` / IAM Role / call path / Target are all independent
   (3) Operates independently of the ECS View
       - the Daily Brief Slack fires automatically regardless of the ECS View (`portfolio-view-service`) desiredCount 0/1 operation
       - port-view's existing `SlackNotificationService` is retained, not removed (responsibility for View Daily Batch manual execution result notifications)
       - the AWS common Slack notifier (`portfolio-event-notifier`) is separated as a single entry point for operational event notifications (OD-MS-030 alignment)
 2) This date's operator direct new-work facts
   (1) 2 new Builder Lambdas
       - `portfolio-approval-slack-summary-builder` — the main-Daily-execution-side Approval Required Slack builder
       - `portfolio-daily-brief-slack-summary-builder` — the Daily Brief Slack builder (Python 3.12 + `pg8000` + `DB_PASSWORD_SECRET_VALUE_FROM` Secrets Manager `valueFrom`)
   (2) new mini Step Functions
       - `portfolio-daily-brief-slack-notification` (ACTIVE / structure `BuildDailyBriefPayload → SendSlackNotifier`)
   (3) 2 new IAM Roles
       - `portfolio-daily-brief-sfn-role` (limited to Builder + Notifier Lambda invoke / 0 Resource · Action wildcards)
       - `portfolio-daily-brief-scheduler-role` (limited to Daily Brief state machine StartExecution / 0 Resource · Action wildcards)
   (4) 2 Schedulers added ENABLED
       - pre-market `portfolio-daily-brief-morning-slack-0750-kst` (cron `cron(50 7 ? * MON-FRI *)` / Asia/Seoul / Flexible OFF / input `MORNING_BRIEF`)
       - post-market `portfolio-daily-brief-evening-slack-1550-kst` (cron `cron(50 15 ? * MON-FRI *)` / Asia/Seoul / Flexible OFF / input `EVENING_BRIEF`)
   (5) Notifier formatter improvement
       - 2 eventType aliases (`MORNING_BRIEF → PRE_MARKET_STATUS` / `EVENING_BRIEF → POST_MARKET_STATUS`)
       - nested `balance` / `positions` adapter
       - Builder `title` priority
       - post-market `어제 대비` display
       - consistent application of the profit/loss prefix rule (negative `🔵` / positive `🔴` / 0 `⚪`)
   (6) smoke passed
       - morning smoke `daily-brief-morning-smoke-safe-20260630-193255-68f50aeb` `SUCCEEDED`
       - evening smoke `daily-brief-evening-smoke-safe-20260630-193300-aa2b9a12` `SUCCEEDED`
       - Slack pre-market · post-market reception confirmed
       - latest balance snapshot `id=281` / `as_of_date=2026-06-30` / `total_eval_amount=8,706,505` / `cash_balance=8,706,505` / `cumulativeProfitRate=-12.94%` / `cumulativeProfitAmount=-1,293,495` / `positionCount=0` / evening delta `0원`
 3) This date's 05 spec scope change facts
   (1) 0 changes to the port-view ECS Fargate task definition `portfolio-view:2` (Daily Brief automation is unrelated to the ECS View / 0 changes to the port-view image / SG / CloudWatch Logs `/ecs/portfolio-view`)
   (2) the ECS service desiredCount 0/1 operation policy is retained as is (aligned with desiredCount 0 termination after validation)
   (3) 0 changes to the ECS View → AWS Step Functions Step 12~17 approval execution flow
   (4) 0 changes to the MarketConnector EC2 / Crawler EC2 lifecycle automation

### Decision / risk mapping (2026-06-30 afternoon Slack)

- OD-MS-002 / OD-MS-009 / OD-MS-030 / OD-MS-031 / OD-MS-037 evidence reinforced without changing the bodies / OD-MS-038 new (Daily Brief Slack mini workflow operation method / Decision Summary count 96 → 97 / confirmed 51 → 52 / provisional 42 retained)
- R-AUTO-035 new (risk of Daily Brief Slack automatic delivery failure or duplicate delivery / Status `Mitigated` / first live automatic-fire validation is a next-business-day follow-up)
- R-AUTO-024 (risk of Slack webhook URL plaintext exposure / `Accepted`) mitigation retained as is / migrate to Secrets Manager or SSM SecureString after operation stabilization (06 spec follow-up phase responsibility)

### This date's factual recording scope (2026-06-30 afternoon Slack)

- This date's Kiro work = performed only the cross-reference accumulation of this section in 05 spec `operation-notes.md`
- Operator direct-performed area = 2 new Builder Lambdas + 1 new mini state machine + 2 Schedulers ENABLED + 2 new IAM Roles + Notifier formatter improvement + `portfolio-paper-daily-step1-17-approval` ASL update
- 0 Kiro-side changes on this date to AWS CLI / boto3 / psql / Lambda execution / Step Functions execution / Slack webhook / KIS API calls / 0 Kiro-side changes on this date to AWS resource creation · modification · deletion
- 0 plaintext quotations of Lambda code body / Step Functions ASL body / Scheduler target JSON body / Slack message body / Builder output full text / Notifier input full text / CloudWatch Logs full text / full IAM Policy body / Slack webhook URL / actual IAM Role ARN / actual state machine ARN / raw 12-digit account number / DB password (R-DOCS-001 alignment)
- Operational identifiers (recorded as facts per the user-specified policy / not secrets):
  - 2 Builder Lambda names / Notifier Lambda name / mini state machine name.
  - 2 IAM Role names / 2 Scheduler names / 2 cron expressions / Asia/Seoul / Flexible OFF.
  - 4 eventType aliases + 3 labels here.
  - Lambda runtime `Python 3.12` / DB driver `pg8000`.
  - DB password injection method `DB_PASSWORD_SECRET_VALUE_FROM` / Slack webhook environment-variable name `SLACK_WEBHOOK_URL`.
  - profit/loss prefix labels / 2 smoke execution names / balance snapshot summary.


## 2026-07-01 — paper Daily Step 12~17 automatic execution ENABLED (View manual approval fallback retained)

- **The ECS View → Step Functions Step 12~17 approval execution path already passed on 2026-06-30 afternoon**:
  - port-view ECS Fargate first-porting + Daily Batch AWS Step 12~17 approval-button execution inside the ECS View passed.
  - executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / state machine `portfolio-paper-daily-step12-17-approval` / status `SUCCEEDED`.
  - DB after-check passed / `connector_balance_snapshot id=281` / `as_of_date=2026-06-30`.
  - aligned with the "3. ECS Fargate porting: complete" block in the 05 spec body / R-AUTO-033 · R-AUTO-034 [2026-06-30 afternoon reinforcement].
- **2026-07-01 is Step 12~17 automatic Scheduler ENABLED without View manual approval**:
  - Step 12~17 Scheduler `portfolio-paper-daily-step12-17-order-0901-kst` DISABLED → ENABLED transition complete.
  - LastModificationDate `2026-07-01T13:53:57.160+09:00` / cron `cron(1 9 ? * MON-FRI *)` / Asia/Seoul / FlexibleTimeWindow OFF.
  - Target Lambda `portfolio-paper-daily-scheduler-dispatcher` / Target Input `{"scheduleType":"STEP12_17_ORDER","dryRun":false}`.
  - the weekday 09:01 KST automatic execution triggers StartExecution of the Step 12~17 approval workflow (`portfolio-paper-daily-step12-17-approval`).
  - candidate present → proceeds automatically through broker order submission without View manual approval / no candidate → NO_TARGET safe termination.
- **port-view retains the query · approval · operations UI role** — no change to the port-view MS's own compute decision (first priority = ECS Fargate Service / OD-MS-002 alignment) / the port-view screens (Dashboard / Balance / Positions / Orders / Reports / Daily) query · approval · operations UI responsibility retained as is / port-view `SlackNotificationService` retained (responsibility for View Daily Batch manual execution result notifications / OD-MS-010 alignment).
- **paper automatic orders switched to the 09:01 Scheduler path** — until 2026-06-30 afternoon, Step 12~17 approval execution was entered via the ECS View or Local View → Step Functions StartExecution path (manual approval first). From 2026-07-01, for rounds with a candidate, automatic execution runs via the 09:01 Scheduler path / the Daily Batch AWS Step 12~17 approval button inside port-view is redefined into the **operator fallback / manual re-execution path** (kept in a state where approval execution is possible inside the View too, without interrupting the operational round).
- **View manual execution retained as operator fallback** — when a 09:01 Scheduler / Dispatcher Lambda / Step Functions execution failure · wrong-state automatic execution is detected:
  - (a) block automatic firing with the 09:01 Scheduler `disable-schedule` (R-AUTO-025 [2026-07-01 automatic ENABLE entry] + R-AUTO-037 rollback alignment).
  - (b) manual re-execution via the Daily Batch approval-execute button inside port-view or the Local View wrapper.
  - (c) proceed with DB after-check + Slack channel post-audit.
  - for the Step 12~17 Scheduler enable/disable operation procedure, see [`./runbook.md`](./runbook.md).
- **Fargate Task Role follow-up** — the `states:StartExecution` Resource pattern of `portfolio-paper-view-task-role` is granted limited to both the general workflow ARN + approval workflow ARN (0 Action / Resource wildcards / 06 spec follow-up phase responsibility retained as is) / this date's 09:01 Scheduler ENABLE transition proceeded without changing the View Task Role privilege (the Dispatcher Lambda is responsible for the Step Functions StartExecution call via a separate IAM Role / OD-MS-032 alignment).
- **Decision / risk changes** — no new decision / no change to the Decision Summary count (total 97 / confirmed 52 / provisional 42 retained). OD-MS-002 / OD-MS-009 / OD-MS-032 / OD-MS-033 / OD-MS-037 / OD-SAFE-001 ~ OD-SAFE-004 evidence reinforced without changing the bodies. No R-AUTO-033 · R-AUTO-034 mitigation added on this date (the 2026-06-30 afternoon reinforcement retained) / R-AUTO-025 [2026-07-01 automatic ENABLE entry] + R-AUTO-037 new are 04 spec follow-up responsibilities (from the port-view perspective, only the View manual execution fallback retention alignment is re-confirmed).
- **No aws-live policy change** — **This change is limited to aws-paper. It does not change the aws-live automatic BUY / SELL policy, and live retains the candidate + manual-approval-first policy.** (OD-SAFE-002 / OD-SAFE-003 alignment)
- **This date's Kiro-side factual record**:
  - 0 AWS CLI / boto3 / psql / Spring Boot / external API execution.
  - 0 Kiro-side changes to AWS resource creation · modification · deletion.
  - 0 broker order submissions / 0 port-view source changes.
  - 0 changes on this date to port-view README / AGENTS.md / CHANGELOG / docs / worklog (only spec-area document updates performed).
- **0 plaintext records of sensitive information** — all `[REDACTED]`-family placeholders:
  - Slack webhook URL / DB password / KIS app key · KIS app secret.
  - raw 12-digit account number / token / RDS password / RDS endpoint hostname / raw 12-digit account-id.
  - actual IAM Role · secret · state machine · Lambda ARN / public IP / image digest full sha256 / task ARN / ENI ID / raw broker_order_no.
- **Operational identifiers** (recorded as facts per the user-specified policy):
  - Scheduler name · cron · Asia/Seoul · Target Input.
  - state machine name · executionName · status · start · stop timestamp.
  - Slack event label · balance snapshot id · as_of_date · amount · count · 7 lineup names.
