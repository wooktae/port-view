# AWS 월 비용 시뮬레이션 — PORT-STRATEGY-AI AWS Migration

본 문서는 PORT-STRATEGY-AI 8개 MS의 AWS Migration 전체 비용 판단 참고 문서다. `01-aws-migration-foundation`에서 정의한 목표 구조를 출발점으로 dev / paper / live 3개 환경의 **월 예상 비용**을 산정하고, 후속 spec(02 ~ 10)에서도 비용 결정 자료로 공통 참조한다. 실제 결제가 아니라 의사결정 보조용 추정이며, 실제 AWS 리소스 생성, IaC 생성, 8개 MS 코드 / 문서 수정은 본 작업 범위가 아니다.

## 1. 가정과 단서

- 리전: 서울 ap-northeast-2 (1순위). 가격이 불확실한 항목은 "AWS Pricing Calculator 확인 필요"로 표기한다.
- 통화: USD 기준. KRW 환산은 운영자 결정 placeholder.
- 실제 secret 값은 본 문서에 출력하지 않는다. 모두 `[REDACTED]`로 표기한다.
- 단가는 2025~2026년 시점의 일반 공개 가격을 참고한 근사치다. 본 spec 승인 후 IaC 작성 시 AWS Pricing Calculator로 재검증해야 한다.
- 컴퓨트 단가는 On-Demand 기준. Reserved Instance / Savings Plans 적용 시 EC2 / RDS / Fargate 비용은 1년 기준 약 30~50% 절감 가능. 본 시뮬레이션에는 반영하지 않는다.
- 본 작업은 문서 작성만 수행한다. 실제 AWS 리소스 생성, 비용 결제, 컨소시엄 견적은 본 작업 범위 밖이다.

## 2. 단가 가정 (서울 ap-northeast-2)

값은 USD/월(730시간 기준) 또는 USD/단위. 모두 "AWS Pricing Calculator 확인 필요" 단서가 붙는다.

### 컴퓨트

- EC2 On-Demand (Linux)
  - `t4g.small` (2 vCPU / 2 GB): ~$15/월
  - `t4g.medium` (2 vCPU / 4 GB): ~$30/월
  - `t3.small` (2 vCPU / 2 GB, x86): ~$19/월
  - `t3.medium` (2 vCPU / 4 GB, x86): ~$38/월
  - `m6i.large` (2 vCPU / 8 GB): ~$90/월
- ECS Fargate
  - vCPU: ~$0.05056 / vCPU-hour
  - 메모리: ~$0.00553 / GB-hour
  - 예: 0.5 vCPU + 1 GB Task가 730시간 상시 실행 → ~$22.5/월
  - 예: 0.5 vCPU + 1 GB Task가 일 30분 실행(월 ~15시간) → ~$0.5/월
- AWS Batch (Fargate compute env): Fargate 단가와 동일.

### 네트워크

- EIP: instance에 attach되어 있고 instance가 running이면 무료. detach 또는 stop 상태에서는 ~$3.6/월/EIP.
- NAT Gateway: ~$0.059/시간 → ~$43/월 + 데이터 처리 ~$0.059/GB.
- ALB: ~$0.0225/시간 → ~$16.5/월 + LCU 사용량(저트래픽이면 +$1~$5/월).
- VPC Endpoint (Interface): ~$0.011/시간/AZ → AZ당 ~$8/월 + 데이터 처리.
- VPC Endpoint (Gateway, S3 / DynamoDB): 무료.
- 데이터 전송: 같은 AZ 내부 무료. 다른 AZ ~$0.01/GB. 인터넷 outbound ~$0.114/GB(처음 10TB까지).

### 데이터

- RDS PostgreSQL On-Demand
  - `db.t4g.micro` (2 vCPU / 1 GB): single-AZ ~$13/월, multi-AZ ~$26/월
  - `db.t4g.small` (2 vCPU / 2 GB): single-AZ ~$26/월, multi-AZ ~$52/월
  - `db.t4g.medium` (2 vCPU / 4 GB): single-AZ ~$52/월, multi-AZ ~$104/월
  - `db.m6g.large` (2 vCPU / 8 GB): single-AZ ~$165/월, multi-AZ ~$330/월
