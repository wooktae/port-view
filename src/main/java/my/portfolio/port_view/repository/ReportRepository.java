package my.portfolio.port_view.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Uses JdbcTemplate to query the summary, statistics, and Trade Details required by the Backtest Report view.
 * Depends on the schema search_path because it reads multiple Strategy/Research tables.
 */
@Repository
public class ReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<BacktestRunRow> findLatestBacktestRun() {
        String sql = """
                SELECT
                    NULL::bigint AS id,
                    run_id::text AS run_id,
                    strategy_name,
                    strategy_version AS engine_version,
                    backtest_start_date AS start_date,
                    backtest_end_date AS end_date,
                    total_return AS cumulative_return,
                    mdd AS max_drawdown,
                    sharpe AS sharpe_ratio,
                    trade_count AS total_trades,
                    started_at AS created_at
                FROM strategy_backtest_run
                ORDER BY started_at DESC, run_id DESC
                LIMIT 1
                """;

        List<BacktestRunRow> rows = jdbcTemplate.query(sql, this::mapBacktestRunRow);
        return rows.stream().findFirst();
    }

    public List<String> findStrategyConfigVersions() {
        String sql = """
                SELECT DISTINCT strategy_config_version
                FROM strategy_backtest_run
                WHERE strategy_config_version IS NOT NULL
                  AND BTRIM(strategy_config_version) <> ''
                ORDER BY strategy_config_version DESC
                """;

        return jdbcTemplate.queryForList(sql, String.class);
    }

    public Optional<BacktestRunRow> findLatestBacktestRunByStrategyConfigVersion(String strategyConfigVersion) {
        String sql = """
                SELECT
                    NULL::bigint AS id,
                    run_id::text AS run_id,
                    strategy_name,
                    strategy_version AS engine_version,
                    backtest_start_date AS start_date,
                    backtest_end_date AS end_date,
                    total_return AS cumulative_return,
                    mdd AS max_drawdown,
                    sharpe AS sharpe_ratio,
                    trade_count AS total_trades,
                    started_at AS created_at
                FROM strategy_backtest_run
                WHERE strategy_config_version = ?
                ORDER BY started_at DESC, run_id DESC
                LIMIT 1
                """;

        List<BacktestRunRow> rows = jdbcTemplate.query(
                sql,
                this::mapBacktestRunRow,
                strategyConfigVersion
        );
        return rows.stream().findFirst();
    }

    public Optional<BacktestRunRow> findBacktestRunByRunId(String runId) {
        String sql = """
                SELECT
                    NULL::bigint AS id,
                    run_id::text AS run_id,
                    strategy_name,
                    strategy_version AS engine_version,
                    backtest_start_date AS start_date,
                    backtest_end_date AS end_date,
                    total_return AS cumulative_return,
                    mdd AS max_drawdown,
                    sharpe AS sharpe_ratio,
                    trade_count AS total_trades,
                    started_at AS created_at
                FROM strategy_backtest_run
                WHERE run_id = ?::uuid
                LIMIT 1
                """;

        List<BacktestRunRow> rows = jdbcTemplate.query(sql, this::mapBacktestRunRow, runId);
        return rows.stream().findFirst();
    }

    public List<ExitReasonStatRow> findExitReasonStats(String runId) {
        String sql = """
                SELECT
                    reason,
                    trades,
                    avg_return,
                    win_rate
                FROM strategy_backtest_analysis
                WHERE run_id = ?::uuid
                ORDER BY
                    CASE
                        WHEN avg_return < 0 THEN 0
                        ELSE 1
                    END ASC,
                    avg_return ASC,
                    trades DESC
                """;

        return jdbcTemplate.query(sql, this::mapExitReasonStatRow, runId);
    }

    public List<HoldingPeriodStatRow> findHoldingPeriodStats(String runId) {
        String sql = """
                SELECT
                    holding_period,
                    trades,
                    avg_return,
                    win_rate
                FROM strategy_backtest_holding_analysis
                WHERE run_id = ?::uuid
                ORDER BY holding_period ASC
                """;

        return jdbcTemplate.query(sql, this::mapHoldingPeriodStatRow, runId);
    }

    public List<BlockWeakStatRow> findBlockWeakStats(String runId) {
        String sql = """
                SELECT
                    holding_bucket,
                    candidate_type,
                    flow_bucket,
                    final_bucket,
                    profit_bucket,
                    trades,
                    avg_return,
                    win_rate
                FROM strategy_backtest_mbweak_bucket_analysis
                WHERE run_id = ?::uuid
                ORDER BY
                    trades DESC,
                    avg_return ASC
                LIMIT 20
                """;

        return jdbcTemplate.query(sql, this::mapBlockWeakStatRow, runId);
    }

    public List<DailyTradeStatusRow> findDailyTradeStatuses(String runId) {
        String sql = """
                WITH buy_agg AS (
                    SELECT
                        buy_date AS trade_date,
                        COUNT(*) AS buy_count,
                        STRING_AGG(
                            company_name || '(' || ticker_code || ')',
                            ', '
                            ORDER BY company_name, ticker_code
                        ) AS buy_names
                    FROM strategy_backtest_daily_position
                    WHERE run_id = ?::uuid
                    AND buy_date IS NOT NULL
                    AND date = buy_date
                    GROUP BY buy_date
                ),
                sell_agg AS (
                    SELECT
                        sell_date AS trade_date,
                        COUNT(*) AS sell_count,
                        AVG(
                            CASE
                                WHEN return_pct IS NULL THEN NULL
                                WHEN replace(return_pct, '%', '') ~ '^-?[0-9]+(\\.[0-9]+)?$'
                                    THEN replace(return_pct, '%', '')::numeric / 100
                                ELSE NULL
                            END
                        ) AS avg_sell_return,
                        SUM(
                            CASE
                                WHEN return_pct IS NULL THEN 0
                                WHEN replace(return_pct, '%', '') ~ '^-?[0-9]+(\\.[0-9]+)?$'
                                    AND replace(return_pct, '%', '')::numeric > 0
                                    THEN 1
                                ELSE 0
                            END
                        ) AS win_sell_count,
                        STRING_AGG(
                            company_name || '(' || ticker_code || ')',
                            ', '
                            ORDER BY company_name, ticker_code
                        ) AS sell_names
                    FROM strategy_trade_log
                    WHERE run_id = ?::uuid
                    AND sell_date IS NOT NULL
                    GROUP BY sell_date
                )
                SELECT
                    d.date,
                    d.market_signal,
                    d.position_count,
                    d.daily_return,
                    d.cum_return,
                    COALESCE(b.buy_count, 0) AS buy_count,
                    COALESCE(s.sell_count, 0) AS sell_count,
                    s.avg_sell_return,
                    COALESCE(s.win_sell_count, 0) AS win_sell_count,
                    b.buy_names,
                    s.sell_names
                FROM strategy_backtest_daily d
                LEFT JOIN buy_agg b
                ON b.trade_date = d.date
                LEFT JOIN sell_agg s
                ON s.trade_date = d.date
                WHERE d.run_id = ?::uuid
                ORDER BY d.date DESC
                LIMIT 120
                """;

        return jdbcTemplate.query(sql, this::mapDailyTradeStatusRow, runId, runId, runId);
    }

    public List<TradeDetailRow> findTradeDetails(String runId) {
        String sql = """
                SELECT
                    id,
                    ticker_code,
                    company_name,
                    buy_date,
                    sell_date,
                    holding_period,
                    return_pct,

                    buy_open_price,
                    buy_close_price,
                    buy_low_price,
                    sell_open_price,
                    sell_close_price,
                    sell_low_price,

                    buy_info ->> 'entry_market_signal' AS entry_market_signal,
                    NULLIF(buy_info ->> 'position_size', '')::numeric AS position_size,
                    NULLIF(buy_info ->> 'flow', '')::numeric AS buy_flow_score,
                    NULLIF(buy_info ->> 'score', '')::numeric AS buy_score,
                    NULLIF(buy_info ->> 'info', '')::numeric AS buy_info_score,
                    NULLIF(buy_info ->> 'short', '')::numeric AS buy_short_pressure,
                    NULLIF(buy_info ->> 'vol', '')::numeric AS buy_volatility,
                    NULLIF(buy_info ->> 'intraday_range', '')::numeric AS buy_intraday_range,
                    CASE
                        WHEN lower(buy_info ->> 'has_info_flag') = 'true' THEN true
                        ELSE false
                    END AS has_info_flag,

                    sell_info ->> 'reason' AS sell_reason,
                    NULLIF(sell_info ->> 'flow', '')::numeric AS sell_flow_score,
                    NULLIF(sell_info ->> 'final', '')::numeric AS sell_final_score,
                    NULLIF(sell_info ->> 'cum_return', '')::numeric AS sell_cum_return
                FROM strategy_trade_log
                WHERE run_id = ?::uuid
                ORDER BY
                    buy_date DESC NULLS LAST,
                    sell_date DESC NULLS LAST,
                    id DESC
                LIMIT 80
                """;

        return jdbcTemplate.query(sql, this::mapTradeDetailRow, runId);
    }

    private DailyTradeStatusRow mapDailyTradeStatusRow(ResultSet rs, int rowNum) throws SQLException {
        return new DailyTradeStatusRow(
                toLocalDate(rs.getDate("date")),
                rs.getString("market_signal"),
                getInteger(rs, "position_count"),
                rs.getBigDecimal("daily_return"),
                rs.getBigDecimal("cum_return"),
                getInteger(rs, "buy_count"),
                getInteger(rs, "sell_count"),
                rs.getBigDecimal("avg_sell_return"),
                getInteger(rs, "win_sell_count"),
                rs.getString("buy_names"),
                rs.getString("sell_names")
        );
    }

    private TradeDetailRow mapTradeDetailRow(ResultSet rs, int rowNum) throws SQLException {
        return new TradeDetailRow(
                rs.getLong("id"),
                rs.getString("ticker_code"),
                rs.getString("company_name"),
                toLocalDate(rs.getDate("buy_date")),
                toLocalDate(rs.getDate("sell_date")),
                getInteger(rs, "holding_period"),
                rs.getString("return_pct"),

                rs.getBigDecimal("buy_open_price"),
                rs.getBigDecimal("buy_close_price"),
                rs.getBigDecimal("buy_low_price"),
                rs.getBigDecimal("sell_open_price"),
                rs.getBigDecimal("sell_close_price"),
                rs.getBigDecimal("sell_low_price"),

                rs.getString("entry_market_signal"),
                rs.getBigDecimal("position_size"),
                rs.getBigDecimal("buy_flow_score"),
                rs.getBigDecimal("buy_score"),
                rs.getBigDecimal("buy_info_score"),
                rs.getBigDecimal("buy_short_pressure"),
                rs.getBigDecimal("buy_volatility"),
                rs.getBigDecimal("buy_intraday_range"),
                getBoolean(rs, "has_info_flag"),

                rs.getString("sell_reason"),
                rs.getBigDecimal("sell_flow_score"),
                rs.getBigDecimal("sell_final_score"),
                rs.getBigDecimal("sell_cum_return")
        );
    }

    private BacktestRunRow mapBacktestRunRow(ResultSet rs, int rowNum) throws SQLException {
        return new BacktestRunRow(
                rs.getLong("id"),
                rs.getString("run_id"),
                rs.getString("strategy_name"),
                rs.getString("engine_version"),
                toLocalDate(rs.getDate("start_date")),
                toLocalDate(rs.getDate("end_date")),
                rs.getBigDecimal("cumulative_return"),
                rs.getBigDecimal("max_drawdown"),
                rs.getBigDecimal("sharpe_ratio"),
                rs.getInt("total_trades"),
                getOffsetDateTime(rs, "created_at")
        );
    }

    private ExitReasonStatRow mapExitReasonStatRow(ResultSet rs, int rowNum) throws SQLException {
        return new ExitReasonStatRow(
                rs.getString("reason"),
                rs.getInt("trades"),
                rs.getBigDecimal("avg_return"),
                rs.getBigDecimal("win_rate")
        );
    }

    private HoldingPeriodStatRow mapHoldingPeriodStatRow(ResultSet rs, int rowNum) throws SQLException {
        return new HoldingPeriodStatRow(
                rs.getInt("holding_period"),
                rs.getInt("trades"),
                rs.getBigDecimal("avg_return"),
                rs.getBigDecimal("win_rate")
        );
    }

    private BlockWeakStatRow mapBlockWeakStatRow(ResultSet rs, int rowNum) throws SQLException {
        return new BlockWeakStatRow(
                rs.getString("holding_bucket"),
                rs.getString("candidate_type"),
                rs.getString("flow_bucket"),
                rs.getString("final_bucket"),
                rs.getString("profit_bucket"),
                rs.getInt("trades"),
                rs.getBigDecimal("avg_return"),
                rs.getBigDecimal("win_rate")
        );
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private OffsetDateTime getOffsetDateTime(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);

        if (value == null) {
            return null;
        }

        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime;
        }

        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().atOffset(java.time.ZoneOffset.ofHours(9));
        }

        return OffsetDateTime.parse(value.toString());
    }

    public record BacktestRunRow(
            Long id,
            String runId,
            String strategyName,
            String engineVersion,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal cumulativeReturn,
            BigDecimal maxDrawdown,
            BigDecimal sharpeRatio,
            Integer totalTrades,
            OffsetDateTime createdAt
    ) {
    }

    public record ExitReasonStatRow(
            String reason,
            Integer trades,
            BigDecimal avgReturn,
            BigDecimal winRate
    ) {
    }

    public record HoldingPeriodStatRow(
            Integer holdingPeriod,
            Integer trades,
            BigDecimal avgReturn,
            BigDecimal winRate
    ) {
    }

    public record BlockWeakStatRow(
            String holdingBucket,
            String candidateType,
            String flowBucket,
            String finalBucket,
            String profitBucket,
            Integer trades,
            BigDecimal avgReturn,
            BigDecimal winRate
    ) {
    }
    public record TradeDetailRow(
        Long id,
        String tickerCode,
        String companyName,
        LocalDate buyDate,
        LocalDate sellDate,
        Integer holdingPeriod,
        String returnPct,

        BigDecimal buyOpenPrice,
        BigDecimal buyClosePrice,
        BigDecimal buyLowPrice,
        BigDecimal sellOpenPrice,
        BigDecimal sellClosePrice,
        BigDecimal sellLowPrice,

        String entryMarketSignal,
        BigDecimal positionSize,
        BigDecimal buyFlowScore,
        BigDecimal buyScore,
        BigDecimal buyInfoScore,
        BigDecimal buyShortPressure,
        BigDecimal buyVolatility,
        BigDecimal buyIntradayRange,
        Boolean hasInfoFlag,

        String sellReason,
        BigDecimal sellFlowScore,
        BigDecimal sellFinalScore,
        BigDecimal sellCumReturn
    ) {
    }

    private Integer getInteger(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private Boolean getBoolean(ResultSet rs, String columnName) throws SQLException {
        boolean value = rs.getBoolean(columnName);
        return rs.wasNull() ? null : value;
    }

    public record DailyTradeStatusRow(
        LocalDate date,
        String marketSignal,
        Integer positionCount,
        BigDecimal dailyReturn,
        BigDecimal cumulativeReturn,
        Integer buyCount,
        Integer sellCount,
        BigDecimal avgSellReturn,
        Integer winSellCount,
        String buyNames,
        String sellNames
    ) {
    }

}
