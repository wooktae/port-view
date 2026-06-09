# Traceability Matrix — 01-aws-migration-foundation

본 문서는 `01-aws-migration-foundation` spec의 요구사항(Requirement N) → 설계(design.md 섹션) → 작업(tasks.md task 번호) → 검증 / 운영자 결정의 매핑을 단일 표로 보여 준다. 본 문서는 매핑 자료이며, 결정값을 변경하지 않는다.

## 컬럼 정의

- Requirement ID: `01-aws-migration-foundation/requirements.md`의 `Requirement N`.
- Requirement Summary: 한 줄 요약. 상세는 원문 참조.
- Design Section: `01-aws-migration-foundation/design.md`의 섹션 제목.
- Task ID: `01-aws-migration-foundation/tasks.md`의 task 번호.
- Validation / Evidence: 본 spec은 문서 산출물 spec이라 evidence는 문서 작성 / 표 확정 / `git status`. 후속 spec에서 실행되는 검증은 해당 spec의 validation-checklist에 위임.
- Related Decision ID: `../_common/operator-decisions.md`의 Decision ID.

## 매핑 표

| Requirement ID | Requirement Summary | Design Section | Task ID | Validation / Evidence | Related Decision ID |
|---|---|---|---|---|---|
| Requirement 1 | 8개 MS별 AWS 컴퓨트 후보 비교와 권고 | `MS별 컴퓨트 후보 비교 및 권고` | 1, 2 | design.md 표 작성 완료, Appendix A 인벤토리 표 | OD-MS-001 ~ OD-MS-009 |
| Requirement 2 | 단일 portfolio DB의 RDS for PostgreSQL 전환 전략 | `RDS for PostgreSQL 전환 설계` | 3, 4 | design.md 환경별 RDS 표, role 매트릭스 표 | OD-DB-001, OD-DB-002, OD-DB-003, OD-DB-004, OD-DB-006, OD-RDS-001, OD-RDS-004, OD-RDS-008, OD-CUT-001 |
| Requirement 3 | 민감정보 외부화(Secrets Manager / SSM) | `Secrets / 환경변수 설계` | 5, 6 | design.md 매핑 표, `[REDACTED]` 일관성 grep 검토 | OD-SEC-001, OD-SEC-002, OD-SEC-003, OD-DB-003 |
| Requirement 4 | 네트워크 / 외부 접근 경로 설계(VPC / SG / EIP / 운영자 접근) | `네트워크 설계` | 7 | design.md VPC 다이어그램 / SG / EIP 권고 / SSM 정책 명시 | OD-ENV-005, OD-NET-003, OD-NET-009 |
| Requirement 5 | 컨테이너 이미지 / ECR / CI/CD 표준 | `컨테이너 이미지와 CI/CD` | 8 | design.md ECR 명명 / CI/CD 비교 / 빌드 시 secret 정책 | OD-MS-005 (packaging), OD-CICD-* (07에서 추가) |
| Requirement 6 | 관측 / 알림 / 운영 Runbook 골격 | `Observability / 알림 / Runbook`, `Runbook 항목` | 9, 10, 11 | design.md log group / 메트릭 / Slack / Runbook 5 시나리오 / 자동 재시도 정책 표 | OD-OBS-001, OD-OBS-002, OD-OBS-003, OD-OBS-004, OD-MS-010, OD-SAFE-004 |
| Requirement 7 | 환경 분리 / 비용 추정 / 6 stage cutover 로드맵 | `환경 분리 / 비용 / 단계적 cutover` | 12, 13 | design.md 환경 표 / 비용 항목 / stage 표(진입/이탈 조건) / 후속 spec 후보 | OD-ENV-001, OD-ENV-002, OD-ENV-003, OD-ENV-004, OD-CUT-003, OD-CUT-004 |
| Requirement 8 | 본 spec 안전 제약과 산출물 범위 | `본 spec의 안전 제약` | 14 | `git status --short` 8개 MS 변경 없음, secret 패턴 grep 결과 `[REDACTED]`만 발견, MS entrypoint 미실행 | (안전 제약, 별도 Decision ID 없음) |

