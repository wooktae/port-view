# Tasks — 08-interest-crawler-and-preprocessor-ecs

본 tasks 는 [`./design.md`](./design.md) / [`./requirements.md`](./requirements.md) 결정을 진행 단위로 분해한 최소 체크리스트다. 실제 AWS / docker / ecs / iam 작업은 운영자가 직접 수행하고 Kiro 는 문서 / 절차 / 검증 항목 정리만 담당한다. 일자별 상세 결과는 [`./operation-notes.md`](./operation-notes.md) 참조(2026-06-10 · 2026-06-12 · 2026-06-13 · 2026-06-15 · 2026-06-16 · 2026-06-17 · 2026-06-20 · 2026-06-21 · 2026-06-22).

**Hybrid execution model 요약** — 자세한 결과는 operation-notes 참조.

- 2026-06-12 분리 확정: non-GUI crawler = ECS Fargate Task 후보 유지 / KRX GUI crawler = Windows EC2 worker / preprocessor = ECS Fargate Task
- 2026-06-13 자동화 진입점 확정: SSM RunCommand → `schtasks /Run` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1` / ECS crawler rev6 Selenium Chrome smoke 통과 → hybrid 1차 완성
- 2026-06-15 stale raw data 식별: rev6 은 smoke 전용 / non-GUI daily 운영 경로 부재로 raw 최신성 부족(R-DATA-009 / R-DATA-010) 1차 식별
- 2026-06-16 hybrid 구조 완료: `interest_crawler_daily_nongui.py` 신규 + ECS Task Definition rev7(`paper-20260616-nongui`) RunTask exitCode 0 / 8종 step SUCCESS / non-GUI raw 7종 + KRX raw 2종 직전 거래일 적재 / Windows EC2 worker Autologon + Administrator interactive session + Scheduled Task + SSM trigger 자동화 성공
- 결정 락: OD-MS-022 신규 + OD-MS-011 / OD-MS-015 / OD-MS-020 1차 실증
- 후속: Preprocessor ECS 재실행(task 77) / Backend AWS E2E dry-run 재개(OD-MS-021)

## Task Section Overview

각 섹션별 완료 / 이월 상태 요약. 자세한 근거는 각 섹션 참조.

| Section | Range | 상태 | Note |
|---|---|---|---|
| §1 ECR Repository | task 1~5 | 🟢 완료 | 2026-06-10 |
| §2 Dockerfile 점검 | task 6~9 | 🟢 완료 | 2026-06-10 |
| §3 로컬 빌드 | task 10~13 | 🟢 완료 | 2026-06-10 |
| §4 ECR Push | task 14~17 | 🟢 완료 | 2026-06-10 |
| §5 ECS Cluster/Role/Log Group | task 18~24 | 🟢 완료 | 2026-06-10 |
| §6 Preprocessor 단발 실행 | task 25~28 | 🟢 완료 | 2026-06-10 |
| §7 Crawler outbound 리스크 | task 29~32 | 🟠 부분 완료 | task 30 / 31 outbound 후속(58) |
| §8 NAT-free 재확인 | task 33~34 | 🟢 완료 | — |
| §9 안전 제약 / 산출물 한정 | task 35~40 | 🟢 완료 | — |
| §10 완료 기준 | task 41~43 | 🟢 완료 | — |
| §11 Windows EC2 worker (KRX GUI) | task 44~52 | 🟢 완료 | 2026-06-12 |
| §12 후속 task | task 53~60 | 🟠 부분 | 53/57 완료 / 54·55·56·58·59·60 후속 |
| §13 SSM + ECS smoke | task 61~69 | 🟢 완료 | 2026-06-13 |
| §14 E2E dry-run / 재판정 | task 70~81 | 🟢 완료 | 2026-06-15 / 77·78 후속 |
| §15 Crawler 데이터 미수집 해결 | task 82~92 | 🟢 완료 | 2026-06-16 |
| §16 Step 2 성공판정 강화 | task 100~110 | 🟢 완료 | 2026-06-20 / 21 |
| §17 Daily run 재검증 | task 112~115 | 🟢 완료 | 2026-06-22 |

**후속 유지 (open)** — task 54 / 55 / 56 / 59 / 78 / 111. 자세한 근거는 각 섹션 및 [`./operation-notes.md`](./operation-notes.md) Open Risks & Next Checks 참조.

## 1. ECR Repository 생성 준비 (§2)

- [x] 1. `portfolio-interest-crawler` repository 생성 기준(이름 / region / scan on push) 정리 → 2026-06-10 운영자 직접 생성 완료 (§2.1)
- [x] 2. `portfolio-interest-preprocessor` repository 생성 기준(이름 / region / scan on push) 정리 → 2026-06-10 운영자 직접 생성 완료 (§2.1)
- [x] 3. repository URI placeholder 표기(`<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:<image-tag>`, `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-preprocessor:<image-tag>`) 명시 (§2.1, §11)
- [x] 4. 공통 base image 분리 여부는 후속 검토로 분리 명시 (§2.2)
- [x] 5. ECR repository 환경 미분리 정책(paper / live 동일 artifact, 6개 항목 분리) 정리 → 2026-06-10 paper / live 미분리 확정 (§2.3)

## 2. Dockerfile 기준 점검 (§3)

- [x] 6. crawler Dockerfile 존재 여부 / Selenium·Chrome·chromedriver 의존성 점검 항목 정리 → 2026-06-10 운영자 직접 신규 생성(base `python:3.13-slim`, Chromium / chromedriver 포함, command `python interest_crawler_daily.py`) (§3.2)
- [x] 7. preprocessor Dockerfile 존재 여부 / RDS env 5종 호환성 점검 항목 정리 → 2026-06-10 운영자 직접 신규 생성(base `python:3.13-slim`, command `python pre_daily.py`) (§3.1)
- [x] 8. requirements 설치 방식 / entrypoint·CMD / 환경변수 주입 방식 점검 항목 정리 → 2026-06-10 두 MS requirements.txt 신규 생성(preprocessor: `psycopg2-binary` / `requests`. crawler: `beautifulsoup4` / `pandas` / `psycopg2-binary` / `requests` / `selenium` / `yfinance`) (§3.1, §3.2)
- [x] 9. Dockerfile 부재·결함 발견 시 후속 spec / 운영자 단계 책임 분리 명시 → 2026-06-10 두 MS Dockerfile / requirements 운영자 직접 신규 생성으로 조치 완료 (§3)

## 3. 로컬 이미지 빌드 정리 (§4)

- [x] 10. preprocessor 우선 빌드 절차 정리 → 2026-06-10 `portfolio-interest-preprocessor:paper-20260610` / `paper-latest` 빌드 완료 (§4.1)
- [x] 11. crawler 빌드 절차 정리 → 2026-06-10 `portfolio-interest-crawler:paper-20260610` / `paper-latest` 빌드 완료 (Chromium 포함으로 preprocessor 보다 큰 이미지) (§4.1)
- [x] 12. 빌드 실패 원인 후보 5건(requirements / Python ver / import / system pkg / Selenium) 정리 → 2026-06-10 Dockerfile 줄 연속 문자(`\`) 오류 1건 발견·수정(PowerShell 백틱 아님) (§4.2)
- [x] 13. crawler Selenium 의존성 결함은 preprocessor 흐름 차단 금지 명시 → 2026-06-10 crawler 빌드 단계 리스크 1차 해소(빌드 성공) / preprocessor 흐름 차단 0건 (§4.1)

## 4. ECR Push 정리 (§5)

- [x] 14. aws-paper image tag 기준(`paper-<yyyymmdd>` / `paper-latest` placeholder) 결정 기준 정리 → 2026-06-10 결정 / 사용 (§5.1, §5.2)
- [x] 15. preprocessor push 절차 정리 → 2026-06-10 `paper-20260610` / `paper-latest` push 완료 (§5.1)
- [x] 16. crawler push 절차 또는 push 실패 원인 후보 5건 정리 → 2026-06-10 `paper-20260610` / `paper-latest` push 완료 (§5.3)
- [x] 17. image digest 확인 절차 정리(`<image-digest>` placeholder 만 기록) → 2026-06-10 운영자가 digest 확인 / 실값은 본 spec 산출물에 미기록 (§5.1, §11)

## 5. ECS Cluster / Role / Log Group 준비 (§6)

- [x] 18. aws-paper ECS Cluster 1개 생성 기준(이름 / Fargate) 정리 → 2026-06-10 `portfolio-paper-cluster` 생성 / 확인, Fargate capacity provider, ACTIVE 상태 확인 (§6.1)
- [x] 19. Task Execution Role 권한 매트릭스(ECR pull / Logs write / Secrets·SSM read) 정리 → 2026-06-10 `portfolio-paper-ecs-task-execution-role` 생성, trust `ecs-tasks.amazonaws.com`, managed `AmazonECSTaskExecutionRolePolicy` + preprocessor DB secret read inline policy(단일 secret ARN 한정) (§6.2)
- [x] 20. crawler / preprocessor Task Role 분리 정리(service prefix 별도) → 2026-06-10 `portfolio-paper-preprocessor-task-role` / `portfolio-paper-crawler-task-role` 생성·확인 (§6.1, §10.2)
- [x] 21. CloudWatch Log Group 2개 사전 생성 기준 정리 → 2026-06-10 `/portfolio/paper/preprocessor` / `/portfolio/paper/crawler` 생성 + retention 14일 설정 (§6.1)
- [x] 22. RDS 접속 SG 통과 정책(`sg-preprocessor-task` → `sg-rds-postgres` 5432) 정리 → 2026-06-10 preprocessor task SG → RDS PostgreSQL SG 5432 inbound 허용 확인 (§7.2)
- [x] 23. Resource·Action wildcard 금지 정책 재확인 → 2026-06-10 preprocessor DB secret read policy 단일 secret ARN 한정 / wildcard 0건 (§6.3)
- [x] 24. Task Execution Role / Task Role 책임 분리 정책(Task Definition `secrets` 우선) 정리 → 2026-06-10 본 1차 검증은 Task Definition `secrets` 주입 방식으로 통과 (§6.4)

## 6. Preprocessor 단발 실행 검증 정리 (§7)

- [x] 25. awsvpc / public subnet / `assignPublicIp = ENABLED` 확인 항목 정리 → 2026-06-10 preprocessor RunTask 검증 통과 (§7.1)
- [x] 26. `preprocessor_app` 기준 RDS 접속 성공 점검 항목 정리 → 2026-06-10 1차 host 누락 / 2차 sequence 권한 부족 조치 후 접속·실행 성공 (§7.2, §7.3)
- [x] 27. CloudWatch Logs 출력 / Task exit code 0 점검 항목 정리 → 2026-06-10 lastStatus `STOPPED` / exitCode `0` / `PREPROCESSOR PIPELINE END` 확인 (§7.2)
- [x] 28. 실행 실패 원인 후보 5건(env / Secret 권한 / SG / VPC Endpoint / image) 정리 → 2026-06-10 사례 누적: (a) Secrets Manager JSON `host` key 누락 → psycopg2 가 Unix socket `/var/run/postgresql/.s.PGSQL.5432` 시도 → secret 재생성으로 해소. (b) `permission denied for sequence pre_marketbreadth_daily_feature_id_seq` → public schema 잔존 sequence 2건에 `preprocessor_app` USAGE / SELECT 부여로 해소 (§7.4)

## 7. Crawler 외부 outbound 리스크 별도 정리 (§8)

- [x] 29. Selenium / Chrome 필요 여부 1차 검토 항목 정리 → 2026-06-10 Crawler Dockerfile 에 Chromium / chromedriver 포함 / 빌드 단계 통과. 2026-06-12 검증 결과 KRX GUI 의존 수집(KRX program / KRX shortsell) 은 Windows EC2 worker 로 분리 확정 (§8.1)
- [ ] 30. KRX / Naver / yfinance outbound 도달 여부 1차 검토 항목 정리 → 부분 완료: 2026-06-12 Windows EC2 worker 에서 KRX program / KRX shortsell outbound 도달·수집 성공(2026-06-09 ~ 2026-06-11 각 일자 적재 확인). 2026-06-13 ECS / Fargate `portfolio-paper-interest-crawler:6` Selenium Chrome smoke 에서 example.com / Naver Finance 접속 성공(`SELENIUM CHROME SMOKE SUCCESS` 확인). KRX outbound 의 ECS Fargate 경로 검증과 yfinance 의 ECS Fargate 경로 검증, non-GUI crawler 실제 Task Definition 분리는 후속(task 58)으로 이월 (§8.1)
- [ ] 31. public subnet + `assignPublicIp` 도달 검증 항목 정리 → 부분 완료: 2026-06-10 preprocessor RunTask 로 NAT-free 자체 검증 통과. 2026-06-13 ECS crawler smoke RunTask(public-a / public-b subnet + `sgroup-crawler-tasks` SG + `assignPublicIp = ENABLED`)에서 외부 outbound 도달 1차 통과(exitCode 0 / Naver Finance 접속 성공). non-GUI crawler 실제 Task Definition 분리는 후속(task 58)으로 이월(KRX GUI 경로는 EC2 worker 로 분리) (§8.2, §9)
- [x] 32. 운영 안정화는 본 spec 범위 밖 / 후속 spec·후속 phase 책임 분리 명시 → 2026-06-10 crawler 안정화 100% 는 오늘 범위 밖으로 유지. 2026-06-12 KRX GUI 의존 수집은 EC2 worker 로 1차 운영 가능 상태 도달 / 완전 자동화는 후속 (§8)

## 8. NAT-free 정책 재확인 (§9)

- [x] 33. NAT Gateway 사용 0건 점검 항목 정리 → 2026-06-10 preprocessor RunTask public subnet + `assignPublicIp = ENABLED` 통과로 NAT-free 1차 검증 (§9.1)
- [x] 34. NAT Gateway 발견 시 OD-NET-001 / OD-NET-002 재확인 흐름 정리 (§9.1)

## 9. 안전 제약 / 산출물 한정 재확인 (§11)

- [x] 35. 실제 AWS / ECR / ECS / IAM 변경 0건 / 운영자 직접만 재확인 → Kiro 본 spec 작업으로 인한 AWS 리소스 변경 0건. 운영자 직접 수행 항목은 [`./operation-notes.md`](./operation-notes.md) 2026-06-10 섹션 참조 (§11.1)
- [x] 36. 8개 MS 코드 / docs / 패키징 / Dockerfile 미수정 재확인 → Kiro 본 spec 작업으로 인한 변경 0건. 운영자 직접 작업으로 port-interest-preprocessor / port-interest-crawler 의 Dockerfile / requirements.txt 신규 생성(operation-notes 참조) (§11.1)
- [x] 37. 외부 호출 / 크롤링 / 주문 / RDS DDL·DML 0건 재확인 → Kiro 본 spec 작업으로 인한 호출 0건. 운영자 직접 수행한 RDS sequence GRANT 2건과 preprocessor pipeline RunTask 결과는 operation-notes 참조 (§11.1)
- [x] 38. 민감정보 평문 기록 0건(`[REDACTED]` / placeholder) 재확인 (§11.1)
- [x] 39. 본 08 초기 문서 phase 산출물은 requirements.md / design.md / tasks.md 3개로 한정 재확인 → operation-notes.md 는 운영자 실행 결과 누적 기록용으로 별도 작성(R11 정합) (§11.2)
- [x] 40. runbook.md / validation-checklist.md / CHANGELOG.md / WORKLOG.md 는 운영자 실행 이후 별도 작성 재확인 → operation-notes.md 는 2026-06-10 운영자 실행 결과로 신규 생성. runbook / validation-checklist / CHANGELOG / WORKLOG 는 후속 단계 (§11.2)

## 10. 완료 기준

- [x] 41. requirements.md / design.md / tasks.md 3개 모두 존재
- [x] 42. 후속 운영자 단계 인계 핵심 결정 포인트(ECR repo / image tag / Cluster·Role·Log Group / preprocessor 단발 실행 골격) 가 design.md 에 명시됨
- [x] 43. crawler Selenium / Chrome / KRX·Naver·yfinance 리스크가 §8 에 별도 분리됨

## 11. Windows EC2 worker 기반 KRX GUI 의존 수집 (2026-06-12 추가)

- [x] 44. EC2 worker 시작 / RDP 접속 / 작업 디렉터리 / venv / Python 3.13.5 / IAM Role 인식 점검 항목 정리 → 2026-06-12 점검 통과 ([`./operation-notes.md`](./operation-notes.md) §1)
- [x] 45. RDS Secret 기반 DB 환경변수 주입 절차(`load-crawler-db-env.ps1`) 정리 → 2026-06-12 `/portfolio/paper/rds/crawler-app` 기반 5종 env 주입 성공 / password value 미출력 (§1, §2)
- [x] 46. 다운로드 경로 불일치(`C:\Users\USER\Downloads` vs `C:\Users\Administrator\Downloads`) junction 조치 절차 정리 → 2026-06-12 junction 적용 후 CSV 인식 성공 ([`./operation-notes.md`](./operation-notes.md) §3)
- [x] 47. KRX program 단독 수집 검증(`interest_program.py`) 절차 정리 → 2026-06-12 2026-06-09 / 2026-06-10 / 2026-06-11 각 1건 적재 / `interest_program_raw` 543 → 546 확인 ([`./operation-notes.md`](./operation-notes.md) §4)
- [x] 48. KRX shortsell 단독 수집 검증(`interest_shortsell.py`) 절차 정리 → 2026-06-12 2026-06-09 / 2026-06-10 / 2026-06-11 각 349건 적재 / `interest_shortsell_raw` 189158 → 190205 확인 ([`./operation-notes.md`](./operation-notes.md) §5)
- [x] 49. 한글 literal / 인코딩 검증 절차 정리(BOM 제거 + utf-8 저장) → 2026-06-12 Python patch script 방식 채택 결정 ([`./operation-notes.md`](./operation-notes.md) §6)
- [x] 50. KRX Secrets Manager 연동(`/portfolio/paper/krx/crawler-login`, `username` / `password`) 절차 정리 → 2026-06-12 Secret 신규 생성 / IAM 권한 추가 / `load-krx-env.ps1` 주입 / 로그인 성공 ([`./operation-notes.md`](./operation-notes.md) §7)
- [x] 51. EC2 worker daily wrapper(`run_krx_worker_daily.ps1`) 절차 정리 → 2026-06-12 wrapper 1차 실행 / 재실행 시 `[Collected Date] None` no-op 정상 완료 / 로그 파일 형식 `krx_worker_daily_yyyyMMdd_HHmmss.log` 확인 ([`./operation-notes.md`](./operation-notes.md) §8)
- [x] 52. KRX GUI 의존 수집은 Windows EC2 worker / non-GUI crawler 는 ECS Fargate Task 후보 유지 / preprocessor 는 ECS Fargate Task hybrid execution model 분류 결정 → 2026-06-12 운영자 결정으로 확정 ([`./operation-notes.md`](./operation-notes.md) §9, [`./design.md`](./design.md) Hybrid execution model)

## 12. 후속 task (이월 / 분리)

- [x] 53. SSM RunCommand 기반 EC2 worker 무인 실행 절차 정리 → 부분 완료(2026-06-13). SSM RunCommand 가 wrapper 를 SYSTEM Session 0 에서 직접 실행하는 방식은 KRX GUI 로그인에 부적합으로 판단되어 채택하지 않음. 1차 자동화 방식은 SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1` 흐름으로 확정 ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §1 ~ §4)
- [ ] 54. EventBridge Scheduler → SSM RunCommand 연계 절차 정리 (후속 분리 — `schtasks /Run` 트리거 정기 실행)
- [ ] 55. Step Functions 에서 ECS Task + EC2 worker 혼합 orchestration 골격 정리 (후속 분리)
- [ ] 56. CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집 절차 정리 (후속 분리)
- [x] 57. wrapper 내 DB 검증 출력 자동 추가(`interest_program_raw` / `interest_shortsell_raw` 일자별 row count 출력) 절차 정리 → 2026-06-21 완료
  - 구조: `step-02-interest-crawler.ps1` 가 `ExpectedKrxRawDate`(RunDate 기준 전 영업일) 계산 → SSM `INTEREST_CRAWLER_KRX_DB_VALIDATE` 호출 → `interest_krx_raw_validate_daily.py --expected-date <yyyy-mm-dd>` 실행 → row_count + `max(trade_date)` 검증
  - fail-closed: step result 에 `KrxDbValidationCommandId` 포함 / non-zero exit 또는 row_count 0 시 Step 2 fail
  - 2026-06-21 단독 검증: SSM commandId `2279c6d7-2da6-4317-9c10-7cc77374b317` / interest_program_raw expected=`2026-06-19` max_date=`2026-06-19` expected_count=`1` / interest_shortsell_raw expected=`2026-06-19` max_date=`2026-06-19` expected_count=`349` / R-AUTO-007 mitigation 1차 실증
  - 2026-06-22 Daily run 재검증: 회귀 0건 / ExpectedKrxRawDate `2026-06-19` 기준 검증 통과 / Step 2 SUCCESS ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §4 / 2026-06-22 §1)
