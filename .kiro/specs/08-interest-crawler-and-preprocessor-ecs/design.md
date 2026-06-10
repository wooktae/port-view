# Design Document — 08-interest-crawler-and-preprocessor-ecs

## Introduction

본 spec 의 핵심을 한 줄로 요약한다. **`port-interest-crawler` / `port-interest-preprocessor` 두 Python MS 를 `aws-paper` 환경의 ECR / ECS 위에서 1차 실행 검증할 수 있도록 ECR repository / Dockerfile 점검 / 로컬 빌드 / ECR push / ECS Cluster·Role·Log Group / Preprocessor 단발 실행 / Crawler outbound 리스크 분리 절차 기준을 확정한다.**

- 1차 적용 환경: `aws-paper`, region `ap-northeast-2`. `aws-live` 와 10 spec 통합 cutover 는 본 spec 범위 밖.
- 입력: [`./requirements.md`](./requirements.md) R1 ~ R11, [`../02-aws-network-and-rds`](../02-aws-network-and-rds)(VPC / Subnet / SG / VPC Endpoint / RDS), [`../06-secrets-and-iam`](../06-secrets-and-iam)(Secrets / SSM / Role 정책), [`../03-marketconnector-ec2`](../03-marketconnector-ec2)(EC2 → ECS 운영 패턴 §13).
- 본 08 초기 문서 phase 산출물은 requirements.md / design.md / tasks.md 3개로 한정한다. runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md 는 운영자 실행 이후 별도 작성한다(R11 정합).

본 문서는 실제 secret value, account-id, RDS endpoint hostname, image digest, 실제 ARN, instance-id, IAM access key id 를 평문 기록하지 않는다. 모두 `[REDACTED]` 또는 placeholder(`<account-id>`, `<region>`, `<ecr-repo-uri>`, `<image-tag>`, `<task-arn>`, `<rds-endpoint>`, `<cluster-name>`) 만 사용한다(R10.4 정합).

## 1. 범위 / 범위 밖 (R1)

### 1.1 범위 안 / 범위 밖

| 구분 | 항목 |
|------|------|
| 범위 안 | ECR repository 2개 생성 기준 / Dockerfile 점검 / 로컬 이미지 빌드(preprocessor 우선) / ECR push / ECS Cluster·Task Role·Task Execution Role·Log Group 준비 / Preprocessor ECS Task 단발 실행 검증 / Crawler outbound·Selenium 리스크 별도 관리 |
| 범위 밖 | ECS Service 상시 가동 / EventBridge Scheduler / Step Functions 자동 기동 / aws-live 적용(10 spec) / GitHub Actions OIDC·CI/CD Role(07 spec) / 8개 MS README·AGENTS.md·소스·`requirements.txt`·Dockerfile 수정 / Crawler Selenium·Chrome 운영 안정화 100% 보장 |

### 1.2 본 phase 산출물 한정 (R11 근거)

본 08 초기 문서 phase 산출물은 requirements.md / design.md / tasks.md 3개로 한정한다. runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md 는 운영자 실행 이후 별도 작성한다. [`../_common/operator-decisions.md`](../_common/operator-decisions.md) / [`../_common/risk-register.md`](../_common/risk-register.md) / [`../_common/followups-overview.md`](../_common/followups-overview.md) 의 실제 갱신은 후속 phase 책임이며, 본 phase 에서는 후보 식별만 수행한다.

## 2. ECR Repository (R2)

### 2.1 Repository 매트릭스

| Repository 이름 | region | URI placeholder | 대상 MS | image scan on push |
|----------------|--------|----------------|--------|--------------------|
| `portfolio-interest-crawler` | `<region>` (`ap-northeast-2`) | `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:<image-tag>` | `port-interest-crawler` (Crawler MS) | enabled (권고) |
| `portfolio-interest-preprocessor` | `<region>` (`ap-northeast-2`) | `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-preprocessor:<image-tag>` | `port-interest-preprocessor` (Preprocessor MS) | enabled (권고) |

실제 account-id 는 본 design 어디에도 평문 기록하지 않는다(R10.4 정합). 실제 repository 생성은 운영자 직접 작업이며 본 spec 범위 밖.

### 2.2 공통 base image 분리

두 MS 가 공통 Python base image / Selenium base image 를 공유할 가능성이 있으나, 공통 base image repository 분리 여부는 본 spec 범위 밖이며 후속 검토로 분리한다(R2.4 근거).

### 2.3 환경 미분리 정책 (R2.5 근거)

