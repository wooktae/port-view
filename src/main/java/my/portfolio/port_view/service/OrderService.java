package my.portfolio.port_view.service;

import my.portfolio.port_view.dto.order.OrderChainNodeDTO;
import my.portfolio.port_view.dto.order.OrderDetailPageDTO;
import my.portfolio.port_view.dto.order.OrderEventTimelineDTO;
import my.portfolio.port_view.dto.order.OrderFillDTO;
import my.portfolio.port_view.dto.order.OrderPageDTO;
import my.portfolio.port_view.dto.order.OrderRowDTO;
import my.portfolio.port_view.entity.ConnectorFill;
import my.portfolio.port_view.entity.ConnectorOrderEvent;
import my.portfolio.port_view.entity.ConnectorOrderRequest;
import my.portfolio.port_view.repository.ConnectorFillRepository;
import my.portfolio.port_view.repository.ConnectorOrderEventRepository;
import my.portfolio.port_view.repository.ConnectorOrderRequestRepository;
import my.portfolio.port_view.util.OrderLabelUtils;
import my.portfolio.port_view.util.ViewFormatUtils;
import my.portfolio.port_view.util.ViewMessages;
import my.portfolio.port_view.util.ViewTextUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final int DEFAULT_LIMIT = 100;

    private final ConnectorOrderRequestRepository orderRepository;
    private final ConnectorOrderEventRepository orderEventRepository;
    private final ConnectorFillRepository fillRepository;

    public OrderService(
            ConnectorOrderRequestRepository orderRepository,
            ConnectorOrderEventRepository orderEventRepository,
            ConnectorFillRepository fillRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderEventRepository = orderEventRepository;
        this.fillRepository = fillRepository;
    }

    @Transactional(readOnly = true)
    public OrderPageDTO getOrderPage(String accountNo) {
        List<ConnectorOrderRequest> orders =
                orderRepository.findRecentOrdersByAccountNo(accountNo, DEFAULT_LIMIT);

        OrderPageDTO page = new OrderPageDTO();
        page.setAccountNo(accountNo);
        page.setHasOrders(!orders.isEmpty());
        page.setTotalCount(orders.size());

        if (!orders.isEmpty()) {
            page.setLatestRequestedAt(ViewFormatUtils.formatDateTime(orders.get(0).getRequestedAt()));
        } else {
            page.setLatestRequestedAt("-");
        }

        for (ConnectorOrderRequest order : orders) {
            countSummary(page, order);
            page.getRows().add(toRow(order));
        }

        return page;
    }

    @Transactional(readOnly = true)
    public OrderDetailPageDTO getOrderDetail(Long orderId, String accountNo) {
        ConnectorOrderRequest current =
                orderRepository.findOneByIdAndAccountNo(orderId, accountNo);

        OrderDetailPageDTO page = new OrderDetailPageDTO();
        page.setAccountNo(accountNo);
        page.setCurrentOrderId(orderId);

        if (current == null) {
            page.setFound(false);
            page.setTitleText(ViewMessages.text("orderDetail.notFound"));
            page.setLifecycleText("-");
            page.setFinalStatusText("-");
            return page;
        }

        List<ConnectorOrderRequest> chain =
                orderRepository.findOrderChain(orderId, accountNo);

        List<Long> chainIds = chain.stream()
                .map(ConnectorOrderRequest::getId)
                .toList();

        List<String> brokerOrderNos = chain.stream()
                .map(ConnectorOrderRequest::getBrokerOrderNo)
                .filter(v -> v != null && !v.trim().isEmpty())
                .distinct()
                .toList();

        List<ConnectorOrderEvent> events =
                loadEvents(accountNo, chainIds, brokerOrderNos);

        List<ConnectorFill> fills =
                loadFills(chainIds, brokerOrderNos);

        page.setFound(true);
        page.setCurrentOrder(toRow(current));
        page.setTitleText("#" + current.getId() + " "
                + OrderLabelUtils.requestTypeLabel(current.getRequestType())
                + " / "
                + OrderLabelUtils.statusLabel(current.getRequestStatus()));

        page.setChain(toChainNodes(chain, current.getId()));
        page.setEvents(toEventTimeline(events));
        page.setFills(toFillDTOs(fills));

        page.setChainCount(chain.size());
        page.setEventCount(events.size());
        page.setFillCount(fills.size());
        page.setLifecycleText(buildLifecycleText(chain));
        page.setFinalStatusText(OrderLabelUtils.statusLabel(current.getRequestStatus()));

        return page;
    }

    private List<ConnectorOrderEvent> loadEvents(
            String accountNo,
            List<Long> chainIds,
            List<String> brokerOrderNos
    ) {
        List<ConnectorOrderEvent> events = new ArrayList<>();

        if (chainIds != null && !chainIds.isEmpty()) {
            events.addAll(orderEventRepository.findEventsByOrderRequestIds(accountNo, chainIds));
        }

        if (brokerOrderNos != null && !brokerOrderNos.isEmpty()) {
            events.addAll(orderEventRepository.findEventsByBrokerOrderNos(accountNo, brokerOrderNos));
        }

        return events.stream()
                .collect(Collectors.toMap(
                        ConnectorOrderEvent::getId,
                        event -> event,
                        (left, right) -> left
                ))
                .values()
                .stream()
                .sorted(Comparator
                        .comparing(ConnectorOrderEvent::getEventTs,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ConnectorOrderEvent::getId,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<ConnectorFill> loadFills(
            List<Long> chainIds,
            List<String> brokerOrderNos
    ) {
        List<ConnectorFill> fills = new ArrayList<>();

        if (chainIds != null && !chainIds.isEmpty()) {
            fills.addAll(fillRepository.findFillsByOrderRequestIds(chainIds));
        }

        if (brokerOrderNos != null && !brokerOrderNos.isEmpty()) {
            fills.addAll(fillRepository.findFillsByBrokerOrderNos(brokerOrderNos));
        }

        return fills.stream()
                .collect(Collectors.toMap(
                        ConnectorFill::getId,
                        fill -> fill,
                        (left, right) -> left
                ))
                .values()
                .stream()
                .sorted(Comparator
                        .comparing(ConnectorFill::getFillTs,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ConnectorFill::getId,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<OrderChainNodeDTO> toChainNodes(
            List<ConnectorOrderRequest> chain,
            Long currentOrderId
    ) {
        List<OrderChainNodeDTO> result = new ArrayList<>();

        for (int i = 0; i < chain.size(); i++) {
            ConnectorOrderRequest order = chain.get(i);

            OrderChainNodeDTO node = new OrderChainNodeDTO();
            node.setId(order.getId());
            node.setStockName(ViewTextUtils.blankTo(order.getStockName(), "-"));
            node.setTickerCode(ViewTextUtils.nvl(order.getTickerCode()));

            node.setRequestType(ViewTextUtils.nvl(order.getRequestType()));
            node.setRequestTypeLabel(OrderLabelUtils.requestTypeLabel(order.getRequestType()));
            node.setRequestTypeClass(OrderLabelUtils.requestTypeClass(order.getRequestType()));

            node.setRequestStatus(ViewTextUtils.nvl(order.getRequestStatus()));
            node.setRequestStatusLabel(OrderLabelUtils.statusLabel(order.getRequestStatus()));
            node.setRequestStatusClass(OrderLabelUtils.statusClass(order.getRequestStatus()));

            node.setOrderMethod(ViewTextUtils.blankTo(order.getOrderMethod(), "-"));
            node.setOrderPriceText(ViewFormatUtils.formatMoney(order.getOrderPrice()));
            node.setOrderQtyText(ViewFormatUtils.formatQty(order.getOrderQty()));

            node.setBrokerOrderNo(ViewTextUtils.blankTo(order.getBrokerOrderNo(), "-"));
            node.setBrokerBranchCode(ViewTextUtils.blankTo(order.getBrokerBranchCode(), "-"));

            node.setParentOrderRequestId(order.getParentOrderRequestId());
            node.setParentText(order.getParentOrderRequestId() == null ? "-" : "#" + order.getParentOrderRequestId());

            node.setRequestedAtText(ViewFormatUtils.formatDateTime(order.getRequestedAt()));
            node.setLastEventAtText(ViewFormatUtils.formatDateTime(order.getLastEventAt()));

            node.setCurrent(order.getId().equals(currentOrderId));
            node.setRoot(i == 0);

            result.add(node);
        }

        return result;
    }

    private List<OrderEventTimelineDTO> toEventTimeline(List<ConnectorOrderEvent> events) {
        List<OrderEventTimelineDTO> result = new ArrayList<>();

        for (ConnectorOrderEvent event : events) {
            OrderEventTimelineDTO dto = new OrderEventTimelineDTO();

            dto.setId(event.getId());
            dto.setOrderRequestId(event.getOrderRequestId());

            dto.setEventType(ViewTextUtils.nvl(event.getEventType()));
            dto.setEventTypeLabel(OrderLabelUtils.eventTypeLabel(event.getEventType()));
            dto.setEventTypeClass(OrderLabelUtils.eventTypeClass(event.getEventType()));

            dto.setSide(ViewTextUtils.nvl(event.getSide()));
            dto.setSideLabel(OrderLabelUtils.sideLabel(event.getSide()));
            dto.setSideClass(OrderLabelUtils.sideClass(event.getSide()));

            dto.setBrokerOrderNo(ViewTextUtils.blankTo(event.getBrokerOrderNo(), "-"));
            dto.setBrokerBranchCode(ViewTextUtils.blankTo(event.getBrokerBranchCode(), "-"));

            dto.setTickerCode(ViewTextUtils.nvl(event.getTickerCode()));
            dto.setStockName(ViewTextUtils.blankTo(event.getStockName(), "-"));

            dto.setOrderQtyText(ViewFormatUtils.formatQty(event.getOrderQty()));
            dto.setExecutedQtyText(ViewFormatUtils.formatQty(event.getExecutedQty()));
            dto.setRemainingQtyText(ViewFormatUtils.formatQty(event.getRemainingQty()));
            dto.setAvgExecPriceText(ViewFormatUtils.formatMoney(event.getAvgExecPrice()));
            dto.setTotalExecAmountText(ViewFormatUtils.formatMoney(event.getTotalExecAmount()));

            dto.setEventTsText(ViewFormatUtils.formatDateTime(event.getEventTs()));
            dto.setSourceApi(ViewTextUtils.blankTo(event.getSourceApi(), "-"));

            result.add(dto);
        }

        return result;
    }

    private List<OrderFillDTO> toFillDTOs(List<ConnectorFill> fills) {
        List<OrderFillDTO> result = new ArrayList<>();

        for (ConnectorFill fill : fills) {
            OrderFillDTO dto = new OrderFillDTO();

            dto.setId(fill.getId());
            dto.setOrderRequestId(fill.getOrderRequestId());
            dto.setOrderEventId(fill.getOrderEventId());

            dto.setBrokerOrderNo(ViewTextUtils.blankTo(fill.getBrokerOrderNo(), "-"));
            dto.setBrokerBranchCode(ViewTextUtils.blankTo(fill.getBrokerBranchCode(), "-"));

            dto.setTickerCode(ViewTextUtils.nvl(fill.getTickerCode()));

            dto.setSide(ViewTextUtils.nvl(fill.getSide()));
            dto.setSideLabel(OrderLabelUtils.sideLabel(fill.getSide()));
            dto.setSideClass(OrderLabelUtils.sideClass(fill.getSide()));

            dto.setFillSeqText(fill.getFillSeq() == null ? "-" : String.valueOf(fill.getFillSeq()));
            dto.setFillQtyText(ViewFormatUtils.formatQty(fill.getFillQty()));
            dto.setFillPriceText(ViewFormatUtils.formatMoney(fill.getFillPrice()));
            dto.setFillAmountText(ViewFormatUtils.formatMoney(fill.getFillAmount()));
            dto.setFillTsText(ViewFormatUtils.formatDateTime(fill.getFillTs()));

            result.add(dto);
        }

        return result;
    }

    private void countSummary(OrderPageDTO page, ConnectorOrderRequest order) {
        String type = ViewTextUtils.upper(order.getRequestType());
        String status = ViewTextUtils.upper(order.getRequestStatus());

        switch (type) {
            case "BUY" -> page.setBuyCount(page.getBuyCount() + 1);
            case "SELL" -> page.setSellCount(page.getSellCount() + 1);
            case "MODIFY" -> page.setModifyCount(page.getModifyCount() + 1);
            case "CANCEL" -> page.setCancelCount(page.getCancelCount() + 1);
            default -> {
                // unknown request type ignored
            }
        }

        switch (status) {
            case "FILLED" -> page.setFilledCount(page.getFilledCount() + 1);
            case "ACCEPTED", "ORDER_ACCEPTED", "REQUESTED", "MODIFIED" ->
                    page.setAcceptedCount(page.getAcceptedCount() + 1);
            case "CANCELED" -> page.setCanceledCount(page.getCanceledCount() + 1);
            case "CANCEL_ACCEPTED" -> page.setCancelAcceptedCount(page.getCancelAcceptedCount() + 1);
            case "REJECTED" -> page.setRejectedCount(page.getRejectedCount() + 1);
            default -> {
                // unknown status ignored
            }
        }
    }

    private OrderRowDTO toRow(ConnectorOrderRequest order) {
        OrderRowDTO row = new OrderRowDTO();

        row.setId(order.getId());
        row.setAccountNo(order.getAccountNo());

        row.setTickerCode(ViewTextUtils.nvl(order.getTickerCode()));
        row.setStockName(ViewTextUtils.blankTo(order.getStockName(), "-"));

        row.setRequestType(ViewTextUtils.nvl(order.getRequestType()));
        row.setRequestTypeLabel(OrderLabelUtils.requestTypeLabel(order.getRequestType()));
        row.setRequestTypeClass(OrderLabelUtils.requestTypeClass(order.getRequestType()));

        row.setOrderMethod(ViewTextUtils.blankTo(order.getOrderMethod(), "-"));
        row.setOrderPriceText(ViewFormatUtils.formatMoney(order.getOrderPrice()));
        row.setOrderQtyText(ViewFormatUtils.formatQty(order.getOrderQty()));
        row.setRequestedAmountText(ViewFormatUtils.formatMoney(order.getRequestedAmount()));

        row.setParentOrderRequestId(order.getParentOrderRequestId());
        row.setHasParent(order.getParentOrderRequestId() != null);
        row.setParentOrderRequestIdText(
                order.getParentOrderRequestId() == null
                        ? "-"
                        : "#" + order.getParentOrderRequestId()
        );

        row.setRequestStatus(ViewTextUtils.nvl(order.getRequestStatus()));
        row.setRequestStatusLabel(OrderLabelUtils.statusLabel(order.getRequestStatus()));
        row.setRequestStatusClass(OrderLabelUtils.statusClass(order.getRequestStatus()));

        row.setBrokerOrderNo(ViewTextUtils.blankTo(order.getBrokerOrderNo(), "-"));
        row.setBrokerBranchCode(ViewTextUtils.blankTo(order.getBrokerBranchCode(), "-"));
        row.setBrokerText(toBrokerText(order.getBrokerOrderNo(), order.getBrokerBranchCode()));

        row.setStrategyText(toStrategyText(order));
        row.setSignalText(toSignalText(order));

        row.setRequestedAtText(ViewFormatUtils.formatDateTime(order.getRequestedAt()));
        row.setAcceptedAtText(ViewFormatUtils.formatDateTime(order.getAcceptedAt()));
        row.setLastEventAtText(ViewFormatUtils.formatDateTime(order.getLastEventAt()));

        row.setRejected("REJECTED".equalsIgnoreCase(ViewTextUtils.nvl(order.getRequestStatus())));
        row.setRejectionMessage(ViewTextUtils.blankTo(order.getRejectionMessage(), ""));

        return row;
    }

    private String buildLifecycleText(List<ConnectorOrderRequest> chain) {
        if (chain == null || chain.isEmpty()) {
            return "-";
        }

        return chain.stream()
                .map(order -> "#" + order.getId() + " "
                        + OrderLabelUtils.requestTypeLabel(order.getRequestType()))
                .reduce((left, right) -> left + " → " + right)
                .orElse("-");
    }

    private String toBrokerText(String brokerOrderNo, String brokerBranchCode) {
        if (ViewTextUtils.isBlank(brokerOrderNo)) {
            return "-";
        }

        if (ViewTextUtils.isBlank(brokerBranchCode)) {
            return brokerOrderNo;
        }

        return brokerOrderNo + " / " + brokerBranchCode;
    }

    private String toStrategyText(ConnectorOrderRequest order) {
        if (ViewTextUtils.isBlank(order.getStrategyName())) {
            return "-";
        }

        if (ViewTextUtils.isBlank(order.getStrategyVersion())) {
            return order.getStrategyName();
        }

        return order.getStrategyName() + " " + order.getStrategyVersion();
    }

    private String toSignalText(ConnectorOrderRequest order) {
        if (ViewTextUtils.isBlank(order.getSignalType())) {
            return "-";
        }

        StringBuilder sb = new StringBuilder(order.getSignalType());

        if (order.getSignalScore() != null) {
            sb.append(" / score ")
                    .append(order.getSignalScore().setScale(4, RoundingMode.HALF_UP));
        }

        if (order.getSignalDate() != null) {
            sb.append(" / ").append(order.getSignalDate());
        }

        return sb.toString();
    }

}