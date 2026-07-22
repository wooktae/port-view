# PORT-STRATEGY-AI AWS Migration Specs

## Purpose

이 `.kiro` 작업공간은 PORT-STRATEGY-AI 포트폴리오의 AWS Migration 관련 spec을 작성하고 관리하기 위한 공간이다.

## Scope

디렉터리 자체는 `port-view` 안에 있지만, 여기서 다루는 범위는 `port-view` 단일 MS가 아니라 PORT-STRATEGY-AI의 8개 MS 전체다. 따라서 본 작업공간은 cross-service spec 저장소로 동작한다.

- 각 spec은 자기 범위에만 집중한다.
- 루트 공통 문서의 큰 내용을 그대로 복사하지 않고, 필요한 부분만 요약한 뒤 루트 공통 문서를 참조한다.
- 모든 결정사항은 `operator-decisions.md`와 일치시킨다. 결정이 바뀌면 spec에서 임의로 변경하지 않고 `operator-decisions.md`부터 갱신한다.
- 본 작업공간에서는 실제 구현(코드 변경, AWS 리소스 생성, 운영 entrypoint 실행)을 하지 않는다. 구현이 필요한 절차는 spec 또는 runbook으로만 작성한다.

## Current Status Dashboard

본 섹션은 운영자가 한눈에 보기 위한 짧은 진행 상태 요약이다.

- 자세한 일자별 결과·후속 인계는 `specs/_common/followups-overview.md`와 `WORKLOG.md`에 누적한다.
- 각 spec의 운영 결과는 해당 spec의 `operation-notes.md`에 누적한다.
- "완료"는 실제 데이터 적재·최신성 또는 end-to-end 상태 전이를 확인한 경우에만 사용한다.
- 정합 결정: `OD-MS-020` · `OD-MS-021` · `OD-MS-023` · `OD-MS-026` · `OD-MS-027` · `OD-MS-028` · `OD-MS-029`.

### Paper Daily Step 1~11

