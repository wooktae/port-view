# Requirements Document — 08-interest-crawler-and-preprocessor-ecs

## Introduction

본 spec 은 이미 운영 중인 `port-interest-crawler`, `port-interest-preprocessor` 두 Python MS 를 `aws-paper` 환경의 ECS / ECR 위에서 1차 실행 검증할 수 있도록 절차 / 검증 항목 / 실패 원인 후보 작성 기준을 확정한다.

핵심 한 줄: ECR repository 2개 생성 → Dockerfile 점검 → 로컬 이미지 빌드(preprocessor 우선) → ECR push → ECS Cluster·Role 준비 → preprocessor ECS Task 1회 실행 검증 → crawler 의 Selenium / Chrome / KRX / Naver / yfinance outbound 리스크 별도 관리.

선행 입력: [`../02-aws-network-and-rds`](../02-aws-network-and-rds)(VPC / Subnet / SG / VPC Endpoint / RDS), [`../06-secrets-and-iam`](../06-secrets-and-iam)(Secrets / SSM / Role 정책), [`../03-marketconnector-ec2`](../03-marketconnector-ec2)(EC2 → ECS 운영 패턴 매핑 §13). 1차 적용 환경 = `aws-paper`, region `ap-northeast-2`.

본 08 초기 문서 phase 산출물은 [`./requirements.md`](./requirements.md) / [`./design.md`](./design.md) / [`./tasks.md`](./tasks.md) 3개로 한정한다. runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md 는 운영자 실행 이후 별도 작성한다.

본 문서는 실제 secret value, account-id, RDS endpoint, KIS app key, KIS app secret, 계좌번호, IAM access key id, 실제 ARN, ECR repository URI 의 account-id 부, image digest 실값, ECS Cluster / Task / Service 의 실제 ARN, instance-id 를 평문으로 기록하지 않는다. 모두 `[REDACTED]` 또는 placeholder(`<account-id>`, `<region>`, `<ecr-repo-uri>`, `<image-tag>`, `<task-arn>`, `<rds-endpoint>`) 만 사용한다.

## Glossary

- **Crawler MS**: `port-interest-crawler`. Naver / yfinance / KRX 등 외부 원천에서 raw 데이터를 수집하는 Python MS. Selenium / Chrome 의존 가능성 있음.
- **Preprocessor MS**: `port-interest-preprocessor`. 수집된 raw 를 읽어 pre feature 테이블로 가공하는 Python MS. 외부 API / Selenium 의존 없음. RDS read / write 중심.
- **ECR Repository**: AWS Elastic Container Registry. 본 spec 시점에 두 MS 별로 별도 repository 생성. 이름: `portfolio-interest-crawler`, `portfolio-interest-preprocessor`.
- **ECS Cluster**: 본 spec 시점에 신규 생성하는 Fargate 기반 ECS Cluster. aws-paper 단일.
- **Task Execution Role**: ECR pull / CloudWatch Logs write / Secret 주입에 사용되는 ECS 공용 실행 역할.
- **Task Role**: 컨테이너 안 application 이 사용하는 권한(Secrets / SSM read, RDS 접속용 SG 정책). MS 별로 분리.
- **NAT-free 구조**: NAT Gateway 미사용. ECS Fargate Task 는 public subnet + assignPublicIp 로 외부 outbound 를 처리.
- **단발 실행 검증**: ECS Service / Step Functions 자동 기동 없이 운영자가 직접 1회 RunTask 만 수행하는 검증 단계.

## Requirements

### Requirement 1: 본 spec 의 범위와 범위 밖

**User Story:** As 운영자, I want 본 spec 의 범위와 범위 밖을 명시적으로 받기, so that 후속 spec 또는 후속 phase 가 작업 경계를 한눈에 파악할 수 있다.

#### Acceptance Criteria

