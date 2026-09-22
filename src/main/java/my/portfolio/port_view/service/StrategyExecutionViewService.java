package my.portfolio.port_view.service;

import lombok.RequiredArgsConstructor;
import my.portfolio.port_view.dto.strategy.StrategyExecutionOrderDto;
import my.portfolio.port_view.dto.strategy.StrategyExecutionPlanDto;
import my.portfolio.port_view.dto.strategy.StrategyExecutionPlanPageDto;
import my.portfolio.port_view.dto.strategy.StrategyMarketBlockReasonDto;
import my.portfolio.port_view.repository.StrategyExecutionQueryRepository;
import my.portfolio.port_view.util.ViewFormatUtils;
import my.portfolio.port_view.util.ViewMessages;
import my.portfolio.port_view.util.ViewTextUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class StrategyExecutionViewService {

    private final StrategyExecutionQueryRepository repository;

    public StrategyExecutionPlanDto getLatestPlanOrNull() {
        List<StrategyExecutionPlanDto> plans = repository.findPlans();
        return plans.isEmpty() ? null : plans.get(0);
    }

    public StrategyExecutionPlanPageDto getPlanPage() {
        List<StrategyExecutionPlanDto> plans = repository.findPlans();

        StrategyExecutionPlanPageDto page = new StrategyExecutionPlanPageDto();
        page.setPlans(plans);
        page.setTotalPlanCount(plans.size());

        for (StrategyExecutionPlanDto plan : plans) {
            countPlanStatus(page, plan);
            accumulateOrderCounts(page, plan);
        }

        applyOrderActionCounts(page);

        if (!plans.isEmpty()) {
            StrategyExecutionPlanDto latest = plans.get(0);

            page.setLatestPlanDateText(
                    latest.planDate() == null ? "-" : latest.planDate().toString()
            );
            page.setLatestCreatedAtText(ViewFormatUtils.formatDateTime(latest.createdAt()));
            page.setLatestAvailableCashText(ViewFormatUtils.formatMoney(latest.availableCash()));
            page.setLatestMaxOrderAmountText(ViewFormatUtils.formatMoney(latest.maxOrderAmount()));

            page.setLatestPlanStatusText(latest.planStatusLabel());
            page.setLatestMarketSignalText(latest.marketSignalLabel());
        }

        return page;
    }

    public StrategyExecutionPlanDto getPlan(Long planId) {
        return repository.findPlanById(planId)
                .orElseThrow(() -> new IllegalArgumentException("전략 실행 계획을 찾을 수 없음. planId=" + planId));
    }

    public StrategyMarketBlockReasonDto getMarketBlockReason(Long planId) {
        return repository.findMarketBlockReasonByPlanId(planId)
                .map(this::decorateMarketBlockReason)
                .orElse(null);
    }

    private StrategyMarketBlockReasonDto decorateMarketBlockReason(StrategyMarketBlockReasonDto raw) {
        if (raw == null || !raw.hasData()) {
            return raw;
        }

        String headline = buildMarketBlockHeadline(raw);
        String summaryText = buildMarketBlockSummary(raw);
        List<String> reasonLines = buildMarketBlockReasonLines(raw);

        return new StrategyMarketBlockReasonDto(
                raw.featureDate(),
                raw.marketSignal(),
                raw.planRiskRegime(),
                raw.featureRiskRegime(),
                raw.marketRegimeScore(),
                raw.flowPressureScore(),
                raw.breadthPressureScore(),
                raw.marketFlowStrengthScore(),
                raw.marketForeignNetRatio5d(),
                raw.marketInstitutionNetRatio5d(),
                raw.globalRiskScore(),
                raw.macroPressureScore(),
                headline,
                summaryText,
                reasonLines
        );
    }

    private String buildMarketBlockHeadline(StrategyMarketBlockReasonDto reason) {
        String signal = reason.marketSignal() == null ? "" : reason.marketSignal().toUpperCase();

        return switch (signal) {
            case "AGGRESSIVE" -> {
                if (gt(reason.marketRegimeScore(), "0.30") || gt(reason.flowPressureScore(), "0.30")) {
                    yield ViewMessages.text("strategy.market.headline.aggressiveStrong");
                }
                yield ViewMessages.text("strategy.market.headline.aggressive");
            }

            case "NEUTRAL" -> {
                if (gt(reason.breadthPressureScore(), "0") && lt(reason.flowPressureScore(), "0")) {
                    yield ViewMessages.text("strategy.market.headline.neutralFlow");
                }
                yield ViewMessages.text("strategy.market.headline.neutral");
            }

            case "DEFENSIVE" -> {
                if (lt(reason.flowPressureScore(), "0")) {
                    yield ViewMessages.text("strategy.market.headline.defensiveFlow");
                }
                yield ViewMessages.text("strategy.market.headline.defensive");
            }

            case "BLOCK" -> {
                if (lt(reason.flowPressureScore(), "-0.30")) {
                    yield ViewMessages.text("strategy.market.headline.blockFlow");
                }

                if (lt(reason.marketRegimeScore(), "0")) {
                    yield ViewMessages.text("strategy.market.headline.blockScore");
                }

                yield ViewMessages.text("strategy.market.headline.block");
            }

            default -> ViewMessages.text("strategy.market.headline.unknown");
        };
    }

    private String buildMarketBlockSummary(StrategyMarketBlockReasonDto reason) {
        String signal = reason.marketSignal() == null ? "" : reason.marketSignal().toUpperCase();

        return switch (signal) {
            case "AGGRESSIVE" -> ViewMessages.text("strategy.market.summary.aggressive");
            case "NEUTRAL" -> ViewMessages.text("strategy.market.summary.neutral");
            case "DEFENSIVE" -> ViewMessages.text("strategy.market.summary.defensive");
            case "BLOCK" -> ViewMessages.text("strategy.market.summary.block");
            default -> ViewMessages.text("strategy.market.summary.unknown");
        };
    }

    private List<String> buildMarketBlockReasonLines(StrategyMarketBlockReasonDto reason) {
        List<String> lines = new ArrayList<>();

        if (lt(reason.marketRegimeScore(), "0")) {
            lines.add(ViewMessages.text(
                    "strategy.market.reason.regimeNegative",
                    fmt(reason.marketRegimeScore())
            ));
        } else if (reason.marketRegimeScore() != null) {
            lines.add(ViewMessages.text(
                    "strategy.market.reason.regimeMixed",
                    fmt(reason.marketRegimeScore())
            ));
        }

        if (lt(reason.flowPressureScore(), "-0.30")) {
            lines.add(ViewMessages.text(
                    "strategy.market.reason.flowPressure",
                    fmt(reason.flowPressureScore())
            ));
        }

        if (lt(reason.marketFlowStrengthScore(), "-0.30")) {
            lines.add(ViewMessages.text(
                    "strategy.market.reason.flowStrength",
                    fmt(reason.marketFlowStrengthScore())
            ));
        }

        if (lt(reason.marketForeignNetRatio5d(), "0")) {
            lines.add(ViewMessages.text(
                    "strategy.market.reason.foreignFlow",
                    pct(reason.marketForeignNetRatio5d())
            ));
        }

        if (reason.marketInstitutionNetRatio5d() != null) {
            if (gt(reason.marketInstitutionNetRatio5d(), "0")) {
                lines.add(ViewMessages.text(
                        "strategy.market.reason.institutionPositive",
                        pct(reason.marketInstitutionNetRatio5d())
                ));
            } else {
                lines.add(ViewMessages.text(
                        "strategy.market.reason.institutionWeak",
                        pct(reason.marketInstitutionNetRatio5d())
                ));
            }
        }

        if (reason.breadthPressureScore() != null) {
            if (lt(reason.breadthPressureScore(), "0")) {
                lines.add(ViewMessages.text(
                        "strategy.market.reason.breadthWeak",
                        fmt(reason.breadthPressureScore())
                ));
            } else {
                lines.add(ViewMessages.text(
                        "strategy.market.reason.breadthPositive",
                        fmt(reason.breadthPressureScore())
                ));
            }
        }

        if (reason.featureRiskRegime() != null && !reason.featureRiskRegime().isBlank()) {
            if ("RISK_ON".equalsIgnoreCase(reason.featureRiskRegime())) {
                lines.add(ViewMessages.text("strategy.market.reason.riskOn"));
            } else if ("RISK_OFF".equalsIgnoreCase(reason.featureRiskRegime())) {
                lines.add(ViewMessages.text("strategy.market.reason.riskOff"));
            } else {
                lines.add(ViewMessages.text(
                        "strategy.market.reason.riskNeutral",
                        reason.featureRiskRegime()
                ));
            }
        }

        if (lines.isEmpty()) {
            lines.add(ViewMessages.text("strategy.market.reason.insufficient"));
        }

        return lines;
    }

    private boolean lt(BigDecimal value, String threshold) {
        return value != null && value.compareTo(new BigDecimal(threshold)) < 0;
    }

    private boolean gt(BigDecimal value, String threshold) {
        return value != null && value.compareTo(new BigDecimal(threshold)) > 0;
    }

    private String fmt(BigDecimal value) {
        return value == null ? "-" : ViewFormatUtils.formatDecimal2(value);
    }

    private String pct(BigDecimal value) {
        return value == null ? "-" : ViewFormatUtils.formatSignedPercent(value);
    }

    public List<StrategyExecutionOrderDto> getOrders(Long planId) {
        return repository.findOrdersByPlanId(planId);
    }

    public List<Map<String, Object>> getPositionStates(Long planId) {
        return repository.findPositionStatesByPlanId(planId);
    }

    public List<Map<String, Object>> getConnectorOrders(Long planId) {
        return repository.findConnectorOrdersByPlanId(planId);
    }

    public List<Map<String, Object>> getConnectorEventsAndFills(Long planId) {
        return repository.findConnectorEventsAndFillsByPlanId(planId);
    }

    public Map<String, Object> getCurrentPositionSummary() {
        return repository.findCurrentPositionSummary();
    }

    private void countPlanStatus(
            StrategyExecutionPlanPageDto page,
            StrategyExecutionPlanDto plan
    ) {
        String status = ViewTextUtils.upper(plan.planStatus());

        switch (status) {
            case "READY" -> page.setReadyPlanCount(page.getReadyPlanCount() + 1);
            case "BLOCKED" -> page.setBlockedPlanCount(page.getBlockedPlanCount() + 1);
            case "PARTIALLY_BLOCKED" -> page.setPartiallyBlockedPlanCount(page.getPartiallyBlockedPlanCount() + 1);
            case "NO_CANDIDATE" -> page.setNoCandidatePlanCount(page.getNoCandidatePlanCount() + 1);
            case "FAILED", "ERROR" -> page.setFailedPlanCount(page.getFailedPlanCount() + 1);
            default -> page.setOtherPlanCount(page.getOtherPlanCount() + 1);
        }
    }

    private void accumulateOrderCounts(
            StrategyExecutionPlanPageDto page,
            StrategyExecutionPlanDto plan
    ) {
        page.setTotalCandidateCount(
                page.getTotalCandidateCount() + nvl(plan.totalCandidateCount())
        );
        page.setTotalReadyOrderCount(
                page.getTotalReadyOrderCount() + nvl(plan.readyOrderCount())
        );
        page.setTotalBlockedOrderCount(
                page.getTotalBlockedOrderCount() + nvl(plan.blockedOrderCount())
        );
        page.setTotalSkippedOrderCount(
                page.getTotalSkippedOrderCount() + nvl(plan.skippedOrderCount())
        );
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }


    private void applyOrderActionCounts(StrategyExecutionPlanPageDto page) {
        List<Map<String, Object>> rows = repository.findOrderCountsForRecentPlans();

        Map<Long, Integer> buyMap = new HashMap<>();
        Map<Long, Integer> sellMap = new HashMap<>();
        Map<Long, Integer> readyMap = new HashMap<>();

        for (Map<String, Object> row : rows) {
            Long planId = toLong(row.get("execution_plan_id"));

            if (planId == null) {
                continue;
            }

            buyMap.put(planId, toInt(row.get("buy_candidate_count")));
            sellMap.put(planId, toInt(row.get("sell_candidate_count")));
            readyMap.put(planId, toInt(row.get("ready_order_count")));
        }

        page.setBuyCandidateCountByPlanId(buyMap);
        page.setSellCandidateCountByPlanId(sellMap);
        page.setReadyOrderCountByPlanId(readyMap);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
