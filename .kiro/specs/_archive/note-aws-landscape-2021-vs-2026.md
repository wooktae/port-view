# AWS 풍경, 2021 → 2026: 무엇이 변했고 왜 이번엔 ECS Fargate가 답인가

> 일회용 회고 노트. spec 산출물 아님. 본 프로젝트의 의사결정과 별개로, 운영자가 직접 구현했던 2021년 포트폴리오와 2026년 본 spec 권고가 왜 갈리는지 정리한다. 비용 수치는 모두 근사치이며 정확 단가는 AWS Pricing Calculator 확인이 필요하다. secret은 `[REDACTED]`만 사용한다.

## 1. 2021년 포트폴리오의 의도 (재확인)

첨부된 2021년 구성을 그대로 보면 **의도적으로 다양성을 보이는 카탈로그**다.

| MS | Compute | CI/CD 도구 | 의도 |
|----|---------|------------|------|
| API Communication | EC2 (Windows Server 2016) | CodeDeploy + CodePipeline + S3 | broker / Creon Plus 같이 OS 바인딩 강한 워크로드 |
| Interest Me | Lambda + API Gateway + Slack | CodeBuild + CodePipeline | 단발 trigger 함수 |
| Interest Agency | Lambda + CloudWatch + BeautifulSoup | CodeCommit + CodeBuild + CodePipeline | cron 크롤링 |
| Interest News | Lambda + Cloud9 + Selenium / BS4 | CloudFormation + CodePipeline | IaC 학습 |
| Strategy Algorithm | Fargate + ECS Blue/Green + ALB | CodeCommit + ECR + Docker + CodePipeline | 컨테이너 + 무중단 배포 |
| Strategy LSTM | EC2 + EKS + Flask | CodeCommit + ECR + CodeBuild + CodePipeline | Kubernetes 학습 |
| View | Elastic Beanstalk + Spring Boot + Java 8 | CodeCommit + Maven + CodeBuild + EB | PaaS |

이 구성의 본질은 "한 시스템 안에 EC2 / Lambda / Fargate / EKS / Beanstalk + 5종 CI/CD를 모두 보였다"는 것. 면접 / 포트폴리오 관점에서는 영리한 선택이었고, 실제로 AWS 자격증 + 운영 경험을 동시에 어필하기 좋은 카탈로그다.

다만 운영 일관성 / 디버깅 일관성 / 단일 운영자 부담 측면에서는 비효율이 컸다. 이력서용 카탈로그와 운영용 시스템은 평가 기준이 다르다.

## 2. 2021 → 2026, AWS는 실제로 무엇이 변했나

"거의 안 변했다"는 인상은 부분적으로 맞다. 하지만 구체적으로 짚으면 변화는 **주변부에 누적**되어 있다. 굵직한 것만 모으면:

### 컴퓨트 / 컨테이너

- Lambda: 컨테이너 이미지 지원(2020 말, 2021부터 본격 활용), 10GB 메모리, ENI 재사용으로 VPC cold start 완화. SnapStart(2022, Java 한정)로 Spring Boot Lambda도 가능 범주로 진입. 다만 **15분 timeout은 그대로**.
- ECS Fargate: Spot 가격(2022), ARM Graviton2/3 지원, ECS Exec(SSM 기반 셸), capacity provider, 더 빠른 task 기동.
- EKS: Fargate profile 안정화, EKS Auto Mode(2024)로 노드 관리 부담 일부 흡수. 그러나 **control plane 시간 단가와 addon 운영 부담은 본질적으로 그대로**.
- App Runner(2021): 도입 자체가 변화. 단일 컨테이너 PaaS. VPC connector(2022)로 RDS 접근 가능해졌지만 운영 자유도는 ECS만큼 못 따라간다.
- Lambda Function URL(2022), Lambda Adapter(2022)로 컨테이너 → Lambda 마이그레이션 친화도 상승.

### Orchestration / Eventing

