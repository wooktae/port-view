# Runbook — 05-port-view-ecs-and-runbook

This document is the runbook that organizes the port-view ECS Fargate Public IP first-porting operation procedure.

- Single source of truth = the factual record in the 2026-06-30 (afternoon) body (`3. ECS Fargate 포팅: 완료`) of this spec's `operation-notes.md`.
- Execution actor = performed directly by the operator / Kiro does not execute actual commands in this spec's workspace.

## Application scope

- First application environment: `aws-paper`
- region: `ap-northeast-2`
- First application target: port-view (Spring Boot / Thymeleaf operations console)
- This runbook covers only the first-porting approach: no ALB + public subnet + `assignPublicIp=ENABLED` + operator IP/32 SG inbound + CloudWatch Logs retention 7 days.
- ALB / HTTPS / Route53 / Cloudflare Tunnel / authentication · authorization / Auto Scaling / Blue-Green / multi-AZ operation is on hold until after first porting (responsibility of the 05 / 06 / 07 / 10 spec follow-up phases).

## Operation policy / safety criteria

- The `desiredCount` of ECS service `portfolio-view-service` is 1 only at operation time, and is terminated to 0 after validation to save cost.
- Because the public IP changes on Fargate task restart, the operator auto-queries the public IP right after each start and updates the browser URL.
- Security Group `sgroup-port-view-ecs` inbound allows only TCP 8080 / source operator public IP/32. Do not open 0.0.0.0/0 entirely.
- All AWS CLI command examples in this runbook are written in alignment with the 3 "operation command authoring rules (additional)" in `.kiro/AGENTS.md` (`list/describe → extract variable → subsequent verification` / prior `information_schema.columns` check / no SUCCESS marker after a failed command).
- In every command example, the following values are not recorded in plaintext:
  - actual ARN / account-id / public IP / image digest full sha256 / task ARN / ENI ID.
  - RDS endpoint hostname / Slack webhook URL / account number / KIS credential / DB password / actual state machine ARN.
- Placeholders used = `[REDACTED]` / `[REDACTED_ACCOUNT_NO]` / `[REDACTED_PUBLIC_IP]` / `[REDACTED_ARN]` / `[REDACTED_TASK_ARN]` / `[REDACTED_SECRET_ARN]` / `[REDACTED_BROKER_ORDER_NO]`.
- This runbook does not directly execute ECS RunTask / SubmitJob / KIS API / Slack Webhook / Daily Batch automatic trigger. All actual execution is the operator's responsibility.

## Procedure 1. Start ECS View (desiredCount 1)

**Purpose** — start the ECS Fargate task at validation / operation time and obtain the browser access URL.

**Preconditions**:

- ECS cluster `portfolio-paper-cluster` / ECS service `portfolio-view-service` / task definition `portfolio-view:2` are in the registered state.
- Security Group `sgroup-port-view-ecs` inbound = TCP 8080 / operator IP/32 configuration complete.

