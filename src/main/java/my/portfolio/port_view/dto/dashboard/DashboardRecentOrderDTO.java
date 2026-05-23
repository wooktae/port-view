package my.portfolio.port_view.dto.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DashboardRecentOrderDTO {

    private Long id;

    private String tickerCode;
    private String stockName;

    private String requestType;
    private String requestTypeLabel;
    private String requestTypeClass;

    private String requestStatus;
    private String requestStatusLabel;
    private String requestStatusClass;

    private String orderMethod;
    private String orderQtyText;
    private String orderPriceText;
    private String requestedAmountText;

    private String brokerOrderNo;
    private String brokerBranchCode;
    private String brokerText;

    private Long parentOrderRequestId;
    private String parentOrderRequestIdText;
    private boolean hasParent;

    private String requestedAtText;
    private String lastEventAtText;
}