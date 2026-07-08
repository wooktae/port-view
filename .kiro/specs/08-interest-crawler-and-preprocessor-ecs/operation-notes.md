# Operation Notes — 08-interest-crawler-and-preprocessor-ecs

본 문서는 08-interest-crawler-and-preprocessor-ecs 진행 중 운영자 / Kiro 가 실제 수행한 작업 결과를 일자별로 누적 기록하는 운영 노트다. 1차 적용 환경은 `aws-paper`, region 은 `ap-northeast-2`, 1차 검증 대상은 Preprocessor MS(`port-interest-preprocessor`).

## Timeline Dashboard

본 spec 의 운영자 실행 이력 요약. 자세한 내용은 각 일자 섹션 참조.

| 일자 | 핵심 결과 | 주요 결정 / mitigation |
|---|---|---|
| 2026-06-10 | ECR repo 2개 · Dockerfile · 로컬 빌드 · ECR push · ECS Cluster · Task Role · Log Group · Preprocessor RunTask exitCode 0 | OD-MS-011(hybrid) 전제 확정 |
| 2026-06-12 | KRX GUI 수집 = Windows EC2 worker 분리(RDP + wrapper 수동 실행 1차 통과) | KRX Secret 신규 · 다운로드 경로 junction · 한글 literal 정책 |
| 2026-06-13 | SSM RunCommand → `schtasks /Run` → Scheduled Task 자동화 진입점 + ECS crawler rev6 Selenium Chrome smoke 통과 | OD-MS-015(SSM direct 실행 부적합) · hybrid 1차 완성 판단 |
| 2026-06-15 | Backend AWS E2E dry-run 1차 · CONNECTOR_BALANCE 통과 · Preprocessor ECS 실행 성공(데이터 최신성 제약) · stale raw data 이슈 발견 | OD-MS-020(표현 보정) · OD-MS-021(dry-run 안전 기준) · R-DATA-009 / R-DATA-010 신규 |
| 2026-06-16 | non-GUI ECS rev7(`paper-20260616-nongui`) 신규 · RunTask exitCode 0 · raw 8종 최신성 회복 · KRX GUI Autologon + Administrator interactive session + Scheduled Task 자동화 실증 | OD-MS-022 신규 · R-SEC-009 · R-AUTO-017 신규 · hybrid 구조 완료 |
| 2026-06-17 | Daily AWS 17-step E2E 완료(Step 2 crawler + Step 3 preprocessor 통과 · feature 최신성 `2026-06-16`) | R-DATA-009 / R-DATA-010 mitigation 1차 실증 |
| 2026-06-20 | Daily wrapper 최종 점검 · 6/18 중복 실행 시도 안전 중단 · Step 12 미실행 · 6/19 KRX raw 최신성 복구 확인 | R-AUTO-007 식별(Scheduled Task trigger 성공 → Step 2 SUCCESS 위험) |
| 2026-06-21 | Step 2 성공판정 강화 · `interest_krx_raw_validate_daily.py` 신규 · DB validation SSM step 연동 · worker stopped fail-closed | OD-MS-026 신규 · R-AUTO-020 신규 mitigation |
| 2026-06-22 | Daily wrapper 2회차 실 운영 실행 · Step 2 / Step 3 회귀 0건 | R-AUTO-007 / R-AUTO-020 mitigation 회귀 0건 |

## Open Risks & Next Checks

Timeline Dashboard 아래 최신 상태 요약. 자세한 근거는 각 일자 섹션 참조.

### 지금 남은 리스크 (Open)

| ID | 요약 | 상태 | Detection / Mitigation 요약 |
|---|---|---|---|
| R-AUTO-020 | Scheduled Task trigger 성공만으로 Step 2 SUCCESS 처리 시 KRX raw 미적재가 Step 3 이후로 전파 | 🟢 Mitigated (2026-06-22 회귀 0건) | Chrome / chromedriver best-effort reset + Running→Ready wait + Last Result 0 확인 + latest worker log 출력 + KRX raw DB validation + worker stopped fail-closed |
| R-AUTO-016 | crawler worker EC2 stopped 상태에서 Step 2 SUCCESS 진입 | 🟢 Mitigated | 2026-06-21 부터 fail-closed 처리 / skip 폐지 |
| R-AUTO-017 | Chrome / chromedriver stale process 잔존 | 🟢 Mitigated | Step 2 진입 시 best-effort reset / 실패는 warning 유지 |
| R-AUTO-007 | wrapper 성공 종료가 실제 DB 적재를 보장하지 않음 | 🟢 Mitigated | task 57 회수 / `KrxDbValidationCommandId` step result 기록 |
| R-DATA-009 / R-DATA-010 | non-GUI raw 최신성 부족 → Preprocessor 신규 feature date 미생성 | 🟢 Mitigated | 2026-06-16 rev7 운영 경로 생성 + 2026-06-17 17-step E2E 통과 |
| R-SEC-009 | Windows worker Autologon 자격 노출 시간 | 🟠 Open (paper 한정 예외) | 작업 종료 후 EC2 stop / 문서에 자격값 평문 기록 0건 |
| interest_foreignindex_raw HANGSENG/NIKKEI225/SHANGHAI NULL | 지수 일부 NULL Data (non-blocker) | 🟠 Open | Preprocessor blocker 아님 / 후속 점검 |
| interest_ticker_value_raw stale | 최신일 2026-03-09 (별도 도메인) | 🟠 Open | 본 dry-run 차단 요인 제외 / 별도 후속 |
| EC2 lifecycle 자동화 | MarketConnector EC2 stop / start 후 `/tmp/inject-env.sh` 유실 | 🟠 Open | task 59 후속 분리 / 03 spec 책임 |

### 다음 검증 항목 (Next Checks)

- [ ] task 54: EventBridge Scheduler → SSM RunCommand → `schtasks /Run` 정기 trigger 연계
- [ ] task 55: Step Functions 에서 ECS RunTask + SSM RunCommand hybrid orchestration
- [ ] task 56: CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집
- [ ] task 59: EC2 worker 작업 완료 후 stop 절차 명시 (idle 비용 절감)
- [ ] task 78: preprocessor 실행 후 raw / feature 최신성 검증 SQL 자동화
- [ ] task 111: View Daily Batch 에서 `KrxDbValidationCommandId` / latest worker log / Step 2 validation 결과 표시 여부 검토

### Key Fact Preservation (편집 시 유지 필수)

편집 시 다음 근거는 삭제 / 축약 금지. 문서 가독성 개선이 fact loss 로 이어지지 않도록 방어한다.

- KRX GUI worker Last Result `0` (또는 `0x0`) = SUCCESS 조건. `SUCCESS: Attempted to run the scheduled task` marker 만으로는 SUCCESS 처리 금지.
- Scheduled Task trigger 성공 ≠ Step 2 SUCCESS. 6개 성공 조건(non-GUI ECS exitCode 0 / worker EC2 running / Running→Ready 복귀 / Last Result 0 / latest log 출력 / DB validation 통과) 모두 필요.
- KRX raw DB validation: `interest_program_raw` / `interest_shortsell_raw` 의 `max(trade_date)` ≥ ExpectedKrxRawDate, row_count > 0 필요. 실패 시 exit code 30.
- worker EC2 stopped → skip 이 아니라 fail-closed. instanceId / state 를 실패 메시지에 출력.
- Autologon 사용은 paper 전용 Windows worker 한정 보안 예외. 자격증명 값 평문 기록 금지.
- SSM direct Python 실행은 SYSTEM Session 0 부적합으로 채택 거부. SSM 은 `schtasks /Run` trigger 역할만 담당.
- non-GUI ECS Task Definition revision 6 = smoke 전용, revision 7 = daily 운영. 혼동 금지.
- raw 최신성 회복 근거 수치(2026-06-16 §3): `interest_price_raw` 1,207,904 → 1,209,624, `interest_investorflow_raw` 274,204 → 275,949, `interest_shortsell_raw` +349 row 등. row_count / max_date 수치는 유지.

## 기록 형식

- 일자별 `## YYYY-MM-DD <요약>` 헤더로 누적한다(02 / 06 spec operation-notes 와 동일).
- 결과는 성공 / 실패 / 보류 / 해당 없음 / 이월만 짧게 적는다. 실패 사례는 1줄 사유 + 1줄 조치 + 결과까지만 요약한다.
- AWS CLI / Console / CloudWatch 로그 / Task event message / docker build 로그 전문은 본 문서에 인용하지 않는다(보안 / 분량 절감).
- IAM Role / Policy / secret 변경은 변경 일자 / 변경자 / 변경 사유 / 변경 전·후 항목 요약 4줄로만 기록한다(JSON 본문 전체 인용 금지).

## 안전 원칙

