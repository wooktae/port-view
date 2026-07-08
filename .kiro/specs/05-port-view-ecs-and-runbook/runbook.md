# Runbook — 05-port-view-ecs-and-runbook

본 문서는 port-view ECS Fargate Public IP 1차 포팅 운영 절차를 정리한 runbook 이다.

- 단일 기준 = 본 spec `operation-notes.md` 의 2026-06-30 (오후) 본문(`3. ECS Fargate 포팅: 완료`) 사실 기록.
- 실행 주체 = 운영자 직접 수행 / Kiro 는 본 spec 작업공간에서 실제 명령을 실행하지 않는다.

## 적용 범위

- 1차 적용 환경: `aws-paper`
- region: `ap-northeast-2`
- 1차 적용 대상: port-view(Spring Boot / Thymeleaf 운영 콘솔)
- 본 runbook 은 ALB 미사용 + public subnet + `assignPublicIp=ENABLED` + 운영자 IP/32 SG inbound + CloudWatch Logs retention 7일 1차 포팅 방식만 다룬다.
- ALB / HTTPS / Route53 / Cloudflare Tunnel / 인증 · 인가 / Auto Scaling / Blue-Green / multi-AZ 운영은 1차 포팅 이후로 보류(05 / 06 / 07 / 10 spec 후속 phase 책임).

## 운영 정책 / 안전 기준

- ECS service `portfolio-view-service` 의 `desiredCount` 는 운영 시점에만 1, 검증 후 비용 절감을 위해 0 으로 종료한다.
- Fargate task 재기동 시 public IP 가 변경되므로, 운영자는 매 기동 직후 public IP 를 자동 조회 후 브라우저 URL 을 갱신한다.
- Security Group `sgroup-port-view-ecs` inbound 는 TCP 8080 / source 운영자 공인 IP/32 만 허용한다. 0.0.0.0/0 전체 오픈 금지.
- 본 runbook 의 모든 AWS CLI 명령 예시는 `.kiro/AGENTS.md` 의 "운영 명령 작성 규칙(추가)" 3종(`list/describe → 변수 추출 → 후속 검증` / `information_schema.columns` 사전 확인 / 실패 명령 뒤 SUCCESS marker 금지) 정합으로 작성한다.
- 모든 명령 예시에서 아래 값은 평문 기록하지 않는다:
  - 실제 ARN / account-id / public IP / image digest full sha256 / task ARN / ENI ID.
  - RDS endpoint hostname / Slack webhook URL / 계좌번호 / KIS credential / DB password / 실제 state machine ARN.
- 사용 placeholder = `[REDACTED]` / `[REDACTED_ACCOUNT_NO]` / `[REDACTED_PUBLIC_IP]` / `[REDACTED_ARN]` / `[REDACTED_TASK_ARN]` / `[REDACTED_SECRET_ARN]` / `[REDACTED_BROKER_ORDER_NO]`.
- 본 runbook 은 ECS RunTask / SubmitJob / KIS API / Slack Webhook / Daily Batch 자동 trigger 를 직접 실행하지 않는다. 모든 실 실행은 운영자 책임.

## 절차 1. ECS View 기동(desiredCount 1)

**목적** — 검증 / 운영 시점에 ECS Fargate task 를 기동하고 브라우저 접속 URL 을 확보한다.

**전제**:

- ECS cluster `portfolio-paper-cluster` / ECS service `portfolio-view-service` / task definition `portfolio-view:2` 등록 상태.
- Security Group `sgroup-port-view-ecs` inbound = TCP 8080 / 운영자 IP/32 설정 완료.

