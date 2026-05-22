package my.portfolio.port_view.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DashboardViewDTO {

    private String accountNo;
    private boolean hasBalance;

    private String asOfDate;
    private String asOfTs;

    private int positionCount;

    private BigDecimal totalEvalAmount;
    private BigDecimal cashBalance;
    private BigDecimal stockEvalAmount;
    private BigDecimal evalProfit;
    private BigDecimal totalProfitRate;
    private BigDecimal buyAmountToday;
    private BigDecimal sellAmountToday;

    private String totalEvalAmountText;
    private String cashBalanceText;
    private String stockEvalAmountText;
    private String evalProfitText;
    private String totalProfitRateText;
    private String buyAmountTodayText;
    private String sellAmountTodayText;

    /**
     * profit / loss / neutral
     */
    private String profitClass;

    private List<DashboardMetricDTO> metrics = new ArrayList<>();
    private List<DashboardPositionDTO> positions = new ArrayList<>();

    /**
     * Dashboard 하단 최근 주문 5건.
     */
    private List<DashboardRecentOrderDTO> recentOrders = new ArrayList<>();
}