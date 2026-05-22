package my.portfolio.port_view.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailPageDTO {

    private String accountNo;

    private boolean found;

    private Long currentOrderId;
    private String titleText;

    private OrderRowDTO currentOrder;

    private List<OrderChainNodeDTO> chain = new ArrayList<>();
    private List<OrderEventTimelineDTO> events = new ArrayList<>();
    private List<OrderFillDTO> fills = new ArrayList<>();

    private int chainCount;
    private int eventCount;
    private int fillCount;

    private String lifecycleText;
    private String finalStatusText;
}