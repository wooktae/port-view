# Tasks — 08-interest-crawler-and-preprocessor-ecs

본 tasks 는 [`./design.md`](./design.md) / [`./requirements.md`](./requirements.md) 결정을 진행 단위로 분해한 최소 체크리스트다. 실제 AWS 작업과 docker / ecs / iam 작업은 운영자가 직접 수행하고, Kiro 는 문서 / 절차 / 검증 항목 정리만 담당한다. 2026-06-10 운영자 실행 결과는 [`./operation-notes.md`](./operation-notes.md) 2026-06-10 섹션을 참조한다.

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

- [x] 29. Selenium / Chrome 필요 여부 1차 검토 항목 정리 → 2026-06-10 Crawler Dockerfile 에 Chromium / chromedriver 포함 / 빌드 단계 통과 (§8.1)
- [ ] 30. KRX / Naver / yfinance outbound 도달 여부 1차 검토 항목 정리 → 이월: crawler Task Definition 등록 / RunTask runtime 검증 미실행 (§8.1)
- [ ] 31. public subnet + `assignPublicIp` 도달 검증 항목 정리 → 부분 완료: preprocessor RunTask 로 NAT-free 자체 검증 통과 / crawler runtime 검증 이월 (§8.2, §9)
- [x] 32. 운영 안정화는 본 spec 범위 밖 / 후속 spec·후속 phase 책임 분리 명시 → 2026-06-10 crawler 안정화 100% 는 오늘 범위 밖으로 유지 (§8)

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

## Task Dependency Graph (간단)

```text
1~5 (ECR Repository)
  └─> 6~9 (Dockerfile 점검)
        └─> 10~13 (로컬 빌드)
              └─> 14~17 (ECR Push)
                    └─> 18~24 (ECS Cluster / Role / Log Group)
                          └─> 25~28 (Preprocessor 단발 실행 검증)
                                ├─> 29~32 (Crawler outbound 리스크 / 30·31 이월)
                                └─> 33~34 (NAT-free 재확인)
                                      └─> 35~40 (안전 제약 / 산출물 한정)
                                            └─> 41~43 (완료 기준)
```

## 2026-06-10 이월 항목 요약

1. crawler Task Definition 등록 / RunTask runtime 검증 (task 30, 31)
2. crawler outbound(KRX / Naver / yfinance) 도달 / Selenium runtime 안정화 (후속 spec / 후속 phase 책임)
3. public schema 잔존 sequence 추가 점검 (preprocessor 외 도메인 sequence 잔존 여부)
4. runbook.md / validation-checklist.md 작성 시점 결정
