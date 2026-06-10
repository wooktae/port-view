# Tasks — 08-interest-crawler-and-preprocessor-ecs

본 tasks 는 [`./design.md`](./design.md) / [`./requirements.md`](./requirements.md) 결정을 진행 단위로 분해한 최소 체크리스트다. 실제 AWS 작업과 docker / ecs / iam 작업은 운영자가 직접 수행하고, Kiro 는 문서 / 절차 / 검증 항목 정리만 담당한다.

## 1. ECR Repository 생성 준비 (§2)

- [ ] 1. `portfolio-interest-crawler` repository 생성 기준(이름 / region / scan on push) 정리 (§2.1)
- [ ] 2. `portfolio-interest-preprocessor` repository 생성 기준(이름 / region / scan on push) 정리 (§2.1)
- [ ] 3. repository URI placeholder 표기(`<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:<image-tag>`, `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-preprocessor:<image-tag>`) 명시 (§2.1, §11)
- [ ] 4. 공통 base image 분리 여부는 후속 검토로 분리 명시 (§2.2)
- [ ] 5. ECR repository 환경 미분리 정책(paper / live 동일 artifact, 6개 항목 분리) 정리 (§2.3)

## 2. Dockerfile 기준 점검 (§3)

- [ ] 6. crawler Dockerfile 존재 여부 / Selenium·Chrome·chromedriver 의존성 점검 항목 정리 (§3.2)
- [ ] 7. preprocessor Dockerfile 존재 여부 / RDS env 5종 호환성 점검 항목 정리 (§3.1)
- [ ] 8. requirements 설치 방식 / entrypoint·CMD / 환경변수 주입 방식 점검 항목 정리 (§3.1, §3.2)
- [ ] 9. Dockerfile 부재·결함 발견 시 후속 spec / 운영자 단계 책임 분리 명시 (§3)

## 3. 로컬 이미지 빌드 정리 (§4)

- [ ] 10. preprocessor 우선 빌드 절차 정리 (§4.1)
- [ ] 11. crawler 빌드 절차 정리 (§4.1)
- [ ] 12. 빌드 실패 원인 후보 5건(requirements / Python ver / import / system pkg / Selenium) 정리 (§4.2)
- [ ] 13. crawler Selenium 의존성 결함은 preprocessor 흐름 차단 금지 명시 (§4.1)

## 4. ECR Push 정리 (§5)

- [ ] 14. aws-paper image tag 기준(`paper-<yyyymmdd>` / `paper-latest` placeholder) 결정 기준 정리 (§5.1, §5.2)
- [ ] 15. preprocessor push 절차 정리 (§5.1)
- [ ] 16. crawler push 절차 또는 push 실패 원인 후보 5건 정리 (§5.3)
- [ ] 17. image digest 확인 절차 정리(`<image-digest>` placeholder 만 기록) (§5.1, §11)

## 5. ECS Cluster / Role / Log Group 준비 (§6)

- [ ] 18. aws-paper ECS Cluster 1개 생성 기준(이름 / Fargate) 정리 (§6.1)
- [ ] 19. Task Execution Role 권한 매트릭스(ECR pull / Logs write / Secrets·SSM read) 정리 (§6.2)
- [ ] 20. crawler / preprocessor Task Role 분리 정리(service prefix 별도) (§6.1, §10.2)
- [ ] 21. CloudWatch Log Group 2개 사전 생성 기준 정리 (§6.1)
- [ ] 22. RDS 접속 SG 통과 정책(`sg-preprocessor-task` → `sg-rds-postgres` 5432) 정리 (§7.2)
- [ ] 23. Resource·Action wildcard 금지 정책 재확인 (§6.3)
- [ ] 24. Task Execution Role / Task Role 책임 분리 정책(Task Definition `secrets` 우선) 정리 (§6.4)

## 6. Preprocessor 단발 실행 검증 정리 (§7)

- [ ] 25. awsvpc / public subnet / `assignPublicIp = ENABLED` 확인 항목 정리 (§7.1)
- [ ] 26. `preprocessor_app` 기준 RDS 접속 성공 점검 항목 정리 (§7.2, §7.3)
- [ ] 27. CloudWatch Logs 출력 / Task exit code 0 점검 항목 정리 (§7.2)
- [ ] 28. 실행 실패 원인 후보 5건(env / Secret 권한 / SG / VPC Endpoint / image) 정리 (§7.4)

## 7. Crawler 외부 outbound 리스크 별도 정리 (§8)

- [ ] 29. Selenium / Chrome 필요 여부 1차 검토 항목 정리 (§8.1)
- [ ] 30. KRX / Naver / yfinance outbound 도달 여부 1차 검토 항목 정리 (§8.1)
- [ ] 31. public subnet + `assignPublicIp` 도달 검증 항목 정리 (§8.2, §9)
- [ ] 32. 운영 안정화는 본 spec 범위 밖 / 후속 spec·후속 phase 책임 분리 명시 (§8)

## 8. NAT-free 정책 재확인 (§9)

- [ ] 33. NAT Gateway 사용 0건 점검 항목 정리 (§9.1)
- [ ] 34. NAT Gateway 발견 시 OD-NET-001 / OD-NET-002 재확인 흐름 정리 (§9.1)

## 9. 안전 제약 / 산출물 한정 재확인 (§11)

- [ ] 35. 실제 AWS / ECR / ECS / IAM 변경 0건 / 운영자 직접만 재확인 (§11.1)
- [ ] 36. 8개 MS 코드 / docs / 패키징 / Dockerfile 미수정 재확인 (§11.1)
- [ ] 37. 외부 호출 / 크롤링 / 주문 / RDS DDL·DML 0건 재확인 (§11.1)
- [ ] 38. 민감정보 평문 기록 0건(`[REDACTED]` / placeholder) 재확인 (§11.1)
- [ ] 39. 본 08 초기 문서 phase 산출물은 requirements.md / design.md / tasks.md 3개로 한정 재확인 (§11.2)
- [ ] 40. runbook.md / validation-checklist.md / operation-notes.md / CHANGELOG.md / WORKLOG.md 는 운영자 실행 이후 별도 작성 재확인 (§11.2)

## 10. 완료 기준

- [ ] 41. requirements.md / design.md / tasks.md 3개 모두 존재
- [ ] 42. 후속 운영자 단계 인계 핵심 결정 포인트(ECR repo / image tag / Cluster·Role·Log Group / preprocessor 단발 실행 골격) 가 design.md 에 명시됨
- [ ] 43. crawler Selenium / Chrome / KRX·Naver·yfinance 리스크가 §8 에 별도 분리됨

## Task Dependency Graph (간단)

```text
1~5 (ECR Repository)
  └─> 6~9 (Dockerfile 점검)
        └─> 10~13 (로컬 빌드)
              └─> 14~17 (ECR Push)
                    └─> 18~24 (ECS Cluster / Role / Log Group)
                          └─> 25~28 (Preprocessor 단발 실행 검증)
                                ├─> 29~32 (Crawler outbound 리스크)
                                └─> 33~34 (NAT-free 재확인)
                                      └─> 35~40 (안전 제약 / 산출물 한정)
                                            └─> 41~43 (완료 기준)
```