ECR repository 는 paper / live 환경별로 분리하지 않는다. 동일 image artifact 를 환경별 중복 repository 에 push 하지 않으며, paper / live 구분은 다음 6개 항목에서 처리한다: (1) image tag, (2) ECS Task Definition, (3) Secrets Manager / SSM Parameter Store path, (4) IAM Task Role, (5) environment variables, (6) RDS / broker 설정.

## 3. Dockerfile 점검 (R3)

본 spec 작업은 두 MS 의 Dockerfile / 소스 / `requirements.txt` 를 직접 수정하지 않는다. Dockerfile 부재 또는 entrypoint 결함이 발견되는 경우, 후속 spec 또는 운영자 단계 책임으로 분리한다(R3.4 정합).

### 3.1 점검 항목 — Preprocessor MS

| 점검 항목 | 기대 값 / 점검 방식 |
|----------|--------------------|
| Dockerfile 존재 | repo 루트에 `Dockerfile` 1개 |
| base image | Python 3.x slim 계열 권고. 실제 tag 결정은 운영자 |
| requirements 설치 | `pip install -r requirements.txt` 또는 등가 방식 |
| entrypoint / CMD | preprocessor 실행 entrypoint 명시 (예: `python pre_daily.py`) |
| RDS env 호환 | `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD` 환경변수 주입 호환 |
| Selenium / Chrome 의존 | **불필요** (외부 API / Selenium 미사용) |

### 3.2 점검 항목 — Crawler MS

| 점검 항목 | 기대 값 / 점검 방식 |
|----------|--------------------|
| Dockerfile 존재 | repo 루트에 `Dockerfile` 1개 |
| base image | Python 3.x slim 또는 selenium-capable 계열 |
| requirements 설치 | `pip install -r requirements.txt` 또는 등가 방식 |
| entrypoint / CMD | crawler 실행 entrypoint 명시 |
| Chrome / chromedriver 설치 | Selenium 의존 시 Dockerfile 안 설치 단계 존재 여부 1차 확인 |
| RDS env 호환 | 동일 5개 환경변수 키 주입 호환 (raw 적재용) |

## 4. 로컬 이미지 빌드 (R4)

### 4.1 빌드 순서 / 정책

| 순번 | 대상 | 정책 |
|------|------|------|
| 1 | Preprocessor MS | 우선 빌드. 빌드 성공 시 ECR push 단계로 진행 |
| 2 | Crawler MS | Preprocessor 빌드 성공 후 진행. **실패 시 Preprocessor 흐름 차단 금지**(R4.3 정합) |

빌드 결과는 성공 / 실패 / image id 존재 여부만 후속 산출물에 기록한다. 빌드 로그 stdout / stderr 본문은 평문 인용하지 않는다(R4.4 정합).

### 4.2 빌드 실패 원인 후보

| # | 원인 후보 | 1차 점검 위치 |
|---|----------|--------------|
| 1 | `requirements.txt` 호환성 (패키지 충돌 / 빌드 휠 부재) | `pip install` 단계 로그 |
| 2 | Python version mismatch (3.9 / 3.10 / 3.11 등) | base image tag |
| 3 | import path / module 부재 | entrypoint 실행 단계 |
| 4 | system package 부족 (`build-essential`, `libpq-dev` 등) | Dockerfile 의 `apt-get install` 단계 |
| 5 | Crawler MS 의 Selenium / Chrome / chromedriver 설치 실패 | Crawler Dockerfile 한정. Preprocessor 무관 |

## 5. ECR Push (R5)

### 5.1 Tag / 순서 정책

| 항목 | 정책 |
|------|------|
| image tag 형식 | `paper-<yyyymmdd>` 또는 `paper-latest` 형태의 placeholder `<image-tag>`. 실제 tag 결정은 운영자 직접 단계 |
| push 순서 | (1) Preprocessor → (2) Crawler |
| digest 확인 | push 성공 시 `sha256:...` 확인. 실값은 본 design / 후속 산출물에 평문 기록 금지. placeholder `<image-digest>` 만 사용 |

### 5.2 image tag 전략 (R5.5 근거)

| 단계 | tag 형식 | 비고 |
|------|---------|------|
| aws-paper 1차 검증 | `paper-<yyyymmdd>` / `paper-latest` | 본 spec 시점 사용 |
| aws-live 적용 | `live-<yyyymmdd>` / `live-latest` | 10 spec cutover 이후 |
| CI/CD 성숙 단계 | `git-<sha>` 추가 가능 | 후속 spec(07 OIDC) 이후 |

push target URI 는 `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:<image-tag>`, `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-preprocessor:<image-tag>` placeholder 만 사용한다.

