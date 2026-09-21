package my.portfolio.port_view.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * connector_position_snapshot
 *
 * Position Snapshot collected by the Connector MS.
 * Used by the View MS to display Position cards and tables.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "connector_position_snapshot")
public class ConnectorPositionSnapshot {

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

    @Column(name = "market")
    private String market;

    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    @Column(name = "as_of_ts", nullable = false)
    private OffsetDateTime asOfTs;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "sellable_quantity")
    private Integer sellableQuantity;

    @Column(name = "avg_buy_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal avgBuyPrice;

    @Column(name = "buy_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal buyAmount;

    @Column(name = "current_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "eval_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal evalAmount;

    @Column(name = "eval_profit", nullable = false, precision = 20, scale = 2)
    private BigDecimal evalProfit;

    @Column(name = "eval_profit_rate", precision = 18, scale = 8)
    private BigDecimal evalProfitRate;

    @Column(name = "source_api")
    private String sourceApi;

    @Column(name = "source_version")
    private String sourceVersion;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}