- 실제 secret value, password, KIS app key, KIS app secret, 계좌번호, RDS endpoint hostname, RDS password, token, IAM access key id, account-id, 실제 secret ARN, 실제 KMS Key ARN, instance-id, image digest 는 본 문서에 평문 기록 금지. 모두 `[REDACTED]` 또는 placeholder.
- secret 조회 결과(value)는 기록 금지. 성공 / 실패 + 마지막 갱신 시각(필요 시 ISO 8601 `YYYY-MM-DDTHH:MM:SS+09:00`)만.
- 실제 AWS 리소스 생성 / 수정 / 삭제는 운영자가 직접 수행한다. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행한다.
- 8개 MS(`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) 의 README / AGENTS.md / CHANGELOG / docs / worklog 는 본 spec 작업으로 변경하지 않는다. 운영자가 직접 작성한 Dockerfile / requirements.txt 는 운영자 직접 작업이며 본 노트에 사실만 기록한다.
- `secretsmanager:GetSecretValue` 실호출은 운영자만. Kiro 자동 검증은 `secretsmanager:DescribeSecret` metadata 만.
- secret value 가 작업 채팅 / 명령 출력 / 콘솔 캡처 / 운영자 노트에 평문 노출되지 않도록 후속 작업에서도 동일 원칙 유지(R-DOCS-001 정합).

## 2026-06-10 08-interest-crawler-and-preprocessor-ecs 운영자 실행 기록

**Summary** — ECS / ECR 기본 포팅 1차 결과.

- Kiro: 문서 / 절차 정리만
- 운영자: 실제 AWS / IAM / Secrets / RDS 작업 직접 수행

### 1. ECR Repository 생성

1. ECR repository 2개 생성: 완료
   1) `portfolio-interest-crawler`: 생성 완료
   2) `portfolio-interest-preprocessor`: 생성 완료
2. 환경 분리 정책: paper / live 환경별 ECR repository 분리하지 않기로 확정
   1) 동일 image artifact 를 환경별 중복 repository 에 push 하지 않음
   2) paper / live 구분은 image tag / ECS Task Definition / Secrets·SSM path / IAM Task Role / environment variables / RDS·broker 설정 6개 항목에서 처리
3. 공통 base image 후보: 후속 검토로 유지

### 2. Dockerfile / requirements 신규 생성

1. port-interest-preprocessor: Dockerfile 최초 부재 → 운영자 직접 신규 생성
   1) base image: `python:3.13-slim`
   2) command: `python pre_daily.py`
   3) requirements.txt 신규 생성: 주요 의존성 `psycopg2-binary`, `requests`
2. port-interest-crawler: Dockerfile 최초 부재 → 운영자 직접 신규 생성
   1) base image: `python:3.13-slim`
   2) command: `python interest_crawler_daily.py`
   3) Chromium / chromedriver 포함
   4) requirements.txt 신규 생성: 주요 의존성 `beautifulsoup4`, `pandas`, `psycopg2-binary`, `requests`, `selenium`, `yfinance`
3. Crawler 의 Selenium / Chrome / chromedriver 필요성 확인
4. Dockerfile 줄 연속 문자(`\`) 오류 1건을 빌드 단계에서 1차 발견·수정 (PowerShell 백틱이 아니라 Dockerfile 의 `\` 사용)

### 3. 로컬 이미지 빌드

1. preprocessor 우선 빌드: 완료
   1) `portfolio-interest-preprocessor:paper-20260610`
   2) `portfolio-interest-preprocessor:paper-latest`
2. crawler 빌드: 완료
   1) `portfolio-interest-crawler:paper-20260610`
   2) `portfolio-interest-crawler:paper-latest`
   3) Chromium / chromedriver 포함으로 preprocessor 보다 큰 이미지
3. crawler Selenium 의존성 빌드 단계 리스크 1차 해소(빌드 성공). 런타임 안정화는 본 spec 범위 밖

### 4. ECR Push

1. preprocessor push: 완료
   1) tag `paper-20260610`
   2) tag `paper-latest`
2. crawler push: 완료
   1) tag `paper-20260610`
   2) tag `paper-latest`
3. image digest: 운영자가 직접 확인. 실제 digest 값은 본 노트 / spec 산출물에 미기록(`<image-digest>` placeholder)

### 5. ECS Cluster / Task Execution Role / Task Role / Log Group 준비

1. ECS Cluster 생성 / 확인: 완료
   1) 이름: `portfolio-paper-cluster`
   2) Fargate capacity provider 확인
   3) ACTIVE 상태 확인
2. Task Execution Role 생성: 완료
   1) 이름: `portfolio-paper-ecs-task-execution-role`
   2) trust: `ecs-tasks.amazonaws.com`
   3) managed policy: `AmazonECSTaskExecutionRolePolicy` (ECR pull / CloudWatch Logs write 기본 권한 확보)
   4) preprocessor DB secret read inline policy 추가 (단일 secret ARN 기준 한정 / Resource·Action wildcard 0건)
3. Task Role 생성 / 확인: 완료
   1) `portfolio-paper-preprocessor-task-role`
   2) `portfolio-paper-crawler-task-role`
4. CloudWatch Log Group 생성 + retention 14일: 완료
   1) `/portfolio/paper/preprocessor`
   2) `/portfolio/paper/crawler`
5. RDS 접속 SG 확인: 완료
   1) preprocessor task SG → RDS PostgreSQL SG 5432 inbound 허용 확인
   2) Resource wildcard / Action wildcard 금지 원칙 유지
   3) 실제 ARN / account-id 본 노트 미기록

### 6. Secrets Manager / Task Definition / Preprocessor RunTask 검증

1. Secrets Manager `/portfolio/paper/rds/preprocessor-app` 신규 생성: 완료
   1) 최초 미존재 확인 후 신규 생성
   2) JSON multi-key 방식 사용 (`host` / `port` / `dbname` / `username` / `password`)
   3) 실제 endpoint / password / ARN / account-id 본 노트 미기록
2. preprocessor Task Definition 등록: 완료
   1) family: `portfolio-paper-interest-preprocessor`
   2) revision: 1
   3) networkMode: `awsvpc`
   4) cpu: 512 / memory: 1024
   5) image tag: `paper-20260610`
   6) Secrets Manager JSON key 를 ECS `secrets` 필드로 env 주입
3. preprocessor ECS RunTask 1차 검증: 완료
   1) public subnet + `assignPublicIp = ENABLED` 방식 사용
   2) CloudWatch Logs 출력 확인
   3) 최종 lastStatus: `STOPPED`
   4) 최종 exitCode: `0`
   5) `PREPROCESSOR PIPELINE END` 확인

### 7. Preprocessor RunTask 검증 중 발견 / 조치한 이슈

1. 1차 실패: Secrets Manager JSON secret 의 `host` key 누락
   1) 증상: psycopg2 가 Unix socket `/var/run/postgresql/.s.PGSQL.5432` 로 접속 시도
   2) 조치: `/portfolio/paper/rds/preprocessor-app` JSON secret 재생성(`host` / `port` / `dbname` / `username` / `password` 모두 포함)
   3) 결과: 해소
2. 2차 실패: DB sequence 권한 부족
   1) 증상: `permission denied for sequence pre_marketbreadth_daily_feature_id_seq`
   2) 원인: 일부 preprocessor 관련 sequence 가 `public` schema 에 잔존
   3) 조치: 운영자가 직접 GRANT 실행
       - `public.pre_marketbreadth_daily_feature_id_seq` 에 `preprocessor_app` USAGE / SELECT 부여
       - `public.pre_macroeconomic_daily_feature_id_seq` 에 `preprocessor_app` USAGE / SELECT 부여
   4) 확인: 위 2개 sequence 에 대한 `preprocessor_app` USAGE / SELECT 권한이 true
3. 최종 재실행: preprocessor ECS Task exitCode `0` 확인

### 8. Crawler 상태 (이월 정리)

1. 완료 항목
   1) crawler Dockerfile / requirements 신규 생성
   2) Selenium / Chromium / chromedriver 포함 이미지 빌드 성공
   3) ECR push 완료(`paper-20260610` / `paper-latest`)
2. 이월 항목
   1) crawler Task Definition 등록
   2) crawler RunTask runtime 검증
   3) KRX / Naver / yfinance outbound runtime 도달 여부 검증
   4) crawler 안정화 100% 는 본 spec 범위 밖(후속 spec / 후속 phase 책임)

### 9. 본 일자 안전 / 문서 기록 점검

1. 실제 password / secret value / endpoint hostname / account-id / 실제 ARN / image digest / IAM access key id 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
2. Kiro 본 spec 작업으로 인한 AWS / IAM / Secrets Manager / RDS 변경 0건. 모든 실제 작업은 운영자가 직접 수행했고 Kiro 는 절차 / 결과 / 실패 사유 / 조치를 문서로만 정리.
3. Kiro 본 spec 작업으로 인한 8개 MS 코드 / 패키징 미수정. port-interest-preprocessor / port-interest-crawler 의 Dockerfile / requirements.txt 는 운영자 직접 작업으로 신규 생성(본 노트 §2 참조).
4. Kiro 본 spec 작업으로 인한 외부 호출 / 크롤링 / 주문 / RDS DDL·DML 0건. 운영자 직접 수행한 RDS sequence GRANT 2건과 preprocessor pipeline RunTask 결과는 본 노트 §6 / §7 참조.
5. secret value 가 작업 채팅 / 명령 출력 / 콘솔 캡처 / 운영자 노트에 평문 노출되지 않도록 후속 작업에서도 동일 원칙 유지(R-DOCS-001 정합).

### 10. 후속 인계

1. crawler Task Definition / RunTask runtime 검증 — 후속 작업으로 진행
2. crawler outbound(KRX / Naver / yfinance / Selenium) 운영 안정화 — 후속 spec / 후속 phase 책임
3. `public` schema 잔존 sequence 추가 점검 — preprocessor 관련 외 도메인 sequence 잔존 여부 후속 점검(이월)
4. 본 spec 산출물(requirements.md / design.md / tasks.md) 갱신 0건. 본 노트 신규 생성 1건. runbook.md / validation-checklist.md 는 운영자 실행 이후 별도 작성

## IAM 변경 기록 템플릿 (필요 시 일자별 추가)

본 spec 의 Task Execution Role / Task Role / Permission Policy / Resource ARN 목록이 변경되는 경우 본 섹션에 누적 기록한다. 변경 1건 = 4줄 요약 형식.

```
## YYYY-MM-DD IAM 변경
- 변경 일자: YYYY-MM-DDTHH:MM:SS+09:00
- 변경자: [운영자 식별자(닉네임 / 직무)]  # 실제 IAM user / email 평문 금지
- 변경 사유: [한 줄 요약]
- 변경 전 / 후 항목 요약: [Resource 추가 / 삭제 항목 수, Action 추가 / 삭제 항목 수, KMS statement 추가 여부 등 — JSON 본문 전체 인용 금지]
```

## 2026-06-12 Windows EC2 worker 기반 KRX GUI 의존 수집 1차 검증

**Summary** — Windows EC2 worker 기반 KRX program / shortsell 수집 1차 검증.

- 분류 결정: KRX GUI 수집 = Windows EC2 worker / non-GUI crawler = ECS Fargate Task 후보 유지 / preprocessor = ECS Fargate Task 유지
- Kiro: 문서 / 절차 / 검증 항목 정리만
- 운영자: 실제 AWS / IAM / Secrets Manager / EC2 / RDS 작업 직접 수행

### 1. EC2 worker 환경 확인

1. EC2 worker 시작 후 RDP 접속: 성공
2. 작업 디렉터리 `C:\portfolio\port-interest-crawler` 확인: 정상
3. venv `C:\portfolio\venvs\interest-crawler` activate: 정상
4. Python 인터프리터 버전: 3.13.5
5. EC2 IAM Role 인식: `portfolio-paper-crawler-worker-role` 인식 확인 (실제 ARN / account-id 본 노트 미기록)
6. 사전 점검: DB 환경변수 미주입 상태에서 `localhost:5432` 접속 시도로 실패함을 확인 (예상 동작)

### 2. RDS Secret 기반 DB 환경변수 주입

1. `C:\portfolio\load-crawler-db-env.ps1` 실행 후 Secrets Manager 기반 DB 환경변수 주입: 성공
2. 사용 secret: `/portfolio/paper/rds/crawler-app` (실제 ARN / account-id 본 노트 미기록)
3. 주입된 환경변수 확인
   1) `INTEREST_DB_HOST`: 주입 확인
   2) `INTEREST_DB_PORT`: 주입 확인
   3) `INTEREST_DB_NAME`: 주입 확인
   4) `INTEREST_DB_USER`: 주입 확인
   5) `INTEREST_DB_PASSWORD`: 주입 확인 (값 미출력 / 미기록)
4. secret value 작업 채팅 / 명령 출력 / 노트 평문 노출 0건(R-DOCS-001 정합)

### 3. 다운로드 경로 불일치 조치

1. 코드 기대 경로: `interest_program.py` 가 `C:\Users\USER\Downloads` 사용을 가정
2. 실제 Chrome 다운로드 경로: `C:\Users\Administrator\Downloads`
3. 조치
   1) 빈 폴더 `C:\Users\USER\Downloads` 제거
   2) `C:\Users\USER\Downloads` → `C:\Users\Administrator\Downloads` junction 연결
4. 결과: junction 적용 후 `interest_program.py` 가 정상적으로 CSV 파일을 인식하고 수집 성공
5. 후속: wrapper 안에서 junction 존재 여부 / 정합성 점검을 매 실행 시 자동 확인

### 4. KRX program 단독 수집 결과

1. 선행 실행: `interest_krx_login_new.py`
   1) `KRX ID/PW Login Success`
   2) `KRX Login Ready`
2. `interest_program.py` 단독 실행: 성공
3. 수집 일자별 처리 결과
   1) 2026-06-09: Collected
   2) 2026-06-10: Collected
   3) 2026-06-11: Collected
4. DB 검증 — `interest_program_raw`
   1) row count 변화: 543 → 546 (+3)
   2) 일자별 row count: 2026-06-09 / 2026-06-10 / 2026-06-11 각 1건
   3) 샘플 컬럼 확인: `trade_date`, 수량 / 금액 컬럼군, `source`, `source_version`, `created_at`, `updated_at`, `collected_at` 모두 정상

### 5. KRX shortsell 단독 수집 결과

1. `interest_shortsell.py` 단독 실행: 성공
2. 수집 일자별 처리 결과
   1) 2026-06-09: 349 Company Collected
   2) 2026-06-10: 349 Company Collected
   3) 2026-06-11: 349 Company Collected
3. DB 검증 — `interest_shortsell_raw`
   1) row count 변화: 189158 → 190205 (+1047)
   2) 일자별 row count: 2026-06-09 / 2026-06-10 / 2026-06-11 각 349건
   3) 종목 수 × 일수: 349 × 3 = 1047 일치
   4) 샘플 컬럼 확인: `trade_date`, `ticker_code`, `ticker_name`, `short_volume`, `short_amount`, `total_volume`, `short_ratio`, `source`, `source_version`, `collected_at` 모두 정상

### 6. 한글 literal / 인코딩 검증

1. 한글 포함 `encoding_test.py` 생성 및 실행: 정상 동작 확인
2. Python patch script 방식으로 한글 문자열 수정: 정상 (BOM 제거 + utf-8 저장)
3. 검증 방식
   1) `read_text(encoding="utf-8-sig")` 로 BOM 자동 제거 확인
   2) `write_text(encoding="utf-8")` 로 utf-8 저장 확인
4. 결정: 향후 EC2 내부에서 Python 소스 수정이 필요한 경우 PowerShell 의 `Get-Content` / `Set-Content` 방식 대신 Python patch script 방식만 사용한다(한글 literal 훼손 방지)

### 7. KRX Secret Manager 연동

1. KRX 로그인 전용 Secret 신규 생성: 완료
   1) Secret path: `/portfolio/paper/krx/crawler-login`
   2) JSON key: `username`, `password`
   3) Secret value 본 노트 평문 기록 0건(`[REDACTED]` / "Secrets Manager 에서 주입" 만)
2. 권한 누락 1차 사례
   1) 증상: `portfolio-paper-crawler-worker-role` 에 신규 KRX secret read 권한 없어 `AccessDeniedException` 발생
   2) 운영자 IAM 변경 — 4줄 요약
       - 변경 일자: 2026-06-12
       - 변경자: 운영자(직무 식별자만)
       - 변경 사유: KRX 로그인 secret read 누락 해소
       - 변경 전 / 후 항목 요약: `portfolio-paper-crawler-worker-role` 에 inline policy 추가, 허용 action `secretsmanager:DescribeSecret` / `secretsmanager:GetSecretValue`, 대상 Resource 는 KRX crawler login secret 한정(Resource·Action wildcard 0건)
   3) 결과: 권한 추가 후 EC2 worker 에서 secret 조회 성공
3. KRX 환경변수 주입
   1) `C:\portfolio\load-krx-env.ps1` 신규 생성
   2) `KRX_USER_ID` 주입 확인
   3) `KRX_USER_PASSWORD` 주입 확인 (값 미출력 / length 만 점검)
4. 재실행 검증: `interest_krx_login_new.py` 가 Secrets Manager 기반 자격으로 KRX 로그인 성공

### 8. EC2 worker daily wrapper

1. `C:\portfolio\run_krx_worker_daily.ps1` 신규 생성 (실제 ps1 본문 전체는 본 노트에 미기록 / 사실만 기록)
2. wrapper 역할(요약)
   1) venv activate
   2) RDS Secret 기반 DB 환경변수 주입
   3) KRX Secret 기반 KRX 환경변수 주입
   4) 다운로드 경로 junction 존재 확인
   5) `interest_krx_login_new.py` → `interest_program.py` → `interest_shortsell.py` 순차 실행
   6) `C:\portfolio\logs` 하위 실행 로그 저장
3. 로그 파일 형식 확인: `krx_worker_daily_yyyyMMdd_HHmmss.log`
4. wrapper 1차 실행 결과
   1) `KRX already logged in`
   2) `KRX Login Ready`
   3) program 실행 성공
   4) shortsell 실행 성공
5. wrapper 재실행 결과: `[Collected Date] None`
   1) 원인: 2026-06-09 ~ 2026-06-11 이미 수집 완료 상태 → 신규 수집 대상 0건
   2) 판단: 실패가 아닌 idempotent / no-op 성격의 정상 완료
6. DB 재확인 (`check_program_rows.py`, `check_shortsell_rows.py`)
   1) program: 2026-06-09 / 2026-06-10 / 2026-06-11 각 1건 유지
   2) shortsell: 2026-06-09 / 2026-06-10 / 2026-06-11 각 349건 유지

### 9. 최종 판단 / 분류 결정

1. KRX GUI 의존 수집(KRX program / KRX shortsell): Windows EC2 worker 사용 — 확정
2. non-GUI crawler(Naver / yfinance / KRX 비-GUI 경로): ECS Fargate Task 후보 유지
3. preprocessor: ECS Fargate Task 유지(2026-06-10 1차 검증 결과 그대로)
4. 전체 Interest Crawler / Preprocessor 흐름: hybrid execution model 로 정리(EC2 worker + ECS Fargate Task 혼합)
5. EC2 worker 기반 KRX program / shortsell 1차 운영 가능 상태: 도달
6. 완전 자동화: 본 일자 범위 밖
7. 자동화 후속(SSM RunCommand / EventBridge Scheduler / Step Functions hybrid orchestration / CloudWatch Logs Agent / wrapper 내 DB 검증 출력 자동 추가)은 후속으로 분리

### 10. 본 일자 안전 / 문서 기록 점검

1. 실제 secret value / password / KRX 로그인 password / KIS app key / KIS app secret / 계좌번호 / RDS endpoint hostname / IAM access key id / account-id / 실제 ARN / image digest / instance-id 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
2. KRX 로그인 ID / password 는 "Secrets Manager 에서 주입" 으로만 표기. 값 미기록.
3. Kiro 본 일자 작업으로 인한 AWS / IAM / Secrets Manager / EC2 / RDS 변경 0건. 모든 실제 작업은 운영자 직접 수행. Kiro 는 절차 / 결과 / 실패 사유 / 조치를 문서로만 정리.
4. Kiro 본 일자 작업으로 인한 8개 MS(`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 미수정.
5. 운영자가 EC2 내부에 직접 생성한 ps1 / py 테스트 / wrapper 파일은 사실만 본 노트에 기록(파일 본문 전체 미기록).
6. Kiro 본 일자 작업으로 인한 외부 호출 / 크롤링 / 주문 / RDS DDL·DML 0건. 운영자 직접 수행한 KRX 수집 결과 / sequence GRANT / wrapper 실행은 본 노트 §3 ~ §8 참조.
7. secret value 가 작업 채팅 / 명령 출력 / 콘솔 캡처 / 운영자 노트에 평문 노출되지 않도록 후속 작업에서도 동일 원칙 유지(R-DOCS-001 정합).

### 11. 후속 인계

1. SSM RunCommand 기반 EC2 worker 무인 실행: 후속 분리
2. EventBridge Scheduler → SSM RunCommand 연계: 후속 분리
3. Step Functions 에서 ECS Task + EC2 worker 혼합 orchestration: 후속 분리
4. CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집: 후속 분리
5. wrapper 내 DB 검증 출력 자동 추가(`interest_program_raw` / `interest_shortsell_raw` 의 일자별 row count 출력): 후속 분리
6. ECS / Fargate non-GUI crawler 범위 재정리(어떤 crawler 가 EC2 worker 로 / 어떤 crawler 가 ECS Fargate Task 로 가는지 인벤토리 확정): 후속 분리
7. EC2 worker 작업 완료 후 stop 절차 명시(idle 시간 비용 절감): 후속 분리
8. KRX GUI 수집 headless 리팩토링: 장기 후보로만 유지(현재 결정은 EC2 worker 사용)

## 2026-06-13 SSM RunCommand 자동화 + ECS crawler smoke 1차 검증

**Summary** — KRX GUI 자동화 진입점 확정 + ECS crawler smoke 통과.

- 자동화 방식 확정: SSM RunCommand → `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"` → Windows Scheduled Task → Administrator interactive session → wrapper. SSM direct wrapper 실행은 채택 거부(SYSTEM Session 0 부적합)
- ECS crawler Task Definition revision 6 = Selenium / Chrome / outbound smoke 전용
- Kiro: 문서 / 절차 / 검증 항목 정리만
- 운영자: 실제 AWS / SSM / EC2 / ECS / CloudWatch / RDS 확인 작업 직접 수행
- 결정 락: [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-MS-012 / OD-MS-015

### 1. SSM 관리 상태 확인

1. Systems Manager 노드 살펴보기: 완료
   1) `portfolio-paper-crawler-worker` Managed Node 등록 확인
   2) RunCommand 대상 지정 가능 확인
   3) `AWS-RunPowerShellScript` 실행 가능 확인
