# Validation Checklist — 05-port-view-ecs-and-runbook

본 문서는 port-view ECS Fargate Public IP 1차 포팅 + ECS View → AWS Step Functions Step 12~17 승인 실행 검증 체크리스트다.

- 단일 기준 = 본 spec [`./operation-notes.md`](./operation-notes.md) 2026-06-30 (오후) "3. ECS Fargate 포팅: 완료" block.
- 사용 조건 = [`./runbook.md`](./runbook.md) 절차 1 ~ 절차 3 진행 후 항목 단위 점검.

## Dashboard (2026-06-30 오후 통과 상태 요약)

| 카테고리 | 항목 수 | 통과 (완료) | 최근 통과 회차 |
|---|---|---|---|
| Deploy (Docker · ECR · TaskDefinition · ECS Service) | 4 | 4 | 2026-06-30 오후 |
| Runtime (Spring Boot / RDS) | 2 | 2 | 2026-06-30 오후 |
| View 화면 조회 (Dashboard / Balance / Positions / Orders / Reports / Daily) | 1 | 1 | 2026-06-30 오후 |
| Daily Batch UI gate (Step 1~11 · Step 12~17 승인 버튼) | 2 | 2 | 2026-06-30 오후 |
| Step Functions 실행 + Slack 수신 | 2 | 2 | 2026-06-30 오후 |
| DB after-check + desiredCount 0 종료 | 2 | 2 | 2026-06-30 오후 |

## 적용 범위 / 안전 정책

- 1차 적용 환경 = `aws-paper` / region = `ap-northeast-2` / 대상 = port-view.
- 대상 포팅 방식 = ALB 미사용 + public subnet + `assignPublicIp=ENABLED` + 운영자 IP/32 SG inbound + CloudWatch Logs retention 7일 1차 포팅.
- 보류 항목 (1차 포팅 이후 / 05 · 06 · 07 · 10 spec 후속 phase 책임):
  - ALB / HTTPS / Route53 / Cloudflare Tunnel.
  - 인증 · 인가 / Auto Scaling / Blue-Green / multi-AZ.
- 명령 작성 규칙 = [`../../AGENTS.md`](../../AGENTS.md) "운영 명령 작성 규칙(추가)" 3종 정합:
  - `list/describe → 변수 추출 → 후속 검증`.
  - `information_schema.columns` 사전 확인.
  - 실패 명령 뒤 SUCCESS marker 금지.
- 실행 주체 = 운영자 직접 / Kiro 는 본 spec 작업공간에서 실행하지 않는다.
- 평문 기록 0건 (`[REDACTED]` 또는 placeholder):
  - 비밀번호 / token / API key / 계좌번호 12자리 원문 / RDS password / Slack webhook URL.
  - 실제 ARN / public IP / image digest full sha256 / task ARN / ENI ID / broker_order_no 원문.

## 13개 체크 항목 / 본 일자 통과 결과

