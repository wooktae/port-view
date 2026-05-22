package my.portfolio.port_view.dto;

import my.portfolio.port_view.util.StrategyExecutionLabelUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;

public record StrategyExecutionOrderDto(
        Long id,
        Long executionPlanId,
        Long strategySignalId,

        Long sourceDailyRunId,
        Long sourceDailySignalId,
        Long sourceDailyPositionDecisionId,
        String sourceType,

        LocalDate signalDate,
        String tickerCode,
        String stockName,
        String actionType,
        String signalType,
        BigDecimal signalScore,
        BigDecimal signalPositionSize,
        String marketSignal,
        BigDecimal marketRegimeScore,
        BigDecimal flowScore,
        BigDecimal infoScore,
        BigDecimal tapeScore,
        BigDecimal finalScore,
        BigDecimal shortPressureScore,
        BigDecimal targetAmount,
        Integer currentQty,
        BigDecimal currentEvalAmount,
        Integer orderQty,
        BigDecimal orderPrice,
        String orderMethod,
        String executionStatus,
        String blockReason,
        Long connectorOrderRequestId,
        String validationResult,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public String actionTypeLabel() {
        return StrategyExecutionLabelUtils.actionTypeLabel(actionType);
    }

    public String signalTypeLabel() {
        return StrategyExecutionLabelUtils.signalTypeLabel(signalType);
    }

    public String sourceTypeLabel() {
        return StrategyExecutionLabelUtils.sourceTypeLabel(sourceType);
    }

    public String marketSignalLabel() {
        return StrategyExecutionLabelUtils.marketSignalLabel(marketSignal);
    }

    public String orderMethodLabel() {
        return StrategyExecutionLabelUtils.orderMethodLabel(orderMethod);
    }

    public String executionStatusLabel() {
        return StrategyExecutionLabelUtils.executionStatusLabel(executionStatus);
    }

    public String blockReasonLabel() {
        return StrategyExecutionLabelUtils.blockReasonLabel(blockReason);
    }

    //public String executionModeLabel() {
    //    return "-";
    //}

    public String connectorStatusLabel() {
        return connectorOrderRequestId == null ? "미전송" : "커넥터 연결";
    }

    public boolean hasDailySignalSource() {
        return sourceDailySignalId != null;
    }

    public boolean hasDailyRunSource() {
        return sourceDailyRunId != null;
    }
}