package my.portfolio.port_view.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class DashboardPositionDTO {

    private String tickerCode;
    private String stockName;

    private Integer quantity;
    private Integer sellableQuantity;

    private BigDecimal avgBuyPrice;
    private BigDecimal buyAmount;
    private BigDecimal currentPrice;
    private BigDecimal evalAmount;
    private BigDecimal evalProfit;
    private BigDecimal evalProfitRate;

    private String avgBuyPriceText;
    private String buyAmountText;
    private String currentPriceText;
    private String evalAmountText;
    private String evalProfitText;
    private String evalProfitRateText;

    private String asOfDate;

    /**
     * profit / loss / neutral
     */
    private String profitClass;
}