**Execution / validation**:

 1) Transition ECS service desiredCount to 1
   (1) Command pattern
       - `aws ecs update-service --cluster portfolio-paper-cluster --service portfolio-view-service --desired-count 1`
       - Confirm in the command output that `desiredCount` is updated to 1
   (2) Wait for stable state
       - `aws ecs wait services-stable --cluster portfolio-paper-cluster --services portfolio-view-service`
       - Or confirm `rolloutState=COMPLETED` in `aws ecs describe-services --cluster portfolio-paper-cluster --services portfolio-view-service --query 'services[0].deployments'`
   (3) Confirm RUNNING
       - `aws ecs list-tasks --cluster portfolio-paper-cluster --service-name portfolio-view-service --desired-status RUNNING`
       - Confirm at least 1 task ARN appears in the result
 2) Auto-query the public IP (rule 1 alignment)
   (1) Extract TASK_ARN
       - `aws ecs list-tasks --cluster portfolio-paper-cluster --service-name portfolio-view-service --desired-status RUNNING --query 'taskArns[0]' --output text` → store the result in the `TASK_ARN` variable
   (2) Extract ENI_ID
       - `aws ecs describe-tasks --cluster portfolio-paper-cluster --tasks $TASK_ARN --query "tasks[0].attachments[0].details[?name=='networkInterfaceId'].value" --output text` → store the result in the `ENI_ID` variable
   (3) Extract PUBLIC_IP
       - `aws ec2 describe-network-interfaces --network-interface-ids $ENI_ID --query 'NetworkInterfaces[0].Association.PublicIp' --output text` → store the result in the `PUBLIC_IP` variable
   (4) Output the browser access URL
       - Output `http://$PUBLIC_IP:8080` (no plaintext recording of the public IP / `[REDACTED_PUBLIC_IP]` placeholder alignment)
       - Confirm the Dashboard / Balance / Positions / Orders / Reports / Daily screens display normally at the output URL
 3) Confirm Spring Boot started
   (1) Auto-query CloudWatch Logs
       - `aws logs describe-log-streams --log-group-name /ecs/portfolio-view --order-by LastEventTime --descending --limit 1 --query 'logStreams[0].logStreamName' --output text` → store in the `LOG_STREAM` variable
       - Confirm Spring Boot started / HikariPool RDS connection success in the output body of `aws logs get-log-events --log-group-name /ecs/portfolio-view --log-stream-name $LOG_STREAM --limit 50`
   (2) No plaintext recording of the raw log body (R-DOCS-001 alignment) — record only the fact "Spring Boot started confirmed" in the operator notes

## Procedure 2. Stop ECS View (desiredCount 0)

**Purpose** — terminate the Fargate task after validation to block the accumulation of the Fargate hourly unit price + public IPv4 cost.

**Precondition** — ECS service `portfolio-view-service` is RUNNING in the desiredCount 1 state.

**Execution / validation**:

 1) Transition ECS service desiredCount to 0
   (1) Command pattern
       - `aws ecs update-service --cluster portfolio-paper-cluster --service portfolio-view-service --desired-count 0`
       - Confirm `desiredCount=0` in the output
   (2) Confirm task termination
       - Confirm the result of `aws ecs list-tasks --cluster portfolio-paper-cluster --service-name portfolio-view-service --desired-status RUNNING --query 'taskArns'` transitions to an empty array (`[]`)
       - Fargate task terminated / public IP released / Fargate cost accumulation stopped
 2) Caution on next start
   (1) Public IP reissue
       - On the `desiredCount 0 → 1` transition, a new public IP is issued. The operator re-runs the public IP auto-query in Procedure 1.2 to update the browser URL.
   (2) Retain the Security Group inbound rule
       - Retain the operator IP/32 inbound of `sgroup-port-view-ecs` as is (see Procedure 4).

## Procedure 3. Approval execution of AWS Step Functions Step 12~17 from ECS View

**Purpose** — approve and execute the Step 12~17 approval workflow from the Daily Batch screen inside ECS View, and complete the post-validation.

**Preconditions**:

- ECS View start complete via Procedure 1 / public IP access success / RDS connection success.
- `paperOrderEnabled=true` + approval range gate passed state.
- The preflight 4-item 0-count condition is maintained (see the execution order below).