- **EventBridge Scheduler(2022)**: 본 프로젝트가 Daily Batch 트리거에 직접 쓰는 그 서비스. 2021에는 CloudWatch Events rule을 cron 식으로 쓰는 것 외에 마땅한 것이 없었다.
- Step Functions Distributed Map(2022): 수십 ~ 수만 step를 병렬화 가능. backtest 파라미터 sweep에 잠재적으로 유용.
- VPC Lattice(2023): VPC 간 / 서비스 간 통신 추상화. 1인 운영에는 과함.
- AWS Verified Access(2023): VPN 없이 ZTNA. 운영자 콘솔 접근에 활용 가능.

### 보안 / IAM

- IAM Roles Anywhere(2022): 외부 워크로드(예: 사내 PC, GitHub Actions)도 IAM Role을 임시 자격으로 사용. 결과적으로 OIDC 흐름이 표준이 되었다.
- GitHub Actions OIDC(2021 말): IAM access key를 CI에 박지 않고 단기 token으로 받는 방식이 표준화. 본 프로젝트도 이 방식 권고.
- Secrets Manager 자동 rotation 도구 확장.

### 네트워크 / 데이터

- IPv6-only VPC(2023), AWS PrivateLink for SaaS, Cross-VPC peering 안정화.
- RDS Optimized Reads / Writes, Aurora Serverless v2(2022) — 기존 RDS PostgreSQL 기준 운영 부담은 거의 동일.
- VPC Endpoint 가격 인하 흐름은 거의 없음. 여전히 AZ당 ~$8/월 수준.

### 개발자 도구

- AWS Q Developer(구 CodeWhisperer) — IDE 통합 코드 어시스턴트.
- AWS Copilot CLI 안정화 — ECS 표준 배포 패턴 자동화.
- CDK v2, AWS SAM 안정화 — IaC 입문 부담 감소.

### 한국 시장 영향

- 서울 region(ap-northeast-2)에 신규 서비스가 더 빠르게 들어옴. 2021년에는 미국 region 우선이던 항목이 지금은 거의 동시 출시.
- AWS Outposts / Local Zones 확장 — 본 프로젝트와 직접 관계 없음.

### 짧게 요약

- 변한 것: 운영 편의(SnapStart, ECS Exec, Copilot, Q Developer), 새 orchestration 서비스(EventBridge Scheduler, Step Functions Distributed Map), 보안 표준(OIDC + IAM Roles Anywhere), App Runner 같은 PaaS 옵션 추가.
- 안 변한 것: VPC / Subnet / SG / Route Table / IGW / NAT 모델, IAM Role / Policy 구조, RDS의 운영 책임 경계, Lambda 15분 timeout, EKS control plane 부담, **컴퓨트 3분류(VM / 컨테이너 / 함수) 자체**.

## 3. 그래서 왜 "거의 안 변했다"는 인상이 강한가

이건 AWS의 게으름이 아니라 클라우드 플랫폼이 **성숙기에 들어섰다**는 신호다. 구조적인 이유를 짚으면:

### 1) 컴퓨트 3분류는 물리학에 가깝다

- "장기 실행 stateful instance"(VM/EC2), "패키징된 워크로드를 단발 또는 상시 실행"(컨테이너), "단발 stateless 함수"(FaaS). 이 셋은 워크로드 패턴 자체가 다르고, 새 카테고리가 끼어들 자리가 좁다.
- App Runner / Lightsail Containers / Lambda 컨테이너 이미지 같은 신상품도 결국 위 셋의 **편의 래퍼**다.

### 2) 호환성과 lock-in이 혁신을 묶는다

- AWS가 가장 잘하는 것은 **기존 API를 깨지 않는 것**. 5년 전에 짠 boto3 / CloudFormation이 지금도 거의 그대로 돈다. 이 호환성이 곧 사용자 신뢰의 근거이고, 동시에 큰 구조 변화를 어렵게 만든다.
- Kubernetes가 컨테이너 orchestration 시장을 사실상 표준화한 이후 AWS도 EKS에 베팅했지만, ECS를 끄지 않는다. 둘 다 유지하는 비용을 감수한다.

