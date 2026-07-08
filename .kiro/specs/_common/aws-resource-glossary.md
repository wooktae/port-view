# AWS Resource Glossary — AWS Migration

## Purpose

본 문서는 AWS 용어가 익숙하지 않은 운영자가 AWS Migration spec(`01-aws-migration-foundation` · `02-aws-network-and-rds` 및 후속 03 ~ 10) 을 읽을 때 빠르게 의미를 잡을 수 있도록 만든 공통 용어집이다. 02 spec 과 후속 spec 모두 본 용어집을 공유 참조한다.

문서 흐름: **Purpose → Category TOC → Glossary → Usage Notes → Update Rules → Security Notes**. Glossary 는 AWS 개념 이해 중심, `Usage Notes` 는 특정 날짜 운영 실증 메모 중심으로 분리한다.

각 용어 항목은 5-field template 을 따른다.

- 설명 (한 줄 개념 설명)
- 프로젝트 역할 (이 포트폴리오에서의 역할)
- 비용 (비용 발생 여부와 대략 driver)
- 주의 (운영자가 조심해야 할 점)
- 관련 spec (spec 번호 01 ~ 10)

용어 단가는 서울 `ap-northeast-2` 기준 근사치이며 "AWS Pricing Calculator 확인 필요" 단서가 붙는다.

민감정보 원문(secret · password · token · KIS app key · KIS app secret · Slack webhook URL · 계좌번호 · account-id · 실제 ARN · public IP · broker_order_no) 은 어디에도 기록하지 않고 `[REDACTED]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_PUBLIC_IP]` · `[REDACTED_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_SECRET_ARN]` · `[REDACTED_BROKER_ORDER_NO]` placeholder 계열만 사용한다.

---

## Category TOC

운영자가 카테고리별로 용어를 빠르게 찾을 수 있도록 정리한 목차. 각 앵커는 문서 내 해당 헤딩으로 점프한다. 상세 template (한 줄 설명 · 이 포트폴리오에서의 역할 · 비용 발생 여부 · 운영자가 조심해야 할 점 · 관련 spec) 은 각 항목 본문 참조.

### Network