2. RunCommand 기본 실행 검증: 완료
   1) `hostname` 실행 성공
   2) `whoami` 결과: `nt authority\system` 확인
   3) PowerShell 5.1 실행 확인
   4) `C:\portfolio` 파일 접근 가능 확인

### 2. SSM RunCommand 직접 실행 시도 / 실패 판단

1. wrapper 직접 실행 시도(SSM RunCommand → `C:\portfolio\run_krx_worker_daily.ps1`): 부분 완료
   1) wrapper 실행 시작: 확인
   2) DB Secret 로딩: 성공
   3) KRX Secret 로딩: 성공
   4) 다운로드 junction 확인: 성공
   5) KRX 로그인 단계: 실패
2. 실패 원인 판단: 완료
   1) SSM RunCommand 는 Session 0 / SYSTEM 계정에서 실행됨
   2) Chrome 프로세스가 SessionId 0 에서 실행됨 확인
   3) RDP 사용자 세션은 SessionId 2 로 분리됨 확인
   4) KRX GUI 의존 로그인은 SYSTEM Session 0 직접 실행 방식과 호환 부족(GUI / Display / Chrome download 폴더 / OTP 세션 의존성)
3. 결론: SSM RunCommand 가 wrapper 를 SYSTEM Session 0 에서 직접 실행하는 방식은 KRX GUI 로그인에 부적합. 직접 실행 방식은 채택하지 않음.

### 3. Scheduled Task 보완 방식 적용

1. Scheduled Task 등록: 완료
   1) Task 이름: `Portfolio-KRX-Worker-Daily`
   2) 실행 사용자: Administrator (interactive 세션 기준)
   3) 실행 대상: `C:\portfolio\run_krx_worker_daily.ps1`
2. 1차 trigger 검증(Administrator RDP 세션 기준): 완료
   1) `Start-ScheduledTask` 로 Task 실행 성공
   2) Chrome / Selenium GUI 실행 가능 확인
3. wrapper 1차 실행 결과(Scheduled Task 기준): 완료
   1) `KRX ID/PW Login Success` 확인
   2) `KRX Login Ready` 확인
   3) `interest_program` 2026-06-12 수집 성공
   4) `interest_shortsell` 2026-06-12 349 Company Collected 확인
   5) `KRX worker daily wrapper DONE` 정상 종료 확인

### 4. SSM 기반 Scheduled Task trigger 검증 (RDP closed)

1. RDP 창을 닫고 EC2 가 Running 상태로만 유지된 상태에서 SSM RunCommand 로 Scheduled Task 를 trigger: 완료
   1) 실행 명령: `schtasks /Run /TN Portfolio-KRX-Worker-Daily`
   2) SSM 응답: `SUCCESS: Attempted to run the scheduled task` 확인
2. Scheduled Task 상태 확인: 완료
   1) trigger 직후 State = `Running`
   2) RDP 재접속 후 KRX worker daily wrapper 가 Administrator interactive 세션 위에서 작동 중 확인
   3) 최종 State = `Ready` (실행 종료)
3. wrapper 최신 로그 확인: 완료
   1) 로그 파일: `krx_worker_daily_20260613_021904.log`
   2) `KRX login` 성공 확인
   3) `interest_program [Collected Date] None` 확인
   4) `interest_shortsell [Collected Date] None` 확인
   5) `DONE :: KRX worker daily` 확인
4. `[Collected Date] None` 판단: 완료
   1) 원인: 2026-06-12 까지 이미 수집 완료 상태 → 신규 수집 대상 0건
   2) 판단: 실패가 아닌 idempotent / no-op 정상 완료(2026-06-12 §8 정합)
5. 결론: SSM RunCommand 가 Scheduled Task 를 trigger 하는 방식이 1차 자동화 방식으로 확정. RDP 미접속 상태에서 EC2 Running 만으로 자동 실행 가능 확인.

### 5. ECS crawler Task Definition / RunTask smoke 검증

1. non-GUI crawler 범위 확인: 완료
   1) crawler 소스 파일 목록 확인
   2) Selenium / WebDriver / Chrome / `debuggerAddress` 의존 파일 식별
   3) KRX GUI 의존 파일 = Windows EC2 worker 분리 유지(2026-06-12 그대로)
   4) `requests` / `yfinance` 중심 파일 = ECS Fargate 후보 유지
   5) `interest_crawler_daily.py` 는 KRX GUI 의존 파일과 non-GUI 파일을 함께 호출 → ECS 단독 실행 대상에서 제외
   6) crawler task 와 EC2 worker 역할 경계 정리 완료
2. Task Definition 확인: 완료
   1) family: `portfolio-paper-interest-crawler` revision 1 ~ 6
   2) 최신 revision: `portfolio-paper-interest-crawler:6`
   3) image: `portfolio-interest-crawler:paper-20260611`
   4) 호환: Fargate
   5) cpu: 1024 / memory: 2048
   6) network mode: `awsvpc`
   7) `taskRoleArn`: `portfolio-paper-crawler-task-role`
   8) `executionRoleArn`: `portfolio-paper-ecs-task-execution-role`
   9) crawler DB Secret 환경변수 연결 확인
   10) CloudWatch Logs group: `/portfolio/paper/crawler`
   11) log stream prefix: `ecs-selenium-chrome-smoke`
   12) revision 6 의 `command`: 실제 daily crawler entrypoint 가 아니라 Selenium Chrome smoke command (ECS / Chrome / network smoke 검증용)
3. RunTask smoke 실행: 완료
   1) Subnet: public-a / public-b 확인
   2) Security Group: `sgroup-crawler-tasks` 확인
   3) `assignPublicIp = ENABLED` 기준으로 RunTask 실행
   4) Task 최종 lastStatus: `STOPPED`
   5) `stoppedReason`: `Essential container in task exited`
   6) container exitCode: `0`
4. CloudWatch Logs 확인: 완료
   1) Log group: `/portfolio/paper/crawler`
   2) Log stream: `ecs-selenium-chrome-smoke/interest-crawler/<task-id>`
   3) Selenium 4.40.0 확인
   4) `CHROME_BIN=/usr/bin/chromium` 확인
   5) `CHROMEDRIVER_PATH=/usr/bin/chromedriver` 확인
   6) `example.com` 접속 성공
   7) Naver Finance 접속 성공
   8) TITLE naver finance: `Npay 증권` 확인
   9) `SELENIUM CHROME SMOKE SUCCESS` 확인
   10) `DRIVER QUIT` 확인
   11) `SELENIUM CHROME SMOKE END` 확인
5. 결론: ECS / Fargate Selenium Chrome smoke 1차 통과. revision 6 은 smoke 검증용이며 실제 daily crawler Task Definition 등록과는 분리된다. non-GUI crawler 의 실제 Task Definition 분리는 후속 task(58) 책임.

### 6. Hybrid execution model 1차 완성 판단

1. KRX program / KRX shortsell: EC2 worker 기준 완료
   1) SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1` 실행 구조 확인
2. non-GUI crawler 후보: ECS Fargate 기준으로 분리 완료
3. ECS crawler Task Definition 기본 골격: 확인 완료(revision 6, smoke 용도)
4. ECS / Fargate Selenium Chrome smoke: 성공
5. public subnet + `assignPublicIp = ENABLED` outbound 접근 가능: 확인
6. CloudWatch Logs 확인 가능: 확인
7. preprocessor 전 단계 데이터 적재 구조: hybrid execution model 기준 유지
8. 판단: Interest Crawler hybrid execution model 1차 완성

### 7. 본 일자 안전 / 문서 기록 점검

1. 실제 secret value / password / KRX 로그인 password / KIS app key / KIS app secret / 계좌번호 / RDS endpoint hostname / IAM access key id / account-id / 실제 ARN / image digest / instance-id / task ARN 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
2. KRX 로그인 ID / password 는 "Secrets Manager 에서 주입" 으로만 표기. 값 미기록.
3. Kiro 본 일자 작업으로 인한 AWS / SSM / EC2 / ECS / CloudWatch / IAM / Secrets Manager / RDS 변경 0건. 모든 실제 작업은 운영자 직접 수행.
4. Kiro 본 일자 작업으로 인한 8개 MS(`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 미수정.
5. 운영자가 EC2 내부에 직접 등록한 Scheduled Task / wrapper / 환경변수 주입 ps1 파일은 사실만 본 노트에 기록(파일 본문 전체 미기록).
6. Kiro 본 일자 작업으로 인한 외부 호출 / 크롤링 / 주문 / RDS DDL·DML 0건. 운영자 직접 수행한 SSM RunCommand / Scheduled Task trigger / ECS RunTask smoke / CloudWatch Logs 확인은 본 노트 §1 ~ §5 참조.
7. wrapper 로그 본문 / SSM 응답 본문 / CloudWatch Logs 전문 / Selenium / Chrome 의 stdout / stderr 전문은 본 노트에 인용하지 않음(분량 절감 + R-DOCS-001 / R-DOCS-002 정합).
8. `secretsmanager:GetSecretValue` 실호출은 운영자만. Kiro 자동 검증 / 본 노트 작성 과정에서 secret value 호출 0건.

### 8. 후속 인계

1. EventBridge Scheduler → SSM RunCommand 연계(`schtasks /Run` 트리거 정기 실행): 후속 분리
2. Step Functions 에서 ECS Task + EC2 worker 혼합 orchestration: 후속 분리
3. CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집: 후속 분리
4. wrapper 내 DB 검증 출력 자동 추가(`interest_program_raw` / `interest_shortsell_raw` 의 일자별 row count 출력) — R-AUTO-007 정합: 후속 분리
5. ECS / Fargate non-GUI crawler 실제 Task Definition 분리(인벤토리 확정 + smoke 용 revision 6 과 운영용 Task Definition 분리): 후속 분리
6. EC2 worker 작업 완료 후 stop 절차 명시(idle 시간 비용 절감): 후속 분리
7. 본 spec 산출물(requirements.md / design.md / tasks.md) 갱신은 본 노트 §1 ~ §6 결과 반영분 한정. runbook.md / validation-checklist.md 는 운영자 실행 이후 별도 작성 책임 유지.

## 2026-06-15 Backend AWS E2E dry-run 1차 / Interest Crawler 상태 재판정

**Summary** — Backend AWS E2E dry-run 을 17단계 순서로 1차 점검. View 진입 전 stale raw data 이슈를 조기 발견.

작업 범위:

- (a) AWS 계정 / region / EC2 / ECS / AWS Batch 사전 점검
- (b) MarketConnector EC2 기반 `CONNECTOR_BALANCE` 1차 실행
- (c) Windows EC2 worker 기반 KRX worker 재실행 + `interest_program_raw` / `interest_shortsell_raw` 최신일자 확인
- (d) non-GUI crawler `interest_*_raw` 최신일자 SQL 점검
- (e) preprocessor ECS RunTask 1회 단발 실행 + DB `updated_at` 갱신 확인

안전:

- Kiro: 문서 / 절차 / 검증 항목 정리만. 운영자: 실제 AWS / SSM / EC2 / ECS / Batch / RDS 작업 직접 수행
- 실제 BUY / SELL / `--execute` 주문 전송 계열 0건(OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 ~ R-AUTO-011 정합)

### 1. 사전 점검 (AWS 계정 / region / EC2 / ECS / AWS Batch)

1. AWS 계정 / region 확인: 완료
   1) region: `ap-northeast-2`
   2) account 확인 완료 (account-id 평문 기록 0건 — `<account-id>` placeholder 만)
   3) 로컬 AWS CLI 기본 실행 가능
