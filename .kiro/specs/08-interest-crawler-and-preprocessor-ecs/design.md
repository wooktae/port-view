# Design Document — 08-interest-crawler-and-preprocessor-ecs

## Introduction

본 spec 의 핵심을 한 줄로 요약한다. **`port-interest-crawler` / `port-interest-preprocessor` 두 Python MS 를 `aws-paper` 환경의 ECR / ECS 위에서 1차 실행 검증할 수 있도록 ECR repository / Dockerfile 점검 / 로컬 빌드 / ECR push / ECS Cluster·Role·Log Group / Preprocessor 단발 실행 / Crawler outbound 리스크 분리 절차 기준을 확정한다.**

- 1차 적용 환경: `aws-paper`, region `ap-northeast-2`. `aws-live` 와 10 spec 통합 cutover 는 본 spec 범위 밖.
- 입력: [`./requirements.md`](./requirements.md) R1 ~ R11, [`../02-aws-network-and-rds`](../02-aws-network-and-rds)(VPC / Subnet / SG / VPC Endpoint / RDS), [`../06-secrets-and-iam`](../06-secrets-and-iam)(Secrets / SSM / Role 정책), [`../03-marketconnector-ec2`](../03-marketconnector-ec2)(EC2 → ECS 운영 패턴 §13).
- 본 08 초기 문서 phase 산출물은 requirements.md / design.md / tasks.md 3개로 한정한다. runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md 는 운영자 실행 이후 별도 작성한다(R11 정합).

본 문서는 실제 secret value, account-id, RDS endpoint hostname, image digest, 실제 ARN, instance-id, IAM access key id 를 평문 기록하지 않는다. 모두 `[REDACTED]` 또는 placeholder(`<account-id>`, `<region>`, `<ecr-repo-uri>`, `<image-tag>`, `<task-arn>`, `<rds-endpoint>`, `<cluster-name>`) 만 사용한다(R10.4 정합).

## Runtime Role Split at a Glance

Hybrid execution model 의 runtime 경계는 GUI 요구 여부로 결정된다. 자세한 근거는 §12(hybrid 분류) / §13(SSM 자동화) / §15(rev7 non-GUI 운영 경로 + Autologon) / §17(Step 2 성공판정 강화) 참조.

