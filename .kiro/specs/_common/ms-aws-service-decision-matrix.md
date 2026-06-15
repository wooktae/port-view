# MS × AWS Service Decision Matrix — AWS Migration

본 문서는 PORT-STRATEGY-AI 8개 MS 각각에 대해 사용 가능한 AWS 서비스 후보를 넓게 비교하고, 운영 안정성 / 비용 / 포트폴리오 어필 관점의 최종 판단을 정리한 AWS Migration 전체의 8개 MS 서비스 선택 기준표다. `01-aws-migration-foundation`과 후속 spec(02 ~ 10)이 공통으로 참조하며, 각 spec의 design.md / decision-matrix.md는 ECS Fargate / EC2 / AWS Batch 중심 권고를 담고 있는데, 이 문서는 그 결정을 유지하면서도 Lambda / EKS / Elastic Beanstalk / App Runner / ECS on EC2 등 다른 후보가 왜 1순위가 아닌지를 명확히 한다.

본 문서는 문서일 뿐이며 실제 AWS 리소스 생성, IaC 작성, 8개 MS 코드 / README / AGENTS.md / docs / CHANGELOG / worklog 수정은 본 작업 범위가 아니다. 모든 secret은 `[REDACTED]`로만 표기한다. 단가는 서울 ap-northeast-2 기준 근사치이고 정확 값은 "AWS Pricing Calculator 확인 필요" 단서가 붙는다. 환경 모델은 local-dev / aws-paper / aws-live 3개를 따른다.

## 1. Overview

### 1.1 이 문서의 목적

- 8개 MS의 워크로드 특성을 AWS 컴퓨트 / orchestration / 데이터 / 보조 서비스 후보와 1:N 매핑한다.
- 운영 안정성 / 비용 / 워크로드 적합성 기준의 1순위 권고는 그대로 유지하되, 2순위 / 3순위 후보와 그 사유까지 한 화면에 정리한다.
- 운영자가 "왜 ECS인가"가 아니라 "왜 Lambda / EKS / Beanstalk / App Runner는 1순위가 아닌가"까지 근거를 가지고 결정할 수 있게 한다.
- 포트폴리오 어필 관점의 보강안을 별도로 제시한다(7~9장).

### 1.2 왜 현재 권고안만으로는 부족한가

- 현재 권고는 ECS Fargate 중심이라, 같은 결정만 반복해서 보면 AWS 서비스 다양성을 평가했는지 판단하기 어렵다.
- AWS Migration 포트폴리오 관점에서는 EC2 + EIP, ECS Fargate, AWS Batch, Step Functions, EventBridge Scheduler, Lambda(보조), Secrets Manager, SSM Parameter Store, CloudWatch, S3 등 합리적으로 넓은 조합을 보이는 것이 유리하다. 다만 학습용으로 EKS 같은 과한 서비스를 무리하게 끼워 넣으면 비용과 운영 부담이 커진다.
- 본 문서는 두 관점을 분리한다.
  - 운영 안정성 기준 권고: aws-paper / aws-live 1차 진입에 사용. 비용 / 가용성 / 운영 부담 우선.
  - 포트폴리오 어필 기준 보강 후보: 옵션 또는 후속 track. 운영 안정성을 해치지 않는 한도에서 추가.

### 1.3 두 관점 분리 요약

- 운영 안정성 기준 1순위 (aws-paper에 적용)
  - port-marketconnector: EC2 + EIP
  - port-view: ECS Fargate Service
  - port-interest-crawler: ECS Fargate Task (필요 시 ECS on EC2 승격)
  - port-interest-preprocessor: ECS Fargate Task
  - port_strategy_common: 별도 컴퓨트 없음, packaging only
  - port_strategy_decision: ECS Fargate Task + EventBridge Scheduler
  - port_strategy_execution: ECS Fargate Task + Step Functions + EventBridge Scheduler
  - port_strategy_research: AWS Batch 1순위 / ECS Fargate Task 2순위
- 포트폴리오 어필 기준 보강 (선택적)
  - 마켓커넥터: EC2 + EIP + SSM + CloudWatch Agent + S3 backup
  - port-view: ECS Fargate vs Elastic Beanstalk 비교 결과 본문에 포함(실제 운영은 ECS Fargate)
  - Daily Batch: Step Functions + EventBridge Scheduler + ECS RunTask
  - 인프라 알람: CloudWatch Alarm → SNS → Lambda → Slack webhook
  - research 결과물: AWS Batch + S3
  - secrets: Secrets Manager + SSM Parameter Store 분리
  - 컨테이너 레지스트리: ECR
  - EKS는 후속 optional track. appendix(7장)으로 분리.

### 1.4 Daily Batch 16단계 실측 근거 (2026-06)

본 권고는 다음 Daily Batch 16단계 실측 시간과 MS별 실행 빈도 / 역할을 함께 근거로 한다. 비용 수치 / 권고는 변경하지 않으며, 본 절은 결정의 근거 출처만 명시한다.

- 짧은 단발 step (수백 ms ~ 5초): CONNECTOR_BALANCE / CONNECTOR_ORDER_CHECK / BALANCE_REFRESH (marketconnector), DAILY_BUY_SIGNAL / DAILY_POSITION_SIGNAL (decision), DAILY_BUY_EXECUTION / DAILY_SELL_EXECUTION / DAILY_AUTO_BUY / DAILY_AUTO_SELL / SYNC_*_FILL / SYNC_BUY_POSITION (execution).
- 중간 길이 batch (45초 ~ 약 10분): BACKTEST_RESEARCH (research, 약 45초), BACKTEST_REPORT (research, 약 2초), PREPROCESSOR (preprocessor, 약 5분 54초), INTEREST_CRAWLER (crawler, 약 9분 53초).
- 실행 빈도(현재 기준): crawler / preprocessor / research / decision = 하루 1회. execution = 하루 1회 + 장중 반복. marketconnector = 필요 시 수시. port-view = 상시. port_strategy_common = 라이브러리(별도 실행 없음).

이 실측 데이터는 Lambda 1순위가 부적절한 이유의 직접 근거가 된다.

- INTEREST_CRAWLER 약 9분 53초와 PREPROCESSOR 약 5분 54초는 Lambda 15분 timeout / VPC cold start / RDS connection 누수 위험 영역 안에 있다. 본 spec은 두 step을 ECS Fargate Task 1순위로 둔다.
- BACKTEST_RESEARCH는 현재 약 45초이지만 백테스트 기간 확대 / 파라미터 실험 / 메모리 증가에 따라 분 단위 ~ 시간 단위로 늘어날 수 있다. vCPU·메모리 자유도가 큰 AWS Batch 1순위가 안전하다.
- DAILY_BUY_SIGNAL 1초 / DAILY_POSITION_SIGNAL 387ms처럼 현재 매우 짧은 step도 다중 schema read·write + 공통 전략 라이브러리 + sizing/signal 저장이 누적되면서 Lambda 무재시도 / connection 통제가 어려워진다. ECS Fargate Task + Step Functions로 idempotent / 무재시도 구분을 인프라 레벨에서 강제한다.
- SYNC_*_FILL / DAILY_AUTO_BUY / DAILY_AUTO_SELL은 현재는 1초 미만이지만 broker 호출이 들어가는 영역이라 live 자동 재시도 금지가 핵심이다. Lambda 단독으로 무재시도 정책을 안전하게 강제하기 어렵다.