### 5.3 Push 실패 원인 후보

| # | 원인 후보 | 1차 점검 위치 |
|---|----------|--------------|
| 1 | ECR login token 만료 (`aws ecr get-login-password` 재발급 필요) | `docker login` 응답 |
| 2 | Task Execution Role / 운영자 IAM 권한 누락 (`ecr:PutImage` 등) | IAM 정책 |
| 3 | repository 미생성 | ECR Console / `aws ecr describe-repositories` |
| 4 | Docker daemon 미기동 / 로컬 환경 결함 | `docker info` |
| 5 | region 불일치 (`ap-northeast-2` 외) | login URI / push 대상 URI |

## 6. ECS Cluster / Role / Log Group 준비 (R6)

### 6.1 인프라 산출물 매트릭스

| 종류 | 개수 | 이름 placeholder | 정책 |
|------|------|-----------------|------|
| ECS Cluster (Fargate) | 1 | `<cluster-name>` | aws-paper 단일. 본 spec 시점 신규 생성 기준 |
| Task Execution Role | 1 | `portfolio-paper-ecs-task-execution-role` | ECR pull / CloudWatch Logs write / Secrets·SSM read 권한 |
| Task Role — Crawler | 1 | `portfolio-paper-crawler-task-role` | `/portfolio/paper/crawler/*` Secrets·SSM read 한정 |
| Task Role — Preprocessor | 1 | `portfolio-paper-preprocessor-task-role` | `/portfolio/paper/preprocessor/*` Secrets·SSM read 한정 |
| CloudWatch Log Group | 2 | `/portfolio/paper/crawler`, `/portfolio/paper/preprocessor` | 운영자 사전 생성. application 임의 log group 생성 금지 |

실제 Role / Cluster / Log Group 생성은 운영자 직접 작업이며 본 spec 범위 밖(R10.1 정합).

### 6.2 Task Execution Role — 권한 골격

| Statement | Action 후보 | Resource 범위 |
|-----------|-------------|--------------|
| ECR Pull | `ecr:GetAuthorizationToken`, `ecr:BatchGetImage`, `ecr:GetDownloadUrlForLayer` | repository ARN 한정 (`<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:<image-tag>`, `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-preprocessor:<image-tag>`) |
| CloudWatch Logs Write | `logs:CreateLogStream`, `logs:PutLogEvents`, `logs:DescribeLogStreams` | `/portfolio/paper/crawler*`, `/portfolio/paper/preprocessor*` log group ARN 한정 |
| Secrets / SSM Read | `secretsmanager:GetSecretValue`, `ssm:GetParameters` | service prefix Resource ARN 한정 |

### 6.3 wildcard 금지 정책 (03 §13 정합, R6.5 근거)

| 패턴 | 정책 |
|------|------|
| Resource `*` | **금지** |
| Action wildcard (`secretsmanager:*`, `ssm:*`, `ecs:*`, `Action: "*"`) | **금지** |
| service prefix 분리 | crawler / preprocessor 각각 독립 prefix 만 부여. 다른 service prefix 부여 금지 |

### 6.4 Task Execution Role / Task Role 책임 분리 (R6.6 근거)

Task Definition `secrets` 필드 주입용 Secrets Manager / SSM read 권한은 **Task Execution Role** 이 보유한다. application runtime 에서 AWS SDK 로 Secrets Manager / SSM Parameter Store 를 직접 조회하는 권한은 **Task Role** 이 보유한다. 본 1차 검증은 Task Definition `secrets` 필드 주입을 우선 사용한다.

## 7. Preprocessor 단발 실행 검증 (R7)

### 7.1 Task 실행 형태

| 항목 | 결정값 |
|------|--------|
| networkMode | `awsvpc` |
| Subnet 배치 | public subnet (02 spec `public-a` 또는 `public-b`) |
| `assignPublicIp` | `ENABLED` (NAT-free outbound) |
| 실행 방식 | `aws ecs run-task` 1회 단발. ECS Service / EventBridge / Step Functions 미사용 |
| 본 spec 범위 밖 | Service 상시 가동 / Scheduler 정기 기동 (R7.3 정합) |

### 7.2 검증 항목

| # | 점검 항목 | 기대 결과 |
|---|----------|----------|
| 1 | `preprocessor_app` 기준 RDS 접속 | 성공 (private endpoint, `sg-preprocessor-task` → `sg-rds-postgres` 5432 통과) |
| 2 | CloudWatch Logs 출력 | `/portfolio/paper/preprocessor` log group 안 stream 생성 / 본문 출력 |
| 3 | Task exit code | `0` |
| 4 | image pull / Secret 주입 | Task Execution Role 로그 / 환경변수 주입 정상 |