| Workload | Runtime | 진입점 / trigger | 성공 판정 |
|---|---|---|---|
| Preprocessor MS | ECS Fargate Task (single-run) | `aws ecs run-task` 1회 | exit code 0 / CloudWatch Logs `PREPROCESSOR PIPELINE END` / feature `updated_at` 갱신 |
| non-GUI crawler | ECS Fargate Task (`portfolio-paper-interest-crawler:7`, daily 운영용) | `aws ecs run-task` 1회 (wrapper Step 2 안에서) | exit code 0 / 8종 non-GUI step SUCCESS / raw table `max(trade_date)` 정합 |
| KRX GUI crawler | Windows EC2 worker (Autologon → Administrator console → Scheduled Task) | SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` | Running→Ready 복귀 + Last Result 0 + latest log tail + KRX raw DB validation exit code 0 |
| ECS Task Definition rev6 (참조 이력) | ECS Fargate (smoke 전용) | 운영 진입점 아님 | 2026-06-13 §5 통과 시점 상태 유지 / 운영 대상 아님 |

**진입점 원칙**

- SSM direct Python / wrapper 실행은 SYSTEM Session 0 부적합. 채택 거부(§13.2 / §15.5).
- SSM RunCommand 는 `schtasks /Run` trigger 역할만 담당.
- Scheduled Task trigger 성공 ≠ Step 2 SUCCESS. 6개 성공 조건(§17.1) 모두 통과 필요.
- Headless / 비대화형 KRX 수집은 로컬 검증상 운영 방식에서 제외(§15.6).
- Autologon 은 paper 전용 Windows worker 한정 보안 예외(R-SEC-009 / §12 · §15.4).

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

## 14. 2026-06-15 운영자 검증 결과 / Interest Crawler 상태 재판정

본 섹션은 2026-06-15 운영자가 직접 수행한 Backend AWS E2E dry-run 1차 점검 결과를 반영한 보강 섹션이다. §12 Hybrid execution model 분류와 §13 1차 자동화 완성 판단 자체는 변경하지 않고, 본 spec 의 crawler runtime 상태 표현을 "전체 완료" 가 아닌 "hybrid 1차 / 부분 완료" 로 보정한다. 자세한 운영자 실행 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-15 섹션 / 본 일자 결정 락은 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-MS-020 / OD-MS-021 참조.

### 14.1 표현 보정 (OD-MS-020 정합)

기존 표현은 KRX GUI worker 의 운영 가능 도달과 ECS / Fargate Selenium Chrome smoke 통과를 묶어 Interest Crawler 전체가 완성된 것으로 해석될 여지가 있어 보정한다.

| 기존 표현 | 보정 표현 |
|----------|----------|
| Interest Crawler 완성: 완료 | Interest Crawler hybrid 1차 구현: 부분 완료 |
| Interest Crawler 는 hybrid execution model 기준으로 1차 완성 | KRX GUI worker 는 운영 가능 상태로 1차 완성 / ECS · Fargate crawler 는 smoke 검증 완료 / non-GUI daily raw 수집 운영 경로와 raw 전체 최신성 검증은 후속 |

본 표현 보정은 다음 원칙을 따른다.

- "완료" 표기는 실제 데이터 적재 / 최신성 검증까지 확인된 경우에만 사용한다.
- KRX GUI worker 완료와 Interest Crawler 전체 완료를 혼동하지 않는다.
- smoke 성공과 daily raw 최신성 성공을 분리한다.

### 14.2 워크로드 별 본 일자 상태 (2026-06-15)

§12.1 hybrid execution model 분류 표를 본 일자 상태로 갱신한다(분류 자체는 변경 없음).

| 워크로드 | runtime | 본 일자 상태 | 비고 |
|---------|---------|-------------|------|
| Preprocessor MS (`port-interest-preprocessor`) | ECS Fargate Task | 본 일자 단발 RunTask 성공(exitCode 0 / `updated_at` 갱신) — 데이터 최신성 제약 | §7 그대로 유지 |
| KRX GUI 의존 crawler (KRX program / KRX shortsell, Selenium / Chrome) | Windows EC2 worker | 본 일자 wrapper 재실행 성공 — `[Collected Date] None` idempotent / `interest_program_raw` · `interest_shortsell_raw` 최신일 2026-06-12 | KRX 거래일 정상(2026-06-15 월요일 기준 직전 거래일까지 적재) |
| non-GUI crawler (Naver / yfinance / KRX 비-GUI 경로 후보) | ECS Fargate Task 후보 유지 | **운영 실행 미도달** — Selenium Chrome smoke(2026-06-13 §13.3 revision 6)는 통과하였으나 실제 daily raw 수집 운영 경로 / Task Definition / command 분리 / outbound 도달 검증은 미완료 | task 58 / task 72 후속 분리 |

### 14.3 non-GUI crawler 인벤토리 분류 (운영 경로 분리 후속)

본 일자 1차 분류는 다음과 같다. 실제 운영용 Task Definition / command 분리는 task 58 / task 72 후속.

- ECS Fargate 후보(non-GUI 가능성 높음): `interest_news.py` / `interest_agency.py` / `interest_foreignindex.py` / `interest_commodity.py` / `interest_macroeconomic.py` / `interest_price.py` / `interest_investorflow.py` / `interest_marketbreadth.py`
- ECS Fargate 제외(KRX GUI 의존 또는 별도 분리): `interest_krx_login_new.py` / `interest_program.py` / `interest_shortsell.py` / `interest_ticker_value.py`

위 분류는 운영용 Task Definition 분리 시점에 1:1 매핑으로 다시 점검한다. `interest_crawler_daily.py` 자체는 KRX GUI 의존 파일과 non-GUI 파일을 동시에 호출하므로 ECS Fargate 단독 실행 대상에서 제외(2026-06-13 §13.3 정합).

### 14.4 raw 최신성 검증 부족과 downstream 영향

본 일자 SQL 점검 결과(2026-06-15 시점):

- `interest_program_raw` / `interest_shortsell_raw` 최신일 = 2026-06-12 (KRX GUI worker 경로 정상)
- non-GUI raw 7종(`interest_agency_raw` / `interest_news_raw` / `interest_commodity_raw` / `interest_foreignindex_raw` / `interest_investorflow_raw` / `interest_marketbreadth_raw` / `interest_price_raw`) — 직전 거래일까지 적재되지 않음
- `interest_ticker_value_raw` 최신일 = 2026-03-09(본 dry-run 핵심 차단 요인에서는 제외 / 별도 후속 분리)

이 상태에서 preprocessor ECS RunTask 가 성공하더라도 신규 feature date 생성이 제한된다(본 일자 §4 정합). Backend AWS E2E dry-run 흐름에서 `BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL` 로 넘어가기 전 raw 최신성 회복이 선결 조건이며, 후속 spec(04 / 09)에서 stale data 입력 위험으로 작용한다(R-DATA-009 / R-DATA-010 정합).

### 14.5 Backend AWS E2E dry-run 진입 정합

§12 / §13 의 hybrid execution model 1차 완성 판단은 그대로 유지하되, View Daily Batch 17단계 순서를 기준으로 본 일자 backend AWS 실행 상태를 정리한 결과는 다음과 같다(자세한 표는 [`./operation-notes.md`](./operation-notes.md) 2026-06-15 §6).

| 분류 | 본 일자 상태 |
|------|-------------|
| 완료 | 1번 `CONNECTOR_BALANCE` / 3번 `PREPROCESSOR`(데이터 최신성 제약) |
| 부분 완료 / follow-up 승격 | 2번 `INTEREST_CRAWLER` |
| 미진행 | 4 ~ 7번(`BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL`) |
| 미진행 / dry-run skip 예정 | 8 ~ 17번(`DAILY_BUY_EXECUTION` / `DAILY_SELL_EXECUTION` / `DAILY_AUTO_SELL` / `DAILY_AUTO_BUY` / `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` / `CONNECTOR_ORDER_CHECK` / `SYNC_SELL_FILL` / `SYNC_BUY_FILL` / `SYNC_BUY_POSITION` / `BALANCE_REFRESH`) |

본 표는 OD-MS-021 결정 락의 입력으로 사용된다. 실제 BUY / SELL / `--execute` / fill·position sync 자동 재시도 / aws-live 작업은 모두 0건이다(§11.1 / OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 ~ R-AUTO-011 정합).

### 14.6 본 섹션 갱신 원칙

- 기존 §1 ~ §13 결정값은 변경하지 않는다.
- §14 는 §12 의 hybrid execution model 분류와 §13 의 1차 자동화 완성 판단 위에 표현 보정 / 본 일자 상태 갱신 / non-GUI 운영 미도달 명시 / raw 최신성 부족 영향 명시만 추가한다.
- non-GUI crawler 의 실제 운영용 Task Definition 분리, raw 최신성 회복 작업, preprocessor 재실행, raw / feature 최신성 검증 SQL 자동화는 모두 후속 spec / 후속 phase 책임이다(task 72 / 73 / 74 / 77 / 78 / 58 정합).

## 15. 2026-06-16 운영자 검증 결과 / Hybrid execution model 갱신 (Crawler 데이터 미수집 해결 + KRX EC2 자동화 성공)

본 섹션은 2026-06-16 운영자 직접 수행 결과를 반영한 보강 섹션이다. §1 ~ §14 결정값은 변경하지 않고 아래만 보강한다.

작업 범위:

- (a) Crawler 데이터 미수집 원인 해소(non-GUI 전용 orchestration / Task Definition 부재 식별)
- (b) `interest_crawler_daily_nongui.py` 신규 + Docker rebuild + ECR push + ECS Task Definition revision 7 등록 + RunTask exitCode 0
- (c) non-GUI raw 6종 + KRX raw 2종 + news / agency 2026-06-16 적재 회복
- (d) Windows EC2 worker Autologon bootstrap + Administrator console session Active 확인 + SSM RunCommand → `schtasks /Run` → Scheduled Task 흐름 재검증

보강 대상: hybrid execution model 의 운영 상태 표현 / non-GUI rev7 의미 / KRX GUI 자동 로그인 운영 방식 1차 실증 / SSM direct Python · wrapper 실행 부적합 / Headless · 비대화형 KRX 수집 운영 방식 제외 결정.

참조: [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §1 ~ §5 / 결정 락 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-MS-022 + OD-MS-011 / OD-MS-015 / OD-MS-020.

### 15.1 표현 보정 (OD-MS-020 / OD-MS-022 정합)

§14.1 의 보정 표현은 본 일자 결과로 추가 보강한다. "완료" 표기는 실제 데이터 적재 / 최신성 검증까지 확인된 경우에만 사용한다는 원칙은 그대로 유지한다.

| 기존 표현 | 보정 표현 |
|----------|----------|
| Interest Crawler hybrid 1차 구현: 부분 완료 | Interest Crawler hybrid 구조 완료(non-GUI rev7 운영 경로 생성 + RunTask 성공 + raw 최신성 회복) |
| non-GUI daily raw 수집 운영 경로와 raw 전체 최신성 검증은 후속 | non-GUI ECS / Fargate rev7 운영 경로 생성 및 RunTask 성공(전체 step SUCCESS / raw 최신성 회복) |
| KRX EC2 수집: 진행 예정 | KRX GUI crawler = Windows EC2 worker + Autologon + Administrator interactive session + Scheduled Task + SSM trigger 성공 |
| KRX headless 장기 후보 | 로컬 검증상 운영 방식에서 제외(KRX 로그인 / nos_setup / 키보드보안 / iframe 제약) |
| Preprocessor 실행 완료(데이터 최신성 제약) | Preprocessor = raw 입력 데이터 회복 후 ECS 재실행 가능 상태 도달(재실행은 후속) |

### 15.2 워크로드 별 본 일자 상태 (2026-06-16)

§12.1 / §14.2 hybrid execution model 분류 표를 본 일자 상태로 갱신한다(분류 자체는 변경 없음).

| 워크로드 | runtime | 본 일자 상태 | 비고 |
|---------|---------|-------------|------|
| Preprocessor MS (`port-interest-preprocessor`) | ECS Fargate Task | raw 입력 데이터 회복 후 재실행 가능 상태 도달 / 재실행은 후속(task 77) | §7 / §14.2 그대로 유지 |
| non-GUI crawler | ECS Fargate Task Definition revision 7 | 운영 경로 생성 및 RunTask 성공(failures 0 / exitCode 0 / 약 9분 51초 / 전체 step SUCCESS) | §15.3 신규 |
| KRX GUI crawler (KRX program / KRX shortsell) | Windows EC2 worker + Autologon + Administrator interactive session + Scheduled Task + SSM trigger | 본 일자 KRX login / program / shortsell 2026-06-15 적재 성공(`interest_program_raw` 547 → 548 / `interest_shortsell_raw` 190,554 → 190,903) | §15.4 신규 |

### 15.3 ECS Task Definition revision 6 / revision 7 의미 분리

| 항목 | revision 6 (Selenium / Chrome smoke 전용) | revision 7 (non-GUI daily 운영용) |
|------|-----------------------------------------|---------------------------------|
| family | `portfolio-paper-interest-crawler` | `portfolio-paper-interest-crawler` |
| revision | 6 | 7 |
| image tag | `paper-20260611` | `paper-20260616-nongui` |
| `command` | Selenium Chrome smoke command | `["python", "interest_crawler_daily_nongui.py"]` |
| log stream prefix | `ecs-selenium-chrome-smoke` | `ecs-crawler-nongui-daily` |
| 의미 | ECS / Fargate / Chromium runtime 가용성에 대한 smoke 보증 | non-GUI 8종(`interest_news` / `interest_agency` / `interest_foreignindex` / `interest_commodity` / `interest_macroeconomic` / `interest_price` / `interest_investorflow` / `interest_marketbreadth`) daily raw 운영 entrypoint |
| 운영 시점 사용 여부 | 사용 안 함(smoke 검증 종료) | 본 일자부터 daily 운영 entrypoint 로 사용 |

revision 6 의 RunTask smoke 결과(2026-06-13 §13.3) 는 그대로 유효하지만 daily 운영용 Task Definition 은 본 일자 revision 7 로 분리되었다. revision 7 은 KRX GUI 계열 3종(`interest_krx_login_new` / `interest_program` / `interest_shortsell`) 을 import 하지 않으며, ECS / Fargate 단독 실행 대상에서 제외되는 KRX GUI 단계는 §15.4 의 Windows EC2 worker 경로로 분리된다.

### 15.4 KRX GUI 자동 로그인 기반 운영 방식 (OD-MS-022 정합)

KRX GUI 의존 crawler 의 1차 자동화 흐름(2026-06-13 §13.1) 은 그대로 유지하되, 본 일자 Autologon bootstrap 1차 실증으로 다음 흐름이 운영 방식으로 확정된다.

| 단계 | 구성 요소 | 본 일자 상태 |
|------|----------|-------------|
| 사전 조건 | Microsoft Sysinternals Autologon 으로 Administrator 자동 로그인 / EC2 재부팅 후 SSM Online + `query user` Administrator console session Active 확인 | 1차 실증 통과 |
| 트리거 | SSM RunCommand (`AWS-RunPowerShellScript`) | 1차 실증 통과 |
| 트리거 명령 | `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"` | 1차 실증 통과 |
| 실행 컨테이너 | Windows Scheduled Task `Portfolio-KRX-Worker-Daily` (Logon Mode `Interactive only` / Run As User `Administrator`) | 1차 실증 통과 |
| 실행 스크립트 | `powershell.exe -ExecutionPolicy Bypass -File C:\portfolio\run_krx_worker_daily.ps1` | 1차 실증 통과 |
| 자식 호출 | `python interest_krx_login_new.py` → `python interest_program.py` → `python interest_shortsell.py` | 1차 실증 통과 |
| 결과 | Last Result `0` / wrapper `DONE :: KRX worker daily` / `interest_program_raw` 2026-06-15 / `interest_shortsell_raw` 2026-06-15 | 1차 실증 통과 |

