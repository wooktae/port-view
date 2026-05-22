package my.portfolio.port_view.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderPageDTO {

    private String accountNo;

    private boolean hasOrders;

    private int totalCount;
    private int buyCount;
    private int sellCount;
    private int modifyCount;
    private int cancelCount;

    private int filledCount;
    private int acceptedCount;
    private int canceledCount;
    private int rejectedCount;
    private int cancelAcceptedCount;

    private String latestRequestedAt;

    private List<OrderRowDTO> rows = new ArrayList<>();
}