- [ ] 58. ECS / Fargate non-GUI crawler 실제 Task Definition 분리(인벤토리 확정 + smoke 용 revision 과 운영용 Task Definition 분리) (후속 분리)
- [ ] 59. EC2 worker 작업 완료 후 stop 절차 명시(idle 비용 절감) (후속 분리)
- [ ] 60. KRX GUI 수집 headless 리팩토링은 장기 후보로만 유지(현재 결정은 EC2 worker 사용) (장기 후보)

## 13. SSM RunCommand 자동화 + ECS crawler smoke (2026-06-13 추가)

- [x] 61. SSM Managed Node 등록 / `AWS-RunPowerShellScript` 가용성 / `hostname` · `whoami` · PowerShell 5.1 / `C:\portfolio` 접근 점검 항목 정리 → 2026-06-13 점검 통과(`whoami` = `nt authority\system` 확인) ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §1)
- [x] 62. SSM RunCommand 직접 wrapper 실행 시 SYSTEM Session 0 / SessionId 0 / SessionId 2 분리에 의한 KRX GUI 로그인 부적합 사례 정리 → 2026-06-13 직접 실행 방식 채택 거부 결정 ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §2)
- [x] 63. Windows Scheduled Task `Portfolio-KRX-Worker-Daily` 등록(Administrator interactive 세션 기준 / `C:\portfolio\run_krx_worker_daily.ps1` 실행 / `Start-ScheduledTask` 1차 검증) 절차 정리 → 2026-06-13 등록·실행 성공 ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §3)
- [x] 64. RDP closed 상태에서 SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` trigger 검증 절차 정리 → 2026-06-13 `SUCCESS: Attempted to run the scheduled task` / Task State `Running` → `Ready` / wrapper 최신 로그 `krx_worker_daily_20260613_021904.log` / `[Collected Date] None` idempotent 정상 완료 확인 ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §4)
- [x] 65. ECS crawler Task Definition `portfolio-paper-interest-crawler` revision 6 의 Selenium Chrome smoke command 분리(실제 daily crawler entrypoint 와 분리) 명시 → 2026-06-13 확정 / log stream prefix `ecs-selenium-chrome-smoke` ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §5)
- [x] 66. ECS / Fargate Selenium Chrome smoke RunTask(public-a / public-b + `sgroup-crawler-tasks` + `assignPublicIp = ENABLED`) 절차 정리 → 2026-06-13 exitCode 0 / `SELENIUM CHROME SMOKE SUCCESS` / `DRIVER QUIT` / `SELENIUM CHROME SMOKE END` 확인. example.com 접속 성공 / Naver Finance TITLE `Npay 증권` 확인 ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §5)
- [x] 67. non-GUI crawler 후보 인벤토리 1차 분류(`requests` / `yfinance` 중심 = ECS Fargate 후보 유지 / `interest_crawler_daily.py` 는 KRX GUI 의존 파일과 non-GUI 파일 동시 호출로 ECS 단독 실행 대상 제외) → 2026-06-13 분류 정리 완료 / 실제 운영용 Task Definition 분리는 task 58 (후속) ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §5)
- [x] 68. Hybrid execution model 1차 완성 판단(KRX program / shortsell = EC2 worker / non-GUI crawler = ECS Fargate 후보 / preprocessor = ECS Fargate / Selenium Chrome smoke 통과 / public subnet + `assignPublicIp` outbound 통과 / CloudWatch Logs 확인 가능) → 2026-06-13 판단 완료 ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §6)
- [x] 69. 본 일자 안전 / 문서 기록 점검(secret value / password / 실제 ARN / image digest / task ARN / instance-id / account-id 평문 기록 0건 / 8개 MS 미수정 / 외부 호출 0건) 재확인 → 2026-06-13 통과 ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §7)

## 14. Backend AWS E2E dry-run 1차 / Interest Crawler 상태 재판정 (2026-06-15 추가)

본 섹션은 2026-06-15 운영자가 직접 수행한 Backend AWS E2E dry-run 1차 점검과 Interest Crawler 상태 재판정 결과를 task 단위로 분해해 기록한다. 본 spec 시점의 hybrid execution model 1차 완성 판단(2026-06-13 §13) 자체는 그대로 유지하고, KRX GUI worker 운영 가능 / non-GUI daily 운영 미완료 / raw 최신성 검증 부족 / preprocessor ECS 실행 성공(데이터 최신성 제약) 을 후속으로 분리해 정리한다. 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-15 §1 ~ §8 참조. 본 일자 결정 락 OD-MS-020 / OD-MS-021 정합.

- [x] 70. KRX GUI worker 운영 가능 상태 1차 완성 재확인(Windows EC2 worker / Scheduled Task / wrapper / KRX login / program / shortsell 단건 수집 / DB Secret · KRX Secret 로딩 / 다운로드 경로 junction / 로그 파일 생성) → 2026-06-15 KRX worker 재실행 결과 `KRX already logged in` / `KRX Login Ready` / `[Collected Date] None` idempotent 정상 완료 확인 / `interest_program_raw` · `interest_shortsell_raw` 최신일 2026-06-12 확인(2026-06-15 월요일 기준 직전 거래일까지 적재 정상) ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §3)
- [x] 71. KRX raw 최신일자 SQL 점검 절차 정리 → 2026-06-15 `check_program_rows.py` / `check_shortsell_rows.py` 로 DB 상태 재확인. KRX raw 직전 거래일까지 적재 정상 ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §3)
- [x] 72. non-GUI Interest Crawler daily 운영 Task Definition / command 분리 → 2026-06-16 완료. 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §1, §2
  - `interest_crawler_daily_nongui.py` 신규 생성(KRX GUI 계열 import 제외 / non-GUI 계열 import 포함)
  - `paper-20260616-nongui` 빌드 + ECR push
  - Task Definition `portfolio-paper-interest-crawler:7` 등록: command `["python", "interest_crawler_daily_nongui.py"]` / log stream prefix `ecs-crawler-nongui-daily`
  - RunTask 성공: exitCode 0 / 약 9분 51초 / 전체 step SUCCESS(non-GUI 8종: `interest_news` · `interest_agency` · `interest_foreignindex` · `interest_commodity` · `interest_macroeconomic` · `interest_price` · `interest_investorflow` · `interest_marketbreadth`)
- [x] 73. non-GUI raw 최신일자 SQL 점검 정기 항목 정리 → 2026-06-16 1차 회복 적재 결과 확인 완료. non-GUI raw 7종(`interest_news_raw` 2026-06-16 / `interest_agency_raw` 2026-06-16 / `interest_price_raw` 2026-06-15 / `interest_investorflow_raw` 2026-06-15 / `interest_marketbreadth_raw` 2026-06-15 / `interest_commodity_raw` 2026-06-15 / `interest_foreignindex_raw` 2026-06-15) 직전 거래일까지 적재 회복. 정기 자동화는 후속 분리(task 78 / R-DATA-009 / R-DATA-010 mitigation 정합) ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §3)
- [x] 74. raw 최신성 회복 작업 정리 → 2026-06-16 완료 / R-DATA-010 mitigation 1차 실증 / 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §3, §4

  non-GUI ECS / Fargate rev7 RunTask 로 다음 raw 최신성 회복(max date 이동 / row count 증가):

  | raw table | max date | row count |
  |---|---|---|
  | `interest_price_raw` | 2026-06-08 → 2026-06-15 | 1,207,904 → 1,209,624 |
  | `interest_investorflow_raw` | 2026-06-08 → 2026-06-15 | 274,204 → 275,949 |
  | `interest_marketbreadth_raw` | 2026-06-08 → 2026-06-15 | 4,788 → 4,793 |
  | `interest_commodity_raw` | 2026-06-08 → 2026-06-15 | 29,132 → 29,162 |
  | `interest_foreignindex_raw` | 2026-06-08 → 2026-06-15 | 33,738 → 33,766 |
  | `interest_news_raw` | 2026-06-11 → 2026-06-16 | 68,881 → 71,614 |
  | `interest_agency_raw` | 2026-06-11 → 2026-06-16 | 49,018 → 49,044 |
  | `interest_macroeconomic_raw` | 2026-06-08 → 2026-06-15 | 75,742 → 75,777 |

  `interest_foreignindex_raw` HANGSENG · NIKKEI225 · SHANGHAI 일부 NULL Data 는 non-blocker 후보로 분리. KRX EC2 worker 로 `interest_program_raw` 547 → 548 / `interest_shortsell_raw` 190,554 → 190,903 적재 추가.
- [x] 75. preprocessor ECS RunTask 단발 실행(Backend E2E dry-run 3번) 절차 정리 → 2026-06-15 cluster `portfolio-paper-cluster` / task definition `portfolio-paper-interest-preprocessor:1` / FARGATE / awsvpc / public-a + public-b / `assignPublicIp = ENABLED` / `sgroup-preprocessor-tasks` / lastStatus `STOPPED` / desiredStatus `STOPPED` / stopCode `EssentialContainerExited` / container `interest-preprocessor` / exitCode 0 / 실행 시간 약 3분 43초 ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §4)
- [x] 76. preprocessor `updated_at` 갱신 / 신규 feature date 점검 절차 정리 → 2026-06-15 `updated_at` 2026-06-15 11:03:55+00(KST 2026-06-15 20:03:55) 갱신 확인 / DB write 경로 동작 확인. 신규 2026-06-15 feature date 0건 — 원인은 preprocessor 장애가 아니라 §3 raw 최신성 부족(R-DATA-010 정합) ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §4)
- [ ] 77. preprocessor 재실행 절차 정리 — 미완료 / 후속 분리. raw 최신성 회복 후 `portfolio-paper-interest-preprocessor` 재실행 → exitCode 0 확인 → feature table max date / `updated_at` 확인 → 신규 feature date 생성 여부 확인 ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §8)
- [ ] 78. preprocessor 실행 후 raw / feature 최신성 검증 SQL 자동화 — 미완료 / 후속 분리(R-DATA-009 / R-DATA-010 mitigation 정합) ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §8)
- [x] 79. Interest Crawler 표현 보정 결정 락 — 기존 "Interest Crawler 완성: 완료" / "Interest Crawler 는 hybrid execution model 기준으로 1차 완성" 표현을 "Interest Crawler hybrid 1차 구현: 부분 완료" / "KRX GUI worker 는 운영 가능 상태로 1차 완성" / "ECS / Fargate crawler 는 smoke 검증 완료" / "non-GUI daily raw 수집 운영 경로와 raw 전체 최신성 검증은 후속" 으로 보정 → 2026-06-15 OD-MS-020 결정 락 ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §3)
- [x] 80. Backend AWS E2E dry-run 17단계 점검표 정리 → 2026-06-15 1번 CONNECTOR_BALANCE 완료 / 2번 INTEREST_CRAWLER 부분 완료 / 3번 PREPROCESSOR 실행 완료(데이터 최신성 제약) / 4 ~ 7번 미진행 / 8 ~ 17번 미진행 또는 dry-run skip 예정. 실제 BUY / SELL / `--execute` 주문 전송 0건. fill / position sync 자동 재시도 0건. aws-live 작업 0건. OD-MS-021 결정 락 ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §6)
- [x] 81. Secret / IAM 권한 분리 1차 실증 정리 — MarketConnector EC2 role(`portfolio-paper-marketconnector-ec2-role`)에서 `/portfolio/paper/rds/preprocessor-app` `GetSecretValue` 시도 시 `AccessDeniedException`. 장애가 아니라 OD-SEC-006 / OD-DB-008 정합으로 정상 동작. MarketConnector EC2 role 에 preprocessor secret read 권한 추가 0건. preprocessor DB 확인은 preprocessor ECS Task 또는 운영자 로컬 SSM Port Forwarding 으로 수행 ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §5)

## 15. Crawler 데이터 미수집 해결 + KRX EC2 자동화 성공 (2026-06-16 추가)

본 섹션은 2026-06-16 운영자가 직접 수행한 아래 작업을 task 단위로 기록한다.

- (a) Crawler 데이터 미수집 원인 진단
- (b) non-GUI crawler 운영용 ECS Task Definition revision 7 분리 + RunTask 성공
- (c) raw 최신성 회복
- (d) KRX EC2 worker Autologon + Administrator console session + Scheduled Task + SSM trigger 자동화 재검증

2026-06-15 이월 항목 중 task 72(non-GUI Task Definition 분리) / task 73(non-GUI raw 점검) / task 74(raw 최신성 회복) 완료 처리. 결정 락 OD-MS-022 신규 / OD-MS-011 · OD-MS-015 · OD-MS-020 1차 실증 메모 보강. 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §1 ~ §5. 실제 BUY / SELL / `--execute` / fill·position sync 자동 재시도 / aws-live 작업 0건.

- [x] 82. Crawler 데이터 미수집 원인 진단 정리 → 2026-06-16 (a) ECS / Fargate revision 6 = Selenium / Chrome smoke command(daily 수집 entrypoint 아님) (b) 원본 `interest_crawler_daily.py` 가 KRX GUI 단계 포함으로 ECS / Fargate 직접 실행 부적합 (c) non-GUI 전용 orchestration / Task Definition 부재가 핵심 원인으로 정리 ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §1)
- [x] 83. non-GUI crawler 실제 운영용 Task Definition 분리 → 2026-06-16 완료(task 58 / task 72 와 합쳐 진행). 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §2
  - Python patch script: `interest_crawler_daily_nongui.py` 신규 생성 / `encoding="utf-8"` 저장 / `py_compile` / AST import 검증 / Docker image 내부 파일 포함 확인
  - KRX GUI 계열 import 제외: `interest_krx_login_new` / `interest_program` / `interest_shortsell`
  - non-GUI 계열 8종 import 포함
  - Docker rebuild + ECR push: `paper-20260616-nongui`
  - Task Definition 등록: `portfolio-paper-interest-crawler:7`(FARGATE / awsvpc / cpu 1024 / memory 2048 / log group `/portfolio/paper/crawler` / log stream prefix `ecs-crawler-nongui-daily` / crawler-app DB secret 주입)
- [x] 84. non-GUI crawler RunTask 1차 실행 검증 → 2026-06-16 통과. 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §2
  - cluster `portfolio-paper-cluster` / Task Definition `portfolio-paper-interest-crawler:7`
  - RunTask submit 성공 / failures 0 / lastStatus `STOPPED` / stopCode `EssentialContainerExited`
  - container exitCode 0 / 약 9분 51초 / CloudWatch log stream 생성 확인
  - 전체 step SUCCESS(non-GUI 8종: `interest_news` · `interest_agency` · `interest_foreignindex` · `interest_commodity` · `interest_macroeconomic` · `interest_price` · `interest_investorflow` · `interest_marketbreadth`)
- [x] 85. non-GUI raw 최신성 회복 SQL 검증 → 2026-06-16 완료 / raw table 별 최신성 확인은 task 74 표 참조 / 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §3
  - 8종 raw table 의 max date 이동 + row count 변화 확인(price · investorflow · marketbreadth · commodity · foreignindex · macroeconomic 2026-06-08 → 2026-06-15 / news · agency 2026-06-11 → 2026-06-16)
  - `interest_foreignindex_raw` HANGSENG · NIKKEI225 · SHANGHAI 일부 NULL Data 는 Preprocessor blocker 가 아닌 non-blocker 후보로 분리
- [x] 86. KRX EC2 worker Autologon bootstrap → 2026-06-16 Microsoft Sysinternals Autologon 적용 / Administrator 자동 로그인 설정 / EC2 재부팅 후 SSM managed instance Online 확인 / Autologon 은 paper 전용 Windows worker 보안 예외(R-SEC-009 신규) / Administrator password 본 노트 평문 기록 0건 ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §4)
- [x] 87. Administrator console session Active 확인 → 2026-06-16 `query user` 결과 USERNAME `administrator` / SESSIONNAME `console` / ID `1` / STATE `Active` / `whoami` 결과 `nt authority\system`(SSM RunCommand 자체가 SYSTEM 으로 실행되기 때문에 정상 / 필요한 조건은 별도 Administrator console interactive session 존재였고 Active 확인 완료) ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §4)
- [x] 88. SSM RunCommand → schtasks /Run → Scheduled Task → Administrator interactive session 흐름 재검증 → 2026-06-16 완료. 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §4
  - trigger: `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"` SUCCESS
  - Scheduled Task: Status `Running` → `Ready` / Last Result `267009` → `0` / Last Run Time 2026-06-16 04:55:49
  - wrapper 로그: `C:\portfolio\logs\krx_worker_daily_20260616_045550.log`
  - KRX login 성공(elapsed 92.83s) / `interest_program` 2026-06-15 / `interest_shortsell` 2026-06-15 349 Company / wrapper `DONE :: KRX worker daily`
