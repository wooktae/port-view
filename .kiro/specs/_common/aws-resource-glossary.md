# AWS Resource Glossary — AWS Migration

본 문서는 AWS 용어가 익숙하지 않은 운영자가 AWS Migration spec(01-aws-migration-foundation, 02-aws-network-and-rds 및 후속 03 ~ 10)을 읽을 때 빠르게 의미를 잡을 수 있도록 만든 공통 용어집이다. 02 spec과 후속 spec 모두 본 용어집을 공유 참조한다. 각 항목은 다음 형식이다.

- 한 줄 설명
- 이 포트폴리오에서의 역할
- 비용 발생 여부
- 운영자가 조심해야 할 점
- 관련 후속 Spec

본 문서에는 실제 secret / password / token / app key / app secret / 계좌번호 / webhook URL 값을 적지 않는다. 모두 `[REDACTED]`만 사용한다.

용어 단가는 서울 ap-northeast-2 기준 근사치이며 "AWS Pricing Calculator 확인 필요" 단서가 붙는다.

---

## Region

- 한 줄 설명: AWS 데이터센터가 모여 있는 지리적 위치 단위.
- 역할: 본 프로젝트는 서울 `ap-northeast-2` 단일 region에서 운영한다. broker(KIS)와 사용자 모두 한국 기준이라 latency / 규제 측면에서 단일 region 권고.
- 비용 발생: region 자체에는 비용이 없다. 다만 다른 region으로 데이터 전송 시 비용 발생.
- 조심할 점: region을 옮기면 RDS / ECR / 모든 endpoint URL이 바뀐다. 본 spec 안에서는 region 변경 금지.
- 관련 spec: 02, 06, 07, 10.

## VPC (Virtual Private Cloud)

- 한 줄 설명: AWS 안의 사설 네트워크 컨테이너. 사내 네트워크처럼 IP 대역과 라우팅을 직접 정의한다.
- 역할: PORT-STRATEGY-AI 8개 MS가 들어갈 단일 VPC `portfolio-vpc`. 환경(aws-paper, aws-live)은 같은 VPC 안에서 SG와 subnet 태그로 분리한다.
- 비용 발생: VPC 자체는 무료. NAT Gateway, VPC Endpoint, NAT Instance 등 부속 리소스가 비용을 만든다.
- 조심할 점: CIDR 결정 시 사내 다른 네트워크와 겹치면 안 된다. 한번 정한 CIDR은 변경 어려움.
- 관련 spec: 02 우선, 그 외 모두 의존.

## Subnet

- 한 줄 설명: VPC 안에서 IP 대역을 작게 잘라낸 네트워크 구획.
- 역할: 본 spec은 public / private (app) / private (data) 3종 × 2 AZ로 6개 subnet 권고.
- 비용 발생: subnet 자체는 무료.
- 조심할 점: subnet은 한 AZ에만 속한다. multi-AZ를 원하면 subnet도 AZ별로 따로 만든다.
- 관련 spec: 02.

## Public Subnet

- 한 줄 설명: Internet Gateway로 직접 인터넷 outbound가 가능한 subnet.
- 역할: 마켓커넥터 EC2(EIP) 와 인터넷 outbound가 필요한 ECS Fargate Task(NAT-free 전략)는 public subnet에 배치.
- 비용 발생: subnet 자체는 무료. public IP 사용량에 따라 IPv4 비용 발생 가능(`$0.005/IPv4-시간`).
- 조심할 점: Task 또는 EC2를 무심코 public subnet에 두면 외부에 직접 노출될 수 있다. 반드시 SG로 inbound를 차단.
- 관련 spec: 02, 03, 08.

## Private Subnet

