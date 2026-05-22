package my.portfolio.port_view.repository;

import lombok.RequiredArgsConstructor;
import my.portfolio.port_view.dto.DailyBatchRunDto;
import my.portfolio.port_view.dto.DailyBatchStepLogDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import my.portfolio.port_view.dto.IntradayPositionCheckDto;

import my.portfolio.port_view.dto.BlockWatchCandidateDto;

@Repository
@RequiredArgsConstructor
public class DailyBatchRepository {

    private static final int RECENT_RUN_LIMIT = 20;

    private final JdbcTemplate jdbcTemplate;

    // =====================================================
    // Query - Run
    // =====================================================

    public boolean existsRunningBatch() {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM strategy_daily_batch_run
                    WHERE run_status = 'RUNNING'
                )
                """;

        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    public Optional<DailyBatchRunDto> findRunningBatch() {
        String sql = """
                SELECT
                    id,
                    batch_date,
                    run_type,
                    run_status,
                    requested_by,
                    environment,
                    account_no,
                    current_step_code,
                    current_step_name,
                    total_step_count,
                    success_step_count,
                    failed_step_count,
                    skipped_step_count,
                    no_target_step_count,
                    started_at,
                    finished_at,
                    duration_ms,
                    error_step_code,
                    error_message,
                    request_payload::text AS request_payload,
                    result_payload::text AS result_payload,
                    created_at,
                    updated_at
                FROM strategy_daily_batch_run
                WHERE run_status = 'RUNNING'
                ORDER BY started_at DESC NULLS LAST, id DESC
                LIMIT 1
                """;

        List<DailyBatchRunDto> rows = jdbcTemplate.query(sql, this::mapRun);
        return rows.stream().findFirst();
    }

    public Optional<DailyBatchRunDto> findLatestRun() {
        String sql = """
                SELECT
                    id,
                    batch_date,
                    run_type,
                    run_status,
                    requested_by,
                    environment,
                    account_no,
                    current_step_code,
                    current_step_name,
                    total_step_count,
                    success_step_count,
                    failed_step_count,
                    skipped_step_count,
                    no_target_step_count,
                    started_at,
                    finished_at,
                    duration_ms,
                    error_step_code,
                    error_message,
                    request_payload::text AS request_payload,
                    result_payload::text AS result_payload,
                    created_at,
                    updated_at
                FROM strategy_daily_batch_run
                ORDER BY created_at DESC, id DESC
                LIMIT 1
                """;

        List<DailyBatchRunDto> rows = jdbcTemplate.query(sql, this::mapRun);
        return rows.stream().findFirst();
    }

    public Optional<DailyBatchRunDto> findRunById(Long batchRunId) {
        String sql = """
                SELECT
                    id,
                    batch_date,
                    run_type,
                    run_status,
                    requested_by,
                    environment,
                    account_no,
                    current_step_code,
                    current_step_name,
                    total_step_count,
                    success_step_count,
                    failed_step_count,
                    skipped_step_count,
                    no_target_step_count,
                    started_at,
                    finished_at,
                    duration_ms,
                    error_step_code,
                    error_message,
                    request_payload::text AS request_payload,
                    result_payload::text AS result_payload,
                    created_at,
                    updated_at
                FROM strategy_daily_batch_run
                WHERE id = ?
                """;

        List<DailyBatchRunDto> rows = jdbcTemplate.query(sql, this::mapRun, batchRunId);
        return rows.stream().findFirst();
    }

    public List<DailyBatchRunDto> findRecentRuns() {
        String sql = """
                SELECT
                    id,
                    batch_date,
                    run_type,
                    run_status,
                    requested_by,
                    environment,
                    account_no,
                    current_step_code,
                    current_step_name,
                    total_step_count,
                    success_step_count,
                    failed_step_count,
                    skipped_step_count,
                    no_target_step_count,
                    started_at,
                    finished_at,
                    duration_ms,
                    error_step_code,
                    error_message,
                    request_payload::text AS request_payload,
                    result_payload::text AS result_payload,
                    created_at,
                    updated_at
                FROM strategy_daily_batch_run
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, this::mapRun, RECENT_RUN_LIMIT);
    }

    // =====================================================
    // Query - Step
    // =====================================================

    public List<DailyBatchStepLogDto> findSteps(Long batchRunId) {
        String sql = """
                SELECT
                    id,
                    batch_run_id,
                    step_order,
                    step_code,
                    step_name,
                    step_status,
                    work_dir,
                    command_text,
                    exit_code,
                    started_at,
                    finished_at,
                    duration_ms,
                    stdout_tail,
                    stderr_tail,
                    error_message,
                    result_payload::text AS result_payload,
                    created_at,
                    updated_at
                FROM strategy_daily_batch_step_log
                WHERE batch_run_id = ?
                ORDER BY step_order ASC
                """;

        return jdbcTemplate.query(sql, this::mapStep, batchRunId);
    }

    public List<IntradayPositionCheckDto> findRecentIntradayChecks(int limit) {
        String sql = """
                SELECT
                    id,
                    check_ts,
                    position_state_id,
                    ticker_code,
                    stock_name,
                    current_price,
                    pnl_rate,
                    hard_stop_rate,
                    should_stop,
                    stop_reason,
                    created_execution_order_id,
                    warning_count
                FROM strategy_intraday_position_check
                ORDER BY check_ts DESC, id DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, this::mapIntradayCheck, limit);
    }

    public List<BlockWatchCandidateDto> findLatestBlockWatchCandidates(int limit) {
        String sql = """
                WITH latest_watch_date AS (
                    SELECT run_date
                    FROM strategy_block_watch_candidate
                    ORDER BY run_date DESC, id DESC
                    LIMIT 1
                )
                SELECT
                    id,
                    daily_run_id,
                    run_date,
                    data_date,
                    ticker_code,
                    stock_name,
                    market_signal,
                    watch_status,
                    flow_score,
                    final_score,
                    info_score,
                    volatility_20d,
                    intraday_range,
                    short_pressure_score,
                    watch_reason,
                    close_price,
                    return_1d,
                    return_3d,
                    return_5d,
                    return_10d,
                    created_at,
                    updated_at
                FROM strategy_block_watch_candidate
                WHERE run_date = (SELECT run_date FROM latest_watch_date)
                ORDER BY final_score DESC NULLS LAST, flow_score DESC NULLS LAST, id ASC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, this::mapBlockWatchCandidate, limit);
    }

    // =====================================================
    // Command - Run
    // =====================================================

    public Long createRun(
            LocalDate batchDate,
            String runType,
            String requestedBy,
            String environment,
            String accountNo,
            int totalStepCount,
            String requestPayload
    ) {
        String sql = """
                INSERT INTO strategy_daily_batch_run (
                    batch_date,
                    run_type,
                    run_status,
                    requested_by,
                    environment,
                    account_no,
                    total_step_count,
                    started_at,
                    request_payload
                )
                VALUES (
                    ?,
                    ?,
                    'RUNNING',
                    ?,
                    ?,
                    ?,
                    ?,
                    now(),
                    ?::jsonb
                )
                RETURNING id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                batchDate,
                runType,
                requestedBy,
                environment,
                accountNo,
                totalStepCount,
                requestPayload
        );
    }

    public void updateRunCurrentStep(
            Long batchRunId,
            String stepCode,
            String stepName
    ) {
        String sql = """
                UPDATE strategy_daily_batch_run
                SET
                    current_step_code = ?,
                    current_step_name = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, stepCode, stepName, batchRunId);
    }

    public void markRunSuccess(
            Long batchRunId,
            String resultPayload
    ) {
        String sql = """
                UPDATE strategy_daily_batch_run
                SET
                    run_status = 'SUCCESS',
                    current_step_code = NULL,
                    current_step_name = NULL,
                    finished_at = now(),
                    duration_ms = CASE
                        WHEN started_at IS NULL THEN NULL
                        ELSE CAST(EXTRACT(EPOCH FROM (now() - started_at)) * 1000 AS BIGINT)
                    END,
                    error_step_code = NULL,
                    error_message = NULL,
                    result_payload = ?::jsonb
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, resultPayload, batchRunId);
        refreshRunStepSummary(batchRunId);
    }

    public void markRunFailed(
            Long batchRunId,
            String errorStepCode,
            String errorMessage,
            String resultPayload
    ) {
        String sql = """
                UPDATE strategy_daily_batch_run
                SET
                    run_status = 'FAILED',
                    current_step_code = NULL,
                    current_step_name = NULL,
                    finished_at = now(),
                    duration_ms = CASE
                        WHEN started_at IS NULL THEN NULL
                        ELSE CAST(EXTRACT(EPOCH FROM (now() - started_at)) * 1000 AS BIGINT)
                    END,
                    error_step_code = ?,
                    error_message = ?,
                    result_payload = ?::jsonb
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                errorStepCode,
                truncate(errorMessage, 8000),
                resultPayload,
                batchRunId
        );

        refreshRunStepSummary(batchRunId);
    }

    public void refreshRunStepSummary(Long batchRunId) {
        String sql = """
                UPDATE strategy_daily_batch_run r
                SET
                    total_step_count = summary.total_count,
                    success_step_count = summary.success_count,
                    failed_step_count = summary.failed_count,
                    skipped_step_count = summary.skipped_count,
                    no_target_step_count = summary.no_target_count
                FROM (
                    SELECT
                        batch_run_id,
                        COUNT(*)::int AS total_count,
                        COUNT(*) FILTER (WHERE step_status = 'SUCCESS')::int AS success_count,
                        COUNT(*) FILTER (WHERE step_status = 'FAILED')::int AS failed_count,
                        COUNT(*) FILTER (WHERE step_status = 'SKIPPED')::int AS skipped_count,
                        COUNT(*) FILTER (WHERE step_status = 'NO_TARGET')::int AS no_target_count
                    FROM strategy_daily_batch_step_log
                    WHERE batch_run_id = ?
                    GROUP BY batch_run_id
                ) summary
                WHERE r.id = summary.batch_run_id
                """;

        jdbcTemplate.update(sql, batchRunId);
    }

    // =====================================================
    // Command - Step
    // =====================================================

    public Long createStep(
            Long batchRunId,
            int stepOrder,
            String stepCode,
            String stepName,
            String workDir,
            String commandText
    ) {
        String sql = """
                INSERT INTO strategy_daily_batch_step_log (
                    batch_run_id,
                    step_order,
                    step_code,
                    step_name,
                    step_status,
                    work_dir,
                    command_text
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    'PENDING',
                    ?,
                    ?
                )
                RETURNING id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                batchRunId,
                stepOrder,
                stepCode,
                stepName,
                workDir,
                commandText
        );
    }

    public void markStepRunning(Long stepLogId) {
        String sql = """
                UPDATE strategy_daily_batch_step_log
                SET
                    step_status = 'RUNNING',
                    started_at = now(),
                    finished_at = NULL,
                    duration_ms = NULL,
                    exit_code = NULL,
                    stdout_tail = NULL,
                    stderr_tail = NULL,
                    error_message = NULL,
                    result_payload = NULL
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, stepLogId);
    }

    public void markStepSuccess(
            Long stepLogId,
            Integer exitCode,
            String stdoutTail,
            String stderrTail,
            String resultPayload
    ) {
        String sql = """
                UPDATE strategy_daily_batch_step_log
                SET
                    step_status = 'SUCCESS',
                    exit_code = ?,
                    finished_at = now(),
                    duration_ms = CASE
                        WHEN started_at IS NULL THEN NULL
                        ELSE CAST(EXTRACT(EPOCH FROM (now() - started_at)) * 1000 AS BIGINT)
                    END,
                    stdout_tail = ?,
                    stderr_tail = ?,
                    error_message = NULL,
                    result_payload = ?::jsonb
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                exitCode,
                stdoutTail,
                stderrTail,
                resultPayload,
                stepLogId
        );
    }

    public void markStepNoTarget(
            Long stepLogId,
            Integer exitCode,
            String stdoutTail,
            String stderrTail,
            String resultPayload
    ) {
        String sql = """
                UPDATE strategy_daily_batch_step_log
                SET
                    step_status = 'NO_TARGET',
                    exit_code = ?,
                    finished_at = now(),
                    duration_ms = CASE
                        WHEN started_at IS NULL THEN NULL
                        ELSE CAST(EXTRACT(EPOCH FROM (now() - started_at)) * 1000 AS BIGINT)
                    END,
                    stdout_tail = ?,
                    stderr_tail = ?,
                    error_message = NULL,
                    result_payload = ?::jsonb
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                exitCode,
                stdoutTail,
                stderrTail,
                resultPayload,
                stepLogId
        );
    }

    public void markStepFailed(
            Long stepLogId,
            Integer exitCode,
            String stdoutTail,
            String stderrTail,
            String errorMessage,
            String resultPayload
    ) {
        String sql = """
                UPDATE strategy_daily_batch_step_log
                SET
                    step_status = 'FAILED',
                    exit_code = ?,
                    finished_at = now(),
                    duration_ms = CASE
                        WHEN started_at IS NULL THEN NULL
                        ELSE CAST(EXTRACT(EPOCH FROM (now() - started_at)) * 1000 AS BIGINT)
                    END,
                    stdout_tail = ?,
                    stderr_tail = ?,
                    error_message = ?,
                    result_payload = ?::jsonb
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                exitCode,
                stdoutTail,
                stderrTail,
                truncate(errorMessage, 8000),
                resultPayload,
                stepLogId
        );
    }

    public void markStepSkipped(
            Long stepLogId,
            String errorMessage,
            String resultPayload
    ) {
        String sql = """
                UPDATE strategy_daily_batch_step_log
                SET
                    step_status = 'SKIPPED',
                    finished_at = now(),
                    duration_ms = CASE
                        WHEN started_at IS NULL THEN 0
                        ELSE CAST(EXTRACT(EPOCH FROM (now() - started_at)) * 1000 AS BIGINT)
                    END,
                    error_message = ?,
                    result_payload = ?::jsonb
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                truncate(errorMessage, 8000),
                resultPayload,
                stepLogId
        );
    }

    public void markPendingStepsSkippedAfterFailure(
            Long batchRunId,
            String failedStepCode,
            String reason
    ) {
        String sql = """
                UPDATE strategy_daily_batch_step_log
                SET
                    step_status = 'SKIPPED',
                    finished_at = now(),
                    duration_ms = 0,
                    error_message = ?,
                    result_payload = jsonb_build_object(
                        'source', 'DailyBatchRepository.markPendingStepsSkippedAfterFailure',
                        'failed_step_code', ?,
                        'reason', ?
                    )
                WHERE batch_run_id = ?
                  AND step_status = 'PENDING'
                """;

        jdbcTemplate.update(
                sql,
                truncate(reason, 8000),
                failedStepCode,
                truncate(reason, 8000),
                batchRunId
        );
    }

    // =====================================================
    // Mapper
    // =====================================================

    private DailyBatchRunDto mapRun(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new DailyBatchRunDto(
                rs.getLong("id"),
                rs.getObject("batch_date", java.time.LocalDate.class),
                rs.getString("run_type"),
                rs.getString("run_status"),
                rs.getString("requested_by"),
                rs.getString("environment"),
                rs.getString("account_no"),
                rs.getString("current_step_code"),
                rs.getString("current_step_name"),
                getInteger(rs, "total_step_count"),
                getInteger(rs, "success_step_count"),
                getInteger(rs, "failed_step_count"),
                getInteger(rs, "skipped_step_count"),
                getInteger(rs, "no_target_step_count"),
                rs.getObject("started_at", OffsetDateTime.class),
                rs.getObject("finished_at", OffsetDateTime.class),
                getLong(rs, "duration_ms"),
                rs.getString("error_step_code"),
                rs.getString("error_message"),
                rs.getString("request_payload"),
                rs.getString("result_payload"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("updated_at", OffsetDateTime.class)
        );
    }

    private DailyBatchStepLogDto mapStep(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new DailyBatchStepLogDto(
                rs.getLong("id"),
                rs.getLong("batch_run_id"),
                getInteger(rs, "step_order"),
                rs.getString("step_code"),
                rs.getString("step_name"),
                rs.getString("step_status"),
                rs.getString("work_dir"),
                rs.getString("command_text"),
                getInteger(rs, "exit_code"),
                rs.getObject("started_at", OffsetDateTime.class),
                rs.getObject("finished_at", OffsetDateTime.class),
                getLong(rs, "duration_ms"),
                rs.getString("stdout_tail"),
                rs.getString("stderr_tail"),
                rs.getString("error_message"),
                rs.getString("result_payload"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("updated_at", OffsetDateTime.class)
        );
    }

    private IntradayPositionCheckDto mapIntradayCheck(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {
        return new IntradayPositionCheckDto(
                rs.getLong("id"),
                rs.getObject("check_ts", OffsetDateTime.class),
                getLong(rs, "position_state_id"),
                rs.getString("ticker_code"),
                rs.getString("stock_name"),
                rs.getBigDecimal("current_price"),
                rs.getBigDecimal("pnl_rate"),
                rs.getBigDecimal("hard_stop_rate"),
                rs.getBoolean("should_stop"),
                rs.getString("stop_reason"),
                getLong(rs, "created_execution_order_id"),
                getInteger(rs, "warning_count")
        );
    }

    private BlockWatchCandidateDto mapBlockWatchCandidate(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {
        return new BlockWatchCandidateDto(
                rs.getLong("id"),
                rs.getLong("daily_run_id"),
                rs.getObject("run_date", LocalDate.class),
                rs.getObject("data_date", LocalDate.class),
                rs.getString("ticker_code"),
                rs.getString("stock_name"),
                rs.getString("market_signal"),
                rs.getString("watch_status"),
                rs.getBigDecimal("flow_score"),
                rs.getBigDecimal("final_score"),
                rs.getBigDecimal("info_score"),
                rs.getBigDecimal("volatility_20d"),
                rs.getBigDecimal("intraday_range"),
                rs.getBigDecimal("short_pressure_score"),
                rs.getString("watch_reason"),
                rs.getBigDecimal("close_price"),
                rs.getBigDecimal("return_1d"),
                rs.getBigDecimal("return_3d"),
                rs.getBigDecimal("return_5d"),
                rs.getBigDecimal("return_10d"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("updated_at", OffsetDateTime.class)
        );
    }

    // =====================================================
    // Helpers
    // =====================================================

    private Integer getInteger(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private Long getLong(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}