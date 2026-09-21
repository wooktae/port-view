package my.portfolio.port_view.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * connector_order_event
 *
 * Broker/Order Lifecycle Event generated after an Order Request.
 * Used in the timeline on the Step4 Order Details view.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "connector_order_event")
public class ConnectorOrderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_request_id")
    private Long orderRequestId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "account_no", nullable = false, length = 32)
    private String accountNo;

    @Column(name = "broker_order_no")
    private String brokerOrderNo;

    @Column(name = "broker_branch_code")
    private String brokerBranchCode;

    @Column(name = "ticker_code", nullable = false, length = 32)
    private String tickerCode;

    @Column(name = "stock_name")
    private String stockName;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(name = "side")
    private String side;

    @Column(name = "order_type_name")
    private String orderTypeName;

    @Column(name = "order_qty")
    private Integer orderQty;

    @Column(name = "executed_qty")
    private Integer executedQty;

    @Column(name = "remaining_qty")
    private Integer remainingQty;

    @Column(name = "avg_exec_price", precision = 18, scale = 4)
    private BigDecimal avgExecPrice;

    @Column(name = "total_exec_amount", precision = 20, scale = 2)
    private BigDecimal totalExecAmount;

    @Column(name = "cancel_flag")
    private String cancelFlag;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "order_time")
    private OffsetDateTime orderTime;

    @Column(name = "event_ts", nullable = false)
    private OffsetDateTime eventTs;

    @Column(name = "source_api")
    private String sourceApi;

    @Column(name = "source_version")
    private String sourceVersion;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "event_key")
    private String eventKey;
}