본 체크리스트의 13개 항목은 본 spec 의 [`./operation-notes.md`](./operation-notes.md) 2026-06-30 (오후) "3. ECS Fargate 포팅: 완료" block 의 "검증 체크리스트" 와 동일하다. 본 일자 통과 결과는 사실 기록 / 운영자 직접 검증한 항목만 "완료" 로 표시한다.

 1) Docker image build: 완료
   (1) 운영자 직접 빌드
       - 확인 대상: image tag / digest 생성 사실 (image digest full sha256 본 노트 평문 기록 0건 / `[REDACTED]` placeholder)
       - 통과 기준: Docker build 결과 exit code 0 / 빌드 로그에 에러 없음
 2) ECR push: 완료
   (1) ECR repository `portfolio-view` 에 image push
       - 확인 대상: ECR repository 이름 `portfolio-view` / push 결과 사실 (image digest 평문 기록 0건)
       - 통과 기준: `aws ecr describe-images --repository-name portfolio-view --query 'imageDetails[0].imagePushedAt' --output text` 의 결과 timestamp 가 본 일자 push 시점과 일치
 3) task definition registration: 완료
   (1) `portfolio-view:1` → `portfolio-view:2` 보정
       - 확인 대상: revision 1 → 2 / 기본 계좌번호 env 누락 보정(`PORTFOLIO_BATCH_DEFAULT_ACCOUNT_NO` + `PORTFOLIO_VIEW_ACCOUNT_DEFAULT_ACCOUNT_NO`)
       - 통과 기준: `aws ecs describe-task-definition --task-definition portfolio-view --query 'taskDefinition.revision' --output text` 결과 = `2`
 4) ECS service `portfolio-view-service` RUNNING: 완료
   (1) desiredCount 1 + RUNNING task ARN 1건 이상
       - 확인 대상: ECS cluster `portfolio-paper-cluster` / ECS service `portfolio-view-service`
       - 통과 기준: `aws ecs list-tasks --cluster portfolio-paper-cluster --service-name portfolio-view-service --desired-status RUNNING --query 'taskArns'` 의 배열 길이 ≥ 1
 5) CloudWatch Logs `/ecs/portfolio-view` 에서 Spring Boot started: 완료
   (1) 최근 log stream 에서 Spring Boot started 확인
       - 확인 대상: log group `/ecs/portfolio-view` / 최근 log stream
       - 통과 기준: 운영자 노트에 "Spring Boot started 확인" 사실 기록(raw log 본문 평문 인용 0건 / R-DOCS-001 정합)
 6) RDS connection success(HikariPool start completed): 완료
   (1) HikariPool 시작 완료 + Spring Boot RDS 연결
       - 확인 대상: log group `/ecs/portfolio-view` / Spring profile `aws-paper` / default schema `ops` / HikariPool start completed
       - 통과 기준: 운영자 노트에 "HikariPool RDS connection 성공 / default schema `ops` 확인" 사실 기록 / RDS endpoint hostname / DB password 평문 기록 0건
 7) Dashboard / Balance / Positions / Orders / Reports / Daily 화면 조회: 완료
   (1) 6종 화면 정상 표시
       - 확인 대상: `http://[REDACTED_PUBLIC_IP]:8080/dashboard` / `/balance-summary` / `/positions` / `/orders` / `/strategy/reports/latest` / `/daily-batch`
       - 통과 기준: 6종 화면 모두 정상 렌더링 / 기본 계좌번호 정상 반영 / 화면 캡처는 본 노트에 첨부하지 않는다(민감정보 노출 회피)
 8) AWS Step 1~11 버튼 표시: 완료
   (1) 안전 gate 조건 정합
       - 확인 대상: Daily Batch 화면 안 AWS Step 1~11 버튼 활성 조건(`fullPipelineExecutionEnabled=true` 상태에서도 Step 1~11 버튼 조건 충돌 없음)
       - 통과 기준: 화면에 AWS Step 1~11 버튼 표시 / 본 일자 자동 실행은 별도 EventBridge Scheduler 경로 유지(본 체크리스트 범위 밖)
 9) AWS Step 12~17 승인 버튼 표시: 완료
   (1) 안전 gate 조건 정합
       - 확인 대상: Daily Batch 화면 안 AWS Step 12~17 승인 버튼 활성 조건(`paperOrderEnabled=true` + approval range gate 통과)
       - 통과 기준: 화면에 AWS Step 12~17 승인 버튼 표시 / `fullPipelineExecutionEnabled=true` 상태에서도 충돌 없이 활성
 10) ECS View → Step 12~17 execution `SUCCEEDED`: 완료
   (1) 운영자 직접 승인 클릭 후 정상 종료
       - executionName: `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8`
       - state machine: `portfolio-paper-daily-step12-17-approval`
       - status: `SUCCEEDED`
       - start: `2026-06-30T14:15:42.899+09:00`
       - stop: `2026-06-30T14:18:48.358+09:00`
       - 최종 state: `ExecutionSucceeded`
       - 실제 state machine ARN 본 노트 평문 기록 0건(`[REDACTED_ARN]` placeholder)
 11) Slack `DAILY_EXECUTION_SUCCESS` 수신: 완료
   (1) 운영자 Slack 채널 수신 확인
       - 확인 대상: Slack 이벤트 `DAILY_EXECUTION_SUCCESS`
       - 통과 기준: 운영자 Slack 채널에서 본 일자 14:18 KST 무렵 수신 사실 확인 / Slack 메시지 본문 / webhook URL 평문 기록 0건
 12) DB after-check 0건 확인: 완료
   (1) preflight 4종 + balance snapshot 회수
       - REQUESTED `strategy_execution_order` after: 0
       - retryable rejected `strategy_execution_order` after: 0
       - active `connector_order_request` after: 0
       - today connector orders after: 0 rows
       - 최신 `connector_balance_snapshot id=281` / `as_of_date=2026-06-30`
       - `total_eval_amount=8,706,505` / `cash_balance=8,706,505` / `eval_profit=0`
       - `source_version=connector-intraday-snapshot-refresh-1.0.0`
       - 보유 종목 검증은 `connector_position_snapshot` 의 `account_no` + `as_of_date` 기준(컬럼 `balance_snapshot_id` 부재 / 규칙 2 정합)
       - 과거 stale `connector_order_request` 6건 식별 사실(2026-04-27 ACCEPTED 잔여 / 본 일자 실행과 무관 / 후속 cleanup 후보 / preflight count 오염 위험은 후속 후보)
 13) desiredCount 0 종료: 완료
   (1) Fargate task 종료 / public IP 해제
       - 확인 대상: ECS service `portfolio-view-service` desiredCount 0 / running task ARN 0건
       - 통과 기준: `aws ecs list-tasks --cluster portfolio-paper-cluster --service-name portfolio-view-service --desired-status RUNNING --query 'taskArns'` 결과가 빈 배열(`[]`)
       - 다음 기동 시 새 public IP 발급 전제 / SG inbound 운영자 IP/32 유지

