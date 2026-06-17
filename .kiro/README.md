# PORT-STRATEGY-AI AWS Migration Specs

## 목적

이 `.kiro` 작업공간은 PORT-STRATEGY-AI 포트폴리오의 AWS Migration 관련 spec을 작성하고 관리하기 위한 공간이다.

디렉터리 자체는 `port-view` 안에 있지만, 여기서 다루는 범위는 `port-view` 단일 MS가 아니라 PORT-STRATEGY-AI의 8개 MS 전체다. 따라서 본 작업공간은 cross-service spec 저장소로 동작한다.

## 대상 8개 MS

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

## 폴더 구조

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

## 주요 파일

- `AGENTS.md` — Kiro 작업공간의 작업 규칙. 범위, 루트 참조 문서, 단일 기준 문서 규칙, MS별 AGENTS.md 참조 규칙, spec 작성 규칙, 보안 규칙, 실행 규칙을 정의한다.
- `README.md` — 본 문서. `.kiro` 작업공간의 목적, 범위, 폴더 구조, 주요 문서 역할을 안내한다.
- `CHANGELOG.md` — `.kiro` 안의 spec 문서 변경 이력만 간단히 기록한다. 각 MS의 코드/문서 변경 이력은 기록하지 않는다.
- `WORKLOG.md` — Kiro 작업 세션의 간단 로그를 하나의 파일에 누적한다. 날짜별 worklog 파일을 만들지 않는다.
- `specs/` — AWS Migration spec 폴더와 루트 공통 참조 문서를 담는다.

## 루트 공통 문서

`specs/` 아래의 다음 문서는 모든 spec이 공유하는 단일 기준 문서다. 새 spec을 만들거나 기존 spec을 수정하기 전에 항상 먼저 확인한다.

- `specs/_common/operator-decisions.md` — 운영자 결정사항의 단일 기준 문서. 환경, 네트워크, RDS, DB schema/role, compute/service placement, security, observability, cutover, safety 결정을 누적 관리한다.
- `specs/_common/ms-aws-service-decision-matrix.md` — 8개 MS별 AWS 서비스 권고와 판단 근거의 단일 기준 문서. 컴퓨트, orchestration 1순위 결정의 비교 표를 담는다.
- `specs/_common/cost-simulation.md` — 비용 가정, 환경별 월 예상 비용의 단일 기준 문서. RDS 크기, VPC Endpoint 수, ALB/NAT 사용 여부, Fargate 사용량 가정을 정리한다.
- `specs/_common/followups-overview.md` — 후속 spec(03 ~ 10) 진행 순서와 의존성 맵의 단일 기준 문서.
- `specs/_common/aws-resource-glossary.md` — AWS 용어 설명의 단일 기준 문서.
- `specs/_common/risk-register.md` — AWS Migration 운영 / 보안 / 비용 리스크의 단일 누적 기록 문서. 후속 spec(03 ~ 10)에서 새 리스크가 식별되면 동일 형식으로 누적.
- `specs/_archive/note-aws-landscape-2021-vs-2026.md` — 2021년 AWS 구성과 2026년 권고안 비교 노트.

## Spec 보조 문서 (runbook / validation / traceability / risk)

01-aws-migration-foundation, 02-aws-network-and-rds 같은 각 spec은 본문(`requirements.md`, `design.md`, `tasks.md`, 필요 시 `decision-matrix.md`)에 더해 다음 보조 문서를 가질 수 있다.

- `runbook.md` — 운영자가 AWS Console에서 한 단계씩 따라 할 수 있는 실행 절차서. 각 Step은 목적 / 사전 확인 / Console 작업 순서 / 생성 후 확인 / 실패 시 조치로 구성한다.
- `validation-checklist.md` — runbook을 진행한 뒤 통과 여부를 체크박스 단위로 점검하는 문서.
- `traceability-matrix.md` — 요구사항 → 설계 → 작업 → 검증 → 운영자 결정의 매핑 표.
- 루트 공통 `risk-register.md` — 모든 spec에 걸친 리스크를 단일 표로 누적 관리.

## Spec 작성 원칙

- 각 spec은 자기 범위에만 집중한다.
- 루트 공통 문서의 큰 내용을 그대로 복사하지 않고, 필요한 부분만 요약한 뒤 루트 공통 문서를 참조한다.
- 모든 결정사항은 `operator-decisions.md`와 일치시킨다. 결정이 바뀌면 spec에서 임의로 변경하지 않고 `operator-decisions.md`부터 갱신한다.
- 본 작업공간에서는 실제 구현(코드 변경, AWS 리소스 생성, 운영 entrypoint 실행)을 하지 않는다. 구현이 필요한 절차는 spec 또는 runbook으로만 작성한다.

## 보안 원칙