### 7.3 RDS 환경변수 매핑 (06 / 03 §6 인용)

| Secret 경로 | JSON key | 환경변수 키 |
|------------|----------|-------------|
| `/portfolio/paper/rds/preprocessor-app` | `host` | `INTEREST_DB_HOST` |
| 동상 | `port` | `INTEREST_DB_PORT` |
| 동상 | `dbname` | `INTEREST_DB_NAME` |
| 동상 | `username` | `INTEREST_DB_USER` |
| 동상 | `password` | `INTEREST_DB_PASSWORD` |

실제 RDS endpoint hostname / password 는 본 design 어디에도 평문 기록 금지. placeholder `<rds-endpoint>` / `[REDACTED]` 만 사용.

### 7.4 실행 실패 원인 후보

| # | 원인 후보 | 1차 점검 위치 |
|---|----------|--------------|
| 1 | 환경변수 주입 누락 (Task Definition `secrets` 필드 결손) | Task Definition JSON / CloudWatch Logs |
| 2 | Secret read 권한 누락 (Task Role 또는 Task Execution Role) | IAM 정책 / Task event 메시지 |
| 3 | RDS SG inbound 미허용 (`sg-preprocessor-task` 미등록) | RDS SG inbound rule |
| 4 | VPC Endpoint 누락 또는 public subnet 배치 누락 | Subnet / Route Table / VPC Endpoint |
| 5 | image entrypoint 결함 / requirements 미설치 | image build 단계로 회귀 |

## 8. Crawler 외부 outbound 리스크 별도 관리 (R8)

본 spec 시점 Crawler 는 1차 검토(Dockerfile / 빌드 가능 여부 / outbound 도달 여부) 까지만 수행한다. 운영 안정화 100% 보장은 본 spec 범위 밖이며, Selenium / Chrome 의존성 결함 발견 시 Preprocessor 검증 흐름을 차단하지 않는다(R8.3 / R8.4 정합).

### 8.1 외부 의존 / outbound 도메인 후보

| 의존 / 도메인 | 용도 | 1차 검토 |
|--------------|------|---------|
| Selenium / Chrome / chromedriver | 동적 페이지 수집 | Dockerfile 안 설치 단계 존재 여부만 확인. 안정성 검증은 후속 |
| `data.krx.co.kr`, `open.krx.co.kr` | KRX OTP / 시세 / 수급 / 공매도 | DNS / 443 outbound 도달 여부 1차 확인 |
| `finance.naver.com` | Naver 시세 / 뉴스 / 리포트 | 동상 |
| `query1.finance.yahoo.com` | yfinance API | 동상 |

### 8.2 리스크 후보 (분리 관리)

| ID 후보 | 리스크 | 본 spec 처리 |
|---------|-------|-------------|
| R-CRAWL-XXX | KRX 로그인 / OTP 만료 / rate limit | 1차 검토만. 운영 안정화는 후속 spec |
| R-CRAWL-XXX | Selenium / Chrome 비정상 종료 / headless 호환성 | 동상 |
| R-CRAWL-XXX | Naver / yfinance 응답 schema 변경 | 동상 |
| R-CRAWL-XXX | NAT-free 환경 outbound 도달 실패 (public subnet IP 차단) | §9 NAT-free 정책 재확인 |

## 9. NAT-free 정책 (R9)

### 9.1 정책 매트릭스

| 항목 | 정책 |
|------|------|
| NAT Gateway | **사용 금지** (비용 발생 차단) |
| 본 spec ECS Fargate Task outbound | 100% public subnet + `assignPublicIp = ENABLED` |
| 외부 API outbound (KRX / Naver / yfinance / holiday) | 동상 |
| NAT Gateway 발견 시 처리 | 본 spec 임의 결정 금지. [`../_common/operator-decisions.md`](../_common/operator-decisions.md) **OD-NET-001** / **OD-NET-002** 재확인 후 운영자 결정으로만 처리 (R9.3 정합) |

## 10. EC2 → ECS 운영 패턴 인계 (03 spec §13 입력)

### 10.1 매핑 표 (03 §13.1 인용 + crawler / preprocessor 채움)

