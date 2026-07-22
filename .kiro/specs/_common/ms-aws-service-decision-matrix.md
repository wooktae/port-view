# MS × AWS Service Decision Matrix — AWS Migration

## Purpose

본 문서는 PORT-STRATEGY-AI 8개 MS의 AWS 서비스 선택 결론을 빠르게 확인하기 위한 단일 진실원이다.

각 MS는 아래 항목을 `항목 / 값` 2열 표로 정리한다.

| 항목 | 값 |
| --- | --- |
| 1순위 | 최종 채택 서비스 |
| 2순위 | 조건부 대안 또는 후속 후보 |
| 채택 이유 | 워크로드 특성과 운영 기준 |
| 비용 | 주요 비용 발생 요인 |
| 운영 리스크 | 운영자가 관리할 핵심 위험 |
| 관련 spec | 설계·구현·운영 책임 spec |

서비스 선택 결론은 임의로 변경하지 않는다. 날짜별 실행 이력, executionName, Task Definition revision, ARN, smoke 결과, DB after-check, Risk·Decision 보강 내역은 본 문서에서 관리하지 않는다.

상세 운영 근거는 각 spec의 `operation-notes.md`, 결정 이력은 `operator-decisions.md`, 리스크는 `risk-register.md`에서 관리한다.

민감정보 원문은 기록하지 않고 `[REDACTED]` 계열 placeholder만 사용한다.

## 운영 원칙

| 항목 | 값 |
| --- | --- |
| 환경 | `local-dev` · `aws-paper` · `aws-live` |
| Region | 서울 `ap-northeast-2` |
| 운영 인원 | 1인 운영 기준 |
| 네트워크 | NAT Gateway 기본 미사용 |
| Database | RDS PostgreSQL · 단일 portfolio DB · schema-per-domain |
| 컨테이너 | ECS Fargate 중심 |
| orchestration | Step Functions + EventBridge Scheduler |
| 자동 재시도 | idempotent step만 허용 |
| 주문 관련 step | BUY · SELL · Fill Sync · Position 변경 자동 재시도 금지 |
| 운영 접근 | SSM Session Manager 우선 |
| 정확한 비용 | AWS Pricing Calculator에서 별도 확인 |

## Final Recommendation

### `port-marketconnector`

| 항목 | 값 |
| --- | --- |
| 1순위 | `EC2 + EIP` |
| 2순위 | ECS Fargate · broker IP 정책이 변경되는 경우 |
| 채택 이유 | broker 등록 IP 고정 · 단일 access token · 단일 세션 유지 |
| 운영 방식 | EC2에 EIP를 연결하고 SSM으로 운영 |
| 비용 | EC2 instance-hour · EBS · Public IPv4 |
| 운영 리스크 | EC2 교체 시 EIP 재연결 · broker 등록 IP 정합 · token/session 관리 |
| 비권고 | Lambda · Elastic Beanstalk · App Runner · EKS |
| 관련 spec | 03 · 06 · 10 |
| 상세 | [03 operation-notes](../03-marketconnector-ec2/operation-notes.md) |

### `port-view`

| 항목 | 값 |
| --- | --- |
| 1순위 | `ECS Fargate Service` |
| 2순위 | Elastic Beanstalk |
| 채택 이유 | Spring Boot 상시 서비스 · 컨테이너 표준 · Step Functions 연계 |
| 운영 방식 | View는 화면·제어 역할 · 실제 Batch는 Step Functions 실행 |
| 비용 | 상시 Fargate vCPU·Memory · Public IPv4 · ALB 도입 시 추가 비용 |
| 운영 리스크 | Task Role 최소 권한 · 외부 접근 통제 · 인증·HTTPS 후속 |
| 현재 상태 | ECS Fargate Service 1차 실증 상태 유지 |
| P2 미수행 | ALB · HTTPS · Route53 · 인증 · Auto Scaling · Blue/Green (현재 범위 제외) |
| 재검토 | 외부 공개 또는 다중 사용자 운영 필요 시 |
| 비권고 | App Runner · EC2 단독 · EKS · Lambda |
| 관련 spec | 05 · 06 · 07 · 10 |
| 상세 | [05 operation-notes](../05-port-view-ecs-and-runbook/operation-notes.md) |

### `port-interest-crawler`

