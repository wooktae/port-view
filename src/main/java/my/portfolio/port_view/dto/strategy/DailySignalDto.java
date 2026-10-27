package my.portfolio.port_view.dto.strategy;

import my.portfolio.port_view.util.StrategyExecutionLabelUtils;
import my.portfolio.port_view.util.ViewMessages;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record DailySignalDto(
        Long id,
        Long dailyRunId,

        String strategyName,
        String strategyVersion,

        LocalDate runDate,
        LocalDate dataDate,
        LocalDate signalDate,

        String tickerCode,
        String companyName,

        String signalType,
        String signalStatus,

        Integer rankNo,

        String marketSignal,
        BigDecimal baseExposure,

        BigDecimal finalScore,
        BigDecimal flowScore,
        BigDecimal tapeScore,
        BigDecimal infoScore,
        BigDecimal shortScore,
        BigDecimal volatility20d,
        BigDecimal intradayRange,

        BigDecimal positionSize,
        BigDecimal targetAmount,
        Integer targetQty,

        String entryReason,
        String blockReason,

        String buyInfo,
        String rawFeatures,

        String sourceTable,
        String processorVersion,

        Long executionPlanId,
        Long executionOrderId,
        String executionStatus,
        Integer executionOrderQty,
        BigDecimal executionOrderPrice,
        BigDecimal executionTargetAmount,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String signalTypeLabel() {
        return StrategyExecutionLabelUtils.signalTypeLabel(signalType);
    }

    public String signalStatusLabel() {
        return switch (upper(signalStatus)) {
            case "READY" -> ViewMessages.text("status.readyComplete");
            case "BLOCKED" -> ViewMessages.text("status.blocked");
            case "SKIPPED" -> ViewMessages.text("status.skipped");
            case "USED" -> ViewMessages.text("status.used");
            case "FAILED" -> ViewMessages.text("status.failed");
            default -> fallback(signalStatus);
        };
    }

    public String executionStatusLabel() {
        return StrategyExecutionLabelUtils.executionStatusLabel(executionStatus);
    }

    public String marketSignalLabel() {
        return StrategyExecutionLabelUtils.marketSignalLabel(marketSignal);
    }

    public String baseExposureText() {
        return decimalText(baseExposure, 6);
    }

    public String finalScoreText() {
        return decimalText(finalScore, 8);
    }

    public String flowScoreText() {
        return decimalText(flowScore, 8);
    }

    public String tapeScoreText() {
        return decimalText(tapeScore, 8);
    }

    public String infoScoreText() {
        return decimalText(infoScore, 8);
    }

    public String shortScoreText() {
        return decimalText(shortScore, 8);
    }

    public String positionSizeText() {
        return decimalText(positionSize, 8);
    }

    public String volatility20dText() {
        return decimalText(volatility20d, 8);
    }

    public String intradayRangeText() {
        return decimalText(intradayRange, 8);
    }

    public String createdAtText() {
        return dateTimeText(createdAt);
    }

    public String updatedAtText() {
        return dateTimeText(updatedAt);
    }

    public boolean ready() {
        return "READY".equalsIgnoreCase(signalStatus);
    }

    public boolean hasExecutionOrder() {
        return executionOrderId != null;
    }

    private static String dateTimeText(OffsetDateTime value) {
        if (value == null) {
            return "-";
        }

        return value.atZoneSameInstant(KST).format(DATE_TIME_FORMATTER);
    }

    private static String decimalText(BigDecimal value, int scale) {
        if (value == null) {
            return "-";
        }

        return value.setScale(scale, RoundingMode.HALF_UP).toPlainString();
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