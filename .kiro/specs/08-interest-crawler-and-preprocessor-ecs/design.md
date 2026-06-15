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

## 12. 2026-06-12 운영자 검증 결과 / Hybrid execution model

본 섹션은 2026-06-12 Windows EC2 worker 기반 KRX GUI 의존 수집 1차 검증 결과를 반영한 보강 섹션이다. 기존 ECS 중심 설계를 전면 재작성하지 않고, 검증 결과로 갱신된 분류 결정과 Secrets Manager 연동 방식만 추가로 명시한다. 자세한 운영자 실행 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-12 섹션 참조.

### 12.1 Hybrid execution model 분류

본 spec 의 crawler / preprocessor runtime 은 단일 ECS Fargate 구조가 아니라 다음과 같이 역할을 분리한다.

| 워크로드 | runtime | 1차 운영 가능 상태 | 비고 |
|---------|---------|-------------------|------|
| Preprocessor MS (`port-interest-preprocessor`) | ECS Fargate Task (NAT-free public subnet + `assignPublicIp=ENABLED`) | 2026-06-10 도달 | §7 그대로 유지 |
| non-GUI crawler (Naver / yfinance / KRX 비-GUI 경로 후보) | ECS Fargate Task 후보 유지 | 미도달 (이월) | runtime 검증 / Selenium 미사용 / outbound 도달 검증 후속 |
| KRX GUI 의존 crawler (KRX program / KRX shortsell, Selenium / Chrome) | Windows EC2 worker | 2026-06-12 도달 (1차 운영 가능 / 완전 자동화는 후속) | wrapper 기반 수동 실행 |

### 12.2 KRX GUI 의존 수집이 EC2 worker 로 분리된 이유

| 이유 | 설명 |
|------|------|
| Chrome GUI / Download 의존 | KRX OTP 후 CSV 다운로드가 Chrome 다운로드 폴더로 떨어지는 흐름. 다운로드 경로가 OS 사용자 / 세션 의존 |
| 로그인 session 유지 | KRX 로그인 후 OTP / 세션 토큰이 브라우저 컨텍스트에 stateful 하게 결합 |
| Debug attach / 운영자 점검 용이성 | RDP 진입으로 즉시 GUI 상태 / 다운로드 폴더 / Chrome devtools 점검 가능 |
| KRX 사이트 특성 | 비정형 JavaScript / 동적 element / OTP 등으로 headless 안정성 미달 가능성 |
| ECS Fargate Task 의 GUI / Display 미지원 | Fargate 는 GUI / X11 / Display 지원이 사실상 없음. KRX OTP / Chrome download 흐름과 호환 부담 |

### 12.3 Secrets Manager 연동 방식 (2026-06-12 반영)

| Secret path | 용도 | JSON key | 1차 사용 시점 | 비고 |
|-------------|------|---------|--------------|------|
| `/portfolio/paper/rds/preprocessor-app` | preprocessor RDS 접속 | `host` / `port` / `dbname` / `username` / `password` | 2026-06-10 | §7.3 |
| `/portfolio/paper/rds/crawler-app` | EC2 worker 의 RDS 접속(crawler_app role) | `host` / `port` / `dbname` / `username` / `password` | 2026-06-12 | EC2 IAM Role `portfolio-paper-crawler-worker-role` 에서 read |
| `/portfolio/paper/krx/crawler-login` | KRX 로그인 자격 | `username` / `password` | 2026-06-12 | EC2 IAM Role inline policy 에 `secretsmanager:DescribeSecret` / `secretsmanager:GetSecretValue` 추가 (Resource 한정 / wildcard 0건) |

위 Secret 의 실제 value / RDS endpoint hostname / KRX 로그인 password / 실제 ARN / account-id 는 본 design 어디에도 평문 기록 금지(R10.4 / R-DOCS-001 정합). KRX 로그인 ID / password 는 "Secrets Manager 에서 주입" 으로만 표기한다.

### 12.4 EC2 worker 운영 모드 분리

| 모드 | 설명 | 본 spec 시점 |
|------|------|-------------|
| 수동 실행 (wrapper 기반) | 운영자가 RDP 접속 후 `run_krx_worker_daily.ps1` 1회 실행. venv activate / DB Secret / KRX Secret / 다운로드 경로 junction / KRX 로그인 / KRX program / KRX shortsell / 로그 저장을 1회 흐름으로 처리 | **1차 운영 가능 상태(2026-06-12 도달)** |
| 자동 실행 (SSM RunCommand + EventBridge Scheduler + 옵션상 Step Functions hybrid orchestration) | EC2 worker 무인 실행. 본 spec 범위 밖 / 후속 분리 | 미도달 |
| Idle 비용 절감 | 작업 종료 후 EC2 stop 절차 명시. 본 spec 범위 밖 / 후속 분리 | 미도달 |