본 일자 결과로 KRX GUI crawler 의 운영 모드는 "wrapper 기반 수동 실행"(OD-MS-012)에서 "Autologon + Administrator interactive session + Scheduled Task + SSM trigger 자동화"(OD-MS-022)로 1차 자동화 진입 완료. EventBridge Scheduler 정기 trigger 연계는 §11 / §12.5 / §13.5 그대로 후속 분리한다.

### 15.5 SSM direct Python / wrapper 실행 부적합 명시

§13.2 의 SYSTEM Session 0 직접 실행 부적합 판단을 본 일자 결과로 보강한다.

- SSM RunCommand 가 wrapper(`run_krx_worker_daily.ps1`) 또는 Python(`interest_krx_login_new.py`) 을 직접 실행하는 방식은 **운영 방식에서 제외**한다.
- 이유 — SSM RunCommand 자체가 SYSTEM 으로 실행되며 Session 0 / 비대화형 컨텍스트에서 KRX GUI / Chrome download / nos_setup / 키보드보안 / iframe 흐름을 처리할 수 없다.
- 정상 동작 — SSM RunCommand 의 `whoami` 결과는 `nt authority\system` 으로 출력되는 것이 정상이며, 실제 KRX GUI 실행 컨텍스트는 별도 Administrator console interactive session 안의 Scheduled Task 가 담당한다.