- 실제 secret, password, token, app key, app secret, 계좌번호, webhook URL은 본 작업공간 어떤 문서에도 절대 작성하지 않는다.
- 민감정보가 필요한 위치에는 항상 `[REDACTED]`만 사용한다.
- 예시, 표, 요약, 로그, 생성 문서 어디에도 민감정보 값을 출력하지 않는다.

## 변경 시 갱신 위치

- 작업공간의 목적, 폴더 구조, 주요 문서 목록이 바뀌면 본 `README.md`를 갱신한다.
- spec 구조 변경, 루트 공통 문서 변경, 신규 spec 생성, 주요 문서 재구성이 발생하면 `CHANGELOG.md`를 갱신한다.
- 의미 있는 Kiro 문서 작업 세션이 끝나면 `WORKLOG.md`에 간단히 누적한다.
- Kiro 작업 규칙(범위, 단일 기준 문서, 보안/실행 정책)이 바뀌면 `AGENTS.md`를 갱신한다.

## 현재 진행 상태 요약 (2026-06-17 기준)

본 섹션은 운영자가 한눈에 보기 위한 짧은 진행 상태 요약이다. 자세한 일자별 결과 / 후속 인계는 `specs/_common/followups-overview.md` 와 `WORKLOG.md` 에 누적되어 있고, 각 spec 의 운영 결과는 해당 spec 의 `operation-notes.md` 에 누적된다. 본 섹션의 "완료" 표기는 실제 데이터 적재 / 최신성 검증 또는 end-to-end 상태 전이 검증까지 확인된 경우에만 사용한다(OD-MS-020 / OD-MS-021 정합).

