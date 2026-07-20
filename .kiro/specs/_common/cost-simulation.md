# AWS 월 비용 시뮬레이션 — PORT-STRATEGY-AI AWS Migration

## Purpose

본 문서는 PORT-STRATEGY-AI 8개 MS 의 AWS Migration 전체 비용 판단 참고 문서다. `01-aws-migration-foundation` 에서 정의한 목표 구조를 출발점으로 `local-dev` · `aws-paper` · `aws-live` 3개 환경의 **월 예상 비용** 을 산정한다. 후속 spec(02 ~ 10) 에서도 비용 결정 자료로 공통 참조한다.

실제 결제가 아니라 의사결정 보조용 추정이며, 실제 AWS 리소스 생성 · IaC 생성 · 8개 MS 코드 · 문서 수정은 본 작업 범위가 아니다. 모든 수치는 서울 `ap-northeast-2` 기준 근사치이며 **AWS Pricing Calculator 확인 필요** 단서가 붙는다. 본 9차 회차는 큰 구조 변경 · 신규 비용 수치 추가를 회피하는 최소 변경 회차다.

## Cost Dashboard

운영자 매일 조회용 월 비용 요약. 모든 수치는 서울 `ap-northeast-2` 기준 근사치 · **AWS Pricing Calculator 확인 필요**. 상세 단가 · 자원 가정 · 시나리오 비교는 아래 chapter 2 ~ 6 참조.

> **모델링 vs 실측 구분** — 본 Dashboard 및 chapter 4 의 `paper realistic ~212 USD` · `live realistic ~492 USD` 등의 수치는 **NAT Gateway 사용 · multi-AZ Endpoint 등을 포함한 모델링 시나리오** 값이다. 현재 aws-paper 운영은 **NAT Gateway 미사용 · VPC Endpoint 비용 중심 구조**이므로 실측 청구액과는 다르다. 2026 년 6 월 실제 청구액 · cost driver · 절감 실행 내역은 아래 `2026-07-01 Actual Cost Analysis (June 2026)` 섹션을 참조한다.

### paper 월 예상 비용 (realistic 시나리오)

**약 212 USD/월** (chapter 4.2 realistic 합계). AWS Pricing Calculator 확인 필요.

주요 구성(USD/월 근사치):

| 카테고리 | 항목 | 금액 |
|---|---|---|
| Compute | EC2 marketconnector | 15 |
| Compute | ECS Fargate port-view | 23 |
| Compute | Fargate execution + intraday | 25 |
| Compute | AWS Batch research | 6 |
| Database | RDS db.t4g.small single-AZ | 26 |
| Database | RDS storage | 7 |
| Network | NAT Gateway | 43 |
| Network | NAT 데이터 처리 | 5 |
| Network | VPC Endpoints | 16 |
| Observability | CloudWatch Logs | 16 |
| Security | Secrets Manager | 5 |

절감 옵션: NAT-off 시 realistic 대비 ~$48/월 절감 (chapter 5.2 · chapter 6.1 권고안 A · 약 $130~$170/월).

### live 월 예상 비용 (realistic 시나리오)

**약 492 USD/월** (chapter 4.3 realistic 합계). AWS Pricing Calculator 확인 필요.

주요 구성(USD/월 근사치):

| 카테고리 | 항목 | 금액 |
|---|---|---|
| Compute | EC2 marketconnector `t3.medium` | 38 |
| Compute | ECS Fargate port-view | 45 |
| Compute | Fargate execution + intraday | 35 |
| Database | RDS db.t4g.medium multi-AZ | 104 |
| Database | RDS storage | 26 |
| Database | RDS backup | 8 |
| Network | NAT Gateway multi-AZ | 86 |
| Network | NAT 데이터 처리 | 12 |
| Network | ALB internal | 17 |
| Network | VPC Endpoints multi-AZ | 24 |
| Observability | CloudWatch Logs | 40 |

`db.m6g.large` multi-AZ 상향 시 합계 ~$650~$830/월 (chapter 4.3 비고 참조).

### 3환경 합산 (dev + paper + live) — realistic