- [Region](#region)
- [VPC (Virtual Private Cloud)](#vpc-virtual-private-cloud)
- [Subnet](#subnet) · [Public Subnet](#public-subnet) · [Private Subnet](#private-subnet)
- [Route Table](#route-table)
- [Internet Gateway (IGW)](#internet-gateway-igw)
- [NAT Gateway](#nat-gateway) · [NAT Instance](#nat-instance)
- [VPC Endpoint](#vpc-endpoint)
- [Security Group (SG)](#security-group-sg)
- [Elastic IP (EIP)](#elastic-ip-eip)
- [ALB (Application Load Balancer)](#alb-application-load-balancer)

### Compute

- [EC2 (Elastic Compute Cloud)](#ec2-elastic-compute-cloud)
- [ECS (Elastic Container Service)](#ecs-elastic-container-service)
- [Fargate (ECS launch type)](#fargate-ecs-launch-type)
- [ECR (Elastic Container Registry)](#ecr-elastic-container-registry)

### Database

- [RDS (Relational Database Service)](#rds-relational-database-service)
- [RDS Subnet Group](#rds-subnet-group)
- [RDS Parameter Group](#rds-parameter-group)

### Security / IAM / Secrets

- [Secrets Manager](#secrets-manager)
- [SSM Parameter Store](#ssm-parameter-store)
- [IAM User](#iam-user)
- [IAM Role](#iam-role)
- [IAM Policy](#iam-policy)
- [Instance Profile](#instance-profile)
- [KMS (Key Management Service)](#kms-key-management-service) — 문서 하단 관련 참조 · 옵션 카테고리

### Orchestration

- [Step Functions](#step-functions)
- [EventBridge Scheduler](#eventbridge-scheduler)
- [Lambda](#lambda) — 주 compute 아님 · Dispatcher / Notifier / lifecycle 보조 계층 (OD-MS-009 / OD-MS-030 / OD-MS-032 / OD-MS-034 정합)
- SSM RunCommand / Windows Scheduled Task / Windows Autologon / Cloud Map — 관련 항목은 본문 내 SSM · IAM · EC2 섹션과 결합해 참조

### Observability

- [CloudWatch Logs](#cloudwatch-logs)
- [CloudWatch Metrics](#cloudwatch-metrics)
- [CloudWatch Alarm](#cloudwatch-alarm)

### Storage / Artifact

- [S3 (Simple Storage Service)](#s3-simple-storage-service) — 문서 하단 관련 항목 참조 (research report / token backup)
- [AWS Batch](#aws-batch) — 장시간 backtest + report artifact 저장소로 S3 와 결합 사용

> **안전 제약** — 본 문서는 문서 편집만 수행한다. 실제 AWS 리소스 생성 / 수정 / 삭제는 운영자 직접 수행 영역이며 Kiro 는 실행하지 않는다. 모든 secret / password / token / app key / app secret / 계좌번호 / webhook URL / 실제 ARN / public IP / broker_order_no 원문은 본 문서에 기록하지 않으며 `[REDACTED]` 계열 placeholder 만 사용한다.

---

## Glossary

카테고리별 AWS 서비스 · 리소스 용어 상세. 각 항목은 5-field template(설명 · 프로젝트 역할 · 비용 · 주의 · 관련 spec) 을 따른다.

### Region

- 한 줄 설명: AWS 데이터센터가 모여 있는 지리적 위치 단위.
- 역할: 본 프로젝트는 서울 `ap-northeast-2` 단일 region에서 운영한다. broker(KIS)와 사용자 모두 한국 기준이라 latency / 규제 측면에서 단일 region 권고.
- 비용 발생: region 자체에는 비용이 없다. 다만 다른 region으로 데이터 전송 시 비용 발생.
- 조심할 점: region을 옮기면 RDS / ECR / 모든 endpoint URL이 바뀐다. 본 spec 안에서는 region 변경 금지.
- 관련 spec: 02, 06, 07, 10.

### VPC (Virtual Private Cloud)

- 한 줄 설명: AWS 안의 사설 네트워크 컨테이너. 사내 네트워크처럼 IP 대역과 라우팅을 직접 정의한다.
- 역할: PORT-STRATEGY-AI 8개 MS가 들어갈 단일 VPC `portfolio-vpc`. 환경(aws-paper, aws-live)은 같은 VPC 안에서 SG와 subnet 태그로 분리한다.
- 비용 발생: VPC 자체는 무료. NAT Gateway, VPC Endpoint, NAT Instance 등 부속 리소스가 비용을 만든다.
- 조심할 점: CIDR 결정 시 사내 다른 네트워크와 겹치면 안 된다. 한번 정한 CIDR은 변경 어려움.
- 관련 spec: 02 우선, 그 외 모두 의존.

### Subnet

- 한 줄 설명: VPC 안에서 IP 대역을 작게 잘라낸 네트워크 구획.
- 역할: 본 spec은 public / private (app) / private (data) 3종 × 2 AZ로 6개 subnet 권고.
- 비용 발생: subnet 자체는 무료.
- 조심할 점: subnet은 한 AZ에만 속한다. multi-AZ를 원하면 subnet도 AZ별로 따로 만든다.
- 관련 spec: 02.

### Public Subnet

- 한 줄 설명: Internet Gateway로 직접 인터넷 outbound가 가능한 subnet.
- 역할: 마켓커넥터 EC2(EIP) 와 인터넷 outbound가 필요한 ECS Fargate Task(NAT-free 전략)는 public subnet에 배치.
- 비용 발생: subnet 자체는 무료. public IP 사용량에 따라 IPv4 비용 발생 가능(`$0.005/IPv4-시간`).
- 조심할 점: Task 또는 EC2를 무심코 public subnet에 두면 외부에 직접 노출될 수 있다. 반드시 SG로 inbound를 차단.
- 관련 spec: 02, 03, 08.

### Private Subnet

- 한 줄 설명: Internet Gateway 없이 내부 통신 또는 NAT/VPC Endpoint로만 외부에 접근 가능한 subnet.
- 역할: RDS, 일반 ECS Task, 외부 outbound가 없는 워크로드 배치 위치. 본 spec은 NAT-free 전략이므로 외부 인터넷 outbound가 꼭 필요한 워크로드는 private subnet에 두지 않는다.
- 비용 발생: subnet 자체는 무료.
- 조심할 점: NAT 미사용 환경에서 private subnet에 외부 outbound가 필요한 워크로드를 두면 동작하지 않는다. VPC Endpoint로 AWS 서비스에만 접근 가능.
- 관련 spec: 02, 04, 05, 09.

### Route Table

- 한 줄 설명: subnet 트래픽이 어디로 흘러가는지 정하는 규칙 묶음.
- 역할: public Route Table은 0.0.0.0/0 → IGW. private (app) Route Table은 NAT-free 전략에서는 외부 라우트 없이 VPC Endpoint로만 AWS 서비스에 접근. data Route Table은 외부 라우트 없음.
- 비용 발생: 무료.
- 조심할 점: Route Table을 잘못 매핑하면 ECS Task가 RDS에 못 붙거나 외부 outbound가 끊긴다. SG보다 우선 점검 항목.
- 관련 spec: 02.

### Internet Gateway (IGW)

- 한 줄 설명: VPC와 인터넷을 연결하는 게이트웨이.
- 역할: public subnet의 외부 outbound와 EIP 부여 EC2의 외부 출구.
- 비용 발생: IGW 자체는 무료. 다만 인터넷 outbound 데이터 전송 비용은 별도(~$0.114/GB).
- 조심할 점: 한 VPC에 1개만 attach. detach 후 변경 시 외부 통신이 끊긴다.
- 관련 spec: 02, 03.

### NAT Gateway

- 한 줄 설명: private subnet 워크로드가 인터넷으로 outbound만 나가게 해 주는 AWS 관리형 NAT.
- 역할: 본 프로젝트는 비용 절감을 위해 aws-paper / aws-live 모두 미사용을 기본안으로 채택했다. 인터넷 outbound가 필요한 워크로드는 public subnet 또는 EC2+EIP로 대체.
- 비용 발생: 사용 시 ~$43/월/AZ + 데이터 처리 ~$0.059/GB. 가장 큰 단일 고정비 중 하나.
- 조심할 점: multi-AZ NAT는 비용이 2배. 한 번 켜고 나면 비용 점검을 잊기 쉽다.
- 관련 spec: 02 비교 항목, 03/08 NAT-free 대안 검토.

### NAT Instance

- 한 줄 설명: NAT 역할을 하는 자체 EC2(예: `t4g.nano`).
- 역할: NAT Gateway 비용을 줄이고 싶을 때 후보. 본 spec에서는 비교만 하고 권고는 NAT-free.
- 비용 발생: EC2 시간 단가(~$3.5/월) + EBS + 데이터 처리. NAT GW보다 훨씬 저렴.
- 조심할 점: 단일 EC2이므로 SPOF. 패치/모니터링 직접. Multi-AZ HA를 운영자가 직접 구현해야 한다. 본 프로젝트에서는 이 운영 부담을 피하기 위해 NAT-free 전략 선택.
- 관련 spec: 02 비교 항목.

### VPC Endpoint

- 한 줄 설명: NAT 없이 VPC 내부에서 AWS 서비스에 직접 접근하게 해 주는 사설 연결.
- 역할: NAT-free 전략의 핵심. ECR / S3 / Secrets Manager / SSM / CloudWatch Logs는 VPC Endpoint로 가져온다.
  - S3: Gateway endpoint(무료). ECR layer 다운로드 / report 저장에 필수.
  - ECR (api+dkr): Interface endpoint. 컨테이너 이미지 pull.
  - Secrets Manager: Interface endpoint. ECS Task / EC2 startup secret 주입.
  - SSM: Interface endpoint. SSM Session Manager 운영자 접속, Parameter Store 조회.
  - CloudWatch Logs: Interface endpoint. ECS / EC2 로그 송신.
  - STS / KMS: 필요 시 활성. CMK 사용하면 KMS endpoint 필요할 수 있다.
- 비용 발생: Gateway endpoint 무료. Interface endpoint AZ당 ~$8/월 + 데이터 처리. multi-AZ 활성 시 endpoint 5개 × 2 AZ = ~$80/월.
- 조심할 점: NAT-free 환경에서 endpoint를 빠뜨리면 ECS Task가 ECR pull 실패로 기동하지 못한다. Endpoint 활성 항목과 SG inbound(443)를 먼저 점검.
- 관련 spec: 02, 06, 07.

### Security Group (SG)

- 한 줄 설명: AWS 리소스 단위의 stateful 방화벽 규칙.
- 역할: MS별 SG 분리. RDS SG는 inbound를 다른 SG 참조로만 허용(0.0.0.0/0 절대 금지).
- 비용 발생: 무료.
- 조심할 점: SG는 stateful이라 inbound 허용 시 outbound는 자동 허용. 그러나 outbound는 별도 명시. SG 규칙 변경 시 즉시 반영되므로 잘못 수정하면 운영 중 단절 발생.
- 관련 spec: 02 우선, 그 외 모두 사용.

### Elastic IP (EIP)

- 한 줄 설명: 고정 public IP. EC2 또는 NAT Gateway에 부여 가능.
- 역할: 마켓커넥터 EC2에 부여해 broker(KIS) 측 IP 등록 정책에 대응. broker 측에 EIP를 등록한다.
- 비용 발생: instance에 attach + running이면 무료. detach 또는 stop 상태에서는 ~$3.6/월.
- 조심할 점: EC2 교체 시 EIP detach → 새 EC2에 attach. attach 안 된 시간 동안 미사용 비용 + broker outbound IP 변경 위험.
- 관련 spec: 03 marketconnector-ec2.

### ALB (Application Load Balancer)

- 한 줄 설명: HTTP/HTTPS 트래픽 분산 로드 밸런서.
- 역할: port-view 운영 콘솔 앞단(internal ALB)에 둘 수 있다. 본 spec 결정에서는 aws-paper 초기 미사용, aws-live도 비용 절감안에서는 보류.
- 비용 발생: ~$16.5/월 + LCU 사용량.
- 조심할 점: ALB SG inbound는 운영자 IP allowlist 또는 인증 게이트로 좁히는 것이 안전.
- 관련 spec: 05 port-view-ecs-and-runbook.

### EC2 (Elastic Compute Cloud)

- 한 줄 설명: AWS 가상 서버.
- 역할: 마켓커넥터(`port-marketconnector`)는 broker IP 등록과 단일 access_token 세션 제약 때문에 EC2 + EIP를 1순위로 채택. Crawler용 Selenium은 후보 중 하나. **[2026-06-24 EC2 lifecycle 자동화 ENABLED]** — MarketConnector EC2 는 **07:50 KST start / 15:50 KST stop**(영업일만 / 휴일 / 주말은 Lambda holiday guard 가 fail-closed 로 skip).
  - Crawler EC2 는 **07:50 KST start / Step Functions Step 1~11 성공 시 stop / 실패 시 디버깅 위해 유지**(OD-MS-034 신규 / R-AUTO-028 신규 정합 / EC2 lifecycle Lambda `portfolio-paper-ec2-lifecycle-dispatcher` 담당). EIP attach 상태 유지 / KIS broker 측 IP 등록 변경 0건 / R-BROKER-001 mitigation 회귀 0건.
  - **[2026-06-24 장중 10분 refresh 대상 — 문서 반영 단계]** — MarketConnector EC2 는 장중 영업시간 10분 간격으로 SSM RunCommand 를 통해 snapshot refresh entrypoint(`connector_balance_snapshot` + `connector_position_snapshot` 갱신) 와 StrategyExecution Intraday Evaluate 신규 entrypoint(`strategy_intraday_position_check` 저장 + `INTRADAY_STOP_SELL` `READY` order 생성) 의 실행 대상이 됨(OD-MS-035 신규 / 🟢 확정 / 영향 spec 03 · 04 · 05 · 10).
  - Crawler EC2 는 장중 refresh 대상이 아님(Daily Step 1~11 흐름의 KRX raw 적재 책임 한정).
- 비용 발생: 인스턴스 시간 단가(예: `t4g.small` ~$15/월, `t3.medium` ~$38/월) + EBS + 데이터 전송. **영업 시간 외 EC2 stop 으로 instance-hour 누적 감소**(정확한 절감액은 운영 회차 누적 후 산출).
- 조심할 점: SSH 22 inbound 0.0.0.0/0 금지. SSM Session Manager만 사용. 인스턴스 교체 시 EIP / 토큰 / IAM Role 재부여.
  - **MarketConnector EC2 stop 직후 Step 12~17 진입 시 SSM RunCommand 실패 위험** — 09:01 schedule 자동 ENABLE 보류 정책(OD-MS-033) 으로 차단 / 운영자가 수동 invoke 시점에 EC2 `running` 상태 사전 점검 책임 / Step Functions Step 1 / 12 / 13 / 17 의 SSM RunCommand 호출 실패 위험은 R-AUTO-021 / R-AUTO-022 mitigation 결합.
  - **[장중 refresh 진입 시 EC2 `running` 정합]** — 1단계 refresh 진입 시 MarketConnector EC2 가 `stopped` 또는 `pending` 상태이면 SSM RunCommand 실패 위험 / 07:50 start Scheduler ENABLED 정책으로 영업일 07:50 이후 `running` 상태 유지 / 15:50 stop Scheduler 이후 또는 EC2 lifecycle 사고 시 1단계 refresh fail-closed.
- 관련 spec: 03, 08(NAT-free 대안).

### ECS (Elastic Container Service)

- 한 줄 설명: AWS 관리형 컨테이너 오케스트레이터.
- 역할: 본 프로젝트는 거의 모든 Python / Java MS를 ECS 위에서 운영. Cluster는 환경별로 분리(`portfolio-paper`, `portfolio-live`).
- 비용 발생: 클러스터 자체는 무료. 안에서 도는 Fargate / EC2 launch type에 따라 비용 발생.
- 조심할 점: Task Definition revision 관리. 배포 시 잘못된 revision로 롤백 가능하도록 보존.
- 관련 spec: 04, 05, 08.

### Fargate (ECS launch type)

- 한 줄 설명: 서버 관리 없이 컨테이너만 실행하는 ECS launch type.
- 역할: Daily Batch step, port-view 서비스, crawler/preprocessor Task 등 대부분의 컨테이너 워크로드. 시간 단가는 vCPU / 메모리에 비례.
  - **[2026-06-30 (오후) port-view ECS Fargate Public IP 1차 포팅 1차 실증]** — 본 일자 운영자가 직접 수행한 port-view ECS Fargate 1차 포팅 통과 운영 예시 — ECS cluster `portfolio-paper-cluster` / ECS service `portfolio-view-service` / ECS task definition `portfolio-view:2`(revision 1 → 2 / 기본 계좌번호 env `PORTFOLIO_BATCH_DEFAULT_ACCOUNT_NO` + `PORTFOLIO_VIEW_ACCOUNT_DEFAULT_ACCOUNT_NO` 보정 한정) / ECR repository `portfolio-view` /
  - launch type `FARGATE` / network mode `awsvpc` / cpu 512 / memory 1024 / container port 8080 / Spring profile `aws-paper` / Tomcat 8080 / `assignPublicIp=ENABLED` / public subnet 배치 / ALB · NAT Gateway 미사용 / 운영자 IP/32 SG inbound + CloudWatch Logs `/ecs/portfolio-view` retention 7일 / 검증 후 desiredCount 0 종료(R-AUTO-033 [2026-06-30 오후 보강] / OD-MS-002 evidence).
  - ECS View → AWS Step Functions Step 12~17 승인 실행 1차 실증 통과(executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / status `SUCCEEDED` / NO_TARGET / Slack `DAILY_EXECUTION_SUCCESS` 수신).
  - **[2026-06-29 (2) port-view Fargate 정합 메모]** — port-view 는 ECS Fargate Service 1순위(OD-MS-002 정합 / 본 표 4.2 본문 그대로 유지) 로 상시 웹 콘솔 역할만 담당하고, Daily Batch 의 실제 실행은 View 컨테이너 안 subprocess 가 아니라 **Step Functions `StartExecution`** 으로 이관(OD-MS-009 / OD-MS-037 정합 / commit `e72de6f` 1차 실증 / 본 메모 코드 본문 인용 0건 / R-DOCS-001 정합).
  - Fargate 운영 안전 기본값은 `portfolio.batch.local-file-execution-enabled=false` · `portfolio.batch.paper-order-enabled=false` · `portfolio.batch.full-pipeline-execution-enabled=false` · `portfolio.batch.max-executable-step-order=11`(R-AUTO-034 신규 정합 / 05 spec 후속 phase 책임).
- 비용 발생: ~$0.05056 / vCPU-시간 + ~$0.00553 / GB-시간. 0.5 vCPU + 1 GB 24/7 ≈ $22.5/월.
- 조심할 점: Task가 너무 자주 멈추면 cold start 비용 증가. 인터넷 outbound가 필요한 Task는 NAT-free 전략에서 public subnet에 두고 SG 통제.
  - **[port-view Fargate Task Role 권한 분리]** — port-view Fargate Task Role 은 `states:StartExecution` 권한을 특정 state machine ARN(`portfolio-paper-daily-step1-17-approval` 권장) 한정으로 부여 / Resource · Action wildcard 0건(OD-SEC-005 / OD-SEC-006 / R-AUTO-034 신규 mitigation 정합 / 06 spec 후속 phase 책임).
  - RDS 접속정보는 image / properties 직접 기록 금지 / Secrets Manager 또는 SSM SecureString 주입 / `stateMachineArn` · region · `executionNamePrefix` 도 env / config 주입.
- 관련 spec: 04, 05, 08, 09.

### ECR (Elastic Container Registry)

- 한 줄 설명: AWS 컨테이너 이미지 저장소.
- 역할: 8개 MS 중 컨테이너 배포 대상 MS의 이미지 저장(`port-view`, `port-interest-crawler`, `port-interest-preprocessor`, `port-strategy-decision`, `port-strategy-execution`, `port-strategy-research`, 옵션으로 `port-marketconnector`).
- 비용 발생: storage ~$0.10/GB-월. 같은 region pull은 무료. NAT 미사용 환경에서는 ECR Endpoint 비용이 추가.
- 조심할 점: 이미지 태그 관리(`:{git-sha}` + `:{env}`). lifecycle policy로 오래된 이미지 정리. 빌드 시 secret을 이미지에 굽지 않는다.
- 관련 spec: 07 cicd-pipelines.

### RDS (Relational Database Service)

- 한 줄 설명: AWS 관리형 RDBMS. 본 프로젝트는 RDS for PostgreSQL 16 이상 사용.
- 역할: 단일 portfolio DB. schema-per-domain 10개 schema 유지. aws-paper와 aws-live 환경별 인스턴스 분리.
- 비용 발생: 인스턴스 시간 단가 + storage + backup. multi-AZ는 약 2배.
- 조심할 점: publicly accessible false. SG inbound는 다른 SG 참조만. master password는 Secrets Manager 보관.
- 관련 spec: 02, 06, 10.

### RDS Subnet Group

- 한 줄 설명: RDS가 배치될 subnet 묶음 정의.
- 역할: data-a, data-b 두 private (data) subnet을 묶어 multi-AZ 가능하도록 구성. single-AZ를 시작점으로 두더라도 subnet group은 multi-AZ 가능 형태로 구성.
- 비용 발생: 무료.
- 조심할 점: subnet group은 RDS 생성 후 변경 어려움. 처음부터 2 AZ subnet으로 구성.
- 관련 spec: 02.

### RDS Parameter Group

- 한 줄 설명: PostgreSQL 설정값 모음.
- 역할: `rds.force_ssl=1` (paper/live), `log_min_duration_statement`, `log_lock_waits=1`, `idle_in_transaction_session_timeout` 등 운영 권고값을 묶어 적용.
- 비용 발생: 무료.
- 조심할 점: parameter group 변경 시 일부 항목은 reboot 필요. 운영 중 변경 시 reboot window 주의.
- 관련 spec: 02.

### Secrets Manager

- 한 줄 설명: AWS 관리형 비밀 저장소. 자동 rotation 지원.
- 역할: KIS app key / app secret / base URL / 계좌번호 / DB master password / **Slack webhook URL(`portfolio-event-notifier` Lambda 의 `SLACK_WEBHOOK_URL` — 운영 안정화 후 이전 예정 / OD-MS-030 / R-AUTO-024 정합)** 등 고민감 secret 저장. 모두 `[REDACTED]`로만 표기.
- 비용 발생: $0.40 / secret / 월 + $0.05 / 10,000 API calls.
- 조심할 점: 본 spec에서 최종 결정 항목과 IAM 정책 매트릭스는 06 spec에서 확정. rotation 적용 시 application 재기동 영향 고려. **Slack webhook 이전 시 Lambda 코드는 `secretsmanager:GetSecretValue` 호출로 runtime 조회 / IAM Role(`portfolio-event-notifier-lambda-role`) 에 Resource ARN 한정 read 권한 추가(wildcard 0건)**.
  - **[2026-06-30 (오후) Builder Lambda DB password `valueFrom` 방식 메모]** — `portfolio-daily-brief-slack-summary-builder` Lambda 는 DB password 를 환경변수 `DB_PASSWORD_SECRET_VALUE_FROM` 에 Secrets Manager `valueFrom` 으로 주입 + Lambda 코드 안에서 `secretsmanager:GetSecretValue` runtime 조회 + `pg8000` 으로 RDS 접속(`psycopg2` 미사용 / OD-MS-038 신규 / R-AUTO-035 신규 mitigation 정합 / OD-SEC-002 정합).
  - Lambda code 본문 / 실제 secret ARN / Secrets Manager secret value / `GetSecretValue` 응답 본문 평문 기록 0건(R-DOCS-001 정합 / 운영 식별자 = 환경변수명 `DB_PASSWORD_SECRET_VALUE_FROM` / DB driver `pg8000` / Lambda runtime `Python 3.12` 만 사실 기록 — secret 아님).
- 관련 spec: 06 secrets-and-iam.

### SSM Parameter Store

- 한 줄 설명: AWS 환경변수 / 설정 / SecureString 저장소.
- 역할: `INTEREST_DB_HOST/PORT/NAME/USER`, `PORTFOLIO_DB_NAME`, `PORT_BROKER_NAME`, `PORT_ENVIRONMENT`, `PORT_STRATEGY_*`, `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT` 등 저민감 환경변수 보관 후보. SecureString 으로 일부 secret 대체 가능. **Slack webhook URL 도 운영 안정화 후 Secrets Manager 와 함께 후보(SecureString) — 최종 위치는 06 spec 후속 결정**(OD-MS-030 / R-AUTO-024 정합).
- 비용 발생: Standard tier 무료(파라미터 10,000개까지). Advanced tier $0.05/파라미터/월.
- 조심할 점: 환경변수 키 호환성을 위해 키 이름은 그대로 유지. 잘못된 환경에 잘못된 값을 넣으면 paper에서 live broker로 호출 가능 → live 자동매매 사고 위험.
- 관련 spec: 06 secrets-and-iam.

### IAM User

- 한 줄 설명: AWS 계정 안에서 사람(또는 외부 system)이 콘솔 / API에 로그인할 때 쓰는 신원.
- 역할: 본 프로젝트는 운영자 1인이 사용하는 IAM 관리자 사용자 `portadmin`을 만들고 모든 일상 작업을 portadmin으로 수행한다. Root 계정은 비상용(청구 / 계정 폐쇄 / IAM 정책 변경)으로만 보관한다. 02 runbook Step 0에서 portadmin 생성 / AdministratorAccess 부여 / 콘솔 sign-in URL 확보 / MFA 활성 / Root 보안 강화 절차를 다룬다.
- 비용 발생: IAM User 자체는 무료.
- 조심할 점: portadmin 비밀번호 / MFA 시리얼 / 백업 코드, Root MFA 시리얼은 본 작업공간 어떤 문서에도 평문 기록 금지(모두 `[REDACTED]`). Root access key는 발급되어 있으면 즉시 삭제. 비밀번호 분실 / MFA 분실 시 복구 절차는 R-SEC-002, R-SEC-003 참조([`./risk-register.md`](./risk-register.md)).
- 관련 spec: 02(Step 0 portadmin 생성), 06(IAM 권한 매트릭스 / portadmin 외 추가 사용자 검토).

### IAM Role

- 한 줄 설명: AWS 리소스가 다른 AWS 리소스를 호출할 때 쓰는 권한 묶음.
- 역할: ECS Task Role / EC2 Instance Role / Lambda Role / RDS Enhanced Monitoring Role 등. **[2026-06-30 (오후) MarketConnector EC2 Instance Role inline policy 분리 1차 실증]** — 본 일자 오후 운영자가 직접 부여한 inline policy 가 IAM Role 책임 경계 보존의 1차 실증으로 사용됨(R-AUTO-036 신규 mitigation 정합 / OD-SEC-005 / OD-SEC-006 정합).
  - MarketConnector EC2 의 Instance Role `portfolio-paper-marketconnector-ec2-role` 에 inline policy `portfolio-paper-marketconnector-event-notifier-invoke`(action `lambda:InvokeFunction` / Resource `portfolio-event-notifier` Lambda 한정 / Resource · Action wildcard 0건) 부여 + EC2 측 invoke smoke 성공 + 실제 runner 1회 안전 검증 통과(SSM commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` / `EMPTY_NORMAL`).
  - 본 inline policy 는 Daily Batch / Step Functions 측 IAM Role(`portfolio-paper-stepfunctions-execution-role` · `portfolio-paper-eventbridge-scheduler-role` · `portfolio-paper-daily-scheduler-dispatcher-role` · `portfolio-paper-ec2-lifecycle-dispatcher` Lambda Role) / Daily Brief 알림 전용 IAM Role(`portfolio-daily-brief-sfn-role` · `portfolio-daily-brief-scheduler-role`) 과 모두 별도 / 권한 광역 회귀 0건.
  - **[2026-06-30 (오후) port-view ECS Task Role / Execution Role 분리 1차 실증]** — Fargate 로 배포된 port-view 의 Task Role 이름 `portfolio-paper-view-task-role`(실제 ARN `[REDACTED_ARN]`) 의 `states:StartExecution` 권한이 Step 12~17 approval state machine ARN(`portfolio-paper-daily-step12-17-approval`) 한정으로 부여되어 실제 Fargate 환경에서 1차 실증 통과(R-AUTO-034 [2026-06-30 오후 보강] / 06 spec 후속 phase 책임).
  - Task Role 과 ECS Task Execution Role 이름 `portfolio-paper-ecs-task-execution-role`(실제 ARN `[REDACTED_ARN]`) 분리 — Task Execution Role 은 ECR pull / CloudWatch Logs `PutLogEvents` / Secrets Manager `GetSecretValue` 책임 / Task Role 은 application 측 `states:StartExecution` 책임 / 실제 ARN / account-id / IAM access key id 본 spec 평문 기록 0건.
  - **[2026-06-29 (2) port-view Fargate Task Role 메모]** — Fargate 로 배포된 port-view 의 Task Role 은 `states:StartExecution` 권한을 특정 state machine ARN 한정으로 부여(`portfolio-paper-daily-step1-17-approval` 권장 / Resource · Action wildcard 0건 / OD-SEC-005 / OD-SEC-006 / R-AUTO-034 신규 mitigation 정합 / 06 spec 후속 phase 책임).
  - 실제 ARN / account-id / IAM access key id 본 spec 평문 기록 0건(`[REDACTED]` 또는 placeholder).
- 비용 발생: 무료.
- 조심할 점: 권한 누락 시 ECR pull, Secrets Manager 조회, CloudWatch Logs 송신이 모두 실패. 권한 과다 시 감사 위험. **[port-view 측 권한 과다 위험]** — Fargate View Task Role 의 `states:StartExecution` 권한이 광역(`Resource: *` 또는 다중 state machine) 으로 부여되면 의도하지 않은 state machine `StartExecution` 이 View 화면 또는 외부 POST 호출로 트리거될 위험(R-AUTO-034 신규 / Status `Open`). 본 권한 부여 시 Resource 패턴은 특정 ARN 한정 / IAM Policy 작성 시점에 cross-spec audit 필요.
- 관련 spec: 06.

### IAM Policy

- 한 줄 설명: IAM Role / User에 붙이는 권한 명세서(JSON).
- 역할: ECS Task Role에 ECR pull, Secrets Manager 조회(specific ARN), CloudWatch Logs PutLogEvents 등을 명시. **[2026-06-29 (2) port-view Fargate Task Role 메모]** — port-view Fargate Task Role 의 `states:StartExecution` IAM Policy 는 Resource 패턴을 특정 state machine ARN 한정으로 좁힌다 / 본 IAM Policy 전체 본문 / Resource ARN 평문 기록 0건(R-DOCS-001 정합 / 06 spec 후속 phase 책임).
- 비용 발생: 무료.
- 조심할 점: ARN 패턴이 너무 넓으면(`*`) 다른 환경 secret까지 읽을 수 있다. 환경별 prefix(`/portfolio/paper/...`)로 좁히기. **[port-view 측 정합]** — `states:StartExecution` 의 Resource 패턴이 wildcard 또는 광역 ARN 으로 부여되면 R-AUTO-034 위험이 즉시 발현 / Resource 패턴은 `arn:aws:states:{region}:{account-id}:stateMachine:portfolio-paper-daily-step1-17-approval` 형태로 한정 / account-id 부분은 본 spec 산출물에 평문 기록 금지(`[REDACTED]` 또는 placeholder).
- 관련 spec: 06.

### Instance Profile

- 한 줄 설명: EC2가 IAM Role을 사용하기 위한 wrapper.
- 역할: 마켓커넥터 EC2에 attach해 SSM Session Manager, Secrets Manager, CloudWatch Agent, S3 토큰 백업을 사용.
- 비용 발생: 무료.
- 조심할 점: 한 EC2당 1개. 변경 시 application 재인증이 필요할 수 있다.
- 관련 spec: 03, 06.

### CloudWatch Logs

- 한 줄 설명: AWS 관리형 로그 수집/저장 서비스.
- 역할: ECS Task 로그(`awslogs` driver), EC2 CloudWatch agent 로그, RDS PostgreSQL 로그(`/aws/rds/instance/{id}/postgresql`).
- 비용 발생: ingestion ~$0.76/GB, storage ~$0.04/GB-월. NAT 미사용 시 Logs Endpoint 비용 추가.
- 조심할 점: retention을 길게 두면 storage 비용이 누적. 본 spec은 aws-paper 7일, aws-live 14일 시작 권고.
- 관련 spec: 04, 05, 08, 09.

### CloudWatch Metrics

- 한 줄 설명: 시계열 모니터링 데이터.
- 역할: ECS / EC2 / RDS 표준 메트릭 + 도메인 custom metric(broker error rate, Daily Batch step 결과, intraday heartbeat 등).
- 비용 발생: AWS 표준 메트릭 무료. custom metric ~$0.30/metric/월.
- 조심할 점: custom metric을 무분별하게 만들면 비용이 누적. 운영 의사결정에 직접 쓰는 메트릭만 추가.
- 관련 spec: 04, 05, 08.

### CloudWatch Alarm

- 한 줄 설명: 메트릭 threshold 기반 경보.
- 역할: RDS 연결 수 폭주, ECS Task 실패, broker error rate 급증, intraday monitor heartbeat 끊김 등 감지.
- 비용 발생: standard alarm ~$0.10/alarm/월.
- 조심할 점: alarm을 너무 많이 만들면 알림 피로 → Slack 채널이 무시당함. 핵심 알람만 유지.
- 관련 spec: 05, 10.

### EventBridge Scheduler

- 한 줄 설명: cron 또는 rate 기반 스케줄러. Daily Batch / intraday polling 트리거.
- 역할: Daily Batch 는 EventBridge Scheduler → Step Functions → ECS RunTask 로 트리거. EC2/Spring 안의 subprocess 호출 구조를 대체. **2026-06-23 시점에 7번 EventBridge 자동화 구현 완료(OD-MS-032 신규 / 🟢 확정)** — 2개 schedule 분리 + Dispatcher Lambda 호출 + 단계적 활성화.
  - (a) `portfolio-paper-daily-step1-11-approval-0800-kst`(`cron(0 8 ? * MON-FRI *)` / Timezone `Asia/Seoul` / Flexible time window `OFF` / Target Lambda `portfolio-paper-daily-scheduler-dispatcher` / Target Role `portfolio-paper-eventbridge-scheduler-role` / Target input `{"scheduleType":"STEP1_11_APPROVAL","dryRun":false}` / **State `ENABLED`**).
  - (b) `portfolio-paper-daily-step12-17-order-0901-kst`(`cron(1 9 ? * MON-FRI *)` / Asia/Seoul / OFF / Target Lambda 동일 / Target input `{"scheduleType":"STEP12_17_ORDER","dryRun":false}` / **State `DISABLED`** / 주문 자동화 ENABLE 전 최종 안전 점검 후 별도 판단).
  - **[2026-06-24 1차 실 실행 검증 통과]** — 08:00 schedule 첫 실 호출 → Dispatcher Lambda → Step Functions `portfolio-paper-daily-step1-17-approval` execution 생성 → Step 1~11 수행 후 approval-required 흐름으로 완료 / `APPROVAL_REQUIRED` Slack 수신 / Step 12 이후 주문 차단 / 신규 주문 0건(R-AUTO-025 [2026-06-24 보강] 정합).
  - 09:01 schedule `DISABLED` 유지 — Step 12~17 수동 검증 성공 이후에도 자동 ENABLE 은 별도 운영자 승인 보류로 단계적 자동화 안전장치 유지(OD-MS-033 신규 정합 / `APPROVAL_REQUIRED` Slack summary 0/0 표시 후속(R-AUTO-027) 개선 전까지 신중히 판단).
  - **1차 Slack 범위 3종(`APPROVAL_REQUIRED` + `DAILY_EXECUTION_SUCCESS` + `DAILY_EXECUTION_FAILED`) 한정**(OD-MS-031 정합) / 장 전 잔고 · 장 후 잔고 · 장중 손절 알림은 후속 분리.
- 비용 발생: 월 14M invocations 무료. 본 프로젝트는 평일 2회(08:00 + 09:01) = 월 약 44회로 무료 한도 안에서 충분.
- 조심할 점: 스케줄 시각 / timezone 설정. cron 잘못 입력 시 batch 미실행. **Scheduler 자체는 KRX 휴장일을 모르므로 Dispatcher Lambda(`portfolio-paper-daily-scheduler-dispatcher`) 가 휴장일 / 주말 guard 단일 책임**(Lambda 환경변수 `TIMEZONE=Asia/Seoul` / `HOLIDAY_COUNTRY=KR` / `FAIL_CLOSED_ON_HOLIDAY_ERROR=true` fail-closed 정책 / OD-MS-032 정합).
  - Scheduler · Dispatcher Lambda · Step Functions 연결 사슬 실패 위험은 R-AUTO-025 신규 mitigation(simulate-principal-policy / Lambda dryRun / Scheduler `get-schedule` 상태 확인 / 단계적 활성화) 으로 1차 차단 + 2026-06-24 08:00 첫 실 실행 통과로 1차 실증.
  - 정기 트리거 cron 시각 / timezone / 휴장일 가드 결정은 04 / 10 spec 후속 phase 책임 — 본 일자 결정은 OD-MS-032 의 cron 표현식 + Asia/Seoul + 단계적 활성화 한정.
  - **[2026-06-24 EC2 lifecycle Scheduler 2개 추가]** — Daily orchestration Scheduler 2개 외에 EC2 lifecycle Scheduler 2개 추가 ENABLED: (a) **07:50 EC2 start Scheduler** `portfolio-paper-ec2-start-0750-kst`(`cron(50 7 ? * MON-FRI *)` / Asia/Seoul / Flexible OFF / Target Lambda `portfolio-paper-ec2-lifecycle-dispatcher` / Target input `{"action":"start","target":"BOTH","holidayCheck":true,...}` / `State ENABLED`).
  - (b) **15:50 MarketConnector stop Scheduler** `portfolio-paper-marketconnector-stop-1550-kst`(`cron(50 15 ? * MON-FRI *)` / Asia/Seoul / Flexible OFF / Target Lambda 동일 / Target input `{"action":"stop","target":"MARKETCONNECTOR","holidayCheck":false,...}` / `State ENABLED`).
  - 두 Scheduler 모두 ENABLED / start 요청에만 Lambda holiday guard 가 동작 / stop 요청에는 휴일 체크 미적용(이미 stopped 상태면 idempotent / OD-MS-034 신규 / R-AUTO-028 신규 정합).
  - **[2026-06-24 10분 장중 Snapshot Refresh Scheduler 후보 — 문서 반영 단계]** — 장중 영업시간 10분 간격으로 MarketConnector EC2 의 snapshot refresh 를 트리거하는 신규 Scheduler 가 후속 구현 예정(OD-MS-035 신규 / 🟢 확정 / 영향 spec 03 · 04 · 05 · 10).
  - 후보 이름 예: `portfolio-paper-intraday-snapshot-refresh-10min-kst` / 휴일 · 주말 skip 은 Dispatcher Lambda guard 동일 패턴 / `TIMEZONE=Asia/Seoul` · `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true` / 정식 cron 표현식 + flexible time window 결정은 후속 phase / 실제 Scheduler 생성 0건 / 본 메모는 결정 락만 다룸.
- 관련 spec: 04 strategy-batch-stepfunctions, 10 cutover-and-validation-runbook.
- **[2026-07-01 Step 12~17 Scheduler ENABLED 운영 예시]** — 본 일자부터 `portfolio-paper-daily-step12-17-order-0901-kst` 가 DISABLED → ENABLED 전환. cron `cron(1 9 ? * MON-FRI *)` / Asia/Seoul / Target Lambda `portfolio-paper-daily-scheduler-dispatcher` / Target Input `{"scheduleType":"STEP12_17_ORDER","dryRun":false}`.
  - Scheduler → Dispatcher Lambda → Step Functions `portfolio-paper-daily-step12-17-approval` 흐름으로 후보가 있는 경우 View 수동 승인 없이 broker 주문 제출까지 자동 진행 / 후보 없는 경우 NO_TARGET 안전 종료(OD-MS-033 승격 조건 통과 evidence / R-AUTO-025 [2026-07-01 자동 ENABLE 진입] + R-AUTO-037 신규 mitigation 정합 / aws-live 정책 변경 없음).
  - 08:00 Step 1~11 approval Scheduler + 09:01 Step 12~17 order Scheduler 2개 + EC2 lifecycle Scheduler 2개(07:50 start / 15:50 stop) + Daily Brief Slack Scheduler 2개(07:50 장전 / 15:50 장후) + 10분 장중 손절 Scheduler 1개 총 7종이 aws-paper Daily 자동화 라인업으로 ENABLED.
  - Scheduler 자체 비용은 무료 한도 안(평일 09:01 1회 = 월 약 22 invocations / 라인업 전체 합산도 14M 무료 한도 안). ENABLED / DISABLED 전환 절차는 [`../05-port-view-ecs-and-runbook/runbook.md`](../05-port-view-ecs-and-runbook/runbook.md) Step 12~17 Scheduler enable/disable 운영 절차 참조.
- **[2026-06-30 (오후) Daily Brief Scheduler 2개 ENABLED 운영 예시]** — Daily 본 실행 Scheduler 2개 + EC2 lifecycle Scheduler 2개와 별도로 Daily Brief Slack 전용 Scheduler 2개 ENABLED 추가(OD-MS-038 신규 / R-AUTO-035 신규 mitigation 1차 통과).
  - (a) 장전 `portfolio-daily-brief-morning-slack-0750-kst` — `cron(50 7 ? * MON-FRI *)` / Timezone `Asia/Seoul` / Flexible time window `OFF` / Target Step Functions `portfolio-daily-brief-slack-notification` StartExecution / Target IAM Role `portfolio-daily-brief-scheduler-role` / Target input eventType `MORNING_BRIEF` / **State `ENABLED`**.
  - (b) 장후 `portfolio-daily-brief-evening-slack-1550-kst` — `cron(50 15 ? * MON-FRI *)` / Asia/Seoul / OFF / 동일 Target 구조 / Target input eventType `EVENING_BRIEF` / **State `ENABLED`**.
  - Scheduler input 에 고정 `runDate` 미주입 / Builder Lambda 가 실행 시점 KST 기준 처리 / MarketConnector EC2 start · stop 자동화(07:50 / 15:50) 와 시간대 동일하지만 책임 · Target · Lambda · IAM Role 모두 독립 / Daily Brief Slack 실패가 Daily 본 실행에 영향을 주지 않는다.
  - 첫 실 자동 발사 검증은 다음 평일 후속(followups-overview 2026-06-30 (오후) Slack 후속 메모 정합).

### Step Functions

- 한 줄 설명: AWS 관리형 워크플로 orchestration. state machine.
- 역할: Daily Batch 파이프라인의 여러 ECS Task 를 순서대로 실행, 실패 시 분기. live 자동 재시도 금지 정책을 state machine 레벨에서 강제. **state machine `portfolio-paper-daily-step1-17-approval` 의 false / true path 운영 절차(OD-MS-029) + Step 12 시작부 retry-normalizer 정합(OD-MS-028) + Catch 경로에서 AWS 공통 Slack notifier Lambda(`portfolio-event-notifier`) 의 `DAILY_EXECUTION_FAILED` 발송 정합(OD-MS-031 / R-AUTO-023)**.
- 비용 발생: Standard ~$0.025 / 1,000 transitions. Express 는 다른 단가. 본 프로젝트는 Standard 시작.
- 조심할 점: BUY/SELL/fill sync/position 변경/intraday stop SELL 생성 step 에는 Retry 정책을 비활성화. idempotent step 만 자동 재시도 허용. **Catch 경로의 Slack notifier invoke 매핑 누락 시 운영자가 실패를 즉시 인지하지 못할 위험(R-AUTO-023) — Step Functions execution history 의 Catch state audit 로 사후 검증 가능 / DLQ · retry · CloudWatch Alarm 도입은 후속**.
  - **[2026-06-24 1차 실 실행 검증 통과]** — `portfolio-paper-daily-step1-17-approval` 실 execution 생성 → Step 1~11 수행 후 approval-required 흐름 완료 / `portfolio-paper-daily-step12-17-approval` 수동 Dispatcher invoke 로 Step 12~17 실행 + 4건 전량체결 + `DAILY_EXECUTION_SUCCESS` Slack 수신(OD-MS-033 / R-AUTO-026 mitigation 1차 실증).
  - **[2026-06-24 Step 1~11 success path 에 Crawler stop task 추가]** — `portfolio-paper-daily-step1-17-approval` state machine 의 `Step6ToStep11_Succeeded` 다음에 `StopCrawlerEc2AfterStep11Success` task state 신규 삽입 → `SendApprovalRequiredSlack` → `Step12_CheckApproval` 흐름 / Step 1~11 성공 시 Crawler EC2 stop 자동 실행 / Step 1~11 실패 시 Crawler EC2 디버깅 위해 유지 + `SendDailyExecutionFailedSlack` 경로 /
  - Approval Slack 은 Crawler stop 완료 후 발송 / RevisionId `edd92cc9-1d94-4752-9a43-b7eb5b2f3c2c` / ASL 백업본 보유 / State Machine `ACTIVE`(OD-MS-034 신규 / R-AUTO-028 신규 정합).
  - **[2026-06-24 Intraday Stop Sell Submit & Refresh 별도 state machine 후보 — 문서 반영 단계]** — 장중 포지션 확인 3단계(Intraday Stop Sell Submit & Refresh)는 Daily Step 1~17 state machine 과 분리된 **별도 Step Functions state machine** 으로 후속 구현 예정(OD-MS-035 신규 / 🟢 확정 / 영향 spec 03 · 04 · 05 · 10).
  - 첫 task 는 `source_type=INTRADAY_STOP_SELL` + `execution_status=READY` + `connector_order_request_id IS NULL` 필터 / 다음 task 는 broker 제출 + 체결조회 + 포지션 sync + 잔고 refresh + Slack 알림(`INTRADAY_STOP_SELL_SUBMITTED` 또는 동등 eventType).
  - **초기에는 자동 ENABLE 보류 + 수동 · 승인 후 실행 정책**(OD-MS-033 의 09:01 보류 패턴과 동일한 안전 게이트 / R-AUTO-030 신규). 실제 state machine 정의 / IAM execution role / Slack 연계는 후속 phase 책임 / 본 메모는 결정 락만 다룸.
  - **[2026-06-29 (2) port-view 외부 caller 1차 실증 메모]** — port-view 가 Step Functions `StartExecution` external caller 로 붙는 첫 local 검증 통과(commit `e72de6f` / `aws-stepfunctions` mode 분리 / `StepFunctionsDailyBatchExecutionService` 신규).
  - 외부 caller 가 `StartExecution` 입력에 **`runDate`(Asia/Seoul yyyy-MM-dd) 필수** — ASL 의 `runDate.$=$.runDate` 참조 state(예: `StopCrawlerEc2AfterStep11Success`) 가 input 없이 진입하면 `States.Runtime` 발생(View 1차 검증에서 식별 후 보완 / 04 spec 후속 갱신 책임).
  - Step 1~11 → `StopCrawlerEc2AfterStep11Success` → `SendApprovalRequiredSlack` → Slack `APPROVAL_REQUIRED` 수신 end-to-end 통과 / Step 12~17 차단 유지 / broker 주문 제출 0건 / 신규 `connector_order_request` 0건.
  - View 의 안전 gate(`canStartAwsStepfunctions` / `hasRunningBatch` / `stateMachineArn` 빈 값 / `min-` · `max-executable-step-order` / `allowPaperOrderExecute=false` 상태 Step 12 이상 / approval 요청 `paperOrderEnabled=true` 외) 가 서비스 레벨에서 모두 차단(R-AUTO-033 [2026-06-29 보강 (2)] / R-AUTO-034 신규 mitigation 정합 / 05 spec `operation-notes.md` 6 · 7 섹션 / 06 spec Task Role `states:StartExecution` 최소 권한 후속 책임).
  - **[2026-06-29 (3) port-view Step 12~17 approval workflow 외부 caller 1차 실증 메모]** — Local View 가 일반 workflow ARN(`portfolio-paper-daily-step1-17-approval`) + approval workflow ARN(`portfolio-paper-daily-step12-17-approval`) 2종 state machine 에 외부 caller 로 붙는 두 번째 phase 검증 통과(executionName `port-view-step12-17-step12-17-20260629-194314-ba5edaf8` / status `SUCCEEDED` / 운영 marker `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS`).
  - 외부 caller 가 approval workflow 에 진입할 때 `Step12_CheckApproval` Choice `BooleanEquals` 조건과 맞도록 payload 의 **`allowPaperOrderExecute` · `paperOrderEnabled` 는 boolean** / **`fromStepOrder` · `toStepOrder` · `startStep` · `endStep` 는 numeric** 으로 전달해야 함(View 1차 검증에서 string boolean 차단 사례 식별 후 보완 / 04 spec 후속 갱신 책임).
  - `Step12_CheckApproval` 통과 → `Step12_RunMarketConnectorStrategyOrderExecute` → `Step12_GetCommandInvocation` → Step 13~17 전체 진행 → `ExecutionSucceeded` 흐름 정합 / DB 신규 broker 주문 0건.
  - Fargate Task Role 의 `states:StartExecution` Resource 패턴은 **일반 ARN + approval ARN 2종 모두 한정** 부여 필요(Resource · Action wildcard 0건 유지 / 06 spec 후속 phase 책임 / R-AUTO-034 mitigation 결합).
  - **[2026-06-30 (오후) 장중 손절 approval gate 분리 구조 evidence 보강]** — `portfolio-paper-intraday-stop-sell-approval` state machine 은 본 일자 오후 추가 작업분(MarketConnector EC2 runner 측 Slack 발송 부착) 이후에도 broker 주문 제출 책임 분리 구조를 그대로 유지(OD-MS-035 / OD-MS-036 정합).
  - 장중 손절 흐름은 (a) MarketConnector EC2 runner 가 `INTRADAY_STOP_SELL` `READY` 생성 + Notifier Lambda invoke 까지 책임, (b) `portfolio-paper-intraday-stop-sell-approval` state machine 의 `CheckIntradayStopApproval` Choice 통과 후 broker 주문 제출 책임 으로 2단계 분리 / state machine 자체 ASL · IAM Role · 자동 ENABLE 정책 본 일자 변경 0건(자동 ENABLE 진입은 여전히 후속 phase / OD-MS-036 / R-AUTO-030 ~ R-AUTO-032 / R-BROKER-005 정합 그대로 유지).
  - 실제 broker 주문 제출은 본 state machine 의 `allowIntradayStopOrderExecute=true` 입력 + 운영자 별도 승인 후에만 가능.
  - **[2026-06-30 (오후) Approval Required Builder 연동 + Daily Brief mini state machine 신규]** — Daily 본 실행 state machine `portfolio-paper-daily-step1-17-approval` 안에 Approval Required Slack Builder 연동 추가 — `StopCrawlerEc2AfterStep11Success → BuildApprovalSlackPayload → SendApprovalRequiredSlack → Step12_CheckApproval` 흐름 / Builder Lambda `portfolio-approval-slack-summary-builder` 호출 /
  - RevisionId `da8642c6-8409-41b6-ad57-e066ff672332` / Builder output → Notifier Slack smoke 성공(`marketStatusCode=BLOCK` / 매수·매도 후보 없음 표시).
  - Daily Brief 알림은 Daily 본 실행 state machine 과 분리된 **별도 mini state machine `portfolio-daily-brief-slack-notification`(ACTIVE / 구조 `BuildDailyBriefPayload → SendSlackNotifier`)** 으로 운영(OD-MS-038 신규 / R-AUTO-035 신규 mitigation 1차 통과 / Builder Lambda `portfolio-daily-brief-slack-summary-builder` + Notifier Lambda `portfolio-event-notifier` 호출 / IAM Role `portfolio-daily-brief-sfn-role` 책임).
  - 분리 이유 = 알림 실패가 Daily 본 실행에 영향을 주지 않도록 격리 + 알림 측 ASL 변경이 본 실행 ASL 에 영향을 주지 않도록 격리. Manual smoke morning `daily-brief-morning-smoke-safe-20260630-193255-68f50aeb` + evening `daily-brief-evening-smoke-safe-20260630-193300-aa2b9a12` 모두 `SUCCEEDED` / Slack 수신 확인.
- 관련 spec: 04, 09, 10.

### Lambda

- 한 줄 설명: 서버리스 함수 실행.
- 역할: 인프라 알람 SNS → Lambda → Slack webhook fan-out, 짧은 후처리(예: Daily Batch 결과 요약).
  - **AWS 공통 Slack notifier(`portfolio-event-notifier` / Python 3.12 / IAM Role `portfolio-event-notifier-lambda-role` / 2026-06-23 1차 검증 통과) 의 단일 진입점 — Step Functions / EventBridge / EC2 SSM / Batch / Lambda 어디서든 호출 가능한 운영 이벤트 알림 보조 계층**(OD-MS-030 신규 정합).
  - **Daily 자동화 Dispatcher** (`portfolio-paper-daily-scheduler-dispatcher` / Python 3.12 / Timeout 30s / Memory 256MB / 2026-06-23 dryRun 검증 통과) — EventBridge Scheduler 가 호출, KST `runDate` 생성, 주말/휴장일 skip, scheduleType 별 payload 분기, Step Functions `StartExecution` 호출 후 즉시 종료 (OD-MS-032 정합).
  - Selenium / Chrome / KRX 로그인은 Lambda 비권고(컨테이너 한계, cold start, 세션 stateful). **Lambda 는 본 프로젝트의 8개 MS 의 주 compute 1순위가 아님 — 운영 이벤트 알림 / 운영 자동화 dispatcher / 짧은 후처리 / 인프라 알람 fan-out 보조 서비스로만 사용**(ms-aws-service-decision-matrix 본문의 Lambda 비권고 정책 정합 / OD-MS-010 / OD-MS-030 / OD-MS-032 정합).
- 비용 발생: 월 1M requests + 400,000 GB-sec 무료. 본 프로젝트 인프라 알람 fan-out + Slack notifier 운영 이벤트 알림 + Daily 자동화 dispatcher(평일 2회 = 월 약 44회)는 모두 무료 한도 안.
- 조심할 점: VPC 연결 시 cold start 증가. crawler 처럼 Selenium 의존 워크로드는 Lambda 부적합. **Slack notifier 의 webhook URL 은 secret 으로 취급 — 현재 Lambda 환경변수 `SLACK_WEBHOOK_URL` 로 1차 검증 / 운영 안정화 후 Secrets Manager 또는 SSM Parameter Store(SecureString) 로 이전 예정(R-AUTO-024 신규 / Status `Accepted`)**.
  - **Daily 자동화 Dispatcher Lambda 의 환경변수(`TIMEZONE` / `HOLIDAY_COUNTRY` / `FAIL_CLOSED_ON_HOLIDAY_ERROR` / Step Functions ARN 2종)는 운영 식별자(이름 / 값 의미)만 본 spec 문서에 기록 — ARN 평문 / 환경변수 value 평문 기록 0건**(R-DOCS-001 정합).
  - Scheduler · Dispatcher Lambda · Step Functions 연결 사슬 실패 위험은 R-AUTO-025 신규 mitigation 으로 1차 차단 + 2026-06-24 08:00 첫 실 실행 통과로 1차 실증.
  - **[2026-06-24 실증 메모]** — Dispatcher Lambda 가 scheduleType 에 따라 (a) `STEP1_11_APPROVAL` → Step1~11 approval workflow 또는 (b) `STEP12_17_ORDER` → Step12~17 order workflow 로 분기 / 08:00 schedule 실 호출 + 09:01 수동 Dispatcher invoke 두 경로 모두 정상 동작 / 09:01 schedule 자동 ENABLE 별도 운영자 승인 보류로 단계적 자동화 안전장치 유지(OD-MS-033 신규 정합).
  - **[2026-06-24 Lambda 역할 3분리 완료]** — (a) `portfolio-paper-daily-scheduler-dispatcher`(08:00 / 09:01 Step Functions schedule dispatcher), (b) `portfolio-paper-ec2-lifecycle-dispatcher`(07:50 / 15:50 EC2 start · stop dispatcher / Step Functions Step 1~11 성공 시 Crawler stop 책임 / start 요청에만 휴일 체크 / stop 요청 미적용 /
  - `TIMEZONE=Asia/Seoul` · `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true`), (c) `portfolio-event-notifier`(Slack 알림 전담 / `APPROVAL_REQUIRED` + `DAILY_EXECUTION_SUCCESS` + `DAILY_EXECUTION_FAILED` + 향후 `PRE_MARKET` · `POST_MARKET` · `INTRADAY_STOP_LOSS`).
  - Lambda 는 본 프로젝트의 8개 MS 주 compute 가 아니라 운영 orchestration dispatcher / notifier / lifecycle 보조 계층 유지(OD-MS-034 신규 / R-AUTO-028 신규 / R-AUTO-025 [2026-06-24 두 번째 보강] 정합).
  - **[2026-06-24 Intraday snapshot refresh orchestration helper 후보 — 문서 반영 단계]** — 장중 포지션 확인 1단계용 신규 Dispatcher Lambda(예: `portfolio-paper-intraday-snapshot-refresh-dispatcher` 후보) 가 후속 구현 예정(OD-MS-035 신규 / 🟢 확정 / 영향 spec 03 · 04 · 05 · 10) — EventBridge Scheduler → Lambda → SSM → MarketConnector EC2 흐름의 SSM 호출 단계에서 holiday guard + scheduleType 분기 + SSM `SendCommand` 호출 책임 /
  - Lambda 는 본 spec 의 주 compute 가 아니라 orchestration 보조 계층이라는 기존 판단 유지(OD-MS-009 / OD-MS-030 / OD-MS-032 / OD-MS-034 본문 변경 없음) / 정식 Lambda 생성 0건 / 본 메모는 결정 락만 다룸.
  - webhook URL 평문은 본 spec 문서 / 운영자 노트 / Lambda 콘솔 캡처 / CloudWatch Logs / Slack 메시지 본문 / Step Functions execution history 에 기록 금지(R-DOCS-001 정합 / 모두 `[REDACTED]` 또는 placeholder).
  - **[2026-06-30 (오후) 장중 손절 Slack 실 연동 — MarketConnector EC2 → Notifier Lambda invoke 경로 추가]** — Notifier Lambda `portfolio-event-notifier` 호출 진입점이 본 일자 오후 추가 작업분으로 MarketConnector EC2 runner 까지 확대(OD-MS-030 / OD-MS-035 / OD-MS-036 / OD-MS-038 evidence 보강 / R-AUTO-035 [2026-06-30 오후 추가 보강] / R-AUTO-036 신규).
  - 진입점은 (i) EventBridge Scheduler 2개(Daily Brief 장전 / 장후) + (ii) Step Functions Daily 본 실행 ASL(`portfolio-paper-daily-step1-17-approval` Approval Required) + Daily Brief mini state machine + (iii) **MarketConnector EC2 runner `/home/ec2-user/apps/port-marketconnector/scripts/run_intraday_snapshot_and_evaluate.sh` → `connector_intraday_position_evaluate.py --notify-slack` 호출** 3종으로 운영.
  - eventType `INTRADAY_STOP_LOSS` formatter 지원 / Slack 문구 `🚨 [장중 손절]` / `evalProfitRate` `%` suffix / 로컬 smoke `🔵 -4.2%` 표시 / EC2 IAM invoke smoke 수신 확인.
  - MarketConnector EC2 IAM 권한 — role `portfolio-paper-marketconnector-ec2-role` 에 inline policy `portfolio-paper-marketconnector-event-notifier-invoke`(`lambda:InvokeFunction` / Resource `portfolio-event-notifier` 한정 / Resource · Action wildcard 0건 / OD-SEC-005 / OD-SEC-006 정합) 부여.
  - broker 주문 제출은 `portfolio-paper-intraday-stop-sell-approval` Step Functions approval gate 통과 후에만 가능 — 분리 구조 그대로 유지(OD-MS-035 / OD-MS-036 정합).
  - Lambda code 본문 / IAM Policy 전체 본문 / `connector_intraday_position_evaluate.py` 본문 / runner ps1 본문 / Slack 메시지 본문 / SSM 응답 본문 평문 인용 0건(R-DOCS-001 정합 / 운영 식별자 만 사실 기록).

**[2026-07-03 Step Functions 실행 이력 OPS mirror Recorder Lambda 신규]** — Daily Batch 화면이 AWS Step Functions 실행 이력을 볼 수 있는 기반을 마련하기 위한 Recorder Lambda 1종이 운영자 직접 작업으로 추가(OD-MS-039 신규 · OD-DB-012 신규 · R-AUTO-038 신규 Mitigated 정합).

- (d) `portfolio-daily-batch-ops-recorder` — `portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval` 2종 State Machine 안 mirror step 에서 호출 / Python 3.12 · ap-northeast-2 · VPC Lambda / Secrets Manager `ops_recorder_app` secret 사용 / 지원 action `RECORD_START` · `RECORD_STEP` · `RECORD_SUCCESS` · `RECORD_FAILURE` / `ops.strategy_daily_batch_run` + `ops.strategy_daily_batch_step_log` writer 책임.
  - DB role 은 `ops_recorder_app` 전용 최소 권한(`ops` schema USAGE · run · step_log SELECT / INSERT / UPDATE · 관련 sequence USAGE / SELECT · DELETE 미부여) — `view_app` 재사용 안 함 · `execution_app` 권한 확대 안 함 · 업무 테이블 write 권한 확대 0건 · View 는 reader / controller 역할 유지(OD-DB-012 / OD-SEC-006 정합).
  - Step Functions 실행 role 에 `lambda:InvokeFunction` 을 본 Recorder Lambda ARN 한정으로 부여 · Resource · Action wildcard 0건.
  - `chk_strategy_daily_batch_run_type` 에 `AWS_STEPFUNCTIONS` 값 추가 · 기존 `MANUAL` / `SCHEDULED` / `RETRY` / `MANUAL_PARTIAL` 값 유지.
  - 1차 범위 = 전체 세부 step mirror 가 아니라 run-level + 대표 workflow step 중심. 세부 step 확장은 후속(followups-overview 2026-07-03 후속 반영).
  - 검증 요약 — RECORD_START 응답 `ok true` · RECORD_STEP 응답 `ok true` + `stepLogId` 생성 · RECORD_SUCCESS 응답 `ok true` · commit smoke `batchRunId=53` · Step 12 approval-blocked smoke 후 `ops.strategy_daily_batch_run` + step log 기록 확인 통과.
  - Lambda code 본문 / Secrets Manager value / RDS 응답 본문 / Step Functions execution history 본문 / IAM Policy 본문 평문 인용 0건(R-DOCS-001 정합 / 운영 식별자 = Lambda 이름 · runtime · DB role 이름 · action 이름 · 테이블 이름 · check constraint 이름 · smoke `batchRunId=53` 만 사실 기록 — secret 아님).

**[2026-06-30 (오후) Approval Required Builder + Daily Brief Builder 신규 Lambda 2종]** — Daily Batch 측 Slack 알림을 풍부한 메시지로 보내기 위한 Builder Lambda 2종이 운영자 직접 작업으로 추가(OD-MS-038 신규 / R-AUTO-035 신규 정합).

- (a) `portfolio-approval-slack-summary-builder` — `portfolio-paper-daily-step1-17-approval` 안의 `BuildApprovalSlackPayload` state 에서 호출 / RDS read 책임(전략 상태 · Daily 매수 신호 · Daily 포지션 판단 · 매수/매도 후보 요약) / Builder output 을 Notifier Lambda `portfolio-event-notifier` 의 `APPROVAL_REQUIRED` 입력으로 전달.
- (b) `portfolio-daily-brief-slack-summary-builder` — mini state machine `portfolio-daily-brief-slack-notification` 안의 `BuildDailyBriefPayload` state 에서 호출 / Python 3.12 + `pg8000` + Secrets Manager `valueFrom` (`DB_PASSWORD_SECRET_VALUE_FROM` 환경변수) 방식 / `psycopg2` 미사용.
  - View 잔고·보유종목 조회 기준: 최신 `connector_balance_snapshot` + 같은 `as_of_date` `connector_position_snapshot` + `quantity > 0` 만 + 평가금액 내림차순 + ticker code 오름차순 + null/blank `-` 처리 / `MORNING_BRIEF` · `EVENING_BRIEF` 2종 eventType 지원.
- (c) `portfolio-event-notifier` 측 formatter 개선 — eventType alias 2종(`MORNING_BRIEF → PRE_MARKET_STATUS` / `EVENING_BRIEF → POST_MARKET_STATUS`) + nested `balance` / `positions` adapter + Builder `title` 우선 + 장후 `어제 대비` 표시 + 손익 prefix 규칙(음수 `🔵` / 양수 `🔴` / 0 `⚪`) 일관 적용(누적 수익률 · 평가손익 · 어제 대비 · 보유종목 평가손익 · 보유종목 평가손익률 모두).
- Lambda 코드 본문 / Builder output 전문 / Notifier input 전문 / Slack 메시지 본문 / CloudWatch Logs 전문 / IAM Policy 본문 평문 인용 0건(R-DOCS-001 정합 / 운영 식별자 = Lambda 이름 · Lambda runtime · DB driver · DB password 주입 방식 라벨 · Slack webhook 환경변수명 · smoke status · smoke execution name 만 사실 기록 — secret 아님).
- 관련 spec: 04, 05, 08, 10.

### S3 (Simple Storage Service)

- 한 줄 설명: 오브젝트 스토리지.
- 역할: KIS access_token EC2 로컬 백업 보관, port_strategy_research 텍스트 리포트 저장, RDS 외부 dump 보관.
- 비용 발생: Standard storage ~$0.025/GB-월. Gateway endpoint로 무료 outbound 가능.
- 조심할 점: 버킷 public access는 기본 차단 유지. 환경별 prefix(`portfolio/paper/...`)로 분리.
- 관련 spec: 03, 09.

### AWS Batch

- 한 줄 설명: 장시간 / 가변 자원 batch job 실행 서비스.
- 역할: `port_strategy_research` 백테스트 1순위 후보. Step Functions에서 호출 가능.
- 비용 발생: 내부 compute env(Fargate 또는 EC2) 단가 그대로. AWS Batch 자체 단가 없음.
- 조심할 점: job queue 우선순위, 동시 실행 수 제한, 장시간 실행 중 RDS connection 누수 점검.
- 관련 spec: 09 strategy-research-batch.

### KMS (Key Management Service)

- 한 줄 설명: 암호화 키 관리 서비스.
- 역할: RDS encryption at rest, Secrets Manager, S3 SSE-KMS, ECR 이미지 암호화 옵션의 키 보관.
- 비용 발생: CMK $1.0/월/key + API calls. AWS 관리형 키는 무료.
- 조심할 점: aws-paper는 KMS default 권고. aws-live는 CMK 권고이나 IAM Policy에서 KMS 권한을 빠뜨리면 암호화/복호화 실패.
- 관련 spec: 02, 06.

### Cloud Map / Service Discovery

- 한 줄 설명: ECS 서비스 이름으로 내부 DNS 이름을 자동 등록하는 service discovery.
- 역할: port-view ECS Service에서 marketconnector EC2 또는 다른 ECS Service에 ALB 없이 내부 호출. ALB 비용을 줄이는 1차 대안.
- 비용 발생: 매우 작음. 등록된 서비스 / 호스팅 영역 기준.
- 조심할 점: DNS TTL 짧게 유지. 새 Task 기동 시 IP 변경 반영 시간을 application 측이 견딜 수 있어야 한다.
- 관련 spec: 05.

### SSM RunCommand

- 한 줄 설명: AWS Systems Manager 의 원격 명령 실행 기능. EC2 / on-prem 에 대해 `AWS-RunPowerShellScript` / `AWS-RunShellScript` 등 document 로 명령 실행.
- 단가 가정: 사실상 0(API 호출 비용 없음). SSM Endpoint(VPC Endpoint) 비용은 OD-NET-005 권고 세트에 이미 포함.
- 운영 주의: 본 프로젝트는 (a) MarketConnector EC2 의 `CONNECTOR_BALANCE` 등 단발 명령 진입점, (b) Windows EC2 worker 의 KRX worker Scheduled Task trigger(`schtasks /Run /TN "Portfolio-KRX-Worker-Daily"`) 진입점으로 사용. 직접 wrapper 또는 Python 실행은 SYSTEM Session 0 / 비대화형 GUI 한계로 KRX GUI 로그인에 부적합(OD-MS-022 정합).
  - **[2026-06-24 장중 포지션 확인 SSM 제어 경로 추가 — 문서 반영 단계]** — 장중 포지션 확인 1단계(MarketConnector 10분 Snapshot Refresh)와 2단계(StrategyExecution Intraday Evaluate) 모두 MarketConnector EC2 의 venv python entrypoint 를 SSM RunCommand 로 실행하는 제어 경로 채택(OD-MS-035 신규 / 🟢 확정 / 영향 spec 03 · 04 · 05 · 10).
  - 1단계 = snapshot refresh entrypoint(`connector_balance_snapshot` + `connector_position_snapshot` 갱신 / broker 호출 + DB write 까지 idempotent) / 2단계 = StrategyExecution 신규 evaluate entrypoint(`strategy_intraday_position_check` 저장 + `INTRADAY_STOP_SELL` `READY` order 생성 / broker 주문 제출 0건).
  - `daily_intraday_position_monitor_run.py` 수정 없이 신규 파일로 evaluate 구현. 실제 SSM Document / IAM policy / 정식 명령 본문 작성은 후속 phase 책임 / 본 메모는 결정 락만 다룸.
- 관련 spec: 03, 08.

### Windows Scheduled Task

- 한 줄 설명: Windows OS 내장 작업 스케줄러. 등록된 task 를 시간 trigger 또는 외부 trigger(`schtasks /Run`) 로 실행.
- 단가 가정: 0(Windows EC2 OS 기본 기능).
- 운영 주의: 본 프로젝트는 Windows EC2 worker 에서 KRX GUI worker 의 Administrator interactive session 실행 트리거로 사용. SSM RunCommand 가 SYSTEM Session 0 에서 실행되더라도 Scheduled Task 가 Administrator console interactive session 위에서 wrapper 를 실행하므로 Chrome GUI / Display 컨텍스트 확보 가능(OD-MS-015 / OD-MS-022 정합). Logon Mode `Interactive only`, Run As User `Administrator` 기준.
- 관련 spec: 08.

### Windows Autologon (Sysinternals)

- 한 줄 설명: Microsoft Sysinternals 도구. Windows 부팅 직후 지정 사용자(보통 Administrator)로 자동 로그인을 활성화해 console interactive session 을 자동 생성.
- 단가 가정: 0(도구 자체 비용 없음 / Windows EC2 OS 안에서만 동작).
- 운영 주의: **paper 전용 Windows worker 보안 예외**(OD-MS-022 / R-SEC-009 정합). 자동 로그인 자격 증명이 Windows registry / LSA secret 영역에 저장되어 관리자 권한 보유자에 의해 복호화될 가능성 존재 — Administrator password 는 본 spec 산출물 / 콘솔 캡처 / 로그 평문 기록 금지(`[REDACTED]` 만). RDP inbound 는 운영자 IP 한정 또는 SSM Session Manager 우선 사용. EC2 worker 작업 완료 후 stop 절차로 idle 노출 시간 최소화. 추후 전용 local user(예: `krxworker`) 로 전환 검토 / aws-live 적용은 별도 결정.
- 관련 spec: 08.

---

## Usage Notes

특정 날짜 운영 실증 메모는 개별 용어 본문에서 분리해 이 섹션에 정리한다. AWS 개념 이해는 상단 `Glossary` 참조, 구체 실행 evidence 는 각 spec `operation-notes.md` 상대경로 링크로 대체(raw log · full executionName · SHA256 hex · Slack payload 인용 금지).

이관 우선순위(가장 활발하게 evidence 가 누적되는 서비스 순):

- EventBridge Scheduler → Daily Brief Scheduler ENABLED · Step 1~11 / Step 12~17 자동화 예시.
- Lambda → Approval Lambda · Daily Brief Lambda 실제 운영 사례.
- IAM Role → Task Role · Execution Role · Scheduler Role 실제 운영 사례.
- ECS/Fargate → Fargate Task Definition · service · task 실행 이력.
- Step Functions → Step 1~11 / Step 12~17 State Machine 실행 이력.

본 9차 회차에서는 상단 골격 재편 · 카테고리 H3 강등 · 용어 H3 정렬을 우선 완료했고, 각 서비스별 실증 메모의 세부 이관은 후속 회차에서 이어간다. 개별 서비스 본문에 이미 있는 특정 날짜 evidence 는 원문 그대로 유지되며 삭제하지 않는다.

## Update Rules

본 문서를 갱신할 때 반드시 따라야 하는 규칙이다.

- 새 AWS 서비스 · 리소스 용어가 필요하면 해당 카테고리(Network · Compute · Database · Security / IAM / Secrets · Orchestration · Observability · Storage / Artifact) H3 아래 새 H3 항목으로 추가한다.
- 각 용어 항목은 5-field template(설명 · 프로젝트 역할 · 비용 · 주의 · 관련 spec) 을 유지한다.
- 같은 AWS 서비스가 여러 spec 에서 사용되면 중복 항목을 만들지 않고 한 항목의 `프로젝트 역할` 필드에 통합 정리한다.
- 특정 날짜 운영 실증 evidence 는 개별 용어 본문에 추가하지 않고 `Usage Notes` 로 이관한다.
- 용어 항목명 · 관련 spec 번호(01 ~ 10) · Scheduler / State Machine / Lambda / IAM Role 이름 · 3개 환경 모델(`local-dev` · `aws-paper` · `aws-live`) 문자열은 원문 그대로 유지한다.

## Security Notes

- 본 문서는 후속 spec(03 ~ 10)에서 입력으로 사용할 용어집이다.
- 본 문서 작성 시 실제 AWS 리소스를 만들거나 변경하지 않았다.
- 8개 MS의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog는 수정하지 않았다.
- 모든 secret 자리에 `[REDACTED]`만 사용했다. 실제 password / token / app key / app secret / 계좌번호 / webhook URL 값은 본 문서에 들어 있지 않다.
- 06-secrets-and-iam은 본 작업 시점에 작성하지 않는다.
