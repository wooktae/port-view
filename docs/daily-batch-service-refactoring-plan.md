# DailyBatchService 분리 설계 계획

이 문서는 `DailyBatchService`를 실제로 분리하기 전 책임 경계, 후보 클래스, 단계별 실행 순서, 검증 기준을 정리한다. 이번 계획은 설계 문서이며 Java 코드는 수정하지 않는다.

## 1. 현재 DailyBatchService 책임 요약

`DailyBatchService`는 현재 Daily Batch의 조회, 실행 준비, 실행 오케스트레이션, 외부 프로세스 실행, 결과 판정, 상태 저장, Slack 알림까지 담당한다.

- 화면 조회 DTO 조립
  - `getLatestPage()`
  - `getPage(Long batchRunId)`
  - `getAvailableStepOptions()`
  - `getRecentIntradayChecks()`
- batch run 생성과 상태 전이
  - `startDailyPipelineRun()`
  - `startDailyPipelineRunFromStep(String fromStepCode)`
  - `startDailyPipelineRerunFailed(Long parentBatchRunId)`
  - `executeDailyPipeline(Long batchRunId)`
  - `markRunFailedByAsyncException(Long batchRunId, Exception e)`
- step registry와 step definition
  - `buildSteps()`
  - `buildIntradayPositionMonitorStep()`
  - `findBatchStep(...)`
  - 내부 record `BatchStep`
- 외부 Python 프로세스 실행
  - `executeStep(BatchStep step)`
  - 내부 class `StreamCollector`
  - 내부 record `StepExecutionResult`
- stdout/stderr 수집과 timeout 처리
  - `StreamCollector`
  - `process.waitFor(...)`
  - `process.destroyForcibly()`
  - `tail(String value)`
- result payload JSON 생성
  - `buildStepResultPayload(...)`
  - `jsonObject(...)`
  - `jsonEscape(...)`
- step log 저장과 run summary 갱신
  - `createPendingSteps(...)`
  - `repository.markStepRunning(...)`
  - `repository.markStepSuccess(...)`
  - `repository.markStepNoTarget(...)`
  - `repository.markStepFailed(...)`
  - `repository.markPendingStepsSkippedAfterFailure(...)`
  - `repository.refreshRunStepSummary(...)`
- retry/from-step/rerun-failed 흐름
  - `startDailyPipelineRunFromStep(...)`
  - `startDailyPipelineRerunFailed(...)`
- Slack summary 호출
  - `sendDailyBatchSlackSafely(Long batchRunId)`
- Intraday Monitor 단독 실행
  - `runIntradayPositionMonitor()`

## 2. 분리 후보 클래스 목록

| 후보 클래스 | 책임 | 입력/출력 | 주요 의존성 | 이동 대상 메서드 후보 |
|---|---|---|---|---|
| `DailyBatchQueryService` | Daily Batch 화면 조회 모델 조립 | batchRunId optional -> `DailyBatchPageDto`, step option/check list | `DailyBatchRepository` | `getLatestPage`, `getPage`, `getAvailableStepOptions`, `getRecentIntradayChecks` |
| `DailyBatchStepRegistry` | step 정의와 step 조회 | properties -> step definition list | `DailyBatchProperties` | `buildSteps`, `buildIntradayPositionMonitorStep`, `findBatchStep`, `BatchStep` |
| `ExternalProcessRunner` | 외부 프로세스 실행, timeout, exit code 반환 | command/workDir/timeout -> process result | 없음 또는 timeout config | `executeStep`의 ProcessBuilder 영역 |
| `ProcessOutputCollector` | stdout/stderr stream 수집 | input stream -> text | 없음 | `StreamCollector` |
| `DailyBatchResultPayloadBuilder` | request/result payload JSON 생성 | key/value, step/result -> JSON string | 없음 | `jsonObject`, `jsonEscape`, `buildStepResultPayload` |
| `DailyBatchRunService` | run 생성, pending step 생성, run 상태 전이 | run type/step list/parent run -> batchRunId | `DailyBatchRepository`, `DailyBatchStepRegistry`, `DailyBatchResultPayloadBuilder`, `DailyBatchProperties` | `startDailyPipelineRun`, `createPendingSteps`, run payload 생성 |
| `DailyBatchRetryPlanner` | from-step/rerun-failed 실행 구간 산정 | fromStepCode 또는 parentRunId -> retry step list | `DailyBatchRepository`, `DailyBatchStepRegistry` | `startDailyPipelineRunFromStep`의 step slicing, `startDailyPipelineRerunFailed`의 failed step 탐색 |
| `DailyBatchStepExecutor` | step 실행 결과 판정과 step log 저장 | batchRunId, pending step logs -> final status | `ExternalProcessRunner`, `DailyBatchRepository`, `DailyBatchResultPayloadBuilder`, `DailyBatchStepRegistry`, `DailyBatchProperties` | `executeDailyPipeline` loop, `isNoTarget`, `tail`, `firstNonBlank`, `stackTraceText` |
| `DailyBatchOrchestrator` | 전체 pipeline 실행 흐름 제어 | batchRunId -> run success/failed | `DailyBatchRunService`, `DailyBatchStepExecutor`, `DailyBatchSlackNotifier` | `executeDailyPipeline`, `markRunFailedByAsyncException`의 최상위 orchestration |
| `DailyBatchSlackNotifier` | Daily Batch 결과 Slack 알림 안전 호출 | batchRunId -> void | `SlackNotificationService` | `sendDailyBatchSlackSafely` |
| `IntradayPositionMonitorService` | Intraday monitor 단독 실행과 메시지 변환 | none -> result message | `DailyBatchStepRegistry` 또는 command factory, `ExternalProcessRunner`, `DailyBatchProperties` | `runIntradayPositionMonitor` |

