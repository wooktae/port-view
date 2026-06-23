# Operation Notes — 09-strategy-research-batch

본 문서는 09-strategy-research-batch 진행 중 운영자 / Kiro 가 실제 수행한 작업 결과를 일자별로 누적 기록하는 운영 노트다. 1차 적용 환경은 `aws-paper`, region 은 `ap-northeast-2`, 1차 검증 대상은 Strategy Research MS(`port_strategy_research`)의 AWS Batch 용 Docker image / ECR push 1차 준비다. AWS Batch Compute Environment / Job Queue / Job Definition / SubmitJob 은 본 일자 작업 범위 밖이며 09 spec 후속 phase 책임이다. Strategy Research 의 최종 컴퓨트 1순위는 AWS Batch 유지(OD-MS-008 정합) — ECS Fargate Task 는 2순위 / 본 일자 image smoke 의 대체 수단으로만 활용된다.

## 기록 형식

- 일자별 `## YYYY-MM-DD <요약>` 헤더로 누적한다(02 / 03 / 04 / 06 / 08 spec operation-notes 와 동일).
- 결과는 성공 / 실패 / 보류 / 해당 없음 / 이월만 짧게 적는다. 실패 사례는 1줄 사유 + 1줄 조치 + 결과까지만 요약한다.
- AWS CLI / Console / docker build / ECR push / CloudWatch 로그 / Task event message / heavy backtest 출력 전문은 본 문서에 인용하지 않는다(보안 / 분량 절감).
- IAM Role / Policy / secret 변경은 변경 일자 / 변경자 / 변경 사유 / 변경 전·후 항목 요약 4줄로만 기록한다(JSON 본문 전체 인용 금지).
- 8개 MS 의 소스 / Dockerfile / requirements.txt / adapter 본문 전체 인용은 금지한다. 운영자가 직접 신규 작성 / 수정한 사실만 기록한다.

## 안전 원칙