| 항목 | 값 |
| --- | --- |
| 1순위 | `ECS Fargate Task + Windows EC2 worker` |
| 현재 운영 | Hybrid (non-GUI=ECS Fargate Task · KRX GUI=Windows EC2 interactive worker) |
| 장기 목표 | KRX headless 전환 가능 시 ECS 통합 재검토 |
| Fargate 역할 | Naver · yfinance · 비-GUI 수집 |
| Windows 역할 | KRX GUI 로그인·다운로드가 필요한 수집 |
| 2순위 | ECS on EC2 · Selenium 안정성 미달 시 |
| 보조 후보 | AWS Batch · 대량 history backfill |
| 채택 이유 | non-GUI와 GUI 워크로드의 실행 조건이 다름 |
| 비용 | Fargate 실행 시간 · Windows EC2 실행 시간 · Public IPv4 |
| 운영 리스크 | Windows interactive session · Autologon 보안 예외 · 종료 코드 판정 |
| 시간대 | `TZ=Asia/Seoul` 또는 timezone-aware 코드 필수 |
| 비권고 | Lambda · App Runner · Elastic Beanstalk |
| 관련 spec | 08 · 04 · 10 |
| 상세 | [08 operation-notes](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) |

### `port-interest-preprocessor`

| 항목 | 값 |
| --- | --- |
| 1순위 | `ECS Fargate Task` |
| 2순위 | Lambda · 짧은 step 한정 |
| 보조 후보 | AWS Batch · 대량 backfill |
| 채택 이유 | 장시간 upsert · idempotent batch · 실행 후 종료 |
| 비용 | Fargate vCPU·Memory 실행 시간 |
| 운영 리스크 | DB 권한 · 입력 데이터 최신성 · public subnet outbound |
| 시간대 | KST 기준 batch는 timezone 명시 |
| 비권고 | 상시 EC2 · EKS · Elastic Beanstalk · App Runner |
| 관련 spec | 08 · 04 · 10 |
| 상세 | [08 operation-notes](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) |

### `port_strategy_common`

| 항목 | 값 |
| --- | --- |
| 1순위 | `별도 컴퓨트 없음 · git submodule packaging` |
| 2순위 | wheel + CodeArtifact |
| 채택 이유 | 순수 Python 공통 라이브러리 · 독립 실행 워크로드 아님 |
| 비용 | git submodule 방식은 추가 AWS 비용 없음 |
| 운영 리스크 | MS별 image build 시점 동기화 · version drift |
| 비권고 | ECS · EC2 · Lambda · EKS |
| 관련 spec | 07 |
| 상세 | [07 spec 폴더](../07-cicd-pipelines/) |

### `port_strategy_decision`

