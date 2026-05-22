package my.portfolio.port_view.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderFillDTO {

    private Long id;
    private Long orderRequestId;
    private Long orderEventId;

    private String brokerOrderNo;
    private String brokerBranchCode;

    private String tickerCode;

    private String side;
    private String sideLabel;
    private String sideClass;

    private String fillSeqText;
    private String fillQtyText;
    private String fillPriceText;
    private String fillAmountText;

    private String fillTsText;
}