- 실제 secret value, password, RDS endpoint hostname, RDS password, token, IAM access key id, account-id, 실제 secret ARN, 실제 KMS Key ARN, instance-id, image digest, Batch job ARN 은 본 문서에 평문 기록 금지. 모두 `[REDACTED]` 또는 placeholder(`<account-id>` / `<region>` / `<rds-endpoint>` / `<image-tag>` / `<image-digest>` / `<job-arn>`).
- secret 조회 결과(value)는 기록 금지. 성공 / 실패 + 마지막 갱신 시각만.
- 실제 AWS 리소스 생성 / 수정 / 삭제는 운영자가 직접 수행한다. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행한다.
- 8개 MS(`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) 의 README / AGENTS.md / CHANGELOG / docs / worklog 는 본 spec 작업으로 변경하지 않는다. 운영자가 직접 작성 / 수정한 Dockerfile / requirements.txt / adapter 파일은 운영자 직접 작업이며 본 노트에 사실만 기록한다.
- `secretsmanager:GetSecretValue` 실호출은 운영자만. Kiro 자동 검증은 `secretsmanager:DescribeSecret` metadata 만.
- secret value 가 작업 채팅 / 명령 출력 / 콘솔 캡처 / docker build 로그 / ECR push 로그 / CloudWatch Logs 본문 / 운영자 노트에 평문 노출되지 않도록 후속 작업에서도 동일 원칙 유지(R-DOCS-001 정합).
- full backtest / 장시간 research / report 생성 / RDS DDL/DML / broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 본 일자 검증 범위 밖.

## 2026-06-13 Strategy Research Batch image 1차 준비

운영자가 2026-06-13 직접 수행한 `port_strategy_research` 의 AWS Batch 용 Docker / ECR 1차 준비 결과를 누적 기록한다. 본 일자에 Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행했고, 실제 As-Is 분석 / heavy·light 분류 / Research adapter 신규 생성 / Dockerfile · requirements.txt 신규 생성 / 로컬 docker build / py_compile · import smoke / ECR push 작업은 운영자가 직접 진행했다. 본 일자에는 AWS Batch Compute Environment / Job Queue / Job Definition / SubmitJob / CloudWatch Log Group(Research 용) / Secrets Manager(`/portfolio/paper/rds/research-app`) / IAM Role(execution / job) 모두 생성하지 않았다 — 후속 분리.

### 1. AWS 실행 구조 확인

1. As-Is entrypoint 확인: 완료
   1) 로컬 repository 경로 확인: 완료 (`C:\Workspaces\port_strategy_research`)
   2) 주요 entrypoint 후보 확인: 완료
       - `backtest_research_run.py`
       - `backtest_report_run.py`
       - `run_extended_analysis.py`
       - `block_watch_analysis_run.py`
       - `block_watch_backtest_run.py`
       - `block_exception_buy_backtest_run.py`
       - `block_exception_buy_engine_run.py`
   3) 기존 Daily Batch 안의 Research step 확인: 완료
       - `BACKTEST_RESEARCH` (port-view DailyBatchService 기준 약 45초)
       - `BACKTEST_REPORT` (port-view DailyBatchService 기준 약 2초)
2. heavy / light command 분리: 완료
   1) heavy 후보(본 일자 실행 제외):
       - full backtest
       - 장시간 research
       - report 생성
       - extended analysis
       - block / watch backtest 계열
       - block exception engine backtest 계열
   2) light / smoke 후보(본 일자 사용):
       - `python -m py_compile`
       - import smoke
       - Docker image 내 MS 경계 확인(`/app/port_strategy_decision` 부재 확인)
3. requirements / dependency 확인: 완료
   1) 외부 dependency:
       - `psycopg2-binary`
       - `pandas`
       - `numpy`
   2) 내부 dependency:
       - `port_strategy_research` (본 MS)
       - `port_strategy_common`
       - `port_strategy_decision` 은 최종 image 포함 대상에서 제외(아래 §2 정합)
4. `research_app` DB 환경변수 확인: 완료 (`db_config.py` 기준)
   1) `INTEREST_DB_PASSWORD` 필수 (import 시점 요구)
   2) `INTEREST_DB_HOST`
   3) `INTEREST_DB_PORT`
   4) `INTEREST_DB_NAME`
   5) `INTEREST_DB_USER`
   6) search_path: `research, preprocessor, interest, reference, legacy, public` (OD-DB-006 / 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) §3 정합)
       - 비고: `legacy` 는 search_path 에 포함되지만 USAGE 미부여로 실제 접근 차단(OD-DB-007 정합)

### 2. Research → Decision 직접 런타임 의존 제거

1. 결정 배경: 완료 (OD-MS-018 정합)
   1) `port_strategy_research` 가 기존에 `port_strategy_decision` 의 backtest adapter 3개를 직접 import 하던 구조였음
       - `port_strategy_decision.backtest_market`
       - `port_strategy_decision.backtest_filter`
       - `port_strategy_decision.backtest_sizing`
   2) 해당 3개 파일은 실제 핵심 로직이라기보다 `port_strategy_common` 로직을 호출하는 얇은 adapter 임
   3) Research Batch image 에 Decision MS 전체를 vendoring 하면 MS 경계가 흐려지므로 Research 내부 adapter 로 이관하기로 결정
2. Research 신규 adapter 3개 생성: 완료 (운영자 직접 작성)
   1) `research_backtest_market_adapter.py`
   2) `research_backtest_filter_adapter.py`
   3) `research_backtest_sizing_adapter.py`
   4) 모든 adapter 는 `port_strategy_common` 호출만 수행 — 자체 판단 로직 미포함(R-DATA-008 mitigation 정합)
   5) adapter 본문 전체 인용 0건 — 운영자가 직접 작성한 사실만 기록
3. import 변경 파일: 완료 (운영자 직접 작성)
   1) `backtest_engine.py`
   2) `backtest_buy_logic.py`
   3) `block_exception_buy_engine_run.py`
4. 변경 전 / 후 import 매핑: 완료
   1) 변경 전:
       - `from port_strategy_decision.backtest_filter import filter_buy_candidates`
       - `from port_strategy_decision.backtest_market import evaluate_market`
       - `from port_strategy_decision.backtest_sizing import allocate_positions`
   2) 변경 후:
       - `from port_strategy_research.research_backtest_filter_adapter import filter_buy_candidates`
       - `from port_strategy_research.research_backtest_market_adapter import evaluate_market`
       - `from port_strategy_research.research_backtest_sizing_adapter import allocate_positions`
5. 변경 방식 / 검증: 완료
   1) Python patch script 별도 생성 방식으로 수정 (08 spec 한글 인코딩 보강 결정 정합 — PowerShell `Get-Content` / `Set-Content` 직접 replace 미사용)
   2) 원본 읽기 `encoding="utf-8-sig"` 사용 / 저장 `encoding="utf-8"` 사용
   3) 한글 preview 출력 정상
   4) `python -m py_compile` 통과
   5) `from port_strategy_decision` / `import port_strategy_decision` 잔존 0건 (`grep` 점검 결과)
   6) Docker image 안 `/app/port_strategy_decision` 부재 확인 (§3-3 정합)
6. 후속 분리: 완료
   1) 본 일자 결정은 잠정(OD-MS-018 🟡) — 장기적으로는 adapter 를 `port_strategy_common` 정식 package 로 이동하는 후보 유지(R-DATA-008 mitigation 정합)

### 3. Batch 용 Docker / ECR 정리

1. Dockerfile / requirements.txt 신규 생성: 완료 (운영자 직접 작성)
   1) `port_strategy_research/requirements.txt` 신규
   2) `port_strategy_research/Dockerfile` 신규
   3) Docker build context: `C:\Workspaces` (Strategy Decision Dockerfile 패턴 정합 — 04 spec 2026-06-13 §1 / OD-MS-014 vendoring 패턴 정합)
   4) image 포함 대상:
       - `port_strategy_research`
       - `port_strategy_common`
   5) image 제외 대상:
       - `port_strategy_decision` (OD-MS-018 정합 — 본 일자 결정에 따라 image 안에 포함하지 않음)
   6) 기본 CMD 는 안전한 `py_compile` 기준으로 설정 — 실제 운영 entrypoint 는 후속 phase 의 AWS Batch Job Definition `command` 또는 SubmitJob `--container-overrides` 에서 결정
   7) Dockerfile / requirements.txt 본문 전체 인용 0건 — 운영자가 직접 신규 작성한 사실만 기록(R-DOCS-001 정합)
2. local docker build: 완료
   1) image tag: `portfolio-strategy-research:paper-20260613`
   2) build 성공
   3) dependency install 성공 (`psycopg2-binary` / `pandas` / `numpy`)
3. py_compile / import smoke: 완료
   1) 기본 CMD `py_compile` 실행 성공
   2) container import smoke 통과:
       - `port_strategy_research.db_config`
       - `port_strategy_research.backtest_engine`
       - `port_strategy_research.backtest_buy_logic`
       - `port_strategy_research.block_exception_buy_engine_run`
   3) Docker image MS 경계 확인 성공:
       - `/app/port_strategy_decision` 존재 여부: False
       - 즉 OD-MS-018 의 image 제외 대상 정책 1차 검증 통과
   4) 본 일자 import smoke 는 dummy env 또는 import 시점 환경변수 우회로 통과 — 실제 AWS Batch 실행에서는 Secrets Manager 기반 environment injection 으로 해결(후속 분리)
4. ECR push: 완료
   1) ECR repository `portfolio-strategy-research` 신규 생성
       - 환경 분리 정책: paper / live 환경별 repository 분리하지 않음 (08 / 04 spec 정합)
       - paper / live 구분은 image tag / Job Definition / Secrets path / IAM Job Role / environment variables / RDS 설정 6개 항목에서 처리
   2) tag `paper-20260613` push 완료
   3) tag `paper-latest` push 완료
   4) ECR image 확인: image size 약 90MB
   5) image digest / 실제 ECR URI / account-id / repository ARN 본 노트 / spec 산출물 평문 기록 0건(`<image-digest>` placeholder)

### 4. AWS Batch 실행 구조 (후속)

본 일자 작업 범위 밖 — 모두 후속 분리.

1. Compute Environment 후보 확인: 후속
   1) Fargate compute environment vs EC2 managed compute environment 비교 후속
   2) NAT-free public subnet + assignPublicIp 정책 또는 VPC Endpoint 통한 private subnet 정책 검토 후속(R-NET-002 / R-NET-003 정합)
2. Job Queue 후보 확인: 후속
3. Job Definition 초안 또는 1차 생성: 후속
   1) family / revision / cpu / memory / timeout / job role / execution role / log configuration 후속
4. CloudWatch Log Group 확인 또는 생성: 후속
   1) 후보 이름: `/portfolio/paper/strategy-research`
   2) retention 14일 정책 정합 후속(OD-OBS-002)
5. Secrets Manager `/portfolio/paper/rds/research-app` 확인 또는 생성: 후속
   1) JSON multi-key 방식(`host` / `port` / `dbname` / `username` / `password`) — 08 / 04 spec 정합 후속
6. IAM Role / execution role / job role 확인: 후속
   1) Execution Role 에 research-app secret read inline policy 추가 후속(secret ARN 한정 / wildcard 0건 / OD-SEC-006 정합)
   2) Job Role 신규 생성 후속(`portfolio-paper-research-job-role` 후보)
7. Batch Job timeout / vCPU / memory 기준: 후속
   1) heavy backtest 비용 / 시간 / timeout 기준 별도 결정 후속(R-AUTO-015 mitigation 정합)

### 5. 실제 Batch SubmitJob (후속)

1. 짧은 no-op / import smoke command 가 있으면 후속에서 검토: 후속
   1) job name prefix `smoke` / `full` 구분 정책 후속(R-AUTO-015 mitigation 정합)
2. full backtest / 장시간 research 실행: 본 일자 범위 밖 — 별도 비용 / 시간 / timeout / 운영자 승인 기준 확정 후 실행
3. AWS Batch 기반 SubmitJob 검증: 후속(다음 작업으로 분리)
4. research schema row 생성 검증: 후속

### 6. 1차 검증 완료 기준

1. Strategy Research AWS Batch 본체 생성 전 준비 완료
2. As-Is entrypoint / heavy-light 분류 완료
3. Research → Decision 직접 런타임 의존 제거 완료(OD-MS-018 정합)
4. Research Batch image 는 `port_strategy_research` + `port_strategy_common` 기준으로 정리 완료
5. local Docker build / py_compile / import smoke 완료
6. ECR repository `portfolio-strategy-research` 생성 및 push 완료(`paper-20260613` / `paper-latest`)
7. AWS Batch Compute Environment / Job Queue / Job Definition / SubmitJob 은 후속 분리
8. full backtest / 장시간 research 실행 0건
9. RDS DDL / DML 0건
10. broker / KIS 호출 0건
11. 판단: Strategy Research AWS Batch image 1차 준비 완료. 후속 phase 에서 Batch CE / Queue / JD / SubmitJob 진행 가능.

### 7. 본 일자 범위 밖 / 후속 인계

1. AWS Batch Compute Environment / Job Queue / Job Definition / SubmitJob 1차 생성: 후속
2. CloudWatch Log Group / Secrets Manager / IAM Role 정식 생성: 후속
3. 짧은 no-op / import smoke SubmitJob 검증 — `smoke` job name prefix + light command allowlist 기반: 후속(R-AUTO-015 mitigation 정합)
4. full backtest / 장시간 research / report 생성 — 별도 비용 / 시간 / timeout / 운영자 승인 기준 확정 후 실행: 후속
5. `research_app` schema 별 GRANT 매트릭스 정식 정리 — 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) 후속 갱신(R-DATA-005 정합)
6. Research 내부 adapter 3개의 장기 정리 — `port_strategy_common` 정식 package / adapter 이동(R-DATA-008 mitigation 정합 / OD-MS-005 / OD-MS-014 후속 정합)
7. Step Functions 통합 / EventBridge Scheduler 연계: 후속(04 spec 후속 phase / OD-MS-008 보조 정책 정합)
8. CI/CD OIDC / GitHub Actions 자동 build / push: 후속(07 spec 책임)
9. aws-live cutover: 후속(10 spec 책임)

### 8. 안전 / 보안 점검 결과

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 변경 0건. 운영자가 직접 작성한 `port_strategy_research` Dockerfile / requirements.txt / adapter 3개 / import 변경 3개 파일은 본 노트 §2 / §3 에 사실로만 기록(본문 전체 인용 0건).
2. 실제 secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 ARN / image digest / IAM access key id / job ARN 본 노트 평문 기록 0건.
3. `secretsmanager:GetSecretValue` 실호출 0건 (Secrets Manager `/portfolio/paper/rds/research-app` 자체가 본 일자에 미생성). Kiro 자동 검증 / 본 노트 작성 과정에서 secret value 호출 0건(R-DOCS-001 정합).
4. AWS / Docker / ECR / IAM / Secrets Manager / RDS 작업은 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행.
5. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 0건. 본 일자 검증은 Strategy Research image 1차 준비(local build + py_compile / import smoke + ECR push) 만 다룸.
6. full backtest / 장시간 research / report 생성 / extended analysis / block 계열 backtest 0건. 모두 heavy 분류 후속 분리.
7. RDS DDL / DML 0건. AWS Batch SubmitJob 0건.
8. live 자동 batch / report 생성은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 본 일자는 paper image 1차 준비.
9. 운영 식별자(앞 일자 작업의 instance id / private IP / SSM session id / RDS endpoint hostname) 그대로 재사용. 본 섹션 추가 운영 식별자: image tag(`paper-20260613` / `paper-latest`), ECR repository 이름(`portfolio-strategy-research`), Dockerfile / requirements.txt 파일 경로(`port_strategy_research/Dockerfile` / `port_strategy_research/requirements.txt`), Research adapter 3개 파일 경로(`research_backtest_market_adapter.py` / `research_backtest_filter_adapter.py` / `research_backtest_sizing_adapter.py`) — 모두 운영 식별자로서 사실 기록 / secret 가 아님. image size 약 90MB 도 운영 식별자.

## IAM 변경 기록 템플릿 (필요 시 일자별 추가)

```
## YYYY-MM-DD IAM 변경
- 변경 일자: YYYY-MM-DDTHH:MM:SS+09:00
- 변경자: [운영자 식별자]
- 변경 사유: [한 줄]
- 변경 전 / 후 항목 요약: [추가·삭제 statement 수, Action·Resource 변경 요약 — JSON 본문 전체 인용 금지]
```

## 2026-06-15 Strategy Research AWS Batch 실행 골격 완료

운영자가 2026-06-15 직접 수행한 `port_strategy_research` 의 AWS Batch 실행 골격(Compute Environment / Job Queue / Job Definition / CloudWatch Log Group / Secrets Manager / IAM Role) 신규 생성 + smoke SubmitJob 1차 검증 + Strategy Common 1차 정합성 확인 결과를 누적 기록한다. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행했다. 본 섹션은 같은 spec 의 2026-06-13 §1 ~ §8 후속이며 6/13 후속 항목 중 (a) AWS Batch CE / Queue / JD 1차 생성, (b) CloudWatch Log Group / Secrets Manager / IAM Role 정식 생성, (c) 짧은 no-op / import smoke SubmitJob 검증을 본 일자에 회수했다.

### 1. AWS Batch Compute Environment 생성

1. CE 생성: 완료
   1) name: `portfolio-paper-strategy-research-ce`
   2) type: `MANAGED`
   3) compute resources type: `FARGATE`
   4) maxvCpus: 4
   5) state: `ENABLED`
   6) status: `VALID`
2. NAT-free public subnet + assignPublicIp 정책 정합 확인: 완료(OD-NET-004 정합 / Strategy Decision / Strategy Execution / preprocessor / crawler smoke 와 동일 패턴)
3. Service-linked role(`AWSServiceRoleForBatch`) 사용 확인: 완료(별도 신규 IAM Role 생성 없음)

### 2. Job Queue 생성

1. Queue 생성: 완료
   1) name: `portfolio-paper-strategy-research-queue`
   2) priority: 10
   3) state: `ENABLED`
   4) status: `VALID`
   5) compute environment order: `portfolio-paper-strategy-research-ce` 단일

### 3. Job Definition revision 1 등록

1. JD 등록: 완료
   1) family: `portfolio-paper-strategy-research`
   2) revision: 1
   3) image tag: `paper-latest` (2026-06-13 §3 push 본 그대로 재사용)
   4) platformCapabilities: `FARGATE`
   5) assignPublicIp: `ENABLED`
   6) vCPU: 1
   7) memory: 2048
   8) timeout(`attemptDurationSeconds`): 600초
   9) 기본 `command`: 안전한 `py_compile` 계열 smoke (운영용 entrypoint 는 SubmitJob `--container-overrides` / 후속 revision 에서 결정)
   10) execution role / job role / log configuration: §6 / §4 정합

### 4. CloudWatch Log Group 생성

1. Log Group 생성: 완료
   1) name: `/portfolio/paper/strategy-research`
   2) retention: 14일 (OD-OBS-002 정합)
2. Job Definition `logConfiguration` = `awslogs` 연결: 완료(log driver 본문 전체 인용 0건)
3. log delivery 오류: 0건 (§7 / §8 / 본 일자 후속 §11 모두 정상 수신 확인)

### 5. Secrets Manager research-app 준비

1. secret 신규 생성: 완료
   1) name: `/portfolio/paper/rds/research-app`
   2) JSON multi-key 구조: `host` / `port` / `dbname` / `username` / `password` (08 / 04 spec 정합)
2. secret value 노출 0건 — 본 노트 / 운영 노트 / 작업 채팅 / CloudWatch Logs / docker build / ECR push / Console 캡처 평문 기록 0건(R-DOCS-001 정합)
3. key presence 검증 완료 — `secretsmanager:DescribeSecret` metadata 출력으로 5개 key 존재 1차 확인. `GetSecretValue` 결과값은 운영자만 호출했으며 본 노트에 평문 기록 0건. Kiro 자동 검증 / 본 노트 작성 과정에서 `GetSecretValue` 호출 0건.

### 6. IAM Role 준비

1. Execution Role: 완료
   1) name: `portfolio-paper-research-batch-execution-role`
   2) trust: `ecs-tasks.amazonaws.com` (AWS Batch on Fargate 표준)
   3) attached managed policy: `AmazonECSTaskExecutionRolePolicy`
   4) inline policy: research-app secret read (`secretsmanager:GetSecretValue` / `secretsmanager:DescribeSecret`) — Resource 는 `/portfolio/paper/rds/research-app` secret ARN 단일 한정. wildcard 0건(OD-SEC-006 / 03 §13 정합)
2. Job Role: 완료
   1) name: `portfolio-paper-research-job-role`
   2) trust: `ecs-tasks.amazonaws.com`
   3) 최초 smoke 단계 권한: 최소 권한(secret read 는 Execution Role 책임 / Job Role 자체는 RDS 접속 시 환경변수 기반 + container 내 직접 접근만 사용)
   4) S3 PutObject 권한 추가: 본 일자 §11(BACKTEST_REPORT S3 업로드 보강) 단계에서 별도 추가 — Resource 는 `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` 한정 / Action 은 `s3:PutObject` 한정 / public read 권한 부여 0건
3. IAM 변경 4줄 요약(operation-notes IAM 변경 기록 템플릿 정합):
   - 변경 일자: 2026-06-15(KST)
   - 변경자: 운영자(Kiro 직접 수행 0건)
   - 변경 사유: AWS Batch Strategy Research smoke / full / report 실행에 필요한 Execution / Job Role 신규 생성, research-app secret read inline policy 부여, 본 일자 후반부에 Job Role 에 S3 PutObject 한정 권한 추가
   - 변경 전 / 후 항목 요약: Execution Role 신규 / managed policy 1개 + inline policy 1개 / Job Role 신규 / inline policy 1개(S3 PutObject Resource 한정). 모든 정책 본문 전체 인용 0건. account-id / 실제 IAM Role ARN / 실제 secret ARN 본 노트 평문 기록 0건(`<account-id>` / `<region>` / `<role-arn>` / `<secret-arn>` placeholder).

### 7. py_compile smoke SubmitJob

1. SubmitJob 실행: 완료
   1) jobName: `smoke-strategy-research-import-20260615`
   2) jobId: `81ec3581-0204-43ea-8238-a2a6d22f3f28`
   3) jobDefinition: `portfolio-paper-strategy-research:1`
   4) container override command: `python -m py_compile` 계열 smoke
   5) status: `SUCCEEDED`
   6) exitCode: 0
2. CloudWatch Log Stream 생성 확인 완료. log delivery 오류 0건. Log 본문 전체 인용 0건.
3. image pull 오류 0건 / secret injection 오류 0건(본 SubmitJob 은 secret 미주입 / `py_compile` 단순 import 검증 / RDS 접근 0건).
4. `smoke` job name prefix 정책(R-AUTO-015 mitigation 정합) 1차 적용.

### 8. DB smoke SubmitJob

1. SubmitJob 실행: 완료
   1) jobName: `smoke-strategy-research-db-20260615`
   2) jobId: `5399aa10-0fdd-466b-8079-236d3b7e7e37`
   3) jobDefinition: `portfolio-paper-strategy-research:1`
   4) container override command: DB smoke 검증(import + Secrets Manager 환경변수 주입 기반 `psycopg2.connect` + `current_user` / `current_database` / `current_schema` 출력)
   5) status: `SUCCEEDED`
   6) exitCode: 0
2. DB smoke 결과:
   1) `db smoke ok`
   2) `current_user = research_app`
   3) `current_database = portfolio`
   4) schema 1차 출력 = `research`(첫 search_path 항목)
   5) search_path 정합(`research, preprocessor, interest, reference, legacy, public` / OD-DB-006 / OD-DB-007 정합)
3. 안전 점검:
   1) secret value 노출 0건 (CloudWatch Logs 본문에 password 평문 출력 0건)
   2) image pull 오류 0건
   3) secret injection 오류 0건 (Execution Role 의 inline policy 로 정상 주입)
   4) log delivery 오류 0건
4. RDS DDL / DML 0건 / SELECT 한정 metadata 호출만 사용.

### 9. Strategy Common 1차 정합성 확인

1. 현재 역할 확인: 완료
   1) `port_strategy_common` 은 별도 컴퓨트 없음
   2) Decision / Execution / Research image 에 vendoring 방식으로 포함(OD-MS-014 정합 / 04 spec 2026-06-13 §1 정합)
   3) 전략 config / signal / sizing / result 계약 제공
2. 1차 검증: 완료
   1) `port_strategy_common` import smoke 통과
   2) `port_strategy_decision` 의 common import smoke 통과
   3) `port_strategy_execution` 의 common import smoke 통과
   4) `port_strategy_research` 의 common import smoke 통과
   5) common 주요 모듈 `python -m py_compile` 통과:
       - `common_block_watch.py`
       - `common_buy_decision.py`
       - `common_buy_filter.py`
       - `common_buy_guard.py`
       - `common_buy_sizing.py`
       - `common_context.py`
       - `common_market.py`
       - `common_result.py`
       - `common_sell_decision.py`
       - `common_sell_guard.py`
       - `common_types.py`
       - `common_version.py`
       - `config.py`
       - `utils.py`
       - `__init__.py`
   6) Research adapter 3개(`research_backtest_market_adapter.py` / `research_backtest_filter_adapter.py` / `research_backtest_sizing_adapter.py`) `python -m py_compile` 통과
3. packaging 결정: 완료
   1) 당장은 Docker build context 기반 vendoring 유지(OD-MS-014 / 본 일자 §11 image rebuild 와도 정합)
   2) Docker build context `C:\Workspaces` 기준 유지
   3) git submodule 은 후보로만 유지하고 본 일자에 적용하지 않음
   4) image 마다 포함되는 `port_strategy_common` git-sha / commit 기준 기록은 후속(07 spec / Strategy Common 단계)
4. 표현 / 주의:
   1) 본 항목은 Strategy Common 정식 packaging 완료가 아니다.
   2) 표현은 "Strategy Common 1차 정합성 확인 완료 / 정식 package 관리는 후속" 으로 기록한다.
5. 후속 분리:
   1) wheel / sdist 생성
   2) CodeArtifact 또는 내부 artifact repo 검토
   3) 07 spec(CI/CD) 단계에서 package / version 관리 검토
   4) Research adapter 3개 → `port_strategy_common` 정식 adapter 이동 후보 유지(OD-MS-005 / OD-MS-014 / OD-MS-018 후속 정합)
   5) adapter 이동 시 Decision / Research 결과 비교 테스트 필요(R-DATA-008 mitigation 정합)
   6) R-DATA-008 mitigation 기준 smoke / sample 비교 절차 정식 문서화 필요

### 10. 1차 검증 완료 기준

1. AWS Batch Compute Environment / Job Queue / Job Definition revision 1 모두 ACTIVE / VALID
2. CloudWatch Log Group `/portfolio/paper/strategy-research`(retention 14일) 생성 완료
3. Secrets Manager `/portfolio/paper/rds/research-app`(JSON multi-key) 신규 생성 완료
4. IAM Execution Role / Job Role 신규 생성 완료(secret ARN 한정 / wildcard 0건)
5. py_compile smoke SubmitJob `SUCCEEDED` / exitCode 0
6. DB smoke SubmitJob `SUCCEEDED` / exitCode 0 / `db smoke ok` / `research_app` / `portfolio` / `research` schema / search_path 정합
7. Strategy Common 1차 정합성 확인 완료(import smoke / py_compile / vendoring 유지 결정)
8. AWS / Docker / ECR / IAM / Secrets Manager / CloudWatch / Batch / RDS 모든 작업은 운영자 직접 수행(Kiro 직접 수행 0건)
9. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 0건
10. RDS DDL / DML 0건
11. 판단: Strategy Research AWS Batch 실행 골격 + smoke 1차 검증 완료. 후속 phase 의 BACKTEST_RESEARCH / BACKTEST_REPORT full 실행 진입 가능.

### 11. 본 일자 범위 밖 / 후속 인계

1. View Daily Batch 에서 AWS Batch SubmitJob 연동 — 현재는 운영자 수동 SubmitJob. View `DailyBatchService` 측 step 매핑은 후속(05 spec / 04 spec 후속 phase 정합)
2. Step Functions state machine 정의 / EventBridge Scheduler 정기 트리거 — 04 spec 후속 phase 책임
3. block 계열 research entrypoint(`block_watch_analysis_run` / `block_watch_backtest_run` / `block_exception_buy_backtest_run` / `block_exception_buy_engine_run`) AWS Batch 포팅 여부 판단 — 본 일자 OD-MS-019 결정 정합으로 View Daily Batch 기준 포팅 대상에서 제외, 필요 시 수동 보조 도구로만 분류. heavy 분류 후속.
4. Research 내부 adapter 3개의 `port_strategy_common` 정식 adapter 이동 — R-DATA-008 mitigation 정합 / OD-MS-005 / OD-MS-014 / OD-MS-018 후속 정합
5. `port_strategy_common` 정식 package / version 관리(wheel / CodeArtifact / git submodule) — 07 spec / Strategy Common 단계
6. CI/CD OIDC build / push 자동화 — 07 spec 책임
7. aws-live cutover — 10 spec 책임

### 12. 안전 / 보안 점검 결과

1. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 본 일자 작업으로 인한 변경 0건. 운영자가 직접 수정 / 작성한 `port_strategy_research` Dockerfile / requirements.txt / adapter 3개 / import 변경 3개 파일은 본 spec operation-notes(2026-06-13 §2 / §3 / 본 섹션 §9 / 본 일자 §11) 에 사실로만 기록 / 본문 전체 인용 0건.
2. 실제 secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / IAM access key id 본 노트 평문 기록 0건.
3. account-id / 실제 secret ARN / 실제 IAM Role ARN / image digest 본 노트 평문 기록 0건. placeholder(`<account-id>` / `<region>` / `<secret-arn>` / `<role-arn>` / `<image-digest>`) 사용. jobId(`81ec3581-...` / `5399aa10-...`) / Job Definition family / revision / Compute Environment 이름 / Job Queue 이름 / Log Group 이름 / Secret 이름 / Role 이름 / image tag(`paper-latest`) 는 사용자 명시 정책 정합으로 운영 식별자 사실 기록 — secret 가 아님.
4. AWS Batch / IAM / Secrets Manager / CloudWatch / RDS 작업은 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행.
5. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 0건. RDS DDL / DML 0건.
6. 본 섹션의 SubmitJob 2건은 모두 `smoke` job name prefix(R-AUTO-015 mitigation 정합) / timeout 600초 / vCPU 1 / memory 2048 상한 안에서 실행. heavy 분류 SubmitJob 0건.
7. live 자동 batch / report 생성은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 본 일자는 paper 환경 검증.

### 13. 6/13 후속 항목 중 본 일자 회수 (완료)

본 spec 2026-06-13 §7 / §8 / `_common/followups-overview.md` 2026-06-13 09 spec 후속 메모에 후속으로 분리되었던 항목 중 본 일자 골격 / smoke 단계에서 회수된 항목은 다음과 같다. 6/13 본문은 그대로 보존하고 본 섹션에서 완료로만 회수 기록한다.

1. AWS Batch Compute Environment / Job Queue / Job Definition revision 1 1차 생성: 완료(본 일자 §1 / §2 / §3)
2. CloudWatch Log Group `/portfolio/paper/strategy-research` 생성 + retention 14일: 완료(본 일자 §4)
3. Secrets Manager `/portfolio/paper/rds/research-app` JSON multi-key 신규 생성: 완료(본 일자 §5)
4. IAM Role 보강 — Execution Role research-app secret read inline policy 추가(secret ARN 한정 / wildcard 0건) + Job Role `portfolio-paper-research-job-role` 신규 생성: 완료(본 일자 §6 / S3 PutObject 권한 추가는 본 일자 후반부 §11 BACKTEST_REPORT S3 업로드 보강에서 회수)
5. 짧은 no-op / import smoke SubmitJob 검증 — `smoke` job name prefix + light command allowlist 기반(R-AUTO-015 mitigation 정합): 완료(본 일자 §7 / §8)

위 항목 외(BACKTEST_RESEARCH / BACKTEST_REPORT full 실행 / S3 업로드 보강 / View Daily Batch 연동 / Step Functions / EventBridge / adapter 정식 이동 / common packaging / CI/CD / aws-live cutover) 는 본 spec 의 다른 2026-06-15 섹션 또는 후속 분리 유지.

## 2026-06-15 Strategy Research AWS Batch full / report 실행 검증

운영자가 2026-06-15 본 일자 §1 ~ §13(AWS Batch 실행 골격 + smoke) 후속으로 직접 수행한 BACKTEST_RESEARCH(full backtest + 내부 extended analysis) / BACKTEST_REPORT(4개 리포트 생성) AWS Batch 단건 실행 검증 결과를 누적 기록한다. 본 섹션은 같은 일자 앞 섹션의 smoke 단계 통과 후 진입한 본 phase 검증이며, OD-MS-008(Strategy Research 컴퓨트 1순위 = AWS Batch) / OD-MS-018(Research Batch image dependency boundary) 의 1차 실증 메모로도 동작한다. 본 일자 신규 결정 OD-MS-019(Research AWS Batch 포팅 대상 entrypoint = BACKTEST_RESEARCH + BACKTEST_REPORT 2종 / `run_extended_analysis.py` 수동 보조 도구 분류 / report artifact S3 prefix `strategy-research/reports/`) 가 본 섹션에서 락된다.

### 1. BACKTEST_RESEARCH AWS Batch 실행 검증

1. 실행 결과: 완료
   1) jobDefinition: `portfolio-paper-strategy-research:1`(본 일자 §3 등록 본 그대로 사용)
   2) container override command: `python -m port_strategy_research.backtest_research_run`(또는 동등 entrypoint)
   3) status: `SUCCEEDED`
   4) exitCode: 0
   5) full backtest 실행 성공 — `backtest_research_run` 내부에서 extended analysis 까지 함께 수행
2. backtest run 결과 metadata:
   1) `run_id`: `a39b0b0c-cfe9-474e-8a4a-4ddb33f09567`
   2) `total_return`: 4.55930879
   3) `mdd`: -0.08941942
   4) `sharpe`: 2.65561307
   5) `trade_count`: 308
3. 산출물:
   1) `research.strategy_backtest_run` 1건 / `research.strategy_backtest_daily` / `research.strategy_trade_log` / `research.strategy_backtest_*_analysis` 누적 — 본 노트 row count / 분석 본문 전체 인용 0건. 비밀 / 평문 데이터 노출 0건.
4. 안전 점검:
   1) image pull 오류 0건 / secret injection 오류 0건 / log delivery 오류 0건
   2) RDS DDL 0건. backtest run 결과 / extended analysis insert / upsert 는 `port_strategy_research` repository helper 정상 흐름(2026-06-13 §1 정합)
   3) broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 0건
   4) Job Definition timeout(`attemptDurationSeconds = 600`) 안에서 정상 종료 — `full` job name prefix + 운영자 직접 승인 흐름(R-AUTO-015 mitigation 정합)

### 2. BACKTEST_REPORT AWS Batch 실행 검증

1. 실행 결과: 완료
   1) jobDefinition: `portfolio-paper-strategy-research:1`
   2) container override command: `python -m port_strategy_research.backtest_report_run`(또는 동등 entrypoint) — `BACKTEST_RESEARCH` 의 최신 `run_id` 기준
   3) `REPORT_OUTPUT_DIR` 환경변수: `/tmp/portfolio-reports`(본 일자 §11 BACKTEST_REPORT S3 업로드 보강에서 정식 도입 — Fargate ephemeral 영역 정합 / 컨테이너 종료 시 자동 정리)
   4) status: `SUCCEEDED`
   5) exitCode: 0
   6) 4개 리포트 파일 생성 성공
       - 01_요약 리포트
       - 02_일자별 매매 리포트
       - 03_거래 상세 리포트
       - 04_추천 리포트
2. 본 §2 시점에서는 S3 업로드 미적용(로컬 파일 생성만). S3 업로드 보강 + 검증은 본 일자 §11 ~ §17 으로 분리.
3. 안전 점검:
   1) RDS read 한정. RDS DDL / DML 0건.
   2) 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 본 일자 작업으로 인한 변경 0건.
   3) broker / KIS 호출 0건.

### 3. View Daily Batch 기준 AWS Batch 포팅 대상 확정

1. 결정: 완료(OD-MS-019 신규 결정 정합)
   1) 기존 로컬 View Batch 의 step 중 `BACKTEST_RESEARCH` / `BACKTEST_REPORT` 2종만 Strategy Research AWS Batch 포팅 대상으로 확정
   2) `run_extended_analysis.py` 는 별도 AWS Batch 포팅 대상에서 제외(아래 §4 정합)
   3) `block_watch_analysis_run.py` / `block_watch_backtest_run.py` / `block_exception_buy_backtest_run.py` / `block_exception_buy_engine_run.py` 는 View Daily Batch 실행 대상 아님 — 본 일자 시점에 AWS Batch 포팅 대상에서 제외, 필요 시 운영자 수동 보조 도구로만 분류(09 spec 후속 phase 책임 / R-AUTO-015 정합)
2. 적용 범위:
   1) Step Functions state machine 정의 / EventBridge Scheduler 정기 트리거(04 spec 후속 phase) 진입 시 동일 정책 사용
   2) View Daily Batch 의 ProcessBuilder → AWS Batch SubmitJob 매핑(05 spec 후속 phase) 진입 시 동일 정책 사용

### 4. run_extended_analysis.py 처리 기준 확정

1. 처리 기준: 완료
   1) `run_extended_analysis.py` 는 View Daily Batch 실행 대상 아님
   2) `BACKTEST_RESEARCH`(`backtest_research_run`) 내부에서 extended analysis 가 이미 수행됨(본 일자 §1 정합)
   3) 별도 AWS Batch 포팅 대상에서 제외
   4) 필요 시 기존 `run_id` 의 분석 테이블(`strategy_backtest_*_analysis`) 재생성용 수동 보조 스크립트로 분류
   5) heavy 분류 — 본 일자 SubmitJob 0건 / 후속 SubmitJob 시 `full` job name prefix + 운영자 별도 승인 + Batch Job timeout / vCPU / memory 상한 안에서만 진행(R-AUTO-015 mitigation 정합)

### 5. 1차 검증 완료 기준

1. BACKTEST_RESEARCH full 실행 `SUCCEEDED` / exitCode 0 / `run_id a39b0b0c-...` / total_return / mdd / sharpe / trade_count 출력 확인
2. BACKTEST_REPORT 4개 리포트 생성 `SUCCEEDED` / exitCode 0 / `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` 적용 확인
3. View Daily Batch 기준 AWS Batch 포팅 대상 2종(BACKTEST_RESEARCH / BACKTEST_REPORT) 확정
4. `run_extended_analysis.py` 처리 기준 확정(BACKTEST_RESEARCH 내부 처리 + 수동 보조 도구 분류)
5. block 계열 4종은 AWS Batch 포팅 대상에서 제외 / 후속 수동 보조 도구로만 분류
6. 판단: BACKTEST_RESEARCH / BACKTEST_REPORT 1차 본 phase 검증 완료. 후속은 S3 업로드 보강(§11 ~ §17) 과 View / Step Functions / EventBridge 연동(후속 spec 책임)으로 분리.

### 6. 본 phase 후속 인계

1. View Daily Batch 의 `BACKTEST_RESEARCH` / `BACKTEST_REPORT` step 을 ProcessBuilder 직접 실행에서 AWS Batch SubmitJob 호출로 매핑 — 05 spec 후속 phase 또는 04 spec 후속 phase 책임
2. Step Functions state machine 안에서 BACKTEST_RESEARCH → BACKTEST_REPORT 순서 강제 + EventBridge Scheduler 정기 트리거 — 04 spec 후속 phase
3. block 계열 research entrypoint(`block_watch_*` / `block_exception_buy_*`) 의 별도 분리 운영 모드 — 09 spec 후속 phase 책임 / heavy 분류
4. `port_strategy_common` 정식 package / version 관리 — 07 spec / Strategy Common 단계
5. Research 내부 adapter 3개 → `port_strategy_common` 정식 adapter 이동 — R-DATA-008 mitigation 정합 / OD-MS-005 / OD-MS-014 / OD-MS-018 후속 정합
6. CI/CD OIDC build / push 자동화 — 07 spec 책임
7. aws-live cutover — 10 spec 책임

### 7. 안전 / 보안 점검 결과

1. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 본 phase 작업으로 인한 변경 0건.
2. 실제 secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / IAM access key id 본 노트 평문 기록 0건.
3. account-id / 실제 secret ARN / 실제 IAM Role ARN / image digest 본 노트 평문 기록 0건. 운영 식별자(jobDefinition family / revision / `run_id` / `REPORT_OUTPUT_DIR` 경로 / job name prefix `full` / metric 값) 는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.
4. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 0건. RDS DDL / DML 0건. AWS Batch SubmitJob 은 본 phase 에서 BACKTEST_RESEARCH / BACKTEST_REPORT 단건씩만 운영자 직접 실행 — `full` job name prefix(R-AUTO-015 mitigation 정합).
5. live 자동 batch / report 생성은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 본 일자는 paper 환경 검증.

## 2026-06-15 BACKTEST_REPORT S3 업로드 보강

운영자가 2026-06-15 본 일자 §1 ~ §13(골격) / 본 일자 §1 ~ §7(BACKTEST_RESEARCH / BACKTEST_REPORT 본 phase 검증) 후속으로 직접 수행한 BACKTEST_REPORT S3 업로드 보강 결과를 누적 기록한다. 본 phase 는 OD-MS-019 의 "report artifact S3 prefix `strategy-research/reports/`" 정책 1차 실증을 다룬다. 기존 S3 bucket 재사용 / boto3 dependency 추가 / wrapper S3 업로드 옵션 추가 / Docker rebuild + ECR push / Job Definition revision 3 등록 / S3 업로드 SubmitJob 검증 / S3 파일 4개 존재 확인 순으로 구성된다. 운영자가 직접 작성 / 수정한 `port_strategy_research` 의 requirements.txt 추가분 / report wrapper 변경분은 본 노트에 사실로만 기록 / 본문 전체 인용 0건.

### 11. 기존 S3 bucket 재사용

1. bucket 재사용: 완료
   1) name: `portfolio-paper-migration-yukiever`
   2) Strategy Research 신규 bucket 생성 0건 — 기존 운영 bucket 재사용
   3) 사용 prefix: `strategy-research/reports/`(OD-MS-019 정합)

### 12. TaskRole S3 PutObject 권한 추가

1. IAM 변경: 완료
   1) target Role: `portfolio-paper-research-job-role`(본 일자 §6 Job Role 본문에 권한 추가)
   2) 권한 범위: `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*`
   3) 허용 Action: `s3:PutObject` 한정
   4) public read 권한 추가 0건(`s3:PutObjectAcl` / `public-read` 등 미부여)
   5) 다른 prefix 또는 다른 bucket 접근 0건(Resource wildcard 0건 / 03 §13 / OD-SEC-006 정합)
2. IAM 변경 4줄 요약(IAM 변경 기록 템플릿 정합):
   - 변경 일자: 2026-06-15(KST)
   - 변경자: 운영자(Kiro 직접 수행 0건)
   - 변경 사유: BACKTEST_REPORT 산출물 S3 업로드를 위해 Job Role 에 prefix 한정 PutObject 권한 1건 추가
   - 변경 전 / 후 항목 요약: Job Role inline policy 1개 추가(Resource = `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` / Action = `s3:PutObject` 한정 / public read 0건 / wildcard 0건). 정책 본문 전체 인용 0건. 실제 IAM Role ARN / account-id 본 노트 평문 기록 0건.

### 13. boto3 dependency 추가

1. 변경: 완료(운영자 직접 작성)
   1) `port_strategy_research/requirements.txt` 에 `boto3` 1줄 추가(본문 전체 인용 0건)
   2) Docker image 내부 import smoke 통과 — `python -c "import boto3"` 정상
   3) 기존 dependency(`psycopg2-binary` / `pandas` / `numpy`)는 그대로 유지
2. R-DATA-008 / OD-MS-018 정합 — adapter 자체 판단 로직 미포함 정책 변경 0건. boto3 는 wrapper 의 S3 업로드 단계에서만 사용.

### 14. report wrapper S3 업로드 옵션 추가

1. 변경: 완료(운영자 직접 작성 / 본 노트 본문 전체 인용 0건)
   1) `REPORT_S3_BUCKET` 환경변수 설정 시 → S3 업로드 수행
   2) `REPORT_S3_BUCKET` 미설정 시 → 기존처럼 로컬 파일 생성 후 upload skip(기능 회귀 0건)
   3) `REPORT_S3_PREFIX` 환경변수 기본값 = `strategy-research/reports`
   4) `AWS_BATCH_JOB_ID`(AWS Batch 가 자동 주입하는 환경변수) 기준 하위 경로 분리 — 운영자가 본 일자 §16 / §17 에서 검증한 실제 경로 패턴은 `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/...`
   5) `REPORT_OUTPUT_DIR` 환경변수 기본값 = `/tmp/portfolio-reports` 유지(Fargate ephemeral 영역 정합 / 컨테이너 종료 시 자동 정리)
2. 안전 점검:
   1) 본 wrapper 는 SELECT / 로컬 파일 생성 / S3 PutObject 만 수행 / RDS DDL / DML 0건
   2) public read 부여 0건 / KMS encryption 별도 결정 후속(본 일자에는 bucket 의 default SSE 사용)

### 15. Docker rebuild / ECR push

1. rebuild + push: 완료
   1) imageTag: `paper-20260615-report-s3`
   2) digest: `<image-digest>` placeholder(`sha256:7d28...4c4a7` truncated form / 09 spec 2026-06-13 §3 / R-DOCS-001 정합 — full digest 평문 기록 0건)
   3) pushedAt: 2026-06-15T17:00:10+09:00(KST)
   4) size: 약 106MB(106315984 bytes) — 2026-06-13 §3 의 약 90MB 대비 boto3 추가분 + 캐시 차이 반영
2. Docker build context `C:\Workspaces` / image 포함 = `port_strategy_research` + `port_strategy_common` / image 제외 = `port_strategy_decision`(OD-MS-018 정합) 정책 변경 0건.
3. ECR repository `portfolio-strategy-research` 신규 생성 0건 — 2026-06-13 §3 본 그대로 재사용.

### 16. Job Definition revision 3 등록

1. JD 등록: 완료
   1) family: `portfolio-paper-strategy-research`
   2) revision: 3(본 일자 §3 의 revision 1 + 중간 단계 revision 2 후속)
   3) image: `paper-20260615-report-s3`
   4) TaskRole: `portfolio-paper-research-job-role`(본 일자 §12 권한 추가본 그대로)
   5) Execution Role / log configuration / network mode / vCPU / memory / timeout / assignPublicIp 정책 변경 없음(본 일자 §3 / §4 / §6 정합)
   6) 환경변수: `REPORT_OUTPUT_DIR=/tmp/portfolio-reports`, `REPORT_S3_BUCKET=portfolio-paper-migration-yukiever`, `REPORT_S3_PREFIX=strategy-research/reports`(`AWS_BATCH_JOB_ID` 는 AWS Batch 자동 주입)

### 17. S3 업로드 Batch 실행 검증

1. SubmitJob 실행: 완료
   1) jobName: `strategy-research-backtest-report-s3-20260615`
   2) jobId: `112f5fe4-02f3-4614-a88c-60a9842e1447`
   3) jobDefinition: `portfolio-paper-strategy-research:3`
   4) container override command: BACKTEST_REPORT 실행 + S3 업로드(본 일자 §14 wrapper 옵션 적용)
   5) status: `SUCCEEDED`
   6) exitCode: 0
   7) logStreamName: `strategy-research/default/b18e548d46764cd791028088e1d32a6d`
2. 안전 점검:
   1) image pull 오류 0건 / secret injection 오류 0건 / log delivery 오류 0건
   2) RDS read 한정. RDS DDL / DML 0건.
   3) broker / KIS 호출 0건.
   4) `full` 분류 SubmitJob 이지만 timeout 600초 / vCPU 1 / memory 2048 상한 안에서 정상 종료(R-AUTO-015 mitigation 정합).
   5) AWS Batch automatic retry 0건(OD-SAFE-004 / R-AUTO-001 정합 — Job Definition `attempts = 1`).

### 18. S3 파일 4개 존재 확인

1. S3 객체 4건 확인: 완료
   1) `s3://portfolio-paper-migration-yukiever/strategy-research/reports/20260615/112f5fe4-02f3-4614-a88c-60a9842e1447/01_요약_리포트_20260615_v1.txt`
   2) `s3://portfolio-paper-migration-yukiever/strategy-research/reports/20260615/112f5fe4-02f3-4614-a88c-60a9842e1447/02_일자별_매매_리포트_20260615_v1.txt`
   3) `s3://portfolio-paper-migration-yukiever/strategy-research/reports/20260615/112f5fe4-02f3-4614-a88c-60a9842e1447/03_거래_상세_리포트_20260615_v1.txt`
   4) `s3://portfolio-paper-migration-yukiever/strategy-research/reports/20260615/112f5fe4-02f3-4614-a88c-60a9842e1447/04_추천_리포트_20260615_v1.txt`