- 한 줄 설명: Internet Gateway 없이 내부 통신 또는 NAT/VPC Endpoint로만 외부에 접근 가능한 subnet.
- 역할: RDS, 일반 ECS Task, 외부 outbound가 없는 워크로드 배치 위치. 본 spec은 NAT-free 전략이므로 외부 인터넷 outbound가 꼭 필요한 워크로드는 private subnet에 두지 않는다.
- 비용 발생: subnet 자체는 무료.
- 조심할 점: NAT 미사용 환경에서 private subnet에 외부 outbound가 필요한 워크로드를 두면 동작하지 않는다. VPC Endpoint로 AWS 서비스에만 접근 가능.
- 관련 spec: 02, 04, 05, 09.

## Route Table

- 한 줄 설명: subnet 트래픽이 어디로 흘러가는지 정하는 규칙 묶음.
- 역할: public Route Table은 0.0.0.0/0 → IGW. private (app) Route Table은 NAT-free 전략에서는 외부 라우트 없이 VPC Endpoint로만 AWS 서비스에 접근. data Route Table은 외부 라우트 없음.
- 비용 발생: 무료.
- 조심할 점: Route Table을 잘못 매핑하면 ECS Task가 RDS에 못 붙거나 외부 outbound가 끊긴다. SG보다 우선 점검 항목.
- 관련 spec: 02.

## Internet Gateway (IGW)

- 한 줄 설명: VPC와 인터넷을 연결하는 게이트웨이.
- 역할: public subnet의 외부 outbound와 EIP 부여 EC2의 외부 출구.
- 비용 발생: IGW 자체는 무료. 다만 인터넷 outbound 데이터 전송 비용은 별도(~$0.114/GB).
- 조심할 점: 한 VPC에 1개만 attach. detach 후 변경 시 외부 통신이 끊긴다.
- 관련 spec: 02, 03.

## NAT Gateway

- 한 줄 설명: private subnet 워크로드가 인터넷으로 outbound만 나가게 해 주는 AWS 관리형 NAT.
- 역할: 본 프로젝트는 비용 절감을 위해 aws-paper / aws-live 모두 미사용을 기본안으로 채택했다. 인터넷 outbound가 필요한 워크로드는 public subnet 또는 EC2+EIP로 대체.
- 비용 발생: 사용 시 ~$43/월/AZ + 데이터 처리 ~$0.059/GB. 가장 큰 단일 고정비 중 하나.
- 조심할 점: multi-AZ NAT는 비용이 2배. 한 번 켜고 나면 비용 점검을 잊기 쉽다.
- 관련 spec: 02 비교 항목, 03/08 NAT-free 대안 검토.

## NAT Instance

- 한 줄 설명: NAT 역할을 하는 자체 EC2(예: `t4g.nano`).
- 역할: NAT Gateway 비용을 줄이고 싶을 때 후보. 본 spec에서는 비교만 하고 권고는 NAT-free.
- 비용 발생: EC2 시간 단가(~$3.5/월) + EBS + 데이터 처리. NAT GW보다 훨씬 저렴.
- 조심할 점: 단일 EC2이므로 SPOF. 패치/모니터링 직접. Multi-AZ HA를 운영자가 직접 구현해야 한다. 본 프로젝트에서는 이 운영 부담을 피하기 위해 NAT-free 전략 선택.
- 관련 spec: 02 비교 항목.

## VPC Endpoint

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

## Security Group (SG)

- 한 줄 설명: AWS 리소스 단위의 stateful 방화벽 규칙.
- 역할: MS별 SG 분리. RDS SG는 inbound를 다른 SG 참조로만 허용(0.0.0.0/0 절대 금지).
- 비용 발생: 무료.
- 조심할 점: SG는 stateful이라 inbound 허용 시 outbound는 자동 허용. 그러나 outbound는 별도 명시. SG 규칙 변경 시 즉시 반영되므로 잘못 수정하면 운영 중 단절 발생.
- 관련 spec: 02 우선, 그 외 모두 사용.

## Elastic IP (EIP)