- RDS storage (gp3): ~$0.131/GB-월
- RDS backup storage: DB 크기까지 무료, 초과분 ~$0.105/GB-월
- S3 Standard storage: ~$0.025/GB-월 (서울)

### 이미지 / Secret / 관측

- ECR storage: ~$0.10/GB-월. 같은 region pull은 무료.
- Secrets Manager: $0.40/secret/월 + $0.05 / 10,000 API calls.
- SSM Parameter Store
  - Standard: 무료(파라미터 10,000개까지).
  - Advanced: $0.05/파라미터/월.
- CloudWatch Logs: ingestion ~$0.76/GB, storage ~$0.04/GB-월.
- CloudWatch custom metrics: ~$0.30/metric/월.
- CloudWatch Alarms: standard ~$0.10/alarm/월.

### Orchestration

- Step Functions Standard: ~$0.025 / 1,000 state transitions.
- EventBridge Scheduler: 월 14M invocations까지 무료, 이후 $1.00/M.
- Lambda: 월 1M requests + 400,000 GB-sec 무료. 이후 $0.20/M requests + ~$0.0000166667/GB-sec.

## 3. MS별 자원 가정

dev / paper / live 환경별로 사용량 가정을 다르게 둔다. 모든 시나리오는 design.md의 "MS별 컴퓨트 후보 비교 및 권고"와 정합된다.

### 3.1 컴퓨트 자원 가정

- `port-marketconnector` (EC2 1순위)
  - dev: EC2 미상시. paper / live에서만 항상 on. dev에서는 ECS Fargate Task로 broker mock만 호출하거나 미배포.
  - paper: `t4g.small` 24/7
  - live: `t3.medium` 24/7
- `port-view` (ECS Fargate)
  - dev: 0.25 vCPU / 0.5 GB, 일 8시간 실행 가정
  - paper: 0.5 vCPU / 1 GB, 24/7
  - live: 1 vCPU / 2 GB, 24/7
  - **[2026-06-30 (오후) ECS Fargate 1차 포팅 실증 메모]** — 본 일자 운영자 직접 수행한 ECS Fargate Public IP 1차 포팅에서 실제 운영된 task 사양은 launch type `FARGATE` / awsvpc / cpu 512 / memory 1024 / Spring profile `aws-paper` / Tomcat 8080 / container port 8080(0.5 vCPU / 1 GB 가정 정합). 다만 **본 일자 운영 방식은 24/7 상시 desiredCount 1 이 아니라 검증 시점만 desiredCount 1, 검증 후 desiredCount 0 종료 정책**(R-AUTO-033 [2026-06-30 오후 보강] / OD-MS-002 evidence). 본 1차 포팅 단계에서는 24/7 상시 운영 가정(`paper: 0.5 vCPU / 1 GB, 24/7`) 보다 실제 사용량은 더 낮을 수 있으며(예: 일 1~2시간 운영 + 검증 시점만 활성), 정식 24/7 운영 진입 전까지는 본 가정을 보수적 상한으로 유지. **추가 비용 항목 1차 실증** — (a) ALB 미사용 + NAT Gateway 미사용으로 4.2 paper 표의 `ALB(선택)` 행과 `NAT Gateway` 행 항목은 본 1차 포팅 단계에서 0 으로 산정 가능, (b) **Public IPv4 비용 추가** — Fargate task 가 `assignPublicIp=ENABLED` 로 public subnet 에 직접 배치되므로 `$0.005/IPv4-시간` 단가가 desiredCount 1 유지 시간에 비례해 부과됨 / 본 일자 검증 시점만 desiredCount 1 가정 시 누적 시간 미미, (c) CloudWatch Logs retention 7일 / 본 1차 포팅 단계 log 발생량은 4.2 paper 표의 CloudWatch Logs 가정(20 GB) 보다 훨씬 낮음. **본 1차 포팅 단계 후속 — AWS Pricing Calculator 재확인** — 4.2 paper 표의 port-view 24/7 가정(0.5 vCPU / 1 GB) 은 그대로 유지하되 desiredCount 0/1 수동 운영 운영 회차가 누적된 후 실측 시간 가중으로 재산정 / 24/7 정식 상시 운영 진입 시점에 본 표 갱신 / ALB / HTTPS / Route53 / Cloudflare Tunnel 도입 시점에 4.2 paper 표 `ALB(선택)` 행에 실측 반영 / multi-AZ / Auto Scaling / Blue/Green 도입 시점에 4.3 live 표에 cross-spec audit. 본 단계 비용 평문 인용(실제 영수증 / Billing dashboard 본문) 0건 / 운영자가 AWS Pricing Calculator 로 재산정 시점에 본 표 surgical edit.