| 항목 | 값 |
| --- | --- |
| 진행 상태 | 🟢 자동 ENABLED |
| 2026-07-22 회차 | 정상 Scheduler 자동 실행 SUCCEEDED |
| Crawler Program | 최신 거래일 2026-07-21 · 1건 |
| Crawler Shortsell | 최신 거래일 2026-07-21 · 349건 |
| Daily Run | 2026-07-22 COMPLETED · AGGRESSIVE |
| Execution Plan | READY · 주문 준비 대상 1건 |
| 날짜 기준 | 2026-07-15 KST 적용 완료 |
| KRX worker | Python 실패 전파 · expected trade date validator |
| 안정화 | Paper Daily 1차 안정화 완료 |
| 최근 검증 일자 | 2026-07-22 |
| 관련 spec | 04, 08 |
| 상세 | [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |
| 관련 근거 | [^tz-patch]<br>[^daily-kst-2026-07-15]<br>[^daily-buy-e2e-2026-07-16]<br>[^krx-validator-2026-07-21]<br>[^paper-daily-stable-2026-07-22] |

### Paper Daily Step 12~17

| 항목 | 값 |
| --- | --- |
| 진행 상태 | 🟢 자동 ENABLED |
| 2026-07-22 회차 | 정상 Scheduler 자동 실행 SUCCEEDED |
| READY Plan → Order | validator 자동 통과 |
| 주문·체결 | 한국전력 83주 매수 체결 |
| Order Chain | validator 자동 통과 |
| Position | 83주 OPEN |
| Balance Snapshot | 2026-07-22 생성 |
| Slack·OPS | 성공 Slack 자동 수신 · 성공 기록 확인 |
| P1 acceptance | 🟢 다음 정상 자동 회차 end-to-end 관찰 완료 |
| 안정화 | Paper Daily 1차 안정화 완료 |
| 2026-07-20 이력 | 매도 2건 · Step 14~17 수동 복구 · P0 강화 |
| 2026-07-21 이력 | 정상 무주문 자동 회차 FAILED 보존 · `--allow-no-target` 보완 |
| 최근 검증 일자 | 2026-07-22 |
| 관련 spec | 04 |
| 상세 | [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |
| 관련 근거 | [^approval-slack-2026-07-15]<br>[^daily-recovery-2026-07-20]<br>[^daily-exec-slack-2026-07-20]<br>[^daily-p0-2026-07-20]<br>[^daily-noorder-2026-07-21]<br>[^paper-daily-stable-2026-07-22] |

### Intraday Stop Loss Slack

| 항목 | 값 |
| --- | --- |
| 진행 상태 | 🟢 연동 완료 |
| 최근 검증 일자 | 2026-06-30 |
| 관련 spec | 03, 04 |
| 상세 | [operation-notes](specs/03-marketconnector-ec2/operation-notes.md) |

### Daily Brief Slack

| 항목 | 값 |
| --- | --- |
| 진행 상태 | 🟢 자동 ENABLED |
| Holiday Guard | Dispatcher 공통 적용 |
| 실전 확인 | 장후 Slack 수신 |
| 최근 검증 일자 | 2026-07-15 |
| 관련 spec | 04 |
| 상세 | [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |
| 관련 근거 | [^daily-brief-2026-07-15] |

### port-view ECS Fargate

| 항목 | 값 |
| --- | --- |
| 진행 상태 | 🟢 1차 포팅 검증 완료 |
| OPS Mirror UI | `/daily-batch` consumption 확인 |
| 현재 범위 | ECS Fargate 1차 실증 상태 유지 |
| P2 고도화 | 🟠 운영 보안·표시 고도화 미수행 (범위 제외) |
| 재검토 조건 | 외부 공개 또는 다중 사용자 운영 필요 시 |
| 최근 검증 일자 | 2026-07-08 |
| 관련 spec | 05 |
| 상세 | [operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md) |
| 관련 근거 | [^ops-mirror-ui]<br>[^view-p2-2026-07-22] |

### aws-live BUY/SELL 자동화

| 항목 | 값 |
| --- | --- |
| 진행 상태 | 🔴 미진행 |
| 최근 검증 일자 | — |
| 관련 spec | 10 |
| 상세 | [followups-overview](specs/_common/followups-overview.md) |

[^tz-patch]: ECS crawler·preprocessor와 Windows KRX worker의 업무 시간대를 KST로 보정하고 raw 최신성 회복을 확인했다.
    DB timestamp 저장은 UTC를 유지하고 운영 표시는 KST로 변환한다.
    Step2B false-success 핵심 축은 2026-07-21 runner DB validator 적용으로 보완했다.
    상세: `WORKLOG.md` 2026-07-09·2026-07-21, `risk-register.md` R-DATA-010·R-DATA-017·R-AUTO-020.

[^krx-validator-2026-07-21]: KRX Program·Shortsell Python 실패 결과를 non-zero exit로 전파하고 runner 후단에 expected trade date·row count validator를 추가했다.
    기준은 Program 정확히 1건, Shortsell 최소 300건이다.
    단독 검증은 Shortsell 349건·ExitCode 0이었다.
    상세: `WORKLOG.md` 2026-07-21, `risk-register.md` R-DATA-017·R-AUTO-020, `operator-decisions.md` OD-MS-026.

[^ops-mirror-ui]: Step Functions 실행 이력의 OPS Mirror 적재와 `/daily-batch` 화면 소비를 확인했다.
    run·step log, 실행 모드, 상세 렌더링과 AWS 실행 버튼 표시를 검증했다.
    payload redaction·계좌번호 masking·UTC→KST 표시는 후속이다.
    상세: `WORKLOG.md` 2026-07-08, `operator-decisions.md` OD-MS-039, `risk-register.md` R-AUTO-038.

[^daily-brief-2026-07-15]: Daily Brief Holiday Guard를 Dispatcher로 통합하고 Builder는 메시지 생성·DB 조회에 집중하도록 정리했다.
    장후 실전 smoke는 Step Functions SUCCEEDED와 Slack 수신을 확인했다.
    07:50 장전 자동 수신은 후속이다.
    상세: `WORKLOG.md` 2026-07-15, `risk-register.md` R-AUTO-035, `operator-decisions.md` OD-MS-032·OD-MS-038.

[^daily-kst-2026-07-15]: UTC 날짜 오판으로 월요일 BUY 후보가 WEEKEND·NO_TARGET 처리되던 원인을 확인했다.
    ECS와 MarketConnector EC2의 업무 날짜 판단을 Asia/Seoul 기준으로 통일했다.
    다음 영업일 자동 재발 여부 관찰은 후속이다.
    상세: `WORKLOG.md` 2026-07-15, `risk-register.md` R-DATA-010·R-DATA-017, `operator-decisions.md` OD-MS-040.

[^daily-buy-e2e-2026-07-16]: Paper Daily BUY 2건의 주문·체결·Fill·Position E2E를 확인했다.
    초기 조회가 빨라 내부 상태가 부분체결·접수에 머문 문제는 Step13·15·16 재실행으로 정합을 복구했다.
    Step12 후 대기시간은 60초로 조정했다.
    polling·처리 건수 검증은 후속이다.
    상세: `WORKLOG.md` 2026-07-16, `risk-register.md` R-AUTO-037·R-AUTO-038·R-BROKER-004.

[^daily-recovery-2026-07-20]: Step13 다건 주문 조회 중 KIS `EGW00201`이 발생해 자동 실행이 FAILED로 종료됐다.
    조회 간 5초 대기와 제한 재시도를 적용하고 Step13~17을 수동 복구했다.
    09:01 자동 실패 이력은 유지하며 수동 복구를 자동 성공으로 기록하지 않는다.
    상세: `WORKLOG.md` 2026-07-20, `risk-register.md` R-AUTO-001·R-AUTO-037·R-AUTO-038·R-BROKER-004.

[^daily-exec-slack-2026-07-20]: Daily 실행 성공 Slack에 당일 매수·매도 체결 종목과 수량을 표시하도록 Builder와 Notifier를 보강했다.
    Builder→Notifier 수동 smoke에서 실제 Slack 수신을 확인했다.
    이 결과는 09:01 자동 execution 성공을 의미하지 않는다.
    상세: `WORKLOG.md` 2026-07-20, `risk-register.md` R-AUTO-023, `operator-decisions.md` OD-MS-009·OD-MS-030·OD-MS-031.

[^daily-p0-2026-07-20]: Step13 active 주문 polling과 Step14~16 처리 건수 fail-closed를 운영 반영했다.
    READY Plan·Order 및 전체 주문 체인 validator를 연결했다.
    Step12 실패 Slack 경로도 운영 반영했다.
    단독 smoke와 연결 검증은 완료했지만 전체 정상 자동 회차 acceptance는 P1로 남겼다.
    상세: `WORKLOG.md` 2026-07-20, `risk-register.md` R-AUTO-001·R-AUTO-023·R-AUTO-037·R-AUTO-038·R-BROKER-004.

[^daily-noorder-2026-07-21]: 정상 무주문 회차가 `P0_ValidateOrderChain`의 `--allow-no-target` 누락으로 FAILED 처리됐다.
    State Machine command를 보완하고 validator 단독 smoke에서 target=0·errors=0·ExitCode 0을 확인했다.
    09:01 자동 실패 이력은 유지하며 전체 Step12~17 재실행은 하지 않았다.
    상세: `WORKLOG.md` 2026-07-21, `risk-register.md` R-AUTO-001·R-AUTO-037, `operator-decisions.md` OD-SAFE-004·OD-MS-032.

[^approval-slack-2026-07-15]: Approval Required Slack이 서로 다른 Daily Run과 Execution Plan을 혼합하던 문제를 수정했다.
    단일 `daily_run_id` 기준으로 상태·기준일·신호·후보·Plan을 정합시키고 후보 점수를 구조화했다.
    Builder→Notifier→Slack E2E는 완료했고 다음 자동 Scheduler 수신은 후속이다.
    상세: `WORKLOG.md` 2026-07-15, `operator-decisions.md` OD-MS-031.

[^paper-daily-stable-2026-07-22]: 2026-07-22 정상 Scheduler 자동 회차에서 Step 1~11과 Step 12~17이 강제 주문·오류 유발 없이 SUCCEEDED로 완료됐다.
    Crawler 검증·Daily Run COMPLETED·Execution Plan READY·validator 2개 자동 통과·한국전력 83주 주문·체결·Position 83주 OPEN·Balance Snapshot·성공 Slack·OPS 성공 기록을 확인했다.
    P1 다음 정상 자동 회차 end-to-end 관찰을 완료 처리하고 Paper Daily를 1차 안정화 완료로 선언한다.
    aws-live 준비·장기 무장애·전체 AWS Migration 완료를 의미하지 않으며 2026-07-20·2026-07-21 FAILED 이력은 보존한다.
    상세: `WORKLOG.md` 2026-07-22, `operator-decisions.md` OD-SAFE-001.

[^view-p2-2026-07-22]: 운영자는 P2 View 운영 보안·표시 고도화(ALB·HTTPS·Route53·인증·Auto Scaling·Blue/Green·UI 고도화·외부 공개)를 현재 포트폴리오 범위에서 수행하지 않기로 결정했다.
    실패나 미완료가 아니라 의도적인 범위 제외이며 port-view는 ECS Fargate 1차 실증 상태를 유지한다.
    외부 공개 또는 다중 사용자 운영이 필요할 때 재검토한다.
    상세: `WORKLOG.md` 2026-07-22, `operator-decisions.md` OD-MS-002.

## Important Safety Notes

> <span style="color:#D1242F">**aws-live BUY/SELL 자동화는 미진행 상태를 유지한다.**</span>
>
> 본 작업공간에서는 실제 구현(코드 변경, AWS 리소스 생성, 운영 entrypoint 실행)을 하지 않는다.
>
> 각 MS의 소스 코드, README, CHANGELOG, worklog, AGENTS.md 는 본 작업공간의 spec 작업 대상이 아니며, 명시 요청이 없는 한 수정하지 않는다.
>
> `.kiro/scripts/` 하위 운영자 로컬 PowerShell wrapper 는 운영자 로컬 PC 도구이며 Kiro 자동 실행 대상이 아니고, EC2 / ECS / Batch 내부에서 실행하지도 않는다.
>
> 실제 secret, password, token, app key, app secret, 계좌번호, webhook URL 은 본 작업공간 어떤 문서에도 절대 작성하지 않으며, 민감정보가 필요한 위치에는 항상 `[REDACTED]` 만 사용한다.

## Where to Read More

- 04 Strategy Batch StepFunctions — [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md)
- 03 MarketConnector EC2 — [operation-notes](specs/03-marketconnector-ec2/operation-notes.md)
- 05 port-view ECS · Runbook — [operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md), [runbook](specs/05-port-view-ecs-and-runbook/runbook.md)
- 02 AWS Network · RDS — [operation-notes](specs/02-aws-network-and-rds/operation-notes.md)
- 06 Secrets · IAM — [operation-notes](specs/06-secrets-and-iam/operation-notes.md)
- Common — [operator-decisions](specs/_common/operator-decisions.md), [risk-register](specs/_common/risk-register.md), [followups-overview](specs/_common/followups-overview.md)

## Target Microservices

본 작업공간의 spec이 다루는 마이크로서비스는 아래 8개다.

- `port-marketconnector`
- `port-view`
- `port-interest-crawler`
- `port-interest-preprocessor`
- `port_strategy_common`
- `port_strategy_decision`
- `port_strategy_research`
- `port_strategy_execution`

각 MS의 소스 코드, README, CHANGELOG, worklog, AGENTS.md는 본 작업공간의 spec 작업 대상이 아니며, 명시 요청이 없는 한 수정하지 않는다.

## Directory Map

```
.kiro/
├── AGENTS.md           # Kiro 작업 규칙(범위, 단일 기준 문서, 보안/실행 규칙)
├── README.md           # 본 문서. .kiro 작업공간 안내
├── CHANGELOG.md        # spec 문서 변경 이력
├── WORKLOG.md          # 간단 작업 로그(누적형, 단일 파일)
└── specs/              # AWS Migration spec 및 루트 공통 참조 문서
    ├── 01-aws-migration-foundation/
    │   ├── requirements.md
    │   ├── design.md
    │   ├── tasks.md
    │   └── traceability-matrix.md
    ├── 02-aws-network-and-rds/
    │   ├── README.md
    │   ├── requirements.md
    │   ├── design.md
    │   ├── tasks.md
    │   ├── decision-matrix.md
    │   ├── runbook.md
    │   ├── validation-checklist.md
    │   ├── operation-notes.md
    │   └── traceability-matrix.md
    ├── _common/
    │   ├── operator-decisions.md
    │   ├── ms-aws-service-decision-matrix.md
    │   ├── cost-simulation.md
    │   ├── followups-overview.md
    │   ├── aws-resource-glossary.md
    │   └── risk-register.md
    └── _archive/
        └── note-aws-landscape-2021-vs-2026.md
```

## Key Documents

| 파일 | 역할 |
| --- | --- |
| `AGENTS.md` | Kiro 작업공간의 작업 규칙.<br>작업 범위, 단일 기준 문서, MS별 `AGENTS.md` 참조, spec 작성, 보안, 실행 규칙을 정의한다. |
| `README.md` | 본 문서.<br>`.kiro` 작업공간의 목적, 범위, 폴더 구조와 주요 문서 역할을 안내한다. |
| `CHANGELOG.md` | `.kiro` 안의 spec 문서 변경 이력만 기록한다.<br>각 MS의 코드·문서 변경 이력은 기록하지 않는다. |
| `WORKLOG.md` | Kiro 작업 세션의 간단 로그를 단일 파일에 누적한다.<br>날짜별 worklog 파일은 만들지 않는다. |
| `specs/` | AWS Migration spec 폴더와 루트 공통 참조 문서를 담는다. |

## Common Reference Documents

새 spec을 만들거나 기존 spec을 수정하기 전에 아래 단일 기준 문서를 먼저 확인한다.

| 파일 | 역할 |
| --- | --- |
| `specs/_common/operator-decisions.md` | 운영자 결정의 단일 기준 문서.<br>환경, 네트워크, RDS, DB schema·role, compute·service placement, security, observability, cutover, safety 결정을 관리한다. |
| `specs/_common/ms-aws-service-decision-matrix.md` | 8개 MS별 AWS 서비스 권고와 판단 근거의 단일 기준 문서.<br>컴퓨트와 orchestration 1순위 결정을 비교한다. |
| `specs/_common/cost-simulation.md` | 비용 가정과 환경별 월 예상 비용의 단일 기준 문서.<br>RDS 크기, VPC Endpoint 수, ALB·NAT 사용 여부, Fargate 사용량을 정리한다. |
| `specs/_common/followups-overview.md` | 후속 spec 03~10의 진행 순서와 의존성 맵을 관리한다. |
| `specs/_common/aws-resource-glossary.md` | AWS 용어 설명의 단일 기준 문서다. |
| `specs/_common/risk-register.md` | AWS Migration 운영·보안·비용 Risk의 단일 누적 문서.<br>후속 spec에서 새 Risk가 확인되면 동일 형식으로 추가한다. |
| `specs/_archive/note-aws-landscape-2021-vs-2026.md` | 2021년 AWS 구성과 2026년 권고안을 비교한 참고 노트다. |

## Spec Document Types

01-aws-migration-foundation, 02-aws-network-and-rds 같은 각 spec은 본문(`requirements.md`, `design.md`, `tasks.md`, 필요 시 `decision-matrix.md`)에 더해 다음 보조 문서를 가질 수 있다.

- `runbook.md` — 운영자가 AWS Console에서 한 단계씩 따라 할 수 있는 실행 절차서. 각 Step은 아래 5요소로 구성한다.

  | 항목 | 값 |
  | --- | --- |
  | 목적 | 필수 |
  | 사전 확인 | 필수 |
  | Console 작업 순서 | 필수 |
  | 생성 후 확인 | 필수 |
  | 실패 시 조치 | 필수 |
- `validation-checklist.md` — runbook을 진행한 뒤 통과 여부를 체크박스 단위로 점검하는 문서.
- `traceability-matrix.md` — 요구사항 → 설계 → 작업 → 검증 → 운영자 결정의 매핑 표.
- 루트 공통 `risk-register.md` — 모든 spec에 걸친 리스크를 단일 표로 누적 관리.

## Update Rules

- 작업공간의 목적, 폴더 구조, 주요 문서 목록이 바뀌면 본 `README.md`를 갱신한다.
- spec 구조 변경, 루트 공통 문서 변경, 신규 spec 생성, 주요 문서 재구성이 발생하면 `CHANGELOG.md`를 갱신한다.
- 의미 있는 Kiro 문서 작업 세션이 끝나면 `WORKLOG.md`에 간단히 누적한다.
- Kiro 작업 규칙(범위, 단일 기준 문서, 보안/실행 정책)이 바뀌면 `AGENTS.md`를 갱신한다.

## Security Rules

- 실제 secret, password, token, app key, app secret, 계좌번호, webhook URL은 본 작업공간 어떤 문서에도 절대 작성하지 않는다.
- 민감정보가 필요한 위치에는 항상 `[REDACTED]`만 사용한다.
- 예시, 표, 요약, 로그, 생성 문서 어디에도 민감정보 값을 출력하지 않는다.

## 운영자 로컬 PowerShell wrapper

본 작업공간의 `.kiro/scripts/` 폴더에는 운영자가 로컬 Windows PowerShell에서
Daily AWS 17-step을 단계별로 재현하기 위한 wrapper가 들어 있다.

wrapper는 **운영자 로컬 PC 도구**다.

- Kiro 자동 실행 대상이 아님
- EC2 · ECS · Batch 내부에서 실행하지 않음
- 8개 MS 저장소의 소스 · 패키징 · docs와 분리
- worklog · README · AGENTS.md · CHANGELOG 영역과 분리

본 wrapper 의 운영 정책 · 안전 기준 · 후속 책임은 아래 단일 기준 문서에 누적되어 있다. 본 README 섹션은 운영자가 한눈에 보기 위한 짧은 안내만 담는다.

| 파일 | 역할 |
|---|---|
| [`specs/_common/operator-decisions.md`](specs/_common/operator-decisions.md) | `OD-MS-023` |
| [`specs/_common/risk-register.md`](specs/_common/risk-register.md) | `R-AUTO-019` |
| [`specs/_common/followups-overview.md`](specs/_common/followups-overview.md) | 2026-06-17 세 번째 후속 메모 |
| [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) | 2026-06-17 (Daily AWS PowerShell wrapper 구현) 섹션 |

### 폴더 구조

```
.kiro/scripts/
├── run-daily-aws-paper.ps1                # main wrapper
├── daily-aws-paper.config.ps1             # region / cluster / instance id / subnet / SG / task definition / job definition / log group / output path
├── daily-aws-paper.functions.ps1          # SSM / ECS RunTask / AWS Batch SubmitJob 공통 함수 + Step registry + summary
├── run-daily-aws-paper-bundled.ps1        # 단일 파일 bundled wrapper (본 일자 미생성 / 후속 선택)
└── steps/
    ├── step-01-connector-balance.ps1
    ├── step-02-interest-crawler.ps1
    ├── step-03-preprocessor.ps1
    ├── step-04-backtest-research.ps1
    ├── step-05-backtest-report.ps1
    ├── step-06-daily-buy-signal.ps1
    ├── step-07-daily-position-signal.ps1
    ├── step-08-daily-buy-execution.ps1
    ├── step-09-daily-sell-execution.ps1
    ├── step-10-daily-auto-sell.ps1                                  # --execute = strategy execution 내부 상태 갱신 (broker 직접 제출 아님)
    ├── step-11-daily-auto-buy.ps1                                   # --execute = strategy execution 내부 상태 갱신 (broker 직접 제출 아님)
    ├── step-12-marketconnector-strategy-order-execute.ps1           # 실제 KIS paper 주문 제출 가능 — 기본 차단 / -AllowPaperOrderExecute 명시 시에만 허용
    ├── step-13-connector-order-check.ps1                            # 주문 상태 조회 / DB 상태 갱신 (신규 주문 제출 없음)
    ├── step-14-sync-sell-fill.ps1                                   # SELL fill / status DB 갱신 (broker 호출 없음)
    ├── step-15-sync-buy-fill.ps1                                    # BUY fill / status DB 갱신 (broker 호출 없음)
    ├── step-16-sync-buy-position.ps1                                # BUY position DB 갱신 (broker 호출 없음)
    └── step-17-balance-refresh.ps1                                  # balance / position snapshot refresh (신규 주문 제출 없음)
```

### 주요 옵션

| 항목 | 값 |
| --- | --- |
| `-RunDate` | 실행 기준 일자(KST).<br>미지정 시 현재 일자 사용 |
| `-Region` | 기본값 `ap-northeast-2` |
| `-Environment` | `aws-paper`만 허용<br>`aws-live` 분기는 코드에 없음 |
| 정책 근거 | R-AUTO-002 mitigation<br>OD-SAFE-002 · OD-SAFE-003 |
| `-StartStep` · `-EndStep` | 부분 실행 범위 지정 |
| 부분 실행 예시 | `-StartStep 1 -EndStep 7` |
| 단독 Step 실행 | 반드시 `-StartStep N -EndStep N` 형식 사용 |
| 잘못된 파라미터 | `-FromStep` · `-ToStep`은 wrapper에 정의되지 않음 |
| 오입력 위험 | PowerShell이 미정의 파라미터를 무시할 수 있음<br>기본 범위 `1~17`로 진입할 위험 |
| 관련 근거 | 2026-06-18 운영 메모<br>OD-MS-023 · OD-MS-025 |
| `-DryRun` | 실행 계획만 출력 |
| DryRun 실행 영향 | ECS RunTask 0건<br>Batch SubmitJob 0건<br>SSM command 0건 |
| DryRun 용도 | 모든 Step의 `FOUND` · `MISSING` 상태 확인 |
| `-AllowPaperOrderExecute` | Step 12의 KIS paper 주문 제출을 명시적으로 허용 |
| 기본값 | OFF |
| 기본 차단 구조 | wrapper 중앙 `PAPER_ORDER_GATE`<br>Step 12 내부 gate |
| 관련 Risk | R-AUTO-019 mitigation |
| 운영 원칙 | 운영자가 옵션을 직접 명시한 경우에만 허용 |

### Step 12 안전 주의사항

| 항목 | 값 |
| --- | --- |
| Step | Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` |
| 호출 스크립트 | `connector_strategy_order_execute.py --execute` |
| 주문 영향 | 실제 KIS paper 주문 제출 가능 |
| 기본 실행 상태 | 주문 제출 차단 |
| 차단 위치 | wrapper 중앙 `PAPER_ORDER_GATE`<br>Step 12 내부 gate |
| 관련 근거 | OD-MS-023<br>R-AUTO-019 |
| 주문 허용 조건 1 | `-AllowPaperOrderExecute` 명시 |
| 주문 허용 조건 2 | `Environment=aws-paper` |
| 주문 허용 조건 3 | `PORT_DB_TARGET=aws-paper` guard 통과 |
| 관련 Risk | R-AUTO-009<br>R-AUTO-010 |
| live 자동 BUY·SELL | 후속 검증과 승인 전까지 금지 |
| live 정책 | OD-SAFE-002<br>OD-SAFE-003 |

### Step 12 운영 실증과 복구 원칙

| 항목 | 값 |
| --- | --- |
| 최초 실증 일자 | 2026-06-18 |
| 실행 결과 | 운영자가 paper BUY 4건 제출 |
| 실행 조건 | `-AllowPaperOrderExecute` 직접 명시 |
| 최초 장애 | KIS paper API read timeout |
| 복구 방식 | 통제된 `REQUESTED` 복구 후 재시도 |
| 실증 Risk | R-BROKER-004 mitigation 1차 실증 |
| 금지 사항 | 상태 확인 없는 단순 재실행 |
| 금지 이유 | broker 중복 주문 위험 |
| 필수 절차 | 사전 점검 패턴 적용 |
| 상세 근거 | [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) |

### Step 10~17 책임 경계

| 항목 | 값 |
| --- | --- |
| Step 10 · Step 11 `--execute` | strategy execution 내부 상태 생성·갱신 |
| broker 직접 제출 | 없음 |
| KIS 직접 제출 | 없음 |
| 관련 결정 | OD-MS-016 |
| Step 13~17 | DB 상태 갱신·조회·snapshot refresh |
| 신규 broker 주문 | 0건 |
| 신규 KIS 주문 | 0건 |
| Step 13 실증 | broad summary fallback 자동 skip |
| 체결 반영 방식 | `--code` · `--order-no` · `--no-broad` 단건 조회 |
| 관련 Risk | R-AUTO-018 |
| Step 16 보강 | 추가매수 시 unique constraint 충돌 해소 |
| 수정 파일 | `execution_sync_buy_position.py` |
| 수정 방식 | merge 처리 |
| 관련 결정·Risk | OD-MS-024<br>R-DATA-012 |
| 상세 근거 | [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) |

### Step 13 안전 주의사항

| 항목 | 값 |
| --- | --- |
| Step | Step 13 `CONNECTOR_ORDER_CHECK` |
| 실행 방식 | MarketConnector EC2 SSM RunCommand |
| 호출 스크립트 | `connector_order_check.py` |
| 신규 주문 제출 | 없음 |
| 책임 | 주문 상태 조회<br>DB 상태 갱신 |
| 운영 보강 일자 | 2026-06-18 |
| 기본 동작 변경 | broad 일괄 조회 → active 주문 단건 순차 조회 |
| 관련 결정 | OD-MS-025 |
| wrapper 책임 | Step 13 orchestration |
| 조회 방식 책임 | `connector_order_check.py` 내부 |
| 관련 Risk | R-AUTO-018 |

#### `connector_order_check.py` 주요 옵션

| 항목 | 값 |
| --- | --- |
| `--broad` | legacy·진단용<br>기본값 미사용 |
| `--active-limit` | active 주문 단건 순차 조회 최대 건수 |
| `--code {ticker_code}` | 명시 종목코드 |
| `--order-no {broker_order_no}` | 명시 broker 주문번호 |
| `--no-broad` | broad 조회 비활성 |
| 명시 주문 조회 조합 | `--code` + `--order-no` + `--no-broad` |

#### Summary fallback 적용 기준

| 항목 | 값 |
| --- | --- |
| 다건 active 주문 | KIS `inquire-daily-ccld` summary fallback을 DB 반영 근거로 사용하지 않음 |
| 허용 조건 | `connector_order_request` 후보가 주문번호·종목코드 기준 1건으로 확정 |
| 관련 Risk | R-AUTO-018 mitigation |
| 상세 응답 | [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) |

#### Step 13 단독 실행

| 항목 | 값 |
| --- | --- |
| 검증 일자 | 2026-06-18 |
| 실행 명령 | `./run-daily-aws-paper.ps1 -Environment aws-paper -RunDate 2026-06-18 -StartStep 13 -EndStep 13` |
| 결과 | Step 13 `COMPLETED` |
| 사용 파라미터 | `-StartStep` · `-EndStep` |
| 금지 파라미터 | `-FromStep` · `-ToStep` |
| 오입력 위험 | 기본 `1~17` 범위 진입 가능 |
| 상세 결과 | [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) |

### Windows KRX crawler worker 와 Step 2 동작

- Step 2 (`INTEREST_CRAWLER`) 는 아래 3개 흐름을 함께 처리한다.
  - (a) non-GUI ECS · Fargate Task Definition `portfolio-paper-interest-crawler:7` RunTask
  - (b) Windows KRX crawler worker 의 `Portfolio-KRX-Worker-Daily` Scheduled Task trigger
  - (c) **KRX raw DB validation** (`interest_krx_raw_validate_daily.py` SSM step `INTEREST_CRAWLER_KRX_DB_VALIDATE`)
- **2026-06-21 성공판정 강화** (OD-MS-026 신규 · R-AUTO-020 신규 mitigation 정합) — Scheduled Task trigger 성공만으로 Step 2 SUCCESS 처리하지 않는다.
- 아래 5-단계 gate 를 모두 통과해야 SUCCESS 처리한다.

  | Step | 결과 |
  |---|---|
  | 1 | Scheduled Task 상태 polling |
  | 2 | `Last Result` 검사 |
  | 3 | KRX worker 로그 확인 |
  | 4 | `ExpectedKrxRawDate` 기준 `interest_program_raw` DB 검증 |
  | 5 | `ExpectedKrxRawDate` 기준 `interest_shortsell_raw` DB 검증 |
- 상세 gate 정의 및 실행 결과는 [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) 참조.
- **crawler worker EC2 fail-closed** (R-AUTO-016 mitigation 갱신) — Windows KRX crawler worker 의 EC2 instance state 가 `running` 이 아니면 Step 2 는 즉시 실패 처리한다.
- 이전 "wrapper 안에서 KRX GUI Scheduled Task trigger 를 자동 skip" 동작은 본 일자에 폐지.
- 실패 메시지에 `instanceId` · `state` 출력 · KRX GUI worker · DB validation 미수행 상태에서 Step 2 SUCCESS 진입 차단.
- KRX GUI 경로는 SSM direct python 실행이 아니라 Windows Administrator interactive Scheduled Task 흐름을 유지한다 (OD-MS-022 · OD-MS-026 정합).
  - `schtasks /Run /TN Portfolio-KRX-Worker-Daily` → `powershell.exe -ExecutionPolicy Bypass -File C:\portfolio\run_krx_worker_daily.ps1`
- non-GUI ECS RunTask 는 worker 상태와 무관하게 진행하되, `containerOverrides.environment` 로 아래 4개 환경변수를 주입한다(Windows · Linux 인코딩 차이 완화 · UTF-8 기준 명확화).

  | 항목 | 값 |
  |---|---|
  | `TEMP` | `/tmp` |
  | `TMP` | `/tmp` |
  | `PYTHONUTF8` | `1` |
  | `PYTHONIOENCODING` | `utf-8` |
- **Step 2 단독 실행 명령** — `.\run-daily-aws-paper.ps1 -Environment aws-paper -RunDate 2026-06-20 -StartStep 2 -EndStep 2`.
- 2026-06-21 단독 실행 검증 통과 (Status `SUCCESS`).
- `-FromStep` · `-ToStep` 미정의 파라미터로 실수 입력 시 default 1 · 17 진입 위험이 있다.
- 반드시 `-StartStep` · `-EndStep` 형식만 사용한다.
- 상세 RunId · Runner · DB after-check 결과는 [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) 참조.

### bundled wrapper

- `run-daily-aws-paper-bundled.ps1` 단일 파일 bundled wrapper 는 본 일자 미생성.
- 17개 step 별 파일 검증 완료 후 필요 시 생성 가능하다.
- **기본 개발 · 검증 · 운영 기준은 분리 파일 구조를 유지한다** (OD-MS-023 정합).

### 산출물 위치

- run id 별 로그 · overrides JSON · summary 는 `C:\Temp\portfolio-daily-aws-paper\<run-id>\` 하위 `logs/` · `overrides/` · `summary/run-summary.txt` 에 저장한다.
- wrapper summary · overrides JSON · SSM stdout · stderr 파일에는 아래 민감정보 카테고리 평문 출력 **0건**(R-DOCS-001 [2026-06-17 wrapper 보강] mitigation 정합).

  | 항목 | 값 |
  | --- | --- |
  | secret value | 평문 출력 0건 |
  | KIS app key | 평문 출력 0건 |
  | KIS app secret | 평문 출력 0건 |
  | 계좌번호 | 평문 출력 0건 |
  | token | 평문 출력 0건 |
  | RDS password | 평문 출력 0건 |
  | RDS endpoint hostname | 평문 출력 0건 |