1. WHEN design.md 가 작성되면, THE design.md SHALL 1차 적용 환경을 `aws-paper` / `ap-northeast-2` 로 명시하고, `aws-live` 와 10 spec 통합 cutover 는 본 spec 범위 밖임을 명시해야 한다.
2. WHEN design.md 가 작성되면, THE design.md SHALL 본 spec 범위 안으로 다음을 명시해야 한다: ECR repository 2개 생성 기준 / Dockerfile 점검 / 로컬 이미지 빌드 / ECR push / ECS Cluster·Role / preprocessor 단발 실행 검증 / crawler outbound·Selenium 리스크 별도 관리.
3. WHEN design.md 가 작성되면, THE design.md SHALL 본 spec 범위 밖으로 다음을 명시해야 한다: ECS Service / Step Functions 자동 기동, aws-live 적용(10 spec), CI/CD OIDC / GitHub Actions Role(07 spec), 두 MS 의 README / AGENTS.md / 소스 / `requirements.txt` 수정, crawler Selenium·Chrome 운영 안정화 100% 보장.
4. WHEN 본 08 초기 문서 phase 가 진행되면, THE 본 phase SHALL requirements.md / design.md / tasks.md 3개를 산출물로 한정하고, runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md 는 운영자 실행 이후 별도 작성해야 한다.

### Requirement 2: ECR Repository 생성 기준

**User Story:** As 운영자, I want 두 MS 용 ECR repository 의 생성 기준을 받기, so that image push / pull 경로가 일관되게 정해진다.

#### Acceptance Criteria

1. WHEN design.md 가 작성되면, THE design.md SHALL ECR repository 를 다음 2개로 명시해야 한다: `portfolio-interest-crawler`, `portfolio-interest-preprocessor`.
2. THE design.md SHALL 두 repository 의 region 을 `<region>` (`ap-northeast-2`) 로 명시하고, image scan on push 활성화를 권고해야 한다.
3. THE design.md SHALL repository URI 표기를 `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:<image-tag>`, `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-preprocessor:<image-tag>` placeholder 만 사용하고, 실제 account-id 를 평문 기록하지 않아야 한다.
4. WHERE 두 MS 가 공통 base image 를 공유할 가능성이 있는 경우, THE design.md SHALL 공통 base image repository 분리 여부를 본 spec 범위 밖(후속 검토) 으로 명시해야 한다.
5. THE design.md SHALL ECR repository 를 paper / live 환경별로 분리하지 않고 동일 image artifact 를 환경별 중복 repository 에 push 하지 않으며, paper / live 구분은 image tag / ECS Task Definition / Secrets Manager·SSM Parameter Store path / IAM Task Role / environment variables / RDS·broker 설정 6개 항목에서 처리한다는 점을 명시해야 한다.

### Requirement 3: Dockerfile 기준 점검

**User Story:** As 운영자, I want 두 MS 의 Dockerfile 기준 점검 항목을 받기, so that 빌드 / 실행 시점에 발견되는 결함을 사전에 식별할 수 있다.

#### Acceptance Criteria

1. THE design.md SHALL 두 MS repo 안에 Dockerfile 이 존재하는지 여부와 base image, requirements 설치 방식, entrypoint / CMD, 환경변수 주입 방식을 점검 항목으로 명시해야 한다.
2. WHERE Crawler MS 가 Selenium / Chrome 의존이 필요한 경우, THE design.md SHALL Dockerfile 안 Chrome / chromedriver 설치 단계 존재 여부를 점검 항목으로 명시해야 한다.
3. THE design.md SHALL Preprocessor MS Dockerfile 의 RDS 접속 환경변수(`INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`) 주입 호환성을 점검 항목으로 명시해야 한다.
4. IF Dockerfile 이 부재하거나 entrypoint 가 부정확한 경우, THEN THE design.md SHALL 본 spec 작업이 두 MS 소스 / Dockerfile 을 직접 수정하지 않고 후속 spec 또는 운영자 단계 책임으로 분리한다는 점을 명시해야 한다.

### Requirement 4: 로컬 이미지 빌드 (preprocessor 우선)

**User Story:** As 운영자, I want 로컬 이미지 빌드 절차와 우선 순위를 받기, so that ECS 실행 검증 전에 빌드 단계에서 결함이 차단된다.

#### Acceptance Criteria

1. THE design.md SHALL 로컬 이미지 빌드 순서를 (1) Preprocessor MS → (2) Crawler MS 로 명시해야 한다.
2. WHEN 로컬 빌드가 실패하는 경우, THE design.md SHALL 실패 원인 후보로 다음을 명시해야 한다: requirements.txt 호환성 / Python version mismatch / import path / system package 부족 / Crawler MS 의 Selenium·Chrome 의존성.
3. THE design.md SHALL Crawler MS 빌드 실패 시 Selenium / Chrome 의존성 결함은 Crawler 전용 리스크로 분리하고, Preprocessor 빌드를 차단하지 않는다는 점을 명시해야 한다.
4. THE design.md SHALL 빌드 결과(성공 / 실패 / image id 존재 여부) 만 산출물에 기록하고, 빌드 로그 stdout / stderr 본문은 본 spec 산출물에 평문 인용하지 않는다는 정책을 명시해야 한다.