### 3) 운영 비용의 본질은 기술이 아니라 사람

- 2021년에도 1인 운영자에게 "EKS + Beanstalk + 5종 CI/CD"는 무거운 조합이었다.
- 2026년에도 여전히 무거운 조합이다.
- 운영 부담은 AWS가 줄여줄 수 있는 항목이지만 0이 되지 않는다. 결국 1인 운영자는 표준 1~2개로 수렴하게 된다.

### 4) 네트워크 / 보안의 기본 모델이 안 바뀐다

- VPC / Subnet / Route Table / SG는 사실상 IaaS의 산소다. 이걸 바꾸면 호환성이 깨진다.
- IAM도 마찬가지. 새 권한 표현(IAM Identity Center, ABAC)은 들어왔지만 IAM Policy JSON 자체는 거의 그대로.

### 5) "신상품"이 대부분 기존 서비스의 재포장

- App Runner = ECS Fargate + ALB + 자동 빌드의 재포장.
- AWS Copilot = ECS + CloudFormation + ECR 표준 패턴 자동화.
- Lambda 컨테이너 이미지 = ECR 이미지를 Lambda로 실행하는 어댑터.
- EKS Auto Mode = EKS + 일부 노드 운영 자동화.
- 즉, **카탈로그는 늘었지만 핵심 빌딩 블록은 같다**.

이 다섯 가지가 합쳐져서 "5년 동안 거의 안 변했다"는 체감을 만든다. 동시에 작은 변화들이 누적되어 **운영 모델은 의미 있게 단순해졌다**(예: GitHub Actions OIDC, EventBridge Scheduler, ECS Exec, Q Developer).

## 4. 그래서 왜 이번에는 ECS Fargate가 권고인가

2021년의 다양성 카탈로그가 2026년의 답이 아닌 이유는 **목적이 바뀌었기 때문**이다.

| 항목 | 2021 포트폴리오 | 2026 본 프로젝트 |
|------|----------------|-----------------|
| 1순위 목표 | AWS 서비스 카탈로그 폭 어필 | 8개 MS 운영 안정성 + 자동매매 안전성 |
| 운영 인원 | 학습 / 1인 | 1인, 이민용 포트폴리오 + 실서비스 동시 |
| 위험 | 학습 비용 | live broker 자동 재시도 사고 |
| 검증 우선순위 | 다양한 stack 운영 경험 | 짧은 cutover 창 안에서 안정화 |
| 평가 시점 | 단발 데모 | 장기 운영 (paper N영업일 + live) |

ECS Fargate가 8개 MS 중 6개 컴퓨트 워크로드의 1순위가 된 이유는 단순하다.

- 컨테이너 표준이라 vendor lock-in이 약하다(Beanstalk runtime / Lambda runtime / App Runner runtime 의존이 없다).
- worker node 운영 부담이 없다(EKS와의 가장 큰 차이).
- per-task 과금이라 idle 비용이 작다(EC2 24/7과의 가장 큰 차이).
- Step Functions / EventBridge Scheduler / ECS RunTask와 1급 통합이 된다 — 이게 본 프로젝트의 Daily Batch / intraday monitor와 정확히 맞는다.
- IAM Task Role + Secrets Manager + SSM Parameter Store + CloudWatch Logs가 **기본 세트**로 붙어, 본 spec의 06 / 09 / 10이 모두 같은 패턴으로 풀린다.
- ECS Exec(SSM 기반)으로 컨테이너 안에 들어가서 디버깅 가능 — 운영 부담을 더 낮춘다.
- 1인 운영이 가능한 마지노선. 동시에 다른 회사로 옮겨도 가장 흔한 표준이라 경력 이전성이 높다.

