# Traceability Matrix — 02-aws-network-and-rds

본 문서는 `02-aws-network-and-rds` spec의 요구사항(Requirement N) → 설계(design.md 섹션) → 작업(tasks.md task 번호) → 검증(validation-checklist.md 섹션 / runbook.md Step) → 운영자 결정의 매핑을 단일 표로 보여 준다. 본 문서는 매핑 자료이며, 결정값을 변경하지 않는다.

## 컬럼 정의

- Requirement ID: `02-aws-network-and-rds/requirements.md`의 `Requirement N`.
- Requirement Summary: 한 줄 요약. 상세는 원문 참조.
- Design Section: `02-aws-network-and-rds/design.md`의 섹션 제목.
- Task ID: `02-aws-network-and-rds/tasks.md`의 task 번호.
- Validation / Evidence: `02-aws-network-and-rds/validation-checklist.md` 섹션 + `02-aws-network-and-rds/runbook.md` Step.
- Related Decision ID: `../_common/operator-decisions.md`의 Decision ID.

## 매핑 표

| Requirement ID | Requirement Summary | Design Section | Task ID | Validation / Evidence | Related Decision ID |
|---|---|---|---|---|---|
| Requirement 1 | 단일 VPC + local-dev / aws-paper / aws-live 환경 분리 | `Architecture` 환경 모델 / `단일 VPC vs prod / non-prod 분리` | 1, 2, 6 | validation §1, §2 / runbook Step 2, 3, 4 | OD-ENV-001, OD-ENV-002, OD-ENV-003, OD-ENV-005 |
| Requirement 2 | public / private (app) / private (data) subnet + NAT-free Route Table | `VPC 다이어그램`, `리소스 배치 정책`, `Route Table 정책 (NAT-free)` | 7, 10 | validation §2 / runbook Step 5, 8 | OD-NET-001, OD-NET-002, OD-NET-004 |
| Requirement 3 | NAT-free 전략 + VPC Endpoint 활성 항목 | `NAT-free 전략과 인터넷 outbound 옵션`, `VPC Endpoint 권고` | 4, 5, 13 | validation §2(NAT 미생성), §4 / runbook Step 7, 11 | OD-NET-001, OD-NET-002, OD-NET-003, OD-NET-004, OD-NET-005, OD-NET-006 |
| Requirement 4 | MS별 Security Group 최소 권한 | `Security Group 설계` | 11, 12 | validation §3 / runbook Step 9, 10 | OD-NET-009, OD-SEC-004 |
| Requirement 5 | aws-paper / aws-live RDS 인스턴스 구성 | `RDS for PostgreSQL 구성` 환경별 인스턴스 표, Parameter Group 권고 | 3, 14, 15, 17 | validation §5 / runbook Step 12, 13, 14, 15 | OD-RDS-001, OD-RDS-002, OD-RDS-003, OD-RDS-004, OD-RDS-005, OD-RDS-006, OD-RDS-007, OD-RDS-008 |
| Requirement 6 | portfolio DB + 10개 schema + search_path 유지 | `portfolio DB와 10개 schema 유지`, `환경변수 호환성` | 18, 19, 22, 23 | validation §6 / runbook Step 16 | OD-DB-001, OD-DB-002, OD-DB-003, OD-DB-006 |
| Requirement 7 | MS별 DB Role 권한 매트릭스 | `DB Role 권한 매트릭스` Role 정의 / 권한 매트릭스 표 | 4 (01 spec 참조), 20, 21 | validation §6 / runbook Step 16 | OD-DB-004, OD-DB-005, OD-SEC-001, OD-SEC-002 |
| Requirement 8 | local-dev → aws-paper → aws-live cutover 절차 | `데이터 cutover 절차` 단계 개요 / 검증 SQL / Rollback 기준 | 27, 28 | validation §7 / runbook Step 17 | OD-CUT-001, OD-CUT-002, OD-CUT-003, OD-CUT-004 |
| Requirement 9 | RDS backup / snapshot / PITR 정책 | `RDS backup / snapshot / PITR` 표 | 25 | validation §5 (PITR / retention) / runbook Step 14, 15 | OD-RDS-006, OD-RDS-007, OD-RDS-008, OD-RDS-009 |
| Requirement 10 | aws-paper / aws-live 자동 BUY/SELL / 자동 재시도 정책 | `안전장치와 자동 재시도 정책` paper / live / 환경 분리 안전장치 | 29 | validation §7 (자동 재시도 / Secret prefix 분리) | OD-SAFE-001, OD-SAFE-002, OD-SAFE-003, OD-SAFE-004 |
| Requirement 11 | 비용 프로파일(low / realistic / stable) | `비용 영향 요약` + decision-matrix.md §11, §12, §13 | 1 | validation §1, §8 / runbook Step 2 | OD-NET-001, OD-NET-002, OD-NET-005, OD-NET-007, OD-NET-008, OD-RDS-002, OD-RDS-003, OD-OBS-002, OD-OBS-003 |
| Requirement 12 | AWS Console 단계별 진행 절차 | `AWS Console 단계별 진행 흐름` 요약 16단계 | 6 ~ 26 | validation §2 ~ §6 / runbook Step 0 ~ 18 | (전반에 걸친 결정) |
| Requirement 13 | AWS Resource 용어집 + 운영자 결정 기록 | (별도 루트 공통 문서) [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md), [`../_common/operator-decisions.md`](../_common/operator-decisions.md) | (전 spec 공통) | validation §1, §9 | (글로벌) |
| Requirement 14 | 본 spec 안전 제약 | `본 spec의 안전 제약` | 전체 | validation §9 (`[REDACTED]` 일관성, MS 코드 / 문서 무수정) | (안전 제약) |

