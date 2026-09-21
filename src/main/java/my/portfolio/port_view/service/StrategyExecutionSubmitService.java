package my.portfolio.port_view.service;

import lombok.RequiredArgsConstructor;
import my.portfolio.port_view.config.ConnectorProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

@Service
@RequiredArgsConstructor
public class StrategyExecutionSubmitService {

    private static final String BUY_ORDER_PATH = "/api/v1/orders/buy";
    private static final String SELL_ORDER_PATH = "/api/v1/orders/sell";

    private static final String STATUS_READY = "READY";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_FAILED = "FAILED";

    private static final String ACTION_BUY = "BUY";
    private static final String ACTION_SELL = "SELL";

    private static final String APPROVED_BY = "PORT_VIEW";

    private final JdbcTemplate jdbcTemplate;
    private final ConnectorProperties connectorProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    public Long submit(Long executionOrderId) {
        SubmitOrderRow order = findOrderForSubmit(executionOrderId);

        validate(order);

        Map<String, Object> requestPayload = buildConnectorRequestPayload(order);
        String requestPayloadJson = toJson(requestPayload);

        try {
            Map<String, Object> responsePayload = callConnector(order, requestPayload);
            String responsePayloadJson = toJson(responsePayload);

            Long connectorOrderRequestId = extractConnectorOrderRequestId(responsePayload);

            if (connectorOrderRequestId == null) {
                throw new IllegalStateException(
                        "Connector 응답에서 order_request_id를 찾을 수 없음. response=" + responsePayloadJson
                );
            }

            markSubmitted(
                    order.id(),
                    connectorOrderRequestId,
                    requestPayloadJson,
                    responsePayloadJson
            );

            return connectorOrderRequestId;

        } catch (Exception e) {
            markFailed(order.id(), requestPayloadJson, e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private SubmitOrderRow findOrderForSubmit(Long executionOrderId) {
        String sql = """
                SELECT
                    seo.id,
                    seo.execution_plan_id,
                    seo.strategy_signal_id,
                    seo.account_id,
                    seo.account_no,
                    seo.signal_date,
                    seo.ticker_code,
                    seo.stock_name,
                    seo.action_type,
                    seo.signal_type,
                    seo.signal_score,
                    seo.signal_position_size,
                    seo.target_amount,
                    seo.current_qty,
                    seo.current_eval_amount,
                    seo.order_qty,
                    seo.order_price,
                    seo.order_method,
                    seo.execution_status,
                    seo.approval_required,
                    seo.connector_order_request_id,
                    seo.source_trade_log_id,
                    seo.source_run_id,
                    seo.source_type,
                    seo.execution_mode,
                    seo.source_position_state_id,
                    seo.sell_reason,
                    seo.expected_sell_price,
                    seo.expected_pnl_amount,
                    seo.expected_pnl_rate,

                    sep.strategy_name,
                    sep.strategy_version,
                    sep.strategy_run_id,

                    ps.source_connector_order_request_id AS parent_order_request_id
                FROM strategy_execution_order seo
                JOIN strategy_execution_plan sep
                  ON sep.id = seo.execution_plan_id
                LEFT JOIN strategy_position_state ps
                  ON ps.id = seo.source_position_state_id
                WHERE seo.id = ?
                """;

        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException(
                        "전략 실행 주문을 찾을 수 없음. executionOrderId=" + executionOrderId
                );
            }

            return new SubmitOrderRow(
                    rs.getLong("id"),
                    rs.getLong("execution_plan_id"),
                    getLongOrNull(rs.getObject("strategy_signal_id")),
                    getLongOrNull(rs.getObject("account_id")),
                    rs.getString("account_no"),
                    rs.getObject("signal_date", LocalDate.class),
                    rs.getString("ticker_code"),
                    rs.getString("stock_name"),
                    rs.getString("action_type"),
                    rs.getString("signal_type"),
                    rs.getBigDecimal("signal_score"),
                    rs.getBigDecimal("signal_position_size"),
                    rs.getBigDecimal("target_amount"),
                    getIntegerOrNull(rs.getObject("current_qty")),
                    rs.getBigDecimal("current_eval_amount"),
                    getIntegerOrNull(rs.getObject("order_qty")),
                    rs.getBigDecimal("order_price"),
                    rs.getString("order_method"),
                    rs.getString("execution_status"),
                    rs.getBoolean("approval_required"),
                    getLongOrNull(rs.getObject("connector_order_request_id")),
                    getLongOrNull(rs.getObject("source_trade_log_id")),
                    rs.getObject("source_run_id") == null ? null : rs.getString("source_run_id"),
                    rs.getString("source_type"),
                    rs.getString("execution_mode"),
                    getLongOrNull(rs.getObject("source_position_state_id")),
                    rs.getString("sell_reason"),
                    rs.getBigDecimal("expected_sell_price"),
                    rs.getBigDecimal("expected_pnl_amount"),
                    rs.getBigDecimal("expected_pnl_rate"),
                    rs.getString("strategy_name"),
                    rs.getString("strategy_version"),
                    rs.getObject("strategy_run_id") == null ? null : rs.getString("strategy_run_id"),
                    getLongOrNull(rs.getObject("parent_order_request_id"))
            );
        }, executionOrderId);
    }

