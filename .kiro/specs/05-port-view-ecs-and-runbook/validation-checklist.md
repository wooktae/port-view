# Validation Checklist — 05-port-view-ecs-and-runbook

This document is the validation checklist for the port-view ECS Fargate Public IP first porting + ECS View → AWS Step Functions Step 12~17 approval execution.

- Single source of truth = this spec's [`./operation-notes.md`](./operation-notes.md) 2026-06-30 (afternoon) "3. ECS Fargate 포팅: 완료" block.
- Usage condition = per-item inspection after progressing through Procedure 1 ~ Procedure 3 of [`./runbook.md`](./runbook.md).

## Dashboard (2026-06-30 afternoon pass state summary)

| Category | Item count | Pass (complete) | Most recent pass round |
|---|---|---|---|
| Deploy (Docker · ECR · TaskDefinition · ECS Service) | 4 | 4 | 2026-06-30 afternoon |
| Runtime (Spring Boot / RDS) | 2 | 2 | 2026-06-30 afternoon |
| View screen query (Dashboard / Balance / Positions / Orders / Reports / Daily) | 1 | 1 | 2026-06-30 afternoon |
| Daily Batch UI gate (Step 1~11 · Step 12~17 approval button) | 2 | 2 | 2026-06-30 afternoon |
| Step Functions execution + Slack reception | 2 | 2 | 2026-06-30 afternoon |
| DB after-check + desiredCount 0 termination | 2 | 2 | 2026-06-30 afternoon |

## Application scope / safety policy

- First application environment = `aws-paper` / region = `ap-northeast-2` / target = port-view.
- Target porting approach = no ALB + public subnet + `assignPublicIp=ENABLED` + operator IP/32 SG inbound + CloudWatch Logs retention 7 days first porting.
- On-hold items (after first porting / responsibility of the 05 · 06 · 07 · 10 spec follow-up phases):
  - ALB / HTTPS / Route53 / Cloudflare Tunnel.
  - authentication · authorization / Auto Scaling / Blue-Green / multi-AZ.
- Command authoring rules = alignment with the 3 "operation command authoring rules (additional)" in [`../../AGENTS.md`](../../AGENTS.md):
  - `list/describe → extract variable → subsequent verification`.
  - prior `information_schema.columns` check.
  - no SUCCESS marker after a failed command.
- Execution actor = operator direct / Kiro does not execute in this spec's workspace.
- 0 plaintext records (`[REDACTED]` or placeholder):
  - password / token / API key / 12-digit account number raw value / RDS password / Slack webhook URL.
  - actual ARN / public IP / image digest full sha256 / task ARN / ENI ID / raw broker_order_no.

## 13 check items / this date's pass results