### 15.6 Headless / 비대화형 KRX 수집 운영 방식 제외 명시

KRX 사이트의 동작 특성(KRX 로그인 / nos_setup / 키보드보안 / iframe 제약 / OTP 등) 으로 인해 headless 또는 비대화형 KRX 수집은 본 일자까지의 로컬 검증상 운영 안정성 미달로 판단된다.

- Headless KRX 수집: 로컬 검증상 운영 방식에서 제외
- 비대화형 KRX 수집: 운영 방식에서 제외
- 본 결정은 OD-MS-022 정합으로 본 spec 의 KRX GUI crawler 운영 모드 단일화에 사용된다. 장기적으로 KRX 사이트 변경 / 로컬 검증 결과 변경에 따라 재검토 가능 — 본 시점에서는 후속 분리하지 않고 운영 방식에서 제외 결정만 락한다.

### 15.7 Backend AWS E2E dry-run 진입 정합

§14.5 의 표를 본 일자 상태로 갱신한다(17단계 순서와 안전 기준은 OD-MS-021 그대로 유지).

| 분류 | 본 일자 상태 |
|------|-------------|
| 완료 | 1번 `CONNECTOR_BALANCE` / 2번 `INTEREST_CRAWLER`(Crawler 데이터 미수집 해결 완료 / hybrid 구조 완료) |
| Preprocessor 재실행 가능 상태 도달 / 재실행은 후속 | 3번 `PREPROCESSOR` |
| 후속 재개 예정 | 4 ~ 7번(`BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL`) |
| 미진행 / dry-run skip 예정 | 8 ~ 17번(`DAILY_BUY_EXECUTION` / `DAILY_SELL_EXECUTION` / `DAILY_AUTO_SELL` / `DAILY_AUTO_BUY` / `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` / `CONNECTOR_ORDER_CHECK` / `SYNC_SELL_FILL` / `SYNC_BUY_FILL` / `SYNC_BUY_POSITION` / `BALANCE_REFRESH`) |