## Acceptance Criteria 단위 보강 매핑

본 spec은 acceptance criteria 단위가 많아 일부 Requirement는 design / task 안에서 세부 항목으로 흩어져 있다. 운영자가 빠르게 추적할 수 있도록 핵심 acceptance criteria를 별도 컬럼으로 보강한다.

| Requirement ID | Acceptance Criteria | Design 위치 | Task | Decision |
|---|---|---|---|---|
| 1.2 | port-marketconnector EC2 1순위 후보 평가 | `port-marketconnector` 단락 | 2 | OD-MS-001, OD-NET-003 |
| 1.4 | strategy decision/execution/research ECS Fargate / AWS Batch / Step Functions 비교 | `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research` 단락 | 2 | OD-MS-006, OD-MS-007, OD-MS-008 |
| 1.5 | port-interest-crawler Selenium/Chrome 한계 명시 | `port-interest-crawler` 단락 | 2 | OD-MS-003 |
| 1.7 | port_strategy_common 별도 컴퓨트 없음 + packaging 권고 | `port_strategy_common` 단락 | 2 | OD-MS-005 |
| 2.5 | pg_dump / pg_restore 1순위, AWS DMS 2순위 | `데이터 이전` 단락 | 3 | OD-CUT-001, OD-CUT-002 |
| 2.7 | live multi-AZ 권고 + dev/paper single-AZ 허용 | `RDS for PostgreSQL 전환 설계` 환경별 인스턴스 표 | 3 | OD-RDS-002, OD-RDS-003 |
| 3.6 | 환경변수 키 호환성 유지(`INTEREST_DB_*`, `PORT_*` 등) | `환경변수 호환성` 단락 | 5 | OD-DB-003 |
| 3.7 | secret 자리 모두 `[REDACTED]` | 전체 본문 | 5, 14 | (안전 제약) |
| 3.8 | access_token.txt 보관 후보 비교 | `access_token.txt 보관 위치` 단락 | 6 | OD-SEC-003 |
| 4.2 | EC2 + EIP 1순위, NAT Gateway EIP 2순위 | `Outbound IP 정책` 단락 | 7 | OD-NET-003 |
| 4.6 | SSH 노출 금지 + SSM Session Manager 사용 | `네트워크 설계` 운영자 접근 단락 | 7 | OD-NET-009, OD-SEC-004 |
| 6.5 | Runbook 5 시나리오 | `Runbook 항목` 단락 | 11 | OD-MS-010 |
| 6.6 | 자동 재시도 허용 / 금지 구분 | `자동 재시도 정책` 단락 | 11 | OD-SAFE-004 |
| 7.3 | 6 stage cutover 로드맵 진입/이탈 조건 | `단계적 cutover 로드맵` 단락 | 12 | OD-ENV-003, OD-ENV-004 |
| 8.4 | 실제 AWS 리소스 미생성 | 본문 안전 제약 | 14 | (안전 제약) |

## 후속 spec 인계

본 spec은 foundation 단계라 Requirement 단위의 실제 검증(예: VPC 생성 결과, RDS endpoint 확인)을 수행하지 않는다. 후속 spec에서 다음과 같이 인계한다.

- Requirement 1, 2, 4, 7 → 02-aws-network-and-rds 의 [`./traceability-matrix.md`](../02-aws-network-and-rds/traceability-matrix.md), [`./validation-checklist.md`](../02-aws-network-and-rds/validation-checklist.md).
- Requirement 3 → 06-secrets-and-iam (예정).
- Requirement 5, 8 → 07-cicd-pipelines (예정), 본 spec 안전 제약은 모든 후속 spec에 그대로 적용.
- Requirement 6 → 05-port-view-ecs-and-runbook (예정), 10-cutover-and-validation-runbook (예정).

## 본 문서 작업 안전 제약

- 본 문서 작성으로 결정값을 변경하지 않는다.
- 8개 MS의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정하지 않는다.
- 실제 AWS 리소스 생성 / 변경 없음.
- 실제 secret 값 출력 없음(모두 `[REDACTED]`).