위 근거는 5장 최종 권고안과 6장 보강안을 그대로 유지하는 방향으로만 사용한다. 본 spec의 1순위 / 2순위 / 비권고 결정은 변경하지 않는다.

## 2. Evaluation Criteria

각 후보를 평가할 때 사용하는 기준이다. 본 문서의 모든 표는 이 기준을 축약(`Low/Medium/High`, `Yes/No/Conditional`)해 쓴다.

| 기준 | 정의 | Low / Medium / High 의미 |
|------|------|--------------------------|
| Workload fit | 해당 MS의 워크로드 특성(상시 vs batch, stateful vs stateless, 외부 outbound IP 요구 등)에 얼마나 잘 맞는가 | High = 거의 그대로 매핑 / Medium = 일부 제약 / Low = 추가 작업 필요 |
| Cost | 환경 합산 월 비용 영향. 세부 단가는 [`cost-simulation.md`](./cost-simulation.md) + 02 spec의 [`02-aws-network-and-rds/decision-matrix.md`](./02-aws-network-and-rds/decision-matrix.md) 참고 | Low = $0~$30/월 / Medium = $30~$150/월 / High = $150+/월 (해당 MS만 기준) |
| Operational complexity | 운영자가 직접 패치 / 모니터링 / 배포해야 하는 부담 | Low = AWS 관리형 / Medium = 표준 운영 절차 / High = 자체 운영 필수 |
| Security risk | inbound 노출, IAM 과다 권한, secret 누수 가능성 | Low = SG / IAM로 잘 통제 / Medium = 추가 통제 필요 / High = 통제 어려움 |
| Failure / rollback complexity | 장애 시 진단 / 복구 / 롤백 절차의 복잡도 | Low = 무중단 또는 단순 재기동 / Medium = 단계 절차 / High = 수동 개입 다단계 |
| Local-to-AWS migration difficulty | 기존 로컬 운영 코드 / 환경변수 / 데이터 흐름을 옮기는 부담 | Low = 환경변수만 / Medium = Dockerfile 정리 / High = 코드 재구성 필요 |
| Portfolio showcase value | 이력서 / 포트폴리오에서 드러나는 어필도 | Low = 흔하다 / Medium = 일반적 / High = 차별화 |
| Long-term maintainability | 6개월~1년 단위로 운영자가 혼자 유지하기 쉬운가 | Low = 점점 부담 / Medium = 안정 / High = 자동화 잘 됨 |

본 문서의 평가는 동일 운영자(개인 + 단일 region) 가정. 팀 규모가 다르거나 multi-region 요구가 들어오면 결과가 달라질 수 있다.

## 3. AWS Service Candidate Glossary

세부 비용 / 운영 주의사항은 루트 공통 문서 [`aws-resource-glossary.md`](./aws-resource-glossary.md)를 참고한다. 본 절은 워크로드 적합성 위주로 짧게 정리한다. 각 서비스 항목은 용어집의 해당 헤딩으로 점프한다.