2. ACL / 공개 설정: 4건 모두 private 유지(public read 부여 0건). 본문 전체 인용 0건.
3. prefix 정합: `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/...`(OD-MS-019 정합 / 본 일자 §14 wrapper 옵션 결과).

### 19. 1차 검증 완료 기준

1. 기존 S3 bucket `portfolio-paper-migration-yukiever` 재사용 / 신규 bucket 생성 0건
2. Job Role 에 prefix 한정 `s3:PutObject` 권한 추가(wildcard 0건 / public read 0건)
3. boto3 dependency 추가 + import smoke 성공
4. report wrapper 의 `REPORT_S3_BUCKET` / `REPORT_S3_PREFIX` / `AWS_BATCH_JOB_ID` 기반 하위 경로 분리 / `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` 기본값 옵션 추가
5. Docker rebuild(`paper-20260615-report-s3`) + ECR push 완료(image size 약 106MB / image digest placeholder 사용)
6. Job Definition revision 3 등록(`portfolio-paper-strategy-research:3`, image `paper-20260615-report-s3`, TaskRole `portfolio-paper-research-job-role`)
7. SubmitJob `strategy-research-backtest-report-s3-20260615`(jobId `112f5fe4-...`) `SUCCEEDED` / exitCode 0 / logStreamName 확인
8. S3 객체 4건 존재 확인(prefix `strategy-research/reports/20260615/112f5fe4-.../` / 파일명 `01_요약 리포트` ~ `04_추천 리포트`)
9. RDS DDL / DML 0건 / broker / KIS 호출 0건 / 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 변경 0건
10. 판단: BACKTEST_REPORT S3 업로드 보강 1차 검증 완료. 후속은 View / Step Functions / EventBridge 연동 + S3 lifecycle 정책 / KMS encryption 결정 + adapter 정식 이동 + common packaging + CI/CD + aws-live cutover 로 분리.

