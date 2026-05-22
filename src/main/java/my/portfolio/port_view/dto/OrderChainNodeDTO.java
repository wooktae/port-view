package my.portfolio.port_view.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderChainNodeDTO {

    private Long id;

    private String stockName;
    private String tickerCode;

    private String requestType;
    private String requestTypeLabel;
    private String requestTypeClass;

    private String requestStatus;
    private String requestStatusLabel;
    private String requestStatusClass;

    private String orderMethod;
    private String orderPriceText;
    private String orderQtyText;

    private String brokerOrderNo;
    private String brokerBranchCode;

    private Long parentOrderRequestId;
    private String parentText;

    private String requestedAtText;
    private String lastEventAtText;

    private boolean current;
    private boolean root;
}