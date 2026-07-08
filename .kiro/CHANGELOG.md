# Kiro AWS Migration Changelog

본 문서는 `.kiro` 작업공간 안의 AWS Migration spec 문서 변경 이력만 간단히 기록한다.

## 작성 원칙

- spec 구조 변경, 루트 공통 문서 변경, 신규 spec 생성, 주요 문서 재구성만 기록한다.
- 8개 MS의 세부 코드 변경 이력은 본 문서에 기록하지 않는다.
- 너무 자세한 일일 작업 로그는 `.kiro/WORKLOG.md`에 남기고, 본 문서에는 의미 있는 변경만 짧게 정리한다.
- 항목 분류는 `Added`, `Changed`, `Removed`, `Security`로 통일한다.
- 날짜는 한국 기준의 작업 일자를 사용한다.

## 2026-07-06 (kiro-common-docs-readability-9 · 9차 회차 · _common 6개 문서 구조 재편)

### Added

- 🟢 `_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | 상단 신설 | `## Purpose` H2 헤더 |
  | 문서 흐름 앞부분 | Purpose → Review Needed → Status Legend → Decision Dashboard |
  | 문서 흐름 중간부 | At a Glance → Decision Index → Open/Tentative/Deferred Decisions |
  | 문서 흐름 뒷부분 | Decision Details → Change Log → Decision Change Log Details |
  | 문서 흐름 끝 | Decision Update Rules → Security Notes |
  | 원칙 | 원문 보존 · `[REDACTED*]` placeholder 정책 명시 |

- 🟢 `_common/risk-register.md`

  | 항목 | 값 |
  | --- | --- |
  | 상단 신설 | `## Purpose` H2 헤더 |
  | 문서 흐름 앞부분 | Purpose → Risk Dashboard → Immediate Action Risks → Risk Index |
  | 문서 흐름 뒷부분 | Risk Details → Accepted/Closed Risks → Risk Update Rules → Security Notes |
  | 원칙 | Risk ID 재사용 금지 · raw evidence 인용 금지 · `[REDACTED*]` placeholder 정책 |
  | 신설 H2 | `## Accepted/Closed Risks` (R-AUTO-024 · R-AUTO-027 요약 표) |

- 🟢 `_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 상단 신설 | `## Purpose` H2 헤더 |
  | 신설 H2 | `## Spec Roadmap` |
  | Spec Roadmap 하위 H3 | 환경 모델 · 진행 순서 · 각 후속 Spec 요약 · 의존성 다이어그램 |
  | 추가 신설 | `## Update Rules` H2 |

- 🟢 `_common/aws-resource-glossary.md`

  | 항목 | 값 |
  | --- | --- |
  | 상단 신설 | `## Purpose` H2 헤더 |
  | 신설 H2 | `## Glossary` (38개 서비스 항목 H3 강등) |
  | 추가 신설 H2 | `## Usage Notes` |
  | Usage Notes 대상 앞부분 | EventBridge Scheduler · Lambda · IAM Role |
  | Usage Notes 대상 뒷부분 | ECS/Fargate · Step Functions (이관 우선순위 명시) |
  | 추가 신설 | `## Update Rules` H2 |

- 🟢 `_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | 상단 신설 | `## Purpose` H2 (서비스 선택 결론 불변 명시) |
  | 신설 H2 | `## Rejected/Deferred Services` |
  | Rejected/Deferred 하위 H3 | chapter 7 (EKS) · chapter 8 (Lambda) · chapter 9 (Elastic Beanstalk / App Runner) |
  | 추가 신설 H2 | `## Appendix` |
  | Appendix 하위 H3 | chapter 10 최종 결론 · Evidence Details |

- 🟢 `_common/cost-simulation.md`

  | 항목 | 값 |
  | --- | --- |
  | 상단 신설 | `## Purpose` H2 헤더 |
  | 방침 | 최소 변경 · 신규 비용 수치 추가 회피 |

### Changed

- 🟢 `_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | Rename | `## Decision Summary` → `## Decision Dashboard` (재집계 필요 경고 명시) |
  | Rename | `## Detailed Decisions` → `## Decision Index` (표 인덱스 역할 안내 추가) |
  | H2 → H3 강등 | `## Decision Details — Compute / Service Placement` 중복 H2 |
  | 이동 | `Deferred Decisions` → `Decision Details` 앞 |
  | 이동 | `Decision Update Rules` → `Change Log Details` 뒤 |
  | Rename + 이동 | `본 spec 작업 안전 제약` → `Security Notes` (파일 끝) |

- 🟢 `_common/risk-register.md`

  | 항목 | 값 |
  | --- | --- |
  | Rename | `## Risk Table` → `## Risk Index` |
  | H2 → H3 강등 | `## 컬럼 정의` · `## 상태 정의` |
  | Rename | `추가 식별 시 갱신 규칙` → `Risk Update Rules` |
  | Rename | `본 문서 작업 안전 제약` → `Security Notes` |

- 🟢 `_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | H2 → H3 강등 앞부분 | `## 환경 모델` · `## 진행 순서` |
  | H2 → H3 강등 뒷부분 | `## 각 후속 Spec 요약` · `## 의존성 다이어그램` |
  | 통합 위치 | `Spec Roadmap` 하위 |
  | Rename | `공통 작업 범위 제한` → `Security Notes` |

- 🟢 `_common/aws-resource-glossary.md`

  | 항목 | 값 |
  | --- | --- |
  | Rename | `## 카테고리별 목차` → `## Category TOC` |
  | H2 → H3 강등 | 38개 서비스 개별 H2 |
  | 강등 예시 A | VPC · Subnet · NAT Gateway · EC2 |
  | 강등 예시 B | ECS · Fargate · RDS · Secrets Manager |
  | 강등 예시 C | IAM Role · EventBridge Scheduler · Step Functions · Lambda |
  | 강등 예시 D | S3 · AWS Batch · KMS 등 |
  | Rename | `본 spec 작업 안전 제약` → `Security Notes` |

- 🟢 `_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | Rename | `## 5. MS별 최종 권고안` → `## MS Decision Cards` |
  | Rename | `## 6. 포트폴리오 어필 관점의 보강안` → `## Portfolio Appeal Notes` |
  | 흡수 대상 앞부분 | `## 7. EKS 검토 섹션` · `## 8. Lambda 검토 섹션` |
  | 흡수 대상 뒷부분 | `## 9. Elastic Beanstalk / App Runner 검토 섹션` |
  | 흡수 위치 | `Rejected/Deferred Services` 하위 H3 |
  | 흡수 대상 (Appendix) | `## 10. 최종 결론` · `## Evidence Details` |
  | 흡수 위치 | `Appendix` 하위 H3 |
  | 신설 | `## Security Notes` |

- 🟢 `_common/cost-simulation.md`

  | 항목 | 값 |
  | --- | --- |
  | Rename | `## 8. 안전 제약` → `## Security Notes` |
  | 변경 없음 | 큰 구조 · 비용 수치 · 시나리오 라벨 · USD count |

### Security

- 🟢 신규 민감정보 원문 도입 (6개 `_common` 문서 공통)

  | 항목 | 값 |
  | --- | --- |
  | secret · password · token · webhook URL | 0건 |
  | KIS app key · KIS app secret | 0건 |
  | 계좌번호 · account-id · 실제 ARN | 0건 |
  | public IP · broker_order_no · image digest full sha256 | 0건 |

- 🟢 `[REDACTED*]` placeholder count (baseline 이상 유지)

  | 문서 | 값 |
  | --- | --- |
  | operator-decisions | 37 → 42 |
  | risk-register | 7 → 13 |
  | followups-overview | 23 → 29 |
  | aws-resource-glossary | 11 → 17 |
  | ms-aws-service-decision-matrix | 2 → 9 |
  | cost-simulation | 3 → 3 |

- 🟢 인코딩 처리

  | 항목 | 값 |
  | --- | --- |
  | `risk-register.md` pre-existing UTF-8 BOM | Option A (제거) |
  | 나머지 5개 문서 | baseline 부터 UTF-8 No BOM |
  | 6개 대상 문서 최종 저장 인코딩 | UTF-8 No BOM 통일 |

- 🟢 실행 카테고리 (각 0건)

  | 항목 | 값 |
  | --- | --- |
  | AWS · DB · psql · Spring Boot 실행 | 0건 |
  | Slack webhook · Step Functions · Lambda · ECS 실행 | 0건 |
  | SSM · EC2 · KIS API · broker 실행 | 0건 |
  | 크롤러 · Selenium 실행 | 0건 |
  | 자동 매수 · 자동 매도 · fill sync · position sync 실행 | 0건 |
  | intraday monitor 실행 | 0건 |
  | 쓰기 계열 · 롤백 계열 git 명령 실행 | 0건 |

## 2026-07-02 (kiro-common-docs-readability · 8차 회차 · _common 문서 라인 밀도 완화)

### Changed

- 🟢 `_common/cost-simulation.md`

  | 항목 | 값 |
  | --- | --- |
  | 재구성 | paper / live realistic 주요 구성 인라인 bullet → 표 3컬럼 |
  | 표 컬럼 | 카테고리 · 항목 · USD/월 근사치 |
  | 서두 | 안내 3줄 → 2단락 분할 |
  | >300 라인 | 1 → 0 |
  | max 라인 | 320 → 296 |

- 🟢 `_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | 분할 대상 앞부분 | Evidence Details 4.2 (2026-06-30 오후) |
  | 분할 대상 뒷부분 | Evidence Details 4.3 (2026-06-23 · 2026-06-24) |
  | 처리 | 500자 초과 서술 → bullet 다중 라인 |
  | 재조합 대상 | Dispatcher Lambda 사양 · Slack 통과 검증 나열식 → 표/bullet |
  | >500 라인 | 2 → 0 |
  | max 라인 | 514 → 488 |

- 🟢 `_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 분할 대상 앞부분 | 2026-06-13 (Strategy Execution 책임 분리) |
  | 분할 대상 중간부 | 2026-06-17 (Daily wrapper 결정 락) |
  | 분할 대상 뒷부분 | 2026-06-18 (connector_order_check 결정 락) |
  | 처리 | 3문장 이상 · 500자 이상 → bullet 분할 |
  | >500 라인 | 19 → 17 |
  | max 라인 | 544 → 530 |

- 🟢 `_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | 분할 대상 앞부분 | Change Log Details 2026-06-16 (safe subset 안전 기준) |
  | 분할 대상 중간부 | 2026-06-23 (OD-SAFE-* 실증) |
  | 분할 대상 뒷부분 | 2026-06-29 (2) (R-AUTO-034 신규 mitigation) |
  | 처리 | 500자 초과 근거 → bullet 분할 |
  | >500 라인 | 6 → 3 |
  | max 라인 | 557 → 529 |

### Security

- 🟢 신규 민감정보 원문 도입 (6개 `_common` 문서 공통)

  | 항목 | 값 |
  | --- | --- |
  | secret · password · token · webhook URL | 0건 |
  | 실제 ARN · 실제 IP · broker_order_no | 0건 |
  | `[REDACTED*]` placeholder count | before ≤ after 유지 |
  | `risk-register.md` pre-existing BOM (1개) | 상태 그대로 유지 |
  | 신규 BOM 도입 | 0건 |

## 2026-07-01 (kiro-common-docs-readability spec 실행 · _common 문서 Dashboard-first 재구성)

### Added

- 🟢 `_common/risk-register.md` 상단에 `Risk Dashboard` 섹션 신규 추가

  | 항목 | 값 |
  | --- | --- |
  | 그룹 앞부분 | Open High risks · Mitigated High risks |
  | 그룹 뒷부분 | Newly added risks · Risks needing operator action |
  | 형식 | 표 형식 (운영자 매일 조회 용) |
  | 상태 배지 앞부분 | 🔴 Open High · 🟠 Open · 🟢 Mitigated |
  | 상태 배지 뒷부분 | 🔵 Accepted · ⚫ Closed |

- 🟢 `_common/followups-overview.md` 상단에 `Current Follow-up Dashboard` 섹션 신규 추가

  | 항목 | 값 |
  | --- | --- |
  | 그룹 앞부분 | Now · Next · Later |
  | 그룹 뒷부분 | Blocked · Done recently |
  | Now/Next/Later 형식 | `- [ ]` 체크리스트 |
  | Blocked/Done recently 형식 | 표 형식 |
  | 링크 병기 | 관련 spec `operation-notes.md` 상대경로 |

- 🟢 `_common/ms-aws-service-decision-matrix.md` 상단에 `Final Recommendation Summary` 섹션 신규 추가

  | 항목 | 값 |
  | --- | --- |
  | 표 크기 | 8개 MS 고정 순서 · 5컬럼 |
  | 5컬럼 앞부분 | MS · 1순위 서비스 · 2순위/보류 |
  | 5컬럼 뒷부분 | 채택 이유 · 비용/운영 리스크 |
  | 추가 | 비권고 서비스 요약 |

- 🟢 `_common/cost-simulation.md` 상단에 `Cost Dashboard` 섹션 신규 추가

  | 항목 | 값 |
  | --- | --- |
  | 비용 라벨 | paper realistic · live realistic 월 예상 비용 |
  | 추가 요약 앞부분 | cost driver Top 5 |
  | 추가 요약 뒷부분 | 비용 절감 결정 Top 5 |
  | 반복 단서 | `AWS Pricing Calculator 확인 필요` |

- 🟢 `_common/aws-resource-glossary.md` 상단에 카테고리별 목차(TOC) 섹션 신규 추가

  | 항목 | 값 |
  | --- | --- |
  | 카테고리 앞부분 | Network · Compute · Database |
  | 카테고리 중간부 | Security/IAM/Secrets · Orchestration |
  | 카테고리 뒷부분 | Observability · Storage/Artifact |
  | 링크 형식 | 앵커 링크 |

### Changed

- 🟢 `_common/operator-decisions.md` (파일 변경 없음)

  | 항목 | 값 |
  | --- | --- |
  | 사유 | 이미 요구 골격 만족 |
  | 골격 앞부분 | Status Legend → Decision Summary → At a Glance |
  | 골격 뒷부분 | Detailed Decisions → Change Log |

- 🟢 Dashboard-first 골격 상단 배치

  | 항목 | 값 |
  | --- | --- |
  | 적용 문서 | `_common` 나머지 5개 |
  | 정책 | Detail-behind (기존 상세 뒤에 그대로 보존) |
  | 보존 대상 앞부분 | Risk Table · Risk Details · 진행 순서 |
  | 보존 대상 뒷부분 | 날짜별 후속 메모 · chapter 1 ~ 10 · Change Log |

### Security

- 🟢 실행 카테고리 (각 0건)

  | 항목 | 값 |
  | --- | --- |
  | Kiro 실행 명령 | 0건 |
  | AWS CLI · boto3 · psql · Spring Boot 실행 | 0건 |
  | broker · KIS · 크롤러 실행 | 0건 |
  | 자동 매수 · 자동 매도 · fill sync · position sync 실행 | 0건 |
  | intraday monitor 실행 | 0건 |

- 🟢 변경 범위 제한

  | 항목 | 값 |
  | --- | --- |
  | 8개 MS 저장소 변경 | 0건 |
  | `_common/` 외 spec 폴더 변경 | 0건 |
  | 신규 민감정보 평문 기록 | 0건 |
  | placeholder 정책 | `[REDACTED]` 계열 유지 |

- 🟢 git 작업

  | 항목 | 값 |
  | --- | --- |
  | 자동 롤백 | 미수행 |
  | 쓰기 계열 git 앞부분 | `git add` · `git commit` |
  | 쓰기 계열 git 뒷부분 | `git rm` · `git mv` · `git push` |
  | 쓰기 계열 실행 | 0건 |

## 2026-07-01 (paper Daily 자동화 1차 풀 ON + Step 12~17 Scheduler ENABLED)

### Added

- 🟢 **Step 12~17 수동 실행 SUCCEEDED 결과 추가**

  | 항목 | 값 |
  | --- | --- |
  | executionName | `port-manual-daily-step12-17-20260701-043747` |
  | State machine | `portfolio-paper-daily-step12-17-approval` |
  | Status | 🟢 **SUCCEEDED** |
  | Start | `2026-07-01T13:37:47.856+09:00` |
  | Stop | `2026-07-01T13:40:41.212+09:00` |
  | Execution history | `ExecutionSucceeded` |
  | Slack | 3종 수신 (07:50 장전 · 08:24 승인 필요 · Step 12~17 성공) |

  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **DB after-check 통과 결과 추가**

  | 항목 | 값 |
  | --- | --- |
  | `strategy_execution_order` (2026-07-01) | 0건 |
  | REQUESTED 전략 주문 | 0건 |
  | active `connector_order_request` | 0건 |
  | 오늘 connector 주문 | 0건 |
  | 최신 `connector_balance_snapshot` | `id=321` |
  | `as_of_date` | `2026-07-01` |
  | `total_eval_amount` | `8,706,505` |
  | `cash_balance` | `8,706,505` |
  | `eval_profit` | `0` |
  | Step 12~17 판정 | 🔵 **NO_TARGET** 안전 종료 |

  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **paper Daily 자동화 라인업 7종 ENABLED 확인 결과 추가**

  | 시각 | 작업 | 상태 |
  | --- | --- | --- |
  | 07:50 | EC2 start | 🟢 **ENABLED** |
  | 07:50 | 장전 Slack | 🟢 **ENABLED** |
  | 08:00 | Step 1~11 | 🟢 **ENABLED** |
  | **09:01** | **Step 12~17 (본 일자 ENABLED)** | 🟢 **ENABLED** |
  | 09:10~15:50 | 10분 장중 손절 | 🟢 **ENABLED** |
  | 15:50 | 장후 Slack | 🟢 **ENABLED** |
  | 15:50 | MarketConnector stop | 🟢 **ENABLED** |

  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **R-AUTO-037 신규 row 추가**

  | 항목 | 값 |
  | --- | --- |
  | 위험 | Step 12~17 자동 실행이 Step 1~11 실패 또는 데이터 미준비 상태에서도 실행될 위험 |
  | Impact | `High` |
  | Probability | `Low` |
  | Status | 🟢 **Mitigated** |
  | Affected Spec | `04, 05, 10` |

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | 항목 | 값 |
  | --- | --- |
  | R-AUTO-001 | [2026-07-01 보강] mitigation 메모 추가 |
  | 핵심 정책 | BUY / SELL / fill sync / position 변경 step 자동 재시도 금지 유지 |
  | 추가 관찰 | Step 12~17 Scheduler ENABLED 후에도 Step Functions Retry 정책 검토 대상 유지 |
  | Status | 🟠 **Open** |
  | R-AUTO-025 | [2026-07-01 자동 ENABLE 진입] mitigation 메모 추가 |
  | 변경 요약 | Step 12~17 Scheduler 🟠 **DISABLED** → 🟢 **ENABLED** |
  | 진입 조건 | 09:01 schedule 보류 정책이 운영자 승인 후 ENABLE 통과 |
  | Status | 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | Change Log | 2026-07-01 row 추가 |
  | Evidence 보강 | `OD-SAFE-001` · `OD-SAFE-002` · `OD-SAFE-003` · `OD-MS-009` · `OD-MS-032` · `OD-MS-033` |
  | 신규 결정 | 없음 |
  | Decision Summary | 전체 97 · 확정 52 · 잠정 42 유지 |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-07-01 후속 메모 | 추가 |
  | 완료 | 3건 |
  | 남은 후속 | 5건 |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | 보강 범위 | port_strategy_execution · Daily Batch orchestration · EventBridge Scheduler |
  | 핵심 근거 | paper Step 1~11 Scheduler + Step 12~17 Scheduler 모두 🟢 **ENABLED** |
  | Step 12~17 수동 실행 | 🟢 **SUCCEEDED** |
  | 자동 실행 라인업 | 확인 완료 |
  | 서비스 선택 | 변경 없음 |

- 🟢 `.kiro/specs/_common/cost-simulation.md`

  | 항목 | 값 |
  | --- | --- |
  | 트리거 | Step 12~17 Scheduler 🟠 **DISABLED** → 🟢 **ENABLED** |
  | 신규 상시 컴퓨트 비용 | 없음 |
  | EventBridge Scheduler | 무료 한도 안 |
  | Step Functions transitions | 매우 작음 |
  | RDS · EC2 · Fargate · NAT · ALB | 비용 변경 없음 |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | 항목 | 값 |
  | --- | --- |
  | 대상 항목 | EventBridge Scheduler |
  | 운영 예시 보강 | `portfolio-paper-daily-step12-17-order-0901-kst` |
  | 시작 일자 | 2026-07-01 부터 🟢 **ENABLED** |
  | 역할 | paper Step 12~17 자동 실행 담당 |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | Append 섹션 | `2026-07-01 — paper Daily Step 12~17 자동 실행 ENABLED` |
  | Step 12~17 수동 실행 | 🟢 **SUCCEEDED** |
  | DB after-check | 통과 |
  | Scheduler | 🟢 **ENABLED** |
  | Target Input | `{"scheduleType":"STEP12_17_ORDER","dryRun":false}` |
  | 전체 Daily 라인업 | 확인 완료 |
  | Step Functions 구조 | 변경 없음 |
  | aws-live 정책 | 변경 없음 |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-30 ECS View 수동 승인 경로 | 이미 통과 |
  | 2026-07-01 실행 경로 | View 수동 승인 없이 Step 12~17 자동 Scheduler 🟢 **ENABLED** |
  | port-view 역할 | 조회 · 승인 · 운영 UI 유지 |
  | paper 자동 주문 | 09:01 Scheduler 경로 전환 |
  | View 수동 실행 | 운영자 fallback 유지 |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/runbook.md`

  | 항목 | 값 |
  | --- | --- |
  | 추가 절차 | Step 12~17 Scheduler enable / disable 운영 |
  | 절차 항목 | 현재 상태 확인 · Target.Input 확인 · DISABLED → ENABLED · ENABLED → DISABLED rollback |
  | 확인 명령 | `list-schedules` · `get-schedule` |
  | 라인업 확인 | 전체 Daily 라인업 |
  | 주의 | 새 start-execution 중복 생성 |
  | 보안 | ARN · account id · secret 평문 기록 <span style="color:#D1242F">**금지**</span> |
  | Marker 정책 | 실패 뒤 SUCCESS marker <span style="color:#D1242F">**금지**</span> |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/validation-checklist.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-07-01 validation | append |
  | Step 12~17 manual execution | 🟢 **SUCCEEDED** |
  | Slack | received |
  | DB after-check | passed |
  | Step 12~17 Scheduler | 🟢 **ENABLED** |
  | Full Daily automation lineup | checked |
  | active connector order | none |
  | today connector order | none |
  | latest balance snapshot | 2026-07-01 |

- 🟢 `.kiro/specs/06-secrets-and-iam/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | IAM Role 변경 | 없음 |
  | 근거 | 기존 Scheduler role · Dispatcher Lambda 권한 경로 동작 evidence |
  | 새 secret 생성 | 0건 |
  | 새 IAM policy 생성 | 0건 |
  | 새 Role 생성 | 0건 |

### Security

| 항목 | 결과 |
| --- | --- |
| AWS CLI · boto3 · psql · Spring Boot · 외부 API 실행 | 0건 |
| broker 주문 제출 | 0건 |
| aws-live 작업 | 0건 |
| secret 원문 기록 | 0건 |

> Kiro 는 본 작업에서 문서만 수정했다.

## 2026-06-30 (오후) (port-view ECS Fargate Public IP 1차 포팅 완료 + ECS View → AWS Step Functions Step 12~17 승인 실행 통과 + desiredCount 0 종료)

### Added

- 🟢 **port-view ECS Fargate Public IP 1차 포팅 통과 결과 추가**

  | 항목 | 값 |
  | --- | --- |
  | 수행 | 운영자 직접 |
  | 네트워크 | ALB 미사용 · public subnet · `assignPublicIp=ENABLED` · NAT Gateway 미사용 |
  | SG inbound | TCP 8080 운영자 IP/32 한정 |
  | CloudWatch Logs | retention 7일 |
  | ECR | repository `portfolio-view` 생성 · image push |
  | Task definition | `portfolio-view:1` → `portfolio-view:2` (기본 계좌번호 env 누락 보정) |
  | Service | `portfolio-view-service` 🟢 **RUNNING** |
  | 화면 조회 | Fargate public IP 직접 접속 · Dashboard · Balance · Positions · Orders · Reports · Daily 통과 |

  - 상세: [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md)

- 🟢 **ECS View → AWS Step Functions Step 12~17 승인 실행 통과 결과 추가**

  | 항목 | 값 |
  | --- | --- |
  | executionName | `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` |
  | State machine | `portfolio-paper-daily-step12-17-approval` |
  | Status | 🟢 **SUCCEEDED** |
  | Start | `2026-06-30T14:15:42.899+09:00` |
  | Stop | `2026-06-30T14:18:48.358+09:00` |
  | Slack | `DAILY_EXECUTION_SUCCESS` 수신 |
  | 종료 판정 | 🔵 **NO_TARGET** 안전 종료 |

  - 상세: [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md)

- 🟢 **DB after-check 통과 결과 추가**

  | 항목 | 값 |
  | --- | --- |
  | REQUESTED `strategy_execution_order` | 0건 |
  | retryable rejected | 0건 |
  | active `connector_order_request` | 0건 |
  | today connector orders | 0건 |
  | 최신 `connector_balance_snapshot` | `id=281` |
  | `as_of_date` | `2026-06-30` |
  | `total_eval_amount` | `8,706,505` |
  | `cash_balance` | `8,706,505` |

  - 과거 stale `connector_order_request` 6건 식별 (2026-04-27 ACCEPTED 잔여 · 후속 cleanup 후보)
  - 상세: [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md)
- 🟢 **View 운영 경로 5종 정합 정리** — 기존 4종 + ECS View → AWS Step Functions Step 12~17 승인 실행(본 일자 오후) 추가.
- 🟢 **`.kiro/specs/05-port-view-ecs-and-runbook/runbook.md` 신규 생성** — ECS service desiredCount 0/1 운영 명령 + public IP 자동 조회 패턴 + 브라우저 URL 출력 + AWS CLI `list/describe → 변수 추출 → 후속 검증` 패턴 + 운영자 IP 변경 시 SG inbound 갱신 절차 + Step 12~17 승인 실행 절차.
- 🟢 **`.kiro/specs/05-port-view-ecs-and-runbook/validation-checklist.md` 신규 생성** — 13개 체크 항목 + 본 일자 통과 결과.
- 🟢 **`.kiro/AGENTS.md` 운영 명령 작성 규칙 3종 추가** — (1) AWS CLI 명령은 ARN / task ARN / ENI ID / LOG_STREAM 수동 치환 없이 `list/describe → 변수 추출 → 후속 검증` 패턴, (2) psql 검증 쿼리는 `information_schema.columns` 사전 확인(추정 컬럼명 사용 금지), (3) 실패한 SQL/명령 뒤에 SUCCESS/DONE marker 금지.

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | 항목 | 값 |
  | --- | --- |
  | R-AUTO-033 | [2026-06-30 오후 보강] mitigation 메모 추가 |
  | 근거 | Public IP direct access + 운영자 IP/32 SG + ECS View → Step 12~17 approval 3차 실증 |
  | Status | 🟢 **Mitigated** |
  | R-AUTO-034 | [2026-06-30 오후 보강] mitigation 메모 추가 |
  | 근거 | Fargate task role `portfolio-paper-view-task-role` 의 `states:StartExecution` 권한이 Step 12~17 approval state machine ARN 한정 부여 1차 실증 |
  | 잔여 | Fargate Task Role 권한 분리는 06 spec 후속 phase 책임 유지 |
  | Status | 🟠 **Open** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-30 (오후) 후속 메모 | 추가 |
  | 완료 | 4건 |
  | 후속 | 6건 |
  | 신규 위험 메모 | Public IP · desiredCount · 추정 컬럼명 · stale `connector_order_request` |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | Change Log | 2026-06-30 (오후) 항목 추가 |
  | Evidence 보강 | `OD-MS-002` · `OD-MS-009` · `OD-MS-037` · `OD-SAFE-001` ~ `OD-SAFE-004` |
  | 본문 변경 | 없음 |
  | 본 일자 신규 결정 | 없음 |
  | Decision Summary | 전체 96 · 확정 51 · 잠정 42 유지 |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | 대상 행 | port-view 4.2 |
  | 추가 메모 | 2026-06-30 (오후) ECS Fargate Service 1차 실증 완료 |
  | 보류 | ALB · HTTPS · Route53 · Cloudflare Tunnel |
  | 1차 선택 아님 유지 | Elastic Beanstalk · App Runner · Lambda |
  | 운영 메모 | desiredCount 0/1 수동 운영 |

- 🟢 `.kiro/specs/_common/cost-simulation.md`

  | 항목 | 값 |
  | --- | --- |
  | 대상 | port-view Fargate 비용 가정 |
  | 2026-06-30 (오후) 메모 | 추가 |
  | 컴퓨트 가정 | 0.5 vCPU · 1 GB 상시 vs desiredCount 1 일시 운영 |
  | Public IPv4 | 비용 추가 |
  | 제외 | ALB · NAT |
  | 안내 | AWS Pricing Calculator 재확인 문구 유지 |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | 항목 | 값 |
  | --- | --- |
  | 보강 대상 | ECS Fargate · IAM Role · IAM Policy · public subnet · CloudWatch Logs |
  | 운영 예시 | `portfolio-view-service` · `portfolio-view:2` |
  | 네트워크 | `assignPublicIp=ENABLED` |
  | Scale | desiredCount 0/1 |
  | 접근 제어 | 운영자 IP/32 SG |
  | Logs retention | 7일 |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | Append 섹션 | "3. ECS Fargate 포팅: 완료" 전체 block |
  | 세부 block | 1)~6) + 운영 절차 + 검증 체크리스트 + 결정/리스크 매핑 + 사실 기록 범위 |
  | 기준 | 2026-06-30 오후 단일 기준 block |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-30 (오후) cross-reference | append |
  | 실행 흐름 | ECS View → `portfolio-paper-daily-step12-17-approval` `StartExecution` |
  | Status | 🟢 **SUCCEEDED** |
  | Slack | 수신 |
  | Step Functions 구조 | 변경 없음 |

- 🟢 `.kiro/specs/06-secrets-and-iam/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-30 (오후) 결과 | append |
  | Task role | `portfolio-paper-view-task-role` |
  | Execution role | `portfolio-paper-ecs-task-execution-role` |
  | 검증 | 분리 검증 통과 |

- 🟢 `.kiro/README.md`

  | 항목 | 값 |
  | --- | --- |
  | 갱신 섹션 | "현재 진행 상태 요약" 의 port-view / 05 spec 관련 라인 |
  | 반영 사실 | 2026-06-30 (오후) ECS Fargate 1차 검증 완료 |
  | 반영 식별자 | executionName + desiredCount 0 종료 |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 갱신 위치 | 2026-06-30 (오후) 섹션 prepend |
  | 분량 | 5~10줄 요약 |
  | 구분 | 오전 Local View 검증과 별도 섹션 |
  | 종료 조건 | ECS service desiredCount 0 종료까지 포함 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret 원문 (KIS app key/secret · 계좌번호 · token · RDS password · IAM/state machine ARN · Slack webhook URL 등) | 0건 |
| AWS CLI · boto3 · psql · Spring Boot · 외부 API 실행 | 0건 |
| CloudWatch · SFN execution history · Lambda / KIS response body · Spring log · Slack payload · DB after-check raw · ENI/TASK/LOG_STREAM raw 인용 | 0건 |
| broker 신규 주문 (BUY / SELL / 취소 / 정정) | 0건 |
| fill · position sync 자동 재시도 | 0건 |
| aws-live 작업 | 0건 |
| commit · add · reset · checkout · stash | 0건 |

> broker · KIS 호출은 오전 Step 1~11 auto trigger + 오전 Local View → Step 12~17 approval + 본 일자 오후 ECS View → Step 12~17 approval 각 1건 (모두 `SUCCEEDED` / NO_TARGET / broker 주문 제출 0건) + balance refresh 한정.

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- ECS Fargate — cluster `portfolio-paper-cluster` · service `portfolio-view-service` · task def `portfolio-view:2` · ECR `portfolio-view` · Logs `/ecs/portfolio-view` · SG `sgroup-port-view-ecs`.
- IAM — task execution role `portfolio-paper-ecs-task-execution-role` · task role `portfolio-paper-view-task-role`.
- Task 파라미터 — launch type `FARGATE` · awsvpc · cpu 512 · memory 1024 · container port 8080.
- Step Functions — executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` · state machine `portfolio-paper-daily-step12-17-approval` · status `SUCCEEDED` · start `2026-06-30T14:15:42.899+09:00` · stop `2026-06-30T14:18:48.358+09:00` · Slack `DAILY_EXECUTION_SUCCESS`.
- balance snapshot — `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505`.
- placeholder — `[REDACTED]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_PUBLIC_IP]` · `[REDACTED_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_SECRET_ARN]` · `[REDACTED_BROKER_ORDER_NO]`.

</details>

## 2026-06-30 (오후) (Slack 문구 개선 최종 완료 — Approval Required Builder + Daily Brief Builder + notifier formatter + mini Step Functions + 장전/장후 Scheduler)

### Added

- 🟢 **Approval Required Slack Builder 연동 완료 결과 추가**

  | 항목 | 값 |
  | --- | --- |
  | Builder Lambda | `portfolio-approval-slack-summary-builder` (신규) |
  | State machine | `portfolio-paper-daily-step1-17-approval` |
  | 흐름 반영 | `StopCrawlerEc2AfterStep11Success → BuildApprovalSlackPayload → SendApprovalRequiredSlack → Step12_CheckApproval` |
  | revisionId | `da8642c6-8409-41b6-ad57-e066ff672332` |
  | Smoke | Builder output → Notifier Slack 🟢 **성공** |
  | Slack 내용 | `APPROVAL_REQUIRED` · color `#ECB22E` · `marketStatusCode=BLOCK` · `marketStatusLabel=차단` |
  | 표시 항목 | 차단 이유 · Daily 매수 신호 0/0 · Daily 포지션 판단 없음 · 매수·매도 후보 없음 |

  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Daily Brief Slack Builder Lambda 추가**

  | 항목 | 값 |
  | --- | --- |
  | Builder Lambda | `portfolio-daily-brief-slack-summary-builder` (신규) |
  | Runtime | Python 3.12 + `pg8000` + Secrets Manager `valueFrom` (`psycopg2` 미사용) |
  | 이벤트 | `MORNING_BRIEF` · `EVENING_BRIEF` 2종 |
  | 조회 기준 | 최신 `connector_balance_snapshot` + 동일 `as_of_date` `connector_position_snapshot` + `quantity > 0` |
  | 정렬 | 평가금액 내림차순 · ticker code 오름차순 · null/blank `-` 처리 |
  | Smoke | `MORNING_BRIEF` + `EVENING_BRIEF` invoke 🟢 **성공** |
  | Balance snapshot | `id=281` · `as_of_date=2026-06-30` |
  | 금액 | `total_eval_amount=8,706,505원` · `cash_balance=8,706,505원` |
  | 손익 | `cumulativeProfitRate=-12.94%` · `cumulativeProfitAmount=-1,293,495원` · `positionCount=0` |
  | Evening delta | `0원` |

  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Daily Brief mini Step Functions 추가**

  | 항목 | 값 |
  | --- | --- |
  | State machine | `portfolio-daily-brief-slack-notification` 🟢 **ACTIVE** |
  | 흐름 | `BuildDailyBriefPayload → SendSlackNotifier` |
  | IAM Role (신규) | `portfolio-daily-brief-sfn-role` · `portfolio-daily-brief-scheduler-role` |
  | 일자 | 2026-06-30 |

  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Daily Brief morning smoke 통과**

  | 항목 | 값 |
  | --- | --- |
  | executionName | `daily-brief-morning-smoke-safe-20260630-193255-68f50aeb` |
  | Status | 🟢 **SUCCEEDED** |
  | 일자 | 2026-06-30 |
  | Slack 장전 알림 | 수신 확인 |

  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Daily Brief evening smoke 통과**

  | 항목 | 값 |
  | --- | --- |
  | executionName | `daily-brief-evening-smoke-safe-20260630-193300-aa2b9a12` |
  | Status | 🟢 **SUCCEEDED** |
  | 일자 | 2026-06-30 |
  | Slack 장후 알림 | 수신 확인 |

  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **장전/장후 EventBridge Scheduler 2개 ENABLED 추가**

  | Scheduler | cron | Timezone | Flexible | Input eventType | State |
  | --- | --- | --- | --- | --- | --- |
  | `portfolio-daily-brief-morning-slack-0750-kst` | `cron(50 7 ? * MON-FRI *)` | Asia/Seoul | OFF | `MORNING_BRIEF` | 🟢 **ENABLED** |
  | `portfolio-daily-brief-evening-slack-1550-kst` | `cron(50 15 ? * MON-FRI *)` | Asia/Seoul | OFF | `EVENING_BRIEF` | 🟢 **ENABLED** |

  - Scheduler input 에 고정 `runDate` 미주입 · Builder 가 실행 시점 KST 기준 처리
  - MarketConnector EC2 start · stop 과 독립 운영
  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)
- 🟢 **장중 손절 `INTRADAY_STOP_LOSS` Slack 실제 이벤트 연동 완료 결과 추가 (오후 추가 작업분)**

  | 항목 | 값 |
  | --- | --- |
  | Notifier Lambda | `portfolio-event-notifier` |
  | Formatter | `INTRADAY_STOP_LOSS` 지원 확인 |
  | Slack 문구 | `🚨 [장중 손절]` 수신 확인 |
  | `evalProfitRate` 표시 | `%` suffix 추가 반영 |
  | 로컬 smoke | `🔵 -4.2%` 표시 확인 |
  | EC2 IAM invoke smoke | 수신 확인 |

  - 상세: [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md)
- 🟢 **MarketConnector evaluate 스크립트 교체 배포**

  | 항목 | 값 |
  | --- | --- |
  | File | `/home/ec2-user/apps/port-marketconnector/src/connector_intraday_position_evaluate.py` |
  | 버전 | `connector-intraday-position-evaluate-1.1.1-slack-notify` |
  | 신규 CLI option | `--notify-slack` · `--slack-function-name` · `--slack-region` |
  | SHA256 hash 검증 | 통과 |
  | 정적 검증 | `py_compile` + `--help` 통과 |
  | 일자 | 2026-06-30 |

  - 상세: [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md)

- 🟢 **장중 runner 갱신**

  | 항목 | 값 |
  | --- | --- |
  | File | `/home/ec2-user/apps/port-marketconnector/scripts/run_intraday_snapshot_and_evaluate.sh` |
  | 연결 옵션 | `--create-order --notify-slack` |
  | Backup | `.bak.20260630T112255Z.create-order-notify-slack` |
  | SHA256 hash 검증 | 통과 |
  | 문법 검증 | 통과 |
  | 일자 | 2026-06-30 |

  - 상세: [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md)

- 🟢 **장중 runner 1회 안전 검증**

  | 항목 | 값 |
  | --- | --- |
  | commandId | `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` |
  | 결과 | `EMPTY_NORMAL` |
  | Marker | `INTRADAY_SNAPSHOT_AND_EVALUATE=SUCCESS` |
  | Exit code | `0` |
  | `open_position_count` | `0` |
  | 종료 | OPEN position 0건 정상 종료 |
  | 일자 | 2026-06-30 |

  - 상세: [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md)

- 🟢 **MarketConnector EC2 IAM 권한 추가 결과**

  | 항목 | 값 |
  | --- | --- |
  | Role | `portfolio-paper-marketconnector-ec2-role` |
  | Inline policy | `portfolio-paper-marketconnector-event-notifier-invoke` |
  | Action | `lambda:InvokeFunction` |
  | Resource | `portfolio-event-notifier` 한정 |
  | Wildcard | Resource · Action 🟢 **0건** |
  | EC2 invoke smoke | 🟢 **성공** |

  - 상세: [06 operation-notes](specs/06-secrets-and-iam/operation-notes.md)

- 🟢 **장중 손절 DB after-check 통과 결과 추가**

  | 항목 | 값 |
  | --- | --- |
  | Marker | `STEP19C_INTRADAY_STOP_FINAL_DB_AFTER_CHECK=SUCCESS` |
  | `TODAY_INTRADAY_CHECKS` | 0건 |
  | `TODAY_INTRADAY_STOP_EXECUTION_ORDERS` | 0건 |
  | `ACTIVE_INTRADAY_STOP_EXECUTION_ORDERS` | 0건 |
  | `TODAY_INTRADAY_STOP_CONNECTOR_ORDERS` | 0건 |
  | `TODAY_INTRADAY_STOP_CONNECTOR_ORDER_ROWS` | 0 rows |
  | 최신 `connector_balance_snapshot` | `id=281` |
  | `as_of_date` | `2026-06-30` |
  | `as_of_ts` | `2026-06-30 11:23:34.973842+00` |
  | `total_eval_amount` | `8,706,505` |
  | `cash_balance` | `8,706,505` |
  | `source_version` | `connector-intraday-snapshot-refresh-1.0.0` |
  | PSQL exit code | `0` |

  - DB 검증 쿼리는 `information_schema.columns` 사전 확인 후 작성된 컬럼만 사용 (추정 컬럼명 사용 0건)
  - 상세: [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md)

### Changed

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | OD-MS-030 | 본문 변경 없이 evidence 보강 |
  | 기존 정책 | `portfolio-event-notifier` 3종 (`APPROVAL_REQUIRED` · `DAILY_EXECUTION_SUCCESS` · `DAILY_EXECUTION_FAILED`) |
  | 본 일자 확대 | Approval Required + Daily Brief alias 2종 + nested adapter + 손익 prefix 규칙 |
  | 기존 View SlackNotificationService | 유지 |
  | OD-MS-038 | 신규 (Daily Brief Slack mini workflow) |
  | Status | 🟢 **확정** |
  | 영향 spec | 04 · 05 · 10 |
  | Decision Summary | 전체 96 → 97 · 확정 51 → 52 · 잠정 42 유지 |
  | Change Log | 2026-06-30 (오후) Slack 항목 추가 |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | 항목 | 값 |
  | --- | --- |
  | R-AUTO-035 | 신규 (Daily Brief Slack 자동 발송 실패 또는 중복 발송 위험) |
  | Impact | Medium |
  | Probability | Medium |
  | Mitigation | mini Step Functions 분리 · Scheduler 2개 ENABLED · 수동 smoke 🟢 **SUCCEEDED** · Slack 수신 확인 · CloudWatch / SFN audit |
  | Detection | 07:50/15:50 Slack 수신 · SFN execution status · Lambda CloudWatch Logs |
  | Rollback | Scheduler DISABLED 또는 수동 SFN 실행 |
  | Affected Spec | 04, 05, 10 |
  | Status | 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 기존 "장 전 / 장 후 Slack 후속 분리" | 본 일자 완료로 이동 |
  | 신규 후속 | 7건 등록 |
  | 후속 1 | 다음 평일 07:50 자동 장전 Slack 수신 확인 |
  | 후속 2 | 다음 평일 15:50 자동 장후 Slack 수신 확인 |
  | 후속 3 | 다음 Step1~11 실행 시 풍부한 `APPROVAL_REQUIRED` Slack 자동 수신 확인 |
  | 후속 4 | 보유종목 존재 시 종목별 손익·수익률 표시 재확인 |
  | 후속 5 | `DAILY_EXECUTION_SUCCESS` 요약 강화 |
  | 후속 6 | `DAILY_EXECUTION_FAILED` cause truncation 정책 |
  | 후속 7 | `INTRADAY_STOP_LOSS` 실제 이벤트 연동 / 10 spec Daily Brief Slack live 검증 |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | 항목 | 보강 대상 |
  | --- | --- |
  | Lambda | `portfolio-event-notifier` · `portfolio-approval-slack-summary-builder` · `portfolio-daily-brief-slack-summary-builder` |
  | Step Functions | `portfolio-daily-brief-slack-notification` mini workflow |
  | EventBridge Scheduler | 07:50 / 15:50 Daily Brief Scheduler 2종 |
  | Secrets Manager | `DB_PASSWORD_SECRET_VALUE_FROM` valueFrom 기반 password 주입 방식 |
  | secret 원문 기록 | 0건 (R-DOCS-001 정합) |

- 🟢 `.kiro/specs/_common/cost-simulation.md`

  | 항목 | 값 |
  | --- | --- |
  | Daily Brief Slack 자동화 비용 | Lambda 2개 + SFN 소규모 transitions + Scheduler 2개 |
  | 월 비용 | 매우 작음 |
  | Fargate · ALB · NAT 비용 | 무관 |
  | 정확 금액 | AWS Pricing Calculator 재확인 단서 유지 |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | 대상 행 | port-view 4.2 |
  | 알림 보조 계층 확장 | Lambda notifier + Builder Lambda + mini Step Functions |
  | Daily Batch orchestration | Step Functions + EventBridge 결정 유지 |
  | Daily Brief 경로 | 별도 mini Step Functions 로 분리 (주문 실행 경로와 결합 없음) |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | Approval Builder 연동 | 완료 |
  | State machine | `portfolio-paper-daily-step1-17-approval` |
  | ASL 흐름 반영 | `BuildApprovalSlackPayload → SendApprovalRequiredSlack` |
  | revisionId | 사실 기록 |
  | Slack smoke | 🟢 **통과** |
  | 후속 | 다음 Step1~11 실제 실행 시 자동 Approval Required Slack 수신 확인 |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | Daily Brief Slack 자동화 | 오후 ECS Fargate 1차 포팅 + Step 12~17 승인과 별도 phase |
  | MarketConnector EC2 start · stop | Daily Brief Slack 과 독립 운영 |

- 🟢 `.kiro/specs/06-secrets-and-iam/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | IAM Role 2종 | `portfolio-daily-brief-sfn-role` · `portfolio-daily-brief-scheduler-role` |
  | Builder Lambda 주입 방식 | `DB_PASSWORD_SECRET_VALUE_FROM` + Secrets Manager `get_secret_value` |
  | secret 원문 기록 | 0건 |
  | 오후 추가 Role | `portfolio-paper-marketconnector-ec2-role` |
  | 추가 inline policy | `portfolio-paper-marketconnector-event-notifier-invoke` |
  | action | `lambda:InvokeFunction` |
  | Resource | `portfolio-event-notifier` 한정 |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (오후 추가)

  | 항목 | 값 |
  | --- | --- |
  | 장중 손절 READY 생성 책임 | MarketConnector EC2 runner 로 위임 |
  | Runner 경로 | `/home/ec2-user/apps/port-marketconnector/scripts/run_intraday_snapshot_and_evaluate.sh` |
  | 실제 broker 주문 제출 | `portfolio-paper-intraday-stop-sell-approval` SFN approval gate 책임 유지 |

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md` (오후 추가)

  | 항목 | 값 |
  | --- | --- |
  | evaluate 교체 배포 | `connector-intraday-position-evaluate-1.1.1-slack-notify` |
  | runner 갱신 | `--create-order --notify-slack` |
  | SHA256 hash 검증 | 통과 |
  | EC2 invoke smoke | 통과 |
  | Runner 1회 안전 검증 | commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` · `EMPTY_NORMAL` |

- 🟢 `.kiro/specs/_common/operator-decisions.md` (오후 추가)

  | 항목 | 값 |
  | --- | --- |
  | Change Log | `2026-06-30 (오후) 장중 손절 Slack` 항목 추가 |
  | 본문 변경 | 없음 (OD-MS-030 · OD-MS-035 · OD-MS-036 · OD-MS-038 evidence 보강만) |
  | 신규 결정 | 없음 |
  | Decision Summary | 전체 97 · 확정 52 · 잠정 42 유지 |

- 🟢 `.kiro/specs/_common/risk-register.md` (오후 추가)

  | 항목 | 값 |
  | --- | --- |
  | R-AUTO-035 | mitigation 에 [2026-06-30 오후 추가 보강] 메모 추가 |
  | 보강 내용 | MarketConnector EC2 runner 도 Notifier Lambda invoke 진입점 |
  | 실 자동 발사 검증 | 후속 |
  | Status | 🟢 **Mitigated** 유지 |
  | R-AUTO-036 | 신규 (장중 손절 READY 생성 후 Slack 실패 시 rollback 없는 정책 위험) |
  | Impact | Medium |
  | Probability | Low |
  | Mitigation | Lambda CloudWatch Logs · SFN execution audit · DB after-check |
  | Affected Spec | 03, 04, 06, 10 |
  | Status | 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/followups-overview.md` (오후 추가)

  | 항목 | 값 |
  | --- | --- |
  | `INTRADAY_STOP_LOSS` 실제 장중 포지션 이벤트 연동 | 완료로 이동 |
  | 신규 후속 | 4건 |
  | 후속 1 | 실 보유 종목 발생 후 hard stop 조건 충족 시 실이벤트 수신 확인 |
  | 후속 2 | READY 생성 + approval gate 차단 상태 재확인 |
  | 후속 3 | Slack 메시지에 계좌 · 현재가 · 진입가 · 예상손익금액 추가 여부 검토 |
  | 후속 4 | 장중 손절 READY 생성 후 별도 approval summary Slack 추가 여부 검토 |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md` (오후 추가)

  | 항목 | 보강 대상 |
  | --- | --- |
  | Lambda | MarketConnector EC2 → `portfolio-event-notifier` invoke 경로 · `INTRADAY_STOP_LOSS` formatter |
  | IAM Role | `portfolio-paper-marketconnector-ec2-role` inline policy |
  | Step Functions | `portfolio-paper-intraday-stop-sell-approval` approval gate 분리 |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md` (오후 추가)

  | 항목 | 값 |
  | --- | --- |
  | 대상 행 | port-marketconnector / `port_strategy_execution` |
  | MarketConnector EC2 runner 책임 | 장중 snapshot refresh · position evaluate · READY 생성 · Slack notify |
  | broker 주문 제출 | SFN approval gate 분리 구조 유지 |
  | Lambda 위치 | Slack notifier / summary builder 보조 계층 유지 |

- 🟢 `.kiro/specs/_common/cost-simulation.md` (오후 추가)

  | 항목 | 값 |
  | --- | --- |
  | Lambda invoke · SFN transitions · Slack notify | 모두 저빈도 |
  | EC2 MarketConnector | 기존 자원 재사용 |
  | 신규 상시 비용 | 의미 있는 증가 0건 |
  | Fargate · ALB · NAT 비용 | 무관 |
  | 정확 금액 | AWS Pricing Calculator 재확인 단서 유지 |

- 🟢 `.kiro/AGENTS.md`

  | 항목 | 값 |
  | --- | --- |
  | 신규 규칙 | 4종 |
  | 규칙 1 | psql `information_schema.columns` 사전 확인 의무 강화 (존재하지 않는 컬럼 추정 <span style="color:#D1242F">**금지**</span>) |
  | 규칙 2 | 실패 명령 / SQL 뒤 SUCCESS marker 출력 <span style="color:#D1242F">**금지**</span> |
  | 규칙 3 | PowerShell native command not found / psql 실패 시 `$LASTEXITCODE` / `$?` 차이 주의 |
  | 규칙 4 | Windows AWS CLI stdout emoji / 특수문자 cp949 encoding 오류 대응 (EC2 측 sanitize 또는 status-only 조회 우선) |
  | 규칙 5 | SSM multiline command 는 UTF-8 No BOM JSON + `--parameters file://...` 패턴 |
  | 규칙 6 | DB password / secret 값 채팅 / 문서 / 로그 / 명령 예시 포함 <span style="color:#D1242F">**금지**</span> |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 갱신 위치 | 2026-06-30 (오후) 섹션 끝부분 |
  | 내용 | 장중 손절 Slack 실이벤트 연동 최종 완료 요약 5~10줄 |
  | 누적 | 결정 / 리스크 / 후속 |

- 🟢 `.kiro/README.md`

  | 항목 | 값 |
  | --- | --- |
  | 갱신 위치 | "현재 진행 상태 요약" 섹션 |
  | 반영 사실 | Slack 개선 라인에 장중 손절 연동 완료 |
  | 검증 로그 | 미포함 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret 원문 (Slack webhook URL · DB password · IAM ARN · state machine ARN · KIS app key/secret · 계좌번호 · token) | 0건 |
| AWS · Lambda · SFN · Scheduler · IAM · RDS · Secrets Manager · Slack webhook · KIS API 실행 | 0건 |
| Lambda code · SFN ASL · Scheduler target JSON · Slack payload · Builder output · Notifier input · CloudWatch Logs · IAM Policy 본문 평문 인용 | 0건 |
| broker · KIS 신규 호출 | 0건 |
| smoke 외 Slack 실이벤트 발송 | 0건 |
| commit · add · reset · checkout · stash | 0건 |

> <span style="color:#D1242F">운영자 로컬 세션에서 DB password 가 우연히 노출된 이력은 password 값 없이 "credential rotation / history cleanup 권고" 수준으로만 남김 (R-SEC-010 / R-DOCS-001 / R-DOCS-002 · 06 spec 후속).</span>

Slack webhook URL 은 환경변수명(`SLACK_WEBHOOK_URL`) 까지만 기록. 운영 안정화 후 Secrets Manager 또는 SSM SecureString 이전 예정 (R-AUTO-024 · 06 spec 후속).

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- Slack 개선 — Builder Lambda `portfolio-approval-slack-summary-builder` · `portfolio-daily-brief-slack-summary-builder` · Notifier Lambda `portfolio-event-notifier`.
- mini SFN — `portfolio-daily-brief-slack-notification` (revisionId `da8642c6-8409-41b6-ad57-e066ff672332`).
- IAM Role — `portfolio-daily-brief-sfn-role` · `portfolio-daily-brief-scheduler-role`.
- Scheduler — `portfolio-daily-brief-morning-slack-0750-kst` · `portfolio-daily-brief-evening-slack-1550-kst` · cron `cron(50 7 ? * MON-FRI *)` / `cron(50 15 ? * MON-FRI *)` · Asia/Seoul.
- eventType 라벨 — `MORNING_BRIEF` · `EVENING_BRIEF` · `PRE_MARKET_STATUS` · `POST_MARKET_STATUS` · `APPROVAL_REQUIRED` · `DAILY_EXECUTION_SUCCESS` · `DAILY_EXECUTION_FAILED` · `INTRADAY_STOP_LOSS`.
- 장중 손절 — SSM commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` · IAM Role `portfolio-paper-marketconnector-ec2-role` · inline policy `portfolio-paper-marketconnector-event-notifier-invoke` · state machine `portfolio-paper-intraday-stop-sell-approval` · marker 3종 `STEP19C_INTRADAY_STOP_FINAL_DB_AFTER_CHECK=SUCCESS` · `INTRADAY_SNAPSHOT_AND_EVALUATE=SUCCESS` · `EMPTY_NORMAL`.
- balance snapshot — `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505` · `cumulativeProfitRate=-12.94%` · `cumulativeProfitAmount=-1,293,495` · `positionCount=0`.
- 상세 evidence — [03](specs/03-marketconnector-ec2/operation-notes.md) · [04](specs/04-strategy-batch-stepfunctions/operation-notes.md) · [05](specs/05-port-view-ecs-and-runbook/operation-notes.md) · [06](specs/06-secrets-and-iam/operation-notes.md).

</details>

## 2026-06-30 (Local View → AWS Step Functions Step 12~17 승인 실행 검증 통과 + Daily Batch gate 수정 + View 운영 경로 4종 정리)

### Added

- 🟢 **오전 AWS Step Functions Step 1~11 정기 실행 통과 결과 추가**

  | 항목 | 값 |
  | --- | --- |
  | Trigger | 기존 EventBridge Scheduler · Dispatcher Lambda 자동 trigger 경로 (OD-MS-032 정합) |
  | Step 12~17 정책 | 별도 승인형 state machine + paper-order gate 유지 |
  | 09:01 자동 ENABLE | 보류 (OD-MS-033) |

  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Local View → AWS Step Functions Step 12~17 승인 실행 통과 결과 추가**

  | 항목 | 값 |
  | --- | --- |
  | 사전 조건 | Daily Batch gate (`DailyBatchController.java`) 수정 후 운영자 의도 정합으로 활성 |
  | executionName | `port-view-daily-step12-17-20260630-095111-aae2595c` |
  | State machine | `portfolio-paper-daily-step12-17-approval` |
  | Trigger | Local View · AWS Step Functions approval range button |
  | Status | 🟢 **SUCCEEDED** |
  | Start | `2026-06-30T09:51:11.903+09:00` |
  | Stop | `2026-06-30T09:54:16.484+09:00` |
  | 종료 판정 | 주문 대상 없음 상태에서 안전 종료 |

  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **DB 후검증 통과 결과 추가**

  | 항목 | 값 |
  | --- | --- |
  | 신규 `connector_order_request` | 0건 |
  | REQUESTED `strategy_execution_order` 잔여 | 0건 |
  | active `connector_order_request` | 0건 |
  | 최신 `connector_balance_snapshot` | `id=281` |
  | `as_of_date` | `2026-06-30` |
  | `total_eval_amount` | `8,706,505` |
  | `cash_balance` | `8,706,505` |
  | `eval_profit` | `0` |
  | `source_version` | `connector-intraday-snapshot-refresh-1.0.0` |
  | 보유 종목 | 0건 |

  - `connector_position_snapshot` 은 `balance_snapshot_id` 컬럼 부재로 `account_no` + `as_of_date` 기준으로 검증
  - 상세: [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md)

- 🟢 **View 운영 경로 4종 정리 결과 추가**

  | 경로 | 일자 · Run |
  | --- | --- |
  | (a) Local View → Local File Step 1 단독 | 2026-06-28 Run #46 |
  | (b) Local View → Local File Step 1~11 | 2026-06-28 Run #47 |
  | (c) Local View → Local File Step 12~17 | 2026-06-29 Run #48 |
  | (d) **Local View → AWS Step Functions Step 12~17 승인 실행** | **본 일자** |

  - Local File 실행과 AWS Step Functions 실행이 분리 동작 정합
  - Step 12~17 주문성 구간은 별도 승인형 state machine + paper-order gate 를 통해 실행 정합
  - 상세: [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md)

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | 항목 | 값 |
  | --- | --- |
  | R-AUTO-033 | [2026-06-30 보강] mitigation 메모 추가 |
  | 근거 | Daily Batch gate 운영 의도 정합 + AWS Step Functions 승인 실행 1차 실증 |
  | Gate 조합 | `fullPipelineExecutionEnabled=true` + `paperOrderEnabled=true` |
  | 정합 사실 | AWS Step 1~11 + AWS Step 12~17 승인 버튼 동시 활성 조건과 Local File gate 분리 |
  | Status | 🟢 **Mitigated** |
  | R-AUTO-034 | [2026-06-30 보강] mitigation 메모 추가 |
  | 근거 | View 측 4가지 운영 경로 분리 1차 실증 + DB 검증 쿼리 작성 원칙 보강 |
  | 잔여 | Fargate Task Role 권한 분리는 06 spec 후속 phase 책임 유지 |
  | Status | 🟠 **Open** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-30 후속 메모 | 추가 |
  | 완료 | 5건 |
  | 후속 | 7건 |
  | 신규 기록 | DB 검증 쿼리 작성 원칙 |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | Append 범위 | 2026-06-30 §1~§6 |
  | 오전 실행 | Step 1~11 자동 trigger 통과 |
  | 오전 실행 (View) | Local View → approval workflow 운영자 수동 trigger 2차 실증 |
  | Gate 정합 | Daily Batch gate 운영 의도 정합 확인 |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | Append 8) | View 운영 경로 4종 검증 완료 |
  | Append 9) | Daily Batch gate 운영 의도 정합 수정 사실 |
  | Append 10) | DB 검증 쿼리 작성 원칙 추가 |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 갱신 위치 | 2026-06-30 섹션 prepend |
  | 분량 | 5~10줄 요약 |
  | 포함 사실 | Daily Batch gate 수정 · executionName · DB 후검증 · 4종 운영 경로 정리 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret 원문 (KIS app key/secret · 계좌번호 · token · RDS password · IAM/state machine ARN · Slack webhook URL 등) | 0건 |
| AWS CLI · boto3 · psql · Spring Boot · 외부 API 실행 | 0건 |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| CloudWatch · SFN execution history · Lambda / KIS response body · Spring log · Slack payload · DB 후검증 raw 인용 | 0건 |
| broker · KIS 신규 BUY / SELL / 취소 / 정정 | 0건 |
| fill · position sync 자동 재시도 | 0건 |
| aws-live 작업 | 0건 |
| commit · add · reset · checkout · stash | 0건 |

> broker · KIS 호출 = 오전 Step 1~11 자동 trigger 한정 (`connector_order_request` 신규 0건) + Local View → AWS Step Functions Step 12~17 승인 실행 1건 (`SUCCEEDED` / NO_TARGET / broker 주문 제출 0건) + balance refresh 한정.

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- Step Functions — executionName `port-view-daily-step12-17-20260630-095111-aae2595c` · state machine `portfolio-paper-daily-step12-17-approval`.
- Spring — Controller `DailyBatchController` · gate 라벨 6종 (`executionEnabled` · `localFileExecutionEnabled` · `fullPipelineExecutionEnabled` · `paperOrderEnabled` / 허용 범위 `1~17` / 화면 라벨 `Execution ON` · `Local File OFF` · `Full Pipeline ON` · `Paper Order ON`).
- Balance snapshot — `id=281`.
- DB 컬럼명 — `balance_snapshot_id`(부재) · `account_no` · `as_of_date`.
- Pipeline run — `#46` · `#47` · `#48` · 운영 경로 4종 라벨.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-29 (3) (port-view Step 12~17 승인형 검증 완료 + Local View wrapper 정리 완료 + Approval state machine ARN 분리)

### Added

- **Local View → AWS Step Functions Step 12~17 승인형 실행 검증 통과** — Daily Pipeline 측이 아니라 Local View 가 직접 Step Functions `StartExecution` external caller 로 붙는 두 번째 phase.
   - executionName `port-view-step12-17-step12-17-20260629-194314-ba5edaf8`.
   - state machine `portfolio-paper-daily-step12-17-approval` · status `SUCCEEDED`.
   - start `2026-06-29T19:43:15.673+09:00` · stop `2026-06-29T19:46:06.546+09:00`.
   - 진행: `Step12_CheckApproval` 통과 → `Step12_RunMarketConnectorStrategyOrderExecute` → `Step12_GetCommandInvocation` → Step 13~17 전체 진행 → `ExecutionSucceeded`.
   - DB 안전 후검증 통과 — marker `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS` · 오늘 신규 `connector_order_request` 0건 · READY · REQUESTED `strategy_execution_order` 0건 · 신규 broker 주문 0건.
   - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md).
- **AWS Step 12~17 승인 실행 endpoint / 버튼 추가**
   - `POST /daily-batch/aws-stepfunctions/start-approval-range` Controller endpoint 추가.
   - `daily_batch.html` 에 승인 실행 버튼 분리 (safe / approval 활성화 조건 분리).
   - payload — `requestedBy=VIEW_APPROVAL_BUTTON` · `allowPaperOrderExecute=true` · `paperOrderEnabled=true` (boolean).
   - `executionName` + redaction 된 `executionArn` 화면 표시.
- **Step 12~17 전용 state machine ARN 분리**
   - 일반 workflow ARN `portfolio-paper-daily-step1-17-approval` 과 approval workflow ARN `portfolio-paper-daily-step12-17-approval` 분리.
   - `application.properties` 키 `portfolio.batch.aws-stepfunctions-approval-state-machine-arn` 추가.
   - 환경변수 `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_APPROVAL_STATE_MACHINE_ARN` 추가.
   - `DailyBatchProperties` 에 `awsStepfunctionsApprovalStateMachineArn` 필드 + getter / setter 추가.
   - `StepFunctionsDailyBatchExecutionService` — `startSafeRange` 는 일반 ARN · `startApprovalRange` 는 approval 전용 ARN 사용.
   - approval ARN 비어 있으면 승인형 실행 차단.
- **로컬 View 구동 wrapper 2종 정리**
   - `Start-PortfolioViewAwsPaperLocalFile.ps1` — env `PORTFOLIO_BATCH_EXECUTION_MODE=local-file` + 모든 gate ON · aws-stepfunctions gate OFF.
   - `Start-PortfolioViewAwsPaperStepFunctions.ps1` — env `PORTFOLIO_BATCH_EXECUTION_MODE=aws-stepfunctions` + 모든 gate ON · 일반 + approval ARN set.
   - 두 wrapper 모두 `aws-paper` profile + Step 1~17 전체 실행 가능.
   - env loader 2종(`Load-PortfolioViewAwsPaperLocalFileEnv.ps1` · `Load-PortfolioViewAwsPaperStepFunctionsEnv.ps1`) 동시 정리.
   - wrapper 본체는 운영자 로컬 도구 폴더(`C:\Workspaces\portfolio-local-env\`) 로 본 spec 범위 밖.
- **05 spec `operation-notes.md` 4) Step 12~17 승인형 검증 완료 + 5) 로컬 View 구동 wrapper 정리 완료 섹션 추가** — 기존 미완료 항목 → 완료로 갱신 + Docker · ECR · Task Definition / Fargate 검증 항목을 6) · 7) 로 renumber.

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | 항목 | 값 |
  | --- | --- |
  | R-AUTO-033 | [2026-06-29 보강 (3)] mitigation 메모 추가 |
  | 근거 | Step 12~17 approval state machine 분리 + boolean / numeric payload 1차 실증 통과 |
  | Status | 🟢 **Mitigated** |
  | R-AUTO-034 | [2026-06-29 보강] mitigation 메모 추가 |
  | 근거 | approval ARN 분리 + 서비스 레벨 안전 gate 1차 실증 |
  | 잔여 | Fargate Task Role 권한 분리는 06 spec 후속 phase 책임 |
  | Status | 🟠 **Open** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-29 (3) 후속 메모 | 추가 |
  | 완료 | 5건 |
  | 완료 1 | Step 12~17 승인형 검증 통과 |
  | 완료 2 | approval ARN 분리 |
  | 완료 3 | payload boolean / numeric 보완 |
  | 완료 4 | 로컬 View wrapper 2종 정리 |
  | 완료 5 | 05 spec operation-notes 갱신 |
  | Resolved | 3건 (string boolean payload 차단 · wrong ARN 호출 · safe-only gate 잘못 남음) |
  | 신규 후속 | 4건 (2026-04-27 삼성전자 stale ACCEPTED cleanup · Fargate 초기 `paperOrderEnabled` 기본값 · ALB source IP · 최소 인증 · CloudWatch Logs · alarms · failure Slack) |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | 본문 변경 | 없음 |
  | Evidence 보강 | `OD-MS-002` · `OD-MS-009` · `OD-MS-037` · `OD-SAFE-001` ~ `OD-SAFE-004` |
  | Change Log | 2026-06-29 (3) 항목 추가 |
  | Decision Summary | 전체 96 · 확정 51 · 잠정 42 유지 |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | 대상 행 | port-view 4.2 |
  | Step 12~17 approval | state machine ARN 분리 |
  | payload | boolean / numeric 정합 |
  | 로컬 wrapper | 2종 정리 |
  | Fargate Task Role `states:StartExecution` | 일반 + approval 2종 모두 Resource 한정 부여 정책 |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | 항목 | 값 |
  | --- | --- |
  | Step Functions 진입 지원 | Step 1~11 safe + Step 12~17 approval 2종 |
  | Choice 조건 | `Step12_CheckApproval` `BooleanEquals` |
  | boolean payload | `allowPaperOrderExecute` · `paperOrderEnabled` |
  | numeric payload | `fromStepOrder` · `toStepOrder` · `startStep` · `endStep` |
  | Fargate Task Role 권한 분리 | 일반 + approval ARN 2종 모두 Resource 한정 |

- 🟢 `.kiro/specs/05-port-view-ecs-and-runbook/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | "7. ECS Fargate 포팅 구현" 4) | Step 12~17 승인형 검증 완료 |
  | 5) | 로컬 View 구동 wrapper 정리 완료 |
  | 6) · 7) | Docker · ECR · Task Definition · Fargate 검증 renumber |
  | 결론 섹션 | 추가 |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | Append 범위 | 2026-06-29 (3) §1~§5 |
  | 실증 사실 | View 가 `portfolio-paper-daily-step12-17-approval` external caller 로 붙은 두 번째 phase |
  | payload 정합 | `Step12_CheckApproval` `BooleanEquals` Choice 조건과 boolean 타입 |
  | 정책 변경 | 없음 |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 갱신 위치 | 2026-06-29 (3) 섹션 prepend |
  | 분량 | 5~10줄 요약 |
  | 포함 사실 | Step 12~17 승인형 + ARN 분리 + payload boolean 보완 + wrapper 정리 + resolved 3건 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret 원문 (KIS app key/secret · 계좌번호 · token · RDS password · IAM/state machine ARN · Slack webhook URL 등) | 0건 |
| AWS · Lambda · SFN · EventBridge · SSM · EC2 · RDS · S3 실행 | 0건 |
| AWS CLI · boto3 · psql · Spring Boot · 외부 API 실행 | 0건 |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| CloudWatch · SFN execution history · Lambda / KIS response body · Spring log · Slack payload · commit diff · wrapper 본체 평문 인용 | 0건 |
| broker · KIS 신규 BUY / SELL / 취소 / 정정 | 0건 |
| fill · position sync 자동 재시도 | 0건 |
| aws-live 작업 | 0건 |
| commit · add · reset · checkout · stash | 0건 |

> broker · KIS 호출 = Step 12 `--execute` 1건 (<span style="color:#0969DA">**NO_TARGET**</span>) + Step 13 active connector order 조회 1건 (대상 0건) + Step 17 balance refresh 한정. `connector_order_request` 신규 0건 / broker 주문 제출 0건.

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- port-view executionName `port-view-step12-17-step12-17-20260629-194314-ba5edaf8`.
- 운영 marker `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS`.
- state machine 2종 — `portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval`.
- state 이름 4종 — `Step12_CheckApproval` · `Step12_RunMarketConnectorStrategyOrderExecute` · `Step12_GetCommandInvocation` · `ExecutionSucceeded`.
- Controller endpoint 2종 — `/daily-batch/aws-stepfunctions/start-range` · `/daily-batch/aws-stepfunctions/start-approval-range`.
- Spring properties key 7종 — 기존 6종 + `portfolio.batch.aws-stepfunctions-approval-state-machine-arn`.
- PowerShell wrapper 4종 — `Start-PortfolioViewAwsPaperLocalFile.ps1` · `Load-PortfolioViewAwsPaperLocalFileEnv.ps1` · `Start-PortfolioViewAwsPaperStepFunctions.ps1` · `Load-PortfolioViewAwsPaperStepFunctionsEnv.ps1`.
- DB URL `jdbc:postgresql://127.0.0.1:15433/portfolio` · View DB user `view_app` · Tomcat port `8080` · Spring profile `aws-paper`.
- start · stop timestamp `2026-06-29T19:43:15.673+09:00` ~ `2026-06-29T19:46:06.546+09:00`.
- placeholder — `[REDACTED]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_ARN]` · `[REDACTED_SECRET_ARN]` · `[REDACTED_BROKER_ORDER_NO]`.

</details>

## 2026-06-29 (2) (port-view aws-stepfunctions Daily Batch trigger 구현 + 로컬 Step 1~11 StartExecution 검증 통과)

### Added

- **port-view aws-stepfunctions Daily Batch trigger 1차 구현 결과 반영** — Kiro 문서 업데이트이며 실제 코드 변경은 port-view commit `e72de6f` (`feat(view): add Step Functions daily batch trigger`) 참조.
   - 변경 파일 — `pom.xml` · `DailyBatchProperties.java` · `DailyBatchController.java` · `StepFunctionsDailyBatchExecutionService.java` · `application-aws-paper.properties` · `daily_batch.html`.
   - AWS SDK v2 Step Functions client 추가.
   - Daily Batch 실행 backend 를 `local-file` / `aws-stepfunctions` 로 분리.
   - Python subprocess · 로컬 source 직접 실행 없이 Step Functions `StartExecution` 만 수행.
   - `stateMachineArn` · region · `executionNamePrefix` 는 env 주입.
   - 화면에 AWS Step 1~11 safe trigger 버튼 + `POST /daily-batch/aws-stepfunctions/start-range` endpoint 추가.
   - 성공 시 `executionName` + account-id redaction 처리된 `executionArn` flash message 표시.
- **로컬 Step 1~11 `StartExecution` 검증 통과 결과 반영** — 로컬 View 를 `aws-paper` profile + `aws-stepfunctions` backend 로 실행 → 화면 AWS Step 1~11 safe trigger 클릭 → `StartExecution` 성공 → Step 1~11 workflow 실행 → Step 12~17 주문성 구간 `allowPaperOrderExecute=false` 기준 차단 유지 → `APPROVAL_REQUIRED` Slack 수신 end-to-end 통과 / broker 주문 제출 0건 / 신규 `connector_order_request` 0건.
- **runDate 누락 이슈 및 보완 결과 반영** — 최초 검증에서 Step 1~11 후 `StopCrawlerEc2AfterStep11Success` 상태에서 `States.Runtime` 발생(원인 = ASL Payload `runDate.$=$.runDate` 참조에 대해 View `StartExecution` input 에 `runDate` 누락) / `StepFunctionsDailyBatchExecutionService` 에서 Asia/Seoul 기준 `runDate`(yyyy-MM-dd) 를 input JSON 에 추가 / 재검증 시 `StopCrawlerEc2AfterStep11Success` 이후 `SendApprovalRequiredSlack` 까지 통과.
- **신규 R-AUTO-034 row 추가** — Fargate View 의 `states:StartExecution` 권한 과다 부여 + Step 12 이상 주문성 구간 gate 우회 위험 · Status `Open` · Task Role `states:StartExecution` 특정 state machine ARN 한정 + `paperOrderEnabled=false` 기본값 + `maxExecutableStepOrder=11` 초기값 + Step 12~17 preflight + approval gate + paper-order gate 뒤에서만 활성 + `executionArn` · account-id redaction 유지 정책(05 · 06 · 10 spec 후속 phase 책임).
- **05 spec `port-view-ecs-and-runbook` 폴더 신규 생성** — `.kiro/specs/05-port-view-ecs-and-runbook/operation-notes.md` 신규 생성(6. ECS Fargate 포팅 계획: 완료 + 7. ECS Fargate 포팅 구현(Step Functions backend · aws-stepfunctions mode · 로컬 Step 1~11 검증 완료 + Step 12~17 승인형 · Docker · ECR · ECS Task Definition · Fargate 검증 미완료) 사실 기록).

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | 항목 | 값 |
  | --- | --- |
  | R-AUTO-033 | [2026-06-29 보강] mitigation 메모 추가 |
  | 근거 | port-view 가 Step Functions `StartExecution` client 로 붙는 첫 local 검증 통과 |
  | 정책 | Step 1~11 safe trigger 분리 · Step 12~17 차단 정책 유지 |
  | Status | 🟢 **Mitigated** |
  | R-AUTO-034 | 신규 row 추가 |
  | 위험 | Fargate View `states:StartExecution` 권한 과다 + Step 12 gate 우회 |
  | Status | 🟠 **Open** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-29 두 번째 후속 메모 | 추가 |
  | 완료 | port-view local aws-stepfunctions Step 1~11 검증 |
  | 신규 후속 | 7건 |
  | 후속 항목 | Dockerfile · ECR push · ECS Task Definition · ECS Service smoke · Fargate Step 1~11 StartExecution · Step 12~17 승인형 · Task Role 최소 권한 |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | 본문 변경 | 없음 |
  | Evidence 보강 | `OD-MS-002` · `OD-MS-009` · `OD-MS-037` |
  | 1차 실증 | `StepFunctionsDailyBatchExecutionService` 신규 · aws-stepfunctions backend 1차 완료 · Fargate 진입 전 local Step Functions trigger 선검증 |
  | Change Log | 2026-06-29 (2) 항목 추가 |
  | Decision Summary | 변경 없음 |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | 대상 행 | port-view |
  | 운영 안정성 1순위 | ECS Fargate Service 유지 |
  | 실행 orchestration | View 내부 subprocess → Step Functions `StartExecution` 이관 |
  | 선검증 | 로컬 aws-stepfunctions mode 완료 |
  | 비권고 판단 | Elastic Beanstalk · App Runner · Lambda 유지 |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | 항목 | 값 |
  | --- | --- |
  | 보강 대상 | Step Functions · ECS Fargate · IAM Role · IAM Policy |
  | View Fargate 역할 | 상시 웹 콘솔 |
  | Daily Batch 실제 실행 | Step Functions `StartExecution` |
  | Task Role 권한 | `states:StartExecution` 특정 state machine ARN 한정 |
  | `aws-stepfunctions` mode | View 내 subprocess 실행 없음 |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | 사실 | port-view 가 SFN `StartExecution` external caller 로 붙은 첫 local 검증 |
  | View input 필수 | `runDate` (Asia/Seoul yyyy-MM-dd) |
  | ASL 참조 | `runDate.$=$.runDate` |
  | 흐름 검증 | Step 1~11 → `StopCrawlerEc2AfterStep11Success` → `SendApprovalRequiredSlack` |
  | Step 12~17 | approval gate / paper-order gate 정책 유지 |

- 🟢 `.kiro/specs/06-secrets-and-iam/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | Fargate port-view Task Role | `states:StartExecution` 최소 권한 후속 필요 (state machine ARN 한정 권장) |
  | RDS 접속정보 | image · properties 직접 기록 <span style="color:#D1242F">**금지**</span> |
  | 주입 방식 | Secrets Manager 또는 SSM SecureString |
  | env / config 주입 대상 | `stateMachineArn` · region · `executionNamePrefix` |
  | secret 값 | 문서 평문 기록 <span style="color:#D1242F">**금지**</span> |

- 🟢 10 spec `cutover-and-validation-runbook` 폴더

  | 항목 | 값 |
  | --- | --- |
  | 본 일자 시점 | 미존재 |
  | 위임 | 후속 phase 책임 분리 |
  | 기록 위치 | followups-overview 2026-06-29 (2) 후속 메모 |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 갱신 위치 | 2026-06-29 (2) 섹션 prepend |
  | 분량 | 5~10줄 요약 |
  | 포함 사실 | 구현 1차 완료 · Step 1~11 검증 · runDate 보완 · 신규 후속 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret 원문 (KIS app key/secret · 계좌번호 · token · RDS password · IAM/state machine ARN · Slack webhook URL · DB password 등) | 0건 |
| AWS · Lambda · SFN · EventBridge · SSM · EC2 · RDS · S3 실행 | 0건 |
| AWS CLI · boto3 · psql · Spring Boot · 외부 API 실행 | 0건 |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| Lambda code · IAM Policy · SFN ASL · SFN execution history · CloudWatch · KIS response · Spring log · Slack payload 평문 인용 | 0건 |
| broker · KIS 신규 호출 | 0건 |
| aws-live 작업 | 0건 |
| commit · add · reset · checkout · stash | 0건 |

> 검증 시점은 port-view commit `e72de6f` 적용 후 운영자 직접 로컬 실행 한정. 신규 broker 주문 제출 0건 · Step 12~17 approval gate 차단 유지.

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- port-view commit hash `e72de6f` · commit message `feat(view): add Step Functions daily batch trigger`.
- Controller endpoint path `/daily-batch/aws-stepfunctions/start-range`.
- Spring Boot properties key 6종 — `portfolio.batch.execution-mode` · `portfolio.batch.aws-stepfunctions-region` · `portfolio.batch.aws-stepfunctions-state-machine-arn` · `portfolio.batch.aws-stepfunctions-execution-name-prefix` · `portfolio.batch.aws-stepfunctions-start-enabled` · `portfolio.batch.aws-stepfunctions-step-start-enabled`.
- StartExecution payload — 11종 + `runDate` (Asia/Seoul yyyy-MM-dd).
- state name — `StopCrawlerEc2AfterStep11Success` · `SendApprovalRequiredSlack` · `Step6ToStep11_Succeeded` · `Step12_CheckApproval`.
- Slack 이벤트 라벨 `APPROVAL_REQUIRED` · 에러 라벨 `States.Runtime` · Class `StepFunctionsDailyBatchExecutionService`.
- placeholder — `[REDACTED]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_ARN]` · `[REDACTED_SECRET_ARN]`.

</details>

## 2026-06-29 (View Local Batch Step 12~17 실행 통과 + View AWS Paper Batch 공용 launcher 정리 / DB password rotate 후속 등록)

### Added

- 🟢 **View Local Batch Step 12~17 전 구간 실행 통과 결과 추가**

  | 항목 | 값 |
  | --- | --- |
  | Daily Pipeline run | `#48` |
  | 결과 | 🟢 **SUCCESS** |
  | 실행 유형 | `MANUAL_PARTIAL` |
  | 요청자 | `VIEW_BUTTON` |
  | 실행 범위 | Step 12~17 |
  | total · success · no_target · failed · skipped | `6 · 5 · 1 · 0 · 0` |
  | duration | `22,336ms` |
  | 진입 전 DB preflight | REQUESTED `strategy_execution_order` 0건 · retryable rejected 0건 · active `connector_order_request` 0건 |
  | `connector_strategy_order_execute.py` SHA256 hash | EC2 정식 배포본과 동일 (2026-06-25 patch · OD-MS-036 정합) |
  | View gate 6종 | 정합 |

  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` <span style="color:#0969DA">NO_TARGET</span> 안전 종료**

  | 항목 | 값 |
  | --- | --- |
  | subprocess | `marketconnector_app` |
  | command | `python connector_strategy_order_execute.py --execute` |
  | 신규 `connector_order_request` | 0건 |
  | broker 주문 제출 | 0건 |
  | mitigation 회귀 | OD-MS-016 · OD-MS-028 · OD-MS-033 · OD-MS-036 · OD-SAFE-001~004 · R-AUTO-001 · R-AUTO-019 · R-AUTO-031 · R-AUTO-032 · R-BROKER-004 · R-AUTO-033 모두 0건 |

  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Step 13~16 후속 동기화 정상 통과**

  | Step | 결과 | 처리 건수 |
  | --- | --- | --- |
  | `CONNECTOR_ORDER_CHECK` | 🟢 **SUCCESS** | active connector order 0건 |
  | `SYNC_SELL_FILL` | 🟢 **SUCCESS** | `submitted_position_sell_orders=0` |
  | `SYNC_BUY_FILL` | 🟢 **SUCCESS** | `submitted_buy_orders=0` |
  | `SYNC_BUY_POSITION` | 🟢 **SUCCESS** | `filled_buy_orders_without_position=0` |

  - mitigation 회귀 — OD-MS-016 · OD-MS-021 · OD-MS-024 · OD-MS-025 · R-AUTO-018 · R-DATA-012 모두 0건
  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **Step 17 `BALANCE_REFRESH` KIS token 재발급 + 잔고 저장 통과**

  | 항목 | 값 |
  | --- | --- |
  | token 만료 감지 후 | 재발급 |
  | 잔고 조회 Status | `200` |
  | `connector_balance_snapshot id` | `239` |
  | `as_of_date` | `2026-06-29` |
  | `cash_balance` | `8,706,505` |
  | `total_eval_amount` | `8,706,505` |
  | `eval_profit` | `0` |
  | `source_version` | `connector-balance-1.0.0` |
  | `connector_position_snapshot` | 비움 |
  | 보유 종목 | 0건 |
  | `NO_ORDER_SUBMITTED` 후검증 | 통과 |
  | mitigation 회귀 | R-DATA-005 · R-DATA-011 0건 |

  - 상세: [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)

- 🟢 **View AWS Paper Batch 공용 launcher 2개 신규** — 운영자 로컬 PowerShell 도구

  | Launcher | 역할 |
  | --- | --- |
  | `C:/Workspaces/portfolio-local-env/Load-PortfolioViewAwsPaperBatchEnv.ps1` | env loader (`view_app` DB user · tunnel `127.0.0.1:15433` · Batch/local-file ON · full pipeline ON · paper order ON · `1~17` 범위) |
  | `C:/Workspaces/portfolio-local-env/Start-PortfolioViewAwsPaperBatch.ps1` | starter (env loader 호출 → `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=aws-paper`) |

  - Starter 검증 통과 — Tomcat 8080 기동 · DB `127.0.0.1:15433/portfolio` 연결 · Default schema `ops` · 화면 허용 범위 `1~17` · Execution · Local File · Full Pipeline · Paper Order 모두 ON · 실행 버튼 활성

- 🟢 **R-SEC-010 신규 row 추가**

  | 항목 | 값 |
  | --- | --- |
  | 위험 | DB password 평문 노출 이력 → 영향받은 app role password rotate 후속 필요 |
  | Status | 🟠 **Open** |
  | 승격 조건 | rotate 완료 + secret loader / env 재검증 통과 시 🟢 **Mitigated** 승격 후보 |
  | 후속 책임 | 06 spec |
  | 결합 | R-DOCS-001 · R-DOCS-002 · R-SEC-001~003 · OD-SEC-002 |

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | 항목 | 값 |
  | --- | --- |
  | R-AUTO-033 | [2026-06-29 보강] mitigation 메모 추가 |
  | 근거 | Step 12~17 전 구간 · paper 주문성 구간 🔵 **NO_TARGET** 안전 종료 |
  | 신규 `connector_order_request` | 0건 |
  | broker 주문 제출 | 0건 |
  | Step 17 balance refresh | 통과 |
  | 정책 명문화 | 공용 launcher 가 Step 10/11/12 paper step 허용 → starter 실행 후 preflight 필수 |
  | Status | 🟢 **Mitigated** |
  | R-DOCS-002 | [2026-06-29 보강] mitigation 메모 추가 |
  | 근거 | DB password 대화 중 노출 이력 식별 · 신규 R-SEC-010 으로 후속 분리 |
  | Status | 🟢 **Mitigated** 유지 |
  | R-SEC-010 | 신규 row |
  | mitigation | (a) app role rotate (`view_app` · `marketconnector_app` · `crawler_app` · `preprocessor_app` · `research_app` · `decision_app` · `execution_app` 중 노출 role) (b) Secrets Manager / SSM SecureString 갱신 (c) local PowerShell secret loader · env 재검증 (d) rotate 완료 전 starter 실행 신중 점검 |
  | detection | 노출 이력 audit + rotate 완료 시점 운영자 노트 |
  | rollback | rotate 완료 시점에 Status → 🟢 **Mitigated** 승격 |
  | Status | 🟠 **Open** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-29 후속 메모 | 추가 |
  | 완료 | 5건 (Step 12~17 안전 점검 · Step 12~17 실행 · DB 후검증 · 공용 launcher 정리 · 2차 검증 결론) |
  | 후속 | 5건 (preflight 4종 명문화 · DB password rotate · secret loader 재검증 · ECS 진입 설계 · 05 spec 하위 문서 갱신) |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 갱신 위치 | 2026-06-29 섹션 prepend |
  | 분량 | 5~10줄 요약 |
  | 포함 사실 | Run #48 · 공용 launcher 2개 · 결론 · 주의 후속 3종 · 안전/보안 정합 |

- 🟢 05-port-view-ecs-and-runbook · 04-strategy-batch-stepfunctions 하위 operation-notes · validation-checklist · safety-gate-note 정식 갱신은 본 일자 작업 범위 밖. Step 12~17 NO_TARGET 안전 종료 사실 · View gate 6종 · 공용 launcher 정리 사실 정식 반영은 04 · 05 spec 후속 phase 책임으로 분리.

### Security

| 항목 | 결과 |
| --- | --- |
| secret 원문 (KIS app key/secret · 계좌번호 · token · RDS password · IAM/state machine ARN · Slack webhook URL · DB password · Administrator password 등) | 0건 |
| AWS · EventBridge · Lambda · SFN · SSM · EC2 · RDS · S3 실행 | 0건 |
| AWS CLI · boto3 실행 | 0건 |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
| CloudWatch · SFN · Lambda · KIS · Spring log · Pipeline stdout · patch 본문 · launcher 본문 평문 인용 | 0건 |
| broker · KIS 신규 BUY / SELL / 취소 / 정정 | 0건 |
| fill · position sync 자동 재시도 | 0건 |
| aws-live 작업 | 0건 |

> broker · KIS 호출 = Step 17 balance refresh 1건 (KIS token 재발급 + 잔고 조회 Status `200`) + Step 12 `--execute` 1건 (<span style="color:#0969DA">**NO_TARGET**</span> · broker 호출 0건) + Step 13 active connector order 조회 1건 (대상 0건). `connector_order_request` 신규 0건 · `NO_ORDER_SUBMITTED` 후검증 통과. mitigation 회귀 (R-AUTO-001 · R-AUTO-002 · R-AUTO-019 · R-AUTO-031 · R-AUTO-032 · R-BROKER-004 · R-AUTO-033) 모두 0건.

<details>
<summary>🟠 DB password 평문 노출 이력 등록</summary>

- 본 일자 운영자 대화 중 DB password 가 평문으로 노출된 이력이 식별.
- 신규 R-SEC-010 / Status 🟠 **Open** / R-DOCS-002 [2026-06-29 보강].
- rotate 대상 app role — `view_app` · `marketconnector_app` · `crawler_app` · `preprocessor_app` · `research_app` · `decision_app` · `execution_app` 중 노출된 role.
- 승격 조건 — rotate 완료 + Secrets Manager / SSM SecureString 갱신 + 운영자 로컬 PowerShell secret loader · env 재검증 통과 → 🟢 **Mitigated**.
- 후속 책임 — 06 spec phase.

</details>

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- Pipeline run id `#48` · 실행 유형 `MANUAL_PARTIAL` · 요청자 `VIEW_BUTTON` · 실행 범위 `Step 12~17` · step count `6/5/1/0/0` · duration `22,336ms`.
- Step 코드 6종 — `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` · `CONNECTOR_ORDER_CHECK` · `SYNC_SELL_FILL` · `SYNC_BUY_FILL` · `SYNC_BUY_POSITION` · `BALANCE_REFRESH`.
- 결과 라벨 — `SUCCESS` · `NO_TARGET`.
- DB role 2종 — `view_app` (JVM) · `marketconnector_app` (subprocess).
- balance snapshot — `id=239` · `as_of_date=2026-06-29` · `cash_balance=8,706,505` · `total_eval_amount=8,706,505` · `eval_profit=0` · `source_version=connector-balance-1.0.0`.
- launcher 파일 경로 — `C:/Workspaces/portfolio-local-env/Load-PortfolioViewAwsPaperBatchEnv.ps1` · `C:/Workspaces/portfolio-local-env/Start-PortfolioViewAwsPaperBatch.ps1`.
- Spring profile `aws-paper` · Tomcat `8080` · DB `127.0.0.1:15433/portfolio` · default schema `ops` · 화면 허용 범위 `1~17`.
- 모두 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

</details>

## 2026-06-28 (View Local Batch Step 1 + Step 1~11 실행 검증 통과 + 6/26 KRX 기준일 lag 결정적 증거 확보)

### Added

- 🟢 **View Local Batch Step 1 단독 실행 통과**

  | 항목 | 값 |
  | --- | --- |
  | Daily Pipeline run | `#46` |
  | 결과 | 🟢 **SUCCESS** |
  | 실행 유형 | `MANUAL_PARTIAL` |
  | 요청자 | `VIEW_BUTTON` |
  | 실행 범위 | Step 1~1 |
  | Step 1 `CONNECTOR_BALANCE` | 🟢 **SUCCESS** |
  | workDir | `C:/Workspaces/port-marketconnector` |
  | command | `python connector_balance.py` |
  | exit code | `0` |
  | subprocess | `dbUser=marketconnector_app` |
  | legacy `balance_summary` write | 비활성화 |
  | `connector_balance_snapshot` 저장 | 정상 |
  | `connector_position_snapshot` 비움 | 정상 |
  | 보유 종목 | 0건 |
  | mitigation 회귀 | R-DATA-005 · R-DATA-011 0건 |

- 🟢 **View Local Batch Step 1~11 전 구간 실행 통과**

  | 항목 | 값 |
  | --- | --- |
  | Daily Pipeline run | `#47` |
  | 결과 | 🟢 **SUCCESS** |
  | 실행 유형 | `MANUAL_PARTIAL` |
  | 요청자 | `VIEW_BUTTON` |
  | 실행 범위 | Step 1~11 |
  | total · success · no_target · failed · skipped | `11 · 7 · 4 · 0 · 0` |
  | duration | `1,362,039ms` |
  | Step 1~7 (CONNECTOR_BALANCE · INTEREST_CRAWLER · PREPROCESSOR · BACKTEST_RESEARCH · BACKTEST_REPORT · DAILY_BUY_SIGNAL · DAILY_POSITION_SIGNAL) | 모두 🟢 **SUCCESS** |
  | Step 8~11 (DAILY_BUY_EXECUTION · DAILY_SELL_EXECUTION · DAILY_AUTO_SELL · DAILY_AUTO_BUY) | 🔵 **NO_TARGET** |
  | broker 주문 제출 | 0건 |
  | `--execute` | 0건 |

- 🟢 **Step별 DB role override 검증 통과**

  | View / Step | DB Role |
  | --- | --- |
  | View JVM | `view_app` 유지 |
  | Step 1 (subprocess) | `marketconnector_app` |
  | Step 2 (subprocess) | `crawler_app` |
  | Step 3 (subprocess) | `preprocessor_app` |
  | Step 4~5 (subprocess) | `research_app` |
  | Step 6~7 (subprocess) | `decision_app` |
  | Step 8~11 (subprocess) | `execution_app` |

  - 1차 실증 — OD-DB-007 · OD-DB-008 · OD-DB-009 · OD-DB-011 · R-DATA-005 mitigation 회귀 0건.

- 🟢 **6/26 KRX 기준일 lag 결정적 증거 확보**

  | 항목 | 값 |
  | --- | --- |
  | 최초 생성 raw | `interest_program_raw` · `interest_shortsell_raw` 의 `2026-06-25` · `2026-06-26` row |
  | 6/26 AWS Step Function | `step1-11-approval-20260626-080004-0904111b` 🟢 **SUCCEEDED** |
  | Step2B Windows KRX GUI worker | `LastTaskResult=0` |
  | 실제 worker 수집일 | 직전 영업일 `2026-06-24` 까지만 |
  | 결합 정합 | R-DATA-017 [2026-06-28 결정적 증거] · R-AUTO-020 [2026-06-26 보강] |

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | 항목 | 값 |
  | --- | --- |
  | R-DATA-017 | [2026-06-28 결정적 증거] mitigation 메모 추가 |
  | 근거 | Local Step 2 실행 중 6/25 · 6/26 row 최초 생성 |
  | Status | 🟠 **Open** 유지 |
  | 승격 조건 | Step2B 성공 기준 강화 + Slack KRX 기준일 표시 + feature lag 정책 명문화 → 🟢 **Mitigated** |
  | R-AUTO-033 | [2026-06-28 보강] mitigation 메모 추가 |
  | 근거 | Spring feature flag 4종 기본 false + 서버단 POST 차단 + Step별 DB role override + broker 호출 0건 + Step 12 이상 미진입 |
  | Status | 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-28 후속 메모 | 추가 |
  | 완료 | 4건 (Step 1 단독 · Step 1~11 · DB role override · KRX lag 결정적 증거) |
  | 후속 | 4건 (Step 12~17 실행 preflight · Step2B 성공 기준 강화 · Slack KRX 기준일 표시 · feature lag 정책 명문화) |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 갱신 위치 | 2026-06-28 섹션 prepend |
  | 분량 | 5~10줄 요약 |
  | 포함 사실 | Run #46 · Run #47 · DB role 분리 · KRX lag 결정적 증거 · 다음 단계 |

- 🟢 OD-MS-037 (View Local AWS Paper read-only 1차 scope)

  | 항목 | 값 |
  | --- | --- |
  | 본문 변경 | 없음 (operator-decisions.md) |
  | Evidence 보강 | read-only 진입 후 Step 1~11 검증이 2차 로컬 검증으로 진입 |
  | Step 12 이상 | 별도 preflight 후 진입 정책 유지 |
  | 정식 메모 반영 | 후속 Change Log entry 책임 분리 |

- 🟢 04-strategy-batch-stepfunctions / 05-port-view-ecs-and-runbook 하위 operation-notes 정식 갱신은 본 일자 범위 밖 — Step 1~11 결과 · DB role override · KRX lag · Step 12~17 preflight 정식 반영은 04 · 05 spec 후속 phase 책임.

### Security

| 항목 | 결과 |
| --- | --- |
| secret 원문 (KIS app key/secret · 계좌번호 · token · RDS password · IAM/state machine ARN · Slack webhook URL · DB password · Administrator password 등) | 0건 |
| AWS · EventBridge · Lambda · SFN · SSM · EC2 · RDS · S3 · KIS 실행 | 0건 |
| AWS CLI · boto3 실행 | 0건 |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
| CloudWatch · SFN execution history · Lambda / KIS response · Spring log · Pipeline stdout 평문 인용 | 0건 |
| broker · KIS 신규 BUY / SELL / 취소 / 정정 · `--execute` | 0건 |
| fill · position sync 자동 재시도 | 0건 |
| aws-live 작업 | 0건 |
| Slack 발송 (검증 중) | 0건 |

> broker · KIS 호출 = Step 1 CONNECTOR_BALANCE 조회성 + Step 2 KRX worker 정상 실행 (`LastTaskResult=0`) 한정. Step 8~11 <span style="color:#0969DA">**NO_TARGET**</span>.

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- Pipeline run id `#46` · `#47` · 실행 유형 `MANUAL_PARTIAL` · 요청자 `VIEW_BUTTON`.
- 실행 범위 라벨 `Step 1~1` · `Step 1~11`.
- Step 코드 11종 — CONNECTOR_BALANCE · INTEREST_CRAWLER · PREPROCESSOR · BACKTEST_RESEARCH · BACKTEST_REPORT · DAILY_BUY_SIGNAL · DAILY_POSITION_SIGNAL · DAILY_BUY_EXECUTION · DAILY_SELL_EXECUTION · DAILY_AUTO_SELL · DAILY_AUTO_BUY.
- 결과 라벨 — `SUCCESS` · `NO_TARGET`.
- DB role 6종 — `view_app` · `marketconnector_app` · `crawler_app` · `preprocessor_app` · `research_app` · `decision_app` · `execution_app`.
- duration `1,362,039ms` · count 분포 `11/7/4/0/0`.
- workDir `C:/Workspaces/port-marketconnector` · command `python connector_balance.py` · exit code `0`.
- KRX raw 직전 적재일 `2026-06-24` · 본 일자 신규 생성 raw date `2026-06-25` · `2026-06-26`.
- 6/26 Step Function execution name `step1-11-approval-20260626-080004-0904111b`.
- placeholder — `[REDACTED]` · `[REDACTED_ACCOUNT_NO]`.

</details>

## 2026-06-27 (View Local AWS Paper read-only 1차 scope 완료 / ECS Fargate 이전 로컬 검증 통과)

### Added

- 🟢 **View Local AWS Paper read-only 1차 scope 완료**

  | 항목 | 값 |
  | --- | --- |
  | 접속 방식 | Local Spring Boot → SSM Port Forwarding (`127.0.0.1:15433`) → AWS Paper RDS |
  | 모드 | read-only 운영 콘솔 |
  | 정합 | OD-ENV-006 · OD-ENV-007 · OD-NET-010 · OD-NET-011 · OD-DB-009 |
  | Spring profile | aws-paper (신규) |
  | DB user | `view_app` 유지 |
  | Feature flag 4종 (`snapshot-refresh` · `order-refresh` · `strategy.execution.submit` · `connector-refresh`) | 모두 false 기본 |
  | KIS · Connector · Slack · Daily Batch 트리거 | 0건 |

- 🟢 **신규 결정 OD-MS-037**

  | 항목 | 값 |
  | --- | --- |
  | 내용 | View Local AWS Paper read-only 1차 scope 확정 |
  | ECS / Fargate 진입 전 조건 | Batch 2차 검증 (View Local Batch Step 1~17) 선행 |
  | Status | 🟢 **확정** |
  | 영향 spec | 04 · 05 · 10 |
  | Decision Summary | 전체 95 → 96 · 확정 50 → 51 · 잠정 42 유지 |

- 🟢 **잔고 / 보유 종목 화면**

  | 항목 | 값 |
  | --- | --- |
  | `BalanceService` 조회 원천 | legacy `balance_summary` → 최신 `connector_balance_snapshot` |
  | 보유 종목 | `connector_position_snapshot` 기준 |
  | `/balance-summary` · `/positions` | 화면 검증 통과 |
  | 보유 종목 0건 | empty card 정상 |
  | 화면 진입 중 KIS · Connector · Slack · Batch 호출 | 0건 |

- 🟢 **주문 / 주문 상세 화면**

  | 항목 | 값 |
  | --- | --- |
  | `/orders` 목록 · `/orders/56` 상세 | 정상 |
  | 표시 원천 | `connector_order_request` · `connector_order_event` · `connector_fill` |
  | `portfolio.order-refresh.enabled` | false |
  | KIS · Connector refresh · 주문 API 호출 | 0건 |

- 🟢 **전략 / 리포트 화면**

  | 항목 | 값 |
  | --- | --- |
  | `/strategy/execution/plans` | plan #113 표시 (신규 매수 차단 · 실행 후보 없음) |
  | `portfolio.strategy.execution.submit-enabled` | false |
  | POST submit 서버단 차단 | 확인 |
  | `/strategy/reports/latest` 기간 | `2023-01-27 ~ 2026-06-24` |
  | 누적 수익률 | `427.69%` |
  | MDD | `-8.94%` |
  | Sharpe | `2.48` |

- 🟢 **Daily Batch / 대시보드 화면**

  | 항목 | 값 |
  | --- | --- |
  | run · step log | 조회-only 구성 |
  | 실행 버튼 | disabled |
  | 서버단 POST 우회 | 차단 |
  | Step 12 이상 주문성 경로 | 기본 차단 |
  | `/dashboard` 문구 | "AWS Paper / Snapshot 기준 / 조회-only" |

- 🟢 **R-AUTO-033 신규 row 추가**

  | 항목 | 값 |
  | --- | --- |
  | 위험 | View Local 측 실행성 호출 / POST 우회로 의도치 않은 broker · batch · Slack 트리거 |
  | Status | 🟢 **Mitigated** |
  | Mitigation | Spring feature flag 4종 + 서버단 POST 차단 + 운영자 명시 ENABLE 후 단계적 진입 |

### Changed

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | OD-MS-037 | 신규 row |
  | Change Log | 2026-06-27 항목 추가 |
  | 본문 변경 | 없음 (OD-ENV-006 · OD-ENV-007 · OD-NET-010 · OD-NET-011 · OD-DB-009 · OD-MS-010 evidence 보강만) |
  | 1차 실증 | Local Spring Boot 의 AWS Paper RDS read-only 접속 1차 가동 통과 |
  | Decision Summary | 전체 95 → 96 · 확정 50 → 51 · 잠정 42 유지 |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | 항목 | 값 |
  | --- | --- |
  | R-AUTO-033 | 신규 row |
  | 위험 | View Local 측 실행성 호출 우회 |
  | Mitigation | Spring feature flag 4종 기본 false + 서버단 POST 차단 + 단계적 ENABLE |
  | Status | 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-27 후속 메모 | 추가 |
  | 완료 | 5건 (aws-paper profile · 잔고/보유 · 주문/주문 상세 · 전략/리포트 · Daily Batch/대시보드) |
  | 후속 | 4건 (View Local Batch Step 1~17 실행 검증 · Slack notifier 통합 · ECS/Fargate 배포 설계 · 05 spec 하위 갱신) |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 갱신 위치 | 2026-06-27 섹션 prepend |
  | 분량 | 5~10줄 요약 |
  | 포함 사실 | read-only scope 완료 + 화면 검증 + 결론 + 후속 + 안전/보안 정합 |

- 🟢 05-port-view-ecs-and-runbook 하위 정식 갱신은 본 일자 범위 밖. port-view MS 측 Spring Boot 소스 변경분(`application-aws-paper.properties` · `BalanceService.java` · controller · template)은 port-view MS 영역으로 cross-service AWS Migration spec 본 일자 작업으로 인한 변경 0건(spec 영역).

### Security

| 항목 | 결과 |
| --- | --- |
| secret 원문 (KIS app key/secret · 계좌번호 · token · RDS password · Slack webhook · DB password · Administrator password 등) | 0건 |
| AWS · Lambda · SFN · SSM · EC2 · RDS · S3 · KIS 실행 | 0건 |
| AWS CLI · boto3 실행 | 0건 |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
| CloudWatch · SFN · Lambda · KIS · Spring log 평문 인용 | 0건 |
| View Local 측 실행성 호출 (broker · batch · Slack) | 0건 |
| KIS · Connector refresh · 주문 API · Daily Batch run · Slack 발송 | 0건 |

> Spring Boot feature flag 4종 + 서버단 POST 차단으로 의도치 않은 트리거 차단 (R-AUTO-033 신규 정합).

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- `view_app` DB user · `127.0.0.1:15433` local port · SSM Port Forwarding 경유지 패턴.
- plan id `113` · order id `56`.
- 백테스트 기간 `2023-01-27 ~ 2026-06-24` · 누적 수익률 `427.69%` · MDD `-8.94%` · Sharpe `2.48`.
- Spring Boot feature flag 라벨 4종.
- route prefix — `/balance-summary` · `/positions` · `/orders` · `/orders/{id}` · `/strategy/execution/plans` · `/strategy/reports/latest` · `/dashboard` · `/daily-batch`.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-26 (AWS Paper Step 1~11 approval workflow 정기 실행 통과 + Step2B KRX 기준일 lag 1차 식별)

### Added

- 🟢 **08:00 EventBridge Scheduler 정기 실행 결과**

  | 항목 | 값 |
  | --- | --- |
  | Scheduler | `portfolio-paper-daily-step1-11-approval-0800-kst` 🟢 **ENABLED** |
  | Dispatcher Lambda | `portfolio-paper-daily-scheduler-dispatcher` |
  | State machine | `portfolio-paper-daily-step1-17-approval` |
  | execution name | `step1-11-approval-20260626-080004-0904111b` |
  | Status | 🟢 **SUCCEEDED** |
  | Step 12~17 default | false (approval gate 차단 유지) |
  | Slack | `APPROVAL_REQUIRED` 수신 |
  | 실주문 제출 | 0건 |
  | 신규 `connector_order_request` | 0건 |
  | 정합 | R-AUTO-025 [2026-06-24 보강] · OD-MS-029 · OD-MS-031 · OD-MS-032 · OD-MS-033 |

- 🟢 **Step2B Windows KRX GUI worker success 처리**

  | 항목 | 값 |
  | --- | --- |
  | Scheduled Task | `Portfolio-KRX-Worker-Daily` |
  | `LastTaskResult` | `0` |
  | 상태 | Running → Ready 복귀 |
  | 호출 경로 | KRX login / program / shortsell 정상 진입 |
  | mitigation 회귀 | R-AUTO-007 · R-AUTO-008 · R-AUTO-020 모두 0건 |

- 🟢 **R-DATA-017 신규 row**

  | 항목 | 값 |
  | --- | --- |
  | 위험 | Step2B worker process exit 0 / `LastTaskResult=0` 만으로 KRX raw 최신성 미보장 |
  | 영향 | stale 수집일이 preprocessor / decision / research 로 전파 |
  | 1차 식별 사례 | 2026-06-26 worker 로그 기준 수집일 `2026-06-24` |
  | Status | 🟠 **Open** |

### Changed

- 🟢 `.kiro/specs/_common/risk-register.md`

  | 항목 | 값 |
  | --- | --- |
  | R-AUTO-020 | [2026-06-26 보강] mitigation 메모 추가 |
  | 근거 | worker `LastTaskResult=0` + Step Function `SUCCEEDED` 에도 KRX raw 수집일이 직전 영업일 대비 lag |
  | `ExpectedKrxRawDate` 산정 기준 | worker 실제 수집일 분포 미반영 사례 |
  | Status | 🟢 **Mitigated** 유지 (worker 실행 자체는 정합) |
  | R-DATA-017 | 신규 row (Step2B 성공 기준 강화 후 승격 후보) |
  | Status | 🟠 **Open** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-26 후속 메모 | 추가 |
  | 완료 | 2건 (08:00 Scheduler 정기 실행 · Step2B worker 성공 처리) |
  | 후속 | 3건 (Step2B 성공 기준 강화 · Slack KRX 기준일 표시 · feature lag 허용 정책 재검토) |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 갱신 위치 | 2026-06-26 섹션 prepend |
  | 분량 | 5~10줄 요약 |
  | 포함 사실 | 08:00 Scheduler `SUCCEEDED` · Step2B success 처리 · 2026-06-28 local 검증 시 KRX lag 1차 식별 · 후속 3종 |

- 🟢 04 spec operation-notes / validation 문서 정식 갱신은 본 일자 범위 밖 — Step2B 성공 기준 강화 정식 반영은 04 / 08 spec 후속 phase 책임.

### Security

| 항목 | 결과 |
| --- | --- |
| secret 원문 (KIS · 계좌 · token · RDS password · IAM ARN · Slack webhook · Administrator password 등) | 0건 |
| AWS · Lambda · SFN · SSM · EC2 · RDS · S3 · KIS 실행 | 0건 |
| AWS CLI · boto3 실행 | 0건 |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
| CloudWatch · SFN · Lambda · Slack payload · KIS response · SSM stdout 평문 인용 | 0건 |
| broker · KIS 신규 BUY / SELL / 취소 / 정정 · `--execute` | 0건 |
| fill · position sync 자동 재시도 | 0건 |
| aws-live 작업 | 0건 |

> broker · KIS 호출 = 본 일자 08:00 schedule 의 Step 1~11 한정. Step 12 이후 주문 제출 0건.

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- Scheduler `portfolio-paper-daily-step1-11-approval-0800-kst`.
- Dispatcher Lambda `portfolio-paper-daily-scheduler-dispatcher`.
- State machine `portfolio-paper-daily-step1-17-approval`.
- execution name `step1-11-approval-20260626-080004-0904111b`.
- Slack 이벤트 라벨 `APPROVAL_REQUIRED`.
- Windows Scheduled Task `Portfolio-KRX-Worker-Daily` · `LastTaskResult=0`.
- KRX raw 수집일 `2026-06-24` · worker 로그 기준 일자 `2026-06-26` · run_date `2026-06-26`.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-25 (장중 포지션 Step Function 구현 완료 + blocked gate / no-target true-path 검증 통과 / 실주문 보류)

### Added

- 🟢 **장중 포지션 확인 3단계 구조 1·2·3단계 일부 구현 완료** (운영자 직접 수행)

  | 항목 | 값 |
  | --- | --- |
  | Kiro AWS CLI · boto3 · SFN · SSM · Lambda · EC2 · RDS · S3 · KIS API 실행 | 0건 |
  | AWS 리소스 생성 · 수정 · 삭제 | 0건 |
  | 8개 MS README · AGENTS · CHANGELOG · docs · worklog 변경 | 0건 |
  | Entrypoint 변경분 | 03 spec operation-notes 후속 갱신 책임 |
  | OD-MS-036 신규 | 🟢 **확정** |
  | 영향 spec | 03 · 04 · 05 · 10 |

- 🟢 `connector_intraday_snapshot_refresh.py` — MarketConnector 1단계 신규 entrypoint

  | 항목 | 값 |
  | --- | --- |
  | source_version | `connector-intraday-snapshot-refresh-1.0.0` |
  | SHA256 hash 검증 | 통과 |
  | KIS 잔고 · 포지션 snapshot | 갱신 |
  | Upsert 대상 | `connector_balance_snapshot` · `connector_position_snapshot` |
  | broker 주문 제출 | 0건 |
  | idempotent | 확인 |
  | 배포 | MarketConnector EC2 venv (SSM commandId `b44d7c4e-c21c-48c0-a3c0-3a5572935577`) |

- 🟢 `connector_intraday_position_evaluate.py` — StrategyExecution 2단계 신규 entrypoint

  | 항목 | 값 |
  | --- | --- |
  | source_version | `connector-intraday-position-evaluate-1.1.0` |
  | SHA256 hash 검증 | 통과 |
  | 실행 방식 | MarketConnector EC2 venv python SSM 실행 |
  | 안전장치 | 4종 (stale snapshot · duplicate order · `sellable_qty` · `current_price`) |
  | 저장 | `strategy_intraday_position_check` + `INTRADAY_STOP_SELL` READY order 생성 |
  | broker 주문 제출 | 0건 |
  | `daily_intraday_position_monitor_run.py` 수정 | 0건 |
  | 정합 | OD-MS-035 |

- 🟢 **wrapper `run_intraday_snapshot_and_evaluate.sh` 신규** — 1단계 snapshot refresh + 2단계 evaluate 를 SSM RunCommand 한 번에 실행하기 위한 운영 wrapper. MarketConnector EC2 venv python 호출. 운영자 직접 작성 · 배포.

- 🟢 **EventBridge Scheduler 1개 신규 ENABLED**

  | 항목 | 값 |
  | --- | --- |
  | Scheduler | `portfolio-paper-intraday-snapshot-evaluate-10min-kst` |
  | cron | `cron(10/10 9-15 ? * MON-FRI *)` |
  | Timezone | Asia/Seoul |
  | Flexible | OFF |
  | Target | SSM RunCommand 호출 dispatcher |
  | 검증 | 평일 장중 10분 간격 자동 tick 통과 |

- 🟢 **Step Functions 3단계 State Machine 1개 신규 생성**

  | 항목 | 값 |
  | --- | --- |
  | State machine | `portfolio-paper-intraday-stop-sell-approval` |
  | Type | STANDARD |
  | Status | 🟢 **ACTIVE** |
  | state 수 | 18 |
  | 생성 시각 | `2026-06-25T14:58:45+09:00` |
  | 분리 | Daily Step 1~17 state machine 과 완전 분리 |
  | approval gate | `CheckIntradayStopApproval` → `BlockedByIntradayStopApprovalGate` |
  | true-path state | 9종 |
  | broker 호출 위임 | SFN → SSM RunCommand + ECS RunTask.sync → MarketConnector EC2 |
  | Lambda 역할 | dispatcher 한정 (broker 호출 직접 수행 0건) |

  - true-path state 9종 — `RunIntradayStopOrderExecute` · `GetIntradayStopOrderExecuteInvocation` · `RunConnectorOrderCheck` · `GetConnectorOrderCheckInvocation` · `RunSyncSellFill` · `RunConnectorBalanceRefresh` · `GetConnectorBalanceRefreshInvocation` · `IntradayStopWorkflowSucceeded` · `IntradayStopWorkflowFailed`.

- 🟢 `connector_strategy_order_execute.py` — `signal_type=INTRADAY_STOP_SELL` 전용 필터 patch

  | 항목 | 값 |
  | --- | --- |
  | SHA256 hash 검증 | 통과 |
  | 운영 marker | `DEPLOY_CONNECTOR_STRATEGY_ORDER_EXECUTE_INTRADAY_STOP_FILTER=SUCCESS` |
  | 분리 정합 | Daily SELL (Step 12 `--execute`) ↔ Intraday Stop Sell |
  | R-AUTO-032 | 신규 mitigation 1차 실증 |
  | OD-MS-028 · OD-MS-033 | 본문 변경 없음 |

- 🟢 **안전 테스트 (a) blocked gate**

  | 항목 | 값 |
  | --- | --- |
  | executionName | `intraday-stop-blocked-gate-20260625-145928` |
  | input | `allowIntradayStopOrderExecute=false` |
  | Status | 🟢 **SUCCEEDED** |
  | approval gate | 차단 정합 |
  | true-path 진입 | 0건 |
  | marker | `INTRADAY_STOP_SELL_BLOCKED_GATE_TEST=SUCCESS` |

- 🟢 **안전 테스트 (b) true-path no-target**

  | 항목 | 값 |
  | --- | --- |
  | executionName | `intraday-stop-truepath-notarget-20260625-150146` |
  | input | `allowIntradayStopOrderExecute=true` |
  | Status | 🟢 **SUCCEEDED** |
  | Start | `2026-06-25T15:01:46+09:00` |
  | Stop | `15:02:53+09:00` |
  | true-path state 진입 | 정합 |
  | DB 비교 `INTRADAY_STOP_SELL` | 0 rows |
  | `READY/FAILED SELL` | 0 rows |
  | `max_connector_order_request_id` | 56 (변동 없음) |
  | `created_after_truepath_count` | 0 |
  | 신규 `connector_order_request` | 0건 |
  | KIS broker 주문 제출 | 0건 |

- 🟢 **운영 marker 5종 추가 기록**

  - `INTRADAY_STOP_SELL_ASL_DRAFT_VALIDATE=SUCCESS`
  - `INTRADAY_STOP_SELL_IAM_INSPECT=SUCCESS`
  - `INTRADAY_STOP_SELL_STATE_MACHINE_CREATE=SUCCESS`
  - `INTRADAY_STOP_SELL_BLOCKED_GATE_TEST=SUCCESS`
  - `DEPLOY_CONNECTOR_STRATEGY_ORDER_EXECUTE_INTRADAY_STOP_FILTER=SUCCESS`

- 🟢 **신규 리스크 3건 + 보강 1건**

  | ID | 상태 | 메모 |
  | --- | --- | --- |
  | R-AUTO-030 | 🟠 **Open** 유지 | [2026-06-25 보강] State Machine 생성 + blocked gate + no-target 검증 통과. 자동 ENABLE 진입 차단 정책 유지 |
  | R-AUTO-031 | 🟢 **Mitigated** | Intraday Stop Sell approval gate 오설정 위험. blocked gate 테스트 1차 실증 |
  | R-AUTO-032 | 🟢 **Mitigated** | Daily SELL ↔ Intraday Stop Sell `signal_type` 필터 부재 위험. 전용 필터 patch 배포 정합 |
  | R-BROKER-005 | 🟢 **Mitigated** | 보유 종목 0건 상태 실주문 시도 위험. 사전 검증 단계 중단 정합 |

### Changed

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | OD-MS-036 | 신규 row (Intraday Stop Sell Submit Workflow) |
  | Status | 🟢 **확정** |
  | 영향 spec | 03 · 04 · 05 · 10 |
  | Change Log | 2026-06-25 항목 추가 |
  | OD-MS-035 | [2026-06-25 보강] 메모 추가 · 본문 변경 없음 |
  | Decision Summary | 전체 94 → 95 · 확정 49 → 50 · 잠정 42 유지 |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | 항목 | 값 |
  | --- | --- |
  | R-AUTO-030 | [2026-06-25 보강] mitigation 메모 추가 · Status 🟠 **Open** 유지 |
  | R-AUTO-031 | 신규 (approval gate 오설정 위험) · Status 🟢 **Mitigated** |
  | R-AUTO-032 | 신규 (Daily SELL ↔ Intraday Stop Sell `signal_type` 필터 부재) · Status 🟢 **Mitigated** |
  | R-BROKER-005 | 신규 (보유 종목 0건 상태 실주문 시도 위험) · Status 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-25 후속 메모 | 추가 |
  | 완료 | 8건 (1단계 entrypoint · 2단계 entrypoint · Scheduler 자동 tick · `signal_type` 필터 patch · State Machine 생성 · blocked gate · no-target true-path · Step 17 run-date 동적화) |
  | 후속 | 5건 (실 1주 실주문 재개 · Slack 알림 연계 · 자동 ENABLE 진입 정책 · Dispatcher Lambda 로그 보강 · 03/04/05/06/10 spec 후속) |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-25 네 번째 메모 | 추가 |
  | 1순위 / 2순위 / 비권고 결정값 | 변경 없음 |
  | Crawler · Preprocessor · Decision · Execution · Research | 기존 결정 유지 |
  | 운영 메모 갱신 | MarketConnector 1·3단계 · StrategyExecution 2단계 · SFN 3단계 · Scheduler · Lambda dispatcher 역할 |
  | 성격 | 운영자 직접 실행 결과의 사실 기록 |
  | Kiro AWS CLI · boto3 실행 | 0건 |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md` — 본 일자 추가 없음. EventBridge Scheduler · Lambda · SSM · SFN · ECS 항목이 이미 존재하며 이미 있는 항목은 수정하지 않는 정책.

- 🟢 `.kiro/WORKLOG.md` — 2026-06-25 섹션 prepend (newest-first · 5~10줄 요약).

- 🟢 **장중 포지션 확인 전용 경로 상태 갱신**

  | 항목 | 값 |
  | --- | --- |
  | 이전 상태 | 2026-06-24 최종안 확정 / 문서 반영 |
  | 본 일자 진척 | 1단계 + 2단계 + 3단계 일부 구현 |
  | 진척 사항 | Scheduler 10분 tick · State Machine 생성 · 두 안전 테스트 통과 |
  | 실 broker 주문 | 0건 |
  | 실 1주 주문 테스트 | 보유 종목 발생 후 재개 |
  | 자동 ENABLE 진입 | 후속 phase + 별도 운영자 승인 |

- 🟢 **Daily Step 17 잔고 refresh task `RunConnectorBalanceRefresh` run-date 동적화 보강**

  | 항목 | 값 |
  | --- | --- |
  | 이전 | `2026-06-22` 고정 |
  | 변경 | KST 동적 `$(TZ=Asia/Seoul date +%F)` |
  | Daily ASL 변경 반영 | 03 spec operation-notes 후속 갱신 책임 |
  | OD-MS-034 | 본문 변경 없음 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret 원문 (KIS · 계좌 · token · RDS password · IAM ARN · Slack webhook · Administrator password 등) | 0건 |
| AWS · Lambda · SFN · SSM · EC2 · RDS · S3 · KIS 실행 | 0건 |
| AWS CLI · boto3 실행 | 0건 |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
| patch 본문 · SFN ASL · IAM Policy · Lambda · CloudWatch · KIS response · SSM stdout 평문 인용 | 0건 |
| 추가 BUY / SELL / 취소 / 정정 · `--execute` | 0건 |
| 신규 `connector_order_request` | 0건 |
| aws-live 작업 | 0건 |
| account-id 부분 (ARN 내부) | `[REDACTED]` 처리 |

> broker · KIS 호출 = KIS 강제 잔고 refresh 1회 (`EMPTY_NORMAL` · SSM commandId `5b1e5757-ea93-4eb6-a7a0-6d5a38d87840` · `rt_cd=0` · `output1_count=0` · `output2_count=1`) + Scheduler 자동 tick 의 snapshot refresh 한정.

<details>
<summary>🟠 실 1주 `INTRADAY_STOP_SELL` 주문 테스트 중단 · 보류</summary>

- 보유 종목 0건 · `sellable_qty=0` 으로 broker 호출 직전 사전 검증 단계에서 운영자 중단.
- 없는 포지션 매도 주문 생성 0건.
- `allowIntradayStopOrderExecute=false` gate 기본 차단 검증.
- no-target true-path 신규 주문 0건 검증.
- 정합 — R-BROKER-005 · R-AUTO-031 신규 mitigation 1차 실증.

</details>

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- SSM command_id — `b44d7c4e-c21c-48c0-a3c0-3a5572935577` · `5b1e5757-ea93-4eb6-a7a0-6d5a38d87840`.
- execution_name 2종 — blocked gate · true-path no-target (상세는 specs 참조).
- Scheduler `portfolio-paper-intraday-snapshot-evaluate-10min-kst` · cron `cron(10/10 9-15 ? * MON-FRI *)`.
- State Machine `portfolio-paper-intraday-stop-sell-approval` · state 이름 11종.
- 운영 marker 5종.
- source_version `connector-intraday-snapshot-refresh-1.0.0` · `connector-intraday-position-evaluate-1.1.0`.
- SHA256 3종 hash 검증 통과.
- `as_of_date=2026-06-25` · `as_of_ts=2026-06-25T06:07:27.438088` · `max_connector_order_request_id=56` · `output1_count=0` · `output2_count=1` · `EMPTY_NORMAL`.
- signal_type 라벨 `INTRADAY_STOP_SELL` · run_date `2026-06-25`.
- 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

</details>

## 2026-06-24 (08:00 Scheduler 실 실행 검증 + Step 12 retry-normalizer EGW00201 확장 + Step 12~17 수동 실행 4건 FILLED)

### Added

- 🟢 **08:00 EventBridge Scheduler 실 실행 검증** (OD-MS-032 후속)

  | 항목 | 값 |
  | --- | --- |
  | Scheduler | `portfolio-paper-daily-step1-11-approval-0800-kst` (State 🟢 **ENABLED**) |
  | 실행 경로 | Scheduler → Dispatcher Lambda → SFN `portfolio-paper-daily-step1-17-approval` |
  | Step 1~11 | 완료 (approval-required 흐름) |
  | Slack | `APPROVAL_REQUIRED` 수신 |
  | Step 12 이후 주문 | 차단 |
  | 신규 주문 | 0건 |
  | 09:01 Scheduler | 🟠 **DISABLED** 유지 |
  | 1차 실증 | R-AUTO-025 [2026-06-24 보강] mitigation |

- 🟢 **Step 12 retry-normalizer `EGW00201` 재시도 확장**

  | 항목 | 값 |
  | --- | --- |
  | 처리 대상 | 6/23 잔존 4건 (`40580000` 2건 + `EGW00201` 2건) |
  | 보강 사항 | 주문 사이 기본 sleep + `EGW00201` backoff retry + `submit_attempts` · `result_payload` 기록 |
  | 대상 파일 | `port-marketconnector/connector_strategy_order_execute.py` (운영자 직접 전체 교체) |
  | 배포 경로 | S3 업로드 + MarketConnector EC2 정식 배포 |
  | 검증 | `py_compile` + 운영 마커 통과 |
  | 본문 인용 | 0건 (03 spec operation-notes 후속 갱신 책임) |
  | dry-run `candidate_count` | 4 |
  | DB 변경 | 0건 |
  | OD-MS-033 신규 | 🟢 **확정** · 영향 spec 03/04/05/10 |
  | R-AUTO-026 신규 | mitigation 1차 실증 |

- 🟢 **Step 12~17 수동 실행 성공 + 4건 최종 FILLED**

  | 항목 | 값 |
  | --- | --- |
  | 실행 방식 | Dispatcher Lambda `STEP12_17_ORDER` 수동 invoke |
  | Slack | `DAILY_EXECUTION_SUCCESS` 수신 |
  | 4건 처리 결과 | 새 `connector_order_request` 생성 + `broker_order_no` 생성 + 최종 체결 |

  | 종목 코드 | 종목명 | 결과 |
  | --- | --- | --- |
  | `042660` | 한화오션 | 🟢 **FILLED** |
  | `004990` | 롯데지주 | 🟢 **FILLED** |
  | `003490` | 대한항공 | 🟢 **FILLED** |
  | `023530` | 롯데쇼핑 | 🟢 **FILLED** |

- 🟢 **`004990` 부분체결 후 단건 order-check 재조회로 전량체결 확인**

  | 항목 | 값 |
  | --- | --- |
  | 최초 Step 13 조회 | `PARTIAL_FILLED` |
  | 재조회 명령 | `--code 004990 --order-no <broker_order_no> --no-broad` |
  | 재조회 결과 | `tot_ccld_qty=69` 전량체결 |
  | 정합 | OD-MS-025 · R-AUTO-018 mitigation 회귀 0건 |

- 🟢 **신규 결정 · 리스크**

  | ID | Status | 메모 |
  | --- | --- | --- |
  | OD-MS-033 | 🟢 **확정** | Step 12 retry-normalizer 대상 확장 + KIS rate-limit backoff retry + 09:01 자동 ENABLE 보류 |
  | R-AUTO-026 | 🟢 **Mitigated** | `EGW00201` rate limit 재시도 누락 위험 |
  | R-AUTO-027 | 🔵 **Accepted** | `APPROVAL_REQUIRED` Slack summary 0/0 표시 위험 |

- 🟢 **EC2 자동 실행 구현 완료**

  | 항목 | 값 |
  | --- | --- |
  | 07:50 MarketConnector + Crawler start Scheduler | `portfolio-paper-ec2-start-0750-kst` 🟢 **ENABLED** |
  | 07:50 cron | `cron(50 7 ? * MON-FRI *)` · Asia/Seoul |
  | 07:50 Target input | `{"action":"start","target":"BOTH","holidayCheck":true,"reason":"PRE_DAILY_STEP1_11","dryRun":false}` |
  | 15:50 MarketConnector stop Scheduler | `portfolio-paper-marketconnector-stop-1550-kst` 🟢 **ENABLED** |
  | 15:50 cron | `cron(50 15 ? * MON-FRI *)` · Asia/Seoul |
  | 15:50 Target input | `{"action":"stop","target":"MARKETCONNECTOR","holidayCheck":false,"reason":"POST_MARKET_CLOSE","dryRun":false}` |
  | Step 1~11 성공 후 Crawler stop | SFN 의 `StopCrawlerEc2AfterStep11Success` task state 신규 |
  | State machine | `portfolio-paper-daily-step1-17-approval` |
  | RevisionId | `edd92cc9-1d94-4752-9a43-b7eb5b2f3c2c` |
  | 권한 추가 | SFN execution role 에 EC2 lifecycle Lambda invoke |
  | OD-MS-034 신규 | 🟢 **확정** · 영향 spec 03/04/05/08/10 |

- 🟢 **EC2 lifecycle Lambda 신규 구성**

  | 항목 | 값 |
  | --- | --- |
  | Lambda | `portfolio-paper-ec2-lifecycle-dispatcher` |
  | Runtime | Python 3.12 |
  | Handler | `lambda_function.lambda_handler` |
  | Timeout | 30s |
  | Memory | 256MB |
  | State | 🟢 **Active** |
  | IAM Role | `portfolio-paper-ec2-lifecycle-dispatcher` (전담 신규) |
  | 권한 | EC2 start · stop |
  | 환경변수 | `TIMEZONE=Asia/Seoul` · `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true` |
  | 휴일 체크 정책 | start 만 적용 · stop 은 미적용 (장 후 stop 은 휴일과 무관 · 이미 stopped 시 idempotent) |

- 🟢 **EC2 lifecycle 검증 결과**

  | 항목 | 값 |
  | --- | --- |
  | dryRun 3종 (`start BOTH` · `stop CRAWLER` · `stop MARKETCONNECTOR`) | 통과 |
  | 주말 skip (`runDate=2026-06-27` Saturday) | fail-closed 통과 |
  | 두 Scheduler `get-schedule` | State 🟢 **ENABLED** · Flexible OFF · Target Lambda · Target input 정합 |
  | SFN ASL 백업 후 update | 통과 · State Machine 🟢 **ACTIVE** · RevisionId `edd92cc9-1d94-4752-9a43-b7eb5b2f3c2c` |
  | 실 영업일 07:50 / 15:50 실행 검증 | 다음 영업일 예정 |
  | R-AUTO-028 신규 | 🟢 **Mitigated** |

- 🟢 **장중 포지션 확인 3단계 구조 최종안 확정 / 문서 반영** — 실제 구현 완료가 아니라 최종 설계안 확정 / 문서 반영 단계

  | 항목 | 값 |
  | --- | --- |
  | 실제 AWS · Lambda · SSM · SFN · RDS · KIS API 실행 | 0건 |
  | AWS 리소스 생성 · 수정 · 삭제 | 0건 |
  | 애플리케이션 소스 수정 | 0건 |
  | 1단계 (MarketConnector 10분 Snapshot Refresh) | Scheduler → Lambda → SSM → MC EC2 · balance/position snapshot 갱신 · 판단·주문 0건 · idempotent |
  | 2단계 (StrategyExecution Intraday Evaluate) | 1단계 직후 SSM · `strategy_intraday_position_check` 저장 + READY order 생성까지만 · broker 주문 0건 · 안전장치 4종 · 신규 파일 구현 · SFN Retry 자동 주문 연결 금지 |
  | 3단계 (Intraday Stop Sell Submit & Refresh) | 별도 SFN · `source_type=INTRADAY_STOP_SELL` 전용 필터 · 초기 수동 승인 후 실행 · Daily BUY/SELL 흐름과 분리 |
  | OD-MS-035 신규 | 🟢 **확정** · 영향 spec 03/04/05/10 |

- 🟢 **장중 안전장치 4종 명시**

  | 항목 | 검증 내용 |
  | --- | --- |
  | (a) stale snapshot | 1단계 `as_of_ts` + 2단계 진입 전 최신성 검증 |
  | (b) duplicate order | `account_id` + `ticker_code` + `source_type=INTRADAY_STOP_SELL` row 사전 점검 + idempotency 키 후속 |
  | (c) `sellable_qty` | snapshot 의 `sellable_qty` 와 후보 수량 사전 비교 · 부족 시 READY 생성 차단 |
  | (d) `current_price` | null · 0 · 과거 timestamp · 비정상 ±X% 범위 sanity check · 실패 시 evaluate 진입 차단 |

- 🟢 **신규 리스크 5건**

  | ID | Status |
  | --- | --- |
  | R-DATA-014 | 🟢 **Mitigated** |
  | R-AUTO-029 | 🟢 **Mitigated** |
  | R-DATA-015 | 🟢 **Mitigated** |
  | R-DATA-016 | 🟢 **Mitigated** |
  | R-AUTO-030 | 🟠 **Open** (3단계 자동 ENABLE 보류 · 정식 구현 + 검증 통과 후 🟢 **Mitigated** 승격) |

### Changed

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | 변경 유형 | 결정 락 확장 + Change Log 3항목 추가 |
  | OD-MS-033 확장 | Step 12 retry-normalizer 대상 = `40580000` + `EGW00201` (복구 6조건 중 rejection_code 한정 조건만 갱신 · `broker_order_no IS NULL` + `connector_fill` 없음 조건 유지 · R-AUTO-001 / R-BROKER-004 중복 주문 우회 0건) |
  | `EGW00201` backoff 운영 메모 | 즉시 반복 제출 금지 · sleep 간격 확장 재시도 · KIS 권고 backoff 기본값 유지 |
  | OD-MS-034 신규 row + Change Log 2번째 항목 | EC2 lifecycle 자동 실행 |
  | OD-MS-035 신규 row + Change Log 3번째 항목 | 장중 포지션 확인 3단계 구조 최종안 · 🟢 **확정** · 영향 spec 03/04/05/10 |
  | Decision Summary | 전체 92 → 93 → 94 / 확정 47 → 48 → 49 / 잠정 42 유지 |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | 변경 |
  | --- | --- |
  | R-AUTO-025 mitigation | [2026-06-24 보강] 08:00 첫 실 실행 통과 · Scheduler · Dispatcher Lambda · Step Functions 연결 사슬 정상 · Status 🟢 **Mitigated** · 09:01 자동 ENABLE 별도 운영자 승인 보류 유지 |
  | R-AUTO-025 mitigation | [2026-06-24 두 번째 보강] EC2 lifecycle 자동화 사슬 ENABLED |
  | R-AUTO-026 / R-AUTO-027 | 반영 |
  | R-AUTO-028 신규 | EC2 lifecycle Scheduler · Lambda 설정 오류 위험 · Status 🟢 **Mitigated** |
  | R-DATA-014 신규 | stale snapshot 기반 잘못된 손절 판단 · 🟢 **Mitigated** |
  | R-AUTO-029 신규 | duplicate `INTRADAY_STOP_SELL` `READY` order 생성 · 🟢 **Mitigated** |
  | R-DATA-015 신규 | `sellable_qty` 부족 상태에서 손절 주문 제출 · 🟢 **Mitigated** |
  | R-DATA-016 신규 | `current_price` 누락 또는 비정상 가격 기반 판단 · 🟢 **Mitigated** |
  | R-AUTO-030 신규 | 3단계 Submit & Refresh Step Functions 가 너무 일찍 ENABLE 되어 승인 없이 주문 제출 · 🟠 **Open** |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | 항목 | 값 |
  | --- | --- |
  | EventBridge Scheduler / Lambda / Step Functions 3항목 | [2026-06-24 1차 실 실행 검증 통과] 메모 추가 · Scheduler 는 SFN 직접 실행이 아니라 Dispatcher Lambda 호출 · Dispatcher 가 scheduleType 별 workflow 분기 · 09:01 Scheduler `DISABLED` 유지로 단계적 자동화 안전장치 |
  | EventBridge Scheduler 2차 보강 | EC2 lifecycle Scheduler 2개 ENABLED (07:50 EC2 start · 15:50 MarketConnector stop) · Scheduler 는 휴일 미인지 · Lambda holiday guard 가 start 요청에서 동작 |
  | Lambda 2차 보강 | Slack notifier · scheduler dispatcher · EC2 lifecycle dispatcher 3분리 · orchestration 보조 계층 유지 |
  | Step Functions 2차 보강 | Step 1~11 success path 에 Crawler stop Lambda task (`StopCrawlerEc2AfterStep11Success`) · Approval Slack 전 Crawler stop 수행 · RevisionId `edd92cc9-1d94-4752-9a43-b7eb5b2f3c2c` |
  | EC2 항목 신규 | MarketConnector EC2 07:50 start · 15:50 stop · Crawler EC2 07:50 start · Step 1~11 성공 시 stop · 실패 시 디버깅 유지 |
  | 3차 보강 (장중 포지션 확인) | 10분 Snapshot Refresh Scheduler 후보 `portfolio-paper-intraday-snapshot-refresh-10min-kst` 추가<br>intraday snapshot refresh orchestration helper Lambda 후보 추가<br>SSM RunCommand 항목에 snapshot refresh + intraday evaluate 제어 경로 추가<br>Intraday Stop Sell Submit & Refresh 별도 state machine 후보 추가<br>MarketConnector EC2 장중 10분 refresh 대상 메모 추가 |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-24 후속 메모 | 완료 5건 + 후속 5건 |
  | 2026-06-24 두 번째 후속 | 완료 3건 + 후속 9건 = 다음 영업일 07:50 / Step 1~11 성공 후 Crawler stop / 15:50 실 실행 관찰 · 09:01 ENABLE 보류 · Slack summary 개선 · Dispatcher 로그 보강 · webhook URL 이전 · EGW00215 backoff · Holiday API 백업 |
  | 2026-06-24 세 번째 후속 | 장중 포지션 확인 3단계 구조 최종안 확정 · 완료 5건 (3단계 구조 락 · 신규 파일 구현 결정 · 장중 전용 경로 분리 · OD-MS-035 신규 · R-DATA-014 외 5건 신규) + 내일 구현 예정 (1단계 Scheduler · Lambda · SSM · 2단계 신규 evaluate 파일 · 3단계 별도 Step Functions · Slack 알림 연계) + 하위 spec (03 · 04 · 05 · 06 · 10) 수정 후보 |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-24 메모 | Daily Batch orchestration 5계층 조합 1차 실증 · 08:00 Step 1~11 approval-required 자동 실행 · Step 12~17 수동 Dispatcher invoke 4건 FILLED · Step 12 retry-normalizer + KIS rate-limit backoff · Lambda 는 orchestration dispatcher/notifier 보조 계층 유지 |
  | 2026-06-24 두 번째 메모 | EC2 lifecycle 자동 실행 구현 완료 · orchestration 6계층 조합 확장 |
  | 2026-06-24 세 번째 메모 | 장중 포지션 확인 3단계 구조 최종안 · MarketConnector 10분 Snapshot Refresh 는 MC EC2 + SSM 중심 · Intraday Evaluate 는 신규 파일 SSM 실행 후보 · READY order 생성까지만 · Intraday Stop Sell Submit & Refresh 는 별도 SFN 후보 · Lambda 는 Scheduler/lifecycle/notifier/orchestration helper 역할 · 8개 MS 1순위 결정값 변경 없음 |

- 🟢 **Daily Batch orchestration 문서 상태 갱신**

  | 항목 | 값 |
  | --- | --- |
  | 6계층 조합 | SFN + EventBridge Scheduler + Dispatcher Lambda + EC2 lifecycle Lambda + ECS RunTask + SSM RunCommand + AWS Batch |
  | 장중 포지션 확인 전용 경로 | 1단계 Scheduler + Lambda + SSM / 2단계 SSM + Execution 신규 entrypoint / 3단계 별도 SFN + Slack (후속 구현 예정) |
  | Lambda 역할 3분리 | `portfolio-paper-daily-scheduler-dispatcher` (scheduler dispatcher) · `portfolio-paper-ec2-lifecycle-dispatcher` (EC2 lifecycle dispatcher) · `portfolio-event-notifier` (Slack notifier) · 후속 phase 에 intraday snapshot refresh dispatcher 추가 예정 |

- 🟢 **자동화 상태 갱신**

  | 항목 | 값 |
  | --- | --- |
  | EventBridge 자동화 | 구현 완료 · dryRun 검증 완료 · 08:00 실제 실행 검증 완료 · 09:01 자동 실행 🟠 **DISABLED** 유지 |
  | 09:01 자동 ENABLE 보류 | OD-MS-033 정합 · `APPROVAL_REQUIRED` Slack summary 0/0 개선 (R-AUTO-027) · Dispatcher Lambda application log 보강 · 운영 회차 누적 결과 점검 후 판단 |
  | EC2 lifecycle 자동화 | 구현 완료 · Lambda dryRun 3종 + 주말 skip 검증 완료 · 07:50 🟢 **ENABLED** · 15:50 🟢 **ENABLED** · Step 1~11 성공 시 Crawler stop 연결 완료 · 실 영업일 첫 실행 검증 예정 |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-24 섹션 추가 bullet | EC2 lifecycle 자동 실행 구현 완료 · 07:50 · 15:50 Scheduler ENABLED · SFN 성공 경로 변경 · Lambda 역할 3분리 · R-AUTO-028 신규 · R-AUTO-025 두 번째 보강 · Decision Summary 92 → 93 갱신 |

- 🟢 `.kiro/README.md`

  | 항목 | 값 |
  | --- | --- |
  | 현재 진행 상태 요약 | "EC2 lifecycle 자동 실행 구현 완료(2026-06-24)" 항목 추가 · Lambda 역할 3분리 반영 · 기준일 변경 없음 |

### Security

  | 항목 | 결과 |
  | --- | --- |
  | secret · token · password · ARN · IAM · webhook URL · Step Functions ARN · Lambda ARN 평문 기록 | 0건 (모두 `[REDACTED]` 또는 placeholder) |
  | 운영자 직접 patch `port-marketconnector/connector_strategy_order_execute.py` 전체 교체 본문 인용 | 0건 (R-DOCS-001 · 03 spec operation-notes 후속 갱신 책임) |
  | 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 spec 영역 변경 | 0건 |
  | AWS · EventBridge · Lambda · SFN · SSM · EC2 · RDS · S3 · Slack · KIS API | 모두 운영자 직접 수행 · Kiro 는 루트 / `_common` 문서 갱신만 |
  | AWS CLI / boto3 실행 | 0건 |
  | AWS 리소스 생성 · 수정 · 삭제 | 0건 |
  | `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
  | CloudWatch Logs · SFN history · KIS response · Lambda 응답 · Slack 메시지 · Scheduler `get-schedule` · `simulate-principal-policy` · dryRun 응답 · patch 본문 평문 인용 | 0건 |
  | broker / KIS 호출 | Step 12 paper 4건 재제출 (`042660` · `004990` · `003490` · `023530` BUY MARKET · 6/23 잔존 4건 retry-normalizer 후 새 `connector_order_request`) + Step 13 체결조회 |
  | 추가 BUY / SELL / 취소 / 정정 / `--execute` | 0건 |
  | fill · position sync 자동 재시도 | 0건 |
  | aws-live 작업 | 0건 |
  | SELL position `mark_position_sell_ordered()` 호출 | 0건 (SELL 흐름 0건) |

<details><summary>🔵 운영 식별자 요약</summary>

  | 항목 | 값 |
  | --- | --- |
  | rejection_code | `40580000` · `EGW00201` |
  | dry-run | `candidate_count=4` |
  | 종목 코드 | `042660` · `004990` · `003490` · `023530` |
  | `004990` 전환 | PARTIAL_FILLED → FILLED · `tot_ccld_qty=69` |
  | Scheduler | `portfolio-paper-daily-step1-11-approval-0800-kst` · `portfolio-paper-daily-step12-17-order-0901-kst` · `portfolio-paper-ec2-start-0750-kst` · `portfolio-paper-marketconnector-stop-1550-kst` |
  | Dispatcher Lambda | `portfolio-paper-daily-scheduler-dispatcher` · `portfolio-paper-ec2-lifecycle-dispatcher` |
  | SFN state machine | `portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval` |
  | SFN state 이름 | `StopCrawlerEc2AfterStep11Success` · `SendApprovalRequiredSlack` · `SendDailyExecutionFailedSlack` · `Step6ToStep11_Succeeded` · `Step12_CheckApproval` |
  | SFN RevisionId | `edd92cc9-1d94-4752-9a43-b7eb5b2f3c2c` |
  | cron 표현식 | `cron(0 8 ? * MON-FRI *)` · `cron(1 9 ? * MON-FRI *)` · `cron(50 7 ? * MON-FRI *)` · `cron(50 15 ? * MON-FRI *)` |
  | scheduleType | `STEP12_17_ORDER` |
  | EC2 lifecycle action | `start` · `stop` |
  | target 라벨 | `BOTH` · `CRAWLER` · `MARKETCONNECTOR` |
  | reason 라벨 | `PRE_DAILY_STEP1_11` · `STEP1_11_SUCCESS` · `POST_MARKET_CLOSE` · `STEP1_11_FAILED` |
  | Slack 이벤트 | `APPROVAL_REQUIRED` · `DAILY_EXECUTION_SUCCESS` · `DAILY_EXECUTION_FAILED` |
  | run_date | `2026-06-24` |
  | 주말 skip 검증 | `runDate=2026-06-27` |

</details>

## 2026-06-23 (Step Functions approval 실전 검증 + Step 12 retry-normalizer + DB 한글 정상 확인)

### Added

- 🟢 **Step Functions state machine `portfolio-paper-daily-step1-17-approval` 실전 검증**

  | 항목 | 값 |
  | --- | --- |
  | Step 1~17 `allowPaperOrderExecute=false` blocked path | 통과 |
  | Step 12~17 `allowPaperOrderExecute=true` true path | 통과 |
  | 첫 실 SELL 1건 | BGF리테일 `282330` 17주 MARKET |
  | `connector_order_request id 48` | 🟢 **FILLED** |
  | `strategy_execution_order id 40` | 🟢 **FILLED** |
  | `broker_order_no` | `0000006143` |
  | Step 17 balance refresh 후 최신 보유 | 4종목 정상 반영 |
  | Step Functions 자체 구현 상태 | 운영자 실증 단계 진입 · ms-aws-service-decision-matrix 본문 유지 |
  | 03 / 04 spec operation-notes 누적 | 후속 (2차 / 3차) 책임으로 분리 |

- 🟢 **Step 12 retry-normalizer 운영 보완 기록**

  | 항목 | 값 |
  | --- | --- |
  | 대상 파일 | `port-marketconnector/connector_strategy_order_execute.py` (전체 교체) |
  | 반영 위치 | Step 12 시작부 · 기존 Step 12 내부 안전 보완 · 별도 Step 11.5 아님 |
  | 복구 조건 | `execution_mode = PAPER_STRATEGY` + `action_type IN (BUY, SELL)` + `execution_status IN (READY, FAILED)` + `connector_order_request_id IS NOT NULL` + linked `connector_order_request.request_status = REJECTED` + `rejection_code = 40580000` + `broker_order_no IS NULL` + `connector_fill` 없음 |
  | 복구 처리 | `execution_status = REQUESTED` + `connector_order_request_id = NULL` + `result_payload.retry_normalizer` 에 old request 이력 저장 |
  | EC2 정식 배포 | 완료 |
  | `.venv/bin/python` dry-run | 통과 (retry 후보 0건 · REQUESTED 주문 0건) |
  | Step Functions Step 12 | `.venv/bin/python` 사용 정합 |

- 🟢 **AWS 공통 Slack notifier Lambda 구현 + 1차 검증**

  | 항목 | 값 |
  | --- | --- |
  | Lambda | `portfolio-event-notifier` |
  | Runtime | Python 3.12 |
  | IAM Role | `portfolio-event-notifier-lambda-role` |
  | 역할 | PORT-STRATEGY-AI 전체 운영 이벤트 (SFN · EventBridge · EC2 SSM · Batch · Lambda) 공통 알림 계층 단일 진입점 |
  | 검증 | `hello wook` 수동 invoke 성공 · Slack 수신 · Portfolio Daily Bot 메시지 수신 |
  | 완료 marker | `SLACK_LAMBDA_SMOKE_TEST=SUCCESS` |
  | Lambda 코드 / 환경변수 값 / webhook URL 평문 기록 | 0건 (R-DOCS-001 정합 · 모두 `[REDACTED]` 또는 placeholder) |

- 🟢 **Slack 공통 메시지 템플릿 6종 1차 구성**

  | 번호 | 메시지 |
  | --- | --- |
  | (1) | 장 전 잔고 / 보유 종목 상태 |
  | (2) | Daily 검증 완료 · 승인 필요 |
  | (3) | Daily 실행 성공 |
  | (4) | Daily 실행 실패 |
  | (5) | 장중 손절 후보 |
  | (6) | 장 후 잔고 / 보유 종목 상태 |
  | 손익 이모지 | 🔴 · 🔵 · ⚪ + Slack attachment color bar |
  | 완료 marker | `PORTFOLIO_EVENT_NOTIFIER_TEMPLATE_TEST=SUCCESS` |

- 🟢 **Step Functions approval workflow 3종 Slack 수신 검증**

  | 이벤트 | 결과 |
  | --- | --- |
  | (1) `APPROVAL_REQUIRED` | Step 1~11 완료 후 approval gate 진입 시 발송 · 승인 필요 인지 흐름 🟢 **통과** |
  | (2) `DAILY_EXECUTION_SUCCESS` | Step 17 완료 후 전체 성공 시 발송 · 🟢 **통과** |
  | (3) `DAILY_EXECUTION_FAILED` | SFN Catch 경로 · 12~17 test-only 실패 수신 + 1~17 full test-only 실패 ASL 적용 · 🟢 **통과** · 실제 broker 주문 실패 유발 0건 |

- 🟢 **7. EventBridge 자동화 구현: 완료** — OD-MS-032 신규 · 🟢 **확정** · 영향 spec 04 · 05 · 10

  | 항목 | 값 |
  | --- | --- |
  | Scheduler `portfolio-paper-daily-step1-11-approval-0800-kst` | cron `cron(0 8 ? * MON-FRI *)`<br>Asia/Seoul<br>Flexible OFF<br>Target Lambda `portfolio-paper-daily-scheduler-dispatcher`<br>Target Role `portfolio-paper-eventbridge-scheduler-role`<br>Target input `{"scheduleType":"STEP1_11_APPROVAL","dryRun":false}`<br>State 🟢 **ENABLED**<br>`allowPaperOrderExecute=false` |
  | Scheduler `portfolio-paper-daily-step12-17-order-0901-kst` | cron `cron(1 9 ? * MON-FRI *)` · Asia/Seoul · Flexible OFF · Target Lambda 동일 · Target input `{"scheduleType":"STEP12_17_ORDER","dryRun":false}` · State 🟠 **DISABLED** · `allowPaperOrderExecute=true` |
  | Dispatcher Lambda `portfolio-paper-daily-scheduler-dispatcher` | Runtime Python 3.12<br>Handler `lambda_function.lambda_handler`<br>Timeout 30s<br>Memory 256MB<br>State 🟢 **Active**<br>IAM Role `portfolio-paper-daily-scheduler-dispatcher-role`<br>환경변수 `TIMEZONE=Asia/Seoul`<br>`HOLIDAY_COUNTRY=KR`<br>`FAIL_CLOSED_ON_HOLIDAY_ERROR=true`<br>Step1~17 / Step12~17 State Machine ARN 미기록 |
  | Dispatcher Lambda 역할 | KST `runDate` (YYYY-MM-DD) 생성 · 주말 · 휴장일 skip · scheduleType 별 payload 분기 · SFN `StartExecution` 호출 · 즉시 종료 (SFN 완료 대기 없음) |
  | IAM Role / Inline policy | 4종 + 3종 |

- 🟢 **EventBridge 자동화 검증 완료**

  | 검증 | 결과 |
  | --- | --- |
  | IAM `simulate-principal-policy` | `states:StartExecution` + `lambda:InvokeFunction` 모두 🟢 **allowed** |
  | Lambda dryRun (08:00 `STEP1_11_APPROVAL`) | 통과 · `started=false` · `reason=DRY_RUN_NO_START_EXECUTION` |
  | Lambda dryRun (09:01 `STEP12_17_ORDER`) | 통과 · target state machine + `allowPaperOrderExecute` 분기 정합 |
  | Scheduler `get-schedule` | 08:00 🟢 **ENABLED** / 09:01 🟠 **DISABLED** (단계적 활성화 정합) |
  | 대상 Step Functions ACTIVE | `portfolio-paper-daily-step1-17-approval` + `portfolio-paper-daily-step12-17-approval` |
  | R-AUTO-025 신규 | Scheduler · Dispatcher Lambda · SFN 연결 실패 위험 · Status 🟢 **Mitigated** |

### Changed

- 🟢 `.kiro/README.md`

  | 항목 | 값 |
  | --- | --- |
  | 기준일 | `2026-06-22` → `2026-06-23` |
  | Backend AWS E2E / SFN 상태 행 보강 | Daily AWS Paper wrapper 1~17 실운영 완주 · SFN approval false / true path 실전 검증 · Step 12 retry-normalizer EC2 배포 + dry-run 성공 · DB 한글 저장 정상 확인 |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | OD-MS-028 신규 | Step 12 retry-normalizer 내장 정책 · 🟠 **잠정** · 영향 spec 03/04/10 |
  | OD-MS-029 신규 | SFN approval false / true path 운영 절차 · 🟢 **확정** · 영향 spec 04/10 |
  | Decision Summary | 전체 86 → 88 / 확정 43 → 44 / 잠정 40 → 41 |
  | 1차 실증 보강 | OD-MS-009 · OD-MS-027 · OD-SAFE-001 · OD-SAFE-004 |
  | Change Log | 2026-06-23 항목 추가 |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | 변경 |
  | --- | --- |
  | R-AUTO-022 신규 | 장종료 REJECTED · `40580000` 후 rejected `connector_order_request_id` 물고 있는 execution_order 가 다음날 자동 재제출되지 않는 위험 · 🟢 **Mitigated** |
  | R-AUTO-021 mitigation | [2026-06-23 보강] SFN 전환 이후에도 Step 1 · 12 · 13 · 17 bootstrap 유지 필요 |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-23 후속 메모)

  | 구분 | 항목 |
  | --- | --- |
  | 완료 | SFN approval false / true path 실전 검증 · Step 12 retry-normalizer EC2 배포 + dry-run 검증 · DB 한글 저장 정상 확인 |
  | 후속 유지 | EGW00215 balance refresh rate-limit backoff · retry policy · PowerShell · SSM · AWS CLI 한글 출력 표시 · Step 12 retry-normalizer 실 후보 발생 시 운영 검증 · View Daily Batch 화면 retry-normalizer · approval gate 결과 표시 검토 |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | port-interest-crawler 행 위 메모 | "2026-06-23 SFN approval workflow false · true path 실전 검증 완료 · MarketConnector EC2 + SSM + SFN 조합으로 Step 12~17 true path 완료 · Strategy Execution · MarketConnector 간 REQUESTED → broker submit → fill sync → balance refresh 경로 확인 · retry-normalizer 는 Step 12 내부 안전 보완으로 반영" 한 단락 추가 |
  | 결정값 (1순위 / 2순위 / 비권고) | 변경 없음 |

- 🟢 **EventBridge 자동화 1차 범위를 Slack 3종으로 한정**

  | 항목 | 값 |
  | --- | --- |
  | OD-MS-031 신규 | 🟢 **확정** · 영향 spec 04 · 05 · 10 |
  | 1차 Slack 이벤트 | `APPROVAL_REQUIRED` + `DAILY_EXECUTION_SUCCESS` + `DAILY_EXECUTION_FAILED` |
  | 후속 분리 | 장 전 잔고 · 장 후 잔고 · 장중 손절 Slack |
  | 1차 목표 | 문구 고도화가 아니라 SFN 실행 흐름에서 Slack 수신 여부 검증 |

- 🟢 **기존 port-view Slack 유지**

  | 항목 | 값 |
  | --- | --- |
  | 책임 | port-view SlackNotificationService · View Daily Batch 수동 실행 결과 알림 |
  | 이번 작업 | 제거 · 대체 없음 |
  | 향후 | 공통 notifier 로 이전 가능성만 기록 (OD-MS-010 본문 변경 없음 · OD-MS-030 신규 정합) |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md`

  | 항목 | 값 |
  | --- | --- |
  | 1차 보강 | Lambda · EventBridge Scheduler · SFN · Secrets Manager · SSM Parameter Store 4개 항목 · Slack notifier `portfolio-event-notifier` 메모 · SFN Catch 경로 정합 · Slack webhook 이전 후보 · OD-MS-030 · OD-MS-031 · R-AUTO-023 · R-AUTO-024 정합 |
  | 2차 보강 (Lambda / EventBridge Scheduler) | Dispatcher Lambda `portfolio-paper-daily-scheduler-dispatcher` 운영 자동화 dispatcher · SFN StartExecution 후 즉시 종료 · Scheduler 는 KRX 휴장일 미인지 · Lambda guard 단일 책임 · 2개 schedule + 단계적 활성화 (OD-MS-032 / R-AUTO-025 정합) |
  | 기존 본문 / 결정값 / 비용 모델 | 변경 없음 |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md` (2차 · 3차 메모)

  | 메모 | 내용 |
  | --- | --- |
  | 2026-06-23 두 번째 | AWS 공통 Slack notifier 구현 + SFN 3종 Slack 검증 · OD-MS-030 · OD-MS-031 신규 · R-AUTO-023 · R-AUTO-024 신규 · Lambda 비권고 유지 · 운영 이벤트 알림 보조 서비스로만 사용 근거 보강 · SFN + Scheduler + Lambda notifier 를 운영 관측성 / 포트폴리오 어필 관점 기록 · Daily Batch 1차 Slack 3종 · port-view Slack 유지 · 결정값 변경 없음 |
  | 2026-06-23 세 번째 | Scheduler + Dispatcher Lambda 기반 Daily 자동화 구현 완료 · OD-MS-032 신규 · R-AUTO-025 신규 · Daily Batch orchestration 5계층 조합 (SFN + Scheduler + Dispatcher Lambda + ECS RunTask + SSM RunCommand + AWS Batch) · Lambda 는 orchestration input 보정 · 휴장일 guard · StartExecution dispatcher · 결정값 변경 없음 · 단계적 활성화 정합 |

- 🟢 `.kiro/specs/_common/operator-decisions.md` (2차 · 3차 항목)

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-23 두 번째 항목 | OD-MS-030 · OD-MS-031 신규 · OD-MS-009 · OD-MS-010 · OD-MS-029 1차 실증 보강 |
  | Decision Summary 갱신 | 전체 88 → 90 / 확정 44 → 45 / 잠정 41 → 42 |
  | 2026-06-23 세 번째 항목 | OD-MS-032 신규 · OD-MS-009 · OD-MS-029 · OD-MS-031 1차 실증 보강 |
  | Decision Summary 갱신 | 전체 90 → 91 / 확정 45 → 46 / 잠정 42 유지 |

- 🟢 `.kiro/specs/_common/risk-register.md` (신규 3건)

  | ID | 내용 |
  | --- | --- |
  | R-AUTO-023 | SFN 실패 경로 Slack 누락 위험 |
  | R-AUTO-024 | Slack webhook URL Lambda 환경변수 장기 보관 secret 관리 약화 위험 |
  | R-AUTO-025 | Scheduler · Dispatcher Lambda · SFN 연결 실패 위험 · Status 🟢 **Mitigated** |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2 · 3차 후속 메모)

  | 메모 | 내용 |
  | --- | --- |
  | 2026-06-23 두 번째 (완료) | Slack notifier Lambda smoke test / template test / SFN 3종 Slack 수신 검증 |
  | 2026-06-23 두 번째 (후속) | Scheduler 정기 트리거 진입 · 장 전 · 장 후 · 장중 손절 Slack 분리 구현 · Slack webhook URL Secrets Manager · SSM Parameter Store 이전 · DLQ · retry · CloudWatch Alarm 검토 |
  | 2026-06-23 세 번째 (완료) | Scheduler 2개 생성 · Dispatcher Lambda 구현 · Scheduler → Lambda 권한 검증 · Lambda dryRun 검증 · 08:00 🟢 **ENABLED** / 09:01 🟠 **DISABLED** 유지 |
  | 2026-06-23 세 번째 (후속) | 내일 08:00 실행 결과 확인 |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-23 §9 추가 | SFN 3종 Slack 수신 검증 결과 |

- 🟢 `.kiro/README.md`

  | 항목 | 값 |
  | --- | --- |
  | 추가 항목 | "AWS 공통 Slack notifier 1차 검증 완료(2026-06-23)" + "EventBridge 자동화 리소스 구현 완료(2026-06-23)" |
  | 기준일 | 변경 없음 |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-23 섹션 bullet 누적 | Slack notifier 구현 완료 · 3종 Slack 수신 검증 완료 · EventBridge 자동화 1차 범위 3종 한정 · port-view Slack 유지 |
  | 두 번째 누적 | Scheduler 2개 생성 · Dispatcher Lambda 구현 · IAM `simulate-principal-policy` + Lambda dryRun + Scheduler `get-schedule` 상태 검증 · 08:00 ENABLED + 09:01 DISABLED 단계적 활성화 · 내일 08:00 첫 실 `StartExecution` 검증 예정 |
  | 2026-06-23 섹션 | 신규 prepend |

### Security

  | 항목 | 결과 |
  | --- | --- |
  | secret · KIS app key · secret · 계좌번호 · 비밀번호 · token · RDS password · RDS endpoint · account-id · IAM Role ARN · secret ARN · access key id · instance-id · EIP · image digest · task ARN · job ARN · broker 응답 전문 · webhook URL · KIS paper login credential · Administrator password 평문 기록 | 0건 (모두 `[REDACTED]` 또는 placeholder) |
  | DB 한글 확인 SQL 결과 | `server_encoding` · `client_encoding` 모두 `UTF8` 확인 · 테이블 4종 한글 정상 (민감정보 없이 요약) |
  | Slack webhook URL 평문 기록 | 0건 (`SLACK_WEBHOOK_URL` 은 키 이름 · 의미만 · 값 · Lambda 코드 본문 · Slack 메시지 · SFN Catch state ASL · Slack webhook 응답 인용 0건 · R-DOCS-001 정합) |
  | webhook URL 이전 계획 | 1차 검증용 한정 → Secrets Manager / SSM Parameter Store(SecureString) · R-AUTO-024 신규 · Status 🔵 **Accepted** · 06 spec 후속 · Lambda runtime 조회 · IAM Role Resource ARN 한정 read (wildcard 0건 · OD-SEC-006 정합) |
  | webhook 노출 grep 점검 | Lambda 콘솔 · `get-function-configuration` 응답 · CloudFormation · SAM · Terraform state · git history 대상 가능 · 값 설정은 운영자 직접 수행 · Kiro 는 key 이름 · 의미만 기록 |
  | `-AllowPaperOrderExecute` 사용 | SFN approval true path 승인 시점 한정 · 본 일자 `282330` SELL 17주 1건 |
  | 추가 BUY · 취소 · 정정 · `--execute` | 0건 |
  | SELL position `mark_position_sell_ordered()` 책임 | MarketConnector executor SELL 성공 시점 (OD-MS-016 정합) |
  | fill · position sync 자동 재시도 | 0건 |
  | aws-live 작업 | 0건 |
  | `DAILY_EXECUTION_FAILED` 검증 | test-only 실패 주입 · 실제 broker 주문 실패 유발 0건 (R-AUTO-023 mitigation · OD-SAFE-001 ~ 004 정합 · 추가 broker 호출 0건) |
  | AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · RDS · GRANT · KIS · SFN · S3 · CloudWatch · Lambda · Slack · Scheduler 작업 | 모두 운영자 직접 수행 · Kiro 는 루트 / `_common` / 04 spec operation-notes 문서 갱신만 |
  | AWS CLI · boto3 실행 | 0건 |
  | AWS 리소스 생성 · 수정 · 삭제 | 0건 |
  | `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
  | CloudWatch Logs · SSM 응답 · KIS API · SFN history · Slack 메시지 · Lambda 코드 · `get-schedule` · `simulate-principal-policy` · dryRun 응답 · Docker · PowerShell stdout 전문 평문 인용 | 0건 |
  | `connector_strategy_order_execute.py` 전체 교체 / retry-normalizer 본문 인용 | 0건 (R-DOCS-001 · `_common` 메타 / 03 spec 후속 갱신 책임) |
  | Lambda `portfolio-event-notifier` · Dispatcher Lambda 코드 · 환경변수 · IAM inline policy · SFN Catch ASL · Target Role · Target Lambda ARN 본문 인용 | 0건 |
  | 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 spec 영역 변경 | 0건 |

<details><summary>🔵 운영 식별자 요약</summary>

  | 항목 | 값 |
  | --- | --- |
  | SFN state machine | `portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval` |
  | `strategy_execution_order id` | `40` |
  | `connector_order_request id` | `48` |
  | `broker_order_no` | `0000006143` |
  | 종목 코드 | `282330` (종목명 · 수량 17) |
  | approval gate | true · false 라벨 |
  | DB session table name | 4종 |
  | Lambda 이름 | `portfolio-event-notifier` · `portfolio-paper-daily-scheduler-dispatcher` |
  | IAM Role | `portfolio-event-notifier-lambda-role` · `portfolio-paper-eventbridge-scheduler-role` · `portfolio-paper-daily-scheduler-dispatcher-role` |
  | Inline policy | `portfolio-paper-scheduler-start-execution-policy` · `portfolio-paper-scheduler-invoke-dispatcher-policy` · `portfolio-paper-daily-scheduler-dispatcher-policy` |
  | Scheduler | `portfolio-paper-daily-step1-11-approval-0800-kst` · `portfolio-paper-daily-step12-17-order-0901-kst` |
  | cron 표현식 | `cron(0 8 ? * MON-FRI *)` · `cron(1 9 ? * MON-FRI *)` |
  | Timezone · Runtime · Handler | `Asia/Seoul` · `Python 3.12` · `lambda_function.lambda_handler` |
  | Timeout / Memory | `30s` / `256MB` |
  | 환경변수 key | `SLACK_WEBHOOK_URL` · `TIMEZONE` · `HOLIDAY_COUNTRY` · `FAIL_CLOSED_ON_HOLIDAY_ERROR` (값 미기록) |
  | Slack 이벤트 | `APPROVAL_REQUIRED` · `DAILY_EXECUTION_SUCCESS` · `DAILY_EXECUTION_FAILED` |
  | scheduleType | `STEP1_11_APPROVAL` · `STEP12_17_ORDER` |
  | Target input JSON | `{"scheduleType":"...","dryRun":false}` |
  | 완료 marker | `SLACK_LAMBDA_SMOKE_TEST=SUCCESS` · `PORTFOLIO_EVENT_NOTIFIER_TEMPLATE_TEST=SUCCESS` |
  | dryRun 응답 | `started=false` · `reason=DRY_RUN_NO_START_EXECUTION` |
  | smoke 메시지 / 봇 이름 | `hello wook` / `Portfolio Daily Bot` |

</details>

## 2026-06-22 (Daily AWS Paper 1~17 완주 + MarketConnector env bootstrap + Step 9 권한 보정 + SELL E2E 검증)

### Added

- 🟢 **Daily AWS Paper Wrapper Step 1~17 실 운영 완주**

  | 항목 | 값 |
  | --- | --- |
  | wrapper | `.kiro/scripts/run-daily-aws-paper.ps1` |
  | 범위 | Step 1 ~ Step 17 전 단계 실 운영 |
  | 종료 상태 | 실제 Paper SELL 주문 제출 · KIS 접수 · 체결조회 · SELL fill sync · position CLOSED · balance refresh end-to-end 통과 |
  | 기록 의미 | AWS 운영 wrapper 첫 완주성 운영 결과 |
  | 환경 · RunDate · region | `aws-paper` · `2026-06-22` · `ap-northeast-2` |
  | KIS paper SELL 1건 | `088350` 한화생명 244주 MARKET |
  | `execution_order id 37` | 🟢 **SUBMITTED** |
  | `connector_order_request id 46` | 🟢 **ACCEPTED** |
  | broker 응답값 평문 기록 | 0건 (R-DOCS-001 정합) |

- 🟢 **MarketConnector env bootstrap 재생성 패턴**

  | 항목 | 값 |
  | --- | --- |
  | 대상 파일 | `daily-aws-paper.functions.ps1` (운영자 직접 patch) |
  | 함수 역할 | MarketConnector env bootstrap 신규 추가 · Secrets Manager JSON SecretString 내부 key 추출 · APP_* / KIS_* alias export · secret value 미출력 · `/tmp/inject-env.sh` chmod 700 · step 실행 시점 재생성 |
  | 호출 지점 | Step 1 · 12 · 13 · 17 공통 |
  | 검증 | PowerShell parser validation 통과 · Step 1 재실행 성공 · EC2 stop / start 후 `/tmp` 휘발 대응 |
  | 본문 인용 | 0건 (R-DOCS-001 정합 · OD-MS-027 신규 · R-AUTO-021 신규 mitigation) |

- 🟢 **Step 9 `execution_app` 권한 보정**

  | 항목 | 값 |
  | --- | --- |
  | GRANT (운영자 직접) | `GRANT USAGE ON SCHEMA decision TO execution_app` + `GRANT UPDATE ON TABLE decision.strategy_daily_position_decision TO execution_app` |
  | 권한 확인 | SELECT + UPDATE |
  | 결과 | Step 9 SELL execution_order 생성 후 daily position decision `execution_order_id` UPDATE 가능 |
  | Step 9 ~ Step 11 재실행 | 🟢 **통과** |
  | 결정 · 리스크 | OD-DB-011 신규 · R-DATA-013 신규 mitigation 1차 실증 · R-DATA-005 [2026-06-22 보강] 정합 |
  | 02 spec db-roles-and-grants 정식 매트릭스 갱신 | 후속 유지 |

- 🟢 **실제 Paper SELL 주문 E2E 검증 결과**

  | 항목 | 값 |
  | --- | --- |
  | execution plan id | `96` |
  | `plan_date` | `2026-06-22` |
  | `plan_status` | `PARTIALLY_BLOCKED` |
  | total_candidate · ready · blocked · skipped | 3 · 1 · 2 · 0 |
  | total_target_amount | `1,237,080` |
  | available_cash | `-62,763` |
  | max_order_amount | `-31,381.50` |
  | Step 12 대상 | `088350` 한화생명 SELL 244주 MARKET 1건 (BUY 2건 현금 부족 BLOCKED · 중복 주문 0 rows) |
  | 매도 사유 | `SELL_HARD_STOP` |
  | entry_date / entry_price | `2026-06-17` / `5,744.4057` |
  | current_price | `5,070` |
  | expected_pnl_rate | 약 `-11.7402%` |
  | hard_stop_loss_rate | `-10%` |
  | holding_days | 5 |
  | snapshot_qty · sellable_qty · remaining_qty | 모두 244 |
  | expected_pnl_amount | 약 `-164,554.9908` |
  | Step 13 체결조회 | connector_fill id `34` · fill_qty 244 · fill_price `5,075.8607` · fill_amount `1,238,510.01` · side SELL · fill_ts `2026-06-22 00:46:58 UTC` |
  | Step 14 · 15 · 16 ECS exitCode | 0 |
  | Step 17 SSM | 🟢 **Success** |
  | `strategy_position_state id 9` | remaining_qty 0 · position_status 🟢 **CLOSED** · latest_sell_reason `SELL_HARD_STOP` |
  | `connector_position_snapshot` 최신 created_at | `2026-06-22 00:50:50 UTC` |
  | 보유 5종목 | `003490` 58주 · `004990` 69주 · `023530` 8주 · `042660` 11주 · `282330` 17주 |
  | `088350` 한화생명 | 잔고 스냅샷에서 제거 확인 |

- 🟢 **신규 결정 · 리스크**

  | ID | Status | 메모 |
  | --- | --- | --- |
  | OD-MS-027 신규 | 🟠 **잠정** | MarketConnector env bootstrap 재생성 운영 정책 · `/tmp/inject-env.sh` 선존재 가정 폐기 · step 실행 시점 재생성 · wrapper 공통 함수 · secret value 미출력 · chmod 700 |
  | OD-DB-011 신규 | 🟢 **확정** | `execution_app` 의 `decision.strategy_daily_position_decision` 제한적 UPDATE 권한 · SELL execution link update 책임 한정 |
  | R-AUTO-021 신규 | 🟢 **Mitigated** | MC EC2 stop · start 후 `/tmp` 휘발로 Step 1 · 12 · 13 · 17 실패 위험 |
  | R-DATA-013 신규 | 🟢 **Mitigated** | `execution_app` 의 `decision` schema UPDATE 권한 누락으로 Step 9 SELL execution link update 실패 위험 |

### Changed

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-22 §1~§6 | MarketConnector env bootstrap 재생성 + Paper SELL 주문 제출 검증 |
  | (a) Step 1 최초 실패 원인 | `/tmp/inject-env.sh not found` (EC2 stop / start 후 `/tmp` 휘발 + SSM step 의 env 파일 선존재 가정) |
  | (b) 함수 추가 | `daily-aws-paper.functions.ps1` MarketConnector env bootstrap · Step 1 · 12 · 13 · 17 호출 |
  | (c) 값 처리 | JSON SecretString 내부 key 추출 · APP_* / KIS_* alias export · secret value 미출력 |
  | (d) Step 1 재실행 | 성공 |
  | (e) Step 12 | 한화생명 244주 SELL MARKET 제출 성공 · broker_order_no / broker_branch_code 생성 · 본문 broker 응답 평문 0건 · `[REDACTED]` 정합 |
  | (f) Step 13 | 체결조회 성공 + 안전 / 보안 점검 |

- 🟢 `.kiro/specs/03-marketconnector-ec2/runbook.md` §2 (env 주입)

  | 항목 | 값 |
  | --- | --- |
  | 명시 사항 | Daily wrapper 운영 시 Step 1 · 12 · 13 · 17 이 wrapper 공통 bootstrap 함수로 `/tmp/inject-env.sh` 를 step 실행 시점에 재생성 |
  | 명시 사항 (추가) | stop / start 후 `/tmp` 휘발 가능성 · `/tmp/inject-env.sh not found` 또는 env missing 시 bootstrap 함수 반영 여부 확인 · secret value 로그 미출력 · chmod 700 유지 |
  | Step 12 실 주문 | `-AllowPaperOrderExecute` 명시 시에만 허용 (safety gate 재강조) |

- 🟢 `.kiro/specs/03-marketconnector-ec2/tasks.md`

  | 항목 | 값 |
  | --- | --- |
  | task 7 · 26 · 27 | 2026-06-22 보강 메모 추가 · MC Daily wrapper SSM steps env bootstrap 재생성 반영 완료 · stop · start 후 `/tmp` 휘발 대응 완료 · Step 12 Paper SELL 제출 검증 완료 |
  | systemd / startup script 정식화 | 후속 유지 |

- 🟢 `.kiro/specs/03-marketconnector-ec2/validation-checklist.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-22 검증 누적 | MC env bootstrap 재생성 검증 · Step 1 CONNECTOR_BALANCE 재실행 성공 · Step 12 Paper SELL 제출 성공 · Step 13 체결조회 성공 · secret value 로그 미노출 |
  | 확인 주체 | 운영자 직접 통과 표기 |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-22 섹션 | Daily AWS Paper execution steps 6 ~ 17 운영 검증 |
  | Step 6 · 7 · 8 · 10 · 11 · 14 · 15 · 16 · `--execute` | strategy execution 내부 상태 갱신 1차 통과 |
  | Step 9 | 최초 권한 누락 실패 → GRANT 보정 후 통과 |
  | Step 12 ~ 17 | 실제 SELL 주문 · 체결 · 동기화 · 잔고 refresh 통과 |
  | 표현 | "Daily wrapper 기반 수동 orchestration 검증" · SFN 자체 구현 아님 · 후속 orchestration target 유지 |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-22 섹션 | Daily AWS Paper Step 2 / Step 3 재검증 |
  | Step 2 INTEREST_CRAWLER | 🟢 **성공** · non-GUI ECS exitCode 0 · Windows KRX worker SSM Success · KRX raw DB validation Success · ExpectedKrxRawDate `2026-06-19` 통과 · 2026-06-21 OD-MS-026 정합 Daily run 재검증 |
  | Step 3 PREPROCESSOR | ECS exitCode 0 |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md`

  | 항목 | 값 |
  | --- | --- |
  | task 57 · 105 · 107 · 110 | 2026-06-22 Daily run 재검증 완료 · Step 2 KRX raw DB validation · Scheduled Task wait · Last Result · worker stopped fail-closed · Step 3 Preprocessor Daily wrapper 재통과 |
  | EC2 lifecycle · CloudWatch Logs Agent · SFN hybrid orchestration · View 표시 연동 | 후속 유지 |

- 🟢 `.kiro/specs/09-strategy-research-batch/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-22 섹션 | Daily AWS Paper Step 4 / Step 5 재검증 |
  | Step 4 BACKTEST_RESEARCH AWS Batch | 🟢 **SUCCEEDED** |
  | Step 5 BACKTEST_REPORT AWS Batch | 🟢 **SUCCEEDED** |
  | 상태 | Daily wrapper 1 ~ 17 운영 중 research · report 정상 통과 · heavy 분류 SubmitJob 0건 유지 (OD-MS-019 / R-AUTO-015 정합) |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | Change Log 2026-06-22 항목 추가 | 완료 |
  | 신규 결정 | OD-MS-027 · OD-DB-011 |
  | 1차 실증 보강 | OD-MS-016 · OD-MS-021 · OD-MS-023 |
  | Decision Summary | 전체 84 → 86 / 잠정 39 → 40 / 확정 42 → 43 |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | 변경 |
  | --- | --- |
  | R-AUTO-021 신규 | 등록 |
  | R-DATA-013 신규 | 등록 |
  | R-DATA-005 detection · mitigation | [2026-06-22 보강] execution_app decision schema USAGE / table UPDATE 누락 사례 추가 · 정식 매트릭스 갱신 후속 |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-22 후속 메모)

  | 항목 | 값 |
  | --- | --- |
  | SFN 전환 시 | Step 1 · 12 · 13 · 17 MC env bootstrap 재생성 로직 포함 |
  | Step 9 권한 요구사항 | DB role bootstrap / grant 문서에 반영 |
  | Step 12 | 주문 전 DB preflight view 또는 wrapper summary 출력 보강 |
  | Step 13 | connector_fill · order_request · execution_order 상태 자동 summary 보강 |
  | Step 17 | 최신 잔고 snapshot 청산 종목 제거 확인 wrapper summary 포함 |
  | 기타 | PGPASSWORD / psql 운영 편의성 runbook 후속 |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | 대상 행 | port-interest-crawler · Strategy Decision · Execution · MarketConnector · Research Batch |
  | 추가 메모 | "2026-06-22 Daily AWS Paper 1~17 두 번째 실 완주 (첫 실제 SELL E2E)" 한 단락 |
  | 조합 | PowerShell wrapper + ECS RunTask + AWS Batch + SSM RunCommand · E2E 리허설 2회차 성공 |
  | SFN | 후속 orchestration target 유지 |
  | 결정값 (1순위 / 2순위 / 비권고) | 변경 없음 |

- 🟢 `.kiro/README.md`

  | 항목 | 값 |
  | --- | --- |
  | 기준일 | `2026-06-21` → `2026-06-22` |
  | 진행 상태 요약 03 · 04 · 08 · 09 행 + Backend AWS E2E 17-step 진행 상태 행 | 첫 실 SELL E2E 완주 · Step 9 권한 보정 · env bootstrap 재생성 |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-22 섹션 | 신규 prepend (Daily AWS Paper 1~17 완주 + MC env bootstrap + Step 9 권한 보정 + SELL E2E 검증) |

### Security

  | 항목 | 결과 |
  | --- | --- |
  | secret / KIS · 계좌 · token · RDS · account-id · IAM Role ARN · secret ARN · access key · instance-id · EIP · image digest · task ARN · job ARN · broker_order_no 원문 · broker_branch_code 원문 · KIS paper credential · Administrator password 평문 기록 | 0건 (모두 `[REDACTED]` 또는 placeholder) |
  | `-AllowPaperOrderExecute` 사용 | Step 12 단독 승인 시점 한정 · 한화생명 SELL 1건 · 운영자 직접 옵션 명시 (R-AUTO-019 mitigation · wrapper summary `PaperOrder : True` 사후 검증 가능) |
  | 추가 BUY · 취소 · 정정 · `--execute` | 0건 |
  | SELL position `mark_position_sell_ordered()` 호출 | MarketConnector executor 측 SELL 성공 시점 책임 (OD-MS-016 정합) |
  | AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · SSM Parameter Store · RDS · GRANT · KIS · S3 · CloudWatch · Docker · ECR 작업 | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 · 검증 항목 정리만 |
  | AWS CLI · boto3 실행 | 0건 |
  | AWS 리소스 생성 · 수정 · 삭제 | 0건 |
  | `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
  | CloudWatch Logs · SSM 응답 · KIS API · Docker build/push · PowerShell stdout 전문 평문 인용 | 0건 |
  | RDS DDL | 0건 |
  | RDS DML (17-step 정상 흐름 한정) | 아래 · 표기 rows 참조 |
  | · `decision.strategy_daily_signal` | 기록 |
  | · `decision.strategy_daily_run` | 기록 |
  | · `decision.strategy_daily_position_decision` | SELL HOLD 결정 + execution_order_id link UPDATE |
  | · `execution.strategy_execution_plan` | insert (`id 96`) |
  | · `execution.strategy_execution_order` | insert · update (`id 37` READY → REQUESTED → SUBMITTED → FILLED) |
  | · `execution.strategy_position_state` | update (`id 9` remaining_qty 244 → 0 / OPEN → CLOSED / latest_sell_reason `SELL_HARD_STOP`) |
  | · `connector.connector_order_request` | insert (`id 46` ACCEPTED → FILLED) |
  | · `connector.connector_order_event` | 기록 |
  | · `connector.connector_fill` | insert (`id 34`) |
  | · `connector.connector_balance_snapshot` | insert |
  | · `connector.connector_position_snapshot` | insert (보유 5종목) |
  | GRANT (운영자 직접) | `USAGE ON SCHEMA decision` + `UPDATE ON decision.strategy_daily_position_decision` to `execution_app` |
  | 운영자 patch 본문 인용 (`daily-aws-paper.functions.ps1` MC env bootstrap · GRANT SQL) | 0건 (사실만 기록) |
  | 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 spec 영역 변경 | 0건 |

<details><summary>🔵 운영 식별자 요약</summary>

  | 항목 | 값 |
  | --- | --- |
  | `execution_plan_id` | `96` |
  | execution_order id | `37` |
  | connector_order_request id | `46` |
  | connector_fill id | `34` |
  | position_state_id | `9` |
  | 종목 코드 | `088350` · `003490` · `004990` · `023530` · `042660` · `282330` |
  | data_date / signal_date · run_date | `2026-06-22` |
  | fill_ts · created_at | UTC |

</details>

## 2026-06-21 (Step 2 `INTEREST_CRAWLER` 성공판정 강화 + KRX raw DB validation 연동)

### Added

- 🟢 `port-interest-crawler/interest_krx_raw_validate_daily.py` (신규 · 운영자 직접 작성)

  | 항목 | 값 |
  | --- | --- |
  | 대상 | `interest_program_raw` · `interest_shortsell_raw` |
  | 검증 | expected trade_date 기준 row_count + max(trade_date) |
  | 실패 처리 | exit code 30 반환 |
  | 로컬 검증 | py_compile · UTF-8 read · program · shortsell · exit30 marker 통과 |
  | S3 배포 | `s3://portfolio-paper-migration-yukiever/tmp/krx/interest_krx_raw_validate_daily.py` → `C:\portfolio\port-interest-crawler\interest_krx_raw_validate_daily.py` |
  | EC2 단독 검증 | crawler_app · interest schema · search_path `interest, reference, legacy, public` |
  | interest_program_raw | expected=`2026-06-19` · max_date=`2026-06-19` · expected_count=`1` |
  | interest_shortsell_raw | expected=`2026-06-19` · max_date=`2026-06-19` · expected_count=`349` |
  | exit code | 0 |

- 🟢 `step-02-interest-crawler.ps1` KRX raw DB validation SSM step 연동

  | 항목 | 값 |
  | --- | --- |
  | SSM step | `INTEREST_CRAWLER_KRX_DB_VALIDATE` |
  | wrapper 처리 | `ExpectedKrxRawDate` (RunDate 기준 전 영업일) 계산 → Windows crawler worker EC2 SSM 호출 |
  | 호출 명령 | `load-crawler-db-env.ps1` + `venvs/interest-crawler` venv + `interest_krx_raw_validate_daily.py --expected-date <yyyy-mm-dd>` |
  | 실패 조건 | row_count 0 또는 non-zero exit → Step 2 fail |
  | step result | `KrxDbValidationCommandId` 포함 |

- 🟢 **신규 결정 · 리스크**

  | ID | Status | 메모 |
  | --- | --- | --- |
  | OD-MS-026 신규 | 🟠 **잠정** | Step 2 성공 기준 = Scheduled Task trigger 가 아니라 KRX raw DB validation · KRX GUI 경로는 Windows Administrator interactive Scheduled Task 유지 · wrapper 는 실행 · 종료 대기 · Last Result · latest log · DB validation orchestration · validation script 가 raw 최신성 검증 · worker stopped 는 fail-closed |
  | R-AUTO-020 신규 | 🟢 **Mitigated** | Scheduled Task trigger 성공만 보고 Step 2 SUCCESS 처리 시 KRX raw 미적재가 Step 3 이후로 전파될 위험 · mitigation = Chrome / chromedriver best-effort reset + Running → Ready wait + Last Result + latest log + DB validation + worker stopped fail-closed |

### Changed

- 🟢 `step-02-interest-crawler.ps1` 성공판정 강화

  | 항목 | 값 |
  | --- | --- |
  | (a) 실행 전 리셋 | Chrome / chromedriver stale process best-effort reset · reset 실패는 warning |
  | (b) 실행 경로 | Administrator interactive Scheduled Task `Portfolio-KRX-Worker-Daily` `schtasks /Run` 유지 · SSM direct python 채택 거부 · KRX GUI 의존 구조 · Windows Administrator interactive session 기준 |
  | (c) 상태 대기 | Scheduled Task Running polling 후 Ready 복귀 wait · `sawRunning` 로그 · timeout 시 Step 2 실패 |
  | (d) 결과 판정 | Last Result 0 또는 0x0 만 <span style="color:#1A7F37">**SUCCESS**</span> · trigger 성공만으로 SUCCESS 처리 금지 |
  | (e) 로그 출력 | `C:\portfolio\logs\krx_worker_daily_*.log` 최신 path · last write time · size · tail |
  | (f) worker fail-closed | `running` 아니면 즉시 실패 · instanceId · state 출력 · KRX GUI worker · DB validation 미수행 상태에서 Step 2 SUCCESS 진입 차단 · 이전 자동 skip 폐지 · R-AUTO-016 mitigation 갱신 |

- 🟢 `daily-aws-paper.functions.ps1` non-GUI ECS RunTask overrides 보강

  | 항목 | 값 |
  | --- | --- |
  | 환경변수 | `TEMP=/tmp` · `TMP=/tmp` · `PYTHONUTF8=1` · `PYTHONIOENCODING=utf-8` |
  | 공통 함수 | `Invoke-DailyAwsPaperEcsTask` `EnvironmentVariables` 파라미터 · ECS RunTask `containerOverrides.environment` 전달 |
  | 공통 함수 | `New-SsmParameterFile` `ExecutionTimeoutSeconds` 파라미터 · `Invoke-SsmCommandAndWait` `ExecutionTimeoutSeconds` 전달 |
  | 목적 | Windows / Linux 인코딩 차이 완화 · crawler 로그 · 파일 처리 UTF-8 기준 · 임시 파일 경로 명시 · KRX worker · DB validation 장시간 실행 SSM timeout 제어 |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-21 섹션 누적 | Step 2 성공판정 강화 · Chrome reset · Scheduled Task wait + Last Result · latest worker log · validation script 생성 + S3 presigned URL 배포 + EC2 단독 검증 · step-02 DB validation 연동 · worker stopped fail-closed · Step 2 단독 실행 검증 성공 |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md`

  | 항목 | 값 |
  | --- | --- |
  | task 57 (wrapper 내 DB 검증 자동 추가) | 완료 |
  | task 92 (Chrome process 잔존 정리) | 1차 완료 |
  | 신규 task 항목 | KRX GUI worker Scheduled Task wait + Last Result · KRX raw DB validation 스크립트 생성 + S3 배포 + EC2 단독 검증 · step-02 DB validation 연동 · worker stopped fail-closed · Step 2 단독 실행 검증 성공 |
  | 후속 유지 | EC2 lifecycle · SFN hybrid orchestration · CloudWatch Logs Agent · View 표시 연동 |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/design.md`

  | 항목 | 값 |
  | --- | --- |
  | Hybrid execution model Step 2 성공 조건 | non-GUI ECS exitCode 0 · Crawler Worker EC2 running · Windows Scheduled Task Running → Ready · Last Result 0 또는 0x0 · latest worker log path · tail · KRX raw DB validation 통과 |
  | 명시 사항 | worker stopped fail-closed · KRX raw validation script 가 Step 2 guard 로 사용 |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/requirements.md`

  | 항목 | 값 |
  | --- | --- |
  | Acceptance Criteria 보강 | Scheduled Task trigger 성공만으로 Step 2 성공 불가 · `ExpectedKrxRawDate` 기준 `interest_program_raw` · `interest_shortsell_raw` 검증 성공이 성공 조건 · worker stopped 는 skip 이 아니라 fail-closed |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | Change Log 2026-06-21 | 추가 |
  | OD-MS-026 | 신규 |
  | Decision Summary | 갱신 |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | 변경 |
  | --- | --- |
  | R-AUTO-020 | 신규 |
  | R-AUTO-007 / R-AUTO-016 / R-AUTO-017 | mitigation · detection 보강 |
  | R-AUTO-016 "wrapper 안에서 자동 skip" 표현 | 본 일자에 fail-closed 로 갱신 |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-21 후속 메모)

  | 구분 | 항목 |
  | --- | --- |
  | 완료 | Step 2 성공판정 강화 · KRX raw DB validation wrapper 연동 |
  | 후속 유지 | EC2 lifecycle 자동 start · stop · SFN 혼합 orchestration · View 표시 연동 · worker log centralized collection |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | port-interest-crawler 행 메모 추가 | Step 2 성공 조건 (non-GUI ECS exitCode 0 + KRX GUI worker Scheduled Task wait + Last Result + latest log + KRX raw DB validation) + worker stopped fail-closed |
  | 결정값 (1순위 / 2순위 / 비권고) | 변경 없음 |

- 🟢 `.kiro/README.md`

  | 항목 | 값 |
  | --- | --- |
  | "Windows KRX crawler worker 와 Step 2 동작" 섹션 | 이전 "wrapper 안에서 KRX GUI Scheduled Task trigger 를 자동 skip 한다" 표현 폐지 · 현재 fail-closed |
  | Step 2 단독 실행 예시 | `.\run-daily-aws-paper.ps1 -Environment aws-paper -RunDate 2026-06-20 -StartStep 2 -EndStep 2` |
  | Step 2 성공 조건 명시 | non-GUI ECS exitCode 0 · Crawler Worker EC2 running · Scheduled Task Running → Ready · Last Result 0 · latest worker log · KRX raw DB validation |
  | "현재 진행 상태 요약" 08 행 | 갱신 |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-21 섹션 | 신규 누적 (Step 2 성공판정 강화 + KRX raw DB validation 연동) |

### Security

  | 항목 | 결과 |
  | --- | --- |
  | secret / KIS · 계좌 · token · RDS · account-id · IAM Role ARN · secret ARN · access key · EIP · image digest · task ARN · job ARN · KIS paper credential · Administrator password · S3 presigned URL 실값 평문 기록 | 0건 (모두 `[REDACTED]` 또는 placeholder) |
  | broker / KIS 호출 · 신규 BUY / SELL / 취소 / 정정 / `--execute` | 0건 |
  | SELL position `mark_position_sell_ordered()` 호출 | 0건 |
  | fill · position sync 자동 재시도 · aws-live 작업 | 0건 (본 일자는 `aws-paper` 한정) |
  | AWS · SSM · EC2 · S3 · ECS · RDS · KRX 호출 | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 · 검증 항목 정리만 |
  | AWS CLI · boto3 실행 · AWS 리소스 생성 · 수정 · 삭제 | 0건 |
  | `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
  | CloudWatch Logs · SSM 응답 · PowerShell stdout 전문 평문 인용 | 0건 |
  | RDS DDL | 0건 |
  | RDS DML | 검증 SQL SELECT 한정 · `interest_program_raw` · `interest_shortsell_raw` 신규 row 는 KRX worker 적재 결과 정합 · 본 일자 신규 Step 2 wrapper 실행에서는 idempotent · no-op |
  | 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 패키징 spec 영역 변경 | 0건 |
  | 운영자 patch 본문 인용 (`interest_krx_raw_validate_daily.py` · `step-02-interest-crawler.ps1` · `daily-aws-paper.functions.ps1`) | 0건 (R-DOCS-001 정합 · 사실만 기록) |

<details><summary>🔵 운영 식별자 요약</summary>

  | 항목 | 값 |
  | --- | --- |
  | SSM commandId (4건) | `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3` · `c844aea5-1429-430a-9510-39fc99f17f05` · `2279c6d7-2da6-4317-9c10-7cc77374b317` · `f9d82fcc-1e26-4710-87c3-1d20483b63ef` |
  | RunId | `daily-aws-paper-20260621-204017` |
  | taskDefinition | `portfolio-paper-interest-crawler:7` |
  | taskId | `78979b5cbb714d0eb94f5946e15a14ce` |
  | S3 key | `tmp/krx/interest_krx_raw_validate_daily.py` |
  | Scheduled Task | `Portfolio-KRX-Worker-Daily` |
  | latest worker log | `krx_worker_daily_20260621_114154.log` |
  | wrapper 옵션 | `-StartStep` · `-EndStep` |
  | DB session | user · search_path · row_count |

</details>

## 2026-06-20 (AWS 자동 Wrapper 최종 확인 + 6/18 중복 실행 시도 안전 중단 + EC2 lifecycle 후속 필요성)

### Changed

- 🟢 **문서 갱신**

  | 대상 | 내용 |
  | --- | --- |
  | `.kiro/WORKLOG.md` 2026-06-20 섹션 | 누적 |
  | `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` 2026-06-20 | 누적 |
  | `.kiro/specs/_common/followups-overview.md` 2026-06-20 후속 메모 | EC2 lifecycle 자동 start / stop 보강 · MC EC2 stop · start 후 `/tmp/inject-env.sh` 유실 대응 · Step 12 주문 제출 별도 승인 전까지 금지 |

- 🟢 **Daily AWS Paper Wrapper 구조 · 안전 기준 · EC2 기동 기준 최종 점검**

  | 항목 | 값 |
  | --- | --- |
  | wrapper 구조 | `run-daily-aws-paper.ps1` + `daily-aws-paper.config.ps1` + `daily-aws-paper.functions.ps1` + `steps/step-01 ~ step-17` |
  | 안전 기준 | Step 1 ~ Step 11 broker · KIS 주문 제출 전 · Step 12 만 실제 KIS paper 주문 제출 · `-AllowPaperOrderExecute` 미명시 시 차단 · 토 · 휴장일 Step 12 제외 |
  | EC2 기동 기준 | MarketConnector EC2 = Step 1 · 12 · 13 · 17 필요 · Crawler Worker EC2 = Step 2 KRX GUI worker 필요 · 두 EC2 stopped 시 wrapper 실행 전 start 필요 · EC2 stop · start 후 MC `/tmp/inject-env.sh` 유실 가능 · lifecycle 보강 후속 |

- 🟢 **6/18 wrapper 중복 실행 시도 안전 중단**

  | 항목 | 값 |
  | --- | --- |
  | RunDate | `2026-06-18` |
  | 범위 | Step 1 ~ Step 11 재실행 시도 |
  | Step 1 최초 실패 | `/tmp/inject-env.sh` 유실 → 재생성 후 통과 |
  | Step 2 | non-GUI ECS exitCode 0 · KRX worker Scheduled Task trigger 성공 |
  | Step 3 | PREPROCESSOR 진입 직후 Ctrl+C 로 중단 |
  | 재실행 후 상태 | ECS RUNNING task 0건 · AWS Batch RUNNING · SUBMITTED · PENDING · RUNNABLE 0건 · Scheduled Task State `Ready` · LastTaskResult 0 · Step 12 미실행 |
  | 신규 DB row | execution_plan 0건 · strategy_execution_order 0건 · connector_order_request 0건 · KIS 신규 주문 제출 0건 |
  | 기존 이력 | `execution_plan_id 94` BUY 4건 🟢 **FILLED** 4건 · connector linked 4건 정상 완료 |
  | 종료 처리 | Crawler Worker · MarketConnector EC2 stop |

- 🟢 **6/19 KRX raw 최신성 복구 상태**

  | 테이블 | 값 |
  | --- | --- |
  | `interest_program_raw` max_date | `2026-06-19` |
  | `interest_program_raw` 2026-06-18 row_count | `1` |
  | `interest_program_raw` 2026-06-19 row_count | `1` |
  | `interest_shortsell_raw` max_date | `2026-06-19` |
  | `interest_shortsell_raw` 2026-06-18 row_count | `349` |
  | `interest_shortsell_raw` 2026-06-19 row_count | `349` |
  | 결론 | KRX raw 기준 최신성 복구 완료 |
  | 후속 | Scheduled Task trigger / LASTEXITCODE 중심 성공판정 한계 식별 → Step 2 wrapper 성공판정 강화 2026-06-21 분리 |

### Security

  | 항목 | 결과 |
  | --- | --- |
  | secret · 계좌 · token · RDS · account-id · ARN · access key · EIP · image digest · task ARN · job ARN · KIS paper credential · Administrator password 평문 기록 | 0건 (모두 `[REDACTED]` 또는 placeholder) |
  | Step 12 미실행 · broker · KIS 신규 주문 제출 · aws-live 작업 · fill · position sync 자동 재시도 · `mark_position_sell_ordered()` 호출 | 0건 (본 일자는 `aws-paper` 한정) |
  | AWS · SSM · EC2 · RDS 호출 | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 정리만 |
  | AWS CLI · boto3 실행 · AWS 리소스 생성 · 수정 · 삭제 | 0건 |
  | RDS DDL · DML | 0건 (SELECT 한정) |
  | 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 spec 영역 변경 | 0건 |

<details><summary>🔵 운영 식별자 요약</summary>

  | 항목 | 값 |
  | --- | --- |
  | `execution_plan_id` | `94` |
  | RunDate | `2026-06-18` |
  | 검증 대상 | `interest_program_raw` · `interest_shortsell_raw` row_count · max_date |

</details>

## 2026-06-18 (Step 13 `connector_order_check.py` 단건 순차 조회 기본화 — 운영 안전성 강화)

### Changed

- 🟢 `port-marketconnector/connector_order_check.py` (운영자 직접 patch)

  | 항목 | 값 |
  | --- | --- |
  | 기본 모드 변경 | broad 체결조회 일괄 → active 주문 단건 순차 조회 |
  | local commit | `75cb804` (remote push destination 미설정 · git push 미수행 · 후속 분리) |
  | EC2 반영 | S3 경유 |
  | 본문 인용 | 0건 (R-DOCS-001 정합) |
  | 신규 옵션 | `--broad` (legacy broad 모드 · 기본 미사용) · `--active-limit` (단건 순차 조회 최대 건수) |
  | 명시 주문 조회 | `--code {ticker_code} --order-no {broker_order_no} --no-broad` (broad fallback 없이) |
  | 안전 기준 | 다건 active 상태에서 `output2 summary-only` DB 반영 근거 사용 금지 · `connector_order_request` 후보가 주문번호 / 종목코드 기준 1건 확정 시에만 summary fallback 허용 · `connector_order_event` · `connector_fill` 오매핑 방지 (R-AUTO-018 [2026-06-18 추가 보강] mitigation 정합) |

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md` (2026-06-18 §1~§6)

  | 섹션 | 내용 |
  | --- | --- |
  | (a) 배경 | KIS paper 체결조회 응답 패턴 (`output1 empty` + `output2 summary-only`) 운영 안전성 강화 · 운영 실패 아님 · 설계 변경 · 잘못된 connector_fill 이 Step 15 / 16 으로 전파 차단 |
  | (b) 변경 내용 | 기본 실행 모드 변경 · `--broad` · `--active-limit` 옵션 추가 · 명시 주문 조회 조합 · wrapper ps1 은 실행 orchestration 만 · 체결조회 방식 제어는 `connector_order_check.py` 내부 (OD-MS-025 신규 정합) |
  | (c) 로컬 검증 | `python -m py_compile` 통과 · `--help` 옵션 확인 · commit `75cb804` |
  | (d) S3 업로드 + EC2 반영 | bucket `portfolio-paper-migration-yukiever` · key `deploy/marketconnector/connector_order_check.py` · `39159 bytes` · EC2 backup `connector_order_check.py.bak-20260618-step13-per-order` · 소유권 `ec2-user:ec2-user` 복구 |
  | (e) EC2 검증 + 단건 direct-only 조회 | `python3 connector_order_check.py --code 004990 --order-no 0000025576 --no-broad` · SSM Status 🟢 **Success** · ResponseCode `0` · StdErr empty |
  | (f) wrapper Step 13 단독 실행 | `.\run-daily-aws-paper.ps1 -Environment aws-paper -RunDate 2026-06-18 -StartStep 13 -EndStep 13` (`FromStep` / `ToStep` 아님) · SSM commandId `b344d404-07a4-4bb6-9d63-34151e648bab` · status 🟢 **Success** · responseCode `0` · Step 13 🟢 **COMPLETED** |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | Change Log 2026-06-18 두 번째 항목 | 추가 |
  | OD-MS-025 신규 | <span style="color:#BF8700">**잠정**</span> · `connector_order_check.py` 운영 모드 = active 주문 단건 순차 조회 기본 · broad 일괄 조회는 legacy · `--broad` 명시 시만 · 명시 주문 조회는 `--code` · `--order-no` · `--no-broad` · 다건 active 시 summary fallback DB 반영 금지 · 단건 후보 확정 시만 허용 · wrapper ps1 은 orchestration 만 · 체결조회 방식 제어는 `connector_order_check.py` 내부 · OD-MS-016 · OD-MS-021 · OD-MS-023 본문 유지 |
  | Decision Summary | 전체 82 → 83 / 잠정 37 → 38 |
  | OD-MS-016 · OD-MS-021 · OD-MS-023 | 본문 변경 없이 1차 실증 메모 보강 |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | 변경 |
  | --- | --- |
  | R-AUTO-018 detection / mitigation | [2026-06-18 추가 보강] `connector_order_check.py` 기본 실행 모드가 단건 순차 조회로 변경 · broad 호출 빈도 감소 · summary fallback 노출 표면 감소 · Status 🟢 **Mitigated** 유지 · 단건 direct-only 조회 + wrapper Step 13 단독 실행 모두 통과 |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-18 두 번째 후속 메모)

  | 후속 항목 | 값 |
  | --- | --- |
  | Git remote push destination 설정 | commit `75cb804` push |
  | Step 13 summary 출력 보강 | active_order_count · single_check_success_count · single_check_failed_count · broad_mode_used |
  | View Daily Batch 화면 연동 | Step 13 결과를 주문별 표시 |
  | Windows PowerShell · AWS CLI SSM 이모지 StdOut | cp949 인코딩 오류 회피 패턴 정리 |

- 🟢 `.kiro/README.md`

  | 항목 | 값 |
  | --- | --- |
  | "운영자 로컬 PowerShell wrapper" 섹션 | **Step 13 안전 주의사항** 신규 항목 · active 주문 단건 순차 조회 기본 · `--broad` legacy · 명시 주문 조회는 `--code` · `--order-no` · `--no-broad` · wrapper ps1 은 orchestration 만 · 체결조회 방식 제어는 `connector_order_check.py` 내부 |
  | 주요 옵션 `-StartStep` / `-EndStep` 설명 | `-FromStep` / `-ToStep` 은 미존재 파라미터 · 실수 입력 시 default `-StartStep 1 -EndStep 17` 로 동작할 위험 |
  | "현재 진행 상태 요약" 03 행 | 본 일자 두 번째 세션 결과 한 줄 보강 |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-18 섹션 | 신규 누적 (Step 13 단건 순차 조회 기본화 — 운영 안전성 강화) |

### Security

  | 항목 | 결과 |
  | --- | --- |
  | secret · KIS · 계좌 · token · RDS · account-id · ARN · access key · EIP · image digest · task ARN · job ARN · KIS paper credential 평문 기록 | 0건 (모두 `[REDACTED]` 또는 `<account-id>` · `<region>` · `<rds-endpoint>` · `<role-arn>` · `<secret-arn>`) |
  | `port-marketconnector/connector_order_check.py` patch 본문 인용 | 0건 (R-DOCS-001 정합 · 사실만 기록) |
  | broker / KIS 호출 | 단건 direct-only 조회 1건 (`004990` · broker_order_no `0000025576`) + wrapper Step 13 단독 실행 1건 (모두 조회성) |
  | 신규 BUY · SELL · 취소 · 정정 · `--execute` | 0건 |
  | `mark_position_sell_ordered()` · fill · position sync 자동 재시도 | 0건 |
  | 본 세션 환경 | `aws-paper` 한정 · aws-live 작업 0건 |
  | AWS · SSM · EC2 · S3 · IAM · Secrets Manager · SSM Parameter Store · RDS · KIS | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 · 검증 항목 정리만 |
  | AWS CLI · boto3 실행 · AWS 리소스 생성 · 수정 · 삭제 | 0건 |
  | `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
  | CloudWatch Logs · SSM 응답 · KIS API response body · PowerShell stdout 전문 평문 인용 | 0건 |
  | RDS DDL | 0건 |
  | RDS DML | Step 13 단건 direct-only 조회 정상 흐름 한정 · `connector.connector_order_event` · `connector.connector_fill` 신규 row 는 broker 응답값 정합 · 단건 direct-only 1회 + wrapper Step 13 단독 1회 · 잘못된 연관 row 0건 |
  | 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 패키징 spec 영역 변경 | 0건 |

<details><summary>🔵 운영 식별자 요약</summary>

  | 항목 | 값 |
  | --- | --- |
  | commit | `75cb804` |
  | S3 bucket · key · 파일 크기 | `portfolio-paper-migration-yukiever` · `deploy/marketconnector/connector_order_check.py` · `39159 bytes` |
  | EC2 instance id | `i-0fce77927b7397b88` |
  | EC2 backup 파일명 | `connector_order_check.py.bak-20260618-step13-per-order` |
  | SSM commandId | `b344d404-07a4-4bb6-9d63-34151e648bab` |
  | 종목 코드 | `004990` |
  | broker_order_no | `0000025576` |
  | wrapper 옵션 / 파라미터 | `-StartStep` · `-EndStep` |

</details>

## 2026-06-18 (Daily AWS Paper Wrapper 17단계 실운영 검증 완료)

### Changed

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md` (2026-06-18 §1~§4)

  | 섹션 | 내용 |
  | --- | --- |
  | (a) Step 1 CONNECTOR_BALANCE | 운영자 직접 SSM RunCommand 🟢 **통과** |
  | (b) Step 12 MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE (`-AllowPaperOrderExecute` 명시) | 1차 KIS paper API read timeout · connector_order_request id `38~41` <span style="color:#D1242F">**FAILED**</span> · strategy_execution_order id `30~33` <span style="color:#D1242F">**FAILED**</span> · 네트워크·DNS·TCP·HTTPS 정상 → KIS paper endpoint 일시 지연성 장애 판단 |
  | (b) 통제된 REQUESTED 복구 | `connector_order_request` · `connector_api_call_log` · `broker_order_no` 존재 여부 사전 점검 후 운영자 직접 작업 |
  | (b) 재시도 KIS paper BUY 4건 제출 | connector_order_request id `42~45` 🟢 **ACCEPTED** · broker_order_no `0000025576` 004990 · `0000025740` 023530 · `0000025744` 003490 · `0000025747` 042660 · strategy_execution_order id `30~33` 🟢 **SUBMITTED** |
  | (c) Step 13 CONNECTOR_ORDER_CHECK | broad 응답 `output1 empty` + `output2 summary-only` · active candidate 4건 · summary fallback guard 자동 skip (R-AUTO-018 mitigation 1차 실증) → `--code` · `--order-no` · `--no-broad` 단건 재조회로 4건 모두 체결 반영 · 4주 · 8주 · 6주 · 11주 |
  | (d) Step 17 BALANCE_REFRESH | SSM commandId `66ec8831-74b9-469c-8410-6ccb11cb3400`<br>responseCode 0<br>Status Success<br>`connector.connector_position_snapshot` row_count `36`<br>max_created_at `2026-06-18 04:33:07.456056+00`<br>`legacy.holdings` row_count `41`<br>max_created_at `2026-06-18 04:33:07.420226`<br>view_app 의 legacy schema SELECT 권한 부재 → `portfolio_admin` 우회 (R-DATA-005 보강<br>followups 후속) |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (2026-06-18 §1~§6)

  | Step | 결과 |
  | --- | --- |
  | Step 6 DAILY_BUY_SIGNAL | BUY READY 4건 · `004990` 롯데지주 · `023530` 롯데쇼핑 · `003490` 대한항공 · `042660` 한화오션 |
  | Step 7 DAILY_POSITION_SIGNAL | 보유 종목 HOLD decision 정상 생성 |
  | Step 8 DAILY_BUY_EXECUTION | `execution_plan_id 94` · BUY READY 4건 · blocked 0 · skipped 0 · 추가매수 허용으로 기존 보유 (`004990` · `003490`) 도 BUY 후보 · 004990 4주 · 023530 8주 · 003490 6주 · 042660 11주 |
  | Step 9 DAILY_SELL_EXECUTION | 정상 skip (SELL 대상 없음 · exitCode 0) |
  | Step 10 DAILY_AUTO_SELL | 정상 skip (READY SELL 없음 · exitCode 0) |
  | Step 11 DAILY_AUTO_BUY | BUY READY 4건 → 🟢 **REQUESTED** |
  | Step 14 SYNC_SELL_FILL | 정상 skip (SELL 체결 대상 없음) |
  | Step 15 SYNC_BUY_FILL | connector_fill 기준 strategy_execution_order id `30~33` 🟢 **FILLED** · `result_payload.sync_result` 생성 |
  | Step 16 SYNC_BUY_POSITION | 아래 4단계 phase 참조 |
  | · phase 1 충돌 | 1차 추가매수 unique constraint 충돌 (R-DATA-012 신규 정합) |
  | · phase 2 패치 | `execution_sync_buy_position.py` — `account_id` · `ticker_code` 기준 OPEN 조회 · 기존 OPEN 시 `merge_open_position_state()` · `buy_info.additional_buys` 이력 · `execution_order_id` · `connector_order_request_id` 기준 중복 방지 |
  | · phase 3 rebuild | Docker rebuild (`portfolio-strategy-execution:paper-20260613`) + ECR push (image digest 운영자 보관 · 평문 0건) |
  | · phase 4 재실행 | ECS task 재실행 exitCode 0 · 🟢 **SUCCESS** · position_sync_result 생성 |
  | Step 16 포지션 반영 | 004990 65 → 69주 (`position_state_id 7` 유지 · additional_buys 30) · 003490 52 → 58주 (`position_state_id 8` 유지 · additional_buys 32) · 023530 신규 `position_state_id 11` 8주 entry_price `194225.0000` · 042660 신규 `position_state_id 12` 11주 entry_price `126118.1818` |
  | Docker build 주의 | Dockerfile `port_strategy_execution/...` COPY → `C:\Workspaces` 기준 build · PowerShell pipe 기반 `docker login` 400 시 `cmd /c` pipe 우회 |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | Change Log 2026-06-18 | 추가 |
  | OD-MS-024 신규 | 🟠 **잠정** · 추가매수 허용 정책 · 기존 OPEN 시 신규 INSERT 아닌 `merge_open_position_state()` · `buy_info.additional_buys` 이력 · idempotency · `port_strategy_execution/execution_sync_buy_position.py` 패치 · OD-MS-016 · OD-MS-017 정합 유지 |
  | Decision Summary | 전체 81 → 82 / 잠정 36 → 37 |
  | 1차 실증 메모 보강 | OD-MS-016 (Step 11 · 12 · 15 · 16 end-to-end 실증) · OD-MS-021 (wrapper 17단계 안전 기준) · OD-MS-023 (wrapper 실 실행 통과 · Step 12 첫 사용) |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | 변경 |
  | --- | --- |
  | R-BROKER-004 신규 | KIS paper API read timeout 시 단순 재실행 broker 중복 주문 위험 · mitigation = 사전 점검 후 통제된 REQUESTED 복구 · 1차 실증 · Status 🟢 **Mitigated** |
  | R-DATA-012 신규 | 추가매수 시 strategy_position_state `unique(account_id, ticker_code, status='OPEN')` 충돌 · mitigation = `execution_sync_buy_position.py` merge 패치 + idempotency · Status 🟢 **Mitigated** |
  | R-AUTO-018 detection / mitigation | [2026-06-18 보강] wrapper Step 13 broad 조회에서 summary fallback 자동 skip 1차 실증 · 단건 `--code` · `--order-no` · `--no-broad` 조회로만 fallback 허용 |
  | R-AUTO-009 · R-AUTO-010 · R-AUTO-011 · R-AUTO-019 | Step 12 `-AllowPaperOrderExecute` 첫 사용 · 1차 timeout → 통제된 복구 → KIS paper BUY 4건 end-to-end 통과 · Status 🟢 **Mitigated** 유지 · live cutover 전 평일 · 안전 데이터 추가 검증 후속 |
  | R-DATA-005 detection / mitigation | [2026-06-18 보강] view_app 의 `legacy.holdings` SELECT 권한 부재 · Step 17 결과 확인 시 `portfolio_admin` 우회 · 정식 GRANT 후속 (02 spec) |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-18 후속 메모)

  | 후속 항목 | 값 |
  | --- | --- |
  | Step 13 단건 체결조회 | wrapper 자동화 보완 |
  | KIS API timeout 재시도 정책 | 명문화 |
  | Step 16 추가매수 merge 패치 | 정식 commit + 07 spec CI/CD 연동 |
  | view_app `legacy.holdings` SELECT 권한 | 02 spec db-roles-and-grants 후속 |
  | wrapper run summary 출력 | 보강 |
  | View Daily Batch 화면 연동 전 | 최종 운영 summary 개선 |
  | bundled wrapper | 생성 여부 결정 |
  | 기타 | EventBridge 정기 트리거 · SFN 이전 · aws-live cutover |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | 결정값 (컴퓨트 1순위 / 2순위 / 비권고) | 변경 없음 |
  | 1차 실증 메모 보강 | MC (EC2) Step 12·13·17 · Strategy Execution (ECS Fargate + command override) Step 15·16 · Strategy Decision (ECS Fargate) Step 6·7 · Strategy Research (AWS Batch) Step 4·5 · Interest Crawler (hybrid) Step 2 · Preprocessor (ECS Fargate) Step 3 |
  | 상태 | wrapper 기준 17 step E2E 운영 검증 완료 · Daily 본 실행 · 실제 paper 주문 4건 · 체결 · sync · balance refresh 정합 |

- 🟢 `.kiro/README.md`

  | 항목 | 값 |
  | --- | --- |
  | "현재 진행 상태 요약" | 기준일 2026-06-18 · wrapper 1~17 실 실행 완료 · Step 12 첫 사용 · 운영 예외 3종 식별·복구 |
  | "운영자 로컬 PowerShell wrapper" | "Step 12 안전 주의사항" 에 본 일자 첫 `-AllowPaperOrderExecute` 사용 1차 실증 메모 보강 |
  | bundled wrapper 미생성 정책 | 유지 |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-18 섹션 | 신규 누적 (Daily AWS Paper Wrapper 17단계 실운영 검증 완료) |

### Security

  | 항목 | 결과 |
  | --- | --- |
  | secret · KIS · 계좌 · token · RDS · account-id · IAM Role ARN · secret ARN · access key · instance-id · EIP · image digest · task ARN · job ARN 평문 기록 | 0건 (모두 `[REDACTED]` 또는 `<account-id>` · `<region>` · `<rds-endpoint>` · `<role-arn>` · `<secret-arn>` · `<instance-id>` · `<eip>` · `<image-digest>` · `<task-arn>` · `<job-arn>`) |
  | ECR push image digest | 운영자 보관 · spec 산출물 평문 금지 (R-DOCS-001 정합) |
  | broker / KIS 호출 | KIS paper BUY 4건 (Step 12 본 실행) + balance · order check 조회성 |
  | SELL · 취소 · 정정 · 추가 `--execute` · `mark_position_sell_ordered()` | 0건 |
  | fill · position sync 자동 재시도 | 0건 (운영자 직접 patch 후 재실행) |
  | 본 세션 환경 | `aws-paper` 한정 · aws-live 작업 0건 |
  | KIS paper `broker_order_no` (`0000025576` · `0000025740` · `0000025744` · `0000025747`) | broker 응답값 운영 식별자 · 실계좌 아님 |
  | AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · SSM Parameter Store · RDS · GRANT · KIS · S3 · CloudWatch · Docker · ECR | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 · 검증 정리만 |
  | AWS CLI · boto3 · AWS 리소스 생성 · 수정 · 삭제 | 0건 |
  | `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
  | CloudWatch Logs · SSM 응답 · KIS API · Docker build · push · PowerShell stdout 전문 평문 인용 | 0건 |
  | RDS DDL | 0건 |
  | RDS DML (17-step 정상 흐름) | 아래 · 표기 rows 참조 |
  | · `decision.strategy_daily_signal` | 기록 |
  | · `decision.strategy_daily_run` | 기록 |
  | · `decision.strategy_daily_position_decision` | HOLD |
  | · `execution.strategy_execution_plan` | insert (`id 94`) |
  | · `execution.strategy_execution_order` | insert · update (BUY READY 4건 → REQUESTED → 1차 FAILED → 2차 SUBMITTED → FILLED) |
  | · `execution.strategy_position_state` | insert · merge (추가매수 merge 2건 + 신규 INSERT 2건) |
  | · `connector.connector_order_request` | insert (id `38~41` FAILED + `42~45` ACCEPTED) |
  | · `connector.connector_order_event` | 기록 |
  | · `connector.connector_fill` | insert (체결 4건) |
  | · `connector.connector_balance_snapshot` | insert |
  | · `connector.connector_position_snapshot` | insert (row_count 36) |
  | · `legacy.holdings` | insert (row_count 41) |
  | `port_strategy_execution/execution_sync_buy_position.py` 변경분 본문 인용 | 0건 (04 spec operation-notes 사실만 기록) |
  | 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 spec 영역 변경 | 0건 |

<details><summary>🔵 운영 식별자 요약</summary>

  | 항목 | 값 |
  | --- | --- |
  | `execution_plan_id` | `94` |
  | connector_order_request id | `38~41` (FAILED) + `42~45` (ACCEPTED) |
  | strategy_execution_order id | `30~33` |
  | broker_order_no | `0000025576` · `0000025740` · `0000025744` · `0000025747` |
  | position_state_id | `7` · `8` · `11` · `12` |
  | SSM commandId | `66ec8831-74b9-469c-8410-6ccb11cb3400` |
  | `connector.connector_position_snapshot` row_count | `36` |
  | `legacy.holdings` row_count | `41` |
  | 종목 코드 | `004990` · `023530` · `003490` · `042660` · `088350` · `282330` |
  | data_date | `2026-06-17` |
  | signal_date · run_date | `2026-06-18` |

</details>

## 2026-06-17 (Daily AWS PowerShell wrapper 구현)

### Added

- 🟢 `.kiro/scripts/` (신규 · 운영자 로컬 Windows PowerShell wrapper 구조)

  | 항목 | 값 |
  | --- | --- |
  | 위치 | 운영자 로컬 도구 · Kiro spec 산출물 외부 · 8개 MS 소스와 별개 |
  | main wrapper | `run-daily-aws-paper.ps1` · 파라미터 `RunDate` · `Region` · `Environment` · `StartStep` · `EndStep` · `DryRun` · `AllowPaperOrderExecute` · Step 12 PAPER_ORDER_GATE 중앙 차단 · summary 생성 |
  | config | `daily-aws-paper.config.ps1` · region · cluster · instance id · subnet · SG · task definition · job definition · log group · output path 관리 |
  | functions | `daily-aws-paper.functions.ps1`<br>Step registry<br>SSM 공통 (`AWS-RunShellScript` Linux + `AWS-RunPowerShellScript` Windows)<br>ECS RunTask 공통 (UTF-8 no BOM JSON `--overrides file://...`)<br>AWS Batch SubmitJob 공통<br>CloudWatch log 수집<br>SSM stdout<br>stderr 저장<br>summary 기록<br>성공·실패·blocker 판정<br>PowerShell UTF-8 보정 |
  | step 파일 | `steps/step-01-connector-balance.ps1` ~ `steps/step-17-balance-refresh.ps1` (17개) |
  | bundled wrapper | `run-daily-aws-paper-bundled.ps1` 본 일자 미생성 · 후속 선택 작업 |

- 🟢 **신규 결정 · 리스크**

  | ID | Status | 메모 |
  | --- | --- | --- |
  | OD-MS-023 신규 | 🟠 **잠정** | Daily AWS wrapper 정책 · Windows PowerShell 기준 · 분리 파일 구조 · bundled wrapper 필요 시 · Step 12 는 `-AllowPaperOrderExecute` 없이 차단 · 완전 자동화 이전 단계별 확인 가능한 CLI 기준선 · 환경 입력 `aws-paper` 만 · wrapper 자체는 EC2 · ECS · Batch 내부 실행 대상 아님 |
  | R-AUTO-019 신규 | 🟢 **Mitigated** | wrapper 기반 Step 12 의도하지 않은 `-AllowPaperOrderExecute` 사용 시 실 KIS paper 주문 제출 위험<br>mitigation = 중앙 PAPER_ORDER_GATE + Step 12 내부 이중 gate + 옵션 명시<br>default OFF<br>사용 시 운영자 노트 사전 기록 권고<br>detection = summary `PaperOrder : True`<br>Step 12 옵션 사용 빈도<br>`connector_order_request` insert audit<br>rollback = Step 12 즉시 중단<br>SSM RunCommand stop<br>broker 취소 |

### Changed

- 🟢 `.kiro/README.md`

  | 항목 | 값 |
  | --- | --- |
  | "운영자 로컬 PowerShell wrapper" 섹션 | 신규 추가 · 기본 경로 `.kiro/scripts/` · 주요 옵션 `-DryRun` · `-StartStep` · `-EndStep` · `-AllowPaperOrderExecute` · Step 12 안전 주의사항 · bundled wrapper 미생성 · 환경 입력 `aws-paper` 만 |
  | "현재 진행 상태 요약" 03 / 진행 상태 행 | wrapper 기준선 수립 1차 milestone 메모 보강 |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | Change Log 2026-06-17 세 번째 항목 | 추가 |
  | OD-MS-023 신규 | 컴퓨트 / 서비스 placement Compute Decisions 안에 Daily Batch orchestration 후속 결정으로 분류 · OD-MS-009 · OD-MS-021 의 운영자 도구 1단계 |
  | OD-MS-009 · OD-MS-021 | 본문 변경 없이 1차 실증 메모 보강 · wrapper 가 같은 17 step 매핑 · 같은 안전 기준 (BUY · SELL 실행 금지 · `--execute` 계열 차단 · aws-live 금지 · Research → Decision · Connector Balance 1번 · Balance Refresh 17번) 을 코드 레벨에서 강제 |
  | OD-SAFE-002 · OD-SAFE-003 · OD-SAFE-004 | 1차 실증 메모 보강 · Step 12 PAPER_ORDER_GATE 차단 · Step 10 · Step 11 `--execute` 는 strategy execution 내부 상태 갱신 (broker / KIS 직접 제출 아님) · 신규 자동 retry 미도입 |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | 변경 |
  | --- | --- |
  | R-AUTO-019 신규 | Step 12 wrapper gate 우회 위험 · Status 🟢 **Mitigated** |
  | R-AUTO-016 detection / mitigation | [2026-06-17 wrapper 보강] wrapper Step 2 가 Windows KRX crawler worker EC2 state `running` 사전 점검 · `running` 아니면 KRX GUI Scheduled Task trigger 자동 skip 1차 실증 |
  | R-AUTO-002 mitigation | [2026-06-17 wrapper 보강] wrapper 환경 입력은 `aws-paper` 만 · aws-live 분기 자체가 코드 레벨에서 미존재 |
  | R-DOCS-001 detection | [2026-06-17 wrapper 보강] wrapper summary · overrides JSON · SSM stdout · stderr 파일 secret 평문 출력 0건 점검 · `/tmp/inject-env.sh` v5 env injection 만 사용 |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-17 세 번째 후속 메모)

  | 후속 작업 | 값 |
  | --- | --- |
  | bundled wrapper | 생성 여부 결정 |
  | Step 1~7 safe subset | 실제 실행 검증 |
  | Step 8~11 execution-side | 상태 생성·갱신 구간 별도 검증 |
  | Step 12 실 주문 제출 | 운영자 확인 후 `-AllowPaperOrderExecute` 명시로만 |
  | Step 13~17 | 주문 / 체결 결과 확인 후 연계 |
  | Windows KRX worker stopped 시 Step 2 skip | 동작 검증 |
  | 기타 | View backend orchestration 또는 SFN 이전 검토 · EventBridge 정기 트리거 · aws-live cutover (10 spec) |

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md` (2026-06-17 §1~§6)

  | 섹션 | 내용 |
  | --- | --- |
  | (a) wrapper 산출물 인벤토리 | main · config · functions · step 17개 · bundled 미생성 |
  | (b) Step 1 단독 SSM 검증 | MC EC2 · `connector_balance.py` · 🟢 **Success** · ResponseCode 0 · `connector_balance_snapshot` 저장 · 보유종목 0건 |
  | (c) Step 12 PAPER_ORDER_GATE 안전 차단 | `-StartStep 12 -EndStep 12` · `DryRun: False` · `PaperOrder: False` 기본 · 중앙 wrapper gate 차단 · 내부 이중 gate · `-AllowPaperOrderExecute` 없으면 SSM command 제출 0건 · 실 KIS 주문 제출 0건 |
  | (d) Step 13 / Step 17 wrapper 흐름 | Step 13 = 주문 상태 조회 · DB 상태 갱신 · Step 17 = balance · position snapshot refresh · 둘 다 신규 broker · KIS 주문 제출 없음 |
  | (e) 전체 1~17 DryRun | FOUND 17건 통과 · parser validation 20건 OK · 위험 키워드 safety grep 통과 · Step 10 / 11 `--execute` 는 execution 내부 상태 갱신 · Step 12 `--execute` 만 KIS paper 주문 제출 가능 step 분류 |
  | (f) 안전 / 보안 점검 | secret · KIS · 계좌 · token · RDS · account-id · ARN · access key · EIP · image digest 평문 기록 0건 · 실 broker · KIS · `--execute` 주문 제출 0건 · aws-live 작업 0건 · wrapper 기반 전체 1~17 실제 재실행 0건 · bundled wrapper 미생성 |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-17 섹션 | 신규 누적 (Daily AWS PowerShell wrapper 구현) |

### Security

  | 항목 | 결과 |
  | --- | --- |
  | secret · KIS · 계좌 · token · RDS · account-id · IAM Role ARN · secret ARN · access key · instance-id · EIP · image digest · task ARN · job ARN 평문 기록 | 0건 (`[REDACTED]` 또는 `<account-id>` · `<region>` · `<rds-endpoint>` · `<role-arn>` · `<secret-arn>` · `<instance-id>` · `<eip>` · `<image-digest>` · `<task-arn>` · `<job-arn>`) |
  | wrapper summary · overrides JSON · SSM stdout · stderr 파일 secret 평문 | 0건 (`/tmp/inject-env.sh` v5 env injection 만 · value 평문 출력 0건 · key presence / length 만 점검) |
  | 실 broker · KIS · 신규 BUY · SELL · 취소 · 정정 · `--execute` · `mark_position_sell_ordered()` · fill · position sync 자동 재시도 | 0건 |
  | 본 세션 환경 | `aws-paper` 한정 · aws-live 작업 0건 · wrapper 환경 입력 자체가 `aws-paper` 만 |
  | Step 12 PAPER_ORDER_GATE | `-AllowPaperOrderExecute` 명시 없이 차단 · 본 일자 옵션 사용 0건 · 실 KIS paper 주문 제출 0건 |
  | AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · SSM Parameter Store · RDS · KIS | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 · 검증 정리만 |
  | AWS CLI · boto3 · AWS 리소스 생성 · 수정 · 삭제 | 0건 |
  | `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
  | CloudWatch Logs · SSM 응답 · KIS API · PowerShell stdout 전문 평문 인용 | 0건 |
  | wrapper summary · step stdout · stderr 저장 파일 위치 | `C:\Temp\portfolio-daily-aws-paper` 하위 run 폴더 · 운영자 로컬 PC 도구 · Kiro spec 산출물 외부 |
  | RDS DDL | 0건 |
  | RDS DML | Step 1 단독 SSM 1차 검증의 `connector.connector_balance_snapshot` insert 1건 · `connector.connector_position_snapshot` 보유종목 0건 · `legacy.holdings` 저장은 03 spec Step 1 흐름 정합 한정 · 그 외 17 step DryRun 은 ECS RunTask / Batch SubmitJob / SSM command 제출 0건이므로 write 0건 |
  | `.kiro/scripts/` 위치 | 운영자 로컬 Windows PC 도구 · Kiro spec 산출물 외부 · 8개 MS 소스 · 패키징 · docs · worklog · README · AGENTS.md · CHANGELOG 영역과 분리 |
  | 04 · 06 · 08 · 09 spec operation-notes 본 일자 wrapper 작업으로 인한 변경 | 0건 · wrapper 는 cross-cutting 운영자 도구이므로 03 spec operation-notes 에만 누적 · 다른 spec 은 followups-overview 메모로만 참조 |

## 2026-06-17 (Daily AWS 17-step E2E 완료)

### Changed

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md` (2026-06-17 §1~§5)

  | 섹션 | 내용 |
  | --- | --- |
  | (a) Step 1 CONNECTOR_BALANCE 연결 | 첫 세션 (v5 env injection · `as_of_date 2026-06-17`) 을 17-step 시작 상태로 사실 연결 |
  | (b) Step 12 MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE 최종 성공 | KIS paper BUY 4건 · `execution_order` id `26~29` 🟢 **SUBMITTED** · `connector_order_request` id `34~37` · `broker_order_no 0000035906` · `0000035912` · `0000035918` · `0000035932` |
  | (b) 1차 실패 · 보정 사유 | `connector_strategy_order_execute.py` MC EC2 미배포 · system python `psycopg` 부재 → venv python 사용 필요 · `execution` table UPDATE 권한 누락 → 운영자 직접 GRANT 보정 · `source_run_id` fallback 패치 후 정식 배포 |
  | (c) Step 13 CONNECTOR_ORDER_CHECK | 1차 응답 `output1 empty` + `output2 aggregate summary` 다건 active fallback 매핑 오염 → 잘못 생성된 `connector_order_event` / `connector_fill` 삭제 + `connector_order_request` 상태 복구 + summary fallback guard 패치 (1건일 때만 허용 · 다건 시 event / fill / status 변경 금지) + `broker_order_no` 별 단건 조회로 체결 동기화 성공 (`connector_order_request 34~37` FILLED · `connector_fill 26~29` 생성) |
  | (d) Step 17 BALANCE_REFRESH | 1차 실패 = `marketconnector_app` legacy schema USAGE<br>`legacy.holdings` DML<br>sequence<br>search_path 누락 → search_path 보정 + USAGE · DML · sequence GRANT + default privileges 보정 → 재실행 🟢 **Success**<br>ResponseCode 0<br>`connector_position_snapshot` 4종목<br>`position_snapshot_id 120~123`<br>quantity `52 · 65 · 244 · 17`<br>avg_buy_price `28980.77 · 27043.08 · 5744.41 · 120182.35` |
  | (e) 안전 / 보안 점검 | secret · KIS · 계좌 · token · RDS · account-id · ARN · access key · EIP · image digest 평문 기록 0건 · SELL · 취소 · 정정 호출 0건 · aws-live 0건 · patch 본문 인용 0건 |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (2026-06-17 §1~§6)

  | Step | 결과 |
  | --- | --- |
  | Step 6 DAILY_BUY_SIGNAL | ECS RunTask exitCode 0 · run_date `2026-06-17` · data_date `2026-06-16` · `decision.strategy_daily_signal` BUY READY 4건 · 후보 4종 `282330` · `004990` · `003490` · `088350` |
  | Step 7 DAILY_POSITION_SIGNAL | positions 0 · decision_count 0 정상 skip · `decision.strategy_daily_position_decision` 신규 0건 |
  | Step 8 DAILY_BUY_EXECUTION | 1차 blocker = `execution_app` `interest` schema USAGE · table SELECT · sequence · default privileges 누락 → 운영자 GRANT 보정 → 재실행 `execution_plan_id 92` · BUY READY 4건 · `connector_order_request_id` 4건 모두 NULL · broker / KIS 호출 0건 |
  | Step 9 DAILY_SELL_EXECUTION | 정상 skip · sell_decisions 0 · orders_to_upsert 0 · broker 호출 0건 |
  | Step 10 DAILY_AUTO_SELL | 정상 skip · READY SELL 0건 · broker 호출 0건 |
  | Step 11 DAILY_AUTO_BUY | BUY 4건 `READY -> REQUESTED` · `execution_plan_id 92` · `total_qty 378` · `total_target_amount 6908189.40` · `connector_order_request_id` 모두 NULL · broker / KIS 호출 0건 · OD-MS-016 책임 분리 1차 실증 |
  | Step 14 SYNC_SELL_FILL | 정상 skip · SELL fill 0건 |
  | Step 15 SYNC_BUY_FILL | `connector_fill 26~29` 기준 BUY fill sync · `execution_order 26~29` 🟢 **FILLED** |
  | Step 16 SYNC_BUY_POSITION | `strategy_position_state` 4건 OPEN · `position_state_id 6~9` |
  | 안전 / 보안 | broker / KIS 호출 0건 (주문 제출은 03 spec Step 12) · `--execute` 는 11번 / 03 spec 12번 한정 · aws-live 0건 · Strategy Execution = 후보 / 상태 전이 · MC = 실 KIS 주문 제출 책임 경계 1차 실증 · 운영자 직접 보정한 `execution_app` interest GRANT 는 02 · 06 spec 에 사실 기록 |

- 🟢 `.kiro/specs/09-strategy-research-batch/operation-notes.md` (2026-06-17 §1~§3)

  | Step | 결과 |
  | --- | --- |
  | Step 4 BACKTEST_RESEARCH | AWS Batch 🟢 **SUCCEEDED** · latest result date `2026-06-16` · Sharpe Ratio `2.68` · `research.strategy_backtest_daily` · `research.strategy_backtest_daily_position` 최신성 `2026-06-16` |
  | Step 5 BACKTEST_REPORT | AWS Batch 🟢 **SUCCEEDED** · S3 report 4개 생성 · upload prefix 정합 · OD-MS-019 정합 |
  | 안전 / 보안 | RDS DDL 0건 · DML 정상 backtest run 흐름 · Job Role S3 PutObject Resource 한정 · public read 0건 · image digest · job ARN 평문 0건 · heavy 분류 (`run_extended_analysis` · `block_watch_*` · `block_exception_buy_*`) SubmitJob 0건 (R-AUTO-015 정합) |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` (2026-06-17 §1~§3)

  | Step | 결과 |
  | --- | --- |
  | Step 2 INTEREST_CRAWLER | non-GUI ECS Fargate Task `portfolio-paper-interest-crawler:7` 성공 · KRX Windows EC2 worker Scheduled Task 성공 · `interest_program_raw` · `interest_shortsell_raw` 2026-06-16 적재 · crawler worker stop 요청 완료 · KRX GUI = Windows interactive desktop session · non-GUI + KRX GUI hybrid 구조 1차 실증 |
  | Step 3 PREPROCESSOR | ECS RunTask exitCode 0 · `PREPROCESSOR PIPELINE END` · `pre_total_market_daily_feature` · `pre_total_stock_daily_feature` 최신성 `2026-06-16` |
  | 1차 실패 이슈 | `execution_app` interest schema · table SELECT 권한 누락 (Step 8 영향 · preprocessor 자체는 정상 · 02 · 06 spec 사실 기록) · aws-live 0건 · RDS DDL 0건 |

- 🟢 `.kiro/specs/02-aws-network-and-rds/operation-notes.md` (2026-06-17 §1~§3)

  | 섹션 | 내용 |
  | --- | --- |
  | (a) `execution_app` 의 `interest` schema | USAGE + table SELECT + sequence + default privileges 보정 (Step 8 1차 실패 → GRANT 후 재실행 통과 · R-DATA-005 [2026-06-17 보강] 정합) |
  | (b) `marketconnector_app` 의 `legacy` | schema USAGE + `legacy.holdings` DML + sequence + database search_path 보정 (Step 17 1차 실패 → search_path `connector, execution, legacy, reference, public` + USAGE · DML · sequence GRANT + default privileges 보정 후 재실행 통과 · R-DATA-005 · R-DATA-011 정합) |
  | (c) 검증 SQL 후보 | app role 별 `current_setting('search_path')` 점검 · future default privileges 보정 · bare table name 의존 legacy 경로 검증 |
  | 02 spec db-roles-and-grants 정식 갱신 | 후속 분리 (태스크 단위) · 본 일자 password · endpoint · account-id · ARN · 계좌번호 평문 기록 0건 |

- 🟢 `.kiro/specs/06-secrets-and-iam/operation-notes.md` (2026-06-17 §1~§3)

  | 섹션 | 내용 |
  | --- | --- |
  | (a) MC EC2 Instance Role 기반 Secrets Manager / SSM Parameter read | 성공 1차 실증 · JSON SecretString 내부 key 추출 정책 (`kis-app-key` · `kis-app-secret` · `paper-account` · `kis-paper-base-url` 등) |
  | (b) 검증 방식 | value 평문 출력 0건 · key presence · length · alias presence 중심 |
  | (c) 02 spec DB role / grants 연결 | `execution_app` interest SELECT 보정 · `marketconnector_app` legacy · `legacy.holdings` 권한 보정 · default privileges · sequence 검증 후보 · value 평문 기록 0건 · OD-SEC-006 1차 실증 정합 |

- 🟢 03-marketconnector-ec2 하위 문서 4종

  | 파일 | 변경 |
  | --- | --- |
  | `runbook.md` §4 | 검증 SQL 후보 보강 (Step 12 `connector_order_request` `SUBMITTED` + `execution_order` `SUBMITTED` 정합<br>Step 13 active 후보 1건일 때만 summary fallback<br>Step 17 `connector_position_snapshot`<br>`connector_balance_snapshot` 최신성 `as_of_ts` / `max(created_at)` 구분 점검)<br>§2 env 주입에 venv python 사용 필요 (system python `psycopg` 없음) 메모<br>결정값 변경 없음 |
  | `validation-checklist.md` §4/§5/§7 | 2026-06-17 17-step E2E 행 추가 · Step 12 KIS paper BUY 4건 <span style="color:#1A7F37">**[O]**</span> · Step 13 summary fallback guard 1차 실증 <span style="color:#1A7F37">**[O]**</span> · Step 17 legacy.holdings 권한 / search_path 보정 후 재실행 <span style="color:#1A7F37">**[O]**</span> · SELL · 취소 · 정정 · aws-live 0건 <span style="color:#1A7F37">**[O]**</span> |
  | `tasks.md` | task 5 · 6 · 9 · 12 17-step E2E 정합 사실 보강 · §8 신규 task 29 · 30 · 31 (`inquire-daily-ccld` summary fallback 테스트 · `connector_strategy_order_execute.py` `source_daily_signal_id` null 보정 · `connector_balance_snapshot` 최신성 SQL 정리) · 결정값 변경 없음 |
  | `design.md` §8.3 | Step 12 / 13 책임 경계 1차 실증 · `inquire-daily-ccld` `output1` / `output2` 처리 정책 · summary fallback guard 정책 · 결정값 변경 없음 |

- 🟢 06-secrets-and-iam · 08-interest · 02-aws 하위 문서 4종

  | 파일 | 변경 |
  | --- | --- |
  | `06/runbook.md` §4/§5 | `execution_app` interest SELECT · `marketconnector_app` legacy.holdings 권한 · default privileges 후속 · 02 spec 정식 갱신 후속 · 결정값 변경 없음 |
  | `06/validation-checklist.md` §3/§4 | 2026-06-17 1차 실증 행 (MC EC2 Instance Role + Secrets Manager + SSM Parameter Store + JSON SecretString 내부 key 추출) 🟢 **[O]** |
  | `08/tasks.md` | Step 2 INTEREST_CRAWLER 17-step E2E 정합 · crawler worker stop 요청 결과 · Step 3 PREPROCESSOR 결과 · 결정값 변경 없음 |
  | `02/db-roles-and-grants.md` §4/§5 | 후속 항목 메모 · `execution_app` interest USAGE · SELECT · sequence · default privileges 정식 매트릭스 후속 · `marketconnector_app` legacy schema · `legacy.holdings` DML · sequence · search_path 정식 매트릭스 후속 · R-DATA-005 · R-DATA-011 정합 · 결정값 변경 없음 |
  | `02/validation-checklist.md` | 2026-06-17 1차 실증 행 · `execution_app` interest SELECT 통과 🟢 **[O]** · `marketconnector_app` legacy · `legacy.holdings` 권한 / search_path 통과 🟢 **[O]** |

- 🟢 `.kiro/README.md` "현재 진행 상태 요약" 갱신

  | 행 | 내용 |
  | --- | --- |
  | 03 | MC EC2 KIS paper BUY 4건 (execution_order id `26~29` SUBMITTED · connector_order_request id `34~37` · summary fallback guard 1차 실증 · BALANCE_REFRESH legacy 권한 보정 후 재실행 통과) |
  | 04 | Strategy Decision / Execution AWS E2E paper 1차 실증 (execution_plan_id `92` · READY → REQUESTED → SUBMITTED → FILLED end-to-end · `strategy_position_state` 4건 OPEN) |
  | 06 | `execution_app` interest 권한 보정 + `marketconnector_app` legacy 권한 보정 · 02 · 06 spec 양쪽 사실 기록 |
  | 08 | Interest Crawler hybrid + Preprocessor ECS 재가동 1차 실증 (2026-06-16 적재 · `pre_total_*` 2026-06-16 · crawler worker stop 완료) |
  | 09 | AWS Batch BACKTEST_RESEARCH + BACKTEST_REPORT (Sharpe Ratio `2.68` · S3 4개 객체) |
  | 02 | `execution_app` interest + `marketconnector_app` legacy schema · `legacy.holdings` 권한 / search_path 보정 1차 실증 |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | Change Log 2026-06-17 두 번째 항목 | 추가 |
  | 신규 OD | 0건 |
  | OD-MS-016 · OD-MS-021 · OD-DB-008 · OD-MS-008 · OD-MS-019 | 본문 변경 없이 1차 실증 메모 보강 |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | 변경 |
  | --- | --- |
  | R-AUTO-018 신규 | `inquire-daily-ccld` summary fallback 오매핑 위험 · mitigation = guard 패치 (1건일 때만 허용) + `broker_order_no` 별 단건 조회 · detection = mapping 정합성 · rollback = 잘못 생성된 row 삭제 + 재실행 · Status 🟢 **Mitigated** |
  | R-DATA-011 신규 | `marketconnector_app` legacy schema USAGE · `legacy.holdings` DML · sequence · search_path 누락으로 BALANCE_REFRESH 실패 위험 · mitigation = search_path `connector, execution, legacy, reference, public` + GRANT + default privileges · detection = `relation "holdings" does not exist` · Status 🟢 **Mitigated** |
  | R-DOCS-001 detection / mitigation | [2026-06-17 보강(17-step E2E)] 재실증 |
  | R-DATA-005 detection / mitigation | [2026-06-17 보강] `execution_app` interest / `marketconnector_app` legacy.holdings 권한 누락 사례 + GRANT 해소 · 02 spec 정식 갱신 후속 |
  | R-AUTO-009 · R-AUTO-010 · R-AUTO-011 | Status 🟢 **Mitigated** · KIS paper BUY 4건 한정 end-to-end 통과 · live cutover 전 평일 · 안전 데이터 추가 검증 후속 |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-17 17-step E2E 후속 메모)

  | 후속 | 내용 |
  | --- | --- |
  | 완료 범위 | 17-step |
  | 후속 유지 | `connector_order_check.py` summary fallback 테스트 · `connector_strategy_order_execute.py` `source_daily_signal_id` null 보정 · `legacy.holdings` 권한 / search_path 정식 문서화 · `connector_balance_snapshot` 최신성 `as_of_ts` SQL 정리 · Windows cp949 콘솔 stdout 이모지 회피 · AWS paper 자동화 orchestrator 후보 · View AWS 실행 매핑 (05) · Step Functions + EventBridge Scheduler (04) · aws-live cutover (10) · CI/CD OIDC (07) |

- 🟢 `.kiro/specs/_common/ms-aws-service-decision-matrix.md`

  | 항목 | 값 |
  | --- | --- |
  | 대상 | 4.1 · 4.3 · 4.4 · 4.6 · 4.7 · 4.8 · 5장 · 6.1 권고안 |
  | 결정값 (1순위) | 변경 없음 |
  | 1차 실증 메모 보강 | MC EC2 · crawler hybrid (non-GUI ECS Fargate + KRX Windows EC2) · preprocessor ECS Fargate · research AWS Batch · decision · execution ECS Fargate · Daily AWS 17-step E2E paper 1차 통과로 검증 |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-17 섹션 | 신규 누적 (Daily AWS 17-step E2E 완료) |
  | 같은 일자 첫 세션 (MC 조회성 dry-run 재검증) | 본문 변경 없음 |

### Security

  | 항목 | 결과 |
  | --- | --- |
  | secret · KIS · 계좌 · token · RDS · account-id · IAM Role ARN · secret ARN · access key · instance-id · EIP · image digest · task ARN · job ARN 평문 기록 | 0건 (모두 `[REDACTED]` 또는 `<account-id>` · `<region>` · `<rds-endpoint>` · `<role-arn>` · `<secret-arn>` · `<instance-id>` · `<eip>` · `<image-digest>` · `<task-arn>` · `<job-arn>`) |
  | broker / KIS 호출 | KIS paper BUY 4건 (`MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` 본 실행) + `inquire-balance` / `inquire-daily-ccld` 조회성 |
  | SELL · 취소 · 정정 · 추가 `--execute` · `mark_position_sell_ordered()` | 0건 |
  | 본 세션 환경 | `aws-paper` 한정 · aws-live 작업 0건 |
  | KIS paper `broker_order_no` | broker 응답값 · 실계좌 아님 |
  | AWS · SSM · EC2 · ECS · AWS Batch · IAM · Secrets Manager · SSM Parameter Store · RDS · GRANT · KIS · S3 · CloudWatch | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 · 검증 정리만 |
  | AWS CLI · boto3 · AWS 리소스 생성 · 수정 · 삭제 | 0건 |
  | `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
  | CloudWatch Logs · SSM 응답 · KIS API · Docker build · PowerShell stdout 전문 평문 인용 | 0건 |
  | RDS DDL | 0건 |
  | RDS DML (17-step 정상 흐름) | 아래 · 표기 rows 참조 |
  | · `connector.connector_order_request` | insert · update · upsert |
  | · `connector.connector_order_event` | insert · update · upsert |
  | · `connector.connector_fill` | insert · update · upsert |
  | · `execution.strategy_execution_order` | insert · update · upsert |
  | · `execution.strategy_execution_plan` | insert · update · upsert |
  | · `execution.strategy_position_state` | insert · update · upsert |
  | · `decision.strategy_daily_signal` | insert · update · upsert |
  | · `decision.strategy_daily_run` | insert · update · upsert |
  | · `decision.strategy_daily_position_decision` | insert · update · upsert |
  | · `connector.connector_balance_snapshot` | insert · update · upsert |
  | · `connector.connector_position_snapshot` | insert · update · upsert |
  | · `interest.*_raw` | insert · update · upsert |
  | · `pre_total_*_feature` | insert · update · upsert |
  | · `research.strategy_backtest_*` | insert · update · upsert |
  | · `--execute` 영향 row 4건 (KIS paper BUY) | 모두 SUBMITTED → FILLED → position OPEN 정합 전이 |
  | 운영자 patch (`connector_strategy_order_execute.py` · `connector_order_check.py`) 본문 인용 | 0건 (03 spec operation-notes 사실만 기록) |
  | 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 spec 영역 변경 | 0건 |

<details><summary>🔵 운영 식별자 요약</summary>

  | 항목 | 값 |
  | --- | --- |
  | execution_plan_id | `92` |
  | execution_order id | `26~29` |
  | connector_order_request id | `34~37` |
  | broker_order_no | `0000035906` · `0000035912` · `0000035918` · `0000035932` |
  | position_state_id | `6~9` |
  | connector_position_snapshot id | `120~123` |
  | 종목 코드 | `282330` · `004990` · `003490` · `088350` |
  | total_qty · total_target_amount | `378` · `6908189.40` |
  | Sharpe Ratio | `2.68` |
  | data_date | `2026-06-16` |
  | signal_date · run_date | `2026-06-17` |

</details>

## 2026-06-17 (MarketConnector 조회성 dry-run 재검증)

### Changed

- 🟢 `.kiro/specs/03-marketconnector-ec2/design.md`

  | 항목 | 값 |
  | --- | --- |
  | 보강 대상 | §8.2 · §8.2.1 · §8.2.2 · §8.2.3 · §8.3 |
  | 정책 명시 | `APP_*` 호환 key + `KIS_*` alias 동시 export (`KIS_APP_KEY` · `KIS_APP_SECRET` · `KIS_PAPER_ACNT` · `KIS_ACNT_PRDT_CD` · `KIS_BASE_URL`) |
  | JSON SecretString 내부 key 추출 | `/portfolio/paper/marketconnector/kis-app-key` · `kis-app-secret` · `paper-account` 의 SecretString 은 JSON · 내부 key `APP_KEY` · `APP_SECRET` · `PAPER_ACNT` · `ACNT_PRDT_CD` 추출 후 export |
  | v5 보정 사례 | JSON dict 전체 export 한 mapping 오류 → JSON parse 후 내부 key value 만 추출 + 호환 key + alias 동시 export 로 해소 |
  | 결정값 (env 주입 4분류 · EC2 Instance Role 단일 자격 증명 · `CONNECTOR_DEBUG=false` 강제 · SG inbound 5000 외부 노출 0건) | 변경 없음 |

- 🟢 `.kiro/specs/03-marketconnector-ec2/runbook.md`

  | 항목 | 값 |
  | --- | --- |
  | §2 env 주입 절차 보강 | KIS_* env key 추가 · JSON parse 절차 · `aws secretsmanager get-secret-value` 결과를 `python -c` 또는 `jq -r .APP_KEY` 로 내부 value 추출 · JSON dict 전체 export 금지 · value length 또는 key presence 만 확인 |
  | §4.1 신규 (`connector_balance.py` 검증 SQL) | `connector_api_call_log` BALANCE `inquire-balance` 최신 row + `connector_balance_snapshot` 최신 row + `connector_position_snapshot` 보유종목 0건 정합 |
  | §4.2 신규 (`connector_order_check.py` 검증 SQL) | `connector_api_call_log` ORDER `inquire-daily-ccld` + `connector_order_request` · `connector_order_event` · `connector_fill` row count 인벤토리 + 신규 0건 정상 판단 |

- 🟢 `.kiro/specs/03-marketconnector-ec2/validation-checklist.md`

  | 표 | 2026-06-17 추가 행 |
  | --- | --- |
  | §4 Connector / Flask 조회성 smoke test | `CONNECTOR_BALANCE` 🟢 **[O]** · `CONNECTOR_ORDER_CHECK` 🟢 **[O]** (MC 조회계열 선행 검증) |
  | §5 Secrets Manager / SSM env 주입 | v5 패턴 (JSON SecretString 내부 key 추출 + `APP_*` 호환 key + `KIS_*` alias 동시 export) 🟢 **[O]** · secret · 계좌 · token 평문 기록 0건 🟢 **[O]** |
  | §7 신규 주문 · 매수 · 매도 · 취소 · 정정 호출 0건 | 2026-06-17 재검증 🟢 **[O]** |

- 🟢 `.kiro/specs/03-marketconnector-ec2/operation-notes.md` (2026-06-17 §1~§3)

  | 섹션 | 내용 |
  | --- | --- |
  | (a) CONNECTOR_BALANCE | 1차 실패 원인 = JSON SecretString 전체 export mapping 오류 → v5 패턴 보정 → 최종 성공 · `connector_balance_snapshot` 최신 row (`as_of_date 2026-06-17` · `as_of_ts 2026-06-17 00:46:17 UTC` · `created_at 2026-06-17 00:46:17 UTC` · `source_api inquire-balance` · `source_version connector-balance-1.0.0` · 보유종목 0건 정상) |
  | (b) CONNECTOR_ORDER_CHECK | v5 재사용 (MC 조회계열 선행 검증) · `inquire-daily-ccld` `response_status=200` · `response_code=0` · `is_success=true` · `called_at 2026-06-17 00:51:03 UTC` · row count `connector_order_request 33` · `connector_order_event 18` · `connector_fill 13` · 신규 0건 정상 · PowerShell 변수 일시 소실은 운영자 측 경미 이슈 |
  | (c) 안전 / 보안 점검 | 신규 주문 0건 · `--execute` 0건 · aws-live 0건 · RDS DDL 0건 · `connector_balance_snapshot` insert 1건만 · secret · 계좌 · token · RDS · account-id · ARN · access key · instance-id · EIP · image digest 평문 기록 0건 · 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 변경 0건 |

- 🟢 `.kiro/specs/03-marketconnector-ec2/tasks.md`

  | 항목 | 값 |
  | --- | --- |
  | task 5 (임시 export 스크립트 패턴 정리) | 🟢 **완료** |
  | task 6 (환경변수 매핑 표 확정) | 🟢 **완료** |
  | task 9 (`connector_balance.py` / `connector_order_check.py` 조회성 실행 절차 정리) | 🟢 **완료** |
  | task 12 (신규 주문 호출 0건 정책 명시) | 🟢 **완료** |
  | §8 신규 task 26 / 27 / 28 | systemd · startup script 정상 운영 모드 전환 시 v5 env mapping 패턴 반영 · `/tmp/inject-env.sh` 운영 스크립트 승격 판단 · `APP_*` 호환 key vs `KIS_*` alias 단일화 결정 |

- 🟢 `.kiro/README.md`

  | 항목 | 값 |
  | --- | --- |
  | 현재 진행 상태 요약 03 `marketconnector-ec2` 행 갱신 | 2026-06-17 `CONNECTOR_BALANCE` · `CONNECTOR_ORDER_CHECK` 조회성 경로 재검증 완료 · 신규 주문 · 매수 · 매도 · 취소 · 정정 호출 0건 명시 |

- 🟢 `.kiro/specs/_common/followups-overview.md`

  | 항목 | 값 |
  | --- | --- |
  | 03-marketconnector-ec2 후속 메모 보강 | SSM RunCommand + Secrets Manager + SSM Parameter Store + Instance Role 기반 재검증 · systemd · startup script 정상 운영 모드 전환 시 v5 env injection 정식화 · View · SFN 연동 전 safe command wrapper 정리 |

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | Change Log 2026-06-17 | 추가 |
  | 신규 OD | 0건 |
  | OD-SEC-006 1차 실증 | MC EC2 Instance Role 기반 secret read 성공 · secret 평문 0건 · JSON SecretString 내부 key parsing 필요성 1차 실증 |
  | OD-MS-001 · OD-MS-009 1차 실증 | MC EC2 조회성 경로 재검증 완료 · `CONNECTOR_ORDER_CHECK` 는 Daily 17단계 후반이지만 본 실행은 선행 단건 검증으로 기록 |
  | OD-SAFE-001 ~ OD-SAFE-004 | 정합 유지 · 신규 주문 · `--execute` 호출 0건 |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | 변경 |
  | --- | --- |
  | R-DOCS-001 detection / mitigation | [2026-06-17 보강] JSON SecretString 내부 value 추출 후 환경변수 export · value 평문 출력 0건 · runbook §4.1 · §4.2 검증 SQL 기반 점검 · `connector_api_call_log` BALANCE · ORDER 의 `response_status` · `response_code` · `is_success` · `connector_balance_snapshot` 최신 row · `CONNECTOR_ORDER_CHECK` 신규 row 0건도 정상 판단 |
  | 신규 R | 0건 |

- 🟢 `.kiro/WORKLOG.md`

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-17 섹션 | 신규 누적 (MC 조회성 dry-run 재검증) |

### Security

  | 항목 | 결과 |
  | --- | --- |
  | secret · KIS · 계좌 · token · RDS · account-id · IAM Role ARN · secret ARN · access key · instance-id · EIP · image digest · task ARN · job ARN 평문 기록 | 0건 (모두 `[REDACTED]` 또는 `<account-id>` · `<region>` · `<rds-endpoint>` · `<role-arn>` · `<secret-arn>` · `<instance-id>` · `<eip>` · `<venv-path>`) |
  | JSON SecretString 내부 key parsing | mapping 사실만 기록 · value 평문 0건 · raw SecretString 전체 export 한 1차 실패 사례는 원인 · 조치 · 결과 중심 요약 (R-DOCS-001 [2026-06-17 보강] 정합) |
  | AWS · SSM · EC2 · RDS · Secrets Manager · SSM Parameter Store · KIS API | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 · 검증 정리만 |
  | AWS CLI 실행 · AWS 리소스 생성 · 수정 · 삭제 | 0건 |
  | `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
  | CloudWatch Logs · SSM 응답 · PowerShell stdout · KIS API response body 평문 인용 | 0건 |
  | broker / KIS · 신규 주문 · 매수 · 매도 · 취소 · 정정 · `--execute` · `mark_position_sell_ordered()` · fill · position sync 자동 재시도 | 0건 |
  | 본 세션 환경 | `aws-paper` 한정 · aws-live 작업 0건 |
  | RDS DDL | 0건 |
  | RDS DML | `connector.connector_balance_snapshot` insert 1건 한정 · `connector.connector_position_snapshot` 신규 0건 (보유종목 0건 정상) · 그 외 SELECT 한정 |
  | 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 변경 | 0건 |

<details><summary>🔵 운영 식별자 요약</summary>

  | 항목 | 값 |
  | --- | --- |
  | secret name path · SSM Parameter name path | 사용자 명시 정책 정합 |
  | 환경변수 key 이름 · KIS API category · api_name | 기록 |
  | response_status · response_code · is_success | 기록 |
  | DB table name · row count | 기록 |
  | `as_of_date` · `as_of_ts` · `called_at` · `source_api` · `source_version` | 기록 |

</details>

## 2026-06-16 (Backend AWS E2E dry-run safe subset 재개)

### Changed

- 🟢 `.kiro/specs/09-strategy-research-batch/operation-notes.md` (2026-06-16 §1~§8)

  | 항목 | 값 |
  | --- | --- |
  | (a) BACKTEST_RESEARCH AWS Batch | `portfolio-paper-strategy-research:5` 🟢 **SUCCEEDED**<br>exitCode 0<br>run_id `439d78e7-41fd-4bb7-b455-18564ddff758`<br>backtest_end_date `2026-06-15`<br>`strategy_trade_log` 310<br>`strategy_backtest_daily` 822<br>`strategy_backtest_daily_position` 2375<br>total_return `4.66534417`<br>mdd `-0.08941942`<br>sharpe `2.68071466`<br>trade_count `310` |
  | (b) BACKTEST_REPORT rev1~rev3 교정 | rev1 local-only · rev2 wrapper 경로 부재 · rev3 module 호출 방식으로 최종 확정 |
  | (c) `portfolio-paper-strategy-report:3` SubmitJob | 🟢 **SUCCEEDED** · S3 prefix `strategy-research/reports/20260616/67522706-9b5f-4770-a312-ceb1987c4655/` 안 4개 객체 (`01_요약_리포트` 7,258 bytes · `02_일자별_매매_리포트` 552,540 bytes · `03_거래_상세_리포트` 329,088 bytes · `04_추천_리포트` 11,847 bytes · private 유지 · public read 0건) |
  | (d) Task 완료 처리 | BACKTEST_RESEARCH · BACKTEST_REPORT AWS Batch 실행 검증 + S3 업로드 보강 + 운영 경로 교정 완료 (rev1 / rev2 는 closed 교정 이력) |

- 🟢 `.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (2026-06-16 §1~§7)

  | 항목 | 값 |
  | --- | --- |
  | (a) DAILY_BUY_SIGNAL 단건 ECS / Fargate | `portfolio-paper-strategy-decision-buy-signal:1` exitCode 0 · image `paper-20260613` · `decision.strategy_daily_signal` row 4건 🟢 **READY** · signal_date `2026-06-16` · data_date `2026-06-15` · rank `1~4` |
  | (b) DAILY_POSITION_SIGNAL 단건 ECS / Fargate | `portfolio-paper-strategy-decision-position-signal:1` exitCode 0 · daily_run_id `45` · market_signal `AGGRESSIVE` · positions 0 정상 skip · `decision.strategy_daily_position_decision` 신규 0건 · max_decision_date `2026-05-29` 유지 |
  | (c) Backend AWS E2E dry-run safe subset 상태 | 1~7번 완료 · 8~17번 미진행 또는 dry-run skip 예정 |
  | (d) Task 완료 처리 | DAILY_BUY_SIGNAL / DAILY_POSITION_SIGNAL 검증 완료 · 주문 · 체결 · sync · execution 단계는 후속 보류 |

- 🟢 문서 갱신 6종

  | 파일 | 변경 |
  | --- | --- |
  | `_common/followups-overview.md` | 2026-06-16 두 번째 후속 메모 · safe subset 완료 (Preprocessor · BACKTEST_RESEARCH · BACKTEST_REPORT · DAILY_BUY_SIGNAL · DAILY_POSITION_SIGNAL) · 후속 (View AWS 매핑 · safe step 우선 · 주문·체결·sync 계열 승인 전 보류 · Execution 계열 dry-run 별도 판단) |
  | `_common/risk-register.md` | R-AUTO-015 detection 에 BACKTEST_REPORT rev1 · rev2 경로 불일치 사례 보강 + rev3 module 확정 · Status 🟢 **Mitigated** 유지 · 신규 리스크 0건 |
  | `_common/operator-decisions.md` | Change Log 2026-06-16 두 번째 항목 · 신규 OD 0건 · OD-MS-008 · OD-MS-013 · OD-MS-019 · OD-MS-021 1차 실증 메모 보강 |
  | `_common/ms-aws-service-decision-matrix.md` | 4.7 · 4.8 · 5장 · 6.1 권고안 · Research (AWS Batch 1순위) · Decision (ECS Fargate 1순위) 1차 실증 완료 메모 · 결정값 변경 없음 |
  | `.kiro/WORKLOG.md` | 2026-06-16 두 번째 세션 신규 누적 · 첫 세션 본문 변경 없음 |

### Security

  | 항목 | 결과 |
  | --- | --- |
  | secret · RDS · KIS · 계좌 · token · account-id · IAM Role · secret ARN · image digest · access key · job ARN · task ARN 평문 기록 | 0건 (모두 `[REDACTED]` 또는 `<account-id>` · `<region>` · `<task-arn>` · `<job-arn>` · `<image-digest>` · `<role-arn>` · `<secret-arn>`) |
  | AWS Batch · ECS · IAM · S3 · Docker · ECR · RDS 작업 | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 · 검증 정리만 |
  | `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
  | CloudWatch · Batch console · ECS event · Docker build · S3 client 로그 평문 인용 | 0건 |
  | broker / KIS · 주문 · 체결 · Daily Batch entrypoint 직접 호출 · BUY · SELL · 취소 · 정정 · `--execute` · fill · position sync 자동 재시도 · `mark_position_sell_ordered()` | 0건 |
  | 본 세션 환경 | `aws-paper` 한정 · aws-live 작업 0건 |
  | RDS DDL | 0건 |
  | RDS DML | BACKTEST_RESEARCH 정상 backtest run + DAILY_BUY_SIGNAL `decision.strategy_daily_signal` row 4건 insert 한정 · `strategy_daily_position_decision` 신규 0건 (positions 0 정상 skip) |
  | 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 변경 | 0건 |
  | `port_strategy_research/aws_batch_backtest_report_wrapper.py` · Dockerfile · requirements.txt 변경분 본문 인용 | 0건 (09 spec operation-notes 사실 기록) |

<details><summary>🔵 운영 식별자 요약</summary>

  | 항목 | 값 |
  | --- | --- |
  | Job Definition family · revision | 기록 |
  | image tag · Compute Environment · Job Queue | 기록 |
  | S3 bucket · prefix · 파일명 · 파일 size | 기록 |
  | run_id · metric 값 · signal_status · signal_date | 기록 |

</details>

## 2026-06-16 (Crawler 데이터 미수집 해결 + KRX EC2 자동화 성공)

### Added

- 🟢 `.kiro/specs/_common/operator-decisions.md`

  | 항목 | 값 |
  | --- | --- |
  | OD-MS-022 신규 | 🟠 **잠정**<br>KRX GUI crawler 자동 로그인 운영 방식 = Windows Autologon + Administrator interactive session + Scheduled Task + SSM trigger<br>SSM RunCommand 가 wrapper<br>Python 을 SYSTEM Session 0 에서 직접 실행하는 방식은 KRX GUI 로그인에 부적합<br>Headless<br>비대화형 KRX 수집은 로컬 검증상 운영 방식에서 제외<br>Autologon 은 paper 전용 Windows worker 보안 예외<br>Administrator session 부재 시 실패 가능성<br>Chrome process 잔존 가능성 |
  | Decision Summary | 전체 79 → 80 / 잠정 34 → 35 |
  | Change Log 2026-06-16 | 추가 |
  | OD-MS-011 · OD-MS-015 · OD-MS-020 | 본문 변경 없이 1차 실증 메모 보강 · KRX GUI = Windows EC2 worker + Autologon + Scheduled Task + SSM trigger 1차 실증 · non-GUI = ECS Fargate Task Definition `portfolio-paper-interest-crawler:7` + RunTask exitCode 0 · Preprocessor = raw 회복 후 ECS 재실행 가능 상태 |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` (2026-06-16 §1~§5)

  | 섹션 | 내용 |
  | --- | --- |
  | (a) 원인 확인 | rev6 = Selenium / Chrome smoke command · 원본 `interest_crawler_daily.py` 는 KRX GUI 단계 포함 · non-GUI orchestration 부재 |
  | (b) non-GUI Task Definition 신규 | 아래 · 표기 rows 참조 |
  | · script | `interest_crawler_daily_nongui.py` 신규 · Python patch · `encoding="utf-8"` · py_compile / AST import 검증 |
  | · Docker · ECR | `portfolio-interest-crawler:paper-20260616-nongui` 빌드 · ECR push |
  | · Task Definition | `portfolio-paper-interest-crawler:7` · FARGATE · awsvpc · cpu 1024 · memory 2048 · log group `/portfolio/paper/crawler` · log stream prefix `ecs-crawler-nongui-daily` · crawler-app DB secret 주입 유지 |
  | · RunTask | cluster `portfolio-paper-cluster` · failures 0 · lastStatus STOPPED · stopCode `EssentialContainerExited` · exitCode 0 · 약 9분 51초 |
  | · CloudWatch Logs 전 step 🟢 **SUCCESS** | `interest_news` · `interest_agency` · `interest_foreignindex` · `interest_commodity` · `interest_macroeconomic` · `interest_price` · `interest_investorflow` · `interest_marketbreadth` |
  | (c) raw 최신성 회복 | 아래 · 표기 rows 참조 |
  | · `interest_price_raw` | 2026-06-08 → 2026-06-15 (1,207,904 → 1,209,624 · 324 Price) |
  | · `interest_investorflow_raw` | 274,204 → 275,949 · 349 Investor |
  | · `interest_marketbreadth_raw` | 4,788 → 4,793 |
  | · `interest_commodity_raw` | 29,132 → 29,162 · 6 Commodity |
  | · `interest_foreignindex_raw` | 33,738 → 33,766 · HANGSENG · NIKKEI225 · SHANGHAI 일부 NULL non-blocker 분리 |
  | · `interest_news_raw` | 2026-06-11 → 2026-06-16 (68,881 → 71,614 · 349 News) |
  | · `interest_agency_raw` | 2026-06-11 → 2026-06-16 (49,018 → 49,044 · 15 Agency Reports) |
  | · `interest_macroeconomic_raw` | 75,742 → 75,777 · 7 Macro |
  | (d) KRX EC2 worker 자동화 재검증 | 아래 phase 참조 |
  | · SSM direct wrapper · Python 실행 부적합 | Session 0 · SYSTEM 비대화형 GUI |
  | · Headless · 비대화형 KRX 수집 | 로컬 검증상 제외 |
  | · Autologon | Microsoft Sysinternals Autologon 적용 · EC2 재부팅 후 SSM Online + `query user` Administrator console session Active |
  | · trigger chain | SSM RunCommand → `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"` → Scheduled Task → Administrator console → `powershell.exe -ExecutionPolicy Bypass -File C:\portfolio\run_krx_worker_daily.ps1` |
  | · KRX login | 성공 (elapsed 92.83s) · `interest_program` 2026-06-15 · `interest_shortsell` 2026-06-15 349 Company |
  | · wrapper 실행 결과 | `DONE :: KRX worker daily` · Last Result 0 · Last Run Time 2026-06-16 04:55:49 · 로그 `C:\portfolio\logs\krx_worker_daily_20260616_045550.log` |
  | · DB row count | `interest_program_raw` 547 → 548 · `interest_shortsell_raw` 190,554 → 190,903 |
  | · Chrome process 잔존 | 후속 정리 옵션 분리 |
  | (e) 최종 판단 | Crawler hybrid 구조 완료 · Preprocessor ECS 재실행 가능 상태 · 후속 = Preprocessor 재실행 · Backend AWS E2E dry-run 재개 · View 구현 |

- 🟢 `.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md`

  | 항목 | 값 |
  | --- | --- |
  | §15 신규 task 82~91 | non-GUI Task Definition rev 7 분리<br>non-GUI raw 최신성 회복<br>KRX EC2 Autologon bootstrap<br>Administrator console session Active<br>SSM → schtasks → Scheduled Task 재검증<br>KRX program<br>shortsell 2026-06-15 DB 최신성<br>Preprocessor 재실행 가능 상태 판단<br>SSM direct Python<br>wrapper 실행 부적합 결정 락<br>Headless<br>비대화형 KRX 수집 제외 결정 락<br>Chrome process 잔존 후속 분리 |
  | 2026-06-15 이월 항목 완료 처리 | task 72 (non-GUI Task Definition 분리) · task 74 (raw 최신성 회복) |
  | Task Dependency Graph | 82~91 분기 추가 |
  | 2026-06-16 이월 항목 요약 | 추가 |

- 🟢 `.kiro/specs/_common/risk-register.md`

  | ID | 변경 |
  | --- | --- |
  | R-SEC-009 신규 | Windows Autologon 사용에 따른 worker 보안 예외 · paper 전용 · RDP inbound 제한 · 자격 증명 문서화 금지 · Administrator password 미기록 · EC2 stop 절차 · 전용 local user 검토 · Status <span style="color:#BF8700">**Open**</span> |
  | R-AUTO-016 신규 | Administrator interactive session 부재 시 KRX GUI 수집 실패 · mitigation = Autologon bootstrap · `query user` pre-check · schtasks trigger 전 session 확인 · Status 🟠 **Open** |
  | R-AUTO-017 신규 | Chrome process 잔존 · mitigation = wrapper 종료 시 Chrome 정리 · 다음 실행 전 정리 · Status 🟠 **Open** |
  | R-AUTO-008 | detection · mitigation 보강 (2026-06-16 Autologon bootstrap + Administrator session Active + Scheduled Task trigger 흐름 1차 실증) |
  | R-DATA-009 | mitigation 보강 (2026-06-16 raw 최신성 회복으로 1차 실증) |
  | R-DATA-010 | mitigation 보강 (non-GUI raw 6종 + KRX raw 2종 적재 회복 · news · agency 2026-06-16 · Preprocessor 재실행 가능 · foreignindex NULL 은 non-blocker 분리) |

- 🟢 `.kiro/specs/_common/followups-overview.md` (2026-06-16 후속 메모)

  | 구분 | 항목 |
  | --- | --- |
  | 1차 완료 | non-GUI Task Definition rev 7 분리 · RunTask exitCode 0 · non-GUI raw 6종 + KRX raw 2종 + news · agency 2026-06-16 · KRX EC2 Autologon + Administrator session Active + Scheduled Task trigger 재검증 |
  | 결정 락 | OD-MS-022 신규 · OD-MS-011 · OD-MS-015 · OD-MS-020 1차 실증 메모 |
  | 보강 · 신규 리스크 | R-SEC-009 · R-AUTO-016 · R-AUTO-017 신규 · R-AUTO-008 보강 · R-DATA-009 · R-DATA-010 보강 |
  | 남은 후속 | Preprocessor ECS 재실행 · Backend AWS E2E dry-run 재개 · EventBridge Scheduler · SFN hybrid orchestration · CloudWatch Logs Agent · SSM output 기반 EC2 worker 로그 수집 · wrapper 내 DB 검증 출력 자동 추가 · EC2 worker 작업 완료 후 stop · Chrome process 정리 · View 구현 |

- 🟢 `.kiro/specs/_common/aws-resource-glossary.md` (신규 용어 3건)

  | 용어 | 설명 |
  | --- | --- |
  | Windows Autologon | Microsoft Sysinternals 도구 · 비용 0 · paper 전용 worker 보안 예외 · 자격 증명 문서화 금지 · 관리자 권한 복호화 가능성 리스크 · 관련 spec 08 |
  | Windows Scheduled Task | Windows 내장 스케줄러 · 비용 0 · KRX GUI worker Administrator interactive session 실행 트리거 · `schtasks /Run` SSM RunCommand 에서 trigger · 관련 spec 08 |
  | SSM RunCommand | AWS Systems Manager 원격 명령 실행 · 비용 0 · `AWS-RunPowerShellScript` 등 document · KRX worker Scheduled Task trigger 와 MC EC2 `CONNECTOR_BALANCE` 실행 진입점 · 직접 wrapper · Python 실행은 SYSTEM Session 0 / 비대화형 GUI 한계로 KRX GUI 로그인에 부적합 · 관련 spec 03 · 08 |

### Changed

- 🟢 08 spec 하위 4종 · common 2종 · README · WORKLOG

  | 파일 | 변경 |
  | --- | --- |
  | `08/design.md` §15 | 2026-06-16 Hybrid execution model 갱신 · rev 6 / rev 7 의미 분리 · SSM direct Python · wrapper 실행 부적합 · Headless · 비대화형 KRX 수집 제외 · §12~§14 결정값 변경 없음 · 워크로드 별 상태 (Preprocessor MS ECS Fargate 유지 · non-GUI crawler ECS rev 7 · KRX GUI crawler Windows EC2 + Autologon + Scheduled Task + SSM) |
  | `08/requirements.md` R8 · R9 | KRX GUI = Windows interactive session 필수 · SSM 은 직접 Python 실행이 아니라 Scheduled Task trigger · Autologon 은 paper 전용 예외 · KRX program · shortsell 완료 기준 = DB max date + row count · non-GUI crawler = ECS Fargate 에서 KRX GUI import 제외 · step 별 SUCCESS 로그 · raw table 최신성 검증 |
  | `_common/cost-simulation.md` 6.1 | KRX Windows worker 비용 메모 · Autologon · Scheduled Task · SSM RunCommand 자체 비용 0 · Windows EC2 running · EIP · storage · 로그 저장량 중심 · 작업 완료 후 stop 후속 비용 절감 · paper / live 단가 변경 없음 |
  | `_common/ms-aws-service-decision-matrix.md` 4.3 · 5장 | hybrid execution model 메모 갱신 (2026-06-16 결과) · KRX GUI = Windows EC2 worker · non-GUI = ECS Fargate · preprocessor = ECS Fargate · KRX GUI headless · Lambda · ECS 단독 · SSM direct 부적합 근거 메모 · non-GUI rev 7 실증 완료 · 결정값 변경 없음 |
  | `.kiro/README.md` | "현재 진행 상태 요약" · Interest Crawler "hybrid 1차 부분 완료" → "hybrid 구조 완료 (non-GUI = ECS rev 7 · KRX GUI = Windows EC2 + Autologon + Scheduled Task + SSM · raw 최신성 회복 · Preprocessor 재실행 가능)" · View / Backend AWS E2E dry-run 후속 재개 유지 |
  | `.kiro/WORKLOG.md` | 2026-06-16 (Crawler 데이터 미수집 해결 + KRX EC2 자동화 성공) 섹션 신규 누적 · 다른 세션 변경 없음 |

### Security

  | 항목 | 결과 |
  | --- | --- |
  | secret · Administrator password · KRX 로그인 password · RDS · KIS · 계좌 · token · account-id · IAM · secret ARN · ECR URI · image digest · instance-id · task ARN 평문 기록 | 0건 (모두 `[REDACTED]` 또는 `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` · `<role-arn>` · `<secret-arn>`) |
  | Autologon 처리 | paper 전용 Windows worker 보안 예외 (R-SEC-009) · Autologon 기반 Administrator interactive session 자동 생성 · 자격 증명 문서화 금지 · Administrator password 평문 기록 0건 · 관리자 권한 복호화 가능성 리스크 명시 |
  | SSM direct Python · wrapper 실행 | 운영 방식 제외 (SYSTEM Session 0 / 비대화형 GUI · KRX GUI 로그인 부적합 · OD-MS-022 정합) |
  | Headless · 비대화형 KRX 수집 | 운영 방식 제외 (KRX 로그인 · nos_setup · 키보드보안 · iframe 제약 · OD-MS-022 정합) |
  | AWS · SSM · EC2 · ECS · ECR · Docker · IAM · Secrets Manager · RDS · GRANT | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 · 검증 정리만 |
  | `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
  | CloudWatch Logs · wrapper 로그 · SSM 응답 · Selenium · Chrome stdout · stderr · Docker build 로그 평문 인용 | 0건 |
  | broker / KIS 호출 · 실 BUY · SELL · `--execute` 주문 전송 · fill · position sync 자동 재시도 | 0건 |
  | RDS DDL · aws-live 작업 | 0건 (본 일자는 `aws-paper` 한정) |
  | 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog Kiro 작업 변경 | 0건 |
  | `port-interest-crawler` / `port-interest-preprocessor` Dockerfile · requirements.txt · `interest_crawler_daily_nongui.py` 본문 인용 | 0건 (08 spec operation-notes 사실 기록) |

<details><summary>🔵 운영 식별자 요약</summary>

  | 항목 | 값 |
  | --- | --- |
  | image tag | `paper-20260616-nongui` |
  | Task Definition | `portfolio-paper-interest-crawler:7` |
  | Scheduled Task | `Portfolio-KRX-Worker-Daily` |
  | wrapper 로그 | `krx_worker_daily_20260616_045550.log` |
  | KRX 적재 일자 | 2026-06-15 |
  | news · agency 적재 일자 | 2026-06-16 |
  | non-GUI raw 6종 적재 일자 | 2026-06-15 |

</details>

## 2026-06-15 (Backend AWS E2E dry-run 1차 + Interest Crawler 상태 재판정)

### Added

- 🟡 **OD-MS-020 · OD-MS-021 신규 결정 락**

  | 항목 | 값 |
  | --- | --- |
  | OD-MS-020 | `port-interest-crawler` 상태 재판정 — Interest Crawler hybrid 1차 구현 부분 완료 · KRX GUI worker 운영 가능 · ECS Fargate crawler smoke 검증 완료 · non-GUI daily raw 수집 운영 경로와 raw 전체 최신성 검증은 후속 |
  | OD-MS-020 판정 원칙 | "완료" 표기는 실제 데이터 적재 · 최신성 검증까지 확인된 경우에만 사용 |
  | OD-MS-021 | Backend AWS E2E dry-run 17단계 순서 + 안전 기준 |
  | 17단계 순서 | 1 `CONNECTOR_BALANCE`<br>2 `INTEREST_CRAWLER`<br>3 `PREPROCESSOR`<br>4 `BACKTEST_RESEARCH`<br>5 `BACKTEST_REPORT`<br>6 `DAILY_BUY_SIGNAL`<br>7 `DAILY_POSITION_SIGNAL`<br>8~11 BUY/SELL/AUTO<br>12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`<br>13 `CONNECTOR_ORDER_CHECK`<br>14~16 fill·position sync<br>17 `BALANCE_REFRESH` |
  | 안전 기준 | 실제 BUY / SELL 실행 <span style="color:#D1242F">**금지**</span> · `--execute` 주문 전송 <span style="color:#D1242F">**금지**</span> · fill·position sync 자동 재시도 <span style="color:#D1242F">**금지**</span> · aws-live 작업 <span style="color:#D1242F">**금지**</span> · Research 는 Decision 보다 먼저 · Execution·MarketConnector 계열은 dry-run 또는 skip |
  | Status | 두 항목 모두 🟠 **잠정** |
  | Decision Summary | 전체 77 → 79 · 잠정 32 → 34 |
  | Change Log | 2026-06-15 두 번째 항목 |
  | OD-SEC-006 | 본문 변경 없음 · MarketConnector EC2 role 의 preprocessor secret `AccessDeniedException` 1차 실증 메모 보강 · Status 잠정 유지 |
  | OD-DB-008 | 본문 변경 없음 |

- 🟢 **`.kiro/README.md` "현재 진행 상태 요약" 섹션 신규**

  | 항목 | 값 |
  | --- | --- |
  | Interest Crawler | hybrid 1차 · 부분 완료 |
  | Preprocessor | ECS 실행 성공 · 입력 raw 최신성 제약 |
  | Connector Balance | 1차 실행 성공 |
  | Strategy Research | AWS Batch 1차 실증 통과 |
  | Strategy Decision · Execution | 1차 RunTask 검증 통과 |
  | aws-live | 미진행 |
  | 상세 참조 | `followups-overview.md` · `WORKLOG.md` |

### Changed

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` (2026-06-15 §1~§8)**

  | 섹션 | 내용 |
  | --- | --- |
  | (a) 사전 점검 | AWS 계정 · region · EC2 · ECS · AWS Batch |
  | (b) `CONNECTOR_BALANCE` | MarketConnector EC2 · venv python · SSM RunCommand · KIS Secrets JSON key parsing 정정 (`APP_KEY` → `KIS_APP_KEY` 등 mapping) · 기존 `access_token.txt` 백업 · 신규 token 발급 · KIS balance API status 200 · 모의투자 잔고 조회 · `connector_balance_snapshot` 저장 · `connector_position_snapshot` 보유 0건 · legacy holdings 0건 |
  | (c) KRX GUI worker 재실행 | `KRX already logged in` · `KRX Login Ready` · `[Collected Date] None` idempotent · `interest_program_raw` · `interest_shortsell_raw` 최신일 2026-06-12 |
  | (d) non-GUI raw 7종 SQL 점검 | `interest_agency_raw` 2026-06-11 · `interest_news_raw` 2026-06-11 · `interest_commodity_raw` · `interest_foreignindex_raw` · `interest_investorflow_raw` · `interest_marketbreadth_raw` · `interest_price_raw` 모두 2026-06-08 · `interest_ticker_value_raw` 2026-03-09 (dry-run 핵심 차단 요인 제외) |
  | (e) preprocessor ECS RunTask | cluster `portfolio-paper-cluster`<br>TD `portfolio-paper-interest-preprocessor:1`<br>FARGATE<br>awsvpc<br>public-a+public-b<br>`assignPublicIp=ENABLED`<br>SG `sgroup-preprocessor-tasks`<br>lastStatus `STOPPED`<br>stopCode `EssentialContainerExited`<br>container `interest-preprocessor`<br>exitCode 0<br>약 3분 43초<br>DB `updated_at` 2026-06-15 11:03:55+00 (KST 20:03:55) 갱신<br>신규 2026-06-15 feature date 0건 |
  | (f) Secret / IAM 권한 분리 실증 | MarketConnector EC2 role → preprocessor secret `GetSecretValue` `AccessDeniedException` · 정책 정합성 정상 동작 · 권한 추가 0건 · preprocessor DB 확인은 ECS Task 또는 SSM Port Forwarding · IAM 변경 0건 |
  | (g) 17단계 진행 상태 점검표 | 완료 1·3 · 부분 완료 2 · 미진행 4~7 · 미진행/skip 예정 8~17 · 실제 BUY·SELL·`--execute`·fill·position sync 자동 재시도 0건 · aws-live 0건 |
  | (h) 후속 인계 | non-GUI daily 운영 Task 분리 · raw 최신성 회복 · preprocessor 재실행 + feature date 점검 · dry-run 재개 · 표현 통일 점검 |
  | 표현 보정 결정 락 | OD-MS-020 정합 · "Interest Crawler 완성: 완료" → "Interest Crawler hybrid 1차 구현: 부분 완료" |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md` §14 신규 task 70~81**

  | 항목 | 값 |
  | --- | --- |
  | 신규 task 범위 | KRX GUI worker 운영 가능 재확인 · KRX raw 최신일 SQL · non-GUI Task Definition · command 분리 후속 · non-GUI raw 최신일 SQL 정기 · raw 최신성 회복 · preprocessor RunTask 절차 · `updated_at` 갱신 확인 · preprocessor 재실행 절차 · raw·feature 최신성 검증 SQL 자동화 · 표현 보정 결정 락 · 17단계 점검표 · Secret·IAM 권한 분리 실증 |
  | Task Dependency Graph | 70~81 분기 추가 |
  | 2026-06-15 이월 항목 | 7건 요약 추가 |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/design.md` §14 보강**

  | 항목 | 값 |
  | --- | --- |
  | 보강 범위 | 2026-06-15 운영자 검증 결과 · Interest Crawler 상태 재판정 |
  | §12 · §13 유지 | hybrid execution model · 1차 자동화 완성 판단 본문 변경 없음 |
  | 워크로드 별 상태 | Preprocessor MS = 단발 RunTask 성공 + 데이터 최신성 제약 · KRX GUI worker = 재실행 idempotent · non-GUI crawler = 운영 실행 미도달 |
  | non-GUI 인벤토리 1차 분류 | ECS Fargate 후보 8종 · 제외 4종 |
  | 표현 보정 | OD-MS-020 정합 · raw 최신성 검증 부족 → downstream 영향 표 · Backend AWS E2E dry-run 진입 정합 표 |
  | 갱신 원칙 | §1~§13 변경 없음 · non-GUI 운영 미도달 명시 · 후속 분리 |

- 🟢 **`.kiro/specs/_common/risk-register.md` 신규 R-DATA-009 · R-DATA-010**

  | ID | 내용 |
  | --- | --- |
  | R-DATA-009 신규 | smoke 검증을 daily 데이터 최신성 완료로 오해할 위험 · mitigation = "완료" 표기 규칙 (OD-MS-020 정합) · smoke 통과와 daily raw 최신성 통과 분리 · 문서·보고·슬라이드 표현 통일 점검 · KRX GUI worker 완료와 전체 완료 혼동 <span style="color:#D1242F">**금지**</span> · detection = 잔존 표현 grep · rollback = 보정 표현으로 즉시 수정 · Affected Spec 08·04·09·전체 · Status <span style="color:#BF8700">**Open**</span> |
  | R-DATA-010 신규 | 아래 · 표기 rows 참조 |
  | · 위험 | raw 최신성 부족 상태에서 preprocessor / Research / Decision 이 stale raw 를 입력으로 사용 · 신규 feature date 미생성 · Daily Decision 과거 거래일 기준 산출 · Research backtest 직전 거래일 미반영 |
  | · mitigation | OD-MS-020 · OD-MS-021 정합 · dry-run 진입 시 raw 최신성 검증 SQL 선행 · non-GUI raw 7종 최신일자 정기 확인 · preprocessor 재실행 후 feature max date · `updated_at` 검증 · task 78 자동화 후속 |
  | · detection | `interest_*_raw` `MAX(trade_date)` 비교 · `data_date` 비교 |
  | · rollback | raw 최신성 회복 후 preprocessor 재실행 + feature date 점검 |
  | · Affected Spec | 08·04·09 |
  | · Status | 🟠 **Open** |
  | R-DATA-005 · R-DATA-006 · R-DATA-007 · R-DATA-008 · R-COST-003 | 본문 변경 없음 |

- 🟢 **`.kiro/specs/_common/followups-overview.md` 2026-06-15 두 번째 후속 메모**

  | 구분 | 내용 |
  | --- | --- |
  | 1차 완료 | 사전 점검 · `CONNECTOR_BALANCE` 1차 · KRX GUI worker 재실행 idempotent · KRX raw 최신일 · non-GUI raw 최신일 SQL · preprocessor ECS RunTask 성공 · Secret·IAM 권한 분리 실증 · 17단계 진행 상태 정리 |
  | 결정 락 | OD-MS-020 · OD-MS-021 신규 · OD-SEC-006 1차 실증 메모 |
  | 보강·신규 리스크 | R-DATA-009 · R-DATA-010 신규 |
  | 남은 후속 | non-GUI crawler 운영 TD·command 분리 · interest raw 최신성 검증 자동화 · preprocessor 실행 후 raw·feature 최신성 검증 SQL 자동화 · raw 최신성 회복 · preprocessor 재실행 + 신규 feature date 점검 · Backend E2E dry-run 재개 · 표현 통일 점검 |

- 🟢 **`.kiro/WORKLOG.md`**

  | 항목 | 값 |
  | --- | --- |
  | 신규 세션 | 2026-06-15 (Backend AWS E2E dry-run 1차 + Interest Crawler 상태 재판정) 두 번째 세션 |
  | 첫 번째 세션 | Strategy Research AWS Batch 골격 + full/report + S3 업로드 · 본문 변경 없음 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret · token · password · RDS · KIS · 계좌 · KRX 로그인 password · account-id · IAM access key · secret ARN · IAM Role ARN · ECR URI · image digest full sha256 · instance-id · task ARN · EIP 평문 기록 | 0건 |
| placeholder 사용 | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` · `<role-arn>` · `<secret-arn>` |
| AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · RDS · GRANT 작업 | 모두 운영자 직접 수행 · Kiro 는 문서·절차·검증 정리만 |
| `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
| KIS Secrets JSON key → 환경변수 mapping | mapping 사실만 기록 · 값 평문 기록 0건 · raw SecretString export <span style="color:#D1242F">**금지**</span> 정책 1차 실증 |
| CloudWatch · wrapper 로그 (`krx_worker_daily_*.log`) · SSM 응답 · docker build · Selenium·Chrome stdout·stderr 평문 인용 | 0건 |
| broker / KIS 호출 | `CONNECTOR_BALANCE` 한정 조회성 (status 200 · 모의투자 잔고) · `connector_balance_snapshot` · `connector_position_snapshot` insert |
| 신규 BUY · SELL · 취소 · 정정 · `--execute` | 0건 (OD-SAFE-001~004 · R-AUTO-009~011 정합) |
| fill · position sync 자동 재시도 | 0건 |
| RDS DDL | 0건 |
| aws-live 작업 | 0건 (본 일자는 `aws-paper` 한정) |
| MarketConnector EC2 role `AccessDeniedException` | 정책 정합성 1차 실증만 기록 · 권한 추가 0건 · IAM 변경 0건 |
| 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog 변경 | 0건 |

<details><summary>🔵 운영 식별자 요약</summary>

- MarketConnector EC2 role 이름 · preprocessor TD family·revision
- cluster `portfolio-paper-cluster` · SG `sgroup-preprocessor-tasks`
- Log Group `/portfolio/paper/preprocessor`
- Secret `/portfolio/paper/rds/preprocessor-app` · `/portfolio/paper/kis/marketconnector`
- Compute Environment · Job Queue · Job Definition revision 3
- preprocessor `updated_at` 2026-06-15 11:03:55+00
- KRX raw 최신일 2026-06-12 · non-GUI raw 7종 최신일 인벤토리

</details>

## 2026-06-15 (Strategy Research AWS Batch 실행 검증 완료)

### Added

- 🟡 **OD-MS-019 신규 결정 락 · OD-MS-008 · OD-MS-018 1차 실증**

  | 항목 | 값 |
  | --- | --- |
  | OD-MS-019 신규 | `port_strategy_research` AWS Batch 포팅 대상 = `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종 (View Daily Batch 기준) |
  | OD-MS-019 제외 | `run_extended_analysis.py` = BACKTEST_RESEARCH 내부에서 이미 수행 · 별도 AWS Batch 포팅 대상 제외 + 수동 보조 도구 분류 |
  | OD-MS-019 제외 | `block_watch_*` · `block_exception_buy_*` 4종 = AWS Batch 포팅 대상 제외 + heavy 분류 후속 |
  | report artifact 보존 | S3 bucket `portfolio-paper-migration-yukiever` 재사용 · prefix `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/` |
  | Job Role S3 PutObject | Resource `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` 한정 · public read 0건 · wildcard 0건 |
  | Status | 🟠 **잠정** |
  | Decision Summary | 전체 76 → 77 · 잠정 31 → 32 |
  | OD-MS-008 | 본문 변경 없음 · Strategy Research 컴퓨트 1순위 = AWS Batch 유지 · BACKTEST_RESEARCH + BACKTEST_REPORT SubmitJob 모두 `SUCCEEDED` / exitCode 0 1차 실증 |
  | OD-MS-018 | 본문 변경 없음 · Research Batch image dependency boundary = Research 내부 adapter 이관 · image 포함 = `port_strategy_research` + `port_strategy_common` · `port_strategy_decision` 미포함 |
  | image rebuild | `paper-20260615-report-s3` · image digest sha256 placeholder · size 약 106MB · pushedAt 2026-06-15T17:00:10+09:00 |
  | Strategy Common 확인 | Research adapter 3개 · common 주요 모듈 py_compile 통과 · 각 MS common import smoke 통과 · "1차 정합성 확인 완료 / 정식 package 관리는 후속" |

### Changed

- 🟢 **`.kiro/specs/09-strategy-research-batch/operation-notes.md` (2026-06-15 3개 섹션)**

  <details><summary>(a) AWS Batch 실행 골격 완료 (§1~§13)</summary>

  | 항목 | 값 |
  | --- | --- |
  | Compute Environment | `portfolio-paper-strategy-research-ce` · MANAGED · FARGATE · maxvCpus 4 · state ENABLED · status VALID |
  | Job Queue | `portfolio-paper-strategy-research-queue` · priority 10 · state ENABLED · status VALID |
  | Job Definition rev1 | `portfolio-paper-strategy-research:1` · image `paper-latest` · vCPU 1 · memory 2048 · timeout 600초 · FARGATE · assignPublicIp ENABLED · 기본 command 안전한 `py_compile` smoke |
  | CloudWatch Log Group | `/portfolio/paper/strategy-research` · retention 14일 |
  | Secrets Manager | `/portfolio/paper/rds/research-app` · JSON multi-key `host`/`port`/`dbname`/`username`/`password` · secret value 노출 0건 |
  | Execution Role | `portfolio-paper-research-batch-execution-role` · `AmazonECSTaskExecutionRolePolicy` + research-app secret read inline · ARN 한정 · wildcard 0건 |
  | Job Role | `portfolio-paper-research-job-role` · 최초 smoke 단계 최소 권한 · §11 단계에서 prefix 한정 `s3:PutObject` 권한 추가 · public read 0건 |
  | py_compile smoke SubmitJob | `smoke-strategy-research-import-20260615` · jobId `81ec3581-0204-43ea-8238-a2a6d22f3f28` · 🟢 **SUCCEEDED** · exitCode 0 |
  | DB smoke SubmitJob | `smoke-strategy-research-db-20260615` · jobId `5399aa10-0fdd-466b-8079-236d3b7e7e37` · 🟢 **SUCCEEDED** · exitCode 0 · `db smoke ok` · `research_app` · `portfolio` · schema `research` · search_path 정합 · image pull · secret injection · log delivery 오류 0건 · secret 노출 0건 |
  | Strategy Common | 별도 컴퓨트 없음 · vendoring 유지 · 정식 package 관리는 후속 |
  | 6/13 후속 회수 | (i) AWS Batch CE · Queue · JD 1차 생성 (ii) Log Group · Secrets Manager · IAM Role 정식 생성 (iii) no-op · import smoke SubmitJob 검증 |

  </details>

  <details><summary>(b) full / report 실행 검증 (§1~§7)</summary>

  | 항목 | 값 |
  | --- | --- |
  | BACKTEST_RESEARCH full | 🟢 **SUCCEEDED** · exitCode 0 |
  | run_id | `a39b0b0c-cfe9-474e-8a4a-4ddb33f09567` |
  | total_return | `4.55930879` |
  | mdd | `-0.08941942` |
  | sharpe | `2.65561307` |
  | trade_count | `308` |
  | extended analysis | 내부 수행 포함 |
  | BACKTEST_REPORT | 4개 리포트 생성 🟢 **SUCCEEDED** · exitCode 0 · `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` 적용 |
  | AWS Batch 포팅 대상 확정 | BACKTEST_RESEARCH + BACKTEST_REPORT 2종 |
  | `run_extended_analysis.py` | BACKTEST_RESEARCH 내부 처리 + 수동 보조 도구 분류 |
  | heavy 분류 SubmitJob | 0건 |
  | RDS DDL · DML | 0건 |
  | broker · KIS 호출 | 0건 |
  | Batch automatic retry | 0건 |

  </details>

  <details><summary>(c) BACKTEST_REPORT S3 업로드 보강 (§11~§22)</summary>

  | 항목 | 값 |
  | --- | --- |
  | S3 bucket | `portfolio-paper-migration-yukiever` 재사용 |
  | Job Role 권한 추가 | `s3:PutObject` · Resource `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` · public read 0건 · wildcard 0건 |
  | requirements.txt | `boto3` 추가 · Docker 내부 import smoke 성공 |
  | report wrapper 옵션 | `REPORT_S3_BUCKET` 미설정 시 skip · `REPORT_S3_PREFIX` 기본값 `strategy-research/reports` · `AWS_BATCH_JOB_ID` 기준 하위 경로 분리 · `REPORT_OUTPUT_DIR` 기본값 `/tmp/portfolio-reports` 유지 |
  | Docker rebuild + ECR push | image tag `paper-20260615-report-s3` · digest sha256 placeholder · size 약 106MB · pushedAt 2026-06-15T17:00:10+09:00 |
  | Job Definition rev3 | image `paper-20260615-report-s3` · TaskRole `portfolio-paper-research-job-role` |
  | S3 업로드 SubmitJob | `strategy-research-backtest-report-s3-20260615` · jobId `112f5fe4-02f3-4614-a88c-60a9842e1447` · 🟢 **SUCCEEDED** · exitCode 0 · logStreamName `strategy-research/default/b18e548d46764cd791028088e1d32a6d` |
  | S3 객체 4건 | prefix `strategy-research/reports/20260615/112f5fe4-.../` · `01_요약 리포트` ~ `04_추천 리포트` · private 유지 · public read 0건 |
  | 6/13 후속 회수 | full backtest · 장시간 research · report 생성 · `REPORT_OUTPUT_DIR` 분리 · S3 업로드 보강 |

  </details>

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | 변경 |
  | --- | --- |
  | R-AUTO-015 | mitigation·detection 보강 · Status `Open` → 🟢 **Mitigated** 승격 |
  | R-AUTO-015 보강 근거 | (i) `smoke`/`full` job name prefix 분리 정책<br>smoke 2건 prefix `smoke-strategy-research-*-20260615`<br>BACKTEST_RESEARCH·REPORT 단건은 `full` (ii) JD `attemptDurationSeconds=600`<br>vCPU 1<br>memory 2048<br>`attempts=1` (자동 retry 0건<br>OD-SAFE-004) (iii) 최초 SubmitJob 은 no-op / import smoke 만 (iv) Batch status<br>exitCode<br>Log Stream 정상<br>pull<br>secret injection<br>log delivery 오류 0건 (v) report S3 prefix 한정<br>public read 0건<br>Resource 한정 PutObject |
  | R-DATA-008 | detection 보강 · Strategy Common 1차 정합성 확인 절차 추가 · Status `Open` 유지 |
  | R-COST-003 신규 | 아래 · 표기 rows 참조 |
  | · 위험 | BACKTEST_REPORT S3 보존 lifecycle 미설정 시 storage · PUT · 잘못된 prefix / bucket · public read 부주의 |
  | · mitigation | OD-MS-019 prefix 한정 · Job Role Resource 한정 · public read 0건 · `REPORT_S3_BUCKET` 미설정 시 skip · lifecycle 후속 · 06 spec KMS |
  | · detection | storage 사용량 정기 점검 · `s3:PutObject` audit · public 노출 alarm · prefix 강제 차단 |
  | · rollback | lifecycle 도입 · 잘못 업로드 운영자 정리 · public 노출 시 ACL·policy 즉시 정정 |
  | · Affected Spec | 09·06·10 |
  | · Status | 🟠 **Open** |
  | R-DOCS-001 · R-SEC-001 · R-COST-001 · R-COST-002 | 본문 변경 없음 |

- 🟢 **`.kiro/specs/_common/followups-overview.md` 2026-06-15 09 spec 후속 메모**

  | 구분 | 내용 |
  | --- | --- |
  | 1차 완료 범위 | AWS Batch CE / JQ / JD rev1<br>CloudWatch Log Group<br>Secrets Manager<br>IAM Execution + Job Role<br>py_compile smoke + DB smoke<br>Strategy Common 1차 정합성<br>BACKTEST_RESEARCH full + 내부 extended analysis<br>BACKTEST_REPORT 4개 리포트<br>`REPORT_OUTPUT_DIR=/tmp/portfolio-reports`<br>S3 업로드 보강 + Docker rebuild + ECR push + JD rev3 + SubmitJob + S3 4개 객체 확인 |
  | 결정 락 | OD-MS-019 신규 · OD-MS-008 · OD-MS-018 1차 실증 메모 |
  | 보강·신규 리스크 | R-AUTO-015 🟢 **Mitigated** 승격 · R-DATA-008 detection 보강 · R-COST-003 신규 |
  | 6/13 후속 회수 | 5건 (본 일자 완료) |
  | 남은 후속 | View Daily Batch 의 BACKTEST_RESEARCH / BACKTEST_REPORT → AWS Batch SubmitJob 매핑 · Step Functions state machine (순서 강제 + 자동 재시도 금지) + EventBridge Scheduler · `block_watch_*` · `block_exception_buy_*` 수동 보조 도구 절차 · Research adapter 3개 → `port_strategy_common` 정식 이동 · common 정식 package · CI/CD OIDC · aws-live cutover |
  | 진행 순서 | 08 → 04 → 05 → 09 → 07 → 10 · 변경 없음 · 09 BACKTEST_RESEARCH / REPORT 1차 실행 검증은 본 일자 완료 |

- 🟢 **`.kiro/specs/_common/ms-aws-service-decision-matrix.md`**

  | 항목 | 값 |
  | --- | --- |
  | 보강 범위 | 4.8 port_strategy_research 표 · 5장 최종 권고안 표 · 6.1 보강 항목 매트릭스 Strategy Research 행 |
  | 보강 내용 | AWS Batch + S3 1차 실증 메모 |
  | 권고 변경 | 없음 (AWS Batch 1순위 · ECS Fargate Task 2순위 · Lambda 비권고 유지) |
  | 1.4 · 2장 · 3장 · 5장 다른 MS | 본 일자 변경 0건 |

- 🟢 **`.kiro/WORKLOG.md`**

  | 항목 | 값 |
  | --- | --- |
  | 신규 세션 | 2026-06-15 (Strategy Research AWS Batch 실행 골격 + full / report 검증 + S3 업로드 보강) |
  | 본 일자 다른 세션 | 본문 변경 없음 (본 일자 단일 세션) |

### Security

| 항목 | 결과 |
| --- | --- |
| secret · token · password · RDS · KIS · 계좌 · KRX password · account-id · IAM access key · secret ARN · IAM Role ARN · ECR URI · image digest full sha256 · EIP · Batch job ARN 평문 기록 | 0건 |
| placeholder 사용 | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<job-arn>` · `<role-arn>` · `<secret-arn>` |
| AWS · Docker · ECR · IAM · Secrets Manager · CloudWatch · Batch · S3 · RDS 작업 | 모두 운영자 직접 수행 · Kiro 는 문서·절차·검증 정리만 |
| `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
| CloudWatch Logs 본문 secret value 평문 출력 | 0건 (5개 key 환경변수 주입만 · stdout 마스킹) |
| AWS Batch SubmitJob | 3건 (py_compile smoke · DB smoke · S3 업로드 검증용) + BACKTEST_RESEARCH 단건 + BACKTEST_REPORT 단건 · 모두 운영자 직접 실행 · 자동 트리거 0건 · 자동 retry 0건 (OD-SAFE-004 · R-AUTO-001 · R-AUTO-015) |
| heavy 분류 SubmitJob | 0건 (OD-MS-019 정합 · 수동 보조 도구 분류) |
| RDS DDL / DML | 0건 |
| broker · KIS · 주문 · 체결 · Daily Batch entrypoint 호출 | 0건 |
| live 자동 batch / report 생성 | 후속 승인 전까지 <span style="color:#D1242F">**금지**</span> (OD-SAFE-002 · OD-SAFE-003) · 본 일자는 paper 환경 |
| 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog 변경 | 0건 |
| `port_strategy_research` requirements.txt · report wrapper · Dockerfile 변경분 본문 인용 | 0건 (09 spec operation-notes 사실 기록) |

<details><summary>🔵 운영 식별자 요약</summary>

- image tag `paper-latest` · `paper-20260615-report-s3` · size 약 106MB · pushedAt 2026-06-15T17:00:10+09:00
- Compute Environment `portfolio-paper-strategy-research-ce`
- Job Queue `portfolio-paper-strategy-research-queue`
- Job Definition family `portfolio-paper-strategy-research` revision 1 · 3
- Log Group `/portfolio/paper/strategy-research`
- Secret `/portfolio/paper/rds/research-app`
- Execution Role `portfolio-paper-research-batch-execution-role`
- Job Role `portfolio-paper-research-job-role`
- smoke jobId `81ec3581-...` · `5399aa10-...`
- S3 업로드 jobId `112f5fe4-...`
- logStreamName `strategy-research/default/b18e548d46764cd791028088e1d32a6d`
- S3 bucket `portfolio-paper-migration-yukiever` · prefix `strategy-research/reports/20260615/112f5fe4-.../`
- 파일명 4종
- metric 값 `run_id a39b0b0c-...` · `total_return 4.55930879` · `mdd -0.08941942` · `sharpe 2.65561307` · `trade_count 308`
- image digest full sha256 → placeholder (`<image-digest>` · truncated form)

</details>

## 2026-06-13 (Strategy Research Batch image 1차 준비)

### Added

- 🟢 **`.kiro/specs/09-strategy-research-batch/operation-notes.md` 신규 생성**

  | 항목 | 값 |
  | --- | --- |
  | 신규 파일 | 09 spec 의 첫 산출물 · 일자별 누적 운영 노트 |
  | 기록 형식·안전 원칙 | 02·03·04·06·08 spec operation-notes 와 동일 · `## YYYY-MM-DD <요약>` 헤더 누적 · 결과 = 성공·실패·보류·해당 없음·이월 · docker build · ECR push · CloudWatch 전문 인용 <span style="color:#D1242F">**금지**</span> · IAM 변경 4줄 요약 · Dockerfile · requirements · adapter 본문 인용 <span style="color:#D1242F">**금지**</span> · secret · account-id · 실제 ARN · image digest 평문 0건 · `secretsmanager:GetSecretValue` 운영자 한정 · Kiro 자동 검증은 `DescribeSecret` metadata 만 |
  | IAM 변경 기록 템플릿 | 포함 |

  <details><summary>본 일자 §1~§8 세부</summary>

  | 섹션 | 내용 |
  | --- | --- |
  | §1 AWS 실행 구조 확인 | As-Is entrypoint 7개 · heavy·light 분리 · 외부·내부 dependency · `research_app` 환경변수 + search_path |
  | §2 Research → Decision 직접 의존 제거 | OD-MS-018 정합 · Research 내부 adapter 3개 신규 · import 변경 3개 파일 · Python patch script (`encoding="utf-8-sig"` 읽기 + `encoding="utf-8"` 저장) · `from port_strategy_decision` 잔존 0건 |
  | §3 Batch 용 Docker / ECR | Dockerfile + requirements.txt 신규<br>Docker build context `C:\Workspaces`<br>image 포함 = `port_strategy_research` + `port_strategy_common`<br>image 제외 = `port_strategy_decision`<br>local build `portfolio-strategy-research:paper-20260613`<br>`py_compile` smoke + import smoke 통과<br>`/app/port_strategy_decision` 부재 확인<br>ECR repository `portfolio-strategy-research` 신규 + push `paper-20260613`<br>`paper-latest`<br>image size 약 90MB |
  | §4 ~ §5 AWS Batch 후속 | Compute Environment · Job Queue · Job Definition · CloudWatch Log Group · Secrets Manager · IAM Role · timeout · vCPU · memory · no-op smoke SubmitJob · full backtest 모두 후속 분리 |
  | §6 1차 검증 완료 기준 | 11항목 |
  | §7 본 일자 범위 밖 / 후속 | 9건 |
  | §8 안전·보안 점검 | secret<br>KIS<br>계좌<br>token<br>account-id<br>ARN<br>image digest<br>IAM access key<br>Batch job ARN 평문 0건<br>`secretsmanager:GetSecretValue` 0건<br>AWS Batch SubmitJob 0건<br>full backtest<br>장시간 research<br>report<br>extended<br>block 계열 0건<br>RDS DDL/DML 0건<br>broker<br>KIS 호출 0건<br>8개 MS 변경 0건<br>Dockerfile<br>requirements.txt<br>adapter 3개<br>import 변경 3개 본문 인용 0건 |

  </details>

### Changed

- 🟡 **`.kiro/specs/_common/operator-decisions.md` OD-MS-018 신규**

  | 항목 | 값 |
  | --- | --- |
  | OD-MS-018 신규 | port_strategy_research Batch image dependency boundary = Research 내부 adapter 로 이관 |
  | image 포함 | `port_strategy_research` + `port_strategy_common` |
  | image 제외 | `port_strategy_decision` |
  | Research 내부 adapter 3개 신규 | `research_backtest_market_adapter.py` · `research_backtest_filter_adapter.py` · `research_backtest_sizing_adapter.py` |
  | 대체 대상 | 기존 `port_strategy_decision.backtest_market` · `backtest_filter` · `backtest_sizing` 직접 import 제거 |
  | import 변경 3개 파일 | `backtest_engine.py` · `backtest_buy_logic.py` · `block_exception_buy_engine_run.py` |
  | adapter 원칙 | `port_strategy_common` 호출만 · 자체 판단 로직 미포함 (R-DATA-008 mitigation 정합) |
  | 장기 후보 | adapter → `port_strategy_common` 정식 package 로 이동 (OD-MS-005 · OD-MS-014 후속) |
  | Status | 🟠 **잠정** |
  | Decision Summary | 전체 75 → 76 · 잠정 30 → 31 |
  | Change Log | 2026-06-13 여섯 번째 항목 |
  | OD-MS-008 | 본문 변경 없음 · AWS Batch 1순위 · ECS Fargate Task 2순위 · Lambda 비권고 유지 |

- 🟢 **`.kiro/specs/_common/followups-overview.md` 2026-06-13 09 spec 후속 메모**

  | 구분 | 내용 |
  | --- | --- |
  | 1차 완료 범위 | As-Is entrypoint · heavy-light 분리 · dependency · 환경변수 · Research → Decision 직접 의존 제거 · Dockerfile · requirements.txt · local build · py_compile · import smoke · ECR push |
  | 결정 락 | OD-MS-018 |
  | 보강·신규 리스크 | R-NET-002 · R-NET-003 · R-DATA-005 메모 · R-AUTO-015 · R-DATA-008 |
  | 남은 후속 (10건) | AWS Batch CE · Queue · JD · CloudWatch Log Group · Secret · IAM Role · smoke SubmitJob · full backtest 비용·시간·timeout 기준 · adapter → common package 이동 · Step Functions 통합·EventBridge · CI/CD OIDC · aws-live cutover |
  | 진행 순서 | 08 → 04 → 05 → 09 → 07 → 10 · 변경 없음 · 09 Batch image 준비는 2026-06-13 선행 완료 |

- 🟢 **`.kiro/specs/_common/risk-register.md` 신규 R-AUTO-015 · R-DATA-008**

  | ID | 내용 |
  | --- | --- |
  | R-AUTO-015 신규 | 아래 · 표기 rows 참조 |
  | · 위험 | Strategy Research heavy backtest / report 실수 full 실행 · Batch 비용·장시간 점유 · CE vCPU 점유 · RDS read 부하 · heavy report 산출물이 운영 데이터 잘못 갱신 |
  | · mitigation | OD-MS-018 정합 · 본 일자 image 1차 준비 · py_compile · import smoke 까지만 · heavy job 운영자 승인 · timeout · vCPU · memory 상한 · 최초 SubmitJob 은 no-op / import smoke · `smoke`/`full` prefix · heavy entrypoint allowlist · JD · SubmitJob runbook 후속 |
  | · detection | Batch job duration · smoke 분 단위 / full 시간 단위 alarm · CloudWatch banner · Cost Explorer · `research.strategy_backtest_run` row · CE desired vCPU · Job Queue depth |
  | · rollback | `aws batch terminate-job` · Job Queue disable · CE desired vCPU 0 · 잘못 생성 row 운영자 정리 · Cost Anomaly Detection |
  | · Affected Spec | 09·10 |
  | · Status | 🟠 **Open** |
  | R-DATA-008 신규 | 아래 · 표기 rows 참조 |
  | · 위험 | Research 내부 adapter 3개 가 `port_strategy_common` 계약 변경 (dataclass · enum · reason) 을 따라가지 못해 backtest ↔ Daily Decision 결과 불일치 · `strategy_backtest_run` 신뢰도 흔들림 |
  | · mitigation | OD-MS-018 정합 · adapter common 호출만 · common 변경 시 Research py_compile · import smoke · small sample test 필수 · Daily Decision ↔ Research backtest sample 비교 · 장기 adapter → common 정식 이동 · Decision backtest_* 동일 패턴 |
  | · detection | ImportError · AttributeError · config key missing · dataclass mismatch · reason·signal 불일치 · sizing 결과 불일치 |
  | · rollback | adapter 수정 · 이전 image tag · manual rerun |
  | · Affected Spec | 09·07·10 |
  | · Status | 🟠 **Open** |
  | R-NET-002 · R-NET-003 · R-DATA-005 | 본문 변경 없음 |

- 🟢 **`.kiro/WORKLOG.md`**

  | 항목 | 값 |
  | --- | --- |
  | 신규 세션 | 2026-06-13 Strategy Research Batch image 1차 준비 여섯 번째 섹션 |
  | 같은 일자 1·2·3·4·5번 섹션 | 본문 변경 없음 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret · token · password · RDS · KIS · 계좌 · KRX password · account-id · IAM access key · secret ARN · image digest · EIP · Batch job ARN 평문 기록 | 0건 |
| placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<job-arn>` |
| AWS · Docker · ECR · IAM · Secrets Manager · RDS 작업 | 모두 운영자 직접 수행 · Kiro 는 문서·절차·검증 정리만 |
| `secretsmanager:GetSecretValue` 실호출 | 0건 (Secret `/portfolio/paper/rds/research-app` 자체 미생성) |
| AWS Batch SubmitJob | 0건 |
| Compute Environment · Job Queue · Job Definition | 본 일자 미생성 |
| CloudWatch Log Group · IAM Role (execution / job) | 본 일자 미생성 · 모두 후속 분리 |
| full backtest · 장시간 research · report 생성 · extended analysis · block 계열 | 0건 |
| RDS DDL / DML | 0건 |
| broker · KIS · 주문 · 체결 · Daily Batch entrypoint | 0건 |
| live 자동 batch / report 생성 | 후속 승인 전까지 <span style="color:#D1242F">**금지**</span> (OD-SAFE-002 · OD-SAFE-003) · 본 일자는 paper image 1차 준비 |
| 운영자 직접 작성 Dockerfile · requirements.txt · adapter 3개 · import 변경 3개 파일 본문 인용 | 0건 (09 spec operation-notes 사실 기록) |
| 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog 변경 | 0건 |

<details><summary>🔵 운영 식별자 요약</summary>

- image tag `paper-20260613` · `paper-latest`
- ECR repository `portfolio-strategy-research`
- Dockerfile · requirements.txt · adapter 파일 경로
- image size 약 90MB

</details>

## 2026-06-13 (Strategy Execution ECS / Fargate 1차 포팅 검증)

### Changed

- 🟢 **`.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (2026-06-13 §1~§11)**

  <details><summary>§1~§7 인프라 세부</summary>

  | 항목 | 값 |
  | --- | --- |
  | §1 AWS 실행 구조 · Docker 결정 | 책임 분리 (OD-MS-016) · 7개 entrypoint 인벤토리 · `execution_config.py` `INTEREST_DB_PASSWORD` 요구 · 단일 Task Definition + command override 방식 확정 |
  | §2 Dockerfile · requirements.txt | 운영자 직접 신규 · Docker build context `C:\Workspaces` · 소스 vendoring · 1차 dependency `psycopg2-binary` · default CMD 안전한 `py_compile` · 본문 인용 0건 |
  | §3 로컬 build + smoke | `portfolio-strategy-execution:paper-20260613` · `paper-latest` · container `py_compile` smoke + import smoke 통과 (dummy env) |
  | §4 ECR push | repository `portfolio-strategy-execution` · `paper-20260613` · `paper-latest` |
  | §5 CloudWatch · Secret · IAM | Log Group `/portfolio/paper/strategy-execution` retention 14일 · Secret `/portfolio/paper/rds/execution-app` JSON multi-key · Execution Role `portfolio-paper-ecs-task-execution-role` + execution-app secret read inline (ARN 한정 · wildcard 0건) · Task Role `portfolio-paper-execution-task-role` 확인 |
  | §6 Network | cluster `portfolio-paper-cluster` · public-a + public-b · SG `sgroup-strategy-tasks` · RDS SG inbound + VPC Endpoint SG 443 source 허용 · `assignPublicIp=ENABLED` |
  | §7 Task Definition | `portfolio-paper-strategy-execution` revision 1 ACTIVE · awsvpc · Fargate · cpu 512 · memory 1024 · container `strategy-execution` · `INTEREST_DB_*` 5종 secrets injection · `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` |

  </details>

  <details><summary>§8 RunTask 검증 8종</summary>

  | 케이스 | 결과 |
  | --- | --- |
  | (a) 기본 `py_compile` | exitCode 0 · lastStatus STOPPED |
  | (b) `execution_sync_buy_fill.py` | NO_TARGET · submitted·synced·skipped 모두 0 · exitCode 0 |
  | (c) `execution_sync_sell_fill.py` | NO_TARGET · exitCode 0 |
  | (d) `execution_sync_buy_position.py` | filled_buy_orders_without_position 0 · exitCode 0 |
  | (e) `daily_buy_execution_run.py` | WEEKEND guard 차단 · NO_TARGET · `connector_order_request` 0 · exitCode 0 |
  | (f) `daily_sell_execution_run.py` | WEEKEND guard 차단 · exitCode 0 |
  | (g) `daily_auto_sell_execute_run.py --execute` | WEEKEND guard 차단 · `READY -> REQUESTED` 전환 0 · broker · KIS 호출 0 · exitCode 0 |
  | (h) `daily_auto_buy_execute_run.py --execute` | WEEKEND guard 차단 · 동상 · exitCode 0 |

  </details>

  | 항목 | 값 |
  | --- | --- |
  | §9 1차 검증 완료 기준 | 11항목 |
  | §10 본 일자 범위 밖 / 후속 (9건) | Step Functions state machine · EventBridge Scheduler · MarketConnector executor 인계 · `READY -> REQUESTED -> SUBMITTED` end-to-end · View 관제 UI 격상 · state-command allowlist 표 · `execution_app` GRANT 매트릭스 · aws-live cutover · CI/CD OIDC |
  | §11 안전·보안 점검 | secret · KIS · 계좌 · token · account-id · ARN · image digest · task ARN 평문 0건 · `secretsmanager:GetSecretValue` 결과값 0건 · CloudWatch secret value 평문 0건 · 8개 MS 변경 0건 |

- 🟡 **`.kiro/specs/_common/operator-decisions.md` OD-MS-017 신규**

  | 항목 | 값 |
  | --- | --- |
  | OD-MS-017 신규 | port_strategy_execution Task Definition 운영 방식 = 단일 Task Definition + command override |
  | family · revision | `portfolio-paper-strategy-execution` revision 1 ACTIVE · awsvpc · Fargate · cpu 512 · memory 1024 · container `strategy-execution` |
  | 7종 entrypoint allowlist | `daily_buy_execution_run` · `daily_sell_execution_run` · `daily_auto_buy_execute_run` · `daily_auto_sell_execute_run` · `execution_sync_buy_fill` · `execution_sync_sell_fill` · `execution_sync_buy_position` |
  | override 위치 | RunTask `--overrides` 의 `containerOverrides[].command` |
  | MarketConnector executor | `connector_strategy_order_execute.py --execute` 는 본 Task Definition 미포함 (OD-MS-016 정합) |
  | Status | 🟠 **잠정** |
  | Decision Summary | 전체 74 → 75 · 잠정 29 → 30 |
  | OD-MS-007 · OD-MS-009 · OD-SAFE-004 | 본문 변경 없음 · 1차 실증 메모 보강 |
  | OD-MS-013 대비 | Decision 2개 분리와 달리 · 7개 entrypoint 가 같은 image · role · secret · log group · cpu · memory 공유 |

- 🟢 **`.kiro/specs/_common/followups-overview.md` 2026-06-13 Strategy Execution 검증 후속 메모**

  | 구분 | 내용 |
  | --- | --- |
  | 1차 완료 범위 (8종) | Dockerfile · ECR · Log Group · Secret · IAM · Network · Task Definition · RunTask |
  | 결정 락 | OD-MS-017 |
  | 보강·신규 리스크 | R-AUTO-001 · R-AUTO-014 |
  | 남은 후속 (8건) | Step Functions state machine · EventBridge Scheduler · MarketConnector executor 인계 · `READY -> REQUESTED -> SUBMITTED` end-to-end · View ProcessBuilder → 관제 UI 격상 · state ↔ command allowlist 1:1 표 · `execution_app` GRANT 매트릭스 · 실패·skip·manual approval gate · CI/CD OIDC |
  | 진행 순서 | 08 → 04 → 05 → 09 → 07 → 10 · 변경 없음 |

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | 변경 |
  | --- | --- |
  | R-AUTO-001 | mitigation·detection 보강 · state machine 리뷰 체크리스트 (a) 각 state Retry 정책 비활성화 (b) command override 명이 7종 allowlist 안 (c) BUY / SELL / fill sync / position 변경 계열 step Retry block 부재 · detection 에 SFN execution history retry attempt · state ↔ command override audit · `connector_order_request` 의 `idempotency_key` / `client_order_id` 중복 row 점검 추가 |
  | R-AUTO-014 신규 | 아래 · 표기 rows 참조 |
  | · 위험 | 단일 Task Definition + command override 오매핑 · 잘못 매핑 시 의도치 않은 entrypoint 실행 |
  | · mitigation | OD-MS-017 · state ↔ command override 1:1 표 정식 정리 · 7종 allowlist · 자동 retry <span style="color:#D1242F">**금지**</span> · View / SFN 호출부 임의 command 거부 · MarketConnector executor 본 TD 포함 <span style="color:#D1242F">**금지**</span> |
  | · detection | Task 시작 banner ↔ state name 비교 · ECS RunTask `overrides` audit (CloudTrail) · `execution.strategy_execution_order` / `connector.connector_order_request` row count 검증 · SFN task input·output 정합성 |
  | · rollback | SFN `StopExecution` · ECS `StopTask` · 운영자 직접 SQL 점검 · broker 호출 시 KIS 취소·수동 정리 |
  | · Status | 🟠 **Open** |

- 🟢 **`.kiro/WORKLOG.md`**

  | 항목 | 값 |
  | --- | --- |
  | 신규 세션 | 2026-06-13 Strategy Execution ECS / Fargate 1차 포팅 검증 다섯 번째 |
  | 같은 일자 1·2·3·4번 섹션 | 본문 변경 없음 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret · token · password · RDS · KIS · 계좌 · KRX · account-id · IAM access key · secret ARN · image digest · EIP · task ARN 평문 기록 | 0건 |
| placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · RDS 작업 | 모두 운영자 직접 수행 · Kiro 는 문서·절차·검증 정리만 |
| `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
| CloudWatch Logs 본문 secret value 평문 출력 | 0건 (TD `secrets` 필드 = 환경변수 · stdout 마스킹) |
| Dockerfile · requirements.txt 본문 인용 | 0건 (본 spec operation-notes 사실 기록) |
| broker · KIS · 주문 · 체결 · Daily Batch entrypoint 호출 | 0건 |
| `--execute` 실호출 | 2종 발생 (`daily_auto_sell_execute_run.py` · `daily_auto_buy_execute_run.py`) · 한국 영업일 아님 · WEEKEND guard 차단 · `READY -> REQUESTED` 전환 0건 · `connector_order_request` 생성 0건 · broker · KIS 호출 0건 (R-AUTO-009·010·011 정합) |
| live 자동 주문 | 후속 승인 전까지 <span style="color:#D1242F">**금지**</span> (OD-SAFE-002 · OD-SAFE-003) |
| 8개 MS 변경 | 0건 |

<details><summary>🔵 운영 식별자 요약</summary>

- image tag `paper-20260613` · `paper-latest`
- Task Definition family `portfolio-paper-strategy-execution` revision 1
- container `strategy-execution` · SG `sgroup-strategy-tasks`
- Log Group `/portfolio/paper/strategy-execution`
- Secret `/portfolio/paper/rds/execution-app`
- Task Role `portfolio-paper-execution-task-role`
- Execution Role `portfolio-paper-ecs-task-execution-role`

</details>

## 2026-06-13 (Local-to-AWS Paper RDS SSM Port Forwarding 보강 — psql 18 client + pgAdmin4 접속 검증)

### Changed

- 🟢 **`.kiro/specs/02-aws-network-and-rds/operation-notes.md` (2026-06-13 SSM Port Forwarding 보강 §1~§5)**

  | 섹션 | 내용 |
  | --- | --- |
  | §1 psql 18 client 직접 경로 실행 | client `18.1` · server `18.4` · SSL `TLSv1.3` · `inet_server_addr=10.0.20.165` · `inet_server_port=5432` · 일반 `psql` PATH 미등록은 R-AUTO-013 mitigation 보강으로 좁힘 |
  | §2 pgAdmin4 Server 등록 | Name `AWS Paper RDS - portfolio` · Host `localhost` · Port `15433` · Maintenance database `portfolio` · Username `portfolio_admin` · SSL mode `Prefer` |
  | §2 검증 SQL | `current_user` · `current_database` · `inet_server_addr` · `inet_server_port` · `search_path` |
  | §2 조회 가능 확인 | 6개 schema (connector · decision · execution · interest · ops · preprocessor) · 핵심 table 16개 SELECT 가능 |
  | §2 `portfolio_admin` search_path | `"$user", public` 출력 · `ALTER ROLE ... SET search_path` 적용 대상 아님 · 정상 (OD-DB-006 정합) |
  | §3 Runbook 보강 | 같은 일자 §10 Runbook 본문 변경 없이 보조 절차 (psql 18 직접 경로 · pgAdmin4 Server 등록 · 성공 기준) 추가 |
  | §4 안전·보안 점검 | AWS · RDS · IAM · Secrets Manager · SSM 변경 0건 · RDS DDL/DML 0건 · broker · KIS · 주문 · 체결 entrypoint 호출 0건 · password 평문 기록 0건 |
  | §5 본 일자 범위 밖 · 후속 (4건) | 일반 `psql` PATH 등록 · pgAdmin4 환경별 서버 분리 · pgAdmin4 connection pool reconnect · Strategy Execution 본 phase |

- 🟢 **`.kiro/specs/03-marketconnector-ec2/operation-notes.md` (SSM Port Forwarding 표준 경유지 §1~§3)**

  | 항목 | 값 |
  | --- | --- |
  | 본 EC2 | `portfolio-paper-marketconnector-ec2` · instance id `i-0fce77927b7397b88` |
  | 역할 | 같은 일자 앞 섹션 SSM Port Forwarding 경유지 · 추가 client 2종 (psql 18 · pgAdmin4) 동일 tunnel 통과 |
  | SG · Instance Role · Instance Profile 권한 변경 | 0건 |
  | EC2 신규 생성 · 타입 변경 · EBS 재생성 · public subnet 변경 · EIP detach | 0건 (03 design §2.3 정합) |

- 🟢 **`.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (Strategy Execution 사전 검증 §1~§4)**

  | 항목 | 값 |
  | --- | --- |
  | 후속 성격 | 같은 일자 앞 섹션 (`execution_app` Python `psycopg2` 접속 1차 실증) 의 추가 client 2종 보강 |
  | pgAdmin4 조회 가능 확인 | Strategy Execution 핵심 schema/table (`execution.strategy_execution_order`<br>`execution.strategy_execution_plan`<br>`execution.strategy_position_state`<br>`execution.connector_signal_order_map`<br>`connector.connector_account`<br>`connector.connector_order_request`<br>`connector.connector_order_event`<br>`connector.connector_fill`<br>`decision.strategy_daily_position_decision`<br>`decision.strategy_daily_run`) |
  | 조회 범위 | SELECT 한정 · INSERT · UPDATE · DELETE · DDL 0건 |
  | Strategy Execution 본 phase | Dockerfile · requirements.txt · ECR push · `/portfolio/paper/rds/execution-app` · Task Role · Task Definition · RunTask 단건 검증 모두 후속 분리 |

- 🟡 **`.kiro/specs/_common/operator-decisions.md` OD-NET-011 신규**

  | 항목 | 값 |
  | --- | --- |
  | OD-NET-011 신규 | Local-to-AWS Paper RDS pgAdmin4 사용 원칙 |
  | 등록 원칙 | `localhost:15433` SSM tunnel 만 등록 · RDS endpoint 직접 등록 <span style="color:#D1242F">**금지**</span> |
  | Server 등록 표준 | Name `AWS Paper RDS - portfolio` · Host `localhost` · Port `15433` · Maintenance database `portfolio` · Username `portfolio_admin` 또는 MS 별 app role · SSL mode `Prefer` |
  | Status | 🟠 **잠정** |
  | Decision Summary | 전체 73 → 74 · 잠정 28 → 29 |
  | OD-NET-010 | 본문 변경 없음 · 같은 tunnel 위 client 확장 (Python `psycopg2` · psql 18 · pgAdmin4 3종 통과) |
  | OD-ENV-006 · OD-ENV-007 · OD-ENV-008 | 본문 변경 없이 다중 client 1차 실증 메모 · Status 모두 🟠 **잠정** 유지 |
  | OD-NET-009 · R-SEC-001 · R-NET-004 | 본문 변경 없음 · RDS `PubliclyAccessible=False` 유지 |

- 🟢 **`.kiro/specs/_common/followups-overview.md` 2026-06-13 SSM Port Forwarding 보강 후속 메모**

  | 구분 | 내용 |
  | --- | --- |
  | 1차 추가 검증 완료 | psql 18 직접 경로 실행 · pgAdmin4 Server 등록·검증 SQL·SELECT · Runbook 보강 |
  | 결정 락 | OD-NET-011 |
  | 보강 리스크 | R-AUTO-012 detection · R-AUTO-013 mitigation |
  | 남은 후속 (5건) | 일반 `psql` PATH 등록 · pgAdmin4 환경별 서버 분리 prefix 정책 · pgAdmin4/psql 의 app role 별 비밀번호 보관 · pgAdmin4 connection pool reconnect · MS app role 별 client 접속 검증 |
  | 진행 순서 | 08 → 04 → 05 → 09 → 07 → 10 · 변경 없음 |

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | 변경 |
  | --- | --- |
  | R-AUTO-012 | mitigation·detection·rollback 보강 · pgAdmin4 도 같은 SSM tunnel 위 · tunnel 종료 시 pgAdmin4 즉시 단절 · `Connection terminated` · `server closed the connection unexpectedly` 패턴 모니터 · tunnel 재기동 후 자동 복구 · Status 🟢 **Mitigated** 유지 |
  | R-AUTO-013 | mitigation·detection·rollback 보강<br>PostgreSQL 18 client `C:\Program Files\PostgreSQL\18\bin\psql.exe` 이미 설치<br>full path 직접 실행으로 1차 실증 통과 (client 18.1<br>server 18.4)<br>리스크는 client 미설치가 아니라 일반 `psql` PATH 미등록 문제<br>detection 에 `Get-Item`<br>`$env:Path` 점검<br>rollback 에 `setx PATH ...` 또는 GUI 명시<br>Status 🟢 **Mitigated** 유지 |

- 🟢 **`.kiro/WORKLOG.md`**

  | 항목 | 값 |
  | --- | --- |
  | 신규 세션 | 2026-06-13 SSM Port Forwarding 보강 — psql 18 client + pgAdmin4 접속 검증 네 번째 |
  | 같은 일자 세 번째 섹션 · 이전 두 섹션 | 본문 변경 없음 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret · token · password · RDS · KIS · 계좌 · KRX · account-id · IAM access key · secret ARN · image digest · EIP 평문 기록 | 0건 |
| placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · SSM · EC2 · RDS · GRANT 변경 작업 | 0건 |
| `secretsmanager:GetSecretValue` 실호출 | 0건 |
| broker · KIS · 주문 · 체결 · Daily Batch entrypoint 호출 | 0건 |
| `--execute` 실호출 | 0건 |
| RDS DDL/DML | 0건 |
| 사용 도구 | read-only AWS API · 같은 SSM Port Forwarding tunnel 재사용 · psql 18 / pgAdmin4 / Python `psycopg2` SELECT 조회만 |
| pgAdmin4 · psql 18 비밀번호 | 운영자 로컬 PC 환경 한정 · 본 spec 산출물 · 운영 노트 · 콘솔 캡처 · 로그 평문 기록 <span style="color:#D1242F">**금지**</span> (R-DOCS-001 정합) |

<details><summary>🔵 운영 식별자 요약</summary>

- 앞 섹션 재사용 identifier: instance id · private IP · local port `15433` · SSM session id `terraform-vjp3fv3nz73konetcevdzjh9de` · RDS endpoint hostname `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com`
- 로컬 PostgreSQL 설치 경로: `C:\Program Files\PostgreSQL\18\bin\psql.exe` (운영자 로컬 PC 도구 경로 · secret 아님)

</details>

## 2026-06-13 (Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook 1차 본문)

### Changed

- 🟢 **`.kiro/specs/02-aws-network-and-rds/operation-notes.md` (2026-06-13 SSM Port Forwarding §1~§12)**

  | 항목 | 값 |
  | --- | --- |
  | 사전 도구 점검 | AWS CLI `2.27.50` · Session Manager Plugin `1.2.814.0` |
  | 표준 경유지 결정 | OD-NET-010 = `portfolio-paper-marketconnector-ec2` · instance id `i-0fce77927b7397b88` · `portfolio-paper-crawler-worker` 는 KRX 전용으로 분리 유지 |
  | target EC2 SSM Online | ping `Online` · agent `3.3.4515.0` |
  | RDS endpoint | `PubliclyAccessible=False` 유지 |
  | SSM Port Forwarding tunnel | local port `15433` · session id `terraform-vjp3fv3nz73konetcevdzjh9de` · `Port 15433 opened` |
  | 로컬 psql client | PATH 미등록 발견 (R-AUTO-013) |
  | Python `psycopg2` 점검 | `psycopg2 OK` |
  | `portfolio_admin` 접속 실증 | `inet_server_addr=10.0.20.165` · `inet_server_port=5432` |
  | `execution_app` 접속 실증 | search_path = `execution, decision, research, connector, preprocessor, interest, reference, legacy, public` (OD-DB-006 · OD-DB-007 정합) |
  | Runbook 1차 본문 | 7단계 + `[실행]`/`[확인]`/`[준비]`/`[복구]` 라벨 + 표준 환경변수 + MS 별 app role 매핑 + 성공 기준 + 실패·복구 |
  | 후속 인계 | 7건 |
  | 안전·보안 점검 | password · secret · 계좌 · KIS · token · account-id · IAM access key · secret ARN · EIP 평문 기록 0건 · 운영 식별자는 사실 기록 (secret 아님) |

- 🟢 **`.kiro/specs/03-marketconnector-ec2/operation-notes.md` (SSM Port Forwarding 경유지 역할 §1~§5)**

  | 항목 | 값 |
  | --- | --- |
  | 본 EC2 | 03 spec 정식 운영 EC2 (2026-06-10 검증) 와 동일 |
  | 추가 역할 | Local-to-AWS Paper RDS 접속의 SSM Port Forwarding 표준 경유지 1차 검증 |
  | EC2 신규 생성 · 타입 변경 · EBS 재생성 · public subnet 변경 · EIP detach | 0건 (03 design §2.3 정합) |
  | SG · Instance Role · Instance Profile 권한 변경 | 0건 (`AmazonSSMManagedInstanceCore` managed policy 그대로) |
  | 본 일자 검증 요약 | SSM Port Forwarding tunnel · `portfolio_admin` 접속 · `execution_app` 접속 · RDS DDL/DML 0건 · SELECT 조회 한정 |

- 🟢 **`.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (Strategy Execution 사전 검증 §1~§4)**

  | 항목 | 값 |
  | --- | --- |
  | 성격 | 같은 일자 Strategy Decision ECS / Fargate 1차 포팅 검증 및 책임 분리 섹션과 별개 |
  | 검증 대상 | Strategy Execution AWS 포팅 진입 전 `execution_app` 기반 접속 1차 실증 |
  | 검증 범위 | Python `psycopg2` SELECT 조회 한정 |
  | Strategy Execution 본 phase | Dockerfile · requirements.txt · ECR push · `/portfolio/paper/rds/execution-app` · Execution Role inline policy · Task Role · Task Definition · RunTask 단건 검증 모두 후속 분리 |

- 🟡 **`.kiro/specs/_common/operator-decisions.md` OD-NET-010 신규**

  | 항목 | 값 |
  | --- | --- |
  | OD-NET-010 신규 | Local-to-AWS Paper RDS SSM Port Forwarding 표준 경유지 |
  | 경유지 | `portfolio-paper-marketconnector-ec2` · instance id `i-0fce77927b7397b88` 단일 |
  | local port | `15433` |
  | RDS target | `portfolio-paper-rds:5432` |
  | Status | 🟠 **잠정** |
  | Decision Summary | 전체 72 → 73 · 잠정 27 → 28 |
  | OD-ENV-006 · OD-ENV-007 · OD-ENV-008 | 본문 변경 없이 실증 메모 보강 · Status 모두 잠정 유지 · aws-live cutover 시점에 확정 승격 후보 |
  | OD-NET-009 · R-SEC-001 · R-NET-004 · OD-DB-006 · OD-DB-007 | 본문 변경 없음 |

- 🟢 **`.kiro/specs/_common/followups-overview.md` 2026-06-13 SSM Port Forwarding 후속 메모**

  | 구분 | 내용 |
  | --- | --- |
  | 1차 완료 범위 | 로컬 도구 점검 · 표준 경유지 결정 · SSM Online · RDS endpoint · tunnel 오픈 · `portfolio_admin` / `execution_app` 접속 · Runbook 1차 본문 |
  | 결정 락 | OD-NET-010 |
  | 신규·보강 리스크 | R-AUTO-012 · R-AUTO-013 · R-DATA-007 detection 보강 |
  | 남은 후속 (6건) | psql client 정식 설치 · 모든 MS Paper mode DB 환경변수 점검 · Strategy Execution AWS 포팅 본 phase · MarketConnector executor EC2 배포 후보 · `READY → REQUESTED → SUBMITTED` end-to-end · SSM Port Forwarding session 자동 keep-alive |
  | 진행 순서 | 08 → 04 → 05 → 09 → 07 → 10 · 변경 없음 |

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | 변경 |
  | --- | --- |
  | R-DATA-007 | detection 보강 · 2026-06-13 SSM Port Forwarding 실증 · `inet_server_addr` / `inet_server_port` 출력 기반 환경 식별 정합성 점검 추가 · DB host 만으로 환경 식별 불가 실증 |
  | R-AUTO-012 신규 | Local-to-AWS Paper RDS SSM Port Forwarding tunnel 의존성 · tunnel 창 종료 · SSM session timeout · target EC2 stop · SSM Online 상실 시 DB 접속 즉시 단절 · mitigation = OD-NET-010 · Runbook §10.2 / §10.7 · 새 session 재기동 · Status 🟢 **Mitigated** |
  | R-AUTO-013 신규 | 운영자 로컬 PC PostgreSQL `psql` client 미설치 · PATH 미등록 · Python `psycopg2` 대체 사용 · 정식 보정은 02 spec runbook 부록 A · Status 🟢 **Mitigated** |

- 🟢 **`.kiro/WORKLOG.md`**

  | 항목 | 값 |
  | --- | --- |
  | 신규 세션 | 2026-06-13 SSM Port Forwarding 연결 검증 + Runbook 1차 본문 세 번째 |
  | 같은 일자 Strategy Decision · 책임 분리 섹션 | 본문 변경 없음 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret · token · password · RDS · KIS · 계좌 · KRX · account-id · IAM access key · secret ARN · image digest · EIP 평문 기록 | 0건 |
| placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · SSM · EC2 · RDS · GRANT 변경 작업 | 0건 |
| `secretsmanager:GetSecretValue` 실호출 | 0건 |
| broker · KIS · 주문 · 체결 · Daily Batch entrypoint 호출 | 0건 |
| `--execute` 실호출 | 0건 |
| RDS DDL/DML | 0건 |
| 사용 도구 | read-only AWS API + SSM Port Forwarding 세션 + Python `psycopg2` SELECT 조회만 |

<details><summary>🔵 운영 식별자 요약</summary>

- instance id `i-0fce77927b7397b88` · `i-0ff768ea639a91355`
- private IP `10.0.0.181` · `10.0.0.169` · `10.0.20.165`
- local port `15433`
- SSM session id `terraform-vjp3fv3nz73konetcevdzjh9de` (임시값 · 다음 session 재기동 시 재발급)
- RDS endpoint hostname `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com`

</details>

## 2026-06-13 (Strategy Execution / MarketConnector 책임 분리 + View Daily Batch 17단계 + Local-to-AWS Paper RDS 운영 원칙)

### Changed

- 🟢 **`.kiro/specs/03-marketconnector-ec2/operation-notes.md` (Strategy 주문 실행 executor 추가 §1~§6)**

  | 항목 | 값 |
  | --- | --- |
  | 책임 분리 배경 | Strategy Execution 의 connector 직접 호출 · `sys.path` 삽입 구조에서 책임 경계 분리 |
  | 신규 파일 | `connector_strategy_order_execute.py` (489 insertions) |
  | 기존 entrypoint 8종 | `connector_buy.py` · `connector_sell.py` · `connector_order_common.py` · `connector_order_check.py` · `connector_balance.py` · `db_config.py` · `config.py` · `token_manager.py` · 미변경 보장 |
  | 신규 executor 동작 | REQUESTED + `connector_order_request_id IS NULL` 조회 · SELL 우선·BUY 후순위 · 기본 dry run |
  | `--execute` 동작 | `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` guard · `connector_buy` / `connector_sell` lazy import · `SUBMITTED` / `FAILED` 갱신 · SELL position `SELL_ORDERED` 갱신 |
  | 정적 검증 | `python -m py_compile` 통과 · UTF-8 한글 정상 · dry run 결과 `[NO_TARGET] REQUESTED strategy order 없음` |
  | 본 일자 범위 밖 | 실제 `--execute` 실행 · EC2 배포 · 평일/안전 테스트 데이터 end-to-end |
  | 안전·보안 | KIS / broker API 호출 0건 · RDS DDL/DML 0건 · secret · 계좌 · RDS · KIS · token · account-id · ARN · instance-id · EIP 평문 0건 |

- 🟢 **`.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (책임 분리 + View 17단계 §1~§7)**

  | 섹션 | 내용 |
  | --- | --- |
  | §1 책임 분리 결정 | `--execute` 의미 변경 · `connector_order_request_id` 정책 · SELL `mark_position_sell_ordered()` 이관 |
  | §2 Strategy Execution 변경 | `execution_repository.py` · `daily_auto_buy_execute_run.py` · `daily_auto_sell_execute_run.py` 3개 파일 · 8개 변경 포인트 · `py_compile` 통과 · 직접 import / call 검색 0건 · 주말 가드로 dry run DB 후보 조회까지 미확인 |
  | §3 MarketConnector 신규 executor 인계 | 03 spec 정합 |
  | §4 View 17단계 변경 | `DailyBatchService.java` · `DailyBatchLabelUtils.java` 2개 파일 · 신규 step `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` (order 12) · 후속 step order 13~17 조정 · isNoTarget 5종 · 라벨 추가 · `.\mvnw.cmd clean compile` 성공 |
  | §5 Local 개발 / AWS Paper RDS 운영 원칙 | Paper source of truth = AWS Paper RDS 단일 · 환경 라벨 LOCAL_DEV·PAPER·LIVE · SSM Port Forwarding 방향 · 환경변수 예시 · 동기화 <span style="color:#D1242F">**금지**</span> 5건 |
  | §6 후속 인계 | 6건 |
  | §7 안전·보안 점검 | 결과 누적 |

- 🟢 **`.kiro/specs/02-aws-network-and-rds/operation-notes.md` (Local-to-AWS Paper RDS 운영 모드 §1~§6)**

  | 섹션 | 내용 |
  | --- | --- |
  | §1 Paper source of truth | AWS Paper RDS 단일 |
  | §2 환경 구분 라벨 | LOCAL_DEV · PAPER · LIVE |
  | §3 SSM Port Forwarding 방향 | 명문화 |
  | §4 예시 환경변수 | 명문화 |
  | §5 <span style="color:#D1242F">**금지**</span> 사항 | 5건 |
  | §6 후속 인계 | 명문화 |
  | RDS Public access · OD-NET-009 · R-SEC-001 · R-NET-004 | 본문 변경 없음 |
  | 04 spec §5 | Local-to-AWS Paper RDS 와 동기화 |
  | RDS endpoint · SSM session id · 비밀번호 평문 기록 | 0건 |

- 🟡 **`.kiro/specs/_common/operator-decisions.md` OD-MS-016 · OD-ENV-006 · OD-ENV-007 · OD-ENV-008 신규**

  | ID | 값 |
  | --- | --- |
  | OD-MS-016 | Strategy Execution / MarketConnector 주문 실행 책임 분리<br>Strategy Execution `--execute` = `READY -> REQUESTED`<br>MarketConnector executor `--execute` = `REQUESTED -> SUBMITTED`/`FAILED`<br>SELL `mark_position_sell_ordered()` 는 MarketConnector<br>`connector_order_request_id` 는 Strategy Execution 미생성·미갱신<br>🟠 **잠정** |
  | OD-ENV-006 | Paper 환경 DB source of truth = AWS Paper RDS 단일 · `PORT_ENVIRONMENT=paper` 이면 로컬 실행에서도 AWS Paper RDS 사용 · 🟠 **잠정** |
  | OD-ENV-007 | Local PC → AWS Paper RDS 접속 = SSM Port Forwarding 만 · RDS Private 유지 · paper 주문 실행 guard = `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` · 🟠 **잠정** |
  | OD-ENV-008 | Local DB ↔ AWS Paper RDS 동기화 미사용 · `connector_order_request` · `connector_fill` · `strategy_execution_order` · `strategy_position_state` 병합 <span style="color:#D1242F">**금지**</span> · <span style="color:#BF8700">**잠정**</span> |
  | Decision Summary | 전체 68 → 72 · 잠정 23 → 27 |
  | Change Log | 2026-06-13 두 번째 항목 |
  | 기존 결정값 (OD-NET-009 · OD-DB-003 · OD-MS-009 · OD-MS-013 등) | 본문 변경 없음 |

- 🟢 **`.kiro/specs/_common/followups-overview.md` 2026-06-13 책임 분리 + Local-to-AWS Paper RDS 후속 메모**

  | 구분 | 내용 |
  | --- | --- |
  | 1차 완료 범위 | Strategy Execution 3개 · MarketConnector 1개 신규 · View 2개 · Local-to-AWS Paper RDS 운영 원칙 5건 |
  | 결정 락 | OD-MS-016 · OD-ENV-006 · OD-ENV-007 · OD-ENV-008 |
  | 남은 후속 (6건) | SSM Port Forwarding runbook 정리 · 8개 MS 환경변수 점검 · Strategy Execution AWS 포팅 · MarketConnector executor EC2 배포 후보 · `READY → REQUESTED → SUBMITTED` end-to-end · 신규 View 17단계 운영 직전 end-to-end |
  | 진행 순서 | 08 → 04 → 05 → 09 → 07 → 10 · 변경 없음 |

- 🟢 **`.kiro/specs/_common/risk-register.md` 신규 리스크 4건**

  | ID | 내용 |
  | --- | --- |
  | R-DATA-007 신규 | local PostgreSQL ↔ AWS Paper RDS 사이 주문·체결·포지션·strategy execution 상태 데이터 병합·동기화 시 중복 주문·fill 누적·position 상태 충돌·source of truth 정합성 붕괴 · mitigation = OD-ENV-006 · OD-ENV-007 · OD-ENV-008 명문화 + paper guard + DB host 만으로 환경 식별 <span style="color:#D1242F">**금지**</span> · Status <span style="color:#BF8700">**Open**</span> |
  | R-AUTO-009 신규 | Strategy Execution `--execute` 후 MarketConnector executor `--execute` 를 실제 REQUESTED 주문 row 있는 상태에서 검증 못한 채 운영 진입 · mitigation = 본 일자 정적·dry run 까지만 · 평일/안전 테스트 데이터 end-to-end 후속 · 검증 전 `--execute` 실호출 <span style="color:#D1242F">**금지**</span> · View 신규 step paper 운영 활성화 보류 · Status <span style="color:#BF8700">**Open**</span> |
  | R-AUTO-010 신규 | MarketConnector executor `--execute` guard 가 잘못된 환경에서 우회되거나 환경변수 누락 시 통과 · 잘못된 broker 주문 위험 · mitigation = guard 코드 환경변수 누락 시 default `--execute` 거부 · 운영자 직접 환경변수 점검 · 비밀번호·계좌·endpoint 평문 출력 <span style="color:#D1242F">**금지**</span> · Status <span style="color:#BF8700">**Open**</span> |
  | R-AUTO-011 신규 | View 신규 step `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` (order 12) 삽입 · 12~17단계 구간 end-to-end 미검증 · mitigation = `.\mvnw.cmd clean compile` 까지만 · Daily Batch 실행 · Python 주문 스크립트 · DB · AWS 접근 0건 · 운영 직전 평일/안전 환경 end-to-end 후속 · 변경 이전 형상 git 보존 · Status 🟠 **Open** |

- 🟢 **`.kiro/WORKLOG.md`**

  | 항목 | 값 |
  | --- | --- |
  | 신규 세션 | 2026-06-13 책임 분리 / View 17단계 / Local-to-AWS Paper RDS 두 번째 |
  | Strategy Decision ECS / Fargate 1차 포팅 검증 섹션 | 본문 변경 없음 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret · token · password · RDS · KIS · 계좌 · SSM session id · KRX · account-id · ARN · image digest · IAM access key · instance-id · EIP · task ARN 평문 기록 | 0건 |
| placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · SSM · EC2 · RDS · GRANT 작업 | 0건 |
| `secretsmanager:GetSecretValue` 실호출 | 0건 |
| broker · KIS · 주문 · 체결 · Daily Batch entrypoint 호출 | 0건 |
| `--execute` 실호출 | 0건 |

<details><summary>🔵 운영자 직접 작성 변경 목록 (본문 인용 0건)</summary>

- Strategy Execution 3개 파일: `execution_repository.py` · `daily_auto_buy_execute_run.py` · `daily_auto_sell_execute_run.py`
- MarketConnector 1개 신규 파일: `connector_strategy_order_execute.py`
- View 2개 파일: `DailyBatchService.java` · `DailyBatchLabelUtils.java`
- 본 spec 산출물 · 운영 노트 · 본 changelog 에 사실만 기록

</details>

## 2026-06-13 (Strategy Decision ECS / Fargate 1차 포팅 검증 + 08 spec SSM 자동화 + ECS crawler smoke)

### Added

- 🟢 **`.kiro/specs/04-strategy-batch-stepfunctions/operation-notes.md` (Strategy Decision ECS §1~§10)**

  | 항목 | 값 |
  | --- | --- |
  | AWS 실행 구조 확인 | As-Is 분석 · 데이터 의존성 · To-Be 확정 (통합 `daily_decision_run.py` 보류 · Task Definition 2개 분리 · Docker build context `C:\Workspaces` · 이미지 안 `port_strategy_common` + `port_strategy_decision` vendoring) |
  | Dockerfile · requirements.txt | 운영자 직접 신규 |
  | 로컬 build + container smoke | `portfolio-strategy-decision:paper-20260613` |
  | ECR push | repository `portfolio-strategy-decision` · `paper-20260613` · `paper-latest` |
  | CloudWatch Log Group | `/portfolio/paper/strategy-decision` · retention 14일 |
  | Secrets Manager | `/portfolio/paper/rds/decision-app` JSON multi-key 신규 |
  | Execution Role · Task Role | decision-app secret read inline (ARN 한정 · wildcard 0건) + `portfolio-paper-decision-task-role` 신규 |
  | Task Definition 2개 | `portfolio-paper-strategy-decision-buy-signal` · `portfolio-paper-strategy-decision-position-signal` · awsvpc · Fargate · cpu 512 · memory 1024 · `INTEREST_DB_*` 5종 |
  | RunTask 실행 결과 | exitCode 0 · CloudWatch 로그 · RDS 접속 성공 · `daily_run_id` 44 · `data_date` 2026-06-08 · `market_signal` BLOCK · block_watch 1건 · position decision 0건 |
  | 1차 실패 · 조치 | (a) buy-signal `research.strategy_block_watch_candidate` 권한 부족 → 보정 (b) position-signal `relation "strategy_position_state" does not exist` (`execution.strategy_position_state` · `decision_app` 의 execution + decision schema 권한 부족) → 보정 |
  | 후속 인계 (7건) | EventBridge Scheduler · Step Functions state machine · Strategy Execution ECS 검증 · `port_strategy_common` 정식 package·version · `decision_app` 권한 매트릭스 · aws-live cutover · CI/CD OIDC |
  | 실제 secret · RDS · KIS · 계좌 · token · account-id · ARN · image digest · IAM access key · task ARN 평문 기록 | 0건 |

### Changed

- 🟡 **`.kiro/specs/_common/operator-decisions.md` OD-MS-013 · OD-MS-014 · OD-MS-015 신규**

  | ID | 값 |
  | --- | --- |
  | OD-MS-013 | `port_strategy_decision` Task Definition 분리 정책 = 통합 `daily_decision_run.py` 보류<br>Daily Batch 구조 계승<br>buy-signal / position-signal 별도 TD 2개<br>family `portfolio-paper-strategy-decision-buy-signal`<br>`portfolio-paper-strategy-decision-position-signal`<br>command `python -m port_strategy_decision.daily_buy_signal_run`<br>`python -m port_strategy_decision.daily_position_signal_run`<br>🟠 **잠정** |
  | OD-MS-014 | `port_strategy_common` 1차 배포 = 1차 ECS smoke image 에서 vendoring · 정식 package/version 관리는 Strategy Common 또는 DevOps 고도화 단계 후속 · 🟠 **잠정** |
  | OD-MS-015 | KRX GUI crawler 1차 자동화 = SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1`<br>SSM 이 wrapper 를 SYSTEM Session 0 / SessionId 0 에서 직접 실행하는 방식은 KRX GUI 로그인 부적합으로 채택 <span style="color:#D1242F">**거부**</span><br><span style="color:#BF8700">**잠정**</span> |
  | Decision Summary | 전체 65 → 68 · 잠정 20 → 23 |
  | Change Log | 2026-06-13 항목 2건 |
  | OD-MS-009 · OD-MS-012 | 본문 변경 없음 · 자동화 진입점 1단계는 OD-MS-015 로 분리 |
  | secret · endpoint · account-id · ARN · image digest 평문 기록 | 0건 |

- 🟢 **`.kiro/specs/_common/followups-overview.md`**

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-13 후속 메모 2건 | 04 spec (Strategy Decision ECS / Fargate 1차 검증) · 08 spec (SSM RunCommand 자동화 + ECS crawler smoke) |
  | 04 · 08 spec 본문 형식 | 03 · 08 spec 과 동일한 "2026-06-13 1차 적용 결과" 섹션 추가 |
  | 진행 순서 | 08 → 04 → 05 → 09 → 07 → 10 · 변경 없음 |

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | 변경 |
  | --- | --- |
  | R-DATA-005 | mitigation·detection·rollback·Affected Spec·Status 보강 (2026-06-13 04 spec 1차 적용 결과) |
  | R-AUTO-005 · R-AUTO-007 | mitigation·detection 보강 (2026-06-13 ECS crawler smoke + SSM 자동화 결과) |
  | R-AUTO-008 신규 | SSM RunCommand 가 SYSTEM Session 0 / SessionId 0 에서 KRX GUI wrapper 직접 실행 시 KRX 로그인 실패 · mitigation = OD-MS-015 의 Scheduled Task trigger 우회 · Status 🟢 **Mitigated** |
  | `decision_app` 권한 매트릭스 정식 정리 | 02 spec [db-roles-and-grants.md](../02-aws-network-and-rds/db-roles-and-grants.md) 후속 갱신으로 분리 |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` (SSM 자동화 + ECS crawler smoke §1~§8)**

  | 항목 | 값 |
  | --- | --- |
  | SSM Managed Node 점검 | 통과 |
  | SSM 직접 wrapper 실행 부적합 판단 | SessionId 0 vs 2 분리 |
  | Windows Scheduled Task | `Portfolio-KRX-Worker-Daily` 등록 + 1차 trigger 검증 |
  | RDP closed 상태 trigger | SSM RunCommand → `schtasks /Run` · `SUCCESS: Attempted to run the scheduled task` · Task State Running → Ready · 최신 로그 `krx_worker_daily_20260613_021904.log` · `[Collected Date] None` idempotent 정상 완료 |
  | ECS crawler smoke | `portfolio-paper-interest-crawler:6` · Selenium Chrome smoke RunTask · public-a/public-b + `sgroup-crawler-tasks` + `assignPublicIp=ENABLED` · exitCode 0 · `SELENIUM CHROME SMOKE SUCCESS` · `example.com` · Naver Finance (`Npay 증권`) 도달 · `DRIVER QUIT` · `SELENIUM CHROME SMOKE END` |
  | Hybrid execution model | 1차 완성 판단 |
  | 후속 인계 | 7건 |
  | 평문 기록 | secret · KRX 로그인 · RDS endpoint · account-id · ARN · image digest · task ARN · instance-id 0건 · KRX 로그인 ID / password 는 "Secrets Manager 에서 주입" 으로만 · wrapper · SSM 응답 · CloudWatch · Selenium · Chrome stdout·stderr 전문 평문 인용 0건 |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md`**

  | 항목 | 값 |
  | --- | --- |
  | task 30 · 31 부분 완료 | ECS crawler smoke 1차 통과 · non-GUI crawler 운영용 TD 분리는 task 58 후속 |
  | task 53 부분 완료 | SSM 직접 wrapper 실행 거부 · Scheduled Task trigger 방식 채택 |
  | §13 신규 task 61~69 | SSM Managed Node 점검 · SSM 직접 실행 부적합 사례 · Windows Scheduled Task 등록 · RDP closed SSM trigger · ECS crawler rev6 smoke command 분리 · Selenium Chrome smoke RunTask · non-GUI crawler 인벤토리 · Hybrid execution model 1차 완성 · 안전·문서 기록 점검 |
  | Task Dependency Graph · 이월 요약 | 2026-06-10 · 2026-06-12 · 2026-06-13 갱신 |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/design.md`**

  | 섹션 | 내용 |
  | --- | --- |
  | §13 신규 | "2026-06-13 운영자 검증 결과 / Hybrid execution model 1차 자동화 완성" |
  | §13.1 | KRX GUI 경로 1차 자동화 구조 · SSM RunCommand → `schtasks /Run` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1` · EventBridge Scheduler 후속 |
  | §13.2 | SYSTEM Session 0 직접 실행 부적합 판단 |
  | §13.3 | ECS crawler TD revision 6 = Selenium / Chrome / outbound smoke 전용 · 운영용 TD 분리는 task 58 |
  | §13.4 | Hybrid execution model 1차 완성 판단 6개 차원 |
  | §13.5 | 갱신 원칙 |
  | §1~§12 기존 결정값 | 변경 없음 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret · token · password · RDS · KRX · KIS · 계좌 · RDS endpoint · account-id · ARN · image digest · IAM access key · instance-id · task ARN 평문 기록 | 0건 |
| placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · SSM · EC2 · RDS · GRANT 작업 | 모두 운영자 직접 수행 · Kiro 는 문서·절차·검증 정리만 |
| `secretsmanager:GetSecretValue` 실호출 | 0건 |
| broker · KIS · 주문 · 체결 · Daily Batch entrypoint 호출 | 0건 |
| KRX 로그인 ID / password | "Secrets Manager 에서 주입" 으로만 표기 |
| 운영자 직접 작성 Dockerfile · requirements.txt · Scheduled Task · wrapper · 환경변수 주입 ps1 본문 인용 | 0건 (본 spec operation-notes 사실 기록) |

## 2026-06-12

### Changed

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` (Windows EC2 worker §1~§11)**

  | 항목 | 값 |
  | --- | --- |
  | EC2 worker 환경 확인 | 완료 |
  | RDS Secret DB env 주입 | `/portfolio/paper/rds/crawler-app` |
  | 다운로드 경로 junction 조치 | 완료 |
  | KRX program 수집 | 2026-06-09 · 2026-06-10 · 2026-06-11 각 1건 · `interest_program_raw` 543 → 546 |
  | KRX shortsell 수집 | 각 349건 · `interest_shortsell_raw` 189158 → 190205 |
  | 한글 literal · 인코딩 검증 | Python patch script 방식 결정 |
  | KRX Secrets Manager 연동 | `/portfolio/paper/krx/crawler-login` + IAM 권한 추가 |
  | EC2 worker daily wrapper | `run_krx_worker_daily.ps1` 1차 실행 + 재실행 idempotent no-op |
  | Hybrid execution model 분류 | KRX GUI = Windows EC2 worker · non-GUI = ECS Fargate Task 후보 유지 · preprocessor = ECS Fargate Task 유지 |
  | 후속 인계 | 8건 |
  | 평문 기록 | secret · KRX password · RDS endpoint · account-id · ARN · instance-id · image digest 0건 · KRX 로그인 ID/password 는 "Secrets Manager 에서 주입" 으로만 표기 |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md`**

  | 항목 | 값 |
  | --- | --- |
  | crawler runtime task 29 · 30 · 31 · 32 | KRX GUI = Windows EC2 worker 분리 확정 · non-GUI runtime 검증은 부분 완료 또는 보류 · workload 재분류 |
  | §11 신규 task 44~52 | EC2 worker 점검 · DB env 주입 · 다운로드 경로 junction · KRX program 수집 · KRX shortsell 수집 · 한글 인코딩 검증 · KRX Secrets Manager 연동 · wrapper 1차 실행 · Hybrid execution model 분류 결정 |
  | §12 후속 task 53~60 | SSM RunCommand · EventBridge Scheduler · Step Functions hybrid orchestration · CloudWatch Logs Agent · wrapper 내 DB 검증 출력 자동 추가 · non-GUI crawler 범위 재정리 · EC2 worker stop 절차 · KRX 수집 headless 리팩토링 장기 |
  | Task Dependency Graph · 이월 요약 | 2026-06-10 · 2026-06-12 갱신 |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/design.md` §12 신규**

  | 섹션 | 내용 |
  | --- | --- |
  | §12.1 Hybrid execution model 분류 표 | 신규 |
  | §12.2 KRX GUI EC2 worker 분리 이유 | Chrome GUI · download · login session · debug attach · KRX 사이트 특성 · Fargate GUI 미지원 |
  | §12.3 Secrets Manager 연동 방식 | RDS preprocessor-app · RDS crawler-app · KRX crawler-login · secret value 평문 기록 <span style="color:#D1242F">**금지**</span> |
  | §12.4 EC2 worker 운영 모드 | wrapper 기반 수동 실행 1차 운영 가능 · 자동화 후속 |
  | §12.5 갱신 원칙 | 명문화 |
  | §1~§11 기존 결정값 | 변경 없음 |

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/requirements.md`**

  | 항목 | 값 |
  | --- | --- |
  | R8 acceptance criterion 5 | 추가 · KRX GUI 의존 수집이 ECS Fargate 의 GUI/Chrome download/OTP 흐름과 호환되지 않는 경우 Windows EC2 worker 실행 · non-GUI crawler / KRX GUI crawler runtime 분리 (hybrid execution model) 명시 |
  | R1~R11 본문 | 변경 없음 |

- 🟡 **`.kiro/specs/_common/operator-decisions.md` OD-MS-011 · OD-MS-012 · OD-SEC-008 신규**

  | ID | 값 |
  | --- | --- |
  | OD-MS-011 | port-interest-crawler runtime = Hybrid · KRX GUI = Windows EC2 worker · non-GUI = ECS Fargate Task 후보 유지 · preprocessor = ECS Fargate Task 유지 · 🟠 **잠정** |
  | OD-MS-012 | KRX GUI crawler 1차 운영 모드 = wrapper 기반 수동 실행 · 🟠 **잠정** |
  | OD-SEC-008 | KRX 로그인 자격 = Secrets Manager `/portfolio/{env}/krx/crawler-login` JSON `username`/`password` · 🟠 **잠정** |
  | Decision Summary | 전체 62 → 65 · 잠정 17 → 20 |
  | Change Log | 2026-06-12 항목 |
  | 기존 결정값 (OD-NET-004 등) | 본문 변경 없음 |

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | 변경 |
  | --- | --- |
  | R-AUTO-005 | mitigation/detection/rollback 보강 (EC2 worker 1차 운영 가능 · wrapper 로그 패턴 모니터 · wrapper 재실행 즉시 복구) · Status `Open` → 🟢 **Mitigated** |
  | R-AUTO-006 신규 | EC2 worker 다운로드 경로 불일치 · junction 조치 · Status 🟢 **Mitigated** |
  | R-SEC-005 신규 | KRX 로그인 secret read 권한 누락 시 worker 기동 실패 · IAM Role inline policy + Resource·Action wildcard 0건 · Status 🟢 **Mitigated** |
  | R-DOCS-002 신규 | EC2 worker 안 password 평문 노출 위험 · length 점검 + grep 점검 · Status 🟢 **Mitigated** |
  | R-AUTO-007 신규 | wrapper 성공이 실제 DB 적재 성공 보장 못함 · wrapper 내 DB 검증 출력 자동 추가 후속 · Status 🟠 **Open** |

- 🟢 **`.kiro/specs/_common/followups-overview.md`**

  | 구분 | 내용 |
  | --- | --- |
  | 2026-06-12 후속 메모 | 08 spec Hybrid execution model 1차 운영 가능 상태 도달 (KRX program/shortsell EC2 worker 수집 성공 + 다운로드 경로 junction + RDS/KRX Secret 주입 + wrapper) |
  | 분류 결정 | KRX GUI = Windows EC2 worker · non-GUI = ECS Fargate Task 후보 유지 · preprocessor = ECS Fargate Task 유지 |
  | 후속 분리 (8건) | SSM RunCommand · EventBridge Scheduler · Step Functions hybrid orchestration · CloudWatch Logs Agent · wrapper 내 DB 검증 출력 자동 추가 · non-GUI crawler 범위 재정리 · EC2 worker stop 절차 · KRX 수집 headless 리팩토링 장기 |
  | 진행 순서표 | 변경 없음 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret · token · password · KRX 로그인 · KIS · 계좌 · RDS endpoint · account-id · ARN · image digest · IAM access key · instance-id 평문 기록 | 0건 |
| placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| KRX 로그인 ID / password | "Secrets Manager 에서 주입" 으로만 기록 |
| password rotate | 본 일자 작업 범위 밖 |
| 운영자가 EC2 내부에 직접 생성한 ps1 / py / wrapper 파일 본문 인용 | 0건 (operation-notes 사실 기록) |

## 2026-06-10

### Added

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/operation-notes.md` 신규 생성**

  | 항목 | 값 |
  | --- | --- |
  | ECR repository | 2개 생성 |
  | Dockerfile · requirements.txt | port-interest-preprocessor · port-interest-crawler 신규 |
  | 로컬 빌드 | `paper-20260610` · `paper-latest` |
  | ECR push | 완료 |
  | ECS Cluster | `portfolio-paper-cluster` |
  | Task Execution Role · Task Role | 2종 |
  | CloudWatch Log Group | 2종 · retention 14일 |
  | Secrets Manager | `/portfolio/paper/rds/preprocessor-app` JSON multi-key 신규 |
  | preprocessor Task Definition | family `portfolio-paper-interest-preprocessor` · awsvpc · cpu 512 · memory 1024 · image `paper-20260610` |
  | preprocessor RunTask 1차 검증 | lastStatus `STOPPED` · exitCode 0 · `PREPROCESSOR PIPELINE END` |
  | 1차 실패 | Secrets JSON `host` 누락 → Unix socket 시도 |
  | 2차 실패 | public schema 잔존 sequence 2건 권한 부족 |
  | crawler runtime 검증 | 이월 |
  | 평문 기록 | secret · endpoint · account-id · ARN · image digest 0건 |

### Changed

- 🟢 **`.kiro/specs/08-interest-crawler-and-preprocessor-ecs/tasks.md`**

  | 항목 | 값 |
  | --- | --- |
  | 체크 완료로 갱신 | ECR repository 생성 · Dockerfile · requirements · 로컬 빌드 · ECR push · ECS Cluster · Role · Log Group · Secrets Manager · Task Definition · Preprocessor RunTask · CloudWatch Logs · exit code 0 |
  | crawler build / push | 체크 완료 |
  | task 30 (crawler runtime outbound 검증) · task 31 (crawler runtime 도달 검증) | 이월 표기 |
  | 실패 원인 반영 | task 28 (Secrets JSON `host` 누락) · task 26 (public schema 잔존 sequence 권한 부족 2건) |
  | 본 spec 안전 제약 (35~40) | Kiro 본 spec 작업 기준 0건 유지 · 운영자 직접 작업은 operation-notes 참조 |

- 🟢 **`.kiro/specs/_common/risk-register.md`**

  | ID | 변경 |
  | --- | --- |
  | R-DATA-005 | mitigation/detection 보강 · public schema 잔존 sequence 권한 누락 사례 + ECS Task event `permission denied for sequence` 패턴 모니터 |
  | R-DOCS-001 | detection 보강 · secret value 작업 채팅 · 명령 출력 · 콘솔 캡처 · CloudWatch Logs 비노출 운영 점검 |
  | R-DATA-006 신규 | Secrets Manager JSON multi-key 의 `host` key 누락으로 ECS Task 가 Unix socket `/var/run/postgresql/.s.PGSQL.5432` 시도 |
  | R-AUTO-005 신규 | crawler Selenium · Chromium · chromedriver runtime · KRX · Naver · yfinance outbound 도달 미검증 상태로 운영 진입 |
  | 원칙 | 신규 항목 모두 password rotate 를 mitigation 으로 강제하지 않음 · secret value 비노출 · runtime 검증 후속 분리 |

- 🟢 **`.kiro/specs/_common/followups-overview.md`**

  | 항목 | 값 |
  | --- | --- |
  | 2026-06-10 후속 메모 | 08 preprocessor ECS Task 1회 실행 성공 (lastStatus `STOPPED` · exitCode 0) 반영 |
  | 08 spec 섹션 | 2026-06-10 1차 적용 결과 (완료 범위 / 범위 밖 / 후속 spec 인계) 추가 |
  | 이월 항목 | crawler Task Definition / RunTask runtime 검증 · public schema 잔존 sequence 정리 |

- 🟡 **`.kiro/specs/_common/operator-decisions.md`**

  | 항목 | 값 |
  | --- | --- |
  | Change Log OD-NET-004 메모 | crawler / preprocessor outbound = public subnet + assignPublicIp · 2026-06-10 preprocessor RunTask 1차 검증 |
  | Status | 🟠 **잠정** 유지 (crawler runtime 검증 이월) |
  | 결정값 (선택지·선택값·비용·리스크·후속 spec 영향) | 변경 없음 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret · token · password · KIS · 계좌 · webhook URL · RDS endpoint · account-id · ARN · image digest · IAM access key 평문 기록 | 0건 |
| placeholder | `[REDACTED]` · `<account-id>` · `<region>` · `<rds-endpoint>` · `<image-tag>` · `<image-digest>` · `<task-arn>` |
| password rotate | 본 일자 문서 작업 범위 밖 |
| secret value 노출 원칙 | 후속 작업에서도 동일 유지 (R-DOCS-001 정합) |

## 2026-06-09

### Added

- 🟢 **02 spec 보조 문서 2종 신규 생성**

  | 파일 | 역할 |
  | --- | --- |
  | `.kiro/specs/02-aws-network-and-rds/README.md` | 운영자용 한 장 요약 · 현재 상태 · 다음 작업 · 완료 AWS 리소스 · 관련 문서 링크 · 자동 갱신 기준 · 결정값 · RDS endpoint hostname · secret value 미기록 |
  | `.kiro/specs/02-aws-network-and-rds/db-roles-and-grants.md` | DB Role · 권한 분리 보조 · 7개 app role 설계 · 권한 매트릭스 · search_path 전략 · GRANT · ALTER ROLE · DEFAULT PRIVILEGES SQL 초안 · 검증 SQL · rollback 절차 · legacy 미부여 · marketconnector_app execution R-only 축소 명시 |

### Changed

- 🟢 **`.kiro/specs/02-aws-network-and-rds/operation-notes.md` 2026-06-09 섹션 추가**

  | 항목 | 값 |
  | --- | --- |
  | RDS Restore 경로 | 로컬 → S3 → EC2 → RDS |
  | dump 크기 | 422,334,494 bytes · TOC 812 |
  | dump source | PostgreSQL 18.1 |
  | RDS 버전 변경 | 16.14 → 18.4 재생성 |
  | 1차 오류 | `role "postgres" does not exist` |
  | 재실행 | `--no-owner --no-privileges` · 정합성 diff 0 |
  | DB Role / 권한 분리 1차 적용 | 7개 app role · `portfolio_owner` 도입 · 9개 도메인 schema owner 이관 · 기존 table/sequence/index owner 는 `portfolio_admin` 유지 · `REASSIGN OWNED` 미실행 · GRANT 매트릭스 · DEFAULT PRIVILEGES · search_path 적용 · 3개 role 접속 검증 성공 |

- 🟢 **`.kiro/specs/02-aws-network-and-rds/validation-checklist.md`**

  | 항목 | 값 |
  | --- | --- |
  | §6 DB/schema/role 준비 5건 | `[운영자 확인 필요]` → `[O]` 격상 |
  | §11 DB Role 권한 매트릭스 Validation | 신규 · 13개 라벨 · `[O]` 11 · `[운영자 확인 필요]` 2 |
  | RDS Restore 검증 (schema · table · index · sequence · FK · trigger · row count 일치) | `[O]` 정리 |

- 🟢 **`.kiro/specs/02-aws-network-and-rds/runbook.md`**

  | 항목 | 값 |
  | --- | --- |
  | 보강 섹션 | "RDS Restore Runner & DB Role / 권한 적용 교훈" |
  | 내용 | major version mismatch 회피 · private RDS 는 EC2 / SSM 경유 · dump role mismatch 시 `--no-owner --no-privileges` · schema-only owner 이관 + REASSIGN 분리 |
  | 결정값 · 기존 Step 번호 | 변경 없음 |

- 🟢 **`.kiro/specs/_common/risk-register.md` 신규 리스크 4건**

  | ID | 내용 |
  | --- | --- |
  | R-DATA-003 | PostgreSQL major version mismatch 로 restore 실패 |
  | R-DATA-004 | dump owner role 과 RDS role 불일치로 restore 실패 |
  | R-NET-004 | private RDS 직접 접속 불가 · EC2 / SSM restore runner 의존 |
  | R-DATA-005 | app role 최소 권한 적용 후 view / execution / marketconnector 권한 경계 검증 필요 |

- 🟢 **`.kiro/specs/_common/operator-decisions.md` OD-DB-007 ~ OD-DB-010 신규**

  | ID | 내용 |
  | --- | --- |
  | OD-DB-007 | legacy schema 는 모든 app role 미부여 |
  | OD-DB-008 | marketconnector_app execution R-only |
  | OD-DB-009 | view_app execution R-only (write 는 후속 spec 재검토) · OD-DB-005 는 OD-DB-009 로 분리 · 본문 유지 · Status 유지 · 중복 row 미생성 |
  | OD-DB-010 | 1차 적용에서 `REASSIGN OWNED BY portfolio_admin TO portfolio_owner` 미실행 |

- 🟢 **`.kiro/specs/_common/followups-overview.md`**

  | 항목 | 값 |
  | --- | --- |
  | 진행 순서 | 2026-06-10 후속 메모 추가 |
  | `03-marketconnector-ec2` 항목 | MarketConnector 기본 포팅 이월 작업 · ECS 기본 포팅 의존성 명시 |
  | 링크 | 03 spec 입력으로 사용할 RDS 1차 적용 결과 |

- 🔵 **범위 밖 · 실행 없음**

  | 항목 | 값 |
  | --- | --- |
  | 코드 / AWS 리소스 / SQL 실행 변경 | 없음 |
  | EC2 신규 생성 · 기존 RDS 삭제 · 재생성 · SQL 적용 | 운영자 직접 진행 · 본 변경은 결과 기록만 |
  | 8개 MS 코드 · README · AGENTS.md · CHANGELOG · docs · worklog 수정 | 없음 |

- 🟢 **같은 날짜 사전 변경 (폴더 이동 + 링크 보정)**

  | 항목 | 값 |
  | --- | --- |
  | 루트 공통 문서 6종 이동 | `operator-decisions.md` · `ms-aws-service-decision-matrix.md` · `cost-simulation.md` · `followups-overview.md` · `aws-resource-glossary.md` · `risk-register.md` → `.kiro/specs/_common/` · 파일명·본문 변경 없음 |
  | 아카이브 이동 | `.kiro/specs/note-aws-landscape-2021-vs-2026.md` → `.kiro/specs/_archive/` · 파일명·본문 변경 없음 |
  | `.kiro/README.md` 갱신 | 폴더 구조 예시 · 루트 공통 문서 링크 경로를 `_common/` · `_archive/` 로 |
  | `.kiro/AGENTS.md` 갱신 | 루트 참조 문서 섹션 경로 표기 · 작업 규칙 의미는 변경 없음 |
  | 링크 보정 | `.kiro/CHANGELOG.md` · `.kiro/WORKLOG.md` · `.kiro/docs/kiro-readonly-validator-iam.md` · `01-aws-migration-foundation/*.md` · `02-aws-network-and-rds/*.md` · `../X.md` → `../_common/X.md` · `specs/X.md` → `specs/_common/X.md` |

### Security

| 항목 | 결과 |
| --- | --- |
| secret · token · password · app key · app secret · 계좌번호 · webhook URL · RDS endpoint hostname · account-id · access key id 평문 기록 | 0건 |
| placeholder | `[REDACTED]` 만 사용 |

## 2026-06-06

### Added

- 🟢 **02 spec 1차 적용 보조 문서 4종 + 리스크·용어 보강**

  | 파일 | 역할 |
  | --- | --- |
  | `.kiro/specs/_common/risk-register.md` | AWS Migration 운영·보안·비용 리스크 단일 누적 표 · R-NET-001 ~ R-COST-002 12개 등록 · 같은 일자 후속으로 R-SEC-002 (Root 보안) · R-SEC-003 (portadmin 자격 분실) 2건 추가 누적 |
  | `.kiro/specs/01-aws-migration-foundation/traceability-matrix.md` | 01 spec Requirement → Design → Task → Decision 매핑 |
  | `.kiro/specs/02-aws-network-and-rds/runbook.md` | 운영자용 실행 절차서 · 1차 = 18 Step (Region ~ Rollback) · 같은 일자 후속으로 신규 Step 0 `IAM 관리자 사용자 portadmin 생성` 추가 → 총 19 Step (Step 0 ~ Step 19) 재구성 |
  | `.kiro/specs/02-aws-network-and-rds/validation-checklist.md` | 02 spec runbook 통과 여부 체크박스 · 10개 섹션 (Pre-flight · Network · SG · Endpoint · RDS · DB · Cutover · Cost · Docs · Rollback) |
  | `.kiro/specs/02-aws-network-and-rds/traceability-matrix.md` | 02 spec Requirement → Design → Task → Validation → Decision 매핑 + Risk 매핑 |
  | `.kiro/specs/_common/aws-resource-glossary.md` | IAM User 용어 항목 신규 · portadmin 같은 IAM 관리자 사용자 개념이 02 runbook 에서 본격 사용되기 시작한 것 반영 |

### Changed

- 🟢 **`.kiro/README.md`**

  | 항목 | 값 |
  | --- | --- |
  | 신규 보조 문서 역할 안내 | runbook · validation-checklist · traceability-matrix · risk-register |
  | 폴더 구조 | 갱신 |

- 🟢 **`.kiro/specs/02-aws-network-and-rds/runbook.md`**

  | 항목 | 값 |
  | --- | --- |
  | 실행 구분 라벨 | 모든 Step 제목에 `[실행]` · `[확인]` · `[준비]` · `[복구]` 추가 |
  | 라벨 범례 | 본문 상단 신설 |
  | Step 재번호 | Step 0 (IAM portadmin) 추가에 따라 기존 Step 0 ~ 18 → Step 1 ~ 19 일괄 재번호 |
  | cross-reference | `Step N 완료` · `Step N 에서 다시 결정` 등 모두 +1 갱신 |
  | 결정값 · Console 클릭 경로 · 입력값 · 검증 · rollback 순서 | 변경 없음 |

- 🟢 **`.kiro/specs/02-aws-network-and-rds/validation-checklist.md` · `traceability-matrix.md`**

  | 파일 | 변경 |
  | --- | --- |
  | validation-checklist.md | runbook Step 재번호에 맞춰 `Step 18 rollback` → `Step 19 rollback` |
  | traceability-matrix.md | 매핑 표 (Requirement · Acceptance Criteria 보강 · Risk 매핑) 안의 runbook Step 참조 숫자 모두 +1 일괄 갱신 |

### Security

| 항목 | 결과 |
| --- | --- |
| secret · token · password · app key · app secret · 계좌번호 · webhook URL 평문 기록 | 0건 |
| placeholder | `[REDACTED]` 만 사용 |
| portadmin 비밀번호 · MFA 시리얼 · 백업 코드 · Root MFA 시리얼 | 본 작업공간 어떤 문서에도 <span style="color:#D1242F">**금지**</span> · 운영자 외부 안전한 위치 별도 보관 · 문서는 `[REDACTED]` |

## 2026-06-05

### Added

- `.kiro/README.md`를 추가하여 AWS Migration spec 작업공간의 목적과 문서 구조를 정리했다.
- `.kiro/CHANGELOG.md`를 추가하여 Kiro spec 문서 변경 이력 관리 기준을 만들었다.
- `.kiro/WORKLOG.md`를 추가하여 간단 작업 로그를 하나의 파일에 누적하는 방식으로 정리했다.

### Changed

- `.kiro/AGENTS.md`에 README / CHANGELOG / WORKLOG 관리 규칙을 추가했다. 단, 기존 작업 규칙(범위, 단일 기준 문서, MS별 AGENTS.md 참조, spec 작성, 보안, 실행 규칙)의 의미는 변경하지 않았다.
- `.kiro/specs/_common/operator-decisions.md`의 구조를 운영자 가독성 중심으로 재정리했다. 실제 결정값(Decision ID, 선택지, 선택값, 비용 영향, 운영 리스크, 후속 spec 영향)은 변경하지 않았고, Status 표시만 한글/색상 라벨(🟢 확정 · 🟡 잠정 · 🔴 미정 · 🔵 보류)을 함께 사용하도록 개선했다. Status Legend, Decision Summary, At a Glance, 카테고리별 상세 결정표, Open · Tentative · Deferred 결정 모음, Decision Update Rules, Change Log 섹션을 추가했다.

### Security

- secret, token, password, app key, app secret, 계좌번호, webhook URL은 본 작업공간 문서에 절대 기록하지 않고 `[REDACTED]`로만 표기한다는 원칙을 재확인했다.