- `port-interest-crawler` (ECS Fargate Task, 스케줄)
  - dev: 0.5 vCPU / 1 GB × 30분 / 일 → 월 ~15시간
  - paper: 0.5 vCPU / 1 GB × 60분 / 일 → 월 ~30시간
  - live: 1 vCPU / 2 GB × 60분 / 일 → 월 ~30시간 + history backfill 별도
- `port-interest-preprocessor` (ECS Fargate Task, 스케줄)
  - dev: 0.5 vCPU / 1 GB × 30분 / 일 → 월 ~15시간
  - paper: 0.5 vCPU / 1 GB × 60분 / 일 → 월 ~30시간
  - live: 1 vCPU / 2 GB × 60분 / 일 → 월 ~30시간
- `port_strategy_common`: 컴퓨트 비용 없음(packaging).
- `port_strategy_decision` (ECS Fargate Task, 스케줄 1~2회/일)
  - dev: 0.25 vCPU / 0.5 GB × 30분 → 월 ~15시간
  - paper: 0.5 vCPU / 1 GB × 30분 × 2회 → 월 ~30시간
  - live: 1 vCPU / 2 GB × 30분 × 2회 → 월 ~30시간
- `port_strategy_execution` (ECS Fargate Task)
  - dev: 0.25 vCPU / 0.5 GB × 30분/일 → 월 ~15시간
  - paper: 0.5 vCPU / 1 GB × 60분/일(BUY/SELL/sync) + intraday polling Step Functions(거래일 6시간 × 0.25 vCPU 가정) → 월 ~150시간 합산
  - live: 1 vCPU / 2 GB 일일 + intraday polling 동일 → 월 ~150~180시간 합산
- `port_strategy_research` (AWS Batch / Fargate, 비정기)
  - dev: 1 vCPU / 2 GB × 1시간 × 월 5회 → 월 ~5시간
  - paper: 2 vCPU / 4 GB × 2시간 × 월 10회 → 월 ~20시간
  - live: 2 vCPU / 4 GB × 2시간 × 월 10회 → 월 ~20시간(live와 paper backtest 부하 거의 동일)

### 3.2 데이터 자원 가정

- RDS for PostgreSQL 단일 인스턴스 / 환경
  - dev: `db.t4g.micro` single-AZ, storage 20 GB, backup 7일.
  - paper: `db.t4g.small` single-AZ, storage 50 GB, backup 7일.
  - live: `db.t4g.medium` 또는 `db.m6g.large` multi-AZ, storage 100~200 GB, backup 14일, PITR on.
- RDS는 schema-per-domain 단일 DB이므로 환경당 1 인스턴스만 존재.

### 3.3 네트워크 / 관측 / 기타 자원 가정

- VPC: 환경별 분리 권고지만 비용은 VPC 자체보다 NAT / ALB / Endpoint에 집중.
- NAT Gateway: 1개/환경(시나리오 NAT-on). NAT-off 시나리오에서는 0개 + VPC Endpoints 사용.
- ALB: live는 internal ALB 1개. dev / paper는 ECS Service Discovery로 대체 권고.
- VPC Endpoints: S3 / ECR(api+dkr) / Secrets / SSM / CloudWatch Logs (interface endpoint × AZ 수).
- ECR: 8개 MS × 평균 1 GB 이미지 × 5 태그 정도 → ~5 GB / 환경.
- Secrets Manager: 환경당 ~10개 secret 가정(KIS app key / app secret / base URL / 계좌번호 / 계좌 상품 코드 / access token / DB password / Slack webhook / Naver client secret / 기타). 모두 `[REDACTED]`.
- SSM Parameter Store: standard tier만 사용 가정. 비용 0.
- CloudWatch
  - Logs ingestion: dev ~5 GB/월, paper ~20 GB/월, live ~50 GB/월
  - Custom metrics: dev 5개, paper 20개, live 50개
  - Alarms: dev 5개, paper 20개, live 50개
