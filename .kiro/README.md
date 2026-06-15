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

## 현재 진행 상태 요약 (2026-06-15 기준)

본 섹션은 운영자가 한눈에 보기 위한 짧은 진행 상태 요약이다. 자세한 일자별 결과 / 후속 인계는 `specs/_common/followups-overview.md` 와 `WORKLOG.md` 에 누적되어 있고, 각 spec 의 운영 결과는 해당 spec 의 `operation-notes.md` 에 누적된다. 본 섹션의 "완료" 표기는 실제 데이터 적재 / 최신성 검증까지 확인된 경우에만 사용한다(OD-MS-020 정합).

- 02 `aws-network-and-rds` — VPC / Subnet / RDS / DB Role 1차 적용 완료. SSM Port Forwarding 표준 경유지 1차 운영(OD-NET-010 / OD-NET-011).
- 03 `marketconnector-ec2` — EC2 + EIP 정상 운영 모드 1차 검증 완료. 본 일자(2026-06-15) `CONNECTOR_BALANCE` 1차 실행 성공(KIS balance API status 200 / `connector_balance_snapshot` 저장).
- 06 `secrets-and-iam` — paper 환경 Secrets / IAM 1차 적용 완료. MS 별 Secret 접근 분리(OD-SEC-006) 정책 정합성 1차 실증(MarketConnector EC2 role 의 preprocessor secret `AccessDeniedException` 정상 동작).
- 08 `interest-crawler-and-preprocessor-ecs` — **Interest Crawler hybrid 1차 구현: 부분 완료**. KRX GUI worker 는 운영 가능 상태로 1차 완성(KRX program / KRX shortsell 직전 거래일까지 적재). ECS / Fargate crawler 는 smoke 검증 완료. **non-GUI daily raw 수집 운영 경로와 raw 전체 최신성 검증은 후속**(non-GUI raw 7종 직전 거래일까지 미적재). preprocessor MS 는 ECS 단발 RunTask 실행 성공(exitCode 0 / `updated_at` 갱신) — **단, 입력 raw 최신성 부족으로 신규 feature date 생성은 제한**(R-DATA-010).
- 04 `strategy-batch-stepfunctions` — Strategy Decision 2개 Task Definition / Strategy Execution 단일 Task Definition + command override 1차 RunTask 검증 완료(OD-MS-013 / OD-MS-017). Step Functions / EventBridge 정기 트리거는 후속.
- 09 `strategy-research-batch` — AWS Batch Compute Environment / Job Queue / Job Definition revision 1 ~ 3 + S3 업로드 보강 1차 실증 통과(BACKTEST_RESEARCH full + BACKTEST_REPORT 4개 리포트 + S3 prefix `strategy-research/reports/` 한정). View Daily Batch 기준 AWS Batch 포팅 대상은 `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종 한정(OD-MS-019). `block_watch_*` / `block_exception_buy_*` / `run_extended_analysis.py` 는 수동 보조 도구로 분류.
- 05 / 07 / 10 — 미진행. View Daily Batch 의 ProcessBuilder → AWS Batch · ECS RunTask 매핑(05) / Step Functions state machine + EventBridge Scheduler / CI/CD OIDC(07) / aws-live cutover(10) 모두 후속 분리.
- aws-live — **미진행** / 본 일자까지 모든 검증은 `aws-paper` 한정.
- 가장 최근 Backend AWS E2E dry-run 진행 상태(2026-06-15) — 1번 `CONNECTOR_BALANCE` 완료 / 2번 `INTEREST_CRAWLER` 부분 완료 / 3번 `PREPROCESSOR` 실행 완료(데이터 최신성 제약) / 4 ~ 7번(`BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL`) 미진행 / 8 ~ 17번 미진행 또는 dry-run skip 예정. 실제 BUY / SELL / `--execute` 주문 전송 0건 / fill·position sync 자동 재시도 0건(OD-MS-021 / OD-SAFE-001 ~ OD-SAFE-004 정합).