2. EC2 상태 확인: 완료
   1) MarketConnector EC2 running 확인
   2) Windows crawler worker running 확인
   3) SSM managed instance Online 확인
3. ECS 상태 확인: 완료
   1) `portfolio-paper-cluster` ACTIVE 확인
   2) preprocessor task definition(family `portfolio-paper-interest-preprocessor`) 확인
   3) Strategy Decision buy-signal task definition(family `portfolio-paper-strategy-decision-buy-signal`) 확인
   4) Strategy Decision position-signal task definition(family `portfolio-paper-strategy-decision-position-signal`) 확인
4. AWS Batch Research 상태 확인: 완료
   1) Compute Environment `portfolio-paper-strategy-research-ce`: ENABLED / VALID / Healthy 확인
   2) Job Queue `portfolio-paper-strategy-research-queue`: ENABLED / VALID / Healthy 확인
   3) Research Job Definition `portfolio-paper-strategy-research` active revision 확인
   4) Backtest report S3 upload 포함 latest revision(`portfolio-paper-strategy-research:3`) 확인

### 2. CONNECTOR_BALANCE 실행 (Backend E2E dry-run 1번)

1. MarketConnector EC2 에서 실행: 완료
   1) 작업 디렉터리: `/home/ec2-user/apps/port-marketconnector`
   2) 실행 환경: venv python 사용
   3) 진입점: SSM RunCommand
2. 초기 실패 원인 확인 / 조치: 완료
   1) 1차 실패 — `KIS_APP_KEY` 등 환경변수 미주입 상태에서 import smoke 실패
   2) 원인 — KIS Secrets 가 JSON 형태(`/portfolio/paper/kis/marketconnector` JSON multi-key)임을 확인. SecretString 원문을 그대로 export 하면 안 되고 JSON key 를 파싱해야 함
   3) 조치 — Secret JSON key → 환경변수 mapping 정정
3. KIS Secret key mapping 확인: 완료
   1) `APP_KEY` → `KIS_APP_KEY`
   2) `APP_SECRET` → `KIS_APP_SECRET`
   3) `PAPER_ACNT` → `KIS_PAPER_ACNT`
   4) `ACNT_PRDT_CD` → `KIS_ACNT_PRDT_CD`
4. 실행 성공: 완료
   1) 기존 `access_token.txt` 백업
   2) 신규 token 발급 성공 (token 값 평문 기록 0건)
   3) KIS balance API status 200
   4) 모의투자 잔고 조회 성공
   5) `connector.connector_balance_snapshot` 저장 성공
   6) `connector.connector_position_snapshot` — 현재 보유종목 0건 기준 정상 처리
   7) legacy holdings 데이터 없음 확인
5. 문서화 필요 사항(R-DOCS-001 정합):
   1) KIS Secrets 는 JSON key parsing 필요(raw SecretString export 금지)
   2) secret value / KIS app key / KIS app secret / 계좌번호 / token 본 노트 평문 기록 0건
6. 안전 점검: 본 단계는 조회성 호출 + balance / position snapshot 저장 한정. broker BUY / SELL 주문 호출 0건. `--execute` 0건.

### 3. INTEREST_CRAWLER 상태 재판정 (Backend E2E dry-run 2번)

1. KRX GUI worker 상태: 완료(2026-06-12 §1 ~ §9 / 2026-06-13 §1 ~ §6 누적분 그대로 유지)
   1) Windows EC2 worker 기반 KRX login 성공
   2) `interest_program.py` 단건 수집 성공
   3) `interest_shortsell.py` 단건 수집 성공
   4) `run_krx_worker_daily.ps1` wrapper 생성 / 실행 성공
   5) DB Secret(`/portfolio/paper/rds/crawler-app`) / KRX Secret(`/portfolio/paper/krx/crawler-login`) 로딩 확인
   6) 다운로드 경로 junction 처리 완료(`C:\Users\USER\Downloads` → `C:\Users\Administrator\Downloads`)
   7) program / shortsell 로그 파일 생성 확인(`C:\portfolio\logs\krx_worker_daily_*.log`)
2. 2026-06-15 KRX worker 재실행 결과: 완료
   1) `KRX already logged in` 확인
   2) `KRX Login Ready` 확인
   3) program 실행 성공
   4) shortsell 실행 성공
   5) `[Collected Date] None` 출력 — 신규 수집 대상 0건(2026-06-13 / 14 동안 누적된 거래일 없음 + 2026-06-15 월요일 시점에 직전 거래일 2026-06-12 까지 이미 적재됨)
   6) `check_program_rows.py` / `check_shortsell_rows.py` 로 DB 상태 재확인
3. KRX raw 최신일자 (2026-06-15 시점):
   1) `interest_program_raw` 최신일: 2026-06-12
   2) `interest_shortsell_raw` 최신일: 2026-06-12
   3) 2026-06-15 월요일 기준 직전 거래일 2026-06-12 금요일까지 적재된 상태(KRX 거래일 정상)
4. non-GUI crawler 상태: 미완료 / 후속 승격
   1) ECS / Fargate Selenium Chrome smoke 검증 완료(2026-06-13 §5)
   2) 실제 daily raw 전체 수집 완료는 아님 — non-GUI daily 운영 Task Definition / command 분리 필요(task 58 후속)
   3) raw 전체 최신성 검증 실패 — non-GUI raw 7종이 직전 거래일까지 적재되지 않음
5. non-GUI raw 최신일자 SQL 점검 결과 (2026-06-15 시점):
   1) `interest_agency_raw` 최신일: 2026-06-11
   2) `interest_news_raw` 최신일: 2026-06-11
   3) `interest_commodity_raw` 최신일: 2026-06-08
   4) `interest_foreignindex_raw` 최신일: 2026-06-08
   5) `interest_investorflow_raw` 최신일: 2026-06-08
   6) `interest_marketbreadth_raw` 최신일: 2026-06-08
   7) `interest_price_raw` 최신일: 2026-06-08
   8) `interest_ticker_value_raw` 최신일: 2026-03-09 — 본 dry-run 핵심 차단 요인에서는 제외(별도 후속 분리)
6. 영향 분석:
   1) Preprocessor 가 실행되더라도 입력 raw 최신성 부족으로 신규 feature date 생성이 제한됨(아래 §4 정합)
   2) Research / Decision 으로 넘어가기 전 데이터 최신성 기준이 약함 — Backend E2E dry-run 의 본 phase 통과 보강 필요
   3) Backend E2E dry-run 기준 2번 `INTEREST_CRAWLER` 는 **완료가 아니라 부분 완료 / follow-up 승격** 으로 재분류
7. 표현 보정 (R-DOCS-001 / 본 일자 기록 정합):
   1) 기존 표현 "Interest Crawler 완성: 완료" / "Interest Crawler 는 hybrid execution model 기준으로 1차 완성" 은 과대 표현
   2) 보정 표현 — "Interest Crawler hybrid 1차 구현: 부분 완료" / "KRX GUI worker 는 운영 가능 상태로 1차 완성" / "ECS / Fargate crawler 는 smoke 검증 완료" / "non-GUI daily raw 수집 운영 경로와 raw 전체 최신성 검증은 후속"
   3) "완료" 표기는 실제 데이터 적재 / 최신성 검증까지 확인된 경우에만 사용한다는 원칙 1차 실증

### 4. PREPROCESSOR ECS Task 단발 실행 (Backend E2E dry-run 3번)

1. ECS RunTask 실행: 완료
   1) cluster: `portfolio-paper-cluster`
   2) task definition: `portfolio-paper-interest-preprocessor:1`
   3) launch type: `FARGATE`
   4) networkMode: `awsvpc`
   5) subnet: public-a / public-b 사용
   6) `assignPublicIp = ENABLED`
   7) Security Group: `sgroup-preprocessor-tasks`
2. 실행 결과: 성공
   1) lastStatus: `STOPPED`
   2) desiredStatus: `STOPPED`
   3) stopCode: `EssentialContainerExited`
   4) stoppedReason: `Essential container in task exited`
   5) container: `interest-preprocessor`
   6) exitCode: 0
   7) 실행 시간 약 3분 43초
3. CloudWatch Logs 확인:
   1) `/portfolio/paper/preprocessor` log stream 생성 확인
   2) 최신 log stream `storedBytes = 0` — log 본문 기반 검증은 제한적
   3) exitCode 0 기준으로 Task 성공으로 판단
   4) log 본문 전체 인용 0건(R-DOCS-001 / 본 노트 안전 원칙 정합)
4. DB 반영 확인:
   1) preprocessor 테이블 `updated_at` 이 2026-06-15 11:03:55+00 으로 갱신됨(KST 기준 2026-06-15 20:03:55)
   2) ECS Task 실행 시간과 일치
   3) DB write 경로는 동작한 것으로 판단
5. 신규 feature date 확인:
   1) 신규 2026-06-15 feature date 는 확인되지 않음
   2) 기존 feature row update / 재계산 형태로 보임
   3) 원인은 preprocessor 장애가 아니라 §3 의 raw 최신성 부족(특히 `interest_price_raw` / `interest_marketbreadth_raw` / `interest_investorflow_raw` / `interest_commodity_raw` / `interest_foreignindex_raw` / `interest_news_raw` / `interest_agency_raw` 가 직전 거래일까지 적재되지 않음)
6. 결론:
   1) PREPROCESSOR ECS 실행 경로는 성공 — exitCode 0 / DB write 동작
   2) 단, 입력 raw 최신성 부족으로 데이터 최신화는 제한
   3) Backend E2E dry-run 기준 PREPROCESSOR 는 **실행 완료** 로 표시하되 **데이터 최신성 제약** 을 함께 기록

### 5. Secret / IAM 권한 분리 1차 실증

1. MarketConnector EC2 에서 preprocessor secret 조회 시도: 실패(예상 동작)
   1) 호출 — `/portfolio/paper/rds/preprocessor-app` `GetSecretValue`
   2) 응답 — `AccessDeniedException` 확인
   3) principal — `portfolio-paper-marketconnector-ec2-role`
   4) 원인 — MarketConnector EC2 role 에 preprocessor secret read 권한이 없기 때문(OD-SEC-006 정합 — service prefix 분리)
2. 판단:
   1) 장애가 아니라 MS 별 Secret 접근 분리(OD-SEC-006 / 03 §13)가 정상 동작한 것으로 판단
   2) MarketConnector EC2 role 에 preprocessor secret read 권한을 **추가하지 않음**
   3) preprocessor DB 확인은 (a) preprocessor ECS Task(`portfolio-paper-preprocessor-task-role`) 또는 (b) 운영자 로컬 PC 의 SSM Port Forwarding(OD-NET-010 / OD-NET-011) 으로 수행
3. 추가 기록 (운영자 로컬 PC 도구 환경):
   1) RDS port-forwarding 은 SSM `StartPortForwardingSessionToRemoteHost` 로 가능
   2) PowerShell 프롬프트 `>>` 를 명령에 같이 붙여넣으면 `StreamAlreadyRedirected` 오류 발생 — 실제 command 에는 `>>` 를 포함하지 않아야 함(운영자 로컬 PC tip)
4. IAM 변경 0건: 본 일자에 MarketConnector EC2 role / preprocessor task role / crawler task role 권한 변경 없음. AccessDenied 발생은 정책 정합성 1차 실증으로만 기록.

### 6. Backend AWS E2E dry-run 17단계 진행 상태 (2026-06-15 시점)

본 표는 로컬 View Daily Batch 17단계 순서를 기준으로 본 일자 backend AWS 측 실행 상태를 정리한 dry-run 점검표다. 실제 BUY / SELL / `--execute` 주문 전송 계열은 모두 0건(OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 / R-AUTO-010 / R-AUTO-011 정합).

| # | Step | 본 일자 상태 | 비고 |
|---|------|-------------|------|
| 1 | CONNECTOR_BALANCE | 완료 | §2 / MarketConnector EC2 / KIS balance API 200 / snapshot 저장 |
| 2 | INTEREST_CRAWLER | 부분 완료 / follow-up 승격 | §3 / KRX GUI worker 완료 / non-GUI raw 최신성 부족 |
| 3 | PREPROCESSOR | 실행 완료 (데이터 최신성 제약) | §4 / ECS Task exitCode 0 / DB `updated_at` 갱신 / 신규 feature date 0건 |
| 4 | BACKTEST_RESEARCH | 미진행 | 2026-06-15 §1 ~ §7(09 spec) 와 별개 — 본 dry-run 흐름에서는 raw 최신성 회복 후 재실행 예정 |
| 5 | BACKTEST_REPORT | 미진행 | 동상 |
| 6 | DAILY_BUY_SIGNAL | 미진행 | Research 가 Decision 보다 먼저 실행되도록 순서 유지(본 일자 안전 기준) |
| 7 | DAILY_POSITION_SIGNAL | 미진행 | 동상 |
| 8 | DAILY_BUY_EXECUTION | 미진행 / dry-run skip 예정 | 안전 기준 — `--execute` 주문 전송 계열 실행 금지 |
| 9 | DAILY_SELL_EXECUTION | 미진행 / dry-run skip 예정 | 동상 |
| 10 | DAILY_AUTO_SELL | 미진행 / dry-run skip 예정 | 동상 |
| 11 | DAILY_AUTO_BUY | 미진행 / dry-run skip 예정 | 동상 |
| 12 | MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE | 미진행 / dry-run skip 예정 | R-AUTO-009 / R-AUTO-010 / R-AUTO-011 정합 — paper 운영 환경 활성화 보류 |
| 13 | CONNECTOR_ORDER_CHECK | 미진행 / dry-run skip 예정 | fill / position sync 자동 재시도 금지(OD-SAFE-004) |
| 14 | SYNC_SELL_FILL | 미진행 / dry-run skip 예정 | 동상 |
| 15 | SYNC_BUY_FILL | 미진행 / dry-run skip 예정 | 동상 |
| 16 | SYNC_BUY_POSITION | 미진행 / dry-run skip 예정 | 동상 |
| 17 | BALANCE_REFRESH | 미진행 | step 17 위치 유지(View Daily Batch 17단계 순서 정합) |

### 7. 본 일자 안전 / 문서 기록 점검