- Step Functions: paper/live 일 ~10 step × 거래일 21일 → 월 ~250 transitions.
- EventBridge Scheduler: 월 invocations 1,000~10,000 수준 → 무료 한도 안.
- Lambda: 인프라 알람 fan-out 용도만 가정. 무료 한도 안.
- **[2026-06-30 (오후) 장중 손절 Slack 실 연동 비용 메모]** — 같은 일자 오후 추가 작업분으로 장중 손절 Slack 실 연동이 완료되었으나 비용 관점에서 신규 상시 자원 추가는 없음. (a) Notifier Lambda `portfolio-event-notifier` 의 호출 진입점이 MarketConnector EC2 runner 까지 확대되었지만 Lambda 자체는 기존(2026-06-23 Slack notifier 1차 검증 시점) 그대로 사용 / Lambda 호출 횟수는 hard stop 조건 충족 회차 한정으로 매우 저빈도(평일 장중 10분 간격 evaluate × 평일 21일 = 월 약 2,520 호출 / Lambda free tier 안). (b) Step Functions 측 transitions 변경 없음 — `portfolio-paper-intraday-stop-sell-approval` 자체는 본 일자에도 변경 없음. (c) EventBridge Scheduler 변경 없음 — 기존 10분 Snapshot Refresh Scheduler(OD-MS-035 정합) 재사용. (d) EC2 MarketConnector 는 기존 EC2 + EIP 재사용 / 추가 EC2 / EBS / EIP 0건. (e) IAM Role inline policy 추가는 무료. (f) Slack webhook outbound 데이터 매우 작음(1건당 수 KB 한정). 합계로 본 일자 오후 추가 작업분의 월 비용 영향은 **AWS free tier 안 또는 1달러 미만**으로 사실상 0에 수렴. ALB / NAT Gateway / RDS multi-AZ / VPC Endpoints / Fargate 비용과 무관 / port-marketconnector EC2 비용 / Daily Batch state machine 비용 / EC2 lifecycle Scheduler 비용 모두 그대로 유지. 정확한 금액은 AWS Pricing Calculator 재확인 필요(본 문서 2장 단가 가정 기반 추정).
- **[2026-06-30 (오후) Daily Brief Slack 자동화 비용 메모]** — Daily Brief Slack 알림은 Lambda 2개(Builder `portfolio-daily-brief-slack-summary-builder` + Notifier `portfolio-event-notifier` / 후자는 기존 Approval / Daily Execution Slack 알림과 공유) + mini Step Functions 1개(`portfolio-daily-brief-slack-notification` / 구조 `BuildDailyBriefPayload → SendSlackNotifier` / Standard transitions 매우 작음 / 2 state × 평일 2회 = 월 약 88 transitions / Standard 단가 ~$0.025 / 1,000 transitions 기준 월 비용 약 $0.002) + EventBridge Scheduler 2개(`portfolio-daily-brief-morning-slack-0750-kst` + `portfolio-daily-brief-evening-slack-1550-kst` / 평일 2회 = 월 약 44 invocations / 14M 무료 한도 안 / 추가 비용 0) + IAM Role 2개(무료) + Secrets Manager `GetSecretValue` 호출(평일 2회 = 월 약 44회 / 단가 $0.05/10,000 calls / 비용 0에 가까움) + Slack webhook outbound 데이터(메시지 1건 수 KB / 누적 매우 작음). 합계로 Daily Brief Slack 자동화 자체의 월 비용은 모두 **AWS free tier 한도 안 또는 1달러 미만**으로 사실상 0에 수렴. Fargate · ALB · NAT Gateway · RDS multi-AZ · VPC Endpoints 비용과 무관한 **lightweight automation** 이며, port-view ECS Fargate Service 비용 / Daily Batch state machine 비용 / EC2 lifecycle Scheduler 비용을 모두 그대로 유지(OD-MS-038 신규 / R-AUTO-035 신규 mitigation 정합). 정확한 금액은 AWS Pricing Calculator 재확인 필요(본 문서 2장 단가 가정 기반 추정).
- S3: access_token 백업 + research report + 일반 dump 합산 dev 1 GB / paper 5 GB / live 20 GB 가정.