| 항목 | 값 |
| --- | --- |
| 1순위 | `ECS Fargate Task + EventBridge Scheduler + Step Functions` |
| 2순위 | AWS Batch · 다수 날짜 재처리 |
| 채택 이유 | daily idempotent batch · 실행 후 종료 · 순서 제어 필요 |
| 구조 | Buy Signal · Position Signal Task Definition 분리 |
| 비용 | Fargate 실행 시간 · Step Functions transition |
| 운영 리스크 | schema 권한 · 입력 데이터 최신성 · KST 기준일 |
| 비권고 | Lambda 주 실행 · 상시 EC2 · EKS · App Runner |
| 관련 spec | 04 · 06 · 10 |
| 상세 | [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

### `port_strategy_execution`

| 항목 | 값 |
| --- | --- |
| 1순위 | `ECS Fargate Task + Step Functions + EventBridge Scheduler` |
| 2순위 | ECS Fargate Service · intraday 상시 처리 필요 시 |
| 채택 이유 | 주문 단계를 분리하고 자동 재시도 금지 정책을 workflow에서 강제 |
| 구조 | 단일 Task Definition + command override |
| 안전 원칙 | 주문 제출 · Fill Sync · Position 변경 step 자동 Retry 금지 |
| 비용 | Fargate 실행 시간 · Step Functions transition |
| 운영 리스크 | Retry 오설정 · 중복 주문 · 주문 체인 불일치 |
| 2026-07-22 실증 | 정상 Scheduler 자동 회차 성공 |
| validator 실증 | READY Plan → Order · Order Chain 자동 통과 |
| 정합 실증 | 주문 · Fill · Position 정합 확인 |
| 안정화 | Paper Daily 1차 안정화 완료 |
| 비권고 | Lambda · 상시 EC2 · EKS · Elastic Beanstalk · App Runner |
| 관련 spec | 03 · 04 · 05 · 10 |
| 상세 | [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

### `port_strategy_research`

| 항목 | 값 |
| --- | --- |
| 1순위 | `AWS Batch + S3 + Step Functions 보조` |
| 2순위 | ECS Fargate Task |
| 채택 이유 | 장시간 backtest · 가변 vCPU/Memory · 동시 실행 · report 산출 |
| Artifact | S3에 report 저장 |
| 비용 | Batch compute 사용량 · S3 storage |
| 운영 리스크 | heavy job 오실행 · 동시 실행 수 · RDS connection 누수 |
| 비권고 | Lambda · 상시 EC2 · EKS · Elastic Beanstalk · App Runner |
| 관련 spec | 09 · 06 · 10 |
| 상세 | [09 operation-notes](../09-strategy-research-batch/operation-notes.md) |

## Shared AWS Services

### Orchestration

| 항목 | 값 |
| --- | --- |
| Step Functions | Daily Batch · 승인 분기 · 실패 처리 · 순서 제어 |
| EventBridge Scheduler | 정기 실행 시작 |
| ECS RunTask | Batch container 실행 |
| Lambda | Dispatcher · Notifier · Builder · lifecycle 보조 |
| 운영 원칙 | Lambda를 8개 MS의 주 compute로 사용하지 않음 |

### Security

| 항목 | 값 |
| --- | --- |
| Secrets Manager | DB password · broker secret · webhook 등 고민감 정보 |
| SSM Parameter Store | 저민감 설정 · 환경별 configuration |
| IAM Role | ECS · EC2 · Lambda · Step Functions 권한 분리 |
| IAM Policy | Action과 Resource를 필요한 범위로 제한 |
| KMS | RDS · S3 · Secrets 암호화 옵션 |
| 운영 원칙 | Access Key 대신 Role 기반 접근 |

### Observability

| 항목 | 값 |
| --- | --- |
| CloudWatch Logs | ECS · EC2 · Lambda · RDS 로그 |
| CloudWatch Metrics | 인프라 및 업무 메트릭 |
| CloudWatch Alarm | 장애·실패·heartbeat 감지 |
| SNS | Alarm fan-out |
| Slack | 운영 알림 최종 전달 |
| 운영 원칙 | 핵심 알람만 유지해 알림 피로 방지 |

### Network and Data

| 항목 | 값 |
| --- | --- |
| VPC | paper/live 워크로드 공통 네트워크 |
| Public Subnet | 인터넷 outbound가 필요한 ECS Task와 EC2 |
| Private Subnet | RDS와 내부 워크로드 |
| VPC Endpoint (S3) | Gateway Endpoint |
| VPC Endpoint (ECR · Secrets · Logs) | Interface Endpoint |
| VPC Endpoint (SSM) | Public outbound 또는 선택형 Interface Endpoint |
| RDS PostgreSQL | portfolio 데이터의 source of truth |
| S3 | research artifact · backup · 장기 보관 |
| ECR | container image 저장 |
| 운영 원칙 | NAT Gateway 기본 미사용 |

## Rejected or Deferred Services

### Lambda

| 항목 | 값 |
| --- | --- |
| 판단 | 핵심 MS compute 비권고 · 보조 계층 적합 |
| 비권고 이유 | 15분 제한 · Selenium 부적합 · stateful session 어려움 |
| 주문 처리 | 자동 재시도 통제가 어려워 주 실행 환경으로 사용하지 않음 |
| 적합 용도 | Dispatcher · Notifier · 짧은 validation · Alarm relay |
| 재검토 조건 | 짧고 stateless하며 idempotent한 독립 작업 |

### EKS

| 항목 | 값 |
| --- | --- |
| 판단 | 현재 비권고 · 후속 optional track |
| 비권고 이유 | 1인 운영 대비 control plane과 cluster 운영 복잡도 과다 |
| 비용 | control plane · worker · addon · observability 비용 |
| 현재 대안 | ECS Fargate + Step Functions |
| 장점 | Kubernetes · GitOps · Helm · Argo CD 경험 |
| 재검토 조건 | multi-team · 대규모 workload · Kubernetes 표준화 필요 |
| 적용 후보 | port-view + 전략 batch 묶음 |

### Elastic Beanstalk

| 항목 | 값 |
| --- | --- |
| 판단 | port-view 2순위 |
| 장점 | Spring Boot 배포가 단순 |
| 비권고 이유 | Step Functions·ECS RunTask 통합 구조가 ECS보다 부자연스러움 |
| 비용 | EC2 · ALB 등 기반 리소스 비용 |
| 재검토 조건 | View 단독 PaaS 운영이 더 중요해지는 경우 |

### App Runner

| 항목 | 값 |
| --- | --- |
| 판단 | 현재 비권고 |
| 장점 | 단일 container 웹 서비스 배포가 단순 |
| 비권고 이유 | VPC 내부 호출 · IAM · Batch orchestration 자유도가 낮음 |
| 적용 가능성 | 단순 공개 웹 서비스로 역할이 축소되는 경우 |

### ECS on EC2

| 항목 | 값 |
| --- | --- |
| 판단 | 조건부 보류 |
| 장점 | Host 수준 제어 · Selenium·Chrome 환경 조정 |
| 비권고 이유 | EC2 patch · scaling · capacity 운영 부담 |
| 현재 대안 | non-GUI는 Fargate · KRX GUI는 Windows EC2 worker |
| 재검토 조건 | Fargate에서 container runtime 안정성을 확보하지 못하는 경우 |

### NAT Gateway

| 항목 | 값 |
| --- | --- |
| 판단 | paper · live 기본 미사용 |
| 장점 | Private Subnet의 일반 인터넷 outbound 단순화 |
| 비권고 이유 | 고정비와 데이터 처리 비용 |
| 현재 대안 | Public Subnet + Public IP · VPC Endpoint · EC2 + EIP |
| 재검토 조건 | Private Subnet outbound 요구가 크게 증가하는 경우 |

## Portfolio Appeal

### 서비스 구성

| 항목 | 값 |
| --- | --- |
| Compute | EC2 · ECS Fargate Service · ECS Fargate Task · AWS Batch |
| Orchestration | Step Functions · EventBridge Scheduler |
| Container | ECR · ECS · Fargate |
| Database | RDS PostgreSQL |
| Storage | S3 |
| Security | IAM · Secrets Manager · SSM Parameter Store · KMS |
| Network | VPC · Subnet · Route Table · SG · IGW · Endpoint · EIP |
| Observability | CloudWatch Logs · Metrics · Alarm · SNS |
| Operations | SSM Session Manager · RunCommand |
| CI/CD | GitHub Actions OIDC · ECR promotion |

### 적용 원칙

| 항목 | 값 |
| --- | --- |
| 우선순위 | 운영 안정성 > 비용 > 포트폴리오 다양성 |
| 서비스 추가 | 실제 운영 목적이 있는 경우에만 도입 |
| 고정비 서비스 | ALB · NAT Gateway · EKS · EFS는 기본 보류 |
| live 안전 | 포트폴리오 어필을 위해 자동 주문 안전장치를 완화하지 않음 |
| optional track | EKS · GitOps · CodePipeline은 1차 cutover 이후 검토 |

## Decision Summary

| 항목 | 값 |
| --- | --- |
| MarketConnector | EC2 + EIP |
| View | ECS Fargate Service |
| Crawler | ECS Fargate Task + Windows EC2 worker |
| Preprocessor | ECS Fargate Task |
| Strategy Common | 별도 컴퓨트 없음 |
| Strategy Decision | ECS Fargate Task + EventBridge Scheduler + Step Functions |
| Strategy Execution | ECS Fargate Task + Step Functions + EventBridge Scheduler |
| Strategy Research | AWS Batch + S3 |
| 핵심 기준 | NAT-free · 1인 운영 · paper 우선 검증 · 주문 자동 Retry 금지 |
| 결론 변경 | 없음 |

## Evidence Management

| 항목 | 값 |
| --- | --- |
| 본 문서 | 서비스 선택 결론과 판단 기준만 유지 |
| 날짜별 실행 이력 | `WORKLOG.md` |
| 상세 운영 증거 | 각 spec `operation-notes.md` |
| 결정 이력 | `operator-decisions.md` |
| 리스크와 mitigation | `risk-register.md` |
| 비용 상세 | `cost-simulation.md` |
| Evidence Details | 본 문서에 누적하지 않음 |
| executionName · ARN · SHA256 | 본 문서에 기록하지 않음 |

## Update Rules

| 항목 | 값 |
| --- | --- |
| 결정 변경 | 운영자 승인 후 Final Recommendation부터 수정 |
| 1순위 문자열 | 승인 없이 변경 금지 |
| 새 서비스 후보 | Rejected or Deferred Services에 먼저 추가 |
| 운영 실증 | 본문에 날짜별 메모를 추가하지 않고 operation-notes로 연결 |
| 중복 금지 | Final Recommendation과 Decision Summary 외 반복 설명 금지 |
| 표 형식 | 새 독립 표는 `항목 / 값` 2열 |
| 긴 셀 | 여러 행으로 분리 |
| 비용 | 서울 Region 근사치 · 정확 금액은 Pricing Calculator 확인 |
| 민감정보 | `[REDACTED]` 계열 placeholder만 사용 |

## Security Notes

| 항목 | 값 |
| --- | --- |
| 실제 AWS 실행 | 없음 |
| 애플리케이션 코드 수정 | 없음 |
| AWS 리소스 생성·수정·삭제 | 없음 |
| broker · KIS · DB 실행 | 없음 |
| aws-live 자동 주문 | 별도 승인 전까지 금지 |
| 민감정보 원문 | 기록 금지 |
| 허용 표기 | `[REDACTED]` 계열 placeholder |
| 문서 역할 | AWS 서비스 선택 단일 진실원 |
