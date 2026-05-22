package my.portfolio.port_view.dto;

import my.portfolio.port_view.util.StrategyExecutionLabelUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record DailyPositionDecisionDto(
        Long id,
        Long dailyRunId,

        String strategyName,
        String strategyVersion,

        LocalDate runDate,
        LocalDate dataDate,
        LocalDate decisionDate,

        Long positionStateId,

        Long accountId,
        String accountNo,

        String tickerCode,
        String stockName,

        String decisionType,
        String decisionStatus,

        String sellReason,
        String holdReason,
        String skipReason,

        Integer holdingDays,

        LocalDate entryDate,
        BigDecimal entryPrice,
        Integer entryQty,
        Integer remainingQty,

        BigDecimal currentPrice,
        Integer currentQty,
        Integer sellableQty,

        BigDecimal expectedPnlAmount,
        BigDecimal expectedPnlRate,

        String marketSignal,
        BigDecimal marketRegimeScore,

        BigDecimal flowScore,
        BigDecimal infoScore,
        BigDecimal tapeScore,
        BigDecimal finalScore,
        BigDecimal shortPressureScore,

        Integer orderQty,
        BigDecimal orderPrice,
        BigDecimal targetAmount,

        String positionContext,
        String sellInfo,
        String validationResult,
        String rawStockFeature,
        String rawMarketFeature,

        String sourceTable,
        String processorVersion,

        Long executionOrderId,

        String errorMessage,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public String decisionTypeLabel() {
        return switch (upper(decisionType)) {
            case "SELL" -> "매도";
            case "HOLD" -> "보유";
            case "SKIP" -> "건너뜀";
            default -> fallback(decisionType);
        };
    }

    public String decisionStatusLabel() {
        return switch (upper(decisionStatus)) {
            case "CREATED" -> "생성됨";
            case "READY" -> "실행 준비";
            case "EXECUTION_CREATED" -> "실행 주문 생성";
            case "SKIPPED" -> "건너뜀";
            case "FAILED" -> "실패";
            case "ERROR" -> "오류";
            default -> fallback(decisionStatus);
        };
    }

    public String marketSignalLabel() {
        return StrategyExecutionLabelUtils.marketSignalLabel(marketSignal);
    }

    public String reasonText() {
        if (sellReason != null && !sellReason.isBlank()) {
            return sellReason;
        }
        if (holdReason != null && !holdReason.isBlank()) {
            return holdReason;
        }
        if (skipReason != null && !skipReason.isBlank()) {
            return skipReason;
        }
        return "-";
    }

    public boolean sell() {
        return "SELL".equalsIgnoreCase(decisionType);
    }

    public boolean hold() {
        return "HOLD".equalsIgnoreCase(decisionType);
    }

    public boolean skip() {
        return "SKIP".equalsIgnoreCase(decisionType);
    }

    private static String upper(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase();
    }

    private static String fallback(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }
}