### 12.5 본 섹션 갱신 원칙

- 기존 §1 ~ §11 결정값(ECR repository / Dockerfile 점검 / 로컬 빌드 / ECR push / ECS Cluster·Role·Log Group / Preprocessor 단발 실행 / NAT-free 정책 / 안전 제약) 은 변경하지 않는다.
- crawler 관련 §3.2 / §4 / §5 의 ECS 단일 전환 가정은 §12.1 hybrid execution model 분류로 보강한다(전면 재작성 아님).
- non-GUI crawler 가 ECS Fargate Task 로 운영되는 시점에는 §7 의 preprocessor 단발 실행 검증 패턴을 그대로 재사용한다.
- KRX GUI 의존 수집이 EC2 worker 로 운영되는 동안에도 NAT-free 정책(§9)은 EC2 worker 의 outbound 경로에 동일하게 적용된다(public subnet + EIP 또는 동등 방식 / 운영자 결정).
- 후속 spec / 후속 phase 책임 분리 원칙은 §11 그대로 유지한다.

## 13. 2026-06-13 운영자 검증 결과 / Hybrid execution model 1차 자동화 완성

본 섹션은 2026-06-13 SSM RunCommand 자동화 + ECS crawler revision 6 의 Selenium / Chrome / outbound smoke 1차 검증 결과를 반영한 보강 섹션이다. §12 hybrid execution model 분류는 그대로 유지하고, KRX GUI 경로의 자동화 1차 구조와 ECS crawler smoke 의 의미만 추가로 명시한다. 자세한 운영자 실행 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-13 섹션 참조.

### 13.1 KRX GUI 경로 1차 자동화 구조

| 단계 | 구성 요소 | 결정값 |
|------|----------|--------|
| 트리거 | EventBridge Scheduler | **후속 분리** (현재는 운영자 또는 SSM 수동 트리거) |
| 진입점 | SSM RunCommand (`AWS-RunPowerShellScript`) | 1차 자동화 진입점으로 사용 |
| 트리거 명령 | `schtasks /Run /TN Portfolio-KRX-Worker-Daily` | wrapper 직접 실행이 아니라 Scheduled Task 1회 실행 트리거만 수행 |
| 실행 컨테이너 | Windows Scheduled Task `Portfolio-KRX-Worker-Daily` | Administrator interactive session 으로 실행. SYSTEM Session 0 사용 금지 |
| 실행 스크립트 | `C:\portfolio\run_krx_worker_daily.ps1` | venv activate / RDS Secret 주입 / KRX Secret 주입 / 다운로드 경로 junction 점검 / `interest_krx_login_new.py` → `interest_program.py` → `interest_shortsell.py` 순차 실행 / 로그 저장 |
| 로그 위치 | `C:\portfolio\logs\krx_worker_daily_yyyyMMdd_HHmmss.log` | 파일 본문 전체는 본 design / operation-notes 평문 인용 금지 |

본 1차 자동화 구조는 paper 환경에서 1차 검증 완료 상태(2026-06-13 도달)이며, EventBridge Scheduler 정기 트리거 연계는 후속 분리한다.

### 13.2 SYSTEM Session 0 직접 실행 부적합 판단

| 항목 | 결과 |
|------|------|
| SSM RunCommand 의 실행 컨텍스트 | SessionId 0 / `nt authority\system` |
| RDP 사용자 세션 | SessionId 2(Administrator) |
| Chrome 프로세스 | SessionId 0 직접 실행 시 GUI / Display / Chrome download 폴더 / OTP 세션 컨텍스트 분리로 KRX 로그인 단계 실패 가능 |
| 결정 | SSM RunCommand 가 wrapper 를 SYSTEM Session 0 에서 직접 실행하는 방식은 **KRX GUI 로그인에 부적합**으로 판단. 직접 실행 방식은 채택하지 않음 |
| 우회 방식 | SSM RunCommand 는 `schtasks /Run` 트리거만 담당하고, 실제 wrapper 실행은 Administrator interactive session 안의 Scheduled Task 가 담당 |

### 13.3 ECS crawler Task Definition revision 6 의 의미

| 항목 | 결정값 |
|------|--------|
| family | `portfolio-paper-interest-crawler` |
| revision | 1 ~ 6 (최신 revision = 6) |
| image | `portfolio-interest-crawler:paper-20260611` |
| cpu / memory | 1024 / 2048 |
| network mode | `awsvpc` |
| Task Role | `portfolio-paper-crawler-task-role` |
| Task Execution Role | `portfolio-paper-ecs-task-execution-role` |
| Log Group | `/portfolio/paper/crawler` |
| Log stream prefix | `ecs-selenium-chrome-smoke` |
| `command` 의 의미 | **실제 daily crawler entrypoint 가 아니라 Selenium / Chrome / outbound smoke 검증용** |
| 운영용 Task Definition 분리 | **후속 분리** (실제 daily crawler 의 Task Definition / image / command 는 별도 revision 또는 별도 family 로 분리 — task 58) |

