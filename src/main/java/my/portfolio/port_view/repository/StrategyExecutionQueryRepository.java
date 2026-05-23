package my.portfolio.port_view.repository;

import my.portfolio.port_view.dto.strategy.StrategyExecutionOrderDto;
import my.portfolio.port_view.dto.strategy.StrategyExecutionPlanDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import my.portfolio.port_view.dto.strategy.StrategyMarketBlockReasonDto;

@Repository
@RequiredArgsConstructor
public class StrategyExecutionQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<StrategyExecutionPlanDto> findPlans() {
        String sql = """
                SELECT
                    id,
                    plan_date,
                    strategy_name,
                    strategy_version,
                    strategy_run_id,
                    market_signal,
                    market_regime_score,
                    risk_regime,
                    account_no,
                    plan_status,
                    total_candidate_count,
                    ready_order_count,
                    blocked_order_count,
                    skipped_order_count,
                    total_target_amount,
                    available_cash,
                    max_order_amount,
                    blocked_reason,
                    memo,
                    created_at,
                    updated_at
                FROM strategy_execution_plan
                ORDER BY plan_date DESC, id DESC
                LIMIT 50
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new StrategyExecutionPlanDto(
                rs.getLong("id"),
                rs.getObject("plan_date", java.time.LocalDate.class),
                rs.getString("strategy_name"),
                rs.getString("strategy_version"),
                rs.getObject("strategy_run_id", java.util.UUID.class),
                rs.getString("market_signal"),
                rs.getBigDecimal("market_regime_score"),
                rs.getString("risk_regime"),
                rs.getString("account_no"),
                rs.getString("plan_status"),
                rs.getInt("total_candidate_count"),
                rs.getInt("ready_order_count"),
                rs.getInt("blocked_order_count"),
                rs.getInt("skipped_order_count"),
                rs.getBigDecimal("total_target_amount"),
                rs.getBigDecimal("available_cash"),
                rs.getBigDecimal("max_order_amount"),
                rs.getString("blocked_reason"),
                rs.getString("memo"),
                rs.getObject("created_at", java.time.OffsetDateTime.class),
                rs.getObject("updated_at", java.time.OffsetDateTime.class)
        ));
    }

    public Optional<StrategyExecutionPlanDto> findPlanById(Long planId) {
        String sql = """
                SELECT
                    id,
                    plan_date,
                    strategy_name,
                    strategy_version,
                    strategy_run_id,
                    market_signal,
                    market_regime_score,
                    risk_regime,
                    account_no,
                    plan_status,
                    total_candidate_count,
                    ready_order_count,
                    blocked_order_count,
                    skipped_order_count,
                    total_target_amount,
                    available_cash,
                    max_order_amount,
                    blocked_reason,
                    memo,
                    created_at,
                    updated_at
                FROM strategy_execution_plan
                WHERE id = ?
                """;

        List<StrategyExecutionPlanDto> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new StrategyExecutionPlanDto(
                rs.getLong("id"),
                rs.getObject("plan_date", java.time.LocalDate.class),
                rs.getString("strategy_name"),
                rs.getString("strategy_version"),
                rs.getObject("strategy_run_id", java.util.UUID.class),
                rs.getString("market_signal"),
                rs.getBigDecimal("market_regime_score"),
                rs.getString("risk_regime"),
                rs.getString("account_no"),
                rs.getString("plan_status"),
                rs.getInt("total_candidate_count"),
                rs.getInt("ready_order_count"),
                rs.getInt("blocked_order_count"),
                rs.getInt("skipped_order_count"),
                rs.getBigDecimal("total_target_amount"),
                rs.getBigDecimal("available_cash"),
                rs.getBigDecimal("max_order_amount"),
                rs.getString("blocked_reason"),
                rs.getString("memo"),
                rs.getObject("created_at", java.time.OffsetDateTime.class),
                rs.getObject("updated_at", java.time.OffsetDateTime.class)
        ), planId);

        return rows.stream().findFirst();
    }

    public Optional<StrategyMarketBlockReasonDto> findMarketBlockReasonByPlanId(Long planId) {
        String sql = """
                SELECT
                    p.market_signal AS plan_market_signal,
                    p.risk_regime AS plan_risk_regime,