The 13 items of this checklist are identical to the "검증 체크리스트" of the 2026-06-30 (afternoon) "3. ECS Fargate 포팅: 완료" block in this spec's [`./operation-notes.md`](./operation-notes.md). This date's pass results mark only factually recorded / operator-directly-validated items as "complete".

 1) Docker image build: complete
   (1) Operator direct build
       - Confirmation target: the fact of image tag / digest creation (0 plaintext records of the image digest full sha256 in this note / `[REDACTED]` placeholder)
       - Pass criterion: Docker build result exit code 0 / no error in the build log
 2) ECR push: complete
   (1) image push to ECR repository `portfolio-view`
       - Confirmation target: ECR repository name `portfolio-view` / the fact of the push result (0 plaintext records of the image digest)
       - Pass criterion: the result timestamp of `aws ecr describe-images --repository-name portfolio-view --query 'imageDetails[0].imagePushedAt' --output text` matches this date's push time
 3) task definition registration: complete
   (1) `portfolio-view:1` → `portfolio-view:2` correction
       - Confirmation target: revision 1 → 2 / default account-number env missing correction (`PORTFOLIO_BATCH_DEFAULT_ACCOUNT_NO` + `PORTFOLIO_VIEW_ACCOUNT_DEFAULT_ACCOUNT_NO`)
       - Pass criterion: the result of `aws ecs describe-task-definition --task-definition portfolio-view --query 'taskDefinition.revision' --output text` = `2`
 4) ECS service `portfolio-view-service` RUNNING: complete
   (1) desiredCount 1 + at least 1 RUNNING task ARN
       - Confirmation target: ECS cluster `portfolio-paper-cluster` / ECS service `portfolio-view-service`
       - Pass criterion: the array length of `aws ecs list-tasks --cluster portfolio-paper-cluster --service-name portfolio-view-service --desired-status RUNNING --query 'taskArns'` ≥ 1
 5) Spring Boot started in CloudWatch Logs `/ecs/portfolio-view`: complete
   (1) Confirm Spring Boot started in the recent log stream
       - Confirmation target: log group `/ecs/portfolio-view` / recent log stream
       - Pass criterion: record the fact "Spring Boot started confirmed" in the operator notes (0 plaintext quotations of the raw log body / R-DOCS-001 alignment)
 6) RDS connection success (HikariPool start completed): complete
   (1) HikariPool start completed + Spring Boot RDS connection
       - Confirmation target: log group `/ecs/portfolio-view` / Spring profile `aws-paper` / default schema `ops` / HikariPool start completed
       - Pass criterion: record the fact "HikariPool RDS connection success / default schema `ops` confirmed" in the operator notes / 0 plaintext records of the RDS endpoint hostname / DB password
 7) Dashboard / Balance / Positions / Orders / Reports / Daily screen query: complete
   (1) 6 screens display normally
       - Confirmation target: `http://[REDACTED_PUBLIC_IP]:8080/dashboard` / `/balance-summary` / `/positions` / `/orders` / `/strategy/reports/latest` / `/daily-batch`
       - Pass criterion: all 6 screens render normally / the default account number is reflected normally / screen captures are not attached to this note (avoid sensitive-information exposure)
 8) AWS Step 1~11 button display: complete
   (1) Safety gate condition alignment
       - Confirmation target: the AWS Step 1~11 button active condition inside the Daily Batch screen (no Step 1~11 button condition conflict even in the `fullPipelineExecutionEnabled=true` state)
       - Pass criterion: the AWS Step 1~11 button is displayed on the screen / this date's automatic execution retains a separate EventBridge Scheduler path (out of scope for this checklist)
 9) AWS Step 12~17 approval button display: complete
   (1) Safety gate condition alignment
       - Confirmation target: the AWS Step 12~17 approval button active condition inside the Daily Batch screen (`paperOrderEnabled=true` + approval range gate passed)
       - Pass criterion: the AWS Step 12~17 approval button is displayed on the screen / active without conflict even in the `fullPipelineExecutionEnabled=true` state
 10) ECS View → Step 12~17 execution `SUCCEEDED`: complete
   (1) Normal termination after the operator directly clicks approval
       - executionName: `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8`
       - state machine: `portfolio-paper-daily-step12-17-approval`
       - status: `SUCCEEDED`
       - start: `2026-06-30T14:15:42.899+09:00`
       - stop: `2026-06-30T14:18:48.358+09:00`
       - final state: `ExecutionSucceeded`
       - 0 plaintext records of the actual state machine ARN in this note (`[REDACTED_ARN]` placeholder)
 11) Slack `DAILY_EXECUTION_SUCCESS` reception: complete
   (1) Confirm reception in the operator Slack channel
       - Confirmation target: Slack event `DAILY_EXECUTION_SUCCESS`
       - Pass criterion: confirm the fact of reception around 14:18 KST on this date in the operator Slack channel / 0 plaintext records of the Slack message body / webhook URL
 12) DB after-check 0-count confirmation: complete
   (1) preflight 4 items + balance snapshot retrieval
       - REQUESTED `strategy_execution_order` after: 0
       - retryable rejected `strategy_execution_order` after: 0
       - active `connector_order_request` after: 0
       - today connector orders after: 0 rows
       - latest `connector_balance_snapshot id=281` / `as_of_date=2026-06-30`
       - `total_eval_amount=8,706,505` / `cash_balance=8,706,505` / `eval_profit=0`
       - `source_version=connector-intraday-snapshot-refresh-1.0.0`
       - holdings validation is based on `account_no` + `as_of_date` of `connector_position_snapshot` (no `balance_snapshot_id` column / rule 2 alignment)
       - the fact of identifying 6 past stale `connector_order_request` (2026-04-27 ACCEPTED remaining / unrelated to this date's execution / follow-up cleanup candidate / preflight count contamination risk is a follow-up candidate)
 13) desiredCount 0 termination: complete
   (1) Fargate task terminated / public IP released
       - Confirmation target: ECS service `portfolio-view-service` desiredCount 0 / 0 running task ARN
       - Pass criterion: the result of `aws ecs list-tasks --cluster portfolio-paper-cluster --service-name portfolio-view-service --desired-status RUNNING --query 'taskArns'` is an empty array (`[]`)
       - premise of a new public IP issued at next start / SG inbound operator IP/32 retained