- 한 줄 설명: 고정 public IP. EC2 또는 NAT Gateway에 부여 가능.
- 역할: 마켓커넥터 EC2에 부여해 broker(KIS) 측 IP 등록 정책에 대응. broker 측에 EIP를 등록한다.
- 비용 발생: instance에 attach + running이면 무료. detach 또는 stop 상태에서는 ~$3.6/월.
- 조심할 점: EC2 교체 시 EIP detach → 새 EC2에 attach. attach 안 된 시간 동안 미사용 비용 + broker outbound IP 변경 위험.
- 관련 spec: 03 marketconnector-ec2.

## ALB (Application Load Balancer)

- 한 줄 설명: HTTP/HTTPS 트래픽 분산 로드 밸런서.
- 역할: port-view 운영 콘솔 앞단(internal ALB)에 둘 수 있다. 본 spec 결정에서는 aws-paper 초기 미사용, aws-live도 비용 절감안에서는 보류.
- 비용 발생: ~$16.5/월 + LCU 사용량.
- 조심할 점: ALB SG inbound는 운영자 IP allowlist 또는 인증 게이트로 좁히는 것이 안전.
- 관련 spec: 05 port-view-ecs-and-runbook.

## EC2 (Elastic Compute Cloud)

- 한 줄 설명: AWS 가상 서버.
- 역할: 마켓커넥터(`port-marketconnector`)는 broker IP 등록과 단일 access_token 세션 제약 때문에 EC2 + EIP를 1순위로 채택. Crawler용 Selenium은 후보 중 하나.
- 비용 발생: 인스턴스 시간 단가(예: `t4g.small` ~$15/월, `t3.medium` ~$38/월) + EBS + 데이터 전송.
- 조심할 점: SSH 22 inbound 0.0.0.0/0 금지. SSM Session Manager만 사용. 인스턴스 교체 시 EIP / 토큰 / IAM Role 재부여.
- 관련 spec: 03, 08(NAT-free 대안).

## ECS (Elastic Container Service)

- 한 줄 설명: AWS 관리형 컨테이너 오케스트레이터.
- 역할: 본 프로젝트는 거의 모든 Python / Java MS를 ECS 위에서 운영. Cluster는 환경별로 분리(`portfolio-paper`, `portfolio-live`).
- 비용 발생: 클러스터 자체는 무료. 안에서 도는 Fargate / EC2 launch type에 따라 비용 발생.
- 조심할 점: Task Definition revision 관리. 배포 시 잘못된 revision로 롤백 가능하도록 보존.
- 관련 spec: 04, 05, 08.

## Fargate (ECS launch type)

- 한 줄 설명: 서버 관리 없이 컨테이너만 실행하는 ECS launch type.
- 역할: Daily Batch step, port-view 서비스, crawler/preprocessor Task 등 대부분의 컨테이너 워크로드. 시간 단가는 vCPU / 메모리에 비례.
- 비용 발생: ~$0.05056 / vCPU-시간 + ~$0.00553 / GB-시간. 0.5 vCPU + 1 GB 24/7 ≈ $22.5/월.
- 조심할 점: Task가 너무 자주 멈추면 cold start 비용 증가. 인터넷 outbound가 필요한 Task는 NAT-free 전략에서 public subnet에 두고 SG 통제.
- 관련 spec: 04, 05, 08, 09.

## ECR (Elastic Container Registry)

- 한 줄 설명: AWS 컨테이너 이미지 저장소.
- 역할: 8개 MS 중 컨테이너 배포 대상 MS의 이미지 저장(`port-view`, `port-interest-crawler`, `port-interest-preprocessor`, `port-strategy-decision`, `port-strategy-execution`, `port-strategy-research`, 옵션으로 `port-marketconnector`).
- 비용 발생: storage ~$0.10/GB-월. 같은 region pull은 무료. NAT 미사용 환경에서는 ECR Endpoint 비용이 추가.
- 조심할 점: 이미지 태그 관리(`:{git-sha}` + `:{env}`). lifecycle policy로 오래된 이미지 정리. 빌드 시 secret을 이미지에 굽지 않는다.
- 관련 spec: 07 cicd-pipelines.

