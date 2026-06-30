# Runbook — 05-port-view-ecs-and-runbook

본 문서는 port-view ECS Fargate Public IP 1차 포팅 운영 절차를 정리한 runbook 이다. 정식 운영 procedure 의 1차 기준 문서로 본 spec 의 `operation-notes.md` 의 2026-06-30 (오후) 본문(`3. ECS Fargate 포팅: 완료`) 사실 기록을 단일 기준으로 한다. 본 runbook 은 운영자가 직접 수행하는 절차이고, Kiro 는 본 spec 작업공간에서 실제 명령을 실행하지 않는다.

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
- 모든 명령 예시에서 실제 ARN / account-id / public IP / image digest full sha256 / task ARN / ENI ID / RDS endpoint hostname / Slack webhook URL / 계좌번호 / KIS credential / DB password / 실제 state machine ARN 은 평문 기록하지 않는다(`[REDACTED]` / `[REDACTED_ACCOUNT_NO]` / `[REDACTED_PUBLIC_IP]` / `[REDACTED_ARN]` / `[REDACTED_TASK_ARN]` / `[REDACTED_SECRET_ARN]` / `[REDACTED_BROKER_ORDER_NO]`).
- 본 runbook 은 ECS RunTask / SubmitJob / KIS API / Slack Webhook / Daily Batch 자동 trigger 를 직접 실행하지 않는다. 모든 실 실행은 운영자 책임.

## 절차 1. ECS View 기동(desiredCount 1)

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
