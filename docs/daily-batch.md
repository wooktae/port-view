# Daily Batch

Daily Batch는 portfolio 관련 외부 모듈을 순차 실행하고, 실행 상태를 화면에서 확인할 수 있도록 DB에 run/step 로그를 남기는 기능입니다.

## 목적

- Daily 전략 데이터 생성/분석/실행 흐름을 한 화면에서 실행하고 추적
- 각 step의 시작/종료/성공/실패 상태 기록
- 실패 step 재실행 또는 특정 step부터 재실행 지원
- 실행 결과를 Slack summary로 공유
- Intraday Monitor를 별도 수동 실행 가능하게 제공

## 주요 진입점

Controller:

- `DailyBatchController`

Service:

- `DailyBatchService`
- `DailyBatchAsyncService`
- `SlackNotificationService`

Repository:

- `DailyBatchRepository`

화면:

- `/daily-batch`
- `/daily-batch/{batchRunId}`

## 전체 Step 흐름

Step 정의는 현재 `DailyBatchService` 내부에서 구성됩니다. 각 step은 외부 project directory, command, 설명, 실행 조건 등을 갖는 내부 record 형태로 관리됩니다.

문서 기준으로 확인 가능한 외부 모듈 범주:

- Market Connector
- Interest Crawler
- Interest Preprocessor
- Strategy Research
- Strategy Decision
- Strategy Execution
- Backtest/Report 계열 step
- Slack summary

정확한 command와 step code는 코드의 `DailyBatchService.buildSteps()` 및 관련 builder method를 기준으로 확인해야 합니다.

## 수동 실행

화면 또는 POST endpoint를 통해 전체 Daily Pipeline 실행을 시작합니다.

- `POST /daily-batch/run`

흐름:

1. batch run row 생성
2. async executor로 실행 위임
3. step log 생성
4. step 순차 실행
5. run 최종 상태 갱신
6. Slack summary 전송 시도

## 특정 Step부터 재실행

특정 step code부터 이후 step을 실행하는 흐름입니다.

- `POST /daily-batch/run-from-step`

사용 목적:

- 앞 단계 결과는 유지하고 뒤 단계만 다시 실행
- 외부 모듈 오류 수정 후 중간부터 재처리

주의:

- step 사이의 데이터 의존성을 고려해야 합니다.
- 이전 step 결과가 유효하지 않으면 뒤 step만 재실행해도 기대한 결과가 나오지 않을 수 있습니다.

## 실패 Step 재실행

기존 batch run의 실패 step을 기준으로 재실행하는 흐름입니다.

- `POST /daily-batch/{batchRunId}/rerun-failed`

사용 목적:

- 실패한 batch run을 기준으로 실패 이후 구간을 재처리
- 실패 원인을 수정한 뒤 재실행 이력을 별도 run으로 남김

## Intraday Monitor

Intraday Monitor는 Daily Batch 전체 pipeline과 별도로 실행할 수 있는 수동 액션입니다.

- `POST /daily-batch/intraday-monitor/run`

역할:

- 장중 position 관련 check 또는 monitor 성격의 외부 command 실행
- 최근 check 결과를 Daily Batch 화면에서 함께 조회

## Slack Summary

Slack summary는 `SlackNotificationService`가 담당합니다.

지원 액션:

- Slack 테스트 메시지
  - `POST /daily-batch/slack-test`
- 특정 batch run summary 테스트
  - `POST /daily-batch/{batchRunId}/slack-summary-test`
- Daily Batch 완료 후 summary 전송 시도

메시지에 포함되는 정보:

- Batch run 상태
- Step 상태 요약
- Daily Run 요약
- Strategy Execution 요약
- Balance/Position 요약

Webhook URL은 secret이므로 문서나 로그에 실제 값을 남기지 않습니다.

## DB 테이블 역할

### strategy_daily_batch_run

Batch 실행 단위의 header 역할을 합니다.

주요 의미:

- batch run id
- run type
- account no
- run status
- 시작/종료 시각
- 실패 step
- error message/result payload

### strategy_daily_batch_step_log

Batch run에 속한 step 실행 로그입니다.

주요 의미:

- batch run id
- step code/name
- step status
- command
- started/finished timestamp
- exit code
- stdout/stderr tail
- result payload

## 상태 의미

현재 문서에서는 화면과 util에서 확인되는 상태 의미를 기준으로 설명합니다.

### SUCCESS

Step 또는 run이 정상 완료된 상태입니다.

### FAILED

외부 command 실패, 예외 발생, timeout 등으로 정상 완료되지 않은 상태입니다.

### SKIPPED

실행 조건상 해당 step을 건너뛴 상태입니다. 특정 step부터 재실행하거나 실패 step 재실행 시 앞 단계가 skip될 수 있습니다.

### NO_TARGET

실행 자체는 되었지만 처리 대상이 없는 상태를 나타내는 후보 상태입니다. 예를 들어 대상 데이터가 없어 후속 액션이 필요하지 않은 경우에 사용될 수 있습니다.

## 운영 시 주의사항

- 외부 project 경로가 정확해야 합니다.
- Python 실행 파일은 환경별로 달라질 수 있습니다.
- command timeout은 step 특성에 맞게 조정해야 합니다.
- stdout/stderr는 tail만 저장하므로 전체 로그가 필요한 경우 외부 로그 정책이 필요합니다.
- 계좌번호, webhook, DB password 등 민감정보는 환경변수 또는 local config로 분리해야 합니다.
