package my.portfolio.port_view.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * connector_fill
 *
 * 실제 체결 내역.
 * 주문 상세 화면 하단의 체결 테이블에 사용.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "connector_fill")
public class ConnectorFill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_request_id")
    private Long orderRequestId;

    @Column(name = "order_event_id")
    private Long orderEventId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "broker_order_no")
    private String brokerOrderNo;

    @Column(name = "broker_branch_code")
    private String brokerBranchCode;

    @Column(name = "ticker_code", nullable = false)
    private String tickerCode;

    @Column(name = "side", nullable = false)
    private String side;

    @Column(name = "fill_seq")
    private Integer fillSeq;

    @Column(name = "fill_qty", nullable = false)
    private Integer fillQty;

    @Column(name = "fill_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal fillPrice;

    /**
     * DB generated column.
     * View MS에서는 읽기만 함.
     */
    @Column(name = "fill_amount", precision = 20, scale = 2, insertable = false, updatable = false)
    private BigDecimal fillAmount;

    @Column(name = "fill_ts", nullable = false)
    private OffsetDateTime fillTs;

    @Column(name = "fee_amount", precision = 18, scale = 4)
    private BigDecimal feeAmount;

    @Column(name = "tax_amount", precision = 18, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}