## 4. 환경별 월 비용 시나리오

각 셀은 USD/월. 단가는 위 2장 기준. 시나리오는 low / realistic / high.

low: 자원 가정 하한값, 데이터 전송 / Logs ingestion 최소.
realistic: 본 spec 권고치 그대로.
high: 사용량 1.5~2배, 추가 backfill / 장애 재실행 반영.

### 4.1 dev 환경

| 항목 | low | realistic | high |
|------|-----|-----------|------|
| EC2 (marketconnector 미배포 가정) | 0 | 0 | 15 |
| EIP | 0 | 0 | 4 |
| EBS gp3 (운영 EC2 옵션) | 0 | 0 | 2 |
| ECS Fargate (port-view 8h/일) | 4 | 6 | 9 |
| ECS Fargate (crawler / preprocessor / decision / execution 합산) | 5 | 8 | 12 |
| AWS Batch / Fargate (research) | 0 | 1 | 3 |
| RDS db.t4g.micro single-AZ | 13 | 13 | 15 |
| RDS storage 20 GB gp3 | 3 | 3 | 4 |
| RDS backup 추가 storage | 0 | 0 | 1 |
| NAT Gateway (1개) | 0 | 43 | 50 |
| NAT 데이터 처리 | 0 | 1 | 3 |
| ALB | 0 | 0 | 0 |
| VPC Endpoints | 0 | 8 | 16 |
| CloudWatch Logs (5 GB) | 4 | 4 | 6 |
| CloudWatch Metrics (5개) | 2 | 2 | 3 |
| CloudWatch Alarms (5개) | 1 | 1 | 1 |
| Secrets Manager (10개) | 4 | 4 | 5 |
| SSM Parameter Store (standard) | 0 | 0 | 0 |
| ECR (5 GB) | 1 | 1 | 1 |
| Step Functions | 0 | 0 | 0 |
| EventBridge Scheduler | 0 | 0 | 0 |
| Lambda | 0 | 0 | 0 |
| S3 backup (1 GB + 요청) | 0 | 1 | 1 |
| **합계 (USD/월)** | **약 37** | **약 96** | **약 151** |

비고: dev에서 NAT를 끄고 모든 Fargate Task를 public subnet + assignPublicIp=ENABLED로 두면 NAT Gateway 비용을 0으로 만들 수 있다. NAT 비교는 5장 참고.

### 4.2 paper 환경

| 항목 | low | realistic | high |
|------|-----|-----------|------|
| EC2 marketconnector (`t4g.small` 24/7) | 14 | 15 | 17 |
| EIP (attach 상태) | 0 | 0 | 0 |
| EBS gp3 (20 GB) | 2 | 2 | 3 |
| ECS Fargate port-view (0.5 vCPU / 1 GB 24/7) | 20 | 23 | 28 |
| ECS Fargate crawler / preprocessor 합산 | 5 | 7 | 10 |
| ECS Fargate decision | 3 | 4 | 6 |
| ECS Fargate execution + intraday | 18 | 25 | 35 |
| AWS Batch research | 4 | 6 | 10 |
| RDS db.t4g.small single-AZ | 26 | 26 | 30 |
| RDS storage 50 GB gp3 | 7 | 7 | 9 |
| RDS backup 추가 storage | 1 | 2 | 4 |
| NAT Gateway (1개) | 43 | 43 | 50 |
| NAT 데이터 처리 | 2 | 5 | 12 |
| ALB (선택) | 0 | 0 | 17 |
| VPC Endpoints | 8 | 16 | 24 |
| CloudWatch Logs (20 GB) | 15 | 16 | 22 |
| CloudWatch Metrics (20개) | 6 | 6 | 9 |
| CloudWatch Alarms (20개) | 2 | 2 | 3 |
| Secrets Manager (10개) | 4 | 5 | 6 |
| SSM Parameter Store | 0 | 0 | 1 |
| ECR (5 GB) | 1 | 1 | 1 |
| Step Functions | 0 | 0 | 1 |
| EventBridge Scheduler | 0 | 0 | 0 |
| Lambda | 0 | 0 | 1 |
| S3 backup (5 GB + 요청) | 1 | 1 | 2 |
| **합계 (USD/월)** | **약 182** | **약 212** | **약 301** |