## RDS (Relational Database Service)

- 한 줄 설명: AWS 관리형 RDBMS. 본 프로젝트는 RDS for PostgreSQL 16 이상 사용.
- 역할: 단일 portfolio DB. schema-per-domain 10개 schema 유지. aws-paper와 aws-live 환경별 인스턴스 분리.
- 비용 발생: 인스턴스 시간 단가 + storage + backup. multi-AZ는 약 2배.
- 조심할 점: publicly accessible false. SG inbound는 다른 SG 참조만. master password는 Secrets Manager 보관.
- 관련 spec: 02, 06, 10.

## RDS Subnet Group

- 한 줄 설명: RDS가 배치될 subnet 묶음 정의.
- 역할: data-a, data-b 두 private (data) subnet을 묶어 multi-AZ 가능하도록 구성. single-AZ를 시작점으로 두더라도 subnet group은 multi-AZ 가능 형태로 구성.
- 비용 발생: 무료.
- 조심할 점: subnet group은 RDS 생성 후 변경 어려움. 처음부터 2 AZ subnet으로 구성.
- 관련 spec: 02.

## RDS Parameter Group

- 한 줄 설명: PostgreSQL 설정값 모음.
- 역할: `rds.force_ssl=1` (paper/live), `log_min_duration_statement`, `log_lock_waits=1`, `idle_in_transaction_session_timeout` 등 운영 권고값을 묶어 적용.
- 비용 발생: 무료.
- 조심할 점: parameter group 변경 시 일부 항목은 reboot 필요. 운영 중 변경 시 reboot window 주의.
- 관련 spec: 02.

## Secrets Manager

- 한 줄 설명: AWS 관리형 비밀 저장소. 자동 rotation 지원.
- 역할: KIS app key / app secret / base URL / 계좌번호 / DB master password / Slack webhook(예정) 등 고민감 secret 저장. 모두 `[REDACTED]`로만 표기.
- 비용 발생: $0.40 / secret / 월 + $0.05 / 10,000 API calls.
- 조심할 점: 본 spec에서 최종 결정 항목과 IAM 정책 매트릭스는 06 spec에서 확정. rotation 적용 시 application 재기동 영향 고려.
- 관련 spec: 06 secrets-and-iam.

## SSM Parameter Store

- 한 줄 설명: AWS 환경변수 / 설정 / SecureString 저장소.
- 역할: `INTEREST_DB_HOST/PORT/NAME/USER`, `PORTFOLIO_DB_NAME`, `PORT_BROKER_NAME`, `PORT_ENVIRONMENT`, `PORT_STRATEGY_*`, `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT` 등 저민감 환경변수 보관 후보. SecureString으로 일부 secret 대체 가능.
- 비용 발생: Standard tier 무료(파라미터 10,000개까지). Advanced tier $0.05/파라미터/월.
- 조심할 점: 환경변수 키 호환성을 위해 키 이름은 그대로 유지. 잘못된 환경에 잘못된 값을 넣으면 paper에서 live broker로 호출 가능 → live 자동매매 사고 위험.
- 관련 spec: 06 secrets-and-iam.

## IAM User