## Acceptance Criteria 단위 보강 매핑

| Requirement ID | Acceptance Criteria | Design 위치 | Task | Validation / Runbook | Decision |
|---|---|---|---|---|---|
| 1.1 | 서울 ap-northeast-2 단일 region | `Architecture` 환경 모델 | 1, 2 | validation §1 / runbook Step 1 | OD-ENV-005 |
| 2.2 | NAT-free Route Table 정책 | `Route Table 정책 (NAT-free)` | 10 | validation §2(rt-app, rt-data 외부 라우트 없음) / runbook Step 8 | OD-NET-001, OD-NET-002 |
| 3.1 | aws-paper / aws-live 모두 NAT GW 미사용 기본안 | `NAT-free 전략과 인터넷 outbound 옵션` | 9 | validation §2(NAT 미생성) / runbook Step 7 | OD-NET-001, OD-NET-002 |
| 3.3 | VPC Endpoint 권고 5종 + STS/KMS 옵션 | `VPC Endpoint 권고` 표 | 5, 13 | validation §4 / runbook Step 11 | OD-NET-005, OD-NET-006 |
| 3.5 | marketconnector EIP 1순위 | `NAT-free 전략과 인터넷 outbound 옵션` 권고 | 4 | validation §3(`sg-marketconnector-ec2` outbound) / runbook Step 10 | OD-NET-003 |
| 4.2 | RDS SG inbound = SG 참조만 | `Security Group 설계` 표 | 12 | validation §3(0.0.0.0/0 없음) / runbook Step 10-1 | OD-SEC-004 |
| 4.3 | EC2 SSH 22 inbound 0/0 금지, SSM Session Manager | `Security Group 설계` 운영자 접근 정책 | 12 | validation §3(SSH 22 0/0 SG 없음) / runbook Step 10 | OD-NET-009, OD-SEC-004 |
| 4.4 | public subnet ECS Task SG inbound 미허용 | `Security Group 설계` 표 | 12 | validation §3(`sg-crawler-tasks`, `sg-preprocessor-tasks` inbound 비어 있음) / runbook Step 10-6, 10-7 | (R-NET-001) |
| 5.2 | aws-paper RDS = `db.t4g.small` single-AZ + retention 7일 + PITR | `RDS for PostgreSQL 구성` 환경별 인스턴스 표 | 17 | validation §5 / runbook Step 14 | OD-RDS-001, OD-RDS-006, OD-RDS-008 |
| 5.6 | parameter group `rds.force_ssl=1` 등 | `Parameter Group 권고` | 15 | validation §5 / runbook Step 13 | OD-RDS-004 |
| 6.4 | 환경변수 키 호환성 유지 | `환경변수 호환성` | 23 | validation §6 / runbook Step 16-3 | OD-DB-003 |
| 7.1 | 7개 role + schema별 권한 매트릭스 | `DB Role 권한 매트릭스` | 20, 21 | validation §6 / runbook Step 16 | OD-DB-004 |
| 7.5 | DB 비밀번호는 placeholder. 최종 결정 06 spec | `DB Role 권한 매트릭스` 본문 + Step 16 placeholder | 16, 20 | validation §6 (`[REDACTED]` 표기) / runbook Step 14-0 | OD-SEC-001, OD-SEC-002 |
| 8.1 | pg_dump+pg_restore 1순위, AWS DMS 보류 | `데이터 cutover 절차` | 27 | validation §7 / runbook Step 17 | OD-CUT-001, OD-CUT-002 |
| 8.4 | 검증 실패 시 local-dev 복귀 rollback | `Rollback 기준` | 28 | validation §7 / runbook Step 17, 19 | OD-CUT-001, OD-CUT-004 |
| 9.1 | backup retention paper 7일 / live 14일 + manual snapshot 시점 + PITR | `RDS backup / snapshot / PITR` 표 | 25 | validation §5(retention 7일, PITR on) / runbook Step 14, 15 | OD-RDS-006, OD-RDS-007, OD-RDS-009, OD-CUT-003 |
| 10.3 | 자동 재시도 금지 step 표 | `안전장치와 자동 재시도 정책` 표 | 29 | validation §7 (자동 재시도 정책 합의) | OD-SAFE-004 |
| 10.4 | aws-paper / aws-live 환경 분리 (Secret prefix 등) | `안전장치와 자동 재시도 정책` 환경 분리 안전장치 | 29 | validation §7 (Secret prefix 분리 합의) | OD-SAFE-002, OD-SAFE-003, OD-SEC-001 |
| 11.1 | aws-paper / aws-live 비용 프로파일 3개 | decision-matrix.md §11, §12 | 1 | validation §1, §8 | (전 결정 합산) |
| 11.2 | 비용 절감 항목별 영향 정리 | decision-matrix.md §13 | 1 | validation §8 | OD-NET-001, OD-NET-007, OD-NET-008, OD-RDS-002, OD-OBS-002, OD-OBS-003 |
| 12.1 | tasks.md Console 단계 분해 | `AWS Console 단계별 진행 흐름` 16단계 | 6 ~ 26 | validation §2 ~ §6 / runbook Step 0 ~ 18 | (전반) |
| 13.1 | aws-resource-glossary 항목 모두 포함 | (루트 공통) [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md) | (전 spec 공통) | validation §9 | (글로벌) |
| 14.5 | secret 자리 `[REDACTED]` 일관성 | 본문 안전 제약 | 전체 | validation §9 (R-DOCS-001 mitigation) | (안전 제약) |