    private void validate(SubmitOrderRow order) {
        if (!STATUS_READY.equalsIgnoreCase(nvl(order.executionStatus()))) {
            throw new IllegalStateException(
                    "READY 상태 주문만 전송 가능. 현재 상태=" + order.executionStatus()
            );
        }

        if (order.connectorOrderRequestId() != null) {
            throw new IllegalStateException(
                    "이미 Connector 주문이 연결되어 있음. connectorOrderRequestId=" + order.connectorOrderRequestId()
            );
        }

        if (!order.approvalRequired()) {
            throw new IllegalStateException("approval_required=false 주문은 View 수동승인 대상이 아님.");
        }

        if (!ACTION_BUY.equalsIgnoreCase(nvl(order.actionType()))
                && !ACTION_SELL.equalsIgnoreCase(nvl(order.actionType()))) {
            throw new IllegalStateException(
                    "BUY/SELL 주문만 전송 가능. actionType=" + order.actionType()
            );
        }

        if (isBlank(order.accountNo())) {
            throw new IllegalStateException("account_no가 비어 있음.");
        }

        if (isBlank(order.tickerCode())) {
            throw new IllegalStateException("ticker_code가 비어 있음.");
        }

        if (order.orderQty() == null || order.orderQty() <= 0) {
            throw new IllegalStateException(
                    "order_qty가 0 이하라 전송 불가. orderQty=" + order.orderQty()
            );
        }

        if (isBlank(order.orderMethod())) {
            throw new IllegalStateException("order_method가 비어 있음.");
        }

        if (ACTION_SELL.equalsIgnoreCase(order.actionType())
                && order.sourcePositionStateId() != null
                && order.parentOrderRequestId() == null) {
            System.out.println(
                    "[WARN] SELL 주문인데 parent_order_request_id가 없음. executionOrderId=" + order.id()
            );
        }
    }