## 3. 후보별 책임/이동 대상 메서드/의존성 상세

### DailyBatchQueryService

- 책임: 화면 진입에 필요한 조회 전용 DTO 조립.
- 이동 대상:
  - `getLatestPage()`
  - `getPage(Long batchRunId)`
  - `getAvailableStepOptions()`
  - `getRecentIntradayChecks()`
- 의존성:
  - `DailyBatchRepository`
  - `DailyBatchStepRegistry`
- 주의:
  - `DailyBatchController`가 현재 `DailyBatchService`만 의존하므로 facade를 유지하거나 controller 변경을 별도 단계로 분리한다.

### DailyBatchStepRegistry

- 책임: Daily Batch step code, 순서, 이름, workDir, command를 한 곳에서 제공.
- 이동 대상:
  - `buildSteps()`
  - `buildIntradayPositionMonitorStep()`
  - `findBatchStep(...)`
  - 내부 record `BatchStep`
- 의존성:
  - `DailyBatchProperties`
- 주의:
  - step 순서와 code는 rerun/from-step 동작의 기준이다. 첫 분리 시 출력 리스트가 기존과 완전히 같아야 한다.

### ExternalProcessRunner / ProcessOutputCollector

- 책임: Java `ProcessBuilder` 실행과 stdout/stderr 수집을 Daily Batch 도메인 로직에서 분리.
- 이동 대상:
  - `executeStep(...)` 중 ProcessBuilder 생성, environment 설정, stream thread 시작, wait/timeout, exit code 반환
  - `StreamCollector`
  - `StepExecutionResult`
- 의존성:
  - timeout 값 또는 실행 옵션 객체
- 주의:
  - `PYTHONIOENCODING`, `PYTHONUTF8` 환경 변수 설정은 반드시 유지한다.
  - timeout 시 process 강제 종료와 stream join 순서를 유지한다.

### DailyBatchResultPayloadBuilder

- 책임: request/result JSON 문자열 생성.
- 이동 대상:
  - `jsonObject(...)`
  - `jsonEscape(...)`
  - `buildStepResultPayload(...)`
- 의존성:
  - 없음
- 주의:
  - 기존 payload key 이름과 값 문자열을 바꾸지 않는다.
  - ObjectMapper 도입은 별도 작업으로 미룬다.

### DailyBatchRunService / DailyBatchRetryPlanner

- 책임:
  - running batch 중복 방지
  - run row 생성
  - pending step log 생성
  - from-step/retry 대상 step 계산
- 이동 대상:
  - `startDailyPipelineRun()`
  - `startDailyPipelineRunFromStep(...)`
  - `startDailyPipelineRerunFailed(...)`
  - `createPendingSteps(...)`
- 의존성:
  - `DailyBatchRepository`
  - `DailyBatchProperties`
  - `DailyBatchStepRegistry`
  - `DailyBatchResultPayloadBuilder`
- 주의:
  - runType, requestedBy, requestPayload source 문자열은 유지한다.
  - 기존 exception 메시지가 화면 flash message에 노출되므로 문구 변경은 최소화한다.

### DailyBatchStepExecutor / DailyBatchOrchestrator

- 책임:
  - pending step 순서대로 실행
  - RUNNING, SUCCESS, NO_TARGET, FAILED, SKIPPED 판정
  - run 최종 성공/실패 상태 전이
- 이동 대상:
  - `executeDailyPipeline(...)`
  - `isNoTarget(...)`
  - `tail(...)`
  - `firstNonBlank(...)`
  - `stackTraceText(...)`
  - `markRunFailedByAsyncException(...)`
- 의존성:
  - `DailyBatchRepository`
  - `DailyBatchStepRegistry`
  - `ExternalProcessRunner`
  - `DailyBatchResultPayloadBuilder`
  - `DailyBatchSlackNotifier`
  - `DailyBatchProperties`
- 주의:
  - 가장 위험한 영역이다. first split 대상으로 삼지 않는다.
  - step loop 중 `refreshRunStepSummary` 호출 타이밍을 바꾸지 않는다.

### DailyBatchSlackNotifier

- 책임: Slack 알림 실패가 Daily Batch 결과를 실패로 바꾸지 않도록 안전하게 감싼다.
- 이동 대상:
  - `sendDailyBatchSlackSafely(Long batchRunId)`