- 한 줄 설명: AWS 계정 안에서 사람(또는 외부 system)이 콘솔 / API에 로그인할 때 쓰는 신원.
- 역할: 본 프로젝트는 운영자 1인이 사용하는 IAM 관리자 사용자 `portadmin`을 만들고 모든 일상 작업을 portadmin으로 수행한다. Root 계정은 비상용(청구 / 계정 폐쇄 / IAM 정책 변경)으로만 보관한다. 02 runbook Step 0에서 portadmin 생성 / AdministratorAccess 부여 / 콘솔 sign-in URL 확보 / MFA 활성 / Root 보안 강화 절차를 다룬다.
- 비용 발생: IAM User 자체는 무료.
- 조심할 점: portadmin 비밀번호 / MFA 시리얼 / 백업 코드, Root MFA 시리얼은 본 작업공간 어떤 문서에도 평문 기록 금지(모두 `[REDACTED]`). Root access key는 발급되어 있으면 즉시 삭제. 비밀번호 분실 / MFA 분실 시 복구 절차는 R-SEC-002, R-SEC-003 참조([`./risk-register.md`](./risk-register.md)).
- 관련 spec: 02(Step 0 portadmin 생성), 06(IAM 권한 매트릭스 / portadmin 외 추가 사용자 검토).

## IAM Role

- 한 줄 설명: AWS 리소스가 다른 AWS 리소스를 호출할 때 쓰는 권한 묶음.
- 역할: ECS Task Role / EC2 Instance Role / Lambda Role / RDS Enhanced Monitoring Role 등.
- 비용 발생: 무료.
- 조심할 점: 권한 누락 시 ECR pull, Secrets Manager 조회, CloudWatch Logs 송신이 모두 실패. 권한 과다 시 감사 위험.
- 관련 spec: 06.

## IAM Policy

- 한 줄 설명: IAM Role / User에 붙이는 권한 명세서(JSON).
- 역할: ECS Task Role에 ECR pull, Secrets Manager 조회(specific ARN), CloudWatch Logs PutLogEvents 등을 명시.
- 비용 발생: 무료.
- 조심할 점: ARN 패턴이 너무 넓으면(`*`) 다른 환경 secret까지 읽을 수 있다. 환경별 prefix(`/portfolio/paper/...`)로 좁히기.
- 관련 spec: 06.

## Instance Profile

- 한 줄 설명: EC2가 IAM Role을 사용하기 위한 wrapper.
- 역할: 마켓커넥터 EC2에 attach해 SSM Session Manager, Secrets Manager, CloudWatch Agent, S3 토큰 백업을 사용.
- 비용 발생: 무료.
- 조심할 점: 한 EC2당 1개. 변경 시 application 재인증이 필요할 수 있다.
- 관련 spec: 03, 06.

## CloudWatch Logs

- 한 줄 설명: AWS 관리형 로그 수집/저장 서비스.
- 역할: ECS Task 로그(`awslogs` driver), EC2 CloudWatch agent 로그, RDS PostgreSQL 로그(`/aws/rds/instance/{id}/postgresql`).
- 비용 발생: ingestion ~$0.76/GB, storage ~$0.04/GB-월. NAT 미사용 시 Logs Endpoint 비용 추가.
- 조심할 점: retention을 길게 두면 storage 비용이 누적. 본 spec은 aws-paper 7일, aws-live 14일 시작 권고.
- 관련 spec: 04, 05, 08, 09.

## CloudWatch Metrics

- 한 줄 설명: 시계열 모니터링 데이터.
- 역할: ECS / EC2 / RDS 표준 메트릭 + 도메인 custom metric(broker error rate, Daily Batch step 결과, intraday heartbeat 등).
- 비용 발생: AWS 표준 메트릭 무료. custom metric ~$0.30/metric/월.
- 조심할 점: custom metric을 무분별하게 만들면 비용이 누적. 운영 의사결정에 직접 쓰는 메트릭만 추가.
- 관련 spec: 04, 05, 08.

## CloudWatch Alarm

- 한 줄 설명: 메트릭 threshold 기반 경보.
- 역할: RDS 연결 수 폭주, ECS Task 실패, broker error rate 급증, intraday monitor heartbeat 끊김 등 감지.
- 비용 발생: standard alarm ~$0.10/alarm/월.
- 조심할 점: alarm을 너무 많이 만들면 알림 피로 → Slack 채널이 무시당함. 핵심 알람만 유지.
- 관련 spec: 05, 10.