- 02 `aws-network-and-rds` — VPC / Subnet / RDS / DB Role 1차 적용 완료. SSM Port Forwarding 표준 경유지 1차 운영(OD-NET-010 / OD-NET-011). **2026-06-17 Daily AWS 17-step E2E 흐름에서 `execution_app` 의 `interest` schema USAGE / table SELECT / sequence / default privileges 보정(Step 8 1차 실패 → 운영자 GRANT 보정 후 통과)과 `marketconnector_app` 의 `legacy` schema USAGE / `legacy.holdings` DML / sequence / database search_path(`connector, execution, legacy, reference, public`) 보정(Step 17 1차 실패 → 보정 후 재실행 통과) 1차 실증.** 정식 매트릭스 갱신은 후속(R-DATA-005 / R-DATA-011 정합).
- 03 `marketconnector-ec2` — EC2 + EIP 정상 운영 모드 1차 검증 완료. 2026-06-15 / 2026-06-17 첫 세션 `CONNECTOR_BALANCE` 조회성 검증 통과. **2026-06-17 Daily AWS 17-step E2E 에서 Step 1 `CONNECTOR_BALANCE`(`as_of_date 2026-06-17`) / Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`(KIS paper BUY 4건 제출 성공 / `execution_order` id `26 ~ 29` SUBMITTED / `connector_order_request` id `34 ~ 37` 생성 / broker_order_no 운영 식별자 4종 사실 기록) / Step 13 `CONNECTOR_ORDER_CHECK`(`inquire-daily-ccld` `output1 empty` + `output2 aggregate summary` 응답 형태 식별 → summary fallback guard 패치 후 `broker_order_no` 별 단건 조회로 체결 동기화 / `connector_order_request 34 ~ 37` FILLED / `connector_fill 26 ~ 29` 생성) / Step 17 `BALANCE_REFRESH`(legacy 권한 / search_path 보정 후 재실행 통과 / `connector_position_snapshot` 4종목 최신) end-to-end 1차 통과.** SELL / 취소 / 정정 호출 0건 / aws-live 작업 0건.
- 06 `secrets-and-iam` — paper 환경 Secrets / IAM 1차 적용 완료. MS 별 Secret 접근 분리(OD-SEC-006) 정책 1차 실증. **2026-06-17 17-step E2E 에서 MarketConnector EC2 Instance Role 기반 Secrets Manager / SSM Parameter read 1차 실증(JSON SecretString 내부 key 추출 정책 정합 / value 평문 출력 0건). app role DB 권한 보정(`execution_app` interest / `marketconnector_app` legacy)은 02 spec / 06 spec 양쪽 사실 기록.**
- 08 `interest-crawler-and-preprocessor-ecs` — Interest Crawler hybrid 구조 완료(non-GUI ECS Fargate + KRX Windows EC2 worker). Preprocessor ECS Fargate Task 유지. **2026-06-17 17-step E2E 의 Step 2 `INTEREST_CRAWLER` / Step 3 `PREPROCESSOR` 통과(`interest_program_raw` / `interest_shortsell_raw` 2026-06-16 / `pre_total_market_daily_feature` / `pre_total_stock_daily_feature` 2026-06-16). crawler worker stop 요청 완료. KRX GUI = Windows interactive desktop session 기반 유지.**
- 04 `strategy-batch-stepfunctions` — Strategy Decision 2개 Task Definition / Strategy Execution 단일 Task Definition + command override 검증 완료. **2026-06-17 17-step E2E 의 Step 6 `DAILY_BUY_SIGNAL`(BUY READY 4건 / 후보 `282330` / `004990` / `003490` / `088350`) / Step 7 `DAILY_POSITION_SIGNAL`(positions 0 정상 skip) / Step 8 `DAILY_BUY_EXECUTION`(`execution_plan_id 92`) / Step 9 `DAILY_SELL_EXECUTION` 정상 skip / Step 10 `DAILY_AUTO_SELL` 정상 skip / Step 11 `DAILY_AUTO_BUY`(`READY -> REQUESTED` 4건 / `total_qty 378` / `total_target_amount 6908189.40`) / Step 14 `SYNC_SELL_FILL` 정상 skip / Step 15 `SYNC_BUY_FILL`(execution_order FILLED 전환) / Step 16 `SYNC_BUY_POSITION`(`strategy_position_state` 4건 OPEN / `position_state_id 6 ~ 9`) end-to-end 1차 통과(OD-MS-016 책임 분리 정합).** Step Functions / EventBridge Scheduler 정기 트리거는 후속.
- 09 `strategy-research-batch` — AWS Batch Compute Environment / Job Queue / Job Definition 1차 실증 통과. **2026-06-17 17-step E2E 의 Step 4 `BACKTEST_RESEARCH`(SUCCEEDED / latest result date `2026-06-16` / Sharpe Ratio `2.68`) / Step 5 `BACKTEST_REPORT`(SUCCEEDED / S3 report 4개) 통과(OD-MS-019 정합).** heavy 분류 SubmitJob 0건 유지(R-AUTO-015 정합).
- 05 / 07 / 10 — 미진행. View Daily Batch 의 ProcessBuilder → AWS Batch · ECS RunTask 매핑(05) / Step Functions state machine + EventBridge Scheduler / CI/CD OIDC(07) / aws-live cutover(10) 모두 후속 분리. 본 일자에 17-step 흐름은 운영자 직접 수동 오케스트레이션으로 통과 — 정기 트리거 / 관제 UI 격상은 별도 phase 책임.
- aws-live — **미진행** / 본 일자까지 모든 검증은 `aws-paper` 한정.
- Backend AWS E2E 17-step 진행 상태(2026-06-17) — **1 ~ 17 모두 완료 또는 정상 skip**(1번 `CONNECTOR_BALANCE` / 2번 `INTEREST_CRAWLER` / 3번 `PREPROCESSOR` / 4번 `BACKTEST_RESEARCH` / 5번 `BACKTEST_REPORT` / 6번 `DAILY_BUY_SIGNAL` / 7번 `DAILY_POSITION_SIGNAL` 정상 skip / 8번 `DAILY_BUY_EXECUTION` / 9번 `DAILY_SELL_EXECUTION` 정상 skip / 10번 `DAILY_AUTO_SELL` 정상 skip / 11번 `DAILY_AUTO_BUY` / 12번 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` / 13번 `CONNECTOR_ORDER_CHECK` / 14번 `SYNC_SELL_FILL` 정상 skip / 15번 `SYNC_BUY_FILL` / 16번 `SYNC_BUY_POSITION` / 17번 `BALANCE_REFRESH`). 실제 broker 호출은 KIS paper BUY 4건 한정. SELL / 취소 / 정정 / 추가 `--execute` 0건. fill · position sync 자동 재시도 0건(OD-SAFE-001 ~ OD-SAFE-004 / OD-MS-021 정합). View 구현 / Step Functions 정기 트리거 / aws-live cutover 는 후속 phase 책임.
- **Daily AWS PowerShell wrapper 기준선 수립(2026-06-17)** — 17-step E2E 완료 후속으로 운영자 로컬 Windows PowerShell wrapper 분리 파일 구조 1차 수립(`.kiro/scripts/run-daily-aws-paper.ps1` + `daily-aws-paper.config.ps1` + `daily-aws-paper.functions.ps1` + `steps/step-01-...` ~ `steps/step-17-...`). 전체 1~17 DryRun FOUND 17건 통과 / Step 12 PAPER_ORDER_GATE 안전 차단 검증 통과 / Step 1 단독 SSM 1차 검증 통과(`connector_balance.py` / Success / 보유종목 0건) / parser validation 20건 OK / 위험 키워드 safety grep 통과. wrapper 기반 전체 1~17 실제 재실행 / Step 12 `-AllowPaperOrderExecute` 사용 실주문 / bundled wrapper(`run-daily-aws-paper-bundled.ps1`) 모두 본 일자 미수행 — 후속 분리(OD-MS-023 / R-AUTO-019 정합).

