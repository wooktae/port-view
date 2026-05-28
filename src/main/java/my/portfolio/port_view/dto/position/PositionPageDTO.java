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
 * 보유 종목 목록 화면에 필요한 요약 카드, 수익률 표시값, 종목 row를 담는 View DTO.
 * 금액/수익률 문구와 CSS class는 Service에서 계산해 Thymeleaf가 그대로 표시한다.
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
