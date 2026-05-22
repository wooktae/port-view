package my.portfolio.port_view.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "trade_orders")
public class TradeOrders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_no", nullable = false)
    private String accountNo;

    @Column(name = "order_no", nullable = false)
    private String orderNo;

    @Column(name = "branch_code", nullable = false)
    private String branchCode;

    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    @Column(name = "order_type")
    private String orderType;

    private String side;

    @Column(name = "order_qty")
    private Integer orderQty;

    @Column(name = "executed_qty")
    private Integer executedQty;

    @Column(name = "avg_exec_price")
    private BigDecimal avgExecPrice;

    @Column(name = "total_exec_amount")
    private BigDecimal totalExecAmount;

    @Column(name = "order_time")
    private String orderTime;

    @Column(name = "cancel_flag")
    private String cancelFlag;

    
    // getters / setters
}
