# AWS Resource Glossary — AWS Migration

## Purpose

본 문서는 PORT-STRATEGY-AI AWS Migration에서 사용하는 AWS 서비스와 리소스의 의미를 빠르게 확인하기 위한 공통 용어집이다.

각 용어는 아래 5개 항목을 동일한 2열 표로 정리한다.

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 서비스 또는 리소스의 기본 의미 |
| 역할 | PORT-STRATEGY-AI에서 사용하는 목적 |
| 비용 발생 | 무료 여부와 주요 과금 기준 |
| 주의 | 운영 시 확인할 핵심 사항 |
| 관련 spec | 연관된 spec 번호 |

날짜별 작업 이력, 실행 이름, ARN, Task Definition revision, smoke 결과, Risk·Decision 보강 내역은 용어 설명에서 제외한다. 이러한 운영 근거는 `WORKLOG.md`, 해당 spec의 `operation-notes.md`, `operator-decisions.md`, `risk-register.md`에서 관리한다.

민감정보 원문은 기록하지 않고 `[REDACTED]` 계열 placeholder만 사용한다.

## 운영 요약

| 항목 | 값 |
| --- | --- |
| 기준 환경 | `local-dev` · `aws-paper` · `aws-live` |
| AWS Region | 서울 `ap-northeast-2` |
| 네트워크 원칙 | paper · live 모두 NAT Gateway 기본 미사용 |
| 외부 outbound | Public Subnet + 필요한 경우 Public IP |
| RDS | Private Subnet 배치 · SG 참조 기반 접근 |
| MarketConnector | EC2 + EIP |
| port-view | ECS Fargate Service |
| Daily Batch | ECS Fargate Task + Step Functions + EventBridge Scheduler |
| KRX GUI | Windows EC2 interactive worker |
| Research | AWS Batch + S3 |
| 민감정보 | `[REDACTED*]` placeholder만 기록 |
| 실제 실행 | 본 문서 범위 밖 · 운영자 직접 수행 |

## 운영 실증 메모 (2026-07-22)

용어 정의와 5-field template은 변경하지 않는다. 아래는 정상 Scheduler 자동 회차에서 확인된 짧은 운영 사실이다.

| 항목 | 값 |
| --- | --- |
| EventBridge Scheduler · Step Functions | 정상 자동 회차 Step 1~11 · Step 12~17 성공 확인 |
| ECS / Fargate | READY Plan → Order · Order Chain validator 2개 자동 통과 |
| SSM RunCommand | MarketConnector 단계 정상 완료 |
| RDS | after-check로 주문·체결·Position·Balance 정합 확인 |
| CloudWatch Logs | Slack 성공 경로 · OPS 성공 기록 확인 |
| port-view | ECS Fargate 1차 실증 상태 유지 · P2(ALB·Route53·Auto Scaling 등) 범위 제외 |

## Category TOC

### Network