### Requirement 5: ECR Push 기준

**User Story:** As 운영자, I want ECR push 기준과 실패 원인 후보를 받기, so that aws-paper 용 image tag 와 digest 가 일관되게 관리된다.

#### Acceptance Criteria

1. THE design.md SHALL ECR push 시 사용되는 tag 를 `paper-<yyyymmdd>` 또는 `paper-latest` 형태의 placeholder `<image-tag>` 로 명시하고, 실제 tag 결정은 운영자 직접 단계로 분리해야 한다.
2. THE design.md SHALL push 순서를 (1) Preprocessor MS push → (2) Crawler MS push (또는 실패 원인 후보 기록) 로 명시해야 한다.
3. WHEN push 가 성공하는 경우, THE design.md SHALL image digest(`sha256:...`) 확인을 점검 항목으로 명시하되, 실제 digest 값은 본 문서에 평문 기록하지 않고 placeholder 만 사용해야 한다.
4. IF push 가 실패하는 경우, THEN THE design.md SHALL 실패 원인 후보로 다음을 명시해야 한다: ECR login token 만료 / Task Execution Role 미부여 / repository 미생성 / Docker daemon 미기동 / region 불일치.
5. THE design.md SHALL image tag 전략과 push target URI 표기를 다음과 같이 명시해야 한다: aws-paper 1차 검증 tag = `paper-<yyyymmdd>` / `paper-latest`, 향후 aws-live tag = `live-<yyyymmdd>` / `live-latest`, 향후 CI/CD 성숙 단계 tag = `git-<sha>` 추가 가능, push target URI = `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:<image-tag>` 와 `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-preprocessor:<image-tag>`.

### Requirement 6: ECS Cluster / Role / Log Group 준비

**User Story:** As 운영자, I want ECS Cluster 와 Role / Log Group 준비 기준을 받기, so that Task 실행 시점에 권한 / log 채널 누락이 발생하지 않는다.

#### Acceptance Criteria

1. THE design.md SHALL aws-paper 용 ECS Cluster 1개 생성 기준(이름 `<cluster-name>`, Fargate 기반) 을 명시해야 한다.
2. THE design.md SHALL Task Execution Role 1개를 명시하고 다음 권한을 포함해야 한다: ECR pull, CloudWatch Logs write, Secrets Manager / SSM read(주입 경로).
3. THE design.md SHALL Crawler / Preprocessor 별로 분리된 Task Role 2개를 명시하고, 각 Role 이 service prefix(`/portfolio/paper/crawler/*`, `/portfolio/paper/preprocessor/*`) 의 Secrets / SSM read 권한과 RDS 접속 SG 통과 권한만 갖도록 명시해야 한다.
4. THE design.md SHALL CloudWatch Log Group 2개를 명시해야 한다: `/portfolio/paper/crawler`, `/portfolio/paper/preprocessor`. log group 사전 생성은 운영자 직접 단계로 분리.
5. WHERE Resource / Action wildcard(`*`) 가 정책에 등장하는 경우, THE design.md SHALL 03 spec §13 의 wildcard 금지 정책(Resource wildcard 금지 / Action wildcard 금지) 을 그대로 따른다는 점을 명시해야 한다.
6. THE design.md SHALL Task Definition `secrets` 필드 주입용 Secrets / SSM read 권한은 Task Execution Role, application runtime 의 AWS SDK 직접 조회 권한은 Task Role 로 구분하고, 본 1차 검증은 Task Definition `secrets` 주입을 우선 사용한다는 점을 명시해야 한다.

### Requirement 7: Preprocessor 단일 Task 실행 검증

**User Story:** As 운영자, I want Preprocessor MS 의 ECS Task 단발 실행 검증 기준을 받기, so that 본 spec 시점의 1차 검증 대상이 명확해진다.

#### Acceptance Criteria