비고: paper 환경의 intraday monitor가 거래일에만 동작하므로 high 시나리오는 backfill / 장애 재실행 반영.

### 4.3 live 환경

| 항목 | low | realistic | high |
|------|-----|-----------|------|
| EC2 marketconnector (`t3.medium` 24/7) | 35 | 38 | 42 |
| EIP (attach 상태) | 0 | 0 | 0 |
| EBS gp3 (30 GB) | 3 | 3 | 4 |
| ECS Fargate port-view (1 vCPU / 2 GB 24/7) | 40 | 45 | 55 |
| ECS Fargate crawler / preprocessor (1 vCPU / 2 GB 합산) | 8 | 12 | 18 |
| ECS Fargate decision | 5 | 7 | 10 |
| ECS Fargate execution + intraday | 25 | 35 | 55 |
| AWS Batch research | 4 | 6 | 12 |
| RDS db.t4g.medium multi-AZ | 100 | 104 | 110 |
| RDS storage 100 GB gp3 multi-AZ | 26 | 26 | 30 |
| RDS backup 추가 storage (14일 보존) | 5 | 8 | 15 |
| NAT Gateway (multi-AZ 권고: 2개) | 86 | 86 | 100 |
| NAT 데이터 처리 | 5 | 12 | 30 |
| ALB internal (1개) | 16 | 17 | 22 |
| VPC Endpoints (multi-AZ) | 16 | 24 | 40 |
| CloudWatch Logs (50 GB) | 36 | 40 | 55 |
| CloudWatch Metrics (50개) | 12 | 15 | 22 |
| CloudWatch Alarms (50개) | 4 | 5 | 7 |
| Secrets Manager (10개) | 4 | 5 | 7 |
| SSM Parameter Store | 0 | 0 | 1 |
| ECR (5~10 GB) | 1 | 1 | 2 |
| Step Functions | 0 | 1 | 2 |
| EventBridge Scheduler | 0 | 0 | 0 |
| Lambda | 0 | 0 | 1 |
| S3 backup (20 GB + 요청) | 1 | 2 | 4 |
| **합계 (USD/월)** | **약 432** | **약 492** | **약 643** |

비고: live는 RDS multi-AZ + NAT multi-AZ + multi-AZ Endpoint가 비용을 끌어올린다. RDS를 `db.m6g.large` multi-AZ로 올리면 합계가 ~$650~$830/월로 증가.

### 4.4 3개 환경 합산 (dev + paper + live)

| 시나리오 | 합계 USD/월 |
|---------|------------|
| low | 약 651 |
| realistic | 약 800 |
| high | 약 1,095 |

세 환경을 동시에 운영하지 않고 dev를 야간 / 주말에 끄거나 cutover 진행 중 dev → paper → live 순서로 단계 운영하면 합계는 추가 절감 가능.

## 5. NAT Gateway 사용 vs 미사용 비교

NAT Gateway는 paper / live 환경 비용에서 가장 큰 고정비 항목 중 하나다. 본 절은 NAT 사용 여부에 따른 비용 차이를 별도로 정리한다.

### 5.1 옵션 비교