### 20. 본 phase 후속 인계 (S3 업로드 보강 단독)

1. S3 lifecycle 정책 — 운영자 결정 후 적용(report 누적에 따른 storage 비용 누증 / R-COST-003 정합). 본 일자에는 lifecycle 미설정.
2. S3 KMS encryption — 현재는 bucket default SSE 사용 / CMK 적용은 후속 spec(06 / 09 후속 phase) 결정.
3. View Daily Batch 의 `BACKTEST_REPORT` step 의 ProcessBuilder → AWS Batch SubmitJob 호출 매핑 + S3 prefix 일관성 전달 — 05 / 04 spec 후속 phase 책임
4. Step Functions state machine 안에서 `BACKTEST_RESEARCH` → `BACKTEST_REPORT` 순서 강제 + EventBridge Scheduler 정기 트리거 — 04 spec 후속 phase
5. heavy job(`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) 의 수동 보조 도구 운영 절차 명문화 — 09 spec 후속 phase / R-AUTO-015 mitigation 정합

### 21. 안전 / 보안 점검 결과 (S3 업로드 보강 단독)

1. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 본 phase 작업으로 인한 변경 0건. 운영자가 직접 작성 / 수정한 `port_strategy_research` requirements.txt(boto3 1줄 추가) / report wrapper 변경분 / Dockerfile 변경분(있는 경우) 은 본 노트 §13 / §14 / §15 에 사실로만 기록 / 본문 전체 인용 0건.
2. 실제 secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / IAM access key id 본 노트 평문 기록 0건.
3. account-id / 실제 secret ARN / 실제 IAM Role ARN / 실제 ECR URI / image digest(full sha256) 본 노트 평문 기록 0건. 운영 식별자(image tag `paper-20260615-report-s3`, image size 약 106MB, pushedAt 2026-06-15T17:00:10+09:00, jobName `strategy-research-backtest-report-s3-20260615`, jobId `112f5fe4-02f3-4614-a88c-60a9842e1447`, logStreamName `strategy-research/default/b18e548d46764cd791028088e1d32a6d`, S3 bucket `portfolio-paper-migration-yukiever`, S3 prefix `strategy-research/reports/20260615/112f5fe4-...`, file name `01_요약 리포트` ~ `04_추천 리포트`) 는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.
4. AWS / IAM / Secrets Manager / S3 / Docker / ECR / Batch / RDS / GRANT 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건. CloudWatch Logs 본문에 secret value 평문 출력 0건. S3 객체 본문 전체 인용 0건.
5. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 0건. RDS DDL / DML 0건. AWS Batch SubmitJob 은 본 phase 에서 BACKTEST_REPORT S3 업로드 검증용 단건 — `full` job name prefix(R-AUTO-015 mitigation 정합).
6. public read 부여 0건 / 잘못된 bucket 업로드 0건 / 잘못된 prefix 업로드 0건. S3 prefix `strategy-research/reports/` 한정 정책 1차 실증(R-COST-003 / OD-MS-019 정합).
7. live 자동 batch / report 생성은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지.

### 22. 6/13 후속 항목 중 본 phase 회수 (완료)

본 spec 2026-06-13 §7 / `_common/followups-overview.md` 2026-06-13 09 spec 후속 메모에 후속으로 분리되었던 항목 중 본 phase(S3 업로드 보강) 에서 회수된 항목은 다음과 같다. 6/13 본문은 그대로 보존하고 본 섹션에서 완료로만 회수 기록한다.

1. full backtest / 장시간 research / report 생성 — 별도 비용 / 시간 / timeout / 운영자 승인 기준 확정 후 실행: 완료(본 일자 §1 / §2 BACKTEST_RESEARCH + BACKTEST_REPORT 단건 검증)
2. BACKTEST_REPORT 의 `REPORT_OUTPUT_DIR` 분리 / S3 업로드 옵션 / S3 prefix 정합: 완료(본 일자 §14 ~ §18)
3. heavy 분류 후보(`backtest_research_run` / `backtest_report_run` / `run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) 별 timeout / vCPU / memory 정식 결정 — 본 phase 에서 BACKTEST_RESEARCH + BACKTEST_REPORT 만 vCPU 1 / memory 2048 / timeout 600초 1차 확정. 나머지(`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`)는 OD-MS-019 결정 정합으로 AWS Batch 포팅 대상에서 제외 / 수동 보조 도구로 분류 / 별도 후속 phase 책임 유지.