**Execution / validation / rollback**:

 1) DB preflight before entry
   (1) Confirm DB column names (rule 2 alignment)
       - First confirm the actual column names of `strategy_execution_order` / `connector_order_request` / `connector_balance_snapshot` / `connector_position_snapshot` with `information_schema.columns`.
       - In particular, `connector_position_snapshot` has no `balance_snapshot_id` column, so write the validation based on `account_no` + `as_of_date`.
   (2) Confirm the preflight 4 items are 0-count
       - REQUESTED `strategy_execution_order` remaining 0
       - retryable rejected `strategy_execution_order` 0
       - active `connector_order_request` 0
       - this date's new `connector_order_request` 0
   (3) Halt immediately on preflight failure
       - If any of the 0-count conditions is violated, halt the Step 12~17 approval execution and proceed with operator inspection. Do not output a SUCCESS marker after a failed SQL (rule 3 alignment).
 2) Click the approval-execute button on the screen
   (1) Enter the Daily Batch screen
       - `http://$PUBLIC_IP:8080/daily-batch`
       - Screen display: `Execution ON` / `Local File OFF` / `Full Pipeline ON` / `Paper Order ON` / allowed range `1~17` / AWS Step 12~17 approval-execute button active
   (2) Click the approval-execute button
       - `POST /daily-batch/aws-stepfunctions/start-approval-range`
       - Confirm the `executionName` + redacted `executionArn` in the response flash message
       - state machine `portfolio-paper-daily-step12-17-approval` execution starts
 3) Confirm the execution result
   (1) Auto-query the Step Functions status
       - Using the `executionName` received in the response as a variable, confirm the status with `aws stepfunctions list-executions --state-machine-arn <REDACTED_ARN> --status-filter SUCCEEDED` or `describe-execution`
       - Confirm status `SUCCEEDED` / last state `ExecutionSucceeded`
   (2) Confirm Slack reception
       - Confirm the `DAILY_EXECUTION_SUCCESS` event message in the operator Slack channel (no plaintext recording of the message body)
   (3) DB after-check (rule 2 alignment)
       - new `connector_order_request` 0
       - REQUESTED `strategy_execution_order` remaining 0
       - active `connector_order_request` 0
       - Confirm the `as_of_date` · `total_eval_amount` · `cash_balance` of the latest `connector_balance_snapshot`
       - Holdings validation is based on `account_no` + `as_of_date` of `connector_position_snapshot` (do not use an assumed column name)

## Procedure 4. Update Security Group inbound when the operator IP changes

 1) Confirm the new operator public IP
   (1) The operator confirms the external IP as `NEW_OPERATOR_IP` using an external IP lookup tool
   (2) Use the `[REDACTED_PUBLIC_IP]` placeholder — no plaintext recording in this runbook
 2) Delete the existing inbound rule
   (1) Confirm the existing rule with `aws ec2 describe-security-groups --group-ids <SG_ID_PLACEHOLDER> --query 'SecurityGroups[0].IpPermissions'`
   (2) The operator revokes the existing operator IP/32 rule directly via console or CLI
 3) Add the new inbound rule
   (1) Add limited to TCP 8080 + source `NEW_OPERATOR_IP/32`
   (2) Do not open 0.0.0.0/0 entirely (R-AUTO-033 mitigation alignment)
 4) Access validation
   (1) Confirm the screen displays normally at the URL from the Procedure 1.2 public IP auto-query result
   (2) On screen display failure, inspect in the order: SG inbound applied / operator IP changed / Fargate task RUNNING

## Procedure 5. Handling faults / anomalies during operation

 1) ECS task RUNNING but screen access fails
   (1) Re-query the public IP (Procedure 1.2)
   (2) Inspect the SG inbound rule (Procedure 4)
   (3) Inspect the Fargate task health check log (CloudWatch Logs `/ecs/portfolio-view`)
 2) Partial screen data missing after Spring Boot started
   (1) Inspect the RDS connection (check for a missing column in advance with `information_schema.columns`)
   (2) Inspect the HikariPool log (do not quote the raw body)
   (3) Inspect whether the default account-number env (`PORTFOLIO_BATCH_DEFAULT_ACCOUNT_NO` / `PORTFOLIO_VIEW_ACCOUNT_DEFAULT_ACCOUNT_NO`) is missing
 3) When the Step Functions execution status is `FAILED`
   (1) Re-inspect whether the preflight 0-count in Procedure 3 of this runbook was violated
   (2) Confirm the last failed state in `aws stepfunctions get-execution-history` (no plaintext recording of the history body)
   (3) Before proceeding with the subsequent Daily Batch automatic trigger, the operator enters separate approval (OD-SAFE-002 / OD-SAFE-003 / OD-MS-033 09:01 hold policy alignment)