- [x] 89. KRX program / shortsell 2026-06-15 DB 최신성 확인 → 2026-06-16 `interest_program_raw` max date 2026-06-15 / row count 547 → 548 / `interest_shortsell_raw` max date 2026-06-15 / row count 190,554 → 190,903 / 중복 insert · upsert 동작 정상 범위 ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §4)
- [x] 90. SSM direct Python 실행 운영 방식 제외 결정 락 + Headless · 비대화형 KRX 수집 운영 방식 제외 결정 락 → 2026-06-16 OD-MS-022 신규(Windows Autologon + Administrator interactive session + Scheduled Task + SSM trigger / SYSTEM Session 0 직접 실행 부적합 / Headless · 비대화형 KRX 수집은 로컬 검증상 운영 방식에서 제외) ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §4, §5)
- [x] 91. Preprocessor 재실행 가능 상태 판단 → 2026-06-16 raw 입력 데이터(non-GUI 6종 + KRX 2종 + news / agency) 회복 완료 / Preprocessor MS = ECS Fargate Task 유지 / `portfolio-paper-interest-preprocessor` 재실행 가능 상태 도달. 실제 재실행 / feature table max date / `updated_at` / 신규 feature date 생성 여부 확인은 task 77 후속 분리 ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §5)
- [x] 92. Chrome process 잔존 정리 옵션 검토 → 2026-06-21 1차 완료. `step-02-interest-crawler.ps1` 가 KRX worker 실행 전 chrome / chromedriver stale process best-effort reset 수행 / reset 실패는 warning 로그만 남기고 진행(R-AUTO-017 mitigation 1차 실증). 후속 EC2 stop / restart 절차는 R-AUTO-016 / EC2 lifecycle 자동화 후속과 결합 ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1)

