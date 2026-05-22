package my.portfolio.port_view.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "balance_summary")
public class BalanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_no", nullable = false)
    private String accountNo;

    @Column(name = "cash_balance")
    private BigDecimal cashBalance;

    @Column(name = "nextday_exec_amt")
    private BigDecimal nextdayExecAmt;

    @Column(name = "prev_closing_amt")
    private BigDecimal prevClosingAmt;

    @Column(name = "total_eval_amount")
    private BigDecimal totalEvalAmount;

    @Column(name = "eval_profit")
    private BigDecimal evalProfit;

    @Column(name = "buy_amount_today")
    private BigDecimal buyAmountToday;

    @Column(name = "sell_amount_today")
    private BigDecimal sellAmountToday;

    @Column(name = "fee_total_today")
    private BigDecimal feeTotalToday;

    @Column(name = "as_of_date")
    private LocalDate asOfDate;

    
    // getters / setters
}