1. THE design.md SHALL Preprocessor Task 의 networkMode 를 `awsvpc` 로 명시하고, public subnet + `assignPublicIp = ENABLED` 로 NAT-free outbound 를 처리한다는 점을 명시해야 한다.
2. WHEN Preprocessor Task 가 1회 RunTask 로 실행되면, THE 검증 SHALL `marketconnector_app` 가 아닌 `preprocessor_app` 기준 RDS 접속 성공 여부, CloudWatch Logs 출력 여부, Task exit code(0) 를 점검 항목으로 포함해야 한다.
3. THE design.md SHALL 본 spec 시점의 Preprocessor 실행은 단발(`aws ecs run-task` 1회) 만 수행하고, ECS Service 상시 가동 / EventBridge Scheduler 정기 기동은 본 spec 범위 밖임을 명시해야 한다.
4. IF Preprocessor Task 가 실패하는 경우, THEN THE design.md SHALL 실패 원인 후보로 다음을 명시해야 한다: 환경변수 주입 누락 / Secret read 권한 누락 / RDS SG inbound 미허용 / VPC Endpoint 누락 / image entrypoint 결함.

### Requirement 8: Crawler Selenium / Chrome / 외부 outbound 리스크 별도 관리

**User Story:** As 운영자, I want Crawler MS 의 Selenium / Chrome / KRX / Naver / yfinance outbound 리스크를 별도 섹션으로 받기, so that Preprocessor 검증과 Crawler 안정화 책임이 분리된다.

#### Acceptance Criteria

1. THE design.md SHALL Crawler MS 의 Selenium / Chrome 필요 여부를 1차 검토 항목으로 명시해야 한다.
2. THE design.md SHALL KRX, Naver, yfinance outbound 접근 경로를 NAT-free 구조에서 public subnet + `assignPublicIp` 로 처리한다는 점을 명시하고, KRX 로그인 / rate limit / Selenium 안정성 미달 가능성을 리스크 후보로 분리해야 한다.
3. THE design.md SHALL Crawler 운영 안정화 100% 보장은 본 spec 범위 밖이며, 본 spec 시점에는 1차 검토(Dockerfile / 빌드 가능 여부 / outbound 도달 여부) 까지만 수행한다는 점을 명시해야 한다.
4. WHERE Crawler 1차 검토에서 Selenium / Chrome 의존성 결함이 발견되는 경우, THE design.md SHALL 후속 spec 또는 후속 phase 책임으로 분리하고, Preprocessor 검증 흐름을 차단하지 않는다는 점을 명시해야 한다.
5. WHERE KRX GUI 의존 수집(예: KRX program / KRX shortsell)이 ECS Fargate Task 의 GUI / Chrome download / OTP 세션 흐름과 호환되지 않는 경우, THE design.md SHALL 해당 KRX GUI 의존 crawler 가 ECS Fargate Task 대신 Windows EC2 worker 위에서 실행될 수 있음을 명시하고, non-GUI crawler 와 KRX GUI 의존 crawler 의 runtime 분리(hybrid execution model) 를 명시해야 한다.
6. WHEN KRX GUI 수집 자동화가 운영 단계로 진입하는 경우, THE design.md SHALL 다음 4개 조건을 명시해야 한다(2026-06-16 결과 정합 / OD-MS-022 정합): (a) KRX GUI 수집은 Windows interactive session 이 필수다 / (b) SSM 은 직접 Python 실행이 아니라 Scheduled Task trigger 역할로만 사용한다 / (c) Autologon 은 paper 전용 운영 예외(보안 예외)다 / (d) KRX program · shortsell 완료 기준은 DB max date 와 row count 증가로 검증한다.
7. WHEN non-GUI crawler 의 ECS Fargate 운영 경로가 분리되는 경우, THE design.md SHALL 다음 4개 조건을 명시해야 한다(2026-06-16 결과 정합 / OD-MS-011 / OD-MS-022 정합): (a) ECS / Fargate Task 의 entrypoint / Python 모듈 import 에서 KRX GUI 의존 모듈을 제외한다 / (b) non-GUI step 별 SUCCESS 로그를 CloudWatch Logs 로 확인한다 / (c) raw table 별 최신성을 SQL 로 검증한다 / (d) 회복된 raw 입력으로 Preprocessor 가 재실행 가능한지 판단한다.

### Requirement 9: NAT-free 정책 강제