## Task Dependency Graph (간단)

```text
1~5 (ECR Repository)
  └─> 6~9 (Dockerfile 점검)
        └─> 10~13 (로컬 빌드)
              └─> 14~17 (ECR Push)
                    └─> 18~24 (ECS Cluster / Role / Log Group)
                          └─> 25~28 (Preprocessor 단발 실행 검증)
                                ├─> 29~32 (Crawler outbound 리스크 / 30·31 부분 완료)
                                ├─> 33~34 (NAT-free 재확인)
                                ├─> 44~52 (Windows EC2 worker / KRX GUI 수집 / 2026-06-12)
                                ├─> 61~69 (SSM 자동화 + ECS crawler smoke / 2026-06-13)
                                ├─> 70~81 (Backend AWS E2E dry-run 1차 / Interest Crawler 상태 재판정 / 2026-06-15)
                                ├─> 82~92 (Crawler 데이터 미수집 해결 + KRX EC2 자동화 성공 / 2026-06-16)
                                └─> 35~40 (안전 제약 / 산출물 한정)
                                      └─> 41~43 (완료 기준)
                                            └─> 53~60 (후속 task / 이월 / 53 부분 완료)
```

## 2026-06-10 이월 항목 요약

1. crawler Task Definition 등록 / RunTask runtime 검증 (task 30, 31 — 2026-06-12 KRX GUI 경로는 EC2 worker 분리로 종결, 2026-06-13 ECS smoke 1차 통과 / 실제 daily Task Definition 분리는 task 58 후속)
2. crawler outbound(KRX / Naver / yfinance) 도달 / Selenium runtime 안정화 (2026-06-12 KRX GUI 의존 경로는 EC2 worker 로 1차 운영 가능 상태 도달, 2026-06-13 Selenium Chrome smoke 1차 통과, KRX outbound 의 ECS Fargate 경로 / yfinance ECS 경로 검증은 후속)
3. public schema 잔존 sequence 추가 점검 (preprocessor 외 도메인 sequence 잔존 여부)
4. runbook.md / validation-checklist.md 작성 시점 결정

