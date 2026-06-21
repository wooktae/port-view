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

## 현재 진행 상태 요약 (2026-06-21 기준)

본 섹션은 운영자가 한눈에 보기 위한 짧은 진행 상태 요약이다. 자세한 일자별 결과 / 후속 인계는 `specs/_common/followups-overview.md` 와 `WORKLOG.md` 에 누적되어 있고, 각 spec 의 운영 결과는 해당 spec 의 `operation-notes.md` 에 누적된다. 본 섹션의 "완료" 표기는 실제 데이터 적재 / 최신성 검증 또는 end-to-end 상태 전이 검증까지 확인된 경우에만 사용한다(OD-MS-020 / OD-MS-021 / OD-MS-023 / OD-MS-026 정합).

- 02 `aws-network-and-rds` — VPC / Subnet / RDS / DB Role 1차 적용 완료. SSM Port Forwarding 표준 경유지 1차 운영(OD-NET-010 / OD-NET-011). **2026-06-17 Daily AWS 17-step E2E 흐름에서 `execution_app` 의 `interest` schema USAGE / table SELECT / sequence / default privileges 보정(Step 8 1차 실패 → 운영자 GRANT 보정 후 통과)과 `marketconnector_app` 의 `legacy` schema USAGE / `legacy.holdings` DML / sequence / database search_path(`connector, execution, legacy, reference, public`) 보정(Step 17 1차 실패 → 보정 후 재실행 통과) 1차 실증.** **2026-06-18 wrapper Step 17 결과 확인 시 view_app 의 `legacy.holdings` SELECT 권한 부재로 운영자가 `portfolio_admin` 으로 우회 조회 → view_app legacy schema GRANT 검토 후속(R-DATA-005 보강).** 정식 매트릭스 갱신은 후속(R-DATA-005 / R-DATA-011 정합).
- 03 `marketconnector-ec2` — EC2 + EIP 정상 운영 모드 1차 검증 완료. **2026-06-17 Daily AWS 17-step E2E 에서 KIS paper BUY 4건(broker_order_no `0000035906` / `0000035912` / `0000035918` / `0000035932`) end-to-end 1차 통과(`execution_order 26 ~ 29` SUBMITTED → FILLED / `connector_order_request 34 ~ 37` ACCEPTED / `connector_position_snapshot 120 ~ 123`).** **2026-06-18 Daily AWS Paper Wrapper 17단계 실운영 검증에서 Step 12 `-AllowPaperOrderExecute` 첫 사용 / 1차 KIS paper API read timeout(`connector_order_request 38 ~ 41` FAILED) → 통제된 REQUESTED 복구 → 재시도 KIS paper BUY 4건 제출 성공(broker_order_no `0000025576` / `0000025740` / `0000025744` / `0000025747` / `connector_order_request 42 ~ 45` ACCEPTED / `strategy_execution_order 30 ~ 33` SUBMITTED → FILLED). Step 13 broad 조회 `output1 empty` + `output2 summary-only` 응답에서 active candidate 4건 상태 → summary fallback 자동 skip → 단건 `--code` · `--order-no` · `--no-broad` 조회로 4건 체결 반영 / Step 17 SSM Success / `connector_position_snapshot` row_count 36.** **2026-06-18 두 번째 세션에서 Step 13 운영 안전성 강화 — `connector_order_check.py` 기본 실행 모드 변경(broad 일괄 → active 주문 단건 순차 조회 / `--broad` 옵션은 legacy / 명시 주문 조회는 `--code` · `--order-no` · `--no-broad` / wrapper ps1 은 실행 orchestration 만 / 체결조회 방식 제어는 `connector_order_check.py` 내부 책임 / OD-MS-025 신규 / R-AUTO-018 [2026-06-18 추가 보강] mitigation 정합 / `-StartStep 13 -EndStep 13` 단독 실행 검증 통과).** SELL / 취소 / 정정 호출 0건 / aws-live 작업 0건.
- 06 `secrets-and-iam` — paper 환경 Secrets / IAM 1차 적용 완료. MS 별 Secret 접근 분리(OD-SEC-006) 정책 1차 실증. **2026-06-17 17-step E2E 에서 MarketConnector EC2 Instance Role 기반 Secrets Manager / SSM Parameter read 1차 실증(JSON SecretString 내부 key 추출 정책 정합 / value 평문 출력 0건).** **2026-06-18 wrapper 1~17 실 실행에서도 동일 정책 정합 — secret 평문 출력 0건 / `/tmp/inject-env.sh` v5 env injection 만 사용.**
- 08 `interest-crawler-and-preprocessor-ecs` — Interest Crawler hybrid 구조 완료(non-GUI ECS Fargate + KRX Windows EC2 worker). Preprocessor ECS Fargate Task 유지. **2026-06-17 17-step E2E 의 Step 2 / Step 3 통과(2026-06-16 적재).** **2026-06-18 wrapper Step 2 / Step 3 재통과(interest raw 2026-06-17 회복 / `pre_total_*_feature` 2026-06-17).** **2026-06-19 KRX raw 최신성 회복(interest_program_raw / interest_shortsell_raw max_date `2026-06-19`).** **2026-06-21 Step 2 wrapper 성공판정 강화 — Scheduled Task trigger 성공만으로 SUCCESS 처리 금지 / Chrome reset + Running → Ready wait + Last Result 0 / 0x0 + latest worker log + KRX raw DB validation(`interest_krx_raw_validate_daily.py` 신규 / SSM step `INTEREST_CRAWLER_KRX_DB_VALIDATE`) 통과까지 SUCCESS 조건. crawler worker EC2 stopped 시 fail-closed(이전 자동 skip 폐지). Step 2 단독 실행 검증 통과(RunId `daily-aws-paper-20260621-204017` / ExpectedKrxRawDate `2026-06-19` / OD-MS-026 신규 / R-AUTO-020 신규 mitigation 1차 실증).** crawler worker stop / KRX GUI = Windows interactive desktop session 기반 유지.
- 04 `strategy-batch-stepfunctions` — Strategy Decision 2개 Task Definition / Strategy Execution 단일 Task Definition + command override 검증 완료. **2026-06-17 17-step E2E 의 Step 6 ~ 11 / 14 ~ 16 end-to-end 1차 통과(OD-MS-016 책임 분리 정합).** **2026-06-18 wrapper Step 6 ~ 11 / 14 ~ 16 재통과(`execution_plan_id 94` / BUY 4건 / `strategy_execution_order 30 ~ 33` SUBMITTED → FILLED). Step 16 추가매수 `position_state` unique constraint 충돌 → `execution_sync_buy_position.py` merge 패치(`merge_open_position_state()` + `additional_buys` + idempotency) → ECR push → ECS 재실행 통과 / 추가매수 2건(004990 65→69주 / 003490 52→58주) + 신규 OPEN 2건(023530 8주 id `11` / 042660 11주 id `12`) 정상 반영(R-DATA-012 신규 mitigation 1차 실증 / OD-MS-024 신규 정합).**
- 09 `strategy-research-batch` — AWS Batch Compute Environment / Job Queue / Job Definition 1차 실증 통과. **2026-06-17 17-step E2E / 2026-06-18 wrapper Step 4 / Step 5 모두 통과(SUCCEEDED / 결과 최신성 2026-06-17 / S3 산출물 / OD-MS-019 정합 / heavy 분류 SubmitJob 0건 유지).**
- 05 / 07 / 10 — 미진행. View Daily Batch 의 ProcessBuilder → AWS Batch · ECS RunTask 매핑(05) / Step Functions state machine + EventBridge Scheduler / CI/CD OIDC(07) / aws-live cutover(10) 모두 후속 분리. 2026-06-17 / 2026-06-18 17-step 흐름은 운영자 로컬 PowerShell wrapper 와 직접 SubmitJob / RunTask / SSM 의 단계별 수동 오케스트레이션으로 통과 — 정기 트리거 / 관제 UI 격상은 별도 phase 책임.
- aws-live — **미진행** / 본 일자까지 모든 검증은 `aws-paper` 한정.
- Backend AWS E2E 17-step 진행 상태(2026-06-18) — **wrapper 기반 1 ~ 17 실 실행 완료**(Step 1 ~ Step 17 / 운영 예외 3종 식별·복구 통과 / 실제 paper BUY 4건 제출 + 체결 + sync + balance refresh 까지 E2E 완료 / `aws-paper` 한정 / aws-live 작업 0건). 운영 예외 — KIS paper API read timeout(R-BROKER-004 신규 / Mitigated) / KIS `inquire-daily-ccld` `output1 empty` + `output2 summary-only` 응답(R-AUTO-018 mitigation 1차 실증 / Mitigated) / 추가매수 position_state unique constraint 충돌(R-DATA-012 신규 / Mitigated). 모두 운영 중 식별 / 복구 / 검증 완료.
- **Daily AWS PowerShell wrapper 기준선 1차 완성(2026-06-18)** — 2026-06-17 wrapper 구조 1차 수립 후속으로 2026-06-18 실 실행 통과. wrapper 기반 전체 1~17 실제 실행 완료 / Step 12 `-AllowPaperOrderExecute` 첫 사용 / 운영 예외 3종 식별·복구 검증 / `execution_sync_buy_position.py` 추가매수 merge 패치 운영자 직접 작업 후 통과(OD-MS-024 신규 정합). bundled wrapper(`run-daily-aws-paper-bundled.ps1`) 는 본 일자에도 미생성 — 후속 선택 작업.

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
- `-StartStep` / `-EndStep` : 부분 실행 지원(예: `-StartStep 1 -EndStep 7` safe subset). **파라미터 이름 주의 — `-FromStep` / `-ToStep` 은 wrapper 에 존재하지 않는 미정의 파라미터이며, 실수 입력 시 PowerShell 이 silent 무시하고 wrapper 가 default `-StartStep 1 -EndStep 17` 로 진입할 위험이 있다(2026-06-18 운영 메모 / OD-MS-023 / OD-MS-025 정합).** Step 13 / Step 16 등 단독 step 검증 시에는 반드시 `-StartStep N -EndStep N` 형식으로 명시한다.
- `-DryRun` : 실행 계획만 출력 / 실제 ECS RunTask · Batch SubmitJob · SSM command 제출 0건. 모든 step 의 `FOUND` / `MISSING` 상태 점검 용도.
- `-AllowPaperOrderExecute` : Step 12 KIS paper 주문 제출을 명시적으로 허용. **본 옵션 없이는 Step 12 가 wrapper 중앙 PAPER_ORDER_GATE + Step 12 내부 이중 gate 로 차단됨**(R-AUTO-019 mitigation). 옵션은 운영자가 직접 명시해야 하며 default OFF.