## Security / cost policy

- desiredCount 1 is retained only at operation time / terminated to 0 after validation (R-AUTO-033 [2026-06-30 afternoon reinforcement] / from a cost perspective, blocks the accumulation of the Fargate hourly unit price + public IPv4 cost).
- Do not open SG inbound 0.0.0.0/0 entirely (see Procedure 4 / R-AUTO-034 mitigation alignment).
- New public IP issued after task restart / operator re-queries at each start (Procedure 1.2 / 0 plaintext records in this runbook alignment).
- ALB · HTTPS · Route53 · Cloudflare Tunnel · authentication · authorization · multi-AZ · Auto Scaling · Blue/Green are on hold until after first porting / when introduced in the future, they are the responsibility of a separate phase (05 / 06 / 07 / 10 spec).
- Actual execution of AWS CLI / boto3 / psql / Spring Boot is the operator's direct responsibility / Kiro does not execute the commands in this runbook.

## References

- Single source-of-truth document of this runbook: the 2026-06-30 (afternoon) "3. ECS Fargate 포팅: 완료" block of [`./operation-notes.md`](./operation-notes.md)
- Validation checklist: [`./validation-checklist.md`](./validation-checklist.md)
- Decisions / risks: [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-MS-002 / OD-MS-009 / OD-MS-037 / [`../_common/risk-register.md`](../_common/risk-register.md) R-AUTO-033 / R-AUTO-034
- Operation command authoring rules: [`../../AGENTS.md`](../../AGENTS.md) 3 "operation command authoring rules (additional)"

## Step 12~17 Scheduler enable/disable operation procedure (added 2026-07-01)

**Purpose** — the ENABLED / DISABLED transition procedure for the aws-paper Step 12~17 Scheduler `portfolio-paper-daily-step12-17-order-0901-kst`.

**Preconditions**:

- From 2026-07-01, this Scheduler is in the ENABLED state and handles automatic execution on weekdays at 09:01 KST.
- **This change is limited to aws-paper** — it does not change the aws-live automatic BUY / SELL policy, and live retains the candidate + manual-approval-first policy (OD-SAFE-002 / OD-SAFE-003 alignment).

### Prior principles

- AWS CLI commands follow the `list/describe → extract variable → subsequent verification` pattern (AGENTS.md operation command authoring rule 1).
- Do not record the actual ARN / the 12-digit account-id raw value / Lambda ARN / state machine ARN / Slack webhook URL / secret plaintext in this document · operator notes · git commit message (R-DOCS-001 alignment / use a `[REDACTED]`-family placeholder when needed).
- Do not output a SUCCESS marker after a failed SQL / command (AGENTS.md operation command authoring rule 3).
- Operation identifiers such as the Scheduler name · cron · Asia/Seoul · Target Input · state machine name · Dispatcher Lambda name are not secrets, so they are recorded as facts in this document.

### 1) Confirm current state (list-schedules → get-schedule)

The Scheduler name is decided in advance (`portfolio-paper-daily-step12-17-order-0901-kst`). First confirm its existence with `list-schedules`, then confirm the State · cron · Timezone · FlexibleTimeWindow · Target Lambda · Target Input alignment with `get-schedule`.

```powershell
$Region      = 'ap-northeast-2'
$SchedName   = 'portfolio-paper-daily-step12-17-order-0901-kst'

# (a) 목록에서 이름 존재 확인
aws scheduler list-schedules `
  --region $Region `
  --name-prefix $SchedName `
  --query "Schedules[?Name=='$SchedName'].{Name:Name,State:State,GroupName:GroupName}" `
  --output table

# (b) 상세 정보(cron · Asia/Seoul · Flexible OFF · Target Lambda · Target Input)
aws scheduler get-schedule `
  --region $Region `
  --name $SchedName `
  --query "{State:State,Group:GroupName,Cron:ScheduleExpression,TZ:ScheduleExpressionTimezone,Flexible:FlexibleTimeWindow.Mode,TargetArnPresent:contains(@,'Target'),Input:Target.Input}" `
  --output json
```

- Response example (after the 2026-07-01 ENABLE transition) — `State=ENABLED` / `Cron=cron(1 9 ? * MON-FRI *)` / `TZ=Asia/Seoul` / `Flexible=OFF` / `Input={"scheduleType":"STEP12_17_ORDER","dryRun":false}`.
- The Target Lambda ARN is exposed as is in the response, so `[REDACTED_ARN]` masking is needed when sharing a screen capture · note.

### 2) Confirm Target.Input alignment

Confirm the `scheduleType=STEP12_17_ORDER` + `dryRun=false` alignment inside the `Target.Input` JSON. If Target.Input has changed, the Dispatcher Lambda may trigger the wrong state machine, so re-confirm without fail before the ENABLE transition.

- It must be `scheduleType=STEP12_17_ORDER` for the Dispatcher Lambda to trigger the `portfolio-paper-daily-step12-17-approval` state machine (OD-MS-032 alignment).
- It must be `dryRun=false` to proceed all the way to the actual broker order flow. In the `dryRun=true` state, automatic execution proceeds only to the dry-run level (no broker order submission).
- If `scheduleType=STEP1_11_APPROVAL` or another value is present, the Dispatcher Lambda may trigger the wrong state machine, so revert immediately.

### 3) DISABLED → ENABLED transition

For the ENABLED transition, specify `--state ENABLED` with `update-schedule`. Because `update-schedule` does not retain existing fields and replaces them with the newly specified values, always back up the existing values with `get-schedule` first, then re-specify the same fields.

```powershell
# (a) 기존 정의 백업(운영자 로컬 임시 파일 / 파일 안에 실제 ARN 이 포함되므로 secret 정책 정합 관리)
aws scheduler get-schedule `
  --region $Region `
  --name $SchedName `
  --output json | Out-File -Encoding utf8 "$env:TEMP\schedule-$SchedName-before-enable.json"

# (b) ENABLED 로 전환
$RoleArn = (aws scheduler get-schedule --region $Region --name $SchedName --query 'Target.RoleArn' --output text)
$TargetArn = (aws scheduler get-schedule --region $Region --name $SchedName --query 'Target.Arn' --output text)
$TargetInput = (aws scheduler get-schedule --region $Region --name $SchedName --query 'Target.Input' --output text)

aws scheduler update-schedule `
  --region $Region `
  --name $SchedName `
  --state ENABLED `
  --schedule-expression 'cron(1 9 ? * MON-FRI *)' `
  --schedule-expression-timezone 'Asia/Seoul' `
  --flexible-time-window '{"Mode":"OFF"}' `
  --target "{\"Arn\":\"$TargetArn\",\"RoleArn\":\"$RoleArn\",\"Input\":\"$TargetInput\"}"

# (c) State ENABLED 재확인
aws scheduler get-schedule `
  --region $Region `
  --name $SchedName `
  --query "{State:State,Cron:ScheduleExpression,TZ:ScheduleExpressionTimezone,Input:Target.Input}" `
  --output table
```

- Response alignment after applying this procedure on 2026-07-01 = `State=ENABLED` / LastModificationDate `2026-07-01T13:53:57.160+09:00`.
- **Caution: duplicate new start-execution**:
  - If you fire a manual `start-execution` together right after the ENABLED transition, it may duplicate with the 09:01 KST automatic execution.
  - If manual validation is needed, execute it in a time window that does not overlap the 09:01 automatic execution, or execute it after the 09:01 round completes.
  - Duplicate broker order risk = R-AUTO-001 / R-BROKER-004 alignment.

### 4) ENABLED → DISABLED rollback

If automatic execution is in progress in a wrong state or safety detection fails, transition to DISABLED immediately.

```powershell
# (a) 기존 정의 백업
aws scheduler get-schedule `
  --region $Region `
  --name $SchedName `
  --output json | Out-File -Encoding utf8 "$env:TEMP\schedule-$SchedName-before-disable.json"

# (b) DISABLED 로 전환
$RoleArn = (aws scheduler get-schedule --region $Region --name $SchedName --query 'Target.RoleArn' --output text)
$TargetArn = (aws scheduler get-schedule --region $Region --name $SchedName --query 'Target.Arn' --output text)
$TargetInput = (aws scheduler get-schedule --region $Region --name $SchedName --query 'Target.Input' --output text)

aws scheduler update-schedule `
  --region $Region `
  --name $SchedName `
  --state DISABLED `
  --schedule-expression 'cron(1 9 ? * MON-FRI *)' `
  --schedule-expression-timezone 'Asia/Seoul' `
  --flexible-time-window '{"Mode":"OFF"}' `
  --target "{\"Arn\":\"$TargetArn\",\"RoleArn\":\"$RoleArn\",\"Input\":\"$TargetInput\"}"

# (c) State DISABLED 재확인
aws scheduler get-schedule `
  --region $Region `
  --name $SchedName `
  --query "{State:State,Cron:ScheduleExpression,TZ:ScheduleExpressionTimezone,Input:Target.Input}" `
  --output table
```

- After the DISABLED transition, an in-progress Step Functions execution does not stop immediately and continues. To halt an in-progress execution, call `aws stepfunctions stop-execution` separately (R-AUTO-037 rollback alignment).
- After rollback, manual re-execution fallback is possible via the Daily Batch AWS Step 12~17 approval-execute button inside port-view or the Local View wrapper.

### 5) Confirm the state of all 7 Daily lineup items

From 2026-07-01, confirm the 7 aws-paper Daily automation lineup items at once. If even one State differs from expected, audit the cause immediately.

```powershell
$Region = 'ap-northeast-2'
$Names = @(
  'portfolio-paper-ec2-start-0750-kst',
  'portfolio-daily-brief-morning-slack-0750-kst',
  'portfolio-paper-daily-step1-11-approval-0800-kst',
  'portfolio-paper-daily-step12-17-order-0901-kst',
  'portfolio-paper-intraday-snapshot-evaluate-10min-kst',
  'portfolio-daily-brief-evening-slack-1550-kst',
  'portfolio-paper-marketconnector-stop-1550-kst'
)

foreach ($n in $Names) {
  aws scheduler get-schedule `
    --region $Region `
    --name $n `
    --query "{Name:'$n',State:State,Cron:ScheduleExpression,TZ:ScheduleExpressionTimezone}" `
    --output json
}
```

- Expected result at the 2026-07-01 pass point = all 7 items `State=ENABLED` / `Timezone=Asia/Seoul` / `FlexibleTimeWindow=OFF` / cron as each Scheduler's defined value.
- If any one of the 7 lineup items is `State=DISABLED` or the response fails, audit the cause immediately — Dispatcher Lambda IAM Role · Scheduler Group · CloudWatch Logs · whether the Step Functions state machine is ACTIVE · Target Input JSON alignment.

### 6) Safety constraints

- This procedure is limited to aws-paper — there is no change to the aws-live automatic BUY / SELL policy until entering the aws-live cutover phase (10 spec follow-up phase) (OD-SAFE-002 / OD-SAFE-003 alignment).
- On the ENABLED / DISABLED transition, do not record the actual ARN / the 12-digit account-id raw value / Lambda ARN / state machine ARN / Slack webhook URL / secret plaintext in this document · operator notes · git commit message.
- Observe the first 09:01 KST automatic execution round after the ENABLED transition live on the next business day:
  - Scheduler invocation log / Dispatcher Lambda CloudWatch Logs.
  - Step Functions execution creation / Slack reception / DB after-check alignment audit.
  - followups-overview 2026-07-01 follow-up memo alignment.
- The no-automatic-retry policy (OD-SAFE-004 / R-AUTO-001) is retained as is — even after the Step 12~17 Scheduler is ENABLED, the no-Retry-block policy for BUY / SELL / fill sync / position-change-series states inside the Step Functions state machine is retained.