## 2026-06-12 이월 항목 요약

1. SSM RunCommand 기반 EC2 worker 무인 실행 (task 53 — 2026-06-13 부분 완료 / Scheduled Task trigger 방식으로 확정)
2. EventBridge Scheduler → SSM RunCommand 연계 (task 54 — `schtasks /Run` 트리거 정기 실행)
3. Step Functions 에서 ECS Task + EC2 worker 혼합 orchestration (task 55)
4. CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집 (task 56)
5. wrapper 내 DB 검증 출력 자동 추가 (task 57 / R-AUTO-007 정합)
6. ECS / Fargate non-GUI crawler 범위 재정리 (task 58)
7. EC2 worker 작업 완료 후 stop 절차 명시 (task 59)
8. KRX GUI 수집 headless 리팩토링은 장기 후보로만 유지 (task 60)

## 2026-06-13 이월 항목 요약

1. EventBridge Scheduler → SSM RunCommand → `schtasks /Run` 정기 trigger 연계 (task 54)
2. Step Functions 에서 ECS Task + EC2 worker hybrid orchestration (task 55)
3. CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집 (task 56)
4. wrapper 내 DB 검증 출력 자동 추가(`interest_program_raw` / `interest_shortsell_raw` 일자별 row count 출력 / R-AUTO-007 정합) (task 57)
5. non-GUI crawler 실제 운영용 Task Definition 분리(smoke 용 revision 과 운영용 Task Definition 분리) (task 58)
6. EC2 worker 작업 완료 후 stop 절차 명시(idle 비용 절감) (task 59)
7. KRX GUI 수집 headless 리팩토링은 장기 후보로만 유지(현재 결정은 EC2 worker 사용) (task 60)