## 결정 / 리스크 매핑

- OD-MS-002(port-view 컴퓨트 = ECS Fargate Service 1순위 / 🟢 확정) — 본 일자 본 체크리스트 1) ~ 13) 통과로 1차 실증 evidence 보강(결정 본문 변경 없음).
- OD-MS-009 (Daily Batch orchestration = Step Functions + EventBridge Scheduler + ECS RunTask) — 본 체크리스트 10) ECS View → Step Functions Step 12~17 approval execution `SUCCEEDED` 통과로 1차 실증 evidence 보강 (결정 본문 변경 없음).
- OD-MS-037(View Local AWS Paper read-only 1차 scope + ECS / Fargate 진입 전 batch 2차 검증 선행 정책) — 본 체크리스트 10) 통과는 OD-MS-037 의 후속 phase 인 ECS / Fargate 진입 후 첫 실증으로 evidence 보강(결정 본문 변경 없음).
- R-AUTO-033 [2026-06-30 오후 보강] — 본 체크리스트 4) · 7) · 9) · 10) · 11) · 12) · 13) 통과로 Public IP direct access + 운영자 IP/32 SG + ECS View → Step 12~17 approval 3차 실증 / Status `Mitigated` 유지.
- R-AUTO-034 [2026-06-30 오후 보강]:
  - 본 체크리스트 10) 통과로 Fargate ECS task role(`portfolio-paper-view-task-role`) 의 `states:StartExecution` 권한이 Step 12~17 approval state machine ARN 한정 부여 1차 실증.
  - Status `Open` 유지 (향후 ALB · HTTPS · CloudWatch alarms 도입 시 cross-spec audit / 06 spec 후속 phase 책임).
- 본 체크리스트 12) 의 stale `connector_order_request` 6건 식별 사실은 신규 후속 후보(preflight count 오염 위험 / cleanup 결정) 로 followups-overview 2026-06-30 (오후) 후속 메모에 기록.

## 본 일자 사실 기록 범위 (2026-06-30 오후)

- 본 체크리스트 13개 항목은 모두 운영자가 직접 검증 / Kiro 측 자동 검증 0건.
- AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 본 일자 Kiro 측 변경 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 본 일자 Kiro 측 변경 0건.
- broker / KIS 호출 = ECS View → Step 12~17 승인 실행 1건(`SUCCEEDED` / NO_TARGET / broker 주문 제출 0건) + balance refresh 한정 / 추가 BUY · SELL · 취소 · 정정 0건 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건.
- 민감정보 평문 기록 0건 — 모두 placeholder:
  - `[REDACTED]` / `[REDACTED_ACCOUNT_NO]` / `[REDACTED_PUBLIC_IP]`.
  - `[REDACTED_ARN]` / `[REDACTED_TASK_ARN]` / `[REDACTED_SECRET_ARN]` / `[REDACTED_BROKER_ORDER_NO]`.

## 참조

- 단일 기준 문서: [`./operation-notes.md`](./operation-notes.md) 의 2026-06-30 (오후) "3. ECS Fargate 포팅: 완료" block
- 운영 절차: [`./runbook.md`](./runbook.md)
- 결정 / 리스크 매핑: [`../_common/operator-decisions.md`](../_common/operator-decisions.md) / [`../_common/risk-register.md`](../_common/risk-register.md)
- 운영 명령 작성 규칙: [`../../AGENTS.md`](../../AGENTS.md) "운영 명령 작성 규칙(추가)" 3종

## 2026-07-01 검증 결과 — paper Daily 자동화 1차 풀 ON + Step 12~17 Scheduler ENABLED

본 검증은 aws-paper 한정. aws-live 정책 변경 없음(OD-SAFE-002 / OD-SAFE-003 정합).

- [x] **Step 12~17 manual execution `SUCCEEDED`**:
  - executionName `port-manual-daily-step12-17-20260701-043747` / state machine `portfolio-paper-daily-step12-17-approval`.
  - status `SUCCEEDED` / execution history `ExecutionSucceeded`.
  - start `2026-07-01T13:37:47.856+09:00` / stop `2026-07-01T13:40:41.212+09:00`.