실제 BUY / SELL / `--execute` / fill · position sync 자동 재시도 / aws-live 작업은 모두 0건이다(§11.1 / OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 ~ R-AUTO-011 / OD-MS-021 정합).

### 15.8 본 섹션 갱신 원칙

- 기존 §1 ~ §14 결정값은 변경하지 않는다.
- §15 는 §12 / §13 / §14 위에 본 일자 1차 실증 결과(non-GUI rev7 운영 경로 생성 + RunTask 성공 + raw 최신성 회복 + KRX EC2 자동 로그인 기반 운영 방식 실증)를 반영한 표현 갱신과 결정 락(OD-MS-022)만 추가한다.
- Preprocessor ECS 재실행 / Backend AWS E2E dry-run 재개(`BACKTEST_RESEARCH` → `BACKTEST_REPORT` → `DAILY_BUY_SIGNAL` → `DAILY_POSITION_SIGNAL` 순서) / `interest_foreignindex_raw` HANGSENG · NIKKEI225 · SHANGHAI NULL Data 후속 점검 / EventBridge Scheduler 정기 trigger / Step Functions hybrid orchestration / wrapper 내 DB 검증 출력 자동 추가 / EC2 worker stop 절차 / Chrome process 정리 옵션 / View 구현은 모두 후속 spec / 후속 phase 책임이다.

