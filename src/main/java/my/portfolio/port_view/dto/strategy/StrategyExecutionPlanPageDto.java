package my.portfolio.port_view.dto.strategy;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class StrategyExecutionPlanPageDto {

    private List<StrategyExecutionPlanDto> plans = new ArrayList<>();

    private int totalPlanCount;

    private int readyPlanCount;
    private int blockedPlanCount;
    private int partiallyBlockedPlanCount;
    private int noCandidatePlanCount;
    private int failedPlanCount;
    private int otherPlanCount;

    private int totalCandidateCount;
    private int totalReadyOrderCount;
    private int totalBlockedOrderCount;
    private int totalSkippedOrderCount;

    private String latestPlanDateText = "-";
    private String latestCreatedAtText = "-";
    private String latestAvailableCashText = "-";
    private String latestMaxOrderAmountText = "-";
    private String latestPlanStatusText = "-";
    private String latestMarketSignalText = "-";

    private Map<Long, Integer> buyCandidateCountByPlanId = new HashMap<>();
    private Map<Long, Integer> sellCandidateCountByPlanId = new HashMap<>();
    private Map<Long, Integer> readyOrderCountByPlanId = new HashMap<>();

    public boolean isEmpty() {
        return plans == null || plans.isEmpty();
    }

    public int buyCandidateCount(Long planId) {
        if (planId == null) {
            return 0;
        }

        return buyCandidateCountByPlanId.getOrDefault(planId, 0);
    }

    public int sellCandidateCount(Long planId) {
        if (planId == null) {
            return 0;
        }

        return sellCandidateCountByPlanId.getOrDefault(planId, 0);
    }

    public int readyOrderCount(Long planId) {
        if (planId == null) {
            return 0;
        }

        return readyOrderCountByPlanId.getOrDefault(planId, 0);
    }

}