## 2026-06-15 이월 항목 요약

1. non-GUI Interest Crawler daily 운영 Task Definition / command 분리(task 72 / task 58 와 합쳐 진행)
2. raw 최신성 회복 — `interest_price_raw` / `interest_investorflow_raw` / `interest_marketbreadth_raw` / `interest_commodity_raw` / `interest_foreignindex_raw` / `interest_news_raw` / `interest_agency_raw` 직전 거래일 적재(task 74)
3. preprocessor 재실행 — raw 최신성 회복 후 `portfolio-paper-interest-preprocessor` 재실행 → exitCode 0 확인 → feature table max date / `updated_at` 확인 → 신규 feature date 생성 여부 확인(task 77)
4. preprocessor 실행 후 raw / feature 최신성 검증 SQL 자동화(task 78 / R-DATA-009 / R-DATA-010 mitigation 정합)
5. non-GUI raw 최신일자 SQL 점검 자동화(task 73)
6. Backend AWS E2E dry-run 재개 — `BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL` 순서로 진행. Research 는 Decision 보다 먼저 실행(OD-MS-021 정합). 주문 전송 / execution 계열은 안전 기준에 따라 skip 또는 dry-run 만 수행
7. Interest Crawler 표현 통일 정책 후속 점검(task 79 / OD-MS-020) — 운영 문서 / 보고 / 슬라이드 등에서 "Interest Crawler 완성: 완료" 표현이 잔존하는지 정기 점검

## 2026-06-15 이월 항목 회수 / 후속 분리 (2026-06-16 시점)

본 섹션은 2026-06-15 이월 항목 표(앞 절)를 2026-06-16 결과 기준으로 회수 / 후속 분리 / 변경 없음으로 재분류한다. task 번호 매핑은 그대로 유지한다.

- 회수 완료(2026-06-16):
  1. non-GUI Interest Crawler daily 운영 Task Definition / command 분리 — task 72 / task 58 → 2026-06-16 task 83 으로 회수(non-GUI 8종 포함 / Task Definition revision 7 신규 등록 / RunTask exitCode 0 통과)
  2. raw 최신성 회복 — task 74 → 2026-06-16 task 85 로 회수(non-GUI 7종 + macro 2026-06-15 / news · agency 2026-06-16 / `interest_foreignindex_raw` 일부 NULL 은 non-blocker 분리)
  3. non-GUI raw 최신일자 SQL 점검 — task 73 → 2026-06-16 task 85 와 함께 회수(점검 결과 회복 통과)
- 후속 유지:
  4. Preprocessor 재실행 — task 77 → 2026-06-16 시점에는 "재실행 가능 상태 도달"(task 90)까지만 회수, 실제 재실행은 후속 분리
  5. preprocessor 실행 후 raw / feature 최신성 검증 SQL 자동화 — task 78 → 후속 분리(R-DATA-009 / R-DATA-010 mitigation 정합)
  6. Backend AWS E2E dry-run 재개 — `BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL` 순서 / Research 우선 / 주문·execution 계열 skip 또는 dry-run → 후속 분리(OD-MS-021 정합)
  7. Interest Crawler 표현 통일 정기 점검 — task 79 → OD-MS-020 / 본 일자 OD-MS-022 정합 표현으로 갱신 후속 분리

## 2026-06-16 이월 / 후속 항목 요약

1. EventBridge Scheduler → SSM RunCommand → `schtasks /Run` 정기 trigger 연계(task 54)
2. Step Functions ECS Task + EC2 worker hybrid orchestration(task 55)
3. CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집(task 56)
4. wrapper 내 DB 검증 출력 자동 추가(task 57 / R-AUTO-007 정합)
5. EC2 worker 작업 완료 후 stop 절차 명시(task 59)
6. Chrome process 정리 옵션 — wrapper 종료 시 정리 후보(R-AUTO-017 mitigation 후속 / 본 §15 task 88)
7. Preprocessor ECS 재실행 + 신규 feature date 생성 여부 확인(task 77)
8. Backend AWS E2E dry-run 재개(`BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL`)
9. `interest_foreignindex_raw` HANGSENG / NIKKEI225 / SHANGHAI NULL Data 후속 점검(non-blocker 후보)
10. KRX GUI 수집 headless / 비대화형 리팩토링은 로컬 검증상 제외 / 운영 방식에서 제외 — 기존 "장기 후보" 표현은 본 일자에 보정(OD-MS-022 정합)

## 2026-06-17 이월 항목 회수 / 후속 분리 (Daily AWS 17-step E2E 완료 시점)

본 섹션은 2026-06-17 Daily AWS 17-step E2E 흐름에서 본 spec 범위 step 2 `INTEREST_CRAWLER` / step 3 `PREPROCESSOR` 가 모두 통과한 시점의 이월 항목 회수 / 후속 분리 정리다. 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-17 Daily AWS 17-step E2E 완료 (Interest Crawler + Preprocessor) §1 ~ §3 정합.