**실행 / 검증**:

 1) ECS service desiredCount 1 전환
   (1) 명령 패턴
       - `aws ecs update-service --cluster portfolio-paper-cluster --service portfolio-view-service --desired-count 1`
       - 명령 출력에서 `desiredCount` 가 1 로 갱신됐는지 확인
   (2) 안정 상태 대기
       - `aws ecs wait services-stable --cluster portfolio-paper-cluster --services portfolio-view-service`
       - 또는 `aws ecs describe-services --cluster portfolio-paper-cluster --services portfolio-view-service --query 'services[0].deployments'` 의 `rolloutState=COMPLETED` 확인
   (3) RUNNING 확인
       - `aws ecs list-tasks --cluster portfolio-paper-cluster --service-name portfolio-view-service --desired-status RUNNING`
       - 결과에 task ARN 1건 이상 노출 확인
 2) public IP 자동 조회 (규칙 1 정합)
   (1) TASK_ARN 추출
       - `aws ecs list-tasks --cluster portfolio-paper-cluster --service-name portfolio-view-service --desired-status RUNNING --query 'taskArns[0]' --output text` → 결과를 `TASK_ARN` 변수에 저장
   (2) ENI_ID 추출
       - `aws ecs describe-tasks --cluster portfolio-paper-cluster --tasks $TASK_ARN --query "tasks[0].attachments[0].details[?name=='networkInterfaceId'].value" --output text` → 결과를 `ENI_ID` 변수에 저장
   (3) PUBLIC_IP 추출
       - `aws ec2 describe-network-interfaces --network-interface-ids $ENI_ID --query 'NetworkInterfaces[0].Association.PublicIp' --output text` → 결과를 `PUBLIC_IP` 변수에 저장
   (4) 브라우저 접속 URL 출력
       - `http://$PUBLIC_IP:8080` 출력(public IP 평문 기록 금지 / `[REDACTED_PUBLIC_IP]` placeholder 정합)
       - 출력된 URL 에서 Dashboard / Balance / Positions / Orders / Reports / Daily 화면 정상 표시 확인
 3) Spring Boot started 확인
   (1) CloudWatch Logs 자동 조회
       - `aws logs describe-log-streams --log-group-name /ecs/portfolio-view --order-by LastEventTime --descending --limit 1 --query 'logStreams[0].logStreamName' --output text` → `LOG_STREAM` 변수에 저장
       - `aws logs get-log-events --log-group-name /ecs/portfolio-view --log-stream-name $LOG_STREAM --limit 50` 출력 본문에서 Spring Boot started / HikariPool RDS connection 성공 확인
   (2) raw log 본문 평문 기록 금지(R-DOCS-001 정합) — 운영자 노트에는 "Spring Boot started 확인" 사실만 기록

## 절차 2. ECS View 종료(desiredCount 0)

**목적** — 검증 완료 후 Fargate task 를 종료해 Fargate 시간 단가 + public IPv4 비용 누적을 차단한다.

**전제** — ECS service `portfolio-view-service` 가 desiredCount 1 상태에서 RUNNING 중.

**실행 / 검증**:

 1) ECS service desiredCount 0 전환
   (1) 명령 패턴
       - `aws ecs update-service --cluster portfolio-paper-cluster --service portfolio-view-service --desired-count 0`
       - 출력에서 `desiredCount=0` 확인
   (2) task 종료 확인
       - `aws ecs list-tasks --cluster portfolio-paper-cluster --service-name portfolio-view-service --desired-status RUNNING --query 'taskArns'` 결과가 빈 배열(`[]`) 로 전환 확인
       - Fargate task 종료 / public IP 해제 / Fargate 비용 누적 중단
 2) 다음 기동 시 주의
   (1) public IP 재발급
       - `desiredCount 0 → 1` 전환 시 새로운 public IP 가 발급된다. 운영자는 절차 1.2 의 public IP 자동 조회를 다시 수행해 브라우저 URL 을 갱신한다.
   (2) Security Group inbound rule 유지
       - `sgroup-port-view-ecs` 의 운영자 IP/32 inbound 는 그대로 유지(절차 4 참조).

## 절차 3. ECS View 에서 AWS Step Functions Step 12~17 승인 실행

**목적** — ECS View 안 Daily Batch 화면에서 Step 12~17 approval workflow 를 승인 실행하고 후검증까지 완료한다.

**전제**:

- 절차 1 로 ECS View 기동 완료 / public IP 접속 성공 / RDS connection 성공.
- `paperOrderEnabled=true` + approval range gate 통과 상태.
- preflight 4종 0건 조건 유지 (아래 실행 순서 참조).