1. 실제 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS endpoint hostname / RDS password / IAM access key id / account-id / 실제 ARN / image digest / instance-id / task ARN 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
2. KIS Secret JSON key → 환경변수 mapping(`APP_KEY` → `KIS_APP_KEY` / `APP_SECRET` → `KIS_APP_SECRET` / `PAPER_ACNT` → `KIS_PAPER_ACNT` / `ACNT_PRDT_CD` → `KIS_ACNT_PRDT_CD`) 은 mapping 사실만 기록 / 값 미기록.
3. Kiro 본 일자 작업으로 인한 AWS / SSM / EC2 / ECS / Batch / IAM / Secrets Manager / RDS / GRANT 변경 0건. 모든 실제 작업은 운영자가 직접 수행했고 Kiro 는 절차 / 결과 / 실패 사유 / 조치를 문서로만 정리.
4. Kiro 본 일자 작업으로 인한 8개 MS(`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 미수정.
5. broker / KIS 호출 — `CONNECTOR_BALANCE` 한정 조회성 호출만 발생(KIS balance API status 200 / 모의투자 잔고 조회). 신규 주문 / 매수 / 매도 / 취소 / 정정 / `--execute` 0건. fill / position sync 자동 재시도 0건.
6. RDS DDL 0건 / DML 은 `connector_balance_snapshot` / `connector_position_snapshot` insert + preprocessor pipeline 의 정상 흐름 한정. 직접 SQL 변경 0건.
7. aws-live 작업 0건 — 본 일자는 `aws-paper` 한정.
8. CloudWatch Logs 본문 / Selenium / Chrome 의 stdout / stderr / SSM 응답 본문 / docker build 로그 전문은 본 노트에 인용 0건.
9. wrapper 로그(`krx_worker_daily_*.log`) 본문 전체는 본 노트에 미기록 — 사실(로그 파일명 / `[Collected Date] None` 출력 / `KRX login` 성공 여부) 만 기록.

### 8. 후속 인계

1. non-GUI Interest Crawler 운영 실행 경로 정리 — `interest_crawler_daily.py` 에서 KRX GUI 단계 제외한 실행 경로 분리 + ECS Fargate 용 non-GUI crawler command 분리(task 58 후속)
   1) 대상 후보 — `interest_news.py` / `interest_agency.py` / `interest_foreignindex.py` / `interest_commodity.py` / `interest_macroeconomic.py` / `interest_price.py` / `interest_investorflow.py` / `interest_marketbreadth.py`
   2) 제외 후보 — `interest_krx_login_new.py` / `interest_program.py` / `interest_shortsell.py` / `interest_ticker_value.py`
2. raw 최신성 회복 — `interest_price_raw` / `interest_investorflow_raw` / `interest_marketbreadth_raw` / `interest_commodity_raw` / `interest_foreignindex_raw` / `interest_news_raw` / `interest_agency_raw` 직전 거래일 적재
3. preprocessor 재실행 — raw 최신성 회복 후 `portfolio-paper-interest-preprocessor` 재실행 → exitCode 0 확인 → feature table max date / `updated_at` 확인 → 신규 feature date 생성 여부 확인
4. Backend E2E dry-run 재개 — `BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL` 순서로 진행. Research 는 Decision 보다 먼저 실행. 주문 전송 / execution 계열은 안전 기준에 따라 skip 또는 dry-run 만 수행
5. 표현 통일 — 기존 문서에서 "Interest Crawler 완성: 완료" / "Interest Crawler 는 hybrid execution model 기준으로 1차 완성" 표현은 본 일자 결정 정합으로 보정(WORKLOG.md / followups-overview.md / operator-decisions.md OD-MS-020 정합)
6. 본 일자 dry-run 결과는 실패가 아니라 **stale raw data 이슈를 조기에 발견한 검증 성공** 으로 기록 — Connector Balance / Preprocessor ECS 실행 경로 성공 + Interest Crawler 재분류 + non-GUI daily 운영 후속 분리

## 2026-06-16 Crawler 데이터 미수집 해결 + KRX EC2 자동화 성공

**Summary** — 2026-06-15 stale raw data 이슈 해소 + Hybrid execution model 1차 자동화 진입점 보강.

작업 범위:

- (a) Crawler 데이터 미수집 원인 진단(rev6 = Selenium / Chrome smoke command / 원본 `interest_crawler_daily.py` KRX GUI 단계 포함 / non-GUI orchestration 부재)
- (b) non-GUI crawler ECS / Fargate 운영 경로 신규 생성 + RunTask 성공(`portfolio-paper-interest-crawler:7` / `paper-20260616-nongui`)
- (c) raw 최신성 회복(non-GUI 6종 + KRX 2종 + news / agency)
- (d) Windows EC2 worker Autologon bootstrap + Administrator interactive session 실증
- (e) SSM RunCommand → `schtasks /Run` → Scheduled Task 흐름으로 KRX login / program / shortsell 2026-06-15 적재 성공

안전:

- Kiro: 문서 / 절차 / 검증 항목 정리만. 운영자: 실제 AWS / SSM / EC2 / ECS / ECR / Docker / IAM / Secrets Manager / RDS 작업 직접 수행
- 실제 BUY / SELL / `--execute` 주문 전송 계열 0건(OD-SAFE-001 ~ OD-SAFE-004 / OD-MS-021 / R-AUTO-009 ~ R-AUTO-011 정합)

### 1. Crawler 데이터 미수집 원인 확인

1. ECS / Fargate revision 6 의 실제 동작 재확인: 완료
   1) `portfolio-paper-interest-crawler:6` 의 `command` 가 실제 daily raw 수집이 아니라 Selenium / Chrome / outbound smoke command 였음(2026-06-13 §5 정합)
   2) revision 6 RunTask 가 통과해도 `interest_*_raw` 직전 거래일 적재로 이어지지 않음
   3) 표현 보정 정합 — "ECS · Fargate crawler smoke 검증 완료" 와 "non-GUI daily raw 수집 운영 경로 도달" 은 다른 단계임을 1차 실증
2. 원본 `interest_crawler_daily.py` 의 ECS / Fargate 직접 실행 부적합 확인: 완료
   1) KRX GUI 단계(`interest_krx_login_new` / `interest_program` / `interest_shortsell`) 포함
   2) ECS / Fargate Task 의 Display 미지원 / Chrome download 폴더 / KRX OTP / 세션 stateful 의존성으로 단독 실행 부적합
   3) 결론: ECS / Fargate 단독 실행 대상에서 제외 결정 유지(2026-06-13 §5 / 2026-06-15 §3 정합)
3. non-GUI orchestration / Task Definition 부재가 핵심 원인으로 정리: 완료
   1) revision 6 = smoke 전용
   2) 실제 daily raw 수집용 운영 Task Definition 미존재
   3) 따라서 본 일자 1차 작업 = non-GUI 전용 orchestration 신규 생성 + 운영용 Task Definition 등록 + RunTask 검증

### 2. non-GUI crawler ECS / Fargate 운영 경로 생성

1. `interest_crawler_daily_nongui.py` 신규 생성: 완료
   1) 생성 방식: Python patch script 방식(`encoding="utf-8"` 저장 / 한글 literal 훼손 방지 / 2026-06-12 §6 정합)
   2) 정적 검증: `py_compile` 통과 + AST import 검증 통과
   3) Docker image 내부 파일 포함 확인 통과
   4) 본 파일 본문 전체는 본 노트 / spec 산출물에 평문 인용하지 않음(R-DOCS-001 정합 — 사실 기록 한정)
2. KRX GUI 계열 제외(3종): 완료
   1) `interest_krx_login_new`
   2) `interest_program`
   3) `interest_shortsell`
3. non-GUI 계열 포함(8종): 완료
   1) `interest_news`
   2) `interest_agency`
   3) `interest_foreignindex`
   4) `interest_commodity`
   5) `interest_macroeconomic`
   6) `interest_price`
   7) `interest_investorflow`
   8) `interest_marketbreadth`
4. Docker rebuild + ECR push: 완료
   1) image tag: `paper-20260616-nongui`
   2) image digest: 운영자 직접 확인 (실값은 본 노트 미기록 — `<image-digest>` placeholder)
   3) ECR repository: `portfolio-interest-crawler`(2026-06-10 §1 그대로 재사용 / 환경 미분리 정책 정합)
5. ECS Task Definition 등록: 완료
   1) family: `portfolio-paper-interest-crawler`
   2) revision: 7 (6 = Selenium Chrome smoke 전용 / 7 = non-GUI daily 운영용으로 분리)
   3) image: `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:paper-20260616-nongui`
   4) launch type: FARGATE / awsvpc
   5) cpu: 1024 / memory: 2048
   6) command: `python` / `interest_crawler_daily_nongui.py`
   7) Log group: `/portfolio/paper/crawler`
   8) Log stream prefix: `ecs-crawler-nongui-daily`
   9) crawler-app DB Secret 환경변수 주입 유지(2026-06-10 §6 그대로)
6. RunTask 결과: 완료
   1) cluster: `portfolio-paper-cluster`
   2) Task Definition: `portfolio-paper-interest-crawler:7`
   3) RunTask submit: 성공
   4) failures: 0건
   5) 최종 task status: `STOPPED`
   6) stopCode: `EssentialContainerExited`
   7) container exitCode: `0`
   8) 실행 시간: 약 9분 51초
   9) CloudWatch log stream 생성 확인 — 본문 전체 평문 인용 0건(R-DOCS-001 정합)
   10) 전체 step SUCCESS 확인(8종 step 모두): `interest_news` / `interest_agency` / `interest_foreignindex` / `interest_commodity` / `interest_macroeconomic` / `interest_price` / `interest_investorflow` / `interest_marketbreadth`

### 3. raw 최신성 회복 (DB 검증)

1. `interest_price_raw`: 완료
   1) max date 변화: 2026-06-08 → 2026-06-15
   2) row count 변화: 1,207,904 → 1,209,624
   3) 2026-06-15 = 324 Price Collected
2. `interest_investorflow_raw`: 완료
   1) max date 변화: 2026-06-08 → 2026-06-15
   2) row count 변화: 274,204 → 275,949
   3) 2026-06-15 = 349 Investor Collected
3. `interest_marketbreadth_raw`: 완료
   1) max date 변화: 2026-06-08 → 2026-06-15
   2) row count 변화: 4,788 → 4,793
4. `interest_commodity_raw`: 완료
   1) max date 변화: 2026-06-08 → 2026-06-15
   2) row count 변화: 29,132 → 29,162
   3) 2026-06-15 = 6 Commodity Collected
5. `interest_foreignindex_raw`: 부분 완료(non-blocker 후보)
   1) max date 변화: 2026-06-08 → 2026-06-15
   2) row count 변화: 33,738 → 33,766
   3) 2026-06-15 일부 지수 NULL Data 확인 — `HANGSENG` / `NIKKEI225` / `SHANGHAI`
   4) 전체 실패가 아니라 일부 지수 데이터 공백으로 분리 — Preprocessor blocker 가 아닌 후속 점검 후보로 기록
6. `interest_news_raw`: 완료
   1) max date 변화: 2026-06-11 → 2026-06-16
   2) row count 변화: 68,881 → 71,614
   3) 2026-06-16 = 349 News Collected
7. `interest_agency_raw`: 완료
   1) max date 변화: 2026-06-11 → 2026-06-16
   2) row count 변화: 49,018 → 49,044
   3) 2026-06-16 = 15 Agency Reports Collected
8. `interest_macroeconomic_raw`: 완료
   1) max date 변화: 2026-06-08 → 2026-06-15
   2) row count 변화: 75,742 → 75,777
   3) 2026-06-15 = 7 Macro Collected
9. 완료 판단:
   1) news / agency: 2026-06-16 최신화
   2) price / investorflow / marketbreadth / commodity / foreignindex / macro: 2026-06-15 최신화
   3) stale raw data 주요 원인 해소(2026-06-15 §3 / R-DATA-009 / R-DATA-010 mitigation 정합)
   4) Preprocessor 입력 데이터로 사용 가능한 수준 도달(재실행은 후속 §5 / task 77)

### 4. KRX EC2 worker 자동화 재검증

1. SSM direct wrapper / Python 실행 부적합 재확인: 완료
   1) 2026-06-13 §2 그대로 — SSM RunCommand 는 Session 0 / SYSTEM 계정 비대화형 실행
   2) Chrome GUI 가 Administrator RDP 화면에 표시되지 않음
   3) KRX 로그인 / nos_setup / 키보드보안 / iframe 제약으로 headless / 비대화형 수집 부적합
   4) 결정: SSM direct Python 실행은 운영 방식에서 제외 유지(2026-06-13 OD-MS-015 정합)
2. Headless / 비대화형 KRX 수집 제외: 완료
   1) 로컬 검증상 KRX headless / 비대화형 수집은 불가능한 방향으로 판단
   2) 운영 방식에서 제외(이전 "장기 후보" 표현은 본 일자에 "로컬 검증상 제외 / 운영 방식에서 제외" 로 보정)
3. Autologon bootstrap: 완료
   1) Microsoft Sysinternals Autologon 사용
   2) Administrator 자동 로그인 설정 완료
   3) EC2 재부팅 후 SSM managed instance Online 확인
   4) `query user` 출력으로 Administrator console session Active 확인
       - USERNAME: `administrator`
       - SESSIONNAME: `console`
       - ID: `1`
       - STATE: `Active`
   5) `whoami` 출력은 `nt authority\system` — SSM RunCommand 자체가 SYSTEM 으로 실행되기 때문에 정상
   6) 필요한 조건은 별도 Administrator console interactive session 존재였고 Active 확인 통과
   7) Autologon 자격 증명(DefaultUserName / DefaultPassword) 본 노트 평문 기록 0건(R-DOCS-001 / R-SEC-009 정합)
   8) Autologon 사용은 paper 전용 Windows worker 한정 보안 예외(R-SEC-009 신규)
   9) Administrator / KRX / DB password 본 노트 평문 기록 0건
4. 최종 자동화 실행 경로 1차 실증: 완료
   1) SSM RunCommand
   2) `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"`
   3) Windows Scheduled Task
   4) Administrator console interactive session
   5) `powershell.exe -ExecutionPolicy Bypass -File C:\portfolio\run_krx_worker_daily.ps1`
   6) `python interest_krx_login_new.py`
   7) `python interest_program.py`
   8) `python interest_shortsell.py`