## Decision / risk mapping

- OD-MS-002 (port-view compute = ECS Fargate Service first priority / 🟢 확정) — reinforced first-validation evidence through this date's checklist 1) ~ 13) pass (no change to the decision body).
- OD-MS-009 (Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) — reinforced first-validation evidence through this checklist's 10) ECS View → Step Functions Step 12~17 approval execution `SUCCEEDED` pass (no change to the decision body).
- OD-MS-037 (View Local AWS Paper read-only first scope + policy of preceding batch second validation before ECS / Fargate entry) — this checklist's 10) pass reinforces evidence as the first validation after ECS / Fargate entry, the follow-up phase of OD-MS-037 (no change to the decision body).
- R-AUTO-033 [2026-06-30 afternoon reinforcement] — third validation of Public IP direct access + operator IP/32 SG + ECS View → Step 12~17 approval through this checklist's 4) · 7) · 9) · 10) · 11) · 12) · 13) pass / Status `Mitigated` retained.
- R-AUTO-034 [2026-06-30 afternoon reinforcement]:
  - through this checklist's 10) pass, first validation that the Fargate ECS task role (`portfolio-paper-view-task-role`) `states:StartExecution` privilege is granted limited to the Step 12~17 approval state machine ARN.
  - Status `Open` retained (cross-spec audit when ALB · HTTPS · CloudWatch alarms are introduced in the future / responsibility of the 06 spec follow-up phase).
- The fact of identifying 6 stale `connector_order_request` in this checklist's 12) is recorded as a new follow-up candidate (preflight count contamination risk / cleanup decision) in the followups-overview 2026-06-30 (afternoon) follow-up memo.

## This date's factual recording scope (2026-06-30 afternoon)

- All 13 items of this checklist were validated directly by the operator / 0 Kiro-side automatic validations.
- 0 Kiro-side changes to AWS CLI / boto3 / psql / Spring Boot execution / external API calls on this date / 0 Kiro-side changes to AWS resource creation · modification · deletion on this date.
- broker / KIS calls = 1 ECS View → Step 12~17 approval execution (`SUCCEEDED` / NO_TARGET / 0 broker order submissions) + balance refresh only / 0 additional BUY · SELL · cancel · modify / 0 fill · position sync automatic retries / 0 aws-live actions.
- 0 plaintext records of sensitive information — all placeholders:
  - `[REDACTED]` / `[REDACTED_ACCOUNT_NO]` / `[REDACTED_PUBLIC_IP]`.
  - `[REDACTED_ARN]` / `[REDACTED_TASK_ARN]` / `[REDACTED_SECRET_ARN]` / `[REDACTED_BROKER_ORDER_NO]`.

## References

- Single source-of-truth document: the 2026-06-30 (afternoon) "3. ECS Fargate 포팅: 완료" block of [`./operation-notes.md`](./operation-notes.md)
- Operation procedure: [`./runbook.md`](./runbook.md)
- Decision / risk mapping: [`../_common/operator-decisions.md`](../_common/operator-decisions.md) / [`../_common/risk-register.md`](../_common/risk-register.md)
- Operation command authoring rules: [`../../AGENTS.md`](../../AGENTS.md) 3 "operation command authoring rules (additional)"

## 2026-07-01 validation results — paper Daily automation first full ON + Step 12~17 Scheduler ENABLED

This validation is limited to aws-paper. No aws-live policy change (OD-SAFE-002 / OD-SAFE-003 alignment).

- [x] **Step 12~17 manual execution `SUCCEEDED`**:
  - executionName `port-manual-daily-step12-17-20260701-043747` / state machine `portfolio-paper-daily-step12-17-approval`.
  - status `SUCCEEDED` / execution history `ExecutionSucceeded`.
  - start `2026-07-01T13:37:47.856+09:00` / stop `2026-07-01T13:40:41.212+09:00`.
- [x] **Slack received** — 3 receptions confirmed (Portfolio Daily Bot channel):
  - 07:50 pre-market Slack.
  - 08:24 approval-required Slack.
  - Step 12~17 success Slack.