| 옵션 | 설명 | 보안 / 운영 | 월 고정비 (paper 1개 NAT 가정) |
|------|------|-------------|-------------------------------|
| A. NAT Gateway 1개 | 단일 AZ NAT, 모든 outbound 통합 | AWS 관리. 가장 단순. | ~$43 + 데이터 처리 |
| B. NAT Gateway 2개 (multi-AZ) | live 권고. AZ 장애 격리 | HA. live 안전. | ~$86 + 데이터 처리 |
| C. NAT Instance (`t4g.nano`) | NAT를 자체 EC2로 운영 | 직접 패치 / 모니터링. SPOF. | ~$3.5 + EBS + 데이터 |
| D. NAT 미사용 + Fargate public subnet | Task에 public IP 할당, IGW 직접 사용 | 보안 그룹으로만 inbound 차단. 감사 관점에서 보수적이지 않음. | ~$0 (public IP 비용 0~소액) |
| E. NAT 미사용 + 인터넷 outbound 필요 시에만 EC2(public + EIP) | 마켓커넥터 / 크롤러만 public, 나머지는 VPC endpoint로만 | 인터넷 outbound 가능 워크로드를 명시적으로 제한 | ~$0 (NAT 자체 비용) |

### 5.2 시나리오별 절감 효과

paper 환경 realistic 기준 합계 ~$212에서 NAT 항목(NAT Gateway $43 + 데이터 처리 $5)을 제거하면 NAT-off 절감은 ~$48/월. live 환경에서 multi-AZ NAT 2개를 1개로 축소하면 ~$43/월 절감, NAT 완전 제거 시 ~$98/월 절감 가능.

### 5.3 권고

- live: NAT Gateway 2개(multi-AZ) 유지. 운영 안정성과 감사 관점에서 권고.
- paper: NAT Gateway 1개 또는 NAT Instance(`t4g.nano`) 1개. 비용 민감하면 D / E 후보 검토 가능.
- dev: D 옵션(Fargate public subnet) 권고. NAT 비용 0.

데이터 전송 비용(NAT 데이터 처리)은 마켓커넥터 broker 호출 / 크롤러 외부 페이지 다운로드량에 비례. 본 시뮬레이션은 paper 월 ~10 GB, live 월 ~50 GB 가정.

## 6. 두 가지 마이그레이션 권고안

### 6.1 권고안 A — 최소 비용 paper 검증형

목적: AWS 이전 위험을 최소 비용으로 검증한다. live cutover 전까지의 paper 검증 단계에 적용.

핵심 결정

- dev: 비활성. 또는 단일 ECS cluster 안에 namespace로만 두고 사용 시점에만 켠다.
- paper: NAT Gateway 미사용. Fargate Task는 public subnet + assignPublicIp=ENABLED. 마켓커넥터 EC2 + EIP는 public subnet에 직접 배치.
- live: 본 권고안에 포함하지 않음. live는 권고안 B 적용.
- RDS: paper는 `db.t4g.small` single-AZ. backup 7일.
- ALB 미사용. ECS Service Discovery로 내부 통신.
- Step Functions / EventBridge / Lambda 무료 한도 안에서만 사용.
- VPC Endpoints는 ECR / S3 / Secrets / SSM / CloudWatch Logs 중 ECR과 S3만 우선 활성화(나머지는 NAT-off 전제에서 인터넷 경유).

paper 환경 합계 추정: 약 $130~$170 / 월 (NAT 제거로 realistic 대비 ~$48 절감).

리스크

- Fargate Task가 public subnet에서 동작 → 보안 그룹 / IAM 검증을 더 엄격하게 해야 한다.
- NAT 미사용이므로 인터넷 outbound 필요 워크로드를 명시적으로 식별해야 한다(크롤러, 마켓커넥터).
- live 안정성과는 별개 권고이므로 권고안 B로 cutover 전 최소 N영업일 검증 필요.
- KRX Windows worker(paper, 2026-06-12 ~ 2026-06-16 도입) 비용 메모: Autologon / Windows Scheduled Task / SSM RunCommand 자체 비용은 사실상 0이다. 실제 비용 영향은 Windows EC2 running 시간 + EIP(필요 시) + EBS storage + CloudWatch Logs 저장량으로 발생한다. KRX program / shortsell daily 수집은 영업일 단위 짧은 실행이므로 EC2 running 시간을 줄이려면 작업 완료 후 stop 절차(08 spec task 59 후속)로 idle 시간을 최소화한다. EC2 stop 후에도 EIP는 분 단위 과금이 발생할 수 있어 운영자 결정으로 detach 여부 검토 가능. Autologon은 paper 전용 보안 예외(OD-MS-022 / R-SEC-009)로 운영하며, 추후 전용 local user 전환 / 또는 aws-live 단계 도입 시 별도 결정 책임.

