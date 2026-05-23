package my.portfolio.port_view.dto.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderEventTimelineDTO {

    private Long id;
    private Long orderRequestId;

    private String eventType;
    private String eventTypeLabel;
    private String eventTypeClass;

    private String side;
    private String sideLabel;
    private String sideClass;

    private String brokerOrderNo;
    private String brokerBranchCode;

    private String tickerCode;
    private String stockName;

    private String orderQtyText;
    private String executedQtyText;
    private String remainingQtyText;
    private String avgExecPriceText;
    private String totalExecAmountText;

    private String eventTsText;
    private String sourceApi;
}