### 2026-06-17 회수된 항목 (Daily AWS 17-step E2E 통과)

1. Step 2 `INTEREST_CRAWLER` 17-step E2E 통과: 완료
   1) non-GUI ECS Fargate `portfolio-paper-interest-crawler:7` RunTask exitCode 0 / 전체 step SUCCESS
   2) KRX Windows EC2 worker Scheduled Task 흐름 통과(SSM RunCommand → `schtasks /Run` → Administrator interactive session → `run_krx_worker_daily.ps1` → KRX login)
   3) `interest_program_raw` / `interest_shortsell_raw` 2026-06-16 적재 확인
   4) crawler worker stop 요청 완료 — idle 비용 절감 + Autologon 노출 시간 최소화(R-SEC-009 mitigation 정합)
2. Step 3 `PREPROCESSOR` 17-step E2E 통과: 완료
   1) ECS Fargate `portfolio-paper-interest-preprocessor:1` RunTask exitCode 0 / `PREPROCESSOR PIPELINE END`
   2) `pre_total_market_daily_feature` / `pre_total_stock_daily_feature` 최신성 `2026-06-16` 확인
   3) `execution_app` interest 권한 누락은 본 step 영향이 아닌 8번 step 영향(R-DATA-005 [2026-06-17 보강] 정합) — preprocessor 자체는 정상 완료
3. R-DATA-009 / R-DATA-010 mitigation 1차 실증: 완료(2026-06-16 §3 / 본 일자 §3 정합)

### 2026-06-17 이월 항목 요약

1. EventBridge Scheduler → SSM RunCommand → `schtasks /Run` 정기 trigger 연계(task 54)
2. Step Functions 에서 ECS Task + EC2 worker hybrid orchestration(task 55)
3. CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집(task 56)
4. wrapper 내 DB 검증 출력 자동 추가(task 57 / R-AUTO-007 정합)
5. EC2 worker 작업 완료 후 stop 절차 자동화(task 59 / 본 일자 운영자 직접 stop 요청 통과 / 정기 자동화는 후속)
6. Chrome process 정리 옵션 검토(task 92 / R-AUTO-017 신규)
7. `interest_foreignindex_raw` HANGSENG / NIKKEI225 / SHANGHAI NULL Data 후속 점검(non-blocker)
8. `interest_ticker_value_raw` 최신성 회복(2026-03-09 → 직전 거래일 / 별도 후속)
9. preprocessor 실행 후 raw / feature 최신성 검증 SQL 자동화(task 78 / R-DATA-009 / R-DATA-010 mitigation 정합)
10. View 구현 — 후속 예정

## 16. 2026-06-20 ~ 2026-06-21 Step 2 `INTEREST_CRAWLER` 성공판정 강화 (운영자 직접 작업 + Kiro 문서 정리)

본 절은 2026-06-20 ~ 2026-06-21 운영자 직접 수행한 아래 작업을 task 단위로 기록한다.

- 2026-06-20: Daily AWS Paper Wrapper 최종 점검 / 6/18 중복 실행 시도 안전 중단 / 6/19 KRX raw 최신성 복구 상태
- 2026-06-21: `step-02-interest-crawler.ps1` 성공판정 강화 / non-GUI ECS env 보강 / KRX raw DB validation 스크립트 생성 + S3 배포 + EC2 단독 검증 / step-02 DB validation 연동 / worker stopped fail-closed / Step 2 단독 실행 검증

참조: [`./operation-notes.md`](./operation-notes.md) 2026-06-20 §1 ~ §5 / 2026-06-21 §1 ~ §8 / [`../_common/operator-decisions.md`](../_common/operator-decisions.md) Change Log 2026-06-21(OD-MS-026 신규) / [`../_common/risk-register.md`](../_common/risk-register.md) R-AUTO-020 신규 + R-AUTO-007 / R-AUTO-016 / R-AUTO-017 보강.

- [x] 100. Daily AWS Paper Wrapper 구조 / 안전 기준 / EC2 기동 기준 최종 점검 → 2026-06-20 완료. 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-20 §1
  - (a) wrapper 구조: `run-daily-aws-paper.ps1` + `daily-aws-paper.config.ps1` + `daily-aws-paper.functions.ps1` + `steps/step-01 ~ step-17`
  - (b) 주문 안전: Step 1 ~ 11 broker · KIS 주문 제출 전 단계 / Step 12 만 실제 KIS paper 주문 제출 / `-AllowPaperOrderExecute` 미명시 시 차단 / 토 · 휴장일 Step 12 제외
  - (c) EC2 기동 기준: MarketConnector EC2(Step 1 / 12 / 13 / 17) + Crawler Worker EC2(Step 2 KRX GUI worker) / EC2 stop · start 후 `/tmp/inject-env.sh` 유실 가능 → lifecycle 보강 후속 분리
- [x] 101. 6/18 wrapper 중복 실행 시도 안전 중단 → 2026-06-20 완료. 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-20 §2
  - RunDate `2026-06-18` / Step 1 ~ 11 범위 재실행 시도
  - Step 1 최초 실패(`/tmp/inject-env.sh` 유실) → 재생성 후 통과
  - Step 2 non-GUI ECS exitCode 0 + KRX worker Scheduled Task trigger 성공
  - Step 3 진입 직후 Ctrl+C 중단
  - 종료 상태: ECS RUNNING 0건 + AWS Batch RUNNING · SUBMITTED · PENDING · RUNNABLE 0건 + Scheduled Task `Ready` + LastTaskResult 0
  - 신규 주문 0건: Step 12 미실행 / execution_plan 0건 / strategy_execution_order 0건 / connector_order_request 0건 / KIS 신규 주문 0건
  - 기존 `execution_plan_id 94` 정상 완료 이력 / Crawler Worker chrome 잔여 프로세스 EC2 stop 으로 정리 / 두 EC2 stop 처리