위 항목 외(View Daily Batch SubmitJob 연동 / Step Functions / EventBridge / adapter 정식 이동 / common packaging / CI/CD / aws-live cutover) 는 본 spec 의 후속 분리 유지.


## 2026-06-16 Strategy Research AWS Batch Backend dry-run 재검증

운영자가 2026-06-16 직접 수행한 (a) BACKTEST_RESEARCH AWS Batch 단건 재실행, (b) BACKTEST_REPORT 정식 Job Definition `portfolio-paper-strategy-report` 신규 등록 + rev1 ~ rev3 교정 진행 + 최종 rev3 단건 실행 성공 + S3 업로드 4건 존재 확인 결과를 누적 기록한다. 본 섹션은 같은 spec 의 2026-06-15 BACKTEST_RESEARCH / BACKTEST_REPORT 본 phase 검증 + S3 업로드 보강(§1 ~ §22) 의 후속이며, 08 spec 의 2026-06-16 Crawler 데이터 미수집 해결 + KRX EC2 자동화 성공으로 raw 최신성이 회복된 입력 데이터 위에서 backend AWS E2E dry-run 의 safe subset(Research → Decision)을 재가동하기 위한 1차 검증이다. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행했고, 실제 AWS Batch / IAM / S3 / Docker / ECR 작업은 운영자가 직접 수행했다. 실제 BUY / SELL 주문 / `--execute` 주문 전송 / fill · position sync 자동 재시도 0건 / aws-live 작업 0건(OD-SAFE-001 ~ OD-SAFE-004 / OD-MS-021 정합).

