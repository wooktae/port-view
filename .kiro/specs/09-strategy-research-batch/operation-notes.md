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