revision 6 의 RunTask smoke 결과(2026-06-13)는 다음을 1차 검증한다.

- ECS / Fargate Selenium 4.40.0 / Chromium / chromedriver runtime 동작
- public-a / public-b subnet + `sgroup-crawler-tasks` SG + `assignPublicIp = ENABLED` 기반 outbound 도달
- `example.com` HTTPS 도달 / Naver Finance 페이지 로딩(TITLE `Npay 증권` 확인)
- exitCode 0 / `SELENIUM CHROME SMOKE SUCCESS` / `DRIVER QUIT` / `SELENIUM CHROME SMOKE END`

따라서 revision 6 은 ECS / Fargate / Chromium runtime 가용성에 대한 smoke 보증으로만 해석한다. 실제 daily crawler 의 운영 안정화는 본 spec 범위 밖이며, non-GUI crawler 인벤토리 확정과 운영용 Task Definition 분리(task 58)는 후속 spec / 후속 phase 책임이다.

### 13.4 Hybrid execution model 1차 완성 판단

| 워크로드 | 실행 위치 | 1차 운영 가능 상태 |
|---------|----------|-------------------|
| Preprocessor MS | ECS Fargate Task | 2026-06-10 도달 |
| KRX GUI 의존 crawler (KRX program / KRX shortsell) | Windows EC2 worker (Scheduled Task + wrapper) | 2026-06-12 도달 |
| KRX GUI 경로 자동화 trigger | SSM RunCommand → `schtasks /Run` | 2026-06-13 도달 |
| non-GUI crawler runtime 가용성 (Selenium / Chrome / outbound) | ECS Fargate Task (smoke 용 revision 6) | 2026-06-13 1차 통과 |
| EventBridge Scheduler / Step Functions 정기 trigger | — | 미도달 (후속 분리) |
| EC2 worker 무인 stop / 비용 절감 | — | 미도달 (후속 분리) |

본 시점의 hybrid execution model 1차 완성 판단은 위 6개 차원의 1차 검증 통과 + 자동화 trigger 1단계 도달을 근거로 한다. EventBridge Scheduler 정기 trigger / Step Functions hybrid orchestration / non-GUI crawler 운영용 Task Definition 분리 / wrapper 내 DB 검증 자동 출력 / EC2 worker stop 절차는 모두 후속 spec / 후속 phase 책임이다.

### 13.5 본 섹션 갱신 원칙

- 기존 §1 ~ §12 결정값은 변경하지 않는다.
- §13 은 §12.4 EC2 worker 운영 모드 분리 표의 `자동 실행 (SSM RunCommand + EventBridge Scheduler + 옵션상 Step Functions hybrid orchestration)` 행을 1차 자동화 진입점(SSM RunCommand → Scheduled Task trigger)까지 도달한 상태로 갱신하는 보강이다.
- EventBridge Scheduler 정기 trigger 연계는 본 spec 범위 밖이며 후속 분리 원칙(§11 / §12.5) 그대로 유지한다.
- non-GUI crawler 의 실제 운영용 Task Definition 분리는 본 §13 의 smoke 결과(revision 6)와는 다른 후속 작업으로 구분한다(task 58).

## Testing Strategy (참고)

본 spec 은 ECR / ECS / IAM / 절차서 산출물이며, 코드 / 순수 함수 / 입력 변동에 따라 행위가 달라지는 알고리즘이 없다. 따라서 property-based testing(PBT) 은 적용되지 않으며, 본 design 은 Correctness Properties 섹션을 포함하지 않는다(03 spec Testing Strategy 동일 정책).

본 spec 의 검증은 다음 두 형태로만 수행되며 모두 후속 phase 책임이다.

- **정적 점검**: ECR repository 존재 / repository scan 설정 / image tag·digest 존재 / Task Definition `secrets` 매핑 / Task Role·Task Execution Role 권한 골격 / Log Group 존재 / NAT Gateway 부재 / wildcard 부재 / Access Key 부재. validation-checklist.md(후속 phase) 책임.
- **단발 실행 검증**: Preprocessor ECS Task 1회 `aws ecs run-task` → RDS 접속 성공 / CloudWatch Logs 출력 / exit code 0. runbook.md(후속 phase) 책임. Crawler outbound 검증은 본 spec 범위 밖이며 별도 phase 또는 후속 spec 책임.