| 영역 | EC2 (03 spec) | ECS Fargate (본 spec, 08) |
|------|--------------|--------------------------|
| 자격증명 | Instance Role + IMDSv2 | Task Role (crawler / preprocessor 분리) |
| secret / parameter read | 환경변수 주입(`/tmp/inject-env.sh` 또는 systemd `EnvironmentFile=`) | Task Definition `secrets` 필드 + Task Role |
| 로그 | CloudWatch Logs Agent 또는 `PutLogEvents` | awslogs driver → `/portfolio/paper/crawler`, `/portfolio/paper/preprocessor` |
| 접속 / 디버깅 | SSM Session Manager | ECS Exec |
| 자동 기동 | systemd unit 또는 startup script | **본 spec 범위 밖** (Service / EventBridge / Step Functions 후속 spec) |

### 10.2 service prefix 분리 (03 §13.2 정합)

| MS | service prefix |
|----|---------------|
| Crawler | `/portfolio/paper/crawler/*` |
| Preprocessor | `/portfolio/paper/preprocessor/*` |

후속 spec 은 다른 service prefix Resource 부여 금지 / Resource wildcard 금지 / Action wildcard 금지 / env prefix `paper` / `live` 만 사용 / Access Key 미사용 원칙(IMDSv2 + Role only) 을 변경하지 않는다(03 §13.3 정합).

## 11. 안전 제약 (R10, R11)

### 11.1 영역별 정책 (R10)

| 영역 | 정책 |
|------|------|
| 실제 AWS 리소스 | ECR repository / ECS Cluster / Task Definition / Service / IAM Role / Policy / CloudWatch Log Group / Secrets / SSM Parameter / RDS 의 생성·변경·삭제는 **운영자 직접 작업으로만**. 본 spec 의 모든 phase 에서 직접 수행 금지 |
| 8개 MS 코드 / docs / 패키징 | `port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research` 의 README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / `requirements.txt` / `Dockerfile` / `setup.py` / `pyproject.toml` 미수정 |
| 외부 호출 | KRX / Naver / yfinance / Selenium / Chrome / KIS API 호출 0건 |
| 크롤링 / 주문 | 크롤링 0건. 매수 / 매도 / 취소 / 정정 0건 |
| RDS DDL/DML | 0건. 조회성 SELECT 도 본 phase 시점 0건 |
| secret / 식별자 표기 | 실제 secret value, password, KIS app key·app secret, 계좌번호, token, RDS endpoint hostname, account-id, 실제 ARN, image digest, IAM access key id, instance-id 평문 기록 금지. `[REDACTED]` 또는 placeholder 만 |
| `GetSecretValue` 호출 | 운영자만 수행. Kiro 자동 검증은 `secretsmanager:DescribeSecret` metadata 만 사용 (06 §11 정합) |

### 11.2 phase 분리 (R11)

본 08 초기 문서 phase 산출물은 requirements.md / design.md / tasks.md 3개로 한정한다. runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md 는 운영자 실행 이후 별도 작성한다.

| 산출물 | 본 phase 처리 |
|--------|--------------|
| `requirements.md` / `design.md` / `tasks.md` | 본 08 초기 문서 phase 산출물 3개로 한정 |
| `runbook.md` / `validation-checklist.md` / `operation-notes.md` | 운영자 실행 이후 별도 작성 |
| `CHANGELOG.md` / `WORKLOG.md` (`docs/worklog/YYYY-MM-DD.md`) | 운영자 실행 이후 별도 작성 |
| `_common/*.md` 갱신 | 본 phase 실제 갱신 금지. 후보만 후속 phase 에서 식별 |

본 호출 단위 정책: 본 호출은 design.md 1개만 갱신하며 requirements.md / tasks.md 는 별도 호출 책임으로 분리한다.

## Testing Strategy (참고)

본 spec 은 ECR / ECS / IAM / 절차서 산출물이며, 코드 / 순수 함수 / 입력 변동에 따라 행위가 달라지는 알고리즘이 없다. 따라서 property-based testing(PBT) 은 적용되지 않으며, 본 design 은 Correctness Properties 섹션을 포함하지 않는다(03 spec Testing Strategy 동일 정책).

본 spec 의 검증은 다음 두 형태로만 수행되며 모두 후속 phase 책임이다.

- **정적 점검**: ECR repository 존재 / repository scan 설정 / image tag·digest 존재 / Task Definition `secrets` 매핑 / Task Role·Task Execution Role 권한 골격 / Log Group 존재 / NAT Gateway 부재 / wildcard 부재 / Access Key 부재. validation-checklist.md(후속 phase) 책임.
- **단발 실행 검증**: Preprocessor ECS Task 1회 `aws ecs run-task` → RDS 접속 성공 / CloudWatch Logs 출력 / exit code 0. runbook.md(후속 phase) 책임. Crawler outbound 검증은 본 spec 범위 밖이며 별도 phase 또는 후속 spec 책임.