## EventBridge Scheduler

- 한 줄 설명: cron 또는 rate 기반 스케줄러. Daily Batch / intraday polling 트리거.
- 역할: Daily Batch는 EventBridge Scheduler → Step Functions → ECS RunTask로 트리거. EC2/Spring 안의 subprocess 호출 구조를 대체.
- 비용 발생: 월 14M invocations 무료. 본 프로젝트는 무료 한도 안에서 충분.
- 조심할 점: 스케줄 시각 / timezone 설정. cron 잘못 입력 시 batch 미실행.
- 관련 spec: 04 strategy-batch-stepfunctions.

## Step Functions

- 한 줄 설명: AWS 관리형 워크플로 orchestration. state machine.
- 역할: Daily Batch 파이프라인의 여러 ECS Task를 순서대로 실행, 실패 시 분기. live 자동 재시도 금지 정책을 state machine 레벨에서 강제.
- 비용 발생: Standard ~$0.025 / 1,000 transitions. Express는 다른 단가. 본 프로젝트는 Standard 시작.
- 조심할 점: BUY/SELL/fill sync/position 변경/intraday stop SELL 생성 step에는 Retry 정책을 비활성화. idempotent step만 자동 재시도 허용.
- 관련 spec: 04, 09, 10.

## Lambda

- 한 줄 설명: 서버리스 함수 실행.
- 역할: 인프라 알람 SNS → Lambda → Slack webhook fan-out, 짧은 후처리(예: Daily Batch 결과 요약). Selenium / Chrome / KRX 로그인은 Lambda 비권고(컨테이너 한계, cold start, 세션 stateful).
- 비용 발생: 월 1M requests + 400,000 GB-sec 무료. 본 프로젝트 인프라 알람 fan-out은 무료 한도 안.
- 조심할 점: VPC 연결 시 cold start 증가. crawler처럼 Selenium 의존 워크로드는 Lambda 부적합.
- 관련 spec: 04, 05, 08.

## S3 (Simple Storage Service)

- 한 줄 설명: 오브젝트 스토리지.
- 역할: KIS access_token EC2 로컬 백업 보관, port_strategy_research 텍스트 리포트 저장, RDS 외부 dump 보관.
- 비용 발생: Standard storage ~$0.025/GB-월. Gateway endpoint로 무료 outbound 가능.
- 조심할 점: 버킷 public access는 기본 차단 유지. 환경별 prefix(`portfolio/paper/...`)로 분리.
- 관련 spec: 03, 09.

## AWS Batch

- 한 줄 설명: 장시간 / 가변 자원 batch job 실행 서비스.
- 역할: `port_strategy_research` 백테스트 1순위 후보. Step Functions에서 호출 가능.
- 비용 발생: 내부 compute env(Fargate 또는 EC2) 단가 그대로. AWS Batch 자체 단가 없음.
- 조심할 점: job queue 우선순위, 동시 실행 수 제한, 장시간 실행 중 RDS connection 누수 점검.
- 관련 spec: 09 strategy-research-batch.

## KMS (Key Management Service)

- 한 줄 설명: 암호화 키 관리 서비스.
- 역할: RDS encryption at rest, Secrets Manager, S3 SSE-KMS, ECR 이미지 암호화 옵션의 키 보관.
- 비용 발생: CMK $1.0/월/key + API calls. AWS 관리형 키는 무료.
- 조심할 점: aws-paper는 KMS default 권고. aws-live는 CMK 권고이나 IAM Policy에서 KMS 권한을 빠뜨리면 암호화/복호화 실패.
- 관련 spec: 02, 06.

## Cloud Map / Service Discovery

