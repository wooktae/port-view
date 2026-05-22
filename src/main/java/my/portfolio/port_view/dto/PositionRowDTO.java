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
public class PositionRowDTO {

    private String tickerCode;
    private String stockName;
    private String market;

    private int quantity;
    private int sellableQuantity;

    private BigDecimal avgBuyPrice;
    private BigDecimal buyAmount;
    private BigDecimal currentPrice;
    private BigDecimal evalAmount;
    private BigDecimal evalProfit;
    private BigDecimal evalProfitRate;
    private BigDecimal weightRate;

    private String avgBuyPriceText;
    private String buyAmountText;
    private String currentPriceText;
    private String evalAmountText;
    private String evalProfitText;
    private String evalProfitRateText;
    private String weightRateText;

    /**
     * profit / loss / neutral
     */
    private String profitClass;

    /**
     * CSS width 값으로 사용.
     * 예: 42 -> width: 42%
     */
    private int weightBarWidth;

    /**
     * 수익률 bar width.
     */
    private int profitBarWidth;

    private String asOfDate;
    private String asOfTs;

    private List<PositionProfitPointDTO> profitPoints = new ArrayList<>();
    
}