- [Region](#region)
- [VPC](#vpc-virtual-private-cloud)
- [Subnet](#subnet)
- [Public Subnet](#public-subnet)
- [Private Subnet](#private-subnet)
- [Route Table](#route-table)
- [Internet Gateway](#internet-gateway-igw)
- [NAT Gateway](#nat-gateway)
- [NAT Instance](#nat-instance)
- [VPC Endpoint](#vpc-endpoint)
- [Security Group](#security-group-sg)
- [Elastic IP](#elastic-ip-eip)
- [ALB](#alb-application-load-balancer)

### Compute

- [EC2](#ec2-elastic-compute-cloud)
- [ECS](#ecs-elastic-container-service)
- [Fargate](#fargate-ecs-launch-type)
- [ECR](#ecr-elastic-container-registry)

### Database

- [RDS](#rds-relational-database-service)
- [RDS Subnet Group](#rds-subnet-group)
- [RDS Parameter Group](#rds-parameter-group)

### Security / IAM / Secrets

- [Secrets Manager](#secrets-manager)
- [SSM Parameter Store](#ssm-parameter-store)
- [IAM User](#iam-user)
- [IAM Role](#iam-role)
- [IAM Policy](#iam-policy)
- [Instance Profile](#instance-profile)
- [KMS](#kms-key-management-service)

### Orchestration

- [EventBridge Scheduler](#eventbridge-scheduler)
- [Step Functions](#step-functions)
- [Lambda](#lambda)
- [SSM RunCommand](#ssm-runcommand)
- [Windows Scheduled Task](#windows-scheduled-task)
- [Windows Autologon](#windows-autologon-sysinternals)

### Observability

- [CloudWatch Logs](#cloudwatch-logs)
- [CloudWatch Metrics](#cloudwatch-metrics)
- [CloudWatch Alarm](#cloudwatch-alarm)

### Storage / Artifact

- [S3](#s3-simple-storage-service)
- [AWS Batch](#aws-batch)
- [Cloud Map](#cloud-map--service-discovery)

## Glossary

### Region

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 데이터센터가 모여 있는 지리적 위치 단위. |
| 역할 | 본 프로젝트는 서울 `ap-northeast-2` 단일 region에서 운영한다. broker(KIS)와 사용자 모두 한국 기준이라 latency / 규제 측면에서 단일 region 권고. |
| 비용 발생 | 무료. 다른 Region 간 데이터 전송은 별도 과금. |
| 주의 | region을 옮기면 RDS / ECR / 모든 endpoint URL이 바뀐다. 본 spec 안에서는 region 변경 금지. |
| 관련 spec | 02, 06, 07, 10. |

### VPC (Virtual Private Cloud)

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 안의 사설 네트워크 컨테이너. 사내 네트워크처럼 IP 대역과 라우팅을 직접 정의한다. |
| 역할 | PORT-STRATEGY-AI 8개 MS가 들어갈 단일 VPC `portfolio-vpc`. 환경(aws-paper, aws-live)은 같은 VPC 안에서 SG와 subnet 태그로 분리한다. |
| 비용 발생 | 무료. NAT Gateway, VPC Endpoint 등 부속 리소스는 과금. |
| 주의 | CIDR 결정 시 사내 다른 네트워크와 겹치면 안 된다. 한번 정한 CIDR은 변경 어려움. |
| 관련 spec | 02 우선, 그 외 모두 의존. |

### Subnet

| 항목 | 값 |
| --- | --- |
| 설명 | VPC 안에서 IP 대역을 작게 잘라낸 네트워크 구획. |
| 역할 | 본 spec은 public / private (app) / private (data) 3종 × 2 AZ로 6개 subnet 권고. |
| 비용 발생 | 무료. |
| 주의 | subnet은 한 AZ에만 속한다. multi-AZ를 원하면 subnet도 AZ별로 따로 만든다. |
| 관련 spec | 02. |

### Public Subnet

| 항목 | 값 |
| --- | --- |
| 설명 | Internet Gateway로 직접 인터넷 outbound가 가능한 subnet. |
| 역할 | 마켓커넥터 EC2(EIP) 와 인터넷 outbound가 필요한 ECS Fargate Task(NAT-free 전략)는 public subnet에 배치. |
| 비용 발생 | 무료. Public IPv4 사용 시 별도 과금. |
| 주의 | Task 또는 EC2를 무심코 public subnet에 두면 외부에 직접 노출될 수 있다. 반드시 SG로 inbound를 차단. |
| 관련 spec | 02, 03, 08. |

### Private Subnet

| 항목 | 값 |
| --- | --- |
| 설명 | Internet Gateway 없이 내부 통신 또는 NAT/VPC Endpoint로만 외부에 접근 가능한 subnet. |
| 역할 | RDS, 일반 ECS Task, 외부 outbound가 없는 워크로드 배치 위치. 본 spec은 NAT-free 전략이므로 외부 인터넷 outbound가 꼭 필요한 워크로드는 private subnet에 두지 않는다. |
| 비용 발생 | 무료. |
| 주의 | NAT 미사용 환경에서 private subnet에 외부 outbound가 필요한 워크로드를 두면 동작하지 않는다. VPC Endpoint로 AWS 서비스에만 접근 가능. |
| 관련 spec | 02, 04, 05, 09. |

### Route Table

| 항목 | 값 |
| --- | --- |
| 설명 | subnet 트래픽이 어디로 흘러가는지 정하는 규칙 묶음. |
| 역할 | public Route Table은 0.0.0.0/0 → IGW. private (app) Route Table은 NAT-free 전략에서는 외부 라우트 없이 VPC Endpoint로만 AWS 서비스에 접근. data Route Table은 외부 라우트 없음. |
| 비용 발생 | 무료. |
| 주의 | Route Table을 잘못 매핑하면 ECS Task가 RDS에 못 붙거나 외부 outbound가 끊긴다. SG보다 우선 점검 항목. |
| 관련 spec | 02. |

### Internet Gateway (IGW)

| 항목 | 값 |
| --- | --- |
| 설명 | VPC와 인터넷을 연결하는 게이트웨이. |
| 역할 | public subnet의 외부 outbound와 EIP 부여 EC2의 외부 출구. |
| 비용 발생 | 무료. 인터넷 데이터 전송은 별도 과금. |
| 주의 | 한 VPC에 1개만 attach. detach 후 변경 시 외부 통신이 끊긴다. |
| 관련 spec | 02, 03. |

### NAT Gateway

| 항목 | 값 |
| --- | --- |
| 설명 | private subnet 워크로드가 인터넷으로 outbound만 나가게 해 주는 AWS 관리형 NAT. |
| 역할 | 본 프로젝트는 비용 절감을 위해 aws-paper / aws-live 모두 미사용을 기본안으로 채택했다. 인터넷 outbound가 필요한 워크로드는 public subnet 또는 EC2+EIP로 대체. |
| 비용 발생 | 사용 시 ~$43/월/AZ + 데이터 처리 ~$0.059/GB. 가장 큰 단일 고정비 중 하나. |
| 주의 | multi-AZ NAT는 비용이 2배. 한 번 켜고 나면 비용 점검을 잊기 쉽다. |
| 관련 spec | 02 비교 항목, 03/08 NAT-free 대안 검토. |

### NAT Instance

| 항목 | 값 |
| --- | --- |
| 설명 | NAT 역할을 하는 자체 EC2(예: `t4g.nano`). |
| 역할 | NAT Gateway 비용을 줄이고 싶을 때 후보. 본 spec에서는 비교만 하고 권고는 NAT-free. |
| 비용 발생 | EC2 시간 단가(~$3.5/월) + EBS + 데이터 처리. NAT GW보다 훨씬 저렴. |
| 주의 | 단일 EC2이므로 SPOF. 패치/모니터링 직접. Multi-AZ HA를 운영자가 직접 구현해야 한다. 본 프로젝트에서는 이 운영 부담을 피하기 위해 NAT-free 전략 선택. |
| 관련 spec | 02 비교 항목. |

### VPC Endpoint

| 항목 | 값 |
| --- | --- |
| 설명 | NAT 없이 VPC 내부에서 AWS 서비스에 접근하는 사설 연결. S3는 Gateway Endpoint, 그 외는 Interface Endpoint. |
| 역할 | 기본 유지 세트는 S3 Gateway + ECR api/dkr Interface + Secrets Manager Interface + CloudWatch Logs Interface. SSM은 필수 아님(Public outbound 또는 SSM Interface Endpoint 선택형). |
| 비용 발생 | Gateway Endpoint 무료. Interface Endpoint는 개수와 AZ 수에 비례. |
| 주의 | 기본 세트 누락 시 NAT 없이 해당 AWS API 접근 불가. Interface Endpoint SG inbound 443을 먼저 점검. SSM Endpoint 유무는 SSM Session Manager 사용과 별개. |
| 관련 spec | 02, 06, 07. |

### Security Group (SG)

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 리소스 단위의 stateful 방화벽 규칙. |
| 역할 | MS별 SG 분리. RDS SG는 inbound를 다른 SG 참조로만 허용(0.0.0.0/0 절대 금지). |
| 비용 발생 | 무료. |
| 주의 | SG는 stateful이라 inbound 허용 시 outbound는 자동 허용. 그러나 outbound는 별도 명시. SG 규칙 변경 시 즉시 반영되므로 잘못 수정하면 운영 중 단절 발생. |
| 관련 spec | 02 우선, 그 외 모두 사용. |

### Elastic IP (EIP)

| 항목 | 값 |
| --- | --- |
| 설명 | 고정 public IP. EC2 또는 NAT Gateway에 부여 가능. |
| 역할 | 마켓커넥터 EC2에 부여해 broker(KIS) 측 IP 등록 정책에 대응. broker 측에 EIP를 등록한다. |
| 비용 발생 | instance에 attach + running이면 무료. detach 또는 stop 상태에서는 ~$3.6/월. |
| 주의 | EC2 교체 시 EIP detach → 새 EC2에 attach. attach 안 된 시간 동안 미사용 비용 + broker outbound IP 변경 위험. |
| 관련 spec | 03 marketconnector-ec2. |

### ALB (Application Load Balancer)

| 항목 | 값 |
| --- | --- |
| 설명 | HTTP/HTTPS 트래픽 분산 로드 밸런서. |
| 역할 | port-view 운영 콘솔 앞단(internal ALB)에 둘 수 있다. 본 spec 결정에서는 aws-paper 초기 미사용, aws-live도 비용 절감안에서는 보류. |
| 비용 발생 | ~$16.5/월 + LCU 사용량. |
| 주의 | ALB SG inbound는 운영자 IP allowlist 또는 인증 게이트로 좁히는 것이 안전. |
| 관련 spec | 05 port-view-ecs-and-runbook. |

### EC2 (Elastic Compute Cloud)

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 가상 서버. |
| 역할 | 고정 IP가 필요한 MarketConnector와 Windows GUI 기반 KRX worker를 실행한다. |
| 비용 발생 | 인스턴스 시간 단가(예: `t4g.small` ~$15/월, `t3.medium` ~$38/월) + EBS + 데이터 전송. **영업 시간 외 EC2 stop 으로 instance-hour 누적 감소**(정확한 절감액은 운영 회차 누적 후 산출). |
| 주의 | SSH 공개 허용을 피하고 SSM Session Manager를 사용한다. 인스턴스 교체 시 EIP·IAM Role·설정값 연결을 다시 확인한다. |
| 관련 spec | 03, 08(NAT-free 대안). |

### ECS (Elastic Container Service)

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 관리형 컨테이너 오케스트레이터. |
| 역할 | 본 프로젝트는 거의 모든 Python / Java MS를 ECS 위에서 운영. Cluster는 환경별로 분리(`portfolio-paper`, `portfolio-live`). |
| 비용 발생 | 클러스터 자체는 무료. 안에서 도는 Fargate / EC2 launch type에 따라 비용 발생. |
| 주의 | Task Definition revision 관리. 배포 시 잘못된 revision로 롤백 가능하도록 보존. |
| 관련 spec | 04, 05, 08. |

### Fargate (ECS launch type)

| 항목 | 값 |
| --- | --- |
| 설명 | 서버 관리 없이 컨테이너만 실행하는 ECS launch type. |
| 역할 | Daily Batch, port-view, crawler, preprocessor 등 컨테이너 워크로드를 서버 관리 없이 실행한다. |
| 비용 발생 | ~$0.05056 / vCPU-시간 + ~$0.00553 / GB-시간. 0.5 vCPU + 1 GB 24/7 ≈ $22.5/월. |
| 주의 | 인터넷 outbound가 필요하면 subnet·public IP·SG 구성을 확인한다. KST 기준 작업은 `TZ=Asia/Seoul` 또는 timezone-aware 코드를 사용한다. |
| 관련 spec | 04, 05, 08, 09. |

### ECR (Elastic Container Registry)

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 컨테이너 이미지 저장소. |
| 역할 | 8개 MS 중 컨테이너 배포 대상 MS의 이미지 저장(`port-view`, `port-interest-crawler`, `port-interest-preprocessor`, `port-strategy-decision`, `port-strategy-execution`, `port-strategy-research`, 옵션으로 `port-marketconnector`). |
| 비용 발생 | storage ~$0.10/GB-월. 같은 region pull은 무료. NAT 미사용 환경에서는 ECR Endpoint 비용이 추가. |
| 주의 | 이미지 태그 관리(`:{git-sha}` + `:{env}`). lifecycle policy로 오래된 이미지 정리. 빌드 시 secret을 이미지에 굽지 않는다. |
| 관련 spec | 07 cicd-pipelines. |

### RDS (Relational Database Service)

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 관리형 RDBMS. 본 프로젝트는 RDS for PostgreSQL 16 이상 사용. |
| 역할 | 단일 portfolio DB. schema-per-domain 10개 schema 유지. aws-paper와 aws-live 환경별 인스턴스 분리. |
| 비용 발생 | 인스턴스 시간 단가 + storage + backup. multi-AZ는 약 2배. |
| 주의 | publicly accessible false. SG inbound는 다른 SG 참조만. master password는 Secrets Manager 보관. |
| 관련 spec | 02, 06, 10. |

### RDS Subnet Group

| 항목 | 값 |
| --- | --- |
| 설명 | RDS가 배치될 subnet 묶음 정의. |
| 역할 | data-a, data-b 두 private (data) subnet을 묶어 multi-AZ 가능하도록 구성. single-AZ를 시작점으로 두더라도 subnet group은 multi-AZ 가능 형태로 구성. |
| 비용 발생 | 무료. |
| 주의 | subnet group은 RDS 생성 후 변경 어려움. 처음부터 2 AZ subnet으로 구성. |
| 관련 spec | 02. |

### RDS Parameter Group

| 항목 | 값 |
| --- | --- |
| 설명 | PostgreSQL 설정값 모음. |
| 역할 | `rds.force_ssl=1` (paper/live), `log_min_duration_statement`, `log_lock_waits=1`, `idle_in_transaction_session_timeout` 등 운영 권고값을 묶어 적용. |
| 비용 발생 | 무료. |
| 주의 | parameter group 변경 시 일부 항목은 reboot 필요. 운영 중 변경 시 reboot window 주의. |
| 관련 spec | 02. |

### Secrets Manager

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 관리형 비밀 저장소. 자동 rotation 지원. |
| 역할 | DB password, KIS app key·secret, 계좌번호, Slack webhook URL 같은 민감정보를 보관한다. |
| 비용 발생 | $0.40 / secret / 월 + $0.05 / 10,000 API calls. |
| 주의 | 애플리케이션에는 secret 원문을 기록하지 않고 실행 시 조회한다. IAM 권한은 필요한 secret ARN으로 제한한다. |
| 관련 spec | 06 secrets-and-iam. |

### SSM Parameter Store

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 환경변수 / 설정 / SecureString 저장소. |
| 역할 | DB 접속정보, 환경 구분, 전략 설정값 등 저민감 설정을 보관한다. 필요하면 SecureString으로 일부 민감 설정도 관리한다. |
| 비용 발생 | Standard tier 무료(파라미터 10,000개까지). Advanced tier $0.05/파라미터/월. |
| 주의 | 환경변수 키 호환성을 위해 키 이름은 그대로 유지. 잘못된 환경에 잘못된 값을 넣으면 paper에서 live broker로 호출 가능 → live 자동매매 사고 위험. |
| 관련 spec | 06 secrets-and-iam. |

### IAM User

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 계정 안에서 사람(또는 외부 system)이 콘솔 / API에 로그인할 때 쓰는 신원. |
| 역할 | 본 프로젝트는 운영자 1인이 사용하는 IAM 관리자 사용자 `portadmin`을 만들고 모든 일상 작업을 portadmin으로 수행한다. Root 계정은 비상용(청구 / 계정 폐쇄 / IAM 정책 변경)으로만 보관한다. 02 runbook Step 0에서 portadmin 생성 / AdministratorAccess 부여 / 콘솔 sign-in URL 확보 / MFA 활성 / Root 보안 강화 절차를 다룬다. |
| 비용 발생 | 무료. |
| 주의 | 운영자 비밀번호, MFA 정보, 백업 코드는 평문으로 기록하지 않는다. Root access key는 사용하지 않는 것을 원칙으로 한다. |
| 관련 spec | 02(Step 0 portadmin 생성), 06(IAM 권한 매트릭스 / portadmin 외 추가 사용자 검토). |

### IAM Role

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 리소스가 다른 AWS 리소스를 호출할 때 쓰는 권한 묶음. |
| 역할 | ECS Task, EC2, Lambda, Step Functions가 다른 AWS 리소스를 호출할 때 사용하는 권한 묶음이다. |
| 비용 발생 | 무료. |
| 주의 | Task Role과 Task Execution Role의 책임을 분리하고, Action과 Resource는 필요한 범위로만 제한한다. |
| 관련 spec | 06. |

### IAM Policy

| 항목 | 값 |
| --- | --- |
| 설명 | IAM Role / User에 붙이는 권한 명세서(JSON). |
| 역할 | IAM User와 IAM Role에 허용하거나 거부할 AWS API 작업과 대상 리소스를 정의한다. |
| 비용 발생 | 무료. |
| 주의 | Action 또는 Resource에 광역 wildcard를 사용하지 않고 환경과 리소스별로 최소 권한을 적용한다. |
| 관련 spec | 06. |

### Instance Profile

| 항목 | 값 |
| --- | --- |
| 설명 | EC2가 IAM Role을 사용하기 위한 wrapper. |
| 역할 | 마켓커넥터 EC2에 attach해 SSM Session Manager, Secrets Manager, CloudWatch Agent, S3 토큰 백업을 사용. |
| 비용 발생 | 무료. |
| 주의 | 한 EC2당 1개. 변경 시 application 재인증이 필요할 수 있다. |
| 관련 spec | 03, 06. |

### CloudWatch Logs

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 관리형 로그 수집/저장 서비스. |
| 역할 | ECS Task 로그(`awslogs` driver), EC2 CloudWatch agent 로그, RDS PostgreSQL 로그(`/aws/rds/instance/{id}/postgresql`). |
| 비용 발생 | ingestion ~$0.76/GB, storage ~$0.04/GB-월. NAT 미사용 시 Logs Endpoint 비용 추가. |
| 주의 | retention을 길게 두면 storage 비용이 누적. 본 spec은 aws-paper 7일, aws-live 14일 시작 권고. |
| 관련 spec | 04, 05, 08, 09. |

### CloudWatch Metrics

| 항목 | 값 |
| --- | --- |
| 설명 | 시계열 모니터링 데이터. |
| 역할 | ECS / EC2 / RDS 표준 메트릭 + 도메인 custom metric(broker error rate, Daily Batch step 결과, intraday heartbeat 등). |
| 비용 발생 | AWS 표준 메트릭 무료. custom metric ~$0.30/metric/월. |
| 주의 | custom metric을 무분별하게 만들면 비용이 누적. 운영 의사결정에 직접 쓰는 메트릭만 추가. |
| 관련 spec | 04, 05, 08. |

### CloudWatch Alarm

| 항목 | 값 |
| --- | --- |
| 설명 | 메트릭 threshold 기반 경보. |
| 역할 | RDS 연결 수 폭주, ECS Task 실패, broker error rate 급증, intraday monitor heartbeat 끊김 등 감지. |
| 비용 발생 | standard alarm ~$0.10/alarm/월. |
| 주의 | alarm을 너무 많이 만들면 알림 피로 → Slack 채널이 무시당함. 핵심 알람만 유지. |
| 관련 spec | 05, 10. |

### EventBridge Scheduler

| 항목 | 값 |
| --- | --- |
| 설명 | cron 또는 rate 기반 스케줄러. Daily Batch / intraday polling 트리거. |
| 역할 | Daily Batch, EC2 시작·중지, 장중 작업, Daily Brief 등 정해진 시각의 자동 실행을 시작한다. |
| 비용 발생 | 소규모 사용은 무료 한도 내. 초과 호출은 별도 과금. |
| 주의 | cron, timezone, 활성 상태를 확인한다. 주말·휴장일 판단은 별도 Dispatcher 또는 guard에서 fail-closed로 처리한다. |
| 관련 spec | 04 strategy-batch-stepfunctions, 10 cutover-and-validation-runbook. |

### Step Functions

| 항목 | 값 |
| --- | --- |
| 설명 | AWS 관리형 워크플로 orchestration. state machine. |
| 역할 | ECS Task와 EC2 명령을 순서대로 실행하고 성공·실패·승인 분기를 관리한다. |
| 비용 발생 | State transition 수에 따라 과금. |
| 주의 | 주문 제출처럼 중복 실행 위험이 있는 단계에는 자동 Retry를 사용하지 않는다. 실패 경로와 알림 경로를 함께 검증한다. |
| 관련 spec | 04, 09, 10. |

### Lambda

| 항목 | 값 |
| --- | --- |
| 설명 | 서버리스 함수 실행. |
| 역할 | Scheduler dispatcher, Slack notifier, EC2 lifecycle, 짧은 요약·기록 작업을 담당하는 보조 실행 계층이다. |
| 비용 발생 | 요청 수와 실행 시간에 따라 과금. 소규모 운영은 무료 한도 내. |
| 주의 | Selenium·장시간 처리·상태 유지가 필요한 작업에는 사용하지 않는다. secret은 환경변수 평문보다 Secrets Manager 조회를 우선한다. |
| 관련 spec | 04, 05, 08, 10. |

### S3 (Simple Storage Service)

| 항목 | 값 |
| --- | --- |
| 설명 | 오브젝트 스토리지. |
| 역할 | KIS access_token EC2 로컬 백업 보관, port_strategy_research 텍스트 리포트 저장, RDS 외부 dump 보관. |
| 비용 발생 | Standard storage ~$0.025/GB-월. Gateway endpoint로 무료 outbound 가능. |
| 주의 | 버킷 public access는 기본 차단 유지. 환경별 prefix(`portfolio/paper/...`)로 분리. |
| 관련 spec | 03, 09. |

### AWS Batch

| 항목 | 값 |
| --- | --- |
| 설명 | 장시간 / 가변 자원 batch job 실행 서비스. |
| 역할 | `port_strategy_research` 백테스트 1순위 후보. Step Functions에서 호출 가능. |
| 비용 발생 | 내부 compute env(Fargate 또는 EC2) 단가 그대로. AWS Batch 자체 단가 없음. |
| 주의 | job queue 우선순위, 동시 실행 수 제한, 장시간 실행 중 RDS connection 누수 점검. |
| 관련 spec | 09 strategy-research-batch. |

### KMS (Key Management Service)

| 항목 | 값 |
| --- | --- |
| 설명 | 암호화 키 관리 서비스. |
| 역할 | RDS encryption at rest, Secrets Manager, S3 SSE-KMS, ECR 이미지 암호화 옵션의 키 보관. |
| 비용 발생 | CMK $1.0/월/key + API calls. AWS 관리형 키는 무료. |
| 주의 | aws-paper는 KMS default 권고. aws-live는 CMK 권고이나 IAM Policy에서 KMS 권한을 빠뜨리면 암호화/복호화 실패. |
| 관련 spec | 02, 06. |

### Cloud Map / Service Discovery

| 항목 | 값 |
| --- | --- |
| 설명 | ECS 서비스 이름으로 내부 DNS 이름을 자동 등록하는 service discovery. |
| 역할 | port-view ECS Service에서 marketconnector EC2 또는 다른 ECS Service에 ALB 없이 내부 호출. ALB 비용을 줄이는 1차 대안. |
| 비용 발생 | 매우 작음. 등록된 서비스 / 호스팅 영역 기준. |
| 주의 | DNS TTL 짧게 유지. 새 Task 기동 시 IP 변경 반영 시간을 application 측이 견딜 수 있어야 한다. |
| 관련 spec | 05. |

### SSM RunCommand

| 항목 | 값 |
| --- | --- |
| 설명 | AWS Systems Manager 의 원격 명령 실행 기능. EC2 / on-prem 에 대해 `AWS-RunPowerShellScript` / `AWS-RunShellScript` 등 document 로 명령 실행. |
| 역할 | MarketConnector EC2의 단발 작업과 Windows KRX worker의 Scheduled Task를 원격으로 시작한다. |
| 비용 발생 | API 호출 자체는 사실상 무료. VPC Endpoint 사용 시 Endpoint 비용 발생. |
| 주의 | Windows GUI 작업을 SYSTEM Session 0에서 직접 실행하지 않는다. 명령 종료 코드와 실제 결과를 함께 검증한다. |
| 관련 spec | 03, 08. |

### Windows Scheduled Task

| 항목 | 값 |
| --- | --- |
| 설명 | Windows OS 내장 작업 스케줄러. 등록된 task 를 시간 trigger 또는 외부 trigger(`schtasks /Run`) 로 실행. |
| 역할 | Windows EC2의 Administrator interactive session에서 KRX GUI worker를 실행한다. |
| 비용 발생 | 무료. Windows EC2 비용은 별도. |
| 주의 | 실행 사용자, Logon Mode, 마지막 실행 결과와 종료 코드를 확인한다. |
| 관련 spec | 08. |

### Windows Autologon (Sysinternals)

| 항목 | 값 |
| --- | --- |
| 설명 | Microsoft Sysinternals 도구. Windows 부팅 직후 지정 사용자(보통 Administrator)로 자동 로그인을 활성화해 console interactive session 을 자동 생성. |
| 역할 | Windows EC2 부팅 후 GUI worker가 사용할 interactive session을 자동으로 만든다. |
| 비용 발생 | 도구는 무료. Windows EC2 비용은 별도. |
| 주의 | paper 전용 보안 예외로만 사용한다. 자격 증명 노출 위험을 줄이기 위해 접근을 제한하고 작업 후 EC2를 중지한다. |
| 관련 spec | 08. |

## Update Rules

| 항목 | 값 |
| --- | --- |
| 새 용어 | 해당 카테고리에 H3 항목으로 추가 |
| 기본 형식 | `설명 / 역할 / 비용 발생 / 주의 / 관련 spec` 5행 2열 표 |
| 중복 방지 | 같은 서비스는 하나의 항목에서 역할을 통합 |
| 운영 이력 | 용어 본문에 추가하지 않고 WORKLOG 또는 `operation-notes.md`에 기록 |
| 민감정보 | `[REDACTED]` 계열 placeholder만 사용 |
| 비용 | 서울 `ap-northeast-2` 기준 근사치 · 정확한 금액은 AWS Pricing Calculator 확인 |

## Security Notes

| 항목 | 값 |
| --- | --- |
| AWS 리소스 실행 | 없음 |
| 애플리케이션 코드 수정 | 없음 |
| 민감정보 원문 기록 | 없음 |
| 허용 표기 | `[REDACTED]` 계열 placeholder |
| 문서 역할 | AWS Migration 공통 용어집 |
