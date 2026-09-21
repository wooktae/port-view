package my.portfolio.port_view.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * connector_balance_snapshot
 *
 * Account Balance Snapshot collected by the Connector MS.
 * Used by the View MS to display the latest Balance card.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "connector_balance_snapshot")
public class ConnectorBalanceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "account_no", nullable = false, length = 32)
    private String accountNo;

    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    @Column(name = "as_of_ts", nullable = false)
    private OffsetDateTime asOfTs;

    @Column(name = "cash_balance", nullable = false)
    private BigDecimal cashBalance;

    @Column(name = "withdrawable_cash")
    private BigDecimal withdrawableCash;

    @Column(name = "orderable_cash")
    private BigDecimal orderableCash;

    @Column(name = "nextday_exec_amt", nullable = false)
    private BigDecimal nextdayExecAmt;

    @Column(name = "prev_closing_amt", nullable = false)
    private BigDecimal prevClosingAmt;

    @Column(name = "total_eval_amount", nullable = false)
    private BigDecimal totalEvalAmount;

    @Column(name = "eval_profit", nullable = false)
    private BigDecimal evalProfit;

    @Column(name = "buy_amount_today", nullable = false)
    private BigDecimal buyAmountToday;

    @Column(name = "sell_amount_today", nullable = false)
    private BigDecimal sellAmountToday;

    @Column(name = "fee_total_today", nullable = false)
    private BigDecimal feeTotalToday;

    @Column(name = "source_api")
    private String sourceApi;

    @Column(name = "source_version")
    private String sourceVersion;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}