**User Story:** As 운영자, I want NAT-free 정책을 본 spec 시점에 다시 강제하기, so that NAT Gateway 비용이 발생하지 않는다.

#### Acceptance Criteria

1. THE design.md SHALL NAT Gateway 사용을 금지하고, 본 spec 의 모든 ECS Fargate Task 가 public subnet + `assignPublicIp = ENABLED` 로 outbound 를 처리한다는 점을 명시해야 한다.
2. THE design.md SHALL 외부 API(Naver / yfinance / KRX / holiday) outbound 도 동일 경로로 처리한다는 점을 명시해야 한다.
3. IF 본 spec 운영 도중 NAT Gateway 가 발견되는 경우, THEN THE design.md SHALL 본 spec 임의 결정 대신 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-NET-001 / OD-NET-002 를 재확인 후 운영자 결정으로만 처리한다는 점을 명시해야 한다.

### Requirement 10: 안전 제약 (작업 분담 / 민감정보 / 외부 호출)

**User Story:** As 운영자, I want 본 spec 작업의 안전 제약을 명시적으로 받기, so that 코드 / 운영 데이터 / AWS 리소스 / 외부 호출이 본 spec 작업으로 인해 변경되지 않는다.

#### Acceptance Criteria

1. WHEN 본 spec 의 모든 phase 가 진행되는 동안, THE 작업 SHALL 실제 AWS 리소스(ECR repository, ECS Cluster / Task Definition / Service, IAM Role / Policy, CloudWatch Log Group, Secrets / SSM Parameter, RDS) 의 생성 / 변경 / 삭제를 직접 수행하지 않고 운영자 직접 작업으로만 처리해야 한다.
2. WHEN 본 spec 의 모든 phase 가 진행되는 동안, THE 작업 SHALL 8개 MS(`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) 의 README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 파일을 수정하지 않아야 한다.
3. WHEN 본 spec 의 모든 phase 가 진행되는 동안, THE 작업 SHALL 외부 호출(KRX / Naver / yfinance / Selenium / Chrome / KIS API) 0건, 크롤링 0건, 주문 / 매수 / 매도 / 취소 / 정정 0건, RDS DDL/DML 0건을 유지해야 한다.
4. WHEN 본 spec 산출물이 secret / 식별자를 다루는 경우, THE 산출물 SHALL 실제 secret value / password / KIS app key / app secret / 계좌번호 / token / RDS endpoint hostname / account-id / 실제 ARN / image digest / IAM access key id / instance-id 를 평문으로 적지 않고 모두 `[REDACTED]` 또는 placeholder(`<account-id>`, `<region>`, `<ecr-repo-uri>`, `<image-tag>`, `<task-arn>`, `<rds-endpoint>`) 만 사용해야 한다.
5. WHEN secret 조회를 다루는 경우, THE 산출물 SHALL `secretsmanager:GetSecretValue` 호출은 운영자만 수행하고 Kiro 자동 검증은 `secretsmanager:DescribeSecret` metadata 만 사용한다는 점을 명시해야 한다(06 spec §11 정합).

### Requirement 11: 본 spec 초기 문서 phase 산출물 한정

**User Story:** As 운영자, I want 본 spec 초기 문서 phase 산출물 범위를 한정 받기, so that 후속 phase 와 운영자 실행 단계 간 책임 경계가 명확해진다.

#### Acceptance Criteria

1. WHEN 본 08 초기 문서 phase 가 진행되는 동안, THE 작업 SHALL 산출물을 requirements.md / design.md / tasks.md 3개로 한정해야 한다.
2. WHEN 본 08 초기 문서 phase 가 진행되는 동안, THE 작업 SHALL runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md(또는 `docs/worklog/YYYY-MM-DD.md`) 를 본 phase 에서 작성하지 않고 운영자 실행 이후 별도 작성으로 분리해야 한다.
3. WHEN 각 phase 호출이 진행되는 동안, THE 작업 SHALL 해당 호출의 단일 phase 문서(requirements 호출 → requirements.md, design 호출 → design.md, tasks 호출 → tasks.md) 만 갱신하고 나머지 phase 문서는 후속 호출 책임으로 분리해야 한다.
4. WHEN 본 phase 가 진행되는 동안, THE 작업 SHALL [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/risk-register.md`](../_common/risk-register.md), [`../_common/followups-overview.md`](../_common/followups-overview.md) 의 실제 갱신을 수행하지 않고, 갱신 후보만 후속 phase(design / tasks) 에서 식별하도록 분리해야 한다.