**실행 / 검증 / rollback**:

 1) 진입 전 DB preflight
   (1) DB 컬럼명 확인(규칙 2 정합)
       - `information_schema.columns` 로 `strategy_execution_order` / `connector_order_request` / `connector_balance_snapshot` / `connector_position_snapshot` 의 실제 컬럼명을 먼저 확인.
       - 특히 `connector_position_snapshot` 은 `balance_snapshot_id` 컬럼이 없으므로 `account_no` + `as_of_date` 기준 검증으로 작성.
   (2) preflight 4종 0건 확인
       - REQUESTED `strategy_execution_order` 잔여 0건
       - retryable rejected `strategy_execution_order` 0건
       - active `connector_order_request` 0건
       - 본 일자 신규 `connector_order_request` 0건
   (3) preflight 실패 시 즉시 중단
       - 0건 조건 중 하나라도 위반되면 Step 12~17 승인 실행을 중단하고 운영자 점검 진행. 실패 SQL 뒤 SUCCESS marker 출력 금지(규칙 3 정합).
 2) 화면에서 승인 실행 클릭
   (1) Daily Batch 화면 진입
       - `http://$PUBLIC_IP:8080/daily-batch`
       - 화면 표시: `Execution ON` / `Local File OFF` / `Full Pipeline ON` / `Paper Order ON` / 허용 범위 `1~17` / AWS Step 12~17 승인 실행 버튼 활성
   (2) 승인 실행 버튼 클릭
       - `POST /daily-batch/aws-stepfunctions/start-approval-range`
       - 응답 flash message 에서 `executionName` + redaction 처리된 `executionArn` 확인
       - state machine `portfolio-paper-daily-step12-17-approval` 실행 시작
 3) 실행 결과 확인
   (1) Step Functions 상태 자동 조회
       - 응답에서 받은 `executionName` 을 변수로 사용해 `aws stepfunctions list-executions --state-machine-arn <REDACTED_ARN> --status-filter SUCCEEDED` 또는 `describe-execution` 으로 status 확인
       - status `SUCCEEDED` / 마지막 state `ExecutionSucceeded` 확인
   (2) Slack 수신 확인
       - 운영자 Slack 채널에서 `DAILY_EXECUTION_SUCCESS` 이벤트 메시지 확인(메시지 본문 평문 기록 금지)
   (3) DB after-check (규칙 2 정합)
       - 신규 `connector_order_request` 0건
       - REQUESTED `strategy_execution_order` 잔여 0건
       - active `connector_order_request` 0건
       - 최신 `connector_balance_snapshot` 의 `as_of_date` · `total_eval_amount` · `cash_balance` 확인
       - 보유 종목 검증은 `connector_position_snapshot` 의 `account_no` + `as_of_date` 기준(추정 컬럼명 사용 금지)

## 절차 4. 운영자 IP 변경 시 Security Group inbound 갱신

 1) 신규 운영자 공인 IP 확인
   (1) 운영자가 외부 IP 확인 도구로 `NEW_OPERATOR_IP` 확인
   (2) `[REDACTED_PUBLIC_IP]` placeholder 사용 — 본 runbook 평문 기록 금지
 2) 기존 inbound rule 삭제
   (1) `aws ec2 describe-security-groups --group-ids <SG_ID_PLACEHOLDER> --query 'SecurityGroups[0].IpPermissions'` 로 기존 rule 확인
   (2) 운영자 직접 console 또는 CLI 로 기존 운영자 IP/32 rule revoke
 3) 신규 inbound rule 추가
   (1) TCP 8080 + source `NEW_OPERATOR_IP/32` 한정 add
   (2) 0.0.0.0/0 전체 오픈 금지(R-AUTO-033 mitigation 정합)
 4) 접속 검증
   (1) 절차 1.2 의 public IP 자동 조회 결과 URL 에서 화면 정상 표시 확인
   (2) 화면 표시 실패 시 SG inbound 적용 여부 / 운영자 IP 변경 여부 / Fargate task RUNNING 여부 순서로 점검

## 절차 5. 운영 중 장애 / 이상 대응

 1) ECS task RUNNING 이지만 화면 접속 실패
   (1) public IP 재조회(절차 1.2)
   (2) SG inbound rule 점검(절차 4)
   (3) Fargate task health check 로그 점검(CloudWatch Logs `/ecs/portfolio-view`)
 2) Spring Boot started 후 화면 일부 데이터 누락
   (1) RDS 접속 점검(`information_schema.columns` 로 컬럼 부재 여부 사전 확인)
   (2) HikariPool 로그 점검(raw 본문 인용 금지)
   (3) 기본 계좌번호 env(`PORTFOLIO_BATCH_DEFAULT_ACCOUNT_NO` / `PORTFOLIO_VIEW_ACCOUNT_DEFAULT_ACCOUNT_NO`) 누락 여부 점검
 3) Step Functions execution status 가 `FAILED` 인 경우
   (1) 본 runbook 절차 3 의 preflight 0건 위반 여부 재점검
   (2) `aws stepfunctions get-execution-history` 의 마지막 실패 state 확인(history 본문 평문 기록 금지)
   (3) 후속 Daily Batch 자동 trigger 진행 전 운영자 별도 승인 진입(OD-SAFE-002 / OD-SAFE-003 / OD-MS-033 09:01 보류 정책 정합)

