package my.portfolio.port_view.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "holdings")
public class Holdings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_no", nullable = false)
    private String accountNo;

    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    private Integer quantity;

    @Column(name = "avg_buy_price")
    private BigDecimal avgBuyPrice;

    @Column(name = "buy_amount")
    private BigDecimal buyAmount;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @Column(name = "eval_amount")
    private BigDecimal evalAmount;

    @Column(name = "eval_profit")
    private BigDecimal evalProfit;

    @Column(name = "as_of_date")
    private LocalDate asOfDate;

    // getters / setters
}