## 16. 2026-06-20 운영자 검증 결과 / Daily AWS Paper Wrapper 최종 점검 + EC2 lifecycle 후속 필요성

본 섹션은 2026-06-20 운영자 직접 수행한 (a) Daily AWS Paper Wrapper 구조 / 안전 기준 / EC2 기동 기준 최종 점검, (b) RunDate `2026-06-18` / Step 1 ~ Step 11 범위 wrapper 재실행 시도 안전 중단, (c) 6/19 KRX raw 최신성 복구 상태 점검 결과를 반영한 보강 섹션이다. §12 ~ §15 결정값은 변경하지 않고, EC2 lifecycle 후속 필요성과 Step 2 wrapper 성공판정 강화 진입 근거만 추가로 명시한다. 자세한 운영자 실행 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-20 §1 ~ §5 참조.

### 16.1 EC2 기동 기준 (Step 별 의존성)

본 spec 의 Step 2 INTEREST_CRAWLER 책임 범위와 인접 step 의 EC2 의존성을 명시한다.

| Step | 의존 EC2 | 본 일자 운영 메모 |
|------|----------|------------------|
| Step 1 `CONNECTOR_BALANCE` | MarketConnector EC2 | EC2 stop · start 후 `/tmp/inject-env.sh` 유실 가능 / 본 일자 1차 실증(재생성 후 통과). 03 spec 책임 |
| Step 2 `INTEREST_CRAWLER` | (a) non-GUI ECS Fargate Task / (b) Crawler Worker EC2(KRX GUI) | non-GUI 는 EC2 의존 없음 / KRX GUI 는 Crawler Worker EC2 가 `running` 상태 + Administrator interactive session 필요 |
| Step 12 / 13 / 17 | MarketConnector EC2 | broker / KIS 호출 또는 BALANCE_REFRESH / 03 spec 책임 |

### 16.2 EC2 lifecycle 후속 필요성 (08 spec 입력)

