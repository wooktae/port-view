package my.portfolio.port_view.service;

import my.portfolio.port_view.dto.dashboard.DashboardMetricDTO;
import my.portfolio.port_view.dto.dashboard.DashboardPositionDTO;
import my.portfolio.port_view.dto.dashboard.DashboardRecentOrderDTO;
import my.portfolio.port_view.dto.dashboard.DashboardViewDTO;
import my.portfolio.port_view.entity.ConnectorBalanceSnapshot;
import my.portfolio.port_view.entity.ConnectorOrderRequest;
import my.portfolio.port_view.entity.ConnectorPositionSnapshot;
import my.portfolio.port_view.repository.ConnectorBalanceSnapshotRepository;
import my.portfolio.port_view.repository.ConnectorOrderRequestRepository;
import my.portfolio.port_view.repository.ConnectorPositionSnapshotRepository;
import my.portfolio.port_view.util.OrderLabelUtils;
import my.portfolio.port_view.util.ViewFormatUtils;
import my.portfolio.port_view.util.ProfitClassUtils;
import my.portfolio.port_view.util.ViewTextUtils;
import my.portfolio.port_view.util.ViewMessages;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Assembles the Balance, Positions, recent Orders, and Return metrics displayed on the Dashboard.
 * Converts Connector Snapshot and Order/Position Repository results into view-specific DTOs.
 */
@Service
public class DashboardService {

    private static final int RECENT_ORDER_LIMIT = 5;

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final ConnectorBalanceSnapshotRepository balanceRepository;
    private final ConnectorPositionSnapshotRepository positionRepository;
    private final ConnectorOrderRequestRepository orderRepository;