### 1. BACKTEST_RESEARCH AWS Batch 재실행

1. SubmitJob 실행: 완료
   1) Job Definition: `portfolio-paper-strategy-research:5`
   2) cluster: `portfolio-paper-strategy-research-ce` 단일 / queue `portfolio-paper-strategy-research-queue`
   3) 실행 status: `SUCCEEDED`
   4) container exitCode: `0`
   5) Batch automatic retry 0건(`attempts = 1` / OD-SAFE-004 / R-AUTO-001 / R-AUTO-015 정합)
2. 최신 run 결과 확인: 완료
   1) `research.strategy_backtest_run` 최신 row run_id: `439d78e7-41fd-4bb7-b455-18564ddff758`
   2) backtest_end_date: `2026-06-15` 확인
   3) `strategy_trade_log` row_count: `310`
   4) `strategy_backtest_daily` row_count: `822`
   5) `strategy_backtest_daily_position` row_count: `2375`
3. 성과 지표: 완료
   1) total_return: `4.66534417`
   2) mdd: `-0.08941942`
   3) sharpe: `2.68071466`
   4) trade_count: `310`
4. 안전 점검: 완료
   1) RDS DDL 0건 / DML 은 backtest run 의 정상 흐름 한정
   2) broker / KIS 호출 0건 / Daily Batch entrypoint 직접 호출 0건
   3) heavy 분류(`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) SubmitJob 0건(OD-MS-019 정합)
   4) image digest / job ARN / account-id 본 노트 평문 기록 0건(`<image-digest>` / `<job-arn>` / `<account-id>` placeholder)

### 2. BACKTEST_REPORT 정식 Job Definition 진행 흐름 (rev1 ~ rev3)

1. rev1 등록: 완료(교정 이력)
   1) family: `portfolio-paper-strategy-report:1`
   2) command: `python -m port_strategy_research.backtest_report_run`(직접 실행 구조)
   3) 결과: local report 4개 생성 성공 / S3 업로드 wrapper 미수행
   4) 판단: 정식 BACKTEST_REPORT 운영 경로로는 부적합 — wrapper 호출이 누락되어 S3 업로드 단계로 이어지지 않음
2. rev2 등록: 완료(교정 이력)
   1) family: `portfolio-paper-strategy-report:2`
   2) command: `python aws_batch_backtest_report_wrapper.py`(직접 파일 실행 구조)
   3) 결과: 컨테이너 안 `/app/aws_batch_backtest_report_wrapper.py` 경로 부재로 실패
   4) 원인: 현재 image 의 module 배치(`port_strategy_research/aws_batch_backtest_report_wrapper.py`) 와 직접 파일 경로 호출 방식이 정합하지 않음
   5) 조치: rev3 에서 module 호출 방식(`python -m`)으로 교정
3. rev3 등록: 완료(최종 운영 경로)
   1) family: `portfolio-paper-strategy-report:3`
   2) command: `python -m port_strategy_research.aws_batch_backtest_report_wrapper`
   3) image tag: `paper-20260615-report-s3`(2026-06-15 §15 보강분 그대로 재사용)
   4) 환경변수
       - `REPORT_OUTPUT_DIR=/tmp/portfolio-reports`
       - `REPORT_S3_BUCKET=portfolio-paper-migration-yukiever`
       - `REPORT_S3_PREFIX=strategy-research/reports`
   5) Job Role: `portfolio-paper-research-job-role` 의 S3 PutObject Resource = `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` 한정(2026-06-15 §12 보강분 재사용 / public read 0건 / wildcard 0건)
4. 최종 결정: BACKTEST_REPORT 정식 Job Definition 은 `portfolio-paper-strategy-report:3` 로 확정. rev1 / rev2 는 closed 성격의 교정 이력으로만 기록(R-AUTO-015 mitigation·detection 보강).

### 3. BACKTEST_REPORT rev3 SubmitJob 실행 검증

1. SubmitJob 실행: 완료
   1) jobName: `portfolio-paper-backtest-report-s3-20260616-rev3`
   2) Job Definition: `portfolio-paper-strategy-report:3`
   3) status: `SUCCEEDED`
   4) container exitCode: `0`
   5) Batch automatic retry 0건(`attempts = 1`)
2. 입력 run_id: `439d78e7-41fd-4bb7-b455-18564ddff758`(§1 정합)
3. local report 4개 생성 확인: 완료
   1) `01_요약_리포트_20260616_v1.txt`
   2) `02_일자별_매매_리포트_20260616_v1.txt`
   3) `03_거래_상세_리포트_20260616_v1.txt`
   4) `04_추천_리포트_20260616_v1.txt`
4. wrapper S3 upload completed count: `4` 확인

### 4. S3 업로드 결과 확인

1. S3 prefix: 완료
   1) `strategy-research/reports/20260616/67522706-9b5f-4770-a312-ceb1987c4655/`
   2) bucket: `portfolio-paper-migration-yukiever`(기존 재사용 / OD-MS-019 정합)
2. S3 객체 4건 존재 확인: 완료
   1) `01_요약_리포트_20260616_v1.txt` — 7,258 bytes
   2) `02_일자별_매매_리포트_20260616_v1.txt` — 552,540 bytes
   3) `03_거래_상세_리포트_20260616_v1.txt` — 329,088 bytes
   4) `04_추천_리포트_20260616_v1.txt` — 11,847 bytes
3. private 유지 / public read 부여 0건 / KMS encryption 별도 결정은 후속(R-COST-003 정합)
4. 실제 account-id / 실제 IAM Role ARN / 실제 secret ARN / 실제 S3 object ARN / image digest full sha256 본 노트 평문 기록 0건(`<account-id>` / `<role-arn>` / `<secret-arn>` / `<image-digest>` placeholder)

### 5. 1차 검증 완료 기준

1. BACKTEST_RESEARCH AWS Batch 실행 검증: 완료(2026-06-16 §1 정합 / `portfolio-paper-strategy-research:5` `SUCCEEDED` / run_id `439d78e7-...`)
2. BACKTEST_REPORT AWS Batch 실행 검증: 완료(2026-06-16 §2 / §3 정합 / 최종 Job Definition `portfolio-paper-strategy-report:3` / `SUCCEEDED` / exitCode 0)
3. BACKTEST_REPORT S3 업로드 보강 1차 실증: 완료(2026-06-16 §4 정합 / S3 prefix `strategy-research/reports/20260616/67522706-9b5f-4770-a312-ceb1987c4655/` 안 4개 객체 존재)
4. BACKTEST_REPORT 운영 경로 교정: 완료(rev1 local-only / rev2 경로 부재 / rev3 module 호출 방식으로 최종 확정)
5. heavy 분류 SubmitJob 0건 / Batch automatic retry 0건 / RDS DDL 0건 / broker · KIS 호출 0건 / aws-live 작업 0건
6. 판단: Strategy Research AWS Batch 정식 BACKTEST_REPORT Job Definition 확정 + safe subset 재실행 검증 완료

### 6. 후속 인계

1. View Daily Batch 의 `BACKTEST_RESEARCH` / `BACKTEST_REPORT` step 을 ProcessBuilder 직접 실행에서 AWS Batch SubmitJob 호출로 매핑 — 05 spec / 04 spec 후속 phase 책임
2. Step Functions state machine 정의(BACKTEST_RESEARCH → BACKTEST_REPORT 순서 강제 + 자동 재시도 금지) + EventBridge Scheduler 정기 트리거 — 04 spec 후속 phase 책임
3. S3 lifecycle 정책 + KMS encryption 결정 — R-COST-003 정합 / 06 spec 후속 phase
4. `portfolio-paper-strategy-report:1` / `portfolio-paper-strategy-report:2` 정리(운영 사용 금지 / Inactive 처리 또는 deregister)는 후속 운영자 결정으로 분리
5. heavy 분류 entrypoint(`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) 수동 보조 도구 운영 절차 명문화 — 09 spec 후속 phase / R-AUTO-015 mitigation 정합
6. aws-live cutover — 10 spec 책임

