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
public class PositionPageDTO {

    private String accountNo;
    private boolean hasPositions;

    private String asOfDate;
    private String asOfTs;

    private int positionCount;

    private BigDecimal totalBuyAmount;
    private BigDecimal totalEvalAmount;
    private BigDecimal totalEvalProfit;
    private BigDecimal totalProfitRate;

    private String totalBuyAmountText;
    private String totalEvalAmountText;
    private String totalEvalProfitText;
    private String totalProfitRateText;

    private String pageProfitClass;

    private String bestPositionName;
    private String bestPositionRateText;

    private String worstPositionName;
    private String worstPositionRateText;

    private List<PositionRowDTO> rows = new ArrayList<>();
}