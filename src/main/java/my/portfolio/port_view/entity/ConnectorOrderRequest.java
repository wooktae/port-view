package my.portfolio.port_view.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * connector_order_request
 *
 * Connector MS의 주문 요청 원장.
 * BUY / SELL / MODIFY / CANCEL 요청의 중심 테이블.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "connector_order_request")
public class ConnectorOrderRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "account_no", nullable = false, length = 32)
    private String accountNo;

    @Column(name = "ticker_code", nullable = false, length = 32)
    private String tickerCode;

    @Column(name = "stock_name")
    private String stockName;

    /**
     * BUY / SELL / MODIFY / CANCEL
     */
    @Column(name = "request_type", nullable = false, length = 32)
    private String requestType;

    /**
     * LIMIT / MARKET 등
     */
    @Column(name = "order_method", nullable = false, length = 32)
    private String orderMethod;

    @Column(name = "order_price", precision = 18, scale = 4)
    private BigDecimal orderPrice;

    @Column(name = "order_qty", nullable = false)
    private Integer orderQty;

    @Column(name = "requested_amount", precision = 20, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "parent_order_request_id")
    private Long parentOrderRequestId;

    @Column(name = "strategy_name")
    private String strategyName;

    @Column(name = "strategy_version")
    private String strategyVersion;

    @Column(name = "strategy_run_id")
    private String strategyRunId;

    @Column(name = "strategy_signal_id")
    private Long strategySignalId;

    @Column(name = "signal_date")
    private LocalDate signalDate;

    @Column(name = "signal_type")
    private String signalType;

    @Column(name = "signal_score", precision = 18, scale = 8)
    private BigDecimal signalScore;

    @Column(name = "signal_position_size", precision = 18, scale = 8)
    private BigDecimal signalPositionSize;

    /**
     * REQUESTED / ACCEPTED / FILLED / CANCELED / CANCEL_ACCEPTED / REJECTED 등
     */
    @Column(name = "request_status", nullable = false, length = 32)
    private String requestStatus;

    @Column(name = "broker_order_no")
    private String brokerOrderNo;

    @Column(name = "broker_branch_code")
    private String brokerBranchCode;

    @Column(name = "rejection_code")
    private String rejectionCode;

    @Column(name = "rejection_message")
    private String rejectionMessage;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @Column(name = "last_event_at")
    private OffsetDateTime lastEventAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}