                    m.date AS feature_date,
                    m.risk_regime AS feature_risk_regime,
                    COALESCE(p.market_regime_score, m.market_regime_score) AS market_regime_score,
                    m.flow_pressure_score,
                    m.breadth_pressure_score,
                    m.market_flow_strength_score,
                    m.market_foreign_net_ratio_5d,
                    m.market_institution_net_ratio_5d,
                    m.global_risk_score,
                    m.macro_pressure_score

                FROM strategy_execution_plan p

                LEFT JOIN LATERAL (
                    SELECT
                        date,
                        risk_regime,
                        market_regime_score,
                        flow_pressure_score,
                        breadth_pressure_score,
                        market_flow_strength_score,
                        market_foreign_net_ratio_5d,
                        market_institution_net_ratio_5d,
                        global_risk_score,
                        macro_pressure_score
                    FROM pre_total_market_daily_feature
                    WHERE date <= p.plan_date
                    ORDER BY date DESC
                    LIMIT 1
                ) m ON TRUE

                WHERE p.id = ?
                """;

        List<StrategyMarketBlockReasonDto> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new StrategyMarketBlockReasonDto(
                rs.getObject("feature_date", java.time.LocalDate.class),
                rs.getString("plan_market_signal"),
                rs.getString("plan_risk_regime"),
                rs.getString("feature_risk_regime"),

                rs.getBigDecimal("market_regime_score"),
                rs.getBigDecimal("flow_pressure_score"),
                rs.getBigDecimal("breadth_pressure_score"),
                rs.getBigDecimal("market_flow_strength_score"),
                rs.getBigDecimal("market_foreign_net_ratio_5d"),
                rs.getBigDecimal("market_institution_net_ratio_5d"),
                rs.getBigDecimal("global_risk_score"),
                rs.getBigDecimal("macro_pressure_score"),

                null,
                null,
                List.of()
        ), planId);

        return rows.stream().findFirst();
    }

    public List<StrategyExecutionOrderDto> findOrdersByPlanId(Long planId) {
        String sql = """
                SELECT
                    id,
                    execution_plan_id,
                    strategy_signal_id,

                    source_daily_run_id,
                    source_daily_signal_id,
                    source_daily_position_decision_id,
                    source_type,

