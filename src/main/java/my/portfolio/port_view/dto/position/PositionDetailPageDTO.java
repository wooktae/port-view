package my.portfolio.port_view.dto.position;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PositionDetailPageDTO {

    private String accountNo;
    private String tickerCode;
    private String stockName;
    private String market;

    private boolean hasPosition;

    private int quantity;
    private int sellableQuantity;

    private BigDecimal avgBuyPrice;
    private BigDecimal buyAmount;
    private BigDecimal currentPrice;
    private BigDecimal evalAmount;
    private BigDecimal evalProfit;
    private BigDecimal evalProfitRate;

    private String quantityText;
    private String sellableQuantityText;

    private String avgBuyPriceText;
    private String buyAmountText;
    private String currentPriceText;
    private String evalAmountText;
    private String evalProfitText;
    private String evalProfitRateText;

    private String profitClass;

    private String asOfDate;
    private String asOfTs;

    private PositionDetailSummaryDTO summary = new PositionDetailSummaryDTO();

    private List<PositionAgencyReportDTO> reports = new ArrayList<>();
    private List<PositionNewsDTO> news = new ArrayList<>();
}