### Step 12 안전 주의사항

- Step 12 (`MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`) 는 `connector_strategy_order_execute.py --execute` 를 호출 / 실제 KIS paper 주문 제출 가능 step.
- 기본 실행 모드에서는 wrapper 중앙 `PAPER_ORDER_GATE` 와 Step 12 내부 이중 gate 가 동시에 차단(OD-MS-023 / R-AUTO-019 정합).
- `-AllowPaperOrderExecute` 명시 + `aws-paper` 환경 + `PORT_DB_TARGET=aws-paper` guard 통과 시에만 실주문 진입(R-AUTO-009 / R-AUTO-010 정합).
- **2026-06-18 첫 실주문 사용 사례** — 운영자가 `-AllowPaperOrderExecute` 명시로 실 paper BUY 4건 제출. 1차 시도에서 KIS paper API read timeout 발생(connector_order_request id `38 ~ 41` FAILED) → `connector_order_request` / `connector_api_call_log` / `broker_order_no` 존재 여부 사전 점검 후 통제된 REQUESTED 복구 → 재시도 KIS paper BUY 4건 제출 성공(broker_order_no `0000025576` / `0000025740` / `0000025744` / `0000025747` / R-BROKER-004 신규 mitigation 1차 실증). 단순 재실행으로 복구하면 broker 중복 주문 위험이 있으므로 반드시 사전 점검 패턴을 따른다.
- live 자동 BUY / SELL E2E 검증은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지.
- Step 10 / Step 11 의 `--execute` 는 strategy execution 내부 상태 생성 / 갱신 의미 / broker · KIS 직접 제출 아님(OD-MS-016 책임 분리 정합).
- Step 13 / Step 14 / Step 15 / Step 16 / Step 17 은 모두 DB 상태 갱신 / 조회 / snapshot refresh 책임 / 신규 broker · KIS 주문 제출 0건. **2026-06-18 Step 13 broad 조회 `output1 empty` + `output2 summary-only` 응답 + active candidate 4건 상태에서 summary fallback 자동 skip → 단건 `--code` · `--order-no` · `--no-broad` 조회로만 체결 반영 1차 실증(R-AUTO-018 정합).** **Step 16 의 추가매수 케이스에서 `strategy_position_state` unique constraint 충돌은 `execution_sync_buy_position.py` merge 패치(`merge_open_position_state()` + `additional_buys` + idempotency)로 해소(OD-MS-024 신규 / R-DATA-012 신규 mitigation 1차 실증).**