- [x] **Slack received** — 3종 수신 확인 (Portfolio Daily Bot 채널):
  - 07:50 장전 Slack.
  - 08:24 승인 필요 Slack.
  - Step 12~17 성공 Slack.
- [x] **DB after-check passed** (empty-state 확인):
  - 2026-07-01 `strategy_execution_order` 0건 / REQUESTED 전략 주문 0건.
  - active `connector_order_request` 0건 / 오늘 `connector_order_request` 0건.
  - Step 12~17 NO_TARGET 안전 종료 판정.
- [x] **DB after-check passed** (stale refresh 확인 — 최신 balance snapshot):
  - `connector_balance_snapshot id=321` / `as_of_date=2026-07-01`.
  - `total_eval_amount=8,706,505` / `cash_balance=8,706,505` / `eval_profit=0`.
- [x] **Step 12~17 Scheduler ENABLED**:
  - `portfolio-paper-daily-step12-17-order-0901-kst` DISABLED → ENABLED 전환 완료 / State `ENABLED`.
  - LastModificationDate `2026-07-01T13:53:57.160+09:00`.
  - cron `cron(1 9 ? * MON-FRI *)` / Asia/Seoul / FlexibleTimeWindow OFF.
  - Target Lambda `portfolio-paper-daily-scheduler-dispatcher` / Target Input `{"scheduleType":"STEP12_17_ORDER","dryRun":false}`.
- [x] **Full Daily automation lineup checked** — 7종 Scheduler 모두 ENABLED 확인.
  - [x] 07:50 EC2 start — `portfolio-paper-ec2-start-0750-kst`.
  - [x] 07:50 장전 Slack — `portfolio-daily-brief-morning-slack-0750-kst` / eventType `MORNING_BRIEF`.
  - [x] 08:00 Step 1~11 — `portfolio-paper-daily-step1-11-approval-0800-kst` / dryRun false.
  - [x] **09:01 Step 12~17 — `portfolio-paper-daily-step12-17-order-0901-kst` / dryRun false / 본 일자 ENABLED**.
  - [x] 09:10~15:50 10분 장중 손절 — `portfolio-paper-intraday-snapshot-evaluate-10min-kst` / cron `cron(10/10 9-15 ? * MON-FRI *)`.
  - [x] 15:50 장후 Slack — `portfolio-daily-brief-evening-slack-1550-kst` / eventType `EVENING_BRIEF`.
  - [x] 15:50 MarketConnector stop — `portfolio-paper-marketconnector-stop-1550-kst`.
- [x] **No active connector order** — active `connector_order_request` 0건(2026-04-27 stale ACCEPTED 6건은 최신 balance 기준일 2026-07-01 판정과 분리된 stale 후보 / 후속 cleanup).
- [x] **No today connector order** — 2026-07-01 `connector_order_request` 신규 row 0건 / broker 주문 제출 0건 / `connector_fill` 신규 0건.
- [x] **Latest balance snapshot 2026-07-01** — `connector_balance_snapshot id=321` / `as_of_date=2026-07-01` / `total_eval_amount=8,706,505` / `cash_balance=8,706,505` / `eval_profit=0`.
- [x] **aws-live 정책 변경 없음** — **본 변경은 aws-paper 에 한정된다. aws-live 자동 BUY / SELL 정책은 변경하지 않으며, live 는 후보 + 수동 승인 우선 정책을 유지한다.**(OD-SAFE-002 / OD-SAFE-003 정합)
- [x] **Kiro 문서 수정만** — AWS CLI / boto3 / psql / Spring Boot / 외부 API 실행 0건 / broker 주문 제출 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 본 일자 Kiro 측 변경 0건(운영자 직접 수행 영역) / secret 원문 기록 0건.

### 남은 검증 (다음 영업일 이후 / 후속 phase)

- [ ] 다음 영업일 09:01 자동 실행 실전 관찰 — Scheduler invocation log · Dispatcher Lambda CloudWatch Logs · Step Functions execution 생성 · Slack 수신 · DB after-check 정합.
- [ ] 후보 있는 날 자동 주문 제출 · 체결 · balance refresh · Slack 확인 — 본 일자는 후보 없음(NO_TARGET) / 실 후보 회차 첫 검증.
- [ ] stale `connector_position_snapshot`(2026-06-23 4건) 정리 또는 최신 balance 기준 판정 쿼리 보완.
- [ ] 2026-04-27 삼성전자 stale ACCEPTED `connector_order_request` 6건 cleanup(03 · 04 spec 후속 phase 책임).
- [ ] aws-live 자동화 정책 별도 cutover phase 재검토(10 spec 후속 phase 책임).