- [x] **DB after-check passed** (empty-state confirmation):
  - 2026-07-01 `strategy_execution_order` 0 / REQUESTED strategy orders 0.
  - active `connector_order_request` 0 / today's `connector_order_request` 0.
  - Step 12~17 NO_TARGET safe-termination judgment.
- [x] **DB after-check passed** (stale refresh confirmation — latest balance snapshot):
  - `connector_balance_snapshot id=321` / `as_of_date=2026-07-01`.
  - `total_eval_amount=8,706,505` / `cash_balance=8,706,505` / `eval_profit=0`.
- [x] **Step 12~17 Scheduler ENABLED**:
  - `portfolio-paper-daily-step12-17-order-0901-kst` DISABLED → ENABLED transition complete / State `ENABLED`.
  - LastModificationDate `2026-07-01T13:53:57.160+09:00`.
  - cron `cron(1 9 ? * MON-FRI *)` / Asia/Seoul / FlexibleTimeWindow OFF.
  - Target Lambda `portfolio-paper-daily-scheduler-dispatcher` / Target Input `{"scheduleType":"STEP12_17_ORDER","dryRun":false}`.
- [x] **Full Daily automation lineup checked** — all 7 Schedulers confirmed ENABLED.
  - [x] 07:50 EC2 start — `portfolio-paper-ec2-start-0750-kst`.
  - [x] 07:50 pre-market Slack — `portfolio-daily-brief-morning-slack-0750-kst` / eventType `MORNING_BRIEF`.
  - [x] 08:00 Step 1~11 — `portfolio-paper-daily-step1-11-approval-0800-kst` / dryRun false.
  - [x] **09:01 Step 12~17 — `portfolio-paper-daily-step12-17-order-0901-kst` / dryRun false / ENABLED on this date**.
  - [x] 09:10~15:50 10-minute intraday stop-loss — `portfolio-paper-intraday-snapshot-evaluate-10min-kst` / cron `cron(10/10 9-15 ? * MON-FRI *)`.
  - [x] 15:50 post-market Slack — `portfolio-daily-brief-evening-slack-1550-kst` / eventType `EVENING_BRIEF`.
  - [x] 15:50 MarketConnector stop — `portfolio-paper-marketconnector-stop-1550-kst`.
- [x] **No active connector order** — active `connector_order_request` 0 (the 6 stale 2026-04-27 ACCEPTED are stale candidates separated from the 2026-07-01 latest-balance-reference-date judgment / follow-up cleanup).
- [x] **No today connector order** — 2026-07-01 `connector_order_request` 0 new rows / 0 broker order submissions / 0 new `connector_fill`.
- [x] **Latest balance snapshot 2026-07-01** — `connector_balance_snapshot id=321` / `as_of_date=2026-07-01` / `total_eval_amount=8,706,505` / `cash_balance=8,706,505` / `eval_profit=0`.
- [x] **No aws-live policy change** — **This change is limited to aws-paper. It does not change the aws-live automatic BUY / SELL policy, and live retains the candidate + manual-approval-first policy.** (OD-SAFE-002 / OD-SAFE-003 alignment)
- [x] **Kiro documentation edits only** — 0 AWS CLI / boto3 / psql / Spring Boot / external API executions / 0 broker order submissions / 0 Kiro-side changes to AWS resource creation · modification · deletion on this date (operator direct-execution area) / 0 secret raw-value records.

### Remaining validation (after the next business day / follow-up phase)

- [ ] Live observation of the next business day's 09:01 automatic execution — Scheduler invocation log · Dispatcher Lambda CloudWatch Logs · Step Functions execution creation · Slack reception · DB after-check alignment.
- [ ] Automatic order submission · fill · balance refresh · Slack confirmation on a day with candidates — this date had no candidate (NO_TARGET) / first validation on a real candidate round.
- [ ] Clean up stale `connector_position_snapshot` (4 items on 2026-06-23) or supplement the query for latest-balance-reference-date judgment.
- [ ] Clean up the 6 stale 2026-04-27 삼성전자 ACCEPTED `connector_order_request` (responsibility of the 03 · 04 spec follow-up phases).
- [ ] Re-review the aws-live automation policy in a separate cutover phase (responsibility of the 10 spec follow-up phase).