- [x] 102. 6/19 KRX raw 최신성 복구 상태 점검 → 2026-06-20 `interest_program_raw` max_date `2026-06-19` / 2026-06-18 row_count `1` / 2026-06-19 row_count `1` / `interest_shortsell_raw` max_date `2026-06-19` / 2026-06-18 row_count `349` / 2026-06-19 row_count `349` / KRX raw 기준 최신성 복구 완료 / Scheduled Task trigger / LASTEXITCODE 중심 성공판정 한계(R-AUTO-007) 식별 → Step 2 wrapper 성공판정 강화 후속(task 103 ~ 110) 분리 ([`./operation-notes.md`](./operation-notes.md) 2026-06-20 §3)
- [x] 103. `step-02-interest-crawler.ps1` Chrome / chromedriver best-effort reset 추가 → 2026-06-21 chrome / chromedriver stale process 정리 / reset 실패는 warning 로그만 출력하고 진행 / R-AUTO-017 mitigation 1차 실증 ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1.1)
- [x] 104. `step-02-interest-crawler.ps1` Administrator interactive Scheduled Task 실행 경로 유지 → 2026-06-21 `Portfolio-KRX-Worker-Daily` `schtasks /Run` 실행 / SSM direct python 채택 거부 / OD-MS-022 / OD-MS-015 정합 / Windows Administrator interactive session 기준 유지 ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1.2)
- [x] 105. `step-02-interest-crawler.ps1` Scheduled Task 종료 대기 + Last Result 확인 → 2026-06-21 `Running` 상태 polling → `Ready` 복귀 wait / `sawRunning` 로그 출력 / timeout 시 Step 2 실패 / Last Result 0 또는 0x0 만 SUCCESS / Scheduled Task trigger 성공만으로 Step 2 SUCCESS 처리 금지 / R-AUTO-020 신규 mitigation 1차 실증 ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1.3 ~ §1.4)
- [x] 106. `step-02-interest-crawler.ps1` 최신 worker log 경로 출력 → 2026-06-21 `C:\portfolio\logs\krx_worker_daily_*.log` 최신 파일 path / last write time / size / tail 출력 ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1.5)
- [x] 107. `step-02-interest-crawler.ps1` crawler worker stopped fail-closed 처리 → 2026-06-21 변경 전 동작(자동 skip + Step 2 SUCCESS 가능성) 폐지 / 변경 후 동작(EC2 `running` 아니면 즉시 실패 / instanceId · state 출력 / KRX GUI worker · DB validation 미수행 상태에서 Step 2 SUCCESS 진입 차단) / R-AUTO-016 mitigation 갱신 ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1.6)
- [x] 108. non-GUI ECS crawler env 보강 — `daily-aws-paper.functions.ps1` 공통 함수 + ECS RunTask overrides 환경변수 → 2026-06-21 `Invoke-DailyAwsPaperEcsTask` `EnvironmentVariables` 파라미터 / ECS RunTask `containerOverrides.environment` 전달 / `New-SsmParameterFile` `ExecutionTimeoutSeconds` / `Invoke-SsmCommandAndWait` `ExecutionTimeoutSeconds` 전달 / 환경변수 `TEMP=/tmp` · `TMP=/tmp` · `PYTHONUTF8=1` · `PYTHONIOENCODING=utf-8` 주입 ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §2)
- [x] 109. `interest_krx_raw_validate_daily.py` 운영 검증 스크립트 신규 생성 + 로컬 검증 + S3 배포 + EC2 단독 검증 → 2026-06-21 완료. 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-21 §3
  - 신규 파일 생성: 운영자 직접(본 spec 산출물 사실 기록만) / 로컬 py_compile + UTF-8 read + program · shortsell · exit30 marker 통과
  - S3 배포: `s3://portfolio-paper-migration-yukiever/tmp/krx/interest_krx_raw_validate_daily.py` / presigned URL 경유 EC2 다운로드(SSM commandId `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3`)
  - EC2 배포 대상: `C:\portfolio\port-interest-crawler\interest_krx_raw_validate_daily.py`
  - EC2 단독 검증: SSM commandId `c844aea5-1429-430a-9510-39fc99f17f05` / venv `C:\portfolio\venvs\interest-crawler` / Python `3.13.5`
  - DB session: user=`crawler_app` / schema=`interest` / search_path=`interest, reference, legacy, public`
  - interest_program_raw: expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`1`
  - interest_shortsell_raw: expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`349` / exit code 0
- [x] 110. `step-02-interest-crawler.ps1` DB validation 연동(`INTEREST_CRAWLER_KRX_DB_VALIDATE`) + Step 2 단독 실행 검증 → 2026-06-21 완료. 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-21 §4 ~ §5
  - 연동: `ExpectedKrxRawDate` 계산(RunDate 기준 전 영업일) → `load-crawler-db-env.ps1` + venv + `interest_krx_raw_validate_daily.py --expected-date <yyyy-mm-dd>` 실행
  - fail-closed: row_count 0 또는 non-zero exit 시 Step 2 fail / step result 에 `KrxDbValidationCommandId` 포함
  - Step 2 단독 검증: RunId `daily-aws-paper-20260621-204017` / Status `SUCCESS` / Runner `ECS+SSM` / ExpectedKrxRawDate `2026-06-19`
  - non-GUI ECS: `portfolio-paper-interest-crawler:7` / taskId `78979b5cbb714d0eb94f5946e15a14ce` / exitCode 0
  - KRX worker SSM: commandId `f9d82fcc-1e26-4710-87c3-1d20483b63ef` / Scheduled Task elapsedSeconds=`111` / sawRunning=True / FinalStatus=Ready / FinalLastResult=0
  - latest worker log: `krx_worker_daily_20260621_114154.log` / KRX login · program · shortsell SUCCESS
  - KRX raw DB validation SSM commandId `2279c6d7-2da6-4317-9c10-7cc77374b317` / ResponseCode 0 / interest_program_raw OK / interest_shortsell_raw OK / validation exit code 0

### 후속 유지

- [ ] 54. EventBridge Scheduler → SSM RunCommand → `schtasks /Run` 정기 trigger 연계 (후속 분리)
- [ ] 55. Step Functions 에서 ECS RunTask + SSM RunCommand 혼합 orchestration (후속 분리)
- [ ] 56. CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집 (후속 분리)
- [ ] 59. EC2 worker 작업 완료 후 stop 절차 명시(idle 비용 절감) — 2026-06-20 §1 / §2 의 `/tmp/inject-env.sh` 유실 대응 + 자동 start / stop lifecycle 보강과 통합 (후속 분리)
- [ ] 111. View Daily Batch 화면에서 `KrxDbValidationCommandId` / latest worker log / Step 2 validation 결과 표시 여부 검토 (후속 분리 / 05 spec)

## 17. 2026-06-22 Daily run 재검증 (Step 2 / Step 3 회귀 0건)

본 절은 2026-06-22 Daily AWS Paper Wrapper 1 ~ 17 두 번째 실 운영 실행 결과 중 본 spec(08 / Interest Crawler · Preprocessor) 책임 task 의 재검증 메모다. 자세한 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-22 §1 ~ §4 / [`../_common/followups-overview.md`](../_common/followups-overview.md) 2026-06-22 후속 메모 참조.

- [x] 112. Step 2 KRX raw DB validation Daily run 재검증 — `INTEREST_CRAWLER_KRX_DB_VALIDATE` SSM step 회귀 0건 / DB session user `crawler_app` / schema `interest` / search_path `interest, reference, legacy, public` / `ExpectedKrxRawDate 2026-06-19` 기준 `interest_program_raw` · `interest_shortsell_raw` 검증 통과 / step result `KrxDbValidationCommandId` 자동 기록(OD-MS-026 / R-AUTO-020 mitigation 회귀 0건 / R-AUTO-007 mitigation 회귀 0건) ([`./operation-notes.md`](./operation-notes.md) 2026-06-22 §1.4)
- [x] 113. Step 2 worker stopped fail-closed Daily run 회귀 0건 — 본 일자 Crawler Worker EC2 state `running` / fail-closed 분기 미진입 / R-AUTO-016 mitigation 갱신 회귀 0건(자동 skip 동작 폐지 그대로 유지) ([`./operation-notes.md`](./operation-notes.md) 2026-06-22 §1.3)
- [x] 114. Step 2 Scheduled Task Running → Ready wait + Last Result 확인 Daily run 회귀 0건 — `Portfolio-KRX-Worker-Daily` Scheduled Task 의 `sawRunning=True` / FinalLastResult 0 / latest worker log path · last write time · size · tail 정상 출력 / Chrome / chromedriver best-effort reset 회귀 0건(R-AUTO-017 mitigation 회귀 0건) ([`./operation-notes.md`](./operation-notes.md) 2026-06-22 §1.3)
- [x] 115. Step 3 Preprocessor Daily wrapper 연동 검증 완료 — `portfolio-paper-interest-preprocessor:1` ECS RunTask exitCode 0 / `PREPROCESSOR PIPELINE END` 라벨 확인 / raw → feature 흐름 정합(R-DATA-009 / R-DATA-010 mitigation 회귀 0건) ([`./operation-notes.md`](./operation-notes.md) 2026-06-22 §2)

### 후속 유지 (2026-06-22 그대로)

- [ ] 54. EventBridge Scheduler → SSM RunCommand → `schtasks /Run` 정기 trigger 연계 (후속 분리)
- [ ] 55. Step Functions 에서 ECS RunTask + SSM RunCommand hybrid orchestration (후속 분리)
- [ ] 56. CloudWatch Logs Agent 또는 SSM output 기반 EC2 worker 로그 수집 (후속 분리)
- [ ] 59. EC2 worker 작업 완료 후 stop 절차 명시(idle 비용 절감) — Daily wrapper 측 EC2 lifecycle 자동 start / stop 보강과 결합(followups-overview 2026-06-20 §1 / 2026-06-22 §1 정합 / 후속 분리)
- [ ] 111. View Daily Batch 화면에서 `KrxDbValidationCommandId` / latest worker log / Step 2 validation 결과 표시 여부 검토 (후속 분리 / 05 spec)