### 7. 안전 / 보안 점검 결과

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog 변경 0건. 운영자가 직접 작성 / 수정한 `port_strategy_research/aws_batch_backtest_report_wrapper.py` / Dockerfile / requirements.txt 변경분은 본 노트 §2 / §3 에 사실로만 기록(본문 전체 인용 0건).
2. 실제 secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 secret ARN / 실제 IAM Role ARN / image digest full sha256 / IAM access key id / job ARN 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
3. AWS Batch / IAM / S3 / Docker / ECR / RDS 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건.
4. CloudWatch Logs 본문 / Batch console 응답 본문 / Docker build 로그 / S3 업로드 client 로그 본 노트 평문 인용 0건. 사실(jobName / status / exitCode / row count / 파일명 / 객체 size / S3 prefix) 만 기록.
5. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 직접 호출 0건. 신규 BUY / SELL / 취소 / 정정 / `--execute` 0건. fill / position sync 자동 재시도 0건.
6. RDS DDL 0건 / DML 은 BACKTEST_RESEARCH 의 정상 backtest run 흐름 + BACKTEST_REPORT 의 read-only 조회 한정.
7. live 자동 batch / report 생성은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 본 일자는 paper 환경 한정.

### 8. Task 완료 처리 (본 spec)

본 spec 은 별도 tasks.md 가 없으므로 본 noted task 단위 완료 처리는 본 섹션에서 직접 기록한다.

1. BACKTEST_RESEARCH AWS Batch 실행 검증: 완료(§1 / `portfolio-paper-strategy-research:5` `SUCCEEDED` / run_id `439d78e7-...`)
2. BACKTEST_REPORT AWS Batch 실행 검증: 완료(§2 / §3 / 최종 `portfolio-paper-strategy-report:3` `SUCCEEDED`)
3. BACKTEST_REPORT S3 업로드 보강: 완료(§4 / S3 객체 4건 존재 확인)
4. BACKTEST_REPORT 정식 Job Definition 운영 경로 교정: 완료(§2 / rev1 local-only / rev2 경로 부재 / rev3 module 호출 방식으로 확정)
5. View Daily Batch 의 `BACKTEST_RESEARCH` / `BACKTEST_REPORT` step → AWS Batch SubmitJob 매핑: 후속(§6 / 05 spec / 04 spec 후속 phase)
6. Step Functions state machine + EventBridge Scheduler: 후속(§6 / 04 spec 후속 phase)
7. S3 lifecycle / KMS encryption: 후속(§6 / R-COST-003 / 06 spec)
8. heavy 분류 운영 절차 명문화: 후속(§6 / R-AUTO-015)
9. aws-live cutover: 후속(§6 / 10 spec)


## 2026-06-17 Daily AWS 17-step E2E 완료 (Strategy Research)