## 2026-06-16 보강 — KRX GUI 수집 / non-GUI crawler 요구사항 강화

본 절은 2026-06-16 운영자 검증 결과(`./operation-notes.md` 2026-06-16 §1 ~ §6)를 입력으로 R8(Crawler Selenium / Chrome / 외부 outbound 리스크 별도 관리)과 R6 / R7(ECS Cluster / Role / Log Group / Preprocessor 단발 실행)에 대한 추가 acceptance criteria 를 보강 메모로 명시한다. 기존 R1 ~ R11 결정값(SHALL / SHALL NOT) 은 변경하지 않으며, 본 절은 hybrid execution model 1차 자동화 진입점과 non-GUI 운영 경로 신규 생성에 따른 검증 기준만 보강한다. 결정 락은 OD-MS-022(신규) + OD-MS-011 / OD-MS-015 / OD-MS-020 1차 실증 메모 보강 정합.

### Requirement 12: KRX GUI 수집 운영 방식 (R8 보강)

**User Story:** As 운영자, I want KRX GUI 의존 수집의 운영 방식 기준을 받기, so that SSM direct 실행 / Headless 수집 같은 부적합 방식이 운영에 잘못 들어가지 않는다.

#### Acceptance Criteria

1. WHEN KRX GUI 의존 수집(KRX program / KRX shortsell)이 운영 방식으로 진입하는 경우, THE design.md SHALL Windows EC2 worker 위의 **Administrator console interactive session** 이 필수 조건임을 명시해야 한다.
2. THE design.md SHALL SSM RunCommand 가 KRX GUI 경로에서 사용되는 방식을 **`schtasks /Run` 트리거 역할** 로만 제한하고, SSM RunCommand 가 wrapper / Python 을 SYSTEM Session 0 / 비대화형 세션에서 직접 실행하는 방식은 **운영 방식에서 제외** 한다는 점을 명시해야 한다(2026-06-13 §13.2 / 2026-06-16 §15.3 정합).
3. WHERE Administrator console interactive session 이 부재한 경우, THE 운영 방식 SHALL Autologon bootstrap 으로 EC2 부팅 후 자동 생성된 Administrator console session 을 사용한다는 점을 명시하고, Autologon 사용은 paper 전용 Windows worker 한정 보안 예외(R-SEC-009 정합)임을 명시해야 한다.
4. WHEN KRX GUI 수집 결과의 완료 기준을 다루는 경우, THE 검증 SHALL DB max date 와 row count 증가를 함께 점검(예: `interest_program_raw` 의 max date = 직전 거래일 / row count 증가량 = 영업일 수, `interest_shortsell_raw` 의 max date = 직전 거래일 / row count 증가량 = 영업일 수 × 종목 수) 하는 SQL 점검을 통과해야 한다.
5. THE design.md SHALL Headless / 비대화형 KRX 수집은 로컬 검증상 제외 / 운영 방식에서 제외(2026-06-16 §15.3 정합)임을 명시하고, "장기 후보" 표현은 본 일자 보정으로 더 이상 사용하지 않는다는 점을 명시해야 한다.

### Requirement 13: non-GUI Interest Crawler 운영 방식 (R8 보강)

**User Story:** As 운영자, I want non-GUI Interest Crawler 의 ECS / Fargate 운영 방식 기준을 받기, so that smoke 검증과 daily 운영용 Task Definition 이 분리되고 raw 최신성 검증이 빠지지 않는다.

#### Acceptance Criteria

