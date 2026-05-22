package my.portfolio.port_view.repository;

import lombok.RequiredArgsConstructor;
import my.portfolio.port_view.dto.DailyPositionDecisionDto;
import my.portfolio.port_view.dto.DailyRunDto;
import my.portfolio.port_view.dto.DailySignalDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StrategyDailyViewRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<DailyRunDto> findLatestDailyRun() {
        String sql = """
                SELECT
                    id,
                    strategy_name,
                    strategy_version,
                    run_date,
                    data_date,
                    run_type,
                    run_status,
                    market_signal,
                    base_exposure,
                    max_positions,
                    candidate_count,
                    signal_count,
                    run_note,
                    error_message,
                    started_at,
                    finished_at,
                    created_at,
                    updated_at
                FROM strategy_daily_run
                ORDER BY run_date DESC, data_date DESC, id DESC
                LIMIT 1
                """;

        List<DailyRunDto> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new DailyRunDto(
                rs.getLong("id"),
                rs.getString("strategy_name"),
                rs.getString("strategy_version"),
                rs.getObject("run_date", java.time.LocalDate.class),
                rs.getObject("data_date", java.time.LocalDate.class),
                rs.getString("run_type"),
                rs.getString("run_status"),
                rs.getString("market_signal"),
                rs.getBigDecimal("base_exposure"),
                rs.getObject("max_positions") == null ? null : rs.getInt("max_positions"),
                rs.getObject("candidate_count") == null ? null : rs.getInt("candidate_count"),
                rs.getObject("signal_count") == null ? null : rs.getInt("signal_count"),
                rs.getString("run_note"),
                rs.getString("error_message"),
                rs.getObject("started_at", java.time.OffsetDateTime.class),
                rs.getObject("finished_at", java.time.OffsetDateTime.class),
                rs.getObject("created_at", java.time.OffsetDateTime.class),
                rs.getObject("updated_at", java.time.OffsetDateTime.class)
        ));

        return rows.stream().findFirst();
    }

    public Optional<DailyRunDto> findDailyRunById(Long dailyRunId) {
        String sql = """
                SELECT
                    id,
                    strategy_name,
                    strategy_version,
                    run_date,
                    data_date,
                    run_type,
                    run_status,
                    market_signal,
                    base_exposure,
                    max_positions,
                    candidate_count,
                    signal_count,
                    run_note,
                    error_message,
                    started_at,
                    finished_at,
                    created_at,
                    updated_at
                FROM strategy_daily_run
                WHERE id = ?
                """;

        List<DailyRunDto> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new DailyRunDto(
                rs.getLong("id"),
                rs.getString("strategy_name"),
                rs.getString("strategy_version"),
                rs.getObject("run_date", java.time.LocalDate.class),
                rs.getObject("data_date", java.time.LocalDate.class),
                rs.getString("run_type"),
                rs.getString("run_status"),
                rs.getString("market_signal"),
                rs.getBigDecimal("base_exposure"),
                rs.getObject("max_positions") == null ? null : rs.getInt("max_positions"),
                rs.getObject("candidate_count") == null ? null : rs.getInt("candidate_count"),
                rs.getObject("signal_count") == null ? null : rs.getInt("signal_count"),
                rs.getString("run_note"),
                rs.getString("error_message"),
                rs.getObject("started_at", java.time.OffsetDateTime.class),
                rs.getObject("finished_at", java.time.OffsetDateTime.class),
                rs.getObject("created_at", java.time.OffsetDateTime.class),
                rs.getObject("updated_at", java.time.OffsetDateTime.class)
        ), dailyRunId);

        return rows.stream().findFirst();
    }

    public List<DailyRunDto> findRecentDailyRuns() {
        String sql = """
                SELECT
                    id,
                    strategy_name,
                    strategy_version,
                    run_date,
                    data_date,
                    run_type,
                    run_status,
                    market_signal,
                    base_exposure,
                    max_positions,
                    candidate_count,
                    signal_count,
                    run_note,
                    error_message,
                    started_at,
                    finished_at,
                    created_at,
                    updated_at
                FROM strategy_daily_run
                ORDER BY run_date DESC, data_date DESC, id DESC
                LIMIT 20
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new DailyRunDto(
                rs.getLong("id"),
                rs.getString("strategy_name"),
                rs.getString("strategy_version"),
                rs.getObject("run_date", java.time.LocalDate.class),
                rs.getObject("data_date", java.time.LocalDate.class),
                rs.getString("run_type"),
                rs.getString("run_status"),
                rs.getString("market_signal"),
                rs.getBigDecimal("base_exposure"),
                rs.getObject("max_positions") == null ? null : rs.getInt("max_positions"),
                rs.getObject("candidate_count") == null ? null : rs.getInt("candidate_count"),
                rs.getObject("signal_count") == null ? null : rs.getInt("signal_count"),
                rs.getString("run_note"),
                rs.getString("error_message"),
                rs.getObject("started_at", java.time.OffsetDateTime.class),
                rs.getObject("finished_at", java.time.OffsetDateTime.class),
                rs.getObject("created_at", java.time.OffsetDateTime.class),
                rs.getObject("updated_at", java.time.OffsetDateTime.class)
        ));
    }

    public List<DailySignalDto> findDailySignals(Long dailyRunId) {
        String sql = """
                SELECT
                    s.id,
                    s.daily_run_id,
                    s.strategy_name,
                    s.strategy_version,
                    s.run_date,
                    s.data_date,
                    s.signal_date,
                    s.ticker_code,
                    s.company_name,
                    s.signal_type,
                    s.signal_status,
                    s.rank_no,
                    s.market_signal,
                    s.base_exposure,
                    s.final_score,
                    s.flow_score,
                    s.tape_score,
                    s.info_score,
                    s.short_score,
                    s.volatility_20d,
                    s.intraday_range,
                    s.position_size,
                    s.target_amount,
                    s.target_qty,
                    s.entry_reason,
                    s.block_reason,
                    s.buy_info::text AS buy_info,
                    s.raw_features::text AS raw_features,
                    s.source_table,
                    s.processor_version,

                    eo.execution_plan_id,
                    eo.id AS execution_order_id,
                    eo.execution_status,
                    eo.order_qty AS execution_order_qty,
                    eo.order_price AS execution_order_price,
                    eo.target_amount AS execution_target_amount,

                    s.created_at,
                    s.updated_at
                FROM strategy_daily_signal s
                LEFT JOIN strategy_execution_order eo
                    ON eo.source_daily_signal_id = s.id
                    AND eo.source_type = 'DAILY_SIGNAL_BUY'
                    AND eo.action_type = 'BUY'
                WHERE s.daily_run_id = ?
                ORDER BY s.rank_no ASC NULLS LAST, s.id ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new DailySignalDto(
                rs.getLong("id"),
                rs.getLong("daily_run_id"),
                rs.getString("strategy_name"),
                rs.getString("strategy_version"),
                rs.getObject("run_date", java.time.LocalDate.class),
                rs.getObject("data_date", java.time.LocalDate.class),
                rs.getObject("signal_date", java.time.LocalDate.class),
                rs.getString("ticker_code"),
                rs.getString("company_name"),
                rs.getString("signal_type"),
                rs.getString("signal_status"),
                rs.getObject("rank_no") == null ? null : rs.getInt("rank_no"),
                rs.getString("market_signal"),
                rs.getBigDecimal("base_exposure"),
                rs.getBigDecimal("final_score"),
                rs.getBigDecimal("flow_score"),
                rs.getBigDecimal("tape_score"),
                rs.getBigDecimal("info_score"),
                rs.getBigDecimal("short_score"),
                rs.getBigDecimal("volatility_20d"),
                rs.getBigDecimal("intraday_range"),
                rs.getBigDecimal("position_size"),
                rs.getBigDecimal("target_amount"),
                rs.getObject("target_qty") == null ? null : rs.getInt("target_qty"),
                rs.getString("entry_reason"),
                rs.getString("block_reason"),
                rs.getString("buy_info"),
                rs.getString("raw_features"),
                rs.getString("source_table"),
                rs.getString("processor_version"),

                rs.getObject("execution_plan_id") == null ? null : rs.getLong("execution_plan_id"),
                rs.getObject("execution_order_id") == null ? null : rs.getLong("execution_order_id"),
                rs.getString("execution_status"),
                rs.getObject("execution_order_qty") == null ? null : rs.getInt("execution_order_qty"),
                rs.getBigDecimal("execution_order_price"),
                rs.getBigDecimal("execution_target_amount"),

                rs.getObject("created_at", java.time.OffsetDateTime.class),
                rs.getObject("updated_at", java.time.OffsetDateTime.class)
        ), dailyRunId);
    }

    public List<DailyPositionDecisionDto> findDailyPositionDecisions(Long dailyRunId) {
        String sql = """
                SELECT
                    id,
                    daily_run_id,
                    strategy_name,
                    strategy_version,
                    run_date,
                    data_date,
                    decision_date,
                    position_state_id,
                    account_id,
                    account_no,
                    ticker_code,
                    stock_name,
                    decision_type,
                    decision_status,
                    sell_reason,
                    hold_reason,
                    skip_reason,
                    holding_days,
                    entry_date,
                    entry_price,
                    entry_qty,
                    remaining_qty,
                    current_price,
                    current_qty,
                    sellable_qty,
                    expected_pnl_amount,
                    expected_pnl_rate,
                    market_signal,
                    market_regime_score,
                    flow_score,
                    info_score,
                    tape_score,
                    final_score,
                    short_pressure_score,
                    order_qty,
                    order_price,
                    target_amount,
                    position_context::text AS position_context,
                    sell_info::text AS sell_info,
                    validation_result::text AS validation_result,
                    raw_stock_feature::text AS raw_stock_feature,
                    raw_market_feature::text AS raw_market_feature,
                    source_table,
                    processor_version,
                    execution_order_id,
                    error_message,
                    created_at,
                    updated_at
                FROM strategy_daily_position_decision
                WHERE daily_run_id = ?
                ORDER BY
                    CASE decision_type
                        WHEN 'SELL' THEN 1
                        WHEN 'HOLD' THEN 2
                        WHEN 'SKIP' THEN 3
                        ELSE 9
                    END,
                    ticker_code ASC,
                    id ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new DailyPositionDecisionDto(
                rs.getLong("id"),
                rs.getLong("daily_run_id"),
                rs.getString("strategy_name"),
                rs.getString("strategy_version"),
                rs.getObject("run_date", java.time.LocalDate.class),
                rs.getObject("data_date", java.time.LocalDate.class),
                rs.getObject("decision_date", java.time.LocalDate.class),
                rs.getLong("position_state_id"),
                rs.getObject("account_id") == null ? null : rs.getLong("account_id"),
                rs.getString("account_no"),
                rs.getString("ticker_code"),
                rs.getString("stock_name"),
                rs.getString("decision_type"),
                rs.getString("decision_status"),
                rs.getString("sell_reason"),
                rs.getString("hold_reason"),
                rs.getString("skip_reason"),
                rs.getObject("holding_days") == null ? null : rs.getInt("holding_days"),
                rs.getObject("entry_date", java.time.LocalDate.class),
                rs.getBigDecimal("entry_price"),
                rs.getObject("entry_qty") == null ? null : rs.getInt("entry_qty"),
                rs.getObject("remaining_qty") == null ? null : rs.getInt("remaining_qty"),
                rs.getBigDecimal("current_price"),
                rs.getObject("current_qty") == null ? null : rs.getInt("current_qty"),
                rs.getObject("sellable_qty") == null ? null : rs.getInt("sellable_qty"),
                rs.getBigDecimal("expected_pnl_amount"),
                rs.getBigDecimal("expected_pnl_rate"),
                rs.getString("market_signal"),
                rs.getBigDecimal("market_regime_score"),
                rs.getBigDecimal("flow_score"),
                rs.getBigDecimal("info_score"),
                rs.getBigDecimal("tape_score"),
                rs.getBigDecimal("final_score"),
                rs.getBigDecimal("short_pressure_score"),
                rs.getObject("order_qty") == null ? null : rs.getInt("order_qty"),
                rs.getBigDecimal("order_price"),
                rs.getBigDecimal("target_amount"),
                rs.getString("position_context"),
                rs.getString("sell_info"),
                rs.getString("validation_result"),
                rs.getString("raw_stock_feature"),
                rs.getString("raw_market_feature"),
                rs.getString("source_table"),
                rs.getString("processor_version"),
                rs.getObject("execution_order_id") == null ? null : rs.getLong("execution_order_id"),
                rs.getString("error_message"),
                rs.getObject("created_at", java.time.OffsetDateTime.class),
                rs.getObject("updated_at", java.time.OffsetDateTime.class)
        ), dailyRunId);
    }

    public Map<String, Object> findDailySummary(Long dailyRunId) {
        String sql = """
                SELECT
                    (
                        SELECT COUNT(*)
                        FROM strategy_daily_signal
                        WHERE daily_run_id = ?
                    ) AS signal_count,

                    (
                        SELECT COUNT(*)
                        FROM strategy_daily_signal
                        WHERE daily_run_id = ?
                          AND signal_status = 'READY'
                    ) AS ready_signal_count,

                    (
                        SELECT COUNT(*)
                        FROM strategy_daily_position_decision
                        WHERE daily_run_id = ?
                    ) AS position_decision_count,

                    (
                        SELECT COUNT(*)
                        FROM strategy_daily_position_decision
                        WHERE daily_run_id = ?
                          AND decision_type = 'SELL'
                    ) AS sell_decision_count,

                    (
                        SELECT COUNT(*)
                        FROM strategy_daily_position_decision
                        WHERE daily_run_id = ?
                          AND decision_type = 'HOLD'
                    ) AS hold_decision_count,

                    (
                        SELECT COUNT(*)
                        FROM strategy_daily_position_decision
                        WHERE daily_run_id = ?
                          AND decision_type = 'SKIP'
                    ) AS skip_decision_count,

                    (
                        SELECT COUNT(*)
                        FROM strategy_execution_order
                        WHERE source_daily_run_id = ?
                    ) AS execution_order_count,

                    (
                        SELECT COUNT(*)
                        FROM strategy_execution_order
                        WHERE source_daily_run_id = ?
                          AND execution_status = 'READY'
                    ) AS ready_execution_order_count
                """;

        return jdbcTemplate.queryForMap(
                sql,
                dailyRunId,
                dailyRunId,
                dailyRunId,
                dailyRunId,
                dailyRunId,
                dailyRunId,
                dailyRunId,
                dailyRunId
        );
    }
}