- 두 EC2 가 stopped 상태이면 wrapper 실행 전 운영자가 직접 start 필요 / 종료 후 stop 필요(R-AUTO-016 / R-AUTO-017 mitigation 정합).
- MarketConnector EC2 의 `/tmp/inject-env.sh` 유실 대응 / SSM Online wait / KRX worker EC2 stop 절차의 자동화는 본 spec 범위 밖 / 후속 분리(task 59 정합).
- 본 일자 6/18 wrapper 중복 실행 시도 결과 — Step 12 미실행 / 신규 broker 주문 0건 / 신규 execution_plan 0건 / Crawler Worker chrome 잔여 프로세스는 EC2 stop 으로 정리 / R-AUTO-002 / R-AUTO-019 mitigation 정합.

### 16.3 본 섹션 갱신 원칙

- §12 ~ §15 결정값은 변경하지 않는다.
- §16 은 EC2 기동 기준 / EC2 lifecycle 후속 필요성 / 6/18 중복 실행 시도 결과만 보강한다.
- KRX GUI 경로의 자동 로그인(OD-MS-022) / non-GUI ECS Fargate 운영 경로(OD-MS-011) / 표현 보정(OD-MS-020) 정책은 본 일자에도 그대로 유지된다.

## 17. 2026-06-21 운영자 검증 결과 / Step 2 INTEREST_CRAWLER 성공판정 강화 (OD-MS-026 신규)

본 섹션은 2026-06-21 운영자 직접 수행 결과를 반영한 보강 섹션이다. §12 ~ §16 결정값은 변경하지 않고, Step 2 성공 조건과 worker stopped fail-closed 설계 변경 / KRX raw validation script 의 Step 2 guard 역할만 추가로 명시한다.

작업 범위:

- (a) `step-02-interest-crawler.ps1` 성공판정 강화
- (b) non-GUI ECS crawler env 보강
- (c) `interest_krx_raw_validate_daily.py` 신규 생성 + EC2 배포 + EC2 단독 검증
- (d) `step-02-interest-crawler.ps1` DB validation 연동
- (e) crawler worker stopped fail-closed 처리
- (f) Step 2 단독 실행 검증