1. WHEN non-GUI crawler 가 ECS / Fargate 위에서 daily 운영으로 실행되는 경우, THE Task Definition SHALL KRX GUI 의존 import(`interest_krx_login_new` / `interest_program` / `interest_shortsell`) 를 **제외** 해야 한다(2026-06-16 §15.2 정합).
2. THE Task Definition SHALL non-GUI step(`interest_news` / `interest_agency` / `interest_foreignindex` / `interest_commodity` / `interest_macroeconomic` / `interest_price` / `interest_investorflow` / `interest_marketbreadth`)별로 **CloudWatch Logs 안 SUCCESS** 메시지 또는 동등한 step 종료 신호를 확인할 수 있어야 한다.
3. THE 검증 SHALL ECS / Fargate smoke 전용 revision(예: revision 6 / log stream prefix `ecs-selenium-chrome-smoke`) 과 daily 운영용 revision(예: revision 7 / log stream prefix `ecs-crawler-nongui-daily`) 을 **분리 명시** 하고, smoke 통과만으로 daily 운영 완료를 단정하지 않아야 한다(R-DATA-009 mitigation 정합).
4. THE 검증 SHALL 각 raw table 의 max date 가 직전 거래일과 일치하는지(또는 거래일 N±1 범위 안인지) SQL 로 확인하고, row count 증가량이 영업일 수 × 종목 수 / 영업일 수 × 지수 수 / 영업일 수 × 카테고리 수 와 부합하는지 함께 점검해야 한다.
5. WHEN raw 최신성 검증을 통과한 경우, THE 검증 SHALL Preprocessor ECS RunTask 의 입력 가능 여부(직전 거래일까지 적재 + max date 일치 + 신규 row 생성 확인)를 함께 판단해야 한다(R-DATA-010 mitigation 정합).

### Requirement 14: hybrid execution model 자동 로그인 기반 운영 방식 보안 (R10 보강)

**User Story:** As 운영자, I want hybrid execution model 에서 paper 전용 Windows worker 의 자동 로그인 보안 예외 기준을 받기, so that 운영자 노트 / spec 산출물 / 채팅 / CloudWatch Logs 어디에도 자동 로그인 자격 증명이 평문 기록되지 않는다.

#### Acceptance Criteria

1. WHEN Autologon 자격 증명을 다루는 경우, THE 산출물 SHALL DefaultUserName / DefaultPassword / Administrator password 를 평문으로 적지 않고 모두 `[REDACTED]` 또는 placeholder 만 사용해야 한다.
2. THE design.md SHALL Autologon 사용을 **paper 전용 Windows worker 한정 보안 예외** 로 분류하고, 일반 운영 환경 standard 가 아니라는 점을 명시해야 한다(R-SEC-009 정합).
3. THE design.md SHALL Autologon 적용 대상 EC2 의 **RDP inbound 제한**(0.0.0.0/0 금지 / 운영자 단일 IP 또는 SSM Session Manager 우선 / OD-NET-009 정합) 을 함께 명시해야 한다.
4. THE 검증 SHALL `query user` 결과의 Administrator console session Active 여부, SSM managed instance Online 여부, Scheduled Task 의 Last Result 코드를 함께 점검해야 한다.
5. WHEN EC2 worker 작업이 종료되는 경우, THE 후속 작업 SHALL EC2 stop 절차(idle 비용 절감) / Chrome process 정리 옵션 / 추후 전용 local user 검토 후보를 함께 후속 인계로 명시해야 한다(R-AUTO-017 / R-SEC-009 mitigation 정합).


## Addendum (2026-06-16) — KRX GUI 자동 로그인 운영 방식 / non-GUI 운영 경로 검증 보강

본 부록은 2026-06-16 운영자가 직접 수행한 (a) `interest_crawler_daily_nongui.py` 신규 + ECS Task Definition revision 7 등록 + RunTask 성공, (b) Windows EC2 worker Autologon bootstrap + Administrator console session Active 확인 + SSM RunCommand → `schtasks /Run` → Scheduled Task 흐름 재검증, (c) raw 최신성 회복 결과를 반영해 R8(Crawler Selenium / Chrome / 외부 outbound 리스크 별도 관리) 와 R9(NAT-free 정책 강제) 의 acceptance criteria 를 보강 메모로 추가한다. R1 ~ R7 / R10 / R11 본문은 변경하지 않는다. 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §1 ~ §5 / 본 일자 결정 락은 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-MS-022 + OD-MS-011 / OD-MS-015 / OD-MS-020 1차 실증 메모 참조.

### Addendum-A. KRX GUI 수집 요구사항 보강 (R8 보강)

본 부록은 R8(Crawler Selenium / Chrome / 외부 outbound 리스크 별도 관리) 의 KRX GUI 의존 수집 항목을 본 일자 결과 기준으로 보강한다.

#### Acceptance Criteria 보강

