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

- 자세한 일자별 결과 · 후속 인계는 `specs/_common/followups-overview.md` 와 `WORKLOG.md` 에 누적된다.
- 각 spec 의 운영 결과는 해당 spec 의 `operation-notes.md` 에 누적된다.
- "완료" 표기는 실제 데이터 적재 · 최신성 검증 또는 end-to-end 상태 전이 검증까지 확인된 경우에만 사용한다.
- 정합 결정: `OD-MS-020` · `OD-MS-021` · `OD-MS-023` · `OD-MS-026` · `OD-MS-027` · `OD-MS-028` · `OD-MS-029`.

| 영역 | 진행 상태 | 최근 검증 일자 | 관련 spec | 링크 |
|---|---|---|---|---|
| Paper Daily Step 1-11 | 🟢 자동 ENABLED | 2026-07-01 | 04 | [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |
| Paper Daily Step 12-17 | 🟢 자동 ENABLED | 2026-07-01 | 04 | [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |
| Intraday Stop Loss Slack | 🟢 연동 완료 | 2026-06-30 | 03, 04 | [operation-notes](specs/03-marketconnector-ec2/operation-notes.md) |
| Daily Brief Slack | 🟢 자동 ENABLED | 2026-06-30 | 04 | [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |
| port-view ECS Fargate | 🟢 1차 포팅 검증 | 2026-06-30 | 05 | [operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md) |
| aws-live BUY/SELL 자동화 | 🔴 미진행 | — | 10 | [followups-overview](specs/_common/followups-overview.md) |

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

- `AGENTS.md` — Kiro 작업공간의 작업 규칙. 범위, 루트 참조 문서, 단일 기준 문서 규칙, MS별 AGENTS.md 참조 규칙, spec 작성 규칙, 보안 규칙, 실행 규칙을 정의한다.
- `README.md` — 본 문서. `.kiro` 작업공간의 목적, 범위, 폴더 구조, 주요 문서 역할을 안내한다.
- `CHANGELOG.md` — `.kiro` 안의 spec 문서 변경 이력만 간단히 기록한다. 각 MS의 코드/문서 변경 이력은 기록하지 않는다.
- `WORKLOG.md` — Kiro 작업 세션의 간단 로그를 하나의 파일에 누적한다. 날짜별 worklog 파일을 만들지 않는다.
- `specs/` — AWS Migration spec 폴더와 루트 공통 참조 문서를 담는다.

## Common Reference Documents

`specs/` 아래의 다음 문서는 모든 spec이 공유하는 단일 기준 문서다. 새 spec을 만들거나 기존 spec을 수정하기 전에 항상 먼저 확인한다.

- `specs/_common/operator-decisions.md` — 운영자 결정사항의 단일 기준 문서. 환경, 네트워크, RDS, DB schema/role, compute/service placement, security, observability, cutover, safety 결정을 누적 관리한다.
- `specs/_common/ms-aws-service-decision-matrix.md` — 8개 MS별 AWS 서비스 권고와 판단 근거의 단일 기준 문서. 컴퓨트, orchestration 1순위 결정의 비교 표를 담는다.
- `specs/_common/cost-simulation.md` — 비용 가정, 환경별 월 예상 비용의 단일 기준 문서. RDS 크기, VPC Endpoint 수, ALB/NAT 사용 여부, Fargate 사용량 가정을 정리한다.
- `specs/_common/followups-overview.md` — 후속 spec(03 ~ 10) 진행 순서와 의존성 맵의 단일 기준 문서.
- `specs/_common/aws-resource-glossary.md` — AWS 용어 설명의 단일 기준 문서.
- `specs/_common/risk-register.md` — AWS Migration 운영 / 보안 / 비용 리스크의 단일 누적 기록 문서. 후속 spec(03 ~ 10)에서 새 리스크가 식별되면 동일 형식으로 누적.
- `specs/_archive/note-aws-landscape-2021-vs-2026.md` — 2021년 AWS 구성과 2026년 권고안 비교 노트.

## Spec Document Types

01-aws-migration-foundation, 02-aws-network-and-rds 같은 각 spec은 본문(`requirements.md`, `design.md`, `tasks.md`, 필요 시 `decision-matrix.md`)에 더해 다음 보조 문서를 가질 수 있다.

- `runbook.md` — 운영자가 AWS Console에서 한 단계씩 따라 할 수 있는 실행 절차서. 각 Step은 아래 5요소로 구성한다.

  | Step 구성 요소 |
  |---|
  | 목적 |
  | 사전 확인 |
  | Console 작업 순서 |
  | 생성 후 확인 |
  | 실패 시 조치 |
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

본 작업공간의 `.kiro/scripts/` 폴더에는 운영자가 로컬 Windows PowerShell 에서 Daily AWS 17-step 을 단계별로 재현하기 위한 wrapper 가 들어 있다. wrapper 자체는 **운영자 로컬 PC 도구**이며 Kiro 자동 실행 대상이 아니고, EC2 · ECS · Batch 내부에서 실행하지도 않는다. 8개 MS 저장소의 소스 · 패키징 · docs · worklog · README · AGENTS.md · CHANGELOG 영역과 분리된다.

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

- `-RunDate` : 실행 기준 일자(KST). 미지정 시 현재 일자.
- `-Region` : 기본 `ap-northeast-2`.
- `-Environment` : `aws-paper` 만 허용. aws-live 분기는 코드 레벨에서 미존재(R-AUTO-002 mitigation 정합 / OD-SAFE-002 / OD-SAFE-003 정합).
- `-StartStep` / `-EndStep` : 부분 실행 지원 (예: `-StartStep 1 -EndStep 7` safe subset).
- **파라미터 이름 주의 — `-FromStep` / `-ToStep` 은 wrapper 에 존재하지 않는 미정의 파라미터다.** 실수 입력 시 PowerShell 이 silent 무시하고 wrapper 가 default `-StartStep 1 -EndStep 17` 로 진입할 위험이 있다 (2026-06-18 운영 메모 · OD-MS-023 · OD-MS-025 정합).
- Step 13 / Step 16 등 단독 step 검증 시에는 반드시 `-StartStep N -EndStep N` 형식으로 명시한다.
- `-DryRun` : 실행 계획만 출력 / 실제 ECS RunTask · Batch SubmitJob · SSM command 제출 0건. 모든 step 의 `FOUND` / `MISSING` 상태 점검 용도.
- `-AllowPaperOrderExecute` : Step 12 KIS paper 주문 제출을 명시적으로 허용. **본 옵션 없이는 Step 12 가 wrapper 중앙 PAPER_ORDER_GATE + Step 12 내부 이중 gate 로 차단됨**(R-AUTO-019 mitigation). 옵션은 운영자가 직접 명시해야 하며 default OFF.

### Step 12 안전 주의사항

- Step 12 (`MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) 는 `connector_strategy_order_execute.py --execute` 를 호출 / 실제 KIS paper 주문 제출 가능 step.
- 기본 실행 모드에서는 wrapper 중앙 `PAPER_ORDER_GATE` 와 Step 12 내부 이중 gate 가 동시에 차단(OD-MS-023 / R-AUTO-019 정합).
- `-AllowPaperOrderExecute` 명시 + `aws-paper` 환경 + `PORT_DB_TARGET=aws-paper` guard 통과 시에만 실주문 진입(R-AUTO-009 / R-AUTO-010 정합).
- **2026-06-18 첫 실주문 사용 사례** — 운영자가 `-AllowPaperOrderExecute` 명시로 실 paper BUY 4건 제출.
- 1차 시도 KIS paper API read timeout 후 통제된 REQUESTED 복구를 거쳐 재시도 성공 (R-BROKER-004 신규 mitigation 1차 실증).
- **단순 재실행으로 복구하면 broker 중복 주문 위험이 있으므로 반드시 사전 점검 패턴을 따른다.** 상세 DB after-check 이력은 [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) 참조.
- live 자동 BUY / SELL E2E 검증은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지.
- Step 10 / Step 11 의 `--execute` 는 strategy execution 내부 상태 생성 · 갱신 의미다.
- broker · KIS 직접 제출 아님(OD-MS-016 책임 분리 정합).
- Step 13 ~ Step 17 은 모두 DB 상태 갱신 · 조회 · snapshot refresh 책임이며 신규 broker · KIS 주문 제출 0건.
- **2026-06-18 Step 13 broad 조회 summary fallback 자동 skip → 단건 `--code` · `--order-no` · `--no-broad` 조회로만 체결 반영 1차 실증**(R-AUTO-018 정합).
- **Step 16 의 추가매수 케이스에서 `strategy_position_state` unique constraint 충돌은 `execution_sync_buy_position.py` merge 패치로 해소**(OD-MS-024 신규 · R-DATA-012 신규 mitigation 1차 실증).
- 상세 broad 응답 케이스 및 DB after-check 이력은 [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) 참조.

### Step 13 안전 주의사항

- Step 13 (`CONNECTOR_ORDER_CHECK`) 는 MarketConnector EC2 SSM RunCommand 로 `connector_order_check.py` 를 호출 / 신규 broker · KIS 주문 제출 없음 / 주문 상태 조회 + DB 상태 갱신 step.
- **2026-06-18 운영 안전성 강화 — 기본 실행 모드 변경** (OD-MS-025 신규).
- `connector_order_check.py` 의 기본 동작이 broad 체결조회 일괄 처리 → active 주문 단건 순차 조회로 변경됨.
- wrapper ps1 은 Step 13 실행 orchestration 만 담당하고, 체결조회 방식 제어는 `connector_order_check.py` 내부 책임으로 분리 (R-AUTO-018 [2026-06-18 추가 보강] 정합).
- `connector_order_check.py` 주요 옵션은 아래 표를 참조한다.

  | 용어 | 설명 |
  |---|---|
  | `--broad` | legacy · 진단용 · 기본값 미사용 |
  | `--active-limit` | active 주문 단건 순차 조회 최대 처리 건수 |
  | `--code {ticker_code} --order-no {broker_order_no} --no-broad` | 명시 주문 조회 조합 |
- **다건 active 주문 상태에서 KIS `inquire-daily-ccld` summary fallback 은 DB 반영 근거로 사용하지 않는다.**
- `connector_order_request` 후보가 주문번호 · 종목코드 기준으로 1건 확정된 경우에만 summary fallback 허용 (R-AUTO-018 mitigation 정합).
- 상세 응답 케이스는 [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) 참조.
- **2026-06-18 Step 13 단독 실행 검증** — `.\run-daily-aws-paper.ps1 -Environment aws-paper -RunDate 2026-06-18 -StartStep 13 -EndStep 13` 으로 통과 (Step 13 COMPLETED).
- `-FromStep` · `-ToStep` 미정의 파라미터로 실수 입력 시 default 1 · 17 진입 위험이 있다.
- 반드시 `-StartStep` · `-EndStep` 형식만 사용한다.
- 상세 SSM 실행 결과는 [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) 참조.

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

  | 민감정보 카테고리 |
  |---|
  | secret value |
  | KIS app key |
  | KIS app secret |
  | 계좌번호 |
  | token |
  | RDS password |
  | RDS endpoint hostname |
