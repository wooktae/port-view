package my.portfolio.port_view.dto;

import my.portfolio.port_view.util.StrategyExecutionLabelUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record DailyRunDto(
        Long id,
        String strategyName,
        String strategyVersion,
        LocalDate runDate,
        LocalDate dataDate,
        String runType,
        String runStatus,
        String marketSignal,
        BigDecimal baseExposure,
        Integer maxPositions,
        Integer candidateCount,
        Integer signalCount,
        String runNote,
        String errorMessage,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String runStatusLabel() {
        return switch (upper(runStatus)) {
            case "CREATED" -> "생성됨";
            case "RUNNING" -> "실행 중";
            case "COMPLETED" -> "완료";
            case "FAILED" -> "실패";
            case "ERROR" -> "오류";
            default -> fallback(runStatus);
        };
    }

    public String marketSignalLabel() {
        return StrategyExecutionLabelUtils.marketSignalLabel(marketSignal);
    }

    public String baseExposureText() {
        return decimalText(baseExposure, 6);
    }

    public String startedAtText() {
        return dateTimeText(startedAt);
    }

    public String finishedAtText() {
        return dateTimeText(finishedAt);
    }

    public String createdAtText() {
        return dateTimeText(createdAt);
    }

    public String updatedAtText() {
        return dateTimeText(updatedAt);
    }

    public boolean completed() {
        return "COMPLETED".equalsIgnoreCase(runStatus);
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

        return value.setScale(scale, java.math.RoundingMode.HALF_UP).toPlainString();
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