## 보안 / 비용 정책

- desiredCount 1 은 운영 시점에만 유지 / 검증 후 0 종료(R-AUTO-033 [2026-06-30 오후 보강] / 비용 관점에서 Fargate 시간 단가 + public IPv4 비용 누적 차단).
- SG inbound 0.0.0.0/0 전체 오픈 금지(절차 4 참조 / R-AUTO-034 mitigation 정합).
- task 재시작 후 새 public IP 발급 / 운영자 매 기동 시 재조회(절차 1.2 / 본 runbook 평문 기록 0건 정합).
- ALB · HTTPS · Route53 · Cloudflare Tunnel · 인증 · 인가 · multi-AZ · Auto Scaling · Blue/Green 은 1차 포팅 이후 보류 / 향후 도입 시 별도 phase 책임(05 / 06 / 07 / 10 spec).
- AWS CLI / boto3 / psql / Spring Boot 실 실행은 운영자 직접 수행 책임 / Kiro 는 본 runbook 의 명령을 실행하지 않는다.

## 참조

- 본 runbook 의 단일 기준 문서: [`./operation-notes.md`](./operation-notes.md) 의 2026-06-30 (오후) "3. ECS Fargate 포팅: 완료" block
- 검증 체크리스트: [`./validation-checklist.md`](./validation-checklist.md)
- 결정 / 리스크: [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-MS-002 / OD-MS-009 / OD-MS-037 / [`../_common/risk-register.md`](../_common/risk-register.md) R-AUTO-033 / R-AUTO-034
- 운영 명령 작성 규칙: [`../../AGENTS.md`](../../AGENTS.md) "운영 명령 작성 규칙(추가)" 3종

## Step 12~17 Scheduler enable/disable 운영 절차 (2026-07-01 추가)

**목적** — aws-paper Step 12~17 Scheduler `portfolio-paper-daily-step12-17-order-0901-kst` 의 ENABLED / DISABLED 전환 절차.

**전제**:

- 2026-07-01 부터 본 Scheduler 는 ENABLED 상태로 평일 09:01 KST 자동 실행을 담당한다.
- **본 변경은 aws-paper 에 한정** — aws-live 자동 BUY / SELL 정책은 변경하지 않으며 live 는 후보 + 수동 승인 우선 정책 유지 (OD-SAFE-002 / OD-SAFE-003 정합).

### 사전 원칙

- AWS CLI 명령은 `list/describe → 변수 추출 → 후속 검증` 패턴을 따른다(AGENTS.md 운영 명령 작성 규칙 1).
- 실제 ARN / account-id 12자리 원문 / Lambda ARN / state machine ARN / Slack webhook URL / secret 평문은 본 문서 · 운영자 노트 · git commit message 에 기록하지 않는다(R-DOCS-001 정합 / 필요 시 `[REDACTED]` 계열 placeholder).
- 실패한 SQL / 명령 뒤에 SUCCESS marker 를 출력하지 않는다(AGENTS.md 운영 명령 작성 규칙 3).
- Scheduler 이름 · cron · Asia/Seoul · Target Input · state machine 이름 · Dispatcher Lambda 이름 등 운영 식별자는 secret 이 아니므로 본 문서에 사실로 기록한다.

### 1) 현재 상태 확인 (list-schedules → get-schedule)

Scheduler 이름은 사전에 결정되어 있다 (`portfolio-paper-daily-step12-17-order-0901-kst`). 먼저 `list-schedules` 로 존재 여부를 확인한 뒤, `get-schedule` 로 State · cron · Timezone · FlexibleTimeWindow · Target Lambda · Target Input 정합을 확인한다.

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

- 응답 예시(2026-07-01 ENABLE 전환 후) — `State=ENABLED` / `Cron=cron(1 9 ? * MON-FRI *)` / `TZ=Asia/Seoul` / `Flexible=OFF` / `Input={"scheduleType":"STEP12_17_ORDER","dryRun":false}`.
- Target Lambda ARN 은 응답에 그대로 노출되므로 화면 캡처 · 노트 공유 시 `[REDACTED_ARN]` 마스킹 필요.

### 2) Target.Input 정합 확인

`Target.Input` JSON 안에 `scheduleType=STEP12_17_ORDER` + `dryRun=false` 정합 확인. Target.Input 이 변경된 경우 Dispatcher Lambda 가 잘못된 state machine 을 트리거할 수 있으므로 ENABLE 전환 전에 반드시 재확인한다.

- `scheduleType=STEP12_17_ORDER` 이어야 Dispatcher Lambda 가 `portfolio-paper-daily-step12-17-approval` state machine 을 트리거한다(OD-MS-032 정합).
- `dryRun=false` 여야 실제 broker 주문 흐름까지 진행 가능. `dryRun=true` 상태에서는 자동 실행이 dry-run 수준까지만 진행(broker 주문 제출 없음).
- `scheduleType=STEP1_11_APPROVAL` 또는 다른 값이 들어있으면 Dispatcher Lambda 가 잘못된 state machine 을 트리거할 수 있으므로 즉시 원복.

### 3) DISABLED → ENABLED 전환

ENABLED 전환은 `update-schedule` 로 `--state ENABLED` 를 명시한다. `update-schedule` 은 기존 필드를 유지하지 않고 새로 지정된 값으로 replace 하므로 반드시 `get-schedule` 로 기존 값 백업 후 동일 필드 재지정한다.

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

- 2026-07-01 본 절차 적용 후 응답 정합 = `State=ENABLED` / LastModificationDate `2026-07-01T13:53:57.160+09:00`.
- **새 start-execution 중복 생성 주의**:
  - ENABLED 전환 직후 수동 `start-execution` 을 함께 발사하면 09:01 KST 자동 실행과 중복될 수 있다.
  - 수동 검증이 필요한 경우 09:01 자동 실행과 시간이 겹치지 않는 시간대에 실행하거나 09:01 회차 완료 후 실행한다.
  - 중복 broker 주문 위험 = R-AUTO-001 / R-BROKER-004 정합.

### 4) ENABLED → DISABLED rollback

잘못된 상태에서 자동 실행이 진행 중이거나 안전 감지 실패 시 즉시 DISABLED 로 전환한다.

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

- DISABLED 전환 후 진행 중인 Step Functions execution 은 즉시 stop 하지 않고 계속 진행된다. 진행 중 execution 을 중단하려면 별도로 `aws stepfunctions stop-execution` 을 호출한다(R-AUTO-037 rollback 정합).
- rollback 후 port-view 안 Daily Batch AWS Step 12~17 승인 실행 버튼 또는 Local View wrapper 로 수동 재실행 fallback 가능.

### 5) 전체 Daily 라인업 7종 상태 확인

2026-07-01 이후 aws-paper Daily 자동화 라인업 7종을 한 번에 확인한다. 하나라도 State 가 예상과 다르면 즉시 원인 audit.

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

- 2026-07-01 통과 시점의 예상 결과 = 7종 모두 `State=ENABLED` / `Timezone=Asia/Seoul` / `FlexibleTimeWindow=OFF` / cron 은 각 Scheduler 정의값 그대로.
- 7종 라인업 중 어느 하나라도 `State=DISABLED` 또는 응답 실패 시 즉시 원인 audit — Dispatcher Lambda IAM Role · Scheduler Group · CloudWatch Logs · Step Functions state machine ACTIVE 여부 · Target Input JSON 정합.

### 6) 안전 제약

- 본 절차는 aws-paper 한정 — aws-live cutover phase(10 spec 후속 phase) 진입 전까지 aws-live 자동 BUY / SELL 정책 변경 없음(OD-SAFE-002 / OD-SAFE-003 정합).
- ENABLED / DISABLED 전환 시 실제 ARN / account-id 12자리 원문 / Lambda ARN / state machine ARN / Slack webhook URL / secret 평문은 본 문서 · 운영자 노트 · git commit message 에 기록하지 않는다.
- ENABLED 전환 후 첫 09:01 KST 자동 실행 회차는 다음 영업일 실전 관찰:
  - Scheduler invocation log / Dispatcher Lambda CloudWatch Logs.
  - Step Functions execution 생성 / Slack 수신 / DB after-check 정합 audit.
  - followups-overview 2026-07-01 후속 메모 정합.
- 자동 재시도 금지 정책(OD-SAFE-004 / R-AUTO-001) 은 그대로 유지 — Step 12~17 Scheduler ENABLED 후에도 Step Functions state machine 안 BUY / SELL / fill sync / position 변경 계열 state 의 Retry block 부재 정책 유지.