    private Map<String, Object> buildConnectorRequestPayload(SubmitOrderRow order) {
        Map<String, Object> payload = new LinkedHashMap<>();

        /*
         * Fields actually used by connector_app.py:
         * - stock_code
         * - qty
         * - order_method
         * - order_price
         * - stock_name
         * - parent_order_request_id : available only for SELL
         * - strategy_name
         * - strategy_version
         * - strategy_run_id
         * - strategy_signal_id
         * - signal_date
         * - signal_type
         * - signal_score
         * - signal_position_size
         */
        payload.put("stock_code", order.tickerCode());
        payload.put("qty", order.orderQty());
        payload.put("order_method", normalizeOrderMethod(order.orderMethod()));
        payload.put("order_price", normalizeOrderPrice(order));
        payload.put("stock_name", order.stockName());

        if (ACTION_SELL.equalsIgnoreCase(order.actionType())) {
            payload.put("parent_order_request_id", order.parentOrderRequestId());
        }

        payload.put("strategy_name", blankTo(order.strategyName(), "strategy_ai"));
        payload.put("strategy_version", blankTo(order.strategyVersion(), "view-submit"));
        payload.put("strategy_run_id", order.strategyRunId());
        payload.put("strategy_signal_id", order.strategySignalId());

        payload.put("signal_date", order.signalDate() == null ? null : order.signalDate().toString());
        payload.put("signal_type", blankTo(order.signalType(), order.actionType()));
        payload.put("signal_score", order.signalScore());
        payload.put("signal_position_size", order.signalPositionSize());

        /*
         * Connector does not use the following fields directly,
         * but retaining them in request_payload is useful for View/DB traceability.
         */
        payload.put("view_execution_order_id", order.id());
        payload.put("view_execution_plan_id", order.executionPlanId());
        payload.put("view_source_type", order.sourceType());
        payload.put("view_execution_mode", order.executionMode());
        payload.put("view_source_trade_log_id", order.sourceTradeLogId());
        payload.put("view_source_position_state_id", order.sourcePositionStateId());
        payload.put("view_sell_reason", order.sellReason());
        payload.put("view_expected_sell_price", order.expectedSellPrice());
        payload.put("view_expected_pnl_amount", order.expectedPnlAmount());
        payload.put("view_expected_pnl_rate", order.expectedPnlRate());

        return payload;
    }

    private Map<String, Object> callConnector(
            SubmitOrderRow order,
            Map<String, Object> requestPayload
    ) {
        String path = ACTION_BUY.equalsIgnoreCase(order.actionType())
                ? BUY_ORDER_PATH
                : SELL_ORDER_PATH;

        String url = connectorProperties.buildUrl(path);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException(
                        "Connector HTTP 실패. status=" + response.getStatusCode()
                );
            }

            Map<String, Object> body = response.getBody();

            if (body == null) {
                throw new IllegalStateException("Connector 응답 body가 비어 있음.");
            }

            if (body.get("error") != null) {
                throw new IllegalStateException("Connector 오류 응답: " + body.get("error"));
            }