## Risk와의 매핑

본 spec의 핵심 리스크는 [`../_common/risk-register.md`](../_common/risk-register.md)에서 관리한다. Requirement → Risk 매핑 보강.

| Requirement ID | Related Risk ID | mitigation 위치 |
|---|---|---|
| Requirement 2 (NAT-free Route Table) | R-NET-002 | runbook Step 5, 8 / validation §2 |
| Requirement 3 (NAT-free + Endpoint) | R-NET-003, R-COST-002 | runbook Step 7, 11 / validation §2(NAT), §4(Endpoint) |
| Requirement 4 (SG 최소 권한) | R-NET-001, R-SEC-001 | runbook Step 9, 10 / validation §3 |
| Requirement 5 (RDS 구성) | R-SEC-001, R-COST-001 | runbook Step 14, 15 / validation §5, §8 |
| Requirement 6 (search_path 유지) | R-DATA-001 | runbook Step 16 / validation §6 |
| Requirement 8 (cutover) | R-DATA-002 | runbook Step 17 / validation §7 |
| Requirement 10 (자동 재시도 정책) | R-AUTO-001, R-AUTO-002 | runbook Step 17 (paper Secret 분리) / validation §7 |
| Requirement 11 (비용 프로파일) | R-COST-001, R-COST-002 | decision-matrix §11, §12, §13 / validation §8 |
| Requirement 14 (안전 제약) | R-DOCS-001 | 본문 안전 제약 / validation §9 |

## 후속 spec 인계

본 spec의 결정 / 산출물은 다음 후속 spec에 인계된다.

- 06-secrets-and-iam (예정): Secrets 보관 / IAM 매트릭스 최종 결정. Requirement 7, 10의 secret placeholder 락.
- 03-marketconnector-ec2 (예정): EIP / SSM 운영자 접근 / access_token 절차. Requirement 3, 4 적용.
- 08-interest-crawler-and-preprocessor-ecs (예정): public subnet 배치 + OPT-1 / OPT-3. Requirement 3, 4 적용.
- 04-strategy-batch-stepfunctions (예정): Step Functions 자동 재시도 정책. Requirement 10 적용.
- 05-port-view-ecs-and-runbook (예정): ALB 도입 결정 + view_app execution write 재검토. Requirement 7 보강.
- 10-cutover-and-validation-runbook (예정): 실제 cutover 통합. Requirement 8, 9 실행.

## 본 문서 작업 안전 제약

- 본 문서 작성으로 결정값을 변경하지 않는다.
- 8개 MS의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정하지 않는다.
- 실제 AWS 리소스 생성 / 변경 없음.
- 실제 secret 값 출력 없음(모두 `[REDACTED]`).