참조: [`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1 ~ §8 / 결정 락 OD-MS-026 신규 / R-AUTO-020 신규 mitigation 1차 실증 / R-AUTO-007 / R-AUTO-016 / R-AUTO-017 보강.

### 17.1 Step 2 성공 조건 (강화)

Step 2 `INTEREST_CRAWLER` 의 SUCCESS 조건은 다음 6개를 모두 통과해야 한다(OD-MS-026 정합 / R-AUTO-020 mitigation 정합).

1. **non-GUI ECS crawler exitCode 0** — Task Definition `portfolio-paper-interest-crawler:7` RunTask 의 lastStatus `STOPPED` / container exitCode 0 / stoppedReason `Essential container in task exited` / CloudWatch log 저장 확인.
2. **Crawler Worker EC2 running** — instance state `running` 이 아니면 Step 2 fail-closed(아래 §17.2 참조).
3. **Windows Scheduled Task `Portfolio-KRX-Worker-Daily` Running → Ready 복귀** — `schtasks /Run` 으로 trigger / `Running` 상태 polling → `Ready` 복귀 wait / `sawRunning` 로그 출력 / timeout 시 Step 2 실패.
4. **Last Result 0 또는 0x0** — Scheduled Task 종료 후 Last Result 확인 / Scheduled Task trigger 성공만으로 SUCCESS 처리하지 않는다(R-AUTO-020 mitigation 핵심).
5. **latest worker log path / tail 출력** — `C:\portfolio\logs\krx_worker_daily_*.log` 최신 파일 path / last write time / size / tail 출력. `KRX login SUCCESS` / `KRX program SUCCESS` / `KRX shortsell SUCCESS` / `DONE :: KRX worker daily` 라벨 확인(본문 평문 인용 0건 / R-DOCS-001 정합).
6. **KRX raw DB validation 통과** — `INTEREST_CRAWLER_KRX_DB_VALIDATE` SSM step → `interest_krx_raw_validate_daily.py --expected-date <ExpectedKrxRawDate>` 실행 → `interest_program_raw` / `interest_shortsell_raw` 의 expected trade_date 기준 row_count + `max(trade_date)` 검증 / non-zero exit 또는 row_count 0 시 Step 2 fail. step result 에 `KrxDbValidationCommandId` 포함.

### 17.2 worker stopped 처리 (skip → fail-closed)

| 시점 | 처리 방식 | 결과 |
|------|-----------|------|
| ~ 2026-06-20 (이전) | crawler worker EC2 가 `running` 이 아니면 wrapper 안에서 KRX GUI Scheduled Task trigger 자동 skip | skip 상태에서도 Step 2 SUCCESS 가능성 존재 / KRX raw 미적재가 Step 3 이후로 전파될 위험(R-AUTO-016 / R-AUTO-020 정합) |
| 2026-06-21 (현재) | crawler worker EC2 가 `running` 이 아니면 즉시 Step 2 실패(fail-closed) / instanceId / state 출력 | KRX GUI worker · DB validation 미수행 상태에서 Step 2 SUCCESS 진입 차단 |

본 변경은 R-AUTO-016 mitigation 갱신과 R-AUTO-020 신규 mitigation 의 핵심 설계 변경이며, 본 일자 단독 실행 검증에서는 worker state `running` 이 1차 실증되어 fail-closed 분기 자체는 진입하지 않았다(`§17.4 정합`).

### 17.3 KRX raw validation script 의 Step 2 guard 역할

- `interest_krx_raw_validate_daily.py` 는 Step 2 guard 로 사용되며, Step 2 가 SUCCESS 처리되기 전 단계의 마지막 체크 포인트다.
- 검증 대상은 `interest_program_raw` / `interest_shortsell_raw` 두 raw table 이며, expected trade_date 기준 row_count + `max(trade_date)` 가 expected 이상인지 확인한다. 실패 시 exit code 30 반환.
- DB session 정합 — user `crawler_app` / schema `interest` / search_path `interest, reference, legacy, public`(2026-06-21 EC2 단독 검증 결과 정합).
- 본 script 가 Step 2 guard 위치에 배치됨으로써 wrapper 가 Scheduled Task trigger 성공만 보고 Step 2 SUCCESS 처리하던 한계(R-AUTO-007 / R-AUTO-020)가 1차 차단된다.

### 17.4 Step 2 단독 실행 검증 결과 (2026-06-21)

| 항목 | 값 / 결과 |
|------|-----------|
| RunId | `daily-aws-paper-20260621-204017` |
| Environment / RunDate / 실행 범위 | `aws-paper` / `2026-06-20` / `-StartStep 2 -EndStep 2` |
| ExpectedKrxRawDate | `2026-06-19` |
| StepCode / Status / Runner | `INTEREST_CRAWLER` / `SUCCESS` / `ECS+SSM` |
| non-GUI ECS taskDefinition / taskId / exitCode | `portfolio-paper-interest-crawler:7` / `78979b5cbb714d0eb94f5946e15a14ce` / `0` |
| Crawler Worker EC2 state | `running` |
| KRX worker SSM commandId | `f9d82fcc-1e26-4710-87c3-1d20483b63ef` |
| Scheduled Task | elapsedSeconds=`111` / sawRunning=True / FinalStatus=`Ready` / FinalLastResult=`0` |
| latest worker log | `C:\portfolio\logs\krx_worker_daily_20260621_114154.log` |
| KRX raw DB validation SSM commandId | `2279c6d7-2da6-4317-9c10-7cc77374b317` |
| `interest_program_raw` 검증 | expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`1` / OK |
| `interest_shortsell_raw` 검증 | expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`349` / OK |
| validation exit code | `0` (stderr empty) |

### 17.5 본 섹션 갱신 원칙

- §12 ~ §16 결정값은 변경하지 않는다.
- §17 은 (a) Step 2 성공 조건 강화, (b) worker stopped 처리 변경(skip → fail-closed), (c) KRX raw validation script 의 Step 2 guard 역할, (d) Step 2 단독 실행 검증 결과만 추가로 명시한다.
- KRX GUI 경로의 자동 로그인(OD-MS-022) / non-GUI ECS Fargate 운영 경로(OD-MS-011) / 표현 보정(OD-MS-020) / wrapper 운영 정책(OD-MS-023) 결정은 그대로 유지된다.
- Step 3 PREPROCESSOR 이후 단계 진행 / EC2 lifecycle 자동화 / Step Functions 혼합 orchestration / View 표시 연동 / worker log centralized collection 은 모두 후속 spec / 후속 phase 책임으로 유지한다.