다른 후보가 1순위가 아닌 이유는 본 spec의 `ms-aws-service-decision-matrix.md` 4장 / 7장 / 8장 / 9장에 표로 정리되어 있으므로 여기서는 한 줄씩만:

- Lambda: Daily Batch 실측에서 crawler 약 9분 53초 / preprocessor 약 5분 54초가 15분 timeout 위험 영역. 게다가 live BUY/SELL 자동 재시도 금지 정책을 인프라 레벨에서 강제하기 어렵다.
- EKS: control plane 시간 단가(~$73/월) + addon 운영 부담이 1인 운영에 과함.
- Elastic Beanstalk: 컨테이너 표준에서 멀어지고, Daily Batch를 Step Functions로 옮기는 흐름과 자연스럽지 않다. 5년 전에 좋았지만 지금은 ECS Fargate에 자리를 내준 케이스.
- App Runner: VPC 자유도가 낮고, 외부 outbound IP 통제가 broker IP 등록 정책에 부합 안 함.
- EC2: marketconnector처럼 broker IP 등록 + 단일 토큰 같은 stateful 제약이 있을 때만 1순위. 그 외 워크로드는 24/7 idle 비용 손해.

## 5. 그래도 다양성은 살아 있다 — 이번 권고의 실체

ECS Fargate "단일 카드"로 보이지만 본 spec의 운영 안정성 1순위 결정만 따라가도 다음 카테고리가 자연스럽게 들어간다.

- 컴퓨트: **EC2** (marketconnector) + **ECS Fargate Service** (port-view) + **ECS Fargate Task** (crawler / preprocessor / decision / execution) + **AWS Batch** (research)
- Orchestration: **Step Functions** + **EventBridge Scheduler** + **ECS RunTask**
- 보조: **Lambda** (인프라 알람 fan-out 전용) + **SNS** + **CloudWatch Logs / Metrics / Alarms**
- 보안: **Secrets Manager** + **SSM Parameter Store** + **IAM Role / Policy** + **KMS**
- 데이터: **RDS for PostgreSQL** + **S3**
- 네트워크: **VPC** + Subnet + IGW + **VPC Endpoint** + Security Group + **Elastic IP** + (옵션) **ALB**
- 배포 / 레지스트리: **ECR** + **GitHub Actions OIDC**
- 운영 접근: **SSM Session Manager** (SSH 미사용)

이만하면 2021년의 카탈로그 대비 부족하지 않고, 운영 일관성은 훨씬 높다. EKS는 후속 optional track으로 분리해 "포트폴리오 어필이 더 필요하면 별도 트랙"으로 두면 된다(본 spec 7장).

## 6. 한 줄 결론

5년이 지나도 AWS의 **빌딩 블록**은 거의 그대로다. 그래서 변화가 적어 보인다. 그러나 **빌딩 블록을 어떻게 조립하느냐**의 표준이 바뀌었다. 2021년의 답은 "다양성 카탈로그", 2026년의 답은 "ECS Fargate를 중심에 두고 EC2 / Batch / Step Functions / EventBridge / Lambda(보조)를 합리적으로 섞기"다. 같은 AWS인데 답이 다른 이유는 AWS가 변해서가 아니라, 운영자의 **목적**이 학습용에서 운영용으로 바뀌었기 때문이다.

## 7. 본 노트 안전 제약

- 일회용 회고 노트. spec 산출물 아님. 다른 spec 문서가 본 노트를 참조 / 의존하지 않는다.
- 실제 AWS 리소스 생성 / IaC 작성 / 8개 MS 코드 수정 없음.
- 본 spec의 권고(`ms-aws-service-decision-matrix.md` 5장 / `operator-decisions.md` 9장 OD-MS-001 ~ OD-MS-010)는 본 노트에 의해 변경되지 않는다.
- 비용 수치는 모두 근사치이며 정확 값은 AWS Pricing Calculator 확인 필요.
- 모든 secret은 `[REDACTED]`로만 표기.