- [EC2](./aws-resource-glossary.md#ec2-elastic-compute-cloud): 가상 서버. stateful / 외부 IP 고정 / 단일 세션 / OS 레벨 패키지(Selenium 등)가 필요한 워크로드에 적합.
- [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) [Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) Service: 24/7 컨테이너 서비스. Spring Boot 운영 콘솔, REST API 백엔드처럼 항상 떠 있어야 하는 워크로드.
- [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) [Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) Task: cron 또는 트리거 기반 단발성 컨테이너 실행. 일일 batch / 전처리 / 단발 backtest에 적합.
- ECS on [EC2](./aws-resource-glossary.md#ec2-elastic-compute-cloud): 컨테이너지만 GPU / 큰 디스크 / Docker-in-Docker / 헤드리스 브라우저 등 EC2-only 기능이 필요한 경우.
- EKS: Kubernetes 관리형. 정책 표준화 / 멀티팀 / 거대 워크로드 운영에 적합. 1인 운영 + 단일 region에는 과한 경향. (용어집 별도 헤딩 없음. 본 spec 7장 EKS 검토 섹션 참고.)
- [Lambda](./aws-resource-glossary.md#lambda): 짧은(<15분) stateless 함수. 알람 fan-out / S3 trigger / 후처리 / lightweight validation에 적합. Selenium / KRX 로그인 / long-running upsert에는 부적합.
- [AWS Batch](./aws-resource-glossary.md#aws-batch): 장시간 / 가변 vCPU·메모리가 필요한 batch. backtest / report 생성 후보.
- [Step Functions](./aws-resource-glossary.md#step-functions): 여러 서비스 호출을 state machine으로 묶는 orchestration. retry / catch / 분기 / 운영자 승인 게이트 정의에 적합.
- [EventBridge Scheduler](./aws-resource-glossary.md#eventbridge-scheduler): cron / rate 기반 스케줄러. 일일 batch / intraday polling 트리거.
- Elastic Beanstalk: Java / Python 등 platform-as-a-service. 컨테이너 표준화에서 벗어나지만, Spring Boot WAR/JAR을 빠르게 띄우기 좋은 옵션. (용어집 별도 헤딩 없음. 본 spec 9장 Elastic Beanstalk / App Runner 검토 섹션 참고.)
- App Runner: 단일 컨테이너 PaaS. 자동 HTTPS / 자동 스케일. VPC 내부 자원 접근에 제약. (용어집 별도 헤딩 없음. 본 spec 9장 검토 섹션 참고.)
- [S3](./aws-resource-glossary.md#s3-simple-storage-service): 오브젝트 스토리지. token 백업 / research report / 일반 dump / 전송용 staging.
- EFS: 다중 instance 공유 파일시스템. token 파일을 EC2/ECS 모두에서 보고 싶을 때 후보. (용어집 별도 헤딩 없음.)
- [RDS](./aws-resource-glossary.md#rds-relational-database-service): 관리형 PostgreSQL. 본 프로젝트의 단일 portfolio DB.
- CloudWatch: 로그 / 메트릭 / 알람. ([Logs](./aws-resource-glossary.md#cloudwatch-logs) / [Metrics](./aws-resource-glossary.md#cloudwatch-metrics) / [Alarm](./aws-resource-glossary.md#cloudwatch-alarm) 별도 헤딩.)
- [Secrets Manager](./aws-resource-glossary.md#secrets-manager): 고민감 secret 저장 + rotation.
- [SSM Parameter Store](./aws-resource-glossary.md#ssm-parameter-store): 환경변수 / 설정 / SecureString.

## 4. 8개 MS별 AWS 서비스 후보 비교표

각 표 컬럼은 다음과 같다.

- AWS Service Option
- 가능 여부 (Yes / Conditional / No)
- 비용 수준 (Low / Medium / High, 해당 MS만 기준)
- 운영 난이도 (Low / Medium / High)
- 장점
- 단점
- 장애 / 보안 리스크
- 포트폴리오 어필도 (Low / Medium / High)
- 최종 판단 (1순위 / 2순위 / 비권고)

세부 비용 단가는 루트 공통 문서 [`cost-simulation.md`](./cost-simulation.md). 정확 값은 AWS Pricing Calculator 확인 필요.

### 4.1 port-marketconnector

KIS broker 연동 / Flask API / 단일 access_token.txt / broker outbound IP 등록 가능성. stateful 성격이 강하다.

| AWS Service Option | 가능 여부 | 비용 | 운영 난이도 | 장점 | 단점 | 장애 / 보안 리스크 | 어필도 | 최종 판단 |
|--------------------|-----------|------|-------------|------|------|-------------------|--------|-----------|
| [EC2](./aws-resource-glossary.md#ec2-elastic-compute-cloud) + [EIP](./aws-resource-glossary.md#elastic-ip-eip) | Yes | Medium | Medium | 고정 EIP로 broker IP 등록 / 단일 토큰 파일 / 단일 세션 보장 / SSM + CW Agent로 운영 | 24/7 단일 instance, 패치 / 백업 운영자 부담 | EC2 SG 잘못 시 외부 노출. SSM + SG 통제 필수 | High | **1순위** |
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) [Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) + NAT-free public + EIP 고정 안 됨 | Conditional | Medium | Medium | 컨테이너 표준화 | broker가 IP 등록 요구하면 NAT-free 환경에서 EIP 부여 어려움. 단일 토큰 파일 보존이 까다로움 | 토큰 동시 갱신 충돌 가능 | Medium | 2순위 |
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) [Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) + [NAT GW](./aws-resource-glossary.md#nat-gateway) [EIP](./aws-resource-glossary.md#elastic-ip-eip) | Conditional | High | Medium | NAT EIP를 broker에 등록 | NAT GW 비용 + 다른 outbound도 같이 NAT EIP로 나간다 | NAT GW 단일 AZ 장애 시 outbound 단절 | Medium | 비권고 (현 NAT-free 결정과 충돌) |
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) on [EC2](./aws-resource-glossary.md#ec2-elastic-compute-cloud) | Conditional | Medium | High | EC2 EIP 부여 + 컨테이너 운영 가능 | EC2 자체 운영 + ECS 운영 부담 동시. 1인 운영에 과함 | EC2 + ECS 두 군데 모니터링 | Medium | 비권고 |
| EKS | Conditional | High | High | Kubernetes 표준화 | 단일 MS에 EKS는 과함. 비용 / 운영 둘 다 무거움 | etcd / control plane / addon 모니터링 부담 | High | 비권고 |
| [Lambda](./aws-resource-glossary.md#lambda) | No | n/a | n/a | n/a | Flask + token 파일 + 상시 broker 세션은 Lambda에 부적합 | 15분 timeout / 동시성 / 토큰 파일 미공유 | Low | 비권고 |
| Elastic Beanstalk | Conditional | Medium | Medium | 빠른 배포 | EIP 고정 + 단일 인스턴스 강제 + 토큰 파일 보존이 어렵다 | EB 환경 재기동 시 토큰 분실 위험 | Low | 비권고 |
| App Runner | No | n/a | n/a | n/a | 외부 EIP 부여 어려움. VPC 내부 RDS 접근에 별도 connector 필요 | broker IP 등록 정책에 부합 어려움 | Low | 비권고 |

### 4.2 port-view

Spring Boot / Thymeleaf 통합 운영 콘솔. 24/7. Daily Batch orchestration 호출 주체. SlackNotificationService 보유. JVM 메모리 일정 수준 필요.

| AWS Service Option | 가능 여부 | 비용 | 운영 난이도 | 장점 | 단점 | 장애 / 보안 리스크 | 어필도 | 최종 판단 |
|--------------------|-----------|------|-------------|------|------|-------------------|--------|-----------|
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) [Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) Service | Yes | Medium | Low | 컨테이너 표준 / Task Definition revision / 무중단 배포 / Step Functions와 자연스럽게 연결 | 첫 셋업에 IAM / SG / Task Definition 학습 필요 | 표준 패턴이라 통제 쉬움 | High | **1순위** |
| Elastic Beanstalk (Tomcat / Corretto) | Yes | Medium | Low | Spring Boot WAR/JAR 빠른 배포. 운영자에게 친숙 | 컨테이너 표준에서 멀어짐. EB 환경 자체의 패치 / 버전 lock-in. Daily Batch 호출 패턴 변경 시 EB 안에 가두기 부담 | EB 자체 장애 시 진단 어려움 | Medium | 2순위 (참고) |
| App Runner | Conditional | Medium | Low | 자동 HTTPS / 자동 스케일 / 가장 단순 | VPC 내부 RDS / marketconnector EC2 호출에 VPC connector 추가 필요. ALB 세분 통제 어려움 | 외부 노출 통제가 ALB만큼 세밀하지 않다 | Medium | 비권고 |
| [EC2](./aws-resource-glossary.md#ec2-elastic-compute-cloud) (단일 JVM) | Yes | Medium | Medium | EC2 자유도 / SSM 직접 / EB·ECS 학습 부담 회피 | 무중단 배포 직접 구현 / 패치 부담 / 컨테이너 표준 미적용 | EC2 단일 SPOF | Low | 비권고 |
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) on [EC2](./aws-resource-glossary.md#ec2-elastic-compute-cloud) | Yes | Medium | High | EC2 자유도 + 컨테이너 표준 | 24/7 EC2 + ECS 동시 운영 부담 | 두 군데 모니터링 | Medium | 비권고 |
| EKS | Yes | High | High | Kubernetes 표준화 | 단일 운영자 / 단일 MS에 K8s는 과함 | control plane 비용 + addon 부담 | High | 비권고 (appendix 7장 검토) |
| [Lambda](./aws-resource-glossary.md#lambda) | No | n/a | n/a | Spring Boot 24/7에 부적합 | cold start / 15분 / JVM 메모리 / 세션 stateful | n/a | Low | 비권고 |

### 4.3 port-interest-crawler

Naver / yfinance / KRX 수집. KRX는 Selenium / Chrome 의존. KRX 로그인은 stateful. 일부 backfill은 long-running.

| AWS Service Option | 가능 여부 | 비용 | 운영 난이도 | 장점 | 단점 | 장애 / 보안 리스크 | 어필도 | 최종 판단 |
|--------------------|-----------|------|-------------|------|------|-------------------|--------|-----------|
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) [Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) Task ([Public Subnet](./aws-resource-glossary.md#public-subnet), NAT-free) | Yes | Low | Low | 외부 outbound 가능 / cron 트리거 / 컨테이너 안정 / Step Functions와 결합 쉬움 | public subnet SG 통제 필요 | SG 실수 시 외부 inbound 위험 | High | **1순위** |
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) on [EC2](./aws-resource-glossary.md#ec2-elastic-compute-cloud) (crawler 전용 EC2) | Yes | Medium | Medium | Chrome / 큰 디스크 / 캐시 / 헤드리스 안정성 | EC2 운영 부담 | EC2 패치 / 모니터링 추가 | Medium | 2순위 (Selenium 안정성 미달 시 승격) |
| [EC2](./aws-resource-glossary.md#ec2-elastic-compute-cloud) (단일 인스턴스) | Yes | Medium | Medium | 컨테이너 없이 직접 운영 / 디버깅 쉬움 | 24/7 비용 / 컨테이너 표준 미적용 | EC2 SPOF | Low | 비권고 |
| [AWS Batch](./aws-resource-glossary.md#aws-batch) ([Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type)) | Conditional | Low | Medium | 동시 다수 backfill 잘 맞음 | 일일 단일 실행에는 과함. Selenium 컨테이너는 동일하지만 Batch state 관리 추가 학습 필요 | Batch 큐 모니터링 추가 | High | 2순위 (history backfill용) |
| [Lambda](./aws-resource-glossary.md#lambda) | No | n/a | n/a | Selenium / Chrome / KRX 로그인 / 15분 timeout 모두 위험 | Lambda layer Chrome은 OS 의존성 깨짐 / 동시성 / cold start | KRX 세션 끊김 | Low | 비권고 |
| EKS | Conditional | High | High | Kubernetes job / cronjob | 단일 운영자에 과함 | etcd / control plane | High | 비권고 |
| Elastic Beanstalk | No | n/a | n/a | batch 워크로드에 부적합 | n/a | n/a | Low | 비권고 |
| App Runner | No | n/a | n/a | batch / 외부 outbound IP 통제 안 됨 | n/a | n/a | Low | 비권고 |

### 4.4 port-interest-preprocessor

raw → pre feature 가공. DB upsert 비중. 일부 step은 외부 holiday API 호출. 거의 모든 step idempotent.

| AWS Service Option | 가능 여부 | 비용 | 운영 난이도 | 장점 | 단점 | 장애 / 보안 리스크 | 어필도 | 최종 판단 |
|--------------------|-----------|------|-------------|------|------|-------------------|--------|-----------|
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) [Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) Task | Yes | Low | Low | long upsert 안정 / 컨테이너 표준 / Step Functions에 자연스럽게 끼움 | 매우 짧은 step에는 약간 over-spec | NAT-free에서 holiday API outbound는 public subnet 필요 | High | **1순위** |
| [Lambda](./aws-resource-glossary.md#lambda) | Conditional | Low | Low | 짧은 step / 무료 한도 / 빠른 트리거 | long upsert에서 15분 timeout 위험. RDS connection 누수 위험. VPC 연결 시 cold start 증가 | timeout 시 idempotent 재시도 필요 | Medium | 2순위 (짧은 step만) |
| [AWS Batch](./aws-resource-glossary.md#aws-batch) | Conditional | Low | Medium | 장시간 backfill 가능 | 일일 batch에는 과함 | Batch 큐 모니터링 | Medium | 2순위 (backfill용) |
| [EC2](./aws-resource-glossary.md#ec2-elastic-compute-cloud) | No | n/a | n/a | 24/7 미필요 | 불필요한 비용 | n/a | Low | 비권고 |
| EKS | No | n/a | n/a | 과함 | 비용 / 운영 | n/a | High | 비권고 |
| Elastic Beanstalk / App Runner | No | n/a | n/a | batch 부적합 | n/a | n/a | Low | 비권고 |

### 4.5 port_strategy_common

Python 순수 라이브러리. DB / HTTP / IO 없음. 별도 컴퓨트 없음.

| AWS Service Option | 가능 여부 | 비용 | 운영 난이도 | 장점 | 단점 | 장애 / 보안 리스크 | 어필도 | 최종 판단 |
|--------------------|-----------|------|-------------|------|------|-------------------|--------|-----------|
| 별도 컴퓨트 없음 + git submodule packaging | Yes | Low | Low | 다른 MS Dockerfile에서 `pip install ./port_strategy_common`. 단순 / 비용 0 | 모든 MS 이미지 빌드 시점에 동기화 필요 | 버전 표시는 git-sha 기반 | Medium | **1순위** |
| 별도 컴퓨트 없음 + wheel/sdist + CodeArtifact | Yes | Low | Medium | 버전 명시 / 의존성 lock 가능 | CodeArtifact 학습 / 추가 비용 / OIDC 통합 | 잘못된 버전 publish 시 모든 MS에 영향 | High | 2순위 (성숙기) |
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) [Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) Service / Task | No | n/a | n/a | 본 라이브러리는 컴퓨트 대상 아님 | n/a | n/a | Low | 비권고 |
| [Lambda](./aws-resource-glossary.md#lambda) layer | Conditional | Low | Low | 다른 Lambda에서 layer로 import | 본 프로젝트는 Lambda 핵심 워크로드가 적어 효과 미미 | layer 버전 관리 | Medium | 비권고 |
| EKS | No | n/a | n/a | n/a | n/a | n/a | Low | 비권고 |

### 4.6 port_strategy_decision

daily BUY signal + daily position HOLD/SELL decision. 일일 batch 1~2회. RDS 다수 schema read + decision write. idempotent.

| AWS Service Option | 가능 여부 | 비용 | 운영 난이도 | 장점 | 단점 | 장애 / 보안 리스크 | 어필도 | 최종 판단 |
|--------------------|-----------|------|-------------|------|------|-------------------|--------|-----------|
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) [Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) Task + [EventBridge Scheduler](./aws-resource-glossary.md#eventbridge-scheduler) | Yes | Low | Low | cron 트리거 + 컨테이너 표준 + Step Functions step으로 결합 가능 | 학습 비용 약간 | 표준 패턴 | High | **1순위** |
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) [Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) Task + [Step Functions](./aws-resource-glossary.md#step-functions)(상위) | Yes | Low | Medium | retry / 분기 / 운영자 승인 게이트 표현 | Step Functions transitions 비용은 작지만 학습 부담 | live 자동 재시도 금지 정책을 state machine으로 강제 가능 | High | 1순위 (`04-strategy-batch-stepfunctions`에서 통합) |
| [AWS Batch](./aws-resource-glossary.md#aws-batch) | Conditional | Low | Medium | 동시 다수 day 재처리 | 일일 단일 실행에는 과함 | Batch 큐 모니터링 | Medium | 비권고 |
| [Lambda](./aws-resource-glossary.md#lambda) | Conditional | Low | Low | 짧은 step에 매력 | 다수 schema read + sizing 같은 step에서 timeout / 메모리 한계 가능 | RDS connection 누수 위험 | Medium | 비권고 |
| [EC2](./aws-resource-glossary.md#ec2-elastic-compute-cloud) cron | Yes | Medium | Medium | 단순 | 24/7 EC2 비용 / 패치 부담 | SPOF | Low | 비권고 |
| EKS CronJob | Yes | High | High | Kubernetes 표준화 | 단일 batch에 EKS 과함 | n/a | High | 비권고 (appendix 7장) |
| Elastic Beanstalk / App Runner | No | n/a | n/a | batch 부적합 | n/a | n/a | Low | 비권고 |

### 4.7 port_strategy_execution

execution order 생성 / connector 주문 호출 / fill sync / position sync / intraday monitor. live BUY/SELL 자동 재시도 금지 정책 핵심 적용 대상. 일부 step은 idempotent, 일부는 절대 idempotent 아님.

| AWS Service Option | 가능 여부 | 비용 | 운영 난이도 | 장점 | 단점 | 장애 / 보안 리스크 | 어필도 | 최종 판단 |
|--------------------|-----------|------|-------------|------|------|-------------------|--------|-----------|
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) [Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) Task + [EventBridge Scheduler](./aws-resource-glossary.md#eventbridge-scheduler) + [Step Functions](./aws-resource-glossary.md#step-functions) | Yes | Low~Medium | Medium | 일일 + 장중 step을 state machine으로 분리. retry 정책을 step별로 다르게 줄 수 있어 BUY/SELL은 무재시도, fill sync 일부는 idempotent 재시도 가능 | Step Functions 학습 / state 설계 부담 | live 자동 재시도 금지를 인프라 레벨에서 강제 | High | **1순위** |
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) [Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) Service (intraday 상시) | Conditional | Medium | Medium | 장중 polling을 상시 서비스로 운영 | 거래시간 외 idle 비용 | 상시 떠 있어 비용 + 모니터링 | Medium | 2순위 |
| [AWS Batch](./aws-resource-glossary.md#aws-batch) | Conditional | Low | Medium | 다수 backfill / order 재처리 | 일일 / 장중 단발 step에는 과함 | Batch 큐 모니터링 | Medium | 비권고 |
| [Lambda](./aws-resource-glossary.md#lambda) | No | n/a | n/a | broker 호출 / fill sync는 Lambda 환경에 부적합 (15분 timeout / connection 안정성) | 자동 재시도 정책 강제 어려움 | live 환경에서 자동 재시도 사고 위험 | Low | 비권고 |
| [EC2](./aws-resource-glossary.md#ec2-elastic-compute-cloud) cron | Yes | Medium | Medium | 단순 | live 자동 재시도 정책 강제 어려움 | SPOF + retry 통제 약함 | Low | 비권고 |
| EKS | Yes | High | High | k8s job 표준화 | 1인 운영 과함 | etcd / addon | High | 비권고 (appendix 7장) |

### 4.8 port_strategy_research

backtest 실행 / analysis / 텍스트 report 생성. 비정기. 한 번 돌리면 길어질 수 있다(수십 분~수 시간).

> [2026-06-15 보강] 본 일자에 운영자가 직접 수행한 (a) AWS Batch Compute Environment / Job Queue / Job Definition revision 1 / 3 + CloudWatch Log Group + Secrets Manager + IAM Execution / Job Role 신규 생성, (b) py_compile smoke + DB smoke SubmitJob 1차 검증, (c) BACKTEST_RESEARCH full + BACKTEST_REPORT 본 phase 단건 SubmitJob `SUCCEEDED` / exitCode 0 검증(`run_id a39b0b0c-...` / `total_return 4.55930879` / `mdd -0.08941942` / `sharpe 2.65561307` / `trade_count 308`), (d) BACKTEST_REPORT 산출물의 S3 prefix 한정 보존 검증으로 본 절의 1순위(AWS Batch + S3) 권고가 1차 실증되었다. View Daily Batch 기준 AWS Batch 포팅 대상은 `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종으로 한정되었고(OD-MS-019), `run_extended_analysis.py` 는 `BACKTEST_RESEARCH` 내부에서 이미 수행되어 별도 AWS Batch 포팅 대상에서 제외되었다. `block_watch_*` / `block_exception_buy_*` 4종은 heavy 분류로서 운영자 수동 보조 도구로만 분류된다. 1순위 / 2순위 / 비권고 표 자체의 권고는 변경하지 않는다. 자세한 결과는 [`../09-strategy-research-batch/operation-notes.md`](../09-strategy-research-batch/operation-notes.md) 2026-06-15 3개 섹션 참조.

| AWS Service Option | 가능 여부 | 비용 | 운영 난이도 | 장점 | 단점 | 장애 / 보안 리스크 | 어필도 | 최종 판단 |
|--------------------|-----------|------|-------------|------|------|-------------------|--------|-----------|
| [AWS Batch](./aws-resource-glossary.md#aws-batch) ([Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) compute env) + [S3](./aws-resource-glossary.md#s3-simple-storage-service) (report) | Yes | Low~Medium | Medium | 장시간 / 동시 다수 backtest / vCPU·메모리 설정 자유 / report S3 보관. [2026-06-15 보강] 1차 실증 통과(BACKTEST_RESEARCH full + BACKTEST_REPORT 4개 리포트 + S3 업로드) | Batch state machine 학습 | Batch 큐 / RDS connection 누수 점검 | High | **1순위** |
| [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) [Fargate](./aws-resource-glossary.md#fargate-ecs-launch-type) Task | Yes | Low | Low | 단발 backtest에 단순 | 1시간 이상 backtest는 retry 부담 / 동시 실행 제한 직접 관리 | Fargate Task 동시성 / RDS connection | High | 2순위 |
| [Step Functions](./aws-resource-glossary.md#step-functions) + [ECS](./aws-resource-glossary.md#ecs-elastic-container-service) RunTask 또는 [Batch](./aws-resource-glossary.md#aws-batch) SubmitJob | Yes | Low | Medium | run → analysis → report 단계 묶기 좋음 | 학습 부담 | state machine 진단 | High | 1순위 보조 |
| [EC2](./aws-resource-glossary.md#ec2-elastic-compute-cloud) cron / 단일 EC2 backtest 머신 | Yes | Medium | Medium | 단순 | 24/7 idle 비용 또는 매번 start/stop 부담 | SPOF | Low | 비권고 |
| [Lambda](./aws-resource-glossary.md#lambda) | No | n/a | n/a | 장시간 / 큰 메모리 부적합 | 15분 timeout | n/a | Low | 비권고 |
| EKS | Conditional | High | High | k8s job 표준화 | 1인 운영 과함 | etcd / addon | High | 비권고 (appendix 7장) |

## 5. MS별 최종 권고안 (운영 안정성 / 비용 / 워크로드 적합성 기준)

| MS | 1순위 | 2순위 | 비권고 | 핵심 사유 |
|----|-------|-------|--------|----------|
| port-marketconnector | EC2 + EIP | ECS Fargate (broker IP 정책 변경 시) | Lambda / Beanstalk / App Runner | broker IP 등록 + 단일 access_token + 단일 세션. EC2 + EIP가 워크로드와 가장 정합. NAT-free 환경에서 EIP 고정 가능 |
| port-view | ECS Fargate Service | Elastic Beanstalk | App Runner / EC2 / EKS | Spring Boot 24/7 + Daily Batch orchestration. 컨테이너 표준 + Step Functions와 자연스러운 결합 |
| port-interest-crawler | ECS Fargate Task (NAT-free public) | ECS on EC2 (Selenium 안정성 미달 시) | Lambda / Beanstalk / App Runner | Selenium/Chrome + KRX 로그인 + cron. Fargate Task가 워크로드와 가장 정합. Lambda는 Selenium / 15분 timeout 부적합 |
| port-interest-preprocessor | ECS Fargate Task | Lambda(짧은 step만) / AWS Batch(backfill) | EC2 / EKS / Beanstalk / App Runner | long upsert + idempotent. Fargate Task가 가장 단순 |
| port_strategy_common | 별도 컴퓨트 없음 (git submodule packaging) | wheel + CodeArtifact (성숙기) | ECS / EC2 / Lambda / EKS | 순수 라이브러리. 컴퓨트 대상 아님 |
| port_strategy_decision | ECS Fargate Task + EventBridge Scheduler (+ Step Functions in 04) | AWS Batch (다수 day 재처리) | Lambda / EC2 / EKS / Beanstalk / App Runner | daily idempotent batch. cron + 컨테이너로 충분 |
| port_strategy_execution | ECS Fargate Task + EventBridge Scheduler + Step Functions | ECS Fargate Service (intraday 상시) | Lambda / EC2 / EKS / Beanstalk / App Runner | live 자동 재시도 금지 정책을 step별로 인프라 레벨에서 강제하기 위해 Step Functions 1순위 |
| port_strategy_research | AWS Batch + S3 (Step Functions 보조) | ECS Fargate Task | Lambda / EC2 / EKS / Beanstalk / App Runner | 장시간 backtest + report 산출. Batch가 vCPU/메모리 자유도 + 동시 실행 모두 우수. [2026-06-15 보강] AWS Batch Compute Environment `portfolio-paper-strategy-research-ce`(MANAGED / FARGATE / maxvCpus 4) + Job Queue `portfolio-paper-strategy-research-queue`(priority 10) + Job Definition revision 1 / 3 등록 후 BACKTEST_RESEARCH full + BACKTEST_REPORT 본 phase 단건 SubmitJob `SUCCEEDED` / exitCode 0 으로 1순위 1차 실증 통과(`run_id a39b0b0c-...` / `total_return 4.55930879` / `mdd -0.08941942` / `sharpe 2.65561307` / `trade_count 308`). report artifact 는 S3 prefix `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/`(OD-MS-019 정합) 한정 보존. AWS Batch 1순위 / ECS Fargate Task 2순위 / Lambda 비권고 권고 변경 없음 |

권고가 ECS Fargate에 집중되는 사유는 다음과 같다.

- 8개 MS 중 6개가 컨테이너로 옮길 만한 표준 Python / Java 워크로드.
- 나머지 2개(marketconnector, common)는 워크로드 특성상 EC2 또는 packaging이 자연스러운 결정.
- Step Functions / EventBridge / Batch / S3 / Secrets Manager / SSM / CloudWatch / SNS / Lambda(보조) / ALB(옵션) 등을 함께 쓰면 실제 권고 자체로도 AWS 서비스 다양성이 충분히 드러난다.



## 6. 포트폴리오 어필 관점의 보강안

5장 권고를 그대로 두면 ECS 중심으로 보일 수 있다. 운영 안정성을 해치지 않는 한도에서 다음과 같이 AWS 서비스 다양성을 자연스럽게 더한다. 모든 항목은 본 spec 또는 후속 spec(03~10) 안에서 합리적인 위치를 갖는다.

### 6.1 보강 항목 매트릭스

| MS / 영역 | 운영 안정성 1순위 | 어필 보강 항목 | 위치(spec) | 추가 비용 | 추가 운영 부담 |
|-----------|-------------------|----------------|------------|-----------|----------------|
| port-marketconnector | EC2 + EIP | + SSM Session Manager + CloudWatch Agent + S3 token backup + Secrets Manager + SSM Parameter Store | 02 / 03 / 06 | EC2 단가 외 거의 무료 | 낮음 |
| port-view | ECS Fargate Service | + Elastic Beanstalk 비교 본문 + (선택) internal ALB + Cloud Map + Slack webhook | 05 | ALB 도입 시 ~$17/월 | 중 |
| Daily Batch orchestration | ECS Fargate Task | + Step Functions + EventBridge Scheduler + ECS RunTask | 04 | Step Functions transitions 무시 가능 | 학습 1회 |
| infra alarm | CloudWatch Alarm | + SNS + Lambda + Slack webhook fan-out | 05 / 10 | 무료 한도 안 | 낮음 |
| research artifact | AWS Batch | + S3 (report 보관) + S3 lifecycle | 09 | S3 storage ~$0.025/GB-월 | 낮음 |

> [2026-06-15 보강] research artifact 행은 본 일자에 1차 실증되었다. 기존 S3 bucket `portfolio-paper-migration-yukiever` 재사용 / Job Role 의 `s3:PutObject` Resource 는 `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` 한정 / public read 0건 / wildcard 0건. report wrapper 의 `REPORT_S3_BUCKET` 미설정 시 업로드 skip / 설정 시 `REPORT_S3_PREFIX=strategy-research/reports` 기본값 + `AWS_BATCH_JOB_ID` 기준 하위 경로 분리 / `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` 기본값 유지(Fargate ephemeral 영역 정합). S3 lifecycle 정책은 본 일자 미설정 — 후속 분리(R-COST-003 / 06 후속 phase 책임). KMS encryption 도 후속 spec 결정. 본 매트릭스의 권고(AWS Batch 1순위 + S3 보관 + S3 lifecycle 후속) 자체는 변경하지 않는다.
| secrets | Secrets Manager | + SSM Parameter Store SecureString(저민감) | 06 | secret 개당 $0.40/월 | 06에서 결정 |
| container registry | (자동 사용) | ECR + lifecycle policy + image scanning | 07 | storage ~$0.10/GB-월 | 낮음 |
| optional future | (없음) | EKS optional track / CodePipeline appendix | 7장 + 별도 spec | 추가 시 큼 | 본 spec 범위 밖 |

이렇게 보강하면 다음 카테고리가 자연스럽게 포트폴리오에 들어간다.

- 컴퓨트: EC2, ECS Fargate Service, ECS Fargate Task, AWS Batch
- orchestration: Step Functions, EventBridge Scheduler
- 보조: Lambda(infra alarm 전용), SNS, CloudWatch Logs / Metrics / Alarms
- 보안: Secrets Manager, SSM Parameter Store, IAM Role / Policy, KMS(옵션)
- 데이터: RDS for PostgreSQL, S3, EFS(옵션)
- 네트워크: VPC, Subnet, Route Table, Security Group, Internet Gateway, VPC Endpoint, Elastic IP, ALB(옵션)
- 운영: SSM Session Manager, CloudWatch Agent, ECR

### 6.2 무리하지 않는 원칙

- 어필 보강은 항상 운영 안정성 1순위 권고 위에 얹는다. 1순위를 바꾸지 않는다.
- 비용 영향이 있는 항목(ALB, NAT GW, EFS, EKS)은 본 spec의 NAT-free / aws-paper 절감 결정에 정합되도록 옵션으로만 두고, 운영자가 명시적으로 켜는 task가 있어야 한다.
- 포트폴리오 어필이 운영 사고로 이어지면 안 된다. 특히 live 자동 재시도 금지 정책은 어필을 위해 절대 풀지 않는다.

## 7. EKS 검토 섹션 (현재 비권고, 후속 optional track)

### 7.1 왜 이 프로젝트에 EKS는 과한가

- 운영 인원이 1인이고 다수의 짧은 batch(decision / execution / preprocessor / crawler)와 24/7 단일 서비스(port-view) 위주이다. ECS Fargate + EventBridge + Step Functions만으로 모두 표현 가능하다.
- EKS는 control plane 시간 단가($0.10/시간 × 730 ≈ ~$73/월) + worker node + addon(CoreDNS / VPC CNI / kube-proxy) + observability stack(Prometheus / Fluent Bit) 운영 부담이 누적된다.
- 1차 cutover에서 EKS를 도입하면 Kubernetes 자체 학습이 cutover 일정을 지연시킬 가능성이 크다.
- 본 프로젝트는 multi-AZ + multi-region 요구가 없고 multi-team 자원 분리도 필요 없다. 이런 요구가 들어오는 시점이 "EKS 도입 트리거"가 된다.

### 7.2 그래도 어필 관점에서의 장점

- "Kubernetes 운영 가능"을 이력서에 직접 적을 수 있다.
- IaC / GitOps / Helm / kustomize / Argo CD 같은 표준 도구를 함께 보일 수 있다.
- Kubernetes job / cronjob / horizontal pod autoscaler 등 추상화는 ECS Fargate 대비 표현력이 높다.
- 후속 면접 / 포트폴리오 리뷰에서 "왜 EKS 안 썼나"라는 질문에 본 문서로 답할 수 있다는 점도 어필에 도움이 된다.

### 7.3 EKS를 쓰려면 어느 MS가 가장 적합한가

- port-view + 전략 batch 묶음: 24/7 서비스 + 다수 cronjob을 한 cluster에서 표현 가능. 가장 자연스럽다.
- port-interest-crawler / preprocessor: cronjob 표현이 쉽지만 EKS 단독으로 옮길 만한 부피는 아니다. port-view와 묶어야 의미가 있다.
- port-marketconnector: EKS 위에서 운영하기 까다롭다(EIP 고정 + 단일 토큰 + 단일 세션). EC2 + EIP가 그대로 적합. EKS 적용해도 결국 statefulset + persistent volume + headless service로 표현해야 하므로 ECS 대비 이득이 적다.
- port_strategy_research: EKS Job으로 표현 가능하나 AWS Batch가 vCPU/메모리 자유도 + 동시 실행 정책이 더 직관적.

### 7.4 EKS 후속 optional track 작업 (요약)

- 별도 spec(예: `11-eks-migration-track`)을 만들어 다음 작업 흐름을 정리한다.
  - aws-paper 환경에서 EKS cluster 생성(`aws-paper-eks`).
  - port-view + 전략 batch 묶음을 Kubernetes manifest로 변환.
  - GitOps 도구(Argo CD or Flux) 도입.
  - Logs / Metrics를 CloudWatch Container Insights 또는 외부 Prometheus / Grafana로 통합.
  - rollback 절차로 ECS 환경을 일정 기간 함께 유지.
- 실제 진행은 본 spec 시리즈 1차 cutover(02~10) 완료 이후에 검토한다.

### 7.5 비용 / 운영 난이도 / 학습효과 비교

| 항목 | ECS Fargate + Step Functions | EKS |
|------|-----------------------------|-----|
| Control plane 비용 | 0 | ~$73/월 |
| Worker / Task 비용 | Fargate per-task | EC2 worker(자체) 또는 Fargate profile |
| 운영 난이도 | 중 | 상 |
| 1인 운영 적합도 | 높음 | 낮음 |
| 학습효과 (포트폴리오) | 중 | 매우 높음 |
| 1차 cutover 적합 | 매우 적합 | 부적합 |
| 후속 track 적합 | 그대로 유지 | 적합 |

권고: 본 spec 시리즈 1차 cutover 동안에는 EKS를 도입하지 않는다. 시리즈 완료 후 별도 optional spec으로 검토.

## 8. Lambda 검토 섹션 (핵심 batch에는 제한적, 보조에는 적합)

### 8.1 핵심 batch에 제한적인 사유

- Selenium / Chrome / KRX 로그인
  - Lambda layer Chrome은 OS 라이브러리 의존성이 자주 깨진다. Selenium 동시성 / cold start / 메모리 한계.
  - KRX 로그인은 stateful 세션. 동일 user-agent / 쿠키 / 토큰 유지가 필요하나 Lambda 환경에서 session affinity 보장 어렵다.
- long-running DB upsert
  - Lambda 최대 실행 시간 15분.
  - VPC 연결 시 cold start + ENI 부하로 RDS connection 누수 위험.
  - preprocessor의 일부 step이 15분을 넘길 가능성이 있어 안전하지 않다.
- broker 주문 / fill sync
  - 자동 재시도 정책을 step별로 분리하기 어렵다. live 자동 재시도 금지 정책을 인프라 레벨에서 강제하기 위해 Step Functions + ECS Task가 더 안전.

### 8.2 적합한 보조 용도

| 용도 | 설명 | 위치(spec) |
|------|------|-----------|
| Slack infra alarm relay | CloudWatch Alarm → SNS → Lambda → Slack webhook | 05 / 10 |
| small validation job | RDS row count 검증 / 환경변수 확인 / 짧은 health check | 02 / 10 |
| lightweight health check | marketconnector EC2의 broker reachability 확인 | 03 |
| post-batch notification | Step Functions 종료 시 결과 요약 메시지 | 04 |
| S3 report metadata processor | research report 업로드 시 metadata index 갱신 | 09 |

권고: Lambda는 본 spec에서 인프라 알람 fan-out과 짧은 보조 용도로만 사용한다. 핵심 batch는 ECS Fargate Task / AWS Batch를 사용한다.

## 9. Elastic Beanstalk / App Runner 검토 섹션 (port-view 기준)

### 9.1 비교표

port-view는 Spring Boot 운영 콘솔 + Daily Batch orchestration 호출 주체.

| 항목 | ECS Fargate Service | Elastic Beanstalk | App Runner |
|------|---------------------|-------------------|-----------|
| 워크로드 적합 | 24/7 컨테이너 표준 | Spring Boot 친화 | 단일 컨테이너 PaaS |
| 비용 | Fargate per-task 시간당 | EB 자체 무료 + EC2 / ALB 별도 | App Runner 시간 단가 + 데이터 |
| 배포 난이도 | Task Definition revision + service update | EB 환경 단위 배포 | 매우 단순(자동 빌드) |
| 운영 자유도 | 매우 높음 | 중. EB 추상화에 가두기 | 낮음. lock-in 강함 |
| ALB / 인증 / VPC 내부 호출 | 자유. internal ALB 또는 Service Discovery 선택 | EB 표준 ALB. 인증 게이트는 별도 | App Runner는 VPC 내부 자원 호출에 VPC connector 필요 |
| Daily Batch 연계성 | Step Functions + ECS RunTask 호출 자연스러움 | EB 안에서 호출 어색. EB → SF → ECS 흐름이 부자연스러움 | App Runner 내부에서 호출은 어색. 별도 호출 주체 필요 |
| Logs / Metrics | CloudWatch awslogs | EB 자체 + CW | App Runner 자체 + CW |
| 포트폴리오 어필 | 표준. 다른 항목과 결합해 다양성 강조 | 한국 운영자에게 친숙 | 단순 PaaS 어필 |
| 운영자 결정 권고 | 1순위 | 2순위 (참고) | 비권고 |

### 9.2 결정 근거

- port-view는 Daily Batch를 기동하는 주체이므로 Step Functions 호출과 ECS RunTask 호출 흐름이 자주 발생한다. ECS Fargate Service에 두면 같은 IAM Task Role / 같은 VPC SG / 같은 Logs 패턴으로 자연스럽게 통합된다.
- Elastic Beanstalk은 Spring Boot WAR/JAR을 빠르게 배포할 수 있지만 EB 자체 추상화 안에서 Daily Batch orchestration을 구성하기가 어색하다. EB는 운영자에게 친숙하지만 본 프로젝트의 통합 운영 콘솔이라는 역할에는 맞지 않는다.
- App Runner는 단일 컨테이너 단순 배포에 강하지만 VPC 내부 자원(RDS, marketconnector EC2)을 호출하려면 VPC connector를 추가로 두어야 하고, Daily Batch orchestration 주체로 두기에는 운영 자유도가 낮다.

권고: port-view는 ECS Fargate Service 1순위 유지. Elastic Beanstalk은 본 문서 안에 비교 결과로만 기록(어필 보강). App Runner는 비권고.

## 10. 최종 결론

### 10.1 운영 안정성 / 비용 우선 결론 (1차 cutover 권고)

| MS | 권고 | 비고 |
|----|------|------|
| port-marketconnector | EC2 + EIP | broker IP 등록, 단일 토큰, 단일 세션 |
| port-view | ECS Fargate Service | Daily Batch orchestration 자연스러운 결합 |
| port-interest-crawler | ECS Fargate Task (NAT-free public) | Selenium / Chrome 안정성 미달 시 ECS on EC2 승격 |
| port-interest-preprocessor | ECS Fargate Task | long upsert / idempotent 단순화 |
| port_strategy_common | 별도 컴퓨트 없음 (git submodule packaging) | 라이브러리, 컴퓨트 대상 아님 |
| port_strategy_decision | ECS Fargate Task + EventBridge Scheduler | daily idempotent batch |
| port_strategy_execution | ECS Fargate Task + Step Functions + EventBridge Scheduler | live 자동 재시도 금지 정책 강제 |
| port_strategy_research | AWS Batch + S3 (Step Functions 보조) | 장시간 backtest + report 산출 |

이 결론은 NAT-free + 단일 portfolio DB + schema-per-domain + 단일 region + 1인 운영 가정에 정합되며, `01-aws-migration-foundation`의 권고 라인을 그대로 유지한다.

### 10.2 포트폴리오 어필 / AWS 서비스 다양성 우선 결론

위 운영 안정성 결론을 그대로 두고, 다음을 조합해 다양성을 자연스럽게 보인다.

- 컴퓨트 다양성: EC2 (marketconnector) + ECS Fargate Service (port-view) + ECS Fargate Task (crawler / preprocessor / decision / execution) + AWS Batch (research)
- orchestration: Step Functions + EventBridge Scheduler + ECS RunTask
- 보조 / 알람: CloudWatch Alarm + SNS + Lambda + Slack webhook
- 보안: Secrets Manager + SSM Parameter Store + IAM Role / Policy + KMS(옵션)
- 데이터: RDS for PostgreSQL + S3 (research report, token backup)
- 네트워크: VPC + Subnet + IGW + VPC Endpoint(S3 / ECR / Secrets / SSM / Logs) + Security Group + Elastic IP + (옵션) ALB
- 배포 / 레지스트리: ECR + GitHub Actions OIDC role(`07-cicd-pipelines`)
- 운영 접근: SSM Session Manager (SSH 미사용)
- optional future: EKS는 후속 track으로 분리(7장 참고). 현 단계 도입 안 함.

### 10.3 현재 단계에서 가장 균형적인 조합

- 1차 cutover (aws-paper) 단계에서는 10.1 운영 안정성 결론을 그대로 적용한다.
- 위 결론에 6장 보강 항목을 함께 도입해 AWS 서비스 다양성을 자연스럽게 노출한다(ALB / EFS / EKS는 도입 보류).
- aws-paper에서 자동 BUY/SELL E2E 검증 통과 후 aws-live cutover에서도 동일 조합을 유지한다. live 자동 재시도 금지 정책은 Step Functions state machine에서 강제한다.
- 포트폴리오 어필을 더 키우고 싶으면 cutover 안정 운영 후 후속 track(EKS optional / GitOps / CodePipeline appendix)을 별도 spec으로 추가한다.

### 10.4 본 문서 안전 제약

- 실제 AWS 리소스 생성 금지.
- 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 금지.
- 실제 secret 출력 금지(모두 `[REDACTED]`).
- 정확 가격은 AWS Pricing Calculator 확인 필요. 본 문서 단가는 모두 근사치.
- 06-secrets-and-iam은 본 작업 시점에 작성하지 않는다.