            return body;

        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "Connector 호출 실패. url=" + url + ", message=" + e.getMessage(),
                    e
            );
        }
    }

    private Long extractConnectorOrderRequestId(Map<String, Object> responsePayload) {
        Long direct = toLongOrNull(firstNonNull(
                responsePayload.get("order_request_id"),
                responsePayload.get("orderRequestId"),
                responsePayload.get("connector_order_request_id"),
                responsePayload.get("connectorOrderRequestId")
        ));

        if (direct != null) {
            return direct;
        }

        Object response = responsePayload.get("response");

        if (response instanceof Map<?, ?> responseMap) {
            Long nested = toLongOrNull(firstNonNull(
                    responseMap.get("order_request_id"),
                    responseMap.get("orderRequestId"),
                    responseMap.get("connector_order_request_id"),
                    responseMap.get("connectorOrderRequestId")
            ));

            if (nested != null) {
                return nested;
            }
        }

        Object data = responsePayload.get("data");

        if (data instanceof Map<?, ?> dataMap) {
            Long nested = toLongOrNull(firstNonNull(
                    dataMap.get("order_request_id"),
                    dataMap.get("orderRequestId"),
                    dataMap.get("connector_order_request_id"),
                    dataMap.get("connectorOrderRequestId")
            ));

            if (nested != null) {
                return nested;
            }
        }

        Object output = responsePayload.get("output");

        if (output instanceof Map<?, ?> outputMap) {
            return toLongOrNull(firstNonNull(
                    outputMap.get("order_request_id"),
                    outputMap.get("orderRequestId"),
                    outputMap.get("connector_order_request_id"),
                    outputMap.get("connectorOrderRequestId")
            ));
        }

        return null;
    }

    private void markSubmitted(
            Long executionOrderId,
            Long connectorOrderRequestId,
            String requestPayloadJson,
            String resultPayloadJson
    ) {
        String sql = """
                UPDATE strategy_execution_order
                SET execution_status = ?,
                    approved_at = now(),
                    approved_by = ?,
                    connector_order_request_id = ?,
                    request_payload = ?::jsonb,
                    result_payload = ?::jsonb,
                    updated_at = now()
                WHERE id = ?
                """;

        int updated = jdbcTemplate.update(
                sql,
                STATUS_SUBMITTED,
                APPROVED_BY,
                connectorOrderRequestId,
                requestPayloadJson,
                resultPayloadJson,
                executionOrderId
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "strategy_execution_order SUBMITTED 업데이트 실패. executionOrderId=" + executionOrderId
            );
        }
    }

    private void markFailed(
            Long executionOrderId,
            String requestPayloadJson,
            Exception e
    ) {
        Map<String, Object> errorPayload = new LinkedHashMap<>();
        errorPayload.put("source", "StrategyExecutionSubmitService");
        errorPayload.put("status", STATUS_FAILED);
        errorPayload.put("message", e.getMessage());
        errorPayload.put("exception", e.getClass().getName());

        String errorPayloadJson = toJson(errorPayload);

        String sql = """
                UPDATE strategy_execution_order
                SET execution_status = ?,
                    approved_at = now(),
                    approved_by = ?,
                    request_payload = ?::jsonb,
                    result_payload = ?::jsonb,
                    updated_at = now()
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                STATUS_FAILED,
                APPROVED_BY,
                requestPayloadJson,
                errorPayloadJson,
                executionOrderId
        );
    }

    private String normalizeOrderMethod(String orderMethod) {
        String method = nvl(orderMethod).toUpperCase();

        if (method.isBlank()) {
            return "MARKET";
        }

        if ("MKT".equals(method)) {
            return "MARKET";
        }

        if ("LMT".equals(method)) {
            return "LIMIT";
        }

        return method;
    }

    private BigDecimal normalizeOrderPrice(SubmitOrderRow order) {
        String method = normalizeOrderMethod(order.orderMethod());

        if ("MARKET".equalsIgnoreCase(method)) {
            return null;
        }

        return order.orderPrice();
    }

    /*
     * Creates a JSON string for jsonb storage without Jackson.
     * This is sufficient because the current payload primarily contains Map / String / Number / Boolean / null.
     */
    private String toJson(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");

            boolean first = true;

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    sb.append(",");
                }

                sb.append("\"")
                        .append(escapeJson(String.valueOf(entry.getKey())))
                        .append("\":")
                        .append(toJsonValue(entry.getValue()));

                first = false;
            }

            sb.append("}");
            return sb.toString();
        }

        return toJsonValue(value);
    }

    private String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof Map<?, ?>) {
            return toJson(value);
        }

        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }

        return "\""
                + escapeJson(String.valueOf(value))
                + "\"";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private Object firstNonNull(Object... values) {
        if (values == null) {
            return null;
        }

        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private Long getLongOrNull(Object value) {
        return toLongOrNull(value);
    }

    private Long toLongOrNull(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        String text = String.valueOf(value).trim();

        if (text.isEmpty()) {
            return null;
        }

        return Long.parseLong(text);
    }

    private Integer getIntegerOrNull(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        String text = String.valueOf(value).trim();

        if (text.isEmpty()) {
            return null;
        }

        return Integer.parseInt(text);
    }

    private String nvl(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankTo(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record SubmitOrderRow(
            Long id,
            Long executionPlanId,
            Long strategySignalId,
            Long accountId,
            String accountNo,
            LocalDate signalDate,
            String tickerCode,
            String stockName,
            String actionType,
            String signalType,
            BigDecimal signalScore,
            BigDecimal signalPositionSize,
            BigDecimal targetAmount,
            Integer currentQty,
            BigDecimal currentEvalAmount,
            Integer orderQty,
            BigDecimal orderPrice,
            String orderMethod,
            String executionStatus,
            boolean approvalRequired,
            Long connectorOrderRequestId,
            Long sourceTradeLogId,
            String sourceRunId,
            String sourceType,
            String executionMode,
            Long sourcePositionStateId,
            String sellReason,
            BigDecimal expectedSellPrice,
            BigDecimal expectedPnlAmount,
            BigDecimal expectedPnlRate,
            String strategyName,
            String strategyVersion,
            String strategyRunId,
            Long parentOrderRequestId
    ) {
    }
}