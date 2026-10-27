package my.portfolio.port_view.dto.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRowDTO {

    private Long id;

    private String accountNo;

    private String tickerCode;
    private String stockName;
    private String stockDisplayName;
    private boolean showSeparateTicker;

    private String requestType;
    private String requestTypeLabel;
    private String requestTypeClass;

    private String orderMethod;
    private String orderPriceText;
    private String orderQtyText;
    private String requestedAmountText;

    private Long parentOrderRequestId;
    private String parentOrderRequestIdText;
    private boolean hasParent;

    private String requestStatus;
    private String requestStatusLabel;
    private String requestStatusClass;

    private String brokerOrderNo;
    private String brokerBranchCode;
    private String brokerText;

    private String strategyText;
    private String signalText;

    private String requestedAtText;
    private String acceptedAtText;
    private String lastEventAtText;

    private boolean rejected;
    private String rejectionMessage;
}