**약 800 USD/월** (chapter 4.4 realistic 합계). low 651 · high 1,095.

### 주요 cost driver Top 5

| 순위 | Driver | 근거 |
|---|---|---|
| 1 | NAT Gateway (data processing 포함) | live realistic $86 + $12 · chapter 5 서두 "가장 큰 고정비 항목 중 하나" |
| 2 | RDS multi-AZ (`db.t4g.medium` / `db.m6g.large` + storage) | live realistic $104 + $26 · chapter 4.3 비고 "비용을 끌어올린다" |
| 3 | Fargate 24/7 상시 컴퓨트 (port-view + execution) | live realistic $45 + $35 · paper $23 + $25 |
| 4 | ALB internal (live) | live realistic $17 + LCU |
| 5 | VPC Endpoints (multi-AZ interface) | live realistic $24 · paper $16 |

### 비용 절감 결정 Top 5

| 순위 | 절감 결정 | 근거 |
|---|---|---|
| 1 | NAT-off (dev · paper 옵션 D/E) | dev 비고 · paper NAT 미사용 → ~$48/월 절감 |
| 2 | ALB-off (dev · paper) | dev · paper ECS Service Discovery 로 대체 · 권고안 A |
| 3 | `assignPublicIp=ENABLED` (Fargate public subnet + NAT 미사용) | dev 비고 · 권고안 A · port-view 1차 포팅 실증 |
| 4 | Fargate Task desiredCount 0/1 수동 운영 (port-view 실증) | 2026-06-30 (오후) port-view ECS Fargate 1차 포팅 실증 메모 · desiredCount 0 종료 정책 |
| 5 | EIP attach 유지 (detach / stop 회피) | attach + running 무료 vs detach/stop ~$3.6/월 |

> **안전 안내** — 본 Dashboard 의 수치는 모두 공개 가격 기준 근사치. 실제 청구 금액은 인터넷 데이터 전송량 · 거래일 수 · 장애 재실행 빈도에 따라 변동한다. IaC 작성 시 AWS Pricing Calculator 로 재검증 필요.

## 2026-07-01 Actual Cost Analysis (June 2026)

본 절은 2026-07-01 운영자 직접 수행한 2026 년 6 월 AWS 실제 청구액 분석과 VPC Endpoint 정리 절감 실행 결과를 사실 그대로 반영한다. 모든 수치는 실 청구액 근사치 · **AWS Pricing Calculator 확인 필요**. 상세 근거는 아래 `Cost Details` 하위 섹션 참조.

### 6 월 실측 요약

| 항목 | 값 |
|---|---|
| 대상 월 | 2026 년 6 월 |
| 세전 실제 비용 | 135.40 USD |
| 세금 | 13.55 USD |
| 세금 포함 실제 비용 | 약 148.95 USD |
| 기존 월말 예상(180 USD) 재평가 | 7 월 full automation 기준 보수적이지만 합리적인 추정 |
| 예상 월 절감액(SSM endpoint 제거 + endpoint 1 AZ 축소 완료) | 약 56.16 USD |
| 현재 aws-paper NAT Gateway 사용 여부 | 미사용 (VPC Endpoint 비용 중심 구조) |

### 카테고리별 실측 비용 (2026 년 6 월)

| 항목 | 값 |
|---|---|
| Virtual Private Cloud | 74.24 USD |
| Relational Database Service | 32.57 USD |
| Elastic Compute Cloud | 25.15 USD |
| Secrets Manager | 2.88 USD |
| ECS / S3 / ECR / Cost Explorer / Data Transfer | 소액 (합산 소액) |
| CloudWatch / Lambda / Step Functions / CloudWatch Events | 0.00 USD 수준 |
| 합계 (세전) | 135.40 USD |
| 세금 | 13.55 USD |
| 합계 (세금 포함) | 약 148.95 USD |

### 핵심 cost driver — VPC Endpoint

