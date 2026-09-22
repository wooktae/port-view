package my.portfolio.port_view.service;

import lombok.RequiredArgsConstructor;
import my.portfolio.port_view.config.DailyBatchProperties;
import my.portfolio.port_view.dto.dailybatch.BlockWatchCandidateDto;
import my.portfolio.port_view.dto.dailybatch.DailyBatchPageDto;
import my.portfolio.port_view.dto.dailybatch.DailyBatchRunDto;
import my.portfolio.port_view.dto.dailybatch.DailyBatchStepLogDto;
import my.portfolio.port_view.dto.dailybatch.DailyBatchStepOptionDto;
import my.portfolio.port_view.dto.dailybatch.IntradayPositionCheckDto;
import my.portfolio.port_view.repository.DailyBatchRepository;
import my.portfolio.port_view.util.ViewMessages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DailyBatchService {

    private static final String RUN_TYPE_MANUAL = "MANUAL";
    private static final String RUN_TYPE_MANUAL_PARTIAL = "MANUAL_PARTIAL";
    private static final String RUN_TYPE_RETRY = "RETRY";
    private static final String REQUESTED_BY_VIEW_BUTTON = "VIEW_BUTTON";

    private static final String DB_USER_MARKETCONNECTOR = "marketconnector_app";
    private static final String DB_USER_CRAWLER = "crawler_app";
    private static final String DB_USER_PREPROCESSOR = "preprocessor_app";
    private static final String DB_USER_RESEARCH = "research_app";
    private static final String DB_USER_DECISION = "decision_app";
    private static final String DB_USER_EXECUTION = "execution_app";

    private static final String DAILY_AUTO_BUY_STEP_CODE = "DAILY_AUTO_BUY";
    private static final int BROKER_SUBMIT_STEP_ORDER = 12;
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalTime KOREA_REGULAR_MARKET_OPEN_TIME = LocalTime.of(9, 0);
    private static final String DAILY_AUTO_BUY_BEFORE_MARKET_OPEN_MESSAGE =
            "정규장 시작 전이므로 자동 매수 실행을 보류합니다. 09:00 이후 다시 실행하세요.";

    private final DailyBatchRepository repository;
    private final DailyBatchProperties properties;
    private final SlackNotificationService slackNotificationService;

    // =====================================================
    // View Page
    // =====================================================

    @Transactional(readOnly = true)
    public DailyBatchPageDto getLatestPage() {
        Optional<DailyBatchRunDto> latestRun = repository.findLatestRun();

        List<DailyBatchStepLogDto> steps = latestRun
                .map(run -> repository.findSteps(run.id()))
                .orElseGet(List::of);

        List<DailyBatchRunDto> recentRuns = repository.findRecentRuns();

        Optional<DailyBatchRunDto> runningBatch = repository.findRunningBatch();

        List<BlockWatchCandidateDto> blockWatchCandidates = repository.findLatestBlockWatchCandidates(20);

        return new DailyBatchPageDto(
                latestRun.orElse(null),
                steps,
                recentRuns,
                blockWatchCandidates,
                runningBatch.isPresent(),
                runningBatch.map(DailyBatchRunDto::id).orElse(null)
        );
    }

    @Transactional(readOnly = true)
    public DailyBatchPageDto getPage(Long batchRunId) {
        DailyBatchRunDto selectedRun = repository.findRunById(batchRunId)
                .orElseThrow(() -> new IllegalArgumentException("Daily Batch 실행 정보를 찾을 수 없음. batchRunId=" + batchRunId));

        List<DailyBatchStepLogDto> steps = repository.findSteps(batchRunId);
        List<DailyBatchRunDto> recentRuns = repository.findRecentRuns();
        Optional<DailyBatchRunDto> runningBatch = repository.findRunningBatch();

        List<BlockWatchCandidateDto> blockWatchCandidates = repository.findLatestBlockWatchCandidates(20);

        return new DailyBatchPageDto(
                selectedRun,
                steps,
                recentRuns,
                blockWatchCandidates,
                runningBatch.isPresent(),
                runningBatch.map(DailyBatchRunDto::id).orElse(null)
        );
    }

    public Long startDailyPipelineRun() {
        assertFullLocalFilePipelineExecutionAllowed("Daily Pipeline 전체 실행");

        List<BatchStep> steps = buildSteps();
        assertStepRangeAllowed(steps, "Daily Pipeline 전체 실행");

        return createRunWithSteps(
                RUN_TYPE_MANUAL,
                "DailyBatchService.startDailyPipelineRun",
                steps,
                "FULL_PIPELINE",
                null,
                null
        );
    }

    public Long startDailyPipelineRunRange(String fromStepCode, String toStepCode) {
        assertLocalFileBatchExecutionAllowed("Daily Pipeline 범위 실행");

        if (fromStepCode == null || fromStepCode.isBlank()) {
            throw new IllegalArgumentException("시작 stepCode가 비어 있음.");
        }

        if (toStepCode == null || toStepCode.isBlank()) {
            throw new IllegalArgumentException("종료 stepCode가 비어 있음.");
        }

        List<BatchStep> allSteps = buildSteps();

        BatchStep fromStep = findBatchStep(allSteps, fromStepCode);
        BatchStep toStep = findBatchStep(allSteps, toStepCode);

        if (fromStep.stepOrder() > toStep.stepOrder()) {
            throw new IllegalArgumentException(
                    "시작 step이 종료 step보다 뒤에 있음. fromStepCode="
                            + fromStepCode
                            + ", toStepCode="
                            + toStepCode
            );
        }

        List<BatchStep> rangeSteps = allSteps.stream()
                .filter(step -> step.stepOrder() >= fromStep.stepOrder())
                .filter(step -> step.stepOrder() <= toStep.stepOrder())
                .toList();

        if (rangeSteps.isEmpty()) {
            throw new IllegalStateException(
                    "범위 실행 대상 step이 없음. fromStepCode="
                            + fromStepCode
                            + ", toStepCode="
                            + toStepCode
            );
        }

        assertStepRangeAllowed(rangeSteps, "Daily Pipeline 범위 실행");

        return createRunWithSteps(
                RUN_TYPE_MANUAL_PARTIAL,
                "DailyBatchService.startDailyPipelineRunRange",
                rangeSteps,
                "RANGE",
                fromStep,
                toStep
        );
    }

    public Long startDailyPipelineRunFromStep(String fromStepCode) {
        assertLocalFileBatchExecutionAllowed("Daily Pipeline 부분 실행");

        if (fromStepCode == null || fromStepCode.isBlank()) {
            throw new IllegalArgumentException("재실행 시작 stepCode가 비어 있음.");
        }

        List<BatchStep> allSteps = buildSteps();
        BatchStep fromStep = findBatchStep(allSteps, fromStepCode);

        List<BatchStep> partialSteps = allSteps.stream()
                .filter(step -> step.stepOrder() >= fromStep.stepOrder())
                .toList();

        if (partialSteps.isEmpty()) {
            throw new IllegalStateException("부분 실행 대상 step이 없음. fromStepCode=" + fromStepCode);
        }

        assertStepRangeAllowed(partialSteps, "Daily Pipeline 부분 실행");

        return createRunWithSteps(
                RUN_TYPE_MANUAL_PARTIAL,
                "DailyBatchService.startDailyPipelineRunFromStep",
                partialSteps,
                "FROM_STEP",
                fromStep,
                null
        );
    }

    public Long startDailyPipelineRerunFailed(Long parentBatchRunId) {
        assertLocalFileBatchExecutionAllowed("Daily Pipeline 실패 단계 재실행");

        if (parentBatchRunId == null) {
            throw new IllegalArgumentException("parentBatchRunId가 비어 있음.");
        }

        assertNoRunningBatch();

        DailyBatchRunDto parentRun = repository.findRunById(parentBatchRunId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "재실행 대상 Daily Batch를 찾을 수 없음. parentBatchRunId=" + parentBatchRunId
                ));

        List<DailyBatchStepLogDto> parentSteps = repository.findSteps(parentBatchRunId);

        DailyBatchStepLogDto failedStep = parentSteps.stream()
                .filter(DailyBatchStepLogDto::isFailed)
                .sorted(Comparator.comparing(step -> step.stepOrder() == null ? 0 : step.stepOrder()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "재실행할 FAILED step이 없음. parentBatchRunId=" + parentBatchRunId
                ));

        List<BatchStep> allSteps = buildSteps();
        BatchStep fromStep = findBatchStep(allSteps, failedStep.stepCode());

        List<BatchStep> retrySteps = allSteps.stream()
                .filter(step -> step.stepOrder() >= fromStep.stepOrder())
                .toList();

        if (retrySteps.isEmpty()) {
            throw new IllegalStateException("재실행 대상 step이 없음. failedStepCode=" + failedStep.stepCode());
        }

        assertStepRangeAllowed(retrySteps, "Daily Pipeline 실패 단계 재실행");

        String requestPayload = jsonObject(
                "source", "DailyBatchService.startDailyPipelineRerunFailed",
                "runType", RUN_TYPE_RETRY,
                "requestedBy", REQUESTED_BY_VIEW_BUTTON,
                "accountNo", properties.getDefaultAccountNo(),
                "environment", properties.getEnvironment(),
                "dbTarget", properties.getDbTarget(),
                "executionMode", properties.getExecutionMode(),
                "totalStepCount", String.valueOf(retrySteps.size()),
                "asyncMode", "ASYNC",
                "rerunMode", "FAILED_STEP",
                "parentBatchRunId", String.valueOf(parentBatchRunId),
                "parentRunStatus", parentRun.runStatus(),
                "failedStepCode", fromStep.stepCode(),
                "failedStepName", fromStep.stepName(),
                "failedStepOrder", String.valueOf(fromStep.stepOrder()),
                "paperOrderEnabled", String.valueOf(properties.isPaperOrderEnabled()),
                "minExecutableStepOrder", String.valueOf(properties.getMinExecutableStepOrder()),
                "maxExecutableStepOrder", String.valueOf(properties.getMaxExecutableStepOrder())
        );

        Long batchRunId = repository.createRun(
                LocalDate.now(),
                RUN_TYPE_RETRY,
                REQUESTED_BY_VIEW_BUTTON,
                properties.getEnvironment(),
                properties.getDefaultAccountNo(),
                retrySteps.size(),
                requestPayload
        );

        createPendingSteps(batchRunId, retrySteps);

        return batchRunId;
    }

    private Long createRunWithSteps(
            String runType,
            String source,
            List<BatchStep> steps,
            String restartMode,
            BatchStep fromStep,
            BatchStep toStep
    ) {
        assertNoRunningBatch();

        String requestPayload = jsonObject(
                "source", source,
                "runType", runType,
                "requestedBy", REQUESTED_BY_VIEW_BUTTON,
                "accountNo", properties.getDefaultAccountNo(),
                "environment", properties.getEnvironment(),
                "dbTarget", properties.getDbTarget(),
                "executionMode", properties.getExecutionMode(),
                "totalStepCount", String.valueOf(steps.size()),
                "asyncMode", "ASYNC",
                "restartMode", restartMode,
                "restartFromStepCode", fromStep == null ? null : fromStep.stepCode(),
                "restartFromStepName", fromStep == null ? null : fromStep.stepName(),
                "restartFromStepOrder", fromStep == null ? null : String.valueOf(fromStep.stepOrder()),
                "restartToStepCode", toStep == null ? null : toStep.stepCode(),
                "restartToStepName", toStep == null ? null : toStep.stepName(),
                "restartToStepOrder", toStep == null ? null : String.valueOf(toStep.stepOrder()),
                "paperOrderEnabled", String.valueOf(properties.isPaperOrderEnabled()),
                "fullPipelineExecutionEnabled", String.valueOf(properties.isFullPipelineExecutionEnabled()),
                "minExecutableStepOrder", String.valueOf(properties.getMinExecutableStepOrder()),
                "maxExecutableStepOrder", String.valueOf(properties.getMaxExecutableStepOrder())
        );

        Long batchRunId = repository.createRun(
                LocalDate.now(),
                runType,
                REQUESTED_BY_VIEW_BUTTON,
                properties.getEnvironment(),
                properties.getDefaultAccountNo(),
                steps.size(),
                requestPayload
        );

        createPendingSteps(batchRunId, steps);

        return batchRunId;
    }

    public void executeDailyPipeline(Long batchRunId) {
        assertLocalFileBatchExecutionAllowed("Daily Pipeline step 실행");

        DailyBatchRunDto run = repository.findRunById(batchRunId)
                .orElseThrow(() -> new IllegalArgumentException("Daily Batch 실행 정보를 찾을 수 없음. batchRunId=" + batchRunId));

        if (!run.isRunning()) {
            throw new IllegalStateException("RUNNING 상태의 Daily Batch만 실행 가능. batchRunId=" + batchRunId + ", runStatus=" + run.runStatus());
        }

        List<BatchStep> steps = buildSteps();

        List<DailyBatchStepLogDto> pendingStepLogs = repository.findSteps(batchRunId)
                .stream()
                .filter(step -> "PENDING".equalsIgnoreCase(step.stepStatus()))
                .sorted(Comparator.comparing(step -> step.stepOrder() == null ? 0 : step.stepOrder()))
                .toList();

        boolean failed = false;
        String failedStepCode = null;
        String failedMessage = null;

        for (DailyBatchStepLogDto stepLog : pendingStepLogs) {
            if (failed) {
                continue;
            }

            BatchStep step = findBatchStep(steps, stepLog.stepCode());

            repository.updateRunCurrentStep(
                    batchRunId,
                    step.stepCode(),
                    step.stepName()
            );

            repository.markStepRunning(stepLog.id());

            StepExecutionResult result = executeStep(step);

            if (result.timedOut()) {
                failed = true;
                failedStepCode = step.stepCode();
                failedMessage = "Step timeout. timeoutMinutes=" + properties.getCommandTimeoutMinutes();

                repository.markStepFailed(
                        stepLog.id(),
                        result.exitCode(),
                        tail(result.stdout()),
                        tail(result.stderr()),
                        failedMessage,
                        buildStepResultPayload(step, result)
                );
            } else if (result.exitCode() != 0) {
                failed = true;
                failedStepCode = step.stepCode();
                failedMessage = firstNonBlank(
                        result.stderr(),
                        result.stdout(),
                        "Step failed. exitCode=" + result.exitCode()
                );

                repository.markStepFailed(
                        stepLog.id(),
                        result.exitCode(),
                        tail(result.stdout()),
                        tail(result.stderr()),
                        failedMessage,
                        buildStepResultPayload(step, result)
                );
            } else if (isNoTarget(step, result.stdout(), result.stderr())) {
                repository.markStepNoTarget(
                        stepLog.id(),
                        result.exitCode(),
                        tail(result.stdout()),
                        tail(result.stderr()),
                        buildStepResultPayload(step, result)
                );
            } else {
                repository.markStepSuccess(
                        stepLog.id(),
                        result.exitCode(),
                        tail(result.stdout()),
                        tail(result.stderr()),
                        buildStepResultPayload(step, result)
                );
            }

            repository.refreshRunStepSummary(batchRunId);
        }

        if (failed) {
            String skipReason = "이전 단계 실패로 이후 단계 실행 생략. failedStepCode=" + failedStepCode;
            repository.markPendingStepsSkippedAfterFailure(batchRunId, failedStepCode, skipReason);

            repository.markRunFailed(
                    batchRunId,
                    failedStepCode,
                    failedMessage,
                    jsonObject(
                            "source", "DailyBatchService.executeDailyPipeline",
                            "status", "FAILED",
                            "failedStepCode", failedStepCode,
                            "message", failedMessage,
                            "environment", properties.getEnvironment(),
                            "dbTarget", properties.getDbTarget(),
                            "executionMode", properties.getExecutionMode(),
                            "paperOrderEnabled", String.valueOf(properties.isPaperOrderEnabled()),
                            "minExecutableStepOrder", String.valueOf(properties.getMinExecutableStepOrder()),
                            "maxExecutableStepOrder", String.valueOf(properties.getMaxExecutableStepOrder())
                    )
            );
        } else {
            repository.markRunSuccess(
                    batchRunId,
                    jsonObject(
                            "source", "DailyBatchService.executeDailyPipeline",
                            "status", "SUCCESS",
                            "message", "Daily Pipeline completed",
                            "environment", properties.getEnvironment(),
                            "dbTarget", properties.getDbTarget(),
                            "executionMode", properties.getExecutionMode(),
                            "asyncMode", "ASYNC",
                            "paperOrderEnabled", String.valueOf(properties.isPaperOrderEnabled()),
                            "minExecutableStepOrder", String.valueOf(properties.getMinExecutableStepOrder()),
                            "maxExecutableStepOrder", String.valueOf(properties.getMaxExecutableStepOrder())
                    )
            );
        }

        repository.refreshRunStepSummary(batchRunId);
        sendDailyBatchSlackSafely(batchRunId);
    }

    public void markRunFailedByAsyncException(Long batchRunId, Exception e) {
        String message = stackTraceText(e);

        repository.markRunFailed(
                batchRunId,
                "ASYNC_EXECUTION",
                message,
                jsonObject(
                        "source", "DailyBatchService.markRunFailedByAsyncException",
                        "status", "FAILED",
                        "failedStepCode", "ASYNC_EXECUTION",
                        "message", message,
                        "environment", properties.getEnvironment(),
                        "dbTarget", properties.getDbTarget(),
                        "executionMode", properties.getExecutionMode(),
                        "paperOrderEnabled", String.valueOf(properties.isPaperOrderEnabled()),
                        "minExecutableStepOrder", String.valueOf(properties.getMinExecutableStepOrder()),
                        "maxExecutableStepOrder", String.valueOf(properties.getMaxExecutableStepOrder())
                )
        );

        repository.refreshRunStepSummary(batchRunId);
        sendDailyBatchSlackSafely(batchRunId);
    }

    private List<StepHandle> createPendingSteps(Long batchRunId, List<BatchStep> steps) {
        List<StepHandle> handles = new ArrayList<>();

        for (BatchStep step : steps) {
            Long stepLogId = repository.createStep(
                    batchRunId,
                    step.stepOrder(),
                    step.stepCode(),
                    step.stepName(),
                    step.workDir(),
                    step.commandText()
            );

            handles.add(new StepHandle(stepLogId, step));
        }

        repository.refreshRunStepSummary(batchRunId);

        return handles;
    }

    private BatchStep findBatchStep(List<BatchStep> steps, String stepCode) {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalStateException("Daily Batch step definition이 비어 있음.");
        }

        for (BatchStep step : steps) {
            if (step.stepCode().equals(stepCode)) {
                return step;
            }
        }

        throw new IllegalArgumentException("Daily Batch step definition을 찾을 수 없음. stepCode=" + stepCode);
    }

    @Transactional(readOnly = true)
    public List<DailyBatchStepOptionDto> getAvailableStepOptions() {
        return buildSteps()
                .stream()
                .map(step -> new DailyBatchStepOptionDto(
                        step.stepOrder(),
                        step.stepCode(),
                        step.stepName()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IntradayPositionCheckDto> getRecentIntradayChecks() {
        return repository.findRecentIntradayChecks(5);
    }

    public String runIntradayPositionMonitor() {
        assertIntradayMonitorAllowed();

        BatchStep step = buildIntradayPositionMonitorStep();

        StepExecutionResult result = executeStep(step);

        if (result.timedOut()) {
            throw new IllegalStateException(
                    "장중 포지션 점검 timeout. timeoutMinutes=" + properties.getCommandTimeoutMinutes()
            );
        }

        if (result.exitCode() != 0) {
            String message = firstNonBlank(
                    result.stderr(),
                    result.stdout(),
                    "장중 포지션 점검 실패. exitCode=" + result.exitCode()
            );

            throw new IllegalStateException(message);
        }

        String stdout = result.stdout() == null ? "" : result.stdout();

        if (stdout.contains("[ALERT]")) {
            return ViewMessages.text("intraday.monitor.alert");
        }

        if (stdout.contains("[NO_TARGET]")) {
            return ViewMessages.text("intraday.monitor.noTarget");
        }

        if (stdout.contains("장중 모니터링 대상 OPEN 포지션 없음")) {
            return ViewMessages.text("intraday.monitor.noOpen");
        }

        return ViewMessages.text("intraday.monitor.completed");
    }

    private void sendDailyBatchSlackSafely(Long batchRunId) {
        if (!properties.isSlackSummaryEnabled()) {
            System.out.println("[INFO] Daily Batch Slack summary disabled. batchRunId="
                    + batchRunId
                    + ", environment=" + properties.getEnvironment()
                    + ", dbTarget=" + properties.getDbTarget()
                    + ", executionMode=" + properties.getExecutionMode());
            return;
        }

        try {
            slackNotificationService.sendDailyBatchSummary(batchRunId);
        } catch (Exception e) {
            System.out.println("[WARN] Slack notification failed. batchRunId=" + batchRunId + ", message=" + e.getMessage());
        }
    }

    private BatchStep buildIntradayPositionMonitorStep() {
        String python = properties.getPythonExecutable();

        return new BatchStep(
                0,
                "INTRADAY_POSITION_MONITOR",
                "장중 포지션 점검",
                properties.getExecutionDir(),
                DB_USER_EXECUTION,
                List.of(
                        python,
                        "daily_intraday_position_monitor_run.py"
                )
        );
    }

    // =====================================================
    // Step Definition
    // =====================================================

    private List<BatchStep> buildSteps() {
        String python = properties.getPythonExecutable();

        List<BatchStep> steps = new ArrayList<>();

        steps.add(new BatchStep(
                1,
                "CONNECTOR_BALANCE",
                "잔고/보유종목 확인",
                properties.getMarketconnectorDir(),
                DB_USER_MARKETCONNECTOR,
                List.of(python, "connector_balance.py")
        ));

        steps.add(new BatchStep(
                2,
                "INTEREST_CRAWLER",
                "Interest 수집",
                properties.getInterestCrawlerDir(),
                DB_USER_CRAWLER,
                List.of(python, "interest_crawler_daily.py")
        ));

        steps.add(new BatchStep(
                3,
                "PREPROCESSOR",
                "Preprocessor 전처리",
                properties.getPreprocessorDir(),
                DB_USER_PREPROCESSOR,
                List.of(python, "pre_daily.py")
        ));

        steps.add(new BatchStep(
                4,
                "BACKTEST_RESEARCH",
                "백테스트 분석",
                properties.getWorkspaceRoot(),
                DB_USER_RESEARCH,
                List.of(python, "-m", "port_strategy_research.backtest_research_run")
        ));

        steps.add(new BatchStep(
                5,
                "BACKTEST_REPORT",
                "백테스트 리포트",
                properties.getWorkspaceRoot(),
                DB_USER_RESEARCH,
                List.of(python, "-m", "port_strategy_research.backtest_report_run")
        ));

        steps.add(new BatchStep(
                6,
                "DAILY_BUY_SIGNAL",
                "Daily 매수 신호",
                properties.getWorkspaceRoot(),
                DB_USER_DECISION,
                List.of(python, "-m", "port_strategy_decision.daily_buy_signal_run")
        ));

        steps.add(new BatchStep(
                7,
                "DAILY_POSITION_SIGNAL",
                "Daily 포지션 신호",
                properties.getWorkspaceRoot(),
                DB_USER_DECISION,
                List.of(python, "-m", "port_strategy_decision.daily_position_signal_run")
        ));

        steps.add(new BatchStep(
                8,
                "DAILY_BUY_EXECUTION",
                "Daily 매수 연결",
                properties.getExecutionDir(),
                DB_USER_EXECUTION,
                List.of(python, "daily_buy_execution_run.py")
        ));

        steps.add(new BatchStep(
                9,
                "DAILY_SELL_EXECUTION",
                "Daily 매도 연결",
                properties.getExecutionDir(),
                DB_USER_EXECUTION,
                List.of(python, "daily_sell_execution_run.py")
        ));

        steps.add(new BatchStep(
                10,
                "DAILY_AUTO_SELL",
                "자동 매도 실행",
                properties.getExecutionDir(),
                DB_USER_EXECUTION,
                List.of(python, "daily_auto_sell_execute_run.py", "--execute")
        ));

        steps.add(new BatchStep(
                11,
                "DAILY_AUTO_BUY",
                "자동 매수 실행",
                properties.getExecutionDir(),
                DB_USER_EXECUTION,
                List.of(python, "daily_auto_buy_execute_run.py", "--execute")
        ));

        steps.add(new BatchStep(
                12,
                "MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE",
                "Strategy 주문 실행",
                properties.getMarketconnectorDir(),
                DB_USER_MARKETCONNECTOR,
                List.of(python, "connector_strategy_order_execute.py", "--execute")
        ));

        steps.add(new BatchStep(
                13,
                "CONNECTOR_ORDER_CHECK",
                "체결 조회",
                properties.getMarketconnectorDir(),
                DB_USER_MARKETCONNECTOR,
                List.of(python, "connector_order_check.py")
        ));

        steps.add(new BatchStep(
                14,
                "SYNC_SELL_FILL",
                "매도 체결/포지션 동기화",
                properties.getExecutionDir(),
                DB_USER_EXECUTION,
                List.of(python, "execution_sync_sell_fill.py")
        ));

        steps.add(new BatchStep(
                15,
                "SYNC_BUY_FILL",
                "매수 체결 동기화",
                properties.getExecutionDir(),
                DB_USER_EXECUTION,
                List.of(python, "execution_sync_buy_fill.py")
        ));

        steps.add(new BatchStep(
                16,
                "SYNC_BUY_POSITION",
                "매수 포지션 동기화",
                properties.getExecutionDir(),
                DB_USER_EXECUTION,
                List.of(python, "execution_sync_buy_position.py")
        ));

        steps.add(new BatchStep(
                17,
                "BALANCE_REFRESH",
                "잔고/보유종목 최신화",
                properties.getMarketconnectorDir(),
                DB_USER_MARKETCONNECTOR,
                List.of(python, "connector_balance.py")
        ));

        return steps;
    }

    // =====================================================
    // Process Execute
    // =====================================================

    private StepExecutionResult executeStep(BatchStep step) {
        assertLocalFileBatchExecutionAllowed("Python step 실행: " + step.stepCode());
        assertStepAllowedForExecution(step, "Python step 실행");

        Process process = null;

        try {
            if (isDailyAutoBuyBlockedBeforeMarketOpen(step)) {
                return new StepExecutionResult(
                        0,
                        DAILY_AUTO_BUY_BEFORE_MARKET_OPEN_MESSAGE,
                        "",
                        false
                );
            }

            ProcessBuilder processBuilder = new ProcessBuilder(step.command());
            processBuilder.directory(new File(step.workDir()));

            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            processBuilder.environment().put("PYTHONUTF8", "1");

            applyDatabaseRoleEnvironment(processBuilder, step);

            process = processBuilder.start();

            StreamCollector stdoutCollector = new StreamCollector(process.getInputStream());
            StreamCollector stderrCollector = new StreamCollector(process.getErrorStream());

            Thread stdoutThread = new Thread(stdoutCollector, "daily-batch-stdout-" + step.stepCode());
            Thread stderrThread = new Thread(stderrCollector, "daily-batch-stderr-" + step.stepCode());

            stdoutThread.start();
            stderrThread.start();

            boolean finished = process.waitFor(
                    properties.getCommandTimeoutMinutes(),
                    TimeUnit.MINUTES
            );

            if (!finished) {
                process.destroyForcibly();

                stdoutThread.join(3000);
                stderrThread.join(3000);

                return new StepExecutionResult(
                        -1,
                        stdoutCollector.content(),
                        stderrCollector.content(),
                        true
                );
            }

            int exitCode = process.exitValue();

            stdoutThread.join(3000);
            stderrThread.join(3000);

            return new StepExecutionResult(
                    exitCode,
                    stdoutCollector.content(),
                    stderrCollector.content(),
                    false
            );
        } catch (Exception e) {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }

            return new StepExecutionResult(
                    -999,
                    "",
                    stackTraceText(e),
                    false
            );
        }
    }

    private void applyDatabaseRoleEnvironment(ProcessBuilder processBuilder, BatchStep step) {
        if (step.dbUser() == null || step.dbUser().isBlank()) {
            return;
        }

        processBuilder.environment().put("INTEREST_DB_USER", step.dbUser());

        String rolePassword = resolveRolePassword(step.dbUser());

        if (rolePassword != null && !rolePassword.isBlank()) {
            processBuilder.environment().put("INTEREST_DB_PASSWORD", rolePassword);
        }

        System.out.println("[DailyBatchService] step DB role override. stepCode="
                + step.stepCode()
                + ", dbUser="
                + step.dbUser()
                + ", passwordOverride="
                + (rolePassword != null && !rolePassword.isBlank() ? "YES" : "NO"));
    }

    private String resolveRolePassword(String dbUser) {
        if (dbUser == null || dbUser.isBlank()) {
            return null;
        }

        String normalized = dbUser.trim().toUpperCase(Locale.ROOT).replace("-", "_");

        String servicePrefix = switch (dbUser) {
            case DB_USER_MARKETCONNECTOR -> "MARKETCONNECTOR";
            case DB_USER_CRAWLER -> "CRAWLER";
            case DB_USER_PREPROCESSOR -> "PREPROCESSOR";
            case DB_USER_RESEARCH -> "RESEARCH";
            case DB_USER_DECISION -> "DECISION";
            case DB_USER_EXECUTION -> "EXECUTION";
            default -> normalized.replace("_APP", "");
        };

        return firstNonBlankEnv(
                "PORTFOLIO_BATCH_" + servicePrefix + "_DB_PASSWORD",
                servicePrefix + "_DB_PASSWORD",
                "INTEREST_DB_PASSWORD_" + servicePrefix,
                "PORTFOLIO_" + servicePrefix + "_DB_PASSWORD",
                "PORTFOLIO_BATCH_" + normalized + "_DB_PASSWORD",
                normalized + "_DB_PASSWORD"
        );
    }

    private String firstNonBlankEnv(String... keys) {
        if (keys == null) {
            return null;
        }

        for (String key : keys) {
            if (key == null || key.isBlank()) {
                continue;
            }

            String value = System.getenv(key);

            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private boolean isNoTarget(BatchStep step, String stdout, String stderr) {
        String text = (
                (stdout == null ? "" : stdout) + "\n" +
                        (stderr == null ? "" : stderr)
        ).toLowerCase();

        if (text.isBlank()) {
            return false;
        }

        if (DAILY_AUTO_BUY_STEP_CODE.equals(step.stepCode())) {
            return text.contains("자동매수 대상 ready daily buy 주문이 없음".toLowerCase())
                    || text.contains("자동매수 대상")
                    || text.contains(DAILY_AUTO_BUY_BEFORE_MARKET_OPEN_MESSAGE.toLowerCase())
                    || text.contains("한국 영업일이 아니므로 주문 관련 처리를 실행하지 않습니다".toLowerCase())
                    || text.contains("connector_order_request를 생성하지 않고 종료합니다".toLowerCase())
                    || text.contains("weekend")
                    || text.contains("kr_holiday")
                    || text.contains("no target")
                    || text.contains("no ready");
        }

        if ("DAILY_AUTO_SELL".equals(step.stepCode())) {
            return text.contains("자동매도 대상 ready sell 주문이 없음".toLowerCase())
                    || text.contains("자동매도 대상")
                    || text.contains("ready sell 주문이 없음".toLowerCase())
                    || text.contains("실행 가능한 자동매도 주문이 없음")
                    || text.contains("한국 영업일이 아니므로 주문 관련 처리를 실행하지 않습니다".toLowerCase())
                    || text.contains("connector_order_request를 생성하지 않고 종료합니다".toLowerCase())
                    || text.contains("weekend")
                    || text.contains("kr_holiday")
                    || text.contains("no target")
                    || text.contains("no ready");
        }

        if ("MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE".equals(step.stepCode())) {
            return text.contains("requested strategy order 없음")
                    || text.contains("strategy execution requested 주문이 없음")
                    || text.contains("[no_target] requested strategy order 없음")
                    || text.contains("no requested")
                    || text.contains("no target");
        }

        if ("DAILY_SELL_EXECUTION".equals(step.stepCode())) {
            return text.contains("생성할 daily position sell execution order 후보가 없음".toLowerCase())
                    || text.contains("sell execution order 후보가 없음".toLowerCase())
                    || text.contains("한국 영업일이 아니므로 주문 관련 처리를 실행하지 않습니다".toLowerCase())
                    || text.contains("weekend")
                    || text.contains("kr_holiday")
                    || text.contains("no target")
                    || text.contains("no candidate");
        }

        if ("DAILY_BUY_EXECUTION".equals(step.stepCode())) {
            return text.contains("생성할 daily signal execution order 후보가 없음".toLowerCase())
                    || text.contains("execution order 후보가 없음".toLowerCase())
                    || text.contains("한국 영업일이 아니므로 주문 관련 처리를 실행하지 않습니다".toLowerCase())
                    || text.contains("weekend")
                    || text.contains("kr_holiday")
                    || text.contains("no target")
                    || text.contains("no candidate");
        }

        return false;
    }

    private boolean isDailyAutoBuyBlockedBeforeMarketOpen(BatchStep step) {
        return DAILY_AUTO_BUY_STEP_CODE.equals(step.stepCode())
                && LocalTime.now(KOREA_ZONE_ID).isBefore(KOREA_REGULAR_MARKET_OPEN_TIME);
    }

    private void assertNoRunningBatch() {
        if (repository.existsRunningBatch()) {
            DailyBatchRunDto running = repository.findRunningBatch()
                    .orElse(null);

            String suffix = running == null ? "" : " runningBatchRunId=" + running.id();
            throw new IllegalStateException("이미 실행 중인 Daily Batch가 있음." + suffix);
        }
    }

    private void assertLocalFileBatchExecutionAllowed(String actionName) {
        if (properties.canRunLocalFileBatch()) {
            return;
        }

        throw new IllegalStateException(
                actionName + " 차단됨. "
                        + "Daily Batch local-file 실행이 비활성화되어 있습니다. "
                        + modeText()
        );
    }

    private void assertFullLocalFilePipelineExecutionAllowed(String actionName) {
        if (properties.canRunFullLocalFilePipeline()) {
            return;
        }

        throw new IllegalStateException(
                actionName + " 차단됨. "
                        + "전체 1~17 실행은 현재 비활성화되어 있습니다. "
                        + modeText()
        );
    }

    private void assertIntradayMonitorAllowed() {
        if (properties.isExecutionEnabled()
                && properties.isLocalFileMode()
                && properties.isLocalFileExecutionEnabled()
                && properties.isIntradayMonitorEnabled()) {
            return;
        }

        throw new IllegalStateException(
                "장중 포지션 점검 차단됨. "
                        + "intraday-monitor 실행이 비활성화되어 있습니다. "
                        + modeText()
        );
    }

    private void assertStepRangeAllowed(List<BatchStep> steps, String actionName) {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalStateException(actionName + " 차단됨. 실행 대상 step이 없습니다.");
        }

        for (BatchStep step : steps) {
            assertStepAllowedForExecution(step, actionName);
        }
    }

    private void assertStepAllowedForExecution(BatchStep step, String actionName) {
        if (step == null) {
            throw new IllegalStateException(actionName + " 차단됨. step 정의가 없습니다.");
        }

        if (step.stepOrder() < properties.getMinExecutableStepOrder()
                || step.stepOrder() > properties.getMaxExecutableStepOrder()) {
            throw new IllegalStateException(
                    actionName + " 차단됨. "
                            + "허용된 step 범위를 벗어났습니다. "
                            + "stepOrder=" + step.stepOrder()
                            + ", stepCode=" + step.stepCode()
                            + ", allowedRange="
                            + properties.getMinExecutableStepOrder()
                            + "~"
                            + properties.getMaxExecutableStepOrder()
                            + ". "
                            + modeText()
            );
        }

        if (step.stepOrder() >= BROKER_SUBMIT_STEP_ORDER && !properties.isPaperOrderEnabled()) {
            throw new IllegalStateException(
                    actionName + " 차단됨. "
                            + "Step 12~17은 Paper 주문 실행 gate가 꺼져 있어 실행할 수 없습니다. "
                            + "stepOrder=" + step.stepOrder()
                            + ", stepCode=" + step.stepCode()
                            + ", paperOrderEnabled=" + properties.isPaperOrderEnabled()
                            + ". "
                            + modeText()
            );
        }
    }

    private String modeText() {
        return "environment=" + properties.getEnvironment()
                + ", dbTarget=" + properties.getDbTarget()
                + ", executionMode=" + properties.getExecutionMode()
                + ", executionEnabled=" + properties.isExecutionEnabled()
                + ", localFileExecutionEnabled=" + properties.isLocalFileExecutionEnabled()
                + ", fullPipelineExecutionEnabled=" + properties.isFullPipelineExecutionEnabled()
                + ", paperOrderEnabled=" + properties.isPaperOrderEnabled()
                + ", minExecutableStepOrder=" + properties.getMinExecutableStepOrder()
                + ", maxExecutableStepOrder=" + properties.getMaxExecutableStepOrder()
                + ", awsStepfunctionsStartEnabled=" + properties.isAwsStepfunctionsStartEnabled()
                + ", intradayMonitorEnabled=" + properties.isIntradayMonitorEnabled()
                + ", slackSummaryEnabled=" + properties.isSlackSummaryEnabled();
    }

    // =====================================================
    // Payload / Text Helpers
    // =====================================================

    private String buildStepResultPayload(BatchStep step, StepExecutionResult result) {
        return jsonObject(
                "source", "DailyBatchService.executeStep",
                "stepCode", step.stepCode(),
                "stepName", step.stepName(),
                "workDir", step.workDir(),
                "command", step.commandText(),
                "dbUser", step.dbUser(),
                "exitCode", String.valueOf(result.exitCode()),
                "timedOut", String.valueOf(result.timedOut()),
                "environment", properties.getEnvironment(),
                "dbTarget", properties.getDbTarget(),
                "executionMode", properties.getExecutionMode(),
                "paperOrderEnabled", String.valueOf(properties.isPaperOrderEnabled()),
                "minExecutableStepOrder", String.valueOf(properties.getMinExecutableStepOrder()),
                "maxExecutableStepOrder", String.valueOf(properties.getMaxExecutableStepOrder())
        );
    }

    private String tail(String value) {
        if (value == null) {
            return null;
        }

        int maxLength = properties.getLogTailLength();

        if (maxLength <= 0) {
            maxLength = 8000;
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(value.length() - maxLength);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return tail(value);
            }
        }

        return null;
    }

    private String stackTraceText(Exception e) {
        StringBuilder sb = new StringBuilder();

        sb.append(e.getClass().getName())
                .append(": ")
                .append(e.getMessage() == null ? "" : e.getMessage());

        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(System.lineSeparator())
                    .append("    at ")
                    .append(element);
        }

        return sb.toString();
    }

    private String jsonObject(String... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            if (i > 0) {
                sb.append(",");
            }

            sb.append("\"")
                    .append(jsonEscape(keyValues[i]))
                    .append("\":");

            String value = keyValues[i + 1];

            if (value == null) {
                sb.append("null");
            } else {
                sb.append("\"")
                        .append(jsonEscape(value))
                        .append("\"");
            }
        }

        sb.append("}");
        return sb.toString();
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }

        return sb.toString();
    }

    // =====================================================
    // Internal Records
    // =====================================================

    private record BatchStep(
            int stepOrder,
            String stepCode,
            String stepName,
            String workDir,
            String dbUser,
            List<String> command
    ) {
        String commandText() {
            return String.join(" ", command);
        }
    }

    private record StepHandle(
            Long stepLogId,
            BatchStep step
    ) {
    }

    private record StepExecutionResult(
            int exitCode,
            String stdout,
            String stderr,
            boolean timedOut
    ) {
    }

    private static class StreamCollector implements Runnable {

        private final InputStream inputStream;
        private final StringBuilder content = new StringBuilder();

        private StreamCollector(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            Charset charset = java.nio.charset.StandardCharsets.UTF_8;

            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream, charset)
                    )
            ) {
                String line;

                while ((line = reader.readLine()) != null) {
                    content.append(line).append(System.lineSeparator());
                }
            } catch (Exception e) {
                content.append("[STREAM_READ_ERROR] ")
                        .append(e.getClass().getSimpleName())
                        .append(": ")
                        .append(e.getMessage())
                        .append(System.lineSeparator());
            }
        }

        private String content() {
            return content.toString();
        }
    }
}