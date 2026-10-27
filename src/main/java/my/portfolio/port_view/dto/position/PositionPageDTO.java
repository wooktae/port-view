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
/**
 * View DTO containing the summary cards, Return display values, and Stock rows required by the Positions view.
 * The Service calculates the amount/Return text and CSS classes for Thymeleaf to display directly.
 */
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

    private String bestPositionTitleText;
    private String bestPositionName;
    private String bestPositionRateText;
    private String bestPositionProfitClass;

    private String worstPositionName;
    private String worstPositionRateText;
    private String worstPositionProfitClass;

    private List<PositionRowDTO> rows = new ArrayList<>();
}