| 항목 | 값 |
|---|---|
| VPC 총 비용 | 74.24 USD/월 |
| 그중 VPC Endpoint 비용 | 70.69 USD/월 |
| VPC Endpoint Hours (6 월 누적) | 5,438 시간 |
| 월 720 시간 기준 상시 유지 Interface VPC Endpoint 추정 개수 | 약 7 ~ 8 개 |
| 판단 | 핵심 cost driver 는 VPC Endpoint |

### 2026-07-01 절감 실행 (분석 아님)

본 절감은 "분석만 한 것" 이 아니라 **실제 VPC Endpoint 정리 실행** 결과다.

| 실행 항목 | 상태 |
|---|---|
| SSM endpoint 제거 | 완료 |
| `com.amazonaws.ap-northeast-2.ecr.api` endpoint 2 AZ → 1 AZ 축소 | 완료 |
| `com.amazonaws.ap-northeast-2.ecr.dkr` endpoint 2 AZ → 1 AZ 축소 | 완료 |
| `com.amazonaws.ap-northeast-2.logs` endpoint 2 AZ → 1 AZ 축소 | 완료 |
| `com.amazonaws.ap-northeast-2.secretsmanager` endpoint 2 AZ → 1 AZ 축소 | 완료 |
| 추가 endpoint 삭제 | 보류 (작동 리스크 대비 절감 명분 약함) |

| 항목 | 값 |
|---|---|
| 예상 월 절감액 | 약 56.16 USD |
| 절감 형태 | 상시 컴퓨트 아님 · Interface Endpoint AZ · 개수 축소 |
| 검증 방식 | 다음 청구 주기 실측 · **AWS Pricing Calculator 확인 필요** |

### 다른 서비스 판단

| 서비스 | 6 월 실제 비용 위치 | 판단 |
|---|---|---|
| RDS PostgreSQL | 32.57 USD | 운영 persistence 중심 · 유지 가치 높음 |
| EC2 (Linux + Windows worker) | 25.15 USD | 6 월 기준 핵심 비용 원인 아님 |
| Secrets Manager | 2.88 USD | 소액 유지 |
| ECS / S3 / ECR / Cost Explorer / Data Transfer | 소액 | 소액 유지 |
| CloudWatch / Lambda / Step Functions / CloudWatch Events | 0.00 USD 수준 | 무료 한도 안 |

### 7 월 full automation 재평가

| 항목 | 값 |
|---|---|
| 기존 월말 예상 | 180 USD |
| 6 월 세금 포함 실측 | 약 148.95 USD |
| 6 월 실측 대비 7 월 여유 | 약 31 USD |
| 판단 | 7 월 full automation 기준 보수적이지만 합리적인 추정 |
| 흡수 가능 요소 | 자동화 시간 확대 · intraday 실행 회차 증가 · 장애 재실행 |

### 운영 모드별 월 비용 모델 (근사치)

현재 aws-paper 운영은 NAT Gateway 미사용 · VPC Endpoint 비용 중심 구조. 아래 표는 운영 모드별 월 비용 근사치로, IaC / Pricing Calculator 재확인 없이는 절대값으로 인용하지 않는다. **AWS Pricing Calculator 확인 필요**.

| 운영 모드 | 월 예상 비용 (USD) |
|---|---|
| Archive Mode | 5 ~ 20 |
| DB Retained Mode | 45 ~ 70 |
| Private AWS API Mode | 110 ~ 150 |
| Paper Daily Full ON | 150 ~ 190 |
| Demo / Interview Mode | 170 ~ 220 |
| Live Trading Ready Mode | 200 ~ 280 |

### Cost Details — 2026 년 6 월 실측 근거