5. Scheduled Task 결과: 완료
   1) TaskName: `Portfolio-KRX-Worker-Daily`
   2) Logon Mode: `Interactive only`
   3) Run As User: `Administrator`
   4) `schtasks /Run` 응답: `SUCCESS: Attempted to run the scheduled task "Portfolio-KRX-Worker-Daily"`
   5) 실행 중 상태: Status `Running` / Last Result `267009`
   6) 실행 완료 상태: Status `Ready` / Last Result `0`
   7) Last Run Time: 2026-06-16 04:55:49
   8) wrapper local log: `C:\portfolio\logs\krx_worker_daily_20260616_045550.log`
6. KRX worker log 결과: 완료(파일 본문 전체 평문 인용 0건)
   1) KRX login 성공: `KRX ID/PW Login Success` / `KRX Login Ready` (elapsed 92.83s)
   2) `interest_program` 성공: Collected Date 2026-06-15 / 2026-06-15 Collected
   3) `interest_shortsell` 성공: Collected Date 2026-06-15 / 2026-06-15 349 Company Collected
   4) wrapper 완료: `DONE :: KRX worker daily`
   5) Python process 종료 확인
   6) Chrome process 잔존 확인 — KRX debug attach 구조상 잔존 가능 / 후속 정리 옵션 분리(R-AUTO-017 신규)
7. KRX DB 최신성 확인: 완료
   1) `interest_program_raw`: max date 2026-06-15 / row count 547 → 548
   2) `interest_shortsell_raw`: max date 2026-06-15 / row count 190,554 → 190,903
   3) 2026-06-15 데이터 신규 생성 확인
   4) 중복 insert / upsert 동작은 정상 범위로 판단

### 5. 최종 판단 / 후속 인계

1. Crawler hybrid 구조 완료 판단: 완료
   1) non-GUI = ECS / Fargate Task Definition revision 7 운영 경로 생성 + RunTask 성공 + raw 최신성 회복
   2) KRX GUI = Windows EC2 worker + Autologon + Administrator interactive session + Scheduled Task + SSM trigger 1차 자동화 흐름 실증
   3) Preprocessor = ECS / Fargate Task 유지(2026-06-10 §6 / 2026-06-15 §4 정합)
   4) Crawler 데이터 미수집 해결 완료
2. KRX GUI 의존 수집은 Windows EC2 worker 경로로 분리 확정: 완료(OD-MS-011 / OD-MS-022 정합)
3. non-GUI ECS / Fargate rev7 과 KRX EC2 worker 의 역할 분리 확정: 완료
4. Preprocessor ECS 재실행 가능 상태 도달: 완료(재실행 자체는 task 77 후속 — "재실행 가능 상태" 까지만 본 일자 기록)
5. 표현 통일(OD-MS-020 정합):
   1) "Crawler 데이터 미수집 해결: 완료"
   2) "Interest Crawler hybrid 구조 완료" (non-GUI rev7 운영 경로 생성 + RunTask 성공 + raw 최신성 회복 / KRX GUI worker 운영 가능 + 자동화 진입점 1차 실증)
   3) "Preprocessor: raw 입력 데이터 회복 후 ECS 재실행 가능 상태"
   4) "Backend AWS E2E dry-run: 후속 재개 예정"
   5) "View 구현: 후속 예정"
6. 후속 인계:
   1) Preprocessor ECS 재실행(raw 최신성 회복 입력 기반 / feature table max date / `updated_at` / 신규 feature date 생성 여부 확인 / R-DATA-010 mitigation 정합 / task 77)
   2) Backend AWS E2E dry-run 재개(`BACKTEST_RESEARCH` → `BACKTEST_REPORT` → `DAILY_BUY_SIGNAL` → `DAILY_POSITION_SIGNAL` 순서 / Research 우선 / 주문·execution 계열 skip 또는 dry-run / OD-MS-021 정합)
   3) `interest_foreignindex_raw` 의 HANGSENG / NIKKEI225 / SHANGHAI NULL Data 후속 점검(non-blocker 후보)
   4) EventBridge Scheduler → SSM RunCommand → `schtasks /Run` 정기 trigger 연계(task 54)
   5) Step Functions hybrid orchestration(task 55)
   6) CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집(task 56)
   7) wrapper 내 DB 검증 출력 자동 추가(task 57 / R-AUTO-007 정합)
   8) EC2 worker 작업 완료 후 stop 절차 명시(task 59)
   9) Chrome process 정리 옵션(R-AUTO-017 mitigation / task 90 신규)
   10) View 구현(05 spec 후속 분리)

### 6. 본 일자 안전 / 문서 기록 점검

1. 실제 secret value / Administrator password / Autologon DefaultPassword / KRX 로그인 password / RDS password / RDS endpoint hostname / KIS app key / KIS app secret / 계좌번호 / token / account-id / 실제 secret ARN / 실제 IAM Role ARN / image digest full sha256 / IAM access key id / instance-id / task ARN / Task Definition ARN 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
2. KRX 로그인 ID / password / Administrator 자격 증명 / Autologon 자격 증명은 "Secrets Manager 또는 Sysinternals Autologon 으로 주입" 로만 표기. 값 미기록.
3. Kiro 본 일자 작업으로 인한 AWS / SSM / EC2 / ECS / ECR / Docker / IAM / Secrets Manager / RDS / GRANT 변경 0건. 모든 실제 작업은 운영자 직접 수행.
4. Kiro 본 일자 작업으로 인한 8개 MS(`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 미수정.
5. 운영자가 직접 작성 / 수정한 `port-interest-crawler/interest_crawler_daily_nongui.py` 신규 파일 / Dockerfile 변경분 / requirements.txt 변경분(있는 경우) 은 본 노트에 사실로만 기록 — 본문 전체 인용 0건.
6. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 호출 0건. 신규 BUY / SELL / 취소 / 정정 / `--execute` 0건. fill / position sync 자동 재시도 0건(OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-009 / R-AUTO-010 / R-AUTO-011 정합).
7. RDS DDL 0건 / DML 은 crawler raw insert / KRX program / shortsell insert / preprocessor pipeline 의 정상 흐름 한정. 직접 SQL 변경 0건.
8. aws-live 작업 0건 — 본 일자는 `aws-paper` 한정.
9. CloudWatch Logs 본문 / wrapper 로그(`krx_worker_daily_20260616_045550.log`) / SSM 응답 본문 / docker build 로그 / Selenium · Chrome stdout · stderr 전문 평문 인용 0건.
10. Autologon 사용은 paper 전용 Windows worker 한정 보안 예외(R-SEC-009) — 운영자 노트 / spec 산출물에서 자동 로그인 자격 증명 평문 기록 금지 정책 1차 실증.

## 2026-06-17 Daily AWS 17-step E2E 완료 (Interest Crawler + Preprocessor)

**Summary** — Daily AWS 17-step E2E 흐름이 본 일자에 끝까지 연결됨.

- 본 spec 범위 step: 2번 `INTEREST_CRAWLER` / 3번 `PREPROCESSOR`
- 17 step 전체 진행 상태: 03 / 04 / 09 spec operation-notes 의 2026-06-17 섹션 참조
- Kiro: 문서 / 절차 정리만. 운영자: 실제 ECS RunTask / SSM RunCommand / Windows EC2 worker 작업 직접 수행
- 안전: `aws-paper` 한정 / aws-live 작업 0건 / hybrid 구조 1차 실증 정합(OD-MS-011 / OD-MS-022 정합)

### 1. Step 2 `INTEREST_CRAWLER`

1. non-GUI ECS Fargate crawler RunTask: 성공
   1) Task Definition: `portfolio-paper-interest-crawler:7`(2026-06-16 §1 정합 / 본 일자 동일 revision 재사용)
   2) launch type: FARGATE / awsvpc / public subnet + `assignPublicIp = ENABLED`(OD-NET-004 정합)
   3) command: `interest_crawler_daily_nongui.py`
   4) lastStatus: `STOPPED` / stopCode: `EssentialContainerExited` / exitCode: `0`
   5) 전체 non-GUI step SUCCESS — `interest_news` / `interest_agency` / `interest_foreignindex` / `interest_commodity` / `interest_macroeconomic` / `interest_price` / `interest_investorflow` / `interest_marketbreadth`
2. KRX Windows EC2 worker Scheduled Task: 성공
   1) SSM RunCommand → `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"` 흐름(OD-MS-022 정합)
   2) Windows Scheduled Task → Administrator console interactive session → `run_krx_worker_daily.ps1` 실행
   3) KRX login 성공 / `interest_program_raw` / `interest_shortsell_raw` 2026-06-16 적재 확인
3. raw 최신성 확인: 완료
   1) `interest.interest_program_raw` 최신일 `2026-06-16` 확인
   2) `interest.interest_shortsell_raw` 최신일 `2026-06-16` 확인
   3) non-GUI raw 6종 + news / agency 도 직전 거래일까지 적재(R-DATA-009 / R-DATA-010 mitigation 1차 실증 / 2026-06-16 §3 정합)
4. crawler worker stop 요청: 완료
   1) Windows EC2 worker 작업 완료 후 stop 요청 운영자 직접 수행 — idle 비용 절감(R-SEC-009 mitigation 정합 / Autologon 노출 시간 최소화)
   2) 다음 실행은 SSM Online + Administrator session Active pre-check 후 schtasks trigger(OD-MS-022 / R-AUTO-016 mitigation 정합)
5. KRX GUI 수집 운영 방식 유지: 확인
   1) KRX GUI = Windows interactive desktop session 기반 유지(OD-MS-022 정합)
   2) Headless / 비대화형 KRX 수집은 운영 방식에서 제외(OD-MS-022 정합)
   3) non-GUI crawler 와 KRX GUI worker 는 Hybrid 구조로 문서화(OD-MS-011 / OD-MS-020 정합)

### 2. Step 3 `PREPROCESSOR`

1. ECS Fargate RunTask: 성공
   1) Task Definition: `portfolio-paper-interest-preprocessor:1`(2026-06-10 §6 정합 / 본 일자 동일 revision 재사용)
   2) launch type: FARGATE / awsvpc / public subnet + `assignPublicIp = ENABLED`
   3) command: `pre_daily.py` orchestration
   4) lastStatus: `STOPPED` / stopCode: `EssentialContainerExited` / exitCode: `0`
   5) `PREPROCESSOR PIPELINE END` CloudWatch Logs 출력 확인
2. feature 최신성 확인: 완료
   1) `pre_total.pre_total_market_daily_feature` 최신일 `2026-06-16` 확인
   2) `pre_total.pre_total_stock_daily_feature` 최신일 `2026-06-16` 확인
3. 1차 실패 이슈와 본 step 정합:
   1) 본 일자 17-step E2E 흐름의 1차 blocker 중 하나는 `execution_app` 의 interest schema/table SELECT 권한 누락(R-DATA-005 [2026-06-17 보강] 정합) — 해당 이슈는 **8번 step `DAILY_BUY_EXECUTION`** 영향이며 본 step `PREPROCESSOR` 자체는 정상 완료(R-DATA-005 와 본 step 의 인과관계는 분리되어 있음)
   2) `preprocessor_app` 권한은 본 일자 시점에 정합 — preprocessor pipeline 실행 / DB write 정상 동작
   3) 02 spec / 06 spec operation-notes 2026-06-17 §1 ~ §3 에 `execution_app` interest 권한 보정 / `marketconnector_app` legacy 권한 보정 사실 기록 정합

### 3. 안전 / 보안 점검 결과

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 변경 0건. 운영자가 직접 수행한 SSM RunCommand / Windows EC2 worker stop / KRX wrapper 실행 결과는 본 노트에 사실로만 기록.
2. 아래 민감정보 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder:
   - secret value / Administrator password / KRX 로그인 password / RDS password / RDS endpoint hostname
   - KIS app key / KIS app secret / 계좌번호 / token
   - account-id / 실제 secret ARN / 실제 IAM Role ARN / image digest full sha256 / IAM access key id / instance-id / task ARN

   운영 식별자만 사실 기록: Task Definition family `portfolio-paper-interest-crawler` / `portfolio-paper-interest-preprocessor` / Scheduled Task `Portfolio-KRX-Worker-Daily` / 적재 일자 2026-06-16 / feature 최신일 2026-06-16.
3. AWS / SSM / EC2 / ECS / ECR / Docker / IAM / Secrets Manager / RDS / GRANT 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. `secretsmanager:GetSecretValue` 결과값 평문 기록 0건. CloudWatch Logs 본문 / wrapper 로그(`krx_worker_daily_*.log`) / SSM 응답 본문 / Selenium · Chrome stdout 본 노트 평문 인용 0건.
4. broker / KIS / 주문 / 체결 / Daily Batch entrypoint 본 spec 범위 직접 호출 0건. RDS DDL 0건. DML 은 본 일자 17-step 정상 흐름 한정 — `interest.interest_*_raw` insert / `pre_total.pre_total_*_feature` insert / `pre_*.*_feature` insert.
5. crawler worker stop 요청은 운영자 직접 작업으로 수행 — Autologon 노출 시간 최소화(R-SEC-009 mitigation 정합). 다음 실행 시 EC2 start 후 Administrator console session Active pre-check 흐름 정합(R-AUTO-016 mitigation 정합).
6. live 자동 crawler / preprocessor 실행은 후속 검증 / 승인 전까지 여전히 금지. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건. Daily AWS 17-step E2E paper 1차 통과로 backend AWS E2E 의 2번 / 3번 step 정합 — OD-MS-011 / OD-MS-020 / OD-MS-022 mitigation 1차 실증 / Status 기존 값 그대로 유지.

## 2026-06-20 AWS 자동 Wrapper 최종 확인 + 6/18 중복 실행 시도 안전 중단 + 6/19 KRX raw 최신성 복구 상태

**Summary** — Daily wrapper 구조 / 안전 기준 / EC2 기동 기준 최종 점검 + 6/18 중복 실행 시도 안전 중단.

- (a) Daily AWS Paper Wrapper 구조 / 안전 기준 / EC2 기동 기준 최종 점검
- (b) RunDate `2026-06-18` / Step 1 ~ Step 11 범위 wrapper 재실행 시도 안전 중단(Step 12 미실행 확인)
- (c) 6/19 KRX raw DB 최신성 복구 상태 점검
- Kiro: 문서 / 절차 정리만. 운영자: 실제 AWS / SSM / EC2 / RDS 작업 직접 수행
- 안전: `aws-paper` 한정 / aws-live 작업 0건 / 신규 broker · KIS 주문 제출 0건 / `--execute` 호출 0건