    public DashboardService(
            ConnectorBalanceSnapshotRepository balanceRepository,
            ConnectorPositionSnapshotRepository positionRepository,
            ConnectorOrderRequestRepository orderRepository
    ) {
        this.balanceRepository = balanceRepository;
        this.positionRepository = positionRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public DashboardViewDTO getDashboard(String accountNo) {
        ConnectorBalanceSnapshot latestBalance = balanceRepository
                .findTopByAccountNoOrderByAsOfDateDescAsOfTsDesc(accountNo)
                .orElse(null);

        List<ConnectorPositionSnapshot> latestPositions =
                positionRepository.findLatestPositionsByAccountNo(accountNo);

        List<ConnectorOrderRequest> recentOrders =
                orderRepository.findRecentOrdersByAccountNo(accountNo, RECENT_ORDER_LIMIT);

        DashboardViewDTO dashboard = new DashboardViewDTO();
        dashboard.setAccountNo(accountNo);
        dashboard.setHasBalance(latestBalance != null);
        dashboard.setPositionCount(latestPositions.size());

        if (latestBalance != null) {
            dashboard.setAsOfDate(
                    latestBalance.getAsOfDate() == null
                            ? "-"
                            : latestBalance.getAsOfDate().toString()
            );

            dashboard.setAsOfTs(ViewFormatUtils.formatDateTime(latestBalance.getAsOfTs()));

            BigDecimal totalEvalAmount = nvl(latestBalance.getTotalEvalAmount());
            BigDecimal evalProfit = nvl(latestBalance.getEvalProfit());
            BigDecimal buyAmountToday = nvl(latestBalance.getBuyAmountToday());
            BigDecimal sellAmountToday = nvl(latestBalance.getSellAmountToday());

            BigDecimal stockEvalAmount = latestPositions.stream()
                    .map(ConnectorPositionSnapshot::getEvalAmount)
                    .map(this::nvl)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            /*
            * Cash displayed in the view.
            *
            * KIS cash_balance(dnca_tot_amt) may appear high after a Stock purchase because it retains deposit-like semantics.
            * The Dashboard displays the expected cash-like amount: total valuation minus current Stock valuation.
            *
            * Example: 2026-05-27
            * totalEvalAmount 9,931,122 - stockEvalAmount 1,859,060 = 8,072,062
            */
            BigDecimal cashBalance = totalEvalAmount.subtract(stockEvalAmount);

            BigDecimal principal = totalEvalAmount.subtract(evalProfit);
            BigDecimal totalProfitRate = safeRate(evalProfit, principal);

            dashboard.setTotalEvalAmount(totalEvalAmount);
            dashboard.setCashBalance(cashBalance);
            dashboard.setStockEvalAmount(stockEvalAmount);
            dashboard.setEvalProfit(evalProfit);
            dashboard.setTotalProfitRate(totalProfitRate);
            dashboard.setBuyAmountToday(buyAmountToday);
            dashboard.setSellAmountToday(sellAmountToday);

            dashboard.setTotalEvalAmountText(ViewFormatUtils.formatMoney(totalEvalAmount));
            dashboard.setCashBalanceText(ViewFormatUtils.formatMoney(cashBalance));
            dashboard.setStockEvalAmountText(ViewFormatUtils.formatMoney(stockEvalAmount));
            dashboard.setEvalProfitText(ViewFormatUtils.formatSignedMoney(evalProfit));
            dashboard.setTotalProfitRateText(ViewFormatUtils.formatSignedPercentAlready(totalProfitRate));
            dashboard.setBuyAmountTodayText(ViewFormatUtils.formatMoney(buyAmountToday));
            dashboard.setSellAmountTodayText(ViewFormatUtils.formatMoney(sellAmountToday));
            dashboard.setProfitClass(ProfitClassUtils.toProfitClass(evalProfit));
        } else {
            dashboard.setAsOfDate("-");
            dashboard.setAsOfTs("-");
            dashboard.setTotalEvalAmountText("-");
            dashboard.setCashBalanceText("-");
            dashboard.setStockEvalAmountText("-");
            dashboard.setEvalProfitText("-");
            dashboard.setTotalProfitRateText("-");
            dashboard.setBuyAmountTodayText("-");
            dashboard.setSellAmountTodayText("-");
            dashboard.setProfitClass("neutral");
        }

        dashboard.setPositions(toPositionDTOs(latestPositions));
        dashboard.setRecentOrders(toRecentOrderDTOs(recentOrders));
        dashboard.setMetrics(toMetrics(dashboard));

        return dashboard;
    }

    private List<DashboardPositionDTO> toPositionDTOs(List<ConnectorPositionSnapshot> positions) {
        List<DashboardPositionDTO> result = new ArrayList<>();

        for (ConnectorPositionSnapshot p : positions) {
            DashboardPositionDTO dto = new DashboardPositionDTO();

            BigDecimal evalProfit = nvl(p.getEvalProfit());

            /*
             * Calculate the view Return from eval_profit / buy_amount instead of DB eval_profit_rate.
             * This keeps the view stable even if the DB scale changes.
             */
            BigDecimal evalProfitRatePercent = safeRate(nvl(p.getEvalProfit()), nvl(p.getBuyAmount()));

            dto.setTickerCode(p.getTickerCode());
            dto.setStockName(ViewFormatUtils.emptyIfNull(p.getStockName()));
            dto.setQuantity(p.getQuantity());
            dto.setSellableQuantity(p.getSellableQuantity());
            dto.setAvgBuyPrice(p.getAvgBuyPrice());
            dto.setBuyAmount(p.getBuyAmount());
            dto.setCurrentPrice(p.getCurrentPrice());
            dto.setEvalAmount(p.getEvalAmount());
            dto.setEvalProfit(p.getEvalProfit());
            dto.setEvalProfitRate(evalProfitRatePercent);
            dto.setAsOfDate(p.getAsOfDate() == null ? "-" : p.getAsOfDate().toString());

            dto.setAvgBuyPriceText(ViewFormatUtils.formatMoney(p.getAvgBuyPrice()));
            dto.setBuyAmountText(ViewFormatUtils.formatMoney(p.getBuyAmount()));
            dto.setCurrentPriceText(ViewFormatUtils.formatMoney(p.getCurrentPrice()));
            dto.setEvalAmountText(ViewFormatUtils.formatMoney(p.getEvalAmount()));
            dto.setEvalProfitText(ViewFormatUtils.formatSignedMoney(evalProfit));
            dto.setEvalProfitRateText(ViewFormatUtils.formatSignedPercentAlready(evalProfitRatePercent));
            dto.setProfitClass(ProfitClassUtils.toProfitClass(evalProfit));

            result.add(dto);
        }

        return result;
    }

    private List<DashboardRecentOrderDTO> toRecentOrderDTOs(List<ConnectorOrderRequest> orders) {
        List<DashboardRecentOrderDTO> result = new ArrayList<>();

        for (ConnectorOrderRequest order : orders) {
            DashboardRecentOrderDTO dto = new DashboardRecentOrderDTO();

            dto.setId(order.getId());

            dto.setTickerCode(ViewTextUtils.nvl(order.getTickerCode()));
            dto.setStockName(ViewTextUtils.blankTo(order.getStockName(), "-"));

            dto.setRequestType(ViewTextUtils.nvl(order.getRequestType()));
            dto.setRequestTypeLabel(OrderLabelUtils.requestTypeLabel(order.getRequestType()));
            dto.setRequestTypeClass(OrderLabelUtils.requestTypeClass(order.getRequestType()));

            dto.setRequestStatus(ViewTextUtils.nvl(order.getRequestStatus()));
            dto.setRequestStatusLabel(OrderLabelUtils.statusLabel(order.getRequestStatus()));
            dto.setRequestStatusClass(OrderLabelUtils.statusClass(order.getRequestStatus()));

            dto.setOrderMethod(ViewTextUtils.blankTo(order.getOrderMethod(), "-"));
            dto.setOrderQtyText(ViewFormatUtils.formatQty(order.getOrderQty()));
            dto.setOrderPriceText(ViewFormatUtils.formatMoney(order.getOrderPrice()));
            dto.setRequestedAmountText(ViewFormatUtils.formatMoney(order.getRequestedAmount()));

            dto.setBrokerOrderNo(ViewTextUtils.blankTo(order.getBrokerOrderNo(), "-"));
            dto.setBrokerBranchCode(ViewTextUtils.blankTo(order.getBrokerBranchCode(), "-"));
            dto.setBrokerText(toBrokerText(order.getBrokerOrderNo(), order.getBrokerBranchCode()));

            dto.setParentOrderRequestId(order.getParentOrderRequestId());
            dto.setHasParent(order.getParentOrderRequestId() != null);
            dto.setParentOrderRequestIdText(
                    order.getParentOrderRequestId() == null
                            ? "-"
                            : "#" + order.getParentOrderRequestId()
            );

            dto.setRequestedAtText(ViewFormatUtils.formatDateTime(order.getRequestedAt()));
            dto.setLastEventAtText(ViewFormatUtils.formatDateTime(order.getLastEventAt()));

            result.add(dto);
        }

        return result;
    }

    private List<DashboardMetricDTO> toMetrics(DashboardViewDTO dashboard) {
        List<DashboardMetricDTO> metrics = new ArrayList<>();

        metrics.add(new DashboardMetricDTO(
                ViewMessages.text("dashboard.totalEvaluation"),
                dashboard.getTotalEvalAmountText(),
                ViewMessages.text("dashboard.cashPlusStocks"),
                "primary"
        ));

        metrics.add(new DashboardMetricDTO(
                ViewMessages.text("dashboard.cash"),
                dashboard.getCashBalanceText(),
                ViewMessages.text("dashboard.orderableResources"),
                "cash"
        ));

        metrics.add(new DashboardMetricDTO(
                ViewMessages.text("dashboard.stockEvaluation"),
                dashboard.getStockEvalAmountText(),
                ViewMessages.text("dashboard.currentPositionsBased"),
                "stock"
        ));

        metrics.add(new DashboardMetricDTO(
                ViewMessages.text("dashboard.evaluationProfit"),
                dashboard.getEvalProfitText(),
                dashboard.getTotalProfitRateText(),
                dashboard.getProfitClass()
        ));

        return metrics;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal safeRate(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return numerator
                .multiply(HUNDRED)
                .divide(denominator, 4, RoundingMode.HALF_UP);
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

}