운영자가 같은 일자 첫 번째 세션(MarketConnector 조회성 dry-run 재검증) 후속으로 직접 수행한 Daily AWS 17-step E2E 흐름이 본 일자에 끝까지 연결됐다. 본 spec 범위에 해당하는 step 은 4번 `BACKTEST_RESEARCH` / 5번 `BACKTEST_REPORT` 2개. 자세한 17 step 전체 진행 상태는 03 / 04 / 08 spec operation-notes 의 2026-06-17 섹션 참조. Kiro 는 문서 작성 / 절차 정리만 수행. 실제 AWS Batch SubmitJob / IAM / RDS / S3 작업은 운영자 직접 진행. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건. heavy 분류(`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) SubmitJob 0건 유지(OD-MS-019 / R-AUTO-015 정합).

### 1. Step 4 `BACKTEST_RESEARCH`

1. AWS Batch SubmitJob 실행: 완료
   1) Job Definition: `portfolio-paper-strategy-research:5`(2026-06-16 §1 정합 / 본 일자 동일 revision 재사용)
   2) launch type: FARGATE / awsvpc / image tag `paper-20260616` 또는 그 시점의 최신 paper image(운영자 직접 결정)
2. 실행 결과: 성공
   1) job status: `SUCCEEDED`
   2) container exitCode: `0`
   3) Batch automatic retry 0건(OD-SAFE-004 / R-AUTO-001 정합)
3. 결과 확인: 완료
   1) latest result date: `2026-06-16`
   2) Sharpe Ratio: `2.68`
   3) `research.strategy_backtest_daily` 최신성: `2026-06-16` 확인
   4) `research.strategy_backtest_daily_position` 최신성: `2026-06-16` 확인
4. 안전 점검: 완료
   1) extended analysis 는 BACKTEST_RESEARCH 내부에서 이미 수행(OD-MS-019 정합) — 별도 SubmitJob 없음
   2) heavy 분류 SubmitJob 0건 유지
   3) RDS DDL 0건 / broker · KIS 호출 0건 / `--execute` 호출 0건

### 2. Step 5 `BACKTEST_REPORT`

1. AWS Batch SubmitJob 실행: 완료
   1) Job Definition: `portfolio-paper-strategy-report:3`(rev3 module 호출 방식 / 2026-06-16 §2 / §3 정합)
   2) image tag `paper-20260615-report-s3` 또는 운영자 직접 갱신한 paper image
   3) env: `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` / `REPORT_S3_BUCKET=portfolio-paper-migration-yukiever` / `REPORT_S3_PREFIX=strategy-research/reports`
2. 실행 결과: 성공
   1) job status: `SUCCEEDED`
   2) container exitCode: `0`
3. S3 객체 4건 생성 확인: 완료
   1) prefix: `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/` 정합(OD-MS-019 정합)
   2) `01_요약_리포트` / `02_일자별_매매_리포트` / `03_거래_상세_리포트` / `04_추천_리포트` 4종 모두 존재 확인
   3) private 유지 / public read 부여 0건 / wildcard 0건(R-COST-003 mitigation 정합)
4. report upload prefix / object count 검증: 완료
   1) Job Role 의 `s3:PutObject` Resource 가 `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` 한정 정책 정합(OD-MS-019 / R-COST-003 정합)
   2) 다른 prefix / 다른 bucket 으로의 PutObject 패턴 0건

### 3. 안전 / 보안 점검 결과

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 변경 0건. 운영자가 직접 갱신한 `port_strategy_research/aws_batch_backtest_report_wrapper.py` / 관련 Dockerfile / requirements.txt 가 있다면 사실로만 기록(본문 전체 인용 0건).
2. 실제 secret value / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 secret ARN / 실제 IAM Role ARN / image digest full sha256 / IAM access key id / job ARN / task ARN / S3 bucket ARN 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder. 운영 식별자(Job Definition family `portfolio-paper-strategy-research` / `portfolio-paper-strategy-report` / S3 prefix `strategy-research/reports/` / Sharpe Ratio `2.68` / latest result date `2026-06-16` / 4개 리포트 파일명) 만 사실 기록.
3. AWS Batch / ECS / IAM / S3 / Docker / ECR / RDS 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건. CloudWatch Logs 본문 / Batch console 응답 / S3 client 로그 본 노트 평문 인용 0건.
4. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 직접 호출 0건. heavy 분류(`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) SubmitJob 0건 유지(R-AUTO-015 정합).
5. RDS DDL 0건. DML 은 BACKTEST_RESEARCH 정상 backtest run 흐름 한정 — `research.strategy_backtest_run` / `research.strategy_backtest_daily` / `research.strategy_backtest_daily_position` / `research.strategy_trade_log` / `research.strategy_backtest_*_analysis` 정상 insert / upsert.
6. live 자동 batch / report 생성은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건. S3 lifecycle 정책 / KMS encryption 결정은 후속(R-COST-003 정합).
7. Daily AWS 17-step E2E paper 1차 통과로 backend AWS E2E 의 4번 / 5번 step 정합 — OD-MS-008 / OD-MS-019 mitigation 1차 실증 / Status 기존 값 그대로 유지.


## 2026-06-22 Daily AWS Paper Step 4 / Step 5 재검증 (Strategy Research)

운영자가 2026-06-22 직접 수행한 Daily AWS Paper Wrapper 1 ~ 17 두 번째 실 운영 실행 중 본 spec 책임 step(Step 4 / Step 5) 결과를 누적 기록한다. 환경 `aws-paper` / region `ap-northeast-2` / RunDate `2026-06-22`. 본 spec 범위에 해당하는 step 은 4번 `BACKTEST_RESEARCH` / 5번 `BACKTEST_REPORT` 2개. 자세한 17 step 전체 진행 상태 / Daily wrapper 1 ~ 17 두 번째 실 완주 결과는 03 / 04 / 08 spec operation-notes 2026-06-22 / [`../_common/operator-decisions.md`](../_common/operator-decisions.md) Change Log 2026-06-22 참조. Kiro 는 문서 작성 / 절차 정리만 수행. 실제 AWS Batch SubmitJob / IAM / RDS / S3 / CloudWatch 작업은 운영자 직접 수행. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건. heavy 분류(`run_extended_analysis` / `block_watch_*` / `block_exception_buy_*`) SubmitJob 0건 유지(OD-MS-019 / R-AUTO-015 정합).

### 1. Step 4 `BACKTEST_RESEARCH`

1. AWS Batch SubmitJob: 완료
   1) Job Queue `portfolio-paper-strategy-research-queue` / Job Definition `portfolio-paper-strategy-research:5`(이전 일자 정합 / revision 변경 없음)
   2) Job Status `SUCCEEDED` / exit code 0 — 본 노트 jobId / job ARN 평문 기록 0건 / placeholder 처리
2. 결과 정합: 확인
   1) `research.strategy_backtest_run` 신규 row — RunDate 정합
   2) `research.strategy_backtest_daily` / `research.strategy_backtest_daily_position` 정상 누적
3. heavy 분류 SubmitJob 0건 유지(R-AUTO-015 정합)

### 2. Step 5 `BACKTEST_REPORT`

1. AWS Batch SubmitJob: 완료
   1) Job Queue 동일 / Job Definition `portfolio-paper-strategy-report:3`(이전 일자 정합 / revision 변경 없음)
   2) Job Status `SUCCEEDED` / exit code 0
2. 결과 정합: 확인
   1) S3 prefix `s3://portfolio-paper-migration-yukiever/strategy-research/reports/20260622/<aws-batch-job-id>/`(jobId / job ARN placeholder 처리) 안 4개 리포트 객체 정합
   2) public read 0건 / Job Role inline policy Resource 한정 정책 회귀 0건(OD-MS-019 정합 / R-COST-003 mitigation 정합)

### 3. Daily wrapper 1 ~ 17 두 번째 실 완주 정합

1. Daily wrapper 1 ~ 17 두 번째 실 완주 결과는 본 spec 범위 밖이며, 본 일자 Step 4 / Step 5 는 첫 번째 완주(2026-06-18) 와 동일한 흐름으로 회귀 0건 통과.
2. Strategy Research AWS Batch 1순위 결정(OD-MS-008) / Research Batch image dependency boundary(OD-MS-018) / 포팅 대상 + S3 prefix(OD-MS-019) 본문 결정값은 변경 없음.
3. heavy 분류 entrypoint 가 SubmitJob 으로 흘러들어가지 않도록 차단 정책 회귀 0건 / Compute Environment vCPU / Job Queue depth / Cost Explorer 비용 spike 0건.

### 4. 결정 / 리스크 변경 요약

1. 신규 결정: 0건. 신규 리스크: 0건.
2. 본문 변경 없는 결정: 1차 실증 메모 보강
   1) OD-MS-008 — Research AWS Batch 1순위가 Daily run 실 회귀 0건으로 1차 실증
   2) OD-MS-019 — `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종 한정 + S3 prefix `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/` 회귀 0건 / heavy 분류 SubmitJob 0건 유지
3. 본문 변경 없는 리스크: 보강 메모
   1) R-AUTO-015 — heavy 분류 SubmitJob 차단 정책 회귀 0건 / Status `Mitigated` 유지
   2) R-COST-003 — S3 report 누적 비용 / lifecycle 미설정 위험은 본 일자에도 변경 없음 / 후속 lifecycle 결정 후속 유지

### 5. 안전 / 보안 점검 결과 (2026-06-22)

1. 본 spec 범위에서 broker / KIS 호출 0건. `--execute` 호출 0건. fill · position sync 자동 재시도 0건. aws-live 작업 0건.
2. RDS DDL 0건. DML 은 `research.strategy_backtest_run` / `research.strategy_backtest_daily` / `research.strategy_backtest_daily_position` / `research.strategy_backtest_*_analysis` 정상 누적 한정. S3 PutObject 는 Job Role inline policy Resource 한정(OD-MS-019 정합).
3. 실제 secret value / RDS password / RDS endpoint hostname / account-id / 실제 secret ARN / 실제 IAM Role ARN / image digest full sha256 / IAM access key id / jobId / job ARN 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
4. AWS / Batch / IAM / Secrets Manager / RDS / S3 / CloudWatch 작업은 모두 운영자 직접 수행 — Kiro 는 문서 작성 / 절차 정리만 수행. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건. CloudWatch Logs 본문 / Batch job describe 본문 / S3 object 본문 평문 인용 0건.
5. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역). 운영 식별자(Job Queue / Job Definition family · revision / Log Group 이름 / S3 prefix 패턴 / RunDate `2026-06-22`) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.