### 1. Daily AWS Paper Wrapper 최종 점검 (구조 / 안전 기준 / EC2 기동 기준): 완료

1. 구조 확인: 완료
   1) main wrapper `C:\Workspaces\port-view\.kiro\scripts\run-daily-aws-paper.ps1`
   2) config `C:\Workspaces\port-view\.kiro\scripts\daily-aws-paper.config.ps1`
   3) functions `C:\Workspaces\port-view\.kiro\scripts\daily-aws-paper.functions.ps1`
   4) step 파일 17개 `C:\Workspaces\port-view\.kiro\scripts\steps\step-01 ~ step-17`
2. 안전 기준 확인: 완료
   1) Step 1 ~ Step 11 은 broker / KIS 주문 제출 전 단계
   2) Step 12 (`MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) 만 실제 KIS paper 주문 제출 가능 step
   3) Step 12 는 `-AllowPaperOrderExecute` 미명시 시 차단(OD-MS-023 / R-AUTO-019 mitigation 정합)
   4) 토요일 / 휴장일 Step 12 실제 주문 제출 제외(OD-SAFE-001 ~ OD-SAFE-004 / R-AUTO-002 정합)
3. EC2 기동 기준 확인: 완료
   1) MarketConnector EC2 = Step 1 / Step 12 / Step 13 / Step 17 에서 필요
   2) Crawler Worker EC2 = Step 2 KRX GUI worker 실행 시 필요
   3) 두 EC2 가 stopped 상태이면 wrapper 실행 전 start 필요
   4) EC2 stop / start 후 MarketConnector EC2 의 `/tmp/inject-env.sh` 가 유실될 수 있음(본 일자 1차 실증)
   5) wrapper 에 필요한 EC2 만 start 하고 종료 후 stop 하는 lifecycle 보강 필요(후속 분리)

### 2. 6/18 wrapper 중복 실행 시도 안전 중단: 완료

1. 실행 상황: 확인
   1) 2026-06-20 에 RunDate `2026-06-18` / Step 1 ~ Step 11 범위로 wrapper 재실행 시도
   2) Step 1 `CONNECTOR_BALANCE` 최초 실패 — 원인 = MarketConnector EC2 의 `/tmp/inject-env.sh` 유실(EC2 stop / start 영향)
   3) MarketConnector EC2 에 `/tmp/inject-env.sh` 재생성 후 Step 1 재실행 통과
   4) Step 2 `INTEREST_CRAWLER` non-GUI ECS task exitCode 0 확인 / Step 2 KRX GUI worker Scheduled Task trigger 성공 확인
   5) 기존 6/18 실행 이력 중복 가능성 인지 후 Step 3 `PREPROCESSOR` 진입 직후 Ctrl+C 로 wrapper 중단
2. 안전 / AWS 작업 잔여 여부 확인: 완료
   1) ECS RUNNING task 0건
   2) AWS Batch RUNNING / SUBMITTED / PENDING / RUNNABLE job 0건
   3) KRX `Portfolio-KRX-Worker-Daily` Scheduled Task State `Ready` / LastTaskResult 0
   4) Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` 미실행 확인
3. DB 중복 여부 확인: 완료
   1) 2026-06-18 기존 `execution_plan_id 94` 정상 완료 이력(BUY 4건 / FILLED 4건 / connector linked 4건)
   2) 2026-06-20 04:20 UTC 이후 신규 execution_plan 0건
   3) 신규 strategy_execution_order 0건
   4) 신규 connector_order_request 생성 없음
   5) KIS 신규 주문 제출 0건
4. 종료 처리: 완료
   1) Crawler Worker chrome 잔여 프로세스는 EC2 stop 으로 정리(R-AUTO-017 mitigation 정합)
   2) MarketConnector EC2 / Crawler Worker EC2 stop 처리

### 3. 6/19 KRX raw 최신성 복구 상태 점검: 완료

1. `interest_program_raw`: 확인
   1) max_date `2026-06-19`
   2) 2026-06-18 row_count `1`
   3) 2026-06-19 row_count `1`
2. `interest_shortsell_raw`: 확인
   1) max_date `2026-06-19`
   2) 2026-06-18 row_count `349`
   3) 2026-06-19 row_count `349`
3. 결론: 확인
   1) KRX raw 기준 최신성은 `2026-06-19` 까지 복구 완료
   2) Windows EC2 worker 자동화는 Scheduled Task trigger / LASTEXITCODE 중심 성공판정 한계(R-AUTO-007 정합)
   3) Step 2 wrapper 성공판정 강화 필요로 판단(2026-06-21 작업으로 분리)

### 4. 본 일자 범위 밖 / 후속 인계: 정리

1. EC2 lifecycle 자동 start / stop 보강(MarketConnector EC2 stop / start 후 `/tmp/inject-env.sh` 유실 대응 / SSM Online wait 포함): 후속 분리
2. Step 12 실제 paper 주문 제출은 별도 승인 전까지 실행하지 않음(OD-SAFE-002 / OD-SAFE-003 / R-AUTO-002 정합)
3. Step 2 wrapper 성공판정 강화 — Chrome reset / Scheduled Task wait + Last Result / latest worker log / KRX raw DB validation: 2026-06-21 분리(본 노트 다음 섹션)
4. Step Functions 에서 ECS RunTask + SSM RunCommand 혼합 orchestration: 후속 유지
5. View Daily Batch 화면 연동 / worker log centralized collection: 후속 유지

### 5. 안전 / 보안 점검 결과 (2026-06-20)

1. broker / KIS / 신규 주문 호출 0건. `--execute` 실호출 0건. SELL / 취소 / 정정 호출 0건. fill · position sync 자동 재시도 0건. aws-live 작업 0건.
2. RDS DDL 0건. DML 은 검증 SQL SELECT 한정(`interest_program_raw` / `interest_shortsell_raw` / `strategy_execution_plan` / `strategy_execution_order` / `connector_order_request` row_count + max_date 점검).
3. 실제 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / EIP / image digest full sha256 / task ARN / job ARN / KIS paper login credential / Administrator password 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
4. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역). AWS / SSM / EC2 / RDS 호출은 모두 운영자 직접 수행.
5. 운영 식별자(`execution_plan_id 94` / RunDate `2026-06-18` / `interest_program_raw` · `interest_shortsell_raw` row_count + max_date / Scheduled Task 이름 `Portfolio-KRX-Worker-Daily` / wrapper 파라미터 `-StartStep` · `-EndStep`) 는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

## 2026-06-21 Step 2 `INTEREST_CRAWLER` 성공판정 강화 + KRX raw DB validation 연동

**Summary** — Step 2 성공판정 강화 + KRX raw DB validation 연동(OD-MS-026 / R-AUTO-020 신규 mitigation).

작업 범위:

- (a) `step-02-interest-crawler.ps1` 성공판정 강화
- (b) non-GUI ECS crawler env 보강(`TEMP=/tmp` / `TMP=/tmp` / `PYTHONUTF8=1` / `PYTHONIOENCODING=utf-8`)
- (c) `interest_krx_raw_validate_daily.py` 신규 생성 + S3 presigned URL 경유 Windows crawler worker EC2 배포 + EC2 단독 검증
- (d) `step-02-interest-crawler.ps1` DB validation 연동(`INTEREST_CRAWLER_KRX_DB_VALIDATE` SSM step)
- (e) crawler worker stopped fail-closed 처리(skip → fail-closed)
- (f) Step 2 단독 실행 검증

안전:

- Kiro: 문서 / 절차 정리만. 운영자: 실제 코드 작성 / py_compile / S3 업로드 / SSM RunCommand / EC2 단독 검증 / wrapper 단독 실행 직접 수행
- `aws-paper` 한정 / aws-live 작업 0건 / 신규 broker · KIS 주문 제출 0건 / `--execute` 호출 0건

### 1. `step-02-interest-crawler.ps1` 성공판정 강화: 완료

1. KRX worker 실행 전 Chrome / chromedriver best-effort reset: 완료
   1) chrome / chromedriver stale process 정리 추가(R-AUTO-017 mitigation 정합)
   2) reset 실패는 즉시 중단하지 않고 warning 로그만 남김
2. Administrator interactive Scheduled Task 실행 경로 유지: 완료
   1) `Portfolio-KRX-Worker-Daily` Scheduled Task 를 `schtasks /Run` 으로 실행
   2) SSM direct python 실행 채택 거부 / OD-MS-022 / OD-MS-015 정합
   3) KRX GUI 의존 구조를 Windows Administrator interactive session 기준 유지
3. Scheduled Task 종료 대기: 완료
   1) 실행 후 `Running` 상태 polling
   2) `Ready` 상태로 돌아올 때까지 wait
   3) timeout 시 Step 2 실패 처리
   4) `Running` 상태 관측 여부를 `sawRunning` 으로 로그 출력
4. Last Result 확인: 완료
   1) Scheduled Task 종료 후 Last Result 확인
   2) Last Result 가 `0` 또는 `0x0` 이 아니면 Step 2 실패
   3) Scheduled Task trigger 성공만으로 Step 2 SUCCESS 처리 금지(R-AUTO-020 신규 mitigation 정합)
5. 최신 worker log 경로 출력: 완료
   1) `C:\portfolio\logs\krx_worker_daily_*.log` 중 최신 파일 확인
   2) latest log path / last write time / size 출력
   3) latest worker log tail 출력
6. crawler worker stopped fail-closed 처리: 완료
   1) 변경 전 동작 — crawler worker EC2 가 `running` 이 아니면 KRX GUI worker skip 가능 / skip 상태에서도 Step 2 SUCCESS 가능성 존재
   2) 변경 후 동작 — crawler worker EC2 가 `running` 이 아니면 즉시 실패 처리 / 실패 메시지에 instanceId / state 출력 / KRX GUI worker · DB validation 미수행 상태에서 Step 2 SUCCESS 진입 차단(R-AUTO-016 mitigation 갱신)

### 2. non-GUI ECS crawler env 보강: 완료

1. ECS RunTask overrides 환경 변수 보강: 완료
   1) `TEMP=/tmp`
   2) `TMP=/tmp`
   3) `PYTHONUTF8=1`
   4) `PYTHONIOENCODING=utf-8`
2. 공통 함수 지원 보강(`daily-aws-paper.functions.ps1`): 완료
   1) `Invoke-DailyAwsPaperEcsTask` `EnvironmentVariables` 파라미터 추가
   2) ECS RunTask `containerOverrides.environment` 전달 추가
   3) `New-SsmParameterFile` `ExecutionTimeoutSeconds` 파라미터 추가
   4) `Invoke-SsmCommandAndWait` `ExecutionTimeoutSeconds` 전달 추가
3. 목적: 확인
   1) Windows / Linux 실행 환경 간 인코딩 차이 완화
   2) crawler 로그 / 파일 처리 시 UTF-8 기준 명확화
   3) 임시 파일 경로 의존성 명시화
   4) KRX worker 및 DB validation 장시간 실행 시 SSM timeout 제어 가능

### 3. `interest_krx_raw_validate_daily.py` 신규 생성 + EC2 배포 + EC2 단독 검증: 완료

1. 신규 파일 생성: 완료
   1) `C:\Workspaces\port-interest-crawler\interest_krx_raw_validate_daily.py`(운영자 직접 작성 / 본 spec 산출물에는 사실 기록만 / R-DOCS-001 정합)
2. 검증 대상: 확인
   1) `interest_program_raw`
   2) `interest_shortsell_raw`
3. 검증 기준: 확인
   1) expected `trade_date` 기준 row_count 확인
   2) `max(trade_date)` 가 expected date 이상인지 확인
   3) 실패 시 exit code 30 반환
4. 로컬 검증: 완료
   1) py_compile 통과
   2) UTF-8 read 확인
   3) program / shortsell / exit30 marker 확인
5. EC2 배포(S3 presigned URL 경유): 완료
   1) S3 업로드 — `s3://portfolio-paper-migration-yukiever/tmp/krx/interest_krx_raw_validate_daily.py`
   2) presigned URL 생성(URL 실값 본 노트 평문 기록 금지 / R-DOCS-001 정합)
   3) SSM RunPowerShellScript 로 Windows crawler worker EC2 다운로드 — 배포 대상 `C:\portfolio\port-interest-crawler\interest_krx_raw_validate_daily.py`
   4) 원격 검증 — 원격 py_compile 통과 / `interest_program_raw` marker 확인 / `interest_shortsell_raw` marker 확인 / `--expected-date` · `--run-date` 인자 확인 / exit code 30 실패 반환 로직 확인 / `sys.exit(run(parsed_args))` 확인
   5) SSM commandId `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3`
6. EC2 단독 검증: 완료
   1) Windows crawler worker EC2 내부에서 실행
   2) `C:\portfolio\load-crawler-db-env.ps1` 로 AWS RDS 접속 env 로드 확인
   3) `C:\portfolio\venvs\interest-crawler` venv 활성화 확인
   4) Python `3.13.5` 실행 확인
   5) DB session — user=`crawler_app` / schema=`interest` / search_path=`interest, reference, legacy, public`
   6) 검증 결과 — `interest_program_raw` expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`1` / `interest_shortsell_raw` expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`349`
   7) validation exit code 0 확인
   8) SSM commandId `c844aea5-1429-430a-9510-39fc99f17f05`

### 4. `step-02-interest-crawler.ps1` DB validation 연동: 완료

1. `ExpectedKrxRawDate` 계산 추가: 완료
   1) RunDate 기준 전 영업일 계산
   2) `ExpectedKrxRawDate` 로그 출력
2. Windows crawler worker EC2 내부 DB 검증 호출 추가: 완료
   1) `C:\portfolio\load-crawler-db-env.ps1` 로 AWS RDS 접속 env 로드
   2) `C:\portfolio\venvs\interest-crawler` venv 활성화
   3) `C:\portfolio\port-interest-crawler\interest_krx_raw_validate_daily.py` 실행 / `--expected-date <ExpectedKrxRawDate>` 전달
3. 실패 판정 강화: 완료
   1) `interest_program_raw` expected date row_count 0 이면 실패
   2) `interest_shortsell_raw` expected date row_count 0 이면 실패
   3) validation exit code non-zero 이면 Step 2 실패
4. 검증 command 추적 추가: 완료
   1) `INTEREST_CRAWLER_KRX_DB_VALIDATE` SSM command id 출력
   2) stdout / stderr log path 출력
   3) step result 에 `KrxDbValidationCommandId` 포함