- 의존성:
  - `SlackNotificationService`
- 주의:
  - 로그/출력에 webhook URL이나 민감정보를 남기지 않는다.
  - Slack 알림 실패를 batch 실패로 전파하지 않는 기존 정책을 유지한다.

## 4. 1차 분리 추천 대상

가장 먼저 실제 코드로 분리할 후보는 `DailyBatchStepRegistry`이다.

이유:

- DB 상태 전이와 외부 프로세스 실행을 건드리지 않는다.
- 입력은 `DailyBatchProperties`, 출력은 step definition list로 경계가 명확하다.
- `getAvailableStepOptions`, `startDailyPipelineRun`, from-step/retry, 실행 loop가 같은 step source를 쓰도록 유지하면 동작 변화가 작다.
- compile 검증과 step option 화면 확인으로 1차 검증이 가능하다.

1차 분리 시 권장 방식:

- `DailyBatchService`에 facade public method는 유지한다.
- 내부 `BatchStep` record를 새 package-private 또는 public nested-independent type으로 옮길지 먼저 결정한다.
- `buildSteps`, `buildIntradayPositionMonitorStep`, `findBatchStep`만 이동한다.
- step code/order/name/workDir/command 값이 기존과 동일한지 diff 또는 테스트로 확인한다.

## 5. 2차/3차 분리 계획

### 1차

1. `DailyBatchStepRegistry` 분리
2. `DailyBatchSlackNotifier` 분리
3. `DailyBatchResultPayloadBuilder` 분리

### 2차

1. `DailyBatchQueryService` 분리
2. `ExternalProcessRunner`와 `ProcessOutputCollector` 분리
3. `IntradayPositionMonitorService` 분리

### 3차

1. `DailyBatchRunService` 분리
2. `DailyBatchRetryPlanner` 분리
3. `DailyBatchStepExecutor` 분리
4. `DailyBatchOrchestrator` 도입 여부 결정

## 6. 위험 영역

- `executeDailyPipeline(...)`의 step loop
  - step 실행 순서, 실패 시 skip 처리, summary refresh 타이밍이 모두 결과에 영향.
- `isNoTarget(...)`
  - stdout/stderr 문구 기반 판정이라 작은 변경도 SUCCESS/NO_TARGET/FAILED 결과를 바꿀 수 있음.
- retry/from-step/rerun-failed
  - step order 기준 slicing이 틀어지면 중복 실행 또는 누락 실행 가능.
- 외부 프로세스 실행
  - workDir, command, timeout, UTF-8 환경 변수, stream join 순서 변경 위험.
- Slack summary 호출
  - 실패를 삼키는 정책 유지 필요.
- request/result payload
  - DB에 남는 JSON key와 source 값은 운영 추적에 쓰이므로 변경 최소화.

## 7. 검증 체크리스트

기본 검증:

- `.\mvnw.cmd clean compile`
- Daily Batch 화면 진입: `/daily-batch`
- 특정 run 상세 진입: `/daily-batch/{batchRunId}`
- Step option 목록 표시 확인
- 최근 Intraday Monitor 결과 표시 확인

실행 검증:

- Manual run
- from-step run
- rerun-failed
- Intraday Monitor 단독 실행
- Slack test message
- Daily Batch Slack summary test

상태 검증:

- `SUCCESS` step
- `NO_TARGET` step
- `FAILED` step
- 실패 이후 pending step `SKIPPED`
- run 최종 `SUCCESS`
- run 최종 `FAILED`
- timeout 케이스

DB/로그 검증:

- `strategy_daily_batch_run` row 생성과 최종 상태
- `strategy_daily_batch_step_log` row 생성과 순서
- stdout/stderr tail 저장 길이
- request_payload/result_payload JSON key 유지
- 민감정보 값 미출력

## 8. 롤백 전략

- 분리는 한 PR/commit에 하나의 책임만 이동한다.
- public API와 controller 호출부를 유지하는 facade 방식으로 시작한다.
- 문제가 생기면 새 클래스로 이동한 메서드 호출부만 기존 `DailyBatchService` 내부 구현으로 되돌린다.
- DB schema, property key, template, endpoint를 동시에 변경하지 않는다.
- 외부 프로세스 실행 분리 단계는 timeout/NO_TARGET/FAILED 케이스 검증 전까지 다른 리팩토링과 묶지 않는다.

## 9. 완료 기준

- `DailyBatchService`가 orchestration facade 수준으로 축소된다.
- step 정의, process 실행, payload 생성, query DTO 조립, retry planning, Slack notify 책임이 분리된다.
- 기존 public method signature와 controller 흐름이 유지된다.
- Manual/from-step/rerun-failed/Slack summary/Intraday Monitor가 기존과 동일하게 동작한다.
- `SUCCESS`, `NO_TARGET`, `FAILED`, `SKIPPED` 상태 전이가 기존과 동일하다.
- 민감정보 값이 로그, 문서, diff에 포함되지 않는다.