## 운영자 로컬 PowerShell wrapper

본 작업공간의 `.kiro/scripts/` 폴더에는 운영자가 로컬 Windows PowerShell 에서 Daily AWS 17-step 을 단계별로 재현하기 위한 wrapper 가 들어 있다. wrapper 자체는 **운영자 로컬 PC 도구**이며 Kiro 자동 실행 대상이 아니고, EC2 / ECS / Batch 내부에서 실행하지도 않는다. 8개 MS 소스 / 패키징 / docs / worklog / README / AGENTS.md / CHANGELOG 영역과 분리된다.

본 wrapper 의 운영 정책 / 안전 기준 / 후속 책임은 [`specs/_common/operator-decisions.md`](specs/_common/operator-decisions.md) 의 OD-MS-023, [`specs/_common/risk-register.md`](specs/_common/risk-register.md) 의 R-AUTO-019, [`specs/_common/followups-overview.md`](specs/_common/followups-overview.md) 2026-06-17 세 번째 후속 메모, [`specs/03-marketconnector-ec2/operation-notes.md`](specs/03-marketconnector-ec2/operation-notes.md) 2026-06-17 (Daily AWS PowerShell wrapper 구현) 섹션에 단일 기준 문서로 누적되어 있다. 본 README 섹션은 운영자가 한눈에 보기 위한 짧은 안내만 담는다.

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
- `-StartStep` / `-EndStep` : 부분 실행 지원(예: `-StartStep 1 -EndStep 7` safe subset).
- `-DryRun` : 실행 계획만 출력 / 실제 ECS RunTask · Batch SubmitJob · SSM command 제출 0건. 모든 step 의 `FOUND` / `MISSING` 상태 점검 용도.
- `-AllowPaperOrderExecute` : Step 12 KIS paper 주문 제출을 명시적으로 허용. **본 옵션 없이는 Step 12 가 wrapper 중앙 PAPER_ORDER_GATE + Step 12 내부 이중 gate 로 차단됨**(R-AUTO-019 mitigation). 옵션은 운영자가 직접 명시해야 하며 default OFF.

### Step 12 안전 주의사항

- Step 12 (`MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) 는 `connector_strategy_order_execute.py --execute` 를 호출 / 실제 KIS paper 주문 제출 가능 step.
- 기본 실행 모드에서는 wrapper 중앙 `PAPER_ORDER_GATE` 와 Step 12 내부 이중 gate 가 동시에 차단(OD-MS-023 / R-AUTO-019 정합).
- `-AllowPaperOrderExecute` 명시 + `aws-paper` 환경 + `PORT_DB_TARGET=aws-paper` guard 통과 시에만 실주문 진입(R-AUTO-009 / R-AUTO-010 정합).
- live 자동 BUY / SELL E2E 검증은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지.
- Step 10 / Step 11 의 `--execute` 는 strategy execution 내부 상태 생성 / 갱신 의미 / broker · KIS 직접 제출 아님(OD-MS-016 책임 분리 정합).
- Step 13 / Step 14 / Step 15 / Step 16 / Step 17 은 모두 DB 상태 갱신 / 조회 / snapshot refresh 책임 / 신규 broker · KIS 주문 제출 0건.

### Windows KRX crawler worker 와 Step 2 동작

- Step 2 (`INTEREST_CRAWLER`) 는 (a) non-GUI ECS / Fargate Task Definition `portfolio-paper-interest-crawler:7` RunTask 와 (b) Windows KRX crawler worker 의 `Portfolio-KRX-Worker-Daily` Scheduled Task trigger 두 흐름을 함께 처리한다.
- Windows KRX crawler worker 의 EC2 instance state 가 `running` 이 아니면 wrapper 안에서 KRX GUI Scheduled Task trigger 를 자동 skip 한다(R-AUTO-016 [2026-06-17 wrapper 보강] mitigation 정합 / OD-MS-022 정합).
- non-GUI ECS RunTask 는 worker 상태와 무관하게 진행한다.

### bundled wrapper

- `run-daily-aws-paper-bundled.ps1` 단일 파일 bundled wrapper 는 본 일자 미생성. 17개 step 별 파일 검증 완료 후 필요 시 생성 가능 / 기본 개발 / 검증 / 운영 기준은 분리 파일 구조 유지(OD-MS-023 정합).

### 산출물 위치

- run id 별 로그 / overrides JSON / summary 는 `C:\Temp\portfolio-daily-aws-paper\<run-id>\` 하위 `logs/` / `overrides/` / `summary/run-summary.txt` 에 저장. wrapper summary / overrides JSON / SSM stdout · stderr 파일에는 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname 평문 출력 0건(R-DOCS-001 [2026-06-17 wrapper 보강] mitigation 정합).
