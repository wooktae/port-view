# Operation Notes — 08-interest-crawler-and-preprocessor-ecs

본 문서는 08-interest-crawler-and-preprocessor-ecs 진행 중 운영자 / Kiro 가 실제 수행한 작업 결과를 일자별로 누적 기록하는 운영 노트다. 1차 적용 환경은 `aws-paper`, region 은 `ap-northeast-2`, 1차 검증 대상은 Preprocessor MS(`port-interest-preprocessor`).

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

운영자가 2026-06-10 직접 수행한 ECS / ECR 기본 포팅 1차 결과를 누적 기록한다. 본 일자에 Kiro 는 문서 작성 / 절차 정리만 수행했고, 실제 AWS / IAM / Secrets / RDS 작업은 운영자가 직접 진행했다.

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

운영자가 2026-06-12 직접 수행한 Windows EC2 worker 기반 KRX program / shortsell 수집 1차 검증 결과를 누적 기록한다. 본 일자에 Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행했고, 실제 AWS / IAM / Secrets Manager / EC2 / RDS 작업은 운영자가 직접 수행했다. KRX GUI 의존 수집은 Windows EC2 worker 로 분리 확정되었으며, non-GUI crawler 는 ECS Fargate Task 후보로 유지하고, preprocessor 는 ECS Fargate Task 를 그대로 유지한다.

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