1. THE design.md SHALL KRX GUI 수집(예: KRX program / KRX shortsell) 이 Windows interactive session 위에서만 실행되도록 명시해야 한다(SYSTEM Session 0 / 비대화형 GUI 직접 실행은 운영 방식에서 제외).
2. THE design.md SHALL SSM RunCommand 의 역할을 wrapper / Python 직접 실행이 아니라 Scheduled Task trigger(`schtasks /Run /TN "Portfolio-KRX-Worker-Daily"`) 로 한정한다는 점을 명시해야 한다(OD-MS-022 정합 / 2026-06-13 §13.2 + 2026-06-16 §15.5 정합).
3. THE design.md SHALL Windows Autologon 사용을 paper 전용 Windows worker 보안 예외로 명시해야 한다(R-SEC-009 / OD-MS-022 정합). aws-live 적용 여부는 본 spec 범위 밖이며 후속 spec(10) 책임으로 분리.
4. WHEN KRX program / KRX shortsell 의 완료 여부를 판단하는 경우, THE 검증 SHALL 다음 두 항목을 모두 만족해야 한다: (a) 해당 raw 테이블의 `MAX(trade_date)` 가 직전 거래일과 일치, (b) row count 가 정상 범위로 증가(예: 2026-06-15 적재 시 `interest_program_raw` 547 → 548 / `interest_shortsell_raw` 190,554 → 190,903 같은 패턴).
5. WHERE Headless / 비대화형 KRX 수집이 검토되는 경우, THE design.md SHALL 본 spec 시점의 결정값(로컬 검증상 운영 방식에서 제외)을 그대로 따르고, 임의로 운영 방식에 포함하지 않아야 한다(OD-MS-022 정합).

### Addendum-B. non-GUI crawler 요구사항 보강 (R8 보강)

본 부록은 R8 의 non-GUI crawler 항목을 본 일자 결과 기준으로 보강한다.

#### Acceptance Criteria 보강

1. THE design.md SHALL non-GUI Interest Crawler 의 ECS / Fargate 운영 entrypoint 가 KRX GUI 계열(`interest_krx_login_new` / `interest_program` / `interest_shortsell`) 을 import 하지 않도록 명시해야 한다(`interest_crawler_daily_nongui.py` 정합).
2. THE design.md SHALL non-GUI step(`interest_news` / `interest_agency` / `interest_foreignindex` / `interest_commodity` / `interest_macroeconomic` / `interest_price` / `interest_investorflow` / `interest_marketbreadth`) 별 SUCCESS 로그를 CloudWatch Logs 에서 확인하는 점검 항목을 명시해야 한다(2026-06-16 §2 정합).
3. THE design.md SHALL non-GUI raw 테이블별 최신성 검증 점검 항목을 명시해야 한다(`interest_*_raw` 의 `MAX(trade_date)` 가 직전 거래일과 일치 / row count 정상 범위 증가).
4. WHEN non-GUI crawler RunTask 가 성공한 경우, THE 검증 SHALL Preprocessor 입력 데이터로 사용 가능한 수준 도달 여부를 판단해야 한다(R-DATA-009 / R-DATA-010 mitigation 정합). 단, 일부 지수 NULL Data(예: `interest_foreignindex_raw` HANGSENG / NIKKEI225 / SHANGHAI) 가 발견되더라도 전체 적재 실패가 아니라 non-blocker 후보로 분리한다.

### Addendum-C. NAT-free 정책 보강 (R9 보강)

본 부록은 R9(NAT-free 정책 강제) 의 적용 범위를 본 일자 결과 기준으로 명시한다.

#### Acceptance Criteria 보강

1. THE design.md SHALL non-GUI crawler ECS Task Definition revision 7 이 public subnet + `assignPublicIp = ENABLED` 로 NAT-free outbound 를 처리한다는 점을 명시해야 한다(2026-06-16 §2 RunTask 결과 정합 / NAT Gateway 0건 유지).
2. THE design.md SHALL Windows EC2 worker(KRX GUI crawler) 의 outbound 도 NAT-free 정책을 따른다는 점을 명시해야 한다(public subnet + EIP 또는 동등 방식 / OD-NET-001 / OD-NET-002 정합).
3. IF NAT Gateway 가 본 spec 운영 도중 발견되는 경우, THEN THE design.md SHALL 본 spec 임의 결정 대신 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-NET-001 / OD-NET-002 를 재확인 후 운영자 결정으로만 처리한다는 R9.3 정책을 그대로 유지한다.