- 한 줄 설명: ECS 서비스 이름으로 내부 DNS 이름을 자동 등록하는 service discovery.
- 역할: port-view ECS Service에서 marketconnector EC2 또는 다른 ECS Service에 ALB 없이 내부 호출. ALB 비용을 줄이는 1차 대안.
- 비용 발생: 매우 작음. 등록된 서비스 / 호스팅 영역 기준.
- 조심할 점: DNS TTL 짧게 유지. 새 Task 기동 시 IP 변경 반영 시간을 application 측이 견딜 수 있어야 한다.
- 관련 spec: 05.

## SSM RunCommand

- 한 줄 설명: AWS Systems Manager 의 원격 명령 실행 기능. EC2 / on-prem 에 대해 `AWS-RunPowerShellScript` / `AWS-RunShellScript` 등 document 로 명령 실행.
- 단가 가정: 사실상 0(API 호출 비용 없음). SSM Endpoint(VPC Endpoint) 비용은 OD-NET-005 권고 세트에 이미 포함.
- 운영 주의: 본 프로젝트는 (a) MarketConnector EC2 의 `CONNECTOR_BALANCE` 등 단발 명령 진입점, (b) Windows EC2 worker 의 KRX worker Scheduled Task trigger(`schtasks /Run /TN "Portfolio-KRX-Worker-Daily"`) 진입점으로 사용. 직접 wrapper 또는 Python 실행은 SYSTEM Session 0 / 비대화형 GUI 한계로 KRX GUI 로그인에 부적합(OD-MS-022 정합).
- 관련 spec: 03, 08.

## Windows Scheduled Task

- 한 줄 설명: Windows OS 내장 작업 스케줄러. 등록된 task 를 시간 trigger 또는 외부 trigger(`schtasks /Run`) 로 실행.
- 단가 가정: 0(Windows EC2 OS 기본 기능).
- 운영 주의: 본 프로젝트는 Windows EC2 worker 에서 KRX GUI worker 의 Administrator interactive session 실행 트리거로 사용. SSM RunCommand 가 SYSTEM Session 0 에서 실행되더라도 Scheduled Task 가 Administrator console interactive session 위에서 wrapper 를 실행하므로 Chrome GUI / Display 컨텍스트 확보 가능(OD-MS-015 / OD-MS-022 정합). Logon Mode `Interactive only`, Run As User `Administrator` 기준.
- 관련 spec: 08.

## Windows Autologon (Sysinternals)

- 한 줄 설명: Microsoft Sysinternals 도구. Windows 부팅 직후 지정 사용자(보통 Administrator)로 자동 로그인을 활성화해 console interactive session 을 자동 생성.
- 단가 가정: 0(도구 자체 비용 없음 / Windows EC2 OS 안에서만 동작).
- 운영 주의: **paper 전용 Windows worker 보안 예외**(OD-MS-022 / R-SEC-009 정합). 자동 로그인 자격 증명이 Windows registry / LSA secret 영역에 저장되어 관리자 권한 보유자에 의해 복호화될 가능성 존재 — Administrator password 는 본 spec 산출물 / 콘솔 캡처 / 로그 평문 기록 금지(`[REDACTED]` 만). RDP inbound 는 운영자 IP 한정 또는 SSM Session Manager 우선 사용. EC2 worker 작업 완료 후 stop 절차로 idle 노출 시간 최소화. 추후 전용 local user(예: `krxworker`) 로 전환 검토 / aws-live 적용은 별도 결정.
- 관련 spec: 08.

---

## 본 spec 작업 안전 제약

- 본 문서는 후속 spec(03 ~ 10)에서 입력으로 사용할 용어집이다.
- 본 문서 작성 시 실제 AWS 리소스를 만들거나 변경하지 않았다.
- 8개 MS의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog는 수정하지 않았다.
- 모든 secret 자리에 `[REDACTED]`만 사용했다. 실제 password / token / app key / app secret / 계좌번호 / webhook URL 값은 본 문서에 들어 있지 않다.
- 06-secrets-and-iam은 본 작업 시점에 작성하지 않는다.