                    signal_date,
                    ticker_code,
                    stock_name,
                    action_type,
                    signal_type,
                    signal_score,
                    signal_position_size,
                    market_signal,
                    market_regime_score,
                    flow_score,
                    info_score,
                    tape_score,
                    final_score,
                    short_pressure_score,
                    target_amount,
                    current_qty,
                    current_eval_amount,
                    order_qty,
                    order_price,
                    order_method,
                    execution_status,
                    block_reason,
                    connector_order_request_id,
                    validation_result::text AS validation_result,
                    created_at,
                    updated_at
                FROM strategy_execution_order
                WHERE execution_plan_id = ?
                ORDER BY
                    CASE execution_status
                        WHEN 'READY' THEN 1
                        WHEN 'CANDIDATE' THEN 2
                        WHEN 'BLOCKED' THEN 3
                        WHEN 'SKIPPED' THEN 4
                        ELSE 9
                    END,
                    signal_score DESC NULLS LAST,
                    id ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new StrategyExecutionOrderDto(
                rs.getLong("id"),
                rs.getLong("execution_plan_id"),
                rs.getObject("strategy_signal_id") == null ? null : rs.getLong("strategy_signal_id"),

                rs.getObject("source_daily_run_id") == null ? null : rs.getLong("source_daily_run_id"),
                rs.getObject("source_daily_signal_id") == null ? null : rs.getLong("source_daily_signal_id"),
                rs.getObject("source_daily_position_decision_id") == null ? null : rs.getLong("source_daily_position_decision_id"),
                rs.getString("source_type"),

                rs.getObject("signal_date", java.time.LocalDate.class),
                rs.getString("ticker_code"),
                rs.getString("stock_name"),
                rs.getString("action_type"),
                rs.getString("signal_type"),
                rs.getBigDecimal("signal_score"),
                rs.getBigDecimal("signal_position_size"),
                rs.getString("market_signal"),
                rs.getBigDecimal("market_regime_score"),
                rs.getBigDecimal("flow_score"),
                rs.getBigDecimal("info_score"),
                rs.getBigDecimal("tape_score"),
                rs.getBigDecimal("final_score"),
                rs.getBigDecimal("short_pressure_score"),
                rs.getBigDecimal("target_amount"),
                rs.getInt("current_qty"),
                rs.getBigDecimal("current_eval_amount"),
                rs.getInt("order_qty"),
                rs.getBigDecimal("order_price"),
                rs.getString("order_method"),
                rs.getString("execution_status"),
                rs.getString("block_reason"),
                rs.getObject("connector_order_request_id") == null ? null : rs.getLong("connector_order_request_id"),
                rs.getString("validation_result"),
                rs.getObject("created_at", java.time.OffsetDateTime.class),
                rs.getObject("updated_at", java.time.OffsetDateTime.class)
        ), planId);
    }

    public List<Map<String, Object>> findPositionStatesByPlanId(Long planId) {
        String sql = """
                SELECT
                    ps.id,
                    ps.account_id,
                    ps.account_no,
                    ps.ticker_code,
                    ps.stock_name,
                    ps.source_buy_execution_order_id,
                    ps.source_connector_order_request_id,
                    ps.source_connector_fill_id,
                    ps.entry_date,
                    ps.entry_price,
                    ps.entry_qty,
                    ps.remaining_qty,
                    ps.position_status,
                    ps.last_evaluated_date,
                    ps.latest_sell_reason,
                    ps.latest_sell_info::text AS latest_sell_info,
                    ps.latest_validation_result::text AS latest_validation_result,

                    NULLIF(ps.latest_sell_info ->> 'action', '') AS sell_action,
                    NULLIF(ps.latest_sell_info ->> 'detail', '') AS sell_detail,
                    NULLIF(ps.latest_sell_info ->> 'holding_days', '')::integer AS holding_days,
                    NULLIF(ps.latest_sell_info ->> 'sellable_qty', '')::integer AS sellable_qty,
                    NULLIF(ps.latest_sell_info ->> 'snapshot_qty', '')::integer AS snapshot_qty,
                    NULLIF(ps.latest_sell_info ->> 'current_price', '')::numeric AS current_price,
                    NULLIF(ps.latest_sell_info ->> 'expected_pnl_amount', '')::numeric AS expected_pnl_amount,
                    NULLIF(ps.latest_sell_info ->> 'expected_pnl_rate', '')::numeric AS expected_pnl_rate,
                    NULLIF(ps.latest_sell_info ->> 'market_signal', '') AS eval_market_signal,
                    NULLIF(ps.latest_sell_info ->> 'flow_score', '')::numeric AS eval_flow_score,
                    NULLIF(ps.latest_sell_info ->> 'final_score', '')::numeric AS eval_final_score,

                    ps.buy_info::text AS buy_info,
                    ps.memo,
                    ps.created_at,
                    ps.updated_at,

                    buy_seo.id AS buy_execution_order_id,
                    buy_seo.execution_plan_id AS buy_execution_plan_id,
                    buy_seo.source_type AS buy_source_type,
                    buy_seo.action_type AS buy_action_type,
                    buy_seo.execution_mode AS buy_execution_mode,
                    buy_seo.execution_status AS buy_execution_status,
                    buy_seo.connector_order_request_id AS buy_connector_order_request_id,

                    buy_cor.id AS buy_connector_request_id,
                    buy_cor.request_type AS buy_connector_request_type,
                    buy_cor.request_status AS buy_connector_request_status,
                    buy_cor.broker_order_no AS buy_broker_order_no,
                    buy_cor.broker_branch_code AS buy_broker_branch_code,

                    buy_cf.id AS buy_fill_id,
                    buy_cf.fill_qty AS buy_fill_qty,
                    buy_cf.fill_price AS buy_fill_price,
                    buy_cf.fill_amount AS buy_fill_amount,
                    buy_cf.fill_ts AS buy_fill_ts,

                    sell_seo.id AS sell_execution_order_id,
                    sell_seo.execution_plan_id,
                    sell_seo.source_type,
                    sell_seo.action_type,
                    sell_seo.execution_mode,
                    sell_seo.execution_status,
                    sell_seo.connector_order_request_id,
                    sell_seo.sell_reason,
                    sell_seo.expected_sell_price AS sell_order_expected_sell_price,
                    sell_seo.expected_pnl_amount AS sell_order_expected_pnl_amount,
                    sell_seo.expected_pnl_rate AS sell_order_expected_pnl_rate

                FROM strategy_position_state ps

                LEFT JOIN strategy_execution_order buy_seo
                ON buy_seo.id = ps.source_buy_execution_order_id
                AND buy_seo.action_type = 'BUY'

                LEFT JOIN connector_order_request buy_cor
                ON buy_cor.id = ps.source_connector_order_request_id

                LEFT JOIN connector_fill buy_cf
                ON buy_cf.id = ps.source_connector_fill_id

                LEFT JOIN strategy_execution_order sell_seo
                ON sell_seo.source_position_state_id = ps.id
                AND sell_seo.source_type = 'POSITION_SELL'
                AND sell_seo.action_type = 'SELL'
                AND sell_seo.execution_plan_id = ?

                WHERE
                    sell_seo.execution_plan_id = ?
                    OR buy_seo.execution_plan_id = ?
                    OR ps.position_status IN ('OPEN', 'SELL_READY', 'SELL_ORDERED')

                ORDER BY
                    CASE ps.position_status
                        WHEN 'SELL_READY' THEN 1
                        WHEN 'SELL_ORDERED' THEN 2
                        WHEN 'OPEN' THEN 3
                        WHEN 'CLOSED' THEN 4
                        ELSE 9
                    END,
                    ps.updated_at DESC,
                    ps.id DESC
                """;

        return jdbcTemplate.queryForList(sql, planId, planId, planId);
    }

    public List<Map<String, Object>> findConnectorOrdersByPlanId(Long planId) {
        String sql = """
                SELECT
                    cor.id,
                    cor.account_id,
                    cor.account_no,
                    cor.ticker_code,
                    cor.stock_name,
                    cor.request_type,
                    cor.order_method,
                    cor.order_price,
                    cor.order_qty,
                    cor.request_status,
                    cor.broker_order_no,
                    cor.broker_branch_code,
                    cor.signal_type,
                    cor.signal_score,
                    cor.signal_position_size,
                    cor.requested_at,
                    cor.accepted_at,
                    cor.last_event_at,
                    cor.rejection_code,
                    cor.rejection_message,
                    cor.response_payload::text AS response_payload,

                    seo.id AS execution_order_id,
                    seo.execution_plan_id,
                    seo.source_position_state_id,
                    seo.source_type,
                    seo.action_type,
                    seo.execution_status
                FROM strategy_execution_order seo
                JOIN connector_order_request cor
                  ON cor.id = seo.connector_order_request_id
                WHERE seo.execution_plan_id = ?
                ORDER BY cor.requested_at DESC, cor.id DESC
                """;

        return jdbcTemplate.queryForList(sql, planId);
    }

    public List<Map<String, Object>> findConnectorEventsAndFillsByPlanId(Long planId) {
        String sql = """
                SELECT
                    seo.id AS execution_order_id,
                    seo.execution_plan_id,
                    seo.source_position_state_id,
                    seo.source_type,
                    seo.action_type,
                    seo.execution_status,

                    cor.id AS connector_order_request_id,
                    cor.request_type,
                    cor.request_status,
                    cor.ticker_code,
                    cor.stock_name,
                    cor.broker_order_no,
                    cor.broker_branch_code,

                    coe.id AS event_id,
                    coe.event_type,
                    coe.side AS event_side,
                    coe.order_qty AS event_order_qty,
                    coe.executed_qty,
                    coe.remaining_qty,
                    coe.avg_exec_price,
                    coe.total_exec_amount,
                    coe.event_ts,
                    coe.event_key,

                    cf.id AS fill_id,
                    cf.side AS fill_side,
                    cf.fill_qty,
                    cf.fill_price,
                    cf.fill_amount,
                    cf.fill_ts
                FROM strategy_execution_order seo
                LEFT JOIN connector_order_request cor
                  ON cor.id = seo.connector_order_request_id
                LEFT JOIN connector_order_event coe
                  ON coe.order_request_id = cor.id
                LEFT JOIN connector_fill cf
                  ON cf.order_event_id = coe.id
                WHERE seo.execution_plan_id = ?
                  AND seo.connector_order_request_id IS NOT NULL
                ORDER BY
                    seo.id DESC,
                    coe.event_ts DESC NULLS LAST,
                    coe.id DESC NULLS LAST,
                    cf.fill_ts DESC NULLS LAST,
                    cf.id DESC NULLS LAST
                """;

        return jdbcTemplate.queryForList(sql, planId);
    }

    public Map<String, Object> findCurrentPositionSummary() {
        String sql = """
                SELECT
                    COUNT(*) AS total_position_count,

                    COUNT(*) FILTER (
                        WHERE position_status = 'OPEN'
                    ) AS open_position_count,

                    COUNT(*) FILTER (
                        WHERE position_status = 'SELL_READY'
                    ) AS sell_ready_position_count,

                    COUNT(*) FILTER (
                        WHERE position_status = 'SELL_ORDERED'
                    ) AS sell_ordered_position_count,

                    COUNT(*) FILTER (
                        WHERE position_status = 'CLOSED'
                    ) AS closed_position_count,

                    COUNT(*) FILTER (
                        WHERE latest_sell_info ->> 'action' = 'HOLD'
                    ) AS hold_position_count,

                    COUNT(*) FILTER (
                        WHERE latest_sell_info ->> 'action' = 'SELL'
                    ) AS sell_signal_position_count,

                    COUNT(*) FILTER (
                        WHERE latest_sell_info ->> 'action' = 'SKIP'
                    ) AS skip_position_count,

                    MAX(last_evaluated_date) AS latest_evaluated_date,

                    MAX(updated_at) AS latest_position_updated_at
                FROM strategy_position_state
                """;

        return jdbcTemplate.queryForMap(sql);
    }

    public List<Map<String, Object>> findOrderCountsForRecentPlans() {
        String sql = """
                SELECT
                    seo.execution_plan_id,

                    COUNT(*) FILTER (
                        WHERE seo.action_type = 'BUY'
                    )::int AS buy_candidate_count,

                    COUNT(*) FILTER (
                        WHERE seo.action_type = 'SELL'
                    )::int AS sell_candidate_count,

                    COUNT(*) FILTER (
                        WHERE seo.execution_status = 'READY'
                    )::int AS ready_order_count

                FROM strategy_execution_order seo
                WHERE seo.execution_plan_id IN (
                    SELECT p.id
                    FROM strategy_execution_plan p
                    ORDER BY p.plan_date DESC, p.id DESC
                    LIMIT 50
                )
                GROUP BY seo.execution_plan_id
                """;

        return jdbcTemplate.queryForList(sql);
    }

}