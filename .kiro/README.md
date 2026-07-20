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
| Paper Daily Step 1-11 | 🟢 자동 ENABLED · 2026-07-15 KST 날짜 기준 적용 완료 · 2026-07-16 `daily_run_id=76` 복구 · BUY 후보 2건 생성 [^daily-kst-2026-07-15] [^daily-buy-e2e-2026-07-16] | 2026-07-16 | 04, 08 | [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |
| Paper Daily Step 12-17 | 🟢 자동 ENABLED · 2026-07-20 매도 2건 체결 및 Step 14~17 수동 복구 완료 · Step 13 `EGW00201` 전용 재시도와 주문 간 5초 대기 배포 · 잔고 runDate 동적화 · 결과 실패 시 실패 Slack 경로 보완 · `DAILY_EXECUTION_SUCCESS` 실제 매수·매도 체결 내역 표시 개선 (Builder → Notifier · 수동 smoke 수신) [^daily-recovery-2026-07-20] [^daily-exec-slack-2026-07-20] | 2026-07-20 | 04 | [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |
| Intraday Stop Loss Slack | 🟢 연동 완료 | 2026-06-30 | 03, 04 | [operation-notes](specs/03-marketconnector-ec2/operation-notes.md) |
| Daily Brief Slack | 🟢 자동 ENABLED + Dispatcher 공통 Holiday Guard 적용 + 장후 실 Slack 수신 확인 [^daily-brief-2026-07-15] | 2026-07-15 | 04 | [operation-notes](specs/04-strategy-batch-stepfunctions/operation-notes.md) |
| port-view ECS Fargate | 🟢 1차 포팅 검증 · `/daily-batch` OPS Mirror UI consumption 확인 완료 [^ops-mirror-ui] | 2026-07-08 | 05 | [operation-notes](specs/05-port-view-ecs-and-runbook/operation-notes.md) |
| aws-live BUY/SELL 자동화 | 🔴 미진행 | — | 10 | [followups-overview](specs/_common/followups-overview.md) |

[^tz-patch]: 2026-07-08 회차에서 ECS Fargate crawler / preprocessor 컨테이너의 UTC 기본 timezone + naive `datetime.now()` 사용이 stale raw / feature / decision `data_date` 원인으로 확정되었다. `portfolio-paper-interest-crawler:8` · `portfolio-paper-interest-preprocessor:2` 신규 revision 에 `TZ=Asia/Seoul` 을 추가하고 State Machine `portfolio-paper-daily-step1-17-approval` 의 Step2A / Step3 task revision 을 갱신했다. one-off TZ smoke 는 성공. 2026-07-09 회차에서는 KRX CRAWLER Windows EC2 timezone 이 UTC 였던 원인 축을 추가로 확인하여 Korea Standard Time 으로 변경했고, KRX Scheduled Task 수동 재실행으로 `interest_program_raw` · `interest_shortsell_raw` `MAX(trade_date)=2026-07-08` (program rows 1 · shortsell rows 349) 회복을 확인했다. core ECS raw + KRX GUI worker 모두 최신성 회복. DB `created_at` session timezone 은 UTC 유지 권고이며 운영 표시는 `at time zone 'Asia/Seoul'` 로 KST 변환하여 본다. Step2B false-success 이슈(worker exit 0 · SSM ResponseCode 0 · Step Functions SUCCEEDED ≠ DB raw 최신성)는 R-DATA-017 Open 유지 · Step2B DB validation 추가는 후속. 상세는 `WORKLOG.md` 2026-07-09 krx-crawler-ec2-timezone-fix-and-recollect 섹션과 `specs/_common/risk-register.md` R-DATA-010 · R-DATA-017 · `specs/_common/operator-decisions.md` OD-MS-040 참조.

[^ops-mirror-ui]: 2026-07-08 회차에서 Step Functions 실행 이력 OPS Mirror(OD-DB-012 · OD-MS-039 · R-AUTO-038) 의 UI consumption 검증이 완료되었다. `view_app` 기준 `ops` schema USAGE · `ops.strategy_daily_batch_run` · `ops.strategy_daily_batch_step_log` SELECT 확인 · `AWS_STEPFUNCTIONS` run 이력 적재 확인(#61 Step 12~17 SUCCESS · #60 Step 1~11 `APPROVAL_BLOCKED` / SKIPPED) · `/daily-batch` 화면 aws-stepfunctions 실행 모드 표시 · 최근 실행 이력 `AWS_STEPFUNCTIONS` 라벨 · 선택 run 상세와 step log 렌더링 · AWS Step 1~11 시작 버튼 · AWS Step 12~17 승인 실행 버튼 표시 · Local File 실행 모드 OFF · aws-stepfunctions 실행 모드 ON 표시까지 확인. 전체 세부 step mirror 확장은 후속 유지. `/daily-batch` 화면의 `requestPayload` / `resultPayload` 표시 redaction · `accountNo` 마스킹 · `executionArn` / `stateMachineArn` redaction · timestamp UTC → KST 표시 보완 · `APPROVAL_BLOCKED` / SKIPPED 문구 운영자 친화적 개선 은 후속으로 신규 등록. 상세는 `WORKLOG.md` 2026-07-08 view-daily-batch-ops-mirror-ui-consumption-confirmed 섹션과 `specs/_common/operator-decisions.md` OD-MS-039 · `specs/_common/risk-register.md` R-AUTO-038 · `specs/_common/followups-overview.md` Done recently 2026-07-08 row 참조.

[^daily-brief-2026-07-15]: 2026-07-15 회차에서 자동 장후 Daily Brief Slack 미발송 원인이 확정되었다. Builder Lambda 가 VPC 내부에서 외부 Holiday API 호출에 실패해 Fail-Closed 로 장전 · 장후 Slack 이 skip 되던 상태였고, Builder 내부 Holiday Guard 를 비활성화한 뒤에는 정상 응답에 `skipped` 필드가 없어 mini state machine `CheckHolidaySkip` Choice 가 `States.Runtime` 을 냈으며, 배포 ZIP 에 `pg8000` 이 없어 DB 경로 진입 시 import 오류가 발생했다. 운영자가 직접 Daily Scheduler Dispatcher 에 `MORNING_BRIEF` · `EVENING_BRIEF` 지원과 Daily Brief State Machine ARN 환경변수 · Resource 한정 `states:StartExecution` IAM 을 추가하고 장전 · 장후 Scheduler Target 을 Dispatcher Lambda 호출로 전환해 Holiday Guard 책임을 Dispatcher 로 통합했다. Builder 는 메시지 생성 · DB 조회 책임만 유지하도록 내부 Guard 를 환경변수 제어로 비활성화하고 정상 응답에 `skipped=false` · `skipReason=null` 을 추가하며 `pg8000` 및 관련 의존성을 패키징해 재배포했다. 최종 4종 경로(07:50 장전 · 08:00 Step 1~11 · 09:01 Step 12~17 · 15:50 장후) 는 동일 Dispatcher · 동일 Holiday Guard 통과 구조를 사용하고, EC2 stop Scheduler 15:50 은 휴일 여부와 무관하게 실행하는 기존 구조를 유지한다. 2026-07-15 15:50 자동 장후 실행이 초기 `CheckHolidaySkip` 오류로 실패한 뒤 16:02 장후 실전 smoke 에서 Step Functions `SUCCEEDED` · Notifier `statusCode=200` · Slack 실제 수신 확인. 07:50 자동 장전 실전 수신은 다음 평일 후속 유지. Kiro 는 본 회차에서 문서만 갱신한다. 신규 Risk ID · Decision ID · Decision Summary count 변경 없음. 상세는 `WORKLOG.md` 2026-07-15 daily-brief-slack-recovery 섹션 · `specs/_common/risk-register.md` R-AUTO-035 Mitigation history · `specs/_common/operator-decisions.md` OD-MS-032 · OD-MS-038 Details · `specs/_common/followups-overview.md` Done recently 2026-07-15 row 참조.

[^daily-kst-2026-07-15]: 2026-07-15 오후 회차에서 2026-07-13 (월) 매수 후보 4건이 실행되지 않은 원인이 확정되었다. 실제 실행 시각 2026-07-13 08:00 KST 는 UTC 로 2026-07-12 23:00 이고, 실행 컨테이너가 `date.today()` · `datetime.today()` naive 함수를 사용해 업무 날짜를 2026-07-12 (일) 로 판단 · Step8 이 `WEEKEND / NO_TARGET` 으로 종료 · Execution Plan · Execution Order 미생성 · ExitCode 0 으로 Step Functions 전체 SUCCESS 로 표시되어 자동 감지가 어려웠다. Daily 전략 계산과 2026-07-10 기준 데이터는 정상(`daily_run_id=73` · `run_date=2026-07-12` · `data_date=2026-07-10` · `market_signal=AGGRESSIVE` · BUY 신호 4건 · 후보 4건). 운영자가 직접 원칙을 DB timestamp 저장 UTC 유지 · 업무 날짜 판단 Asia/Seoul 통일로 확정하고 Daily BUY / SELL 실행 관련 ECS Task Definition 에 `TZ=Asia/Seoul` 을 추가하고 Market EC2 서버 timezone 을 Korea Standard Time 으로 변경했으며 실행 스크립트가 로컬 · ECS 에서 동일한 KST 업무 날짜를 사용하도록 보완하고 신규 Task Definition revision 을 실제 Step Functions 실행 경로에 연결했다. UTC 날짜 오판으로 월요일 후보가 WEEKEND 처리된 직접 원인은 해결 완료 · ECS · Market EC2 KST 적용 · 신규 revision 연결 완료. 다음 자동 실행 재발 여부는 다음 영업일 실전 관찰 대상으로 유지 · `NO_TARGET` 과 실제 성공 상태의 구분 강화 · 주문 미생성 또는 내부 실패가 ExitCode 0 · Step Functions SUCCESS 로 묻히지 않도록 실패 전파 강화 · Plan / Order / Connector Request / Signal Order Map / Broker 접수 / Fill / Position 검증 체인 보강은 별도 후속. Kiro 는 본 회차에서 문서만 갱신한다. 신규 Decision ID · Risk ID · Decision Summary count · Risk Dashboard count 변경 없음. 상세는 `WORKLOG.md` 2026-07-15 오후 Daily BUY KST 섹션 · `specs/04-strategy-batch-stepfunctions/operation-notes.md` 2026-07-15 (오후) 섹션 · `specs/_common/risk-register.md` R-DATA-010 · R-DATA-017 Mitigation history · `specs/_common/operator-decisions.md` OD-MS-040 Details · `specs/_common/followups-overview.md` Now 참조.

[^daily-buy-e2e-2026-07-16]: 2026-07-16 회차에서 Paper Daily 자동화 라인업의 실 매수·체결·Fill·Position E2E 가 처음 완료됐다. Daily Run 76 이 `run_date=2026-07-16` · `data_date=2026-07-15` · `market_signal=AGGRESSIVE` 로 정상 계산되고 BUY 후보 2건(엔씨소프트 036570 · 코오롱생명과학 102940) 이 생성됐다. 운영자가 Step 12~17 을 수동 재실행해 13주 · 78주 시장가 매수 주문을 접수했고 두 주문 모두 KIS 모의투자에서 전량 체결됐다(엔씨소프트 최종 보유수량 13주 · 평균 체결가 223,500 원 · 코오롱생명과학 최종 보유수량 78주 · 평균 체결가 약 38,839.7436 원). 초기 Step 13 조회가 주문 제출 후 약 10초 시점에 이뤄져 엔씨소프트 9주 부분체결 · 코오롱생명과학 접수 상태로 고착된 뒤 실제 시장가 주문은 이후 계속 체결됐으나 내부 상태가 최초 조회 결과에 머무는 문제가 확인됐다(전체 Step Functions 는 ExitCode 0 기준 SUCCESS 처리). 운영자가 직접 Step 13(`connector_order_check.py`) 재실행으로 두 주문을 `FILLED` 반영하고 Connector Order Event 최신 상태 · Connector Fill 13주 · 78주 정상 반영 · Step 15(`execution_sync_buy_fill.py`) 재실행으로 Execution Order 2건을 `SUBMITTED` → `FILLED` · Step 16(`execution_sync_buy_position.py`) 재실행으로 `filled_buy_orders_without_position=2` · `synced_count=2` · `skipped_count=0` · Position ID 13(엔씨소프트) · Position ID 14(코오롱생명과학) 모두 `OPEN` 생성 순서로 DB 정합을 복구했다. 원인은 시장가 주문 자체 실패가 아니라 모의투자 주문 체결 및 조회 API 반영보다 Step 13 조회 시점이 빨랐던 점이고, 후속 동기화는 자동 반복되지 않아 Execution Order 가 `SUBMITTED` 상태에 머물러 Step 16 조회 조건(`execution_status='FILLED'`) 대상에서 제외됐다(Step 15 재실행 후에만 Step 16 Position 생성 가능). 재발 방지 조치로 State Machine `portfolio-paper-daily-step12-17-approval` 의 Wait State `Step12_WaitBeforeCheck` `Seconds` 를 10 → 60 으로 변경하고 Next State `Step12_GetCommandInvocation` 유지 · State Machine 업데이트 후 재조회 검증을 마쳤다(실제 ARN · revision ID · broker 주문번호 원문은 본 문서 미기록). 대기시간 조정만으로 완전 해결됐다고 판단하지 않으며, ACCEPTED / PARTIAL_FILLED 주문의 후속 재조회 polling · Step 13 · 15 · 16 처리 건수 기반 실패 전파 강화 · Execution Order · Connector Request · Fill · Position 불일치 시 Workflow FAIL · OPS Mirror 세부 Step 확장 · 10분 잔고 스냅샷과 주문 · 체결 · Position 보정 연계는 후속 유지된다. Kiro 는 본 회차에서 문서만 수정한다. 신규 Risk ID · Decision ID · Decision Summary count · Risk Dashboard count 변경 없음. 상세는 `WORKLOG.md` 2026-07-16 섹션 · `specs/_common/risk-register.md` R-AUTO-037 · R-AUTO-038 · R-BROKER-004 Mitigation history · `specs/_common/operator-decisions.md` OD-MS-032 · OD-SAFE-001 Details · `specs/_common/followups-overview.md` Done recently 2026-07-16 row · `specs/_common/ms-aws-service-decision-matrix.md` `port_strategy_execution` 짧은 운영 메모 · `specs/_common/aws-resource-glossary.md` Usage Notes 2026-07-16 참조.

[^daily-recovery-2026-07-20]: 2026-07-20 09:01 자동 실행된 State Machine `portfolio-paper-daily-step12-17-approval` 이 Step 13(주문·체결 조회)에서 실패했다. Step 12 주문 제출 자체는 정상 완료되어 매도 주문 2건이 broker 에 정상 접수됐고 첫 주문은 Step 13 조회에서 전량 체결로 반영됐으나, 두 번째 주문 조회에서 KIS `EGW00201`(초당 거래건수 초과) 가 발생했다. 기존 `connector_order_check.py` 에는 `EGW00201` 전용 재시도가 없고 다건 주문을 주문 사이 대기 없이 연속 조회하던 점이 직접 원인이었고, Step 13 실패로 Step 14~17 과 성공 Slack 이 자동 실행되지 않았다. 운영자가 직접 `connector_order_check.py` 전체 교체본(connector-order-check-2.0.1 → 2.0.2)을 S3 경유로 EC2 에 배포해 주문 조회 사이 5초 대기와 `EGW00201` 한정 최대 2회 재시도(1차 1.5초 · 2차 5초)를 추가했다(다른 오류 코드 미적용 · 배포 전 원본 백업 · SHA 검증 · Python compile · AST 검증 성공 · broker 주문 제출 로직 변경 없음 · 이 재시도는 주문 제출 재시도가 아니라 주문·체결 조회 API 재시도다). Step 12 는 중복 주문 위험 때문에 재실행하지 않고 Step 13 만 수동 실행해 둘째 매도 주문 13주 전량 체결 · `SUMMARY_ONLY_FILLED` 이벤트와 fill 데이터 생성 · 두 주문 모두 `FILLED` · active 대상 0건으로 복구했다(신규 이벤트는 connector-order-check-2.0.2 로 기록). 이어 Step 14(`execution_sync_sell_fill.py`) · Step 15(`execution_sync_buy_fill.py`) · Step 16(`execution_sync_buy_position.py`) ECS Task 를 임시 State Machine 없이 하나씩 순차 수동 실행(모두 ExitCode 0) 하고 Step 17(`run_connector_balance_daily.sh` · run-date=2026-07-20) 으로 잔고 API Status 200 · `connector_balance_snapshot` 저장 성공 · 보유 종목 0건(과거 스냅샷 이력 보존 · 현금잔고 8,706,505 원 · 총평가금액 8,057,330 원 · 당일 매도금액 5,298,000 원) 을 확인했다. 성공 Slack 은 자동 실행 결과가 아니라 수동 복구 완료 후 `portfolio-event-notifier` Lambda 를 `DAILY_EXECUTION_SUCCESS`(runDate=2026-07-20 · stage=AFTER_STEP_17)로 수동 호출해 StatusCode 200 · ok=true · Portfolio Daily Bot 채널 실제 수신으로 확인했으며, 원래 09:01 자동 Step Functions 실행 이력은 실제 장애 보존을 위해 FAILED 로 유지한다(수동 복구를 자동 실행 성공으로 덮어쓰지 않음). 또한 `Step1_SendConnectorBalanceCommand` 의 `--run-date 2026-06-22` 하드코딩을 제거하고 실행 입력 `$.runDate` 를 States.Array · States.Format 기반 동적 commands 배열로 사용하도록 변경했고, Task 는 정상 종료했지만 결과값이 비정상인 경우 Choice Default 가 Fail State 로 직행해 실패 Slack 을 우회하던 문제를 Step13·14·15·16·Step1 각 Default 를 실패 컨텍스트 Pass State(`$.dailyExecutionFailure` 저장 후 실패 Slack · OPS 실패 기록 · 최종 상태는 FAILED 유지)로 변경해 보완했다(`Step12_Failed` 는 이번 변경 범위 제외 · ASL 검증 OK · 배포 후 재조회 확인). 다음 실제 다건 주문일에 주문 간 5초 대기 로그 · `EGW00201` 발생 시 1.5초·5초 재시도 로그 확인은 강제 오류 유발 없이 실운영 회차에서 확인하는 후속으로 유지한다. Kiro 는 본 회차에서 문서만 수정한다. 신규 Risk ID · Decision ID · Decision Summary count · Risk Dashboard count 변경 없음. 상세는 `WORKLOG.md` 2026-07-20 섹션 · `specs/_common/risk-register.md` R-AUTO-001 · R-AUTO-037 · R-AUTO-038 · R-BROKER-004 Mitigation history · `specs/_common/operator-decisions.md` OD-SAFE-001 · OD-SAFE-004 · OD-MS-009 · OD-MS-030 · OD-MS-031 · OD-MS-032 Details · `specs/_common/followups-overview.md` Done recently 2026-07-20 · `specs/_common/ms-aws-service-decision-matrix.md` 4.1 · 4.7 운영 메모 · `specs/_common/aws-resource-glossary.md` Usage Notes 2026-07-20 참조.

[^daily-exec-slack-2026-07-20]: 2026-07-20 장애 복구와 별개로, `DAILY_EXECUTION_SUCCESS` 성공 Slack 이 당일 실제 매수·매도 체결 종목·수량을 표시하도록 개선했다. 기존 성공 Slack 은 실행일·최종 상태·Workflow 정도만 표시했다. 신규 `portfolio-daily-execution-slack-summary-builder` Lambda(Python 3.12)가 `view_app` 로 `connector.connector_fill` 의 당일 체결(`connector_fill.created_at` 을 Asia/Seoul 날짜로 변환해 runDate 비교)을 조회하고 `reference.stock_master` 로 종목명을 붙여 `buyFills` · `sellFills`(종목별 `tickerCode` · `stockName` · `fillQty`) payload 를 생성한다. `portfolio-event-notifier` 의 `DAILY_EXECUTION_SUCCESS` formatter 는 제목 ✅ [Daily 실행] 성공 · 실행일 · 최종 상태 · [매수 체결] · [매도 체결] 구조로 바뀌었고 체결이 없으면 `- 없음`, 있으면 `- 종목명 / 수량주` 로 표시하며 기존 Workflow · Execution · 주문/체결 요약 라인은 제거됐다(기존 Lambda 코드 백업 · Python compile · import · formatter 로컬 테스트 통과 · 배포 후 Active · LastUpdateStatus=Successful · marker/해시 일치 · SHA256 원문 미기록). State Machine `portfolio-paper-daily-step12-17-approval` 성공 경로에 신규 State `BuildDailyExecutionSuccessSlackSummary` 를 넣어 Step 완료 → Builder(runDate 입력 · 결과 `$.dailyExecutionSuccessSummary` 저장) → Notifier(`$.dailyExecutionSuccessSummary.Payload` 입력) → `RecordWorkflowStepSuccess` 로 연결했고 Builder 실패 시 기존 `SendDailyExecutionFailedSlack` 경로로 연결한다. IAM 은 Builder 전용 최소 실행 Role(로그·VPC ENI·`view_app` DB 비밀번호 Secret 조회)과 State Machine Role 의 `lambda:InvokeFunction` inline policy(`portfolio-daily-execution-slack-builder-invoke`)만 추가했다. ASL 검증 OK · diagnostic 0건 · 로컬/AWS 정의 canonical(key 정렬) 비교 의미상 차이 0건. 2026-07-20 데이터로 Builder → Notifier 를 순차 수동 호출하는 smoke 만 수행(State Machine 전체 execution 미시작)해 매수 0건 · 매도 엔씨소프트(036570) 13주 · 코오롱생명과학(102940) 78주 · Notifier StatusCode 200 · FunctionError 없음 · ok=true · Portfolio Daily Bot 채널 실제 수신을 확인했다. 이 smoke 는 09:01 자동 실행의 성공 결과가 아니며 09:01 자동 State Machine 실행은 Step 13 `EGW00201` 장애로 FAILED 상태를 유지한다. 실제 ARN · account-id · subnet ID · Security Group ID · RDS endpoint · Secret 참조 · webhook URL · SHA256 원문은 본 문서에 기록하지 않는다. Kiro 는 본 회차에서 문서만 갱신한다. 신규 Risk ID · Decision ID · Decision Summary count · Risk Dashboard count 변경 없음. 상세는 `WORKLOG.md` 2026-07-20 섹션 · `specs/_common/risk-register.md` R-AUTO-023 · `specs/_common/operator-decisions.md` OD-MS-009 · OD-MS-030 · OD-MS-031 Details · `specs/_common/followups-overview.md` Done recently 2026-07-20 · `specs/_common/aws-resource-glossary.md` Usage Notes 2026-07-20 참조.

[^approval-slack-2026-07-15]: 2026-07-15 오후 회차에서 Approval Required Slack 이 서로 다른 Daily Run 과 Execution Plan 을 혼합해 실제 DB 에 존재하지 않는 상태 `DEFENSIVE` · 기준일 `2026-07-09` · 신호 4건 · 후보 4건 조합을 표시하던 문제가 확정되었다. Builder 가 최신 Execution Plan(`latestPlanId=133` · `latestPlanDate=2026-07-09`) 과 최신 Daily Run(`latestDailyRunId=73`) 을 각각 독립 조회한 것이 원인이었다. 운영자가 직접 Builder 조회 기준을 단일 `daily_run_id` 로 확정하고 상태 · 기준일 · 신호 수 · 후보 수를 동일한 Daily Run 기준으로 조회하도록 수정 · `source_daily_run_id` 로 연결된 Execution Order 만 조회 · 해당 Order 가 참조하는 Execution Plan 만 사용 · 연결된 Plan 이 없으면 `latestPlanId=null` 로 처리했다. 후보 표시는 실제 `company_name` 또는 `ticker_code` 사용 · Builder 가 후보별 점수를 구조화하여 Notifier 로 전달 · Notifier 가 소수점 셋째 자리까지 표시 · 한글 라벨(종합 · 수급 · 정보 · 추세 · 공매도) 적용 · 원본 `buy_info` dict Slack 노출 제거로 개선했다. `portfolio-approval-slack-summary-builder` · `portfolio-event-notifier` 재배포는 두 Lambda 모두 `Active` · `LastUpdateStatus=Successful` · Runtime · Handler 변경 없음 · 이전 버전 롤백 ZIP 확보 완료. Builder 실제 DB 검증은 상태 `AGGRESSIVE` · 기준일 2026-07-10 · 신호 4건 · 후보 4건 · `latestPlanId=null` · 후보 4건 정상(DL · 대주전자재료 · 삼성SDI · 한국피아이엠) · 후보별 5개 점수 필드 정상. Builder → Notifier → Slack 메시지 표시 E2E 는 완료 (실제 자동 주문 체결 E2E 는 아니며 · `latestPlanId=null` 은 선택된 Daily Run 에 연결된 Execution Order · Plan 이 없음을 의미하며 임의의 과거 Plan 을 사용하지 않았다는 검증 결과다). 다음 자동 Scheduler 실전 수신 관찰은 후속으로 유지한다. Kiro 는 본 회차에서 문서만 갱신한다. 신규 Decision ID · Risk ID · Decision Summary count · Risk Dashboard count 변경 없음. 상세는 `WORKLOG.md` 2026-07-15 오후 Approval Slack 섹션 · `specs/04-strategy-batch-stepfunctions/operation-notes.md` 2026-07-15 (오후) 섹션 · `specs/_common/operator-decisions.md` OD-MS-031 Details · `specs/_common/followups-overview.md` Done recently 2026-07-15 row 참조.

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