### Step 13 안전 주의사항

- Step 13 (`CONNECTOR_ORDER_CHECK`) 는 MarketConnector EC2 SSM RunCommand 로 `connector_order_check.py` 를 호출 / 신규 broker · KIS 주문 제출 없음 / 주문 상태 조회 + DB 상태 갱신 step.
- **2026-06-18 운영 안전성 강화 — 기본 실행 모드 변경**(OD-MS-025 신규). `connector_order_check.py` 의 기본 동작이 broad 체결조회 일괄 처리 → active 주문 단건 순차 조회로 변경됨. wrapper ps1 은 Step 13 실행 orchestration 만 담당하고, 체결조회 방식 제어(active 주문 조회 / 주문별 direct-only 체결조회 / broad 모드 분리) 는 `connector_order_check.py` 내부 책임으로 분리(R-AUTO-018 [2026-06-18 추가 보강] 정합).
- `connector_order_check.py` 옵션 — `--broad`(legacy / 진단용 / 기본값 미사용) / `--active-limit`(active 주문 단건 순차 조회 최대 처리 건수) / 명시 주문 조회는 `--code {ticker_code} --order-no {broker_order_no} --no-broad` 조합.
- 다건 active 주문 상태에서 KIS `inquire-daily-ccld` 가 `output1 empty` + `output2 summary-only` 응답을 반환하더라도 summary fallback 을 DB 반영 근거로 사용하지 않는다. `connector_order_request` 후보가 주문번호 / 종목코드 기준으로 1건 확정된 경우에만 summary fallback 허용 / `connector_order_event` · `connector_fill` 오매핑 차단(R-AUTO-018 mitigation 정합).
- 2026-06-18 Step 13 단독 실행 검증은 `.\run-daily-aws-paper.ps1 -Environment aws-paper -RunDate 2026-06-18 -StartStep 13 -EndStep 13` 으로 통과(SSM commandId / status `Success` / responseCode 0 / Step 13 COMPLETED). `-FromStep` / `-ToStep` 미정의 파라미터로 실수 입력 시 default 1 / 17 진입 위험 — 반드시 `-StartStep` / `-EndStep` 사용.