적용 대상 stage: design.md 로드맵의 Stage 2~4(파일럿 + 마켓커넥터 + 전략 paper).

### 6.2 권고안 B — 운영 안정성 우선형

목적: live cutover와 사후 안정 운영을 위한 표준 구조. 비용보다 가용성 / 보안 / 감사를 우선한다.

핵심 결정

- dev: 항상 켜두지 않음. 일과 시간만 / 필요 시점에만 운영.
- paper: NAT Gateway 1개. 본 시뮬레이션 4.2 realistic 기준 그대로.
- live: NAT Gateway 2개(multi-AZ), RDS `db.t4g.medium` 또는 `db.m6g.large` multi-AZ, internal ALB 1개, multi-AZ VPC Endpoints, CloudWatch Alarms 50개 이상.
- 마켓커넥터: EC2 `t3.medium` + EIP. broker IP 등록 EIP 기준.
- 모든 secret은 Secrets Manager. SSM Parameter Store는 환경 의존 비밀번호 외 값에만.
- RDS PITR + 14일 backup retention.
- 인프라 알람: SNS → Lambda → Slack webhook(`port-view`의 `SlackNotificationService`와는 별도 채널 권고).

비용 합계 추정

- paper: 약 $212 / 월 (4.2 realistic).
- live: 약 $492 / 월 (4.3 realistic). RDS를 `db.m6g.large` multi-AZ로 올리면 $650~$700 / 월.
- 3개 환경 합산 realistic: 약 $800 / 월.

리스크

- NAT Gateway 2개 + multi-AZ Endpoints + multi-AZ RDS가 비용의 대부분을 차지한다. 운영 부담을 줄이는 대신 비용을 받아들이는 구조다.
- Reserved Instance / Savings Plans 적용 시 EC2 / RDS / Fargate에서 약 30~50% 추가 절감 가능. 본 시뮬레이션에는 미반영.

적용 대상 stage: design.md 로드맵의 Stage 5~6(port-view ECS + Daily Batch + live cutover).

## 7. 비용 모니터링과 후속 검증

- AWS Pricing Calculator로 본 추정값을 spec 승인 후 IaC 작성 시점에 재산정한다.
- AWS Budgets 또는 Cost Explorer로 환경 / 서비스 단위 월 한도 알림을 설정한다(threshold는 운영자 결정 placeholder).
- 첫 1~2개월은 매주 Cost Explorer로 NAT Gateway 데이터 처리, CloudWatch Logs ingestion, RDS storage 증가율을 점검한다.
- Reserved Instance / Savings Plans 검토는 live 안정 운영 3개월 이후 실데이터 기반으로 진행한다.
- 본 spec 안의 모든 추정은 공개 가격 기준 근사치다. 실제 청구 금액은 인터넷 데이터 전송량, 거래일 수, 장애 재실행 빈도에 따라 변동한다.

## 8. 안전 제약

- 본 작업은 문서 작성만 수행한다. 실제 AWS 리소스 생성 / IaC 생성 / 8개 MS 코드 / README / AGENTS.md / docs / CHANGELOG / worklog 수정은 하지 않는다.
- 실제 secret 값은 본 문서에 포함하지 않는다. 모두 `[REDACTED]`로 표기한다.
- 본 문서의 단가는 모두 "AWS Pricing Calculator 확인 필요" 단서 하에 사용한다.
- 8개 MS의 어떤 entrypoint도 실행하지 않았으며, broker / KIS / Selenium / Naver / yfinance / RDS / DDL / DML / 주문 / 체결 / 크롤링 / 외부 API 호출은 본 작업에 포함되지 않는다.
