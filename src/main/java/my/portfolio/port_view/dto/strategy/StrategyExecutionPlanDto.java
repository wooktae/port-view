package my.portfolio.port_view.dto.strategy;

import my.portfolio.port_view.util.StrategyExecutionLabelUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.UUID;

public record StrategyExecutionPlanDto(
        Long id,
        LocalDate planDate,
        String strategyName,
        String strategyVersion,
        UUID strategyRunId,
        String marketSignal,
        BigDecimal marketRegimeScore,
        String riskRegime,
        String accountNo,
        String planStatus,
        Integer totalCandidateCount,
        Integer readyOrderCount,
        Integer blockedOrderCount,
        Integer skippedOrderCount,
        BigDecimal totalTargetAmount,
        BigDecimal availableCash,
        BigDecimal maxOrderAmount,
        String blockedReason,
        String memo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public String planStatusLabel() {
        return StrategyExecutionLabelUtils.planStatusLabel(planStatus);
    }

    public String marketSignalLabel() {
        return StrategyExecutionLabelUtils.marketSignalLabel(marketSignal);
    }

    public String riskRegimeLabel() {
        return StrategyExecutionLabelUtils.riskRegimeLabel(riskRegime);
    }
}