### Windows KRX crawler worker 와 Step 2 동작

- Step 2 (`INTEREST_CRAWLER`) 는 (a) non-GUI ECS / Fargate Task Definition `portfolio-paper-interest-crawler:7` RunTask 와 (b) Windows KRX crawler worker 의 `Portfolio-KRX-Worker-Daily` Scheduled Task trigger 와 (c) **KRX raw DB validation**(`interest_krx_raw_validate_daily.py` SSM step `INTEREST_CRAWLER_KRX_DB_VALIDATE`) 세 흐름을 함께 처리한다.
- **2026-06-21 성공판정 강화 (OD-MS-026 신규 / R-AUTO-020 신규 mitigation 정합)** — Scheduled Task trigger 성공만으로 Step 2 SUCCESS 처리하지 않는다. wrapper 는 (1) Chrome / chromedriver stale process best-effort reset(reset 실패는 warning), (2) Scheduled Task `Running` 상태 polling → `Ready` 복귀 wait + `sawRunning` 로그 출력 + timeout 시 Step 2 실패, (3) `Last Result` 0 또는 0x0 만 SUCCESS, (4) `C:\portfolio\logs\krx_worker_daily_*.log` 최신 파일 path / last write time / size / tail 출력, (5) `ExpectedKrxRawDate` 기준 `interest_program_raw` / `interest_shortsell_raw` row_count + `max(trade_date)` 검증(non-zero exit 또는 row_count 0 시 Step 2 fail) 까지 모두 통과해야 SUCCESS. Step 2 step result 에는 `KrxDbValidationCommandId` 가 포함된다.
- **crawler worker EC2 fail-closed (R-AUTO-016 mitigation 갱신)** — Windows KRX crawler worker 의 EC2 instance state 가 `running` 이 아니면 Step 2 는 즉시 실패 처리한다(이전 "wrapper 안에서 KRX GUI Scheduled Task trigger 를 자동 skip" 동작은 본 일자에 폐지). 실패 메시지에 `instanceId` / `state` 출력 / KRX GUI worker · DB validation 미수행 상태에서 Step 2 SUCCESS 진입 차단.
- KRX GUI 경로는 SSM direct python 실행이 아니라 Windows Administrator interactive Scheduled Task `schtasks /Run /TN Portfolio-KRX-Worker-Daily` → `powershell.exe -ExecutionPolicy Bypass -File C:\portfolio\run_krx_worker_daily.ps1` 흐름을 유지한다(OD-MS-022 / OD-MS-026 정합).
- non-GUI ECS RunTask 는 worker 상태와 무관하게 진행하되, `containerOverrides.environment` 로 `TEMP=/tmp` / `TMP=/tmp` / `PYTHONUTF8=1` / `PYTHONIOENCODING=utf-8` 를 주입한다(Windows / Linux 인코딩 차이 완화 / UTF-8 기준 명확화).
- **Step 2 단독 실행 명령** — `.\run-daily-aws-paper.ps1 -Environment aws-paper -RunDate 2026-06-20 -StartStep 2 -EndStep 2`. 2026-06-21 단독 실행 검증 통과(RunId `daily-aws-paper-20260621-204017` / Status `SUCCESS` / Runner `ECS+SSM` / ExpectedKrxRawDate `2026-06-19`). `-FromStep` / `-ToStep` 미정의 파라미터로 실수 입력 시 default 1 / 17 진입 위험 — 반드시 `-StartStep` / `-EndStep` 사용.

### bundled wrapper

- `run-daily-aws-paper-bundled.ps1` 단일 파일 bundled wrapper 는 본 일자 미생성. 17개 step 별 파일 검증 완료 후 필요 시 생성 가능 / 기본 개발 / 검증 / 운영 기준은 분리 파일 구조 유지(OD-MS-023 정합).

### 산출물 위치

- run id 별 로그 / overrides JSON / summary 는 `C:\Temp\portfolio-daily-aws-paper\<run-id>\` 하위 `logs/` / `overrides/` / `summary/run-summary.txt` 에 저장. wrapper summary / overrides JSON / SSM stdout · stderr 파일에는 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname 평문 출력 0건(R-DOCS-001 [2026-06-17 wrapper 보강] mitigation 정합).