- 실측 청구 기준 — AWS Billing 세전 총액 135.40 USD + 세금 13.55 USD = 약 148.95 USD. 청구 원문 · account-id · billing report URL 은 본 문서에 포함하지 않는다 (secret / 계정 식별자 보호 정책 정합).
- VPC 74.24 USD 중 VPC Endpoint 70.69 USD · 5,438 endpoint hours 는 6 월 누적치. 720 시간/월 기준으로 나누면 상시 유지 Interface VPC Endpoint 는 약 7 ~ 8 개 수준으로 해석된다.
- 절감 실행 후 유지 중인 Interface VPC Endpoint 는 `ecr.api` · `ecr.dkr` · `logs` · `secretsmanager` 를 포함해 최소 필요 세트만 1 AZ 로 축소된 상태. Gateway Endpoint (`s3`) 는 무료이므로 비용 영향 없음.
- 추가 endpoint 삭제 후보 — endpoint 를 더 제거하면 NAT Gateway 를 재도입해야 하거나 인터넷 outbound 를 public subnet 으로 우회해야 하므로 절감 명분이 약해진다. 본 회차에서는 **보류**.
- OD-NET-005 (VPC Endpoint 활성 항목 — 권고 세트) 는 결정 수준의 변경이 아니므로 본 회차에서 `operator-decisions.md` 는 수정하지 않는다. AZ 축소 · SSM endpoint 제거를 결정 문서에 반영할지는 후속 결정 (`needs_manual_review=true`).
- 7 월 full automation 재평가 근거 — Step 12 ~ 17 Scheduler ENABLE 전환 (chapter 3.3 `[2026-07-01 …]` 메모) 로 상시 컴퓨트 증가는 없고 EventBridge / Step Functions transitions 증가분은 무료 한도 안. 따라서 6 월 대비 7 월 증가분은 intraday 실행 회차 · CloudWatch Logs ingestion 소폭 상승 정도로 예상.

### Historical Notes — Modeling vs Actual 차이

