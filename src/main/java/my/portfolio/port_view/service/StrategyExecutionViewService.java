package my.portfolio.port_view.service;

import lombok.RequiredArgsConstructor;
import my.portfolio.port_view.dto.StrategyExecutionOrderDto;
import my.portfolio.port_view.dto.StrategyExecutionPlanDto;
import my.portfolio.port_view.dto.StrategyExecutionPlanPageDto;
import my.portfolio.port_view.dto.StrategyMarketBlockReasonDto;
import my.portfolio.port_view.repository.StrategyExecutionQueryRepository;
import my.portfolio.port_view.util.ViewFormatUtils;
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
                    yield "시장 조건이 좋아 공격 운용 가능한 구간임";
                }
                yield "시장 흐름이 우호적이라 적극 진입을 검토하는 구간임";
            }

            case "NEUTRAL" -> {
                if (gt(reason.breadthPressureScore(), "0") && lt(reason.flowPressureScore(), "0")) {
                    yield "일부 지표는 양호하지만 수급 확인이 필요한 선별 진입 구간임";
                }
                yield "시장 조건이 중립이라 좋은 후보만 선별하는 구간임";
            }

            case "DEFENSIVE" -> {
                if (lt(reason.flowPressureScore(), "0")) {
                    yield "수급 압력이 약해 방어 운용이 필요한 구간임";
                }
                yield "시장 리스크가 커져 보수적으로 운용하는 구간임";
            }

            case "BLOCK" -> {
                if (lt(reason.flowPressureScore(), "-0.30")) {
                    yield "시장 수급 압력이 약해서 신규 매수를 차단했음";
                }

                if (lt(reason.marketRegimeScore(), "0")) {
                    yield "시장 종합 점수가 방어 구간이라 신규 매수를 멈췄음";
                }

                yield "전략이 위험 관리 우선 구간으로 판단했음";
            }

            default -> "시장 판단 상태를 확인 중임";
        };
    }

    private String buildMarketBlockSummary(StrategyMarketBlockReasonDto reason) {
        String signal = reason.marketSignal() == null ? "" : reason.marketSignal().toUpperCase();

        return switch (signal) {
            case "AGGRESSIVE" ->
                    "시장 종합 점수와 수급 흐름이 우호적이라 신규 매수 후보를 적극적으로 검토할 수 있는 상태. 다만 종목별 점수와 변동성 조건은 그대로 확인 필요.";

            case "NEUTRAL" ->
                    "시장 방향성이 강하지 않아 모든 종목을 공격적으로 사기보다는, 수급·점수·가격흐름이 좋은 후보만 선별하는 상태.";

            case "DEFENSIVE" ->
                    "시장 리스크가 커지고 있어 신규 매수는 줄이고, 기존 포지션의 손익과 수급 약화를 더 엄격하게 보는 상태.";

            case "BLOCK" ->
                    "지수 자체보다 수급 쪽 약세가 핵심으로 보임. 외국인 수급과 시장 수급 강도가 약해서 전략이 신규 진입보다 현금 방어를 우선한 상태.";

            default ->
                    "시장 판단 데이터가 부족해서 상세 해석은 제한적임.";
        };
    }

    private List<String> buildMarketBlockReasonLines(StrategyMarketBlockReasonDto reason) {
        List<String> lines = new ArrayList<>();

        if (lt(reason.marketRegimeScore(), "0")) {
            lines.add("시장 종합 점수가 " + fmt(reason.marketRegimeScore()) + "로 음수권이라 전체 시장 판단이 방어 쪽으로 기울었음.");
        } else if (reason.marketRegimeScore() != null) {
            lines.add("시장 종합 점수는 " + fmt(reason.marketRegimeScore()) + "로 크게 나쁘진 않지만, 다른 위험 지표와 함께 보수적으로 해석됨.");
        }

        if (lt(reason.flowPressureScore(), "-0.30")) {
            lines.add("수급 압력 점수가 " + fmt(reason.flowPressureScore()) + "로 낮아서 매수세 유입보다 이탈 압력이 더 크게 반영됨.");
        }

        if (lt(reason.marketFlowStrengthScore(), "-0.30")) {
            lines.add("시장 수급 강도도 " + fmt(reason.marketFlowStrengthScore()) + "로 약해서 신규 매수 후보를 만들기 어려운 환경임.");
        }

        if (lt(reason.marketForeignNetRatio5d(), "0")) {
            lines.add("외국인 5일 순매수 비율이 " + pct(reason.marketForeignNetRatio5d()) + "로 음수라 최근 외국인 수급이 약했음.");
        }

        if (reason.marketInstitutionNetRatio5d() != null) {
            if (gt(reason.marketInstitutionNetRatio5d(), "0")) {
                lines.add("기관 5일 수급은 " + pct(reason.marketInstitutionNetRatio5d()) + "로 일부 방어했지만, 외국인/전체 수급 약세를 뒤집기엔 부족했음.");
            } else {
                lines.add("기관 5일 수급도 " + pct(reason.marketInstitutionNetRatio5d()) + "라 수급 방어력이 강하지 않았음.");
            }
        }

        if (reason.breadthPressureScore() != null) {
            if (lt(reason.breadthPressureScore(), "0")) {
                lines.add("시장 폭/확산도 점수도 " + fmt(reason.breadthPressureScore()) + "로 약해서 상승 종목 확산이 부족했음.");
            } else {
                lines.add("시장 폭/확산도는 " + fmt(reason.breadthPressureScore()) + "로 일부 양호했지만, 수급 압력 약세가 더 크게 작용했음.");
            }
        }

        if (reason.featureRiskRegime() != null && !reason.featureRiskRegime().isBlank()) {
            if ("RISK_ON".equalsIgnoreCase(reason.featureRiskRegime())) {
                lines.add("피처 기준 위험 상태는 RISK_ON으로 계산되어 시장 위험 선호는 살아 있지만, 다른 수급/확산 지표와 함께 최종 운용 상태가 결정됨.");
            } else if ("RISK_OFF".equalsIgnoreCase(reason.featureRiskRegime())) {
                lines.add("피처 기준 위험 상태는 RISK_OFF로 계산되어 시장 리스크 관리가 필요한 구간으로 해석됨.");
            } else {
                lines.add("피처 기준 위험 상태는 " + reason.featureRiskRegime() + "로 계산되어 중립적인 시장 환경으로 해석됨.");
            }
        }

        if (lines.isEmpty()) {
            lines.add("세부 시장 피처가 부족해서 정량 사유는 제한적이지만, 실행 계획의 시장 상태에 따라 운용 강도를 조절했음.");
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