### 5. Step 2 단독 실행 검증: 완료

1. 실행 조건: 확인
   1) Environment `aws-paper`
   2) RunDate `2026-06-20`
   3) StartStep `2` / EndStep `2`
   4) PaperOrder `False`
   5) RunId `daily-aws-paper-20260621-204017`
2. wrapper 실행 결과: 완료
   1) StepCode `INTEREST_CRAWLER`
   2) Status `SUCCESS`
   3) Runner `ECS+SSM`
   4) ExpectedKrxRawDate `2026-06-19`
3. non-GUI ECS crawler 검증: 완료
   1) taskDefinition `portfolio-paper-interest-crawler:7`
   2) taskId `78979b5cbb714d0eb94f5946e15a14ce`
   3) exitCode 0
   4) stoppedReason `Essential container in task exited`
   5) CloudWatch log 저장 — `C:\Temp\portfolio-daily-aws-paper\daily-aws-paper-20260621-204017\logs\ecs-interest-crawler-nongui-78979b5cbb714d0eb94f5946e15a14ce-cloudwatch.txt`(본 노트에는 파일 경로만 기록 / 본문 인용 0건)
4. KRX GUI worker 검증: 완료
   1) crawler worker state `running`
   2) SSM commandId `f9d82fcc-1e26-4710-87c3-1d20483b63ef`
   3) SSM 결과 `Success` / ResponseCode 0
   4) Scheduled Task 실행 흐름 — `TaskStatus=Running` 확인 → `TaskStatus=Ready` 복귀 / elapsedSeconds=`111` / sawRunning=True
   5) Scheduled Task 최종 상태 — FinalStatus=`Ready` / FinalLastResult=`0` / Last Result=`0`
   6) Scheduled Task 실행 경로 — Logon Mode `Interactive only` / Run As `Administrator` / Task To Run `powershell.exe -ExecutionPolicy Bypass -File C:\portfolio\run_krx_worker_daily.ps1`
   7) latest worker log `C:\portfolio\logs\krx_worker_daily_20260621_114154.log` — KRX login SUCCESS / KRX program SUCCESS / KRX shortsell SUCCESS / `DONE :: KRX worker daily`(본 노트에는 SUCCESS 라벨만 기록 / 본문 평문 인용 0건)
5. KRX raw DB validation 검증: 완료
   1) SSM step code `INTEREST_CRAWLER_KRX_DB_VALIDATE`
   2) SSM commandId `2279c6d7-2da6-4317-9c10-7cc77374b317`
   3) SSM 결과 `Success` / ResponseCode 0
   4) DB session — user=`crawler_app` / schema=`interest` / search_path=`interest, reference, legacy, public`
   5) 검증 결과 — `interest_program_raw` expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`1` / OK / `interest_shortsell_raw` expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`349` / OK
   6) validation exit code 0
   7) stderr empty
6. 최종 판단: 확인
   1) Step 2 단독 실행 검증 성공
   2) Scheduled Task trigger 성공만이 아니라 worker 종료 / Last Result / DB validation 성공까지 확인됨
   3) KRX raw 미적재 상태에서 Step 3 이후로 진행되는 위험을 차단함(R-AUTO-020 mitigation 1차 실증)
   4) Step 3 PREPROCESSOR 이후 단계 진행 가능한 상태이나, 본 일자 작업 범위에서는 진행하지 않음

### 6. 결정 / 리스크 변경 요약: 완료

1. 신규 결정: 완료
   1) OD-MS-026(Step 2 INTEREST_CRAWLER 운영 성공 기준 = Scheduled Task trigger 가 아니라 KRX raw DB validation 까지 / KRX GUI 경로는 Windows Administrator interactive Scheduled Task / wrapper 는 실행 · 종료 대기 · Last Result · latest log · DB validation orchestration 담당 / `interest_krx_raw_validate_daily.py` 가 raw 최신성 검증 담당 / crawler worker stopped 는 fail-closed, 🟡 잠정)
2. 신규 리스크: 완료
   1) R-AUTO-020(Scheduled Task trigger 성공만 보고 Step 2 SUCCESS 처리 시 KRX raw 미적재가 Step 3 이후로 전파될 위험, mitigation = Chrome / chromedriver best-effort reset + Running → Ready wait + Last Result 확인 + latest worker log 출력 + KRX raw DB validation + worker stopped fail-closed, Status `Mitigated`)
3. 본문 변경 없는 리스크: detection / mitigation 메모 보강
   1) R-AUTO-007(wrapper 성공 종료가 실제 DB 적재 성공을 보장하지 못함) — wrapper 안 DB 검증 자동 출력이 본 일자 1차 실증으로 task 57 후속에서 task 단위 완료로 승격(`KrxDbValidationCommandId` step result 추적 / `interest_program_raw` · `interest_shortsell_raw` expected date row_count 자동 확인)
   2) R-AUTO-016(Administrator interactive session 부재 시 KRX GUI 수집 실패) — 이전 "wrapper 안에서 자동 skip" 표현은 본 일자에 fail-closed 로 갱신 / crawler worker EC2 stopped 시 즉시 실패 처리 / instanceId / state 출력 / Step 2 SUCCESS 진입 차단
   3) R-AUTO-017(Chrome process 잔존) — Chrome / chromedriver best-effort reset 1차 실증 / 실패는 warning 으로 진행 / 후속 EC2 stop / restart 절차는 R-AUTO-016 보강과 결합

### 7. 안전 / 보안 점검 결과 (2026-06-21)

1. broker / KIS 호출 0건. `--execute` 실호출 0건. SELL / 취소 / 정정 호출 0건. SELL position `mark_position_sell_ordered()` 호출 0건. fill · position sync 자동 재시도 0건. aws-live 작업 0건.
2. RDS DDL 0건. DML 은 검증 SQL SELECT 한정(`interest_program_raw` / `interest_shortsell_raw` row_count + max_date / DB session user · schema · search_path 점검). 본 일자 신규 Step 2 wrapper 실행에서는 idempotent / no-op.
3. 실제 secret value / KIS app key / KIS app secret / 계좌번호 / 계좌 비밀번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / EIP / image digest full sha256 / task ARN / job ARN / KIS paper login credential / Administrator password / S3 presigned URL 실값 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
4. AWS / SSM / EC2 / S3 / ECS / RDS / KRX 호출은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. AWS CLI / boto3 실행 0건. AWS 리소스 생성 / 수정 / 삭제 0건. CloudWatch Logs 본문 / SSM 응답 본문 / 운영자 PowerShell stdout 전문 평문 인용 0건.
5. 운영자 직접 작성 / patch 한 `port-interest-crawler/interest_krx_raw_validate_daily.py` / `step-02-interest-crawler.ps1` / `daily-aws-paper.functions.ps1` 변경분은 본 노트에 사실로만 기록(본문 전체 인용 0건 / R-DOCS-001 정합). 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역).
6. 운영 식별자만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님:
   - SSM commandId 4종: `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3` / `c844aea5-1429-430a-9510-39fc99f17f05` / `2279c6d7-2da6-4317-9c10-7cc77374b317` / `f9d82fcc-1e26-4710-87c3-1d20483b63ef`
   - RunId: `daily-aws-paper-20260621-204017`
   - Task Definition / taskId: `portfolio-paper-interest-crawler:7` / `78979b5cbb714d0eb94f5946e15a14ce`
   - S3 key: `tmp/krx/interest_krx_raw_validate_daily.py`
   - Scheduled Task: `Portfolio-KRX-Worker-Daily`
   - latest worker log: `krx_worker_daily_20260621_114154.log`
   - wrapper 옵션: `-StartStep` · `-EndStep`
   - DB session: user `crawler_app` / schema `interest` / search_path `interest, reference, legacy, public`
   - DB validation 결과: row_count 1 · 349 / max_date 2026-06-19

### 8. 본 일자 범위 밖 / 후속 인계: 정리

1. EC2 lifecycle 자동 start / SSM Online wait / stop 절차: 후속 유지(2026-06-20 §4 정합)
2. Step Functions 에서 ECS RunTask + SSM RunCommand 혼합 orchestration: 후속 유지
3. View Daily Batch 화면에서 `KrxDbValidationCommandId` / latest worker log / Step 2 validation 결과 표시 여부 검토: 후속 유지
4. worker log centralized collection(CloudWatch Logs Agent 또는 SSM output 기반): 후속 유지
5. Step 12 실제 paper 주문 제출은 별도 승인 전까지 실행하지 않음(OD-SAFE-002 / OD-SAFE-003 / R-AUTO-002 정합)
6. 본 일자 작업 범위는 Step 2 보강 완료로 마감 — Step 3 PREPROCESSOR 이후 단계는 본 일자 다음 작업으로 강제하지 않음

## 2026-06-22 Daily AWS Paper Step 2 / Step 3 재검증 (OD-MS-026 1차 실증 Daily run 회귀)

**Summary** — Daily wrapper 2회차 실 운영 실행에서 Step 2 / Step 3 회귀 0건 재검증.

- 환경: `aws-paper` / RunDate `2026-06-22` / region `ap-northeast-2`
- 재검증 통과: 2026-06-21 Step 2 성공판정 강화(OD-MS-026 / R-AUTO-020) + KRX raw DB validation 연동
- Kiro: 문서 / 절차 정리만. 운영자: 실제 ECS RunTask / SSM RunCommand / Windows EC2 worker / RDS 작업 직접 수행
- 안전: `aws-paper` 한정 / aws-live 작업 0건 / broker · KIS / 주문 / 체결 / Daily Batch entrypoint 직접 호출 0건

### 1. Step 2 `INTEREST_CRAWLER` 재검증 (OD-MS-026 정합)

1. wrapper Step 2 결과: 완료
   1) `StepCode INTEREST_CRAWLER` / `Status SUCCESS` / `Runner ECS+SSM`
   2) 6개 성공 조건(non-GUI ECS exitCode 0 / Crawler Worker EC2 running / Scheduled Task Running → Ready / Last Result 0 또는 0x0 / latest worker log / KRX raw DB validation 통과) 모두 통과(OD-MS-026 정합)
2. non-GUI ECS crawler 검증: 완료
   1) taskDefinition `portfolio-paper-interest-crawler:7`
   2) exitCode 0 / stoppedReason `Essential container in task exited`
   3) CloudWatch log 저장 — 본 노트 파일 본문 평문 인용 0건
3. Windows KRX worker SSM command 검증: 완료
   1) crawler worker EC2 state `running`(R-AUTO-016 mitigation 갱신 fail-closed 분기 미진입)
   2) SSM RunCommand Success / ResponseCode 0
   3) Scheduled Task `Portfolio-KRX-Worker-Daily` Running → Ready 복귀 / `sawRunning=True` / FinalLastResult 0
   4) latest worker log `C:\portfolio\logs\krx_worker_daily_*.log`(파일 path / last write time / size / tail 출력 — KRX login / program / shortsell SUCCESS / `DONE :: KRX worker daily` 라벨 확인 / 본문 평문 인용 0건)
4. KRX raw DB validation 검증: 완료
   1) SSM step `INTEREST_CRAWLER_KRX_DB_VALIDATE` / Success / ResponseCode 0
   2) DB session user `crawler_app` / schema `interest` / search_path `interest, reference, legacy, public`
   3) `ExpectedKrxRawDate 2026-06-19` 기준 — `interest_program_raw` 와 `interest_shortsell_raw` 둘 다 row_count > 0 / max_date ≥ expected / validation exit code 0
   4) stderr empty / step result 에 `KrxDbValidationCommandId` 포함(R-AUTO-020 mitigation 정합)

### 2. Step 3 `PREPROCESSOR` 재검증

1. wrapper Step 3 결과: 완료
   1) ECS RunTask `portfolio-paper-interest-preprocessor:1` exitCode 0
   2) `PREPROCESSOR PIPELINE END` 라벨 확인 — 본 노트 본문 평문 인용 0건
2. raw → feature 흐름 정합: 확인
   1) Step 2 KRX raw + non-GUI raw 최신성 회복 후 진입했으므로 stale raw data 위험 미발생(R-DATA-009 / R-DATA-010 mitigation 정합)
   2) `pre_total_market_daily_feature` / `pre_total_stock_daily_feature` 등 신규 feature row 가 정상 생성 — Step 4 BACKTEST_RESEARCH 및 Step 6 DAILY_BUY_SIGNAL 진입 가능 상태 확보

### 3. 결정 / 리스크 변경 요약

1. 신규 결정: 0건. 신규 리스크: 0건.
2. 본문 변경 없는 결정: 1차 실증 메모 보강
   1) OD-MS-026 — Step 2 성공판정 강화의 6개 성공 조건이 Daily run 실 통과로 회귀 0건 1차 실증
   2) OD-MS-011 / OD-MS-022 / OD-MS-023 — hybrid execution model / KRX GUI 자동 로그인 / wrapper 운영 정책 회귀 0건
3. 본문 변경 없는 리스크: 보강 메모
   1) R-AUTO-020 — Scheduled Task trigger 성공만으로 Step 2 SUCCESS 처리하던 한계가 KRX raw DB validation guard 도입 후 회귀 0건 1차 실증
   2) R-AUTO-016 — crawler worker EC2 `running` 상태 / fail-closed 분기 미진입
   3) R-AUTO-017 — Chrome / chromedriver best-effort reset 회귀 0건 / wrapper Step 2 stdout 의 reset 라벨 정상

### 4. 안전 / 보안 점검 결과 (2026-06-22)

1. broker / KIS 호출 0건. `--execute` 호출 0건. 신규 BUY · SELL · 취소 · 정정 호출 0건. fill · position sync 자동 재시도 0건. aws-live 작업 0건.
2. RDS DDL 0건. DML 은 KRX worker / non-GUI crawler / Preprocessor 의 raw → feature upsert 한정.
3. 실제 secret value / KRX 로그인 password / RDS password / RDS endpoint hostname / account-id / 실제 ARN / IAM access key id / EIP / image digest full sha256 / task ARN / Administrator password 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder.
4. AWS / SSM / EC2 / ECS / RDS / KRX 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리만 수행. CloudWatch Logs 본문 / SSM 응답 본문 / wrapper PowerShell stdout 전문 평문 인용 0건.
5. 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역). 운영 식별자(taskDefinition revision / RunDate / ExpectedKrxRawDate / Scheduled Task 이름 / latest worker log 파일명 / DB session user · search_path) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.