- chapter 4.2 paper realistic 합계 `~212 USD/월` 은 NAT Gateway 1 개 + 데이터 처리 + Endpoint 일부 포함한 **모델링 시나리오** 값이다. 현재 실제 aws-paper 운영은 NAT Gateway 미사용 이므로 NAT 관련 두 행 (`NAT Gateway (1개) $43` · `NAT 데이터 처리 $5`) 은 실측 청구에 포함되지 않는다.
- 실측 148.95 USD (세금 포함) 는 chapter 4.2 low 합계 `~182 USD` 보다도 낮다. 이는 (a) NAT 미사용 · (b) VPC Endpoint 를 필요 최소 세트로만 유지 · (c) Fargate desiredCount 상시 1 이 아닌 검증 시점만 활성 (chapter 3.1 `[2026-06-30 (오후) …]` 메모 정합) · (d) CloudWatch Logs / Metrics / Alarms 사용량이 표 가정 (20 GB · 20 metrics · 20 alarms) 보다 낮음 등의 조합 결과로 해석된다.
- chapter 4.2 표 자체는 IaC 작성 시 재검증 기준 (상한 참고값) 으로 유지하고, 실측값은 본 절 `2026-07-01 Actual Cost Analysis` 를 우선 참조한다.
- **[2026-07-08 note · OD-MS-040 정합]** — ECS Fargate crawler / preprocessor TaskDefinition 에 env `TZ=Asia/Seoul` 을 추가하는 단기 패치는 월 비용 영향 없음. Fargate cpu / memory · CloudWatch Logs · Step Functions transitions 무변화. 비용 수치 재산정 대상 아님.

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
  - **[2026-06-30 (오후) ECS Fargate 1차 포팅 실증 메모]** — 본 일자 운영자 직접 수행한 ECS Fargate Public IP 1차 포팅에서 실제 운영된 task 사양은 launch type `FARGATE` / awsvpc / cpu 512 / memory 1024 / Spring profile `aws-paper` / Tomcat 8080 / container port 8080(0.5 vCPU / 1 GB 가정 정합).
  - 본 일자 운영 방식은 24/7 상시 desiredCount 1 이 아니라 **검증 시점만 desiredCount 1, 검증 후 desiredCount 0 종료 정책**(R-AUTO-033 [2026-06-30 오후 보강] / OD-MS-002 evidence). 1차 포팅 단계는 24/7 가정보다 실제 사용량이 낮을 수 있어 정식 상시 운영 진입 전까지 본 가정을 보수적 상한으로 유지.
  - 추가 비용 항목 1차 실증:
    - (a) ALB · NAT Gateway 미사용으로 4.2 paper 표의 해당 행은 0 산정 가능
    - (b) **Public IPv4 비용 추가** — `assignPublicIp=ENABLED` 시 `$0.005/IPv4-시간` 단가가 desiredCount 1 유지 시간에 비례해 부과 (검증 시점만 활성 시 누적 시간 미미)
    - (c) CloudWatch Logs retention 7일 / 1차 포팅 단계 log 발생량은 4.2 paper 가정(20 GB) 보다 훨씬 낮음
  - 본 1차 포팅 단계 후속: **AWS Pricing Calculator 재확인 필요**. 24/7 정식 상시 진입 시점에 본 표 갱신 / ALB · HTTPS · Route53 · Cloudflare Tunnel 도입 시점에 4.2 paper 표 `ALB(선택)` 행 반영 / multi-AZ · Auto Scaling · Blue/Green 도입 시점에 4.3 live 표 cross-spec audit.
  - 본 단계 비용 평문 인용(실제 영수증 / Billing dashboard 본문) 0건 / 운영자가 AWS Pricing Calculator 로 재산정 시점에 본 표 surgical edit.
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
- **[2026-06-30 (오후) 장중 손절 Slack 실 연동 비용 메모]** — 같은 일자 오후 추가 작업분으로 장중 손절 Slack 실 연동이 완료되었으나 비용 관점에서 **신규 상시 자원 추가 없음**.
  - (a) Notifier Lambda `portfolio-event-notifier` 의 호출 진입점이 MarketConnector EC2 runner 까지 확대되었지만 Lambda 자체는 기존 사용 / Lambda 호출 횟수는 hard stop 조건 충족 회차 한정 저빈도(평일 장중 10분 간격 evaluate × 평일 21일 = 월 약 2,520 호출 / Lambda free tier 안).
  - (b) Step Functions transitions 변경 없음 — `portfolio-paper-intraday-stop-sell-approval` 본 일자 변경 없음.
  - (c) EventBridge Scheduler 변경 없음 — 기존 10분 Snapshot Refresh Scheduler(OD-MS-035 정합) 재사용.
  - (d) EC2 MarketConnector 는 기존 EC2 + EIP 재사용 / 추가 EC2 · EBS · EIP 0건 / (e) IAM Role inline policy 추가는 무료 / (f) Slack webhook outbound 데이터 매우 작음(1건당 수 KB).
  - 합계로 본 일자 오후 추가 작업분의 월 비용 영향은 **AWS free tier 안 또는 1달러 미만**으로 사실상 0에 수렴. ALB · NAT Gateway · RDS multi-AZ · VPC Endpoints · Fargate 비용과 무관 / port-marketconnector EC2 비용 · Daily Batch state machine 비용 · EC2 lifecycle Scheduler 비용 모두 유지. **AWS Pricing Calculator 확인 필요**.
- **[2026-06-30 (오후) Daily Brief Slack 자동화 비용 메모]** — Daily Brief Slack 알림 자동화의 자원 및 월 비용 요약. 자세한 결정은 OD-MS-038 신규 / R-AUTO-035 신규 mitigation 참조.
  - Lambda 2개 — Builder `portfolio-daily-brief-slack-summary-builder` + Notifier `portfolio-event-notifier`(후자는 기존 Approval / Daily Execution Slack 알림과 공유).
  - mini Step Functions 1개 — `portfolio-daily-brief-slack-notification` / 구조 `BuildDailyBriefPayload → SendSlackNotifier` / 2 state × 평일 2회 = 월 약 88 transitions / Standard 단가 ~$0.025 / 1,000 transitions 기준 월 비용 약 $0.002.
  - EventBridge Scheduler 2개(장전 `portfolio-daily-brief-morning-slack-0750-kst` + 장후 `portfolio-daily-brief-evening-slack-1550-kst`) — 평일 2회 = 월 약 44 invocations / 14M 무료 한도 안 / 추가 비용 0.
  - IAM Role 2개(무료) + Secrets Manager `GetSecretValue` 호출(평일 2회 = 월 약 44회 / 단가 $0.05/10,000 calls / 비용 거의 0) + Slack webhook outbound 데이터(메시지 1건 수 KB / 누적 매우 작음).
  - 합계: Daily Brief Slack 자동화의 월 비용은 **AWS free tier 안 또는 1달러 미만** (사실상 0). Fargate · ALB · NAT Gateway · RDS multi-AZ · VPC Endpoints 비용과 무관한 **lightweight automation**. **AWS Pricing Calculator 확인 필요**.
