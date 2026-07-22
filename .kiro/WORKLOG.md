# Kiro AWS Migration Worklog

본 문서는 `.kiro` 작업공간의 간단 작업 로그다. 각 MS의 `docs/worklog/YYYY-MM-DD.md`처럼 상세하게 쓰지 않으며, 날짜별로 별도 파일을 만들지 않고 본 단일 파일에 누적한다.

## 작성 원칙

- 날짜별 섹션을 본 파일 하나에 누적한다.
- 각 날짜는 `Summary → Completed → Evidence → Risks → Follow-ups → Security` 순서를 기본으로 한다.
- 완료 사실과 검증 결과는 가능한 한 `항목 / 값` 2열 표로 정리한다.
- 긴 셀은 여러 행으로 나누고, raw log·전체 SQL·전체 AWS 응답은 `operation-notes.md`로 분리한다.
- 실제 AWS 실행 주체, 애플리케이션 수정 여부, 8개 MS 문서 수정 여부와 민감정보 기록 여부를 명시한다.

## 2026-07-22 (Paper Daily 정상 자동 회차 end-to-end 성공 · P1 acceptance 완료 · Paper Daily 1차 안정화 완료 · P2 View 고도화 미수행 결정)

### 🧭 Summary

2026-07-22 정상 Scheduler 자동 회차에서 Paper Daily Step 1~11 과 Step 12~17 이 강제 주문·오류 유발 없이 모두 SUCCEEDED 로 완료됐다.
Crawler DB 검증, Daily Run COMPLETED, Execution Plan READY, READY Plan → Order validator 자동 통과, 실제 주문 회차 Order Chain validator 자동 통과가 확인됐다.
한국전력 83주 주문·체결·Position 반영, Balance Snapshot 생성, 성공 Slack 자동 수신, OPS 성공 기록까지 확인됐다.
이 회차를 근거로 기존 P1 후속 항목인 다음 정상 자동 회차 end-to-end 관찰을 완료 처리하고 Paper Daily 를 1차 안정화 완료로 선언한다. 단, 이는 모든 운영 리스크 해결, aws-live 준비 완료, 장기 무장애 운영 검증, 전체 AWS Migration 완료를 의미하지 않는다.
별도로 운영자는 P2 View 운영 보안·표시 고도화(ALB · HTTPS · Route53 · 인증 · Auto Scaling · Blue/Green · View 표시 고도화 · 외부 공개 운영)를 현재 포트폴리오 범위에서 수행하지 않기로 결정했다. 실패나 미완료가 아니라 의도적인 범위 제외이며 외부 공개 또는 다중 사용자 운영이 필요할 때 재검토한다.
Kiro 는 본 회차에서 문서만 수정했고 실제 운영 확인은 운영자가 수행했다. 실제 AWS·DB·broker·KIS·Slack·ECS·EC2·Lambda·Step Functions 실행은 0건이다.

### ✅ Completed

- 🟢 Step 1~11 정상 Scheduler 자동 실행 (운영자 직접 확인)

| 항목 | 값 |
| --- | --- |
| Scheduler 상태 | ENABLED |
| 실행 시각 | 2026-07-22 08:00 KST |
| 실행 결과 | SUCCEEDED |
| 종료 시각 | 2026-07-22 08:21 KST |
| 실행 방식 | 강제 실행·오류 유발 없는 정상 회차 |

- 🟢 Crawler 데이터 검증

| 항목 | 값 |
| --- | --- |
| Program 최신 거래일 | 2026-07-21 |
| Program 데이터 | 1건 |
| Shortsell 최신 거래일 | 2026-07-21 |
| Shortsell 데이터 | 349건 |
| 검증 기준 | Program 정확히 1건 · Shortsell 최소 300건 |
| 결과 | 정상 |

- 🟢 Daily Run · Execution Plan

| 항목 | 값 |
| --- | --- |
| Daily Run 날짜 | 2026-07-22 |
| 데이터 기준일 | 2026-07-21 |
| Daily Run 상태 | COMPLETED |
| Market Signal | AGGRESSIVE |
| 후보 수 | 1건 |
| Signal 수 | 1건 |
| Execution Plan 상태 | READY |
| 주문 준비 대상 | 1건 |
| 차단·스킵 대상 | 0건 |

- 🟢 Step 12~17 정상 Scheduler 자동 실행 (운영자 직접 확인)

| 항목 | 값 |
| --- | --- |
| 실행 시각 | 2026-07-22 09:01 KST |
| 실행 결과 | SUCCEEDED |
| 종료 시각 | 2026-07-22 09:06 KST |
| 실행 방식 | 정상 Scheduler 자동 회차 |
| READY Plan → Order validator | 자동 통과 |
| Order Chain validator | 자동 통과 |

- 🟢 주문·체결·Position·Balance after-check

| 항목 | 값 |
| --- | --- |
| 종목 | 한국전력 (015760) |
| 주문 방향 | BUY |
| 주문 수량 | 83주 |
| Execution Order | FILLED |
| Connector Order Request | FILLED |
| Fill 수량 | 83주 |
| Fill 건수 | 1건 |
| Position 상태 | OPEN |
| Position 수량 | 83주 |
| Balance Snapshot | 2026-07-22 생성 완료 |

- 🟢 Slack · OPS 기록

| 항목 | 값 |
| --- | --- |
| Approval Slack | Step 1~11 자동 경로에서 정상 발송 |
| Daily 성공 Slack | Step 12~17 자동 경로에서 정상 수신 |
| 체결 표시 | 한국전력 83주 매수 체결 표시 확인 |
| Workflow Step 기록 | 성공 기록 확인 |
| Batch 기록 | 성공 기록 확인 |

- 🟢 완료 판단·선언

| 항목 | 값 |
| --- | --- |
| P1 acceptance | 다음 정상 자동 회차 end-to-end 관찰 완료 |
| Paper Daily | 1차 안정화 완료 선언 |
| 확대 해석 금지 | 전체 리스크 해결·aws-live 준비·장기 무장애·전체 Migration 완료 아님 |

- 🟢 P2 View 운영 보안·표시 고도화 미수행 결정 (운영자 결정)

| 항목 | 값 |
| --- | --- |
| P2 상태 | 미수행 |
| 현재 범위 | ECS Fargate 1차 실증 상태 유지 |
| 사유 | 개인 운영·포트폴리오 시연 목적 · 외부 미공개 |
| 결정 성격 | 의도적인 범위 제외 (실패·미완료 아님) |
| 재검토 조건 | 외부 공개 또는 다중 사용자 운영 필요 시 |

### 🔵 Evidence

| 항목 | 값 |
| --- | --- |
| README | Current Status Dashboard Paper Daily Step 1~11 · Step 12~17 · port-view row |
| 후속 | [followups-overview](specs/_common/followups-overview.md) Now → Done recently 2026-07-22 |
| 결정 | [operator-decisions](specs/_common/operator-decisions.md) OD-SAFE-001 · OD-MS-002 |
| 상세 실행 근거 | [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| 신규 Risk ID | 없음 (Risk 76건 · 상태 집계 보존) |
| 상태 변경 | 없음 (정상 회차 성공은 기존 mitigation 재확인 · 위험 제거 아님) |
| P2 관련 Risk | 위험 시나리오 유지 시 Open 유지 · 임의 Mitigated/Closed 변경 없음 |

### 📌 Follow-ups

- [ ] 장기 무장애 자동 회차 누적 관찰 (1차 안정화 이후 지속 관찰)
- [ ] OPS Mirror 세부 Step 확장 · 자동 실행 run 대 수동 복구 run 구분
- [ ] Slack 예외 케이스 검증 확대 (승인·성공·실패·손절 경로)
- [ ] P2 재검토 조건 충족(외부 공개·다중 사용자) 시 View 운영 보안·표시 고도화 재검토

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS · DB · broker · KIS · Slack · ECS · EC2 · Lambda · Step Functions 명령 | Kiro 실행 0건 · 운영자 직접 수행 |
| State Machine ARN · execution ARN · ECS Task ARN · account-id · 계좌번호 · broker 주문번호 · SHA256 원문 신규 기록 | 0건 |
| git add · commit · push · reset · restore | 0건 |
| placeholder 정책 | `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_BROKER_ORDER_NO]` 계열만 사용 |
| 저장 인코딩 | UTF-8 No BOM 유지 |

## 2026-07-21 (AWS Migration 공통 문서 최종 정합 보완)

### 🧭 Summary

`_common` 공통 문서 5종의 잔존 불일치를 문서 정합 관점에서만 보완했다. SSM VPC Endpoint 표현을 실제 운영 구조(S3 Gateway + ECR/Secrets/Logs Interface 기본 세트 · SSM은 Public outbound 또는 Interface Endpoint 선택형)로 통일하고, Crawler 현재 Hybrid 운영과 장기 목표 표현을 통일했으며, followups orchestration 문구와 죽은 참조를 정리했다.
본 작업은 문서 정합 수정이며 실제 AWS 리소스 변경이 아니다. Kiro 는 문서만 수정했고 실제 AWS·DB·broker·KIS·Slack 실행은 0건이다.

### ✅ Completed

| 항목 | 값 |
| --- | --- |
| operator-decisions.md | OD-NET-005 선택값을 기본 세트 + SSM 선택형으로 정합 |
| operator-decisions.md | OD-SEC-007 SSM outbound 경로(Public 또는 Endpoint) 정합 |
| operator-decisions.md | OD-MS-027~040 죽은 `See details` 꼬리 문구 제거 |
| operator-decisions.md | OD-MS-003(목표) · OD-MS-011(현재 Hybrid) 관점 구분 |
| risk-register.md | R-NET-003 위험·대응을 접근 경로/Endpoint 세트 불일치로 정합 |
| aws-resource-glossary.md | VPC Endpoint Gateway/Interface 구분 · SSM 선택형 정합 |
| ms-aws-service-decision-matrix.md | Network and Data · Crawler 카드 정합 |
| followups-overview.md | Historical Notes 잔재 제거 · Daily orchestration 문구 갱신 |

### 🔵 Evidence

| 항목 | 값 |
| --- | --- |
| 결정 | [operator-decisions](specs/_common/operator-decisions.md) |
| 리스크 | [risk-register](specs/_common/risk-register.md) |
| 후속 | [followups-overview](specs/_common/followups-overview.md) |

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| 신규 Risk ID | 없음 (Risk 76건 · 상태 집계 보존) |
| 신규 Decision ID | 없음 (Decision 100건 · 상태 집계 보존) |

### 📌 Follow-ups

- [ ] 현재 aws-paper에서 SSM Session Manager가 SSM Interface Endpoint 없이 어떤 outbound 경로로 정상 동작하는지 운영자가 실제 네트워크 구성으로 최종 확인

### 🔐 Security

문서만 수정, 실행 0건, broker 주문 0건, aws-live 작업 0건, secret 원문 기록 0건. SSM Endpoint 정책 변경은 문서 정합 수정이며 실제 AWS 리소스 변경 아님. git 명령 실행 0건. UTF-8 No BOM · 2열 표 정합 · 300자 초과 줄 없음 확인.

## 2026-07-21 (정상 무주문 회차 validator 실패 · `--allow-no-target` 보완)

### 🧭 Summary

2026-07-21 09:01 자동 실행된 State Machine `portfolio-paper-daily-step12-17-approval` 이 Step 12~16 을 모두 통과한 뒤 최종 `P0_ValidateOrderChain` 에서 FAILED 로 종료됐다.
실패 원인은 실제 주문 체인 불일치나 주문 제출 실패가 아니라 2026-07-21 `strategy_execution_order` 실제 생성 건수가 0건인 정상 무주문 회차를 후단 Order Chain validator 가 실패로 판정한 설정 누락이었다.
로컬 `execution_validate_order_chain.py` 에는 이미 `--allow-no-target` 무주문 성공 처리 기능이 있었으나 운영 State Machine 의 `P0_ValidateOrderChain` ECS command 에 인자가 누락돼 있었다.
운영자가 State Machine command 에 `--allow-no-target` 를 추가하고 ASL 검증·배포 후 재조회·validator 단독 ECS smoke 로 정상 무주문 회차의 성공 처리를 확인했다. Python 소스·이미지·Task Definition 변경과 전체 Step 12~17 재실행은 없다. Kiro 는 본 회차에서 문서만 수정하고 운영 작업은 운영자가 수행했다.

### ✅ Completed

- 🔴 09:01 Step 12~17 자동 실행 실패 (운영자 직접 확인)

| 항목 | 값 |
| --- | --- |
| State Machine | `portfolio-paper-daily-step12-17-approval` |
| Step 12~16 | 모두 성공 |
| 실패 State | `P0_ValidateOrderChain` (최종 validator) |
| 자동 execution 최종 상태 | FAILED |
| ECS 컨테이너 종료 | ExitCode 1 |
| Task Definition | `portfolio-paper-strategy-execution:3` |
| 실행 이미지 | `paper-20260720-p0-integrity-v1` |
| 실패 Slack | `DAILY_EXECUTION_FAILED` 정상 수신 |

- 🟠 실패 원인 확인 (운영자 직접 확인)

| 항목 | 값 |
| --- | --- |
| 실제 주문 생성 | `strategy_execution_order` 0건 |
| 조회 결과 | signal_date_count=0 · created_kst_count=0 · current_validator_target_count=0 |
| validator 로그 | target_count=0 · error_count=0 · ORDER_CHAIN_VALIDATION=FAILED |
| reason | NO_EXECUTION_ORDER_TARGET |
| 직접 원인 | 정상 무주문 회차를 후단 Order Chain validator 가 실패로 판정 |
| 설정 누락 | 운영 State Machine `P0_ValidateOrderChain` command 에 `--allow-no-target` 미전달 |

- 🟢 수정 및 배포 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 변경 전 command | `... 'execution_validate_order_chain.py', '--run-date', $.runDate` |
| 변경 후 command | `... 'execution_validate_order_chain.py', '--run-date', $.runDate, '--allow-no-target'` |
| Python 소스 | 변경 없음 |
| ECS 이미지 재빌드 | 없음 |
| Task Definition 변경 | 없음 |
| 기존 정의 백업 | 로컬 백업 |
| 수정 정의 인코딩 | UTF-8 No BOM |
| ASL 검증 | validate-state-machine-definition OK |
| 배포 후 확인 | 재조회로 `--allow-no-target` 반영 확인 |

- 🟢 validator 단독 smoke (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 방식 | read-only validator 단독 ECS smoke |
| smoke 기준일 | 2026-07-21 |
| smoke 결과 | ORDER_CHAIN_VALIDATION=SUCCESS target=0 errors=0 |
| 컨테이너 | ExitCode 0 |
| fail-closed 유지 | 실제 주문 있는 회차 Plan→Order→Request→Fill→Position 검증 유지 |
| 전체 Step 12~17 재실행 | 없음 |
| Step 12 주문 제출 재실행 | 없음 |
| 09:01 자동 execution 이력 | FAILED 보존 (성공으로 덮어쓰지 않음) |

### 🔵 Evidence

- 🔵 실제 State Machine ARN · execution ARN · ECS Task ARN · account-id · SHA256 원문은 본 로그에 붙여 넣지 않는다. 필요 시 `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_ACCOUNT_NO]` placeholder 로만 참조한다.
- 🔵 `_common/risk-register.md` R-AUTO-001 · R-AUTO-037 Mitigation history 2026-07-21 보강 · R-DATA-017 `Mitigated` 승격 · R-AUTO-020 보강 · `_common/operator-decisions.md` OD-SAFE-004 · OD-MS-032 · OD-MS-026 Details 2026-07-21
  `_common/followups-overview.md` Next 완료 항목 정리 · Now P1 갱신 · Done recently 2026-07-21(validator · P3 crawler) · `_common/ms-aws-service-decision-matrix.md` 4.3 · 4.7 운영 메모 · `_common/aws-resource-glossary.md` Usage Notes 2026-07-21.

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| Risk ID | R-AUTO-001 |
| 상태 변화 | 🔴 Open 유지 |
| 요약 | broker 주문 재시도 금지·fail-closed 유지 · State Machine command 만 수정 · 자동 재시도 broker 중복 주문 0건 |

| 항목 | 값 |
| --- | --- |
| Risk ID | R-AUTO-037 |
| 상태 변화 | 🟢 Mitigated 유지 |
| 요약 | 정상 무주문 회차 validator 설정 누락 확인 · `--allow-no-target` 보완 · 단독 smoke ExitCode 0 |

| 항목 | 값 |
| --- | --- |
| Risk ID | R-DATA-017 |
| 상태 변화 | 🟢 Open → Mitigated 승격 |
| 요약 | KRX Python 실패 전파 + runner expected trade date · row count validator · fail-closed · 단독 validator Program 1건 · Shortsell 349건 · ExitCode 0 |

| 항목 | 값 |
| --- | --- |
| Risk ID | R-AUTO-020 |
| 상태 변화 | 🟢 Mitigated 유지 |
| 요약 | runner 후단 DB validation 보강(Program 1건 · Shortsell 최소 300건) |

### 📌 Follow-ups

- [ ] [P1] 다음 정상 자동 회차에서 전체 성공 경로와 성공 Slack 자동 진입 재관찰 (강제 주문·오류 유발 없이) — 실패 전파 관찰 완료 · 수정 후 전체 Step 12~17 자동 성공 acceptance 미확인 유지
- [ ] `NO_TARGET` 과 실제 성공 상태의 구분 강화 (정상 무주문 회차 명시 성공 처리 · 실제 주문 회차 fail-closed 유지)
- [ ] KRX 정상 자동 회차에서 crawler Python 실패 전파 · runner DB validator 실운영 관찰 (2026-07-21 runner 단독 validator 성공 · 정상 자동 회차 실증은 별도)

### 🧭 P3 — KRX Crawler 실패 전파 강화 및 DB validator:완료

위 정상 무주문 validator 수정과 별개 작업이다. · Windows Scheduled Task `Portfolio-KRX-Worker-Daily` 가 `C:\portfolio\run_krx_worker_daily.ps1` 를 실행하며 기존 runner 는 Python `$LASTEXITCODE` non-zero 시 실패 처리하나, `interest_program.py`
`interest_shortsell.py` 는 내부 실패 결과에도 프로세스 exit 0 로 끝날 수 있던 false-success 축(R-DATA-017) 이 있었다. · 운영자가 직접 수행했고 Kiro 는 문서만 수정했다.

- 🟢 Python 종료 코드 전파 개선 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 대상 파일 | `interest_program.py` · `interest_shortsell.py` 두 파일만 |
| exit 0 조건 | `SUCCESS` · 정상 `NO_CHANGE` 이고 `error_count=0` |
| exit 1 조건 | `FAILED` · 부분 오류 · 알 수 없는 상태 |
| 로그 | `PROCESS_EXIT_DECISION` 추가 |
| 미수정 | 로그인·Chrome 보조 파일 |
| 검증 | 로컬 import·종료 코드 단위 테스트 성공 · 운영 venv py_compile·종료 코드 단위 테스트 성공 |

- 🟢 runner DB validator 추가 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 대상 | 로컬 `ops/run_krx_worker_daily.ps1` |
| 검증 기준 (1) | `interest_program_raw` 최신 거래일 = expected · 정확히 1건 |
| 검증 기준 (2) | `interest_shortsell_raw` 최신 거래일 = expected · 최소 300건 |
| 실행 순서 | KRX login → program → shortsell → Validate crawler DB → DONE |
| validator 임시 파일 | `$AppDir` 아래 생성 후 삭제 |
| 실패 전파 | 검증 실패 시 runner non-zero → SSM → Step Functions |
| 단독 실행 결과 | expected trade date 2026-07-20 · Program 1건 · Shortsell 349건 · `CRAWLER_DB_VALIDATION=SUCCESS` · ExitCode 0 |

- 🟢 배포·정합 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 순서 | 로컬 원본 확보 → 수정 → 검증 → 운영 백업 → 배포 → 원격 검증 |
| 파일 정합 | 로컬·원격 SHA256 일치 · 원격 PowerShell parse 성공 |
| 실제 재실행 | crawler 재수집·전체 State Machine 재실행 없음 |
| Risk / Decision | R-DATA-017 `Open` → `Mitigated` 승격 · R-AUTO-020 `Mitigated` 유지 · OD-MS-026 Details 보강 · 신규 ID 없음 |

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS · DB · broker · KIS · Slack · ECS · EC2 · Lambda · Step Functions · Scheduled Task 명령 | Kiro 실행 0건 · 운영자 직접 수행 |
| State Machine ARN · execution ARN · ECS Task ARN · account-id · instance-id · Command ID · broker 주문번호 · SHA256 원문 신규 기록 | 0건 |
| git add · commit · push · reset · restore | 0건 |
| placeholder 정책 | `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_ACCOUNT_NO]` 계열만 사용 |
| 저장 인코딩 | UTF-8 No BOM 유지 |

## 2026-07-20 (Step 13 EGW00201 복구 · rate-limit 보완 · Step 14~17 수동 완주 · State Machine 실패 Slack 및 runDate 정합)

### 🧭 Summary

2026-07-20 09:01 자동 실행된 State Machine `portfolio-paper-daily-step12-17-approval` 이 Step 13(주문·체결 조회)에서 실패했다. Step 12 주문 제출 자체는 정상 완료되어 매도 주문 2건이 broker 에 정상 접수됐고 첫 주문은 Step 13 조회에서 전량 체결로 반영됐으나, 두 번째 주문 조회에서 KIS `EGW00201`(초당 거래건수 초과) 가 발생했다.
기존 `connector_order_check.py` 에는 `EGW00201` 전용 재시도가 없고 다건 주문을 주문 사이 대기 없이 연속 조회하던 점이 원인이었다. Step 13 실패로 Step 14~17 과 성공 Slack 이 자동 실행되지 않았다.
운영자가 직접 조회 API 한정 rate-limit 보완 패치(주문 간 5초 대기 + `EGW00201` 한정 최대 2회 재시도)를 배포하고, Step 12 는 중복 주문 위험 때문에 재실행하지 않은 채 Step 13 만 수동 실행해 두 주문 모두 `FILLED` 로 복구했다.
이어 Step 14~17 을 임시 State Machine 없이 하나씩 수동 실행해 완주했고, 성공 Slack 은 자동 실행 결과가 아니라 수동 복구 완료 후 Lambda 를 수동 호출해 수신 확인했다. 원래 09:01 자동 실행 이력은 실제 장애 보존을 위해 FAILED 로 유지한다. State Machine 의 잔고 runDate 하드코딩(`--run-date 2026-06-22`) 제거와 결과 실패 시 실패 Slack 우회 경로 수정도 함께 수행했다.
별도 후속으로, 장애 복구와 무관하게 `DAILY_EXECUTION_SUCCESS` 성공 Slack 이 당일 실제 매수·매도 체결 종목·수량을 표시하도록 개선했다. 신규 `portfolio-daily-execution-slack-summary-builder` Lambda 가 `connector.connector_fill`
`reference.stock_master` 를 조회해 payload 를 만들고 State Machine 성공 경로를 Builder → Notifier 로 연결했으며, 2026-07-20 데이터로 수동 smoke 만 수행(전체 자동 execution 미시작)해 매수 0건 매도 2종목 Slack 수신을 확인했다. 이 장애 복구 이후 별도 P0 안전성 강화를 수행했다.
Step 13 active 주문 제한 polling(`connector_order_check.py` 2.0.3), Step14~16 fail-closed 정합 검증, 신규 read-only validator 2개(`execution_validate_ready_plan_order.py`
`execution_validate_order_chain.py`) 의 State Machine 연결, Execution 불변 이미지 태그 `paper-20260720-p0-integrity-v1` Task Definition revision 3, `Step12_Failed` 실패 Slack 경로 수정, `DAILY_EXECUTION_FAILED` formatter 단독 smoke 를 완료했다.
다만 이는 P0 구현·운영 배포·단독 smoke·State Machine 연결까지의 범위이며 다음 정상 자동 회차 성공을 의미하지 않는다 — 전체 자동 회차 acceptance test 는 미수행이고 09:01 자동 execution 은 Step 13 `EGW00201` FAILED 로 보존한다. Kiro 는 본 회차에서 문서만 갱신하고 운영 작업은 운영자가 수행했다.

### ✅ Completed

- 🔴 Step 12~17 자동 실행 장애 (운영자 직접 확인)

| 항목 | 값 |
| --- | --- |
| State Machine | `portfolio-paper-daily-step12-17-approval` |
| 실패 지점 | Step 13 (주문·체결 조회) |
| Step 12 주문 제출 | 정상 완료 · 매도 2건 broker 정상 접수 |
| 첫 주문 Step 13 | 전량 체결 반영 |
| 둘째 주문 Step 13 | KIS `EGW00201` (초당 거래건수 초과) |
| 직접 원인 | `EGW00201` 전용 재시도 부재 · 주문 사이 대기 없음 |
| 후속 영향 | Step 14~17 · 성공 Slack 자동 미실행 |

- 🟢 Step 13 rate-limit 보완 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 대상 파일 | `connector_order_check.py` 전체 교체본 · S3 경유 EC2 배포 |
| 버전 | connector-order-check-2.0.1 → 2.0.2 |
| 주문 간 대기 | 조회 사이 5초 추가 |
| 재시도 | `EGW00201` 한정 최대 2회 (1차 1.5초 · 2차 5초) |
| 다른 오류 코드 | 자동 재시도 미적용 |
| 배포 전 검증 | 원본 백업 · SHA 검증 · Python compile · AST 검증 성공 |
| broker 주문 제출 로직 | 변경 없음 |

- 🟢 미처리 주문 Step 13 수동 복구 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| Step 12 재실행 | 중복 주문 위험으로 미실행 |
| Step 13 수동 실행 | active 주문 1건 |
| 둘째 매도 주문 | 13주 전량 체결 · `FILLED` 반영 |
| 생성 데이터 | `SUMMARY_ONLY_FILLED` 주문 이벤트 · fill 데이터 |
| 최종 상태 | 두 주문 모두 `FILLED` · active 대상 0건 |
| 신규 이벤트 버전 | connector-order-check-2.0.2 |

- 🟢 Step 14~17 순차 수동 복구 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 임시 State Machine | 미생성 · Step 14·15·16·17 하나씩 순서 실행 |
| Step 14 `execution_sync_sell_fill.py` | ECS Task ExitCode 0 |
| Step 15 `execution_sync_buy_fill.py` | ECS Task ExitCode 0 |
| Step 16 `execution_sync_buy_position.py` | ECS Task ExitCode 0 |
| Step 17 `run_connector_balance_daily.sh` | run-date=2026-07-20 · 잔고 API Status 200 · `connector_balance_snapshot` 저장 성공 |
| 보유 종목 | 0건 (과거 스냅샷 이력 보존) |

- 🟢 최신 잔고 스냅샷 (2026-07-20 기준)

| 항목 | 값 |
| --- | --- |
| 현금잔고 | 8,706,505 원 |
| 총평가금액 | 8,057,330 원 |
| 당일 매도금액 | 5,298,000 원 |
| 오늘 포지션 행 | 없음 (실제 보유 0건) |

- 🟢 성공 Slack 수동 복구 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 호출 Lambda | `portfolio-event-notifier` |
| 이벤트 | `DAILY_EXECUTION_SUCCESS` · runDate=2026-07-20 · stage=AFTER_STEP_17 |
| 결과 | StatusCode 200 · ok=true · Portfolio Daily Bot 채널 실제 수신 |
| 09:01 자동 실행 이력 | FAILED 유지 (수동 복구를 자동 성공으로 덮어쓰지 않음) |

- 🟢 State Machine runDate 하드코딩 제거 · 실패 Slack 경로 수정 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| Step1_SendConnectorBalanceCommand | `--run-date 2026-06-22` 하드코딩 제거 · `$.runDate` 동적 전달 (States.Array · States.Format) |
| 결과 실패 우회 문제 | Task 정상 종료·결과 비정상 시 Choice Default 가 Fail State 직행해 실패 Slack 우회 |
| 수정 | Step13·14·15·16·Step1 각 Default 를 실패 컨텍스트 Pass State 로 변경 · `$.dailyExecutionFailure` 저장 후 실패 Slack · OPS 실패 기록 · 최종 상태는 FAILED 유지 |
| Step12_Failed | 이번 변경 범위 제외 |
| 검증 | ASL 검증 OK · 배포 후 재조회로 2026-06-22 제거·$.runDate 적용·실패 경로 확인 |

- 🟢 Daily 실행 성공 Slack 실제 체결 내역 표시 개선 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 신규 Builder Lambda | `portfolio-daily-execution-slack-summary-builder` · Python 3.12 |
| 역할 | 당일 실제 체결 조회 후 성공 Slack payload 생성 |
| DB 사용자 | `view_app` (schema USAGE · table SELECT · column SELECT 확인) |
| 체결 원천 | `connector.connector_fill` |
| 종목명 원천 | `reference.stock_master` |
| 조회 기준일 | `connector_fill.created_at` 을 Asia/Seoul 날짜로 변환해 runDate 비교 |
| 결과 필드 | `buyFills` · `sellFills` · 종목별 `tickerCode` · `stockName` · `fillQty` |

- 🟢 성공 Slack formatter 변경 · Builder 연결 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 대상 Notifier | `portfolio-event-notifier` · `DAILY_EXECUTION_SUCCESS` formatter |
| 성공 메시지 구조 | 제목 ✅ [Daily 실행] 성공 · 실행일 · 최종 상태 · [매수 체결] · [매도 체결] |
| 체결 표기 | 없으면 `- 없음` · 있으면 `- 종목명 / 수량주` |
| 제거된 라인 | 기존 Workflow · Execution · 주문/체결 요약 라인 |
| 배포 전 | 기존 Lambda 코드 백업 · Python compile · import · formatter 로컬 테스트 통과 |
| 배포 후 | 상태 Active · LastUpdateStatus=Successful · marker/해시 일치 확인 (SHA256 원문 미기록) |

- 🟢 State Machine 성공 경로 연결 · IAM 최소 권한 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 대상 State Machine | `portfolio-paper-daily-step12-17-approval` |
| 신규 State | `BuildDailyExecutionSuccessSlackSummary` |
| 성공 경로 | Step 완료 → Builder → Notifier → `RecordWorkflowStepSuccess` |
| Builder 입력 · 결과 | runDate 입력 · 결과 `$.dailyExecutionSuccessSummary` 저장 |
| Notifier 입력 | `$.dailyExecutionSuccessSummary.Payload` |
| Builder 실패 시 | 기존 `SendDailyExecutionFailedSlack` 경로 연결 |
| IAM | Builder 전용 실행 Role 최소 권한 · State Machine Role 에 `lambda:InvokeFunction` inline (`portfolio-daily-execution-slack-builder-invoke`) |
| ASL 검증 | OK · diagnostic 0건 · canonical(key 정렬) 비교 의미상 차이 0건 |

- 🟢 수동 smoke 결과 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 방식 | 2026-07-20 데이터로 Builder → Notifier 순차 수동 호출 |
| 전체 execution | 미시작 (State Machine 전체 실행 안 함) |
| 매수 체결 | 0건 |
| 매도 체결 (1) | 엔씨소프트(036570) 13주 |
| 매도 체결 (2) | 코오롱생명과학(102940) 78주 |
| Notifier | StatusCode 200 · FunctionError 없음 · ok=true · Slack 실제 수신 |
| 09:01 자동 실행 이력 | Step 13 `EGW00201` FAILED 유지 (수동 smoke 를 자동 성공으로 표현하지 않음) |

- 🟢 P0 안전성 강화 — Step 13 active 주문 polling (장애 복구 이후 수행 · 운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 대상 파일 · 버전 | `connector_order_check.py` connector-order-check-2.0.2 → 2.0.3 |
| active 상태 처리 | ACCEPTED · SUBMITTED · PENDING · PARTIAL_FILLED · PARTIALLY_FILLED |
| polling | 최초 조회 후 최대 3회 추가 · 기본 10초 간격 · 종료 후 active 이면 exit 1 |
| 기존 유지 | 주문 간 5초 대기 · `EGW00201` 전용 재시도 |
| 배포 | S3 임시 객체 경유 EC2 배포 · 타임스탬프 백업 · 배포 후 임시 객체 삭제 |
| 검증 | 교체 전후 Python compile · 로컬·원격 SHA256 일치 (원문 해시 미기록) |
| 성격 | 주문·체결 조회 API 반복 조회 · 주문 제출 재시도 아님 |

- 🟢 P0 안전성 강화 — Step14~16 데이터 정합 fail-closed (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| Step14 SELL Fill | 대상 건수·처리 건수 검증 · 체결·실행 주문·원본 포지션·포지션 종료 상태 함께 검증 |
| Step15 BUY Fill | 대상 건수·처리 건수 검증 · 누락·실패 시 rollback 후 exit 1 |
| Step16 BUY Position | 요청·체결·포지션 연결·수량 검증 · 불일치 시 rollback 후 exit 1 |
| commit 조건 | 전체 검증 일치 시에만 commit·성공 처리 |

- 🟢 P0 안전성 강화 — 신규 validator 2개 · Execution 이미지 · ECS 반영 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| `execution_validate_ready_plan_order.py` | 승인 후 Step12 직전 · READY Plan `ready_order_count` 대비 실제 PAPER_STRATEGY Order 건수 검증 · 누락·건수 불일치·잘못된 수량/종목 시 exit 1 |
| 실데이터 검증 (1) | 2026-07-20 Plan 143 · Order 2건 검증 성공 · ECS Fargate smoke ExitCode 0 |
| `execution_validate_order_chain.py` | Step16 직후 · Plan→Order→Request→Fill→Position read-only 검증 · 불일치 시 exit 1 |
| 실데이터 검증 (2) | 2026-07-20 SELL 주문 2건 검증 성공 · ECS Fargate smoke ExitCode 0 |
| 실패 경로 | 두 validator 모두 실패 시 공통 실패 Slack · OPS 실패 기록 경로 연결 |
| ECR 태그 | 불변 태그 `paper-20260720-p0-integrity-v1` 신규 빌드 · 기존 태그 미덮어쓰기 |
| Task Definition | `portfolio-paper-strategy-execution` revision 3 · Step14·15·16 연결 |
| 신규 이미지 검증 | 수정 파일·validator py_compile · 신규 Task Definition ECS Fargate smoke 성공 |

- 🟢 P0 안전성 강화 — Step12 실패 전파 · 실패 formatter smoke (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| Step12_Failed | 직접 Fail 종료 제거 · Error·Cause 를 `dailyExecutionFailure` 저장 후 `SendDailyExecutionFailedSlack` → `RecordBatchFailure` → 최종 Fail 연결 |
| 검증 | 변경 전 정의 로컬 백업 · ASL validation OK · 배포 후 재조회 검증 |
| 실패 formatter smoke | 실제 주문 실패 없이 Notifier Lambda 를 `DAILY_EXECUTION_FAILED` 테스트 이벤트로 직접 호출 |
| 표시 확인 | 실행일 · Workflow · 실패 Step · 원인 · 주문 제출 여부 · 다음 액션 · 원인 문구에 테스트 이벤트 명시 · Portfolio Daily Bot 실제 수신 |

- 🟠 P0 완료 범위 vs 미완료 구분

| 항목 | 값 |
| --- | --- |
| 완료 | ACCEPTED·PARTIAL_FILLED polling · Step14·15·16 처리 건수·정합 검증 · Plan→Order→Request→Fill→Position 불일치 실패 전파 · READY Plan 대비 Order 부재·건수 불일치 실패 처리 · Step12_Failed 실패 Slack 경로 · 실패 formatter smoke |
| 완료 성격 | P0 구현 · 운영 배포 · 단독 smoke · State Machine 연결까지 |
| 미완료 (P1) | 다음 정상 자동 회차 성공 관찰 · 전체 자동 회차 acceptance test 미수행 |
| 자동 실행 이력 | 09:01 자동 execution 은 Step 13 `EGW00201` FAILED 로 보존 (수동 복구·P0 완료를 자동 성공으로 기록하지 않음) |

### 🔵 Evidence

- 🔵 실제 State Machine ARN · execution ARN · SSM Command ID · ECS Task ARN · broker 주문번호 · account-id · SHA256 원문은 본 로그에 붙여 넣지 않는다. 필요 시 `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_BROKER_ORDER_NO]` · `[REDACTED_ACCOUNT_NO]` placeholder 로만 참조한다.
- 🔵 `_common/risk-register.md` R-AUTO-001 · R-AUTO-037 · R-AUTO-038 · R-BROKER-004 Mitigation history 2026-07-20 보강 · `_common/operator-decisions.md` OD-SAFE-001 · OD-SAFE-004 · OD-MS-009 · OD-MS-030 · OD-MS-031 · OD-MS-032 Details 짧은 운영 메모
  `_common/followups-overview.md` Done recently 2026-07-20 · Now 갱신 · `_common/ms-aws-service-decision-matrix.md` 4.1 · 4.7 짧은 운영 메모 · `_common/aws-resource-glossary.md` Usage Notes 2026-07-20.

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| Risk ID | R-AUTO-001 |
| 상태 변화 | 🔴 Open 유지 |
| 요약 | broker 주문 자체 재시도 여전히 금지 · 조회 API `EGW00201` 에만 제한 재시도 · Step 12 중복 방지 위해 미재실행 |

| 항목 | 값 |
| --- | --- |
| Risk ID | R-AUTO-037 |
| 상태 변화 | 🟢 Mitigated 유지 |
| 요약 | Step 13 실패로 후속 Step 중단 확인 · runDate 하드코딩 제거 · 결과 실패 Slack 경로 보완 |

| 항목 | 값 |
| --- | --- |
| Risk ID | R-AUTO-038 |
| 상태 변화 | 🟢 Mitigated 유지 |
| 요약 | 자동 실행 실패 이력과 수동 복구 이력 구분 필요 · OPS 세부 Step 확장 후속 유지 |

| 항목 | 값 |
| --- | --- |
| Risk ID | R-BROKER-004 |
| 상태 변화 | 🟢 Mitigated 유지 |
| 요약 | Step 12 재실행 없이 Step 13 만 복구 · broker 중복 주문 0건 |

### 📌 Follow-ups

- [ ] [P1] 다음 정상 자동 회차 end-to-end 관찰 (강제 주문·오류 유발 없이) — Step1~11 Scheduler 자동 성공 · Step12~17 자동 실행 · Builder→Notifier 자동 진입 및 성공 Slack 당일 체결 내역 표시 · DB after-check · 다건 주문 5초 대기 로그 · `EGW00201` 미발생 시 불필요한 재시도 없음 · 신규 validator 2개 자동 경로 통과
- [ ] [P1] 전체 자동 회차 acceptance test 수행 (P0 는 구현·배포·단독 smoke·State Machine 연결까지만 완료)
- [ ] 실 다건 주문 회차에서 주문 간 5초 대기 · active 주문 polling · `EGW00201` 재시도 로그 실운영 확인 (미발생 시 재시도 미진입 정상)
- [ ] OPS Mirror 세부 Step 확장 · 자동 실행 run 대 수동 복구 run 구분 mirror
- [ ] 주문 검증 체인 중 Signal Order Map · Broker 접수 구간 보강 (Plan→Order→Request→Fill→Position 구간은 `execution_validate_order_chain.py` 로 검증 완료)

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS · DB · broker · KIS · Slack · SSM · ECS · EC2 · Lambda · Step Functions 명령 | Kiro 실행 0건 · 운영자 직접 수행 |
| broker 주문번호 · execution ARN · State Machine ARN · SSM Command ID · ECS Task ARN · account-id · SHA256 원문 | 신규 기록 0건 |
| git add · commit · push · reset · restore · checkout · stash | 0건 |
| placeholder 정책 | `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_BROKER_ORDER_NO]` · `[REDACTED_ACCOUNT_NO]` 계열만 사용 |
| 저장 인코딩 | UTF-8 No BOM 유지 |

## 2026-07-16 (daily-buy-e2e-first-run · Step 12 대기시간 60초 · 후속 polling 후보)

### 🧭 Summary

2026-07-16 회차에서 Daily Run 76 이 정상 계산되어 BUY 후보 2건(엔씨소프트 036570 코오롱생명과학 102940) 이 생성됐고, 운영자가 Step 12~17 을 수동 재실행해 실제 KIS 모의투자 시장가 매수 주문이 접수되고 전량 체결되어 Fill Position 까지 연결된 첫 실 회차가 완료됐다.
초기 Step 13 조회가 주문 제출 후 약 10초 시점에 이뤄져 중간 체결 상태가 고착되는 문제가 확인됐고, 운영자가 직접 Step 13 → Step 15 → Step 16 순서로 재실행해 DB 정합을 복구했다. 재발 방지 조치로 State Machine `portfolio-paper-daily-step12-17-approval` 의 Wait State `Step12_WaitBeforeCheck` `Seconds` 를 10 → 60 으로 변경했다.
후속 polling Execution Order 정합 실패 전파 강화 OPS Mirror 세부 Step 확장은 Follow-up 으로 유지된다. Kiro 는 본 회차에서 문서만 갱신한다.

### ✅ Completed

- 🟢 Daily Step 1~11 복구 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| daily_run_id | 76 |
| run_date | 2026-07-16 |
| data_date | 2026-07-15 |
| market_signal | AGGRESSIVE |
| BUY 후보 | 엔씨소프트 036570 · 코오롱생명과학 102940 |

- 🟢 Step 12~17 수동 재실행 · 실 매수 주문 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 엔씨소프트 주문 | 13주 시장가 매수 |
| 코오롱생명과학 주문 | 78주 시장가 매수 |
| KIS 모의투자 접수 | 두 주문 모두 성공 |
| broker 주문번호 원문 | 문서 미기록 |

- 🟢 실제 잔고 확인

| 항목 | 값 |
| --- | --- |
| 엔씨소프트 최종 보유수량 | 13주 |
| 엔씨소프트 평균 체결가 | 223,500 원 |
| 코오롱생명과학 최종 보유수량 | 78주 |
| 코오롱생명과학 평균 체결가 | 약 38,839.7436 원 |
| 전량 체결 여부 | 두 주문 모두 완료 |

- 🟠 최초 주문·체결 조회 문제

| 항목 | 값 |
| --- | --- |
| 최초 Step 13 조회 시점 | 주문 제출 후 약 10초 |
| 엔씨소프트 최초 조회 | 9주 부분체결 |
| 코오롱생명과학 최초 조회 | 접수 상태 |
| 실제 시장가 주문 | 이후 계속 체결 진행 |
| 내부 상태 | 최초 조회 결과에 고착 |
| 전체 Step Functions | ExitCode 0 기준 SUCCESS 처리 |

- 🟢 수동 복구 순서 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| Step 13 재실행 | `connector_order_check.py` · 두 주문 `FILLED` 반영 · Connector Order Event 최신 상태 · Connector Fill 13주 · 78주 반영 |
| Step 15 재실행 | `execution_sync_buy_fill.py` · Execution Order 2건 `SUBMITTED` → `FILLED` |
| Step 16 재실행 | `execution_sync_buy_position.py` · `filled_buy_orders_without_position=2` · `synced_count=2` · `skipped_count=0` |
| 신규 Position | ID 13(엔씨소프트) · ID 14(코오롱생명과학) · 모두 `OPEN` |

- 🟢 최종 데이터 체인 정합

| 항목 | 값 |
| --- | --- |
| Daily Signal | 정상 |
| Execution Plan | 정상 |
| Execution Order | `FILLED` |
| Connector Order Request | `FILLED` |
| Connector Fill | 정상 |
| Strategy Position State | `OPEN` |
| 실제 잔고 vs 내부 Position | 정합 확인 |

- 🟢 재발 방지 변경 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 대상 State Machine | `portfolio-paper-daily-step12-17-approval` |
| 대상 Wait State | `Step12_WaitBeforeCheck` |
| 변경 전 · 후 | `Seconds=10` → `Seconds=60` |
| Next State | `Step12_GetCommandInvocation` 유지 |
| 검증 | State Machine 업데이트 후 재조회 통과 |
| 실제 ARN · revision ID 원문 | 문서 미기록 |

### 🔵 Evidence

- 🔵 broker 주문번호 · 실제 State Machine ARN · executionArn · revision ID · account-id · Slack payload · SSM 응답 · psql raw output 은 본 로그에 붙여 넣지 않는다. 필요 시 `[REDACTED_BROKER_ORDER_NO]` · `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_ACCOUNT_NO]` placeholder 로만 참조한다.
- 🔵 `_common/risk-register.md` R-AUTO-037 · R-AUTO-038 · R-BROKER-004 Mitigation history 2026-07-16 보강 · `_common/operator-decisions.md` OD-MS-032 · OD-SAFE-001 Details 짧은 운영 실증 메모
  `_common/followups-overview.md` Done recently 2026-07-16 row · `_common/ms-aws-service-decision-matrix.md` `port_strategy_execution` 짧은 운영 메모 · `_common/aws-resource-glossary.md` Usage Notes 2026-07-16 표.

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| Risk ID | R-AUTO-037 |
| 상태 변화 | 🟢 Mitigated 유지 |
| 요약 | 실 매수·체결·Fill·Position E2E 첫 실증 · Step 12 Wait 60초 반영 · polling · 처리 건수 실패 전파 · OPS Mirror 세부 확장 남음 |

| 항목 | 값 |
| --- | --- |
| Risk ID | R-AUTO-038 |
| 상태 변화 | 🟢 Mitigated 유지 |
| 요약 | OPS Mirror 는 Step 12~17 전체를 대표 Step 1건으로 기록 · 세부 Step 확장 필요성 재확인 |

| 항목 | 값 |
| --- | --- |
| Risk ID | R-BROKER-004 |
| 상태 변화 | 🟢 Mitigated 유지 |
| 요약 | 실 매수 회차에서 broker 중복 주문 0건 · 사전 점검 정책 회귀 없음 |

### 📌 Follow-ups

- [ ] `ACCEPTED` / `PARTIAL_FILLED` 주문의 후속 재조회 polling — 60초 후에도 미완료 시 30~60초 간격 제한 횟수 재조회 — 04 spec
- [ ] Step 13 · 15 · 16 처리 건수 기반 실패 전파 강화 — 04 spec
- [ ] Execution Order · Connector Request · Fill · Position 불일치 시 Workflow FAIL 처리 — 04 spec
- [ ] 10분 잔고 스냅샷과 주문 · 체결 · Position 보정 연계 검토 — 03, 04 spec

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS · DB · broker · KIS · Slack · Scheduler · Step Functions · ECS · EC2 명령 | Kiro 실행 0건 · 운영자 직접 수행 |
| broker · KIS 자동 매수 · 자동 매도 · fill sync · position sync · intraday monitor · 크롤러 실행 | 0건 |
| broker 주문번호 · executionArn · State Machine ARN · account-id · secret · webhook URL · payload raw · 계좌번호 · Fill raw 원문 | 신규 기록 0건 |
| Lambda · IAM Policy · Step Functions history · SSM 응답 · SQL raw output | 신규 기록 0건 |
| git add · commit · push · rebase · reset · restore | 0건 |
| placeholder 정책 | `[REDACTED_BROKER_ORDER_NO]` · `[REDACTED_ARN]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_ACCOUNT_NO]` 계열만 사용 |
| 저장 인코딩 | UTF-8 No BOM 유지 |

## 2026-07-15 (daily-brief-slack-recovery · Dispatcher 공통 Holiday Guard 전환 · Daily BUY KST 날짜 오판 해결 · Approval Slack 데이터 정합 및 후보 표시 개선)

### 🧭 Summary

2026-07-15 오후에 Daily Brief 자동 장후 Slack 이 발송되지 않는 문제가 식별되었다. 원인은 Builder Lambda 가 VPC 안에서 외부 Holiday API 호출에 실패해 Fail-Closed 로 장전
장후 Slack 이 skip 되고 있던 점, Builder 내부 Holiday Guard 를 비활성화한 뒤에도 정상 응답에 `skipped` 필드가 없어 mini state machine 의 `CheckHolidaySkip` Choice 가 `States.Runtime` 을 낸 점, 그리고 Builder 배포 ZIP 에 `pg8000` 이 없어 DB 경로 진입 시 import 오류가 발생한 점이었다.
운영자가 직접 Daily Scheduler Dispatcher 에 `MORNING_BRIEF` `EVENING_BRIEF` 지원과 Daily Brief State Machine ARN + `states:StartExecution` IAM 을 추가하고, 장전 장후 Scheduler Target 을 Dispatcher Lambda 호출로 전환해 Holiday Guard 를 Dispatcher 에 통합했다.
Builder 는 내부 Guard 를 환경변수 제어 방식으로 비활성화하고 정상 응답에 `skipped=false` `skipReason=null` 을 추가하며 `pg8000` 을 패키징해 재배포했다. 이후 2026-07-15 16:02 장후 실전 smoke 에서 Step Functions `SUCCEEDED` Notifier `statusCode=200` Slack 실제 수신을 확인했다. Kiro 는 본 회차에서 문서만 갱신한다.

### ✅ Completed

- 🟠 장애 원인 확정 (운영자 직접 확인)

| 항목 | 값 |
| --- | --- |
| 1차 원인 | Builder Lambda 가 VPC 안에서 외부 Holiday API 호출 실패 |
| Fail-Closed 결과 | 장전 · 장후 Slack 발송이 생략 |
| 2차 원인 | Builder 내부 Holiday Guard 비활성화 후 정상 응답에 `skipped` 필드 없음 |
| Choice 결과 | `CheckHolidaySkip` 에서 `States.Runtime` 발생 |
| 3차 원인 | Builder 배포 ZIP 에 `pg8000` 없음 |
| 부수 결과 | DB 경로 진입 시 import 오류 |

- 🟢 최종 자동화 구조 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 장전 07:50 | Scheduler → Daily Scheduler Dispatcher → 공통 Holiday Guard → Daily Brief State Machine |
| Daily 검증 08:00 | Scheduler → 동일 Dispatcher → 동일 Holiday Guard → Step 1~11 |
| Daily 실행 09:01 | Scheduler → 동일 Dispatcher → 동일 Holiday Guard → Step 12~17 |
| 장후 15:50 | Scheduler → 동일 Dispatcher → 동일 Holiday Guard → Daily Brief State Machine |
| EC2 Stop 15:50 | 기존 Scheduler 유지 · 휴일 여부 무관 실행 유지 |

- 🟢 실제 수정 사항 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| Dispatcher 지원 eventType | `MORNING_BRIEF` · `EVENING_BRIEF` 추가 |
| Dispatcher 환경변수 | Daily Brief State Machine ARN 추가 (원문 미기록) |
| Dispatcher IAM | Daily Brief State Machine `states:StartExecution` 권한 추가 |
| Dispatcher IAM 범위 | Resource 한정 · 와일드카드 사용 없음 |
| 장전 Scheduler Target | Daily Brief State Machine 직접 호출 → Dispatcher Lambda 호출 |
| 장후 Scheduler Target | Daily Brief State Machine 직접 호출 → Dispatcher Lambda 호출 |
| Scheduler 시각 · 상태 | 기존 시각 유지 · ENABLED 유지 |
| Builder Holiday Guard | 환경변수 제어 · 운영 환경에서 비활성화 |
| Builder 정상 응답 필드 | `skipped=false` · `skipReason=null` 추가 |
| Builder 의존성 | `pg8000` 및 관련 의존성 패키징 |
| 기존 08:00 · 09:01 Daily Scheduler | 변경 없음 |
| EC2 시작 · 종료 Scheduler | 변경 없음 |

- 🟢 검증 결과 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| Dispatcher Step 1~11 · Step 12~17 기존 경로 | 정상 |
| Dispatcher 장전 · 장후 Daily Brief 경로 | 정상 |
| 주말 실제 실행 모드 smoke | Dispatcher Holiday Guard 차단 확인 |
| Builder DB 조회 | 정상 |
| Builder 정상 응답 필드 | `skipped=false` · `skipReason=null` 확인 |
| 2026-07-15 15:50 자동 장후 실행 | 초기 `CheckHolidaySkip` 오류로 실패 |
| 2026-07-15 16:02 장후 실전 smoke | Step Functions `SUCCEEDED` |
| Notifier | `statusCode=200` |
| Slack | 실제 수신 확인 |

- 🟢 실 수신 내용 요약 (2026-07-15 장후)

| 항목 | 값 |
| --- | --- |
| 기준일 | 2026-07-15 |
| 총 평가금액 | 8,706,505 원 |
| 현금 | 8,706,505 원 |
| 누적 수익률 | -12.94% |
| 평가손익 | -1,293,495 원 |
| 어제 대비 | 0 원 |
| 보유 종목 | 없음 |

### 🔵 Evidence

- 🔵 Lambda 소스 · IAM Policy · Step Functions history · PowerShell 출력 · SSM 응답 전문 · 실제 State Machine ARN · accountNo · executionArn · RequestId · SHA256 · webhook URL 원문은 본 로그에 붙여 넣지 않는다. 필요 시 `[REDACTED_ARN]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_EXECUTION_ARN]` placeholder 로만 참조한다.
- 🔵 `_common/risk-register.md` R-AUTO-035 Mitigation history 2026-07-15 보강 · `_common/followups-overview.md` Now 의 15:50 장후 Slack 항목을 Done recently 로 이동 · Now 의 07:50 장전 Slack 항목 유지 · `_common/operator-decisions.md` OD-MS-032
  OD-MS-038 Details 짧은 운영 실증 메모 · `_common/ms-aws-service-decision-matrix.md` Daily Batch orchestration 절 짧은 운영 메모 · `_common/aws-resource-glossary.md` EventBridge Scheduler · Lambda Usage Notes 짧은 메모.

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| Risk ID | R-AUTO-035 |
| 상태 변화 | 🟢 Mitigated 유지 |
| 요약 | Dispatcher 공통 Holiday Guard 로 통합 · Builder 정상 skip 필드 추가 · `pg8000` 패키징 · 2026-07-15 16:02 장후 실 Slack 수신 확인으로 Mitigation history 보강 |

### 📌 Follow-ups

- [ ] 다음 평일 07:50 자동 장전 Slack 실 수신 확인 유지 — 04 spec
- [ ] 보유 종목 존재 시 Daily Brief 종목별 표시 재확인 — 04 spec
- [ ] Daily Brief Holiday API fallback 정책 결정 (Dispatcher 공통 Holiday Guard 통합 후 후속) — 04 spec

### 🧭 오후 — Daily BUY KST 날짜 오판 해결

Daily Brief 미발송 복구와 서로 다른 장애다. 2026-07-13 (월) `daily_run_id=73` 이 정상 계산되고 BUY 신호 4건 후보 4건이 있었음에도 Execution Plan Execution Order 가 생성되지 않아 매수 후보 4건이 실행 대상에 오르지 못했다. 실행 컨테이너가 `date.today()`
`datetime.today()` naive 함수를 사용해 08:00 KST (= 2026-07-12 23:00 UTC) 실행 시점의 업무 날짜를 2026-07-12 (일) 로 판단 Step8 이 `WEEKEND / NO_TARGET` 으로 종료 ExitCode 0 으로 Step Functions 전체 SUCCESS 표시되어 자동 감지가 어려웠다. Daily 전략 계산과 2026-07-10 기준 데이터는 정상 (`daily_run_id=73`
`run_date=2026-07-12` `data_date=2026-07-10` `market_signal=AGGRESSIVE`).

- 🟢 KST 날짜 기준 수정 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| DB timestamp 저장 기준 | UTC 유지 |
| 업무 날짜 판단 기준 | Asia/Seoul 로 통일 |
| Daily BUY / SELL 실행 ECS Task Definition | `TZ=Asia/Seoul` 추가 |
| Market EC2 서버 timezone | Korea Standard Time 변경 |
| 실행 스크립트 | 로컬 · ECS 모두 KST 업무 날짜 사용 보완 |
| Step Functions 실행 경로 | 신규 Task Definition revision 연결 |

- 🟢 완료 vs 미완료 구분

| 항목 | 상태 |
| --- | --- |
| UTC 날짜 오판 직접 원인 | 🟢 해결 완료 |
| ECS · Market EC2 KST 적용 | 🟢 완료 |
| 신규 Task Definition revision 연결 | 🟢 완료 |
| 다음 자동 실행 재발 여부 | 🟠 실전 관찰 유지 |
| `NO_TARGET` 과 성공 상태 구분 강화 | 🟠 후속 |
| 내부 주문 실패 ExitCode 및 SF 실패 전파 강화 | 🟠 후속 |
| 주문 검증 체인 (Plan → Order → Connector Request → Signal Order Map → Broker → Fill → Position) 보강 | 🟠 후속 |

- 🔵 Evidence — 04 operation-notes 2026-07-15 (오후) 섹션 · `_common/risk-register.md` R-DATA-010 [2026-07-15 보강] · R-DATA-017 [2026-07-15 보강] · `_common/operator-decisions.md` OD-MS-040 [2026-07-15 보강] · `_common/followups-overview.md` Now.

### 🧭 오후 — Approval Slack 데이터 정합 및 후보 표시 개선

Approval Required Slack 메시지에 서로 다른 Daily Run 과 Execution Plan 이 혼합된 조합이 표시되던 문제. · Builder 가 최신 Execution Plan (`latestPlanId=133`
`latestPlanDate=2026-07-09`) 과 최신 Daily Run (`latestDailyRunId=73`) 을 각각 독립 조회해 실제 DB 에 존재하지 않는 상태 `DEFENSIVE` · 기준일 `2026-07-09` · 신호 4건 · 후보 4건 조합이 렌더링되던 상태였다. · 실제 결과는 `daily_run_id=73` · 기준 데이터 2026-07-10 · 시장 상태 `AGGRESSIVE`
BUY 신호 4건 · 후보 4건.

- 🟢 Approval Slack 조회 구조 수정 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 조회 기준 | 단일 `daily_run_id` 확정 후 상태 · 기준일 · 신호 · 후보 동일 Run 기준 조회 |
| 독립 latest 조회 제거 | Plan · Run 각각 따로 선택 방식 제거 |
| Order 조회 | `source_daily_run_id` 로 연결된 Execution Order 만 |
| Plan 조회 | 해당 Order 가 참조하는 Execution Plan 만 |
| 연결된 Plan 부재 시 | `latestPlanId=null` · 임의 과거 Plan 사용 안 함 |
| 메시지 상태 · 기준일 | 선택된 Daily Run 의 `market_signal` · `data_date` 사용 |

- 🟢 후보 표시 개선

| 항목 | 값 |
| --- | --- |
| 종목명 | 실제 `company_name` 또는 `ticker_code` 사용 |
| 점수 필드 | Builder 가 구조화하여 Notifier 로 전달 |
| 점수 소수점 | 셋째 자리까지 표시 |
| 한글 라벨 | 종합 · 수급 · 정보 · 추세 · 공매도 |
| 원본 `buy_info` dict | Slack 미노출 |

- 🟢 배포 · 검증 결과 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| Builder Lambda `portfolio-approval-slack-summary-builder` | `Active` · `LastUpdateStatus=Successful` · Runtime · Handler 변경 없음 |
| Notifier Lambda `portfolio-event-notifier` | `Active` · `LastUpdateStatus=Successful` · Runtime · Handler 변경 없음 |
| 이전 버전 롤백 ZIP | 확보 완료 |
| Builder DB 조회 상태 · 기준일 · 신호 수 · 후보 수 | AGGRESSIVE · 2026-07-10 · 4 · 4 |
| `latestPlanId` | null (선택 Run 에 Order · Plan 없음 · 과거 Plan 미사용 검증) |
| 후보 종목 | DL · 대주전자재료 · 삼성SDI · 한국피아이엠 |
| Builder → Notifier → Slack 메시지 표시 E2E | 완료 (실제 자동 주문 체결 E2E 아님) |

- 🔵 Evidence — 04 operation-notes 2026-07-15 (오후) 섹션 · `_common/operator-decisions.md` OD-MS-031 [2026-07-15 보강] · `_common/followups-overview.md` Done recently 2026-07-15 row.

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS · Lambda · Step Functions · Scheduler · IAM · DB · Slack · ECS · EC2 실행 | Kiro 실행 0건 · 운영자 직접 수행 |
| broker · KIS · crawler · 자동 매수 · 자동 매도 · fill sync · position sync · intraday monitor 실행 | 0건 |
| secret · password · token · webhook URL · 계좌번호 · 실제 ARN · executionArn · RequestId · SHA256 · Slack payload raw 원문 신규 기록 | 0건 |
| Lambda 코드 · IAM Policy · Step Functions history · PowerShell 출력 · SSM 응답 전문 · SQL raw output 신규 기록 | 0건 |
| git add · commit · push 실행 | 0건 |
| placeholder 정책 | `[REDACTED_ARN]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_EXECUTION_ARN]` · `[REDACTED_LAMBDA_ARN]` 계열만 사용 |
| 저장 인코딩 | UTF-8 No BOM 유지 |

## 2026-07-09 (krx-crawler-ec2-timezone-fix-and-recollect)

### 🧭 Summary

2026-07-08 회차 이후 KRX raw 정체(`interest_program_raw` · `interest_shortsell_raw` `MAX(trade_date)=2026-07-07`) 의 잔여 원인이 KRX CRAWLER Windows EC2 timezone 이 UTC 로 설정되어 있어 08:00 KST 실행 시 서버 로컬 날짜가 UTC 기준 전일 23시대로 계산된 것으로 확인되었다.
운영자가 CRAWLER Windows EC2 timezone 을 Korea Standard Time 으로 변경하고 KRX worker 수동 재수집을 수행하여 2026-07-08 raw 회복을 확인했다. · Kiro 는 본 회차에서 문서만 갱신한다.

### ✅ Completed

- 🟠 CRAWLER Windows EC2 timezone 이슈 확인 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 증상 | `interest_program_raw` / `interest_shortsell_raw` 최신일자 2026-07-07 정체 |
| Step Functions Step1~11 | SUCCEEDED (KRX Step2B raw 최신성은 보장 아님) |
| 직접 원인 | CRAWLER Windows EC2 timezone UTC 설정 |
| 부가 원인 | `datetime.today() - 1` · `datetime.now().date() - 1` 계열 로직이 target date 를 2026-07-07 로 계산 |

- 🟢 CRAWLER Windows EC2 timezone 조치 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| timezone 변경 | UTC → Korea Standard Time |
| Get-Date · Get-TimeZone · Python datetime | KST 기준 계산 확인 |
| 서버 시간 문제 | 해결 완료 |
| DB `created_at` session timezone | UTC 유지 권고 (표시 시 `at time zone 'Asia/Seoul'` 변환) |

- 🟢 KRX 재수집 결과 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| CRAWLER EC2 start | 완료 |
| SSM Online | 확인 |
| Portfolio-KRX-Worker-Daily Scheduled Task | 수동 실행 |
| `interest_program_raw` latest_date | 2026-07-08 |
| `interest_program_raw` rows_20260708 | 1 |
| `interest_shortsell_raw` latest_date | 2026-07-08 |
| `interest_shortsell_raw` rows_20260708 | 349 |

- 🟢 적재 시각 (KST 표시)

| 항목 | 값 |
| --- | --- |
| program `created_at` (UTC) | 2026-07-09 02:23:33 |
| program 표시 (KST) | 2026-07-09 11:23:33 |
| shortsell `created_at` (UTC) | 2026-07-09 02:24:08 |
| shortsell 표시 (KST) | 2026-07-09 11:24:08 |

- 🟢 최종 판정

| 항목 | 값 |
| --- | --- |
| KRX program / shortsell 2026-07-08 재수집 | 완료 |
| CRAWLER EC2 timezone 수정 효과 | 확인 |
| Step1~11 최신성 검증 | core ECS raw + KRX GUI worker 모두 회복 |

- 🟠 Step2B 자동 실행 구조 재확인

| 항목 | 값 |
| --- | --- |
| 07:50 KST | EventBridge Scheduler → EC2 lifecycle dispatcher (`action=start` · `target=BOTH` · `holidayCheck=true` · `reason=PRE_DAILY_STEP1_11` · `dryRun=false`) |
| 08:00 KST | Step1~11 scheduler → daily scheduler dispatcher |
| Step2B 흐름 | Step Functions Step2B_RunKrxGuiWorker → SSM AWS-RunPowerShellScript → Windows Scheduled Task Portfolio-KRX-Worker-Daily → `C:\portfolio\run_krx_worker_daily.ps1` → KRX login / program / shortsell |
| 운영 해석 | Step1~11 자체가 CRAWLER EC2 를 켜지 않으며 CRAWLER EC2 running + SSM Online 을 전제로 Step2B 명령 발송 · 수동 재검증 시에도 선행 확인 필요 |

- 🔴 잔여 false-success 이슈 (Open 유지)

| 항목 | 값 |
| --- | --- |
| `interest_program.py` / `interest_shortsell.py` | `[Error]` 출력해도 exit code 0 가능 |
| `run_krx_worker_daily.ps1` | 위를 성공으로 오판 가능 |
| Windows Scheduled Task LastTaskResult | 0 |
| Step2B SSM ResponseCode | 0 |
| Step Functions 전체 | SUCCEEDED |
| 그러나 DB | 해당 trade_date raw 부재 가능 |

### 🔵 Evidence

- 🔵 상세 KRX raw · Scheduled Task · SSM 실행 내역 · 실제 ARN / CommandId / instance-id / execution ARN 원문은 본 로그에 붙여 넣지 않는다. 필요 시 `[REDACTED_ARN]` · `[REDACTED_COMMAND_ID]` · `[REDACTED_INSTANCE_ID]` placeholder 로만 참조한다.
- 🔵 `_common/risk-register.md` R-DATA-010 Mitigation history 2026-07-09 보강 · R-DATA-017 Open 유지 (false-success 이슈 잔존) · `_common/followups-overview.md` Now 에서 Done recently 로 이동 및 `KRX Step2B DB validation 추가` Next 신규 등록
  `_common/operator-decisions.md` OD-MS-040 Details 에 CRAWLER Windows EC2 KST 메모 추가 · `_common/ms-aws-service-decision-matrix.md` 4.3 port-interest-crawler 절 KST 관련 짧은 메모 보강.

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| Risk ID | R-DATA-010 |
| 상태 변화 | 🔴 Open → 🟢 Mitigated |
| 요약 | ECS Fargate TZ 패치(2026-07-08) + CRAWLER Windows EC2 KST 변경 + KRX 재수집 완료로 최신성 회복 확인 · 정기 audit + detection 유지 |

| 항목 | 값 |
| --- | --- |
| Risk ID | R-DATA-017 |
| 상태 변화 | 🔴 Open 유지 |
| 요약 | Step2B false-success (worker exit 0 · SSM ResponseCode 0 · Step Functions SUCCEEDED ≠ KRX raw 최신성) 잔존 |

### 📌 Follow-ups

- [ ] `run_krx_worker_daily.ps1` 에서 `[Error]` 감지 시 exit 1 로 종료 — 08 spec
- [ ] `interest_program.py` / `interest_shortsell.py` 실패 시 `sys.exit(1)` — 08 spec
- [ ] Step2B 뒤 DB validation 추가 (expected trade_date 기준 program >= 1 · shortsell = 349 · 검증 실패 시 Step Functions Fail) — 04, 08 spec
- [ ] CRAWLER Windows EC2 timezone KST 유지 정책은 OD-MS-040 Details 메모로 관리 · 근본 개선(코드 timezone-aware helper) 은 08 spec 후속 phase 유지

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS · DB · psql · Spring Boot · Lambda · Step Functions · ECS · SSM · EC2 · IAM · RDS 실행 | Kiro 실행 0건 · 운영자 직접 수행 |
| broker · KIS · crawler · 자동 매수 · 자동 매도 · fill sync · position sync · intraday monitor 실행 | 0건 |
| secret · password · token · webhook URL 원문 신규 기록 | 0건 |
| accountNo · account-id · 실제 ARN · public IP · broker_order_no · CommandId · executionArn · instance-id 원문 신규 기록 | 0건 |
| raw SQL 전체 출력 · CloudWatch 전문 · Step Functions history 전문 신규 기록 | 0건 |
| git add · commit · push 실행 | 0건 |
| placeholder 정책 | `[REDACTED_ARN]` · `[REDACTED_ACCOUNT]` · `[REDACTED_INSTANCE_ID]` · `[REDACTED_COMMAND_ID]` 계열만 사용 |

## 2026-07-08 (view-daily-batch-ops-mirror-ui-consumption-confirmed)

### 🧭 Summary

2026-07-03 완료된 Step Functions 실행 이력 OPS Mirror(OD-DB-012 · OD-MS-039 · R-AUTO-038) 의 후속 UI consumption 검증을 완료했다. · `view_app` 기준 `ops` schema 권한 정합 확인 · `ops.strategy_daily_batch_run` · `ops.strategy_daily_batch_step_log` SELECT 확인
`/daily-batch` 화면이 `AWS_STEPFUNCTIONS` run 이력을 실 렌더링하는 것까지 확인했다. · Kiro 는 본 회차에서 문서만 갱신한다.

### ✅ Completed

- 🟢 DB 권한 확인 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 대상 role | `view_app` |
| `ops` schema USAGE | 확인 |
| `ops.strategy_daily_batch_run` SELECT | 확인 |
| `ops.strategy_daily_batch_step_log` SELECT | 확인 |

- 🟢 OPS Mirror 적재 확인 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| `AWS_STEPFUNCTIONS` run 이력 적재 | 확인 |
| 최근 `run_status` 흐름 | SUCCESS |
| run #61 계열 | Step 12~17 |
| run #60 계열 | Step 1~11 |

- 🟢 step_log 상세 확인 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| #61 step_log 대표 항목 | `SFN_STEP12_17_WORKFLOW` / SUCCESS |
| #60 step_log 대표 항목 | `APPROVAL_BLOCKED` / SKIPPED |
| run + step 상세 데이터 조회 | 가능 |

- 🟢 View 화면 확인 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| `/daily-batch` 실행 모드 표시 | aws-stepfunctions |
| 최근 실행 이력 라벨 | `AWS_STEPFUNCTIONS` |
| 선택 run 상세 렌더링 | 확인 (#61) |
| 선택 run step log 렌더링 | 확인 (#61) |
| AWS Step 1~11 시작 버튼 | 표시 |
| AWS Step 12~17 승인 실행 버튼 | 표시 |
| Local File 실행 모드 | OFF |
| aws-stepfunctions 실행 모드 | ON |

- 🟢 최종 판정

| 항목 | 값 |
| --- | --- |
| View Daily Batch / OPS Mirror 기능 | 확인 완료 |
| OPS Mirror DB 적재 | 정상 |
| View 조회 권한 | 정상 |
| `/daily-batch` run 목록 · selected run · step log 렌더링 | 정상 |

### 🔵 Evidence

- 🔵 상세 SELECT 결과 전문 · raw HTML · run/step 상세 JSON · executionArn · stateMachineArn · accountNo 원문은 본 로그에 붙여 넣지 않는다. 필요 시 각 spec `operation-notes.md` 로 분리한다.
- 🔵 `_common/operator-decisions.md` OD-MS-039 · `_common/risk-register.md` R-AUTO-038 Mitigation history 2026-07-08 보강 · `_common/followups-overview.md` Done recently 2026-07-08 row.

### 📌 Follow-ups

- [ ] `/daily-batch` 화면의 `requestPayload` / `resultPayload` 표시 redaction 검토 — 05 spec.
- [ ] `accountNo` 원문 마스킹 검토 (`[REDACTED_ACCOUNT_NO]` 계열 화면 표기) — 05 spec.
- [ ] `executionArn` / `stateMachineArn` 원문 redaction 검토 — 05 spec.
- [ ] `timestamp` 를 UTC 원문 대신 KST 표시로 보완 검토 — 05 spec.
- [ ] #60 계열 `APPROVAL_BLOCKED` / SKIPPED 표시 문구를 "승인 대기 / 승인 차단 정상 종료" 같은 운영자 친화적 문구로 개선 검토 — 05 spec.

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS · DB · psql · Spring Boot · Lambda · Step Functions · ECS · SSM · EC2 · IAM · RDS 실행 | Kiro 실행 0건 · 운영자 직접 수행 |
| broker · KIS · crawler · 자동 매수 · 자동 매도 · fill sync · position sync · intraday monitor 실행 | 0건 |
| secret · password · token · webhook URL 원문 신규 기록 | 0건 |
| accountNo · account-id · 실제 ARN · public IP · broker_order_no 원문 신규 기록 | 0건 |
| raw payload 전문 · raw HTML · SQL 전체 출력 신규 기록 | 0건 |
| git add · commit · push 실행 | 0건 |
| placeholder 정책 | `[REDACTED_ACCOUNT_NO]` · `[REDACTED_ARN]` · `[REDACTED_PUBLIC_IP]` · `[REDACTED_SECRET_ARN]` 계열만 사용 |

## 2026-07-08 (daily-step1-11-ecs-timezone-tz-patch · stale data 원인 확정)

### 🧭 Summary

DB 최신일자 2026-07-06 정체 원인을 ECS Fargate UTC timezone + timezone 없는 `datetime.now()` 사용으로 확정했고, crawler · preprocessor TaskDefinition 에 `TZ=Asia/Seoul` 을 추가한 단기 패치를 적용했다. · State Machine `portfolio-paper-daily-step1-17-approval` 의 Step2A
Step3 task revision 도 신규 revision 으로 갱신했다. · 단, Step1~11 재실행은 하지 않고 다음 자동 실행일인 2026-07-09 08:00 KST 자동 실행에서 최신성 회복 여부를 검증한다. · Kiro 는 본 회차에서 문서만 갱신한다.

### ✅ Completed

- 🟠 DB 최신성 이상 발견

| 항목 | 값 |
| --- | --- |
| Daily Step1~11 자동 실행 (2026-07-08 08:00 KST) | SUCCEEDED |
| interest_price_raw latest | 2026-07-06 |
| interest_investorflow_raw latest | 2026-07-06 |
| interest_program_raw latest | 2026-07-06 |
| interest_shortsell_raw latest | 2026-07-06 |
| pre_total_market_daily_feature latest | 2026-07-06 |
| pre_total_stock_daily_feature latest | 2026-07-06 |
| decision.strategy_daily_run | run_date 2026-07-07 · data_date 2026-07-06 |
| execution.strategy_execution_plan | plan_date 2026-07-07 · NO_CANDIDATE |

- 🟠 Step Functions / ECS TaskDefinition 확인

| 항목 | 값 |
| --- | --- |
| State Machine (변경 전) | `portfolio-paper-daily-step1-17-approval` |
| Step2A task (변경 전) | `portfolio-paper-interest-crawler:7` |
| Step2A command | `python interest_crawler_daily_nongui.py` |
| Step3 task (변경 전) | `portfolio-paper-interest-preprocessor:1` |
| Step3 command | `python pre_daily.py` |
| runDate / targetDate / untilDate | Step2A · Step3 모두 별도 파라미터 전달 없음 |

- 🟠 crawler CloudWatch log 확인 (Step2A 실행 결과)

| 항목 | 값 |
| --- | --- |
| interest_news 수집 대상일 | 2026-07-07 |
| interest_agency 수집 대상일 | 2026-07-06 · 2026-07-07 |
| interest_price 수집 대상일 | 2026-07-06 |
| interest_investorflow 수집 대상일 | 2026-07-06 |
| interest_marketbreadth 수집 대상일 | 2026-07-06 |

- 🟢 holiday 오판 배제

| 항목 | 값 |
| --- | --- |
| `interest_get_holidays.is_holiday(2026-07-07, "KR")` | False |
| 2026-07-07 KRX 정상 개장 | 확인 |
| 판정 | 2026-07-07 휴장일 오판 원인 아님 |

- 🟢 UTC timezone 원인 확정

| 항목 | 값 |
| --- | --- |
| 실제 Step2A 실행 시각 | 2026-07-08 08:01 KST |
| 동일 시각 UTC | 2026-07-07 23:01 |
| 컨테이너 기본 timezone | UTC 로 관찰 |
| `datetime.now().date()` 계산 결과 | 2026-07-07 |
| `-1 day` 적용 후 target date | 2026-07-06 |
| 판정 | 컨테이너 UTC + naive `datetime.now()` 사용이 stale target date 직접 원인 |

- 🟢 crawler:8 / preprocessor:2 등록 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 신규 TaskDefinition (1) | `portfolio-paper-interest-crawler:8` |
| 신규 TaskDefinition (2) | `portfolio-paper-interest-preprocessor:2` |
| 추가 env | `TZ=Asia/Seoul` |
| 이미지 · 명령 · IAM Role · 리소스 스펙 | 변경 없음 |

- 🟢 State Machine Step2A / Step3 task revision 갱신 (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| 대상 State Machine | `portfolio-paper-daily-step1-17-approval` |
| Step2A_RunInterestCrawlerNongui | crawler `:7` → `:8` |
| Step3_RunPreprocessor | preprocessor `:1` → `:2` |
| revisionId | `[REDACTED_REVISION_ID]` (원문 미기록) |

- 🟢 crawler:8 / preprocessor:2 one-off TZ smoke 성공

| 항목 | 값 |
| --- | --- |
| crawler:8 TZ_ENV | `Asia/Seoul` |
| crawler:8 time.tzname | `('KST', 'KST')` |
| crawler:8 naive_yesterday | 2026-07-07 |
| preprocessor:2 TZ_ENV | `Asia/Seoul` |
| preprocessor:2 time.tzname | `('KST', 'KST')` |
| preprocessor:2 naive_yesterday | 2026-07-07 |

### 🔵 Evidence

- 🔵 상세 CloudWatch log 전문 · Step Functions execution history 전문 · ECS RegisterTaskDefinition 응답 raw · UpdateStateMachine 응답 raw · SSM output raw 는 본 로그에 붙여 넣지 않는다. 필요 시 각 spec `operation-notes.md` 로 분리한다.
- 🔵 실제 Task ARN · execution ARN · account-id · secret ARN · state machine ARN · Task Definition ARN 원문은 본 로그에 기록하지 않는다. `[REDACTED_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_REVISION_ID]` placeholder 만 사용.

### 📌 Follow-ups

- [ ] 2026-07-09 08:00 KST Daily Step1~11 자동 실행 관찰 — Step2A crawler log 에서 `interest_price` · `interest_investorflow` 가 2026-07-07 을 수집하는지 확인.
- [ ] 2026-07-09 preprocessor latest date 가 2026-07-07 로 올라오는지 확인 (`pre_total_market_daily_feature` · `pre_total_stock_daily_feature`).
- [ ] 2026-07-09 decision `data_date` 가 2026-07-07 로 잡히는지 확인 (`decision.strategy_daily_run`).
- [ ] 2026-07-09 09:01 Step 12~17 자동 실행 결과가 stale data 영향 없이 정상 흐름인지 확인.
- [ ] 근본 개선(코드에서 `datetime.now()` 직접 사용을 timezone-aware helper 로 대체) 는 08 spec 후속 phase 로 유지.

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| Risk ID | R-DATA-010 |
| 상태 | 🔴 Open (partial mitigation) |
| 요약 | non-GUI raw 최신성 부족. 2026-07-08 TZ 단기 패치 적용 · 다음 자동 실행 검증 대기. |

| 항목 | 값 |
| --- | --- |
| Risk ID | R-DATA-017 |
| 상태 | 🔴 Open |
| 요약 | KRX GUI worker 계열 별도 위험. 본 회차 원인과 구분. |

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| Kiro AWS · DB · psql · ECS · Step Functions · Lambda · SSM · EC2 실행 | 0건 (문서만 갱신) |
| ECS RegisterTaskDefinition · State Machine UpdateStateMachine · one-off TZ smoke | 운영자 직접 수행 · Kiro 실행 0건 |
| broker · KIS · crawler · 자동 매수 · 자동 매도 · fill sync · position sync · intraday monitor 실행 | 0건 |
| secret · password · token · webhook URL 원문 신규 기록 | 0건 |
| account-id · 실제 ARN · public IP · broker_order_no · image digest · execution ARN 전체 신규 기록 | 0건 |
| State Machine revisionId 원문 기록 | 0건 (`[REDACTED_REVISION_ID]` 사용) |
| AWS CLI 전체 출력 전문 신규 기록 | 0건 |
| git add · commit · push 실행 | 0건 |
| placeholder 정책 | `[REDACTED*]` 계열만 사용 |

## 2026-07-08 (scheduler-inventory-and-holiday-guard-boost · 운영 상태표 준비 회차)

### 🧭 Summary

운영 상태표 1차 준비 중 Scheduler · Lambda · Step Functions holiday guard 경로를 점검한 회차. · Scheduler 7종 인벤토리를 확인하고, 기존 EC2 lifecycle · Daily scheduler dispatcher 의 holiday guard 는 확인만 수행.
Intraday scheduler dispatcher 와 Daily Brief Slack Summary Builder 에는 holiday guard 를 신규 보완했다. · 실제 Lambda 배포 · env 추가 · Step Functions definition 변경 · dryRun 검증은 운영자가 직접 완료했고, Kiro 는 본 회차에서 문서만 수정한다.

### ✅ Completed

- 🟢 Scheduler 인벤토리 재확인

| 항목 | 값 |
| --- | --- |
| 총 Scheduler 수 | 7 |
| ENABLED | 7 |
| DISABLED | 0 |
| Timezone | Asia/Seoul |
| FlexibleTimeWindow | OFF |
| 요일 기준 | MON-FRI |
| DLQ | 없음 |

- 🟢 자동화 라인업 7종 상태

| 항목 | 값 |
| --- | --- |
| 시각 | 07:50 |
| 대상 | 장전 Slack |
| 상태 | ENABLED |

| 항목 | 값 |
| --- | --- |
| 시각 | 07:50 |
| 대상 | EC2 start |
| 상태 | ENABLED |

| 항목 | 값 |
| --- | --- |
| 시각 | 08:00 |
| 대상 | Step 1~11 |
| 상태 | ENABLED |

| 항목 | 값 |
| --- | --- |
| 시각 | 09:01 |
| 대상 | Step 12~17 |
| 상태 | ENABLED |

| 항목 | 값 |
| --- | --- |
| 시각 | 09:10~15:50 |
| 대상 | 장중 snapshot / evaluate |
| 상태 | ENABLED |

| 항목 | 값 |
| --- | --- |
| 시각 | 15:50 |
| 대상 | 장후 Slack |
| 상태 | ENABLED |

| 항목 | 값 |
| --- | --- |
| 시각 | 15:50 |
| 대상 | MarketConnector stop |
| 상태 | ENABLED |

- 🟢 holiday guard 경로 점검 결과

| 경로 | 상태 |
| --- | --- |
| EC2 lifecycle dispatcher (start · holidayCheck=true) | 기존 guard 확인 |
| EC2 lifecycle dispatcher (stop) | 안전 종료 목적으로 휴일에도 stop 가능 구조 유지 |
| Daily scheduler dispatcher (Step 1~11 · Step 12~17) | 기존 guard 확인 |
| Intraday scheduler dispatcher | guard 신규 배포 · env 2종 추가 · dryRun 검증 완료 |
| Daily Brief Slack workflow | Builder guard 배포 · state machine `CheckHolidaySkip` Choice 삽입 · 휴일 dryRun 확인 |

- 🟢 최종 판정

| 항목 | 값 |
| --- | --- |
| Scheduler 자체 | 휴일 제외가 아니라 MON-FRI cron 기준 |
| 휴일 실제 차단 지점 | Lambda / State Machine 내부 guard |
| 확인된 차단 경로 | EC2 start · Daily Step 1~11 · Daily Step 12~17 · Intraday · Daily Brief Slack |
| 유지 구조 | EC2 stop 은 휴일에도 호출 가능 |

### 🔵 Evidence

- 🔵 Intraday dispatcher 의 dryRun 검증에서 `runDate=2026-07-08` 는 `skipped=false` / `MARKET_DAY_CANDIDATE`, `runDate=2026-08-15` 는 `skipped=true` / `WEEKEND` 라벨로 관찰됨. 실제 SSM 실행 없이 dryRun 만 사용.
- 🔵 Daily Brief Slack workflow 휴일 dryRun 에서 `SkipDailyBriefSlack` 진입 확인 · `SendSlackNotifier` 미진입 확인.
- 🔵 상세 Lambda 소스 / env 값 / state machine ASL / execution ARN / dryRun 응답 전문은 본 로그에 기록하지 않는다.

### 📌 Follow-ups

- [ ] 다음 평일 07:50 자동 장전 Slack · 15:50 장후 Slack 실 수신 관찰 (Daily Brief mini workflow — 아직 실전 수신 미확인).
- [ ] Holiday API 백업 경로 / fallback 정책 (R-AUTO-028 mitigation 확장 후속) 유지.
- [ ] 최근 Step Functions execution 확인 · DB 최신 일자 쿼리 확인 · View Daily Batch · OPS Mirror 확인 · 운영 상태표 1차 작성 후속 유지.

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS · DB · psql · Spring Boot 실행 | 0건 |
| Lambda 코드 배포 · env 추가 · Step Functions definition 업데이트 | Kiro 실행 0건 · 운영자 직접 수행 |
| SSM · EC2 · broker · KIS · crawler 실행 | 0건 |
| secret · password · token · webhook URL 원문 신규 기록 | 0건 |
| account no · 실제 ARN · public IP · broker_order_no · image digest · Lambda zip SHA256 · execution ARN 전체 신규 기록 | 0건 |
| AWS CLI 전체 출력 전문 신규 기록 | 0건 |
| git add · commit · push 실행 | 0건 |
| placeholder 정책 | `[REDACTED*]` 계열만 사용 |

## 2026-07-07 (root-docs-readability · Kiro 실행 오버헤드 개선 · _common 10차 결과 요약)

### 🧭 Summary

`.kiro` 루트 3개 문서(README · WORKLOG · CHANGELOG) 가독성 정리를 시도한 회차. 실제 AWS 운영/기능 구현 없이 문서 편집과 Kiro 작업 방식 점검만 수행. 목표 스타일은 2026-07-01 CHANGELOG 스타일(표 중심 · 짧은 bullet · Security 표). 병행하여 `_common` 10차 회차 결과 요약을 본 로그에 기록.

### ✅ Completed

- 🟢 루트 3개 문서 가독성 목표 정의

| 항목 | 값 |
| --- | --- |
| 대상 | `.kiro/README.md` · `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` |
| 목표 스타일 참조 | 2026-07-01 CHANGELOG 섹션 |
| 목표 서식 | 표 중심 · 짧은 bullet · Security 표 중심 |
| 실제 수정 범위 | 루트 3개 문서 한정 |
| 대상 아님 | `.kiro/specs/**` · 8개 MS README/CHANGELOG/docs/worklog/AGENTS.md |

- 🟢 `_common` 10차 결과 요약 기록

| 항목 | 값 |
| --- | --- |
| 대상 | `_common/risk-register.md` · `_common/followups-overview.md` · `_common/operator-decisions.md` |
| risk-register.md over300 | 134 → 9 |
| followups-overview.md over300 | 210 → 70 |
| operator-decisions.md over300 | 240 → 2 |
| tableO300 | 0건 |
| 인코딩 | UTF-8 No BOM 유지 |
| 신규 민감정보 원문 유입 | 0건 |
| 판정 | 장문 Details 이동 완료 |

- 🟠 Kiro 실행 오버헤드 확인

| 항목 | 값 |
| --- | --- |
| 지연 지점 | 루트 3개 파일 위반 매트릭스 정리 단계 |
| 원인(1) | ViolationRecord matrix 생성 |
| 원인(2) | content_hash 기록 |
| 원인(3) | safety_exception 분류 |
| 원인(4) | sub-agent 호출 시도 |
| 원인(5) | workspace 밖/TEMP 기반 scan 시도 |
| 원인(6) | 긴 PowerShell one-liner · trust prompt 발생 |
| 판정 | 문서 국소 편집에 비해 audit pipeline 과도 |

- 🟠 대응 방향(문서화만 · 규칙 변경은 별도 회차)

| 항목 | 값 |
| --- | --- |
| 원칙 유지 | 0번 최우선 문서 가독성 규칙 |
| 기본 적용 범위 | 새로 작성/직접 수정하는 문장 · 표 · bullet 로 한정 |
| 기존 문서 전체 위반 | 명시 요청 없으면 수정하지 않고 후속 후보로만 기록 |
| 전수 감사 항목 | ViolationRecord matrix · content_hash · 전체 before/after audit 은 명시 요청 시만 수행 |
| AGENTS.md 실제 수정 | 본 회차 미수행 · 별도 후속 회차 |

### 🔵 Evidence

- 🔵 `_common` 10차 회차의 실제 편집 상세는 각 파일 자체와 `.kiro/CHANGELOG.md` 10차 관련 항목에서 참조 가능. 본 회차는 루트 로그 요약만 기록.
- 🔵 본 회차는 실제 AWS 리소스 생성/수정/삭제 없이 문서 편집과 작업 방식 점검만 수행.

### ⚠️ Risks

- 🟠 루트 3개 문서 가독성 정리 원안이 audit pipeline 과부하로 완전 반영되지 못했음. 잔여 라인 위반은 후속 회차 대상.
- 🟠 0번 규칙의 적용 범위가 명문화되지 않아 다음 회차에서도 전수 스캔이 재발할 여지가 있음. AGENTS.md 보강 회차로 분리 예정.

### 📌 Follow-ups

- [ ] AGENTS.md 0번 규칙에 "기본 적용 범위 = 새로 작성/직접 수정 부분" 조항 명시(별도 회차).
- [ ] 전수 스캔/ViolationRecord matrix/content_hash 는 명시 요청 시만 수행한다는 예외 조항 명시(별도 회차).
- [ ] 루트 3개 문서 잔여 가독성 정리 대상 재산정 후 국소 편집으로 재시도.
- [ ] `_common` 3개 파일 잔여 over300 라인(risk-register 9 · followups-overview 70 · operator-decisions 2) 후속 감소 판단.

### 🔐 Security

- 🟢 실행 카테고리 (문서만 수정 · 각 0건)

| 항목 | 값 |
| --- | --- |
| AWS · DB · psql · Spring Boot 실행 | 0건 |
| Slack webhook · Step Functions · Lambda · ECS 실행 | 0건 |
| SSM · EC2 · broker · KIS · crawler 실행 | 0건 |
| 자동 매수 · 자동 매도 · fill sync · position sync 실행 | 0건 |
| intraday monitor · live cutover 실행 | 0건 |

- 🟢 git 작업

| 항목 | 값 |
| --- | --- |
| 쓰기 계열 (`git add` · `git commit` · `git push`) | 0건 |
| 롤백 계열 (`git reset` · `git checkout` · `git restore`) | 0건 |
| 상태 확인 명령 | 미실행 |

- 🟢 민감정보 원문 신규 기록 (각 0건)

| 항목 | 값 |
| --- | --- |
| secret · password · token · webhook URL | 0건 |
| KIS app key · KIS app secret | 0건 |
| 계좌번호 · account-id · 실제 ARN | 0건 |
| 실제 public IP · broker_order_no · image digest full sha256 | 0건 |
| placeholder 정책 | `[REDACTED*]` 계열만 사용 |

## 2026-07-06 (spec-docs-readability-sessions · 01~09 spec 하위 폴더 4개 Session 병렬 가독성 개선)

### 🧭 Summary

`.kiro/specs/` 01~09 하위 폴더 문서를 Session A ~ D 4개 그룹으로 나눠 병렬 가독성 개선을 진행했다. 실제 AWS 운영/기능 구현 없이 문서 편집만 수행한 회차.

- 각 Session 은 별도 대상 범위와 개별 목표(>300 · >500 라인 축소 · table row / bullet 축소 · placeholder coverage 정리)로 진행.
- critical fact-loss 없음 · secret · 실제 ARN · account-id · public IP · broker_order_no 원문 신규 기록 0건.

### ✅ Completed

- 🟢 Session A — 01~03 spec 문서 가독성 개선

| 항목 | 값 |
| --- | --- |
| 범위 앞부분 | 01-aws-migration-foundation · 02-aws-network-and-rds |
| 범위 뒷부분 | 03-marketconnector-ec2 |
| 수정 파일 수 | 13개 |
| 원본 유지 파일 수 | 5개 |
| >300 · >500 라인 | 18개 파일 전체 0건 달성 |
| 초장문 table row · bullet | 0건 |
| secret · ARN · account-id · public IP 원문 추가 | 0건 |

- 🟢 Session B — 04~05 spec 문서 가독성 개선

| 항목 | 값 |
| --- | --- |
| 범위 앞부분 | 04-strategy-batch-stepfunctions / operation-notes.md |
| 범위 중간부 | 05-port-view-ecs-and-runbook / operation-notes.md |
| 범위 뒷부분 | 05 runbook.md · validation-checklist.md |
| 04 신설 앞부분 | Automation Lineup Dashboard |
| 04 신설 뒷부분 | 일자별 인덱스 |
| 05 신설 | View 실행 위치별 backend 매트릭스 |
| >500 라인 | 04 / 05 주요 문서 0건 달성 |
| Table rows >300 · >500 | 0건 |
| Bullets >300 · >500 | 0건 |
| secret 원문 추가 | 0건 |

- 🟢 Session C — 06 secrets-and-iam spec 문서 가독성 개선

| 항목 | 값 |
| --- | --- |
| 범위 앞부분 | requirements.md · design.md · tasks.md |
| 범위 뒷부분 | operation-notes.md · runbook.md · validation-checklist.md |
| 수정 파일 수 | 6개 |
| gt300 (>300 라인) | 39 → 0 |
| gt500 (>500 라인) | 9 → 0 |
| Placeholder Coverage | 정리 완료 |
| 보안 정책 약화 | 없음 |
| 민감정보 원문 신규 기록 | 0건 |

- 🟢 Session D — 08 / 09 data · research 계열 spec 문서 가독성 개선

| 항목 | 값 |
| --- | --- |
| 범위 앞부분 | 08 requirements.md · design.md · tasks.md · operation-notes.md |
| 범위 뒷부분 | 09 operation-notes.md |
| 수정 파일 수 | 5개 |
| 편집 방식 | additive-only |
| 신설 블록 앞부분 | Role Split · Runtime Role Split · Task Section Overview |
| 신설 블록 뒷부분 | Open Risks & Next Checks · Key Fact Preservation |
| 500자 초과 라인 | 0건 |
| fact-loss (stale data · latest_trade_date · KRX worker · research batch) | 0건 |
| secret 원문 추가 | 0건 |

### 🔵 Evidence

- 🔵 각 Session 별 실제 수정 파일 상세는 개별 spec 폴더의 operation-notes.md · runbook.md · validation-checklist.md 안에서 확인 가능(본 회차에서 해당 파일들은 수정 대상이었으므로 하위 문서 우선 참조).
- 🔵 본 회차는 실제 AWS 운영/기능 구현이 아닌 `.kiro/specs` 문서 가독성 개선 전용 회차임을 명시.

### ⚠️ Risks

- 신규 리스크 없음. R-DOCS-001(secret 평문 기록 금지) 정합 유지.

### 📌 Follow-ups

- [ ] 07 spec 하위 폴더 가독성 개선 대상 여부 별도 판단.
- [ ] 10 spec 이후 후속 spec 대상 회차 판단.
- [ ] Session A ~ D 각 Session 결과의 baseline metric 을 각 spec operation-notes.md 안에 소규모 append 여부 판단.

### 🔐 Security

- 🟢 실행 카테고리 (문서만 수정 · 각 0건)

| 항목 | 값 |
| --- | --- |
| AWS · DB · psql · Spring Boot 실행 | 0건 |
| Slack webhook · Step Functions · Lambda · ECS 실행 | 0건 |
| SSM · EC2 · broker · KIS 실행 | 0건 |
| 크롤러 · Selenium 실행 | 0건 |
| 자동 매수 · 자동 매도 · fill sync · position sync 실행 | 0건 |
| intraday monitor · live cutover 실행 | 0건 |

- 🟢 민감정보 원문 신규 기록 (각 0건)

| 항목 | 값 |
| --- | --- |
| secret · password · token · webhook URL | 0건 |
| KIS app key · KIS app secret | 0건 |
| 계좌번호 · account-id · 실제 ARN | 0건 |
| 실제 public IP · broker_order_no · image digest full sha256 | 0건 |
| placeholder 정책 | `[REDACTED*]` 계열만 사용 |

## 2026-07-06 (kiro-common-docs-readability-6-7 · 6~7차 복구 성격 회차)

### 🧭 Summary

`.kiro/specs/_common/` 문서에 대해 6~7차 복구 성격 작업을 수행했다. 8차 라인 밀도 완화(별도 2026-07-02 섹션) · 9차 구조 재편(하단 2026-07-06 섹션) 사이의 복구 전용 회차로 분리 기록한다.

- `operator-decisions.md` 표시 문구 mojibake 복구.
- `risk-register.md` 일부 wrap artifact 복구.
- 단일 문자 bullet · 깨진 한글 · broken wrap 후보 확인.
- UTF-8 No BOM 정책 유지 · 신규 secret 원문 기록 0건.

### ✅ Completed

- 🟢 `_common/operator-decisions.md`

| 항목 | 값 |
| --- | --- |
| 처리 | 표시 문구 mojibake 복구 |
| fact-loss | 없음 |
| Decision ID · Status · [REDACTED*] count | baseline 이상 유지 |

- 🟢 `_common/risk-register.md`

| 항목 | 값 |
| --- | --- |
| 처리 | 일부 wrap artifact 복구 |
| 확인 항목 앞부분 | 단일 문자 bullet · 깨진 한글 |
| 확인 항목 뒷부분 | broken wrap 후보 식별 |
| fact-loss | 없음 |
| Risk ID · Status · [REDACTED*] count | baseline 이상 유지 |

### 🔵 Evidence

- 🔵 6차 / 7차 각 회차의 문서 metrics 는 후속 9차 baseline 문서(2026-07-06 kiro-common-docs-readability-9 섹션)에 흡수 완료.
- 🔵 본 회차는 실제 AWS 운영 없이 문서 국소 편집만 수행.

### ⚠️ Risks

- 신규 리스크 없음. broken wrap 후속 후보만 관찰 유지.

### 📌 Follow-ups

- [ ] 후속 문서 편집 회차에서 broken wrap 후보 추가 발견 시 국소 복구.
- [ ] mojibake / wrap artifact 재발 감지 시 재확인.

### 🔐 Security

- 🟢 실행 · 민감정보 원문 신규 기록 (각 0건)

| 항목 | 값 |
| --- | --- |
| AWS · DB · Slack · crawler · broker · KIS 실행 | 0건 |
| Step Functions · Lambda · SSM · psql · Spring Boot 실행 | 0건 |
| secret · password · token · webhook · 실제 ARN · account-id | 0건 |
| public IP · broker_order_no · image digest full sha256 원문 | 0건 |
| 6개 대상 문서 저장 인코딩 | UTF-8 No BOM 유지 |

## 2026-07-06 (kiro-common-docs-readability-9 · 9차 회차 · 6개 _common 문서 구조 재편)

### 🧭 Summary

`.kiro/specs/_common/` 6개 문서를 `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` 처럼 예측 가능한 3단 고정 흐름으로 재편했다.

- 흐름 구성: 상단 Purpose/Dashboard/Summary · 중간 Short Index · 하단 Details/Appendix/Historical Notes/Usage Notes.
- 성공 기준은 라인 감소가 아닌 예측 가능한 구조.
- 6개 문서 모두에 `## Purpose` 상단 헤더 신설 · 안전 제약 섹션을 `## Security Notes` 로 rename 해 파일 끝으로 정렬.
- `risk-register.md` 의 pre-existing UTF-8 BOM 은 Option A(제거) 로 처리.
- Decision ID · Risk ID · 날짜 · USD · Status · `[REDACTED*]` count 는 모두 baseline 이상으로 보존됨.

### ✅ Completed

- `_common/operator-decisions.md` — 아래 항목 정리.
   - Purpose 신설.
   - Decision Summary → Decision Dashboard rename (재집계 필요 표시).
   - Detailed Decisions → Decision Index rename.
   - Deferred Decisions 를 Decision Details 앞으로 이동.
   - 중복 H2 (Compute/Service Placement) 를 H3 로 강등.
   - 본 spec 작업 안전 제약 → Security Notes rename 및 파일 끝 이동.
   - Decision Update Rules 를 Change Log Details 뒤로 이동.
   - H2 순서 target 정합.
- `_common/risk-register.md` — 아래 항목 정리.
   - pre-existing BOM Option A 제거 (186458→186455 bytes).
   - Purpose 신설.
   - Risk Table → Risk Index rename.
   - 컬럼 정의/상태 정의 H2 → H3 강등.
   - Accepted/Closed Risks 요약 섹션 신설(`R-AUTO-024` · `R-AUTO-027`).
   - 추가 식별 시 갱신 규칙 → Risk Update Rules rename.
   - 본 문서 작업 안전 제약 → Security Notes rename.
- `_common/followups-overview.md` — 아래 항목 정리.
   - Purpose 신설.
   - 환경 모델/진행 순서/각 후속 Spec 요약/의존성 다이어그램을 Spec Roadmap H2 하위 H3 로 통합.
   - Update Rules 신설.
   - 공통 작업 범위 제한 → Security Notes rename.
- `_common/aws-resource-glossary.md` — 아래 항목 정리.
   - Purpose 신설.
   - 카테고리별 목차 → Category TOC rename.
   - Glossary H2 신설.
   - 38개 개별 서비스 H2 → H3 강등 (남은 H2: Purpose · Category TOC · Glossary · Usage Notes · Update Rules · Security Notes).
   - Usage Notes 신설 (EventBridge Scheduler · Lambda · IAM Role · ECS/Fargate · Step Functions 우선 이관 방침 명시).
   - Update Rules 신설.
   - 본 spec 작업 안전 제약 → Security Notes rename.
- `_common/ms-aws-service-decision-matrix.md` — 아래 항목 정리.
   - Purpose 신설 (서비스 선택 결론 불변 명시).
   - chapter 5 → MS Decision Cards rename.
   - chapter 6 → Portfolio Appeal Notes rename.
   - Rejected/Deferred Services H2 신설 후 chapter 7/8/9 을 하위 H3 로 흡수.
   - chapter 10 → Appendix 하위 H3 흡수.
   - Evidence Details H2 → H3 강등.
   - Security Notes 신설.
- `_common/cost-simulation.md` — 최소 변경. Purpose 신설. chapter 8 안전 제약 → Security Notes rename. 300자 초과 0 상태 유지 · 신규 비용 수치 추가 없음 · USD count 3 그대로.

### 🔵 Evidence

- 🔵 재구성 전/후 fact-loss 대조 및 라인 count 정리 — `.kiro/specs/kiro-common-docs-readability-9/tasks.md` Task 9 검증 결과 참조.
- 🔵 파일별 lines · 300자 초과 count (Baseline → After)

| 파일 | 값 |
| --- | --- |
| operator-decisions.md | 2045 · 238 → 2057 · 240 |
| followups-overview.md | 1443 · 209 → 1463 · 210 |
| risk-register.md | 1062 · 133 (pre-existing BOM) → 1093 · 134 (BOM 제거) |
| aws-resource-glossary.md | 493 · 35 → 527 · 36 |
| ms-aws-service-decision-matrix.md | 1065 · 41 → 1100 · 42 |
| cost-simulation.md | 445 · 0 → 447 · 0 |

### ⚠️ Risks

- 🟠 표 셀 300자 초과 라인 Details 이동

| 항목 | 값 |
| --- | --- |
| 본 회차 처리 | surface-level 정리만 수행 |
| 개별 Details 콘텐츠 이동 | 후속 회차 이연 |
| 방식 | fact-loss 위험 없이 순차 이동 예정 |

- 🟠 `operator-decisions.md` 미통합 항목

| 항목 | 값 |
| --- | --- |
| OD-MS-028 중복 row | 미통합 |
| Decision Summary count 재집계 | Review Needed 로 이월 유지 |

- 🟠 `ms-aws-service-decision-matrix.md` chapter 1 · 2 · 3 · 4

| 항목 | 값 |
| --- | --- |
| 위치 | Final Recommendation Summary 뒤 그대로 |
| Appendix 로의 물리적 이동 | 회피 (anchor 안정성 이유) |

### 📌 Follow-ups

- [ ] 표 셀 안 3문장 이상 · 120자 이상 · 4개 이상 슬래시 체인 잔존 항목의 Decision Details / Risk Details / Usage Notes 개별 이동(다음 회차).
- [ ] `operator-decisions.md` OD-MS-028 중복 row 통합 판단.
- [ ] `operator-decisions.md` Decision Summary count 재집계.
- [ ] `ms-aws-service-decision-matrix.md` chapter 1 · 2 · 3 · 4 를 Appendix 하위 물리적 이동.

### 🔐 Security

- 🟢 실행 카테고리 (문서만 수정 · 각 0건)

| 항목 | 값 |
| --- | --- |
| AWS · psql · Spring Boot 실행 | 0건 |
| Slack webhook · Step Functions · Lambda · ECS 실행 | 0건 |
| SSM · EC2 · broker · KIS 실행 | 0건 |
| 크롤러 · Selenium 실행 | 0건 |
| 자동 매수 · 자동 매도 · fill sync · position sync 실행 | 0건 |
| intraday monitor · live cutover 실행 | 0건 |

- 🟢 git 작업

| 항목 | 값 |
| --- | --- |
| 쓰기 계열 앞부분 | `git add` · `git commit` |
| 쓰기 계열 뒷부분 | `git rm` · `git mv` · `git push` |
| 쓰기 계열 실행 | 0건 |
| 롤백 계열 앞부분 | `git checkout` · `git reset` |
| 롤백 계열 뒷부분 | `git stash` · `git restore` |
| 롤백 계열 자동 실행 | 0건 |
| 상태 확인 명령 | `git status --short` 만 사용 |

- 🟢 민감정보 원문 신규 기록 (각 0건)

| 항목 | 값 |
| --- | --- |
| secret · password · token · webhook URL | 0건 |
| KIS app key · KIS app secret | 0건 |
| 계좌번호 · account-id · 실제 ARN | 0건 |
| 실제 public IP · broker_order_no · image digest full sha256 | 0건 |
| placeholder 정책 | `[REDACTED*]` 계열만 사용 |

- 🟢 인코딩

| 항목 | 값 |
| --- | --- |
| 6개 대상 문서 저장 인코딩 | UTF-8 No BOM 통일 |
| `risk-register.md` pre-existing BOM | Option A (제거) 처리 |

## 2026-07-03 (Step Functions 실행 이력 OPS mirror 기반 완료)

### 🧭 Summary

AWS Step Functions 실행 이력을 OPS 테이블에 mirror 하기 위한 기반이 완료됐다.

- 대상 테이블: `ops.strategy_daily_batch_run` · `ops.strategy_daily_batch_step_log`.
- `run_type` 에 `AWS_STEPFUNCTIONS` 추가 · 전용 최소 권한 role · `portfolio-daily-batch-ops-recorder` Lambda 사용.
- RECORD_START / RECORD_STEP / RECORD_SUCCESS smoke 통과 · commit smoke `batchRunId=53` · Step 12 approval-blocked smoke 후 OPS 테이블 기록 확인.
- View Daily Batch 화면이 AWS Step Functions run 이력을 조회할 수 있는 기반이 마련됨(2026-07-02 View 화면에서 확인된 Daily Batch 이력 불일치 원인 정합).

### ✅ Completed

1. OPS mirror 기반 완료
   1) DB 스키마 정합:완료
       - `ops.strategy_daily_batch_run` · `ops.strategy_daily_batch_step_log` 사용
       - `run_type` 값에 `AWS_STEPFUNCTIONS` 추가
   2) 실행 주체 분리:완료
       - Lambda `portfolio-daily-batch-ops-recorder`
       - 전용 최소 권한 role `ops_recorder_app` 채택
   3) Smoke 통과:완료
       - RECORD_START · RECORD_STEP · RECORD_SUCCESS 3종 smoke
       - commit smoke `batchRunId=53`
       - Step 12 approval-blocked smoke 후 OPS 테이블 기록 확인

### 🔵 Evidence

- 🔵 결정/리스크/후속 상세

| 문서 | 위치 |
| --- | --- |
| operator-decisions | `OD-DB-012` · `OD-MS-039` |
| risk-register | `R-AUTO-038` |
| followups-overview | 2026-07-03 항목 |

### ⚠️ Risks

- 🟢 신규 리스크 `R-AUTO-038` 등록 · 초기 상태 `Mitigated` (smoke 통과 · 실 운영 관찰 후속).
- 🟠 View Daily Batch 화면과 OPS 테이블 연결은 후속 phase(View 조회 반영 필요).

### 📌 Follow-ups

- [ ] View Daily Batch 화면이 `ops.strategy_daily_batch_run` `AWS_STEPFUNCTIONS` run 이력을 조회하도록 반영.
- [ ] 실 Step 1~17 자동 실행 회차에서 OPS mirror 정합 관찰.
- [ ] `ops_recorder_app` role 최소 권한 정합 재점검(장기 유지 여부).

### 🔐 Security

- 🟢 Kiro 는 본 작업에서 문서만 수정. 실행 0건 · broker 주문 0건 · aws-live 작업 0건 · secret 원문 기록 0건.
- 🟢 AWS CLI · psql · Spring Boot · Slack webhook · Step Functions · Lambda · ECS · SSM · KIS API 실행 0건.
- 🟢 신규 secret · password · token · webhook URL · 계좌번호 · 실제 ARN · public IP · broker_order_no 원문 기록 0건 · `[REDACTED*]` placeholder 정책 유지.

## 2026-07-02 (portfolio-event-notifier Slack 문구 개선 + View 조회 화면 안정화 smoke)

### 🧭 Summary

`portfolio-event-notifier` 성공 계열 Slack 문구 개선과 View 조회 화면 기준선 smoke 를 함께 통과했다.

- APPROVAL_REQUIRED · DAILY_EXECUTION_SUCCESS 2종 성공 formatter smoke 통과.
- View 화면 7종(`/dashboard` · `/balance-summary` · `/positions` · `/orders` · `/strategy/execution/plans` · `/strategy/reports/latest` · `/daily-batch`) 기준선 smoke 통과.
- 최신 balance snapshot `id=357` · `as_of_date=2026-07-02` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505` · 보유 종목 0건.

### ✅ Completed

1. Slack 문구 개선
   1) APPROVAL_REQUIRED formatter:완료
       - 제목 `[Daily 검증] 성공` 으로 정리
       - "승인 대기" 라인 제거
   2) DAILY_EXECUTION_SUCCESS formatter:완료
       - 제목 `[Daily 실행] 성공` 으로 정리
       - `SUCCESS` 표기를 `성공` 으로 통일
   3) Smoke 통과:완료
       - 성공 formatter 2종 smoke 수신 확인
       - DAILY_EXECUTION_FAILED formatter 는 별도 smoke 필요 · 후속 유지

2. View 조회 화면 안정화 smoke
   1) 화면 7종 기준선 smoke 통과:완료
       - `/dashboard` · `/balance-summary` · `/positions` · `/orders`
       - `/strategy/execution/plans` · `/strategy/reports/latest` · `/daily-batch`
   2) 최신 balance snapshot 정합:완료
       - `id=357` · `as_of_date=2026-07-02`
       - `total_eval_amount=8,706,505` · `cash_balance=8,706,505` · 보유 종목 0건
   3) /positions 과거 종목 미노출:확인
       - 과거 `connector_position_snapshot` row 가 있어도 최신 기준일 기준으로 화면에 노출되지 않음
   4) Daily Batch 화면 이력 불일치 원인 정리:확인
       - View 조회 버그 아님
       - Step Functions 실행 이력 mirror 부재로 정리(2026-07-03 OPS mirror 후속에서 해소 진행)

### 🔵 Evidence

- 🔵 상세 evidence 링크

| 문서 | 위치 |
| --- | --- |
| followups-overview | 2026-07-02 항목 (Slack · View) |
| risk-register | Slack formatter 계열 리스크 잔여 항목 |

### ⚠️ Risks

- 🟠 DAILY_EXECUTION_FAILED formatter 는 별도 smoke 미통과 상태 유지 · 실 실패 이벤트 노출 시 문구 검증 필요.
- 🟠 Daily Batch 화면 이력 불일치는 View 측 원인 아님 · Step Functions run mirror(2026-07-03) 반영 후 재검증 필요.

### 📌 Follow-ups

- [ ] DAILY_EXECUTION_FAILED formatter smoke 진행.
- [ ] Daily Batch 화면이 OPS mirror `AWS_STEPFUNCTIONS` run 이력을 조회하도록 반영.
- [ ] View 화면 7종 정기 smoke 회차 유지.

### 🔐 Security

- 🟢 Kiro 는 본 작업에서 문서만 수정. 실행 0건 · broker 주문 0건 · aws-live 작업 0건 · secret 원문 기록 0건.
- 🟢 AWS CLI · psql · Spring Boot · Slack webhook · Step Functions · Lambda · ECS · SSM · KIS API 실행 0건.
- 🟢 신규 민감정보 원문 기록 0건 · `[REDACTED*]` placeholder 정책 유지.

## 2026-07-02 (kiro-common-docs-readability · 8차 회차 · 6개 _common 문서 라인 밀도 완화)

### 🧭 Summary

`.kiro/specs/_common/` 6개 문서에 대해 8차 회차 가독성 개선을 수행했다.

- Dashboard / Summary / At a Glance 영역은 유지하고, 표 셀·문단 안의 장문 서술을 bullet 로 쪼개 최대 라인 길이와 >500·>300 라인 수를 축소.
- `cost-simulation.md` 는 >300 라인이 완전히 0에 도달.
- Decision ID · Risk ID · 날짜 · 상태 라벨 · USD 수치 · `[REDACTED*]` placeholder count 는 모두 보존됨.

### ✅ Completed

- `_common/cost-simulation.md` — paper/live realistic 주요 구성 bullet → 표로 재구성, 서두 3줄 분할. >300=1→0.
- `_common/ms-aws-service-decision-matrix.md` — Evidence Details 4.1 · 4.2 · 4.6(2026-06-23 · 6-24) 안의 다수 500자 이상 서술을 bullet 다중 라인 구조로 분할. >500=2→0.
- `_common/followups-overview.md` — 2026-06-13 · 2026-06-17 · 2026-06-18 Historical Notes 안 500자 초과 서술을 bullet 로 쪼개기.
- `_common/operator-decisions.md` — Change Log Details 2026-06-16 · 2026-06-23 · 2026-06-29 (2)·(3) 안 500자 초과 서술을 bullet 로 쪼개기. >500=6→3.
- `_common/risk-register.md` — 본 회차 편집 없음(다른 대상 우선).
- `_common/aws-resource-glossary.md` — 본 회차 편집 없음(다른 대상 우선).

### 🔵 Evidence

- 🔵 Metrics before → after (본 회차 basis 기준)

| 파일 | 값 |
| --- | --- |
| `operator-decisions.md` | 2020 → 2045 lines · >500 6 → 3 · max 557 → 529 |
| `followups-overview.md` | 1420 → 1443 lines · >500 19 → 17 · max 544 → 530 |
| `ms-aws-service-decision-matrix.md` | 973 → 1065 lines · >500 2 → 0 · max 514 → 488 |
| `cost-simulation.md` | 413 → 445 lines · >300 1 → 0 · max 320 → 296 |
| `risk-register.md` · `aws-resource-glossary.md` | unchanged |

- 🔵 Fact preservation (모두 before ≤ after 만족)

| 항목 | 값 |
| --- | --- |
| Decision ID · Risk ID | before ≤ after |
| date · status | before ≤ after |
| USD · REDACTED placeholder count | before ≤ after |

### ⚠️ Risks

- 신규 리스크 없음. R-DOCS-001(secret 평문 기록 금지) 정합 유지.

### 📌 Follow-ups

- [ ] `_common/risk-register.md` 표 셀 축약 및 Risk Details 이동(8차 미완료 · 9차 회차 후보).
- [ ] `_common/aws-resource-glossary.md` 5-field template 정합 및 날짜별 evidence 이동(8차 미완료 · 9차 회차 후보).
- [ ] `_common/operator-decisions.md` Detailed Decisions row 안 3문장 이상 근거 Change Log Details 로 추가 이동(8차 부분 완료 · 9차 회차 후보).
- [ ] `_common/followups-overview.md` Historical Notes 날짜당 3~5 bullet 정합 재조정(8차 부분 완료 · 9차 회차 후보).
- [ ] `_common/risk-register.md` 의 pre-existing BOM 처리 정책 결정(현재 유지 / 후속 별도 결정).

### 🔐 Security

문서만 수정, 실행 0건, broker 주문 0건, aws-live 작업 0건, secret 원문 기록 0건. AWS CLI · boto3 · psql · Spring Boot · Slack webhook · Step Functions · Lambda · ECS · KIS API 호출 0건. 새 `[REDACTED*]` placeholder 값 확장 없음(before/after count 동일).

## 2026-07-01 (6월 실제 AWS 비용 분석 · VPC Endpoint 절감 정합)

### 🧭 Summary

6월 aws-paper 실제 비용 확인과 VPC Endpoint 절감 조치가 정리됐다.

- 6월 세전 `135.40 USD` · 세금 `13.55 USD` · 세금 포함 약 `148.95 USD`.
- 월말 `180 USD` 추정은 7월 full automation 기준 보수적이지만 합리적.
- 핵심 cost driver 는 VPC Endpoint(전체 VPC 비용 `74.24 USD` 중 endpoint 부분 `70.69 USD`).
- SSM endpoint 제거 완료 · `ecr.api` · `ecr.dkr` · `logs` · `secretsmanager` endpoint 는 2 AZ → 1 AZ 축소 완료.
- 예상 월 절감액 약 `56.16 USD` · 추가 endpoint 삭제는 작동 리스크 대비 보류.

### ✅ Completed

1. 6월 실제 비용 확인
   1) 총 비용 정합:완료
       - 세전 `135.40 USD` · 세금 `13.55 USD` · 세금 포함 약 `148.95 USD`
   2) 월말 추정 정합:확인
       - 180 USD 는 7월 full automation 기준 보수적이지만 합리적 판정
   3) Cost driver Top 정합:확인
       - VPC 비용 `74.24 USD` 중 VPC Endpoint `70.69 USD`

2. VPC Endpoint 절감 조치
   1) SSM endpoint 제거:완료
       - EC2 · Fargate 접근 경로에서 SSM endpoint 의존 최소화 후 제거 진행
   2) 4종 endpoint 2 AZ → 1 AZ 축소:완료
       - `ecr.api` · `ecr.dkr` · `logs` · `secretsmanager`
   3) 예상 절감액 정합:확인
       - 약 `56.16 USD` / 월
   4) 추가 endpoint 삭제 보류:확인
       - 작동 리스크 대비 절감 폭이 낮아 후속 유지

### 🔵 Evidence

- 🔵 상세 evidence 링크

| 문서 | 위치 |
| --- | --- |
| cost-simulation | `_common/cost-simulation.md` 6월 실측 · 절감 정합 |

### ⚠️ Risks

- 🟠 VPC Endpoint 추가 삭제 보류 · 필요 시 작동 리스크 재평가.
- 🟠 7월 full automation 진입 후 실제 월 비용이 180 USD 추정과 얼마나 정합되는지 실측 회차 필요.

### 📌 Follow-ups

- [ ] 7월 실제 비용 확정 시 `cost-simulation.md` 실측 append.
- [ ] `ecr.api` · `ecr.dkr` · `logs` · `secretsmanager` 1 AZ 축소 후 SSM · ECS · Lambda 정상 동작 정기 관찰.
- [ ] 추가 endpoint 삭제 재평가 조건 정의(작동 리스크 vs 절감액).

### 🔐 Security

- 🟢 Kiro 는 본 작업에서 문서만 수정. 실행 0건 · broker 주문 0건 · aws-live 작업 0건 · secret 원문 기록 0건.
- 🟢 AWS CLI · psql · Spring Boot · Slack webhook · Step Functions · Lambda · ECS · SSM · KIS API 실행 0건.
- 🟢 실제 USD 총액 · endpoint 이름은 evidence 값으로만 기록 · 계좌번호 · payment method · billing account id 원문 기록 0건.

## 2026-07-01 (kiro-common-docs-readability spec 실행 · 6개 _common 문서 Dashboard-first 재구성)

### 🧭 Summary

`kiro-common-docs-readability` spec 을 실행해 `.kiro/specs/_common/` 하위 6개 단일 기준 문서에 운영자 매일 조회용 Dashboard/Summary 섹션을 상단에 추가했다.

- 기존 상세 내용은 삭제하지 않고 Dashboard 뒤에 그대로 유지.
- `operator-decisions.md` 는 이미 필요한 골격(`Status Legend` → `Decision Summary` → `At a Glance` → `Detailed Decisions` → `Change Log`)을 갖추고 있어 재구성 대신 그대로 보존.
- 실제 AWS 리소스 · MS 소스 · 하위 spec 폴더 변경 0건.

### ✅ Completed

- 🟢 `_common/risk-register.md`

| 항목 | 값 |
| --- | --- |
| 신규 추가 | `Risk Dashboard` 4개 그룹 |
| 그룹 앞부분 | Open High · Mitigated High |
| 그룹 뒷부분 | Newly added · 운영자 조치 필요 |
| 원본 유지 | `Risk Table` · `Risk Details` |

- 🟢 `_common/followups-overview.md`

| 항목 | 값 |
| --- | --- |
| 신규 추가 | `Current Follow-up Dashboard` 5개 그룹 |
| 그룹 앞부분 | Now · Next · Later |
| 그룹 뒷부분 | Blocked · Done recently |
| 원본 유지 | 진행 순서 · 날짜별 후속 메모 |

- 🟢 `_common/ms-aws-service-decision-matrix.md`

| 항목 | 값 |
| --- | --- |
| 신규 추가 | `Final Recommendation Summary` 5컬럼 표 |
| 표 크기 | 8개 MS 고정 순서 |
| 추가 요약 | 비권고 서비스 요약 |
| 원본 유지 | chapter 1 ~ 10 |

- 🟢 `_common/cost-simulation.md`

| 항목 | 값 |
| --- | --- |
| 신규 추가 | `Cost Dashboard` |
| 대시보드 앞부분 | paper · live realistic 월 예상 비용 |
| 대시보드 뒷부분 | cost driver Top 5 · 절감 결정 Top 5 |
| 원본 유지 | chapter 1 ~ 8 |

- 🟢 `_common/aws-resource-glossary.md`

| 항목 | 값 |
| --- | --- |
| 신규 추가 | 카테고리별 TOC 7종 |
| 카테고리 앞부분 | Network · Compute · Database |
| 카테고리 중간부 | Security/IAM/Secrets · Orchestration |
| 카테고리 뒷부분 | Observability · Storage/Artifact |
| 원본 유지 | 용어 항목 전체 |

- 🟢 `_common/operator-decisions.md`

| 항목 | 값 |
| --- | --- |
| 결과 | 기존 골격 정합 확인 · 파일 변경 없음 |
| 골격 앞부분 | Status Legend → Decision Summary → At a Glance |
| 골격 뒷부분 | Detailed Decisions → Change Log |

### 🔵 Evidence

- [`_common/risk-register.md`](specs/_common/risk-register.md) `Risk Dashboard` 섹션
- [`_common/followups-overview.md`](specs/_common/followups-overview.md) `Current Follow-up Dashboard` 섹션
- [`_common/ms-aws-service-decision-matrix.md`](specs/_common/ms-aws-service-decision-matrix.md) `Final Recommendation Summary` 섹션
- [`_common/cost-simulation.md`](specs/_common/cost-simulation.md) `Cost Dashboard` 섹션
- [`_common/aws-resource-glossary.md`](specs/_common/aws-resource-glossary.md) 카테고리별 TOC 섹션

### ⚠️ Risks

신규 리스크 0건. 기존 리스크 mitigation / detection / status 변경 0건.

### 📌 Follow-ups

- [ ] 상세 Detail-behind 이동(장문 mitigation · 표 셀 3문장 이상 · 4개 이상 슬래시 체인) 은 후속 phase 책임. 본 회차는 Dashboard-first 상단 배치만 완료.
- [ ] 05 spec port-view-ecs-and-runbook 후속 phase 에서 View 화면 연동 시 각 `_common` Dashboard 링크 노출 검토.
- [ ] Historical Notes / Change Log 축약 · Appendix(EKS · Lambda · Beanstalk / App Runner) 분리는 후속 회차 책임.
- [ ] operation-notes 상대경로 링크(`../<spec>/operation-notes.md`) 정합성 정기 점검 항목으로 유지.

### 🔐 Security

- 문서 수정: `.kiro/specs/_common/` 하위 5개 md 파일 · `.kiro/WORKLOG.md` 1개 파일 한정.
- AWS CLI · boto3 · psql · Spring Boot 실행 0건.
- broker · KIS · Selenium · KRX 크롤러 실행 0건.
- 자동 매수 · 자동 매도 · fill sync · position sync · intraday monitor 실행 0건.
- 8개 MS 저장소 소스 · README · AGENTS.md · CHANGELOG · docs / worklog 변경 0건(spec 영역).
- 하위 spec 폴더(`_common/` 외 spec) 변경 0건.
- 신규 secret · password · token · webhook URL · 계좌번호 · 실제 ARN · public IP · broker_order_no 원문 기록 0건.
- 자동 롤백 미수행 · 쓰기 계열 git 명령(`git add` · `git commit` · `git rm` · `git mv` · `git push`) 실행 0건.

## 2026-07-01 (paper Daily 자동화 1차 풀 ON + Step 12~17 자동 실행 ENABLED)

### 🧭 Summary

2026-07-01 기준 aws-paper Daily 자동화는 1차 풀 ON 상태가 되었다. Step 1~11 Scheduler 와 Step 12~17 Scheduler 가 모두 ENABLED 이며, 후보가 있는 경우 09:01 KST Step 12~17 경로가 View 수동 승인 없이 자동 진행된다.

> **안전 안내** — 본 변경은 aws-paper 에 한정된다. **aws-live 자동 BUY · SELL 정책 변경 없음**. live 는 후보 + 수동 승인 우선 정책을 유지한다.

### ✅ Completed

- 운영자 Step 12~17 수동 실행 1회 통과. status **`SUCCEEDED`** · Slack 3종 수신(07:50 장전 Slack · 08:24 승인 필요 Slack · Step 12~17 성공 Slack).

| 항목 | 값 |
| --- | --- |
| executionName | `port-manual-daily-step12-17-20260701-043747` |
| state machine | `portfolio-paper-daily-step12-17-approval` |
| start | `2026-07-01T13:37:47.856+09:00` |
| stop | `2026-07-01T13:40:41.212+09:00` |
| execution history | `ExecutionSucceeded` |

- DB pre-check · after-check 통과. Step 12~17 판정 **NO_TARGET 안전 종료**.

| 항목 | 값 |
| --- | --- |
| 2026-07-01 `strategy_execution_order` | 0건 |
| REQUESTED 전략 주문 | 0건 |
| active `connector_order_request` | 0건 |
| 오늘 `connector_order_request` | 0건 |
| 최신 `connector_balance_snapshot` | `id=321` |
| `as_of_date` | `2026-07-01` |
| `total_eval_amount` | `8,706,505` |
| `cash_balance` | `8,706,505` |
| `eval_profit` | `0` |

- Step 12~17 Scheduler `portfolio-paper-daily-step12-17-order-0901-kst` **DISABLED → ENABLED** 전환 완료.

| 항목 | 값 |
| --- | --- |
| LastModificationDate | `2026-07-01T13:53:57.160+09:00` |
| ScheduleExpression | `cron(1 9 ? * MON-FRI *)` |
| Timezone | Asia/Seoul |
| FlexibleTimeWindow | OFF |
| Target | `portfolio-paper-daily-scheduler-dispatcher` |
| Target Input | `{"scheduleType":"STEP12_17_ORDER","dryRun":false}` |

- 전체 Daily 스케줄 라인업 7종 모두 **🟢 ENABLED** 확인.

| 항목 | 값 |
| --- | --- |
| `portfolio-paper-ec2-start-0750-kst` | 07:50 KST EC2 start |
| `portfolio-daily-brief-morning-slack-0750-kst` | eventType `MORNING_BRIEF` |
| `portfolio-paper-daily-step1-11-approval-0800-kst` | dryRun false |
| **`portfolio-paper-daily-step12-17-order-0901-kst`** | **dryRun false — 본 일자 ENABLED** |
| `portfolio-paper-intraday-snapshot-evaluate-10min-kst` | cron `cron(10/10 9-15 ? * MON-FRI *)` |
| `portfolio-daily-brief-evening-slack-1550-kst` | eventType `EVENING_BRIEF` |
| `portfolio-paper-marketconnector-stop-1550-kst` | 15:50 KST MC EC2 stop |

- stale `connector_position_snapshot`(2026-06-23 4건 잔여)은 최신 balance 기준일 2026-07-01 과 분리된 stale snapshot 으로 확인. 판정은 최신 balance + 당일 주문 0건 기준 전액 현금 **NO_TARGET** 안전 종료.
- Kiro 는 본 작업에서 문서만 수정. 실행 · 주문 · 기록 이력은 아래 표 참조.

| 항목 | 결과 |
| --- | --- |
| AWS CLI 실행 | 0건 |
| boto3 실행 | 0건 |
| psql 실행 | 0건 |
| Spring Boot 실행 | 0건 |
| 외부 API 실행 | 0건 |
| broker 주문 제출 | 0건 |
| aws-live 작업 | 0건 |
| secret 원문 기록 | 0건 |

### 🔵 Evidence

- Step 12~17 수동 실행 SUCCEEDED — [04 operation-notes 2026-07-01 §1](specs/04-strategy-batch-stepfunctions/operation-notes.md).
- DB pre-check · after-check 통과 · NO_TARGET 안전 종료 판정 — [04 operation-notes 2026-07-01 §2](specs/04-strategy-batch-stepfunctions/operation-notes.md).
- Step 12~17 Scheduler DISABLED → ENABLED 전환 완료 — [04 operation-notes 2026-07-01 §3](specs/04-strategy-batch-stepfunctions/operation-notes.md).
- Daily 스케줄 라인업 7종 ENABLED 확인 — [04 operation-notes 2026-07-01 §4](specs/04-strategy-batch-stepfunctions/operation-notes.md).

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| 신규 리스크 ID | R-AUTO-037 |
| 상태 배지 | 🟢 `Mitigated` |
| Impact | High |
| Probability | Low |

- R-AUTO-037 신규 — Step 12~17 자동 실행이 Step 1~11 실패 또는 데이터 미준비 상태에서도 실행될 위험.
- R-AUTO-001 mitigation 보강 [2026-07-01 보강] — BUY · SELL · fill sync · position 변경 step 자동 재시도 금지 정책 유지.
- R-AUTO-001 mitigation 보강 [2026-07-01 보강] — Step 12~17 Scheduler ENABLED 후에도 Step Functions Retry 정책 검토 대상 유지.
- R-AUTO-025 mitigation 보강 [2026-07-01 자동 ENABLE 진입] — 09:01 schedule 보류 정책이 운영자 승인 후 ENABLE 진입 통과. Status `Mitigated` 그대로 유지.
- OD-MS-033(09:01 보류) 승격 조건은 결정 본문에 이미 포함되어 evidence 보강만 진행.
- 본 일자 신규 결정 없음. Decision Summary 카운트 변경 없음(전체 97 · 확정 52 · 잠정 42 유지).

### 📌 Follow-ups

- [ ] 다음 영업일 09:01 자동 실행 실전 관찰.
- [ ] 후보가 있는 날 자동 주문 제출 · 체결 · balance refresh · Slack 수신 확인.
- [ ] stale `connector_position_snapshot` 정리 또는 최신 balance 기준 판정 쿼리 보완.
- [ ] 2026-04-27 삼성전자 stale ACCEPTED `connector_order_request` 6건 cleanup.
- [ ] aws-live 자동화 정책 별도 cutover phase 재검토.

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| secret 원문 기록 | 0건 |
| AWS CLI 실행 | 0건 |
| psql 실행 | 0건 |
| broker 주문 제출 | 0건 |
| aws-live 정책 변경 | 0건 |

> <span style="color:#D1242F">aws-live BUY / SELL 자동화 정책 변경 없음.</span>

<details>
<summary>🔵 placeholder / 미기록 raw 카테고리 보기</summary>

- placeholder 정책 catalog(본 세션 사용 없음) — `[REDACTED]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_PUBLIC_IP]` · `[REDACTED_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_SECRET_ARN]` · `[REDACTED_BROKER_ORDER_NO]`.
- 미기록 raw 민감정보 카테고리 — secret · password · KIS app key · KIS app secret · token · Slack webhook URL · 계좌번호 원문 · RDS endpoint · account-id · 실제 IAM Role ARN · 실제 state machine ARN · 실제 Lambda ARN · broker_order_no 원문 · public IP · EIP · ENI ID · task ARN · image digest full sha256.
- 운영 식별자만 사실 기록 — state machine name · scheduler name · Lambda function name · execution name · status · timestamp · balance snapshot id · 금액 · count.

</details>

## 2026-06-30 (오후) (port-view ECS Fargate Public IP 1차 포팅 완료 + ECS View → AWS Step Functions Step 12~17 승인 실행 통과 + desiredCount 0 종료)

### 🧭 Summary

2026-06-30 오후 세션은 세 갈래 대규모 작업이 모두 통과했다.
(1) port-view ECS Fargate Public IP 1차 포팅이 완료되어 ECS View → AWS Step Functions Step 12~17 승인 실행 `SUCCEEDED` 및 desiredCount 0 종료까지 도달했고, (2) Approval Required + Daily Brief Slack Builder + notifier formatter 개선과 mini Step Functions
+ 장전/장후 Scheduler 2개 ENABLED 가 통과했으며, (3) MarketConnector EC2 evaluate 교체 배포로 `INTRADAY_STOP_LOSS` Slack 실이벤트 연동까지 확장되었다. · 세 갈래 모두 DB after-check 및 Slack 수신 확인을 마쳤고 aws-live 자동 정책은 변경 없이 유지된다.

### ✅ Completed

#### 1. port-view ECS Fargate Public IP 1차 포팅

| 항목 | 값 |
| --- | --- |
| 전체 상태 | 🟢 완료 |
| ECR | `portfolio-view` push 완료 |
| Task Definition | `portfolio-view:1` → `portfolio-view:2` |
| ECS Service | `portfolio-view-service` RUNNING |
| Spring profile | `aws-paper` |
| Web server | Tomcat 8080 |
| DB | RDS PostgreSQL |
| Connection pool | HikariPool |
| 기본 schema | `ops` |
| 화면 검증 | Dashboard · Balance · Positions · Orders · Reports · Daily 조회 통과 |

#### 2. ECS View → Step Functions Step 12~17 승인 실행

| 항목 | 값 |
| --- | --- |
| 실행 결과 | 🟢 `SUCCEEDED` |
| executionName | `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` |
| State Machine | `portfolio-paper-daily-step12-17-approval` |
| Slack | `DAILY_EXECUTION_SUCCESS` 수신 |
| 주문 결과 | `NO_TARGET` 안전 종료 |
| 신규 broker 주문 | 0건 |

#### 3. DB after-check

| 항목 | 값 |
| --- | --- |
| 검증 결과 | 🟢 통과 |
| REQUESTED `strategy_execution_order` | 0건 |
| retryable rejected | 0건 |
| active `connector_order_request` | 0건 |
| 최신 snapshot ID | `281` |
| `as_of_date` | `2026-06-30` |
| `total_eval_amount` | `8,706,505` |
| `cash_balance` | `8,706,505` |

#### 4. ECS 종료와 네트워크 운영

| 항목 | 값 |
| --- | --- |
| `desiredCount` | 0 전환 완료 |
| Fargate task | 종료 완료 |
| 다음 기동 | 새 public IP 발급 |
| SG inbound | 운영자 IP `/32` 유지 |
| 운영 경로 정합 | 5종 확인 |
| Local File 경로 | Step 1 · Step 1~11 · Step 12~17 |
| Local → AWS SFN | Step 12~17 오전 검증 |
| ECS → AWS SFN | Step 12~17 오후 검증 |

#### 5. Approval Required Slack Builder

| 항목 | 값 |
| --- | --- |
| 전체 상태 | 🟢 완료 |
| Builder Lambda | `portfolio-approval-slack-summary-builder` |
| State Machine | `portfolio-paper-daily-step1-17-approval` |
| revisionId | `da8642c6-8409-41b6-ad57-e066ff672332` |
| Notifier smoke | 성공 |
| eventType | `APPROVAL_REQUIRED` |
| color | `#ECB22E` |
| `marketStatusCode` | `BLOCK` |
| `marketStatusLabel` | `차단` |

#### 6. Daily Brief Slack Builder

| 항목 | 값 |
| --- | --- |
| 전체 상태 | 🟢 완료 |
| Builder Lambda | `portfolio-daily-brief-slack-summary-builder` |
| Runtime | Python 3.12 |
| DB library | `pg8000` |
| Secret 주입 | Secrets Manager `valueFrom` |
| smoke | 통과 |
| snapshot ID | `281` |
| 총평가금액 | `8,706,505` |
| 현금잔고 | `8,706,505` |
| 누적 수익률 | `-12.94%` |
| 누적 손익 | `-1,293,495` |
| 보유 종목 | 0건 |
| 장후 증감 | 0원 |

#### 7. `portfolio-event-notifier` formatter

| 항목 | 값 |
| --- | --- |
| 전체 상태 | 🟢 완료 |
| alias 1 | `MORNING_BRIEF` → `PRE_MARKET_STATUS` |
| alias 2 | `EVENING_BRIEF` → `POST_MARKET_STATUS` |
| 손익 prefix | 🔵 · 🔴 · ⚪ |
| 장전 Slack | `🌅 [장 전] 6/30 (화)` 수신 |
| 장후 Slack | `🌅 [장 후] 6/30 (화)` 수신 |
| 누적 수익률 표시 | `🔵 -12.94%` |
| 전일 대비 표시 | `⚪ 0원` |

#### 8. Daily Brief mini Step Functions와 IAM

| 항목 | 값 |
| --- | --- |
| 전체 상태 | 🟢 완료 |
| State Machine | `portfolio-daily-brief-slack-notification` |
| State Machine 상태 | ACTIVE |
| 실행 Role | `portfolio-daily-brief-sfn-role` |
| Scheduler Role | `portfolio-daily-brief-scheduler-role` |
| morning smoke | `daily-brief-morning-smoke-safe-20260630-193255-68f50aeb` |
| morning 결과 | `SUCCEEDED` |
| evening smoke | `daily-brief-evening-smoke-safe-20260630-193300-aa2b9a12` |
| evening 결과 | `SUCCEEDED` |

#### 9. EventBridge Scheduler

| 항목 | 값 |
| --- | --- |
| 전체 상태 | 🟢 2개 ENABLED |
| 장전 Scheduler | `portfolio-daily-brief-morning-slack-0750-kst` |
| 장전 cron | `cron(50 7 ? * MON-FRI *)` |
| 장전 timezone | `Asia/Seoul` |
| 장전 input | `MORNING_BRIEF` |
| 장후 Scheduler | `portfolio-daily-brief-evening-slack-1550-kst` |
| 장후 cron | `cron(50 15 ? * MON-FRI *)` |
| 장후 timezone | `Asia/Seoul` |
| 장후 input | `EVENING_BRIEF` |

#### 10. 장중 손절 Slack 실이벤트 연동

| 항목 | 값 |
| --- | --- |
| 전체 상태 | 🟢 완료 |
| 배포 대상 | MarketConnector evaluate |
| 버전 | `connector-intraday-position-evaluate-1.1.1-slack-notify` |
| CLI 옵션 | `--notify-slack` |
| CLI 옵션 | `--slack-function-name` |
| CLI 옵션 | `--slack-region` |
| 정적 검증 | `py_compile` 통과 |
| CLI 검증 | `--help` 통과 |

#### 11. MarketConnector EC2 IAM invoke 권한

| 항목 | 값 |
| --- | --- |
| 전체 상태 | 🟢 완료 |
| Role | `portfolio-paper-marketconnector-ec2-role` |
| inline policy | `portfolio-paper-marketconnector-event-notifier-invoke` |
| Resource | `portfolio-event-notifier` 한정 |
| invoke smoke | 성공 |

#### 12. 장중 runner 갱신

| 항목 | 값 |
| --- | --- |
| 전체 상태 | 🟢 완료 |
| 신규 옵션 | `--create-order` |
| 신규 옵션 | `--notify-slack` |
| backup | `run_intraday_snapshot_and_evaluate.sh.bak.20260630T112255Z.create-order-notify-slack` |
| 문법 검증 | 통과 |

#### 13. 실 runner 1회 안전 검증

| 항목 | 값 |
| --- | --- |
| 검증 결과 | 🟢 통과 |
| commandId | `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` |
| `open_position_count` | 0 |
| 상태 | `EMPTY_NORMAL` |
| marker | `INTRADAY_SNAPSHOT_AND_EVALUATE=SUCCESS` |
| 종료 코드 | 0 |
| 주문 조회 | 미실행 |
| 주문 생성 | 미실행 |
| Slack 전송 | 미실행 |
| 정상 종료 이유 | OPEN position 0건 |

#### 14. 장중 손절 DB after-check

| 항목 | 값 |
| --- | --- |
| 검증 결과 | 🟢 통과 |
| marker | `STEP19C_INTRADAY_STOP_FINAL_DB_AFTER_CHECK=SUCCESS` |
| `TODAY_INTRADAY_CHECKS` | 0 |
| `TODAY_INTRADAY_STOP_EXECUTION_ORDERS` | 0 |
| `ACTIVE_INTRADAY_STOP_EXECUTION_ORDERS` | 0 |
| `TODAY_INTRADAY_STOP_CONNECTOR_ORDERS` | 0 |
| `TODAY_INTRADAY_STOP_CONNECTOR_ORDER_ROWS` | 0 |
| 최신 snapshot ID | `281` |
| source version | `connector-intraday-snapshot-refresh-1.0.0` |
| psql 종료 코드 | 0 |

### 🔵 Evidence

| 항목 | 값 |
| --- | --- |
| 구분 | ECS Fargate 1차 포팅 |
| 결과 | 🟢 통과 |
| 상세 | [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md) |

| 항목 | 값 |
| --- | --- |
| 구분 | Step Functions Step 12~17 승인 |
| 결과 | 🟢 SUCCEEDED |
| 상세 | [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |

| 항목 | 값 |
| --- | --- |
| 구분 | DB after-check |
| 결과 | 🟢 통과 · balance id=281 |
| 상세 | [05 operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md) |

| 항목 | 값 |
| --- | --- |
| 구분 | Slack 3종 개선(Approval / Daily Brief / Notifier) |
| 결과 | 🟢 smoke SUCCEEDED |
| 상세 | [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |

| 항목 | 값 |
| --- | --- |
| 구분 | Daily Brief mini SFN + Scheduler 2개 |
| 결과 | 🟢 ENABLED |
| 상세 | [04 operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |

| 항목 | 값 |
| --- | --- |
| 구분 | 장중 손절 evaluate 교체 · runner 연동 |
| 결과 | 🟢 안전 검증 통과 |
| 상세 | [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md) |

| 항목 | 값 |
| --- | --- |
| 구분 | 장중 손절 DB after-check |
| 결과 | 🟢 통과 |
| 상세 | [03 operation-notes](specs/03-marketconnector-ec2/operation-notes.md) |

| 항목 | 값 |
| --- | --- |
| 구분 | MarketConnector EC2 IAM invoke 권한 |
| 결과 | 🟢 부여 · smoke 성공 |
| 상세 | [06 operation-notes](specs/06-secrets-and-iam/operation-notes.md) |

- View 운영 경로 5종 정합(2026-06-28 Run #46 · #47 → 2026-06-29 Run #48 → 2026-06-30 오전 `port-view-daily-step12-17-20260630-095111-aae2595c` → **오후 `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8`**).
- 🟠 stale `connector_order_request` 6건(2026-04-27 ACCEPTED 잔여) 식별 — 본 일자 실행과 무관, 후속 cleanup 후보.
- 🟠 실패 SQL 뒤 SUCCESS marker 출력 사례 1건 식별 — `.kiro/AGENTS.md` 규칙 3 보강 반영.

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| 리스크 ID | R-AUTO-033 |
| 상태 | 🟢 `Mitigated` |
| 메모 | Public IP direct + 운영자 IP/32 SG 1차 실증 [2026-06-30 오후 보강] |

| 항목 | 값 |
| --- | --- |
| 리스크 ID | R-AUTO-034 |
| 상태 | 🟠 `Open` |
| 메모 | Fargate Task Role `states:StartExecution` 한정 부여 1차 실증 (권한 분리 후속은 06 spec) |

| 항목 | 값 |
| --- | --- |
| 리스크 ID | R-AUTO-035 |
| 상태 | 🟢 `Mitigated` |
| 메모 | Daily Brief mini SFN 분리 + Scheduler 2개 ENABLED + smoke SUCCEEDED (신규) |

| 항목 | 값 |
| --- | --- |
| 리스크 ID | R-AUTO-036 |
| 상태 | 🟢 `Mitigated` |
| 메모 | 장중 손절 READY 생성 후 Slack 실패 시 rollback 없는 정책 (신규 · CloudWatch/SFN audit/DB after-check 로 감지) |

- 결정 변경 — OD-MS-002 · OD-MS-009 · OD-MS-030 · OD-MS-035 · OD-MS-036 · OD-MS-037 · OD-SAFE-001 ~ OD-SAFE-004 본문 무변경 · evidence 보강.
- 신규 결정 — OD-MS-038(Daily Brief Slack mini workflow, 🟢 확정 / 영향 spec 04 · 05 · 10).
- Decision Summary — 전체 96 → 97 · 확정 51 → 52 · 잠정 42 유지.

### 📌 Follow-ups

ECS / View 후속:

- [ ] Daily 화면 문구를 ECS / AWS mode 에 맞게 정리.
- [ ] stale `connector_order_request` 6건(2026-04-27) cleanup 결정.
- [ ] desiredCount 0/1 운영 명령 runbook 정식화.
- [ ] 운영자 IP 변경 시 SG inbound 갱신 절차.
- [ ] ALB · HTTPS · Route53 · Cloudflare Tunnel · 인증 · application-ecs.yml 분리 · ECS Auto Scaling · Blue/Green 은 05 · 06 · 07 · 10 spec 후속 phase.
- [ ] AWS CLI · psql · SUCCESS marker 운영 원칙 `.kiro/AGENTS.md` 보강(본 일자 반영).

Slack 후속:

- [ ] 다음 평일 07:50 / 15:50 자동 Daily Brief Slack 수신 확인.
- [ ] 다음 Step 1~11 실제 실행 시 풍부한 `APPROVAL_REQUIRED` Slack 자동 수신 확인.
- [ ] 보유종목 존재 시 종목별 손익·수익률 표시 재확인.
- [ ] `DAILY_EXECUTION_SUCCESS` 요약 강화 / `DAILY_EXECUTION_FAILED` cause truncation 정책.
- [ ] Slack webhook URL Secrets Manager 또는 SSM SecureString 이전 (R-AUTO-024 / 06 spec).

장중 손절 후속:

- [ ] 실 보유 종목 발생 시 `INTRADAY_STOP_LOSS` 실이벤트 수신 확인 (본 일자는 `open_position_count=0` 한정).
- [ ] 실 보유 종목 발생 시 `INTRADAY_STOP_SELL` READY 생성 + approval gate 차단 재확인.
- [ ] Slack 메시지에 계좌 · 현재가 · 진입가 · 예상손익금액 추가 여부 검토.
- [ ] 장중 손절 READY 생성 후 별도 approval summary Slack 추가 여부 검토.
- [ ] `portfolio-paper-intraday-stop-sell-approval` 자동 ENABLE 진입은 후속 유지.

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| secret 원문 기록 | 0건 |
| AWS CLI · boto3 · psql · Spring Boot · Lambda · SFN · Slack webhook · KIS 실행 | 0건 |
| broker 주문 제출 · fill · position sync 자동 재시도 | 0건 |
| Slack 실이벤트 발송 (smoke 외) | 0건 |
| commit · add · reset · checkout · stash | 0건 |
| aws-live 정책 변경 | 0건 |

> <span style="color:#D1242F">Kiro 는 본 일자 `.kiro` 루트 + `.kiro/specs` 문서 갱신만 수행. AWS 리소스 신규 생성 · 수정 · 삭제는 모두 운영자 직접 수행 영역.</span>

<details>
<summary>🔵 사용된 placeholder 및 운영 식별자 요약 보기</summary>

- placeholder — `[REDACTED]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_PUBLIC_IP]` · `[REDACTED_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_SECRET_ARN]` · `[REDACTED_BROKER_ORDER_NO]`.
- ECS Fargate — cluster `portfolio-paper-cluster` · service `portfolio-view-service` · task def `portfolio-view:2` · ECR `portfolio-view` · Logs `/ecs/portfolio-view` · SG `sgroup-port-view-ecs`.
- Step Functions — executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` · state machine `portfolio-paper-daily-step12-17-approval` · status `SUCCEEDED` · Slack `DAILY_EXECUTION_SUCCESS`.
- 장중 손절 — Lambda `portfolio-event-notifier` · state machine `portfolio-paper-intraday-stop-sell-approval` · SSM commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` · IAM Role `portfolio-paper-marketconnector-ec2-role` · inline policy `portfolio-paper-marketconnector-event-notifier-invoke`.
- Slack 개선 — Builder Lambda `portfolio-approval-slack-summary-builder` · `portfolio-daily-brief-slack-summary-builder` · mini SFN `portfolio-daily-brief-slack-notification` · Scheduler `portfolio-daily-brief-morning-slack-0750-kst` · `portfolio-daily-brief-evening-slack-1550-kst`.
- balance snapshot — `id=281` · `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505`.
- 상세 evidence — [04](specs/04-strategy-batch-stepfunctions/operation-notes.md) · [05](specs/05-port-view-ecs-and-runbook/operation-notes.md) · [06](specs/06-secrets-and-iam/operation-notes.md) · [03](specs/03-marketconnector-ec2/operation-notes.md).

</details>

## 2026-06-30 (Local View → AWS Step Functions Step 12~17 승인 실행 검증 통과 + Daily Batch gate 수정 + View 운영 경로 4종 정리)

### 🧭 Summary

2026-06-30 오전 세션은 Local View → AWS Step Functions Step 12~17 승인 실행이 최초로 통과했다.
오전 AWS Step Functions Step 1~11 정기 실행이 자동 trigger 경로로 통과했고, Daily Batch gate(`DailyBatchController.java`) 수정 후 Local View 에서 executionName `port-view-daily-step12-17-20260630-095111-aae2595c` 이 `SUCCEEDED` 로 종료되어 View 운영 경로 4종 정합이 확인되었다.
Step 12~17 주문성 구간은 별도 승인형 state machine + paper-order gate 정책을 그대로 유지한다.

### ✅ Completed

1. 오전 AWS Step Functions Step 1~11 정기 실행 통과:완료
   1) Trigger: EventBridge Scheduler · Dispatcher Lambda 자동 trigger 경로
   2) 정합: OD-MS-032

2. Local View → AWS Step Functions Step 12~17 승인 실행:완료

| 항목 | 값 |
| --- | --- |
| executionName | `port-view-daily-step12-17-20260630-095111-aae2595c` |
| state machine | `portfolio-paper-daily-step12-17-approval` |
| Status | 🟢 **SUCCEEDED** |
| Start | `2026-06-30T09:51:11.903+09:00` |
| Stop | `2026-06-30T09:54:16.484+09:00` |
| 종료 판정 | 주문 대상 없음 상태 안전 종료 |

3. Daily Batch gate(`DailyBatchController.java`) 수정 검증:완료

| 항목 | 값 |
| --- | --- |
| Gate 조합 | `fullPipelineExecutionEnabled=true` + `paperOrderEnabled=true` |
| 활성 조건 | AWS Step 12~17 승인 버튼 활성 · Local File / AWS Step Functions gate 분리 · approval range gate 통과 시 실행 |
| mvn compile | 성공 |
| Local View 재기동 | `aws-paper` profile 통과 |
| 화면 라벨 | `Execution ON` · `Local File OFF` · `Full Pipeline ON` · `Paper Order ON` |
| 허용 범위 | `1~17` |

4. DB 후검증:완료

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

5. View 운영 경로 4종 정합 확인:완료
   1) Local View → Local File Step 1 (2026-06-28 Run #46)
   2) Local View → Local File Step 1~11 (2026-06-28 Run #47)
   3) Local View → Local File Step 12~17 (2026-06-29 Run #48)
   4) **Local View → AWS Step Functions Step 12~17 (본 일자)**

### 🔵 Evidence

- 오전 AWS Step Functions Step 1~11 정기 실행 통과 — EventBridge Scheduler · Dispatcher Lambda 자동 trigger 경로 · OD-MS-032 정합. Step 12~17 주문성 구간은 별도 승인형 state machine + paper-order gate 정책 유지 (OD-MS-033 09:01 자동 ENABLE 보류).
- Local View → AWS Step Functions Step 12~17 승인 실행 통과 — Daily Batch gate 수정 후 운영자 의도 정합으로 승인 버튼 활성. `paperOrderEnabled=true` + approval range gate 통과 시에만 실행.

| 검증 | 결과 |
| --- | --- |
| mvn compile | 성공 |
| Local View `aws-paper` profile 재기동 | 통과 |
| 화면 표시 (Execution ON · Local File OFF · Full Pipeline ON · Paper Order ON · 허용 범위 `1~17` · 승인 버튼 활성) | 통과 |

- AWS Step Functions 실행 사실 · executionName `port-view-daily-step12-17-20260630-095111-aae2595c` · state machine `portfolio-paper-daily-step12-17-approval` · trigger = Local View AWS Step Functions approval range button
  status 🟢 **SUCCEEDED** · start `2026-06-30T09:51:11.903+09:00` · stop `2026-06-30T09:54:16.484+09:00` · 주문 대상 없음 안전 종료.
- DB 후검증 통과 — 위 Completed 4번 표 참조. 보유 종목 0건 검증은 `connector_position_snapshot.balance_snapshot_id` 컬럼 부재로 `account_no` + `as_of_date` 기준 사용.
- 4가지 운영 경로 정합 — Local File 실행과 AWS Step Functions 실행 분리 동작 정합 / Step 12~17 주문성 구간은 별도 승인형 state machine + paper-order gate 를 통해 실행 정합 / R-AUTO-033 · R-AUTO-034 mitigation 회귀 0건.

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| ID | OD-MS-002 · OD-MS-009 · OD-MS-037 · OD-SAFE-001 ~ OD-SAFE-004 |
| Status | 본문 무변경 |
| 메모 | 1차 실증 evidence 보강 (2026-06-29 (3) 보강 결합) |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-033 |
| Status | 🟢 **Mitigated** |
| 메모 | [2026-06-30 보강] Daily Batch gate 운영 의도 정합 + AWS Step Functions 승인 실행 1차 실증 |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-034 |
| Status | 🟠 **Open** |
| 메모 | [2026-06-30 보강] View 측 4가지 운영 경로 분리 1차 실증 (Fargate Task Role 권한 분리는 06 spec 후속 phase) |

| 항목 | 값 |
| --- | --- |
| ID | Decision Summary |
| Status | 무변경 |
| 메모 | 전체 96 · 확정 51 · 잠정 42 유지 |

| 항목 | 값 |
| --- | --- |
| ID | 본 일자 신규 결정 |
| Status | 없음 |
| 메모 | — |

### 📌 Follow-ups

- View / ECS 후속:
  - View Local → AWS Step Functions 경로 안정화 후 ECS / Fargate 포팅 설계 진입 (05 · 06 · 07 spec 후속 phase)
  - View ECS 배포 시 Local File 실행 제외 또는 별도 운영자 전용 경로 유지 결정
  - ECS Fargate 에서 AWS Step Functions trigger 중심 운영 방향 검토
  - Daily Batch 화면 문구를 local-file 중심에서 `aws-stepfunctions` mode 도 명확히 보이도록 정리 (05 spec 후속)
  - Dockerfile · ECR · ECS Task Definition · Fargate 조회-only smoke test · Fargate Step 1~11 + Step 12~17 검증 (05 · 06 · 07 spec 후속)
- DB 후속:
  - DB 검증 쿼리 작성 원칙 보강 — `information_schema.columns` 사전 확인 · 예상 컬럼 사용 금지 · 후검증 쿼리는 SELECT 결과 노출 · 본 일자 식별된 `connector_position_snapshot.balance_snapshot_id` 컬럼 부재 사례 정합
- 이전 후속 유지:
  - R-BROKER-005 · R-DATA-017 · R-SEC-010 · R-AUTO-024 · R-AUTO-027 · R-AUTO-028
  - 2026-04-27 삼성전자 stale ACCEPTED cleanup
  - OD-MS-033 09:01 보류

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| KIS · Connector · Slack · Daily Batch trigger 본 일자 신규 변경 | 0건 (검증은 운영자 직접 로컬 실행 한정) |
| `connector_order_request` 신규 | 0건 |
| `--execute` 본 일자 실행 | 1건 (🔵 **NO_TARGET**) |
| broker 주문 제출 | 0건 |
| aws-live 작업 | 0건 |
| secret 원문 기록 | 0건 |

> port-view MS 측 commit(`DailyBatchController.java` Daily Batch gate 수정) 변경분은 port-view MS 영역. cross-service AWS Migration spec 본 일자 작업으로 인한 변경 0건(spec 영역).

<details>
<summary>🔵 미기록 raw 카테고리 및 운영 식별자 요약 보기</summary>

- 미기록 raw 민감정보 · secret value · KIS app key · KIS app secret · 계좌번호 · 계좌 비밀번호 · token · RDS password · RDS endpoint hostname · account-id 12자리 원문 · 실제 IAM Role ARN · 실제 secret ARN · IAM access key id · instance-id · EIP
  image digest full sha256 · task ARN · job ARN · broker_order_no 원문 · KIS paper login credential · Slack webhook URL · DB password · Administrator password · 실제 state machine ARN. · 모두 `[REDACTED]` 또는 placeholder 처리.
- 사실 기록된 운영 식별자 · executionName `port-view-daily-step12-17-20260630-095111-aae2595c` · state machine 이름 `portfolio-paper-daily-step12-17-approval` · status `SUCCEEDED` · start · stop timestamp · balance snapshot `id=281`
  `as_of_date=2026-06-30` · `total_eval_amount=8,706,505` · `cash_balance=8,706,505` · `eval_profit=0` · `source_version=connector-intraday-snapshot-refresh-1.0.0` · 화면 상태 라벨 4종 · 허용 범위 `1~17` · Daily Batch gate 라벨
  - DB 컬럼명 `balance_snapshot_id` · `account_no` · `as_of_date`. 사용자 명시 정책 정합 — secret 아님.

</details>

## 2026-06-29 (3) (port-view Step 12~17 승인형 검증 완료 + Local View wrapper 정리 완료 + Approval state machine ARN 분리)

### ✅ Completed

- 🟢 **View Local Batch Step 12~17 approval state machine 승인형 검증 통과**

| 항목 | 값 |
| --- | --- |
| executionName | `port-view-step12-17-step12-17-20260629-194314-ba5edaf8` |
| state machine | `portfolio-paper-daily-step12-17-approval` |
| Status | 🟢 **SUCCEEDED** |
| Start | `2026-06-29T19:43:15.673+09:00` |
| Stop | `2026-06-29T19:46:06.546+09:00` |
| 진입 phase | Local View 가 SFN `StartExecution` external caller 로 붙는 두 번째 phase |
| 흐름 | `Step12_CheckApproval` → `Step12_RunMarketConnectorStrategyOrderExecute` → `Step12_GetCommandInvocation` → Step 13~17 → `ExecutionSucceeded` |
| DB 후검증 marker | `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS` |
| 오늘 신규 `connector_order_request` | 0건 |
| READY · REQUESTED `strategy_execution_order` | 0건 |
| 신규 broker 주문 | 0건 |
| mitigation 회귀 | R-AUTO-033 · R-AUTO-034 0건 |

- 🟢 **진입 전 preflight 4종 통과**

| 항목 | 결과 |
| --- | --- |
| REQUESTED / READY `strategy_execution_order` | 0건 |
| retryable rejected `connector_order_request` | 0건 |
| active `connector_order_request` | 0건 |
| strategy-linked active `connector_order_request` | 없음 |
| stale ACCEPTED 주문 (2026-04-27 삼성전자) | 별도 cleanup 대상으로 followups-overview 등록 |

- 🟢 **구현 변경**

| 항목 | 값 |
| --- | --- |
| `DailyBatchProperties` 필드 | `awsStepfunctionsApprovalStateMachineArn` + getter/setter |
| `application-aws-paper.properties` 키 | `portfolio.batch.aws-stepfunctions-approval-state-machine-arn` |
| 환경변수 | `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_APPROVAL_STATE_MACHINE_ARN` |
| `startSafeRange` | 일반 ARN (`portfolio-paper-daily-step1-17-approval`) |
| `startApprovalRange` | approval ARN (`portfolio-paper-daily-step12-17-approval`) |
| approval ARN 빈 값 | 승인형 실행 차단 |
| boolean payload | `allowPaperOrderExecute` · `paperOrderEnabled` |
| numeric payload | `fromStepOrder` · `toStepOrder` · `startStep` · `endStep` |
| `DailyBatchController` 신규 endpoint | `POST /daily-batch/aws-stepfunctions/start-approval-range` |
| `daily_batch.html` | AWS Step 12~17 승인 실행 버튼 분리 (safe / approval 조건 분리) |

- 🟢 **payload 타입 보완**

| 항목 | 값 |
| --- | --- |
| 최초 검증 결과 | approval state machine 진입 후 `Step12_CheckApproval` `BooleanEquals` 조건 불일치로 차단 |
| 원인 | boolean 필드 문자열 `"true"` 전달 + numeric 필드 문자열 전달 |
| 조치 | boolean / numeric 타입으로 보완 |
| 재검증 결과 | `Step12_CheckApproval` 통과 + 🟢 **ExecutionSucceeded** 확인 |

- 🟢 **로컬 View 구동 wrapper 정리 통과**

| 항목 | 값 |
| --- | --- |
| Local-file wrapper | `Start-PortfolioViewAwsPaperLocalFile.ps1` |
| AWS SFN wrapper | `Start-PortfolioViewAwsPaperStepFunctions.ps1` |
| 공통 Spring profile | `aws-paper` |
| 실행 범위 | Step 1~17 전체 (safe-only gate 아닌 운영자 선택형) |
| 검증 통과 항목 | `Unblock-File` + `ExecutionPolicy Bypass` + `VIEW_AWS_PAPER_STEPFUNCTIONS_ENV_READY` + DB `jdbc:postgresql://127.0.0.1:15433/portfolio` + View DB user `view_app` + Tomcat 8080 + `PortViewApplication started` |
| wrapper 본체 위치 | `C:\Workspaces\portfolio-local-env\` (spec 범위 밖) |

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| ID | OD-MS-002 · OD-MS-009 · OD-MS-037 · OD-SAFE-001 ~ OD-SAFE-004 |
| Status | 본문 변경 없음 |
| 메모 | 05 spec operation-notes 4)·5) Step 12~17 승인형 + wrapper 정리 완료 / 6)·7) Docker·ECR·Task Def·Fargate 검증 미완료 renumber |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-033 |
| Status | 🟢 **Mitigated** |
| 메모 | [2026-06-29 (3) 보강] Step 12~17 approval SFN 분리 + boolean/numeric payload 1차 실증 |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-034 |
| Status | 🟠 **Open** |
| 메모 | [2026-06-29 (3) 보강] approval ARN 분리 + 서비스 레벨 안전 gate 1차 실증 · Fargate Task Role 분리는 06 spec 후속 |

| 항목 | 값 |
| --- | --- |
| ID | Decision Summary |
| Status | 변경 없음 |
| 메모 | 전체 96 · 확정 51 · 잠정 42 유지 |

### 📌 Follow-ups

- [ ] Dockerfile 작성
- [ ] ECR repository · image push
- [ ] ECS Task Definition 등록
- [ ] ECS Service 조회-only smoke test
- [ ] Fargate View Step 1~11 `StartExecution` 검증
- [ ] Fargate View Step 12~17 approval 검증
- [ ] Fargate Task Role `states:StartExecution` 최소 권한 (일반 + approval 2종 모두 Resource 한정)
- [ ] 2026-04-27 삼성전자 stale ACCEPTED `connector_order_request` cleanup
- [ ] Fargate 초기 `paperOrderEnabled` 기본값 결정
- [ ] ALB source IP 제한 또는 최소 인증
- [ ] CloudWatch Logs · alarms · failure Slack 연동 점검
- (05 · 06 · 07 · 10 spec 후속 phase 책임)

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| KIS · Connector · Slack · Daily Batch trigger 본 일자 신규 | 0건 (검증은 운영자 직접 로컬 실행 한정) |
| `connector_order_request` 신규 | 0건 |
| `--execute` 본 일자 실행 | 1건 (🔵 **NO_TARGET**) |
| broker 주문 제출 | 0건 |
| aws-live 작업 | 0건 |
| secret 원문 (KIS · 계좌 · token · RDS password · IAM ARN · SFN ARN · Slack webhook · DB password · Administrator password 등) 기록 | 0건 |
| commit · add · reset · checkout · stash | 0건 |

> port-view MS 측 commit 변경분 (`DailyBatchProperties.java` · `StepFunctionsDailyBatchExecutionService.java` · `application-aws-paper.properties` · `DailyBatchController.java` · `daily_batch.html` + PowerShell wrapper 2종 + env loader 2종) 은 port-view MS 영역. cross-service AWS Migration spec 본 일자 변경 0건.

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- executionName `port-view-step12-17-step12-17-20260629-194314-ba5edaf8`.
- 운영 marker `AFTER_STEP12_17_APPROVAL_SFN_FINAL_CHECK=SUCCESS`.
- state machine 2종 — `portfolio-paper-daily-step1-17-approval` · `portfolio-paper-daily-step12-17-approval`.
- Spring properties key + 환경변수 라벨.
- payload 필드 라벨 + boolean / numeric 타입.
- PowerShell wrapper 파일명 (4종).
- DB URL `jdbc:postgresql://127.0.0.1:15433/portfolio` · View DB user `view_app` · Tomcat port `8080`.
- start · stop timestamp `2026-06-29T19:43:15.673+09:00` ~ `2026-06-29T19:46:06.546+09:00`.
- state 이름 4종 — `Step12_CheckApproval` · `Step12_RunMarketConnectorStrategyOrderExecute` · `Step12_GetCommandInvocation` · `ExecutionSucceeded`.
- `requestedBy=VIEW_APPROVAL_BUTTON` 라벨.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-29 (2) (port-view aws-stepfunctions Daily Batch trigger 구현 + 로컬 Step 1~11 StartExecution 검증 통과)

### ✅ Completed

- 🟢 **port-view aws-stepfunctions Daily Batch trigger 첫 구현 통과**

| 항목 | 값 |
| --- | --- |
| 신규 클래스 | `StepFunctionsDailyBatchExecutionService` |
| `DailyBatchProperties` gate | `canStartAwsStepfunctions` · `awsStepfunctionsStartEnabled` · `awsStepfunctionsStepStartEnabled` 등 |
| Controller endpoint | `POST /daily-batch/aws-stepfunctions/start-range` |
| 화면 버튼 | `/daily-batch` AWS Step 1~11 safe trigger |
| `application-aws-paper.properties` | env override placeholder 추가 |
| AWS SDK | v2 Step Functions client |
| commit | `e72de6f` (`feat(view): add Step Functions daily batch trigger`) |

- 🟢 **로컬 Step 1~11 `StartExecution` end-to-end 검증 통과**

| 항목 | 값 |
| --- | --- |
| 실행 조건 | `aws-paper` profile + `execution-mode=aws-stepfunctions` |
| 트리거 | `/daily-batch` AWS Step 1~11 safe trigger 버튼 |
| 결과 | `StartExecution` 성공 · `executionName` 반환 · account-id redaction 처리된 `executionArn` flash message |
| Step 1~11 workflow | 실행 |
| Step 12~17 주문성 구간 | `allowPaperOrderExecute=false` 차단 유지 |
| Slack | `APPROVAL_REQUIRED` 수신 |
| broker 주문 제출 | 0건 |
| 신규 `connector_order_request` | 0건 |
| aws-live 작업 | 0건 |

- 🟢 **runDate 누락 이슈 보완**

| 항목 | 값 |
| --- | --- |
| 최초 검증 실패 지점 | `StopCrawlerEc2AfterStep11Success` 상태에서 `States.Runtime` |
| 원인 | ASL Payload `runDate.$=$.runDate` 참조에 대해 View `StartExecution` input 에 `runDate` 없음 |
| 조치 | `StepFunctionsDailyBatchExecutionService` 가 Asia/Seoul 기준 `runDate` (yyyy-MM-dd) 를 input JSON 에 포함 |
| 재검증 결과 | `StopCrawlerEc2AfterStep11Success` 이후 `SendApprovalRequiredSlack` 통과 · Slack `APPROVAL_REQUIRED` 수신 |

- 🟢 **안전 정합** — 서비스 레벨에서 모두 차단

| 케이스 | 결과 |
| --- | --- |
| `canStartAwsStepfunctions=false` | 차단 |
| `hasRunningBatch=true` | 차단 |
| `stateMachineArn` 비어있음 | 차단 |
| `minExecutableStepOrder` · `maxExecutableStepOrder` 범위 밖 | 차단 |
| `allowPaperOrderExecute=false` 상태 Step 12 이상 | 차단 |
| approval 요청 `paperOrderEnabled=true` 조건 외 | 차단 |

  - 1차 실증 — R-AUTO-033 [2026-06-29 보강] · 신규 R-AUTO-034 mitigation.

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| ID | OD-MS-002 · OD-MS-009 · OD-MS-037 |
| Status | 본문 변경 없음 |
| 메모 | 1차 실증 메모 보강 (SFN backend 1차 완료 · Fargate 진입 전 local SFN trigger 선검증 완료) |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-033 |
| Status | 🟢 **Mitigated** |
| 메모 | [2026-06-29 보강] Step Functions trigger 분리 1차 실증 |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-034 |
| Status | 🟠 **Open** |
| 메모 | 신규 · Fargate View `states:StartExecution` 권한 과다 + Step 12 이상 주문성 gate 우회 위험 |

| 항목 | 값 |
| --- | --- |
| ID | Decision Summary |
| Status | 변경 없음 |
| 메모 | 전체 96 · 확정 51 · 잠정 42 유지 |

### 📌 Follow-ups

- [ ] (a) Dockerfile 작성
- [ ] (b) ECR repository · image push
- [ ] (c) ECS Task Definition 등록
- [ ] (d) ECS Service 조회-only smoke test
- [ ] (e) Fargate View 에서 Step 1~11 `StartExecution` 검증
- [ ] (f) Step 12~17 승인형 trigger / preflight / paper-order gate 후속 구현
- [ ] (g) Fargate Task Role `states:StartExecution` 최소 권한 (state machine ARN 한정)
- (05 · 06 · 10 spec 후속 phase 책임)

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS · EventBridge · Lambda · SFN · SSM · EC2 · RDS · S3 · KIS 실행 | 0건 |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| commit `e72de6f` 외 port-view MS 소스 변경 | 0건 |
| 8개 MS README · AGENTS · CHANGELOG · docs · worklog · 패키징 변경 | 0건 (spec 영역) |
| secret 원문 (KIS · 계좌 · token · RDS password · IAM ARN · Slack webhook · Administrator password 등) 기록 | 0건 |

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- port-view commit hash `e72de6f` · commit message `feat(view): add Step Functions daily batch trigger`.
- Controller endpoint `/daily-batch/aws-stepfunctions/start-range`.
- Spring properties key 라벨 · payload 필드 라벨.
- state name — `StopCrawlerEc2AfterStep11Success` · `SendApprovalRequiredSlack`.
- Slack 이벤트 라벨 `APPROVAL_REQUIRED` · 에러 라벨 `States.Runtime`.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-29 (View Local Batch Step 12~17 실행 통과 + View AWS Paper Batch 공용 launcher 정리 / DB password rotate 후속 등록)

### ✅ Completed

- 🟢 **Step 12 진입 전 안전 점검 통과**

| 항목 | 결과 |
| --- | --- |
| REQUESTED `strategy_execution_order` | 0건 |
| retryable rejected `connector_order_request` (`40580000` · `EGW00201`) | 0건 |
| active `connector_order_request` | 0건 |
| 실주문 제출 대상 | 없음 |
| `connector_strategy_order_execute.py` SHA256 | `379895709A7FD1AF6E95D41CF85009FF913A5D60D40788C20630F28730A5F5AE` (EC2 정식 배포본과 동일) |
| 필터 / 가드 정합 | `--intraday-stop-only` · `signal_type` · retry-normalizer (`40580000` + `EGW00201`) · `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` execute guard |
| 정합 | OD-MS-028 · OD-MS-033 · OD-MS-036 · OD-SAFE-004 |

- 🟢 **View Local Batch Step 12~17 전 구간 실행 통과**

| 항목 | 값 |
| --- | --- |
| Daily Pipeline run | `#48` |
| 결과 | 🟢 **SUCCESS** |
| 실행 유형 | `MANUAL_PARTIAL` |
| 요청자 | `VIEW_BUTTON` |
| 계좌 | `[REDACTED]` |
| 실행 범위 | Step 12~17 |
| total · success · no_target · failed · skipped | `6 · 5 · 1 · 0 · 0` |
| duration | `22,336ms` |
| View gate | `executionEnabled=true` · `localFileExecutionEnabled=true` · `fullPipelineExecutionEnabled=false` · `paperOrderEnabled=true` · `minExecutableStepOrder=12` · `maxExecutableStepOrder=17` |
| Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` | 🔵 **NO_TARGET** · `marketconnector_app` · 신규 주문 0건 |
| Step 13~16 (`CONNECTOR_ORDER_CHECK` · `SYNC_SELL_FILL` · `SYNC_BUY_FILL` · `SYNC_BUY_POSITION`) | 🟢 **SUCCESS** · 모두 대상 0건 |
| Step 17 `BALANCE_REFRESH` | KIS token 만료 → 재발급 통과 · 잔고 조회 Status `200` |
| `connector_balance_snapshot id` | `239` |
| `as_of_date` | `2026-06-29` |
| `cash_balance` · `total_eval_amount` | `8,706,505` |
| `eval_profit` | `0` |
| `source_version` | `connector-balance-1.0.0` |
| `connector_position_snapshot` | 비움 · 보유 종목 0건 |
| `NO_ORDER_SUBMITTED` 후검증 | 통과 |
| mitigation 회귀 | R-AUTO-019 · R-AUTO-031 · R-AUTO-032 · R-BROKER-004 · R-AUTO-033 모두 0건 |

- 🟢 **View AWS Paper Batch 공용 launcher 정리 완료**

| Launcher | 역할 |
| --- | --- |
| `C:/Workspaces/portfolio-local-env/Load-PortfolioViewAwsPaperBatchEnv.ps1` | env loader · `view_app` DB user · tunnel `127.0.0.1:15433` · Batch/local-file ON · full pipeline ON · paper order ON · `1~17` 범위 |
| `C:/Workspaces/portfolio-local-env/Start-PortfolioViewAwsPaperBatch.ps1` | starter · env loader 호출 → `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=aws-paper` |

| 항목 | 결과 |
| --- | --- |
| Tomcat | 8080 기동 |
| DB | `127.0.0.1:15433/portfolio` 연결 |
| Default schema | `ops` |
| 화면 허용 범위 | `1~17` |
| Execution · Local File · Full Pipeline · Paper Order | 모두 ON |
| 실행 버튼 | Step 1 단독 · 1~11 · 1~17 · 선택 범위 활성 |

- 🟢 **결론**

| 항목 | 값 |
| --- | --- |
| View Local Batch 2차 검증 | 완료 (Step 1 단독 · Step 1~11 · Step 12~17 모두 통과) |
| 흐름 검증 | Local View 버튼 → 로컬 source ProcessBuilder 실행 → AWS Paper DB 저장 · 조회 |
| Step별 DB role override | 검증 완료 (`view_app` JVM + step별 subprocess 분리) |
| 주문성 구간 Step 12 | 🔵 **NO_TARGET** 안전 종료 |
| ECS / Fargate 이전 로컬 실행 오케스트레이션 | 검증 완료 |
| OD-MS-037 | 2차 검증 부분 완료 메모 보강 · 본문 변경 없음 |

### 📌 Follow-ups (주의 사항 등록)

- [ ] (a) 공용 launcher 가 전체 Step 1~17 및 Step 10 / 11 / 12 paper 주문성 step 을 허용하므로 starter 실행 후 반드시 preflight 4종 (REQUESTED `strategy_execution_order` + retryable rejected + active `connector_order_request` + paper order gate) 사전 점검 진입 정책 명문화.
- [ ] (b) 🟠 **DB password 가 대화 중 노출된 이력** — app role password rotate 후속 필요.

| 항목 | 값 |
| --- | --- |
| 신규 리스크 | R-SEC-010 |
| R-DOCS-002 | [2026-06-29 보강] |
| Status | 🟠 **Open** |

- [ ] (c) rotate 이후 운영자 로컬 secret loader (`Load-PortfolioViewAwsPaperBatchEnv.ps1` 외 동등 도구) + env 재검증 진입 / 06 spec 후속 phase 책임 / 운영자가 rotate 완료 시점까지 starter 실행 전 신중 점검.

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| `connector_order_request` 신규 | 0건 |
| `--execute` 본 일자 실행 | 1건 (Step 12 · 🔵 **NO_TARGET**) |
| broker 주문 제출 | 0건 |
| fill · position sync 자동 재시도 | 0건 |
| aws-live 작업 | 0건 |
| broker · KIS 호출 | Step 17 balance refresh 1건 + token 재발급 한정 |
| Spring Boot · 8개 MS README · AGENTS · CHANGELOG · docs · worklog 변경 | 0건 (spec 영역) |
| secret 원문 (계좌 · KIS · token · RDS password · IAM ARN · Slack webhook · DB password · Administrator password) 기록 | 0건 |

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- Pipeline run id `#48` · 실행 유형 `MANUAL_PARTIAL` · 요청자 `VIEW_BUTTON` · 실행 범위 `Step 12~17` · `6/5/1/0/0` · duration `22,336ms`.
- `connector_strategy_order_execute.py` SHA256 `379895709A7FD1AF6E95D41CF85009FF913A5D60D40788C20630F28730A5F5AE`.
- View gate 6종.
- balance snapshot — `id=239` · `as_of_date=2026-06-29` · `cash_balance=8,706,505` · `total_eval_amount=8,706,505` · `eval_profit=0` · `source_version=connector-balance-1.0.0`.
- launcher 파일 경로 2종.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-28 (View Local Batch Step 1 + Step 1~11 실행 검증 통과 + 6/26 KRX 기준일 lag 결정적 증거 확보)

### ✅ Completed

- 🟢 **View Local Batch Step 1 단독 실행 통과**

| 항목 | 값 |
| --- | --- |
| Daily Pipeline run | `#46` |
| 결과 | 🟢 **SUCCESS** |
| 실행 유형 | `MANUAL_PARTIAL` |
| 요청자 | `VIEW_BUTTON` |
| 계좌 | `[REDACTED]` |
| 실행 범위 | Step 1~1 |
| Step 1 `CONNECTOR_BALANCE` | 🟢 **SUCCESS** |
| subprocess | `marketconnector_app` |
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
| Step 1~7 | 모두 🟢 **SUCCESS** |
| Step 8~11 | 🔵 **NO_TARGET** |
| broker 주문 제출 | 0건 |
| `--execute` | 0건 |

- 🟢 **Step별 DB role override 검증 통과**

| View / Step | DB Role |
| --- | --- |
| View JVM | `view_app` |
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
| Local Step 2 실행 중 최초 생성 raw | `interest_program_raw` · `interest_shortsell_raw` 의 `2026-06-25` · `2026-06-26` |
| 6/26 AWS Step Function | `step1-11-approval-20260626-080004-0904111b` 🟢 **SUCCEEDED** |
| Step2B Windows KRX GUI worker | `LastTaskResult=0` |
| 실제 worker 적재일 | 직전 영업일 `2026-06-24` |
| 결합 정합 | R-DATA-017 [2026-06-28 결정적 증거] · R-AUTO-020 [2026-06-26 보강] |
| 후속 우선순위 격상 | Step2B 성공 기준 강화 (`latest_trade_date` + row_count 자동 검증) · Slack KRX 기준일 표시 · feature lag 정책 명문화 |

### 📌 Follow-ups

- [ ] View Local Batch Step 12~17 실행 검증으로 진행 (OD-MS-037 후속 phase).
- [ ] Step 12 주문성 구간은 별도 preflight (REQUESTED `strategy_execution_order` + retryable rejected `connector_order_request` + active `connector_order_request` + Spring PAPER_ORDER_GATE feature flag) 후 진입.

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| KIS · Connector · Slack · Daily Batch trigger (Local View 의도된 manual) 외 | 0건 |
| `connector_order_request` 신규 | 0건 |
| `--execute` | 0건 |
| aws-live 작업 | 0건 |
| Spring Boot · 8개 MS README · AGENTS · CHANGELOG · docs · worklog 변경 | 0건 (spec 영역) |
| secret 원문 (계좌 · KIS · token · RDS password · IAM ARN · Slack webhook · DB password · Administrator password) 기록 | 0건 |

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- Pipeline run id `#46` · `#47`.
- 실행 유형 `MANUAL_PARTIAL` · 요청자 `VIEW_BUTTON`.
- DB role 6종.
- duration `1,362,039ms` · step count `11/7/4/0/0`.
- KRX raw 적재일 `2026-06-24` · 2026-06-28 신규 생성 raw date `2026-06-25` · `2026-06-26`.
- 6/26 Step Function execution name `step1-11-approval-20260626-080004-0904111b`.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-27 (View Local AWS Paper read-only 1차 scope 완료 / ECS Fargate 이전 로컬 검증 통과)

### ✅ Completed

- 🟢 **View Local AWS Paper read-only 1차 가동 통과**

| 항목 | 값 |
| --- | --- |
| 접속 경로 | Local Spring Boot → SSM Port Forwarding (`127.0.0.1:15433`) → AWS Paper RDS |
| 정합 | OD-ENV-006 · OD-ENV-007 · OD-NET-010 |
| Spring profile | aws-paper (신규) |
| DB user | `view_app` 유지 |
| Feature flag 4종 (`snapshot-refresh` · `order-refresh` · `strategy.execution.submit` · Slack · Connector 실행성 호출) | 모두 false 기본 |
| KIS · Connector refresh · Slack · Daily Batch 트리거 | 0건 |

- 🟢 **화면 검증 통과**

| 케이스 | 결과 |
| --- | --- |
| (a) 잔고 / 보유 종목 | `BalanceService` legacy `balance_summary` → 최신 `connector_balance_snapshot` · 보유 `connector_position_snapshot` 기준 · 보유 0건 empty card 정상 · `/balance-summary` · `/positions` OK |
| (b) 주문 / 주문 상세 | `connector_order_request` · `event` · `fill` 기반 · `/orders` 목록 + `/orders/56` 상세 OK · 주문 API 호출 0건 |
| (c) 전략 / 리포트 | `/strategy/execution/plans` plan #113 (신규 매수 차단) + `/strategy/reports/latest` (기간 `2023-01-27 ~ 2026-06-24` · 누적 수익률 `427.69%` · MDD `-8.94%` · Sharpe `2.48`) · POST submit 서버단 차단 |
| (d) Daily Batch · 대시보드 | run · step log 조회-only · 실행 버튼 disabled · 서버단 POST 우회 차단 · Step 12 이상 기본 차단 · `/dashboard` "AWS Paper · Snapshot 기준 · 조회-only" 문구 |

- 🟢 **결론 · 결정 / 리스크**

| 항목 | 값 |
| --- | --- |
| Local View AWS Paper DB 조회 read-only 콘솔 | 동작 확인 |
| ECS Fargate 이전 로컬 검증 1차 완료 (05 spec) | read-only scope 한정 |
| OD-MS-037 신규 | 🟢 **확정** · 영향 spec 04 · 05 · 10 |
| R-AUTO-033 신규 | 🟢 **Mitigated** (Spring feature flag 4종 + 서버단 POST 차단 + 단계적 ENABLE) |
| Decision Summary | 전체 95 → 96 · 확정 50 → 51 · 잠정 42 유지 |

### 📌 Follow-ups

- [ ] ECS / Fargate 즉시 진입 전 View Local Batch Step 1~17 실행 검증 (2차 로컬 검증) 선행.
- [ ] read-only 검증과 batch 실행 검증은 리스크 성격이 다르므로 분리 유지.
- [ ] ECS 배포 설계 진입은 2차 검증 통과 후 (05 spec port-view-ecs-and-runbook 후속 phase 책임).

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| KIS · Connector · Slack · Daily Batch · ECS RunTask · SubmitJob 호출 | 0건 |
| 신규 `connector_order_request` · `--execute` | 0건 |
| aws-live 작업 | 0건 |
| Spring Boot application properties · Java controller · service · Thymeleaf template 변경 | port-view MS 영역 (spec 변경 0건) |
| secret 원문 (계좌 · KIS · token · RDS password · IAM ARN · Slack webhook · DB password · Administrator password) 기록 | 0건 |

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- `view_app` DB user · `127.0.0.1:15433` local port.
- plan id `113` · order id `56`.
- 누적 수익률 `427.69%` · MDD `-8.94%` · Sharpe `2.48` · 백테스트 기간 `2023-01-27 ~ 2026-06-24`.
- placeholder — `[REDACTED]`.

</details>

## 2026-06-26 (AWS Paper Step 1~11 approval workflow 정기 실행 통과 + Step2B KRX 기준일 lag 1차 식별)

- 🟢 **08:00 EventBridge Scheduler 정기 실행 결과 추가**

| 항목 | 값 |
| --- | --- |
| Scheduler | `portfolio-paper-daily-step1-11-approval-0800-kst` 🟢 **ENABLED** |
| Dispatcher | Dispatcher Lambda |
| State machine | `portfolio-paper-daily-step1-17-approval` |
| Execution | `step1-11-approval-20260626-080004-0904111b` |
| Status | 🟢 **SUCCEEDED** |
| Step 12~17 | default false · approval gate 차단 유지 |
| Slack | `APPROVAL_REQUIRED` 수신 |
| Broker order | 0건 |
| New `connector_order_request` | 0건 |

  - 정합: R-AUTO-025 [2026-06-24 보강] mitigation · OD-MS-032 · OD-MS-033

- 🟢 **Step2B Windows KRX GUI worker success 처리**

| 항목 | 값 |
| --- | --- |
| Scheduled Task | `Portfolio-KRX-Worker-Daily` |
| `LastTaskResult` | `0` |
| 상태 | Running → Ready 복귀 |
| 호출 경로 | KRX login / program / shortsell 정상 진입 |
| mitigation 회귀 | R-AUTO-007 · R-AUTO-008 · R-AUTO-020 모두 0건 |

- 🟠 **6/26 KRX 기준일 lag 1차 식별** (2026-06-28 Local Step 1~11 검증 중 확인)

| 항목 | 값 |
| --- | --- |
| 6/26 08:05 KST worker 로그 기준 `interest_program_raw` · `interest_shortsell_raw` 수집일 | `2026-06-24` |
| Step Function SUCCESS | worker process exit 0 기준 정합 |
| 전략 데이터 최신성 관점 | `latest_trade_date` 검증 부족 |
| 리스크 | R-AUTO-020 [2026-06-26 보강] · R-DATA-017 신규 |

### 📌 Follow-ups

- [ ] (a) Step2B 성공 기준에 KRX program / shortsell 수집 후 `latest_trade_date` + row_count 검증 추가 (worker exit 0 만으로 SUCCESS 처리 금지 · `interest_krx_raw_validate_daily.py` 의 `ExpectedKrxRawDate` 산정 기준 재검토).
- [ ] (b) Slack `APPROVAL_REQUIRED` 메시지에 KRX 기준일 표시 (R-AUTO-027 mitigation 확장 결합 · 04 · 05 spec 후속).
- [ ] (c) preprocessor / decision / research 의 `interest_program` · `interest_shortsell` feature lag 허용 정책 재검토 (R-DATA-010 결합 · 08 · 09 spec 후속).

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS 리소스 신규 생성 · 수정 · 삭제 | 0건 |
| 애플리케이션 소스 수정 | 0건 |
| 8개 MS README · AGENTS · CHANGELOG · docs · worklog 변경 | 0건 (spec 영역) |
| AWS CLI · boto3 · SSM · Lambda · SFN · RDS · KIS API 실행 | 0건 |
| secret 원문 (계좌 · KIS · token · RDS password · IAM ARN · Slack webhook · Administrator password) 기록 | 0건 |

> SFN execution name · Scheduler 이름 · Lambda 이름 · State Machine 이름 · Slack 이벤트 라벨 · run_date 만 사용자 명시 정책 정합으로 사실 기록.

## 2026-06-25 (장중 포지션 Step Function 구현 완료 + blocked gate / no-target true-path 검증 통과 / 실주문 보류)

### ✅ Completed

- 🟢 **장중 포지션 확인 3단계 구조 1·2·3단계 일부 구현 진척** (운영자 직접 수행)

| 항목 | 값 |
| --- | --- |
| Kiro 작업 범위 | `.kiro` 루트 / `_common` spec 문서 갱신 |
| Kiro AWS CLI · boto3 실행 | 0건 |
| AWS 리소스 생성 · 수정 · 삭제 | 0건 |
| 신규 entrypoint (1단계) | `connector_intraday_snapshot_refresh.py` |
| 1단계 source_version | `connector-intraday-snapshot-refresh-1.0.0` |
| 1단계 sha256 | `99f7d1394fcd28dc5e070c072a9cdd244244df6afd9def09e62cd08b829b2269` |
| 신규 entrypoint (2단계) | `connector_intraday_position_evaluate.py` |
| 2단계 source_version | `connector-intraday-position-evaluate-1.1.0` |
| 2단계 sha256 | `B56C35753C47D6FC72D83DE892D7AC6534CDBF93F2628D9B047F5FBC30CD50A1` |
| Wrapper | `run_intraday_snapshot_and_evaluate.sh` |
| 배포 | MarketConnector EC2 venv (SSM commandId `b44d7c4e-c21c-48c0-a3c0-3a5572935577`) |
| `daily_intraday_position_monitor_run.py` 수정 | 0건 (OD-MS-035 정합) |

- 🟢 **`connector_strategy_order_execute.py` `signal_type=INTRADAY_STOP_SELL` 전용 필터 patch**

| 항목 | 값 |
| --- | --- |
| patch sha256 | `379895709A7FD1AF6E95D41CF85009FF913A5D60D40788C20630F28730A5F5AE` |
| 배포 | 운영자 직접 EC2 |
| 운영 marker | `DEPLOY_CONNECTOR_STRATEGY_ORDER_EXECUTE_INTRADAY_STOP_FILTER=SUCCESS` |
| KIS 강제 잔고 refresh | 1회 (SSM commandId `5b1e5757-ea93-4eb6-a7a0-6d5a38d87840`) |
| `rt_cd` · `output1_count` · `output2_count` | `0` · `0` · `1` |
| `as_of_date` · `as_of_ts` | `2026-06-25` · `2026-06-25T06:07:27.438088` |
| 상태 | `EMPTY_NORMAL` |
| patch 본문 평문 인용 | 0건 (R-DOCS-001 정합 · 03 spec operation-notes 후속 갱신 책임) |

- 🟢 **EventBridge Scheduler 신규 ENABLED**

| 항목 | 값 |
| --- | --- |
| Scheduler | `portfolio-paper-intraday-snapshot-evaluate-10min-kst` |
| cron | `cron(10/10 9-15 ? * MON-FRI *)` |
| Timezone | Asia/Seoul |
| 검증 | 평일 장중 10분 간격 자동 tick 통과 |
| 실행 정합 | 1단계 snapshot refresh + 2단계 evaluate |
| broker 주문 제출 | 0건 |
| Snapshot 갱신 | idempotent 동작 |
| 안전장치 4종 | stale snapshot · duplicate order · `sellable_qty` · `current_price` |

- 🟢 **3단계 별도 State Machine 신규 생성**

| 항목 | 값 |
| --- | --- |
| State Machine | `portfolio-paper-intraday-stop-sell-approval` |
| Type · Status | STANDARD · 🟢 **ACTIVE** |
| state 수 | 18 |
| 생성 시각 | `2026-06-25T14:58:45+09:00` |
| Daily Step 1~17 SFN | 완전 분리 |
| approval gate | `CheckIntradayStopApproval` → `BlockedByIntradayStopApprovalGate` |
| true-path 9개 state | `RunIntradayStopOrderExecute` · `GetIntradayStopOrderExecuteInvocation` · `RunConnectorOrderCheck` · `GetConnectorOrderCheckInvocation` · `RunSyncSellFill` · `RunConnectorBalanceRefresh` |
| true-path 9개 state (계속) | `GetConnectorBalanceRefreshInvocation` · `IntradayStopWorkflowSucceeded` · `IntradayStopWorkflowFailed` |
| 운영 marker | `INTRADAY_STOP_SELL_ASL_DRAFT_VALIDATE=SUCCESS` · `INTRADAY_STOP_SELL_IAM_INSPECT=SUCCESS` · `INTRADAY_STOP_SELL_STATE_MACHINE_CREATE=SUCCESS` |
| broker 호출 위임 | SFN → SSM RunCommand + ECS RunTask.sync → MarketConnector EC2 |
| Lambda broker 호출 직접 | 0건 (OD-MS-030 · OD-MS-032 · OD-MS-034 본문 변경 없음) |

- 🟢 **안전 테스트 (a) blocked gate**

| 항목 | 값 |
| --- | --- |
| execution name | `intraday-stop-blocked-gate-20260625-145928` |
| input | `allowIntradayStopOrderExecute=false` |
| Status | 🟢 **SUCCEEDED** |
| approval gate | 차단 정합 |
| true-path 진입 | 0건 |
| 운영 marker | `INTRADAY_STOP_SELL_BLOCKED_GATE_TEST=SUCCESS` |

- 🟢 **안전 테스트 (b) true-path no-target**

| 항목 | 값 |
| --- | --- |
| execution name | `intraday-stop-truepath-notarget-20260625-150146` |
| input | `allowIntradayStopOrderExecute=true` |
| Status | 🟢 **SUCCEEDED** |
| Start | `2026-06-25T15:01:46+09:00` |
| Stop | `15:02:53+09:00` |
| true-path state 진입 | 정합 |
| DB 비교 `INTRADAY_STOP_SELL` · `READY/FAILED SELL` | 0 rows |
| `max_connector_order_request_id` | `56` (변동 없음) |
| `created_after_truepath_count` | 0 |
| 신규 `connector_order_request` | 0건 |
| KIS broker 주문 제출 | 0건 |

- 🟠 **실제 1주 `INTRADAY_STOP_SELL` 주문 테스트 중단 · 보류**

| 항목 | 값 |
| --- | --- |
| 보유 종목 | 0건 |
| `sellable_qty` | 0 |
| 조치 | broker 호출 직전 사전 검증 단계에서 운영자 중단 |
| 없는 포지션 매도 주문 생성 | 0건 |
| 재개 조건 | 다음 보유 포지션 발생 (R-BROKER-005 신규 mitigation 1차 실증) |

- 🟢 **Daily Step 17 `RunConnectorBalanceRefresh` run-date 동적화 보강**

| 항목 | 값 |
| --- | --- |
| 이전 | `2026-06-22` 고정 |
| 변경 | KST 동적 `$(TZ=Asia/Seoul date +%F)` |
| Daily ASL 변경 반영 | 03 spec operation-notes 후속 갱신 책임 |

### ⚠️ Risks

| 항목 | 값 |
| --- | --- |
| ID | OD-MS-036 |
| Status | 🟢 **확정** |
| 메모 | 신규 (Intraday Stop Sell Submit Workflow · 별도 SFN + 수동 승인 + 전용 `signal_type` 필터 + Daily Step 12 분리 · 영향 spec 03/04/05/10) |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-030 |
| Status | 🟠 **Open** |
| 메모 | [2026-06-25 보강] State Machine 생성 + blocked gate + no-target 검증 통과 · 자동 ENABLE 진입 차단 정책 유지 |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-031 |
| Status | 🟢 **Mitigated** |
| 메모 | 신규 (approval gate 오설정 위험 · blocked gate 테스트 1차 실증) |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-032 |
| Status | 🟢 **Mitigated** |
| 메모 | 신규 (Daily SELL ↔ Intraday Stop Sell `signal_type` 필터 부재 위험 · 전용 필터 patch 배포 정합) |

| 항목 | 값 |
| --- | --- |
| ID | R-BROKER-005 |
| Status | 🟢 **Mitigated** |
| 메모 | 신규 (보유 종목 0건 상태 실주문 시도 위험 · 사전 검증 단계 중단 정합) |

| 항목 | 값 |
| --- | --- |
| ID | Decision Summary |
| Status | 갱신 |
| 메모 | 전체 94 → 95 · 확정 49 → 50 · 잠정 42 유지 |

### 🧾 Kiro 작업 산출물

- `.kiro/WORKLOG.md` 본 섹션
- `.kiro/CHANGELOG.md` 2026-06-25 섹션
- `.kiro/specs/_common/operator-decisions.md` — Change Log 2026-06-25 + OD-MS-036 신규 + OD-MS-035 [2026-06-25 보강] + Decision Summary
- `.kiro/specs/_common/risk-register.md` — R-AUTO-030 [2026-06-25 보강] + R-AUTO-031 · R-AUTO-032 · R-BROKER-005 신규
- `.kiro/specs/_common/followups-overview.md` — 2026-06-25 후속 메모
- `.kiro/specs/_common/ms-aws-service-decision-matrix.md` — 2026-06-25 네 번째 메모 (1순위 결정값 변경 없음)
- `.kiro/specs/_common/aws-resource-glossary.md` — 본 일자 추가 없음 (이미 존재 · 이미 있는 항목은 수정하지 않는 정책)

### 🔐 Security

| 항목 | 결과 |
| --- | --- |
| AWS · EventBridge · Lambda · SFN · SSM · EC2 · RDS · S3 · KIS API 호출 | 모두 운영자 직접 수행 |
| Kiro AWS CLI · boto3 실행 | 0건 |
| AWS 리소스 생성 · 수정 · 삭제 | 0건 |
| `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
| CloudWatch · SFN · KIS response · Lambda · IAM Policy · SFN ASL · patch 본문 · entrypoint 본문 · SSM stdout mojibake 원문 평문 인용 | 0건 |
| 8개 MS README · AGENTS · CHANGELOG · docs · worklog · 소스 · 패키징 변경 | 0건 (spec 영역) |
| broker · KIS 신규 BUY / SELL / 취소 / 정정 · `--execute` | 0건 |
| 신규 `connector_order_request` | 0건 |
| aws-live 작업 | 0건 |
| secret 원문 (KIS · 계좌 · token · RDS password · IAM ARN · Slack webhook · Administrator password 등) 기록 | 0건 |

> broker · KIS 호출 = KIS 강제 잔고 refresh 1회 (`EMPTY_NORMAL`) + Scheduler 자동 tick 의 snapshot refresh 한정. account-id 부분은 `[REDACTED]` 처리.

<details>
<summary>🔵 사실 기록된 운영 식별자 요약 보기</summary>

- SSM command_id — `b44d7c4e-c21c-48c0-a3c0-3a5572935577` · `5b1e5757-ea93-4eb6-a7a0-6d5a38d87840`.
- execution_name — `intraday-stop-blocked-gate-20260625-145928` · `intraday-stop-truepath-notarget-20260625-150146`.
- cron 표현식 `cron(10/10 9-15 ? * MON-FRI *)` · 운영 marker 5종.
- source_version 2종 · sha256 3종.
- `as_of_date=2026-06-25` · `max_connector_order_request_id=56`.
- placeholder — `[REDACTED]`.
- 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님.

</details>

## 2026-06-24 (08:00 Scheduler 실 실행 검증 + Step 12 retry-normalizer EGW00201 확장 + Step 12~17 수동 실행 4건 FILLED)

### ✅ Completed

- 🟢 **08:00 EventBridge Scheduler 실 실행 검증 완료**

| 항목 | 값 |
| --- | --- |
| Scheduler | `portfolio-paper-daily-step1-11-approval-0800-kst` (State 🟢 **ENABLED**) |
| 실행 경로 | Scheduler → Dispatcher Lambda → SFN `portfolio-paper-daily-step1-17-approval` |
| Step 1~11 | approval-required 흐름 완료 |
| Slack | `APPROVAL_REQUIRED` 수신 |
| Step 12 이후 주문 제출 경로 | 차단 |
| 신규 주문 제출 (08:00 기준) | 0건 |
| 09:01 Scheduler | `portfolio-paper-daily-step12-17-order-0901-kst` 🟠 **DISABLED** 유지 |
| 1차 실증 | R-AUTO-025 [2026-06-24 보강] · OD-MS-032 · OD-MS-033 |

- 🟢 **Step 12 주문 재시도 오류 수정 (EGW00201 확장)**

| 항목 | 값 |
| --- | --- |
| 6/23 잔존 4건 상태 | `REJECTED` |
| `rejection_code` 분포 | `40580000` 2건 + `EGW00201` 2건 |
| 기존 retry-normalizer 처리 대상 | `40580000` 단독 |
| `EGW00201` 판단 | KIS gateway rate limit / 초당 거래건수 초과 (broker 주문 내용 오류 아님) |
| 조치 파일 | `port-marketconnector/connector_strategy_order_execute.py` 전체 교체 |
| 보강 사항 | retry-normalizer `EGW00201` 추가 + 주문 사이 기본 sleep + backoff 재시도 + `submit_attempts` `result_payload` 기록 |
| 배포 | S3 업로드 + MarketConnector EC2 정식 배포 |
| 검증 | `py_compile` + 운영 마커 통과 |
| OD-MS-033 신규 | 🟢 **확정** |
| R-AUTO-026 | mitigation 1차 실증 |
| 본문 평문 인용 | 0건 (R-DOCS-001 정합 · 03 spec operation-notes 후속) |

- 🟢 **Step 12 dry-run + 수동 실행 검증**

| 항목 | 값 |
| --- | --- |
| retry-normalizer `candidate_count` | 4 (`40580000` 2건 + `EGW00201` 2건 모두 후보 인식) |
| dry-run DB 변경 | 0건 |
| 수동 invoke | Dispatcher Lambda `STEP12_17_ORDER` |
| Slack | `DAILY_EXECUTION_SUCCESS` 수신 |
| 4건 결과 | 새 `connector_order_request` 생성 + `broker_order_no` 생성 + 최종 체결 |

| 항목 | 값 |
| --- | --- |
| 종목 코드 | `042660` |
| 종목명 | 한화오션 |
| 결과 | 🟢 **FILLED** |

| 항목 | 값 |
| --- | --- |
| 종목 코드 | `004990` |
| 종목명 | 롯데지주 |
| 결과 | 🟢 **FILLED** (`PARTIAL_FILLED` → 단건 재조회 · `tot_ccld_qty=69`) |

| 항목 | 값 |
| --- | --- |
| 종목 코드 | `003490` |
| 종목명 | 대한항공 |
| 결과 | 🟢 **FILLED** |

| 항목 | 값 |
| --- | --- |
| 종목 코드 | `023530` |
| 종목명 | 롯데쇼핑 |
| 결과 | 🟢 **FILLED** |

- 🟢 **결정 / 리스크 변경**

| 항목 | 값 |
| --- | --- |
| ID | OD-MS-033 |
| Status | 🟢 **확정** |
| 메모 | 신규 (Step 12 retry-normalizer `40580000` + `EGW00201` + rate-limit backoff + 09:01 자동 ENABLE 별도 승인 보류 · 영향 spec 03/04/05/10) |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-025 |
| Status | 🟢 **Mitigated** |
| 메모 | [2026-06-24 보강] 08:00 첫 실 실행 통과 |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-026 |
| Status | 🟢 **Mitigated** |
| 메모 | 신규 (`EGW00201` rate limit 재시도 누락 위험) |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-027 |
| Status | 🔵 **Accepted** |
| 메모 | 신규 (`APPROVAL_REQUIRED` Slack summary 0/0 표시 운영자 오해 위험) |

| 항목 | 값 |
| --- | --- |
| ID | Decision Summary |
| Status | 갱신 |
| 메모 | 전체 91 → 92 · 확정 46 → 47 · 잠정 42 유지 |

- 🟢 **EC2 lifecycle 자동 실행 구현 완료** (같은 일자 후속)

| 항목 | 값 |
| --- | --- |
| Lambda | `portfolio-paper-ec2-lifecycle-dispatcher` |
| Runtime · Handler | Python 3.12 · `lambda_function.lambda_handler` |
| Timeout · Memory | 30s · 256MB |
| State | 🟢 **Active** |
| IAM Role | 신규 · EC2 start·stop 권한 부여 |
| 환경변수 | `TIMEZONE=Asia/Seoul` · `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true` |
| 휴일 체크 정책 | start 만 적용 · stop 은 미적용 |
| dryRun 3종 (`start BOTH` · `stop CRAWLER` · `stop MARKETCONNECTOR`) | 통과 |
| 주말 skip (`runDate=2026-06-27`) | 통과 |

- 🟢 **EventBridge Scheduler 2개 ENABLED 추가**

| 항목 | 값 |
| --- | --- |
| Scheduler | `portfolio-paper-ec2-start-0750-kst` 🟢 **ENABLED** |
| cron | `cron(50 7 ? * MON-FRI *)` · Asia/Seoul |
| Target input | `{"action":"start","target":"BOTH","holidayCheck":true,"reason":"PRE_DAILY_STEP1_11","dryRun":false}` |

| 항목 | 값 |
| --- | --- |
| Scheduler | `portfolio-paper-marketconnector-stop-1550-kst` 🟢 **ENABLED** |
| cron | `cron(50 15 ? * MON-FRI *)` · Asia/Seoul |
| Target input | `{"action":"stop","target":"MARKETCONNECTOR","holidayCheck":false,"reason":"POST_MARKET_CLOSE","dryRun":false}` |

  - 두 Scheduler `get-schedule` 응답 정합 확인. Scheduler invoke Role · Lambda invoke 권한 (Resource · Action wildcard 0건).

- 🟢 **Step Functions 성공 경로 변경**

| 항목 | 값 |
| --- | --- |
| State machine | `portfolio-paper-daily-step1-17-approval` |
| 흐름 | `Step6ToStep11_Succeeded` → `StopCrawlerEc2AfterStep11Success` (EC2 lifecycle · Crawler stop) → `SendApprovalRequiredSlack` → `Step12_CheckApproval` |
| Step 1~11 실패 시 | `SendDailyExecutionFailedSlack` (`portfolio-event-notifier` · Crawler EC2 유지) |
| 상태 | 🟢 **ACTIVE** |
| RevisionId | `edd92cc9-1d94-4752-9a43-b7eb5b2f3c2c` |
| SFN execution role | EC2 lifecycle Lambda invoke 권한 추가 |
| OD-MS-034 신규 | 🟢 **확정** · 영향 spec 03/04/05/08/10 |

- 🟢 **Lambda 역할 3분리**

| Launcher | 역할 |
| --- | --- |
| `portfolio-paper-daily-scheduler-dispatcher` | 08:00 / 09:01 SFN schedule dispatcher |
| `portfolio-paper-ec2-lifecycle-dispatcher` | 07:50 / 15:50 EC2 start·stop dispatcher · Step 1~11 성공 시 Crawler stop |
| `portfolio-event-notifier` | Slack 알림 전담 |

| 항목 | 값 |
| --- | --- |
| 리스크 | R-AUTO-028 |
| Status | 🟢 **Mitigated** |
| 메모 | 신규 (EC2 lifecycle Scheduler · Lambda 설정 오류 위험) |

| 항목 | 값 |
| --- | --- |
| 리스크 | R-AUTO-025 |
| Status | 🟢 **Mitigated** |
| 메모 | [2026-06-24 두 번째 보강] EC2 lifecycle 자동화 사슬까지 ENABLED |

| 항목 | 값 |
| --- | --- |
| 리스크 | Decision Summary |
| Status | 갱신 |
| 메모 | 전체 92 → 93 · 확정 47 → 48 · 잠정 42 유지 |

| 항목 | 값 |
| --- | --- |
| 리스크 | 실 영업일 검증 |
| Status | 다음 영업일 첫 검증 예정 (07:50 start · Crawler stop · 15:50 stop) |
| 메모 |  |

- 🟢 **장중 포지션 확인 최종안 확정 / 내일 구현 예정**

| 항목 | 값 |
| --- | --- |
| 실제 AWS · 애플리케이션 소스 실행 | 0건 |
| 1단계 (MarketConnector 10분 Snapshot Refresh) | Scheduler → Lambda → SSM → MC EC2 · balance/position snapshot 갱신 · 판단·주문 0건 · idempotent |
| 2단계 (StrategyExecution Intraday Evaluate) | 1단계 직후 SSM · `strategy_intraday_position_check` 저장 + READY order 생성까지 · broker 주문 0건 · 안전장치 4종 · 신규 파일 구현 · SFN Retry 자동 주문 연결 금지 |
| 3단계 (Intraday Stop Sell Submit & Refresh) | 별도 SFN · `source_type=INTRADAY_STOP_SELL` 전용 필터 · 초기 수동 승인 후 실행 · Daily BUY/SELL 분리 |
| OD-MS-035 신규 | 🟢 **확정** · 영향 spec 03/04/05/10 |

| ID | Status |
| --- | --- |
| R-DATA-014 (stale snapshot 기반 잘못된 손절) | 🟢 **Mitigated** |
| R-AUTO-029 (duplicate `INTRADAY_STOP_SELL` READY) | 🟢 **Mitigated** |
| R-DATA-015 (`sellable_qty` 부족 시 손절 주문) | 🟢 **Mitigated** |
| R-DATA-016 (`current_price` 누락 · 비정상) | 🟢 **Mitigated** |
| R-AUTO-030 (3단계 조기 ENABLE 위험) | 🟠 **Open** |
| Decision Summary | 전체 93 → 94 · 확정 48 → 49 · 잠정 42 유지 |

### 📌 Follow-ups

- [ ] (a) `APPROVAL_REQUIRED` Slack summary 0/0 표시 개선 (R-AUTO-027 mitigation 확장 · 04 · 05 spec 후속).
- [ ] (b) Dispatcher Lambda application log 보강 (04 spec 후속).
- [ ] (c) 09:01 Scheduler 자동 ENABLE 여부 보류 (OD-MS-033 정합 · Slack summary 개선 + Lambda 로그 보강 + 운영 회차 누적 후 최종 판단).
- [ ] (d) Slack webhook URL Secrets Manager / SSM Parameter Store 이전 (R-AUTO-024 정합 · 06 spec 후속).
- [ ] (e) balance refresh `EGW00215` rate-limit backoff · retry policy (2026-06-23 후속 §1 유지).
- [ ] 내일 구현 예정 — 1단계 Scheduler·Lambda·SSM · 2단계 신규 evaluate 파일 + 안전장치 4종 · 3단계 별도 SFN + Slack (자동 ENABLE 보류).
- [ ] 03 · 04 · 05 · 06 · 10 spec 하위 문서 갱신은 후속 phase 책임.
- Kiro 는 본 일자 루트 · `_common` 문서 갱신만 수행 · 실제 AWS CLI · boto3 실행 0건 · AWS 리소스 생성 · 수정 · 삭제 0건. · Lambda 코드 본문 · IAM Policy 전체 본문 · Step Functions ASL 전체 본문 · Lambda 응답 전문 · CloudWatch Logs 전문 · KIS API 응답 전문 · 실제 ARN
  instance-id 평문 인용 0건(R-DOCS-001 정합). · 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 본 작업으로 인한 변경 0건(spec 영역). · `daily_intraday_position_monitor_run.py` 수정 0건.
- 실제 AWS / EventBridge Scheduler / Lambda / Step Functions / SSM / EC2 / RDS / S3 / Slack webhook / KIS API 작업은 모두 운영자 직접 수행 — Kiro 는 본 일자 루트 / `_common` 문서 갱신만 수행. AWS CLI / boto3 실행 0건 / AWS 리소스 생성 수정
  삭제 0건 / `secretsmanager:GetSecretValue` 결과값 평문 기록 0건 / CloudWatch Logs Step Functions execution history KIS API response body Lambda 응답 본문 `connector_strategy_order_execute.py` patch 본문 평문 인용 0건.
  8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 본 일자 작업으로 인한 변경 0건(spec 영역) — 운영자 직접 patch 한 `connector_strategy_order_execute.py` 전체 교체 변경분은 03 spec operation-notes 후속 갱신 책임. broker / KIS 호출 = Step 12 paper 4건 재제출(`042660` `004990` `003490`
  `023530` BUY MARKET) + Step 13 체결조회 한정 / 추가 BUY SELL 취소 정정 0건 / aws-live 작업 0건.
- 민감정보(secret value · KIS app key · KIS app secret · 계좌번호 · 계좌 비밀번호 · token · RDS password · RDS endpoint hostname · account-id · 실제 IAM Role ARN · 실제 secret ARN · IAM access key id · instance-id · EIP · image digest full sha256 · task ARN
  job ARN · broker_order_no 원문 · broker_branch_code 원문 · KIS paper login credential · Slack webhook URL · Step Functions ARN · Lambda ARN) 신규 기록 없음. · 모두 `[REDACTED]` 또는 placeholder. · 운영 식별자(rejection_code `40580000`
  - `EGW00201` · dry-run `candidate_count=4` · 종목 코드 `042660` · `004990` · `003490` · `023530` · 종목명 · `004990` PARTIAL_FILLED → FILLED 전환 · `tot_ccld_qty=69` · Scheduler 이름 2종 · Dispatcher Lambda 이름 · Step Functions state machine 이름 2종
    scheduleType 라벨 `STEP12_17_ORDER` · Slack 이벤트 라벨 `APPROVAL_REQUIRED` · `DAILY_EXECUTION_SUCCESS` · run_date `2026-06-24`) 만 사용자 명시 정책 정합으로 사실 기록 · secret 가 아님.

## 2026-06-23 (Step Functions approval 실전 검증 + Step 12 retry-normalizer + DB 한글 정상 확인)

- 🟢 **Step Functions state machine `portfolio-paper-daily-step1-17-approval` 실전 검증 완료**

| 항목 | 값 |
| --- | --- |
| Step 1~17 `allowPaperOrderExecute=false` blocked | 통과 |
| Step 12~17 `allowPaperOrderExecute=true` true path | 통과 (false path 사전 검증 후 REQUESTED 상태 확인 → true path 승인 실행) |
| 첫 실 SELL | BGF리테일 `282330` 17주 MARKET |
| `connector_order_request id 48` · `strategy_execution_order id 40` | 🟢 **FILLED** |
| `broker_order_no` | `0000006143` |
| Step 17 balance refresh 후 보유 | 4종목 정상 반영 |
| 결정 · 리스크 | OD-MS-029 신규 · R-AUTO-021 [2026-06-23 보강] |

- 🟢 **Step 12 retry-normalizer 운영 보완**

| 항목 | 값 |
| --- | --- |
| 배경 위험 (R-AUTO-022 신규) | 장종료 REJECTED / `40580000` 후 execution_order 가 rejected `connector_order_request_id` 물고 있으면 다음날 Step 12 자동 재제출 경로 (`REQUESTED` + `connector_order_request_id IS NULL` 기본 필터) 끊김 |
| 대상 파일 | `port-marketconnector/connector_strategy_order_execute.py` 전체 교체 |
| 반영 위치 | Step 12 시작부 · 별도 Step 11.5 아님 |
| 복구 조건 | `execution_mode = PAPER_STRATEGY` + `action_type IN (BUY, SELL)` + `execution_status IN (READY, FAILED)` + `connector_order_request_id IS NOT NULL` |
| 복구 조건 (계속) | linked `connector_order_request.request_status = REJECTED` + `rejection_code = 40580000` + `broker_order_no IS NULL` + `connector_fill` 없음 |
| 복구 처리 | `execution_status = REQUESTED` + `connector_order_request_id = NULL` + `result_payload.retry_normalizer` 에 old request 이력 저장 |
| EC2 정식 배포 | 완료 |
| `.venv/bin/python` dry-run | 통과 · retry 후보 0건 · REQUESTED 주문 0건 |
| SFN Step 12 | `.venv/bin/python` 사용 정합 (OD-MS-028 신규) |

- 🟢 **DB 한글 표시 문제 정정**

| 항목 | 값 |
| --- | --- |
| PGAdmin4 확인 대상 | `connector_order_request` · `strategy_execution_order` · `connector_api_call_log` · `connector_position_snapshot` |
| 저장 / 조회 | 모두 정상 |
| `server_encoding` · `client_encoding` | 모두 `UTF8` |
| 결론 | DB 한글 깨짐 문제 아님 · PowerShell / SSM / AWS CLI 콘솔 표시 경로 인코딩 이슈 · DB 리스크 신규 등록 없음 · followups-overview 2026-06-23 후속 메모로 분리 |

- 🟢 **Step 17 balance refresh 후속**

| 항목 | 값 |
| --- | --- |
| EGW00123 token 만료 후 재발급 | 통과 확인 |
| EGW00215 rate limit | 발생 확인 |
| balance refresh rate-limit backoff · retry policy | 후속 과제 유지 (followups-overview 2026-06-23 §1 정합 · 신규 R 부여 보류) |

- 🟢 **AWS 공통 Slack notifier 구현 완료**

| 항목 | 값 |
| --- | --- |
| Lambda | `portfolio-event-notifier` (Runtime Python 3.12 · IAM Role `portfolio-event-notifier-lambda-role`) |
| 역할 | SFN · EventBridge · EC2 SSM · Batch · Lambda 어디서든 호출 가능한 운영 이벤트 알림 보조 계층 |
| `hello wook` 수동 invoke | 성공 · Portfolio Daily Bot 수신 |
| 완료 marker | `SLACK_LAMBDA_SMOKE_TEST=SUCCESS` · `PORTFOLIO_EVENT_NOTIFIER_TEMPLATE_TEST=SUCCESS` |
| 6종 메시지 템플릿 | 장 전 잔고 · Daily 검증 완료 · Daily 실행 성공 · Daily 실행 실패 · 장중 손절 · 장 후 잔고 · 🔴 · 🔵 · ⚪ 손익 이모지 + Slack attachment color bar |
| port-view SlackNotificationService | 유지 · View Daily Batch 수동 실행 결과 알림 책임 · 제거 · 대체 없음 · 향후 공통 notifier 이전 가능성만 기록 |
| webhook URL 처리 | Lambda 환경변수 `SLACK_WEBHOOK_URL` 1차 검증 · 값 미기록 (R-DOCS-001 정합) · Secrets Manager 또는 SSM Parameter Store 이전 예정 (OD-MS-030 신규 · R-AUTO-024 신규 · Status 🔵 **Accepted**) |

- 🟢 **Step Functions approval workflow 3종 Slack 수신 검증**

| 이벤트 | 결과 |
| --- | --- |
| (1) `APPROVAL_REQUIRED` | Step 1~11 완료 후 approval gate 진입 시 Slack 수신 · 수동 승인 인지 흐름 검증 |
| (2) `DAILY_EXECUTION_SUCCESS` | Step 17 완료 후 전체 성공 시 Slack 수신 |
| (3) `DAILY_EXECUTION_FAILED` | SFN Catch · 12~17 test-only + 1~17 full test-only 실패 ASL · 수신 검증 통과 · 실제 broker 주문 실패 유발 0건 |
| 결정 · 리스크 | OD-MS-031 신규 (🟢 확정) · R-AUTO-023 신규 (Status 🟢 **Mitigated**) |

- 🟢 **6. Slack 구현: 완료 / 7. EventBridge 자동화는 다음 단계**

| 항목 | 값 |
| --- | --- |
| 1차 EventBridge / SFN 연동 Slack 범위 | `APPROVAL_REQUIRED` + `DAILY_EXECUTION_SUCCESS` + `DAILY_EXECUTION_FAILED` 3종 한정 |
| 후속 분리 | 장 전 잔고 · 장 후 잔고 · 장중 손절 알림 · Scheduler 정기 트리거 · 정식 production 자동화 · DLQ · retry · CloudWatch Alarm · Slack webhook Secrets Manager 이전 · 04 · 05 · 06 · 10 spec 후속 phase 책임 |

- 🟢 **7. EventBridge 자동화 구현: 완료** (OD-MS-032 신규 · 🟢 확정)

| 항목 | 값 |
| --- | --- |
| Scheduler 2개 | 08:00 KST `portfolio-paper-daily-step1-11-approval-0800-kst` + 09:01 KST `portfolio-paper-daily-step12-17-order-0901-kst` |
| Dispatcher Lambda | `portfolio-paper-daily-scheduler-dispatcher` (Python 3.12 · Handler `lambda_function.lambda_handler` · Timeout 30s · Memory 256MB · State Active) |
| IAM Role 4종 | `portfolio-paper-eventbridge-scheduler-role` · `portfolio-paper-daily-scheduler-dispatcher-role` · SFN 2개 state machine ACTIVE 정합 |
| Inline policy 3종 | `portfolio-paper-scheduler-start-execution-policy` + `portfolio-paper-scheduler-invoke-dispatcher-policy` + `portfolio-paper-daily-scheduler-dispatcher-policy` · Resource · Action wildcard 0건 |
| Dispatcher Lambda 역할 | KST `runDate` (YYYY-MM-DD) 생성 + 주말 · 휴장일 skip + scheduleType 별 payload 분기 + SFN `StartExecution` + 즉시 종료 |

- 🟢 **EventBridge 자동화 검증 완료**

| 검증 | 결과 |
| --- | --- |
| `simulate-principal-policy` | `states:StartExecution` + `lambda:InvokeFunction` 모두 🟢 **allowed** |
| Lambda dryRun (08:00 `STEP1_11_APPROVAL` · 09:01 `STEP12_17_ORDER`) | `started=false` · `reason=DRY_RUN_NO_START_EXECUTION` · target state machine + `allowPaperOrderExecute` 분기 정합 |
| Scheduler `get-schedule` | 08:00 🟢 **ENABLED** · 09:01 🟠 **DISABLED** 단계적 활성화 |
| Lambda 환경변수 | `TIMEZONE=Asia/Seoul` · `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true` · Step1~17 / Step12~17 ARN (평문 기록 0건 · R-DOCS-001 정합) |

- 🟢 **EventBridge 단계적 활성화 정합**

| 항목 | 값 |
| --- | --- |
| 08:00 Scheduler | 🟢 **ENABLED** · 내일 08:00 KST 실행 · Scheduler → Dispatcher Lambda → Step1~17 approval · `allowPaperOrderExecute=false` · Step 12 approval gate 차단 · 주문 제출 없음 |
| 09:01 Scheduler | 🟠 **DISABLED** · 내일 09:01 KST 자동 실행 차단 · Step12~17 자동 실행 없음 · 주문 자동화 ENABLE 전 최종 안전 점검 후 별도 운영자 판단 |
| 실 `StartExecution` (`dryRun=false`) | 본 일자 미검증 · 내일 08:00 실행 시 첫 검증 예정 (Scheduler invocation · Lambda logs · SFN execution 생성 · `APPROVAL_REQUIRED` Slack 수신 · Step 12 approval gate 차단 · 주문 제출 없음) |
| R-AUTO-025 신규 | Status 🟢 **Mitigated** |

- 🟢 **작업 범위 · 안전 · 산출물**

| 항목 | 값 |
| --- | --- |
| 실제 AWS 리소스 · 코드 수정 · DB GRANT | 모두 운영자 직접 수행 · Kiro 는 루트 / `_common` 문서 갱신만 |
| 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 spec 영역 변경 | 0건 |
| 운영자 직접 patch `connector_strategy_order_execute.py` 전체 교체 · EC2 배포 · dry-run 결과 | `_common` 메타 · 03 spec operation-notes 후속 갱신 (2차 · 3차) 책임으로 분리 |
| Slack notifier Lambda 코드 · SFN Catch state ASL · Slack 메시지 · webhook URL · Dispatcher Lambda 코드 · SFN ARN 평문 기록 | 0건 (R-DOCS-001 정합) |

## 2026-06-22 (Daily AWS Paper Wrapper 1~17 완주 + MarketConnector env bootstrap + Step 9 권한 보정 + SELL E2E 검증)

- 🟢 **Daily AWS Paper Wrapper Step 1~17 실 운영 완주**

| 항목 | 값 |
| --- | --- |
| wrapper | `.kiro/scripts/run-daily-aws-paper.ps1` |
| 범위 | Step 1 ~ Step 17 전 단계 실 운영 |
| 종료 상태 | 실 Paper SELL 주문 제출 · KIS 접수 · 체결조회 · SELL fill sync · position CLOSED · balance refresh end-to-end 통과 |
| 기록 의미 | AWS 운영 wrapper 첫 완주성 운영 결과 |
| 환경 · RunDate · region | `aws-paper` · `2026-06-22` · `ap-northeast-2` |
| 참조 | `_common/operator-decisions.md` Change Log · 03 · 04 · 08 · 09 spec operation-notes 2026-06-22 |

- 🟢 **MarketConnector `/tmp/inject-env.sh` 휘발 문제와 bootstrap 패치**

| 항목 | 값 |
| --- | --- |
| Step 1 최초 실패 원인 | MC EC2 stop / start 이후 `/tmp` 휘발로 runtime env bootstrap 파일 부재 |
| patch | `daily-aws-paper.functions.ps1` MC env bootstrap 함수 · Step 1 · 12 · 13 · 17 공통 호출 |
| 값 처리 | Secrets Manager JSON SecretString 내부 key (APP_KEY · APP_SECRET · PAPER_ACNT · ACNT_PRDT_CD) 추출 + APP_* · KIS_* alias export · secret value 로그 미출력 · `/tmp/inject-env.sh` 권한 700 유지 |
| 검증 | PowerShell parser validation 통과 · Step 1 재실행 통과 |
| 결정 · 리스크 | OD-MS-027 신규 · R-AUTO-021 신규 mitigation 1차 실증 |

- 🟢 **Step 9 `execution_app` UPDATE 권한 누락과 GRANT 보정**

| 항목 | 값 |
| --- | --- |
| 1차 실패 | Step 9 `DAILY_SELL_EXECUTION` `decision.strategy_daily_position_decision` UPDATE 권한 누락 |
| 배경 | `execution_app` 기존 SELECT 만 보유 · Step 9 는 SELL execution_order 생성 후 daily position decision row 의 `execution_order_id` 업데이트 필요 |
| GRANT (운영자 직접) | `GRANT USAGE ON SCHEMA decision TO execution_app` + `GRANT UPDATE ON TABLE decision.strategy_daily_position_decision TO execution_app` |
| 결과 | SELECT + UPDATE 권한 확인 · Step 9 ~ Step 11 재실행 통과 |
| 결정 · 리스크 | OD-DB-011 신규 · R-DATA-013 신규 mitigation 1차 실증 · R-DATA-005 [2026-06-22 보강] |

- 🟢 **Step 12 한화생명 244주 SELL_HARD_STOP MARKET E2E**

| 항목 | 값 |
| --- | --- |
| execution_plan_id | `96` (`plan_date 2026-06-22` · `strategy_name strategy_ai` · `market_signal DEFENSIVE` · `risk_regime DEFENSIVE` · `plan_status PARTIALLY_BLOCKED`) |
| plan 지표 | total_candidate 3 · ready 1 · blocked 2 · skipped 0 · total_target_amount `1,237,080` · available_cash `-62,763` · max_order_amount `-31,381.50` |
| Step 12 실 대상 | `088350` 한화생명 SELL 244주 MARKET 1건 (BUY 2건 현금 부족 BLOCKED · 중복 주문 0 rows) |
| 매도 사유 `SELL_HARD_STOP` | entry_date `2026-06-17` · entry_price `5,744.4057` · current_price `5,070` · expected_pnl_rate 약 `-11.7402%` · hard_stop_loss_rate `-10%` · holding_days 5 · snapshot_qty<br>sellable_qty · remaining_qty 모두 244 · expected_pnl_amount 약 `-164,554.9908` |
| 사전 확인 | `strategy_position_state id 9` OPEN entry_qty 244 · remaining_qty 244 와 Step 12 SELL 수량 244 일치 |
| Step 12 `-AllowPaperOrderExecute` 실행 | execution_order id `37` 🟢 **SUBMITTED** · connector_order_request id `46` 🟢 **ACCEPTED** · request_type SELL · order_method MARKET · order_qty 244 · broker_order_no · broker_branch_code 생성 · rejection 없음 |
| Step 12 `-AllowPaperOrderExecute` 실행 (계속) | 본 노트 broker 응답 평문 0건 (R-DOCS-001 정합) |
| Step 13 체결조회 | connector_order_request id `46` 🟢 **FILLED** · connector_fill id `34` · fill_qty 244 · fill_price `5,075.8607` · fill_amount `1,238,510.01` · side SELL · fill_ts `2026-06-22 00:46:58 UTC` |
| Step 14 / 15 / 16 · Step 17 | ECS exitCode 0 · SSM 🟢 **Success** |
| 최종 DB 검증 | `strategy_execution_order id 37` 🟢 **FILLED** · `strategy_position_state id 9` remaining_qty 0 · position_status 🟢 **CLOSED** · latest_sell_reason `SELL_HARD_STOP` |
| 최신 잔고 | `connector_position_snapshot` created_at `2026-06-22 00:50:50 UTC` · 보유 5종목 (`003490` 58주 · `004990` 69주 · `023530` 8주 · `042660` 11주 · `282330` 17주) · `088350` 한화생명 스냅샷에서 제거 확인 |

- 🟢 **결정 · 리스크 변경**

| 항목 | 값 |
| --- | --- |
| ID | OD-MS-027 신규 |
| Status | 🟠 **잠정** |
| 메모 | MC env bootstrap 재생성 운영 정책 · `/tmp/inject-env.sh` 선존재 가정 폐기 · step 실행 시점 재생성 · wrapper 공통 함수 · secret value 미출력 |

| 항목 | 값 |
| --- | --- |
| ID | OD-DB-011 신규 |
| Status | 🟢 **확정** |
| 메모 | `execution_app` decision.strategy_daily_position_decision 제한적 UPDATE 권한 · SELL execution link update 책임 한정 |

| 항목 | 값 |
| --- | --- |
| ID | OD-MS-016 · OD-MS-021 · OD-MS-023 |
| Status | 보강 |
| 메모 | wrapper 1~17 두 번째 실 완주 1차 실증 |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-021 신규 |
| Status | 🟢 **Mitigated** |
| 메모 | MC EC2 stop · start 후 `/tmp` 휘발로 Step 1 · 12 · 13 · 17 실패 |

| 항목 | 값 |
| --- | --- |
| ID | R-DATA-013 신규 |
| Status | 🟢 **Mitigated** |
| 메모 | `execution_app` decision schema UPDATE 권한 누락 |

| 항목 | 값 |
| --- | --- |
| ID | R-DATA-005 [2026-06-22 보강] |
| Status | 보강 |
| 메모 | `execution_app` decision schema USAGE + table UPDATE 누락이 Step 9 1차 실패 원인 · 02 spec 정식 갱신 후속 |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` 2026-06-22 | 신규 |
| `.kiro/README.md` | 진행 상태 요약 (기준일 2026-06-22 · Backend AWS E2E 두 번째 실 완주) |
| `_common/operator-decisions.md` | Change Log · OD-MS-027 · OD-DB-011 신규 · Summary 84 → 86 · 잠정 39 → 40 · 확정 42 → 43 |
| `_common/risk-register.md` | R-AUTO-021 · R-DATA-013 신규 + R-DATA-005 보강 |
| `_common/followups-overview.md` · `_common/ms-aws-service-decision-matrix.md` | 2026-06-22 후속 · E2E 리허설 2회차 |
| 03 · 04 · 08 · 09 spec operation-notes 2026-06-22 | 누적 |
| 03 spec runbook · tasks · validation-checklist | env 주입 · task 7 · 26 · 27 · 검증 |
| 08 spec tasks | task 57 · 105 · 110 보강 |

- 🟢 **안전 · 보안**

| 항목 | 결과 |
| --- | --- |
| AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · RDS · GRANT · KIS | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 정리만 |
| AWS CLI · boto3 · AWS 리소스 생성 · 수정 · 삭제 | 0건 |
| `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
| CloudWatch Logs · SSM 응답 · KIS API response body 전문 인용 | 0건 |
| broker / KIS 호출 | KIS paper SELL 1건 (Step 12) + balance · order check 조회성 |
| 추가 BUY · 취소 · 정정 · `--execute` · fill · position sync 자동 재시도 · aws-live 작업 | 0건 |
| 민감정보 (secret · KIS · 계좌 · token · RDS · account-id · IAM · secret ARN · access key · instance-id · EIP · image digest · task ARN · job ARN · broker_order_no · broker_branch_code · KIS paper credential · Administrator password) | 신규 기록 없음 · 모두 `[REDACTED]` 또는 placeholder |

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

- 🟢 **작업 배경 · 결과 요약**

| 항목 | 값 |
| --- | --- |
| 입력 | 2026-06-20 후속 메모 (EC2 lifecycle 보강 · Step 12 별도 승인 전 금지) + 6/19 KRX raw 최신성 회복 결과 |
| 강화 범위 | Step 2 wrapper 성공판정 = (a) Chrome · chromedriver best-effort reset · (b) Scheduled Task Running → Ready 복귀 wait + Last Result 0 · 0x0 · (c) 최신 worker log path · size · tail 출력 · (d) KRX raw DB validation 통과 |
| 작업 주체 | 운영자 직접 patch · 검증 · 본 spec 영역에만 누적 |
| 8개 MS 소스 · README · docs · worklog 변경 | 0건 |
| 참조 | `_common/operator-decisions.md` (OD-MS-026 신규) · 08 spec operation-notes 2026-06-21 · `_common/followups-overview.md` 2026-06-21 |

- 🟢 **변경 내용**

| 항목 | 값 |
| --- | --- |
| (a) `step-02-interest-crawler.ps1` | Chrome · chromedriver stale reset · Scheduled Task `Portfolio-KRX-Worker-Daily` `schtasks /Run` · Running polling → Ready wait · Last Result 0 / 0x0 만 🟢 **SUCCESS** · latest log path · size · tail |
| (a) `step-02-interest-crawler.ps1` (계속) | worker EC2 running 아니면 <span style="color:#D1242F">**fail-closed**</span> |
| (b) non-GUI ECS RunTask overrides | `TEMP=/tmp` · `TMP=/tmp` · `PYTHONUTF8=1` · `PYTHONIOENCODING=utf-8` + 공통 함수 `Invoke-DailyAwsPaperEcsTask` `EnvironmentVariables` · `New-SsmParameterFile` `ExecutionTimeoutSeconds` · `Invoke-SsmCommandAndWait` `ExecutionTimeoutSeconds` |
| (c) 신규 스크립트 | `interest_krx_raw_validate_daily.py`<br>`interest_program_raw` · `interest_shortsell_raw` expected trade_date 검증<br>row_count + max(trade_date) ≥ expected · 실패 시 exit 30<br>로컬 py_compile + marker 통과 |
| (d) S3 presigned URL 배포 | S3 bucket `portfolio-paper-migration-yukiever`<br>key `tmp/krx/interest_krx_raw_validate_daily.py` → EC2<br>SSM commandId `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3` |
| (e) EC2 단독 검증 | user=`crawler_app` · schema=`interest` · search_path `interest, reference, legacy, public` · `interest_program_raw` `2026-06-19` count=`1` OK · `interest_shortsell_raw` `2026-06-19` count=`349` OK · exit 0 |
| (e) EC2 단독 검증 (계속) | SSM commandId `c844aea5-1429-430a-9510-39fc99f17f05` |
| (f) wrapper 통합 | `step-02-interest-crawler.ps1` 이 `ExpectedKrxRawDate` (RunDate 기준 전 영업일) 계산<br>→ worker EC2 SSM `INTEREST_CRAWLER_KRX_DB_VALIDATE` 실행<br>non-zero exit / row_count 0 시 Step 2 fail<br>step result 에 `KrxDbValidationCommandId` 기록 |

- 🟢 **검증 milestone (2026-06-21)**

| 항목 | 값 |
| --- | --- |
| wrapper 단독 실행 | `.\run-daily-aws-paper.ps1 -Environment aws-paper -RunDate 2026-06-20 -StartStep 2 -EndStep 2` |
| RunId · StepCode · Status · Runner · ExpectedKrxRawDate | `daily-aws-paper-20260621-204017` · `INTEREST_CRAWLER` · 🟢 **SUCCESS** · `ECS+SSM` · `2026-06-19` |
| (a) non-GUI ECS | taskDefinition `portfolio-paper-interest-crawler:7` · taskId `78979b5cbb714d0eb94f5946e15a14ce` · exitCode 0 · stoppedReason `Essential container in task exited` · CloudWatch log saved |
| (b) KRX GUI worker | state `running` · commandId `f9d82fcc-1e26-4710-87c3-1d20483b63ef` · 🟢 **Success**<br>`TaskStatus=Running` → `Ready` (elapsedSeconds=111)<br>Logon `Interactive only`<br>Run As `Administrator`<br>latest log `krx_worker_daily_20260621_114154.log` |
| (c) KRX raw DB validation | SSM `INTEREST_CRAWLER_KRX_DB_VALIDATE` · commandId `2279c6d7-2da6-4317-9c10-7cc77374b317` · 🟢 **Success** · DB user=`crawler_app` · schema=`interest` · `interest_program_raw` `2026-06-19` count=`1` OK |
| (c) KRX raw DB validation (계속) | `interest_shortsell_raw` `2026-06-19` count=`349` OK |

- 🟢 **결정 · 리스크 변경**

| 항목 | 값 |
| --- | --- |
| ID | OD-MS-026 신규 |
| Status | 🟠 **잠정** |
| 메모 | Step 2 성공 기준 = Scheduled Task trigger 가 아니라 KRX raw DB validation 까지 · KRX GUI = Windows Administrator interactive Scheduled Task · wrapper 는 실행 · 대기 · Last Result · latest log · DB validation orchestration |
| 메모 (계속) | validation script 가 program · shortsell 최신성 검증 · worker stopped 는 fail-closed |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-020 신규 |
| Status | 🟢 **Mitigated** |
| 메모 | Scheduled Task trigger 성공만 보고 Step 2 SUCCESS 처리하면 KRX raw 미적재가 Step 3 이후로 전파될 위험 · mitigation = Chrome / chromedriver best-effort reset + Running → Ready wait + Last Result 확인 + latest log 출력 + DB validation + worker stopped fail-closed |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-007 mitigation · detection |
| Status | 보강 |
| 메모 | wrapper 안 DB 검증 자동 출력이 1차 실증 · task 57 후속에서 task 단위 완료 승격 · `KrxDbValidationCommandId` step result · expected date row_count 자동 확인 |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-016 mitigation |
| Status | 보강 |
| 메모 | 이전 "wrapper 안 자동 skip" → fail-closed 로 대체 · crawler worker EC2 stopped 즉시 실패 처리 · instanceId · state 출력 · KRX GUI worker / DB validation 미수행 시 Step 2 SUCCESS 진입 차단 |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-017 mitigation |
| Status | 보강 |
| 메모 | Chrome / chromedriver best-effort reset 1차 실증 · 실패는 warning · 후속 EC2 stop / restart 절차는 R-AUTO-016 보강과 결합 |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` 2026-06-21 | 신규 |
| `.kiro/README.md` | "Windows KRX crawler worker 와 Step 2 동작" fail-closed 갱신 + Step 2 단독 실행 예시 |
| `_common/operator-decisions.md` | Change Log · OD-MS-026 신규 · Summary 갱신 |
| `_common/risk-register.md` | R-AUTO-020 신규 + R-AUTO-007 · R-AUTO-016 · R-AUTO-017 보강 |
| `_common/followups-overview.md` · `_common/ms-aws-service-decision-matrix.md` | 2026-06-21 후속 · Step 2 성공 조건 + worker stopped fail-closed |
| 08 spec operation-notes 2026-06-21 | 누적 |
| 08 spec tasks | task 57 · 92 · 94 완료 + 신규 task 항목 |
| 08 spec design · requirements | Step 2 성공 조건 + fail-closed + Acceptance Criteria 보강 |

- 🟢 **안전 · 보안**

| 항목 | 결과 |
| --- | --- |
| AWS · SSM · EC2 · S3 · ECS · RDS · KRX 호출 | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 · 검증 정리만 |
| AWS CLI · boto3 · AWS 리소스 생성 · 수정 · 삭제 | 0건 |
| broker / KIS · 신규 BUY · SELL · 취소 · 정정 · `--execute` · fill · position sync 자동 재시도 · aws-live 작업 | 0건 |
| RDS DDL · DML | 0건 (검증 SQL SELECT 한정 · `interest_program_raw` · `interest_shortsell_raw` 신규 row 는 KRX worker 적재 결과 · 본 일자 Step 2 wrapper 는 idempotent · no-op) |
| 민감정보 (secret · KIS · 계좌 · token · RDS · account-id · IAM · secret ARN · access key · EIP · image digest · task ARN · job ARN · KIS paper credential · Administrator password · S3 presigned URL 실값) | 신규 기록 없음 · 모두 `[REDACTED]` 또는 placeholder |

- 🟢 **다음 작업**

| 항목 | 값 |
| --- | --- |
| 본 일자 마감 | Step 2 보강 완료 |
| 후속 분리 | EC2 lifecycle 자동 start · SSM Online wait · stop 절차 · SFN 에서 ECS RunTask + SSM RunCommand 혼합 orchestration · View Daily Batch 에서 `KrxDbValidationCommandId` · latest worker log · Step 2 validation 결과 표시 · worker log centralized collection |
| Step 12 실 paper 주문 제출 | 별도 승인 전 실행 금지 |

<details><summary>🔵 운영 식별자 요약</summary>

| 항목 | 값 |
| --- | --- |
| SSM commandId (3건) | `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3` · `c844aea5-1429-430a-9510-39fc99f17f05` · `2279c6d7-2da6-4317-9c10-7cc77374b317` · `f9d82fcc-1e26-4710-87c3-1d20483b63ef` |
| RunId | `daily-aws-paper-20260621-204017` |
| taskDefinition · taskId | `portfolio-paper-interest-crawler:7` · `78979b5cbb714d0eb94f5946e15a14ce` |
| S3 key | `tmp/krx/interest_krx_raw_validate_daily.py` |
| Scheduled Task · wrapper 옵션 | `Portfolio-KRX-Worker-Daily` · `-StartStep` · `-EndStep` |
| latest worker log | `krx_worker_daily_20260621_114154.log` |
| DB session | user · search_path · row_count |

</details>

## 2026-06-20 (AWS 자동 Wrapper 최종 확인 + 6/18 중복 실행 시도 안전 중단 + 6/19 KRX raw 최신성 복구 상태)

- 🟢 **작업 범위**

| 항목 | 값 |
| --- | --- |
| (a) 최종 점검 | Daily AWS Paper Wrapper (`run-daily-aws-paper.ps1` + config + functions + `steps/step-01 ~ 17`) 구조 · 안전 기준 · EC2 기동 기준 |
| (b) 재실행 시도 안전 중단 | RunDate `2026-06-18` · Step 1 ~ Step 11 범위 · 중복 실행 가능성 인지 · Step 3 PREPROCESSOR 진입 직후 Ctrl+C |
| (c) 6/19 KRX raw DB 최신성 복구 상태 점검 | 누적 |
| 실작업 | 운영자 직접 (AWS · EC2 · SSM · RDS · KRX) · Kiro 는 문서 · 절차 정리만 |
| 참조 | `_common/followups-overview.md` 2026-06-20 · 08 spec operation-notes 2026-06-20 |

- 🟢 **wrapper 안전 기준 · EC2 기동 기준 재확인**

| 항목 | 값 |
| --- | --- |
| Step 1 ~ 11 | broker / KIS 주문 제출 전 단계 |
| Step 12 (`MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) | 실 KIS paper 주문 제출 가능 · `-AllowPaperOrderExecute` 미명시 시 차단 · 토 · 휴장일 제외 |
| EC2 기동 기준 | MC EC2 (Step 1 · 12 · 13 · 17) + Crawler Worker EC2 (Step 2 KRX GUI) · 둘 다 stopped 이면 wrapper 실행 전 start 필요 |
| `/tmp/inject-env.sh` 유실 가능성 | EC2 stop / start 후 · 본 일자 1차 실증 (재생성 후 Step 1 재실행 통과) · lifecycle 보강 후속 |

- 🟢 **6/18 중복 실행 시도 결과**

| 항목 | 값 |
| --- | --- |
| Step 1 CONNECTOR_BALANCE | 최초 실패 (env 유실) → MC EC2 `/tmp/inject-env.sh` 재생성 후 재실행 통과 |
| Step 2 INTEREST_CRAWLER | non-GUI ECS exitCode 0 · KRX GUI worker Scheduled Task trigger 성공 |
| 확인 상태 | 6/18 ECS RUNNING 0건 · AWS Batch RUNNING · SUBMITTED · PENDING · RUNNABLE 0건 · KRX Scheduled Task `Ready` · LastTaskResult 0 · Step 12 미실행 |
| 중단 처리 | Step 3 PREPROCESSOR 진입 직후 Ctrl+C · 중복 plan 생성 차단 |
| DB 점검 | 2026-06-18 기존 `execution_plan_id 94` 정상 완료 (BUY 4건 · FILLED 4건 · connector linked 4건) · 2026-06-20 04:20 UTC 이후 신규 execution_plan · strategy_execution_order · connector_order_request 0건 · KIS 신규 주문 0건 |
| 정리 | Crawler Worker chrome 잔여 프로세스는 EC2 stop 으로 정리 · MC · Crawler Worker EC2 stop 처리 |

- 🟢 **6/19 KRX raw 최신성 복구 상태**

| 테이블 | 값 |
| --- | --- |
| `interest_program_raw` max_date | `2026-06-19` · 2026-06-18 row_count `1` · 2026-06-19 row_count `1` |
| `interest_shortsell_raw` max_date | `2026-06-19` · 2026-06-18 row_count `349` · 2026-06-19 row_count `349` |
| 결론 | KRX raw 기준 최신성 복구 완료 |
| 후속 | Windows EC2 worker 자동화의 Scheduled Task trigger / LASTEXITCODE 중심 성공판정 한계 (R-AUTO-007 정합) · Step 2 wrapper 성공판정 강화 2026-06-21 분리 |

- 🟢 **결정 · 리스크 · 산출물 · 안전**

| 항목 | 결과 |
| --- | --- |
| 신규 OD · R | 0건 |
| 후속 인계 | EC2 lifecycle 자동 start · stop · `/tmp/inject-env.sh` 재생성 · Scheduled Task trigger-only 성공판정 한계 → Step 2 wrapper 강화 (2026-06-21) |
| Kiro 작업 산출물 | `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` 2026-06-20 · 08 spec operation-notes 2026-06-20 누적 · `_common/followups-overview.md` 2026-06-20 후속 (EC2 lifecycle · `/tmp/inject-env.sh` · Step 12 별도 승인 전 금지) |
| AWS · SSM · EC2 · RDS 작업 | 모두 운영자 직접 수행 · AWS CLI / boto3 · AWS 리소스 생성 · 수정 · 삭제 · broker · KIS · `--execute` · aws-live 작업 · RDS DDL · DML 0건 (SELECT 한정) |
| 민감정보 | 신규 기록 없음 · 모두 `[REDACTED]` 또는 placeholder |
| 운영 식별자 | `execution_plan_id 94` · RunDate · `interest_program_raw` · `interest_shortsell_raw` row_count / max_date |

- 🟢 **다음 작업**

| 항목 | 값 |
| --- | --- |
| 본 일자 마감 | Step 2 wrapper 성공판정 강화는 2026-06-21 분리 |
| 후속 유지 | EC2 lifecycle · SFN · View 표시 연동 · worker log centralized collection |

## 2026-06-18 (Step 13 `connector_order_check.py` 단건 순차 조회 기본화 — 운영 안전성 강화)

- 🟢 **작업 배경 · 성격**

| 항목 | 값 |
| --- | --- |
| 배경 | 같은 일자 Daily AWS Paper Wrapper 17단계 실운영 검증 완료 후속 |
| 성격 | 운영 실패 아님 · 운영 안전성 강화를 위한 설계 변경 |
| 이슈 | KIS paper 체결조회 응답 (`output1 empty` + `output2 summary-only`) 이 다건 active 상태에서도 반환 가능 (R-AUTO-018 정합) → 기본 동작에서 한 단계 더 차단 |
| 책임 분리 | wrapper ps1 은 Step 13 실행 orchestration · 체결조회 방식 제어는 `connector_order_check.py` 내부 (OD-MS-025 신규 정합) |
| 참조 | `_common/operator-decisions.md` Change Log · 03 spec operation-notes 2026-06-18 (Step 13) · `_common/followups-overview.md` 2026-06-18 두 번째 후속 |

- 🟢 **변경 내용**

| 항목 | 값 |
| --- | --- |
| 대상 파일 | `port-marketconnector/connector_order_check.py` (EC2 `/home/ec2-user/apps/port-marketconnector/src/connector_order_check.py`) |
| 기본 모드 변경 | broad 체결조회 중심 → active 주문 목록 조회 + 주문번호 / 종목코드 기준 단건 direct-only 순차 조회 |
| 신규 옵션 | `--broad` (legacy · 기본 미사용) · `--active-limit` (단건 순차 조회 최대 건수) |
| 명시 주문 조회 | `--code {ticker_code} --order-no {broker_order_no} --no-broad` (broad fallback 없이) |
| 안전 기준 | 다건 active 상태에서 `output2 summary-only` 응답 DB 반영 근거 사용 금지 · `connector_order_request` 후보가 주문번호 / 종목코드 기준 1건 확정 시에만 summary fallback 허용 · `connector_order_event` · `connector_fill` 오매핑 방지 (R-AUTO-018 [2026-06-18 추가 보강] mitigation 정합) |

- 🟢 **검증 milestone (2026-06-18 두 번째 세션)**

| 항목 | 값 |
| --- | --- |
| 로컬 검증 | `python -m py_compile connector_order_check.py` 통과 · `--help` 옵션 표시 확인 |
| commit | `75cb804` (`fix(connector): run order checks sequentially per active order` · remote push destination 미설정 → git push 미수행) |
| EC2 반영 | S3 → MC EC2 (`i-0fce77927b7397b88`) 배포 · bucket `portfolio-paper-migration-yukiever` · key `deploy/marketconnector/connector_order_check.py` · 39159 bytes |
| EC2 반영 (계속) | backup `connector_order_check.py.bak-20260618-step13-per-order` 생성 후 교체 · 소유권 `ec2-user:ec2-user` 복구 · `--broad` · `--active-limit` 마커 확인 |
| EC2 검증 | `../.venv` 활성화 + `/tmp/inject-env.sh` 로드 → `python3 -m py_compile` 통과 · `--help` 옵션 표시 |
| 단건 direct-only 테스트 | `python3 connector_order_check.py --code 004990 --order-no 0000025576 --no-broad` · SSM Status 🟢 **Success** · ResponseCode `0` · StdErr empty |
| wrapper Step 13 단독 실행 | `-StartStep 13 -EndStep 13` (RunDate `2026-06-18`)<br>Selected `Step 13 CONNECTOR_ORDER_CHECK only`<br>SSM commandId `b344d404-07a4-4bb6-9d63-34151e648bab` · 🟢 **Success** · responseCode `0`<br>Step 13 🟢 **COMPLETED** |

- 🟢 **결정 · 리스크 변경**

| 항목 | 값 |
| --- | --- |
| ID | OD-MS-025 신규 |
| Status | <span style="color:#BF8700">**잠정**</span> |
| 메모 | `connector_order_check.py` 운영 모드 = active 주문 단건 순차 조회 기본 · broad 일괄 조회는 legacy · `--broad` 명시 시에만 · 명시 주문 조회는 `--code` · `--order-no` · `--no-broad` · 다건 active 시 summary fallback DB 반영 금지 · 단건 후보 확정 시에만 허용 |
| 메모 (계속) | wrapper ps1 은 orchestration 만 · 체결조회 방식 제어는 `connector_order_check.py` 내부 |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-018 detection / mitigation |
| Status | [2026-06-18 추가 보강] |
| 메모 | 첫 세션 wrapper 안 자동 skip 1차 실증 + `connector_order_check.py` 기본 실행 모드가 단건 순차 조회로 변경 · broad 호출 빈도 감소 · summary fallback 노출 표면 감소 · Status 🟢 **Mitigated** 유지 |

| 항목 | 값 |
| --- | --- |
| ID | 기존 Step 13 자동화 후속 |
| Status | 1차 해소 |
| 메모 | 잔여 후속 (Step 13 summary 출력 보강 · View 화면 연동 · Git remote · cp949 인코딩) 은 followups 2026-06-18 두 번째 후속 메모 |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` 2026-06-18 두 번째 | 신규 |
| `.kiro/README.md` | 운영자 로컬 PowerShell wrapper 섹션의 Step 13 안전 주의사항 신규 + StartStep / EndStep 파라미터 명시 (FromStep / ToStep 실수 방지) |
| `_common/operator-decisions.md` | Change Log · OD-MS-025 신규 · Summary 갱신 |
| `_common/risk-register.md` | R-AUTO-018 [2026-06-18 추가 보강] |
| `_common/followups-overview.md` | 2026-06-18 두 번째 후속 (Git remote 설정 · Step 13 summary · View 주문별 표시 · cp949 인코딩) |
| 03 spec operation-notes 2026-06-18 (Step 13) §1~§6 | 누적 |
| 04 spec · aws-resource-glossary · cost-simulation · ms-aws-service-decision-matrix | 변경 0건 (본 변경은 03 spec 책임 · Step 13 한정) |

- 🟢 **안전 · 보안**

| 항목 | 결과 |
| --- | --- |
| AWS · SSM · EC2 · S3 · KIS 호출 | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 정리만 |
| AWS CLI · boto3 · AWS 리소스 생성 · 수정 · 삭제 | 0건 |
| broker / KIS 호출 | 단건 direct-only 조회 1건 + wrapper Step 13 단독 실행 1건 (모두 조회성) |
| 신규 BUY · SELL · 취소 · 정정 · `--execute` · fill · position sync 자동 재시도 · aws-live 작업 | 0건 |
| RDS DDL | 0건 |
| RDS DML | Step 13 정상 흐름 한정 · `connector.connector_order_event` · `connector.connector_fill` 신규 row 는 단건 direct-only 결과 정합 |
| 민감정보 | 신규 기록 없음 · 모두 `[REDACTED]` 또는 placeholder |

- 🟢 **다음 작업**

| 항목 | 값 |
| --- | --- |
| Git remote push destination 설정 | commit `75cb804` push |
| Step 13 summary 출력 보강 | active_order_count · single_check_success_count · single_check_failed_count · broad_mode_used |
| View Daily Batch 화면 연동 | Step 13 결과를 주문별 표시 |
| Windows PowerShell · AWS CLI SSM 이모지 stdout | cp949 인코딩 오류 회피 패턴 정리 |

<details><summary>🔵 운영 식별자 요약</summary>

| 항목 | 값 |
| --- | --- |
| commit | `75cb804` |
| S3 bucket · key · 크기 | `portfolio-paper-migration-yukiever` · `deploy/marketconnector/connector_order_check.py` · `39159 bytes` |
| EC2 instance · backup 파일 | `i-0fce77927b7397b88` · `connector_order_check.py.bak-20260618-step13-per-order` |
| SSM commandId | `b344d404-07a4-4bb6-9d63-34151e648bab` |
| 종목 · broker_order_no | `004990` · `0000025576` |
| wrapper 옵션 | `--broad` · `--active-limit` · `--code` · `--order-no` · `--no-broad` |
| wrapper 파라미터 | `StartStep` · `EndStep` |

</details>

## 2026-06-18 (Daily AWS Paper Wrapper 17단계 실운영 검증 완료)

- 🟢 **Daily AWS Paper Wrapper Step 1~17 실 실행 완료**

| 항목 | 값 |
| --- | --- |
| wrapper | `.kiro/scripts/run-daily-aws-paper.ps1` |
| 의미 | View 구현 전 CLI 기준 Daily 전체 실행 기준선 1차 완성 |
| 환경 · RunDate · region | `aws-paper` · `2026-06-18` · `ap-northeast-2` |
| Step 12 옵션 | `-AllowPaperOrderExecute` 명시로 paper 주문 허용 |
| 결과 | 실 KIS paper BUY 4건 제출 · 체결 · fill sync · position sync · balance refresh 까지 E2E 정상 연결 |
| aws-live 작업 | 0건 |

- 🟢 **17 step 결과 (시간순)**

| Step | 결과 |
| --- | --- |
| Step 1 CONNECTOR_BALANCE | MC EC2 SSM RunCommand 완료 |
| Step 2 INTEREST_CRAWLER | non-GUI ECS + Windows KRX worker · interest raw 최신성 `2026-06-17` 회복 |
| Step 3 PREPROCESSOR | `pre_total_*_feature` 최신성 `2026-06-17` |
| Step 4 BACKTEST_RESEARCH | AWS Batch 🟢 **SUCCEEDED** · 결과 최신성 `2026-06-17` |
| Step 5 BACKTEST_REPORT | 🟢 **SUCCEEDED** · S3 산출물 |
| Step 6 DAILY_BUY_SIGNAL | BUY READY 4건 (`004990` 롯데지주 · `023530` 롯데쇼핑 · `003490` 대한항공 · `042660` 한화오션) |
| Step 7 DAILY_POSITION_SIGNAL | 보유 종목 HOLD decision 정상 |
| Step 8 DAILY_BUY_EXECUTION | `execution_plan_id 94` · 추가매수 허용 정책 · blocked 0 · skipped 0 |
| Step 9 / Step 10 | 정상 skip (SELL 대상 없음) |
| Step 11 DAILY_AUTO_BUY | 4건 `READY -> REQUESTED` |
| Step 12 MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE | 1차 KIS paper API read timeout · request `38~41` · order `30~33` <span style="color:#D1242F">**FAILED**</span> · 통제 REQUESTED 복구 후 재시도 → BUY 4건 🟢 **ACCEPTED** · request `42~45` · order `30~33` 🟢 **SUBMITTED** |
| Step 12 MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE (계속) | broker_order_no `0000025576`, `0000025740`, `0000025744`, `0000025747` |
| Step 13 CONNECTOR_ORDER_CHECK | broad 조회 `output1 empty` + `output2 summary-only` · active candidate 4건 · summary fallback 🟢 **skipped** (R-AUTO-018 mitigation 1차 실증) → `--code` / `--order-no` / `--no-broad` 단건 재조회로 4건 체결 반영 (connector_order_event · connector_fill 생성) |
| Step 14 | 정상 skip |
| Step 15 SYNC_BUY_FILL | strategy_execution_order `30~33` 🟢 **FILLED** |
| Step 16 SYNC_BUY_POSITION | 1차 추가매수 unique constraint 충돌 · `execution_sync_buy_position.py` merge 패치 (`merge_open_position_state()` + `additional_buys` 누적 + idempotency) · Docker rebuild + ECR push (image digest placeholder) · ECS 재실행 exitCode 0 |
| Step 16 SYNC_BUY_POSITION (계속) | 반영: 004990 65→69주 · 003490 52→58주 · 023530 신규 8주 id 11 · 042660 신규 11주 id 12 |
| Step 17 BALANCE_REFRESH | SSM commandId `66ec8831-74b9-469c-8410-6ccb11cb3400` · responseCode 0 · `connector.connector_position_snapshot` row_count 36 · max_created_at `2026-06-18 04:33:07.456056+00` · `legacy.holdings` row_count 41 |
| Step 17 BALANCE_REFRESH (계속) | max_created_at `2026-06-18 04:33:07.420226` (view_app 권한 부재로 `portfolio_admin` 직접 조회) |

- 🟢 **결정 · 리스크 변경**

| 항목 | 값 |
| --- | --- |
| ID | OD-MS-024 신규 |
| Status | 🟠 **잠정** |
| 메모 | 추가매수 허용 정책 + 기존 OPEN 시 신규 INSERT 아닌 `merge_open_position_state()` · `buy_info.additional_buys` 이력 · execution_order_id · connector_order_request_id 기준 idempotency |

| 항목 | 값 |
| --- | --- |
| ID | R-BROKER-004 신규 |
| Status | 🟢 **Mitigated** |
| 메모 | KIS paper API read timeout 시 단순 재실행 중복 주문 위험 · mitigation = 사전 점검 후 통제된 REQUESTED 복구 · 1차 실증 |

| 항목 | 값 |
| --- | --- |
| ID | R-DATA-012 신규 |
| Status | 🟢 **Mitigated** |
| 메모 | 추가매수 시 strategy_position_state unique constraint 충돌 위험 · mitigation = merge 패치 + idempotency |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-009 · R-AUTO-010 · R-AUTO-011 · R-AUTO-019 |
| Status | 보강 · 🟢 **Mitigated** |
| 메모 | KIS paper BUY 4건 1차 timeout → 복구 → end-to-end 통과 · Step 12 `-AllowPaperOrderExecute` 첫 사용 |

| 항목 | 값 |
| --- | --- |
| ID | R-AUTO-018 detection |
| Status | 보강 |
| 메모 | broad 조회 다건 active 상태 summary fallback 자동 skip 1차 실증 · 단건 `--code` · `--order-no` · `--no-broad` 조회만 fallback 허용 |

| 항목 | 값 |
| --- | --- |
| ID | R-DATA-005 후속 |
| Status | 보강 |
| 메모 | view_app 의 `legacy.holdings` SELECT 권한 부재 · `portfolio_admin` 우회 · 정식 GRANT 후속 (02 spec) |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `.kiro/WORKLOG.md` · `.kiro/CHANGELOG.md` 2026-06-18 | 신규 |
| `.kiro/README.md` | 진행 상태 요약 · wrapper 섹션 갱신 (wrapper 1~17 실 완료 · Step 12 `-AllowPaperOrderExecute` 첫 사용 · bundled 미생성) |
| `_common/operator-decisions.md` | Change Log · OD-MS-024 신규 · Summary 갱신 |
| `_common/risk-register.md` | R-BROKER-004 · R-DATA-012 신규 + R-AUTO-009 · R-AUTO-010 · R-AUTO-011 · R-AUTO-018 · R-AUTO-019 · R-DATA-005 보강 |
| `_common/followups-overview.md` | 2026-06-18 후속 (Step 13 단건 자동화 · KIS timeout 재시도 정책 · Step 16 patch 커밋 · view_app legacy 권한 · wrapper 로그·summary · View 화면 연동) |
| 03 spec operation-notes 2026-06-18 §1~§4 | Step 1 · Step 12 timeout 복구 · Step 13 broad → 단건 · Step 17 결과 + view_app legacy 우회 |
| 04 spec operation-notes 2026-06-18 §1~§6 | Step 6~11 · 14~16 결과 + Step 16 merge 패치 + Docker build context 주의 + ECR push / digest 운영자 보관 |
| `_common/ms-aws-service-decision-matrix.md` | 결정값 변경 없이 1차 실증 메모 (MC Step 12·13·17 · Execution Step 15·16 · Daily wrapper 17 step E2E) |
| `_common/aws-resource-glossary.md` · `_common/cost-simulation.md` | 본문 변경 0건 |

- 🟢 **안전 · 보안**

| 항목 | 결과 |
| --- | --- |
| AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · RDS · KIS | 모두 운영자 직접 수행 · Kiro 는 문서 · 절차 · 검증 정리만 |
| AWS CLI · boto3 · AWS 리소스 생성 · 수정 · 삭제 | 0건 |
| `secretsmanager:GetSecretValue` 결과값 · CloudWatch Logs · SSM 응답 · KIS API response body · PowerShell stdout 전문 평문 인용 | 0건 |
| broker / KIS 호출 | KIS paper BUY 4건 (Step 12 본 실행) + balance · order check 조회성 |
| SELL · 취소 · 정정 · 추가 `--execute` · `mark_position_sell_ordered()` · fill · position sync 자동 재시도 · aws-live 작업 | 0건 |
| 민감정보 | 신규 기록 없음 · 모두 `[REDACTED]` 또는 placeholder |

- 🟢 **다음 작업**

| 항목 | 값 |
| --- | --- |
| 후속 유지 | Step 13 단건 자동화 보완 · KIS timeout 재시도 정책 명문화 · Step 16 merge 패치 정식 commit + 07 spec CI/CD · view_app `legacy.holdings` SELECT 권한 검토 · wrapper summary 보강 · View Daily Batch 화면 연동 전 summary 개선 · bundled wrapper 생성 여부 |
| 후속 유지 (계속) | EventBridge 정기 트리거 · SFN 이전 · aws-live cutover (10 spec) |

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
| 종목 코드 | `004990` · `023530` · `003490` · `042660` |
| data_date · signal_date · run_date | `2026-06-17` · `2026-06-18` |

</details>

# 2026-06-16~17 AWS 작업 기록 2열 정리

## 2026-06-17 — Daily AWS PowerShell wrapper 구현

### 작업 목표

| 항목 | 값 |
| --- | --- |
| 배경 | 같은 일자 17-step E2E 완료 후속 |
| 실행 위치 | 운영자 로컬 Windows PowerShell |
| 목표 1 | 수동 AWS Console 의존도 감소 |
| 목표 2 | 재현 가능한 Daily 실행 단위 제공 |
| 목표 3 | View 구현 전 CLI 실행 기준선 확보 |
| 목표 4 | 완전 자동화 전 단계별 확인 가능한 안전 도구 |
| Step 12 정책 | 기본 차단 |
| 주문 허용 | 운영자가 명시 승인 옵션을 입력한 경우만 |
| wrapper 기반 1~17 실 재실행 | 0건 |
| aws-live 실행 | 0건 |

### 산출물

| 파일 | 역할 |
| --- | --- |
| `run-daily-aws-paper.ps1` | main wrapper |
| main wrapper 파라미터 | `RunDate` · `Region` · `Environment` · `StartStep` · `EndStep` · `DryRun` · `AllowPaperOrderExecute` |
| main wrapper 안전 기능 | Step 12 `PAPER_ORDER_GATE` 중앙 차단 |
| main wrapper 출력 | 실행 summary 생성 |
| `daily-aws-paper.config.ps1` | region · cluster · instance ID · subnet · SG · Task Definition · Job Definition · Log Group · output path 관리 |
| `daily-aws-paper.functions.ps1` | Step registry와 공통 실행 함수 |
| SSM 공통 | Linux `AWS-RunShellScript` |
| SSM 공통 | Windows `AWS-RunPowerShellScript` |
| ECS 공통 | RunTask + UTF-8 No BOM JSON + `file://` overrides |
| Batch 공통 | SubmitJob |
| 로그 | CloudWatch · SSM stdout · stderr 저장 |
| 판정 | 성공 · 실패 · blocker 판정 |
| PowerShell | UTF-8 출력 보정 |
| Step 파일 | `step-01-connector-balance.ps1` ~ `step-17-balance-refresh.ps1` |
| bundled wrapper | 본 일자 미생성 |
| bundled wrapper 상태 | 후속 선택 작업 |

### 검증 결과

| 항목 | 값 |
| --- | --- |
| 전체 DryRun | Step 1~17 모두 `FOUND` |
| ECS RunTask 제출 | 0건 |
| Batch SubmitJob 제출 | 0건 |
| SSM command 제출 | 0건 |
| Step 1 단독 검증 | 통과 |
| Step 1 실행 | MarketConnector EC2 SSM RunCommand |
| 실행 스크립트 | `connector_balance.py` |
| SSM 상태 | Success |
| ResponseCode | 0 |
| DB 결과 | `connector_balance_snapshot` 저장 |
| 보유 종목 | 0건 |
| Step 12 단독 검증 | 중앙 gate 차단 통과 |
| Step 12 기본값 | `PaperOrder=False` |
| Step 12 내부 gate | 이중 차단 |
| 승인 옵션 미지정 | SSM command 제출 0건 |
| 실제 KIS 주문 | 0건 |
| parser validation | 20개 파일 모두 OK |
| parser error | 0건 |
| safety grep | 통과 |
| Step 10·11 `--execute` | strategy execution 내부 상태 생성·갱신 |
| Step 12 `--execute` | KIS paper 주문 제출 가능 |
| 의도하지 않은 주문 command | 0건 |

### 결정과 Risk

| 항목 | 값 |
| --- | --- |
| 신규 Decision | `OD-MS-023` |
| Decision 내용 | 운영자 로컬 PowerShell 기준 · 분리 파일 구조 · Step 12 명시 승인 |
| Decision 상태 | 🟡 잠정 |
| 신규 Risk | `R-AUTO-019` |
| Risk | 의도하지 않은 `-AllowPaperOrderExecute` 사용 |
| mitigation | 중앙 gate + Step 12 내부 gate |
| mitigation | 운영자 직접 옵션 명시 |
| 기본값 | OFF |
| Risk 상태 | Mitigated |
| 보강 Risk | `R-AUTO-016` |
| 보강 내용 | Windows KRX worker 미기동 시 Step 2 자동 skip |
| 사전 점검 | EC2 state · SSM Online |
| 보강 Risk | `R-AUTO-002` |
| 보강 내용 | `aws-paper`만 허용 · aws-live 분기 없음 |
| 보강 Risk | `R-DOCS-001` |
| 보강 내용 | summary · overrides JSON · stdout · stderr에 secret 평문 0건 |

### 문서 반영

| 파일 | 변경 |
| --- | --- |
| `.kiro/WORKLOG.md` | 본 세션 추가 |
| `.kiro/CHANGELOG.md` | 2026-06-17 세 번째 섹션 |
| `.kiro/README.md` | PowerShell wrapper 실행 개요 |
| `operator-decisions.md` | `OD-MS-023` 추가 |
| `followups-overview.md` | 2026-06-17 세 번째 후속 메모 |
| `risk-register.md` | `R-AUTO-019` 추가 |
| `risk-register.md` | R-AUTO-002 · R-AUTO-016 · R-DOCS-001 보강 |
| `03 operation-notes.md` | 2026-06-17 §1~§6 |
| 다른 common 문서 | 변경 없음 |
| 04·06·08·09 operation notes | 변경 없음 |

### 실행·보안 경계

| 항목 | 값 |
| --- | --- |
| AWS 작업 주체 | 운영자 |
| Kiro 역할 | 문서·절차·검증 항목 정리 |
| AWS CLI · boto3 | 0건 |
| AWS 리소스 생성·수정·삭제 | 0건 |
| broker·KIS 직접 호출 | 0건 |
| 신규 BUY·SELL·취소·정정 | 0건 |
| fill·position 자동 재시도 | 0건 |
| aws-live | 0건 |
| 민감정보 평문 | 0건 |
| placeholder | `[REDACTED]` 계열 |
| wrapper 실행 위치 | 운영자 로컬 Windows PC |
| EC2·ECS·Batch 내부 실행 | 대상 아님 |

### 다음 작업

| 후속 작업 | 값 |
| --- | --- |
| bundled wrapper | 생성 여부 결정 |
| safe subset | Step 1~7 실제 검증 |
| execution-side subset | Step 8~11 별도 검증 |
| Step 12 | 운영자 확인 후 명시 승인 |
| Step 13~17 | 주문·체결 결과 확인 후 실행 |
| KRX worker stopped | Step 2 skip 검증 |
| orchestration | View 또는 Step Functions 검토 |
| 정기 실행 | EventBridge Scheduler |
| live | 10 spec cutover 후속 |

## 2026-06-17 — Daily AWS 17-step E2E 완료

### 전체 결과

| 항목 | 값 |
| --- | --- |
| 전체 상태 | 🟢 완료 |
| 환경 | `aws-paper` |
| 전체 범위 | Step 1~17 |
| 실제 broker 호출 | KIS paper BUY 4건 |
| SELL | 0건 |
| 취소·정정 | 0건 |
| aws-live | 0건 |

### Step 1~17 결과

| Step | 결과 |
| --- | --- |
| 1 CONNECTOR_BALANCE | 완료 · snapshot 기준일 2026-06-17 |
| 2 INTEREST_CRAWLER | 완료 · non-GUI ECS + KRX Windows hybrid |
| 3 PREPROCESSOR | 완료 · feature date 2026-06-16 |
| 4 BACKTEST_RESEARCH | AWS Batch SUCCEEDED · Sharpe 2.68 |
| 5 BACKTEST_REPORT | AWS Batch SUCCEEDED · S3 report 4개 |
| 6 DAILY_BUY_SIGNAL | READY 4건 |
| 7 DAILY_POSITION_SIGNAL | positions 0 · 정상 skip |
| 8 DAILY_BUY_EXECUTION | Plan 92 · BUY READY 4건 |
| 9 DAILY_SELL_EXECUTION | 정상 skip |
| 10 DAILY_AUTO_SELL | 정상 skip |
| 11 DAILY_AUTO_BUY | 4건 READY → REQUESTED |
| 12 ORDER_EXECUTE | KIS paper BUY 4건 제출 |
| 13 ORDER_CHECK | 단건 조회로 FILLED 동기화 |
| 14 SYNC_SELL_FILL | 정상 skip |
| 15 SYNC_BUY_FILL | 4건 FILLED |
| 16 SYNC_BUY_POSITION | Position 4건 OPEN |
| 17 BALANCE_REFRESH | 최신 포지션 스냅샷 4종목 |

### 주요 데이터

| 항목 | 값 |
| --- | --- |
| BUY 종목코드 | 282330 · 004990 · 003490 · 088350 |
| 총수량 | 378 |
| 목표금액 | 6,908,189.40 |
| Execution Order | ID 26~29 |
| Connector Order Request | ID 34~37 |
| Position State | ID 6~9 |
| 최종 수량 | 17 · 65 · 52 · 244 |

### 결정과 Risk

| 항목 | 값 |
| --- | --- |
| 신규 Decision | 0건 |
| 신규 Risk | 2건 |
| 보강 Decision | OD-MS-016 |
| 보강 Decision | OD-MS-021 |
| 보강 Decision | OD-DB-008 |
| 신규 Risk | R-AUTO-018 |
| 내용 | aggregate summary 오매핑 위험 |
| mitigation | active 후보 1건일 때만 fallback |
| 상태 | Mitigated |
| 신규 Risk | R-DATA-011 |
| 내용 | legacy schema 권한·search_path 누락 |
| mitigation | GRANT·search_path 보정 |
| 상태 | Mitigated |
| 추가 보강 | R-DOCS-001 · R-DATA-005 |
| 주문 계열 보강 | R-AUTO-009 · R-AUTO-010 · R-AUTO-011 |
| Research Risk | R-AUTO-015 Mitigated 유지 |

### 실행·보안 경계

| 항목 | 값 |
| --- | --- |
| 실제 운영 작업 | 운영자 직접 수행 |
| Kiro 실행 | 0건 |
| secret 평문 | 0건 |
| AWS CLI · boto3 | 0건 |
| AWS 리소스 변경 | Kiro 0건 |
| broker 호출 | paper BUY 4건 한정 |
| SELL·취소·정정 | 0건 |
| aws-live | 0건 |

### 다음 작업

| 후속 작업 | 값 |
| --- | --- |
| source_daily_signal_id | null 보정 검토 |
| summary fallback | 테스트 케이스 추가 |
| legacy 권한 | 정식 문서화 |
| balance 최신성 SQL | 의미 구분 보완 |
| Windows cp949 | 출력 실패 회피 패턴 |
| orchestration | View 또는 Step Functions |
| 정기 실행 | EventBridge Scheduler |
| package | Strategy Common 정식화 |
| live | 10 spec cutover |

## 2026-06-17 — MarketConnector 조회성 dry-run 재검증

### 검증 결과

| 항목 | 값 |
| --- | --- |
| CONNECTOR_BALANCE | 재검증 성공 |
| 1차 실패 원인 | JSON SecretString 전체 export |
| 보정 | 내부 key value 추출 |
| env 호환 | APP_* + KIS_* alias |
| 최신 snapshot date | 2026-06-17 |
| source API | `inquire-balance` |
| 보유 종목 | 0건 |
| CONNECTOR_ORDER_CHECK | 재검증 성공 |
| API | `inquire-daily-ccld` |
| response status | 200 |
| response code | 0 |
| is_success | true |
| 신규 주문·체결 | 0건 |
| PowerShell 변수 소실 | 운영자 측 경미 이슈 |
| AWS 영향 | 없음 |

### 결정·Risk

| 항목 | 값 |
| --- | --- |
| 신규 Decision | 0건 |
| 신규 Risk | 0건 |
| 보강 Decision | OD-SEC-006 |
| 보강 Decision | OD-MS-001 |
| 보강 Decision | OD-MS-009 |
| 안전 정합 | OD-SAFE-001~004 · OD-MS-021 |
| 보강 Risk | R-DOCS-001 |
| secret value 출력 | 0건 |

### 다음 작업

| 후속 작업 | 값 |
| --- | --- |
| safe subset | Step 2~7 재개 |
| execution 구간 | Step 8~17 별도 판단 |
| env mapping | startup script 반영 |
| inject env | 운영 스크립트 승격 검토 |
| APP_*·KIS_* | 단일화 결정 |
| View 매핑 | 05 spec |
| Step Functions | 04 spec |
| live | 10 spec |

## 2026-06-16 — Crawler 데이터 미수집 해결과 KRX EC2 자동화

### 원인과 조치

| 항목 | 값 |
| --- | --- |
| 원인 | rev6가 Selenium smoke command |
| 원인 | 원본 daily script에 KRX GUI 포함 |
| 원인 | non-GUI orchestration 부재 |
| 신규 파일 | `interest_crawler_daily_nongui.py` |
| 이미지 | Docker rebuild + ECR push |
| Task Definition | `portfolio-paper-interest-crawler:7` |
| RunTask | ExitCode 0 |
| 실행 시간 | 약 9분 51초 |
| non-GUI step | 모두 SUCCESS |

### raw 최신성

| 항목 | 값 |
| --- | --- |
| price | 2026-06-15 |
| investorflow | 2026-06-15 |
| marketbreadth | 2026-06-15 |
| commodity | 2026-06-15 |
| foreignindex | 2026-06-15 |
| macro | 2026-06-15 |
| news | 2026-06-16 |
| agency | 2026-06-16 |

### Windows KRX worker

| 항목 | 값 |
| --- | --- |
| bootstrap | Autologon |
| session | Administrator Active |
| trigger | SSM RunCommand → Scheduled Task |
| 수집 | KRX login · program · shortsell |
| 적재일 | 2026-06-15 |
| stop 요청 | 완료 |

### 결정과 Risk

| 항목 | 값 |
| --- | --- |
| 보강 Decision | OD-MS-011 · OD-MS-015 |
| 신규 Decision | OD-MS-022 |
| 내용 | Autologon + interactive session + Scheduled Task + SSM |
| 상태 | 🟡 잠정 |
| 신규 Risk | R-SEC-009 |
| 신규 Risk | R-AUTO-016 |
| 신규 Risk | R-AUTO-017 |
| 보강 Risk | R-AUTO-008 |
| 보강 Risk | R-DATA-009 · R-DATA-010 |
| BUY·SELL 실행 | 0건 |
| aws-live | 0건 |

### 다음 작업

| 후속 작업 | 값 |
| --- | --- |
| Preprocessor | raw 기반 재실행 |
| feature 검증 | max date · updated_at |
| E2E | Research 이후 safe subset |
| foreignindex NULL | 후속 점검 |
| Scheduler | 정기 trigger |
| orchestration | Step Functions hybrid |
| 로그 수집 | CloudWatch Agent·SSM |
| worker 종료 | stop 절차 |
| Chrome | 잔존 프로세스 정리 |
| View | 후속 구현 |

## 2026-06-16 — Backend AWS E2E dry-run safe subset 재개

### 실행 결과

| 항목 | 값 |
| --- | --- |
| BACKTEST_RESEARCH | AWS Batch SUCCEEDED |
| run_id | `439d78e7-41fd-4bb7-b455-18564ddff758` |
| end date | 2026-06-15 |
| total return | 4.66534417 |
| MDD | -0.08941942 |
| Sharpe | 2.68071466 |
| trade count | 310 |
| BACKTEST_REPORT | rev3 최종 성공 |
| report 방식 | `python -m port_strategy_research.aws_batch_backtest_report_wrapper` |
| Job Definition | `portfolio-paper-strategy-report:3` |
| S3 객체 | 4건 |
| DAILY_BUY_SIGNAL | READY 4건 |
| signal date | 2026-06-16 |
| DAILY_POSITION_SIGNAL | positions 0 · 정상 skip |
| 신규 position decision | 0건 |

### safe subset 상태

| Step | 결과 |
| --- | --- |
| 1 CONNECTOR_BALANCE | 완료 |
| 2 INTEREST_CRAWLER | hybrid 완료 |
| 3 PREPROCESSOR | 재실행 가능 |
| 4 BACKTEST_RESEARCH | 완료 |
| 5 BACKTEST_REPORT | 완료 |
| 6 DAILY_BUY_SIGNAL | 완료 |
| 7 DAILY_POSITION_SIGNAL | 정상 skip |
| 8~17 | 미진행 |

### BACKTEST_REPORT 교정 이력

| 항목 | 값 |
| --- | --- |
| revision 1 | local-only |
| revision 2 | wrapper 경로 부재 실패 |
| revision 3 | module 호출 방식 성공 |
| 최종 image | `paper-20260615-report-s3` |
| output dir | `/tmp/portfolio-reports` |
| S3 prefix | `strategy-research/reports` |
| IAM | PutObject Resource prefix 한정 |
| public read | 0건 |
| wildcard | 0건 |

### 결정과 Risk

| 항목 | 값 |
| --- | --- |
| 신규 Decision | 0건 |
| 신규 Risk | 0건 |
| 보강 Decision | OD-MS-008 |
| 보강 Decision | OD-MS-013 |
| 보강 Decision | OD-MS-019 |
| 보강 Decision | OD-MS-021 |
| 보강 Risk | R-AUTO-015 |
| Risk 상태 | Mitigated 유지 |
| broker·KIS 주문 | 0건 |
| aws-live | 0건 |

### 다음 작업

| 후속 작업 | 값 |
| --- | --- |
| View 매핑 | 05 spec |
| Step Functions | 04 spec |
| Scheduler | EventBridge |
| 주문·체결 구간 | 별도 운영 승인 |
| execution dry-run | 가능 여부 판단 |
| S3 lifecycle | 결정 필요 |
| KMS | 암호화 정책 결정 |
| heavy backtest | 운영 절차 명문화 |
| live | 10 spec cutover |


## 2026-06-15 (Backend AWS E2E dry-run 1차 + Interest Crawler 상태 재판정)

- 🟢 **작업 요약** (같은 일자 두 번째 세션 · Strategy Research AWS Batch 골격 + full / report + S3 업로드 후속)

| 항목 | 값 |
| --- | --- |
| (a) 사전 점검 | AWS 계정 · region · EC2 · ECS · AWS Batch |
| (b) `CONNECTOR_BALANCE` 1차 실행 | MC EC2 기반 |
| (c) KRX worker 재실행 · KRX raw 최신일 점검 | Windows EC2 worker |
| (d) non-GUI raw 최신일 SQL 점검 | 완료 |
| (e) preprocessor ECS RunTask 1회 단발 | + DB `updated_at` 갱신 확인 |
| 의미 | View 진입 전 backend AWS 측 dry-run 을 17단계 순서로 1차 점검 · stale raw data 이슈 조기 식별 |
| 참조 | 08 spec operation-notes 2026-06-15 |

- 🟢 **사전 점검 (완료)**

| 항목 | 값 |
| --- | --- |
| region · account | `ap-northeast-2` · 확인 |
| 로컬 AWS CLI | 기본 실행 가능 |
| MarketConnector EC2 · Windows crawler worker | running |
| SSM managed instance | Online |
| `portfolio-paper-cluster` | ACTIVE |
| Task Definition 확인 | preprocessor · decision buy-signal · decision position-signal |
| Strategy Research | CE · JQ · JD active revision · BACKTEST_REPORT S3 upload 포함 latest = `portfolio-paper-strategy-research:3` · ENABLED · VALID · Healthy |
| account-id · 실제 ARN 평문 기록 | 0건 |

- 🟢 **`CONNECTOR_BALANCE` (Backend E2E dry-run 1번) 완료**

| 항목 | 값 |
| --- | --- |
| 실행 | MarketConnector EC2 · `/home/ec2-user/apps/port-marketconnector` · venv python · SSM RunCommand |
| 1차 실패 원인 | KIS Secrets 는 JSON 형태 · SecretString 원문 그대로 export 한 실수 |
| 정정 | Secret JSON key → 환경변수 mapping (`APP_KEY` → `KIS_APP_KEY` · `APP_SECRET` → `KIS_APP_SECRET` · `PAPER_ACNT` → `KIS_PAPER_ACNT` · `ACNT_PRDT_CD` → `KIS_ACNT_PRDT_CD`) |
| 결과 | 기존 `access_token.txt` 백업 · 신규 token 발급 성공 · KIS balance API status 200 · 모의투자 잔고 조회 성공 · `connector.connector_balance_snapshot` 저장 · `connector.connector_position_snapshot` 보유종목 0건 정상 · legacy holdings 0건 |
| secret · KIS · 계좌 · token 평문 기록 | 0건 |

- 🟡 **`INTEREST_CRAWLER` (Backend E2E dry-run 2번) 부분 완료 / follow-up 승격**

| 항목 | 값 |
| --- | --- |
| KRX GUI worker | 운영 가능 상태 1차 완성 · `KRX already logged in` · `KRX Login Ready` · `[Collected Date] None` idempotent 정상 완료 |
| KRX raw 최신일 | `interest_program_raw` 2026-06-12 · `interest_shortsell_raw` 2026-06-12 (직전 거래일까지 정상) |
| non-GUI raw 7종 최신일 | `interest_agency_raw` 2026-06-11 · `interest_news_raw` 2026-06-11 · `interest_commodity_raw` 2026-06-08 · `interest_foreignindex_raw` 2026-06-08 · `interest_investorflow_raw` 2026-06-08 |
| non-GUI raw 7종 최신일 (계속) | `interest_marketbreadth_raw` 2026-06-08 · `interest_price_raw` 2026-06-08 |
| `interest_ticker_value_raw` | 2026-03-09 · dry-run 핵심 차단 요인 제외 · 별도 후속 |
| ECS · Fargate Selenium Chrome smoke | 2026-06-13 revision 6 통과 · 실제 daily raw 수집 운영 경로 · TD · command 분리 미완료 |
| 결론 (표현 보정) | "Interest Crawler 완성: 완료" → "Interest Crawler hybrid 1차 구현: 부분 완료" · "KRX GUI worker 는 운영 가능 상태로 1차 완성" · "ECS · Fargate crawler 는 smoke 검증 완료" · "non-GUI daily raw 수집 운영 경로와 raw 전체 최신성 검증은 후속" |

- 🟡 **`PREPROCESSOR` (Backend E2E dry-run 3번) 실행 완료 (데이터 최신성 제약)**

| 항목 | 값 |
| --- | --- |
| cluster · TD | `portfolio-paper-cluster` · `portfolio-paper-interest-preprocessor:1` |
| Runtime | FARGATE · awsvpc · public-a + public-b · `assignPublicIp=ENABLED` · SG `sgroup-preprocessor-tasks` |
| 종료 상태 | lastStatus `STOPPED` · desiredStatus `STOPPED` · stopCode `EssentialContainerExited` · container `interest-preprocessor` · exitCode 0 · 약 3분 43초 |
| CloudWatch Logs | `/portfolio/paper/preprocessor` log stream 생성 확인 · 최신 stream `storedBytes=0` · 본문 검증 제한 · exitCode 0 기준 성공 |
| DB `updated_at` | 2026-06-15 11:03:55+00 (KST 20:03:55) 갱신 |
| 신규 2026-06-15 feature date | 0건 · 원인 = preprocessor 장애 아님 · raw 최신성 부족 (R-DATA-010 정합) |

- 🟢 **Secret / IAM 권한 분리 1차 실증**

| 항목 | 값 |
| --- | --- |
| MarketConnector EC2 role | `portfolio-paper-marketconnector-ec2-role` |
| 시도 | `/portfolio/paper/rds/preprocessor-app` `GetSecretValue` |
| 결과 | `AccessDeniedException` · 장애 아님 · OD-SEC-006 · OD-DB-008 정합 정상 동작 |
| preprocessor secret read 권한 추가 | 0건 |
| preprocessor DB 확인 경로 | preprocessor ECS Task 또는 운영자 로컬 SSM Port Forwarding (OD-NET-010 · OD-NET-011) 만 |
| IAM 변경 | 0건 |
| 운영자 로컬 PC tip | PowerShell 프롬프트 `>>` 가 명령 본문에 포함되면 `StreamAlreadyRedirected` 오류 |

- 🔵 **17단계 진행 상태**

| Step | 결과 |
| --- | --- |
| 1번 `CONNECTOR_BALANCE` | 🟢 **완료** |
| 2번 `INTEREST_CRAWLER` | 🟠 **부분 완료** |
| 3번 `PREPROCESSOR` | 실행 완료 · 데이터 최신성 제약 |
| 4~7번 (`BACKTEST_RESEARCH` · `BACKTEST_REPORT` · `DAILY_BUY_SIGNAL` · `DAILY_POSITION_SIGNAL`) | 미진행 |
| 8~17번 | 미진행 또는 dry-run skip 예정 |
| 실제 BUY · SELL · `--execute` 주문 전송 | 0건 |
| fill · position sync 자동 재시도 | 0건 |
| aws-live 작업 | 0건 |
| 순서 유지 | Research 가 Decision 보다 먼저 · Connector Balance 1번 · Balance Refresh 17번 |

- 🟡 **결정 락 (2026-06-15 두 번째 세션)**

| ID | 내용 |
| --- | --- |
| OD-MS-020 | Interest Crawler 상태 재판정 · hybrid 1차 구현 부분 완료 표현 통일 · 🟠 **잠정** |
| OD-MS-021 | Backend AWS E2E dry-run 17단계 순서 + 안전 기준 · 🟠 **잠정** |
| OD-SEC-006 | 본문 변경 없이 실증 메모 보강 · MarketConnector EC2 role 에 preprocessor secret read 미부여 정상 동작 · Status 잠정 유지 |
| OD-DB-008 | 본문 변경 없음 |
| R-DATA-009 신규 | smoke 검증을 daily 데이터 최신성 완료로 오해할 위험 · Status `Open` |
| R-DATA-010 신규 | raw 최신성 부족으로 downstream Research / Decision 결과가 stale data 기반이 될 위험 · Status `Open` |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `08/operation-notes.md` | 2026-06-15 §1~§8 누적 |
| `08/tasks.md` | §14 신규 (70~81) + Task Dependency Graph 보강 + 2026-06-15 이월 요약 |
| `08/design.md` | §14 보강 (표현 보정 · 본 일자 상태 · non-GUI 미도달 · raw 최신성 부족 영향) |
| `_common/operator-decisions.md` | OD-MS-020 · OD-MS-021 추가 + OD-SEC-006 실증 메모 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | 2026-06-15 두 번째 후속 메모 |
| `_common/risk-register.md` | R-DATA-009 · R-DATA-010 신규 |
| `.kiro/README.md` | 진행 상태 요약 섹션 신규 |
| `.kiro/CHANGELOG.md` | 2026-06-15 두 번째 섹션 |
| `.kiro/WORKLOG.md` | 본 파일 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| AWS · SSM · EC2 · ECS · Batch · IAM · Secrets Manager · RDS · GRANT 작업 | 모두 운영자 직접 수행 · Kiro 는 문서·절차·검증 정리만 |
| `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
| CloudWatch Logs · wrapper 로그 · SSM 응답 · docker build 로그 전문 평문 인용 | 0건 |
| broker / KIS 호출 | `CONNECTOR_BALANCE` 한정 조회성 (status 200 · 모의투자 잔고) |
| 신규 주문 · `--execute` | 0건 |
| RDS DDL | 0건 |
| RDS DML | `connector_balance_snapshot` · `connector_position_snapshot` insert + preprocessor pipeline 정상 흐름 한정 |
| 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 변경 | 0건 |
| 민감정보 (secret · KIS · 계좌 · token · RDS · account-id · IAM · image digest · instance-id · task ARN) 신규 기록 | 0건 · 모두 `[REDACTED]` 또는 placeholder |

  <details><summary>🔵 운영 식별자 요약</summary>

  - MarketConnector EC2 role 이름
  - preprocessor TD family·revision
  - cluster `portfolio-paper-cluster` · SG `sgroup-preprocessor-tasks`
  - Log Group `/portfolio/paper/preprocessor`
  - Secret `/portfolio/paper/rds/preprocessor-app` · `/portfolio/paper/kis/marketconnector`
  - preprocessor `updated_at` 2026-06-15 11:03:55+00
  - KRX raw 최신일 2026-06-12
  - non-GUI raw 7종 최신일 인벤토리

  </details>

- 다음 작업(내일) · non-GUI Interest Crawler 운영 실행 (ECS Fargate 용 non-GUI crawler command 분리 · `interest_crawler_daily.py` 의 KRX GUI 단계 제외 경로 정리) · raw 최신성 회복 (`interest_price_raw` · `interest_investorflow_raw` · `interest_marketbreadth_raw`
  `interest_commodity_raw` · `interest_foreignindex_raw` · `interest_news_raw`
  - `interest_agency_raw`) · preprocessor 재실행 + feature date 신규 생성 여부 확인 · Backend E2E dry-run 재개 (`BACKTEST_RESEARCH` → `BACKTEST_REPORT` → `DAILY_BUY_SIGNAL` → `DAILY_POSITION_SIGNAL` 순서 · Research 가 Decision 보다 먼저 · 주문 전송
    execution 계열은 안전 기준에 따라 skip 또는 dry-run 만 수행) · 모두 후속 분리. · 본 일자 dry-run 결과는 실패가 아니라 stale raw data 이슈를 조기에 발견한 검증 성공으로 기록.

## 2026-06-15 (Strategy Research AWS Batch 실행 골격 + full / report 검증 + S3 업로드 보강)

- 🟢 **작업 요약** (2026-06-13 Strategy Research Batch image 1차 준비 후속)

| 항목 | 값 |
| --- | --- |
| (a) AWS Batch 실행 골격 신규 | Compute Environment · Job Queue · Job Definition revision 1 · CloudWatch Log Group · Secrets Manager `/portfolio/paper/rds/research-app` · Execution Role + Job Role |
| (b) 1차 검증 | py_compile smoke + DB smoke SubmitJob |
| (c) full 실행 · report 검증 | BACKTEST_RESEARCH full + BACKTEST_REPORT 4개 리포트 생성 |
| (d) S3 업로드 보강 | Job Definition revision 3 등록 + S3 4개 객체 존재 확인 |
| 참조 | 09 spec operation-notes 2026-06-15 3개 섹션 |
| Strategy Research 최종 컴퓨트 1순위 | AWS Batch 유지 (OD-MS-008 본문 변경 없음) |

- 🟢 **AWS Batch 골격 리소스**

| 항목 | 값 |
| --- | --- |
| Compute Environment | `portfolio-paper-strategy-research-ce` · MANAGED · FARGATE · maxvCpus 4 · state ENABLED · status VALID |
| Job Queue | `portfolio-paper-strategy-research-queue` · priority 10 · state ENABLED · status VALID |
| Job Definition rev1 | `portfolio-paper-strategy-research:1` · image `paper-latest` · vCPU 1 · memory 2048 · timeout 600초 · FARGATE · assignPublicIp ENABLED · 기본 command 안전한 `py_compile` smoke |
| CloudWatch Log Group | `/portfolio/paper/strategy-research` · retention 14일 |
| Secrets Manager | `/portfolio/paper/rds/research-app` · JSON multi-key `host`/`port`/`dbname`/`username`/`password` · secret value 노출 0건 · key presence 검증 완료 |
| Execution Role | `portfolio-paper-research-batch-execution-role` · `AmazonECSTaskExecutionRolePolicy` + research-app secret read inline · ARN 한정 · wildcard 0건 |
| Job Role | `portfolio-paper-research-job-role` · 최초 smoke 단계 최소 권한 · §11 단계에서 S3 PutObject 권한 추가 |

- 🟢 **smoke SubmitJob 2건 모두 SUCCEEDED / exitCode 0**

| 항목 | 값 |
| --- | --- |
| py_compile smoke | `smoke-strategy-research-import-20260615` · jobId `81ec3581-0204-43ea-8238-a2a6d22f3f28` |
| DB smoke | `smoke-strategy-research-db-20260615` · jobId `5399aa10-0fdd-466b-8079-236d3b7e7e37` · `db smoke ok` · `research_app` · `portfolio` · schema `research` |
| image pull · secret injection · log delivery 오류 | 0건 |
| secret value 노출 | 0건 |
| `smoke` job name prefix 정책 | 1차 적용 (R-AUTO-015 mitigation 정합) |

- 🟢 **Strategy Common 1차 정합성 확인**

| 항목 | 값 |
| --- | --- |
| 별도 컴퓨트 | 없음 (OD-MS-014 정합) |
| vendoring | Decision · Execution · Research image 유지 |
| py_compile 통과 대상 | `port_strategy_common` 주요 모듈 15종 (`common_*.py` · `config.py` · `utils.py` · `__init__.py`) + Research adapter 3개 · 상세 목록은 [09 spec operation-notes 2026-06-15](specs/09-strategy-research-batch/operation-notes.md) |
| 각 MS common import smoke | 통과 |
| 표현 | "Strategy Common 1차 정합성 확인 완료 / 정식 package 관리는 후속" |
| 후속 분리 | wheel · sdist · CodeArtifact · 07 CI/CD package·version 관리 · Research adapter common 정식 이동 (R-DATA-008 mitigation 정합) |

- 🟢 **BACKTEST_RESEARCH full 실행 검증**

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 **SUCCEEDED** · exitCode 0 |
| run_id | `a39b0b0c-cfe9-474e-8a4a-4ddb33f09567` |
| total_return · mdd · sharpe · trade_count | `4.55930879` · `-0.08941942` · `2.65561307` · `308` |
| extended analysis | 내부 수행 포함 |
| RDS DDL · broker · KIS 호출 · Batch automatic retry | 0건 (OD-SAFE-004 정합) |

- 🟡 **BACKTEST_REPORT 본 phase 검증 · OD-MS-019 신규 결정 락**

| 항목 | 값 |
| --- | --- |
| 결과 | 최신 `run_id` 기준 4개 리포트 생성 성공 · `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` 적용 · 🟢 **SUCCEEDED** · exitCode 0 |
| OD-MS-019 신규 | Research AWS Batch 포팅 대상 = BACKTEST_RESEARCH + BACKTEST_REPORT 2종 · `run_extended_analysis.py` = BACKTEST_RESEARCH 내부 이미 수행 · 별도 AWS Batch 포팅 대상 제외 + 수동 보조 도구 분류 |
| OD-MS-019 신규 (계속) | report artifact 보존 = S3 prefix `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/` · 🟠 **잠정** |
| `block_watch_*` · `block_exception_buy_*` 4종 | AWS Batch 포팅 대상 제외 + heavy 분류 후속 |

- 🟢 **BACKTEST_REPORT S3 업로드 보강**

| 항목 | 값 |
| --- | --- |
| bucket | `portfolio-paper-migration-yukiever` 재사용 |
| Job Role 권한 추가 | `s3:PutObject` · Resource `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` · public read 0건 · wildcard 0건 |
| requirements.txt | `boto3` 추가 · Docker 내부 import smoke 성공 |
| report wrapper 옵션 | `REPORT_S3_BUCKET` 미설정 시 skip · `REPORT_S3_PREFIX` 기본값 `strategy-research/reports` · `AWS_BATCH_JOB_ID` 기준 하위 경로 분리 · `REPORT_OUTPUT_DIR` 기본값 `/tmp/portfolio-reports` 유지 |
| Docker rebuild + ECR push | `paper-20260615-report-s3` · image digest sha256 placeholder · size 약 106MB · pushedAt 2026-06-15T17:00:10+09:00 |
| Job Definition rev3 | image `paper-20260615-report-s3` · TaskRole `portfolio-paper-research-job-role` |

- 🟢 **S3 업로드 SubmitJob 결과**

| 항목 | 값 |
| --- | --- |
| jobName · jobId | `strategy-research-backtest-report-s3-20260615` · `112f5fe4-02f3-4614-a88c-60a9842e1447` |
| 상태 | 🟢 **SUCCEEDED** · exitCode 0 |
| logStreamName | `strategy-research/default/b18e548d46764cd791028088e1d32a6d` |
| S3 객체 4건 | `strategy-research/reports/20260615/112f5fe4-.../01_요약_리포트_20260615_v1.txt` · `02_일자별_매매_리포트_20260615_v1.txt` · `03_거래_상세_리포트_20260615_v1.txt` · `04_추천_리포트_20260615_v1.txt` |
| private 유지 · public read 부여 | 유지 · 0건 |

- 🟡 **결정 락 (2026-06-15) · 리스크 갱신**

| ID | 값 |
| --- | --- |
| OD-MS-019 | 신규 · 🟠 **잠정** |
| OD-MS-008 · OD-MS-018 | 본문 변경 없이 실증 메모 보강 · Status 유지 |
| R-AUTO-015 | mitigation 보강 · Status `Open` → 🟢 **Mitigated** 승격 (SFN orchestration 전까지 완전 Closed 아닌 Mitigated 적절) |
| R-DATA-008 | detection 보강 (common 변경 시 Research py_compile / import smoke / sample 비교 필수) |
| R-COST-003 신규 | Research S3 report 누적 비용 · lifecycle 미설정 · mitigation = OD-MS-019 prefix 한정 + Job Role Resource 한정 + public read 0건 + S3 lifecycle 후속 · Status 🟠 **Open** |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `09/operation-notes.md` | 2026-06-15 3개 섹션 누적 |
| `_common/operator-decisions.md` | OD-MS-019 추가 + OD-MS-008 · OD-MS-018 실증 메모 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | 2026-06-15 09 spec 후속 메모 |
| `_common/risk-register.md` | R-AUTO-015 보강 + Status `Mitigated` · R-DATA-008 detection · R-COST-003 신규 |
| `_common/ms-aws-service-decision-matrix.md` | 4.8 · 5장 · 6.1 보강 매트릭스의 Strategy Research 행 실증 메모 · 권고 변경 0건 |
| `.kiro/CHANGELOG.md` | 2026-06-15 섹션 |
| `.kiro/WORKLOG.md` | 본 파일 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| AWS · Docker · ECR · IAM · Secrets Manager · CloudWatch · Batch · S3 · RDS 작업 | 모두 운영자 직접 수행 · Kiro 는 문서·절차·검증 정리만 |
| `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
| CloudWatch Logs 본문 secret value 평문 출력 | 0건 |
| RDS DDL / DML | 0건 |
| broker · KIS · 주문 · 체결 · Daily Batch entrypoint 호출 | 0건 |
| live 자동 batch / report 생성 | 후속 승인 전까지 <span style="color:#D1242F">**금지**</span> (OD-SAFE-002 · OD-SAFE-003) |
| 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 변경 | 0건 |
| 운영자 직접 작성 `port_strategy_research` requirements.txt · report wrapper 변경분 본문 인용 | 0건 (09 spec operation-notes 사실 기록) |
| 민감정보 (secret · RDS · KIS · 계좌 · token · account-id · secret ARN · IAM Role ARN · image digest full sha256 · IAM access key) 신규 기록 | 0건 · 모두 `[REDACTED]` 또는 placeholder |

  <details><summary>🔵 운영 식별자 요약</summary>

  - image tag `paper-latest` · `paper-20260615-report-s3` · size 약 106MB · pushedAt 2026-06-15T17:00:10+09:00
  - Compute Environment · Job Queue · Job Definition family·revision
  - Log Group `/portfolio/paper/strategy-research`
  - Secret · Role 이름
  - jobName · jobId · logStreamName
  - S3 bucket `portfolio-paper-migration-yukiever` · prefix `strategy-research/reports/20260615/112f5fe4-.../`
  - 파일명 4종
  - metric 값 `run_id a39b0b0c-...` · `total_return` · `mdd` · `sharpe` · `trade_count`

  </details>

- 다음 작업 — View Daily Batch 의 `BACKTEST_RESEARCH` / `BACKTEST_REPORT` step 을 ProcessBuilder 직접 실행 → AWS Batch SubmitJob 호출로 매핑 (05 / 04 spec 후속) / Step Functions state machine (BACKTEST_RESEARCH → BACKTEST_REPORT 순서 강제 + EventBridge Scheduler 정기 트리거
  OD-SAFE-004 정합 · 04 spec 후속) / `block_watch_*` `block_exception_buy_*` 수동 보조 도구 운영 절차 (09 후속) / Research 내부 adapter 3개 → `port_strategy_common` 정식 adapter 이동 (R-DATA-008 OD-MS-005 OD-MS-014 OD-MS-018 후속) / `port_strategy_common` 정식 package · version 관리 (07
  Strategy Common) / CI/CD OIDC build push 자동화 (07 spec) / aws-live cutover (10 spec) / S3 lifecycle 정책 + KMS encryption (R-COST-003 · 06 후속) — 모두 후속 분리.

## 2026-06-13 (Strategy Research Batch image 1차 준비)

- 🟢 **작업 요약** (같은 일자 1·2·3·4·5번 세션 후속)

| 항목 | 값 |
| --- | --- |
| 성격 | 운영자 직접 수행 · `port_strategy_research` AWS Batch 용 Docker / ECR 1차 준비 |
| 참조 | 09 spec operation-notes 2026-06-13 Strategy Research Batch image 1차 준비 |
| Strategy Research 최종 컴퓨트 1순위 | AWS Batch 유지 (OD-MS-008 본문 변경 없음) |
| 본 일자 범위 | AWS Batch 용 container image 준비 · ECR push 1차 검증 |
| 후속 분리 | AWS Batch Compute Environment · Job Queue · Job Definition · SubmitJob |

- 🟢 **AWS 실행 구조 확인**

| 항목 | 값 |
| --- | --- |
| As-Is entrypoint 7개 | `backtest_research_run` · `backtest_report_run` · `run_extended_analysis` · `block_watch_analysis_run` · `block_watch_backtest_run` · `block_exception_buy_backtest_run` · `block_exception_buy_engine_run` |
| heavy vs light 분리 | heavy = full backtest · 장시간 research · report · extended · block 계열 · light = py_compile · import smoke · Docker MS 경계 확인 |
| 외부 dependency | `psycopg2-binary` · `pandas` · `numpy` |
| 내부 dependency | `port_strategy_research` + `port_strategy_common` |
| 환경변수 · search_path | `research_app` · `INTEREST_DB_PASSWORD` 등 · search_path `research, preprocessor, interest, reference, legacy, public` |

- 🟢 **Research → Decision 직접 런타임 의존 제거**

| 항목 | 값 |
| --- | --- |
| 신규 adapter 3개 | `research_backtest_market_adapter.py` · `research_backtest_filter_adapter.py` · `research_backtest_sizing_adapter.py` |
| import 변경 3개 파일 | `backtest_engine.py` · `backtest_buy_logic.py` · `block_exception_buy_engine_run.py` |
| 변경 전 → 변경 후 | `from port_strategy_decision.backtest_market import evaluate_market` 등 → `from port_strategy_research.research_backtest_market_adapter import evaluate_market` |
| 방법 | Python patch script · `encoding="utf-8-sig"` 읽기 · `encoding="utf-8"` 저장 · 한글 preview 정상 |
| 결과 | py_compile 성공 · `from port_strategy_decision` / `import port_strategy_decision` 잔존 0건 |

- 🟢 **Batch 용 Docker / ECR**

| 항목 | 값 |
| --- | --- |
| Dockerfile · requirements.txt | 운영자 직접 신규 |
| Docker build context | `C:\Workspaces` |
| image 포함 · 제외 | 포함 = `port_strategy_research` + `port_strategy_common` · 제외 = `port_strategy_decision` |
| 기본 CMD | 안전한 `py_compile` 계열 |
| 로컬 build | `portfolio-strategy-research:paper-20260613` |
| container smoke | `py_compile` + import smoke 통과 (`port_strategy_research.db_config` · `backtest_engine` · `backtest_buy_logic` · `block_exception_buy_engine_run`) |
| MS 경계 확인 | `/app/port_strategy_decision` 부재 · OD-MS-018 정합 1차 검증 |
| ECR repository | `portfolio-strategy-research` 신규 · push `paper-20260613` · `paper-latest` · image size 약 90MB |

- 🟡 **결정 락 (2026-06-13 여섯 번째 세션)**

| ID | 내용 |
| --- | --- |
| OD-MS-018 | Research Batch image dependency boundary = Research 내부 adapter 로 이관 · image 포함 = `port_strategy_research` + `port_strategy_common` · `port_strategy_decision` 미포함 · 장기 adapter → `port_strategy_common` 정식 package 이동 후보 유지 · 🟠 **잠정** |
| OD-MS-008 | 본문 변경 없음 · 1차 실증 메모만 보강 |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `09/operation-notes.md` | 신규 생성 · 2026-06-13 §1~§8 |
| `_common/operator-decisions.md` | OD-MS-018 추가 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | 2026-06-13 09 spec 후속 메모 |
| `_common/risk-register.md` | R-AUTO-015 · R-DATA-008 신규 |
| `.kiro/CHANGELOG.md` | 2026-06-13 여섯 번째 섹션 |
| `.kiro/WORKLOG.md` | 본 파일 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| AWS · Docker · ECR · IAM · Secrets Manager · RDS 작업 | 모두 운영자 직접 수행 · Kiro 는 문서·절차·검증 정리만 |
| AWS Batch SubmitJob | 0건 |
| CloudWatch Log Group · Secret · IAM Role 본 일자 생성 | 미생성 |
| full backtest · 장시간 research · report 생성 · extended analysis · block 계열 backtest | 0건 |
| RDS DDL/DML | 0건 |
| broker · KIS 호출 | 0건 |
| `secretsmanager:GetSecretValue` 실호출 | 0건 (secret 미생성) |
| 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog 변경 | 0건 |
| 운영자 작성 Dockerfile · requirements.txt · adapter 3개 · import 변경 3개 본문 인용 | 0건 (09 spec operation-notes 사실 기록) |
| 민감정보 (secret · RDS · KIS · 계좌 · token · account-id · ARN · image digest · IAM access key · Batch job ARN) 신규 기록 | 0건 · 모두 `[REDACTED]` 또는 placeholder |

  <details><summary>🔵 운영 식별자 요약</summary>

  - image tag `paper-20260613` · `paper-latest`
  - ECR repository `portfolio-strategy-research`
  - Dockerfile · adapter 파일 경로
  - image size 약 90MB

  </details>

- 다음 작업 · AWS Batch Compute Environment · Job Queue · Job Definition 1차 생성 · CloudWatch Log Group · Secrets Manager `/portfolio/paper/rds/research-app` · IAM Role (execution + job) · 짧은 no-op
  import smoke SubmitJob (`smoke` prefix + allowlist) · full backtest · report 생성은 별도 비용·시간·timeout·운영자 승인 기준 확정 후 · Research 내부 adapter 의 `port_strategy_common` 정식 package 이동 · Step Functions 통합 · CI/CD OIDC · 모두 후속 분리.

## 2026-06-13 (Strategy Execution ECS / Fargate 1차 포팅 검증)

- 🟢 **작업 요약** (같은 일자 1·2·3·4번 세션 후속)

| 항목 | 값 |
| --- | --- |
| 성격 | 운영자 직접 수행 · `port_strategy_execution` ECS / Fargate 본 phase 1차 포팅 검증 |
| 참조 | 04 spec operation-notes 2026-06-13 Strategy Execution ECS / Fargate 1차 포팅 검증 |
| broker · KIS 호출 · `connector_order_request` 생성 · `READY -> REQUESTED` 실제 전환 | 0건 |
| live 자동 주문 | 후속 승인 전까지 <span style="color:#D1242F">**금지**</span> (OD-SAFE-002 · OD-SAFE-003) |

- 🟢 **Docker / ECR**

| 항목 | 값 |
| --- | --- |
| Dockerfile · requirements.txt | 운영자 직접 신규 · Docker build context `C:\Workspaces` · 소스 vendoring · 1차 dependency `psycopg2-binary` · default CMD 안전한 `py_compile` |
| 로컬 build | `portfolio-strategy-execution:paper-20260613` · `paper-latest` |
| container smoke | `py_compile` + import smoke · `execution_config.py` `INTEREST_DB_PASSWORD` 요구 구조 확인 · dummy env 통과 · 실제 ECS 실행은 Secrets Manager 주입 |
| ECR push | repository `portfolio-strategy-execution` · `paper-20260613` · `paper-latest` |

- 🟢 **AWS 실행 리소스 · Network**

| 항목 | 값 |
| --- | --- |
| CloudWatch Log Group | `/portfolio/paper/strategy-execution` · retention 14일 |
| Secrets Manager | `/portfolio/paper/rds/execution-app` JSON multi-key |
| Execution Role | `portfolio-paper-ecs-task-execution-role` · execution-app secret read inline (ARN 한정 · wildcard 0건) |
| Task Role | `portfolio-paper-execution-task-role` 확인 |
| Network | cluster `portfolio-paper-cluster` · public-a + public-b · SG `sgroup-strategy-tasks` · RDS SG inbound + VPC Endpoint SG 443 source 허용 · `assignPublicIp=ENABLED` |

- 🟢 **Task Definition**

| 항목 | 값 |
| --- | --- |
| family · revision | `portfolio-paper-strategy-execution` revision 1 ACTIVE |
| 스펙 | awsvpc · Fargate · cpu 512 · memory 1024 · container `strategy-execution` · `INTEREST_DB_*` 5종 secrets injection · `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` |
| Strategy Decision 대비 | Decision 2개 분리 (OD-MS-013) 와 의도적으로 다른 패턴 · 7개 entrypoint 가 같은 image · role · secret · log group · cpu · memory 공유 · Task Definition 7종 분리는 과도 |
| command override 매핑 | 확정 (OD-MS-017) |

- 🟢 **RunTask 검증 8종 (모두 exitCode 0 / lastStatus STOPPED)**

| 케이스 | 결과 |
| --- | --- |
| (a) 기본 `py_compile` | 통과 |
| (b) `execution_sync_buy_fill.py` | NO_TARGET · submitted·synced·skipped 모두 0 |
| (c) `execution_sync_sell_fill.py` | 동상 |
| (d) `execution_sync_buy_position.py` | filled_buy_orders_without_position 0 |
| (e) `daily_buy_execution_run.py` | WEEKEND guard 차단 · NO_TARGET · `connector_order_request` 0 |
| (f) `daily_sell_execution_run.py` | 동상 |
| (g) `daily_auto_sell_execute_run.py --execute` | WEEKEND guard 차단 · `READY -> REQUESTED` 전환 0 · broker · KIS 호출 0 |
| (h) `daily_auto_buy_execute_run.py --execute` | 동상 |

- 🟡 **결정 락 (2026-06-13 다섯 번째 세션)**

| ID | 내용 |
| --- | --- |
| OD-MS-017 | port_strategy_execution TD 운영 = 단일 TD + command override · family revision 1 ACTIVE · 7개 entrypoint allowlist · 🟠 **잠정** |
| OD-MS-007 · OD-MS-009 · OD-SAFE-004 | 본문 변경 없이 1차 실증 메모 보강 |
| R-AUTO-001 | mitigation·detection 보강 |
| R-AUTO-014 신규 | 단일 TD + command override 오매핑 위험 · Status 🟠 **Open** |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `04/operation-notes.md` | 2026-06-13 Strategy Execution ECS / Fargate 섹션 §1~§11 |
| `_common/operator-decisions.md` | OD-MS-017 추가 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | 2026-06-13 Strategy Execution 1차 검증 후속 메모 |
| `_common/risk-register.md` | R-AUTO-001 보강 + R-AUTO-014 신규 |
| `.kiro/CHANGELOG.md` | 2026-06-13 다섯 번째 섹션 |
| `.kiro/WORKLOG.md` | 본 파일 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · RDS · GRANT 작업 | 모두 운영자 직접 수행 · Kiro 는 문서·절차·검증 정리만 |
| 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 변경 | 0건 |
| `secretsmanager:GetSecretValue` 결과값 평문 기록 | 0건 |
| CloudWatch Logs 본문 secret value 평문 출력 | 0건 |
| 민감정보 (secret · RDS · KIS · 계좌 · token · account-id · ARN · image digest · IAM access key · task ARN) 신규 기록 | 0건 · 모두 `[REDACTED]` 또는 placeholder |

  <details><summary>🔵 운영 식별자 요약</summary>

  - image tag `paper-20260613` · `paper-latest`
  - Task Definition family · revision · container 이름
  - SG · Log Group · Secret · Role 이름

  </details>

- 다음 작업 · Step Functions state machine (Strategy Decision 2개 + Strategy Execution 단일 + command override 매핑) · EventBridge Scheduler 연계 · MarketConnector executor 인계 end-to-end 검증
  평일 또는 안전 테스트 데이터로 `READY -> REQUESTED -> SUBMITTED` end-to-end 검증 · View Daily Batch ProcessBuilder → 관제 UI 격상 (장기) · `execution_app` schema 권한 매트릭스 정식 정리 (02 spec) · CI/CD OIDC (07 spec) · 모두 후속 분리.

## 2026-06-13 (Local-to-AWS Paper RDS SSM Port Forwarding 보강 — psql 18 client + pgAdmin4 접속 검증)

- 🟢 **작업 요약** (같은 일자 세 번째 세션 · SSM Port Forwarding 연결 검증 + Runbook 1차 본문 후속)

| 항목 | 값 |
| --- | --- |
| 성격 | 운영자 직접 · 동일 SSM tunnel 위에서 추가 client 2종 (로컬 PostgreSQL 18 `psql.exe` 직접 경로 + pgAdmin4) 으로 AWS Paper RDS 접속 1차 실증 |
| SSM tunnel | 같은 명령 · 같은 local port `15433` 재사용 |
| RDS Public 미허용 · OD-NET-009 · R-SEC-001 · R-NET-004 | 본문 변경 없음 |
| AWS · RDS · IAM · Secrets Manager · SSM 변경 | 0건 |
| 사용 도구 | read-only AWS API + SSM Port Forwarding 세션 + psql / pgAdmin4 SELECT 조회만 |

- 🟢 **로컬 PostgreSQL 18 psql client 직접 경로 검증**

| 항목 | 값 |
| --- | --- |
| 명령 | `& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -p 15433 -U portfolio_admin -d portfolio` |
| client · server | `18.1` · `18.4` |
| SSL connection | `TLSv1.3` |
| 출력 | `current_user=portfolio_admin` · `current_database=portfolio` · `inet_server_addr=10.0.20.165` · `inet_server_port=5432` |
| 정합성 | client major 18 · full 18.4 (R-DATA-003 · 03 spec design §5) |
| 일반 `psql` PATH 등록 | 미완료 · full path 직접 실행으로 작업 진행 |
| 비밀번호 처리 | PowerShell 세션의 `PGPASSWORD` 환경변수 사용 · `Remove-Item Env:PGPASSWORD` 로 제거 가능 · 평문 기록 0건 |

- 🟢 **로컬 PostgreSQL 설치 인벤토리**

| 항목 | 값 |
| --- | --- |
| 경로 | `C:\Program Files\PostgreSQL` |
| 버전 폴더 | `17` · `18` |
| 본 일자 검증 사용 | `18` |
| 추가 client 미설치 | 0건 |

- 🟢 **pgAdmin4 Server 등록 · 검증**

| 항목 | 값 |
| --- | --- |
| Name | `AWS Paper RDS - portfolio` |
| Host name/address | `localhost` |
| Port | `15433` |
| Maintenance database | `portfolio` |
| Username | `portfolio_admin` |
| Password | `[REDACTED]` |
| SSL mode | `Prefer` 또는 기본 TLS |
| tunnel 창 확인 | `Connection accepted for session [...]` 출력 |
| 검증 SQL | `select current_user, current_database(), inet_server_addr(), inet_server_port(), current_setting('search_path');` |
| 결과 | `portfolio_admin` · `portfolio` · `10.0.20.165` · `5432` · `"$user", public` |
| search_path 해석 | `portfolio_admin` 은 `ALTER ROLE ... SET search_path` 적용 대상 아님 · `"$user", public` 은 정상 (OD-DB-006 정합) |

- 🟢 **pgAdmin4 SELECT 가능 확인 · 조회 범위**

| 항목 | 값 |
| --- | --- |
| schema 수 · 핵심 table 수 | 6개 · 16개 |
| 조회 가능 table | 6개 schema · 16개 table SELECT 통과<br>connector · decision · execution · interest · ops · preprocessor 각 schema<br>상세 table 목록은 [02 spec operation-notes 2026-06-13](specs/02-aws-network-and-rds/operation-notes.md) |
| 조회 범위 | SELECT 한정 · INSERT · UPDATE · DELETE · DDL 0건 |

- 🟢 **pgAdmin4 운영 원칙**

| 항목 | 값 |
| --- | --- |
| tunnel 필수 | pgAdmin4 로 AWS Paper RDS 를 보려면 SSM Port Forwarding PowerShell 창 먼저 열려 있어야 함 · `Port 15433 opened` · `Waiting for connections...` 상태 유지 필수 |
| tunnel 종료 | tunnel 창 종료 또는 `Ctrl + C` 시 pgAdmin4 연결 즉시 단절 |
| Server 등록 | RDS endpoint 직접 등록 <span style="color:#D1242F">**금지**</span> · `localhost:15433` 만 설정 |
| 실제 접속 대상 | AWS Private RDS (`10.0.20.165:5432`) |

- 🟡 **결정 락 (2026-06-13 네 번째 세션)**

| ID | 내용 |
| --- | --- |
| OD-NET-011 | Local-to-AWS Paper RDS pgAdmin4 사용 원칙 = `localhost:15433` SSM tunnel 만 등록 · RDS endpoint 직접 등록 <span style="color:#D1242F">**금지**</span> · Server 등록 표준 (Name `AWS Paper RDS - portfolio` · Host `localhost` · Port `15433` |
| OD-NET-011 (계속) | Maintenance database `portfolio` · Username `portfolio_admin` 또는 MS 별 app role · SSL mode `Prefer`) · <span style="color:#BF8700">**잠정**</span> |

- 🟢 **Runbook 보강 · Strategy Execution 사전 검증 종합 · 리스크 보강**

| 항목 | 값 |
| --- | --- |
| 02 spec Runbook §10 | 본문 변경 없이 보조 절차 §13 추가 |
| 보조 절차 | (a) PostgreSQL 18 psql client 직접 경로 실행 (b) pgAdmin4 Server 등록 (Name · Host · Port · DB · Username · SSL mode) (c) 성공 기준 보강 (`inet_server_addr=10.0.20.165` · `inet_server_port=5432` · RDS `PubliclyAccessible=False`) |
| Strategy Execution 사전 검증 | 본 일자 client 3종 (Python `psycopg2` · psql 18 · pgAdmin4) 모두 SSM tunnel 위에서 AWS Paper RDS 접속 가능 1차 실증 · Strategy Execution 본 phase 진입 전 다중 client 실증 완료 |
| R-AUTO-012 detection 보강 | pgAdmin4 connection 단절 패턴 · `Connection terminated` · `server closed the connection unexpectedly` 모니터 + tunnel 재기동 후 자동 복구 |
| R-AUTO-013 mitigation 보강 | PostgreSQL 18 client 이미 설치 (`C:\Program Files\PostgreSQL\18\bin\psql.exe`) · 리스크는 client 미설치가 아니라 일반 `psql` PATH 미등록 · Status 🟢 **Mitigated** 유지 |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `02/operation-notes.md` | 2026-06-13 SSM Port Forwarding 보강 (psql 18 + pgAdmin4) 섹션 §1~§5 |
| `03/operation-notes.md` | 2026-06-13 SSM Port Forwarding 표준 경유지 역할 · psql 18 + pgAdmin4 보강 §1~§3 |
| `04/operation-notes.md` | 2026-06-13 Strategy Execution 사전 검증 · psql 18 + pgAdmin4 보강 §1~§4 |
| `_common/operator-decisions.md` | OD-NET-011 추가 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | 2026-06-13 SSM Port Forwarding 보강 후속 메모 |
| `_common/risk-register.md` | R-AUTO-012 · R-AUTO-013 mitigation·detection 보강 |
| `.kiro/CHANGELOG.md` | 2026-06-13 네 번째 섹션 |
| `.kiro/WORKLOG.md` | 본 파일 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| AWS · RDS · IAM · Secrets Manager · SSM 변경 | 0건 |
| 사용 도구 | read-only AWS API + SSM 세션 재사용 + psql 18 / pgAdmin4 / Python `psycopg2` SELECT 조회만 |
| RDS DDL/DML | 0건 |
| broker · KIS · 주문 · 체결 · Daily Batch entrypoint 호출 | 0건 |
| `--execute` 실호출 | 0건 |
| 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 변경 | 0건 |
| pgAdmin4 · psql 18 client | 운영자 로컬 PC 도구 · 별도 spec 산출물 영향 없음 |
| 민감정보 (password · secret · KIS · 계좌 · token · account-id · IAM access key · secret ARN · EIP) 신규 기록 | 0건 · 모두 `[REDACTED]` 또는 placeholder |

  <details><summary>🔵 운영 식별자 요약</summary>

  - 앞 세 번째 세션 재사용: instance id · private IP · local port · SSM session id `terraform-vjp3fv3nz73konetcevdzjh9de` · RDS endpoint hostname
  - 추가: 로컬 PostgreSQL 설치 경로 `C:\Program Files\PostgreSQL\18\bin\psql.exe` (운영자 로컬 PC 도구 · secret 아님)

  </details>

- 다음 작업 · 일반 `psql` PATH 등록 (`C:\Program Files\PostgreSQL\18\bin`) · pgAdmin4 환경별 (paper · live) 서버 분리 등록 정책 표준화 (서버 이름 prefix) · pgAdmin4 connection pool 자동 reconnect 정책 · MS app role 별 (Strategy Execution `execution_app`
  MarketConnector `marketconnector_app` · Interest Crawler `crawler_app` · Interest Preprocessor `preprocessor_app` · View `view_app`) 추가 client 접속 검증 · 평일 또는 안전 테스트 데이터 기반 `READY -> REQUESTED -> SUBMITTED` end-to-end dry · integration 검증
  모두 후속 분리.

## 2026-06-13 (Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook 1차 본문)

- 🟢 **작업 요약** (같은 일자 세 번째 세션)

| 항목 | 값 |
| --- | --- |
| 성격 | 운영자 직접 · SSM Port Forwarding 기반 로컬 → AWS Paper RDS 연결 1차 실증 |
| 결정 락 | OD-NET-010 (SSM Port Forwarding 표준 경유지) 신규 + OD-ENV-006 · OD-ENV-007 · OD-ENV-008 1차 실증 메모 보강 |

- 🟢 **사전 도구 점검**

| 항목 | 값 |
| --- | --- |
| `aws --version` | `aws-cli/2.27.50 Python/3.13.4 Windows/11 exe/AMD64` |
| Session Manager Plugin | 1차 인식 실패 → 설치 후 `1.2.814.0` |

- 🟢 **SSM Port Forwarding 표준 경유지 결정**

| 항목 | 값 |
| --- | --- |
| 후보 EC2 1 | `portfolio-paper-marketconnector-ec2` · instance id `i-0fce77927b7397b88` · private ip `10.0.0.181` · running |
| 후보 EC2 2 | `portfolio-paper-crawler-worker` · instance id `i-0ff768ea639a91355` · private ip `10.0.0.169` · running |
| 결정 | `portfolio-paper-marketconnector-ec2` |
| 사유 | RDS 접근 검증 이력 (2026-06-09 RDS restore runner) 보유 · MarketConnector / Strategy Execution / View 가 바라볼 Paper DB 접근 경유지로 자연스러움 |
| crawler worker | KRX GUI / Windows worker 역할 유지 · SSM Port Forwarding 경유지 사용 안함 |

- 🟢 **target EC2 SSM Online · AWS Paper RDS endpoint · SSM tunnel 오픈**

| 항목 | 값 |
| --- | --- |
| `describe-instance-information` | ping `Online` · platform `Linux` · agent `3.3.4515.0` |
| RDS 상태 | DB name `portfolio` · endpoint `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com` · port `5432` · `PubliclyAccessible=False` · status `available` |
| RDS Private 정책 | 변경 없음 (R-SEC-001 · R-NET-004 · OD-NET-009) |
| `start-session` 명령 | `AWS-StartPortForwardingSessionToRemoteHost` · host `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com` · portNumber `5432` · localPortNumber `15433` |
| session id · local port | `terraform-vjp3fv3nz73konetcevdzjh9de` · `15433` |
| 출력 | `Port 15433 opened` · `Waiting for connections...` |

- 🟢 **로컬 psql 인식 실패 · Python `psycopg2` 사용**

| 항목 | 값 |
| --- | --- |
| 실패 명령 | `psql -h localhost -p 15433 -U portfolio_admin -d portfolio` (PowerShell 미인식) |
| 원인 | 로컬 PostgreSQL client PATH 미등록 (AWS / SSM / RDS 정합성 문제 아님) |
| 조치 | psql 설치 보류 · Python `psycopg2` 로 진행 (R-AUTO-013 신규 mitigation) |
| `psycopg2` 점검 | `python -c "import psycopg2; print('psycopg2 OK')"` = `psycopg2 OK` |

- 🟢 **`portfolio_admin` 접속 검증**

| 항목 | 값 |
| --- | --- |
| host · port · dbname · user | `localhost` · `15433` · `portfolio` · `portfolio_admin` |
| password | 환경변수 `PGPASSWORD` 사용 · 평문 노출 0건 |
| Python 접속 결과 | `('portfolio_admin', 'portfolio', '10.0.20.165', 5432)` |
| 의미 | 로컬 PC → SSM tunnel → AWS Paper RDS 접속 1차 실증 통과 · `localhost:15433` 의 실제 대상이 AWS Paper RDS 임을 `inet_server_addr=10.0.20.165` · `inet_server_port=5432` 로 확인 (R-DATA-007 detection 보강) |

- 🟢 **`execution_app` 접속 검증**

| 항목 | 값 |
| --- | --- |
| host · port · dbname · user | `localhost` · `15433` · `portfolio` · `execution_app` |
| password | 환경변수 `PGPASSWORD` 사용 |
| current_user · current_database | `execution_app` · `portfolio` |
| search_path | `execution, decision, research, connector, preprocessor, interest, reference, legacy, public` (OD-DB-006 · OD-DB-007 정합 · legacy USAGE 미부여로 접근 차단) |
| 의미 | Strategy Execution AWS 포팅 진입 전 AWS Paper RDS app role 접속 1차 실증 통과 |

- 🟢 **Local-to-AWS Paper RDS SSM Port Forwarding Runbook 1차 본문**

| 항목 | 값 |
| --- | --- |
| 구성 | 사전 점검 · tunnel 오픈 · 환경변수 표준 export · 접속 검증 · MS 별 app role 매핑 · 성공 기준 · 실패·복구 7단계 + `[실행]`/`[확인]`/`[준비]`/`[복구]` 라벨 |
| 표준 환경변수 | `PORT_ENVIRONMENT=paper` · `PORT_DB_TARGET=aws-paper` · `INTEREST_DB_HOST=localhost` · `INTEREST_DB_PORT=15433` · `INTEREST_DB_NAME=portfolio` |
| MS 별 app role | Strategy Execution `execution_app` · MarketConnector `marketconnector_app` · Interest Crawler `crawler_app` · Interest Preprocessor `preprocessor_app` · View `view_app` |
| 평문 기록 | password · secret value 본문 · 운영자 노트 · 콘솔 캡처 · 로그 <span style="color:#D1242F">**금지**</span> (R-DOCS-001 정합) |

- 🟡 **결정 락 (2026-06-13 세 번째 세션)**

| ID | 내용 |
| --- | --- |
| OD-NET-010 | SSM Port Forwarding 표준 경유지 = `portfolio-paper-marketconnector-ec2` · local port `15433` · 🟠 **잠정** |
| OD-ENV-006 · OD-ENV-007 · OD-ENV-008 | 본문 변경 없이 1차 실증 메모 보강 |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `02/operation-notes.md` | 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook §1~§12 |
| `03/operation-notes.md` | 2026-06-13 SSM Port Forwarding 표준 경유지 역할 1차 검증 §1~§5 |
| `04/operation-notes.md` | 2026-06-13 Strategy Execution AWS 포팅 사전 검증 §1~§4 |
| `_common/operator-decisions.md` | OD-NET-010 추가 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | 2026-06-13 SSM Port Forwarding 후속 메모 |
| `_common/risk-register.md` | R-DATA-007 detection 보강 + R-AUTO-012 · R-AUTO-013 신규 |
| `.kiro/CHANGELOG.md` | 2026-06-13 세 번째 섹션 |
| `.kiro/WORKLOG.md` | 본 파일 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| AWS · RDS · IAM · Secrets Manager · SSM 변경 | 0건 |
| 사용 도구 | read-only AWS API 호출 + SSM Port Forwarding 세션 + Python `psycopg2` SELECT 조회만 |
| RDS DDL/DML | 0건 |
| broker · KIS · 주문 · 체결 · Daily Batch entrypoint 호출 | 0건 |
| `--execute` 실호출 | 0건 |
| 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 변경 | 0건 |
| 민감정보 (password · secret · KIS · 계좌 · token · account-id · IAM access key · secret ARN · EIP) 신규 기록 | 0건 · 모두 `[REDACTED]` 또는 placeholder |

  <details><summary>🔵 운영 식별자 요약</summary>

  - instance id `i-0fce77927b7397b88` · `i-0ff768ea639a91355`
  - private IP `10.0.0.181` · `10.0.0.169` · `10.0.20.165`
  - local port `15433`
  - SSM session id `terraform-vjp3fv3nz73konetcevdzjh9de`
  - RDS endpoint hostname `portfolio-paper-rds.c72ecae22z3y.ap-northeast-2.rds.amazonaws.com`

  </details>

- 다음 작업 · psql client 정식 설치 · PATH 등록 (R-AUTO-013) · 모든 MS 의 Paper mode DB 환경변수 인벤토리 점검 · Strategy Execution ECS · Fargate 포팅 본 phase · MarketConnector 신규 executor EC2 배포 후보 zip · tag 산출
  평일 또는 안전 테스트 데이터로 `READY -> REQUESTED -> SUBMITTED` end-to-end dry · integration 검증 · SSM Port Forwarding session 자동 keep-alive · reconnect 도입 여부 결정 · 모두 후속 분리.

## 2026-06-13 (Strategy Execution / MarketConnector 책임 분리 + View Daily Batch 17단계 + Local-to-AWS Paper RDS 운영 원칙)

- 🟢 **작업 요약** (같은 일자 두 번째 세션 · Strategy Decision ECS / Fargate 검증 섹션과 별개)

| 항목 | 값 |
| --- | --- |
| (a) Strategy Execution 책임 분리 | `execution_repository.py` · `daily_auto_buy_execute_run.py` · `daily_auto_sell_execute_run.py` |
| (b) MarketConnector 신규 executor | `connector_strategy_order_execute.py` |
| (c) View Daily Batch 17단계 재구성 | `DailyBatchService.java` · `DailyBatchLabelUtils.java` |
| (d) Local 개발 / AWS Paper RDS 운영 원칙 | 정리 |
| 성격 | 운영자 직접 · Kiro 는 문서·절차·검증 정리만 |

- 🟢 **Strategy Execution 변경**

| 항목 | 값 |
| --- | --- |
| 파일 3개 | `execution_repository.py` · `daily_auto_buy_execute_run.py` · `daily_auto_sell_execute_run.py` |
| 신규 repository 함수 | `mark_execution_order_requested(conn, execution_order_id, result_payload)` |
| 제거 | MarketConnector 경로 하드코딩 · `sys.path` 삽입 · `connector_buy` / `connector_sell` import · `buy_stock()` / `sell_stock()` 직접 호출 · SELL `mark_position_sell_ordered()` 호출 |
| `--execute` 의미 축소 | 실제 주문 제출 → `READY -> REQUESTED` 상태 전환 |
| `connector_order_request_id` | Strategy Execution 측 생성 / 업데이트 안함 |
| 정적 검증 | `python -m py_compile` 3파일 통과 · 직접 import / call 검색 0건 · UTF-8 한글 정상 |
| dry run 미확인 | 주말 가드 (WEEKEND) 영향으로 DB 후보 조회 단계 미확인 |
| `--execute` 실호출 | 0건 |

- 🟢 **MarketConnector 변경**

| 항목 | 값 |
| --- | --- |
| 신규 파일 | `connector_strategy_order_execute.py` (489 insertions) |
| 조회 · 정렬 | `REQUESTED` + `connector_order_request_id IS NULL` · SELL 우선 · BUY 후순위 |
| 기본 동작 | dry run · 목록 출력만 · KIS 호출 · DB update 0건 |
| `--execute` guard | `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` · `connector_buy` / `connector_sell` lazy import 후 `buy_stock()` / `sell_stock()` 호출 · 성공 시 `strategy_execution_order` `SUBMITTED` · 실패 시 `FAILED` · SELL 성공 시 position `SELL_ORDERED` |
| 기존 entrypoint 8종 변경 | 0건 (`connector_buy.py` · `connector_sell.py` · `connector_order_common.py` · `connector_order_check.py` · `connector_balance.py` · `db_config.py` · `config.py` · `token_manager.py`) |
| 정적 검증 | `python -m py_compile` 통과 · UTF-8 한글 정상 · dry run 결과 `[NO_TARGET] REQUESTED strategy order 없음` (REQUESTED 대상 0건) |
| `--execute` 실호출 | 0건 |

- 🟢 **View 변경**

| 항목 | 값 |
| --- | --- |
| 파일 2개 | `src/main/java/my/portfolio/port_view/service/DailyBatchService.java` · `src/main/java/my/portfolio/port_view/util/DailyBatchLabelUtils.java` |
| 신규 step | `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` · order 12 · name `Strategy 주문 실행` · command `python connector_strategy_order_execute.py --execute` |
| 삽입 위치 | `DAILY_AUTO_BUY`(11) 다음 · `CONNECTOR_ORDER_CHECK` 이전 |
| 후속 step order 조정 | `CONNECTOR_ORDER_CHECK` 13 · `SYNC_SELL_FILL` 14 · `SYNC_BUY_FILL` 15 · `SYNC_BUY_POSITION` 16 · `BALANCE_REFRESH` 17 |
| isNoTarget 인식 문구 5종 | `REQUESTED strategy order 없음` · `strategy execution requested 주문이 없음` · `[NO_TARGET] REQUESTED strategy order 없음` · `no requested` · `no target` |
| Label 추가 | `DailyBatchLabelUtils.stepCodeLabel()` · `stepCodeShortLabel()` 양쪽 |
| 컴파일 | `.\mvnw.cmd clean compile` 성공 |
| Daily Batch 실행 · Python 주문 스크립트 · DB · AWS 접근 | 0건 |

- 🟢 **Local 개발 / AWS Paper RDS 운영 원칙**

| 항목 | 값 |
| --- | --- |
| Paper source of truth | AWS Paper RDS 단일 고정 |
| `PORT_ENVIRONMENT=paper` | 로컬 실행에서도 AWS Paper RDS 사용 · AWS 실행도 동일 |
| 로컬 PostgreSQL | `LOCAL_DEV` fixture · 실험 · 백업 참고용 |
| 환경 라벨 | `LOCAL_DEV` · `PAPER` · `LIVE` 3종 · `LIVE` 는 후속 설계 대상 |
| Local → AWS Paper RDS 접속 | SSM Port Forwarding · RDS Private 유지 (R-SEC-001 · R-NET-004 정합) |
| 환경 식별 | `localhost:15433` 이라도 실제 대상 = AWS Paper RDS · host 만으로 환경 식별 <span style="color:#D1242F">**금지**</span> |
| paper 주문 실행 guard | `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` |
| Local DB ↔ AWS Paper RDS 동기화 | `connector_order_request` · `connector_fill` · `strategy_execution_order` · `strategy_position_state` 병합 <span style="color:#D1242F">**금지**</span> |

- 🟡 **결정 락 (2026-06-13 두 번째 세션)**

| ID | 내용 |
| --- | --- |
| OD-MS-016 | Strategy Execution / MarketConnector 책임 분리 · 🟠 **잠정** |
| OD-ENV-006 | Paper 환경 DB source of truth = AWS Paper RDS 단일 · 🟠 **잠정** |
| OD-ENV-007 | Local PC → AWS Paper RDS 접속 방식 = SSM Port Forwarding 만 · 🟠 **잠정** |
| OD-ENV-008 | Local DB ↔ AWS Paper RDS 동기화 미사용 · 🟠 **잠정** |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `03/operation-notes.md` | 2026-06-13 Strategy 주문 실행 executor 추가 섹션 |
| `04/operation-notes.md` | 2026-06-13 Strategy Execution 책임 분리 + View Daily Batch 17단계 변경 섹션 |
| `02/operation-notes.md` | 2026-06-13 Local-to-AWS Paper RDS 운영 모드 정리 섹션 |
| `_common/operator-decisions.md` | OD-MS-016 · OD-ENV-006 · OD-ENV-007 · OD-ENV-008 추가 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | 2026-06-13 책임 분리 + Local-to-AWS Paper RDS 후속 메모 |
| `_common/risk-register.md` | R-DATA-007 · R-AUTO-009 · R-AUTO-010 · R-AUTO-011 신규 |
| `.kiro/CHANGELOG.md` | 2026-06-13 두 번째 섹션 |
| `.kiro/WORKLOG.md` | 본 파일 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| AWS · RDS · SSM · EC2 · Docker · ECR · ECS · IAM · Secrets Manager 작업 | 0건 |
| broker · KIS · 주문 · 체결 · Daily Batch entrypoint 호출 | 0건 |
| RDS DDL/DML | 0건 |
| `--execute` 실호출 | 0건 |
| 코드 변경 (Strategy Execution 3건 · MarketConnector 1건 신규 · View 2건) | 모두 운영자 직접 수행 · Kiro 는 사실·절차·검증 정리만 |
| 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog 변경 | 0건 |
| 운영자 직접 작성 Python / Java 변경분 본문 인용 | 0건 (본 파일 · spec 산출물 사실 기록) |
| 민감정보 (secret · 계좌 · RDS · KIS · token · account-id · ARN · instance-id · EIP · IAM access key) 신규 기록 | 0건 · 모두 `[REDACTED]` 또는 placeholder |

- 다음 작업 · SSM Port Forwarding runbook 정리 · 모든 MS 의 `PORT_ENVIRONMENT` · `PORT_DB_TARGET` 점검 · Strategy Execution AWS 포팅 (`port_strategy_execution` ECS · Fargate) · MarketConnector 신규 executor EC2 배포 후보 zip · tag 산출
  평일 또는 안전한 테스트 데이터로 `READY -> REQUESTED -> SUBMITTED` end-to-end dry · integration 검증 · 신규 View 17단계 운영 직전 end-to-end 검증 · 모두 후속 분리.

## 2026-06-13 (Strategy Decision ECS / Fargate 1차 포팅 검증 + 08 spec SSM 자동화 + ECS crawler smoke)

- 🟢 **작업 요약** (04 spec Strategy Decision + 08 spec SSM 자동화 + ECS crawler smoke)

| 항목 | 값 |
| --- | --- |
| (04) Strategy Decision | Dockerfile · ECR · Log Group · Secret · Role · TD 2개 · RunTask 2건 |
| (08) SSM 자동화 | Managed Node · Scheduled Task · `schtasks /Run` trigger |
| (08) ECS crawler smoke | `portfolio-paper-interest-crawler:6` · Selenium Chrome smoke |
| 성격 | 운영자 직접 · Kiro 는 문서·절차·검증 정리만 |

- 🟢 **04 spec — Strategy Decision Docker / ECR**

| 항목 | 값 |
| --- | --- |
| Dockerfile · requirements.txt | 운영자 직접 신규 · Docker build context `C:\Workspaces` · image vendoring `port_strategy_common` + `port_strategy_decision` |
| 로컬 빌드 · container smoke | `portfolio-strategy-decision:paper-20260613` · import smoke 성공 |
| ECR push | repository `portfolio-strategy-decision` · `paper-20260613` · `paper-latest` |

- 🟢 **04 spec — AWS 리소스**

| 항목 | 값 |
| --- | --- |
| CloudWatch Log Group | `/portfolio/paper/strategy-decision` · retention 14일 |
| Secrets Manager | `/portfolio/paper/rds/decision-app` JSON multi-key 신규 |
| Execution Role | decision-app secret read inline (ARN 한정 · wildcard 0건) |
| Task Role | `portfolio-paper-decision-task-role` 신규 |
| ECS Cluster · Task Execution Role | `portfolio-paper-cluster` · `portfolio-paper-ecs-task-execution-role` (08 spec 에서 1차 생성 · 본 일자 재사용) |

- 🟢 **04 spec — Task Definition 2개 · RunTask 2건**

| 항목 | 값 |
| --- | --- |
| TD 2개 | `portfolio-paper-strategy-decision-buy-signal` · `portfolio-paper-strategy-decision-position-signal` |
| 스펙 | awsvpc · Fargate · cpu 512 · memory 1024 · 환경변수 5종 `INTEREST_DB_HOST` · `INTEREST_DB_PORT` · `INTEREST_DB_NAME` · `INTEREST_DB_USER` · `INTEREST_DB_PASSWORD` |
| 통합 `daily_decision_run.py` 신규 | 보류 · 기존 Daily Batch step 6 / step 7 구조 계승 · TD 2개 분리 확정 (OD-MS-013) |
| RunTask 결과 | exitCode 0 · CloudWatch 로그 + RDS 접속 성공 · `daily_run_id` 44 · `run_date` 2026-06-13 · `data_date` 2026-06-08 · `market_signal` BLOCK · block_watch 1건 · position decision 0건 |
| 1차 실패 · 조치 | (a) buy-signal: `research.strategy_block_watch_candidate` 권한 부족 → GRANT 보정 (b) position-signal: `execution.strategy_position_state` 탐색 실패 (execution + decision schema 권한 부족) → GRANT 보정 · 재실행 성공 |

- 🟢 **08 spec — SSM 자동화 · Scheduled Task**

| 항목 | 값 |
| --- | --- |
| SSM Managed Node | `portfolio-paper-crawler-worker` 점검 |
| SSM 직접 wrapper 실행 판정 | KRX GUI 로그인 부적합 · SYSTEM Session 0 / SessionId 0 vs SessionId 2 분리 |
| Windows Scheduled Task 신규 | `Portfolio-KRX-Worker-Daily` · Administrator interactive · `C:\portfolio\run_krx_worker_daily.ps1` |
| `Start-ScheduledTask` 1차 검증 | 성공 · `KRX ID/PW Login Success` · `interest_program` 2026-06-12 1건 · `interest_shortsell` 2026-06-12 349건 · `KRX worker daily wrapper DONE` |
| RDP closed → SSM RunCommand → `schtasks /Run` trigger | 성공 · `SUCCESS: Attempted to run the scheduled task` · Task State Running → Ready · 최신 로그 `krx_worker_daily_20260613_021904.log` · `[Collected Date] None` = 2026-06-12 까지 이미 수집 완료 · idempotent / no-op 정상 |
| KRX GUI 경로 자동화 확정 | OD-MS-015 |

- 🟢 **08 spec — ECS crawler smoke**

| 항목 | 값 |
| --- | --- |
| Task Definition | `portfolio-paper-interest-crawler:6` · image `portfolio-interest-crawler:paper-20260611` · Fargate · cpu 1024 · memory 2048 · `taskRoleArn` `portfolio-paper-crawler-task-role` |
| RunTask 조건 | public-a / public-b + `sgroup-crawler-tasks` + `assignPublicIp=ENABLED` |
| 결과 | exitCode 0 · `SELENIUM CHROME SMOKE SUCCESS` · Selenium 4.40.0 · Chromium · chromedriver · `example.com` 도달 · Naver Finance (`Npay 증권`) 도달 · `DRIVER QUIT` · `SELENIUM CHROME SMOKE END` |
| revision 6 의미 | Selenium / Chrome / outbound smoke 전용 · 운영용 TD 분리는 후속 (task 58) |

- 🔵 **Hybrid execution model 1차 완성 판단**

| 항목 | 결과 |
| --- | --- |
| KRX program / shortsell | EC2 worker |
| non-GUI crawler runtime 가용성 | ECS Fargate smoke 통과 |
| preprocessor | ECS Fargate Task |
| 자동화 진입점 1단계 | SSM RunCommand → Scheduled Task trigger |
| 후속 분리 | EventBridge Scheduler 정기 trigger · Step Functions hybrid orchestration · non-GUI crawler 운영용 TD 분리 · wrapper 내 DB 검증 출력 자동 추가 (R-AUTO-007) · EC2 worker stop 절차 |

- 🟡 **결정 락 (2026-06-13)**

| ID | 내용 |
| --- | --- |
| OD-MS-013 | Strategy Decision TD 분리 정책 · 🟠 **잠정** |
| OD-MS-014 | `port_strategy_common` 1차 배포 = vendoring · 🟠 **잠정** |
| OD-MS-015 | KRX GUI crawler 1차 자동화 = SSM RunCommand → `schtasks /Run` → Scheduled Task → Administrator interactive · SSM 직접 실행 부적합 · 🟠 **잠정** |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `04/operation-notes.md` | 신규 생성 (2026-06-13 §1~§10) |
| `08/operation-notes.md` | 2026-06-13 섹션 추가 |
| `08/tasks.md` | task 30 · 31 · 53 갱신 + §13 task 61~69 신규 + Task Dependency Graph · 이월 요약 |
| `08/design.md` | §13 신규 |
| `_common/operator-decisions.md` | OD-MS-013 · OD-MS-014 · OD-MS-015 추가 + Decision Summary · Tentative · Change Log |
| `_common/followups-overview.md` | 2026-06-13 후속 메모 2건 + 04 / 08 spec 본문 1차 적용 결과 보강 |
| `_common/risk-register.md` | R-DATA-005 · R-AUTO-005 · R-AUTO-007 보강 + R-AUTO-008 신규 |
| `.kiro/CHANGELOG.md` | 2026-06-13 섹션 |
| `.kiro/WORKLOG.md` | 본 파일 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| AWS · Docker · ECR · ECS · IAM · Secrets Manager · SSM · EC2 · RDS · GRANT 작업 | 모두 운영자 직접 수행 · Kiro 는 문서·절차·검증 정리만 |
| 8개 MS (`port-view` · `port-marketconnector` · `port-interest-crawler` · `port-interest-preprocessor` · `port_strategy_common` · `port_strategy_decision` · `port_strategy_execution`<br>`port_strategy_research`) README · AGENTS.md · CHANGELOG · docs · worklog · 소스 변경 | 0건 |
| 운영자 직접 작성 `port_strategy_decision` Dockerfile · requirements.txt · EC2 Scheduled Task · wrapper · 환경변수 주입 ps1 본문 인용 | 0건 (operation-notes 사실 기록) |
| `secretsmanager:GetSecretValue` 실호출 | 0건 |
| broker · KIS · 주문 · 체결 · Daily Batch entrypoint 호출 | 0건 |
| KRX 로그인 ID / password | "Secrets Manager 에서 주입" 으로만 표기 |
| 민감정보 (secret · RDS · KRX · KIS · 계좌 · token · account-id · ARN · image digest · IAM access key · instance-id · task ARN) 신규 기록 | 0건 · 모두 `[REDACTED]` 또는 placeholder |

- 다음 작업 · EventBridge Scheduler → ECS RunTask (04) · EventBridge Scheduler → SSM RunCommand → `schtasks /Run` (08) 정기 trigger 연계
  Step Functions state machine (buy-signal → position-signal 순서 강제 + 자동 재시도 <span style="color:#D1242F">**금지**</span> OD-SAFE-004) · SFN 에서 ECS Task + EC2 worker hybrid orchestration · Strategy Execution ECS · Fargate 포팅 검증
  `port_strategy_common` 정식 package
  version 관리 (07 / Strategy Common) / `decision_app` 권한 매트릭스 정식 정리 (02 spec db-roles-and-grants 후속) / CloudWatch Logs Agent
  SSM output 기반 EC2 worker 로그 수집 / wrapper 내 DB 검증 출력 자동 추가 (R-AUTO-007) / non-GUI crawler 실제 운영용 TD 분리 (task 58) / EC2 worker 작업 완료 후 stop 절차 — 모두 후속 분리.

## 2026-06-12 (Windows EC2 worker 기반 KRX GUI 의존 수집 1차 검증)

- 🟢 **작업 요약**

| 항목 | 값 |
| --- | --- |
| 대상 | 08 spec KRX GUI 의존 수집 (KRX program · KRX shortsell) 1차 운영 가능 상태 도달 |
| 환경 | Windows EC2 worker · venv `C:\portfolio\venvs\interest-crawler` · Python 3.13.5 |
| RDS Secret DB env 주입 | `/portfolio/paper/rds/crawler-app` |
| 다운로드 경로 junction | `C:\Users\USER\Downloads` → `C:\Users\Administrator\Downloads` |
| KRX Secrets Manager 신규 | `/portfolio/paper/krx/crawler-login` + IAM 권한 추가 (secret 한정 · wildcard 0건) |
| 실행 성공 파일 | `interest_krx_login_new.py` · `interest_program.py` · `interest_shortsell.py` 단독 실행 |

- 🟢 **DB 적재 결과**

| 테이블 | 값 |
| --- | --- |
| `interest_program_raw` | 543 → 546 (2026-06-09 · 2026-06-10 · 2026-06-11 각 1건) |
| `interest_shortsell_raw` | 189158 → 190205 (각 349건) |
| 적재 정합성 검증 | `check_program_rows.py` · `check_shortsell_rows.py` 로 운영자 직접 확인 |

- 🟢 **EC2 worker daily wrapper**

| 항목 | 값 |
| --- | --- |
| 신규 wrapper | `run_krx_worker_daily.ps1` 운영자 직접 |
| 1차 실행 | KRX login · program · shortsell 모두 성공 |
| 재실행 | 신규 수집 대상 없음 · `[Collected Date] None` 출력 · idempotent / no-op 정상 완료 |
| 로그 파일 형식 | `krx_worker_daily_yyyyMMdd_HHmmss.log` |

- 🔵 **분류 결정 (Hybrid execution model)**

| 항목 | 결과 |
| --- | --- |
| KRX GUI 의존 crawler | Windows EC2 worker 로 분리 확정 |
| non-GUI crawler | ECS Fargate Task 후보 유지 |
| preprocessor | ECS Fargate Task 유지 (2026-06-10 그대로) |
| 완전 자동화 (SSM RunCommand · EventBridge Scheduler · SFN hybrid orchestration · CloudWatch Logs · wrapper 내 DB 검증 자동 추가 · EC2 worker stop 절차) | 후속 분리 |

- 🟢 **인코딩 원칙**

| 항목 | 값 |
| --- | --- |
| 향후 EC2 Python 소스 수정 | PowerShell `Get-Content` / `Set-Content` 직접 replace 대신 Python patch script 사용 |
| Python patch script | `read_text(encoding="utf-8-sig")` + `write_text(encoding="utf-8")` |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `08/operation-notes.md` | 2026-06-12 섹션 추가 |
| `08/tasks.md` | crawler runtime 항목 갱신 + §11 Windows EC2 worker task 44~52 + §12 후속 task 53~60 |
| `08/design.md` | §12 Hybrid execution model 보강 |
| `08/requirements.md` | R8 acceptance criterion 5 추가 |
| `_common/operator-decisions.md` | OD-MS-011 · OD-MS-012 · OD-SEC-008 추가 + Decision Summary · Tentative · Change Log |
| `_common/risk-register.md` | R-AUTO-005 mitigation 보강 + R-AUTO-006 · R-SEC-005 · R-DOCS-002 · R-AUTO-007 신규 |
| `_common/followups-overview.md` | 2026-06-12 후속 메모 |
| `.kiro/CHANGELOG.md` | 2026-06-12 섹션 |
| `.kiro/WORKLOG.md` | 본 파일 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| AWS · IAM · Secrets Manager · EC2 · RDS 작업 | 모두 운영자 직접 수행 · Kiro 는 문서·절차·검증 정리만 |
| 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog · 소스 · 패키징 변경 | 0건 |
| 운영자 EC2 내부 생성 ps1 · py 테스트 · wrapper 본문 인용 | 0건 (operation-notes 사실 기록) |
| KRX 로그인 ID / password | "Secrets Manager 에서 주입" 으로만 표기 |
| password rotate | 본 일자 작업 범위 밖 |
| 민감정보 (secret · KRX password · RDS · KIS · 계좌 · token · RDS endpoint · account-id · ARN · image digest · IAM access key · instance-id) 신규 기록 | 0건 · 모두 `[REDACTED]` 또는 placeholder |

- 다음 작업 — SSM RunCommand 기반 EC2 worker 무인 실행 · EventBridge Scheduler · Step Functions hybrid orchestration · CloudWatch Logs Agent 연동 · wrapper 내 DB 검증 출력 자동 추가 · ECS · Fargate non-GUI crawler 범위 재정리 · EC2 worker stop 절차 명시 — 모두 후속 분리.

## 2026-06-10 (afternoon ~ evening session)

- 🟢 **작업 요약** (08-interest-crawler-and-preprocessor-ecs 기본 포팅 1차 진행)

| 항목 | 값 |
| --- | --- |
| ECR repository 2개 신규 | `portfolio-interest-crawler` · `portfolio-interest-preprocessor` |
| Dockerfile + requirements.txt 신규 | preprocessor: `python:3.13-slim` + `pre_daily.py` · crawler: `python:3.13-slim` + Chromium / chromedriver + `interest_crawler_daily.py` |
| 로컬 빌드 · ECR push | `paper-20260610` · `paper-latest` 완료 |
| crawler Selenium 의존성 | 빌드 단계에서 1차 해소 |

- 🟢 **ECS 리소스 · Role · Log Group**

| 항목 | 값 |
| --- | --- |
| ECS Cluster | `portfolio-paper-cluster` |
| Task Execution Role | `portfolio-paper-ecs-task-execution-role` · managed `AmazonECSTaskExecutionRolePolicy` + preprocessor DB secret read inline · 단일 secret ARN 한정 |
| Task Role 2종 | `portfolio-paper-preprocessor-task-role` · `portfolio-paper-crawler-task-role` |
| CloudWatch Log Group 2개 | `/portfolio/paper/preprocessor` · `/portfolio/paper/crawler` · retention 14일 |
| Network | preprocessor task SG → RDS PostgreSQL SG 5432 inbound 허용 확인 |

- 🟢 **Preprocessor Task Definition · RunTask 1차 검증**

| 항목 | 값 |
| --- | --- |
| Secrets Manager 신규 | `/portfolio/paper/rds/preprocessor-app` JSON multi-key |
| Task Definition | family `portfolio-paper-interest-preprocessor` · revision 1 · awsvpc · cpu 512 · memory 1024 · image `paper-20260610` · ECS `secrets` env 주입 |
| RunTask 결과 | lastStatus `STOPPED` · exitCode 0 · `PREPROCESSOR PIPELINE END` |

- 🟡 **1차 · 2차 실패 · 조치**

| 케이스 | 결과 |
| --- | --- |
| 1차 실패 | Secrets Manager JSON `host` key 누락 · psycopg2 가 Unix socket `/var/run/postgresql/.s.PGSQL.5432` 시도 · secret 재생성으로 해소 |
| 2차 실패 | `public` schema 잔존 sequence 2건 (`pre_marketbreadth_daily_feature_id_seq` · `pre_macroeconomic_daily_feature_id_seq`) 의 `preprocessor_app` USAGE / SELECT 권한 부족 · 운영자 직접 GRANT · 해소 |
| 처리 원칙 | 두 사례 모두 운영자 직접 SQL 실행 · Kiro 는 결과 · 사유 · 조치 문서 정리만 |

- 🔵 **Crawler 진행 상태**

| 항목 | 값 |
| --- | --- |
| 완료 | Dockerfile · requirements · 빌드 · push |
| 이월 | Task Definition 등록 · RunTask runtime · KRX·Naver·yfinance outbound 도달 검증 |
| 안정화 100% | 본 spec 범위 밖 |

- 🟢 **Kiro 작업 산출물**

| 파일 | 변경 |
| --- | --- |
| `08/tasks.md` | 갱신 |
| `08/operation-notes.md` | 신규 생성 |
| `_common/risk-register.md` | R-DATA-005 · R-DOCS-001 보강 + R-DATA-006 · R-AUTO-005 신규 |
| `_common/followups-overview.md` | 2026-06-10 후속 메모 + 08 spec 1차 적용 결과 보강 |
| `_common/operator-decisions.md` | Change Log OD-NET-004 메모 |
| `.kiro/CHANGELOG.md` | 2026-06-10 섹션 |
| `.kiro/WORKLOG.md` | 본 파일 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| AWS · ECR · ECS · IAM · Secrets · RDS 작업 | 모두 운영자 직접 수행 · Kiro 는 문서·절차·검증 정리만 |
| 8개 MS README · AGENTS.md · CHANGELOG · docs · worklog 본 spec 작업 변경 | 0건 (운영자 직접 작업으로 port-interest-preprocessor · port-interest-crawler Dockerfile · requirements.txt 신규 생성 사실은 운영 노트에만) |
| 민감정보 (secret · password · KIS · 계좌 · token · RDS endpoint · account-id · ARN · image digest · IAM access key) 신규 기록 | 0건 · 모두 `[REDACTED]` 또는 placeholder |
| password rotate | 본 일자 문서 작업 범위 밖 |

- 다음 작업 — crawler Task Definition 등록 · RunTask runtime · KRX·Naver·yfinance outbound runtime 도달 검증 / `public` schema 잔존 sequence 추가 점검 / 08 spec runbook · validation-checklist 작성 시점 결정.

## 2026-06-09 (afternoon ~ evening session)

- 🟢 **작업 요약**

| 항목 | 값 |
| --- | --- |
| Daily 실행 | 현황 점검만 · 코드 · 자동 매매 변경 0건 |
| `.kiro` 문서 구조 정리 | 각 문서의 목적 · 자동 수정 가능 여부 · 수기 수정 필요성 · 유지 필요성 일관 정리 |
| 후속 작업 | `_common/operator-decisions.md` 파일명 오타 (`operator-dicisions.md` 등) 발견 시 정정 예정 |

- 🟢 **Local PostgreSQL pg_dump 백업**

| 항목 | 값 |
| --- | --- |
| 파일 | `portfolio_full_20260609.dump` · 422,334,494 bytes |
| 보관 위치 | `C:\Workspaces\db-backup\portfolio_20260609\` (로컬) |
| 기준선 | schema별 table count · table별 row count snapshot · index · trigger · sequence · FK 주요 object count |
| 총 table 수 | 81개 |

- 🟢 **RDS restore runner 결정**

| 항목 | 값 |
| --- | --- |
| 로컬 → RDS 직접 접속 | timeout · RDS endpoint private IP resolve · Publicly accessible=No · 기대 동작 |
| RDS restore runner | MarketConnector EC2 로 결정 |

- 🟢 **aws-paper MarketConnector EC2 생성**

| 항목 | 값 |
| --- | --- |
| OS · subnet · EIP | Amazon Linux 2023 · public subnet · EIP attach |
| IAM Role | SSM managed policy + S3 임시 migration bucket read |
| 접속 | EC2 Instance Connect 성공 · 로컬 SSH 직접 접속은 outbound 22 제한 가능성으로 보류 |
| 기본 확인 | EC2 OS 업데이트 · AWS CLI 기본 제공 |
| PostgreSQL client | 15.18 시작 · dump archive header 불일치 확인 후 18.4 로 전환 (psql 18.4 · pg_restore 18.4) |
| private RDS 접속 | EC2 에서 성공 |

- 🟢 **RDS Restore**

| 항목 | 값 |
| --- | --- |
| 경로 | 로컬 dump → S3 임시 bucket → MarketConnector EC2 |
| 파일 크기 일치 | 로컬 · S3 · EC2 모두 422,334,494 bytes |
| `pg_restore --list` | TOC 812 entries · line count 823 · dump source PostgreSQL 18.1 · format CUSTOM + gzip |
| major version mismatch 판단 | 기존 RDS PostgreSQL 16.14 에 18.1 dump restore = 하위 major restore 위험 |
| 조치 | 기존 RDS 삭제 후 PostgreSQL 18.4 기준으로 재생성 (Public access No · initial DB `portfolio`) |
| 1차 restore 오류 | `role "postgres" does not exist` · 원인 = dump owner role ↔ RDS role 불일치 |
| 재실행 | `portfolio` DB drop/recreate 후 `--no-owner --no-privileges` · 에러 없이 완료 |
| RDS 객체 owner | restore 실행 계정 기준 정리 |

- 🟢 **정합성 검증**

| 항목 | 값 |
| --- | --- |
| schema별 table count | 81개 일치 |
| table · index · sequence · FK · trigger | FK 33 · trigger 23 · 모두 로컬 기준선과 diff 0 |
| row count CSV | 82줄 diff 0 |
| CRLF · LF 차이 | `--strip-trailing-cr` 정규화 후 비교 완료 |

- 🟢 **DB Role / 권한 분리 1차 적용**

| 항목 | 값 |
| --- | --- |
| SQL 실행 | `db-roles-and-grants.md` §4 · 운영자 직접 |
| `portfolio_owner` | NOLOGIN 생성 · `portfolio_admin` 에 `portfolio_owner` 멤버십 부여 |
| schema owner 이관 대상 (9개 도메인) | reference · interest · preprocessor · research · decision · execution · connector · ops · legacy (public 변경 없음) |
| 기존 table / sequence / index owner | `portfolio_admin` 유지 |
| `REASSIGN OWNED BY portfolio_admin TO portfolio_owner` | 1차 적용에서 미실행 결정 |

- 🟢 **7개 app role 생성 · GRANT 매트릭스**

| 항목 | 값 |
| --- | --- |
| app role 7개 | `marketconnector_app` · `view_app` · `crawler_app` · `preprocessor_app` · `decision_app` · `research_app` · `execution_app` |
| 속성 | 모두 LOGIN true · SUPERUSER · CREATEDB · CREATEROLE · REPLICATION · BYPASSRLS false |
| GRANT 매트릭스 | legacy 미부여 · marketconnector_app = connector R/W + execution R-only · view_app = ops R/W + execution R-only |
| DEFAULT PRIVILEGES · search_path | 7건 적용 |
| legacy USAGE | 모든 role false |
| sequence 권한 요약 | connector 9 · decision 3 · execution 5 · interest 14 · ops 2 · preprocessor 15 · public 2 · reference 3 · research 19 |

- 🟢 **App role 접속 테스트**

| 항목 | 결과 |
| --- | --- |
| marketconnector_app | connector_order_request 33 · strategy_execution_order 17 read 성공 · execution write 차단 · legacy USAGE false |
| execution_app | execution · decision · connector read + execution write 성공 · legacy USAGE false |
| view_app | execution · connector · decision read 성공 · execution write 차단 · legacy USAGE false |
| 판정 | 모두 기대 동작과 일치 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| 본 세션 수행 | 문서 + DB 안 SQL 실행만 |
| AWS 리소스 (EC2 신규 · 기존 RDS 삭제/재생성) | 운영자 직접 · Kiro 는 결과만 기록 |
| 8개 MS 코드 · README · AGENTS.md · CHANGELOG · docs · worklog 수정 | 0건 |
| 민감정보 (password · secret · endpoint · account-id · 계좌 · token · app key · app secret · webhook URL) 신규 기록 | 0건 · 모두 `[REDACTED]` 또는 placeholder |

- 다음 작업(2026-06-10 예정) · MarketConnector 기본 포팅 (EC2 Python venv · requirements · 소스 배치 · KIS paper 계좌·RDS 접속 외부화 · `connector_balance.py` · `connector_order_check.py` 검증) · 03 spec runbook · validation-checklist · operation-notes 반영
  통합 검증·rollback 절차 문서 반영. · 원래 계획이던 ECS 기본 포팅도 진행 예정이나 이월된 MarketConnector 기본 포팅을 우선.

## 2026-06-09

- 🟢 **작업 요약** (폴더 이동 + 링크 보정)

| 항목 | 값 |
| --- | --- |
| 루트 공통 문서 6종 이동 | `operator-decisions.md` · `ms-aws-service-decision-matrix.md` · `cost-simulation.md` · `followups-overview.md` · `aws-resource-glossary.md` · `risk-register.md` → `_common/` |
| 아카이브 이동 | `note-aws-landscape-2021-vs-2026.md` → `_archive/` · 파일명·본문 유지 |
| `02/README.md` 신규 | 운영자용 한 장 요약 · 현재 상태 · 다음 작업 · 완료 리소스 · 관련 문서 링크 · 자동 갱신 기준 5개 섹션 |
| `.kiro/README.md` · `.kiro/AGENTS.md` | 폴더 구조 · 루트 참조 문서 섹션을 `_common/` · `_archive/` 기준으로 갱신 |
| 상대 링크 일괄 보정 | 01 / 02 spec 폴더의 모든 `.md` · `.kiro/docs/kiro-readonly-validator-iam.md` · 루트 메타 문서 (README · CHANGELOG · WORKLOG) · 본문 내용은 링크 경로 외 변경 없음 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| 실제 AWS 리소스 생성 · 변경 · 삭제 | 없음 |
| 8개 MS 코드 · README · AGENTS.md · CHANGELOG · docs · worklog 수정 | 없음 |
| 민감정보 (secret · token · password · app key · app secret · 계좌 · webhook URL · RDS endpoint · account-id · access key id) 신규 기록 | 0건 |

## 2026-06-06

- 🟢 **작업 요약** (02 spec 1차 적용 보조 문서 4종)

| 항목 | 값 |
| --- | --- |
| 목적 | 운영자가 AWS Console 에서 단계별로 따라 할 수 있는 보조 문서 4종 추가 |

- 🟢 **`.kiro/specs/_common/risk-register.md` 신규**

| 항목 | 값 |
| --- | --- |
| 카테고리 | R-NET · R-SEC · R-DATA · R-BROKER · R-AUTO · R-DOCS · R-COST |
| 초기 등록 | 12개 리스크 |
| 같은 날 후속 추가 | R-SEC-002 (Root 자격 · MFA 분실) · R-SEC-003 (portadmin 자격 분실) 2건 |

- 🟢 **02 spec 보조 문서 3종**

| 파일 | 변경 |
| --- | --- |
| `01/traceability-matrix.md` | Requirement → Design → Task → Decision 매핑 |
| `02/runbook.md` 1차 | 18 Step (VPC → Subnet → IGW → Route Table → NAT 미사용 확인 → SG → VPC Endpoint → RDS → DB 준비 → cutover 사전 준비 → 검증 → rollback) 실행 절차서 |
| `02/runbook.md` 후속 (같은 날) | 모든 Step 제목에 한글 실행 구분 라벨 (`[실행]` · `[확인]` · `[준비]` · `[복구]`) 추가 + 본문 상단 라벨 범례 섹션 신설 |
| `02/runbook.md` Step 0 추가 | `IAM 관리자 사용자 portadmin 생성` · 기존 Step 0~18 → Step 1~19 재번호 · 본문 cross-reference 모두 +1 일괄 갱신 |
| Step 0 sub-step 6개 | portadmin 사용자 생성 → AdministratorAccess 정책 부여 → 콘솔 sign-in URL · account alias → MFA 활성 → Root 로그아웃 후 portadmin 로그인 → Root 보안 강화 |
| `02/validation-checklist.md` | 10개 섹션 체크박스 · runbook 통과 여부 점검 · `Step 18 rollback` → `Step 19 rollback` |
| `02/traceability-matrix.md` | Requirement → Design → Task → Validation → Decision 매핑 · Risk 매핑 · 매핑 표 / Acceptance Criteria 보강 / Risk 매핑 안의 runbook Step 참조 숫자 모두 +1 |

- 🟢 **`.kiro/specs/_common/aws-resource-glossary.md`**

| 항목 | 값 |
| --- | --- |
| 신규 용어 | IAM User |
| 반영 근거 | portadmin 같은 IAM 관리자 사용자 개념이 02 runbook 에서 본격 사용 시작 |

- 🟢 **문서 갱신 · 결정값 유지**

| 항목 | 값 |
| --- | --- |
| `.kiro/README.md` · `.kiro/CHANGELOG.md` | 신규 문서 안내 · 변경 이력 갱신 |
| 결정값 (Decision ID · 선택값 · 비용 영향 · 운영 리스크 · 후속 spec 영향) | 변경 없음 |
| Console 클릭 경로 · 입력값 · 검증 항목 · rollback 순서 | 변경 없음 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| 실제 AWS 리소스 생성 | 없음 |
| 8개 MS 코드 · README · AGENTS.md · CHANGELOG · docs · worklog 수정 | 없음 |
| 민감정보 (secret · token · password · app key · app secret · 계좌 · webhook URL · portadmin 비밀번호 · Root·portadmin MFA 시리얼 · 백업 코드) 기록 | 0건 · `[REDACTED]` 또는 placeholder |

## 2026-06-08

- 🟢 **작업 요약** (02 spec Foundation · 운영자 직접 구축 · Kiro 는 ReadOnly 검증 + 문서화)

| 항목 | 값 |
| --- | --- |
| AWS Foundation 구축 | VPC · Subnet · IGW · Route Table · SG 8종 · VPC Endpoint 6종 · RDS subnet group · parameter group · `portfolio-paper-rds` · Secrets Manager `/portfolio/paper/rds/master` |
| 성격 | 운영자 AWS Console 직접 · Kiro 는 ReadOnly 검증 + 문서화만 |

- 🟢 **신규 · 갱신 문서**

| 파일 | 변경 |
| --- | --- |
| `.kiro/docs/kiro-readonly-validator-iam.md` 신규 | ReadOnly 전용 IAM 사용자 `portfolio-kiro-readonly-validator` · 정책 `PortfolioKiroReadOnlyValidatorPolicy` 설계 · Allow / Deny 정책 JSON · Console 절차 · AWS CLI 절차 · 검증 명령 모음 · 자동 검증 가능 / 수동 확인 분류 · `secretsmanager:GetSecretValue` |
| `.kiro/docs/kiro-readonly-validator-iam.md` 신규 (계속) | KMS Decrypt 명시 Deny |
| `02/operation-notes.md` 신규 | AWS Foundation 실행 기록 (SG 이름 prefix `sgroup-` 결정 · RDS 생성 결과 · KMS default 등) + 후속 검증 세션 누적 |
| `02/validation-checklist.md` 상태 라벨 시스템 신규 | 체크박스 `- [ ]` → 4종 라벨 `[O]` · `[X]` · `[Kiro 후속 작업 필요]` · `[운영자 확인 필요]` · AWS ReadOnly 자동 검증 + 문서 비교 + grep 반영 · 최종 카운트 `[O]` 78 · `[X]` 0 · `[Kiro 후속]` 2 · `[운영자]` 20 |

- 🟢 **AWS ReadOnly 자동 검증 결과 · 기대값 일치**

| 항목 | 값 |
| --- | --- |
| VPC · Subnet 6개 · IGW | 기대값 일치 |
| NAT | 미생성 |
| Route Table · SG 8종 · VPC Endpoint 6종 · RDS instance · Secrets metadata | 기대값 일치 |
| ALB · ELB · Cost Anomaly Detection | 0건 |

- 🟢 **안전 · 보안 · 실행 원칙**

| 항목 | 결과 |
| --- | --- |
| secret value 조회 | 없음 (`DescribeSecret` metadata 만) |
| KMS Decrypt 호출 | 없음 |
| AWS 리소스 생성 / 수정 / 삭제 | 없음 |
| 8개 MS 코드 · README · AGENTS.md · CHANGELOG · docs · worklog 수정 | 없음 (`git status --short` 8개 워크스페이스 모두 무변경 확인) |
| 민감정보 (secret · password · access key · token · webhook URL · 계좌) 기록 | 0건 |
| `.kiro/**/*.md` grep 결과 평문 패턴 | 0건 |

## 2026-06-05

- `.kiro/README.md`, `.kiro/CHANGELOG.md`, `.kiro/WORKLOG.md` 생성 작업을 진행했다.
- `.kiro` 작업공간이 8개 MS 전체 AWS Migration spec을 관리하는 공간임을 README에 문서화했다.
- 루트 공통 문서(`operator-decisions.md`, `ms-aws-service-decision-matrix.md`, `cost-simulation.md`, `followups-overview.md`, `aws-resource-glossary.md`, `note-aws-landscape-2021-vs-2026.md`)의 역할과 source of truth 기준을 README에 정리했다.
- Kiro 작업 로그는 날짜별 파일로 나누지 않고 `.kiro/WORKLOG.md` 하나에 간단히 누적하기로 했다.
- `.kiro/AGENTS.md`에 README / CHANGELOG / WORKLOG 관리 규칙 섹션을 추가했다. 기존 작업 규칙의 의미는 변경하지 않았다.
- 같은 날 진행한 `.kiro/specs/_common/operator-decisions.md` 구조 재정리(Status 한글 라벨, At a Glance, Open/Tentative/Deferred 모음 추가)는 결정값을 변경하지 않은 가독성 개선이며, 자세한 변경 항목은 `.kiro/CHANGELOG.md` 2026-06-05 항목에 기록했다.
- 실제 AWS 리소스 생성 없음.
- 애플리케이션 소스 코드 수정 없음.
- 8개 MS 문서(README · CHANGELOG · worklog · AGENTS.md · 소스) 수정 없음.
- 민감정보 기록 없음.