- **[2026-07-01 Step 12~17 Scheduler DISABLED → ENABLED 비용 메모]** — 본 일자 aws-paper Step 12~17 Scheduler `portfolio-paper-daily-step12-17-order-0901-kst` DISABLED → ENABLED 전환으로 인한 **신규 상시 컴퓨트 비용 없음**.
  - (a) EventBridge Scheduler invocation — 09:01 평일 1회 = 월 약 22회 추가 / 기존 14M 무료 한도 안 / 추가 비용 0.
  - (b) Step Functions Standard transitions — 후보 없는 날 NO_TARGET 안전 종료 시 2~3 transitions / 후보 있는 날 Step 12~17 전 구간 20~30 transitions / 평일 22회 최대 660 transitions 기준 월 비용 약 $0.02 미만.
  - (c) Dispatcher Lambda `portfolio-paper-daily-scheduler-dispatcher` invocation — 기존 08:00 approval 1회 + 09:01 order 1회 = 평일 2회 / free tier 안.
  - (d) RDS · EC2 · Fargate · NAT Gateway · ALB · VPC Endpoints 비용 변경 없음 — 기존 MarketConnector EC2 + port_strategy_execution ECS Fargate Task + RDS 그대로 재사용 / 신규 자원 0건.
  - (e) Slack outbound(`APPROVAL_REQUIRED` + `DAILY_EXECUTION_SUCCESS` 또는 `DAILY_EXECUTION_FAILED` 등) 메시지 1건 수 KB / 누적 매우 작음.
  - 합계 — 본 일자 Scheduler ENABLE 전환의 월 비용 영향은 모두 **AWS free tier 한도 안 또는 사실상 0**에 수렴 / 4.2 paper · 4.3 live 표 값 변경 없음. 후보 있는 날 실 실행 회차 누적 후 실측 기반 재산정 가능 / **AWS Pricing Calculator 확인 필요**.
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
- KRX Windows worker(paper, 2026-06-12 ~ 2026-06-16 도입) 비용 메모 — Autologon · Windows Scheduled Task · SSM RunCommand 자체 비용은 사실상 0.
  - 실제 비용 영향은 Windows EC2 running 시간 + EIP(필요 시) + EBS storage + CloudWatch Logs 저장량으로 발생. KRX program / shortsell daily 수집은 영업일 단위 짧은 실행이므로 stop 절차(08 spec task 59 후속)로 idle 시간 최소화.
  - EC2 stop 후에도 EIP 는 분 단위 과금 발생 가능 → 운영자 결정으로 detach 여부 검토 가능. Autologon 은 paper 전용 보안 예외(OD-MS-022 / R-SEC-009) 로 운영하며, 추후 전용 local user 전환 또는 aws-live 도입 시 별도 결정 책임.

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

## Security Notes

- 본 작업은 문서 작성만 수행한다. 실제 AWS 리소스 생성 / IaC 생성 / 8개 MS 코드 / README / AGENTS.md / docs / CHANGELOG / worklog 수정은 하지 않는다.
- 실제 secret 값은 본 문서에 포함하지 않는다. 모두 `[REDACTED]`로 표기한다.
- 본 문서의 단가는 모두 "AWS Pricing Calculator 확인 필요" 단서 하에 사용한다.
- 8개 MS의 어떤 entrypoint도 실행하지 않았으며, broker / KIS / Selenium / Naver / yfinance / RDS / DDL / DML / 주문 / 체결 / 크롤링 / 외부 API 호출은 본 작업에 포함되지 않는다.
